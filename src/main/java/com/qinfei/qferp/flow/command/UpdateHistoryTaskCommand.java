package com.qinfei.qferp.flow.command;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntity;

/**
 * Flowable历史记录的描述更新；
 * 
 * @Author ：Yuan；
 * @Date ：2018/12/13 0013 17:09；
 */
public class UpdateHistoryTaskCommand implements Command {
	// 任务ID；
    private final String taskId;
	// 审核人姓名；
    private final String owner;
	// 审核人ID
    private final String assignee;
	// 描述内容；
    private final String deleteReason;

	public UpdateHistoryTaskCommand(String taskId, String owner, String assignee, String deleteReason) {
		this.taskId = taskId;
		this.owner = owner;
		this.assignee = assignee;
		this.deleteReason = deleteReason;
	}

	@Override
	public Void execute(CommandContext commandContext) {
		// 获取任务执行对象；
		HistoricTaskInstanceEntity historicTaskInstance = CommandContextUtil.getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
		if (historicTaskInstance != null) {
			// 更新内容；
			historicTaskInstance.setOwner(owner);
			historicTaskInstance.setAssignee(assignee);
			historicTaskInstance.markEnded(deleteReason);
		}
		return null;
	}
}