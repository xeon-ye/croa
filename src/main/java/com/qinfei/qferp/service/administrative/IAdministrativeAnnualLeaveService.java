package com.qinfei.qferp.service.administrative;

import com.qinfei.core.ResponseData;

public interface IAdministrativeAnnualLeaveService {
    //通过类型id获取员工的假期信息
    ResponseData getAnnualLeaveByTypeId(Integer typeId);
    //假期的初始化接口
    ResponseData initialize();
}
