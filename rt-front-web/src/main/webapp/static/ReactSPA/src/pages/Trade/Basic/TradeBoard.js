import React, { PureComponent } from 'react';
import { Row, Col } from 'antd';
import { connect } from 'dva';
import PositionsGrid from './Virtualized/PositionsGrid';
import OrdersGrid from './Virtualized/OrdersGrid';
import TicksGrid from './Virtualized/TicksGrid';


@connect(({order,tick,position,basicTradeForm}) => ({
  order,tick,position,basicTradeForm
}))
class Center extends PureComponent {
  constructor(props) {
    super(props);
    this.state={
       tableHeight: ((window.innerHeight - 290) / 3>180?(window.innerHeight - 290) / 3:180) || 180
    }
  }

  onWindowResize=()=>{
    this.setState({
      tableHeight: ((window.innerHeight - 290) / 3>180?(window.innerHeight - 290) / 3:180) || 180
    })
  }

  componentDidMount = () => {
    const { dispatch } = this.props;
    dispatch({
      type: 'order/fetchOrders',
      payload: {},
    });
    dispatch({
      type: 'tick/fetchTicks',
      payload: {},
    });
    dispatch({
      type: 'position/fetchPositions',
      payload: {},
    });
    dispatch({
      type: 'contract/fetchContracts',
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
      order,tick,position,    } = this.props;
    
    const {tableHeight} = this.state

    return (
      <Row gutter={0}>
        <Col span={24}>
          <Row>
            <Col span={24}>
              <h6>行情</h6>
              <TicksGrid updateTradeForm={this.updateTradeForm} list={tick.ticks} height={tableHeight} />
            </Col>
          </Row>
          <Row>
            <Col span={24}>
              <h6>持仓</h6>
              <PositionsGrid updateTradeForm={this.updateTradeForm} list={position.positions} height={tableHeight} />
            </Col>
          </Row>
          <Row>
            <Col span={24}>
              <h6>可撤</h6>
              <OrdersGrid updateTradeForm={this.updateTradeForm} list={order.workingOrders} height={tableHeight} />
            </Col>
          </Row>
        </Col>
      </Row>
    );
  }
}

export default Center;
