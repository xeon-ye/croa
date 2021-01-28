package com.qinfei.qferp.service.workbench;

import java.util.List;
import java.util.Map;

import com.qinfei.qferp.entity.workbench.MessageRead;
import org.springframework.data.domain.Pageable;

import com.qinfei.qferp.entity.workbench.Message;
import com.github.pagehelper.PageInfo;

public interface IMessageService {
	PageInfo<Map> list(Map item, Pageable pageable);

    //根据条件查询未读消息id集合
	List<Integer> queryMessageIds(Map map);

	/**
	 * 获取消息类型的数量
	 * @return
	 */
	List<Map> getMessageParentTypeNum();

	boolean readMessage(Message message);

	boolean addMessage(Message message);

	/**
	 * 批量发送消息；
	 * 
	 * @param messages：消息集合；
	 */
	void batchAddMessage(List<Message> messages);

	/**
	 * 根据待办id查询消息id集合
	 * @param itemId
	 * @return
	 */
	List<Integer> queryMessageIdsByItemId(Integer itemId);

	/**
	 * 根据待办id集合查询消息ids
	 * @param list
	 * @return
	 */
	List<Integer> queryIdsByItemIds(List<Integer> list);

	/**
	 * 批量修改已读状态
	 */
	 void batchUpdateMessage(Integer [] ids);

	/**
	 * 一键修改已读状态
	 */
	void batchAllMessage(Map map);

	/**
	 * 消息修改成已读状态
	 * @param ids
	 */
	void updateMessage(List<Integer> ids);
}
