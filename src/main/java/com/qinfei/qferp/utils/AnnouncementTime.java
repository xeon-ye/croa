package com.qinfei.qferp.utils;



import com.qinfei.qferp.mapper.announcementinform.MediaPassMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AnnouncementTime {
    @Autowired
    private MediaPassMapper mediaPassMapper;

    @Scheduled(cron="0 0 0 0/1 * ?") //每天执行一次
    public void statusCheck() {
        mediaPassMapper. announcementTime();

    }

}
