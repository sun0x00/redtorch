import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Button,Row, Table ,Col, Tag, Divider,Modal} from 'antd';
import GridContent from '@/components/PageHeaderWrapper/GridContent';
import styles from './GatewayBoard.less';
import GatewayForm from './GatewayForm'
import {sleep} from '../../utils/RtUtils'


const {confirm} = Modal

@connect(({login,gateway}) => ({
  login,gateway
}))
class Center extends PureComponent {
  
  constructor(props) {
    super(props);
    this.gatewayFormRef = React.createRef();
    this.state = { visible: false , editGateway:null, fetchGatewaysLoading: false}
  }
  

  componentDidMount() {
    this.fetchGateways()
  }

  
  handleGatewayEditModalOK(){
    const { dispatch,login } = this.props;
    const that = this;

    this.gatewayFormRef.current.validateFieldsAndScroll((err, values) => {
      let data;

      // 判断表单是否有错误
      if(err!=null){
        if(err.gatewayType!==undefined
        ||err.gatewayDisplayName!==undefined
        ||err.gatewayClassName!==undefined){
          return;
        }
      }

      // 如果是CTP接口,只判断CTP接口的错误
      if(values.gatewayType==='CTP'){
        // 如果是CTP接口,只判断CTP接口的错误
        if(err!=null){
          if(err.ctpSettingBrokerID!==undefined
          ||err.ctpSettingMdAddress!==undefined
          ||err.ctpSettingPassword!==undefined
          ||err.ctpSettingUserID!==undefined
          ||err.ctpSettingMdAddress!==undefined
          ||err.ctpSettingTdAddress!==undefined){
            return;
          }
        }

        data = {
          token:login.token,
          gatewayID:values.gatewayID,
          gatewayClassName:values.gatewayClassName,
          gatewayDisplayName:values.gatewayDisplayName,
          gatewayType:values.gatewayType,
          ctpSetting:{
            userID:values.ctpSettingUserID,
            password:values.ctpSettingPassword,
            brokerID:values.ctpSettingBrokerID,
            mdAddress:values.ctpSettingMdAddress,
            tdAddress:values.ctpSettingTdAddress,
            authCode:values.ctpSettingAuthCode,
            userProductInfo:values.ctpSettingUserProductInfo
          }
        }

      }else if(values.gatewayType==='IB'){
        // 如果是IB接口,只判断IB接口的错误
        if(err!=null){
          if(err.ibSettingHost!==undefined
          ||err.ibSettingPort!==undefined
          ||err.ibSettingClientID!==undefined){
            return;
          }
        }
        data = {
          token:login.token,
          gatewayID:values.gatewayID,
          gatewayClassName:values.gatewayClassName,
          gatewayDisplayName:values.gatewayDisplayName,
          gatewayType:values.gatewayType,
          ibsetting:{
            host:values.ibSettingHost,
            port:values.ibSettingPort,
            clientID:values.ibSettingClientID,
            accountCode:values.ibSettingAccountCode
          }
        }

      }

      dispatch({
        type: 'gateway/savaOrUpdateGateway',
        payload: data,
      });
      
      sleep(200).then(()=>{
        that.setState({ visible: false});
        that.fetchGateways()
      })
      
    });

  }

  handleGatewayEditModalCancel(){
    this.setState({ visible:false });
  }



  changeGatewayConnectStatus(gateway){
    const { dispatch,login } = this.props;
    const that = this;
    
    that.setState({ fetchGatewaysLoading: true});
    if(gateway.runtimeStatus){
      confirm({
        title: '确定要断开这个网关吗?',
        content: gateway.gatewayID,
        onOk() {
          dispatch({
            type: 'gateway/changeGatewayConnectStatus',
            payload: {
              token: login.token,
              gatewayID: gateway.gatewayID
            },
          });
          sleep(1000).then(()=>{
            that.setState({ visible: false, fetchGatewaysLoading: false});
            that.fetchGateways()
          })
          
        },
        onCancel() {
          that.setState({ fetchGatewaysLoading: false});
        },
      });
    }else{
      dispatch({
        type: 'gateway/changeGatewayConnectStatus',
        payload: {
          token:login.token,
          gatewayID: gateway.gatewayID
        },
      });
      sleep(1000).then(()=>{
        that.setState({ visible: false, fetchGatewaysLoading: false});
        that.fetchGateways()
      })
    }



  }

