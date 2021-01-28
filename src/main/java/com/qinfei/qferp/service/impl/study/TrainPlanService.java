package com.qinfei.qferp.service.impl.study;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.TrainPlan;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.study.TrainPlanMapper;
import com.qinfei.qferp.service.study.ITrainPlanService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainPlanService
 * @Description: 培训计划表接口
 * @Author: Xuxiong
 * @Date: 2020/4/8 0008 10:34
 * @Version: 1.0
 */
@Service
public class TrainPlanService implements ITrainPlanService {
    @Autowired
    private TrainPlanMapper trainPlanMapper;

    @Transactional
    @Override
    public void save(TrainPlan trainPlan) {
        try{
            User user = AppUtil.getUser();
            validTrainPlan(user, trainPlan);//数据校验
            trainPlanMapper.save(trainPlan);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"新增培训计划异常！");
        }
    }

    @Transactional
    @Override
    public void updateStateById(byte state, int id) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            trainPlanMapper.updateStateById(state, user.getId(), id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"修改培训计划状态异常！");
        }
    }

    @Transactional
    @Override
    public void update(TrainPlan trainPlan) {
        try{
            User user = AppUtil.getUser();
            validTrainPlan(user, trainPlan);//数据校验
            trainPlanMapper.updateById(trainPlan);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"编辑培训计划异常！");
        }
    }

    @Override
    public int getTrainPlanTotal(Map<String, Object> param) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            result = trainPlanMapper.getTrainPlanTotal(param);
        }
        return result;
    }

    @Override
    public PageInfo<TrainPlan> listTrainPlan(Map<String, Object> param, Pageable pageable) {
        List<TrainPlan> trainPlanList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            trainPlanList = trainPlanMapper.listTrainPlanByParam(param);
        }
        return new PageInfo<>(trainPlanList);
    }

    //校验培训计划
    private void validTrainPlan(User user, TrainPlan trainPlan){
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(StringUtils.isEmpty(trainPlan.getTitle())){
            throw new QinFeiException(1002, "培训计划标题不能为空！");
        }
        if(trainPlan.getCoursePlate() == null){
            throw new QinFeiException(1002, "课程板块不能为空！");
        }
        if(trainPlan.getTrainWay() == null){
            throw new QinFeiException(1002, "培训方式不能为空！");
        }
        if(trainPlan.getCoursewareKeep() == null){
            trainPlan.setCoursewareKeep((byte) 0);
        }
        trainPlan.setCompanyCode(user.getCompanyCode());
        trainPlan.setCreateId(user.getId());
        trainPlan.setUpdateId(user.getId());
    }
}
