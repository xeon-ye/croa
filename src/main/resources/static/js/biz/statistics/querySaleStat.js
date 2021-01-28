var deptId = user.dept.id;//当前用户部门ID
var deptName = user.dept.name;//当前用户部门ID
var searchForm = {
    init:function () {
        searchForm.getDeptId();
        searchForm.loadDept();
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
                    deptName = root.name;
                }else{
                    deptId = 517;//整个集团的业务和媒介部
                    deptName = "集团";
                }
            });
        }else if(deptCode == "CW" || user.currentCompanyQx || deptCode == "GL"){
            requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST","json",false,function (result) {
                var company = result.data.company;
                if (company){
                    deptId = company.id;//整个集团的业务和媒介部
                    deptName = company.name;
                }
            });
        }
        $("#deptName").val(deptName);
        return deptId ;
    },
    loadDept:function(){
        if(user.dept.code == 'GL'|| user.dept.code == 'CW'){
            document.getElementById("deptDiv").style.display = 'block';
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
    initYearSelect();//初始化年下拉选项

    var myDate = new Date();
    var year = myDate.getFullYear();
    var month = myDate.getMonth();//这个month从0开始计数
    //默认上一个月
    if(month==0){
        year = year - 1;
        month = 12 ;
    }else{
        month = month ;
    }
    $("#year").val(year);
    $("#year1").val(year);
    $("#month").val(month);
    $("#month1").val(month);
    searchForm.init();//初始化部门树

    $("#selDept").click(function () {
        $("#deptModal").modal('toggle');
    });
    $('#treeview').treeview({
        data: [getTreeData()],
        onNodeSelected: function (event, data) {
            $("#deptModal").modal('hide');
            $("#currentDeptId").val(data.id);
            $("#deptName").val(data.text);
            $("#querySearch").trigger("click");
        }
    });
    $("#cleanDept").click(function () {
        $("#currentDeptId").val(deptId);
        $("#deptName").val(deptName);
        $("#querySearch").trigger("click");
    });

    $(".alterBtn").click(function () {
        page("/biz/statistics/queryArticleChange?year="+year+"&month="+month,"异动统计");
    });

    $("#ywb").click(function(){
        $("#queryForm [name='type']").val(1);
        $("#ywb").removeClass("btn-white");
        $("#ywb").addClass("btn-outline btn-primary");
        $("#ywz").removeClass("btn-outline btn-primary");
        $("#ywz").addClass("btn-white");
        document.getElementById("deptDiv").style.display = 'block';
        document.getElementById("deptDiv1").style.display = 'none';
        $("#querySearch").trigger("click");
    });
    $("#ywz").click(function(){
        $("#queryForm [name='type']").val(2);
        $("#ywz").removeClass("btn-white");
        $("#ywz").addClass("btn-outline btn-primary");
        $("#ywb").removeClass("btn-outline btn-primary");
        $("#ywb").addClass("btn-white");
        // $("#currentDeptId").val("");
        document.getElementById("deptDiv").style.display = 'none';
        document.getElementById("deptDiv1").style.display = 'block';
        $("#querySearch").trigger("click");
    });

    $("#queryForm [name='year']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#queryForm [name='month']").change(function () {
        $("#querySearch").trigger("click");
    });

    $('#queryDiv').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            $("#querySearch").click();
        }
    });

    reflushTreeTable();
    // $("#query_table_logs").jqGrid({
    //     url: baseUrl + '/businessStatistics/querySaleStat',
    //     datatype: "json",
    //     mtype: 'POST',
    //     postData: $("#queryForm").serializeJson(), //发送数据
    //     altRows: true,
    //     altclass: 'bgColor',
    //     height: "auto",
    //     autowidth: true,//自动匹配宽度
    //     gridview: true, //加速显示
    //     treeGrid: true,
    //     treeGridModel:"adjacency",
    //     ExpandColumn:"deptName", //我们显示的节点
    //     ExpandColClick:true,//点击展开
    //     shrinkToFit: true,
    //     scroll:true,
    //     rowNum: -1,//每页显示记录数
    //     jsonReader: {
    //         root: "list", repeatitems: false, id: "id"
    //     },
    //     colNames: ['部门id','部门名称', '业绩（含税）', '回款','税金','退款', '其它支出', '成本（不含税）','利润','提成',],
    //     colModel: [
    //         {name: 'id', index:'id', label: 'id', editable: true,hidden:true, width: 200},
    //         {name: 'deptName', label: 'deptName', editable: true, align:'center', width: 200,sortable:false,
    //             formatter: function (value, grid, rows) {
    //                 var html = "";
    //                 if (rows.companyCode != undefined) {
    //                     html += rows.deptName + "(";
    //                     html += rows.companyCode;
    //                     html += ")";
    //                 } else {
    //                     html += value;
    //                 }
    //                 return html;
    //             },
    //         },
    //         {name: 'sale', label: '业绩（含税）', editable: true, width: 200, align:'center', formatter: "currency",
    //             formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    //         },
    //         {name: 'income', label: '回款', editable: true, width: 200, align:'center', formatter: "currency",
    //             formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    //         },
    //         {name: 'tax', label: '税金', editable: true, width: 200, classes: '', align:'center', formatter: "currency",
    //             formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    //         },
    //         {name: 'refund', label: '退款', editable: true, width: 200, align:'center', formatter: "currency",
    //             formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    //         },
    //         {name: 'otherPay', label: '其它支出', editable: true, width: 200, align:'center', formatter: "currency",
    //             formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    //         },
    //         {name: 'outgo', label: '成本（不含税）', editable: true, width: 200, align:'center', formatter: "currency",
    //             formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    //         },
    //         {name: 'profit', label: '利润', editable: true, width: 200, classes: '', align:'center', formatter: "currency",
    //             formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    //         },
    //         {name: 'comm', label: '提成', editable: true, width: 200, align:'center', formatter: "currency",
    //             formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
    //         }
    //     ],
    //     treeReader:{
    //         level_field:"level",
    //         parent_id_field:"parent",
    //         leaf_field:"isLeaf",
    //         expanded_field:"expanded",
    //     },
    //     gridComplete: function () {
    //         var width = $('#query_table_logs').closest('.jqGrid_wrapper').width() || $(document).width();
    //         $('#query_table_logs').setGridWidth(width);
    //
    //         var rows = $("#query_table_logs").jqGrid("getRowData") ;
    //         var sale = 0 ; var income = 0 ; var tax = 0 ;var refund = 0 ;
    //         var otherPay = 0 ; var outgo = 0 ; var profit = 0 ;var comm = 0 ;
    //         for(var i = 0, l = rows.length; i<l; i++) {
    //             sale += (rows[i].sale - 0);income += (rows[i].income - 0);tax += (rows[i].tax - 0);refund += (rows[i].refund - 0);
    //             otherPay += (rows[i].otherPay - 0);outgo += (rows[i].outgo - 0);profit += (rows[i].profit - 0);comm += (rows[i].comm - 0);
    //         }
    //         $("#query_table_logs").jqGrid("footerData", "set", {deptName:"合计",
    //             sale,sale,income,income,tax,tax,refund,refund,
    //             otherPay,otherPay,outgo,outgo,profit,profit,comm,comm
    //         });
    //     },
    //     viewrecords: true,
    //     caption: "",
    //     add: false,
    //     edit: true,
    //     addtext: 'Add',
    //     edittext: 'Edit',
    //     hidegrid: false,
    //     footerrow: true,
    //     ondblClickRow: function (rowid, iRow, iCol, e) {
    //         var rowData = jQuery("#query_table_logs").jqGrid("getRowData", rowid);
    //         view(rowData.deptId);
    //     }
    // });
    $("#querySearch").click(function () {
        reflushTreeTable();
    });

    $("#exportBtn").click(function () {
        var params =  removeBlank($("#queryForm").serializeJson());
        location.href = "/businessStatistics/exportSaleStat" + "?" + $.param(params);
    });

    $("#query_user_table_logs").jqGrid({
        url: baseUrl + '/businessStatistics/querySaleStat',
        datatype: "json",
        mtype: 'POST',
        postData: $("#queryUserForm").serializeJson(), //发送数据
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
            {name: 'deptName', label: '部门名称', editable: true, align:'center', width: 180},
            {name: 'deptId', label: '部门id', editable: true,hidden:true, width: 200},
            {name: 'userName', label: '业务员', editable: true, align:'center', width: 180},
            {name: 'sale', label: '业绩（含税）', editable: true, width: 180, align:'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'income', label: '回款', editable: true, width: 180, align:'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'tax', label: '税金', editable: true, width: 180, classes: '', align:'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'refund', label: '退款', editable: true, width: 180, align:'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'otherPay', label: '其它支出', editable: true, width: 180, align:'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'outgo', label: '成本（不含税）', editable: true, width: 180, align:'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'profit', label: '利润', editable: true, width: 180, classes: '', align:'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'comm', label: '提成', editable: true, width: 180, align:'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            }
        ],
        gridComplete: function () {
            var width = $('#query_user_table_logs').parents(".jqGrid_wrapper").width() || $('#query_table_logs').parents(".jqGrid_wrapper").width();
            $('#query_user_table_logs').setGridWidth(width);

            var rows = $("#query_user_table_logs").jqGrid("getRowData") ;
            var sale = 0 ; var income = 0 ; var tax = 0 ;var refund = 0 ;
            var otherPay = 0 ; var outgo = 0 ; var profit = 0 ;var comm = 0 ;
            for(var i = 0, l = rows.length; i<l; i++) {
                sale += (rows[i].sale - 0);income += (rows[i].income - 0);tax += (rows[i].tax - 0);refund += (rows[i].refund - 0);
                otherPay += (rows[i].otherPay - 0);outgo += (rows[i].outgo - 0);profit += (rows[i].profit - 0);comm += (rows[i].comm - 0);
            }
            $("#query_user_table_logs").jqGrid("footerData", "set", {userName:"合计",
                sale:sale,income:income,tax:tax,refund:refund,
                otherPay:otherPay,outgo:outgo,profit:profit,comm:comm
            });
        },
        pager: jQuery("#query_user_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        footerrow: true,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            var rowData = jQuery("#query_user_table_logs").jqGrid("getRowData", rowid);
            view(rowData.deptId);
        }
    });

    aggregateAmountUser();
    $("#queryUserSearch").click(function () {
        $("#query_user_table_logs").emptyGridParam();
        $("#query_user_table_logs").jqGrid('setGridParam', {
            postData: $("#queryUserForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        aggregateAmountUser();
    });

    $("#exportUserBtn").click(function () {
        var params =  removeBlank($("#queryUserForm").serializeJson());
        location.href = "/businessStatistics/exportSaleStat" + "?" + $.param(params);
    });
});
// function view(deptId) {
//     page("/biz/statistics/business_manager_notIncome_user?deptId="+deptId,"未到款统计(业务员)");
// }
function initTreeTable() {
    $("#query_table_logs").jqGrid({
        treeGrid: true,
        treeGridModel: 'adjacency', //treeGrid模式，跟json元数据有关
        ExpandColumn : 'deptName',
        scroll: "true",
        datatype: "local",
        colModel: [
            {name: 'id', label: '部门id', editable: true,hidden:true, sortable:false},
            {name: 'selfSale', label: 'selfSale', editable: true,hidden:true},
            {name: 'selfIncome', label: 'selfIncome', editable: true,hidden:true},
            {name: 'selfTax', label: 'selfTax', editable: true,hidden:true},
            {name: 'selfOutgo', label: 'selfOutgo', editable: true,hidden:true},
            {name: 'selfRefund', label: 'selfRefund', editable: true,hidden:true},
            {name: 'selfOtherPay', label: 'selfOtherPay', editable: true,hidden:true},
            {name: 'selfProfit', label: 'selfProfit', editable: true,hidden:true},
            {name: 'selfComm', label: 'selfComm', editable: true,hidden:true},
            {name: 'deptName', label: '部门名称', editable: true, align:'center', width: 200,sortable:false,
                formatter: function (value, grid, rows) {
                    var html = "";
                    if (rows.companyCode != undefined) {
                        html += rows.deptName + "(";
                        html += rows.companyCode;
                        html += ")";
                    } else {
                        html += value;
                    }
                    return html;
                },
            },
            {name: 'sale', label: '业绩（含税）', editable: true, width: 200, align:'center', formatter: "currency",sortable:false,
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'income', label: '回款', editable: true, width: 200, align:'center', formatter: "currency",sortable:false,
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'tax', label: '税金', editable: true, width: 200, classes: '', align:'center', formatter: "currency",sortable:false,
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'refund', label: '退款', editable: true, width: 200, align:'center', formatter: "currency",sortable:false,
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'otherPay', label: '其它支出', editable: true, width: 200, align:'center', formatter: "currency",sortable:false,
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'outgo', label: '成本（不含税）', editable: true, width: 200, align:'center', formatter: "currency",sortable:false,
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'profit', label: '利润', editable: true, width: 200, classes: '', align:'center', formatter: "currency",sortable:false, formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'comm', label: '提成', editable: true, width: 200, align:'center', formatter: "currency",sortable:false,
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            }
        ],
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
        shrinkToFit:false,  // 控制水平滚动条
        footerrow: true,
        gridComplete: function () {
            var width = $('#query_table_logs').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#query_table_logs').setGridWidth(width);

            var rows = $("#query_table_logs").jqGrid("getRowData") ;
            var sale = 0 ; var income = 0 ; var tax = 0 ;var refund = 0 ;
            var otherPay = 0 ; var outgo = 0 ; var profit = 0 ;var comm = 0 ;
            for(var i = 0, l = rows.length; i<l; i++) {
                sale += (rows[i].selfSale - 0);income += (rows[i].selfIncome - 0);tax += (rows[i].selfTax - 0);refund += (rows[i].selfRefund - 0);
                otherPay += (rows[i].selfOtherPay - 0);outgo += (rows[i].selfOutgo - 0);profit += (rows[i].selfProfit - 0);comm += (rows[i].selfComm - 0);
            }
            $("#query_table_logs").jqGrid("footerData", "set", {deptName:"合计",
                sale:sale,income:income,tax:tax,refund:refund,
                otherPay:otherPay,outgo:outgo,profit:profit,comm:comm
            });

            //顶部金额合计
            $("#saleSum").text(fmtMoneyBringUnit(sale) || 0);
            $("#incomeSum").text(fmtMoneyBringUnit(income) || 0);
            $("#taxSum").text(fmtMoneyBringUnit(tax) || 0);
            $("#refundSum").text(fmtMoneyBringUnit(refund) || 0);
            $("#otherPaySum").text(fmtMoneyBringUnit(otherPay) || 0);
            $("#outgoSum").text(fmtMoneyBringUnit(outgo) || 0);
            $("#profitSum").text(fmtMoneyBringUnit(profit) || 0);
            $("#commSum").text(fmtMoneyBringUnit(comm) || 0);
        }
    });
    $("#query_table_logs").setGridHeight(460);
}
function reflushTreeTable() {
    initTreeTable();
    requestData($("#queryForm").serializeJson(), "/businessStatistics/querySaleStat", "post", "json", false, function (data) {
        var reader = {
            root: function(obj) { return data.list; }
        };
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").setGridParam({data: data.list, reader: reader}).trigger('reloadGrid');
    })
}

//初始化年下拉选项
function initYearSelect() {
    var currentYear = new Date().getFullYear();
    var yearHtml = "";
    for(var y = 2018; y < currentYear + 6; y++){
        yearHtml += " <option value=\""+y+"\">"+y+"</option>";
    }
    $("select[name='year']").html(yearHtml);
}

function aggregateAmount(param,flag) {

    $("#tj").find(".text-danger").html(0);
    $.ajax({
        type:"post",
        data:param,
        url:baseUrl+"/businessStatistics/querySaleStatSum",
        dataType:"json",
        async:false,
        success:function (resDate) {
            if (resDate){
                $("#saleSum").text(fmtMoneyBringUnit(resDate.sale) || 0);
                $("#incomeSum").text(fmtMoneyBringUnit(resDate.income) || 0);
                $("#taxSum").text(fmtMoneyBringUnit(resDate.tax) || 0);
                $("#refundSum").text(fmtMoneyBringUnit(resDate.refund) || 0);
                $("#otherPaySum").text(fmtMoneyBringUnit(resDate.otherPay) || 0);
                $("#outgoSum").text(fmtMoneyBringUnit(resDate.outgo) || 0);
                $("#profitSum").text(fmtMoneyBringUnit(resDate.profit) || 0);
                $("#commSum").text(fmtMoneyBringUnit(resDate.comm) || 0);
                if(flag){//初始化的时候把两个tab页的统计都初始化
                    $("#saleSum1").text(fmtMoneyBringUnit(resDate.sale) || 0);
                    $("#incomeSum1").text(fmtMoneyBringUnit(resDate.income) || 0);
                    $("#taxSum1").text(fmtMoneyBringUnit(resDate.tax) || 0);
                    $("#refundSum1").text(fmtMoneyBringUnit(resDate.refund) || 0);
                    $("#otherPaySum1").text(fmtMoneyBringUnit(resDate.otherPay) || 0);
                    $("#outgoSum1").text(fmtMoneyBringUnit(resDate.outgo) || 0);
                    $("#profitSum1").text(fmtMoneyBringUnit(resDate.profit) || 0);
                    $("#commSum1").text(fmtMoneyBringUnit(resDate.comm) || 0);
                }
            }
        }
    })
}

function aggregateAmountUser() {
    $("#tj1").find(".text-danger").html(0);
    $.ajax({
        type:"post",
        data:$("#queryUserForm").serializeJson(),
        url:baseUrl+"/businessStatistics/querySaleStatSum",
        dataType:"json",
        async:false,
        success:function (resDate) {
            if (resDate){
                $("#saleSum1").text(fmtMoneyBringUnit(resDate.sale) || 0);
                $("#incomeSum1").text(fmtMoneyBringUnit(resDate.income) || 0);
                $("#taxSum1").text(fmtMoneyBringUnit(resDate.tax) || 0);
                $("#refundSum1").text(fmtMoneyBringUnit(resDate.refund) || 0);
                $("#otherPaySum1").text(fmtMoneyBringUnit(resDate.otherPay) || 0);
                $("#outgoSum1").text(fmtMoneyBringUnit(resDate.outgo) || 0);
                $("#profitSum1").text(fmtMoneyBringUnit(resDate.profit) || 0);
                $("#commSum1").text(fmtMoneyBringUnit(resDate.comm) || 0);
            }
        }
    })
}