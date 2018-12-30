import React, { PureComponent } from 'react';
import {Table} from 'antd';
import { connect } from 'dva';
import styles from './Tables.less';
import {DIRECTION_TRANSLATER,DIRECTION_LONG,DIRECTION_SHORT,OFFSET_TRANSLATER, STATUS_TRANSLATER,STATUS_REJECTED, STATUS_CANCELLED, STATUS_ALLTRADED,STATUS_UNKNOWN, STATUS_NOTTRADED, STATUS_PARTTRADED} from '../../../../utils/RtConstant'
import {numberFormat,sortOrderByTimeAndID} from "../../../../utils/RtUtils"

const INLINE_LABEL_STYLE={
  display:'inline-block',
  float:'left',
  color:'#AAA',
  paddingLeft:'2px'
}

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
    
    let tableScroll= {y: 250,x:1020};;
    if(scroll!==undefined){
      tableScroll = {...tableScroll,...scroll};
    }
    
    const rtSymbolSet = new Set();
    const gatewaySet = new Set();
    tableList.forEach(record=>{  
      rtSymbolSet.add(record.rtSymbol)
      gatewaySet.add(`${record.gatewayDisplayName}(${record.gatewayID})`)
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

    const columns = [{
      title: '产品',
      dataIndex: 'rtSymbol',
      width: 150,
      key:'rtSymbol',
      sorter: (a, b) => a.rtSymbol > b.rtSymbol,
      filters: rtSymbolFilterArray,
      onFilter: (value, record) =>record.rtSymbol === value,
      render: (text, record) => (
        (
          <div className={`${styles.cell} ${styles.cursorPointer}`}>
            <div className={`${styles.colorYellow}`}>{record.rtSymbol}<br /></div>
            <div>{record.contractName}</div>
          </div>
        )
      )
    }, {
      title: '账户',
      dataIndex: 'gatewayDisplayName',
      width: 180,
      filters:gatewayFilterArray,
      onFilter: (value, record) => `${record.gatewayDisplayName}(${record.gatewayID})` === value,
      render: (text, record) => (
        <div className={`${styles.displayRight}`}>
          <div>{ record.accountID}<br /></div>
          <div style={{color:"#BBB"}}>{ record.gatewayDisplayName}</div>
        </div>
      )
    },  {
      title: '方向',
      dataIndex: 'direction',
      width: 100,
      render: (text, record) => (
        <div className={`${styles.displayRight}`}>
          {
            record.direction === DIRECTION_LONG &&
            <div className={`${styles.colorBuy}`}><span style={INLINE_LABEL_STYLE}>方向：</span>{DIRECTION_TRANSLATER.get( record.direction)}<br /></div>
          }
          
          {
            record.direction === DIRECTION_SHORT &&
            <div className={`${styles.colorSell}`}><span style={INLINE_LABEL_STYLE}>方向：</span>{DIRECTION_TRANSLATER.get( record.direction)}<br /></div>
          }
          {
            ( record.direction !== DIRECTION_LONG &&  record.direction !== DIRECTION_SHORT) &&
            <div><span style={INLINE_LABEL_STYLE}>方向：</span>{DIRECTION_TRANSLATER.get( record.direction)}<br /></div>
          }
          <div><span style={INLINE_LABEL_STYLE}>开平：</span>{OFFSET_TRANSLATER.get( record.offset)}</div>
        </div>
      )
    }, {
      title: '价格',
      dataIndex: 'price',
      width: 120,
      align: 'right',
      render:(text,record)=>(
        <div className={`${styles.displayRight}`}>
          {numberFormat(record.price,4)}
        </div>
      )
    }, {
      title: '量',
      align: 'right',
      dataIndex: 'totalVolume',
      width: 150,
      render:(text,record)=>(
        <div className={`${styles.displayRight} ${styles.colorCount}`}>
          <div><span style={INLINE_LABEL_STYLE}>委托：</span>{record.totalVolume}<br /></div>
          <div><span style={INLINE_LABEL_STYLE}>成交：</span>{record.tradedVolume}</div>
        </div>
      )
    },{
      title: '状态',
      dataIndex: 'status',
      key:'status',
      width: 150,
      render: (text, record) => (
        <div className={`${styles.displayRight}`}>
          <div><span style={INLINE_LABEL_STYLE}>状态：</span>{STATUS_TRANSLATER.get(record.status)}<br /></div>
          <div><span style={INLINE_LABEL_STYLE}>编号：</span>{record.orderID}</div>
        </div>
      ),
      sorter: (a, b) => a.status > b.status,
      filters: statusFilterArray,
      onFilter: (value, record) => record.status === value
      
    }, {
      title: '时间',
      dataIndex: 'orderTime',
      width: 150,
      key:'orderTime',
      sorter: (a, b) => a.orderTime > b.orderTime,
      render: (text, record) => (
        <div className={`${styles.displayRight}`}>
          <div><span style={INLINE_LABEL_STYLE}>委托：</span>{record.orderTime}<br /></div>
          <div><span style={INLINE_LABEL_STYLE}>更新：</span>{record.updateTime}</div>
        </div>
      ),
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
