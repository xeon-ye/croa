package com.qinfei.qferp.service.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeLeave;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

public interface IAdministrativeLeaveService {
    //增加请假信息
    ResponseData addLeave(AdministrativeLeave leave, MultipartFile[] multipartFiles, MultipartFile[] pics);
    //更新请假信息
    int updateLeave(AdministrativeLeave leave);
    //删除请假信息
    int deleteLeave(int leaveId);
    //通过id获取请假信息
    AdministrativeLeave getLeaveById(Integer leaveId);
    //开启审核流
    ResponseData edit(AdministrativeLeave leave,MultipartFile[] multipartFiles, MultipartFile[] pics,Integer nextUser, String nextUserName, Integer nextUserDept);
    //开启审核流（已保存）
    ResponseData editUpdateLeave(AdministrativeLeave leave,MultipartFile[] multipartFiles, MultipartFile[] pics, Integer nextUser, String nextUserName, Integer nextUserDept);
    //请假申请的流程更新状态
    void processLeava(Administrative leave);
    //通过id获取请假信息
    ResponseData getLeaveByAdministrativeId(Integer leaveId);
    //通过流程id更新请假信息
    ResponseData updateLeaveByAdmId(AdministrativeLeave leave, MultipartFile[] files, MultipartFile[] pics);
    //根据流程id删除数据库数据
    ResponseData deleteLeaveByAdmId(Integer admId);
    //判断是否可新增请假
    ResponseData validationAddLeave(Date endDate, Integer num, Integer id);
}
