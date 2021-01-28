package com.qinfei.qferp.service.inventory;
import com.qinfei.qferp.entity.inventory.ReceiveDetails;
import java.util.List;
import java.util.Map;

/**
 * 物品领用明细接口
 * @author tsf
 */
public interface IReceiveDetailsService {
    /**
     * 根据领用id查询领用明细记录
     * @param id
     * @param companyCode
     * @return
     */
    List<ReceiveDetails> getReceiveDetailById(Integer id,String companyCode);

    List<ReceiveDetails> getStockDetailByWareId(Map map);

    /**
     * 根据领用明细id查询明细信息
     * @param id
     * @return
     */
    ReceiveDetails getById(Integer id);

    /**
     * 批量添加物品领用信息
     * @param details
     */
    void saveReceiveDetailsBatch(List<ReceiveDetails> details);

    /**
     * 修改领用明细出库数量
     * @param map
     */
    void editReceiveDetailsByParam(Map map);

    /**
     * 删除物品领用明细
     * @param id
     */
    void delReceiveDetails(Integer id);

    /**
     * 物品领用明细物理删除
     * @param id
     */
    void deleteReceiveDetails(Integer id);
}
