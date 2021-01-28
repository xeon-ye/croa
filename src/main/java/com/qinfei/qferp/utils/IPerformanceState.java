package com.qinfei.qferp.utils;

/**
 * 考核评分的状态；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/16 0028 14:42；
 */
public interface IPerformanceState {
	// 进行中；
	int HANDING = 0;
	// 已完成；
	int FINISH = 1;
	// 被拒绝；
	int REJECT = 2;
}