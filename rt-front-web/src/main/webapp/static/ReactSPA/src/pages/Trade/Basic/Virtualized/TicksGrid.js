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
  constructor(props) {
    super(props);
    this.state={
      // hoveredColumnIndex: null,
      // hoveredRowIndex: null,
      clickedRowIndex: null
    }
 }

  render() {
    const {
      list,
      dispatch,
      height,
    } = this.props;

    const {
      // hoveredRowIndex,
      clickedRowIndex
    } = this.state;

    
    this.state={
       // hoveredColumnIndex: null,
      //  hoveredRowIndex: null,
       clickedRowIndex: null
    }

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
      headerMap.set(columnCount,"产品"); // 0
      headerMap.set(columnCount+=1,"买卖价格"); // 1
      headerMap.set(columnCount+=1,"买卖挂单"); // 2
      headerMap.set(columnCount+=1,"最新价"); // 3
      headerMap.set(columnCount+=1,"量"); // 4
      headerMap.set(columnCount+=1,"持仓"); // 5
      headerMap.set(columnCount+=1,"时间"); // 6
      headerMap.set(columnCount+=1,"涨跌停"); // 7
      headerMap.set(columnCount+=1,"开收价格"); // 8
      headerMap.set(columnCount+=1,"高低价格"); // 9
      headerMap.set(columnCount+=1,"昨结"); // 10
      headerMap.set(columnCount+=1,"网关"); // 11
      tableList.push(headerMap)
      columnCount+=1
    }

    list.sort(sortBySymbol).forEach(element => {

      const newElement = element

      newElement.priceRatio = 0;
      if(element.preClosePrice!==undefined&&element.preClosePrice!==0&&!Number.isNaN(element.preClosePrice)){
        newElement.priceRatio = (element.lastPrice-element.preClosePrice)/element.preClosePrice
      }

      newElement.dateTimeStr = timestampFormat(element.dateTime.millis,'HH:mm:ss.SSS')
      tableList.push(newElement)

    })


    const rowCount = tableList.length;
    
    const multiGridRef = React.createRef();

    const cellRenderer=({columnIndex, key, rowIndex, style})=>{

      // const hoveredCellClass = rowIndex === hoveredRowIndex ? styles.hoveredCell : '';
      const hoveredCellClass = rowIndex === clickedRowIndex ? styles.hoveredCell : '';

      const handleDoubleClick=()=>{
        dispatch({
          type: 'operation/unsubscribe',
          payload: {
            rtSymbol:tableList[rowIndex].rtSymbol,
            gatewayID:tableList[rowIndex].gatewayID,
          },
        });
      }

      const handleClick=()=>{
        const {updateTradeForm} = this.props
        if(updateTradeForm!==null&&updateTradeForm!==undefined){
          updateTradeForm({
            symbol:tableList[rowIndex].symbol
          })
        }
        if(rowIndex!==0){
          this.setState({clickedRowIndex:rowIndex})
        }
      }


      // 第0行 表头
      if(rowIndex === 0){
        return (
          <div className={styles.headerCell} key={key} style={style}>
            <div>{tableList[rowIndex].get(columnIndex)}</div>
          </div>
        );

      }

      // 第0列 产品信息
      if(columnIndex === 0){
        return (
          <div onClick={handleClick} onDoubleClick={handleDoubleClick} onFocus={() => undefined} className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight} ${styles.cursorPointer}`} key={key} style={style}>
            <div className={`${styles.colorYellow}`}>{tableList[rowIndex].rtSymbol}<br /></div>
            <div>{tableList[rowIndex].contractName}</div>
          </div>
        );
      }

      // 第1列 买卖1价格
      if(columnIndex === 1){
        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight}`} key={key} style={style}>
            <div className={`${styles.colorSell}`}><span style={INLINE_LABEL_STYLE}>卖1：</span>{numberFormat(tableList[rowIndex].askPrice1,4)}<br /></div>
            <div className={`${styles.colorBuy}`}><span style={INLINE_LABEL_STYLE}>买1：</span>{numberFormat(tableList[rowIndex].bidPrice1,4)}</div>
          </div>
        )
      }
      
      // 第2列 买卖1量
      if(columnIndex === 2){
        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight} ${styles.colorCount}`} key={key} style={style}>
            <div><span style={INLINE_LABEL_STYLE}>卖1量：</span>{tableList[rowIndex].askVolume1}<br /></div>
            <div><span style={INLINE_LABEL_STYLE}>买1量：</span>{tableList[rowIndex].bidVolume1}</div>
          </div>
        )
      }

      // 第3列 最新价格
      if(columnIndex === 3){
        if(tableList[rowIndex].priceRatio>0){
          return (
            <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight} ${styles.colorBuy}`} key={key} style={style}>
              <div>{numberFormat(tableList[rowIndex].lastPrice,4)}<br /></div>
              <div>{numberFormat(tableList[rowIndex].priceRatio*100,2)}%</div>
            </div>
          )
        }
        
        if(tableList[rowIndex].priceRatio<0){
          return (
            <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight} ${styles.colorSell}`} key={key} style={style}>
              <div>{numberFormat(tableList[rowIndex].lastPrice,4)}<br /></div>
              <div>{numberFormat(tableList[rowIndex].priceRatio*100,2)}%</div>
            </div>
          )
        }

        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight}`} key={key} style={style}>
            <div>{numberFormat(tableList[rowIndex].lastPrice,4)}<br /></div>
            <div>{numberFormat(tableList[rowIndex].priceRatio*100,2)}%</div>
          </div>
        )
      }

      // 第4列 量
      if(columnIndex === 4){
        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight}`} key={key} style={style}>
            <div className={`${styles.colorCount}`}><span style={INLINE_LABEL_STYLE}>成交量：</span>{tableList[rowIndex].lastVolume}<br /></div>
            <div><span style={INLINE_LABEL_STYLE}>总成交：</span>{tableList[rowIndex].volume}</div>
          </div>
        )
      }

      // 第5列 持仓
      if(columnIndex === 5){
        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight}`} key={key} style={style}>
            <div><span style={INLINE_LABEL_STYLE}>持仓：</span>{tableList[rowIndex].openInterest}<br /></div>
            <div><span style={INLINE_LABEL_STYLE}>日增：</span>{tableList[rowIndex].openInterest-tableList[rowIndex].preOpenInterest}</div>
          </div>
        )
      }

      // 第6列 时间
      if(columnIndex === 6){
        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayCenter}`} key={key} style={style}>
            {tableList[rowIndex].dateTimeStr}
          </div>
        )
      }

      // 第7列 涨跌停
      if(columnIndex === 7){
        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight}`} key={key} style={style}>
            <div className={`${styles.colorBuy}`}><span style={INLINE_LABEL_STYLE}>涨停：</span>{numberFormat(tableList[rowIndex].upperLimit,4)}<br /></div>
            <div className={`${styles.colorSell}`}><span style={INLINE_LABEL_STYLE}>跌停：</span>{numberFormat(tableList[rowIndex].lowerLimit,4)}</div>
          </div>
        )
      }

      // 第8列 开收价格
      if(columnIndex === 8){

        if(tableList[rowIndex].openPrice>tableList[rowIndex].preSettlePrice){
          return (
            <div className={`${styles.cell} ${hoveredCellClass} ${styles.displayRight}`} key={key} style={style}>
              <div className={`${styles.colorBuy}`}><span style={INLINE_LABEL_STYLE}>开盘：</span>{numberFormat(tableList[rowIndex].openPrice,4)}<br /></div>
              <div><span style={INLINE_LABEL_STYLE}>昨收：</span>{numberFormat(tableList[rowIndex].preClosePrice,4)}</div>
            </div>
          )
        }

        if(tableList[rowIndex].openPrice<tableList[rowIndex].preSettlePrice){
          return (
            <div className={`${styles.cell} ${hoveredCellClass} ${styles.displayRight}`} key={key} style={style}>
              <div className={`${styles.colorSell}`}><span style={INLINE_LABEL_STYLE}>开盘：</span>{numberFormat(tableList[rowIndex].openPrice,4)}<br /></div>
              <div><span style={INLINE_LABEL_STYLE}>昨收：</span>{numberFormat(tableList[rowIndex].preClosePrice,4)}</div>
            </div>
          )
        }

        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight}`} key={key} style={style}>
            <div><span style={INLINE_LABEL_STYLE}>开盘：</span>{numberFormat(tableList[rowIndex].openPrice,4)}<br /></div>
            <div><span style={INLINE_LABEL_STYLE}>昨收：</span>{numberFormat(tableList[rowIndex].preClosePrice,4)}</div>
          </div>
        )

      }

      // 第9列 高低
      if(columnIndex === 9){

        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight}`} key={key} style={style}>
            { (tableList[rowIndex].highPrice>tableList[rowIndex].preSettlePrice) &&
              <div className={`${styles.colorBuy}`}><span style={INLINE_LABEL_STYLE}>最高：</span>{numberFormat(tableList[rowIndex].highPrice,4)}<br /></div>
            }
            { (tableList[rowIndex].highPrice<tableList[rowIndex].preSettlePrice) &&
              <div className={`${styles.colorSell}`}><span style={INLINE_LABEL_STYLE}>最高：</span>{numberFormat(tableList[rowIndex].highPrice,4)}<br /></div>
            }
            { (tableList[rowIndex].highPrice === tableList[rowIndex].preSettlePrice) &&
              <div><span style={INLINE_LABEL_STYLE}>最高：</span>{numberFormat(tableList[rowIndex].highPrice,4)}<br /></div>
            }
            { (tableList[rowIndex].lowPrice>tableList[rowIndex].preSettlePrice) &&
              <div className={`${styles.colorBuy}`}><span style={INLINE_LABEL_STYLE}>最低：</span>{numberFormat(tableList[rowIndex].lowPrice,4)}</div>
            }
            { (tableList[rowIndex].lowPrice<tableList[rowIndex].preSettlePrice) &&
              <div className={`${styles.colorSell}`}><span style={INLINE_LABEL_STYLE}>最低：</span>{numberFormat(tableList[rowIndex].lowPrice,4)}</div>
            }
            { (tableList[rowIndex].lowPrice === tableList[rowIndex].preSettlePrice) &&
              <div><span style={INLINE_LABEL_STYLE}>最低：</span>{numberFormat(tableList[rowIndex].lowPrice,4)}</div>
            }
          </div>
        )

      }

      // 第10列 昨结价格
      if(columnIndex === 10){
        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayRight}`} key={key} style={style}>
            {numberFormat(tableList[rowIndex].preSettlePrice,4)}
          </div>
        )
      }

      // 第11列 网关
      if(columnIndex === 11){
        return (
          <div className={`${styles.cell} ${hoveredCellClass}  ${styles.displayCenter}`} key={key} style={style}>
            {tableList[rowIndex].gatewayDisplayName}
          </div>
        )
      }

      return (
        <div />
      );
    }

    const getColumnWidth=({index}) => {
      switch (index) {
        case 0:
          return 150;
        case 2:
          return 120;
        case 10:
          return 110;
        case 11:
          return 180;
        default:
          return 140;
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
