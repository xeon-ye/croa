package com.qinfei.qferp.controller.plan;

import com.qinfei.core.ResponseData;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserGroupRelate;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.plan.IUserGroupService;
import com.qinfei.qferp.service.sys.IUserService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @CalssName UserGroupController
 * @Description 用户群组接口
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:07
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/userGroup")
@Api("用户群组接口")
public class UserGroupController {
    @Autowired
    private IUserGroupService userGroupService;

    /**
     * 查询公司所有人员
     * @param name
     * @return
     */
    @RequestMapping("/listBusinessPart")
    @ResponseBody
    public List<User> characterName(String name) {
        return userGroupService.listBusinessPart(name);
    }

    /**
     * 查询批次表
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<UserGroup> list(@RequestParam Map map, @PageableDefault() Pageable pageable){
        return userGroupService.listPg(map,pageable);
    }

    /**
     * 编辑批次
     */
    @RequestMapping("/edit")
    @ResponseBody
    public ResponseData edit(@RequestParam("id") Integer id){
        try{
            ResponseData data = ResponseData.ok();
            UserGroup userGroup = userGroupService.getById(id);
            List<UserGroup> list = userGroupService.queryUserId(userGroup.getId());
            data.putDataValue("list", list);
            data.putDataValue("entity", userGroup);
            return data;
        }
        catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 删除批次
     */
    @RequestMapping(value = "/del")
    @ResponseBody
    public ResponseData del(@RequestParam("id") Integer id){
        try{
            userGroupService.delById(id);
            return  ResponseData.ok().putDataValue("message","操作成功");
        }catch (QinFeiException e){
          return ResponseData.customerError(e.getCode(), e.getMessage());
        } catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage());
        }
    }



}
