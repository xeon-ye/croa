package com.qinfei.qferp.mapper.employ;

import com.qinfei.qferp.entity.employ.EmployEntryComment;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 入职申请的审核数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployEntryCommentMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployEntryComment record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployEntryComment record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployEntryComment record);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployEntryComment record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployEntryComment record);

	/**
	 * 根据员工ID更新数据状态；
	 *
	 * @param params：数据集合对象；
	 * @return ：操作影响的记录数；
	 */
	int updateStateByEmpId(Map<String, Object> params);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param comId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployEntryComment selectByPrimaryKey(Integer comId);

	/**
	 * 根据父表ID和类型查询主键ID；
	 *
	 * @param record：数据对象；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentId(EmployEntryComment record);

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：查询结果的封装对象；
	 */
	EmployEntryComment selectByParentId(EmployEntryComment record);

	/**
	 * 根据入职申请ID查询相关的审核信息；
	 * 
	 * @param entryId：入职申请ID；
	 * @return ：审核信息集合；
	 */
	List<EmployEntryComment> selectEmployInfo(Integer entryId);

	List<EmployEntryComment> listByEntryId(@Param("entryIdList") List<Integer> entryIdList);
}