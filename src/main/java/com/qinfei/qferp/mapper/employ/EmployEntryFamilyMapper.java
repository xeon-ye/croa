package com.qinfei.qferp.mapper.employ;

import com.qinfei.qferp.entity.employ.EmployEntryFamily;

import java.util.List;

/**
 * 入职申请的家庭婚姻数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployEntryFamilyMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployEntryFamily record);

	/**
	 * 根据主键和父表ID删除单条记录；
	 * 
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKeyAndParentId(EmployEntryFamily record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployEntryFamily record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployEntryFamily record);

	//批量插入家庭成员
	int insertSelectiveExcelBatch(List<EmployEntryFamily> employEntryFamilyList);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployEntryFamily record);

	//更新家庭成员信息通过名称和关系
	int updateByRelationAndEntryId(EmployEntryFamily employEntryFamily);

	List<EmployEntryFamily> listFamInfoByRelationAndEntryId(EmployEntryFamily employEntryFamily);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployEntryFamily record);

	/**
	 * 根据父表ID更新创建人信息；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateCreateInfoByParentId(EmployEntryFamily record);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param famId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployEntryFamily selectByPrimaryKey(Integer famId);

	/**
	 * 根据入职申请的ID查询家庭成员信息集合；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：家庭成员信息集合；
	 */
	List<EmployEntryFamily> selectByEntryId(Integer entryId);
}