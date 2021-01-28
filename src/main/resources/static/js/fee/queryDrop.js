var arrayNewList = new Array();
var saleSum = 0 ;
var outgoSum = 0 ;
var mediaUserPlateMap =[];

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
                url: baseUrl + "/drop/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#query_table_logs").emptyGridParam() ;
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/drop/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message) ;
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
function returnBack(taskId,itemId) {
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
                data: {taskId: taskId,itemId:itemId},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#query_table_logs").emptyGridParam() ;
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/drop/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message) ;
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

//审核记录查看
function showHistory(id) {
    //process详见IProcess
    $("#historyModal").modal('toggle');
    $.ajax({
        type: "post",
        url: baseUrl + "/process/history",
        data: {dataId: id, process: 11},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                $("#history").empty();
                if (data.data.data != null) {
                    var html = "";
                    html += "<div class='form-control'>" +
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
                    html += "<div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=11&t=" + new Date().getTime() + "' style='width: 150%; margin-left: -330px; margin-top: -100px; margin-bottom: -100px;'/></div>";
                    $("#history").append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
};

//flag=1审核页面，flag=-1查看页面,flag=0稿件管理跳转到查看页面,flag=2确认页面
function view(id, flag) {
    $("#flagEdit").val("");
   $("#flag").val(flag);
    $("#showHistory1").data("id",id);
    if (flag == 1) {
        $("#auditTrue").show();
        $("#auditFalse").hide();
    } else if (flag == -1) {
        var returnType= getQueryString("returnType");
        if(returnType==5){
            $("#auditTrue").show();
            $("#auditFalse").hide();
            $("#reject1").hide();
            $("#pass1").hide();
            $("#showHistory1").hide();
        }else {
            $("#auditTrue").hide();
            $("#auditFalse").show();
            //查看页面返回按钮隐藏
            $("#closeModal").show();
            $("#goback").hide();}

    } else if(flag==2) {
        $("#auditTrue").hide();
        $("#auditFalse").show();
        $("#closeModal").show();
        $("#goback").hide();
    }else{
        $("#auditTrue").hide();
        $("#auditFalse").show();
        $("#closeModal").hide();
        $("#goback").show();
    }
    $("#navTabs").hide() ;
    $("#editModal").modal({backdrop: "static"});
    document.getElementById("editForm").reset();
    $(".firstDiv").hide();
    $(".secondDiv").hide();
    $(".thirdDiv").show();
    $("#thirdStep").click();
    $("#thirdFoot").hide() ;
    $("#viewFoot").show() ;
    $.ajax({
        type: "post",
        url: baseUrl + "/drop/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            if(data.code == 200){
                for (var attr in data.data.entity) {
                    $("#editForm [name=" + attr + "]").removeAttrs("readonly") ;
                    $("#editForm [name=" + attr + "]").prop("style","border:0;") ;
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
                                stateStr = "会计审核";
                                break;
                            case 10 :
                                stateStr = "业务员确认";
                                break;
                            case 12 :
                                stateStr = "财务负责人确认出款";
                                break;
                            case 16 :
                                stateStr = "出纳审核";
                                break;
                            default :
                                break;
                        }
                        if (dataStr != null) {
                            $("#editForm [name='state']").val(stateStr);
                        }
                    }else{
                        $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);
                    }

                    if (attr == "supplierPhone"){
                        // var fl = false;
                        // var plateId = data.data.entity["plateIds"];
                        // if (plateId){
                        //     if (mediaUserPlateMap.contains(plateId)){
                        //         fl= true;
                        //     }
                        // }
                        var value =  data.data.entity[attr] || "";
                        if (value){
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

                        }else {
                            $("#supplierPhone").val("")
                        }
                    }
                    if(attr=="title"){
                        $("#editModal .modal-title").html(data.data.entity[attr])
                    }
                }
                $("#saleSum").html(data.data.saleSum);
                $("#outgoSum").html(data.data.outgoSum);
                $("#selected_article_table_logs").jqGrid('setGridParam', {
                    datatype:'json',
                    postData: {id: id}, //发送数据
                }).trigger("reloadGrid"); //重新载入
                resize("#selected_article_table_logs");
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
};

//初始化业务部门
function getDept(){
    var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
    var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
    var deptDiv = document.getElementById("deptDiv");
    //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
    if((currentDeptQx || currentCompanyQx || isZC()) || user.dept.code == 'CW'){
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
    requestData({deptId: deptId,deptCode:'MJ'},"/dept/listAllDeptByIdAndCode","POST",function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

function edit(id,flag) {
    $("#flagEdit").val(flag);
    $("#editModal").modal({backdrop: "static"});
    $("#navTabs").show() ;
    $(".firstDiv").hide();
    $(".secondDiv").hide();
    $(".thirdDiv").show();
    $("#thirdStep").click();
    $("#thirdFoot").show() ;
    $("#viewFoot").hide() ;
    $("#flag").val("");

    document.getElementById("editForm").reset();
    $("#editForm").find("input").removeClass('error');
    $("#editForm").validate().resetForm();
    $.ajax({
        type: "post",
        url: baseUrl + "/drop/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.entity) {
                $("#editForm [name=" + attr + "]").removeAttrs("style") ;
                $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);
                if (attr == "supplierPhone"){
                    // var fl = false;
                    // var plateId =  data.data.entity["plateIds"];
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
                switch (attr) {
                    case "title":
                        $("#editModal .modal-title").html("退稿详情") ;
                        break ;
                    case "title":
                        $("#editForm [name=" + attr + "]").removeAttrs("readonly") ;
                        break ;
                    case "remark":
                        $("#editForm [name=" + attr + "]").removeAttrs("readonly") ;
                        break ;
                    case "payAmount":
                        $("#editForm [name=" + attr + "]").removeAttrs("readonly") ;
                        break ;
                    default:
                        $("#editForm [name=" + attr + "]").prop("readonly","readonly") ;
                }
            }
            if(data.data.entity["type"] == 1){//已经输入了备用金，不允许更改
                $("#editForm #payAmount").attr("readonly","readonly") ;
            }
            $("#saleSum").html(data.data.saleSum);
            $("#outgoSum").html(data.data.outgoSum);

            $("#selected_article_table_logs").jqGrid('setGridParam', {
                datatype:'json',
                postData: {id: id}, //发送数据
            }).trigger("reloadGrid"); //重新载入
            resize("#selected_article_table_logs");
        }
    });
}

function saveStepOne() {
    var lock = true ;
    startModal("#selectArticle");//锁定按钮，防止重复提交
    layer.confirm('已选定稿件和供应商？确定后不能更改！', {
        btn: ['确定', '取消'], //按钮
        shade: false, //不显示遮罩
        end:function () {
            Ladda.stopAll();   //解锁按钮锁定
            return;
        }
    }, function (index) {
        layer.close(index);
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/drop/saveStepOne",    //向后端请求数据的url
                data: $("#secondForm").serializeJson(),
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    if (data.code == 200) {
                        $("#query_table_logs").emptyGridParam() ;
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/drop/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        edit(data.data.entity.id);
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
}

$(document).ready(function () {
    getDept();
    $.jgrid.defaults.styleUI = 'Bootstrap';
    // userMediaPlateList();

    mediaUserPlateMap = userMedaiPlateList();
    //flag=1审核，否则查看
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        view(getQueryString("id"), getQueryString("flag"));
    }
    if (getQueryString("editId") != null && getQueryString("editId") != "" && getQueryString("editId") != undefined) {
        edit(getQueryString("editId"));
    }

    if(hasRoleMJ()){
        $("#addBtn").show();
    }else{
        $("#addBtn").hide();
    }
    $("#refresh").click(function () {
        if ($("#flag").val()) {

            view($("#id").val(), $("#flag").val());
        }
        if($("#flagEdit").val()){

            edit($("#id").val(),0)}

    });


    loadAllCompany2();

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/drop/listPg',
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
        colModel: [
            {name: 'code', label: '退稿编号', editable: true, width: 100},
            {name: 'title', label: '退稿标题', editable: true, width: 180},
            {name: 'supplierName', label: '供应商名称', editable: true, width: 180},
            {name: 'supplierContactor', label: '供应商联系人', editable: true, width: 100},
            {name: 'phone', label: '联系人手机号', editable: true, width: 60,formatter:function(value, grid, rows){
                return  supplierPhone(value, grid, rows);
            }},
            {name: 'applyName', label: '申请人', editable: true, width: 100},
            {name: 'deptName', label: '所在部门', editable: true, width: 100},
            {name: 'applyTime', label: '申请日期', editable: true, width: 100},
            {name: 'fundAmount', label: '关联的备用金', editable: true, hidden: false, width: 80},
            {name: 'payAmount', label: '退还的备用金', editable: true, hidden: false, width: 80},
            {name: 'taskId', label: 'taskId', editable: true, hidden: true, width: 80},
            {name: 'state', label: 'state', editable: true, hidden: true, width: 80},
            {name: 'itemId', label: 'itemId', editable: true, hidden: true, width: 80},
            {
                name: 'state1', label: '状态', editable: true, width: 120,
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
                            return "<span style='color:red'>会计审核</span>";
                        case 10 :
                            return "<span style='color:red'>业务员确认</span>";
                        case 12 :
                            return "<span style='color:red'>财务负责人确认出款</span>";
                        case 16 :
                            return "<span style='color:red'>出纳审核</span>";
                    }
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 180,
                formatter: function (value, grid, rows) {
                    var html = "";
                    // if (rows.isOwner && rows.applyId==user.id ) {//审批通过的链接有问题，先注释
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='view(" + rows.id + ",1)'>审批&nbsp;&nbsp;</a>";
                    // }
                    if (rows.taskId != null && rows.taskId !='') {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='showHistory(" + rows.id + ")'>审核详情&nbsp;&nbsp;</a>";
                    }
                    // html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='view(" + rows.id + ",0)'>查看&nbsp;&nbsp;</a>";
                    if ((rows.state == 0 || rows.state == -1)&&rows.applyId==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue'  onclick='edit(" + rows.id + ", 0)'>编辑&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1)&&rows.applyId==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='del(" + rows.id + ")'>删除&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state >3 && rows.state<10)&&rows.applyId==user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='returnBack(" +"\""+ rows.taskId + "\"," + rows.itemId + ")'>撤回&nbsp;&nbsp;</a>";
                    }
                    // if (hasRoleCWCN()&&rows.state == 2) {
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='CWReject(" + rows.id + ")'>财务撤回&nbsp;&nbsp;</a>";
                    // }
                    return html;
                },
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
            view(rowid, -1);
        },
    });
    resize("#query_table_logs");
    $("#queryForm select[name='state']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
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

    $("#supplierSearch").off("click").on("click", function () {
        var param = {
            supplierNameQc: $("#supplierNameQc1").val(),
            supplierContactorQc: $("#supplierContactorQc1").val()
        };
        //判断复选框是否被选中
        if($("#isOldSupplier").prop("checked")){
            param["isOldSupplier"] = 1;
        }else {
            delete param["isOldSupplier"];
        }
        $("#select_supplier_table_logs").emptyGridParam();
        $("#select_supplier_table_logs").jqGrid('setGridParam', {
            postData: param, //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
    //隐藏供应商选择全选框
    document.getElementById("cb_select_supplier_table_logs").style.display="none";
    $("#selectSupplier").off("click").on("click", function () {
        var rowid = $("#select_supplier_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = jQuery("#select_supplier_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
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
            arrayNewList.length = 0;//清空选中的稿件id
            saleSum = 0 ;
            outgoSum = 0 ;
            $("#saleSum2").text(saleSum.toFixed(2));
            $("#outgoSum2").text(outgoSum.toFixed(2));
            $("#supplierId").val(rowData.id);
            $("#supplierName").val(rowData.name);
            $("#mediaTypeId").val(rowData.media_type_id);
            $("#supplierContactor").val(rowData.contactor);
            $("#select_article_table_logs").emptyGridParam();
            $(".firstDiv").hide();
            $(".secondDiv").show();
            $("#secondStep").click();
            //加载稿件信息
            $("#supplierIdSec").val(rowData.id);
            $("#supplierNameSec").val(rowData.name);
            $("#supplierContactorSec").val(rowData.contactor);
            $("#select_article_table_logs").jqGrid('setGridParam', {
                datatype: "json",
                postData: $("#secondForm").serializeJson(), //发送数据
            }).trigger("reloadGrid"); //重新载入
            resize("#select_article_table_logs");
            if($("#supplierId").val()){
                $("#refresh").off("click").on("click",function () {
                    $("#select_article_table_logs").emptyGridParam();
                    $("#select_article_table_logs").jqGrid('setGridParam', {
                        datatype: 'json',
                        postData: $("#secondForm").serializeJson(), //发送数据
                    }).trigger("reloadGrid"); //重新载入
                });
            }
        };
    })

    $("#selected_article_table_logs").jqGrid({
        url: baseUrl + '/drop/listPgForSelectedArticle',
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
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 80},
            {name: 'supplierName', label: '供应商名称', editable: true, width: 100},
            {name: 'supplierContactor', label: '联系人', editable: true, width: 80},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 80},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 100},
            {name: 'innerOuter', label: '内外部', editable: true, width: 80},
            {name: 'channel', label: '频道', editable: true, width: 80},
            {name: 'electricityBusinesses', label: '电商商家', editable: true, width: 80},
            {name: 'userName', label: '业务员', editable: true, width: 80},
            {name: 'mediaUserName', label: '媒介', editable: true, width: 80},
            {name: 'Num', label: '数量', editable: true, width: 60},
            {name: 'title', label: '标题', editable: true, width: 180},
            {name: 'link', label: '链接', editable: true, width: 180,
                formatter: function (v, options, row) {
                    if (!v) {
                        return "";
                    } else {
                        var str = row.link.substring(0,4).toLowerCase();
                        if(str=="http"){
                            return "<a href='" + row.link + "' target='_blank'>" + row.link + "</a>";
                        }else{
                            return "<a href='//" + row.link + "' target='_blank'>" + row.link + "</a>";
                        }
                    }

                }
            },
            {name: 'issuedDate', label: '发布日期', editable: true, width: 100},
            {name: 'priceType', label: '价格类型', editable: true, width: 100},
            {name: 'saleAmount', label: '报价', editable: true, width: 100},
            {name: 'incomeAmount', label: '进账金额', editable: true, width: 100},
            {name: 'outgoAmount', label: '请款金额', editable: true, width: 100}
            // {name: 'owner', label: '联系人', editable: true, width: 120},
        ],
        pager: jQuery("#selected_article_pager_logs"),
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });
    $("#addBtn").click(function () {
        $("#flag").val("");
        $("#flagEdit").val("");
        $("#supplierId").val("");
        $("#supplierName").val("");
        $("#supplierContactor").val("");
        $(".firstDiv").show();
        $(".secondDiv").hide();
        $(".thirdDiv").hide();
        $("#firstStep").click();
        document.getElementById("editForm").reset();
        $("#editModal .modal-title").html("退稿流程");
        $("#editModal").modal({backdrop: "static"});
        $("#select_supplier_table_logs").emptyGridParam();
        $("#select_supplier_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {
                supplierNameQc: $("#supplierNameQc1").val(),
                supplierContactorQc: $("#supplierContactorQc1").val()
            } //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#select_supplier_table_logs");

        $("#refresh").click(function () {
            $("#select_supplier_table_logs").jqGrid('setGridParam',{
                datatype:"json",
            }).trigger("reloadGrid");
            resize("#select_supplier_table_logs");
            $("#supplierNameQc1").val("");
            $("#supplierContactorQc1").val("");
        })
    });

    $("#exportBtn").click(function () {
        var params = removeBlank($("#queryForm").serializeJson());
        location.href = "/drop/exportOutgo" + "?" + $.param(params);
    });

    $("#select_article_table_logs").jqGrid({
        url: baseUrl + '/drop/listPgForSelectArticle',
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
        colModel: [
            {name: 'outgoCode', label: '请款编号', editable: true, width: 80},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 80},
            {name: 'supplierName', label: '供应商名称', editable: true, width: 100},
            {name: 'supplierContactor', label: '联系人', editable: true, width: 80},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 80},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 120},
            {name: 'innerOuter', label: '内外部', editable: true, width: 80},
            {name: 'channel', label: '频道', editable: true, width: 80},
            {name: 'electricityBusinesses', label: '电商商家', editable: true, width: 80},
            {name: 'userName', label: '业务员', editable: true, width: 80},
            {name: 'mediaUserName', label: '媒介', editable: true, width: 80},
            {name: 'title', label: '标题', editable: true, width: 180,
                formatter: function (d) {
                    var html = "";
                    if (d) {
                        if (d && d.length > 15) {
                            var text = d.substring(0, 15);
                            html = "<span title='" + d + "'>" + text + "...</span>";
                        } else {
                            html = d;
                        }

                    }
                    return html;
                }
            },
            {name: 'Num', label: '数量', editable: true, width: 60},
            {name: 'link', label: '链接', editable: true, width: 180,
                formatter: function (v, options, row) {
                    if (!v) {
                        return "";
                    } else {
                        var str = row.link.substring(0,4).toLowerCase();
                        if(str=="http"){
                            return "<a href='" + row.link + "' target='_blank'>" + row.link + "</a>";
                        }else{
                            return "<a href='//" + row.link + "' target='_blank'>" + row.link + "</a>";
                        }
                    }

                }
            },
            {name: 'issuedDate', label: '发布日期', editable: true, width: 100},
            {name: 'priceType', label: '价格类型', editable: true, width: 100},
            {name: 'saleAmount', label: '报价', editable: true, width: 100},
            {name: 'incomeAmount', label: '进账', editable: true, width: 100},
            {name: 'outgoAmount', label: '请款', editable: true, width: 100},
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
        },
        onSelectRow: function (rowid, status) {
            if (status == true) {
                if (!(arrayNewList.indexOf(rowid) > -1)) {
                    saveData(rowid);
                }
            } else {
                deleteIndexData(rowid);
            }
            $("#saleSum2").text(saleSum.toFixed(2));
            $("#outgoSum2").text(outgoSum.toFixed(2));
        }
    });
    function saveData(obj) {
        arrayNewList.push(obj);
        var rowData = jQuery("#select_article_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
        saleSum += parseFloat(rowData.saleAmount);
        outgoSum += parseFloat(rowData.outgoAmount);
    }

    function deleteIndexData(obj) {
        //获取obj在arrayNewList数组中的索引值
        for (i = 0; i < arrayNewList.length; i++) {
            $("#row" + obj).remove()
            if (arrayNewList[i] == obj) {
                //根据索引值删除arrayNewList中的数据
                var rowData = jQuery("#select_article_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
                saleSum = saleSum - parseFloat(rowData.saleAmount);
                outgoSum = outgoSum - parseFloat(rowData.outgoAmount);
                arrayNewList.splice(i, 1);
            }
        }
    }
    $("#media_type_id").change(function () {
        arrayNewList.length = 0;//清空选中的稿件id
        saleSum = 0;
        outgoSum = 0;
        $("#saleSum2").text(saleSum.toFixed(2));
        $("#outgoSum2").text(outgoSum.toFixed(2));
        $("#select_article_table_logs").emptyGridParam();
        $("#select_article_table_logs").jqGrid('setGridParam', {
            datatype: 'json',
            postData: $("#secondForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
    $("#companyCode").change(function () {
        arrayNewList.length = 0;//清空选中的稿件id
        saleSum = 0;
        outgoSum = 0;
        $("#saleSum2").text(saleSum.toFixed(2));
        $("#outgoSum2").text(outgoSum.toFixed(2));
        $("#select_article_table_logs").emptyGridParam();
        $("#select_article_table_logs").jqGrid('setGridParam', {
            datatype: 'json',
            postData: $("#secondForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
    $("#articleSearch").off("click").on("click", function () {
        $("#select_article_table_logs").emptyGridParam();
        $("#select_article_table_logs").jqGrid('setGridParam', {
            postData: $("#secondForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
    $("#selectArticle").off("click").on("click", function () {
        if (arrayNewList.length == 0) {
            swal("请先选择稿件！");
        } else {
            $("#articleIdsSec").val(arrayNewList.toString());
            saveStepOne();
        }
    });
    $("#backStepOne").off("click").on("click", function () {
        //清空已选中供应商和稿件
        $("#supplierId").val("");
        $("#supplierName").val("");
        $("#supplierContactor").val("");
        $("#articleIdsSec").val("");
        $(".firstDiv").show();
        $(".secondDiv").hide();
        $(".thirdDiv").hide();
        $("#firstStep").click();
        //清空搜索供应商条件
        $("#supplierNameQc1").val("");
        $("#supplierContactorQc1").val("");
        document.getElementById("editForm").reset();
        $("#select_supplier_table_logs").emptyGridParam();
        $("#select_supplier_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {
                supplierNameQc: $("#supplierNameQc1").val(),
                supplierContactorQc: $("#supplierContactorQc1").val()
            } //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#select_supplier_table_logs");
    })

});

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
    // if (rows.hasOwnProperty('plateIds')){
    //     var plateIds = rows.plateIds.split(",");
    //     if (plateIds) {
    //         for (var i = 0; i < plateIds.length; i++) {
    //             if (mediaUserPlateMap.contains(plateIds[i])) {
    //                 //当前用户的板块包含了该供应商的板块
    //                 flag = true;
    //             }
    //         }
    //     }
    // }

    value = rows.phone || "";
    if(value){
        // if( (rows.supplierCreator == user.id)||(flag && hasRoleMJBZ()) || (flag && hasRoleMJZZ()) || (flag && hasRoleMJZJ()) || hasRoleCW() ){
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

function submitHander(t, url, state) {
    var lock = true ;
    if ($("#editForm").valid()) {
        if(parseFloat($("#fundAmount").val())<parseFloat($("#payAmount").val())){
            swal("退还的备用金不能大于关联的请款备用金！") ;
            return ;
        }
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
            var param = $("#editForm").serializeForm();
            startModal("#" + t.id);//锁定按钮，防止重复提交
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: url,
                    data: param,
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();   //解锁按钮锁定
                        if (data.code == 200) {
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/drop/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            $("#editModal").modal('hide');
                            swal(data.data.message) ;
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
}

//审核通过
function pass(t) {
    approveTask($("#taskId").val(), 1, t.id)
}

//审核驳回
function reject(t) {
    approveTask($("#taskId").val(), 0, t.id)
}

function loadAllCompany2() {
    $.get(baseUrl + "/dept/listJTAllCompany", null, function (data) {
        if (data.code == 200 && data.data.result) {
            $(data.data.result).each(function (i, d) {
                if (user.companyCode == d.code) {
                    $("#companyCode").append("<option value='" + d.code + "' data='" + d.id + "' selected='selected'>" + d.name + "</option>");
                    $("#companyCode2").append("<option value='" + d.code + "' data='" + d.id + "' selected='selected'>" + d.name + "</option>");
                } else {
                    $("#companyCode").append("<option value='" + d.code + "' data='" + d.id + "'>" + d.name + "</option>");
                    $("#companyCode2").append("<option value='" + d.code + "' data='" + d.id + "'>" + d.name + "</option>");
                }
            });
        }
    }, "json");
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