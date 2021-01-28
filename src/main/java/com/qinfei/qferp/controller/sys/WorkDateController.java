package com.qinfei.qferp.controller.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.WorkDate;
import com.qinfei.qferp.service.sys.IWorkDateService;
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
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @CalssName WorkDateController
 * @Description 工作日接口
 * @Author xuxiong
 * @Date 2019/10/15 0015 20:14
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/workDate")
@Api(description = "工作日接口")
public class WorkDateController {
    @Autowired
    private IWorkDateService workDateService;

    @PostMapping("initBatchSave")
    @ResponseBody
    @Verify(code = "/system/workDateManage", module = "日期管理/默认日期批量设置", action = "1")
    @ApiOperation(value = "默认日期批量设置", notes = "默认日期批量设置")
    public ResponseData initBatchSave(@RequestBody WorkDate workDate){
        try {
            workDateService.initBatchSave(workDate);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "默认日期批量设置失败！");
        }
    }

    @PostMapping("edit")
    @ResponseBody
    @Verify(code = "/system/workDateManage", module = "日期管理/编辑日期", action = "2")
    @ApiOperation(value = "编辑日期", notes = "编辑日期")
    public ResponseData edit(@RequestBody WorkDate workDate){
        try {
            workDateService.updateById(workDate);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "编辑日期日期失败！");
        }
    }

    @PostMapping("batchEdit")
    @ResponseBody
    @Verify(code = "/system/workDateManage", module = "日期管理/范围编辑日期", action = "2")
    @ApiOperation(value = "范围编辑日期", notes = "范围编辑日期")
    public ResponseData batchEdit(@RequestBody WorkDate workDate){
        try {
            workDateService.batchEdit(workDate);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "范围编辑日期失败！");
        }
    }

    @PostMapping("getWorkDateByDate")
    @ResponseBody
    @ApiOperation(value = "根据日期返回工作日", notes = "根据日期返回工作日")
    public WorkDate getWorkDateByDate(@RequestParam("workDate") String workDate){
        try {
            return workDateService.getWorkDateByDate(workDate);
        }catch (Exception e){
            return null;
        }
    }

    @PostMapping("getCalendar")
    @ResponseBody
    @Verify(code = "/system/workDateManage", module = "日期管理/根据年月返回日历", action = "4")
    @ApiOperation(value = "根据年月返回日历", notes = "根据年月返回日历")
    public List<Map<String, String>> getDate(@RequestParam("year") Integer year, @RequestParam("month") Integer month){
        return  workDateService.getCalendar(year,month);
    }

    @PostMapping("listWorkDate")
    @ResponseBody
    @Verify(code = "/system/workDateManage", module = "日期管理/日期列表", action = "4")
    @ApiOperation(value = "日期列表", notes = "日期列表")
    public PageInfo<WorkDate> listWorkDate(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return workDateService.listByParam(map, pageable);
    }

    @GetMapping("exportWorkDate")
    @Verify(code = "/system/workDateManage", module = "日期管理/导出日期列表", action = "4")
    @ApiOperation(value = "导出日期列表", notes = "导出日期列表")
    public void exportWorkDate(HttpServletResponse response, @RequestParam Map<String, Object> map){
        try {
            response.setContentType("application/binary;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("日期列表.xls", "UTF-8"));
            OutputStream outputStream = response.getOutputStream();
            workDateService.exportWorkDate(map,outputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
