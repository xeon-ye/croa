package com.qinfei.qferp.flow.listener;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

class MyExecutionListener implements ExecutionListener {
    public void notify(DelegateExecution execution) {
        String eventName = execution.getEventName();
        //start
        if ("start".equals(eventName)) {
            System.out.println("start=========");
        } else if ("end".equals(eventName)) {
            System.out.println("end=========");
        }
    }
}
