var arrayNewList = new Array();
var borrowList = new Array();
var mediaUserPlateMap =[];
var saleSum = 0;
var outgoSum = 0;

function del(id) {
    var lock = true;
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if (lock) {
            lock = false;
            $.ajax({
                type: "post",
                url: baseUrl + "/outgo/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message);
                    } else if (data.code == 1002) {
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            });
        }
    }, function () {
        return;
    });
};

//申请人撤回
function returnBack(taskId, itemId) {
    var lock = true;
    layer.confirm('确认撤回申请？', {
        btn: ['撤回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if (lock) {
            lock = false;
            $.ajax({
                type: "post",
                url: baseUrl + "/process/withdraw",    //向后端请求数据的url
                data: {taskId: taskId, itemId: itemId},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else if (data.code == 1002) {
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            });
        }
    }, function () {
        return;
    });
}

//state=2用这个撤回，不还原稿件和借款的状态
function CWReject(t) {
    var lock = true;
    var id = $("#editForm #id").val();
    layer.confirm('确认撤回？', {
        btn: ['撤回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#confirm");//锁定按钮，防止重复提交
        startModal("#CWReject2");//锁定按钮，防止重复提交
        if (lock) {
            lock = false;
            $.ajax({
                type: "post",
                url: baseUrl + "/outgo/CWReject",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        $("#editModal").modal("hide");
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message);
                        approveTask($("#taskId").val(), 0, t.id, $("#desc1").val())
                    } else if (data.code == 1002) {
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                },
                error: function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        }
    }, function () {
        return;
    });
};

//state=1||state=12用这个撤回，会还原稿件状态和借款状态
function CWReturn(t) {
    var lock = true;
    var id = $("#editForm #id").val();
    layer.confirm('确认撤回？', {
        btn: ['撤回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#review");//锁定按钮，防止重复提交
        startModal("#CWReturn");//锁定按钮，防止重复提交
        if (lock) {
            lock = false;
            $.ajax({
                type: "post",
                url: baseUrl + "/outgo/CWReturn",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        $("#editModal").modal("hide");
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message);
                        approveTask($("#taskId").val(), 0, t.id, $("#desc2").val())
                    } else if (data.code == 1002) {
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                },
                error: function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        }
    }, function () {
        return;
    });
};

//唤醒对象
var workupObj = {
    //请款标题, 收款人, 收款账号, 收款开户行, 期望付款日期, 实际请款金额, 请款备注, 实际请款成本,请款成本抹零,实际请款抹零
    // 是否开票,税点,税金,进票抬头，实际出款金额,发票编号,票面税点,普票/专票
    editKeyList: ["title", "accountName", "accountBankNo", "accountBankName", "expertPayTime", "applyAmount", "remark",
        "actualCost", "costEraseAmount", "outgoEraseAmount", "invoiceFlag", "outgoTax", "taxAmount", "invoiceRise", "payAmount",
        "invoiceCode", "invoiceTax", "invoiceType"],
    oldOutgoEntiry: {},
    pass: function (t) {
        approveTask($("#workupPass").attr("data-taskId"), 1, t.id, $("#workupDesc").val());
    },
    reject: function (t) {
        approveTask($("#workupReject").attr("data-taskId"), 0, t.id, $("#workupDesc").val());
    },
    showHistory: function (t) {
        var workupNode = new WorkupProcessCompont({
            id: $(t).attr("data-id"),
            historyModal: "#historyModal",
            target: "#history"
        });
        workupNode.showHistory();
    }
}

//请款流程唤醒
function workup(row, type) {
    if (type == 1) {
        //所有改变数据清空
        $.each(workupObj.editKeyList, function (i, c) {
            $("#" + c + "-errorSelf").css("display", "none");
            $("#" + c + "-errorSelf").text("");
        });
        $("#actualCombined-errorSelf").css("display", "none");
        $("#actualCombined-errorSelf").text("");
        view(row.id, 23, row.mediaTypeId, row.parentType);
    } else {
        var workupNode = new WorkupProcessCompont({
            id: row.id,
            historyModal: "#historyModal",
            target: "#history",
            searchBtnId: "#querySearch"
        });
        workupNode.showHistory();
    }
}

//判断请款稿件列表是否存在正常的稿件
function getOutgoFlag() {
    //判断请款稿件列表是否存在正常的稿件
    var outgoFlag = false; //默认不可以唤醒请款
    var rows = $("#selected_article_table_logs").jqGrid('getRowData');
    if (rows && rows.length > 0) {
        for (var z = 0; z < rows.length; z++) {
            if (rows[z].state1 != -9) {
                outgoFlag = true;
                break; //存在一个正常的就可以请款
            }
        }
    }
    return outgoFlag;
}

//唤醒处理
function workupHander() {
    if (!getOutgoFlag()) {
        swal("很抱歉，不能唤醒，请款稿件列表无数据或者所有稿件被删除！");
        return;
    }
    //应付合计
    var sumOutgo = $("#editForm [name = 'sumOutgo']").val() == "" ? 0 : $("#editForm [name = 'sumOutgo']").val();
    //实际请款成本
    var actualCost = $("#editForm [name='actualCost']").val() == "" ? 0 : $("#editForm [name = 'actualCost']").val();
    if (parseFloat(actualCost) > parseFloat(sumOutgo)) {
        swal("实际请款成本要小于等于合计成本！");
        // $("#actualCost").val(0);
        return;
    }
    var title = $("#title").val();
    var fundAmount = parseFloat($("#fundAmount").val());
    var applyAmount = parseFloat($("#applyAmount").val());
    if ((title.indexOf("消账") > -1 || title.indexOf("销账") > -1 || title.indexOf("备用金") > -1) && fundAmount == 0) {
        swal("备用金或销账时备用金额必须大于0！");
        return;
    }
    if (fundAmount > applyAmount) {
        swal("备用金额不能大于实际请款金额！");
        return;
    }
    if (fundAmount > 0 && fundAmount != applyAmount) {
        swal("选择的备用金必须等于实际请款金额");
        return;
    }
    if ($("#invoiceFlag1").val() == 1) {
        if ($("#outgoTax").val() == 0) {
            swal("请选择税点!");
            return;
        }
        //税点可以为0 所以注释。
        // if($("#outgoTax").val()==3 && ($("#outgoTax1").val()=="" || $("#outgoTax1").val()==0)){
        //     swal("请输入税点!");
        //     return;
        // }
        var applyMoney = $("#applyAmount").val();
        var sum = (parseFloat($("#actualCost").val())+parseFloat($("#taxAmount").val())).toFixed(2);
        if (applyMoney > sum) {
            swal("实际请款金额要小于等于请款成本加税金！");
            return;
        }
    } else {
        if (applyAmount > parseFloat(actualCost)) {
            swal("实际请款金额要小于等于请款成本！");
            return;
        }
    }

    //实际出款金额计算= 请款金额 - 备用金
    $("#payAmount").val(applyAmount - fundAmount);
    if ($("#editForm").valid()) {
        var formParam = $("#editForm").serializeForm();
        if (formParam.invoiceType == null && formParam.invoiceType == undefined) {
            formParam.invoiceType = $("#editForm input[name='invoiceType']").val();
        }

        //如果有后缀
        if ($("#titleSuffix").val()) {
            formParam.title = formParam.title + "(" + $("#titleSuffix").val() + ")";
        }

        var param = {id: formParam.id, parentType: formParam.parentType};
        if ($("#mediaGroupLeader1").attr("data-divId") == "hide") {
            param.mediaGroupLeader = "";
        } else {
            param.mediaGroupLeader = $("#mediaGroupLeader1").val();
            param.groupLeaderFlag = 1; //组长下拉列表展示，需要根据唤醒节点判断是否需要选择审核人
        }
        if (!formParam.invoiceFlag) {
            formParam.invoiceFlag = $("#invoiceFlag1").val();
        }
        var filterNode = []; //唤醒时需要过滤掉的节点
        //当不开票时，需要将回填发票节点过滤，同时清空回填发票信息
        if (formParam.invoiceFlag == 2) {
            filterNode.push("mediumBackfill");
            formParam.invoiceType = "";
            formParam.invoiceCode = "";
            formParam.invoiceTax = "";
        }
        if (formParam.outgoTax == 3) {
            formParam.outgoTax = parseFloat($("#outgoTax1").val()) / 100;
        }
        //判断那几个值是否有编辑
        for (var key in formParam) {
            if (workupObj.editKeyList.contains(key) && workupObj.oldOutgoEntiry) {
                if (workupObj.oldOutgoEntiry[key] != formParam[key]) {
                    param[key] = formParam[key];
                }
            }
        }
        var processType = "";
        var processName = "";
        var checkBMoney = null; //获取网关比较值
        var coma = processCompanyCode();
        if (coma && coma.contains(user.companyCode)) {
            processType = "24";
            processName = "河图请款流程";
            //如果业务员非配置的公司
            if (!coma.contains(workupObj.oldOutgoEntiry.companyCode)) {
                if (formParam.parentType == 1) {
                    checkBMoney = 1000;
                } else {
                    checkBMoney = 5000;
                }
            }
        } else {
            //父级板块：1-网络、2-新媒体
            if (formParam.parentType == 1) {
                if (formParam.mediaTypeId == 8 || formParam.mediaTypeId == 317) {
                    processType = "22";
                    processName = "自媒体请款流程";
                } else {
                    processType = "3";
                    processName = "网络请款流程";
                }
                //如果业务员为配置的公司，则一定走财务总监，否则金额判断
                if (!(coma && coma.contains(workupObj.oldOutgoEntiry.companyCode))) {
                    checkBMoney = 1000;
                }
            } else {
                if (formParam.mediaTypeId == 3) {
                    processType = "25";
                    processName = "报纸请款流程";
                } else {
                    processType = "3";
                    processName = "新媒体请款流程";
                }
                //如果业务员为配置的公司，则一定走财务总监，否则金额判断
                if (!(coma && coma.contains(workupObj.oldOutgoEntiry.companyCode))) {
                    checkBMoney = 5000;
                }
            }
        }
        //如果出现改变金额，并且金额改变会影响网关gateCheckB时，不让其选择财务以及之后的节点
        if (param["applyAmount"] && checkBMoney) {
            //如果出现改变金额，并且金额改变会影响网关gateCheckB时，不让其选择任何节点
            if ((workupObj.oldOutgoEntiry["applyAmount"] <= checkBMoney && param["applyAmount"] > checkBMoney) || (workupObj.oldOutgoEntiry["applyAmount"] > checkBMoney && param["applyAmount"] <= checkBMoney)) {
                filterNode.push("AllNode");
            }
        }

        var workupNode = new WorkupProcessCompont({
            id: $("#editForm").find("#id").val(),
            name: $("#editForm").find("#title").val(),
            processType: processType,
            processName: processName,
            param: param,
            filterNode: filterNode,
            url: "/outgo/workupOutgo",
            historyModal: "#historyModal",
            target: "#history",
            startModal: "#editModal",
            taskId: $("#editForm").find("#taskId").val(),
            searchBtnId: "#querySearch"
        })
        workupNode.render();
    }
}

//审核记录查看
function showHistory(id, mediaTypeId, parentType, creatorcompanyCode, processType) {
    var process;
    var coma = processCompanyCode();
    if (coma && coma.contains(creatorcompanyCode)) {
        process = 24;
    } else {
        if (processType) {
            process = processType
        } else {
            //1是网络
            if (parentType == 1) {
                if (mediaTypeId && mediaTypeId == 8) {
                    process = 22;
                } else {
                    process = 3;
                }
            }
            //2是新媒体
            else {
                if ((mediaTypeId && mediaTypeId == 3)) {
                    process = 25;
                } else {
                    process = 3;
                }
            }
        }
    }
    //process详见IProcess
    $("#historyModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/process/history",
        data: {dataId: id, process: process},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                $("#history").empty();
                if (data.data.data != null) {
                    var html = "";
                    html += "<div style='position: relative;z-index: 10;'>" +
                        "<div class='form-control'>" +
                        "<div class='col-sm-3 text-center'>审核节点</div>" +
                        "<div class='col-sm-3 text-center'>操作人</div>" +
                        "<div class='col-sm-3 text-center'>操作详情</div>" +
                        "<div class='col-sm-3 text-center'>操作时间</div></div>";
                    for (var i = 0; i < data.data.data.length; i++) {
                        html += "<div class='form-control'>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>" +
                            "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>" +
                            "</div>";
                    }
                    if (process == 24) {
                        html += "</div><div class='col-sm-12 text-center' style='position:relative ' ><img src='/process/getImage?dataId=" + id + "&process=" + process + "&t=" + new Date().getTime() + "' style='width: 136%; margin-left: -193px; margin-top: -100px; margin-bottom: -100px; '/></div>";
                    } else if (process == 22) {
                        html += "</div><div class='col-sm-12 text-center' style='position:relative ' ><img src='/process/getImage?dataId=" + id + "&process=" + process + "&t=" + new Date().getTime() + "' style='width: 136%; margin-left: -193px; margin-top: -100px; margin-bottom: -100px; '/></div>";
                    } else if (process == 25) {
                        html += "</div><div class='col-sm-12 text-center' style='position:relative ' ><img src='/process/getImage?dataId=" + id + "&process=" + process + "&t=" + new Date().getTime() + "' style='width: 136%; margin-left: -193px; margin-top: -100px; margin-bottom: -100px; '/></div>";
                    } else {
                        html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=" + process + "&t=" + new Date().getTime() + "' style='width: 130%; margin-left: -165px; margin-top: -100px; margin-bottom: -100px; '/></div>";
                    }
                    $("#history").append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
};


// 用户银行账户 map
var accountMap = new Map();

/**
 * 根据map改变开户行的值
 */
function changeAccountBankName() {
    var key = $("#accountBankNo").val();
    $("#accountBankName").val(accountMap.get(key))
}

/**
 * 获取银行账号下拉数据，并填充map数据（key为收款账号，value 为收款开户行）
 */
function getAccountBankNos() {
    $.ajax({
        type: "get",
        url: baseUrl + "/outgo/getAccountBankData",
        dataType: "json",
        success: function (data) {
            accountMap = new Map;
            var html = "";
            for (var i = 0; i < data.length; i++) {
                accountMap.set(data[i].receivingAccount, data[i].receivingBank)
                html += '<option value="' + data[i].receivingAccount + '">'
            }
            $("#accountBankNos").html(html);
        }
    });

}

//已废弃：flag=1审核页面，flag=-1查看页面,flag=0稿件管理跳转到查看页面,flag=2对公账户财务负责人复核出款，flag=3出纳出款
/**
 * flag 的值对应的页面
 * 0 编辑页面
 * 1 双击查看页面
 * 2 其他页面跳转的查看页面
 * 3 审核页面
 * 4 出纳出款CWReject
 * 5 复核出款CWReturn
 * 6 mediaTypeId 媒体板块id
 * 7 parentType 父级板块ID
 */
function view(id, flag,mediaTypeId, parentType) {
    layui.use('form',function () {
        var form = layui.form;
        form.on('switch(switchTest)',function (data) {
            if(data.elem.checked){
                $("#editForm [name = 'accountBinding']").val(1);
            }else {
                $("#editForm [name = 'accountBinding']").val(0);
            }
        });
    });
    $("#flag").val(flag);
    getAccountBankNos();
    $("#editModal").modal({backdrop: "static"});
    $("#mediaGroupLeader").removeAttrs("data-divId");
    // //刷新select 下拉框
    // layui.use(["form"], function () {
    //     layui.form.render();
    // });
    $("#mediaGroupLeader").empty();
    $(".firstDiv").hide();
    $(".secondDiv").hide();
    $(".thirdDiv").show();
    $("#back").hide();
    $("#thirdStep").click();
    document.getElementById("editForm").reset();
    $("#editForm").find("input").removeClass('error');
    $("#editForm").validate().resetForm();
    $("#editModal input:radio").removeAttr("checked");
    $("#editModal input:radio").parent().removeClass("checked");
    if (flag == 0) {//编辑
        $("#backMedia").hide();
        $("#navTabs").show();
        $("#viewAndConfirm").hide();
        $("#viewDiv").hide();
        $("#editFooter").show();
        $("#viewFooter").hide();
        $("#workupEditFooter").hide();
        $("#confirmFooter").hide();
        $("#selectAccountBtnDiv").show();
        $("#selectBorrowBtnDiv").show();
        $("#invoiceInformation").hide();
        $("#editForm [name='title']").attr("readonly", false);
        $("#editForm [name='accountName']").attr("readonly", false);
        $("#editForm [name='accountBankNo']").attr("readonly", false);
        $("#editForm [name='accountBankName']").attr("readonly", false);
        $("#editForm [name='actualCost']").attr("readonly", false);
        $("#editForm [name='applyAmount']").attr("readonly", false);
        $("#editForm select[name='outgoTax']").attr("disabled", false);
        $("#editForm input[name='invoiceFlag']").attr("disabled", false);
        $("#outgoTax1").attr("readonly", false);
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-3");
        $("#editModal .modal-title").html("请款信息");
        $("#hideOnEdit").hide();
    } else if (flag == 23) { //唤醒编辑
        $("#navTabs").show();
        $("#viewAndConfirm").hide();
        $("#viewDiv").hide();
        $("#editFooter").hide();
        $("#workupEditFooter").show();
        $("#viewFooter").hide();
        $("#confirmFooter").hide();
        $("#selectAccountBtnDiv").show();
        $("#backMedia").hide();
        $("#invoiceInformation").hide();
        $("#selectBorrowBtnDiv").hide();
        $("#editForm [name='title']").attr("readonly", false);
        $("#editForm [name='actualCost']").attr("readonly", false);
        $("#editForm [name='applyAmount']").attr("readonly", false);
        $("#editForm select[name='outgoTax']").attr("disabled", false);
        $("#editForm input[name='invoiceFlag']").attr("disabled", false);
        $("#outgoTax1").attr("readonly", false);
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-3");
        $("#editModal .modal-title").html("唤醒编辑");
        $("#mediaGroupLeader1").empty();//清空下拉列表值
        mediaGroupLeader1("#mediaGroupLeader1", mediaTypeId);
        $("#hideOnEdit").hide();
    } else if (flag == 7) {
        $("#confirmFooter").hide();
        $("#auditFalse").hide();
        $("#viewFooter").hide();
        $("#editFooter").hide();
        $("#workupEditFooter").hide();
        $("#backMedia").show();
        $("#hideOnEdit").hide();
        $("#invoiceInformation").show();
        $("#selectAccountBtnDiv").hide();
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-3");
        $("#selectBorrowBtnDiv").hide();
        $("#editForm input[name='invoiceType'][value='1']").iCheck("check");
        $("#editForm input[name='invoiceType'][value='1']").parent().iCheck("check");
        $("#editForm [name='title']").attr("readonly", "readonly");
        $("#editForm [name='accountName']").attr("readonly", "readonly");
        $("#editForm [name='accountBankNo']").attr("readonly", "readonly");
        $("#editForm [name='accountBankName']").attr("readonly", "readonly");
        $("#editForm [name='actualCost']").attr("readonly", "readonly");
        $("#editForm [name='applyAmount']").attr("readonly", "readonly");
        $("#editForm select[name='outgoTax']").attr("disabled", true);
        $("#editForm input[name='invoiceFlag']").attr("disabled", true);
        $("#outgoTax1").attr("readonly", "readonly");

    } else {
        $("#backMedia").hide();
        $("#navTabs").hide();
        $("#editFooter").hide();
        $("#workupEditFooter").hide();
        $("#workupEditFooter").hide();
        $("#selectAccountBtnDiv").hide();
        $("#selectBorrowBtnDiv").hide();
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-3");
        $("#hideOnEdit").show();
        if (flag == 4) {//出款
            $("#viewFooter").hide();
            $("#confirmFooter").show();
            $("#editForm input[name='invoiceFlag']").attr("disabled", true);
            $("#editForm select[name='outgoTax']").attr("disabled", true);
            $("#outgoTax1").attr("readonly", "readonly");
        } else {
            if (flag == 1) {//查看
                var type = getQueryString("type");
                if (type == 2) {
                    $("#back").show();
                }
                var returnType = getQueryString("returnType");
                if (returnType == 5) {
                    $("#confirmFooter").hide();
                    $("#auditTrue").show();
                    $("#workupAuditTrue").hide();
                    $("#auditFalse").hide();
                    $("#reject1").hide();
                    $("#pass1").hide();
                    $("#showHistory1").hide()
                } else {
                    $("#review").hide();
                    $("#confirmFooter").hide();
                    $("#viewFooter").show();
                    $("#auditTrue").hide();
                    $("#workupAuditTrue").hide();
                    $("#auditFalse").show();
                    $("#changeAccountBtn").hide();
                    $("#closeModal").show();//查看页面返回按钮隐藏
                    $("#CWReturn").hide();
                    $("#opinion").hide();
                    $("#goback").hide();
                }
                $("#editForm input[name='invoiceFlag']").attr("disabled", true);
            } else if (flag == -1) {
                $("#confirmFooter").hide();
                $("#auditTrue").show();
                $("#workupAuditTrue").hide();
                $("#auditFalse").hide();
                $("#reject1").hide();
                $("#pass1").hide();
                $("#showHistory1").hide();
            } else if (flag == 2) {//跳转的查看
                $("#viewFooter").show();
                $("#auditTrue").hide();
                $("#workupAuditTrue").hide();
                $("#auditFalse").show();
                $("#CWReturn").hide();
                $("#closeModal").hide();
                $("#review").hide();
                $("#goback").show();
                $("#confirmFooter").hide();
                $("#changeAccountBtn").hide();
            } else if (flag == 3) {//审批查看
                $("#viewFooter").show();
                $("#editForm input[name='invoiceFlag']").attr("disabled", true);
                $("#editForm select[name='outgoTax']").attr("disabled", true);
                $("#outgoTax1").attr("readonly", "readonly");
                if (getQueryString("workupFlag") == 23) {
                    $("#auditTrue").hide();
                    $("#auditFalse").hide();
                    $("#workupAuditTrue").show();
                    $("#workupIcon").show();
                } else {
                    $("#auditTrue").show();
                    $("#auditFalse").hide();
                    $("#workupAuditTrue").hide();
                    $("#workupIcon").hide();
                }
                $("#confirmFooter").hide();
            } else if (flag == 5) {//确认公账出款
                $("#editForm input[name='invoiceFlag']").attr("disabled", true);
                $("#editForm select[name='outgoTax']").attr("disabled", true);
                $("#outgoTax1").attr("readonly", "readonly");
                $("#viewFooter").show();
                $("#auditTrue").hide();
                $("#workupAuditTrue").hide();
                $("#auditFalse").show();
                $("#goback").hide();
                $("#closeModal").show();
                $("#review").show();
                $("#CWReturn").show();
                $("#opinion").show();
                $("#confirmFooter").hide();
                $("#changeAccountBtn").hide();
            } else if (flag == 6) {//更改出款账户
                $("#viewFooter").show();
                $("#auditTrue").hide();
                $("#workupAuditTrue").hide();
                $("#auditFalse").show();
                $("#goback").hide();
                $("#closeModal").show();
                $("#review").hide();
                $("#CWReturn").hide();
                $("#opinion").hide();
                $("#confirmFooter").hide();
                $("#changeAccountBtn").show();
            }
        }
    }
    $.ajax({
        type: "post",
        url: baseUrl + "/outgo/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                if (flag == 23) {
                    workupObj.oldOutgoEntiry = data.data.entity;
                }
                if (mediaTypeId != null && flag == 0) {
                    var mt = mediaType(mediaTypeId);
                    mediaGroupLeader1("#mediaGroupLeader", mediaTypeId);
                } else if (mediaTypeId == null && flag == 0) {
                    var mt = mediaType(data.data.entity['mediaTypeId']);
                    mediaGroupLeader1("#mediaGroupLeader", data.data.entity['mediaTypeId']);
                }

                for (var attr in data.data.entity) {
                    $("#editForm [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                    if (attr == "mediaTypeId") {
                        $("#mediaTypeId").val(data.data.entity[attr]);
                    }
                    if (attr == "creatorcompanyCode") {
                        $("#creatorcompanyCode").val(data.data.entity[attr]);
                    }
                    if (attr == "parentType") {
                        $("#parentType").val(data.data.entity[attr]);
                    }
                    if (attr == "processType") {
                        $("#processType").val(data.data.entity[attr]);
                    }
                    if (attr == "invoiceFlag") {
                        //changeLicence(data.data.entity[attr]);
                        $("#editForm input[name='invoiceFlag'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                        $("#editForm input[name='invoiceFlag'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                        //如果是企业供应商必须开票，个体供应商可随意选择(编辑和唤醒)
                        if (flag == 0 || flag == 23) {
                            var supplierNature = data.data.entity["supplierNature"] || "";
                            //个体供应商
                            if (supplierNature == "1") {
                                $("#editForm input[name='invoiceFlag']").attr("disabled", false);
                            } else {
                                $("#editForm input[name='invoiceFlag'][value='2']").attr("disabled", true);//企业供应商必须开票，关闭不开票选项
                            }
                        } else {
                            $("#editForm input[name='invoiceFlag']").attr("disabled", true);
                        }

                        if (data.data.entity[attr] == 1) {
                            $(".invoice").show();
                            $("#invoiceFlag1").val(1);
                        } else {
                            $(".invoice").hide();
                            $("#invoiceFlag1").val(2);
                            // $("#editForm [name='outgoTax']").val(0);
                            // $("#taxAmount").val(0);
                            // $("#invoiceRise").val("");
                            // $("#applyAmount").val($("#actualCost").val());
                            // $("#outgoEraseAmount").val(0);
                            // $("#invoiceFlag1").val(2);
                            // $("#actualCombined").val($("#costEraseAmount").val())
                        }
                    }
                    if (attr == "supplierPhone"){
                        // var fl = false;
                        // var plateId = data.data.entity["mediaTypeId"];
                        // if (plateId){
                        //     if (mediaUserPlateMap.contains(plateId)){
                        //         fl= true;
                        //     }
                        // }
                        var value =  data.data.entity[attr];
                        // if(( data.data.entity['supplierCreator'] == user.id )||(fl && hasRoleMJBZ()) || (fl && hasRoleMJZZ()) || (fl && hasRoleMJZJ()) || hasRoleCW() ){
                        //     $("#supplierPhone").val(value) ;
                        // }else {
                            if(value.length >= 11){
                                var start = value.length > 11 ? "*****" : "****";
                                $("#supplierPhone").val(value.substring(0, 3) + start + value.substring(value.length - 4, value.length)) ;
                            }else if(value.length >= 3){
                               $("#supplierPhone").val(value[0] + "***" + value[value.length - 1]);
                            }else {
                               $("#supplierPhone").val("**") ;
                            }


                    }
                    if (attr == 'accountBinding'){
                        if (data.data.entity['accountBinding']==1){
                            // $("#accountBinding").prop("checked",true);
                            $("input[type ='checkbox'][name ='accountBinding']").prop("checked", true);
                        }else {
                            $("input[type ='checkbox'][name ='accountBinding']").prop("checked", false);
                        }
                        layui.use(["form"], function () {
                            layui.form.render("checkbox");
                        });
                    }

                    if(attr == "invoiceType" && flag!=7){
                        $("#editForm input[name='invoiceType'][value='" + data.data.entity[attr] + "']").iCheck("check");
                        $("#editForm input[name='invoiceType'][value='" + data.data.entity[attr] + "']").parent().iCheck("check");
                    }
                    if (attr == "outgoTax") {
                        if (data.data.entity[attr] != 0.03 && data.data.entity[attr] != 0.06) {
                            $("#outgoTax").val(3);
                            if (isNaN(parseFloat(data.data.entity[attr]) * 100)) {
                                $("#outgoTax1").val(0);
                            } else {
                                $("#outgoTax1").val((parseFloat(data.data.entity[attr]) * 100).toFixed(2));
                            }
                        } else {
                            $("#inputTaxPoint").hide();
                            $("#outgoTax1").val(0);
                        }
                    }
                    if (attr == "state") {
                        if ((data.data.entity[attr] == 26 || data.data.entity[attr] == 1) && flag != 23) {
                            $("#invoiceInformation").show();
                        } else {
                            $("#invoiceInformation").hide();
                        }
                    }
                    $("#actualCombined").val((parseFloat(data.data.entity['outgoEraseAmount']) + parseFloat(data.data.entity['costEraseAmount'])).toFixed(2));
                    if (flag == 0 || flag == 23 || flag == 7) {
                        if (attr == "supplierName" ||
                            attr == "supplierContactor" ||
                            attr == "code" ||
                            attr == "applyName" ||
                            attr == "deptName" ||
                            attr == "expertPayTime" ||
                            attr == "payTime" ||
                            attr == "fundAmount") {
                            $("#editForm [name=" + attr + "]").attr("readonly", "readonly");
                        }
                        $("#editForm [name=" + attr + "]").css("border", "1px solid #e5e6e7");
                        // $("#editForm [name=" + attr + "]").removeAttrs("style");
                        //对于编辑 和 唤醒编辑标题需要拆分，对于回填标题不需要
                        if (attr == "title") {
                            if (flag == 0 || flag == 23) {
                                var tmpPercent = 0;
                                if (data.data.saleSum && data.data.saleSum > 0) {
                                    tmpPercent = (data.data.saleSum - (data.data.outgoSum || 0)) * 100 / data.data.saleSum;
                                }
                                var titleSuffix = "成本:" + (data.data.outgoSum || 0) + " + 报价:" + (data.data.saleSum || 0) + " + 利润率:" + tmpPercent.toFixed(2) + "%";
                                var titlePrefix = "";
                                //重新赋值，拆分标题中的成本+报价+利率出来
                                var tmpTitle = data.data.entity[attr] || "";
                                if (tmpTitle && tmpTitle[tmpTitle.length - 1] == ")" && tmpTitle.lastIndexOf("(") > 0) {
                                    titlePrefix = tmpTitle.substring(0, tmpTitle.lastIndexOf("("));
                                    // titleSuffix = tmpTitle.substring(tmpTitle.lastIndexOf("(")+1, tmpTitle.length - 1);
                                } else {
                                    titlePrefix = tmpTitle;
                                }

                                //显示后缀
                                $("#titleSuffix").css("display", "inline-block");
                                $("#title").css("width", "64.7%");
                                $("#titleSuffix").val(titleSuffix);
                                $("#title").val(titlePrefix);
                            } else {
                                $("#titleSuffix").css("display", "none");
                                $("#title").css("width", "100%");
                            }
                        }

                        if (data.data.entity['outgoTax'] == null) {
                            $("#editForm option[value='0']").prop("selected", true);
                            // $("#editForm select[name ='outgoTax']").val(0);
                            $("#inputTaxPoint").hide();
                            $("#outgoTax1").val(0);
                        }

                    } else {
                        if (attr == "expertPayTime" || attr == "payTime") {
                            dateData = data.data.entity[attr];
                            // 判断是否为空；
                            if (dateData != null) {
                                // 解析为日期默认的格式为斜杠，需要进行处理；
                                data.data.entity[attr] = new Date(dateData.replace(/-/g, "/")).format("yyyy-MM-dd hh:mm");
                            }
                        }
                        $("#editForm [name=" + attr + "]").removeAttrs("readonly");
                        $("#editForm [name=" + attr + "]").css("border", "0");
                        // $("#editForm [name=" + attr + "]").prop("style", "border:0;");
                        //对于编辑 和 唤醒编辑标题需要拆分，对于回填标题不需要
                        if (attr == "title") {
                            $("#titleSuffix").css("display", "none");
                            $("#title").css("width", "100%");
                        }

                        //添加审批详情查看按钮的参数
                        if(flag == 3 && attr == "mediaTypeId"){
                            $("#showHistory1").attr("data-id",id);
                            $("#showHistory1").attr("data-mediaTypeId",data.data.entity[attr]);
                        }
                        if (attr == "title") {
                            var titleStr = data.data.entity[attr] == "" ? "" : data.data.entity[attr];
                            $("#editModal .modal-title").html("<span class='text-red'>" + titleStr + "</span>")
                        }
                        // 分公司只能查看供应商公司名称，不允许查看供应商联系人
                        if(attr == 'companyCode'){
                            $("#companyCodeUser").val(data.data.entity[attr]);
                            if(data.data.entity[attr]== user.companyCode||user.companyCode=='XH' ){
                                // $("#editForm [name='supplierName']").val(data.data.entity.supplierName);
                                $("#editForm [name='supplierContactor']").val(data.data.entity.supplierContactor);
                            }else{
                                // $("#editForm [name='supplierName']").val("");
                                $("#editForm [name='supplierContactor']").val("");
                            }
                        }
                        if(flag==1 && data.data.entity["state"]==23){
                            $("#invoiceInformation").show();
                        }
                        if (attr == "state") {
                            var stateStr;
                            var dataStr = data.data.entity[attr];
                            switch (dataStr) {
                                case -1 :
                                    stateStr = "审核驳回";
                                    break;
                                case 0 :
                                    stateStr = "已保存";
                                    break;
                                case 1 :
                                    stateStr = "已完成";
                                    break;
                                case 2 :
                                    stateStr = "审核通过";
                                    break;
                                case 3 :
                                    stateStr = "组长审核";
                                    break;
                                case 4 :
                                    stateStr = "部长审核";
                                    break;
                                case 5 :
                                    stateStr = "总监审核";
                                    break;
                                case 6 :
                                    stateStr = "财务总监审核";
                                    break;
                                case 7 :
                                    stateStr = "副总经理审核";
                                    break;
                                case 8 :
                                    stateStr = "总经理审核";
                                    break;
                                case 9 :
                                    stateStr = "会计确认出款";
                                    break;
                                case 10 :
                                    stateStr = "业务员确认";
                                    break;
                                case 12 :
                                    stateStr = "财务部长审核";
                                    break;
                                case 16 :
                                    stateStr ="出纳出款";
                                    break;
                                case 23 :
                                    stateStr ="唤醒中";
                                    break;
                                case 26 :
                                    stateStr ="媒介回填开票信息";
                                    break;
                                default :
                                    break;
                            }
                            if (dataStr != null) {
                                $("#editForm [name='state1']").val(stateStr);
                            }
                        }
                        //唤醒taskId添加
                        if(flag == 3 && getQueryString("workupFlag") == 23 && attr == 'workupTaskId'){
                            $("#workupHistory").attr("data-id", data.data.entity['id']);
                            $("#workupPass").attr("data-taskId", data.data.entity[attr]);
                            $("#workupReject").attr("data-taskId", data.data.entity[attr]);
                        }
                        //显示修改内容
                        if((flag == 3 || flag == 1) && getQueryString("workupFlag") == 23 && attr == 'editJson'){
                            var editContent = JSON.parse(data.data.entity[attr]);
                            if(editContent && Object.getOwnPropertyNames(editContent).length > 0){
                                for(var editKey in editContent){
                                    if(editKey == 'jumpNode'){
                                        $("#jumpNodeName").text(editContent[editKey]);
                                    }else if(editKey == 'workupReason'){
                                        $("#workupReason").text(editContent[editKey] || "无");
                                        $("#workupReason").closest("span").attr("title", "唤醒原因:"+ (editContent[editKey] || "无"));
                                    }else {
                                        if(editContent[editKey] || editContent[editKey] == '' || editContent[editKey] == 0){
                                            $("#"+editKey+"-errorSelf").css("display","block");
                                            if(editKey == "outgoTax"){
                                                $("#"+editKey+"-errorSelf").text(parseFloat(editContent[editKey]).toFixed(2) * 100 + "点");
                                            }else if(editKey == "invoiceFlag"){
                                                if(editContent[editKey] == 1){
                                                    $("#"+editKey+"-errorSelf").text("是");
                                                }else {
                                                    $("#"+editKey+"-errorSelf").text("否");
                                                }
                                            }else if(editKey == "invoiceType"){
                                                if(editContent[editKey] == 1){
                                                    $("#"+editKey+"-errorSelf").text("普票");
                                                }
                                                if(editContent[editKey] == 2){
                                                    $("#"+editKey+"-errorSelf").text("专票");
                                                }
                                            } else if (editKey == "accountBankNo" || editKey == "accountBankName") {
                                                $("#" + editKey + "-errorSelf").text((editContent[editKey] || "").trim().replace(/&nbsp;/ig, ' '));
                                            }else {
                                                $("#"+editKey+"-errorSelf").text(editContent[editKey]);
                                            }
                                        }else {
                                            $("#"+editKey+"-errorSelf").css("display","none");
                                            $("#"+editKey+"-errorSelf").text("");
                                        }
                                    }
                                }
                                //如果存在抹零金额改动、则实际抹零进行展示
                                if(editContent["costEraseAmount"] || editContent["costEraseAmount"] == 0
                                    || editContent["outgoEraseAmount"] || editContent["outgoEraseAmount"] == 0){
                                    $("#actualCombined-errorSelf").css("display","block");
                                    if(editContent["costEraseAmount"] && editContent["outgoEraseAmount"]){
                                        $("#actualCombined-errorSelf").text((parseFloat(editContent["costEraseAmount"] || 0) + parseFloat(editContent["outgoEraseAmount"] || 0)).toFixed(2));
                                    }else if(editContent["costEraseAmount"] || editContent["costEraseAmount"] == 0){
                                        $("#actualCombined-errorSelf").text((parseFloat(editContent["costEraseAmount"] || 0) + parseFloat(data.data.entity["outgoEraseAmount"] || 0)).toFixed(2));
                                    }else {
                                        $("#actualCombined-errorSelf").text((parseFloat(data.data.entity["costEraseAmount"] || 0) + parseFloat(editContent["outgoEraseAmount"] || 0)).toFixed(2));
                                    }
                                }else {
                                    $("#actualCombined-errorSelf").css("display","none");
                                    $("#actualCombined-errorSelf").text("");
                                }
                            }
                        }
                    }
                    $("#affixDiv").empty();
                    $("#affixLink").empty();
                    $("#affixLink").show();
                    if(data.data.entity["affixName"] != ""){
                        var affixName = data.data.entity["affixName"].split(',');
                        var affixLink = data.data.entity["affixLink"].split(",");
                        if (affixName.length>0 && affixLink.length>0){
                            var html = "";
                            for (var i=0 ; i<affixName.length ; i++) {
                                var filePath = affixLink[i];
                                var fileName = affixName[i];
                                html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                                html += "<a href=" + filePath + " target=_blank  download='"+fileName+"'>下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase() ;
                                var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                                var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                                if(fileName.indexOf(".")>-1){
                                    var str=fileExt + '|';
                                    if(strFilter.indexOf(str)>-1){//是图片
                                        html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                    }else{
                                        if(fileExtArray.contains(fileExt)){
                                            html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                        }
                                    }
                                }else {
                                    html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                }
                            }
                            $("#affixDiv").append(html);
                        }
                    }
                }

             //去除&nbsp;字符
             $("#editForm [name='accountBankNo']").val(($("#editForm [name='accountBankNo']").val() || "").trim().replace(/&nbsp;/ig, ' '));
             $("#editForm [name='accountBankName']").val(($("#editForm [name='accountBankName']").val() || "").trim().replace(/&nbsp;/ig, ' '));

                //如果唤醒有发票回填，则展示
                if(flag == 23){
                    if(data.data.entity["invoiceFlag"] == 1){
                        $("#invoiceInformation").show();
                    }else {
                        $("#invoiceInformation").hide();
                    }
                }
                //如果是唤醒审核，开票数据都会发生要显示
                if((flag == 3 || flag == 1) && getQueryString("workupFlag") == 23){
                    var editContent = JSON.parse(data.data.entity["editJson"]);
                    //如果原来有回填发票则展示出来
                    if(data.data.entity["invoiceFlag"] == 1){
                        $("#invoiceInformation").show();
                    }
                    //如果原来没有开票，进行唤醒也不开票，则不显示开票信息，否则显示
                    if(data.data.entity["invoiceFlag"] == 2 && (!editContent["invoiceFlag"] || editContent["invoiceFlag"] == 2)){
                        $("#outgoTaxChoose").hide();
                    }else {
                        $("#outgoTaxChoose").show();
                    }
                }
                $("#outgoId").val(data.data.entity['id']) ;
                if((flag==4 && hasRoleCWCN() && (data.data.entity['state'] == 2 || data.data.entity['state'] == 16))||(data.data.entity['state'] == 1&&flag==6&&hasRoleCWBZ())){
                    $("#viewAndConfirm").show();
                    $(".showOnView").hide();
                    $(".showOnConfirm").show();
                    $("#editForm #payTime").removeAttrs("style");
                    $("#editForm #payTime").attr("readonly","readonly");
                    if(data.data.entity['outAccountId']){
                        $("#editForm #outAccountSelect").val(data.data.entity['outAccountId']);
                        $("#editForm #outAccountIds").val(data.data.entity['outAccountId']);
                        layui.use(["form"], function () {
                            layui.form.render('select');
                        });
                    }
                }else if (data.data.entity['state'] == 1 || data.data.entity['state'] == 12 || data.data.entity['state'] == 9) {//已出款，直接显示出款文本
                    $("#viewAndConfirm").show();
                    $(".showOnView").show();
                    $(".showOnConfirm").hide();
                } else {//未出款，不显示出款账户信息
                    $("#viewAndConfirm").hide();
                }
                if(flag == 23){
                    $("#parentType").val(parentType);
                    $("#viewAndConfirm").hide(); //如果是唤醒时，该行信息不显示
                    $("#actualCost").val(data.data.entity['actualCost'] == 0 ? data.data.outgoSum: data.data.entity['actualCost'])
                    if (data.data.entity['outgoTax'] ==null){
                        $("#editForm option[value='0']").prop("selected",true);
                        // $("#editForm select[name ='outgoTax']").val(0);
                        $("#inputTaxPoint").hide();
                        $("#outgoTax1").val(0);
                    }
                    var pc = processCompanyCode();
                    if (pc && pc.contains(user.companyCode)){
                        $("#workupEditFooter .licence").show();
                        $("#mediaGroupLeader1").removeAttrs("data-divId")
                    }else {
                        if(parentType && parentType == 1){
                            if ($("#applyAmount").val() > 2000){
                                $("#workupEditFooter .licence").hide();
                                $("#mediaGroupLeader1").attr("data-divId","hide");
                            }else {
                                $("#workupEditFooter .licence").show();
                                $("#mediaGroupLeader1").removeAttrs("data-divId")
                            }
                        }else {
                            if (data.data.entity['mediaTypeId']==3){
                                if ($("#applyAmount").val() > 2000){
                                    $("#workupEditFooter .licence").hide();
                                    $("#mediaGroupLeader1").attr("data-divId","hide");
                                }else {
                                    $("#workupEditFooter .licence").show();
                                    $("#mediaGroupLeader1").removeAttrs("data-divId")
                                }
                            }else {
                                if ($("#applyAmount").val() > 2000){
                                    $("#workupEditFooter .licence").hide();
                                    $("#mediaGroupLeader1").attr("data-divId","hide");
                                }else {
                                    $("#workupEditFooter .licence").show();
                                    $("#mediaGroupLeader1").removeAttrs("data-divId");
                                }
                            }
                        }
                    }
                    layui.form.render();
                }
                if(flag == 0){
                    $("#applyAmount").val(data.data.entity['applyAmount'] == 0 ? data.data.outgoSum : data.data.entity['applyAmount']);
                    $("#actualCost").val(data.data.entity['actualCost'] == 0 ? data.data.outgoSum: data.data.entity['actualCost'])
                    // 设置公司代码；
                    if (data.data.saleSum != null) {
                        var percent = 0;
                        if (data.data.saleSum > 0) {
                            percent = (data.data.saleSum - data.data.outgoSum) * 100 / data.data.saleSum;
                        }
                        $("#percent").html(percent.toFixed(2) + "%");
                    }


                    if (data.data.entity['outgoTax'] ==null){
                        $("#editForm option[value='0']").prop("selected",true);
                        // $("#editForm select[name ='outgoTax']").val(0);
                        $("#inputTaxPoint").hide();
                        $("#outgoTax1").val(0);
                    }
                    var pc = processCompanyCode();
                    if (pc && pc.contains(user.companyCode)){
                        $(".licence").show();
                        $("#mediaGroupLeader").removeAttrs("data-divId")
                    }else {
                        if(mt == 1 && mt != null){
                            if ($("#applyAmount").val()>2000){
                                $(".licence").hide();
                                $("#mediaGroupLeader").attr("data-divId","hide");
                                //$("#thirdDiv select[name='mediaGroupLeader']").html("");
                            }else {
                                $(".licence").show();
                                $("#mediaGroupLeader").removeAttrs("data-divId");
                            }
                        }else {
                            if (mediaTypeId==3){
                                if ($("#applyAmount").val()>2000){
                                    $(".licence").hide();
                                    $("#mediaGroupLeader").attr("data-divId","hide");
                                   // $("#thirdDiv select[name='mediaGroupLeader']").html("");
                                }else {
                                    $(".licence").show();
                                    $("#mediaGroupLeader").removeAttrs("data-divId");

                                }
                            }else {
                                if ($("#applyAmount").val()>2000){
                                    $(".licence").hide();
                                    $("#mediaGroupLeader").attr("data-divId","hide");
                                   // $("#thirdDiv select[name='mediaGroupLeader']").html("");
                                }else {
                                    $(".licence").show();
                                    $("#mediaGroupLeader").removeAttrs("data-divId");
                                }
                            }

                        }
                    }
                }
                //出纳出款时算出一个默认出款金额
                if(flag == 4){
                    if ($("#payAmount").val() == 0) {
                        var applyAmount = $("#applyAmount").val() == null ? 0 : $("#applyAmount").val() ;
                        var fundAmount = $("#fundAmount").val() == null ? 0 : $("#fundAmount").val() ;
                        $("#payAmount").val(parseFloat(applyAmount) - parseFloat(fundAmount));
                    }
                    $("#editForm #outAccountName").removeAttrs("style");
                    $("#editForm #payAmount").attr("readonly","readonly");
                }

             //如果是历史供应商请款，则收款人支持手动输入，否则仅能选择
             //TODO 这里时间和上线时间保持一致
             // if (data.data.entity["supplierCreateTime"] && new Date(data.data.entity["supplierCreateTime"]) >= new Date("2020-08-30")) {
             //     $("#editForm [name='accountName']").attr("readonly", "readonly");
             //     $("#editForm [name='accountBankNo']").attr("readonly", "readonly");
             //     $("#editForm [name='accountBankName']").attr("readonly", "readonly");
             // }

                $("#sumOutgo").val(data.data.outgoSum);
                $("#saleSum").html(data.data.saleSum);
                $("#outgoSum").html(data.data.outgoSum);
                if (data.data.saleSum != null) {
                    var percent = 0;
                    if (data.data.saleSum > 0) {
                        percent = (data.data.saleSum - data.data.outgoSum) * 100 / data.data.saleSum;
                    }
                    $("#percent").html(percent.toFixed(2) + "%");
                }
                $("#borrowInfo").empty();
                if (data.data.list.length > 0) {
                    html = '<div><h3>备用金扣除详情</h3></div><table class="table table-bordered" style="text-align: center"><thead>' +
                        '<th style="text-align:center;vertical-align:middle;">借款编号</th>' +
                        '<th style="text-align:center;vertical-align:middle;">借款标题</th>' +
                        '<th style="text-align:center;vertical-align:middle;">借款类型</th>' +
                        '<th style="text-align:center;vertical-align:middle;">借款人</th>' +
                        '<th style="text-align:center;vertical-align:middle;">所属部门</th>' +
                        '<th style="text-align:center;vertical-align:middle;">借款金额</th>' +
                        '<th style="text-align:center;vertical-align:middle;">已还金额</th>' +
                        '<th style="text-align:center;vertical-align:middle;">未还金额</th>' +
                        '<th style="text-align:center;vertical-align:middle;">备用金金额</th>' +
                        '</thead>';
                    for (var i = 0; i < data.data.list.length; i++) {
                        var typeStr = data.data.list[i]['type'] == 0 ? "备用金" : "其它";
                        html += '<tr><td>' + data.data.list[i]['code'] + '</td>' +
                            '<td>' + data.data.list[i]['title'] + '</td>' +
                            '<td>' + typeStr + '</td>' +
                            '<td>' + data.data.list[i]['apply_name'] + '</td>' +
                            '<td>' + data.data.list[i]['dept_name'] + '</td>' +
                            '<td>' + data.data.list[i]['apply_amount'] + '</td>' +
                            '<td>' + data.data.list[i]['repay_amount'] + '</td>' +
                            '<td>' + data.data.list[i]['remain_amount'] + '</td>' +
                            '<td>' + data.data.list[i]['amount'] + '</td></tr>';
                    }
                    html += '</tbody></table>';
                    $("#borrowInfo").append(html);
                }
                $("#selected_article_table_logs").jqGrid('setGridParam', {
                    datatype: 'json',
                    postData: {id: id}, //发送数据
                }).trigger("reloadGrid"); //重新载入
                resize("#selected_article_table_logs");
            } else if(data.code == 1002){
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
                $("#editModal").modal("hide");
            } else {
                if (getResCode(data))
                    return;
            }
        }

    });
};


//申请金额不能大于稿件请款金额合计
// function  checkAmount() {
//     let applyAmount = $("#applyAmount").val();
//     let sumOutgo=$("#sumOutgo").val();
//
//     if (parseFloat(applyAmount)>parseFloat(sumOutgo)){
//         swal("输入金额要小于应付合计");
//         $("#applyAmount").val(sumOutgo) ;
//     }
// }

//财务负责人确认出款
function checkBtoB() {
        var lock = true ;
        layer.confirm('确认出款？', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#review");//锁定按钮，防止重复提交
            startModal("#CWReturn");//锁定按钮，防止重复提交
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/outgo/checkBtoB",    //向后端请求数据的url
                    data: {id: $("#id").val(),desc:$("#desc2").val()},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();//解锁按钮锁定
                        if (data.code == 200) {
                            $("#editModal").modal("hide");
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            swal(data.data.message);
                        } else if(data.code == 1002){
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    },
                    error: function () {
                        Ladda.stopAll();//隐藏加载按钮
                    }
                });
            }
        }, function () {
            return;
        });
}

