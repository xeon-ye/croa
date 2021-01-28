package com.qinfei.qferp.controller.meeting;


import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.meeting.MeetingRecord;
import com.qinfei.qferp.entity.meeting.MeetingTask;
import com.qinfei.qferp.service.meeting.IMeetingTaskService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.util.Map;

@Controller
@RequestMapping("/meetingTask")
public class MeetingTaskController {
    @Autowired
    private IMeetingTaskService meetingTaskService;


    /**
     * 会议任务（全部）列表
     */
    @RequestMapping("meetingTaskListPg")
    @ResponseBody
    public PageInfo<MeetingTask> meetingTaskListPg(@RequestParam Map map, @PageableDefault()Pageable pageable){
        return meetingTaskService.meetingTaskListPg(map,pageable);
    }

    /**
     * 会议任务（我的任务）列表
     */
    @RequestMapping("meMeet")
    @ResponseBody
    public PageInfo<MeetingTask> meMeet(@RequestParam Map map, @PageableDefault()Pageable pageable){
        return meetingTaskService.meMeet(map,pageable);
    }
    /**
     * 会议任务(督办任务)列表
     */
    @RequestMapping("duMeet")
    @ResponseBody
    public PageInfo<MeetingTask> duMeet(@RequestParam Map map, @PageableDefault()Pageable pageable){
        return meetingTaskService.duMeet(map,pageable);
    }


    /**
     * 会议任务增加
     *
     */
    @PostMapping("addMeetingTask")
    @ResponseBody
    public ResponseData addMeetingTask(@RequestBody MeetingTask meetingTask){
        try {
            return meetingTaskService.addMeetingTask(meetingTask);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，添加会议任务异常，请联系技术人员！");
        }
    }

    /**
     * 根据会议id 查询会议任务
     *
     */
    @RequestMapping("/meetingTask")
    @ResponseBody
    public PageInfo<MeetingTask> meetingTask(Integer id, @PageableDefault(size = 10) Pageable pageable){
        return meetingTaskService.meetingTask(id,pageable);
    }

    @PostMapping("getMeetingTaskTotal")
    @ResponseBody
    @ApiOperation(value = "会议任务总数", notes = "会议任务总数")
    public ResponseData getMyMeetingApplyTotal(@RequestParam Integer id){
        try {
            return ResponseData.ok().putDataValue("total",meetingTaskService.getMeetingTaskTotal(id));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取会议任务总数异常，请联系技术人员！");
        }
    }
    /**
     *
     * 编辑 会议任务、获取会议任务
     */
    @RequestMapping("/getMeetingTask")
    @ResponseBody
    public ResponseData getMeetingTask(Integer id){
        return meetingTaskService.getMeetingTask(id);
    }
    /***
     * 会议任务编辑保存
     */
    @PostMapping("/editBaoMeetTask")
    @ResponseBody
    public ResponseData editBaoMeetTask(@RequestBody MeetingTask meetingTask){
        return  meetingTaskService.editBaoMeetTask(meetingTask);
    }

}
