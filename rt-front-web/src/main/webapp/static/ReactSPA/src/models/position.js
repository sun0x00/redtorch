import { queryPositions } from '@/services/coreApi';

export default {
  namespace: 'position',

  state: {
    positions: [],
    positionMap: new Map()
  },

  effects: {
    *fetchPositions({ payload }, { call, put }) {
      const response = yield call(queryPositions,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'savePositions',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : [],
      });
    },
    *updatePositions({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'updateStatePositions',
        payload: tmpPayload
      });
    }
    
  },

  reducers: {
    savePositions(state, action) {
      const newPositionMap = new Map()
      action.payload.forEach(element => {
        newPositionMap.set(element.rtPositionID, element);
      });
    
      return {
        ...state,
        positions: action.payload,
        positionMap: newPositionMap,
      };
    },
    updateStatePositions(state,action){
      action.payload.forEach(element => {
        state.positionMap.set(element.rtPositionID, element);
      });

      return {
        ...state,
        positions:  Array.from(state.positionMap.values()),
        positionMap: state.positionMap,
      };
    }
  },
}