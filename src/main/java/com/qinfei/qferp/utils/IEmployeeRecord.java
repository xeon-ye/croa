package com.qinfei.qferp.utils;

/**
 * 员工相关的记录状态；
 * 
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:42；
 */
public interface IEmployeeRecord {
	// 审核中；
	int STATE_APPROVE = 0;
	// 审核通过；
	int STATE_PASS = 1;
	// 审核拒绝；
	int STATE_REJECT = 2;
	// 审核完成，比如离职完成、调岗完成；
	int STATE_FINISH = 3;
	// 准备交接；
	int STATE_CONNECT_READY = 4;
	// 交接处理中；
	int STATE_CONNECT = 5;
}