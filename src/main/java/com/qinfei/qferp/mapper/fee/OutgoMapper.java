package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.entity.fee.OutgoArticle;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface OutgoMapper extends BaseMapper<Outgo, Integer> {


	@Select({ "<script>",
			" SELECT distinct a.id id,a.process_type processType,a.code code,a.title title,a.supplier_id supplierId," +
					" a.supplier_name supplierName,a.supplier_contactor supplierContactor,"
					+ " a.account_id accountId,a.account_name accountName,a.account_bank_no accountBankNo,"
					+ " a.account_bank_name accountBankName,a.out_account_id outAccountId,a.out_account_name outAccountName,"
					+ " a.apply_id applyId,a.apply_name applyName,a.dept_id deptId,a.dept_name deptName,"
					+ " a.apply_amount applyAmount,a.remark remark,a.company_code companyCode, su.company_code companyCodet,"
					+ " DATE_FORMAT(a.expert_pay_time,\"%Y-%m-%d\") expertPayTime, "
					+ " DATE_FORMAT(a.pay_time,\"%Y-%m-%d\") payTime, "
					+ " DATE_FORMAT(a.apply_time,\"%Y-%m-%d %H:%i\") applyTime, "
					+ " a.pay_amount payAmount,"
					+ " a.state state,a.creator creator,a.create_time createTime,"
					+ " a.update_user_id updateUserId,a.update_time updateTime,a.fund_flag fundFlag,a.fund_amount fundAmount,"
					+ " a.task_id taskId,a.item_id itemId,a.media_type_id mediaTypeId,tmp.parent_type parentType," +
					" a.invoice_flag invoiceFlag ,a.invoice_type invoiceType,a.actual_cost actualCost,a.outgo_tax outgoTax," +
					" a.tax_amount taxAmount,a.outgo_erase_amount outgoEraseAmount,a.cost_erase_amount costEraseAmount,a.invoice_tax invoiceTax," +
					" a.invoice_rise invoiceRise, a.invoice_code invoiceCode ,a.backfill_time backfillTime,tii.accept_worker acceptWorker, a.time_scale as timeScale,t.phone,t.creator as supplierCreator"
					+ " FROM fee_outgo a " +
					"left join t_media_supplier t on a.supplier_id =t.id" +
					" left join t_media_plate tmp on tmp.id = a.media_type_id " +
					"  LEFT JOIN t_index_items tii on a.item_id = tii.id " +
					"   left join sys_user su on su.id = a.apply_id "
					+ " where a.state>-2",
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
			" <when test='roleType==\"MJ\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" <foreach collection='user.deptIdSet' item='item' open=' and a.dept_id in (' close=')' separator=','>",
			" #{item}",
			" </foreach>",
			" </when>",
			"</choose>",
			" </when>",
			" <when test='roleType==\"YW\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" and a.company_code=#{user.dept.companyCode} ",
			" </when>",
			"</choose>",
			" </when>",

			" <otherwise>",
			" and a.id=0",
			" </otherwise>",
			" </choose>",
			" <when test='supplierNameQc!=null and supplierNameQc!=\"\"'>",
			" AND a.supplier_name like concat('%',#{supplierNameQc},'%')",
			" </when>",
			" <when test='mTypeName!=null and mTypeName!=\"\"'>",
			" and t.media_type_id = #{mTypeName}",
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
			" <when test='companyCodeQc!=null and companyCodeQc!=\"\"'>",
			" AND a.company_code = #{companyCodeQc}",
			" </when>",
			" <when test='supplierContactor!=null and supplierContactor!=\"\"'>",
			" AND a.supplier_contactor like concat('%',#{supplierContactor},'%')",
			" </when>",
			" <when test='titleQc1!=null and titleQc1!=\"\"'>",
			" AND a.title like concat('%',#{titleQc1},'%')",
			" </when>",
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND a.code like concat('%',#{codeQc},'%')",
			" </when>",
			" <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
			" AND a.apply_time &gt;= #{applyStartTimeQc}",
			" </when>",
			" <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
			" AND a.apply_time &lt;= STR_TO_DATE(concat(#{applyEndTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='backfillTimeState!=null and backfillTimeState!=\"\"'>",
			" AND a.backfill_time &gt;= #{backfillTimeState}",
			" </when>",
			" <when test='backfillTimeEnd!=null and backfillTimeEnd!=\"\"'>",
			" AND a.backfill_time &lt;= #{backfillTimeEnd}",
			" </when>",
			" <when test='payStartTimeQc!=null and payStartTimeQc!=\"\"'>",
			" AND a.pay_time &gt;= #{payStartTimeQc}",
			" </when>",
			" <when test='payEndTimeQc!=null and payEndTimeQc!=\"\"'>",
			" AND a.pay_time &lt;= STR_TO_DATE(concat(#{payEndTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='expertStartTime!=null and expertStartTime!=\"\"'>",
			" AND a.expert_pay_time &gt;= #{expertStartTime}",
			" </when>",
			" <when test='expertEndTime!=null and expertEndTime!=\"\"'>",
			" AND a.expert_pay_time &lt;= STR_TO_DATE(concat(#{expertEndTime},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_id in (${deptIds})",
			" </when>",
			" <when test='taxAmountMin!=null and taxAmountMin!=\"\"'>",
			" AND a.tax_amount &gt;=#{taxAmountMin}",
			" </when>",
			" <when test='taxAmountMax!=null and taxAmountMax!=\"\"'>",
			" AND a.tax_amount &lt;=#{taxAmountMax}",
			" </when>",
			" <when test='invoiceCodeQc!=null and invoiceCodeQc!=\"\"'>",
			" AND a.invoice_code like concat('%',#{invoiceCodeQc},'%')",
			" </when>",
			" <when test='stateQc!=null and stateQc!=\"\"'>",
			" <choose>",
			" <when test='stateQc==-1'>",
			" AND a.state =-1 ",
			" </when>" +
					"<when test = 'stateQc == 10'>" +
					"and a.state =26 and TIMESTAMPDIFF(DAY,a.pay_time,NOW())>10 " +
					"</when>",
			" <when test='stateQc==0'>",
			" AND a.state =0 ",
			" </when>",
			" <when test='stateQc==1'>",
			" AND a.state =1 ",
			" </when>",
			" <when test='stateQc==23'>",
			" AND a.state =23 ",
			" </when>",
			" <when test='stateQc==26'>",
			" AND a.state =26 ",
			" </when>",
			" <when test='stateQc==2'>",
			" AND a.state in (2,16)  ",
			" </when>",
			"<when test ='stateQc==4'>",
			"and a.state in (9,12)",
			"</when>",
			" <when test='stateQc==3'>",
			" AND a.state in (3,4,5,6,7,8,10) ",
			" </when>",
			" <otherwise>",
			" and a.id=0 ",
			" </otherwise>",
			" </choose>",
			" </when>",
			" <when test='sidx != null and sidx != \"\"'>",
			" ORDER BY FIELD(a.state,0,-1,3,4,5,6,7,8,10,2,16,9,12,1),${sidx} ${sord}",
			" </when>",
			" <when test='sidx == null or sidx ==  \"\"'>",
			" ORDER BY FIELD(a.state,0,-1,3,4,5,6,7,8,10,2,16,9,12,1),a.id desc",
			" </when>",
			"</script>" })
	List<Map> listPg(Map map);

	@Select("select a.*,tmt.parent_type as parentType,su.company_code as creatorcompanyCode,tms.phone as supplierPhone, tms.creator as supplierCreator,tms.supplier_nature as supplierNature, tms.create_time as supplierCreateTime from fee_outgo a LEFT JOIN t_media_plate tmt on a.media_type_id= tmt.id LEFT JOIN sys_user su ON a.apply_id = su.id LEFT JOIN t_media_supplier tms ON a.supplier_id = tms.id  where a.state > -2 and a.id = #{id}")
	Outgo getById(Integer id);

	@Insert("insert into fee_outgo_article (outgo_id,article_id) values (#{outgoId},#{articleId})")
	void insertOutgoArticle(@Param("outgoId") Integer outgoId, @Param("articleId") Integer articleId);

	@Delete("delete from fee_outgo_article where outgo_id=#{outgoId}")
	void delOutgoArticle(@Param("outgoId") Integer outgoId);

	@Select({ "<script>",
			"SELECT \n" +
					"  c.id articleId,\n" +
					"  a.id outgoId,\n" +
					"  e.company_code_name companyCodeName," +
					"  c.supplier_contactor supplierContactor,\n" +
					"  c.num num,\n" +
					"  c.price_type priceType,\n" +
					"  c.supplier_name supplierName,\n" +
					"  c.media_name mediaName,\n" +
					"  c.media_user_name mediaUserName,\n" +
					"  c.title,\n" +
					"  c.link,\n" +
					"  c.other_pay otherPay,\n" +
					"  c.income_amount incomeAmount,\n" +
					"  c.outgo_amount outgoAmount,\n" +
					"  c.sale_amount saleAmount,\n" +
					"  c.pay_amount payAmount,\n" +
					"  c.channel channel," +
					"  c.inner_outer innerOuter," +
					"  c.electricity_businesses electricityBusinesses, " +
					"  c.media_type_name mediaTypeName,\n" +
					"  (select \n" +
					"    GROUP_CONCAT(fi.trade_time) \n" +
					"  from\n" +
					"    fee_income fi,\n" +
					"    fee_income_article fia \n" +
					"  where fi.id = fia.income_id \n" +
					"    and c.id = fia.article_id) tradeTime,\n" +
					"  c.issued_date issuedDate,\n" +
					"  c.promise_date promiseDate,\n" +
					"  d.user_name userName,\n" +
					"  d.company_name companyName,\n" +
					"  c.income_date incomeDate,\n" +
					"  d.cust_name custName,\n" +
					"  c.unit_price unitPrice,\n" +
					"  c.state state, \n" +
					"  c.other_expenses otherExpenses \n" +
					"FROM\n" +
					"  fee_outgo a,\n" +
					"  fee_outgo_article b,\n" +
					"  t_biz_order d,\n" +
					"  t_biz_article c, \n" +
					"  sys_dept e \n"+
					"WHERE a.id = b.outgo_id \n" +
					"  AND b.article_id = c.id \n" +
					"  AND c.order_id = d.id \n" +
					"  AND e.id=d.depat_id \n"+
					"  AND a.state > - 2 \n" +
					"  AND d.state > - 2 \n" +
					"  AND a.id = #{id} \n" +
			"</script>" })
	List<Map> listPgForSelectedArticle(Integer id);

	/**
	 * 查询出待请款稿件
	 * 1、已完善客户未请款
	 * 2、未完善客户未请款，新媒体请款金额<500,网络请款金额<5000
	 * @param map
	 * @return
	 */
	@Select({ "<script>",
			"(select a.id id,a.media_name mediaName,a.supplier_name supplierName,a.supplier_contactor supplierContactor,\n"
					+ "    a.media_user_name mediaUserName,a.brand brand,a.title title,a.link link,a.inner_outer innerOuter,a.channel channel,a.electricity_businesses electricityBusinesses,\n"
					+ "    a.sale_amount saleAmount,a.income_amount incomeAmount,a.taxes taxes,a.num num,\n"
					+ "    a.price_column priceColumn,a.price_type priceType,a.pay_amount payAmount,\n"
					+ "    a.outgo_amount outgoAmount,a.income_date incomeDate,\n"
					+ "    a.other_pay otherPay,a.refund_amount refundAmount,a.remarks remarks,a.media_type_name mediaTypeName,\n"
					+ "    a.commission commission,b.company_name companyName,b.cust_name custName,"
					+ "    DATE_FORMAT(a.issued_date,\"%Y-%m-%d\") issuedDate,"
					+ "    DATE_FORMAT(a.promise_date,\"%Y-%m-%d\") promiseDate,"
					+ "    b.user_name userName,a.unit_price unitPrice,b.cust_id custId  \n"
					+ "    from t_biz_article a\n"
					+ "    left join t_biz_order b on a.order_id=b.id\n"
					+ "    left join sys_user su on b.user_id=su.id\n"
					+ "    where a.state = 1 and b.state>-2 and a.outgo_states=0 and a.issue_states=4 "
					+ " and b.cust_id is not null"
					+ " and  a.supplier_id=${supplierIdSec}  and  su.company_code=#{companyCode}"
					+ " <when test='mediaUserNameQc!=null and mediaUserNameQc!=\"\"'>",
			" AND a.media_user_name like concat('%',#{mediaUserNameQc},'%')",
			" </when>",
			" <when test='media_type_id != null'>",
			" AND a.media_type_id = #{media_type_id}",
			" </when>",
			" <when test='mediaNameIdQc!=null and mediaNameIdQc!=\"\"'>",
			" AND a.media_user_id like concat('%',#{mediaNameIdQc},'%')",
			" </when>",
			" <when test='mediaNameQc!=null and mediaNameQc!=\"\"'>",
			" AND a.media_name like concat('%',#{mediaNameQc},'%')",
			" </when>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='userNameQc!=null and userNameQc!=\"\"'>",
			" AND b.user_name like concat('%',#{userNameQc},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.issued_date &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.issued_date &lt;= #{endTime}",
			" </when>",
			" order by a.id desc) "
					+ " union all (select a.id id,a.media_name mediaName,a.supplier_name supplierName,a.supplier_contactor supplierContactor,\n"
					+ "    a.media_user_name mediaUserName,a.brand brand,a.title title,a.link link,a.inner_outer innerOuter,a.channel channel,a.electricity_businesses electricityBusinesses,\n"
					+ "    a.sale_amount saleAmount,a.income_amount incomeAmount,a.taxes taxes,a.num num,\n"
					+ "    a.price_column priceColumn,a.price_type priceType,a.pay_amount payAmount,\n"
					+ "    a.outgo_amount outgoAmount,a.income_date incomeDate,\n"
					+ "    a.other_pay otherPay,a.refund_amount refundAmount,a.remarks remarks,a.media_type_name mediaTypeName,\n"
					+ "    a.commission commission,b.company_name companyName,b.cust_name custName,"
					+ "    DATE_FORMAT(a.issued_date,\"%Y-%m-%d\") issuedDate,"
					+ "    DATE_FORMAT(a.promise_date,\"%Y-%m-%d\") promiseDate,"
					+ "    b.user_name userName,a.unit_price unitPrice,b.cust_id custId \n"
					+ "    from t_biz_article a\n"
					+ "    left join t_biz_order b on a.order_id=b.id\n"
					+ "    left join sys_user su on b.user_id=su.id\n"
					+ "    where a.state = 1 and b.state>-2 and a.outgo_states=0 and a.issue_states=4 "
					+ " and (case when #{user.dept.type}='XMT' then a.outgo_amount &lt;= 2000 else a.outgo_amount &lt;= 5000 end )"
					+ " and  a.supplier_id=${supplierIdSec} and b.cust_id is null  and  su.company_code=#{companyCode}"
					+ " <when test='mediaUserNameQc!=null and mediaUserNameQc!=\"\"'>",
			" AND a.media_user_name like concat('%',#{mediaUserNameQc},'%')",
			" </when>",
			" <when test='media_type_id != null'>",
			" AND a.media_type_id = #{media_type_id}",
			" </when>",
			" <when test='mediaNameIdQc!=null and mediaNameIdQc!=\"\"'>",
			" AND a.media_user_id like concat('%',#{mediaNameIdQc},'%')",
			" </when>",
			" <when test='mediaNameQc!=null and mediaNameQc!=\"\"'>",
			" AND a.media_name like concat('%',#{mediaNameQc},'%')",
			" </when>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='userNameQc!=null and userNameQc!=\"\"'>",
			" AND b.user_name like concat('%',#{userNameQc},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.issued_date &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.issued_date &lt;= #{endTime}",
			" </when>",
			" order by a.id desc)",
			"</script>" })
	List<Map> listPgForSelectArticle(Map map);

	@Select({ "<script>",
			"select sum(c.outgoAmount) outgoAmountSum,sum(c.saleAmount) saleAmountSum  from ((select a.id id,a.media_name mediaName,a.supplier_name supplierName,a.supplier_contactor supplierContactor,\n"
					+ "    a.media_user_name mediaUserName,a.brand brand,a.title title,a.link link,a.inner_outer innerOuter,a.channel channel,a.electricity_businesses electricityBusinesses,\n"
					+ "    a.sale_amount saleAmount,a.income_amount incomeAmount,a.taxes taxes,a.num num,\n"
					+ "    a.price_column priceColumn,a.price_type priceType,a.pay_amount payAmount,\n"
					+ "    a.outgo_amount outgoAmount,a.income_date incomeDate,\n"
					+ "    a.other_pay otherPay,a.refund_amount refundAmount,a.remarks remarks,a.media_type_name mediaTypeName,\n"
					+ "    a.commission commission,b.company_name companyName,b.cust_name custName,"
					+ "    DATE_FORMAT(a.issued_date,\"%Y-%m-%d\") issuedDate,"
					+ "    DATE_FORMAT(a.promise_date,\"%Y-%m-%d\") promiseDate,"
					+ "    b.user_name userName,a.unit_price unitPrice,b.cust_id custId  \n"
					+ "    from t_biz_article a\n"
					+ "    left join t_biz_order b on a.order_id=b.id\n"
					+ "    left join sys_user su on b.user_id=su.id\n"
					+ "    where a.state = 1 and b.state>-2 and a.outgo_states=0 and a.issue_states=4 "
					+ " and b.cust_id is not null"
					+ " and  a.supplier_id=${supplierIdSec}  and  su.company_code=#{companyCode}"
					+ " <when test='mediaUserNameQc!=null and mediaUserNameQc!=\"\"'>",
			" AND a.media_user_name like concat('%',#{mediaUserNameQc},'%')",
			" </when>",
			" <when test='media_type_id != null'>",
			" AND a.media_type_id = #{media_type_id}",
			" </when>",
			" <when test='mediaNameIdQc!=null and mediaNameIdQc!=\"\"'>",
			" AND a.media_user_id like concat('%',#{mediaNameIdQc},'%')",
			" </when>",
			" <when test='mediaNameQc!=null and mediaNameQc!=\"\"'>",
			" AND a.media_name like concat('%',#{mediaNameQc},'%')",
			" </when>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='userNameQc!=null and userNameQc!=\"\"'>",
			" AND b.user_name like concat('%',#{userNameQc},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.issued_date &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.issued_date &lt;= #{endTime}",
			" </when>",
			" ) "
					+ " union all (select a.id id,a.media_name mediaName,a.supplier_name supplierName,a.supplier_contactor supplierContactor,\n"
					+ "    a.media_user_name mediaUserName,a.brand brand,a.title title,a.link link,a.inner_outer innerOuter,a.channel channel,a.electricity_businesses electricityBusinesses,\n"
					+ "    a.sale_amount saleAmount,a.income_amount incomeAmount,a.taxes taxes,a.num num,\n"
					+ "    a.price_column priceColumn,a.price_type priceType,a.pay_amount payAmount,\n"
					+ "    a.outgo_amount outgoAmount,a.income_date incomeDate,\n"
					+ "    a.other_pay otherPay,a.refund_amount refundAmount,a.remarks remarks,a.media_type_name mediaTypeName,\n"
					+ "    a.commission commission,b.company_name companyName,b.cust_name custName,"
					+ "    DATE_FORMAT(a.issued_date,\"%Y-%m-%d\") issuedDate,"
					+ "    DATE_FORMAT(a.promise_date,\"%Y-%m-%d\") promiseDate,"
					+ "    b.user_name userName,a.unit_price unitPrice,b.cust_id custId \n"
					+ "    from t_biz_article a\n"
					+ "    left join t_biz_order b on a.order_id=b.id\n"
					+ "    left join sys_user su on b.user_id=su.id\n"
					+ "    where a.state = 1 and b.state>-2 and a.outgo_states=0 and a.issue_states=4 "
					+ " and (case when #{user.dept.type}='XMT' then a.outgo_amount &lt;= 2000 else a.outgo_amount &lt;= 5000 end )"
					+ " and  a.supplier_id=${supplierIdSec} and b.cust_id is null  and  su.company_code=#{companyCode}"
					+ " <when test='mediaUserNameQc!=null and mediaUserNameQc!=\"\"'>",
			" AND a.media_user_name like concat('%',#{mediaUserNameQc},'%')",
			" </when>",
			" <when test='media_type_id != null'>",
			" AND a.media_type_id = #{media_type_id}",
			" </when>",
			" <when test='mediaNameIdQc!=null and mediaNameIdQc!=\"\"'>",
			" AND a.media_user_id like concat('%',#{mediaNameIdQc},'%')",
			" </when>",
			" <when test='mediaNameQc!=null and mediaNameQc!=\"\"'>",
			" AND a.media_name like concat('%',#{mediaNameQc},'%')",
			" </when>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='userNameQc!=null and userNameQc!=\"\"'>",
			" AND b.user_name like concat('%',#{userNameQc},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.issued_date &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.issued_date &lt;= #{endTime}",
			" </when>",
			" )) c",
			"</script>" })
	Map listPgForSelectArticleSum(Map map);

	@Select("select c.id " + " from fee_outgo a,fee_outgo_article b,t_biz_article c" + " where a.id=b.outgo_id and b.article_id=c.id " + " and a.state>-2 and c.state>-2 and a.id=#{outgoId}")
	List<Integer> queryArticleIdsByOutgoId(@Param("outgoId") Integer outgoId);

	@Select("select c.id " + " from fee_outgo a,fee_outgo_article b,t_biz_article c" + " where a.id=b.outgo_id and b.article_id=c.id " + " and a.state>-2 and c.state>-2 and a.id=#{outgoId} and c.outgo_states=#{state}")
	List<Integer> queryArticleIdsByOutgoIdAndState(@Param("outgoId") Integer outgoId, @Param("state") Integer state);

	@Select("select c.id " + " from fee_outgo a,fee_outgo_article b,t_biz_article c" + " where a.id=b.outgo_id and b.article_id=c.id " + " and a.state>-2 and c.state>-2 and a.id=#{outgoId}")
	List<Integer> selectArticleId(@Param("outgoId") Integer outgoId);

	@Select({"<script>",
			" select sum(ifnull(c.outgo_amount, 0)) outgoSum,sum(ifnull(c.sale_amount, 0))saleSum FROM fee_outgo a,fee_outgo_article b,t_biz_article c\n"
					+ " where a.id=b.outgo_id and b.article_id=c.id"
					+ " and a.state>-2 and c.state > -2 and a.id=#{id}",
			"</script>"})
	Map querySumAmount(@Param("id") Integer id);

	@Select("select b.amount,c.* " + " from fee_outgo a,fee_outgo_borrow b,fee_borrow c" + " where a.id=b.outgo_id and b.borrow_id=c.id " + " and a.state>-2 and c.state>-2 and a.id=#{outgoId}")
	List<Map> queryBorrowById(@Param("outgoId") Integer outgoId);

	//查询从表
	@Select({"<script>",
			"select b.amount,b.outgo_id,c.*  from fee_outgo a,fee_outgo_borrow b,fee_borrow c where a.id=b.outgo_id and b.borrow_id=c.id and a.state>-2 and c.state>-2 and a.id in " +
					"<foreach collection=\"ids\" item=\"id\" open=\"(\" close=\")\" separator=\",\">\n" +
					"    #{id}\n" +
					"</foreach>",
			"</script>"})
	List<Map<String, Object>> listBorrowByIds(@Param("ids") List<Integer> ids);

	//查询应付合计
	@Select({"<script>",
			" select a.id, sum(ifnull(c.outgo_amount, 0)) as outgoSum,sum(ifnull(c.sale_amount, 0)) as saleSum FROM fee_outgo a,fee_outgo_article b,t_biz_article c\n"
					+ " where a.id=b.outgo_id and b.article_id=c.id"
					+ " and a.state>-2 and c.state > -2 and a.id in " +
					"<foreach collection=\"ids\" item=\"id\" open=\"(\" close=\")\" separator=\",\">\n" +
					"    #{id}\n" +
					"</foreach>" +
                    "GROUP BY a.id",
			"</script>"})
	List<Map<String, Object>> listSumAmountByIds(@Param("ids") List<Integer> ids);

	@Update("update fee_outgo set fund_amount=0 where id=#{outgoId}")
	void initFundAmount(@Param("outgoId") Integer outgoId);

	@Select("select feeOutgo.outgo_id from fee_outgo_article feeOutgo,t_biz_article article where feeOutgo.article_id=article.id and article.id=#{articleId}")
	Integer queryOutgoId(@Param("articleId") Integer articleId);

	@Update("update fee_outgo set out_account_id=null,out_account_name=null,pay_time=null,pay_user_id=null,pay_amount=0,state=-1,update_user_id=#{userId} where id = #{id}")
	void returnOutgoInfo(@Param("id") Integer id, @Param("userId") Integer userId);

	/**
	 * 废弃
	 * @param list
	 */
	@Update({"<script>",
			" update t_biz_article " +
					"<trim prefix='set' suffixOverrides=','>" +
					"<trim prefix='outgo_states = case' suffix='end,'>" +
					"<foreach collection='list' item='item' index='index' >" +
					" when id=#{item.id} then #{item.outgoStates} " +
					"</foreach>" +
					"</trim>" +
					"<trim prefix='outgo_total_amount = case' suffix='end,'>" +
					"<foreach collection='list' item='item' index='index' >" +
					" when id=#{item.id} then #{item.outgoTotalAmount} " +
					"</foreach>" +
					"</trim>" +
					"<trim prefix='outgo_id = case' suffix='end,'>" +
					"<foreach collection='list' item='item' index='index' >" +
					" when id=#{item.id} then #{item.outgoId} " +
					"</foreach>" +
					"</trim>" +
					"<trim prefix='outgo_code = case' suffix='end,'>" +
					"<foreach collection='list' item='item' index='index' >" +
					" when id=#{item.id} then #{item.outgoCode} " +
					"</foreach>" +
					"</trim>" +
					"</trim>" +
					" where " +
					" <foreach collection='list' separator='or' item='i' index='index'>" +
					" id=#{i.id}" +
					" </foreach>",
			"</script>"})
	void updateArticleOutgoInfoBatch(List<Article> list);

	@Update({"<script>",
			" update t_biz_article set " +
					" outgo_id=#{outgoId, jdbcType=INTEGER}," +
					" outgo_code=#{outgoCode, jdbcType=VARCHAR}," +
					" outgo_states=#{outgoStates, jdbcType=INTEGER}," +
					" outgo_total_amount=#{outgoTotalAmount, jdbcType=DOUBLE}" +
					" where id in " +
					"<foreach collection='articleIds' item='item' open='(' separator=',' close=')'>" +
					"#{item, jdbcType=INTEGER}" +
					"</foreach>",
			"</script>"})
	void updateArticleOutgoInfoBatchNew(Map<String, Object> map);

	@Update({"<script>",
			" update t_biz_article set " +
					" outgo_total_amount=#{outgoTotalAmount, jdbcType=DOUBLE} " +
					" where id in " +
					"<foreach collection='list' item='item' open='(' separator=',' close=')'>" +
					"#{item, jdbcType=INTEGER}" +
					"</foreach>",
			"</script>"})
	void updateArticleOutgoTotalAmountBatch(Map<String, Object> map);

	@Update({"<script>" ,
			" update fee_outgo set " +
					" invoice_code=#{invoiceCode ,jdbcType=VARCHAR}," +
					" invoice_rise=#{invoiceRise ,jdbcType=VARCHAR}," +
					" invoice_tax=#{invoiceTax,jdbcType=DOUBLE}," +
					" invoice_type=#{invoiceType,jdbcType=INTEGER}," +
					" backfill_time=#{backfillTime}" +
					" where id=#{id}" +
					" </script>"
	})
	int backfill(Outgo outgo);

//	@Update({"<script>",
//			" update t_biz_article " +
//					"<trim prefix='set' suffixOverrides=','>" +
//					"<trim prefix='outgo_total_amount = case' suffix='end,'>" +
//					"<foreach collection='list' item='item' index='index' >" +
//					" when id=#{item.id} then #{item.outgoTotalAmount} " +
//					"</foreach>" +
//					"</trim>" +
//					"</trim>" +
//					" where " +
//					" <foreach collection='list' separator='or' item='i' index='index'>" +
//					" id=#{i.id}" +
//					" </foreach>",
//			"</script>"})
//	void updateArticleOutgoTotalAmountBatch(List<Article> list);

	@Insert({"<script>",
			" insert into fee_outgo_article (" +
					"outgo_id," +
					"article_id" +
					") values " +
					" <foreach collection='list' item='item' separator=',' >" +
					"(#{item.outgoId}," +
					"#{item.articleId})" +
					"</foreach>",
			"</script>"})
	void insertOutgoArticleBatch(List<OutgoArticle> list);
	@Select({"<script>" +
			"select sum(a.pay_amount) payAmount ,sum(a.apply_amount)applyAmount" +
			" FROM fee_outgo a " +
			"left join t_media_supplier t on a.supplier_id =t.id"
			+ " where a.state>-2",
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
			" <when test='roleType==\"MJ\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" <foreach collection='user.deptIdSet' item='item' open=' and a.dept_id in (' close=')' separator=','>",
			" #{item}",
			" </foreach>",
			" </when>",
			"</choose>",
			" </when>",
			" <when test='roleType==\"YW\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" and a.company_code=#{user.dept.companyCode} ",
			" </when>",
			"</choose>",
			" </when>",

			" <otherwise>",
			" and  a.id=0",
			" </otherwise>",
			" </choose>",
			" <when test='supplierNameQc!=null and supplierNameQc!=\"\"'>",
			" AND a.supplier_name like concat('%',#{supplierNameQc},'%')",
			" </when>",
			" <when test='mTypeName!=null and mTypeName!=\"\"'>",
			" and t.media_type_id = #{mTypeName}",
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
			" <when test='companyCodeQc!=null and companyCodeQc!=\"\"'>",
			" AND a.company_code = #{companyCodeQc}",
			" </when>",
			" <when test='supplierContactor!=null and supplierContactor!=\"\"'>",
			" AND a.supplier_contactor like concat('%',#{supplierContactor},'%')",
			" </when>",
			" <when test='titleQc1!=null and titleQc1!=\"\"'>",
			" AND a.title like concat('%',#{titleQc1},'%')",
			" </when>",
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND a.code like concat('%',#{codeQc},'%')",
			" </when>",
			" <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
			" AND a.apply_time &gt;= #{applyStartTimeQc}",
			" </when>",
			" <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
			" AND a.apply_time &lt;= STR_TO_DATE(concat(#{applyEndTimeQc}, ' 23:59:59'), '%Y/%m/%d %T')",
			" </when>",
			" <when test='payStartTimeQc!=null and payStartTimeQc!=\"\"'>",
			" AND a.pay_time &gt;= #{payStartTimeQc}",
			" </when>",
			" <when test='payEndTimeQc!=null and payEndTimeQc!=\"\"'>",
			" AND a.pay_time &lt;= STR_TO_DATE(concat(#{payEndTimeQc}, ' 23:59:59'), '%Y/%m/%d %T')",
			" </when>",
			" <when test='expertStartTime!=null and expertStartTime!=\"\"'>",
			" AND a.expert_pay_time &gt;= #{expertStartTime}",
			" </when>",
			" <when test='expertEndTime!=null and expertEndTime!=\"\"'>",
			" AND a.expert_pay_time &lt;= #{expertEndTime}",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_id in (${deptIds})",
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
			" <when test='stateQc==23'>",
			" AND a.state =23 ",
			" </when>",
			" <when test='stateQc==26'>",
			" AND a.state =26 ",
			" </when>",
			"<when test = 'stateQc == 10'>" +
					"and a.state =26 and TIMESTAMPDIFF(DAY,a.pay_time,NOW())>10 " +
					"</when>",
			" <when test='stateQc==2'>",
			" AND a.state in (2,16)  ",
			" </when>",
			"<when test ='stateQc==4'>",
			"and a.state in (9,12)",
			"</when>",
			" <when test='stateQc==3'>",
			" AND a.state in (3,4,5,6,7,8,10) ",
			" </when>",
			" <otherwise>",
			" and  a.id=0 ",
			" </otherwise>",
			" </choose>",
			" </when>",
			" order by ",
			" <when test='sidx != null and sidx != \"\"'>",
			" a.state desc,${sidx} ${sord}",
			" </when>",
			" <when test='sidx == null or sidx ==  \"\"'>",
			" a.state desc,a.id desc",
			" </when>",
			"</script>"})
	Map reimburseSum(Map map);

	@Select ("update fee_outgo SET out_account_id = #{outAccountId},out_account_name=#{outAccountName}," +
			"pay_amount =#{payAmount},pay_time =#{payTime} , pay_user_id =#{updateUserId}, update_user_id=#{updateUserId}," +
			"state = #{state} where id=#{id}")
	Outgo updateaccount(Outgo entity);

	@Select("select parent_type from t_media_plate where id=#{mediaTypeId}")
	Integer selectMediaType(Integer mediaTypeId);

	@Select("SELECT\n" +
			"\tsu.id,\n" +
			"  su.`name`,\n" +
			" tumt.media_type_id\n" +
			"FROM\n" +
			"\tsys_user su\n" +
			"LEFT JOIN sys_user_role sur ON su.id = sur.user_id\n" +
			"LEFT JOIN sys_role sr ON sur.role_id = sr.id\n" +
			"LEFT JOIN t_user_media_type tumt on su.id = tumt.user_id \n" +
			"AND sr.state >- 2\n" +
			"WHERE\n" +
			"\tsu.state >- 2\n" +
			"AND sr.type ='MJ'\n" +
			"AND sr. CODE ='ZZ'\n" +
			"AND su.company_code = #{companyCode}\n" +
			"and tumt.media_type_id = #{mediaTypeId} " )
	List<Map> mediaGroupLeader(Map map);

	@Select("SELECT parent_type from t_media_plate where id= #{mediaTypeId}")
	int mediaTypeId(Integer mediaTypeId);

	@Select ("update fee_outgo SET pay_time = null where id=#{id}")
	void updatePayTimeForNull(@Param("id") Integer outgoId);

	@Select({"<script>" ,
			"select outgo_id as id,SUM(outgo_amount) as outgoAmountSum from t_biz_article  " ,
			"<when test ='list != null and list.size()>0'>" ,
			"where state>-2 and  outgo_id in " ,
			"<foreach item='item' collection = 'list' index= 'index' open='(' close=')' separator=','>" ,
			" #{item}" ,
			"</foreach>" ,
			"</when>" ,
			"group by outgo_id" ,
			"</script>"})
	List<Map> sum(List<Integer> list);

	@Select({ "<script>",
			" SELECT distinct a.id id ,a.code code,a.title title,a.supplier_id supplierId," +
					" a.supplier_name supplierName,a.supplier_contactor supplierContactor,"
					+ " a.account_id accountId,a.account_name accountName,a.account_bank_no accountBankNo,"
					+ " a.account_bank_name accountBankName,a.out_account_id outAccountId,a.out_account_name outAccountName,"
					+ " a.apply_id applyId,a.apply_name applyName,a.dept_id deptId,a.dept_name deptName,"
					+ " a.apply_amount applyAmount,a.remark remark,a.company_code companyCode, t.company_code companyCodet,"
					+ " DATE_FORMAT(a.expert_pay_time,\"%Y-%m-%d\") expertPayTime, "
					+ " DATE_FORMAT(a.pay_time,\"%Y-%m-%d\") payTime, "
					+ " DATE_FORMAT(a.apply_time,\"%Y-%m-%d %H:%i\") applyTime, "
					+ " a.pay_amount payAmount,"
					+ " a.state state,a.creator creator,a.create_time createTime,"
					+ " a.update_user_id updateUserId,a.update_time updateTime,a.fund_flag fundFlag,a.fund_amount fundAmount,"
					+ " a.task_id taskId,a.item_id itemId,a.media_type_id mediaTypeId,tmp.parent_type parentType," +
					" a.invoice_flag invoiceFlag ,a.invoice_type invoiceType,a.actual_cost actualCost,a.outgo_tax outgoTax," +
					" a.tax_amount taxAmount,a.outgo_erase_amount outgoEraseAmount,a.cost_erase_amount costEraseAmount,a.invoice_tax invoiceTax," +
					" a.invoice_rise invoiceRise, a.invoice_code invoiceCode ,a.backfill_time backfillTime,su.company_code creatorCompanyCode "
					+ " FROM fee_outgo a " +
					"left join t_media_supplier t on a.supplier_id =t.id" +
					" left join t_media_plate tmp on tmp.id = a.media_type_id " +
					" left join sys_user su on a.creator = su.id"
					+ " where a.state>-2 and (a.cost_erase_amount!=0 or a.outgo_erase_amount!=0) ",
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
			" <when test='roleType==\"MJ\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" <foreach collection='user.deptIdSet' item='item' open=' and a.dept_id in (' close=')' separator=','>",
			" #{item}",
			" </foreach>",
			" </when>",
			"</choose>",
			" </when>",
			" <when test='roleType==\"YW\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" and a.company_code=#{user.dept.companyCode} ",
			" </when>",
			"</choose>",
			" </when>",

			" <otherwise>",
			" and  a.id=0",
			" </otherwise>",
			" </choose>",
			" <when test='supplierNameQc!=null and supplierNameQc!=\"\"'>",
			" AND a.supplier_name like concat('%',#{supplierNameQc},'%')",
			" </when>",
			" <when test='mTypeName!=null and mTypeName!=\"\"'>",
			" and t.media_type_id = #{mTypeName}",
			" </when>",
			" <when test='invoiceRise!=null and invoiceRise!=\"\"'>",
			" and a.invoice_rise = #{invoiceRise}",
			" </when>",
			" <when test='invoiceCode!=null and invoiceCode!=\"\"'>",
			" and a.invoice_code = #{invoiceCode}",
			" </when>",
			" <when test='outgoTax!=null and outgoTax!=\"\"'>",
			" and a.outgo_tax = #{outgoTax}/100 and a.invoice_flag= 1",
			" </when>",
			" <when test='invoiceTax!=null and invoiceTax!=\"\"'>",
			" and a.invoice_tax = #{invoiceTax} and a.invoice_flag= 1",
			" </when>",
			" <when test='invoiceType!=null and invoiceType!=\"\"'>",
			" and a.invoice_type = #{invoiceType}",
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
			" <when test='companyCodeQc!=null and companyCodeQc!=\"\"'>",
			" AND a.company_code = #{companyCodeQc}",
			" </when>",
			" <when test='supplierContactor!=null and supplierContactor!=\"\"'>",
			" AND a.supplier_contactor like concat('%',#{supplierContactor},'%')",
			" </when>",
			" <when test='titleQc1!=null and titleQc1!=\"\"'>",
			" AND a.title like concat('%',#{titleQc1},'%')",
			" </when>",
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND a.code like concat('%',#{codeQc},'%')",
			" </when>",
			" <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
			" AND a.apply_time &gt;= #{applyStartTimeQc}",
			" </when>",
			" <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
			" AND a.apply_time &lt;= STR_TO_DATE(CONCAT(#{applyEndTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='backfillTimeState!=null and backfillTimeState!=\"\"'>",
			" AND a.backfill_time &gt;= #{backfillTimeState}",
			" </when>",
			" <when test='backfillTimeEnd!=null and backfillTimeEnd!=\"\"'>",
			" AND a.backfill_time &lt;= STR_TO_DATE(CONCAT(#{backfillTimeEnd},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='payStartTimeQc!=null and payStartTimeQc!=\"\"'>",
			" AND a.pay_time &gt;= #{payStartTimeQc}",
			" </when>",
			" <when test='payEndTimeQc!=null and payEndTimeQc!=\"\"'>",
			" AND a.pay_time &lt;= STR_TO_DATE(CONCAT(#{payEndTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='expertStartTime!=null and expertStartTime!=\"\"'>",
			" AND a.expert_pay_time &gt;= #{expertStartTime}",
			" </when>",
			" <when test='expertEndTime!=null and expertEndTime!=\"\"'>",
			" AND a.expert_pay_time &lt;= #{expertEndTime}",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_id in (${deptIds})",
			" </when>",
			" <when test='stateQc!=null and stateQc!=\"\"'>",
			" <choose>",
			" <when test='stateQc==-1'>",
			" AND a.state =-1 ",
			" </when>" +
					"<when test = 'stateQc == 10'>" +
					"and a.state =26 and TIMESTAMPDIFF(DAY,a.pay_time,NOW())>10 " +
					"</when>",
			" <when test='stateQc==0'>",
			" AND a.state =0 ",
			" </when>",
			" <when test='stateQc==1'>",
			" AND a.state =1 ",
			" </when>",
			" <when test='stateQc==23'>",
			" AND a.state =23 ",
			" </when>",
			" <when test='stateQc==26'>",
			" AND a.state =26 ",
			" </when>",
			" <when test='stateQc==2'>",
			" AND a.state in (2,16)  ",
			" </when>",
			"<when test ='stateQc==4'>",
			"and a.state in (9,12)",
			"</when>",
			" <when test='stateQc==3'>",
			" AND a.state in (3,4,5,6,7,8,10) ",
			" </when>",
			" <otherwise>",
			" and  a.id=0 ",
			" </otherwise>",
			" </choose>",
			" </when>",
			" <when test='sidx != null and sidx != \"\"'>",
			" ORDER BY FIELD(a.state,0,-1,3,4,5,6,7,8,10,2,16,9,12,1),${sidx} ${sord}",
			" </when>",
			" <when test='sidx == null or sidx ==  \"\"'>",
			" ORDER BY FIELD(a.state,0,-1,3,4,5,6,7,8,10,2,16,9,12,1),a.id desc",
			" </when>",
			"</script>" })
	List<Map> resetListPg(Map map);



	@Select({"<script>" +
			"select sum(a.pay_amount) payAmount ,sum(a.apply_amount)applyAmount ," +
			" (SELECT SUM(outgo_amount)  from t_biz_article tba left join fee_outgo a on tba.outgo_id= a.id where tba.state >-9 and a.state>-2 and (a.cost_erase_amount!=0 or a.outgo_erase_amount!=0) " +
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND outgo_code like concat('%',#{codeQc},'%')",
			" </when>",
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
			" <when test='roleType==\"MJ\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" <foreach collection='user.deptIdSet' item='item' open=' and a.dept_id in (' close=')' separator=','>",
			" #{item}",
			" </foreach>",
			" </when>",
			"</choose>",
			" </when>",
			" <when test='roleType==\"YW\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" and a.company_code=#{user.dept.companyCode} ",
			" </when>",
			"</choose>",
			" </when>",

			" <otherwise>",
			" and  a.id=0",
			" </otherwise>",
			" </choose>",
			" <when test='supplierNameQc!=null and supplierNameQc!=\"\"'>",
			" AND a.supplier_name like concat('%',#{supplierNameQc},'%')",
			" </when>",
			" <when test='mTypeName!=null and mTypeName!=\"\"'>",
			" and t.media_type_id = #{mTypeName}",
			" </when>",
			" <when test='outgoTax!=null and outgoTax!=\"\"'>",
			" and a.outgo_tax = #{outgoTax}/100 and a.invoice_flag= 1",
			" </when>",
			" <when test='invoiceTax!=null and invoiceTax!=\"\"'>",
			" and a.invoice_tax = #{invoiceTax} and a.invoice_flag= 1",
			" </when>",
			" <when test='invoiceRise!=null and invoiceRise!=\"\"'>",
			" and a.invoice_rise = #{invoiceRise}",
			" </when>",
			" <when test='invoiceCode!=null and invoiceCode!=\"\"'>",
			" and a.invoice_code = #{invoiceCode}",
			" </when>",
			" <when test='invoiceType!=null and invoiceType!=\"\"'>",
			" and a.invoice_type = #{invoiceType}",
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
			" <when test='companyCodeQc!=null and companyCodeQc!=\"\"'>",
			" AND a.company_code = #{companyCodeQc}",
			" </when>",
			" <when test='supplierContactor!=null and supplierContactor!=\"\"'>",
			" AND a.supplier_contactor like concat('%',#{supplierContactor},'%')",
			" </when>",
			" <when test='titleQc1!=null and titleQc1!=\"\"'>",
			" AND a.title like concat('%',#{titleQc1},'%')",
			" </when>",
			" <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
			" AND a.apply_time &gt;= #{applyStartTimeQc}",
			" </when>",
			" <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
			" AND a.apply_time &lt;= #{applyEndTimeQc}",
			" </when>",
			" <when test='backfillTimeState!=null and backfillTimeState!=\"\"'>",
			" AND a.backfill_time &gt;= #{backfillTimeState}",
			" </when>",
			" <when test='backfillTimeEnd!=null and backfillTimeEnd!=\"\"'>",
			" AND a.backfill_time &lt;= #{backfillTimeEnd}",
			" </when>",
			" <when test='payStartTimeQc!=null and payStartTimeQc!=\"\"'>",
			" AND a.pay_time &gt;= #{payStartTimeQc}",
			" </when>",
			" <when test='payEndTimeQc!=null and payEndTimeQc!=\"\"'>",
			" AND a.pay_time &lt;= #{payEndTimeQc}",
			" </when>",
			" <when test='expertStartTime!=null and expertStartTime!=\"\"'>",
			" AND a.expert_pay_time &gt;= #{expertStartTime}",
			" </when>",
			" <when test='expertEndTime!=null and expertEndTime!=\"\"'>",
			" AND a.expert_pay_time &lt;= #{expertEndTime}",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_id in (${deptIds})",
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
			" <when test='stateQc==23'>",
			" AND a.state =23 ",
			" </when>",
			" <when test='stateQc==26'>",
			" AND a.state =26 ",
			" </when>",
			"<when test = 'stateQc == 10'>" +
					"and a.state =26 and TIMESTAMPDIFF(DAY,a.pay_time,NOW())>10 " +
					"</when>",
			" <when test='stateQc==2'>",
			" AND a.state in (2,16)  ",
			" </when>",
			"<when test ='stateQc==4'>",
			"and a.state in (9,12)",
			"</when>",
			" <when test='stateQc==3'>",
			" AND a.state in (3,4,5,6,7,8,10) ",
			" </when>",
			" <otherwise>",
			" and  a.id=0 ",
			" </otherwise>",
			" </choose>",
			" </when>",
			" order by ",
			" <when test='sidx != null and sidx != \"\"'>",
			" a.state desc,${sidx} ${sord}",
			" </when>",
			" <when test='sidx == null or sidx ==  \"\"'>",
			" a.state desc,a.id desc",
			" </when>",
			" ) outgoamount," +
					" SUM(a.cost_erase_amount) costEraseAmountSum," +
					"  SUM(a.tax_amount) taxesTotalSum," +
					"  sum(a.outgo_erase_amount) outgoEraseAmountSum" +
					" FROM fee_outgo a " +
					"left join t_media_supplier t on a.supplier_id =t.id " +
					" where a.state>-2 and (a.cost_erase_amount!=0 or a.outgo_erase_amount!=0)",
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
			" <when test='roleType==\"MJ\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" <foreach collection='user.deptIdSet' item='item' open=' and a.dept_id in (' close=')' separator=','>",
			" #{item}",
			" </foreach>",
			" </when>",
			"</choose>",
			" </when>",
			" <when test='roleType==\"YW\"'>",
			" <choose>",
			" <when test='roleCode==\"YG\"'>",
			" and a.apply_id = #{user.id}",
			" </when>",
			" <when test='roleCode==\"ZZ\"'>",
			" and a.dept_id=#{user.deptId}",
			" </when>",
			" <when test='roleCode==\"BZ\"'>",
			" and a.company_code=#{user.dept.companyCode} ",
			" </when>",
			"</choose>",
			" </when>",

			" <otherwise>",
			" and  a.id=0",
			" </otherwise>",
			" </choose>",
			" <when test='supplierNameQc!=null and supplierNameQc!=\"\"'>",
			" AND a.supplier_name like concat('%',#{supplierNameQc},'%')",
			" </when>",
			" <when test='mTypeName!=null and mTypeName!=\"\"'>",
			" and t.media_type_id = #{mTypeName}",
			" </when>",
			" <when test='invoiceRise!=null and invoiceRise!=\"\"'>",
			" and a.invoice_rise = #{invoiceRise}",
			" </when>",
			" <when test='invoiceCode!=null and invoiceCode!=\"\"'>",
			" and a.invoice_code = #{invoiceCode}",
			" </when>",
			" <when test='invoiceType!=null and invoiceType!=\"\"'>",
			" and a.invoice_type = #{invoiceType}",
			" </when>",
			" <when test='outgoTax!=null and outgoTax!=\"\"'>",
			" and a.outgo_tax = #{outgoTax}/100 and a.invoice_flag= 1",
			" </when>",
			" <when test='invoiceTax!=null and invoiceTax!=\"\"'>",
			" and a.invoice_tax = #{invoiceTax} and a.invoice_flag= 1",
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
			" <when test='companyCodeQc!=null and companyCodeQc!=\"\"'>",
			" AND a.company_code = #{companyCodeQc}",
			" </when>",
			" <when test='supplierContactor!=null and supplierContactor!=\"\"'>",
			" AND a.supplier_contactor like concat('%',#{supplierContactor},'%')",
			" </when>",
			" <when test='titleQc1!=null and titleQc1!=\"\"'>",
			" AND a.title like concat('%',#{titleQc1},'%')",
			" </when>",
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND a.code like concat('%',#{codeQc},'%')",
			" </when>",
			" <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
			" AND a.apply_time &gt;= #{applyStartTimeQc}",
			" </when>",
			" <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
			" AND a.apply_time &lt;= #{applyEndTimeQc}",
			" </when>",
			" <when test='backfillTimeState!=null and backfillTimeState!=\"\"'>",
			" AND a.backfill_time &gt;= #{backfillTimeState}",
			" </when>",
			" <when test='backfillTimeEnd!=null and backfillTimeEnd!=\"\"'>",
			" AND a.backfill_time &lt;= #{backfillTimeEnd}",
			" </when>",
			" <when test='payStartTimeQc!=null and payStartTimeQc!=\"\"'>",
			" AND a.pay_time &gt;= #{payStartTimeQc}",
			" </when>",
			" <when test='payEndTimeQc!=null and payEndTimeQc!=\"\"'>",
			" AND a.pay_time &lt;= #{payEndTimeQc}",
			" </when>",
			" <when test='expertStartTime!=null and expertStartTime!=\"\"'>",
			" AND a.expert_pay_time &gt;= #{expertStartTime}",
			" </when>",
			" <when test='expertEndTime!=null and expertEndTime!=\"\"'>",
			" AND a.expert_pay_time &lt;= #{expertEndTime}",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_id in (${deptIds})",
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
			" <when test='stateQc==23'>",
			" AND a.state =23 ",
			" </when>",
			" <when test='stateQc==26'>",
			" AND a.state =26 ",
			" </when>",
			"<when test = 'stateQc == 10'>" +
					"and a.state =26 and TIMESTAMPDIFF(DAY,a.pay_time,NOW())>10 " +
					"</when>",
			" <when test='stateQc==2'>",
			" AND a.state in (2,16)  ",
			" </when>",
			"<when test ='stateQc==4'>",
			"and a.state in (9,12)",
			"</when>",
			" <when test='stateQc==3'>",
			" AND a.state in (3,4,5,6,7,8,10) ",
			" </when>",
			" <otherwise>",
			" and  a.id=0 ",
			" </otherwise>",
			" </choose>",
			" </when>",
			" order by ",
			" <when test='sidx != null and sidx != \"\"'>",
			" a.state desc,${sidx} ${sord}",
			" </when>",
			" <when test='sidx == null or sidx ==  \"\"'>",
			" a.state desc,a.id desc",
			" </when>",
			"</script>"})
	Map calculationOfTotal(Map map);
	@Select({"<script>" +
			" select code FROM sys_dict where type_code=#{typeCode} and type=#{type}" +
			"</script>"})
	String selectHTLScompanyCode(Map<String,Object> map);


	/**
	 * 更新请款状态
	 *
	 * @param map map参数中list是list<Integer>
	 */
	@Update({"<script>",
			"	update t_biz_article set outgo_states = #{state} where id in " +
					"		<foreach item='item' index='index' collection='list' open='(' separator=',' close=')'>" +
					"            #{item}" +
					"        </foreach>",
			"</script>"})
	void changeOutgoState(Map map);
}