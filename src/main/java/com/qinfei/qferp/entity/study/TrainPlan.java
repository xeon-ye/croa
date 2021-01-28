package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName: TrainPlan
 * @Description: 培训计划表
 * @Author: Xuxiong
 * @Date: 2020/4/7 0007 18:08
 * @Version: 1.0
 */
@Table(name = "t_train_plan")
@Data
public class TrainPlan implements Serializable {
    @Id
    private Integer id;//主键ID
    private String title;//标题
    private Integer coursePlate;//课程板块
    private Integer trainWay;//培训方式
    private Byte coursewareKeep;//课件是否存档：0-不存档、1-存档
    private String trainTimeDesc;//培训时间描述
    private String remake;//描述
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private String companyCode;//公司代码
    private Byte state;//状态：0-启用、1-禁用、-9-删除
}
