package com.qinfei.qferp.service.sys;

import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.sys.UserBinding;
import org.springframework.cache.annotation.Cacheable;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @CalssName IUserBindingService
 * @Description 用户绑定接口
 * @Author xuxiong
 * @Date 2019/11/23 0023 11:23
 * @Version 1.0
 */
public interface IUserBindingService {
    String CACHE_KEY = "UserBinding";

    void binding(UserBinding userBinding);

    List<UserBinding> bindingUserList();

    User exchangeUser(HttpSession session, Integer userId);

    void cancelBinding(Integer userId);
}
