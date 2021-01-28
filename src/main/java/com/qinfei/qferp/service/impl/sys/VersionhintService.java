package com.qinfei.qferp.service.impl.sys;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.sys.VersionHint;
import com.qinfei.qferp.entity.sys.VersionHintRelate;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.mapper.sys.VersionHintMapper;
import com.qinfei.qferp.mapper.sys.VersionHintRelateMapper;
import com.qinfei.qferp.service.sys.IVersionhintService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: VersionhintService
 * @Description: 系统提示表接口
 * @Author: Xuxiong
 * @Date: 2020/1/20 0020 17:03
 * @Version: 1.0
 */
@Service
@Slf4j
public class VersionhintService implements IVersionhintService {
    @Autowired
    private VersionHintMapper versionHintMapper;
    @Autowired
    private VersionHintRelateMapper versionHintRelateMapper;
    @Autowired
    private UserMapper userMapper;

    @Transactional
    @Override
    public void save(VersionHint versionHint) {
        try{
            User user = AppUtil.getUser();
            validateSave(user, versionHint);
            //如果是更新
            if(versionHint.getId() != null){
                versionHintMapper.updateStateById((byte) -9, user.getId(), versionHint.getId());
                versionHintRelateMapper.updateStateByHintId((byte) -9, versionHint.getId());
            }
            versionHintMapper.save(versionHint);
            List<Integer> userIds = userMapper.listUserByDeptIds(versionHint.getDeptIds());
            if(CollectionUtils.isNotEmpty(userIds)){
                List<VersionHintRelate> versionHintRelateList = new ArrayList<>();
                for(Integer userId : userIds){
                    VersionHintRelate versionHintRelate = new VersionHintRelate();
                    versionHintRelate.setHintId(versionHint.getId());
                    versionHintRelate.setUserId(userId);
                    versionHintRelate.setReadFlag((byte) 0);//是否阅读：0-否，1-是
                    versionHintRelate.setState((byte) 0);
                    versionHintRelateList.add(versionHintRelate);
                }
                if(CollectionUtils.isNotEmpty(versionHintRelateList)){
                    versionHintRelateMapper.saveBatch(versionHintRelateList);
                }
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "新增系统版本提示异常！");
        }
    }

    @Transactional
    @Override
    public void notice(int id) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            versionHintMapper.updateStateById((byte) 1, user.getId(), id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "系统版本提示通知异常！");
        }
    }

    @Transactional
    @Override
    public void del(int id) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            versionHintMapper.updateStateById((byte) -9, user.getId(), id);
            versionHintRelateMapper.updateStateByHintId((byte) -9, id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "删除系统版本提示异常！");
        }
    }

    @Override
    public PageInfo<VersionHint> list(Map<String, Object> map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<VersionHint> versionHintList = versionHintMapper.listHintByParam(map);
        return new PageInfo<>(versionHintList);
    }

    @Transactional
    @Override
    public void updateReadFlag() {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            versionHintRelateMapper.updateReadFlagById((byte) 1, user.getId());
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "不再提醒处理异常！");
        }
    }

    @Override
    public Map<String, List<Map<String, Object>>> listAllVersionHint() {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        List<Map<String, Object>> versionHintList = versionHintMapper.listAllVersionHint();
        if(CollectionUtils.isNotEmpty(versionHintList)){
            for(Map<String, Object> versionHint : versionHintList){
                if(!result.containsKey(String.valueOf(versionHint.get("userId")))){
                    result.put(String.valueOf(versionHint.get("userId")), new ArrayList<>());
                }
                result.get(String.valueOf(versionHint.get("userId"))).add(versionHint);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> historyVersionHint(Map<String, Object> param) {
        List<Map<String, Object>> result = new ArrayList<>();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("userId", user.getId());
                result = versionHintMapper.listHistoryVersionHint(param);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listDeptTree() {
        List<Map<String, Object>> deptTree = versionHintMapper.listDeptTree();
        if(CollectionUtils.isNotEmpty(deptTree)){
            deptTree.get(0).put("open", true);
        }
        return deptTree;
    }

    //校验新增
    private void validateSave(User user, VersionHint versionHint){
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(StringUtils.isEmpty(versionHint.getTitle())){
            throw new QinFeiException(1002, "提示标题不能为空！");
        }
        if(StringUtils.isEmpty(versionHint.getContent())){
            throw new QinFeiException(1002, "提示内容不能为空！");
        }
        if(CollectionUtils.isEmpty(versionHint.getDeptIds())){
            throw new QinFeiException(1002, "提示部门不能为空！");
        }
        versionHint.setDeptId(StringUtils.join(versionHint.getDeptIds(), ","));
        versionHint.setCreateId(user.getId());
        versionHint.setUpdateId(user.getId());
        versionHint.setCompanyCode(user.getCompanyCode());
    }

}
