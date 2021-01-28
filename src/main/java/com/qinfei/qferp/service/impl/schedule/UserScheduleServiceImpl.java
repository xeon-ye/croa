package com.qinfei.qferp.service.impl.schedule;

import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.schedule.UserSchedule;
import com.qinfei.qferp.entity.schedule.UserScheduleRelate;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.schedule.UserScheduleMapper;
import com.qinfei.qferp.mapper.schedule.UserScheduleRelateMapper;
import com.qinfei.qferp.service.schedule.IUserScheduleService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @CalssName UserScheduleServiceImpl
 * @Description 用户日程服务接口
 * @Author xuxiong
 * @Date 2019/9/2 0002 18:32
 * @Version 1.0
 */
@Service
@Slf4j
public class UserScheduleServiceImpl implements IUserScheduleService {
    @Autowired
    private UserScheduleMapper userScheduleMapper;
    @Autowired
    private UserScheduleRelateMapper userScheduleRelateMapper;
    @Autowired
    private IMessageService messageService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public List<UserSchedule> save(UserSchedule userSchedule, int userId) {
       try{
           validateAddSchedule(userSchedule); //校验日程
           Date currentDate = new Date();
           userSchedule.setCreateDate(currentDate);
           userSchedule.setCreateId(userId);
           userSchedule.setUpdateDate(currentDate);
           userSchedule.setUpdateId(userId);
           userScheduleMapper.save(userSchedule);
           List<UserSchedule> addScheduleList = new ArrayList<>(); //添加新增的日程到今日提醒的缓存中
           handleAddSchedule(userSchedule, userId, currentDate, addScheduleList); //计算日程提醒时间
           return addScheduleList;
       }catch (QinFeiException e){
           throw new QinFeiException(e.getCode(),e.getMessage());
       }catch (Exception e){
           e.printStackTrace();
           throw new QinFeiException(1002, "很抱歉，新建日程出错啦！");
       }
    }

    @Override
    public List<UserSchedule> todayCachePut(List<UserSchedule> userScheduleList) {
        return userScheduleList;
    }

    @Override
    public List<UserSchedule> todayCachePut() {
        return userScheduleMapper.listScheduleByDate(DateUtils.format(new Date(),"yyyy-MM-dd"), null);
    }

    //校验新建日程参数
    private void validateAddSchedule(UserSchedule userSchedule){
        if(StringUtils.isEmpty(userSchedule.getName())){
            throw new QinFeiException(1002,"请输入日程标题！");
        }
        if(userSchedule.getStartDate() == null || userSchedule.getEndDate() == null){
            throw new QinFeiException(1002,"请选择日期！");
        }
        if(userSchedule.getStartDate().compareTo(userSchedule.getEndDate()) > 0){
            throw new QinFeiException(1002,"开始日期不能大于结束日期！");
        }
    }

