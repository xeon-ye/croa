package com.qinfei.qferp.service.impl.sys;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.login.SessionManagement;
import com.qinfei.core.utils.IpUtils;
import com.qinfei.core.utils.MD5Utils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.sys.Resource;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.sys.UserBinding;
import com.qinfei.qferp.mapper.sys.UserBindingMapper;
import com.qinfei.qferp.service.impl.BaseService;
import com.qinfei.qferp.service.sys.*;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @CalssName UserBindingService
 * @Description 用户绑定接口
 * @Author xuxiong
 * @Date 2019/11/23 0023 11:24
 * @Version 1.0
 */
@Service
public class UserBindingService implements IUserBindingService {
    @Autowired
    private UserBindingMapper userBindingMapper;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IDeptService deptService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IResourceService resourceService;

    @Transactional
    @Override
    public void binding(UserBinding userBinding) {
        try{
            User user = AppUtil.getUser();
            validateBinding(user, userBinding); //数据校验
            UserBinding currentUserBinding = userBindingMapper.getUserBindingByUserId(user.getId()); //获取当前用户是否被绑定，第一次绑定时会同时绑定自己
            if(currentUserBinding == null){
                String unionId = UUIDUtil.get32UUID();
                userBinding.setUnionId(unionId);
                //创建当前用户关联对象
                UserBinding currentBinding = new UserBinding();
                BeanUtils.copyProperties(userBinding, currentBinding);
                currentBinding.setUserId(user.getId());
                if(userBinding.getEditFlag()){
                    userBindingMapper.save(currentBinding);
                    userBindingMapper.updateUnionByUserId(userBinding);
                }else{
                    List<UserBinding> userBindingList = new ArrayList<>();
                    userBindingList.add(userBinding);
                    userBindingList.add(currentBinding);
                    userBindingMapper.saveBatch(userBindingList);
                }
            }else {
                userBinding.setUnionId(currentUserBinding.getUnionId());
                if(userBinding.getEditFlag()){
                    userBindingMapper.updateUnionByUserId(userBinding);
                }else {
                    userBindingMapper.save(userBinding);
                }
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "关联用户出错，请联系技术人员！");
        }
    }

    @Override
    public List<UserBinding> bindingUserList() {
        User user = AppUtil.getUser();
        List<UserBinding> result = new ArrayList<>();
        if(user != null){
            UserBinding userBinding = userBindingMapper.getUserBindingByUserId(user.getId());
            if(userBinding != null && StringUtils.isNotEmpty(userBinding.getUnionId())){
                result = userBindingMapper.listUserBindingByUnionId(userBinding.getUnionId(), null);
            }
        }
        return result;
    }

