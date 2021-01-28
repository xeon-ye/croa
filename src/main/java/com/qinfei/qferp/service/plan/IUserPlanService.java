package com.qinfei.qferp.service.plan;

import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserPlan;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @CalssName IUserPlanService
 * @Description 用户计划服务接口
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:04
 * @Version 1.0
 */
public interface IUserPlanService {
    String CACHE_KEY = "userPlan";

    //新增计划
    void save(UserPlan userPlan);

    //个人计划列表
    PageInfo<UserPlan> listPlanByCurrentUser(Map<String, Object> map, Pageable pageable);

    //获取指定用户计划列表
    UserPlan getTotalByUserId(Map<String, Object> map);

    //计划管理列表
    PageInfo<UserPlan> listPlanByParam(Map<String, Object> map, Pageable pageable);

    //计划管理合计
    UserPlan getTotalByParam(Map<String, Object> map);

    //计划统计列表
    PageInfo<UserPlan> listPlanStatisticsByParam(Map<String, Object> map, Pageable pageable);

    //计划统计合计
    UserPlan getStatisticsTotalByParam(Map<String, Object> map);

    //获取期号下拉列表
    List<UserGroup> listUserGroupByParam(Map<String, Object> map);

    PageInfo<Map<String, Object>> listNotEnterPlanUserByParam(Map<String, Object> map, Pageable pageable);

    //导出计划
    void exportPlanByUserId(Map<String, Object> map, OutputStream outputStream);

    //导出计划
    void exportPlanByParam(Map<String, Object> map, OutputStream outputStream);

    //导出计划统计
    void exportPlanStatisticsByParam(Map<String, Object> map, OutputStream outputStream);

    //导出未填写计划列表
    void exportNotEnterPlanByParam(Map<String, Object> map, OutputStream outputStream);

    //个人排名
    Map<String, List<Map<String, String>>> listUserSummaryRanking(Map<String, Object> param);

    //部门排名
    Map<String, List<Map<String, Object>>> listDeptSummary(Map<String, Object> param);

    //获取计划总结列表 和  计划总结统计权限
    Map<String, Object> getPlanPermission(HttpServletRequest request);
}
