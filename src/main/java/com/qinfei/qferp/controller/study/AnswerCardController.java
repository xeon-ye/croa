package com.qinfei.qferp.controller.study;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.AnswerCard;
import com.qinfei.qferp.service.study.IAnswerCardService;
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
import java.util.Map;

/**
 * @CalssName: AnswerCardController
 * @Description: 答题卡接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 19:11
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/answerCard")
@Api(description = "答题卡接口")
public class AnswerCardController {
    @Autowired
    private IAnswerCardService answerCardService;

    @PostMapping("listAnswerCard")
    @ResponseBody
    @ApiOperation(value = "课程管理-课程考试", notes = "阅卷")
    public PageInfo<AnswerCard> listAnswerCard(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
        return  answerCardService.listAnswerCard(param, pageable);
    }

    @PostMapping("save")
    @ApiOperation(value = "答题卡管理", notes = "新增答题卡")
    @ResponseBody
    public ResponseData save(@RequestBody AnswerCard answerCard){
        try{
            answerCardService.save(answerCard);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "提交答题卡异常！");
        }
    }

    @PostMapping("mark")
    @ApiOperation(value = "答题卡管理", notes = "阅卷")
    @ResponseBody
    public ResponseData mark(@RequestBody AnswerCard answerCard){
        try{
            answerCardService.mark(answerCard);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "阅卷异常！");
        }
    }

    @PostMapping("getAnswerCard")
    @ApiOperation(value = "学员考试", notes = "获取学员答题卡")
    @ResponseBody
    public ResponseData getAnswerCard(@RequestParam("paperId") Integer paperId, @RequestParam(name = "studentId", required = false) Integer studentId,
                                      @RequestParam(name = "examFlag", required = false, defaultValue = "false") Boolean examFlag){
        try{
            return ResponseData.ok().putDataValue("result", answerCardService.getAnswerCardByPaperId(paperId, studentId, examFlag));
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "获取学员答题卡异常！");
        }
    }

    @GetMapping("trainPaperAnswerExport")
    @ApiOperation(value = "试卷管理", notes = "考生答题卡列表导出")
    @ResponseBody
    public void trainPaperAnswerExport(HttpServletRequest request, HttpServletResponse response, @RequestParam("paperId") Integer paperId, @RequestParam("courseTitle") String courseTitle, @RequestParam("coursePlate") String coursePlate) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String agent = request.getHeader("user-agent");
            String sheetName = "考生答题卡列表";
            response.setHeader("Content-Disposition", "attachment;fileName=" + DataImportUtil.encodeDownLoadFilename(sheetName, agent) + ".xls");
            OutputStream outputStream = response.getOutputStream();
            answerCardService.trainPaperAnswerExport(outputStream, paperId, courseTitle, coursePlate);
        } catch (IOException e) {
            log.error("导出模板失败", e);
        }
    }
}
