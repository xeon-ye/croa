package com.qinfei.qferp.mapper.news;


import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.news.News;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface NewsMapper extends BaseMapper<News,Integer> {
    List<News> getNewsByNews(News news);

    int getAdminNewsCount(Map<String, Object> param);

    List<News> getAdminNewsByNews(News news);

    News getNextNews(News news);

    News getAdminNextNews(News news);

    News getAdminPreNews(News news);

    News getPreNews(News news);

    //批量更新状态
    int batchUpdateStateByIds(@Param("state") byte state, @Param("updateId") Integer updateId, @Param("ids") List<Integer> ids);
}


