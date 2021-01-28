$(function () {
    // 设置复选框样式；
    $("#approveForm").find("input[type='checkbox']").closest(".i-checks").css("width", "30%");

    // 单选框、多选框美化；
    $(".i-checks").iCheck({
        checkboxClass: "icheckbox_square-green",
        radioClass: "iradio_square-green",
    });

    loadLeaveData(getQueryString("code"));

    // 增加表单校验；
    $("#approveForm").validate();
});

// 打开选择审核的窗口；
function loadLeaveData(code) {
    if (code && code.length > 0) {
        // 先隐藏其他补充的输入框；
        $("#approveForm textarea[name='leaveCompanyOther']").closest(".form-group").hide();
        $("#approveForm textarea[name='leavePersonOther']").closest(".form-group").hide();
        $("#approveForm textarea[name='otherReasonRemark']").closest(".form-group").hide();
        // 获取审核用户信息；
        $.post(baseUrl + "/employeeManage/getLeaveInfo", {code: code}, function (data) {
            var dataValue = data.data.taskId;
            if (dataValue == null) {
                alertMessage("该审核已完成。");
                // 返回审核页面；
                setTimeout(function () {
                    window.location.href = '/process/queryTask';
                }, 1000);
            } else {
                // 隐藏数据；
                $("#approveForm input[name='deptId']").val(data.data.deptId);
                $("#approveForm input[name='taskId']").val(dataValue);
                // 审核数据；
                dataValue = data.data.employee;
                for (var key in dataValue) {
                    $("#approveForm").find("input[type='text'][name='" + key + "']").val(dataValue[key]);
                    $("#approveForm").find("textarea[name='" + key + "']").val(dataValue[key]);
                }

                // 员工状态；
                var empState = dataValue["empState"];
                if (empState != undefined) {
                    if (empState == 0) {
                        $("#approveForm").find("input[data='empState']").val("试用期");
                    }
                    if (empState == 1) {
                        $("#approveForm").find("input[data='empState']").val("转正");
                    }
                }

                // 入职日期；
                var empDate = dataValue["empDate"];
                if (empDate) {
                    $("#approveForm input[name='empDate']").val(new Date(empDate).format("yyyy-MM-dd"));
                }

                // 离职日期；
                var leaveDate = dataValue["leaveDate"];
                if (leaveDate) {
                    $("#approveForm input[name='leaveDate']").val(new Date(leaveDate).format("yyyy-MM-dd"));
                }

                // 性质；
                setFormRadioChecked($("#approveForm input[name='leaveType']"), dataValue["leaveType"]);
                // 离职缘由；
                var reasonArray = ["终止试用", "公司辞退", "终止续签", "试用辞退", "个人辞职", "合同期满"];
                $("#approveForm").find("input[name='leaveTypeContent']").val(reasonArray[dataValue["leaveTypeContent"]]);

                // 设置公司原因复选框；
                setDataRequired("leaveCompany", "leaveCompanyOther", dataValue["leaveCompany"]);

                // 设置个人原因复选框；
                setDataRequired("leavePerson", "leavePersonOther", dataValue["leavePerson"]);

                // 设置其他原因复选框；
                setDataRequired("otherReason", "otherReasonRemark", dataValue["otherReason"]);

                // 审核人；
                dataValue = data.data.user;
                if (dataValue != null && dataValue.length > 0) {
                    layui.use(["form"], function () {
                        var userObj = $("#approveForm select[name='userId']");
                        for (var i = 0; i < dataValue.length; i++) {
                            userObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                        }
                        // 设置用户名；
                        var userNameObj = $("#approveForm").find("input[name='userName']");
                        userNameObj.val(dataValue[0].name);
                        // 初始化；
                        layui.form.render();

                        // 下拉框的onchange事件；
                        layui.form.on("select(userId)", function (userData) {
                            // 更新隐藏域；
                            userNameObj.val(getLayUISelectText(userData));
                        });
                    });
                    $("#approveForm .approveUser").show();
                } else {
                    $("#approveForm .approveUser").hide();
                }
                $("#employModal").modal({backdrop: "static", keyboard: false});
            }
        }, "json");
    } else {
        alertMessage("无权限访问。");
        // 返回审核页面；
        setTimeout(function () {
            window.location.href = '/process/queryTask';
        }, 1000);
    }
}

// 离职其他原因的处理；
function setDataRequired(inputName, textAreaName, checkData) {
    // 先清空选中；
    var companyArray;
    if (checkData) {
        companyArray = checkData.split(",");
        // 如果有其他选项，设置必填；
        var length = companyArray.length;
        // 设置必填；
        setRequired(inputName, companyArray[length - 1], true);
    }
    // 设置选中；
    setFormCheckBoxChecked($("#approveForm input[name='" + inputName + "']"), companyArray);
}

// 设置必填；
function setRequired(inputName, inputValue, flag) {
    var processRequired;
    var textAreaElement;
    if (inputName == "leaveCompany") {
        processRequired = inputValue == 8;
        textAreaElement = "leaveCompanyOther";
    }
    if (inputName == "leavePerson") {
        processRequired = inputValue == 7;
        textAreaElement = "leavePersonOther";
    }
    if (inputName == "otherReason") {
        processRequired = inputValue == 3;
        textAreaElement = "otherReasonRemark";
    }
    // 判断操作类型；
    if (flag) {
        if (processRequired) {
            var obj = $("#approveForm textarea[name='" + textAreaElement + "']");
            obj.closest(".form-group").show();
        }
    } else {
        if (processRequired) {
            var obj = $("#approveForm textarea[name='" + textAreaElement + "']");
            obj.closest(".form-group").hide();
        }
    }
}