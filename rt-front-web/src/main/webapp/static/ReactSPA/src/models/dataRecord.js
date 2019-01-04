import { querySubscribeReqs,reqPutSubscribeReq,reqDeleteSubscribeReq} from '@/services/coreApi';

export default {
  namespace: 'dataRecord',

  state: {
    subscribeReqs: [],
  },

  effects: {
    *fetchSubscribeReqs({ payload }, { call, put }) {
      const response = yield call(querySubscribeReqs,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'saveStateSubscribeReqs',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : [],
      });
    },
    *savaOrUpdateSubscribeReq({ payload }, { call }){
      yield call(reqPutSubscribeReq,{...payload,token:sessionStorage.getItem('token')});
    },
    *deleteSubscribeReq({ payload }, { call }){
      yield call(reqDeleteSubscribeReq,{...payload,token:sessionStorage.getItem('token')});
    }
    
  },

  reducers: {
    saveStateSubscribeReqs(state, action) {
      return {
        ...state,
        subscribeReqs: action.payload,
      };
    },
  },
};
