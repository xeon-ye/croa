package com.qinfei.qferp.service.impl.study;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.EncryptUtils;
import com.qinfei.qferp.entity.study.*;
import com.qinfei.qferp.entity.sys.DeptZw;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.study.*;
import com.qinfei.qferp.mapper.sys.DeptZwMapper;
import com.qinfei.qferp.service.study.ITrainCourseService;
import com.qinfei.qferp.service.study.ITrainSettingService;
import com.qinfei.qferp.service.study.ITrainStudentService;
import com.qinfei.qferp.service.study.ITrainTeacherService;
import com.qinfei.qferp.service.sys.IUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @CalssName: TrainCourseService
 * @Description: 培训课程表接口
 * @Author: Xuxiong
 * @Date: 2020/4/9 0009 11:25
 * @Version: 1.0
 */
@Service
@Slf4j
public class TrainCourseService implements ITrainCourseService {
    @Autowired
    private TrainCourseMapper trainCourseMapper;
    @Autowired
    private TrainTeacherMapper trainTeacherMapper;
    @Autowired
    private TrainCourseRangeMapper trainCourseRangeMapper;
    @Autowired
    private TrainCourseSignMapper trainCourseSignMapper;
    @Autowired
    private TrainCourseCommentMapper trainCourseCommentMapper;
    @Autowired
    private AnswerCardMapper answerCardMapper;
    @Autowired
    private ITrainTeacherService trainTeacherService;
    @Autowired
    private ITrainSettingService trainSettingService;
    @Autowired
    private ITrainStudentService trainStudentService;
    @Autowired
    private IUserService userService;
    @Autowired
    private DeptZwMapper deptZwMapper;

