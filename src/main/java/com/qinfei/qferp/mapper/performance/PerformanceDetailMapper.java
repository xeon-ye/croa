package com.qinfei.qferp.mapper.performance;

import java.util.List;

import com.qinfei.qferp.entity.performance.PerformanceDetail;

/**
 * 个人评分明细数据库操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0025 15:07；
 */
public interface PerformanceDetailMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(PerformanceDetail record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(PerformanceDetail record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(PerformanceDetail record);

	/**
	 * 批量插入数据；
	 *
	 * @param records：数据对象集合；
	 * @return ：操作影响的记录数；
	 */
	int insertBatch(List<PerformanceDetail> records);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(PerformanceDetail record);

	/**
	 * 更新评分信息；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateScoreData(PerformanceDetail record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(PerformanceDetail record);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param detailId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	PerformanceDetail selectByPrimaryKey(Integer detailId);

	/**
	 * 根据父表ID查询关联的评分明细；
	 * 
	 * @param scoreId：父表ID；
	 * @return ：评分明细集合；
	 */
	List<PerformanceDetail> selectByParentId(Integer scoreId);
}