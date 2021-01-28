package com.qinfei.qferp.service.impl.fee;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.fee.TaxUser;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.fee.TaxUserMapper;
import com.qinfei.qferp.service.fee.ITaxUserService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class TaxUserService implements ITaxUserService {
    @Autowired
    private TaxUserMapper taxUserMapper;
    @Autowired
    private IProcessService processService;
    @Override
    public List<User> assistantUser(Map<String, Object> param){
        User user= AppUtil.getUser();
        param.put("companyCode",user.getCompanyCode());
        return taxUserMapper.assistantUser(param);
    }

    @Override
    public List<TaxUser> getTaxUser(Integer taxId){
        return taxUserMapper.getTaxUser(taxId);

    }

    @Override
    public List<User> taxAssistant(String taxType){
        Map map = new HashMap();
        User user = AppUtil.getUser();
        map.put("taxType",taxType);
        map.put("companyCode",user.getCompanyCode());
        return taxUserMapper.taxAssistant(map);
    }

    @Override
    public void editDictUser(Integer dictId1){
        taxUserMapper.editDictUser(dictId1);
    }

    @Override
    public void insertAssistant(List<TaxUser> taxUserList){
        taxUserMapper.addTaxUser(taxUserList);
    }

}
