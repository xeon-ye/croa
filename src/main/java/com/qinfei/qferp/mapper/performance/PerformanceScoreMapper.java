package com.qinfei.qferp.mapper.performance;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.performance.PerformanceScore;

/**
 * 个人评分数据库操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0025 15:07；
 */
public interface PerformanceScoreMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(PerformanceScore record);

	/**
	 * 根据计划id删除评分记录；
	 * 
	 * @param map：评分记录；
	 * @return ：操作影响的记录数；
	 */
	int deleteByProId(Map map);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(PerformanceScore record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(PerformanceScore record);

	/**
	 * 批量插入数据；
	 * 
	 * @param records：数据对象集合；
	 * @return ：操作影响的记录数；
	 */
	int insertBatch(List<PerformanceScore> records);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(PerformanceScore record);

	/**
	 * 更新评分信息；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateScoreData(PerformanceScore record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(PerformanceScore record);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param scoreId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	PerformanceScore selectByPrimaryKey(Integer scoreId);

	/**
	 * 根据权限查询码查询发起流程审核所需的数据；
	 *
	 * @param params：查询参数；
	 * @return ：数据集合；
	 */
	PerformanceScore selectApproveInfoByCode(Map<String, Object> params);

	/**
	 * 分页查询评分信息；
	 *
	 * @param params：查询参数；
	 * @return ：查询的员工信息集合；
	 */
	List<PerformanceScore> selectPagePerformanceScore(Map<String, Object> params);

	/**
	 * 通过计划id获取所有没有提交考核的人的信息
	 * @param proId
	 * @return
	 */
	List<PerformanceScore> listScore(Integer proId);

	/**
	 * 修改绩效计划流程关联待办事项为已办状态
	 * @param map
	 */
	void updateItemData(Map map);
}