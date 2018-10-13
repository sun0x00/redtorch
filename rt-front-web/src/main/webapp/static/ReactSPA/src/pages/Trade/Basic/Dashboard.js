import React, { PureComponent } from 'react';
// import { connect } from 'dva';
import router from 'umi/router';
import { Card, Row, Col} from 'antd';
import GridContent from '@/components/PageHeaderWrapper/GridContent';
import styles from './Dashboard.less';
import TradeForm from './TradeForm'

class Center extends PureComponent {
  state = {
    newTags: [],
    inputValue: '',
  };

  onTabChange = key => {
    const { match } = this.props;
    switch (key) {
      case 'tradeBoard':
        router.push(`${match.url}/tradeBoard`);
        break;
      case 'accounts':
        router.push(`${match.url}/accounts`);
        break;
      case 'positions':
        router.push(`${match.url}/positions`);
        break;
      case 'orders':
        router.push(`${match.url}/orders`);
        break;
      case 'transactions':
        router.push(`${match.url}/transactions`);
        break;
      default:
        break;
    }
  };

  saveInputRef = input => {
    this.input = input;
  };

  handleInputChange = e => {
    this.setState({ inputValue: e.target.value });
  };

  handleInputConfirm = () => {
    const { state } = this;
    const { inputValue } = state;
    let { newTags } = state;
    if (inputValue && newTags.filter(tag => tag.label === inputValue).length === 0) {
      newTags = [...newTags, { key: `new-${newTags.length}`, label: inputValue }];
    }
    this.setState({
      newTags,
      inputValue: '',
    });
  };


  render() {
    const {
      listLoading,
      match,
      location,
      children
    } = this.props;

    const operationTabList = [
      {
        key: 'tradeBoard',
        tab: (
          <span>
            交易
            {/* <span style={{ fontSize: 14 }}>(8)</span> */}
          </span>
        ),
      },
      {
        key: 'orders',
        tab: (
          <span>
            委托
          </span>
        ),
      },
      {
        key: 'transactions',
        tab: (
          <span>
            成交
          </span>
        ),
      },
      {
        key: 'positions',
        tab: (
          <span>
            持仓
          </span>
        ),
      },
      {
        key: 'accounts',
        tab: (
          <span>
            账户
          </span>
        ),
      },
    ];

    return (
      <GridContent className={styles.userCenter}>
        <Row gutter={24}>
          <Col lg={7} md={24}>
            <TradeForm  />
          </Col>
          <Col lg={17} md={24}>
            <Card
              className={styles.tabsCard}
              bordered={false}
              tabList={operationTabList}
              activeTabKey={location.pathname.replace(`${match.path}/`, '')}
              onTabChange={this.onTabChange}
              loading={listLoading}
            >
              {children}
            </Card>
          </Col>
        </Row>
      </GridContent>
    );
  }
}

export default Center;
