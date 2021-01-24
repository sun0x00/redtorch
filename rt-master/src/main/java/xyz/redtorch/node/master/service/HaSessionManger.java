package xyz.redtorch.node.master.service;

import xyz.redtorch.node.master.po.UserPo;

public interface HaSessionManger {
    UserPo getUserPoByAuthToken(String freshAuthToken);
    UserPo userAuth(UserPo userPo);
    void freshAuthToken(String authToken);
    void removeAuthToken(String authToken);
}
