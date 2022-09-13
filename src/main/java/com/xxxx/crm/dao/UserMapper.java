package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.vo.User;

public interface UserMapper extends BaseMapper<User,Integer> {

    //通过用户名称查询数据
    public User queryUserByName(String name);
}