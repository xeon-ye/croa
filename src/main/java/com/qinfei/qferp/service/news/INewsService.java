package com.qinfei.qferp.service.news;

import com.qinfei.qferp.entity.news.News;
import com.qinfei.qferp.entity.performance.PerformanceHistory;
import com.qinfei.qferp.entity.performance.PerformanceScheme;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface INewsService {

      boolean save(News news);

      boolean update(News news);

      List<News>  getNewsByDto(News news);

      int getAdminNewsCount(Map<String, Object> param);

      List<News> getAdminNewsByDto(News news);

      boolean deleteById(Integer id);

      List<News> listByType(Integer type);

      News queryById(Integer id);

      News getPreNews(News news);

      News getNextNews(News news);

      News getAdminPreNews(News news);

      News getAdminNextNews(News news);

      void batchDel(List<Integer> ids);
}
