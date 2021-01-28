package com.qinfei.qferp.service.impl;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.sys.DeptZwMapper;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author gzw
 * create by Administrator on 2019/4/26
 */
@Component
public class BaseService {
    @Autowired
    private IUserService userService;
    @Autowired
    private DeptZwMapper deptZwMapper;

    //添加数据权限相关的内容
    public void addSecurity(Map map) {
        User user = AppUtil.getUser();
        Object pcompanyCode = map.get(IConst.COMPANY_CODE);//优先取参数中的公司编码
        Object companyCode1 = map.get("companyCode1");//优先取参数中的公司编码
        String companyCode = user.getCompanyCode();
        if (!StringUtils.isEmpty(pcompanyCode))
            map.put("companyCode1", companyCode1 == null ? pcompanyCode : companyCode1);
        if (!AppUtil.isRoleType(IConst.ROLE_TYPE_JT)) {
            map.put("companyCode", companyCode != null ? companyCode : pcompanyCode);

            if (user.getCurrentDeptQx()) {
                Integer deptId = MapUtils.getInteger(map, "currentDeptId");
                deptId = deptId == null ? user.getDeptId() : deptId;
                String deptIds = "";
                if (deptId != null) {
                    deptIds = userService.getChilds(deptId);
                    if (deptIds.indexOf("$,") > -1) {
                        deptIds = deptIds.substring(2);
                    }
                    map.put("deptIds", deptIds);
                }
            } else {
                //只统计当前用户自己的数据
                map.put("currentUserId", user.getId());
            }
            //如果参数中没有companyCode 且是祥和的媒介就不需要区分公司查询
            if (IConst.COMPANY_CODE_XH.equals(companyCode) && AppUtil.isRoleType(IConst.ROLE_TYPE_MJ))
                map.remove("companyCode");
        }
    }

    //根据前端界面选择的部门和人员来设置查询条件
    public void addSecurityByFore(Map map) {
        if (map.get("currentUserId") == null) { //当页面没有选具体人员时，查询部门
            User user = AppUtil.getUser();
            Object pcompanyCode = map.get(IConst.COMPANY_CODE);//获取参数中的公司编码
            String companyCode = user.getCompanyCode();//获取当前用户的公司编码
            if (AppUtil.isRoleType(IConst.ROLE_TYPE_JT) && MapUtils.getInteger(map, "currentDeptId") == null) { //如果有集团权限，并且没有在前端选择部门，则查询所有
                return;
            }
            map.put("companyCode", pcompanyCode != null ? pcompanyCode : companyCode);//优先取参数中的公司编码
            if (user.getCurrentDeptQx() || AppUtil.isRoleCode(IConst.ROLE_CODE_ZW)) {
                Integer deptId = MapUtils.getInteger(map, "currentDeptId");//页面选择的部门
                deptId = deptId == null ? user.getDeptId() : deptId;
                String deptIds = "";
                if (deptId != null) {
                    deptIds = userService.getChilds(deptId);
                    if (deptIds.indexOf("$,") > -1) {
                        deptIds = deptIds.substring(2);
                    }
                    map.put("deptIds", deptIds);
                }
            } else {
                map.put("currentUserId", user.getId()); //只统计当前用户自己的数据
            }
            //如果参数中没有companyCode 且是祥和的媒介就不需要区分公司查询
            if (IConst.COMPANY_CODE_XH.equals(companyCode) && AppUtil.isRoleType(IConst.ROLE_TYPE_MJ)) {
                map.remove("companyCode");
            }
        }
    }

    //政委只能查看自己管理部门的数据
    public void addSecurityByZw(Map<String, Object> map) {
        if (map.get("currentUserId") == null) {
            User user = AppUtil.getUser();
            if (user == null) {
                throw new QinFeiException(1002, "请先登录！");
            }
            map.put("companyCode", user.getCompanyCode());
            //如果前台没有传递指定部门，则查询当前政委所管理的所有部门下人员，否则，查看当前部门及其子部门（政委管理的）
            String deptCode = map.get("deptCode") != null ? String.valueOf(map.get("deptCode")) : null;
            if (map.get("currentDeptId") == null) {
                List<Map<String, Object>> deptList = deptZwMapper.listDeptInfoByParam(null, user.getId(), deptCode);
                if (CollectionUtils.isEmpty(deptList)) {
                    throw new QinFeiException(1002, "当前政委没有绑定对应部门！");
                }
                List<Integer> deptIds = new ArrayList<>();
                for (Map<String, Object> dept : deptList) {
                    deptIds.add(Integer.parseInt(String.valueOf(dept.get("id"))));
                }
                map.put("deptIds", org.apache.commons.lang3.StringUtils.join(deptIds, ","));
            } else {
                String deptIds = userService.getChilds(Integer.parseInt(String.valueOf(map.get("currentDeptId"))));
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(deptIds) && deptIds.indexOf("$,") > -1) {
                    deptIds = deptIds.substring(2);
                }
                map.put("deptIds", org.apache.commons.lang3.StringUtils.join(deptZwMapper.listChildDeptIdByUserId(deptIds, user.getId()), ","));
                map.remove("currentDeptId");
            }
        }
    }

    /**
     * 判断用户是否有当前公司的权限
     *
     * @param user
     * @return
     */
    public static boolean currentCompanyQx(User user) {
        List<Role> roles = user.getRoles();
        if (roles != null) {
            for (Role r : roles) {
                if ((IConst.ROLE_CODE_ZJ.equals(r.getCode()) && !IConst.ROLE_TYPE_YW.equals(r.getType()) && !IConst.ROLE_TYPE_MJ.equals(r.getType()) && !IConst.ROLE_TYPE_XM.equals(r.getType()))
                        || IConst.ROLE_CODE_ZJL.equals(r.getCode()) || IConst.ROLE_CODE_FZ.equals(r.getCode())
                        || IConst.ROLE_CODE_ZC.equals(r.getCode()) || IConst.ROLE_CODE_FZC.equals(r.getCode()) || (IConst.ROLE_TYPE_JT.equals(r.getType()) && IConst.ROLE_CODE_KJ.equals(r.getCode()))) {
                    return true;
                }
            }
        }
        return false;
    }
}
