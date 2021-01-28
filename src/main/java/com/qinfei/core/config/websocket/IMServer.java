package com.qinfei.core.config.websocket;

import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.entity.WSMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gzw
 */
@Slf4j
@ServerEndpoint(value = "/im/{userId}")
@Component
public class IMServer {

    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);
    // concurrent包的线程安全Set，用来存放每个客户端对应的Session对象。
    private static final CopyOnWriteArraySet<IMServer> SESSIONS = new CopyOnWriteArraySet<IMServer>();
    //    CopyOnWriteArraySet.
    //    private static ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<String, Session>();
    private Session session;
    private String userId;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session) {
        this.session = session;
        this.userId = userId;
        int cnt = ONLINE_COUNT.incrementAndGet(); // 在线数加1
        log.info("有连接加入，当前连接数为：{}", cnt);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        SESSIONS.remove(session);
        session.close();
        int cnt = ONLINE_COUNT.decrementAndGet();
//        log.info("有连接关闭，当前连接数为：{}", cnt);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("来自客户端的消息：{}", message);
//        sendMessage(session, "收到消息，消息内容：" + message);
    }

    /**
     * 出现错误
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.error("发生错误：{}，Session ID： {}", error.getMessage(), session.getId());
        error.printStackTrace();
        this.onClose(session);
    }

    /**
     * 发送消息，实践表明，每次浏览器刷新，session会发生变化。
     *
     * @param session
     * @param message
     */
    private static void sendMessage(Session session, WSMessage message) {
//        try {
//            session.getBasicRemote().sendText(String.format("%s (From Server，Session ID=%s)", message, session.getId()));
//        session.getAsyncRemote().sendText(String.format("<a href='" + message.getUrl() + "'>%s</a>", message.getContent(), session.getId()));
        session.getAsyncRemote().sendText(JSONObject.toJSONString(message));
//        } catch (IOException e) {
//            log.error("发送消息出错：{}", e.getMessage());
//            e.printStackTrace();
//        }
    }

    /**
     * 群发消息
     *
     * @param message
     * @throws IOException
     */
    public static void broadCastInfo(WSMessage message) {
        for (IMServer server : SESSIONS) {
            if (server.session.isOpen()) {
                sendMessage(server.session, message);
            }
        }
    }

    /**
     * 指定Session发送消息
     *
     * @param message
     * @throws IOException
     */
    public static void sendMessage(WSMessage message) {
        Session session = null;
        for (IMServer server : SESSIONS) {
            if (server.userId == message.getReceiveUserId()) {
                session = server.session;
                break;
            }
        }
        if (session != null) {
            sendMessage(session, message);
        } else {
            log.warn("没有找到你指定ID的会话：{}", message.getReceiveUserId());
        }
    }

}  