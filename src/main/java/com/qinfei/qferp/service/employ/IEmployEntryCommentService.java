package com.qinfei.qferp.service.employ;

import java.util.List;

import com.qinfei.qferp.entity.employ.EmployEntryComment;

/**
 * 入职申请的审核记录接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/12 0012 20:28；
 */
public interface IEmployEntryCommentService {
	/**
	 * 保存或更新审核信息；
	 * 
	 * @param record：审核信息对象；
	 * @return ：处理完毕的对象；
	 */
	EmployEntryComment saveOrUpdate(EmployEntryComment record);

	/**
	 * 保存部门的审核信息；
	 * 
	 * @param entryId：父表ID；
	 * @param comType：类型；
	 * @param opinion：意见；
	 * @param desc：说明；
	 */
	void saveLeaderComment(int entryId, int comType, int opinion, String desc);

	/**
	 * 根据员工ID更新数据的状态；
	 *
	 * @param empId：员工ID；
	 * @param entryId：入职申请ID；
	 * @param operate：操作类型，0为删除，1为离职；
	 */
	void updateStateByEmpId(Integer empId, Integer entryId, int operate);

	/**
	 * 根据父表ID和类型查询主键ID；
	 *
	 * @param record：数据对象；
	 * @return ：查询的记录ID；
	 */
	Integer selectIdByParentId(EmployEntryComment record);

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：查询结果的封装对象；
	 */
	EmployEntryComment selectByParentId(EmployEntryComment record);

	/**
	 * 根据入职申请ID查询相关的审核信息；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：审核信息集合；
	 */
	List<EmployEntryComment> selectEmployInfo(int entryId);
}