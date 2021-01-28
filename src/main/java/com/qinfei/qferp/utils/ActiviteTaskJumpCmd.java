package com.qinfei.qferp.utils;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.impl.cmd.NeedsActiveTaskCmd;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.engine.runtime.Execution;
import org.flowable.identitylink.service.IdentityLinkService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntityManager;

/**
 * @description: 自由跳转流程
 */
class ActiviteTaskJumpCmd extends NeedsActiveTaskCmd<Boolean> {
    //执行实例id
    private final String processId;
    //目标节点
    private final String targetNodeId;
    /** 
     * @param taskId 当前任务id   
     * @param processId 实例id   
     * @param targetNodeId 目标节点   
     * @description   
     * @date 2019-05-17 21:06   
     */
    public ActiviteTaskJumpCmd(String taskId,String processId,String targetNodeId) {
        super(taskId);
        this.processId = processId;
        this.targetNodeId = targetNodeId;}
    /** 
     * @param commandContext
     * @param task
     * @return Boolean
     * @throws 
     * @description 
     * @date 2019-05-17 21:00   
     */
        @Override
        protected Boolean execute(CommandContext commandContext, TaskEntity task) {
            ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
            //Execution execution = CommandContextUtil.getProcessEngineConfiguration().getRuntimeService().createProcessInstanceQuery().processInstanceId(processId).singleResult();
            IdentityLinkService identityLinkService = CommandContextUtil.getIdentityLinkService();
            TaskEntityManager taskEntityManager = CommandContextUtil.getTaskServiceConfiguration().getTaskEntityManager();
            //获取任务实例
            TaskEntity taskEntity = taskEntityManager.findById(taskId);
            //拼装查询条件
            ExecutionEntity ee = executionEntityManager.findById(taskEntity.getExecutionId());
            //获取流程实例
            Process process = ProcessDefinitionUtil.getProcess(ee.getProcessDefinitionId());
            //获取节点信息
            FlowElement targetFlowElement = process.getFlowElement(targetNodeId);
            ee.setCurrentFlowElement(targetFlowElement);
            FlowableEngineAgenda agenda = CommandContextUtil.getAgenda();
            agenda.planContinueProcessInCompensation(ee);

            identityLinkService.deleteIdentityLinksByTaskId(taskId);
            taskEntityManager.delete(taskId);
            return true;
        }



    //调用此命令类如下
    // taskId 为当前节点的
    // taskId processInstId 为当前实例
    // idtargetNodeId 为目标节点的id
    // Boolean bool =managementService.executeCommand(new ActiviteTaskJumpCmd(taskId, processInstId, targetNodeId));



}

