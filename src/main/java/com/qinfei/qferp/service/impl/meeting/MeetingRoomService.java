package com.qinfei.qferp.service.impl.meeting;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.meeting.MeetingRoom;
import com.qinfei.qferp.entity.meeting.MeetingRoomApply;
import com.qinfei.qferp.entity.meeting.MeetingUser;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.meeting.MeetingMapper;
import com.qinfei.qferp.mapper.meeting.MeetingRoomApplyMapper;
import com.qinfei.qferp.mapper.meeting.MeetingRoomMapper;
import com.qinfei.qferp.mapper.meeting.MeetingUserMapper;
import com.qinfei.qferp.service.meeting.IMeetingRoomService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;

/**
 * @CalssName MeetingRecordServiceImpl
 * @Description 会议室表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:36
 * @Version 1.0
 */
@Service
@Slf4j
public class MeetingRoomService implements IMeetingRoomService {
    @Autowired
    private MeetingRoomMapper meetingRoomMapper;
    @Autowired
    private MeetingRoomApplyMapper meetingRoomApplyMapper;
    @Autowired
    private MeetingMapper meetingMapper;
    @Autowired
    private MeetingUserMapper meetingUserMapper;


    @Override
    @Transactional
    public void save(MeetingRoom meetingRoom) {
        try{
            validateSave(meetingRoom); //校验并设置值
            meetingRoomMapper.save(meetingRoom);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "很抱歉，添加会议室出错啦，请联系技术人员！");
        }
    }

    @Override
    @Transactional
    public void update(MeetingRoom meetingRoom) {
        try{
            validateUpdate(meetingRoom); //校验并设置值
            meetingRoomMapper.updateById(meetingRoom);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "很抱歉，修改会议室出错啦，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void del(Integer id) {
        try{
            validateDel(id);
            meetingRoomMapper.updateStateById(id, -9);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "很抱歉，删除会议室出错啦，请联系技术人员！");
        }
    }

    @Override
    public MeetingRoom getMeetingRoomById(Integer id) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            return meetingRoomMapper.getMeetingRoomById(id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "很抱歉，修改会议室出错啦，请联系技术人员！");
        }
    }

    @Override
    public PageInfo<MeetingRoom> listMeetingRoom(Map<String, Object> param, Pageable pageable) {
        List<MeetingRoom> meetingRoomList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            param.put("companyCode", user.getCompanyCode());
            meetingRoomList = meetingRoomMapper.listByParam(param);
        }
        return new PageInfo<>(meetingRoomList);
    }

    @Override
    public PageInfo<MeetingRoom> listApplyMeetingRoom(Map<String, Object> param, Pageable pageable) {
        List<MeetingRoom> meetingRoomList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            param.put("companyCode", user.getCompanyCode());
            meetingRoomList = meetingRoomMapper.listByParam(param);
            Map<Integer, MeetingRoom> meetingRoomMap = new HashMap<>();
            List<Integer> meetingIdList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(meetingRoomList)){
                for(MeetingRoom meetingRoom : meetingRoomList){
                    meetingIdList.add(meetingRoom.getId());
                    meetingRoomMap.put(meetingRoom.getId(), meetingRoom);
                }
                Map<String, Object> param1 = new HashMap<>();
                String applyDate = (String) param.get("applyDate");
                if(StringUtils.isEmpty(applyDate)){ //如果有传日期
                    applyDate = DateUtils.format(new Date(), "yyyy-MM-dd");
                }
                param1.put("currentDay", applyDate);
                param1.put("roomIdList",meetingIdList);
                param1.put("companyCode", user.getCompanyCode());
                List<MeetingRoomApply> meetingRoomApplyList = meetingRoomApplyMapper.listMeetingApplyByRoomId(param1);
                if(CollectionUtils.isNotEmpty(meetingRoomApplyList)){
                    for(MeetingRoomApply meetingRoomApply : meetingRoomApplyList){
                        if(CollectionUtils.isEmpty(meetingRoomMap.get(meetingRoomApply.getMeetRoomId()).getMeetingRoomApplyList())){
                            meetingRoomMap.get(meetingRoomApply.getMeetRoomId()).setMeetingRoomApplyList(new ArrayList<>());
                        }
                        meetingRoomMap.get(meetingRoomApply.getMeetRoomId()).getMeetingRoomApplyList().add(meetingRoomApply);
                    }
                }

                //无论是否有预约列表，都需要计算出时间段
                for(Integer id : meetingRoomMap.keySet()){
                    calculateTimeSlot(meetingRoomMap.get(id).getMeetingRoomApplyList(),meetingRoomMap.get(id), applyDate);
                }
            }
        }
        return new PageInfo<>(meetingRoomList);
    }

    @Override
    public List<Map<String, String>> getMeetingTimeSlot(Integer id) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录");
            }
            MeetingRoom meetingRoom = meetingRoomMapper.getMeetingRoomById(id);
            if(meetingRoom == null){
                throw new QinFeiException(1002, "会议室不存在！");
            }
            String currentDate = DateUtils.format(new Date(), "yyyy-MM-dd");
            String startTime = StringUtils.isNotEmpty(meetingRoom.getOpenTimeStart()) ? meetingRoom.getOpenTimeStart() : "00:00";
            String endTime = StringUtils.isNotEmpty(meetingRoom.getOpenTimeEnd()) ? meetingRoom.getOpenTimeEnd() : "24:00";
            //计算时间段
            Date startTime1 = DateUtils.parse(currentDate+" "+startTime, "yyyy-MM-dd HH:mm");
            Date endTime1 = DateUtils.parse(currentDate+" "+endTime, "yyyy-MM-dd HH:mm");
            //查询开放时间内已预约的时间段
            Map<String,  Object> param = new HashMap<>();
            param.put("companyCode", user.getCompanyCode());
            param.put("startTime", startTime1);
            param.put("endTime", endTime1);
            List<MeetingRoomApply> meetingRoomApplyList = meetingRoomApplyMapper.listMeetingApplyByParam(param);
            int num = (int) ((endTime1.getTime() - startTime1.getTime()) / 1000 / 60 / meetingRoom.getMeetUnit());
            if(num > 0){
                List<Map<String, String>> result = new ArrayList<>();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startTime1);
                for(int i = 0; i < num; i++){
                    Date tempStartTime = calendar.getTime(); //最小粒度开始时间
                    String start = DateUtils.format(tempStartTime, "HH:mm");
                    String hour = DateUtils.format(calendar.getTime(), "H");
                    String wholeTimeFlag = calendar.get(Calendar.MINUTE) == 0 ? "1" : "0"; //是否整点：1-整点， 0-非整点
                    String passTimeFlag = calendar.getTime().compareTo(new Date()) > 0 ? "0" : "1"; //是否已过时间：1-已过、0-未过
                    calendar.add(Calendar.MINUTE, meetingRoom.getMeetUnit());
                    Date tempEndTime = calendar.getTime(); //最小粒度开始时间
                    String end = DateUtils.format(tempEndTime, "HH:mm");
                    Map<String, String> map = new HashMap<>();
                    String applyFlag = validateTimeSlot(tempStartTime, tempEndTime, meetingRoomApplyList, map) ? "1" : "0"; //是否预约：1-已被预约、0-未被预约
                    map.put("hour", hour);
                    map.put("startTime", start);
                    map.put("endTime", end);
                    map.put("wholeTimeFlag", wholeTimeFlag);
                    map.put("passTimeFlag", passTimeFlag);
                    map.put("applyFlag", applyFlag);
                    result.add(map);
                }
                return result;
            }else {
                throw new QinFeiException(1002, "很抱歉，会议室每日开放时间范围设置不合理！");
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "很抱歉，会议室预约页面开小差了！");
        }
    }

    @Override
    public int getCountByParam(Map<String, Object> param) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            result = meetingRoomMapper.getCountByParam(param);
        }
        return result;
    }

    @Override
    public void exportMeetingList(HttpServletResponse response, Map<String, Object> param) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("会议室使用详情列表.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录");
            }
            param.put("companyCode", user.getCompanyCode());
            List<Map<String, Object>> resultList = meetingMapper.listMeetingByRoomId(param);
            if(CollectionUtils.isNotEmpty(resultList)){
                List<Integer> meetIds = new ArrayList<>();
                Map<Integer, Map<String, Object>> meetMap = new HashMap<>();
                for(Map<String, Object> meet : resultList){
                    meetIds.add(Integer.parseInt(String.valueOf(meet.get("meetId"))));
                    Object otherHost = meet.get("otherHost");
                    Object otherOrganizer = meet.get("otherOrganizer");
                    Object otherPark = meet.get("otherPark");
                    int allUserNum = 0; //总数
                    if(otherHost != null){
                        allUserNum += String.valueOf(otherHost).split(",").length;
                    }
                    if(otherOrganizer != null){
                        allUserNum += String.valueOf(otherOrganizer).split(",").length;
                    }
                    if(otherPark != null){
                        allUserNum += String.valueOf(otherPark).split(",").length;
                    }
                    Date startTime = DateUtils.parse(String.valueOf(meet.get("startTime")), "yyyy-MM-dd HH:mm");
                    Date endTime = DateUtils.parse(String.valueOf(meet.get("endTime")), "yyyy-MM-dd HH:mm");
                    meet.put("acceptNum", 0); //公司接受数量
                    meet.put("allUserNum", allUserNum); //公司人数列表
                    meet.put("meetDate", DateUtils.format(startTime, "yyyy-MM-dd"));
                    meet.put("allUserSet", new HashSet<>()); //会议公司所有人员
                    meet.put("acceptUserSet", new HashSet<>()); //会议接受人员
                    meet.put("meetRange", DateUtils.format(startTime, "HH:mm") + "~" + DateUtils.format(endTime, "HH:mm"));
                    long minute = (endTime.getTime() - startTime.getTime()) / 1000 / 60; //分钟
                    meet.put("timeNum", minute);
                    meetMap.put(Integer.parseInt(String.valueOf(meet.get("meetId"))), meet);
                }
                Map<String, Object> param1 = new HashMap<>();
                param1.put("meetIdList", meetIds);
                List<MeetingUser> meetingUserList = meetingUserMapper.listUserByParam(param1);
                if(CollectionUtils.isNotEmpty(meetingUserList)){
                    for(MeetingUser meetingUser : meetingUserList){
                        Integer meetId = meetingUser.getMeetId();
                        ((Set<Integer>)meetMap.get(meetId).get("allUserSet")).add(meetingUser.getUserId());
                        if(meetingUser.getAcceptFlag() == 1){
                            ((Set<Integer>)meetMap.get(meetId).get("acceptUserSet")).add(meetingUser.getUserId());
                        }
                    }
                }
            }
            String[] heads = {"会议状态", "会议日期", "会议时间", "会议室", "预订人", "会议室预定时间", "审批人","邀请人数", "接受人数","会议时长（分钟）"};
            String[] fields={"meetState","meetDate","meetRange","roomName","createName","meetRoomCreateTime","auditFlag","allUserNum","acceptNum","timeNum"};
            ExcelUtil.exportExcel("会议室使用详情列表",heads, fields, resultList, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if(value != null){
                    if("meetState".equals(field)){
                        if("-1".equals(String.valueOf(value))){
                            cell.setCellValue("审核驳回");
                        }else if("1".equals(String.valueOf(value))){
                            cell.setCellValue("预约成功");
                        }else {
                            cell.setCellValue("审核中");
                        }
                    } else if("auditFlag".equals(field)){
                        if("1".equals(value.toString())){
                            if(resultList.get(rowIndex).get("approverUserName") != null){
                                cell.setCellValue(resultList.get(rowIndex).get("approverUserName").toString());
                            }else{
                                cell.setCellValue("是");
                            }
                        }else {
                            cell.setCellValue("否");
                        }
                    } else if("allUserNum".equals(field)){
                        Integer allUserNum = Integer.parseInt(String.valueOf(value)) + ((Set<Integer>)resultList.get(rowIndex).get("allUserSet")).size();
                        cell.setCellValue(allUserNum);
                    } else if("acceptNum".equals(field)){
                        Integer acceptNum = Integer.parseInt(String.valueOf(value)) + ((Set<Integer>)resultList.get(rowIndex).get("acceptUserSet")).size();
                        cell.setCellValue(acceptNum);
                    } else if("roomCreateTime".equals(field)){
                        cell.setCellValue(String.valueOf(value));
                    } else{
                        cell.setCellValue(value.toString());
                    }
                }else {
                    cell.setCellValue("");
                }
            });
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "导出文件失败");
        }
    }

    @Override
    public void exportMeetingRoomList(HttpServletResponse response, Map<String, Object> param) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("会议室列表.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录");
            }
            param.put("companyCode", user.getCompanyCode());
            List<Map<String, Object>> resultList = meetingRoomMapper.listMapByParam(param);
            String[] heads = {"会议室名称", "会议室设备", "可容纳人数", "会议室地址", "会议室开启预定", "预定是否需要审批"};
            String[] fields={"name","otherDevice","peopleNum","address","enabled","auditFlag"};
            ExcelUtil.exportExcel("会议室列表",heads, fields, resultList, outputStream,"yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                if(value != null){
                    if("peopleNum".equals(field)){
                        cell.setCellValue(value.toString()+"人");
                    } else if("enabled".equals(field)){
                        if("1".equals(value.toString())){
                            cell.setCellValue("否");
                        }else {
                            cell.setCellValue("是");
                        }
                    } else if("auditFlag".equals(field)){
                        if("1".equals(value.toString())){
                            if(resultList.get(rowIndex).get("approverUserName") != null){
                                cell.setCellValue(resultList.get(rowIndex).get("approverUserName").toString());
                            }else{
                                cell.setCellValue("是");
                            }
                        }else {
                            cell.setCellValue("否");
                        }
                    }else{
                        cell.setCellValue(value.toString());
                    }
                }else {
                    cell.setCellValue("");
                }
            });
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "导出文件失败");
        }
    }

    @Override
    public List<MeetingRoom> listMeetingRoom() {
        List<MeetingRoom> meetingRoomList = new ArrayList<>();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                meetingRoomList = meetingRoomMapper.listMeetingRoomByCompanyCode(user.getCompanyCode());
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return meetingRoomList;
    }

    //校验新增
    private void validateSave(MeetingRoom meetingRoom){
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        //校验非空字段判断
        if(StringUtils.isEmpty(meetingRoom.getName())){
            throw new QinFeiException(1002, "请输入会议室名称！");
        }
        if(StringUtils.isEmpty(meetingRoom.getAddress())){
            throw new QinFeiException(1002, "请输入会议室地址！");
        }
        if(meetingRoom.getPeopleNum() == null){
            throw new QinFeiException(1002, "请输入会议室可容纳人数！");
        }
        if(meetingRoom.getAuditFlag() != null && meetingRoom.getAuditFlag() == 1 && meetingRoom.getAuditUserId() == null){
            throw new QinFeiException(1002, "请选择会议室审核人！");
        }
        //校验同一个公司会议室名称是否存在
        MeetingRoom room = meetingRoomMapper.getMeetingRoomByName(null, meetingRoom.getName(), user.getCompanyCode());
        if(room != null){
            throw new QinFeiException(1002, "存在该名称的会议室！");
        }
        //设置一些初始值，创建人等
        if(meetingRoom.getMeetUnit() == null){
            meetingRoom.setMeetUnit(30); //默认30分钟为单位
        }
        //校验一次预定时间、开放时间
        validateCommon(meetingRoom);
        meetingRoom.setCompanyCode(user.getCompanyCode());
        meetingRoom.setCreateId(user.getId());
        meetingRoom.setUpdateId(user.getId());
        meetingRoom.setUpdateDate(new Date());
    }

    //校验一次预定时间、开放时间
    private void validateCommon(MeetingRoom meetingRoom){
        String startTime = StringUtils.isNotEmpty(meetingRoom.getOpenTimeStart()) ? meetingRoom.getOpenTimeStart() : "00:00";
        String endTime = StringUtils.isNotEmpty(meetingRoom.getOpenTimeEnd()) ? meetingRoom.getOpenTimeEnd() : "24:00";
        long time = DateUtils.parse("2019-10-23 "+endTime, "yyyy-MM-dd HH:mm").getTime() - DateUtils.parse("2019-10-23 "+startTime, "yyyy-MM-dd HH:mm").getTime();
        if(time <= 0){
            throw new QinFeiException(1002, "每日开放时间范围开始时间应小于结束时间！");
        }
        long  minute = time / 1000 / 60; //转换成分钟
        if((minute - meetingRoom.getMeetUnit()) < 0){
            throw new QinFeiException(1002, "每日开放时间范围应大于最小预约时间！");
        }
        //单次预约最长时间必须小于开放时间范围
        if(meetingRoom.getOnceTime() != null && meetingRoom.getOnceTime() > minute){
            throw new QinFeiException(1002, "单次预约最长时间必须小于开放时间范围！");
        }
    }

    //校验更新
    private void validateUpdate(MeetingRoom meetingRoom){
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(meetingRoom == null || meetingRoom.getId() == null){
            throw new QinFeiException(1002, "数据不存在！");
        }
        if(meetingRoom.getAuditFlag() != null && meetingRoom.getAuditFlag() == 1 && meetingRoom.getAuditUserId() == null){
            throw new QinFeiException(1002, "请选择会议室审核人！");
        }
        //根据ID查询是否存在当前要修改的会议室
        MeetingRoom room = meetingRoomMapper.getMeetingRoomById(meetingRoom.getId());
        if(room == null){
            throw new QinFeiException(1002, "会议室不存在！");
        }
        //校验一次预定时间、开放时间
        validateCommon(meetingRoom);
        //设置一些初始值，更新人等
        meetingRoom.setUpdateId(user.getId());
        meetingRoom.setUpdateDate(new Date());
    }

    //校验时间段是否包含在预约的时间段中
    private boolean validateTimeSlot(Date startTime, Date endTime, List<MeetingRoomApply> meetingRoomApplyList, Map<String, String> map) {
        boolean flag = false; //默认不包含
        if(CollectionUtils.isNotEmpty(meetingRoomApplyList)){
            for(MeetingRoomApply meetingRoomApply : meetingRoomApplyList){
                Date tempStartTime = meetingRoomApply.getStartTime();
                Date tempEndTime = meetingRoomApply.getEndTime();
                //更改时间最小粒度，会影响，需要判断时间有交集，就设置为被预约
                if((tempStartTime.compareTo(startTime) < 0 && tempEndTime.compareTo(startTime) > 0) ||
                    tempStartTime.compareTo(endTime) < 0 && tempEndTime.compareTo(endTime) > 0 ||
                    tempStartTime.compareTo(startTime) >= 0 && tempEndTime.compareTo(endTime) <= 0){
                    flag = true;
                    map.put("applyId", String.valueOf(meetingRoomApply.getId()));//定义预约记录ID，用于前台根据该值获取一个区域
                    map.put("applyUserName", meetingRoomApply.getUserName());//定义预约人姓名
                    map.put("applyUserDeptName", meetingRoomApply.getDeptName());//定义预约人姓名
                    map.put("applyTimeSlot", String.format("%s ~ %s", DateUtils.format(tempStartTime, "HH:mm"), DateUtils.format(tempEndTime, "HH:mm")));//定义预约时间段
                    break;
                }
            }
        }
        return flag;
    }

    //校验会议室删除
    private void validateDel(Integer id){
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        //根据ID查询是否存在当前要修改的会议室
        MeetingRoom room = meetingRoomMapper.getMeetingRoomById(id);
        if(room == null){
            throw new QinFeiException(1002, "会议室不存在！");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("companyCode",user.getCompanyCode());
        param.put("meetRoomId", id);
        List<MeetingRoomApply> meetingRoomApplyList = meetingRoomApplyMapper.listApplyMeetingRoomByParam(param);
        if(CollectionUtils.isNotEmpty(meetingRoomApplyList)){
            throw new QinFeiException(1002, "当前会议室还有预约中的时间段，不能进行删除！");
        }
    }

    //计算时间段
    private void calculateTimeSlot(List<MeetingRoomApply> meetingRoomApplyList, MeetingRoom meetingRoom, String applyDate){
        String startTime = StringUtils.isNotEmpty(meetingRoom.getOpenTimeStart()) ? meetingRoom.getOpenTimeStart() : "00:00";
        String endTime = StringUtils.isNotEmpty(meetingRoom.getOpenTimeEnd()) ? meetingRoom.getOpenTimeEnd() : "24:00";


        //计算时间段
        Date startTime1 = DateUtils.parse(applyDate+" "+startTime, "yyyy-MM-dd HH:mm");
        Date endTime1 = DateUtils.parse(applyDate+" "+endTime, "yyyy-MM-dd HH:mm");
        int num = (int) ((endTime1.getTime() - startTime1.getTime()) / 1000 / 60 / meetingRoom.getMeetUnit());
        if(num > 0){
            List<Map<String, String>> result = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime1);
            // 超前时间的 calendar类
            Calendar aheadCalendar = null;
            if(meetingRoom.getApplyRange() != null){
                Date currentDate = DateUtils.parse(DateUtils.format(new Date(),"yyyy-MM-dd"), "yyyy-MM-dd");
                aheadCalendar = Calendar.getInstance();
                aheadCalendar.setTime(currentDate); //排除时分秒影响
                aheadCalendar.add(Calendar.DAY_OF_MONTH, (meetingRoom.getApplyRange() + 1));//当前时间加上最大提前天数 + 1进行判断
            }
            for(int i = 0; i < num; i++){
                Date tempStartTime = calendar.getTime(); //最小粒度开始时间
                String start = DateUtils.format(tempStartTime, "HH:mm");
                String hour = DateUtils.format(calendar.getTime(), "H");
                String wholeTimeFlag = calendar.get(Calendar.MINUTE) == 0 ? "1" : "0"; //是否整点：1-整点， 0-非整点
                String passTimeFlag = calendar.getTime().compareTo(new Date()) > 0 ? "0" : "1"; //是否已过时间：1-已过、0-未过
                calendar.add(Calendar.MINUTE, meetingRoom.getMeetUnit());
                Date tempEndTime = calendar.getTime(); //最小粒度开始时间
                String end = DateUtils.format(tempEndTime, "HH:mm");
                end = "00:00".equals(end) ? "24:00" : end;
                Map<String, String> map = new HashMap<>();
                String applyFlag = validateTimeSlot(tempStartTime, tempEndTime, meetingRoomApplyList, map) ? "1" : "0"; //是否预约：1-已被预约、0-未被预约

                String aheadFlag = aheadCalendar.getTime().compareTo(tempEndTime) <= 0 ? "1" : "0"; // 是否超前：1-以超前、0-未超前

                map.put("startTime", start);
                map.put("endTime", end);
                map.put("hour", hour);
                if((i == num - 1) && "00".equals(end.split(":")[1])){
                    map.put("hour1", end.split(":")[0]);
                    if("1".equals(wholeTimeFlag)){
                        map.put("wholeTimeFlag", "3"); //针对于最后一个专门设置
                    }else {
                        map.put("wholeTimeFlag", "2"); //针对于最后一个专门设置
                    }
                }else{
                    map.put("wholeTimeFlag", wholeTimeFlag);
                }
                map.put("passTimeFlag", passTimeFlag);
                map.put("applyFlag", applyFlag);
                map.put("aheadFlag", aheadFlag);
                result.add(map);
            }
            meetingRoom.setTimeSlotList(result);
        }
    }
}
