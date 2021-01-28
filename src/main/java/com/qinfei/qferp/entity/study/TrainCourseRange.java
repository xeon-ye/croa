package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.util.Date;

/**
 * @CalssName: TrainCourseRange
 * @Description: 课程范围表
 * @Author: Xuxiong
 * @Date: 2020/4/9 0009 10:04
 * @Version: 1.0
 */
@Table(name = "t_train_course_range")
@Data
public class TrainCourseRange {
    @Id
    private Integer id;//主键ID
    private Integer courseId;//课程ID
    private Byte courseEnrollFlag;//课程报名范围：1-部门、2-角色、3-用户
    private String rangeId;//范围表主键ID：根据课程报名范围，对应不同表的主键ID
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private Byte state;//状态：0-有效、-9-删除
}
