package com.qinfei.qferp.mapper.fee;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.fee.TaxUser;
import com.qinfei.qferp.entity.meeting.Meeting;
import com.qinfei.qferp.entity.sys.User;

import java.util.List;
import java.util.Map;

public interface TaxUserMapper extends BaseMapper<Meeting, Integer> {

    List<User> assistantUser(Map<String,Object> map);

    int addTaxUser(List<TaxUser> taxUserList);

    List<TaxUser> getTaxUser(Integer taxId);

    List<User> taxAssistant(Map<String,Object> map);

    void editDictUser(Integer dictId1);


}