function saveStepOne() {
  //  mediaGroupLeader1("#mediaGroupLeader",$("#media_type_id").val());
    var lock = true;
    startModal("#backStepOne");//锁定按钮，防止重复提交
    startModal("#selectArticle");//锁定按钮，防止重复提交
    startModal("#cancelArticle");//锁定按钮，防止重复提交
    layer.confirm('已选定稿件和供应商？确定后不能更改！', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/outgo/saveStepOne",    //向后端请求数据的url
                dataType: "json",
                data: $("#secondForm").serializeJson(),
                success: function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    if (data.code == 200) {
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        //var mt = mediaType($("#media_type_id").val());
                        view(data.data.entity.id,0,$("#media_type_id").val());
                    } else if(data.code == 1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                },
                error: function () {
                    Ladda.stopAll();
                }
            });
        }
    }, function () {
        Ladda.stopAll();   //解锁按钮锁定
        return;
    });
};

//初始化业务部门
function getDept(){
    var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
    var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
    var deptDiv = document.getElementById("deptDiv");
    //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
    if(((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'MJ'|| user.dept.code == 'GL')) || user.dept.code == 'CW'){
        deptDiv.style.display = 'block';
        $("#selDept").click(function () {
            $("#deptModal").modal({backdrop: "static"});
        });
        $('#treeview').treeview({
            data: [getTreeData(isZC())],
            onNodeSelected: function (event, data) {
                $("#companyCode1").val("");//每次选择时，先清空
                $("#deptId1").val("");//每次选择时，先清空
                $("#chooseDeptName").val(data.text);
                $("#deptModal").modal('hide');
                $("#deptId1").val(data.id);
                $("#companyCode1").val(data.companyCode);
                $("#businessUserId").empty();//初始化
                $("#businessUserId").append('<option value="">全部</option>');
            }
        });
        $("#cleanDept").click(function () {
            $("#companyCode1").val("");//清空
            $("#businessUserId").empty();//初始化
            $("#businessUserId").append('<option value="">全部</option>');
            $("#deptId1").val("");
            $("#chooseDeptName").val("");
        });
    }
}

