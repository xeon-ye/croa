package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @CalssName: TrainCourseComment
 * @Description: 课程评论表
 * @Author: Xuxiong
 * @Date: 2020/4/15 0015 9:03
 * @Version: 1.0
 */
@Table(name = "t_train_course_comment")
@Data
public class TrainCourseComment implements Serializable {
    @Id
    private Integer id;//主键ID
    private Integer courseId;//课程ID
    private Integer parentId;//父级评论ID，默认：0，当回复别人评论时，该值为被回复的评论ID
    private String courseComment;//课程评论
    private Integer createId;//创建人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;//创建时间
    private Integer updateId;//更新人ID
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;//更新时间
    private String companyCode;//公司代码
    private Byte state;//状态：0-启用、-9-删除
}
