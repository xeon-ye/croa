package com.qinfei.qferp.service.impl.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.meeting.Meeting;
import com.qinfei.qferp.entity.meeting.MeetingRoom;
import com.qinfei.qferp.entity.meeting.MeetingRoomApply;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.meeting.MeetingMapper;
import com.qinfei.qferp.mapper.meeting.MeetingRoomApplyMapper;
import com.qinfei.qferp.mapper.meeting.MeetingRoomMapper;
import com.qinfei.qferp.mapper.meeting.MeetingUserMapper;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.meeting.IMeetingRoomApplyService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @CalssName MeetingRecordServiceImpl
 * @Description 会议室预约表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:36
 * @Version 1.0
 */
@Service
@Slf4j
public class MeetingRoomApplyService implements IMeetingRoomApplyService {
    @Autowired
    private MeetingRoomMapper meetingRoomMapper;
    @Autowired
    private MeetingRoomApplyMapper meetingRoomApplyMapper;
    @Autowired
    private MeetingMapper meetingMapper;
    @Autowired
    private MeetingUserMapper meetingUserMapper;
    @Autowired
    private IProcessService processService;
    @Autowired
    private IMessageService messageService;

    @Transactional
    @Override
    public ResponseData save(MeetingRoomApply meetingRoomApply) {
        try{
            validateSave(meetingRoomApply); //校验 并 赋值
            meetingRoomApplyMapper.save(meetingRoomApply);
            List<Map<String, String>> timeSlot = calculateTimeSlot(meetingRoomApply.getStartTime(), meetingRoomApply.getEndTime(), meetingRoomApply.getMeetingRoom().getMeetUnit());
            meetingRoomApply.getMeetingRoom().setTimeSlotList(timeSlot);
            return ResponseData.ok().putDataValue("result", meetingRoomApply);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "新增会议室预约失败！");
        }
    }

    @Transactional
    @Override
    public void update(MeetingRoomApply meetingRoomApply) {
        try{
            validateUpdate(meetingRoomApply); //校验 并 赋值
            meetingRoomApplyMapper.updateById(meetingRoomApply);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "修改会议室预约失败！");
        }
    }

    @Transactional
    @Override
    public void del(Integer id) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            MeetingRoomApply roomApply = meetingRoomApplyMapper.getMeetingRoomApplyById(id);
            if(roomApply == null){
                throw new QinFeiException(1002, "会议预约记录不存在！");
            }
            if(roomApply.getMeetId() != null){ //如果有会议则更新会议
                //获取会议，判断是否存在
                Meeting meeting = meetingMapper.getMeetingById(roomApply.getMeetId());
                if(meeting != null){
                    if(roomApply.getAuditFlag() == 1 && roomApply.getState() == 2){
                        processService.withdrawProcess(roomApply.getTaskId(), roomApply.getItemId()); //判断是否有流程，有的话驳回
                    }
                    //更改会议状态
                    meeting.setState(-9);
                    meeting.setUpdateId(user.getId());
                    meetingMapper.updateStateById(meeting);
                    //通知相关人员会议取消
                    Map<String, Object> param = new HashMap<>();
                    param.put("id", meeting.getId());
                    List<User> meetingUserList = meetingUserMapper.meetUsers(param);
                    if(CollectionUtils.isNotEmpty(meetingUserList)){
                        sendMessage(user, meeting, meetingUserList, 2);
                    }
                }
            }
            meetingRoomApplyMapper.updateStateById(id, -9);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "取消预订失败！");
        }
    }

    @Override
    public void updateProcess(MeetingRoomApply meetingRoomApply) {
        try{
            meetingRoomApplyMapper.updateById(meetingRoomApply);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "更新流程任务ID失败！");
        }
    }

    @Override
    public ResponseData addMeetingPage(Integer id) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            MeetingRoomApply meetingRoomApply = meetingRoomApplyMapper.getMyMeetingApplyById(id);
            if(meetingRoomApply == null){
                throw new QinFeiException(1002, "会议室预约信息不存在！");
            }
            List<Map<String, String>> timeSlot = calculateTimeSlot(meetingRoomApply.getStartTime(), meetingRoomApply.getEndTime(), meetingRoomApply.getMeetingRoom().getMeetUnit());
            meetingRoomApply.getMeetingRoom().setTimeSlotList(timeSlot);
            return ResponseData.ok().putDataValue("result", meetingRoomApply);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "添加会议页面失败！");
        }
    }

    @Override
    public PageInfo<MeetingRoomApply> listMeetingApplyByParam(Map<String, Object> param, Pageable pageable) {
        List<MeetingRoomApply> meetingRoomApplyList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            param.put("companyCode", user.getCompanyCode());
            meetingRoomApplyList = meetingRoomApplyMapper.listMeetingApplyByParam(param);
        }
        return new PageInfo<>(meetingRoomApplyList);
    }

    @Override
    public PageInfo<MeetingRoomApply> listMyMeetingApply(Map<String, Object> param, Pageable pageable) {
        List<MeetingRoomApply> meetingRoomApplyList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            param.put("companyCode", user.getCompanyCode());
            param.put("userId", user.getId());
            meetingRoomApplyList = meetingRoomApplyMapper.listMyMeetingApplyByParam(param);
        }
        return new PageInfo<>(meetingRoomApplyList);
    }

    @Override
    public int getMyMeetingApplyTotal(Map<String, Object> param) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            param.put("userId", user.getId());
            result = meetingRoomApplyMapper.getMyApplyCountByParam(param);
        }
        return result;
    }

    @Override
    @Transactional
    public ResponseData noticeMeetingUser(Integer meetApplyId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            MeetingRoomApply roomApply = meetingRoomApplyMapper.getMeetingRoomApplyById(meetApplyId);
            if(roomApply == null){
                throw new QinFeiException(1002, "会议预约记录不存在！");
            }
            if(roomApply.getMeetId() != null){ //如果有会议，则通知，否则没有可通知人员
                //获取会议，判断是否存在
                Meeting meeting = meetingMapper.getMeetingById(roomApply.getMeetId());
                Map<String, Object> param = new HashMap<>();
                param.put("id", meeting.getId());
                List<User> meetingUserList =  meetingUserMapper.meetUsers(param);
                if(CollectionUtils.isNotEmpty(meetingUserList)){
                    sendMessage(user, meeting, meetingUserList, 1);
                }
            }
            return ResponseData.ok();
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "通知会议人员失败！");
        }
    }

    @Override
    public PageInfo<Map<String, Object>> listMeetingRoomHasApply(Map<String, Object> param, Pageable pageable) {
        List<Map<String, Object>> meetingRoomApplyList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            param.put("companyCode", user.getCompanyCode());
            meetingRoomApplyList = meetingRoomApplyMapper.listMeetingRoomHasApplyByParam(param);
        }
        return new PageInfo<>(meetingRoomApplyList);
    }

    @Override
    public int getMeetingRoomHasApplyTotal(Map<String, Object> param) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            result = meetingRoomApplyMapper.getMeetingRoomHasApplyCount(param);
        }
        return result;
    }

    /**
     * 发送消息私用方法
     * @param user 当前登录用户
     * @param meeting 会议实例
     * @param meetingUserList 会议相关的用户列表
     * @param noticeType 通知类型：1-直接通知，2-取消会议通知
     */
    private void sendMessage(User user, Meeting meeting, List<User> meetingUserList, int noticeType){
        List<Message> messagesList = new ArrayList<>();
        for(User tempUser : meetingUserList){
            //通知的时候，把自己去除掉，没必要自己给自己发送消息
            if(user.getId().equals(tempUser.getId())){
                continue;
            }
            String pictureAddress = tempUser.getImage() == null ? "/img/mrtx_2.png" : tempUser.getImage();
            String content = noticeType == 1 ? "[会议通知]会议[%s]记得准时参加" : "[取消会议通知]会议[%s]已取消";
            // 推送WebSocket消息；
            WSMessage message = new WSMessage();
            message.setReceiveUserId(tempUser.getId() + "");
            message.setReceiveName(tempUser.getName());
            message.setSendName(user.getUserName());
            message.setSendUserId(user.getName() + "");
            message.setSendUserImage(pictureAddress);
            message.setContent(String.format(content, meeting.getTitle()));
            WebSocketServer.sendMessage(message);

            Message newMessage = new Message();
            newMessage.setPic(pictureAddress);
            newMessage.setContent(String.format(content, meeting.getTitle()));
            newMessage.setInitiatorDept(user.getDeptId());
            newMessage.setInitiatorWorker(user.getId());
            newMessage.setAcceptWorker(tempUser.getId());
            newMessage.setAcceptDept(tempUser.getDeptId());
            //消息分类
            newMessage.setParentType(3);//通知
            newMessage.setType(23);//会议
            newMessage.setUrl(null);
            newMessage.setUrlName(null);
            messagesList.add(newMessage);
        }
        messageService.batchAddMessage(messagesList);
    }

    //校验保存
    private void validateSave(MeetingRoomApply meetingRoomApply){
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(meetingRoomApply.getMeetRoomId() == null){
            throw new QinFeiException(1002, "请选择会议室！");
        }
        if(meetingRoomApply.getStartTime() == null || meetingRoomApply.getEndTime() == null){
            throw new QinFeiException(1002, "请选择预约时间段！");
        }
        if(meetingRoomApply.getStartTime().compareTo(new Date()) < 0){
            throw new QinFeiException(1002, "预约开始时间小于当前时间！");
        }
        MeetingRoom meetingRoom = meetingRoomMapper.getMeetingRoomById(meetingRoomApply.getMeetRoomId());
        if(meetingRoom == null){
            throw new QinFeiException(1002, "会议室不存在");
        }
        //如果有设置提前多少天预约，则需要判断当前预约日期是否满足要求
        if(meetingRoom.getApplyRange() != null){
            Date currentDate = DateUtils.parse(DateUtils.format(new Date(),"yyyy-MM-dd"), "yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate); //排除时分秒影响
            calendar.add(Calendar.DAY_OF_MONTH, (meetingRoom.getApplyRange() + 1));//当前时间加上最大提前天数 + 1进行判断
            if(calendar.getTime().compareTo(meetingRoomApply.getStartTime()) <= 0){
                throw new QinFeiException(1002, "很抱歉，您预约日期已超过最大提前预约时间！");
            }
        }
        //判断本次预约是否超过单次最长预约时间
        if(meetingRoom.getOnceTime() != null){
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(meetingRoomApply.getStartTime());
            calendar1.add(Calendar.MINUTE, meetingRoom.getOnceTime());
            if(calendar1.getTime().compareTo(meetingRoomApply.getEndTime()) < 0){
                throw new QinFeiException(1002, "很抱歉，您预约时间已超过单次最大预约时间！");
            }
        }
        //判断该时间段是否有时间被预约
        List<MeetingRoomApply> meetingRoomApplyList = meetingRoomApplyMapper.listMeetingApplyByTimeRange(meetingRoomApply.getStartTime(),
                meetingRoomApply.getEndTime(), user.getCompanyCode(), meetingRoomApply.getMeetRoomId(),null);
        if(CollectionUtils.isNotEmpty(meetingRoomApplyList)){
            throw new QinFeiException(1002, "很抱歉，您预约的时间范围内存在被他人预约的时间！");
        }
        //如果会议室需要审核，则需要审核
        if(meetingRoom.getAuditFlag() == 1){
            meetingRoomApply.setAuditFlag(meetingRoom.getAuditFlag());
        }
        //设置默认值
        meetingRoomApply.setCompanyCode(user.getCompanyCode());
        meetingRoomApply.setCreateId(user.getId());
        meetingRoomApply.setCreateDate(new Date());
        meetingRoomApply.setUpdateId(user.getId());
        meetingRoomApply.setUpdateDate(new Date());
        meetingRoomApply.setApproverUserId(meetingRoom.getAuditUserId());
        meetingRoomApply.setMeetingRoom(meetingRoom); //保存会议室信息，后期有用
    }

    //校验更新
    private void validateUpdate(MeetingRoomApply meetingRoomApply){
        if(meetingRoomApply.getId() == null){
            throw new QinFeiException(1002, "数据为空！");
        }
        MeetingRoomApply roomApply = meetingRoomApplyMapper.getMeetingRoomApplyById(meetingRoomApply.getId());
        if(roomApply == null){
            throw new QinFeiException(1002, "会议预约记录不存在！");
        }
        validateSave(meetingRoomApply);
    }

    //计算时间段
    private List<Map<String, String>> calculateTimeSlot(Date startTime, Date endTime, int meetUnit){
        List<Map<String, String>> result = new ArrayList<>();
        //计算时间段
        int num = (int) ((endTime.getTime() - startTime.getTime()) / 1000 / 60 / meetUnit);
        if(num > 0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);
            for(int i = 0; i < num; i++){
                Date tempStartTime = calendar.getTime(); //最小粒度开始时间
                String start = DateUtils.format(tempStartTime, "HH:mm");
                String hour = DateUtils.format(calendar.getTime(), "H");
                String wholeTimeFlag = calendar.get(Calendar.MINUTE) == 0 ? "1" : "0"; //是否整点：1-整点， 0-非整点
                String passTimeFlag = calendar.getTime().compareTo(new Date()) > 0 ? "0" : "1"; //是否已过时间：1-已过、0-未过
                calendar.add(Calendar.MINUTE, meetUnit);
                Date tempEndTime = calendar.getTime(); //最小粒度开始时间
                String end = DateUtils.format(tempEndTime, "HH:mm");
                end = "00:00".equals(end) ? "24:00" : end;
                Map<String, String> map = new HashMap<>();
                map.put("hour", hour);
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
                result.add(map);
            }
        }
        return result;
    }
}
