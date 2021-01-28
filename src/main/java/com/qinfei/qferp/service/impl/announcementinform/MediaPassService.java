package com.qinfei.qferp.service.impl.announcementinform;
import com.qinfei.core.config.websocket.WebSocketServer;
import com.qinfei.core.entity.WSMessage;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.announcementinform.MediaPass;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.*;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.entity.workbench.Message;
import com.qinfei.qferp.mapper.announcementinform.MediaPassMapper;
import com.qinfei.qferp.mapper.sys.AutoNumberMapper;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.service.announcementinform.IMediaPassService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.service.workbench.IMessageService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;


@Service
public class MediaPassService implements IMediaPassService {
    @Autowired
    private MediaPassMapper mediaPassMapper;
    @Autowired
    private AutoNumberMapper autoNumberMapper;
    @Autowired
    private DeptMapper deptMapper;
    // 消息推送接口；
    @Autowired
    private IMessageService messageService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ItemsService itemsService;

    /**
     * 查询稿件数据，或者表格选中的
     *
     * @param map
     * @param pageable
     * @return
     */
    public PageInfo<MediaPass> selectByPrimaryKey(Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        User emp = AppUtil.getUser();
        List<MediaPass> list;
        Boolean flag = false;
        if (emp.getRoles() !=null && emp.getRoles().size()>0){
            for (Role role : emp.getRoles()){
                if (IConst.ROLE_CODE_BZ.equals(role.getCode()) && IConst.ROLE_TYPE_XZ.equals(role.getType())){
                    flag=true;
                }
            }
        }
        if (flag) {
            map.put("companyCode",emp.getCompanyCode());
            list = mediaPassMapper.selectAll(map);
        }else {
            map.put("userId", emp.getId());
            map.put("deptId", emp.getDeptId());
            list = mediaPassMapper.selectByMap(map);
        }

        return (PageInfo<MediaPass>) new PageInfo(list);

    }

    /*
    * 新增公告
    * */
    @Override
    @Transactional
    public MediaPass add(MediaPass mediaPass, Integer[] deptIds) {
        mediaPass.setId(null);
        User user = AppUtil.getUser();
        user = user == null ? new User() : user;
        // 更新人信息；
        mediaPass.setCreateId(AppUtil.getUser().getId());
        mediaPass.setPublishDeptId(AppUtil.getUser().getDeptId());
        //通知该编号no
        mediaPass.setNo(IConst.INFOEM_no + CodeUtil.getDayStr() + CodeUtil.getFourCode(getCode(), 4));
        Integer mediaPassId = 0;//
        Integer state = mediaPass.getState();
        //m 是否强制
        Integer m = mediaPass.getMandatory();
        mediaPass.setUpdateId(user.getId());
        mediaPass.setReleaseTime(new Date());
        mediaPass.setUpdateName(user.getName());
        mediaPass.setUpdateTime(new Date());
        mediaPass.setCompanyCode(user.getCompanyCode());

        //增加通知
        mediaPassMapper.insert(mediaPass);
        if (state == 4) {//state=0表示 提交  写入公告和强制阅读表
            //m=1 强制阅读  写入强制阅读关系表
            if (m == 1)
                //senditems 增加待办
                if(deptIds!=null){
                    senditems(mediaPass, deptIds);
                }else{
                    throw new QinFeiException(1002,"请选择生效部门");
                }
            //sendMessage 增加消息发送
            sendMessage(mediaPass);
        }
        return mediaPass;

    }