//判断当前用户是否总裁
var isZC = function () {
    var roles = user.roles;//获取用户角色
    var isZC = false;//是否总裁角色
    if(roles){
        for(var i=0; i < roles.length; i++){
            if(roles[i].code == 'ZC' || roles[i].code == 'FZC'){
                isZC = true;
                break;
            }
        }
    }
    return isZC;
}

var requestData = function (data, url, requestType,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: false,
        success: callBackFun
    });
}

//获取部门树数据
function getTreeData() {
    var deptTreeData = {};
    $.ajax({
        type: "POST",
        url: baseUrl + "/dept/listDeptAllMJ",
        dataType: "json",
        async: false,
        success: function (result) {
            var arrays = result.data.list;
            if (arrays != null && arrays.length > 0)
                deptTreeData = arrays[0];
        }
    });
    return deptTreeData;
}

//刷新请款供应商列表
function reflushSupplierTable(){
    arrayNewList.length = 0;//清空选中的稿件id
    saleSum = 0;
    outgoSum = 0;
    $("#saleSum2").text(saleSum.toFixed(2));
    $("#outgoSum2").text(outgoSum.toFixed(2));
    var percent2 = 0;
    if (saleSum > 0) {
        percent2 = (saleSum - outgoSum) * 100 / saleSum;
    }
    $("#percent2").text(percent2.toFixed(2) + "%");
    $("#select_article_table_logs").emptyGridParam();
    $("#select_article_table_logs").jqGrid('setGridParam', {
        datatype: 'json',
        postData: $("#secondForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

$(document).ready(function () {
    //加载打印权限
    downLoadAndPrintObj.loadFlowPrintPermission();
    // userMediaPlateList();

    //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
    // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
    /*$('#editModal').on('hidden.bs.modal', function () {
        $("body").addClass("modal-open");
    });

    $('#accountModal').on('hidden.bs.modal', function () {
        $("body").addClass("modal-open");
    });

    $('#mediaSupplierEditModal').on('hidden.bs.modal', function () {
        $("body").addClass("modal-open");
    });


    $('#deptModal').on('hidden.bs.modal', function () {
        $("body").addClass("modal-open");
    });

    $('#borrowModal').on('hidden.bs.modal', function () {
        $("body").addClass("modal-open");
    });*/


    getDept();
    $.jgrid.defaults.styleUI = 'Bootstrap';

    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    $.ajax({
        type: "post",
        url: "/account/queryCompanyAccountList",
        data: {companyCode: user.dept.companyCode},
        async: false,
        dataType: "json",
        success: function (data) {
            var html = "<option value=''></option>";
            for (var i = 0; i < data.length; i++) {
                html += "<option value='" + data[i].id + "'>" + data[i].name + "</option>";
            }
            $("#editForm select[name='outAccountIds']").append(html);
        }
    });
    layui.use('form', function () {
        var form = layui.form;
        form.render('select');
        form.on('select', function (data) {
            $("#outAccountIds").val(data.value);
        });
    });

    mediaUserPlateMap = userMedaiPlateList();
    loadAllCompany2();
    aggregateAmount();
    //flag=1审核，否则查看
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        view(getQueryString("id"), getQueryString("flag"));
    }

    if (getQueryString("approveId") != null && getQueryString("approveId") != "" && getQueryString("approveId") != undefined) {
        view(getQueryString("approveId"),4);
    }

    if (getQueryString("editId") != null && getQueryString("editId") != "" && getQueryString("editId") != undefined) {
        view(getQueryString("editId"),0);
    };
    $("#invoiceFlag").find(".i-checks").on("ifChanged",function () {
        var lin = $("#editForm input:radio[name='invoiceFlag']:checked").val();
        // var lin = $(this).find("input").val();
        changeLicence(lin);
    });
    $("#refresh").click(function () {
        if ($("#flag").val()) {
            view($("#id").val(), $("#flag").val());
        }
    });

    if(hasRoleMJ()){
        $("#addBtn").show();
    }else{
        $("#addBtn").hide();
    }

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/outgo/listPg',
        datatype: "json",
        mtype: 'POST',
        postData: $("#queryForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: false,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50, 100, 200],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [
            {name: 'mediaTypeId', label: '媒体板块id', hidden: true, width: 120},
            {name: 'code', label: '请款编号', editable: true, width: 120,
                formatter: function (value, grid, rows) {
                    var time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    var sDate1 = Date.parse(time);
                    var sDate2 = Date.parse(rows.payTime);
                    var dateSpan = sDate1 - sDate2;
                    dateSpan = Math.abs(dateSpan);
                    var iDays = Math.floor(dateSpan / (24 * 3600 * 1000));
                    if (rows.state == 26 && iDays > 10) {
                        return "<span style='color:red'>"+value+"</span>";
                    } else {
                        return value;
                    }
                }
            },
            {name: 'invoiceCode', label: '发票编号', editable: true, width: 100},
            {name: 'applyName', label: '请款人', editable: true, width: 80},
            {name: 'deptName', label: '所在部门', editable: true, width: 80},
            {name: 'applyTime', label: '请款日期', editable: true, width: 120},
            {name: 'title', label: '请款标题', editable: true, width: 120,
                formatter: function (value, grid, rows) {
                    var time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    var sDate1 = Date.parse(time);
                    var sDate2 = Date.parse(rows.payTime);
                    var dateSpan = sDate1 - sDate2;
                    dateSpan = Math.abs(dateSpan);
                    var iDays = Math.floor(dateSpan / (24 * 3600 * 1000));
                    if(value){
                        if (rows.state == 26 && iDays > 10) {
                            return "<span style='color:red'>"+value+"</span>";
                        } else {
                            return value;
                        }
                    }else {
                        return "" ;
                    }
                }
            },
            {name: 'supplierName', label: '供应商名称', editable: true, width: 100},
            {name: 'supplierContactor', label: '供应商联系人', editable: true, width: 60},
            {name: 'phone', label: '联系人手机号', editable: true, width: 60,formatter:function(value, grid, rows){
                return  supplierPhone(value, grid, rows);
            }},
            {name: 'accountName', label: '收款人', editable: true, width: 80},
            {name: 'accountBankNo', label: '收款账户', editable: true, width: 80},
            {name: 'accountBankName', label: '收款开户行', editable: true, width: 80},
            {name: 'applyAmount', label: '应付金额', editable: true, width: 80},
            {name: 'expertPayTime', label: '期望付款日期', editable: true, width: 80},
            {name: 'outAccountName', label: '实际出款账户', editable: true, hidden: false, width: 80},
            {name: 'payAmount', label: '实际出款金额', editable: true, width: 80},
            {name: 'taxAmount', label: '成本税金', editable: true, width: 80},
            {name: 'payTime', label: '实际出款日期', editable: true, width: 80},
            {name: 'taskId', label: 'taskId', editable: true, hidden: true, width: 80},
            {name: 'state', label: 'state', editable: true, hidden: true, width: 80},
            {name: 'itemId', label: 'itemId', editable: true, hidden: true, width: 80},
            {
                name: 'state1', label: '状态', editable: true, width: 120,
                formatter: function (value, grid, rows) {
                    var time = new Date().format("yyyy-MM-dd hh:mm:ss")
                    var sDate1 = Date.parse(time);
                    var sDate2 = Date.parse(rows.payTime);
                    var dateSpan= sDate1-sDate2;
                    dateSpan = Math.abs(dateSpan);
                    var iDays = Math.floor(dateSpan / (24 * 3600 * 1000));
                    if (rows.state==26 && iDays>10){
                        return "<span style='color:red'>超时未回票</span>";
                    }else {
                        switch (rows.state) {
                            case -1 :
                                return "<span style='color:red'>审核驳回</span>";
                            case 0 :
                                return "<span style=''>已保存</span>";
                            case 1 :
                                return "<span style=''>已完成</span>";
                            case 2 :
                                return "<span style='color:red'>审核通过</span>";
                            case 3 :
                                return "<span style='color:red'>组长审核</span>";
                            case 4 :
                                return "<span style='color:red'>部长审核</span>";
                            case 5 :
                                return "<span style='color:red'>总监审核</span>";
                            case 6 :
                                return "<span style='color:red'>财务总监审核</span>";
                            case 7 :
                                return "<span style='color:red'>副总经理审核</span>";
                            case 8 :
                                return "<span style='color:red'>总经理审核</span>";
                            case 9 :
                                return "<span style='color:red'>会计确认出款</span>";
                            case 10 :
                                return "<span style='color:red'>业务员确认</span>";
                            case 12 :
                                return "<span style='color:red'>财务部长审核</span>";
                            case 16 :
                                return "<span style='color:red'>出纳出款</span>";
                            case 23 :
                                return "<span style='color:green'>唤醒中</span>";
                            case 26 :
                                return "<span style='color:red'>媒介回填开票信息</span>";

                        }
                    }
                }
            },
            {name: 'isOwner', label: 'isOwner', editable: true, hidden: true, width: 80},
            {
                name: 'operate', label: "操作", index: '', width: 180,
                formatter: function (value, grid, rows) {
                    var html = "";

                    //如果有打印下载权限，则展示
                    if(downLoadAndPrintObj.dowloadAndPritPermission){
                        html += "<a href='javascript:;' onclick='downLoadAndPrintObj.viewModalShow("+rows.id+", "+rows.processType+");'>预览&nbsp;</a>";
                    }
                    // if (rows.isOwner && rows.applyId==user.id ) {//审批通过的链接有问题，先注释
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='view(" + rows.id + ",3)'>审批&nbsp;&nbsp;</a>";
                    // }
                    if (rows.taskId != null && rows.taskId !='') {
                        if(rows.state == 1 && rows.applyId == user.id){
                            html += "<a href='javascript:void(0)' style='height:22px;width:40px;' onclick='workup("+JSON.stringify(rows)+", 1)'>唤醒&nbsp;&nbsp;</a>";
                        }
                        if (rows.state == 23) {
                            html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='workup("+JSON.stringify(rows)+", 2)'>唤醒详情&nbsp;&nbsp;</a>";
                        }
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='showHistory(" + rows.id +","+rows.mediaTypeId+","+rows.parentType+",\""+rows.companyCodet+"\","+rows.processType+")'>审核详情&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue'  onclick='view(" + rows.id + ",0,"+rows.mediaTypeId+")'>编辑&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='del(" + rows.id + ")'>删除&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWCN() && (rows.state == 2||rows.state == 16) && rows.acceptWorker==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",4)'>出纳出款&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state==3 || rows.state==4
                            || rows.state==5 || rows.state==6
                            || rows.state==7 || rows.state==8
                            || rows.state==10 || rows.state==12) && (rows.applyId == user.id || hasRoleMJBZ())) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='returnBack(" + "\"" + rows.taskId + "\"," + rows.itemId + ")'>撤回申请&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWKJ() &&(rows.state == 12 ||rows.state == 9) && rows.acceptWorker==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",5)'>确认公账出款&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWBZ() && rows.state == 1) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",6)'>更改出款信息&nbsp;&nbsp;</a>";
                    }
                    if(rows.applyId == user.id && rows.state==26){
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",7)'>回填&nbsp;&nbsp;</a>";
                    }


                    //因为state从 2--->12或2--->1的时候稿件的请款状态会变，备用金中借款的状态也会变，所以用两个按钮，一个改稿件和借款状态，一个不改
                    // if (hasRoleCWCN() && rows.state == 12) {
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='CWReturn(" + rows.id + ")'>出纳撤回&nbsp;&nbsp;</a>";
                    // }
                    // if (hasRoleCWCN() && rows.state == 2) {
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='CWReject(" + rows.id + ")'>出纳撤回&nbsp;&nbsp;</a>";
                    // }
                    return html;
                },
            },
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            view(rowid, 1);
        },
    });
    resize("#query_table_logs");
    $("#queryForm select[name='stateQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#queryForm select[name='companyCodeQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        aggregateAmount();
    });

    $("#addBtn").click(function () {
        $("#flag").val("");
        $("#supplierId").val("");
        $("#supplierName").val("");
        $("#supplierContactor").val("");
        $(".firstDiv").show();
        $(".secondDiv").hide();
        $(".thirdDiv").hide();
        $("#firstStep").click();
        $("#editModal .modal-title").html("请款信息");
        document.getElementById("editForm").reset();
        $("#editModal").modal({backdrop: "static"});
        loadSupplierInfo();

        $("#refresh").click(function () {
            $("#select_supplier_table_logs").emptyGridParam() ;
            $("#select_supplier_table_logs").jqGrid('setGridParam', {
                datatype: "json",
            }).trigger("reloadGrid"); //重新载入
            resize("#select_supplier_table_logs");
        })

    });
    //=============================================选择供应商开始=================================================//
    //点击添加按钮后加载选择供应商页面
    function loadSupplierInfo() {
        document.getElementById("firstForm").reset() ;
        $("#select_supplier_table_logs").emptyGridParam();
        $("#select_supplier_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {
                supplierNameQc: $("#supplierNameQc1").val(),
                supplierContactorQc: $("#supplierContactorQc1").val()
            }, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#select_supplier_table_logs");
        $("#supplierSearch").off("click").on("click", function () {
            var param = {
                supplierNameQc: $("#supplierNameQc1").val(),
                supplierContactorQc: $("#supplierContactorQc1").val(),
            };
            //判断复选框是否被选中
            if($("#isOldSupplier").prop("checked")){
                param["isOldSupplier"] = 1;
            }else {
                delete param["isOldSupplier"];
            }
            $("#select_supplier_table_logs").emptyGridParam();
            $("#select_supplier_table_logs").jqGrid('setGridParam', {
                datatype: "json",
                postData: param, //发送数据
            }).trigger("reloadGrid"); //重新载入
        });
        //隐藏供应商选择全选框
        document.getElementById("cb_select_supplier_table_logs").style.display = "none";

        $("#selectSupplier").off("click").on("click", function () {
            $("input [name='checkboxState']").iCheck('uncheck');
            var rowid = $("#select_supplier_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
            var rowData = $("#select_supplier_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
            if (rowData.id == null || rowData.id == undefined || rowData.id == "") {
                layer.msg("请先选中供应商！", {time: 3000, icon: 5});
            } else {
                //历史供应商不加规范判断
                if(!$("#isOldSupplier").prop("checked")){
                    //供应商规范判断
                    var message = supplierEditObj.judgeFeeOutgo(rowData);
                    if(message){
                        //判断是否可编辑，仅有当前用户为供应商责任人才能编辑
                        if(rowData["creator"] == user.id){
                            layer.confirm(message, {
                                btn: ['编辑', '取消'], //按钮
                                shade: false //不显示遮罩
                            }, function (index) {
                                layer.close(index);
                                supplierEditObj.editSupplierClick(rowData);
                            });
                        }else {
                            layer.alert(message);
                        }
                        return;
                    }
                }
                $("#supplierId").val(rowData.id);
                $("#supplierName").val(rowData.name);
                $("#supplierContactor").val(rowData.contactor);
                $(".firstDiv").hide();
                $(".secondDiv").show();
                $("#secondStep").click();
                $("#saleSum1").text("0.00");
                $("#outgoSum1").text("0.00");
                //加载稿件信息
                arrayNewList.length = 0;//清空选中的稿件id
                saleSum = 0;
                outgoSum = 0;
                $("#saleSum2").text(saleSum.toFixed(2));
                $("#outgoSum2").text(outgoSum.toFixed(2));
                var percent2 = 0;
                if (saleSum > 0) {
                    percent2 = (saleSum - outgoSum) * 100 / saleSum;
                }
                $("#percent2").text(percent2.toFixed(2) + "%");
                loadArticleInfo(rowData.id, rowData.name, rowData.contactor);
            }
        });
    }
//=============================================选择供应商结束=================================================//

    $("#companyCode").change(function () {
        reflushSupplierTable();
    });

    $("#media_type_id").change(function () {
        reflushSupplierTable();
    });

    if(hasRoleCWBZ()){
        $("#exportBtn").show() ;
    }else{
        $("#exportBtn").hide() ;
    }
    $("#exportBtn").click(function () {
        var params = removeBlank($("#queryForm").serializeJson());
        location.href = "/outgo/exportOutgo" + "?" + $.param(params);
    });

    $("#select_article_table_logs").jqGrid({
        url: baseUrl + '/outgo/listPgForSelectArticle',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#secondForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        // multiboxonly: true,
        // beforeSelectRow: beforeSelectRow,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100, 500, 1000],//分页选项，可以下拉选择每页显示记录数
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            // {name: 'no', label: '订单编号', editable: true, width: 120},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 80},
            {
                name: 'custId',
                index: 'custId',
                label: '客户状态',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "<span class='text-red'>未完善</span>";
                    }
                    return "已完善";
                }
            },
            {name: 'userName', label: '业务员', editable: true, width: 80},
            {name: 'supplierName', label: '供应商名称', editable: true, width: 120},
            {name: 'supplierContactor', label: '联系人', editable: true, width: 80},
            {name: 'innerOuter', label: '内外部', editable: true, width: 80},
            {name: 'channel', label: '频道', editable: true, width: 80},
            {name: 'electricityBusinesses', label: '电商商家', editable: true, width: 80},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 120},
            {name: 'mediaUserName', label: '媒介', editable: true, width: 80},
            {name: 'link', label: '链接', editable: true,hidden:true, width: 80},
            {name: 'title', label: '标题', editable: true, width: 160,
                formatter: function (v, options, row) {
                    if (v==undefined ||v==null || v == "") {
                        return "";
                    } else {
                        var link = row.link ;
                        if(!(link==undefined || link==null || link=="")){
                            var str = link.substring(0, 4).toLowerCase();
                            if (str == "http") {
                                return "<a href='" + link + "' target='_blank'>" + v + "</a>";
                            } else {
                                return "<a href='//" + link + "' target='_blank'>" + v + "</a>";
                            }
                        }else{
                            return v ;
                        }
                    }
                }
            },
            {name: 'issuedDate', label: '发布日期', editable: true,hidden:false, width: 120},
            {name: 'promiseDate', label: '答应到款时间', editable: true,hidden:false, width: 120},
            {name: 'promiseDate', 答应到款时间: '链接', editable: true,hidden:true, width: 120},
            {name: 'num', label: '数量', editable: true, width: 80},
            {name: 'unitPrice', label: '单价', editable: true, width: 100},
            {name: 'priceType', label: '价格类型', editable: true, width: 100},
            {name: 'saleAmount', label: '报价', editable: true, width: 100},
            {name: 'outgoAmount', label: '应付金额', editable: true, width: 120},
            {name: 'otherExpenses', label: '其他费用', editable: true, width: 60},
        ],
        pager: jQuery("#select_article_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        gridComplete: function () {
            var rowData = $(this).jqGrid('getRowData');
            //遍历所有的行，如果是选中的，说明它在数组中，让他选中
            for (var i = 0, n = rowData.length; i < n; i++) {
                var item = rowData[i];
                //判断是否存在数据
                if (arrayNewList.length > 0) {
                    if (arrayNewList.indexOf(item.id) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_select_article_table_logs_" + item.id).attr("checked", true);
                    }
                }
            }
        },
        loadComplete: function (xhr) {
            var array = xhr.list;
            if (arrayNewList.length > 0) {
                $.each(array, function (i, item) {
                    if (arrayNewList.indexOf(item.id.toString()) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_select_article_table_logs_" + item.id).attr("checked", true);
                    }
                });
            }
        },
        onSelectAll: function (aRowids, status) {
            if (status == true) {
                //循环aRowids数组，将code放入arrayNewList数组中
                $.each(aRowids, function (i, item) {
                    //已选中的先排除
                    if (!(arrayNewList.indexOf(item) > -1)) {
                        saveData(item);
                    }
                })
            } else {
                //循环aRowids数组，将code从arrayNewList中删除
                $.each(aRowids, function (i, item) {
                    deleteIndexData(item);
                })
            }
            $("#saleSum2").text(saleSum.toFixed(2));
            $("#outgoSum2").text(outgoSum.toFixed(2));
            var percent2 = 0;
            if (saleSum > 0) {
                percent2 = (saleSum - outgoSum) * 100 / saleSum;
            }
            $("#percent2").text(percent2.toFixed(2) + "%");
        },
        onSelectRow: function (rowid, status) {
            // var rowData= $(this).jqGrid("getRowData",rowid);
            if (status == true) {
                if (!(arrayNewList.indexOf(rowid) > -1)) {
                    saveData(rowid);
                }
            } else {
                deleteIndexData(rowid);
            }
            $("#saleSum2").text(saleSum.toFixed(2));
            $("#outgoSum2").text(outgoSum.toFixed(2));
            var percent2 = 0;
            if (saleSum > 0) {
                percent2 = (saleSum - outgoSum) * 100 / saleSum;
            }
            $("#percent2").text(percent2.toFixed(2) + "%");
        },
    });

    function saveData(obj) {
        arrayNewList.push(obj);
        var rowData = $("#select_article_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
        saleSum += parseFloat(rowData.saleAmount);
        outgoSum += parseFloat(rowData.outgoAmount);
    }

    function deleteIndexData(obj) {
        //获取obj在arrayNewList数组中的索引值
        for (i = 0; i < arrayNewList.length; i++) {
            $("#row" + obj).remove()
            if (arrayNewList[i] == obj) {
                //根据索引值删除arrayNewList中的数据
                var rowData = $("#select_article_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
                saleSum = saleSum - parseFloat(rowData.saleAmount);
                outgoSum = outgoSum - parseFloat(rowData.outgoAmount);
                arrayNewList.splice(i, 1);
            }
        }
    }

    $("#selected_article_table_logs").jqGrid({
        url: baseUrl + '/outgo/listPgForSelectedArticle',
        datatype: "local",
        mtype: 'POST',
        // postData: {id: id}, //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: false,
        // multiboxonly: true,
        // beforeSelectRow: beforeSelectRow,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100, 500, 1000],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            // {name: 'no', label: '订单编号', editable: true, width: 120},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 80},
            {name: 'userName', label: '业务员', editable: true, width: 80},
            {name: 'companyCodeName', label: '公司', editable: true, width: 80},
            {name: 'innerOuter',label:'内外部',editable:true,width:80},
            {name: 'channel',label:'频道',editable:true,width:80},
            {name: 'electricityBusinesses',label:'电商',editable:true,width:80},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 120},
            {name: 'mediaUserName', label: '媒介', editable: true, width: 80},
            {name: 'link', label: '链接', editable: true,hidden:true, width: 80},
            {name: 'title', label: '标题', editable: true, width: 160,
                formatter: function (v, options, row) {
                    if (v==undefined ||v==null || v == "") {
                        return "";
                    } else {
                        var link = row.link ;
                        if(!(link==undefined || link==null || link=="")){
                            var str = link.substring(0, 4).toLowerCase();
                            if (str == "http") {
                                return "<a href='" + link + "' target='_blank'>" + v + "</a>";
                            } else {
                                return "<a href='//" + link + "' target='_blank'>" + v + "</a>";
                            }
                        }else{
                            return v ;
                        }
                    }
                }
            },
            {
                name: 'issuedDate', label: '发布日期', editable: true, width: 100, formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'promiseDate', label: '答应到款时间', editable: true, width: 100, formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'num', label: '数量', editable: true, width: 60},
            {name: 'unitPrice', label: '单价', editable: true, width: 60},
            {name: 'priceType', label: '价格类型', editable: true, width: 120},
            {name: 'saleAmount', label: '报价', editable: true, width: 60},
            {name: 'incomeAmount', label: '回款金额', editable: true, width: 120},
            {name:'incomeDate',label:'回款日期',editable: true, width:120,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }},
            {name: 'outgoAmount', label: '应付金额', editable: true, width: 100},
            {name: 'otherExpenses', label: '其他费用', editable: true, width: 60},
            {name: 'state1', label: '稿件状态', editable: true, width: 60,hidden:true,
                formatter: function (v, options, row) {
                   return row.state;
                }},
            {
                name: 'state', label: '稿件状态', editable: true, width: 80, formatter: function (d) {
                    if (d == -9) {
                        return "<span class='text-red'>已删</span>";
                    }
                    return "正常";
                }
            }
        ],
        pager: jQuery("#selected_article_pager_logs"),
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#select_supplier_table_logs").jqGrid({
        url: baseUrl + '/supplier/querySupplierListByTypeNew',
        datatype: "local",
        mtype: 'POST',
        // postData: {
        //     supplierNameQc: $("#supplierNameQc1").val(),
        //     supplierContactorQc: $("#supplierContactorQc1").val()
        // }, //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        multiboxonly: true,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'id', label: 'id', editable: true, hidden: true, width: 0},
            {name: 'supplierNature', label: 'supplierNature', editable: false, width: 60, hidden: true},
            {name: 'standarCompanyFlag', label: 'standarCompanyFlag', editable: false, width: 60, hidden: true},
            {name: 'standarPhoneFlag', label: 'standarPhoneFlag', editable: false, width: 60, hidden: true},
            {name: 'phone', label: 'phone', editable: false, width: 60, hidden: true},
            {name: 'qqwechat', label: 'qqwechat', editable: false, width: 60, hidden: true},
            {name: 'qq', label: 'qq', editable: false, width: 60, hidden: true},
            {name: 'creator', label: 'creator', editable: false, width: 60, hidden: true},
            {
                name: 'supplierNatureName',
                label: '供应商性质',
                editable: false,
                width: 80,
                sortable:false,
                formatter: function (value, grid, rows) {
                    if (rows.supplierNature == 1) {
                        return "个体供应商";
                    }else {
                        return "企业供应商";
                    }
                }
            },
            {name: 'name', label: '供应商公司名称', editable: true, width: 180, sortable: false},
            {
                name: 'standarCompanyFlagName',
                label: '是否标准公司',
                editable: false,
                width: 80,
                sortable:false,
                formatter: function (value, grid, rows) {
                    if(rows.standarCompanyFlag == 1){
                        return "<span style='color: green;'>标准</span>";
                    }else {
                        return "<span style='color: red;'>非标准</span>";
                    }
                }
            },
            {name: 'contactor', label: '供应商联系人', editable: true, width: 100, sortable: false},
            {
                name: 'standarPhoneFlagName',
                label: '是否规范联系人',
                editable: false,
                width: 100,
                sortable:false,
                formatter: function (value, grid, rows) {
                    if(rows.standarPhoneFlag == 1){
                        return "<span style='color: green;'>规范</span>";
                    }else {
                        return "<span style='color: red;'>不规范</span>";
                    }
                }
            },
            {
                name: 'phoneStr',
                label: '手机号',
                editable: false,
                width: 100,
                sortable:false,
                formatter: function (value, grid, rows) {
                    var flag = false;
                    if (rows.hasOwnProperty('plateIds')){
                        var plateIds = rows.plateIds.split(",");
                        if (plateIds) {
                            for (var i = 0; i < plateIds.length; i++) {
                                if (mediaUserPlateMap.contains(plateIds[i])) {
                                    //当前用户的板块包含了该供应商的板块
                                    flag = true;
                                }
                            }
                        }
                    }

                    value = rows.phone || "";
                    if(value){
                        if((rows.creator == user.id)|| (flag && rows.flag)){
                            return value;
                        }else {
                            if(value.length >= 11){
                                var start = value.length > 11 ? "*****" : "****";
                                return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                            }else if(value.length >= 3){
                                return value[0] + "***" + value[value.length - 1];
                            }else {
                                return "**";
                            }
                        }
                    }else {
                        return "";
                    }
                }
            },
            {name: 'contactorDesc', label: '联系人描述', editable: true, width: 240},
        ],
        pager: jQuery("#select_supplier_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        //实现单选
        beforeSelectRow: function () {
            $("#select_supplier_table_logs").jqGrid('resetSelection');
            return (true);
        }
    });


