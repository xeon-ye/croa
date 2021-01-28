package com.qinfei.qferp.service.impl.fee;

import com.alibaba.fastjson.JSONObject;
import com.qinfei.qferp.mapper.fee.UserAccountMapper;
import com.qinfei.qferp.service.fee.IUserAccountService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 请款用户账户业务层
 *
 * @author: 66
 * @date: 2020/9/14 17:07
 */
@Service
public class UserAccountService implements IUserAccountService {

    @Resource
    UserAccountMapper userAccountMapper;

    /**
     * 插入数据
     *
     * @param receivingBank    收款开户行
     * @param receivingAccount 收款账号
     * @param createUserId     创建用户id
     * @return 影响数据行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(String receivingBank, String receivingAccount, Integer createUserId) {
        return userAccountMapper.insert(receivingBank, receivingAccount, createUserId);
    }

    /**
     * 保存数据
     *
     * @param receivingBank    收款开户行
     * @param receivingAccount 收款账号
     * @param createUserId     创建用户id
     * @return 影响数据行数
     */
    @Override
    public int save(String receivingBank, String receivingAccount, Integer createUserId) {
        if (StringUtils.isEmpty(receivingBank) || StringUtils.isEmpty(receivingAccount)) {
            return 0;
        }
        int count = userAccountMapper.findCountByReceivingAccountAndUserId(createUserId, receivingAccount);
        if (count > 0) {
            return 0;
        }
        return insert(receivingBank, receivingAccount, createUserId);
    }

    /**
     * 根据用户id查找列表
     *
     * @param userId 用户id
     * @return 列表
     */
    @Override
    public List<JSONObject> findListByUserId(Integer userId) {
        return userAccountMapper.findListByUserId(userId);
    }
}
