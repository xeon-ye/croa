package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.AnswerCard;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @CalssName: QuestionMapper
 * @Description: 答题卡表
 * @Author: Xuxiong
 * @Date: 2020/03/13 0020 15:54
 * @Version: 1.0
 */
public interface AnswerCardMapper extends BaseMapper<AnswerCard, Integer> {
    //新增
    int save(AnswerCard answerCard);

    //更新
    int updateById(AnswerCard answerCard);

    //更新答题卡状态
    int updateStateById(@Param("state") byte state, @Param("updateId") int updateId, @Param("id") int id);

    //更新答题卡分数
    int updateGradeById(@Param("paperGrade") float paperGrade, @Param("updateId") int updateId, @Param("id") int id);

    //删除学员指定课程的所有考试记录，学员取消报名使用
    int delStudentCourseAnswerCard(@Param("courseId") int courseId, @Param("createId") int createId);

    //删除指定课程的所有考试记录，管理员取消课程使用
    int delCourseAnswerCard(@Param("courseId") int courseId);

    //根据参数获取答题卡列表
    List<AnswerCard> listAnswerCardByParam(Map<String, Object> param);

    //根据参数获取答题卡列表
    List<Map<String, Object>> listAnswerCardByPaper(@Param("paperId") Integer paperId);

    //根据试卷ID获取答题卡
    List<AnswerCard> listAnswerCardByPaperId(@Param("paperId") int paperId, @Param("createId") int createId);

    //根据课程和学员查询学员该课程所有试卷成绩， 仅查询已阅卷的答题卡
    Float getGradeByCourseIdAndUserId(@Param("courseId") int courseId, @Param("createId") int createId);

    //根据试卷ID获取答题卡数量
    int getAnswerCardCountByPaperId(@Param("paperId") int paperId);

    //根据学员ID集合获取测试次数
    List<Map<String, Object>> listExamNumByUserId(@Param("userIds") List<Integer> userIds);

}
