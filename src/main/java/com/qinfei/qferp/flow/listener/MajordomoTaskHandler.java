package com.qinfei.qferp.flow.listener;

import com.qinfei.qferp.utils.IConst;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * 部门直属领导的审核监听服务；
 *
 * @Author ：HX；
 * @Date ：2019/07/04 0007 9:50；
 */
public class MajordomoTaskHandler extends CommonTaskHandler {
	/**
	 * 根据流程类型获取对应流程需要更新的审核状态；
	 *
	 * @param delegateTask：任务对象，用于更新流程审核人；
	 */
	@Override
	public void handleApproveData(DelegateTask delegateTask) {
		boolean agree = getOpinion(delegateTask);
		int state;
		if (agree) {
			state = IConst.STATE_ZJ;
		} else {
			state = IConst.STATE_REJECT;
		}

		// 更新到数据库中；
		delegateTask.setVariable("state", state);
		// 设置审核人；
		setApproveUser(delegateTask, state);
	}
}