package com.qinfei.qferp.service.impl.employ;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployEntryFamily;
import com.qinfei.qferp.mapper.employ.EmployEntryFamilyMapper;
import com.qinfei.qferp.service.employ.IEmployEntryFamilyService;
import com.qinfei.qferp.service.employ.IEmployEntryService;
import com.qinfei.qferp.utils.IEmployEntry;

/**
 * 入职申请的家庭婚姻信息业务接口实现类；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/1 0001 11:36；
 */
@Service
public class EmployEntryFamilyService implements IEmployEntryFamilyService {
	// 数据接口；
	@Autowired
	private EmployEntryFamilyMapper familyMapper;
	// 入职申请业务接口；
	@Autowired
	private IEmployEntryService entryService;

	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：入职申请家庭、婚姻系信息对象；
	 * @return ：处理完毕的入职申请家庭、婚姻系信息对象；
	 */
	@Override
	public EmployEntryFamily saveOrUpdate(EmployEntryFamily record) {
		Integer entryId = record.getEntryId();
		// 确认数据的状态允许编辑；
		if (entryService.checkEnableUpdate(entryId)) {
			if (record.getFamId() == null) {
				// 设置创建人信息；
				record.setCreateInfo();
				record.setState(IEmployEntry.ENTRY_PENDING);
				familyMapper.insertSelective(record);
			} else {
				// 设置更新人信息；
				record.setUpdateInfo();
				// 父表主键不允许修改；
				record.setEntryId(null);
				familyMapper.updateByPrimaryKeySelective(record);
				// 重新封装回对象；
				record.setEntryId(entryId);
			}
		}
		return record;
	}

	@Override
	public EmployEntryFamily saveOrUpdateAfterInjob(EmployEntryFamily record) {
		Integer entryId = record.getEntryId();
		if (record.getFamId() == null) {
			// 设置创建人信息；
			record.setCreateInfo();
			record.setState(IEmployEntry.ENTRY_PENDING);
			familyMapper.insertSelective(record);
		} else {
			// 设置更新人信息；
			record.setUpdateInfo();
			// 父表主键不允许修改；
			record.setEntryId(null);
			familyMapper.updateByPrimaryKeySelective(record);
			// 重新封装回对象；
			record.setEntryId(entryId);
		}
		return record;
	}

	/**
	 * 更新创建人信息；
	 *
	 * @param entryId：主键ID；
	 * @param userId：创建人ID；
	 * @param userName：创建人名称；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int updateCreateInfoByParentId(int entryId, int userId, String userName) {
		EmployEntryFamily family = new EmployEntryFamily();
		family.setUpdateInfo();

		// 前面会清空，此处重新设置；
		family.setCreateId(userId);
		family.setCreateName(userName);
		family.setEntryId(entryId);
		return familyMapper.updateCreateInfoByParentId(family);
	}

	/**
	 * 根据主键删除单条记录；
	 *
	 * @param famId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int deleteByPrimaryKey(int famId) {
		EmployEntryFamily family = new EmployEntryFamily();
		family.setFamId(famId);
		family.setUpdateInfo();
		return familyMapper.deleteByPrimaryKey(family);
	}

	/**
	 * 根据主键和父表ID删除单条记录；
	 *
	 * @param entryId：父表ID；
	 * @param famId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int deleteByPrimaryKeyAndParentId(int entryId, int famId) {
		EmployEntryFamily family = new EmployEntryFamily();
		family.setFamId(famId);
		family.setEntryId(entryId);
		family.setUpdateInfo();
		return familyMapper.deleteByPrimaryKeyAndParentId(family);
	}

	/**
	 * 根据入职申请的ID查询家庭成员信息集合；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：家庭成员信息集合；
	 */
	@Override
	public List<EmployEntryFamily> selectByEntryId(int entryId) {
		return familyMapper.selectByEntryId(entryId);
	}
}