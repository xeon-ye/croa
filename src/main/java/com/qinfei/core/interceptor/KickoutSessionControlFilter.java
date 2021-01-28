//package com.qinfei.core.interceptor;
//
//
//import org.springframework.cache.CacheManager;
//import org.springframework.web.util.WebUtils;
//
//import javax.security.auth.Subject;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.Serializable;
//import java.util.Deque;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Map;
//
//public class KickoutSessionControlFilter extends AccessControlFilter {
//
//    private String kickoutUrl; //踢出后到的地址
//    private boolean kickoutAfter = false; //踢出之前登录的/之后登录的用户 默认踢出之前登录的用户
//    private int maxSession = 1; //同一个帐号最大会话数 默认1
//
//    private SessionManager sessionManager;
//    private Cache<String, Deque<Serializable>> cache;
//
//    public void setKickoutUrl(String kickoutUrl) {
//        this.kickoutUrl = kickoutUrl;
//    }
//
//    public void setKickoutAfter(boolean kickoutAfter) {
//        this.kickoutAfter = kickoutAfter;
//    }
//
//    public void setMaxSession(int maxSession) {
//        this.maxSession = maxSession;
//    }
//
//    public void setSessionManager(SessionManager sessionManager) {
//        this.sessionManager = sessionManager;
//    }
//
//    //设置Cache的key的前缀
//    public void setCacheManager(CacheManager cacheManager) {
//        this.cache = cacheManager.getCache("shiro_redis_cache:");
//    }
//
//    /**
//     * 表示是否允许访问；mappedValue就是[urls]配置中拦截器参数部分，如果允许访问返回true，否则false；
//     * (感觉这里应该是对白名单（不需要登录的接口）放行的)
//     * 如果isAccessAllowed返回true则onAccessDenied方法不会继续执行
//     *
//     * @param request
//     * @param response
//     * @param mappedValue
//     * @return
//     * @throws Exception
//     */
//
//    @Override
//    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
//        System.err.println(">>>>>>>>>>>>>>>>Session 队列>>>>>>>>>>>>>>>>>>");
//
//
//        Subject subject = getSubject(request, response);
//        if (!subject.isAuthenticated() && !subject.isRemembered()) {
//            //如果没有登录，直接进行登录的流程
//            return true;
//        }
//        Session session = subject.getSession();
//        Uuser user = (Uuser) subject.getPrincipal();
//        String username = user.getEmail();
//        Serializable sessionId = session.getId();
//
//        //读取缓存   没有就存入
//        Deque<Serializable> deque = cache.get(username);
//
//        //如果此用户没有session队列，也就是还没有登录过，缓存中没有
//        //就new一个空队列，不然deque对象为空，会报空指针
//        if (deque == null) {
//            deque = new LinkedList<Serializable>();
//            deque.push(sessionId);
//            session.setAttribute("username", username);
//            cache.put(username, deque);
//        }
//        String name = String.valueOf(session.getAttribute("username"));
//           session.setAttribute("kickout",true);
//
//        //如果队列里没有此sessionId，且用户没有被踢出；放入队列
//        if (!deque.contains(sessionId) && name.equals(username)) {
//            //将sessionId存入队列
//            deque.push(sessionId);
//
//            //将用户的sessionId队列缓存
//            cache.put(username, deque);
//        }
//
//        //如果队列里的sessionId数超出最大会话数，开始踢人
//        Session kickoutSession = null;
//        while (deque.size() > maxSession) {
//            Serializable kickoutSessionId = null;
//
//            kickoutSessionId = deque.removeLast();
//            //踢出后再更新下缓存队列
//            cache.put(username, deque);
//
//
//            try {
//                //获取被踢出的sessionId的session对象
//                kickoutSession = sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
//                if (kickoutSession != null) {
//                    //设置会话的kickout属性表示踢出了
//                    session.setAttribute("kickout", false);
//                }
//            } catch (Exception e) {//ignore exception
//            }
//        }
//
//        //如果被踢出了，直接退出，重定向到踢出后的地址
//
//        if (!((Boolean) session.getAttribute("kickout"))) {
//
//
//            Map<String, String> resultMap = new HashMap<String, String>();
//            //判断是不是Ajax请求
//            if ("XMLHttpRequest".equalsIgnoreCase(((HttpServletRequest) request).getHeader("X-Requested-With"))) {
//                resultMap.put("user_status", "300");
//                resultMap.put("message", "您已经在其他地方登录，请重新登录！");
//                //输出json串
//                out(response, resultMap);
//                System.out.println("您已经在其他地方登录，请重新登录！");
//                return Boolean.FALSE;
//            }
//
//        }
//        return Boolean.TRUE;
//
//    }
//
//
//    /**
//     * 表示当访问拒绝时是否已经处理了；如果返回true表示需要继续处理；
//     * 如果返回false表示该拦截器实例已经处理了，将直接返回即可。
//     * onAccessDenied是否执行取决于isAccessAllowed的值，
//     * 如果返回true则onAccessDenied不会执行；如果返回false，执行onAccessDenied
//     * 如果onAccessDenied也返回false，则直接返回，
//     * 不会进入请求的方法（只有isAccessAllowed和onAccessDenied的情况下）
//     *
//     * @param request
//     * @param response
//     * @return
//     * @throws Exception
//     */
//    @Override
//    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
//        //退出
//        Subject subject = getSubject(request, response);
//        subject.logout();
//        //保存访问路径
//        saveRequest(request);
//        //重定向
//        WebUtils.issueRedirect(request, response, kickoutUrl);
//
//        return Boolean.FALSE;
//    }
//
//
//    private void out(ServletResponse hresponse, Map<String, String> resultMap) {
//        try {
//            hresponse.setCharacterEncoding(StandardCharsets.UTF_8);
//            PrintWriter out = hresponse.getWriter();
//            out.println(JSONObject.fromObject(resultMap).toString());
//            out.flush();
//            out.close();
//        } catch (Exception e) {
//            System.err.println("KickoutSessionFilter.class 输出JSON异常，可以忽略。");
//        }
//    }
//}