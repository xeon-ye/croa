package com.qinfei.qferp.service.impl.crm;

import com.qinfei.qferp.service.crm.ICrmCompanyUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CrmCompanyScheduleService {
    @Autowired
    private ICrmCompanyUserService crmCompanyUserService;

    @Scheduled(cron = "0 0 7 * * *", zone = "GMT+08:00")
    public void publicCompanyUser(){
        Long start = System.currentTimeMillis();
        crmCompanyUserService.doWithPublic();
        Long end = System.currentTimeMillis();
        System.out.println("****公海客户定时任务总耗时："+(end-start)+"ms");
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "GMT+08:00")
    public void evalCompanyUser(){
        Long start = System.currentTimeMillis();
        crmCompanyUserService.doWithEval();
        Long end = System.currentTimeMillis();
        System.out.println("****客户考核定时任务总耗时："+(end-start)+"ms");
    }
}
