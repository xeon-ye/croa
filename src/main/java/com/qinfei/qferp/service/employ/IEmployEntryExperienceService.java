package com.qinfei.qferp.service.employ;

import java.util.List;

import com.qinfei.qferp.entity.employ.EmployEntryExperience;

/**
 * 入职申请的工作经历业务接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/1 0001 14:22；
 */
public interface IEmployEntryExperienceService {
	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：入职申请工作经历信息对象；
	 * @return ：处理完毕的入职申请工作经历信息对象；
	 */
	EmployEntryExperience saveOrUpdate(EmployEntryExperience record);
	EmployEntryExperience saveOrUpdateInJob(EmployEntryExperience record);
	/**
	 * 根据父表ID更新创建人信息；
	 *
	 * @param entryId：父表ID；
	 * @param userId：创建人ID；
	 * @param userName：创建人名称；
	 * @return ：操作影响的记录数；
	 */
	int updateCreateInfoByParentId(int entryId, int userId, String userName);

	/**
	 * 根据主键删除单条记录；
	 *
	 * @param expId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(int expId);

	/**
	 * 根据主键和父表ID删除单条记录；
	 *
	 * @param entryId：父表ID；
	 * @param expId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKeyAndParentId(int entryId, int expId);

	/**
	 * 根据入职申请的ID查询工作经历信息集合；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：工作经历信息集合；
	 */
	List<EmployEntryExperience> selectByEntryId(int entryId);
}