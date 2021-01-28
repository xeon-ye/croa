package com.qinfei.qferp.controller.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.meeting.Meeting;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.meeting.IMeetingService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/meeting")
public class MeetingController {
    @Autowired
    private IMeetingService meetingService;


    /**
     * 会议列表查询
     */
    @RequestMapping("/meetingListPg")
    @ResponseBody
    public PageInfo<Meeting> meetingListPg(@RequestParam Map map,@PageableDefault() Pageable pageable){
        return meetingService.meetingListPg(map,pageable);
    }

    @RequestMapping("/meetingAllList")
    @ResponseBody
    public PageInfo<Meeting> meetingAllList (@RequestParam Map map,@PageableDefault() Pageable pageable){

        return meetingService.meetingAllList(map,pageable);
    }

    /**
     *
     * @param map
     * @param pageable
     * @return
     */
    @PostMapping("listMeetingByRoomId")
    @ApiOperation(value = "查询会议列表", notes = "查询会议列表")
    @ResponseBody
    public PageInfo<Map<String, Object>> listMeetingByRoomId(@RequestParam Map<String, Object> map,@PageableDefault() Pageable pageable){
        return meetingService.listMeetingByRoomId(map, pageable);
    }

    @PostMapping("getMeetingTotal")
    @ApiOperation(value = "查询会议总数", notes = "查询会议总数")
    @ResponseBody
    public ResponseData meetingListPg(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("total",meetingService.getMeetingTotal(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取会议总数异常，请联系技术人员！");
        }
    }

    /**
     * 新增会议
     */
    @PostMapping("/addMeeting")
    @ResponseBody
    public ResponseData addMeeting( Meeting meeting, @RequestParam(value = "attachment",required = false) MultipartFile[] attachment){
        return meetingService.addMeeting(meeting,attachment);
    }
    /**
     * 编辑获取会议详情,根据会议id
     */
    @RequestMapping("/editMeeting")
    @ResponseBody
    public ResponseData editMeeting(Integer id){
        return meetingService.editMeeting(id);
    }

    /**
     * 删除会议
     */
    @RequestMapping("/delMeeting")
    @ResponseBody
    public ResponseData delMeeting (Integer id){
        return meetingService.delMeeting(id);
    }

    /**
     * 编辑保存
     *
     */
    @RequestMapping("/updateMeeting")
    @ResponseBody
    public  ResponseData updateMeeting(Meeting meeting, @RequestParam(value = "attachment",required = false) MultipartFile[] files){
        return meetingService.updateMeeting(meeting ,files);
    }

    /**
     * 关联会议  查询组织者为自己的会议
     */
    @RequestMapping("/meetingorganization")
    @ResponseBody
    public List<Meeting> meetingorganization (){
        return meetingService.meetingorganization();

    }

    /**
     *
     * @param id 会议id
     * @param flag  操作 1是接受  2 不接受   0 是未响应
     * @return
     */
    @RequestMapping("/meetingFlag")
    @ResponseBody
    public ResponseData meetingFlag(Integer id, Integer flag){
       return  meetingService.meetingFlag(id,flag);
    }


    /**
     * @param param
     * @return
     */
    @RequestMapping("/meetUsers")
    @ResponseBody
    public List<User> meetUsers(@RequestParam Map<String, Object> param){
        return meetingService.meetUsers(param);
    }
}
