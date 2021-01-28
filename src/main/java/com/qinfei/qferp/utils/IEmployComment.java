package com.qinfei.qferp.utils;

/**
 * 入职申请的审核记录类型；
 *
 * @Author ：Yuan；
 * @Date ：2019/03/04 0028 14:42；
 */
public interface IEmployComment {
	// 人事部；
	int COMMENT_PERSONAL = 0;
	// 部门专业测试员；
	int COMMENT_TEST = 1;
	// 部分负责人；
	int COMMENT_LEADER = 2;
}