package com.qinfei.qferp.utils;

/**
 * 系统常量
 *
 * @author QinFei gzw
 */
public interface IConst {
	/**
	 * 系统登录sessionKey
	 */
	String USER_KEY = "user";

	String APP_NAME = "内部智能管理系统";
	/**
	 * 验证码
	 */
	String VERIFY_CODE = "verifyCode";

	String AUTHOR = "Authorization";

	String PROCESSES = "processes";

	String TOKEN = "token";

	String REGION = "region";

	String REGIONS = "regions";

	String PROJECT = "project";

	String USERS = "users";

	String COMPANIES = "companies";

	String USER_RESOURCE = "resources";

	String ACTIVE = "spring.profiles.active";

	String ENVIRONMENT = "server.environment";

	String USER_ROLE = "roles";

	String INVOICE_CODE = "FP";// 开票代号
	String ACCOUNT_CODE = "ZH";// 账户代号，未启用
	String INCOME_CODE = "JZ";// 进账代号，未启用
	String ASSIGN_CODE = "FK";// 分款代号，未启用
	String OUTGO_CODE = "QK";// 请款代号
	String REFUND_CODE = "TK";// 退款代号
	String BORROW_CODE = "JK";// 借款代号
	String COMMISSION_CODE = "TC";// 提成代号
	String DROP_CODE = "SG";// 退稿代号
	String INFOEM_no = "TZ-";// 通知代号
	String REIMBURSEMENT_CODE = "BX"; // 报销代号
	String PURCHASE_CODE = "WPCG";//物品采购代号
	String PROJECT_CODE = "XM";// 项目
	String PURCHASE_SUPPLIER_CODE = "GYS";//物品供应商代号
	String INVENTORY_STOCK ="RK"; //进销存入库编号
	String INVENTORY_CODE="KC";//库存编号
	String APPLY_CODE="LY";//物品领用编号
	String OUTSTOCK_CODE="CK";//物品出库编号
	String REPAIR_CODE="BX";//物品报修编号
	String SCRAP_CODE="BF";//物品报废编号
	String RETURN_CODE="GH";//物品归还编号
	String STOCK_CHECK_CODE="PD";//物品归还编号
	String ACCOUNTS_MESS = "LZ";//烂账代号
	String STANDARDIZED_COMPANY_APPLICATION = "GSSQ";// 标准化公司申请
	/**
	 * JT 集团
	 */
	String ROLE_TYPE_JT = "JT";
	/**
	 * YW 业务
	 */
	String ROLE_TYPE_YW = "YW";
	/**
	 * MJ 媒介
	 */
	String ROLE_TYPE_MJ = "MJ";
	/**
	 * HQ 后勤
	 */
	String ROLE_TYPE_HQ = "HQ";
	/**
	 * CW 财务
	 */
	String ROLE_TYPE_CW = "CW";

	/**
	 * RS 人事
	 */
	String ROLE_TYPE_RS = "RS";
	/**
	 * XZ 行政
	 */
	String ROLE_TYPE_XZ = "XZ";
	/**
	 * 总经办
	 */
	String ROLE_TYPE_ZJB = "ZJB";
	/**
	 * 系统管理部
	 */
	String ROLE_TYPE_XT = "XT";

	/**
	 * 项目
	 */
	String ROLE_TYPE_XM ="XM";

	/**
	 * 部长
	 */
	String ROLE_CODE_BZ = "BZ";
	/**
	 * 员工
	 */
	String ROLE_CODE_YG = "YG";
	/**
	 * 组长
	 */
	String ROLE_CODE_ZZ = "ZZ";
	/**
	 * 副总
	 */
	String ROLE_CODE_FZ = "FZ";
	/**
	 * 总裁
	 */
	String ROLE_CODE_ZC = "ZC";
	/**
	 * 副总裁
	 */
	String ROLE_CODE_FZC = "FZC";
	/**
	 * 会计
	 */
	String ROLE_CODE_KJ = "KJ";
	/**
	 * 出纳
	 */
	String ROLE_CODE_CN = "CN";
	/**
	 * 出纳
	 */
	String ROLE_CODE_ZJ = "ZJ";
	/**
	 * 财务助理
	 */
	String ROLE_CODE_ZL = "ZL";
	/**
	 * 总经理
	 */
	String ROLE_CODE_ZJL = "ZJL";
	/**
	 * 政委
	 */
	String ROLE_CODE_ZW = "ZW";
	/**
	 * 专员
	 */
	String ROLE_CODE_ZY = "ZY";

