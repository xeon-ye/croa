package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.fee.Commission;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface CommissionMapper extends BaseMapper<Commission,Integer> {

    @Select({"<script>",
            " SELECT * " +
                " FROM fee_commission " +
                " WHERE state > -2 " +
                    " <choose>",
                    " <when test='roleCode == \"ZC\"'>",
                    " and company_code != null ",
                    " </when>",
                    " <when test='roleType == \"JT\"'>",
                    " </when>",
                    " <when test='roleCode == \"ZJL\" or roleCode==\"FZ\"'>",
                        " and company_code = #{user.dept.companyCode} ",
                    " </when>",
                    " <when test='roleType==\"CW\"'>",
                    " and company_code = #{user.dept.companyCode} ",
                    " </when>",
                    " <when test='roleType==\"YW\"'>",
                        " <choose>",
                        " <when test='roleCode==\"YG\"'>",
                        " and user_id=#{user.id}",
                        " </when>",
                        " <when test='roleCode==\"ZZ\"'>",
                        " and dept_id in (#{user.deptId})",
                        " </when>",
                        " <when test='roleCode==\"BZ\"'>",
                        " <foreach collection='user.deptIdSet' item='item' open='and dept_id in (' close=')' separator=','>",
                        " #{item}",
                        " </foreach>",
                        " </when>",
                        " <when test='roleCode==\"ZJ\"'>",
                        " and dept_id in (#{user.deptIds})",
                        " </when>",
                        "</choose>",
                    " </when>",
                    " <otherwise>",
                    " and  id=0",
                    " </otherwise>",
                    " </choose>",
                    " <when test='name!=null and name!=\"\"'>",
                    " AND name like concat('%',#{name},'%')",
                    " </when>",
                    " <when test='deptName!=null and deptName!=\"\"'>",
                    " AND dept_name like concat('%',#{deptName},'%')",
                    " </when>",
                    " <when test='year!=null and year!=\"\"'>",
                    " AND year = #{year}",
                    " </when>",
                    " <when test='month!=null and month!=\"\"'>",
                    " AND month = #{month}",
                    " </when>",
                " order by id desc",
            "</script>"})
    List<Map> listPg(Map map);

    @Select("select * from fee_commission where state>-2 and id = #{id}")
    Commission getById(Integer id);

    @Select("select * from fee_commission where state>-2 and user_id = #{userId} and year=#{year} and month=#{month} and state=1")
    List<Commission> queryCommissionByUserAndState(@Param("userId")Integer userId,@Param("year")Integer year,@Param("month")Integer month);

    @Select("select * from fee_commission where state>-2 and user_id = #{userId} and year=#{year} and month=#{month} and state!=1")
    List<Commission> queryCommissionByUser(@Param("userId")Integer userId,@Param("year")Integer year,@Param("month")Integer month);

    @Update("update fee_commission set state=-9 where id = #{id}")
    void del(Integer id);

    /**
     * 分页查询提成
     */
    List<Map> listFeeCommissionByPage(Map map);

    void updateBatch(Map map);

    void initArticleCommBatch(List<Integer> list);

    //对于企业供应商，必须是公司名称标准才能开票
    @Select({"<script>SELECT " +
            "  a.*,b.company_name company_name,b.cust_name cust_name,b.user_name user_name," +
            "(SELECT CONVERT (\n" +
            " group_concat(e. CODE, ':', c.amount),\n" +
            " CHAR\n" +
            ") FROM fee_income e,fee_income_article c where  c.income_id = e.id and  c.article_id = a.id ) AS amountDetail " +
            " from t_biz_article a " +
            " inner join t_media_supplier tms on tms.id = a.supplier_id " +
            " left join t_biz_order b on a.order_id=b.id " +
            " left join sys_dept d on b.depat_id = d.id "+
            " WHERE a.state = 1 and b.state > -2" +
            " AND d.company_code = #{companyCode, jdbcType = VARCHAR}" +
            "  AND a.income_states = 1 " ,
            "  and a.issue_states=4" ,
            " and a.commission_states=#{commissionStates}" ,
            " <when test='userId!=null and userId!=\"\"'>",
            " AND b.user_id = #{userId}",
            " </when>",
            " <when test='deptId!=null and deptId!=\"\"'>",
            " AND FIND_IN_SET(b.depat_id,getChilds(#{deptId}))",
            " </when>",
            " <when test='custCompanyName!=null and custCompanyName!=\"\"'>",
            " AND b.company_name like concat('%',#{custCompanyName},'%')",
            " </when>",
            " <when test='title!=null and title!=\"\"'>",
            " AND a.title like concat('%',#{title},'%')",
            " </when>",
            " <when test='custName!=null and custName!=\"\"'>",
            " AND b.cust_name like concat('%',#{custName},'%')",
            " </when>",
            " <when test='mediaName!=null and mediaName!=\"\"'>",
            " AND a.media_name like concat('%',#{mediaName},'%')",
            " </when>",
            " <when test='mediaUserName!=null and mediaUserName!=\"\"'>",
            " AND a.media_user_name like concat('%',#{mediaUserName},'%')",
            " </when>",
            " <when test='publishStartTime!=null and publishStartTime!=\"\"'>",
            " AND a.issued_date &gt;= #{publishStartTime}",
            " </when>",
            " <when test='publishEndTime!=null and publishEndTime!=\"\"'>",
            " AND a.issued_date &lt;= #{publishEndTime}",
            " </when>",
            " <when test='incomeStates!=null and incomeStates!=\"\"'>",
            " AND a.income_states = #{incomeStates}",
            " </when>",
            " <when test='outgoStates!=null and outgoStates!=\"\"'>",
            " AND a.outgo_states = #{outgoStates}",
            " </when>",
            " <when test='invoiceStates!=null and invoiceStates!=\"\"'>",
            " AND a.invoice_states = #{invoiceStates}",
            " </when>",
            " <when test='refundStates!=null and refundStates!=\"\"'>",
            " AND a.refund_states = #{refundStates}",
            " </when>",
            " <when test='otherPayStates!=null and otherPayStates!=\"\"'>",
            " AND a.other_pay_states = #{otherPayStates}",
            " </when>",
            " <when test='tradeMan!=null and tradeMan!=\"\"'>",
            " AND a.income_man like concat('%',#{tradeMan},'%')",
            " </when>",
            " <when test='tradeStartTime!=null and tradeStartTime!=\"\"'>",
            " AND a.income_date &gt;= #{tradeStartTime}",
            " </when>",
            " <when test='tradeEndTime!=null and tradeEndTime!=\"\"'>",
            " AND a.income_date &lt;= #{tradeEndTime}",
            " </when>",
            " <when test='incomeCode!=null and incomeCode!=\"\"'>",
            " AND a.income_code = #{incomeCode}",
            " </when>",
            " order by a.id desc" ,
            "</script>"})
    List<Map> queryArticleByCommStates(Map map);

    /**
     * @param map
     * @return
     */
    @Select({"<script>select " +
            "  a.*,b.company_name company_name,b.cust_name cust_name,b.user_name user_name," +
            "(SELECT CONVERT (\n" +
            " group_concat(e. CODE, ':', c.amount),\n" +
            " CHAR\n" +
            ") FROM fee_income e,fee_income_article c where  c.income_id = e.id and  c.article_id = a.id ) AS amountDetail " +
            " from t_biz_article a " +
            " left join t_biz_order b on a.order_id=b.id "+
            " WHERE a.state = 1 and b.state>-2" +
            "  AND a.income_states = 1 " +
            " and a.issue_states=4" +
            " and b.user_id=#{userId}" +
            "<when test='commissionYear!=null and commissionYear!=\"\"'>",
            " AND a.year = #{commissionYear}",
            " </when> ",
            "<when test='commissionMonth!=null and commissionMonth!=\"\"'>",
            " AND a.month = #{commissionMonth}",
            " </when> ",
            " <when test='custCompanyName!=null and custCompanyName!=\"\"'>",
            " AND b.company_name like concat('%',#{custCompanyName},'%')",
            " </when>",
            " <when test='custName!=null and custName!=\"\"'>",
            " AND b.cust_name like concat('%',#{custName},'%')",
            " </when>",
            " <when test='title!=null and title!=\"\"'>",
            " AND a.title like concat('%',#{title},'%')",
            " </when>",
            " <when test='mediaName!=null and mediaName!=\"\"'>",
            " AND a.media_name like concat('%',#{mediaName},'%')",
            " </when>",
            " <when test='mediaUserName!=null and mediaUserName!=\"\"'>",
            " AND a.media_user_name like concat('%',#{mediaUserName},'%')",
            " </when>",
            " <when test='publishStartTime!=null and publishStartTime!=\"\"'>",
            " AND a.issued_date &gt;= #{publishStartTime}",
            " </when>",
            " <when test='publishEndTime!=null and publishEndTime!=\"\"'>",
            " AND a.issued_date &lt;= #{publishEndTime}",
            " </when>",
            " <when test='incomeStates!=null and incomeStates!=\"\"'>",
            " AND a.income_states = #{incomeStates}",
            " </when>",
            " <when test='outgoStates!=null and outgoStates!=\"\"'>",
            " AND a.outgo_states = #{outgoStates}",
            " </when>",
            " <when test='invoiceStates!=null and invoiceStates!=\"\"'>",
            " AND a.invoice_states = #{invoiceStates}",
            " </when>",
            " <when test='refundStates!=null and refundStates!=\"\"'>",
            " AND a.refund_states = #{refundStates}",
            " </when>",
            " <when test='otherPayStates!=null and otherPayStates!=\"\"'>",
            " AND a.other_pay_states = #{otherPayStates}",
            " </when>",
            " <when test='tradeMan!=null and tradeMan!=\"\"'>",
            " AND a.income_man like concat('%',#{tradeMan},'%')",
            " </when>",
            " <when test='tradeStartTime!=null and tradeStartTime!=\"\"'>",
            " AND a.income_date &gt;= #{tradeStartTime}",
            " </when>",
            " <when test='tradeEndTime!=null and tradeEndTime!=\"\"'>",
            " AND a.income_date &lt;= #{tradeEndTime}",
            " </when>",
            " <when test='incomeCode!=null and incomeCode!=\"\"'>",
            " AND a.income_code = #{incomeCode}",
            " </when>",
            " <when test='commissionStartTime!=null and commissionStartTime!=\"\"'>",
            " AND a.commission_date &gt;= STR_TO_DATE(CONCAT(#{commissionStartTime},' 00:00:00'),'%Y-%m-%d %T')",
            " </when>",
            " <when test='commissionEndTime!=null and commissionEndTime!=\"\"'>",
            " AND a.commission_date &lt;= STR_TO_DATE(CONCAT(#{commissionEndTime},' 23:59:59'),'%Y-%m-%d %T')",
            " </when>",
            " <when test='commissionStates!=null and commissionStates!=\"\"'>",
            " and a.commission_states=#{commissionStates}" ,
            " </when>",
            " <when test='year!=null and year!=\"\"'>",
            " and a.year=#{year}" ,
            " </when>",
            " <when test='month!=null and month!=\"\"'>",
            " and a.month=#{month}" ,
            " </when>",
            " order by a.id desc" ,
            "</script>"})
    List<Map> queryArticleByUserAndYearAndMonth(Map map);

    /**
     * 更新提成状态
     *
     * @param map
     */
    void changeCommissionState(Map map);

    @Select("select a.* from t_biz_article a left join t_biz_order b on a.order_id=b.id and b.state>-2 where a.state = 1 and a.issue_states = 4 " +
            " and b.user_id=#{userId} and a.year=#{year} and a.month=#{month}")
    List<Article> queryArticleListByUserAndYearAndMonth(@Param("userId") Integer userId, @Param("year") Integer year, @Param("month") Integer month) ;

}
