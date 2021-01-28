package com.qinfei.qferp.service.impl.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeAnnualLeave;
import com.qinfei.qferp.entity.administrative.AdministrativeOnBusiness;
import com.qinfei.qferp.entity.administrative.AdministrativeVacationTime;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.administrative.AdministrativeAnnualLeaveMapper;
import com.qinfei.qferp.mapper.administrative.AdministrativeMapper;
import com.qinfei.qferp.mapper.administrative.AdministrativeOnBusinessMapper;
import com.qinfei.qferp.mapper.administrative.AdministrativeOvertimeworkMapper;
import com.qinfei.qferp.service.administrative.IAdministrativeOnBusinessService;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.workbench.ItemsService;
import com.qinfei.qferp.utils.AppUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AdministrativeOnBusinessService implements IAdministrativeOnBusinessService {
    @Autowired
    private AdministrativeOnBusinessMapper onBusinessMapper;
    @Autowired
    private Config config;
    @Autowired
    private IProcessService processService;
    @Autowired
    private AdministrativeMapper administrativeMapper;
    @Autowired
    private AdministrativeVacationTimeService vacationTime;
    @Autowired
    private AdministrativeAnnualLeaveMapper annualLeaveMap;
    @Autowired
    private AdministrativeOvertimeworkMapper workTime;
    @Autowired
    private ItemsService itemsService;

    //获取年/月/日目录结构
    private String getCurrentDateDir(){
        return DateUtils.format(new Date(), "/yyyy-MM");
    }

    /**
     * 添加出差信息
     * @param onBusiness
     * @param multipartFiles
     * @param pics
     * @return
     */
    @Transactional
    @Override
    public ResponseData addOnBusiness(AdministrativeOnBusiness onBusiness, MultipartFile[] multipartFiles, MultipartFile[] pics) {
        //获取员工信息
        User emp = AppUtil.getUser();
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> picNames = new ArrayList<>();
        List<String> picPaths = new ArrayList<>();
        String dateDir = null;
        try {
            if(multipartFiles != null && multipartFiles.length > 0){
                dateDir = StringUtils.isEmpty(dateDir) ? getCurrentDateDir() : dateDir;
                //将文件名和路径拼装成字符串
                for (MultipartFile multipartFile : multipartFiles) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = dateDir + "/administrative/onBusiness/";
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
                for (MultipartFile pic : pics) {
                    if (pic.getSize() > 0) {
                        String temp = pic.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = dateDir + "/administrative/onBusiness/";
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
            onBusiness.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
            onBusiness.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            onBusiness.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
            onBusiness.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            /*onBusiness.setEmpId(emp.getId());
            onBusiness.setEmpName(emp.getName());*/
            //添加创建人和更新人信息
            onBusiness.setCreateInfo();
            ResponseData data = ResponseData.ok();
            //将数据插入到行政管理主表中
            Administrative administrative = new Administrative();
            administrative.setTitle(onBusiness.getTitle());//标题
            administrative.setAdministrativeType(4);
            administrative.setAdministrativeName("出差");
            administrative.setAdministrativeTime(onBusiness.getDays());//时长(天)
            administrative.setBeginTime(onBusiness.getBeginTime());
            administrative.setEmpId(onBusiness.getEmpId());
            administrative.setEmpName(onBusiness.getEmpName());
            administrative.setEndtime(onBusiness.getEndTime());
            administrative.setCreateInfo();
            administrative.setDeptId(onBusiness.getEmpDept());
            administrative.setDeptName(onBusiness.getEmpDeptName());
            administrative.setCompanyCode(emp.getCompanyCode());
            administrativeMapper.insertSelective(administrative);
            onBusiness.setAdministrativeId(administrative.getId());
            onBusinessMapper.insertSelective(onBusiness);//将详细信息保存到出差表中
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", onBusiness);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }


    /**
     * 更新出差信息
     *
     * @param onBusiness
     * @return
     */
    @Override
    public int updateOnBusiness(AdministrativeOnBusiness onBusiness) {
        return onBusinessMapper.updateByPrimaryKeySelective(onBusiness);
    }

    /**
     * 伪删除
     *
     * @param onBusinessId
     * @return
     */
    @Override
    public int deleteOnBusiness(int onBusinessId) {
        return onBusinessMapper.deleteById(onBusinessId);
    }

    /**
     * 通过Id获取出差信息
     *
     * @param onBusinessId
     * @return
     */
    @Override
    public AdministrativeOnBusiness getOnBusinessById(Integer onBusinessId) {
        return onBusinessMapper.selectByPrimaryKey(onBusinessId);
    }


    /**
     * 开启出差审批
     *
     * @param onBusiness
     * @param multipartFiles
     * @return
     */
    @Transactional
    @Override
    public ResponseData edit(AdministrativeOnBusiness onBusiness, MultipartFile[] multipartFiles, MultipartFile[] pics) {
        //保存出差信息
        ResponseData data = addOnBusiness(onBusiness, multipartFiles, pics);
        //根据id获取数据
        Administrative administrative = administrativeMapper.selectByPrimaryKey(onBusiness.getAdministrativeId());
        //开启审批流
        processService.addOnbusinessWorkProcess(administrative, onBusiness, 3);
        return data;
    }

    /**
     * 开启出差（保存后）审批
     *
     * @param onBusiness
     * @param multipartFiles
     * @param pics
     * @return
     */
    @Override
    public ResponseData editUpdateOnBusiness(AdministrativeOnBusiness onBusiness, MultipartFile[] multipartFiles, MultipartFile[] pics, MultipartFile[] reports) {
        //保存出差信息
        ResponseData data = updateOnBusinessByAdmId(onBusiness, multipartFiles, pics, reports);
        //根据id获取数据
        Administrative administrative = administrativeMapper.selectByPrimaryKey(onBusiness.getAdministrativeId());
        //开启审批流
        processService.addOnbusinessWorkProcess(administrative, onBusiness, 3);
        return data;
    }

    /**
     * 通过行政流程id获取出差信息
     *
     * @param leaveId
     * @return
     */
    @Override
    public ResponseData getOnBusinessByAdministrativeId(Integer leaveId) {
        ResponseData data = ResponseData.ok();
        AdministrativeOnBusiness onBusiness = onBusinessMapper.selectByAdministrativeId(leaveId);
        //获取taskId
        Administrative administrative = administrativeMapper.selectByPrimaryKey(leaveId);
        onBusiness.setTaskId(administrative.getTaskId() == null ? "" : administrative.getTaskId());
        data.putDataValue("message", "操作成功");
        data.putDataValue("entity", onBusiness);
        return data;
    }

    /**
     * 更新出差表和流程表两张表中的数据
     *
     * @param onBusiness
     * @param files
     * @return
     */
    @Override
    @Transactional
    public ResponseData updateOnBusinessByAdmId(AdministrativeOnBusiness onBusiness, MultipartFile[] files, MultipartFile[] pics, MultipartFile[] reports) {
        //获取员工信息
        User emp = AppUtil.getUser();
        List<String> fileNames = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();
        List<String> picNames = new ArrayList<>();
        List<String> picPaths = new ArrayList<>();
        List<String> reportNames = new ArrayList<>();
        List<String> reportPaths = new ArrayList<>();
        String dateDir = null;
        try {
            if(files != null && files.length > 0){
                dateDir = StringUtils.isEmpty(dateDir) ? getCurrentDateDir() : dateDir;
                //将文件名和路径拼装成字符串
                for (MultipartFile multipartFile : files) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = dateDir + "/administrative/onBusiness/";
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
                for (MultipartFile pic : pics) {
                    if (pic.getSize() > 0) {
                        String temp = pic.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = dateDir + "/administrative/onBusiness/";
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
            if(reports != null && reports.length > 0){
                dateDir = StringUtils.isEmpty(dateDir) ? getCurrentDateDir() : dateDir;
                //将总结报告
                for (MultipartFile report : reports) {
                    if (report.getSize() > 0) {
                        String temp = report.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath = dateDir + "/administrative/onBusiness/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        report.transferTo(destFile);
                        reportNames.add(report.getOriginalFilename());
                        reportPaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
            }

            onBusiness.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
            onBusiness.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
            onBusiness.setPicture(picNames.toString().replaceAll("\\[|\\]", ""));
            onBusiness.setPictureLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            onBusiness.setReport(reportNames.toString().replaceAll("\\[|\\]", ""));
            onBusiness.setReportLink(reportPaths.toString().replaceAll("\\[|\\]", ""));
            /*onBusiness.setEmpId(emp.getId());
            onBusiness.setEmpName(emp.getName());*/
            //添加创建人和更新人信息
            onBusiness.setCreateInfo();
            ResponseData data = ResponseData.ok();
            //更新行政管理主表中数据
            Administrative administrative = new Administrative();
            administrative.setId(onBusiness.getAdministrativeId());
            administrative.setTitle(onBusiness.getTitle());//标题
            administrative.setAdministrativeType(4);
            administrative.setAdministrativeName("出差");
            administrative.setAdministrativeTime(onBusiness.getDays());//时长
            administrative.setBeginTime(onBusiness.getBeginTime());
            administrative.setEmpId(onBusiness.getEmpId());
            administrative.setEmpName(onBusiness.getEmpName());
            administrative.setEndtime(onBusiness.getEndTime());
           /* administrative.setDeptId(emp.getDeptId());
            administrative.setDeptName(emp.getDeptName());*/
            administrative.setDeptId(onBusiness.getEmpDept());
            administrative.setDeptName(onBusiness.getEmpDeptName());
            administrative.setCompanyCode(emp.getCompanyCode());
            administrativeMapper.updateByPrimaryKeySelective(administrative);
            onBusiness.setAdministrativeId(administrative.getId());
            onBusinessMapper.updateByPrimaryKeySelective(onBusiness);//将详细信息保存到出差表中
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", onBusiness);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 根据流程管理id删除数据
     *
     * @param admId
     * @return
     */
    @Override
    @Transactional
    public ResponseData deleteOnBusinessByAdmId(Integer admId) {
        ResponseData data = ResponseData.ok();
        //先删除流程主表中的数据
        administrativeMapper.updateStateByPrimaryKey(admId);
        //再删除出差表中的数据
        onBusinessMapper.deleteByAdmId(admId);
        //处理待办
        Administrative adm = administrativeMapper.selectByPrimaryKey(admId);
        if (adm.getItemId() != null) {
            Items items = new Items();
            items.setId(adm.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
        data.putDataValue("message", "操作成功");
        data.putDataValue("entity", admId);
        return data;
    }


    /**
     * 添加或更新出差信息时，判断该类型的假期是否有时间
     *
     * @param typeId
     * @param empId
     * @return
     */
    public boolean getLeaveTime(Integer typeId, Integer empId) {
        if (typeId == 1) {
            AdministrativeVacationTime vacation = vacationTime.getVacationTime(empId);
            return vacation.getVacationTime() <= 0;
        }
        AdministrativeAnnualLeave annualLeave = annualLeaveMap.getAnnualLeave(typeId, empId);
        return annualLeave.getSurplusTime() <= 0;
    }

}
