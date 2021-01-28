package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.fee.Income;
import com.qinfei.qferp.entity.fee.IncomeArticle;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface IncomeMapper extends BaseMapper<Income,Integer> {


    @Select({"<script>",
            " SELECT a.* " +
                    " ,CONVERT(GROUP_CONCAT(b.name,':',b.receive_amount)USING utf8) receiveInfo" +
                " FROM fee_income a left join fee_income_user b on a.id=b.income_id and b.state>-2 " +
                " where a.state > -2" +
                    " <choose>",
                        " <when test='roleCode == \"ZC\"'>",
                        " and a.company_code != null ",
                        " </when>",
                        " <when test='roleType == \"JT\"'>",
                        " </when>",
                        " <when test='roleCode == \"ZJL\" or roleCode==\"FZ\"'>",
                            " and a.company_code = #{user.dept.companyCode} ",
                        " </when>",
                        " <when test='roleType==\"CW\"'>",
                            "  and a.company_code = #{user.dept.companyCode} ",
                        " </when>",
                        " <when test='roleType==\"YW\"'>",
                            " <choose>",
                                " <when test='roleCode==\"ZL\"'>",
                                    " and a.company_code = #{user.dept.companyCode} and #{deptId} in (select d.dept_id from fee_account_dept d where d.account_id=a.account_id) and a.level='C' and a.create_time &gt;= #{startDate} and a.unclaimed_amount>0 ",
                                " </when>",
                                " <when test='roleCode==\"YG\"'>",
                                    " and a.company_code = #{user.dept.companyCode} and #{deptId} in (select d.dept_id from fee_account_dept d where d.account_id=a.account_id) and a.level='C' and a.create_time &gt;= #{startDate} and a.unclaimed_amount>0 ",
                                " </when>",
                                " <when test='roleCode==\"ZZ\"'>",
                                    " and a.company_code = #{user.dept.companyCode} and #{deptId} in (select d.dept_id from fee_account_dept d where d.account_id=a.account_id) and a.level in ('B','C') and a.create_time &gt;= #{startDate} and a.unclaimed_amount>0 ",
                                " </when>",
                                " <when test='roleCode==\"BZ\"'>",
                                    " and a.company_code = #{user.dept.companyCode} and #{deptId} in (select d.dept_id from fee_account_dept d where d.account_id=a.account_id) and a.level in ('B','C') and a.create_time &gt;= #{startDate} and a.unclaimed_amount>0 ",
                                " </when>",
                                " <when test='roleCode==\"ZJ\"'>",
                                    " and a.company_code = #{user.dept.companyCode} and #{deptId} in (select d.dept_id from fee_account_dept d where d.account_id=a.account_id) and a.level in ('B','C') and a.create_time &gt;= #{startDate} and a.unclaimed_amount>0 ",
                                " </when>",
                                " <otherwise>",
                                    " and  a.id=0",
                                " </otherwise>",
                            "</choose>",
                        " </when>",
                        " <otherwise>",
                            " and  a.id=0",
                        " </otherwise>",
                    " </choose>",
                    " <choose>",
                    " <when test='commissionStates==0'>",
                    " and a.unclaimed_amount=0",
                    " </when>",
                    " <when test='commissionStates==1'>",
                    " and a.unclaimed_amount>0",
                    " </when>",
                    "</choose>",
                    " <otherwise>",
                    " </otherwise>",
                    " <when test='codeQc!=null and codeQc!=\"\"'>",
                    " AND a.code like concat('%',#{codeQc},'%')",
                    " </when>",
                    " <when test='accountNameQc!=null and accountNameQc!=\"\"'>",
                    " AND a.account_name like concat('%',#{accountNameQc},'%')",
                    " </when>",
                    " <when test='bankNoQc!=null and bankNoQc!=\"\"'>",
                    " AND a.bank_no like concat('%',#{bankNoQc},'%')",
                    " </when>",
                    " <when test='preclaimedAmount!=null and preclaimedAmount!=\"\"'>",
                    " AND a.preclaimed_amount like concat('%',#{preclaimedAmount},'%')",
                    " </when>",
                    " <when test='unclaimedAmount!=null and unclaimedAmount!=\"\"'>",
                    " AND a.unclaimed_amount like concat('%',#{unclaimedAmount},'%')",
                    " </when>",

                    " <when test='tradeManQc!=null and tradeManQc!=\"\"'>",
                    " AND a.trade_man like concat('%',#{tradeManQc},'%')",
                    " </when>",
                    " <when test='tradeBankQc!=null and tradeBankQc!=\"\"'>",
                    " AND a.trade_bank like concat('%',#{tradeBankQc},'%')'",
                    " </when>",
                    " <when test='startTimeQc!=null and startTimeQc!=\"\"'>",
                    " AND a.trade_time &gt;= #{startTimeQc}",
                    " </when>",
                    " <when test='endTimeQc!=null and endTimeQc!=\"\"'>",
                    " AND a.trade_time &lt;= STR_TO_DATE(concat(#{endTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
                    " </when>",
                    " <when test='tradeAmountQc!=null and tradeAmountQc!=\"\"'>",
                    " AND a.trade_amount like concat('%',#{tradeAmountQc},'%')",
                    " </when>",
                    " <when test='levelQc!=null and levelQc!=\"\"'>",
                    " AND a.level like concat('%',#{levelQc},'%')",
                    " </when>",
                    "  group by a.id",
                    " order by ",
                    " <when test='sidx != null and sidx != \"\"'>",
                    " ${sidx} ${sord}",
                    " </when>",
                    " <when test='sidx == null or sidx ==  \"\"'>",
                    " a.id desc",
                    " </when>",
                    "</script>"})
    List<Map> listPg(Map map);

    @Select("select * from fee_income where state>-2 and id = #{id}")
    Income getById(Integer id);

    /**
     * 查询分款未完成的稿件，用于分款
     * 条件：未分款或分款未完成 && 已发布 && 已完善客户 && 当前业务员 && 去除2019年1月份老系统的稿件
     * @param map
     * @return 未分款的稿件列表
     */
    @Select({"<script>",
            " SELECT a.*,b.no no,b.user_name userName,b.company_name companyName,b.cust_name custName " +
                " FROM t_biz_article a,t_biz_order b " +
                " where a.order_id=b.id and a.state = 1 and b.state>-2 " +
                    " and a.income_states!=1 and a.issue_states=4 and b.cust_id is not null " +
                    " and b.user_id=#{userId} AND (year(a.issued_date)=2019 and month(a.issued_date)!=1 or year(a.issued_date)!=2019)" +
                    " <when test='custCompanyNameQc!=null and custCompanyNameQc!=\"\"'>",
                    " AND b.company_name like concat('%',#{custCompanyNameQc},'%')",
                    " </when>",
                    " <when test='custNameQc!=null and custNameQc!=\"\"'>",
                    " AND b.cust_name like concat('%',#{custNameQc},'%')",
                    " </when>",
                    " <when test='titleQc!=null and titleQc!=\"\"'>",
                    " AND a.title like concat('%',#{titleQc},'%')",
                    " </when>",
                    " <when test='invoiceStates!=null and invoiceStates!=\"\"'>",
                    " AND a.invoice_states = #{invoiceStates}",
                    " </when>",
                    " <when test='startTime!=null and startTime!=\"\"'>",
                    " AND a.issued_date &gt;= #{startTime}",
                    " </when>",
                    " <when test='brandQc!=null and brandQc!=\"\"'>",
                    " AND a.brand like concat('%',#{brandQc},'%')",
                    " </when>",
                    " <when test='mediaName!=null and mediaName!=\"\"'>",
                    " AND a.media_name like concat('%',#{mediaName},'%')",
                    " </when>",
                    " <when test='endTime!=null and endTime!=\"\"'>",
                    " AND a.issued_date &lt;= #{endTime}",
                    " </when>",
                    " <when test='saleAmount!=null and saleAmount!=\"\"'>",
                    " AND a.sale_amount = #{saleAmount}",
                    " </when>",
                    " <when test='electricityBusinesses!=null and electricityBusinesses!=\"\"'>",
                    " AND a.electricity_businesses like concat('%',#{electricityBusinesses},'%')",
                    " </when>",
                    " <when test='mediaUserName!=null and mediaUserName!=\"\"'>",
                    " AND a.media_user_name like concat('%',#{mediaUserName},'%')",
                    " </when>",
            "  order by a.issued_date asc,a.id desc",
            "</script>"})
    List<Map> queryArticleForAssign(Map map);

    /**
     * 根据进账id查询出该进账的已分款详情
     * @param incomeId 进账id
     * @return
     */
    @Select({"<script>",
            " select a.id incomeId,c.id articleId,a.code code,c.invoice_states invoiceStates,c.commission_states commStates," +
                    "c.supplier_name supplierName,c.media_name mediaName,c.media_user_name mediaUserName," +
                    "c.title title,c.link link, c.income_amount incomeAmount,c.outgo_amount outgoAmount,c.issued_date issueDate, " +
                    "c.sale_amount saleAmount,c.pay_amount payAmount,b.amount assignAmount,d.user_name userName," +
                    " DATE_FORMAT(b.date,\"%Y-%m-%d %H:%i\") assignDate," +
                    " DATE_FORMAT(c.issued_date,\"%Y-%m-%d\") issuedDate," +
                    " d.no no,d.company_name companyName,d.cust_name custName,c.media_type_name mediaTypeName,c.brand,brand" +
                    " FROM fee_income a,fee_income_article b,t_biz_article c,t_biz_order d" +
                    " where a.id=b.income_id and b.article_id=c.id and a.id=#{incomeId} and c.order_id=d.id " +
                    " and a.state >-2 and b.state>-2 and c.state>-2 and d.state>-2 " +
                    " order by c.issued_date asc,c.id desc",
            "</script>"})
    List<Map> listPgForSelectedArticle(Integer incomeId);

    /**
     * 根据稿件id查询出当前稿件的进账列表
     * 一个稿件可能有多个进账
     * @param article 只有一个id有值
     * @return
     */
    @Select({"select a.*,b.date date,b.amount amount from fee_income a " +
            "left join fee_income_article b on a.id = b.income_id " +
            "where a.state>-2 and b.article_id = #{id}"})
    List<Map> listPgByArticleId(Article article);

    @Select("select t.* from fee_income t where t.state>-2 and t.account_id=#{accountId}")
    List<Income> queryIncomeByAccountId(Integer accountId);

    @Select("select t.id incomeId,t.code incomeCode,t.account_name incomeAccount, " +
            " t.trade_man tradeMan,t.trade_amount tradeAmount,t.trade_time tradeTime," +
            " fia.date assignDate" +
            " from fee_income t " +
            " left join fee_income_article fia on t.id=fia.income_id " +
            " where t.state>-2 and fia.article_id = #{articleId} order by fia.id desc ")
    List<Map> queryIncomeByArticleId(Integer articleId);

    @Select("select t.id incomeId,t.code incomeCode,t.account_name incomeAccount, " +
            " t.trade_man tradeMan,t.trade_amount tradeAmount,t.trade_time tradeTime," +
            " fia.date assignDate" +
            " from fee_income t " +
            " left join fee_income_article fia on t.id=fia.income_id " +
            " where t.state>-2 and fia.article_id = #{articleId} and t.id != #{incomeId} order by fia.id desc ")
    List<Map> queryIncomeByArticleIdAndNotIncomeId(@Param("articleId") Integer articleId, @Param("incomeId") Integer incomeId);

    @Insert({"<script>",
            " insert into fee_income (" +
                    "account_id," +
                    "account_name," +
                    "bank_no," +
                    "code," +
                    "trade_time," +
                    "trade_bank," +
                    "trade_man," +
                    "trade_amount," +
                    "unclaimed_amount," +
                    "preclaimed_amount," +
                    "cust_company_id," +
                    "cust_company_name," +
                    "state," +
                    "creator," +
                    "create_time," +
                    "update_user_id," +
                    "update_time," +
                    "dept_id," +
                    "dept_name," +
                    "task_id," +
                    "level," +
                    "visiable_day," +
                    "deadline," +
                    "company_code" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.accountId}," +
                    "#{item.accountName}," +
                    "#{item.bankNo}," +
                    "#{item.code}," +
                    "#{item.tradeTime}," +
                    "#{item.tradeBank}," +
                    "#{item.tradeMan}," +
                    "#{item.tradeAmount}," +
                    "#{item.unclaimedAmount}," +
                    "#{item.preclaimedAmount}," +
                    "#{item.custCompanyId}," +
                    "#{item.custCompanyName},#{item.state},#{item.creator},#{item.createTime},#{item.updateUserId}," +
                    "#{item.updateTime}," +
                    "#{item.deptId}," +
                    "#{item.deptName}," +
                    "#{item.taskId}," +
                    "#{item.level}," +
                    "#{item.visiableDay}," +
                    "#{item.deadline}," +
                    "#{item.companyCode})" +
                    "</foreach>",
            "</script>"})
    void saveBatch(List<Income> list);

    @Update({"<script>",
            " update t_biz_article " +
                    "<trim prefix='set' suffixOverrides=','>" +
                        "<trim prefix='income_states = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.incomeStates} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix='income_amount = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.incomeAmount} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix='income_id = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.incomeId} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix='income_code = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.incomeCode} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix='income_account = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.incomeAccount} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix='income_man = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.incomeMan} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix='income_total_amount = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.incomeTotalAmount} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix='income_date = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.incomeDate} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix='assign_date = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.assignDate} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix='profit = case' suffix='end,'>" +
                        "<foreach collection='list' item='item' index='index' >" +
                        " when id=#{item.id} then #{item.profit} " +
                        "</foreach>" +
                        "</trim>" +
                        "<trim prefix=\"alter_flag =case\" suffix=\"end,\">" +
                        " <foreach collection=\"list\" item=\"item\" index=\"index\">" +
                        "  <if test=\"item.alterFlag!=null\">" +
                        "   when id=#{item.id} then #{item.alterFlag}" +
                        "  </if>" +
                        " </foreach>" +
                        "</trim>" +
                    "</trim>" +
                    " where " +
                    " <foreach collection='list' separator='or' item='i' index='index'>" +
                    " id=#{i.id}" +
                    " </foreach>",
            "</script>"})
    void updateArticleIncomeInfoBatch(List<Article> list);

    @Insert({"<script>",
            " insert into fee_income_article (" +
                    "income_id," +
                    "article_id," +
                    "amount," +
                    "date," +
                    "income_user_id" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.incomeId}," +
                    "#{item.articleId}," +
                    "#{item.amount}," +
                    "now()," +
                    "#{item.incomeUserId})" +
                    "</foreach>",
            "</script>"})
    void insertIncomeArticleBatch(List<IncomeArticle> list);

    @Select({"<script>" +
            "select sum(b.trade_amount)tradeAmount ,sum(a.receive_amount )receiveAmount,sum(a.assign_amount)assignAmount,sum(a.remain_amount)remainAmount" +
            " from fee_income_user a,fee_income b" +
            " where a.income_id=b.id and a.state>-2 and b.state>-2 and a.user_id=#{id}" +
            " <when test='accountNameQc!=null and accountNameQc!=\"\"'>",
            " AND b.account_name like concat('%',#{accountNameQc},'%')",
            " </when>",
            " <when test='codeQc!=null and codeQc!=\"\"'>",
            " AND b.code like concat('%',#{codeQc},'%')",
            " </when>",
            " <when test='bankNoQc!=null and bankNoQc!=\"\"'>",
            " AND b.bank_no like concat('%',#{bankNoQc},'%')",
            " </when>",
            " <when test='tradeManQc!=null and tradeManQc!=\"\"'>",
            " AND b.trade_man like concat('%',#{tradeManQc},'%')",
            " </when>",
            " <when test='tradeBankQc!=null and tradeBankQc!=\"\"'>",
            " AND b.trade_bank like concat('%',#{tradeBankQc},'%')",
            " </when>",
            " <when test='startTimeQc!=null and startTimeQc!=\"\"'>",
            " AND b.trade_time &gt;= #{startTimeQc}",
            " </when>",
            " <when test='endTimeQc!=null and endTimeQc!=\"\"'>",
            " AND b.trade_time &lt;= #{endTimeQc}",
            " </when>",
            " <when test='tradeAmountQc!=null and tradeAmountQc!=\"\"'>",
            " AND b.trade_amount like concat('%',#{tradeAmountQc},'%')",
            " </when>",
            " <when test='incomeQc!=null and incomeQc!=\"\"'>",
            " AND b.trade_amount = #{incomeQc}",
            " </when>",
            " order by a.id desc ",
            "</script>"})
    Map reimburseSum(Map map);

    /**
     *根据公司查询业务员未分款
     * @return
     */
    @Select({"SELECT\n" +
            " a.user_id userId,\n" +
            " a. NAME userName,\n" +
            " a.dept_id deptId,\n" +
            " a.dept_name deptName,\n" +
            " sum(a.remain_amount) remainAmountSum\n" +
            " FROM\n" +
            " fee_income_user a\n" +
            " LEFT JOIN sys_dept b ON a.dept_id = b.id\n" +
            " WHERE\n" +
            " a.remain_amount > 0\n" +
            " AND a.state > - 2\n" +
            " AND b.company_code = #{companyCode}\n" +
            " GROUP BY\n" +
            " a.user_id,\n" +
            " a. NAME,\n" +
            " a.dept_id,\n" +
            " a.dept_name"})
    List<Map> queryNotAssignAmount(String companyCode);
}
