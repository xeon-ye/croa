package com.qinfei.qferp.service.impl.employ;

import java.util.Map;

import com.qinfei.qferp.entity.employ.EmployEntry;
import com.qinfei.qferp.mapper.employ.EmployEntryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployeeHire;
import com.qinfei.qferp.mapper.employ.EmployeeHireMapper;
import com.qinfei.qferp.service.employ.IEmployeeHireService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IEmployEntry;
import com.qinfei.qferp.utils.IEmployeeRecord;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 员工的录用记录接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/19 0012 20:28；
 */
@Service
public class EmployeeHireService implements IEmployeeHireService {
	// 数据库操作接口；
	@Autowired
	private EmployeeHireMapper hireMapper;
	@Autowired
	private EmployEntryMapper employEntryMapper;

	/**
	 * 保存或更新录用记录信息；
	 *
	 * @param record：录用记录对象；
	 * @return ：处理完毕的对象；
	 */
	@Override
	public EmployeeHire saveOrUpdate(EmployeeHire record) {
		Integer entryId = record.getEntryId();
		Integer hireId = record.getHireId();
		// 检查数据库，防止出现父表ID相同的数据；
		if (hireId == null && entryId != null) {
			hireId = hireMapper.selectIdByParentId(entryId);
			record.setHireId(hireId);
		}
		if (hireId == null) {
			// 设置创建人信息；
			record.setCreateInfo();
			// 保存到数据库中；
			hireMapper.insertSelective(record);
		} else {
			Integer empId = record.getEmpId();
			// 父表主键不允许修改；
			record.setEntryId(null);
			record.setEmpId(null);
			// 设置更新人信息；
			record.setUpdateInfo();
			hireMapper.updateByPrimaryKeySelective(record);

			// 重新封装回对象；
			record.setEntryId(entryId);
			record.setEmpId(empId);
		}
		//同步一级部门、二级部门、职位到e_entry表
		EmployEntry employEntry = new EmployEntry();
		employEntry.setEntryId(entryId);
		employEntry.setEntryFirstDept(record.getEntryFirstDept());
		employEntry.setEntryDept(record.getEmpDept());
		employEntry.setEntryProfession(record.getEmpProfession());
		employEntry.setEntryState(record.getEntryState());
		employEntryMapper.updateByPrimaryKeySelective(employEntry);
		return record;
	}

	/**
	 * 根据入职申请的流程审核结果更新关联的录用记录状态；
	 *
	 * @param entryId：父表ID；
	 * @param state：状态；
	 */
	@Override
	public void updateHireState(int entryId, int state) {
		Integer hireId = hireMapper.selectIdByParentId(entryId);
		if (hireId != null) {
			EmployeeHire hire = new EmployeeHire();
			hire.setHireId(hireId);
			hire.setUpdateInfo();
			if (state == IEmployEntry.ENTRY_PENDING) {
				hire.setState(IEmployeeRecord.STATE_REJECT);
			} else if (state == IEmployEntry.ENTRY_AGREE) {
				hire.setState(IEmployeeRecord.STATE_PASS);
			} else {
				hire.setState(IEmployeeRecord.STATE_APPROVE);
			}
			hireMapper.updateByPrimaryKeySelective(hire);
		}
	}

	/**
	 * 更新工号信息；
	 *
	 * @param entryId：父表ID；
	 * @param empId：员工ID；
	 * @param empNum：工号；
	 */
	@Override
	public void updateHireNum(int entryId, int empId, String empNum) {
		Integer hireId = selectIdByParentId(entryId);
		if (hireId != null) {
			EmployeeHire hire = new EmployeeHire();
			hire.setHireId(hireId);
			hire.setEmpId(empId);
			hire.setEmpNum(empNum);
			hire.setUpdateInfo();
			hireMapper.updateByPrimaryKeySelective(hire);
		}
	}

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询的记录ID；
	 */
	@Override
	public Integer selectIdByParentId(int entryId) {
		return hireMapper.selectIdByParentId(entryId);
	}

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询结果的封装对象；
	 */
	@Override
	public EmployeeHire selectByParentId(int entryId) {
		return hireMapper.selectByParentId(entryId);
	}

	/**
	 * 分页查询员工录用信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工录用信息集合；
	 */
	@Override
	public PageInfo<Map<String, Object>> selectPageHire(Map<String, Object> params, Pageable pageable) {
		// 公司代码过滤；
		params.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(hireMapper.selectPageHire(params));
	}

}