package com.qinfei.qferp.service.impl.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeAnnualLeave;
import com.qinfei.qferp.entity.administrative.AdministrativeLeave;
import com.qinfei.qferp.entity.employ.Employee;
import com.qinfei.qferp.entity.employ.EmployeeConnect;
import com.qinfei.qferp.entity.employ.EmployeeLeave;
import com.qinfei.qferp.mapper.administrative.AdministrativeAnnualLeaveMapper;
import com.qinfei.qferp.mapper.employ.EmployeeBasicMapper;
import com.qinfei.qferp.mapper.employ.EmployeeConnectMapper;
import com.qinfei.qferp.mapper.employ.EmployeeLeaveMapper;
import com.qinfei.qferp.mapper.employ.EmployeeMapper;
import com.qinfei.qferp.service.administrative.IAdministrativeAnnualLeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdministrativeAnnualLeaveService implements IAdministrativeAnnualLeaveService {

    @Autowired
    private AdministrativeAnnualLeaveMapper annualLeaveMap;
    @Autowired
    private EmployeeMapper employee;
    @Autowired
    private EmployeeConnectMapper connectMapper;
    @Autowired
    private EmployeeBasicMapper employeeBasic;

    /**
     * 根据类型id获取员工的假期信息
     * @param typeId
     * @return
     */
    @Override
    public ResponseData getAnnualLeaveByTypeId(Integer typeId) {
        ResponseData data = ResponseData.ok();
        AdministrativeAnnualLeave annualLeave = annualLeaveMap.getAnnualLeaveByTypeId(typeId);
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", annualLeave);
        return data;
    }

    /**
     * 年假初始化接口
     * @return
     */
    @Override
    public ResponseData initialize() {
        ResponseData data = ResponseData.ok();
        //1.获取所有在职员工
        List<Map<String, Object>> list = employee.selectPageEmployee(new HashMap<>());
        //2.将所有信息插入到假期表中
        for (Map<String, Object> map:list){
            Map<String, Object> params = new HashMap<>();
            //根据员工id，获取入职时间，根据入职时间设置年假时间
            params.put("empId",map.get("user_id"));
            EmployeeConnect emp = connectMapper.selectByParentId(params);
            Date empTime = emp.getEmpDate();
            //获取一年前时间
            Date lastYear = DateUtils.getLastYearDate();
            Integer days = DateUtils.getDiffDays(empTime,lastYear);
            double year = days/365;
            double surplusTime = Math.ceil(year)+1;
            //获取数据
            AdministrativeAnnualLeave annualLeave = new AdministrativeAnnualLeave();
            annualLeave.setDempId(emp.getEmpDept());
            annualLeave.setDempName(emp.getEmpDeptName());
            annualLeave.setEmpId(emp.getEmpId());
            annualLeave.setEmpName(emp.getEmpName());
            annualLeave.setSurplusTime((int)surplusTime);
            annualLeave.setTypeId(2);//年假
            annualLeave.setTime((int)surplusTime);
            annualLeave.setTypeName("年假");
            annualLeave.setCreateInfo();
        }
        data.putDataValue("message","操作成功");
        return data;
    }

}
