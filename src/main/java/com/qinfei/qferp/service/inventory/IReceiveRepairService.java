package com.qinfei.qferp.service.inventory;
import com.qinfei.qferp.entity.inventory.ReceiveRepair;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 报修表(ReceiveRepair)表服务接口
 * @author tsf
 * @since 2020-05-07 10:19:21
 */
public interface IReceiveRepairService {

    /**
     * 通过ID查询报修数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ReceiveRepair queryById(Integer id);

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
     * @param pageable 分页参数
     * @return 对象列表
     */
    PageInfo<Map> listPg(Map map, Pageable pageable);

    /**
     * 获取物品报修编号
     * @return
     */
    String getRepairCode();

    /**
     * 新增数据
     *
     * @param receiveRepair 实例对象
     * @return 实例对象
     */
    ReceiveRepair addRepair(ReceiveRepair receiveRepair);

    /**
     * 修改数据
     *
     * @param receiveRepair 实例对象
     * @return 实例对象
     */
    void editRepair(ReceiveRepair receiveRepair);

    /**
     * 物品报修流程更新状态
     * @param receiveRepair
     */
    void processRepair(ReceiveRepair receiveRepair);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    void deleteRepair(Integer id);
}