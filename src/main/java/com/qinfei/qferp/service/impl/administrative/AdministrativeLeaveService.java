package com.qinfei.qferp.service.impl.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.administrative.*;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.administrative.*;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.service.sys.IWorkDateService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.ILeave;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AdministrativeLeaveService implements IAdministrativeLeaveService {
    @Autowired
    private AdministrativeLeaveMapper leaveMapper;
    @Autowired
    private Config config;
    @Autowired
    private IProcessService processService ;
    @Autowired
    private AdministrativeMapper administrativeMapper;
    @Autowired
    private AdministrativeVacationTimeService vacationTime;
    @Autowired
    private AdministrativeAnnualLeaveMapper annualLeaveMap;
    @Autowired
    private AdministrativeOvertimeworkMapper workTime;
    @Autowired
    private ItemsService itemsService ;
    @Autowired
    private UserBusinessPlanMapper userBusinessPlanMapper;
    @Autowired
    private IWorkDateService workDateService;

    //获取年/月/日目录结构
    private String getCurrentDateDir(){
        return DateUtils.format(new Date(), "/yyyy-MM");
    }

    /**
     *添加请假信息
     * @param leave
     * @param multipartFiles
     * @param pics
     * @return
     */
    @Transactional
    @Override
    public ResponseData addLeave(AdministrativeLeave leave, MultipartFile[] multipartFiles, MultipartFile[] pics) {
        //获取员工信息
        User emp = AppUtil.getUser();
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> picNames = new ArrayList<>();
        List<String> picPaths = new ArrayList<>();
        String dateDir = null;
        try{
            if(multipartFiles != null && multipartFiles.length > 0){
                dateDir = StringUtils.isEmpty(dateDir) ? getCurrentDateDir() : dateDir;
                //将文件名和路径拼装成字符串
                for (MultipartFile multipartFile:multipartFiles) {
                    if(multipartFile.getSize()>0){
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if(temp.indexOf(".")>-1){
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = dateDir + "/administrative/leave/";
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
            if(pics != null && pics.length > 0){
                dateDir = StringUtils.isEmpty(dateDir) ? getCurrentDateDir() : dateDir;
                //将图片
                for (MultipartFile pic:pics) {
                    if(pic.getSize()>0){
                        String temp = pic.getOriginalFilename();
                        String ext = null;
                        if(temp.contains(".")){
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID()+ext;
                        String childPath = dateDir + "/administrative/leave/";
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
            leave.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
            leave.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            leave.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
            leave.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            leave.setEmpId(emp.getId());
            leave.setEmpName(emp.getName());
            //添加创建人和更新人信息
            leave.setCreateInfo();
            ResponseData data = ResponseData.ok();
            //将数据插入到行政管理主表中
            Administrative administrative = new Administrative();
            administrative.setTitle(leave.getTitle());//标题
            administrative.setAdministrativeType(1);
            administrative.setAdministrativeName("请假");
            administrative.setAdministrativeTime(leave.getLeaveTime());//时长
            administrative.setBeginTime(leave.getBeginTime());
            administrative.setEmpId(leave.getEmpId());
            administrative.setEmpName(leave.getEmpName());
            administrative.setEndtime(leave.getEndTime());
            administrative.setCreateInfo();
            administrative.setDeptId(emp.getDeptId());
            administrative.setDeptName(emp.getDeptName());
            administrative.setCompanyCode(emp.getCompanyCode());
            administrativeMapper.insertSelective(administrative);
            leave.setAdministrativeId(administrative.getId());
            leaveMapper.insertSelective(leave);//将详细信息保存到请假表中
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", leave) ;
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage());
        }
    }


    /**
     * 更新请假信息
     * @param leave
     * @return
     */
    @Override
    public int updateLeave(AdministrativeLeave leave) {
        return leaveMapper.updateByPrimaryKeySelective(leave);
    }

    /**
     * 伪删除
     * @param leaveId
     * @return
     */
    @Override
    public int deleteLeave(int leaveId) {
        return leaveMapper.deleteById(leaveId);
    }

    /**
     * 通过Id获取请假信息
     * @param leaveId
     * @return
     */
    @Override
    public AdministrativeLeave getLeaveById(Integer leaveId) {
        return leaveMapper.selectByPrimaryKey(leaveId);
    }


    /**
     * 开启请假审批
     * @param leave
     * @param multipartFiles
     * @return
     */
    @Transactional
    @Override
    public ResponseData edit(AdministrativeLeave leave, MultipartFile[] multipartFiles, MultipartFile[] pics, Integer nextUser, String nextUserName, Integer nextUserDept) {
        //保存请假信息
        ResponseData data = addLeave(leave,multipartFiles,pics);
        //根据id获取数据
        Administrative administrative = administrativeMapper.selectByPrimaryKey(leave.getAdministrativeId());
        Double leaveTime = leave.getLeaveTime();
        if(leaveTime<=8){
            administrative.setLeaveDay(1);
        }else if(leaveTime>8&&leaveTime<=16){
            administrative.setLeaveDay(2);
        }else{
            administrative.setLeaveDay(3);
        }
        //开启审批流
        processService.addVocationProcess(administrative, 3, nextUser, nextUserName, nextUserDept);
        return data;
    }

    /**
     * 开启请假（保存后）审批
     * @param leave
     * @param multipartFiles
     * @param pics
     * @return
     */
    @Override
    public ResponseData editUpdateLeave(AdministrativeLeave leave, MultipartFile[] multipartFiles, MultipartFile[] pics, Integer nextUser, String nextUserName, Integer nextUserDept) {
        //保存请假信息
        ResponseData data = updateLeaveByAdmId(leave,multipartFiles,pics);
        //根据id获取数据
        Administrative administrative = administrativeMapper.selectByPrimaryKey(leave.getAdministrativeId());
        Double leaveTime = leave.getLeaveTime();
        if(leaveTime<=8){
            administrative.setLeaveDay(1);
        }else if(leaveTime>8&&leaveTime<=16){
            administrative.setLeaveDay(2);
        }else{
            administrative.setLeaveDay(3);
        }
        //开启审批流
        processService.addVocationProcess(administrative, 3, nextUser, nextUserName, nextUserDept);
        return data;
    }

    /**
     * 更新请假申请的审批状态
     * @param leave
     */
    @Override
    public void processLeava(Administrative leave) {
        // 根据流程状态来更新请假的状态
        Integer processState = leave.getState();
        //通过id获取该流程信息
        Administrative adm = administrativeMapper.selectByPrimaryKey(leave.getId());
        StringBuffer userId = new StringBuffer(adm.getApproverUserId()==null?"":adm.getApproverUserId());
        userId.append(","+leave.getApproverUserId());
        leave.setApproverUserId(userId.toString());
        leave.setUpdateInfo();
        switch (processState) {
            // 拒绝的流程状态改为已拒绝；
            case IConst.STATE_REJECT:
                leave.setState(ILeave.LEAVE_REFUSE);
                leave.setApproveState(ILeave.LEAVE_REFUSE);
                leave.setFinishTime(new Date());
                break;
            // 审批完成，更新状态为同意；
            case IConst.STATE_PASS:
                leave.setState(ILeave.LEAVE_AGREE);
                leave.setApproveState(ILeave.LEAVE_AGREE);
                leave.setFinishTime(new Date());
                break;
            case IConst.STATE_FINISH:
                leave.setState(ILeave.LEAVE_AGREE);
                leave.setApproveState(ILeave.LEAVE_AGREE);
                leave.setFinishTime(new Date());
                break;
            default:
                // 审批中的流程状态保持不变；
                leave.setState(ILeave.LEAVE_APPROVE);
                leave.setApproveState(ILeave.LEAVE_APPROVE);
                break;
        }

        //当审批完成并通过
        if(processState == IConst.STATE_FINISH){
            //当行政流程是请假
            if(adm.getAdministrativeType()==1){
                //获取请假详情
                AdministrativeLeave leave1 = leaveMapper.selectByAdministrativeId(leave.getId());
               if(leave1!=null&&leave1.getLeaveType()==1){
                   //当请假是调休的时候，将剩余调休时长减去
                   vacationTime.changVacationTime(leave1.getEmpId(),leave1.getEmpName(),leave1.getLeaveTime(),-1,leave1.getAdministrativeId());
               }
            }else if(adm.getAdministrativeType()==2){
                //当行政流程是加班
                AdministrativeOverTimeWork overTimeWork = workTime.selectByAdministrativeId(leave.getId());
                if(overTimeWork!=null){
                    vacationTime.changVacationTime(overTimeWork.getEmpId(),overTimeWork.getEmpName(),overTimeWork.getWorkTime(),1,overTimeWork.getAdministrativeId());
                }
            }else if(adm.getAdministrativeType()==4){
                userBusinessPlanMapper.updateState(leave.getId());
            }
        }
        administrativeMapper.updateByPrimaryKeySelective(leave);

    }

    /**
     * 通过行政流程id获取请假信息
     * @param leaveId
     * @return
     */
    @Override
    public ResponseData getLeaveByAdministrativeId(Integer leaveId) {
        ResponseData data = ResponseData.ok();
        AdministrativeLeave leave =leaveMapper.selectByAdministrativeId(leaveId);
        //获取taskId
        Administrative administrative = administrativeMapper.selectByPrimaryKey(leaveId);
        leave.setTaskId(administrative.getTaskId()==null?"":administrative.getTaskId());
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", leave) ;
        return data;
    }

    /**
     * 更新请假表和流程表两张表中的数据
     * @param leave
     * @param files
     * @return
     */
    @Override
    @Transactional
    public ResponseData updateLeaveByAdmId(AdministrativeLeave leave, MultipartFile[] files, MultipartFile[] pics) {
        /*boolean sta = getLeaveTime(leave.getLeaveType(),leave.getEmpId());
        if(sta){
            return ResponseData.customerError(1001,"该请假类型已经没有请假时间了！");
        }*/
        //获取员工信息
        User emp = AppUtil.getUser();
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> picNames = new ArrayList<>();
        List<String> picPaths = new ArrayList<>();
        String dateDir = null;
        try{
            if(files != null && files.length > 0){
                dateDir = StringUtils.isEmpty(dateDir) ? getCurrentDateDir() : dateDir;
                //将文件名和路径拼装成字符串
                for (MultipartFile multipartFile:files) {
                    if(multipartFile.getSize()>0){
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if(temp.indexOf(".")>-1){
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID()+ext;
                        String childPath = dateDir + "/administrative/leave/";
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
            if(pics != null && pics.length > 0){
                dateDir = StringUtils.isEmpty(dateDir) ? getCurrentDateDir() : dateDir;
                //将图片
                for (MultipartFile pic:pics) {
                    if(pic.getSize()>0){
                        String temp = pic.getOriginalFilename();
                        String ext = null;
                        if(temp.indexOf(".")>-1){
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID()+ext;
                        String childPath = dateDir + "/administrative/leave/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        pic.transferTo(destFile);
                        fileNames.add(pic.getOriginalFilename());
                        filePaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
            }
            leave.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
            leave.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            leave.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
            leave.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            leave.setEmpId(emp.getId());
            leave.setEmpName(emp.getName());
            //添加创建人和更新人信息
            leave.setCreateInfo();
            ResponseData data = ResponseData.ok();
            //更新行政管理主表中数据
            Administrative administrative = new Administrative();
            administrative.setId(leave.getAdministrativeId());
            administrative.setTitle(leave.getTitle());//标题
            administrative.setAdministrativeType(1);
            administrative.setAdministrativeName("请假");
            administrative.setAdministrativeTime(leave.getLeaveTime());//时长
            administrative.setBeginTime(leave.getBeginTime());
            administrative.setEmpId(leave.getEmpId());
            administrative.setEmpName(leave.getEmpName());
            administrative.setEndtime(leave.getEndTime());
            administrative.setDeptId(emp.getDeptId());
            administrative.setDeptName(emp.getDeptName());
            administrative.setCompanyCode(emp.getCompanyCode());
            administrativeMapper.updateByPrimaryKeySelective(administrative);
            leave.setAdministrativeId(administrative.getId());
            leaveMapper.updateByPrimaryKeySelective(leave);//将详细信息保存到请假表中
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", leave) ;
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage());
        }
    }

    /**
     * 根据流程管理id删除数据
     * @param admId
     * @return
     */
    @Override
    @Transactional
    public ResponseData deleteLeaveByAdmId(Integer admId) {
        Administrative adm = administrativeMapper.selectByPrimaryKey(admId);
        ResponseData data = ResponseData.ok();
        //先删除流程主表中的数据
        administrativeMapper.updateStateByPrimaryKey(admId);
        //再删除请假表中的数据
        leaveMapper.deleteByAdmId(admId);
        //处理待办
        if(adm.getItemId()!=null){
            Items items = new Items();
            items.setId(adm.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", admId) ;
        return data;
    }

    @Override
    public ResponseData validationAddLeave(Date endDate, Integer num, Integer id) {
        try{
            if(endDate == null || num == null || num <= 0){
                return ResponseData.ok();
            }else{
                Date submitDate = new Date();
                if(id != null){
                    Administrative administrative = administrativeMapper.selectByPrimaryKey(id);
                    //如果是撤销状态或者保存，取创建时间为提交时间
                    if(administrative != null && (administrative.getApproveState() == null || administrative.getApproveState() == -1)){
                        submitDate = administrative.getCreateTime();
                    }
                }
                String lastWorkDate = workDateService.getLastNumWorkDate(num,submitDate); //获取当前日期之前的第num个工作日
                if(endDate.compareTo(DateUtils.parse(lastWorkDate, "yyyy-MM-dd"))  >= 0){
                    return ResponseData.ok();
                }else{
                    String [] numArr = {"一","二","三","四","五","六","七","八","九","十",};
                    return ResponseData.customerError(1002,"请假申请不能超过"+(num > 10 ? num : numArr[num-1])+"个工作日补！");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"请假申请时间校验异常！");
        }
    }


    /**
     * 添加或更新请假信息时，判断该类型的假期是否有时间
     * @param typeId
     * @param empId
     * @return
     */
    public boolean getLeaveTime(Integer typeId,Integer empId){
        if(typeId==1){
            AdministrativeVacationTime vacation = vacationTime.getVacationTime(empId);
            if(vacation.getVacationTime()>0){
                return false;
            }else{
                return true;
            }
        }
        AdministrativeAnnualLeave annualLeave = annualLeaveMap.getAnnualLeave(typeId,empId);
        if(annualLeave.getSurplusTime()>0){
            return false;
        }else{
            return true;
        }
    }

}
