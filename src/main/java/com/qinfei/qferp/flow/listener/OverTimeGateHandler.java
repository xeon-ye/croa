package com.qinfei.qferp.flow.listener;

import com.qinfei.qferp.utils.IConst;
import org.flowable.task.service.delegate.DelegateTask;

/**
 * 加班外出的时间的监听服务；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/15 0007 15:24；
 */
public class OverTimeGateHandler extends CommonTaskHandler {
    /**
     * 根据流程类型获取对应流程需要更新的审核状态；
     *
     * @param delegateTask：任务对象，用于更新流程审核人；
     */
    @Override
    public void handleApproveData(DelegateTask delegateTask) {
        boolean agree = getOpinion(delegateTask);
        int state;
        if (agree) {
            // 判断时间；
            Boolean gateCheckB = delegateTask.getVariable("gateCheckB", Boolean.class);
            gateCheckB = gateCheckB == null ? false : gateCheckB;
            // 判断网关；
            if (gateCheckB) {
                state = IConst.STATE_XZZJ;
            } else {
                state = IConst.STATE_FINISH;
            }
        } else {
            state = IConst.STATE_REJECT;
        }

        // 更新到数据库中；
        delegateTask.setVariable("state", state);
        // 设置审核人；
        setApproveUser(delegateTask, state);
    }
}