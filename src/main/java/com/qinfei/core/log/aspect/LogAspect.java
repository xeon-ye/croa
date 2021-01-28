package com.qinfei.core.log.aspect;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.interceptor.SecurityInterceptor;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.serivce.ILogService;
import com.qinfei.core.utils.IpUtils;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.utils.AppUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.flowable.spring.boot.app.App;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

@Aspect
@Component
public class LogAspect {
    private final ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    @Autowired
    ILogService logService;

    //    @Pointcut("execution(* com.qinfei.qferp.controller.*.*(..)) and @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    @Pointcut("execution(* com.qinfei.*.controller.*.*.*(..)) and @annotation(com.qinfei.core.log.annotation.Log)")
    public void executeLog() {
    }

    /**
     * 拦截器具体实现
     *
     * @param joinPoint
     * @return JsonResult（被拦截方法的执行结果，或需要登录的错误提示。）
     */
    @Before("executeLog()")
    public void before(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());
//        try {
//            MethodInvocationProceedingJoinPoint methodPoint = (MethodInvocationProceedingJoinPoint) joinPoint;
//            Field proxy = methodPoint.getClass().getDeclaredField("methodInvocation");
//            proxy.setAccessible(true);
//            ReflectiveMethodInvocation j = (ReflectiveMethodInvocation) proxy.get(methodPoint);
//            Method method = j.getMethod();
//            Log la = method.getAnnotation(Log.class);
//            if (la != null) {
//                info = new StringBuilder();
//                log = new com.qinfei.wss.entity.Log();
//                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//                HttpServletRequest request = attributes.getRequest();
//                User user = SecurityInterceptor.getLogin(request);
//                //url
//                String url = request.getRequestURI();
//                info.append(",url ={").append(url).append("},");
//                //method
//                info.append("method={").append(request.getMethod()).append("},");
//                //ip
//                String ip = request.getRemoteAddr();
//                info.append("ip={").append(ip).append("},");
//                //类方法
//                Signature signature = joinPoint.getSignature();
//                String className = signature.getDeclaringTypeName();
//                String methodName = signature.getName();
////                info.append("class_method={}", className + '.' + methodName);//获取类名及类方法
//                info.append("class_method={").append(className).append('.').append(methodName).append("},");
//                //参数
//                Object[] args = joinPoint.getArgs();
//                StringBuilder params = new StringBuilder();
//                if (args != null)
//                    for (Object arg : args) {
//                        params.append(arg != null ? arg.toString() : "").append(",");
//                    }
//                info.append("args={").append(args).append("},");
//                log.setOpType(la.opType().getName());
//                log.setNote(la.note());
//                log.setModule(la.module());
//                log.setClassName(className);
//                log.setMethodName(methodName);
//                log.setArgs(params.toString());
//                log.setOpDate(new Date());
//                log.setIp(ip);
//                log.setUrl(url);
//                if (user != null)
//                    log.setUser(user);
//            }
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
    }

    // result of return
    @AfterReturning(pointcut = "executeLog()", returning = "retVal")
    public void after(JoinPoint joinPoint, Object retVal) {
//        startTime.set(System.currentTimeMillis());
        try {
            MethodInvocationProceedingJoinPoint methodPoint = (MethodInvocationProceedingJoinPoint) joinPoint;
            Field proxy = methodPoint.getClass().getDeclaredField("methodInvocation");
            proxy.setAccessible(true);
            ReflectiveMethodInvocation j = (ReflectiveMethodInvocation) proxy.get(methodPoint);
            Method method = j.getMethod();
            Log la = method.getAnnotation(Log.class);
            if (la != null) {
                StringBuilder info = new StringBuilder();
                com.qinfei.core.entity.Log log = new com.qinfei.core.entity.Log();
//                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//                HttpServletRequest request = attributes.getRequest();
//                User user = SecurityInterceptor.getLogin(request);
                HttpServletRequest request = AppUtil.getRequest();
                User user = AppUtil.getUser();
                //url
                String url = request.getRequestURI();
//                info.append("浏览器基本信息：[").append(request.getHeader("user-agent")).append("]");
//                info.append("客户端系统名称：[").append(System.getProperty("os.name")).append("]");
//                info.append("客户端系统版本：[").append(System.getProperty("os.version")).append("]");
//                info.append("客户端操作系统位数：[").append(System.getProperty("os.arch")).append("]");
//                info.append("HTTP协议版本：[").append(request.getProtocol()).append("]");
//                info.append("请求编码格式：[").append(request.getCharacterEncoding()).append("]");
//                info.append("Accept：[").append(request.getHeader("Accept")).append("]");
//                info.append("Accept-语言：[").append(request.getHeader("Accept-Language")).append("]");
//                info.append("Accept-编码：[").append(request.getHeader("Accept-Encoding")).append("]");
//                info.append("Connection：[").append(request.getHeader("Connection"));
//                info.append("Cookie：[").append(request.getHeader("Cookie")).append("]");
//                info.append("客户端发出请求时的完整URL：[").append(request.getRequestURL()).append("]");
//                info.append("请求行中的资源名部分：[").append(request.getRequestURI()).append("]");
//                info.append("请求行中的参数部分：[").append(request.getRemoteAddr()).append("]");
                info.append("客户机所使用的网络端口号：[").append(request.getRemotePort()).append("]");
//                info.append("WEB服务器的IP地址：[").append(request.getLocalAddr()).append("]");
//                info.append("WEB服务器的主机名：[").append(request.getLocalName()).append("]");
//                info.append("客户机请求方式：[").append(request.getMethod()).append("]");
//                info.append("请求的文件的路径：[").append(request.getServerName()).append("]");
//                info.append("请求体的数据流：[");//.append(request.getReader()).append("]");
////                info.append("请求体的数据流：[").append(request.getReader()).append("]");
//                BufferedReader br = request.getReader();
//                String res = "";
//                while ((res = br.readLine()) != null) {
//                    info.append("request body:[").append(res);
//                }
//                info.append("请求所使用的协议名称：[").append(request.getProtocol()).append("]");
//                info.append("请求中所有参数的名字:[").append(request.getParameterNames()).append("]");
//                info.append(",url ={").append(url).append("},");
//                info.append(",url ={").append(url).append("},");
                //method
//                info.append("method={").append(request.getMethod()).append("},");
                //ip
                String ip = IpUtils.getIpAddress(request);//request.getRemoteAddr();

//                info.append("ip={").append(ip).append("},");
                //类方法
                Signature signature = joinPoint.getSignature();
                String className = signature.getDeclaringTypeName();
                String methodName = signature.getName();
//                info.append("class_method={}", className + '.' + methodName);//获取类名及类方法
//                info.append("class_method={").append(className).append('.').append(methodName).append("},");
                //参数
                Object[] args = joinPoint.getArgs();
                StringBuilder params = new StringBuilder();
                if (args != null)
                    for (Object arg : args) {
                        params.append(arg != null ? arg.toString() : "").append(",");
                    }
//                info.append("args={").append(args).append("},");
                log.setOpType(la.opType().getName());
                log.setNote(la.note());
                log.setModule(la.module());
                log.setClassName(className);
                log.setMethodName(methodName);
                log.setArgs(params.toString());
                log.setOpDate(new Date());
                log.setIp(ip);
                // 本地时可注掉 getMac方法，以本地查询提升效率
                String mac = IpUtils.getMac(ip);
//                String mac = "";
                log.setUrl(url);
                log.setMac(mac);
                if (user != null)
                    log.setUser(user);
//                info.append("response={").append(retVal == null ? "" : retVal.toString()).append("}");
                log.setNote(log.getNote() + info.toString());
                log.setRetVal(JSON.toJSONString(retVal));

                logService.save(log);
                log = null;
                info = null;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.gc();
        }
    }

    @After("executeLog()")//无论Controller中调用方法以何种方式结束，都会执行
    public void doAfter() {
    }
}