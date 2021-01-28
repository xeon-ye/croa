package com.qinfei.qferp.flow.listener;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntityManager;

/**
 * 流程跳转命令工具；
 *
 * @Author ：Yuan；
 * @Date ：2018/12/10 0010 18:08；
 */
public class JumpTaskCommand implements Command<Void> {
	// 流程的当前任务ID；
    private final String taskId;
	// 需要跳转的目标节点的元素ID；
    private final String target;

	public JumpTaskCommand(String taskId, String target) {
		this.taskId = taskId;
		this.target = target;
	}

	@Override
	public Void execute(CommandContext commandContext) {
		// 获取当前任务对象；
		TaskEntityManager taskEntityManager = org.flowable.task.service.impl.util.CommandContextUtil.getTaskEntityManager();
		TaskEntity taskEntity = taskEntityManager.findById(taskId);

		// 获取流程的执行对象；
		ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
		ExecutionEntity executionEntity = executionEntityManager.findById(taskEntity.getExecutionId());

		// 获取流程定义对象；
		Process process = ProcessDefinitionUtil.getProcess(executionEntity.getProcessDefinitionId());

		// 找到目标节点；
		FlowElement targetFlowElement = process.getFlowElement(target);
		// 设置为当前节点；
		executionEntity.setCurrentFlowElement(targetFlowElement);

		// 获取流程执行计划；
		FlowableEngineAgenda agenda = CommandContextUtil.getAgenda();
		// 更新执行计划；
		agenda.planContinueProcessInCompensation(executionEntity);
		// 删除原任务；
		taskEntityManager.delete(taskId);
		return null;
	}
}