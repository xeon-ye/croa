package com.qinfei.qferp.flow.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * 老板审核
 */
@Slf4j
public class BossTaskHandler implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        String taskUser = delegateTask.getVariable("taskUser", String.class);
        Integer money = delegateTask.getVariable("money", Integer.class);
        String nextUser = delegateTask.getVariable("nextUser", String.class);
        delegateTask.setVariable("nextUser", nextUser);
        delegateTask.addCandidateUser(nextUser);
        delegateTask.setAssignee(nextUser);
        log.debug(taskUser);
        log.debug(money + "");
    }
}
