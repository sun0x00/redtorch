import React, { PureComponent } from 'react';
import { connect } from 'dva';
import { Card, Row, Col, Tag, Button, Table, Collapse} from 'antd';
import GridContent from '@/components/PageHeaderWrapper/GridContent';
import {sleep,uuidv4} from '../../../utils/RtUtils'
import styles from './ZeusBoard.less';

const ButtonGroup = Button.Group;
const {Panel} = Collapse;

@connect(({zeus}) => ({
  zeus
}))
class Center extends PureComponent {

  componentDidMount = () => {
    this.fetchSrategyInfos()
  }

  
  fetchSrategyInfos = () => {
    const { dispatch } = this.props;

    dispatch({
      type: 'zeus/fetchSrategyInfos',
      payload: {},
    });
  }

  changeStrategyStatus=(payload)=>{
    const { dispatch } = this.props;
    const tmpPayload = payload
    dispatch({
      type: 'zeus/changeStrategyStatus',
      payload: tmpPayload,
    });

    sleep(1000).then(()=>{
      this.fetchSrategyInfos()
    })
  }

  renderTable = (dataMap) => {

    const keys = Object.keys(dataMap)

    const columns = []
    const data = {key:"just-row-key"}
    let scrollX=0
    keys.forEach(element=>{
      scrollX+=80
      columns.push(
        {
          title: element,
          dataIndex: element,
          key: element,
          width:80
        }
      )
      data[element] = dataMap[element]
    })
    const dataSource = [data]
    return <Table bordered scroll={{x:scrollX}} size='small' dataSource={dataSource} columns={columns} pagination={false} />

  }
  
  render() {
    const {zeus} = this.props

    const gridLayout={
      xs: { span: 24 },
      sm: { span: 24 },
      md: { span: 20,offset:2},
      lg: { span: 18,offset:3},
    }

    const titleSpanStyle={
      display:'inline-block',
      width:150,
      border:'1px solid #CCC',
      backgroundColor:'#F3F3F3',
      paddingLeft:5,
      marginLeft:3
    }

    const panelList = [];
    let i = 0;
    zeus.strategyInfos.forEach(strategyInfo => {

      const paramColumns = [{
          title: "参数名称",
          dataIndex: "name",
          key:  "name",
        },{
          title: "参数值",
          dataIndex: "value",
          key:  "value",
      }]
  
      const varColumns = [{
          title: "变量名称",
          dataIndex: "name",
          key:  "name",
        },{
          title: "变量值",
          dataIndex: "value",
          key:  "value",
      }]
  
      const paramDataList = []
      const paramKeys = Object.keys(strategyInfo.paramMap)
      paramKeys.forEach(element=>{
        paramDataList.push(
          {
            name: element,
            value: strategyInfo.paramMap[element],
          }
        )
      })

      const varDataList = []
      const varKeys = Object.keys(strategyInfo.varMap)
      varKeys.forEach(element=>{
        varDataList.push(
          {
            name: element,
            value: strategyInfo.varMap[element],
          }
        )
      })
  
      
      i+=1
      const panel = (
        <Panel 
          header={
            <Row>
              <Col span={6}>
                <div><span>名称:</span><span style={titleSpanStyle}>{strategyInfo.strategyName}</span></div>
              </Col>
              <Col span={6}>
                <div><span>ID:</span><span style={titleSpanStyle}>{strategyInfo.strategyID}</span></div>
              </Col>
              <Col span={8} offset={4}>
                <div style={{float:'right',paddingRight:10}}>
                  {strategyInfo.isLoaded?
                    <Tag color="green">已加载</Tag>
                    :
                    <Tag color="red">未加载</Tag>
                  }
                  {strategyInfo.initStatus?
                    <Tag color="green">已初始化</Tag>
                    :
                    <Tag color="red">未初始化</Tag>
                  }
                  {strategyInfo.trading?
                    <Tag color="green">已启动</Tag>
                    :
                    <Tag color="red">未启动</Tag>
                  }
                </div>
              </Col>
            </Row>
          } 
          key={`${i}`}
        >
          <Card style={{marginTop:20}}>
            <ButtonGroup size='small' style={{float:'right',marginRight:10,zIndex:999}}>
              <Button icon="up-circle" disabled={(!strategyInfo.isLoaded)||strategyInfo.initStatus} onClick={()=>this.changeStrategyStatus({actionType:'init',strategyID:strategyInfo.strategyID})}>初始化</Button>
              <Button icon="play-circle" disabled={(!strategyInfo.isLoaded)||strategyInfo.trading} onClick={()=>this.changeStrategyStatus({actionType:'start',strategyID:strategyInfo.strategyID})}>启动</Button>
              <Button type="danger" disabled={!strategyInfo.trading} onClick={()=>this.changeStrategyStatus({actionType:'stop',strategyID:strategyInfo.strategyID})} icon="stop">停止</Button>
              <Button type="danger" disabled={!strategyInfo.isLoaded} onClick={()=>this.changeStrategyStatus({actionType:'reload',strategyID:strategyInfo.strategyID})} icon="reload">重新加载</Button>
            </ButtonGroup>
            <Row>
              <Col span={10}>
                <h3>参数</h3>
                <Table bordered size='small' rowKey={()=>(uuidv4())} dataSource={paramDataList} columns={paramColumns} pagination={false} />
              </Col>
              <Col span={10} offset={4}>
                <h3>变量</h3>
                <Table bordered size='small' rowKey={()=>(uuidv4())} dataSource={varDataList} columns={varColumns} pagination={false} />
              </Col>
            </Row>
          </Card>
        </Panel>
      );
      panelList.push(panel)
    });



    return (
      <GridContent className={styles.userCenter}>
        <Row gutter={24}>
          <Col {...gridLayout}>
            <Card>
              <ButtonGroup>
                <Button icon="up-circle" onClick={()=>this.changeStrategyStatus({actionType:'initAll'})}>全部初始化</Button>
                <Button icon="play-circle" onClick={()=>this.changeStrategyStatus({actionType:'startAll'})}>全部启动</Button>
                <Button type="danger" icon="stop" onClick={()=>this.changeStrategyStatus({actionType:'stopAll'})}>全部停止</Button>
                <Button type="danger" icon="reload" onClick={()=>this.changeStrategyStatus({actionType:'reloadAll'})}>全部重新加载</Button>
                <Button icon="sync" onClick={()=>this.fetchSrategyInfos()}>刷新状态</Button>
              </ButtonGroup>
            </Card>

            <Collapse>
              {panelList}
            </Collapse>
          </Col>
        </Row>
      </GridContent>
    );
  }
}

export default Center;