//=============================================选择稿件开始=================================================//
    //加载稿件信息
    function loadArticleInfo(supplierId, supplierName, supplierContactor) {
        $("#supplierIdSec").val(supplierId);
        $("#supplierNameSec").val(supplierName);
        $("#supplierContactorSec").val(supplierContactor);
        $("#select_article_table_logs").emptyGridParam();
        $("#select_article_table_logs").jqGrid('setGridParam', {
            datatype: 'json',
            postData: $("#secondForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#select_article_table_logs");
        reloadSumAmount();
        $("#articleSearch").off("click").on("click", function () {
            $("#select_article_table_logs").emptyGridParam();
            $("#select_article_table_logs").jqGrid('setGridParam', {
                datatype: 'json',
                postData: $("#secondForm").serializeJson(), //发送数据
            }).trigger("reloadGrid"); //重新载入
            reloadSumAmount();
        });
        $("#refresh").off("click").on("click",function () {
            arrayNewList.length = 0;//清空选中的稿件id
            saleSum = 0;
            outgoSum = 0;
            $("#saleSum2").text(saleSum.toFixed(2));
            $("#outgoSum2").text(outgoSum.toFixed(2));
            var percent2 = 0;
            if (saleSum > 0) {
                percent2 = (saleSum - outgoSum) * 100 / saleSum;
            }
            $("#percent2").text(percent2.toFixed(2) + "%");
            $("#select_article_table_logs").emptyGridParam();
            $("#select_article_table_logs").jqGrid('setGridParam', {
                datatype: 'json',
                postData: $("#secondForm").serializeJson(), //发送数据
            }).trigger("reloadGrid"); //重新载入
            reloadSumAmount();
        });
        $("#selectArticle").off("click").on("click", function () {
            var startTime = $("#startTime").val().length>0?$("#startTime").val():'';
            var endTime = $("#endTime").val().length>0?$("#endTime").val():'';
            var timeStr = startTime+" ~ "+ endTime ;
            $("#timeScaleSec").val(timeStr);
            var checkbox = document.getElementsByName("checkboxState");
            if(checkbox[0].checked){
                checkState = checkbox[0].value
            }else{
                checkState = null;
            }
            $("#checkState").val(checkState);
            if (arrayNewList.length == 0 && !checkState) {
                layer.msg("请先选择稿件！", {time: 2000, icon: 5});
            } else {
                $("#articleIdsSec").val(arrayNewList.toString());
                saveStepOne();
            }
        });
        $("#backStepOne").off("click").on("click", function () {
            $("#supplierId").val("");
            $("#supplierName").val("");
            $("#supplierContactor").val("");
            $("#articleIdsSec").val("");
            $(".firstDiv").show();
            $(".secondDiv").hide();
            $(".thirdDiv").hide();
            $("#firstStep").click();
        })
    }
    //=============================================选择稿件结束=================================================//

//=============================================银行账户表格开始=================================================//
    $("#account_table_logs").jqGrid({
        url: baseUrl + '/account/querySupplierAccount',
        datatype: "local",
        mtype: 'POST',
        // postData: {
        //     supplierId: supplierId
        // }, //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        multiboxonly: true,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100, 500, 1000],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'companyName', label: '供应商名称', editable: true, width: 240},
            {name: 'name', label: '账户名称', editable: true, width: 180},
            {name: 'owner', label: '账户户主', editable: true, width: 240},
            {name: 'bankNo', label: '银行账号', editable: true, width: 240},
            {name: 'bankName', label: '账号开户行', editable: true, width: 240},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 0},
        ],
        pager: jQuery("#account_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        //实现单选
        beforeSelectRow: function () {
            $("#account_table_logs").jqGrid('resetSelection');
            return (true);
        }
    });
    resize("#account_table_logs");

//=============================================银行账户表格结束=================================================//
    //=============================================银行账户操作开始=================================================//
    $("#selAccount").click(function () {
        //加载银行账户
        loadAccountInfo($("#supplierId").val());
        $("#accountModal").modal({backdrop: "static"});
        $("#editModal").modal('hide');
    });
//初始化银行账户
    function loadAccountInfo(supplierId) {
        $("#account_table_logs").jqGrid('setGridParam', {
            datatype: "json" ,
            postData: {supplierId: supplierId}, //发送数据
        }).trigger("reloadGrid"); //重新载入

        $("#accountSearch").off("click").on("click", function () {
            $("#account_table_logs").emptyGridParam();
            $("#account_table_logs").jqGrid('setGridParam', {
                datatype: 'json',
                postData: {supplierId: supplierId,
                    ownerQc: $("#ownerQc2").val(),
                    bankNameQc: $("#bankNameQc2").val(),
                    bankNoQc: $("#bankNoQc2").val()
                }, //发送数据
            }).trigger("reloadGrid"); //重新载入
        });

        $(".cleanAccount").off("click").on("click", function () {
            $("#accountId").val("");
            $("#accountName").val("");
            $("#accountBankNo").val("");
            $("#accountBankName").val("");
        });

        $("#selectAccount").off("click").on("click", function () {
            var rowid = $("#account_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
            var rowData = $("#account_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
            $("#accountId").val(rowData.id);
            $("#accountName").val(rowData.owner);
            $("#accountBankNo").val((rowData.bankNo || "").trim().replace(/&nbsp;/ig, ' '));
            $("#accountBankName").val((rowData.bankName || "").trim().replace(/&nbsp;/ig, ' '));
            $("#accountModal").modal('hide');
            document.getElementById("accountForm").reset();
            $("#editModal").modal({backdrop: "static"});
        });
        $("#cancelAccount").off("click").on("click", function () {
            $("#accountModal").modal('hide');
            $("#editModal").modal({backdrop: "static"});
        });
    }
//=============================================银行账户操作结束=================================================//
    //=============================================备用金表格开始=================================================//
    $("#borrow_table_logs").jqGrid({
        url: baseUrl + '/borrow/listPgForOutgo',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#borrowForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        beforeSelectRow: function () {
            $("#borrow_table_logs").jqGrid('resetSelection');
            return (true);
        },
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100, 500, 1000],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'code', label: '借款编号', editable: true, width: 120},
            {name: 'title', label: '借款标题', editable: true, width: 180},
            {
                name: 'type', label: '类型', editable: true, width: 60,
                formatter: function (value, grid, rows) {
                    if (rows.type == 0) {
                        return "<span style=''>备用金</span>";
                    } else if (rows.type == 1) {
                        return "<span style=''>其它</span>";
                    } else {
                        return "";
                    }
                }
            },
            {name: 'applyName', label: '借款人', editable: true, width: 80},
            {name: 'deptName', label: '所在部门', editable: true, width: 80},
            {name: 'applyAmount', label: '申请金额', editable: true, width: 80},
            {name: 'payAmount', label: '实付金额', editable: true, width: 80},
            {name: 'repayAmount', label: '已还金额', editable: true, width: 80},
            {name: 'remainAmount', label: '未还金额', editable: true, width: 80},
            {name: 'payTime', label: '实际支付日期', editable: true, width: 140},
            {name: 'remark', label: '借款原因', editable: true, width: 180}
        ],
        pager: jQuery("#borrow_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        gridComplete: function () {
            var rowData = $(this).jqGrid('getRowData');
            //遍历所有的行，如果是选中的，说明它在数组中，让他选中
            for (var i = 0, n = rowData.length; i < n; i++) {
                var item = rowData[i];
                //判断是否存在数据
                if (borrowList.length > 0) {
                    if (borrowList.indexOf(item.id) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_borrow_table_logs_" + item.id).attr("checked", true);
                    }
                }
            }
        },
        loadComplete: function (xhr) {
            var array = xhr.list;
            if (borrowList.length > 0) {
                $.each(array, function (i, item) {
                    if (borrowList.indexOf(item.id.toString()) > -1) {
                        //判断borrowList中存在item.code值时，选中前面的复选框，
                        $("#jqg_borrow_table_logs_" + item.id).attr("checked", true);
                    }
                });
            }
        },
        onSelectAll: function (aRowids, status) {
            if (status == true) {
                //循环aRowids数组，将code放入borrowList数组中
                $.each(aRowids, function (i, item) {
                    if (!(borrowList.indexOf(item) > -1)) {
                        saveData1(item);
                    }
                })
            } else {
                //循环aRowids数组，将code从borrowList中删除
                $.each(aRowids, function (i, item) {
                    deleteIndexData1(item);
                })
            }
        },
        onSelectRow: function (rowid, status) {
            if (status == true) {
                if (!(borrowList.indexOf(rowid) > -1)) {
                    saveData1(rowid);
                }
            } else {
                deleteIndexData1(rowid);
            }
        }
    });
    resize("#borrow_table_logs");
    document.getElementById("cb_borrow_table_logs").style.display = "none";
    function saveData1(obj) {
        borrowList.length=0;
        $("#borrowDetail").empty();
        borrowList.push(obj);
        var rowData = $("#borrow_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
        html = '<tr id="rows' + rowData.id + '"><td>' + rowData.code + '</td><td>' + rowData.title + '</td>' +
            '<td>' + rowData.type + '</td><td>' + rowData.applyName + '</td>' +
            '<td>' + rowData.deptName + '</td><td>' + rowData.applyAmount + '</td>' +
            '<td>' + rowData.payAmount + '</td><td>' + rowData.payTime + '</td>' +
            '<td><input type="hidden" name="pay_' + rowData.id + '" id="pay_' + rowData.id + '" value="' + rowData.repayAmount + '">' + rowData.repayAmount + '</td>' +
            '<td><input type="hidden" name="remain_' + rowData.id + '" id="remainAmount" value="' + rowData.remainAmount + '">' + rowData.remainAmount + '</td>' +
            '<td><input type="text" name="fund_' + rowData.id + '" id="fundAmount1" value="' + $("#applyAmountBorrow").val() + '" onkeyup="this.value=this.value.toString().match(/^\\d+(?:\\.\\d{0,2})?/)"></td></tr>';
        $("#borrowDetail").append(html);
    }

    function deleteIndexData1(obj) {
        //获取obj在borrowList数组中的索引值
        for (i = 0; i < borrowList.length; i++) {
            $("#rows" + obj).remove()
            if (borrowList[i] == obj) {
                //根据索引值删除borrowList中的数据
                borrowList.splice(i, 1);
            }
        }
    }
    //=============================================备用金表格结束=================================================//
    //=============================================备用金操作开始=================================================//
    //关联借款信息，弹出窗口
    $("#selBorrow").click(function () {
        if ($("#fundAmount").val() == 0) {
            borrowList.length = 0;
            document.getElementById("borrowForm").reset();
            $("#borrowDetail").empty();
            $("#borrow_table_logs").jqGrid('setGridParam', {
                datatype: 'json',
                postData: $("#borrowForm").serializeJson(), //发送数据
            }).trigger("reloadGrid"); //重新载入
            $("#editModal").modal('hide');
            $("#borrowModal").modal({backdrop: "static"});
            $("#applyNameQc").val($("#applyName").val());
            $("#applyAmountBorrow").val($("#applyAmount").val());
            $("#borrowSearch").trigger("click");
        } else {
            swal("已选择了备用金信息，不支持修改，只支持删除后重新选择备用金!")
        }
    });
    //清除借款信息
    $(".cleanBorrow").click(function () {
        var lock = true ;
        layer.confirm('删除该请款单的备用金信息？', {
            btn: ['删除', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/outgo/cleanOutgoBorrow",    //向后端请求数据的url
                    data: {id: $("#id").val()},
                    dataType: "json",
                    success: function (data) {
                        layer.close(index);
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            $("#fundAmount").val(0);
                            $("#borrowDetail").empty();
                            $("#borrowInfo").empty();
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    }
                });
            }
        }, function () {
            return;
        });
    });
    $("#borrowSearch").click(function () {
        $("#borrow_table_logs").emptyGridParam();
        $("#borrow_table_logs").jqGrid('setGridParam', {
            datatype: 'json',
            postData: $("#borrowForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
    //使用备用金
    $("#selectBorrow").click(function () {
        if($("#fundAmount1").val()>$("remainAmount").val()){
            swal("备用金不能大于未还金额！");
            return;
        }
        if(!($("#fundAmount1").val()==$("#applyAmountBorrow").val())){
            swal("备用金必须和实际请款金额相等！");
            return;
        }
        if (borrowList.length == 0) {
            swal("请先选择账户！");
            return;
        }
        $("#borrowIds").val(borrowList.toString());
        if(checkAmount()) {
            var lock = true ;
            layer.confirm('请确认抵消的借款信息？提交后不能取消！', {
                btn: ['确定', '取消'], //按钮
                shade: false //不显示遮罩
            }, function (index) {
                layer.close(index);
                var param = $("#borrowForm").serializeJson();
                startModal("#selectBorrow");//锁定按钮，防止重复提交
                if(lock){
                    lock = false ;
                    $.ajax({
                        type: "post",
                        data: param,
                        url: baseUrl + "/outgo/saveOutgoBorrow",
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();   //解锁按钮锁定
                            $("#borrowModal").modal('hide');
                            $("#editModal").modal({backdrop: "static"});
                            if (data.code == 200) {
                                $("#fundAmount").val(data.data.amount);
                                $("#borrow_table_logs").jqGrid('setGridParam', {
                                    datatype: 'json',
                                    postData: $("#borrowForm").serializeJson(), //发送数据
                                }).trigger("reloadGrid"); //重新载入
                                $("#borrowInfo").empty();
                                if (data.data.list.length > 0) {
                                    html = '<div><h3>备用金扣除详情</h3></div><table class="table table-bordered" style="text-align: center"><thead>' +
                                        '<th style="text-align:center;vertical-align:middle;">借款编号</th>' +
                                        '<th style="text-align:center;vertical-align:middle;">借款标题</th>' +
                                        '<th style="text-align:center;vertical-align:middle;">借款类型</th>' +
                                        '<th style="text-align:center;vertical-align:middle;">借款人</th>' +
                                        '<th style="text-align:center;vertical-align:middle;">所属部门</th>' +
                                        '<th style="text-align:center;vertical-align:middle;">借款金额</th>' +
                                        '<th style="text-align:center;vertical-align:middle;">已还金额</th>' +
                                        '<th style="text-align:center;vertical-align:middle;">未还金额</th>' +
                                        '<th style="text-align:center;vertical-align:middle;">备用金金额</th>' +
                                        '</thead>';
                                    for (var i = 0; i < data.data.list.length; i++) {
                                        var typeStr = data.data.list[i]['type'] == 1 ? "备用金" : "其它";
                                        html += '<tr><td>' + data.data.list[i]['code'] + '</td>' +
                                            '<td>' + data.data.list[i]['title'] + '</td>' +
                                            '<td>' + typeStr + '</td>' +
                                            '<td>' + data.data.list[i]['apply_name'] + '</td>' +
                                            '<td>' + data.data.list[i]['dept_name'] + '</td>' +
                                            '<td>' + data.data.list[i]['apply_amount'] + '</td>' +
                                            '<td>' + data.data.list[i]['repay_amount'] + '</td>' +
                                            '<td>' + data.data.list[i]['remain_amount'] + '</td>' +
                                            '<td>' + data.data.list[i]['amount'] + '</td></tr>';
                                    }
                                    html += '</tbody></table>';
                                    $("#borrowInfo").append(html);
                                }
                            } else if(data.code == 1002){
                                swal({
                                    title: "异常提示",
                                    text: data.msg,
                                });
                            } else {
                                if (getResCode(data))
                                    return;
                            }

                        },
                        error: function () {
                            Ladda.stopAll();
                        }
                    });
                }
            }, function () {
                return;
            });
        }
    });
    $("#closeBorrow").off("click").on("click", function () {
        $("#editModal").modal({backdrop: "static"});
    });
    //=============================================备用金操作结束=================================================//

    supplierEditObj.addSupplierCheckRule("mediaSupplierEditModal");//供应商登记框添加校验规则

    //表单渲染
    layui.use('form', function(){
        var form = layui.form;
        form.render();
    });
});


// function userMediaPlateList() {
//     $.ajax({
//         url: baseUrl + "/mediaPlate/userId",  //mediaType/listByUserId
//         data: {"userId": user.id},
//         type: "post",
//         dataType: "json",
//         success: function (data) {
//             if (data) {
//                 for (var i = 0; i < data.length; i++) {
//                     mediaUserPlateMap.push(data[i].id)
//                 }
//             }
//         }
//     });
// }

/**
 * 抵消金额不能超过未还金额
 * @returns {boolean}
 */
function checkAmount(){
    var flag = true;
    $("#borrowDetail>tr").each(function (i, d) {
        var oldAmount = $(d).find("td:nth-child(10)>input").val();
        var newAmount = $(d).find("td:nth-child(11)>input").val();
        if(newAmount!=null && newAmount!=""){
            var money = parseFloat(newAmount);
            if(money>oldAmount){
                swal("抵消金额不能超过未还金额");
                flag = false;
            }
        }else{
            swal("抵消金额不能为空");
            flag=false;
        }
    });
    return flag;
}

function submitHander(t, url, state) {
    if(!getOutgoFlag()){
        swal("很抱歉，请款稿件列表无数据或者所有稿件被删除！");
        return;
    }

    if ($("#mediaGroupLeader").attr("data-divId")=="hide"){
        $("#mediaGroupLeader").val("");
    }else if ($("#mediaGroupLeader").val()== null|| $("#mediaGroupLeader").val()==""){
        swal("请选择审核人!");
        return;
    }

    //应付合计
    var  sumOutgo = $("#editForm [name = 'sumOutgo']").val() == "" ? 0 : $("#editForm [name = 'sumOutgo']").val();
    //实际请款成本
    var actualCost=$("#editForm [name='actualCost']").val() == "" ? 0 : $("#editForm [name = 'actualCost']").val();

    if(parseFloat(actualCost)>parseFloat(sumOutgo)){
        swal("实际请款成本要小于等于合计成本！");
        return;
    }

    var lock = true ;
    var title = $("#title").val() ;
    var fundAmount = parseFloat($("#fundAmount").val()) ;
    var applyAmount = parseFloat($("#applyAmount").val());
    if((title.indexOf("消账")>-1 || title.indexOf("销账")>-1 || title.indexOf("备用金")>-1)&&fundAmount==0){
        swal("备用金或销账时备用金额必须大于0！");
        return ;
    }
    if(fundAmount>applyAmount){
        swal("备用金额不能大于实际请款金额！") ;
        return ;
    }
    if(fundAmount>0 && fundAmount!=applyAmount){
        swal("选择的备用金必须等于实际请款金额") ;
        return ;
    }

    if( $("#invoiceFlag1").val()==1){
        if($("#outgoTax").val()==0){
            swal("请选择税点!");
            return;
        }
        if($("#outgoTax").val()==3 && $("#outgoTax1").val()==""){
            swal("请输入税点!");
            return;
        }
        var sum = (parseFloat($("#actualCost").val())+parseFloat($("#taxAmount").val())).toFixed(2);
        // var sum = parseFloat($("#actualCost").val())+parseFloat($("#taxAmount").val());
        if(applyAmount > sum ){
            swal("实际请款金额要小于等于请款成本加税金！");
            return;
        }
    }else {
        if (applyAmount> parseFloat(actualCost)){
            swal("实际请款金额要小于等于请款成本！");
            return;
        }


    }
    //实际出款金额计算= 请款金额 - 备用金
    // $("#payAmount").val(applyAmount - fundAmount);
    if ($("#editForm").valid()) {
        var tips;
        if (state == 0) {
            $("#state").val(state);
            tips = "确认保存？";
        } else {
            $("#state").val(state);
            tips = "确认提交？提交后不能修改";
        }
        layer.confirm(tips, {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            //有图片添加传参
            var formData = new FormData($("#editForm")[0]);
            formData.append('mediaGroupLeader',$("#mediaGroupLeader").val())
            if (formData.get('invoiceFlag') == null || formData.get('invoiceFlag') == undefined) {
                formData.append('invoiceFlag',$("#invoiceFlag1").val())
            }
            if (formData.get('outgoTax') == 3) {
                formData.set('outgoTax',parseFloat($("#outgoTax1").val()) / 100)
            }
            startModal("#save");//锁定按钮，防止重复提交
            startModal("#update");//锁定按钮，防止重复提交
            //如果有后缀
            if ($("#titleSuffix").val()) {
                formData.set('title',(formData.get('title') + "(" + $("#titleSuffix").val() + ")"));
            }
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: url,
                    data: formData,
                    dataType: "json",
                    async: true,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        Ladda.stopAll();   //解锁按钮锁定
                        if (data.code == 200) {
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            if (state == 4) {
                                swal(data.data.message);
                                $("#editModal").modal('hide');
                            } else {
                                layer.msg(data.data.message)
                            }
                        } else if(data.code == 1002){
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;
                            $("#editModal").modal('hide');
                        }
                    },
                    error: function () {
                        Ladda.stopAll();
                    }
                });
            }
        }, function () {
            return;
        });
    }
}