    @Override
    public User exchangeUser(HttpSession session, Integer userId) {
        try{
            User user = AppUtil.getUser();
            User exchangeUser = validateExchangeUser(userId, user); //规则校验
            //先进行预登录，登录成功信息都有了之后，再退出当前用户，切换新的用户
            preLogin(exchangeUser); //预登录
            logout(user); //退出当前登录用户
            SessionManagement sessionManagement = SessionManagement.getInstance();
            sessionManagement.addSession(session.getId(), session);
            reloadLoginUser(exchangeUser, session); //重新加载登录用户
            return exchangeUser;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "切换用户出错，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void cancelBinding(Integer userId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(userId == null){
                throw new QinFeiException(1002, "请选择解绑用户！");
            }
            //判断切换用户和当前用户是否有关联关系，解除关联，浏览器不刷新还能切换登录
            UserBinding userBinding = userBindingMapper.getUserBindingByUserId(user.getId());
            if(userBinding == null){
               throw new QinFeiException(1002, "切换用户和当前用户已没有关联关系，请关闭弹窗重新打开查看是否有建立关联！");
            }else {
               UserBinding userBinding1 = userBindingMapper.getUserBindingByUserIdAndUnion(userId, userBinding.getUnionId());
               if(userBinding1 == null){
                    throw new QinFeiException(1002, "切换用户和当前用户已没有关联关系，请关闭弹窗重新打开查看是否有建立关联！");
               }
            }
            userBindingMapper.updateStateByUserId(userId, -9, user.getId());
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "用户解绑出错，请联系技术人员！");
        }
    }

    //校验关联信息
    private void validateBinding(User user, UserBinding userBinding){
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(StringUtils.isEmpty(userBinding.getBindingUserName())){
            throw new QinFeiException(1002, "用户名必输！");
        }
        if(StringUtils.isEmpty(userBinding.getBindingPassword())){
            throw new QinFeiException(1002, "用户密码必输！");
        }
        userBinding.setBindingUserName(userBinding.getBindingUserName().trim());
        userBinding.setBindingPassword(MD5Utils.encode(userBinding.getBindingPassword()));
        userBinding.setCreateId(user.getId());
        userBinding.setUpdateId(user.getId());
        User bindingUser = userBindingMapper.getUserByUserNameAndPassword(userBinding.getBindingUserName(), userBinding.getBindingPassword());
        if(bindingUser == null){
            throw new QinFeiException(1002, "用户名或密码错误！");
        }
        if(bindingUser.getId().equals(user.getId())){
            throw new QinFeiException(1002, "不允许关联自己！");
        }
        userBinding.setUserId(bindingUser.getId()); //设置关联用户ID
        UserBinding oldUserBinding = userBindingMapper.getUserBindingInfoByUserId(bindingUser.getId());
        //如果待关联的用户被绑定了，需要判断关联码是否正确
        if(oldUserBinding != null){
            if(StringUtils.isEmpty(userBinding.getUnionId())){
                throw new QinFeiException(1002, String.format("用户曾被关联过，请联系[%s]提供对应关联码进行关联变更！", oldUserBinding.getUser().getName()));
            }
            userBinding.setUnionId(userBinding.getUnionId().trim()); //去除空字符
            if(!oldUserBinding.getUnionId().equals(userBinding.getUnionId())){
                throw new QinFeiException(1002, String.format("用户曾被关联过，请联系[%s]提供对应关联码进行关联变更！", oldUserBinding.getUser().getName()));
            }
            userBinding.setEditFlag(true);
        }else {
            userBinding.setEditFlag(false);
        }
    }

    //校验切换用户
    private User validateExchangeUser(Integer userId, User user){
        User exchangeUser = null;
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(userId == null){
            throw new QinFeiException(1002, "请选择切换用户！");
        }
        //判断切换用户和当前用户是否有关联关系，解除关联，浏览器不刷新还能切换登录
        UserBinding userBinding = userBindingMapper.getUserBindingByUserId(user.getId());
        if(userBinding == null){
            throw new QinFeiException(1002, "切换用户和当前用户已没有关联关系，请关闭弹窗重新打开查看是否有建立关联！");
        }else {
            UserBinding userBinding1 = userBindingMapper.getUserBindingByUserIdAndUnion(userId, userBinding.getUnionId());
            if(userBinding1 == null){
                throw new QinFeiException(1002, "切换用户和当前用户已没有关联关系，请关闭弹窗重新打开查看是否有建立关联！");
            }
        }
        exchangeUser = userBindingMapper.getUserInfoById(userId);
        if(exchangeUser == null){
            throw new QinFeiException(1002, "切换用户不存在或已被删除！");
        }
        return exchangeUser;
    }

    //预登录，出现问题则不会退出当前用户
    private void preLogin(User exchangeUser){
        //设置角色
        exchangeUser.setRoles(roleService.queryRoleByUserId(exchangeUser.getId()));
        //设置部门
        String depts = deptService.idsByParentId(exchangeUser.getDeptId());
        exchangeUser.setDeptIdSet(new HashSet<>(Arrays.asList(depts.split(","))));
        exchangeUser.setCurrentDeptQx(currentDept(exchangeUser));
        exchangeUser.setCurrentCompanyQx(BaseService.currentCompanyQx(exchangeUser));
        HttpServletRequest request = AppUtil.getRequest();
        exchangeUser.setSessionId(AppUtil.getSession().getId());
        new Thread(() -> {
            String ip = IpUtils.getIpAddress(request);
            exchangeUser.setLoginIp(ip);
            exchangeUser.setMac(IpUtils.getMac(ip));
            exchangeUser.setLoginTime(new Date());
            userService.update(exchangeUser);
        }).start();
        // 图片显示优化；
        String image = exchangeUser.getImage();
        exchangeUser.setImage(StringUtils.isEmpty(image) ? "/img/mrtx_2.png" : image.replace("\\images\\", "/images/"));
    }

    //退出当前登录用户
    private void logout(User user){
        if(user != null){
            userService.logout(user);
        }
    }

    //重新加载登录用户
    private void reloadLoginUser(User exchangeUser, HttpSession session){
        session.setAttribute(IConst.USER_KEY, exchangeUser);
        List<Resource> set = resourceService.queryResourceByUserIdNew(exchangeUser.getId());
        // set放入session中
        session.setAttribute(IConst.USER_RESOURCE, set);
    }

    //判断当前用户是否具有当前部门的权限
    private boolean currentDept(User user) {
        boolean b = false;
        int level = user.getDept().getLevel();
        List<Role> roles = user.getRoles();
        if (roles != null) {
            for (Role r : roles) {
                if (IConst.ROLE_CODE_ZJ.equals(r.getCode()) || IConst.ROLE_CODE_ZJL.equals(r.getCode()) || IConst.ROLE_CODE_FZ.equals(r.getCode()) || IConst.ROLE_CODE_ZC.equals(r.getCode())  || IConst.ROLE_CODE_FZC.equals(r.getCode()) || (IConst.ROLE_TYPE_JT.equals(r.getType()) && IConst.ROLE_CODE_KJ.equals(r.getCode()))) {
                    return true;
                }
                if (IConst.ROLE_CODE_BZ.equals(r.getCode()) && IConst.ROLE_TYPE_CW.equals(r.getType())) {
                    return true;
                }
                if (IConst.ROLE_CODE_BZ.equals(r.getCode())) {
                    if (level >= 2) {
                        return true;
                    }
                }
                if (IConst.ROLE_CODE_ZZ.equals(r.getCode())) {
                    if (level >= 3) {
                        return true;
                    }
                }
            }
        }
        return b;
    }


}
