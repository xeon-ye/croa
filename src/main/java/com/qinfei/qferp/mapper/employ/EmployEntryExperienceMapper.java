package com.qinfei.qferp.mapper.employ;

import java.util.List;

import com.qinfei.qferp.entity.employ.EmployEntryExperience;

/**
 * 入职申请的工作经历数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployEntryExperienceMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployEntryExperience record);

	//删除所有工作经历
	int deleteByEntryId(EmployEntryExperience record);

	/**
	 * 根据主键和父表ID删除单条记录；
	 * 
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKeyAndParentId(EmployEntryExperience record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployEntryExperience record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployEntryExperience record);

	//批量插入工作经历
	int insertSelectiveExcelBatch(List<EmployEntryExperience> employEntryExperienceList);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param expId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployEntryExperience selectByPrimaryKey(Integer expId);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployEntryExperience record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployEntryExperience record);

	/**
	 * 根据父表ID更新创建人信息；
	 * 
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateCreateInfoByParentId(EmployEntryExperience record);

	/**
	 * 根据入职申请的ID查询工作经历信息集合；
	 * 
	 * @param entryId：入职申请ID；
	 * @return ：工作经历信息集合；
	 */
	List<EmployEntryExperience> selectByEntryId(Integer entryId);
}