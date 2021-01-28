package com.qinfei.qferp.service.crm;

import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

public interface ICustManagerStatisticsService {
    Map topStatistics(Map map);

    List<Map> everyDeptUserCount(List<String> list);

    Map custPie(Map map);

    PageInfo<Map<String, Object>> listCustByParam(Integer pageNum, Integer pageSize, Map map);
}
