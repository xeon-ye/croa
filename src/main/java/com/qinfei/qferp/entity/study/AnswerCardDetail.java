package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName: AnswerCardDetail
 * @Description: 答题卡详情表
 * @Author: Xuxiong
 * @Date: 2020/3/18 0018 16:21
 * @Version: 1.0
 */
@Table(name = "t_train_answer_card_detail")
@Data
public class AnswerCardDetail implements Serializable {
    @Id
    private Integer id;//主键ID
    private Integer answerCardId;//答题卡ID
    private Integer questionId;//题目ID
    private String studentAnswer;//考生答案：多选和填空题多个答案，使用“<@>”分隔
    private Float teacherGrade;//讲师分数 或者 自动阅卷的单选、多选、判断题分数
    private String teacherRemark;//批阅信息
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private Byte state;//状态：0-有效、-9-删除

    @Transient
    private Question question; //答题详情对应的题目
}
