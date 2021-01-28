package com.qinfei.qferp.utils;

/**
 * 员工估计的操作类型；
 * 
 * @Author ：Yuan；
 * @Date ：2019/03/24 0028 14:42；
 */
public interface ITrajectoryTransaction {
	// 入职；
	int TRANSACTION_ENTRY = 0;
	// 录用；
	int TRANSACTION_EMPLOY = 1;
	// 转正；
	int TRANSACTION_FORMAL = 2;
	// 离职；
	int TRANSACTION_LEAVE = 3;
	// 调岗；
	int TRANSACTION_TRANSFER = 4;
	// 交接；
	int TRANSACTION_CONNECT = 5;
	// 其他；
	int TRANSACTION_OTHER = 6;
}