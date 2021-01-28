package com.qinfei.qferp.service.impl.workbench;

import java.util.*;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.entity.workbench.MessageRead;
import com.qinfei.qferp.mapper.workbench.MessageMapper;
import com.qinfei.qferp.mapper.workbench.MessageReadMapper;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.util.StringUtils;

@Service
public class MessageService implements IMessageService {
	@Autowired
	MessageMapper messageMapper;
	@Autowired
	MessageReadMapper messageReadMapper;

	/**
	 * 查询消息
	 * @param item
	 * @param pageable
	 * @return
	 */
	public PageInfo<Map> list(Map item, Pageable pageable) {
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
		item.put("userId", AppUtil.getUser().getId());
		item.put("userDept", AppUtil.getUser().getDeptId());
		item.put("acceptWork", AppUtil.getUser().getId());
		List<Map> items = messageMapper.listMsg(item);
        return new PageInfo<Map>(items);
	}

	@Override
	public List<Integer> queryMessageIds(Map map) {
		map.put("userId", AppUtil.getUser().getId());
		map.put("userDept", AppUtil.getUser().getDeptId());
		map.put("acceptWork", AppUtil.getUser().getId());
		String state = (String) map.get("state");
		//如果前台不传值，则默认查询未读消息
		if(StringUtils.isEmpty(state)){
			map.put("state", 1);
			return messageMapper.queryMessageIds(map);
		}else {
			int stateQc = Integer.valueOf(state);
			map.put("state", stateQc);
			//如果查询条件是已读则不进行一键已读
			return stateQc==1?messageMapper.queryMessageIds(map):null;
		}
	}

	/**
	 * 读取消息
	 * 
	 * @param message
	 * @return
	 */
	public boolean readMessage(Message message) {
		MessageRead messageRead = new MessageRead(AppUtil.getUser().getId(), message.getId(), new Date());
		messageReadMapper.insert(messageRead);
		return true;
	}

	/**
	 * 添加一条消息
	 * 
	 * @param message
	 * @return
	 */
	public boolean addMessage(Message message) {
		message.setState(Const.MESSAGE_STATE_W);
		message.setCreateTime(new Date());
		messageMapper.insert(message);
		return true;
	}

	/**
	 * 批量发送消息；
	 *
	 * @param messages：消息集合；
	 */
	@Override
	public void batchAddMessage(List<Message> messages) {
		messageMapper.batchAddMessage(messages);
	}

	/**
	 * 根据待办id查询消息id集合
	 * @param itemId
	 * @return
	 */
	@Override
	public List<Integer> queryMessageIdsByItemId(Integer itemId) {
		return messageMapper.queryMessageIdsByItemId(itemId);
	}

	@Override
	public List<Integer> queryIdsByItemIds(List<Integer> list) {
		return messageMapper.queryIdsByItemIds(list);
	}

	/**
	 * 获取未读各个消息类型的数量
	 * @return
	 */
	@Override
	public List<Map> getMessageParentTypeNum() {
		Map map = new HashMap();
		map.put("userId", AppUtil.getUser().getId());
//		map.put("userDept", AppUtil.getUser().getDeptId());
		map.put("acceptWork", AppUtil.getUser().getId());
		return messageMapper.getMessageParentTypeNum(map);
	}

	/**
	 * 批量修改已读状态
	 */
	@Override
	public void batchUpdateMessage(Integer [] ids) {
		List<MessageRead> messageReadList = new ArrayList<>();
		Map<String,Object> map = new HashMap<>();
		int userId = AppUtil.getUser().getId();
		List<Integer> list = Arrays.asList(ids) ;
		map.put("ids",list);
		map.put("userId",userId);
		//筛选未读的消息id
		List<Integer> messageId = messageReadMapper.findMessageId(map);
		if(messageId.size()>0){
			for (int i=0;i<messageId.size();i++){
				MessageRead messageRead = new MessageRead(AppUtil.getUser().getId(), messageId.get(i), new Date());
				messageReadList.add(messageRead);
			}
			messageReadMapper.updateMessage(messageReadList);
		}
	}

	@Override
	public void batchAllMessage(Map map) {
		//step1:根据条件查询所有未读的消息Id
		map.put("userId", AppUtil.getUser().getId());
		map.put("userDept", AppUtil.getUser().getDeptId());
		map.put("acceptWork", AppUtil.getUser().getId());
		map.put("state",1);
        List<Integer> list = messageMapper.queryMessageIds(map);
		List<MessageRead> messageReadList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(list)){
			for (int i=0;i<list.size();i++){
				MessageRead messageRead = new MessageRead(AppUtil.getUser().getId(), list.get(i), new Date());
				messageReadList.add(messageRead);
			}
			//step2:批量修改已读消息
			messageReadMapper.updateMessage(messageReadList);
		}
	}

	/**
	 * 消息修改成已读状态
	 * @param ids
	 */
	@Override
	public void updateMessage(List<Integer> ids){
		if(CollectionUtils.isNotEmpty(ids)){
			List<MessageRead> messageReadList = new ArrayList<>();
			for(int i=0;i<ids.size();i++){
				MessageRead messageRead = new MessageRead(AppUtil.getUser().getId(), ids.get(i), new Date());
				messageReadList.add(messageRead);
			}
			messageReadMapper.updateMessage(messageReadList);
		}
	}
}
