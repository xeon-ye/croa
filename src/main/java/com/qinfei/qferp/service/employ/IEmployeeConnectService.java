package com.qinfei.qferp.service.employ;

import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.qferp.entity.employ.EmployeeConnect;
import com.github.pagehelper.PageInfo;

/**
 * 员工交接管理的业务接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/28 0028 19:26；
 */
public interface IEmployeeConnectService {
	/**
	 * 保存或更新交接记录信息；
	 *
	 * @param record：交接记录对象；
	 * @return ：处理完毕的对象；
	 */
	EmployeeConnect saveOrUpdate(EmployeeConnect record);

	/**
	 * 根据交接的流程审核结果更新关联的交接记录状态；
	 *
	 * @param empId：父表ID；
	 * @param conType：数据类型，参考com.qinfei.qferp.utils.IEmployConnect；
	 * @param state：状态；
	 */
	void updateConnectState(int empId, int conType, int state);

	/**
	 * 根据交接类型获取对应的流程ID；
	 * 
	 * @param conType：交接类型，定义参考com.qinfei.qferp.utils.IEmployConnect；
	 * @return ：流程定义的ID；
	 */
	Integer getHandOverProcessId(int conType);

	/**
	 * 分页查询员工交接信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工交接信息集合；
	 */
	PageInfo<Map<String, Object>> selectPageConnect(Map<String, Object> params, Pageable pageable);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @param conType：数据类型，参考com.qinfei.qferp.utils.IEmployConnect；
	 * @return ：交接记录对象；
	 */
	EmployeeConnect selectByParentId(int empId, int conType);

	/**
	 * 根据关联的数据查询单条记录；
	 *
	 * @param params：查询参数；
	 * @return ：查询结果的封装对象；
	 */
	EmployeeConnect selectByRelateData(Map<String, Object> params);
}