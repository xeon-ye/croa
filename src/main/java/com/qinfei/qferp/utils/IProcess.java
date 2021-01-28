package com.qinfei.qferp.utils;

/**
 * 流程代号，用于在监听器中进行区分；
 * 
 * @Author ：Yuan；
 * @Date ：2018/12/6 0006 19:02；
 */
public interface IProcess {
	// =================================================流程定义开始=================================================
	// 开票申请流程；
	int PROCESS_BALLOT = 1;
	// 借款申请流程；
	int PROCESS_BORROW = 2;
	// 媒介请款流程；网络请款
	int PROCESS_MEDIAREFUND = 3;
	// 退款流程；
	int PROCESS_REFUND = 4;
	// 财务提成流程；
	int PROCESS_ROYALTY = 5;
	// 员工录用流程；
	int PROCESS_EMPLOY = 6;
	// 请假流程（1天）；
	int PROCESS_VOCATION_ONE = 7;
	// 请假流程（2天）；
	int PROCESS_VOCATION_TWO = 8;
	// 请假流程（3天）；
	int PROCESS_VOCATION_THREE = 9;
	// 转正流程；
	int PROCESS_FORMAL = 10;
	// 离职流程；
	int PROCESS_LEAVE = 13;
	// 调岗流程；
	int PROCESS_TRANSFER = 14;
	// 离职交接流程；
	int PROCESS_HANDOVER_LEAVE = 15;
	// 调岗交接流程；
	int PROCESS_HANDOVER_TRANSFER = 16;
	// 退稿流程；
	int PROCESS_MANUSCRIPT = 11;
	// 费用报销流程；
	int PROCESS_REIMBURSEMENT = 12;
	// 外出流程；
	int PROCESS_OUTWORK = 17;
	// 加班申请流程；
	int PROCESS_WORKOVERTIME = 18;
	// 绩效考核流程；
	int PROCESS_PERFORMANCE = 19;
	// 出差申请流程
	int PROCESS_ONBUSINESS = 20;
	// 会议室审批流程
	int PROCESS_MEETINGROOM = 21;
	// 自媒体请款流程
	int PROCESS_SELF_MEDIAREFUND = 22;
	//唤醒流程
	int PROCESS_WORKUP = 23;
	// 河图请款流程
	int PROCESS_HTOUTPLEASETYPE =24;
	//报纸板块请款
	int PROCESS_NEWSPAPEROUTGO =25;
	//项目管理流程
	int PROCESS_PROJECT=26;
	//物品采购流程
	int PROCESS_PURCHASE=27;
	//物品领用流程
	int PROCESS_APPLY=28;
	//物品报修流程
	int PROCESS_REPAIR=29;
	//物品报废流程
	int PROCESS_SCRAP=30;
	//物品归还流程
	int PROCESS_RETURN=31;
	//烂账流程
	int PROCESS_MESS =32;
	//客户保护流程
	int PROCESS_PROTECT=33;
	// 标准化公司申请
	int PROCESS_STANDARDIZED_COMPANY = 37;


	// =================================================流程定义结束=================================================

	// =================================================以下内容为流程配置的变量和状态，如不清楚业务请勿修改=================
	// 流程正在审核中；
	int PROCESS_HANDLING = 0;
	// 流程已审核完成；
	int PROCESS_FINISHED = 1;
	// 流程审核拒绝；
	int PROCESS_REJECT = 2;
	// 待办事项显示的名称；
	String PROCESS_NAME = "processName";
	// 流程审核人跳转页面；
	String PROCESS_APPROVE_URL = "processUrl";
	// 流程驳回后的跳转页面地址；
	String PROCESS_EDIT_URL = "editUrl";
	// 流程审核通过后的财务出纳节点专用URL；
	String PROCESS_PASS_URL = "cashierUrl";
	// 流程任务完成和的待办事项跳转URL；
	String PROCESS_FINISH_URL = "dataUrl";
	//财务会计确认出账跳转url
	String PROCESS_ACCIYBR_URL ="accUrl";
	//媒介回填跳转url
	String PROCESS_BACKFILL_URL ="backUrl";
	//财务助理开票跳转url
	String PROCESS_ASSISTANT_URL="assUrl";
	// =================================================配置结束=================================================
}