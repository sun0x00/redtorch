import React, { PureComponent } from 'react';
import { connect } from 'dva';
import OrdersTable from './Tables/OrdersTable';

@connect(({tick,basicTradeForm,order}) => ({
  tick,basicTradeForm,order
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
      type: 'order/fetchOrders',
      payload: {},
    });

    window.addEventListener('resize', this.onWindowResize)
  }

  componentWillUnmount = () =>{
      window.removeEventListener('resize', this.onWindowResize)
  }

  updateTradeForm = (payload) =>{

    const {symbol} = payload

    const {basicTradeForm,dispatch,tick} = this.props

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
      order,
    } = this.props;

    const {tableHeight} = this.state;
    
    return (
      <OrdersTable updateTradeForm={this.updateTradeForm} scroll={{x: 1150,y:tableHeight}} pagination={{pageSize: 50}} list={order.orders} />
    );
  }
}

export default Center;
