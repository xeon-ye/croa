package com.qinfei.qferp.service.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeVacationTime;
import com.qinfei.qferp.entity.sys.User;

public interface IAdministrativeVacationTimeService {
    /**
     * 根据员工id获取剩余的调休时间
     * @param empId
     * @return
     */
    AdministrativeVacationTime getVacationTime(Integer empId);

    //减少剩余调休时间
    int decreaseVacationTime(AdministrativeVacationTime vacationTime);

    //增加剩余调休时间
    int addVacationTime(AdministrativeVacationTime vacationTime);

    int changVacationTime(Integer empId,String empName, Double changeTime, Integer state, Integer leaveId);

}
