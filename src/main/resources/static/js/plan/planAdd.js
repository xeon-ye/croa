$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';

    $("#userName").val(user.name);
    $("#userDept").val(user.dept.name);
    $("#currentDate").val(new Date().format("yyyy-MM-dd"));
});

/**
 * 后台请求方法
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
        success: callBackFun
    });
}

//新增计划
function savePlan() {
    if (!$("#plateFrom").valid()) return;
    startModal("#saveBtn");
    requestData(JSON.stringify($("#plateFrom").serializeJson()), "/userPlan/save", "post", "json", true, function (data) {
        Ladda.stopAll();
        swal({
            title: data.code == 200 ? "成功!" : "失败",
            text: data.code == 200 ? "计划录入成功！" : data.msg,
            type: data.code == 200 ? "success" : "error",
            html: true
        });

        if( data.code == 200){
            triggerPageBtnClick("plan/planList","searchButton"); //触发个人计划查询页面刷新
            triggerPageBtnClick("/plan/planStatistics","searchButton"); //触发计划总结统计页面刷新
            triggerPageBtnClick("/plan/planManage","searchButton"); //触发计划总结列表页面刷新
            triggerPageBtnClick("/plan/planNotEnterList","searchButton"); //触发未填写页面刷新
            closeCurrentTab(); //关闭当前页面
        }
    });
}