package com.qinfei.qferp.utils;

/**
 * 员工花名册的信息状态；
 * 
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:42；
 */
public interface ITrajectoryOperate {
	// 提起申请；
	int OPERATE_SUBMIT = 0;
	// 申请通过；
	int OPERATE_PASS = 1;
	// 申请被拒；
	int OPERATE_REJECT = 2;
	// 内容变更；
	int OPERATE_UPDATE = 3;
}