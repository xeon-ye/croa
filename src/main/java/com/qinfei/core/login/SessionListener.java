package com.qinfei.core.login;

import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.IConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.*;
import javax.swing.*;
import java.util.Objects;

@WebListener
public class SessionListener implements HttpSessionListener/*, HttpSessionAttributeListener*/ {


    public static boolean isExpired = false;

    private static final SessionManagement sessionManagement = SessionManagement.getInstance();
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        System.out.println("------------ sessionCreated");//session被创建 添加到HashMap
        sessionManagement.addSession(httpSessionEvent.getSession().getId(), httpSessionEvent.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        System.out.println("------------ sessionDestroyed");
        isExpired = true; //标记 判断session是否过期
        sessionManagement.removeSession(httpSessionEvent.getSession().getId());
    }

//    /**
//     * 初始化方法 stringRedisTemplate
//     *
//     * @param session
//     * @author chenweixian 陈惟鲜
//     * @date 2018年2月26日 上午10:40:02
//     */
//    public void initStringRedisTemplate(HttpSession session) {
//        if (stringRedisTemplate == null) {
//            ServletContext servletContext = session.getServletContext();
//            ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
//            stringRedisTemplate = context.getBean("stringRedisTemplate", StringRedisTemplate.class);
//        }
//    }
//
//    /**
//     * 处理用户信息
//     *
//     * @param event
//     * @author chenweixian 陈惟鲜
//     * @date 2018年2月26日 上午11:07:11
//     */
//    private void handleUserInfo(HttpSessionBindingEvent event) {
//        if (IConst.USER_KEY.equals(event.getName())) {
//            this.initStringRedisTemplate(event.getSession());
//            User user = (User) event.getValue();
//            BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate.boundHashOps(IUserService.CACHE_KEY);
//            // 踢出之前在线的用户
//            if (user != null) {
//                if (boundHashOperations.keys().size() > 0) {
//                    for (String key : boundHashOperations.keys()) {
//                        if (Objects.equals(key, user.getId())) {
//                            // 踢出
//                            boundHashOperations.delete(key);
//                        }
//                    }
//                }
//            }
//            // 加入当前登录用户
//            boundHashOperations.put(event.getSession().getId(), user.getId() + "");
//        }
//    }
//
//    @Override
//    public void attributeAdded(HttpSessionBindingEvent event) {
//        this.handleUserInfo(event);
//    }
//
//    @Override
//    public void attributeRemoved(HttpSessionBindingEvent event) {
//
//    }
//
//    @Override
//    public void attributeReplaced(HttpSessionBindingEvent event) {
//
//    }
}
