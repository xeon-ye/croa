package com.qinfei.qferp.flow.listener;

import org.flowable.task.service.delegate.DelegateTask;

import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;

/**
 * 申请人是否最高领导的网关监听服务；
 * 
 * @Author ：Yuan；
 * @Date ：2018/3/15 0007 15:24；
 */
public class PostGateHandler extends CommonTaskHandler {
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
			// 获取网关判定；
			Boolean gateCheck = delegateTask.getVariable("gateCheck", Boolean.class);
			gateCheck = gateCheck == null ? false : gateCheck;
			// 判断网关；
			if (gateCheck) {
				// 获取审核的流程类型，类型定义参考接口：com.qinfei.qferp.utils.IProcess；
				Integer process = delegateTask.getVariable("process", Integer.class);
				process = process == null ? IProcess.PROCESS_VOCATION_ONE : process;
				// 部门最高领导请假一天以内的流程无需行政审核；
				if (process == IProcess.PROCESS_VOCATION_ONE) {
					state = IConst.STATE_PASS;
				} else {
					state = IConst.STATE_XZ;
				}
			} else {
				state = IConst.STATE_BZ;
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