    @Transactional
    @Override
    public void signUp(TrainCourse trainCourse) {
        try {
            User user = AppUtil.getUser();
            validCourse(user, trainCourse);
            trainCourse.setState((byte) 0);//状态：0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、-1-审核驳回、-9-删除
            //如果有ID则新增，否则编辑
            trainCourseMapper.save(trainCourse);
            //判断当前用户是否是讲师，不是设置成讲师，由于此处每次报名都会校验，所以读取采用缓存
            TrainTeacher trainTeacher = trainTeacherService.getTrainTeacherByUserId(user.getId());
            if(trainTeacher == null){
                TrainTeacher trainTeacherTmp = new TrainTeacher();
                trainTeacherTmp.setCreateId(user.getId());
                trainTeacherTmp.setCompanyCode(user.getCompanyCode());
                trainTeacherTmp.setUserId(user.getId());
                trainTeacherMapper.save(trainTeacherTmp);
            }
            //判断是否有课程范围
            handleCourseRange(trainCourse);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "培训计划报名操作异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void update(TrainCourse trainCourse) {
        try {
            User user = AppUtil.getUser();
            validCourse(user, trainCourse);
            trainCourseMapper.updateById(trainCourse);
            //删除原来的课程范围
            trainCourseRangeMapper.updateStateByCourseId((byte) -9, user.getId(), trainCourse.getId());
            //判断是否有课程范围
            handleCourseRange(trainCourse);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程修改操作异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void updateStateById(byte state, int id) {
        try {
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            trainCourseMapper.updateStateById(state, user.getId(), id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "修改课程状态异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void auditState(byte state, String rejectReason, int id) {
        try {
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //审核同意，计算积分
            if(state == 2){
                TrainCourse trainCourse = trainCourseMapper.getTrainCourseById(id);
                if(trainCourse == null){
                    throw new QinFeiException(1002, "课程不存在！");
                }

                trainCourse.setState(state);//更新状态
                trainCourse.setRejectReason(rejectReason);//审核原因
                updateCourseIntergal(trainCourse);//更新课程积分
            }else {
                trainCourseMapper.auditById(state, rejectReason, user.getId(), id);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程审批异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void batchAuditState(byte state, String rejectReason, List<Integer> ids) {
        try {
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //审核同意，计算积分
            if(state == 2){
                List<TrainCourse> trainCourseList = trainCourseMapper.listTrainCourseByIds(ids);
                if(CollectionUtils.isEmpty(trainCourseList)){
                    throw new QinFeiException(1002, "课程不存在！");
                }
                Map<String, Object> param = new HashMap<>();
                param.put("settingModule", "TEACHER_INTEGRAL_RULE");
                param.put("orderFlag", true);
                List<TrainSetting> formulaList = trainSettingService.listTrainSetting(param);
                trainCourseList.forEach(trainCourse -> {
                    trainCourse.setState(state);//更新状态
                    trainCourse.setRejectReason(rejectReason);//审核原因
                    calCourseIntergal(formulaList, trainCourse);//积分计算
                });
                trainCourseMapper.batchUpdate(user.getId(), trainCourseList);
            }else {
                trainCourseMapper.batchAuditById(state, rejectReason, user.getId(), ids);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程审批异常，请联系技术人员！");
        }
    }

    @Override
    public List<TrainCourse> listTrainCourseByTeacher() {
        List<TrainCourse> trainCourseList = null;
        try{
            User user = AppUtil.getUser();
            if(user != null){
                trainCourseList = trainCourseMapper.listTrainCourseByTeacher(user.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return trainCourseList;
    }

    @Override
    public int getCourseTotal(Map<String, Object> param) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            if(param.get("rangeIdList") != null && StringUtils.isNotEmpty(String.valueOf(param.get("rangeIdList")))){
                String str = String.valueOf(param.get("rangeIdList"));
                param.put("rangeIdList", Arrays.asList(str.split(",")));
            }else {
                param.remove("rangeIdList");
            }
            if(param.get("stateList") != null && StringUtils.isNotEmpty(String.valueOf(param.get("stateList")))){
                String str = String.valueOf(param.get("stateList"));
                param.put("stateList", Arrays.asList(str.split(",")));
            }else {
                param.remove("stateList");
            }
            //如果是报名页面，则查看自己可报名的课程
            if(param.get("sign") != null){
                param.put("rangeUserId", user.getId());
                param.put("rangeDeptId", user.getDeptId());
                if(CollectionUtils.isNotEmpty(user.getRoles())){
                    List<String> rangeRole = new ArrayList<>();
                    user.getRoles().forEach(role -> {
                        rangeRole.add(String.format("%s%s", role.getType(), role.getCode()));
                    });
                    param.put("rangeRole", rangeRole);
                }
            }
            result = trainCourseMapper.getCourseTotal(param);
        }
        return result;
    }

    @Override
    public PageInfo<TrainCourse> listCourseByParam(Map<String, Object> param, Pageable pageable) {
        List<TrainCourse> trainPlanList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            if(param.get("rangeIdList") != null && StringUtils.isNotEmpty(String.valueOf(param.get("rangeIdList")))){
                String str = String.valueOf(param.get("rangeIdList"));
                param.put("rangeIdList", Arrays.asList(str.split(",")));
            }else {
                param.remove("rangeIdList");
            }
            if(param.get("stateList") != null && StringUtils.isNotEmpty(String.valueOf(param.get("stateList")))){
                String str = String.valueOf(param.get("stateList"));
                param.put("stateList", Arrays.asList(str.split(",")));
            }else {
                param.remove("stateList");
            }
            //如果是报名页面，则查看自己可报名的课程
            if(param.get("sign") != null){
                param.put("rangeUserId", user.getId());
                param.put("rangeDeptId", user.getDeptId());
                if(CollectionUtils.isNotEmpty(user.getRoles())){
                    List<String> rangeRole = new ArrayList<>();
                    user.getRoles().forEach(role -> {
                        rangeRole.add(String.format("%s%s", role.getType(), role.getCode()));
                    });
                    param.put("rangeRole", rangeRole);
                }
            }
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            trainPlanList = trainCourseMapper.listCourseByParam(param);
        }
        return new PageInfo<>(trainPlanList);
    }

    @Override
    public TrainCourse getTrainCourseDeailById(int id) {
        TrainCourse trainCourse = null;
        try {
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            trainCourse = trainCourseMapper.getTrainCourseDeailById(id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "获取课程详情异常，请联系技术人员！");
        }
        return trainCourse;
    }

    @Override
    public List<TrainCourseSign> listCourseSign(int courseId) {
        List<TrainCourseSign> result = null;
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            result = trainCourseSignMapper.listCourseSignByCourseId(courseId);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "获取课程学员列表异常，请联系技术人员！");
        }
        return result;
    }

    @Override
    public Map<String, Object> getCourseDetailById(int id) {
        Map<String, Object> result = null;
        try {
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            result = trainCourseMapper.getCourseDetailById(id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "获取课程详情异常，请联系技术人员！");
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listCourseRange(String signStr, String name) {
        List<Map<String, Object>> result = new ArrayList<>();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                if("role".equals(signStr)){
                    result = trainCourseMapper.listRoleByParam(name);
                }else if("dept".equals(signStr)){
                    result = trainCourseMapper.listDeptByParam(name, user.getCompanyCode());
                }else {
                    result = trainCourseMapper.listUserByParam(name, user.getCompanyCode());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Transactional
    @Override
    public void courseSignUp(int courseId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            TrainCourse trainCourse = trainCourseMapper.getTrainCourseById(courseId);
            if(trainCourse == null){
                throw new QinFeiException(1002, "课程不存在！");
            }
            if(trainCourse.getSignStartTime() != null && new Date().compareTo(trainCourse.getSignStartTime()) == -1 ){
                throw new QinFeiException(1002, "课程未到报名时间，不能进行报名！");
            }
            if(trainCourse.getSignEndTime() != null && new Date().compareTo(trainCourse.getTrainEndTime()) == 1 ){
                throw new QinFeiException(1002, "课程已过报名时间，不能进行报名！");
            }
            TrainCourseSign oldTrainCourseSign = trainCourseSignMapper.getCourseSignByUserId(courseId, user.getId());
            if(oldTrainCourseSign != null){
                throw new QinFeiException(1002, "您已报名课程，不能重复报名！");
            }
            TrainCourseSign trainCourseSign = new TrainCourseSign();
            trainCourseSign.setCourseId(courseId);
            trainCourseSign.setUserId(user.getId());
            trainCourseSign.setCreateId(user.getId());
            trainCourseSign.setUpdateId(user.getId());
            trainCourseSign.setCompanyCode(user.getCompanyCode());
            trainCourseSign.setState((byte) 0);
            //计算学员积分
            updateStudentIntergal(trainCourse, trainCourseSign);
            //课程时间，单位：分钟
            int completeTime = (int) ((trainCourse.getTrainEndTime().getTime() - trainCourse.getTrainStartTime().getTime()) / 1000 / 60);
            trainCourseSign.setCompleteTime(completeTime);
            trainCourseSignMapper.save(trainCourseSign);
            //更新课程报名人数 + 1
            trainCourse.setCourseSignNum(trainCourse.getCourseSignNum() + 1);
            //更新课程完课人数 + 1
            trainCourse.setCourseCompleteNum(trainCourse.getCourseCompleteNum() + 1);
            //设置课程状态为正常:0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回、-9-删除',
            trainCourse.setState((byte) 1);
            updateCourseIntergal(trainCourse);//更新课程积分
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程报名异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void courseVent(int courseId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            TrainCourse trainCourse = trainCourseMapper.getTrainCourseById(courseId);
            if(trainCourse == null){
                throw new QinFeiException(1002, "课程不存在！");
            }
            if(new Date().compareTo(trainCourse.getTrainEndTime()) == -1 ){
                throw new QinFeiException(1002, "培训还未结束，不能进行吐槽！");
            }
            if(new Date().compareTo(trainCourse.getCourseEndTime()) > 0){
                throw new QinFeiException(1002, "已过课程反馈截止时间，不能进行吐槽！");
            }
            TrainCourseSign oldTrainCourseSign = trainCourseSignMapper.getCourseSignByUserId(courseId, user.getId());
            if(oldTrainCourseSign == null){
                throw new QinFeiException(1002, "您未报名课程，不能进行吐槽！");
            }
            if(oldTrainCourseSign.getState() != 0){
                throw new QinFeiException(1002, "您课程存在早退/迟到/旷课情况，不允许吐槽！");
            }
            //是否吐槽：0-未吐槽、1-吐槽，未吐槽则触发吐槽
            if(oldTrainCourseSign.getVentFlag() == 0){
                oldTrainCourseSign.setVentFlag((byte) 1);
                trainCourse.setCourseVentNum(trainCourse.getCourseVentNum() + 1);//吐槽数 + 1
            }else {
                oldTrainCourseSign.setVentFlag((byte) 0);
                trainCourse.setCourseVentNum(trainCourse.getCourseVentNum() - 1);//吐槽数 - 1
            }
            trainCourseSignMapper.updateById(oldTrainCourseSign);//更新数据
            updateCourseIntergal(trainCourse);//更新课程积分
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程吐槽异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void courseLike(int courseId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            TrainCourse trainCourse = trainCourseMapper.getTrainCourseById(courseId);
            if(trainCourse == null){
                throw new QinFeiException(1002, "课程不存在！");
            }
            if(new Date().compareTo(trainCourse.getTrainEndTime()) == -1 ){
                throw new QinFeiException(1002, "培训还未结束，不能进行点赞！");
            }
            if(new Date().compareTo(trainCourse.getCourseEndTime()) > 0 ){
                throw new QinFeiException(1002, "已过课程反馈截止时间，不能进行点赞！");
            }
            TrainCourseSign oldTrainCourseSign = trainCourseSignMapper.getCourseSignByUserId(courseId, user.getId());
            if(oldTrainCourseSign == null){
                throw new QinFeiException(1002, "您未报名课程，不能进行点赞！");
            }
            if(oldTrainCourseSign.getState() != 0){
                throw new QinFeiException(1002, "您课程存在早退/迟到/旷课情况，不允许点赞！");
            }
            //是否点赞：0-未点赞、1-点赞，未点赞则进行点赞
            if(oldTrainCourseSign.getLikeFlag() == 0){
                oldTrainCourseSign.setLikeFlag((byte) 1);
                trainCourse.setCourseLikeNum(trainCourse.getCourseLikeNum() + 1);//点赞数 + 1
            }else {
                oldTrainCourseSign.setLikeFlag((byte) 0);
                trainCourse.setCourseLikeNum(trainCourse.getCourseLikeNum() - 1);//点赞数 - 1
            }
            trainCourseSignMapper.updateById(oldTrainCourseSign);//更新数据
            updateCourseIntergal(trainCourse);//更新课程积分
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程点赞异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void courseSetScore(int courseId, float score) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            TrainCourse trainCourse = trainCourseMapper.getTrainCourseById(courseId);
            if(trainCourse == null){
                throw new QinFeiException(1002, "课程不存在！");
            }
            if(new Date().compareTo(trainCourse.getTrainEndTime()) == -1 ){
                throw new QinFeiException(1002, "培训还未结束，不能进行评分！");
            }
            if(new Date().compareTo(trainCourse.getCourseEndTime()) > 0){
                throw new QinFeiException(1002, "已过课程反馈截止时间，不能进行评分！");
            }
            TrainCourseSign oldTrainCourseSign = trainCourseSignMapper.getCourseSignByUserId(courseId, user.getId());
            if(oldTrainCourseSign == null){
                throw new QinFeiException(1002, "您未报名课程，不能进行评分！");
            }
            if(oldTrainCourseSign.getScoreFlag() == 1){
                throw new QinFeiException(1002, "您已经进行过评分，不允许多次评分！");
            }
            if(oldTrainCourseSign.getState() != 0){
                throw new QinFeiException(1002, "您课程存在早退/迟到/旷课情况，不允许评分！");
            }
            oldTrainCourseSign.setScore(score);
            oldTrainCourseSign.setScoreFlag((byte) 1);
            trainCourseSignMapper.updateById(oldTrainCourseSign);//更新报名数据
            trainCourse.setCourseScore(trainCourse.getCourseScore() + score);//课程总评分数 + score
            trainCourse.setCourseScoreNum(trainCourse.getCourseScoreNum() + 1);//课程评分人数 + 1
            updateCourseIntergal(trainCourse);//更新课程积分
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程评分异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void courseSetComment(TrainCourseComment courseComment) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(StringUtils.isEmpty(courseComment.getCourseComment())){
                throw new QinFeiException(1002, "评论内容不能为空！");
            }
            TrainCourseSign oldTrainCourseSign = trainCourseSignMapper.getCourseSignByUserId(courseComment.getCourseId(), user.getId());
            if(oldTrainCourseSign == null){
                throw new QinFeiException(1002, "您未报名课程，不能进行评论！");
            }
            TrainCourse trainCourse = trainCourseMapper.getTrainCourseById(courseComment.getCourseId());
            if(trainCourse == null){
                throw new QinFeiException(1002, "课程不存在！");
            }
            courseComment.setCompanyCode(user.getCompanyCode());
            courseComment.setUpdateId(user.getId());
            courseComment.setCreateId(user.getId());
            trainCourseCommentMapper.save(courseComment);
            trainCourse.setCourseCommentNum(trainCourse.getCourseCommentNum() + 1);//课程评论数 + 1
            trainCourseMapper.updateById(trainCourse);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程评论异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public TrainCourseSign courseSignState(TrainCourseSign trainCourseSign) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            TrainCourseSign oldTrainCourseSign = trainCourseSignMapper.get(TrainCourseSign.class, trainCourseSign.getId());
            if(oldTrainCourseSign == null){
                throw new QinFeiException(1002, "未报名课程，不能进行修改课程学员状态操作！");
            }
            TrainCourse trainCourse = trainCourseMapper.getTrainCourseById(trainCourseSign.getCourseId());
            if(trainCourse == null){
                throw new QinFeiException(1002, "课程不存在！");
            }
            //课程时间，单位：分钟
            int completeTime = (int) ((trainCourse.getTrainEndTime().getTime() - trainCourse.getTrainStartTime().getTime()) / 1000 / 60);
            //计算完课率
            trainCourseSign.setCompleteTime(trainCourseSign.getCompleteTime() == null ? 0 : trainCourseSign.getCompleteTime());
            trainCourseSign.setCompleteRate(100 * new BigDecimal(trainCourseSign.getCompleteTime()).divide(new BigDecimal(completeTime), 4, RoundingMode.HALF_UP).floatValue());
            trainCourseSign.setUpdateId(user.getId());
            //状态：0-正常、1-迟到、2-早退、3-旷课、-9-删除
            if(trainCourseSign.getState() != 0 && oldTrainCourseSign.getState() == 0){
                //1-迟到、2-早退、3-旷课不能进行点赞、吐槽、评分，需要将数据报名数据清空
                trainCourseSign.setLikeFlag((byte) 0);
                trainCourseSign.setVentFlag((byte) 0);
                trainCourseSign.setScoreFlag((byte) 0);
                trainCourseSign.setScore((float) 0);
                //1-迟到、2-早退、3-旷课不算完课
                trainCourse.setCourseCompleteNum(trainCourse.getCourseCompleteNum() - 1);//课程完课人数 - 1
                //如果学员原来有评分，则需要将课程总评分、总评分人数字段减当前学员评分
                if(oldTrainCourseSign.getScoreFlag() == 1){
                    trainCourse.setCourseScoreNum(trainCourse.getCourseScoreNum() - 1);//评分人数 - 1
                    trainCourse.setCourseScore(trainCourse.getCourseScore() - oldTrainCourseSign.getScore());//评分 - 学员评分
                }
                //如果学员原来有点赞，则课程点赞数 - 1
                if(oldTrainCourseSign.getLikeFlag() == 1){
                    trainCourse.setCourseLikeNum(trainCourse.getCourseLikeNum() - 1);//点赞数 - 1
                }

                //如果学员原来有吐槽，则课程吐槽数 - 1
                if(oldTrainCourseSign.getVentFlag() == 1){
                    trainCourse.setCourseVentNum(trainCourse.getCourseVentNum() - 1);//吐槽数 - 1
                }

                updateCourseIntergal(trainCourse);//更新课程积分
            }
            trainStudentService.updateStudentIntergal(trainCourse, trainCourseSign); //更新学员积分

            return trainCourseSign;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程学员状态修改异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void stopCourse(int courseId, Date trainStartTime) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //停课时间不能大于培训开始时间
            if(new Date().compareTo(trainStartTime) >= 0){
                throw new QinFeiException(1002, "停课时间不能大于培训开始时间！");
            }
            //判断是否有报名学员，有的话不允许停课
            if(CollectionUtils.isNotEmpty(trainCourseSignMapper.listCourseSignByCourseId(courseId))){
                throw new QinFeiException(1002, "课程已有学员报名，需要学员取消报名，才可进行停课！");
            }
            //0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回、-9-删除
            trainCourseMapper.updateStateById((byte) 3, user.getId(), courseId);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程停课异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void adminStopCourse(int courseId) {
        try {
            User user = AppUtil.getUser();
            if (user == null) {
                throw new QinFeiException(1002, "请先登录！");
            }
            TrainCourse trainCourse = trainCourseMapper.getTrainCourseById(courseId);
            if (trainCourse == null) {
                throw new QinFeiException(1002, "课程不存在！");
            }
            //1、课程变成待审核的状态，同时课程所有动态计算的字段都清空
            //状态：0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回
            trainCourse.setState((byte) 0);
            trainCourse.setCourseIntegral(0f);
            trainCourse.setCourseScore(0f);
            trainCourse.setCourseScoreNum(0);
            trainCourse.setCourseLikeNum(0);
            trainCourse.setCourseVentNum(0);
            trainCourse.setCourseCommentNum(0);
            trainCourse.setCourseSignNum(0);
            trainCourse.setCourseCompleteNum(0);
            trainCourse.setUpdateId(user.getId());
            trainCourseMapper.updateById(trainCourse);
            //2、学员报名数据全部删除
            trainCourseSignMapper.updateStateByCourseId((byte) -9, user.getId(), courseId);
            //3、学员答题卡数据全部删除
            answerCardMapper.delCourseAnswerCard(courseId);
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002, "取消课程异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void recoverCourse(int courseId, Date trainStartTime) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            //复课时间不能大于培训开始时间
            if(new Date().compareTo(trainStartTime) >= 0){
                throw new QinFeiException(1002, "复课时间不能大于培训开始时间！");
            }
            //0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回、-9-删除
            trainCourseMapper.updateStateById((byte) 2, user.getId(), courseId);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "课程复课异常，请联系技术人员！");
        }
    }

    @Transactional
    @Override
    public void cancelCourseSign(int signId) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            TrainCourseSign oldTrainCourseSign = trainCourseSignMapper.get(TrainCourseSign.class, signId);
            if(oldTrainCourseSign == null){
                throw new QinFeiException(1002, "您未报名课程，不能进行此操作！");
            }
            TrainCourse trainCourse = trainCourseMapper.getTrainCourseById(oldTrainCourseSign.getCourseId());
            if(trainCourse == null){
                throw new QinFeiException(1002, "课程不存在！");
            }
            if(new Date().compareTo(trainCourse.getTrainStartTime()) > 0){
                throw new QinFeiException(1002, "已过培训开始时间，不能取消报名！");
            }
            trainCourseSignMapper.updateStateById((byte) -9, user.getId(),signId);
            //删除当前报名期间考试的答题卡
            answerCardMapper.delStudentCourseAnswerCard(trainCourse.getId(), oldTrainCourseSign.getUserId());
            //更新课程报名人数 - 1
            trainCourse.setCourseSignNum(trainCourse.getCourseSignNum() - 1);
            //如果报名状态是正常，则更新课程完课人数 - 1
            if(oldTrainCourseSign.getState() == 0){
                trainCourse.setCourseCompleteNum(trainCourse.getCourseCompleteNum() - 1);
            }
            //设置课程状态为正常:0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回、-9-删除
            if(trainCourse.getCourseSignNum() <= 0){
                trainCourse.setState((byte) 2);
            }
            updateCourseIntergal(trainCourse);//更新课程积分
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "取消课程报名异常，请联系技术人员！");
        }
    }

    @Override
    public List<Map<String, Object>> listCourseSignStudent(Integer courseId, Integer viewFlag) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            User user = AppUtil.getUser();
            if (user != null) {
                Map<String, Object> param = new HashMap<>();
                param.put("courseId", courseId);
                //viewFlag: 1-仅看自己、2-部门报名学员、3-培训管理员/讲师自己
                if (viewFlag == 1) {
                    param.put("userId", courseId);
                } else {
                    if (viewFlag == 2) {
                        List<Integer> deptIds = new ArrayList<>();
                        String userDeptIds = userService.getChilds(user.getDept().getId());
                        if (StringUtils.isNotEmpty(userDeptIds) && userDeptIds.indexOf("$,") > -1) {
                            for (String deptId : userDeptIds.substring(2).split(",")) {
                                deptIds.add(Integer.parseInt(deptId));
                            }
                        }
                        //判断当前用户是否是政委，如果是政委则可查看自己所管理部门，否则仅查看自己部门及其子部门
                        if (IConst.ROLE_CODE_ZW.equals(user.getDept().getCode()) && AppUtil.isRoleCode(IConst.ROLE_CODE_ZW)) {
                            List<DeptZw> deptZwList = deptZwMapper.listDeptZwByParam(user.getId(), null);
                            if (CollectionUtils.isNotEmpty(deptZwList)) {
                                for (DeptZw deptZw : deptZwList) {
                                    deptIds.add(deptZw.getDeptId());
                                }
                            }
                        }
                        param.put("deptIds", deptIds);
                    }
                }
                result = trainCourseSignMapper.listStudentByCourseIdAndDept(param);
            }
        } catch (Exception e) {
            log.error("【课程管理】报名名单列表查询异常：{}", e.getMessage());
        }
        return result;
    }

    @Override
    public void listCourseSignStudentExport(OutputStream outputStream, Integer courseId, Integer viewFlag) {
        try {
            List<Map<String, Object>> result = listCourseSignStudent(courseId, viewFlag);
            if (CollectionUtils.isNotEmpty(result)) {
                String[] titles = {"学员姓名", "所属部门", "课程名称", "上课时间/分钟", "所获积分", "报名时间", "状态"};
                String[] obj = {"userName", "deptName", "courseTitle", "completeTime", "integral", "createDate", "state"};
                ExcelUtil.exportExcel("报名名单", titles, obj, result, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                    if (value != null) {
                        if ("state".equals(field)) {
                            if ("0".equals(value.toString())) {
                                cell.setCellValue("正常");
                            } else if ("1".equals(value.toString())) {
                                cell.setCellValue("迟到");
                            } else if ("2".equals(value.toString())) {
                                cell.setCellValue("早退");
                            } else if ("3".equals(value.toString())) {
                                cell.setCellValue("旷课");
                            }
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("【课程管理】报名名单列表导出异常：{}", e.getMessage());
        }
    }

    //校验课程报名数据
    private void validCourse(User user, TrainCourse trainCourse){
        if(user == null){
            throw new QinFeiException(1002, "请先登录");
        }
        if(StringUtils.isEmpty(trainCourse.getTitle())){
            throw new QinFeiException(1002, "课程标题不能为空！");
        }
        if(trainCourse.getCoursePlate() == null){
            throw new QinFeiException(1002, "课程板块不能为空！");
        }
        if(trainCourse.getTrainWay() == null){
            throw new QinFeiException(1002, "培训方式不能为空！");
        }
        if(trainCourse.getSignStartTime() == null){
            throw new QinFeiException(1002, "课程报名开始时间不能为空！");
        }
        if(trainCourse.getSignEndTime() == null){
            throw new QinFeiException(1002, "课程报名结束时间不能为空！");
        }
        if(trainCourse.getTrainStartTime() == null){
            throw new QinFeiException(1002, "课程培训开始时间不能为空！");
        }
        if(trainCourse.getTrainEndTime() == null){
            throw new QinFeiException(1002, "课程培训结束时间不能为空！");
        }
        if(trainCourse.getTrainGrade() == null){
            throw new QinFeiException(1002, "课程学分不能为空！");
        }
        if(trainCourse.getCourseEndTime() == null){
            throw new QinFeiException(1002, "课程反馈结束时间不能为空！");
        }
        //报名开始时间大于报名截止时间
        if(trainCourse.getSignStartTime().compareTo(trainCourse.getSignEndTime()) >= 0){
            throw new QinFeiException(1002, "报名开始时间大于报名截止时间！");
        }
        //培训开始时间小于报名截止时间
        if(trainCourse.getSignEndTime().compareTo(trainCourse.getTrainStartTime()) >= 0){
            throw new QinFeiException(1002, "培训开始时间小于报名截止时间！");
        }
        //培训截止时间小于培训开始时间
        if(trainCourse.getTrainStartTime().compareTo(trainCourse.getTrainEndTime()) >= 0){
            throw new QinFeiException(1002, "培训截止时间小于培训开始时间！");
        }
        //课程反馈截止时间小于培训截止时间
        if(trainCourse.getTrainEndTime().compareTo(trainCourse.getCourseEndTime()) >= 0){
            throw new QinFeiException(1002, "课程反馈截止时间小于培训截止时间！");
        }
        //如果课程范围没有选择，则默认为0
        if(CollectionUtils.isEmpty(trainCourse.getCourseSignRangeList())){
            trainCourse.setCourseEnrollFlag((byte) 0);
        }
        trainCourse.setCreateId(user.getId());
        trainCourse.setUpdateId(user.getId());
        trainCourse.setCompanyCode(user.getCompanyCode());
    }

    //判断是否有课程范围
    private void handleCourseRange(TrainCourse trainCourse){
        //判断是否有课程范围
        if(CollectionUtils.isNotEmpty(trainCourse.getCourseSignRangeList())){
            List<TrainCourseRange> trainCourseRangeList = new ArrayList<>();
            trainCourse.getCourseSignRangeList().forEach(rangeId -> {
                TrainCourseRange trainCourseRange = new TrainCourseRange();
                trainCourseRange.setCourseId(trainCourse.getId());
                trainCourseRange.setCourseEnrollFlag(trainCourse.getCourseEnrollFlag());
                trainCourseRange.setRangeId(rangeId);
                trainCourseRange.setCreateId(trainCourse.getCreateId());
                trainCourseRange.setUpdateId(trainCourse.getUpdateId());
                trainCourseRangeList.add(trainCourseRange);
            });
            trainCourseRangeMapper.saveBatch(trainCourseRangeList);
        }
    }

    //计算课程学分
    private void calCourseIntergal(List<TrainSetting> formulaList, TrainCourse trainCourse){
        if(CollectionUtils.isNotEmpty(formulaList)){
            //计算课程积分
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("a", (trainCourse.getCourseScore() != null ? trainCourse.getCourseScore().toString() : "0"));//课程总评分
            variableMap.put("b", (trainCourse.getCourseScoreNum() != null ? trainCourse.getCourseScoreNum().toString() : "0"));//课程评分人数
            variableMap.put("c", (trainCourse.getCourseLikeNum() != null ? trainCourse.getCourseLikeNum().toString() : "0"));//课程点赞数
            variableMap.put("d", (trainCourse.getCourseCompleteNum() != null ? trainCourse.getCourseCompleteNum().toString() : "0"));//课程总完课人数
            variableMap.put("e", (trainCourse.getCourseVentNum() != null ? trainCourse.getCourseVentNum().toString() : "0"));//课程吐槽人数
            trainCourse.setCourseIntegral(trainSettingService.calFormula(formulaList, variableMap));//计算并赋值
        }
    }

    //更新课程积分
    private void updateCourseIntergal(TrainCourse trainCourse){
        Map<String, Object> param = new HashMap<>();
        param.put("settingModule", "TEACHER_INTEGRAL_RULE");
        param.put("orderFlag", true);
        List<TrainSetting> formulaList = trainSettingService.listTrainSetting(param);
        calCourseIntergal(formulaList, trainCourse);//积分计算
        trainCourseMapper.updateById(trainCourse);
    }

    //更新学员积分
    private void updateStudentIntergal(TrainCourse trainCourse, TrainCourseSign trainCourseSign) {
        Map<String, Object> param = new HashMap<>();
        param.put("settingModule", "STUDENT_INTEGRAL_RULE");
        param.put("orderFlag", true);
        List<TrainSetting> formulaList = trainSettingService.listTrainSetting(param);
        if(CollectionUtils.isNotEmpty(formulaList)){
            //计算课程积分
            Map<String, String> variableMap = new HashMap<>();
            variableMap.put("x", (trainCourse.getTrainGrade() != null ? trainCourse.getTrainGrade().toString() : "0"));//课程学分
            variableMap.put("y", (trainCourseSign.getState() == 0 ? "1" : "0"));//学员完课状态
            //计算阅卷状态的答题卡
            variableMap.put("z", "0");//考试成绩
            trainCourseSign.setIntegral(trainSettingService.calFormula(formulaList, variableMap));
        }else {
            trainCourseSign.setIntegral((float) 0);//默认考试成绩
        }
    }

}
