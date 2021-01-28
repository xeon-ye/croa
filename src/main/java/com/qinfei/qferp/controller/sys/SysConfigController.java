package com.qinfei.qferp.controller.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.SysConfig;
import com.qinfei.qferp.service.sys.ISysConfigService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: SysConfigController
 * @Description: 系统配置参数功能接口
 * @Author: Xuxiong
 * @Date: 2020/2/3 0003 9:45
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/sysConfig")
@Api(description = "系统版本提示接口")
public class SysConfigController {
    @Autowired
    private ISysConfigService sysConfigService;

    @PostMapping("save")
    @ResponseBody
//    @Verify(code = "/system/configManage", module = "系统配置管理/配置参数添加", action = "1")
    @ApiOperation(value = "系统配置管理", notes = "配置参数添加")
    public ResponseData save(@RequestBody SysConfig sysConfig){
        try{
            return ResponseData.ok().putDataValue("data", sysConfigService.save(sysConfig));
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "添加系统配置参数错误！");
        }
    }

    @PostMapping("update")
    @ResponseBody
//    @Verify(code = "/system/configManage", module = "系统配置管理/配置参数修改", action = "1")
    @ApiOperation(value = "系统配置管理", notes = "配置参数修改")
    public ResponseData update(@RequestBody SysConfig sysConfig){
        try{
            sysConfigService.update(sysConfig);
            return  ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "修改系统配置参数错误！");
        }
    }

    @GetMapping("enable")
    @ResponseBody
//    @Verify(code = "/system/configManage", module = "系统配置管理/配置参数启用", action = "1")
    @ApiOperation(value = "系统配置管理", notes = "配置参数启用")
    public ResponseData enable(@RequestParam("id") Integer id){
        try{
            sysConfigService.updateStateById(id, (byte) 0);
            return  ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "系统配置参数启用错误！");
        }
    }

    @GetMapping("disable")
    @ResponseBody
//    @Verify(code = "/system/configManage", module = "系统配置管理/配置参数禁用", action = "1")
    @ApiOperation(value = "系统配置管理", notes = "配置参数禁用")
    public ResponseData disable(@RequestParam("id") Integer id){
        try{
            sysConfigService.updateStateById(id, (byte) 1);
            return  ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "系统配置参数禁用错误！");
        }
    }

    @GetMapping("del")
    @ResponseBody
//    @Verify(code = "/system/configManage", module = "系统配置管理/配置参数删除", action = "1")
    @ApiOperation(value = "系统配置管理", notes = "配置参数删除")
    public ResponseData del(@RequestParam("id") Integer id){
        try{
            sysConfigService.updateStateById(id, (byte) -9);
            return  ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "系统配置参数删除错误！");
        }
    }

    @PostMapping("list")
    @ResponseBody
    @Verify(code = "/system/configManage", module = "系统配置管理/配置参数列表", action = "1")
    @ApiOperation(value = "系统配置管理", notes = "配置参数列表")
    public PageInfo<SysConfig> list(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return sysConfigService.list(map, pageable);
    }

    @GetMapping("getAllConfig")
    @ResponseBody
    @ApiOperation(value = "系统配置管理", notes = "获取系统所有启用配置")
    public Map<String, Map<String, Object>> getAllConfig() {
        return sysConfigService.getAllConfig();
    }

    @GetMapping("listTableData")
    @ResponseBody
    @ApiOperation(value = "系统配置管理", notes = "根据配置类型获取配置值可选列表")
    public List<Map<String, Object>> listTableData(@RequestParam("configType") String configType) {
        return sysConfigService.listTableData(configType);
    }

    @GetMapping("getOneConfigByKey")
    @ResponseBody
    @ApiOperation(value = "系统配置管理", notes = "根据配置项键获取配置项值")
    public SysConfig getOneConfigByKey(@RequestParam("configKey") String configKey) {
        return sysConfigService.getOneConfigByKey(configKey);
    }

}
