package com.qinfei.qferp.service.impl.plan;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.plan.UserGroup;
import com.qinfei.qferp.entity.plan.UserGroupType;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.plan.UserGroupMapper;
import com.qinfei.qferp.mapper.plan.UserGroupTypeMapper;
import com.qinfei.qferp.service.plan.IUserGroupTypeService;
import com.qinfei.qferp.utils.AppUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @CalssName UserGroupServiceImpl
 * @Description 用户群组类型服务
 * @Author xuxiong
 * @Date 2019/8/9 0009 18:07
 * @Version 1.0
 */
@Service
public class UserGroupTypeServiceImpl implements IUserGroupTypeService {
    @Autowired
    private UserGroupTypeMapper userGroupTypeMapper;
    @Autowired
    private UserGroupMapper userGroupMapper;


    @Override
    @Transactional
    public UserGroupType save(UserGroupType userGroupType) {
        try {
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(StringUtils.isEmpty(userGroupType.getName())){
                throw new QinFeiException(1002, "名称不能为空！");
            }
            if(CollectionUtils.isNotEmpty(userGroupTypeMapper.getSameNameCount(null, userGroupType.getName(),user.getCompanyCode()))){
                throw new QinFeiException(1002, "已存在相同名称的群组类型！");
            }
            Date currentDate = new Date();
            userGroupType.setCompanyCode(user.getCompanyCode());
            userGroupType.setCreateDate(currentDate);
            userGroupType.setCreateId(user.getId());
            userGroupType.setUpdateDate(currentDate);
            userGroupType.setUpdateId(user.getId());
            userGroupTypeMapper.save(userGroupType);
            return userGroupType;
        }catch (QinFeiException e){
            throw new QinFeiException(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "新增群组类型错误！");
        }
    }

    @Override
    @Transactional
    public UserGroupType update(UserGroupType userGroupType) {
        try {
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(StringUtils.isEmpty(userGroupType.getName())){
                throw new QinFeiException(1002, "名称不能为空！");
            }
            if(userGroupTypeMapper.getGroupTypeById(userGroupType.getId()) == null){
                throw new QinFeiException(1002, "群组类型不存在！");
            }
            if(CollectionUtils.isNotEmpty(userGroupTypeMapper.getSameNameCount(userGroupType.getId(),userGroupType.getName(),user.getCompanyCode()))){
                throw new QinFeiException(1002, "已存在相同名称的群组类型！");
            }
            userGroupType.setUpdateId(user.getId());
            userGroupTypeMapper.updateById(userGroupType);
            return userGroupType;
        }catch (QinFeiException e){
            throw new QinFeiException(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "修改群组类型错误！");
        }
    }

    @Override
    @Transactional
    public void del(Integer id) {
        try {
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(userGroupTypeMapper.getGroupTypeById(id) == null){
                throw new QinFeiException(1002, "群组类型不存在！");
            }
            int row = userGroupTypeMapper.updateStateById(id,-9,user.getId());
            if(row > 0){
                userGroupMapper.updateStateByType(id, -9, user.getId());
            }
        }catch (QinFeiException e){
            throw new QinFeiException(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "删除群组类型错误！");
        }
    }

    @Override
    public List<UserGroupType> listAllGroupType() {
        try {
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            return userGroupTypeMapper.listAllGroupType(user.getCompanyCode());
        }catch (QinFeiException e){
            throw new QinFeiException(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "查询群组类型错误！");
        }
    }
}
