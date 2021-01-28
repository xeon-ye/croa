var XMflag = false;
var deptId = user.dept.id;//当前用户部门ID
var searchForm = {
    init:function () {
        searchForm.getDeptId();
        searchForm.loadDept();
        layui.use('form', function(){
            layui.form.on('select(user)', function(){
                $("#querySearch").trigger("click");
            });
        });
    },
    //如果是财务、管理等岗位获取公司或集团的id
    getDeptId:function(){
        var deptCode = user.dept.code;//当前部门编码
        var deptCompanyCode = user.dept.companyCode;//部门公司代码
        if(deptCompanyCode == "JT" && (deptCode == "CW" || user.currentCompanyQx || deptCode == "GL")){
            requestData(null,"/dept/getRootDept","POST","json",false,function (result) {
                var root = result.data.root;
                if (root){
                    deptId = root.id;//整个集团的业务和媒介部
                    $("#deptName").val(root.name);
                }else{
                    deptId = 517;//整个集团的业务和媒介部
                    $("#deptName").val("集团总经办");
                }
            });
        }else if(deptCode == "CW" || user.currentCompanyQx || deptCode == "GL"){
            requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST","json",false,function (result) {
                var company = result.data.company;
                if (company){
                    deptId = company.id;//整个集团的业务和媒介部
                    $("#deptName").val(company.name);
                }
            });
        }else{
            $("#deptName").val(user.dept.name);
        }
        return deptId ;
    },
    loadDept:function(){
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        var deptDiv = document.getElementById("deptDiv");
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且只允许财务员工（催款员）
        if(((currentDeptQx || currentCompanyQx) && (user.dept.code == 'YW' || user.dept.code == 'GL'|| user.dept.code == 'CW'))||(user.dept.code=='CW'&&user.dept.type=='YG')||hasRoleCWYG()){
            deptDiv.style.display = 'block';

            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData()],
                onNodeSelected: function (event, data) {
                    $("#deptModal").modal('hide');
                    $("#currentDeptId").val(data.id);
                    deptId = data.id ;
                    $("#deptName").val(data.text);
                    searchForm.loadWorker(data.id,"YW"); //查询业务员工
                    layui.use('form', function(){
                        layui.form.render('select');//layui重新渲染下拉列表
                    });
                    $("#querySearch").trigger("click");
                }
            });
            $("#cleanDept").click(function () {
                $("#currentUserId").empty();//初始化
                $("#currentUserId").append('<option value="">业务员</option>');
                $("#currentDeptId").val("");
                deptId = searchForm.getDeptId() ;
                $("#deptName").val("");
                searchForm.loadWorker(deptId,"YW"); //查询业务员工
                layui.use('form', function(){
                    layui.form.render('select');//layui重新渲染下拉列表
                });
                $("#querySearch").trigger("click");
            });
        }
    },
    //加载此部门下的业务员
    loadWorker: function(deptId,roleType,userId){
        var ele = $("#currentUserId");
        ele.empty();
        ele.append('<option value="">业务员</option>');
        //如果没有部门权限 和 公司权限，则只加载当前用户,财务员工（催款员）除外
        if(!user.currentDeptQx && !user.currentCompanyQx && !hasRoleCWYG()){
            ele.append("<option value="+user.id+" selected>"+user.name+"</option>");
        }else {
            if(roleType){
                searchForm.loadDeptUser(deptId,roleType,"currentUserId",userId);
            }
        }
    },
    loadDeptUser: function (deptId, roleType, attr,userId) {
        deptId = deptId || "";
        var attribute = attr || 'users';
        layui.use(['form'], function () {
            Views.layuiForm = layui.form;
            var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
            $.ajax({
                    url: baseUrl + "/user/listUserByDeptAndRole",
                    type: "post",
                    data: {deptId: deptId, roleType: roleType},
                    async: false,
                    dataType: "json",
                    success: function (users) {
                        var userList = users.data.list;
                        if(userList && userList.length > 0){
                            for (var i = 0; i < userList.length; i++) {
                                if(userList[i].id==userId){
                                    ele.append("<option value=" + userList[i].id + " selected='true'>" + userList[i].name + "</option>");
                                }else{
                                    ele.append("<option value=" + userList[i].id + ">" + userList[i].name + "</option>");
                                }
                            }
                            Views.layuiForm.render();
                        }
                    }
                }
            );
        });
    },
};

