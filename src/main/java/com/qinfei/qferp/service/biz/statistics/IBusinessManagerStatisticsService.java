package com.qinfei.qferp.service.biz.statistics;

import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

public interface IBusinessManagerStatisticsService {
    List<Map> topOptionSetValue(Map map);

    List<Map> orderSort(Map map,Integer pageNum,Integer pageSize);

    PageInfo<Map> profitSort(Map map, Integer pageNum, Integer pageSize);

    PageInfo<Map> businessNoIncomeSort(Map map, Integer pageNum, Integer pageSize);

    PageInfo<Map> custSaleAmountSort(Map map, Integer pageNum, Integer pageSize);

    PageInfo<Map> custNoIncomeSort(Map map, Integer pageNum, Integer pageSize);

    List<Map> articleSort(Map map,Integer pageNum,Integer pageSize);

    List<Map> everyDeptBusiness(List<Integer> list,String dateSelect);

    List<Map> businessTop(Map map);
}
