package com.qinfei.qferp.service.impl.employ;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployeeLeave;
import com.qinfei.qferp.mapper.employ.EmployeeLeaveMapper;
import com.qinfei.qferp.service.employ.IEmployeeLeaveService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 员工离职信息业务接口实现类；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 10:36；
 */
@Service
public class EmployeeLeaveService extends EmployeeCommonService implements IEmployeeLeaveService {
	// 获取数据操作接口；
	@Autowired
	private EmployeeLeaveMapper leaveMapper;

	/**
	 * 保存或更新离职记录信息；
	 *
	 * @param record：离职记录对象；
	 * @return ：处理完毕的对象；
	 */
	@Override
	public EmployeeLeave saveOrUpdate(EmployeeLeave record) {
		Integer empId = record.getEmpId();
		Integer leaveId = record.getLeaveId();
		// 检查数据库，防止出现父表ID相同的数据；
		if (leaveId == null && empId != null) {
			leaveId = leaveMapper.selectIdByParentId(empId);
			record.setLeaveId(leaveId);
		}
		if (leaveId == null) {
			// 设置创建人信息；
			record.setCreateInfo();
			// 保存到数据库中；
			leaveMapper.insertSelective(record);
		} else {
			// 父表主键不允许修改；
			record.setEmpId(null);
			// 设置更新人信息；
			record.setUpdateInfo();
			leaveMapper.updateByPrimaryKeySelective(record);
			// 重新封装回对象；
			record.setEmpId(empId);
		}
		return record;
	}

	/**
	 * 根据离职的流程审核结果更新关联的离职记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	@Override
	public void updateLeaveState(int empId, int state) {
		Integer leaveId = leaveMapper.selectIdByParentId(empId);
		if (leaveId != null) {
			EmployeeLeave leave = new EmployeeLeave();
			leave.setState(state);
			updateState(leave);
			leave.setLeaveId(leaveId);
			leaveMapper.updateByPrimaryKeySelective(leave);
		}
	}

	/**
	 * 根据离职交接的流程审核结果更新关联的离职记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	@Override
	public void updateConnectLeaveState(int empId, int state) {
		Integer leaveId = leaveMapper.selectIdByParentId(empId);
		if (leaveId != null) {
			EmployeeLeave leave = new EmployeeLeave();
			leave.setState(state);
			updateConnectState(leave);
			leave.setLeaveId(leaveId);
			leaveMapper.updateByPrimaryKeySelective(leave);
		}
	}

	/**
	 * 分页查询员工离职信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工离职信息集合；
	 */
	@Override
	public PageInfo<Map<String, Object>> selectPageLeave(Map<String, Object> params, Pageable pageable) {
		// 公司代码过滤；
		params.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(leaveMapper.selectPageLeave(params));
	}

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @return ：离职记录对象；
	 */
	@Override
	public EmployeeLeave selectByParentId(int empId) {
		return leaveMapper.selectByParentId(empId);
	}
}