package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.sys.Dept;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface AccountMapper extends BaseMapper<Account, Integer> {


	@Select({ "<script>",
            " SELECT distinct a.id id,a.type type,a.company_id companyId,a.company_name companyName,a.contactor contactor,a.name name,a.bank_no bankNo,"
                    + " a.bank_name bankName, a.owner owner,a.phone phone,a.balance balance,"
                    + " a.remark remark,a.state state,a.account_type accountType,"
			        + " a.creator creator,a.create_time createTime,a.update_user_id updateUserId,a.update_time updateTime"
                    + " FROM fee_account a"
                    + " " +
			 " <choose>",
				" <when test='roleCode == \"ZC\" or user.companyCode == \"JT\"'>",
					" WHERE a.state >-2 and a.creator = #{user.id} ",
				" </when>",
				" <when test='roleCode == \"ZJL\" or roleCode==\"FZ\"'>",
					" WHERE a.state >-2 and a.company_code = #{user.companyCode} and a.creator = #{user.id} ",
				" </when>",
				" <when test='roleType==\"CW\"'>",
					" <choose>",
						" <when test='roleCode==\"ZL\"'>",
							" WHERE a.state >-2 and a.company_code = #{user.companyCode} and a.creator in <foreach collection='creators' item='creator' open='(' close=')' separator=','>",
							"#{creator.id}",
							" </foreach>",
						" </when>",
						" <when test='roleCode==\"YG\"'>",
							" WHERE a.state >-2 and a.company_code = #{user.companyCode} and a.creator in <foreach collection='creators' item='creator' open='(' close=')' separator=','>",
							"#{creator.id}",
							" </foreach>",
						" </when>",
						" <when test='roleCode==\"KJ\"'>",
							" WHERE a.state >-2 and a.company_code = #{user.companyCode}  and a.type = 1 ",
						" </when>",
						" <when test='roleCode==\"CN\"'>",
							" WHERE a.state >-2 and a.company_code = #{user.companyCode}  and a.type = 1 ",
						" </when>",
						" <when test='roleCode==\"ZZ\"'>",
							" WHERE a.state >-2 and a.company_code = #{user.companyCode}  and a.type = 1 ",
						" </when>",
						" <when test='roleCode==\"BZ\"'>",
							" WHERE a.state >-2 and a.company_code = #{user.companyCode}  and a.type = 1 ",
						" </when>",
						" <when test='roleCode==\"ZJ\"'>",
							" WHERE a.state >-2 and a.company_code = #{user.companyCode}  and a.type = 1 ",
						" </when>",
						"<otherwise>",
							" WHERE a.state >-2 and a.company_code = #{user.companyCode} and a.creator in <foreach collection='creators' item='creator' open='(' close=')' separator=','>",
							"#{creator.id}",
							" </foreach>",
						"</otherwise>",
					"</choose>",
				" </when>",
//			 	" <when test='roleType==\"YW\"'>",
//					" WHERE a.state >-2 and a.company_code = #{user.companyCode} and a.creator in <foreach collection='creators' item='creator' open='(' close=')' separator=','>",
//						"#{creator.id}",
//					" </foreach>",
//			 	" </when>",
			 	" <when test='roleType==\"MJ\"'>",
								" <choose>",
								" <when test='typeQc == 2'>",
								" inner join t_media_supplier b on a.company_id = b.id and a.type=2 " +
										" INNER JOIN t_user_media_type c ON a.creator = c.user_id and b.media_type_id=c.media_type_id and c.media_type_id in " +
										" (select d.media_type_id from t_user_media_type d where d.user_id = #{user.id}) " +
										" WHERE  a.state>-2 and a.company_code = #{user.companyCode}  ",
								" </when>",
								" <when test='typeQc == 4'>",
								" WHERE a.state >-2 and a.creator=#{user.id} and a.company_code = #{user.companyCode} ",
								" </when>",
								" <otherwise>",
									" WHERE  a.id=0 ",
								" </otherwise>",
								" </choose>",
				" </when>",
				" <otherwise>",
					" WHERE a.state >-2 and a.company_code = #{user.companyCode} and a.creator in <foreach collection='creators' item='creator' open='(' close=')' separator=','>",
						"#{creator.id}",
					" </foreach>",
				" </otherwise>",
			 " </choose>",
			" <when test='typeQc!=null and typeQc!=\"\"'>",
            " AND a.type = #{typeQc}",
            " </when>",
            " <when test='companyId!=null and companyId!=\"\"'>",
            " AND a.company_id = #{companyId}",
            " </when>",
            " <when test='companyNameQc!=null and companyNameQc!=\"\"'>",
			" AND a.company_name = #{companyNameQc}",
			" </when>",
			" <when test='companyNameQc==null or companyNameQc==\"\"'>",
				" <choose>",
				" <when test='roleType==\"MJ\" and roleCode==\"YG\"'>",
				" and a.creator = #{user.id}",
				" </when>",
				" <when test='roleType==\"MJ\" and roleCode==\"ZZ\"'>",
				" and a.creator = #{user.id}",
				" </when>",
				" <otherwise>",
				" </otherwise>",
				"</choose>",
			" </when>",
			" <when test='contactorQc!=null and contactorQc!=\"\"'>",
			" AND a.contactor like concat('%',#{contactorQc},'%')",
			" </when>",
            " <when test='nameQc!=null and nameQc!=\"\"'>",
            " AND a.name like concat('%',#{nameQc},'%')",
            " </when>",
            " <when test='dockingIdQc!=null and dockingIdQc!=\"\"'>",
			" AND a.docking_id = #{dockingIdQc}",
            " </when>",
            " <when test='bankNoQc!=null and bankNoQc!=\"\"'>",
            " AND a.bank_no like concat('%',#{bankNoQc},'%')",
            " </when>",
            " <when test='bankNameQc!=null and bankNameQc!=\"\"'>",
			" AND a.bank_name like concat('%',#{bankNameQc},'%')",
            " </when>",
            " <when test='ownerQc!=null and ownerQc!=\"\"'>",
            " AND a.owner like concat('%',#{ownerQc},'%')",
            " </when>",
			" <when test='accountTypeQc!=null'>",
			" AND a.account_type = #{accountTypeQc}",
			" </when>",
//			" <when test='creators != null'>",
//            "and a.creator in <foreach collection='creators' item='creator' open='(' close=')' separator=','>",
//            "#{creator}",
//            " </foreach>",
//            " </when>",
            " order by id desc",
            "</script>" })
	List<Map> listPg(Map map);

	@Select("select * from fee_account where id=#{id} and state>-2")
	Account getById(Integer id);
	@Select("select supplier_nature from t_media_supplier where id = #{id}")
	Integer supplierType(Integer id);
	@Select("select COUNT(*) from fee_account where bank_no = #{backNo} and state=1 and company_id =#{companyId} ")
	Integer selectAccount(@Param("backNo") String backNo, @Param("companyId") Integer companyId);

	@Select("select * from fee_account where name=#{name} and company_code=#{companyCode} and type=1 and company_id=0 and state>-2 order by id desc")
	List<Account> getCompanyAccountByName(@Param("name") String name,@Param("companyCode") String companyCode);

	@Select("select * from fee_account where type=1 and company_id=0 and company_code = #{companyCode} and state>-2 order by id desc")
	List<Account> queryCompanyAccountList(String companyCode);


	/**
	 *
	 * @param 'companyId'
	 *            根据companyId和type判断类型，公司内部账户companyId=0，其他为供应商id或者客户公司id
	 * @param 'type'
	 *            type=0未指定，type=1公司账户，type=2媒体供应商账户,type=3客户账户
	 * @return
	 */
	@Select({ "<script>",
			" SELECT id id,type type,company_id companyId,company_name companyName,"
                    + " name name,bank_no bankNo, bank_name bankName, owner owner,phone phone,"
                    + " balance balance, remark remark,state state,"
					+ " creator creator,create_time createTime,update_user_id updateUserId,"
                    + "update_time updateTime,docking_id dockingId,account_type accountType"
                    + " FROM fee_account "
                    + " where company_id=#{companyId} and type=#{type} and company_code = #{user.dept.companyCode} and state > -2 "
					+ " <when test='dockingId!=null and dockingId!=\"\"'>",
			" AND docking_id = #{dockingId} ",
            " </when>",
            " <when test='companyNameQc!=null and companyNameQc!=\"\"'>",
            " AND company_name like concat('%',#{companyNameQc},'%')",
            " </when>",
			" <when test='userId!=null and userId!=\"\"'>",
			" AND creator = #{userId} ",
			" </when>",
            " <when test='nameQc!=null and nameQc!=\"\"'>",
            " AND name like concat('%',#{nameQc},'%')",
			" </when>",
            " <when test='bankNoQc!=null and bankNoQc!=\"\"'>",
            " AND bank_no like concat('%',#{bankNoQc},'%')",
            " </when>",
            " <when test='bankNameQc!=null and bankNameQc!=\"\"'>",
            " AND bank_name like concat('%',#{bankNameQc},'%')",
            " </when>",
			" <when test='ownerQc!=null and ownerQc!=\"\"'>",
            " AND owner like concat('%',#{ownerQc},'%')",
            " </when>",
            " order by id desc ",
            "</script>" })
	List<Map> listPgForSelectAccount(Map map);

	@Select({"<script>",
			" SELECT id id,type type,company_id companyId,company_name companyName,"
					+ " name name,bank_no bankNo, bank_name bankName, owner owner,phone phone,"
					+ " balance balance, remark remark,state state,"
					+ " creator creator,create_time createTime,update_user_id updateUserId,"
					+ "update_time updateTime,docking_id dockingId,account_type accountType"
					+ " FROM fee_account "
					+ " where company_id=#{companyId} and type=#{type} and state > -2 "
					+ " <when test='dockingId!=null and dockingId!=\"\"'>",
			" AND docking_id = #{dockingId} ",
			" </when>",
			" <when test='companyNameQc!=null and companyNameQc!=\"\"'>",
			" AND company_name like concat('%',#{companyNameQc},'%')",
			" </when>",
			" <when test='userId!=null and userId!=\"\"'>",
			" AND creator = #{userId} ",
			" </when>",
			" <when test='nameQc!=null and nameQc!=\"\"'>",
			" AND name like concat('%',#{nameQc},'%')",
			" </when>",
			" <when test='bankNoQc!=null and bankNoQc!=\"\"'>",
			" AND bank_no like concat('%',#{bankNoQc},'%')",
			" </when>",
			" <when test='bankNameQc!=null and bankNameQc!=\"\"'>",
			" AND bank_name like concat('%',#{bankNameQc},'%')",
			" </when>",
			" <when test='ownerQc!=null and ownerQc!=\"\"'>",
			" AND owner like concat('%',#{ownerQc},'%')",
			" </when>",
			" order by id desc ",
			"</script>"})
	List<Map> listPgForSelectAccountNotCompanyCode(Map map);

	@Insert({"<script>",
			" insert into fee_account_dept (" +
					"account_id," +
					"dept_id" +
					") values " +
					" <foreach collection='list' item='item' separator=',' >" +
					"(#{item.accountId}," +
					"#{item.deptId})" +
					"</foreach>",
			"</script>"})
	void insertAccountDeptBatch(List<Map> list);

	@Delete("delete from fee_account_dept where account_id=#{accountId}")
	void deleteAccountDeptByAccountId(@Param("accountId") Integer accountId);

	@Delete(" <script>delete from fee_account_dept where account_id=#{accountId} " +
			" and dept_id in " +
			" <foreach item=\"item\" index=\"index\" collection=\"list\"\n" +
			"   open=\"(\" separator=\",\" close=\")\">\n" +
			"   #{item.id}\n" +
			" </foreach>" +
			" </script>")
	void deleteAccountDept(Map map);

	@Select(" select c.* from fee_account a,fee_account_dept b,sys_dept c " +
            " where a.id=b.account_id and b.dept_id=c.id " +
            " and a.state>-2 and c.state>-2 and a.id=#{id} ")
	List<Dept> queryDeptByAccountId(Integer id);

	@Update("update fee_account SET company_name = #{companyCodeName} where company_id = 0 and state <> -9 and company_code=#{companyCode}")
	void editAccountCompanyName(@Param("companyCodeName")String companyCodeName,@Param("companyCode")String companyCode);
}
