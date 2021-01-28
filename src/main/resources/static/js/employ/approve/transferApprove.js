$(function () {
    loadFormalData(getQueryString("code"));

    // 增加表单校验；
    $("#approveForm").validate();
});

// 打开选择审核的窗口；
function loadFormalData(code) {
    if (code && code.length > 0) {
        // 获取审核用户信息；
        $.post(baseUrl + "/employeeManage/getTransferInfo", {code: code}, function (data) {
            var dataValue = data.data.taskId;
            if (dataValue == null) {
                alertMessage("该审核已完成。");
                // 返回审核页面；
                setTimeout(function () {
                    window.location.href = '/process/queryTask';
                }, 1000);
            } else {
                // 获取当前节点；
                var processState = data.data.processState;
                // 个人确认的按钮文字要变更；
                if (processState == 15) {
                    $("#agreeEmploy").html('<i class="fa fa-check"></i>&nbsp;&nbsp;确认');
                    $("#rejectEmploy").html('<i class="fa fa-close"></i>&nbsp;&nbsp;有误');
                    $("#approveForm textarea[name='desc']").attr("placeholder", "如信息有误，请在此处输入内容");
                }

                // 隐藏数据；
                $("#approveForm input[name='deptId']").val(data.data.deptId);
                $("#approveForm input[name='taskId']").val(dataValue);
                // 审核数据；
                dataValue = data.data.employee;
                if(dataValue){
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
                        if (empState == 5) {
                            $("#approveForm").find("input[data='empState']").val("实习");
                        }
                    }
                    // 入职日期；
                    var empDate = dataValue["empDate"];
                    if (empDate) {
                        $("#approveForm input[name='empDate']").val(new Date(empDate).format("yyyy-MM-dd"));
                    }

                    // 执行日期；
                    var transDate = dataValue["transDate"];
                    if (transDate) {
                        $("#approveForm input[name='transDate']").val(new Date(transDate).format("yyyy-MM-dd"));
                    }
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

                    // 如果是部长节点没有获取到审批人，禁用信息；
                    if (processState == 4) {
                        $("#agreeEmploy").hide();
                        $("#agreeEmploy").parent().append("<b style='color: red;'>*&nbsp;该员工申请的转入部门无负责人，请联系管理员。</b>");
                    }
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