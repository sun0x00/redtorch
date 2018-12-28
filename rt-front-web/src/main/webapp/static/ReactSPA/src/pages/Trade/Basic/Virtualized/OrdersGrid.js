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
      //  hoveredRowIndex: null,
      clickedRowIndex: null
    }
 }

  render() {
    const {
      list,
      height,
      dispatch
    } = this.props;
    const {
      // hoveredRowIndex,
      clickedRowIndex
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
      headerMap.set(columnCount,"产品"); // 0 
      headerMap.set(columnCount+=1,"账户"); // 1
      headerMap.set(columnCount+=1,"方向"); // 2 
      headerMap.set(columnCount+=1,"价格"); // 3
      headerMap.set(columnCount+=1,"量"); // 4
      headerMap.set(columnCount+=1,"状态"); // 5
      headerMap.set(columnCount+=1,"时间"); // 6
      tableList.push(headerMap)
      columnCount+=1
    }


    list.sort(sortOrderByTimeAndID).forEach(element => {
      const newElement = element
      // 预留
      tableList.push(newElement)
    })


    const rowCount = tableList.length;
    
    const multiGridRef = React.createRef();

    const cellRenderer=({columnIndex, key, rowIndex, style})=>{

      // let hoveredCellClass = rowIndex === hoveredRowIndex ? styles.hoveredCell : '';
      const hoveredCellClass = rowIndex === clickedRowIndex ? styles.hoveredCell : '';

      
      const handleDoubleClick=()=>{
        dispatch({
          type: 'operation/cancelOrder',
          payload: {
            rtOrderID:tableList[rowIndex].rtOrderID
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
          <div onClick={handleClick} onDoubleClick={handleDoubleClick} onFocus={() => undefined} className={`${styles.cell}  ${styles.displayRight} ${hoveredCellClass} ${styles.cursorPointer}`} key={key} style={style}>
            <div className={`${styles.colorYellow}`}>{tableList[rowIndex].rtSymbol}<br /></div>
            <div>{tableList[rowIndex].contractName}</div>
          </div>
        );
      }
      

      // 第1列 账户
      if(columnIndex === 1){
        return (
          <div className={`${styles.cell}  ${styles.displayRight}  ${hoveredCellClass}`} key={key} style={style}>
            <div>{tableList[rowIndex].accountID}<br /></div>
            <div style={{color:"#BBB"}}>{tableList[rowIndex].gatewayDisplayName}</div>
          </div>
        )
      }

      // 第2列 方向
      if(columnIndex === 2){
        return(
          <div className={`${styles.cell}  ${styles.displayRight}  ${hoveredCellClass}`} key={key} style={style}>
            {
              tableList[rowIndex].direction === DIRECTION_LONG &&
              <div className={`${styles.colorBuy}`}><span style={INLINE_LABEL_STYLE}>方向：</span>{DIRECTION_TRANSLATER.get(tableList[rowIndex].direction)}<br /></div>
            }
            
            {
              tableList[rowIndex].direction === DIRECTION_SHORT &&
              <div className={`${styles.colorSell}`}><span style={INLINE_LABEL_STYLE}>方向：</span>{DIRECTION_TRANSLATER.get(tableList[rowIndex].direction)}<br /></div>
            }
            {
              (tableList[rowIndex].direction !== DIRECTION_LONG && tableList[rowIndex].direction !== DIRECTION_SHORT) &&
              <div><span style={INLINE_LABEL_STYLE}>方向：</span>{DIRECTION_TRANSLATER.get(tableList[rowIndex].direction)}<br /></div>
            }
            <div><span style={INLINE_LABEL_STYLE}>开平：</span>{OFFSET_TRANSLATER.get(tableList[rowIndex].offset)}</div>
          </div>
        )
      }

      // 第3列 价格
      if(columnIndex === 3){
        return (
          <div className={`${styles.cell}  ${styles.displayRight} ${hoveredCellClass}`} key={key} style={style}>
            {numberFormat(tableList[rowIndex].price,4)}
          </div>
        )
      }

      // 第4列 量
      if(columnIndex === 4){
        return (
          <div className={`${styles.cell}  ${styles.displayRight} ${styles.colorCount} ${hoveredCellClass}`} key={key} style={style}>
            <div><span style={INLINE_LABEL_STYLE}>委托：</span>{tableList[rowIndex].totalVolume}<br /></div>
            <div><span style={INLINE_LABEL_STYLE}>成交：</span>{tableList[rowIndex].tradedVolume}</div>
          </div>
        )
      }
      // 第5列 状态
      if(columnIndex === 5){
        return (
          <div className={`${styles.cell}  ${styles.displayRight}  ${hoveredCellClass}`} key={key} style={style}>
            <div><span style={INLINE_LABEL_STYLE}>状态：</span>{STATUS_TRANSLATER.get(tableList[rowIndex].status)}<br /></div>
            <div><span style={INLINE_LABEL_STYLE}>编号：</span>{tableList[rowIndex].orderID}</div>
          </div>
        )
      }
      
      // 第6列 时间
      if(columnIndex === 6){
        return (
          <div className={`${styles.cell}  ${styles.displayRight}  ${hoveredCellClass}`} key={key} style={style}>
            <div><span style={INLINE_LABEL_STYLE}>委托：</span>{tableList[rowIndex].orderTime}<br /></div>
            <div><span style={INLINE_LABEL_STYLE}>更新：</span>{tableList[rowIndex].updateTime}</div>
          </div>
        )
      }

      return (
        <div />
      )

    }

    const getColumnWidth=({index}) => {
      switch (index) {
        case 0:
          return 150;
        case 1:
          return 180;
        case 2:
          return 120;
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
