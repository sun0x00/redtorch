import {socketListen,socketStatus} from '@/services/socketApi';
import { notification } from 'antd';

export default {
  namespace: 'socket',

  state: {
  },


  subscriptions: {

    setupHistory ({ dispatch, history}) {
      history.listen((location) => {
        if(!socketStatus()&&location.pathname.indexOf('user/login')<0&&sessionStorage.getItem('token') !== undefined&&sessionStorage.getItem('token') !==null){
          socketListen((data)=> {
            switch (data.type) {
              case 'E_TICKS|':
                dispatch({
                  type: 'tick/updateTicks', 
                  payload: data.payload,
                });
                dispatch({
                  type: 'basicTradeForm/updateTick', 
                  payload: data.payload,
                });
                break;
              case 'E_TICKS_CHANGED|':
                dispatch({
                  type: 'tick/fetchTicks', 
                  payload: data.payload,
                });
                break;
              case 'E_ORDERS|':
                dispatch({
                  type: 'order/updateOrders', 
                  payload: data.payload,
                });
                break;
              case 'E_POSITIONS|':
                dispatch({
                  type: 'position/updatePositions', 
                  payload: data.payload,
                });
                break;
              case 'E_TRADES|':
                dispatch({
                  type: 'trade/updateTrades', 
                  payload: data.payload,
                });
                break;
              case 'E_ACCOUNTS|':
                dispatch({
                  type: 'account/updateAccounts', 
                  payload: data.payload,
                });
                break;
              case 'E_LOGS|':
                dispatch({
                  type: 'logData/updateLogDatas', 
                  payload: data.payload,
                });
                break;
              case 'E_GATEWAY|':
                dispatch({
                  type: 'gateway/fetchGateways', 
                  payload: data.payload,
                });
                
                dispatch({
                  type: 'order/fetchOrders', 
                  payload: data.payload,
                });
                
                dispatch({
                  type: 'position/fetchPositions', 
                  payload: data.payload,
                });

                
                dispatch({
                  type: 'account/fetchAccounts', 
                  payload: data.payload,
                });

                dispatch({
                  type: 'tick/fetchTicks', 
                  payload: data.payload,
                });
                
                dispatch({
                  type: 'trade/fetchTrades', 
                  payload: data.payload,
                });
                break;
              case 'disconnect':
                notification.error({
                  message: `SocketIO`,
                  description: "连接已断开",
                });
                dispatch({
                  type: 'login/logout',
                });
                break;
              case 'connect_failed':
                notification.error({
                  message: `SocketIO`,
                  description: "建立连接失败",
                });
                dispatch({
                  type: 'login/logout',
                });
                break;
              case 'connect':
                notification.info({
                  message: `SocketIO`,
                  description: "已建立连接"
                });
                break;
              case 'connecting':
                break;
              case 'error':
                notification.error({
                  message: `SocketIO`,
                  description: "错误",
                });
                dispatch({
                  type: 'login/logout',
                });
                break;
              case 'connect_error':
                notification.error({
                  message: `SocketIO`,
                  description: "建立连接错误",
                });
                dispatch({
                  type: 'login/logout',
                });

                break;
              default:
                  break;
            }
          })
        }
      })
    },

  },
};
