import { queryTrades } from '@/services/coreApi';

export default {
  namespace: 'trade',

  state: {
    trades: [],
    tradeMap: new Map()
  },

  effects: {
    *fetchTrades({ payload }, { call, put }) {
      const response = yield call(queryTrades,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'saveTrades',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : [],
      });
    },
    *updateTrades({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'updateStateTrades',
        payload: tmpPayload
      });
    }
    
  },

  reducers: {
    saveTrades(state, action) {
      const newTradeMap = new Map()
      action.payload.forEach(element => {
        newTradeMap.set(element.rtTradeID, element);
      });
    
      return {
        ...state,
        trades: action.payload,
        tradeMap: newTradeMap,
      };
    },
    updateStateTrades(state,action){
      action.payload.forEach(element => {
        state.tradeMap.set(element.rtTradeID, element);
      });

      return {
        ...state,
        trades:  Array.from(state.tradeMap.values()),
        tradeMap: state.tradeMap,
      };
    }
  },
}