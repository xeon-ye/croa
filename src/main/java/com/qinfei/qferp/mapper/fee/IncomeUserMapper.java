package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.core.utils.ProviderUtil;
import com.qinfei.qferp.entity.fee.IncomeUser;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;


public interface IncomeUserMapper extends BaseMapper<IncomeUser,Integer> {

    @Select("select * from fee_income_user where state>-2 and id = #{id}")
    IncomeUser getById(Integer id);

    @Select("select b.* from fee_income_user b where b.state>-2 and b.income_id = #{incomeId} and b.user_id = #{userId}")
    IncomeUser getIncomeUser(@Param("incomeId") Integer incomeId, @Param("userId") Integer userId);

    /**
     * 查询出当前用户的已领款的列表
     * @param map
     * @return
     */
    @Select({"<script>",
            " select b.code code,a.name name,a.dept_name deptName,a.receive_amount receiveAmount,a.name name," +
                    " DATE_FORMAT(a.receive_time,\"%Y-%m-%d %H:%i\") receiveTime," +
                    " a.assign_amount assignAmount,a.remain_amount remainAmount," +
                    " b.id incomeId,b.account_name accountName,DATE_FORMAT(b.trade_time,\"%Y-%m-%d\") tradeTime," +
                    " b.trade_bank tradeBank,b.bank_no bankNo," +
                    " b.trade_man tradeMan,b.trade_amount tradeAmount,b.cust_company_name custCompanyName " +
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
                        " AND b.trade_time &lt;= STR_TO_DATE(concat(#{endTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
                        " </when>",
                        " <when test='tradeAmountQc!=null and tradeAmountQc!=\"\"'>",
                        " AND b.trade_amount like concat('%',#{tradeAmountQc},'%')",
                        " </when>",
                        " <when test='incomeQc!=null and incomeQc!=\"\"'>",
                        " AND b.trade_amount = #{incomeQc}",
                        " </when>",
                    " order by a.id desc ",
            "</script>"})
    List<Map> listPgForAssign(Map map);

