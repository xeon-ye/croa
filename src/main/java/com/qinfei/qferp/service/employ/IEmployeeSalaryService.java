package com.qinfei.qferp.service.employ;

import com.qinfei.qferp.entity.employ.EmployeeSalary;

/**
 * 员工的薪资管理接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/20 0012 20:28；
 */
public interface IEmployeeSalaryService {
	/**
	 * 保存或更新薪资信息；
	 * 
	 * @param record：薪资对象；
	 * @return ：处理完毕的对象；
	 */
	EmployeeSalary saveOrUpdate(EmployeeSalary record);

	/**
	 * 更新工号信息；
	 *
	 * @param entryId：父表ID；
	 * @param empId：员工ID；
	 * @param empNum：工号；
	 */
	void updateSalaryNum(int entryId, int empId, String empNum);

	/**
	 * 根据员工ID更新数据的状态；
	 *
	 * @param empId：员工ID；
	 * @param operate：操作类型，0为删除，1为离职；
	 */
	void updateStateByEmpId(int empId, int operate);

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentEntryId(int entryId);

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeSalary selectByParentEntryId(int entryId);

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param empId：父表ID；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentEmpId(int empId);

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param empId：父表ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeSalary selectByParentEmpId(int empId);
}