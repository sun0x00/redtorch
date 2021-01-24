import java.time.LocalDateTime;

import xyz.redtorch.common.util.CommonUtils;

public class LDTP_TEST {
	public static void main(String[] args) {
		long start = System.nanoTime();
		for (int i = 0; i < 1000000; i++) {
			LocalDateTime ldt = CommonUtils.millsToLocalDateTime(1558207248000L);
			if(i%10000 == 0) {
				System.out.println("耗时"+(System.nanoTime()-start)/(1000f*10000)); 
				start = System.nanoTime();
			}
		}
	}
}	
