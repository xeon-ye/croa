package com.qinfei.core.controller;


import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.utils.QRCodeUtil;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.core.utils.VerifyCodeUtils;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.impl.sys.UserService;
import com.qinfei.qferp.service.sys.IResourceService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Gzw
 */
@Controller
@RequestMapping("code")
public class CodeController {
//
//    @Autowired
//    DefaultKaptcha defaultKaptcha;

    @Autowired
     RedisTemplate redisTemplate;
    @Autowired
    IUserService userService;
    @Autowired
    IResourceService resourceService;

    @Autowired
    Config config;

    @GetMapping("/image")
    public void getImage(HttpSession session,
                         HttpServletResponse response) throws Exception {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        //生成随机字串
        String verifyCode = VerifyCodeUtils.getCode(4);
        //存入会话session
        session.setAttribute("verifyCode", verifyCode.toLowerCase());
        //生成图片
        int w = 100, h = 34;
        VerifyCodeUtils.outImage(w, h, response.getOutputStream(), verifyCode);
    }

    @GetMapping("/login")
    public void loginImage(HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("title", "扫码二维码登录");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        StringBuilder path = new StringBuilder();
        path.append(request.getScheme()).append("://");
        path.append(request.getServerName()).append(":");
//        request.getServerName()
        path.append(request.getServerPort());
        path.append(request.getContextPath()).append("/");
        String sessionId = request.getSession().getId();
        path.append("code/");
        String uuid = UUIDUtil.get32UUID();
        path.append(uuid);
        Map<String,Object> codeMap = new HashMap<>();
        codeMap.put("createDate",new Date().getTime());
        codeMap.put("validate",false);
//        codeMap.put("request",request);
//        codeMap.put("response",response);
        redisTemplate.opsForHash().putAll(uuid,codeMap);
        Map<String,Object> uuidMap = new HashMap<>();
        uuidMap.put("uuid",uuid);
        redisTemplate.opsForHash().putAll(sessionId,uuidMap);
        QRCodeUtil.encode(path.toString(), response.getOutputStream());
    }

    /**
     * 回调H5
     * @param code
     * @return
     */
    @GetMapping("/{code}")
    @ResponseBody
    public ResponseData test(@PathVariable String code){
        ResponseData data  = ResponseData.ok();
        Map<Object,Object> codeMap  = redisTemplate.opsForHash().entries(code);
        data.putDataValue("code",code);
        if(codeMap ==null || codeMap.size()<1){
            return ResponseData.customerError(10001,"验证码信息不存在");
        }else{
            return data;
        }
    }

    /**
     * 用户授权网页登录
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/authLogin")
    @ResponseBody
    public ResponseData authLogin(@RequestParam String code,@RequestParam()String token) throws Exception {
        ResponseData data  = ResponseData.ok();
        Map<Object,Object> codeMap  = redisTemplate.opsForHash().entries(code);
        if(codeMap ==null || codeMap.size()<1){
            return ResponseData.customerError(10001,"验证码信息不存在");
        }else{
            //封装用户信息
            Long createDate = Long.parseLong(codeMap.get("createDate").toString());
            if(new Date().getTime()>(createDate+60*1000)){
                return ResponseData.customerError(10002,"验证码信息已过期");
            }else{
                String str[] = token.split("===");
                String loginName =str[0];
                String password=str[1];
                codeMap.put("loginName" ,loginName);
                codeMap.put("password" ,password);
                codeMap.put("validate" ,true);
                User user=new User();
                user.setUserName(loginName);
                user.setPassword(password);
//                HttpServletRequest request = (HttpServletRequest) codeMap.get("request");
//                HttpServletResponse response = (HttpServletResponse) codeMap.get("response");
                redisTemplate.opsForHash().putAll(code,codeMap);
//                user=userService.login(user);
//                HttpSession session = request.getSession();
//                session.setAttribute(IConst.USER_KEY,user);
//                List<Resource> set = resourceService.queryResourceByUserId(user.getId());
//                // set放入session中
//                session.setAttribute(IConst.USER_RESOURCE, set);
//                request.getRequestDispatcher("/").forward(request,response);
            }
        }

        return data;
    }


    /**
     * 用户验证是否授权登录  返回用户信息
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/authValidate")
    @ResponseBody
    public ResponseData authValidate(HttpServletRequest request) throws Exception {
        ResponseData data  = ResponseData.ok();
        String sessionId = request.getSession().getId();
        String code = (String)redisTemplate.opsForHash().get(sessionId,"uuid");
        Map<Object,Object> codeMap  = redisTemplate.opsForHash().entries(code);
        if(codeMap ==null || codeMap.size()>1){
            Boolean validate = (Boolean)codeMap.get("validate");
            if(!validate){
                return ResponseData.customerError(10001,"验证码未授权");
            }
            //重定向登录
            String loginName = codeMap.get("loginName").toString();
            String password = codeMap.get("password").toString();
            data.putDataValue("userName",loginName);
            data.putDataValue("password",password);

        }else{
            return ResponseData.customerError(10001,"验证码未授权");
        }
        return data;
    }


    @GetMapping("fileExport")
    @ResponseBody
    public void fileExport(@RequestParam(name = "fileName",required = false)String fileName,String filePath,HttpServletResponse response) throws Exception {
//        String downlaodFilename = URLEncoder.encode(fileName);
        String downlaodFilename =URLEncoder.encode(fileName, "GBK");
        response.setHeader("Content-Disposition", "attachment; filename="+downlaodFilename+"");
        response.setContentType("application/octet-stream;charset=UTF-8");
        OutputStream os = response.getOutputStream();
        String path="/apk/";
        if(!ObjectUtils.isEmpty(filePath)) {
            path = path+filePath+"/"+fileName;
        }else {
            path = path+fileName;
        }
        String fileDir = config.getUploadDir();
        File file = new File(fileDir+path);
//        if(!file.exists()){
//            return  ResponseData.customerError(10001,"文件不存在");
//        }
        FileInputStream is  = new FileInputStream(file) ;
        byte[] b = new byte[1024];
        int len = -1;
        while((len = is.read(b)) != -1) {
            os.write(b, 0, len);
        }
        is.close();
        os.flush();
        os.close();
  //      return ResponseData.ok();
    }


}
