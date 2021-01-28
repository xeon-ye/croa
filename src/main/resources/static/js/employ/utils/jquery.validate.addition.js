// 关键内容（如名称等）的校验规则4-20位；
jQuery.validator.addMethod("keyname", function (value, element) {
    return this.optional(element) || /^[\u4e00-\u9fa5A-Za-z][\u4e00-\u9fa5A-Za-z\w]{3,19}$/.test(value);
});

// 常用名称2-8位；
jQuery.validator.addMethod("normalname", function (value, element) {
    return this.optional(element) || /^[\u4e00-\u9fa5A-Za-z][\u4e00-\u9fa5A-Za-z\w]{1,7}$/.test(value);
});

// 文本内容4位以上；
jQuery.validator.addMethod("textname", function (value, element) {
    return this.optional(element) || /^[\u4e00-\u9fa5A-Za-z][\u4e00-\u9fa5A-Za-z\w]{1,}$/.test(value);
});

// 密码校验6-20位；
jQuery.validator.addMethod("keypass", function (value, element) {
    return this.optional(element) || /^[a-zA-Z]\w{5,19}$/.test(value);
});

// ip地址校验；
jQuery.validator.addMethod("ip", function (value, element) {
    return this.optional(element) || /^((2[0-4]\d|25[0-5]|[01]?\d\d?)\.){3}(2[0-4]\d|25[0-5]|[01]?\d\d?)$/.test(value);
});

// 手机号码校验；
jQuery.validator.addMethod("phone", function (value, element) {
    return this.optional(element) || /^(1[34578]\d{9})$/.test(value);
});

// 固定电话号码校验；
jQuery.validator.addMethod("telephone", function (value, element) {
    return this.optional(element) || /^((\d{3,4}\-)|)\d{7,8}(|([-\u8f6c]{1}\d{1,5}))$/.test(value);
});

//特殊字符；
jQuery.validator.addMethod("special", function (value, element) {
    return this.optional(element) || /^[^'&$]+$/.test(value);
});

// 包含数字、字母和下划线；
jQuery.validator.addMethod("noch", function (value, element) {
    return this.optional(element) || /^[A-Za-z0-9_-]+$/.test(value);
});

// 验证码校验；
jQuery.validator.addMethod("code", function (value, element) {
    return this.optional(element) || /^[a-zA-Z0-9]{4}$/.test(value);
});

// 身份证校验；
jQuery.validator.addMethod("indentity", function (value, element) {
    return this.optional(element) || /^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{2}[0-9Xx]$/.test(value) || /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}[0-9Xx]$/.test(value);
});

// 不能相同；
jQuery.validator.addMethod("diffrentFrom", function (value, element, param) {
    var target = $(param).unbind(".validate-equalTo").bind("blur.validate-equalTo", function () {
        $(element).valid();
    });
    return value != target.val();
});

// 比较大小；
jQuery.validator.addMethod("bigThan", function (value, element, param) {
    var target = $(param).unbind(".validate-equalTo").bind("blur.validate-equalTo", function () {
        $(element).valid();
    });
    return parseFloat(value) >= parseFloat(target.val());
});

// 比较大小；
jQuery.validator.addMethod("smallThan", function (value, element, param) {
    var target = $(param).unbind(".validate-equalTo").bind("blur.validate-equalTo", function () {
        $(element).valid();
    });
    return parseFloat(value) <= parseFloat(target.val());
});

// 区间不包含；
jQuery.validator.addMethod("scope", function (value, element, param) {
    return this.optional(element) || (value > param[0] && value < param[1]);
});

// 日期比较大小；
jQuery.validator.addMethod("compareDate", function (value, element, param) {
    var target = $(param).unbind(".validate-equalTo").bind("blur.validate-equalTo", function () {
        $(element).valid();
    });
    var startDate = target.val();
    var date1 = new Date(Date.parse(startDate.replace("-", "/")));
    var date2 = new Date(Date.parse(value.replace("-", "/")));
    return this.optional(element) || date1 <= date2;
});