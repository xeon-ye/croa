package com.qinfei.qferp.flow.listener;

import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.EntityUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 行政总监直接结束完成处理；
 */
public class AdministrativeGateFinishHandler implements ExecutionListener {

	@Override
	public void notify(DelegateExecution delegateExecution) {
		boolean agree = delegateExecution.getVariable("agree", Boolean.class);
		int state;
		if (agree) {
			state = IConst.STATE_FINISH;
		} else {
			state = IConst.STATE_REJECT;
		}
		// 用于保存创建代办事项和消息数据的集合；
		Map<String, Object> map = new HashMap<>();

		// 更新消息内容，根据状态来确定消息内容；
		String processName = delegateExecution.getVariable(IProcess.PROCESS_NAME, String.class);
		// 获取需要审核的数据ID，类型不确定，统一使用字符串获取；
		String dataId = delegateExecution.getVariable("dataId", String.class);
		dataId = dataId == null ? "-1" : dataId;
		// WebSocket发送消息需要此字段；
		map.put(IProcess.PROCESS_NAME, processName);

		// 发送信息需要的信息；
		User loginUser = AppUtil.getUser();
		String userImage = loginUser.getImage();
		// 获取消息显示的图片；
		String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
		String dataUrl = delegateExecution.getVariable(IProcess.PROCESS_FINISH_URL, String.class);

		map.put("newPic", pictureAddress);
		map.put("newContent", String.format("您提交的%s审核已完成。", processName));
		// 处理中的待办事项跳转到流程审核页面；
		map.put("transactionAddress", dataUrl);

		// 获取流程发起人信息；
		Integer initiatorDept = delegateExecution.getVariable("initiatorDept", Integer.class);
		Integer initiatorWorker = delegateExecution.getVariable("initiatorWorker", Integer.class);
		map.put("initiatorDept", initiatorDept);
		map.put("initiatorWorker", initiatorWorker);

		// 获取上个待办事项的ID，在添加待办事项的时候更新上个待办事项的状态；
		Integer oldItemId = delegateExecution.getVariable("itemId", Integer.class);
		if (oldItemId != null) {
			updateWork(oldItemId);
		}

		// 获取流程接收人的信息；
		Integer acceptDept = delegateExecution.getVariable("acceptDept", Integer.class);
		Integer acceptWorker = delegateExecution.getVariable("acceptWorker", Integer.class);
		if (acceptDept != null && acceptWorker != null) {
			map.put("acceptDept", acceptDept);
			map.put("acceptWorker", acceptWorker);
		}

		// 如果有新消息，通知流程发起人；
		Object object = map.get("newContent");
		if (object != null) {
			map.put("pic", map.get("newPic").toString());
			map.put("content", object.toString());
			// 接收人改为流程发起人；
			map.put("acceptDept", initiatorDept);
			map.put("acceptWorker", initiatorWorker);
			//消息分类
			map.put("parentType",3);//通知
			sendMessage(map);
		}

		// 流程当前的任务ID；
		Administrative leave = new Administrative();
		leave.setId(Integer.parseInt(dataId));
		leave.setState(state);
		IAdministrativeLeaveService leaveService = SpringUtils.getBean("administrativeLeaveService");
		leaveService.processLeava(leave);
	}

	//消息发送默认方法
	void sendMessage(Map<String, Object> map) {
		// 发送消息；
		Object object = EntityUtil.getNewObject(map, Message.class);
		if (object != null) {
			Message message = (Message) object;
			if (!StringUtils.isEmpty(message.getContent())) {
				// 系统右侧消息推送；
				IMessageService messageService = SpringUtils.getBean("messageService");
				messageService.addMessage(message);

				// WebSocket的消息推送；
				WSMessage wsMessage = new WSMessage();

				// 接收消息的用户信息；
				Integer userId = message.getAcceptWorker();
				wsMessage.setReceiveUserId(userId.toString());
				IUserService userService = SpringUtils.getBean("userService");
				User user = userService.getById(userId);
				// 防止用户被删除出现异常；
				if (user != null) {
					wsMessage.setReceiveName(user.getName());

					// 发送消息的用户信息；
					User loginUser = AppUtil.getUser();
					wsMessage.setSendName(loginUser.getName());
					wsMessage.setSendUserId(loginUser.getId().toString());
					wsMessage.setSendUserImage(loginUser.getImage());

					// 消息内容；
					wsMessage.setContent(message.getContent());
					wsMessage.setSubject(map.get(IProcess.PROCESS_NAME).toString());
					wsMessage.setUrl(map.get("transactionAddress").toString());
					// 提交信息；
					WebSocketServer.sendMessage(wsMessage);
				}
			}
		}
	}

	//修改待办默认方法
	void updateWork(int itemId){
		Items items = new Items();
		items.setId(itemId);
		items.setTransactionState(Const.ITEM_Y);
		IItemsService itemsService = SpringUtils.getBean("itemsService");
		itemsService.finishItems(items);
	}
}