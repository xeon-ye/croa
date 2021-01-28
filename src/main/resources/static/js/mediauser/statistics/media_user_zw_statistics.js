//查询区域
var searchForm = {
    init: function () {
        searchForm.loadDept();
        searchForm.loadWorker(null,"MJ",null,null);
    },
    loadDept:function(){
        var deptDiv = document.getElementById("deptDiv");
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
        if(isZW()){
            deptDiv.style.display = 'block';

            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData()],
                onNodeSelected: function (event, data) {
                    $("#currentDeptId").val(data.id);
                    $("#chooseDeptName").val(data.text);
                    $("#deptModal").modal('hide');
                    searchForm.loadWorker(data.id,data.code);
                    searchForm.search();
                }
            });
            $("#cleanDept").click(function () {
                $("#currentUserId").empty();//初始化
                $("#currentUserId").append('<option value="">全部</option>');
                $("#currentDeptId").val("");//部门初始化
                $("#chooseDeptName").val("");//部门初始化
                layui.use('form', function(){  //刷新下拉列表
                    var form = layui.form;
                    form.render('select');
                });
                searchForm.search();
            });
        }
    },
    //加载媒介
    loadWorker: function (deptId,roleType,mediaUserId,mediaUserName) {
        var ele = $("#currentUserId");
        //如果没有部门权限，则只加载当前用户
        ele.empty();
        if (!isZW()) {
            ele.append("<option value=" + user.id + ">" + user.name + "</option>");
        } else {
            if(mediaUserId){
                ele.append("<option value=" + mediaUserId + ">" + mediaUserName + "</option>");
                ele.val(mediaUserId);
            }else{
                ele.append('<option value="">全部</option>');
                searchForm.loadDeptUser(deptId,roleType,"currentUserId",searchForm.worker);
            }
        }
        layui.use('form', function(){
            var form = layui.form;
            form.render('select');
        });
    },
    loadDeptUser: function (deptId, roleType, attr) {
        var attribute = attr || 'users';
        layui.use(['form'], function () {
            Views.layuiForm = layui.form;
            var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
            $.ajax({
                    url: baseUrl + "/deptZw/listZwUserByDeptAndRole",
                    type: "post",
                    data: {deptId: deptId, roleType: roleType, deptCode: "MJ"},
                    async: false,
                    dataType: "json",
                    success: function (users) {
                        var userList = users.data.list;
                        if(userList && userList.length > 0){
                            for (var i = 0; i < userList.length; i++) {
                                ele.append("<option value=" + userList[i].id + ">" + userList[i].name + "</option>");
                                Views.layuiForm.render();
                            }
                        }
                    }
                }
            );
        });
    },
    //显示或隐藏发布日期
    showIssuedDate: function (val) {
        var btnNode = $("#searchButton");
        if (val == 3) {
            $("#issuedDateFormGroup").show();
            $("#searchButton").remove();
            $("#rowTwo").append(btnNode);//当选择时间区间，按钮展示在其后面
        } else {
            $("#issuedDateFormGroup").hide();
            $("#searchButton").remove();
            $("#rowOne").append(btnNode);//当选择非时间区间，按钮在初始化位置
            searchForm.search();
        }
    },
    //查询
    search: function () {
        var searchFormData = $("#searchForm").serializeJson();
        //加载媒介统计结果
        $.ajax({
            url: baseUrl + "/mediaUsereStatistics/zwMediaUserResult",
            type: "post",
            data: searchFormData,
            dataType: "json",
            success: function (resData) {
                var val = resData.data.result.mediaUserStatistics["articleNum"] || 0;
                $("#articleNum").text(fmtMoneyBringUnit(val,"件"));

                mediaUserTrend.reflushTrend(resData.data.result.trend);
            }
        });
    }
};

//判断当前用户是否媒介政务
var isZW = function () {
    var roles = user.roles;//获取用户角色
    var isZW = false;//是否媒介政务
    if(roles){
        for(var i=0; i < roles.length; i++){
            if(roles[i].code == 'ZW' && roles[i].type == 'MJ'){
                isZW = true;
                break;
            }
        }
    }
    return isZW;
}

//获取部门树数据
function getTreeData() {
    var deptTreeData = {};
    //具体查询
    requestData({deptCode:'MJ'},"/deptZw/listDeptTreeByZw","POST",function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });

    $("#currentDeptId").val(deptTreeData.id);
    $("#chooseDeptName").val(deptTreeData.text);

    return deptTreeData;
}

/**
 * 同步后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param callBackFun 成功回调方法
 */
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

//统计结果
var mediaUserTrend = {
    trend: {},
    trendOption:{
        "color": ["#EE7383"],
        "backgroundColor": "#fff",
        "legend": {"data": ["稿件数量"]},
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
        "tooltip": {
            "show": true,
            trigger: 'axis'
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            boundaryGap : false,
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：件)'
            }
        ],
        "series": [
            {
                "name": "稿件数量",
                "type": "line",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            }
        ]
    }, //趋势图表配置
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            mediaUserTrend.trend = echarts.init(document.getElementById('statisticsTrend'));
            mediaUserTrend.trend.setOption(mediaUserTrend.trendOption);
            window.onresize = function () {
                mediaUserTrend.trend.resize();
            }
        },0);
    },
    reflushTrend: function (dataList){
        //清空原来的数据
        mediaUserTrend.trendOption.xAxis[0].data = [];
        mediaUserTrend.trendOption.series[0].data = [];
        //数据封装
        var timeUnit = $("#timeQuantum").val() == 1 ? "日" : "月";
        for (var i = 0; i < dataList.length; i++) {
            mediaUserTrend.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
            mediaUserTrend.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
        }
        //加载图表
        mediaUserTrend.loadTrend();
    },
    reflush: function (mediaTypeList) {
        mediaUserTrend.load();
    }
};
