package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.Drop;
import com.qinfei.qferp.entity.fee.Outgo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface DropMapper extends BaseMapper<Drop, Integer> {


	@Select({ "<script>",
			" SELECT distinct a.id id ,a.code code,a.title title,a.supplier_id supplierId," +
					" a.supplier_name supplierName,a.supplier_contactor supplierContactor,"
					+ " a.account_id accountId,a.account_name accountName,a.account_bank_no accountBankNo,"
					+ " a.account_bank_name accountBankName,a.out_account_id outAccountId,a.out_account_name outAccountName,"
					+ " a.apply_id applyId,a.apply_name applyName,a.dept_id deptId,a.dept_name deptName,"
					+ " a.apply_amount applyAmount,a.remark remark,"
					+ " DATE_FORMAT(a.expert_pay_time,\"%Y-%m-%d\") expertPayTime, "
					+ " DATE_FORMAT(a.pay_time,\"%Y-%m-%d\") payTime, "
					+ " DATE_FORMAT(a.apply_time,\"%Y-%m-%d %H:%i\") applyTime, "
					+ " a.pay_amount payAmount,"
					+ " a.state state,a.creator creator,a.create_time createTime,"
					+ " a.update_user_id updateUserId,a.update_time updateTime,a.fund_flag fundFlag,a.fund_amount fundAmount,"
					+ " a.task_id taskId,a.item_id itemId,tms.phone , tms.creator as supplierCreator,GROUP_CONCAT(DISTINCT tma.plate_id) AS plateIds "
					+ " FROM fee_drop a " +
					" LEFT JOIN t_media_supplier tms on tms.id = a.supplier_id " +
					"  LEFT JOIN t_media_supplier_relate_audit tmsra ON  tms.id = tmsra.supplier_id\n" +
					"   LEFT JOIN t_media_audit tma ON tmsra.media_id = tma.id "
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
						" <otherwise>",
						" and  a.id=0",
						" </otherwise>",
					" </choose>",
			" <when test='supplierName!=null and supplierName!=\"\"'>",
			" AND a.supplier_name like concat('%',#{supplierName},'%')",
			" </when>",
			" <when test='accountName!=null and accountName!=\"\"'>",
			" AND a.account_name like concat('%',#{accountName},'%')",
			" </when>",
			" <when test='outAccountName!=null and outAccountName!=\"\"'>",
			" AND a.out_account_name like concat('%',#{outAccountName},'%')",
			" </when>",
			" <when test='companyCode!=null and companyCode!=\"\"'>",
			" AND a.company_code = #{companyCode}",
			" </when>",
			" <when test='supplierContactor!=null and supplierContactor!=\"\"'>",
			" AND a.supplier_contactor like concat('%',#{supplierContactor},'%')",
			" </when>",
			" <when test='title!=null and title!=\"\"'>",
			" AND a.title like concat('%',#{title},'%')",
			" </when>",
			" <when test='applyStartTime!=null and applyStartTime!=\"\"'>",
			" AND a.apply_time &gt;= #{applyStartTime}",
			" </when>",
			" <when test='applyEndTime!=null and applyEndTime!=\"\"'>",
			" AND a.apply_time &lt;= STR_TO_DATE(concat(#{applyEndTime},' 23:59:59'),'%Y/%m/%d %T')",
			" </when>",
			" <when test='payStartTime!=null and payStartTime!=\"\"'>",
			" AND a.pay_time &gt;= #{payStartTime}",
			" </when>",
			" <when test='payEndTime!=null and payEndTime!=\"\"'>",
			" AND a.pay_time &lt;= #{payEndTime}",
			" </when>",
			" <when test='applyName!=null and applyName!=\"\"'>",
			" AND a.apply_name like concat('%',#{applyName},'%')",
			" </when>",
			" <when test='deptIds!=null and deptIds!=\"\"'>",
			" AND a.dept_id in (${deptIds})",
			" </when>",
			" <when test='state!=null and state!=\"\"'>",
				" <choose>",
					" <when test='state==-1'>",
						" AND a.state =-1 ",
					" </when>",
					" <when test='state==0'>",
					" AND a.state =0 ",
					" </when>",
					" <when test='state==1'>",
						" AND a.state =1 ",
					" </when>",
					" <when test='state==2'>",
						" AND a.state =2  ",
					" </when>",
					" <when test='state==12'>",
						" AND a.state=12 ",
					" </when>",
					" <when test='state==3'>",
						" AND a.state between 3 and 11 ",
					" </when>",
					" <otherwise>",
						" and  a.id=0 ",
					" </otherwise>",
				" </choose>",
			" </when>" +
					"group by a.id ",
			" order by ",
			" <when test='sidx != null and sidx != \"\"'>",
			" ${sidx} ${sord}",
			" </when>",
			" <when test='sidx == null or sidx ==  \"\"'>",
			" a.id desc",
			" </when>",
			"</script>" })
	List<Map> listPg(Map map);

	@Select("SELECT\n" +
			"\tfd.*,\n" +
			"\ttms.phone as supplierPhone,\n" +
			"\ttms.creator AS supplierCreator,\n" +
			"\tGROUP_CONCAT( DISTINCT tma.plate_id ) AS plateIds \n" +
			"FROM\n" +
			"\tfee_drop fd\n" +
			"\tLEFT JOIN t_media_supplier tms ON tms.id = fd.supplier_id\n" +
			"\tLEFT JOIN t_media_supplier_relate_audit tmsra ON tms.id = tmsra.supplier_id\n" +
			"\tLEFT JOIN t_media_audit tma ON tmsra.media_id = tma.id \n" +
			"WHERE\n" +
			"\tfd.state >- 2 \n" +
			"\tAND fd.id = #{id}\n" +
			"\tGROUP BY fd.id")
	Drop getById(Integer id);

	@Insert("insert into fee_drop_article (drop_id,article_id,state) values (#{dropId},#{articleId},0)")
	void insertDropArticle(@Param("dropId") Integer dropId, @Param("articleId") Integer articleId);

	@Delete("delete from fee_drop_article where drop_id=#{dropId}")
	void delDropArticle(@Param("dropId") Integer dropId);

	@Select({ "<script>",
			" select a.id dropId,c.id articleId,c.supplier_contactor supplierContactor,c.num Num,c.price_type priceType,\n"
					+ "c.supplier_name supplierName,c.media_name mediaName,c.media_user_name mediaUserName,\n"
					+ "c.title,c.link, c.income_amount incomeAmount,c.outgo_amount outgoAmount,"
					+ "c.sale_amount saleAmount,c.pay_amount payAmount,c.media_type_name mediaTypeName,c.inner_outer innerOuter,c.channel channel,c.electricity_businesses electricityBusinesses,"
                    + "DATE_FORMAT(c.issued_date,\"%Y-%m-%d\") issuedDate,DATE_FORMAT(c.promise_date,\"%Y-%m-%d\") promiseDate,"
					+ "d.user_name userName "
					+ " FROM fee_drop a,fee_drop_article b,t_biz_article c,t_biz_order d\n"
					+ " where a.id=b.drop_id and b.article_id=c.id and c.order_id=d.id "
					+ " and a.state>-2 and a.id=#{id}  "
					+ " order by c.id desc",
			"</script>" })
	List<Map> listPgForSelectedArticle(Integer id);

	@Select({ "<script>",
			"select a.id id,a.media_name mediaName,a.supplier_name supplierName,a.supplier_contactor supplierContactor,\n"
					+ "    a.media_user_name mediaUserName,a.brand brand,a.title title,a.link link,\n"
					+ "    a.sale_amount saleAmount,a.income_amount incomeAmount,a.taxes taxes,a.num Num,\n"
					+ "    a.price_column priceColumn,a.price_type priceType,a.pay_amount payAmount,\n"
					+ "    a.outgo_amount outgoAmount,a.income_date incomeDate,\n"
					+ "    a.other_pay otherPay,a.refund_amount refundAmount,a.remarks remarks,a.media_type_name mediaTypeName,\n"
					+ "    a.commission commission,a.inner_outer innerOuter,a.channel channel,a.electricity_businesses electricityBusinesses,"
					+ "    DATE_FORMAT(a.issued_date,\"%Y-%m-%d\") issuedDate,"
					+ "    DATE_FORMAT(a.promise_date,\"%Y-%m-%d\") promiseDate,"
					+ "    b.user_name userName,fo.code outgoCode \n"
					+ "    from t_biz_article a\n"
					+ "    left join t_biz_order b on a.order_id=b.id\n"
					+ "    left join sys_user su on b.user_id=su.id\n"
					+ "    left join fee_outgo_article fob on a.id=fob.article_id\n"
					+ "    left join fee_outgo fo on fo.id=fob.outgo_id and fo.state>-2\n"
					+ "    where b.state>-2 and a.state = 1 and a.issue_states = 4 and a.outgo_states=1 and a.commission_states=0 and a.income_states=0 and a.invoice_states=0 and mess_state=0"
					+ " and a.supplier_id=#{supplierIdSec} and su.company_code=#{companyCode}"
					+ " <when test='mediaUserNameQc!=null and mediaUserNameQc!=\"\"'>",
			" AND a.media_user_name like concat('%',#{mediaUserNameQc},'%')",
			" </when>",
			" <when test='media_type_id != null'>",
			" AND a.media_type_id = #{media_type_id}",
			" </when>",
			" <when test='mediaNameId!=null and mediaNameId!=\"\"'>",
			" AND a.media_user_id like concat('%',#{mediaNameId},'%')",
			" </when>",
			" <when test='mediaName!=null and mediaName!=\"\"'>",
			" AND a.media_name like concat('%',#{mediaName},'%')",
			" </when>",
			" <when test='title!=null and title!=\"\"'>",
			" AND a.title like concat('%',#{title},'%')",
			" </when>",
			" <when test='startTime!=null and startTime!=\"\"'>",
			" AND a.issued_date &gt;= #{startTime}",
			" </when>",
			" <when test='endTime!=null and endTime!=\"\"'>",
			" AND a.issued_date &lt;= #{endTime}",
			" </when>",
			" order by a.id desc",
			"</script>" })
	List<Map> listPgForSelectArticle(Map map);

	@Select("select c.id "
			+ " from fee_drop a,fee_drop_article b,t_biz_article c"
			+ " where a.id=b.drop_id and b.article_id=c.id "
			+ " and a.state>-2 and a.id=#{dropId}")
	List<Integer> queryArticleIdsByDropId(@Param("dropId") Integer dropId);

	@Select({"<script>",
			" select sum(ifnull(c.outgo_amount, 0)) outgoSum,sum(ifnull(c.sale_amount, 0))saleSum FROM fee_drop a,fee_drop_article b,t_biz_article c\n"
					+ " where a.id=b.drop_id and b.article_id=c.id"
					+ " and a.state>-2 and a.id=#{id}",
			"</script>"})
	Map querySumAmount(@Param("id") Integer id);

	@Select("select feeDrop.drop_id from fee_drop_article feeDrop,t_biz_article article where feeDrop.article_id=article.id and article.id=#{articleId}")
	Integer queryDropId(@Param("articleId") Integer articleId);

	@Select({"<script>" +
			" SELECT distinct a.* FROM fee_outgo a left join fee_outgo_article b on a.id = b.outgo_id where b.article_id in  " +
			" <foreach collection='articleIds' item='item' open=' (' close=')' separator=','>",
			" #{item}",
			" </foreach>",
			"</script>"})
	List<Outgo> queryOutgoByArticleIds(@Param("articleIds") List articleIds);

	/**
	 * 更新利润，map中list为Integer
	 *
	 * @param map
	 */
	@Update({"<script>",
			" update t_biz_article set state=#{state},update_time=now(),update_user_id=#{userId} where id in " +
					"        <foreach item='item' index='index' collection='list'" ,
					"                 open='(' separator=',' close=')'>" ,
					"            #{item}" ,
					"        </foreach>",
			"</script>"})
	void changeDropArticleBatch(Map map);

}
