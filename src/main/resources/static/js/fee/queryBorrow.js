function del(id) {
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
                url: baseUrl + "/borrow/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
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

//state=2用这个撤回
function CWReject(t) {
    var lock = true ;
    var id = $("#editForm #id").val();
    layer.confirm('确认撤回？', {
        btn: ['撤回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#confirm") ;
        startModal("#CWReject2") ;
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/borrow/CWReject",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll() ;
                    if (data.code == 200) {
                        $("#editModal").modal("hide");
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message);
                        approveTask($("#taskId").val(), 0,t.id,$("#desc").val());
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
};

//state=1||state=12用这个撤回，
function CWReturn(t) {
    var lock = true ;
    var id = $("#editForm #id").val();
    layer.confirm('确认撤回？', {
        btn: ['撤回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#review") ;
        startModal("#rejected") ;
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/borrow/CWReturn",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        swal(data.data.message);
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message);
                        approveTask($("#taskId").val(), 0,t.id,$("#desc").val());
                    } else if(data.code == 1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    }
                    else {
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

//审核记录查看
function showHistory(id) {
    //process详见IProcess
    $("#historyModal").modal('toggle');
    $.ajax({
        type: "post",
        url: baseUrl + "/process/history",
        data: {dataId: id, process: 2},
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
                    html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=2&t=" + new Date().getTime() + "' style='width: 121%; margin-left: -42px; margin-top: -28px;margin-bottom: -100px;'/></div>";
                    $("#history").append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}

//审核记录查看
function repayDetail(id) {
    $("#detailModal").modal('toggle');
    $.ajax({
        type: "post",
        url: baseUrl + "/borrow/queryRepayByBorrowId",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                $("#detail").empty();
                if (data.data.list != null) {
                    var html = "";
                    var params = [] ;
                    html += "<div class='form-control'>" +
                        "<div class='col-sm-2 text-center'>还款编号</div>" +
                        // "<div class='col-sm-1 text-center'>类型</div>" +
                        "<div class='col-sm-2 text-center'>还款金额</div>" +
                        "<div class='col-sm-2 text-center'>状态</div>" +
                        "<div class='col-sm-2 text-center'>备注</div>" +
                        "<div class='col-sm-2 text-center'>还款时间</div>" +
                        "<div class='col-sm-2 text-center'>还款人</div>" +
                        "</div>";
                    for (var i = 0; i < data.data.list.length; i++) {
                        var param = [] ;
                        var state = data.data.list[i].state == 0 ? '<span style="color:red">进行中</span>' : '<span>已完成</span>' ;
                        param.push(data.data.list[i].repayCode);
                        param.push(data.data.list[i].amount);
                        param.push(data.data.list[i].state == 0 ? '进行中' : '已完成');
                        param.push(data.data.list[i].remark);
                        param.push(data.data.list[i].createTime.substring(0,10));
                        param.push(data.data.list[i].createName);
                        params.push(param) ;
                        if(i<10){
                            html += "<div class='form-control'>" +
                                "<div class='col-sm-2 text-center'>" + data.data.list[i].repayCode + "</div>" +
                                // "<div class='col-sm-1 text-center'>" + data.data.list[i].type + "</div>" +
                                "<div class='col-sm-2 text-center'>" + data.data.list[i].amount + "</div>" +
                                "<div class='col-sm-2 text-center'>" + state + "</div>" +
                                "<div class='col-sm-2 text-center'>" + data.data.list[i].remark + "</div>" +
                                "<div class='col-sm-2 text-center'>" + data.data.list[i].createTime.substring(0,10)+ "</div>" +
                                "<div class='col-sm-2 text-center'>" + data.data.list[i].createName + "</div>" +
                                "</div>";
                        }
                    }
                    if(i>9){
                        html += "<div class='form-control'><div class='col-sm-12 text-center'>更多详情请点击上方还款明细下载</div></div>" ;
                    }
                    $("#detail").append(html);
                    $("#exportDetail").off("click").on("click",function () {
                        var jsonData = JSON.stringify(params) ;
                        //列标题，逗号隔开，每一个逗号就是隔开一个单元格
                        var headStr = data.data.entity.code+data.data.entity.title + '\n';
                        headStr += '还款编号,还款金额,状态,备注,还款时间,还款人\n';
                        //增加\t为了不让表格显示科学计数法或者其他格式
                        for(var i = 0 ; i < params.length ; i++ ) {
                            for(var j = 0 ; j < 6 ; j++ ){
                                var str = params[i][j];
                                // headStr += 'str,';
                                // headStr += ""+str + '\t' +",";
                                if(j==1){
                                    headStr += str +",";
                                }else{
                                    headStr += str + '\t' +",";
                                }

                                // if(j == 1){
                                //     headStr += "${params[i][j]},";
                                // }else {
                                //     headStr += "${params[i][j] + '\t'},";
                                // }
                            }
                            headStr += '\n';
                        }
                            //encodeURIComponent解决中文乱码
                        var uri = 'data:text/csv;charset=utf-8,\ufeff' + encodeURIComponent(headStr);
                        //通过创建a标签实现
                        var link = document.createElement("a");
                        link.href = uri;
                        //对下载的文件命名
                        link.download =  "还款详情"+new Date().getTime()+".xls";
                        document.body.appendChild(link);
                        link.click();
                        document.body.removeChild(link);
                    })
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}

//flag=1审核页面，flag=-1查看页面,flag=0稿件管理跳转到查看页面,flag=2对公账户财务负责人复核出款，flag=3出纳出款
/**
 * flag 的值对应的页面
 * 0 编辑页面
 * 1 双击查看页面
 * 2 其他页面跳转的查看页面
 * 3 审核页面
 * 4 出纳出款
 * 5 复核出款
 */
function view(id, flag) {
    $("#editModal").modal({backdrop: "static"});
    document.getElementById("editForm").reset();
    $("#editForm").find("input").removeClass('error');
    $("#editForm").validate().resetForm();
    $("#editModal input:radio").removeAttr("checked");
    $("#editModal input:radio").parent().removeClass("checked");
    if(flag == 0){//编辑
        $("#editFooter").show();
        $("#viewFooter").hide();
        $("#confirmFooter").hide();
        $(".licence").hide();
        $(".licence").find("input").each(function () {
        });
        $("#selectAccountBtnDiv").show();
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-2");
        $(".hideOnEdit").hide();
        $(".showOnEdit").show();
        $("#editModal .modal-title").html("更改借款信息");
        $(".save").hide();
        $(".update").show();
    }else{
        $("#editFooter").hide();
        $("#selectAccountBtnDiv").hide();
        $("#editForm .changeOnView").removeClass("col-sm-2");
        $("#editForm .changeOnView").addClass("col-sm-3");
        $(".hideOnEdit").show();
        $(".showOnEdit").hide();
        if(flag == 4){//出款
            $("#viewFooter").hide();
            $("#confirmFooter").show();
            $("#changeAccountBtn").hide();
        }else{
            if(flag == 1){//查看
                $(".licence").hide();
                $(".licence").find("input").each(function () {
                });
                var returnType= getQueryString("returnType");
                if(returnType == 5){
                    $("#auditTrue").show();
                    $("#showHistory1").hide();
                    $("#reject1").hide();
                    $("#pass1").hide();
                    $("#confirmFooter").hide();
                    $("#auditFalse").hide();
                }else {
                    $("#viewFooter").show();
                    $("#auditTrue").hide();
                    $("#auditFalse").show();
                    $("#closeModal").show();//查看页面返回按钮隐藏
                    $("#goback").hide();
                    $("#review").hide();
                    $("#confirmFooter").hide();
                    $("#rejected").hide();
                }

            }else if (flag == -1) {
                $("#viewFooter").show();
                $("#auditTrue").show();
                $("#reject1").hide();
                $("#pass1").hide();
                $("#auditFalse").hide();
                $("#closeModal").hide();
                $("#review").hide();
                $("#goback").hide();
                $("#confirmFooter").hide();
                $("#showHistory1").hide();
            }

            else if(flag == 2){//跳转的查看

                $("#viewFooter").show();
                $("#auditTrue").hide();
                $("#auditFalse").show();
                $("#closeModal").hide();
                $("#review").hide();
                $("#goback").show();
                $("#confirmFooter").hide();
            }else if(flag == 3){//审批查看
                $(".licence").show();
                $(".licence").find("input").each(function () {
                });
                $("#viewFooter").show();
                $("#auditTrue").show();
                $("#auditFalse").hide()
                $("#showHistory1").data("id",id);
                $("#confirmFooter").hide();
            }else if(flag == 5){//确认公账出款
                $("#viewFooter").show();
                $("#auditTrue").hide();
                $("#auditFalse").show();
                $("#goback").hide();
                $("#closeModal").show();
                $("#review").show();
                $("#confirmFooter").hide();
            }else if (flag == 6){
                $("#auditTrue").hide();
                $("#CWReject2").hide();
                $("#viewFooter").hide();
                $("#confirm").hide();
                $(".licence").hide();

            }
        }
    }

    $.ajax({
        type: "post",
        url: baseUrl + "/borrow/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            $("#affixDiv").empty();
            if(data.code==200){
                for (var attr in data.data.entity) {
                    //借款查看精确赋值，影响导出数据
                    $("#editForm [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                    if(flag == 0){
                        if(attr == "expertPayTime"){
                            $("#editForm [name=" + attr + "]").attr("readonly","readonly");
                        }
                        $("#editForm [name=" + attr + "]").removeAttrs("style");
                    }else{
                        $("#editForm [name=" + attr + "]").removeAttrs("readonly");
                        $("#editForm [name=" + attr + "]").attr("style", "border:0;");
                    }
                    if (attr == "type") {
                        if(flag == 0){
                            $("#editForm input[name='type'][value='" + data.data.entity[attr] + "']").iCheck("check");
                        }else{
                            $("#editForm input[name='type']").attr("disabled", "disabled");
                            $("#editForm input[name='type'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                            $("#editForm input[name='type'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                        }
                    }
                    if(attr=="affixName"){
                        $("#affixLink").empty();
                        $("#affixLink").show();
                        if(data.data.entity[attr] === "") continue;
                        var affixName = data.data.entity[attr].split(',');
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
                                stateStr = "会计确认";
                                break;
                            case 10 :
                                stateStr = "业务员确认";
                                break;
                            case 12 :
                                stateStr = "财务部长审核";
                                break;
                            case 16 :
                                stateStr = "出纳出款";
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
                        if (dataStr != null) {
                            $("#editForm [name='state1']").val(stateStr);
                            $("#editForm [name='state1']").prop("style", "border:0;");
                        }
                    }
                }
                if(flag==4 && hasRoleCWCN() && (data.data.entity['state'] == 2 || data.data.entity['state'] == 16)||(data.data.entity['state'] == 1&&flag==6&&hasRoleCWBZ())){
                    $(".viewAndConfirm").show();
                    $(".showOnView").hide();
                    $(".showOnConfirm").show();
                    if ($("#payAmount").val() == 0) {
                        var applyAmount = $("#applyAmount").val() == null ? 0 : $("#applyAmount").val() ;
                        $("#payAmount").val(parseFloat(applyAmount));
                    }
                    $("#editForm #payTime").removeProp("style");
                    $("#editForm #payTime").attr("readonly","readonly");
                    $("#editForm #payAmount").attr("readonly","readonly");
                    if(data.data.entity['outAccountId']){
                        $("#editForm #outAccountSelect").val(data.data.entity['outAccountId']);
                        $("#editForm #outAccountIds").val(data.data.entity['outAccountId']);
                        layui.use(["form"], function () {
                            layui.form.render('select');
                        });
                    }
                }else if (data.data.entity['state'] == 1 || data.data.entity['state'] == 12 || data.data.entity['state'] == 9) {//已出款，直接显示出款文本
                    $(".viewAndConfirm").show();
                    $(".showOnView").show();
                    $(".showOnConfirm").hide();
                } else {//未出款，不显示出款账户信息
                    $(".viewAndConfirm").hide();
                }
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
}

//初始化业务部门
function getDept(){
    var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
    var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
    var deptDiv = document.getElementById("deptDiv");
    //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务  媒介部门
    if((currentDeptQx || currentCompanyQx || isZC()) || user.dept.code == 'CW'){
        deptDiv.style.display = 'block';
        $("#selDept").click(function () {
            $("#deptModal").modal('toggle');
        });
        $('#treeview').treeview({
            data: [getTreeData(isZC())],
            onNodeSelected: function (event, data) {
                $("#companyCode").val("");//每次选择时，先清空
                $("#deptId1").val("");//每次选择时，先清空
                $("#chooseDeptName").val(data.text);
                $("#deptModal").modal('hide');
                $("#deptId1").val(data.id);
                $("#userType").val(data.code);
            }
        });
        $("#cleanDept").click(function () {
            $("#userType").val("");
            $("#companyCode").val("");//清空
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
    requestData({deptId: deptId},"/dept/listForSonTreeView","POST",function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

//财务负责人确认出款
function checkBtoB() {
    var lock = true ;
    layer.confirm('确认出款？', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#review");//锁定按钮，防止重复提交
        startModal("#rejected");//锁定按钮，防止重复提交
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/borrow/checkBtoB",    //向后端请求数据的url
                data: {id: $("#id").val(),desc:$("#desc").val()},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    $("#deptId1").val("");//出款后会有一个dept值存进去，导致显示的数据不正确，先清空再查询
                    if (data.code == 200) {
                        $("#editModal").modal("hide");
                        swal(data.data.message);
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
                error:function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        }
    }, function () {
        return;
    });
}


function repay(id) {
    $("#repayModal").modal({backdrop:"static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/borrow/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.entity) {
                $("[name=" + attr + "5][type!='radio']").val(data.data.entity[attr]);
            }
        }
    });
}

function repayConfirm(id) {
    $("#repayConfirmModal").modal({backdrop:"static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/borrow/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.entity) {
                $("[name=" + attr + "6][type!='radio']").val(data.data.entity[attr]);
            }
        }
    });
}

$(document).ready(function () {
    //加载打印权限
    downLoadAndPrintObj.loadFlowPrintPermission();

    getDept();
    if(hasRoleCWBZ()){
        $("#exportBtn").show();
    }else{
        $("#exportBtn").hide();
    }
    $("#exportBtn").click(function () {
        var params = removeBlank($("#queryForm").serializeJson());
        location.href = "/borrow/exportBorrow" + "?" + $.param(params);
    });

    var e = "<i class='fa fa-times-circle'></i> ";
    $("#editForm").validate({
        rules: {
            applyAmount: {number: true}
        }, message: {
            applyAmount: {required: e + "请输入正确的借款金额"}
        }
    });
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
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
    reimbursementSum();
    //flag=1审核，否则查看
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        view(getQueryString("id"), getQueryString("flag"));
    }

    if (getQueryString("approveId") != null && getQueryString("approveId") != "" && getQueryString("approveId") != undefined) {
        view(getQueryString("approveId"),4);
    }

    if (getQueryString("repayId") != null && getQueryString("repayId") != "" && getQueryString("repayId") != undefined) {
        repayConfirm(getQueryString("repayId"));
    }

    if (getQueryString("editId") != null && getQueryString("editId") != "" && getQueryString("editId") != undefined) {
        edit(getQueryString("editId"),0);
    }

    if(getQueryString("admId")!=null && getQueryString("admId")!="" && getQueryString("admId")!=undefined){
        add(getQueryString("admId"));
    }
    ;

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/borrow/listPg',
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
            {name: 'code', label: '借款编号', editable: true, width: 100},
            {name: 'title', label: '标题', editable: true, width: 100},
            {name: 'applyName', label: '申请人', editable: true, width: 60},
            {name: 'deptName', label: '所属部门', editable: true, width: 80},
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
            {name: 'accountName', label: '收款账户（人）', editable: true, width: 80},
            {name: 'accountBankNo', label: '收款账号', editable: true, width: 80},
            {name: 'accountBankName', label: '收款开户行', editable: true, width: 80},
            {name: 'applyAmount', label: '申请金额', editable: true, width: 60},
            {name: 'applyTime', label: '申请日期', editable: true, width: 100},
            {name: 'outAccountName', label: '实际出款账户', editable: true, width: 80},
            {name: 'payAmount', label: '实际出款金额', editable: true, width: 60},
            {name: 'payTime', label: '实际出款日期', editable: true, width: 80},
            {name: 'taskId', label: 'taskId', editable: true, hidden: true, width: 80},
            {name: 'itemId', label: 'itemId', editable: true, hidden: true, width: 80},
            {name: 'repayFlag', label: 'repayFlag', editable: true, hidden: true, width: 80},
            {name: 'state', label: 'state', editable: true, hidden: true, width: 80},
            {name: 'repaying', label: 'repaying', editable: true, hidden: true, width: 80},
            {name: 'repayFlag', label: 'repayFlag', editable: true, hidden: true, width: 80},
            // {name: 'remark', label: '借款原因', editable: true, width: 150},
            {
                name: 'state1', label: '借款状态', editable: true, width: 80,
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
            {name: 'repayAmount', label: '已还金额', editable: true, width: 60},
            {name: 'remainAmount', label: '未还金额', editable: true, width: 60},
            {
                name: 'operate', label: "操作", index: '', width: 180,
                formatter: function (value, grid, rows) {
                    var html = "";

                    //如果有打印下载权限，则展示
                    if(downLoadAndPrintObj.dowloadAndPritPermission){
                        html += "<a href='javascript:;' onclick='downLoadAndPrintObj.viewModalShow("+rows.id+");'>预览&nbsp;</a>";
                    }
                    // if (rows.isOwner && rows.applyId==user.id ) {//审批通过的链接有问题，先注释
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='view(" + rows.id + ",3)'>审批&nbsp;&nbsp;</a>";
                    // }
                    if (rows.taskId != null && rows.taskId != '') {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='showHistory(" + rows.id + ")'>审核详情&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",0)'>编辑&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red;'  onclick='del(" + rows.id + ")'>删除&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWCN() && (rows.state == 2 || rows.state == 16) && rows.acceptWorker==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",4)'>出纳出款&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state==3 || rows.state==4
                        || rows.state==5 || rows.state==6
                        || rows.state==7 || rows.state==8
                        || rows.state==10 || rows.state==12) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='returnBack(" + "\"" + rows.taskId + "\"," + rows.itemId + ")'>撤回&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWKJ()&& rows.state==9 && rows.acceptWorker==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",5)'>确认公账出款&nbsp;&nbsp;</a>";
                    }
                    if (rows.state == 1 && rows.repayFlag != 1 && rows.repaying != 1 && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='repay(" + rows.id + ")'>申请还款&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWCN() && (rows.state == 1 && rows.repaying == 1)) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='repayConfirm(" + rows.id + ")'>出纳确认还款</a>";
                    }
                    if (rows.state == 1 ) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='repayDetail(" + rows.id + ")'>还款明细&nbsp;&nbsp;</a>";
                        if(hasRoleCWBZ()){
                            html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ",6)'>更改出款信息&nbsp;&nbsp;</a>";
                        }
                    }
                    return html;
                }
            }
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
        }
    });
    resize("#query_pager_logs");

    $("#queryForm select[name='stateQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#queryForm select[name='typeQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#queryForm select[name='repayFlagQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        reimbursementSum();
    });

    function add(admId){
        $("#editForm").find("input").removeClass("error");
        $("#editForm").find("textarea").removeClass("error");
        $("#editForm").validate().resetForm();
        $("#editForm input[name='type']").removeAttr("checked");
        $("#editForm input[name='type']").parent().removeClass("checked");
        $("#editForm input[name='type']").removeAttr("disabled");
        document.getElementById("editForm").reset();
        $("#affixDiv").empty();
        $("#editModal .modal-title").html("新增借款信息");
        $("#administrativeId").val(admId);
        $("#editModal").modal({backdrop: "static"});
        $("#editFooter").show();
        $("#viewFooter").hide();
        $("#confirmFooter").hide();
        $("#selectAccountBtnDiv").show();
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-3");
        $(".hideOnEdit").hide();
        $(".showOnEdit").show();
        $(".save").show();
        $(".update").hide();
        $(".viewAndConfirm").hide();
        $(".hideOnEdit").hide();
        $("#editForm [name='expertPayTime']").attr("readonly","readonly");
        $("#editForm").find('input').removeAttr("style");
        $("#editForm").find('textarea').removeAttr("style");

    }
    $("#addBtn").click(function () {
        $("#editForm").find("input").removeClass("error");
        $("#editForm").find("textarea").removeClass("error");
        $("#editForm").validate().resetForm();
        $(".licence").hide();
        $(".licence").find("input").each(function () {
        });
        $("#editForm input[name='type']").removeAttr("checked");
        $("#editForm input[name='type']").parent().removeClass("checked");
        $("#editForm input[name='type']").removeAttr("disabled");
        $("#editForm input[name='type']").parent().removeAttr("disabled");
        document.getElementById("editForm").reset();
        $("#taskId").val("");//reset无法将hidden的input值清空，导致查看借款后，接着发起借款出现被查看的借款流程被删除问题
        $("#affixDiv").empty();
        $("#editModal .modal-title").html("新增借款信息");
        $("#editModal").modal({backdrop: "static"});
        $("#editFooter").show();
        $("#viewFooter").hide();
        $("#confirmFooter").hide();
        $("#selectAccountBtnDiv").show();
        $("#editForm .changeOnView").removeClass("col-sm-3");
        $("#editForm .changeOnView").addClass("col-sm-2");
        $(".hideOnEdit").hide();
        $(".showOnEdit").show();
        $(".save").show();
        $(".update").hide();
        $(".viewAndConfirm").hide();
        $(".hideOnEdit").hide();
        $("#editForm [name='expertPayTime']").attr("readonly","readonly");
        $("#editForm").find('input').removeAttr("style");
        $("#editForm").find('textarea').removeAttr("style");
        if(hasRoleMJ()){
            $("#editForm input[name='type'][value='0']").attr("checked","checked");
            $("#editForm input[name='type'][value='0']").parent().addClass("checked");
        }else {
            $("#editForm input[name='type'][value='1']").attr("checked","checked");
            $("#editForm input[name='type'][value='1']").parent().addClass("checked");
            $("#editForm input[name='type'][value='0']").attr("disabled","disabled");
            $("#editForm input[name='type'][value='0']").parent().attr("disabled","disabled");
        }
    });


    // =========================================弹框选择账户信息======================================
    $("#account_table_logs").jqGrid({
        url: baseUrl + '/account/queryIndividualAccount',
        datatype: "json",
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
        beforeSelectRow: beforeSelectRow,
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

    //实现单选
    function beforeSelectRow() {
        $("#account_table_logs").jqGrid('resetSelection');
        return (true);
    }


    $("#accountSearch").click(function () {
        $("#account_table_logs").emptyGridParam();
        $("#account_table_logs").jqGrid('setGridParam', {
            postData: $("#accountForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $("#selAccount").click(function () {
        $("#accountModal").modal({backdrop: "static"});
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
        $("#accountModal").modal('hide');
        document.getElementById("accountForm").reset();
    });

});
//提交申请
function submitHander(t, url, state) {
    var lock = true ;
    if($('#editForm input:radio[name="type"]:checked').val()==undefined){
        swal("借款类型不能为空！") ;
        return ;
    }
    if ($("#editForm").valid()) {
        var tips;
        if (state == 0) {
            $("#state").val(state);
            tips = "确认保存？";
        } else {
            $("#state").val(state);
            tips = "请确认借款信息？提交后不能取消";
        }
        layer.confirm(tips, {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#save1");//锁定按钮，防止重复提交
            startModal("#save2");//锁定按钮，防止重复提交
            startModal("#update1");//锁定按钮，防止重复提交
            startModal("#update2");//锁定按钮，防止重复提交
            //有图片添加传参
            var formData = new FormData($("#editForm")[0]);
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
                        Ladda.stopAll();
                        if (data.code == 200) {
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            var admId = $("#administrativeId").val();
                            if(admId && admId !=0){
                                var addScheme = "/administrative/administrative?type=5&id="+admId;
                                page(addScheme, "新增借款流程");
                                //goBackProcess();
                            }
                            if (state == 4) {
                                swal(data.data.message);
                                $("#editModal").modal('hide');
                            } else {
                                layer.msg(data.data.message);
                                view(data.data.entity.id,0);
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
                    error: function (data) {
                        Ladda.stopAll();
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
//出款
function submitHanderCW(t, url) {
    var lock = true ;
    if ($("#outAccountIds").val() == undefined || $("#outAccountIds").val() == "" || $("#outAccountIds").val() == null) {
        swal("请先选择出款账户！")
        return;
    }
    var param = {
        id: $("#id").val(),
        outAccountIds: $("#outAccountIds").val(),
        payTime: $("#payTime").val(),
        payAmount: $("#payAmount").val(),
        desc:$("#desc").val()

    };
    layer.confirm('请确认借款信息？提交后不能取消！', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#" + t.id);//锁定按钮，防止重复提交
        startModal("#CWReject2");//锁定按钮，防止重复提交
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: url,
                data: param,
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        $("#editModal").modal('hide');
                        swal(data.data.message);
                        $("#deptId1").val("");//出款后会有一个dept值存进去，导致显示的数据不正确，先清空再查询
                        $("#outAccountIds").val("");
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
                error:function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        }
    }, function () {
        return;
    });
}

//还款
function submitHander5(t, url) {
    var lock = true ;
    var payAmount = $("#payAmount5").val();
    var repayAmount = $("#repayAmount5").val();
    var amount = $("#amount5").val();
    if (amount > (payAmount - repayAmount)) {
        swal("还款金额过大！");
        return;
    }
    if ($("#repayForm").valid()) {
        var formData = {
            id: $("#id5").val(),
            amount: $("#amount5").val(),
            repayRemark: $("#repayRemark5").val()
        };
        layer.confirm('确认提交还款申请？提交后不能取消！', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: url,
                    data: formData,
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            $("#repayModal").modal('hide');
                            swal(data.data.message);
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
                    error:function () {
                        Ladda.stopAll();//隐藏加载按钮
                    }
                });
            }
        });
    }
}

//还款确认
function submitHander6(t, url) {
    var lock = true ;
    layer.confirm('请确认还款信息？确认后不能取消！', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#submit6");//锁定按钮，防止重复提交
        startModal("#submit7");//锁定按钮，防止重复提交
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: url,
                data: {
                    id: $("#id6").val(),
                    amount: $("#amount6").val()
                },
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        $("#repayConfirmModal").modal('hide');
                        swal(data.data.message);
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
                error:function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        }
    }, function () {

    });
}

//还款驳回
function submitHander7(t, url) {
    var lock = true ;
    layer.confirm('请确认还款信息？确认后不能取消！', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        startModal("#submit6");//锁定按钮，防止重复提交
        startModal("#submit7");//锁定按钮，防止重复提交
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: url,
                data: { id: $("#id6").val(),amount: $("#amount6").val(), },
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll() ;
                    if (data.code == 200) {
                        $("#repayConfirmModal").modal('hide');
                        swal(data.data.message);
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
                error:function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        }
    }, function () {
        return;
    });
}

//审核通过
function approve(t) {
    approveTask($("#taskId").val(), 1, t.id,$("#desc").val())
}

//审核驳回
function reject(t) {
    approveTask($("#taskId").val(), 0, t.id,$("#desc").val(),function () {
        $("#editModal").modal("hide");
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
        return 1;
    })
}

function reimbursementSum() {
    $.ajax({
        type:"post",
        data:$("#queryForm").serializeJson(),
        url: baseUrl + "/borrow/reimburseSum",
        dataType:"json",
        async:false,
        success:function (resData) {
            if (resData){
                $("#applyAmount1").text(fmtMoneyBringUnit(resData.applyAmount) || 0);
                $("#payAmount1").text(fmtMoneyBringUnit(resData.payAmount) || 0);
                $("#repayAmount1").text(fmtMoneyBringUnit(resData.repayAmount) || 0);
                $("#remainAmount1").text(fmtMoneyBringUnit(resData.remainAmount) || 0);

            } else {
                $("#tj").find(".text-danger").htmleditForm(0);
            }

        }
    })

}
function changeAccount() {
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
                url: baseUrl + "/borrow/changeAccount",
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    if (data.code == 200) {
                        $("#editModal").modal('hide');
                        swal(data.data.message);
                        $("#outAccountIds").val("");
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/borrow/listPg", $("#queryForm").serializeJson(), "json", null, null);
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

//下载打印功能
var downLoadAndPrintObj = {
    dowloadAndPritPermission: false, //打印权限控制
    state:{"-1":"审核驳回","0":"已保存","1":"已完成","2":"审核通过","3":"组长审核","4":"部长审核","5":"总监审核","6":"财务总监审核","7":"副总经理审核",
        "8":"总经理审核","9":"会计确认","10":"业务员确认","12":"财务部长审核","16":"出纳出款","30":"集团财务部门负责人","31":"集团财务分管领导","32":"集团总裁"},
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
            content: $("#borrowViewModal").html(),
            btn: ['下载','打印','取消'],
            area: ['70%', '60%'],
            closeBtn: 0,
            shadeClose: 0,
            resize: false,
            move: '.modalTitle',
            moveOut: true,
            success: function(layero, index){
                downLoadAndPrintObj.downloadMap = {};
                requestData({id: id}, "/borrow/view", "post", function (data) {
                    if(data.code==200){
                        var title = "【借款】"+data.data.entity["code"]+"-"+data.data.entity["applyName"]+"-"+data.data.entity["applyTime"];
                        downLoadAndPrintObj.downloadMap["modalTitle"] = title;
                        $(layero[0]).find("div .modalTitle").text(title);
                        for (var attr in data.data.entity) {
                            //借款类型
                            if(attr == 'type'){
                                $(layero[0]).find("div ."+attr).text(downLoadAndPrintObj.type[data.data.entity[attr]]  || "");
                                downLoadAndPrintObj.downloadMap['typeName'] = downLoadAndPrintObj.type[data.data.entity[attr]]  || "";
                            }else if(attr == "state"){
                                $(layero[0]).find("div ."+attr).text(downLoadAndPrintObj.state[data.data.entity[attr]] || "");
                                downLoadAndPrintObj.downloadMap[attr] = downLoadAndPrintObj.type[data.data.entity[attr]]  || "";
                            }else {
                                $(layero[0]).find("div ."+attr).text(data.data.entity[attr] || "");
                                downLoadAndPrintObj.downloadMap[attr] = data.data.entity[attr] || "";
                            }
                        }
                    }
                });
                requestData({dataId:id, process:2},"/process/history", "post", function (data) {
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
                requestData(downLoadAndPrintObj.downloadMap, "/borrow/downloadBorrowData", "post", function (data) {
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
        requestData($("#queryForm").serializeJson(),"/borrow/batchPrintData", "post", function (data) {
            if(data.code == 200){
                if(data.data.list && data.data.list.length > 0){
                    $.each(data.data.list, function (k, borrow) {
                        for (var attr in borrow) {
                            //借款类型
                            if(attr == 'type'){
                                $("#modalContentWrap").find("div ."+attr).text(downLoadAndPrintObj.type[borrow[attr]]  || "")
                            }else if(attr == "auditTable"){
                                var auditHtml = "";
                                if(borrow[attr] && borrow[attr].length > 0){
                                    $.each(borrow[attr], function (i, item) {
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
                            }else {
                                $("#modalContentWrap").find("."+attr).text("");
                                $("#modalContentWrap").find("."+attr).text(borrow[attr] || "");
                            }
                        }
                        html += $("#modalContentWrap")[0].outerHTML + "\n";
                    });
                }else {
                    layer.msg('无借款打印数据！', {time: 2000, icon: 6});
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
        requestData($("#queryForm").serializeJson(),"/borrow/batchDownloadData", "post", function (data) {
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