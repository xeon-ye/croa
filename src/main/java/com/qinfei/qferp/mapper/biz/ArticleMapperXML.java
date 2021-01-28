package com.qinfei.qferp.mapper.biz;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qinfei.qferp.service.dto.BizProfitDto;
import org.apache.ibatis.annotations.*;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Article;

public interface ArticleMapperXML extends BaseMapper<Article, Integer> {

    /**
     * articleList稿件查询页面
     *
     * @param map
     * @return
     */
    List<Map<String, Object>> articleList(Map map);

    List<Map<String, Object>> articleListCSY(Map map);

    List<Map<String, Object>> articleListYW(Map map);

    List<Map<String, Object>> articleListMJ(Map map);

    /**
     * articleResult稿件查询页面的统计
     *
     * @param map
     * @return
     */
    Map articleResult(Map map);

    Map articleResultCSY(Map map);

    Map articleResultYW(Map map);

    Map articleResultMJ(Map map);

    /**
     * articleListPage稿件查询页面分页导出
     *
     * @param map
     * @return
     */
    List<Map<String, Object>> articleListPage(Map map);

    List<Map<String, Object>> articleListPageCSY(Map map);

    List<Map<String, Object>> articleListPageYW(Map map);

    List<Map<String, Object>> articleListPageMJ(Map map);

    /**
     * articleListCount稿件查询页面稿件数量
     *
     * @param map
     * @return
     */
    int articleListCount(Map map);

    int articleListCountCSY(Map map);

    int articleListCountYW(Map map);

    int articleListCountMJ(Map map);

    /**
     * 业务查询
     *
     * @param map
     * @return
     */
    List<Map<String, Object>> articleByMJ(Map map);

    /**
     * 编辑稿件时查出稿件信息
     *
     * @param map
     * @return
     */
    Map editArticle(Map map);

    /**
     * 更新稿件信息
     *
     * @param map
     * @return
     */
    int updateArticle(Map map);

    /**
     * 稿件发布使用
     */
    int updateArticlePublish(Map<String, Object> map);

    /**
     * 删除稿件
     *
     * @param id
     * @return
     */
    int deleteArticle(@Param("artId") Integer id, @Param("userId") Integer userId);

    /**
     * 根据订单ID删除媒体
     *
     * @param orderId
     * @return
     */
    int deleteArticleByOrderId(@Param("orderId") Integer orderId);

    /**
     * 改变稿件状态
     *
     * @param article
     * @return
     */
    int changeIssueStates(Article article);

    void saveBatch(List<Article> article);

    void updateBatch(List<Article> article);

    @Select("select IFNULL(sum(a.profit),0) as profit" +
            " from t_biz_article a,t_biz_order b" +
            " where a.state = 1" +
            " and a.order_id = b.id" +
            " and b.user_id = #{bizId}" +
            " and a.issued_date between (#{startDate}) and (#{endDate})")
    @Results({
            @Result(property = "profit", column = "profit")
    })
    BizProfitDto findProfitByMediaId(@Param("bizId") int bizId,
                                     @Param("startDate") Date startDate,
                                     @Param("endDate") Date endDate);

    @Select("select IFNULL(sum(a.profit),0) as profit" +
            " from t_biz_article a,t_biz_order b" +
            " where a.state = 1" +
            " and a.order_id = b.id" +
            " and b.user_id in (select id from sys_user where state = 1 and dept_id = #{deptId})" +
            " and a.issued_date between (#{startDate}) and (#{endDate})")
    @Results({
            @Result(property = "profit", column = "profit")
    })
    BizProfitDto findProfitByDeptId(@Param("deptId") int deptId,
                                    @Param("startDate") Date startDate,
                                    @Param("endDate") Date endDate);

    /**
     * 板块统计功能：总计
     *
     * @param map 查询条件
     * @return 统计结果
     */
    Map<String, Object> getMediaTypeStatistics(Map<String, Object> map);

    /**
     * 板块统计功能：板块列表统计
     *
     * @param map 查询条件
     * @return 统计结果
     */
    List<Map> listStatisticsByMediaType(Map<String, Object> map);

