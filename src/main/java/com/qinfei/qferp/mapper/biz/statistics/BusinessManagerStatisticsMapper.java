package com.qinfei.qferp.mapper.biz.statistics;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BusinessManagerStatisticsMapper {
    //设置稿件数量，应收金额...
    List<Map> topOptionSetValue(Map map);

    //查询订单排名
    List<Map> orderSort(Map map);

    //利润排名
    List<Map> profitSort(Map map);

    //业务员未到款排名
    List<Map> businessNoIncomeSort(Map map);

    //客户应收金额排名
    List<Map> custSaleAmountSort(Map map);

    //客户未到款金额排名
    List<Map> custNoIncomeSort(Map map);

    //查询稿件排名
    List<Map> articleSort(Map map);

    //统计各个部门的业务情况
    List<Map> everyDeptBusiness(@Param("list") List<Integer> list, @Param("dateSelect") String dateSelect);

    /**
     * 根据部门ID获取该部门（含子部门）的应收和利润
     */
    Map getDeptStatisticsByDeptId(Map map);

    //统计业务前排名
    List<Map> businessTop(Map map);
}


