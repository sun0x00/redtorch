import React, { PureComponent } from 'react';
import router from 'umi/router';
import { Tabs, Row, Col} from 'antd';
import GridContent from '@/components/PageHeaderWrapper/GridContent';
import styles from './Dashboard.less';
import TradeForm from './TradeForm'

const {TabPane} = Tabs

class Center extends PureComponent {

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

  render() {
    const {
      listLoading,
      match,
      location,
      children
    } = this.props;

    return (
      <GridContent className={styles.userCenter}>
        <Row gutter={10}>
          <Col xxl={5} lg={6} md={24}>
            <TradeForm  />
          </Col>
          <Col xxl={19} lg={18} md={24} style={{background: '#FFF'}}>
            <Tabs
              defaultActiveKey={location.pathname.replace(`${match.path}/`, '')}
              onChange={this.onTabChange}
              animated={false}
              loading={listLoading}
              size='small'
            >
              <TabPane tab="交易" key="tradeBoard">{children}</TabPane>
              <TabPane tab="委托" key="orders">{children}</TabPane>
              <TabPane tab="成交" key="transactions">{children}</TabPane>
              <TabPane tab="持仓" key="positions">{children}</TabPane>
              <TabPane tab="账户" key="accounts">{children}</TabPane>
            </Tabs>
          </Col>
        </Row>
      </GridContent>
    );
  }
}

export default Center;
