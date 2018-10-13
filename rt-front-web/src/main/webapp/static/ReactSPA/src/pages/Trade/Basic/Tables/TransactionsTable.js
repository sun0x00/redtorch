import React, { PureComponent } from 'react';
import {Table} from 'antd';
import styles from './Tables.less';
import {DIRECTION_TRANSLATER, OFFSET_TRANSLATER, DIRECTION_LONG, DIRECTION_SHORT} from '../../../../utils/RtConstant'
import {numberFormat,sortTradeByTimeAndID} from "../../../../utils/RtUtils"

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
    
    let tableScroll;
    if(scroll === undefined){
      tableScroll = {y: 250,x:1120};
    }else{
      tableScroll = scroll;
    }
    
    const rtSymbolSet = new Set();
    const gatewaySet = new Set();
    const rtOrderIDSet = new Set();
    tableList.forEach(record=>{  
      rtSymbolSet.add(record.rtSymbol)
      gatewaySet.add(`${record.gatewayDisplayName}(${record.gatewayID})?`)
      rtOrderIDSet.add(record.rtOrderID)
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
    }, {
      title: '方向',
      dataIndex: 'direction',
      width: 80,
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
      width: 80,
      render: (text, record) => (
        <span> 
          {OFFSET_TRANSLATER.get(record.offset)}
        </span>
      )
    }, {
      title: '价格',
      dataIndex: 'price',
      width: 120,
      render:(text,record)=>(
        <span>
          {numberFormat(record.price,4)}
        </span>
      ),
    }, {
      title: '数量',
      dataIndex: 'volume',
      width: 100,
    }, {
      title: '成交时间',
      dataIndex: 'tradeTime',
      width: 100,
      key:'tradeTime',
      sorter: (a, b) => a.tradeTime > b.tradeTime
    },{
      title: '成交编号',
      dataIndex: 'tradeID',
      width: 80,
    }, {
      title: '委托编号',
      dataIndex: 'orderID',
      width: 80,
      filters: rtOrderIDFilterArray,
      onFilter: (value, record) =>record.rtOrderID === value
    },{
      title: '网关',
      dataIndex: 'gatewayDisplayName',
      width: 120,
      filters: gatewayFilterArray,
      onFilter: (value, record) => `${record.gatewayDisplayName}(${record.gatewayID})?` === value
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
