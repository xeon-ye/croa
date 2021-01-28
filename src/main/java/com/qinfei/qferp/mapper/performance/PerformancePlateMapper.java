package com.qinfei.qferp.mapper.performance;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.qinfei.qferp.entity.performance.PerformancePlate;

/**
 * 考核细则数据库操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/09 0025 15:07；
 */
public interface PerformancePlateMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(PerformancePlate record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(PerformancePlate record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(PerformancePlate record);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(PerformancePlate record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(PerformancePlate record);

	/**
	 * 更新子节点的级别；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByParentId(PerformancePlate record);

	/**
	 * 查询指定ID的权重分数；
	 * 
	 * @param plateId：主键ID；
	 * @return ：分数统计；
	 */
	float selectScoreById(Integer plateId);

	/**
	 * 查询指定ID下权重分数统计；
	 *
	 * @param record：数据对象；
	 * @return ：分数统计；
	 */
	float selectTotalByParentId(PerformancePlate record);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param plateId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	PerformancePlate selectByPrimaryKey(Integer plateId);

	/**
	 * 根据上级ID查询下属的考核细则；
	 *
	 * @param record：数据对象；
	 * @return ：考核细则集合；
	 */
	List<PerformancePlate> listPlateByParentId(PerformancePlate record);

	/**
	 * 查询指定节点下的子节点数量；
	 * 
	 * @param plateParent：节点ID；
	 * @return ：节点数量；
	 */
	@Select("select count(*) from e_performance_plate where state <> -1 and plate_parent = #{plateParent}")
	long countByParentId(@Param("plateParent") Integer plateParent);
}