package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @CalssName: Paper
 * @Description: 考试试卷表
 * @Author: Xuxiong
 * @Date: 2020/3/18 0018 15:58
 * @Version: 1.0
 */
@Table(name = "t_train_paper")
@Data
public class Paper implements Serializable {
    @Id
    private Integer id;//主键ID
    private Integer courseId;//课程ID
    private Integer coursePlate;//试卷板块，引用课程板块
    private String paperTitle;//试卷标题
    private Float paperGrade;//试卷总分，有管理员配置，此值在创建试卷的时候获取配置
    private Integer paperTime;//考试时长，单位：分钟
    private Byte paperTimeSetting;//时间设置：0-不固定时间、1-固定时段
    private Byte paperWay;//展现方式：0-逐题展示、1-整卷展示
    private Byte autoMarkFlag;//是否可自动阅卷：0-是、1-否，针对于只含有单选、多选、判断题的试卷，系统自动判断设置值，默认否
    private String paperDesc;//试卷说明
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date paperStartTime;//参与考试开始时间，这个时间之后才能开始考试
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date paperEndTime;//参与考试截止时间，过了这个时间就不能进行考试
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private String companyCode;//公司代码
    private Byte state;//状态：0-启用、1-禁用、-9-删除

    @Transient
    private List<PaperDetail> paperDetailList;//试卷题目
    @Transient
    private String courseTitle;//课程标题
}
