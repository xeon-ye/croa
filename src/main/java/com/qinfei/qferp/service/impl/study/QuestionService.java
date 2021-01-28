package com.qinfei.qferp.service.impl.study;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.study.Question;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.study.QuestionMapper;
import com.qinfei.qferp.service.study.IQuestionService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @CalssName: QuestionService
 * @Description: 题目接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:44
 * @Version: 1.0
 */
@Service
@Slf4j
public class QuestionService implements IQuestionService {
    @Autowired
    private QuestionMapper questionMapper;

    @Transactional
    @Override
    public void save(Question question) {
        try{
            User user = AppUtil.getUser();
            validQuestion(question, user);//数据校验
            questionMapper.save(question);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "新增题目异常！");
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
            questionMapper.updateStateById(state, user.getId(), id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "修改题目状态异常！");
        }
    }

    @Transactional
    @Override
    public void update(Question question) {
        try{
            User user = AppUtil.getUser();
            validQuestion(question, user);//数据校验
            questionMapper.updateById(question);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "修改题目异常！");
        }
    }

    @Override
    public PageInfo<Question> listQuestion(Map<String, Object> param, Pageable pageable) {
        List<Question> paperList = new ArrayList<>();
        User user = AppUtil.getUser();
        if(user != null){
            param.put("companyCode", user.getCompanyCode());
            if(param.get("excludeIds") != null && StringUtils.isNotEmpty(String.valueOf(param.get("excludeIds")))){
                param.put("excludeIds", Arrays.asList(String.valueOf(param.get("excludeIds")).split(",")));
            }
            PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
            paperList = questionMapper.listQuestionByParam(param);
        }
        return new PageInfo<>(paperList);
    }

    //题目校验
    private void validQuestion(Question question, User user){
        if(user == null){
            throw new QinFeiException(1002, "请先登录！");
        }
        if(question.getCoursePlate() == null){
            throw new QinFeiException(1002, "课程板块不能为空！");
        }
        if(question.getQuestionType() == null){
            throw new QinFeiException(1002, "题目类型不能为空！");
        }
        if(StringUtils.isEmpty(question.getQuestionTitle())){
            throw new QinFeiException(1002, "题目标题不能为空！");
        }
        if(StringUtils.isEmpty(question.getQuestionAnswer())){
            throw new QinFeiException(1002, "题目答案不能为空！");
        }
        question.setQuestionAnswerNum((byte) question.getQuestionAnswer().split("<^_^>").length);
        question.setCreateId(user.getId());
        question.setUpdateId(user.getId());
    }
}
