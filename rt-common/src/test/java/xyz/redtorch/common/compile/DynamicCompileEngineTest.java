package xyz.redtorch.common.compile;

import java.util.HashMap;
import java.util.Map;

import xyz.redtorch.common.service.RpcClientProcessService;

public class DynamicCompileEngineTest
{
	public static void main(String[] args) {
		 
        String  code = "package xyz.redtorch.common.service;\n" + 
        		"\n" + 
        		"import com.google.protobuf.ByteString;\n" + 
        		"import xyz.redtorch.pb.CoreRpc.RpcId;\n" + 
        		"\n" + 
        		"public class RpcClientProcessServiceImplTest implements RpcClientProcessService {\n" + 
        		"\n" + 
        		"	private int V_NUMBER = V_NUMBER_I;\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public void processData(byte[] data) {\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public boolean sendRoutineCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {\n" + 
        		"		return false;\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public boolean sendLz4CoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {\n" + 
        		"		return false;\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public void onWsClosed() {\n" + 
        		"		// TODO Auto-generated method stub\n" + 
        		"\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public void onWsError() {\n" + 
        		"		// TODO Auto-generated method stub\n" + 
        		"\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public void onWsConnected() {\n" + 
        		"		// TODO Auto-generated method stub\n" + 
        		"\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public void onHeartbeat(String result) {\n" + 
        		"		// TODO Auto-generated method stub\n" + 
        		"		\n" + 
        		"	}\n" + 
        		"\n" + 
        		"	@Override\n" + 
        		"	public boolean sendCoreRpc(int targetNodeId, ByteString content, String reqId, RpcId rpcId) {\n" + 
        		"		System.out.println(\"RpcClientProcessServiceImplTest targetNodeId:\" + targetNodeId);\n" + 
        		"		System.out.println(\"RpcClientProcessServiceImplTest V_NUMBER:\" + V_NUMBER);\n" + 
        		"		return false;\n" + 
        		"	}\n" + 
        		"}\n" + 
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
                rcps.sendCoreRpc(3232, null, "", null);
                
            }catch (Exception e) {
                e.printStackTrace();
            }
            System.gc();
            long time2 = System.currentTimeMillis();
            System.out.println("次数："+i+"            time:"+(time2-time1));
        }
	}
}