    /**
     * 板块统计功能：所选板块媒体统计列表
     *
     * @param map 查询条件
     * @return 统计结果
     */
    List<Map> listStatisticsByMedia(Map<String, Object> map);

    /**
     * 板块统计功能：所选板块业务员统计列表
     *
     * @param map 查询条件
     * @return 统计结果
     */
    List<Map> listStatisticsByBusiness(Map<String, Object> map);

    /**
     * 板块统计功能：所选板块媒介统计列表
     *
     * @param map 查询条件
     * @return 统计结果
     */
    List<Map> listStatisticsByMediaUser(Map<String, Object> map);

    /**
     * 板块统计功能：所选板块供应商统计列表
     *
     * @param map 查询条件
     * @return 统计结果
     */
    List<Map> listStatisticsBySupplier(Map<String, Object> map);

    /**
     * 板块统计功能：所选板块客户统计列表
     *
     * @param map 查询条件
     * @return 统计结果
     */
    List<Map> listStatisticsByCust(Map<String, Object> map);

    /**
     * 板块统计功能：趋势图
     *
     * @param map 查询条件
     * @return 统计结果
     */
    List<Map> listStatisticsByDate(Map<String, Object> map);

    /**
     * 统计概况：总计
     */
    Map getStatisticsById(Map map);

    /**
     * 统计概况/客户cust风格内容：客户趋势图
     */
    List<Map> listTrendStatisticsById(Map map);

    /**
     * 统计概况：板块占比
     */
    List<Map> listMediaTypeStatisticsById(Map map);

    /**
     * 统计概况/客户维度：客户涉及媒体列表
     */
    List<Map> listMediaStatisticsById(Map map);

    /**
     * 统计概况/业务员维度：业务员涉及的客户列表
     */
    List<Map> listCustStatisticsBybusinessId(Map map);

    /**
     * 统计概况/媒体维度：媒体涉及的客户列表
     */
    List<Map> listCustStatisticsByMediaId(Map map);

    @Select({"<script>",
            "select count(id)" +
                    " from t_biz_article " +
                    " where outgo_states>0 " +
                    " and id in " +
                    "<foreach collection='list' item='item' open='(' close=')' separator=','>" +
                    "#{item}" +
                    " </foreach>",
            "</script>"})
    Integer listByIdsAndOutgoStates(@Param("list") Set<Integer> list);

    @Select("select count(id)" +
            " from t_biz_article a" +
            " where refund_states>0 " +
            " and a.id in (${ids})")
    Integer listByIdsAndRefundStates(@Param("ids") String ids);

    @Select("select count(id)" +
            " from t_biz_article a" +
            " where other_pay_states>0 " +
            " and a.id in (${ids})")
    Integer listByIdsAndOtherPayStates(@Param("ids") String ids);

    @Select({"<script>",
            "select count(id)" +
                    " from t_biz_article " +
                    " where commission_states>0 " +
                    " and id in " +
                    "<foreach collection='list' item='item' open='(' close=')' separator=','>" +
                    "#{item}" +
                    " </foreach>",
            "</script>"})
    Integer countByByIdsAndCommissionStates(@Param("list") List<Integer> list);

    @Select({"<script>",
            "select count(a.id)" +
                    " from t_biz_article a left join t_biz_order b on a.order_id=b.id " +
                    " where b.cust_id is null " +
                    " and a.id in " +
                    "<foreach collection='list' item='item' open='(' close=')' separator=','>" +
                    "#{item}" +
                    " </foreach>",
            "</script>"})
    Integer countUnCompleteCustByIds(@Param("list") Set<Integer> list);

    @Select({"<script>",
            "select sum(outgo_amount)" +
                    " from t_biz_article  " +
                    " where " +
                    "  id in " +
                    "<foreach collection='list' item='item' open='(' close=')' separator=','>" +
                    "#{item}" +
                    " </foreach>",
            "</script>"})
    Integer sumOutgoAmountByIds(@Param("list") Set<Integer> list);

    //获取反馈列表
    List<Map<String, Object>> listArtFeedback(Map<String, Object> param);

    //未下单订单
    List<Map<String, Object>> listOrderByNotPlaced(Map<String, Object> param);

    int updateBatchArticle(@Param("articles") List<Article> list, @Param("param") Map<String, String> param);
}