    /**
     * 分款管理（财务）页面的列表数据，根据进账关联多张表查询
     * @param map
     * @return
     */
    @Select({"<script>",
            " select a.id incomeId,a.code incomeCode,a.account_name accountName,a.unclaimed_amount unclaimedAmount\n" +
            ",a.trade_man tradeMan,a.trade_bank tradeBank,a.trade_amount tradeAmountQC,a.preclaimed_amount preclaimedAmount\n" +
                    ",DATE_FORMAT(a.trade_time,\"%Y-%m-%d\") tradeTime\n" +
                    ",a.trade_amount tradeAmount,a.unclaimed_amount unclaimedAmount,a.preclaimed_amount preclaimedAmount\n" +
                    ",a.level level,DATE_FORMAT(a.create_time,\"%Y-%m-%d %H:%i\") createTime,a.visiable_day visiableDay" +
                    ",b.id incomeUserId,b.name userName,b.dept_name deptName,b.receive_amount receiveAmount\n" +
                    ",DATE_FORMAT(b.receive_time,\"%Y-%m-%d %H:%i\") receiveTime\n" +
                    ",b.assign_amount assignAmount,b.remain_amount remainAmount \n" +
                    ",c.amount amount,DATE_FORMAT(c.date,\"%Y-%m-%d %H:%i\") assignDate\n" +
                    ",d.id articleId,d.supplier_name supplierName,d.media_name mediaName,d.media_user_name mediaUserName\n" +
                    ",d.title title,d.link link, d.sale_amount saleAmount, d.income_amount incomeAmount\n" +
                    ",d.pay_amount payAmount,d.outgo_amount outgoAmount,DATE_FORMAT(d.issued_date,\"%Y-%m-%d\") issuedDate\n" +
                    ",e.no no,e.company_name companyName, e.cust_name custName,s.company_code companyCode,s.phone,d.supplier_contactor as supplierContactor" +
                    " from fee_income a \n" +
                    " left join fee_income_user b on a.id=b.income_id and b.state>-2 \n" +
                    " left join fee_income_article c on a.id=c.income_id and b.user_id=c.income_user_id and c.state>-2 \n" +
                    " left join t_biz_article d on c.article_id=d.id and d.state>-2 \n" +
                    " left join t_biz_order e on d.order_id=e.id  and e.state>-2 \n" +
                    "left join t_media_supplier s on d.supplier_id = s.id " +
                    " where a.state>-2 " +
                    " <when test='companyCode!=null and companyCode!=\"\" and companyCode!=\"JT\"'>" ,
                    " and a.company_code=#{companyCode}",
                    "</when>",
                " <when test='companyName!=null and companyName!=\"\"'>",
                " AND e.company_name like concat('%',#{companyName},'%')",
                " </when>",
                " <when test='deptIds!=null and deptIds!=\"\"'>",
                " AND b.dept_id in (${deptIds})",
                " </when>",
                " <when test='custName!=null and custName!=\"\"'>",
                " AND e.cust_name like concat('%',#{custName},'%')",
                " </when>"," " +
                "<when test='supplierName!=null and supplierName!=\"\"'>",
                " AND d.supplier_name like concat('%',#{supplierName},'%')",
                " </when>",
                " <when test='mediaName!=null and mediaName!=\"\"'>",
                " AND d.media_name like concat('%',#{mediaName},'%')",
                " </when>",
                " <when test='userName!=null and userName!=\"\"'>",
                " AND b.name like concat('%',#{userName},'%')",
                " </when>",
                " <when test='mediaUserName!=null and mediaUserName!=\"\"'>",
                " AND d.media_user_name like concat('%',#{mediaUserName},'%')",
                " </when>",
                " <when test='code!=null and code!=\"\"'>",
                " AND a.code like concat('%',#{code},'%')",
                " </when>",
                " <when test='title!=null and title!=\"\"'>",
                " AND d.title like concat('%',#{title},'%')",
                " </when>",
                " <when test='receiveStates!=null and receiveStates!=\"\"'>",
                    " <when test='receiveStates==0'>" +
                    "  " +
                    " </when> ",
                    " <when test='receiveStates==1'>" +
                    " AND a.preclaimed_amount=0" +
                    " </when> ",
                    " <when test='receiveStates==2'>" +
                    " AND a.unclaimed_amount>0 AND a.preclaimed_amount>0" +
                    " </when> ",
                    " <when test='receiveStates==3'>" +
                    " AND a.unclaimed_amount=0" +
                    " </when> ",
                    " <when test='receiveStates==4'>" +
                    " AND d.id is not null" +
                    " </when> ",
                " </when>",
                " <when test='startTime!=null and startTime!=\"\"'>",
                " AND a.trade_time &gt;= #{startTime}",
                " </when>",
                " <when test='grade!=null and grade!=\"\"'>",
                " AND a.level = #{grade}",
                " </when>",
                " <when test='endTime!=null and endTime!=\"\"'>",
                " AND a.trade_time &lt;= STR_TO_DATE(concat(#{endTime},' 23:59:59'),'%Y/%m/%d %T')",
                " </when>",
                " <when test='startTimelk!=null and startTimelk!=\"\"'>",
                " AND b.receive_time &gt;= #{startTimelk}",
                " </when>",
                " <when test='endTimelk!=null and endTimelk!=\"\"'>",
                " AND b.receive_time &lt;= STR_TO_DATE(concat(#{endTimelk},' 23:59:59'),'%Y/%m/%d %T')",
                " </when>",
                " <when test='startTimefk!=null and startTimefk!=\"\"'>",
                " AND c.date &gt;= #{startTimefk}",
                " </when>",
                " <when test='endTimefk!=null and endTimefk!=\"\"'>",
                " AND c.date &lt;= STR_TO_DATE(concat(#{endTimefk},' 23:59:59'),'%Y/%m/%d %T')",
                " </when>",
                " <when test='startTimefb!=null and startTimefb!=\"\"'>",
                " AND d.issued_date &gt;= #{startTimefb}",
                " </when>",
                " <when test='endTimefb!=null and endTimefb!=\"\"'>",
                " AND d.issued_date &lt;= STR_TO_DATE(concat(#{endTimefb},' 23:59:59'),'%Y/%m/%d %T')",
                " </when>",
                " <when test='tradeMan!=null and tradeMan!=\"\"'>",
                " AND a.trade_man like concat('%',#{tradeMan},'%')",
                " </when>",
                " <when test='accountName!=null and accountName!=\"\"'>",
                " AND a.account_name like concat('%',#{accountName},'%')",
                " </when>",
            " <when test='tradeAmountQc!=null and tradeAmountQc!=\"\"'>",
            " AND a.trade_amount like concat('%',#{tradeAmountQc},'%')",
            " </when>",
            " <when test='unclaimedAmount!=null and unclaimedAmount!=\"\"'>",
            " AND a.unclaimed_amount like concat('%',#{unclaimedAmount},'%')",
            " </when>",
            " <when test='preclaimedAmount!=null and preclaimedAmount!=\"\"'>",
            " AND a.preclaimed_amount like concat('%',#{preclaimedAmount},'%')",
            " </when>",
                " order by a.id desc ",
            "</script>"})
    List<Map> listPgForAssignCW(Map map);

    /**
     * 根据进账id查询出当前进账的领款人列表
     * @param incomeId
     * @return
     */
    @Select("select b.* from fee_income_user b where b.state>-2 and b.income_id = #{incomeId}")
    List<IncomeUser> queryIncomeUserByIncomeId(@Param("incomeId") Integer incomeId);

    @Select("select b.* from fee_income_user b where b.state>-2 and b.income_id = #{incomeId} and user_id = #{userId}")
    List<IncomeUser> queryIncomeUserByIncomeIdAndUserId(@Param("incomeId") Integer incomeId,@Param("userId") Integer userId);

    @Select("select sum(b.receive_amount) receiveSum,sum(b.remain_amount) remainSum from fee_income_user b where b.state>-2 and b.income_id = #{incomeId} and user_id = #{userId}")
    Map querySumAmount(@Param("incomeId") Integer incomeId,@Param("userId") Integer userId);

}
