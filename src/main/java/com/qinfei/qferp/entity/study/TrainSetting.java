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
 * @CalssName: Question
 * @Description: 培训设置表
 * @Author: Xuxiong
 * @Date: 2020/3/18 0018 15:41
 * @Version: 1.0
 */
@Table(name = "t_train_setting")
@Data
public class TrainSetting implements Serializable {
    @Id
    private Integer id;//主键ID
    private String settingModule;//设置模块：TRAIN_WAY-培训方式、GOOD_AT_FIELD-擅长领域、COURSE_PLATE-课程板块、TEACHER_LEVEL-讲师等级、TEACHER_INTEGRAL_RULE-讲师积分规则、STUDENT_INTEGRAL_RULE-学员积分规则、UP_RULE-升级规则、PAPER_GRADE-试卷总分数
    private Integer parentId;//父级ID，针对于定制规则的公式，会把除数保存起来
    private String settingValue;//设置的值，下拉列表：选项名称、规则：数学公式
    private Float ruleValue;//针对于讲师等级，讲师等级：根据升级规则公司计算升级所需积分值
    private Integer settingLevel;//设置值层级，针对于下拉列表值，讲师等级时，该字段用于级别，配合升级规则计算所需积分，默认从0开始
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private String companyCode;//公司代码
    private Byte state;//状态：0-启用、1-禁用、-9-删除

    @Transient
    private List<String> settingValueList; //前台传过来的参数
    @Transient
    private User user;//创建人对象
}
