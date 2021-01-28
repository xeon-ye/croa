package com.qinfei.qferp.controller.study;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.TrainCourseSign;
import com.qinfei.qferp.service.study.ITrainCourseService;
import com.qinfei.qferp.service.study.ITrainStudentService;
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
 * @CalssName: TrainStudentController
 * @Description: 培训学员接口
 * @Author: Xuxiong
 * @Date: 2020/4/17 0017 15:59
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/trainStudent")
@Api(description = "培训学员接口")
public class TrainStudentController {
    @Autowired
    private ITrainStudentService trainStudentService;
    @Autowired
    private ITrainCourseService trainCourseService;

    @PostMapping("listSignUpCourse")
    @ApiOperation(value = "学员管理", notes = "学员课程列表")
    @ResponseBody
    public List<TrainCourseSign> courseSignState(@RequestParam Map<String, Object> param){
        return trainStudentService.listSignUpCourseByParam(param);
    }

    @PostMapping("getStudentTotal")
    @ApiOperation(value = "学员管理", notes = "学员总数")
    @ResponseBody
    public ResponseData getStudentTotal(@RequestParam Map<String, Object> param){
        try {
            return ResponseData.ok().putDataValue("total",trainStudentService.getStudentTotal(param));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取学员总数异常，请联系技术人员！");
        }
    }

    @PostMapping("listStudent")
    @ApiOperation(value = "学员管理", notes = "学员列表")
    @ResponseBody
    public PageInfo<Map<String, Object>> listStudent(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
        return trainStudentService.listStudentByParam(param, pageable);
    }

    @PostMapping("cancelCourseSign")
    @ApiOperation(value = "学员管理", notes = "取消课程报名")
    @ResponseBody
    public ResponseData cancelCourseSign(@RequestParam("signId") Integer signId){
        try {
            trainCourseService.cancelCourseSign(signId);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "取消课程报名异常，请联系技术人员！");
        }
    }

    @GetMapping("trainStudentExport")
    @ApiOperation(value = "学员管理", notes = "学员列表导出")
    @ResponseBody
    public void trainStudentExport(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> param) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = "学员列表";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            trainStudentService.trainStudentExport(outputStream, param);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }
}
