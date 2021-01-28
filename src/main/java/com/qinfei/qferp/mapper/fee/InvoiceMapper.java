package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.entity.fee.Invoice;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InvoiceMapper extends BaseMapper<Invoice, Integer> {

	@Select({ "<script>",
			" SELECT a.id id,a.code code,a.no no,a.cust_company_id custCompanyId,a.cust_company_name custCompanyName,"
					+ " a.cust_id custId,a.cust_name custName,"
					+ " a.type type,a.invoice_type invoiceType,a.title title,a.tax_code taxCode,"
					+ " a.bank_no bankNo,a.bank_name bankName,a.address address,a.phone phone,a.amount amount,"
					+ " a.tax_point taxPoint,a.tax_amount taxAmount,a.invoice_desc invoiceDesc,"
					+ " a.state state,a.creator creator,"
					+ " DATE_FORMAT(a.invoice_time,\"%Y-%m-%d %H:%i\") invoiceTime,"
					+ " DATE_FORMAT(a.apply_time,\"%Y-%m-%d %H:%i\") applyTime,"
					+ " a.apply_id applyId,a.apply_name applyName,a.dept_id deptId,a.dept_name deptName,"
					+ " a.task_id taskId,a.item_id itemId,a.tax_type taxType,a.invoice_amount invoiceAmount,a.name name,tii.accept_worker acceptWorker"
					+ " FROM fee_invoice a " +
					" LEFT JOIN t_index_items tii on a.item_id = tii.id "
					+ " where a.state>-2 ",
					" <choose>",
						" <when test='roleCode == \"ZC\"'>",
						" and a.company_code != null ",
						" </when>",
						" <when test='roleType == \"JT\"'>",
						//" and a.company_code != null ",
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
						" and a.id=0",
						" </otherwise>",
					" </choose>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='taxType!=null and taxType!=\"\"'>",
			" AND tax_type = #{taxType}",
			" </when>",
			" <when test='invoiceTypeQc!=null and invoiceTypeQc!=\"\"'>",
			" AND invoice_type = #{invoiceTypeQc}",
			" </when>",
			" <when test='custCompanyNameQc!=null and custCompanyNameQc!=\"\"'>",
			" AND cust_company_name like concat('%',#{custCompanyNameQc},'%')",
			" </when>",
			" <when test='custNameQc!=null and custNameQc!=\"\"'>",
			" AND cust_name like concat('%',#{custNameQc},'%')",
			" </when>",
			" <when test='applyStartTimeQc!=null and applyStartTimeQc!=\"\"'>",
			" AND apply_time &gt;= #{applyStartTimeQc}",
			" </when>",
			" <when test='applyEndTimeQc!=null and applyEndTimeQc!=\"\"'>",
			" AND apply_time &lt;= STR_TO_DATE(concat(#{applyEndTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='startTimeQc!=null and startTimeQc!=\"\"'>",
			" AND invoice_time &gt;= #{startTimeQc}",
			" </when>",
			" <when test='endTimeQc!=null and endTimeQc!=\"\"'>",
			" AND invoice_time &lt;= STR_TO_DATE(concat(#{endTimeQc},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='applyNameQc!=null and applyNameQc!=\"\"'>",
			" AND apply_name like concat('%',#{applyNameQc},'%')",
			" </when>",
			" <when test='nameQc!=null and nameQc!=\"\"'>",
			" AND name like concat('%',#{nameQc},'%')",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND dept_id in (${deptIds})",
			" </when>",
			" <when test='stateQc!=null and stateQc!=\"\"'>",
				" <choose>",
					" <when test='stateQc==-1'>",
					" AND state =-1 ",
					" </when>",
					" <when test='stateQc==0'>",
					" AND state = 0 ",
					" </when>",
					" <when test='stateQc==1'>",
					" AND state = 1 ",
					" </when>",
					" <when test='stateQc==2'>",
					" AND state between 3 and 10 ",
					" </when>",
					" <when test='stateQc==3'>",
					" AND state = 2 ",
					" </when>",
					" <when test='stateQc==4'>",
					" AND state = 11 ",
					" </when>",
					" <otherwise>",
					" AND a.id=0 ",
					" </otherwise>",
				" </choose>",
			" </when>",
			" <when test='codeQc!=null and codeQc!=\"\"'>",
			" AND a.code like concat('%',#{codeQc},'%')",
			" </when>",
			" order by id desc",
			"</script>" })
	List<Map> listPg(Map map);

	@Select("select * from fee_invoice where state>-2 and id = #{id}")
	Invoice getById(Integer id);

	@Select({ "<script>", "select a.id id,a.media_name mediaName,a.supplier_name supplierName,\n"
			+ "    a.media_user_name mediaUserName,a.brand brand,a.title title,a.link link,a.inner_outer innerOuter,a.channel channel,a.electricity_businesses electricityBusinesses,\n"
			+ "    a.sale_amount saleAmount,a.income_amount incomeAmount,a.taxes taxes,\n"
			+ "    a.price_column priceColumn,a.price_type priceType,a.pay_amount payAmount,\n"
			+ "    a.outgo_amount outgoAmount,a.promise_date promiseDate,a.income_date incomeDate,"
			+ "    DATE_FORMAT(a.issued_date,\"%Y-%m-%d\") issuedDate,\n"
			+ "    a.other_pay otherPay,a.refund_amount refundAmount,a.remarks remarks,\n"
			+ "    a.commission commission,a.media_type_id mediaTypeId,a.media_type_name mediaTypeName,b.company_name companyName,b.cust_name custName,"
			+ "    b.user_name userName,b.no no,a.num num,a.brand brand\n"
			+ "    from t_biz_article a\n"
			+ "    left join t_biz_order b on a.order_id=b.id\n"
			+ "    where a.state = 1 and a.issue_states = 4 and b.state>-2 and a.commission_states=0 and a.invoice_states=0 " +
				" AND Date(a.issued_date) > '2019-01-31' "
			+ " and  b.company_id=#{custCompanyId} and b.cust_id=#{custId}"
			+ " <when test='mediaUserNameQc!=null and mediaUserNameQc!=\"\"'>",
			" AND a.media_user_name like concat('%',#{mediaUserNameQc},'%')",
			" </when>",
			" <when test='mediaNameQc!=null and mediaNameQc!=\"\"'>",
			" AND a.media_name like concat('%',#{mediaNameQc},'%')",
			" </when>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='brandQc!=null and brandQc!=\"\"'>",
			" AND a.brand like concat('%',#{brandQc},'%')",
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
			" order by a.issued_date asc,a.id desc",
			"</script>" })
	List<Map> listPgForSelectArticle(Map map);

	@Select({ "<script>", "select sum(a.sale_amount) saleAmountSum,sum(a.income_amount) incomeAmountSum,sum(a.outgo_amount) outgoAmountSum\n"
			+ "    from t_biz_article a\n"
			+ "    left join t_biz_order b on a.order_id=b.id\n"
			+ "    where a.state = 1 and a.issue_states = 4 and b.state>-2 and a.commission_states=0 and a.invoice_states=0 " +
			" AND Date(a.issued_date) > '2019-01-31' "
			+ " and  b.company_id=#{custCompanyId} and b.cust_id=#{custId}"
			+ " <when test='mediaUserNameQc!=null and mediaUserNameQc!=\"\"'>",
			" AND a.media_user_name like concat('%',#{mediaUserNameQc},'%')",
			" </when>",
			" <when test='mediaNameQc!=null and mediaNameQc!=\"\"'>",
			" AND a.media_name like concat('%',#{mediaNameQc},'%')",
			" </when>",
			" <when test='titleQc!=null and titleQc!=\"\"'>",
			" AND a.title like concat('%',#{titleQc},'%')",
			" </when>",
			" <when test='brandQc!=null and brandQc!=\"\"'>",
			" AND a.brand like concat('%',#{brandQc},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.issued_date &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.issued_date &lt;= #{endTime}",
			" </when>",
			"</script>" })
	Map listPgForSelectArticleSum(Map map);

	@Select({ "<script>",
			"SELECT\n" +
					"\ta.id incomeId,\n" +
					"\tc.id articleId,\n" +
					"\tc.supplier_name supplierName,\n" +
					"\tc.media_name mediaName,\n" +
					"\tc.media_user_name mediaUserName,\n" +
					"\tc.title title,\n" +
					"\tc.link link," +
					"c.promise_date promiseDate," +
					"c.income_date incomeDate," +
					"\tc.income_amount incomeAmount,\n" +
					"\tc.outgo_amount outgoAmount,\n" +
					"c.issued_date issuedDate,\n" +
					"c.media_type_id mediaTypeId,\n" +
					"c.media_type_name mediaTypeName,\n" +
					"\t(select \n" +
					"    GROUP_CONCAT(fi.trade_time) \n" +
					"  from\n" +
					"    fee_income fi,\n" +
					"    fee_income_article fia \n" +
					"  where fi.id = fia.income_id \n" +
					"    and c.id = fia.article_id) tradeTime,\n" +
					"c.sale_amount saleAmount,\n" +
					"c.taxes taxes," +
					"c.pay_amount payAmount,\n" +
					"d.company_name companyName,\n" +
					"d.cust_name custName,\n" +
					"d.user_id userId," +
					"d.user_name userName," +
					"d.NO NO," +
					"c.num num," +
					"c.inner_outer innerOuter,c.channel channel,c.electricity_businesses electricityBusinesses," +
					"c.brand brand " +
					"FROM " +
					" fee_invoice a," +
					" fee_invoice_article b," +
					" t_biz_order d," +
					" t_biz_article c" +
					" WHERE" +
					" a.id = b.invoice_id" +
					" AND b.article_id = c.id" +
					" AND c.order_id = d.id" +
					" AND a.id =#{id}" +
					" AND a.state >- 2" +
					" AND d.state >- 2" +
					" <when test='issueStartTime!=null and issueStartTime!=\"\"'>",
						" AND c.issued_date &gt;= #{issueStartTime}",
					" </when>",
					" <when test='issueEndTime!=null and issueEndTime!=\"\"'>",
						" AND c.issued_date &lt;= #{issueEndTime}",
					" </when>",
			"</script>" })
	List<Map> listPgForSelectedArticle(Map map);

	@Delete("delete from fee_invoice_article where invoice_id=#{invoiceId}")
	void delInvoiceArticle(@Param("invoiceId") Integer invoiceId);

	@Select("select sum(c.sale_amount) from fee_invoice a,fee_invoice_article b,t_biz_article c " + " where a.id=b.invoice_id and b.article_id=c.id " + " and a.state>-2 " + " and a.id=#{id}")
	Double getSumSaleAmountById(@Param("id") Integer id);

	@Select("select c.* " + " from fee_invoice a,fee_invoice_article b,t_biz_article c" + " where a.id=b.invoice_id and b.article_id=c.id " + " and a.state>-2 and a.id=#{invoiceId}")
	List<Article> queryArticleById(@Param("invoiceId") Integer invoiceId);

	@Select("select c.* " + " from fee_invoice a,fee_invoice_article b,t_biz_article c" + " where a.id=b.invoice_id and b.article_id=c.id " + " and a.state>-2 and c.state>-2 and c.invoice_states=#{state} and a.id=#{invoiceId}")
	List<Article> queryArticleByIdAndState(@Param("invoiceId") Integer invoiceId,@Param("state") Integer state);

	@Select("select c.id " + " from fee_invoice a,fee_invoice_article b,t_biz_article c" + " where a.id=b.invoice_id and b.article_id=c.id " + " and a.state>-2 and a.id=#{invoiceId}")
	List<Integer> queryArticleIdsByInvoiceId(@Param("invoiceId") Integer invoiceId);

	@Select("select c.id " + " from fee_invoice a,fee_invoice_article b,t_biz_article c" + " where a.id=b.invoice_id and b.article_id=c.id " + " and a.state>-2 and a.id=#{invoiceId} and c.invoice_states=#{state}")
	List<Integer> queryArticleIdsByInvoiceIdAndState(@Param("invoiceId") Integer invoiceId,@Param("state") Integer state);

	@Select("select feeInvoice.invoice_id from t_biz_article article , fee_invoice_article feeInvoice where article.id=feeInvoice.article_Id and article.id=#{articleId}")
	Integer queryInvoiceId(@Param("articleId") Integer articleId);

	/**
	 *
	 * map中的list未list<Article>
	 * @return
	 */
	@Update({" <script>" +
			" update t_biz_article " +
			" <trim prefix='set' suffixOverrides=','> " +
			"<trim prefix=\"taxes =case\" suffix=\"end,\">" +
			" <foreach collection=\"list\" item=\"item\" index=\"index\">" +
			"  <if test=\"item.taxes!=null\">" +
			"   when id=#{item.id} then #{item.taxes}" +
			"  </if>" +
			" </foreach>" +
			"</trim>" +
			"<trim prefix=\"tax_type=case\" suffix=\"end,\">" +
			" <foreach collection=\"list\" item=\"item\" index=\"index\">" +
			"  <if test=\"item.taxType!=null\">" +
			"   when id=#{item.id} then #{item.taxType}" +
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
			"<trim prefix=\"invoice_states =case\" suffix=\"end,\">" +
			" <foreach collection=\"list\" item=\"item\" index=\"index\">" +
			"  <if test=\"item.invoiceStates!=null\">" +
			"   when id=#{item.id} then #{item.invoiceStates}" +
			"  </if>" +
			" </foreach>" +
			"</trim>" +
			"<trim prefix=\"update_user_id =case\" suffix=\"end,\">" +
			" <foreach collection=\"list\" item=\"item\" index=\"index\">" +
			"  <if test=\"item.updateUserId!=null\">" +
			"   when id=#{item.id} then #{item.updateUserId}" +
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
	Integer updateArticleInfo(List<Article> list) ;


	@Select({"<script>",
			" select sum(ifnull(c.sale_amount, 0)) saleSum," +
					"sum(ifnull(c.income_amount, 0)) incomeSum, " +
					"sum(ifnull(c.pay_amount, 0)) paySum, " +
					"sum(ifnull(c.outgo_amount, 0)) outgoSum, " +
					"sum(ifnull(c.taxes, 0)) taxSum " +
					" FROM fee_invoice a,fee_invoice_article b,t_biz_article c\n"
					+ " where a.id=b.invoice_id and b.article_id=c.id"
					+ " and a.state>-2 and a.id=#{invoiceId}",
			"</script>"})
	Map querySumAmount(@Param("invoiceId") Integer invoiceId);

	@Insert({"<script>",
			" insert into fee_invoice_article (" +
					"invoice_id," +
					"article_id" +
					") values " +
					" <foreach collection='list' item='item' separator=',' >" +
					"(#{item.invoiceId}," +
					"#{item.articleId})" +
					"</foreach>",
			"</script>"})
	void insertInvoiceArticleBatch(List<Map<String,Integer>> list);

	@Select("select count(c.id) " + " from fee_invoice a,fee_invoice_article b,t_biz_article c" + " where a.id=b.invoice_id and b.article_id=c.id " + " and a.state>-2 and c.state>-2 and c.commission_states>0 and a.id=#{invoiceId}")
	Integer countByInvoiceIdAndCommissionStates(@Param("invoiceId") Integer invoiceId);

	/**
	 * 更新开票状态
	 *
	 * @param map map参数中list是list<Integer>
	 */
	@Update({"<script>",
			" update t_biz_article set invoice_states = #{state} where id in " +
			"        <foreach item='item' index='index' collection='list' " +
			"                 open='(' separator=',' close=')'> " +
			"            #{item} " +
			"        </foreach>",
			"</script>"})
	void changeInvoiceState(Map map);

	/**
	 * 更新开票状态
	 *
	 * @param map map参数中list是list<Article>
	 */
	@Update({"<script>",
			"	update t_biz_article set invoice_states = #{state} where id in " +
			"        <foreach item='item' index='index' collection='list' " +
			"                 open='(' separator=',' close=')'> " +
			"            #{item.id} " +
			"        </foreach>",
			"</script>"})
	void changeInvoiceState2(Map map);

	@Select({"<script>",
			"select count(id)" +
					" from t_biz_article " +
					" where invoice_states>0 " +
					" and id in " +
					"<foreach collection='list' item='item' open='(' close=')' separator=','>" +
					"#{item}" +
					" </foreach>",
			"</script>"})
	Integer listByIdsAndInvoiceStates(@Param("list") Set<Integer> list) ;
}
