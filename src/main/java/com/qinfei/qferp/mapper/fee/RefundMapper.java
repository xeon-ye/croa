package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.fee.Refund;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface RefundMapper extends BaseMapper<Refund, Integer> {


	@Select({ "<script>",
			" SELECT a.id id ,a.code code,a.title title,a.type type,a.cust_id custId,a.cust_name custName,"
					+ " a.cust_company_id custCompanyId,a.cust_company_name custCompanyName,"
					+ " a.account_id accountId,a.account_name accountName,a.account_bank_no accountBankNo,"
					+ " a.account_bank_name accountBankName,a.out_account_id outAccountId,a.out_account_name outAccountName,"
					+ " a.apply_amount applyAmount,"
					+ " DATE_FORMAT(a.apply_time,\"%Y-%m-%d %H:%i\") applyTime, "
					+ " DATE_FORMAT(a.pay_time,\"%Y-%m-%d\") payTime, "
					+ " a.pay_amount payAmount,a.state state,a.creator creator,a.create_time createTime,"
					+ " a.update_user_id updateUserId,a.update_time updateTime,a.task_id taskId,a.item_id itemId,"
					+ " a.apply_id applyId,a.apply_name applyName,a.dept_id deptId,a.dept_name deptName,tii.accept_worker acceptWorker "
					+ " FROM fee_refund a " +
					" LEFT JOIN t_index_items tii on a.item_id = tii.id  "
					+ " where a.state>-2 ",
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
								" and a.company_code=#{user.dept.companyCode} ",
							" </when>",
							" <when test='roleType==\"YW\"'>",
								" <choose>",
									" <when test='roleCode==\"YG\"'>",
										" and a.apply_id = #{user.id}",
									" </when>",
									" <when test='roleCode==\"ZL\"'>",
									" and a.apply_id = #{user.id}",
									" </when>",
									" <when test='roleCode==\"ZZ\"'>",
										" and (a.dept_id=#{user.deptId} or a.apply_id = #{user.id})",
									" </when>",
									" <when test='roleCode==\"BZ\"'>",
									" and ( <foreach collection='user.deptIdSet' item='item' open='  a.dept_id in (' close=')' separator=','>",
									" #{item}",
									" </foreach> or a.apply_id = #{user.id})",
									" </when>",
									" <when test='roleCode==\"ZJ\"'>",
									" and ( <foreach collection='user.deptIdSet' item='item' open='  a.dept_id in (' close=')' separator=','>",
									" #{item}",
									" </foreach> or a.apply_id = #{user.id})",
									" </when>",
									" <otherwise>",
									" and a.id=0",
									" </otherwise>",
								"</choose>",
							" </when>",
							" <otherwise>",
								" and a.id=0",
							" </otherwise>",
						" </choose>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='custCompanyNameQc!=null and custCompanyNameQc!=\"\"'>",
			" AND a.cust_company_name like concat('%',#{custCompanyNameQc},'%')",
			" </when>",
			" <when test='custNameQc!=null and custNameQc!=\"\"'>",
			" AND a.cust_name like concat('%',#{custNameQc},'%')",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_Id in (${deptIds})",
			" </when>",
			" <when test='startTimeQc!=null and startTimeQc!=\"\"'>",
			" AND a.pay_time &gt;= #{startTimeQc}",
			" </when>",
			" <when test='endTimeQc!=null and endTimeQc!=\"\"'>",
			" AND a.pay_time &lt;= STR_TO_DATE(concat(#{endTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='applyTimeStart!=null and applyTimeStart!=\"\"'>",
			" AND a.apply_time &gt;= #{applyTimeStart}",
			" </when>",
			" <when test='applyTimeEnd!=null and applyTimeEnd!=\"\"'>",
			" AND a.apply_time &lt;= STR_TO_DATE(concat(#{applyTimeEnd},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='expertStartTime!=null and expertStartTime!=\"\"'>",
			" AND a.expert_pay_time &gt;= #{expertStartTime}",
			" </when>",
			" <when test='expertEndTime!=null and expertEndTime!=\"\"'>",
			" AND a.expert_pay_time &lt;= STR_TO_DATE(concat(#{expertEndTime},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='typeQc!=null and typeQc!=\"\"'>",
			" AND a.type like concat('%',#{typeQc},'%')",
			" </when>",
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND a.code like concat('%',#{codeQc},'%')",
			" </when>",
			" <when test='accountNameQc!=null and accountNameQc!=\"\"'>",
			" AND a.account_name like concat('%',#{accountNameQc},'%')",
			" </when>",
			" <when test='outAccountNameQc!=null and outAccountNameQc!=\"\"'>",
			" AND a.out_account_name like concat('%',#{outAccountNameQc},'%')",
			" </when>",
			" <when test='payAmountQc!=null and payAmountQc!=\"\"'>",
			" AND a.pay_amount = #{payAmountQc}",
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
					" and a.id=0 ",
					" </otherwise>",
				" </choose>",
			" </when>",
			" order by id desc",
			"</script>" })
	List<Map> listPg(Map map);

	@Select("select * from fee_refund where state>-2 and id = #{id}")
	Refund getById(Integer id);

	/**
	 * 查询没有退款和提成的稿件，已提成和已退款的稿件不支持退款操作
	 * 
	 * @param map
	 * @return
	 */
	@Select({ "<script>",
			"select a.id id,a.media_name mediaName,a.supplier_name supplierName,\n"
					+ "    a.media_user_name mediaUserName,a.brand brand,a.title title,a.link link,a.inner_outer innerOuter,a.channel channel,a.electricity_businesses electricityBusinesses,\n"
					+ "    a.sale_amount saleAmount,a.income_amount incomeAmount,a.taxes taxes,\n"
					+ "    a.price_column priceColumn,a.price_type priceType,a.pay_amount payAmount,\n"
					+ "    a.outgo_amount outgoAmount,a.promise_date promiseDate,a.income_date incomeDate,\n"
					+ "    a.other_pay otherPay,a.refund_amount refundAmount,a.remarks remarks,\n"
					+ "    a.commission commission,b.company_name companyName,b.cust_name custName,\n"
					+ "	   DATE_FORMAT(a.issued_date,\"%Y-%m-%d\") issuedDate,"
					+ "    b.user_name userName,b.no no,a.brand brand,a.num num,a.media_type_name mediaTypeName"
					+ "    from t_biz_article a\n"
					+ "    left join t_biz_order b on a.order_id=b.id\n"
					+ "    where a.state = 1 and a.issue_states = 4 and b.state>-2  and a.commission_states =0 and a.income_states=1 and a.refund_states = 0"
					+ " and  b.company_id=#{custCompanyIdSec} and b.cust_id=#{custIdSec} "
					+ " <when test='mediaUserNameQc!=null and mediaUserNameQc!=\"\"'>",
			" AND a.media_user_name like concat('%',#{mediaUserNameQc},'%')",
			" </when>",
			" <when test='mediaNameQc!=null and mediaNameQc!=\"\"'>",
			" AND a.media_name like concat('%',#{mediaNameQc},'%')",
			" </when>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.issued_date &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.issued_date &lt;= #{endTime}",
			" </when>",
			" <when test='startTimeSec!=null and startTimeSec!=\"\"'>",
			" AND a.issued_date &gt;= #{startTimeSec}",
			" </when>",
			" <when test='endTimeSec!=null and endTimeSec!=\"\"'>",
			" AND a.issued_date &lt;= #{endTimeSec}",
			" </when>",
			" order by a.id desc",
			"</script>" })
	List<Map> listPgForSelectArticle(Map map);

	/**
	 * 查询没有其他支出和提成的稿件，已提成和已申请其他支出的稿件不支持其他支出操作
	 *
	 * @param map
	 * @return
	 */
	@Select({ "<script>",
			"select a.id id,a.media_name mediaName,a.supplier_name supplierName,\n"
					+ "    a.media_user_name mediaUserName,a.brand brand,a.title title,a.link link,a.inner_outer innerOuter,a.channel channel,a.electricity_businesses electricityBusinesses,\n"
					+ "    a.sale_amount saleAmount,a.income_amount incomeAmount,a.taxes taxes,\n"
					+ "    a.price_column priceColumn,a.price_type priceType,a.pay_amount payAmount,\n"
					+ "    a.outgo_amount outgoAmount,a.promise_date promiseDate,a.income_date incomeDate,\n"
					+ "    a.other_pay otherPay,a.refund_amount refundAmount,a.remarks remarks,\n"
					+ "    a.commission commission,b.company_name companyName,b.cust_name custName,\n"
					+ "	   DATE_FORMAT(a.issued_date,\"%Y-%m-%d\") issuedDate,"
					+ "    b.user_name userName,b.no no,a.brand brand,a.num num,a.media_type_name mediaTypeName"
					+ "    from t_biz_article a\n"
					+ "    left join t_biz_order b on a.order_id=b.id\n"
					+ "    where a.state = 1 and a.issue_states = 4 and b.state>-2  and a.commission_states =0 and a.income_states=1 and a.other_pay_states = 0"
					+ " and  b.company_id=#{custCompanyIdSec} and b.cust_id=#{custIdSec} "
					+ " <when test='mediaUserNameQc!=null and mediaUserNameQc!=\"\"'>",
			" AND a.media_user_name like concat('%',#{mediaUserNameQc},'%')",
			" </when>",
			" <when test='mediaNameQc!=null and mediaNameQc!=\"\"'>",
			" AND a.media_name like concat('%',#{mediaNameQc},'%')",
			" </when>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.issued_date &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.issued_date &lt;= #{endTime}",
			" </when>",
			" <when test='startTimeSec!=null and startTimeSec!=\"\"'>",
			" AND a.issued_date &gt;= #{startTimeSec}",
			" </when>",
			" <when test='endTimeSec!=null and endTimeSec!=\"\"'>",
			" AND a.issued_date &lt;= #{endTimeSec}",
			" </when>",
			" order by a.id desc",
			"</script>" })
	List<Map> listPgForSelectArticle2(Map map);
	/**
	 *
	 * @param id
	 *            退款订单id
	 * @return 关联了退款订单的稿件列表
	 */
	@Select({ "<script>",
			" select a.id incomeId,c.id articleId,\n"
					+ "c.supplier_name supplierName,c.media_name mediaName,c.media_user_name mediaUserName,\n"
					+ "c.title title,c.link link, c.income_amount incomeAmount,c.outgo_amount outgoAmount," +
					"DATE_FORMAT(c.issued_date,\"%Y-%m-%d\") issuedDate, "
					+ "c.sale_amount saleAmount,c.refund_amount refundAmount,c.other_pay otherPay,c.pay_amount payAmount,"
					+ "d.company_name companyName,d.cust_name custName,d.user_name userName,d.user_id userId,d.no no," +
					" c.brand brand,c.num num,c.media_type_name mediaTypeName\n"
					+ " FROM fee_refund a,fee_refund_article b,t_biz_article c,t_biz_order d\n"
					+ " where a.id=b.refund_id and b.article_id=c.id and c.order_id=d.id "
					+ " and a.state>-2 and d.state>-2 and a.id=#{id} "
					+ " order by c.id desc",
			"</script>" })
	List<Map> listPgForSelectedArticle(Integer id);

	@Insert("insert into fee_refund_article (refund_id,article_id) values (#{refundId},#{articleId})")
	void insertRefundArticle(@Param("refundId") Integer refundId, @Param("articleId") Integer articleId);

	@Delete("delete from fee_refund_article where refund_id=#{refundId}")
	void delRefundArticle(@Param("refundId") Integer refundId);

	@Select("select sum(c.refund_amount) sumRefundAmount,sum(c.other_pay) sumOtherPay " + " from fee_refund a,fee_refund_article b,t_biz_article c,t_biz_order d " + " where a.id=b.refund_id and b.article_id=c.id and c.order_id=d.id "
			+ "and a.state > -2 and d.state>-2 " + " and a.id=#{id}")
	Map querySumAmountById(@Param("id") Integer id);

	@Select("select c.id " + " from fee_refund a,fee_refund_article b,t_biz_article c" + " where a.id=b.refund_id and b.article_id=c.id " + " and a.state > -2 and a.id=#{refundId}")
	List<Integer> queryArticleIdsByRefundId(@Param("refundId") Integer refundId);

	@Select("select c.id " + " from fee_refund a,fee_refund_article b,t_biz_article c" + " where a.id=b.refund_id and b.article_id=c.id " + " and a.state > -2 and a.id=#{refundId} and c.refund_states = #{refundStates}")
	List<Integer> queryArticleIdsByRefundIdAndRefundState(@Param("refundId") Integer refundId,@Param("refundStates") Integer refundStates);

	@Select("select c.id " + " from fee_refund a,fee_refund_article b,t_biz_article c" + " where a.id=b.refund_id and b.article_id=c.id " + " and a.state > -2 and a.id=#{refundId} and c.other_pay_states = #{otherPayStates}")
	List<Integer> queryArticleIdsByRefundIdAndOtherPayState(@Param("refundId") Integer refundId,@Param("otherPayStates") Integer otherPayStates);

	@Select("select c.* " + " from fee_refund a,fee_refund_article b,t_biz_article c" + " where a.id=b.refund_id and b.article_id=c.id " + " and a.state > -2 and c.state > -2 and a.id=#{refundId}")
	List<Article> queryArticleById(@Param("refundId") Integer refundId);

	@Select("select feeRefund.refund_id from t_biz_article article,fee_refund_article feeRefund,fee_refund refund where article.id=feeRefund.article_Id and feeRefund.refund_id=refund.id and refund.type=1 and article.id=#{articleId}")
	Integer queryRefundId(@Param("articleId") Integer articleId);

	@Select("select feeRefund.refund_id from t_biz_article article,fee_refund_article feeRefund,fee_refund refund where article.id=feeRefund.article_Id and feeRefund.refund_id=refund.id and refund.type=2 and article.id=#{articleId}")
	Integer queryOtherPayId(@Param("articleId") Integer articleId);

	@Update("update fee_refund set out_account_id=null,out_account_name=null,pay_time=null,pay_user_id=null,pay_amount=0,state=-1,update_user_id=#{userId} where id = #{id}")
	void returnRefundInfo(@Param("id") Integer id, @Param("userId") Integer userId);

	@Select({"<script>" +
			"select sum(a.apply_amount)applyAmount,sum(a.pay_amount)payAmount" +
			 " FROM fee_refund a"
			+ " where a.state>-2 ",
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
			" and a.company_code=#{user.dept.companyCode} ",
			" </when>",
			" <when test='roleType==\"YW\"'>",
				" <choose>",
				" <when test='roleCode==\"ZL\"'>",
				" and a.apply_id = #{user.id}",
				" </when>",
				" <when test='roleCode==\"YG\"'>",
				" and a.apply_id = #{user.id}",
				" </when>",
				" <when test='roleCode==\"ZZ\"'>",
				" and (a.dept_id=#{user.deptId} or a.apply_id = #{user.id})",
				" </when>",
				" <when test='roleCode==\"BZ\"'>",
				" and ( <foreach collection='user.deptIdSet' item='item' open='  a.dept_id in (' close=')' separator=','>",
				" #{item}",
				" </foreach> or a.apply_id = #{user.id})",
				" </when>",
				" <when test='roleCode==\"ZJ\"'>",
				" and ( <foreach collection='user.deptIdSet' item='item' open='  a.dept_id in (' close=')' separator=','>",
				" #{item}",
				" </foreach> or a.apply_id = #{user.id})",
				" </when>",
				" <otherwise>",
				" and a.id=0",
				" </otherwise>",
				"</choose>",
			" </when>",
			" <otherwise>",
			" and  a.id=0",
			" </otherwise>",
			" </choose>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='custCompanyNameQc!=null and custCompanyNameQc!=\"\"'>",
			" AND a.cust_company_name like concat('%',#{custCompanyNameQc},'%')",
			" </when>",
			" <when test='custNameQc!=null and custNameQc!=\"\"'>",
			" AND a.cust_name like concat('%',#{custNameQc},'%')",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_Id in (${deptIds})",
			" </when>",
			" <when test='startTimeQc!=null and startTimeQc!=\"\"'>",
			" AND a.pay_time &gt;= #{startTimeQc}",
			" </when>",
			" <when test='endTimeQc!=null and endTimeQc!=\"\"'>",
			" AND a.pay_time &lt;= STR_TO_DATE(concat(#{endTimeQc}, ' 23:59:59'), '%Y/%m/%d %T')",
			" </when>",
			" <when test='applyTimeStart!=null and applyTimeStart!=\"\"'>",
			" AND a.apply_time &gt;= #{applyTimeStart}",
			" </when>",
			" <when test='applyTimeEnd!=null and applyTimeEnd!=\"\"'>",
			" AND a.apply_time &lt;= STR_TO_DATE(concat(#{applyTimeEnd}, ' 23:59:59'), '%Y/%m/%d %T')",
			" </when>",
			" <when test='expertStartTime!=null and expertStartTime!=\"\"'>",
			" AND a.expert_pay_time &gt;= #{expertStartTime}",
			" </when>",
			" <when test='expertEndTime!=null and expertEndTime!=\"\"'>",
			" AND a.expert_pay_time &lt;= STR_TO_DATE(concat(#{expertEndTime}, ' 23:59:59'), '%Y/%m/%d %T')",
			" </when>",
			" <when test='typeQc!=null and typeQc!=\"\"'>",
			" AND a.type like concat('%',#{typeQc},'%')",
			" </when>",
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND a.code like concat('%',#{codeQc},'%')",
			" </when>",
			" <when test='accountNameQc!=null and accountNameQc!=\"\"'>",
			" AND a.account_name like '%${accountNameQc}%'",
			" </when>",
			" <when test='outAccountNameQc!=null and outAccountNameQc!=\"\"'>",
			" AND a.out_account_name like '%${outAccountNameQc}%'",
			" </when>",
			" <when test='payAmountQc!=null and payAmountQc!=\"\"'>",
			" AND a.pay_amount = #{payAmountQc}",
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
			" order by id desc",
			"</script>"})
	Map reimburseSum(Map map);
	@Select("update fee_refund SET out_account_id = #{outAccountId},out_account_name=#{outAccountName}," +
			"pay_amount =#{payAmount},pay_time =#{payTime},pay_user_id=#{payUserId},update_user_id=#{updateUserId},state =#{state} where id=#{id}")
	Refund updateaccount(Refund entity);
}
