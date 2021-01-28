package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.sys.UserBinding;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @CalssName UserBindingMapper
 * @Description 用户绑定表
 * @Author xuxiong
 * @Date 2019/10/18 0018 10:07
 * @Version 1.0
 */
public interface UserBindingMapper extends BaseMapper<UserBinding, Integer> {
    //新增
    int save(UserBinding userBinding);

    //批量新增
    int saveBatch(List<UserBinding> list);

    //修改绑定码
    int updateUnionByUserId(UserBinding userBinding);

    //根据用户ID修改绑定状态
    int updateStateByUserId(@Param("userId") Integer userId, @Param("state") Integer state, @Param("updateId") Integer updateId);

    //根据用户ID获取unionID
    UserBinding getUserBindingByUserId(@Param("userId") Integer userId);

    //根据用户ID和unionID获取绑定信息
    UserBinding getUserBindingByUserIdAndUnion(@Param("userId") Integer userId, @Param("unionId") String unionId);

    //根据用户ID获取用户信息
    UserBinding getUserBindingInfoByUserId(@Param("userId") Integer userId);

    //根据unionID获取切换用户列表
    List<UserBinding> listUserBindingByUnionId(@Param("unionId") String unionId, @Param("userId") Integer userId);

    //根据用户名和密码获取有效用户
    User getUserByUserNameAndPassword(@Param("userName") String userName, @Param("password") String password);

    //根据用户ID获取用户信息
    User getUserInfoById(@Param("userId") Integer userId);
}
