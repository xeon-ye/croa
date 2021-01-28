package com.qinfei.qferp.flow.listener;

import org.flowable.task.service.delegate.DelegateTask;

import com.qinfei.qferp.utils.IConst;

/**
 * 出差流程是否借款的网关监听服务；
 * 
 * @Author ：Yuan；
 * @Date ：2018/03/15 0008 15:56；
 */
public class AdministrativeGateHandler extends CommonTaskHandler {
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
			// 判断金额；
			Boolean gateCheck = delegateTask.getVariable("gateCheckB", Boolean.class);
			if (gateCheck == null) {
				gateCheck = false;
			}
			// 判断网关；
			if (gateCheck) {
				state = IConst.STATE_PASS;
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