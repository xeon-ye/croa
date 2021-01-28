package com.qinfei.qferp.service.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeLeave;
import com.qinfei.qferp.entity.administrative.AdministrativeOverTimeWork;
import org.springframework.web.multipart.MultipartFile;

public interface IAdministrativeOverTimeWorkService {
    //增加加班信息
    ResponseData addTimework(AdministrativeOverTimeWork timework, MultipartFile[] files, MultipartFile[] pics);
    //更新加班信息
    int updateTimework(AdministrativeOverTimeWork timework);
    //删除加班信息
    int deleteTimework(int timeworkId);
    //通过id获取加班信息
    AdministrativeOverTimeWork getTimeworkById(Integer timeworkId);
    //开启审核流
    ResponseData edit(AdministrativeOverTimeWork timework, MultipartFile[] files, MultipartFile[] pics);
    //开启审核流（已保存）
    ResponseData editUpdateTimeWork(AdministrativeOverTimeWork timework, MultipartFile[] multipartFiles, MultipartFile[] pics);
    //通过id获取加班信息
    ResponseData getTimeworkByAdministrativeId(Integer admId);
    //通过流程id更新加班信息
    ResponseData updateTimeworkByAdmId(AdministrativeOverTimeWork timework, MultipartFile[] files, MultipartFile[] pics);
    //根据流程id删除数据库数据
    ResponseData deleteTimeworkByAdmId(Integer admId);

}
