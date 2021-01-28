package com.qinfei.qferp.service.plan;

import com.qinfei.core.entity.Dict;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserGroupRelate;
import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IUserGroupService
 * @Description 用户群组服务接口
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:04
 * @Version 1.0
 */
public interface IUserGroupService {
    String CACHE_KEY = "userGroup";

    List<User> listBusinessPart(String name);

    /**
     * 分页查询批次列表
     */
    PageInfo<UserGroup> listPg(Map map, Pageable pageable);

    UserGroup getById(Integer id);

    List<UserGroup> queryUserId(Integer id);

    void delById(Integer integer);




}
