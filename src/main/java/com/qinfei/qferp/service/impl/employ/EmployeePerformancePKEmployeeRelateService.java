package com.qinfei.qferp.service.impl.employ;

import com.qinfei.qferp.mapper.employ.EmployeePerformancePkEmployeeRelateMapper;
import com.qinfei.qferp.service.employ.IEmployeePerformancePKEmployeeRelateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by yanhonghao on 2019/4/23 17:39.
 */
@Service
class EmployeePerformancePKEmployeeRelateService implements IEmployeePerformancePKEmployeeRelateService {

    @Autowired
    private EmployeePerformancePkEmployeeRelateMapper mapper;
}
