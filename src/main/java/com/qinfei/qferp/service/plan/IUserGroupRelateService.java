package com.qinfei.qferp.service.plan;

import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserGroupRelate;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IUserGroupService
 * @Description 用户群组关系服务接口
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:04
 * @Version 1.0
 */
public interface IUserGroupRelateService {
    String CACHE_KEY = "userGroupRelate";


    UserGroup add(UserGroup userGroup);

    void insertUserGroupRelate (List<Map> file);

    UserGroup edit (UserGroup userGroup);

    void editUserId(Integer groupId1);
}
