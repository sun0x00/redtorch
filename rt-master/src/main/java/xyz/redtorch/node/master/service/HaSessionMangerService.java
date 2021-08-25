package xyz.redtorch.node.master.service;

import xyz.redtorch.node.master.po.UserPo;

public interface HaSessionMangerService {
    UserPo getUserPoByAuthToken(String freshAuthToken);

    UserPo userAuth(UserPo userPo);

    void refreshAuthToken(String authToken);

    void removeSessionByAuthToken(String authToken);

    void removeSessionByUsername(String username);
}
