package com.qinfei.qferp.flow.listener1;

import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.flowable.FlowableMapper;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.EntityUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName ICommonTaskHandler
 * @Description 所有用户任务公共处理方法
 * @Author xuxiong
 * @Date 2019/9/19 0019 9:05
 * @Version 1.0
 */
public interface ICommonTaskHandler {

    //获取审核意见
    default boolean getOpinion(DelegateTask delegateTask){
        Boolean agree = delegateTask.getVariable("agree", Boolean.class);
        // 默认为通过，比如流程的开始和网关的跳转；
        if (agree == null) {
            agree = true;
        }
        return agree;
    }

    //消息发送默认方法
    default void sendMessage(Map<String, Object> map) {
        // 发送消息；
        Object object = EntityUtil.getNewObject(map, Message.class);
        if (object != null) {
            Message message = (Message) object;
            if (!StringUtils.isEmpty(message.getContent())) {
                // 系统右侧消息推送；
                IMessageService messageService = SpringUtils.getBean("messageService");
                //消息分类父级消息类型
                message.setParentType((Integer)map.get("parentType"));
                //子级消息类型
                message.setType((Integer)map.get("type"));
                Integer acceptWorker = (Integer)map.get("acceptWorker");
                Integer initiatorWorker = (Integer)map.get("initiatorWorker");
                //如果接收人为发起人，消息链接跳转查看页面
                if(!initiatorWorker.equals(acceptWorker)){
                    //跳转审核页面
//                    String url = (String)map.get("transactionAddress")+"&itemId="+(Integer)map.get("itemId");
                    String url = (String)map.get("transactionAddress");
                    if(url!=null){
                        if(url.indexOf("?")==-1){
                            //唤醒流程存在链接（process/queryTask&itemId=19634，导致404）
                            url = (String)map.get("transactionAddress")+"?itemId="+(Integer)map.get("itemId");
                        }else{
                            url = (String)map.get("transactionAddress")+"&itemId="+(Integer)map.get("itemId");
                        }
                        message.setUrl(url);
                    }
                }else{
                    Integer flag = (Integer)map.get("finishFlag");
                    Integer itemFlag = (Integer)map.get("itemFlag");
                    //区分发给发起人的提醒，通知消息flag=1发给自己的已完成消息(提醒查看链接)，flag=2发给自己的驳回消息（提醒审核链接）flag=null默认审核通过或者指向财务的流程
                    if(flag==null || flag==1){
                        if(itemFlag==0){
                            //跳转查看页面
                            message.setUrl((String)map.get("finishAddress"));
                        }else{
                            //跳转审核页面(发给发起人的代办链接)
                            String url = (String)map.get("transactionAddress");
                            if(url!=null) {
                                if (url.indexOf("?") == -1) {
                                    //唤醒流程存在链接（process/queryTask&itemId=19634，导致404）
                                    url = (String) map.get("transactionAddress") + "?itemId=" + (Integer) map.get("itemId");
                                } else {
                                    url = (String) map.get("transactionAddress") + "&itemId=" + (Integer) map.get("itemId");
                                }
                                message.setUrl(url);
                                message.setItemId((Integer)map.get("itemId"));
                            }
                        }
                    }else if(flag==2){
                        //跳转审核页面
                        String url = (String)map.get("transactionAddress");
                        if(url!=null){
                            if(url.indexOf("?")==-1){
                                //唤醒流程存在链接（process/queryTask&itemId=19634，导致404）
                                url = (String)map.get("transactionAddress")+"?itemId="+(Integer)map.get("itemId");
                            }else{
                                url = (String)map.get("transactionAddress")+"&itemId="+(Integer)map.get("itemId");
                            }
                            message.setUrl(url);
                            message.setItemId((Integer)map.get("itemId"));
                        }
                    }
                }
                //消息链接类型
                message.setUrlName((String)map.get(IProcess.PROCESS_NAME));
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
                    //如果接收人为发起人，消息链接跳转查看页面
                    if(!initiatorWorker.equals(acceptWorker)){
                        //跳转审核页面
                        wsMessage.setUrl((String)map.get("transactionAddress"));
                    }else{
                        //跳转查看页面
                        wsMessage.setUrl((String)map.get("finishAddress"));
                    }
                    // 提交信息；
                    WebSocketServer.sendMessage(wsMessage);
                }
            }
        }
    }

    //添加待办默认方法
    @Transactional
    default Integer addWork(Map<String, Object> map){
        Integer itemId = null;
        // 创建代办事项；
        Object object = EntityUtil.getNewObject(map, Items.class);
        if (object != null) {
            Items items = (Items) object;
            if (!StringUtils.isEmpty(items.getItemName())) {
                IItemsService itemsService = SpringUtils.getBean("itemsService");
                Date date = new Date();
                items.setStartTime(date);
                // FIXME: 2018/12/8 0008 默认期限为3天，如需修改请在此处编辑；
                items.setEndTime(DateUtils.getAfterDay(date, 3));
                itemsService.addItemsReturnId(items);
                itemId = items.getId();
            }
        }
        return itemId;
    }

    //修改待办默认方法
    @Transactional
    default void updateWork(int itemId){
        Items items = new Items();
        items.setId(itemId);
        items.setTransactionState(Const.ITEM_Y);
        IItemsService itemsService = SpringUtils.getBean("itemsService");
        itemsService.finishItems(items);
        IMessageService messageService = SpringUtils.getBean("messageService");
        //查询待办相关消息集合
        List<Integer> ids = messageService.queryMessageIdsByItemId(itemId);
        //批量已读消息
        messageService.updateMessage(ids);
    }

    //获取基础参数
    default Map<String, Object> getTaskParam(DelegateTask delegateTask){
        // 用于保存创建代办事项和消息数据的集合；
        Map<String, Object> map = new HashMap<>();
        // 更新消息内容，根据状态来确定消息内容；
        String processName = delegateTask.getVariable(IProcess.PROCESS_NAME, String.class);
        map.put(IProcess.PROCESS_NAME, processName); // WebSocket发送消息需要此字段；

        String messageUserName = delegateTask.getVariable("messageUserName", String.class);
        map.put("messageUserName", messageUserName); //消息发起人

        String messageTitle = delegateTask.getVariable("messageTitle", String.class);
        map.put("messageTitle", messageTitle); //消息标题

        // 获取需要审核的数据ID，类型不确定，统一使用字符串获取；
        String dataId = delegateTask.getVariable("dataId", String.class);
        dataId = dataId == null ? "-1" : dataId;
        map.put("dataId", dataId);

        // 发送信息需要的信息；
        User loginUser = AppUtil.getUser();
        String userImage = loginUser.getImage();
        String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;  // 获取消息显示的图片；
        map.put("pictureAddress", pictureAddress);

        String dataUrl = delegateTask.getVariable(IProcess.PROCESS_FINISH_URL, String.class);
        map.put("dataUrl", dataUrl);

        // 获取数据需要更新的状态；
        Integer state = delegateTask.getVariable("state", Integer.class);
        state = state == null ? IConst.STATE_REJECT : state;  // 为空则驳回；
        map.put("state", state);
        return map;
    }

    //上个用户任务驳回，当前用户任务创建时公共处理方法
    default void commonRejectHandle(DelegateTask delegateTask, String pictureAddress, String processName, String dataUrl, Map<String, Object> map){
        // 发送信息需要的信息；
        FlowableMapper flowableMapper = SpringUtils.getBean(FlowableMapper.class);
        Map obj = flowableMapper.findTaskNameAndPoint(delegateTask.getProcessInstanceId());
        //消息标题
        String title = (String)map.get("messageTitle");
        //消息子类名称
        String messageTypeName = (String)map.get("messageTypeName");
        //消息发起人
        String createName = (String)map.get("messageUserName");
        String userName = AppUtil.getUser().getName();
        //待办工作类型不能添加发起人名称（特殊处理）
        String workType = processName;
        //关于员工的流程没有标题
        if(!StringUtils.isEmpty(title)){
            processName = processName+"["+title+"]";
        }else{
            processName = createName!=null?processName+"["+createName+"]":processName;
        }
//        processName = title!=null?processName+"["+title+"]":processName+"["+createName+"]";
        //存在打开两个页面点击右上角审核，工作台待办未刷新情况
        if(obj!=null){
            if(!userName.equals(createName)){
                //当前审核节点
                String point = (String)obj.get("name");
                map.put("content", String.format("[%s]您提交的%s在%s节点被驳回。",messageTypeName,processName, point));
            }else{
                //当前审核节点
                String point = (String)obj.get("name");
                //自己撤回流程
                map.put("content", String.format("[%s]您提交的%s在%s节点撤回。",messageTypeName,processName,point));
            }
        }else{
            map.put("content", String.format("[%s]您提交的%s已被驳回。",messageTypeName,processName));
        }
        map.put("pic", pictureAddress);
        // 增加待办事项需要的信息，跳转的地址需要变更为数据编辑的页面；
        map.put("itemName", String.format("%s - 已被驳回", delegateTask.getVariable("dataName")));
        map.put("itemContent", String.format("您的%s已被驳回，请重新提交", processName));
        // 增加待办事项需要的信息；
        map.put("workType", workType);
        // 获取驳回处理的链接页面；
        String editUrl = delegateTask.getVariable(IProcess.PROCESS_EDIT_URL, String.class);
        if (StringUtils.isEmpty(editUrl)) {
            editUrl = dataUrl;
        }
        //驳回链接标志
        map.put("finishFlag",2);
        // 更新代办事项的跳转地址，跳转到审核关联数据的列表页面；
        map.put("transactionAddress", editUrl);
        // 处理完成的待办事项跳转到关联数据的列表页面；
        map.put("finishAddress", dataUrl);
        // 代办事项的紧急程度；
        map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
    }

    //上个用户任务通过，当前用户任务默认公共处理方法
    default void commonDefaultHandle(DelegateTask delegateTask, String pictureAddress, String processName, String dataUrl, Map<String, Object> map){
        // ===============================发起人===============================
        // 发送信息需要的信息，用于在审核操作后通知流程发起人；
        map.put("newPic", pictureAddress);
        //根据状态判断是提交还是审核通过，从而更改消息内容
        Integer state = (Integer) map.get("state");
        //消息标题
        String title = (String)map.get("messageTitle");
        //消息子类类型名称
        String messageTypeName = (String)map.get("messageTypeName");
        //消息发起人
        String createName = (String)map.get("messageUserName");
        String evenName = delegateTask.getEventName();
        //待办工作类型不能添加发起人名称（特殊处理）
        String workType = processName;
        //不加title的流程名称
        String oldProcessName = processName;
        if(!StringUtils.isEmpty(title)){
            processName = processName+"["+title+"]";
        }else{
            processName = createName!=null?processName+"["+createName+"]":processName;
        }
        String messagePattern = delegateTask.getVariable("messagePattern",String.class);
        String messageSign = delegateTask.getVariable("messageSign",String.class);
//        processName = title!=null?processName+"["+title+"]":processName+"["+createName+"]";
        //根据审核状态区分提交，通过操作
        if(StringUtils.isEmpty(messagePattern)){
            if(state==4){
                map.put("newContent", String.format("[%s]恭喜你，您提交的%s已成功提交,下一审核节点：%s,审核人：%s。",messageTypeName,processName,delegateTask.getName(),delegateTask.getOwner()));
            }else{
                if (evenName.equals("create")) {
                    map.put("newContent", String.format("[%s]您提交的%s已审核通过,下一审核节点：%s,审核人：%s。", messageTypeName, processName, delegateTask.getName(), delegateTask.getOwner()));
                }
            }
        }else{
            map.put("newContent", String.format(messagePattern, messageTypeName, processName, delegateTask.getName(), delegateTask.getOwner()));
            if("1".equals(messageSign)){
                delegateTask.setVariable("messagePattern", "[%s]您提交的%s已审核通过,下一审核节点：%s,审核人：%s。");
                delegateTask.setVariable("messageSign","2");
            }
        }

        // 处理中的待办事项跳转到流程审核页面；
        String processUrl = delegateTask.getVariable(IProcess.PROCESS_APPROVE_URL, String.class);
        // 通知审核人；
        map.put("pic", pictureAddress);
        if (evenName.equals("create")) {
            map.put("content", String.format("[%s]您有新的%s待审核。", messageTypeName, (title!=null?oldProcessName + ":" + createName + "发起的[" + title + "]":processName+"流程")));
            // ===============================发起人结束===============================

            // ===============================审核人===============================
            // 增加待办事项需要的信息；
            map.put("itemName", String.format("%s - 等待审核", delegateTask.getVariable("dataName")));
            map.put("itemContent", String.format("[%s]您有新的%s需要审核", messageTypeName, processName));
            // 增加待办事项需要的信息；
            map.put("workType", workType);
            if (StringUtils.isEmpty(processUrl)) {
                processUrl = "/process/queryTask";
            } else {
                // 如果有动态URL，进行更新；
                String dynamicUrl = delegateTask.getVariable("dynamicUrl", String.class);
                if (!StringUtils.isEmpty(dynamicUrl)) {
                    processUrl = dynamicUrl;
                    delegateTask.setVariable(IProcess.PROCESS_APPROVE_URL, dynamicUrl);
                }
            }
            map.put("transactionAddress", processUrl);
            // 处理完成的待办事项跳转到关联数据的列表页面；
            map.put("finishAddress", dataUrl);
            // 代办事项的紧急程度；
            map.put("urgencyLevel", delegateTask.getVariable("urgencyLevel", Integer.class));
        }
        // ===============================审核人结束===============================
    }

    //任务完成公共处理方法
    default void commonFinishHandle(String pictureAddress, String processName, String dataUrl, Map<String, Object> map){
        // 发送信息需要的信息，用于在审核操作后通知流程发起人；
        //消息标题
        String title = (String)map.get("messageTitle");
        //消息子类型名称
        String messageTypeName = (String)map.get("messageTypeName");
        //消息发起人
        String createName = (String)map.get("messageUserName");
        if(!StringUtils.isEmpty(title)){
            processName = processName+"["+title+"]";
        }else{
            processName = createName!=null?processName+"["+createName+"]":processName;
        }
//        processName = title!=null?processName+"["+title+"]":processName+"["+createName+"]";
        map.put("newPic", pictureAddress);
        map.put("parentType",2);//提醒
        //发给发起人完成提醒消息标志
        map.put("finishFlag",1);
        map.put("newContent", String.format("[%s]您提交的%s审核已完成。",messageTypeName,processName));
        // 处理中的待办事项跳转到流程审核页面；
        map.put("transactionAddress", dataUrl);
    }

    //统一消息处理逻辑，返回新增的待办ID
    default Integer commonSendMessage(DelegateTask delegateTask, Map<String, Object> map){
        // 获取流程接收人的信息；
        Integer acceptDept = delegateTask.getVariable("acceptDept", Integer.class);
        Integer acceptWorker = delegateTask.getVariable("acceptWorker", Integer.class);
        if (acceptDept != null && acceptWorker != null) {
            map.put("acceptDept", acceptDept);
            map.put("acceptWorker", acceptWorker);
        }

        // 获取流程发起人信息；
        Integer initiatorDept = delegateTask.getVariable("initiatorDept", Integer.class);
        Integer initiatorWorker = delegateTask.getVariable("initiatorWorker", Integer.class);
        map.put("initiatorDept", initiatorDept);
        map.put("initiatorWorker", initiatorWorker);

        // 获取上个待办事项的ID，在添加待办事项的时候更新上个待办事项的状态；
        Integer oldItemId = delegateTask.getVariable("itemId", Integer.class);
        if (oldItemId != null) {
            updateWork(oldItemId);
        }
        // 增加待办事项；
        Integer itemId = addWork(map);
        map.put("itemId",itemId);
        // 发送消息(代办标志：链接和待办一致)
        map.put("itemFlag",1);
        sendMessage(map);
        // 更新到数据库，用于在流程流转时更新状态；
        if (itemId != null) {
            delegateTask.setVariable("itemId", itemId);
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
            Integer flag = (Integer)map.get("finishFlag");
            //区分发给发起人的提醒，通知消息flag=1发给自己的已完成消息(提醒查看链接)，flag=2发给自己的驳回消息（提醒审核链接）flag=null默认审核通过或者指向财务的流程
            if(flag==null || (flag!=1 && flag!=2)){
                map.put("parentType", 3);//通知
            }
            //待办标志：链接为查看链接（先满足finishFlag标志）
            map.put("itemFlag",0);
            map.put("finishAddress",(String)map.get("dataUrl"));
            sendMessage(map);
        }
        return itemId;
    }

    //获取审核人
    default String[] getApproveUserId(DelegateTask delegateTask, String type, String code, String company, boolean companyFlag){
        // 获取数据服务接口；
        IUserService userService = SpringUtils.getBean("userService");
        User user = null;
        String nextUser = null;
        String userName = null;
        String companyCode = delegateTask.getVariable("companyCode", String.class);// 获取需要更新的状态；
        if(!StringUtils.isEmpty(companyCode)){
            if(companyFlag){ //优先使用传递过来的company，如果为空则使用companyCode
                companyCode = StringUtils.isEmpty(company) ? companyCode : company;
            }
            companyCode = StringUtils.isEmpty(companyCode) ? IConst.COMPANY_CODE_XH : companyCode;
            List<User> userList = userService.listByTypeAndCode(type, code, companyCode);
            if(CollectionUtils.isEmpty(userList)){
                throw new QinFeiException(1001, "没有下一个审批人，请联系管理员！");
            }
            user = userList.get(0); //默认取第一个
        }
        // 获取用户信息；
        if (user != null) {
            delegateTask.setVariable("acceptDept", user.getDeptId());
            delegateTask.setVariable("acceptWorker", user.getId());
            nextUser = user.getId().toString();
            userName = user.getName();
        }
        return new String[]{nextUser, userName};
    }

    //设置下一个审核人
    void setApproveUser(DelegateTask delegateTask, int state);

    //数据封装
    void handleApproveData(DelegateTask delegateTask);

    //数据更新
    @Transactional
    void updateProcessData(DelegateTask delegateTask);

}
