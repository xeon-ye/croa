package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.PaperDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @CalssName: PaperMapper
 * @Description: 考试试卷表
 * @Author: Xuxiong
 * @Date: 2020/03/13 0020 15:54
 * @Version: 1.0
 */
public interface PaperDetailMapper extends BaseMapper<PaperDetail, Integer> {
    //新增
    int save(PaperDetail paperDetail);

    //批量新增
    int saveBatch(List<PaperDetail> paperDetailList);

    //修改
    int updateById(PaperDetail paperDetail);

    //批量修改试卷详情状态
    int updateStateByIds(@Param("state") byte state, @Param("updateId") int updateId, @Param("ids") List<Integer> ids);

    //批量更新试卷详情信息
    int batchUpdatePaperDetail(@Param("updateId") int updateId, @Param("paperDetailList") List<PaperDetail> paperDetailList);

    //根据试卷ID获取答题详情
    List<PaperDetail> listPaperDetailByPaperId(@Param("paperId") int paperId, @Param("orderFlag") boolean orderFlag);

    //根据试卷ID获取详情ID集合
    List<Integer> listDetailIdByPaperId(@Param("paperId") int paperId);

    //根据试卷ID获取试卷题目列表
    List<PaperDetail> listPaperQuestionByPaperId(@Param("paperId") int paperId);
}
