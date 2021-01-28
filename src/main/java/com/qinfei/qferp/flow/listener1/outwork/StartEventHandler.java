package com.qinfei.qferp.flow.listener1.outwork;

import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import com.qinfei.qferp.utils.IConst;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

/**
 * @CalssName StartEventHandler
 * @Description 流程启动处理
 * @Author xuxiong
 * @Date 2019/9/27 0027 16:19
 * @Version 1.0
 */
public class StartEventHandler implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        // 获取审核的流程类型，类型定义参考接口：com.qinfei.qferp.utils.IProcess；
        Integer process = execution.getVariable("process", Integer.class);
        process = process == null ? -1 : process;
        Boolean gateCheckA = execution.getVariable("gateCheckA", Boolean.class);  // 判断身份；
        gateCheckA = gateCheckA == null ? false : gateCheckA; // 避免空指针；
        Boolean gateCheckB = execution.getVariable("gateCheckB", Boolean.class);  // 判断天数；
        gateCheckB = gateCheckB == null ? false : gateCheckB;
        if (process != -1 && gateCheckA && gateCheckB) {
            Administrative leave = new Administrative();
            leave.setId(Integer.parseInt(execution.getVariable("dataId",String.class)));
            leave.setState(IConst.STATE_FINISH);
            IAdministrativeLeaveService leaveService = SpringUtils.getBean("administrativeLeaveService");
            leaveService.processLeava(leave);
        }
    }
}
