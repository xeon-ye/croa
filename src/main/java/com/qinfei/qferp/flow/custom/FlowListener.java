//package com.qinfei.qferp.flow.custom;
//
//import com.qinfei.qferp.flow.custom.entity.Flow;
//import com.qinfei.qferp.flow.custom.entity.Task;
//import com.qinfei.qferp.utils.AppUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//
//public class FlowListener {
//
//    @Autowired
//    FlowMapper flowMapper;
//
//    public void notify(Flow flow) {
//        Task task = flow.getTask();
//    }
//
//    /**
//     * 启动流程
//     *
//     * @param flow
//     */
//    public final void start(Flow flow) {
//        Task task = flow.getTask();
//        task.setCreateDate(new Date());
//        task.setCreator(AppUtil.getUser());
//        flowMapper.insert(flow);
//        this.notify(flow);
//    }
//}