	/**
	 * state状态对应值 统计查state>0 列表页面查state>-2 删除和编辑功能仅限 state==0 || state==-1
	 * 审核完成后如果需要出纳操作state置2，不需要出纳操作置1 财务出纳操作后置1
	 */
	int STATE_DELETE = -9;// 删除
	int STATE_REJECT = -1;// 审核驳回
	int STATE_SAVE = 0; // 已保存，默认
	int STATE_FINISH = 1;// 已完成
	int STATE_PASS = 2;// 审核通过,出纳出款
	int STATE_ZZ = 3;// 组长审核
	int STATE_BZ = 4;// 部长审核
	int STATE_ZJ = 5;// 总监审核
	int STATE_CFO = 6;// 财务总监复核
	int STATE_VP = 7;// 副总经理复核
	int STATE_CEO = 8;// 总经理审核
	int STATE_KJ = 9;// 会计审核
	int STATE_YW = 10;// 业务员确认
	int STATE_CWKP = 11;// 财务助理开票
	int STATE_CWFH = 12;// 财务部长复审
	int STATE_RS = 13;// 人事部审核；
	int STATE_XZ = 14;// 行政部审核；
	int STATE_GR = 15;// 员工个人确认；
	int STATE_CN = 16; // 出纳审核；
	int STATE_ZRBZ = 17; // 调岗的转入部门领导审核；
	int STATE_XZZJ = 18; // 行政部总监；
	int STATE_GROUP = 19; // 考核小组；
	int STATE_MEETINGROM = 20; //会议室考核（选择特定考核人考核）；
	int STATE_REPORT = 21;//出差总结报告；
	int STATE_ZJLFS = 22 ; //总经理复审
	int STATE_WORKUP = 23 ; //流程唤醒中
	int STATE_RESTART =24;//会议任务重启流程
	int STATE_ZW =25;//政委审核
	int STATE_MEDIUMBACKFILL = 26; //媒介回填
    int STATE_SALES=27;//销售审核
	int STATE_ZJB=28; //总经办审核
	int STATE_CKGLY=29;//仓库管理员审核
	int STATE_JTCWBZ = 30;//集团财务部门负责人
	int STATE_JTCWMGR= 31; //集团财务分管领导
	int STATE_JTZC = 32; //集团总裁
	int STATE_KHGLY =37; // 客户管理员
	int STATE_BMFZR = 41; //部门负责人

	int ACCOUNT_TYPE_COMPANY = 1;// 公司账户，公司财务用
	int ACCOUNT_TYPE_SUPPLIER = 2; // 供应商银行账户
	int ACCOUNT_TYPE_CUST = 3; // 客户银行账户
	int ACCOUNT_TYPE_PERSONAL = 4;// 个人账户，公司内部使用

	/**
	 * 当前登录用户媒体类型列表
	 */
	String USER_MEDIA_TYPE_LIST = "userMediaTypeList";

	/**
	 * 计算提成标识
	 */
	int INCOME_FLAG = 1;
	int OUTGO_FLAG = 2;
	int INVOICE_FLAG = 3;
	int REFUND_FLAG = 4;
	int OTHERPAY_FLAG = 5;

	double COMM_PERCENT = 20;// 提成百分比

	int VISIABLE_DAY = 7;// 进账可见天数

	// 公司代码，部门表用
	String COMPANY_CODE_JT = "JT";// 集团
	String COMPANY_CODE_XH = "XH";// 祥和
	String COMPANY_CODE_HY = "HY";// 华越
	String COMPANY_CODE_BD = "BD";// 波动
	String COMPANY_CODE_DY = "DY";// 第一事业部
	String COMPANY_CODE_HTLS = "HTLS";// 河图洛书
	String COMPANY_CODE_MBWH = "MBWH";// 马骉文化