/**
 * 加载当前用户拥有的板块
 * @param attribute
 */
function loadTypeByUserId(attribute) {
    $.ajax({
        url: baseUrl + "/mediaPlate/userId",  ///mediaType/listByUserId
        data: {"userId": user.id},
        type: "post",
        dataType: "json",
        success: function (data) {
            if (data) {
                var mTypeEle = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
                for (var i = 0; i < data.length; i++) {
                    var mType = data[i];
                    mTypeEle.append("<option value='${id}'>${name}</option>".replace("${id}", mType.id).replace("${name}", mType.name));
                }
            }
        }
    });
}

function submitHanderCW(t, url) {
    var lock = true ;
    if ($("#outAccountIds").val() == undefined || $("#outAccountIds").val() == "" || $("#outAccountIds").val() == null) {
        swal("请先选择出款账户！")
        return;
    }
    layer.confirm('请确认请款信息？提交后不能取消！', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        var param = {
            id: $("#id").val(),
            outAccountIds: $("#outAccountIds").val(),
            payTime: $("#payTime").val(),
            payAmount: $("#payAmount").val(),
            desc:$("#desc1").val()
        };
        startModal("#" + t.id);//锁定按钮，防止重复提交
        startModal("#CWReject2");//锁定按钮，防止重复提交
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                data: param,
                url: url,
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    if (data.code == 200) {
                        $("#editModal").modal('hide');
                        swal(data.data.message);
                        $("#outAccountIds").val("");
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else if(data.code == 1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        $("#editModal").modal('hide');
                        if (getResCode(data))
                            return;
                    }
                },
                error: function () {
                    Ladda.stopAll();
                }
            });
        }
    }, function () {
        return;
    });
}

