package com.qinfei.core.login;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SessionManagement {

    private static SessionManagement sessionManagement;
    private static final HashMap<String, HttpSession> sessionHashMap = new HashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); //线程同步

    private SessionManagement() {
    }

    public synchronized static SessionManagement getInstance() {
        if (sessionManagement == null) {
            sessionManagement = new SessionManagement();
        }
        return sessionManagement;
    }

    public void addSession(String id, HttpSession httpSession) {
        readWriteLock.writeLock().lock();
        try {
            if (httpSession != null && id != null && id.length() > 0) {
                sessionHashMap.put(id, httpSession);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public void removeSession(String id) {
        readWriteLock.writeLock().lock();
        try {
            if (id != null && id.length() > 0) {
                sessionHashMap.remove(id);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public HttpSession getSession(String sessionId) {
        readWriteLock.readLock().lock();
        try {
            if (sessionId == null) {
                return null;
            }
            return sessionHashMap.get(sessionId);
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

}