// import mockjs from 'mockjs';

function orderList(count){
  const orders = [];
  for (let i = 0; i < count; i+=1 ) {
    orders.push({
      rtOrderID: `rtOrderID ${i}`,
      orderID: `orderID ${i}`,
      rtSymbol: `rt ${i}`,
      gatewayDisplayName: `测试网关名称 ${i}`,
      gatewayID: `gatewayID ${i}`,
      offset: `CLOSE`,
      status: `UNKNOWN`,
      direction: 'SHORT'
    });
  }
  for (let i = 0; i < count; i+=1 ) {
    orders.push({
      rtOrderID: `rtOrderID ${i}`,
      orderID: `orderID ${i}`,
      rtSymbol: `rt ${i}`,
      gatewayDisplayName: `测试网关名称 ${i}`,
      gatewayID: `gatewayID ${i}`,
      offset: `CLOSE`,
      status: `ALLTRADED`,
      direction: 'SHORT'
    });
  }
  return orders
}

function queryOrders(req, res){
  if(req.query.token!=='1'){
    res.status(401).json({status:'error'})
  }else{
    res.json({data:orderList(50),status:'success'})
  }
  
}

function positionsList(count){
  const positions = [];
  for (let i = 0; i < count; i+=1 ) {
    positions.push({
      contractName: `合约名称 ${i}`,
      rtSymbol: `RT ${i}`,
      direction: 'LONG',
      position: i,
      ydPosition: i,
      frozen: i,
      price: 900,
      positionProfit: 200,
      gatewayDisplayName: `测试网关名称 ${i}`,
      gatewayID: `gatewayID ${i}`,
      rtPositionID:`rtPositionID ${i}`
    });
  }
  return positions;
}

function queryPositions(req, res){
  if(req.query.token!=='1'){
    res.status(401).json({status:'error'})
  }else{
    res.json({data:positionsList(100),status:'success'})
  }
  
}

function tickList(count){
  const ticks = [];
  for (let i = 0; i < count; i+=1 ) {
    ticks.push({
      rtSymbol: `TA${i}.SZSH`,
      lastPrice: 100,
      volume: 20,
      openPrice:102,
      preClosePrice: 99,
      highPrice: 112,
      lowPrice: 90,
      openInterest: 22343533,
      askPrice1: 101,
      askVolume1: 30,
      bidPrice1: 92,
      bidVolume1: 57886,
      gatewayDisplayName: `接口测试${i}`,
      gatewayID: `GatewayID${i}`,
      dateTime:{
        millis: 1539871606000+i
      }
    });
  }
  return ticks;
}

function queryTicks(req, res){
  if(req.query.token!=='1'){
    res.status(401).json({status:'error'})
  }else{
    res.json({data:tickList(100),status:'success'})
  }
  
}


function accountList(count){
  const accounts = [];
  for (let i = 0; i < count; i+=1 ) {
    accounts.push({
      accountID: `accountID ${i}`,
    });
  }
  return accounts;
}

function queryAccounts(req, res){
  if(req.query.token!=='1'){
    res.status(401).json({data:null,status:'error'})
  }else{
    res.json({data:accountList(100),status:'success'})
  }
  
}

function tradeList(count){
  const trades = [];
  for (let i = 0; i < count; i+=1 ) {
    trades.push({
      rtTradeID: `TEdward King ${i}`,
      rtOrderID: `TEdward King o${i}`,
      age: 32,
      address: `London, Park Lane no. ${i}`,
      rtSymbol: `rt ${i}`,
      gatewayDisplayName: `OEdward King ${i}`,
      gatewayID: `OEdward King ${i}`,
    });
  }
  return trades;
}

function queryTrades(req, res){
  if(req.query.token!=='1'){
    res.status(401).json({status:'error'})
  }else{
    res.json({data:tradeList(100),status:'success'})
  }
  
}

function gatewayList(count){
  const gateways = [];
  for (let i = 0; i < count; i+=1 ) {
    gateways.push({
      gatewayID: `TEdward King ${i}`,
      gatewayDisplayName: `TEdward King ${i}`,
      state:false,
      gatewayClassName: "xyz.redtorch.gateway.ctp.CtpGateway",
      type:"CTP"
    });
  }
  return gateways;
}

function queryGateways(req, res){
  if(req.query.token!=='1'){
    res.status(401).json({status:'error'})
  }else{
    res.json({data:gatewayList(10),status:'success'})
  }
  
}

function changeGatewayConnectStatus(req, res){
  if(req.body.token!=='1'){
    res.status(401).json({status:'error'})
  }else{
    res.status(202).json({status:'success'})
  }
  
}
function deleteGateway(req, res){
  console.log(req)
  console.log(req.body)
  if(req.body.token!=='1'){
    res.status(401).json({status:'error'})
  }else{
    res.json({status:'success'})
  }
  
}

function saveOrUpdateDateGateway(req, res){
  if(req.body.token!=='1'){
    res.status(401).json({status:'error'})
  }else{
    res.json({status:'success'})
  }
  
}


export default {
  'GET /api/core/orders':queryOrders,
  'GET /api/core/positions':queryPositions,
  'GET /api/core/ticks':queryTicks,
  'GET /api/core/accounts':queryAccounts,
  'GET /api/core/trades':queryTrades,
  'GET /api/core/gateways':queryGateways,
  'POST /api/core/changeGatewayConnectStatus':changeGatewayConnectStatus,
  'DELETE /api/core/gateway':deleteGateway,
  'PUT /api/core/gateway':saveOrUpdateDateGateway,
};
