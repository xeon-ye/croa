package com.qinfei.qferp.controller.sys;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.config.Config;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.exception.ResultEnum;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.login.SessionListener;
import com.qinfei.core.login.SessionManagement;
import com.qinfei.core.utils.IpUtils;
import com.qinfei.core.utils.MD5Utils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.sys.Post;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.sms.ISmsService;
import com.qinfei.qferp.service.sys.IResourceService;
import com.qinfei.qferp.service.sys.IRoleService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.ValidateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/user")
@Api(description = "用户管理接口")
class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IResourceService resourceService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    Config config;

    @Autowired
    private ISmsService smsService;

    @Value("${sms.needMsgSwitch}")
    private String needMsgSwitch;


    @PostMapping
    @ApiOperation(value = "新增用户", notes = "新增注册")
    @Log(opType = OperateType.ADD, module = "系统管理/新增用户", note = "新增注册")
    @ResponseBody
    public int addUser(User user) {
        user.setName(user.getName().trim());
        return userService.add(user);
    }

    @GetMapping("/all/{pageNum}/{pageSize}")
    @ApiOperation(value = "查询所有用户分页", notes = "查询所有用户分页")
    @Log(opType = OperateType.QUERY, module = "系统管理/查询所有用户分页", note = "查询所有用户分页")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "当前页数", required = true, paramType = "query", dataType = "Integer"), @ApiImplicitParam(name = "pageSize", value = "每页显示条数", required = true, paramType = "query", dataType = "Integer")})

    @ResponseBody
    public PageInfo<User> findAllUser(@ApiParam(value = "当前页数") @PathVariable("pageNum") int pageNum, @ApiParam(value = "每页显示条数") @PathVariable("pageSize") int pageSize) {
        return userService.listPg(pageNum, pageSize);
    }

    @GetMapping("/list")
    @ApiOperation(value = "查询所有用户列表", notes = "查询所有用户列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "查询用户条件", required = true, paramType = "query", dataType = "User")})
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理/查询所有用户列表", note = "查询所有用户列表")
    public List<User> list(User user) {
        return userService.list(user);
    }
    @GetMapping("/listUser")
    @ApiOperation(value = "查询所有用户列表", notes = "查询所有用户列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "查询用户条件", required = true, paramType = "query", dataType = "User")})
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理/查询所有用户列表", note = "查询所有用户列表")
    public List<User> listUser(User user) {
        return userService.listUser(user);
    }

    @GetMapping("/listByType/{type}")
    @ApiOperation(value = "根据角色类型查询用户列表", notes = "根据角色类型查询用户列表")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理/根据角色类型查询用户列表", note = "根据角色类型查询用户列表")
    public List<User> listByType(@ApiParam(value = "根据角色类型查询用户列表") @PathVariable("type") String type) {
        return userService.listByType(type);
    }

    @GetMapping("/listPart/{type}")
    @ApiOperation(value = "根据角色类型查询用户列表", notes = "根据角色类型查询用户列表")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理/根据角色类型查询用户列表", note = "根据角色类型查询用户列表/listPart")
    public List<User> listPart(@ApiParam(value = "根据角色类型查询用户列表") @PathVariable("type") String type) {
//        User user = AppUtil.getUser();
//        String companyCode = user.getCompanyCode();
        return userService.listPart(type);
    }

    @GetMapping("secretary")
    @ApiOperation(value = "根据公司查询财务部长",notes = "根据公司查询财务部长")
    @ResponseBody
    public List<User> secretary(){
        return userService.secretary();
    }

    /**
     * 新增稿件时，可以选择到所有业务员
     *
     * @param type
     * @return
     */
    @GetMapping("/listPartAll/{type}")
    @ApiOperation(value = "根据角色类型查询用户列表", notes = "根据角色类型查询用户列表")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理/根据角色类型查询用户列表", note = "根据角色类型查询用户列表/listPartAll")
    public List<User> listPartAll(@ApiParam(value = "根据角色类型查询用户列表") @PathVariable("type") String type) {
        User user = AppUtil.getUser();
        String companyCode = user.getCompanyCode();
        return userService.listPartAll(type, companyCode);
    }

    /**
     * 新增稿件时，可以选择到未交接的业务员
     *
     * @param type
     * @return
     */
    @GetMapping("/listPartAll2/{type}")
    @ApiOperation(value = "根据角色类型查询用户列表", notes = "根据角色类型查询用户列表")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理/根据角色类型查询用户列表", note = "根据角色类型查询用户列表/listPartAll2")
    public List<User> listPartAll2(@ApiParam(value = "根据角色类型查询用户列表") @PathVariable("type") String type) {
        User user = AppUtil.getUser();
        String companyCode = user.getCompanyCode();
        return userService.listPartAll2(type, companyCode);
    }

    /**
     * 新增稿件时，可以选择到未交接的业务员
     *
     * @param type
     * @return
     */
    @GetMapping("/listPartAll3/{type}")
    @ApiOperation(value = "根据角色类型查询用户列表", notes = "根据角色类型查询用户列表")
    @ResponseBody
    public List<User> listPartAll3(@ApiParam(value = "根据角色类型查询用户列表") @PathVariable("type") String type) {
        User user = AppUtil.getUser();
        String companyCode = user.getCompanyCode();
        return userService.listPartAll3(type, companyCode);
    }

    @GetMapping("/listByTypeAndCompanyCode/{type}")
    @ApiOperation(value = "根据角色类型查询用户列表", notes = "根据角色类型查询用户列表")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据角色类型查询用户列表/listByTypeAndCompanyCode")
    public List<User> listByTypeAndCompanyCode(@ApiParam(value = "根据角色类型查询用户列表") @PathVariable("type") String type) {
        String companyCode = AppUtil.getUser().getDept().getCompanyCode();
        if (StringUtils.isEmpty(companyCode))
            companyCode = IConst.COMPANY_CODE_XH;
        return userService.listByTypeAndCompanyCode(type, companyCode, null);
    }

    @GetMapping("/listByMediaTypeUserId")
    @ApiOperation(value = "根据角色类型查询用户列表", notes = "根据媒体板块查询用户列表")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据角色类型查询用户列表/listByTypeAndCompanyCode")
    public List<User> listByMediaTypeUserId() {
        User user = AppUtil.getUser();
        return userService.listByMediaTypeUserId(user.getId());
    }

    @GetMapping("/listPastMedia")
    @ApiOperation(value = "根据角色类型查询用户列表", notes = "根据媒体板块查询用户列表")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据媒体板块查询用户列表")
    public List<User> listPastMedia(@RequestParam(name = "deptId", required = false) Integer deptId) {
        return userService.listPastMedia(deptId);
    }

    @GetMapping("/administrativePersonnel")
    @ApiOperation(value = "查找本公司的行政人事专员", notes = "查找本公司的行政人事专员")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "查找本公司的行政人事专员")
    public List<User> administrativePersonnel() {
        User user = AppUtil.getUser();
        String companyCode = user.getCompanyCode();
        return userService.administrativePersonnel(companyCode);
    }

    @GetMapping("/listByDepart")
    @ApiOperation(value = "查询所有用户列表", notes = "查询所有用户列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "查询用户条件", required = true, paramType = "query", dataType = "User")})
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "查询所有用户列表")
    public List<User> list(User user, List<Integer> departIds) {
        return userService.list(user);
    }

    @ResponseBody
    @GetMapping("/all")
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "/all")
    public PageInfo<User> all() {
        return userService.list();
    }

    @ResponseBody
    @PostMapping("/listPg")
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "用户列表/listPg")
    public PageInfo<User> listPg(Pageable pageable, @RequestParam Map map) {
        return userService.listPg(pageable.getPageNumber(), pageable.getPageSize(), map);
    }

    @GetMapping("/{id}")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "根据id查询用户")
    public User get(@PathVariable("id") Integer id) {
        // mailService.sendSimpleMail("395037000@qq.com","test","test");
        Object user = userService.getById(id);
        return (User) user;
    }


    @PostMapping("/sendVerifyCode")
    @ResponseBody
    public ResponseData sendVerifyCode( User user ) {

    	user.setUserName(user.getUserName().trim());
        user.setPassword(MD5Utils.encode(user.getPassword()));
        User userByUsername = userService.getUserByUsername(user.getUserName());
        if (userByUsername == null) {
            throw new QinFeiException(ResultEnum.ACCT_EOOR);
        }
        HttpServletRequest request = AppUtil.getRequest();
    	if(checkIp(request,userByUsername)) {
    		 return ResponseData.customerError(201, "同一IP登陆，可免短信验证码登陆");
    	}
        
        try {
			if (userByUsername.getPassword().equals(user.getPassword())&&!StringUtils.isEmpty(userByUsername.getPhone())) {
				return	smsService.sendVerifyCode(userByUsername.getPhone());
			}
		} catch (ClientException e) {
			e.printStackTrace();
			  return ResponseData.customerError(1001, "短信发送失败");
		}catch(Exception e) {
			e.printStackTrace();
			  return ResponseData.customerError(1001, "查询用户密码失败");
		}
        return ResponseData.customerError(1001, "用户不存在");
    }


    @PostMapping("/login/checkNeedSms")
    @ResponseBody
    public ResponseData checkNeedSms( User user ) {
    	HttpServletRequest request = AppUtil.getRequest();
    	user.setUserName(user.getUserName().trim());
        user.setPassword(MD5Utils.encode(user.getPassword()));
        User userByUsername = userService.getUserByUsername(user.getUserName());

        if (userByUsername == null) {
            throw new QinFeiException(ResultEnum.ACCT_EOOR);
        }
        boolean checkIp=checkIp(request,userByUsername);
        try {
        	
        	if(checkIp) {
        		return ResponseData.ok();
        	}
        	
		} catch (Exception e) {
			  return ResponseData.badRequest();
		}
        return  ResponseData.badRequest();
    }

    
    
    
    
    
    private boolean checkIp(HttpServletRequest request, User userByUsername) {
    	 String ip = IpUtils.getIpAddress(request)+"";
     	if(!StringUtils.isEmpty(userByUsername.getLoginIp())&&userByUsername.getLoginIp().equals(ip)) {
     		return true;
     	}
     	return false;
	}

	@CrossOrigin
    @PostMapping("/login")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "用户管理", note = "用户登录")
    @Verify(code = "/user/login", module = "系统管理/用户登录")
    public ResponseData login(User user, HttpSession session, HttpServletResponse response) {
		/*
		 * 原验证码代码
        String verifyCode = user.getVerifyCode();
		 * if (!ObjectUtils.isEmpty(verifyCode)) { if
		 * (!verifyCode.equalsIgnoreCase((String) session.getAttribute("verifyCode"))) {
		 * return ResponseData.customerError(1001, "验证码输入有误"); } }
		 */

		

        String pwd = user.getPassword();
        user.setUserName(user.getUserName().trim());
        user.setPassword(MD5Utils.encode(user.getPassword()));
        String verifyCode = user.getVerifyCode();
         user = userService.login(user);
        user.setVerifyCode(verifyCode);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PATCH, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        if (user.getId() == null) {
            return ResponseData.customerError(1001, user.getMessage());
        } else {
            if (!userService.validateLogin(user)) {
                return ResponseData.customerError(1002, "登陆失败,重新登录");
            }
            if (!ValidateUtil.checkPwd(pwd)) {
                return ResponseData.customerError(1002, "账号密码错误,重新登录");
            }


            HttpServletRequest request = AppUtil.getRequest();
            if(org.apache.commons.lang3.StringUtils.equals("true", needMsgSwitch)&&!checkIp(request, user)) {
            	  ResponseData checkSms = smsService.checkSms(user);
                  if(checkSms.getCode()!=200) {
                  	  return ResponseData.customerError(1003, "短信验证码错误");
                  }
            }else {
            	log.info("同IP登陆,无需验证验证码");
            }
    		
            final User tempUser=user; 
            new Thread(() -> {
            	String ip = IpUtils.getIpAddress(request);
            	tempUser.setLoginIp(ip);
            	tempUser.setMac(IpUtils.getMac(ip));
            	tempUser.setLoginTime(new Date());
            	userService.update(tempUser);
            }).start();
            

            String sessionId = user.getSessionId();
            SessionManagement sessionManagement = SessionManagement.getInstance();
            HttpSession session_old = sessionManagement.getSession(sessionId);
            if (!SessionListener.isExpired && session_old != null) {
                //强制过期
                session_old.invalidate();
                sessionManagement.removeSession(sessionId);
            }
            sessionManagement.addSession(session.getId(), session);
            session.setAttribute(IConst.USER_KEY, user);
            List<Resource> set = resourceService.queryResourceByUserIdNew(user.getId());
            // set放入session中
            session.setAttribute(IConst.USER_RESOURCE, set);
            ResponseData data = ResponseData.ok();
            data.putDataValue("image", user.getImage());
            data.putDataValue("name", user.getUserName());
            data.putDataValue("pwd", user.getPassword());
            data.putDataValue("token", user.getUserName() + "===" + pwd);
            return data;
        }
    }

    @CrossOrigin
    @PostMapping("/login/other")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "用户登录")
