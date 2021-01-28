package com.qinfei.qferp.service.sms;

import com.aliyuncs.exceptions.ClientException;
import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.sys.User;

public interface ISmsService {

	/**
     * 发送验证码
     * @param phone
     * @return
     */
    ResponseData sendVerifyCode(String phone) throws ClientException;

    /**
     * 校验登陆短信
     * @param user 
     * @return
     */
	ResponseData checkSms(User user);
}
