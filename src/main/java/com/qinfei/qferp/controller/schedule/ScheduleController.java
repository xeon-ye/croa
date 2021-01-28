package com.qinfei.qferp.controller.schedule;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.schedule.UserSchedule;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.schedule.IUserScheduleService;
import com.qinfei.qferp.utils.AppUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @CalssName ScheduleController
 * @Description 日程接口
 * @Author xuxiong
 * @Date 2019/8/30 0030 10:30
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/schedule")
@Api(description = "日程接口")
public class ScheduleController {
    @Autowired
    private IUserScheduleService userScheduleService;

    @PostMapping("save")
    @ResponseBody
    @ApiOperation(value = "新建日程", notes = "新建日程")
    public ResponseData save(@RequestBody UserSchedule userSchedule){
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002,"请先登录！");
            }
            List<UserSchedule> addUserScheduleList = userScheduleService.save(userSchedule, user.getId());
            List<UserSchedule> oldUserScheduleList = userScheduleService.listAllScheduleByDate(DateUtils.format(new Date(),"yyyy-MM-dd"));
            addUserScheduleList.addAll(oldUserScheduleList);
            userScheduleService.todayCachePut(addUserScheduleList);//刷新今天提醒缓存
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("getCalendar")
    @ResponseBody
    @ApiOperation(value = "根据年月返回日历", notes = "根据年月返回日历")
    public List<Map<String, String>> getDate(@RequestParam("year") Integer year, @RequestParam("month") Integer month){
        User user = AppUtil.getUser();
        if(user == null){
            return new ArrayList<>();
        }
        return  userScheduleService.getCalendar(year,month,user.getId());
    }

    @PostMapping("listScheduleByDate")
    @ResponseBody
    @ApiOperation(value = "查询指定日期的日程列表", notes = "查询指定日期的日程列表")
    public List<UserSchedule> listScheduleByDate(@RequestParam("date") String date){
        User user = AppUtil.getUser();
        if(user == null){
            return new ArrayList<>();
        }
        return  userScheduleService.listScheduleByDate(date,user.getId());
    }
}
