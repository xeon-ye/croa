package com.qinfei.qferp.service.impl.employ;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployeeTransfer;
import com.qinfei.qferp.mapper.employ.EmployeeTransferMapper;
import com.qinfei.qferp.service.employ.IEmployeeTransferService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 员工转正记录业务接口实现类；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 10:39；
 */
@Service
public class EmployeeTransferService extends EmployeeCommonService implements IEmployeeTransferService {
	// 数据库操作工具；
	@Autowired
	private EmployeeTransferMapper transferMapper;

	/**
	 * 保存或更新调岗记录信息；
	 *
	 * @param record：调岗记录对象；
	 * @return ：处理完毕的对象；
	 */
	@Override
	public EmployeeTransfer saveOrUpdate(EmployeeTransfer record) {
		Integer tranId = record.getTranId();
		Integer empId = record.getEmpId();
		// 检查数据库，防止出现父表ID相同的数据；
		if (tranId == null && empId != null) {
			tranId = transferMapper.selectIdByParentId(empId);
			record.setTranId(tranId);
		}
		if (tranId == null) {
			// 设置创建人信息；
			record.setCreateInfo();
			// 保存到数据库中；
			transferMapper.insertSelective(record);
		} else {
			// 设置更新人信息；
			record.setUpdateInfo();
			// 父表主键不允许修改；
			record.setEmpId(null);
			transferMapper.updateByPrimaryKeySelective(record);
			// 重新封装回对象；
			record.setEmpId(empId);
		}
		return record;
	}

	/**
	 * 根据调岗的流程审核结果更新关联的调岗记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	@Override
	public void updateTransferState(int empId, int state) {
		Integer tranId = transferMapper.selectIdByParentId(empId);
		if (tranId != null) {
			EmployeeTransfer transfer = new EmployeeTransfer();
			transfer.setState(state);
			updateState(transfer);
			transfer.setTranId(tranId);
			transferMapper.updateByPrimaryKeySelective(transfer);
		}
	}

	/**
	 * 根据调岗交接的流程审核结果更新关联的调岗记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	@Override
	public void updateConnectTransferState(int empId, int state) {
		Integer tranId = transferMapper.selectIdByParentId(empId);
		if (tranId != null) {
			EmployeeTransfer transfer = new EmployeeTransfer();
			transfer.setState(state);
			updateConnectState(transfer);
			transfer.setTranId(tranId);
			transferMapper.updateByPrimaryKeySelective(transfer);
		}
	}

	/**
	 * 分页查询员工调岗信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工调岗信息集合；
	 */
	@Override
	public PageInfo<Map<String, Object>> selectPageTransfer(Map<String, Object> params, Pageable pageable) {
		// 公司代码过滤；
		params.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(transferMapper.selectPageTransfer(params));
	}

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @return ：调岗记录对象；
	 */
	@Override
	public EmployeeTransfer selectByParentId(int empId) {
		return transferMapper.selectByParentId(empId);
	}
}