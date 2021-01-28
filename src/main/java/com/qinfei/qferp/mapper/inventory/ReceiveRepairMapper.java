package com.qinfei.qferp.mapper.inventory;
import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.inventory.ReceiveRepair;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

/**
 * 物品领用明细数据库接口
 * @author tsf
 */
public interface ReceiveRepairMapper extends BaseMapper<ReceiveRepair,Integer> {

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
    List<Map> listPg(Map map, Pageable pageable);

    /**
     * 新增报修数据
     *
     * @param receiveRepair 实例对象
     * @return 实例对象
     */
    Integer saveRepair(ReceiveRepair receiveRepair);

    /**
     * 修改报修数据
     *
     * @param receiveRepair 实例对象
     * @return 实例对象
     */
    void editRepair(ReceiveRepair receiveRepair);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    void deleteRepair(Integer id);
}