    //日程提醒日期处理
    private void handleAddSchedule(UserSchedule userSchedule, int userId, Date currentDate,List<UserSchedule> addScheduleList){
        String currentDateStr = DateUtils.format(currentDate, "yyyy-MM-dd");//获取当前日期
//        if(userSchedule.getRepeatFlag() != 0 && userSchedule.getRemindFlag() != 0){
            int calendarRepeatType = 0; //重复：0-无
            int calendarRepeatNum = 0; //重复周期数
            int calendarRemindType = 0; //提醒：0-无
            int calendarRemindNum = 0; //提前时间数
            //重复：0-无、1-每天、2-每周、3-每两周、4-每月、5-每年
            switch (userSchedule.getRepeatFlag()){
                case 1: calendarRepeatType = Calendar.DAY_OF_MONTH; break;
                case 2:
                case 3: calendarRepeatType = Calendar.WEEK_OF_MONTH; break;
                case 4: calendarRepeatType = Calendar.MONTH;break;
                case 5: calendarRepeatType = Calendar.YEAR;break;
                default: calendarRepeatType = 0;
            }
            //计算周期数
            if(userSchedule.getRepeatFlag() == 0){
                calendarRepeatNum = 0;
            }else if(userSchedule.getRepeatFlag() == 3){
                calendarRepeatNum = 2;
            }else{
                calendarRepeatNum = 1;
            }
            if(userSchedule.getIsAllDay() == 0){  //0-全天, 提醒：0-无、1-日程当天、2-1天前、3-2天前、4-1周前
                calendarRemindType = Calendar.DAY_OF_MONTH;
                if(userSchedule.getRemindFlag() == 4){
                    calendarRemindType = Calendar.WEEK_OF_MONTH;
                }
                if(userSchedule.getRemindFlag() == 2 || userSchedule.getRemindFlag() == 4){
                    calendarRemindNum = -1;
                }else if(userSchedule.getRemindFlag() == 3){
                    calendarRemindNum = -2;
                }else{
                    calendarRemindNum = 0;
                }
            }else{  //提醒：0-无、1-日程开始时、2-5分钟前、3-15分钟前、4-30分钟前、5-1小时前、6-2小时前
                switch (userSchedule.getRemindFlag()){
                    case 1: {
                        calendarRemindType = Calendar.MINUTE;
                        calendarRemindNum = 0;
                    }; break;
                    case 2:{
                        calendarRemindType = Calendar.MINUTE;
                        calendarRemindNum = -5;
                    }; break;
                    case 3: {
                        calendarRemindType = Calendar.MINUTE;
                        calendarRemindNum = -15;
                    }; break;
                    case 4:{
                        calendarRemindType = Calendar.MINUTE;
                        calendarRemindNum = -30;
                    }; break;
                    case 5: {
                        calendarRemindType = Calendar.HOUR_OF_DAY;
                        calendarRemindNum = -1;
                    }; break;
                    case 6: {
                        calendarRemindType = Calendar.HOUR_OF_DAY;
                        calendarRemindNum = -2;
                    }; break;
                    default: {
                        calendarRemindType = 0;
                        calendarRemindNum =  0;
                    };
                }
            }
            List<UserScheduleRelate> userScheduleRelateList = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            Date remindTime = userSchedule.getStartDate();
            boolean isStartDate = true;

            do {
//                if(calendarRemindType != 0 && calendarRepeatType != 0){
                    calendar.setTime(remindTime);
                    int currentRepeatTime = calendar.get(calendarRepeatType);
                    calendar.set(calendarRepeatType, currentRepeatTime + (isStartDate ? 0 : calendarRepeatNum)); //计算下一次重复时间，把开始时间也算进去
                    isStartDate = false;//仅作用一次
                    remindTime = calendar.getTime();
                    int currentRemindTime = calendar.get(calendarRemindType);
                    calendar.set(calendarRemindType, currentRemindTime + calendarRemindNum); //计算实际提醒时间
                    if(calendar.getTime().compareTo(userSchedule.getEndDate()) <= 0){ //如果下一次重复时间超出结束时间就不保存
                        UserScheduleRelate userScheduleRelate = new UserScheduleRelate();
                        userScheduleRelate.setScheduleId(userSchedule.getId());
                        userScheduleRelate.setRemindDate(calendar.getTime());
                        userScheduleRelate.setRepeatDate(remindTime);
                        userScheduleRelate.setCreateId(userId);
                        userScheduleRelate.setUpdateId(userId);
                        userScheduleRelate.setCreateDate(currentDate);
                        userScheduleRelate.setUpdateDate(currentDate);
                        userScheduleRelate.setState(0);
                        userScheduleRelateList.add(userScheduleRelate);

                        //判断日程是否需要添加到当天提醒缓存日程中
                        if(currentDateStr.equals(DateUtils.format(userScheduleRelate.getRemindDate(), "yyyy-MM-dd"))){
                            UserSchedule addSchedule = new UserSchedule();
                            BeanUtils.copyProperties(userSchedule,addSchedule);
                            addSchedule.setRemindDate(userScheduleRelate.getRemindDate());
                            addSchedule.setUserName(AppUtil.getUser().getName());
                            addSchedule.setUserImage(AppUtil.getUser().getImage());
                            addSchedule.setDeptId(AppUtil.getUser().getDeptId());
                            addSchedule.setDeptName(AppUtil.getUser().getDeptName());
                            addScheduleList.add(addSchedule);
                        }
                    }
//                }
            }while (userSchedule.getEndDate().compareTo(remindTime) > 0 && userSchedule.getRepeatFlag() != 0); //根据下次重复时间判断，不使用具体提醒时间，提前几天的情况可能导致死循环,重复类型为0时，只执行一次
            if(CollectionUtils.isNotEmpty(userScheduleRelateList)){
                userScheduleRelateMapper.batchSave(userScheduleRelateList);
            }
//        }
    }