//    @Verify(code = "/user/login", module = "系统管理/用户登录")
    public User loginOther(@RequestBody User user, HttpServletResponse response) {
        String pwd = user.getPassword();
        String userName = user.getUserName().trim();
        String password = MD5Utils.encode(pwd);

        User data = userService.loginOther(userName, password);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PATCH, DELETE, PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        return data;
    }

    @RequestMapping(value = "/view")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "根据id查询用户")
    public ResponseData view(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            User su = userService.getById(id);
            data.putDataValue("user", su);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value = "/del")
    @ResponseBody
    @Log(opType = OperateType.DELETE, module = "系统管理|用户管理", note = "用户删除")
    @Verify(code = "/user/del", module = "系统管理/用户删除")
    public ResponseData delUser(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            userService.delById(id);
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value = "/add")
    @ResponseBody
    @Log(opType = OperateType.ADD, module = "系统管理|用户管理", note = "新增用户")
    @Verify(code = "/user/add", module = "系统管理/新增用户")
    public ResponseData add(User user) {
        try {
            userService.add(user);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("user", user);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @PostMapping("/edit")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|用户管理", note = "修改用户")
    @Verify(code = "/user/edit", module = "系统管理/修改用户")
    public ResponseData edit(User user) {
        try {
            User opUser = AppUtil.getUser();
            user.setUpdateTime(new Date());
            user.setUpdateUserId(opUser.getId());
            userService.update(user);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("user", user);
            if (user.getId().equals(opUser.getId())) {
                AppUtil.getSession().setAttribute(IConst.USER_KEY, user);
            }
            return data;
        } catch (Exception e) {
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @PostMapping("/editSelf")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "系统管理|用户管理", note = "修改个人信息")
    public ResponseData editSelf(User user) {
        try {
            User opUser = AppUtil.getUser();
            if (user.getId().equals(opUser.getId())) {
                ResponseData data = ResponseData.ok();
                user.setUpdateTime(new Date());
                user.setUpdateUserId(opUser.getId());
                userService.update(user);

                opUser.setUserName(user.getUserName());
                opUser.setIsMgr(user.getIsMgr());
                opUser.setSex(user.getSex());
                opUser.setPhone(user.getPhone());
                opUser.setEmail(user.getEmail());
                opUser.setQq(user.getQq());
                opUser.setWechat(user.getWechat());
                opUser.setRemark(user.getRemark());
                AppUtil.getSession().setAttribute(IConst.USER_KEY, opUser);

                data.putDataValue("message", "操作成功");
                data.putDataValue("user", user);
                return data;
            }else{
                return ResponseData.customerError(1001, "未获取到登录信息，请重新登录！");
            }
        } catch (Exception e) {
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value = "/editUserRole")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "用户管理", note = "编辑用户角色关系")
    // @Verify(code = "/user/editUserRole", module = "系统管理/编辑用户角色关系")
    public ResponseData editUserRole(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        List<Role> selRole = roleService.queryRoleByUserId(id);
        List<Role> allRole = roleService.listAll();
        data.putDataValue("selRole", selRole);
        data.putDataValue("allRole", allRole);
        return data;
    }

    @RequestMapping("/submitUserRole")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|用户管理", note = "修改用户的角色")
    @Verify(code = "/user/submitUserRole", module = "系统管理/修改用户的角色")
    public ResponseData submitUserRole(@RequestParam("userId") Integer userId, @RequestParam("checkId") String checkId,@RequestParam("XMZJFlag") boolean XMZJFlag) {
        try {
            userService.submitUserRole(userId, checkId,XMZJFlag);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("userId", userId);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }


    @RequestMapping(value = "/listAll")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|用户管理", note = "查询所有用户")
    public List<User> listAll() {
        return userService.listAll();
    }

    @RequestMapping(value = "/updatePassword")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "系统管理|用户管理", note = "修改密码")
    public ResponseData updatePassword(@RequestParam String oldpassword,
                                       @RequestParam String password1,
                                       @RequestParam String password2,
                                       @RequestParam Integer userId, HttpSession session) {
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseData.noLogin();
        }
        if (!password1.equals(password2)) {
            return ResponseData.customerError(1001, "新密码、确认新密码，两次密码输入不一致！");
        }
        if (oldpassword.equals(password1)) {
            return ResponseData.customerError(1001, "新密码不能和旧密码一致");
        }
        if (!ValidateUtil.checkPwd(password1)) {
            return ResponseData.customerError(1001, "新密码安全级别较低，请重新设置");
        }
        if (!user.getPassword().equals(MD5Utils.encode(oldpassword))) {
            return ResponseData.customerError(1001, "旧密码不正确！");
        }
        userService.updatePassword(user.getId(), MD5Utils.encode(password1));
        session.invalidate();
        return ResponseData.ok("操作成功");
    }

    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }

    @RequestMapping(value = "/resetPassword")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|用户管理", note = "重置密码")
    @Verify(code = "/user/resetPassword", module = "系统管理/重置密码")
    public ResponseData resetPassword(@RequestParam("id") Integer id, @RequestParam("password") String password) {
        try {
            ResponseData data = ResponseData.ok();
            User user = userService.getById(id);
            userService.updatePassword(user.getId(), MD5Utils.encode(password));
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @RequestMapping(value = "/editUserSelf")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "系统管理|编辑用户信息", note = "编辑用户信息")
    public ResponseData editUserSelf(HttpSession session) {
        try {
            ResponseData data = ResponseData.ok();
            data.putDataValue("user", AppUtil.getUser());
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    @RequestMapping(value = "/saveImage")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|保存图片", note = "保存图片")
    public ResponseData saveImage(HttpSession session, @RequestParam(value = "image") MultipartFile multipartFile) {
        //校验图片格式
        String originalFileName = multipartFile.getOriginalFilename();
        String availableExt = "gif|jpg|jpeg|png";//可用的图片格式
        String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        if (availableExt.indexOf(ext) == -1) {
            return ResponseData.customerError(1001, "请选择图片格式文件上传(jpg,png,gif,jpeg等)");
        }
        String fileName = UUIDUtil.get32UUID() + "." + ext;//保存文件名称
        String childPath = getStringData() +File.separator + "images" + File.separator;
        try {
            File destFile = new File(config.getUploadDir() + childPath + fileName);
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            multipartFile.transferTo(destFile);
            User user = (User) session.getAttribute(IConst.USER_KEY);
            user.setImage(config.getWebDir() + childPath + fileName);
            userService.update(user);
            session.setAttribute(IConst.USER_KEY, user);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("user", user);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    @RequestMapping(value = "/checkUserName")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|核实用户名", note = "核实用户名")
    public Boolean checkUserName(@RequestParam(value = "id", required = false) Integer id, @RequestParam("userName") String userName) {
        return userService.checkUserName(id, userName);
    }

    /**
     * 根据职位查找用户
     * @param postId
     * @return
     */
    @RequestMapping("/queryUserByCondition")
    @ResponseBody
    public List<User> queryUserByCondition(@RequestParam(value = "postId",required = false)String postId){
        Map map = new HashMap();
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        map.put("postId",postId);
        List<User> list = userService.queryUserByCondition(map);
        return list;
    }

    /**
     * 查询通讯录
     *
     * @param pageable
     * @return
     */
    @RequestMapping("/queryUserInfo")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询通讯录", note = "查询通讯录")
    public PageInfo<Map> queryUserInfo(Pageable pageable) {
        return userService.queryUserInfo(pageable);
    }

    /**
     * 查询某个部门下的所有用户，或者查询某种角色的用户
     *
     * @param deptId
     * @return
     */
    @RequestMapping("/queryUserByDeptId")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询某个部门下的所有用户", note = "查询某个部门下的所有用户/queryUserByDeptId")
    public List<User> queryUserByDeptId(@RequestParam(value = "deptId") Integer deptId) {
        return userService.queryUserByDeptId(deptId);
    }

    /**
     * 查询某个部门下的所有用户，或者查询某种角色的用户
     *
     * @param deptId
     * @return
     */
    @RequestMapping("/queryDeptUsers")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理|查询某个部门下的所有用户", note = "查询某个部门下的所有用户/queryDeptUsers")
    public List<User> queryDeptUsers(@RequestParam(value = "deptId") Integer deptId) {
        return userService.queryDeptUsers(deptId);
    }

    @RequestMapping("/listMgr")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "查询所有部门领导")
    public PageInfo<Map> listMgr(Pageable pageable, @RequestParam Map map) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(userService.listMgr(map));
    }

    @PostMapping("/mediaType")
    @ResponseBody
    @Log(opType = OperateType.QUERY, module = "系统管理", note = "修改用户媒体板块类型")
    public ResponseData mediaType(@RequestParam("param") String param) {
        JSONObject json = JSON.parseObject(param);
        Integer userId = json.getInteger("userId");
        Integer departId = json.getInteger("departId");
        Object typeId = json.get("typeId");
        List<Map> params = new ArrayList<>();
        if (typeId instanceof JSONArray) {
            JSONArray typeIds = (JSONArray) typeId;
            for (int i = 0; i < typeIds.size(); i++) {
                Map map = new HashMap();
                Integer id = typeIds.getInteger(i);
                map.put("mediaTypeId", id);
                map.put("departId", departId);
                map.put("userId", userId);
                params.add(map);
            }
        } else {
            Map map = new HashMap();
            Integer id = Integer.parseInt(typeId.toString());
            map.put("mediaTypeId", id);
            map.put("departId", departId);
            map.put("userId", userId);
            params.add(map);
        }
        userService.batchSave(params);
        return ResponseData.ok();
    }

    @GetMapping("/listMJByMediaId/{mediaId}")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据媒体id查询所有媒介")
    public ResponseData listMJByMediaId(@PathVariable("mediaId") Integer mediaId) {
        ResponseData ok = ResponseData.ok();
        List<User> listMJByMediaId = userService.listMJByMediaId(mediaId);
        ok.putDataValue("listMJByMediaId", listMJByMediaId);
        return ok;
    }

    @GetMapping("/listMJByMediaTypeId/{mediaTypeId}")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据媒体id查询所有媒介")
    public ResponseData listMJByMediaTypeId(@PathVariable("mediaTypeId") Integer mediaTypeId) {
        ResponseData ok = ResponseData.ok();
        List<User> listMJByMediaTypeId = userService.listMJByMediaTypeId(mediaTypeId);
        ok.putDataValue("listMJByMediaTypeId", listMJByMediaTypeId);
        return ok;
    }

    /**
     * 分公司只能查询到自己公司的媒介员，祥和可以查询到所有
     *
     * @param mediaTypeId
     * @return
     */

    @GetMapping("/listPastMJByMediaTypeId/{mediaTypeId}")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据媒体id查询所有媒介")
    public ResponseData listPastMJByMediaTypeId(@PathVariable("mediaTypeId") Integer mediaTypeId) {
        ResponseData ok = ResponseData.ok();
        User user = AppUtil.getUser();
        String companyCode = user.getCompanyCode();
        List<User> listMJByMediaTypeId = userService.listPastMJByMediaTypeId(mediaTypeId, companyCode);
        ok.putDataValue("listMJByMediaTypeId", listMJByMediaTypeId);
        return ok;
    }

    /**
     * 分公司只能查询到自己公司的媒介员，祥和可以查询到所有
     *
     * @param mediaTypeId
     * @return
     */

    @GetMapping("/listPastMJByMediaTypeId2/{mediaTypeId}")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据媒体id查询所有媒介")
    public ResponseData listPastMJByMediaTypeId2(@PathVariable("mediaTypeId") Integer mediaTypeId) {
        ResponseData ok = ResponseData.ok();
        User user = AppUtil.getUser();
        String companyCode = user.getCompanyCode();
        List<User> listMJByMediaTypeId = userService.listPastMJByMediaTypeId2(mediaTypeId, companyCode);
        ok.putDataValue("listMJByMediaTypeId", listMJByMediaTypeId);
        return ok;
    }

    @GetMapping("/listMJByPlateId/{plateId}")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据媒体id查询所有媒介")
    public ResponseData listMJByPlateId(@PathVariable("plateId") Integer mediaTypeId) {
        ResponseData ok = ResponseData.ok();
        List<User> userList = userService.listMJByPlateId(mediaTypeId);
        ok.putDataValue("userList", userList);
        return ok;
    }

    /**
     * 未移交的媒介
     * @param mediaTypeId
     * @return
     */
    @GetMapping("/listMJByPlateId2/{plateId}")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据媒体id查询所有媒介")
    public ResponseData listMJByPlateId2(@PathVariable("plateId") Integer mediaTypeId) {
        ResponseData ok = ResponseData.ok();
        List<User> userList = userService.listMJByPlateId2(mediaTypeId);
        ok.putDataValue("userList", userList);
        return ok;
    }

    /**
     * 分页查询联系人信息；
     *
     * @param queryContent：查询内容；
     * @param pageNum：页码；
     * @return ：拼接完毕的分页内容
     */
    @RequestMapping("/listUserInfo")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "分页查询联系人信息")
    public void listUserInfo(String queryContent, @RequestParam int pageNum, HttpServletResponse response) {
        try {
            response.getWriter().write(userService.listUserInfo(queryContent, pageNum));
        } catch (IOException e) {
            log.error("获取信息异常", e);
        }
    }

    /**
     * 根据部门查找用户，只查找当前部门的，不包含子部门
     *
     * @param deptId 部门id
     * @return 当前部门用户，不包含子部门
     */
    @RequestMapping("/queryUserByDeptIdONLY")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "系统管理", note = "根据部门查找用户")
    public List<User> queryUserByDeptIdONLY(@RequestParam("deptId") Integer deptId) {
        return userService.queryUserByDeptIdONLY(deptId);
    }

    /**
     * 客户保护数量显示
     *
     * @return
     */
/*    @RequestMapping("/queryCustStatistics")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "客户保护", note = "客户保护数量显示")
    public ResponseData queryCustStatistics(@RequestParam(value = "id", required = false) Integer id) {
        ResponseData data = ResponseData.ok();
        Integer saveCust = 0;
        if (id == null) {
            //客户列表的保护数量显示
            id = AppUtil.getUser().getId();
            User user = userService.getById2(id);
            //保护客户数量
            saveCust = user.getSaveCustNum();
        } else {
            //系统设置的保护数量显示
            User user2 = userService.getById2(id);
            saveCust = user2.getSaveCustNum();
        }
        //已保护客户数量
        int protectedCust = custService.queryProectedNum(id);
        //剩余客户保护数量
        int availableCust = (saveCust - protectedCust);
        data.putDataValue("saveCust", saveCust);
        data.putDataValue("protectedCust", protectedCust);
        data.putDataValue("availableCust", availableCust);
        return data;
    }*/

    /**
     * 查询客户状态
     *
     * @param dockingPeople
     * @return
     */
//    @Log(opType = OperateType.QUERY, module = "客户保护", note = "查询客户状态")
/*    @RequestMapping("/queryCustState")
    @ResponseBody
    public ResponseData queryCustState(@RequestParam("dockingPeople") Integer dockingPeople) {
        ResponseData data = ResponseData.ok();
        int state = userService.queryCustState(dockingPeople);
        data.putDataValue("state", state);
        return data;
    }*/

    /**
     * 查询客户所有的不建议信息
     *
     * @param dockingPeople
     * @return
     */
/*    @RequestMapping("/queryUserCust")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "客户保护", note = "查询客户所有的不建议信息")
    public ResponseData queryUserCust(@RequestParam("dockingPeople") Integer dockingPeople) {
        ResponseData data = ResponseData.ok();
        List<Map> list = userService.queryUserCust(dockingPeople);
        data.putDataValue("list", list);
        return data;
    }*/

    /**
     * 添加客户保护
     *
     * @param saveState
     * @param dockingPeople * @param reason
     * @return
     */
    /*@RequestMapping("/addUserCust")
    @ResponseBody
    @Log(opType = OperateType.ADD, module = "客户列表|添加客户保护", note = "添加客户保护")
    public ResponseData addUserCust(@RequestParam("saveState") Integer saveState, @RequestParam("dockingPeople") Integer dockingPeople, @RequestParam("reason") String reason) {
        return userService.addUserCust(saveState, dockingPeople, reason, AppUtil.getUser().getId());
    }
*/
    /**
     * 查询未建议的人员
     *
     * @return
     */
    @RequestMapping("/queryNoProposeUser")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "建议管理", note = "查询未建议的人员")
    public PageInfo<Map> queryNoProposeUser(@RequestParam Map map, @PageableDefault() Pageable pageable) {
        return userService.queryNoProposeUser(map, pageable);
    }

    /**
     * 设置客户保护数量
     *
     * @param userId
     * @param saveCustNum
     * @return
     */
/*    @RequestMapping("/updateCustNum")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "设置客户保护数量", note = "设置客户保护数量")
    public ResponseData updateCustNum(@RequestParam("userId") Integer userId, @RequestParam("saveCustNum") Integer saveCustNum) {
        ResponseData data = ResponseData.ok();
        Map map = new HashMap();
        map.put("userId", userId);
        map.put("saveCustNum", saveCustNum);
        Integer proectedCustNum = custService.queryProectedNum(userId);
        //如果设置的保护数量大于或等于已保护客户数量才允许设置
        if (proectedCustNum <= saveCustNum) {
            userService.updateUserCust(map);
            data.putDataValue("message", "操作成功");
        } else {
            data.putDataValue("message", "设置的保护数量小于该用户的已保护数量");
        }
        return data;
    }*/

    /**
     * 根据权限和角色查询用户
     *
     * @param deptId
     * @param roleType
     * @return
     */
    @ResponseBody
    @RequestMapping("/deptUsers")
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "根据权限和角色查询用户")
//	public List<User> deptUsers(@RequestParam Map map){
    public List<User> deptUsers(@RequestParam("deptId") Integer deptId, @RequestParam("roleType") String roleType) {
//		Integer deptId= MapUtils.getInteger(map,"deptId");
//		String roleType= MapUtils.getString(map,"roleType");
        return userService.deptUsers(deptId, roleType);
    }

    /**
     * 根据部门ID和角色类型获取该部门下人员
     */
    @RequestMapping("/listUserByDeptAndRole")
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "根据部门ID和角色类型获取该部门下人员")
    @ResponseBody
    public ResponseData listUserByDeptAndRole(@RequestParam Map map) {
        ResponseData responseData = ResponseData.ok();
        return responseData.putDataValue("list", userService.listUserByDeptAndRole(map));
    }

    /**
     * 根据部门ID和角色类型获取该部门下人员
     */
    @RequestMapping("/listUserByDeptAndRole2")
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "根据部门ID和角色类型获取该部门下人员")
    @ResponseBody
    public ResponseData listUserByDeptAndRole2(@RequestParam Map map) {
        ResponseData responseData = ResponseData.ok();
        return responseData.putDataValue("list", userService.listUserByDeptAndRole2(map));
    }

    /**
     * 根据部门ID和角色类型获取该部门下人员
     */
    @RequestMapping("/listUserByDeptAndRoleJT")
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "根据部门ID和角色类型获取该部门下人员")
    @ResponseBody
    public ResponseData listUserByDeptAndRoleJT(@RequestParam Map map) {
        ResponseData responseData = ResponseData.ok();
        return responseData.putDataValue("list", userService.listUserByDeptAndRoleJT(map));
    }

    /**
     * 根据部门ID和角色类型获取该部门下人员
     */
    @RequestMapping("/listUserByDeptAndRole3")
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "根据部门ID和角色类型获取该部门下人员")
    @ResponseBody
    public ResponseData listUserByDeptAndRole3(@RequestParam Map<String, Object> map) {
        ResponseData responseData = ResponseData.ok();
        return responseData.putDataValue("list", userService.listUserByDeptAndRole3(map));
    }
    /**
     * 获取公司的职位
     *
     * @param companyCode
     * @param postNameQC
     * @param pageable
     * @return
     */
    @RequestMapping("/getCompanyPost")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "获取公司的职位")
    public PageInfo<Map> getCompanyPost(@RequestParam(value = "companyCode", required = false) String companyCode, @RequestParam(value = "postNameQC", required = false) String postNameQC, Pageable pageable) {
        Map map = new HashMap();
        if (companyCode == null || companyCode.equals("")) {
            map.put("companyCode", AppUtil.getUser().getCompanyCode());
        } else {
            map.put("companyCode", companyCode);
        }
        map.put("postNameQC", postNameQC);
        return userService.getCompanyPost(map, pageable);
    }

    /**
     * 获取指定公司的职位
     *
     * @param companyCode
     * @return
     */
    @RequestMapping("/queryCompanyPost")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "获取指定公司的职位")
    public List<Map> queryCompanyPost(@RequestParam("companyCode") String companyCode) {
        Map map = new HashMap();
        map.put("companyCode", companyCode);
        return userService.queryCompanyPost(map);
    }

    /**
     * 新增编辑核实职位是否重复
     *
     * @param name
     * @param companyCode
     * @return
     */
    @RequestMapping("/getPostInfo")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "新增编辑核实职位是否重复")
    public ResponseData getPostInfo(@RequestParam("name") String name, @RequestParam(value = "companyCode", required = false) String companyCode, @RequestParam(value = "id", required = false) Integer id) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        if (companyCode == null || companyCode.equals("")) {
            companyCode = AppUtil.getUser().getCompanyCode();
        }
        Post post = userService.getPostInfo(name, companyCode, id);
        if (post != null) {
            //已存在该职位
            data.putDataValue("flag", 1);
        } else {
            data.putDataValue("flag", 2);
        }
        return data;
    }

    /**
     * 根据id获取职位信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/getPostById")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "新增编辑核实职位是否重复")
    public ResponseData getPostById(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        Post post = userService.getPostById(id);
        data.putDataValue("entity", post);
        return data;
    }

    /**
     * 根据id职位信息
     *
     * @param id
     * @return
     */
    @RequestMapping("/findDelPostById")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "判断是否可以删除职位")
    public ResponseData findDelPostById(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        Post post = userService.queryByDeletePost(id);
        if (post != null) {
            data.putDataValue("flag", 1);
        } else {
            data.putDataValue("flag", 0);
        }
        return data;
    }

    /**
     * 保存部门职位
     *
     * @param post
     * @return
     */
    @RequestMapping("/saveDeptPost")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "部门职位", note = "保存部门职位")
    public ResponseData saveDeptPost(Post post) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        userService.saveDeptPost(post);
        return data;
    }

    /**
     * 修改部门职位
     *
     * @param post
     * @return
     */
    @RequestMapping("/updateDeptPost")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "部门职位", note = "修改部门职位")
    public ResponseData updateDeptPost(Post post) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        userService.updateDeptPost(post);
        return data;
    }

    /**
     * 删除部门职位
     *
     * @param id
     * @return
     */
    @RequestMapping("/delDeptPost")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "部门职位", note = "删除部门职位")
    public ResponseData delDeptPost(@RequestParam("id") Integer id) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        userService.delDeptPost(id);
        return data;
    }

    /**
     * 添加部门职位关系
     *
     * @param deptId
     * @param postId
     * @return
     */
    @RequestMapping("/insertDeptPost")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "部门职位", note = "添加部门职位关系")
    public ResponseData insertDeptPost(@Param("deptId") Integer deptId, @Param("postId") String postId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        List<Map> list = new ArrayList<>();
        if (postId != null && !postId.equals("") && postId.length() > 0) {
            //删除部门职位关系
            userService.deletePost(deptId);
            String[] id = postId.split(",");
            for (Integer i = 0; i < id.length; i++) {
                Map map = new HashMap();
                map.put("deptId", deptId);
                map.put("postId", id[i]);
                list.add(map);
            }
            userService.insertDeptPost(list);
        }
        return data;
    }

    /**
     * 绑定部门职位时显示
     *
     * @param companyCode
     * @param deptId
     * @return
     */
    @RequestMapping("/queryDeptPost")
    @ResponseBody
