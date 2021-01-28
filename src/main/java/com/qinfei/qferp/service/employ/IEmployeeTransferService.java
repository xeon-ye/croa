package com.qinfei.qferp.service.employ;

import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.qferp.entity.employ.EmployeeTransfer;
import com.github.pagehelper.PageInfo;

/**
 * 员工调岗记录业务接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 10:23；
 */
public interface IEmployeeTransferService {
	/**
	 * 保存或更新调岗记录信息；
	 *
	 * @param record：调岗记录对象；
	 * @return ：处理完毕的对象；
	 */
	EmployeeTransfer saveOrUpdate(EmployeeTransfer record);

	/**
	 * 根据调岗的流程审核结果更新关联的调岗记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	void updateTransferState(int empId, int state);

	/**
	 * 根据调岗交接的流程审核结果更新关联的调岗记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	void updateConnectTransferState(int empId, int state);

	/**
	 * 分页查询员工调岗信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工调岗信息集合；
	 */
	PageInfo<Map<String, Object>> selectPageTransfer(Map<String, Object> params, Pageable pageable);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @return ：调岗记录对象；
	 */
	EmployeeTransfer selectByParentId(int empId);
}