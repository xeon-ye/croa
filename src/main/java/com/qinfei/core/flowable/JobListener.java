package com.qinfei.core.flowable;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;

/**
 * @Author: GZW
 * @Description:
 * @date: Created in 2018/11/26
 * @Modified By:
 */
public class JobListener implements FlowableEventListener {
    @Override
    public void onEvent(FlowableEvent flowableEvent) {
        switch ((FlowableEngineEventType) flowableEvent.getType()) {

            case JOB_EXECUTION_SUCCESS:
                System.out.println("A job well done!");
                break;

            case JOB_EXECUTION_FAILURE:
                System.out.println("A job has failed...");
                break;
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
