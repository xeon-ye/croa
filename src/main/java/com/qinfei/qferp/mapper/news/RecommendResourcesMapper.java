package com.qinfei.qferp.mapper.news;


import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.news.RecommendResources;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RecommendResourcesMapper extends BaseMapper<RecommendResources,Integer> {
    List<RecommendResources> getListByRes(RecommendResources res);

    RecommendResources getResourceById(@Param("id") Integer id);

    RecommendResources getNextResources(RecommendResources recommendResources);

    RecommendResources getPreResources(RecommendResources recommendResources);
}


