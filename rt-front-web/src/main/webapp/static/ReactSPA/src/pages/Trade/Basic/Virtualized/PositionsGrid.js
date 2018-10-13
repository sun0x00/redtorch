import React, { PureComponent } from 'react';
import {MultiGrid,AutoSizer} from 'react-virtualized'
import { connect } from 'dva';
import {numberFormat,sortBySymbol} from '../../../../utils/RtUtils'
import {DIRECTION_TRANSLATER,DIRECTION_LONG,DIRECTION_SHORT} from '../../../../utils/RtConstant'
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
      dispatch,
      height,
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
      headerMap.set(columnCount+=1,"持仓"); // 3
      headerMap.set(columnCount+=1,"昨仓"); // 4
      headerMap.set(columnCount+=1,"冻结"); // 5
      headerMap.set(columnCount+=1,"均价"); // 6
      headerMap.set(columnCount+=1,"持仓盈亏"); // 7
      headerMap.set(columnCount+=1,"名称"); // 8
      headerMap.set(columnCount+=1,"网关"); // 9
      tableList.push(headerMap)
      columnCount+=1
    }


    const sortedList = list.sort(sortBySymbol)

    if(sortedList !== undefined){
      sortedList.forEach(item=>{
        let i = 0;
        const dataMap = new Map();
        dataMap.set(i,item.rtSymbol);
        dataMap.set(i+=1,item.accountID);
        dataMap.set(i+=1,item.direction);
        dataMap.set(i+=1,item.position);
        dataMap.set(i+=1,item.ydPosition);
        dataMap.set(i+=1,item.frozen);
        dataMap.set(i+=1,numberFormat(item.price,4));
        dataMap.set(i+=1,numberFormat(item.positionProfit,4));
        dataMap.set(i+=1,item.contractName);
        dataMap.set(i+=1,item.gatewayDisplayName);

        // =======不渲染的字段============
        dataMap.set(1001,item.gatewayID); 
        dataMap.set(1002,item.symbol); 
        dataMap.set(1003,item.exchange); 
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
          type: 'operation/subscribe',
          payload: {
            subscribeReq:{
              symbol:tableList[rowIndex].get(0),
            },
            gatewayIDs:[tableList[rowIndex].get(1001)],
          },
        });
      }

      const handleClick = ()=>{
        const {updateTradeForm} = this.props
        if(updateTradeForm!==null&&updateTradeForm!==undefined){
          updateTradeForm({
            symbol:tableList[rowIndex].get(1002)
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
          <div onClick={handleClick} onDoubleClick={handleDoubleClick} onFocus={() => undefined} className={`${styles.cell} ${styles.colorYellow} ${hoveredCellClass} ${styles.cursorPointer}`} key={key} style={style}>
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

      if(columnIndex === 7){
        if(tableList[rowIndex].get(columnIndex) > 0){
          return(
            <div className={`${styles.cell} ${hoveredCellClass} ${styles.colorBuy}`} key={key} style={style}>
              {tableList[rowIndex].get(columnIndex)}
            </div>
          )
        }
        
        if(tableList[rowIndex].get(columnIndex) < 0){
          return(
            <div className={`${styles.cell} ${hoveredCellClass} ${styles.colorSell}`} key={key} style={style}>
              {tableList[rowIndex].get(columnIndex)}
            </div>
          )
        }
      }

      if(columnIndex === 8){
        return (
          <div onDoubleClick={handleDoubleClick} onClick={handleClick} onFocus={() => undefined} className={`${styles.cell} ${hoveredCellClass} ${styles.cursorPointer}`} key={key} style={style}>
            {tableList[rowIndex].get(columnIndex)}
          </div>
        );
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
        case 8:
            return 150;
        case 9:
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
            rowHeight={25}
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
