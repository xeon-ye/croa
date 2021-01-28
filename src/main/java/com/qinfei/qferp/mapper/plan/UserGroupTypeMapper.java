package com.qinfei.qferp.mapper.plan;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.plan.UserGroupType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @CalssName UserGroupTypeMapper
 * @Description 用户群组类型
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:53
 * @Version 1.0
 */
public interface UserGroupTypeMapper extends BaseMapper<UserGroupType, Integer> {
    int save(UserGroupType userGroupType);

    int updateById(UserGroupType userGroupType);

    int updateStateById(@Param("id")Integer id, @Param("state") Integer state, @Param("updateId") Integer updateId);

    UserGroupType getGroupTypeById(@Param("id")Integer id);

    List<UserGroupType> getSameNameCount(@Param("id")Integer id, @Param("name") String name, @Param("companyCode") String companyCode);

    List<UserGroupType> listAllGroupType(@Param("companyCode") String companyCode);
}
