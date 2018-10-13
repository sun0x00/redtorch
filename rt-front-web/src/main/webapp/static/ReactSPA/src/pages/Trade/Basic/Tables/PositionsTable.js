import React, { PureComponent } from 'react';
import {Table} from 'antd';
import { connect } from 'dva';
import styles from './Tables.less';
import {DIRECTION_TRANSLATER,DIRECTION_LONG,DIRECTION_SHORT} from '../../../../utils/RtConstant'
import {numberFormat,sortBySymbol} from "../../../../utils/RtUtils"

@connect(({operation}) => ({
  operation
}))
class Center extends PureComponent {

  handleDblClick = (record)=>{
    const {dispatch} = this.props;

    dispatch({
      type: 'operation/subscribe',
      payload: {
        subscribeReq:{
          symbol:record.rtSymbol,
        },
        gatewayIDs:[record.gatewayID],
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
      tableList = list.sort(sortBySymbol);
    }
    
    let tableScroll;
    if(scroll === undefined){
      tableScroll = {y: 250,x:1040};
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
    const columns = [{
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
    },  {
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
      title: '持仓',
      dataIndex: 'position',
      width:  100,
      align: 'right',
    }, {
      title: '昨仓',
      dataIndex: 'ydPosition',
      width:  100,
      align: 'right',
    }, {
      title: '冻结',
      dataIndex: 'frozen',
      width:  100,
      align: 'right',
    }, {
      title: '均价',
      dataIndex: 'price',
      width:  120,
      align: 'right',
      render:(text,record)=>(
        <span>
          {numberFormat(record.price,4)}
        </span>
      )
    }, {
      title: '持仓盈亏',
      dataIndex: 'positionProfit',
      width:  120,
      align: 'right',
      render:(text,record)=>(
        <span>
          {numberFormat(record.positionProfit,4)}
        </span>
      )
    },{
      title: '名称',
      dataIndex: 'contractName',
      width: 120,
    },{
      title: '网关',
      dataIndex: 'gatewayDisplayName',
      width: 120,
      filters: gatewayFilterArray,
      onFilter: (value, record) => `${record.gatewayDisplayName}(${record.gatewayID})?` === value
    }];



    return (
      <Table
        onRow={(record) => ({onDoubleClick:() => this.handleDblClick(record),onClick:()=>this.handleClick(record)})}   // 点击行
        rowKey='rtPositionID'
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
