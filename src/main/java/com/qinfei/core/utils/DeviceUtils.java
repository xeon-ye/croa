package com.qinfei.core.utils;

import com.qinfei.qferp.utils.IConst;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 判断是否是手机端访问工具类
 */
public class DeviceUtils {
    // \b 是单词边界(连着的两个(字母字符 与 非字母字符) 之间的逻辑上的间隔),
    // 字符串在编译时会被转码一次,所以是 "\\b"
    // \B 是单词内部逻辑间隔(连着的两个字母字符之间的逻辑上的间隔)
    static String phoneReg = "\\b(ip(hone|od)|android|opera m(ob|in)i"
            + "|windows (phone|ce)|blackberry"
            + "|s(ymbian|eries60|amsung)|p(laybook|alm|rofile/midp"
            + "|laystation portable)|nokia|fennec|htc[-_]"
            + "|mobile|up.browser|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";
    static String tableReg = "\\b(ipad|tablet|(Nexus 7)|up.browser"
            + "|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";

    //移动设备正则匹配：手机端、平板
    static Pattern phonePat = Pattern.compile(phoneReg, Pattern.CASE_INSENSITIVE);
    static Pattern tablePat = Pattern.compile(tableReg, Pattern.CASE_INSENSITIVE);

    /**
     * 检测是否是移动设备访问
     */
    public static boolean isMobile(String userAgent) {
        if (null == userAgent) {
            userAgent = "";
        }
        // 匹配
        Matcher matcherPhone = phonePat.matcher(userAgent);
        Matcher matcherTable = tablePat.matcher(userAgent);
        return matcherPhone.find() || matcherTable.find();
    }


    /**
     * 检查访问方式是否为移动端
     */
    public static boolean isMobile(HttpServletRequest request) {
        boolean isMobile = false;
        HttpSession session = request.getSession();
        //检查是否已经记录访问方式（移动端或pc端）
        session.removeAttribute(IConst.USER_AGENT);
        if (null == session.getAttribute(IConst.USER_AGENT)) {
            try {
                //获取ua，用来判断是否为移动端访问
                String userAgent = request.getHeader("USER-AGENT").toLowerCase();
                isMobile = DeviceUtils.isMobile(userAgent);
                //判断是否为移动端访问
                if (isMobile) {
                    session.setAttribute(IConst.USER_AGENT, "mobile");
                } else {
                    session.setAttribute(IConst.USER_AGENT, "pc");
                }
            } catch (Exception ignored) {
            }
        } else {
            isMobile = session.getAttribute(IConst.USER_AGENT).equals("mobile");
        }

        return isMobile;
    }
}
