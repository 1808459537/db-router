package com.zht.dbrouter;

import com.zht.dbrouter.annotation.DBRouter;

public interface IUserDao {
    @DBRouter(key = "userId")
    void insertUser(String req);

}
