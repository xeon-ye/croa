package com.qinfei.qferp.service.statistics;

import com.github.pagehelper.PageInfo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 统计概况业务逻辑
 */
public interface IStatisticsOverviewService {
    /**
     * 统计概况：不同维度总计
     * @param map 请求参数
     */
    Map<String,Object> getStatisticsById(Map<String,Object> map);

    /**
     * 统计概况：不同维度板块占比
     * @param map 请求参数
     */
    List<Map> listMediaTypeStatisticsById(Map<String,Object> map);

    /**
     * 统计概况：不同维度趋势图
     * @param map 请求参数
     */
    List<Map> listTrendStatisticsById(Map<String,Object> map);

    /**
     * 统计概况/客户维度：指定客户媒体列表
     * @param map 请求参数
     * @param pageNum 页码
     * @param pageSize 每页展示数量
     */
    PageInfo<Map> listMediaStatisticsByCustId(Map<String,Object> map, Integer pageNum, Integer pageSize);

    /**
     * 统计概况/业务员维度：指定业务员的客户列表
     * @param map 请求参数
     * @param pageNum 页码
     * @param pageSize 每页展示数量
     */
    PageInfo<Map> listCustStatisticsBybusinessId(Map<String,Object> map, Integer pageNum, Integer pageSize);

    /**
     * 统计概况/媒介维度：指定媒介媒体列表
     * @param map 请求参数
     * @param pageNum 页码
     * @param pageSize 每页展示数量
     */
    PageInfo<Map> listMediaStatisticsByMediaUserId(Map<String,Object> map, Integer pageNum, Integer pageSize);

    /**
     * 统计概况/板块维度：指定板块媒体列表
     * @param map 请求参数
     * @param pageNum 页码
     * @param pageSize 每页展示数量
     */
    PageInfo<Map> listMediaStatisticsByMediaTypeId(Map<String,Object> map, Integer pageNum, Integer pageSize);

    /**
     * 统计概况/媒体维度：指定媒体的客户列表
     * @param map 请求参数
     * @param pageNum 页码
     * @param pageSize 每页展示数量
     */
    PageInfo<Map> listCustStatisticsByMediaId(Map<String,Object> map, Integer pageNum, Integer pageSize);

    /**
     * 统计概况/板块维度：指定板块媒体列表
     * @param map 请求参数
     * @param pageNum 页码
     * @param pageSize 每页展示数量
     */
    PageInfo<Map> listMediaStatisticsBySupplierId(Map<String,Object> map, Integer pageNum, Integer pageSize);

    /**
     * 统计概况导出列表功能
     */
    void statisticsOverviewRankingExport(HttpServletResponse response, Map map);

}
