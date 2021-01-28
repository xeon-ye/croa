package com.qinfei.qferp.flow.command;

import org.flowable.common.engine.impl.db.DbSqlSession;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntity;

/**
 * Flowable历史记录的描述删除；
 *
 * @Author ：Yuan；
 * @Date ：2018/12/13 0013 17:09；
 */
public class DeleteHistoryTaskCommand implements Command {
	// 任务ID；
    private final String taskId;

	public DeleteHistoryTaskCommand(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public Void execute(CommandContext commandContext) {
		// 获取任务执行对象；
		DbSqlSession dbSqlSession = CommandContextUtil.getDbSqlSession();
		HistoricTaskInstanceEntity historicTaskInstance = dbSqlSession.selectById(HistoricTaskInstanceEntity.class, taskId);
		if (historicTaskInstance != null) {
			dbSqlSession.delete(historicTaskInstance);
		}
		return null;
	}
}