    @Override
    public List<Map<String, String>> getCalendar(int year, int month, int userId) {
        //添加缓存，半小时失效
        String key = String.format("userSchedule%s%s%s", userId, year, month);
        Object obj = redisTemplate.opsForValue().get(key);
        if(obj != null){
            return (List<Map<String, String>>) obj;
        }

        List<Map<String, String>> result = new ArrayList<>();
        handleCalendar(year, month, result);//获取指定年月的日期集合
        if(CollectionUtils.isNotEmpty(result)){
            String startDate = result.get(0).get("date");
            String endDate = result.get(result.size()-1).get("date");
            Map<String, Object> param = new HashMap<>();
            param.put("startDate", startDate);
            param.put("endDate", endDate);
            param.put("userId", userId);
            List<Map<String, Object>> scheduleCalendarList = userScheduleMapper.listScheduleCalendarByParam(param);
            if(CollectionUtils.isNotEmpty(scheduleCalendarList)){ //如果有数据，说明日历日期下面有点
                for (Map<String, String> calendar: result) {
                    for (Map<String, Object> scheduleCalendar: scheduleCalendarList) {
                        if(calendar.get("date").equals(scheduleCalendar.get("remindDate"))){
                            if((Long)scheduleCalendar.get("num") > 0){
                                calendar.put("point","1");//日程标记:0-没有日程、1-有日程
                            }
                        }
                    }
                }
            }
        }

        //添加缓存，设置过期时间为1小时
        redisTemplate.opsForValue().set(key, result, 30, TimeUnit.MINUTES);

        return result;
    }

    @Override
    public List<UserSchedule> listScheduleByDate(String date, int userId) {
        //添加缓存，半小时失效
        String key = String.format("userSchedule%s%s", userId, date).replaceAll("-","");
        Object obj = redisTemplate.opsForValue().get(key);
        if(obj != null){
            return (List<UserSchedule>) obj;
        }

        List<UserSchedule> result = userScheduleMapper.listScheduleByDate(date, userId);

        //添加缓存，设置过期时间为1小时
        redisTemplate.opsForValue().set(key, result, 30, TimeUnit.MINUTES);
        return result;
    }

    @Override
    public List<UserSchedule> listAllScheduleByDate(String date) {
        return userScheduleMapper.listScheduleByDate(date, null);
    }

    @Override
    public void deleteCache() {

    }

