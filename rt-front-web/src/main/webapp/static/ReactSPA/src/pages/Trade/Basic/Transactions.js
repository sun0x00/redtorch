import React, { PureComponent } from 'react';
import { connect } from 'dva';
import TransactionsTable from './Tables/TransactionsTable';

@connect(({ tick,basicTradeForm,trade }) => ({
  tick,basicTradeForm,trade,
}))
class Center extends PureComponent {
  constructor(props) {
    super(props);
    this.state={
      tableHeight: ((window.innerHeight - 320) > 520?(window.innerHeight - 320):520) || 520
    }
  }

  onWindowResize=()=>{
    this.setState({
      tableHeight: ((window.innerHeight - 320) > 520?(window.innerHeight - 320):520) || 520
    })
  }

  componentDidMount = () => {
    const { dispatch } = this.props;
    
    dispatch({
      type: 'trade/fetchTrades',
      payload: {},
    });

    window.addEventListener('resize', this.onWindowResize)
  }

  componentWillUnmount = () =>{
      window.removeEventListener('resize', this.onWindowResize)
  }

  updateTradeForm = (payload) =>{

    const {symbol} = payload

    const {tick,basicTradeForm,dispatch} = this.props

    dispatch({
      type: 'basicTradeForm/update',
      payload: {
        fuzzySymbol:symbol,
      },
    });
    if(basicTradeForm.form!=null&&basicTradeForm.form!==undefined){
      basicTradeForm.form.setFieldsValue({
        fuzzySymbol:symbol,
      });
    }
    dispatch({
      type: 'basicTradeForm/updateTick',
      payload: tick.ticks,
    });
    
  }
  

  render() {
    const {
      trade
    } = this.props;
    
    const {tableHeight} = this.state;
  
    return (
      <TransactionsTable updateTradeForm={this.updateTradeForm} scroll={{y:tableHeight}} pagination={{pageSize: 50}} list={trade.trades} />
    );
  }
}

export default Center;
