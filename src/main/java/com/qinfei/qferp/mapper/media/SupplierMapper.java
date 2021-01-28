package com.qinfei.qferp.mapper.media;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.Account;
import com.qinfei.qferp.entity.media.Supplier;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface SupplierMapper extends BaseMapper<Supplier, Integer> {

	// @SelectProvider(type = ProviderUtil.class, method = "listByOrder")
	// List<Supplier> suppliers(@Param("supplier") Supplier supplier, String...
	// order);

	@Select("select * from t_media_supplier where state='0' order by id desc")
	List<Supplier> listAll(Supplier supplier);

	@Select("select * from t_media_supplier where state='0' and id=#{id}")
	Supplier getById(@Param("id") Integer id);

	@Select("select count(id) from t_media_supplier where state != -9 and contactor = #{contactor} and name = #{name} and media_type_id = #{mediaTypeId} and company_code = #{companyCode}")
	int getIdBySupplierNameAndContactor(@Param("name") String supplierName, @Param("contactor") String contactor, @Param("mediaTypeId") Integer mediaTypeId, @Param("companyCode") String companyCode);

	@Select("select count(id) from t_media_supplier where state != -9 and phone = #{phone} and contactor = #{contactor} and media_type_id = #{mediaTypeId} and company_code = #{companyCode}")
	int getIdByContactorAndPhone(@Param("contactor") String contactor, @Param("phone") String phone, @Param("mediaTypeId") Integer mediaTypeId, @Param("companyCode") String companyCode);

	@Select("select count(id) from t_media_supplier where state != -9 and qqwechat = #{qqwechat} and contactor = #{contactor} and media_type_id = #{mediaTypeId} and company_code = #{companyCode}")
	int getIdByContactorAndQqwechat(@Param("contactor") String contactor, @Param("qqwechat") String qqwechat, @Param("mediaTypeId") Integer mediaTypeId, @Param("companyCode") String companyCode);

	@Select("select count(id) from t_media_supplier where state != -9 and contactor = #{contactor} and name = #{name} and media_type_id = #{mediaTypeId} and company_code = #{companyCode} and id != #{id}")
	int getCountBySupplierNameAndContactor(@Param("name") String supplierName, @Param("contactor") String contactor, @Param("mediaTypeId") Integer mediaTypeId, @Param("companyCode") String companyCode, @Param("id")  Integer id);

	@Select("select count(id) from t_media_supplier where state != -9 and phone = #{phone} and contactor = #{contactor} and media_type_id = #{mediaTypeId} and company_code = #{companyCode} and id != #{id}")
	int getCountByContactorAndPhone(@Param("contactor") String contactor, @Param("phone") String phone, @Param("mediaTypeId") Integer mediaTypeId, @Param("companyCode") String companyCode, @Param("id")  Integer id);

	@Select("select count(id) from t_media_supplier where state != -9 and qqwechat = #{qqwechat} and contactor = #{contactor} and media_type_id = #{mediaTypeId} and company_code = #{companyCode} and id != #{id}")
	int getCountByContactorAndQqwechat(@Param("contactor") String contactor, @Param("qqwechat") String qqwechat, @Param("mediaTypeId") Integer mediaTypeId, @Param("companyCode") String companyCode, @Param("id")  Integer id);

	@Select({ "<script>", "select  a.* "
            + " from t_media_supplier a "
            + " where 1=1 and a.state=0 "
            + " <when test='supplierNameQc!=null and supplierNameQc!=\"\"'>",
            " AND a.name like '%${supplierNameQc}%'",
            " </when>",
            " order by a.id desc", "</script>" })
	List<Map> querySupplierList(Map map);

	List<Map> querySupplierListByTypeNew(Map map);

	@Select("select * from t_media_supplier where state>-2 order by id desc")
	List<Supplier> queryAll();

	/**
	 * 查询指定条件下的媒体供应商数据；
	 *
	 * @param map：查询条件集合；
	 * @return ：查询结果；
	 */
	List<Supplier> listPage(Map map);

	/**
	 * 查询指定条件下的媒体供应商数据(导出需要用到)
	 * @param map
	 * @return
	 */
	List<Map> listAllSupplier(Map map);

	/**
	 * 查询指定条件下供应商
	 */
	List<Supplier> listSupplierByPlateCompany(Map map);

	/**
	 * 删除供应商；
	 *
	 * @param id：供应商ID；
	 */
	@Update("update t_media_supplier set state = -9 where id = #{id}")
	void deleteSupplier(int id);

	/**
	 * 物理删除供应商；
	 *
	 * @param id：供应商ID；
	 */
	@Delete("DELETE FROM t_media_supplier WHERE id = #{id}")
	void physicalDeletionSupplier(int id);

	/**
	 * 批量保存数据；
	 *
	 * @param suppliers：供应商数据集合；
	 */
	void saveBatch(List<Supplier> suppliers);
	/**
     * 查询该供应商下是否有稿件
     * @param supplierId
     * @return
     */
    @Select("select count(id) from t_biz_article where state>-2 and supplier_id=#{supplierId}")
    int getArticleCount(Integer supplierId);

    /**
     * 查询该供应商下是否有稿件
     * @param supplierId
     * @return
     */
    @Select("select count(id) from t_biz_article_import where state>-2 and supplier_id=#{supplierId}")
    int getArticleImportCount(Integer supplierId);
    /**
     * 更新供应商信息时同步更新稿件中的供应商字段
     * @param supplier
     */
    @Update("update t_biz_article set supplier_name=#{name},supplier_contactor=#{contactor} where supplier_id = #{id}")
    void updateArticleSupplierInfo(Supplier supplier);
    /**
     * 更新供应商信息时同步更新稿件中的供应商字段
     * @param supplier
     */
    @Update("update t_biz_article_import set supplier_name=#{name},supplier_contactor=#{contactor} where supplier_id = #{id}")
    void updateArticleImportSupplierInfo(Supplier supplier);

    /**
     * 查询该供应商下是否有稿件
     * @param supplierId
     * @return
     */
//    @Select("select count(id) from t_media where state>-2 and supplier_id=#{supplierId}")
//    int getMediaCount(Integer supplierId);

    /**
     * 查询该供应商下是否有稿件
     * @param supplierId
     * @return
     */
//    @Select("select count(id) from t_media_info where state>-2 and supplier_id=#{supplierId}")
//    int getMediaInfoCount(Integer supplierId);
    /**
     * 更新供应商信息时同步更新稿件中的供应商字段
     * @param supplier
     */
//    @Update("update t_media set supplier_name=#{name} where supplier_id = #{id}")
//    void updateMediaSupplierInfo(Supplier supplier);
    /**
     * 更新供应商信息时同步更新稿件中的供应商字段
     * @param supplier
     */
//    @Update("update t_media_info set supplier_name=#{name} where supplier_id = #{id}")
//    void updateMediaInfoSupplierInfo(Supplier supplier);

	/**
	 * 查询改供应商绑定的账户
	 *
	 */
	@Select("select count(company_id) from fee_account where state>-2 and company_id =#{supplierId}")
	int getSupplierAccount(Integer supplierId);
	/**
	 * 更新供应商账户的供应商名
	 *
	 */
	@Update("update fee_account set company_name = #{name}, contactor = #{contactor}, name = #{name}  where company_id=#{id}")
	void  updateSupplierAccount(Supplier supplier);

	/**
	 * 根据媒介或媒介部门查询供应商列表
	 * @param map
	 */
	List<Map> listSupplierByMediaUser(Map map);

	List<Map> oldlistSupplierByMediaUser(Map map);

	/**
	 * 检查是否存在相同的供应商（供应商供应商+联系人、联系人+电话、联系人+QQ微信）
	 * @return 供应商ID
	 */
	Integer checkSupplierForParam(@Param("mediaTypeId") Integer mediaTypeId, @Param("companyCode") String companyCode, @Param("contactor") String contactor,@Param("name") String name, @Param("phone") String phone,  @Param("qqwechat") String qqwechat,  @Param("id")  Integer id);

	//根据供应商获取稿件数量
	int getArtCountBySupplierId(@Param("supplierId")Integer supplierId);

	//修改媒体供应商状态
	int updateMediaSupplierState(@Param("id") Integer id, @Param("state") Integer state, @Param("updateUserId") Integer updateUserId);

	//供应商联系人标准化校验
	Integer checkSupplier(@Param("id") Integer id, @Param("name") String name, @Param("phone") String phone, @Param("onlineTime") String onlineTime);

	//供应商判重校验
	Integer checkSupplierByPhoneList(@Param("name") String name, @Param("phoneList") List<String> phoneList, @Param("onlineTime") String onlineTime);

	//供应商公司标准化校验
	Integer checkSupplierCompany(@Param("name") String name, @Param("onlineTime") String onlineTime);

	//更新供应商信息
	int updateSupplier(Map<String, Object> param);

	//更新供应商公司名称
	int editSupplierCompany(Map<String, Object> param);

	//更新供应商账号
	int updateSupplierAccountByCompanyName(@Param("companyName") String companyName, @Param("onlineTime") String onlineTime);

	//更新供应商稿件
	int updateSupplierArtByCompanyName(@Param("companyName") String companyName, @Param("onlineTime") String onlineTime);

	//获取供应商账户列表
	List<Map<String, Object>> supplierAccountList(Map<String, Object> param);

	//更新媒体责任人
	int updateSupplierUserId(@Param("id") Integer id, @Param("userId") Integer userId, @Param("updatedId") Integer updateId);

	//获取所有供应商
	List<Supplier> listSupplier(@Param("onlineTime") String onlineTime);

	//根据ID获取供应商详细信息-供应商异动
	List<Supplier> listSupplierByIdsOrName(@Param("onlineTime") String onlineTime, @Param("ids") List<Integer> ids, @Param("name") String name);

	//根据供应商ID获取供应商账号详细信息-供应商账户异动
	List<Account> listAccountBySupplierIds(@Param("onlineTime") String onlineTime, @Param("companyIds") List<Integer> companyIds);

	//由于name 和 phone 需要建立唯一所以，所以需要处理历史供应商手机号码相同问题，以及现在被删除的供应商重复问题
	List<Supplier> listSameSupplierId(@Param("onlineTime") String onlineTime);

	int batchUpdateSameSupplier(@Param("supplierList") List<Supplier> supplierList);
}