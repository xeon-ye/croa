package com.qinfei.qferp.controller.plan;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserPlan;
import com.qinfei.qferp.service.plan.IUserPlanService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @CalssName UserGroupController
 * @Description 用户计划接口
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:07
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/userPlan")
@Api(description = "用户计划接口")
public class UserPlanController {
    @Autowired
    private IUserPlanService userPlanService;

    @PostMapping("save")
    @ApiOperation(value = "新增计划", notes = "新增计划")
    @Verify(code = "/plan/planAdd", module = "新增计划/新增计划", action = "4")
    @ResponseBody
    public ResponseData getEditMediaById(@RequestBody UserPlan userPlan){
        try{
            userPlanService.save(userPlan);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, e.getMessage());
        }
    }

    @PostMapping("listPlanByCurrentUser")
    @ApiOperation(value = "个人计划列表", notes = "个人计划列表")
    @Verify(code = "/plan/planList", module = "个人计划列表/个人计划列表", action = "4")
    @ResponseBody
    public PageInfo<UserPlan> listPlanByCurrentUser(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return userPlanService.listPlanByCurrentUser(map, pageable);
    }

    @PostMapping("getTotalByUserId")
    @ApiOperation(value = "获取指定用户计划列表", notes = "获取指定用户计划列表")
    @Verify(code = "/plan/planList", module = "获取指定用户计划列表/获取指定用户计划列表", action = "4")
    @ResponseBody
    public ResponseData getTotalByUserId(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("total", userPlanService.getTotalByUserId(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "个人计划查询，获取合计异常！");
        }
    }

    @PostMapping("listPlanByParam")
    @ApiOperation(value = "计划管理列表", notes = "计划管理列表")
    @Verify(code = "/plan/planManage", module = "计划管理列表/计划管理列表", action = "4")
    @ResponseBody
    public PageInfo<UserPlan> listPlanByParam(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return userPlanService.listPlanByParam(map, pageable);
    }

    @PostMapping("getTotalByParam")
    @ApiOperation(value = "计划管理合计", notes = "计划管理合计")
    @Verify(code = "/plan/planManage", module = "计划管理列表/计划管理列表", action = "4")
    @ResponseBody
    public ResponseData getTotalByParam(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("total", userPlanService.getTotalByParam(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "计划管理，获取合计异常！");
        }
    }

    @PostMapping("listPlanStatisticsByParam")
    @ApiOperation(value = "计划统计列表", notes = "计划统计列表")
    @Verify(code = "/plan/planStatistics", module = "计划统计列表/计划统计列表", action = "4")
    @ResponseBody
    public PageInfo<UserPlan> listPlanStatisticsByParam(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return userPlanService.listPlanStatisticsByParam(map, pageable);
    }

    @PostMapping("getStatisticsTotalByParam")
    @ApiOperation(value = "计划统计合计", notes = "计划统计合计")
    @Verify(code = "/plan/planStatistics", module = "计划统计列表/计划统计列表", action = "4")
    @ResponseBody
    public ResponseData getStatisticsTotalByParam(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("total", userPlanService.getStatisticsTotalByParam(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "计划统计，获取合计异常！");
        }
    }

    @PostMapping("listUserGroupByParam")
    @ApiOperation(value = "用户群组列表", notes = "用户群组列表")
    @ResponseBody
    public List<UserGroup> listUserGroupByParam(@RequestParam Map<String, Object> map){
        return userPlanService.listUserGroupByParam(map);
    }

    @PostMapping("listNotEnterPlanUserByParam")
    @ApiOperation(value = "获取未填写计划人员", notes = "获取未填写计划人员")
    @Verify(code = "/plan/planNotEnterList", module = "获取未填写计划人员/获取未填写计划人员", action = "4")
    @ResponseBody
    public PageInfo<Map<String, Object>> listNotEnterPlanUserByParam(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return userPlanService.listNotEnterPlanUserByParam(map, pageable);
    }

    @GetMapping("exportPlanByUserId")
    @Verify(code = "/plan/planList", module = "个人计划列表/导出计划列表", action = "4")
    @ApiOperation(value = "导出计划列表", notes = "导出计划列表")
    public void exportPlanByUserId(HttpServletResponse response,@RequestParam Map<String, Object> map){
        try {
            String fileName = "个人计划总结列表";
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName+".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            userPlanService.exportPlanByUserId(map,outputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping("exportPlanByParam")
    @Verify(code = "/plan/planManage", module = "计划管理/导出计划列表", action = "4")
    @ApiOperation(value = "计划管理-导出计划列表", notes = "计划管理-导出计划列表")
    public void exportPlanByParam(HttpServletResponse response,@RequestParam Map<String, Object> map){
        try {
            String fileName = "计划总结列表";
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName+".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            userPlanService.exportPlanByParam(map,outputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping("exportPlanStatisticsByParam")
    @Verify(code = "/plan/planStatistics", module = "计划统计/导出计划列表", action = "4")
    @ApiOperation(value = "计划统计-导出计划列表", notes = "计划统计-导出计划列表")
    public void exportPlanStatisticsByParam(HttpServletResponse response,@RequestParam Map<String, Object> map){
        try {
            String fileName = "计划总结统计列表";
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName+".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            userPlanService.exportPlanStatisticsByParam(map,outputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping("exportNotEnterPlanByParam")
    @Verify(code = "/plan/planNotEnterList", module = "未填写计划/导出未填写计划列表", action = "4")
    @ApiOperation(value = "未填写计划-导出未填写计划列表", notes = "未填写计划-导出未填写列表")
    public void exportNotEnterPlanByParam(HttpServletResponse response,@RequestParam Map<String, Object> map){
        try {
            String fileName = "未填写计划总结";
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName+".xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            userPlanService.exportNotEnterPlanByParam(map,outputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @PostMapping("listUserSummaryRanking")
    @ApiOperation(value = "计划总结排名", notes = "人员排名")
    @ResponseBody
    public ResponseData listUserSummaryRanking(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("result", userPlanService.listUserSummaryRanking(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "计划总结排名，人员排名异常！");
        }
    }

    @PostMapping("listDeptSummary")
    @ApiOperation(value = "计划总结排名", notes = "部门排名")
    @ResponseBody
    public ResponseData listDeptSummary(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("result", userPlanService.listDeptSummary(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "计划总结排名，部门排名异常！");
        }
    }

    @PostMapping("getPlanPermission")
    @ResponseBody
    @ApiOperation(value = "培训功能", notes = "获取当前用户培训功能权限")
    public Map<String, Object> getPlanPermission(HttpServletRequest request){
        return userPlanService.getPlanPermission(request);
    }
}
