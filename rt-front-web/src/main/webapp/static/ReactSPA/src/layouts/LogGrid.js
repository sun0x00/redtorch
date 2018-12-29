import React, { PureComponent } from 'react';
import {MultiGrid,AutoSizer} from 'react-virtualized'
import { connect } from 'dva';
import {timestampFormat} from '../utils/RtUtils'
import {DIRECTION_TRANSLATER,DIRECTION_LONG,DIRECTION_SHORT, DIRECTION_NET} from '../utils/RtConstant'
import styles from './LogGrid.less';

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

@connect(({logData}) => ({
  logData
}))
class Center extends PureComponent {
  // constructor(props) {
  //   super(props);
  // }

  componentDidMount = () => {
    const { dispatch } = this.props;

    dispatch({
      type: 'logData/fetchLogDatas',
      payload: {},
    });

  }


  render() {
    const {
      logData,
      height,
    } = this.props;

    const {logDatas} = logData

    let tableHeight;
    if(height === undefined){
      tableHeight = 375;
    }else{
      tableHeight = height;
    }

    let tableList =[]

    let columnCount = 0;
    {
      const headerMap = new Map();
      headerMap.set(columnCount,"时间"); // 0
      headerMap.set(columnCount+=1,"级别"); // 1
      headerMap.set(columnCount+=1,"线程"); // 2
      headerMap.set(columnCount+=1,"类名"); // 3
      headerMap.set(columnCount+=1,"内容"); // 4
      tableList.push(headerMap)
      columnCount+=1
    }
    tableList = tableList.concat(logDatas.reverse())
    
    const rowCount = tableList.length;
    
    const multiGridRef = React.createRef();

    const cellRenderer=({columnIndex, key, rowIndex, style})=>{


      // 第0行 表头
      if(rowIndex === 0){
        return (
          <div className={styles.headerCell} key={key} style={style}>
            <div>{tableList[rowIndex].get(columnIndex)}</div>
          </div>
        );

      }

      let rowLevelClass = "";

      if(tableList[rowIndex].level === 'WARN'){
        rowLevelClass = styles.warnLogCell
      }

      if(tableList[rowIndex].level === 'ERROR'){
        rowLevelClass = styles.errorLogCell
      }

      // 第0列 时间
      if(columnIndex === 0){
        return (
          <div className={`${styles.cell} ${rowLevelClass} ${styles.displayCenter} ${styles.cursorPointer}`} key={key} style={style}>
            <div>{timestampFormat(tableList[rowIndex].timestamp)}</div>
          </div>
        );
      }

      // 第1列 级别
      if(columnIndex === 1){
        return (
          <div className={`${styles.cell} ${rowLevelClass} ${styles.displayLeft}  ${styles.cursorPointer}`} key={key} style={style}>
            <div>{tableList[rowIndex].level}</div>
          </div>
        )
      }

      
      // 第2列 线程名称
      if(columnIndex === 2){
        return (
          <div className={`${styles.cell} ${rowLevelClass} ${styles.displayLeft}  ${styles.cursorPointer}`} key={key} style={style}>
            <div>{tableList[rowIndex].threadName}</div>
          </div>
        )
      }

      // 第3列 类名
      if(columnIndex === 3){
        return (
          <div className={`${styles.cell} ${rowLevelClass} ${styles.displayLeft}  ${styles.cursorPointer}`} key={key} style={style}>
            <div>{tableList[rowIndex].className}</div>
          </div>
        )
      }

      // 第4列 内容
      if(columnIndex === 4){
        return (
          <div className={`${styles.cell} ${rowLevelClass} ${styles.displayLeft}  ${styles.cursorPointer}`} key={key} style={style}>
            <div>{tableList[rowIndex].content}</div>
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
            return 200;
        case 1:
            return 120;
        case 2:
          return 220;
        case 3:
          return 250;
        case 4:
          return 3000;
        default:
          return 100;
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
