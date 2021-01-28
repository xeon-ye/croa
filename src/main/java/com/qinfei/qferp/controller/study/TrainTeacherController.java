package com.qinfei.qferp.controller.study;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.TrainTeacher;
import com.qinfei.qferp.service.study.ITrainTeacherService;
import com.qinfei.qferp.utils.DataImportUtil;
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainTeacherController
 * @Description: 培训讲师接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 19:09
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/trainTeacher")
@Api(description = "培训讲师接口")
public class TrainTeacherController {
    @Autowired
    private ITrainTeacherService trainTeacherService;

    @PostMapping("save")
    @ApiOperation(value = "讲师管理", notes = "申请讲师")
    @ResponseBody
    public ResponseData save(@RequestBody TrainTeacher trainTeacher){
        try{
            trainTeacherService.save(trainTeacher);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "申请讲师错误，请联系技术人员！");
        }
    }

    @PostMapping("update")
    @ApiOperation(value = "讲师管理", notes = "编辑讲师")
    @ResponseBody
    public ResponseData update(@RequestBody TrainTeacher trainTeacher){
        try{
            trainTeacherService.update(trainTeacher);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "编辑讲师错误，请联系技术人员！");
        }
    }

    @PostMapping("del")
    @ApiOperation(value = "讲师管理", notes = "删除讲师")
    @ResponseBody
    public ResponseData del(@RequestParam("id") Integer id, @RequestParam("teacherId") Integer teacherId){
        try{
            trainTeacherService.del(id, teacherId);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "删除讲师错误，请联系技术人员！");
        }
    }

    @PostMapping("getTrainTeacherTotal")
    @ResponseBody
    @ApiOperation(value = "讲师管理", notes = "根据参数获取培训讲师总数")
    public ResponseData getTrainTeacherTotal(@RequestParam Map<String, Object> param){
        try {
            return ResponseData.ok().putDataValue("total",trainTeacherService.getTrainTeacherTotal(param));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取培训计划总数异常，请联系技术人员！");
        }
    }

    @PostMapping("listTrainTeacher")
    @ResponseBody
    @ApiOperation(value = "讲师管理", notes = "培训讲师列表")
    public PageInfo<TrainTeacher> listTrainTeacher(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
        return  trainTeacherService.listTrainTeacher(param, pageable);
    }

    @PostMapping("listUserTeacher")
    @ResponseBody
    @ApiOperation(value = "讲师管理", notes = "申请讲师列表")
    public  List<Map<String, Object>> listUserNotTeacher(@RequestParam(value = "existsFlag", required = false) Boolean existsFlag){
        return  trainTeacherService.listUserNotTeacher(existsFlag);
    }

    @PostMapping("listUser")
    @ResponseBody
    @ApiOperation(value = "讲师管理", notes = "申请讲师列表")
    public  List<Map<String, Object>> listUser(){
        return  trainTeacherService.listUser();
    }

    @GetMapping("trainTeacherStatisticsExport")
    @ApiOperation(value = "讲师管理", notes = "讲师统计导出")
    @ResponseBody
    public void trainTeacherStatisticsExport(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> param) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = "讲师统计";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            trainTeacherService.trainTeacherStatisticsExport(outputStream, param);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }
}
