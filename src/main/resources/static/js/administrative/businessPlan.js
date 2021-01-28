$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $("#applicationDate").val(new Date().format("yyyy-MM-dd"));
    $("#deptName").val(user.dept.name);

//添加按钮点击事件

    $("#addBtn").click(function () {
        $("#addPlan").modal({backdrop: "static"});
    })
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


//新增计划
function saveBusinessPlan() {
    if (!$("#plateFrom").valid()) return;
    startModal("#saveBtn");
    requestData(JSON.stringify($("#plateFrom").serializeJson()), "/userBusinessPlan/saveBusiness", "post", "json", true, function (data) {
        Ladda.stopAll();
        swal({
            title: data.code == 200 ? "成功!" : "失败",
            text: data.code == 200 ? "计划录入成功！" : data.msg,
            type: data.code == 200 ? "success" : "error",
            html: true
        });

        triggerPageBtnClick("plan/planList","searchButton"); //触发个人计划查询页面刷新
        closeCurrentTab(); //关闭当前页面
    });
}

