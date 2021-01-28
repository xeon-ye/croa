$(document).ready(function () {
    var myDate = new Date();
    var year;
    var month;
    if (getQueryString("year") != null && getQueryString("year") != "" && getQueryString("year") != undefined) {
        year = getQueryString("year");
    } else {
        year = myDate.getFullYear();
    }
    if (getQueryString("month") != null && getQueryString("month") != "" && getQueryString("month") != undefined) {
        month = getQueryString("month");
    } else {
        month = myDate.getMonth();//这个month从0开始计数
        if (month == 0) {
            year = year - 1;
            month = 12;
        } else {
            month = month - 1;
        }
    }

    $("#year").val(year);
    $("#year1").val(year);
    $("#month").val(month);
    $("#month1").val(month);

    $(".alterBtn").click(function () {
        page("/biz/statistics/querySaleStat", "业绩统计");
    });

    $("#queryForm [name='year']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#queryForm [name='month']").change(function () {
        $("#querySearch").trigger("click");
    });

    $("#queryUserForm [name='year']").change(function () {
        $("#queryUserSearch").trigger("click");
    });
    $("#queryUserForm [name='month']").change(function () {
        $("#queryUserSearch").trigger("click");
    });

    aggregateAmount();
    aggregateAmountUser();
    $('#queryDiv').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            $("#querySearch").click();
        }
    });

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/articleHistory/queryArticleChange',
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
            {name: 'createYear', label: 'createYear', editable: true, hidden: true},
            {name: 'createMonth', label: 'createMonth', editable: true, hidden: true},
            {name: 'issuedYear', label: 'issuedYear', editable: true, hidden: true},
            {name: 'issuedMonth', label: 'issuedMonth', editable: true, hidden: true},
            {
                name: 'yearAndMonth', label: '修改年月', editable: true, hidden: false, width: 200, align: 'center',sortable:false,
                formatter: function (value, grid, rows) {
                    if (rows.createMonth == undefined || rows.createMonth == "" || rows.createMonth == null) {
                        return value;
                    } else {
                        return rows.createYear + "年" + rows.createMonth + "月";
                    }
                }
            },
            {
                name: 'alterSale', label: '业绩（含税）', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterIncome', label: '回款', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterTax', label: '税金', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterRefund', label: '退款', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterOtherPay',
                label: '其它支出',
                editable: true,
                width: 200,
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterOutgo',
                label: '成本（不含税）',
                editable: true,
                width: 200,
                classes: '',
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterProfit',
                label: '利润',
                editable: true,
                width: 200,
                classes: '',
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterComm', label: '提成', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'operate', label: '操作', editable: true, align: 'center', width: 200,sortable:false,
                formatter: function (value, grid, rows) {
                    var html = "";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  " +
                        "onclick='view(" + rows.issuedYear + "," + rows.issuedMonth + "," + rows.createYear + "," + rows.createMonth + ")'>异动详情&nbsp;&nbsp;</a>";
                    return html;
                }
            }
        ],
        gridComplete: function () {
            var width = $('#query_table_logs').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#query_table_logs').setGridWidth(width);

            var rows = $("#query_table_logs").jqGrid("getRowData");
            var alterSale = 0;
            var alterIncome = 0;
            var alterTax = 0;
            var alterRefund = 0;
            var alterOtherPay = 0;
            var alterOutgo = 0;
            var alterProfit = 0;
            var alterComm = 0;
            for (var i = 0, l = rows.length; i < l; i++) {
                alterSale += (rows[i].alterSale - 0);
                alterIncome += (rows[i].alterIncome - 0);
                alterTax += (rows[i].alterTax - 0);
                alterRefund += (rows[i].alterRefund - 0);
                alterOtherPay += (rows[i].alterOtherPay - 0);
                alterOutgo += (rows[i].alterOutgo - 0);
                alterProfit += (rows[i].alterProfit - 0);
                alterComm += (rows[i].alterComm - 0);
            }
            $("#query_table_logs").jqGrid("footerData", "set", {
                yearAndMonth: "合计",
                alterSale, alterSale, alterIncome, alterIncome, alterTax, alterTax, alterRefund, alterRefund,
                alterOtherPay, alterOtherPay, alterOutgo, alterOutgo, alterProfit, alterProfit, alterComm, alterComm
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
        footerrow: true
    });

    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        aggregateAmount();
    });

    $("#exportBtn").click(function () {
        var params =  removeBlank($("#queryForm").serializeJson());
        location.href = "/articleHistory/exportArticleChange" + "?" + $.param(params);
    });

    $("#exportUserBtn").click(function () {
        var params =  removeBlank($("#queryUserForm").serializeJson());
        location.href = "/articleHistory/exportArticleChange" + "?" + $.param(params);
    });

    $("#query_user_table_logs").jqGrid({
        url: baseUrl + '/articleHistory/queryArticleChange',
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
            {name: 'createYear', label: 'createYear', editable: true, hidden: true},
            {name: 'createMonth', label: 'createMonth', editable: true, hidden: true},
            {name: 'issuedYear', label: 'issuedYear', editable: true, hidden: true},
            {name: 'issuedMonth', label: 'issuedMonth', editable: true, hidden: true},
            {
                name: 'yearAndMonth', label: '发布年月', editable: true, hidden: false, width: 200, align: 'center',sortable:false,
                formatter: function (value, grid, rows) {
                    if (rows.issuedMonth == undefined || rows.issuedMonth == "" || rows.issuedMonth == null) {
                        return value;
                    } else {
                        return rows.issuedYear + "年" + rows.issuedMonth + "月";
                    }
                }
            },
            {
                name: 'alterSale', label: '业绩（含税）', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterIncome', label: '回款', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterOutgo',
                label: '成本（不含税）',
                editable: true,
                width: 200,
                classes: '',
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterTax', label: '税金', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterRefund', label: '退款', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterOtherPay',
                label: '其它支出',
                editable: true,
                width: 200,
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterProfit',
                label: '利润',
                editable: true,
                width: 200,
                classes: '',
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterComm', label: '提成', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'editDesc', label: '操作', editable: true, align: 'center', width: 200,sortable:false,
                formatter: function (value, grid, rows) {
                    var html = "";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  " +
                        "onclick='view(" + rows.issuedYear + "," + rows.issuedMonth + "," + rows.createYear + "," + rows.createMonth + ")'>异动详情&nbsp;&nbsp;</a>";
                    return html;
                }
            }
        ],
        gridComplete: function () {
            var width = $('#query_table_logs').parents(".jqGrid_wrapper").width() || $('#query_user_table_logs').parents(".jqGrid_wrapper").width();
            $('#query_user_table_logs').setGridWidth(width);

            var rows = $("#query_user_table_logs").jqGrid("getRowData");
            var alterSale = 0;
            var alterIncome = 0;
            var alterTax = 0;
            var alterRefund = 0;
            var alterOtherPay = 0;
            var alterOutgo = 0;
            var alterProfit = 0;
            var alterComm = 0;
            for (var i = 0, l = rows.length; i < l; i++) {
                alterSale += (rows[i].alterSale - 0);
                alterIncome += (rows[i].alterIncome - 0);
                alterTax += (rows[i].alterTax - 0);
                alterRefund += (rows[i].alterRefund - 0);
                alterOtherPay += (rows[i].alterOtherPay - 0);
                alterOutgo += (rows[i].alterOutgo - 0);
                alterProfit += (rows[i].alterProfit - 0);
                alterComm += (rows[i].alterComm - 0);
            }
            $("#query_user_table_logs").jqGrid("footerData", "set", {
                yearAndMonth: "合计",
                alterSale, alterSale, alterIncome, alterIncome, alterTax, alterTax, alterRefund, alterRefund,
                alterOtherPay, alterOtherPay, alterOutgo, alterOutgo, alterProfit, alterProfit, alterComm, alterComm
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
        footerrow: true
    });

    $("#queryUserSearch").click(function () {
        $("#query_user_table_logs").emptyGridParam();
        $("#query_user_table_logs").jqGrid('setGridParam', {
            postData: $("#queryUserForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        aggregateAmountUser();
    });

    $("#detail_table_logs").jqGrid({
        url: baseUrl + '/articleHistory/queryArticleChangeDetail',
        datatype: "local",
        mtype: 'POST',
        postData: $("#detailForm").serializeJson(), //发送数据
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
            {name: 'mediaTypeName', label: '媒体板块', editable: true, align: 'center', width: 80},
            {name: 'mediaName', label: '媒体名称', editable: true, align: 'center', width: 100},
            {name: 'mediaUserName', label: '媒介', editable: true, align: 'center', width: 100},
            {name: 'deptName', label: '业务部门', editable: true, align: 'center', width: 100},
            {name: 'userName', label: '业务员', editable: true, align: 'center', width: 100},
            {name: 'title', label: '标题', editable: true, align: 'center', width: 200},
            {name: 'link', label: '链接', editable: true, align: 'center', width: 100},
            {name: 'issuedDate', label: '发布日期', editable: true, hidden: false, align: 'center', width: 200},
            {
                name: 'alterSale', label: '业绩（含税）', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterIncome', label: '回款', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterTax', label: '税金', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterRefund', label: '退款', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterOtherPay',
                label: '其它支出',
                editable: true,
                width: 200,
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterOutgo',
                label: '成本（不含税）',
                editable: true,
                width: 200,
                classes: '',
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterProfit',
                label: '利润',
                editable: true,
                width: 200,
                classes: '',
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterComm', label: '提成', editable: true, width: 200, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'createTime', label: '修改日期', editable: true, width: 200, align: 'center'},
            {name: 'editDesc', label: '修改方式', editable: true, align: 'center', width: 200}
        ],
        gridComplete: function () {
            var width = $('#detail_table_logs').parents(".jqGrid_wrapper").width();
            $('#detail_table_logs').setGridWidth(width);

            var rows = $("#detail_table_logs").jqGrid("getRowData");
            var alterSale = 0;
            var alterIncome = 0;
            var alterTax = 0;
            var alterRefund = 0;
            var alterOtherPay = 0;
            var alterOutgo = 0;
            var alterProfit = 0;
            var alterComm = 0;
            for (var i = 0, l = rows.length; i < l; i++) {
                alterSale += (rows[i].alterSale - 0);
                alterIncome += (rows[i].alterIncome - 0);
                alterTax += (rows[i].alterTax - 0);
                alterRefund += (rows[i].alterRefund - 0);
                alterOtherPay += (rows[i].alterOtherPay - 0);
                alterOutgo += (rows[i].alterOutgo - 0);
                alterProfit += (rows[i].alterProfit - 0);
                alterComm += (rows[i].alterComm - 0);
            }
            $("#detail_table_logs").jqGrid("footerData", "set", {
                issuedDate: "合计",
                alterSale, alterSale, alterIncome, alterIncome, alterTax, alterTax, alterRefund, alterRefund,
                alterOtherPay, alterOtherPay, alterOutgo, alterOutgo, alterProfit, alterProfit, alterComm, alterComm
            });
        },
        pager: jQuery("#detail_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        footerrow: true
    });

    $("#detailSearch").click(function () {
        $("#detail_table_logs").emptyGridParam();
        $("#detail_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#detailForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        aggregateAmountDetail();
    });
    $("#exportDetailBtn").click(function () {
        var params =  removeBlank($("#detailForm").serializeJson());
        location.href = "/articleHistory/exportArticleChangeDetail" + "?" + $.param(params);
    });
});

function aggregateAmount() {
    $("#tj").find(".text-danger").html(0);
    $.ajax({
        type: "post",
        data: $("#queryForm").serializeJson(),
        url: baseUrl + "/articleHistory/queryArticleChangeSum",
        dataType: "json",
        async: false,
        success: function (resDate) {
            if (resDate) {
                $("#saleSum").text(fmtMoneyBringUnit(resDate.sale) || 0);
                $("#incomeSum").text(fmtMoneyBringUnit(resDate.income) || 0);
                $("#taxSum").text(fmtMoneyBringUnit(resDate.tax) || 0);
                $("#refundSum").text(fmtMoneyBringUnit(resDate.refund) || 0);
                $("#otherPaySum").text(fmtMoneyBringUnit(resDate.otherPay) || 0);
                $("#outgoSum").text(fmtMoneyBringUnit(resDate.outgo) || 0);
                $("#profitSum").text(fmtMoneyBringUnit(resDate.profit) || 0);
                $("#commSum").text(fmtMoneyBringUnit(resDate.comm) || 0);
                $("#saleSumOriginal").text(fmtMoneyBringUnit(parseFloat(resDate.saleSumOriginal)-parseFloat(resDate.sale)) || 0);
            }
        }
    })
}

function view(issuedYear,issuedMonth,createYear,createMonth) {
    var issuedStartStr;
    var issuedEndStr;
    var createStartStr;
    var createEndStr;
    issuedStartStr = issuedYear + '-'+issuedMonth + "-1" ;
    if(issuedMonth==12){
        issuedEndStr = issuedYear + '-'+issuedMonth + "-31" ;
    }else{
        var lastIssuedDay = new Date(issuedYear, issuedMonth, 0).getDate();
        issuedEndStr = issuedYear+"-"+issuedMonth+"-"+lastIssuedDay;
    }

    createStartStr = createYear + '-'+createMonth + "-1" ;
    if(createMonth==12){
        createEndStr = createYear + '-'+createMonth + "-31" ;
    }else{
        var lastCreateDay = new Date(createYear, createMonth, 0).getDate();
        createEndStr = createYear+"-"+createMonth+"-"+lastCreateDay;
    }

    $("#detailForm [name='issuedStartTime']").val(issuedStartStr);
    $("#detailForm [name='issuedEndTime']").val(issuedEndStr);
    $("#detailForm [name='createStartTime']").val(createStartStr);
    $("#detailForm [name='createEndTime']").val(createEndStr);
    $("#detailModal").modal({backdrop: "static"});
    $("#detailSearch").trigger("click")
}

function aggregateAmountUser() {
    $("#tj1").find(".text-danger").html(0);
    $.ajax({
        type: "post",
        data: $("#queryUserForm").serializeJson(),
        url: baseUrl + "/articleHistory/queryArticleChangeSum",
        dataType: "json",
        async: false,
        success: function (resDate) {
            if (resDate) {
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

function aggregateAmountDetail() {
    $("#detailTj").find(".text-danger").html(0);
    $.ajax({
        type: "post",
        data: $("#detailForm").serializeJson(),
        url: baseUrl + "/articleHistory/queryArticleChangeDetailSum",
        dataType: "json",
        async: false,
        success: function (resDate) {
            if (resDate) {
                $("#sale").text(fmtMoneyBringUnit(resDate.sale) || 0);
                $("#income").text(fmtMoneyBringUnit(resDate.income) || 0);
                $("#tax").text(fmtMoneyBringUnit(resDate.tax) || 0);
                $("#refund").text(fmtMoneyBringUnit(resDate.refund) || 0);
                $("#otherPay").text(fmtMoneyBringUnit(resDate.otherPay) || 0);
                $("#outgo").text(fmtMoneyBringUnit(resDate.outgo) || 0);
                $("#profit").text(fmtMoneyBringUnit(resDate.profit) || 0);
                $("#comm").text(fmtMoneyBringUnit(resDate.comm) || 0);
            }
        }
    })
}
function getLastDate(year,month)
{
    var new_year = year;  //取当前的年份
    var new_month = month++;//取下一个月的第一天，方便计算（最后一天不固定）
    if(month>12)      //如果当前大于12月，则年份转到下一年
    {
        new_month -=12;    //月份减
        new_year++;      //年份增
    }
    var new_date = new Date(new_year,new_month,1);        //取当年当月中的第一天
    return (new Date(new_date.getTime()-1000*60*60*24)).getDate();//获取当月最后一天日期
}