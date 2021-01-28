package com.qinfei.qferp.service.fee;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author: 66
 * @date: 2020/9/14 17:04
 */
public interface IUserAccountService {

    /**
     * 插入数据
     *
     * @param receivingBank    收款开户行
     * @param receivingAccount 收款账号
     * @param createUserId     创建用户id
     * @return 影响数据行数
     */
    int insert(String receivingBank, String receivingAccount, Integer createUserId);

    /**
     * 保存数据
     *
     * @param receivingBank    收款开户行
     * @param receivingAccount 收款账号
     * @param createUserId     创建用户id
     * @return 影响数据行数
     */
    int save(String receivingBank, String receivingAccount, Integer createUserId);

    /**
     * 根据用户id查找列表
     *
     * @param userId 用户id
     * @return 列表
     */
    List<JSONObject> findListByUserId(Integer userId);

}
