package com.qinfei.qferp.service.impl.study;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.*;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.study.*;
import com.qinfei.qferp.service.study.IAnswerCardService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: AnswerCardService
 * @Description: 答题卡接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:48
 * @Version: 1.0
 */
@Service
@Slf4j
public class AnswerCardService implements IAnswerCardService {
    @Autowired
    private AnswerCardMapper answerCardMapper;
    @Autowired
    private AnswerCardDetailMapper answerCardDetailMapper;
    @Autowired
    private PaperDetailMapper paperDetailMapper;
    @Autowired
    private TrainCourseMapper trainCourseMapper;
    @Autowired
    private TrainCourseSignMapper trainCourseSignMapper;
    @Autowired
    private ITrainStudentService trainStudentService;

    @Override
    public PageInfo<AnswerCard> listAnswerCard(Map<String, Object> param, Pageable pageable) {
        List<AnswerCard> paperList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            paperList = answerCardMapper.listAnswerCardByParam(param);
        }
        return new PageInfo<>(paperList);
    }

    @Transactional
    @Override
    public void save(AnswerCard answerCard) {
        try{
            User user = AppUtil.getUser();
            //1、校验
            validAnswerCard(answerCard, user);
            //2、获取试卷题目
            List<PaperDetail> paperDetailList = paperDetailMapper.listPaperQuestionByPaperId(answerCard.getPaperId());
            Map<Integer, PaperDetail> paperDetailMap = new HashMap<>();
            for(PaperDetail paperDetail : paperDetailList){
                paperDetailMap.put(paperDetail.getQuestionId(), paperDetail);
            }
            //3、判断更新还是新增
            if(answerCard.getId() == null){
                answerCardMapper.save(answerCard);
                //4、答题详情关联答题卡ID
                answerCard.getAnswerCardDetailList().forEach(answerCardDetail -> {
                    //处理新增的答题卡详情
                    handleAddAnswerCardDetail(answerCard, answerCardDetail, paperDetailMap, user);
                });
                answerCardDetailMapper.saveBatch(answerCard.getAnswerCardDetailList());//批量入库
                //5、更新答题卡总分
                answerCardMapper.updateGradeById((answerCard.getPaperGrade() == null ? 0f : answerCard.getPaperGrade()), user.getId(), answerCard.getId());
            }else {
                //清空答题卡总分，下面进行重新计算
                answerCard.setPaperGrade(0f);
                //4、获取上次答题详情，缓存已经答题目， 并计算答题卡总分
                List<AnswerCardDetail> answerCardDetailList = answerCardDetailMapper.listAnswerCardDetailByCardId(answerCard.getId());
                Map<Integer, AnswerCardDetail> answerCardDetailMap = new HashMap<>();//原答题卡详情缓存
                if(CollectionUtils.isNotEmpty(answerCardDetailList)){
                    for(AnswerCardDetail answerCardDetail : answerCardDetailList){
                        answerCardDetailMap.put(answerCardDetail.getQuestionId(), answerCardDetail);
                        answerCard.setPaperGrade(answerCard.getPaperGrade()+(answerCardDetail.getTeacherGrade() == null ? 0f : answerCardDetail.getTeacherGrade()));//计算答题卡总分
                    }
                }

                //5、分离出已答题和新答题
                List<AnswerCardDetail> addAnswerCardDetailList = new ArrayList<>();//本次新增的详情
                List<AnswerCardDetail> editAnswerCardDetailList = new ArrayList<>();//本次编辑的详情
                answerCard.getAnswerCardDetailList().forEach(answerCardDetail -> {
                    //判断是否编辑的答题详情，需要重新计算分数
                    if(answerCardDetailMap.containsKey(answerCardDetail.getQuestionId())){
                        //处理编辑的答题卡详情
                        handleEditAnswerCardDetail(answerCard, answerCardDetail, answerCardDetailMap, editAnswerCardDetailList, paperDetailMap);
                    }else {
                        //处理新增的答题卡详情
                        handleAddAnswerCardDetail(answerCard, answerCardDetail, paperDetailMap, user);
                        addAnswerCardDetailList.add(answerCardDetail);
                    }
                });

                //6、数据处理
                if(CollectionUtils.isNotEmpty(addAnswerCardDetailList)){
                    answerCardDetailMapper.saveBatch(addAnswerCardDetailList);//批量入库
                }
                if(CollectionUtils.isNotEmpty(editAnswerCardDetailList)){
                    answerCardDetailMapper.batchUpdateAnswerCardDetail(user.getId(), editAnswerCardDetailList);//批量更新
                }
                //7、更新答题卡总分、剩余时间
                answerCardMapper.updateById(answerCard);
            }

            //已批改的试卷进行学员积分计算，能自动阅卷
            if(answerCard.getState() == 1){
                updateStudentIntegal(answerCard);//更新学员积分
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "提交试卷异常！");
        }
    }

    @Transactional
    @Override
    public void mark(AnswerCard answerCard) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            if(answerCard.getId() == null){
                throw new QinFeiException(1002, "答题卡不存在！");
            }
            if(CollectionUtils.isEmpty(answerCard.getAnswerCardDetailList())){
                throw new QinFeiException(1002, "答题卡详情不存在！");
            }
            answerCard.setPaperGrade(0f);
            //1、拆分出新增的答题卡详情 和 编辑的答题卡详情
            Map<Integer, AnswerCardDetail> answerCardDetailMap = new HashMap<>();//答题卡详情缓存
            List<AnswerCardDetail> addAnswerCardDetailList = new ArrayList<>();//信息
            List<AnswerCardDetail> editAnswerCardDetailList = new ArrayList<>();//编辑，逐题答题可能会产生这个情况
            for(AnswerCardDetail answerCardDetail : answerCard.getAnswerCardDetailList()){
                if(answerCardDetail.getId() == null){
                    addAnswerCardDetailList.add(answerCardDetail);
                }else {
                    answerCardDetailMap.put(answerCardDetail.getId(), answerCardDetail);
                    editAnswerCardDetailList.add(answerCardDetail);
                }
            }
            //2、获取上次答题详情，缓存已经答题目， 并计算答题卡总分
            List<AnswerCardDetail> answerCardDetailList = answerCardDetailMapper.listAnswerCardDetailByCardId(answerCard.getId());
            //3、统计总分数
            if(CollectionUtils.isNotEmpty(answerCardDetailList)){
                for(AnswerCardDetail answerCardDetail : answerCardDetailList){
                    //如果包含，则使用编辑的分数计算
                    if(answerCardDetailMap.containsKey(answerCardDetail.getId())){
                        answerCard.setPaperGrade(answerCard.getPaperGrade()+(answerCardDetailMap.get(answerCardDetail.getId()).getTeacherGrade() == null ? 0f : answerCardDetailMap.get(answerCardDetail.getId()).getTeacherGrade()));//计算答题卡总分
                    }else {
                        answerCard.setPaperGrade(answerCard.getPaperGrade()+(answerCardDetail.getTeacherGrade() == null ? 0f : answerCardDetail.getTeacherGrade()));//计算答题卡总分
                    }
                }
            }
            //如果有新增的，则统计新增的分数
            if(CollectionUtils.isNotEmpty(addAnswerCardDetailList)){
                for(AnswerCardDetail answerCardDetail : answerCardDetailList){
                    answerCardDetail.setCreateId(user.getId());
                    answerCardDetail.setUpdateId(user.getId());
                    answerCardDetail.setAnswerCardId(answerCard.getId());
                    answerCard.setPaperGrade(answerCard.getPaperGrade()+(answerCardDetail.getTeacherGrade() == null ? 0f : answerCardDetail.getTeacherGrade()));//计算答题卡总分
                }
                answerCardDetailMapper.saveBatch(addAnswerCardDetailList);
            }
            if(CollectionUtils.isNotEmpty(editAnswerCardDetailList)){
                answerCardDetailMapper.batchUpdateAnswerCardDetail(user.getId(), editAnswerCardDetailList);
            }
            //状态：0-未批改（需要老师阅卷的才默认）、1-已批改（如果是自动阅卷, 则直接默认该批注）、-9-删除
            answerCard.setState((byte) 1);
            answerCard.setUpdateId(user.getId());
            answerCardMapper.updateById(answerCard);

            updateStudentIntegal(answerCard);//更新学员积分
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "阅卷异常！");
        }
    }

    @Override
    public AnswerCard getAnswerCardByPaperId(int paperId, Integer studentId, boolean examFlag) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            int userId = studentId == null ? user.getId() : studentId; //如果有学员ID，则查询，否则默认自己的答题卡
            List<AnswerCard> answerCardList = answerCardMapper.listAnswerCardByPaperId(paperId, userId);
            if(CollectionUtils.isNotEmpty(answerCardList)){
                AnswerCard answerCard = answerCardList.get(0);
                //如果是学员进入测试，将数据的答案清空，不然别人考试F12可以获取到
                if(examFlag && CollectionUtils.isNotEmpty(answerCard.getAnswerCardDetailList())){
                    answerCard.getAnswerCardDetailList().forEach(answerCardDetail -> {
                        answerCardDetail.setTeacherGrade(null);
                        answerCardDetail.setTeacherRemark("");
                    });
                }
                return answerCard;
            }else {
                return null;
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "获取答题卡信息异常！");
        }
    }

    @Override
    public void trainPaperAnswerExport(OutputStream outputStream, Integer paperId, String courseTitle, String coursePlate) {
        try {
            List<Map<String, Object>> result = answerCardMapper.listAnswerCardByPaper(paperId);
            if (CollectionUtils.isNotEmpty(result)) {
                for (Map<String, Object> data : result) {
                    data.put("courseTitle", courseTitle);
                    data.put("coursePlate", coursePlate);
                    int paperTime = Integer.parseInt(String.valueOf(data.get("paperTime")));
                    int remainTime = Integer.parseInt(String.valueOf(data.get("remainTime"))) / 60;
                    data.put("userTime", paperTime - remainTime);
                }
                String[] titles = {"考生姓名", "试卷标题", "所属课程", "试卷板块", "展现方式", "考试时长(分钟)", "学员用时(分钟)", "考试分数"};
                String[] obj = {"name", "paperTitle", "courseTitle", "coursePlate", "paperWay", "paperTime", "userTime", "paperGrade"};
                ExcelUtil.exportExcel("报名名单", titles, obj, result, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
                    if (value != null) {
                        if ("paperWay".equals(field)) {
                            if ("1".equals(value.toString())) {
                                cell.setCellValue("整卷展示");
                            } else {
                                cell.setCellValue("逐题展示");
                            }
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("【试卷管理】考生答题卡列表导出异常：{}", e.getMessage());
        }
    }

    //校验答题卡
    private void validAnswerCard(AnswerCard answerCard, User user){
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(answerCard.getPaperId() == null){
            throw new QinFeiException(1002, "试卷不存在！");
        }
        if(CollectionUtils.isEmpty(answerCard.getAnswerCardDetailList())){
            throw new QinFeiException(1002, "试卷不存在题目，提交无效！");
        }
        //1、判断试卷是否可自动批改,是否可自动阅卷：0-是、1-否，决定答题卡状态：0-未批改（需要老师阅卷的才默认）、1-已批改（如果是自动阅卷, 则直接默认该批注）
        if(answerCard.getAutoMarkFlag() == 0){
            answerCard.setState((byte) 1);
        }else {
            answerCard.setState((byte) 0);
        }
        answerCard.setCreateId(user.getId());
        answerCard.setUpdateId(user.getId());
        answerCard.setCompanyCode(user.getCompanyCode());
    }

    //处理新增的答题卡详情
    private void handleAddAnswerCardDetail(AnswerCard answerCard, AnswerCardDetail answerCardDetail, Map<Integer, PaperDetail> paperDetailMap, User user){
        answerCardDetail.setAnswerCardId(answerCard.getId());
        answerCardDetail.setCreateId(user.getId());
        answerCardDetail.setUpdateId(user.getId());
        //对比答案，1-单项选择题、2-多项选择题、3-判断选择题，可直接得出答案，计算分数
        PaperDetail paperDetail = paperDetailMap.get(answerCardDetail.getQuestionId());
        Question question = paperDetail.getQuestion();
        if((question.getQuestionType() == 1 || question.getQuestionType() == 2 || question.getQuestionType() == 3) && question.getQuestionAnswer().equals(answerCardDetail.getStudentAnswer())){
            answerCardDetail.setTeacherGrade(paperDetail.getQuestionGrade());
            answerCard.setPaperGrade((answerCard.getPaperGrade() == null ? 0f : answerCard.getPaperGrade()) + paperDetail.getQuestionGrade());//计算答题卡总分
        }
    }

    //处理编辑的答题卡详情
    private void handleEditAnswerCardDetail(AnswerCard answerCard, AnswerCardDetail answerCardDetail, Map<Integer, AnswerCardDetail> answerCardDetailMap, List<AnswerCardDetail> editAnswerCardDetailList, Map<Integer, PaperDetail> paperDetailMap){
        //对比当前答案 和  上次提交答案是否一致，一致则不处理
        if(!answerCardDetail.getStudentAnswer().equals(answerCardDetailMap.get(answerCardDetail.getQuestionId()).getStudentAnswer())){
            answerCardDetail.setId(answerCardDetailMap.get(answerCardDetail.getQuestionId()).getId());//设置主键ID
            editAnswerCardDetailList.add(answerCardDetail);
            PaperDetail paperDetail = paperDetailMap.get(answerCardDetail.getQuestionId());
            Question question = paperDetail.getQuestion();
            //6、对比答案，1-单项选择题、2-多项选择题、3-判断选择题，可直接得出答案，计算分数，先减去原来的分数，再重新计算总分
            if((question.getQuestionType() == 1 || question.getQuestionType() == 2 || question.getQuestionType() == 3)){
                answerCard.setPaperGrade(answerCard.getPaperGrade()-(answerCardDetailMap.get(answerCardDetail.getQuestionId()).getTeacherGrade() == null ? 0f : answerCardDetailMap.get(answerCardDetail.getQuestionId()).getTeacherGrade()));//减去原来答案的得分
                //判断答案是否正确
                if(question.getQuestionAnswer().equals(answerCardDetail.getStudentAnswer())){
                    answerCardDetail.setTeacherGrade(paperDetail.getQuestionGrade());
                    answerCard.setPaperGrade(answerCard.getPaperGrade()+paperDetail.getQuestionGrade());//计算答题卡总分
                }else {
                    answerCardDetail.setTeacherGrade(0f);
                }
            }

        }
    }

    //计算学员积分
    private void updateStudentIntegal(AnswerCard answerCard){
        if(answerCard != null){
            //计算学员积分
            TrainCourse trainCourse = trainCourseMapper.getCourseByAnswerdCardId(answerCard.getId());
            if(trainCourse == null){
                throw new QinFeiException(1002, "课程不存在！");
            }
            TrainCourseSign trainCourseSign = trainCourseSignMapper.getCourseSignByUserId(trainCourse.getId(), answerCard.getCreateId());
            if(trainCourseSign == null){
                throw new QinFeiException(1002, "学员报名信息不存在！");
            }
            trainStudentService.updateStudentIntergal(trainCourse, trainCourseSign);
        }
    }
}
