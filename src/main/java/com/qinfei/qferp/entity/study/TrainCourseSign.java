package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName: TrainCourseSign
 * @Description: 课程报名表
 * @Author: Xuxiong
 * @Date: 2020/4/9 0009 9:43
 * @Version: 1.0
 */
@Table(name = "t_train_course_sign")
@Data
public class TrainCourseSign implements Serializable {
    @Id
    private Integer id;//主键ID
    private Integer courseId;//课程ID
    private Integer userId;//用户ID
    private Integer completeTime;//学生上课的时间，单位: 分钟, 默认课程时间，迟到、旷课、早退由讲师手动编辑设置
    private Float completeRate;//课程完成率，默认100%，根据完成时间动态计算
    private Float integral;//积分：动态计算（考生交卷、老师阅卷、讲师完课状态）
    private Float score;//评分（最高100分），仅能评分一次
    private Byte scoreFlag;//是否评分：0-未评分、1-已评分
    private Byte likeFlag;//是否点赞：0-未点赞、1-点赞
    private Byte ventFlag;//是否吐槽：0-未吐槽、1-吐槽
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private String companyCode;//公司代码
    private Byte state;//状态：0-有效、-9-删除

    @Transient
    private TrainCourse trainCourse;//报名课程对象
}
