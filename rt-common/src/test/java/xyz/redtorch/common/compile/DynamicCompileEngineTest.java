package xyz.redtorch.common.compile;

import java.util.HashMap;
import java.util.Map;

import xyz.redtorch.common.service.RpcClientProcessService;

public class DynamicCompileEngineTest
{
	public static void main(String[] args) {
		 
        String  code = "package xyz.redtorch.common.service;\r\n" + 
        		"import com.google.protobuf.ByteString;\r\n" + 
        		"import xyz.redtorch.pb.CoreRpcPb.RpcId;\r\n" + 
        		"public class RpcClientProcessServiceImplTest implements RpcClientProcessService {\r\n" + 
        		"	\r\n" + 
        		"	private int V_NUMBER = V_NUMBER_I;\r\n" + 
        		"	\r\n" + 
        		"	@Override\r\n" + 
        		"	public void processData(byte[] data) {\r\n" + 
        		"	}\r\n" + 
        		"	@Override\r\n" + 
        		"	public boolean sendRoutineCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {\r\n" + 
        		"		System.out.println(\"RpcClientProcessServiceImplTest targetNodeId:\"+targetNodeId);\r\n" + 
        		"		System.out.println(\"RpcClientProcessServiceImplTest V_NUMBER:\"+V_NUMBER);\r\n" + 
        		"		return false;\r\n" + 
        		"	}\r\n" + 
        		"	@Override\r\n" + 
        		"	public boolean sendLz4CoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {\r\n" + 
        		"		return false;\r\n" + 
        		"	}\r\n" + 
        		"}\r\n" + 
        		"";
        String name = "xyz.redtorch.common.service.RpcClientProcessServiceImplTest";
 
        for(int i=0;i<10000;i++){
            long time1 = System.currentTimeMillis();
            DynamicCompileEngine dynamicCompileEngine = DynamicCompileEngine.getInstance();
            try {
                Class<?> clazz = dynamicCompileEngine.javaCodeToClass(name,code.replaceAll("V_NUMBER_I",""+i));
                RpcClientProcessService rcps = (RpcClientProcessService)clazz.getDeclaredConstructor().newInstance();
                Map<String,Object> param = new HashMap<>();
                param.put("key",i);
                rcps.sendRoutineCoreRpc(3232, null, "", null);
                
            }catch (Exception e) {
                e.printStackTrace();
            }
            System.gc();
            long time2 = System.currentTimeMillis();
            System.out.println("次数："+i+"            time:"+(time2-time1));
        }
	}
}

