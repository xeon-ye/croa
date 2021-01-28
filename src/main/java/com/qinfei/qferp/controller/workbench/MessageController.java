package com.qinfei.qferp.controller.workbench;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/message")
class MessageController {
    @Autowired
    IMessageService messageService;

    /**
     * 查询消息列表
     * @param params
     * @param pageable
     * @return
     */
    @ResponseBody
    @RequestMapping("/list")
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询所有的消息", note = "查询所有的消息")
    public PageInfo<Map> list(@RequestParam Map params, @PageableDefault(size = 8) Pageable pageable){
        return messageService.list(params,pageable);
    }

    /**
     * 查询消息列表
     * @param map
     * @return
     */
    @ResponseBody
    @RequestMapping("/queryMessageIds")
    public ResponseData queryMessageIds(@RequestParam Map map){
        ResponseData data = ResponseData.ok();
        List<Integer> list = messageService.queryMessageIds(map);
        String message ="";
        if(CollectionUtils.isNotEmpty(list)){
            message=String.format("此次操作将会影响%s条未读消息，确定进行一键已读操作？",list.size());
        }else {
            message=null;
        }
        data.putDataValue("message",message);
        return data;
    }

    /**
     * 获取各个消息类型的数量
     * @return
     */
    @ResponseBody
    @RequestMapping("/getParentTypeNumber")
    public List<Map> getParentTypeNumber(){
        return messageService.getMessageParentTypeNum();
    }

    @ResponseBody
    @RequestMapping("/readMessage")
//    @Log(opType = OperateType.ADD, module = "系统管理|读取消息", note = "读取消息")
    public ResponseData readMessage(Message message){
        try{
            boolean readStatus = messageService.readMessage(message);
            return ResponseData.ok();
        }catch (Exception e){
            log.error("消息读取失败",e);
            return ResponseData.customerError(1001,e.getMessage());
        }
    }

    /**
     * 批量已读消息
     * @param ids
     * @return
     */
    @RequestMapping("/updateMessage")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "系统管理|批量已读消息", note = "批量已读消息")
    public ResponseData rejectBatch(@RequestParam(value = "ids[]") Integer[] ids) {
        ResponseData responseData = ResponseData.ok();
        messageService.batchUpdateMessage(ids);
        responseData.putDataValue("message", "操作完成");
        return responseData;
    }

    /**
     * 一键已读消息
     * @param map
     * @return
     */
    @RequestMapping("/agreeAllMessage")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "系统管理|批量已读消息", note = "批量已读消息")
    public ResponseData agreeAllMessage(@RequestParam Map map) {
        ResponseData responseData = ResponseData.ok();
        messageService.batchAllMessage(map);
        responseData.putDataValue("message", "操作完成");
        return responseData;
    }

}
