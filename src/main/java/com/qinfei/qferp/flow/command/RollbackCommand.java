package com.qinfei.qferp.flow.command;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.utils.AppUtil;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntityManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanhonghao on 2019/6/24 9:34.
 */
public class RollbackCommand implements Command<Void> {
    // 流程的当前任务ID；
    private final String taskId;
    private final String target;
    //设置参数
    private Map<String, Object> param;

    public RollbackCommand(String id, String target, Map<String, Object> param) {
        this.taskId = id;
        this.target = target;
        this.param = param;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        RuntimeService runtimeService = SpringUtils.getBean(RuntimeService.class);
        FlowableMapper flowableMapper = SpringUtils.getBean(FlowableMapper.class);

        // 获取当前任务对象；
        TaskEntityManager taskEntityManager = org.flowable.task.service.impl.util.CommandContextUtil.getTaskEntityManager();
        TaskEntity taskEntity = taskEntityManager.findById(taskId);

        // 获取流程的执行对象；
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
        String executionId = taskEntity.getExecutionId();
        ExecutionEntity executionEntity = executionEntityManager.findById(executionId);
        String processInstanceId = executionEntity.getProcessInstanceId();
        String procInstId = flowableMapper.findProcInstIdByTaskId(taskId);

        String currentName = flowableMapper.findFlowKeyByTaskId(taskId);
        String previousName = StringUtils.isEmpty(target) ? flowableMapper.findPreviousKey(executionId, currentName) : target;
        if (StringUtils.isEmpty(previousName)) throw new QinFeiException(50000, "上一节点是发起者，请选择审核驳回");

        List<String> ids = new ArrayList<>();
        Map<String, String> taskNameMap = new HashMap<>();
        for (Map<String, String> actMap : flowableMapper.listByProcInstId(procInstId)) {
            String actId = actMap.get("id");
            String processType = actMap.get("type");
            taskNameMap.put(processType, actMap.get("name"));

//            flowableMapper.deleteByProcInstIdAndTaskDefKey(procInstId, processType);
            ids.add(actId);
            //如果遍历到当前节点 跳出循环
            if (StringUtils.equalsIgnoreCase(previousName, processType)) {
                break;
            }
        }
        //删除流程图中被驳回的节点和网关
        flowableMapper.deleteActByIds(ids);

        //增加跳转指定人功能
        if(param != null && param.get("nextUser") != null){
            Integer userId = Integer.parseInt(String.valueOf(param.get("nextUser")));
            if(userId != null){
                UserMapper userMapper = SpringUtils.getBean(UserMapper.class);
                User user = userMapper.getById(userId);
                param.put("nextUser", user.getId());
                param.put("nextUserName", user.getName());
                param.put("nextUserDept", user.getDeptId());
            }
        }
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(processInstanceId)
                .processVariables(param)
                .moveActivityIdTo(currentName, previousName)
                .changeState();
        //设置撤回/驳回节点名称
        String targetName = taskNameMap != null && StringUtils.isNotEmpty(taskNameMap.get(previousName)) ? taskNameMap.get(previousName) : previousName;
        param.put("targetName", targetName);
        return null;
    }
}