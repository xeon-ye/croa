package com.qinfei.qferp.mapper.crm;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CustManagerStatisticsMapper {
    //统计前五项的值
    Map topStatistics(Map map);

    //统计各部门的男女人数
    List<Map> everyDeptUserCount(@Param("list") List<String> list);

    //统计
    Map custPie(Map map);

    //统计后四项的值
    Map<String, Object> getCustStatisticsByParam(Map<String, Object> param);

    //获取客户数
    int getCustNumByParam(Map<String, Object> param);

    //客户列表
    List<Map<String, Object>> listCustByParam(Map<String, Object> param);
 }
