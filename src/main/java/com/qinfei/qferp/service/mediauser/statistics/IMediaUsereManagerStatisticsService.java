package com.qinfei.qferp.service.mediauser.statistics;

import com.github.pagehelper.PageInfo;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IMediaUsereManagerStatisticsService {
    List<Map> topOptionSetValue(Map map);
    //供应商排名
    List<Map> supplierSort(Map map,Integer pageNumber,Integer pageSize);
    //请款排名
    List<Map> outgoSort(Map map,Integer pageNumber,Integer pageSize);
    //稿件类型分布
    List<Map> artTypeFb(Map map);
    //稿件供应商排名
    List<Map> supplierListSort(Map map,Integer pageNumber,Integer pageSize);
    //供应商导出
    List<Map> exportSupplier(Map map, OutputStream out);

    /**
     * 供应商统计-供应商统计合计
     * @param map
     */
    Map<String, Object> supplierStatisticsResult(Map map);

    /**
     * 供应商统计-分页查询供应商列表
     * @param map 查询条件
     * @param pageNum  页码
     * @param pageSize 每页显示条数
     */
    PageInfo<Map> listSupplierStatisticsByParam(Map<String, Object> map, Integer pageNum, Integer pageSize);
}

