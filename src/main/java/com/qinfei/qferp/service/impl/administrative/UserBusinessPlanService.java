package com.qinfei.qferp.service.impl.administrative;


import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.UserBusinessConclusion;
import com.qinfei.qferp.entity.administrative.UserBusinessPlan;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.administrative.AdministrativeMapper;
import com.qinfei.qferp.mapper.administrative.UserBusinessPlanMapper;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.administrative.IUserBusinessPlanService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.service.sys.IRoleService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class UserBusinessPlanService implements IUserBusinessPlanService {
    @Autowired
    private UserBusinessPlanMapper userBusinessPlanMapper;
    @Autowired
    private Config config;
    @Autowired
    private AdministrativeMapper administrativeMapper;
    @Autowired
    private IProcessService processService;
    @Autowired
    private ItemsService itemsService;
    @Autowired
    IRoleService roleService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DeptMapper deptMapper;
    @Autowired
    private IUserService userService;

    @Override
    @Transactional
    public void saveBusiness(UserBusinessPlan userBusinessPlan) {
        //获取出差计划的创建时间
        userBusinessPlan.setCreateTime(new Date());
        try {
            if (userBusinessPlan == null) {
                throw new QinFeiException(1002, "录入数据不存在！");
            }
            User user = AppUtil.getUser();
            userBusinessPlan.setCreateId(user.getId());
            userBusinessPlan.setUpdateId(user.getId());
            if (user == null) {
                throw new QinFeiException(1002, "请先登录！");
            }
            userBusinessPlanMapper.insert(userBusinessPlan);
        } catch (QinFeiException e) {
            throw new QinFeiException(e.getCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002, "新增出差计划出问题啦...");
        }
    }

    /**
     * 出差审批
     *
     * @param userBusinessPlan
     * @param multipartFiles
     * @param pics
     * @return
     */
    @Override
    public ResponseData onBusiness(UserBusinessPlan userBusinessPlan, MultipartFile[] multipartFiles, MultipartFile[] pics) {
        //增加出差计划表
        ResponseData data = addBusiness(userBusinessPlan, multipartFiles, pics);
        Administrative administrative = administrativeMapper.selectByPrimaryKey(userBusinessPlan.getAdministrativeId());
        processService.addOnbusiness(administrative, userBusinessPlan, 3);


        return data;
    }
    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }
    /**
     * 增加出差计划表
     */
    @Override
    public ResponseData addBusiness(UserBusinessPlan userBusinessPlan, MultipartFile[] multipartFiles,MultipartFile[] pics) {
        //创建用
        User user = AppUtil.getUser();
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> picNames = new ArrayList<>();
        List<String> picPaths = new ArrayList<>();
            try {
                //将文件名和路径拼装成字符串
                for (MultipartFile multipartFile : multipartFiles) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath =getStringData()+ "/administrative/onBusiness/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile.transferTo(destFile);
                        fileNames.add(multipartFile.getOriginalFilename());
                        filePaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
                //将图片
                for (MultipartFile pic : pics) {
                    if (pic.getSize() > 0) {
                        String temp = pic.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = getStringData()+"/administrative/onBusiness/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        pic.transferTo(destFile);
                        picNames.add(pic.getOriginalFilename());
                        picPaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
                userBusinessPlan.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
                userBusinessPlan.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                userBusinessPlan.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
                userBusinessPlan.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
                ResponseData data = ResponseData.ok();
                //将数据插入到行政管理主表中
                Administrative administrative = new Administrative();
                administrative.setTitle(userBusinessPlan.getTitle());//标题
                //行政类型4表示出差
                administrative.setAdministrativeType(4);
                administrative.setAdministrativeName("出差");

                administrative.setAdministrativeTime(userBusinessPlan.getNumberDay());//时长(天)
                //开始时间
                administrative.setBeginTime(userBusinessPlan.getTravelStateTime());
                //申请人
                administrative.setEmpId(user.getId());
                //申请人名
                administrative.setEmpName(user.getName());
                //结束时间
                administrative.setEndtime(userBusinessPlan.getTravelEndTime());


                administrative.setCreateInfo();
                //申请部门
                administrative.setDeptId(user.getDeptId());
                //申请部门名
                administrative.setDeptName(user.getDeptName());
                administrative.setCompanyCode(user.getCompanyCode());
                //将数据增加到行政主表
                administrativeMapper.insertSelective(administrative);
                userBusinessPlan.setUpdateId(user.getId());
                userBusinessPlan.setUpdateUser(user.getName());
                userBusinessPlan.setUpdateTime(new Date());
                userBusinessPlan.setCreateTime(new Date());
                userBusinessPlan.setCreateId(user.getId());
                userBusinessPlan.setCreateUser(user.getUserName());
                userBusinessPlan.setDeptId(user.getDeptId());
                userBusinessPlan.setApplyId(user.getId());
                //获取主表流程id，增加到出差详情表内
                userBusinessPlan.setAdministrativeId(administrative.getId());
                //将数据增加到详情表中
                userBusinessPlanMapper.insertBusiness(userBusinessPlan);
                data.putDataValue("message", "操作成功");
                data.putDataValue("entity", userBusinessPlan);
                return data;
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseData.customerError(1001, e.getMessage());
            }
        }
    @Override
    public ResponseData getBussiness(Integer id){

        try {
            ResponseData data = ResponseData.ok();
            //通过id查询计划详情
            UserBusinessPlan userBusinessPlan = userBusinessPlanMapper.getById(id);
            //通过id查询行政主表详情
            Administrative administrative = administrativeMapper.selectByPrimaryKey(id);
            //存主表taskid
            userBusinessPlan.setTaskId(administrative.getTaskId() == null ? "" : administrative.getTaskId());
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", userBusinessPlan);
            return data;
        }
        catch(Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }


    @Override
    public ResponseData updateBusiness(UserBusinessPlan userBusinessPlan, MultipartFile[] files, MultipartFile[] pics){
        User user = AppUtil.getUser();
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> picNames = new ArrayList<>();
        List<String> picPaths = new ArrayList<>();
        try {
            UserBusinessPlan old = userBusinessPlanMapper.getById(userBusinessPlan.getAdministrativeId());
            if (files.length>1){
                //将文件名和路径拼装成字符串
                for (MultipartFile multipartFile : files) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = getStringData()+"/administrative/onBusiness/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile.transferTo(destFile);
                        fileNames.add(multipartFile.getOriginalFilename());
                        filePaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
                userBusinessPlan.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
                userBusinessPlan.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            }else {
                MultipartFile multipartFile = files[0];
                if (multipartFile.getSize() > 0) {//表示上传了新附件
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/administrative/onBusiness/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    fileNames.add(multipartFile.getOriginalFilename());
                    filePaths.add(config.getWebDir() + childPath + fileName);
                    userBusinessPlan.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
                    userBusinessPlan.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                } else {//表示附件没有变化
                    userBusinessPlan.setAttachment(old.getAttachment());
                    userBusinessPlan.setAttachmentLink(old.getAttachmentLink());
                }
            }
            if (files.length>1){
                //将图片
                for (MultipartFile pic : pics) {
                    if (pic.getSize() > 0) {
                        String temp = pic.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath =getStringData()+ "/administrative/onBusiness/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        pic.transferTo(destFile);
                        picNames.add(pic.getOriginalFilename());
                        picPaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
                userBusinessPlan.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
                userBusinessPlan.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));

            }else{
                MultipartFile multipartFile = pics[0];
                if (multipartFile.getSize() > 0) {//表示上传了新附件
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/administrative/onBusiness/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    picNames.add(multipartFile.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                    userBusinessPlan.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
                    userBusinessPlan.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
                } else {//表示附件没有变化
                    userBusinessPlan.setPicture(old.getPicture());
                    userBusinessPlan.setPictureLink(old.getPictureLink());
                }
            }

            ResponseData data = ResponseData.ok();
            //将数据插入到行政管理主表中
            Administrative administrative = new Administrative();
            administrative.setId(userBusinessPlan.getAdministrativeId());
            administrative.setTitle(userBusinessPlan.getTitle());//标题
            administrative.setAdministrativeType(4);
            administrative.setAdministrativeName("出差");
            administrative.setAdministrativeTime(userBusinessPlan.getNumberDay());//时长(天)
            //开始时间
            administrative.setBeginTime(userBusinessPlan.getTravelStateTime());
            //申请人
            administrative.setEmpId(user.getId());
            //申请人名
            administrative.setEmpName(user.getName());
            //结束时间
            administrative.setEndtime(userBusinessPlan.getTravelEndTime());

            administrative.setCreateInfo();
            //申请部门
            administrative.setDeptId(user.getDeptId());
            //申请部门名
            administrative.setDeptName(user.getDeptName());
            administrative.setCompanyCode(user.getCompanyCode());
            administrativeMapper.updateByPrimaryKeySelective(administrative);
            userBusinessPlan.setAdministrativeId(administrative.getId());
            userBusinessPlan.setUpdateId(user.getId());
            userBusinessPlan.setUpdateUser(user.getName());
            userBusinessPlan.setUpdateTime(new Date());
            userBusinessPlan.setCreateTime(new Date());
            userBusinessPlan.setCreateId(user.getId());
            userBusinessPlan.setCreateUser(user.getUserName());
            userBusinessPlan.setDeptId(user.getDeptId());
            userBusinessPlan.setApplyId(user.getId());
            userBusinessPlanMapper.updateBusiness(userBusinessPlan);
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", userBusinessPlan);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @Override
    public ResponseData editUpdateOnBusiness(UserBusinessPlan userBusinessPlan, MultipartFile[] files, MultipartFile[] pics){
        ResponseData data = updateBusiness(userBusinessPlan,files , pics);
        Administrative administrative = administrativeMapper.selectByPrimaryKey(userBusinessPlan.getAdministrativeId());
        processService.addOnbusiness(administrative, userBusinessPlan, 3);
        return data;
    }

    @Override
    public ResponseData deleteBussiness(Integer id){
        ResponseData data = ResponseData.ok();
        UserBusinessPlan userBusinessPlan = userBusinessPlanMapper.getById(id);
        if (userBusinessPlan.getItemId() != null){
            Items items = new Items();
            items.setId(userBusinessPlan.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
        //先删除流程主表中的数据
        administrativeMapper.updateStateByPrimaryKey(id);
        userBusinessPlanMapper.deleteBussiness(id);

        Administrative adm = administrativeMapper.selectByPrimaryKey(id);
        if (adm.getItemId() != null) {
            Items items = new Items();
            items.setId(adm.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
        data.putDataValue("message", "操作成功");
        data.putDataValue("entity", id);
        return data;
    }

    @Override
    public ResponseData viewBusiness(Integer id){
        ResponseData data = ResponseData.ok();
        UserBusinessPlan userBusinessPlan = userBusinessPlanMapper.getById(id);
        UserBusinessConclusion userBusinessConclusion = userBusinessPlanMapper.getConclusion(id);
        if (userBusinessConclusion != null) {
            userBusinessPlan.setConclusion(userBusinessConclusion.getConclusion());
            userBusinessPlan.setItemConclusionId(userBusinessConclusion.getAdministrativeId());
            userBusinessPlan.setAttach(userBusinessConclusion.getAttach());
            userBusinessPlan.setAttachLink(userBusinessConclusion.getAttachLink());
            userBusinessPlan.setPic(userBusinessConclusion.getPic());
            userBusinessPlan.setPicLink(userBusinessConclusion.getPicLink());
            userBusinessPlan.setItemConclusionId(userBusinessConclusion.getItemConclusionId());
        }
        Integer taskState=userBusinessPlanMapper.selectTask(id);
        userBusinessPlan.setTaskState(taskState);
        Administrative administrative = administrativeMapper.selectByPrimaryKey(id);
        administrative.setApproveState(taskState);
        userBusinessPlan.setTaskId(administrative.getTaskId() == null ? "" : administrative.getTaskId());


        data.putDataValue("message", "操作成功");
        data.putDataValue("entity", userBusinessPlan);
        return data;
    }

    @Override
    public ResponseData addConclusion(Map map, MultipartFile[] pics,MultipartFile[] attach){
        ResponseData data = ResponseData.ok();
        User user = AppUtil.getUser();
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> picNames = new ArrayList<>();
        List<String> picPaths = new ArrayList<>();
        try {
            if (attach !=null &&  attach.length>0){
            //将文件名和路径拼装成字符串
            for (MultipartFile multipartFile : attach) {
                if (multipartFile.getSize() > 0) {
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath =getStringData()+ "/administrative/onBusiness/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    fileNames.add(multipartFile.getOriginalFilename());
                    filePaths.add(config.getWebDir() + childPath + fileName);
                }
            }
            }
            if (pics!=null &&  pics.length>0){
            //将图片
            for (MultipartFile pic : pics) {
                if (pic.getSize() > 0) {
                    String temp = pic.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath =getStringData()+ "/administrative/onBusiness/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    pic.transferTo(destFile);
                    picNames.add(pic.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                }
            }
            }
            map.put("attach",fileNames.toString().replaceAll("\\[|\\]", ""));
            map.put("attachLink",filePaths.toString().replaceAll("\\[|\\]", ""));
            map.put("pic",picNames.toString().replaceAll("\\[|\\]", ""));
            map.put("picLink",picPaths.toString().replaceAll("\\[|\\]", ""));
            map.put("updateId", user.getId());
            map.put("updateUser", user.getUserName());
            map.put("updateTime", new Date());
            map.put("createUser", user.getUserName());
            map.put("createId", user.getId());
            map.put("updateTime", new Date());
            userBusinessPlanMapper.addConclusion(map);
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", map);
            return data;
        }catch (IOException e) {
                e.printStackTrace();
                return ResponseData.customerError(1001, e.getMessage());
            }

    }
    @Override
    public ResponseData  selectUser(){
        ResponseData data = ResponseData.ok();
        try{
            Map map = new HashMap();
            User user= AppUtil.getUser();
            map.put("id",user.getId());
            map.put("companyCode",user.getCompanyCode());
            String reviewerUser = "";
            //判断当前人是否是部门负责人，如果是部门负责人，则前台审核无需展示部门审核人，否则还需要展示部门审核人及总监
            if(userMapper.isDeptLeader(user.getId(), user.getDeptId()) < 1){
                Dept dept = deptMapper.getById(user.getDeptId());
                if (dept != null) {
                    //如果申请人所在部门为部门以上的部门，则获取部门负责人，否则获取分管领导
                    if(dept.getLevel() > 3){
                        reviewerUser = dept.getMgrLeaderName();
                    }else {
                        reviewerUser = dept.getMgrName();
                    }
                }
            }
            User zjUser = userService.getZJInfo(user.getCompanyCode(), String.valueOf(user.getId()));
            if(zjUser != null){
                reviewerUser += StringUtils.isEmpty(reviewerUser) ? zjUser.getName() : ", " + zjUser.getName();
            }
            //设置前台展示审核人
            data.putDataValue("reviewerUser",StringUtils.isEmpty(reviewerUser) ? "无" : reviewerUser) ;
            //设置前台展示批准人
            data.putDataValue("approverUser",userMapper.listByTypeAndCode(IConst.ROLE_TYPE_ZJB, IConst.ROLE_CODE_ZJL, user.getCompanyCode())) ;
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
       }
    }

   @Override
    public int deleteConclusion(Integer id){
       return userBusinessPlanMapper.deleteConclusion(id);
    }

    @Override
    public int deleteCon(Integer adminId){
        return userBusinessPlanMapper.deleteCon(adminId);
    }


    @Override
    public UserBusinessConclusion selectFile(Integer id){
        return userBusinessPlanMapper.selectFile(id);
    }
    @Override
    public Items addItem(Integer userId,Integer administrativeId ){
        Map map = new HashMap();
        Items items = new Items();
        items.setItemName("出差信息");
        items.setItemContent("有新的出差信息");
        items.setWorkType("出差信息确认");
        //获取接收抄送人的信息
        User user = userBusinessPlanMapper.getUser(userId);
        User user1= AppUtil.getUser();
        //发起者
        items.setInitiatorWorker(user1.getId());
        //发起者部门
        items.setInitiatorDept(user1.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/administrative/administrative?type=4&flag=6&id=" + administrativeId);
        items.setFinishAddress("/administrative/administrative?type=4&flag=2&id=" + administrativeId);
        items.setAcceptWorker(user.getId());
        items.setAcceptDept(user.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        map.put("itemId",items.getId());
        map.put("administrativeId",administrativeId);
        userBusinessPlanMapper.insertItemId(map);
        return items;
    }

    @Override
    public Items addConlusionItem(Integer userId,Integer administrativeId ){
        Map map = new HashMap();
        Items items = new Items();
        items.setItemName("出差信息");
        items.setItemContent("有新的出差总结信息");
        items.setWorkType("出差总结信息确认");
        //获取接收抄送人的信息
        User user = userBusinessPlanMapper.getUser(userId);
        User user1= AppUtil.getUser();
        //发起者
        items.setInitiatorWorker(user1.getId());
        //发起者部门
        items.setInitiatorDept(user1.getDeptId());
        items.setStartTime(new Date());
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DATE, 3);// 增加的天数3，
        items.setEndTime(ca.getTime());
        items.setTransactionAddress("/administrative/administrative?type=4&flag=7&id=" + administrativeId);
        items.setFinishAddress("/administrative/administrative?type=4&flag=2&id=" + administrativeId);
        items.setAcceptWorker(user.getId());
        items.setAcceptDept(user.getDeptId());
        items.setTransactionState(Const.ITEM_W);
        itemsService.addItems(items);
        map.put("itemId",items.getId());
        map.put("administrativeId",administrativeId);
        userBusinessPlanMapper.insertConclusionItemId(map);
        return items;
    }


    @Override
    public void confirm(Integer itemId){
        Items items = new Items();
        items.setId(itemId);
        items.setTransactionState(Const.ITEM_Y);
        itemsService.finishItems(items);
    }
    @Override
    public void confirm1(Integer itemConclusionId){
        Items items = new Items();
        items.setId(itemConclusionId);
        items.setTransactionState(Const.ITEM_Y);
        itemsService.finishItems(items);
    }
    @Override
    public  long selectState(String taskId){
        try {
            Object state = userBusinessPlanMapper.selectState(taskId);
            if(state == null){
                throw new QinFeiException(1002, "请刷新一下！");
            }else {
                return  Long.parseLong(String.valueOf(state));
            }
        }
        catch (QinFeiException e){
            throw e;
        }
        catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "流程状态转换错误！");
        }
    }

    @Override
    public int getConclusion(Integer admId){
        Integer sum = userBusinessPlanMapper.getConclusion1(admId);
        return  sum;
    }
    @Override
    public int getReimbursement(Integer admId){
        return userBusinessPlanMapper.getReimbursement(admId);
    }


}