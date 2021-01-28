//package com.qinfei.core.interceptor;
//
//import com.qinfei.qferp.entity.sys.User;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationListener;
//import org.springframework.data.redis.core.BoundHashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.session.events.SessionDestroyedEvent;
//
//import javax.annotation.Resource;
//import java.util.*;
//import java.util.concurrent.CopyOnWriteArraySet;
//
//@Slf4j
//public class MySessionRegistryImpl implements ApplicationListener<SessionDestroyedEvent> {
//
//
//    private static final String SESSIONIDS = "sessionIds";
//
//    private static final String PRINCIPALS = "principals";
//
//    @Resource
//    private RedisTemplate redisTemplate;
//
////    private final ConcurrentMap<Object, Set<String>> principals = new ConcurrentHashMap();
////    private final Map<String, SessionInformation> sessionIds = new ConcurrentHashMap();
//
//    public MySessionRegistryImpl() {
//    }
//
//    public List<Object> getAllPrincipals() {
//        return new ArrayList(this.getPrincipalsKeySet());
//    }
//
//    public List<User> getAllSessions(Object principal, boolean includeExpiredSessions) {
//        Set<String> sessionsUsedByPrincipal =  this.getPrincipals(((UserDetails)principal).getUsername());
//        if (sessionsUsedByPrincipal == null) {
//            return Collections.emptyList();
//        } else {
//            List<User> list = new ArrayList(sessionsUsedByPrincipal.size());
//            Iterator var5 = sessionsUsedByPrincipal.iterator();
//
//            while (true) {
//                User user;
//                do {
//                    do {
//                        if (!var5.hasNext()) {
//                            return list;
//                        }
//
//                        String sessionId = (String) var5.next();
//                        user = this.getSessionInformation(sessionId);
//                    } while (user == null);
//                } while (!includeExpiredSessions );
//
//                list.add(user);
//            }
//        }
//    }
//
//    public User getSessionInformation(String sessionId) {
//        return this.getSessionInfo(sessionId);
//    }
//
//    public void onApplicationEvent(SessionDestroyedEvent event) {
//        String sessionId = event.getSessionId();
//        this.removeSessionInformation(sessionId);
//    }
//
//    public void refreshLastRequest(String sessionId) {
//        User info = this.getSessionInformation(sessionId);
//        if (info != null) {
//            info.refreshLastRequest();
//        }
//
//    }
//
//    public void registerNewSession(String sessionId, Object principal) {
//
//
//        if (this.getSessionInformation(sessionId) != null) {
//            this.removeSessionInformation(sessionId);
//        }
//
//        this.addSessionInfo(sessionId, new User(principal, sessionId, new Date()));
//
//        Set<String> sessionsUsedByPrincipal = (Set) this.getPrincipals(principal.toString());
//        if (sessionsUsedByPrincipal == null) {
//            sessionsUsedByPrincipal = new CopyOnWriteArraySet();
//            Set<String> prevSessionsUsedByPrincipal = (Set) this.putIfAbsentPrincipals(principal.toString(), sessionsUsedByPrincipal);
//            if (prevSessionsUsedByPrincipal != null) {
//                sessionsUsedByPrincipal = prevSessionsUsedByPrincipal;
//            }
//        }
//        ((Set) sessionsUsedByPrincipal).add(sessionId);
//        this.putPrincipals(principal.toString(), sessionsUsedByPrincipal);
//
//
//    }
//
//    public void removeSessionInformation(String sessionId) {
//        User info = this.getSessionInformation(sessionId);
//        if (info != null) {
//                this.removeSessionInfo(sessionId);
//        }
//    }
//
//
//    public void addSessionInfo(final String sessionId, final User user) {
//        BoundHashOperations<String, String, User> hashOperations = redisTemplate.boundHashOps(SESSIONIDS);
//        hashOperations.put(sessionId, user);
//    }
//
//    public User getSessionInfo(final String sessionId) {
//        BoundHashOperations<String, String, User> hashOperations = redisTemplate.boundHashOps(SESSIONIDS);
//        return hashOperations.get(sessionId);
//    }
//
//    public void removeSessionInfo(final String sessionId) {
//        BoundHashOperations<String, String, User> hashOperations = redisTemplate.boundHashOps(SESSIONIDS);
//        hashOperations.delete(sessionId);
//    }
//
//    public Set<String> putIfAbsentPrincipals(final String key, final Set<String> set) {
//        BoundHashOperations<String, String, Set<String>> hashOperations = redisTemplate.boundHashOps(PRINCIPALS);
//        hashOperations.putIfAbsent(key, set);
//        return hashOperations.get(key);
//    }
//
//    public void putPrincipals(final String key, final Set<String> set) {
//        BoundHashOperations<String, String, Set<String>> hashOperations = redisTemplate.boundHashOps(PRINCIPALS);
//        hashOperations.put(key,set);
//    }
//
//    public Set<String> getPrincipals(final String key) {
//        BoundHashOperations<String, String, Set<String>> hashOperations = redisTemplate.boundHashOps(PRINCIPALS);
//        return hashOperations.get(key);
//    }
//
//    public Set<String> getPrincipalsKeySet() {
//        BoundHashOperations<String, String, Set<String>> hashOperations = redisTemplate.boundHashOps(PRINCIPALS);
//        return hashOperations.keys();
//    }
//
//    public void removePrincipal(final String key) {
//        BoundHashOperations<String, String, Set<String>> hashOperations = redisTemplate.boundHashOps(PRINCIPALS);
//        hashOperations.delete(key);
//    }
//
//
//}