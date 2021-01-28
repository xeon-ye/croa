package com.qinfei.qferp.mapper.plan;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.plan.UserGroup;

import java.util.List;
import java.util.Map;

/**
 * @CalssName UserGroupMapper
 * @Description 用户群组表
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:53
 * @Version 1.0
 */
public interface UserGroupRelateMapper extends BaseMapper<UserGroup, Integer> {



    void insertUserGroupRelate(List<Map> list);

    void editUserId(Integer groupId1);

    void updateGroup(UserGroup userGroup);
}
