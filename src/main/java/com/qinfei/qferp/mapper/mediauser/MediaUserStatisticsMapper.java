package com.qinfei.qferp.mapper.mediauser;


import java.util.List;
import java.util.Map;

public interface MediaUserStatisticsMapper {
    List<Map> mediaUserResult(Map map);
    List<Map> supplierResult(Map map);

    /**
     * 媒介统计结果
     */
    Map getMediaUserStatisticsByParam(Map map);

    /**
     * 媒介统计趋势图
     */
    List<Map<String, Object>> listMediaUserTrendStatisticsByParam(Map<String, Object> map);

    /**
     * 媒介统计板块占比
     */
    List<Map<String, Object>> listMediaUserMediaTypeStatisticsByParam(Map<String, Object> map);

    /**
     * 稿件类型占比
     */
    List<Map<String, Object>> listMediaUserArtTypeStatisticsByParam(Map<String, Object> map);

    /**
     * 获取媒介媒体列表
     * @param map
     */
    List<Map> listMediaUserMediaStatisticsByParam(Map map);

}


