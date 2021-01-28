package com.qinfei.qferp.controller;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.websocket.IMServer;
import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket服务器端推送消息示例Controller
 *
 * @author gzw
 */
@Slf4j
@RestController
@RequestMapping("/api/ws")
class WebSocketController {

    /**
     * 群发消息内容
     *
     * @param message
     * @return
     */
//    @Log(opType = OperateType.QUERY, module = "服务器端推送消息", note = "群发消息内容/sendAll")
    @GetMapping(value = "/sendAll")
    String sendAllMessage(@RequestParam() WSMessage message) {
        WebSocketServer.broadCastInfo(message);
        return "ok";
    }


    /**
     * 指定会话ID发消息
     *
     * @param message 消息内容
     * @return
     */
//    @Log(opType = OperateType.QUERY, module = "服务器端推送消息", note = "指定会话ID发消息/send")
    @RequestMapping("/send")
    public ResponseData sendOneMessage(WSMessage message) {
        WebSocketServer.sendMessage(message);
        return ResponseData.ok();
    }

    /**
     * 指定会话ID发消息
     *
     * @param message 消息内容
     * @return
     */
//    @Log(opType = OperateType.QUERY, module = "服务器端推送消息", note = "指定会话ID发消息/imSend")
    @RequestMapping("/imSend")
    public ResponseData imSend(WSMessage message) {
        IMServer.sendMessage(message);
        return ResponseData.ok();
    }
}