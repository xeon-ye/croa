package com.qinfei.qferp.service.mediauser.statistics;

import java.util.List;
import java.util.Map;

public interface IRankingUserStatisticsService {
    //获取个人排名
    String getSelfRanking(Map map);
    //获取前30名个人信息
    Map<String, Object> getSalesmanRanking(Map map);
    //获取部排名
    List<Map<String, Object>> getDeptRanking(Map<String, Object> map);
}

