package com.qinfei.qferp.service.fee;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.fee.TaxUser;
import com.qinfei.qferp.entity.sys.User;

import java.util.List;
import java.util.Map;

public interface ITaxUserService {


    List<User> assistantUser(Map<String,Object> map);

    List<TaxUser> getTaxUser(Integer taxId);

    List<User> taxAssistant(String taxType);

    void editDictUser(Integer dictId1);

    void insertAssistant(List<TaxUser> uId);

}
