import React, { PureComponent } from 'react';
import {Table} from 'antd';
import styles from './Tables.less';
import {numberFormat,sortByRtAccountID} from "../../../../utils/RtUtils"

class Center extends PureComponent {
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
      tableList = list.sort(sortByRtAccountID);
    }
    
    let tableScroll;
    if(scroll === undefined){
      tableScroll = {y: 250,x:1000};
    }else{
      tableScroll = scroll;
    }

    const gatewaySet = new Set();
    tableList.forEach(record=>{  
      gatewaySet.add(`${record.gatewayDisplayName}(${record.gatewayID})?`)
    })

    const gatewayFilterArray = [];
    Array.from(gatewaySet).forEach(gateway=>{
      gatewayFilterArray.push({text:gateway,value:gateway})
    })
    
    const columns = [{
      title: '账户',
      dataIndex: 'accountID',
      width: 120,
    },
    {
      title: '币种',
      dataIndex: 'currency',
      width: 60,
    }, {
      title: '昨结',
      dataIndex: 'preBalance',
      width: 120,
      key:'preBalance',
      align: 'right',
      sorter: (a, b) => a.preBalance > b.preBalance,
      render:(text,record)=>(
        <span>
          {numberFormat(record.preBalance,4)}
        </span>
      )
    }, {
      title: '权益',
      dataIndex: 'balance',
      width: 120,
      key:'balance',
      align: 'right',
      sorter: (a, b) => a.balance > b.balance,
      render: (text, record) => {
        if(record.balance - record.preBalance>0){
          return(
            <span className={styles.colorBuy}> 
              {numberFormat(record.balance,4)}
            </span>
          );
        }
        if(record.balance - record.preBalance<0){
          return (
            <span className={styles.colorSell}> 
              {numberFormat(record.balance,4)}
            </span>
          );
        }
        return (
          <span> 
            {numberFormat(record.balance,4)}
          </span>
        );
      }
    }, {
      title: '可用',
      dataIndex: 'available',
      width: 120,
      key:'available',
      align: 'right',
      sorter: (a, b) => a.available > b.available,
      render:(text,record)=>(
        <span>
          {numberFormat(record.available,4)}
        </span>
      )
    }, {
      title: '佣金',
      width: 120,
      dataIndex: 'commission',
      key:'commission',
      align: 'right',
      sorter: (a, b) => a.commission > b.commission,
      render:(text,record)=>(
        <span>
          {numberFormat(record.commission,4)}
        </span>
      )
    }, {
      title: '平仓盈亏',
      width: 120,
      dataIndex: 'closeProfit',
      key:'closeProfit',
      align: 'right',
      sorter: (a, b) => a.closeProfit > b.closeProfit,
      render: (text, record) => {
        if(record.closeProfit>0){
          return(
            <span className={styles.colorBuy}> 
              {numberFormat(record.closeProfit,4)}
            </span>
          );
        }
        if(record.closeProfit<0){
          return (
            <span className={styles.colorSell}> 
              {numberFormat(record.closeProfit,4)}
            </span>
          );
        }
        return (
          <span> 
            {numberFormat(record.closeProfit,4)}
          </span>
        );
      }
    }, {
      title: '持仓盈亏',
      dataIndex: 'positionProfit',
      width: 120,
      key:'positionProfit',
      align: 'right',
      sorter: (a, b) => a.positionProfit > b.positionProfit,
      render: (text, record) => {
        if(record.positionProfit>0){
          return(
            <span className={styles.colorBuy}> 
              {numberFormat(record.positionProfit,4)}
            </span>
          );
        }
        if(record.positionProfit<0){
          return (
            <span className={styles.colorSell}> 
              {numberFormat(record.positionProfit,4)}
            </span>
          );
        }
        return (
          <span> 
            {numberFormat(record.positionProfit,4)}
          </span>
        );
      }
    },{
      title: '网关',
      dataIndex: 'gatewayDisplayName',
      width: 120,
      filters: gatewayFilterArray,
      onFilter: (value, record) => `${record.gatewayDisplayName}(${record.gatewayID})?` === value
    }];
    
    return (
      <Table
        rowKey="rtAccountID"
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
