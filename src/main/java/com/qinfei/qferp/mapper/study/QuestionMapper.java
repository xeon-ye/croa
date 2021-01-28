package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: QuestionMapper
 * @Description: 考试题目
 * @Author: Xuxiong
 * @Date: 2020/03/13 0020 15:54
 * @Version: 1.0
 */
public interface QuestionMapper extends BaseMapper<Question, Integer> {
    //新增
    int save(Question question);

    //批量新增
    int saveBatch(List<Question> questionList);

    //根据题目状态
    int updateStateById(@Param("state") byte state, @Param("updateId") int updateId, @Param("id") int id);

    //修改
    int updateById(Question question);

    //批量更新题目信息
    int batchUpdateQuestion(List<Question> questionList);

    //根据题目ID获取题目
    List<Question> listQuestionByIds(@Param("ids") List<Integer> ids);

    //根据参数获取题目列表
    List<Question> listQuestionByParam(Map<String, Object> param);

}
