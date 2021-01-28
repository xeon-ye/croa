package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventory.GoodsRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 库存操作记录表数据库接口
 */
public interface GoodsRecordMapper extends BaseMapper<GoodsRecord,Integer> {
    //添加库存操作记录
    void saveGoodsRecord(GoodsRecord record);

    //编辑库存操作记录
    void editGoodsRecord(GoodsRecord record);

    //根据库存id查询物品库存记录
    List<GoodsRecord> queryByInventoryId(Integer id);

    //根据外键id查询物品库存记录
    GoodsRecord getGoodsRecordById(@Param("type")Integer type,@Param("id") Integer id);

    //修改物品库存记录状态
    void editGoodsRecordState(Map map);

    //将已完成库存记录修改为删除状态，防止我使用列表进行多次库存操作记录
    void editGoodsRecordDelState(@Param("state")Integer state, @Param("id")Integer id);
}
