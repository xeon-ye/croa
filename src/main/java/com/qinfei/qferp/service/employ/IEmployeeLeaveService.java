package com.qinfei.qferp.service.employ;

import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.qferp.entity.employ.EmployeeLeave;
import com.github.pagehelper.PageInfo;

/**
 * 员工离职信息业务接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 10:28；
 */
public interface IEmployeeLeaveService {
	/**
	 * 保存或更新离职记录信息；
	 *
	 * @param record：离职记录对象；
	 * @return ：处理完毕的对象；
	 */
	EmployeeLeave saveOrUpdate(EmployeeLeave record);

	/**
	 * 根据离职的流程审核结果更新关联的离职记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	void updateLeaveState(int empId, int state);

	/**
	 * 根据离职交接的流程审核结果更新关联的离职记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	void updateConnectLeaveState(int empId, int state);

	/**
	 * 分页查询员工离职信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工离职信息集合；
	 */
	PageInfo<Map<String, Object>> selectPageLeave(Map<String, Object> params, Pageable pageable);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @return ：离职记录对象；
	 */
	EmployeeLeave selectByParentId(int empId);
}