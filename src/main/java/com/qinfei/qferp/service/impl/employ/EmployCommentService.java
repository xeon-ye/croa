package com.qinfei.qferp.service.impl.employ;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployEntry;
import com.qinfei.qferp.entity.employ.EmployEntryComment;
import com.qinfei.qferp.mapper.employ.EmployEntryCommentMapper;
import com.qinfei.qferp.service.employ.IEmployEntryCommentService;
import com.qinfei.qferp.service.employ.IEmployEntryService;
import com.qinfei.qferp.utils.*;

/**
 * 入职申请的审核记录接口实现类；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/13 0013 11:23；
 */
@Service
public class EmployCommentService implements IEmployEntryCommentService {
	// 数据库操作接口；
	@Autowired
	private EmployEntryCommentMapper commentMapper;
	// 入职申请的业务接口；
	@Autowired
	private IEmployEntryService entryService;

	/**
	 * 保存或更新审核信息；
	 *
	 * @param record：审核信息对象；
	 * @return ：处理完毕的对象；
	 */
	@Override
	public EmployEntryComment saveOrUpdate(EmployEntryComment record) {
		Integer entryId = record.getEntryId();
		Integer comType = record.getComType();
		// 父表ID和类型不能为空；
		if (entryId != null && comType != null) {
			Integer comId = record.getComId();
			// 检查数据库，防止出现父表ID和类型相同的数据；
			if (comId == null) {
				comId = selectIdByParentId(record);
				record.setComId(comId);
			}
			if (comId == null) {
				// 设置创建人信息；
				record.setCreateInfo();
				commentMapper.insertSelective(record);
			} else {
				// 检查数据的状态；
				EmployEntry employEntry = entryService.selectStateById(entryId);
				if (employEntry != null) {
					int state = employEntry.getState();
					int complete = employEntry.getEntryComplete();
					int comTypeIntValue = comType.intValue();
					// 仅允许待审核状态下修改，审核中允许修改部长的审批信息；
					boolean enableUpdate = state == IEmployEntry.ENTRY_PENDING || (comTypeIntValue == IEmployComment.COMMENT_LEADER && state == IEmployEntry.ENTRY_APPROVE);
					// 审核状态下不允许修改资料完善的信息；
					if (enableUpdate) {
						record.setComCode(null);
						record.setComEducation(null);
						record.setComResidence(null);
						record.setComExperienceDesc(null);
						record.setComImage(null);
						record.setComReport(null);
						record.setComOther(null);
					}
					// 审核通过的、资料完善的且类型为人事的方可修改；
					boolean enableComplete = state == IEmployEntry.ENTRY_AGREE && complete == IEntryComplete.ENTRY_COMPLETE && comTypeIntValue == IEmployComment.COMMENT_PERSONAL;
					// 审核入库阶段不允许修改审核的内容；
					if (enableComplete) {
						record.setComFigure(null);
						record.setComCommunicate(null);
						record.setComFaith(null);
						record.setComQuality(null);
						record.setComExperience(null);
						record.setComTotalScore(null);
						record.setComAdvice(null);
					}
					// 待审核或审核通过的人事审核信息可以修改；
					if (enableUpdate || enableComplete) {
						// 设置更新人信息；
						record.setUpdateInfo();
						// 父表主键不允许修改；
						record.setEntryId(null);
						// 类型不允许修改；
						record.setComType(null);
						commentMapper.updateByPrimaryKeySelective(record);

						// 重新封装回对象；
						record.setEntryId(entryId);
						record.setComType(comType);
					}
				}
			}
		}
		return record;
	}

	/**
	 * 保存部门的审核信息；
	 *
	 * @param entryId：父表ID；
	 * @param comType：类型；
	 * @param opinion：意见；
	 * @param desc：说明；
	 */
	@Override
	public void saveLeaderComment(int entryId, int comType, int opinion, String desc) {
		EmployEntryComment comment = new EmployEntryComment();
		comment.setEntryId(entryId);
		comment.setComType(IEmployComment.COMMENT_LEADER);
		// 存储审核意见，0为同意，1为拒绝；
		comment.setComFigure(opinion);
		// 存储意见内容；
		comment.setComAdvice(desc);
		// 检查数据的状态，审核中才能新增或修改；
		boolean enableApprove = entryService.checkEnableApprove(entryId);
		if (enableApprove) {
			saveOrUpdate(comment);
		}
	}

	/**
	 * 根据员工ID更新数据的状态；
	 *
	 * @param empId：员工ID；
	 * @param entryId：入职申请ID；
	 * @param operate：操作类型，0为删除，1为离职；
	 */
	@Override
	public void updateStateByEmpId(Integer empId, Integer entryId, int operate) {
		Integer state = null;
		switch (operate) {
		case IEmployee.EMPLOYEE_LEAVE:
			state = IEmployEntry.ENTRY_LEAVED;
			break;
		default:
			break;
		}
		// 过滤不符合要求的参数；
		if (state != null && (empId != null || entryId != null)) {
			Map<String, Object> params = new HashMap<>();
			// 此处有清空状态操作，请注意提前设置；
			params.put("empId", empId);
			params.put("entryId", entryId);
			EntityUtil.setUpdateInfo(params);
			params.put("state", state);
			commentMapper.updateStateByEmpId(params);
		}
	}

	/**
	 * 根据父表ID和类型查询主键ID；
	 *
	 * @param record：数据对象；
	 * @return ：查询的记录ID；
	 */
	@Override
	public Integer selectIdByParentId(EmployEntryComment record) {
		return commentMapper.selectIdByParentId(record);
	}

	/**
	 * 根据父表主键查询单条记录；
	 *
	 * @param record：数据对象；
	 * @return ：查询结果的封装对象；
	 */
	@Override
	public EmployEntryComment selectByParentId(EmployEntryComment record) {
		return commentMapper.selectByParentId(record);
	}

	/**
	 * 根据入职申请ID查询相关的审核信息；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：审核信息集合；
	 */
	@Override
	public List<EmployEntryComment> selectEmployInfo(int entryId) {
		return commentMapper.selectEmployInfo(entryId);
	}
}