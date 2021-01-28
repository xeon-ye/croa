package com.qinfei.qferp.flow.listener;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import com.qinfei.qferp.utils.IConst;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

import com.qinfei.qferp.utils.IProcess;

/**
 * 流程开始任务的监听服务；
 * 
 * @Author ：Yuan；
 * @Date ：2018/04/03 0007 9:50；
 */
public class StartEventHandler implements ExecutionListener {
	/**
	 * 部分流程因为网关的关系会在启动后直接抵达终点；
	 * 
	 * @param execution：执行对象；
	 */
	@Override
	public void notify(DelegateExecution execution) {
		// 获取审核的流程类型，类型定义参考接口：com.qinfei.qferp.utils.IProcess；
		Integer process = execution.getVariable("process", Integer.class);
		process = process == null ? -1 : process;

		// 判断身份；
		Boolean gateCheckA = execution.getVariable("gateCheckA", Boolean.class);
		// 避免空指针；
		gateCheckA = gateCheckA == null ? false : gateCheckA;
		// 判断天数；
		Boolean gateCheckB = execution.getVariable("gateCheckB", Boolean.class);
		gateCheckB = gateCheckB == null ? false : gateCheckB;

		switch (process) {
		// 外出申请流程；
		case IProcess.PROCESS_OUTWORK:
			if (gateCheckA && gateCheckB) {
				Administrative leave = new Administrative();
				leave.setId(Integer.parseInt(execution.getVariable("dataId",String.class)));
				leave.setState(IConst.STATE_FINISH);
				IAdministrativeLeaveService leaveService = SpringUtils.getBean("administrativeLeaveService");
				leaveService.processLeava(leave);
			}
			break;
		// 加班申请流程
		case IProcess.PROCESS_WORKOVERTIME:
			if (gateCheckA && gateCheckB) {
				Administrative leave = new Administrative();
				leave.setId(Integer.parseInt(execution.getVariable("dataId",String.class)));
				leave.setState(IConst.STATE_FINISH);
				IAdministrativeLeaveService leaveService = SpringUtils.getBean("administrativeLeaveService");
				leaveService.processLeava(leave);

			}
			break;
		// 流程不存在；
		default:
			break;
		}
	}
}