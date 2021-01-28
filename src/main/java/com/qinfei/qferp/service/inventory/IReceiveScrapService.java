package com.qinfei.qferp.service.inventory;
import com.qinfei.qferp.entity.inventory.ReceiveScrap;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 物品报废(ReceiveScrap)表服务接口
 *
 * @author tsf
 * @since 2020-05-07 10:23:54
 */
public interface IReceiveScrapService {

    /**
     * 通过id查询物品报废信息
     *
     * @param id 主键
     * @return 实例对象
     */
    ReceiveScrap queryById(Integer id);

    /**
     * 获取分页数量
     * @param map
     * @return
     */
    Integer getPageCount(Map map);


    /**
     * 查询分页数据
     *
     * @param map
     * @param pageable
     * @return 对象列表
     */
    PageInfo<Map> listPg(Map map, Pageable pageable);

    //自动生成报废编号
    String getScrapCode();

    /**
     * 新增物品报废数据
     *
     * @param receiveScrap 实例对象
     * @return 实例对象
     */
    ReceiveScrap addScrap(ReceiveScrap receiveScrap);

    /**
     * 修改物品报废数据
     *
     * @param receiveScrap 实例对象
     * @return 实例对象
     */
    void editScrap(ReceiveScrap receiveScrap);

    /**
     * 更新流程状态
     * @param receiveScrap
     */
    void processScrap(ReceiveScrap receiveScrap);

    /**
     * 删除物品报修数据
     *
     * @param id 主键
     */
    void deleteScrap(Integer id);

}