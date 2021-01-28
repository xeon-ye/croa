package com.qinfei.qferp.mapper.employ;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.employ.EmployeeTrajectory;

/**
 * 员工轨迹数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployeeTrajectoryMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployeeTrajectory record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployeeTrajectory record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployeeTrajectory record);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployeeTrajectory record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployeeTrajectory record);

	/**
	 * 批量更新员工轨迹关联的员工ID和姓名；
	 * 
	 * @param params：查询参数和更新内容；
	 * @return ：操作影响的记录数；
	 */
	int updateByIds(Map<String, Object> params);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param trajId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeTrajectory selectByPrimaryKey(Integer trajId);

	/**
	 * 录用流程发起时，员工没有关联的数据，因此保存的是入职申请ID，正式入库后则更新为员工ID，并将员工编号插入进来；
	 * 
	 * @param entryId：入职申请ID；
	 * @return ：主键ID集合；
	 */
	List<Map<String, Integer>> selectIdsByParentId(Integer entryId);

	/**
	 * 分页查询员工轨迹信息；
	 *
	 * @param params：查询参数；
	 * @return ：查询的员工轨迹信息集合；
	 */
	List<Map<String, Object>> selectPageTrajectory(Map<String, Object> params);
}