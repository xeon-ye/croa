package com.qinfei.qferp.service.impl.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeLeaveType;
import com.qinfei.qferp.mapper.administrative.AdministrativeLeaveTypeMapper;
import com.qinfei.qferp.service.administrative.IAdministrativeLeaveTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdministrativeLeaveTypeService implements IAdministrativeLeaveTypeService {

    @Autowired
    private AdministrativeLeaveTypeMapper leaveTypeMapper;

    /**
     * 获取所有的请假类型
     * @return
     */
    @Override
    public List<AdministrativeLeaveType> getType() {
        return leaveTypeMapper.getAllTpe();
    }
}
