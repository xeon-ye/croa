package com.qinfei.qferp.mapper.employ;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.employ.EmployeeSalary;
import com.qinfei.qferp.excel.EmployeeExcelDTO;
import org.apache.ibatis.annotations.Param;

/**
 * 员工薪资数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployeeSalaryMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployeeSalary record);

	int updateEmpIdByEntryId(List<EmployeeExcelDTO> employeeExcelDTOList);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployeeSalary record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployeeSalary record);

	int insertSelectiveFormExcel(List<EmployeeSalary> list);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployeeSalary record);

	int updateByEntryId(EmployeeSalary record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployeeSalary record);

	/**
	 * 根据员工ID更新数据状态；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateStateByEmpId(EmployeeSalary record);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param salId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeSalary selectByPrimaryKey(Integer salId);

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param params：查询参数，entryId或者empId；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentId(Map<String, Object> params);

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param params：查询参数，entryId或者empId；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeSalary selectByParentId(Map<String, Object> params);

	List<EmployeeSalary> listByEntryId(@Param("entryIdList") List<Integer> entryIdList);
}