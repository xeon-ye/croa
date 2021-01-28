package com.qinfei.qferp.controller.plan;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.plan.UserGroupType;
import com.qinfei.qferp.service.plan.IUserGroupTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @CalssName UserGroupTypeController
 * @Description 用户群组接口
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:07
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/userGroupType")
@Api(description = "用户群组类型接口")
public class UserGroupTypeController {
    @Autowired
    private IUserGroupTypeService userGroupTypeService;

    @PostMapping("save")
    @ApiOperation(value = "新增群组类型", notes = "新增群组类型")
    @Verify(code = "/plan/batchList", module = "群组管理/新增群组类型", action = "1")
    @ResponseBody
    public ResponseData save(@RequestBody UserGroupType userGroupType){
        try{
            return ResponseData.ok().putDataValue("groupType",userGroupTypeService.save(userGroupType));
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, e.getMessage());
        }
    }

    @PostMapping("update")
    @ApiOperation(value = "修改群组类型", notes = "修改群组类型")
    @Verify(code = "/plan/batchList", module = "群组管理/修改群组类型", action = "2")
    @ResponseBody
    public ResponseData update(@RequestBody UserGroupType userGroupType){
        try{
            return ResponseData.ok().putDataValue("userGroupType",userGroupTypeService.update(userGroupType));
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, e.getMessage());
        }
    }

    @PostMapping("del")
    @ApiOperation(value = "删除群组类型", notes = "删除群组类型")
    @Verify(code = "/plan/batchList", module = "群组管理/删除群组类型", action = "3")
    @ResponseBody
    public ResponseData del(@RequestParam("id") Integer id){
        try{
            userGroupTypeService.del(id);
            return ResponseData.ok();
        }catch (QinFeiException byeException){
            return ResponseData.customerError(byeException.getCode(), byeException.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002, e.getMessage());
        }
    }

    @PostMapping("listAllGroupType")
    @ApiOperation(value = "群组类型列表", notes = "群组类型列表")
    @Verify(code = "/plan/batchList", module = "群组管理/群组类型列表", action = "4")
    @ResponseBody
    public List<UserGroupType> listAllGroupType(){
        try{
            return userGroupTypeService.listAllGroupType();
        }catch (QinFeiException byeException){
            return new ArrayList<>();
        }catch (Exception e){
            return new ArrayList<>();
        }
    }

}
