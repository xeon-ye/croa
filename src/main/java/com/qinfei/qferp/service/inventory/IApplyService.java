package com.qinfei.qferp.service.inventory;

import com.qinfei.qferp.entity.inventory.ReceiveApply;
import com.qinfei.qferp.entity.inventory.ReceiveReturn;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 物品领用接口
 * @author tsf
 */
public interface IApplyService {
    /**
     * 根据id查询物品领用信息
     * @param id
     * @return
     */
    ReceiveApply getById(Integer id);

    /**
     * 根据id,仓库id查询物品领用信息
     * @param id
     * @return
     */
    ReceiveApply getByWareIdAndApplyId(Integer id);

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
    PageInfo<Map> listPg(Map map,Pageable pageable);

    /**
     * 获取物品使用数据数量
     * @param map
     * @return
     */
    Integer getUserApplyCount(Map map);

    /**
     * 获取物品使用数据
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Map> getUserApplyData(Map map,Pageable pageable);

    /**
     * 获取领用编号
     * @return
     */
    String getApplyCode();

    /**
     * 保存物品领用数据
     * @param apply
     * @return
     */
    ReceiveApply saveApply(ReceiveApply apply,List<Integer> type, List<Integer> goodsId, List<String> unit, List<Integer> amount,List<Double> price,List<Double> totalMoney, List<Integer> handleId, List<Date> returnDate);

    /**
     * 修改物品领用数据
     * @param apply
     * @return
     */
    ReceiveApply editApply(ReceiveApply apply,List<Integer> type, List<Integer> goodsId, List<String> unit, List<Integer> amount,List<Double> price,List<Double> totalMoney, List<Integer> handleId, List<Date> returnDate);

    /**
     * 物品领用流程更新状态
     * @param apply
     * @return
     */
    void processApply(ReceiveApply apply);

    /**
     * 领用出库加载可选择的领用订单
     * @param map
     * @return
     */
    PageInfo<Map> orderList(Map map);

    /**
     * 编辑查看领用出库加载可选择领用订单
     * @param map
     * @return
     */
    PageInfo<Map> orderList2(Map map);

    /**
     * 删除物品领用信息
     * @param id
     */
    void delApply(Integer id);
}
