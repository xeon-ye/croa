package com.qinfei.qferp.service.employ;

import com.qinfei.qferp.entity.employ.EmployeeBasic;

/**
 * 员工基础信息业务接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:22；
 */
public interface IEmployeeBasicService {
	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：员工基础信息对象；
	 * @return ：处理完毕的员工基础信息对象；
	 */
	EmployeeBasic saveOrUpdate(EmployeeBasic record);

	/**
	 * 根据父表ID更新学历相关的信息；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateEducationByParentId(EmployeeBasic record);

	/**
	 * 根据父表ID更新工作履历相关的信息；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateExperienceByParentId(EmployeeBasic record);

	/**
	 * 根据父表ID更新推荐人相关的信息；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateRelativeByParentId(EmployeeBasic record);

	/**
	 * 根据主键ID完善资料；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int completeEntryByParentId(EmployeeBasic record);

	/**
	 * 根据父表ID更新入职日期；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateEmpDateByParentId(EmployeeBasic record);

	/**
	 * 根据父表ID更新创建人信息；
	 *
	 * @param entryId：父表ID；
	 * @param userId：创建人ID；
	 * @param userName：创建人名称；
	 * @return ：操作影响的记录数；
	 */
	int updateCreateInfoByParentId(int entryId, int userId, String userName);
}