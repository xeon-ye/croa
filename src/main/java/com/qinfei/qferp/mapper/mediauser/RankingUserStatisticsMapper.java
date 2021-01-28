package com.qinfei.qferp.mapper.mediauser;


import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface RankingUserStatisticsMapper {
    //获取部门排名
    List<Map<String, Object>> getDeptRanking(Map<String, Object> map);
    //获取前30名员工
    List<Map<String, Object>> getRanking(Map<String, Object> map);
    //按公司获取所有业务部门
    List<Map<String, Object>> getDeptByCompany(Map<String, Object> map);

}