//获取部门树数据
function getTreeData(isZC) {
    var deptTreeData = {};
    //具体查询
    requestData({deptId: deptId,deptCode:'YW'},"/dept/listAllDeptByIdAndCode","post","json",false,function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}
/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,dataType,async,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun
    });
};
$(document).ready(function () {
    searchForm.init();//初始化部门树

    var myDate = new Date();
    var year = myDate.getFullYear();
    $("#year").val(year);//获取当前年

    var userId = getQueryString("userId");
    searchForm.loadWorker(deptId,"YW",userId);
    layui.use('form', function(){
        layui.form.render('select');//layui重新渲染下拉列表
    });

    var project = projectDirector();
    if( project && project.contains(user.id)){
        XMflag =true;
    }else {
        XMflag =false;
    }
    if(hasRoleYWYG()){
        $(".hideForYWYG").hide();
    }else{
        $(".hideForYWYG").show();
    }

    $('#queryDiv').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            $("#querySearch").click();
        }
    });
    $("#queryForm [name='year']").change(function () {
        $("#querySearch").trigger("click");
    });
    var param = $("#queryForm").serializeJson() ;
    if(userId!=null&&userId!=""&&userId!=undefined){
        param['currentUserId']=userId;
    }
    aggregateAmount(param);
    initData(param);
    $("#querySearch").click(function () {
        var param = $("#queryForm").serializeJson();
        $.jgrid.gridUnload("#query_table_logs");
        initData(param);
        aggregateAmount(param);
    });

    $("#exportBtn").click(function () {
        var params =  removeBlank($("#queryForm").serializeJson());
        location.href = "/businessStatistics/exportNotIncome" + "?" + $.param(params);
    });

    $("#detail_table_log").jqGrid({
        url: baseUrl + '/businessStatistics/queryNotIncomeYear',
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
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'deptName', label: '部门名称', editable: true, width: 100,
                formatter: function (value, grid, rows) {
                    var html = "";
                    html += rows.deptName + "(";
                    html += rows.companyCode ;
                    html += ")";
                    return html;
                },
            },
            {name: 'userName', label: '业务员', editable: true, width: 120},
            {name: 'last_sale', label: '业绩', editable: true, width: 120, formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."}},
            {name: 'last_profit',label:'利润',editable:true,width:120, formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."}},
            {name: 'last_notIncome',label:'未到款',editable:true,width:120, formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."}},
            {name: 'last_yqIncome',label:'逾期款',editable:true,width:120, formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."}},
            {name: 'this_sale', label: '业绩', editable: true, width: 120, formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."}},
            {name: 'this_profit',label:'利润',editable:true,width:120, formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."}},
            {name: 'this_notIncome',label:'未到款',editable:true,width:120, formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."}},
            {name: 'this_yqIncome',label:'逾期款',editable:true,width:120, formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."}},
        ],
        // gridComplete: function () {
        //     var width = $('#detail_table_log').closest('.jqGrid_wrapper').width()
        //     $('#detail_table_log').setGridWidth(width);
        // },
        pager: jQuery("#detail_pager_log"),
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });
    $("#detail_table_log").jqGrid('setGroupHeaders', {
        useColSpanStyle: true,
        groupHeaders: [
            { startColumnName: 'last_sale', numberOfColumns: 4, titleText: '去年' },
            { startColumnName: 'this_sale', numberOfColumns: 4, titleText: '今年' },
        ]
    });

    $("#exportDetailBtn").click(function () {
        var params =  removeBlank($("#detailForm").serializeJson());
        location.href = "/businessStatistics/exportNotIncomeDetail" + "?" + $.param(params);
    });
    $('#detailDiv').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            $("#detailSearch").click();
        }
    });
    $("#detailSearch").click(function () {
        $("#detail_table_log").emptyGridParam();
        $("#detail_table_log").jqGrid('setGridParam', {
            postData: $("#detailForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
});


var qtl;

function initData(param) {
    var monthStart = $("#monthStart").val();
    var monthEnd = $("#monthEnd").val();
    if(monthStart==""||monthStart==undefined||monthStart==null){
        swal("开始月份不能为空！");
        return;
    }
    if(monthEnd==""||monthEnd==undefined||monthEnd==null){
        swal("结束月份不能为空！");
        return;
    }
    if(parseInt(monthEnd)<parseInt(monthStart)){
        swal("结束月份不能小于开始月份！");
        return;
    }
    var colNames = new Array();
    var colModels = new Array();
    var groupHeaders = new Array();
    colNames.push('部门名称');
    colNames.push('业务员');
    colNames.push('客户公司');
    colNames.push('客户对接人');
    colNames.push('用户id');
    colNames.push('对接人隐藏域');
    colModels.push({name: 'deptName', label: '部门名称', editable: false, width: 100,frozen: true,
        formatter: function (value, grid, rows) {
            var html = "";
            html += rows.deptName + "(";
            html += rows.companyCode ;
            html += ")";
            return html;
        },
    });
    colModels.push({name: 'userName', label: '业务员', editable: false, width: 100, frozen: true});
    colModels.push({name: 'companyName', label: '客户公司', editable: false, width: 100,frozen: true});
    colModels.push({name: 'custName1', label: '客户对接人', editable: false, width: 100, frozen: true,align:"center",
        formatter: function (value, grid, rows) {
            var html = "";
            if(rows.userId == user.id){
                if(value==undefined||value==""||value==null){
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='openDetail("+JSON.stringify(rows)+",4)'>"+rows.custName + "</a>";
                }else{
                    html += value ;
                }
            }else {
                if (XMflag){
                    if(value==undefined||value==""||value==null) {
                        return "*****"
                    }else {
                        html += value ;
                    }
                }else {
                    if(value==undefined||value==""||value==null){
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='openDetail("+JSON.stringify(rows)+",4)'>"+rows.custName + "</a>";
                    }else{
                        html += value ;
                    }
                }
            }

            return html;
        },
    });
    colModels.push({name: 'userId', label: '业务员id', editable: false, width: 100, hidden:true});
    colModels.push({name: 'custName', label: '对接人隐藏域', editable: false, width: 100, hidden:true});
    for (var i = parseInt(monthStart); i <= parseInt(monthEnd); i++) {
        colNames.push('业绩');
        colNames.push('利润');
        colNames.push('未到款');
        colNames.push('逾期款');
        colModels.push({
            name: 'sale_' + i, label: '业绩', editable: true, width: 100, align: 'center', formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        colModels.push({
            name: 'profit_' + i, label: '利润', editable: true, width: 100, align: 'center', formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        colModels.push({
            name: 'notIncome_' + i, label: '未到款', editable: true, width: 100, align: 'center', formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        colModels.push({
            name: 'yqIncome_' + i,
            label: '逾期款',
            editable: true,
            width: 100,
            classes: 'text-danger',
            align: 'center',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        groupHeaders.push({startColumnName: 'sale_' + i, numberOfColumns: 4, titleText: i + '月'})
    }

    colNames.push('业绩');
    colNames.push('利润');
    colNames.push('未到款');
    colNames.push('逾期款');
    colModels.push({
        name: 'sale_sum', label: '业绩', editable: true, width: 100, align: 'center', formatter: "currency",
        formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    });
    colModels.push({
        name: 'profit_sum', label: '利润', editable: true, width: 100, align: 'center', formatter: "currency",
        formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    });
    colModels.push({
        name: 'notIncome_sum', label: '未到款', editable: true, width: 100, align: 'center', formatter: "currency",
        formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    });
    colModels.push({
        name: 'yqIncome_sum',
        label: '逾期款',
        editable: true,
        width: 100,
        classes: 'text-danger',
        align: 'center',
        formatter: "currency",
        formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    });
    groupHeaders.push({startColumnName: 'sale_sum', numberOfColumns: 4, titleText: '合计'})
    // console.log(JSON.stringify(groupHeaders))
    // console.log(JSON.stringify(colNames))
    qtl = $("#query_table_logs").jqGrid({
        url: baseUrl + '/businessStatistics/queryNotIncome',
        datatype: "json",
        mtype: 'POST',
        postData: param, //发送数据
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
        autoScroll: true,
        // scroll:true,
        shrinkToFit: false,
        prmNames: {rows: "size"},
        rowNum: 15,//每页显示记录数
        rowList: [15, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: colNames,
        colModel: colModels,
        gridComplete: function () {
            $("#query_table_logs").closest(".ui-jqgrid-bdiv").css({"overflow-x": "scroll"});
            var width = $('#query_table_logs').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#query_table_logs').setGridWidth(width);

            var rows = $("#query_table_logs").jqGrid("getRowData");

            var sale_1 = 0;
            var notIncome_1 = 0;
            var yqIncome_1 = 0;
            var profit_1 = 0;
            var sale_2 = 0;
            var notIncome_2 = 0;
            var yqIncome_2 = 0;
            var profit_2 = 0;
            var sale_3 = 0;
            var notIncome_3 = 0;
            var yqIncome_3 = 0;
            var profit_3 = 0;
            var sale_4 = 0;
            var notIncome_4 = 0;
            var yqIncome_4 = 0;
            var profit_4 = 0;
            var sale_5 = 0;
            var notIncome_5 = 0;
            var yqIncome_5 = 0;
            var profit_5 = 0;
            var sale_6 = 0;
            var notIncome_6 = 0;
            var yqIncome_6 = 0;
            var profit_6 = 0;
            var sale_7 = 0;
            var notIncome_7 = 0;
            var yqIncome_7 = 0;
            var profit_7 = 0;
            var sale_8 = 0;
            var notIncome_8 = 0;
            var yqIncome_8 = 0;
            var profit_8 = 0;
            var sale_9 = 0;
            var notIncome_9 = 0;
            var yqIncome_9 = 0;
            var profit_9 = 0;
            var sale_10 = 0;
            var notIncome_10 = 0;
            var yqIncome_10 = 0;
            var profit_10 = 0;
            var sale_11 = 0;
            var notIncome_11 = 0;
            var yqIncome_11 = 0;
            var profit_11 = 0;
            var sale_12 = 0;
            var notIncome_12 = 0;
            var yqIncome_12 = 0;
            var profit_12 = 0;
            var sale_sum = 0;
            var notIncome_sum = 0;
            var yqIncome_sum = 0;
            var profit_sum = 0;
            for (var i = 0, l = rows.length; i < l; i++) {
                sale_1 += (rows[i].sale_1 - 0);
                notIncome_1 += (rows[i].notIncome_1 - 0);
                yqIncome_1 += (rows[i].yqIncome_1 - 0);
                profit_1 += (rows[i].profit_1 - 0);
                sale_2 += (rows[i].sale_2 - 0);
                notIncome_2 += (rows[i].notIncome_2 - 0);
                yqIncome_2 += (rows[i].yqIncome_2 - 0);
                profit_2 += (rows[i].profit_2 - 0);
                sale_3 += (rows[i].sale_3 - 0);
                notIncome_3 += (rows[i].notIncome_3 - 0);
                yqIncome_3 += (rows[i].yqIncome_3 - 0);
                profit_3 += (rows[i].profit_3 - 0);
                sale_4 += (rows[i].sale_4 - 0);
                notIncome_4 += (rows[i].notIncome_4 - 0);
                yqIncome_4 += (rows[i].yqIncome_4 - 0);
                profit_4 += (rows[i].profit_4 - 0);
                sale_5 += (rows[i].sale_5 - 0);
                notIncome_5 += (rows[i].notIncome_5 - 0);
                yqIncome_5 += (rows[i].yqIncome_5 - 0);
                profit_5 += (rows[i].profit_5 - 0);
                sale_6 += (rows[i].sale_6 - 0);
                notIncome_6 += (rows[i].notIncome_6 - 0);
                yqIncome_6 += (rows[i].yqIncome_6 - 0);
                profit_6 += (rows[i].profit_6 - 0);
                sale_7 += (rows[i].sale_7 - 0);
                notIncome_7 += (rows[i].notIncome_7 - 0);
                yqIncome_7 += (rows[i].yqIncome_7 - 0);
                profit_7 += (rows[i].profit_7 - 0);
                sale_8 += (rows[i].sale_8 - 0);
                notIncome_8 += (rows[i].notIncome_8 - 0);
                yqIncome_8 += (rows[i].yqIncome_8 - 0);
                profit_8 += (rows[i].profit_8 - 0);
                sale_9 += (rows[i].sale_9 - 0);
                notIncome_9 += (rows[i].notIncome_9 - 0);
                yqIncome_9 += (rows[i].yqIncome_9 - 0);
                profit_9 += (rows[i].profit_9 - 0);
                sale_10 += (rows[i].sale_10 - 0);
                notIncome_10 += (rows[i].notIncome_10 - 0);
                yqIncome_10 += (rows[i].yqIncome_10 - 0);
                profit_10 += (rows[i].profit_10 - 0);
                sale_11 += (rows[i].sale_11 - 0);
                notIncome_11 += (rows[i].notIncome_11 - 0);
                yqIncome_11 += (rows[i].yqIncome_11 - 0);
                profit_11 += (rows[i].profit_11 - 0);
                sale_12 += (rows[i].sale_12 - 0);
                notIncome_12 += (rows[i].notIncome_12 - 0);
                yqIncome_12 += (rows[i].yqIncome_12 - 0);
                profit_12 += (rows[i].profit_12 - 0);
                sale_sum += (rows[i].sale_sum - 0);
                notIncome_sum += (rows[i].notIncome_sum - 0);
                yqIncome_sum += (rows[i].yqIncome_sum - 0);
                profit_sum += (rows[i].profit_sum - 0);
            }
            $("#query_table_logs").jqGrid("footerData", "set", {
                custName1: "合计",
                sale_1,
                sale_1,
                notIncome_1,
                notIncome_1,
                yqIncome_1,
                yqIncome_1,
                profit_1,
                profit_1,
                sale_2,
                sale_2,
                notIncome_2,
                notIncome_2,
                yqIncome_2,
                yqIncome_2,
                profit_2,
                profit_2,
                sale_3,
                sale_3,
                notIncome_3,
                notIncome_3,
                yqIncome_3,
                yqIncome_3,
                profit_3,
                profit_3,
                sale_4,
                sale_4,
                notIncome_4,
                notIncome_4,
                yqIncome_4,
                yqIncome_4,
                profit_4,
                profit_4,
                sale_5,
                sale_5,
                notIncome_5,
                notIncome_5,
                yqIncome_5,
                yqIncome_5,
                profit_5,
                profit_5,
                sale_6,
                sale_6,
                notIncome_6,
                notIncome_6,
                yqIncome_6,
                yqIncome_6,
                profit_6,
                profit_6,
                sale_7,
                sale_7,
                notIncome_7,
                notIncome_7,
                yqIncome_7,
                yqIncome_7,
                profit_7,
                profit_7,
                sale_8,
                sale_8,
                notIncome_8,
                notIncome_8,
                yqIncome_8,
                yqIncome_8,
                profit_8,
                profit_8,
                sale_9,
                sale_9,
                notIncome_9,
                notIncome_9,
                yqIncome_9,
                yqIncome_9,
                profit_9,
                profit_9,
                sale_10,
                sale_10,
                notIncome_10,
                notIncome_10,
                yqIncome_10,
                yqIncome_10,
                profit_10,
                profit_10,
                sale_11,
                sale_11,
                notIncome_11,
                notIncome_11,
                yqIncome_11,
                yqIncome_11,
                profit_11,
                profit_11,
                sale_12,
                sale_12,
                notIncome_12,
                notIncome_12,
                yqIncome_12,
                yqIncome_12,
                profit_12,
                profit_12,
                sale_sum,
                sale_sum,
                notIncome_sum,
                notIncome_sum,
                yqIncome_sum,
                yqIncome_sum,
                profit_sum,
                profit_sum
            });
        },
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        footerrow: true,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            var rowData = jQuery("#query_table_logs").jqGrid("getRowData", rowid);
            view(rowData.userId,rowData.companyName, rowData.custName);
        }
    });
    $("#query_table_logs").jqGrid('setGroupHeaders', {
        useColSpanStyle: true,
        groupHeaders: groupHeaders
    });
    $("#query_table_logs").jqGrid('setFrozenColumns');
}

function aggregateAmount(param) {
    $("#tj").find(".text-danger").html(0);
    $.ajax({
        type:"post",
        data:param,
        url:baseUrl+"/businessStatistics/querySumNotIncome",
        dataType:"json",
        async:false,
        success:function (resDate) {
            if (resDate){
                $("#saleSum").text(fmtMoneyBringUnit(resDate.sale) || 0);
                $("#notIncomeSum").text(fmtMoneyBringUnit(resDate.notIncome) || 0);
                $("#yqIncomeSum").text(fmtMoneyBringUnit(resDate.yqIncome) || 0);
                $("#profitSum").text(fmtMoneyBringUnit(resDate.profit) || 0);
            }
        }
    })
}

function view(userId,companyName,custName) {
    if(hasRoleYW()){//业务部跳转到业务查询
        page("/biz/business_list?userId="+userId+"&companyName="+companyName+"&custName="+custName+"&incomeState=0&completeStatus=1","业务查询");
    }else if(hasRoleCWYG()){//稿件管理（催款员）
        page("/biz/article_listCSY?userId="+userId+"&companyName="+companyName+"&custName="+custName+"&incomeState=0&completeStatus=1","稿件管理（催款员）");
    }else if(hasRoleJT()){//稿件管理（集团）
        page("/biz/article_list?userId="+userId+"&companyName="+companyName+"&custName="+custName+"&incomeState=0&completeStatus=1","稿件管理（集团）");
    }else{//稿件管理（子公司）
        page("/biz/article_list?userId="+userId+"&companyName="+companyName+"&custName="+custName+"&incomeState=0&completeStatus=1","稿件管理（子公司）");
    }
}

function openDetail(row,type) {
    var year = $("#year").val() ;
    if(year == undefined || year == "" || year == null){
        swal("获取年限不正确，获取到的年限为："+year);
        return ;
    }
    if(row.userName == undefined || row.userName == "" || row.userName == null){
        swal("获取业务员不正确，获取到的业务员为："+row.userName);
        return ;
    }
    if(type == undefined || type == "" || type == null){
        swal("获取类型不正确，获取到的类型为："+type);
        return ;
    }
    if(row.companyCode == undefined || row.companyCode == "" || row.companyCode == null){
        swal("获取公司代码不正确，获取到的公司代码为："+row.companyCode);
        return ;
    }
    $("#chooseYear").html(year);
    $("#detailForm [name='year']").val(year);
    $("#detailForm [name='type']").val(type);
    $("#detailForm [name='userName']").val(row.userName);
    $("#detailForm [name='deptNameYear']").val(row.deptName);
    $("#detailForm [name='companyName']").val(row.companyName);
    $("#detailForm [name='custName']").val(row.custName);
    $("#detailForm [name='companyCode']").val(row.companyCode);
    var param = $("#detailForm").serializeJson();
    $("#detailModal").modal({backdrop: "static"});
    $("#detail_table_log").emptyGridParam();
    $("#detail_table_log").jqGrid('setGridParam', {
        datatype: 'json',
        postData: param, //发送数据
    }).trigger("reloadGrid"); //重新载入
    resize("#detail_table_log");
}