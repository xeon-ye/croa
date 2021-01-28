package com.qinfei.qferp.service.employ;

import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.qferp.entity.employ.EmployeeFormal;
import com.github.pagehelper.PageInfo;

/**
 * 员工转正信息业务接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 10:32；
 */
public interface IEmployeeFormalService {
	/**
	 * 保存或更新转正记录信息；
	 *
	 * @param record：转正记录对象；
	 * @return ：处理完毕的对象；
	 */
	EmployeeFormal saveOrUpdate(EmployeeFormal record);

	/**
	 * 根据转正的流程审核结果更新关联的转正记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	void updateFormalState(int empId, int state);

	/**
	 * 分页查询员工转正信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工转正信息集合；
	 */
	PageInfo<Map<String, Object>> selectPageFormal(Map<String, Object> params, Pageable pageable);

	/**
	 * 根据父表ID查询状态；
	 *
	 * @param empId：父表ID；
	 * @return ：查询的记录ID；
	 */
	Integer selectStateByParentId(int empId);

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @return ：转正记录对象；
	 */
	EmployeeFormal selectByParentId(int empId);
}