package com.qinfei.qferp.service.impl.study;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.TrainTeacher;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.study.TrainTeacherMapper;
import com.qinfei.qferp.service.study.ITrainTeacherService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainTeacherService
 * @Description: 培训讲师接口
 * @Author: Xuxiong
 * @Date: 2020/4/11 0011 8:39
 * @Version: 1.0
 */
@Service
@Slf4j
public class TrainTeacherService implements ITrainTeacherService {
    @Autowired
    private TrainTeacherMapper trainTeacherMapper;

    @Transactional
    @Override
    public void save(TrainTeacher trainTeacher) {
        try {
            User user = AppUtil.getUser();
            validTrainTeacher(user, trainTeacher); //校验
            trainTeacherMapper.save(trainTeacher);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "申请讲师异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void update(TrainTeacher trainTeacher) {
        try {
            User user = AppUtil.getUser();
            validTrainTeacher(user, trainTeacher); //校验
            trainTeacherMapper.updateById(trainTeacher);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "修改讲师异常，请联系技术人员！");
        }
    }

    @Override
    public void del(int id, int userId) {
        try {
           User user = AppUtil.getUser();
           if(user == null){
               throw new QinFeiException(1002, "请先登录！");
           }
           trainTeacherMapper.updateStateById((byte) -9, user.getId(), id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "删除讲师异常，请联系技术人员！");
        }
    }

    @Override
    public TrainTeacher getTrainTeacherByUserId(int userId) {
        return trainTeacherMapper.getTrainTeacherByUserId(userId);
    }

    @Override
    public int getTrainTeacherTotal(Map<String, Object> param) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            result = trainTeacherMapper.getTrainTeacherTotal(param);
        }
        return result;
    }

    @Override
    public PageInfo<TrainTeacher> listTrainTeacher(Map<String, Object> param, Pageable pageable) {
        List<TrainTeacher> trainPlanList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            trainPlanList = trainTeacherMapper.listTrainTeacher(param);
        }
        return new PageInfo<>(trainPlanList);
    }

    @Override
    public List<Map<String, Object>> listUserNotTeacher(boolean existsFlag) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            User user = AppUtil.getUser();
            if(user != null){
                result = trainTeacherMapper.listUserNotTeacher(user.getCompanyCode(), existsFlag);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listUser() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            User user = AppUtil.getUser();
            if(user != null){
                result = trainTeacherMapper.listUser(user.getCompanyCode());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void trainTeacherStatisticsExport(OutputStream outputStream, Map<String, Object> param) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            User user = AppUtil.getUser();
            if (user != null) {
                param.put("companyCode", user.getCompanyCode());
                result = trainTeacherMapper.listTrainTeacherForExport(param);
            }
            if (CollectionUtils.isNotEmpty(result)) {
                for (Map<String, Object> data : result) {
                    float teacherComplateCourseNum = Float.parseFloat(String.valueOf(data.get("teacherComplateCourseNum")));
                    float teacherCourseAvgScore = Float.parseFloat(String.valueOf(data.get("teacherCourseAvgScore")));
                    float teacherCourseLikeNum = Float.parseFloat(String.valueOf(data.get("teacherCourseLikeNum")));
                    float teacherCourseVentNum = Float.parseFloat(String.valueOf(data.get("teacherCourseVentNum")));
                    if (teacherComplateCourseNum == 0) {
                        data.put("teacherCourseAvgScore", new BigDecimal(0).setScale(2));
                        data.put("teacherCourseAvgLikeNum", new BigDecimal(0).setScale(2));
                        data.put("teacherCourseAvgVentNum", new BigDecimal(0).setScale(2));
                    } else {
                        data.put("teacherCourseAvgScore", new BigDecimal(teacherCourseAvgScore / teacherComplateCourseNum).setScale(2, RoundingMode.HALF_UP));
                        data.put("teacherCourseAvgLikeNum", new BigDecimal(teacherCourseLikeNum / teacherComplateCourseNum).setScale(2, RoundingMode.HALF_UP));
                        data.put("teacherCourseAvgVentNum", new BigDecimal(teacherCourseVentNum / teacherComplateCourseNum).setScale(2, RoundingMode.HALF_UP));
                    }
                }
                String[] titles = {"讲师姓名", "关联课程", "总积分", "平均评分(完课单课程平均评分总和/完课课程数)", "平均点赞(完课课程点赞数总和/完课课程数)", "平均吐槽(完课课程吐槽数总和/完课课程数)", "总评论"};
                String[] obj = {"name", "teacherCourseNum", "teacherIntegral", "teacherCourseAvgScore", "teacherCourseAvgLikeNum", "teacherCourseAvgVentNum", "teacherCourseCommentNum"};
                ExcelUtil.exportExcel("讲师统计列表", titles, obj, result, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                });
            }
        } catch (Exception e) {
            log.error("【讲师管理】讲师统计列表导出异常：{}", e.getMessage());
        }
    }

    //校验讲师信息
    private void validTrainTeacher(User user, TrainTeacher  trainTeacher){
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(trainTeacher.getUserId() == null){
            throw new QinFeiException(1002, "申请讲师不能为空！");
        }
        trainTeacher.setCreateId(user.getId());
        trainTeacher.setUpdateId(user.getId());
        trainTeacher.setCompanyCode(user.getCompanyCode());
    }
}
