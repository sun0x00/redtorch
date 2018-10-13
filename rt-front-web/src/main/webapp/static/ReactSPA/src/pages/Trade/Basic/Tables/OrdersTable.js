import React, { PureComponent } from 'react';
import {Table} from 'antd';
import { connect } from 'dva';
import styles from './Tables.less';
import {DIRECTION_TRANSLATER,DIRECTION_LONG,DIRECTION_SHORT,OFFSET_TRANSLATER, STATUS_TRANSLATER,STATUS_REJECTED, STATUS_CANCELLED, STATUS_ALLTRADED,STATUS_UNKNOWN, STATUS_NOTTRADED, STATUS_PARTTRADED} from '../../../../utils/RtConstant'
import {numberFormat,sortOrderByTimeAndID} from "../../../../utils/RtUtils"

@connect(({operation}) => ({
  operation
}))
class Center extends PureComponent {

  handleDblClick = (record)=>{
    const {dispatch} = this.props;

    dispatch({
      type: 'operation/cancelOrder',
      payload: {
        rtOrderID:record.rtOrderID
      }
    });
  }

  handleClick = (record)=>{
    const {updateTradeForm} = this.props
    if(updateTradeForm!==null&&updateTradeForm!==undefined){
      updateTradeForm({
        symbol:record.symbol
      })
    }
  }

  render() {
    
    const {
      list,
      pagination,
      scroll,
      bordered
    } = this.props;

    let tablePagination;
    if(pagination === undefined){
      tablePagination = false;
    }else{
      tablePagination = pagination;
    }

    let tableBordered;
    if(bordered === undefined){
      tableBordered = true;
    }else{
      tableBordered = bordered;
    }

    let tableList;
    if(list === undefined){
      tableList = [];
    }else{
      tableList = list.sort(sortOrderByTimeAndID);
    }
    
    let tableScroll;
    if(scroll === undefined){
      tableScroll = {y: 250,x:1340};
    }else{
      tableScroll = scroll;
    }
    
    const rtSymbolSet = new Set();
    const gatewaySet = new Set();
    tableList.forEach(record=>{  
      rtSymbolSet.add(record.rtSymbol)
      gatewaySet.add(`${record.gatewayDisplayName}(${record.gatewayID})?`)
    })
    const rtSymbolFilterArray = [];
    Array.from(rtSymbolSet).forEach(rtSymbol=>{
      rtSymbolFilterArray.push({text:rtSymbol,value:rtSymbol})
    })
    const gatewayFilterArray = [];
    Array.from(gatewaySet).forEach(gateway=>{
      gatewayFilterArray.push({text:gateway,value:gateway})
    })
    const statusFilterArray=[
      {text:"未成交", value:STATUS_NOTTRADED},
      {text:"部分成交", value:STATUS_PARTTRADED},
      {text:"全部成交", value:STATUS_ALLTRADED},
      {text:"撤销", value:STATUS_CANCELLED},
      {text:"拒单", value:STATUS_REJECTED},
      {text:"未知", value:STATUS_UNKNOWN}
    ]

    const columns = [ {
      title: '代码',
      dataIndex: 'rtSymbol',
      width: 140,
      key:'rtSymbol',
      sorter: (a, b) => a.rtSymbol > b.rtSymbol,
      filters: rtSymbolFilterArray,
      onFilter: (value, record) =>record.rtSymbol === value
        
    }, {
      title: '账户',
      dataIndex: 'accountID',
      width: 120,
      key:'accountID',
    },{
      title: '方向',
      dataIndex: 'direction',
      width: 60,
      render: (text, record) => {
        if(DIRECTION_LONG === record.direction){
          return(
            <span className={styles.colorBuy}> 
              {DIRECTION_TRANSLATER.get(record.direction)}
            </span>
          );
        }
        if(DIRECTION_SHORT === record.direction){
          return (
            <span className={styles.colorSell}> 
              {DIRECTION_TRANSLATER.get(record.direction)}
            </span>
          );
        }
        return (
          <span> 
            {DIRECTION_TRANSLATER.get(record.direction)}
          </span>
        );
        
      }
    }, {
      title: '开平',
      dataIndex: 'offset',
      width: 60,
      render: (text, record) => (
        <span> 
          {OFFSET_TRANSLATER.get(record.offset)}
        </span>
      )
    }, {
      title: '价格',
      dataIndex: 'price',
      width: 120,
      align: 'right',
      render:(text,record)=>(
        <span>
          {numberFormat(record.price,4)}
        </span>
      )
    }, {
      title: '总量',
      align: 'right',
      dataIndex: 'totalVolume',
      width: 100,
    }, {
      title: '成交量',
      align: 'right',
      dataIndex: 'tradedVolume',
      width: 100,
    },{
      title: '状态',
      dataIndex: 'status',
      key:'status',
      width: 80,
      render: (text, record) => (
        <span> 
          {STATUS_TRANSLATER.get(record.status)}
        </span>
      ),
      sorter: (a, b) => a.status > b.status,
      filters: statusFilterArray,
      onFilter: (value, record) => record.status === value
      
    }, {
      title: '委托时间',
      dataIndex: 'orderTime',
      width: 100,
      key:'orderTime',
      sorter: (a, b) => a.orderTime > b.orderTime
    }, {
      title: '更新时间',
      dataIndex: 'updateTime',
      width: 100,
      key:'updateTime',
      sorter: (a, b) => a.updateTime > b.updateTime
    }, {
      title: '委托编号',
      dataIndex: 'orderID',
      width: 80,
      key:'orderID',
    }, {
      title: '网关',
      dataIndex: 'gatewayDisplayName',
      width: 120,
      filters: gatewayFilterArray,
      onFilter: (value, record) => `${record.gatewayDisplayName}(${record.gatewayID})?` === value
    }];
    
    return (
      <Table
        onRow={(record) => ({onDoubleClick:() => this.handleDblClick(record),onClick:()=>this.handleClick(record)})}   // 点击行
        rowKey='rtOrderID' 
        size='small' 
        columns={columns} 
        dataSource={tableList} 
        pagination={tablePagination} 
        scroll={tableScroll} 
        bordered={tableBordered} 
      />
    );
  }
}

export default Center;
