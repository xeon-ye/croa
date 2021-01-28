package com.qinfei.qferp.controller.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.DeptZw;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.sys.IDeptZwService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @CalssName DeptZwController
 * @Description 部门政委服务接口
 * @Author xuxiong
 * @Date 2019/12/19 0019 14:25
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/deptZw")
@Api("部门政委服务接口")
public class DeptZwController {
    @Autowired
    private IDeptZwService deptZwService;

    @PostMapping("bindingDeptZw")
    @ApiOperation(value = "部门管理", notes = "政委绑定")
    @ResponseBody
    public ResponseData bindingDeptZw(@RequestBody DeptZw deptZw){
        try{
            deptZwService.bindingDeptZw(deptZw);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"很抱歉，政委绑定出现异常，请联系技术人员！");
        }
    }

    @PostMapping("/listDeptTreeByZw")
    @ResponseBody
    public ResponseData listDeptTreeByZw(@RequestParam(name = "deptCode",required = false) String deptCode, @RequestParam(name = "mgrFlag",required = false) Integer mgrFlag){
        ResponseData data = ResponseData.ok();
        try{
            return data.putDataValue("list", deptZwService.listDeptTreeByZw(deptCode, mgrFlag));
        }catch (Exception e){
            e.printStackTrace();
            return data.putDataValue("list", new ArrayList<>());
        }
    }

    @PostMapping("listUserByDeptAndRole")
    @ApiOperation(value = "部门管理", notes = "政委列表")
    @ResponseBody
    public List<User> listUserByDeptAndRole(@RequestParam Map<String, Object> map) {
        return deptZwService.listUserByDeptAndRole(map);
    }


    @GetMapping("/{deptId}")
    @ApiOperation(value = "部门管理", notes = "获取已绑定的政委列表")
    @ResponseBody
    public List<DeptZw> listUserByDeptId(@PathVariable Integer deptId){
        return deptZwService.listUserByDeptId(deptId);
    }

    @GetMapping("/listUser/{deptId}")
    @ApiOperation(value = "政委功能", notes = "获取已绑定的政委信息")
    @ResponseBody
    public List<User> listUserByParam(@PathVariable Integer deptId){
        return deptZwService.listUserByParam(deptId);
    }

    @PostMapping("listZwUserByDeptAndRole")
    @ApiOperation(value = "政委功能", notes = "政委管理部门用户列表")
    @ResponseBody
    public ResponseData listZwUserByDeptAndRole(@RequestParam Map<String, Object> map) {
        ResponseData responseData = ResponseData.ok();
        return responseData.putDataValue("list", deptZwService.listZwUserByDeptAndRole(map));
    }
}
