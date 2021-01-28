package com.qinfei.qferp.controller.fee;

import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.ITaxUserService;
import com.qinfei.qferp.service.flow.IProcessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequestMapping("/taxUser")
@Api(description = "税种财务助理用户接口")
public class TaxUserController {
    @Autowired
    private ITaxUserService taxUserService;
    @Autowired
    private IProcessService processService;



    @RequestMapping("/assistantUser")
    @ResponseBody
    public List<User> assistantUser(@RequestParam Map<String,Object> param){
        return taxUserService.assistantUser(param);

    }




    //根据抬头类型查询财务助理
    @RequestMapping("/taxAssistant")
    @ResponseBody
    public List<User> taxAssistant(String taxType){
        return taxUserService.taxAssistant(taxType);


    }

    /**
     * 通用的审核流转；
     *
     * @return ：操作结果；
     */
    @RequestMapping("/completeApprove")
    @ApiOperation(value = "", notes = "审核流程")
//    @Log(opType = OperateType.UPDATE, module = "", note = "审核流程")
    @ResponseBody
    public ResponseData completeApprove(String[] taskIds, String desc, boolean agree, Integer userId, String userName, Integer deptId) {
        ResponseData data = ResponseData.ok();
        if (userId == null){
            data.putDataValue("message","审核人不能为空");
        }else {
            processService.approveProcess(taskIds, desc, agree, userId, userName, deptId);
            data.putDataValue("message", "操作完成。");
        }
        return data;
    }

}
