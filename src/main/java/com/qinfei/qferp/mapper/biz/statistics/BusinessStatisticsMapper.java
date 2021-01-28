package com.qinfei.qferp.mapper.biz.statistics;

import java.util.List;
import java.util.Map;

public interface BusinessStatisticsMapper {
    /**
     * 业务统计结果
     * @param map
     * @return
     */
//    List<Map<String,Object>> statisticsResult(Map map);

    /**
     * 业务统计总计
     */
    Map getBusinessStatisticsByParam(Map map);

    /**
     * 业务统计趋势图
     */
    List<Map<String,Object>> listBusinessTrendStatisticsByParam(Map map);

    /**
     * 业务统计板块占比
     */
    List<Map<String,Object>> listBusinessMediaTypeStatisticsByParam(Map map);

    /**
     * 客户公司类型占比
     */
    List<Map<String, Object>> listBusinessCustTypeStatisticsByParam(Map<String, Object> map);

    /**
     * 客户行业类型占比
     */
    List<Map<String, Object>> listBusinessCustIndustryTypeStatisticsByParam(Map<String, Object> map);

    /**
     * 稿件类型占比
     */
    List<Map<String, Object>> listBusinessArtTypeStatisticsByParam(Map<String, Object> map);

    /**
     * 最近三个月未成交的客户
     * @param map
     */
    List<Map> listCustForNotTrans(Map map);

    /**
     * 最近三个月未成交的客户-废弃
     * @param map
     * @return
     */
    List<Map<String,Object>> statisticsRanking(Map map);

    /**
     * 统计各个部门业务量
     */
    Map<String, Object> getDeptStatisticsByDeptId(Map<String, Object> map);

    /**
     * 业务排名
     */
    List<Map<String, Object>> businessTop(Map<String, Object> map);

    /**
     * 未到款统计
     */
    List<Map<String, Object>> queryNotIncome(Map<String, Object> map);

    /**
     * 根据type类型查询所有的业务员或客户的未到款，
     * type=3业务员
     * type=4客户
     */
    List<Map<String, Object>> queryAllParentByType(Map<String, Object> map);
    /**
     * 根据type类型查询所有的业务员或客户的未到款，
     * type=3业务员
     * type=4客户
     */
    List<Map<String, Object>> queryAllByType(Map<String, Object> map);
    /**
     * 根据type类型查询所有的业务员或客户的未到款，
     * type=3业务员
     * type=4客户
     */
    List<Map<String, Object>> queryAllChildByType(Map<String, Object> map);

    /**
     * 未到款统计上方的统计行信息
     */
    Map<String, Object> querySumNotIncome(Map<String, Object> map);

    /**
     * 未到款统计
     */
    List<Map<String, Object>> queryNotIncomeYear(Map<String, Object> map);

    /**
     * 根据type类型查询业绩，
     * type=3业务员
     * type=4客户
     */
/*
    List<Map<String, Object>> queryAllSaleByType(Map<String, Object> map);
*/

    List<Map<String, Object>> queryAllParentSaleByTypeAndLevel(Map<String, Object> map);
    /**
     * 根据type类型查询业绩，
     * type=3业务员
     * type=4客户
     */
    List<Map<String, Object>> queryAllSaleByTypeAndLevel(Map<String, Object> map);

    Map<String, Object> queryAllSaleByTypeAndLevelSum(Map<String, Object> map);

    //业绩统计：查询所有部门的汇总数据
    List<Map<String, Object>> listAllSaleByParam(Map<String, Object> param);

    //未到款统计：查询所有部门汇总数据
    List<Map<String, Object>> listAllNotIncome(Map<String, Object> param);
    List<Map<String, Object>> listAllNotIncomeYear(Map<String, Object> param);
}
