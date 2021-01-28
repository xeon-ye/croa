package com.qinfei.qferp.service.mediauser.statistics;

import com.github.pagehelper.PageInfo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IMediaUserStatisticsService {
    /**
     * 根据媒介或媒介部门查询供应商列表
     * @param map
     */
    List<Map> listSupplierByMediaUser(Map map);

    /**
     * 获取媒介统计结果
     * @param map
     */
    Map<String, Object> mediaUserResult(Map map);

    /**
     * 获取媒介统计结果
     * @param map
     */
    Map<String, Object> zwMediaUserResult(Map map);

    /**
     * 媒介媒体列表
     * @param map 请求参数
     * @param pageNum 页码
     * @param pageSize 每页展示数量
     */
    PageInfo<Map> listMediaUserMediaStatisticsByParam(Map<String,Object> map, Integer pageNum, Integer pageSize);

    void mediaStatisticsRankingExport(HttpServletResponse response, Map map);

    List<Map> supplierResult(Map map);
}

