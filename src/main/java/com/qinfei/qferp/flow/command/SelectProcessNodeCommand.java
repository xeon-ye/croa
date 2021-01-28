package com.qinfei.qferp.flow.command;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yanhonghao on 2019/6/24 9:34.
 */
public class SelectProcessNodeCommand implements Command<Void> {
    // 流程的当前任务ID；
    private final String taskId;

    private List<Map<String, String>> result = new ArrayList<>();

    public SelectProcessNodeCommand(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        FlowableMapper flowableMapper = SpringUtils.getBean(FlowableMapper.class);

        String procInstId = flowableMapper.findProcInstIdByTaskId(taskId);
        if (!StringUtils.isEmpty(procInstId)){
            result = flowableMapper.listHasAuditTaskByProcInstId(procInstId);
        }
        return null;
    }

    public List<Map<String, String>> getResult() {
        return result;
    }
}
