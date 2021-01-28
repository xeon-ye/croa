package com.qinfei.qferp.mapper.crm;

import java.util.List;
import java.util.Map;

public interface StatisticsMapper {
    /**
     * 客户统计结果
     * @param map
     * @return
     */
    List<Map<String,Object>> statisticsResult(Map map);

    /**
     * 客户排名统计
     * @param map
     * @return
     */
    List<Map<String,Object>> statisticsRanking(Map map);

    /**
     * 根据权限或角色查询当前部门或者当前用户
     * @param map
     * @return
     */
    List<Map> deptUsers(Map map);

    /**
     * 客户统计
     */
    Map getCustStatisticsByParam(Map map);

    /**
     * 客户统计趋势图
     */
    List<Map> listCustTrendStatisticsByParam(Map map);

    /**
     * 客户统计板块占比
     */
    List<Map> listCustMediaTypeStatisticsByParam(Map map);

    /**
     * 客户统计-客户排名
     */
    List<Map> listCustStatisticsRankingByParam(Map map);
}
