package com.qinfei.qferp.service.impl.study;

import com.qinfei.qferp.entity.study.TrainCourse;
import com.qinfei.qferp.entity.study.TrainCourseSign;
import com.qinfei.qferp.entity.study.TrainSetting;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.study.AnswerCardMapper;
import com.qinfei.qferp.mapper.study.TrainCourseSignMapper;
import com.qinfei.qferp.service.study.ITrainSettingService;
import com.qinfei.qferp.service.study.ITrainStudentService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: TrainStudentService
 * @Description: 培训学员接口
 * @Author: Xuxiong
 * @Date: 2020/4/17 0017 16:03
 * @Version: 1.0
 */
@Service
@Slf4j
public class TrainStudentService implements ITrainStudentService {
    @Autowired
    private TrainCourseSignMapper trainCourseSignMapper;
    @Autowired
    private AnswerCardMapper answerCardMapper;
    @Autowired
    private ITrainSettingService trainSettingService;

    @Override
    public List<TrainCourseSign> listSignUpCourseByParam(Map<String, Object> param) {
        List<TrainCourseSign> trainCourseSignList = new ArrayList<>();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("companyCode", user.getCompanyCode());
                trainCourseSignList = trainCourseSignMapper.listSignUpCourseByParam(param);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return trainCourseSignList;
    }

    @Override
    public int getStudentTotal(Map<String, Object> param) {
        int result = 0;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                param.put("companyCode", user.getCompanyCode());
                result = trainCourseSignMapper.getStudentTotal(param);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public PageInfo<Map<String, Object>> listStudentByParam(Map<String, Object> param, Pageable pageable) {
        List<Map<String, Object>> result = new ArrayList<>();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
                param.put("companyCode", user.getCompanyCode());
                result = trainCourseSignMapper.listStudentByParam(param);
                //获取用户列表
                if(CollectionUtils.isNotEmpty(result)){
                    List<Integer> userIds = new ArrayList<>();
                    Map<Integer, Map<String, Object>> tmpMap = new HashMap<>();
                    result.forEach(map -> {
                        Integer userId = Integer.parseInt(String.valueOf(map.get("userId")));
                        tmpMap.put(userId, map);
                        userIds.add(userId);
                    });
                    //获取学员考试成绩
                    List<Map<String, Object>> examList = answerCardMapper.listExamNumByUserId(userIds);
                    if(CollectionUtils.isNotEmpty(examList)){
                        examList.forEach(exam -> {
                            Integer userId = Integer.parseInt(String.valueOf(exam.get("createId")));
                            Integer examNum = Integer.parseInt(String.valueOf(exam.get("examNum")));
                            tmpMap.get(userId).put("examNum", examNum);//设置测试次数
                        });
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new PageInfo<>(result);
    }

    @Transactional
    @Override
    public void updateStudentIntergal(TrainCourse trainCourse, TrainCourseSign trainCourseSign) {
        Map<String, Object> param = new HashMap<>();
        param.put("settingModule", "STUDENT_INTEGRAL_RULE");
        param.put("orderFlag", true);
        List<TrainSetting> formulaList = trainSettingService.listTrainSetting(param);
        //获取学员报名课程对应的所有考试成绩计算积分
        float courseExamGrade = answerCardMapper.getGradeByCourseIdAndUserId(trainCourse.getId(), trainCourseSign.getUserId());
        if(CollectionUtils.isNotEmpty(formulaList)){
            //计算课程积分
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("x", (trainCourse.getTrainGrade() != null ? trainCourse.getTrainGrade().toString() : "0"));//课程学分
            variableMap.put("y", (trainCourseSign.getState() == 0 ? "1" : "0"));//学员完课状态
            //计算阅卷状态的答题卡
            variableMap.put("z", courseExamGrade+"");//考试成绩
            trainCourseSign.setIntegral(trainSettingService.calFormula(formulaList, variableMap));
        }else {
            trainCourseSign.setIntegral(courseExamGrade);//默认考试成绩
        }
        trainCourseSignMapper.updateById(trainCourseSign);
    }

    @Override
    public void trainStudentExport(OutputStream outputStream, Map<String, Object> param) {
        try {
            User user = AppUtil.getUser();
            if (user != null) {
                param.put("companyCode", user.getCompanyCode());
                List<Map<String, Object>> result = trainCourseSignMapper.listStudentByParamForExport(param);
                //获取用户列表
                if (CollectionUtils.isNotEmpty(result)) {
                    List<Integer> userIds = new ArrayList<>();
                    Map<Integer, Map<String, Object>> tmpMap = new HashMap<>();
                    result.forEach(map -> {
                        float signNum = Float.parseFloat(String.valueOf(map.get("signNum")));
                        float completeTime = Float.parseFloat(String.valueOf(map.get("completeTime")));
                        float completeRate = Float.parseFloat(String.valueOf(map.get("completeRate")));
                        if (signNum == 0) {
                            map.put("completeTime", new BigDecimal(0).setScale(2));
                            map.put("completeRate", new BigDecimal(0).setScale(2));
                        } else {
                            map.put("completeTime", new BigDecimal(completeTime / signNum).setScale(2, RoundingMode.HALF_UP));
                            map.put("completeRate", new BigDecimal(completeRate / signNum).setScale(2, RoundingMode.HALF_UP));
                        }
                        Integer userId = Integer.parseInt(String.valueOf(map.get("userId")));
                        tmpMap.put(userId, map);
                        userIds.add(userId);
                    });
                    //获取学员考试成绩
                    List<Map<String, Object>> examList = answerCardMapper.listExamNumByUserId(userIds);
                    if (CollectionUtils.isNotEmpty(examList)) {
                        examList.forEach(exam -> {
                            Integer userId = Integer.parseInt(String.valueOf(exam.get("createId")));
                            Integer examNum = Integer.parseInt(String.valueOf(exam.get("examNum")));
                            tmpMap.get(userId).put("examNum", examNum);//设置测试次数
                        });
                    }
                }
                if (CollectionUtils.isNotEmpty(result)) {
                    String[] titles = {"学员姓名", "所属部门", "职位", "平均完成率(%)", "平均完成时间(分钟)", "测试次数", "积分"};
                    String[] obj = {"userName", "deptName", "postName", "completeRate", "completeTime", "examNum", "integral"};
                    ExcelUtil.exportExcel("学员列表", titles, obj, result, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                        if (value != null) {
                            cell.setCellValue(value.toString());
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.error("【学员管理】学员列表导出异常：{}", e.getMessage());
        }
    }
}
