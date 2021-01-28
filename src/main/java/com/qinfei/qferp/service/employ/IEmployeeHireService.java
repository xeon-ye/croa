package com.qinfei.qferp.service.employ;

import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.qferp.entity.employ.EmployeeHire;
import com.github.pagehelper.PageInfo;

/**
 * 员工的录用记录接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/19 0012 20:28；
 */
public interface IEmployeeHireService {
	/**
	 * 保存或更新录用记录信息；
	 * 
	 * @param record：录用记录对象；
	 * @return ：处理完毕的对象；
	 */
	EmployeeHire saveOrUpdate(EmployeeHire record);

	/**
	 * 根据入职申请的流程审核结果更新关联的录用记录状态；
	 * 
	 * @param entryId：父表ID；
	 * @param state：状态；
	 */
	void updateHireState(int entryId, int state);

	/**
	 * 更新工号信息；
	 * 
	 * @param entryId：父表ID；
	 * @param empId：员工ID；
	 * @param empNum：工号；
	 */
	void updateHireNum(int entryId, int empId, String empNum);

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentId(int entryId);

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeHire selectByParentId(int entryId);

	/**
	 * 分页查询员工录用信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工录用信息集合；
	 */
	PageInfo<Map<String, Object>> selectPageHire(Map<String, Object> params, Pageable pageable);
}