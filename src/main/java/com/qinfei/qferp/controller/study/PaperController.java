package com.qinfei.qferp.controller.study;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.Paper;
import com.qinfei.qferp.service.study.IPaperService;
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
 * @CalssName: PaperController
 * @Description: 试卷接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 19:06
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/paper")
@Api(description = "试卷接口")
public class PaperController {
    @Autowired
    private IPaperService paperService;

    @PostMapping("save")
    @ApiOperation(value = "课程管理-课程考试", notes = "新增试卷")
    @ResponseBody
    public ResponseData save(@RequestBody Paper paper){
        try{
            paperService.save(paper);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "创建试卷异常！");
        }
    }

    @PostMapping("edit")
    @ApiOperation(value = "课程管理-课程考试", notes = "编辑试卷")
    @ResponseBody
    public ResponseData edit(@RequestBody Paper paper){
        try{
            paperService.update(paper);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "编辑试卷异常！");
        }
    }


    @PostMapping("paperState")
    @ApiOperation(value = "课程管理-课程考试", notes = "修改试卷状态")
    @ResponseBody
    public ResponseData enable(@RequestParam("id") Integer id, @RequestParam("state") Byte state){
        try{
            paperService.updateStateById(state, id);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "修改试卷异常！");
        }
    }

    @PostMapping("getCountByParam")
    @ResponseBody
    @ApiOperation(value = "课程管理", notes = "根据参数获取试卷总数")
    public ResponseData getCountByParam(@RequestParam Map<String, Object> map){
        try {
            return ResponseData.ok().putDataValue("total",paperService.getCountByParam(map));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取试卷总数异常，请联系技术人员！");
        }
    }

    @PostMapping("listPaper")
    @ResponseBody
    @ApiOperation(value = "课程管理-课程考试", notes = "试卷列表")
    public PageInfo<Paper> listPaper(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
        return  paperService.listPaper(param, pageable);
    }

    @PostMapping("getPaperDetailById")
    @ResponseBody
    @ApiOperation(value = "课程管理-课程考试", notes = "试卷详情")
    public ResponseData getPaperDetailById(@RequestParam("id") Integer id, @RequestParam(name = "orderFlag", required = false, defaultValue = "false") Boolean orderFlag,
                                           @RequestParam(name = "examFlag", required = false, defaultValue = "false") Boolean examFlag){
        try{
            return ResponseData.ok().putDataValue("result", paperService.getPaperDetailById(id, orderFlag, examFlag));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "获取试卷详情异常！");
        }
    }


    @PostMapping("getUserExamTotal")
    @ResponseBody
    @ApiOperation(value = "课程管理-我的测试", notes = "查询测试的试卷列表")
    public ResponseData getUserExamTotal(@RequestParam Map<String, Object> param){
        try {
            return ResponseData.ok().putDataValue("total",paperService.getUserExamTotal(param));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取测试试卷总数，请联系技术人员！");
        }
    }

    @PostMapping("listUserExam")
    @ResponseBody
    @ApiOperation(value = "课程管理-我的测试", notes = "查询测试的试卷列表")
    public PageInfo<Map<String, Object>> listUserExam(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
        return  paperService.listUserExam(param, pageable);
    }

}
