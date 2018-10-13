import React, { PureComponent } from 'react';
import {Table} from 'antd';
import { connect } from 'dva';
import styles from './Tables.less';
import {numberFormat,sortBySymbol} from "../../../../utils/RtUtils"

@connect(({operation}) => ({
  operation
}))
class Center extends PureComponent {

  handleDblClick = (record)=>{
    const {dispatch} = this.props;

    dispatch({
      type: 'operation/unsubscribe',
      payload: {
        rtSymbol:record.rtSymbol,
        gatewayID:record.gatewayID,
      },
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
      tableList = list.sort(sortBySymbol);;
    }
    
    let tableScroll;
    if(scroll === undefined){
      tableScroll = {y: 250,x:1920};
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


    // const pagination = this.props.pagination;
    const columns = [ {
      title: '代码',
      dataIndex: 'rtSymbol',
      width: 150,
      key:'rtSymbol',
      sorter: (a, b) => a.rtSymbol > b.rtSymbol,
      filters: rtSymbolFilterArray,
      onFilter: (value, record) =>record.rtSymbol === value
    },{
      title: '名称',
      dataIndex: 'contractName',
      width: 150,
    }, {
      title: '最新价',
      dataIndex: 'lastPrice',
      width: 110,
      render: (text, record) => {
        if(record.priceRatio>0){
          return(
            <span className={styles.colorBuy}> 
              {numberFormat(record.lastPrice,4)}
            </span>
          );
        }
        if(record.priceRatio<0){
          return (
            <span className={styles.colorSell}> 
              {numberFormat(record.lastPrice,4)}
            </span>
          );
        }
        return (
          <span> 
            {numberFormat(record.lastPrice,4)}
          </span>
        );
      }
    }, {
      title: '涨跌',
      dataIndex: 'priceRatio',
      width: 110,
      render: (text, record) => {
        if(record.priceRatio>0){
          return(
            <span className={styles.colorBuy}> 
              {numberFormat(record.priceRatio*100,4)}%
            </span>
          );
        }
        if(record.priceRatio<0){
          return (
            <span className={styles.colorSell}> 
              {numberFormat(record.priceRatio*100,4)}%
            </span>
          );
        }
        return (
          <span> 
            {numberFormat(record.priceRatio*100,4)}%
          </span>
        );
      }
    }, {
      title: '卖一量',
      dataIndex: 'askVolume1',
      width: 110,
    }, {
      title: '卖一价',
      dataIndex: 'askPrice1',
      width: 110,
      render:(text,record)=>(
        <span>
          {numberFormat(record.askPrice1,4)}
        </span>
      )
    }, {
      title: '买一价',
      width: 110,
      dataIndex: 'bidPrice1',
      render:(text,record)=>(
        <span>
          {numberFormat(record.bidPrice1,4)}
        </span>
      )
    }, {
      title: '买一量',
      dataIndex: 'bidVolume1',
      width: 110,
    }, {
      title: '成交量',
      dataIndex: 'volume',
      width: 110,
    }, {
      title: '持仓量',
      dataIndex: 'openInterest',
      width: 110,
    }, {
      title: '时间',
      dataIndex: 'dateTime',
      width: 110,
    }, {
      title: '开盘价',
      dataIndex: 'openPrice',
      width: 110,
      render:(text,record)=>(
        <span>
          {numberFormat(record.openPrice,4)}
        </span>
      )
    }, {
      title: '最高价',
      dataIndex: 'highPrice',
      width: 110,
      render:(text,record)=>(
        <span>
          {numberFormat(record.highPrice,4)}
        </span>
      )
    }, {
      title: '最低价',
      dataIndex: 'lowPrice',
      width: 110,
      render:(text,record)=>(
        <span>
          {numberFormat(record.lowPrice,4)}
        </span>
      )
    }, {
      title: '昨收价',
      dataIndex: 'preClosePrice',
      width: 110,
      render:(text,record)=>(
        <span>
          {numberFormat(record.preClosePrice,4)}
        </span>
      )
    },{
      title: '网关',
      dataIndex: 'gatewayDisplayName',
      width: 180,
      filters: gatewayFilterArray,
      onFilter: (value, record) => `${record.gatewayDisplayName}(${record.gatewayID})?` === value
    }];


    return (
      <Table
        onRow={(record) => ({onDoubleClick:() => this.handleDblClick(record),onClick:()=>this.handleClick(record)})}   // 点击行
        rowKey='rtTickID'
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
