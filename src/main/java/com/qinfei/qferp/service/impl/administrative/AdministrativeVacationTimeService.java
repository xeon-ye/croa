package com.qinfei.qferp.service.impl.administrative;

import com.qinfei.qferp.entity.administrative.AdministrativeVacationTime;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.administrative.AdministrativeVacationTimeMapper;
import com.qinfei.qferp.service.administrative.IAdministrativeVacationTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdministrativeVacationTimeService implements IAdministrativeVacationTimeService {
    @Autowired
    private AdministrativeVacationTimeMapper vacationTimeMapper;

    /**
     * 获取员工的调休时间
     * @param empId
     * @return
     */
    @Override
    public AdministrativeVacationTime getVacationTime(Integer empId) {

        return vacationTimeMapper.selectByEmpId(empId);
    }

    /**
     * 减少剩余调休时间
     * @param vacationTime
     * @return
     */
    @Override
    public int decreaseVacationTime(AdministrativeVacationTime vacationTime) {
        return vacationTimeMapper.insertSelective(vacationTime);
    }

    /**
     * 增加调休时间
     * @param vacationTime
     * @return
     */
    @Override
    public int addVacationTime(AdministrativeVacationTime vacationTime) {
        return vacationTimeMapper.insertSelective(vacationTime);
    }

    /**
     * 更新调休的剩余时间
     * @param
     * @param changeTime
     * @param state
     * @return
     */
    @Override
    public int changVacationTime(Integer empId,String empName, Double changeTime, Integer state, Integer id) {
        int data =0;
        //获取该员工的剩余调休时间
        AdministrativeVacationTime lastTime = getVacationTime(empId);
        AdministrativeVacationTime vacationTime = new AdministrativeVacationTime();
        vacationTime.setEmpId(empId);
        vacationTime.setState(state);
        vacationTime.setChangeTime(changeTime);
        vacationTime.setEmpName(empName);
        if(lastTime!=null){
            vacationTime.setVacationTime(lastTime.getVacationTime()==null?0:lastTime.getVacationTime()+changeTime*state);
        }else{
            vacationTime.setVacationTime(changeTime*state);
        }
        vacationTime.setCreateInfo();//设置创建人信息
        data =vacationTimeMapper.insertSelective(vacationTime);
        return  data;
    }


}
