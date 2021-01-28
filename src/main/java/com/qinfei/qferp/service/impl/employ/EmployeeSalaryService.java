package com.qinfei.qferp.service.impl.employ;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployeeSalary;
import com.qinfei.qferp.mapper.employ.EmployeeSalaryMapper;
import com.qinfei.qferp.service.employ.IEmployeeSalaryService;
import com.qinfei.qferp.utils.IEmployEntry;
import com.qinfei.qferp.utils.IEmployee;

/**
 * 员工的薪资管理接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/20 0012 20:28；
 */
@Service
public class EmployeeSalaryService implements IEmployeeSalaryService {
	// 数据库操作接口；
	@Autowired
	private EmployeeSalaryMapper salaryMapper;

	/**
	 * 保存或更新薪资信息；
	 *
	 * @param record：薪资对象；
	 * @return ：处理完毕的对象；
	 */
	@Override
	public EmployeeSalary saveOrUpdate(EmployeeSalary record) {
		Integer entryId = record.getEntryId();
		Integer empId = record.getEmpId();
		Integer salId = record.getSalId();
		// 检查数据库，防止出现父表ID相同的数据；
		if (salId == null && (entryId != null || empId != null)) {
			Map<String, Object> params = new HashMap<>();
			params.put("entryId", entryId);
			params.put("empId", empId);
			salId = salaryMapper.selectIdByParentId(params);
			record.setSalId(salId);
		}
		if (salId == null) {
			// 设置创建人信息；
			record.setCreateInfo();
			salaryMapper.insertSelective(record);
		} else {
			// 设置更新人信息；
			record.setUpdateInfo();
			// 父表主键不允许修改；
			record.setEntryId(null);
			record.setEmpId(null);
			salaryMapper.updateByPrimaryKeySelective(record);

			// 重新封装回对象；
			record.setEntryId(entryId);
			record.setEmpId(empId);
		}
		return record;
	}

	/**
	 * 更新工号信息；
	 *
	 * @param entryId：父表ID；
	 * @param empId：员工ID；
	 * @param empNum：工号；
	 */
	@Override
	public void updateSalaryNum(int entryId, int empId, String empNum) {
		Integer salId = selectIdByParentEntryId(entryId);
		if (salId != null) {
			EmployeeSalary hire = new EmployeeSalary();
			hire.setSalId(salId);
			hire.setEmpId(empId);
			hire.setEmpNum(empNum);
			hire.setUpdateInfo();
			salaryMapper.updateByPrimaryKeySelective(hire);
		}
	}

	/**
	 * 根据员工ID更新数据的状态；
	 *
	 * @param empId：员工ID；
	 * @param operate：操作类型，0为删除，1为离职；
	 * @return ：操作结果提示信息，0为异常，1为操作成功；
	 */
	@Override
	public void updateStateByEmpId(int empId, int operate) {
		Integer state = null;
		switch (operate) {
		case IEmployee.EMPLOYEE_LEAVE:
			state = IEmployEntry.ENTRY_LEAVED;
			break;
		default:
			break;
		}
		// 过滤不符合要求的参数；
		if (state != null) {
			EmployeeSalary salary = new EmployeeSalary();
			// 此处有清空状态操作，请注意提前设置；
			salary.setUpdateInfo();
			salary.setEmpId(empId);
			salary.setState(state);
			salaryMapper.updateStateByEmpId(salary);
		}
	}

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询的记录ID；
	 */
	@Override
	public Integer selectIdByParentEntryId(int entryId) {
		Map<String, Object> params = new HashMap<>();
		params.put("entryId", entryId);
		return salaryMapper.selectIdByParentId(params);
	}

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param entryId：父表ID；
	 * @return ：查询结果的封装对象；
	 */
	@Override
	public EmployeeSalary selectByParentEntryId(int entryId) {
		Map<String, Object> params = new HashMap<>();
		params.put("entryId", entryId);
		return salaryMapper.selectByParentId(params);
	}

	/**
	 * 根据父表ID查询主键ID；
	 *
	 * @param empId：父表ID；
	 * @return ：查询的记录ID；
	 */
	@Override
	public Integer selectIdByParentEmpId(int empId) {
		Map<String, Object> params = new HashMap<>();
		params.put("empId", empId);
		return salaryMapper.selectIdByParentId(params);
	}

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param empId：父表ID；
	 * @return ：查询结果的封装对象；
	 */
	@Override
	public EmployeeSalary selectByParentEmpId(int empId) {
		Map<String, Object> params = new HashMap<>();
		params.put("empId", empId);
		return salaryMapper.selectByParentId(params);
	}

}