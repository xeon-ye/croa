<!-- 声明序号 -->
var orderNumber = 0;
var addUrl = '/reimbursement/add';
var editUrl = '/reimbursement/edit';
var borrowList = new Array();

<!-- 添加一行报销记录方法 -->
function addCheck(costType, purpose, money, numberOfDocument, currentTotalPrice, flag) {
    orderNumber = orderNumber + 1;
    <!-- 判断形参内是否有实际的值 -->
    if (costType == null || typeof (costType) == undefined) {
        costType = "";
    }
    if (purpose == null || typeof (purpose) == undefined) {
        purpose = "";
    }
    if (money == null || money == "" || typeof (money) == undefined) {
        money = 0;
    }
    if (numberOfDocument == null || numberOfDocument == "" || typeof (numberOfDocument) == undefined) {
        numberOfDocument = 1;
    }
    if (currentTotalPrice == null || currentTotalPrice == "" || typeof (currentTotalPrice) == undefined) {
        currentTotalPrice = 0;
    }

    var styleFlag = "";
    if (flag == 1) {
        styleFlag = "border:0px"
    }
    <!-- 添加一行报销记录 -->
    var strContent = "<tr class='checkNum_" + orderNumber + "'>\n" +
        "<td align='center' id='order_" + orderNumber + "'>" + orderNumber + "</td>\n" +
        "<td style='padding-top:0;padding-bottom:0;'><input class=\"costType form-control\" style='" + styleFlag + "' name=\"costType\"  type=\"text\" value='" + costType + "' required=''/></td>\n" +
        "<td style='padding-top:0;padding-bottom:0;height: 60px'><textarea class=\"purpose form-control\" style='" + styleFlag + ";resize: none;height:34px;max-height:48px ' name=\"purpose\"  oninput=\"this.style.height=this.scrollHeight + 'px'\"  onpropertychange=\"this.style.height=this.scrollHeight + 'px'\" type=\"text\" required=''>" + purpose + "</textarea></td>\n" +
        "<td style='padding-top:0;padding-bottom:0;'><input class=\"money form-control\" style='" + styleFlag + "' name=\"money\"  type=\"number\" value='" + money + "' oninput=\"calculatePrice();\"/></td>\n" +
        "<td style='padding-top:0;padding-bottom:0;'><input class=\"numberOfDocument form-control\" style='" + styleFlag + "' name=\"numberOfDocument\"  type=\"number\" value='" + numberOfDocument + "'\"/></td>\n";
    if ($("#flag").val() == -2 || $("#flag").val() == -3) {
        var lastOrder = orderNumber - 1;
        $("#btn_removeCheck_" + lastOrder).attr("disabled", "disabled");
        strContent += "<td align='center'>" +
            "<button type=\"button\" id=\"btn_removeCheck_" + orderNumber + "\" name=\"btn_removeCheck\" class=\"btn btn-white btn-xs\" onclick=\"removeCheck();\">" +
            "<i class=\"glyphicon glyphicon-minus\"></i>" +
            "</button>" +
            "</td>\n";
    } else {
        strContent += "<td align='center'>" +
            "<button type=\"button\" id=\"btn_removeCheck_" + orderNumber + "\" name=\"btn_removeCheck\" class=\"btn btn-white btn-xs\" disabled='disabled' onclick=\"removeCheck();\">" +
            "<i class=\"glyphicon glyphicon-minus\"></i>" +
            "</button>" +
            "</td>\n";
    }
    strContent += "<input class=\"currentTotalPrice form-control\" name=\"currentTotalPrice\"  type=\"hidden\" value='" + currentTotalPrice + "'  readonly=\"readonly\">\n" +
        "</tr>";
    $("#checkTable").append(strContent);
}

<!-- 删除一行报销记录 -->
function removeCheck() {
    $(".checkNum_" + orderNumber + "").remove();
    orderNumber = orderNumber - 1;
    $("#btn_removeCheck_" + orderNumber).removeAttrs("disabled");
    calculatePrice();
}

//初始化业务部门
function getDept() {
    var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
    var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
    var deptDiv = document.getElementById("deptDiv");
    //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务  媒介部门
    if ((currentDeptQx || currentCompanyQx || isZC()) || user.dept.code == 'CW') {
        deptDiv.style.display = 'block';
        $("#selDept").click(function () {
            $("#deptModal").modal('toggle');
        });
        $('#treeview').treeview({
            data: [getTreeData(isZC())],
            onNodeSelected: function (event, data) {
                $("#companyCode1").val("");//每次选择时，先清空
                $("#deptId1").val("");//每次选择时，先清空
                $("#chooseDeptName").val(data.text);
                $("#deptModal").modal('hide');
                $("#deptId1").val(data.id);
                $("#userType").val(data.code);
            }
        });
        $("#cleanDept").click(function () {
            $("#userType").val("");
            $("#companyCode1").val("");//清空
            $("#deptId1").val("");
            $("#chooseDeptName").val("");
        });
    }
}

//判断当前用户是否总裁
var isZC = function () {
    var roles = user.roles;//获取用户角色
    var isZC = false;//是否总裁角色
    if (roles) {
        for (var i = 0; i < roles.length; i++) {
            if (roles[i].code == 'ZC' || roles[i].code == 'FZC') {
                isZC = true;
                break;
            }
        }
    }
    return isZC;
}

