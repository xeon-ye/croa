$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#planTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });

    createTable(); //表格定义
    reflushTotal();//刷新合计数据
});

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
        contentType: 'application/json;charset=utf-8', //设置请求头信息
        async: async,
        success: callBackFun,
        error: function () {
            Ladda.stopAll();//隐藏加载按钮
        }
    });
}

//时间区间隐藏显示
function showDate(val){
    if(val == 3){
        $("#dateFormGroup").show();
    }else{
        $("#dateFormGroup").hide();
        reflushTable();
    }
}

//表格定义
function createTable() {
    var $planTable = $("#planTable");
    $planTable.jqGrid({
        url: baseUrl + '/userPlan/listPlanByCurrentUser',
        datatype: "json",
        postData: $("#queryForm").serializeJson(),
        mtype: 'post',
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: true,
        setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        autoScroll: true,
        shrinkToFit: false,
        prmNames: {rows: "size"},
        rowNum: 50, //每页记录数
        rowList: [10,20, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colNames: ['姓名','时间','业绩（元)','利润业绩（元）','有效客户量','新成交客户量','跟进客户数量','推资源客户数量',
            '业绩（元)','利润业绩（元）','有效客户量','新成交客户量','跟进客户数量','推资源客户数量'],//表头
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'user.userName',
                label: '姓名',
                width: 130,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    if(row.isOvertime == 1 ){
                        return "<span title='录入超时' style=\"color: red;\">"+v+"</span>";
                    }else{
                        return v;
                    }
                }
            },
            {
                name: 'createDate',
                label: '时间',
                width: 160,
                editable: true,
                sortable: true,
                formatter: function (v, options, row) {
                    if(row.isOvertime == 1){
                        return "<span title='录入超时' style=\"color: red;\">"+v+"</span>";
                    }else{
                        return v;
                    }
                }
            },
            {
                name: 'perfoSummary',
                label: '业绩（元)',
                width: 115,
                editable: true,
                sortable: true ,
                formatter: function (v, options, row) {
                    var resultArr = row.summaryResult.split('');
                    if(resultArr[0] == 1){
                        return "<span title='业绩不达标' style=\"color: red;\">"+v+"</span>";
                    }else{
                        return v;
                    }
                }
            },
            {
                name: 'profitSummary',
                label: '利润业绩（元）',
                width: 115,
                editable: true,
                sortable: true ,
                formatter: function (v, options, row) {
                    var resultArr = row.summaryResult.split('');
                    if(resultArr[1] == 1){
                        return "<span title='利润不达标' style=\"color: red;\">"+v+"</span>";
                    }else{
                        return v;
                    }
                }
            },
            {
                name: 'yxCustomSummary',
                label: '有效客户量',
                width: 115,
                editable: true,
                sortable: true ,
                formatter: function (v, options, row) {
                    var resultArr = row.summaryResult.split('');
                    if(resultArr[2] == 1){
                        return "<span title='有效客户量不达标' style=\"color: red;\">"+v+"</span>";
                    }else{
                        return v;
                    }
                }
            },
            {
                name: 'xcjCustomSummary',
                label: '新成交客户量',
                width: 115,
                editable: true,
                sortable: true ,
                formatter: function (v, options, row) {
                    var resultArr = row.summaryResult.split('');
                    if(resultArr[3] == 1){
                        return "<span title='新成交客户量不达标' style=\"color: red;\">"+v+"</span>";
                    }else{
                        return v;
                    }
                }
            },
            {
                name: 'gjCustomSummary',
                label: '跟进客户数量',
                width: 115,
                editable: true,
                sortable: true ,
                formatter: function (v, options, row) {
                    var resultArr = row.summaryResult.split('');
                    if(resultArr[4] == 1){
                        return "<span title='跟进客户数量不达标' style=\"color: red;\">"+v+"</span>";
                    }else{
                        return v;
                    }
                }
            },
            {
                name: 'tzyCustomSummary',
                label: '推资源客户数量',
                width: 115,
                editable: true,
                sortable: true ,
                formatter: function (v, options, row) {
                    var resultArr = row.summaryResult.split('');
                    if(resultArr[5] == 1){
                        return "<span title='推资源客户数量不达标' style=\"color: red;\">"+v+"</span>";
                    }else{
                        return v;
                    }
                }
            },
            {
                name: 'perfoPlan',
                label: '业绩（元)',
                width: 115,
                editable: true,
                sortable: true
            },
            {
                name: 'profitPlan',
                label: '利润业绩（元）',
                width: 115,
                editable: true,
                sortable: true
            },
            {
                name: 'yxCustomPlan',
                label: '有效客户量',
                width: 115,
                editable: true,
                sortable: true
            },
            {
                name: 'xcjCustomPlan',
                label: '新成交客户量',
                width: 115,
                editable: true,
                sortable: true
            },
            {
                name: 'gjCustomPlan',
                label: '跟进客户数量',
                width: 115,
                editable: true,
                sortable: true
            },
            {
                name: 'tzyCustomPlan',
                label: '推资源客户数量',
                width: 115,
                editable: true,
                sortable: true
            },
        ],
        pager: "#planTablePaper",
        viewrecords: true,
        caption: getTableTitle(),
        hidegrid: false,
        gridComplete: function () {
            var width = $('#planTable').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#planTable').setGridWidth(width);
        }
    });
    $planTable.setSelection(4, true);
    $planTable.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $("#planTable").jqGrid('setGroupHeaders', {
        useColSpanStyle: true,
        groupHeaders: [
            { startColumnName: 'perfoSummary', numberOfColumns: 6, titleText: '昨日总结' },
            { startColumnName: 'perfoPlan', numberOfColumns: 6, titleText: '今天计划' }
        ]
    });
}

