import { queryContracts } from '@/services/coreApi';

export default {
  namespace: 'contract',

  state: {
    contracts: [],
    contractMap: new Map(),
    mixContractMap: new Map()
  },

  effects: {
    *fetchContracts({ payload }, { call, put }) {
      const response = yield call(queryContracts,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'saveContracts',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : [],
      });
    },
    *updateContracts({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'updateStateContracts',
        payload: tmpPayload
      });
    }
    
  },

  reducers: {
    saveContracts(state, action) {
      const newContractMap = new Map()
      const newMixContractMap = new Map()
      action.payload.forEach(element => {
        newContractMap.set(element.rtContractID, element);
        newMixContractMap.set(element.rtContractID, element);
        newMixContractMap.set(element.rtSymbol, element);
        newMixContractMap.set(element.symbol, element);
        newMixContractMap.set(`${element.symbol}.${element.gatewayID}`, element);
      });
    
      return {
        ...state,
        contracts: action.payload,
        contractMap: newContractMap,
        mixContractMap:newMixContractMap,
      };
    },
    updateStateContracts(state,action){
      action.payload.forEach(element => {
        state.contractMap.set(element.rtContractID, element);
        state.mixContractMap.set(element.rtSymbol, element);
        state.mixContractMap.set(element.rtContractID, element);
        state.mixContractMap.set(element.symbol, element);
        state.mixContractMap.set(`${element.symbol}.${element.gatewayID}`, element);
      });

      return {
        ...state,
        contracts:  Array.from(state.contractMap.values()),
        contractMap: state.contractMap,
        mixContractMap: state.mixContractMap
      };
    }
  },
}