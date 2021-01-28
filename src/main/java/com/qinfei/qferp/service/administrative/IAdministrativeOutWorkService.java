package com.qinfei.qferp.service.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeOutWork;
import org.springframework.web.multipart.MultipartFile;

public interface IAdministrativeOutWorkService {
    //增加外出信息
    ResponseData addOutWork(AdministrativeOutWork outWork, MultipartFile[] multipartFiles, MultipartFile[] pics);
    //更新外出信息
    int updateOutWork(AdministrativeOutWork outWork);
    //删除外出信息
    int deleteOutWork(int outWorkId);
    //通过id获取外出信息
    AdministrativeOutWork getOutWorkById(Integer outWorkId);
    //开启审核流
    ResponseData edit(AdministrativeOutWork outWork, MultipartFile[] multipartFiles, MultipartFile[] pics);
    //开启审核流（已保存）
    ResponseData editUpdateOutWork(AdministrativeOutWork outWork, MultipartFile[] multipartFiles, MultipartFile[] pics);
    //通过id获取外出信息
    ResponseData getOutWorkByAdministrativeId(Integer outWorkId);
    //通过流程id更新外出信息
    ResponseData updateOutWorkByAdmId(AdministrativeOutWork outWork, MultipartFile[] files, MultipartFile[] pics);
    //根据流程id删除数据库数据
    ResponseData deleteOutWorkByAdmId(Integer admId);
}
