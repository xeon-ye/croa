package com.qinfei.qferp.mapper.biz;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.biz.ArticleImport;
import com.qinfei.qferp.service.impl.biz.ArticleImportExcelInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ArticleImportMapper extends BaseMapper<ArticleImport, Integer> {

    @Select({"<script>",
            " SELECT a.*,b.name createName FROM t_biz_article_import a " +
                    " left join sys_user b on a.creator = b.id " +
                    "  WHERE  a.state>-2 and a.media_type_id in" +
                    " (select media_type_id from t_user_media_type where user_id = #{user.id})" +
                    " <when test='title!=null and title!=\"\"'>",
            " AND a.title like concat('%',#{title},'%')",
            " </when>",
            " <when test='link!=null and link!=\"\"'>",
            " AND a.link like concat('%',#{link},'%')",
            " </when>",
            " <when test='mediaName!=null and mediaName!=\"\"'>",
            " AND a.media_name like concat('%',#{mediaName},'%')",
            " </when>",
            " <when test='mType!=null and mType!=\"\"'>",
            " AND a.media_type_id = #{mType}",
            " </when>",
            " <when test='supplierName!=null and supplierName!=\"\"'>",
            " AND a.supplier_name like concat('%',#{supplierName},'%')",
            " </when>",
            " <when test='supplierContactor!=null and supplierContactor!=\"\"'>",
            " AND a.supplier_contactor like concat('%',#{supplierContactor},'%')",
            " </when>",
            " <when test='mediaUserName!=null and mediaUserName!=\"\"'>",
            " AND a.media_user_name like concat('%',#{mediaUserName},'%')",
            " </when>",
            " <when test='userName!=null and userName!=\"\"'>",
            " AND a.user_name like concat('%',#{userName},'%')",
            " </when>",
            " <when test='startTime!=null and startTime!=\"\"'>",
            " AND a.issued_date &gt;= #{startTime}",
            " </when>",
            " <when test='endTime!=null and endTime!=\"\"'>",
            " AND a.issued_date &lt;= #{endTime}",
            " </when>",
            " <when test='mediaUserId!=null and mediaUserId!=\"\"'>",
            " AND a.media_user_id = #{mediaUserId}",
            " </when>",
            " <when test='priceType!=null and priceType!=\"\"'>",
            " AND a.price_type = #{priceType}",
            " </when>",
            " <when test='createName!=null and createName!=\"\"'>",
            " AND b.name like concat('%',#{createName},'%')",
            " </when>",
            " <when test='createstartTime!=null and createstartTime!=\"\"'>",
            "AND a.create_time &gt;=#{createstartTime}",
            "</when>",
            " <when test='createendTime!=null and createendTime!=\"\"'>",
            "AND a.create_time &lt;=#{createendTime}",
            "</when>",
            " <when test='deptMediaUsers!=null'>",
            " AND a.media_user_id in",
            " <foreach collection='deptMediaUsers' item='userId' open='(' close=')' separator=','>",
            " #{userId}",
            " </foreach>",
            " </when>",
            " order by ",
            " <when test='sidx != null and sord != null'>",
            " a.${sidx} ${sord}",
            " </when>",
            " <when test='sidx == null or sord == null'>",
            " a.id desc",
            " </when>",
            "</script>"})
    List<Map> listPgMJ(Map map);

    @Select({"<script>",
            " SELECT a.*,b.name createName FROM t_biz_article_import a " +
                    " left join sys_user b on a.creator = b.id " +
                    "  WHERE  a.state>-2 and a.media_type_id in" +
                    " (select media_type_id from t_user_media_type where user_id = #{user.id})" +
                    " <when test='title!=null and title!=\"\"'>",
            " AND a.title like concat('%',#{title},'%')",
            " </when>",
            " <when test='link!=null and link!=\"\"'>",
            " AND a.link like concat('%',#{link},'%')",
            " </when>",
            " <when test='mediaName!=null and mediaName!=\"\"'>",
            " AND a.media_name like concat('%',#{mediaName},'%')",
            " </when>",
            " <when test='mType!=null and mType!=\"\"'>",
            " AND a.media_type_id = #{mType}",
            " </when>",
            " <when test='supplierName!=null and supplierName!=\"\"'>",
            " AND a.supplier_name like concat('%',#{supplierName},'%')",
            " </when>",
            " <when test='supplierContactor!=null and supplierContactor!=\"\"'>",
            " AND a.supplier_contactor like concat('%',#{supplierContactor},'%')",
            " </when>",
            " <when test='mediaUserName!=null and mediaUserName!=\"\"'>",
            " AND a.media_user_name like concat('%',#{mediaUserName},'%')",
            " </when>",
            " <when test='userName!=null and userName!=\"\"'>",
            " AND a.user_name like concat('%',#{userName},'%')",
            " </when>",
            " <when test='startTime!=null and startTime!=\"\"'>",
            " AND a.issued_date &gt;= #{startTime}",
            " </when>",
            " <when test='endTime!=null and endTime!=\"\"'>",
            " AND a.issued_date &lt;= #{endTime}",
            " </when>",
            " <when test='mediaUserId!=null and mediaUserId!=\"\"'>",
            " AND a.media_user_id = #{mediaUserId}",
            " </when>",
            " <when test='priceType!=null and priceType!=\"\"'>",
            " AND a.price_type = #{priceType}",
            " </when>",
            " <when test='createName!=null and createName!=\"\"'>",
            " AND b.name like concat('%',#{createName},'%')",
            " </when>",
            " <when test='createstartTime!=null and createstartTime!=\"\"'>",
            "AND a.create_time &gt;=#{createstartTime}",
            "</when>",
            " <when test='createendTime!=null and createendTime!=\"\"'>",
            "AND a.create_time &lt;=#{createendTime}",
            "</when>",
            " <when test='deptMediaUsers!=null'>",
            " AND a.media_user_id in",
            " <foreach collection='deptMediaUsers' item='userId' open='(' close=')' separator=','>",
            " #{userId}",
            " </foreach>",
            " </when>",
            " order by a.id asc",
            "</script>"})
    List<Map> listPgMJAsc(Map map);

    @Select({"<script>",
            " SELECT sum(a.pay_amount) payAmountSum,sum(a.sale_amount) saleAmountSum FROM t_biz_article_import a " +
                    " left join sys_user b on a.creator = b.id " +
                    "  WHERE  a.state>-2 and a.media_type_id in" +
                    " (select media_type_id from t_user_media_type where user_id = #{user.id})" +
                    " <when test='title!=null and title!=\"\"'>",
            " AND a.title like concat('%',#{title},'%')",
            " </when>",
            " <when test='link!=null and link!=\"\"'>",
            " AND a.link like concat('%',#{link},'%')",
            " </when>",
            " <when test='mediaName!=null and mediaName!=\"\"'>",
            " AND a.media_name like concat('%',#{mediaName},'%')",
            " </when>",
            " <when test='mType!=null and mType!=\"\"'>",
            " AND a.media_type_id = #{mType}",
            " </when>",
            " <when test='supplierName!=null and supplierName!=\"\"'>",
            " AND a.supplier_name like concat('%',#{supplierName},'%')",
            " </when>",
            " <when test='supplierContactor!=null and supplierContactor!=\"\"'>",
            " AND a.supplier_contactor like concat('%',#{supplierContactor},'%')",
            " </when>",
            " <when test='mediaUserName!=null and mediaUserName!=\"\"'>",
            " AND a.media_user_name like concat('%',#{mediaUserName},'%')",
            " </when>",
            " <when test='userName!=null and userName!=\"\"'>",
            " AND a.user_name like concat('%',#{userName},'%')",
            " </when>",
            " <when test='startTime!=null and startTime!=\"\"'>",
            " AND a.issued_date &gt;= #{startTime}",
            " </when>",
            " <when test='endTime!=null and endTime!=\"\"'>",
            " AND a.issued_date &lt;= #{endTime}",
            " </when>",
            " <when test='mediaUserId!=null and mediaUserId!=\"\"'>",
            " AND a.media_user_id = #{mediaUserId}",
            " </when>",
            " <when test='priceType!=null and priceType!=\"\"'>",
            " AND a.price_type = #{priceType}",
            " </when>",
            " <when test='createName!=null and createName!=\"\"'>",
            " AND b.name like concat('%',#{createName},'%')",
            " </when>",
            " <when test='createstartTime!=null and createstartTime!=\"\"'>",
            "AND a.create_time &gt;=#{createstartTime}",
            "</when>",
            " <when test='createendTime!=null and createendTime!=\"\"'>",
            "AND a.create_time &lt;=#{createendTime}",
            "</when>",
            " <when test='deptMediaUsers!=null'>",
            " AND a.media_user_id in",
            " <foreach collection='deptMediaUsers' item='userId' open='(' close=')' separator=','>",
            " #{userId}",
            " </foreach>",
            " </when>",
            "</script>"})
    Map getArticleImportSum(Map map);

    @Select({"<script>",
            " SELECT a.*,b.name createName FROM t_biz_article_import a " +
                    " left join sys_user b on a.creator = b.id " +
                    "  WHERE  a.state>-2 AND a.user_id = #{user.id} " +
                    " <when test='title!=null and title!=\"\"'>",
            " AND a.title like concat('%',#{title},'%')",
            " </when>",
            " <when test='link!=null and link!=\"\"'>",
            " AND a.link like concat('%',#{link},'%')",
            " </when>",
            " <when test='mediaName!=null and mediaName!=\"\"'>",
            " AND a.media_name like concat('%',#{mediaName},'%')",
            " </when>",
            " <when test='mType!=null and mType!=\"\"'>",
            " AND a.media_type_id = #{mType}",
            " </when>",
            " <when test='mediaUserName!=null and mediaUserName!=\"\"'>",
            " AND a.media_user_name like concat('%',#{mediaUserName},'%')",
            " </when>",
            " <when test='brand!=null and brand!=\"\"'>",
            " AND a.brand like concat('%',#{brand},'%')",
            " </when>",
            " <when test='userName!=null and userName!=\"\"'>",
            " AND a.uuser_name like concat('%',#{userName},'%')",
            " </when>",
            " <when test='startTime!=null and startTime!=\"\"'>",
            " AND a.issued_date &gt;= #{startTime}",
            " </when>",
            " <when test='endTime!=null and endTime!=\"\"'>",
            " AND a.issued_date &lt;= #{endTime}",
            " </when>",
            " order by ",
            " <when test='sidx != null and sidx != \"\"'>",
            " a.${sidx} ${sord}",
            " </when>",
            " <when test='sidx == null or sidx == \"\"'>",
            " a.title,a.issued_date desc",
            " </when>",
            "</script>"})
    List<Map> listPgYW(Map map);

    /**
     * 排序和listPgYW不一样
     */
    @Select({"<script>",
            " SELECT a.*,b.name createName FROM t_biz_article_import a " +
                    " left join sys_user b on a.creator = b.id " +
                    "  WHERE  a.state>-2 AND a.user_id = #{user.id} " +
                    " <when test='title!=null and title!=\"\"'>",
            " AND a.title like concat('%',#{title},'%')",
            " </when>",
            " <when test='link!=null and link!=\"\"'>",
            " AND a.link like concat('%',#{link},'%')",
            " </when>",
            " <when test='mediaName!=null and mediaName!=\"\"'>",
            " AND a.media_name like concat('%',#{mediaName},'%')",
            " </when>",
            " <when test='mType!=null and mType!=\"\"'>",
            " AND a.media_type_id = #{mType}",
            " </when>",
            " <when test='mediaUserName!=null and mediaUserName!=\"\"'>",
            " AND a.media_user_name like concat('%',#{mediaUserName},'%')",
            " </when>",
            " <when test='brand!=null and brand!=\"\"'>",
            " AND a.brand like concat('%',#{brand},'%')",
            " </when>",
            " <when test='userName!=null and userName!=\"\"'>",
            " AND a.uuser_name like concat('%',#{userName},'%')",
            " </when>",
            " <when test='startTime!=null and startTime!=\"\"'>",
            " AND a.issued_date &gt;= #{startTime}",
            " </when>",
            " <when test='endTime!=null and endTime!=\"\"'>",
            " AND a.issued_date &lt;= #{endTime}",
            " </when>",
            " order by a.id",
            "</script>"})
    List<Map> listPgYWAsc(Map map);

    @Select({"<script>",
            " SELECT count(a.id) FROM t_biz_article a " +
                    "  WHERE  a.state>-2 AND (a.income_states>0 or a.invoice_states>0 or a.commission_states>0) AND a.id in " +
                    "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            "</script>"})
    Integer checkCustInfo(List<Integer> list);

    @Select({"<script>",
            " select count(a.id) from t_biz_article a left join t_biz_order b on a.order_id=b.id" +
                    " where b.user_id!=#{user.id} and a.id in ",
            "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            "</script>"})
    Integer checkUserId(Map map);

    @Select({"<script>",
            " select DATE_FORMAT(max(a.issued_date),'%Y-%m-%d') as endTime, DATE_FORMAT(min(a.issued_date),'%Y-%m-%d') as startTime " +
                    " from t_biz_article a left join t_biz_order b on a.order_id=b.id" +
                    " where a.id in ",
            "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            "</script>"})
    Map getIssuedDateRange(Map map);

    @Select({"<script>",
            " select count(a.id) count " +
                    " from t_biz_article a left join t_biz_order b on a.order_id=b.id" +
                    " where a.sale_amount = 0 and a.id in ",
            "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            "</script>"})
    int countZeroSales(Map map);


    @Select({"<script>",
            " SELECT a.*,b.name createName,c.user_name user_name," +
                    " c.company_name custCompanyName,c.cust_name custName" +
                    " FROM t_biz_article a " +
                    " left join sys_user b on a.creator = b.id " +
                    " left join t_biz_order c on a.order_id=c.id " +
                    "  WHERE  a.state>-2 AND a.id in " +
                    "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            " order by a.issued_date asc,a.id desc",
            "</script>"})
    List<Map> listByIds(List<Integer> list);

    @Select({"<script>",
            " SELECT sum(a.sale_amount) saleSum,sum(a.pay_amount) paySum,sum(a.outgo_amount) outgoSum FROM t_biz_article a " +
                    "  WHERE a.id in " +
                    "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            "</script>"})
    Map querySumArticleByIds(List<Integer> list);

    @Select("select * from t_biz_article_import where state>-2 and id=#{id} ")
    ArticleImport getById(@Param("id") Integer id);

    @Update("update t_biz_article_import set state=#{state} where id=#{id} ")
    void deleteBySql(@Param("id") Integer id, @Param("state") Integer state);

    @Update("update t_biz_article set sale_amount=#{saleAmount},brand=#{brand} where id=#{id} ")
    int updateAmountAndBrand(Map map);

    @Update({" <script>" +
            " update t_biz_article " +
            " <trim prefix='set' suffixOverrides=','> " +
            "<trim prefix=\"sale_amount =case\" suffix=\"end,\">" +
            " <foreach collection=\"list\" item=\"item\" index=\"index\">" +
            "  <if test=\"item.saleAmount!=null\">" +
            "   when id=#{item.id} then #{item.saleAmount}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix=\"taxes =case\" suffix=\"end,\">" +
            " <foreach collection=\"list\" item=\"item\" index=\"index\">" +
            "  <if test=\"item.taxAmount!=null\">" +
            "   when id=#{item.id} then #{item.taxAmount}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix=\"profit =case\" suffix=\"end,\">" +
            " <foreach collection=\"list\" item=\"item\" index=\"index\">" +
            "  <if test=\"item.profit!=null\">" +
            "   when id=#{item.id} then #{item.profit}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix=\"income_states =case\" suffix=\"end,\">" +
            " <foreach collection=\"list\" item=\"item\" index=\"index\">" +
            "  <if test=\"item.incomeStates!=null\">" +
            "   when id=#{item.id} then #{item.incomeStates}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix=\"brand =case\" suffix=\"end,\">" +
            " <foreach collection=\"list\" item=\"item\" index=\"index\">" +
            "  <if test=\"item.brand!=null and item.brand!=''\">" +
            "   when id=#{item.id} then #{item.brand}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix=\"update_user_id =case\" suffix=\"end,\">" +
            " <foreach collection=\"list\" item=\"item\" index=\"index\">" +
            "  <if test=\"item.userId!=null\">" +
            "   when id=#{item.id} then #{item.userId}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix=\"alter_flag =case\" suffix=\"end,\">" +
            " <foreach collection=\"list\" item=\"item\" index=\"index\">" +
            "  <if test=\"item.alterFlag!=null\">" +
            "   when id=#{item.id} then #{item.alterFlag}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            " </trim> " +
            " where" +
            " <foreach collection='list' separator='or' item='item' index='index'>" +
            " id=#{item.id} " +
            " </foreach>",
            " </script>"
    })
    int batchSaleAmount(List<Map<String, Object>> list);

    @Update({" <script>" +
            " update t_biz_article " +
            " <trim prefix='set' suffixOverrides=','> " +
            "<trim prefix='order_id =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.orderId!=null'>" +
            "   when id=#{item.id} then #{item.orderId}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='tax_type =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "   when id=#{item.id} then #{item.taxType}" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='sale_amount =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.saleAmount!=null '>" +
            "   when id=#{item.id} then #{item.saleAmount}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='taxes =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.taxes!=null '>" +
            "   when id=#{item.id} then #{item.taxes}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='state =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.state!=null and item.state!=\"\"'>" +
            "   when id=#{item.id} then #{item.state}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='promise_date =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.promiseDate!=null '>" +
            "   when id=#{item.id} then #{item.promiseDate}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='income_states =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.incomeStates!=null'>" +
            "   when id=#{item.id} then #{item.incomeStates}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='profit =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.profit!=null'>" +
            "   when id=#{item.id} then #{item.profit}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='brand =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.brand!=null and item.brand!=\"\"'>" +
            "   when id=#{item.id} then #{item.brand}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='type_code =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.typeCode!=null and item.typeCode!=\"\"'>" +
            "   when id=#{item.id} then #{item.typeCode}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='type_name =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.typeName!=null and item.typeName!=\"\"'>" +
            "   when id=#{item.id} then #{item.typeName}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='update_user_id =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.updateUserId!=null'>" +
            "   when id=#{item.id} then #{item.updateUserId}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='update_time =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.updateTime!=null'>" +
            "   when id=#{item.id} then #{item.updateTime}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            "<trim prefix='alter_flag =case' suffix='end,'>" +
            " <foreach collection='list' item='item' index='index'>" +
            "  <if test='item.alterFlag!=null'>" +
            "   when id=#{item.id} then #{item.alterFlag}" +
            "  </if>" +
            " </foreach>" +
            "</trim>" +
            " </trim> " +
            " where " +
            " <foreach collection='list' separator='or' item='item' index='index'>" +
            " id=#{item.id} " +
            " </foreach>",
            " </script>"
    })
    int batchComplete(List<Article> list);

    @Update({"<script>",
            " update t_biz_article_import set state=-9 where id in ",
            "<foreach collection='collection' item='id' open='(' close=')' separator=','>",
            "#{id}",
            " </foreach>",
            "</script>"})
    int batchDel(Map map);

    @Insert({"<script>",
            " insert into t_biz_article (" +
                    "order_id," +
                    "media_type_id," +
                    "media_type_name," +
                    "media_id," +
                    "media_name," +
                    "supplier_id," +
                    "supplier_name," +
                    "supplier_contactor," +
                    "media_user_id," +
                    "media_user_name," +
                    "issued_date," +
                    "issue_states," +
                    "title," +
                    "link," +
                    "num," +
                    "price_type," +
//                    "pay_amount," +
                    "outgo_amount," +
                    "unit_price," +
                    "profit," +
                    "state," +
                    "creator," +
                    "create_time," +
                    "remarks," +
                    "inner_outer," +
                    "channel," +
                    "electricity_businesses," +
                    "other_expenses" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.orderId}," +
                    "#{item.mediaTypeId}," +
                    "#{item.mediaTypeName}," +
                    "#{item.mediaId}," +
                    "#{item.mediaName}," +
                    "#{item.supplierId}," +
                    "#{item.supplierName}," +
                    "#{item.supplierContactor}," +
                    "#{item.mediaUserId}," +
                    "#{item.mediaUserName}," +
                    "#{item.issuedDate}," +
                    "#{item.issueStates}," +
                    "#{item.title}," +
                    "#{item.link}," +
                    "#{item.num}," +
                    "#{item.priceType}," +
//                    "#{item.payAmount}," +
                    "#{item.outgoAmount}," +
                    "#{item.unitPrice}," +
                    "-#{item.outgoAmount}," +
                    "#{item.state}," +
                    "#{item.creator}," +
                    "#{item.createTime}," +
                    "#{item.remarks}," +
                    "#{item.innerOuter}," +
                    "#{item.channel}," +
                    "#{item.electricityBusinesses}," +
                    "#{item.otherExpenses})" +
                    "</foreach>",
            "</script>"})
    void saveBatch(List<Article> list);

    @Insert({"<script>",
            " insert into t_biz_article (" +
                    "order_id," +
                    "media_type_id," +
                    "media_type_name," +
                    "media_id," +
                    "media_name," +
                    "supplier_id," +
                    "supplier_name," +
                    "supplier_contactor," +
                    "media_user_id," +
                    "media_user_name," +
                    "issued_date," +
                    "issue_states," +
                    "title," +
                    "link," +
                    "num," +
                    "price_column," +
                    "price_type," +
//                    "pay_amount," +
                    "outgo_amount," +
                    "unit_price," +
                    "profit," +
                    "state," +
                    "creator," +
                    "create_time," +
                    "remarks," +
                    "inner_outer," +
                    "channel," +
                    "electricity_businesses," +
                    "other_expenses" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.orderId}," +
                    "#{item.mediaTypeId}," +
                    "#{item.mediaTypeName}," +
                    "#{item.mediaId}," +
                    "#{item.mediaName}," +
                    "#{item.supplierId}," +
                    "#{item.supplierName}," +
                    "#{item.supplierContactor}," +
                    "#{item.mediaUserId}," +
                    "#{item.mediaUserName}," +
                    "#{item.issuedDate}," +
                    "#{item.issueStates}," +
                    "#{item.title}," +
                    "#{item.link}," +
                    "#{item.num}," +
                    "#{item.priceColumn}," +
                    "#{item.priceType}," +
//                    "#{item.payAmount}," +
                    "#{item.outgoAmount}," +
                    "#{item.unitPrice}," +
                    "-#{item.outgoAmount}," +
                    "#{item.state}," +
                    "#{item.creator}," +
                    "#{item.createTime}," +
                    "#{item.remarks}," +
                    "#{item.innerOuter}," +
                    "#{item.channel}," +
                    "#{item.electricityBusinesses}," +
                    "#{item.otherExpenses})" +
                    "</foreach>",
            "</script>"})
    void saveBatchForEasyExcel(List<ArticleImportExcelInfo> list);

    @Select({"<script>",
            " select count(id) from t_biz_article where (commission_states>0 or invoice_states>0) and id in ",
            "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item.id}",
            " </foreach>",
            "</script>"})
    Integer getCountByInvoiceAndCommissionStates(@Param("list") List<Map<String, Object>> list);

    @Select({"<script>",
            " select distinct(b.depat_id) deptId from t_biz_article a left join t_biz_order b on a.order_id=b.id where a.id in ",
            "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            "</script>"})
    List<Integer> getDetpIdList(@Param("list") List<Integer> list);

    @Select({"<script>",
            " select distinct(b.cust_id) custId from t_biz_article a left join t_biz_order b on a.order_id=b.id where a.id in ",
            "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            "</script>"})
    List<Integer> getCustIdList(@Param("list") List<Integer> list);

    @Select({"<script>",
            " select max(a.issued_date) as issuedDate from t_biz_article a left join t_biz_order b on a.order_id=b.id where b.company_id = #{companyId} and a.id not in ",
            "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            "</script>"})
    Date getMaxIssuedDateByCompanyIdNot(Map map);

    @Select({"<script>",
            " select max(a.issued_date) as issuedDate from t_biz_article a left join t_biz_order b on a.order_id=b.id where b.cust_id = #{custId} and a.id not in ",
            "<foreach collection='list' item='item' open='(' close=')' separator=','>",
            "#{item}",
            " </foreach>",
            "</script>"})
    Date getMaxIssuedDateByCustIdNot(Map map);

    @Select({"<script>",
            "SELECT COUNT(promise_date) ",
            "FROM t_biz_article ",
            "WHERE id in  ",
            "<foreach collection='ids' item='id' open='(' close=')' separator=','>",
            "#{id}",
            " </foreach>",
            "</script>"})
    int countPromiseDateByIds(@Param("ids") List<Integer> ids);
}
