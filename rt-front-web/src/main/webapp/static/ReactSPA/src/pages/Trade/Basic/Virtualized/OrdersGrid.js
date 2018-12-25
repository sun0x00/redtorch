import React, { PureComponent } from 'react';
import {MultiGrid,AutoSizer} from 'react-virtualized'
import { connect } from 'dva';
import {numberFormat,sortOrderByTimeAndID} from '../../../../utils/RtUtils'
import {DIRECTION_TRANSLATER,DIRECTION_LONG,DIRECTION_SHORT,OFFSET_TRANSLATER,STATUS_TRANSLATER} from '../../../../utils/RtConstant'
import styles from './Grid.less';

const STYLE = {
  border: '1px solid #E8E8E8',
};
const STYLE_BOTTOM_LEFT_GRID = {
  borderRight: '1px solid #E8E8E8',
  backgroundColor: '#fafafa',
};
const STYLE_BOTTOM_RIGHT_GRID = {
  outline:'none',
};
const STYLE_TOP_LEFT_GRID = {
  borderBottom: '2px solid #E8E8E8',
  borderRight: '2px solid #E8E8E8',
  fontWeight: 'bold',
};
const STYLE_TOP_RIGHT_GRID = {
  borderBottom: '2px solid #E8E8E8',
  fontWeight: 'bold',
};


@connect(({operation}) => ({
  operation
}))
class Center extends PureComponent {
  constructor(props) {
    super(props);
    this.state={
       // hoveredColumnIndex: null,
       hoveredRowIndex: null
    }
 }

