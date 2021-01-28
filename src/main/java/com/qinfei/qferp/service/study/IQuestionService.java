package com.qinfei.qferp.service.study;

import com.qinfei.qferp.entity.study.Question;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @CalssName: IQuestionService
 * @Description: 题目接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:39
 * @Version: 1.0
 */
public interface IQuestionService {
    String CACHE_KEY = "question";

    //新增题目
    void save(Question question);

    //修改题目状态
    void updateStateById(byte state, int id);

    //修改题目
    void update(Question question);

    //根据参数查询题目
    PageInfo<Question> listQuestion(Map<String, Object> param, Pageable pageable);
}
