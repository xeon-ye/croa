var orderNum = 0;
var inputId;
var inputName;
var inputDeptId;
var inputDeptName;
$(document).ready(function () {
    //flag=1审核，否则查看
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        var flag = getQueryString("flag");
        if(flag == 1){//编辑
            edit(getQueryString("id"));
        }else{//查看
            view(getQueryString("id"), flag);
        }
    }

    if (user.dept.name == '大客户部') {
        document.getElementById("addDiv").style.display = 'block';
    }
    $("#query_table_logs").jqGrid({
        url: baseUrl + '/project/listPg',
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
        // sortable: "false",
        // sortname: "id",
        // sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        // autoScroll: true,
        // scroll:true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['部门名称','部门id',
        //     '业绩（含税）', '回款','税金','退款', '其它支出', '成本（不含税）','利润','提成',
        // ],
        colModel: [
            {name: 'id', label: 'id', editable: true, align: 'center', hidden: true, width: 80},
            {name: 'code', label: '编号', editable: true, align: 'center', width: 160},
            {name: 'name', label: '名称', editable: true, align: 'center', width: 200},
            {name: 'applyName', label: '申请人', editable: true, align: 'center', width: 80},
            {name: 'ywy', label: '销售', editable: true, align: 'center', width: 100},
            {name: 'khzj', label: '客户总监', editable: true, align: 'center', width: 100},
            {name: 'saleSum', label: '报价合计', editable: false, align: 'center', width: 100,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},},
            {name: 'applyTime', label: '申请时间', editable: true, align: 'center', width: 120},
            {name: 'updateTime', label: '修改日期', editable: true, align: 'center', width: 120},
            {name: 'state', label: 'state', editable: true, align: 'center',hidden:true, width: 120},
            {name: 'state1', label: '状态', editable: true, align: 'center', width: 100,
                formatter: function (value, grid, rows) {
                    switch (rows.state) {
                        case -1 :
                            return "<span style='color:red'>审核驳回</span>";
                        case 0 :
                            return "<span style=''>已保存</span>";
                        case 1 :
                            return "<span style=''>已完成</span>";
                        case 27 :
                            return "<span style=''>销售审批</span>";
                        case 28 :
                            return "<span style=''>总经办审批</span>";
                    }
                }
            },
            {name: 'disabled', label: 'disabled', editable: true, align: 'center',hidden:true, width: 80},
            {name: 'disabled1', label: '是否停用', editable: true, align: 'center', width: 60,
                formatter: function (value, grid, rows) {
                    if(rows.disabled == 1){
                        return "<span style='color:red'>是</span>";
                    }else{
                        return "<span style=''>否</span>";
                    }
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 180,
                formatter: function (value, grid, rows, state) {
                    var html = "";
                    // html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='view(" + rows.id + ")'>&nbsp;查看&nbsp;</a>";
                    if (rows.taskId != null && rows.taskId !='') {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='showHistory(" + rows.id + ")'>&nbsp;审核详情&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id ) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='del(" + rows.id + ")'>&nbsp;删除&nbsp;</a>";
                    }
                    if((rows.state == 0 || rows.state == -1 || rows.state == 1) && rows.applyId == user.id ){
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='edit(" + rows.id + ")'>&nbsp;编辑&nbsp;</a>";
                    }
                    if (rows.state == 1) {
                        if(rows.applyId == user.id){
                            if (rows.disabled == 0) {
                                html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='disableProject(" + rows.id + ")'>&nbsp;停用&nbsp;</a>";
                            } else {
                                html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='enableProject(" + rows.id + ")'>&nbsp;启用&nbsp;</a>";
                            }
                        }
                    }
                    return html;
                }
            }
        ],
        gridComplete: function () {
            var width = $('#query_table_logs').parents(".jqGrid_wrapper").width();
            $('#query_table_logs').setGridWidth(width);
        },
        ondblClickRow: function (rowid, iRow, iCol, e) {
            view(rowid,0);
        },
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $("#addBtn").click(function () {
        orderNum=0;
        document.getElementById("editForm").reset();
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();
        $("#sumRatioDiv").removeClass("col-sm-offset-4");
        $("#sumRatioDiv").removeClass("col-sm-offset-6");
        $("#sumRatioDiv").addClass("col-sm-offset-6");
        document.getElementById("sumCommLabel").style.display="none";
        document.getElementById("sumCommSpan").style.display="none";
        document.getElementById("viewDiv1").style.display="none";
        document.getElementById("viewDiv2").style.display="none";
        $("#editFooter").show();
        $("#viewFooter").hide();
        $(".showOnAdd").show();
        $(".showOnEdit").hide();
        document.getElementById("auditOpinionDiv").style.display="none";
        $("#editForm [name='name']").removeAttrs("readonly");
        initNodeConfig();
        $("#editModal").modal({backdrop: "static"});
    });

    /**选中销售**/
    initYWTable();
    $("#ywSearch").click(function () {
        emptyYWTable();
        reloadYW();
    });
    $("#selectYW").click(function () {
        $("#YWModal").modal("hide");
        $("#editModal").modal("show");
        var rowid = $("#yw_table_logs").jqGrid("getGridParam", "selrow");
        var rowData = $("#yw_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        $("#" + inputId).val(rowData.id);
        $("#" + inputName).val(rowData.name);
        $("#" + inputDeptId).val(rowData.deptId);
        $("#" + inputDeptName).val(rowData.deptName);
    });
    $(".closeYW").click(function () {
        $("#YWModal").modal("hide");
        $("#editModal").modal("show");
    });
    /**选中除销售外的其他人员**/
    initUserTable();
    $("#userSearch").click(function () {
        emptyUserTable();
        reloadUser();
    });
    $("#selectUser").click(function () {
        $("#userModal").modal("hide");
        $("#editModal").modal("show");
        var rowid = $("#user_table_logs").jqGrid("getGridParam", "selrow");
        var rowData = $("#user_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        $("#" + inputId).val(rowData.id);
        $("#" + inputName).val(rowData.name);
        $("#" + inputDeptId).val(rowData.deptId);
        $("#" + inputDeptName).val(rowData.deptName);
    });
    $(".closeUser").click(function () {
        $("#userModal").modal("hide");
        $("#editModal").modal("show");
    });

});
function initYWTable(){
    $("#yw_table_logs").jqGrid({
        url: baseUrl + '/user/listUserByTypeAndCompanyCode',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#innerAccount").serializeJson(), //发送数据
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
        multiboxonly: true,
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
            {name: 'deptName', label: '部门名称', editable: true, width: 360},
            {name: 'name', label: '姓名', editable: true, width: 360},
            {name: 'userName', label: '用户名', editable: true, width: 240,hidden:true},
            // {name: 'balance', label: '账号开户行', editable: true, width: 240},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 0},
            {name: 'deptId', label: 'deptId', editable: true, hidden: true, width: 0},
        ],
        pager: "#yw_pager_logs",
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        //实现单选
        beforeSelectRow: function () {
            $("#yw_table_logs").jqGrid('resetSelection');
            return (true);
        }
    });
}
function reloadYW(){
    $("#yw_table_logs").jqGrid('setGridParam', {
        datatype: "json",
        postData: $("#YWForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}
function emptyYWTable(){
    $("#yw_table_logs").emptyGridParam();
}

function initUserTable(){
    $("#user_table_logs").jqGrid({
        url: baseUrl + '/user/listUserByCompanyCode',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#innerAccount").serializeJson(), //发送数据
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
        multiboxonly: true,
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
            {name: 'deptName', label: '部门名称', editable: true, width: 360},
            {name: 'name', label: '姓名', editable: true, width: 360},
            {name: 'userName', label: '用户名', editable: true, width: 240,hidden:true},
            // {name: 'balance', label: '账号开户行', editable: true, width: 240},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 0},
            {name: 'deptId', label: 'deptId', editable: true, hidden: true, width: 0},
        ],
        pager: "#user_pager_logs",
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        //实现单选
        beforeSelectRow: function () {
            $("#user_table_logs").jqGrid('resetSelection');
            return (true);
        }
    });
}
function reloadUser(){
    $("#user_table_logs").jqGrid('setGridParam', {
        datatype: "json",
        postData: $("#userForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}
function emptyUserTable(){
    $("#user_table_logs").emptyGridParam();
}

function sumPoint(t) {
    var sum = 0;
    $(".ratio").each(function (i, d) {
        var amount = $(d).val();
        if(!isNaN(parseFloat(amount))){
            if(amount<=0||amount>=100){
                swal("提成比例必须大于0，小于100！");
                amount = 0;
                $(t).val("");
            }
            sum += parseFloat(amount);
        }
    });
    $("#sumPoints").html(sum.toFixed(2));
}
function edit(id) {
    $.jgrid.gridUnload("#detail_table_logs");
    document.getElementById("editForm").reset();
    $("#editForm").find("input").removeClass('error');
    $("#editForm").validate().resetForm();
    $("#sumRatioDiv").removeClass("col-sm-offset-4");
    $("#sumRatioDiv").removeClass("col-sm-offset-6");
    $("#sumRatioDiv").addClass("col-sm-offset-6");
    document.getElementById("sumCommLabel").style.display="none";
    document.getElementById("sumCommSpan").style.display="none";
    document.getElementById("viewDiv1").style.display="none";
    document.getElementById("viewDiv2").style.display="none";
    getViewData(id,1);
    $("#editModal").modal({backdrop: "static"});

    $("#editFooter").show();
    $("#viewFooter").hide();
    $(".showOnAdd").hide();
    $(".showOnEdit").show();
    document.getElementById("auditOpinionDiv").style.display="none";
}

function view(id,flag) {
    document.getElementById("editForm").reset();
    $("#editForm").find("input").removeClass('error');
    $("#editForm").validate().resetForm();

    if(getQueryString("itemId")!=null && getQueryString("itemId")!="" && getQueryString("itemId") != undefined){
        $("#itemIds").val(getQueryString("itemId"));
    }
    document.getElementById("viewDiv1").style.display="";
    document.getElementById("viewDiv2").style.display="";
    $("#editFooter").hide();
    $("#viewFooter").show();
    if(flag == 0){
        $("#auditTrue").hide();
        $("#auditFalse").show();
        $("#confirm1").hide();

        document.getElementById("auditOpinionDiv").style.display="none";
    }else if(flag == 2){
        $("#expense").data("id",id);//审批要传id
        $("#auditTrue").show();
        $("#auditFalse").hide();
        document.getElementById("auditOpinionDiv").style.display="";
    }else if(flag == 3){
        $("#auditTrue").hide();
        $("#auditFalse").show();
        $("#confirm1").show();
        document.getElementById("auditOpinionDiv").style.display="none";
    }
    $.jgrid.gridUnload("#detail_table_logs");
    $("#editModal").modal({backdrop: "static"});
    getViewData(id,flag);
}

/**
 *
 * @param id
 * @param flag flag=0查看，flag=1编辑,flag=2审批,flag=3抄送确认
 */
function getViewData(id,flag) {
    $.ajax({
        url: baseUrl + "/project/view",
        type: "post",
        data: {id: id},
        dataType: "json",
        async:"false",
        success: function (data) {
            if (data.code === 200) {
                var entity = data.data.data.entity;
                if(entity!=null){
                    for(var attr in entity){
                        $("#"+attr).val(entity[attr]);
                        if(flag!=1){
                            $("#editForm [name=" + attr + "]").attr("readonly", "readonly");
                        }else{
                            $("#editForm [name=" + attr + "]").removeAttrs("readonly");
                        }
                    }
                    var stateStr = entity["state"];
                    switch (stateStr) {
                        case -1 :
                            stateStr = "审核驳回";
                            break;
                        case 0 :
                            stateStr = "已保存";
                            break;
                        case 1 :
                            stateStr = "已完成";
                            break;
                        case 27 :
                            stateStr = "销售审批";
                            break;
                        case 28 :
                            stateStr = "总经办审批";
                            break;
                        default :
                            break;
                    }
                    if (stateStr != null) {
                        $("#state1").html(stateStr);
                    }
                    $("#code1").html(entity["code"]);
                    var sumComm = 0;
                    document.getElementById("sumDiv").style.display="none";
                    $("incomeSum").html("0");
                    $("notIncomeSum").html("0");
                    if((flag == 0 || flag == 3) && entity.state == 1 ){
                        $("#sumRatioDiv").removeClass("col-sm-offset-6");
                        $("#sumRatioDiv").removeClass("col-sm-offset-4");
                        $("#sumRatioDiv").addClass("col-sm-offset-4");
                        document.getElementById("sumCommLabel").style.display="";
                        document.getElementById("sumCommSpan").style.display="";
                        $.ajax({
                            url: baseUrl + "/project/querySumByProjectId",
                            type: "post",
                            data: {id: id},
                            dataType: "json",
                            async: false,
                            success: function (data) {
                                if (data.code === 200) {
                                    sumComm = parseFloat(data.data.commSum).toFixed(2);
                                    if(data.data.count>0){
                                        initDetailGrid(id);
                                        document.getElementById("sumDiv").style.display="";
                                        $("#incomeSum").html(data.data.incomeSum.toFixed(2));
                                        $("#notIncomeSum").html(data.data.notIncomeSum.toFixed(2));
                                    }
                                }
                            }
                        });
                    }else{
                        $("#sumRatioDiv").removeClass("col-sm-offset-4");
                        $("#sumRatioDiv").removeClass("col-sm-offset-6");
                        $("#sumRatioDiv").addClass("col-sm-offset-6");
                        document.getElementById("sumCommLabel").style.display="none";
                        document.getElementById("sumCommSpan").style.display="none";
                    }
                    $("#sumCommSpan").html("¥"+sumComm);
                    var list = data.data.data.list;
                    if(list!=null && list.length>0){
                        orderNum=data.data.data.entity.orderNum;
                        getNodeList(list,flag,entity.state,sumComm);
                    }else{
                        initNodeConfig();
                    }
                    $("#selectNextUser").empty();
                    if(flag==2 && entity.state==27){
                        $("#selectNextDiv").show();
                        $("#selectNextBtn").show();
                        $("#pass1").hide();
                        var html = "<option value='0'>下一个审批人</option>";
                        $.ajax({
                            url: baseUrl + "/user/listByTypeAndCompanyCode/ZJB",
                            type: "get",
                            data: {},
                            dataType: "json",
                            async: false,
                            success: function (data) {
                                if (data!=null && data.length>0) {
                                    for(var i=0;i<data.length;i++){
                                        //仅展示未交接的
                                        if(data[i] && data[i].handoverState == 0){
                                            html += "<option value="+data[i].id+" dataId="+data[i].deptId+">"+data[i].name+"</option>";
                                        }
                                    }
                                }
                            }
                        });
                        $("#selectNextUser").append(html);
                    }else {
                        $("#selectNextDiv").hide();
                        $("#selectNextBtn").hide();
                        $("#pass1").show();
                    }
                }
            }
        }
    });
}
function getNodeList(list,flag,state,sumComm) {
    $("#memberGroup").empty();
    var html = "";
    for(var i=0;i<list.length;i++){
        var readonlyStr = "";
        var sign = true ;
        var comm = 0 ;
        var commReadOnlyStr = "";
        if(flag!=1 || list[i].name==="销售" || list[i].name==="客户总监"){
            readonlyStr = "readonly='readonly'";
            sign = false;
        }
        if(flag!=1){
            commReadOnlyStr = "readonly='readonly'";
        }
        html += "<div class=\"form-group\" style=\"margin-top: 0px;margin-bottom: 0px;\">" +
            "<div class=\"col-sm-2\">" +
            "   <input type='text' class=\"form-control\" id='name_" + list[i].index + "' name='name_" + list[i].index + "' value='"+list[i].name+"' required=\"\" "+readonlyStr+" />" +
            "</div>" +
            "<div class=\"col-sm-2\">" +
            "   <input type=\"text\" class=\"form-control\" name=\"userName_" + list[i].index + "\" id=\"userName_" + list[i].index + "\" " +
                "   value='"+list[i].userName+"' required=\"\" readonly=\"readonly\"/>" +
            "   <input type='hidden' id='userId_" + list[i].index + "' name='userId_" + list[i].index + "' value='"+list[i].userId+"'/>" +
            "   <input type='hidden' id='deptId_" + list[i].index + "' name='deptId_" + list[i].index + "' value='"+list[i].deptId+"'/>" +
            "   <input type='hidden' id='deptName_" + list[i].index + "' name='deptName_" + list[i].index + "' value='"+list[i].deptName+"'/>" +
            "</div>";
        if(flag == 1){
            html += "<div class=\"col-sm-2 selectAccountBtnDiv\">"+
            "<button type=\"button\"" +
                "   class=\"btn btn-primary btn-circle glyphicon  glyphicon-search selUser\" title=\"选择用户\" onclick='selUser(this,"+list[i].index+")'></button>" +
                "<button type=\"button\" class=\"btn btn-warning btn-circle glyphicon  glyphicon-remove cleanUser\"" +
                "   title=\"清空用户\" onclick='cleanUser(this)'></button>" +
            "</div>";
        }
        //flag=1表示编辑要显示选择用户和增加删除的按钮，flag!=1表示查看或审批，(flag == 0或flag ==3) && state == 1要显示提成列，所以要把这个div占用的空间去掉，其他时间不用去掉
        if(flag != 1 ){
            if(!((flag == 0 || flag == 3) && state == 1)){
                html += "<div class=\"col-sm-2 selectAccountBtnDiv\"></div>" ;
            }
        }
        // if(flag==1) {
            html += "<label class=\"col-sm-2 control-label\">提成比例:</label>" +
                "<div class=\"col-sm-2\">" +
                "<div class=\"input-group m-b\">" +
                "<input type=\"text\" class=\"form-control ratio\" name='ratio_" + list[i].index + "' required=\"\" value='" + list[i].ratio + "' " +
                "onkeyup=\"this.value=this.value.toString().match(/^\\d+(?:\\.\\d{0,2})?/)\" onblur='sumPoint(this)' " + commReadOnlyStr + "> " +
                "<span class=\"input-group-addon\">%</span>" +
                "</div>" +
                "</div>";
        // }
        if(flag == 1){
            html += "<div class=\"col-sm-1 selectAccountBtnDiv\">" +
                "<input type='hidden' id='index_" + list[i].index + "' name='index_" + list[i].index + "' value='"+list[i].index+"' />";
            if (list[i].index == 1) {
                html += "<button type=\"button\" class=\"btn btn-white glyphicon  glyphicon-plus addUser\" title=\"增加用户！\" onclick='addUser(this)'></button>";
            }
            if(sign){
                html += "<button type=\"button\" class=\"btn btn-white glyphicon  glyphicon-minus delUser\" title=\"删除用户！\" onclick='delTr(this)'></button>";
            }
            html += "</div>";
        }

        if((flag == 0||flag==3) && state == 1){
            comm = parseFloat(sumComm * list[i].ratio * 0.01).toFixed(2);
            html += "<label class=\"col-sm-2 control-label\">提成金额:</label>" +
                "<span class=\"col-sm-2 text-red\" style='padding-top:8px'>¥"+comm+"</span>";
        }
        html += "</div>";
    }
    $("#memberGroup").append(html);
    sumPoint();
}
function addTr(t) {
    orderNum++;
    var html = "<div class=\"form-group\" style=\"margin-top: 0px;margin-bottom: 0px;\">" +
                "<div class=\"col-sm-2\">" +
                    "<input type='text' class=\"form-control\" id='name_" + orderNum + "' name='name_" + orderNum + "' required=\"\" value=''/></div>" +
                "<div class=\"col-sm-2\">" +
                    "<input type=\"text\" class=\"form-control\" name=\"userName_" + orderNum + "\" id=\"userName_" +  orderNum + "\" required=\"\" readonly=\"readonly\"/>" +
                    "<input type='hidden' id='userId_" + orderNum + "' name='userId_" + orderNum + "'/>" +
                    "<input type='hidden' id='deptId_" + orderNum + "' name='deptId_" + orderNum + "'/>" +
                    "<input type='hidden' id='deptName_" + orderNum + "' name='deptName_" + orderNum + "'/></div>" +
                "<div class=\"col-sm-2 selectAccountBtnDiv\">" +
                    "<button type=\"button\" class=\"btn btn-primary btn-circle glyphicon  glyphicon-search selUser\" title=\"选择用户\" onclick='selUser(this,"+orderNum+")'></button>" +
                    "<button type=\"button\" class=\"btn btn-warning btn-circle glyphicon  glyphicon-remove cleanUser\" title=\"清空用户\" onclick='cleanUser(this)'></button>" +
                "</div>" +
                "<label class=\"col-sm-2 control-label\">提成比例:</label>" +
                "<div class=\"col-sm-2\">" +
                    "<div class=\"input-group m-b\">" +
                        "<input type=\"text\" class=\"form-control ratio\" name=\"ratio_" + orderNum+"\" required=\"\" " +
                            "onkeyup=\"this.value=this.value.toString().match(/^\\d+(?:\\.\\d{0,2})?/)\" onblur='sumPoint(this)'> " +
                        "<span class=\"input-group-addon\">%</span>" +
                    "</div>" +
                "</div>" +
                "<div class=\"col-sm-1 selectAccountBtnDiv\">" +
                    "<input type='hidden' id='index_" + orderNum + "' name='index_" + orderNum + "' value='"+orderNum+"' />" +
                    "<button type=\"button\" class=\"btn btn-white  glyphicon  glyphicon-minus delUser\" onclick=\"delTr(this);\" title=\"删除用户！\"></button> " +
                "</div>" +
                "</div>";
    var root = t.parent().parent().parent().children().last();
    $(root).after(html);
    sumPoint();
}

function selUser(t,index) {
    $("#editModal").modal("hide");
    var array = $(t).parent().prev("div").children();
    inputName = $(array[0]).attr("id");
    inputId = $(array[1]).attr("id");
    inputDeptId = $(array[2]).attr("id");
    inputDeptName = $(array[3]).attr("id");
    if(index == 1){
        document.getElementById("YWForm").reset();
        $("#YWModal").modal({backdrop: "static"});
        reloadYW();
    }else{
        document.getElementById("userForm").reset();
        $("#userModal").modal({backdrop: "static"});
        reloadUser();
    }
}
function cleanUser(t) {
    $(t).parent().prev().find("input").val("");
}
function addUser(t) {
    addTr($(t));
}
function delTr(t) {
    var node = $(t).parent().parent();
    $(node).remove();
    sumPoint();
}

function onClick(e, treeId, treeNode) {
    $("#userId").val(treeNode.id);
    $("#userName").val(treeNode.name);
    $("#" + inputId).val(treeNode.id);
    $("#" + inputName).val(treeNode.name);
    $("#" + deptId).val(treeNode.pid);
    //清除出错校验
    $("#" + inputName).removeClass("error");
    $("#" + inputName + "-error").remove();
    $("#ztreeModal").modal("hide");
    $("#editModal").modal({backdrop: "static"});
}

function initNodeConfig() {
    $.ajax({
        url: baseUrl + "/project/initNodeConfig",
        type: "post",
        data: {},
        dataType: "json",
        async: false,
        success: function (data) {
            $("#memberGroup").empty();
            if (data.code == 200) {
                var html = "";
                var list = data.data.list;
                if (list != null) {
                    for (var i = 0; i < list.length; i++) {
                        orderNum++;
                        var readonlyStr = "";
                        var sign = true ;
                        if(list[i].name==="销售"||list[i].name==="客户总监"){
                            readonlyStr = "readonly='readonly'";
                            sign = false;
                        }
                        html += "<div class=\"form-group\" style=\"margin-top: 0px;margin-bottom: 0px;\">" +
                            "<div class=\"col-sm-2\">" +
                            "<input type='text' class=\"form-control\" id='name_" + orderNum + "' name='name_" + orderNum + "' value='"+list[i].name+"' required=\"\" "+readonlyStr+"/></div>" +
                            "<div class=\"col-sm-2\"><input type=\"text\" class=\"form-control\" name=\"userName_" + orderNum + "\" id=\"userName_" + orderNum + "\" required=\"\" readonly=\"readonly\"/>" +
                            "<input type='hidden' id='userId_" + orderNum + "' name='userId_" + orderNum + "' />" +
                            "<input type='hidden' id='deptId_" + orderNum + "' name='deptId_" + orderNum + "' />" +
                            "<input type='hidden' id='deptName_" + orderNum + "' name='deptName_" + orderNum + "' /></div>" +
                            "<div class=\"col-sm-2 selectAccountBtnDiv\">" +
                            "<button type=\"button\" class=\"btn btn-primary btn-circle glyphicon  glyphicon-search selUser\" title=\"选择用户\" onclick='selUser(this,"+orderNum+")'></button>" +
                            "<button type=\"button\" class=\"btn btn-warning btn-circle glyphicon  glyphicon-remove cleanUser\" title=\"清空用户\" onclick='cleanUser(this)'></button>" +
                            "</div>" +
                            "<label class=\"col-sm-2 control-label\">提成比例:</label>" +
                            "<div class=\"col-sm-2\"><div class=\"input-group m-b\">" +
                            "<input type=\"text\" class=\"form-control ratio\" name='ratio_" + orderNum +"' required=\"\"" +
                                " onkeyup=\"this.value=this.value.toString().match(/^\\d+(?:\\.\\d{0,2})?/)\" onblur='sumPoint(this)'> <span class=\"input-group-addon\" >%</span></div></div>" +
                            "<div class=\"col-sm-1 selectAccountBtnDiv\">" +
                            "<input type='hidden' id='index_" + orderNum + "' name='index_" + orderNum + "' value='"+list[i].index+"' />" ;
                        if (list[i].index == 1) {
                            html += "<button type=\"button\" class=\"btn btn-white glyphicon  glyphicon-plus addUser\" title=\"增加用户！\" onclick='addUser(this)'></button>";
                        }
                        if (sign) {
                            html += "<button type=\"button\" class=\"btn btn-white glyphicon  glyphicon-minus addUser\" title=\"删除用户！\" onclick='delTr(this)'></button>";
                        }
                        html += "</div>" +
                            "</div>";
                    }
                }
                $("#memberGroup").append(html);
                sumPoint();
            }
        }
    });
}
var qtl;
function initDetailGrid(id){
    qtl = $("#detail_table_logs").jqGrid({
        url: baseUrl + '/project/queryArticlesByProjectId',
        datatype: "json",
        mtype: 'POST',
        postData: {id:id}, //发送数据
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
        // sortable: "false",
        // sortname: "id",
        // sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        // autoScroll: true,
        // scroll:true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['部门名称','部门id',
        //     '业绩（含税）', '回款','税金','退款', '其它支出', '成本（不含税）','利润','提成',
        // ],
        colModel: [
            {name: 'artId', label: 'artId', editable: true, align: 'center', hidden: true, width: 80},
            {name: 'title', label: '标题', editable: true, align: 'center', width: 100},
            {name: 'link', label: '链接', editable: true, align: 'center', width: 100,
                formatter: function (v, options, row) {
                    if (!v) {
                        return "";
                    } else {
                        var str = row.link.substring(0, 4).toLowerCase();
                        if (str == "http") {
                            return "<a href='" + row.link + "' target='_blank'>" + row.link + "</a>";
                        } else {
                            return "<a href='//" + row.link + "' target='_blank'>" + row.link + "</a>";
                        }
                    }

                }
            },
            {name: 'issuedDate', label: '发布日期', editable: true, align: 'center', width: 120},
            {name: 'custCompanyName', label: '客户公司', editable: true, align: 'center', width: 100},
            {name: 'custName', label: '对接人', editable: true, align: 'center', width: 100},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, align: 'center', width: 100},
            {name: 'userName', label: '业务员', editable: true, align: 'center', width: 100},
            {name: 'mediaUserName', label: '媒介', editable: true, align: 'center', width: 100},
            {name: 'mediaName', label: '媒体名称', editable: true, align: 'center', width: 100},
            {name: 'promiseDate', label: '客户答应到款时间', editable: true, align: 'center', width: 120},
            {name: 'num', label: '数量', editable: true, align: 'center', width: 60},
            {name: 'brand', label: '品牌', editable: true, align: 'center', width: 80},
            {name: 'saleAmount', label: '应收', editable: true, align: 'center', width: 100},
            {name: 'incomeAmount', label: '回款', editable: true, align: 'center', width: 100},
            {name: 'outgoAmount', label: '请款', editable: true, align: 'center', width: 100},
            {name: 'taxes', label: '税金', editable: true, align: 'center', width: 100},
            {name: 'refundAmount', label: '退款', editable: true, align: 'center', width: 100},
            {name: 'otherPay', label: '其它支出', editable: true, align: 'center', width: 100},
            {name: 'profit', label: '利润', editable: true, align: 'center', width: 100},
            {name: 'comm', label: '提成', editable: true, align: 'center', width: 100}
        ],
        gridComplete: function () {
            var width = $('#detail_table_logs').parents(".jqGrid_wrapper").width();
            $('#detail_table_logs').setGridWidth(width);
        },
        pager: jQuery("#detail_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });
}

function submitHander(t, url, state) {
    var lock = true;
    if ($("#editForm").valid()) {
        if($("#sumPoints").html()!=100){
            swal("提成比例合计必须是100，当前为："+$("#sumPoints").html());
            return;
        }
        $("#orderNum").val(orderNum);
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
            startModal("#save");//锁定按钮，防止重复提交
            startModal("#save1");//锁定按钮，防止重复提交
            startModal("#submit");//锁定按钮，防止重复提交
            startModal("#submit1");//锁定按钮，防止重复提交
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: url,
                    data: param,
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();   //解锁按钮锁定
                        if (data.code == 200) {
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/project/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            if (state > 0) {
                                swal(data.data.message);
                                $("#editModal").modal('hide');
                            } else {
                                layer.msg(data.data.message);
                                edit(data.data.entity.id);
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
                url: baseUrl + "/project/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/project/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
                }
            });
        }
    }, function () {
        return;
    });
}

function enableProject(id) {
    var lock = true ;
    layer.confirm('确认启用该项目？', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/project/enableProject",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/project/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
                }
            });
        }
    }, function () {
        return;
    });
}

function disableProject(id) {
    var lock = true ;
    layer.confirm('确认停用该项目？', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/project/disableProject",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#query_table_logs").emptyGridParam();
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/project/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
    approveTask($("#taskId").val(), 0, t.id,$("#desc").val())
}
//审核记录查看
function showHistory(id) {
    //process详见IProcess
    $("#historyModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: "/process/history",
        data: {dataId: id, process: 26},
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
                    html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=26&t=" + new Date().getTime() + "' style='width: 100%; margin-top: 8px; margin-bottom: -100px; margin-left: -130px;'/></div>";
                    $("#history").append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}

// 提交审核意见；
function completeApprove(flag) {
    var userId = $("#selectNextUser").find("option:selected").val();
    var userName = $("#selectNextUser").find("option:selected").text();
    var deptId = $("#selectNextUser").find("option:selected").attr("dataid");
    if(userId==0){
        swal("请选择下一个审批人!");
        return;
    }
    startModal("#selectNextBtn");
    $.post(baseUrl + "/taxUser/completeApprove",{taskIds:$("#taskId").val(),desc:$("#desc").val(),agree:true,userId:userId,userName:userName,deptId:deptId},
        function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                refrechPage("/homePage");
                closeCurrentTab();
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
        }, "json");
}

function confirm(t) {
    startModal("#confirm1");
    if($("#itemIds").val()==undefined||$("#itemIds").val()==null||$("#itemIds").val()==""){
        swal("未找到待办id，请刷新重试！");
        return ;
    }
    $.ajax({
        type: "post",
        url: baseUrl + "/project/confirm",    //向后端请求数据的url
        data: {itemId:$("#itemIds").val()},
        dataType: "json",
        async: false,
        success: function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                swal(data.data.message);
                setTimeout(function () {
                    // 审核后刷新首页；
                    refrechPage("/homePage");
                    closeCurrentTab();
                }, 1000);
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