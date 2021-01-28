package com.qinfei.qferp.mapper.employ;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.employ.EmployeeFormal;
import org.apache.ibatis.annotations.Param;

/**
 * 员工转正记录数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployeeFormalMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployeeFormal record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployeeFormal record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployeeFormal record);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployeeFormal record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployeeFormal record);

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param empId：父表ID；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentId(Integer empId);

	/**
	 * 根据父表ID查询状态；
	 *
	 * @param empId：父表ID；
	 * @return ：查询的记录ID；
	 */
	Integer selectStateByParentId(Integer empId);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param formId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeFormal selectByPrimaryKey(Integer formId);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @return ：转正记录对象；
	 */
	EmployeeFormal selectByParentId(Integer empId);

	List<EmployeeFormal> listByEmpId(@Param("empIdList") List<Integer> empIdList);

	/**
	 * 分页查询员工转正信息；
	 *
	 * @param params：查询参数；
	 * @return ：查询的员工转正信息集合；
	 */
	List<Map<String, Object>> selectPageFormal(Map<String, Object> params);
}