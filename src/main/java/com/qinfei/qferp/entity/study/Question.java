package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName: Question
 * @Description: 考试题目表
 * @Author: Xuxiong
 * @Date: 2020/3/18 0018 15:41
 * @Version: 1.0
 */
@Table(name = "t_train_question")
@Data
public class Question implements Serializable {
    @Id
    private Integer id;//主键ID
    private Integer coursePlate;//题目板块，引用课程板块
    private Byte questionType;//题目类型: 1-单选题、2-多选题、3-判断题、4-填空题、5-问答题
    private Byte questionLevel;//题目难度：1-易、2-中、3-难
    private String questionTitle;//题目标题
    private Byte questionAnswerNum;//题目答案个数：针对多选题和多个填空答案，存在多个结果，否则其他默认为1
    private String questionAnswer;//题目正确答案：多选和填空题多个答案，使用“<@>”分隔
    private String questionAnswerDesc;//题目答案项内容，使用“<^_^>”分隔
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private String companyCode;//公司代码
    private Byte state;//状态：0-启用、1-禁用、-9-删除

    @Transient
    private Float questionGrade; //题目分数
    @Transient
    private Integer paperDetailId;//试卷详情ID
}
