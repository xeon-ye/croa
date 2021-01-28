package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.sys.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName: TrainTeacher
 * @Description: 培训讲师表
 * @Author: Xuxiong
 * @Date: 2020/4/7 0007 17:49
 * @Version: 1.0
 */
@Table(name = "t_train_teacher")
@Data
public class TrainTeacher implements Serializable {
    @Id
    private Integer id;//主键ID
    private Integer userId;//用户ID
    private Integer goodAtFieldId;//擅长领域Id
    private Byte educationFlag;//是否有授课经验：0-无、1-有
    private String introduce;//个人简介
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private String companyCode;//公司代码
    private Byte state;//状态：0-有效、-9-删除

    @Transient
    private User user;//讲师信息
    @Transient
    private Integer teacherCourseNum;//课程数
    @Transient
    private Integer teacherComplateCourseNum;//课程完课数
    @Transient
    private Float teacherIntegral; //讲师积分
    @Transient
    private Float teacherCourseScore; //讲师课程所得学员总评分
    @Transient
    private Integer teacherCourseScoreNum; //讲师课程所得学员评分人数
    @Transient
    private Float teacherCourseAvgScore; //讲师完课课程平均评分总和
    @Transient
    private Integer teacherCourseLikeNum; //讲师课程点赞人数
    @Transient
    private Integer teacherCourseVentNum; //讲师课程吐槽人数
    @Transient
    private Integer teacherCourseCommentNum;//讲师课程评论次数
    @Transient
    private Integer teacherCourseSignNum;//讲师课程报名人数
    @Transient
    private Integer teacherCourseCompleteNum;//讲师课程完课人数

}
