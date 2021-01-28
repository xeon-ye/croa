package com.qinfei.qferp.mapper.sys;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.DeptZw;
import com.qinfei.qferp.entity.sys.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName DeptZwMapper
 * @Description 部门政委表
 * @Author xuxiong
 * @Date 2019/12/19 0019 11:50
 * @Version 1.0
 */
public interface DeptZwMapper extends BaseMapper<DeptZw, Integer> {
    //新增
    int save(DeptZw deptZw);

    //批量新增
    int saveBatch(List<DeptZw> list);

    //修改绑定状态通过部门ID
    int updateStateByDeptId(@Param("deptId") Integer deptId, @Param("userId") Integer userId, @Param("state") Integer state, @Param("updateId") Integer updateId);

    //根据部门ID和用户ID集合修改绑定状态
    int batchUpdateState(@Param("state") Integer state, @Param("deptId") Integer deptId, @Param("updateId") Integer updateId, @Param("userIds") List<Integer> userIds);

    //根据部门ID集合修改绑定状态
    int batchUpdateStateByDeptId(@Param("state") Integer state, @Param("deptIds") String deptIds, @Param("updateId") Integer updateId);

    //获取绑定政委数据
    List<DeptZw> listDeptZwByParam(@Param("userId") Integer userId, @Param("deptId") Integer deptId);

    //获取绑定的政委信息
    List<User> listUserByParam(@Param("userId") Integer userId, @Param("deptId") Integer deptId);

    //获取绑定的政委对应部门信息
    List<Map<String, Object>> listDeptInfoByParam(@Param("deptIds") List<Integer> deptIds, @Param("userId") Integer userId, @Param("deptCode") String deptCode);

    //根据部门ID集合获取部门信息
    List<Map<String, Object>> listDeptInfoByDeptIds(@Param("deptIds") String deptIds);

    //根据部门类型和角色类型获取所有用户
    List<User> listUserByDeptAndRole(Map<String, Object> param);

    //政委管理部门用户列表
    List<User> listZwUserByDeptAndRole(Map<String, Object> param);

    //根据指定部门ID获取政委管理的其下级子部门列表
    List<Integer> listChildDeptIdByUserId(@Param("deptIds") String deptIds, @Param("userId") Integer userId);
}
