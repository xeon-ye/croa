package com.qinfei.qferp.service.impl.employ;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployeeFormal;
import com.qinfei.qferp.mapper.employ.EmployeeFormalMapper;
import com.qinfei.qferp.service.employ.IEmployeeFormalService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IEmployeeRecord;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 员工转正信息业务接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 10:34；
 */
@Service
public class EmployeeFormalService implements IEmployeeFormalService {
	// 获取数据库操作接口；
	@Autowired
	private EmployeeFormalMapper formalMapper;

	/**
	 * 保存或更新转正记录信息；
	 *
	 * @param record：转正记录对象；
	 * @return ：处理完毕的对象；
	 */
	@Override
	public EmployeeFormal saveOrUpdate(EmployeeFormal record) {
		Integer empId = record.getEmpId();
		Integer formId = record.getFormId();
		// 检查数据库，防止出现父表ID相同的数据；
		if (formId == null && empId != null) {
			formId = formalMapper.selectIdByParentId(empId);
			record.setFormId(formId);
		}
		if (formId != null) {
			// 父表主键不允许修改；
			record.setEmpId(null);
			// 设置更新人信息；
			record.setUpdateInfo();
			formalMapper.updateByPrimaryKeySelective(record);
			// 重新封装回对象；
			record.setEmpId(empId);
		} else {
			// 设置创建人信息；
			record.setCreateInfo();
			// 保存到数据库中；
			formalMapper.insertSelective(record);
		}
		return record;
	}

	/**
	 * 根据转正的流程审核结果更新关联的转正记录状态；
	 *
	 * @param empId：父表ID；
	 * @param state：状态；
	 */
	@Override
	public void updateFormalState(int empId, int state) {
		Integer formId = formalMapper.selectIdByParentId(empId);
		if (formId != null) {
			EmployeeFormal formal = new EmployeeFormal();
			formal.setFormId(formId);
			formal.setUpdateInfo();
			if (state == IConst.STATE_FINISH) {
				formal.setState(IEmployeeRecord.STATE_PASS);
			} else if (state == IConst.STATE_REJECT) {
				formal.setState(IEmployeeRecord.STATE_REJECT);
			} else {
				formal.setState(IEmployeeRecord.STATE_APPROVE);
			}
			formalMapper.updateByPrimaryKeySelective(formal);
		}
	}

	/**
	 * 分页查询员工转正信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工转正信息集合；
	 */
	@Override
	public PageInfo<Map<String, Object>> selectPageFormal(Map<String, Object> params, Pageable pageable) {
		// 公司代码过滤；
		params.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(formalMapper.selectPageFormal(params));
	}

	/**
	 * 根据父表ID查询状态；
	 *
	 * @param empId：父表ID；
	 * @return ：查询的记录ID；
	 */
	@Override
	public Integer selectStateByParentId(int empId) {
		return formalMapper.selectStateByParentId(empId);
	}

	/**
	 * 根据主键查询单条记录；
	 *
	 * @param empId：父表主键ID；
	 * @return ：转正记录对象；
	 */
	@Override
	public EmployeeFormal selectByParentId(int empId) {
		return formalMapper.selectByParentId(empId);
	}
}