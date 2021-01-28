package com.qinfei.qferp.utils;

/**
 * 入职申请的信息状态；
 * 
 * @Author ：Yuan；
 * @Date ：2019/2/28 0028 14:42；
 */
public interface IEmployEntry {
	// 待审核；
	int ENTRY_PENDING = 0;
	// 审核中；
	int ENTRY_APPROVE = 1;
	// 同意录用；
	int ENTRY_AGREE = 2;
	// 已入职；
	int ENTRY_EMPLOY = 3;
	// 不予考虑（存档备用）；
	int ENTRY_ARCHIVE = 4;
	// 已离职；
	int ENTRY_LEAVED = 5;
	// 已删除；
	int ENTRY_DELETE = -1;
}