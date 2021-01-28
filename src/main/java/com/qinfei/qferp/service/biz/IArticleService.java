package com.qinfei.qferp.service.biz;

import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.Order;
import com.qinfei.qferp.entity.media1.MediaAudit;
import com.qinfei.qferp.excelListener.*;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IArticleService {

    String CACHE_KEY = "article";
    String CACHE_KEY_LIST = "articleList";

//	String exportFeedBack(Map map, String companyName);

    /**
     * 稿件管理列表
     *
     * @param params
     * @return
     */
    PageInfo<Map<String, Object>> articleList(Map params, Pageable pageable);

    PageInfo<Map<String, Object>> articleListCSY(Map params, Pageable pageable);

    PageInfo<Map<String, Object>> articleListYW(Map params, Pageable pageable);

    PageInfo<Map<String, Object>> articleListMJ(Map params, Pageable pageable);

    Map articleResult(Map map);

    Map articleResultCSY(Map map);

    Map articleResultYW(Map map);

    Map articleResultMJ(Map map);

    List<ArticleExcel> articleListPage(Map map);

    int articleListCount(Map map);

    List<ArticleExcelCSY> articleListPageCSY(Map map);

    int articleListCountCSY(Map map);

    List<ArticleExcelYW> articleListPageYW(Map map);

    int articleListCountYW(Map map);

    List<ArticleExcelMJ> articleListPageMJ(Map map);

    int articleListCountMJ(Map map);

    Map editArticle(Map map);

    int add(Order order, Article article, MultipartFile[] files);

    int updateArticle(Map map);

    Map<String, Object> deleteArticle(Integer id);

    void deleteArticleByOrderId(Integer id);

    /**
     * 根据订单ID删除稿件信息
     *
     * @param orderId
     * @return
     */
    @SuppressWarnings("SpringCacheAnnotationsOnInterfaceInspection")
    @Transactional
    @CacheEvict(value = CACHE_KEY)
    int delByOrderId(Integer orderId);

    Map<String, Object> batchDelete(String datas) throws Exception;

    @Transactional
    @Cacheable(value = CACHE_KEY, key = "#article.id")
    Article save(Article article);

    void update(Article article);

    @Transactional
    boolean saveBatch(List<Article> article);

    @Transactional
    boolean updateBatch(List<Article> article);

    @Transactional
    PageInfo<Article> listByOrderId(Integer orderId, Pageable pageable);

    List<Article> listByOrderId(Integer orderId);

    Article getById(Integer id);

    boolean updatePathById(Integer id, String path);

    /**
     * 板块统计功能：总计
     *
     * @param map 请求参数
     * @return 各个板块统计数量
     */
    Map<String, Object> getMediaTypeStatistics(Map<String, Object> map);

    /**
     * 板块统计功能：板块列表统计
     *
     * @param map      请求参数
     * @param pageNum  页码
     * @param pageSize 每页展示数量
     * @return 各个板块统计数量
     */
    PageInfo<Map> listStatisticsByMediaType(Map<String, Object> map, Integer pageNum, Integer pageSize);

    /**
     * 板块统计功能：板块列表统计
     *
     * @param map 请求参数
     * @return 各个板块统计数量
     */
    List<Map> listStatisticsByDate(Map<String, Object> map);

    /**
     * 板块统计功能：所选板块媒体统计列表
     *
     * @param map      请求参数
     * @param pageNum  页码
     * @param pageSize 每页展示数量
     * @return 各个板块统计数量
     */
    PageInfo<Map> listStatisticsByMedia(Map<String, Object> map, Integer pageNum, Integer pageSize);

    /**
     * 板块统计功能：所选板块业务员统计列表
     *
     * @param map      请求参数
     * @param pageNum  页码
     * @param pageSize 每页展示数量
     * @return 各个板块统计数量
     */
    PageInfo<Map> listStatisticsByBusiness(Map<String, Object> map, Integer pageNum, Integer pageSize);

    /**
     * 板块统计功能：所选板块媒介统计列表
     *
     * @param map      请求参数
     * @param pageNum  页码
     * @param pageSize 每页展示数量
     * @return 各个板块统计数量
     */
    PageInfo<Map> listStatisticsByMediaUser(Map<String, Object> map, Integer pageNum, Integer pageSize);

    /**
     * 板块统计功能：所选板块供应商统计列表
     *
     * @param map      请求参数
     * @param pageNum  页码
     * @param pageSize 每页展示数量
     * @return 各个板块统计数量
     */
    PageInfo<Map> listStatisticsBySupplier(Map<String, Object> map, Integer pageNum, Integer pageSize);

    /**
     * 板块统计功能：所选板块供应商统计列表
     *
     * @param map      请求参数
     * @param pageNum  页码
     * @param pageSize 每页展示数量
     * @return 各个板块统计数量
     */
    PageInfo<Map> listStatisticsByCust(Map<String, Object> map, Integer pageNum, Integer pageSize);

    /**
     * 板块统计功能：列表导出
     *
     * @param response
     * @param map
     */
    void statisticsRankingExport(HttpServletResponse response, Map map) throws Exception;

    /**
     * 媒体统计功能：列表导出
     *
     * @param response
     * @param map
     */
    void mediaStatisticsRankingExport(HttpServletResponse response, Map map) throws Exception;

    /**
     * 媒体统计功能：总计
     *
     * @param map 请求参数
     * @return 媒体统计数量
     */
    Map<String, Object> getMediaStatistics(Map<String, Object> map);

    /**
     * 媒体统计功能：根据板块ID获取媒体列表
     *
     * @param map 请求参数
     */
    List<MediaAudit> listMediaByType(Map<String, Object> map);

    //分页查询反馈列表
    PageInfo<Map<String, Object>> listArtFeedback(Map<String, Object> param, Pageable pageable);

    //导出反馈列表
    List<Map<String, Object>> exportFeedbackList(Map<String, Object> param);

    /**
     * 批量修改稿件
     *
     * @param param 入参
     * @return 数据库影响行数
     */
    int batchEditArticle(Map<String, String> param);

    /**
     * 根据稿件id拿到请款记录
     * @param articleId 稿件id
     * @return 请款记录
     */
    Map<String, Object> getFeeOutgo(Integer articleId);
}
