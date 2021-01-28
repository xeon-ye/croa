package com.qinfei.qferp.flow.listener1.overtime;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import com.qinfei.qferp.utils.IConst;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

/**
 * @CalssName EndEventHandler
 * @Description 流程结束处理
 * @Author tsf
 */
public class EndEventHandler implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        // 获取审核的流程类型，类型定义参考接口：com.qinfei.qferp.utils.IProcess；
//        Integer process = execution.getVariable("process", Integer.class);
        Boolean gateCheckA = execution.getVariable("gateCheckA", Boolean.class);  // 判断身份；
        gateCheckA = gateCheckA == null ? false : gateCheckA; // 避免空指针；
        Boolean gateCheckB = execution.getVariable("gateCheckB", Boolean.class);  // 判断天数；
        gateCheckB = gateCheckB == null ? false : gateCheckB;
        String taskId = execution.getProcessInstanceId();
        String id= execution.getVariable("dataId", String.class);
        //是部门领导直接跳过流程审批，进行状态修改
        if(gateCheckA){
            //领导加班小于24小时
            if(!gateCheckB){
                Administrative leave = new Administrative();
                leave.setId(Integer.parseInt(id));
                leave.setState(IConst.STATE_FINISH);
                IAdministrativeLeaveService leaveService = SpringUtils.getBean("administrativeLeaveService");
                leaveService.processLeava(leave);
            }
        }
    }
}
