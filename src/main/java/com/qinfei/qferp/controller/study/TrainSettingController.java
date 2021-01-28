package com.qinfei.qferp.controller.study;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.TrainSetting;
import com.qinfei.qferp.service.study.ITrainSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: QuestionController
 * @Description: 培训设置接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 19:09
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/trainSetting")
@Api(description = "培训设置接口")
public class TrainSettingController {
    @Autowired
    private ITrainSettingService trainSettingService;

    @PostMapping("save")
    @ApiOperation(value = "培训设置", notes = "新增设置")
    @ResponseBody
    public ResponseData save(@RequestBody TrainSetting trainSetting){
        try{
            trainSettingService.save(trainSetting);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "新增设置异常！");
        }
    }

    @PostMapping("update")
    @ApiOperation(value = "培训设置", notes = "编辑设置")
    @ResponseBody
    public ResponseData update(@RequestBody TrainSetting trainSetting){
        try{
            trainSettingService.update(trainSetting);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "编辑设置异常！");
        }
    }

    @PostMapping("updateState")
    @ApiOperation(value = "培训设置", notes = "编辑设置状态")
    @ResponseBody
    public ResponseData update(@RequestParam("state") Byte state, @RequestParam("id") Integer id){
        try{
            trainSettingService.updateStateById(state,id);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "编辑设置状态异常！");
        }
    }

    @PostMapping("move")
    @ApiOperation(value = "培训设置", notes = "设置顺序移动")
    @ResponseBody
    public ResponseData move(@RequestParam("move") String move, @RequestParam("settingModule") String settingModule,
                             @RequestParam("settingLevel") Integer settingLevel, @RequestParam("id") Integer id){
        try{
            TrainSetting trainSetting = new TrainSetting();
            trainSetting.setId(id);
            trainSetting.setSettingModule(settingModule);
            trainSetting.setSettingLevel(settingLevel);
            if("UP".equals(move)){
                trainSettingService.up(trainSetting);
            }else if("DOWN".equals(move)){
                trainSettingService.down(trainSetting);
            }else if("TOP".equals(move)){
                trainSettingService.top(trainSetting);
            }else {
                trainSettingService.bottom(trainSetting);
            }
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, "设置顺序移动异常！");
        }
    }

    @PostMapping("listTrainSetting")
    @ResponseBody
    @ApiOperation(value = "培训设置", notes = "列表")
    public List<TrainSetting> listTrainSetting(@RequestParam Map<String, Object> param){
        return  trainSettingService.listTrainSetting(param);
    }

    @PostMapping("getMaxSeqTrainSetting")
    @ResponseBody
    @ApiOperation(value = "培训设置", notes = "获取最大序号的配置")
    public TrainSetting getMaxSeqTrainSetting(@RequestParam("settingModule") String settingModule){
        return  trainSettingService.getMaxSeqTrainSetting(settingModule);
    }

    @PostMapping("getTrainPermission")
    @ResponseBody
    @ApiOperation(value = "培训功能", notes = "获取当前用户培训功能权限")
    public Map<String, Object> getTrainPermission(HttpServletRequest request){
        return trainSettingService.getTrainPermission(request);
    }
}
