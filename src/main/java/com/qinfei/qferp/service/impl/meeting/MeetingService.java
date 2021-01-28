package com.qinfei.qferp.service.impl.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.meeting.*;
import com.qinfei.qferp.entity.schedule.UserSchedule;
import com.qinfei.qferp.entity.schedule.UserScheduleRelate;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.meeting.*;
import com.qinfei.qferp.mapper.schedule.UserScheduleMapper;
import com.qinfei.qferp.mapper.schedule.UserScheduleRelateMapper;
import com.qinfei.qferp.service.impl.flow.ProcessService;
import com.qinfei.qferp.service.meeting.IMeetingService;
import com.qinfei.qferp.service.schedule.IUserScheduleService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.flowable.spring.boot.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @CalssName MeetingServiceImpl
 * @Description 会议服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:36
 * @Version 1.0
 */
@Service
@Slf4j
public class MeetingService implements IMeetingService {
    @Autowired
    private MeetingMapper meetingMapper;
    @Autowired
    private MeetingUserMapper meetingUserMapper;
    @Autowired
    private MeetingRoomMapper meetingRoomMapper;
    @Autowired
    private MeetingRecordMapper meetingRecordMapper;
    @Autowired
    private MeetingRoomApplyMapper meetingRoomApplyMapper;
    @Autowired
    private ProcessService processService;
    @Autowired
    private UserScheduleMapper userScheduleMapper;
    @Autowired
    private UserScheduleRelateMapper userScheduleRelateMapper;
    @Autowired
    private IUserScheduleService userScheduleService;

    @Autowired
    private Config config;

