package com.qinfei.qferp.mapper.employ;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.employ.EmployeeHire;
import org.apache.ibatis.annotations.Param;

/**
 * 员工录用记录数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployeeHireMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployeeHire record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployeeHire record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployeeHire record);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployeeHire record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployeeHire record);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param hireId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeHire selectByPrimaryKey(Integer hireId);

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentId(Integer entryId);

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeHire selectByParentId(Integer entryId);

	List<EmployeeHire> listHireByEntryId(@Param("entryIdList") List<Integer> entryIdList);

	/**
	 * 分页查询员工录用信息；
	 *
	 * @param params：查询参数；
	 * @return ：查询的员工录用信息集合；
	 */
	List<Map<String, Object>> selectPageHire(Map<String, Object> params);
}