package com.qinfei.qferp.mapper.plan;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserPlan;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName UserPlanMapper
 * @Description 用户计划表
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:53
 * @Version 1.0
 */
public interface UserPlanMapper extends BaseMapper<UserPlan, Integer> {

    /**
     * 新增计划
     */
    int save(UserPlan userPlan);

    /**
     * 根据ID修改总结数据
     */
    int updateSummaryById(UserPlan userPlan);

    /**
     * 根据用户ID和日期获取指定类型的计划
     */
    UserPlan getPlanByUserIdAndDate(@Param("userId") Integer userId, @Param("planDate") String planDate, @Param("summaryType") Integer summaryType);

    /**
     *  获取用户最后一个计划总结
     */
    UserPlan getLastPlanByUserId(@Param("userId") Integer userId, @Param("summaryType") Integer summaryType);

    /**
     * 获取指定用户计划列表
     */
    List<UserPlan> listPlanByUserId(Map<String, Object> param);

    List<Map<String, Object>> listPlanMapByUserId(Map<String, Object> param);

    /**
     * 获取指定用户计划列表
     */
    UserPlan getTotalByUserId(Map<String, Object> param);

    /**
     * 获取用户计划列表，含权限判断
     */
    List<UserPlan> listPlanByParam(Map<String, Object> param);

    /**
     * 获取用户计划列表，含权限判断
     */
    List<Map<String, Object>> listPlanMapByParam(Map<String, Object> param);

    /**
     * 计划列表合计，权限判断
     */
    UserPlan getTotalByParam(Map<String, Object> param);

    /**
     * 计划统计，权限判断
     */
    List<UserPlan> listPlanStatisticsByParam(Map<String, Object> param);

    /**
     * 计划统计，权限判断
     */
    List<Map<String, Object>> listPlanStatisticsMapByParam(Map<String, Object> param);

    /**
     * 计划统计合计，权限判断
     */
    UserPlan getStatisticsTotalByParam(Map<String, Object> param);

    /**
     * 获取未填写计划人员，权限判断
     */
    List<Map<String, Object>> listNotEnterPlanUserByParam(Map<String, Object> param);


    /**
     * 获取期号下拉列表值
     * @param param
     * @return
     */
    List<UserGroup> listUserGroupByParam(Map<String, Object> param);

    /**
     * 个人计划总结排名
     * @param param
     * @return
     */
    List<Map<String, Object>> listUserSummaryRanking(Map<String, Object> param);

    /**
     * 部门每日计划总结列表
     * @param param
     * @return
     */
    List<Map<String, Object>> listDeptSummary(Map<String, Object> param);

}
