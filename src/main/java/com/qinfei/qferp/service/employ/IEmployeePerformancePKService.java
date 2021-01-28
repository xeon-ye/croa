package com.qinfei.qferp.service.employ;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.employ.EmployeePerformancePk;

import java.util.List;
import java.util.Map;

/**
 * Created by yanhonghao on 2019/4/23 17:27.
 */
public interface IEmployeePerformancePKService {
    void save(EmployeePerformancePk employeePerformancePk);

    EmployeePerformancePk findById(int id);

    List<EmployeePerformancePk> all(Map<String, Object> map);

    void deleteById(int performanceId);

    void copy(int performanceId);

    void allWithProfit(ResponseData data, Map<String, Object> map);

    void years(ResponseData result);
}
