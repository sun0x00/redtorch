import { queryAccounts } from '@/services/coreApi';

export default {
  namespace: 'account',

  state: {
    accounts: [],
    accountMap: new Map()
  },

  effects: {
    *fetchAccounts({ payload }, { call, put }) {
      const response = yield call(queryAccounts,{...payload,token:sessionStorage.getItem('token')});

      yield put({
        type: 'saveAccounts',
        payload: response&&response.data&&Array.isArray(response.data)?response.data:[],
      });
    },
    *updateAccounts({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'updateStateAccounts',
        payload: tmpPayload
      });
    }
  },

  reducers: {
    saveAccounts(state, action) {
      
      const newAccountMap = new Map()

      action.payload.forEach(element => {
        newAccountMap.set(element.rtAccountID, element);
      });
    
      return {
        ...state,
        accounts: action.payload,
        accountMap: newAccountMap,
      };
    },
    updateStateAccounts(state,action){
      action.payload.forEach(element => {
        state.accountMap.set(element.rtAccountID, element);
      });

      return {
        ...state,
        accounts: Array.from(state.accountMap.values()),
        accountMap: state.accountMap,
      };
    }
  },
};
