package com.qinfei.qferp.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

/**
 * 工作流示例
 *
 * @author Gong.zhiwei
 */
@Slf4j
@Controller
@RequestMapping(value = "expense")
class ExpenseController {
	@Autowired // RuntimeService 执行管理，包括启动、推进、删除流程实例等操作
				// 是activiti的流程执行服务类。可以从这个服务类中获取很多关于流程执行相关的信息。
	private RuntimeService runtimeService;
	@Autowired // 任务管理 是activiti的任务服务类。可以从这个类中获取任务的信息
	private TaskService taskService;
	@Autowired // 是Activiti的仓库服务类。所谓的仓库指流程定义文档的两个文件：bpmn文件和流程图片。
	private RepositoryService repositoryService;
	@Autowired
	private ProcessEngine processEngine;

	/**
	 * 添加报销
	 *
	 * @param userId
	 *            用户Id
	 * @param money
	 *            报销金额
	 * @param descption
	 *            描述;
	 */
	@RequestMapping(value = "add")
	@ResponseBody
	@Log(opType = OperateType.ADD, module = "报销流程", note = "添加报销")
	public String addExpense(String userId, Integer money, String descption) {
		// 启动流程
		HashMap<String, Object> map = new HashMap<>();
		map.put("taskUser", userId);
		map.put("money", money);
		map.put("descption", descption);
		// .addClasspathResource("processes/flowable.bpmn20.xml")
		// repositoryService.createDeployment().addClasspathResource("processes/testProcess.bpmn20.xml").name("测试").deploy();//部署任务
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("test", map);
		// ProcessInstance processInstance =
		// runtimeService.startProcessInstanceByKey("startEvent1", map);
		return "提交成功.流程Id为：" + processInstance.getId();
	}

	/**
	 * 获取审核管理列表
	 */
	@RequestMapping(value = "/list")
	@ResponseBody
	@Log(opType = OperateType.QUERY, module = "报销流程", note = "获取审核管理列表")
	public List<Map<String, Object>> list(String userId) {
		TaskQuery taskQuery = taskService.createTaskQuery();

		if (taskQuery == null)
			return null;
		TaskQuery taskQuery1 = taskQuery.taskAssignee(userId);
		if (taskQuery1 == null)
			return null;
		List<Task> tasks = taskQuery1.orderByTaskCreateTime().desc().list();

		List<Map<String, Object>> list = new ArrayList<>();
		for (Task task : tasks) {
			Map<String, Object> map = new HashMap<>();
			map.put("taskId", task.getId());
			Map<String, Object> vars = taskService.getVariables(task.getId());
			map.put("taskDefinitionKey", task.getTaskDefinitionKey());
			map.put("taskName", task.getName());
			Map<String, Object> process = task.getProcessVariables();

			Map<String, Object> local = task.getTaskLocalVariables();

			list.add(map);
		}
		return list;
	}

	/**
	 * 批准
	 *
	 * @param taskId
	 *            任务ID
	 * @param nextUserId
	 *            下一个审核人ID
	 */
	@RequestMapping(value = "apply")
	@ResponseBody
	@Log(opType = OperateType.UPDATE, module = "报销流程", note = "批准")
	public String apply(String taskId, String nextUserId) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			return "流程不存在";
		}
		// taskService.addCandidateUser("","1001");
		// 设置审核任务的下一個执行人
		// taskService.claim(task.getId(), AppUtil.getUser().getId()+"");
		// 通过审核
		HashMap<String, Object> map = new HashMap<>();
		map.put("outcome", "通过");
		map.put("nextUser", nextUserId);
		// taskService.setAssignee(taskId,nextUserId);
		taskService.complete(taskId, map);
		// List<IdentityLink> list = taskService.getIdentityLinksForTask(taskId);
		// list.forEach(identityLink -> System.out.println(identityLink));
		long count = taskService.createTaskQuery().taskAssignee(nextUserId).count();

		return "processed ok!";
	}

	/**
	 * 拒绝
	 */
	@ResponseBody
	@RequestMapping(value = "reject")
	@Log(opType = OperateType.UPDATE, module = "报销流程", note = "拒绝")
	public String reject(String taskId) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("outcome", "驳回");
		taskService.complete(taskId, map);
		return "reject";
	}

	@ResponseBody
	@RequestMapping(value = "history")
	public void findHistoryProcessVariables() {
		List<HistoricVariableInstance> list = processEngine.getHistoryService()//
				.createHistoricVariableInstanceQuery() // 创建一个历史的流程变量查询对象
				.variableName("请假天数").list();
		if (null != list && list.size() > 0) {
			for (HistoricVariableInstance hvi : list) {
				System.out.println(hvi.getId() + " " + hvi.getProcessInstanceId() + " " + hvi.getVariableName() + "  " + hvi.getVariableTypeName() + "  " + hvi.getValue());
				System.out.println("###################################");
			}
		}
	}

	/**
	 * 生成流程图
	 *
	 * @param processId
	 *            任务ID
	 */
	@RequestMapping(value = "processDiagram")
//	@Log(opType = OperateType.UPDATE, module = "报销流程", note = "生成流程图")
	public void genProcessDiagram(HttpServletResponse response, String processId) throws Exception {
		ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

		// 流程走完的不显示图
		if (pi == null) {
			return;
		}
		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
		// 使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
		String InstanceId = task.getProcessInstanceId();
		List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(InstanceId).list();

		// 得到正在执行的Activity的Id
		List<String> activityIds = new ArrayList<>();
		List<String> flows = new ArrayList<>();
		for (Execution exe : executions) {
			List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
			activityIds.addAll(ids);
		}

		// 获取流程图
		BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
		ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
		ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
		InputStream in = diagramGenerator.generateDiagram(bpmnModel, "PNG", activityIds, flows, "宋体", "宋体", "宋体", null, 1.0, true);
		// InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png",
		// activityIds, flows, engconf.getActivityFontName(),
		// engconf.getLabelFontName(), engconf.getAnnotationFontName(),
		// engconf.getClassLoader(), 1.0);
		OutputStream out = null;
		byte[] buf = new byte[1024];
		int legth = 0;
		try {
			out = response.getOutputStream();
			while ((legth = in.read(buf)) != -1) {
				out.write(buf, 0, legth);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

}
