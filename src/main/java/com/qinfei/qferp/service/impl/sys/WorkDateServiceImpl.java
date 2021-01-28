package com.qinfei.qferp.service.impl.sys;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.sys.WorkDate;
import com.qinfei.qferp.mapper.sys.WorkDateMapper;
import com.qinfei.qferp.service.sys.IWorkDateService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.util.*;

/**
 * @CalssName WorkDateServiceImpl
 * @Description 工作日表服务接口
 * @Author xuxiong
 * @Date 2019/10/15 0015 11:36
 * @Version 1.0
 */
@Service
public class WorkDateServiceImpl implements IWorkDateService {
    @Autowired
    private WorkDateMapper workDateMapper;

    @Override
    @Transactional
    public void save(WorkDate workDate) {
        try{
            validateSave(workDate); //数据校验并填充
            workDateMapper.save(workDate);
        }catch (QinFeiException byeException){
            throw new QinFeiException(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "工作日新增失败！");
        }
    }

    @Override
    public WorkDate getWorkDateByDate(String workDate) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            Date date = DateUtils.parse(workDate, "yyyy-MM-dd");
            WorkDate existWorkDate = workDateMapper.getWorkDateByDate(date, AppUtil.getUser().getCompanyCode());
            return existWorkDate;
        }catch (QinFeiException byeException){
            throw new QinFeiException(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "获取工作日失败！");
        }
    }

    @Override
    public Map<String, List<String>> listDateByRange(String startDate, String endDate) {
        try{
            Map<String, List<String>> result = new HashMap<>();
            List<Date> dateList = findDates(DateUtils.parse(startDate, "yyyy-MM-dd"), DateUtils.parse(endDate, "yyyy-MM-dd"));
            if(CollectionUtils.isNotEmpty(dateList)){
                WorkDate param = new WorkDate();
                param.setCompanyCode(AppUtil.getUser().getCompanyCode());
                param.setStartDate(startDate);
                param.setEndDate(endDate);
                List<WorkDate> existWorkDateList = workDateMapper.listAllByParam(param);//获取时间段内已经添加的日期
                Map<String, Integer> existWorkDateMap = new HashMap<>(); //已存在的日期
                if(CollectionUtils.isNotEmpty(existWorkDateList)){
                    for(WorkDate tempWorkDate : existWorkDateList){
                        existWorkDateMap.put(DateUtils.format(tempWorkDate.getWorkDate(), "yyyy-MM-dd"), tempWorkDate.getDateType());
                    }
                }
                //进行分类
                List<String> workDateList = new ArrayList<>(); //工作日集合
                List<String> restDateList = new ArrayList<>(); //休息日集合
                List<String> holidayList = new ArrayList<>(); //法定节假日集合
                for(Date date : dateList){
                    String dateStr = DateUtils.format(date, "yyyy-MM-dd");
                    if(existWorkDateMap.get(dateStr) != null){
                        if(existWorkDateMap.get(dateStr) == 1){
                            restDateList.add(DateUtils.format(date, "yyyy-MM-dd"));
                        }else if(existWorkDateMap.get(dateStr) == 2){
                            holidayList.add(DateUtils.format(date, "yyyy-MM-dd"));
                        }else {
                            workDateList.add(DateUtils.format(date, "yyyy-MM-dd"));
                        }
                    }else{
                        int week = getWeek(date);
                        if(week == Calendar.SUNDAY || week == Calendar.SATURDAY){
                            restDateList.add(DateUtils.format(date, "yyyy-MM-dd"));
                        }else {
                            workDateList.add(DateUtils.format(date, "yyyy-MM-dd"));
                        }
                    }
                }
                result.put("workDate", workDateList);
                result.put("restDate", restDateList);
                result.put("holiday", holidayList);
            }
            return result;
        }catch (Exception e){
            return null;
        }
    }

    @Override
    @Transactional
    public void initBatchSave(WorkDate workDate) {
        try{
            validateBatchOp(workDate); //数据校验并填充
            List<Date> initDateList = findDates(DateUtils.parse(workDate.getStartDate(), "yyyy-MM-dd"), DateUtils.parse(workDate.getEndDate(), "yyyy-MM-dd"));
            if(CollectionUtils.isNotEmpty(initDateList)){
                Map<String, Object> param = new HashMap<>();
                param.put("companyCode", workDate.getCompanyCode());
                param.put("startDate", workDate.getStartDate());
                param.put("endDate", workDate.getEndDate());
                param.put("state", -9);
                workDateMapper.updateStateByParam(param);//删除指定时间段内容时间
                List<WorkDate> addWorkDateList = new ArrayList<>();
                for(Date date : initDateList){
                    addWorkDateList.add(builderWorkDate(date, workDate));
                }
                if(CollectionUtils.isNotEmpty(addWorkDateList)){
                    workDateMapper.batchSave(addWorkDateList);
                }
            }
        }catch (QinFeiException byeException){
            throw new QinFeiException(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "默认日期批量设置失败！");
        }
    }

    //构建新增的日期对象
    private WorkDate builderWorkDate(Date date, WorkDate workDate){
        WorkDate addWorkDate = new WorkDate();
        BeanUtils.copyProperties(workDate, addWorkDate);
        addWorkDate.setWorkDate(date);
        int week = getWeek(date);
        if(week == Calendar.SUNDAY || week == Calendar.SATURDAY){
            addWorkDate.setDateType(1);
        }else {
            addWorkDate.setDateType(0);
        }
        return addWorkDate;
    }

    @Override
    @Transactional
    public void batchEdit(WorkDate workDate) {
        try{
            validateBatchOp(workDate); //数据校验并填充
            List<Date> dateList = findDates(DateUtils.parse(workDate.getStartDate(), "yyyy-MM-dd"), DateUtils.parse(workDate.getEndDate(), "yyyy-MM-dd"));
            if(CollectionUtils.isNotEmpty(dateList)){
                WorkDate param = new WorkDate();
                param.setCompanyCode(workDate.getCompanyCode());
                param.setStartDate(workDate.getStartDate());
                param.setEndDate(workDate.getEndDate());
                List<WorkDate> existWorkDateList = workDateMapper.listAllByParam(param);//获取时间段内已经添加的日期
                Map<String, WorkDate> existWorkDateMap = new HashMap<>(); //已存在的日期
                if(CollectionUtils.isNotEmpty(existWorkDateList)){
                    for(WorkDate tempWorkDate : existWorkDateList){
                        existWorkDateMap.put(DateUtils.format(tempWorkDate.getWorkDate(), "yyyy-MM-dd"), tempWorkDate);
                    }
                }
                List<WorkDate> addWorkDateList = new ArrayList<>(); //需要新增的日期
                List<WorkDate> editWorkDateList = new ArrayList<>(); //需要编辑的日期
                for(Date date : dateList){
                    String dateStr = DateUtils.format(date, "yyyy-MM-dd");
                    if(existWorkDateMap.get(dateStr) == null){
                        addWorkDateList.add(builderWorkDate(date, workDate));
                    }else{
                        WorkDate existWorkDate = existWorkDateMap.get(dateStr);
                        existWorkDate.setDateType(workDate.getDateType());
                        existWorkDate.setUpdateId(workDate.getUpdateId());
                        existWorkDate.setRemarks(workDate.getRemarks());
                        editWorkDateList.add(existWorkDate);
                    }
                }
                if(CollectionUtils.isNotEmpty(addWorkDateList)){
                    workDateMapper.batchSave(addWorkDateList);
                }
                if(CollectionUtils.isNotEmpty(editWorkDateList)){
                    workDateMapper.batchUpdate(editWorkDateList);
                }
            }
        }catch (QinFeiException byeException){
            throw new QinFeiException(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "范围编辑日期失败！");
        }
    }

    //新增数据校验
    private void validateSave(WorkDate workDate){
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(workDate == null){
            throw new QinFeiException(1002, "新增数据不存在！");
        }
        if(workDate.getWorkDate() == null){
            throw new QinFeiException(1002, "工作日必须输入！");
        }
        WorkDate existWorkDate = workDateMapper.getWorkDateByDate(workDate.getWorkDate(), user.getCompanyCode());
        if(existWorkDate != null){
            throw new QinFeiException(1002, "工作日已经存在，您可以去修改！");
        }
        workDate.setCreateId(user.getId());
        workDate.setUpdateId(user.getId());
        workDate.setUpdateDate(new Date());
        workDate.setCompanyCode(user.getCompanyCode());
    }

    //新增数据校验
    private void validateBatchOp(WorkDate workDate){
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(workDate == null){
            throw new QinFeiException(1002, "日期数据不存在！");
        }
        if(workDate.getStartDate() == null){
            throw new QinFeiException(1002, "开始时间必须输入！");
        }
        if(workDate.getEndDate() == null){
            throw new QinFeiException(1002, "结束时间必须输入！");
        }
        if(DateUtils.parse(workDate.getStartDate(), "yyyy-MM-dd").compareTo(DateUtils.parse(workDate.getEndDate(),"yyyy-MM-dd")) > 0){
            throw new QinFeiException(1002, "开始时间必须小于结束时间！");
        }
        workDate.setCreateId(user.getId());
        workDate.setUpdateId(user.getId());
        workDate.setUpdateDate(new Date());
        workDate.setCompanyCode(user.getCompanyCode());
    }

    //获取时间段内的所有日期
    private List<Date> findDates(Date startDate, Date endDate) {
        if(startDate.compareTo(endDate) > 0){
            return new ArrayList<>();
        }
        List<Date> result = new ArrayList<>();
        result.add(startDate); //将开始时间保存
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        Date currentDate = startDate;
        while (currentDate.compareTo(endDate) < 0){
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            currentDate = calendar.getTime(); //重新设置日期
            result.add(calendar.getTime());
        }
        return result;
    }

    //获取当前时间时间段往前时间列表
    private List<String> findDates(int diff, Date submitDate) {
        List<String> result = new ArrayList<>();
        result.add(DateUtils.format(submitDate, "yyyy-MM-dd")); //将结束时间保存
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(submitDate);
        for(int i = 1; i < diff; i++){
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            result.add(DateUtils.format(calendar.getTime(), "yyyy-MM-dd"));
        }
        return result;
    }

    @Override
    @Transactional
    public void updateById(WorkDate workDate) {
        try{
            validateUpdateById(workDate); //数据校验并填充
            workDateMapper.updateById(workDate);
        }catch (QinFeiException byeException){
            throw new QinFeiException(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "工作日修改失败！");
        }
    }

    //更新数据校验
    private void validateUpdateById(WorkDate workDate){
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(workDate == null || workDate.getId() == null){
            throw new QinFeiException(1002, "数据不能为空！");
        }
        WorkDate existWorkDate = workDateMapper.getWorkDateById(workDate.getId());
        if(existWorkDate == null){
            throw new QinFeiException(1002, "工作日不存在！");
        }
        workDate.setUpdateId(user.getId());
        workDate.setUpdateDate(new Date());
    }

    @Override
    @Transactional
    public void updateStateById(Integer id, Integer state) {
        try{
            WorkDate existWorkDate = workDateMapper.getWorkDateById(id);
            if(existWorkDate == null){
                throw new QinFeiException(1002, "工作日不存在！");
            }
            workDateMapper.updateStateById(id, state);
        }catch (QinFeiException byeException){
            throw new QinFeiException(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "工作日状态修改失败！");
        }
    }

    @Override
    public PageInfo<WorkDate> listByParam(Map<String, Object> param, Pageable pageable) {
        List<WorkDate> result = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            result = workDateMapper.listByParam(param);
        }
        return new PageInfo<>(result);
    }

    @Override
    public List<Map<String, String>> getCalendar(int year, int month) {
        List<Map<String, String>> result = new ArrayList<>();
        Map<String, Map<String, String>> calendarMap = new HashMap<>();
        handleCalendar(year, month, result, calendarMap);//获取指定年月的日期集合
        User user = AppUtil.getUser();
        if(user == null){
            return result;
        }
        if(CollectionUtils.isNotEmpty(result)){
            String startDate = result.get(0).get("date"); //日历开始时间
            String endDate = result.get(result.size()-1).get("date"); //日历结束时间
            WorkDate workDate = new WorkDate();
            workDate.setStartDate(startDate);
            workDate.setEndDate(endDate);
            workDate.setCompanyCode(user.getCompanyCode());
            List<WorkDate> workDateList = workDateMapper.listAllByParam(workDate);
            if(CollectionUtils.isNotEmpty(workDateList)){ //设置日期类型，以及是否有添加
                for(WorkDate tempWorkdate : workDateList){
                    String date = DateUtils.format(tempWorkdate.getWorkDate(), "yyyy-MM-dd");
                    if(calendarMap.get(date) != null){
                        calendarMap.get(date).put("dateType", String.valueOf(tempWorkdate.getDateType()));
                        calendarMap.get(date).put("id", String.valueOf(tempWorkdate.getId()));
                        calendarMap.get(date).put("remarks", tempWorkdate.getRemarks());
                    }
                }

            }
        }
        return result;
    }

    @Override
    public void exportWorkDate(Map<String, Object> map, OutputStream outputStream) {
        User user = AppUtil.getUser();
        List<Map<String, Object>> result = new ArrayList<>();
        if(user != null){
            map.put("companyCode", user.getCompanyCode());
            result = workDateMapper.listMapByParam(map);
            if(CollectionUtils.isEmpty(result)){
                result.add(buildEmptyMap());
            }
        }else {
            result.add(buildEmptyMap());
        }
        String[] heads = {"日期","日期类型","创建时间","更新时间","备注"};
        String[] fields={ "workDate","dateType","createDate","updateDate","remarks"};
        String fileName = getExcelTitle(map);
        ExcelUtil.exportExcel(fileName,heads, fields, result, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if(value!=null){
                if("dateType".equals(field)){
                    if(StringUtils.isEmpty(String.valueOf(value))){
                        cell.setCellValue("");
                    }else {
                        Integer dateType = Integer.parseInt(String.valueOf(value));
                        if(dateType == 1){
                            cell.setCellValue("休息日");
                        }else if(dateType == 2){
                            cell.setCellValue("法定节假日");
                        }else {
                            cell.setCellValue("工作日");
                        }
                    }
                }else{
                    cell.setCellValue(String.valueOf(value));
                }
            }else {
                cell.setCellValue("");
            }
        });
    }

    private Map<String, Object> buildEmptyMap(){
        Map<String, Object> emptyMap = new HashMap<>();
        emptyMap.put("workDate", "");
        emptyMap.put("dateType", "");
        emptyMap.put("createDate", "");
        emptyMap.put("updateDate", "");
        emptyMap.put("remarks", "");
        return emptyMap;
    }

    @Override
    public String getLastNumWorkDate(int num,Date submitDate) {
        try{
            if(submitDate == null){
                submitDate = new Date();
            }
            if(num <= 0){
                return DateUtils.format(submitDate, "yyyy-MM-dd");
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(submitDate);
            int currentYear = calendar.get(Calendar.YEAR); //当前年
            int currentMonth = calendar.get(Calendar.MONTH) + 1;  //当前月
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH); //当前天
            String dateFormat = "%s-%02d-%02d"; //不足前面补零

            int diffDay = 15 * num; //默认查询当前时间往前推num*15天来选择工作日
            List<String> dateList = findDates(diffDay, submitDate);
            if(CollectionUtils.isNotEmpty(dateList)){
                String startDate = dateList.get(dateList.size()-1);
                String endDate = dateList.get(0);
                Map<String, List<String>> dateGroupMap = listDateByRange(startDate, endDate);
                if(dateGroupMap != null && CollectionUtils.isNotEmpty(dateGroupMap.get("workDate"))){
                    List<String> workDateList = dateGroupMap.get("workDate");
                    String currentDate = String.format(dateFormat, currentYear, currentMonth, currentDay);
                    int index = workDateList.size() - 1 - num; //默认包含当前日期，由于可num天内补请假，所以计算最晚可请假的工作日
                    if(!workDateList.contains(currentDate)){ //计算返回工作日的数组下标，如果当前日期属于休息日
                        index += num; //工作日列表不包含当前天时，返回最后一个工作日
                    }
                    return workDateList.get(index);
                }else{
                    int week = getWeek(calendar.getTime());
                    Date date = DateUtils.calDay(calendar.getTime(),-1);
                    if(week == Calendar.MONDAY){ //周一
                        date = DateUtils.calDay(calendar.getTime(),-3);
                    }
                    return DateUtils.format(date, "yyyy-MM-dd");
                }
            }else {
                int week = getWeek(submitDate);
                Date date = DateUtils.calDay(submitDate,-1);
                if(week == Calendar.MONDAY){ //周一
                    date = DateUtils.calDay(submitDate,-3);
                }
                return DateUtils.format(date, "yyyy-MM-dd");
            }
        }catch (Exception e){
            e.printStackTrace();
            int week = getWeek(submitDate);
            Date date = DateUtils.calDay(submitDate,-1);
            if(week == Calendar.MONDAY){ //周一
                date = DateUtils.calDay(submitDate,-3);
            }
            return DateUtils.format(date, "yyyy-MM-dd");
        }
    }

    //获取表格标题
    private String getExcelTitle(Map<String, Object> map){
        Object timeType = map.get("timeQuantum");
        String title = "日期列表(time)";
        String time = "本月";
        if(!StringUtils.isEmpty(timeType)){
            if("1".equals(timeType)){
                time = "本周";
            }else if("2".equals(timeType)){
                time = "本月";
            }else if("3".equals(timeType)){
                time = "本年";
            }else {
                Object startDate = map.get("startDate");
                Object endDate = map.get("endDate");
                if(!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)){
                    time = startDate+" 至 "+endDate;
                }else if (!StringUtils.isEmpty(startDate)) {
                    time = "自"+startDate+"起";
                }else if (!StringUtils.isEmpty(endDate)){
                    time = "截止"+endDate;
                }
            }
        }
        return title.replaceAll("time",time.replaceAll("/","-"));
    }

    /**
     * 处理日历，参考windows电脑日期格式
     * @param year 年
     * @param month 月
     * @param allDayList  返回结果
     * @param calendarMap 返回结果
     */
    private void handleCalendar(int year, int month,  List<Map<String, String>> allDayList, Map<String, Map<String, String>> calendarMap){
        int length = 42; //日历面板总天数
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR); //当前年
        int currentMonth = calendar.get(Calendar.MONTH) + 1;  //当前月
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH); //当前天

        int lastYear = month == 1 ? year - 1 : year; //上一年
        int lastMonth = month == 1 ? 12 : month - 1; //上一月
        int nextYear = month == 12 ? year + 1 : year; //下一年
        int nextMonth = month == 12 ? 1 : month + 1; //下一月
        int lastMonthDayTotalNum =  maxDayInMonth(lastYear, lastMonth); //上一月天数

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
                Integer week = getWeek(lastYear,lastMonth, i);
                String dateType = (week == Calendar.SATURDAY || week == Calendar.SUNDAY) ? "1" : "0";
                dayMap.put("dayFlag", dayFlag);
                dayMap.put("week", week+"");
                dayMap.put("date", year+"-"+tempMonth+"-"+day);
                dayMap.put("day", i+"");
                dayMap.put("monthCH", lastMonth+"月"+i+"日");
                dayMap.put("dateType", dateType); //日期类型： 0-工作日、1-休息日、2-法定节假日，默认周末休息
                dayMap.put("id", ""); //有值则已添加
                dayMap.put("remarks",""); //备注
                allDayList.add(dayMap);
                calendarMap.put(dayMap.get("date"), dayMap);
            }
        }
        //当前月
        for(int i = 1; i <= monthDayTotalNum; i++){
            String day = i < 10 ? "0"+i : i+"";
            String tempMonth = month < 10 ? "0"+month : month+"";
            Map<String, String> dayMap = new HashMap<>();
            int dayFlag = (year == currentYear && month == currentMonth && i == currentDay) ? 1 : 0;
            Integer week = getWeek(year,month, i);
            String dateType = (week == Calendar.SATURDAY || week == Calendar.SUNDAY) ? "1" : "0";
            dayMap.put("dayFlag", dayFlag+"");
            dayMap.put("week", week+"");
            dayMap.put("date", year+"-"+tempMonth+"-"+day);
            dayMap.put("day", i+"");
            dayMap.put("monthCH", month+"月"+i+"日");
            dayMap.put("dateType", dateType); //日期类型： 0-工作日、1-休息日、2-法定节假日，默认周末休息
            dayMap.put("id", ""); //有值则已添加
            dayMap.put("remarks",""); //备注
            allDayList.add(dayMap);
            calendarMap.put(dayMap.get("date"), dayMap);
        }
        //下个月
        if(nextMonthDayNum > 0){
            for(int i = 1; i <= nextMonthDayNum; i++){
                String day = i < 10 ? "0"+i : i+"";
                String tempMonth = nextMonth < 10 ? "0"+nextMonth : nextMonth+"";
                Map<String, String> dayMap = new HashMap<>();
                String dayFlag = (nextYear == currentYear && nextMonth == currentMonth && i == currentDay) ? "1" : "2";
                Integer week = getWeek(nextYear,nextMonth, i);
                String dateType = (week == Calendar.SATURDAY || week == Calendar.SUNDAY) ? "1" : "0";
                dayMap.put("dayFlag", dayFlag);
                dayMap.put("week", week+"");
                dayMap.put("date", nextYear+"-"+tempMonth+"-"+day);
                dayMap.put("day", i+"");
                dayMap.put("monthCH", nextMonth+"月"+i+"日");
                dayMap.put("dateType", dateType); //日期类型： 0-工作日、1-休息日、2-法定节假日，默认周末休息
                dayMap.put("id", ""); //有值则已添加
                dayMap.put("remarks",""); //备注
                allDayList.add(dayMap);
                calendarMap.put(dayMap.get("date"), dayMap);
            }
        }
    }

    /**
     * 获取指定年月的月数
     */
    private int maxDayInMonth(int year, int month) {
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

    /**
     * 获取周序号
     */
    public int getWeek(Date date){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int week = c.get(Calendar.DAY_OF_WEEK);
        return week;
    }


}
