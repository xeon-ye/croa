package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.sys.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @CalssName: AnswerCard
 * @Description: 答题卡表
 * @Author: Xuxiong
 * @Date: 2020/3/18 0018 16:15
 * @Version: 1.0
 */
@Table(name = "t_train_answer_card")
@Data
public class AnswerCard implements Serializable {
    @Id
    private Integer id;//主键ID
    private Integer paperId;//试卷ID
    private Float paperGrade;//考试分数
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date examStartTime;//考试开始时间
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date examEndTime;//考试结束时间
    private Integer remainTime;//考试剩余时间，单位：秒
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private String companyCode;//公司代码
    private Byte state;//状态：0-未批改（需要老师阅卷的才默认）、1-已批改（如果是自动阅卷, 则直接默认该批注）、-9-删除

    @Transient
    private Byte autoMarkFlag;//是否可自动阅卷：0-是、1-否，针对于只含有单选、多选、判断题的试卷，系统自动判断设置值，默认否
    @Transient
    private Paper paper; //答题卡对应试卷
    @Transient
    private User user; //答题卡用户信息
    @Transient
    private List<AnswerCardDetail> answerCardDetailList;//答题卡详情列表
}
