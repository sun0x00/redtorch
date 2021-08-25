package xyz.redtorch.common.util.rpc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.protobuf.ByteString;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import xyz.redtorch.common.constant.CommonConstant;
import xyz.redtorch.common.web.vo.RequestVo;
import xyz.redtorch.common.web.vo.ResponseVo;
import xyz.redtorch.pb.CoreField;
import xyz.redtorch.pb.CoreField.CommonRspField;
import xyz.redtorch.pb.CoreRpc.RpcId;
import xyz.redtorch.pb.Dep.DataExchangeProtocol;
import xyz.redtorch.pb.Dep.DataExchangeProtocol.ContentType;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.concurrent.ExecutorService;

public class RpcUtils {

    private static final Logger logger = LoggerFactory.getLogger(RpcUtils.class);

    public static byte[] generateRpcDep(RpcId rpcId, String transactionId, ByteString content) {
        if (content.size() > 262144) {
            return generateLz4RpcDep(rpcId, transactionId, content);
        } else {
            return generateRoutineRpcDep(rpcId, content);
        }
    }

    public static byte[] generateRoutineRpcDep(RpcId rpcId, ByteString content) {

        DataExchangeProtocol.Builder depBuilder = DataExchangeProtocol.newBuilder() //
                .setContentType(ContentType.ROUTINE) //
                .setRpcId(rpcId.getNumber()) //
                .setContentBytes(content) //
                .setTimestamp(System.currentTimeMillis());

        return depBuilder.build().toByteArray();
    }

    public static byte[] generateLz4RpcDep(RpcId rpcId, String transactionId, ByteString content) {

        ByteString contentByteString = ByteString.EMPTY;
        long beginTime = System.currentTimeMillis();
        try (InputStream in = new ByteArrayInputStream(content.toByteArray()); ByteArrayOutputStream bOut = new ByteArrayOutputStream(); LZ4FrameOutputStream lzOut = new LZ4FrameOutputStream(bOut)) {
            final byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                lzOut.write(buffer, 0, n);
            }
            lzOut.close();
            in.close();
            contentByteString = ByteString.copyFrom(bOut.toByteArray());
            if (contentByteString.size() > 1048576) {
                logger.info("生成DEP数据, RPC:{}, 业务ID:{}, 压缩耗时{}ms,原始数据大小{},压缩后数据大小{},压缩率{}", rpcId.getValueDescriptor().getName(), transactionId, System.currentTimeMillis() - beginTime,
                        content.size(), contentByteString.size(), contentByteString.size() / (double) content.size());
            }
        } catch (Exception e) {
            logger.error("生成DEP数据错误, 压缩异常, RPC:{}, 业务ID:{}", rpcId.getValueDescriptor().getName(), transactionId, e);
            throw new RuntimeException("生成DEP数据错误", e);
        }

        DataExchangeProtocol.Builder depBuilder = DataExchangeProtocol.newBuilder() //
                .setContentType(ContentType.COMPRESSED_LZ4) //
                .setRpcId(rpcId.getNumber()) //
                .setContentBytes(contentByteString) //
                .setTimestamp(System.currentTimeMillis());

