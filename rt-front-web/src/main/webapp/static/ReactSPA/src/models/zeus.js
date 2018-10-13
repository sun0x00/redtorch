import { querySrategyInfos,reqChangeStrategyStatus } from '@/services/coreApi';

export default {
  namespace: 'zeus',

  state: {
    strategyInfos: [],
  },

  effects: {
    *fetchSrategyInfos({ payload }, { call, put }) {
      const response = yield call(querySrategyInfos,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'saveSrategyInfos',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : [],
      });
    },
    *changeStrategyStatus({ payload }, { call}){
      yield call(reqChangeStrategyStatus,{...payload,token:sessionStorage.getItem('token')});
    }
    
  },

  reducers: {
    saveSrategyInfos(state, action) {
      return {
        ...state,
        strategyInfos: action.payload,
      };
    },
  },
}