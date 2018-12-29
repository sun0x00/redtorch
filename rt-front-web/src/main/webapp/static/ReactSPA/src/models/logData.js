import { queryLogDatas } from '@/services/coreApi';

export default {
  namespace: 'logData',

  state: {
    logDatas: [],
    dataLength: 200
  },

  effects: {
    *fetchLogDatas({ payload }, { call, put }) {
      const response = yield call(queryLogDatas,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'saveLogDatas',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : [],
      });
    },
    *updateLogDatas({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'updateStateLogDatas',
        payload: tmpPayload
      });
    }
    
  },

  reducers: {
    saveLogDatas(state, action) {
    
      let newLogDatas = action.payload
      if(newLogDatas.length>state.dataLength){
        newLogDatas = newLogDatas.sort((a,b)=>a.timestamp-b.timestamp).splice(-state.dataLength,state.dataLength);
      }

      return {
        ...state,
        logDatas: newLogDatas,
      };
    },
    updateStateLogDatas(state,action){
      let newLogDatas = state.logDatas.concat(action.payload)

      if(newLogDatas.length>state.dataLength){
        newLogDatas = newLogDatas.sort((a,b)=>a.timestamp-b.timestamp).splice(-state.dataLength,state.dataLength);
      }

      return {
        ...state,
        logDatas: newLogDatas,
      };
    }
  },
}