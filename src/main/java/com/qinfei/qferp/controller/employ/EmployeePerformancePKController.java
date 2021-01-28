package com.qinfei.qferp.controller.employ;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.employ.EmployeePerformancePk;
import com.qinfei.qferp.service.employ.IEmployeePerformancePKService;
import com.qinfei.qferp.service.impl.employ.EmployeePerformancePKPKService;
import com.qinfei.qferp.utils.FileUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * 员工pk管理；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 13:50；
 */
@Controller
@RequestMapping(value = "/employeePerformancePK")
class EmployeePerformancePKController {

    private final Config config;
    private final IEmployeePerformancePKService employeePerformanceService;

    public EmployeePerformancePKController(Config config, EmployeePerformancePKPKService employeePerformancePKService) {
        this.config = config;
        this.employeePerformanceService = employeePerformancePKService;
    }

    /**
     * 上传图片
     *
     * @return ：上传路径；
     */
    @PostMapping("upload")
    @ApiOperation(value = "上传", notes = "上传")
//    @Log(opType = OperateType.ADD, module = "上传", note = "上传")
    @Verify(code = "/employeePerformancePK", module = "员工pk管理", action = "1")
    @ResponseBody
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        String dateDir = DateUtils.format(new Date(), "yyyy/MM/dd/");
        String name = FileUtil.saveFileWithUUID(file, String.format("%s%s%s", config.getUploadDir(), dateDir ,"upload/employeePk/"));
        return String.format("%s%s%s%s", config.getWebDir(), dateDir ,"upload/employeePk/", name);
    }

    /**
     * 保存pk设置
     */
    @PostMapping
    @ApiOperation(value = "保存pk设置", notes = "保存pk设置")
//    @Log(opType = OperateType.ADD, module = "保存pk设置", note = "保存pk设置")
    @Verify(code = "/employeePerformancePK", module = "员工pk管理", action = "1")
    @ResponseBody
    public void save(EmployeePerformancePk employeePerformancePk) {
        employeePerformanceService.save(employeePerformancePk);
    }

    /**
     * 通过id获取
     *
     * @return ：pk设置数据
     */
    @GetMapping
    @ApiOperation(value = "通过id获取", notes = "通过id获取")
//    @Log(opType = OperateType.QUERY, module = "通过id获取", note = "通过id获取")
    @Verify(code = "/employeePerformancePK", module = "员工pk管理", action = "4")
    @ResponseBody
    public ResponseData findById(int id) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("data", employeePerformanceService.findById(id));
        return data;
    }

    /**
     * 列表
     *
     * @return ：pk设置数据
     */
    @GetMapping("all")
    @ApiOperation(value = "列表", notes = "列表")
//    @Log(opType = OperateType.QUERY, module = "列表", note = "列表")
    @Verify(code = "/employeePerformancePK", module = "员工pk管理", action = "4")
    @ResponseBody
    public ResponseData all(@RequestParam Map<String, Object> map) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("data", employeePerformanceService.all(map));
        return data;
    }

    /**
     * pk设置数据带利润列表
     *
     * @return ：pk设置数据带利润
     */
    @GetMapping("view/all")
    @ApiOperation(value = "pk设置数据带利润", notes = "pk设置数据带利润")
//    @Log(opType = OperateType.QUERY, module = "pk设置数据带利润", note = "pk设置数据带利润")
    @Verify(code = "/employeePerformancePK/view", module = "员工pk管理", action = "4")
    @ResponseBody
    public ResponseData allWithProfit(@RequestParam Map<String, Object> map) {
        ResponseData data = ResponseData.ok();
        employeePerformanceService.allWithProfit(data, map);
        return data;
    }

    /**
     * 通过id删除
     */
    @DeleteMapping
    @ApiOperation(value = "通过id删除", notes = "通过id删除")
//    @Log(opType = OperateType.DELETE, module = "通过id删除", note = "通过id删除")
    @Verify(code = "/employeePerformancePK", module = "员工pk管理", action = "3")
    @ResponseBody
    public ResponseData deleteById(int performanceId) {
        employeePerformanceService.deleteById(performanceId);
        return ResponseData.ok();
    }

    /**
     * 拷贝
     */
    @PostMapping("copy")
    @ApiOperation(value = "拷贝", notes = "拷贝")
//    @Log(opType = OperateType.ADD, module = "拷贝", note = "拷贝")
    @Verify(code = "/employeePerformancePK", module = "员工pk管理", action = "1")
    @ResponseBody
    public ResponseData copy(int performanceId) {
        employeePerformanceService.copy(performanceId);
        return ResponseData.ok();
    }

    /**
     * 获取当前pk设置中覆盖的时间信息
     */
    @GetMapping("years")
    @ApiOperation(value = "获取当前pk设置中覆盖的时间信息", notes = "获取当前pk设置中覆盖的时间信息")
//    @Log(opType = OperateType.QUERY, module = "获取当前pk设置中覆盖的时间信息", note = "获取当前pk设置中覆盖的时间信息")
    @Verify(code = "/employeePerformancePK/years", module = "员工pk管理", action = "4")
    @ResponseBody
    public ResponseData years() {
        ResponseData result = ResponseData.ok();
        employeePerformanceService.years(result);
        return result;
    }

    /**
     * pk视图左侧菜单权限查询
     */
    @GetMapping("view/allPermission")
    @ApiOperation(value = "pk视图左侧菜单权限查询", notes = "pk视图左侧菜单权限查询")
//    @Log(opType = OperateType.QUERY, module = "pk视图左侧菜单权限查询", note = "pk视图左侧菜单权限查询")
    @Verify(code = "/employeePerformancePK/viewAll", module = "员工pk管理", action = "4")
    @ResponseBody
    public ResponseData allPermission() {
        return ResponseData.ok();
    }
}