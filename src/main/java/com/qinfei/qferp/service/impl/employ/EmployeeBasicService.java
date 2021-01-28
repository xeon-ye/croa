package com.qinfei.qferp.service.impl.employ;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployeeBasic;
import com.qinfei.qferp.mapper.employ.EmployeeBasicMapper;
import com.qinfei.qferp.service.employ.IEmployEntryService;
import com.qinfei.qferp.service.employ.IEmployeeBasicService;
import com.qinfei.qferp.utils.IEmployEntry;

/**
 * 员工基础信息业务接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:25；
 */
@Service
public class EmployeeBasicService implements IEmployeeBasicService {
	// 数据库操作对象；
	@Autowired
	private EmployeeBasicMapper basicMapper;
	// 入职申请业务接口；
	@Autowired
	private IEmployEntryService entryService;

	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：员工基础信息对象；
	 * @return ：处理完毕的员工基础信息对象；
	 */
	@Override
	public EmployeeBasic saveOrUpdate(EmployeeBasic record) {
		Integer entryId = record.getEntryId();
		// 确认数据的状态允许编辑；
		if (entryId == null || entryService.checkEnableUpdate(entryId)) {
			// 部分关键信息不允许修改；
			record.setEmpEducationFile(null);
			record.setEmpExperience(null);
			record.setEmpExperienceFile(null);
			record.setTrialBegin(null);
			record.setTrialEnd(null);
			record.setEmpLeaveDate(null);
			record.setEmpRelative(null);

			if (record.getBasId() == null) {
				// 设置创建人信息；
				record.setCreateInfo();
				record.setState(IEmployEntry.ENTRY_PENDING);
				basicMapper.insertSelective(record);
			} else {
				// 设置更新人信息；
				record.setUpdateInfo();
				// 父表主键不允许修改；
				record.setEntryId(null);
				basicMapper.updateByPrimaryKeySelective(record);

				// 重新封装回对象；
				record.setEntryId(entryId);
			}
		}
		return record;
	}

	/**
	 * 根据父表ID更新学历相关的信息；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int updateEducationByParentId(EmployeeBasic record) {
		record.setUpdateInfo();
		return basicMapper.updateEducationByParentId(record);
	}

	/**
	 * 根据父表ID更新工作履历相关的信息；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int updateExperienceByParentId(EmployeeBasic record) {
		record.setUpdateInfo();
		return basicMapper.updateExperienceByParentId(record);
	}

	/**
	 * 根据父表ID更新推荐人相关的信息；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int updateRelativeByParentId(EmployeeBasic record) {
		record.setUpdateInfo();
		return basicMapper.updateRelativeByParentId(record);
	}

	/**
	 * 根据主键ID完善资料；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int completeEntryByParentId(EmployeeBasic record) {
		record.setUpdateInfo();
		return basicMapper.completeEntryByParentId(record);
	}

	/**
	 * 根据父表ID更新入职日期；
	 *
	 * @param record：数据对象；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int updateEmpDateByParentId(EmployeeBasic record) {
		record.setUpdateInfo();
		return basicMapper.updateEmpDateByParentId(record);
	}

	/**
	 * 根据父表ID更新创建人信息；
	 *
	 * @param entryId：父表ID；
	 * @param userId：创建人ID；
	 * @param userName：创建人名称；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int updateCreateInfoByParentId(int entryId, int userId, String userName) {
		EmployeeBasic basic = new EmployeeBasic();
		basic.setUpdateInfo();

		// 前面会清空，此处重新设置；
		basic.setEntryId(entryId);
		basic.setCreateId(userId);
		basic.setCreateName(userName);
		return basicMapper.updateCreateInfoByParentId(basic);
	}
}