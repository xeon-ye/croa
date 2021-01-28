package com.qinfei.qferp.controller.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.VersionHint;
import com.qinfei.qferp.service.sys.IVersionhintService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: VersionHintController
 * @Description: 系统版本提示接口
 * @Author: Xuxiong
 * @Date: 2020/2/3 0003 9:45
 * @Version: 1.0
 */
@Slf4j
@Controller
@RequestMapping("/versionHint")
@Api(description = "系统版本提示接口")
public class VersionHintController {
    @Autowired
    private IVersionhintService versionhintService;

    @PostMapping("save")
    @ResponseBody
    @Verify(code = "/system/versionHintManage", module = "日期管理/日期列表", action = "1")
    @ApiOperation(value = "系统版本管理", notes = "添加系统版本提示")
    public ResponseData save(@RequestBody VersionHint versionHint){
        try{
            versionhintService.save(versionHint);
            return  ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "添加系统版本提示异常！");
        }
    }

    @GetMapping("notice")
    @ResponseBody
    @Verify(code = "/system/versionHintManage", module = "日期管理/日期列表", action = "1")
    @ApiOperation(value = "系统版本管理", notes = "系统版本提示通知")
    public ResponseData notice(@RequestParam("id") Integer id){
        try{
            versionhintService.notice(id);
            return  ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "系统版本提示通知异常！");
        }
    }

    @GetMapping("del")
    @ResponseBody
    @Verify(code = "/system/versionHintManage", module = "日期管理/日期列表", action = "1")
    @ApiOperation(value = "系统版本管理", notes = "删除系统版本提示")
    public ResponseData del(@RequestParam("id") Integer id){
        try{
            versionhintService.del(id);
            return  ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "删除系统版本提示异常！");
        }
    }

    @PostMapping("list")
    @ResponseBody
    @Verify(code = "/system/versionHintManage", module = "日期管理/日期列表", action = "4")
    @ApiOperation(value = "系统版本管理", notes = "系统版本列表")
    public PageInfo<VersionHint> list(@RequestParam Map<String, Object> map, @PageableDefault(size = 10) Pageable pageable){
        return versionhintService.list(map,pageable);
    }

    @GetMapping("listAllVersionHint")
    @ResponseBody
    @ApiOperation(value = "系统版本管理", notes = "获取所有版本提示")
    public Map<String, List<Map<String, Object>>> listAllVersionHint(){
        return versionhintService.listAllVersionHint();
    }

    @PostMapping("historyVersionHint")
    @ResponseBody
    @ApiOperation(value = "系统版本管理", notes = "查询历史版本提示")
    public List<Map<String, Object>> historyVersionHint(@RequestParam Map<String, Object> param){
        return versionhintService.historyVersionHint(param);
    }

    @GetMapping("closeHint")
    @ResponseBody
    @ApiOperation(value = "系统版本管理", notes = "不再提示")
    public ResponseData closeHint(){
        try{
            versionhintService.updateReadFlag();
            return  ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "设置不再提示异常！");
        }
    }

    @GetMapping("listDeptTree")
    @ResponseBody
    @Verify(code = "/system/versionHintManage", module = "日期管理/日期列表", action = "4")
    @ApiOperation(value = "系统版本管理", notes = "部门树")
    public List<Map<String, Object>> listDeptTree(){
        return versionhintService.listDeptTree();
    }
}
