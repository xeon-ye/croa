package com.qinfei.qferp.service.impl.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.meeting.Meeting;
import com.qinfei.qferp.entity.meeting.MeetingTask;
import com.qinfei.qferp.entity.meeting.MeetingUser;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.meeting.MeetingTaskMapper;
import com.qinfei.qferp.mapper.meeting.MeetingUserMapper;
import com.qinfei.qferp.service.meeting.IMeetingTaskService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sun.org.apache.regexp.internal.RE;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @CalssName MeetingRecordServiceImpl
 * @Description 会议任务表服务接口
 * @Author xuxiong
 * @Date 2019/10/18 0018 14:36
 * @Version 1.0
 */
@Service
@Slf4j
public class MeetingTaskService implements IMeetingTaskService {
    @Autowired
    private MeetingTaskMapper meetingTaskMapper;
    @Autowired
    private MeetingUserMapper meetingUserMapper;



    /**
     * 会议任务的新增
     */
    @Override
    @Transactional
    public ResponseData addMeetingTask(MeetingTask meetingTask) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            ResponseData data = ResponseData.ok();
            meetingTask.setCreateId(user.getId());
            meetingTask.setUpdateId(user.getId());
            meetingTask.setCompanyCode(user.getCompanyCode());
            meetingTask.setCreateDate(new Date());
            meetingTaskMapper.addMeetingTask(meetingTask);
            List<MeetingUser> meetingUserList = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(meetingTask.getInputUserId())){
                for (Integer userId : meetingTask.getInputUserId()){
                    meetingUserList.add(bulidMeetingUser(meetingTask.getId(), userId, 3, user));
                }
            }
            if(CollectionUtils.isNotEmpty(meetingTask.getInputUserId1())){
                for (Integer userId : meetingTask.getInputUserId1()){
                    meetingUserList.add(bulidMeetingUser(meetingTask.getId(), userId, 4, user));
                }
            }
            if(CollectionUtils.isNotEmpty(meetingUserList)){
                meetingUserMapper.saveBatch(meetingUserList);
            }
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", meetingTask);
            return data;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "添加会议任务失败！");
        }
    }

    //构建会议人员实例
    private MeetingUser bulidMeetingUser(Integer meetTaskId, Integer userId, Integer userType, User user){
        MeetingUser meetingUser = new MeetingUser();
        meetingUser.setAcceptFlag(1); //会议任务都是接受
        meetingUser.setCreateId(user.getId());
        meetingUser.setUpdateId(user.getId());
        meetingUser.setMeetTaskId(meetTaskId);
        meetingUser.setUserId(userId);
        meetingUser.setUserType(userType);
        return meetingUser;
    }

    /**
     * 会议任务列表
     *
     */
    public PageInfo<MeetingTask> meetingTaskListPg(Map map, Pageable pageable){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        List<MeetingTask> list = meetingTaskMapper.meetingTaskListPg(map);
        List<Integer> ids = new ArrayList<>();
        Map<Integer, MeetingTask> existMap = new HashMap<>();
        //判断会议任务是否存在
        if (CollectionUtils.isNotEmpty(list)){
            for (MeetingTask meetingTask : list){
                //将查询到的会议任务id 添加到ids 里
                ids.add(meetingTask.getId());
                //将会议和会议id 封装到map里，以便下面不需判断人员对应哪个会议id
                existMap.put(meetingTask.getId(), meetingTask);
            }
            //通过
            List<MeetingUser> list1 = meetingTaskMapper.meetingUser(ids);
            if(CollectionUtils.isNotEmpty(list1)){
                for (MeetingUser s : list1){
                    Integer meetTaskId = s.getMeetTaskId();
                    Integer userType = s.getUserType();
                    if(userType == 3){
                        existMap.get(meetTaskId).setInputUserName(s.getName());
                        existMap.get(meetTaskId).setUserId(s.getNameId().toString());

                    }else{
                        existMap.get(meetTaskId).setInputUserName1(s.getName());
                        existMap.get(meetTaskId).setUserId1(s.getNameId().toString());

                    }
                }
            }
        }
        return (PageInfo<MeetingTask>) new PageInfo(list);
    }


    public PageInfo<MeetingTask> meMeet(Map map, Pageable pageable){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        List<MeetingTask> list = meetingTaskMapper.meMeet(map);
        List<Integer> ids = new ArrayList<>();
        Map<Integer, MeetingTask> existMap = new HashMap<>();
        //判断会议任务是否存在
        if (CollectionUtils.isNotEmpty(list)){
            for (MeetingTask meetingTask : list){
                //将查询到的会议任务id 添加到ids 里
                ids.add(meetingTask.getId());
                //将会议和会议id 封装到map里，以便下面不需判断人员对应哪个会议id
                existMap.put(meetingTask.getId(), meetingTask);
            }
            //通过
            List<MeetingUser> list1 = meetingTaskMapper.meetingUser(ids);
            if(CollectionUtils.isNotEmpty(list1)){
                for (MeetingUser s : list1){
                    Integer meetTaskId = s.getMeetTaskId();
                    Integer userType = s.getUserType();
                    if(userType == 3){
                        existMap.get(meetTaskId).setInputUserName(s.getName());
                        existMap.get(meetTaskId).setUserId(s.getNameId().toString());

                    }else{
                        existMap.get(meetTaskId).setInputUserName1(s.getName());
                        existMap.get(meetTaskId).setUserId1(s.getNameId().toString());

                    }
                }
            }
        }
        return (PageInfo<MeetingTask>) new PageInfo(list);
    }

    public PageInfo<MeetingTask> duMeet(Map map, Pageable pageable){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        User user = AppUtil.getUser();
        map.put("userId",user.getId());
        List<MeetingTask> list = meetingTaskMapper.duMeet(map);
        List<Integer> ids = new ArrayList<>();
        Map<Integer, MeetingTask> existMap = new HashMap<>();
        //判断会议任务是否存在
        if (CollectionUtils.isNotEmpty(list)){
            for (MeetingTask meetingTask : list){
                //将查询到的会议任务id 添加到ids 里
                ids.add(meetingTask.getId());
                //将会议和会议id 封装到map里，以便下面不需判断人员对应哪个会议id
                existMap.put(meetingTask.getId(), meetingTask);
            }
            //通过
            List<MeetingUser> list1 = meetingTaskMapper.meetingUser(ids);
            if(CollectionUtils.isNotEmpty(list1)){
                for (MeetingUser s : list1){
                    Integer meetTaskId = s.getMeetTaskId();
                    Integer userType = s.getUserType();
                    if(userType == 3){
                        existMap.get(meetTaskId).setInputUserName(s.getName());
                        existMap.get(meetTaskId).setUserId(s.getNameId().toString());

                    }else{
                        existMap.get(meetTaskId).setInputUserName1(s.getName());
                        existMap.get(meetTaskId).setUserId1(s.getNameId().toString());

                    }
                }
            }
        }
        return (PageInfo<MeetingTask>) new PageInfo(list);
    }


    @Override
    public PageInfo<MeetingTask> meetingTask(Integer id,Pageable pageable){
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        //查到會議任务列表
        List<MeetingTask> meetingTasks = meetingTaskMapper.meetingTask(id);
        List<Integer> ids = new ArrayList<>();
        Map<Integer, MeetingTask> existMap = new HashMap<>();
        //判断会议任务是否存在
        if (CollectionUtils.isNotEmpty(meetingTasks)){
            for (MeetingTask meetingTask : meetingTasks){
                //将查询到的会议任务id 添加到ids 里
                ids.add(meetingTask.getId());
                //将会议和会议id 封装到map里，以便下面不需判断人员对应哪个会议id
                existMap.put(meetingTask.getId(), meetingTask);
            }
            //通过
            List<MeetingUser> list = meetingTaskMapper.meetingUser(ids);
            if(CollectionUtils.isNotEmpty(list)){
                for (MeetingUser s : list){
                    Integer meetTaskId = s.getMeetTaskId();
                    Integer userType = s.getUserType();
                    //3是负责人
                    if(userType == 3){
                        existMap.get(meetTaskId).setInputUserName(s.getName());
                        existMap.get(meetTaskId).setUserId(s.getNameId().toString());
                    }else{
                        existMap.get(meetTaskId).setInputUserName1(s.getName());
                        existMap.get(meetTaskId).setUserId1(s.getNameId().toString());

                    }
                }
            }
        }
        return (PageInfo<MeetingTask>) new PageInfo(meetingTasks);
    }

    @Override
    public int getMeetingTaskTotal(Integer meetId) {
        return meetingTaskMapper.getMeetingTaskTotal(meetId);
    }

    public ResponseData getMeetingTask(Integer id){
        try{
            ResponseData data  = ResponseData.ok();
            MeetingTask meetingTask = meetingTaskMapper.getMeetingTask(id);
            List<MeetingUser> list = meetingUserMapper.getMeetintTaskUser(id);
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", meetingTask);
            data.putDataValue("list", list);
            return data;
        } catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }
    @Override
    public ResponseData editBaoMeetTask(MeetingTask meetingTask){
        try{
            ResponseData data  = ResponseData.ok();
            User user = AppUtil.getUser();
            meetingTask.setUpdateId(user.getId());

            meetingTaskMapper.meetingDelay(meetingTask);
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", meetingTask);
            return data;
        }catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }
}
