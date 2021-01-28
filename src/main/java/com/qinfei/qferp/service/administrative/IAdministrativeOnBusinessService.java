package com.qinfei.qferp.service.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeOnBusiness;
import org.springframework.web.multipart.MultipartFile;

public interface IAdministrativeOnBusinessService {
    //增加请假信息
    ResponseData addOnBusiness(AdministrativeOnBusiness onBusiness, MultipartFile[] multipartFiles, MultipartFile[] pics);
    //更新请假信息
    int updateOnBusiness(AdministrativeOnBusiness onBusiness);
    //删除请假信息
    int deleteOnBusiness(int onBusinessId);
    //通过id获取请假信息
    AdministrativeOnBusiness getOnBusinessById(Integer onBusiness);
    //开启审核流
    ResponseData edit(AdministrativeOnBusiness onBusiness, MultipartFile[] multipartFiles, MultipartFile[] pics);
    //开启审核流（已保存）
    ResponseData editUpdateOnBusiness(AdministrativeOnBusiness onBusiness, MultipartFile[] multipartFiles, MultipartFile[] pics, MultipartFile[] reports);
    //通过id获取请假信息
    ResponseData getOnBusinessByAdministrativeId(Integer onBusinessId);
    //通过流程id更新请假信息
    ResponseData updateOnBusinessByAdmId(AdministrativeOnBusiness onBusiness, MultipartFile[] files, MultipartFile[] pics, MultipartFile[] reports);
    //根据流程id删除数据库数据
    ResponseData deleteOnBusinessByAdmId(Integer admId);

}
