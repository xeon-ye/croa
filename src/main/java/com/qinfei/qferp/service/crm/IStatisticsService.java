package com.qinfei.qferp.service.crm;

import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IStatisticsService {
    /**
     * 客户统计结果
     * @param map
     * @return
     */
    List<Map<String,Object>> statisticsResult(Map map);

    /**
     * 客户统计总计
     * @param map
     * @return
     */
    Map<String,Object> custStatisticsResult(Map map);

    /**
     * 客户统计板块占比
     * @param map 请求参数
     */
    List<Map> listMediaTypeStatisticsByParam(Map<String,Object> map);

    /**
     * 统计客户排名
     * @param map
     * @return
     */
    PageInfo<Map<String,Object>> statisticsRanking(Map map, Integer pageNum, Integer pageSize);

    /**
     * 统计客户排名
     * @param map
     * @return
     */
    PageInfo<Map> listCustStatisticsRankingByParam(Map map, Integer pageNum,Integer pageSize);

    /**
     * 统计客户排名
     * @param map
     * @return
     */
    PageInfo<Map> listNewCustStatisticsRankingByParam(Map map, Integer pageNum,Integer pageSize);



    void statisticsRankingAll(Map map, OutputStream outputStream);


}
