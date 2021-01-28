package com.qinfei.qferp.service.impl.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeOutWork;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.administrative.AdministrativeMapper;
import com.qinfei.qferp.mapper.administrative.AdministrativeOutWorkMapper;
import com.qinfei.qferp.mapper.administrative.AdministrativeOvertimeworkMapper;
import com.qinfei.qferp.service.administrative.IAdministrativeOutWorkService;
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
public class AdministrativeOutWorkService implements IAdministrativeOutWorkService {
    @Autowired
    private AdministrativeOutWorkMapper outWorkMapper;
    @Autowired
    private Config config;
    @Autowired
    private IProcessService processService ;
    @Autowired
    private AdministrativeMapper administrativeMapper;
    @Autowired
    private AdministrativeVacationTimeService vacationTime;
    @Autowired
    private AdministrativeOvertimeworkMapper workTime;
    @Autowired
    private ItemsService itemsService ;

    //获取年/月/日目录结构
    private String getCurrentDateDir(){
        return DateUtils.format(new Date(), "/yyyy-MM");
    }

    /**
     * 添加外出信息
     * @param outWork
     * @param multipartFiles
     * @param pics
     * @return
     */
    @Transactional
    @Override
    public ResponseData addOutWork(AdministrativeOutWork outWork, MultipartFile[] multipartFiles, MultipartFile[] pics) {
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
                        String fileName = UUIDUtil.get32UUID()+ext;
                        String childPath = dateDir + "/administrative/OutWork/";
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
                //将图片
                for (MultipartFile pic:pics) {
                    dateDir = StringUtils.isEmpty(dateDir) ? getCurrentDateDir() : dateDir;
                    if(pic.getSize()>0){
                        String temp = pic.getOriginalFilename();
                        String ext = null;
                        if(temp.indexOf(".")>-1){
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID()+ext;
                        String childPath = dateDir + "/administrative/OutWork/";
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
            outWork.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
            outWork.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            outWork.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
            outWork.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            outWork.setEmpId(emp.getId());
            outWork.setEmpName(emp.getName());
            //添加创建人和更新人信息
            outWork.setCreateInfo();
            ResponseData data = ResponseData.ok();
            //将数据插入到行政管理主表中
            Administrative administrative = new Administrative();
            administrative.setTitle(outWork.getTitle());//标题
            administrative.setAdministrativeType(3);
            administrative.setAdministrativeName("外出");
            administrative.setAdministrativeTime(outWork.getTime());//时长
            administrative.setBeginTime(outWork.getBeginTime());
            administrative.setEmpId(outWork.getEmpId());
            administrative.setEmpName(outWork.getEmpName());
            administrative.setEndtime(outWork.getEndTime());
            administrative.setCreateInfo();
            administrative.setDeptId(emp.getDeptId());
            administrative.setDeptName(emp.getDeptName());
            administrative.setCompanyCode(emp.getCompanyCode());
            administrativeMapper.insertSelective(administrative);
            outWork.setAdministrativeId(administrative.getId());
            outWorkMapper.insertSelective(outWork);//将详细信息保存到外出表中
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", outWork) ;
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage());
        }
    }


    /**
     * 更新外出信息
     * @param OutWork
     * @return
     */
    @Override
    public int updateOutWork(AdministrativeOutWork OutWork) {
        return outWorkMapper.updateByPrimaryKeySelective(OutWork);
    }

    /**
     * 伪删除
     * @param outWorkId
     * @return
     */
    @Override
    public int deleteOutWork(int outWorkId) {
        return outWorkMapper.deleteById(outWorkId);
    }

    /**
     * 通过Id获取外出信息
     * @param outWorkId
     * @return
     */
    @Override
    public AdministrativeOutWork getOutWorkById(Integer outWorkId) {
        return outWorkMapper.selectByPrimaryKey(outWorkId);
    }


    /**
     * 开启外出审批
     * @param outWork
     * @param multipartFiles
     * @return
     */
    @Transactional
    @Override
    public ResponseData edit(AdministrativeOutWork outWork, MultipartFile[] multipartFiles, MultipartFile[] pics) {
        //保存外出信息
        ResponseData data = addOutWork(outWork,multipartFiles,pics);
        //根据id获取数据
        Administrative administrative = administrativeMapper.selectByPrimaryKey(outWork.getAdministrativeId());
        //开启审批流
        processService.addOutWorkProcess(administrative,outWork, 3);
        return data;
    }

    /**
     * 开启外出（保存后）审批
     * @param outWork
     * @param multipartFiles
     * @param pics
     * @return
     */
    @Override
    public ResponseData editUpdateOutWork(AdministrativeOutWork outWork, MultipartFile[] multipartFiles, MultipartFile[] pics) {
        //保存外出信息
        ResponseData data = updateOutWorkByAdmId(outWork,multipartFiles,pics);
        //根据id获取数据
        Administrative administrative = administrativeMapper.selectByPrimaryKey(outWork.getAdministrativeId());
        //开启审批流
        processService.addOutWorkProcess(administrative,outWork, 3);

        return data;
    }

    /**
     * 通过行政流程id获取外出信息
     * @param outWorkId
     * @return
     */
    @Override
    public ResponseData getOutWorkByAdministrativeId(Integer outWorkId) {
        ResponseData data = ResponseData.ok();
        AdministrativeOutWork outWork =outWorkMapper.selectByAdministrativeId(outWorkId);
        //获取taskId
        Administrative administrative = administrativeMapper.selectByPrimaryKey(outWorkId);
        outWork.setTaskId(administrative.getTaskId()==null?"":administrative.getTaskId());
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", outWork) ;
        return data;
    }

    /**
     * 更新外出表和流程表两张表中的数据
     * @param outWork
     * @param files
     * @return
     */
    @Override
    @Transactional
    public ResponseData updateOutWorkByAdmId(AdministrativeOutWork outWork, MultipartFile[] files, MultipartFile[] pics) {
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
                        String childPath = dateDir + "/administrative/OutWork/";
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
                        String childPath = dateDir + "/administrative/OutWork/";
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
            outWork.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
            outWork.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            outWork.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
            outWork.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            outWork.setEmpId(emp.getId());
            outWork.setEmpName(emp.getName());
            //添加创建人和更新人信息
            outWork.setCreateInfo();
            ResponseData data = ResponseData.ok();
            //更新行政管理主表中数据
            Administrative administrative = new Administrative();
            administrative.setId(outWork.getAdministrativeId());
            administrative.setTitle(outWork.getTitle());//标题
            administrative.setAdministrativeType(3);
            administrative.setAdministrativeName("外出");
            administrative.setAdministrativeTime(outWork.getTime());//时长
            administrative.setBeginTime(outWork.getBeginTime());
            administrative.setEmpId(outWork.getEmpId());
            administrative.setEmpName(outWork.getEmpName());
            administrative.setEndtime(outWork.getEndTime());
            administrative.setDeptId(emp.getDeptId());
            administrative.setDeptName(emp.getDeptName());
            administrative.setCompanyCode(emp.getCompanyCode());
            administrativeMapper.updateByPrimaryKeySelective(administrative);
            outWork.setAdministrativeId(administrative.getId());
            outWorkMapper.updateByPrimaryKeySelective(outWork);//将详细信息保存到外出表中
            data.putDataValue("message","操作成功");
            data.putDataValue("entity", outWork) ;
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
    public ResponseData deleteOutWorkByAdmId(Integer admId) {
        ResponseData data = ResponseData.ok();
        //先删除流程主表中的数据
        administrativeMapper.updateStateByPrimaryKey(admId);
        //再删除外出表中的数据
        outWorkMapper.deleteByAdmId(admId);
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
