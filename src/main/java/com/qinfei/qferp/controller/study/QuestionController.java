package com.qinfei.qferp.controller.study;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.Question;
import com.qinfei.qferp.service.study.IQuestionService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @CalssName: QuestionController
 * @Description: 题目接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 19:09
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/question")
@Api(description = "题目接口")
public class QuestionController {
    @Autowired
    private IQuestionService questionService;

    @PostMapping("save")
    @ApiOperation(value = "题目管理", notes = "新增题目")
    @ResponseBody
    public ResponseData save(@RequestBody Question question){
        try{
            questionService.save(question);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "创建题目异常！");
        }
    }

    @PostMapping("listQuestion")
    @ResponseBody
    @ApiOperation(value = "添加试卷", notes = "题库选择")
    public PageInfo<Question> listApplyMeetingRoom(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
        return  questionService.listQuestion(param, pageable);
    }
}
