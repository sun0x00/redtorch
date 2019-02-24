package xyz.redtorch.core.service;

import java.util.List;

import xyz.redtorch.core.entity.SubscribeReq;

public interface DataRecordService {
	List<SubscribeReq> getSubscribeReqs();

	void saveOrUpdateSubscribeReq(SubscribeReq subscribeReq);

	void deleteSubscribeReq(String id);
}
