package com.qinfei.qferp.mapper.plan;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserGroupRelate;
import com.qinfei.qferp.entity.sys.User;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName UserGroupMapper
 * @Description 用户群组表
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:53
 * @Version 1.0
 */
public interface UserGroupMapper extends BaseMapper<UserGroup, Integer> {
    List<User> listBusinessPart1(@Param("name") String name, @Param("companyCode") String companyCode);

    /**
     * 分页查询批次列表
     */
    List<UserGroup> listPg(Map map);

    UserGroup getById(Integer id);

    List<UserGroup> queryUserId(Integer id);

   void updateState(UserGroup userGroup);

   int updateStateByType(@Param("groupTypeId")Integer groupTypeId, @Param("state") Integer state, @Param("updateId") Integer updateId);
}
