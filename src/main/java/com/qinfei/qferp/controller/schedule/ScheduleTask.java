package com.qinfei.qferp.controller.schedule;

import com.qinfei.core.ResponseData;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.schedule.UserSchedule;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.service.impl.media1.MediaAuditServiceImpl;
import com.qinfei.qferp.service.media1.IMediaAuditService;
import com.qinfei.qferp.service.propose.IProposeService;
import com.qinfei.qferp.service.schedule.IUserScheduleService;
import com.qinfei.qferp.service.sys.IDeptService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.flowable.spring.boot.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @CalssName ScheduleTask
 * @Description 日程提醒定时任务
 * @Author xuxiong
 * @Date 2019/9/3 0003 19:48
 * @Version 1.0
 */

@Component
@Slf4j
public class ScheduleTask {
    @Autowired
    private IUserScheduleService userScheduleService;
    @Autowired
    private IProposeService proposeService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IDeptService deptService;
    @Autowired
    private MediaAuditServiceImpl mediaAuditServiceImpl;

    @Scheduled(cron = "0 0 1 * * ?")
    public void updateArtMedia() {
        Long start = System.currentTimeMillis();
        mediaAuditServiceImpl.updateArticle();//每天凌晨一点更新稿件表从媒体为主媒体
        Long end = System.currentTimeMillis();
        System.out.println("**************更新主从媒体任务总耗时："+(end-start)+"ms");

    }
    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨一点获取当天需要提醒的日程
    public void deleteCache() {
        userScheduleService.deleteCache(); //每天凌晨一点删除上一次缓存
    }

    @Scheduled(cron = "0 5 1 * * ?") //每天凌晨一点5分获取当天需要提醒的日程
    public void listAllScheduleByDate() {
        String currentDate = DateUtils.format(new Date(), "yyyy-MM-dd");//每天凌晨获取一次今天待提醒数据，缓存redis
        userScheduleService.listAllScheduleByDate(currentDate); //每天凌晨查询一次，进入缓存
    }

    @Scheduled(cron = "0 */1 * * * ?") //每天分钟执行一次
    public void sendMessage() {
        try {
            String currentDate = DateUtils.format(new Date(), "yyyy-MM-dd");//获取当前日期
            String currentDateTime = DateUtils.format(new Date(), "yyyy-MM-dd HH:mm");//获取当前时间，用于对比时间进行提醒
            List<UserSchedule> allScheduleList = userScheduleService.listAllScheduleByDate(currentDate);
            //判断当前时间是否等于消息提示时间，等于的话就发出消息
            if(CollectionUtils.isNotEmpty(allScheduleList)){
                Set<Integer> set = new HashSet<>(); //保存日程ID，排重，防止重复日程
                List<UserSchedule> currentRemindList = new ArrayList<>(); //当前时间需要提醒的日程
                for(UserSchedule userSchedule : allScheduleList){
                    if(DateUtils.format(userSchedule.getRemindDate(),"yyyy-MM-dd HH:mm").equals(currentDateTime)){
                        if(!set.contains(userSchedule.getId())){
                            set.add(userSchedule.getId());
                            currentRemindList.add(userSchedule);
                        }
                    }
                }
                userScheduleService.sendMessage(currentRemindList);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨一点删除上一次缓存数据
    public void deleteProposeCache() {
       userService.delSuggestUserCache();
    }

    @Scheduled(cron = "0 5 1 * * ?") //每天凌晨一点5分获取当天需要提醒的用户
//    @Scheduled(cron = "0  */1  *  *  *  ?")// 每隔1分钟执行一次
    public void listAllUserForPropose() {
         //每天凌晨查询一次，进入缓存
        List<Dept> list = deptService.listJTAllCompany(null);
        for(Dept dept :list){
            userService.querySuggestHintData(dept.getCode());
        }
    }
}
