package xyz.redtorch.web.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import xyz.redtorch.core.entity.SubscribeReq;
import xyz.redtorch.core.service.DataRecordService;
import xyz.redtorch.web.service.DataRecordWebService;

/**
 * @author sun0x00@gmail.com
 */
@Service
public class DataRecordWebServiceImpl implements DataRecordWebService {

	@Autowired
	private DataRecordService dataRecordService;
	
	@Override
	public List<SubscribeReq> getSubscribeReqs() {
		return dataRecordService.getSubscribeReqs();
	}

	@Override
	public void saveOrUpdateSubscribeReq(SubscribeReq subscribeReq) {
		dataRecordService.saveOrUpdateSubscribeReq(subscribeReq);
	}

	@Override
	public void deleteSubscribeReq(String id) {
		dataRecordService.deleteSubscribeReq(id);
	}

}
