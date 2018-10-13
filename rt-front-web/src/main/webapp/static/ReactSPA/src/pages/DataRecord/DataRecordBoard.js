import React, { PureComponent } from 'react';
import Link from 'umi/link';
import {Row, Col} from 'antd';
import Exception from '@/components/Exception';
import GridContent from '@/components/PageHeaderWrapper/GridContent';
import styles from './DataRecordBoard.less';

class Center extends PureComponent {

  componentDidMount() {
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
