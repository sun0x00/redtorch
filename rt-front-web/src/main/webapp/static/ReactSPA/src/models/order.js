import {STATUS_WORKING} from '../utils/RtConstant'
import { queryOrders } from '@/services/coreApi';

export default {
  namespace: 'order',

  state: {
    orders: [],
    orderMap: new Map(),
    workingOrders:[],
  },

  effects: {
    *fetchOrders({ payload }, { call, put }) {
      const response = yield call(queryOrders,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'saveOrders',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : []
      });


      let working = []
      if(!response||!response.data||!Array.isArray(response.data) ){
        working = []
      }else{
        response.data.forEach(element => {
          if(STATUS_WORKING.has(element.status)){
            working.push(element)
          }
        });
      }
 
      yield put({
        type: 'saveWorkingOrders',
        payload: working
      });
    },
    *updateOrders({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'updateStateOrders',
        payload: tmpPayload
      });
    }
    
  },

  reducers: {
    saveOrders(state, action) {
      const newOrderMap = new Map()
      const newWorkingOrders = []
      action.payload.forEach(element => {
        newOrderMap.set(element.rtOrderID, element);
        if(STATUS_WORKING.has(element.status)){
          newWorkingOrders.push(element)
        }
      });
    
      return {
        ...state,
        orders: action.payload,
        orderMap: newOrderMap,
        workingOrders:newWorkingOrders
      };
    },
    updateStateOrders(state,action){
      action.payload.forEach(element => {
        state.orderMap.set(element.rtOrderID, element);
      });

      const newOrders =  Array.from(state.orderMap.values());
      const newWorkingOrders = []
      newOrders.forEach(element => {
        if(STATUS_WORKING.has(element.status)){
          newWorkingOrders.push(element)
        }
      });

      return {
        ...state,
        orders: newOrders,
        orderMap: state.orderMap,
        workingOrders: newWorkingOrders,
      };
    }
  },
}