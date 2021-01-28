package com.qinfei.qferp.utils;

import com.qinfei.qferp.entity.sys.User;
import org.springframework.util.StringUtils;

import java.util.Map;

//数据权限工具类
class DataSecurityUtil {
//    //添加数据权限相关的内容
//    public static void addSecurity(Map map) {
//
//        User user = AppUtil.getUser();
//        if (user.getCurrentDeptQx()) {
//            //如果有当前部门权限，则统计当前部门的数据
//            map.remove("currentUserId");
//            map.put("currentDeptQx", "true");
//
//            //取巧，如果是财务部，把权限上移到总经办，这样财务就能看到业务和媒介数据了
//            Integer deptId = user.getDeptId();
//            if (deptId == 107 || deptId == 103) {
//                map.put("currentDeptId", 101);
//            } else if (deptId == 522 || deptId == 519) {
//                //财务波动公司查所有媒介
//                Object roleType = map.get("roleType");
//                if (roleType != null && "MJ".equals(roleType.toString())) {
//                    map.put("currentDeptId", 101);
//                } else {
//                    map.put("currentDeptId", 519);
//                }
//            } else if (deptId == 568 || deptId == 520) {
//                //财务第一事业部查祥和媒介，查自己业务
//                Object roleType = map.get("roleType");
//                if (roleType != null && "MJ".equals(roleType.toString())) {
//                    map.put("currentDeptId", 101);
//                } else {
//                    map.put("currentDeptId", 520);
//                }
//
//            } else {
//                map.put("currentDeptId", deptId);
//            }
//
//        } else {
//            //只统计当前用户自己的数据
//            map.put("currentUserId", user.getId());
//        }
//    }


//    public static void addSecurity(Map map) {
//        if (!StringUtils.isEmpty(map.get("currentDeptId"))) {
//            map.remove("currentUserId");
//            map.put("currentDeptQx", "true");
//            return;
//        }
//        User user = AppUtil.getUser();
//        if (user.getCurrentDeptQx()) {
//            //如果有当前部门权限，则统计当前部门的数据
//            map.remove("currentUserId");
//            map.put("currentDeptQx", "true");
//
//            //取巧，如果是财务部，把权限上移到总经办，这样财务就能看到业务和媒介数据了
//            Integer deptId = user.getDeptId();
//            if (deptId == 107 || deptId == 103) {
//                map.put("currentDeptId", 101);
//            } else if (deptId == 522 || deptId == 519) {
//                //财务波动公司查所有媒介
//                Object roleType = map.get("roleType");
//                if (roleType != null && "MJ".equals(roleType.toString())) {
//                    map.put("currentDeptId", 101);
//                } else {
//                    map.put("currentDeptId", 519);
//                }
//            } else if (deptId == 568 || deptId == 520) {
//                //财务第一事业部查祥和媒介，查自己业务
//                Object roleType = map.get("roleType");
//                if (roleType != null && "MJ".equals(roleType.toString())) {
//                    map.put("currentDeptId", 101);
//                } else {
//                    map.put("currentDeptId", 520);
//                }
//
//            } else {
//                map.put("currentDeptId", deptId);
//            }
//
//        } else {
//            //只统计当前用户自己的数据
//            map.put("currentUserId", user.getId());
//        }
//    }
}
