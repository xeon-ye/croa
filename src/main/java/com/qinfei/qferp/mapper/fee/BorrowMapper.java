package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.Borrow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface BorrowMapper extends BaseMapper<Borrow, Integer> {

	@Select({ "<script>",
			" SELECT a.id id,a.code code,a.title title,a.type type,"
					+ " a.account_id accountId,a.account_name accountName,a.account_bank_no accountBankNo,"
					+ " a.account_bank_name accountBankName,a.apply_id applyId,a.apply_name applyName,"
					+ " DATE_FORMAT(a.apply_time,\"%Y-%m-%d %H:%i\") applyTime,"
					+ " a.apply_amount applyAmount,DATE_FORMAT(a.expert_pay_time,\"%Y-%m-%d\") expertPayTime,"
					+ " a.out_account_id outAccountId,a.out_account_name outAccountName,a.pay_amount payAmount,"
					+ " DATE_FORMAT(a.pay_time,\"%Y-%m-%d\") payTime,a.pay_user_id payUserId,"
					+ " a.state state,a.remark remark,a.affix_name affix_name,a.affix_link affixLink,"
					+ " a.repay_amount repayAmount,a.repay_flag repayFlag,a.remain_amount remainAmount,"
					+ " DATE_FORMAT(a.repay_time,\"%Y-%m-%d %H:%i\") repayTime,"
					+ " a.creator creator,a.create_time createTime,"
					+ " a.update_user_id updateUserId,a.update_time updateTime,a.task_id taskId,item_id itemId,"
					+ " a.dept_name deptName,a.repaying repaying,tii.accept_worker acceptWorker "
					+ " FROM fee_borrow a " +
					"  LEFT JOIN t_index_items tii on a.item_id = tii.id "
					+ " WHERE a.state>-2",
				" <choose>",
					" <when test='roleCode == \"ZC\"'>",
			        " and a.company_code != '' ",
					" </when>",
					" <when test='roleType == \"JT\"'>",
					" </when>",
					" <when test='roleCode == \"ZJL\" or roleCode==\"FZ\"'>",
						" and a.company_code = #{user.dept.companyCode} ",
					" </when>",
					" <when test='roleType==\"CW\"'>",
						" <choose>",
						" <when test='roleCode==\"ZL\"'>",
						" and a.apply_id = #{user.id}",
						" </when>",
						" <when test='roleCode==\"YG\"'>",
						" and a.apply_id = #{user.id}",
						" </when>",
						" <when test='roleCode==\"KJ\"'>",
							" and a.company_code=#{user.dept.companyCode} ",
						" </when>",
						" <when test='roleCode==\"CN\"'>",
							" and a.company_code=#{user.dept.companyCode} ",
						" </when>",
						" <when test='roleCode==\"ZZ\"'>",
						" and a.company_code=#{user.dept.companyCode} ",
						" </when>",
						" <when test='roleCode==\"BZ\"'>",
							" and a.company_code=#{user.dept.companyCode} ",
						" </when>",
						" <when test='roleCode==\"ZJ\"'>",
						" and a.company_code=#{user.dept.companyCode} ",
						" </when>",
						"<otherwise>",
						" and a.apply_id = #{user.id}",
						"</otherwise>",
						"</choose>",
					" </when>",
			"<when test = 'roleType ==\"XM\"'>",
			"<choose>",
			" <when test='roleCode==\"ZJ\"'>",
			" and a.apply_id = #{user.id} ",
			" </when>",
			" <otherwise>",
			" and a.apply_id = #{user.id}",
			" </otherwise>",
			"</choose>",
			"</when>",
					" <otherwise>",
						" <choose>",
							" <when test='roleCode==\"YG\"'>",
							" and a.apply_id = #{user.id}",
							" </when>",
							" <when test='roleCode==\"ZZ\"'>",
							" and a.dept_id=#{user.deptId}",
							" </when>",
							" <when test='roleCode==\"BZ\"'>",
							" <foreach collection='user.deptIdSet' item='item' open='and a.dept_id in (' close=')' separator=','>",
							" #{item}",
							" </foreach>",
							" </when>",
							" <when test='roleCode==\"ZJ\"'>",
							" <foreach collection='user.deptIdSet' item='item' open='and a.dept_id in (' close=')' separator=','>",
							" #{item}",
							" </foreach>",
							" </when>",
							"<otherwise>",
							" and a.apply_id = #{user.id}",
							"</otherwise>",
						"</choose>",
					" </otherwise>",
				" </choose>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='typeQc!=null and typeQc!=\"\"'>",
			" AND a.type = #{typeQc}",
			" </when>",
			" <when test='outAccountNameQc!=null and outAccountNameQc!=\"\"'>",
			" AND a.out_account_name like concat('%',#{outAccountNameQc},'%')",
			" </when>",
			" <when test='accountNameQc!=null and accountNameQc!=\"\"'>",
			" AND a.account_name like concat('%',#{accountNameQc},'%')",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='payAmountQc!=null and payAmountQc!=\"\"'>",
			" AND a.pay_amount = #{payAmountQc}",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_id in (${deptIds})",
			" </when>",
			" <when test='remainAmountQc!=null and remainAmountQc!=\"\"'>",
			" AND a.remain_amount = #{remainAmountQc}",
			" </when>",
			" <when test='repayAmountQc!=null and repayAmountQc!=\"\"'>",
			" AND a.repay_amount = #{repayAmountQc}",
			" </when>",
			" <when test='repayFlagQc!=null and repayFlagQc!=\"\"'>",
			" AND a.repay_flag = #{repayFlagQc}",
			" </when>",
			" <when test='startTimeQc!=null and startTimeQc!=\"\"'>",
			" AND a.pay_time &gt;= #{startTimeQc}",
			" </when>",
			" <when test='endTimeQc!=null and endTimeQc!=\"\"'>",
			" AND a.pay_time &lt;= STR_TO_DATE(concat(#{endTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
			" AND a.apply_time &gt;= #{applyStartTimeQc}",
			" </when>",
			" <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
			" AND a.apply_time &lt;= STR_TO_DATE(concat(#{applyEndTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='expertStartTime!=null and expertStartTime!=\"\"'>",
			" AND a.expert_pay_time &gt;= #{expertStartTime}",
			" </when>",
			" <when test='expertEndTime!=null and expertEndTime!=\"\"'>",
			" AND a.expert_pay_time &lt;= STR_TO_DATE(concat(#{expertEndTime},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='stateQc!=null and stateQc!=\"\"'>",
			" <choose>",
			" <when test='stateQc==-1'>",
			" AND a.state =-1 ",
			" </when>",
			" <when test='stateQc==0'>",
			" AND a.state =0 ",
			" </when>",
			" <when test='stateQc==1'>",
			" AND a.state =1 ",
			" </when>",
			" <when test='stateQc==2'>",
			" AND a.state in (2,16)  ",
			" </when>",
			" <when test='stateQc==4'>",
			" AND a.state =9 ",
			" </when>",
			" <when test='stateQc==3'>",
			" AND a.state in (3,4,5,6,7,8,10,12)",
			" </when>",
			" <otherwise>",
			" and  a.id=0 ",
			" </otherwise>",
			" </choose>",
			" </when>",
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND a.code like concat('%',#{codeQc},'%')",
			" </when>",
			" order by a.id desc",
			"</script>" })
	List<Map> listPg(Map map);

	@Select("select * from fee_borrow where state>-2 and id = #{id}")
	Borrow getById(Integer id);

	@Select("select c.* " + " from fee_outgo a,fee_outgo_borrow b,fee_borrow c" + " where a.id=b.outgo_id and b.borrow_id=c.id " + " and a.state>-2 and c.state>-2 and a.id=#{outgoId}")
	List<Borrow> queryByOutgoId(@Param("outgoId") Integer outgoId);

	/**
	 * 查询出已借款并且未还款的借款
	 * 用于请款，报销时冲抵
	 * @param map
	 * @return
	 */
	@Select({ "<script>",
			" SELECT a.id id,a.code code,a.title title,a.type type,"
					+ " a.account_id accountId,a.account_name accountName,a.account_bank_no accountBankNo,"
					+ " a.account_bank_name accountBankName,a.apply_id applyId,a.apply_name applyName,"
					+ " DATE_FORMAT(a.apply_time,\"%Y-%m-%d %H:%i\") applyTime,"
					+ " a.apply_amount applyAmount,DATE_FORMAT(a.expert_pay_time,\"%Y-%m-%d\") expertPayTime,"
					+ " a.out_account_id outAccountId,a.out_account_name outAccountName,a.pay_amount payAmount,"
					+ " DATE_FORMAT(a.pay_time,\"%Y-%m-%d\") payTime,a.pay_user_id payUserId,"
					+ " a.state state,a.remark remark,a.affix_name affix_name,a.affix_link affixLink,"
					+ " a.repay_amount repayAmount,a.repay_flag repayFlag,a.remain_amount remainAmount,"
					+ " DATE_FORMAT(a.repay_time,\"%Y-%m-%d %H:%i\") repayTime,"
					+ " a.creator creator,a.create_time createTime,"
					+ " a.update_user_id updateUserId,a.update_time updateTime,"
					+ " a.dept_name deptName"
					+ " FROM fee_borrow a"
					+ " WHERE a.state = 1 and a.type=0 and a.repay_flag!=1 and a.company_code=#{companyCode}"
					+ " <when test='title!=null and title!=\"\"'>",
			" AND a.title like concat('%',#{title},'%')",
			" </when>",
			" <when test='deptName!=null and deptName!=\"\"'>",
			" AND a.dept_name like concat('%',#{deptName},'%')",
			" </when>",
			" <when test='applyName!=null and applyName!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyName},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.pay_time &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.pay_time &lt;= #{endTime}",
			" </when>",
			" <when test='payAmount!=null and payAmount!=\"\"'>",
			" AND a.pay_amount = #{payAmount}",
			" </when>",
			" <when test='remId!=null'>",
			" AND a.creator=#{user.id} AND a.remain_amount>0 ",
			" </when>",
			" order by id desc",
			"</script>" })
	List<Map> listPgForOutgo(Map map);

	/**
	 * 查询出已借款并且未还款的借款
	 * 用于请款，报销时冲抵
	 * @param map
	 * @return
	 */
	@Select({ "<script>",
			" SELECT a.id id,a.code code,a.title title,a.type type,"
					+ " a.account_id accountId,a.account_name accountName,a.account_bank_no accountBankNo,"
					+ " a.account_bank_name accountBankName,a.apply_id applyId,a.apply_name applyName,"
					+ " DATE_FORMAT(a.apply_time,\"%Y-%m-%d %H:%i\") applyTime,"
					+ " a.apply_amount applyAmount,DATE_FORMAT(a.expert_pay_time,\"%Y-%m-%d\") expertPayTime,"
					+ " a.out_account_id outAccountId,a.out_account_name outAccountName,a.pay_amount payAmount,"
					+ " DATE_FORMAT(a.pay_time,\"%Y-%m-%d\") payTime,a.pay_user_id payUserId,"
					+ " a.state state,a.remark remark,a.affix_name affix_name,a.affix_link affixLink,"
					+ " a.repay_amount repayAmount,a.repay_flag repayFlag,a.remain_amount remainAmount,"
					+ " DATE_FORMAT(a.repay_time,\"%Y-%m-%d %H:%i\") repayTime,"
					+ " a.creator creator,a.create_time createTime,"
					+ " a.update_user_id updateUserId,a.update_time updateTime,"
					+ " a.dept_name deptName"
					+ " FROM fee_borrow a"
					+ " WHERE a.state = 1 and a.type=1 and a.repay_flag!=1 and a.company_code=#{user.dept.companyCode} and a.apply_id=#{user.id}"
					+ " <when test='title!=null and title!=\"\"'>",
			" AND a.title like concat('%',#{title},'%')",
			" </when>",
			" <when test='deptName!=null and deptName!=\"\"'>",
			" AND a.dept_name like concat('%',#{deptName},'%')",
			" </when>",
			" <when test='applyName!=null and applyName!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyName},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.pay_time &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.pay_time &lt;= #{endTime}",
			" </when>",
			" <when test='payAmount!=null and payAmount!=\"\"'>",
			" AND a.pay_amount = #{payAmount}",
			" </when>",
			" <when test='remId!=null'>",
			" AND a.creator=#{user.id} AND a.remain_amount>0 ",
			" </when>",
			" order by id desc",
			"</script>" })
	List<Map> listPgForReimbursement(Map map);

	@Update("update fee_borrow set out_account_id=null,out_account_name=null,pay_time=null,pay_user_id=null,pay_amount=0,remain_amount=0,state=-1,update_user_id=#{userId} where id = #{id}")
	void returnBorrowInfo(@Param("id") Integer id, @Param("userId") Integer userId);

	@Select({"<script>" +
			"select sum(apply_amount)applyAmount,sum(pay_amount)payAmount,sum(repay_amount)repayAmount,sum(remain_amount)remainAmount" +
			 " FROM fee_borrow a" +
			 " WHERE a.state>-2",
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
				" <choose>",
				" <when test='roleCode==\"ZL\"'>",
				" and a.apply_id = #{user.id}",
				" </when>",
				" <when test='roleCode==\"YG\"'>",
				" and a.apply_id = #{user.id}",
				" </when>",
				" <when test='roleCode==\"KJ\"'>",
				" and a.company_code=#{user.dept.companyCode} ",
				" </when>",
				" <when test='roleCode==\"CN\"'>",
				" and a.company_code=#{user.dept.companyCode} ",
				" </when>",
				" <when test='roleCode==\"ZZ\"'>",
				" and a.company_code=#{user.dept.companyCode} ",
				" </when>",
				" <when test='roleCode==\"BZ\"'>",
				" and a.company_code=#{user.dept.companyCode} ",
				" </when>",
				" <when test='roleCode==\"ZJ\"'>",
				" and a.company_code=#{user.dept.companyCode} ",
				" </when>",
				"<otherwise>",
				" and a.apply_id = #{user.id}",
				"</otherwise>",
				"</choose>",
			" </when>",
			"<when test = 'roleType ==\"XM\"'>",
			"<choose>",
			" <when test='roleCode==\"ZJ\"'>",
			" and a.apply_id = #{user.id} ",
			" </when>",
			" <otherwise>",
			" and a.apply_id = #{user.id}",
			" </otherwise>",
			"</choose>",
			"</when>",
			" <otherwise>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" <foreach collection='user.deptIdSet' item='item' open='and a.dept_id in (' close=')' separator=','>",
			" #{item}",
			" </foreach>",
			" </when>",
			" <when test='roleCode==\"ZJ\"'>",
			" <foreach collection='user.deptIdSet' item='item' open='and a.dept_id in (' close=')' separator=','>",
			" #{item}",
			" </foreach>",
			" </when>",
			"<otherwise>",
			" and a.apply_id = #{user.id}",
			"</otherwise>",
			"</choose>",
			" </otherwise>",
			" </choose>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='typeQc!=null and typeQc!=\"\"'>",
			" AND a.type = #{typeQc}",
			" </when>",
			" <when test='outAccountNameQc!=null and outAccountNameQc!=\"\"'>",
			" AND a.out_account_name like concat('%',#{outAccountNameQc},'%')",
			" </when>",
			" <when test='accountNameQc!=null and accountNameQc!=\"\"'>",
			" AND a.account_name like concat('%',#{accountNameQc},'%')",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='payAmountQc!=null and payAmountQc!=\"\"'>",
			" AND a.pay_amount = #{payAmountQc}",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_id in (${deptIds})",
			" </when>",
			" <when test='remainAmountQc!=null and remainAmountQc!=\"\"'>",
			" AND a.remain_amount = #{remainAmountQc}",
			" </when>",
			" <when test='repayAmountQc!=null and repayAmountQc!=\"\"'>",
			" AND a.repay_amount = #{repayAmountQc}",
			" </when>",
			" <when test='repayFlagQc!=null and repayFlagQc!=\"\"'>",
			" AND a.repay_flag = #{repayFlagQc}",
			" </when>",
			" <when test='startTimeQc!=null and startTimeQc!=\"\"'>",
			" AND a.pay_time &gt;= #{startTimeQc}",
			" </when>",
			" <when test='endTimeQc!=null and endTimeQc!=\"\"'>",
			" AND a.pay_time &lt;= STR_TO_DATE(concat(#{endTimeQc}, ' 23:59:59'), '%Y/%m/%d %T')",
			" </when>",
			" <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
			" AND a.apply_time &gt;= #{applyStartTimeQc}",
			" </when>",
			" <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
			" AND a.apply_time &lt;= STR_TO_DATE(concat(#{applyEndTimeQc}, ' 23:59:59'), '%Y/%m/%d %T')",
			" </when>",
			" <when test='expertStartTime!=null and expertStartTime!=\"\"'>",
			" AND a.expert_pay_time &gt;= #{expertStartTime}",
			" </when>",
			" <when test='expertEndTime!=null and expertEndTime!=\"\"'>",
			" AND a.expert_pay_time &lt;= STR_TO_DATE(concat(#{expertEndTime}, ' 23:59:59'), '%Y/%m/%d %T')",
			" </when>",
			" <when test='stateQc!=null and stateQc!=\"\"'>",
			" <choose>",
			" <when test='stateQc==-1'>",
			" AND a.state =-1 ",
			" </when>",
			" <when test='stateQc==0'>",
			" AND a.state =0 ",
			" </when>",
			" <when test='stateQc==1'>",
			" AND a.state =1 ",
			" </when>",
			" <when test='stateQc==2'>",
			" AND a.state in (2,16)  ",
			" </when>",
			" <when test='stateQc==4'>",
			" AND a.state =9 ",
			" </when>",
			" <when test='stateQc==3'>",
			" AND a.state in (3,4,5,6,7,8,10,12)",
			" </when>",
			" <otherwise>",
			" and  a.id=0 ",
			" </otherwise>",
			" </choose>",
			" </when>",
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND a.code like concat('%',#{codeQc},'%')",
			" </when>",
			" order by a.id desc",
			"</script>"
	})
	Map reimburseSum(Map map);

	@Select("update fee_borrow SET out_account_id = #{outAccountId},out_account_name=#{outAccountName}," +
			"pay_amount =#{payAmount},remain_amount=#{remainAmount},pay_user_id=#{payUserId},update_user_id=#{updateUserId}," +
			"pay_time =#{payTime},state=#{state} where id=#{id}")
	Borrow updateaccount(Borrow entity);
}
