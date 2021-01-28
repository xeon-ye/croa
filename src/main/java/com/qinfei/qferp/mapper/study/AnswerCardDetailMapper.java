package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.AnswerCardDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @CalssName: QuestionMapper
 * @Description: 答题卡详情表
 * @Author: Xuxiong
 * @Date: 2020/03/13 0020 15:54
 * @Version: 1.0
 */
public interface AnswerCardDetailMapper extends BaseMapper<AnswerCardDetail, Integer> {
    //新增
    int save(AnswerCardDetail answerCardDetail);

    //批量新增
    int saveBatch(List<AnswerCardDetail> answerCardDetailList);

    //修改
    int updateById(AnswerCardDetail answerCardDetail);

    //批量更新答题卡详情信息
    int batchUpdateAnswerCardDetail(@Param("updateId") int updateId,  @Param("answerCardDetailList") List<AnswerCardDetail> answerCardDetailList);

    //根据答题卡ID获取答题详情
    List<AnswerCardDetail> listAnswerCardDetailByCardId(@Param("answerCardId") int answerCardId);

}
