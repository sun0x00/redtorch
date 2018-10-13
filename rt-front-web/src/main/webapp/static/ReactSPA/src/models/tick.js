import { queryTicks,reqSubscribe } from '@/services/coreApi';

export default {
  namespace: 'tick',

  state: {
    ticks: [],
    tickMap: new Map()
  },

  effects: {
    *fetchTicks({ payload }, { call, put }) {
      const response = yield call(queryTicks,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'saveTicks',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : [],
      });
    },
    *reqSubscribe({ payload }, { call }){
      yield call(reqSubscribe,{...payload,token:sessionStorage.getItem('token')});
    },
    *updateTicks({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'updateStateTicks',
        payload: tmpPayload
      });
    }
    
  },

  reducers: {
    saveTicks(state, action) {
      const newTickMap = new Map()

      action.payload.forEach(element => {
        newTickMap.set(element.rtTickID, element);
      });
    
      return {
        ...state,
        ticks: action.payload,
        tickMap: newTickMap,
      };
    },
    updateStateTicks(state,action){
      action.payload.forEach(element => {
        state.tickMap.set(element.rtTickID, element);
      });

      return {
        ...state,
        ticks: Array.from(state.tickMap.values()),
        tickMap: state.tickMap,
      };
    }
  },
};
