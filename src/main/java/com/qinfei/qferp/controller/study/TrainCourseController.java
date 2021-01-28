package com.qinfei.qferp.controller.study;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.TrainCourse;
import com.qinfei.qferp.entity.study.TrainCourseComment;
import com.qinfei.qferp.entity.study.TrainCourseSign;
import com.qinfei.qferp.service.study.ITrainCourseService;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainCourseController
 * @Description: 培训课程接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 19:09
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/trainCourse")
@Api(description = "培训课程接口")
public class TrainCourseController {
    @Autowired
    private ITrainCourseService trainCourseService;

    @PostMapping("signUp")
    @ApiOperation(value = "培训计划管理", notes = "培训计划报名")
    @ResponseBody
    public ResponseData signUp(@RequestBody TrainCourse trainCourse){
        try{
            trainCourseService.signUp(trainCourse);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "培训报名操作错误，请联系技术人员！");
        }
    }

    @PostMapping("update")
    @ApiOperation(value = "课程管理", notes = "课程修改")
    @ResponseBody
    public ResponseData update(@RequestBody TrainCourse trainCourse){
        try{
            trainCourseService.update(trainCourse);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程修改错误，请联系技术人员！");
        }
    }

    @PostMapping("courseState")
    @ApiOperation(value = "课程管理", notes = "修改课程状态")
    @ResponseBody
    public ResponseData planState(@RequestParam("id") Integer id, @RequestParam("state") Byte state){
        try{
            trainCourseService.updateStateById(state, id);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "修改课程状态异常，请联系技术人员！");
        }
    }

    @PostMapping("auditState")
    @ApiOperation(value = "课程管理", notes = "课程审批")
    @ResponseBody
    public ResponseData auditState(@RequestParam("id") Integer id, @RequestParam("state") Byte state,
                                   @RequestParam(value = "rejectReason", required = false) String rejectReason){
        try{
            trainCourseService.auditState(state, rejectReason, id);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程审批异常，请联系技术人员！");
        }
    }

    @PostMapping("batchAuditState")
    @ApiOperation(value = "课程管理", notes = "课程审批")
    @ResponseBody
    public ResponseData auditState(@RequestParam("ids[]") List<Integer> ids, @RequestParam("state") Byte state,
                                   @RequestParam(value = "rejectReason", required = false) String rejectReason){
        try{
            trainCourseService.batchAuditState(state, rejectReason, ids);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程审批异常，请联系技术人员！");
        }
    }

    @PostMapping("listTrainCourseByTeacher")
    @ApiOperation(value = "课程管理", notes = "课程考试-关联课程列表")
    @ResponseBody
    public List<TrainCourse> listTrainCourseByTeacher(){
        return trainCourseService.listTrainCourseByTeacher();
    }

    @PostMapping("getTrainCourseDeailById")
    @ApiOperation(value = "课程管理", notes = "课程预览")
    @ResponseBody
    public ResponseData getTrainCourseDeailById(@RequestParam("id") Integer id){
        try{
            return ResponseData.ok().putDataValue("trainCourse", trainCourseService.getTrainCourseDeailById(id));
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "获取课程详情异常，请联系技术人员！");
        }
    }

    @PostMapping("listCourseSign")
    @ApiOperation(value = "课程管理", notes = "课程预览-学员管理")
    @ResponseBody
    public ResponseData listCourseSign(@RequestParam("courseId") Integer courseId){
        try{
            return ResponseData.ok().putDataValue("studentList", trainCourseService.listCourseSign(courseId));
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "获取课程学员列表异常，请联系技术人员！");
        }
    }

    @PostMapping("getCourseDetailById")
    @ApiOperation(value = "课程管理", notes = "课程编辑")
    @ResponseBody
    public ResponseData getCourseDetailById(@RequestParam("id") Integer id){
        try{
            return ResponseData.ok().putDataValue("trainCourse", trainCourseService.getCourseDetailById(id));
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "获取课程详情异常，请联系技术人员！");
        }
    }

    @PostMapping("getCourseTotal")
    @ResponseBody
    @ApiOperation(value = "课程管理", notes = "课程管理/课程审批/课程报名")
    public ResponseData getCourseTotal(@RequestParam Map<String, Object> param){
        try {
            return ResponseData.ok().putDataValue("total",trainCourseService.getCourseTotal(param));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取课程管理总数异常，请联系技术人员！");
        }
    }

    @PostMapping("listCourseByParam")
    @ResponseBody
    @ApiOperation(value = "课程管理", notes = "课程管理/课程审批/课程报名")
    public PageInfo<TrainCourse> listCourseByParam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
        return  trainCourseService.listCourseByParam(param, pageable);
    }

    @PostMapping("listCourseRange")
    @ApiOperation(value = "培训计划管理", notes = "课程范围权限")
    @ResponseBody
    public List<Map<String, Object>> listCourseRange(@RequestParam("signStr") String signStr, @RequestParam(name = "name", required = false) String name){
        return trainCourseService.listCourseRange(signStr, name);
    }

    @PostMapping("courseSignUp")
    @ApiOperation(value = "课程管理", notes = "课程报名")
    @ResponseBody
    public ResponseData courseSignUp(@RequestParam("courseId") Integer courseId){
        try{
            trainCourseService.courseSignUp(courseId);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程报名异常，请联系技术人员！");
        }
    }

    @PostMapping("courseVent")
    @ApiOperation(value = "课程管理", notes = "课程吐槽")
    @ResponseBody
    public ResponseData courseVent(@RequestParam("courseId") Integer courseId){
        try{
            trainCourseService.courseVent(courseId);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程吐槽异常，请联系技术人员！");
        }
    }

    @PostMapping("courseLike")
    @ApiOperation(value = "课程管理", notes = "课程点赞")
    @ResponseBody
    public ResponseData courseLike(@RequestParam("courseId") Integer courseId){
        try{
            trainCourseService.courseLike(courseId);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程点赞异常，请联系技术人员！");
        }
    }

    @PostMapping("courseSetScore")
    @ApiOperation(value = "课程管理", notes = "课程评分")
    @ResponseBody
    public ResponseData courseSetScore(@RequestParam("courseId") Integer courseId, @RequestParam("score") Float score){
        try{
            trainCourseService.courseSetScore(courseId, score);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程评分异常，请联系技术人员！");
        }
    }

    @PostMapping("courseSetComment")
    @ApiOperation(value = "课程管理", notes = "课程评论")
    @ResponseBody
    public ResponseData courseSetComment(@RequestBody TrainCourseComment trainCourseComment){
        try{
            trainCourseService.courseSetComment(trainCourseComment);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程评论异常，请联系技术人员！");
        }
    }

    @PostMapping("courseSignState")
    @ApiOperation(value = "课程管理", notes = "修改课程学员状态")
    @ResponseBody
    public ResponseData courseSignState(@RequestBody TrainCourseSign trainCourseSign){
        try{
            return ResponseData.ok().putDataValue("courseSign", trainCourseService.courseSignState(trainCourseSign));
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程学员状态修改异常，请联系技术人员！");
        }
    }

    @PostMapping("stopCourse")
    @ApiOperation(value = "课程管理", notes = "停课")
    @ResponseBody
    public ResponseData stopCourse(@RequestParam("courseId") Integer courseId, @RequestParam("trainStartTime") Date trainStartTime){
        try{
            trainCourseService.stopCourse(courseId, trainStartTime);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程停课异常，请联系技术人员！");
        }
    }

    @PostMapping("recoverCourse")
    @ApiOperation(value = "课程管理", notes = "复课")
    @ResponseBody
    public ResponseData recoverCourse(@RequestParam("courseId") Integer courseId, @RequestParam("trainStartTime") Date trainStartTime){
        try{
            trainCourseService.recoverCourse(courseId, trainStartTime);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "课程复课异常，请联系技术人员！");
        }
    }

    @PostMapping("adminStopCourse")
    @ApiOperation(value = "课程管理", notes = "取消课程")
    @ResponseBody
    public ResponseData adminStopCourse(@RequestParam("courseId") Integer courseId) {
        try {
            trainCourseService.adminStopCourse(courseId);
            return ResponseData.ok();
        } catch (QinFeiException byeException) {
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        } catch (Exception e) {
            return ResponseData.customerError(1002, "取消课程异常，请联系技术人员！");
        }
    }

    @PostMapping("listCourseSignStudent")
    @ApiOperation(value = "课程管理", notes = "报名名单")
    @ResponseBody
    public List<Map<String, Object>> listCourseSignStudent(@RequestParam("courseId") Integer courseId, @RequestParam("viewFlag") Integer viewFlag) {
        return trainCourseService.listCourseSignStudent(courseId, viewFlag);
    }

    @GetMapping("listCourseSignStudentExport")
    @ApiOperation(value = "课程管理", notes = "报名名单导出")
    @ResponseBody
    public void listCourseSignStudentExport(HttpServletRequest request, HttpServletResponse response, @RequestParam("courseId") Integer courseId, @RequestParam("viewFlag") Integer viewFlag) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = "报名名单";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            trainCourseService.listCourseSignStudentExport(outputStream, courseId, viewFlag);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }
}
