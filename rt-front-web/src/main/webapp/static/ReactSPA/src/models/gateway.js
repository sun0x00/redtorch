import { queryGateways,reqChangeGatewayConnectStatus ,reqPutGateway,reqDeleteGateway} from '@/services/coreApi';

export default {
  namespace: 'gateway',

  state: {
    gateways: [],
  },

  effects: {
    *fetchGateways({ payload }, { call, put }) {
      const response = yield call(queryGateways,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'saveStateGateways',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : [],
      });
    },
    *changeGatewayConnectStatus({ payload }, { call }){
      yield call(reqChangeGatewayConnectStatus,{...payload,token:sessionStorage.getItem('token')});
    },
    *savaOrUpdateGateway({ payload }, { call }){
      yield call(reqPutGateway,{...payload,token:sessionStorage.getItem('token')});
    },
    *deleteGateway({ payload }, { call }){
      yield call(reqDeleteGateway,{...payload,token:sessionStorage.getItem('token')});
    }
    
  },

  reducers: {
    saveStateGateways(state, action) {
      return {
        ...state,
        gateways: action.payload,
      };
    },
  },
};
