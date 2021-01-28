package com.qinfei.qferp.mapper.inventory;
import com.qinfei.qferp.entity.inventory.ReceiveReturn;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import java.util.Map;

/**
 * 物品归还数据库接口
 * @author tsf
 */
public interface ReceiveReturnMapper {
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
     * 新增物品归还信息
     * @param receiveReturn 实例对象
     * @return 实例对象
     */
    Integer addReturn(ReceiveReturn receiveReturn);

    /**
     * 修改物品归还信息
     * @param receiveReturn 实例对象
     * @return 实例对象
     */
    void editReturn(ReceiveReturn receiveReturn);

    /**
     * 修改归还状态
     * @param id 主键
     */
    void editReturnState(@Param("state")Integer state,@Param("id") Integer id);
}
