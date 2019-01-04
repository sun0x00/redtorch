
import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Select, Form ,Input } from 'antd';
import styles from './DataRecordBoard.less';


const {Option} = Select
const FormItem = Form.Item;

@Form.create()
@connect(({login,dataRecord,gateway}) => ({
  login,dataRecord,gateway
}))
class SubscribeReqForm extends PureComponent {
  constructor(props) {
    super(props);
    this.gatewayFormRef = React.createRef();
  }
  
  componentDidMount() {
    this.fetchGateways()
  }

  resetForm = () =>{
    const {form} = this.props;
    form.resetFields()
  }

  fetchGateways(){
    const { dispatch,login } = this.props;

    dispatch({
      type: 'gateway/fetchGateways',
      payload: {
        token:login.token,
      },
    });
  }

  render() {
    const {
      form: { getFieldDecorator},
      editGateway,gateway
    } = this.props;


    let tmpId = "";
    let tmpGatewayID = "";
    let tmpRtSymbol = "";
    let tmpSymbol="";
    let tmpExchange = "";
    let tmpProductClass = "";

    if(editGateway!==null&&editGateway!==undefined){
      tmpId = editGateway.id;
      tmpGatewayID = editGateway.gatewayID;
      tmpRtSymbol = editGateway.rtSymbol;
      tmpSymbol = editGateway.symbol;
      tmpExchange = editGateway.exchange;
      tmpProductClass = editGateway.productClass;
    }

    const {gateways} = gateway

    const gatewayDomChildren = [];
    gateways.forEach(a=>{ 
      gatewayDomChildren.push(<Option key={a.gatewayID} value={a.gatewayID}>{`网关:[${a.gatewayDisplayName}] ID:[${a.gatewayID}]`}</Option>);
    });

    return (
      <Card bordered={false} gutter={0}>
        <Form hideRequiredMark gutter={0}>
          <FormItem
            className={styles.formItem}
            label={
              <span>
                ID
              </span>
            }
          >
            {getFieldDecorator('id',{
              initialValue: tmpId
            })(
              <Input disabled placeholder="ID" />
            )}
          </FormItem>
          {/* <FormItem className={styles.formItem}>
            {getFieldDecorator('rtSymbol',{
              initialValue: tmpRtSymbol
            })(
              <Input disabled placeholder="RT合约代码" />
            )}
          </FormItem> */}
          <FormItem
            className={styles.formItem}
            label={
              <span>
                合约代码
              </span>
            }
          >
            {getFieldDecorator('symbol',{
              rules: [
                {
                  required: true,
                  message: '请输入合约代码',
                },
              ],
              initialValue: tmpSymbol
            })(
              <Input placeholder="合约代码" />
            )}
          </FormItem>

          <FormItem
            className={styles.formItem}
            label={
              <span>
                交易所
              </span>
            }
          >
            {getFieldDecorator('exchange',{
              initialValue: tmpExchange
            })(
              <Select
                onChange={this.handleExchangeChange}
                showSearch
                placeholder='请选择交易所'
                optionFilterProp="children"
                filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
              >
                <Option value="SEHK">SEHK 港交所</Option>
                <Option value="HKFE">HKFE 香港期货交易所</Option>
                <Option value="IDEALPRO">IDEALPRO IB外汇ECN</Option>
                <Option value="SSE">SSE 上交所</Option>
                <Option value="SZSE">SZSE 深交所</Option>
                <Option value="CFFEX">CFFEX 中金所</Option>
                <Option value="SHFE">SHFE 上期所</Option>
                <Option value="CZCE">CZCE 郑商所</Option>
                <Option value="DCE">DCE 大商所</Option>
                <Option value="SGE">SGE 上金所</Option>
                <Option value="INE">INE 国际能源交易中心</Option>
                <Option value="SMART">SMART IB智能路由</Option>
                <Option value="NYMEX">NYMEX IB期货</Option>
                <Option value="GLOBEX">GLOBEX CME电子交易平台</Option>
                <Option value="CME">CME 芝商所</Option>
                <Option value="ICE">ICE 洲际交易所</Option>
                <Option value="LME">LME 伦敦金属交易所</Option>
                <Option value="OANDA">OANDA 外汇做市商</Option>
                <Option value="FXCM">FXCM 外汇做市商</Option>
              </Select>
            )}
          </FormItem>

          <FormItem
            className={styles.formItem}
            label={
              <span>
                产品类型
              </span>
            }
          >
            {getFieldDecorator('productClass',{
              initialValue: tmpProductClass
            })(
              <Select
                showSearch
                placeholder='请选择产品类型'
                optionFilterProp="children"
                filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
              >
                <Option value="FUTURES">期货</Option>
                <Option value="FOREX">外汇</Option>
                <Option value="EQUITY">股票</Option>
                <Option value="OPTION">期权</Option>
                <Option value="SPOT">现货</Option>
              </Select>
            )}
          </FormItem>

          <FormItem 
            className={styles.formItem}
            label={
              <span>
                网关ID
              </span>
            }
          >
            {getFieldDecorator('gatewayID',{
              initialValue: tmpGatewayID
            })(
              <Select
                showSearch
                dropdownMatchSelectWidth={false}
                placeholder='网关ID'
                optionFilterProp="children"
                filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
              >
                {gatewayDomChildren}
              </Select>
            )}
          </FormItem>
          
        </Form>
      </Card>
    );
  }
}

export default SubscribeReqForm;


