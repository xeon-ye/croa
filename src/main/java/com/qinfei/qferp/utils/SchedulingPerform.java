package com.qinfei.qferp.utils;

import com.qinfei.qferp.service.impl.performance.PerformanceProportionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class SchedulingPerform {
    @Autowired
    private PerformanceProportionService performanceProportionService;
    //绩效考核时间开始后就启动流程
    @Scheduled(cron="0 0 0 0/1 * ?") //每天执行一次
    public void starformance() {
        performanceProportionService.starformance();
    }

    //定时发送绩效提交通知
    @Scheduled(cron="0 0 0 0/1 * ?") //每天执行一次
    public void sendMessage() {
        performanceProportionService.sendMessage();
    }



}
