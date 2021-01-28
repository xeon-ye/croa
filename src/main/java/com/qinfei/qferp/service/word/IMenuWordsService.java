package com.qinfei.qferp.service.word;

import com.qinfei.qferp.entity.word.MenuWords;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @CalssName IMenuWordsService
 * @Description 关键字管理接口
 * @Author tsf
 * @Date 2019/9/23
 */
public interface IMenuWordsService{
    /**
     * 根据id查询菜单关键字信息
     * @param id
     * @return
     */
    MenuWords getById(Integer id);
    /**
     * 获取菜单关键字分页信息
     * @param map
     * @return
     */
    PageInfo<Map> listPg(Map map, Pageable pageable);

    /**
     * 判断屏蔽允许只能存在一条
     * @param map
     * @return
     */
    Boolean checkRepeat(Map map);

    /**
     * 保存菜单关键字信息
     * @param menuWords
     */
    MenuWords saveMenuWords(MenuWords menuWords, String words);

    /**
     * 修改菜单关键字信息
     */
    MenuWords editMenuWords(MenuWords menuWords);

    /**
     * 删除菜单关键字信息
     * @param id
     */
    void deleteMenuWords(Integer id);

    void insertKeyId(List<Map> file);

    void updateState(Integer keyId);

    List<String> selectShielding(Integer menuId, Integer permissionType);
}
