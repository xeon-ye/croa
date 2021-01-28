package com.qinfei.qferp.flow.listener;

import org.flowable.task.service.delegate.DelegateTask;

import com.qinfei.qferp.utils.IConst;

/**
 * 媒介请款判断金额的监听服务；
 * 
 * @Author ：Yuan；
 * @Date ：2018/12/7 0007 15:24；
 */
public class MediaMoneyGateHandler extends CommonTaskHandler {
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
			// 双重网关判断；
			// 判断身份；
			Boolean gateCheckA = delegateTask.getVariable("gateCheckA", Boolean.class);
			// 避免空指针；
			gateCheckA = gateCheckA == null ? false : gateCheckA;
			// 判断金额；
			Boolean gateCheckB = delegateTask.getVariable("gateCheckB", Boolean.class);
			gateCheckB = gateCheckB == null ? false : gateCheckB;
			Boolean gateCheckC = delegateTask.getVariable("gateCheckC", Boolean.class);
			gateCheckC = gateCheckC == null ? false : gateCheckC;

			boolean gate;
			if (gateCheckA) {
				gate = gateCheckB;
			} else {
				gate = gateCheckC;
			}
			// 判断网关；
			if (gate) {
				state = IConst.STATE_CFO;
			} else {
				state = IConst.STATE_PASS;
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