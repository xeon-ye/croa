package com.qinfei.core.serivce.impl;

import com.qinfei.core.serivce.IWorkFlowService;

/**
 * 工作流服务类
 *
 * @author by Gzw
 */
// @Service
// @Slf4j
// @Transactional
class WorkFlowService implements IWorkFlowService {
	// @Autowired//RuntimeService 执行管理，包括启动、推进、删除流程实例等操作
	// 是activiti的流程执行服务类。可以从这个服务类中获取很多关于流程执行相关的信息。
	// protected RuntimeService runtimeService;
	// @Autowired//任务管理 是activiti的任务服务类。可以从这个类中获取任务的信息
	// protected TaskService taskService;
	// @Autowired//是Activiti的仓库服务类。所谓的仓库指流程定义文档的两个文件：bpmn文件和流程图片。
	// protected RepositoryService repositoryService;
	// @Autowired
	// protected ProcessEngine processEngine;
	// @Autowired
	// ManagementService managementService;
	//
	// /**
	// * 部署流程
	// *
	// * @param resource
	// * @param name
	// */
	// public void deploymentProcess(String resource, String name) {
	// //流程部署
	// Deployment deployment = repositoryService.createDeployment()
	// .addClasspathResource(resource)
	// .name(name)
	// .deploy();
	// //增加事件监听
	// runtimeService.addEventListener(new JobListener());
	// }
	//
	// /**
	// * 部署流程
	// *
	// * @param resource
	// */
	// public void deploymentProcess(String resource) {
	// //流程部署
	// Deployment deployment = repositoryService.createDeployment()
	// .addClasspathResource(resource)
	// .deploy();
	// //增加事件监听
	// runtimeService.addEventListener(new JobListener());
	// }
	//
	// /**
	// * 添加流程
	// *
	// * @param userId 指定审核人Id
	// * @param processId 流程id
	// * @param param 参数
	// * @return taskId 返回任务ID
	// */
	// @Override
	// public String addExpense(Integer userId, String processId, Map<String,
	// Object> param) {
	// param.put("taskUser", userId);
	// ProcessInstance processInstance =
	// runtimeService.startProcessInstanceByKey(processId, param);
	// return processInstance.getId();
	// }
	//
	// /**
	// * 添加流程
	// *
	// * @param processId 流程id
	// * @param param 参数
	// * @return ProcessInstance 流程实例
	// */
	// @Override
	// public ProcessInstance addExpense(String processId, Map<String, Object>
	// param) {
	// String userId = param.get("userId").toString();//给他发消息
	//
	// ProcessInstance processInstance =
	// runtimeService.startProcessInstanceByKey(processId, param);
	// return processInstance;
	// }
	//
	// /**
	// * 根据用户id 获取审核管理列表
	// *
	// * @param userId
	// * @return
	// */
	// @Override
	// public List<Map<String, Object>> list(String userId) {
	// TaskQuery taskQuery = taskService.createTaskQuery();
	//
	// if (taskQuery == null) return null;
	// TaskQuery taskQuery1 = taskQuery.taskAssignee(userId);
	// if (taskQuery1 == null) return null;
	// List<Task> tasks = taskQuery1.orderByTaskCreateTime().desc().list();
	//
	// List<Map<String, Object>> list = new ArrayList<>();
	// for (Task task : tasks) {
	// Map<String, Object> map = new HashMap<>();
	// String taskId = task.getId();
	// map.put("taskId", taskId);
	// //获取设置的变量
	// Map<String, Object> vars = taskService.getVariables(taskId);
	// map.put("taskDefinitionKey", task.getTaskDefinitionKey());
	// map.put("taskName", task.getName());
	// Map<String, Object> process = task.getProcessVariables();
	// Map<String, Object> local = task.getTaskLocalVariables();
	// map.putAll(process);
	// map.putAll(local);
	// map.putAll(vars);
	// list.add(map);
	// }
	// return list;
	// }
	//
	// /**
	// * 审核通过
	// *
	// * @param taskId 流程ID
	// * @param nextUserId 下级审核人ID
	// * @param param 审核参数
	// * @return
	// */
	// @Override
	// public String apply(String taskId, String nextUserId, Map<String, Object>
	// param) {
	// Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
	// if (task == null) {
	// return "流程不存在";
	// }
	// if (param == null)
	// param = new HashMap<>();
	// //通过审核
	// param.put("outcome", "通过");
	// if (!StringUtils.isEmpty(nextUserId))
	// param.put("nextUserId", nextUserId);
	// param.put("lastAuditTime", new Date());
	// User user = AppUtil.getUser();
	// param.put("lastAuditId", user.getId());
	// param.put("lastAuditName", user.getName());
	// taskService.complete(taskId, param);
	// return "processed ok!";
	// }
	//
	// /**
	// * 审核通过
	// *
	// * @param taskId 流程ID
	// * @param nextUserId 下级审核人ID
	// * @return
	// */
	// @Override
	// public String apply(String taskId, String nextUserId) {
	// return this.apply(taskId, nextUserId, null);
	// }
	//
	// /**
	// * 审核通过
	// *
	// * @param taskId 流程ID
	// * @return
	// */
	// @Override
	// public String apply(String taskId) {
	// return this.apply(taskId, null, null);
	// }
	//
	// /**
	// * 驳回
	// *
	// * @param taskId 流程ID
	// * @return
	// */
	// @Override
	// public String reject(String taskId, Map<String, Object> map) {
	// map.put("outcome", "驳回");
	// taskService.complete(taskId, map);
	// return "reject";
	// }
	//
	//
	// /**
	// * 获取end节点
	// *
	// * @param processDefId
	// * @return FlowElement
	// */
	// public FlowElement findEndFlowElement(String processDefId) {
	// Process process =
	// repositoryService.getBpmnModel(processDefId).getMainProcess();
	// Collection<FlowElement> list = process.getFlowElements();
	// for (FlowElement f : list) {
	// if (f instanceof EndEvent) {
	// return f;
	// }
	// }
	// return null;
	// }
	//
	// /**
	// * 获取指定节点的节点信息
	// *
	// * @param processDefId
	// * @param flowElementId
	// * @return FlowElement
	// */
	// public Activity findFlowElementById(String processDefId, String
	// flowElementId) {
	// Process process =
	// repositoryService.getBpmnModel(processDefId).getMainProcess();
	// return (Activity) process.getFlowElement(flowElementId);
	// }
	//
	//// /**
	//// * 添加审核意见和修改流程状态
	//// *
	//// * @param taskId
	//// * @param instanceId
	//// */
	//// protected void addCommentAndUpdateProcessStatus(String taskId, String
	// instanceId) {
	//// //兼容处理
	//// Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
	////
	//// //2.修改流程实例的状态
	//// ExtendHisprocinst extendHisprocinst = new
	// ExtendHisprocinst(baseProcessVo.getProcessInstanceId(),
	// baseProcessVo.getProcessStatusEnum().toString());
	//// extendHisprocinstService.updateStatusByProcessInstanceId(extendHisprocinst);
	//// //3.TODO 生成索引
	//// }
	//
	//// /**
	//// * 添加审核意见
	//// *
	//// * @param flowCommentVo
	//// */
	//// private void addFlowComment(FlowCommentVo flowCommentVo) {
	//// FlowCommentCmd cmd = new FlowCommentCmd(flowCommentVo.getTaskId(),
	// flowCommentVo.getUserId(),
	//// flowCommentVo.getProcessInstanceId(), flowCommentVo.getType(),
	// flowCommentVo.getMessage());
	//// managementService.executeCommand(cmd);
	//// }
	//
	// public Map backToStep(String taskId, Map<String, Object> map) {
	// TaskEntity taskEntity = (TaskEntity)
	// taskService.createTaskQuery().taskId(taskId).singleResult();
	// if (taskEntity != null) {
	// Activity distActivity =
	// findFlowElementById(taskEntity.getProcessDefinitionId(),
	// taskEntity.getTaskDefinitionKey());
	// if (taskEntity != null && distActivity != null) {
	// //1. 判断该节点上一个节点是不是并行网关节点
	// List<SequenceFlow> incomingFlows = distActivity.getIncomingFlows();
	// for (SequenceFlow sequenceFlow : incomingFlows) {
	// FlowElement upNode = sequenceFlow.getSourceFlowElement();
	// if (upNode != null && (upNode instanceof ParallelGateway || upNode instanceof
	// InclusiveGateway)) {
	//// new ReturnVo<>(ReturnCode.FAIL, "并行节点无法驳回，请选择其他节点!");
	//
	// }
	// }
	// CommandContextUtil.getCommentEntityManager().deleteCommentsByProcessInstanceId(taskEntity.getProcessInstanceId());
	//
	// //6. 驳回到disk节点
	// Activity currActivity =
	// findFlowElementById(taskEntity.getProcessDefinitionId(),
	// taskEntity.getTaskDefinitionKey());
	// //6.1 如果当前节点是多实例节点 删除当前多实例 如果目标节点不是多实例我们就创建一个孩子实例
	// boolean flag = false;
	// if (currActivity.getBehavior() instanceof MultiInstanceActivityBehavior) {
	// ExecutionEntity executionEntity = (ExecutionEntity)
	// runtimeService.createExecutionQuery().executionId(taskEntity.getExecutionId()).singleResult();
	// managementService.executeCommand(new
	// DeleteMultiInstanceExecutionCmd(executionEntity.getParentId(), false));
	// flag = true;
	// }
	// //6.2 处理并行网关的多实例
	// List<Execution> executions =
	// runtimeService.createExecutionQuery().parentId(taskEntity.getProcessInstanceId()).list();
	// if (executions.size() > 1) {
	// executions.forEach(execution -> {
	// ExecutionEntity e = (ExecutionEntity) execution;
	// managementService.executeCommand(new DeleteChildExecutionCmd(e));
	// });
	// flag = true;
	// }
	// if (flag) {
	// ExecutionEntity parentExecutionEntity = (ExecutionEntity)
	// runtimeService.createExecutionQuery().executionId(taskEntity.getProcessInstanceId()).singleResult();
	// managementService.executeCommand(new
	// AddChildExecutionCmd(parentExecutionEntity));
	// }
	// managementService.executeCommand(new
	// JumpActivityCmd(taskEntity.getProcessInstanceId(), distActivity.getId()));
	// //TODO 7. 处理加签的数据0
	// }
	// }
	// return null;
	// }
	//
	// public class DeleteChildExecutionCmd implements Command<Void> {
	// private ExecutionEntity child;
	//
	// public DeleteChildExecutionCmd(ExecutionEntity child) {
	// this.child = child;
	// }
	//
	// @Override
	// public Void execute(CommandContext commandContext) {
	// ExecutionEntityManager executionEntityManager =
	// CommandContextUtil.getExecutionEntityManager(commandContext);
	// executionEntityManager.delete(child, true);
	// return null;
	// }
	// }
	//
	// public class AddChildExecutionCmd implements Command<Void> {
	//
	// private ExecutionEntity parentExecutionEntity;
	//
	// public AddChildExecutionCmd(ExecutionEntity parentExecutionEntity) {
	// this.parentExecutionEntity = parentExecutionEntity;
	// }
	//
	// @Override
	// public Void execute(CommandContext commandContext) {
	// ExecutionEntityManager executionEntityManager =
	// CommandContextUtil.getExecutionEntityManager(commandContext);
	// executionEntityManager.createChildExecution(parentExecutionEntity);
	// return null;
	// }
	// }
	//
	// public class DeleteTaskCmd implements Command<Void> {
	//
	// private String processInstanceId;
	//
	// public DeleteTaskCmd(String processInstanceId) {
	// this.processInstanceId = processInstanceId;
	// }
	//
	// @Override
	// public Void execute(CommandContext commandContext) {
	// CommandContextUtil.getCommentEntityManager().deleteCommentsByProcessInstanceId(processInstanceId);
	// return null;
	// }
	// }
	//
	// /**
	// * JumpActivityCmd 执行跳转
	// */
	// public class JumpActivityCmd implements Command<Void> {
	//
	// private String target;
	// private String processInstanceId;
	//
	// public JumpActivityCmd(String processInstanceId, String target) {
	// this.processInstanceId = processInstanceId;
	// this.target = target;
	// }
	//
	// @Override
	// public Void execute(CommandContext commandContext) {
	// ExecutionEntityManager executionEntityManager =
	// CommandContextUtil.getExecutionEntityManager(commandContext);
	// List<ExecutionEntity> executionEntities =
	// executionEntityManager.findChildExecutionsByParentExecutionId(processInstanceId);
	// Process process =
	// ProcessDefinitionUtil.getProcess(executionEntities.get(0).getProcessDefinitionId());
	// FlowNode targetFlowElement = (FlowNode) process.getFlowElement(target);
	// FlowableEngineAgenda agenda = CommandContextUtil.getAgenda();
	// executionEntities.forEach(execution -> {
	// execution.setCurrentFlowElement(targetFlowElement);
	// agenda.planContinueProcessInCompensation(execution);
	// });
	// return null;
	// }
	//
	// }
	//
	// /**
	// * 查询历史任务列表
	// */
	// @Override
	// public void findHistoryProcessVariables() {
	// List<HistoricVariableInstance> list = processEngine.getHistoryService()//
	// .createHistoricVariableInstanceQuery() // 创建一个历史的流程变量查询对象
	// .variableName("请假天数").list();
	// if (null != list && list.size() > 0) {
	// for (HistoricVariableInstance hvi : list) {
	// System.out.println(hvi.getId() + " " + hvi.getProcessInstanceId() + " " +
	// hvi.getVariableName() + " "
	// + hvi.getVariableTypeName() + " " + hvi.getValue());
	// System.out.println("###################################");
	// }
	// }
	// }
}
