package com.qinfei.qferp.service.impl.employ;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployeeConnect;
import com.qinfei.qferp.mapper.employ.EmployeeConnectMapper;
import com.qinfei.qferp.service.employ.IEmployeeConnectService;
import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 员工交接管理接口的实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 10:34；
 */
@Service
public class EmployeeConnectService implements IEmployeeConnectService {
	// 获取数据库操作接口；
	@Autowired
	private EmployeeConnectMapper connectMapper;

	/**
	 * 保存或更新交接记录信息；
	 *
	 * @param record：交接记录对象；
	 * @return ：处理完毕的对象；
	 */
	@Override
	public EmployeeConnect saveOrUpdate(EmployeeConnect record) {
		Integer empId = record.getEmpId();
		Integer conId = record.getConId();
		Integer conType = record.getConType();
		// 检查数据库，防止出现父表ID相同的数据；
		if (conId == null && empId != null && conType != null) {
			Map<String, Object> params = new HashMap<>();
			params.put("empId", empId);
			params.put("conType", conType);
			conId = connectMapper.selectIdByParentId(params);
			record.setConId(conId);
		}
		if (conId == null) {
			// 设置创建人信息；
			record.setCreateInfo();
			// 保存到数据库中；
			connectMapper.insertSelective(record);
		} else {
			// 父表主键不允许修改；
			record.setEmpId(null);
			// 交接类型不允许修改；
			record.setConType(null);
			// 设置更新人信息；
			record.setUpdateInfo();
			connectMapper.updateByPrimaryKeySelective(record);
			// 重新封装回对象；
			record.setEmpId(empId);
			record.setConType(conType);
		}
		return record;
	}

	/**
	 * 根据交接的流程审核结果更新关联的交接记录状态；
	 *
	 * @param empId：父表ID；
	 * @param conType：数据类型，参考com.qinfei.qferp.utils.IEmployConnect；
	 * @param state：状态；
	 */
	@Override
	public void updateConnectState(int empId, int conType, int state) {
		Map<String, Object> params = new HashMap<>();
		params.put("empId", empId);
		params.put("conType", conType);
		Integer formId = connectMapper.selectIdByParentId(params);
		if (formId != null) {
			EmployeeConnect formal = new EmployeeConnect();
			formal.setConId(formId);
			formal.setUpdateInfo();
			if (state == IConst.STATE_FINISH) {
				formal.setState(IEmployeeRecord.STATE_FINISH);
			} else if (state == IConst.STATE_REJECT) {
				formal.setState(IEmployeeRecord.STATE_REJECT);
			} else {
				formal.setState(IEmployeeRecord.STATE_APPROVE);
			}
			// 防止属性被修改；
			if (formal.getState() != null) {
				connectMapper.updateByPrimaryKeySelective(formal);
			}
		}
	}

	/**
	 * 根据交接类型获取对应的流程ID；
	 *
	 * @param conType：交接类型，定义参考com.qinfei.qferp.utils.IEmployConnect；
	 * @return ：流程定义的ID；
	 */
	@Override
	public Integer getHandOverProcessId(int conType) {
		Integer processId = null;
		// 判断调用的流程；
		switch (conType) {
		// 离职；
		case IEmployConnect.CONNECT_LEAVE:
			processId = IProcess.PROCESS_HANDOVER_LEAVE;
			break;
		// 调岗；
		case IEmployConnect.CONNECT_TRANSFER:
			processId = IProcess.PROCESS_HANDOVER_TRANSFER;
			break;
		default:
			break;
		}
		return processId;
	}

	/**
	 * 分页查询员工交接信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工交接信息集合；
	 */
	@Override
	public PageInfo<Map<String, Object>> selectPageConnect(Map<String, Object> params, Pageable pageable) {
		// 公司代码过滤；
		params.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(connectMapper.selectPageConnect(params));
	}

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @return ：交接记录对象；
	 */
	@Override
	public EmployeeConnect selectByParentId(int empId, int conType) {
		Map<String, Object> params = new HashMap<>();
		params.put("empId", empId);
		params.put("conType", conType);
		return connectMapper.selectByParentId(params);
	}

	/**
	 * 根据关联的数据查询单条记录；
	 *
	 * @param params：查询参数；
	 * @return ：查询结果的封装对象；
	 */
	@Override
	public EmployeeConnect selectByRelateData(Map<String, Object> params) {
		return connectMapper.selectByRelateData(params);
	}
}