        return depBuilder.build().toByteArray();
    }

    // 这个方法一般用于发送同步请求Req
    public static DataExchangeProtocol sendSyncHttpRpc(RestTemplate restTemplate, URI uri, String authToken, RpcId rpcId, String transactionId, ByteString content) {

        try {
            HttpEntity<String> requestEntity = generateHttpEntity(authToken, rpcId, transactionId, content);

            ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                JSONObject resultJSONObject = JSON.parseObject(responseEntity.getBody());
                if (resultJSONObject.getBooleanValue("status")) {
                    ResponseVo<String> ret = JSON.parseObject(responseEntity.getBody(), new TypeReference<ResponseVo<String>>() {
                    });
                    if (ret == null) {
                        logger.error("HTTP RPC错误,解析JSON数据错误,RPC:{},业务ID:{}", rpcId, transactionId);
                    } else if (ret.isStatus()) {
                        String base64Data = ret.getVoData();
                        if (logger.isDebugEnabled()) {
                            logger.debug("HTTP RPC,RPC{},业务ID:{}接收到的Base64Data:{}", rpcId, transactionId, base64Data);
                        }
                        if (base64Data != null) {
                            byte[] data = Base64.getDecoder().decode(base64Data);
                            return DataExchangeProtocol.parseFrom(data);
                        }
                    } else {
                        logger.error("HTTP RPC返回200,但状态回报错误,RPC:{},业务ID{},信息:{}", rpcId, transactionId, ret.getMessage());
                    }
                } else {
                    logger.error("HTTP RPC状态非200,RPC:{},业务ID{},状态码为:{}", rpcId, transactionId, responseEntity.getStatusCode().value());
                }
            }
        } catch (Exception e) {
            logger.error("HTTP RPC错误,RPC:{},业务ID{}", rpcId, transactionId, e);
        }

        return null;
    }

    // 这个方法一般用于异步发送Rsp回报
    public static void sendAsyncHttpRpc(ExecutorService executor, RestTemplate restTemplate, URI uri, String authToken, RpcId rpcId, String transactionId, ByteString content) {

        executor.execute(() -> {
            try {
                HttpEntity<String> requestEntity = generateHttpEntity(authToken, rpcId, transactionId, content);

                ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    JSONObject resultJSONObject = JSON.parseObject(responseEntity.getBody());
                    if (resultJSONObject.getBooleanValue("status")) {
                        ResponseVo<String> ret = JSON.parseObject(responseEntity.getBody(), new TypeReference<ResponseVo<String>>() {
                        });
                        if (ret == null) {
                            logger.error("HTTP RPC错误,解析JSON数据错误,RPC:{},业务ID:{}", rpcId, transactionId);
                        } else if (!ret.isStatus()) {
                            logger.error("HTTP RPC返回200,但状态回报错误,RPC:{},业务ID:{},信息:{}", rpcId, transactionId, ret.getMessage());
                        }
                    } else {
                        logger.error("HTTP RPC状态非200,RPC:{},业务ID:{},状态码为:{}", rpcId, transactionId, responseEntity.getStatusCode().value());
                    }
                }
            } catch (Exception e) {
                logger.error("HTTP RPC错误,RPC:{},业务ID:{}", rpcId, transactionId, e);
            }
        });

    }

    public static HttpEntity<String> generateHttpEntity(String authToken, RpcId rpcId, String transactionId, ByteString content) {
        byte[] reqData = generateRpcDep(rpcId, transactionId, content);

        RequestVo<String> requestVo = new RequestVo<>();
        requestVo.setVoData(Base64.getEncoder().encodeToString(reqData));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", "application/json");
        requestHeaders.add("Accept", "*/*");
        requestHeaders.add(CommonConstant.KEY_AUTH_TOKEN, authToken);

        return new HttpEntity<>(JSON.toJSONString(requestVo), requestHeaders);
    }

    public static ByteString uncompressLz4(ByteString inContent) {
        ByteString outContent = null;
        try (InputStream in = new ByteArrayInputStream(inContent.toByteArray());
             BufferedInputStream bin = new BufferedInputStream(in);
             LZ4FrameInputStream zIn = new LZ4FrameInputStream(bin)) {
            outContent = ByteString.readFrom(zIn);
        } catch (Exception e) {
            logger.error("解压错误", e);
        }
        return outContent;
    }

    public static ByteString processByteString(ContentType contentType, ByteString inContent, int rpcId, long timestamp) {
        String contentTypeValueName = contentType.getValueDescriptor().getName();
        ByteString outContent;
        if (contentType == ContentType.COMPRESSED_LZ4) {
            outContent = uncompressLz4(inContent);
            if (outContent == null) {
                logger.error("处理DEP错误,内容类型:{},RPC ID:{},时间戳:{},无法使用LZ4正确解析报文内容", contentTypeValueName, rpcId, timestamp);
                return null;
            }
        } else if (contentType == ContentType.ROUTINE) {
            outContent = inContent;
        } else {
            logger.error("处理DEP错误,内容类型:{},RPC ID:{},时间戳:{},不支持的报文类型", contentTypeValueName, rpcId, timestamp);
            return null;
        }

        if (outContent.size() <= 0) {
            logger.error("处理DEP错误,内容类型:{},RPC ID:{},时间戳:{},报文内容长度错误", contentTypeValueName, rpcId, timestamp);
            return null;
        }
        return outContent;
    }

    public static void checkCommonRsp(CommonRspField commonRsp) {
        if (commonRsp == null) {
            logger.error("参数commonRsp缺失");
            throw new IllegalArgumentException("参数commonRsp缺失");
        }

        if (StringUtils.isBlank(commonRsp.getTransactionId())) {
            logger.error("参数transactionId缺失");
            throw new IllegalArgumentException("参数transactionId缺失");
        }
    }

    public static void checkCommonReq(CoreField.CommonReqField commonReq) {
        if (commonReq == null) {
            logger.error("参数commonReq缺失");
            throw new IllegalArgumentException("参数commonReq缺失");
        }

        if (StringUtils.isBlank(commonReq.getTransactionId())) {
            logger.error("参数TransactionId缺失");
            throw new IllegalArgumentException("参数reqId缺失");
        }

        if (StringUtils.isBlank(commonReq.getOperatorId())) {
            logger.error("参数operatorId缺失");
            throw new IllegalArgumentException("参数operatorId缺失");
        }
    }

}