//审核通过
function pass(t) {
    approveTask($("#taskId").val(), 1, t.id,$("#desc").val())
}

//审核驳回
function reject(t) {
    approveTask($("#taskId").val(), 0, t.id,$("#desc").val(),function () {
        $("#editModal").modal("hide");
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
        return 1;
    })
}

function aggregateAmount() {
    $.ajax({
        type:"post",
        data:$("#queryForm").serializeJson(),
        url:baseUrl+"/outgo/aggregateAmount",
        dataType:"json",
        async:false,
        success:function (resDate) {
            if (resDate){
                $("#applyAmout1").text(fmtMoneyBringUnit(resDate.applyAmount) || 0);
                $("#payAmount1").text(fmtMoneyBringUnit(resDate.payAmount) || 0);
            }else {
                $("#tj").find(".text-danger").htmleditForm(0);
            }

        }
    })

}

function loadAllCompany2() {
    $.get(baseUrl + "/dept/listJTAllCompany", null, function (data) {
        if (data.code == 200 && data.data.result) {
            $(data.data.result).each(function (i, d) {
                if (user.companyCode == d.code) {
                    $("#companyCode").append("<option value='" + d.code + "' data='" + d.id + "' selected='selected'>" + d.name + "</option>");
                    // $("#companyCodeQc").append("<option value='" + d.code + "' data='" + d.id + "' selected='selected'>" + d.name + "</option>");
                } else {
                    $("#companyCode").append("<option value='" + d.code + "' data='" + d.id + "'>" + d.name + "</option>");
                }
                $("#companyCodeQc").append("<option value='" + d.code + "' data='" + d.id + "'>" + d.name + "</option>");

            });
        }
    }, "json");
}

//重新加载可以请款的总金额
function reloadSumAmount(){
    $.ajax({
        type: "post",
        data: $("#secondForm").serializeJson(),
        url: '/outgo/listPgForSelectArticleSum',
        dataType: "json",
        success: function (data) {
            $("#saleSum1").text(data.saleAmountSum.toFixed(2));
            $("#outgoSum1").text(data.outgoAmountSum.toFixed(2));
        },
    });
}

function changeAccount(t) {
    var lock = true ;
    if ($("#outAccountIds").val() == undefined || $("#outAccountIds").val() == "" || $("#outAccountIds").val() == null) {
        swal("请先选择出款账户！")
        return;
    }
    layer.confirm('请确认请款信息？', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        var param = {
            id: $("#id").val(),
            outAccountIds: $("#outAccountIds").val(),
            payTime: $("#payTime").val(),
        };
        startModal("#changeAccountBtn");//锁定按钮，防止重复提交
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                data: param,
                url: baseUrl + "/outgo/changeAccount",
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    if (data.code == 200) {
                        $("#editModal").modal('hide');
                        swal(data.data.message);
                        $("#outAccountIds").val("");
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else if(data.code == 1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        $("#editModal").modal('hide');
                        if (getResCode(data))
                            return;
                    }
                },
                error: function () {
                    Ladda.stopAll();
                }
            });
        }
    }, function () {
        return;
    });
}

///根据板块类型查组长
function mediaGroupLeader1(t,mediaTypeId) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/outgo/mediaGroupLeader",{mediaTypeId:mediaTypeId}, function (data) {
            $(t).append("<option value=\"\">请选择</option>");
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}
function applyMedia1() {
    var companyCode =$("#companyCodeUser").val();
    if ($("#mediaTypeId").val()) {
        var mt = mediaType($("#mediaTypeId").val());
    }
    //获取税点
    if ($("#editForm [name='outgoTax']").val() == 3){
        var outgoTax = parseFloat($("#outgoTax1").val())/100;
    }else {
        var outgoTax = $("#editForm [name='outgoTax']").val() == "" ? 0 : $("#editForm [name = 'outgoTax']").val();

    }
    //实际请款成本
    var actualCost=$("#editForm [name='actualCost']").val() == "" ? 0 : $("#editForm [name = 'actualCost']").val();


    // 计算税金
    var taxAmount = (parseFloat(actualCost)*parseFloat(outgoTax));
    if (isNaN(taxAmount)){
        $("#editForm [name = 'taxAmount']").val(0);
    }else {
        //税金赋值
        $("#editForm [name = 'taxAmount']").val(parseFloat(taxAmount).toFixed(2));
    }
//计算实际请款金额
    var applyAmount = (parseFloat(actualCost)+parseFloat(taxAmount));

    if (isNaN(applyAmount)){
        $("#editForm [name = 'applyAmount']").val(0);
    }else {
        $("#editForm [name = 'applyAmount']").val(parseFloat(applyAmount).toFixed(2));
    }
    //应付合计
    var  sumOutgo = $("#editForm [name = 'sumOutgo']").val() == "" ? 0 : $("#editForm [name = 'sumOutgo']").val();
    // 税金
    var taxAmount =$("#editForm [name='taxAmount']").val() == "" ? 0 : $("#editForm [name = 'taxAmount']").val();


    //计算实际请款抹零
    var outgoEraseAmount =  (parseFloat(actualCost)+parseFloat(taxAmount)-parseFloat(applyAmount));

    if (isNaN(outgoEraseAmount)){
        $("#editForm [name = 'outgoEraseAmount']").val(0);
    }else {
        $("#editForm [name = 'outgoEraseAmount']").val(parseFloat(outgoEraseAmount).toFixed(2));
    }

//计算抹零
    var costEraseAmount = (parseFloat(sumOutgo) - parseFloat(actualCost));
    //实际抹零金额
    var actualCombined = parseFloat(outgoEraseAmount) + parseFloat(costEraseAmount);

    if (isNaN(actualCombined)){
        $("#editForm [name = 'actualCombined']").val(0);
    }else {
        $("#actualCombined").val(parseFloat(actualCombined).toFixed(2));
    }
    //请款成本抹零赋值
    if (isNaN(costEraseAmount)){
        $("#editForm [name = 'costEraseAmount']").val(0);
    }else {

        $("#editForm [name = 'costEraseAmount']").val(parseFloat(costEraseAmount).toFixed(2));
    }

    var applyMoney =$("#applyAmount").val();
    var pc= processCompanyCode();
    if (pc && pc.contains(user.companyCode)){
        $(".licence").show();
        $("#mediaGroupLeader").removeAttrs("data-divId");
        $("#mediaGroupLeader1").removeAttrs("data-divId");
    }else {
        if(mt == 1){
            if (applyMoney>2000){
                $(".licence").hide();
                $("#mediaGroupLeader").attr("data-divId","hide");
                $("#mediaGroupLeader1").attr("data-divId","hide");
                // $("#thirdDiv select[name='mediaGroupLeader']").html("");
            }else {
                $(".licence").show();
                $("#mediaGroupLeader").removeAttrs("data-divId");
                $("#mediaGroupLeader1").removeAttrs("data-divId");
            }
        }else {
            if ($("#mediaTypeId").val()==3){
                if ($("#applyAmount").val()>2000){
                    $(".licence").hide();
                    $("#mediaGroupLeader").attr("data-divId","hide");
                    $("#mediaGroupLeader1").attr("data-divId","hide");
                    // $("#thirdDiv select[name='mediaGroupLeader']").html("");
                }else {
                    $(".licence").show();
                    $("#mediaGroupLeader").removeAttrs("data-divId");
                    $("#mediaGroupLeader1").removeAttrs("data-divId");
                }
            }else {
                if (applyMoney>2000){
                    $(".licence").hide();
                    $("#mediaGroupLeader").attr("data-divId","hide");
                    $("#mediaGroupLeader1").attr("data-divId","hide");
                    // $("#thirdDiv select[name='mediaGroupLeader']").html("");
                }else {
                    $(".licence").show();
                    $("#mediaGroupLeader").removeAttrs("data-divId");
                    $("#mediaGroupLeader1").removeAttrs("data-divId");
                }
            }

        }
    }

}


