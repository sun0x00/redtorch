package xyz.redtorch.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.joda.time.DateTime;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import xyz.redtorch.core.entity.Tick;
import xyz.redtorch.core.service.extend.event.EventConstant;

public class SerializationPerformanceTest {
	public static void main(String[] args) {
		Tick tick = new Tick();
		long startT = System.nanoTime();
		String tickStr = JSON.toJSONString(tick);
		System.out.println(System.nanoTime()-startT);
		startT = System.nanoTime();
		JSON.parseObject(tickStr, Tick.class);
		System.out.println(System.nanoTime()-startT);
		
		System.out.println(EventConstant.EVENT_ORDER+"123");
		
		startT = System.nanoTime();
		Kryo kryo = new Kryo();
		kryo.register(Tick.class);
		kryo.register(DateTime.class);
		startT = System.nanoTime();
		for(int i =0;i<10000;i++) {
	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	        Output output = new Output(byteArrayOutputStream);
	        kryo.writeObject(output, tick);
	        output.flush();
			byte[] bytes2 = byteArrayOutputStream.toByteArray();
			//System.out.println(System.nanoTime()-startT);
			//startT = System.nanoTime();
			
	        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes2);
	        Input input = new Input(byteArrayInputStream);
			kryo.readObject(input, Tick.class);
			//System.out.println(System.nanoTime()-startT);
			if(i%100==0) {
				System.out.println((System.nanoTime()-startT)/(i+1));
			}
		}
		System.out.println((System.nanoTime()-startT)/10000);
//		134,015,182
//		12,715,540
	}
}
