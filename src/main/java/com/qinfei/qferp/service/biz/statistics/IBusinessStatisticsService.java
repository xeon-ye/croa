package com.qinfei.qferp.service.biz.statistics;

import com.github.pagehelper.PageInfo;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IBusinessStatisticsService {
    /**
     * 业务统计结果-旧的
     * @param map
     * @return
     */
//    List<Map<String,Object>> statisticsResult(Map map);

    /**
     * 业务统计结果
     * @param map
     * @return
     */
    Map<String,Object> businessStatisticsResult(Map map);

    /**
     * 业务统计结果
     * @param map
     * @return
     */
    Map<String,Object> zwBusinessStatisticsResult(Map map);

    /**
     * 最近三个月未成交客户
     * @param pageNum
     * @param pageSize
     * @param map
     * @return
     */
    PageInfo<Map> listCustForNotTrans(Integer pageNum, Integer pageSize, Map map);

    /**
     * 最近三个月未成交客户
     * @param map
     * @return
     */
    PageInfo<Map<String,Object>> statisticsRanking(Map map, Integer pageNum, Integer pageSize);

    /**
     * 统计各个部门的业务
     */
    List<Map<String, Object>> everyDeptBusiness(List<Integer> list, Map<String, Object> map);

    /**
     * 业务排名
     */
    List<Map<String, Object>> businessTop(Map<String, Object> map);

    /**
     * 未到款统计
     */
    PageInfo<Map<String, Object>> queryNotIncome(int pageNum, int pageSize, Map map);

    List<Map<String, Object>> exportNotIncome(Map map, OutputStream outputStream);

    Map querySumNotIncome(Map map);

    PageInfo<Map<String, Object>> queryNotIncomeYear(int pageNum, int pageSize, Map map);

    List<Map<String, Object>> exportNotIncomeDetail(Map map, OutputStream outputStream);

    /**
     * 业绩统计
     */
    PageInfo<Map<String, Object>> querySaleStat(int pageNum, int pageSize, Map map);

    Map querySaleStatSum(Map map);

    void exportSaleStat(Map map, OutputStream outputStream);
}
