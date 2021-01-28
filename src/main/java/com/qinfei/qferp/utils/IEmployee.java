package com.qinfei.qferp.utils;

/**
 * 员工花名册的信息状态；
 * 
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:42；
 */
public interface IEmployee {
	// 试用期/实习期；
	int EMPLOYEE_PROBATION = 0;
	// 转正；
	int EMPLOYEE_FORMAL = 1;
	// 离职；
	int EMPLOYEE_LEAVE = 2;
	// 准备交接；
	int EMPLOYEE_CONNECT_READY = 3;
	// 交接处理中；
	int EMPLOYEE_CONNECT = 4;
	// 删除；
	int EMPLOYEE_DELETE = -1;
	// 实习；
	int EMPLOYEE_INTERNSHIP = 5;
}