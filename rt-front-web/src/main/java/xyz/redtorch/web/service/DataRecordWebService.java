package xyz.redtorch.web.service;

import java.util.List;

import xyz.redtorch.core.entity.SubscribeReq;

public interface DataRecordWebService {

	List<SubscribeReq> getSubscribeReqs();

	void saveOrUpdateSubscribeReq(SubscribeReq subscribeReq);

	void deleteSubscribeReq(String id);

}
