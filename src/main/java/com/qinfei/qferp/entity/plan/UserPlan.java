package com.qinfei.qferp.entity.plan;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.sys.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @CalssName UserGroup
 * @Description 用户计划表
 * @Author xuxiong
 * @Date 2019/8/9 0009 17:51
 * @Version 1.0
 */
@Table(name = "t_user_plan")
@Getter
@Setter
@ToString
public class UserPlan implements Serializable {
    private Integer id;  //主键ID
    private BigDecimal perfoSummary;  //业绩（元)-总结
    private BigDecimal profitSummary;  //利润业绩（元）-总结
    private Integer yxCustomSummary;  //有效客户量-总结
    private Integer xcjCustomSummary;  //新成交客户量-总结
    private Integer gjCustomSummary;  //跟进客户数量-总结
    private Integer tzyCustomSummary;  //推资源客户数量-总结
    private BigDecimal perfoPlan;  //业绩（元)-计划
    private BigDecimal profitPlan;  //利润业绩（元）-计划
    private Integer yxCustomPlan;  //有效客户量-计划
    private Integer xcjCustomPlan;  //新成交客户量-计划
    private Integer gjCustomPlan;  //跟进客户数量-计划
    private Integer tzyCustomPlan;  //推资源客户数量-计划
    private Integer isOvertime;  //是否超时登记：0-未超时、1-超时，超时的登记计划名称和时间字体标红
    private String summaryResult;  //昨天计划总结结果：0和1组成的字符串，0-完成，1-未完成，按顺序依次对应“业绩（元)、利润业绩（元）、有效客户量、新成交客户量、跟进客户数量、推资源客户数量”，每次登记计划时计算
    private Integer summaryType;  //总结类型：0-昨日总结、1-当天总结，用于新增计划时，新增一条昨天计划和总结的记录
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;  //创建时间
    private Integer createId;  //计划创建者ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;  //更新时间
    private Integer updateId;  //更新人
    private Integer state; //状态：0-正常、-9-删除

    @Transient
    private User user;  //用户信息
    @Transient
    private float rxbSummary; //人效比
    @Transient
    private float rjkhSummary; //人均开发有效客户数
}
