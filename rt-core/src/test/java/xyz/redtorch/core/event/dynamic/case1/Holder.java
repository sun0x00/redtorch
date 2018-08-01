package xyz.redtorch.core.event.dynamic.case1;

import com.lmax.disruptor.EventFactory;

class Holder {

    private String value;

    public static final EventFactory<Holder> EVENT_FACTORY = new EventFactory<Holder>() {
        @Override
        public Holder newInstance() {
            return new Holder();
        }
    };

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}