package com.qinfei.qferp.service.media;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.fee.AccountChange;
import com.qinfei.qferp.entity.media.Supplier;
import com.qinfei.qferp.entity.media1.SupplierChange;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface ISupplierService {

	String CACHE_KEY = "supplier";

	//校验供应商
	void checkSupplier(Integer id, String name, String phone);

	//供应商登记
	void addSupplier(Supplier supplier);

	//更新供应商
	void editSupplier(Supplier supplier);

	//更新供应商
	void updateSupplier(Map<String, Object> param);

	//供应商公司编辑
	void editSupplierCompany(Map<String, Object> param);

	PageInfo<Map<String, Object>> supplierAccountList(Pageable pageable, Map<String, Object> param);

	//更新媒体责任人
	void updateSupplierUserId(Integer id, Integer userId);

	//供应商异动列表
	List<SupplierChange> listSupplierChange(String supplierIds);

	//供应商账号异动列表
	List<AccountChange> listSupplierAccountChange(String accountIds);

	//以下是老代码20200717以前
    //查询媒体列表
	PageInfo<Supplier> listall(Map<String, Object> map, Pageable pageable);

	Supplier update(Supplier supplier);

	//删除供应商
	ResponseData delete(Integer id);

	void active(Integer id);

	void stop(Integer id);

	List<Supplier> listAllSupplierByPlateCompany(Integer mediaPlateId, Integer standarPhoneFlag);

	Supplier getById(Integer id);

	boolean isRepeat(String supplierName, String contactor, Integer mediaTypeId, String phone, String qqwechat);
	boolean isRepeat(String supplierName, String contactor, Integer mediaTypeId, String phone, String qqwechat,Integer id);

	PageInfo<Map> querySupplierList(int pageNum, int pageSize, Map map);

	PageInfo<Map> querySupplierListByTypeNew(int pageNum, int pageSize, Map map);

	//查询供应商信息的集合
	Map<String, Integer> listAllSupplierMap();

	//批量保存媒体数据
	Map<String, Object> batchAddSupplier(List<Object[]> excelContent);

	/**
	 * 获取供应商导入模板的列名集合；
	 *
	 * @return ：列名信息集合；
	 */
	List<String> getSupplierForms();

	/**
	 * 分页查找指定媒体板块的供应商信息；
	 *
	 * @param map：查询条件；
	 * @param pageable：分页对象；
	 * @return ：供应商分页集合；
	 */
	PageInfo<Supplier> listSupplierByPlateCompany(Map<String, Object> map, Pageable pageable);

	/**
	 * 供应商模板文件的操作提示信息；
	 *
	 * @return ：操作提示信息集合；
	 */
	List<String> getSupplierNotices();

	/**
	 * 供应商导出
	 * @param map
	 * @param outputStream
	 * @return
	 */
	List<Map> export(Map map, OutputStream outputStream);

	/**
	 * 供应商拷贝
	 */
	void copy(Integer id);

	//由于name 和 phone 需要建立唯一所以，所以需要处理历史供应商手机号码相同问题，以及现在被删除的供应商重复问题
	ResponseData handlerSameSupplierPhone();
}