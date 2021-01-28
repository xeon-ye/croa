package com.qinfei.qferp.service.employ;

import java.util.List;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.employ.EmployEntryEducation;
import com.qinfei.qferp.entity.employ.EmployEntryExperience;

/**
 * 入职申请的教育培训经历业务接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/1 0001 14:22；
 */
public interface IEmployEntryEducationService {
	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：入职申请教育培训经历信息对象；
	 * @return ：处理完毕的入职申请教育培训经历信息对象；
	 */
	EmployEntryEducation saveOrUpdate(EmployEntryEducation record);
	EmployEntryEducation saveOrUpdateAfterInjob(EmployEntryEducation record);

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
	 * 设置指定的学历为最高学历；
	 *
	 * @param entryId：父表ID；
	 * @param eduId：主键ID；
	 * @param eduCollege：学院、培训机构名称；
	 * @param eduMajor：专业名称；
	 * @return ：操作影响的记录数；
	 */
	int setEducationHighest(int entryId, int eduId, String eduCollege, String eduMajor);

	/**
	 * 根据主键删除单条记录；
	 *
	 * @param eduId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(int eduId);

	/**
	 * 根据主键和父表ID删除单条记录；
	 *
	 * @param entryId：父表ID；
	 * @param eduId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKeyAndParentId(int entryId, int eduId);

	/**
	 * 根据入职申请的ID查询教育经历信息集合；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：教育信息集合；
	 */
	List<EmployEntryEducation> selectByEntryId(int entryId);
}