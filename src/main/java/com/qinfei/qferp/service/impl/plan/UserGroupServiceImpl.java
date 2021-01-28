package com.qinfei.qferp.service.impl.plan;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.plan.UserGroupMapper;
import com.qinfei.qferp.service.plan.IUserGroupService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @CalssName UserGroupServiceImpl
 * @Description 用户群组服务
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:07
 * @Version 1.0
 */
@Service
public class UserGroupServiceImpl implements IUserGroupService {
    @Autowired
    UserGroupMapper userGroupMapper;

    @Override
    public List<User> listBusinessPart(String name){
        User user = AppUtil.getUser();
        if(user == null){
            throw  new QinFeiException(1001, "请先登录");
        }
        String companyCode = user.getCompanyCode();
        return userGroupMapper.listBusinessPart1(name, companyCode);
    }

    @Override
    public PageInfo<UserGroup> listPg(Map map, Pageable pageable){
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1001,"请先登录");
        }
        map.put("userId",user.getId());
        map.put("deptId",user.getDeptId());
        map.put("companyCode", user.getCompanyCode());
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<UserGroup> list = userGroupMapper.listPg(map);
        return new PageInfo(list);

    }
    @Override
    public UserGroup getById(Integer id){
        return  userGroupMapper.getById(id);
    }

    @Override
    public List<UserGroup> queryUserId(Integer id) {
        return userGroupMapper.queryUserId(id);
    }

    @Override
    @Transactional
    public void delById(Integer id) {
        if(id == null){
            throw new QinFeiException(1002, "请选择要删除的期号");
        }
        User user = AppUtil.getUser();
        if(user == null){
            throw new QinFeiException(1002, "请先登录");
        }
        UserGroup userGroup= getById(id);
        if(userGroup == null){
            throw new QinFeiException(1002, "期号不存在");
        }
        userGroup.setState(IConst.STATE_DELETE);
        userGroup.setUpdateId(user.getId());
        userGroupMapper.updateState(userGroup);
    }


}
