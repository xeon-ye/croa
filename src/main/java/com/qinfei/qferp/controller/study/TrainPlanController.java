package com.qinfei.qferp.controller.study;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.TrainPlan;
import com.qinfei.qferp.service.study.ITrainPlanService;
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
 * @CalssName: TrainPlanController
 * @Description: 培训计划接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 19:09
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/trainPlan")
@Api(description = "培训计划接口")
public class TrainPlanController {
    @Autowired
    private ITrainPlanService trainPlanService;

    @PostMapping("save")
    @ApiOperation(value = "培训计划管理", notes = "新增培训计划")
    @ResponseBody
    public ResponseData save(@RequestBody TrainPlan trainPlan){
        try{
            trainPlanService.save(trainPlan);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "新增培训计划错误，请联系技术人员！");
        }
    }

    @PostMapping("update")
    @ApiOperation(value = "培训计划管理", notes = "编辑培训计划")
    @ResponseBody
    public ResponseData update(@RequestBody TrainPlan trainPlan){
        try{
            trainPlanService.update(trainPlan);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "编辑培训计划错误，请联系技术人员！");
        }
    }

    @PostMapping("updateState")
    @ApiOperation(value = "培训计划管理", notes = "编辑培训计划状态")
    @ResponseBody
    public ResponseData update(@RequestParam("state") Byte state, @RequestParam("id") Integer id){
        try{
            trainPlanService.updateStateById(state,id);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "编辑培训计划错误，请联系技术人员！");
        }
    }

    @PostMapping("getTrainPlanTotal")
    @ResponseBody
    @ApiOperation(value = "培训计划管理", notes = "根据参数获取培训计划总数")
    public ResponseData getTrainPlanTotal(@RequestParam Map<String, Object> param){
        try {
            return ResponseData.ok().putDataValue("total",trainPlanService.getTrainPlanTotal(param));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "很抱歉，获取培训计划总数异常，请联系技术人员！");
        }
    }

    @PostMapping("listTrainPlan")
    @ResponseBody
    @ApiOperation(value = "培训计划管理", notes = "培训计划列表")
    public PageInfo<TrainPlan> listTrainPlan(@RequestParam Map<String, Object> param, @PageableDefault(size = 10) Pageable pageable){
        return  trainPlanService.listTrainPlan(param, pageable);
    }
}
