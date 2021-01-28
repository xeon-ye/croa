package com.qinfei.qferp.flow.command;

import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.utils.CodeUtil;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.HistoricActivityInstanceEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntity;

import java.util.Calendar;
import java.util.Date;

/**
 * Flowable历史记录的描述新增；
 * 
 * @Author ：Yuan；
 * @Date ：2018/12/13 0013 17:09；
 */
public class InsertHistoryTaskCommand implements Command {
	// 任务ID；
    private final String taskId;
    //节点名称
	private final String name;
	// 审核人姓名；
    private final String owner;
	// 操作人ID
    private final String assignee;
	// 描述内容；
    private final String deleteReason;

	public InsertHistoryTaskCommand(String taskId, String name, String owner, String assignee, String deleteReason) {
		this.taskId = taskId;
		this.name = name;
		this.owner = owner;
		this.assignee = assignee;
		this.deleteReason = deleteReason;
	}

	@Override
	public Void execute(CommandContext commandContext) {
		// 获取任务执行对象；
		HistoricTaskInstanceEntity historicTaskInstance = CommandContextUtil.getDbSqlSession().selectById(HistoricTaskInstanceEntity.class, taskId);
		if (historicTaskInstance != null) {
			//设置内容；
			historicTaskInstance.setId(UUIDUtil.get32UUID());
			historicTaskInstance.setRevision(3);
			historicTaskInstance.setName(name);
			historicTaskInstance.setOwner(owner);
			historicTaskInstance.setAssignee(assignee);
			historicTaskInstance.setDeleteReason(deleteReason);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.SECOND, -5);
			historicTaskInstance.setStartTime(calendar.getTime());
			historicTaskInstance.setEndTime(new Date());
			CommandContextUtil.getDbSqlSession().insert(historicTaskInstance);
		}
		return null;
	}
}