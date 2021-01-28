package com.qinfei.qferp.service.impl.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeOverTimeWork;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.administrative.AdministrativeMapper;
import com.qinfei.qferp.mapper.administrative.AdministrativeOvertimeworkMapper;
import com.qinfei.qferp.service.administrative.IAdministrativeOverTimeWorkService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.utils.AppUtil;
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
public class AdministrativeOvertimeworkService implements IAdministrativeOverTimeWorkService {
    @Autowired
    private AdministrativeOvertimeworkMapper timeworkMapper;
    @Autowired
    private Config config;
    @Autowired
    private IProcessService processService ;
    @Autowired
    private AdministrativeMapper administrativeMapper;
    @Autowired
    private AdministrativeVacationTimeService vacationTime;
    @Autowired
    private ItemsService itemsService ;

    //获取年/月/日目录结构
    private String getCurrentDateDir(){
        return DateUtils.format(new Date(), "/yyyy-MM");
    }

    /**
     * 添加加班信息
     */
    @Override
    public ResponseData addTimework(AdministrativeOverTimeWork timework, MultipartFile[] files, MultipartFile[] pics) {
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
                        String childPath = dateDir + "/administrative/workTime/";
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
                        String childPath = dateDir + "/administrative/workTime/";
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
            timework.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
            timework.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            timework.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
            timework.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            timework.setEmpId(emp.getId());
            timework.setEmpName(emp.getName());
            //添加创建人和更新人信息
            timework.setCreateInfo();
            ResponseData data = ResponseData.ok();
            //将数据插入到行政管理主表中
            Administrative administrative = new Administrative();
            administrative.setTitle(timework.getTitle());//标题
            administrative.setAdministrativeType(2);
            administrative.setAdministrativeName("加班");
            administrative.setAdministrativeTime(timework.getWorkTime());//时长
            administrative.setBeginTime(timework.getBeginTime());
            administrative.setEmpId(timework.getEmpId());
            administrative.setEmpName(timework.getEmpName());
            administrative.setEndtime(timework.getEndTime());
            administrative.setDeptId(emp.getDeptId());
            administrative.setDeptName(emp.getDeptName());
            administrative.setCompanyCode(emp.getCompanyCode());
            administrative.setCreateInfo();
            administrativeMapper.insertSelective(administrative);
            timework.setAdministrativeId(administrative.getId());
            timeworkMapper.insertSelective(timework);//将详细信息保存到加班表中
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", timework) ;
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage());
        }
    }


    /**
     * 更新加班信息
     * @param timework
     * @return
     */
    @Override
    public int updateTimework(AdministrativeOverTimeWork timework) {
        return timeworkMapper.updateByPrimaryKeySelective(timework);
    }

    /**
     * 伪删除
     * @param timeworkId
     * @return
     */
    @Override
    public int deleteTimework(int timeworkId) {
        return timeworkMapper.deleteById(timeworkId);
    }

    /**
     * 通过Id获取加班信息
     * @param timeworkId
     * @return
     */
    @Override
    public AdministrativeOverTimeWork getTimeworkById(Integer timeworkId) {
        return timeworkMapper.selectByPrimaryKey(timeworkId);
    }


    /**
     * 开启加班审批流
     * @param timework
     * @param
     * @return
     */
    @Transactional
    @Override
    public ResponseData edit(AdministrativeOverTimeWork timework, MultipartFile[] files, MultipartFile[] pics) {
        //保存加班信息
        ResponseData data = addTimework(timework,files,pics);
        //根据id获取数据T
        Administrative administrative = administrativeMapper.selectByPrimaryKey(timework.getAdministrativeId());
        //开启审核流
        processService.addOverTimeWorkProcess(administrative, timework,3);
        return data;
    }

    /**
     * 开启加班审批流（保存后）
     * @param timework
     * @param multipartFiles
     * @param pics
     * @return
     */
    @Transactional
    @Override
    public ResponseData editUpdateTimeWork(AdministrativeOverTimeWork timework, MultipartFile[] multipartFiles, MultipartFile[] pics) {
        //保存加班信息
        ResponseData data = updateTimeworkByAdmId(timework,multipartFiles,pics);
        //根据id获取数据
        Administrative administrative = administrativeMapper.selectByPrimaryKey(timework.getAdministrativeId());
        //开启审批流
        processService.addOverTimeWorkProcess(administrative, timework,3);
        return data;
    }

    /**
     * 通过行政流程id获取加班信息
     * @param admId
     * @return
     */
    @Override
    public ResponseData getTimeworkByAdministrativeId(Integer admId) {
        ResponseData data = ResponseData.ok();
        AdministrativeOverTimeWork timework =timeworkMapper.selectByAdministrativeId(admId);
        //获取taskId
        Administrative administrative = administrativeMapper.selectByPrimaryKey(admId);
        timework.setTaskId(administrative.getTaskId()==null?"":administrative.getTaskId());
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", timework) ;
        return data;
    }

    /**
     * 更新两张表中的数据
     * @param
     * @param
     * @return
     */
    @Override
    @Transactional
    public ResponseData updateTimeworkByAdmId(AdministrativeOverTimeWork work, MultipartFile[] files, MultipartFile[] pics) {
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
                        String childPath = dateDir + "/administrative/workTime/";
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
                        String childPath = dateDir + "/administrative/workTime/";
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
            work.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
            work.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            work.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
            work.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            work.setEmpId(emp.getId());
            work.setEmpName(emp.getName());
            //添加创建人和更新人信息
            work.setUpdateInfo();
            ResponseData data = ResponseData.ok();
            //更新行政管理主表中数据
            Administrative administrative = new Administrative();
            administrative.setId(work.getAdministrativeId());
            administrative.setTitle(work.getTitle());//标题
            administrative.setAdministrativeType(2);
            administrative.setAdministrativeName("加班");
            administrative.setAdministrativeTime(work.getWorkTime());//时长
            administrative.setBeginTime(work.getBeginTime());
            administrative.setEmpId(work.getEmpId());
            administrative.setEmpName(work.getEmpName());
            administrative.setEndtime(work.getEndTime());
            administrative.setDeptId(emp.getDeptId());
            administrative.setDeptName(emp.getDeptName());
            administrative.setCompanyCode(emp.getCompanyCode());
            administrative.setUpdateInfo();
            administrativeMapper.updateByPrimaryKeySelective(administrative);
            timeworkMapper.updateByPrimaryKeySelective(work);//将详细信息保存到加班表中
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", work) ;
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage());
        }
    }

    /**
     * 根据admId删除数据
     * @param admId
     * @return
     */
    @Override
    @Transactional
    public ResponseData deleteTimeworkByAdmId(Integer admId) {
        ResponseData data = ResponseData.ok();
        //先删除流程主表中的数据
        administrativeMapper.updateStateByPrimaryKey(admId);
        //再删除加班表中的数据
        timeworkMapper.deleteByAdmId(admId);
        //处理待办
        Administrative adm = administrativeMapper.selectByPrimaryKey(admId);
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
}
