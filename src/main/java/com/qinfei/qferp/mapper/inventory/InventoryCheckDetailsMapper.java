package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventoryStock.InventoryCheckDetails;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * (InventoryCheckDetails)表数据库访问层
 *
 * @author makejava
 * @since 2020-06-02 16:18:50
 */
public interface InventoryCheckDetailsMapper extends BaseMapper<InventoryCheckDetails, Integer> {

    /**
     * 获取库存预警分页数量
     * @param map
     * @return
     */
    Integer getPageCount(Map map);

    /**
     * 根据id获取库存预警数据
     * @return
     */
    List<Map> getStockData(Map map);

    /**
     * 获取库存数量
     * @param map
     * @return
     */
    List<Integer> getStockIds(Map map);

    /**
     * 根据库存盘点id获取库存明细信息
     * @param id
     * @return
     */
    List<InventoryCheckDetails> getCheckDetailsById(Integer id);

    /**
     * 根据盘点id查询所有盘点明细信息
     * @param id
     * @return
     */
    List<InventoryCheckDetails> queryByCheckId(Integer id);

    //根据盘点id查询有库存id
    List<Integer> queryGoodsIdsByCheckId(Integer id);

    /**
     * 新增库存盘点数据
     * @param list 实例对象
     * @return 影响行数
     */
    int addCheckDetailsBatch(List<InventoryCheckDetails> list);

    /**
     * 修改库存盘点数据
     *
     * @param checkRecord 实例对象
     * @return 影响行数
     */
    int editInventoryCheckDetails(InventoryCheckDetails checkRecord);

    void deleteCheckDetails(@Param("state") Integer state,@Param("id")Integer id);
}