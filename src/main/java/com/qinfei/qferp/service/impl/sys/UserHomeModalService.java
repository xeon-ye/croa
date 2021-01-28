package com.qinfei.qferp.service.impl.sys;

import com.qinfei.qferp.mapper.sys.UserHomeModalMapper;
import com.qinfei.qferp.service.sys.IUserHomeModalService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Created by yanhonghao on 2019/10/8 14:49.
 */
@Service
public class UserHomeModalService implements IUserHomeModalService {
    private final UserHomeModalMapper userHomeModalMapper;

    public UserHomeModalService(UserHomeModalMapper userHomeModalMapper) {
        this.userHomeModalMapper = userHomeModalMapper;
    }

    @Override
    @Transactional
    public void save(Integer userId, String homeModal) {
        userHomeModalMapper.save(userId, homeModal);
    }

    @Override
    @Transactional
    public void updateByUserId(Integer userId, String homeModal) {
        Map<String, String> result = userHomeModalMapper.findHomeModalByUserId(userId);
        if(result == null){
            userHomeModalMapper.save(userId, homeModal);
        }else {
            userHomeModalMapper.updateByUserId(userId, homeModal);
        }
    }

    @Override
    public String findHomeModalByUserId(Integer userId) {
        Map<String, String> resultMap = userHomeModalMapper.findHomeModalByUserId(userId);
        String homeModal = "";
        if(resultMap != null){
            homeModal = resultMap.get("homeModal");
        }
        return homeModal;
    }
}
