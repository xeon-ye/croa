package com.qinfei.qferp.mapper.biz;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.fee.IncomeArticle;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface ArticleMapper extends BaseMapper<Article, Integer> {

    @Select("select * from t_biz_article where order_id=#{orderId}")
    List<Article> listByOrderId(@Param("orderId") Integer orderId);

    @Select("update t_biz_article set file_path=#{filePath} where id=#{id}")
    List<Article> updatePathById(@Param("id") Integer id, @Param("filePath") String filePath);

    @Delete("delete from t_biz_article where order_id=#{orderId}")
    int delByOrderId(@Param("orderId") Integer orderId);

    @Select({"<script>" +
            " SELECT count(id) FROM fee_income_article " +
            " where article_id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            "</script>"})
    Integer getIncomeInfoByArticleIds(List<Integer> list);

    @Select({"<script>" +
            " SELECT count(id) FROM fee_outgo_article " +
            " where article_id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            "</script>"})
    Integer getOutgoInfoByArticleIds(List<Integer> list);

    @Select({"<script>" +
            " SELECT count(id) FROM fee_invoice_article " +
            " where article_id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            "</script>"})
    Integer getInvoiceInfoByArticleIds(List<Integer> list);

    @Select({"<script>" +
            " SELECT count(refund_id) FROM fee_refund_article " +
            " where article_id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            "</script>"})
    Integer getRefundInfoByArticleIds(List<Integer> list);

    @Select({
            "<script>" +
                    " SELECT count(mess_id) FROM t_accounts_mess_article " +
                    " where state > -9 and article_id in " +
                    "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
                    "       #{item}" +
                    "   </foreach>" +
                    "</script>"
    })
    Integer getMessAccountIds(List<Integer> list);

    @Update({"<script>" +
            " update t_biz_article set state= -9  " +
            " where media_user_id=#{userId} and id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            "</script>"})
    Integer deleteBatchArticle(@Param("list") List<Integer> list, @Param("userId") Integer userId);

    @Select({"<script>" +
            " SELECT count(id) FROM t_biz_article " +
            " where commission_states>0 and id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item.articleId}" +
            "   </foreach>" +
            "</script>"})
    Integer getInvoiceAndCommissionByArticleIds(List<IncomeArticle> list);

    @Select({"<script>" +
            " SELECT count(id) FROM t_biz_article " +
            " where commission_states>0 and id in " +
            "   <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">" +
            "       #{item}" +
            "   </foreach>" +
            "</script>"})
    Integer getCommissionByArticleIds(List<Integer> list);

    @Select("SELECT c.company_code companyCode " +
            "from t_biz_article a " +
            "left join t_biz_order b on a.order_id=b.id " +
            "left join sys_user c on b.user_id = c.id" +
            " where a.id =#{artId} ")
    String getCompanyCodeByArtId(@Param("artId") Integer artId);

    @Select({"<script>",
            " SELECT COUNT(1) ",
            " FROM t_biz_article ",
            " WHERE media_user_id = #{mediaUserId} and id in ",
            "       <foreach item=\"item\" index=\"index\" collection=\"array\" open=\"(\" separator=\",\" close=\")\">",
            "       #{item}",
            "   </foreach>",
            "</script>"})
    int findIsOwnArticleNum(@Param("array") String[] array, @Param("mediaUserId") Integer mediaUserId);

    @Select({"<script>",
            " select * from t_biz_article where id in ",
            "       <foreach item=\"item\" index=\"index\" collection=\"ids\" open=\"(\" separator=\",\" close=\")\">",
            "       #{item}",
            "   </foreach>",
            "</script>"})
    List<Article> listByIds(@Param("ids") String[] ids);

    @Select("SELECT " +
            "a.id id," +
            "a.media_type_id mediaTypeId," +
            "a.code code," +
            "a.apply_name applyName," +
            "tmp.parent_type parentType," +
            "t.company_code companyCodet," +
            "a.process_type processType " +
            "FROM " +
            "fee_outgo a " +
            "LEFT JOIN t_media_supplier t ON a.supplier_id = t.id " +
            "LEFT JOIN t_media_plate tmp ON tmp.id = a.media_type_id " +
            "LEFT JOIN fee_outgo_article f ON f.outgo_id = a.id " +
            "LEFT JOIN t_biz_article tba ON tba.id = f.article_id " +
            "WHERE tba.id = #{value} ")
    Map<String, Object> getFeeOutgo(Integer articleId);

    @Select({"<script>",
            " SELECT COUNT(1) FROM t_biz_article a  ",
            " LEFT JOIN t_biz_order o ON a.order_id = o.id  ",
            " WHERE o.user_id = #{userId} AND a.id in",
            "   <foreach item=\"item\" index=\"index\" collection=\"listMap\" open=\"(\" separator=\",\" close=\")\">",
            "       #{item.id}",
            "   </foreach>",
            "</script>"})
    int checkWhetherYourOwnManuscript(@Param("userId") Integer userId, @Param("listMap") List<Map<String, Object>> listMap);
}
