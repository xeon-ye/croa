var arrayList = [];
var saleSum = 0;
var incomeSum = 0;
var outgoSum = 0;
//初始化业务部门
function getDept(){
    var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
    var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
    var deptDiv = document.getElementById("deptDiv");
    //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
    if(((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'YW'|| user.dept.code == 'GL')) || user.dept.code == 'CW'){
        deptDiv.style.display = 'block';

        $("#selDept").click(function () {
            $("#deptModal").modal({backdrop: "static"});
        });
        $('#treeview').treeview({
            data: [getTreeData(isZC())],
            onNodeSelected: function (event, data) {
                $("#deptId").val("");//每次选择时，先清空
                $("#chooseDeptName").val(data.text);
                $("#deptModal").modal('hide');
                $("#deptId").val(data.id);
            }
        });
        $("#cleanDept").click(function () {
            $("#deptId").val("");
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
};

var requestData = function (data, url, requestType,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: false,
        success: callBackFun
    });
};

//获取部门树数据
function getTreeData(isZC) {
    var deptTreeData = {};
    var deptId = user.dept.id;//当前用户部门ID
    var deptCode = user.dept.code;//当前部门编码
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    if(deptCompanyCode == "JT" && (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL")){
        requestData(null,"/dept/getRootDept","POST",function (result) {
            var root = result.data.root;
            if (root){
                deptId = root.id;//整个集团的业务和媒介部
            }else{
                deptId = 517;//整个集团的业务和媒介部
            }
        });
    }else if(deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL"){
        requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST",function (result) {
            var company = result.data.company;
            if (company){
                deptId = company.id;//整个集团的业务和媒介部
            }
        });
    }
    //具体查询
    requestData({deptId: deptId,deptCode:'YW'},"/dept/listAllDeptByIdAndCode","POST",function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

var businessObj = {
    add: function () {
        $("#flag").val("");
        businessObj.initCust(true);
        $("#editModal").modal({backdrop: "static"});
    },

    initCust: function (flag) {
        $("#custCompanyId").val("");
        $("#custCompanyName").val("");
        $("#custId").val("");
        $("#custName").val("");
        $(".firstDiv").show();
        $(".secondDiv").hide();
        $(".thirdDiv").hide();
        $("#firstStep").click();
        document.getElementById("firstForm").reset();
        $("#editModal .modal-title").html("开票信息");
        //清空选中项
        arrayList.length = 0;//清空选中的稿件id
        saleSum = 0;
        incomeSum = 0;
        outgoSum = 0;
        $("#saleSum2").text(saleSum.toFixed(2));
        $("#incomeSum2").text(incomeSum.toFixed(2));
        $("#outgoSum2").text(outgoSum.toFixed(2));
        // if(flag){
            setTimeout(function () {
                gridObj.reloadCust();
            }, 500);
        // }
        //刷新按钮每个tab页执行的方法不一样，先解绑后绑定
        $("#refresh").off("click").on("click",function () {
            businessObj.initCust(false);
        });
    },

    initArticle: function () {
        var rowid = $("#select_cust_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = $("#select_cust_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        if (rowData.custId == null || rowData.custId == undefined || rowData.custId == "") {
            swal("请先选择一个客户!");
            return;
        } else {
            if(rowData.type == 1){//type=1时为true，表示公司客户
                if(rowData.state){//state=1时为true表示有效
                    if(rowData.standardize == 0 || rowData.normalize == 0){
                        layer.msg("企业客户必须标准且规范才能开票，请先修改公司名！", {time: 2500, shade: [0.7, '#393D49']});
                        companyObj.editCompanyBasic(rowData.custId);
                        return;
                    }
                }else{//state=0时为false表示失效
                    if(rowData.standardize == 0 || rowData.normalize == 0){
                        swal("该企业客户是弱保护客户，且已失效，不能开票！");
                        return;
                    }
                }
            }else{//type=0时为false，表示个体工商户
                if(rowData.state){//state=1时为true表示有效
                    if(rowData.normalize == 0){
                        layer.msg("个体工商户必须是规范的才能开票，请先修改公司名！", {time: 2500, shade: [0.7, '#393D49']});
                        companyObj.editCompanyBasic(rowData.custId);
                        return;
                    }
                }else{//state=0时为false表示失效
                    if(rowData.standardize == 0 || rowData.normalize == 0){
                        swal("该个体工商户是非规范客户，且已失效，不能开票！");
                        return;
                    }
                }
            }
            document.getElementById("secondForm").reset();
            $("#custCompanyId").val(rowData.companyId);
            $("#custCompanyName").val(rowData.companyName);
            $("#custId").val(rowData.custId);
            $("#custName").val(rowData.custName);
            $("#saleSum1").text("0.00");
            $("#incomeSum1").text("0.00");
            $("#outgoSum1").text("0.00");
            $(".firstDiv").hide();
            $(".secondDiv").show();
            $("#secondStep").click();
            $("#custCompanyIdSec").val(rowData.companyId);
            $("#custCompanyNameSec").val(rowData.companyName);
            $("#custIdSec").val(rowData.custId);
            $("#custNameSec").val(rowData.custName);
            $("#startTimeSec").val(rowData.startTime);
            $("#endTimeSec").val(rowData.endTime);
            $("#startTimeText").html(rowData.startTime);
            $("#endTimeText").html(rowData.endTime);

            commonObj.sumArticleAmount();
            setTimeout(function () {
                gridObj.reloadSelectArticle();
            }, 500);
        }
        //刷新按钮每个tab页执行的方法不一样，先解绑后绑定
        $("#refresh").off("click").on("click",function () {
            businessObj.initArticle(false);
        });
    },

    articleSearch: function() {
        gridObj.reloadSelectArticle();
        commonObj.sumArticleAmount();
    },

    selectArticle: function(){
        var checkbox = document.getElementsByName("checkboxState");
        if(checkbox[0].checked){
            checkState = checkbox[0].value
        }else{
            checkState = null;
        }
        $("#checkState").val(checkState);
        if (arrayList.length == 0 && !checkState) {
            swal("请先选择稿件！");
        } else {
            $("#articleIdsSec").val(arrayList.toString());
            //刷新按钮每个tab页执行的方法不一样，先解绑后绑定
            $("#refresh").off("click").on("click",function () {
                if ($("#flag").val()) {
                    businessObj.view($("#id").val(),$("#flag").val());
                }
            });
            //下一步
            businessObj.saveStepOne();
        }
    },

    del: function (id) {
        var lock = true ;
        layer.confirm('确认删除？', {
            btn: ['删除', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: "/invoice/del",    //向后端请求数据的url
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        layer.close(index);
                        if (data.code == 200) {
                            swal(data.data.message);
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/invoice/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
    },
    //申请人撤回
    returnBack: function (taskId, itemId) {
        var lock = true ;
        layer.confirm('确认撤回？', {
            btn: ['撤回', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/process/withdraw",    //向后端请求数据的url
                    data: {taskId: taskId, itemId: itemId},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            swal(data.data.message);
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/invoice/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
    },

    //state=2用这个撤回
    CWReject: function (t) {
        var lock = true ;
        var id = $("#editForm #id").val();
        layer.confirm('确认撤回？', {
            btn: ['撤回', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#invoice");//锁定按钮，防止重复提交
            startModal("#ZLReturn");//锁定按钮，防止重复提交
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/invoice/CWReject",    //向后端请求数据的url
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 2000, icon: 6});
                            $("#editModal").modal("hide");
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/invoice/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            approveTask($("#taskId").val(), 0,t.id,$("#desc1").val());
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
            $("#articleIdsSec").val("");
            return;
        });
    },
    //state=1||state=12用这个撤回，
    CWReturn: function(id) {
        var lock = true ;
        layer.confirm('确认撤回？', {
            btn: ['撤回', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/invoice/CWReturn",    //向后端请求数据的url
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 2000, icon: 6});
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/invoice/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
            $("#articleIdsSec").val("");
            return;
        });
    },

    //审核记录查看
    showHistory: function (id) {
        //process详见IProcess
        $("#historyModal").modal({backdrop: "static"});
        $.ajax({
            type: "post",
            url: "/process/history",
            data: {dataId: id, process: 1},
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
                        html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=1&t=" + new Date().getTime() + "' style='width: 100%; margin-top: 8px; margin-bottom: -100px; margin-left: -130px;'/></div>";
                        $("#history").append(html);
                    }
                } else {
                    if (getResCode(data))
                        return;
                }
            }
        });
    },
    //新增页面flag="",flag=-1查看页面,flag=0其他页面跳转的查看页面，flag=1审核页面，flag=2确认页面，flag=3开票
    /**
     * flag 的值对应的页面
     * 0 编辑页面
     * 1 双击查看页面
     * 2 其他页面跳转的查看页面
     * 3 审核页面
     * 4 确认页面
     * 5 开票页面
     */
    view: function (id, flag) {
        $("#flag").val(flag);
        $("#editModal").modal({backdrop: "static"});
        $(".firstDiv").hide();
        $(".secondDiv").hide();
        $(".thirdDiv").show();
        $("#thirdStep").click();
        document.getElementById("editForm").reset();
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();
        $("#editModal input:radio").removeAttr("checked");
        $("#editModal input:radio").parent().removeClass("checked");
        if (flag == 0){//编辑
            $("#navTabs").show();
            $("#hideOnEdit").hide();
            $("#editFooter").show();
            $("#viewFooter").hide();
            $("#invoiceFooter").hide();
        }else{
            $("#navTabs").hide();
            $("#hideOnEdit").show();
            $("#editFooter").hide();
            $("#viewFooter").show();
            if(flag == 5 ){//开票
                $("#viewFooter").hide();
                $("#invoiceFooter").show();
            }else{
                $("#viewFooter").show();
                $("#invoiceFooter").hide();
                if(flag == 1){//查看
                    var returnType= getQueryString("returnType");
                    if(returnType==5){
                        $("#invoiceFooter").hide();
                        $("#auditTrue").show();
                        $("#auditFalse").hide();
                        $("#reject1").hide();
                        $("#approve1").hide();
                        $("#showHistory1").hide();
                    } else {
                        $("#auditTrue").hide();
                        $("#auditFalse").show();
                        $("#KJReturn").hide();
                        $("#goback").hide();//查看页面返回按钮隐藏
                        $("#confirm").hide();
                    }

                }else  if(flag == -1){
                    $("#invoiceFooter").hide();
                    $("#auditTrue").show();
                    $("#auditFalse").hide();
                    $("#reject1").hide();
                    $("#approve1").hide();
                    $("#showHistory1").hide();
                }
                else if(flag == 2){//跳转的查看
                    $("#auditTrue").hide();
                    $("#auditFalse").show();
                    $("#KJReturn").hide();
                    $("#cancel1").hide();
                    $("#confirm").hide();
                }else if(flag == 3){//审核
                    $("#showHistory1").data("id",id);
                    $("#auditTrue").show();
                    $("#auditFalse").hide();
                }else if(flag == 4){//确认
                    $("#auditTrue").hide();
                    $("#auditFalse").show();
                    $("#confirm").show();
                    $("#goback").hide();
                }
            }
        }

        $.ajax({
            type: "post",
            url: "/invoice/view",
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if(data.code == 200){
                    commonObj.taxAssistant("#financialAssistant",data.data.entity.taxType);
                    for (var attr in data.data.entity) {
                        $("#editForm [name='" + attr + "'][type!='radio']").val(data.data.entity[attr]);
                        if(flag == 0){
                            if(attr == "custCompanyName" ||
                                attr == "custName" ||
                                attr == "amount" ||
                                attr == "taxPoint" ||
                                attr == "applyName" ||
                                attr == "deptName" ||
                                attr == "code" ||
                                attr == "title" ||
                                attr == "taxAmount"){
                                $("#editForm [name=" + attr + "]").attr("readonly","readonly");
                            }
                            $("#editForm [name=" + attr + "]").removeAttrs("style");
                        }else{
                            $("#editForm [name=" + attr + "]").removeAttrs("readonly");
                            if(!(flag == 5 && attr == "no")){
                                $("#editForm [name=" + attr + "]").prop("style", "border:0;");
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
                                        stateStr = "确认开票";
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
                                    case 11 :
                                        stateStr = "开票";
                                        break;
                                    case 12 :
                                        stateStr = "财务部长审核";
                                        break;
                                    default :
                                        break;
                                }
                                if(dataStr == 6){
                                    $(".licence").show();
                                    $("#approve1").hide();
                                }else {
                                    $(".licence").hide();
                                    $("#approve1").show();
                                }
                                if (dataStr != null) {
                                    $("#editForm [name='state1']").val(stateStr);
                                }
                                if(dataStr == 1){
                                    $(".showAfterInvoice").show() ;
                                }else{
                                    $(".showAfterInvoice").hide() ;
                                }
                                if(dataStr == 11 || dataStr == 1){
                                    $(".showOnInvoice").show() ;
                                }else{
                                    $(".showOnInvoice").hide() ;
                                }
                            }
                        }

                        if (attr == "invoiceType") {
                            $('#editForm input:radio[name="invoiceType"]').off('ifChecked').on('ifChecked', function (event) {
                                commonObj.checkInvoice();
                            });
                            $("#editForm input[name='invoiceType']").removeAttrs("disabled");
                            if(flag == 0){
                                $("#editForm input[name='invoiceType'][value='" + data.data.entity[attr] + "']").iCheck("check");
                            }else{
                                $("#editForm input[name='invoiceType']").attr("disabled", "disabled");
                                $("#editForm input[name='invoiceType'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                                $("#editForm input[name='invoiceType'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                            }
                        }
                        if (attr == "taxType") {
                            $("#taxesDiv").empty();
                            var html = "" ;
                            if(flag == 0){//编辑添加select框
                                html += "<select class='form-control m-b' name='taxType' id='taxType' required=''>";
                                html += "<option value=''></option>";
                                for (var i = 0; i < data.data.taxes.length; i++) {
                                    if (data.data.taxes[i].name == data.data.entity[attr]) {
                                        html += "<option value='" + data.data.taxes[i].name + "' selected='selected'>" + data.data.taxes[i].name + "</option>"
                                    } else {
                                        html += "<option value='" + data.data.taxes[i].name + "' >" + data.data.taxes[i].name + "</option>"
                                    }
                                }
                                html += "</select>";
                            }else {//查看添加文本域
                                for (var i = 0; i < data.data.taxesAll.length; i++) {
                                    if (data.data.taxesAll[i].name == data.data.entity[attr]) {
                                        html += "<input type='text' class='form-control' id='taxType' name='taxType' style='border:0;' value='"+data.data.taxesAll[i].name+"'/>" ;
                                    }
                                }
                            }
                            $("#taxesDiv").append(html);
                        }
                    }
                    if(flag == 0){
                        var invoiceTitle = data.data.entity['custCompanyName'];
                        if(invoiceTitle == "个体工商户"){
                            invoiceTitle = data.data.entity['custName'];
                        }
                        $("#editForm input[name='title']").val(invoiceTitle);
                        $("#amount").val(data.data.saleSum);
                        var invoiceAmount = data.data.entity["invoiceAmount"];
                        if(invoiceAmount==null){
                            invoiceAmount=data.data.saleSum;
                        }
                        $("#invoiceAmount").val(invoiceAmount);
                        $("#taxType").change(function () {
                            commonObj.getTaxInfo();
                        });
                        $("#editForm select").trigger("change");
                    }else{
                        $("#editModal .modal-title").html(data.data.entity['name']);
                    }
                    $("#outgoSum").html(data.data.outgoSum);
                    $("#saleSum").html(data.data.saleSum);
                    $("#incomeSum").html(data.data.incomeSum);
                    $("#taxSum").html(data.data.taxSum);

                    document.getElementById("selectedForm").reset();
                    $("#selectedForm [name='id']").val(id);
                    setTimeout(function () {
                        gridObj.reloadSelectedArticle();
                    }, 500);
                } else if(data.code == 1002){
                    $("#editModal").modal("hide");
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
    },
    //确认开票
    confirmInfo: function () {
        var lock = true ;
        var id = $("#id").val();
        layer.confirm('确认开票信息？', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#confirm");//锁定按钮，防止重复提交
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: "/invoice/confirm",    //向后端请求数据的url
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();//解锁按钮锁定
                        $("#editModal").modal("hide");
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            $("#query_table_logs").emptyGridParam() ;
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/invoice/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
    },

    saveStepOne: function () {
        var lock = true ;
        startModal("#backStepOne");//锁定按钮，防止重复提交
        startModal("#selectArticle");//锁定按钮，防止重复提交
        layer.confirm('已选定客户和稿件？确定后不能更改！', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: "/invoice/saveStepOne",    //向后端请求数据的url
                    data: $("#secondForm").serializeJson(),
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();//解锁按钮锁定
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            setTimeout(function () {
                                $("#query_table_logs").emptyGridParam() ;
                                $("#query_table_logs").reloadCurrentData(baseUrl + "/invoice/listPg", $("#queryForm").serializeJson(), "json", null, null);
                                businessObj.view(data.data.entity.id,0);
                            }, 1000);
                        } else if(data.code == 1002) {
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        }else{
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
    },

    submitHander: function (t, url) {
        var lock = true ;
        if ($("#editForm").valid()) {
            // return ;
            layer.confirm('确认提交审核？提交后不能更改！', {
                btn: ['确定', '取消'], //按钮
                shade: false //不显示遮罩
            }, function (index) {
                layer.close(index);
                var param = $("#editForm").serializeForm();
                startModal("#" + t.id);//锁定按钮，防止重复提交
                layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        data: param,
                        url: url,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();   //解锁按钮锁定
                            if (data.code == 200) {
                                swal(data.data.message);
                                $("#query_table_logs").emptyGridParam();
                                $("#query_table_logs").reloadCurrentData(baseUrl + "/invoice/listPg", $("#queryForm").serializeJson(), "json", null, null);
                                $("#editModal").modal('hide');
                            } else if (data.code == 1002) {
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
    },
    //开票
    invoice: function (t, url) {
        var lock = true ;
        if ($("#editForm").valid()) {
            layer.confirm('确认开票？开票后不能退回！', {
                btn: ['开票', '取消'], //按钮
                shade: false //不显示遮罩
            }, function (index) {
                layer.close(index);
                var param = $("#editForm").serializeJson();
                param.desc = $("#desc1").val();
                startModal("#" + t.id);//锁定按钮，防止重复提交
                startModal("#ZLReturn");//锁定按钮，防止重复提交
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
                                layer.msg(data.data.message, {time: 1000, icon: 6});
                                $("#query_table_logs").emptyGridParam() ;
                                $("#query_table_logs").reloadCurrentData(baseUrl + "/invoice/listPg", $("#queryForm").serializeJson(), "json", null, null);
                                // approveTask($("#taskId").val(), 1,t.id);
                            } else if(data.code == 1002){
                                swal({
                                    title: "异常提示",
                                    text: data.msg,
                                });
                            } else {
                                if (getResCode(data))
                                    return;
                            }
                            $("#editModal").modal('hide');
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
    },
    //导出
    exportInvoice: function () {
        var params =  removeBlank($("#queryForm").serializeJson());
        location.href = "/invoice/exportInvoice" + "?" + $.param(params);
    }

};

var commonObj = {
    getTaxInfo: function () {
        if ($("#taxType").val() != undefined && $("#taxType").val() != null) {
            $.ajax({
                type: "post",
                url: "/dict/view",
                data: {typeCode: 'tax', name: $("#taxType").val()},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        if (data.data.entity != null) {
                            $("#taxPoint").val(data.data.entity.code);
                            var taxPoint = data.data.entity.code;
                            if(isNaN(parseFloat(taxPoint))){
                                swal("税点不正确！应该是小数，实际是："+taxPoint);
                                return ;
                            }else{
                                taxPoint = parseFloat(taxPoint) ;
                            }
                            var ratio = data.data.entity.type;
                            $("#ratio").val(ratio) ;
                            if(isNaN(parseFloat(ratio))){
                                swal("该抬头对应的换算比不正确！应该为数字，实际是："+ratio);
                                return ;
                            }else{
                                ratio = parseFloat(ratio) ;
                            }
                            var amount = $("#invoiceAmount").val();
                            if(isNaN(parseFloat(amount))){
                                swal("实际开票金额不正确！应该是数字，实际是："+amount);
                                return ;
                            }else{
                                amount = parseFloat(amount) ;
                            }
                            $("#taxAmount").val(Math.round(taxPoint * amount * 100 / ratio) / 100);
                        }
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            })
        }
    },
    checkInvoice: function () {//专票限制
        if ($('#editForm input:radio[name="invoiceType"]:checked').val() == 2) {
            $('.invoiceStar').show();
            $('.specInvoice').attr("required", "");
            $("#editForm").find("input").removeClass('error');//清除验证标签
            $("#editForm").validate().resetForm();
        } else {
            $('.invoiceStar').hide();
            $('.specInvoice').removeAttr("required");
            $("#editForm").find("input").removeClass('error');//清除验证标签
            $("#editForm").validate().resetForm();
        }
    },
    //重新加载可以开票的总金额
    sumArticleAmount: function () {
        $.ajax({
            type: "post",
            data: $("#secondForm").serializeJson(),
            url: '/invoice/listPgForSelectArticleSum',
            dataType: "json",
            success: function (data) {
                $("#saleSum1").text(data.saleAmountSum.toFixed(2));
                $("#incomeSum1").text(data.incomeAmountSum.toFixed(2));
                $("#outgoSum1").text(data.outgoAmountSum.toFixed(2));
            },
        });
    },
    ///根据抬头类型查财务助理
    taxAssistant: function (t,taxType) {
        layui.use(["form"], function () {
            $.get(baseUrl + "/taxUser/taxAssistant",{taxType:taxType}, function (data) {
                if (data.length>1){
                    $(t).append("<option value=\"\">请选择</option>");
                }
                $(data).each(function (i, d) {
                    var value = $(t).attr("data-value");
                    var selected = value == d.id ? "selected=selected" : "";
                    $(t).append("<option value='" + d.id + "' " + selected + " data-id='"+d.deptId+"'>" + d.name + "</option>");
                });
                layui.form.render();
            }, "json");
        });
    },
    // 提示信息增加遮罩层；
    alertMessage: function (content) {
        layer.msg(content, {time: 1000, shade: [0.7, '#393D49']});
    }
};

$(document).ready(function () {
    getDept();
    $.jgrid.defaults.styleUI = 'Bootstrap';
    var e = "<i class='fa fa-times-circle'></i> ";
    $("#editForm").validate({
        rules: {
            taxCode: {maxlength: 25},
            phone: {maxlength: 25}
        },
        messages: {
            taxCode: {maxlength: e + "税号长度必须小于{0}个字符"},
            phone: {maxlength: e + "公司电话长度必须小于{0}个字符"}
        }
    });
    //flag=1审核，否则查看
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        businessObj.view(getQueryString("id"), getQueryString("flag"));
    }

    if (getQueryString("confirmId") != null && getQueryString("confirmId") != "" && getQueryString("confirmId") != undefined) {
        businessObj.view(getQueryString("confirmId"), 4);
    }

    if (getQueryString("invoiceId") != null && getQueryString("invoiceId") != "" && getQueryString("invoiceId") != undefined) {
        businessObj.view(getQueryString("invoiceId"),5);
    }

    if (getQueryString("editId") != null && getQueryString("editId") != "" && getQueryString("editId") != undefined) {
        businessObj.view(getQueryString("editId"),0);
    }

    if (getQueryString("add") != null && getQueryString("add") != "" && getQueryString("add") != undefined) {
        businessObj.add();
    }

    if(hasRoleYW()){
        $("#addBtn").show();
    }else{
        $("#addBtn").hide();
    }

    $("#refresh").click(function () {
        if ($("#flag").val()) {
            businessObj.view($("#id").val(),$("#flag").val());
        }
    });

    $('#editCompanyModal').on('hidden.bs.modal', function () {
        $("#editModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $("#enterpriseBtn").click(function () {
        companyObj.natureClick(this, 1, $("#addCompanyForm input[name='companyName']").val());
    });
    $("#personalBtn").click(function () {
        companyObj.natureClick(this, 0, $("#addCompanyForm input[name='companyName']").val());
    });

    $("#query_table_logs").jqGrid({
        url: '/invoice/listPg',
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
        rowList: [10, 50, 100, 500, 1000],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'code', label: '申请编号', editable: true, width: 80},
            {name: 'name', label: '标题', editable: true, width: 60},
            {name: 'applyName', label: '申请人', editable: true, width: 60},
            {name: 'deptName', label: '所在部门', editable: true, width: 80},
            {name: 'applyTime', label: '申请日期', editable: true, width: 80},
            {name: 'custCompanyName', label: '客户公司名称', editable: true, width: 60},
            {name: 'custName', label: '联系人', editable: true, width: 60},
            {
                name: 'type', label: '发票类型', editable: true, width: 60,
                formatter: function (a, b, rowdata) {
                    var d = rowdata.invoiceType;
                    if (d == 0) {
                        return "<span style=''>未指定</span>"
                    } else if (d == 1) {
                        return "普票";
                    } else if (d == 2) {
                        return "<span style='color:#3f51b5'>专票</span>"
                    }
                }
            },
            {name: 'title', label: '开票公司名称', editable: true, width: 80},
            {name: 'taxCode', label: '税号', editable: true, width: 60},
            {name: 'taxType', label: '抬头', editable: true, width: 60},
            {name: 'taxPoint', label: '税点', editable: true, width: 60},
            {name: 'taxAmount', label: '税额', editable: true, width: 60},
            {name: 'invoiceTime', label: '开票日期', editable: true, width: 80},
            {name: 'invoiceAmount', label: '实际开票金额', editable: true, width: 60},
            {name: 'amount', label: '价税合计', editable: true, width: 60},
            {name: 'taskId', label: 'taskId', editable: true, hidden: true, width: 80},
            {name: 'itemId', label: 'itemId', editable: true, hidden: true, width: 80},
            {name: 'state', label: 'state', editable: true, hidden: true, width: 80},
            {
                name: 'state1', label: '状态', editable: true, width: 80,
                formatter: function (value, grid, rows) {
                    switch (rows.state) {
                        case -1 :
                            return "<span style='color:red'>审核驳回</span>";
                        case 0 :
                            return "<span style=''>已保存</span>";
                        case 1 :
                            return "<span style=''>已完成</span>";
                        case 2 :
                            return "<span style='color:red'>确认开票</span>";
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
                        case 11 :
                            return "<span style='color:red'>开票</span>";
                        case 12 :
                            return "<span style='color:red'>财务部长审核</span>";
                    }
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 180,
                formatter: function (value, grid, rows) {

                    var html = "";
                    // if (rows.isOwner && rows.applyId==user.id ) {//审批通过的链接有问题，先注释
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='view(" + rows.id + ",3)'>审批&nbsp;&nbsp;</a>";
                    // }
                    if (rows.taskId != null && rows.taskId !='') {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='businessObj.showHistory(" + rows.id + ")'>&nbsp;审核详情&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='businessObj.view(" + rows.id + ",0)'>&nbsp;编辑&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='businessObj.del(" + rows.id + ")'>&nbsp;删除&nbsp;</a>";
                    }
                    if (hasRoleCWBZ() && rows.state == 2) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='businessObj.view(" + rows.id + ",4)'>&nbsp;确认开票&nbsp;</a>";
                    }
                    if (hasRoleCWZL() && rows.state == 11 && rows.acceptWorker==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='businessObj.view(" + rows.id + ",5)'>&nbsp;开票&nbsp;</a>";
                        // html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='CWReject(" + rows.id + ")'>&nbsp;助理撤回&nbsp;</a>";
                    }
                    if ((rows.state > 3 && rows.state < 13) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='businessObj.returnBack(" + "\"" + rows.taskId + "\"," + rows.itemId + ")'>&nbsp;撤回&nbsp;</a>";
                    }
                    // if (rows.state == 1) {
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='CWReturn(" + rows.id + ")'>财务撤回&nbsp;&nbsp;</a>";
                    // }
                    // if (hasRoleCWKJ() && (rows.state == 2 || rows.state == 11)) {
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='CWReject(" + rows.id + ")'>&nbsp;助理撤回&nbsp;</a>";
                    // }
                    return html;
                }
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
            // var rowData = jQuery("#query_table_logs").jqGrid("getRowData", rowid);
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            businessObj.view(rowid, 1);
        },
    });
    resize("#query_pager_logs");
    $("#queryForm select[name='stateQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#queryForm select[name='invoiceTypeQc']").change(function () {
        $("#querySearch").trigger("click");
    });

// 重新载入数据；
    function reloadTaskData() {
        $("#query_table_logs").reloadCurrentData(baseUrl + "/invoice/listPg", $("#queryForm").serializeJson(), "json", null, function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
        });
    }

    if(hasRoleCWBZ()){
        $("#exportBtn").show();
    }else{
        $("#exportBtn").hide();
    }

    $("#select_cust_table_logs").jqGrid({
        url: '/crm/company/listCustForYW',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#firstForm").serializeJson(), //发送数据
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
        beforeSelectRow: function () {
            $("#select_cust_table_logs").jqGrid('resetSelection');
            return (true);
        },
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
            {
                name: 'companyId',
                index: 'companyId',
                label: 'companyId',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'companyName',
                index: 'companyName',
                label: '客户公司名称',
                editable: false,
                width: 140,
                align: "center",
                sortable: true,
                sorttype: "string"
            },
            {
                name: 'type',
                index: 'type',
                label: 'type',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'typeText',
                index: 'typeText',
                label: '客户类型',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (rowdata.type == 1) {
                        return "<span style='color:#1ab394'>企业客户</span>";
                    } else if (rowdata.type == 0) {
                        return "<span>个人客户</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'standardize',
                index: 'standardize',
                label: 'standardize',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'standardizeText',
                index: 'standardizeText',
                label: '是否标准',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (rowdata.standardize == 1) {
                        return "<span style='color:#1ab394'>标准</span>";
                    } else if(rowdata.standardize == 0) {
                        return "<span class='text-red'>非标准</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'custId',
                index: 'custId',
                label: '对接人id',
                editable: false,
                width: 35,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true,
            },
            {
                name: 'custName',
                index: 'custName',
                label: '对接人名字',
                editable: false,
                width: 60,
                align: "center",
                sortable: true
            },
            {
                name: 'normalize',
                index: 'normalize',
                label: 'normalize',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'normalizeText',
                index: 'normalizeText',
                label: '是否规范',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (rowdata.normalize == 1) {
                        return "<span style='color:#1ab394'>规范</span>";
                    } else if(rowdata.normalize == 0) {
                        return "<span class='text-red'>非规范</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'protectStrong',
                index: 'protectStrong',
                label: '强弱保护',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "<span style='color:#1ab394'>强保护</span>";
                    } else {
                        return "弱保护";
                    }
                }
            },
            {
                name: 'protectLevel',
                index: 'protectLevel',
                label: '保护等级',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 3) {
                        return "<span style='color:#1ab394'>A类保护</span>";
                    } else if (a == 2) {
                        return "<span style='color:#1ab394'>B类保护</span>";
                    } else if (a == 1) {
                        return "<span style='color:#1ab394'>C类保护</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'startTime',
                index: 'startTime',
                label: '开始时间',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'endTime',
                index: 'endTime',
                label: '结束时间',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'state',
                label: 'state',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'stateText',
                label: '状态',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    if (rowdata.state == 1){
                        return "<span style='color:#1ab394'>有效</span>";
                    }else{
                        return "<span style='color:red'>失效</span>"
                    }
                }
            },
            {
                name: 'operate',
                label: '操作',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.state == 1) {
                        html += "<button class='btn btn-xs btn-primary btn-outline' type='button' title='修改公司名' onclick='companyObj.editCompanyBasic(" + rowdata.custId + ");'>修改公司名</button>";
                    }
                    if(rowdata.standardize != 1  && rowdata.type == 1 ){
                        html += "&nbsp;&nbsp;";
                        var url = "/crm/queryStandardizedCompany?flag=3&companyName="+rowdata.companyName;
                        html += "<button class='btn btn-xs btn-default btn-outline' title='申请为标准公司' onclick=\"page('"+url+"','申请为标准公司');\">申请为标准公司</button>";
                    }
                    return html;
                }
            }
        ],
        pager: "#select_cust_pager_logs",
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        loadComplete: function () {
            $("#cb_select_cust_table_logs").hide();
        },
    });

    $("#selected_article_table_logs").jqGrid({
        url: '/invoice/listPgForSelectedArticle',
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
            {name: 'companyName', label: '客户公司名称', editable: true, width: 120},
            {name: 'custName', label: '客户联系人', editable: true, width: 60},
            {name: 'brand', label: '品牌', editable: true, width: 60},
            {name: 'userName', label: '业务员', editable: true, width: 60},
            // {name: 'supplierName', label: '供应商名称', editable: true, width: 120},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 60},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 120},
/*            {name: 'innerOuter', label: '内外部', editable: true, width: 80},
            {name: 'channel', label: '频道', editable: true, width: 80},*/
            {name: 'mediaUserName', label: '媒介', editable: true, width: 60},
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
            {name: 'num', label: '数量', editable: true, width: 60},
            {
                name: 'issuedDate', label: '发布日期', editable: true, width: 120,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'saleAmount', label: '应收（报价）', editable: true, width: 80},
            {
                name: 'promiseDate', label: '答应到款日期', editable: true, width: 120,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'incomeAmount', label: '回款金额', editable: true, width: 80},
            {
                name: 'incomeDate', label: '回款日期', editable: true, width: 120,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'outgoAmount', label: '成本（请款）', editable: true, width: 80},
            {name: 'taxes', label: '税额', editable: true, width: 80},
            {
                name: 'state', label: '退稿状态', editable: true, width: 60, formatter: function (d) {
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

    $("#select_article_table_logs").jqGrid({
        url: '/invoice/listPgForSelectArticle',
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
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            // {name: 'no', label: '订单编号', editable: true, width: 120},
            {name: 'companyName', label: '客户公司名称', editable: true, width: 100},
            {name: 'custName', label: '客户联系人', editable: true, width: 60},
            {name: 'brand', label: '品牌', editable: true, width: 60},
            {name: 'userName', label: '业务员', editable: true, width: 60},
            // {name: 'supplierName', label: '供应商名称', editable: true, width: 120},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 60},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 100},
/*            {name: 'innerOuter', label: '内外部', editable: true, width: 80},
            {name: 'channel', label: '频道', editable: true, width: 80},*/
            {name: 'mediaUserName', label: '媒介', editable: true, width: 60},
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
            {name: 'num', label: '数量', editable: true, width: 60},
            {name: 'issuedDate', label: '发布日期', editable: true, width: 80},
            {name: 'saleAmount', label: '应收（报价）', editable: true, width: 80},
            {name: 'incomeAmount', label: '回款金额', editable: true, width: 80},
            {name: 'outgoAmount', label: '成本（请款）', editable: true, width: 80}
            // {name: 'bankNameDetail', label: '开户行支行', editable: true, width: 180},
            // {name: 'owner', label: '联系人', editable: true, width: 120},
        ],
        pager: $("#select_article_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        gridComplete: function () {
            if(arrayList.length>0){//保留选中
                for (var i = 0; i < arrayList.length; i++) {
                    $(this).jqGrid('setSelection',arrayList[i]);
                }
            }
        },
        loadComplete: function () {
            $("#jqgh_select_article_table_logs_cb").css("padding-right", "6px");
        },
        onSelectAll: function (aRowids, status) {
            if (status == true) {
                //循环aRowids数组，将code放入arrayList数组中
                $.each(aRowids, function (i, item) {
                    //已选中的先排除
                    if (!(arrayList.indexOf(item) > -1)) {
                        gridObj.saveData(item);
                    }
                })
            } else {
                //循环aRowids数组，将code从arrayList中删除
                $.each(aRowids, function (i, item) {
                    gridObj.deleteIndexData(item);
                })
            }
            $("#saleSum2").text(saleSum.toFixed(2));
            $("#incomeSum2").text(incomeSum.toFixed(2));
            $("#outgoSum2").text(outgoSum.toFixed(2));
        },
        onSelectRow: function (rowid, status) {
            if (status == true) {
                if (!(arrayList.indexOf(rowid) > -1)) {
                    gridObj.saveData(rowid);
                }
            } else {
                gridObj.deleteIndexData(rowid);
            }
            $("#saleSum2").text(saleSum.toFixed(2));
            $("#incomeSum2").text(incomeSum.toFixed(2));
            $("#outgoSum2").text(outgoSum.toFixed(2));
        }
    });

    $("#invoiceAmount").off('input propertychange').on('input propertychange', function () {
        $("select").trigger("change");
    });
});

var gridObj = {
    saveData: function(obj){
        arrayList.push(obj);
        var rowData = $("#select_article_table_logs").jqGrid('getRowData', obj);
        saleSum += parseFloat(rowData.saleAmount);
        incomeSum += parseFloat(rowData.incomeAmount);
        outgoSum += parseFloat(rowData.outgoAmount);
    },
    deleteIndexData: function(obj){
        //获取obj在arrayList数组中的索引值
        for (var i = 0; i < arrayList.length; i++) {
            $("#row" + obj).remove();
            if (arrayList[i] == obj) {
                //根据索引值删除arrayList中的数据
                var rowData = $("#select_article_table_logs").jqGrid('getRowData', obj);
                saleSum = saleSum - parseFloat(rowData.saleAmount);
                incomeSum = incomeSum - parseFloat(rowData.incomeAmount);
                outgoSum = outgoSum - parseFloat(rowData.outgoAmount);
                arrayList.splice(i, 1);
            }
        }
    },
    reloadTable: function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    },
    reloadCust: function () {
        $("#select_cust_table_logs").emptyGridParam();
        $("#select_cust_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#firstForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#select_cust_table_logs");
    },
    reloadSelectArticle: function () {
        $("#select_article_table_logs").emptyGridParam() ;
        $("#select_article_table_logs").jqGrid('setGridParam', {
            datatype:'json',
            postData: $("#secondForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //
        resize("#select_article_table_logs");
    },
    reloadSelectedArticle: function () {
        $("#selected_article_table_logs").emptyGridParam() ;
        $("#selected_article_table_logs").jqGrid('setGridParam', {
            datatype:'json',
            postData: $("#selectedForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //
        resize("#selected_article_table_logs");
    }
};
//审核通过
function approve(t) {
    approveTask($("#taskId").val(), 1, t.id,$("#desc").val())
}

//审核驳回
function reject(t) {
    approveTask($("#taskId").val(), 0, t.id,$("#desc").val())
}

// 提交审核意见；
function completeApprove(flag) {
    $("#userId").val($("#financialAssistant").find("option:selected").val());
    $("#userName").val($("#financialAssistant").find("option:selected").text());
    $("#userDept").val($("#financialAssistant").find("option:selected").attr("data-id"));
    if($("#userId").val()== null ||$("#userId").val()==""){
        swal("请选择财务助理开票人!");
        return;
    }
    if ($("#editForm").valid()) {
        var opinion = false;
        var elementId = "rejectEmploy";
        if (flag == 0) {
            elementId = "agreeEmploy";
            opinion = true;
        }
        startModal("#" + elementId);
        $.post(baseUrl + "/taxUser/completeApprove", {taskIds:$("#taskId").val(),desc:$("#desc").val(),agree:opinion,userId:$("#userId").val(),userName:$("#userName").val(),deptId:$("#userDept").val()},
            function (data) {
            Ladda.stopAll();
            var message = data.data.message;
            if (message == null) {
                commonObj.alertMessage(data.msg);
            } else {
                commonObj.alertMessage(message);
            }
            $("#editModal").modal("hide");
            // 返回审核页面；
            setTimeout(function () {
                window.location.href = '/process/queryTask';
            }, 1000);
        }, "json").error(function () {
            Ladda.stopAll();//隐藏加载按钮
        });
    }
}

var companyObj = {
    companySearchUrl: "/company/companySearch",
    checkCompanyUrl: "/company/checkCompany",
    currentCompanyName: "",
    regexStr: /^[1]([3-9])[0-9]{9}$/,
    firstPageTotal: 0, //第一页查询缓存表数据总数
    renderCompanyItem: function (page, pageSize, companyList) {
        var html = "";
        if (companyList && companyList.length > 0) {
            $.each(companyList, function (m, company) {
                html += "<div onmousedown='companyObj.chooseCompany(this);' class=\"companyNameItem\" title=\"" + (company.companyName || "") + "\"><span>" + (company.companyName || "") + "</span></div>";
            });
        }
        return html;
    },
    search: function (t) {
        $(t).closest("form").find(".companyWrap").html("");
        var keyword = $(t).closest("form").find("input[name='companyName']").val();
        if (!keyword) {
            if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
                $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
            }
            return;
        } else {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
        }
        layui.use('flow', function () {
            var flow = layui.flow;
            flow.load({
                elem: $(t).closest("form").find(".companyWrap"),
                isAuto: false,
                done: function (page, next) {
                    //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                    //请求数据
                    var param = {keyword: keyword};
                    param.page = page; //页码
                    param.size = 20; //每页数据条数
                    requestData(param, companyObj.companySearchUrl, "post", function (data) {
                        //第一页是从缓存表拿数据，记录数据总数
                        if (page == 1) {
                            companyObj.firstPageTotal = data.total;
                        }
                        next(companyObj.renderCompanyItem(page, param.size, data.list), page < data.pages); //如果小于总页数，则继续
                    });
                }
            });
        });
    },
    enterEvent: function (t, event) {
        if ((event.keyCode == '13' || event.keyCode == 13)) {
            companyObj.search(t);
        }
    },
    checkCompany: function (t) {
        var keyword = $(t).closest("form").find("input[name='companyName']").val();
        if (!keyword) {
            $(t).closest("form").find(".companyTipsYes").hide();
            $(t).closest("form").find(".companyTipsNo").hide();
            $(t).closest("form").find("input[name='standardize']").val(0);
            return;
        }
        //如果是个体工商户，不需要校验
        if ($(t).closest("form").find("input[name='custProperty']").val() == 1) {
            requestData({keyword: keyword}, companyObj.checkCompanyUrl, "post", function (data) {
                if (data.code == 200) {
                    $(t).closest("form").find(".companyTipsYes").show();
                    $(t).closest("form").find(".companyTipsNo").hide();
                    $(t).closest("form").find("input[name='standardize']").val(1);
                } else {
                    $(t).closest("form").find(".companyTipsYes").hide();
                    $(t).closest("form").find(".companyTipsNo").show();
                    $(t).closest("form").find("input[name='standardize']").val(0);
                }
            });
        }
        // if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
        //     $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        // }
    },
    chooseCompany: function (t) {
        $(t).closest("form").find("input[name='companyName']").val($(t).attr("title") || "");
        //隐藏弹出筛选框
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOut: function (t) {
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOver: function (t) {
        //如果有内容则展示
        if ($(t).closest("form").find(".companyWrap").find("div").length > 0) {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
            $(t).closest("form").find("input[name='companyName']").focus();
        }
    },
    //验证电话号码格式
    regexPhone: function (t) {
        if (!(companyObj.regexStr.test($(t).val()))) {
            $("#normalize").val(0);
            $("#companyUserTipsYes").hide();
            $("#companyUserTipsNo").show();
        } else {
            $("#normalize").val(1);
            $("#companyUserTipsYes").show();
            $("#companyUserTipsNo").hide();
        }
    },

    dropup: function (t) {
        $(t).hide();
        $(t).parent().find(".glyphicon-chevron-down").parent().show();
        $(t).parent().nextAll().hide();
    },

    dropdown: function (t) {
        $(t).hide();
        $(t).parent().find(".glyphicon-chevron-up").parent().show();
        $(t).parent().nextAll().show();
    },

    natureClick: function (t, val, companyName) {
        $(t).closest("div").find("button").each(function (i, btn) {
            if ($(btn).attr("id") === $(t).attr("id")) {
                if ($(btn).hasClass("btn-white")) {
                    $(btn).removeClass("btn-white");
                }
                if (!$(btn).hasClass("btn-info")) {
                    $(btn).addClass("btn-info");
                }
            } else {
                if ($(btn).hasClass("btn-info")) {
                    $(btn).removeClass("btn-info");
                }
                if (!$(btn).hasClass("btn-white")) {
                    $(btn).addClass("btn-white");
                }
            }
        });
        $("#custProperty").val(val);
        $("#addCompanyForm [name='companyName']").val(companyName);
        if (val == 1) {
            $("#addCompanyForm [name='companyName']").removeAttr("disabled");
            if (companyName != null && companyName != "" && companyName != undefined) {
                requestData({keyword: $("#addCompanyForm [name='companyName']").val()}, companyObj.checkCompanyUrl, "post", function (data) {
                    if (data.code == 200) {
                        $("#companyTipsYes").show();
                        $("#companyTipsNo").hide();
                        $("#standardize").val("1");
                    } else {
                        $("#companyTipsYes").hide();
                        $("#companyTipsNo").show();
                        $("#standardize").val("0");
                    }
                });
            } else {
                $("#companyTipsYes").hide();
                $("#companyTipsNo").hide();
            }
        } else {
            $("#standardize").val("0");//个人公司都是非标准
            $("#addCompanyForm [name='companyName']").attr("disabled", "disabled");
            $("#companyTipsYes").hide();
            $("#companyTipsNo").show();
        }
    },

    editCompanyBasic: function (companyUserId) {
        $.ajax({
            type: "get",
            url: "/crm/company/getBasicById",
            data: {id: companyUserId},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    if (data.data.entity != null) {
                        $("#editModal").modal("hide");
                        $("body").addClass("modal-open");
                        $("#editCompanyModal").modal({backdrop: "static"});
                        if (data.data.entity['auditFlag'] == 1) {
                            swal("该客户保护审核中，无法修改客户信息，请等待审核完成后重试！");
                            return;
                        }
                        document.getElementById("addCompanyForm").reset();
                        $("#addCompanyForm").find("input").removeClass('error');//清除验证标签
                        $("#addCompanyForm").validate().resetForm();

                        for (var attr in data.data.entity) {
                            $("#addCompanyForm [name='" + attr + "']").val(data.data.entity[attr]);
                        }
                        var custProperty = data.data.entity['custProperty'];
                        if (custProperty == 1) {//企业客户
                            companyObj.natureClick($("#enterpriseBtn"), custProperty, data.data.entity['companyName']);
                            $("#addCompanyForm").find(".btn").each(function (index, item) {
                                $(item).attr("disabled", "disabled");
                            });
                            if (data.data.entity['standardize'] == 1) {
                                if (user.dept.code == 'YW') {
                                    $("#addCompanyForm [name='companyName']").attr("disabled", "disabled");
                                }
                                $("#companyTipsYes").show();
                                $("#companyTipsNo").hide();
                            } else {
                                $("#addCompanyForm [name='companyName']").removeAttr("disabled");
                                $("#companyTipsYes").hide();
                                $("#companyTipsNo").show();
                            }
                        } else {//个人客户
                            // $("#companyTipsYes").hide();
                            // $("#companyTipsNo").show();
                            $("#addCompanyForm").find(".btn").each(function (index, item) {
                                $(item).removeAttr("disabled");
                            });
                            companyObj.natureClick($("#personalBtn"), custProperty, data.data.entity['companyName']);
                        }

                        if (data.data.entity['normalize'] == 1 || companyObj.regexStr.test(data.data.entity['mobile'])) {
                            if (user.dept.code == 'YW') {
                                $("#addCompanyForm [name='mobile']").attr("readonly", "readonly");
                            }
                            $("#companyUserTipsYes").show();
                            $("#companyUserTipsNo").hide();
                        } else {
                            if (user.dept.code == 'YW') {
                                $("#addCompanyForm [name='mobile']").removeAttr("readonly");
                            }
                            $("#companyUserTipsYes").hide();
                            $("#companyUserTipsNo").show();
                        }
                    }
                }
            }
        })
    },

    saveCommon: function (t) {
        var message = "企业客户公司名称：";
        var custProperty = $("#addCompanyForm [name='custProperty']").val();
        if (custProperty == 1) {
            requestData({keyword: $("#addCompanyForm [name='companyName']").val()}, companyObj.checkCompanyUrl, "post", function (data) {
                if (data.code == 200) {
                    $("#companyTipsYes").show();
                    $("#companyTipsNo").hide();
                    $("#standardize").val("1");
                    message += "<strong>标准</strong>；";
                } else {
                    $("#companyTipsYes").hide();
                    $("#companyTipsNo").show();
                    $("#standardize").val("0");
                    message += "<strong class='text-red'>非标准</strong>；";
                }
            });
        } else {
            message += "您选择的修改的是个人客户，个人客户公司名称统一为个体工商户且公司名称非标准；";
        }
        message += "手机号码：";
        var mobile = $("#addCompanyForm [name='mobile']").val();
        if (!(companyObj.regexStr.test(mobile))) {
            $("#normalize").val("0");
            $("#companyUserTipsYes").hide();
            $("#companyUserTipsNo").show();
            message += "<strong class='text-red'>非规范</strong>";
        } else {
            $("#normalize").val("1");
            $("#companyUserTipsYes").show();
            $("#companyUserTipsNo").hide();
            message += "<strong>规范</strong>！";
        }
        return message;
    },
    updateCompanyBasic: function (t, url) {
        if ($("#addCompanyForm [name='custProperty']").val() == 0) {
            $("#addCompanyForm [name='companyName']").val("个体工商户")
        } else {
            if ($("#addCompanyForm [name='companyName']").val() == "个体工商户") {
                swal("企业客户公司名称不能是个体工商户，请修改后重试！");
                return;
            }
        }
        if ($("#addCompanyForm").valid()) {
            var message = companyObj.saveCommon(t);
            var lock = true;
            var formData = $("#addCompanyForm").serializeJson();
            layer.confirm(message, {
                title: "请确认信息是否填写正确！",
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        url: url,
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                $("#editCompanyModal").modal("hide");
                                swal("操作成功！");
                                gridObj.reloadCust();
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },
};