package com.qinfei.qferp.service.inventory;

import com.qinfei.qferp.entity.inventory.GoodsRecord;

import java.util.List;

public interface IGoodsRecordService {
    /**
     * 添加库存操作记录
     * @param record
     */
    void saveGoodsRecord(GoodsRecord record);

    /**
     * 编辑库存操作记录
     * @param record
     */
    void editGoodsRecord(GoodsRecord record);

    /**
     * 根据id查询库存操作记录
     * @param id
     * @return
     */
    GoodsRecord getGoodsRecordById(Integer type,Integer id);

    /**
     * 根据库存id查询物品库存记录
     * @param id
     * @return
     */
    List<GoodsRecord> queryByInventoryId(Integer id);
}
