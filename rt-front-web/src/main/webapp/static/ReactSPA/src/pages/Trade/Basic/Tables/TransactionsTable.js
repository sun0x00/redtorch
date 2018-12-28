import React, { PureComponent } from 'react';
import {Table} from 'antd';
import styles from './Tables.less';
import {DIRECTION_TRANSLATER, OFFSET_TRANSLATER, DIRECTION_LONG, DIRECTION_SHORT} from '../../../../utils/RtConstant'
import {numberFormat,sortTradeByTimeAndID} from "../../../../utils/RtUtils"

const INLINE_LABEL_STYLE={
  display:'inline-block',
  float:'left',
  color:'#AAA',
  // paddingLeft:'2px'
}

class Center extends PureComponent {

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
      tableList = list.sort(sortTradeByTimeAndID);
    }
    
    let tableScroll= {y: 250,x:900};
    if(scroll !== undefined){
      tableScroll ={...tableScroll,...scroll};
    }
    
    const rtSymbolSet = new Set();
    const gatewaySet = new Set();
    const rtOrderIDSet = new Set();
    tableList.forEach(record=>{  
      rtSymbolSet.add(record.rtSymbol)
      gatewaySet.add(`${record.gatewayDisplayName}(${record.gatewayID})`)
      rtOrderIDSet.add(`委托编号[${record.orderID}]`)
    })
    const rtSymbolFilterArray = [];
    Array.from(rtSymbolSet).forEach(rtSymbol=>{
      rtSymbolFilterArray.push({text:rtSymbol,value:rtSymbol})
    })
    const gatewayFilterArray = [];
    Array.from(gatewaySet).forEach(gateway=>{
      gatewayFilterArray.push({text:gateway,value:gateway})
    })
    const rtOrderIDFilterArray = [];
    Array.from(rtOrderIDSet).forEach(rtOrderID=>{
      rtOrderIDFilterArray.push({text:rtOrderID,value:rtOrderID})
    })

    const columns = [ {
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
            <div className={`${styles.colorYellow}`}>{record.rtSymbol}</div>
            <div>{record.contractName}</div>
          </div>
        )
      )
    }, {
      title: '账户',
      dataIndex: 'gatewayDisplayName',
      width: 180,
      align: 'center',
      filters:gatewayFilterArray,
      onFilter: (value, record) => `${record.gatewayDisplayName}(${record.gatewayID})` === value,
      render: (text, record) => (
        <div className={` ${styles.displayRight}`}>
          <div>{ record.accountID}</div>
          <div style={{color:"#BBB"}}>{ record.gatewayDisplayName}</div>
        </div>
      )
    }, {
      title: '方向',
      dataIndex: 'direction',
      width: 80,
      align: 'center',
      render: (text, record) => (
        <div className={`${styles.displayRight}`}>
          {
            record.direction === DIRECTION_LONG &&
            <div className={`${styles.colorBuy}`}><span style={INLINE_LABEL_STYLE}>方向：</span>{DIRECTION_TRANSLATER.get( record.direction)}</div>
          }
          
          {
            record.direction === DIRECTION_SHORT &&
            <div className={`${styles.colorSell}`}><span style={INLINE_LABEL_STYLE}>方向：</span>{DIRECTION_TRANSLATER.get( record.direction)}</div>
          }
          {
            ( record.direction !== DIRECTION_LONG &&  record.direction !== DIRECTION_SHORT) &&
            <div><span style={INLINE_LABEL_STYLE}>方向：</span>{DIRECTION_TRANSLATER.get( record.direction)}</div>
          }
          <div><span style={INLINE_LABEL_STYLE}>开平：</span>{OFFSET_TRANSLATER.get( record.offset)}</div>
        </div>
      )
    }, {
      title: '价格',
      dataIndex: 'price',
      width: 120,
      align: 'center',
      render:(text,record)=>(
        <div className={`${styles.displayRight}`}>
          {numberFormat(record.price,4)}
        </div>
      ),
    }, {
      title: '数量',
      dataIndex: 'volume',
      align: 'center',
      width: 80,
      render:(text,record)=>(
        <div className={`${styles.displayRight} ${styles.colorCount}`}>
          {record.volume}
        </div>
      ),
    }, {
      title: '成交时间',
      dataIndex: 'tradeTime',
      width: 120,
      align: 'center',
      key:'tradeTime',
      sorter: (a, b) => a.tradeTime > b.tradeTime,
      render:(text,record)=>(
        <div className={`${styles.displayRight}`}>
          {record.tradeTime}
        </div>
      ),
    },{
      title: '编号',
      dataIndex: 'tradeID',
      align: 'center',
      width: 150,
      filters: rtOrderIDFilterArray,
      onFilter: (value, record) =>`委托编号[${record.orderID}]`=== value,
      render:(text,record)=>(
        <div className={`${styles.displayRight}`}>
          <div><span style={INLINE_LABEL_STYLE}>成交：</span>{record.tradeID}</div>
          <div><span style={INLINE_LABEL_STYLE}>委托：</span>{record.orderID}</div>
        </div>
      )
    }];
    
    return (
      <Table 
        onRow={(record) => ({onClick:()=>this.handleClick(record)})}   // 点击行
        rowKey='rtTradeID'
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
