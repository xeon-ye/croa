package com.qinfei.qferp.mapper.employ;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.employ.EmployeeTransfer;
import org.apache.ibatis.annotations.Param;

/**
 * 员工调岗记录数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployeeTransferMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployeeTransfer record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployeeTransfer record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployeeTransfer record);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployeeTransfer record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployeeTransfer record);

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
	 * @param empId：父表主键ID；
	 * @return ：转正记录对象；
	 */
	EmployeeTransfer selectByParentId(Integer empId);

	List<EmployeeTransfer> listByEmpId(@Param("empIdList") List<Integer> empIdList);

	List<EmployeeTransfer> listById(@Param("ids") List<Integer> ids);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param tranId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeTransfer selectByPrimaryKey(Integer tranId);

	/**
	 * 分页查询员工调岗信息；
	 *
	 * @param params：查询参数；
	 * @return ：查询的员工调岗信息集合；
	 */
	List<Map<String, Object>> selectPageTransfer(Map<String, Object> params);
}