$(function () {
    loadFormalData(getQueryString("code"));

    // 增加表单校验；
    $("#approveForm").validate();
});

// 打开选择审核的窗口；
function loadFormalData(code) {
    if (code && code.length > 0) {
        // 获取审核用户信息；
        $.post(baseUrl + "/employeeManage/getFormalInfo", {code: code}, function (data) {
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
                }

                // 入职日期；
                var empDate = dataValue["empDate"];
                if (empDate) {
                    $("#approveForm input[name='empDate']").val(new Date(empDate).format("yyyy-MM-dd"));
                }

                var processState = data.data.processState;
                $("#approveForm input[name='processState']").val(processState);
                // 个人确认的按钮文字要变更；
                if (processState == 15) {
                    $("#agreeEmploy").html('<i class="fa fa-check"></i>&nbsp;&nbsp;确认');
                    $("#rejectEmploy").html('<i class="fa fa-close"></i>&nbsp;&nbsp;有误');
                    $("#approveForm textarea[name='desc']").attr("placeholder", "如信息有误，请在此处输入内容");
                }

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