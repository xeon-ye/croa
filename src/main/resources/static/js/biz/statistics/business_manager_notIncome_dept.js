var deptId = user.dept.id;//当前用户部门ID
var searchForm = {
    init:function (param) {
        searchForm.getDeptId();
        searchForm.loadDept(param);
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
    loadDept:function(param){
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        if((currentDeptQx || currentCompanyQx) && (user.dept.code == 'YW' || user.dept.code == 'GL'|| user.dept.code == 'CW')){
            document.getElementById("deptDiv").style.display = 'block';

            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData(param)],
                onNodeSelected: function (event, data) {
                    $("#deptModal").modal('hide');
                    $("#currentDeptId").val(data.id);
                    $("#deptName").val(data.text);
                    $("#querySearch").trigger("click");
                }
            });
            $("#cleanDept").click(function () {
                $("#currentDeptId").val("");
                $("#deptName").val("");
                $("#querySearch").trigger("click");
            });
        }
    },

};

//获取部门树数据
function getTreeData(param) {
    var deptTreeData = {};
    //具体查询
    requestData({deptId: deptId,deptCode:'YW',level:4},"/dept/listAllDeptByIdAndCodeAndLevel","post","json",false,function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
        if(deptTreeData.nodes.length>0){
            var parentNodes = deptTreeData.nodes[0].nodes ;
            for(var i=0;i<parentNodes.length;i++){
                var parentNode = parentNodes[i] ;
                if(parentNode.id==param){
                    $("#currentDeptId").val(parentNode.id);
                    $("#deptName").val(parentNode.text);
                    $("#querySearch").trigger("click");
                    break;
                }
                //子部门
                if(parentNode.nodes.length>0){
                    var childNodes = parentNode.nodes ;
                    for(var j=0;j<childNodes.length;j++){
                        var childNode = childNodes[j];
                        if(childNode.id==param){
                            $("#currentDeptId").val(childNode.id);
                            $("#deptName").val(childNode.text);
                            $("#querySearch").trigger("click");
                            break;
                        }
                    }
                }
            }
        }
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
    // 在模态框出现后添加可拖拽功能
    $(document).on("show.bs.modal", "#detailModal", function() {
        // draggable 属性规定元素是否可拖动
        $(this).draggable({
            handle: ".modal-header", // 只能点击头部拖动
            cursor: "move"
        });
        $(this).css("overflow", "hidden"); // 防止出现滚动条，出现的话，你会把滚动条一起拖着走的
    });

    initYearSelect();//初始化年下拉选项
    var myDate = new Date();
    var year = myDate.getFullYear();
    $("#year").val(year);//获取当前年

    $("#ywb").click(function () {
        $("#queryForm [name='type']").val(1);
        $("#ywb").removeClass("btn-white");
        $("#ywb").addClass("btn-outline btn-primary");
        $("#ywz").removeClass("btn-outline btn-primary");
        $("#ywz").addClass("btn-white");
        document.getElementById("deptDiv").style.display = 'block';
        document.getElementById("deptDiv1").style.display = 'none';
        $("#querySearch").trigger("click");
    });
    $("#ywz").click(function () {
        $("#queryForm [name='type']").val(2);
        $("#ywz").removeClass("btn-white");
        $("#ywz").addClass("btn-outline btn-primary");
        $("#ywb").removeClass("btn-outline btn-primary");
        $("#ywb").addClass("btn-white");
        document.getElementById("deptDiv").style.display = 'none';
        document.getElementById("deptDiv1").style.display = 'block';
        $("#querySearch").trigger("click");
    });


    $("#queryForm [name='year']").change(function () {
        $("#querySearch").trigger("click");
    });

    $('#queryDiv').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            $("#querySearch").click();
        }
    });
    searchForm.init();

    reflushTreeTable();
    $("#querySearch").click(function () {
        $.jgrid.gridUnload("#query_table_logs");
        reflushTreeTable();
    });

    $("#exportBtn").click(function () {
        var params =  removeBlank($("#queryForm").serializeJson());
        location.href = "/businessStatistics/exportNotIncome" + "?" + $.param(params);
    });

    $("#exportDetailBtn").click(function () {
        var params =  removeBlank($("#detailForm").serializeJson());
        location.href = "/businessStatistics/exportNotIncomeDetail" + "?" + $.param(params);
    });
});

//初始化年下拉选项
function initYearSelect() {
    var currentYear = new Date().getFullYear();
    var yearHtml = "";
    for(var y = 2018; y < currentYear + 6; y++){
        yearHtml += " <option value=\""+y+"\">"+y+"</option>";
    }
    $("select[name='year']").html(yearHtml);
}

