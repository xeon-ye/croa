package com.qinfei.qferp.mapper.word;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.word.Keywords;

import java.util.List;
import java.util.Map;

/**
 * @CalssName keywordsMapper
 * @Description 关键字-菜单关系表
 * @Author tsf
 * @Date 2019/9/23
 */
public interface KeywordsMapper extends BaseMapper<Keywords, Integer> {

    List<Keywords> getByGroupId(Integer id);

    Keywords getByName(Map map);

    List<Keywords> queryKeywords(Map map);

    void saveKeywords(Keywords keywords);

    void editKeywords(Keywords keywords);

    void saveKeywordsList(List<Keywords> list);

    void deleteKeyWords(Integer id);
}