//    @Log(opType = OperateType.ADD, module = "部门职位", note = "绑定部门职位时显示")
    public List<Map> queryDeptPost(@Param("companyCode") String companyCode, @Param("deptId") Integer deptId) {
        return userService.queryDeptPost(companyCode, deptId);
    }

    /**
     * 获取角色类型
     */
    @RequestMapping("/roleType")
    @ResponseBody
    public List<Dict> listByTypeAndCode() {
        return userService.listByTypeAndCode();
    }

    /**
     * 获取角色名称
     */
    @RequestMapping("/CharacterName/{nameQc}")
    @ResponseBody
    public List<Role> characterName1(@PathVariable("nameQc") String nameQc) {
        return userService.characterName(nameQc);

    }

    /**
     * 获取角色名称
     */
    @RequestMapping("/getRoleByType")
    @ResponseBody
    public List<Role> getRoleByType(@RequestParam("nameQc") String nameQc) {
        return userService.getRoleByType(nameQc);

    }

    /**
     * 获取角色名称
     */
    @RequestMapping("/listBusinessPart")
    @ResponseBody
    public List<User> characterName(String name) {
        return userService.listBusinessPart(name);

    }

    /**
     * 根据部门查询到部门人员
     */
    @RequestMapping("/listDeptUser")
    @ResponseBody
    public List<User> listDeptUser() {
        return userService.listDeptUser();
    }

    @RequestMapping(value = "/forceUpdatePwd")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|用户管理", note = "强制修改密码")
    public ResponseData forceUpdatePwd(@RequestParam("userName") String userName, @RequestParam("oldpassword") String oldpassword, @RequestParam("password1") String password1, @RequestParam("password2") String password2) {

        ResponseData data = ResponseData.ok();
        if (password1.equals(password2)) {
            if (oldpassword.equals(password1)) {
                return ResponseData.customerError(1001, "新密码不能和旧密码一致");
            }
            if (!ValidateUtil.checkPwd(password1)) {
                return ResponseData.customerError(1001, "新密码安全级别较低，请重新设置");
            }
            User user = userService.getUserByUsername(userName);
            if (user != null) {
                if (user.getPassword().equals(MD5Utils.encode(oldpassword))) {
                    userService.updatePassword(user.getId(), MD5Utils.encode(password1));
                } else {
                    return ResponseData.customerError(1001, "旧密码不正确！");
                }
            } else {
                return ResponseData.customerError(1001, "旧密码不正确");
            }

            data.putDataValue("message", "操作成功");
        } else {
            return ResponseData.customerError(1001, "新密码、确认新密码，两次密码输入不一致！");
        }
        return data;
    }

    @RequestMapping(value = "/onHandover")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|用户管理", note = "用户交接")
    @Verify(code = "/user/onHandover", module = "系统管理/用户交接")
    public ResponseData onHandover(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            userService.onHandover(id);
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }
    @RequestMapping("/back")
    @ResponseBody
    @Log(opType = OperateType.UPDATE,module = "系统管理|用户管理",note = "用户找回")
    public ResponseData back(@RequestParam("id") Integer id){
        try{
            ResponseData data = ResponseData.ok();
            userService.back(id);
            data.putDataValue("message","操作成功");
            return data;
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }


    @RequestMapping(value = "/offHandover")
    @ResponseBody
    @Log(opType = OperateType.UPDATE, module = "系统管理|用户管理", note = "用户取消交接")
    @Verify(code = "/user/offHandover", module = "系统管理/用户取消交接")
    public ResponseData offHandover(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            userService.offHandover(id);
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }
    @RequestMapping(value = "/checkDuplicateUserName")
    @ResponseBody
    public ResponseData checkDuplicateUserName(@RequestParam(value = "id",required=false) Integer id,@RequestParam("name") String name) {
        return userService.checkDuplicateUserName(id,name.trim());
    }

    @RequestMapping(value = "/checkDuplicateName")
    @ResponseBody
    public ResponseData checkDuplicateName(@RequestParam(value = "id",required=false) Integer id,@RequestParam("name") String name) {
//        Map map = new HashMap();
//        map.put("id",id);
//        map.put("name",name.trim());
//        return userService.checkDuplicateName(map)?1:0;
            return  userService.checkDuplicateName(id,name);

    }

    @RequestMapping(value = "/getRoleForMediaPlate")
    @ResponseBody
    @Verify(code = "/user/getRoleForMediaPlate", module = "系统设置/媒体板块赋权")
    public ResponseData getRoleForMediaPlate() {
        return ResponseData.ok();
    }


    @RequestMapping("/listUserByTypeAndCompanyCode")
    @ResponseBody
    public PageInfo<Map> listUserByTypeAndCompanyCode(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        PageInfo<Map> list = null ;
        try {
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            User user = AppUtil.getUser();
            map.put("companyCode",user.getCompanyCode());
            List<Map> result = userService.listUserByTypeAndCompanyCode(map);
            list = new PageInfo<>(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @RequestMapping("/listUserByCompanyCode")
    @ResponseBody
    public PageInfo<Map> listUserByCompanyCode(@PageableDefault(size = 5) Pageable pageable,@RequestParam Map map) {
        PageInfo<Map> list = null ;
        try {
            PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
            User user = AppUtil.getUser();
            map.put("companyCode",user.getCompanyCode());
            List<Map> result = userService.listUserByCompanyCode(map);
            list = new PageInfo<>(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取库存仓库管理员信息
     * @return
     */
    @RequestMapping("/getCKZYInfo")
    @ResponseBody
    public ResponseData getCKZYInfo(){
        ResponseData data=ResponseData.ok();
        List<User> list= userService.listByTypeAndCode("CK","ZY",AppUtil.getUser().getCompanyCode());
        data.putDataValue("list",list);
        return data;
    }

    @RequestMapping(value = "/getUserByUsername")
    @ResponseBody
//    @Log(opType = OperateType.QUERY, module = "用户管理", note = "根据id查询用户")
    public ResponseData getUserByUsername(@RequestParam("username") String username) {
        try {
            ResponseData data = ResponseData.ok();
            User su = userService.intranetGetUserByUsername(username);
            data.putDataValue("user", su);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @GetMapping(value = "/getUserByUserId/{id}")
    @ResponseBody
    public ResponseData getUserByUserId(@PathVariable Long id) {
        try {
            ResponseData data = ResponseData.ok();
            Map user = userService.getUserByUserId(id.intValue());
            data.putDataValue("user", user);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @GetMapping("/intranetAll/{pageNum}/{pageSize}")
    @ApiOperation(value = "查询所有用户分页", notes = "查询所有用户分页")
    @Log(opType = OperateType.QUERY, module = "系统管理/查询所有用户分页", note = "查询所有用户分页")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "当前页数", required = true, paramType = "query", dataType = "Integer"), @ApiImplicitParam(name = "pageSize", value = "每页显示条数", required = true, paramType = "query", dataType = "Integer")})
    @ResponseBody
    public PageInfo<List<Map>> nwAll(@ApiParam(value = "当前页数") @PathVariable("pageNum") int pageNum, @ApiParam(value = "每页显示条数") @PathVariable("pageSize") int pageSize,
                                     @RequestParam(required = false)String keyword) {

        return userService.intranetlistPg(pageNum, pageSize,keyword);
    }

    @GetMapping("/intranetUserAll/{pageNum}/{pageSize}")
    @ApiOperation(value = "查询所有用户分页", notes = "查询所有用户分页")
    @Log(opType = OperateType.QUERY, module = "系统管理/查询所有用户分页", note = "查询所有用户分页")
    @ApiImplicitParams({@ApiImplicitParam(name = "pageNum", value = "当前页数", required = true, paramType = "query", dataType = "Integer"), @ApiImplicitParam(name = "pageSize", value = "每页显示条数", required = true, paramType = "query", dataType = "Integer")})
    @ResponseBody
    public PageInfo<List<Map>> nwIntranetAll(@ApiParam(value = "当前页数") @PathVariable("pageNum") int pageNum, @ApiParam(value = "每页显示条数") @PathVariable("pageSize") int pageSize,
                                     @RequestParam(required = false)String keyword) {

        return userService.intranetUserListPg(pageNum, pageSize,keyword);
    }
}