	// 公司代码，部门表用
	String COMPANY_CODE_JT_STR = "集团";// 集团
	String COMPANY_CODE_XH_STR = "祥和文化传播有限公司";// 祥和
	String COMPANY_CODE_HY_STR = "深圳华越";// 华越
	String COMPANY_CODE_BD_STR = "波动";// 波动
	String COMPANY_CODE_DY_STR = "第一事业部";// 第一事业部
	String COMPANY_CODE_HTLS_STR = "河图洛书";// 河图洛书
	String COMPANY_CODE_MBWH_STR = "马骉文化";// 马骉文化

	// 部门类型代码，部门表用
	String DEPT_TYPE_XMT = "XMT";// 新媒体
	String DEPT_TYPE_WL = "WL";// 网络

	String COMPANY_CODE="companyCode";

	int REPAY_TYPE_BORROW = 0 ; //还款类型，手动发起还款
	int REPAY_TYPE_OUTGO = 1 ;//还款类型，请款冲抵还款
	int REPAY_TYPE_REIM = 2 ;//还款类型，报销冲抵还款
	int REPAY_TYPE_DROP = 3 ;//还款类型，退稿退还借款

	int REPAY_STATE_SAVE = 0 ; //还款状态，已保存
	int REPAY_STATE_FINISH = 1 ;//还款状态，已还款
	int REPAY_STATE_PROCESS = 2 ;//还款状态，进行中

	int REPAYING_FALSE = 0 ;//还款流程标志位，不在还款中
	int REPAYING_TRUE = 1 ;//还款流程标志位，正在还款中

	int BORROW_FLAG_FALSE = 0 ;//报销关联借款标志位，
	int BORROW_FLAG_TRUE = 1 ;//报销关联借款标志位

	int DROP_FUND_FLAG_FALSE = 0 ;//退稿退还别用金标志位
	int DROP_FUND_FLAG_TRUE = 1 ;//退稿退还别用金标志位

	int REFUND_TYPE_FULL= 0 ;//全额退款
	int REFUND_TYPE_REFUND = 1 ;//稿件退款
	int REFUND_TYPE_OTHER_PAY = 2 ;//其他支出退款

	int FEE_STATE_SAVE = 0 ;//已保存
	int FEE_STATE_FINISH = 1 ;//已完成
	int FEE_STATE_PROCESS = 2 ;//进行中

	String TYPE_INNER = "内部";
	String TYPE_OUTER = "外部";

	String ACCOUNT_TYPE_B2B = "B2B" ;
	String ACCOUNT_TYPE_B2C = "B2C" ;

	String ACCOUNT_LEVEL_A = "A" ;
	String ACCOUNT_LEVEL_B = "B" ;
	String ACCOUNT_LEVEL_C = "C" ;

	String DICT_TYPE_CODE_TAX = "tax" ;

	Integer INVOICE_TYPE_NONE = 0 ;//未指定发票
	Integer INVOICE_TYPE_NORMAL = 1 ;//普通发票
	Integer INVOICE_TYPE_SPECIAL = 2 ;//专用发票

	String NEWSPAPER_BASE_PRICE = "basePrice" ;
	String NEWSPAPER_TITLE_PRICE = "titlePrice->referencePrice" ;

	Integer handOverStateOn = 1 ;
	Integer handOverStateOff = 0 ;

	String article_change_batch_saleAmount = "导入报价";
	String article_change_business_complete = "完善客户";
	String article_change_business_edit = "业务员修改";
	String article_change_media_edit = "媒介修改";
	String article_change_income_add = "新增分款";
	String article_change_income_del = "删除分款";
	String article_change_invoice_add = "新增开票";
	String article_change_invoice_del = "删除开票";
	String article_change_refund_add = "新增退款";
	String article_change_refund_del = "删除退款";
	String article_change_other_pay_add = "新增其他支出";
	String article_change_other_pay_del = "删除其他支出";
	String article_change_comm_add = "登记提成";
	String article_change_comm_del = "取消登记提成";

	Integer article_alter_flag_false = 0 ;
	Integer article_alter_flag_true = 1 ;

	String project_user_type_name = "name";
	String project_user_type_userName = "userName";
	String project_user_type_userId = "userId";
	String project_user_type_deptId = "deptId";
	String project_user_type_deptName = "deptName";
	String project_user_type_ratio = "ratio";
	String project_user_type_index = "index";

