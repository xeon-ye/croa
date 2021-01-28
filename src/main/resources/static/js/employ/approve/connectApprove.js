$(function () {
    // 设置复选框样式；
    $("#approveForm").find("input[type='checkbox']").closest(".i-checks").css("width", "30%");

    // 单选框、多选框美化；
    $(".i-checks").iCheck({
        checkboxClass: "icheckbox_square-green",
        radioClass: "iradio_square-green",
    });

    loadConnectData(getQueryString("code"));

    // 增加表单校验；
    $("#approveForm").validate();
});

// 打开选择审核的窗口；
function loadConnectData(code) {
    // 隐藏交接清单；
    $("#approveForm").find(".conList").hide();
    if (code && code.length > 0) {
        // 获取审核用户信息；
        $.post(baseUrl + "/employeeManage/getConnectInfo", {code: code, conType: $("#conType").val()}, function (data) {
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
                    $("#approveForm").find("input[type='hidden'][name='" + key + "']").val(dataValue[key]);
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

                // 通知类型；
                var conDateType = dataValue["conDateType"];
                if (conDateType == 0) {
                    $("#approveForm").find("input[data='conDateType']").val("本人书面申请时间");
                } else {
                    $("#approveForm").find("input[data='conDateType']").val("公司通知时间");
                }

                // 通知日期；
                var conDate = dataValue["conDate"];
                if (conDate) {
                    $("#approveForm input[name='conDate']").val(new Date(conDate).format("yyyy-MM-dd"));
                }

                // 离职日期；
                var completeDate = dataValue["completeDate"];
                if (completeDate) {
                    $("#approveForm input[name='completeDate']").val(new Date(completeDate).format("yyyy-MM-dd"));
                }

                // 性质；
                var leaveType = dataValue["leaveType"];
                if (leaveType == 0) {
                    $("#approveForm").find("input[data='leaveType']").val("公司劝退");
                } else {
                    $("#approveForm").find("input[data='leaveType']").val("个人离职");
                }
                // 离职缘由；
                var reasonArray = ["终止试用", "公司辞退", "终止续签", "试用辞退", "个人辞职", "合同期满"];
                $("#approveForm").find("input[data='leaveTypeContent']").val(reasonArray[dataValue["leaveTypeContent"]]);

                // 交接清单；
                var conList = dataValue["conList"];
                if (conList) {
                    $("#conList").html("<b class='form-control' style='border: 0px;'><a href='" + conList + "' target='_blank'>点击查看</a></b>");
                    $("#approveForm").find(".conList").show();
                }

                // 根据审批节点区分显示内容；
                var processState = data.data.processState;
                $("#conId").val(dataValue["conId"]);
                $("#processState").val(processState);
                // 个人确认的节点显示交接清单上传；
                if (processState == 15) {
                    $("#agreeEmploy").html('<i class="fa fa-check"></i>&nbsp;&nbsp;确认');
                    $("#rejectEmploy").html('<i class="fa fa-close"></i>&nbsp;&nbsp;有误');
                    $("#approveForm textarea[name='desc']").attr("placeholder", "如信息有误，请在此处输入内容");

                    $("#approveForm").find(".fileUpload").show();

                    // 隐藏其他部门的审核信息；
                    $("#approveForm").find(".deptApprove").hide();
                    $("#approveForm").find(".personalApprove").hide();
                    $("#approveForm").find(".financeApprove").hide();
                } else {
                    $("#approveForm").find(".fileUpload").hide();

                    // 设置部门工作交接复选框；
                    setDataChecked("conDeptApprove", dataValue["conDeptApprove"]);

                    // 设置人事工作交接复选框；
                    setDataChecked("conPersonal", dataValue["conPersonal"]);

                    // 设置财务工作交接复选框；
                    setDataChecked("conFinance", dataValue["conFinance"]);

                    // 部门的处理人；
                    if (processState == 4) {
                        // 显示部门下拉框和其他部门文本框；
                        $("#approveForm .deptData").find(".selectEmp").show();
                        $("#approveForm .deptData").find(".showEmp").hide();

                        // 允许输入；
                        $("#approveForm .deptData").find("input[type='checkbox']").each(function () {
                            $(this).parent().removeClass("disabled");
                            $(this).removeAttrs("disabled");
                        });
                        $("#approveForm .deptData").find("textarea").removeAttrs("disabled");

                        $("#approveForm .personalApprove").hide();

                        $("#approveForm .financeApprove").hide();

                        // 加载工作交接人；
                        dataValue = data.data.colleague;
                        loadDeptUser(dataValue);
                        // 人事的处理人；
                    } else if (processState == 13) {
                        // 显示部门下拉框和其他部门文本框；
                        $("#approveForm .deptData").find(".selectEmp").hide();
                        $("#approveForm .deptData").find(".showEmp").show();

                        $("#approveForm .personData").find(".selectPerson").show();
                        $("#approveForm .personData").find(".showPerson").hide();

                        // 允许输入；
                        $("#approveForm .personData").find("input[type='checkbox']").each(function () {
                            $(this).parent().removeClass("disabled");
                            $(this).removeAttrs("disabled");
                        });
                        $("#approveForm .personData").find("textarea").removeAttrs("disabled");

                        $("#approveForm").find(".financeApprove").hide();

                        // 加载人事专员；
                        dataValue = data.data.colleague;
                        loadPerosonUser(dataValue);
                        // 财务的处理人；
                    } else if (processState == 6) {
                        // 显示部门下拉框和其他部门文本框；
                        $("#approveForm .deptData").find(".selectEmp").hide();
                        $("#approveForm .deptData").find(".showEmp").show();

                        $("#approveForm .personData").find(".selectPerson").hide();
                        $("#approveForm .personData").find(".showPerson").show();

                        $("#approveForm .financeData").find(".selectFinance").show();
                        $("#approveForm .financeData").find(".showFinance").hide();

                        // 允许输入；
                        $("#approveForm .financeData").find("input[type='checkbox']").each(function () {
                            $(this).parent().removeClass("disabled");
                            $(this).removeAttrs("disabled");
                        });
                        $("#approveForm .financeData").find("textarea").removeAttrs("disabled");

                        // 加载人事专员；
                        dataValue = data.data.colleague;
                        loadFinanceUser(dataValue);
                    } else {
                        // 显示部门下拉框和其他部门文本框；
                        $("#approveForm .deptData").find(".selectEmp").hide();
                        $("#approveForm .deptData").find(".showEmp").show();

                        $("#approveForm .personData").find(".selectPerson").hide();
                        $("#approveForm .personData").find(".showPerson").show();

                        $("#approveForm .financeData").find(".selectFinance").hide();
                        $("#approveForm .financeData").find(".showFinance").show();
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

// 打开文件上传窗口；
function openFileUpload() {
    // 清空文件；
    $("#uploadFile").val("");
    $("#uploadModal").modal({backdrop: "static", keyboard: false});

    $("#employModal").modal("hide");
}

// 关闭文件上传窗口；
function closeFileUpload() {
    $("#uploadModal").modal("hide")

    $("#employModal").modal({backdrop: "static", keyboard: false});
}

// 开始上传文件；
function beginFileUpload() {
    if ($("#uploadFiles").val() == "") {
        alertMessage("请选择需要上传的文件。");
    } else {
        alertMessage("处理中。");
        // 校验表单；
        var content = $("#uploadFile").val();
        var reg = /^.+(.DOC|.doc|.DOCX|.docx|.XLS|.xls|.XLSX|.xlsx|.ZIP|.zip)$/;
        if (content.length > 0 && reg.test(content)) {
            $("#uploadModal").modal("hide");
            var options = {
                type: "POST",
                dataType: "json",
                url: baseUrl + "/entry/upload",
                success: function (data) {
                    // 清空表单；
                    $("#uploadFile").val("");
                    var fileName = data.data.image;
                    if (fileName == null) {
                        getResCode(data);
                    } else {
                        // 设置文件；
                        $("#approveForm").find("input[name='conList']").val(fileName)
                        // 显示内容；
                        $("#conList").html("<b class='form-control' style='border: 0px;'><a href='" + fileName + "' target='_blank'>点击查看</a></b>");
                        $("#approveForm").find(".conList").show();
                        // 切换弹窗；
                        $("#employModal").modal({backdrop: "static", keyboard: false});
                        alertMessage("处理完成。");
                    }
                },
                error: function () {
                    getResCode(data);
                }
            };
            $("#uploadForm").ajaxSubmit(options);
        } else {
            alertMessage("请选择Office办公文件或压缩文件。");
        }
    }
}

// 同意审批
function agreeApprove() {
    var processState = $("#processState").val();
    if (processState == 15) {
        // 个人节点有文件上传则需要更新；
        var conList = $("#approveForm").find("input[name='conList']").val();
        if (conList.length > 0) {
            alertMessage("系统处理中，请稍候。");
            startModal("#agreeEmploy");
            $.post(baseUrl + "/employeeManage/updateConnect", {
                processState: processState,
                conId: $("#conId").val(),
                conList: conList
            }, function (data) {
                Ladda.stopAll();
                var message = data.data.message;
                if (message == null) {
                    alertMessage(data.msg);
                } else {
                    // 提交流程；
                    completeApprove(0);
                }
            }, "json");
        } else {
            // 个人节点需要有交接清单；
            var fileContent = $("#conList").html();
            if (fileContent.length > 0) {
                completeApprove(0);
            } else {
                alertMessage("请上传交接清单文件。");
            }
        }
        // 部门审批节点有内容提交；
    } else if (processState == 4) {
        var conDeptObj = $("#approveForm .deptData").find("input[name='conDeptApprove']:checked");
        var length = conDeptObj.length;
        var conDeptApprove = "";
        if (length > 0) {
            conDeptObj.each(function (i) {
                conDeptApprove += $(this).val();
                if (i < length - 1) {
                    conDeptApprove += ",";
                }
            });
        }
        alertMessage("系统处理中，请稍候。");
        startModal("#agreeEmploy");
        $.post(baseUrl + "/employeeManage/updateConnect", {
            processState: processState,
            conId: $("#conId").val(),
            conDeptApprove: conDeptApprove,
            conEmpId: $("#approveForm .deptData").find("select[name='conEmpId']").val(),
            conEmpName: $("#approveForm .deptData").find("input[name='conEmpName']").val(),
            conDeptRemark: $("#approveForm .deptData").find("textarea[name='conDeptRemark']").val()
        }, function (data) {
            Ladda.stopAll();
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                // 提交流程；
                completeApprove(0);
            }
        }, "json");
        // 人事审批节点有内容提交；
    } else if (processState == 13) {
        var conPersonalObj = $("#approveForm .personData").find("input[name='conPersonal']:checked");
        var length = conPersonalObj.length;
        var conPersonal = "";
        if (length > 0) {
            conPersonalObj.each(function (i) {
                conPersonal += $(this).val();
                if (i < length - 1) {
                    conPersonal += ",";
                }
            });
        }
        alertMessage("系统处理中，请稍候。");
        startModal("#agreeEmploy");
        $.post(baseUrl + "/employeeManage/updateConnect", {
            processState: processState,
            conId: $("#conId").val(),
            conPersonal: conPersonal,
            conPersonalId: $("#approveForm .personData").find("select[name='conPersonalId']").val(),
            conPersonalName: $("#approveForm .personData").find("input[name='conPersonalName']").val(),
            conPersonalRemark: $("#approveForm .personData").find("textarea[name='conPersonalRemark']").val()
        }, function (data) {
            Ladda.stopAll();
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                // 提交流程；
                completeApprove(0);
            }
        }, "json");
        // 财务审批节点有内容提交；
    } else if (processState == 6) {
        var conFinanceObj = $("#approveForm .financeData").find("input[name='conFinance']:checked");
        var length = conFinanceObj.length;
        var conFinance = "";
        if (length > 0) {
            conFinanceObj.each(function (i) {
                conFinance += $(this).val();
                if (i < length - 1) {
                    conFinance += ",";
                }
            });
        }
        alertMessage("系统处理中，请稍候。");
        startModal("#agreeEmploy");
        $.post(baseUrl + "/employeeManage/updateConnect", {
            processState: processState,
            conId: $("#conId").val(),
            conFinance: conFinance,
            conFinanceId: $("#approveForm .financeData").find("select[name='conFinanceId']").val(),
            conFinanceName: $("#approveForm .financeData").find("input[name='conFinanceName']").val(),
            conFinanceRemark: $("#approveForm .financeData").find("textarea[name='conFinanceRemark']").val()
        }, function (data) {
            Ladda.stopAll();
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                // 提交流程；
                completeApprove(0);
            }
        }, "json");
    }else if (processState == 8) {
        var conFinanceObj = $("#approveForm .financeData").find("input[name='conFinance']:checked");
        var length = conFinanceObj.length;
        var conFinance = "";
        if (length > 0) {
            conFinanceObj.each(function (i) {
                conFinance += $(this).val();
                if (i < length - 1) {
                    conFinance += ",";
                }
            });
        }
        alertMessage("系统处理中，请稍候。");
        startModal("#agreeEmploy");
        $.post(baseUrl + "/employeeManage/updateConnect", {
            processState: processState,
            empName:$("#approveForm").find("input[name='empName']").val(),
            empDept:$("#approveForm").find("input[name='empDept']").val(),
            empId:$("#approveForm").find("input[name='empId']").val()

        }, function (data) {
            Ladda.stopAll();
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                // 提交流程；
                completeApprove(0);
            }
        }, "json");
    }

    else {
        // 提交流程；
        completeApprove(0);
    }
}

// 加载部门的交接人；
function loadDeptUser(dataValue) {
    if (dataValue != null && dataValue.length > 0) {
        layui.use(["form"], function () {
            var deptObj = $("#approveForm select[name='conEmpId']");
            var oldDept = $("#approveForm input[name='conEmpId']").val();
            if (oldDept.length <= 0) {
                oldDept = dataValue[0].id;
            }
            var oldDeptName;
            for (var i = 0; i < dataValue.length; i++) {
                if (oldDept == dataValue[i].id) {
                    oldDeptName = dataValue[i].name;
                    deptObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                } else {
                    deptObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
            }
            // 设置用户名；
            var deptNameObj = $("#approveForm").find("input[name='conEmpName']");
            if (!oldDeptName) {
                oldDeptName = dataValue[0].name;
            }
            deptNameObj.val(oldDeptName);
            // 初始化；
            layui.form.render();

            // 下拉框的onchange事件；
            layui.form.on("select(conEmpId)", function (userData) {
                // 更新隐藏域；
                deptNameObj.val(getLayUISelectText(userData));
            });
        });
    } else {
        alertMessage("该部门没有其他员工可以交接工作，请联系管理员。");
        $("#agreeEmploy").hide();
    }
}

// 加载人事专员；
function loadPerosonUser(dataValue) {
    if (dataValue != null && dataValue.length > 0) {
        layui.use(["form"], function () {
            var personObj = $("#approveForm select[name='conPersonalId']");
            var oldPerson = $("#approveForm input[name='conEmpId']").val();
            if (oldPerson.length <= 0) {
                oldPerson = dataValue[0].id;
            }
            var oldPersonName;
            for (var i = 0; i < dataValue.length; i++) {
                if (oldPerson == dataValue[i].id) {
                    oldPersonName = dataValue[i].name;
                    personObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                } else {
                    personObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
            }
            // 设置用户名；
            var personNameObj = $("#approveForm").find("input[name='conPersonalName']");
            if (!oldPersonName) {
                oldPersonName = dataValue[0].name;
            }
            personNameObj.val(oldPersonName);
            // 初始化；
            layui.form.render();

            // 下拉框的onchange事件；
            layui.form.on("select(conPersonalId)", function (userData) {
                // 更新隐藏域；
                personNameObj.val(getLayUISelectText(userData));
            });
        });
    } else {
        alertMessage("该部门没有人事专员，请联系管理员。");
        $("#agreeEmploy").hide();
    }
}

// 加载财务经理；
function loadFinanceUser(dataValue) {
    if (dataValue != null && dataValue.length > 0) {
        layui.use(["form"], function () {
            var financeObj = $("#approveForm select[name='conFinanceId']");
            var oldFinance = $("#approveForm input[name='conEmpId']").val();
            if (oldFinance.length <= 0) {
                oldFinance = dataValue[0].id;
            }
            var oldFinanceName;
            for (var i = 0; i < dataValue.length; i++) {
                if (oldFinance == dataValue[i].id) {
                    oldFinanceName = dataValue[i].name;
                    financeObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                } else {
                    financeObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
            }
            // 设置用户名；
            var financeNameObj = $("#approveForm").find("input[name='conFinanceName']");
            if (!oldFinanceName) {
                oldFinanceName = dataValue[0].name;
            }
            financeNameObj.val(oldFinanceName);
            // 初始化；
            layui.form.render();

            // 下拉框的onchange事件；
            layui.form.on("select(conFinanceId)", function (userData) {
                // 更新隐藏域；
                financeNameObj.val(getLayUISelectText(userData));
            });
        });
    } else {
        alertMessage("该部门没有财务经理，请联系管理员。");
        $("#agreeEmploy").hide();
    }
}

// 工作交接的复选框选中；
function setDataChecked(inputName, checkData) {
    // 先清空选中；
    var companyArray;
    if (checkData) {
        companyArray = checkData.split(",");
    }
    // 设置选中；
    setFormCheckBoxChecked($("#approveForm input[name='" + inputName + "']"), companyArray);
}