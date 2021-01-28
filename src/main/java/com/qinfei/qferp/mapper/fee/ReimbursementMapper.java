package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.Reimbursement;
import com.qinfei.qferp.entity.fee.Reimbursement_d;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface ReimbursementMapper extends BaseMapper<Reimbursement, Integer> {

    //获取到主表中的列表
    //一次性获取到主表中的所有数据
    @Select({ "<script>",
            " SELECT rem.id id,rem.code code,rem.title title,rem.type type,"
                    + " rem.apply_id applyId,rem.apply_name applyName,rem.dept_id deptId, "
                    + " rem.dept_name deptName,rem.account_name accountName,rem.account_bank_no accountBankNo, "
                    + " rem.account_bank_name accountBankName,rem.apply_time applyTime,rem.apply_amount applyAmount, "
                    + " rem.expert_pay_time expertPayTime,rem.out_account_id outAccountId,rem.out_account_name outAccountName,"
                    + " rem.pay_time payTime,rem.pay_user_id payUserId,rem.remark remark,rem.affix affix,rem.state state,"
                    + " rem.task_id taskId,rem.item_id itemId,rem.loan_qc loanQc,rem.unpaid_loan unpaidLoan,rem.reimbursed_money reimbursedMoney, "
                    + " rem.total_money totalMoney,rem.sum_upper sumUpper,rem.affix_name affixName,rem.affix_link affixLink,rem.pay_amount payAmount,tii.accept_worker acceptWorker "
                    + " FROM FEE_REIMBURSEMENT rem " +
                    " LEFT JOIN t_index_items tii on rem.item_id = tii.id  "
                    + " WHERE rem.state>-2",
            " <choose>",
                " <when test='roleType == \"JT\"'>",
                    "<choose>",
                        /*集团角色，总裁副总裁和会计可以看到所有公司的报销，其他角色只能看到自己的*/
                        " <when test='roleCode==\"ZC\" or roleCode==\"FZC\" or roleCode==\"KJ\" or roleCode==\"CN\"'>",
                            " and rem.id>0 ",
                        " </when>",
                        " <otherwise>",
                            " and rem.apply_id = #{user.id}",
                        " </otherwise>",
                    "</choose>",
                " </when>",
                " <when test='roleType == \"ZJB\"'>",
                    "<choose>",
                    /*总经办角色，总经理和副总经理可以看到所在公司的报销，其他角色只能看到自己的*/
                        " <when test='roleCode==\"ZJL\" or roleCode==\"FZ\"'>",
                            " and rem.company_code=#{user.companyCode} ",
                        " </when>",
                        " <otherwise>",
                            " and rem.apply_id = #{user.id}",
                        " </otherwise>",
                    "</choose>",
                " </when>",
                " <when test='roleType == \"CW\"'>",
                    "<choose>",
                        /*分公司财务角色，财务总监、财务部长、会计和出纳可以看到所有公司的报销，其他角色只能看到自己的*/
                        " <when test='roleCode==\"ZJ\" or roleCode==\"BZ\" or roleCode==\"KJ\" or roleCode==\"CN\"'>",
                            " and rem.company_code=#{user.companyCode} ",
                        " </when>",
                        " <otherwise>",
                            " and rem.apply_id = #{user.id}",
                        " </otherwise>",
                    "</choose>",
                " </when>",
                "<when test = 'roleType ==\"XM\"'>",
            "<choose>",
                        /*分公司财务角色，财务总监、财务部长、会计和出纳可以看到所有公司的报销，其他角色只能看到自己的*/
            " <when test='roleCode==\"ZJ\"'>",
            " and rem.apply_id = #{user.id} ",
            " </when>",
            " <otherwise>",
            " and rem.apply_id = #{user.id}",
            " </otherwise>",
            "</choose>",
            "</when>",
                " <otherwise>",
                    " <choose>",
                        /*除集团、总经办、财务外的角色，总监、部长、组长可以看到下属，下属只能看到自己的*/
                        " <when test='roleCode==\"ZJ\"'>",
                        " and ( <foreach collection='user.deptIdSet' item='item' open='  rem.dept_id in (' close=')' separator=','>",
                        " #{item}",
                        " </foreach> or rem.apply_id = #{user.id})",
                        " </when>",
                        " <when test='roleCode==\"BZ\"'>",
                        " and ( <foreach collection='user.deptIdSet' item='item' open='  rem.dept_id in (' close=')' separator=','>",
                        " #{item}",
                        " </foreach> or rem.apply_id = #{user.id})",
                        " </when>",
                        " <when test='roleCode==\"ZZ\"'>",
                        " and (rem.dept_id=#{user.deptId} or rem.apply_id = #{user.id})",
                        " </when>",
                        " <otherwise>",
                            " and rem.apply_id = #{user.id}",
                        " </otherwise>",
                    "</choose>",
                " </otherwise>",
            " </choose>",
                    " <when test='codeQc!=null and codeQc!=\"\"'>" ,
                    " AND rem.code like concat('%',#{codeQc},'%')",
                    "</when>",
                    " <when test='titleQc!=null and titleQc!=\"\"'>" ,
                    " AND rem.title like concat('%',#{titleQc},'%')",
                    "</when>",
                    " <when test='applyNameQc!=null and applyNameQc!=\"\"'>" ,
                    " AND rem.apply_name like concat('%',#{applyNameQc},'%')",
                    "</when>",
                    " <when test='userId!=null and userId!=\"\" and companyCode!=\"JT\"'>" ,
                    "  AND (rem.apply_id=#{userId} or rem.approver_id = #{userId})",
                    "</when>",
                    " <when test='deptIds!=null and deptIds!=\"\"'>" ,
                    " AND rem.dept_id in (${deptIds})",
                    "</when>",
                    " <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
                    " AND rem.apply_time &gt;= #{applyStartTimeQc}",
                    " </when>",
                    " <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
                    " AND rem.apply_time &lt;= STR_TO_DATE(concat(#{applyEndTimeQc},' 23:59:59'),'%Y-%m-%d %T')",
                    " </when>",
                    " <when test='payTimeStateQc!=null and payTimeStateQc!=\"\"'>",
                    " AND rem.pay_time &gt;= #{payTimeStateQc}",
                    " </when>",
                    " <when test='payTimeEndQc!=null and payTimeEndQc!=\"\"'>",
                    " AND rem.pay_time &lt;= STR_TO_DATE(concat(#{payTimeEndQc},' 23:59:59'),'%Y-%m-%d %T')",
                    " </when>",
                    " <when test='accountNameQc!=null and accountNameQc!=\"\"'>" ,
                    " AND rem.account_name like concat('%',#{accountNameQc},'%')",
                    "</when>",
                    " <when test='remarkQc!=null and remarkQc!=\"\"'>" ,
                    " AND rem.remark like concat('%',#{remarkQc},'%')",
                    "</when>",
                    " <when test='accountBankNameQc!=null and accountBankNameQc!=\"\"'>" ,
                    " AND rem.account_bank_name like concat('%',#{accountBankNameQc},'%')",
                    "</when>" ,
                    " <when test='outAccountNameQc!=null and outAccountNameQc!=\"\"'>",
                    " AND rem.out_account_name like concat('%',#{outAccountNameQc},'%')",
                    " </when>",
                    " <when test='payAmountQc!=null and payAmountQc!=\"\"'>",
                    " AND rem.pay_amount = #{payAmountQc}",
                    " </when>",
                    " <when test='stateQc!=null and stateQc!=\"\"'>" ,
                        "<choose>",
                            " <when test='stateQc==-1'>",
                            " AND rem.state =-1 ",
                            " </when>",
                            " <when test='stateQc==0'>",
                            " AND rem.state =0 ",
                            " </when>",
                            " <when test='stateQc==1'>",
                            " AND rem.state =1 ",
                            " </when>",
                            " <when test='stateQc==2'>",
                            " AND rem.state in (2,16) ",
                            " </when>",
                            " <when test='stateQc==4'>",
                            " AND rem.state =9 ",
                            " </when>",
                            " <when test='stateQc==3'>",
                            " AND rem.state in (3,4,5,6,7,8,10,12) ",
                            " </when>",
                            " <otherwise>",
                            " and  rem.id=0 ",
                            " </otherwise>",
                        "</choose>",
                    "</when>",
                    " ORDER BY ID desc ",
            "</script>"})
    List<Map> listPg(Map map);

    //往从表中添加数据的方法
    @Insert({"<script>",
            " insert into fee_reimbursement_d (" +
                    "rem_id," +
                    "cost_type," +
                    "purpose," +
                    "money," +
                    "number_of_document," +
                    "current_total_price," +
                    "update_user_id" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.remId}," +
                    "#{item.costType}," +
                    "#{item.purpose}," +
                    "#{item.money}," +
                    "#{item.numberOfDocument}," +
                    "#{item.currentTotalPrice}," +
                    "#{item.updateUserId})" +
                    "</foreach>",
            "</script>"})
    void saveBatch(List<Reimbursement_d> list);

    //通过id查询主表
    @Select("select * from fee_reimbursement where state>-2 and id = #{id}")
    Reimbursement getById(Integer id);
    //查询从表
    @Select("select * from fee_reimbursement_d where rem_id=#{id} ")
    List<Reimbursement_d> getReimbursement_dsById(Integer id);

    //查询从表
    @Select({"<script>",
            "select * from fee_reimbursement_d where rem_id in " +
                    "<foreach collection=\"ids\" item=\"id\" open=\"(\" close=\")\" separator=\",\">\n" +
                    "    #{id}\n" +
                    "</foreach>",
            "</script>"})
    List<Reimbursement_d> listReimbursementDetailByIds(@Param("ids") List<Integer> ids);

    @Update("delete from fee_reimbursement_d where rem_id=#{remId}")
    void delReimbursement_dByRemId(@Param("remId") Integer remId);

    @Update("UPDATE fee_reimbursement \n" +
            "SET out_account_id = NULL,\n" +
            " out_account_name = NULL,\n" +
            " pay_time = NULL,\n" +
            " pay_user_id = NULL,\n" +
            " pay_amount = 0,\n" +
            " state =- 1,\n" +
            " update_user_id = #{userId} \n" +
            "where id = #{id}")
    void returnRefundInfo(@Param("id") Integer id, @Param("userId") Integer userId);

    @Update("update fee_reimbursement set unpaid_loan=0 where id=#{id}")
    void initBorrowColumns(@Param("id") Integer id);

    //通过admId获取数据
    @Select("select * from fee_reimbursement where state>-2 and administrative_id = #{id}")
    Reimbursement getByAdmId(@Param("id")Integer admId);


    @Select("update fee_reimbursement SET out_account_id = #{outAccountId},out_account_name=#{outAccountName}," +
            "pay_amount =#{payAmount},pay_time =#{payTime} , pay_user_id =#{updateUserId}, update_user_id=#{updateUserId}," +
            "state = #{state} where id=#{id}")
    Reimbursement updateaccount(Reimbursement entity);

    @Select("update fee_reimbursement SET " +
            " pay_time =#{payTime}, pay_user_id =#{updateUserId}, update_user_id=#{updateUserId}" +
            " where id=#{id}")
    Reimbursement updatePayTime(Reimbursement entity);

    /**
     * 统计不要把state=-1和state=0的算进去
     * @param map
     * @return
     */
    @Select({"<script>"+
            "select SUM(rem.total_money) totalMoney,SUM(rem.pay_amount) payAmount from fee_reimbursement rem" +
            " WHERE rem.state>0 ",
        " <choose>",
            " <when test='roleType == \"JT\"'>",
                "<choose>",
                    /*集团角色，总裁副总裁和会计可以看到所有公司的报销，其他角色只能看到自己的*/
                    " <when test='roleCode==\"ZC\" or roleCode==\"FZC\" or roleCode==\"KJ\" '>",
                        " and rem.id>0 ",
                    " </when>",
                    " <otherwise>",
                        " and rem.apply_id = #{user.id}",
                    " </otherwise>",
                "</choose>",
            " </when>",
            " <when test='roleType == \"ZJB\"'>",
                "<choose>",
                /*总经办角色，总经理和副总经理可以看到所在公司的报销，其他角色只能看到自己的*/
                    " <when test='roleCode==\"ZJL\" or roleCode==\"FZ\"'>",
                        " and rem.company_code=#{user.companyCode} ",
                    " </when>",
                    " <otherwise>",
                        " and rem.apply_id = #{user.id}",
                    " </otherwise>",
                "</choose>",
            " </when>",
            "<when test = 'roleType ==\"XM\"'>",
            "<choose>",
                        /*分公司财务角色，财务总监、财务部长、会计和出纳可以看到所有公司的报销，其他角色只能看到自己的*/
            " <when test='roleCode==\"ZJ\"'>",
            " and rem.apply_id = #{user.id} ",
            " </when>",
            " <otherwise>",
            " and rem.apply_id = #{user.id}",
            " </otherwise>",
            "</choose>",
            "</when>",

            " <when test='roleType == \"CW\"'>",
                "<choose>",
                    /*分公司财务角色，财务总监、财务部长、会计和出纳可以看到所有公司的报销，其他角色只能看到自己的*/
                    " <when test='roleCode==\"ZJ\" or roleCode==\"BZ\" or roleCode==\"KJ\" or roleCode==\"CN\"'>",
                        " and rem.company_code=#{user.companyCode} ",
                    " </when>",
                    " <otherwise>",
                        " and rem.apply_id = #{user.id}",
                    " </otherwise>",
                "</choose>",
            " </when>",
            " <otherwise>",
                " <choose>",
                    /*除集团、总经办、财务外的角色，总监、部长、组长可以看到下属，下属只能看到自己的*/
                    " <when test='roleCode==\"ZJ\"'>",
                        " and ( <foreach collection='user.deptIdSet' item='item' open='  rem.dept_id in (' close=')' separator=','>",
                            " #{item}",
                        " </foreach> or rem.apply_id = #{user.id})",
                    " </when>",
                    " <when test='roleCode==\"BZ\"'>",
                        " and ( <foreach collection='user.deptIdSet' item='item' open='  rem.dept_id in (' close=')' separator=','>",
                            " #{item}",
                        " </foreach> or rem.apply_id = #{user.id})",
                    " </when>",
                    " <when test='roleCode==\"ZZ\"'>",
                        " and (rem.dept_id=#{user.deptId} or rem.apply_id = #{user.id})",
                    " </when>",
                    " <otherwise>",
                        " and rem.apply_id = #{user.id}",
                    " </otherwise>",
                "</choose>",
            " </otherwise>",
        " </choose>",
            " <when test='codeQc!=null and codeQc!=\"\"'>" ,
            " AND rem.code like concat('%',#{codeQc},'%')",
            "</when>",
            " <when test='titleQc!=null and titleQc!=\"\"'>" ,
            " AND rem.title like concat('%',#{titleQc},'%')",
            "</when>",
            " <when test='applyNameQc!=null and applyNameQc!=\"\"'>" ,
            " AND rem.apply_name like concat('%',#{applyNameQc},'%')",
            "</when>",
            " <when test='userId!=null and userId!=\"\" and companyCode!=\"JT\"'>" ,
            "  AND (rem.apply_id=#{userId} or rem.approver_id = #{userId})",
            "</when>",
            " <when test='deptIds!=null and deptIds!=\"\"'>" ,
            " AND rem.dept_id in (${deptIds})",
            "</when>",
            " <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
            " AND rem.apply_time &gt;= #{applyStartTimeQc}",
            " </when>",
            " <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
            " AND rem.apply_time &lt;= STR_TO_DATE(concat(#{applyEndTimeQc}, ' 23:59:59'), '%Y-%m-%d %T')",
            " </when>",
            " <when test='payTimeStateQc!=null and payTimeStateQc!=\"\"'>",
            " AND rem.pay_time &gt;= #{payTimeStateQc}",
            " </when>",
            " <when test='payTimeEndQc!=null and payTimeEndQc!=\"\"'>",
            " AND rem.pay_time &lt;= STR_TO_DATE(concat(#{payTimeEndQc}, ' 23:59:59'), '%Y-%m-%d %T')",
            " </when>",
            " <when test='accountNameQc!=null and accountNameQc!=\"\"'>" ,
            " AND rem.account_name like concat('%',#{accountNameQc},'%')",
            "</when>",
            " <when test='remarkQc!=null and remarkQc!=\"\"'>" ,
            " AND rem.remark like concat('%',#{remarkQc},'%')",
            "</when>",
            " <when test='accountBankNameQc!=null and accountBankNameQc!=\"\"'>" ,
            " AND rem.account_bank_name like concat('%',#{accountBankNameQc},'%')",
            "</when>" ,
            " <when test='outAccountNameQc!=null and outAccountNameQc!=\"\"'>",
            " AND rem.out_account_name like concat('%',#{outAccountNameQc},'%')",
            " </when>",
            " <when test='payAmountQc!=null and payAmountQc!=\"\"'>",
            " AND rem.pay_amount = #{payAmountQc}",
            " </when>",
            " <when test='stateQc!=null and stateQc!=\"\"'>" ,
            "<choose>",
            " <when test='stateQc==-1'>",
            " AND rem.state =-1 ",
            " </when>",
            " <when test='stateQc==0'>",
            " AND rem.state =0 ",
            " </when>",
            " <when test='stateQc==1'>",
            " AND rem.state =1 ",
            " </when>",
            " <when test='stateQc==2'>",
            " AND rem.state in (2,16) ",
            " </when>",
            " <when test='stateQc==4'>",
            " AND rem.state =9 ",
            " </when>",
            " <when test='stateQc==3'>",
            " AND rem.state in (3,4,5,6,7,8,10,12) ",
            " </when>",
            " <otherwise>",
            " and  rem.id=0 ",
            " </otherwise>",
            "</choose>",
            "</when>",
            " ORDER BY ID desc ",
            "</script>"})
    Map reimburseSum(Map map);



}
