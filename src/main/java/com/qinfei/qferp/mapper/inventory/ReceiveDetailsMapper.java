package com.qinfei.qferp.mapper.inventory;
import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventory.ReceiveDetails;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 物品领用明细数据库接口
 * @author tsf
 */
public interface ReceiveDetailsMapper extends BaseMapper<ReceiveDetails,Integer> {

    /**
     * 根据领用id查询领用明细记录
     * @param id
     * @return
     */
    List<ReceiveDetails> getReceiveDetailById(@Param("id")Integer id);

    List<ReceiveDetails> getStockDetailByWareId(Map map);

    /**
     * 根据领用明细id查询明细信息
     * @param id
     * @return
     */
    ReceiveDetails getById(@Param("id") Integer id);

    /**
     * 批量添加物品领用明细信息
     * @param details
     */
    void saveReceiveDetailsBatch(List<ReceiveDetails> details);

    /**
     * 修改领用明细出库数量
     * @param map
     */
    void editReceiveDetailsByParam(Map map);

    /**
     * 物品领用明细逻辑删除
     * @param id
     */
    void delReceiveDetails(Integer id);

    /**
     * 物品领用明细物理删除
     * @param id
     */
    void deleteReceiveDetails(Integer id);
}
