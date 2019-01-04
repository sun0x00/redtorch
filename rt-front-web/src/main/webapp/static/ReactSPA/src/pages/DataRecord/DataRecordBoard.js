import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Button,Row, Table ,Col, 
  Select, Divider,Modal} from 'antd';
import GridContent from '@/components/PageHeaderWrapper/GridContent';
import styles from './DataRecordBoard.less';
import SubscribeReqForm from './SubscribeReqForm'
import {sleep,uuidv4} from '../../utils/RtUtils'

const {confirm} = Modal

@connect(({login,dataRecord}) => ({
  login,dataRecord
}))
class Center extends PureComponent {
  
  constructor(props) {
    super(props);
    this.subscribeReqFormRef = React.createRef();
    this.state = { visible: false , editSubscribeReq:null, fetchSubscribeReqsLoading: false}
  }
  

  componentDidMount() {
    this.fetchSubscribeReqs()
  }

  
  handleSubscribeReqEditModalOK(){
    const { dispatch,login } = this.props;
    const that = this;

    this.subscribeReqFormRef.current.validateFieldsAndScroll((err, values) => {

      // 判断表单是否有错误
      if(err!=null){
        if(err.symbol!==undefined
          ||err.exchange!==undefined
          ||err.productClass!==undefined
          ||err.gatewayID!==undefined){
          return;
        }
      }
    
      const data = {
          token:login.token,
          symbol:values.symbol,
          exchange:values.exchange,
          rtSymbol:`${values.symbol}.${values.exchange}`,
          productClass:values.productClass,
          gatewayID:values.gatewayID,
        }


      dispatch({
        type: 'dataRecord/savaOrUpdateSubscribeReq',
        payload: data,
      });
      
      sleep(200).then(()=>{
        that.setState({ visible: false});
        that.fetchSubscribeReqs()
      })
      
    });

  }

  handleSubscribeReqEditModalCancel(){
    this.setState({ visible:false });
  }

  deleteSubscribeReq(subscribeReq){
    const { dispatch,login } = this.props;
    const that = this;
    confirm({
      title: '确定要删除这个记录吗?',
      content: subscribeReq.id,
      onOk() {
        dispatch({
          type: 'dataRecord/deleteSubscribeReq',
          payload: {
            token: login.token,
            subscribeReqID: subscribeReq.id
          },
        });
        
        sleep(100).then(()=>{
          that.setState({ visible: false});
          that.fetchSubscribeReqs()
        })
      },
      onCancel() {
      },
    });

  }

  saveOrUpdateSubscribeReq(subscribeReq){
    this.setState({ visible:true ,editSubscribeReq:subscribeReq});
  }

  fetchSubscribeReqs(){
    const { dispatch,login } = this.props;

    dispatch({
      type: 'dataRecord/fetchSubscribeReqs',
      payload: {
        token:login.token,
      },
    });
  }

  render() {
    const { 
      dataRecord
    } = this.props;

    const { visible,editSubscribeReq,fetchSubscribeReqsLoading} = this.state

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
              <Button type="primary" icon="plus" onClick={()=>this.saveOrUpdateSubscribeReq()}>新增</Button>
              <Button style={{marginLeft:10}} type="primary" icon="reload" loading={fetchSubscribeReqsLoading} onClick={()=>this.fetchSubscribeReqs()}>刷新</Button>
              <Table 
                dataSource={dataRecord.subscribeReqs}
                scroll={{x: 1000}} 
                pagination={false} 
                rowKey={()=>(uuidv4())}
              >
                <Column
                  title="ID"
                  dataIndex="id"
                  key="id"
                  width={180}
                />
                <Column
                  title="RT合约代码"
                  dataIndex="rtSymbol"
                  key="rtSymbol"
                  width={135}
                />
                <Column
                  title="合约代码"
                  dataIndex="symbol"
                  key="symbol"
                  width={135}
                />
                
                <Column
                  title="交易所"
                  dataIndex="exchange"
                  key="exchange"
                  width={135}
                />

                <Column
                  title="产品类型"
                  dataIndex="productClass"
                  key="productClass"
                  width={135}
                />

                <Column
                  title="网关ID"
                  dataIndex="gatewayID"
                  key="gatewayID"
                  width={180}
                />
                <Column
                  title="操作"
                  key="action"
                  width={100}
                  render={(text, record) => (
                    <span>
                      {/* <a href="" onClick={e=>{e.preventDefault();this.saveOrUpdateSubscribeReq(record)}}>修改</a>
                      <Divider type="vertical" /> */}
                      <a href="" onClick={e=>{e.preventDefault();this.deleteSubscribeReq(record)}}>删除</a>
                    </span>)}
                />
              </Table>
            </Card>
            {visible &&  
            <Modal
              title="新增"
              visible={visible}
              onOk={()=>this.handleSubscribeReqEditModalOK()}
              onCancel={()=>this.handleSubscribeReqEditModalCancel()}
              okText="确认"
              cancelText="取消"
            >
              <SubscribeReqForm editSubscribeReq={editSubscribeReq} ref={this.subscribeReqFormRef} />
            </Modal>}
          </Col>
        </Row>
      </GridContent>
    );
  }
}

export default Center;