    @Override
    public PageInfo<Meeting> meetingAllList(Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        //查询
        List<Meeting> list = meetingMapper.meetingAllList(map);
        List<Integer> meetIds = new ArrayList<>();
        Map<Integer, Set<Integer>> allUserMap = new HashMap<>(); //所有会议人员
        Map<Integer, Set<Integer>> acceptUserMap = new HashMap<>(); //会议接受人员
        for (Meeting meeting: list) {
            meetIds.add(meeting.getId());
            if(meeting.getAuditFlag() == null || meeting.getAuditFlag() != 1){
                meeting.setAuditUserName("否");
            }
            String otherOrganizers = meeting.getOtherOrganizer();
            String otherHosts = meeting.getOtherHost();
            String otherParks = meeting.getOtherPark();
            int otherOrganizerSum =0;
            int otherHostSum=0;
            int otherParkSum=0;
            if(!StringUtils.isEmpty(otherOrganizers)){
                String[] otherOrganizer = otherOrganizers.split(",");
                otherOrganizerSum = otherOrganizer.length;
            }
            if(!StringUtils.isEmpty(otherHosts)){
                String[] otherHost = otherHosts.split(",");
                otherHostSum = otherHost.length;
            }
            if(!StringUtils.isEmpty(otherParks)){
                String[] otherPark = otherParks.split(",");
                otherParkSum = otherPark.length;
            }
            int otherSum = otherOrganizerSum + otherHostSum + otherParkSum;
            meeting.setOtherSum(otherSum);
        }
        Map<String, Object> param1 = new HashMap<>();
        param1.put("meetIdList", meetIds);
        List<MeetingUser> meetingUserList = meetingUserMapper.listUserByParam(param1);
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(meetingUserList)){
            for(MeetingUser meetingUser : meetingUserList){
                Integer meetId = meetingUser.getMeetId();
                if(!allUserMap.containsKey(meetId)){
                    allUserMap.put(meetId, new HashSet<>());
                }
                allUserMap.get(meetId).add(meetingUser.getUserId());
                if(meetingUser.getAcceptFlag() == 1){
                    if(!acceptUserMap.containsKey(meetId)){
                        acceptUserMap.put(meetId, new HashSet<>());
                    }
                    acceptUserMap.get(meetId).add(meetingUser.getUserId());
                }
            }
        }
        for (Meeting meeting: list){
            if(allUserMap.get(meeting.getId()) != null){
                meeting.setOtherSum(meeting.getOtherSum()+allUserMap.get(meeting.getId()).size());
            }
            if(acceptUserMap.get(meeting.getId()) != null){
                meeting.setAcceptSum(acceptUserMap.get(meeting.getId()).size());
            }
        }
        return (PageInfo<Meeting>) new PageInfo(list);
    }

    public PageInfo<Meeting> meetingListPg(Map map, Pageable pageable){
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        User user= AppUtil.getUser();
        map.put("userId",user.getId());
        //查询
        List<Meeting> list = meetingMapper.meetingListPg(map);
        List<Integer> meetIds = new ArrayList<>();
        Map<Integer, Set<Integer>> allUserMap = new HashMap<>(); //所有会议人员
        Map<Integer, Set<Integer>> acceptUserMap = new HashMap<>(); //会议接受人员
        for (Meeting meeting: list) {
            meetIds.add(meeting.getId());
            if(meeting.getAuditFlag() == null || meeting.getAuditFlag() != 1){
                meeting.setAuditUserName("否");
            }
            String otherOrganizers = meeting.getOtherOrganizer();
            String otherHosts = meeting.getOtherHost();
            String otherParks = meeting.getOtherPark();
            int otherOrganizerSum =0;
            int otherHostSum=0;
            int otherParkSum=0;
            if(!StringUtils.isEmpty(otherOrganizers)){
                String[] otherOrganizer = otherOrganizers.split(",");
                otherOrganizerSum = otherOrganizer.length;
            }
            if(!StringUtils.isEmpty(otherHosts)){
                String[] otherHost = otherHosts.split(",");
                otherHostSum = otherHost.length;
            }
            if(!StringUtils.isEmpty(otherParks)){
                String[] otherPark = otherParks.split(",");
                otherParkSum = otherPark.length;
            }
            int otherSum = otherOrganizerSum + otherHostSum + otherParkSum;
            meeting.setOtherSum(otherSum);
        }
        Map<String, Object> param1 = new HashMap<>();
        param1.put("meetIdList", meetIds);
        List<MeetingUser> meetingUserList = meetingUserMapper.listUserByParam(param1);
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(meetingUserList)){
            for(MeetingUser meetingUser : meetingUserList){
                Integer meetId = meetingUser.getMeetId();
                if(!allUserMap.containsKey(meetId)){
                    allUserMap.put(meetId, new HashSet<>());
                }
                allUserMap.get(meetId).add(meetingUser.getUserId());
                if(meetingUser.getAcceptFlag() == 1){
                    if(!acceptUserMap.containsKey(meetId)){
                        acceptUserMap.put(meetId, new HashSet<>());
                    }
                    acceptUserMap.get(meetId).add(meetingUser.getUserId());
                }
            }
        }
        for (Meeting meeting: list){
            if(allUserMap.get(meeting.getId()) != null){
                meeting.setOtherSum(meeting.getOtherSum()+allUserMap.get(meeting.getId()).size());
            }
            if(acceptUserMap.get(meeting.getId()) != null){
                meeting.setAcceptSum(acceptUserMap.get(meeting.getId()).size());
            }
        }
        return (PageInfo<Meeting>) new PageInfo(list);
    }

    @Override
    @Transactional
    public ResponseData addMeeting(Meeting meeting, MultipartFile[] files){
        User user = AppUtil.getUser();
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        try{
            //判断是否有预约会议室 并且 是否需要审批
            MeetingRoomApply meetingRoomApply = null;
            int state = 1; //会议状态，如果有预约信息，则跟随会议室预约表的状态,-1: 审核驳回、1-成功、2-审核中（针对需要审核的会议室）、-9-删除
            if(meeting.getMeetRoomApplyId() != null){
                meetingRoomApply = meetingRoomApplyMapper.getMyMeetingApplyById(meeting.getMeetRoomApplyId());
                if(meetingRoomApply == null){
                    throw new QinFeiException(1002, "会议室预约信息不存在！");
                }
                if(meetingRoomApply.getMeetingRoom() == null){
                    throw new QinFeiException(1002, "预约的会议室不存在！");
                }

                state = meetingRoomApply.getMeetingRoom().getAuditFlag() == 1 ? 2 : 1;
                meeting.setMeetRoomId(meetingRoomApply.getMeetingRoom().getId());
            }
            if (meeting !=null){
                if (meeting.getMeetType() == null){
                    throw new QinFeiException(1002, "未选择会议类型！");
                }
                if (meeting.getMeetSummaryId() ==null){
                    throw new QinFeiException(1002, "未选择会议纪要人员！");
                }
                if (meeting.getInputUserId() ==null){
                    throw new QinFeiException(1002, "未选择会议主持人员！");
                }
                if (meeting.getInputUserId1() ==null){
                    throw new QinFeiException(1002, "未选择会议组织人员！");
                }
                if (meeting.getInputUserId2() ==null){
                    throw new QinFeiException(1002, "未选择会议纪要人员！");
                }
            }
            for (MultipartFile multipartFile : files) {
                if (multipartFile.getSize() > 0) {
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath =getStringData()+ "/meeting/meet/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    fileNames.add(multipartFile.getOriginalFilename());
                    filePaths.add(config.getWebDir() + childPath + fileName);               }
            }
            meeting.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            meeting.setAttachmentName(fileNames.toString().replaceAll("\\[|\\]", ""));
            ResponseData data = ResponseData.ok();
            meeting.setCreateDate(new Date());
            meeting.setCreateId(user.getId());
            meeting.setCompanyCode(user.getCompanyCode());
            meeting.setUpdateDate(new Date());
            meeting.setUpdateId(user.getId());
            meeting.setState(state);
            meetingMapper.addMeeting(meeting);
            //保存会议相关用户
            addMeetingUser(meeting, user);
            //判断会议室是否需要审批
            if(meetingRoomApply != null){
                //更新会议预约表
                MeetingRoomApply meetingRoomApply1 = new MeetingRoomApply();
                meetingRoomApply1.setMeetId(meeting.getId());
                meetingRoomApply1.setId(meetingRoomApply.getId());
                meetingRoomApply1.setUpdateDate(new Date());
                meetingRoomApply1.setStartTime(meeting.getStartTime());
                meetingRoomApply1.setEndTime(meeting.getEndTime());
                meetingRoomApply1.setUpdateId(user.getId());
                meetingRoomApply1.setState(state);
                meetingRoomApplyMapper.updateById(meetingRoomApply1);
                if(meetingRoomApply.getMeetingRoom().getAuditFlag() == 1){
                    //发起流程，流程通过添加日程
                    meetingRoomApply.setMeeting(meeting);
                    processService.addMeetingRoomProcess(meetingRoomApply);
                }else {
                    //添加日程  在会议的30分钟之前发送首页日常
                    addSchedule(meeting);
                }
            }else {
                //添加日程  在会议的30分钟之前发送首页日常
                addSchedule(meeting);
            }
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", meeting);
            return data;
        }catch (IOException e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    //添加会议相关人员
    private void addMeetingUser(Meeting meeting, User user){
        List<MeetingUser> meetingUserList = new ArrayList<>();
        if(meeting.getMeetSummaryId() !=  null){
            MeetingUser meetingUser = bulidMeetingUser(meeting, user);
            meetingUser.setUserId(meeting.getMeetSummaryId());
            meetingUser.setUserType(5);
            meetingUser.setAcceptFlag(1);//会议纪要人员默认接受
            meetingUserList.add(meetingUser);
        }
        if(!CollectionUtils.isEmpty(meeting.getInputUserId())){
            for(Integer userId : meeting.getInputUserId()){
                MeetingUser meetingUser = bulidMeetingUser(meeting, user);
                meetingUser.setUserId(userId);
                meetingUser.setUserType(2);
                meetingUser.setAcceptFlag(1);//会议主持人员默认接受
                meetingUserList.add(meetingUser);
            }
        }
        if(!CollectionUtils.isEmpty(meeting.getInputUserId1())){
            for(Integer userId : meeting.getInputUserId1()){
                MeetingUser meetingUser = bulidMeetingUser(meeting, user);
                meetingUser.setUserId(userId);
                meetingUser.setUserType(1);
                meetingUser.setAcceptFlag(1);//会议组织人员默认接受
                meetingUserList.add(meetingUser);
            }
        }
        if(!CollectionUtils.isEmpty(meeting.getInputUserId2())){
            for(Integer userId : meeting.getInputUserId2()){
                MeetingUser meetingUser = bulidMeetingUser(meeting, user);
                meetingUser.setUserId(userId);
                meetingUser.setUserType(0);
                meetingUser.setAcceptFlag(0);//会议参与人员默认拒绝
                meetingUserList.add(meetingUser);
            }
        }
        if(!CollectionUtils.isEmpty(meetingUserList)){
            meetingUserMapper.saveBatch(meetingUserList);
        }
    }

    //构建会议人员实例
    private MeetingUser bulidMeetingUser(Meeting meeting, User user){
        MeetingUser meetingUser = new MeetingUser();
        meetingUser.setCreateId(user.getId());
        meetingUser.setUpdateId(user.getId());
        meetingUser.setMeetId(meeting.getId());
        return meetingUser;
    }

    //添加日程
    private void addSchedule(Meeting meeting){
        List<Integer> userIdList = new ArrayList<>();
//        userIdList.add(meeting.getMeetSummaryId());
//        userIdList.addAll(meeting.getInputUserId());
//        userIdList.addAll(meeting.getInputUserId1());
//        userIdList.addAll(meeting.getInputUserId2());
        userIdList= meetingUserMapper.getInputUserId(meeting.getId());
        if(!CollectionUtils.isEmpty(userIdList)){
            List<UserSchedule> userScheduleList = new ArrayList<>();
            for(Integer userId : userIdList){
                UserSchedule userSchedule = new UserSchedule();
                userSchedule.setName(meeting.getTitle());
                userSchedule.setIsAllDay(1);
                userSchedule.setStartDate(meeting.getStartTime());
                userSchedule.setEndDate(meeting.getEndTime());
                userSchedule.setRepeatFlag(0);
                userSchedule.setRemindFlag(1);
                userSchedule.setScheduleType(1);
                userSchedule.setOtherReplateId(meeting.getId());
                userSchedule.setJumpTitle("会议管理");
                userSchedule.setJumpUrl("meeting/meetingManage?type=2&id=" +meeting.getId());
                userSchedule.setCreateId(userId);
                userSchedule.setUpdateId(userId);
                userScheduleList.add(userSchedule);
            }
            int row = userScheduleMapper.batchSave(userScheduleList);
            if(row > 0){
                List<UserScheduleRelate> userScheduleRelateList = new ArrayList<>();
                for(UserSchedule userSchedule : userScheduleList){
                    UserScheduleRelate userScheduleRelate = new UserScheduleRelate();
                    userScheduleRelate.setScheduleId(userSchedule.getId());
                    userScheduleRelate.setRepeatDate(new Date(meeting.getStartTime().getTime() - 30*60*1000));
                    userScheduleRelate.setRemindDate(new Date(meeting.getStartTime().getTime() - 30*60*1000));//提前30分钟提醒
                    userScheduleRelate.setCreateId(userSchedule.getCreateId());
                    userScheduleRelate.setUpdateId(userSchedule.getUpdateId());
                    userScheduleRelateList.add(userScheduleRelate);
                }
                userScheduleRelateMapper.batchSave(userScheduleRelateList);
            }
            userScheduleService.todayCachePut(); //刷新日程缓存
        }
    }

    @Override
    @Transactional
    public ResponseData editMeeting(Integer id){
        try {
            ResponseData data = ResponseData.ok();
            Map map = new HashMap();
            Meeting meeting = meetingMapper.editMeeting(id);
            String otherOrganizers = meeting.getOtherOrganizer();
            String otherHosts = meeting.getOtherHost();
            String otherParks = meeting.getOtherPark();
            if (meeting.getRelateMeetId() !=null){
                Meeting meeting1 = meetingMapper.editMeeting(meeting.getRelateMeetId());
                data.putDataValue("relateMeeting",meeting1);
            }
            if(!StringUtils.isEmpty(otherOrganizers)){
                String[] otherOrganizer = otherOrganizers.split(",");
                map.put("otherOrganizer",otherOrganizer);
            }
            if(!StringUtils.isEmpty(otherHosts)){
                String[] otherHost = otherHosts.split(",");
                map.put("otherHost",otherHost);
            }
            if(!StringUtils.isEmpty(otherParks)){
                String[] otherPark = otherParks.split(",");
                map.put("otherPark",otherPark);
            }
            List<MeetingUser> list = meetingUserMapper.getUserId(id);
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", meeting);
            data.putDataValue("list",list);
            data.putDataValue("map",map);
            return data;
        } catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseData delMeeting(Integer id){
        ResponseData data = ResponseData.ok();
        UserSchedule userSchedule = new UserSchedule();
        User user= AppUtil.getUser();
        if (id==null){
            throw new QinFeiException(1002, "没有获取到会议id！");
        }
        Meeting meeting = new Meeting();
        meeting.setId(id);
        meeting.setUpdateId(user.getId());
        meeting.setUpdateDate(new Date());
        meeting.setState(IConst.STATE_DELETE);
        meetingMapper.delMeeting(meeting);
        userSchedule.setUpdateId(user.getId());
        userSchedule.setUpdateDate(new Date());
        userSchedule.setState(IConst.STATE_DELETE);
        userSchedule.setOtherReplateId(id);
        userScheduleMapper.updateSchedule(userSchedule);
        List<Integer> ids = userScheduleMapper.selectSchedule(id);
        if (ids.size()>0){
            userScheduleRelateMapper.updateSchedule(ids,user.getId(),new Date(),IConst.STATE_DELETE);
        }
        userScheduleService.todayCachePut(); //刷新日程缓存
        data.putDataValue("message", "操作成功");
        data.putDataValue("entity", id);
        return data;
    }

    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }
    @Override
    @Transactional
    public ResponseData updateMeeting(Meeting meeting,MultipartFile[] files){
        User user= AppUtil.getUser();
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        try{
            Meeting old = meetingMapper.editMeeting(meeting.getId());
            if (files.length>1){
                //将文件名和路径拼装成字符串
                for (MultipartFile multipartFile : files) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath =getStringData()+ "/meeting/meet/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile.transferTo(destFile);
                        fileNames.add(multipartFile.getOriginalFilename());
                        filePaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
                meeting.setAttachmentName(fileNames.toString().replaceAll("\\[|\\]", ""));
                meeting.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            }else {
                MultipartFile multipartFile = files[0];
                if (multipartFile.getSize() > 0) {//表示上传了新附件
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/meeting/meet/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    fileNames.add(multipartFile.getOriginalFilename());
                    filePaths.add(config.getWebDir() + childPath + fileName);
                    meeting.setAttachmentName(fileNames.toString().replaceAll("\\[|\\]", ""));
                    meeting.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                } else {//表示附件没有变化
                    meeting.setAttachmentName(old.getAttachmentName());
                    meeting.setAttachmentLink(old.getAttachmentLink());
                }
            }
            meetingMapper.updateMeeting(meeting);
            //删除原有用户
            meetingMapper.deleteMeetingUser(meeting.getId());
            //保存会议相关用户
            addMeetingUser(meeting, user);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", meeting);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @Override
    public List<Meeting> meetingorganization(){
        User user = AppUtil.getUser();
        Integer userId= user.getId();
        List<Meeting> meeting= meetingMapper.meetingorganization(userId);
        return meeting;
    }

    @Override
    public ResponseData meetingFlag(Integer id , Integer flag){
        Map<String, Object> map =new HashMap();
        User user = AppUtil.getUser();
        map.put("updateId", user.getId());
        map.put("updateDate",new Date());
        map.put("userId",user.getId());
        map.put("meetId",id);
        map.put("acceptFlag",flag);
        Integer t =  meetingMapper.meetingFlag(map);
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        data.putDataValue("entity", t);
        return data;
    }
    @Override
    public List<User> meetUsers(Map<String, Object> param){
        return meetingUserMapper.meetUsers(param);
    }

    @Override
    public PageInfo<Map<String, Object>> listMeetingByRoomId(Map<String, Object> param, Pageable pageable) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            param.put("companyCode", user.getCompanyCode());
            resultList = meetingMapper.listMeetingByRoomId(param);
            if(!CollectionUtils.isEmpty(resultList)){
                List<Integer> meetIds = new ArrayList<>();
                Map<Integer, Map<String, Object>> meetMap = new HashMap<>();
                for(Map<String, Object> meet : resultList){
                    meetIds.add(Integer.parseInt(String.valueOf(meet.get("meetId"))));
                    meetMap.put(Integer.parseInt(String.valueOf(meet.get("meetId"))), meet);
                    meet.put("acceptUserList", new ArrayList<>()); //公司接受人员列表
                    meet.put("refuseUserList", new ArrayList<>()); //公司拒绝人员列表
                    meet.put("notResponseUserList", new ArrayList<>()); //公司未响应人员列表
                }
                Map<String, Object> param1 = new HashMap<>();
                param1.put("meetIdList", meetIds);
                List<MeetingUser> meetingUserList = meetingUserMapper.listUserByParam(param1);
                if(!CollectionUtils.isEmpty(meetingUserList)){
                    for(MeetingUser meetingUser : meetingUserList){
                        Integer meetId = meetingUser.getMeetId();
                        if(meetingUser.getAcceptFlag() == 1){
                            ((List)meetMap.get(meetId).get("acceptUserList")).add(meetingUser);
                        }else if(meetingUser.getAcceptFlag() == 2){
                            ((List)meetMap.get(meetId).get("refuseUserList")).add(meetingUser);
                        }else {
                            ((List)meetMap.get(meetId).get("notResponseUserList")).add(meetingUser);
                        }
                    }
                }
            }
        }
        return new PageInfo<>(resultList);
    }

    @Override
    public int getMeetingTotal(Map<String, Object> param) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            result = meetingMapper.getMeetingTotal(param);
        }
        return result;
    }




}
