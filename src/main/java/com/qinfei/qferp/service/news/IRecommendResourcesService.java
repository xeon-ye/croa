package com.qinfei.qferp.service.news;

import com.qinfei.qferp.entity.news.News;
import com.qinfei.qferp.entity.news.RecommendResources;

import java.util.List;

public interface IRecommendResourcesService {

      Boolean save(RecommendResources recommendRes);

      Boolean update(RecommendResources recommendRes);

      List<RecommendResources>  getResByDto(RecommendResources recommendRes);

      List<RecommendResources> getAdminResByDto(RecommendResources recommendRes, int pageNum, int pateSize, String sort);

      Boolean deleteById(Integer id);

      RecommendResources queryById(Integer id);

      RecommendResources getNextResources(RecommendResources recommendResources);

      RecommendResources getPreResources(RecommendResources recommendResources);

}
