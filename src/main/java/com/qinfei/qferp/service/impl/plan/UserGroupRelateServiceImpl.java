package com.qinfei.qferp.service.impl.plan;

import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserGroupRelate;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.plan.UserGroupRelateMapper;
import com.qinfei.qferp.service.plan.IUserGroupRelateService;
import com.qinfei.qferp.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @CalssName UserGroupRelateServiceImpl
 * @Description 用户群组关系服务
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:06
 * @Version 1.0
 */
@Service
public class UserGroupRelateServiceImpl implements IUserGroupRelateService {
    @Autowired
    private UserGroupRelateMapper userGroupRelateMapper;

    @Override
    @Transactional
    public UserGroup add(UserGroup userGroup){
      userGroup.setId(null);
        User user = AppUtil.getUser();
        user = user == null ? new User() : user;
        userGroup.setCompanyCode(user.getCompanyCode());
        userGroup.setCreateId(user.getId());
        userGroup.setUpdateDate(new Date());
        userGroup.setUpdateId(user.getId());
        userGroupRelateMapper.insert(userGroup);

        return userGroup;
    }
    @Override
    @Transactional
    public  void  insertUserGroupRelate (List<Map> file){

      userGroupRelateMapper.insertUserGroupRelate(file);
    }
    @Override
    @Transactional
    public  UserGroup edit (UserGroup userGroup){
        User user = AppUtil.getUser();
        userGroup.setUpdateId(user.getId());
        userGroup.setUpdateDate(new Date());
        userGroupRelateMapper.updateGroup(userGroup);
        return userGroup;
    }
    @Override
    public void editUserId(Integer groupId1){
        userGroupRelateMapper.editUserId(groupId1);
    }


}