var requestData = function (data, url, requestType, callBackFun) {
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
function getTreeData(isZC) {
    var deptTreeData = {};
    var deptId = user.dept.id;//当前用户部门ID
    var deptCode = user.dept.code;//当前部门编码
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    if (deptCompanyCode == "JT" && (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL")) {
        requestData(null, "/dept/getRootDept", "POST", function (result) {
            var root = result.data.root;
            if (root) {
                deptId = root.id;//整个集团的业务和媒介部
            } else {
                deptId = 517;//整个集团的业务和媒介部
            }
        });
    } else if (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL") {
        requestData({companyCode: deptCompanyCode}, "/dept/getCompanyByCode", "POST", function (result) {
            var company = result.data.company;
            if (company) {
                deptId = company.id;//整个集团的业务和媒介部
            }
        });
    }
    //具体查询
    requestData({deptId: deptId}, "/dept/listForSonTreeView", "POST", function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
    /*var deptTreeData = {};
    $.ajax({
        type: "POST",
        url: baseUrl + "/dept/listForSonTreeView",
        data: {deptId: deptId},
        dataType: "json",
        async: false,
        success: function (result) {
            var arrays = result.data.list;
            if (arrays != null && arrays.length > 0) {
                deptTreeData = arrays[0];
            }
        }
    });
    return deptTreeData;*/
}

function submitForm(t, url, state) {
    var reimbursedMoney = $("#reimbursedMoney").val();
    var totalMoney = $("#totalMoney").val();

    if (parseFloat(reimbursedMoney) < parseFloat(totalMoney)) {
        swal("实报销金额不能大于应报销金额");
        return;
    }

    var lock = true;
    if ($("#editForm").valid()) {
        if ($("#totalMoney").val() < 0) {
            swal("实报销金额不能小于0，请核对金额后重试！");
            return;
        }
        var tips;
        if (state == 0) {
            tips = "确认保存？";
        } else {
            if (parseFloat(reimbursedMoney) > parseFloat(totalMoney)){
                tips = "您的实报销金额小于应报销金额？提交后不能取消!";
            }else {
                tips = "请确认报销信息？提交后不能取消!";
            }

        }
        $("#state").val(state);
        if ($(".costType").val() == "" || $(".costType").val() == null || typeof ($(".costType")) == undefined) {
            swal("请至少添加一个报销详情!");
            return;
        }
        if ($("#reimbursedMoney").val() == 0 && $("#totalMoney").val() == 0) {
            swal("请输入报销记录的费用!");
            return;
        }
        layer.confirm(tips, {
            btn: ['确定', '取消'], //按钮
            shade: false  //不显示遮罩
        }, function (index) {
            layer.close(index);//关闭当前模态层
            startModal("#save");//锁定按钮，防止重复提交
            startModal("#save2");//锁定按钮，防止重复提交
            startModal("#submit");//锁定按钮，防止重复提交
            var formData = new FormData($("#editForm")[0]);//序列化当前表单，并传出file类型
            //采购关联报销传值到后台
            if (lock) {
                lock = false;
                $.ajax({
                    type: "POST",
                    url: url,
                    data: formData,
                    dataType: "json",
                    async: true,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            if (state == 0) {
                                layer.msg(data.data.message, {time: 1000, icon: 6});
                                edit(data.data.entity.id);
                            } else {
                                swal(data.data.message);
                                $("#editModal").modal('hide');
                            }
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/reimbursement/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        } else if (data.code == 1002) {
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            $("#editModal").modal('hide');
                            if (getResCode(data))
                                return;
                        }
                    }, error: function (data) {
                        Ladda.stopAll();
                        $("#editModal").modal('hide');
                        if (getResCode(data))
                            return;
                    }
                });
            }
        }, function () {
            return;
        });
    }
}

//跳转到出差总结页面
function businessConclusion(t) {
    var admId = $("#administrativeId").val();
    $.ajax({
        type: "post",
        url: "/userBusinessPlan/getConlusion",
        dataType: "json",
        data: {admId: admId},
        success: function (data) {
            if (data == 0) {
                swal("友情提示!", "该流程关联的出差流程尚未填写出差总结", "warning");
                return;
            } else {
                var addScheme = "/administrative/administrative?id=" + admId + "&flag=-1&type=4&rtype=1";
                page(addScheme, "查看出差总结");
            }
        }
    });


}

function submitHander2(t, url) {
    var lock = true;
    if ($("#outAccountIds").val() == undefined || $("#outAccountIds").val() == "" || $("#outAccountIds").val() == null) {
        swal("请先选择出款账户！")
        return;
    }
    if (parseFloat($("#payAmount").val()) > parseFloat($("#totalMoney").val())) {
        swal("出款金额不能大于报销金额");
        return;
    }
    if ($("#editForm").valid()) {
        var param = {
            id: $("#id").val(),
            outAccountId: $("#outAccountIds").val(),
            payAmount: $("#payAmount").val(),
            payTime: $("#payTime").val(),
            desc: $("#desc").val()
        };
        // var param = $("#editForm").serializeJson();
        layer.confirm('请确认借款信息？提交后不能取消！', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            startModal("#reject2");
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: url,
                    data: param,
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            //出款后，审批通过
                            // approveTask($("#taskId").val(), 1, t.id);
                            // layer.msg(data.data.message, {time: 1000, icon: 6});
                            $("#editModal").modal('hide');
                            swal(data.data.message);
                            $("#deptId1").val("");
                            $("#outAccountIds").val("");
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/reimbursement/listPg", $("#queryForm").serializeJson(), "json", null, null);
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

}

//计算总金额
function calculatePrice() {
    //需要报销的金额集合
    var money = $(".money");
    //单据张数集合
    var numberOfDocument = $(".numberOfDocument");
    //当前行总价集合
    var currentTotalPrice = $(".currentTotalPrice");
    //未还借款
    var unpaidLoan = $("#unpaidLoan");
    //应报销金额
    var reimbursedMoney = $("#reimbursedMoney");
    //实际报销金额
    var totalMoney = $("#totalMoney");
    //大写
    var sumUpper = $("#sumUpper");

    //判断输入的金额是否合法
    for (var i = 0; i < money.length; i++) {
        var moneyVal = money.eq(i).val();
        if (isNaN(moneyVal)) {
            swal("输入的金额不合法");
            money.eq(i).val(0);
        }
        var numberOfDocumentVal = numberOfDocument.eq(i).val();
        if (isNaN(numberOfDocumentVal) || numberOfDocumentVal < 0 || numberOfDocumentVal.indexOf(".") != -1) {
            swal("输入的单据张数不合法");
            numberOfDocument.eq(i).val(1);
        }
    }

    //遍历每一行的数据，算出每一行的总价
    for (var i = 0; i < money.length; i++) {
        var moneyVal = money.eq(i).val();
        currentTotalPrice.eq(i).val(moneyVal);
    }

    //计算出每行的总价
    var sum = 0;
    //计算应报销的金额 (所有行总价之和)
    for (var i = 0; i < currentTotalPrice.length; i++) {
        sum = sum * 1 + currentTotalPrice.eq(i).val() * 1;
    }

    reimbursedMoney.val(sum.toFixed(2));

    //判定未还借款是否合法
    if (parseFloat(unpaidLoan.val() < 0) || parseFloat(isNaN(unpaidLoan.val()))) {
        swal("未还借款不合法");
        unpaidLoan.val(0);
    }

    // //计算实报销金额 (应报销金额-未还借款)
    // totalMoney.val((parseFloat(reimbursedMoney.val()) - parseFloat(unpaidLoan.val())).toFixed(2));
    // getZhCN();
}

function getZhCN() {
    //将数字转换成中文大写
    //将有格式的数字去除掉所有的逗号
    var toUpperNumber = $("#totalMoney").val().replace(/,/g, "");
    var fraction = ['角', '分'];
    var digit = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'];
    var unit = [['元', '万', '亿'], ['', '拾', '佰', '仟']];
    var head = toUpperNumber < 0 ? '欠' : '';
    Math.abs(toUpperNumber);
    var s = '';
    for (var i = 0; i < fraction.length; i++) {
        s += (digit[Math.floor(toUpperNumber * 10 * Math.pow(10, i)) % 10] + fraction[i]).replace(/零./, '');
    }
    s = s || '整';
    toUpperNumber = Math.floor(toUpperNumber);
    for (var i = 0; i < unit[0].length && toUpperNumber > 0; i++) {
        var p = '';
        for (var j = 0; j < unit[1].length && toUpperNumber > 0; j++) {
            p = digit[toUpperNumber % 10] + unit[1][j] + p;
            toUpperNumber = Math.floor(toUpperNumber / 10);
        }
        s = p.replace(/(零.)*零$/, '').replace(/^$/, '零') + unit[0][i] + s;
    }
    var sumToUpper = head + s.replace(/(零.)*零元/, '元').replace(/(零.)+/g, '零').replace(/^整$/, '零元整');
    $("#sumUpper").val(sumToUpper);
}

//在编辑页面，使用保存触发的方法
function edit(id) {
    view(id, -2);
}

//删除触发的方法
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
                url: baseUrl + "/reimbursement/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/reimbursement/listPg", $("#queryForm").serializeJson(), "json", null, null);
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

//申请人撤回
function returnBack(taskId, itemId) {
    var lock = true;
    layer.confirm('确认撤回？', {
        btn: ['撤回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
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
                        // layer.msg(data.data.message, {time: 2000, icon: 6});
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/reimbursement/listPg", $("#queryForm").serializeJson(), "json", null, null);
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

//flag=1审核页面，flag=-1查看页面,flag=0稿件管理跳转到查看页面
//flag=-2编辑，flag=2出纳出款，flag=3财务部长确认公账出款
//flag = 6 财务部长修改出款账户
function view(id, flag) {
    var rollback = "";
    var hideFlag = true;
    $("#rollback1").html("");
    $("#rollback2").html("");
    $("#rollback3").html("");
    orderNumber = 0;
    $("#flag").val(flag);
    $("#editModal").modal({backdrop: "static"});
    document.getElementById("editForm").reset();
    $("#editForm").find("input").removeClass('error');
    $("#editForm").validate().resetForm();
    $("#editModal input:radio").removeAttr("checked");
    $("#editModal input:radio").parent().removeClass("checked");
    $("#affixDiv").empty();
    $("#borrowInfo").empty();
    //清空详情展示区
    $("#checkTable tbody").html("");
    $("#editForm .showOnAdd").hide();
    $("#editForm .showOnEdit").show();
    $("#reimbursement").show();
    if (flag == -2) {//编辑
        $("#selBorrow").show();
        $("#cleanBorrow").show();
        $("#editFooter").show();
        $("#viewAndConfirm").hide();
        $("#viewFooter").hide();
        $("#confirmFooter").hide();
        $("#affix").show();
        $(".selectAccountBtnDiv").show();
        $("#selectBorrowBtnDiv").show();
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-2");
        $(".stateDiv").hide();
        $('#save').data('url', editUrl);
        $('#submit').data('url', editUrl);
        $(".licence").hide();
        $(".licence").find("input").each(function () {
        });
        //编辑时禁止进行再次添加或者删除报销,只允许修改
        $("#btn_addCheck").removeAttrs("disabled");
        $("#btn_removeCheck").removeAttrs("disabled");
        removeDisableStyle();
    } else {
        $("#editFooter").hide();
        $(".selectAccountBtnDiv").hide();
        $("#selectBorrowBtnDiv").hide();
        $("#affix").hide();
        $("#editForm .changeOnView").removeClass("col-sm-2");
        $("#editForm .changeOnView").addClass("col-sm-3");
        $("#btn_addCheck").attr("disabled", "disabled");
        $("#btn_removeCheck").attr("disabled", "disabled");
        $(".stateDiv").show();
        if (flag == -1) {//查看
            $("#changeAccountBtn").hide();
            $(".licence").hide();
            $(".licence").find("input").each(function () {
            });
            var returnType = getQueryString("returnType");
            addDisableStyle();
            if (returnType == 5) {
                $("#reject1").hide();
                $("#pass1").hide();
                $("#auditTrue").show();
                rollback = "rollback1";
                $("#auditFalse").hide();
                $("#confirmFooter").hide();
                $("#expense").hide();
            } else {
                $("#viewFooter").show();
                $("#confirmFooter").hide();
                $("#auditTrue").hide();
                $("#auditFalse").show();
                rollback = "rollback2";
                //查看页面返回按钮隐藏
                $("#cancel1").show();
                $("#goback").hide();
                $("#review").hide();
            }
        } else if (flag == 0) {//跳转查看
            $("#viewFooter").show();
            $("#confirmFooter").hide();
            $("#auditTrue").hide();
            $("#auditFalse").show();
            rollback = "rollback2";
            $("#cancel1").hide();
            $("#review").hide();
            $("#goback").show();
            addDisableStyle();
        } else if (flag == 1) {//审核
            $("#viewFooter").show();
            $("#confirmFooter").hide();
            $("#auditTrue").show();
            rollback = "rollback1";
            $("#auditFalse").hide();
            $("#expense").data("id", id);
            removeDisableStyle();
            hideFlag = false;
        } else if (flag == 2) {//出纳出款
            $("#viewFooter").hide();
            $("#confirmFooter").show();
            rollback = "rollback3";
            removeDisableStyle();
            hideFlag = false;
        } else if (flag == 3) {//确认
            $("#viewFooter").show();
            $("#confirmFooter").hide();
            $("#auditTrue").hide();
            $("#auditFalse").show();
            rollback = "rollback2";
            $("#reimbursement").hide();
            $("#changeAccountBtn").hide();
            //查看页面返回按钮隐藏
            $("#cancel1").show();
            $("#goback").hide();
            $("#review").show();
            removeDisableStyle();
            hideFlag = false;
        } else if(flag == 6){
            $("#auditTrue").hide();
            $("#changeAccountBtn").show();
            $("#confirmFooter").hide();
            $("#review").hide();
            $("#goback").hide();
            removeDisableStyle();
        }
        else {
            $("#viewFooter").show();
            $("#confirmFooter").hide();
            $("#auditTrue").hide();
            $("#auditFalse").show();
            rollback = "rollback2";
            //查看页面返回按钮隐藏
            $("#cancel1").show();
            $("#goback").hide();
            $("#review").hide();
            removeDisableStyle();
        }
    }

    $.ajax({
        type: "post",
        url: baseUrl + "/reimbursement/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                //报销详情  需要循环遍历所有值
                if (data.data.entity.reimbursementDs != null) {
                    for (var i = 0; i < data.data.entity.reimbursementDs.length; i++) {
                        var costType1 = data.data.entity.reimbursementDs[i].costType;
                        var purpose1 = data.data.entity.reimbursementDs[i].purpose;
                        var money1 = data.data.entity.reimbursementDs[i].money;
                        var numberOfDocument1 = data.data.entity.reimbursementDs[i].numberOfDocument;
                        if (flag == -2) {
                            addCheck(costType1, purpose1, money1, numberOfDocument1);
                        } else {
                            addCheck(costType1, purpose1, money1, numberOfDocument1, null, 1);
                            if(flag==-1 || flag==0){
                                addDisableStyle();
                            }
                        }
                    }
                }

                $("#admId").val(data.data.entity.administrativeId);
                if (flag == -2) {
                    $("#editModal .modal-title").html("编辑费用报销");
                } else {
                    $("#editModal .modal-title").html(data.data.entity['title']);
                }
                for (var attr in data.data.entity) {
                    $("#editForm [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                    if (flag == -2) {
                        $("#editForm input[name=" + attr + "]").removeAttrs("style");
                        if (attr == "applyName" ||
                            attr == "deptName" ||
                            attr == "applyTime" ||
                            attr == "reimbursedMoney" ||
                            attr == "unpaidLoan" ||
                            attr == "sumUpper" ||
                            attr == "state1") {
                            $("#editForm input[name=" + attr + "]").attr("readonly", "readonly");
                        }
                    } else {
                        $("#editForm input[name=" + attr + "]").removeAttrs("readonly");
                        $("#editForm input[name=" + attr + "]").attr("style", "border:0;background-color:white;");
                        if (attr == "payTime") {
                            dateData = data.data.entity[attr];
                            // 判断是否为空；
                            if (dateData != null) {
                                // 解析为日期默认的格式为斜杠，需要进行处理；
                                data.data.entity[attr] = new Date(dateData.replace(/-/g, "/")).format("yyyy-MM-dd hh:mm");
                            }
                        }
                    }
                    // $("[name=" + attr + "]").text(data.data.entity[attr]);
                    if (attr == "type") {
                        $("input[name='type'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                        $("input[name='type'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                    }
                    if (attr == "administrativeId") {
                        if (data.data.entity[attr] == 0) {
                            $("#reimbursement").hide();
                            $("#reimbursement1").hide();
                        }

                    }
                    if (attr == "affixName") {
                        $("#affixDiv").empty();
                        $("#affixDiv").show();
                        if (data.data.entity[attr] === "") continue;
                        var affixName = data.data.entity[attr].split(',');
                        var affixLink = data.data.entity["affixLink"].split(",");
                        if (affixName.length > 0 && affixLink.length > 0) {
                            var html = "";
                            for (var i = 0; i < affixName.length; i++) {
                                var filePath = affixLink[i];
                                var fileName = affixName[i];
                                if (fileName === "" || filePath === "") continue;
                                html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                                html += "<a href=" + filePath + " target=_blank download='" + fileName + "'>下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                                var strFilter = ".jpeg|.gif|.jpg|.png|.bmp|.pic|";
                                var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                                if (fileName.indexOf(".") > -1) {
                                    var str = fileExt + '|';
                                    if (strFilter.indexOf(str) > -1) {//是图片
                                        // html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',1)\" data-id='" + filePath + "'>预览:</a><br/>";
                                        html += "<img alt='" + fileName + "' src='" + filePath + "' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                    } else {
                                        if(fileExtArray.contains(fileExt)){
                                            html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                        }
                                    }
                                } else {
                                    html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                }
                            }
                            $("#affixDiv").append(html);
                        }
                    }
                    //覆盖上面type!=radio的数据
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
                            case 12 :
                                stateStr = "财务部长审核";
                                break;
                            case 10 :
                                stateStr = "业务员确认";
                                break;
                            case 9 :
                                stateStr = "会计确认出款";
                                break;
                            case 16 :
                                stateStr = "出纳审核";
                                break;
                            case 30 :
                                stateStr = "集团财务负责人";
                                break;
                            case 31 :
                                stateStr = "集团财务分管领导";
                                break;
                            case 32 :
                                stateStr = "集团总裁";
                                break;
                            default :
                                break;
                        }
                        if (data.data.entity["state"] == 9) {
                            $(".licence").hide();
                            $(".licence").find("input").each(function () {
                            });
                        }
                        if (dataStr != null) {
                            $("#editForm [name='state1']").val(stateStr);
                            $("#editForm [name='state1']").prop("style", "border:0;background-color:white");
                        }
                    }
                }
                calculatePrice();

                if (flag == 2 && hasRoleCWCN() && (data.data.entity['state'] == 2 || data.data.entity['state'] == 16) ||(data.data.entity['state'] == 1&&flag==6&&hasRoleCWBZ())) {
                    $("#viewAndConfirm").show();
                    $(".showOnView").hide();
                    $(".showOnConfirm").show();
                    $("#editForm #outAccountName").removeAttrs("style");
                    $("#editForm #payTime").removeAttrs("style");
                    $("#editForm #payTime").attr("readonly", "readonly");
                    $("#editForm #payAmount").removeAttrs("style");
                    $("#payAmount").val(parseFloat($("#totalMoney").val())-parseFloat($("#unpaidLoan").val()));
                    $("#payAmount").attr("readonly", "readonly");
                    if(data.data.entity['outAccountId']){
                        $("#editForm #outAccountSelect").val(data.data.entity['outAccountId']);
                        $("#editForm #outAccountIds").val(data.data.entity['outAccountId']);
                        layui.use(["form"], function () {
                            layui.form.render('select');
                        });
                    }
                } else if (data.data.entity['state'] == 1 || data.data.entity['state'] == 12 || data.data.entity['state'] == 9) {//已出款，直接显示出款文本
                    $("#viewAndConfirm").show();
                    $(".showOnView").show();
                    $(".showOnConfirm").hide();
                } else {//未出款，不显示出款账户信息
                    $("#viewAndConfirm").hide();
                }

                if (data.data.list != null && data.data.list.length > 0) {
                    html = '<div><h3>借款冲抵详情</h3></div><table class="table table-bordered" style="text-align: center"><thead>' +
                        '<th style="text-align:center;vertical-align:middle;">借款编号</th>' +
                        '<th style="text-align:center;vertical-align:middle;">借款标题</th>' +
                        '<th style="text-align:center;vertical-align:middle;">借款类型</th>' +
                        '<th style="text-align:center;vertical-align:middle;">借款人</th>' +
                        '<th style="text-align:center;vertical-align:middle;">所属部门</th>' +
                        '<th style="text-align:center;vertical-align:middle;">借款金额</th>' +
                        '<th style="text-align:center;vertical-align:middle;">已还金额</th>' +
                        '<th style="text-align:center;vertical-align:middle;">未还金额</th>' +
                        '<th style="text-align:center;vertical-align:middle;">冲抵金额</th>' +
                        '</thead>';
                    for (var i = 0; i < data.data.list.length; i++) {
                        var typeStr = data.data.list[i]['type'] == 0 ? "备用金" : "其它";
                        html += '<tr><td>' + data.data.list[i]['code'] + '</td>' +
                            '<td>' + data.data.list[i]['title'] + '</td>' +
                            '<td>' + typeStr + '</td>' +
                            '<td>' + data.data.list[i]['applyName'] + '</td>' +
                            '<td>' + data.data.list[i]['deptName'] + '</td>' +
                            '<td>' + data.data.list[i]['applyAmount'] + '</td>' +
                            '<td>' + data.data.list[i]['repayAmount'] + '</td>' +
                            '<td>' + data.data.list[i]['remainAmount'] + '</td>' +
                            '<td>' + data.data.list[i]['amount'] + '</td></tr>';
                    }
                    html += '</tbody></table>';
                    $("#borrowInfo").append(html);
                }

                //是否审核完成
                if((data.state != 1 && data.state != -1 && data.state != -9) && rollback){
                    new RollbackCompont({
                        target: "#"+rollback,
                        modal: "#editModal",
                        taskId: data.data.entity.taskId,
                        btnName:"撤回选择节点",
                        title:"撤回成功",
                        hideFlag: hideFlag,
                        completeCallback: function () {
                        }
                    }).render();
                }

            } else if (data.code == 1002) {
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
}

// //文件预览方法：flag：1图片格式，0文件格式
// function previewFile(fileName,filePath,flag) {
//     if(flag==0){
//         var ext=fileName.substring(fileName.lastIndexOf(".")+1);
//         if(ext.toLowerCase()=="pdf" || ext.toLowerCase()=="html"){
//             $("#fileModal").modal("toggle");
//             var html="<iframe width='100%' height='600px' style='border: 0px;' src='"+filePath+"'></iframe>";
//             $("#fileDiv").html(html);
//             document.getElementById("fileDiv").style.overflowY="unset";
//         }else{
//             var index = layer.load(4,{time:10000});
//             $.ajax({
//                 type: "post",
//                 url: baseUrl +"/reimbursement/previewFile",
//                 data: {fileName:fileName,filePath:filePath},
//                 dataType: "json",
//                 success:function (data) {
//                     layer.close(index);
//                     if(data.code==200){
//                         $("#fileModal").modal("toggle");
//                         if(data.data.ext=="txt"){
//                             $("#fileDiv").html(data.data.stream);
//                             document.getElementById("fileDiv").style.overflowY="scroll";
//                         }else {
//                             var html="<iframe width='100%' height='600px' style='border: 0px;' src='"+data.data.stream+"'></iframe>";
//                             $("#fileDiv").html(html);
//                             document.getElementById("fileDiv").style.overflowY="unset";
//                         }
//                     }else if(data.code==1002){
//                         swal({
//                             title:"提示",
//                             text:data.msg
//                         });
//                     }
//                 },
//                 error:function (data) {
//                     layer.close(index);
//                 }
//             });
//         }
//     }else{
//         $("#fileModal").modal("toggle");
//         var html="<img alt='"+fileName+"' style='max-width: 100%;' src='"+filePath+"'></img>";
//         $("#fileDiv").html(html);
//         document.getElementById("fileDiv").style.overflowY="scroll";
//     }
// }
//查看添加disabled属性
function addDisableStyle() {
    $("#editForm [name='title']").attr("disabled",true);
    $("#editForm [name='accountName']").attr("disabled",true);
    $("#editForm [name='accountBankNo']").attr("disabled",true);
    $("#editForm [name='accountBankName']").attr("disabled",true);
    $("#editForm .costType").attr("disabled",true);
    $("#editForm .purpose").attr("disabled",true);
    $("#editForm .money").attr("disabled",true);
    $("#editForm .numberOfDocument").attr("disabled",true);
    $("#editForm .costType").prop("style","border:0px;background-color:white");
    $("#editForm .purpose").prop("style","border:0px;background-color:white");
    $("#editForm .money").prop("style","border:0px;background-color:white");
    $("#editForm .numberOfDocument").prop("style","border:0px;background-color:white");
    $("#editForm [name='remark']").attr("disabled",true);
    $("#editForm [name='remark']").prop("style","background-color:white");
    $("#editForm [name='reimbursedMoney']").attr("disabled",true);
    $("#editForm [name='totalMoney']").attr("disabled",true);
    $("#editForm [name='unpaidLoan']").attr("disabled",true);
    $("#editForm [name='sumUpper']").attr("disabled",true);
    $("#editForm [name='deptName']").attr("disabled",true);
    $("#editForm [name='applyName']").attr("disabled",true);
    $("#editForm [name='code']").attr("disabled",true);
    $("#editForm [name='applyTime']").attr("disabled",true);
    $("#editForm [name='affix']").attr("disabled",true);
    $("#editForm [name='state1']").attr("disabled",true);
}
//移除disabled属性
function removeDisableStyle() {
    $("#editForm [name='title']").prop("style","");
    $("#editForm [name='title']").attr("disabled",false);
    $("#editForm [name='accountName']").prop("style","");
    $("#editForm [name='accountName']").attr("disabled",false);
    $("#editForm [name='accountBankNo']").prop("style","");
    $("#editForm [name='accountBankNo']").attr("disabled",false);
    $("#editForm [name='accountBankName']").prop("style","");
    $("#editForm [name='accountBankName']").attr("disabled",false);
    $("#editForm .costType").prop("style","");
    $("#editForm .costType").attr("disabled",false);
    $("#editForm .purpose").prop("style","");
    $("#editForm .purpose").attr("disabled",false);
    $("#editForm .money").prop("style","");
    $("#editForm .money").attr("disabled",false);
    $("#editForm .numberOfDocument").prop("style","");
    $("#editForm .numberOfDocument").attr("disabled",false);
    $("#editForm [name='remark']").prop("style","");
    $("#editForm [name='remark']").attr("disabled",false);
    $("#editForm [name='reimbursedMoney']").prop("style","");
    $("#editForm [name='reimbursedMoney']").attr("disabled",false);
    $("#editForm [name='totalMoney']").prop("style","");
    $("#editForm [name='totalMoney']").attr("disabled",false);
    $("#editForm [name='unpaidLoan']").prop("style","");
    $("#editForm [name='unpaidLoan']").attr("disabled",false);
    $("#editForm [name='sumUpper']").prop("style","");
    $("#editForm [name='sumUpper']").attr("disabled",false);
    $("#editForm [name='deptName']").prop("style","");
    $("#editForm [name='deptName']").attr("disabled",false);
    $("#editForm [name='applyName']").prop("style","");
    $("#editForm [name='applyName']").attr("disabled",false);
    $("#editForm [name='code']").prop("style","");
    $("#editForm [name='code']").attr("disabled",false);
    $("#editForm [name='applyTime']").prop("style","");
    $("#editForm [name='applyTime']").attr("disabled",false);
    $("#editForm [name='affix']").prop("style","");
    $("#editForm [name='affix']").attr("disabled",false);
    $("#editForm [name='state1']").prop("style","");
    $("#editForm [name='state1']").attr("disabled",false);
}

/*//通过出差id获取出差流程审批状态
function getStateByadmId(admId) {
    $.ajax({
        type: "post",
        url: baseUrl + "/administrative/getById",
        data: {id: admId},
        dataType: "json",
        success: function (data) {
            console.log(data);
            console.log(data.data.entity.approveState);
            console.log(data.entity.approveState);
            return state = data.data.entity.approveState;
        }
    });
}*/

//审核通过
function approve(t) {
    var admId = $("#administrativeId").val();
    var state;
    if (hasRoleCWBZ() && admId != 0) {
        $.ajax({
            type: "post",
            url: baseUrl + "/administrative/getById",
            data: {id: admId},
            dataType: "json",
            async: false,
            success: function (data) {
                state = data.data.entity.approveState;
            }
        });
        if (state != 2) {
            swal("友情提示!", "该流程关联的出差流程未审批完成，请稍后审批！", "warning");
            return;
        }
    }
    approveTask($("#taskId").val(), 1, t.id, $("#desc").val())
}

//审核驳回
function reject(t) {
    approveTask($("#taskId").val(), 0, t.id, $("#desc").val(),function () {
        $("#editModal").modal("hide");
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").reloadCurrentData(baseUrl + "/reimbursement/listPg", $("#queryForm").serializeJson(), "json", null, null);
        return 1;
    });
}

//state=2用这个撤回，不还原稿件和借款的状态
function CWReject(t) {
    var lock = true ;
    var id = $("#editForm #id").val();
    layer.confirm('确认撤回？', {
        btn: ['撤回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#confirm");//锁定按钮，防止重复提交
        startModal("#reject2");//锁定按钮，防止重复提交
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/reimbursement/CWReject",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        $("#editModal").modal("hide");
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/reimbursement/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message);
                        approveTask($("#taskId").val(), 0, t.id,$("#desc1").val())
                    } else if(data.code == 1002){
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

//审核记录查看
function showHistory(id) {
    //process详见IProcess
    $("#historyModal").modal('toggle');
    $.ajax({
        type: "post",
        url: baseUrl + "/process/history",
        data: {dataId: id, process: 12},
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
                    html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=12&t=" + new Date().getTime() + "' style='width: 121%; margin-left: -45px; margin-top: 25px;margin-bottom: -100px;'/></div>";
                    $("#history").append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}

//财务负责人确认出款
function checkBtoB() {
    var lock = true;
    layer.confirm('确认出款？', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#review");//锁定按钮，防止重复提交
        if (lock) {
            lock = false;
            $.ajax({
                type: "post",
                url: baseUrl + "/reimbursement/checkBtoB",    //向后端请求数据的url
                data: {id: $("#id").val()},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    $("#editModal").modal("hide");
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1500, icon: 6});
                        $("#editModal").modal("hide");
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/reimbursement/listPg", $("#queryForm").serializeJson(), "json", null, null);
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

//清空新增报销隐藏域的值
function clearAddFormHiddenVal(){
    $("#administrativeId").val("");
    $("#id").val("");
    $("#accountId").val("");
    $("#state").val("0");
    $("#outAccountId").val("");
    $("#outAccountIds").val("");
    $("#deptId").val("");
    $("#applyId").val("");
    $("#taskId").val("");
    $("#borrowId").val("");
}

$(document).ready(function () {
    //加载打印权限
    downLoadAndPrintObj.loadFlowPrintPermission();

    getDept();
    $.jgrid.defaults.styleUI = 'Bootstrap';
    var e = "<i class='fa fa-times-circle'></i> ";
    $("#editForm").validate({
        rules: {
            title: {maxlength: 80},
            payAmount: {number: true,},
            costType: {required: true},
            purpose: {required: true},
            remark: {maxlength: 2000},
        },
        messages: {
            title: {maxlength: e + "标题长度必须小于{0}个字符"},
            payAmount: {number: e + "实报销金额必须为数字"},
            costType: {required: e + "必填字段"},
            purpose: {required: e + "必填字段"},
            remark: {maxlength: e + "备注长度必须小于{0}个字符"},
        }
    });
    layui.use('form', function () {
        var form = layui.form;
        $.ajax({
            type: "post",
            url: "/account/queryCompanyAccountList",
            data: {companyCode: user.dept.companyCode},
            dataType: "json",
            success: function (data) {
                var html = "<option value=''></option>";
                for (var i = 0; i < data.length; i++) {
                    html += "<option value='" + data[i].id + "'>" + data[i].name + "</option>";
                }
                $("#editForm select[name='outAccountIds']").append(html);
                form.render('select');
                form.on('select', function (data) {
                    $("#outAccountIds").val(data.value);
                });
            }
        })
    });
    $("#remId").val(user.id);
    reimbursementSum();

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/reimbursement/listPg',
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
        rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'code', label: '报销编号', editable: true, width: 100},
            {name: 'title', label: '报销标题', editable: true, width: 100},
            {name: 'applyName', label: '报销人', editable: true, width: 60},
            {name: 'deptName', label: '报销部门', editable: true, width: 80},
            {
                name: 'applyTime', label: '报销时间', editable: false, width: 100,
                align: "center", sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd hh:mm");
                }
            },
            {name: 'accountName', label: '收款单位(人)', editable: true, width: 80},
            {name: 'accountBankName', label: '收款开户行', editable: true, width: 80},
            {name: 'remark', label: '摘要', editable: true, width: 120},
            {
                name: 'reimbursedMoney', label: '应报销金额', editable: true, width: 80,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            },
            {
                name: 'totalMoney', label: '实报销金额', editable: true, width: 80,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            },
            {
                name: 'unpaidLoan', label: '冲抵借款金额', editable: true, width: 80,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            },
            {name: 'outAccountName', label: '出账账户名称', editable: true, width: 80},
            {
                name: 'payAmount', label: '实际出款金额', editable: true, width: 80,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            },
            {
                name: 'payTime', label: '出款时间', editable: true, width: 80,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'state', label: '报销状态', editable: true, width: 80,
                formatter: function (value, grid, rows) {
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
                        case 30 :
                            return "<span style='color:red'>集团财务负责人</span>";
                        case 31 :
                            return "<span style='color:red'>集团财务分管领导</span>";
                        case 32 :
                            return "<span style='color:red'>集团总裁</span>";
                    }
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 180,
                formatter: function (value, grid, rows) {
                    var html = "";

                    //如果有打印下载权限，则展示
                    if(downLoadAndPrintObj.dowloadAndPritPermission){
                        html += "<a href='javascript:;' onclick='downLoadAndPrintObj.viewModalShow("+rows.id+");'>预览&nbsp;</a>";
                    }

                    // if (rows.isOwner && rows.applyId==user.id ) {//审批通过的链接有问题，先注释
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='view(" + rows.id + ",1)'>审批&nbsp;&nbsp;</a>";
                    // }
                    if (rows.taskId != null && rows.taskId != '') {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='showHistory(" + rows.id + ")'>审批详情&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='edit(" + rows.id + ")'>编辑&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red;'  onclick='del(" + rows.id + ")'>删除&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWCN() && (rows.state == 2 || rows.state == 16) &&  rows.acceptWorker==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",2)'>出纳出款&nbsp;&nbsp;</a>";
                    }
                    //审核中才能撤回
                    if ((rows.state == 3 || rows.state == 4
                        || rows.state == 5 || rows.state == 6
                        || rows.state == 7 || rows.state == 8
                        || rows.state == 10 || rows.state == 12 || rows.state == 32 || rows.state == 31) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='returnBack(" + "\"" + rows.taskId + "\"," + rows.itemId + ")'>撤回&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWKJ() && rows.state == 9 &&  rows.acceptWorker==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",3)'>确认公账出款&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWBZ() && rows.state == 1) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",6)'>更改出款信息&nbsp;&nbsp;</a>";
                    }
                    return html;
                }
            },
            {name: 'accountBankNo', label: '支付账号', editable: true, hidden: true, width: 60},
            {name: 'sumUpper', label: '实报销金额大写', editable: true, hidden: true, width: 80},
            {name: 'reimbursedMoney', label: '应报销金额', editable: true, hidden: true, width: 80},
            {name: 'taskId', label: 'taskId', editable: true, hidden: true, width: 80},
            {name: 'itemId', label: '待办事项', editable: true, hidden: true, width: 80},
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
            view(rowid, -1);
        }
    });

    $("#queryForm select[name='stateQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        reimbursementSum();
    });
    if (hasRoleCWBZ()) {
        $("#exportBtn").show();
    } else {
        $("#exportBtn").hide();
    }
    //导出报销
    $("#exportBtn").click(function () {
        var params =  removeBlank($("#queryForm").serializeJson());
        location.href = "/reimbursement/export?" + $.param(params);
    });


    //flag=1审核，否则查看
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        view(getQueryString("id"), getQueryString("flag"));
    }
    if (getQueryString("approveId") != null && getQueryString("approveId") != "" && getQueryString("approveId") != undefined) {
        view(getQueryString("approveId"), 2);
    }

    if (getQueryString("repayId") != null && getQueryString("repayId") != "" && getQueryString("repayId") != undefined) {
        repayConfirm(getQueryString("repayId"));
    }

    if (getQueryString("editId") != null && getQueryString("editId") != "" && getQueryString("editId") != undefined) {
        edit(getQueryString("editId"));
    }

    if (getQueryString("admId") != null && getQueryString("admId") != "" && getQueryString("admId") != undefined) {
        getReimbursementByAdmId(getQueryString("admId"));
    }
     //采购关联报销
    if (getQueryString("purchaseFlag") != null && getQueryString("purchaseFlag") != "" && getQueryString("purchaseFlag") != undefined) {
        orderNumber = 0;
        removeDisableStyle();
        $("#flag").val(-3);//flag=-2和-3允许增加删除详情
        $("#editModal").modal({backdrop: "static"});
        document.getElementById("editForm").reset();//reset无法将hidden的input值清空，导致查看费用报销后，接着发起报销出现被查看的报销流程被删除问题
        clearAddFormHiddenVal();//清空隐藏域的值
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();
        $("#editModal input:radio").removeAttr("checked");
        $("#editModal input:radio").parent().removeClass("checked");
        $("#affixDiv").empty();
        $(".selectAccountBtnDiv").show();
        $("#selectBorrowBtnDiv").show();
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-2");
        $("#borrowInfo").empty();
        $("#editFooter").show();
        $("#viewFooter").hide();
        $("#confirmFooter").hide();
        $("#viewAndConfirm").hide();
        $(".stateDiv").hide();
        $(".licence").hide();
        $(".licence").find("input").each(function () {
        });
        //清空详情展示区
        $("#checkTable tbody").html("");
        $("#btn_addCheck").removeAttrs("disabled");
        $("#btn_removeCheck").removeAttrs("disabled");
        $("#editModal .modal-title").html("新增费用报销");
        $("#editForm .showOnEdit").hide();
        $("#editForm .showOnAdd").show();
        $("#affix").show();
        $('#save').data('url', addUrl);
        $('#submit').data('url', editUrl);
        //清空数据
        $("#administrativeId").val(0);
        $("#reimbursedMoney").val(0);
        $("#totalMoney").val("");
        $("#sumUpper").val("零元整");
        $("#editForm input").each(function (i, val) {
            $("#editForm [name='" + val.name + "']").removeAttrs("style");
        })
        $("#purchaseIds").val(getQueryString("ids"));
        requestData({ids:getQueryString("ids")},"/purchase/relatedReimbursement","post",function (data) {
            if(data && data.data.list!=null){
                $(data.data.list).each(function (i,item) {
                    var costType="采购报销";
                    var purpose=item.title;
                    var money=item.money;
                    var numberOfDocument=1;
                    addCheck(costType, purpose, money, numberOfDocument);
                    $("button[name='btn_removeCheck']").attr("disabled","disabled");
                    $("button[name='btn_addCheck']").attr("disabled","disabled");
                });
            }
            calculatePrice();
        });
    }
    $("#totalMoney").blur(function () {
        var reimbursedMoney = $("#reimbursedMoney").val();
        var totalMoney = $("#totalMoney").val();
        if (parseFloat(reimbursedMoney) < parseFloat(totalMoney)) {
            swal("实报销金额不能大于应报销金额");
            return;
        }
        getZhCN();
    });

    // =========================================弹框选择账户信息开始======================================
    $("#account_table_logs").jqGrid({
        url: baseUrl + '/account/queryIndividualAccount',
        datatype: "local",
        mtype: 'POST',
        postData: $("#accountForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        autowidth: true,//自动匹配宽度
        //setLabel: "序号",
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        multiboxonly: true,
        beforeSelectRow: function () {
            $("#account_table_logs").jqGrid('resetSelection');
            return (true);
        },
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'companyName', label: '公司名称', editable: true, width: 240},
            {name: 'name', label: '账户名称', editable: true, width: 240},
            {name: 'bankNo', label: '账号', editable: true, width: 240},
            {name: 'bankName', label: '开户行', editable: true, width: 240},
            {name: 'owner', label: '联系人', editable: true, width: 180},
        ],
        pager: jQuery("#account_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });
    resize("#account_pager_logs")

    $("#accountSearch").click(function () {
        $("#account_table_logs").emptyGridParam();
        $("#account_table_logs").jqGrid('setGridParam', {
            postData: $("#accountForm").serializeJson(), //发送数据
            datatype: "json",
        }).trigger("reloadGrid"); //重新载入
    });
    //隐藏全选框
    document.getElementById("cb_account_table_logs").style.display = "none";

    //若用户选择抵消借款，将从借款记录中选择一条借款
    $("#addAccount").click(function () {
        var accountName = $("#accountName").val();
        if (accountName == undefined || accountName == "" || accountName == "") {
            swal("请先输入收款账户！")
            return ;
        }
        var bankNo = $("#accountBankNo").val();
        if (bankNo == undefined || bankNo == "" || bankNo == "") {
            swal("请先输入账号！")
            return ;
        }
        var bankName = $("#accountBankName").val();
        if (bankName == undefined || bankName == "" || bankName == "") {
            swal("请先输入开户行！")
            return ;
        }

        var lock = true;
        layer.confirm('保存账户到个人账户库？', {
            btn: ['保存', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/account/addPersonalAccount",    //向后端请求数据的url
                    data: {name: accountName, bankNo: bankNo, bankName: bankName},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            swal("收款账户存入账户库成功！", "", "success");
                        } else if(data.code==1002) {
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        }else {
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

    $("#selAccount").click(function () {
        $("#editModal").modal("hide");
        $("#accountModal").modal({backdrop: "static"});
        $("#account_table_logs").emptyGridParam();
        $("#account_table_logs").jqGrid('setGridParam', {
            postData: $("#accountForm").serializeJson(), //发送数据
            datatype: "json",
        }).trigger("reloadGrid"); //重新载入
    });
    $(".cleanAccount").click(function () {
        $("#accountId").val("");
        $("#accountName").val("");
        $("#accountBankNo").val("");
        $("#accountBankName").val("");
    });

    $("#selectAccount").click(function () {
        var rowid = $("#account_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = jQuery("#account_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        $("#accountId").val(rowid);
        $("#accountName").val(rowData.name);
        $("#accountBankNo").val(rowData.bankNo);
        $("#accountBankName").val(rowData.bankName);
        $("#editModal").modal({backdrop: "static"});
        $("#accountModal").modal('hide');
        document.getElementById("accountForm").reset();
    });

// =========================================弹框选择账户信息结束======================================

    //通过admId获取报销，如果有则打开编辑页面，没有则打开新增页面
    function getReimbursementByAdmId(admId) {
        var da;
        $.ajax({
            type: "get",
            url: "/reimbursement/getByAdmId?admId=" + admId,
            dataType: "json",
            async: false,
            success: function (data) {
                da = data.data.entity;
            }
        });
        if (da) {
            if ((da.state == 0 || da.state == -1) && da.applyId == user.id) {
                edit(da.id);
            } else {
                view(da.id, -1);
            }
        } else {
            add(admId);
        }
    }

    function reimbursementSum() {
        $.ajax({
            type: "post",
            data: $("#queryForm").serializeJson(),
            url: baseUrl + "/reimbursement/reimburseSum",
            dataType: "json",
            async: false,
            success: function (resData) {
                if (resData) {
                    var totalMoney = resData.totalMoney == undefined ? 0 : resData.totalMoney;
                    var payAmount = resData.payAmount == undefined ? 0 : resData.payAmount;
                    $("#totalMoney1").text(fmtMoneyBringUnit(totalMoney) || 0);
                    $("#payAmount1").text(fmtMoneyBringUnit(payAmount) || 0);
                } else {
                    $("#tj").find(".text-danger").htmleditForm(0);
                }

            }
        })

    }


    function add(admId) {
        $("#administrativeId").val(admId);
        // console.log($("#administrativeId").val());
        orderNumber = 0;
        $("#flag").val(-3);//flag=-2和-3允许增加删除详情
        $("#editModal").modal({backdrop: "static"});
        document.getElementById("editForm").reset();
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();
        $("#editModal input:radio").removeAttr("checked");
        $("#editModal input:radio").parent().removeClass("checked");
        $("#affixDiv").empty();
        $(".selectAccountBtnDiv").show();
        $("#selectBorrowBtnDiv").show();
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-2");
        $("#editFooter").show();
        $("#viewFooter").hide();
        $("#confirmFooter").hide();
        $("#viewAndConfirm").hide();
        $(".stateDiv").hide();
        $("#borrowInfo").empty();
        $(".licence").hide();
        $(".licence").find("input").each(function () {
        });
        //清空详情展示区
        $("#checkTable tbody").html("");
        //添加一个空的<input/>框
        addCheck();
        $("#btn_addCheck").removeAttrs("disabled");
        $("#btn_removeCheck").removeAttrs("disabled");
        $("#editModal .modal-title").html("新增费用报销");
        $("#editForm .showOnEdit").hide();
        $("#editForm .showOnAdd").show();
        $("#affix").show();
        $('#save').data('url', addUrl);
        $('#submit').data('url', editUrl);
        //清空数据
        $("#reimbursedMoney").val(0);
        $("#totalMoney").val("");
        $("#sumUpper").val("零元整");
        $("#editForm input").each(function (i, val) {
            $("#editForm [name='" + val.name + "']").removeAttrs("style");
        })
    }

    //新增一个报销
    $("#addBtn").click(function () {
        orderNumber = 0;
        removeDisableStyle();
        $("#flag").val(-3);//flag=-2和-3允许增加删除详情
        $("#editModal").modal({backdrop: "static"});
        document.getElementById("editForm").reset();//reset无法将hidden的input值清空，导致查看费用报销后，接着发起报销出现被查看的报销流程被删除问题
        clearAddFormHiddenVal();//清空隐藏域的值
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();
        $("#editModal input:radio").removeAttr("checked");
        $("#editModal input:radio").parent().removeClass("checked");
        $("#affixDiv").empty();
        $(".selectAccountBtnDiv").show();
        $("#selectBorrowBtnDiv").show();
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-2");
        $("#borrowInfo").empty();
        $("#editFooter").show();
        $("#viewFooter").hide();
        $("#confirmFooter").hide();
        $("#viewAndConfirm").hide();
        $(".stateDiv").hide();
        $(".licence").hide();
        $(".licence").find("input").each(function () {
        });
        //清空详情展示区
        $("#checkTable tbody").html("");
        //添加一个空的<input/>框
        addCheck();
        $("#btn_addCheck").removeAttrs("disabled");
        $("#btn_removeCheck").removeAttrs("disabled");

        $("#editModal .modal-title").html("新增费用报销");
        $("#editForm .showOnEdit").hide();
        $("#editForm .showOnAdd").show();
        $("#affix").show();
        $('#save').data('url', addUrl);
        $('#submit').data('url', editUrl);
        //清空数据
        $("#administrativeId").val(0);
        $("#reimbursedMoney").val(0);
        $("#totalMoney").val("");
        $("#sumUpper").val("零元整");
        // $("[name=" + attr + "]").val(data.data.entity[attr]);
        // $("[name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
        // if (flag == -2) {
        //     $("#editForm [name=" + attr + "]").removeAttrs("style");
        //     if (attr == "applyName" ||
        //         attr == "deptName" ||
        //         attr == "applyTime" ||
        //         attr == "reimbursedMoney" ||
        //         attr == "unpaidLoan" ||
        //         attr == "sumUpper" ||
        //         attr == "state1") {
        //         $("#editForm [name=" + attr + "]").attr("readonly", "readonly");
        //     }
        // }
        $("#editForm input").each(function (i, val) {
            // console.log(i + "\t" + val.name + "\n") ;
            $("#editForm [name='" + val.name + "']").removeAttrs("style");
        })
    });

    //遍历数据页
    $("#borrow_table_logs").jqGrid({
        url: baseUrl + '/borrow/listPgForReimbursement',
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
        // multiboxonly: true,
        // beforeSelectRow: function () {
        //     $("#borrow_table_logs").jqGrid('resetSelection');
        //     return (true);
        // },
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
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
            // var rowData = $(this).jqGrid('getRowData');
            // //遍历所有的行，如果是选中的，说明它在数组中，让他选中
            // for (var i = 0, n = rowData.length; i < n; i++) {
            //     var item = rowData[i];
            //     //判断是否存在数据
            //     if (borrowList.length > 0) {
            //         if (borrowList.indexOf(item.id) > -1) {
            //             //判断arrayNewList中存在item.code值时，选中前面的复选框，
            //             $("#jqg_borrow_table_logs_" + item.id).attr("checked", true);
            //         }
            //     }
            // }
        },
        loadComplete: function (xhr) {
            // var array = xhr.list;
            // if (borrowList.length > 0) {
            //     $.each(array, function (i, item) {
            //         if (borrowList.indexOf(item.id.toString()) > -1) {
            //             //判断borrowList中存在item.code值时，选中前面的复选框，
            //             $("#jqg_borrow_table_logs_" + item.id).attr("checked", true);
            //         }
            //     });
            // }
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
    //根据条件进行模糊查询
    $("#borrowSearch").click(function () {
        $("#borrow_table_logs").emptyGridParam();
        $("#borrow_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#borrowForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
    resize("#borrow_table_logs");
    //隐藏全选框
    // document.getElementById("cb_borrow_table_logs").style.display = "none";
    function saveData1(obj) {
        // borrowList.length=0;
        // $("#borrowDetail").empty();
        borrowList.push(obj);
        var rowData = $("#borrow_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
        html = '<tr id="rows' + rowData.id + '"><td>' + rowData.code + '</td><td>' + rowData.title + '</td>' +
            '<td>' + rowData.type + '</td><td>' + rowData.applyName + '</td>' +
            '<td>' + rowData.deptName + '</td><td>' + rowData.applyAmount + '</td>' +
            '<td>' + rowData.payAmount + '</td><td>' + rowData.payTime + '</td>' +
            '<td><input type="hidden" name="pay_' + rowData.id + '" id="pay_' + rowData.id + '" value="' + rowData.repayAmount + '">' + rowData.repayAmount + '</td>' +
            '<td><input type="hidden" name="remain_' + rowData.id + '" id="remain_' + rowData.id + '" value="' + rowData.remainAmount + '">' + rowData.remainAmount + '</td>' +
            '<td><input type="text" name="fund_' + rowData.id + '" id="fund_' + rowData.id + '" onkeyup="this.value=this.value.toString().match(/^\\d+(?:\\.\\d{0,2})?/)"></td></tr>';
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

    //若用户选择抵消借款，将从借款记录中选择一条借款
    $("#selBorrow").click(function () {
        $.ajax({
            type: "post",
            url: baseUrl + "/reimbursement/queryBorrowByRemId",    //向后端请求数据的url
            data: {id: $("#id").val()},
            dataType: "json",
            async: false,
            success: function (data) {
                if (data.code == 200) {
                    if (data.data.list != null && data.data.list.length > 0) {
                        swal("已选择了冲抵借款信息，不支持修改，请点击旁边黄色的×按钮，清除之前关联的借款，然后重新选择！", "", "warning");
                    } else {
                        layer.confirm("请确认已保存当前页面信息，跳转后当前页面未保存的信息将丢失!", {
                            btn: ['确认', '返回'], //按钮
                            shade: false //不显示遮罩
                        }, function (index) {
                            layer.close(index);
                            document.getElementById("borrowForm").reset();
                            $("#selectedBorrowId").val("");
                            $("#borrowDetail").empty();
                            borrowList.length = 0;
                            $("#editModal").modal("hide");
                            $("#borrowModal").modal({backdrop: "static"});
                            $("#borrow_table_logs").jqGrid('setGridParam', {
                                datatype: "json",
                                postData: $("#borrowForm").serializeJson(), //发送数据
                            }).trigger("reloadGrid"); //重新载入
                            resize("#borrow_pager_logs");
                        })
                    }
                } else {
                    if (getResCode(data))
                        return;
                }
            }
        });
    });

    //清除借款信息
    $("#cleanBorrow").click(function () {
        var lock = true;
        layer.confirm('删除已选的冲抵借款信息？', {
            btn: ['删除', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/reimbursement/cleanBorrowInfo",    //向后端请求数据的url
                    data: {id: $("#id").val()},
                    dataType: "json",
                    success: function (data) {
                        layer.close(index);
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            $("#borrowId").val("");
                            $("#unpaidLoan").val(0);
                            $("#borrowDetail").empty();
                            $("#borrowInfo").empty();
                            calculatePrice();
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

    //新增报销页面，点击关闭的x按钮
    $("#btn_close").click(function () {
        $("#selBorrow").hide();
        $("#cleanBorrow").hide();
        $("#editModal").modal("toggle");
    });

    $("#selectBorrow").click(function () {
        var lock = true;
        if (borrowList === 0) {
            swal("请先选择借款！");
            return;
        }
        $("#borrowIds").val(borrowList.toString());
        $("#borrowForm [name='id']").val($("#id").val());
        layer.confirm('请确认抵消的借款信息？提交后不能取消！', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            var param = $("#borrowForm").serializeJson();
            startModal("#selectBorrow");//锁定按钮，防止重复提交
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    data: param,
                    url: baseUrl + "/reimbursement/saveBorrowInfo",
                    dataType: "json",
                    success: function (data) {
                        layer.close(index);
                        Ladda.stopAll();   //解锁按钮锁定
                        if (data.code == 200) {
                            $("#borrowModal").modal('hide');
                            view($("#id").val(), -2);
                        } else if (data.code == 1002) {
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
            }
        }, function () {
            return;
        });

    });

    $('#borrowModal').on('hidden.bs.modal', function () {
        $("#editModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $('#accountModal').on('hidden.bs.modal', function () {
        $("#editModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $('#imgModal').on('hidden.bs.modal', function () {
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });
});
//https://www.cnblogs.com/danywdd/p/5667903.html   jquery validate 验证插件 解决多个相同的Name 只验证第一个的方案
if ($.validator) {
    $.validator.prototype.elements = function () {
        var validator = this,
            rulesCache = {};

        // select all valid inputs inside the form (no submit or reset buttons)
        return $(this.currentForm)
            .find("input, select, textarea")
            .not(":submit, :reset, :image, [disabled]")
            .not(this.settings.ignore)
            .filter(function () {
                if (!this.name && validator.settings.debug && window.console) {
                    console.error("%o has no name assigned", this);
                }
                //注释这行代码
                // select only the first element for each name, and only those with rules specified
                //if ( this.name in rulesCache || !validator.objectLength($(this).rules()) ) {
                //    return false;
                //}
                rulesCache[this.name] = true;
                return true;
            });
    }
}

function changeAccount() {
    if ($("#outAccountIds").val() == undefined || $("#outAccountIds").val() == "" || $("#outAccountIds").val() == null) {
        swal("请先选择出款账户！")
        return;
    }
    layer.confirm('请确认出款信息？', {
        btn: ['确认','取消'],
        shade:false
    },function (index) {
    layer.close(index);
    var param ={
        id:$("#id").val(),
        outAccountIds:$("#outAccountIds").val(),
        payTime:$("#payTime").val(),
    };
    startModal("#changeAccountBtn");
    $.ajax({
        type: "post",
        data: param,
        url: baseUrl+"/reimbursement/chaneAccount",
        dataType:"json",
        success:function (data) {
            Ladda.stopAll();   //解锁按钮锁定
            if (data.code == 200) {
                $("#editModal").modal('hide');
                swal(data.data.message);
                $("#outAccountIds").val("");
                $("#query_table_logs").emptyGridParam();
                $("#query_table_logs").reloadCurrentData(baseUrl + "/reimbursement/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
    })

    })

}

//下载打印功能
var downLoadAndPrintObj = {
    dowloadAndPritPermission: false, //打印权限控制
    state:{"-1":"审核驳回","0":"已保存","1":"已完成","2":"审核通过","3":"组长审核","4":"部长审核","5":"总监审核","6":"财务总监审核","7":"副总经理审核",
        "8":"总经理审核","9":"会计确认出款","10":"业务员确认","12":"财务部长审核","16":"出纳出款","30":"集团财务部门负责人","31":"集团财务分管领导","32":"集团总裁"},
    type:{"0":"备用金", "1":"其他"},
    downloadMap:{},
    loadFlowPrintPermission:function () {
        requestData(null,"/refund/getFlowPrintPermission", "post", function (data) {
            if(data.code == 200){
                downLoadAndPrintObj.dowloadAndPritPermission = true;
                $("#batchDownload").css("display", "inline-block");
                $("#batchPrint").css("display", "inline-block");
            }else {
                downLoadAndPrintObj.dowloadAndPritPermission = false;
                $("#batchDownload").css("display", "none");
                $("#batchPrint").css("display", "none");
            }
        });
    },
    viewModalShow: function (id) {
        layer.open({
            type: 1,
            title: 0,
            zIndex: 10,
            content: $("#reimburseViewModal").html(),
            btn: ['下载','打印','取消'],
            area: ['70%', '60%'],
            closeBtn: 0,
            shadeClose: 0,
            resize: false,
            move: '.modalTitle',
            moveOut: true,
            success: function(layero, index){
                downLoadAndPrintObj.downloadMap = {};
                requestData({id: id}, "/reimbursement/view", "post", function (data) {
                    if(data.code==200){
                        var title = "【报销】"+data.data.entity["code"]+"-"+data.data.entity["applyName"]+"-"+data.data.entity["applyTime"];
                        downLoadAndPrintObj.downloadMap["modalTitle"] = title;
                        $(layero[0]).find("div .modalTitle").text(title);
                        for (var attr in data.data.entity) {
                            //附件
                            if(attr == 'names'){
                                if(data.data.entity[attr] && data.data.entity[attr].length > 0){
                                    $(layero[0]).find("div .affixName").text(data.data.entity[attr].join(',') || "");
                                    downLoadAndPrintObj.downloadMap["affixName"] = data.data.entity[attr].join(',') || "";
                                }else {
                                    $(layero[0]).find("div .affixName").text("");
                                    downLoadAndPrintObj.downloadMap["affixName"] = "";
                                }
                            }else if(attr == "payAmount"){
                                var payAmount = parseInt(data.data.entity["totalMoney"] || "0") - parseInt(data.data.entity["unpaidLoan"] || "0");
                                $(layero[0]).find("div ."+attr).text(payAmount);
                                downLoadAndPrintObj.downloadMap[attr] = payAmount || "0";
                            }else if(attr == "state"){
                                $(layero[0]).find("div ."+attr).text(downLoadAndPrintObj.state[data.data.entity[attr]] || "");
                                downLoadAndPrintObj.downloadMap[attr] = downLoadAndPrintObj.state[data.data.entity[attr]]  || "";
                            }else if(attr == "payTime"){
                                $(layero[0]).find("div ."+attr).text(data.data.entity[attr] ? new Date(data.data.entity[attr]).format("yyyy-MM-dd hh:mm:ss") : "");
                                downLoadAndPrintObj.downloadMap[attr] = data.data.entity[attr] ? new Date(data.data.entity[attr]).format("yyyy-MM-dd hh:mm:ss") : "";
                            }else if(attr == "reimbursementDs"){
                                var html = "";
                                if(data.data.entity[attr] && data.data.entity[attr].length > 0){
                                    $.each(data.data.entity[attr], function (i, item) {
                                        html += "<tr>\n" +
                                            "        <td>"+(i+1)+"</td>\n" +
                                            "        <td>"+(item.costType || "")+"</td>\n" +
                                            "        <td>"+(item.purpose || "")+"</td>\n" +
                                            "        <td>"+(item.money || "")+"</td>\n" +
                                            "        <td>"+(item.numberOfDocument || "")+"</td>\n" +
                                            "    </tr>";
                                    });
                                    downLoadAndPrintObj.downloadMap["detailTable"] = JSON.stringify(data.data.entity[attr]);
                                    $(layero[0]).find(".detailTable").find("tbody").html(html);
                                }
                            }else {
                                if(attr != 'affixName'){
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
                                    "              <td>"+(item.applyName || "")+"</td>\n" +
                                    "              <td>"+(item.deptName || "")+"</td>\n" +
                                    "              <td>"+(item.applyAmount || "0")+"</td>\n" +
                                    "              <td>"+(item.repayAmount || "0")+"</td>\n" +
                                    "              <td>"+(item.remainAmount || "0")+"</td>\n" +
                                    "              <td>"+(item.amount || "0")+"</td>\n" +
                                    "          </tr>";
                                item.type = (downLoadAndPrintObj.type[item.type] || "");
                            });
                            downLoadAndPrintObj.downloadMap["borrowTable"] = JSON.stringify(borrowList);
                            $(layero[0]).find(".borrowTable").find("tbody").html(borrowHtml);
                        }else {
                            $(layero[0]).find(".borrowTable").css("display", "none");
                        }
                    }
                });
                requestData({dataId:id, process:12},"/process/history", "post", function (data) {
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
                });
            },
            yes: function (index, layero) {
                requestData(downLoadAndPrintObj.downloadMap, "/reimbursement/downloadData", "post", function (data) {
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
        requestData($("#queryForm").serializeJson(),"/reimbursement/batchPrintData", "post", function (data) {
            if(data.code == 200){
                if(data.data.list && data.data.list.length > 0){
                    $.each(data.data.list, function (k, reimburse) {
                        for (var attr in reimburse) {
                            //备用金详情
                            if(reimburse["borrowTable"] && reimburse["borrowTable"].length > 0){
                                $("#modalContentWrap").find(".borrowTable").css("display", "flex");
                            }else {
                                $("#modalContentWrap").find(".borrowTable").css("display", "none");
                            }
                            if(attr == "detailTable"){
                                var detailHtml = "";
                                if(reimburse[attr] && reimburse[attr].length > 0){
                                    $.each(reimburse[attr], function (i, item) {
                                        detailHtml += "<tr>\n" +
                                            "        <td>"+(i+1)+"</td>\n" +
                                            "        <td>"+(item.costType || "")+"</td>\n" +
                                            "        <td>"+(item.purpose || "")+"</td>\n" +
                                            "        <td>"+(item.money || "")+"</td>\n" +
                                            "        <td>"+(item.numberOfDocument || "")+"</td>\n" +
                                            "    </tr>";
                                    });
                                    $("#modalContentWrap").find(".detailTable").find("tbody").html(detailHtml);
                                }
                            }else if(attr == "auditTable"){
                                var auditHtml = "";
                                if(reimburse[attr] && reimburse[attr].length > 0){
                                    $.each(reimburse[attr], function (i, item) {
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
                                if(reimburse["borrowTable"] && reimburse["borrowTable"].length > 0){
                                    var borrowHtml = "";
                                    $.each(reimburse["borrowTable"], function (i, item) {
                                        borrowHtml += "<tr>\n" +
                                            "              <td>"+(item.code || "")+"</td>\n" +
                                            "              <td>"+(item.title || "")+"</td>\n" +
                                            "              <td>"+(item.type || "")+"</td>\n" +
                                            "              <td>"+(item.applyName || "")+"</td>\n" +
                                            "              <td>"+(item.deptName || "")+"</td>\n" +
                                            "              <td>"+(item.applyAmount || "0")+"</td>\n" +
                                            "              <td>"+(item.repayAmount || "0")+"</td>\n" +
                                            "              <td>"+(item.remainAmount || "0")+"</td>\n" +
                                            "              <td>"+(item.amount || "0")+"</td>\n" +
                                            "          </tr>";
                                    });
                                    $("#modalContentWrap").find(".borrowTable").find("tbody").html(borrowHtml);
                                }
                            }else if(attr == "payTime"){
                                $("#modalContentWrap").find("."+attr).text(reimburse[attr] ? new Date(reimburse[attr]).format("yyyy-MM-dd hh:mm:ss") : "");
                            }else {
                                $("#modalContentWrap").find("."+attr).text("");
                                $("#modalContentWrap").find("."+attr).text(reimburse[attr] || "");
                            }
                        }
                        html += $("#modalContentWrap")[0].outerHTML + "\n";
                    });
                }else {
                    layer.msg('无报销打印数据！', {time: 2000, icon: 6});
                    return;
                }
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
                return;
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
        requestData($("#queryForm").serializeJson(),"/reimbursement/batchDownloadData", "post", function (data) {
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
        });
    }
}