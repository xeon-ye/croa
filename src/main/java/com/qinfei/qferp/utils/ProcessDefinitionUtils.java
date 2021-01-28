package com.qinfei.qferp.utils;

import com.qinfei.core.utils.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.de.odysseus.el.ExpressionFactoryImpl;
import org.flowable.common.engine.impl.de.odysseus.el.util.SimpleContext;
import org.flowable.common.engine.impl.javax.el.ExpressionFactory;
import org.flowable.common.engine.impl.javax.el.ValueExpression;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessDefinitionUtils {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    HistoryService historyService;

    /**
     * 获取end节点
     *
     * @param processDefId
     * @return FlowElement
     */
    public FlowElement findEndFlowElement(String processDefId) {
        Process process = repositoryService.getBpmnModel(processDefId).getMainProcess();
        Collection<FlowElement> list = process.getFlowElements();
        for (FlowElement f : list) {
            if (f instanceof EndEvent) {
                return f;
            }
        }
        return null;
    }

    /**
     * 获取指定节点的节点信息
     *
     * @param processDefId
     * @param flowElementId
     * @return FlowElement
     */
    public Activity findFlowElementById(String processDefId, String flowElementId) {
        Process process = repositoryService.getBpmnModel(processDefId).getMainProcess();
        return (Activity) process.getFlowElement(flowElementId);
    }

    /**
     * 通过taskId获取流程的所有节点
     * @return
     */
    public List<Map<String, Object>> getAllTask(String dataId,int process){
        // 定义集合用来保存数据；
        List<Map<String, Object>> datas = new ArrayList<>();
        // 获取查询对象；
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();
        // 增加查询条件；
        query = query.processVariableValueEquals("process", process).processVariableValueEquals("dataId", dataId);
        // 获得查询结果；
        List<HistoricTaskInstance> list = query.orderByTaskCreateTime().asc().list();
        Map<String, Object> map;
        String user;
        // 遍历获取需要的数据；
        // 审核备注；
        String desc;
        StringBuilder descContent;
        for (HistoricTaskInstance historicTaskInstance : list) {
            // 没有审核人的数据可能在审核中，或者是已驳回的数据，跳过；
            user = historicTaskInstance.getOwner();
            // 老流程使用的Assignee保存审核人姓名；
            if (StringUtils.isEmpty(user)) {
                user = historicTaskInstance.getAssignee();
            }
            if (!StringUtils.isEmpty(user)) {
                map = new HashMap<>();
                map.put("name", historicTaskInstance.getName());
                map.put("user", user);
                map.put("taskId",historicTaskInstance.getId());
                datas.add(map);
            }
        }
        return datas;
    }


    private Object getValue(List<HistoricVariableInstance> hvis, String exp, Class<?> clazz) {
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        for (HistoricVariableInstance entry : hvis) {
            context.setVariable(entry.getVariableName(), factory.createValueExpression(entry.getValue(), Object.class));
        }
        ValueExpression e = factory.createValueExpression(context, exp, clazz);
        return e.getValue(context);
    }

    public String getValue(List<HistoricVariableInstance> hvis, String exp) {
        return (String) getValue(hvis, exp, String.class);
    }
}