function initTreeTable() {
    var monthStart = $("#monthStart").val();
    var monthEnd = $("#monthEnd").val();
    if(!monthStart){
        swal("开始月份不能为空！");
        return;
    }
    if(!monthEnd){
        swal("结束月份不能为空！");
        return;
    }
    if(parseInt(monthEnd)<parseInt(monthStart)){
        swal("结束月份不能小于开始月份！");
        return;
    }
    var colModels = new Array();
    var groupHeaders = new Array();
    colModels.push({name: 'id', label: '部门id', editable: true, hidden: true});
    colModels.push({name: 'companyCode', label: 'companyCode', editable: true, hidden: true});
    for (var m = parseInt(monthStart); m <= parseInt(monthEnd); m++) {
        colModels.push({name: "selfSale_"+m, label: "selfSale_"+m, editable: true, hidden: true});
        colModels.push({name: "selfNotIncome_"+m, label: "selfNotIncome_"+m, editable: true, hidden: true});
        colModels.push({name: "selfYqIncome_"+m, label: "selfYqIncome_"+m, editable: true, hidden: true});
        colModels.push({name: "selfProfit_"+m, label: "selfProfit_"+m, editable: true, hidden: true});
    }
    colModels.push({name: 'selfSale_sum', label: 'selfSale_sum', editable: true, hidden: true});
    colModels.push({name: 'selfNotIncome_sum', label: 'selfNotIncome_sum', editable: true, hidden: true});
    colModels.push({name: 'selfYqIncome_sum', label: 'selfYqIncome_sum', editable: true, hidden: true});
    colModels.push({name: 'selfProfit_sum', label: 'selfProfit_sum', editable: true, hidden: true});
    colModels.push({
        name: 'deptName', label: '部门名称', editable: false, width: 200, frozen: true, align: 'center',sortable:false,
        formatter: function (value, grid, rows) {
            var html = "";
            if(rows.companyCode){
                html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='openDetail(" + JSON.stringify(rows) + ",2)'>" + rows.deptName + "<span>(" + rows.companyCode + ")</span></a>";
            }else {
                html += value;
            }
            return html;
        },
    });
    for (var i = parseInt(monthStart); i <= parseInt(monthEnd); i++) {
        colModels.push({
            name: 'sale_' + i, label: '业绩', editable: true,align: 'center', formatter: "currency",sortable:false,
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        colModels.push({
            name: 'profit_' + i, label: '利润', editable: true, align: 'center', formatter: "currency",sortable:false,
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        colModels.push({
            name: 'notIncome_' + i, label: '未到款', editable: true,  align: 'center', formatter: "currency",sortable:false,
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        colModels.push({
            name: 'yqIncome_' + i,
            label: '逾期款',
            editable: true,
            classes: 'text-danger',
            align: 'center',
            formatter: "currency",
            sortable:false,
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        groupHeaders.push({startColumnName: 'sale_' + i, numberOfColumns: 4, titleText: i + '月'})
    }
    colModels.push({
        name: 'sale_sum', label: '业绩', editable: true,  align: 'center', formatter: "currency",sortable:false,
        formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    });
    colModels.push({
        name: 'profit_sum', label: '利润', editable: true, align: 'center', formatter: "currency",sortable:false,
        formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    });
    colModels.push({
        name: 'notIncome_sum', label: '未到款', editable: true,  align: 'center', formatter: "currency",sortable:false,
        formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    });
    colModels.push({
        name: 'yqIncome_sum',
        label: '逾期款',
        editable: true,
        classes: 'text-danger',
        align: 'center',
        formatter: "currency",
        sortable:false,
        formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    });
    groupHeaders.push({startColumnName: 'sale_sum', numberOfColumns: 4, titleText: '合计'})
    $("#query_table_logs").jqGrid({
        treeGrid: true,
        treeGridModel: 'adjacency', //treeGrid模式，跟json元数据有关
        ExpandColumn : 'deptName',
        scroll: "true",
        datatype: "local",
        colModel: colModels,
        pager: "false",
        sortname: 'id',
        sortorder: "desc",
        treeReader : {
            level_field: "level",
            parent_id_field: "parent",
            leaf_field: "isLeaf",
            expanded_field: "expanded"
        },
        caption: "",
        mtype: "POST",
        height: "auto",    // 设为具体数值则会根据实际记录数出现垂直滚动条
        rowNum : "-1",     // 显示全部记录
        shrinkToFit: false,
        footerrow: true,
        gridComplete: function () {
            $("#query_table_logs").closest(".ui-jqgrid-bdiv").css({"overflow-x": "scroll"});
            var width = $('#query_table_logs').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#query_table_logs').setGridWidth(width);

            var rows = $("#query_table_logs").jqGrid("getRowData");

            var footerData = {deptName: "合计"};
            for (var m = parseInt(monthStart); m <= parseInt(monthEnd); m++) {
                footerData["sale_"+m] = 0;
                footerData["notIncome_"+m] = 0;
                footerData["yqIncome_"+m] = 0;
                footerData["profit_"+m] = 0;
            }
            footerData["sale_sum"] = 0;
            footerData["notIncome_sum"] = 0;
            footerData["yqIncome_sum"] = 0;
            footerData["profit_sum"] = 0;
            for (var i = 0, l = rows.length; i < l; i++) {
                for (var m = parseInt(monthStart); m <= parseInt(monthEnd); m++) {
                    footerData["sale_"+m] = parseFloat(footerData["sale_"+m]) + parseFloat((rows[i]["selfSale_"+m] || 0));
                    footerData["notIncome_"+m] = parseFloat(footerData["notIncome_"+m]) + parseFloat((rows[i]["selfNotIncome_"+m] || 0));
                    footerData["yqIncome_"+m] = parseFloat(footerData["yqIncome_"+m]) + parseFloat((rows[i]["selfYqIncome_"+m] || 0));
                    footerData["profit_"+m] = parseFloat(footerData["profit_"+m]) + parseFloat((rows[i]["selfProfit_"+m] || 0));
                }
                footerData["sale_sum"] = parseFloat(footerData["sale_sum"]) + parseFloat((rows[i]["selfSale_sum"] || 0));
                footerData["notIncome_sum"] = parseFloat(footerData["notIncome_sum"]) + parseFloat((rows[i]["selfNotIncome_sum"] || 0));
                footerData["yqIncome_sum"] = parseFloat(footerData["yqIncome_sum"]) + parseFloat((rows[i]["selfYqIncome_sum"] || 0));
                footerData["profit_sum"] = parseFloat(footerData["profit_sum"]) + parseFloat((rows[i]["selfProfit_sum"] || 0))
            }

            //设置顶部金额合计
            $("#saleSum").text(fmtMoneyBringUnit(footerData["sale_sum"] || 0));
            $("#notIncomeSum").text(fmtMoneyBringUnit(footerData["notIncome_sum"] || 0));
            $("#yqIncomeSum").text(fmtMoneyBringUnit(footerData["yqIncome_sum"] || 0));
            $("#profitSum").text(fmtMoneyBringUnit(footerData["profit_sum"] || 0));

            $("#query_table_logs").jqGrid("footerData", "set", footerData);
        },
        ondblClickRow: function (rowid, iRow, iCol, e) {
            var rowData = jQuery("#query_table_logs").jqGrid("getRowData", rowid);
            if(rowData.id){
                view(rowData.id);
            }
        }
    });
    $("#query_table_logs").jqGrid('setGroupHeaders', {
        useColSpanStyle: true,
        groupHeaders: groupHeaders
    });
    $("#query_table_logs").jqGrid('setFrozenColumns');
    $("#query_table_logs").setGridHeight(460);
    var width = $('#query_table_logs').closest('.jqGrid_wrapper').width() || $(document).width();
    $('#query_table_logs').setGridWidth(width);
}
function reflushTreeTable() {
    initTreeTable();
    requestData($("#queryForm").serializeJson(), "/businessStatistics/queryNotIncome", "post", "json", false, function (data) {
        var reader = {
            root: function(obj) { return data.list; }
        };
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").setGridParam({data: data.list, reader: reader}).trigger('reloadGrid');
    })
}

function initTreeTableYear(thisYear) {
    var lastYear =  thisYear - 1;
    var colModels = new Array();
    var groupHeaders = new Array();
    colModels.push({name: 'id', label: '部门id', editable: true, hidden: true});
    colModels.push({name: 'companyCode', label: 'companyCode', editable: true, hidden: true});
    for (var m = lastYear; m <= thisYear; m++) {
        colModels.push({name: "selfSale_"+m, label: "selfSale_"+m, editable: true, hidden: true});
        colModels.push({name: "selfNotIncome_"+m, label: "selfNotIncome_"+m, editable: true, hidden: true});
        colModels.push({name: "selfYqIncome_"+m, label: "selfYqIncome_"+m, editable: true, hidden: true});
        colModels.push({name: "selfProfit_"+m, label: "selfProfit_"+m, editable: true, hidden: true});
    }
    colModels.push({
        name: 'deptName', label: '部门名称', editable: false, width: 200, frozen: true, align: 'center',sortable:false,
        formatter: function (value, grid, rows) {
            var html = "";
            if(rows.companyCode){
                html += rows.deptName + "("+rows.companyCode+")";
            }else {
                html += value;
            }
            return html;
        },
    });
    for (var i = lastYear; i <= thisYear; i++) {
        colModels.push({
            name: 'sale_' + i, label: '业绩', editable: true,align: 'center', formatter: "currency",sortable:false,
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        colModels.push({
            name: 'profit_' + i, label: '利润', editable: true, align: 'center', formatter: "currency",sortable:false,
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        colModels.push({
            name: 'notIncome_' + i, label: '未到款', editable: true,  align: 'center', formatter: "currency",sortable:false,
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        colModels.push({
            name: 'yqIncome_' + i,
            label: '逾期款',
            editable: true,
            classes: 'text-danger',
            align: 'center',
            formatter: "currency",
            sortable:false,
            formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
        });
        groupHeaders.push({startColumnName: 'sale_' + i, numberOfColumns: 4, titleText: i+"年"})
    }
    $("#detail_table_log").jqGrid({
        treeGrid: true,
        treeGridModel: 'adjacency', //treeGrid模式，跟json元数据有关
        ExpandColumn : 'deptName',
        scroll: "true",
        datatype: "local",
        colModel: colModels,
        pager: "false",
        sortname: 'id',
        sortorder: "desc",
        treeReader : {
            level_field: "level",
            parent_id_field: "parent",
            leaf_field: "isLeaf",
            expanded_field: "expanded"
        },
        caption: "",
        mtype: "POST",
        height: "auto",    // 设为具体数值则会根据实际记录数出现垂直滚动条
        rowNum : "-1",     // 显示全部记录
        shrinkToFit: false,
        footerrow: true,
        gridComplete: function () {
            $("#detail_table_log").closest(".ui-jqgrid-bdiv").css({"overflow-x": "scroll"});
            var width = $('#detail_table_log').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#detail_table_log').setGridWidth(width);

            var rows = $("#detail_table_log").jqGrid("getRowData");

            var footerData = {deptName: "合计"};
            for (var m = lastYear; m <= thisYear; m++) {
                footerData["sale_"+m] = 0;
                footerData["notIncome_"+m] = 0;
                footerData["yqIncome_"+m] = 0;
                footerData["profit_"+m] = 0;
            }
            for (var i = 0, l = rows.length; i < l; i++) {
                for (var m = lastYear; m <= thisYear; m++) {
                    footerData["sale_"+m] = parseFloat(footerData["sale_"+m]) + parseFloat((rows[i]["selfSale_"+m] || 0));
                    footerData["notIncome_"+m] = parseFloat(footerData["notIncome_"+m]) + parseFloat((rows[i]["selfNotIncome_"+m] || 0));
                    footerData["yqIncome_"+m] = parseFloat(footerData["yqIncome_"+m]) + parseFloat((rows[i]["selfYqIncome_"+m] || 0));
                    footerData["profit_"+m] = parseFloat(footerData["profit_"+m]) + parseFloat((rows[i]["selfProfit_"+m] || 0));
                }
            }

            $("#detail_table_log").jqGrid("footerData", "set", footerData);
        },
    });
    $("#detail_table_log").jqGrid('destroyGroupHeader');//最关键的一步、销毁合并表头分组、防止出现表头重叠
    $("#detail_table_log").jqGrid('setGroupHeaders', {
        useColSpanStyle: true,
        groupHeaders: groupHeaders
    });
    $("#detail_table_log").jqGrid('setFrozenColumns');
    var width = $('#detail_table_log').closest('.jqGrid_wrapper').width() || $(document).width();
    $('#detail_table_log').setGridWidth(width);
}

function view(deptId) {
    page("/biz/statistics/business_manager_notIncome_user?deptId=" + deptId, "未到款统计(业务员)");
}

function aggregateAmount() {
    $("#tj").find(".text-danger").html(0);
    $.ajax({
        type: "post",
        data: $("#queryForm").serializeJson(),
        url: baseUrl + "/businessStatistics/querySumNotIncome",
        dataType: "json",
        async: false,
        success: function (resDate) {
            if (resDate) {

            }
        }
    })
}

function openDetail(row, type) {
    var year = $("#year").val();
    if (!year) {
        swal("请选择年份");
        return;
    }
    if (!row.id) {
        swal("请选择部门");
        return;
    }
    $("#detailModal").modal("toggle");
    $("#chooseYear").html(year);
    $("#detailForm [name='year']").val(year);
    $("#detailForm [name='currentDeptId']").val(row.id);
    var param = $("#detailForm").serializeJson();
    $("#detailModal .jqGrid_wrapper").html('<table id="detail_table_log"></table>');//再新增一个grid的渲染容器
    initTreeTableYear(parseInt(year));
    requestData(param, "/businessStatistics/queryNotIncomeYear", "post", "json", false, function (data) {
        var reader = {
            root: function(obj) { return data.list; }
        };
        $("#detail_table_log").emptyGridParam();
        $("#detail_table_log").setGridParam({data: data.list, reader: reader}).trigger('reloadGrid');
    })

}