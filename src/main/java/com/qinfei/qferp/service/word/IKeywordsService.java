package com.qinfei.qferp.service.word;


import com.qinfei.qferp.entity.word.Keywords;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IKeywordsService
 * @Description 关键字管理接口
 * @Author tsf
 * @Date 2019/9/23
 */
public interface IKeywordsService {
    /**
     * 根据id查询关键字信息
     * @param id
     * @return
     */
    List<Keywords> getByGroupId(Integer id);

    /**
     * 根据关键字名称进行去重判断
     * @param keyName
     * @param groupId
     * @param id
     * @return
     */
    Keywords getByName(String keyName, Integer groupId, Integer id);
    /**
     * 查询关键字信息
     * @param map
     * @return
     */
    List<Keywords> listPg(Map map);

    /**
     * 添加关键字
     * @param name
     * @param groupId
     */
    void saveKeywords(String name, Integer groupId);

    /**
     * 添加关键字（集合）
     * @param list
     */
    void saveKeywordsList(List<Keywords> list);

    /**
     * 修改关键字
     * @param keywords
     */
    void editKeywords(Keywords keywords);

    /**
     * 删除关键字（菜单组）
     * @param id
     */
    void deleteKeywords(Integer id);
}
