package com.qinfei.core.config.websocket;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.qinfei.core.entity.WSMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author gzw
 */
@Slf4j
@ServerEndpoint(value = "/ws/{userId}")
@Component
public class WebSocketServer {

	private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);
	// concurrent包的线程安全Set，用来存放每个客户端对应的Session对象。
	private static final CopyOnWriteArraySet<WebSocketServer> SESSIONS = new CopyOnWriteArraySet<WebSocketServer>();
	// CopyOnWriteArraySet.
	// private static ConcurrentHashMap<String, Session> SESSIONS = new
	// ConcurrentHashMap<String, Session>();
	private Session session;
	private String userId;

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(@PathParam("userId") String userId, Session session) {
		// SESSIONS.add(session);
		// SESSIONS.put(userId, session);
		this.session = session;
		this.userId = userId;
		int cnt = ONLINE_COUNT.incrementAndGet(); // 在线数加1
		log.info("有连接加入，当前连接数为：{}", cnt);
		// sendMessage(session, "连接成功");
		SESSIONS.add(this);
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose(Session session) throws IOException {
		this.session.close();
		SESSIONS.remove(this);
		int cnt = ONLINE_COUNT.decrementAndGet();
		log.info("有连接关闭，当前连接数为：{}", cnt);
	}

	/**
	 * 收到客户端消息后调用的方法
	 *
	 * @param message
	 *            客户端发送过来的消息
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		log.info("来自客户端的消息：{}", message);
		// sendMessage(session, "收到消息，消息内容：" + message);
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
		// try {
		// session.getBasicRemote().sendText(String.format("%s (From Server，Session
		// ID=%s)", message, session.getId()));
		String url = message.getUrl();
		try {
			if (StringUtils.isEmpty(url)) {
				synchronized (session) {
					session.getBasicRemote().sendText(message.getContent());
				}
			} else {
				StringBuilder content = new StringBuilder();
//				content.append("<a href='#' onclick=\"page('").append(message.getUrl()).append("','").append(message.getSubject()).append("');return false;\">").append(message.getContent()).append("</a>");
				content.append("<span>").append(message.getContent()).append("</span>");
				synchronized (session) {
					session.getBasicRemote().sendText(content.toString());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// } catch (IOException e) {
		// log.error("发送消息出错：{}", e.getMessage());
		// e.printStackTrace();
		// }
	}

	/**
	 * 群发消息
	 *
	 * @param message
	 * @throws IOException
	 */
	public static void broadCastInfo(WSMessage message) {
		for (WebSocketServer server : SESSIONS) {
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
		CopyOnWriteArraySet<Session> userSessions = new CopyOnWriteArraySet<Session>();
		for (WebSocketServer server : SESSIONS) {
			if (server.userId.equals(message.getReceiveUserId())) {
				userSessions.add(server.session);
			}
		}
		for (Session userSession : userSessions) {
			if (userSession != null && userSession.isOpen()) {
				sendMessage(userSession, message);
			} else {
				log.warn("没有找到你指定ID的会话：{}", message.getReceiveUserId());
			}
		}
	}

	// /**
	// * 指定Session发送消息
	// *
	// * @param userName
	// * @param message
	// * @throws IOException
	// */
	// public static void sendMessage(String userName, String message) {
	// Session session = null;
	// for (Map.Entry<String, Session> e : SESSIONS.entrySet()) {
	// if (e.getKey().equals(userName)) {
	// session = e.getValue();
	// break;
	// }
	// }
	// if (session != null) {
	// sendMessage(session, message);
	// } else {
	// log.warn("没有找到你指定ID的会话：{}", userName);
	// }
	// }
}