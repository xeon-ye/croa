package com.qinfei.qferp.controller.plan;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserGroupRelate;
import com.qinfei.qferp.service.plan.IUserGroupRelateService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName UserGroupController
 * @Description 用户群组关系接口
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:07
 * @Version 1.0
 */
@Slf4j
@Controller
@RequestMapping("/userGroupRelate")
@Api(description = "用户群组关系接口")
public class UserGroupRelateController {
    @Autowired
    private IUserGroupRelateService userGroupRelateService;
    /**
     * 增加批次
     */
    @RequestMapping("/add")
    @ResponseBody
    public ResponseData add(UserGroup userGroup){
        try{
            userGroupRelateService.add(userGroup);
            if (userGroup.getInputUserId()!= null){
                Map map;
                List<Map> file = new ArrayList<>();
                List<Integer> ids = new ArrayList<>();
                for (String userId : userGroup.getInputUserId()){
                    map= new HashMap();
                    map.put("userId",Integer.parseInt(userId));
                    map.put("groupId",userGroup.getId());
                    ids.add(Integer.parseInt(userId));
                    file.add(map);
                }
                userGroupRelateService.insertUserGroupRelate(file);
            }
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", userGroup) ;
            return data ;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }
    }
    /**
     * 编辑批次
     */
    @RequestMapping("/edit")
    @ResponseBody
    public ResponseData edit(UserGroup userGroup){
        try{
            userGroupRelateService.edit(userGroup);
            if(userGroup.getInputUserId()!= null){
              Map map;
                List<Map> file = new ArrayList<>();
                Integer groupId1 = userGroup.getId();
                for (String userId : userGroup.getInputUserId()){
                    map= new HashMap();
                    map.put("userId",Integer.parseInt(userId));
                    map.put("groupId",userGroup.getId());
                    file.add(map);
                }
                userGroupRelateService.editUserId(groupId1);
                userGroupRelateService.insertUserGroupRelate(file);
            }
            ResponseData data = ResponseData.ok() ;
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", userGroup) ;
            return data ;
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage()) ;
        }

    }



}
