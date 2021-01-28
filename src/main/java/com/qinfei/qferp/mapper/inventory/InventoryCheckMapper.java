package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventoryStock.InventoryCheck;
import org.apache.ibatis.annotations.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Map;

/**
 * (InventoryCheck)表数据库访问层
 *
 * @author makejava
 * @since 2020-06-05 11:09:36
 */
public interface InventoryCheckMapper extends BaseMapper<InventoryCheck, Integer> {
    /**
     * 获取库存盘点数量
     * @param map
     * @return
     */
    Integer getPageCount(Map map);

    /**
     * 获取库存盘点分页数量
     * @param map
     * @return
     */
    List<Map> listPg(Map map);

    /**
     * 添加库存盘点
     * @param inventoryCheck
     * @return
     */
    Integer saveInventoryCheck(InventoryCheck inventoryCheck);

    /**
     * 修改库存盘点
     *
     * @param inventoryCheck 实例对象
     * @return 影响行数
     */
    void updateInventoryCheck(InventoryCheck inventoryCheck);

    void delInventoryCheck(@Param("state")Integer state,@Param("id") Integer id);

    /**
     * 查看库存盘点
     * @param id
     * @return
     */
    InventoryCheck editAjax(Integer id);
}