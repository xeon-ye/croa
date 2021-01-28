package com.qinfei.qferp.service.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeLeaveType;

import java.util.List;

public interface IAdministrativeLeaveTypeService {
    /**
     * 获取所有的请假类型
     * @return
     */
    List<AdministrativeLeaveType> getType();
}
