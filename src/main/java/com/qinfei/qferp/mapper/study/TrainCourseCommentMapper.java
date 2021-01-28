package com.qinfei.qferp.mapper.study;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.study.TrainCourseComment;

/**
 * @CalssName: TrainCourseCommentMapper
 * @Description: 课程评论
 * @Author: Xuxiong
 * @Date: 2020/4/15 0015 9:08
 * @Version: 1.0
 */
public interface TrainCourseCommentMapper extends BaseMapper<TrainCourseComment, Integer> {
    //新增
    int save(TrainCourseComment trainCourseComment);
}
