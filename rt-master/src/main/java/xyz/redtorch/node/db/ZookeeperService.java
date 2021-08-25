package xyz.redtorch.node.db;

import com.alibaba.fastjson.JSONObject;
import xyz.redtorch.common.util.ZookeeperUtils;

import java.util.List;

public interface ZookeeperService {

    ZookeeperUtils getZkUtils();

    List<JSONObject> find(String collection);

    JSONObject findById(String collection, String id);

    boolean update(String collection, String id, String data);

    boolean insert(String collection, String id, String data);

    boolean upsert(String collection, String id, String data);

    boolean deleteById(String collection, String id);

}
