package com.qinfei.qferp.mapper.workbench;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.workbench.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface MessageMapper extends BaseMapper<Message, Integer> {

    List<Map> listMsg(Map params);

    //根据条件查询未读消息id集合
    List<Integer> queryMessageIds(Map map);

    List<Map> getMessageParentTypeNum(Map params);


    /**
     * 批量发送消息；
     *
     * @param messages：消息集合；
     */
    void batchAddMessage(List<Message> messages);

    /**
     * 根据待办id查询消息ids
     * @param itemId
     * @return
     */
    List<Integer> queryMessageIdsByItemId(@Param("itemId") Integer itemId);

    /**
     * 根据待办id集合查询消息ids
     * @param list
     * @return
     */
    List<Integer> queryIdsByItemIds(List<Integer> list);
}
