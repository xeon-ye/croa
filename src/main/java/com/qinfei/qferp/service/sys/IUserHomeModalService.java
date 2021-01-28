package com.qinfei.qferp.service.sys;

/**
 * Created by yanhonghao on 2019/10/8 14:46.
 */
public interface IUserHomeModalService {
    void save(Integer userId, String homeModal);

    void updateByUserId(Integer userId, String homeModal);

    String findHomeModalByUserId(Integer userId);
}
