package com.qinfei.qferp.service.impl.study;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.Paper;
import com.qinfei.qferp.entity.study.PaperDetail;
import com.qinfei.qferp.entity.study.Question;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.study.AnswerCardMapper;
import com.qinfei.qferp.mapper.study.PaperDetailMapper;
import com.qinfei.qferp.mapper.study.PaperMapper;
import com.qinfei.qferp.mapper.study.QuestionMapper;
import com.qinfei.qferp.service.study.IPaperService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @CalssName: PaperService
 * @Description: 试卷接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:43
 * @Version: 1.0
 */
@Service
@Slf4j
public class PaperService implements IPaperService {
    @Autowired
    private PaperMapper paperMapper;
    @Autowired
    private PaperDetailMapper paperDetailMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private AnswerCardMapper answerCardMapper;

    @Transactional
    @Override
    public void save(Paper paper) {
        try{
            User user = AppUtil.getUser();
            validPaper(paper, user); //数据校验
            paperMapper.save(paper);
            //试卷对应题目信息处理
            if(CollectionUtils.isNotEmpty(paper.getPaperDetailList())){
                saveQuestion(paper, paper.getPaperDetailList(), null, null, null); //入库新增题目
                paperDetailMapper.saveBatch(paper.getPaperDetailList()); //入库试卷详情
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "新增试卷异常！");
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
            paperMapper.updateStateById(state, user.getId(), id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "修改试卷状态异常！");
        }
    }

