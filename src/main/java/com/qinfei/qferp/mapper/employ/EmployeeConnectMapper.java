package com.qinfei.qferp.mapper.employ;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.employ.EmployeeConnect;
import com.qinfei.qferp.entity.employ.EmployeeTransfer;
import org.apache.ibatis.annotations.Param;

/**
 * 员工交接清单数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployeeConnectMapper {
	/**
	 * 根据主键删除单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(EmployeeConnect record);

	/**
	 * 插入单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insert(EmployeeConnect record);

	/**
	 * 插入单条记录，插入前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int insertSelective(EmployeeConnect record);

	/**
	 * 更新单条记录，更新前会判断对应属性是否为空；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKeySelective(EmployeeConnect record);

	/**
	 * 更新单条记录，全字段插入不做检查；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	int updateByPrimaryKey(EmployeeConnect record);

	/**
	 * 根据父表ID和类型查询主键ID；
	 *
	 * @param params：查询参数；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentId(Map<String, Object> params);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param conId：主键ID；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeConnect selectByPrimaryKey(Integer conId);

	/**
	 * 根据关联的数据查询单条记录；
	 *
	 * @param params：查询参数；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeConnect selectByRelateData(Map<String, Object> params);

	List<EmployeeConnect> listByEmpId(@Param("empIdList") List<Integer> empIdList);

	List<EmployeeConnect> listByIds(@Param("ids") List<Integer> ids);

	/**
	 * 根据父表ID和类型查询单条记录；
	 *
	 * @param params：查询参数；
	 * @return ：交接记录对象；
	 */
	EmployeeConnect selectByParentId(Map<String, Object> params);

	/**
	 * 分页查询员工交接信息；
	 *
	 * @param params：查询参数；
	 * @return ：查询的员工交接信息集合；
	 */
	List<Map<String, Object>> selectPageConnect(Map<String, Object> params);
}