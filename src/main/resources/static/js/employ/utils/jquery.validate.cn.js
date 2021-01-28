// 字段校验汉化；
jQuery.extend(jQuery.validator.messages, {
	required: "此项必须有内容",
	remote: "已存在或输入格式有误",
	email: "请输入有效的邮箱地址",
	url: "请输入有效的链接地址",
	date: "请输入合法的日期",
	dateISO: "日期格式不正确",
	number: "请输入有效的数字",
	digits: "该项仅能输入整数",
	creditcard: "序列号无效",
	equalTo: "请再次输入上一项内容",
	diffrentFrom: "内容不得与上一项相同",
	bigThan: "内容不得小于上一项内容",
	smallThan: "内容不得大于上一项内容",
	//accept: "扩展名无效",
	maxlength: $.validator.format("长度应不能超过【{0}】位"),
	minlength: $.validator.format("长度应不能少于【{0}】位"),
	rangelength: $.validator.format("请输入【{0}~{1}】位"),
	range: $.validator.format("值应在【{0}~{1}】之间"),
	scope: $.validator.format("值应在【{0}~{1}】之间"),
	max: $.validator.format("值应小于等于【{0}】"),
	min: $.validator.format("值应大于等于【{0}】"),
	compareDate: "日期不得小于上一项",
	keyname: "汉字或字母开头，长度4位以上",
	normalname: "汉字或字母开头，长度2位以上",
	textname: "汉字或字母开头，长度2位以上",
	keypass: "字母开头，长度6位以上",
	phone: "请输入有效的手机号码",
	telephone: "请输入有效的固定电话号码",
	special: "不得包含特殊字符",
	noch: "不能包含中文和特殊符号",
	code: "仅能输入字母和数字",
	indentity: "请输入有效的身份证号码",
	ip: "IP地址输入格式有误"
});