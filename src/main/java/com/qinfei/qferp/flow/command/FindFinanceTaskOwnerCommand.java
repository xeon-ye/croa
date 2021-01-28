package com.qinfei.qferp.flow.command;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntityManager;

import java.util.Objects;

/**
 * Created by yanhonghao on 2019/6/28 15:18.
 */
class FindFinanceTaskOwnerCommand implements Command {
    // 任务ID；
    private final String taskId;
    private String userId;

    public FindFinanceTaskOwnerCommand(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public Object execute(CommandContext commandContext) {
        // 获取当前任务对象；
        TaskEntityManager taskEntityManager = org.flowable.task.service.impl.util.CommandContextUtil.getTaskEntityManager();
        TaskEntity taskEntity = taskEntityManager.findById(taskId);

        if (Objects.isNull(taskEntity)) {
            userId = null;
            return null;
        }
        String executionId = taskEntity.getExecutionId();

        FlowableMapper flowableMapper = SpringUtils.getBean(FlowableMapper.class);
        userId = flowableMapper.findAssigneeByExecutorId(executionId);
        return null;
    }

    public String getUserId() {
        return userId;
    }
}
