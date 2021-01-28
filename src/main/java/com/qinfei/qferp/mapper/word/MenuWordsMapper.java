package com.qinfei.qferp.mapper.word;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.word.MenuWords;

import java.util.List;
import java.util.Map;

/**
 * @CalssName MenuWordsMapper
 * @Description 菜单组（关键字）实现类
 * @Author tsf
 * @Date 2019/9/23
 */
public interface MenuWordsMapper extends BaseMapper<MenuWords, Integer> {


    MenuWords getById(Integer id);

    List<Map> queryMenuWords(Map map);

    List<MenuWords> checkRepeat(Map map);

    void saveMenuWords(MenuWords menuWords);

    void editMenuWords(MenuWords menuWords);

    void deleteMenuWords(Integer id);

    void insertKeyId(List<Map> list);

    void updateState(Integer keyId);

    List<String> selectShielding(Map map);
}
