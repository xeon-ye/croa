package com.qinfei.qferp.service.employ;

import java.util.List;

import com.qinfei.qferp.entity.employ.EmployEntryFamily;

/**
 * 入职申请的家庭婚姻信息业务接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/1 0001 11:35；
 */
public interface IEmployEntryFamilyService {
	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：入职申请家庭、婚姻系信息对象；
	 * @return ：处理完毕的入职申请家庭、婚姻系信息对象；
	 */
	EmployEntryFamily saveOrUpdate(EmployEntryFamily record);
	EmployEntryFamily saveOrUpdateAfterInjob(EmployEntryFamily record);

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
	 * @param famId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKey(int famId);

	/**
	 * 根据主键和父表ID删除单条记录；
	 *
	 * @param entryId：父表ID；
	 * @param famId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	int deleteByPrimaryKeyAndParentId(int entryId, int famId);

	/**
	 * 根据入职申请的ID查询家庭成员信息集合；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：家庭成员信息集合；
	 */
	List<EmployEntryFamily> selectByEntryId(int entryId);
}