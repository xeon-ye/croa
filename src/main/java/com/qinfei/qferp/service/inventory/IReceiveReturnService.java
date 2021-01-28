package com.qinfei.qferp.service.inventory;

import com.qinfei.qferp.entity.inventory.ReceiveReturn;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 归还表(ReceiveReturn)表服务接口
 * @author tsf
 * @since 2020-05-07 10:23:54
 */
public interface IReceiveReturnService {

    /**
     * 通过ID查询物品归还信息
     * @param id 主键
     * @return 实例对象
     */
    ReceiveReturn queryById(Integer id);

    /**
     * 获取分页数量
     * @param map
     * @return
     */
    Integer getPageCount(Map map);

    /**
     * 查询分页数据
     * @param map
     * @param pageable
     * @return 对象列表
     */
    PageInfo<Map> listPg(Map map, Pageable pageable);

    /**
     * 归还code
     * @return
     */
    String getReturnCode();

    /**
     * 新增物品归还信息
     * @param receiveReturn 实例对象
     * @return 实例对象
     */
    ReceiveReturn addReturn(ReceiveReturn receiveReturn);

    /**
     * 修改物品归还信息
     * @param receiveReturn 实例对象
     * @return 实例对象
     */
    void editReturn(ReceiveReturn receiveReturn);

    /**
     * 修改流程状态
     * @param receiveReturn
     */
    void processReturn(ReceiveReturn receiveReturn);

    /**
     * 修改物品归还状态
     * @param state
     * @param id
     */
    void editReturnState(Integer state, Integer id);

    /**
     * 删除物品归还信息
     * @param id 主键
     */
    void deleteReturn(Integer id);
}