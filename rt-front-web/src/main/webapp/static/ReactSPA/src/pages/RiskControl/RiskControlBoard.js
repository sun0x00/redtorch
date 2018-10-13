import React, { PureComponent } from 'react';
import { connect } from 'dva';
import Link from 'umi/link';
import router from 'umi/router';
import { Card, Row, Col, Icon, Avatar, Tag, Divider, Spin, Input } from 'antd';
import Exception from '@/components/Exception';
import GridContent from '@/components/PageHeaderWrapper/GridContent';
import styles from './RiskControlBoard.less';

class Center extends PureComponent {

  componentDidMount() {
    const { dispatch } = this.props;
  }

  render() {

    return (
      <GridContent className={styles.userCenter}>
        <Row gutter={24}>
          <Col lg={24} md={24}>
            <Exception type="404" style={{ minHeight: 500, height: '100%' }} linkElement={Link} />
          </Col>
        
        </Row>
      </GridContent>
    );
  }
}

export default Center;
