package com.qinfei.qferp.service.impl.word;
import com.qinfei.qferp.entity.word.Keywords;
import com.qinfei.qferp.entity.word.MenuWords;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.word.KeywordsMapper;
import com.qinfei.qferp.mapper.word.MenuWordsMapper;
import com.qinfei.qferp.service.word.IMenuWordsService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import org.flowable.spring.boot.app.App;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName MenuWordService
 * @Description 菜单组（关键字）实现类
 * @Author tsf
 * @Date 2019/9/23
 */
@Service
public class MenuWordsService implements IMenuWordsService {
    @Autowired
    MenuWordsMapper menuWordsMapper;
    @Autowired
    KeywordsMapper keywordsMapper;

    /**
     * 根据id查询菜单（关键字）
     * @param id
     * @return
     */
    @Override
    public MenuWords getById(Integer id) {
        return menuWordsMapper.getById(id);
    }

    /**
     * 查询所有菜单组
     * @param map
     * @return
     */
    public PageInfo<Map> listPg(Map map, Pageable pageable){
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        map.put("companyCode",AppUtil.getUser().getCompanyCode());
        List<Map> list = menuWordsMapper.queryMenuWords(map);
        PageInfo<Map> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public Boolean checkRepeat(Map map) {
        List<MenuWords> list = menuWordsMapper.checkRepeat(map);
        Boolean flag = false;
        if(list.size()>0){
            flag = false;
        }else{
            flag = true;
        }
        return flag;
    }

    /**
     * 保存菜单关键字关系
     * @param menuWords
     */
    @Transactional
    @Override
    public MenuWords saveMenuWords(MenuWords menuWords,String words) {
        try {
             menuWords.setId(null);
              menuWordsMapper.insert(menuWords);
//            if(words!=null && "".equals(words)){
//            String [] keys = words.split(",");
              User user = AppUtil.getUser();
//                List<Keywords> list = new ArrayList<>();
//                for(int i=0;i<keys.length;i++){
//                    Keywords keywords = new Keywords();
//                    keywords.setCreateId(user.getId());
//                    keywords.setCreateDate(new Date());
//                    keywords.setGroupId(id);
//                    keywords.setState(0);
//                    keywords.setKeyword(keys[i]);
//                    keywords.setCompanyCode(user.getCompanyCode());
//                    list.add(keywords);
//                }
//                keywordsMapper.saveKeywords(list);
//            }
            Keywords keywords = new Keywords();
            keywords.setCreateId(user.getId());
            keywords.setCreateDate(new Date());
            keywords.setGroupId(menuWords.getId());
            keywords.setState(0);
            keywords.setName(words);
            keywords.setCompanyCode(user.getCompanyCode());
            keywordsMapper.saveKeywords(keywords);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return menuWords;
    }

    @Override
    public MenuWords editMenuWords(MenuWords menuWords) {
        User user = AppUtil.getUser();
        menuWords.setUpdateDate(new Date());
        menuWords.setUpdateId(user.getId());
        menuWordsMapper.editMenuWords(menuWords);
        return menuWords;
    }

    @Transactional
    @Override
    public void deleteMenuWords(Integer id) {
        try {
            keywordsMapper.deleteKeyWords(id);
            menuWordsMapper.deleteMenuWords(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertKeyId(List<Map> file){
        menuWordsMapper.insertKeyId(file);

    }

    @Override
    public void updateState(Integer keyId){
        menuWordsMapper.updateState(keyId);
    }
    @Override
    public List<String> selectShielding(Integer menuId, Integer permissionType){
        User user =AppUtil.getUser();
        Map map= new HashMap();
        map.put("companyCode",user.getCompanyCode());
        map.put("menuId",menuId);
        map.put("permissionType",permissionType);
       return menuWordsMapper.selectShielding(map);
    }
}
