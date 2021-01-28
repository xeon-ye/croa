package com.qinfei.qferp.controller.performance;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.service.dto.PlateHistoryOutDto;
import com.qinfei.qferp.service.impl.performance.PerformanceHistoryService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/performanceHistory")
class PerformanceHistoryController {

    @Autowired
    private PerformanceHistoryService performanceHistoryService;

    /**
     * 根据方案id查询
     */
    @GetMapping("plate")
    @ApiOperation(value = "根据方案id查询关联的考核准则", notes = "根据方案id查询关联的考核准则")
//    @Log(opType = OperateType.QUERY, module = "根据方案id查询关联的考核准则", note = "根据方案id查询关联的考核准则")
    @ResponseBody
    public ResponseData getPlateById(@RequestParam Integer plateId, @RequestParam Integer schId, @RequestParam Integer plateLevel) {
        ResponseData data = ResponseData.ok();
        List<PlateHistoryOutDto> result = performanceHistoryService.selectChild(plateId, schId);
        data.putDataValue("plates", result);
        data.putDataValue("parent", performanceHistoryService.selectBySchIdAndPlateId(plateId, schId, plateLevel));
        return data;
    }

    /**
     * 根据方案id查询所有绩效模板
     */
    @GetMapping("listPlate")
    @ApiOperation(value = "根据方案id查询所有绩效模板", notes = "根据方案id查询所有绩效模板")
//    @Log(opType = OperateType.QUERY, module = "根据方案id查询所有绩效模板", note = "根据方案id查询所有绩效模板")
    @ResponseBody
    public ResponseData listPlate(@RequestParam Integer schId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("data", performanceHistoryService.listPlate(schId));
        return data;
    }

    /**
     * 删除方案
     */
    @DeleteMapping
    @ApiOperation(value = "删除方案", notes = "删除方案")
//    @Log(opType = OperateType.DELETE, module = "删除方案", note = "删除方案")
    @ResponseBody
    public ResponseData del(@RequestParam Integer schId) {
        ResponseData data = ResponseData.ok();
        String message = performanceHistoryService.del(schId);
        data.putDataValue("message",message);
        return data;
    }
}