  render() {
    const {
      list,
      height,
      dispatch
    } = this.props;
    const {
      hoveredRowIndex
    } = this.state;

    let tableHeight;
    if(height === undefined){
      tableHeight = 600;
    }else{
      tableHeight = height;
    }

    const tableList = []

    let columnCount = 0;
    {
      const headerMap = new Map();
      headerMap.set(columnCount,"代码"); // 0
      headerMap.set(columnCount+=1,"账户"); // 1
      headerMap.set(columnCount+=1,"方向"); // 2
      headerMap.set(columnCount+=1,"开平"); // 3
      headerMap.set(columnCount+=1,"状态"); // 4
      headerMap.set(columnCount+=1,"价格"); // 5
      headerMap.set(columnCount+=1,"总量"); // 6
      headerMap.set(columnCount+=1,"成交量"); // 7
      headerMap.set(columnCount+=1,"委托时间"); // 8
      headerMap.set(columnCount+=1,"更新时间"); // 9
      headerMap.set(columnCount+=1,"委托编号"); // 10
      headerMap.set(columnCount+=1,"网关"); // 11
      tableList.push(headerMap)
      columnCount+=1
    }

    const sortedList = list.sort(sortOrderByTimeAndID)

    if(sortedList !== undefined){
      sortedList.forEach(item=>{
        let i = 0;
        const dataMap = new Map();
        dataMap.set(i,item.rtSymbol);
        dataMap.set(i+=1,item.accountID);
        dataMap.set(i+=1,item.direction);
        dataMap.set(i+=1,item.offset);
        dataMap.set(i+=1,item.status);
        dataMap.set(i+=1,numberFormat(item.price,4));
        dataMap.set(i+=1,item.totalVolume);
        dataMap.set(i+=1,item.tradedVolume);
        dataMap.set(i+=1,item.orderTime);
        dataMap.set(i+=1,item.updateTime);
        dataMap.set(i+=1,item.orderID);
        dataMap.set(i+=1,item.gatewayDisplayName);

        // =======不渲染的字段============
        dataMap.set(1001,item.gatewayID); 
        dataMap.set(1002,item.rtOrderID); 
        dataMap.set(1003,item.symbol); 
        // ==============================

        tableList.push(dataMap)
      })
      
    }


    const rowCount = tableList.length;
    
    const multiGridRef = React.createRef();

    const cellRenderer=({columnIndex, key, rowIndex, style})=>{

      const hoveredCellClass = rowIndex === hoveredRowIndex ? styles.hoveredCell : '';

      
      const handleDoubleClick=()=>{
        dispatch({
          type: 'operation/cancelOrder',
          payload: {
            rtOrderID:tableList[rowIndex].get(1002)
          },
        });
      }
      const handleClick=()=>{
        const {updateTradeForm} = this.props
        if(updateTradeForm!==null&&updateTradeForm!==undefined){
          updateTradeForm({
            symbol:tableList[rowIndex].get(1003)
          })
        }
      }

      if(rowIndex === 0){
        return (
          <div className={styles.headerCell} key={key} style={style}>
            {tableList[rowIndex].get(columnIndex)}
          </div>
        );

      }
      if(columnIndex === 0){
        return (
          <div onDoubleClick={handleDoubleClick} onClick={handleClick} onFocus={() => undefined} className={`${styles.cell} ${hoveredCellClass} ${styles.colorYellow} ${styles.cursorPointer}`} key={key} style={style}>
            {tableList[rowIndex].get(columnIndex)}
          </div>
        );
      }

      if(columnIndex === 2){
        if(tableList[rowIndex].get(columnIndex) === DIRECTION_LONG){
          return(
            <div className={`${styles.cell} ${hoveredCellClass} ${styles.colorBuy}`} key={key} style={style}>
              {DIRECTION_TRANSLATER.get(tableList[rowIndex].get(columnIndex))}
            </div>
          )
        }
        
        if(tableList[rowIndex].get(columnIndex) === DIRECTION_SHORT){
          return(
            <div className={`${styles.cell} ${hoveredCellClass} ${styles.colorSell}`} key={key} style={style}>
              {DIRECTION_TRANSLATER.get(tableList[rowIndex].get(columnIndex))}
            </div>
          )
        }
        return(
          <div className={`${styles.cell} ${hoveredCellClass}`} key={key} style={style}>
            {DIRECTION_TRANSLATER.get(tableList[rowIndex].get(columnIndex))}
          </div>
        )

      }

      if(columnIndex === 3){
        return(
          <div className={`${styles.cell} ${hoveredCellClass}`} key={key} style={style}>
            {OFFSET_TRANSLATER.get(tableList[rowIndex].get(columnIndex))}
          </div>
        )
      }

      if(columnIndex === 4){
        return(
          <div className={`${styles.cell} ${hoveredCellClass}`} key={key} style={style}>
            {STATUS_TRANSLATER.get(tableList[rowIndex].get(columnIndex))}
          </div>
        )
      }
      

      return (
        <div className={`${styles.cell}  ${hoveredCellClass}`} key={key} style={style}>
          {tableList[rowIndex].get(columnIndex)}
        </div>
      )
    }

    const getColumnWidth=({index}) => {
      switch (index) {
        case 0:
          return 130;
        case 2:
          return 60;
        case 3:
          return 60;
        case 10:
            return 120;
        default:
          return 110;
      }
    }
    
    return (

      <AutoSizer disableHeight>
        {({width}) => (
          <MultiGrid
            ref={multiGridRef}
            fixedColumnCount={1}
            fixedRowCount={1}
            cellRenderer={cellRenderer}
            columnWidth={getColumnWidth}
            columnCount={columnCount}
            enableFixedColumnScroll
            enableFixedRowScroll
            height={tableHeight}
            rowHeight={50}
            rowCount={rowCount}
            style={STYLE}
            styleBottomLeftGrid={STYLE_BOTTOM_LEFT_GRID}
            styleBottomRightGrid={STYLE_BOTTOM_RIGHT_GRID}
            styleTopLeftGrid={STYLE_TOP_LEFT_GRID}
            styleTopRightGrid={STYLE_TOP_RIGHT_GRID}
            width={width}
            hideTopRightGridScrollbar
            hideBottomLeftGridScrollbar
          />
        )}
      </AutoSizer>
    );
  }
}

export default Center;