    @Transactional
    @Override
    public void update(Paper paper) {
        try{
            User user = AppUtil.getUser();
            validPaper(paper, user); //数据校验
            //判断是否有学员进行考试，如果有的话，则不允许编辑试卷了
            if(answerCardMapper.getAnswerCardCountByPaperId(paper.getId()) > 0){
                throw new QinFeiException(1002, "已有学员参加考试，不能进行试卷编辑！");
            }
            paperMapper.updateById(paper);
            List<Integer> paperDetailIdList = paperDetailMapper.listDetailIdByPaperId(paper.getId());//获取修改前的试卷详情ID集合
            //如果有集合，得去判断处理新增、编辑、删除的详情，没有集合，说明都被删除了
            if(CollectionUtils.isNotEmpty(paper.getPaperDetailList())){
                //区分新增的题目  以及  存在的题目
                List<PaperDetail> addPaperDetailList = new ArrayList<>();
                List<PaperDetail> editPaperDetailList = new ArrayList<>();
                List<Integer> tempPaperDetailIdList = null;
                if(CollectionUtils.isNotEmpty(paperDetailIdList)){
                    tempPaperDetailIdList = new ArrayList<>();
                }
                saveQuestion(paper, paper.getPaperDetailList(), addPaperDetailList, editPaperDetailList, tempPaperDetailIdList); //入库新增题目
                //保存新增的试卷详情
                if(CollectionUtils.isNotEmpty(addPaperDetailList)){
                    paperDetailMapper.saveBatch(addPaperDetailList);
                }
                //编辑修改的试卷详情
                if(CollectionUtils.isNotEmpty(editPaperDetailList)){
                    paperDetailMapper.batchUpdatePaperDetail(user.getId(), editPaperDetailList);
                }
                //删除移除的试卷详情
                if(CollectionUtils.isNotEmpty(paperDetailIdList) && CollectionUtils.isNotEmpty(tempPaperDetailIdList)){
                    //先求集合交集
                    tempPaperDetailIdList.retainAll(paperDetailIdList);
                    //用原ID集合减去交集，得到删除的ID集合
                    paperDetailIdList.removeAll(tempPaperDetailIdList);
                }
            }
            //移除掉已经删除的试卷题目关系
            if(CollectionUtils.isNotEmpty(paperDetailIdList)){
                paperDetailMapper.updateStateByIds((byte) -9, user.getId(), paperDetailIdList);
            }
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "修改试卷异常！");
        }
    }

    @Override
    public int getCountByParam(Map<String, Object> param) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            result = paperMapper.getCountByParam(param);
        }
        return result;
    }

    @Override
    public PageInfo<Paper> listPaper(Map<String, Object> param, Pageable pageable) {
        List<Paper> paperList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            paperList = paperMapper.listPaperByParam(param);
        }
        return new PageInfo<>(paperList);
    }

    @Override
    public  Map<String, Object> getPaperDetailById(int paperId, boolean orderFlag, boolean examFlag) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            Map<String, Object> result = null;
            List<PaperDetail> paperDetailList = paperDetailMapper.listPaperDetailByPaperId(paperId, orderFlag);
            if(CollectionUtils.isNotEmpty(paperDetailList)){
                result = new HashMap<>();
                for (PaperDetail paperDetail : paperDetailList){
                    if(!result.containsKey("paper")){
                        paperDetail.getPaper().setId(paperDetail.getPaperId());
                        result.put("paper", paperDetail.getPaper());
                    }
                    String key = String.valueOf(paperDetail.getQuestion().getQuestionType());
                    if(!result.containsKey(key)){
                        result.put(key, new ArrayList<>());
                    }
                    paperDetail.getQuestion().setQuestionGrade(paperDetail.getQuestionGrade());
                    paperDetail.getQuestion().setId(paperDetail.getQuestionId());
                    paperDetail.getQuestion().setPaperDetailId(paperDetail.getId());
                    //如果是学员进入测试，将数据的答案清空，不然别人考试F12可以获取到
                    if(examFlag){
                        //单选题、多选题、判断题答案
                        paperDetail.getQuestion().setQuestionAnswer("");
                        //针对多选题，不然别人知道选项答案个数
                        if(paperDetail.getQuestion().getQuestionType() == 2){
                            paperDetail.getQuestion().setQuestionAnswerNum(null);
                        }
                        //填空题，答案存在标题中
                        if(paperDetail.getQuestion().getQuestionType() == 4){
                            paperDetail.getQuestion().setQuestionTitle("");
                        }
                        //问答题答案存在描述中
                        if(paperDetail.getQuestion().getQuestionType() == 5){
                            paperDetail.getQuestion().setQuestionAnswerDesc("");
                        }
                    }
                    ((List)result.get(key)).add( paperDetail.getQuestion());
                }
            }
            return result;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "获取试卷详细信息异常！");
        }
    }

    @Override
    public int getUserExamTotal(Map<String, Object> param) {
        int result = 0;
        User user = AppUtil.getUser();
        if(user != null){
            param.put("userId", user.getId());
            result = paperMapper.getUserExamTotal(param);
        }
        return result;
    }

    @Override
    public PageInfo<Map<String, Object>> listUserExam(Map<String, Object> param, Pageable pageable) {
        List<Map<String, Object>> paperList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            param.put("userId", user.getId());
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            paperList = paperMapper.listUserExam(param);
        }
        return new PageInfo<>(paperList);
    }

    //校验新增
    private void validPaper(Paper paper, User user){
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(paper.getCourseId() == null){
            throw new QinFeiException(1002, "课程不能为空！");
        }
        if(paper.getCoursePlate() == null){
            throw new QinFeiException(1002, "所属板块不能为空！");
        }
        if(StringUtils.isEmpty(paper.getPaperTitle())){
            throw new QinFeiException(1002, "试卷名称不能为空！");
        }
        if(paper.getPaperTimeSetting() == 1){
            if(paper.getPaperStartTime() == null){
                throw new QinFeiException(1002, "请填写参加考试的开始时间！");
            }
            if(paper.getPaperEndTime() != null && paper.getPaperStartTime().compareTo(paper.getPaperEndTime()) >= 0){
                throw new QinFeiException(1002, "参加考试的开始时间大于结束时间！");
            }
        }
        if(paper.getPaperTime() == null){
           paper.setPaperTime(100); //不输入默认100分钟
        }
        paper.setCreateId(user.getId());
        paper.setUpdateId(user.getId());
        paper.setCompanyCode(user.getCompanyCode());
    }

    //新增题目
    private void saveQuestion(Paper paper, List<PaperDetail> paperDetailList, List<PaperDetail> addPaperDetailList,List<PaperDetail> editPaperDetailList, List<Integer> tempPaperDetailIdList){
        //1、区分出题库的题目 和 新建的题目（需要入库），如果PaperDetail对象questionId属性有值，则为题库题目，否则为新建题目信息在question属性中
        Map<String, Question> paperDetailMap = new HashMap<>(); //缓存所有新添加的题目
        paperDetailList.forEach(paperDetail -> {
            //没有题目ID，则一定是新增的题目，也一定没有试卷详细信息，需要新增
            if(paperDetail.getQuestionId() == null){
                paperDetail.getQuestion().setCoursePlate(paper.getCoursePlate());//新增的题目，所属板块继承试卷的所属板块
                paperDetail.setCreateId(paper.getUpdateId());
                paperDetail.setUpdateId(paper.getUpdateId());
                paperDetail.getQuestion().setCompanyCode(paper.getCompanyCode());
                paperDetail.getQuestion().setCreateId(paper.getUpdateId());
                paperDetail.getQuestion().setUpdateId(paper.getUpdateId());
                if(paperDetail.getQuestion().getQuestionType() == 5){ //问答题，questionAnswer字段没赋值
                    paperDetail.getQuestion().setQuestionAnswer(paperDetail.getQuestion().getQuestionAnswerDesc());
                }
                paperDetail.getQuestion().setQuestionAnswerNum((byte) paperDetail.getQuestion().getQuestionAnswer().split("<\\^_\\^>").length);
                paperDetailMap.put(paperDetail.getQuestionSeq(), paperDetail.getQuestion());
                if(addPaperDetailList != null){
                    addPaperDetailList.add(paperDetail);
                }
            }else {
                //针对编辑试卷：还得判断paperDetail是否有试卷详情ID，有则说明是已经存在关系，需要编辑，否则为题库选择的题目，需要创建试卷详情记录
                if(paperDetail.getId() == null){
                    if(addPaperDetailList != null){
                        addPaperDetailList.add(paperDetail);
                    }
                }else {
                    //保存详情信息
                    if(editPaperDetailList != null){
                        editPaperDetailList.add(paperDetail);
                    }
                    //保存存在的详情ID
                    if(tempPaperDetailIdList != null){
                        tempPaperDetailIdList.add(paperDetail.getId());
                    }
                }
            }
            paperDetail.setPaperId(paper.getId());
        });
        //2、新题目入库
        if(paperDetailMap.size() > 0){
            questionMapper.saveBatch(paperDetailMap.values().stream().collect(Collectors.toList()));
            for(PaperDetail paperDetail : paperDetailList){
                //试卷详情关联新题目ID
                if(paperDetailMap.containsKey(paperDetail.getQuestionSeq())){
                    paperDetail.setQuestionId(paperDetailMap.get(paperDetail.getQuestionSeq()).getId());
                }
            }
        }
    }
}
