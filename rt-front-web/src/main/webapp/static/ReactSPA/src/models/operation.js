import { reqSubscribe,reqSendOrder,reqCancelAllOrders,reqCancelOrder,reqUnsubscribe } from '@/services/coreApi';

export default {
  namespace: 'operation',
  state: {
  },

  effects: {
    *subscribe({ payload }, { call }){
      yield call(reqSubscribe,{...payload,token:sessionStorage.getItem('token')});
    },
    *unsubscribe({ payload }, { call }){
        yield call(reqUnsubscribe,{...payload,token:sessionStorage.getItem('token')});
      },
    *sendOrder({ payload }, { call }){
        yield call(reqSendOrder,{...payload,token:sessionStorage.getItem('token')});
    },
    *cancelOrder({ payload }, { call }){
        yield call(reqCancelOrder,{...payload,token:sessionStorage.getItem('token')});
    },
    *cancelAllOrders({ payload }, { call }){
        yield call(reqCancelAllOrders,{...payload,token:sessionStorage.getItem('token')});
    }
  },
  reducers: {
  },
};
