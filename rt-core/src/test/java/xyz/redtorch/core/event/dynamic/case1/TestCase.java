package xyz.redtorch.core.event.dynamic.case1;


import com.lmax.disruptor.EventHandler;

public class TestCase {

    private static final MyDisruptor disruptor = new MyDisruptor();

    private static class Handler implements EventHandler<Holder> {

        private int count = 0;

        @Override
        public void onEvent(Holder holder, long l, boolean b) throws Exception {
        	System.out.println(holder.getValue());
            count++;
            if (count == 5000 && !holder.getValue().isEmpty()) {
                disruptor.removeHandler(this);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 100000; i++) {
            for (int j = 0; j < 4; j++) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disruptor.addHandler(new Handler());
                    }
                }).start();
            }

            while (disruptor.hasHandlers()) {
                disruptor.publishValue(String.valueOf(System.currentTimeMillis()));
            }
        }
    }
}