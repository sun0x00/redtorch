
import React, { PureComponent } from 'react';
import { Card, Select,Tabs, Form ,Input } from 'antd';
import styles from './GatewayBoard.less';

const {TabPane} = Tabs;
const {Option} = Select
const FormItem = Form.Item;

@Form.create()
class GatewayForm extends PureComponent {
  constructor(props) {
    super(props);
    this.gatewayFormRef = React.createRef();
    this.state={stateGatewayType:undefined}
  }
  
  handleGatewayTypeChange = (key)=>{
    this.setState({stateGatewayType:key})
  }

  resetForm = () =>{
    const {form} = this.props;
    form.resetFields()
  }

  render() {
    const {
      form: { getFieldDecorator},
      editGateway
    } = this.props;

    const {stateGatewayType}=this.state


    let tmpGatewayID = "";
    let tmpGatewayDisplayName = "";
    let tmpGatewayClassName = "xyz.redtorch.gateway.ctp.CtpGateway";
    let tmpGatewayType="CTP";
    let tmpCtpSettingUserID = "";
    let tmpCtpSettingPassword = "";
    let tmpCtpSettingBrokerID = "";
    let tmpCtpSettingTdAddress = "";
    let tmpCtpSettingMdAddress = "";
    let tmpCtpSettingAuthCode = "";
    let tmpCtpSettingUserProductInfo = "";
    let tmpIbSettingHost="127.0.0.1"
    let tmpIbSettingPort=7496
    let tmpIbSettingClientID=0
    let tmpIbSettingAccountCode=""

    if(editGateway!==null&&editGateway!==undefined){
      tmpGatewayID = editGateway.gatewayID;
      tmpGatewayDisplayName = editGateway.gatewayDisplayName;
      tmpGatewayClassName = editGateway.gatewayClassName;

      if(stateGatewayType == null || stateGatewayType === undefined){
        tmpGatewayType = editGateway.gatewayType;
      }else{
        tmpGatewayType = stateGatewayType
      }


      if(tmpGatewayType==="CTP"&&editGateway.ctpSetting!=null&&editGateway.ctpSetting!==undefined){
          tmpCtpSettingUserID = editGateway.ctpSetting.userID;
          tmpCtpSettingPassword = editGateway.ctpSetting.password;
          tmpCtpSettingBrokerID = editGateway.ctpSetting.brokerID;
          tmpCtpSettingTdAddress = editGateway.ctpSetting.tdAddress;
          tmpCtpSettingMdAddress = editGateway.ctpSetting.mdAddress;
          tmpCtpSettingAuthCode = editGateway.ctpSetting.authCode;
          tmpCtpSettingUserProductInfo = editGateway.ctpSetting.userProductInfo;
      }else if(tmpGatewayType==="IB"&&editGateway.ibSetting!=null&&editGateway.ibSetting!==undefined){
          tmpIbSettingHost= editGateway.ibSetting.host
          tmpIbSettingPort= editGateway.ibSetting.port
          tmpIbSettingClientID= editGateway.ibSetting.clientID
          tmpIbSettingAccountCode= editGateway.ibSetting.accountCode
      }
 
    }else if(stateGatewayType != null && stateGatewayType !== undefined){
      tmpGatewayType = stateGatewayType
      if(tmpGatewayType==="CTP"){
        tmpGatewayClassName = "xyz.redtorch.gateway.ctp.CtpGateway";
      }if(tmpGatewayType==="IB"){
        tmpGatewayClassName = "xyz.redtorch.gateway.ib.IbGateway";
      }
    }

    return (
      <Card bordered={false} gutter={0}>
        <Form hideRequiredMark gutter={0}>
          <FormItem className={styles.formItem}>
            {getFieldDecorator('gatewayID',{
              initialValue: tmpGatewayID
            })(
              <Input disabled placeholder="网关ID" />
            )}
          </FormItem>
          <FormItem className={styles.formItem}>
            {getFieldDecorator('gatewayDisplayName',{
              rules: [
                {
                  required: true,
                  message: '请输入网关名称',
                },
              ],
              initialValue: tmpGatewayDisplayName
            })(
              <Input placeholder="网关名称" />
            )}
          </FormItem>
          <FormItem className={styles.formItem}>
            {getFieldDecorator('gatewayClassName',{
              rules: [
                {
                  required: true,
                  message: '请输入网关Java实现类',
                },
              ],
              initialValue: tmpGatewayClassName
            })(
              <Input placeholder="网关Java实现类" />
            )}
          </FormItem>

          <FormItem className={styles.formItem}>
            {getFieldDecorator('gatewayType',{
              initialValue: tmpGatewayType
            })(
              <Select
                disabled
              >
                <Option value="CTP">CTP</Option>
                <Option value="IB">IB</Option>
              </Select>)}
          </FormItem>
          <Tabs 
            defaultActiveKey={tmpGatewayType} 
            onChange={this.handleGatewayTypeChange}
          >
            <TabPane tab="CTP" key="CTP">
              <FormItem className={styles.formItem}>
                {getFieldDecorator('ctpSettingUserID',{
                    rules: [
                        {
                        required: true,
                        message: '请输入账户ID',
                        },
                    ],
                    initialValue: tmpCtpSettingUserID
                })(
                  <Input placeholder="账户ID" />
                )}
              </FormItem>

              <FormItem className={styles.formItem}>
                {getFieldDecorator('ctpSettingPassword',{
                    rules: [
                        {
                        required: true,
                        message: '请输入账户密码',
                        },
                    ],
                    initialValue: tmpCtpSettingPassword
                })(
                  <Input type='password' placeholder="密码" />
                )}
              </FormItem>
              
              <FormItem className={styles.formItem}>
                {getFieldDecorator('ctpSettingBrokerID',{
                    rules: [
                        {
                        required: true,
                        message: '请输入经纪商ID',
                        },
                    ],
                    initialValue: tmpCtpSettingBrokerID
                })(
                  <Input placeholder="经纪商ID" />
                )}
              </FormItem>
              
              <FormItem className={styles.formItem}>
                {getFieldDecorator('ctpSettingTdAddress',{
                    rules: [
                        {
                            required: true,
                            message: '请输入交易地址',
                        },{
                            pattern: /^(tcp:\/\/)(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(:\d{2,5})$/,
                            message: '地址不正确',
                        }
                    ],
                    initialValue: tmpCtpSettingTdAddress
                })(
                  <Input placeholder="交易地址" />
                )}
              </FormItem>

              <FormItem className={styles.formItem}>
                {getFieldDecorator('ctpSettingMdAddress',{
                    rules: [
                        {
                            required: true,
                            message: '请输入行情地址',
                        },{
                            pattern: /^(tcp:\/\/)(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(:\d{2,5})$/,
                            message: '地址不正确',
                        }
                        
                    ],
                    initialValue: tmpCtpSettingMdAddress
                })(
                  <Input placeholder="行情地址" />
                )}
              </FormItem>

              <FormItem className={styles.formItem}>
                {getFieldDecorator('ctpSettingAuthCode',{
                    initialValue: tmpCtpSettingAuthCode
                })(
                  <Input placeholder="授权码" />
                )}
              </FormItem>

              <FormItem className={styles.formItem}>
                {getFieldDecorator('ctpSettingUserProductInfo',{
                    initialValue: tmpCtpSettingUserProductInfo
                })(
                  <Input placeholder="用户产品信息" />
                )}
              </FormItem>
            </TabPane>
            <TabPane tab="IB" key="IB">
              <FormItem className={styles.formItem}>
                {getFieldDecorator('ibSettingHost',{
                    rules: [
                        {
                        required: true,
                        message: '请输入主机名',
                        },
                    ],
                    initialValue: tmpIbSettingHost
                })(
                  <Input placeholder="主机名" />
                )}
              </FormItem>
              <FormItem className={styles.formItem}>
                {getFieldDecorator('ibSettingPort',{
                    rules: [
                        {
                        required: true,
                        message: '请输入端口',
                        },
                    ],
                    initialValue: tmpIbSettingPort
                })(
                  <Input placeholder="ibSettingPort" />
                )}
              </FormItem>
              <FormItem className={styles.formItem}>
                {getFieldDecorator('ibSettingClientID',{
                    rules: [
                        {
                        required: true,
                        message: '请输入客户端ID',
                        },
                    ],
                    initialValue: tmpIbSettingClientID
                })(
                  <Input placeholder="客户端ID" />
                )}
              </FormItem>
              <FormItem className={styles.formItem}>
                {getFieldDecorator('ibSettingAccountCode',{
                    initialValue: tmpIbSettingAccountCode
                })(
                  <Input placeholder="账户代码" />
                )}
              </FormItem>
            </TabPane>
          </Tabs>
        </Form>
      </Card>
    );
  }
}

export default GatewayForm;


