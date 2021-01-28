package com.qinfei.qferp.controller.performance;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.performance.PerformanceScheme;
import com.qinfei.qferp.service.impl.performance.PerformanceSchemeService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/performanceScheme")
class PerformanceSchemeController {
    @Autowired
    private PerformanceSchemeService performanceSchemeService;

    /**
     * 添加方案
     */
    @PostMapping
    @ApiOperation(value = "添加方案", notes = "添加方案")
//    @Log(opType = OperateType.ADD, module = "添加方案", note = "添加方案")
    @ResponseBody
    public ResponseData add(PerformanceScheme scheme) {
        ResponseData data = ResponseData.ok();
        performanceSchemeService.save(scheme);
        return data;
    }

    /**
     * 拷贝方案
     */
    @PostMapping("copy")
    @ApiOperation(value = "拷贝方案", notes = "拷贝方案")
//    @Log(opType = OperateType.ADD, module = "拷贝方案", note = "拷贝方案")
    @ResponseBody
    public ResponseData copy(@Param("schId") Integer schId) {
        ResponseData data = ResponseData.ok();
        performanceSchemeService.copy(schId);
        return data;
    }

    /**
     * 根据方案id查询
     */
    @GetMapping
    @ApiOperation(value = "根据方案id查询", notes = "根据方案id查询")
//    @Log(opType = OperateType.QUERY, module = "根据方案id查询", note = "根据方案id查询")
    @ResponseBody
    public ResponseData getById(@RequestParam Integer id) {
        ResponseData data = ResponseData.ok();
        PerformanceScheme result = performanceSchemeService.selectById(id);
        data.putDataValue("data", result);
        return data;
    }

    /**
     * 根据条件查询排除人员
     */
    @RequestMapping("/listUserByParam")
    @ApiOperation(value = "根据条件查询排除人员", notes = "根据条件查询排除人员")
    @ResponseBody
    public List<Map> listUserByParam(@RequestParam Map map) {
        ResponseData data = ResponseData.ok();
        return performanceSchemeService.listUserByParam(map);
    }

    /**
     * 方案列表
     *
     * @return ：考核细则集合；
     */
    @GetMapping("listPg")
    @ApiOperation(value = "方案列表", notes = "方案列表")
//    @Log(opType = OperateType.QUERY, module = "方案列表", note = "方案列表")
    @ResponseBody
    public PageInfo<PerformanceScheme> listPg(@PageableDefault(size = 15) Pageable pageable, @RequestParam Map map) {
        return performanceSchemeService.listPg(pageable, map);
    }

    /**
     * 查询录入方案中包含的部门信息
     */
    @GetMapping("postInfo")
    @ApiOperation(value = "查询录入方案中包含的部门信息", notes = "查询录入方案中包含的部门信息")
//    @Log(opType = OperateType.QUERY, module = "查询录入方案中包含的部门信息", note = "查询录入方案中包含的部门信息")
    @ResponseBody
    public ResponseData postInfo(@RequestParam("schemeType")Integer schemeType) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("scheme", performanceSchemeService.postInfo(schemeType));
        return data;
    }
}
