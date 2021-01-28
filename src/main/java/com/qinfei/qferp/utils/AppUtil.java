package com.qinfei.qferp.utils;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;

/**
 * 应用程序工具类
 */
public class AppUtil {
    /**
     * 获取request
     *
     * @return HttpServletResponse
     */
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 获取 response
     *
     * @return HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    /**
     * 获取session
     *
     * @return
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public static User getUser() {
        Object obj = getSession().getAttribute(IConst.USER_KEY);
        if (obj != null) {
            return (User) obj;
        } else {
            return null;
        }
    }

    /**
     * 设置用户登录信息
     *
     * @return
     */
    public static void setUser(User user) {
        getSession().setAttribute(IConst.USER_KEY, user);
    }

    /**
     * 获取key获取信息
     *
     * @return
     */
    public static <T> T get(String key) {
        Object obj = getSession().getAttribute(key);
        if (obj != null) {
            return (T) obj;
        }
        return null;
    }

    /**
     * 判断是否是某个职位
     *
     * @return
     */
    public static boolean isRoleCode(String code) {
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        for (Role role : roles) {
            if (code.equalsIgnoreCase(role.getCode())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是某个角色
     *
     * @return
     */
    public static boolean isRoleType(String type) {
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        for (Role role : roles) {
            if (type.equalsIgnoreCase(role.getType())) {
                return true;
            }
        }
        return false;
    }
}