  deleteGateway(gateway){
    const { dispatch,login } = this.props;
    const that = this;
    confirm({
      title: '确定要删除这个网关吗?',
      content: gateway.gatewayID,
      onOk() {
        dispatch({
          type: 'gateway/deleteGateway',
          payload: {
            token: login.token,
            gatewayID: gateway.gatewayID
          },
        });
        
        sleep(100).then(()=>{
          that.setState({ visible: false});
          that.fetchGateways()
        })
      },
      onCancel() {
      },
    });

  }

  saveOrUpdateGateway(gateway){
    this.setState({ visible:true ,editGateway:gateway});
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
      gateway ,
    } = this.props;

    const { visible,editGateway,fetchGatewaysLoading} = this.state

    const gridLayout={
      xs: { span: 24 },
      sm: { span: 24 },
      md: { span: 22,offset:1},
      lg: { span: 20,offset:2},
    }
    
    const {Column} = Table;

    return (
      <GridContent className={styles.userCenter}>
        <Row gutter={24}>
          <Col {...gridLayout}>
            <Card>
              <Button type="primary" icon="plus" onClick={()=>this.saveOrUpdateGateway()}>新增</Button>
              <Button style={{marginLeft:10}} type="primary" icon="reload" loading={fetchGatewaysLoading} onClick={()=>this.fetchGateways()}>刷新状态</Button>
              <Table 
                dataSource={gateway.gateways}
                scroll={{x: 880}} 
                pagination={false} 
                rowKey="gatewayID"
              >
                <Column
                  title="网关状态"
                  dataIndex="runtimeStatus"
                  key="runtimeStatus"
                  width={120}
                  render={(text, record) => {
                    if(record.runtimeStatus){
                      return(
                        <span>
                          <Tag color="green">已连接</Tag>
                        </span>
                      );
                    }
                    return(
                      <span>
                        <Tag color="red">已断开</Tag>
                      </span>
                    );
                  }}
                />
                <Column
                  title="网关ID"
                  dataIndex="gatewayID"
                  key="gatewayID"
                  width={200}
                />
                <Column
                  title="网关名称"
                  dataIndex="gatewayDisplayName"
                  key="gatewayDisplayName"
                  width={160}
                />
                <Column
                  title="网关Java实现类"
                  dataIndex="gatewayClassName"
                  key="gatewayClassName"
                  width={200}
                />
                <Column
                  title="操作"
                  key="action"
                  width={200}
                  render={(text, record) => {
                    if(record.runtimeStatus){
                      return(
                        <span>
                          <a href="" onClick={e=>{e.preventDefault();this.changeGatewayConnectStatus(record)}}>断开</a>
                          <Divider type="vertical" />
                          <a href="" onClick={e=>{e.preventDefault();this.saveOrUpdateGateway(record)}}>修改</a>
                          <Divider type="vertical" />
                          <a href="" onClick={e=>{e.preventDefault();this.deleteGateway(record)}}>删除</a>
                        </span>
                      );
                    }
                    return(
                      <span>
                        <a href="" onClick={e=>{e.preventDefault();this.changeGatewayConnectStatus(record)}}>连接</a>
                        <Divider type="vertical" />
                        <a href="" onClick={e=>{e.preventDefault();this.saveOrUpdateGateway(record)}}>修改</a>
                        <Divider type="vertical" />
                        <a href="" onClick={e=>{e.preventDefault();this.deleteGateway(record)}}>删除</a>
                      </span>
                    );
                  }}
                />
              </Table>
            </Card>
            {visible &&  
            <Modal
              title="编辑网关"
              visible={visible}
              onOk={()=>this.handleGatewayEditModalOK()}
              onCancel={()=>this.handleGatewayEditModalCancel()}
              okText="确认"
              cancelText="取消"
            >
              <GatewayForm editGateway={editGateway} ref={this.gatewayFormRef} />
            </Modal>}
          </Col>
        </Row>
      </GridContent>
    );
  }
}

export default Center;