	String project_user_type_ch = "CH";
	String project_user_type_khjl = "KHJL";//客户经理
	String project_user_type_khzj = "KHZJ";//客户总监
	String project_user_type_khzy = "KHZY";//客户专员
	String project_user_type_xs = "XH";

	String USER_AGENT="User-Agent";

	String INDIVIDUAL_COMPNAY_NAME = "个体工商户";
	Integer PROTECT_LEVEL_A = 3;
	Integer PROTECT_LEVEL_B = 2;
	Integer PROTECT_LEVEL_C = 1;
	Integer PROTECT_LEVEL_D = 0;

	Integer PROTECT_NUM_A = 3;
	Integer PROTECT_NUM_B = 20;
	Integer PROTECT_NUM_C = 500;

	Integer COMMON_FLAG_TRUE = 1;
	Integer COMMON_FLAG_FALSE = 0;

	Integer COMMON_STATE_PROCESS = 2;
	Integer COMMON_STATE_FINISH = 1;
	Integer COMMON_STATE_INIT = 0;

	Integer TRANSFER_TYPE_IN_ADD = 0; //0新增客户
	Integer TRANSFER_TYPE_IN_CLAIM = 1; //1公海认领
	Integer TRANSFER_TYPE_IN_POINT = 2; //2点对点流转
	Integer TRANSFER_TYPE_IN_PROTECT = 3; //申请保护获取

	String TRACK_CONTENT_ADD = "开发客户";//新增客户跟进内容
	String TRACK_CONTENT_DEAL = "成交客户";//成交客户跟进内容
	String TRANSFER_TYPE_OUT_POINT = "点对点流转";
	String TRANSFER_TYPE_OUT_PUBLIC = "抛入公海";
	String TRANSFER_TYPE_OUT_EVAL = "考核不通过抛入公海";
	String TRANSFER_TYPE_OUT_DEL = "删除客户";
	String TRANSFER_TYPE_OUT_PROTECT = "被他人保护";

	String CUST_PROTECT = "CUST_PROTECT";//客户保护参数
	String CUST_PROTECT_NUM = "NUM";//客户保护数量
	String CUST_PROTECT_EVAL = "EVAL";//是否考核
	String CUST_PROTECT_AUDIT = "AUDIT";//是否审核

	String CUST_PROTECT_A = "CUST_PROTECT_A";//A类客户保护参数
	String CUST_PROTECT_NUM_A = "NUM_A";//A类客户保护数量
	String CUST_PROTECT_B = "CUST_PROTECT_B";//B客户保护参数
	String CUST_PROTECT_NUM_B = "NUM_B";//B类客户保护数量
	String CUST_PROTECT_C = "CUST_PROTECT_C";//C客户保护参数
	String CUST_PROTECT_NUM_C = "NUM_C";//C类客户保护数量

	String CUST_TRANSFER = "CUST_TRANSFER";
	String TRACK_EVAL_DAY = "TRACK_EVAL_DAY";//跟进考核天数
	String DEAL_EVAL_DAY = "DEAL_EVAL_DAY";//成交考核天数
	String EVAL_REMIND_DAY = "EVAL_REMIND_DAY";//考核提醒提前天数
	String TO_BLACK_TIMES = "TO_BLACK_TIMES";//客户流转黑名单次数
	String CLAIM_TIMES_DAY = "CLAIM_TIMES_DAY";//客户认领单天限额
	String CLAIM_START_TIME = "CLAIM_START_TIME";//客户认领开始时间点限定

	Integer CUST_TYPE_COMPANY = 1;//企业客户
	Integer CUST_TYPE_PERSONAL = 0;//个体工商户

	String DEPT_CODE_XT = "XT";//系统管理部
	String DEPT_CODE_YW = "YW";//业务部

	int PROTECT_STATE_OPERATOR = 2;//运营人员审批
	int PROTECT_STATE_CEO = 3;//总经理审批
	int PROTECT_STATE_CEOJT = 4;//集团总经理审批
	int PROTECT_STATE_FINISH = 1;//完成
	int PROTECT_STATE_FAIL = -1;//驳回
}
