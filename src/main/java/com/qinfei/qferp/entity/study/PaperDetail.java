package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName: PaperDetail
 * @Description: 试卷详情表
 * @Author: Xuxiong
 * @Date: 2020/3/18 0018 16:09
 * @Version: 1.0
 */
@Table(name = "t_train_paper_detail")
@Data
public class PaperDetail implements Serializable {
    @Id
    private Integer id;//主键ID
    private Integer paperId;//试卷ID
    private Integer questionId;//题目ID
    private Float questionGrade;//题目分数，试卷所有题目分数加起来 = 试卷总分
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private Byte state;//状态：0-有效、-9-删除

    @Transient
    private Paper paper;//试卷信息
    @Transient
    private Question question; //题目
    @Transient
    private String questionSeq; //创建题目时，前台传进来的题目临时编码
}
