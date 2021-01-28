package com.qinfei.qferp.flow.listener;

import org.flowable.task.service.delegate.DelegateTask;

import com.qinfei.qferp.utils.IConst;

/**
 * 业务员确认的监听服务；
 * 
 * @Author ：Yuan；
 * @Date ：2018/12/8 0008 15:49；
 */
public class BusinessTaskHandler extends CommonTaskHandler {
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
			state = IConst.STATE_YW;
		} else {
			state = IConst.STATE_REJECT;
		}

		// 更新到数据库中；
		delegateTask.setVariable("state", state);
		// 设置审核人；
		setApproveUser(delegateTask, state);
	}
}