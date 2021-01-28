package com.qinfei.qferp.service.study;

import com.qinfei.qferp.entity.study.AnswerCard;
import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.Map;

/**
 * @CalssName: IAnswerCardService
 * @Description: 答题卡接口
 * @Author: Xuxiong
 * @Date: 2020/3/19 0019 14:40
 * @Version: 1.0
 */
public interface IAnswerCardService {
    String CACHE_KEY = "answerCard";

    //分页获取试卷答题卡列表
    PageInfo<AnswerCard> listAnswerCard(Map<String, Object> param, Pageable pageable);

    //(学员)新增/编辑答题卡：提交试卷时，创建答题卡，以及答题详情
    void save(AnswerCard answerCard);

    //阅卷
    void mark(AnswerCard answerCard);

    //根据试卷ID和答题人获取答题卡信息
    AnswerCard getAnswerCardByPaperId(int paperId, Integer studentId, boolean examFlag);

    //考生答题卡列表导出
    void trainPaperAnswerExport(OutputStream outputStream, Integer paperId, String courseTitle, String coursePlate);
}
