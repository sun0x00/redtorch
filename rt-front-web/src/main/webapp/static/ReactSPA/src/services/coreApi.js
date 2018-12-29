// import { stringify } from 'qs';
import request from '@/utils/request';

export async function queryOrders(params){
  return request(`/api/core/orders?token=${params.token}`);
}

export async function queryPositions(params){
  return request(`/api/core/positions?token=${params.token}`);
}

export async function queryTicks(params){
  return request(`/api/core/ticks?token=${params.token}`);
}

export async function queryTrades(params){
  return request(`/api/core/trades?token=${params.token}`);
}

export async function queryGateways(params){
  return request(`/api/core/gateways?token=${params.token}`);
}

export async function reqChangeGatewayConnectStatus(params){
  return request(`/api/core/changeGatewayConnectStatus`,{
    method: 'POST',
    body: params,
  });
}

export async function reqDeleteGateway(params){
  return request(`/api/core/gateway`,{
    method: 'DELETE',
    body: params,
  });
}

export async function reqPutGateway(params){
  return request(`/api/core/gateway`,{
    method: 'PUT',
    body: params,
  });
}

export async function queryAccounts(params){
  return request(`/api/core/accounts?token=${params.token}`);
}


export async function queryLogDatas(params){
  return request(`/api/core/logs?token=${params.token}`);
}

export async function queryContracts(params){
  return request(`/api/core/contracts?token=${params.token}`);
}

export async function queryLocalPositionDetails(params){
  return request(`/api/core/localPositionDetails?token=${params.token}`);
}

export async function reqSubscribe(params){
  return request(`/api/core/subscribe`,{
    method: 'POST',
    body: params,
  });
}

export async function reqUnsubscribe(params){
  return request(`/api/core/unsubscribe`,{
    method: 'POST',
    body: params,
  });
}

export async function reqSendOrder(params){
  return request(`/api/core/sendOrder`,{
    method: 'POST',
    body: params,
  });
}

export async function reqCancelOrder(params){
  return request(`/api/core/cancelOrder`,{
    method: 'POST',
    body: params,
  });
}

export async function reqCancelAllOrders(params){
  return request(`/api/core/cancelAllOrders`,{
    method: 'POST',
    body: params,
  });
}


export async function querySrategyInfos(params){
  return request(`/api/zeus/srategyInfos?token=${params.token}`);
}

export async function reqChangeStrategyStatus(params){
  return request(`/api/zeus/changeStrategyStatus`,{
    method: 'POST',
    body: params,
  });
}



