package com.qinfei.qferp.service.impl.word;
import com.qinfei.qferp.entity.word.Keywords;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.word.KeywordsMapper;
import com.qinfei.qferp.service.word.IKeywordsService;
import com.qinfei.qferp.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName KeywordsService
 * @Description 关键字-菜单关系表
 * @Author tsf
 * @Date 2019/9/23
 */
@Service
public  class KeywordsService implements IKeywordsService {
    @Autowired
    KeywordsMapper keywordsMapper;

    @Override
    public List<Keywords> listPg(Map map) {
        return keywordsMapper.queryKeywords(map);
    }

    @Override
    public void saveKeywords(String name,Integer groupId) {
        Keywords keywords = new Keywords();
        keywords.setId(null);
        User user = AppUtil.getUser();
        keywords.setName(name);
        keywords.setGroupId(groupId);
        keywords.setCreateId(user.getId());
        keywords.setCompanyCode(user.getCompanyCode());
        keywords.setCreateDate(new Date());
        keywords.setState(0);
        keywordsMapper.saveKeywords(keywords);
    }

    @Override
    public List<Keywords> getByGroupId(Integer id) {
        return keywordsMapper.getByGroupId(id);
    }

    @Override
    public Keywords getByName(String keyName,Integer groupId,Integer id) {
        Map map = new HashMap();
        map.put("keyName",keyName);
        map.put("groupId",groupId);
        map.put("id",id);
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        return keywordsMapper.getByName(map);
    }

    @Override
    public void saveKeywordsList(List<Keywords> list) {
        keywordsMapper.saveKeywordsList(list);
    }

    @Override
    public void editKeywords(Keywords keywords) {
        keywordsMapper.editKeywords(keywords);
    }

    @Override
    public void deleteKeywords(Integer id) {
        keywordsMapper.deleteKeyWords(id);
    }

}
