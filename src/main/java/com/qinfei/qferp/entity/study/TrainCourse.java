package com.qinfei.qferp.entity.study;

import com.alibaba.fastjson.annotation.JSONField;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import com.qinfei.core.annotation.Transient;
import com.qinfei.qferp.entity.sys.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @CalssName: TrainCourse
 * @Description: 培训课程表
 * @Author: Xuxiong
 * @Date: 2020/4/9 0009 10:13
 * @Version: 1.0
 */
@Table(name = "t_train_course")
@Data
public class TrainCourse {
   @Id
   private Integer id;//主键ID
   private String title;//标题
   private Integer coursePlate;//课程板块
   private Integer trainWay;//培训方式
   @JSONField(format = "yyyy-MM-dd HH:mm:ss")
   private Date signStartTime;//报名开始时间
   @JSONField(format = "yyyy-MM-dd HH:mm:ss")
   private Date signEndTime;//报名截止时间，不能大于培训开始时间
   @JSONField(format = "yyyy-MM-dd HH:mm:ss")
   private Date trainStartTime;//培训开始时间
   @JSONField(format = "yyyy-MM-dd HH:mm:ss")
   private Date trainEndTime;//培训结束时间
   private String trainAddress;//培训地址
   private Float trainGrade;//培训课程学分，默认10分，用于学员所得积分
   @JSONField(format = "yyyy-MM-dd HH:mm:ss")
   private Date courseEndTime;//课程反馈截止时间，大于等于培训结束时间，默认等于培训结束时间
   private String coursePic;//课程图片
   private Byte courseEnrollFlag;//课程报名范围：0-未设置、1-部门、2-角色、3-用户
   private String courseRemake;//课程大纲
   private Float courseIntegral;//讲师课程所得积分，动态计算
   private Float courseScore;//讲师课程所得学员总评分，动态计算
   private Integer courseScoreNum;//讲师课程所得学员评分人数，动态计算
   private Integer courseLikeNum;//讲师课程点赞人数，动态计算
   private Integer courseVentNum;//讲师课程吐槽人数，动态计算
   private Integer courseCommentNum;//讲师课程评论次数，动态计算，一个人可评论多次
   private Integer courseSignNum;//讲师课程报名人数，动态计算
   private Integer courseCompleteNum;//讲师课程完课人数，动态计算
   private Integer createId;//创建人ID
   @JSONField(format = "yyyy-MM-dd HH:mm:ss")
   private Date createDate;//创建时间
   private Integer updateId;//更新人ID
   @JSONField(format = "yyyy-MM-dd HH:mm:ss")
   private Date updateDate;//更新时间
   private String companyCode;//公司代码
   private Byte state;//状态：0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回、-9-删除
   private String rejectReason; //审批拒绝原因

   @Transient
   private List<String> courseSignRangeList; //课程报名范围权限
   @Transient
   private User user;//讲师
   @Transient
   private String signRange;//课程范围
   @Transient
   private List<TrainCourseSign> courseSignList;//课程报名
   @Transient
   private List<TrainCourseComment> courseCommentList;//评论
}
