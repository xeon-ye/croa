package com.qinfei.qferp.mapper.employ;

import com.qinfei.qferp.entity.employ.EmployeeLeave;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 员工离职记录数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployeeLeaveMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployeeLeave record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployeeLeave record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployeeLeave record);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployeeLeave record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployeeLeave record);

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param empId：父表ID；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentId(Integer empId);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param leaveId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeLeave selectByPrimaryKey(Integer leaveId);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @return ：离职记录对象；
	 */
	EmployeeLeave selectByParentId(Integer empId);

	List<EmployeeLeave> listByEmpId(@Param("empIdList") List<Integer> empIdList);

	/**
	 * 分页查询员工离职信息；
	 *
	 * @param params：查询参数；
	 * @return ：查询的员工离职信息集合；
	 */
	List<Map<String, Object>> selectPageLeave(Map<String, Object> params);
}