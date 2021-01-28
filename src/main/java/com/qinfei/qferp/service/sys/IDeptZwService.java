package com.qinfei.qferp.service.sys;

import com.qinfei.qferp.entity.sys.DeptZw;
import com.qinfei.qferp.entity.sys.User;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IDeptZwService
 * @Description 部门政委服务接口
 * @Author xuxiong
 * @Date 2019/12/19 0019 14:19
 * @Version 1.0
 */
public interface IDeptZwService {
    String CACHE_KEY = "deptZw";

    void bindingDeptZw(DeptZw deptZw);

    List<User> listUserByDeptAndRole(Map<String, Object> param);

    List<User> listZwUserByDeptAndRole(Map<String, Object> param);

    List<DeptZw> listUserByDeptId(Integer deptId);

    List<User> listUserByParam(Integer deptId);

    List<Map<String, Object>> listDeptTreeByZw(String deptCode, Integer mgrFlag);
}