    /**
     * 处理日历，参考windows电脑日期格式
     * @param year 年
     * @param month 月
     * @param allDayList  返回结果
     */
    private void handleCalendar(int year, int month,  List<Map<String, String>> allDayList){
        int length = 42; //日历面板总天数
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); //当前年
        int currentMonth = calendar.get(Calendar.MONTH) + 1;  //当前月
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); //当前天

        int lastYear = month == 1 ? year - 1 : year; //上一年
        int lastMonth = month == 1 ? 12 : month - 1; //上一月
        int nextYear = month == 12 ? year + 1 : year; //下一年
        int nextMonth = month == 12 ? 1 : month + 1; //下一月
        int lastMonthDayTotalNum = maxDayInMonth(lastYear, lastMonth); //上一月天数

        int monthDayTotalNum = maxDayInMonth(year, month); //指定月天数

        int firstDayWeek = getWeek(year, month, 1); //指定月份第一天星期

        int lastMonthDayNum = 0; //需要补全上个月天数
        int nextMonthDayNum = 0; //需要补全下个月天数
        if (firstDayWeek == Calendar.SUNDAY){ //当月份第一天是星期天，默认第一行展示上个月最后7天
            lastMonthDayNum = 7 + firstDayWeek-1;
        }else{
            lastMonthDayNum = firstDayWeek-1;
        }
        nextMonthDayNum = length - monthDayTotalNum - lastMonthDayNum; //日历展示下个月的数量
        //遍历计算日期（dayFlag: -1-上个月日期、0-当前月日期、1-当前天日期、2-下个月日期）
        if(lastMonthDayNum > 0){
            for(int i = (lastMonthDayTotalNum - lastMonthDayNum + 1); i <= lastMonthDayTotalNum; i++){
                String day = i < 10 ? "0"+i : i+"";
                String tempMonth = lastMonth < 10 ? "0"+lastMonth : lastMonth+"";
                Map<String, String> dayMap = new HashMap<>();
                String dayFlag = (lastYear == currentYear && lastMonth == currentMonth && i == currentDay) ? "1" : "-1";
                dayMap.put("dayFlag", dayFlag);
                dayMap.put("week", getWeek(lastYear,lastMonth, i)+"");
                dayMap.put("date", year+"-"+tempMonth+"-"+day);
                dayMap.put("day", i+"");
                dayMap.put("monthCH", lastMonth+"月"+i+"日");
                dayMap.put("point", "0"); //日程标记:0-没有日程、1-有日程
                allDayList.add(dayMap);
            }
        }
        //当前月
        for(int i = 1; i <= monthDayTotalNum; i++){
            String day = i < 10 ? "0"+i : i+"";
            String tempMonth = month < 10 ? "0"+month : month+"";
            Map<String, String> dayMap = new HashMap<>();
            int dayFlag = (year == currentYear && month == currentMonth && i == currentDay) ? 1 : 0;
            dayMap.put("dayFlag", dayFlag+"");
            dayMap.put("week", getWeek(year,month, i)+"");
            dayMap.put("date", year+"-"+tempMonth+"-"+day);
            dayMap.put("day", i+"");
            dayMap.put("monthCH", month+"月"+i+"日");
            dayMap.put("point", "0"); //日程标记:0-没有日程、1-有日程
            allDayList.add(dayMap);
        }
        //下个月
        if(nextMonthDayNum > 0){
            for(int i = 1; i <= nextMonthDayNum; i++){
                String day = i < 10 ? "0"+i : i+"";
                String tempMonth = nextMonth < 10 ? "0"+nextMonth : nextMonth+"";
                Map<String, String> dayMap = new HashMap<>();
                String dayFlag = (nextYear == currentYear && nextMonth == currentMonth && i == currentDay) ? "1" : "2";
                dayMap.put("dayFlag", dayFlag);
                dayMap.put("week", getWeek(nextYear,nextMonth, i)+"");
                dayMap.put("date", nextYear+"-"+tempMonth+"-"+day);
                dayMap.put("day", i+"");
                dayMap.put("monthCH", nextMonth+"月"+i+"日");
                dayMap.put("point", "0"); //日程标记:0-没有日程、1-有日程
                allDayList.add(dayMap);
            }
        }
    }

    /**
     * 获取指定年月的月数
     */
    public static int maxDayInMonth(int year, int month) {
        int max = 30;
        if (month == 1 | month == 3 | month == 5 | month == 7 | month == 8 | month == 10 | month == 12){
            max = 31;
        }
        if (month == 2) {
            max = 28;
        }
        if (month == 2 & (((year % 4 == 0) && (year % 100 != 0)) ||  (year % 400 == 0))){
            max = 29;
        }
        return max;
    }

    /**
     * 获取周序号
     */
    private int getWeek(int year, int month, int day){
        Calendar c = Calendar.getInstance();
        c.set(year, month-1, day);
        int week = c.get(Calendar.DAY_OF_WEEK);
        return week;
    }

    @Override
    public void sendMessage(List<UserSchedule> userScheduleList) {
       if(CollectionUtils.isNotEmpty(userScheduleList)){
           List<Message> messagesList = new ArrayList<>();
           for(UserSchedule userSchedule : userScheduleList){
               String pictureAddress = userSchedule.getUserImage() == null ? "/img/mrtx_2.png" : userSchedule.getUserImage();
               // 推送WebSocket消息；
               WSMessage message = new WSMessage();
               message.setReceiveUserId(userSchedule.getCreateId() + "");
               message.setReceiveName(userSchedule.getUserName());
               message.setSendName(userSchedule.getUserName());
               message.setSendUserId(userSchedule.getCreateId() + "");
               message.setSendUserImage(pictureAddress);
               message.setContent("[日程]"+userSchedule.getName());
               WebSocketServer.sendMessage(message);

               Message newMessage = new Message();
               newMessage.setPic(pictureAddress);
               newMessage.setContent("[日程]"+userSchedule.getName());
               newMessage.setInitiatorDept(userSchedule.getDeptId());
               newMessage.setInitiatorWorker(userSchedule.getCreateId());
               newMessage.setAcceptWorker(userSchedule.getCreateId());
               newMessage.setAcceptDept(userSchedule.getDeptId());
               //消息分类
               newMessage.setParentType(3);//通知
               newMessage.setType(15);//日程服务通知
               if(userSchedule.getJumpUrl()!=null && !"".equals(userSchedule.getJumpUrl())){
                   newMessage.setUrl(userSchedule.getJumpUrl());
                   newMessage.setUrlName(userSchedule.getJumpTitle());
               }else{
                   newMessage.setUrl(null);
                   newMessage.setUrlName(null);
               }
               messagesList.add(newMessage);
           }
           messageService.batchAddMessage(messagesList);
       }
    }
}