//根据实际请款金额判断是否需要组长审核
function applyMedia() {
    var companyCode =$("#companyCodeUser").val();
    if ($("#mediaTypeId").val()) {
        var mt = mediaType($("#mediaTypeId").val());
    }
    var applyMoney =$("#applyAmount").val();
    //获取税点
    if ($("#editForm [name='outgoTax']").val() == 3){
        var outgoTax = parseFloat($("#outgoTax1").val())/100;
    }else {
        var outgoTax = $("#editForm [name='outgoTax']").val() == "" ? 0 : $("#editForm [name = 'outgoTax']").val();

    }
    // //计算实际请款抹零
    // var outgoEraseAmount =  (parseFloat($("#sumOutgo").val())-parseFloat($("#applyAmount").val()));
    // $("#editForm [name = 'outgoEraseAmount']").val(parseFloat(outgoEraseAmount).toFixed(2));

    // if (isNaN($("#outgoTax1").val())){
    //     $("#outgoTax1").val(0);
    // }


    //应付合计
    var  sumOutgo = $("#editForm [name = 'sumOutgo']").val() == "" ? 0 : $("#editForm [name = 'sumOutgo']").val();
    //实际请款成本
    var actualCost=$("#editForm [name='actualCost']").val() == "" ? 0 : $("#editForm [name = 'actualCost']").val();

    //获得实际请款金额
    var applyAmount= $("#editForm [name='applyAmount']").val() =="" ? 0:$("#editForm [name = 'applyAmount']").val();


    // 计算税金
    var taxAmount = (parseFloat(actualCost)*parseFloat(outgoTax));
    if (isNaN(taxAmount)){
        $("#editForm [name = 'taxAmount']").val(0);
    }else {
        //税金赋值
        $("#editForm [name = 'taxAmount']").val(parseFloat(taxAmount).toFixed(2));
    }


    // if (isNaN(applyAmount)){
    //     $("#editForm [name = 'applyAmount']").val(0);
    // }else {
    //     $("#editForm [name = 'applyAmount']").val(parseFloat(applyAmount).toFixed(2));
    // }

    //计算实际请款抹零
    var outgoEraseAmount =  (parseFloat(actualCost)+parseFloat(taxAmount)-parseFloat(applyAmount));
    $("#editForm [name = 'outgoEraseAmount']").val(parseFloat(outgoEraseAmount).toFixed(2));

    //计算抹零
    var costEraseAmount = (parseFloat(sumOutgo) - parseFloat(actualCost));
    //实际抹零金额
    var actualCombined = parseFloat(outgoEraseAmount) + parseFloat(costEraseAmount);
    $("#actualCombined").val(parseFloat(actualCombined).toFixed(2));
    var pc= processCompanyCode();
    if (pc && pc.contains(user.companyCode)){
        $(".licence").show();
        $("#mediaGroupLeader").removeAttrs("data-divId");
        $("#mediaGroupLeader1").removeAttrs("data-divId");
    }else {
        if(mt == 1){
            if (applyMoney>2000){
                $(".licence").hide();
                $("#mediaGroupLeader").attr("data-divId","hide");
                $("#mediaGroupLeader1").attr("data-divId","hide");
                // $("#thirdDiv select[name='mediaGroupLeader']").html("");
            }else {
                $(".licence").show();
                $("#mediaGroupLeader").removeAttrs("data-divId");
                $("#mediaGroupLeader1").removeAttrs("data-divId");
            }
        }else {
            if ($("#mediaTypeId").val()==3){
                if ($("#applyAmount").val()>2000){
                    $(".licence").hide();
                    $("#mediaGroupLeader").attr("data-divId","hide");
                    $("#mediaGroupLeader1").attr("data-divId","hide");
                    // $("#thirdDiv select[name='mediaGroupLeader']").html("");
                }else {
                    $(".licence").show();
                    $("#mediaGroupLeader").removeAttrs("data-divId");
                    $("#mediaGroupLeader1").removeAttrs("data-divId");
                }
            }else {
                if (applyMoney>2000){
                    $(".licence").hide();
                    $("#mediaGroupLeader").attr("data-divId","hide");
                    $("#mediaGroupLeader1").attr("data-divId","hide");
                    // $("#thirdDiv select[name='mediaGroupLeader']").html("");
                }else {
                    $(".licence").show();
                    $("#mediaGroupLeader").removeAttrs("data-divId");
                    $("#mediaGroupLeader1").removeAttrs("data-divId");
                }
            }

        }
    }


}

//根据板块id判断该板块属于什么板块类型
function mediaType(mediaTypeId) {
    var meType;
    if(mediaTypeId == null){
        swal("未选择到稿件！")
        return;
    }
    $.ajax({
        type:"post",
        data:{mediaTypeId:mediaTypeId},
        url:baseUrl +"/outgo/selectMediaType",
        dataType:"json",
        async:false,
        success:function (data) {
            meType=data;
            if(data.code == 1002){
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
            }
        },
        error: function () {
            Ladda.stopAll();
        }
    });
    return meType;
}

function changeLicence(dataValue) {
    if (!dataValue || dataValue == 1){
        $(".invoice").show();
        $("#invoiceFlag1").val(1);
    }else{
        $(".invoice").hide();
        $("#editForm [name='outgoTax']").val(0);
        $("#taxAmount").val(0);
        $("#invoiceRise").val("");
        $("#applyAmount").val($("#actualCost").val());
        $("#outgoEraseAmount").val(0);
        $("#invoiceFlag1").val(2);
        $("#actualCombined").val($("#costEraseAmount").val());
        $("#invoiceCode").val("");
        $("#invoiceTax").val("");
        $("#editForm input[name='invoiceType']").val("");
        applyMedia();
    }
}

function showInputTaxPoint() {
    var outgoTaxAmount = $("#outgoTax").val();
    if (outgoTaxAmount==3){
        $("#inputTaxPoint").show();
        $("#outgoTax1").val(0);
    }else {
        $("#inputTaxPoint").hide();
        $("#outgoTax1").val(0);

    }
}

// //税金计算 = 实际请款成本（actualCost） * 税点（outgoTax）
// //请款成本抹零计算 = 应付合计 （） - 实际请款成本（actualCost）
// //实际抹零金额 = 请款成本抹零 + 实际请款抹零
// function  calcuteTaxAmount() {
//     //应付合计
//     var  sumOutgo = $("#editForm [name = 'sumOutgo']").val() == "" ? 0 : $("#editForm [name = 'sumOutgo']").val();
//     //实际请款成本
//     var actualCost=$("#editForm [name='actualCost']").val() == "" ? 0 : $("#editForm [name = 'actualCost']").val();
//     //获取税点
//     if ($("#editForm [name='outgoTax']").val() == 3){
//         var outgoTax = parseFloat($("#outgoTax1").val())/100;
//     }else {
//         var outgoTax = $("#editForm [name='outgoTax']").val() == "" ? 0 : $("#editForm [name = 'outgoTax']").val();
//
//     }
//     //获得实际请款金额
//     var applyAmount= $("#editForm [name='applyAmount']").val() =="" ? 0:$("#editForm [name = 'applyAmount']").val();
//
//     if(actualCost>sumOutgo){
//         swal("实际请款成本要小于合计成本！");
//         $("#actualCost").val(0)
//         return;
//     }
//     // 计算税金
//     var taxAmount = (parseFloat(actualCost)*parseFloat(outgoTax));
//     //税金赋值
//     $("#editForm [name = 'taxAmount']").val(parseFloat(taxAmount).toFixed(2));
//     //计算抹零
//     var costEraseAmount = (parseFloat(sumOutgo) - parseFloat(actualCost));
//     //请款成本抹零赋值
//     $("#editForm [name = 'costEraseAmount']").val(parseFloat(costEraseAmount).toFixed(2));
//     //计算实际请款抹零
//     var outgoEraseAmount =  (parseFloat(sumOutgo)-parseFloat(applyAmount));
//     $("#editForm [name = 'outgoEraseAmount']").val(parseFloat(outgoEraseAmount).toFixed(2));
//     //实际抹零金额
//     var actualCombined = parseFloat(outgoEraseAmount) + parseFloat(costEraseAmount);
//     $("#actualCombined").val(parseFloat(actualCombined).toFixed(2));
//
// }
function backfill(t) {
    var lock = true;
    if($("#invoiceCode").val()==""){
        swal("未填写发票编号！")
        return;
    }
    if($("#invoiceTax").val()==""){
        swal("未填写税点！")
        return;
    }
    if($("#editForm input[name='invoiceType']").val()==""){
        swal("未选择发票类型！")
        return;
    }
    var formData =$("#editForm").serializeJson();
    var tips = "确认保存？";
    layer.confirm(tips,{
        btn: ['确定','取消'],
        shade:false
    },function (index) {
        layer.close(index);
        startModal("#"+t.id);
        if (lock){
            lock=false;
            $.ajax({
                type:"post",
                data:JSON.stringify(formData),
                url:baseUrl +"/outgo/backfill",
                dataType:"json",
                async:false,
                processData: false,   // jQuery不要去处理发送的数据
                contentType: false,
                success:function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    if (data.code == 200) {
                        $("#editModal").modal('hide');
                        swal(data.data.message);
                        $("#outAccountIds").val("");
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/outgo/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else if(data.code == 1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        $("#editModal").modal('hide');
                        if (getResCode(data))
                            return;
                    }
                },
                error: function () {
                    Ladda.stopAll();
                }
            });
        }

    },function () {
        return;
    });


}

function back() {
    triggerPageBtnClick("/fee/resetStatistical","querySearch");
    closeCurrentTab();
}


/**
 * 加载用户所拥有的媒体板块
 */
function userMedaiPlateList() {
    var mediaUserPlateMap=[];
    $.ajax({
        url: baseUrl + "/mediaPlate/userId",  //mediaType/listByUserId
        data: {"userId": user.id},
        type: "post",
        dataType: "json",
        success: function (data) {
            if (data) {
                for (var i = 0; i < data.length; i++) {
                    mediaUserPlateMap.push(data[i].id)
                }
            }
        }
    });
    return mediaUserPlateMap;
}



function supplierPhone(value, grid, rows) {
    // var flag = false;
    // if (rows.hasOwnProperty('mediaTypeId')){
    //     var plateIds = rows.mediaTypeId;
    //     if (plateIds) {
    //         if (mediaUserPlateMap.contains(plateIds)) {
    //             //当前用户的板块包含了该供应商的板块
    //             flag = true;
    //         }
    //     }
    // }

    value = rows.phone || "";
    if(value){
        // if((rows.supplierCreator == user.id )||(flag && hasRoleMJBZ()) || (flag && hasRoleMJZZ()) || (flag && hasRoleMJZJ()) || hasRoleCW() ){
        //     return value;
        // }else {
            if(value.length >= 11){
                var start = value.length > 11 ? "*****" : "****";
                return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
            }else if(value.length >= 3){
                return value[0] + "***" + value[value.length - 1];
            }else {
                return "**";
            }

    }else {
        return "";
    }
}

//下载打印功能
var downLoadAndPrintObj = {
    dowloadAndPritPermission: false, //打印权限控制
    state:{"-1":"审核驳回","0":"已保存","1":"已完成","2":"审核通过","3":"组长审核","4":"部长审核","5":"总监审核","6":"财务总监审核","7":"副总经理审核",
        "8":"总经理审核","9":"会计确认出款","10":"业务员确认","12":"财务部长审核","16":"出纳出款","23":"唤醒中","26":"媒介回填开票信息"},
    invoiceFlag:{"1":"是", "2":"否"},
    type:{"0":"备用金", "1":"其他"},
    moneyList:['outgoSum','fundAmount','actualCost','costEraseAmount','applyAmount','outgoEraseAmount','actualCombined','payAmount'],
    downloadMap:{},
    loadFlowPrintPermission:function () {
        $.ajax({
            type: "post",
            url: baseUrl + "/refund/getFlowPrintPermission",
            data: null,
            dataType: "json",
            async: false,
            success: function (data) {
                if(data.code == 200){
                    downLoadAndPrintObj.dowloadAndPritPermission = true;
                    $("#batchDownload").css("display", "inline-block");
                    $("#batchPrint").css("display", "inline-block");
                }else {
                    downLoadAndPrintObj.dowloadAndPritPermission = false;
                    $("#batchDownload").css("display", "none");
                    $("#batchPrint").css("display", "none");
                }
            }
        });
    },
    viewModalShow: function (id, process) {
        layer.open({
            type: 1,
            title: 0,
            zIndex: 10,
            content: $("#outgoViewModal").html(),
            btn: ['下载','打印','取消'],
            area: ['70%', '60%'],
            closeBtn: 0,
            shadeClose: 0,
            resize: false,
            move: '.modalTitle',
            moveOut: true,
            success: function(layero, index){
                downLoadAndPrintObj.downloadMap = {};
                $.ajax({
                    type: "post",
                    url: baseUrl + "/outgo/view",
                    data: {id: id},
                    dataType: "json",
                    async: false,
                    success: function (data) {
                        if(data.code == 200){
                            var title = "【请款】"+data.data.entity["code"]+"-"+data.data.entity["applyName"]+"-"+data.data.entity["applyTime"];
                            downLoadAndPrintObj.downloadMap["modalTitle"] = title;
                            $(layero[0]).find("div .modalTitle").text(title);
                            data.data.entity["outgoSum"] = data.data.outgoSum || "0";//应付合计
                            //实际抹零
                            data.data.entity["actualCombined"] = parseFloat(data.data.entity['outgoEraseAmount'] || 0) + parseFloat(data.data.entity['costEraseAmount'] || 0)
                            data.data.entity["fundAmount"] = data.data.entity["fundAmount"] || "0";//备用金
                            for (var attr in data.data.entity) {
                                //是否开票
                                if(attr == 'invoiceFlag'){
                                    if(data.data.entity[attr] == 1){
                                        $(layero[0]).find(".taxDiv").css("display", "flex");
                                    }else {
                                        $(layero[0]).find(".taxDiv").css("display", "none");
                                    }
                                    $(layero[0]).find("div ."+attr).text(downLoadAndPrintObj.invoiceFlag[data.data.entity[attr]]  || "");
                                    downLoadAndPrintObj.downloadMap[attr] = data.data.entity[attr] || "";
                                    downLoadAndPrintObj.downloadMap['invoiceFlagName'] = downLoadAndPrintObj.invoiceFlag[data.data.entity[attr]]  || "";
                                }else if(attr == "payAmount"){
                                    var payAmount = parseFloat(data.data.entity["applyAmount"] || "0") - parseFloat(data.data.entity["fundAmount"] || "0");
                                    $(layero[0]).find("div ."+attr).text(payAmount);
                                    downLoadAndPrintObj.downloadMap[attr] = payAmount || "0";
                                }else if(attr == "state"){
                                    $(layero[0]).find("div ."+attr).text(downLoadAndPrintObj.state[data.data.entity[attr]] || "");
                                    downLoadAndPrintObj.downloadMap[attr] = downLoadAndPrintObj.state[data.data.entity[attr]]  || "";
                                }else if(attr == "outgoTax"){
                                    $(layero[0]).find("div ."+attr).text(data.data.entity[attr] ? data.data.entity[attr] * 100 : "");
                                    downLoadAndPrintObj.downloadMap[attr] = data.data.entity[attr] ? data.data.entity[attr] * 100 : "";
                                }else {
                                    if(downLoadAndPrintObj.moneyList.contains(attr)){
                                        $(layero[0]).find("div ."+attr).text(data.data.entity[attr] || "0");
                                        downLoadAndPrintObj.downloadMap[attr] = data.data.entity[attr] || "0";
                                    }else {
                                        $(layero[0]).find("div ."+attr).text(data.data.entity[attr] || "");
                                        downLoadAndPrintObj.downloadMap[attr] = data.data.entity[attr] || "";
                                    }
                                }
                            }
                            var borrowList = data.data.list;
                            if(borrowList && borrowList.length > 0){
                                $(layero[0]).find(".borrowTable").css("display", "flex");
                                var borrowHtml = "";
                                $.each(borrowList, function (i, item) {
                                    borrowHtml += "<tr>\n" +
                                        "              <td>"+(item.code || "")+"</td>\n" +
                                        "              <td>"+(item.title || "")+"</td>\n" +
                                        "              <td>"+(downLoadAndPrintObj.type[item.type] || "")+"</td>\n" +
                                        "              <td>"+(item.apply_name || "")+"</td>\n" +
                                        "              <td>"+(item.dept_name || "")+"</td>\n" +
                                        "              <td>"+(item.apply_amount || "")+"</td>\n" +
                                        "              <td>"+(item.repay_amount || "")+"</td>\n" +
                                        "              <td>"+(item.remain_amount || "")+"</td>\n" +
                                        "              <td>"+(item.amount || "")+"</td>\n" +
                                        "          </tr>";
                                    item.type = (downLoadAndPrintObj.type[item.type] || "");
                                });
                                downLoadAndPrintObj.downloadMap["borrowTable"] = JSON.stringify(borrowList);
                                $(layero[0]).find(".borrowTable").find("tbody").html(borrowHtml);
                            }else {
                                $(layero[0]).find(".borrowTable").css("display", "none");
                            }
                        }
                    }
                });
                if(process){
                    $.ajax({
                        type: "post",
                        url: baseUrl + "/process/history",
                        data: {dataId:id, process:process},
                        dataType: "json",
                        async: false,
                        success: function (data) {
                            if(data.code == 200){
                                $(layero[0]).find(".auditTable").css("display", "flex");
                                var html = "";
                                if(data.data.data && data.data.data.length > 0){
                                    $.each(data.data.data, function (i, item) {
                                        html += "<tr>\n" +
                                            "        <td>"+(item.name || "")+"</td>\n" +
                                            "        <td>"+(item.user || "")+"</td>\n" +
                                            "        <td>"+(item.desc || "")+"</td>\n" +
                                            "        <td>"+(item.time ? new Date(item.time).format("yyyy-MM-dd hh:mm:ss") : "")+"</td>\n" +
                                            "    </tr>";
                                        var descArr = item.desc.split("</");
                                        item.descName = descArr && descArr.length > 0 ? descArr[0].substring(descArr[0].indexOf(">")+1) : "";
                                    });
                                    downLoadAndPrintObj.downloadMap["auditTable"] = JSON.stringify(data.data.data);
                                    $(layero[0]).find(".auditTable").find("tbody").html(html);
                                }else {
                                    $(layero[0]).find(".auditTable").css("display", "none");
                                }
                            }else {
                                $(layero[0]).find(".auditTable").css("display", "none");
                            }
                        }
                    });
                }
            },
            yes: function (index, layero) {
                $.ajax({
                    type: "post",
                    url: baseUrl + "/outgo/downloadData",
                    data: downLoadAndPrintObj.downloadMap,
                    dataType: "json",
                    async: false,
                    success: function (data) {
                        if(data.code == 200){
                            if(data.data.message){
                                layer.msg(data.data.message, {time: 2000, icon: 5});
                            }
                            if(data.data.file){
                                window.location.href = data.data.file;
                            }
                        }else {
                            layer.msg(data.msg, {time: 2000, icon: 5});
                        }
                    }
                });
                return false;
            },
            btn2: function(index, layero){
                var wind = window.open("",'newwindow', 'height=300, width=700, top=100, left=100, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=n o, status=no');
                wind.document.body.innerHTML = $(layero[0]).find("#modalContentWrap")[0].outerHTML;
                wind.print();
                return false;
            }
        });
    },
    batchPrintData:function () {
        var html = "";
        var wind1  = window.open("",'newwindow', 'height=300, width=700, top=100, left=100, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=n o, status=no');
        $.ajax({
            type: "post",
            url: baseUrl + "/outgo/batchPrintData",
            data: $("#queryForm").serializeJson(),
            dataType: "json",
            async: false,
            success: function (data) {
                if(data.code == 200){
                    if(data.data.list && data.data.list.length > 0){
                        $.each(data.data.list, function (k, outgo) {
                            //备用金详情
                            if(outgo["borrowTable"] && outgo["borrowTable"].length > 0){
                                $("#modalContentWrap").find(".borrowTable").css("display", "flex");
                            }else {
                                $("#modalContentWrap").find(".borrowTable").css("display", "none");
                            }
                            for (var attr in outgo) {
                                $("#modalContentWrap").find(".payTime").text("");
                                //是否开票
                                if(attr == 'invoiceFlag'){
                                    if(outgo[attr] == 1){
                                        $("#modalContentWrap").find(".taxDiv").css("display", "flex");
                                    }else {
                                        $("#modalContentWrap").find(".taxDiv").css("display", "none");
                                    }
                                    $("#modalContentWrap").find("div ."+attr).text(downLoadAndPrintObj.invoiceFlag[outgo[attr]] || "");
                                }else if(attr == "auditTable"){
                                    var auditHtml = "";
                                    if(outgo[attr] && outgo[attr].length > 0){
                                        $.each(outgo[attr], function (i, item) {
                                            auditHtml += "<tr>\n" +
                                                "        <td>"+(item.name || "")+"</td>\n" +
                                                "        <td>"+(item.user || "")+"</td>\n" +
                                                "        <td>"+(item.descName || "")+"</td>\n" +
                                                "        <td>"+(item.time ? new Date(item.time).format("yyyy-MM-dd hh:mm:ss") : "")+"</td>\n" +
                                                "    </tr>";
                                        });
                                        $("#modalContentWrap").find(".auditTable").css("display", "flex");
                                        $("#modalContentWrap").find(".auditTable").find("tbody").html(auditHtml);
                                    }else {
                                        $("#modalContentWrap").find(".auditTable").css("display", "none");
                                    }
                                }else if(attr == "borrowTable"){
                                    if(outgo["borrowTable"] && outgo["borrowTable"].length > 0){
                                        var borrowHtml = "";
                                        $.each(outgo["borrowTable"], function (i, item) {
                                            borrowHtml += "<tr>\n" +
                                                "              <td>"+(item.code || "")+"</td>\n" +
                                                "              <td>"+(item.title || "")+"</td>\n" +
                                                "              <td>"+(item.type || "")+"</td>\n" +
                                                "              <td>"+(item.apply_name || "")+"</td>\n" +
                                                "              <td>"+(item.dept_name || "")+"</td>\n" +
                                                "              <td>"+(item.apply_amount || "")+"</td>\n" +
                                                "              <td>"+(item.repay_amount || "")+"</td>\n" +
                                                "              <td>"+(item.remain_amount || "")+"</td>\n" +
                                                "              <td>"+(item.amount || "")+"</td>\n" +
                                                "          </tr>";
                                        });
                                        $("#modalContentWrap").find(".borrowTable").find("tbody").html(borrowHtml);
                                    }
                                }else {
                                    if(downLoadAndPrintObj.moneyList.contains(attr)){
                                        $("#modalContentWrap").find("."+attr).text("0");
                                        $("#modalContentWrap").find("."+attr).text(outgo[attr] || "0");
                                    }else {
                                        $("#modalContentWrap").find("."+attr).text("");
                                        $("#modalContentWrap").find("."+attr).text(outgo[attr] || "");
                                    }

                                }
                            }
                            html += $("#modalContentWrap")[0].outerHTML + "\n";
                        });
                    }else {
                        layer.msg('无请款打印数据！', {time: 2000, icon: 6});
                        return;
                    }
                }else {
                    layer.msg(data.msg, {time: 2000, icon: 5});
                    return;
                }
            }
        });
        if(wind1){
            if(html){
                wind1.document.body.innerHTML = html;
                wind1.print();
            }else {
                wind1.close();
            }
        }
    },
    batchDownloadData:function () {
        $.ajax({
            type: "post",
            url: baseUrl + "/outgo/batchDownloadData",
            data: $("#queryForm").serializeJson(),
            dataType: "json",
            async: false,
            success: function (data) {
                if(data.code == 200){
                    if(data.data.message){
                        layer.msg(data.data.message, {time: 2000, icon: 5});
                    }
                    if(data.data.file){
                        window.location.href = data.data.file;
                    }
                }else {
                    layer.msg(data.msg, {time: 2000, icon: 5});
                }
            }
        });
    }
}

