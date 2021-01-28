package com.qinfei.qferp.service.plan;

import com.qinfei.qferp.entity.plan.UserGroupType;

import java.util.List;

/**
 * @CalssName IUserGroupTypeService
 * @Description 用户群组类型服务接口
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:04
 * @Version 1.0
 */
public interface IUserGroupTypeService {
    String CACHE_KEY = "userGroupType";

    UserGroupType save(UserGroupType userGroupType);

    UserGroupType update(UserGroupType userGroupType);

    void del(Integer id);

    List<UserGroupType> listAllGroupType();
}
