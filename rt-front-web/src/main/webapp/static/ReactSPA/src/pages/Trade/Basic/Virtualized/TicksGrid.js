import React, { PureComponent } from 'react';
import {MultiGrid,AutoSizer} from 'react-virtualized'
import { connect } from 'dva';
import {numberFormat,timestampFormat,sortBySymbol} from '../../../../utils/RtUtils'
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
  render() {
    const {
      list,
      dispatch,
      height,
    } = this.props;

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
      headerMap.set(columnCount+=1,"最新价"); // 1
      headerMap.set(columnCount+=1,"涨跌"); // 2
      headerMap.set(columnCount+=1,"卖一量"); // 3
      headerMap.set(columnCount+=1,"卖一价"); // 4
      headerMap.set(columnCount+=1,"买一价"); // 5
      headerMap.set(columnCount+=1,"买一量"); // 6
      headerMap.set(columnCount+=1,"成交量"); // 7
      headerMap.set(columnCount+=1,"持仓量"); // 8
      headerMap.set(columnCount+=1,"时间"); // 9
      headerMap.set(columnCount+=1,"开盘价"); // 10
      headerMap.set(columnCount+=1,"最高价"); // 11
      headerMap.set(columnCount+=1,"最低价"); // 12
      headerMap.set(columnCount+=1,"昨收价"); // 13
      headerMap.set(columnCount+=1,"网关"); // 14
      tableList.push(headerMap)
      columnCount+=1
    }

    const sortedList = list.sort(sortBySymbol)

    if(sortedList !== undefined){
      sortedList.forEach(item=>{
        let i = 0;
        const dataMap = new Map();
        dataMap.set(i,item.rtSymbol);
        dataMap.set(i+=1,numberFormat(item.lastPrice,4));
        let priceRatio = 0;
        if(item.preClosePrice!==undefined&&item.preClosePrice!==0&&!Number.isNaN(item.preClosePrice)){
          priceRatio = (item.lastPrice-item.preClosePrice)/item.preClosePrice
        }
        dataMap.set(i+=1,priceRatio);
        dataMap.set(i+=1,item.askVolume1);
        dataMap.set(i+=1,numberFormat(item.askPrice1,4));
        dataMap.set(i+=1,numberFormat(item.bidPrice1,4));
        dataMap.set(i+=1,item.bidVolume1);
        dataMap.set(i+=1,item.volume);
        dataMap.set(i+=1,item.openInterest);
        dataMap.set(i+=1,timestampFormat(item.dateTime.millis,'HH:mm:ss.SSS'));
        dataMap.set(i+=1,numberFormat(item.openPrice,4));
        dataMap.set(i+=1,numberFormat(item.highPrice,4));
        dataMap.set(i+=1,numberFormat(item.lowPrice,4));
        dataMap.set(i+=1,numberFormat(item.preClosePrice,4));
        dataMap.set(i+=1,item.gatewayDisplayName);
        // =======不渲染的字段============
        dataMap.set(1001,item.gatewayID); 
        dataMap.set(1002,item.symbol); 
        // ==============================
    
        tableList.push(dataMap)
      })
      
    }

    const rowCount = tableList.length;
    
    const multiGridRef = React.createRef();

    const cellRenderer=({columnIndex, key, rowIndex, style})=>{

      const handleDoubleClick=()=>{
        dispatch({
          type: 'operation/unsubscribe',
          payload: {
            rtSymbol:tableList[rowIndex].get(0),
            gatewayID:tableList[rowIndex].get(1001),
          },
        });
      }

      const handleClick=()=>{
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
          <div onClick={handleClick} onDoubleClick={handleDoubleClick} onFocus={() => undefined} className={`${styles.cell} ${styles.colorYellow} ${styles.cursorPointer}`} key={key} style={style}>
            {tableList[rowIndex].get(columnIndex)}
          </div>
        );
      }

      if(columnIndex === 1 || columnIndex === 2){
        if(tableList[rowIndex].get(2)>0){
          if(columnIndex === 2){
            return (
              <div className={`${styles.cell} ${styles.colorBuy}`} key={key} style={style}>
                {numberFormat(tableList[rowIndex].get(columnIndex)*100,4)}%
              </div>
            );
          }
          return (
            <div className={`${styles.cell} ${styles.colorBuy}`} key={key} style={style}>
              {tableList[rowIndex].get(columnIndex)}
            </div>
          );
        }if(tableList[rowIndex].get(2)<0){
          if(columnIndex === 2){
            return (
              <div className={`${styles.cell} ${styles.colorSell}`} key={key} style={style}>
                {numberFormat(tableList[rowIndex].get(columnIndex)*100,4)}%
              </div>
            );
          }
          return (
            <div className={`${styles.cell} ${styles.colorSell}`} key={key} style={style}>
              {tableList[rowIndex].get(columnIndex)}
            </div>
          );
        }
        if(columnIndex === 2){
          return (
            <div className={`${styles.cell} `} key={key} style={style}>
              {numberFormat(tableList[rowIndex].get(columnIndex)*100,4)}%
            </div>
          );
        }
        return (
          <div className={`${styles.cell} `} key={key} style={style}>
            {tableList[rowIndex].get(columnIndex)}
          </div>
        );
      }

      if(columnIndex === 4){
          return (
            <div className={`${styles.cell} ${styles.colorSell}`} key={key} style={style}>
              {tableList[rowIndex].get(columnIndex)}
            </div>
          );
      }
      if(columnIndex === 5){
        return (
          <div className={`${styles.cell} ${styles.colorBuy}`} key={key} style={style}>
            {tableList[rowIndex].get(columnIndex)}
          </div>
        );
      }
      if(columnIndex === 3 || columnIndex === 6){
        return (
          <div className={`${styles.cell} ${styles.colorCount}`} key={key} style={style}>
            {tableList[rowIndex].get(columnIndex)}
          </div>
        );
      }

      return (
        <div className={`${styles.cell} `} key={key} style={style}>
          {tableList[rowIndex].get(columnIndex)}
        </div>
      );
    }

    const getColumnWidth=({index}) => {
      switch (index) {
        case 0:
          return 130;
        case 14:
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
