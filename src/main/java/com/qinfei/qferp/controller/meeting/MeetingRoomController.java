package com.qinfei.qferp.controller.meeting;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.meeting.MeetingRoom;
import com.qinfei.qferp.service.meeting.IMeetingRoomService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MeetingRoomController
 * @Description 会议室服务
 * @Author xuxiong
 * @Date 2019/10/22 0022 17:20
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/meetingRoom")
@Api(description = "会议室接口")
public class MeetingRoomController {
    @Autowired
    private IMeetingRoomService meetingRoomService;

    @GetMapping("getMeetingRoomSettingAuth")
    @ResponseBody
    @ApiOperation(value = "获取会议室设置权限", notes = "获取会议室设置权限")
    @Verify(code = "/meetingRoom/setting", module = "会议室管理/获取会议室设置权限", action = "4")
    public ResponseData getMeetingRoomSettingAuth(){
        return ResponseData.ok();
    }

    @PostMapping("save")
    @ResponseBody
    @ApiOperation(value = "新增会议室", notes = "新增会议室")
    @Verify(code = "/meetingRoom/setting", module = "会议室管理/新增会议室", action = "1")
    public ResponseData save(@RequestBody MeetingRoom meetingRoom){
        try {
            meetingRoomService.save(meetingRoom);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，新增会议室异常，请联系技术人员！");
        }
    }

    @PostMapping("update")
    @ResponseBody
    @ApiOperation(value = "修改会议室", notes = "修改会议室")
    @Verify(code = "/meetingRoom/setting", module = "会议室管理/修改会议室", action = "2")
    public ResponseData update(@RequestBody MeetingRoom meetingRoom){
        try {
            meetingRoomService.update(meetingRoom);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，修改会议室异常，请联系技术人员！");
        }
    }

    @PostMapping("del")
    @ResponseBody
    @ApiOperation(value = "删除会议室", notes = "删除会议室")
    @Verify(code = "/meetingRoom/setting", module = "会议室管理/删除会议室", action = "3")
    public ResponseData update(@RequestParam("id") Integer id){
        try {
            meetingRoomService.del(id);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，删除会议室异常，请联系技术人员！");
        }
    }

    @PostMapping("getMeetingRoomById")
    @ResponseBody
    @ApiOperation(value = "获取指定会议室信息", notes = "获取指定会议室信息")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/获取指定会议室信息", action = "4")
    public ResponseData getMeetingRoomById(@RequestParam("id") Integer id){
        try {
            return ResponseData.ok().putDataValue("result",meetingRoomService.getMeetingRoomById(id));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取会议室详情异常，请联系技术人员！");
        }
    }

    @PostMapping("getCountByParam")
    @ResponseBody
    @ApiOperation(value = "根据参数获取会议室总数", notes = "根据参数获取会议室总数")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/根据参数获取会议室总数", action = "4")
    public ResponseData getCountByParam(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("total",meetingRoomService.getCountByParam(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取会议室总数异常，请联系技术人员！");
        }
    }

    @PostMapping("listMeetingRoom")
    @ResponseBody
    @ApiOperation(value = "会议室列表", notes = "会议室列表")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/会议室列表", action = "4")
    public PageInfo<MeetingRoom> listMeetingRoom(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return meetingRoomService.listMeetingRoom(map, pageable);
    }

    @PostMapping("listApplyMeetingRoom")
    @ResponseBody
    @ApiOperation(value = "预约会议室列表", notes = "预约会议室列表")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/预约会议室列表", action = "4")
    public PageInfo<MeetingRoom> listApplyMeetingRoom(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return meetingRoomService.listApplyMeetingRoom(map, pageable);
    }

    @PostMapping("getTimeSlotById")
    @ResponseBody
    @ApiOperation(value = "获取会议室时间段", notes = "获取会议室时间段")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/获取会议室时间段", action = "4")
    public ResponseData getTimeSlotById(@RequestParam("id") Integer id){
        try {
            meetingRoomService.getMeetingTimeSlot(id);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "很抱歉，会议室预约页面异常，请联系技术人员！");
        }
    }

    @GetMapping("exportMeetingList")
//    @Log(opType = OperateType.QUERY, note = "会议室使用详情导出", module = "会议室管理/会议室使用详情导出")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/获取会议室时间段", action = "4")
    public void mediaStatisticsRankingExport(HttpServletResponse response, @RequestParam Map<String, Object> map) throws Exception {
        meetingRoomService.exportMeetingList(response,map);
    }

    @GetMapping("exportMeetingRoomList")
//    @Log(opType = OperateType.QUERY, note = "会议室列表导出", module = "会议室管理/会议室列表导出")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/获取会议室时间段", action = "4")
    public void exportMeetingRoomList(HttpServletResponse response, @RequestParam Map<String, Object> map) throws Exception {
        meetingRoomService.exportMeetingRoomList(response,map);
    }

    @GetMapping("listMeetingRoom")
//    @Log(opType = OperateType.QUERY, note = "会议室列表导出", module = "会议室管理/会议室列表导出")
    @Verify(code = "/meeting/meetingRoomManage", module = "会议室管理/获取会议室时间段", action = "4")
    @ResponseBody
    public List<MeetingRoom> listMeetingRoom(){
        return meetingRoomService.listMeetingRoom();
    }
}