    //增加待办
    private Items addItem(MediaPass entity, User user) {
        Items items = new Items();
        items.setItemName(entity.getTitle());
        items.setItemContent("通知公告["+entity.getTitle()+"]须知");
        items.setWorkType("通知公告确认");
        items.setInitiatorWorker(entity.getCreateId());
        items.setInitiatorDept(entity.getPublishDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/announcementinform/Mediapass?pageTab=mediaPassTab&flag=1&id=" + entity.getId());
        items.setFinishAddress("announcementinform/Mediapass?pageTab=mediaPassTab&flag=2&id=" + entity.getId());
        items.setAcceptWorker(user.getId());
        items.setAcceptDept(user.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        return items;
    }


    //从code表取数据
    private synchronized Integer getCode() {
        Integer max = mediaPassMapper.getMaxNo(IConst.INFOEM_no, CodeUtil.getYear(), CodeUtil.getMonth());
        if (max == null) {
            max = 1;
        } else {
            max = max + 1;
        }
        //更新autoNumber表
        AutoNumber number = new AutoNumber();
        number.setCode(IConst.INFOEM_no);
        number.setYear(CodeUtil.getYear());
        number.setMonth(CodeUtil.getMonth());
        number.setValue(max);
        autoNumberMapper.insert(number);
        return max;
    }

//    @Override
//    @Transactional
//    public List<Dept> insertAccountDept(Integer operationDeptId,Integer deptId) {
//        List<Dept> list = deptMapper.listByParentId(deptId);
////        for (Dept dept : list) {
////            mediaPassMapper.insertAccountDept(operationDeptId, dept.getId());
////        }
//        return list;
//    }

    @Override
    @Transactional
    public List<Dept> delDeptAccountDept(Integer operationDeptId, Integer deptId) {
        List<Dept> list = deptMapper.listByParentId(operationDeptId);
        Map<String, Object> map = new HashMap<>();
        map.put("id", deptId);
        map.put("list", list);


        if (list != null && list.size() > 0) {
            mediaPassMapper.delDeptAccountDept(map);
        }
        return list;
    }


    @Override
    @Transactional
    public MediaPass edit(MediaPass entity,Integer[] deptIds) {
        User user = AppUtil.getUser();
        entity.setUpdateId(user.getId());
        entity.setCompanyCode(user.getCompanyCode());
        if (entity.getState() == 0) {
            mediaPassMapper.update(entity);
        } else {
            if (StringUtils.isEmpty(entity.getMandatory())){
                throw new QinFeiException(1002,"请选择是否强制阅读");
            }
            if (entity.getMandatory() == 1){
                senditems(entity, deptIds);
                //sendMessage 增加消息发送
                sendMessage(entity);
            }
            mediaPassMapper.update(entity);
        }
        return entity;
    }

    @Override
    public MediaPass getById(Integer id) {
        return mediaPassMapper.getById(id);
    }

    @Override
    @Transactional
    public void delById(MediaPass entity) {
        User user = AppUtil.getUser();
        entity.setState(IConst.STATE_DELETE);
        entity.setUpdateId(user.getId());
        mediaPassMapper.update(entity);
        List<Integer> list = mediaPassMapper.itemId(entity.getId());
        if (list != null&& list.size() > 0 ){
            Map map = new HashMap();
            for (Integer itemid : list){
                map.put("itemId",itemid);
                mediaPassMapper.updateItemState(map);
            }

        }

    }

    @Override
    public List<Dept> queryDeptByAccountId(Integer id) {
        return mediaPassMapper.queryDeptByAccountId(id);
    }


    @Override
    public void insertoperationDept(List<Map> file) {
        mediaPassMapper.insertoperationDept(file);

    }

    @Override
    public void editoperationDept(List<Integer> list) {
        mediaPassMapper.editoperationDept(list);

    }

    /**
     * 添加通知公告中间表
     *

     */

    private void senditems(MediaPass mediaPass, Integer[] departIds) {
        List<Map> maps = new ArrayList<>();
        for (Integer deptId : departIds) {
            List<User> users = userService.queryUserByDeptIdONLY(deptId);
            for (User user : users) {
                //每个生效部门下的用户增加Itemid
                Items item = addItem(mediaPass, user);
                Integer userId = user.getId();
                Integer itemId = item.getId();
                Integer state = 0;
                Map<String, Object> map = new HashMap<>();
                //userid 生效部门的用户
                map.put("userId", userId);
                //itemId 待办id
                map.put("itemId", itemId);
                //state 生效部门状态
                map.put("state", state);
                //通知公告Id
                map.put("id", mediaPass.getId());
                //生效部门id
                map.put("operationDeptId", deptId);
                maps.add(map);
            }
        }
        mediaPassMapper.insertoperationDept(maps);
    }

    /**
     * 通知公告消息发送
     * @param mediaPass
     */

    private void sendMessage(MediaPass mediaPass) {

        User user = AppUtil.getUser();
        Integer userId = user.getId();
        List<Dept> deptList = mediaPassMapper.queryDeptByAccountId(mediaPass.getId());
        for (Dept dept : deptList) {
            List<User> users = userService.queryUserByDeptIdONLY(dept.getId());
            for (User obj : users) {
                String subject = "[通知公告]你有需要接收的通知:["+mediaPass.getTitle()+"]";
                //你有需要接收的通知
                String content = "[通知公告]你有需要接收的通知:["+mediaPass.getTitle()+"]";
                // 推送WebSocket消息；
                WSMessage message = new WSMessage();
                message.setReceiveUserId(obj.getId() + "");
                message.setReceiveName(mediaPass.getPublishDeptName());
                message.setSendName(user.getName());
                message.setSendUserId(userId + "");
                message.setSendUserImage(user.getImage());
                message.setContent(content);
                message.setSubject(subject);
                message.setUrl("/announcementinform/Mediapass?pageTab=mediaPassTab&flag=2&id=" + mediaPass.getId());
                WebSocketServer.sendMessage(message);

                // 推送系统的消息；
                Message newMessage = new Message();
                String userImage = user.getImage();
                // 获取消息显示的图片；
                String pictureAddress = userImage == null ? "/img/mrtx_2.png" : userImage;
                newMessage.setPic(pictureAddress);
                newMessage.setContent(content);
                newMessage.setInitiatorDept(user.getDeptId());
                newMessage.setInitiatorWorker(userId);
                newMessage.setAcceptDept(dept.getId());
                newMessage.setAcceptWorker(obj.getId());
                //消息分类
                newMessage.setParentType(3);//通知
                newMessage.setType(14);//公告通知
                newMessage.setUrl("/announcementinform/Mediapass?pageTab=mediaPassTab&flag=2&id=" + mediaPass.getId());
                newMessage.setUrlName("通知公告");
                messageService.addMessage(newMessage);
            }
        }
    }

    /**
     * 通知公告待办变已办
     * @param mediaPass
     */

    @Override
    public void announcementConfirming(MediaPass mediaPass) {

            User user = AppUtil.getUser();
            Integer userId =user.getId();
        Integer userDeptId = user.getDeptId();

           Map<String, Object> map = new HashMap<>();
           map.put("userId",userId);
           map.put("operationDeptId",userDeptId);
           map.put("Id", mediaPass.getId());

           Integer[] itemIds =  mediaPassMapper.finItem(map);
            //获取当前
        for (Integer itemId : itemIds ){
            Items items = new Items();
            items.setId(itemId);
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }


        }

    @Override
    public Map<String, Object> getResourcePermission(HttpServletRequest request) {
        Map<String, Object> permissionMap = new HashMap<>();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                HttpSession session = request.getSession();
                //获取当前用户权限资源
                List<Resource> resources = (List<Resource>) session.getAttribute(IConst.USER_RESOURCE);
                if(CollectionUtils.isNotEmpty(resources)){
                    for(Resource resource : resources){
                        if(!StringUtils.isEmpty(resource.getUrl()) && resource.getUrl().contains("announcementinform/Mediapass")){
                            permissionMap.put("mediaPass", true);//有公告通知权限
                        }
                        if(!StringUtils.isEmpty(resource.getUrl()) && resource.getUrl().contains("news/newsAdminList")){
                            permissionMap.put("newsManage", true);//有新闻资讯权限
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return permissionMap;
    }
}