//供应商编辑
var supplierEditObj = {
    checkSupplierUrl: "/supplier/checkSupplier",
    editSupplierUrl: "/supplier/updateSupplier",
    judgeFeeOutgo:function (supplier) {
        //企业供应商 只有 公司名称标准  并且  对接人规范  才能进行请款
        //个人供应商 需要对接人规范才能请款
        var message = "";
        if(!supplier["standarPhoneFlag"] || supplier["standarPhoneFlag"] != 1){
            message = "请款标准：企业供应商，必须满足供应商名称标准，供应商联系人规范；个人供应商，必须满足供应商联系人规范。当前选中的供应商联系人不规范，不满足请款要求，";
            if(supplier["creator"] != user.id){
                message += "请联系当前供应商责任人进行供应商联系人规范操作！";
            }else {
                message += "请进行供应商联系人规范操作";
            }
        }else {
            //如果是企业供应商  并且   公司不标准
            if(supplier["supplierNature"] && supplier["supplierNature"] != 1 && (!supplier["standarCompanyFlag"] || supplier["standarCompanyFlag"] != 1)){
                message = "请款标准：企业供应商，必须满足供应商名称标准，供应商联系人规范；个人供应商，必须满足供应商联系人规范。当前选中的供应商名称不标准，不满足请款要求，";
                if(supplier["creator"] != user.id){
                    message += "请联系当前供应商责任人进行供应商名称标准化操作！";
                }else {
                    message += "请进行供应商名称标准化操作";
                }
            }
        }
        return message;
    },
    addSupplierCheckRule:function (modalId) {
        var icon = "<i class='fa fa-times-circle'></i> ";
        $("#"+modalId).find("form").validate({
            rules: {
                phone: {
                    required: true,
                    maxlength: 11,
                    remote: {
                        url: baseUrl + supplierEditObj.checkSupplierUrl, //后台处理程序
                        type: "post", //数据发送方式
                        dataType: "json", //接受数据格式
                        data: { // 要传递的数据
                            "phone": function () {
                                return $("#"+modalId).find("form input[name='phone']").val();
                            },
                            "name": function () {
                                return $("#"+modalId).find("form input[name='name']").val();
                            },
                            "id": function () {
                                return $("#"+modalId).find("form input[name='id']").val() || "";
                            }
                        },
                        dataFilter: function (data) {
                            data = JSON.parse(data);
                            if (data.code == 200) {
                                return true;
                            } else {
                                $("#"+modalId).find("form input[name='phone']").focus();
                                return false;
                            }
                        }
                    }
                },
            },
            messages: {
                name: {remote: icon + "很抱歉，供应商联系人已经存在！"},
            }
        });
    },
    editSupplierClick:function (supplier) {
        $("#mediaSupplierEditModal").find(".companyWrap").html("");
        $("#mediaSupplierEditModal").find("form").find("input[name='name']").removeAttr("readonly");
        $("#mediaSupplierEditModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsYes").css("display", "none");
        $("#mediaSupplierEditModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsNo").css("display", "none");
        $("#mediaSupplierEditModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsYes").css("display", "none");
        $("#mediaSupplierEditModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsNo").css("display", "none");
        var inputNameList = ["supplierNature", "standarCompanyFlag", "standarPhoneFlag"];
        $("#oldStandarCompanyFlag").val(supplier["standarCompanyFlag"] || "");//记录修改前的联系人标准状态，不能由规范改为非标准
        $("#oldStandarPhoneFlag").val(supplier["standarPhoneFlag"] || "");//记录修改前的联系人规范状态，不能由规范改为非规范
        $("#oldSupplierNature").val(supplier["supplierNature"] || "");
        $("#oldCompanyName").val(supplier["name"] || "");
        $("#oldContactor").val(supplier["contactor"] || "");
        for(var key in supplier){
            if($("#mediaSupplierEditModal").find("form [name='"+key+"']").length > 0){
                var val = supplier[key] || "";
                if(!val && inputNameList.contains(key)){
                    val = 0;
                }
                $("#mediaSupplierEditModal").find("form [name='"+key+"']").val(val);
            }
        }
        if(supplier["supplierNature"] == 1){
            //个体供应商可以改变成企业供应商
            $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("button").css("display", "inline-block");
            $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("label").css("display","none");
            var ele = $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("button.personBtnCls")[0];
            $("#mediaSupplierEditModal").find("form [name='supplierNature']").val(0);//由于下面方法需要值不同才会触发，所以改成默认值
            companyObj.natureClick(ele, 1);//选中个体供应商按钮
        }else {
            //企业用户供应商性质不能改变
            $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("button").css("display", "none");
            $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("label").css("display","inline-block");
            $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("label").text("企业供应商");

            //判断供应商公司名称是否标准
            if(supplier["standarCompanyFlag"] == 1){
                $("#mediaSupplierEditModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsYes").css("display", "inline-block");
                $("#mediaSupplierEditModal").find("form").find("input[name='name']").attr("readonly", true);//非标准公司能修改
            }else {
                $("#mediaSupplierEditModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsNo").css("display", "inline-block");
                $("#mediaSupplierEditModal").find("form").find("input[name='name']").removeAttr("readonly");//标准公司不能修改
            }
        }
        //判断供应商联系人是否标准
        if(supplier["standarPhoneFlag"] == 1){
            $("#mediaSupplierEditModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsYes").css("display", "inline-block");
        }else {
            $("#mediaSupplierEditModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsNo").css("display", "inline-block");
        }
        $("#editModal").modal("toggle");
        $("#mediaSupplierEditModal").modal("toggle");
    },
    editSupplier:function (t) {
        var $form = $(t).closest(".modal-content").find("form");
        if(!$form.valid()){
            return;
        }
        var jsonData = $form.serializeForm();
        //供应商名称不能由标准改为非标准
        if($("#oldStandarCompanyFlag").val() == "1" && $("#oldStandarCompanyFlag").val() != jsonData.standarCompanyFlag){
            layer.msg("供应商名称不能由标准改为非标准！", {time: 2000, icon: 5});
            return;
        }
        //供应商联系人不能由规范改为非规范
        if($("#oldStandarPhoneFlag").val() == "1" && $("#oldStandarPhoneFlag").val() != jsonData.standarPhoneFlag){
            layer.msg("供应商联系人不能由规范改为不规范！", {time: 2000, icon: 5});
            return;
        }
        startModal("#" + $(t).attr("id"));
        companyObj.requestData(jsonData, supplierEditObj.editSupplierUrl, "post", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code == 200){
                layer.msg("编辑供应商联系人成功！", {time: 2000, icon: 6});

                $("#select_supplier_table_logs").emptyGridParam() ;
                $("#select_supplier_table_logs").jqGrid('setGridParam', {
                    datatype: "json",
                }).trigger("reloadGrid"); //重新载入
                $("#editModal").modal("toggle");
                $("#mediaSupplierEditModal").modal("toggle");
            }else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        }, function () {
            Ladda.stopAll();
        });
    },
}

//公司筛选
var companyObj = {
    companySearchUrl: "/company/companySearch",
    checkCompanyUrl:  "/company/checkCompany",
    currentCompanyName:"",
    firstPageTotal: 0, //第一页查询缓存表数据总数
    requestData: function (data, url, requestType,dataType,async,callBackFun,callErrorFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if(callErrorFun){
            param.error = callErrorFun;
        }
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    natureClick:function (t, supplierNature) {
        $(t).closest("form").find(".companyWrap").html("");
        //发生改变才进行处理
        if(supplierNature != $(t).closest("form").find("input[name='supplierNature']").val()){
            if(supplierNature == 1){
                $(t).closest("form").find("input[name='name']").val("个体供应商");
                $(t).closest("form").find("input[name='name']").attr("readonly", true);
                //设置公司名称非标准
                $(t).closest("form").find(".companyTipsYes").hide();
                $(t).closest("form").find(".companyTipsNo").hide();
                $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
            }else {
                $(t).closest("form").find("input[name='name']").val("");
                $(t).closest("form").find("input[name='name']").removeAttr("readonly");
            }
            //改变按钮颜色
            $(t).closest("div").find("button").each(function (i, btn) {
                $(btn).removeClass("btn-info");
                if(!$(btn).hasClass("btn-white")){
                    $(btn).addClass("btn-white");
                }
            });
            $(t).removeClass("btn-white");
            if(!$(t).hasClass("btn-info")){
                $(t).addClass("btn-info");
            }
            $(t).closest("form").find("input[name='supplierNature']").val(supplierNature);
        }
    },
    renderCompanyItem:function (page, pageSize, companyList) {
        var html = "";
        if(companyList && companyList.length > 0){
            $.each(companyList, function (m, company) {
                html += "<div onmousedown='companyObj.chooseCompany(this);' class=\"companyNameItem\" title=\""+(company.companyName || "")+"\"><span>"+(company.companyName || "")+"</span></div>";
            });
        }
        return html;
    },
    search:function (t) {
        $(t).closest("form").find(".companyWrap").html("");
        var keyword = $(t).closest("form").find("input[name='name']").val();
        if(!keyword){
            if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")){
                $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
            }
            return;
        }else {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
        }
        layui.use('flow', function(){
            var flow = layui.flow;
            flow.load({
                elem: $(t).closest("form").find(".companyWrap"),
                isAuto: false,
                done: function(page, next){
                    //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                    //请求数据
                    var param = {keyword:keyword};
                    param.page = page; //页码
                    param.size = 20; //每页数据条数
                    companyObj.requestData(param, companyObj.companySearchUrl, "post", "json", true, function (data) {
                        //第一页是从缓存表拿数据，记录数据总数
                        if(page == 1){
                            companyObj.firstPageTotal = data.total;
                        }
                        next(companyObj.renderCompanyItem(page, param.size, data.list), page < data.pages); //如果小于总页数，则继续
                    });
                }
            });
        });
    },
    enterEvent:function (t, event) {
        if((event.keyCode == '13' || event.keyCode == 13)){
            companyObj.search(t);
        }
    },
    checkCompany: function (t) {
        var keyword = $(t).closest("form").find("input[name='name']").val();
        if(!keyword){
            $(t).closest("form").find(".companyTipsYes").hide();
            $(t).closest("form").find(".companyTipsNo").hide();
            $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
            return;
        }
        //如果是个体工商户，不需要校验
        if($(t).closest("form").find("input[name='supplierNature']").val() != 1){
            companyObj.requestData({keyword:keyword}, companyObj.checkCompanyUrl, "post", "json", false, function (data) {
                if(data.code == 200){
                    $(t).closest("form").find(".companyTipsYes").show();
                    $(t).closest("form").find(".companyTipsNo").hide();
                    $(t).closest("form").find("input[name='standarCompanyFlag']").val(1);
                }else {
                    $(t).closest("form").find(".companyTipsYes").hide();
                    $(t).closest("form").find(".companyTipsNo").show();
                    $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
                }
            });
        }
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")){
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    chooseCompany:function (t) {
        $(t).closest("form").find("input[name='name']").val($(t).attr("title") || "");
        //隐藏弹出筛选框
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")){
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOut:function (t) {
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")){
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOver:function (t) {
        //如果有内容则展示
        if($(t).closest("form").find(".companyWrap").find("div").length > 0){
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
            $(t).closest("form").find("input[name='name']").focus();
        }
    },
    //验证电话号码格式
    checkPhone: function (t) {
        var telPatten = /^[1]([3-9])[0-9]{9}$/;
        if(telPatten.test($(t).val())){
            $(t).closest("form").find(".phoneTipsYes").show();
            $(t).closest("form").find(".phoneTipsNo").hide();
            $(t).closest("form").find("input[name='standarPhoneFlag']").val(1);
        }else {
            $(t).closest("form").find(".phoneTipsYes").hide();
            $(t).closest("form").find(".phoneTipsNo").show();
            $(t).closest("form").find("input[name='standarPhoneFlag']").val(0);
        }
    },
}