package xyz.redtorch.trader.engine.event;


public class FastEventMainTest
{
    public static void main(String[] args) throws Exception
    {
    	
    	FastEventProducerTest FastEventProducerTest1 = new FastEventProducerTest(FastEventEngine.getRingBuffer());
    	
    	FastEventDynamicHandler fedh1 = new FastEventDynamicHandlerTest("1");
    	FastEventEngine.addHandler(fedh1);
    	FastEventProducerTest1.onData("A==========="); //A
    	FastEventDynamicHandler fedh2 = new FastEventDynamicHandlerTest("2");
    	FastEventEngine.addHandler(fedh2);
    	FastEventProducerTest1.onData("B==========="); //BB
    	FastEventDynamicHandler fedh3 = new FastEventDynamicHandlerTest("3");
    	FastEventEngine.addHandler(fedh3);
    	FastEventProducerTest1.onData("C===========");//CCC
    	FastEventEngine.removeHandler(fedh1);
    	FastEventProducerTest1.onData("D===========");//DD
    	FastEventEngine.removeHandler(fedh3);
    	FastEventProducerTest1.onData("E===========");
    	FastEventEngine.removeHandler(fedh2);
    	FastEventProducerTest1.onData("===========");
    	
    	while(true) {
        	FastEventProducerTest1.onData("===========");
    		Thread.sleep(1000);
    	}
    	
    }
}
