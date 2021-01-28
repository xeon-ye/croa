package com.qinfei.qferp.mapper.inventory;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventory.ReceiveApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 物品领用数据库接口
 * @author tsf
 */
public interface ReceiveApplyMapper extends BaseMapper<ReceiveApply,Integer> {
    /**
     * 根据id查询物品领用信息
     * @param id
     * @return
     */
    ReceiveApply getById(@Param("id") Integer id);

    /**
     * 获取物品领用数据数量
     * @param map
     * @return
     */
    Integer getApplyCount(Map map);

    /**
     * 获取物品领用数据
     * @param map
     * @return
     */
    List<Map> listPg(Map map);

    /**
     * 保存物品领用数据
     * @param apply
     * @return
     */
    int saveApply(ReceiveApply apply);

    /**
     * 修改物品领用数据
     * @param apply
     * @return
     */
    int editApply(ReceiveApply apply);

    /**
     * 领用出库选择领用订单所需数据
     * @param map
     * @return
     */
    List<Map> orderList(Map map);

    /**
     * 编辑查看领用出库选择领用订单所需数据
     * @param map
     * @return
     */
    List<Map> orderList2(Map map);

    /**
     * 删除物品领用信息
     * @param id
     */
    void delApply(Integer id);

    /**
     * 修改物品领用状态
     * @param state
     * @param id
     */
    void editApplyState(@Param("state")Integer state,@Param("id")Integer id);
}
