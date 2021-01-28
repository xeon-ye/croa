package com.qinfei.qferp.controller.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.UserBinding;
import com.qinfei.qferp.service.sys.IUserBindingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @CalssName UserBindingController
 * @Description 用户绑定接口
 * @Author xuxiong
 * @Date 2019/11/23 0023 11:27
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/userBinding")
@Api(description = "用户绑定接口")
public class UserBindingController {
    @Autowired
    private IUserBindingService userBindingService;

    @PostMapping("/binding")
    @ApiOperation(value = "关联用户", notes = "关联用户")
    @Verify(code = "/userBinding", module = "关联用户/关联用户", action = "1")
    @ResponseBody
    public ResponseData binding(@RequestBody UserBinding userBinding){
        try{
            userBindingService.binding(userBinding);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "关联用户异常，请联系技术人员！");
        }
    }

    @PostMapping("/bindingUserList")
    @ApiOperation(value = "关联用户列表", notes = "关联用户列表")
    @Verify(code = "/userBinding", module = "关联用户/关联用户列表", action = "4")
    @ResponseBody
    public List<UserBinding> bindingUserList(){
        return userBindingService.bindingUserList();
    }

    @PostMapping("/exchangeUser")
    @ApiOperation(value = "切换用户", notes = "切换用户")
    @Verify(code = "/userBinding", module = "关联用户/关联用户列表", action = "4")
    @ResponseBody
    public ResponseData exchangeUser(@RequestParam("userId") Integer userId, HttpSession session){
        try{
            userBindingService.exchangeUser(session, userId);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002, "切换用户异常，请联系技术人员！");
        }
    }

    @PostMapping("/cancelBinding")
    @ApiOperation(value = "取消用户关联", notes = "取消用户关联")
    @Verify(code = "/userBinding", module = "关联用户/取消用户关联", action = "2")
    @ResponseBody
    public ResponseData cancelBinding(@RequestParam("userId") Integer userId){
        try{
            userBindingService.cancelBinding(userId);
            return ResponseData.ok();
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "用户解绑异常，请联系技术人员！");
        }
    }
}
