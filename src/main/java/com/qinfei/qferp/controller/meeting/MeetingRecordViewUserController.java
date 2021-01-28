package com.qinfei.qferp.controller.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.meeting.MeetingRecord;
import com.qinfei.qferp.entity.meeting.MeetingTask;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.meeting.IMeetingRecordService;
import com.qinfei.qferp.service.meeting.IMeetingRecordViewUserService;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/meetingRecordViewUser")
public class MeetingRecordViewUserController {
    @Autowired
    private IMeetingRecordViewUserService meetingRecordViewUserService;
    @Autowired
    private IMeetingRecordService meetingRecordService;
    /**
     * 新增会议记录
     */
    @PostMapping("/addMeetingRecord")
    @ResponseBody
    public ResponseData addMeetingRecord(@RequestBody MeetingRecord meetingRecord){
        try{
            meetingRecordViewUserService.addMeetingRecord(meetingRecord);
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", meetingRecord) ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }


    }
    /**
     * 编辑会议记录,点击编辑按钮通会议id查询
     */

    @RequestMapping("/editMeetingRecord")
    @ResponseBody
    public ResponseData editMeetingRecord(@RequestParam("id") Integer id){
        try {
            //通过会议id 查询会议记录实体类
           MeetingRecord meetingRecord= meetingRecordService.getById(id);
            List<User> list = meetingRecordViewUserService.getUserId(meetingRecord.getId());
           ResponseData data = ResponseData.ok();
           data.putDataValue("list",list);
           data.putDataValue("entity",meetingRecord);
           return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }

    /**
     * 删除会议记录，通过会议记录id
     */
    /**
     *
     * @param id
     * @return
     */
    @RequestMapping("/delMeetingRecord")
    @ResponseBody
    public ResponseData delMeetingRecord(@RequestParam("id") Integer id){
        try{
            meetingRecordService.delMeetingRecord(id);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message","操作成功") ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }
//    /**
//     * 编辑会议记录
//     */
//    @RequestMapping("/edit")
//    @Log(opType = OperateType.UPDATE, module = "会议记录/编辑会议记录", note = "公告通知/编辑通知公告")
//
//    @ResponseBody
//    public ResponseData edit (){
//
//    }

    @PostMapping("getMeetingRecordTotal")
    @ResponseBody
    @ApiOperation(value = "会议的记录总数", notes = "会议的记录总数")
    public ResponseData getMeetingRecordTotal(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("total",meetingRecordService.getMeetingRecordTotal(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取会议记录总数异常，请联系技术人员！");
        }
    }

    /**
     * 会议记录查询列表
     */
    @RequestMapping("/meetingRecordListPg")
    @ResponseBody
    public PageInfo<MeetingRecord> meetingRecordListPg(@RequestParam Map<String, Object> map,@PageableDefault(size = 10) Pageable pageable){
        return meetingRecordService.meetingRecordListPg(map,pageable);
    }
    /**
     * 会议任务日期延期
     */

    @PostMapping("/meetingDelay")
    @ResponseBody
    public ResponseData meetingDelay(@RequestBody MeetingTask meetingTask){
        return meetingRecordService.meetingDelay(meetingTask);
    }


}
