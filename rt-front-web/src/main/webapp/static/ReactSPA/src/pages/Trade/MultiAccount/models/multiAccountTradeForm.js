import { queryContracts } from '@/services/coreApi';
import { DIRECTION_LONG,DIRECTION_SHORT} from "../../../../utils/RtConstant";
import {roundWithStep,numberFormat} from "../../../../utils/RtUtils";

export default {
  namespace: 'multiAccountTradeForm',

  state: {
    fuzzySymbol: undefined,
    exchange: undefined,
    priceAutoComplete: 'MANUAL',
    price: 0,
    direction: undefined,
    tick: undefined,
    step:0.0001,
    mixContractMap: new Map(),
    form:undefined,
    rtAccountIDs:[]
  },

  effects: {
    *updateTick({ payload }, { put }) {
      const tmpPayload = payload
      yield put({
        type: 'updateStateTick',
        payload: tmpPayload,
      });
    },
    *update({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'updateState',
        payload: tmpPayload
      });
    },*reset({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'resetState',
        payload: tmpPayload
      });
    },
    *updateContracts({ payload }, { put }) {
      const tmpPayload = payload; 
      yield put({
        type: 'updateStateContracts',
        payload: tmpPayload
      });
    },
    *fetchContracts({ payload }, { call, put }) {
      const response = yield call(queryContracts,{...payload,token:sessionStorage.getItem('token')});
      yield put({
        type: 'saveContracts',
        payload: response&&response.data&&Array.isArray(response.data) ? response.data : [],
      });
    }
  },

  reducers: {
    updateState(state,action){
      let tmpStep = 0.0001
      
      let tmpFuzzySymbol;
      if(action.payload.fuzzySymbol!=null
        &&action.payload.fuzzySymbol!==undefined){
          tmpFuzzySymbol = action.payload.fuzzySymbol
      }else if(state.fuzzySymbol!=null
        &&state.fuzzySymbol!==undefined){
          tmpFuzzySymbol = state.fuzzySymbol
      }
      if(tmpFuzzySymbol!=null
        &&tmpFuzzySymbol!==undefined){
        let tmpExchange;
        if(action.payload.exchange!=null&&action.payload.exchange!==undefined){
          tmpExchange = action.payload.exchange;
        } else if(state.exchange!=null&&state.exchange!==undefined){
          tmpExchange = state.exchange;
        }
        if(tmpExchange!=null&&tmpExchange!==undefined){
          tmpFuzzySymbol = `${tmpFuzzySymbol}.${tmpExchange}`
        }

        if(state.mixContractMap!=null&&state.mixContractMap!==undefined&&state.mixContractMap.has(tmpFuzzySymbol)){
          const contract = state.mixContractMap.get(tmpFuzzySymbol)
          tmpStep = contract.priceTick
        }

      }

      let tmpPrice = 0;
      if(action.payload.price!=null
        &&action.payload.price!==undefined){
          tmpPrice = action.payload.price
      }else if(state.price!=null
        &&state.price!==undefined){
          tmpPrice = state.price
      }

      tmpPrice = roundWithStep(tmpPrice,tmpStep)

      return {
        ...state,
        ...action.payload,
        step:tmpStep,
        price:numberFormat(tmpPrice,4)
      };
    },
    resetState(){
      return {
        fuzzySymbol: undefined,
        exchange: undefined,
        priceAutoComplete: 'MANUAL',
        price: 0,
        direction: undefined,
        step:0.0001,
        rtAccountIDs:[]
      };
    },
    saveContracts(state, action) {
      const newMixContractMap = new Map()
      action.payload.forEach(element => {
        newMixContractMap.set(element.rtContractID, element);
        newMixContractMap.set(element.rtSymbol, element);
        newMixContractMap.set(element.symbol, element);
        newMixContractMap.set(`${element.symbol}.${element.gatewayID}`, element);
      });
    
      return {
        ...state,
        mixContractMap:newMixContractMap,
      };
    },
    updateStateContracts(state,action){
      action.payload.forEach(element => {
        state.mixContractMap.set(element.rtSymbol, element);
        state.mixContractMap.set(element.rtContractID, element);
        state.mixContractMap.set(element.symbol, element);
        state.mixContractMap.set(`${element.symbol}.${element.gatewayID}`, element);
      });

      return {
        ...state,
        mixContractMap: state.mixContractMap
      };
    },
    updateStateTick(state,action){
      const ticks = action.payload

      if(state.fuzzySymbol==null
        ||state.fuzzySymbol===undefined
        ||state.direction===undefined
        ||state.direction===null
        ||state.priceAutoComplete==="MANUAL"){
        return state
      }

      let tmpFuzzySymbol = state.fuzzySymbol;
      if(state.exchange!=null&&state.exchange!==undefined){
        tmpFuzzySymbol = `${state.fuzzySymbol}.${state.exchange}`
      }
      let targetTick = null;
      ticks.forEach(element => {
        if(state.exchange!=null&&state.exchange!==undefined){
          if(tmpFuzzySymbol === element.rtSymbol){
            targetTick = element
          }
        }else if(tmpFuzzySymbol === element.symbol){
          targetTick = element
        }
        
      });

      if(targetTick!=null&&targetTick!==undefined){
        let tmpPrice = state.price;


        if(state.priceAutoComplete!=null&&state.priceAutoComplete!==undefined){
          if(state.priceAutoComplete==="LASTPRICE"){
            tmpPrice=targetTick.lastPrice
          }else if(state.priceAutoComplete==="ACTIVE"){
            if(state.direction === DIRECTION_LONG){
              tmpPrice = targetTick.askPrice1
            }else if(state.direction === DIRECTION_SHORT){
              tmpPrice = targetTick.bidPrice1
            }
          }else if(state.priceAutoComplete==="QUEUE"){
            if(state.direction === DIRECTION_LONG){
              tmpPrice = targetTick.bidPrice1
            }else if(state.direction === DIRECTION_SHORT){
              tmpPrice = targetTick.askPrice1
            }
          }else if(state.priceAutoComplete==="ADD2"){
            if(state.mixContractMap!=null&&state.mixContractMap!==undefined&&state.mixContractMap.has(tmpFuzzySymbol)){
              const contract = state.mixContractMap.get(tmpFuzzySymbol)
              const tmpStep = contract.priceTick

              if(state.direction === DIRECTION_LONG){
                tmpPrice = targetTick.askPrice1+tmpStep*2
              }else if(state.direction === DIRECTION_SHORT){
                tmpPrice = targetTick.bidPrice1-tmpStep*2
              }
            }else{
              tmpPrice=targetTick.lastPrice
            }
          }
        }


        return {
          ...state,
          price: numberFormat(tmpPrice,4),
          tick: targetTick,
        }
      }

      return  state;
    }
    
  }
}