//刷新表格
function reflushTable() {
    //刷新表格
    $("#planTable").emptyGridParam(); //清空历史查询数据
    $("#planTable").jqGrid("setCaption",getTableTitle()); //设置表格标题
    $("#planTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入

    reflushTotal();//刷新合计数据
}

//刷新合计数据
function reflushTotal() {
    requestData($("#queryForm").serializeJson(), "/userPlan/getTotalByUserId", "post","json",true,function (data) {
        if(data.code == 200){
            var result = data.data.total;
            $("#perfoSummary").text(result.perfoSummary || "0.00");
            $("#profitSummary").text(result.profitSummary || "0.00");
            $("#yxCustomSummary").text(result.yxCustomSummary || 0);
            $("#xcjCustomSummary").text(result.xcjCustomSummary || 0);
            $("#gjCustomSummary").text(result.gjCustomSummary || 0);
            $("#tzyCustomSummary").text(result.tzyCustomSummary || 0);
            $("#perfoPlan").text(result.perfoPlan || "0.00");
            $("#profitPlan").text(result.profitPlan || "0.00");
            $("#yxCustomPlan").text(result.yxCustomPlan || 0);
            $("#xcjCustomPlan").text(result.xcjCustomPlan || 0);
            $("#gjCustomPlan").text(result.gjCustomPlan || 0);
            $("#tzyCustomPlan").text(result.tzyCustomPlan || 0);
        }else {
            swal({
                title: "失败",
                text: data.msg,
                type: "error"
            });
        }
    });
}

//获取表格标题
function getTableTitle() {
    var timeType = $("#timeQuantum").val();
    var result = "个人计划总结列表（"+new Date().format("yyyy-MM-dd")+"）";
    if(timeType){
        if(timeType == 1){
            result = "个人计划总结列表（本周）";
        }else if(timeType == 2){
            result = "个人计划总结列表（本月）";
        }else {
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if(startDate && endDate){
                result = "个人计划总结列表（"+startDate+" - "+endDate+"）";
            }else if (startDate) {
                result = "个人计划总结列表（自"+startDate+"起）";
            }else if (endDate){
                result = "个人计划总结列表（截止"+endDate+"）";
            }
        }
    }
    return result;
}

//添加按钮点击事件
function addBtnClick() {
    $("#userName").val(user.name);
    $("#userDept").val(user.dept.name);
    $("#currentDate").val(new Date().format("yyyy-MM-dd"));
    $("#addPlateFrom").find("input").removeClass('error');
    $("#addPlateFrom").validate().resetForm();
    $("#addPlan").modal("toggle");
}

//新增计划
function savePlan() {
    if (!$("#addPlateFrom").valid()) return;
    startModal("#saveBtn");
    requestData(JSON.stringify($("#addPlateFrom").serializeJson()), "/userPlan/save", "post", "json", true, function (data) {
        Ladda.stopAll();
        if (data.code == 200) {
            swal({
                title: "成功",
                text: "计划录入成功！",
                type: "success"
            },function () {
                $("#addPlan").modal("toggle");
                $("#addPlateFrom")[0].reset();//重置表单数据
                reflushTable(); //刷新表格
            });
        } else {
            swal({
                title: "失败",
                text: data.msg,
                type: "error"
            });
        }
    });
}