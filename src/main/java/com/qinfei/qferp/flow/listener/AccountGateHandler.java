package com.qinfei.qferp.flow.listener;

import com.qinfei.qferp.utils.IConst;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * 借款网关判断是否对公账户的监听服务；
 * 
 * @Author ：deng；
 * @Date ：2019/7/19 0007 15:24；
 */
public class AccountGateHandler extends CommonTaskHandler {
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
			// 判断对公账户；
			Boolean gateCheck = delegateTask.getVariable("gateCheckC", Boolean.class);
			if (gateCheck == null) {
				gateCheck = false;
			}
			// 判断网关；
			if (gateCheck) {
				state = IConst.STATE_CWFH;
			} else {
				state = IConst.STATE_FINISH;
			}
		} else {
			state = IConst.STATE_REJECT;
		}

		// 更新到数据库中；
		delegateTask.setVariable("state", state);
		// 设置审核人；
		setApproveUser(delegateTask, state);
	}
}