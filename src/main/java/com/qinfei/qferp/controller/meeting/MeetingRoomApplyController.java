package com.qinfei.qferp.controller.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.meeting.MeetingRoomApply;
import com.qinfei.qferp.service.meeting.IMeetingRoomApplyService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @CalssName MeetingRoomController
 * @Description 会议室预约服务
 * @Author xuxiong
 * @Date 2019/10/22 0022 17:20
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/meetingRoomApply")
@Api(description = "会议室预约接口")
public class MeetingRoomApplyController {
    @Autowired
    private IMeetingRoomApplyService meetingRoomApplyService;

    @PostMapping("save")
    @ResponseBody
    @ApiOperation(value = "新增会议室预约", notes = "新增会议室预约")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/新增会议室预约", action = "4")
    public ResponseData save(@RequestBody MeetingRoomApply meetingRoomApply){
        try {
            return meetingRoomApplyService.save(meetingRoomApply);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，新增会议室预约异常，请联系技术人员！");
        }
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value = "修改会议室预约", notes = "修改会议室预约")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/修改会议室预约", action = "4")
    public ResponseData update(@RequestBody MeetingRoomApply meetingRoomApply){
        try {
            meetingRoomApplyService.update(meetingRoomApply);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，修改会议室预约异常，请联系技术人员！");
        }
    }

    @PostMapping("del")
    @ResponseBody
    @ApiOperation(value = "取消预订", notes = "取消预订")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/取消预订", action = "4")
    public ResponseData del(@RequestParam("id") Integer id){
        try {
            meetingRoomApplyService.del(id);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，取消预订会议室异常，请联系技术人员！");
        }
    }

    @PostMapping("addMeetingPage")
    @ResponseBody
    @ApiOperation(value = "添加会议页面", notes = "添加会议页面")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/添加会议页面", action = "4")
    public ResponseData addMeetingPage(@RequestParam("id") Integer applyId){
        try {
            return meetingRoomApplyService.addMeetingPage(applyId);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，添加会议页面异常，请联系技术人员！");
        }
    }

    @PostMapping("listMeetingApply")
    @ResponseBody
    @ApiOperation(value = "会议室预约列表", notes = "会议室预约列表")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/会议室预约列表", action = "4")
    public PageInfo<MeetingRoomApply> listMeetingApply(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return meetingRoomApplyService.listMeetingApplyByParam(map, pageable);
    }

    @PostMapping("listMyMeetingApply")
    @ResponseBody
    @ApiOperation(value = "我的预约列表", notes = "我的预约列表")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/我的预约列表", action = "4")
    public PageInfo<MeetingRoomApply> listMyMeetingApply(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return meetingRoomApplyService.listMyMeetingApply(map, pageable);
    }

    @PostMapping("getMyMeetingApplyTotal")
    @ResponseBody
    @ApiOperation(value = "我的预约总数", notes = "我的预约总数")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/我的预约总数", action = "4")
    public ResponseData getMyMeetingApplyTotal(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("total",meetingRoomApplyService.getMyMeetingApplyTotal(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取会议室预约总数异常，请联系技术人员！");
        }
    }

    @PostMapping("noticeMeetingUser")
    @ResponseBody
    @ApiOperation(value = "通知参会人员", notes = "通知参会人员")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/通知参会人员", action = "4")
    public ResponseData noticeMeetingUserUrl(@Param("meetApplyId") Integer meetApplyId){
        try {
            return meetingRoomApplyService.noticeMeetingUser(meetApplyId);
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，通知参会人员异常，请联系技术人员！");
        }
    }

    @PostMapping("listMeetingRoomHasApply")
    @ResponseBody
    @ApiOperation(value = "会议室预约记录", notes = "会议室预约记录")
    public PageInfo<Map<String, Object>> listMeetingRoomHasApply(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return meetingRoomApplyService.listMeetingRoomHasApply(map, pageable);
    }

    @PostMapping("getMeetingRoomHasApplyTotal")
    @ResponseBody
    @ApiOperation(value = "会议室预约记录总数", notes = "会议室预约记录总数")
    public ResponseData getMeetingRoomHasApplyTotal(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("total",meetingRoomApplyService.getMeetingRoomHasApplyTotal(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取会议室预约记录总数异常，请联系技术人员！");
        }
    }
}
