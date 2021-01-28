var deptId = user.dept.id;//当前用户部门ID
var deptName = user.dept.name;//当前部门名称
var deptCode = user.dept.code;//当前部门编码
var deptCompanyCode = user.dept.companyCode;//部门公司代码
var XMflag= false;
//查询区域
var searchForm = {
    init:function(){
        var custId = getQueryString("custId");//获取统计概况页跳转的请求参数
        var custName = getQueryString("custName");//获取统计概况页跳转的请求参数
        searchForm.loadMediaType();
        searchForm.getDeptId();
        searchForm.loadDept();
        searchForm.loadWorker(deptId,'YW', custId);
    },
    //加载媒体类型
    loadMediaType: function(){
        $.ajax({
            url: baseUrl+"/mediaPlate/0",  //mediaType?parentId=0
            type: "get",
            dataType:"json",
            success: function(data){
                if(data){
                    var mTypeEle = $("[name='mediaType']");
                    for(var i=0;i<data.length;i++){
                        var mType = data[i];
                        mTypeEle.append("<option value='${id}'>${name}</option>".replace("${id}",mType.id).replace("${name}",mType.name));
                    }
                }
            }
        });
    },
    getDeptId: function(){
        if(deptCompanyCode == "JT" && (deptCode == "CW" || isZC() || deptCode == "GL")){
            requestData(null,"/dept/getRootDept","POST",function (result) {
                var root = result.data.root;
                if (root){
                    deptId = root.id;//整个集团的业务和媒介部
                    deptName = root.name;
                }else{
                    deptId = 517;//整个集团的业务和媒介部
                    deptName = "集团";
                }
            });
        }else if(deptCode == "CW" || isZC() || deptCode == "GL"){
            requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST",function (result) {
                var company = result.data.company;
                if (company){
                    deptId = company.id;//整个集团的业务和媒介部
                    deptName = company.name;
                }
            });
        }
        $("#currentDeptId").val(deptId);
        $("#chooseDeptName").val(deptName);
    },
    loadDept:function(){
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        var deptDiv = document.getElementById("deptDiv");
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
        if(((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'YW'|| user.dept.code == 'GL')) || user.dept.code == 'CW'){
            deptDiv.style.display = 'block';
        }
    },
    //加载此部门下的业务员
    loadWorker: function(deptId,roleType,companyUserId){
        var ele = $("#currentUserId");
        //如果没有部门权限，则只加载当前用户
        if(!user.currentDeptQx){
            ele.append("<option value="+user.id+">"+user.name+"</option>");
        }else {
            ele.empty();
            ele.append('<option value="">全部</option>');
            if(roleType){
                searchForm.loadDeptUser(deptId,roleType,"currentUserId",searchForm.worker);
            }
        }
        var ywyVal = $("#currentUserId option:selected").val();
        //根据业务员加载对接人
        searchForm.loadCompanyUser(ywyVal, deptId, companyUserId);
    },
    loadDeptUser: function (deptId, roleType, attr) {
        var attribute = attr || 'users';
        layui.use(['form'], function () {
            Views.layuiForm = layui.form;
            var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
            $.ajax({
                    url: baseUrl + "/user/listUserByDeptAndRole",
                    type: "post",
                    data: {deptId: deptId, roleType: roleType},
                    async: true,
                    dataType: "json",
                    success: function (users) {
                        var userList = users.data.list;
                        if(userList && userList.length > 0){
                            for (var i = 0; i < userList.length; i++) {
                                ele.append("<option value=" + userList[i].id + ">" + userList[i].name + "</option>");
                                Views.layuiForm.render();
                            }
                        }
                        layui.use('form', function(){  //刷新下拉列表
                            var form = layui.form;
                            form.render('select');
                        });
                    }
                }
            );
        });
    },
    //根据业务部门和业务员加载对接人
    loadCompanyUser: function(userId, deptId, companyUserId){
        $.ajax(
            {
                url: baseUrl+"/crm/company/listCompanyUser",
                type: "post",
                async: true,
                data: {userId: userId, currentDeptId: deptId},
                dataType:"json",
                success:function(data){
                    var ele = $("#docPeo");
                    ele.empty();
                    ele.append('<option value="">全部</option>');
                    if(data){
                        for(var i = 0;i < data.length; i++){
                            var temp = data[i];
                            if(temp.id == companyUserId){
                                ele.append("<option selected='selected' value="+temp.id+">"+temp.name+"</option>");
                                continue;
                            }
                            ele.append("<option value="+temp.id+">"+temp.name+"</option>");
                        }
                    }
                    layui.use('form', function(){  //刷新下拉列表
                        var form = layui.form;
                        form.render('select');
                    });
                }
            }
        );
    },
    //显示或隐藏发布日期
    showIssuedDate: function(val){
        var btnNode = $("#searchButton");
        if(val == 3){
            $("#issuedDateFormGroup").show();
            $("#searchButton").remove();
            $("#rowTwo").append(btnNode);
        }else{
            $("#issuedDateFormGroup").hide();
            $("#searchButton").remove();
            $("#rowOne").append(btnNode);
            searchForm.search();
        }
    },
    //查询
    search: function(){
        var searchFormData = $("#searchForm").serializeJson();

        /*if(searchFormData.timeQuantum == 1 || searchFormData.timeQuantum == 2){
            $("#statisticsResultTable").hide();
            $("#statisticsResultPie").hide();
            $("#statisticsResultChart").attr("class","col-sm-12");
        }else{
            $("#statisticsResultTable").show();
            $("#statisticsResultPie").show();
            $("#statisticsResultChart").attr("class","col-sm-6");
        }*/
        //加载客户统计结果
        $.ajax({
            url: baseUrl+"/statistics/custStatisticsResult",  //baseUrl+"/statistics/statisticsResult"
            type: "post",
            data: searchFormData,
            async: true,
            dataType:"json",
            success:function(resData){
                $(".val-content").each(function (i, ele) {
                    var val = resData.data.result.custStatistics[ele.id] || 0,id_ = ele.id;
                    if(id_ === 'articleNum' || id_ === 'custNum'){
                        $(ele).text(fmtMoneyBringUnit(val,"件"));
                    }else{
                        $(ele).text(fmtMoneyBringUnit(val));
                    }
                    if(id_ === 'profit' || id_ === 'noIncomeAmount'){
                        var rate = "0.00%";
                        if((resData.data.result.custStatistics.saleAmount || 0) != 0){
                            rate = (val / resData.data.result.custStatistics.saleAmount * 100).toFixed(2)+"%";
                        }
                        $("#"+id_+"Rate").text(rate);
                    }
                });
                custTrend.reflushTrend(resData.data.result.trend);
                custTrend.reflush(resData.data.result.mediaTypeRate);
            }
        });
        //加载客户排名
        custRanking.loadTable();
    }
};

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
//获取部门树数据
function getTreeData() {
    var deptTreeData = {};
    //具体查询
    requestData({deptId: deptId,deptCode:'YW'},"/dept/listAllDeptByIdAndCode","POST",function (result) {
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
};

$(document).ready(function () {
    //加载tab页
    layui.use('element', function(){
        var element = layui.element;
        element.on('tab(docDemoTabBrief)', function(data){
            custRanking.tabTableSelect(data.index);//当选择对应tab栏目触发表格刷新
            $("#tabTableExportBtn").data("index",data.index);
        });
    });
    statisticsModal.init();//初始化模态框

    //查询条件初始化
    searchForm.init();
    //页面自动加载
    searchForm.search();

    var project = projectDirector();
    if( project && project.contains(user.id)){
        XMflag =true;
        $("#hideFlag").hide();
    }else {
        XMflag =false;
        $("#hideFlag").show();
    }
    /* $("#exportBtn").click(function () {
         var params = $("#searchForm").serializeJson();
         location.href = "/statistics/statisticsRankingAll" + "?" + $.param(params);
     });*/
    $("#tabTableExportBtn").click(function () {
        var params = $("#searchForm").serializeJson();
        if($("#tabTableExportBtn").data("index") == 1){ //新成交客户排名
            params.newCustFlag = true;
        }
        location.href = "/statistics/statisticsRankingAll" + "?" + $.param(params);
    });

    $("#selDept").click(function () {
        $("#deptModal").modal('toggle');
    });

    $('#treeview').treeview({
        data: [getTreeData()],
        onNodeSelected: function (event, data) {
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            $("#deptModal").modal('hide');
            $("#currentDeptId").val(data.id);
            $("#chooseDeptName").val(data.text);
            searchForm.loadWorker(data.id,"YW"); //查询业务员工
            layui.use('form', function(){
                layui.form.render('select');//layui重新渲染下拉列表
            });

            searchForm.loadCompanyUser("", data.id);
            searchForm.search();
        }
    });

    $("#cleanDept").click(function () {
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        $("#currentUserId").empty();//初始化
        $("#currentUserId").append('<option value="">全部</option>');
        $("#currentDeptId").val(deptId);//部门初始化
        $("#chooseDeptName").val(deptName);//部门初始化
        searchForm.loadDept();
        searchForm.loadWorker(deptId,'YW');
        layui.use('form', function(){  //刷新下拉列表
            var form = layui.form;
            form.render('select');
        });
        searchForm.search();
    });

    //使用layui表单，下拉列表改变事件
    layui.use('form', function(){
        var form = layui.form;
        //时间改变事件
        form.on('select(timeQuantum)', function(data){
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            searchForm.showIssuedDate(data.value);
        });
        //板块改变事件
        form.on('select(mediaType)', function(data){
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            searchForm.search();
        });
        //业务员改变事件
        form.on('select(user)', function(data){
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            searchForm.loadCompanyUser(data.value);
            searchForm.search();
            form.render('select');
        });
        //对接人改变事件
        form.on('select(cust)', function(data){
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            searchForm.search();
        });
    });
});
//统计趋势图
var custTrend = {
    pie1: {},
    pie2: {},
    trend: {},
    trendOption:{
        "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E"],
        "backgroundColor": "#fff",
        "legend": {"data": ["稿件数量", "成交总额","未到款额", "逾期款金额","利润"]},
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
                name: '(单位：件、元、元、元、元)'
            }
        ],
        "series": [
            {
                "name": "稿件数量",
                "type": "line",
                // stack:"总量",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            },
            {
                "name": "成交总额",
                type: 'line',
                // stack:"总量",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            },
            {
                "name": "未到款额",
                type: 'line',
                // stack:"总量",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            },
            {
                "name": "逾期款金额",
                type: 'line',
                // stack:"总量",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            },
            {
                "name": "利润",
                type: 'line',
                // stack:"总量",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            }
        ]
    }, //趋势图表配置
    option1: {
        "title": {
            text:"板块占比",
            subtext:"成交金额",
            textStyle:{
                color: '#009688',
                fontSize: '16'
            },
            x:"center"
        },
        "backgroundColor": "#fff",
        "legend": {
            x:'left',
            y:'center',
            padding: 5,
            itemHeight:14,
            itemGap: 5,
            orient : 'vertical',
            "data": []
        },
        noDataLoadingOption:{
            effect:'bar',
            text:'板块占比【成交金额】暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('statisticsPie1');
                if ($(id).width() - p[0]- $(id).find("div .echarts-tooltip").width()-20 <0) {
                    p[0] = p[0] - $(id).find("div .echarts-tooltip").width() -40;
                }
                return [p[0], p[1]];
            },
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": false,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "板块占比",
                "type": "pie",
                "radius": ["30%","45%"],
                "center": ["60%", "50%"],
                "data": [],
                "itemStyle": {
                    normal : {
                        label : {
                            show : false
                        },
                        labelLine : {
                            show : false
                        }
                    },
                    emphasis : {
                        label : {
                            show : true,
                            position : 'center',
                            textStyle : {
                                fontSize : '14',
                                fontWeight : 'bold'
                            }
                        }
                    }
                }
            }]
    },
    option2: {
        "title": {
            text:"板块占比",
            subtext:"利润",
            textStyle:{
                color: '#009688',
                fontSize: '16'
            },
            x:"center"
        },
        "backgroundColor": "#fff",
        "legend": {
            x:'left',
            y:'center',
            padding: 5,
            itemHeight:14,
            itemGap: 5,
            orient : 'vertical',
            "data": []
        },
        noDataLoadingOption:{
            effect:'bar',
            text:'板块占比【利润】暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('statisticsPie2');
                if ($(id).width() - p[0]- $(id).find("div .echarts-tooltip").width()-20 <0) {
                    p[0] = p[0] - $(id).find("div .echarts-tooltip").width() -40;
                }
                return [p[0], p[1]];
            },
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": false,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "板块占比",
                "type": "pie",
                "radius": ["30%","45%"],
                "center": ["60%", "50%"],
                "data": [],
                "itemStyle": {
                    normal : {
                        label : {
                            show : false
                        },
                        labelLine : {
                            show : false
                        }
                    },
                    emphasis : {
                        label : {
                            show : true,
                            position : 'center',
                            textStyle : {
                                fontSize : '14',
                                fontWeight : 'bold'
                            }
                        }
                    }
                }
            }]
    },
    initChart: function () {
        custTrend.pie1 = echarts.init(document.getElementById('statisticsPie1'));
        custTrend.pie2 = echarts.init(document.getElementById('statisticsPie2'));
        //点击事件
        custTrend.pie1.on('click', function (params) {
            custRanking.toggleModal(params.data.mediaTypeId,params.data.name,"mediaType");
        });
        custTrend.pie2.on('click', function (params) {
            custRanking.toggleModal(params.data.mediaTypeId,params.data.name,"mediaType");
        });
    },
    calHeight: function (option, statisticsPieId) {
        var num = option.legend.data.length;//图例数量
        var height = 14;//默认高度14
        if(num > 15){//当图例数量大于15时。图例会分成多行，进行单列算法计算
            var itemCap = option.legend.itemGap;//图例间隔
            var padding =  option.legend.padding;//图例内边距
            var eleHeight = $(statisticsPieId).innerHeight();//div总高度 = 图例高度和，计算公式：eleHeight = padding * 2 + itemCap*(num-1)+heigth*num 向下取整
            height = Math.floor((eleHeight-(padding*2 + itemCap*(num)))/num);
        }
        return height;
    },
    load: function () {
        setTimeout(function () {
            custTrend.initChart();
            custTrend.option1.legend.itemHeight = custTrend.calHeight(custTrend.option1, "#statisticsPie1");
            custTrend.option2.legend.itemHeight = custTrend.calHeight(custTrend.option2, "#statisticsPie2");
            custTrend.pie1.setOption(custTrend.option1);
            custTrend.pie2.setOption(custTrend.option2);
        },0);
    },
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            custTrend.trend = echarts.init(document.getElementById('statisticsTrend'));
            custTrend.trend.setOption(custTrend.trendOption);
            window.onresize = function () {
                custTrend.trend.resize();
            }
        },0);
    },
    reflushPie1: function (dataMap) {
        custTrend.option1.legend.data = [];//清空原数据
        custTrend.option1.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            custTrend.option1.legend.data[i] = dataMap[i].mediaTypeName;
            var serie1 = {name:dataMap[i].mediaTypeName,value:dataMap[i].saleAmount,mediaTypeId:dataMap[i].mediaTypeId};//数据
            custTrend.option1.series[0].data[i] = serie1;
        }
    },
    reflushPie2: function (dataMap) {
        custTrend.option2.legend.data = [];//清空原数据
        custTrend.option2.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            custTrend.option2.legend.data[i] = dataMap[i].mediaTypeName;
            var serie2 = {name:dataMap[i].mediaTypeName,value:dataMap[i].profit,mediaTypeId:dataMap[i].mediaTypeId};//数据
            custTrend.option2.series[0].data[i] = serie2;
        }
    },
    reflushTrend: function (dataList){
        //清空原来的数据
        custTrend.trendOption.xAxis[0].data = [];
        custTrend.trendOption.series[0].data = [];
        custTrend.trendOption.series[1].data = [];
        custTrend.trendOption.series[2].data = [];
        custTrend.trendOption.series[3].data = [];
        custTrend.trendOption.series[4].data = [];
        //数据封装
        var timeUnit = $("#timeQuantum").val() == 1 ? "日" : "月";
        for (var i = 0; i < dataList.length; i++) {
            custTrend.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
            custTrend.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
            custTrend.trendOption.series[1].data[i] = dataList[i].saleAmount || 0;
            custTrend.trendOption.series[2].data[i] = dataList[i].noIncomeAmount || 0;
            custTrend.trendOption.series[3].data[i] = dataList[i].dqysIncomeAmount || 0;
            custTrend.trendOption.series[4].data[i] = dataList[i].profit || 0;
        }
        //加载图表
        custTrend.loadTrend();
    },
    reflush: function (mediaTypeList) {
        var params = $("#searchForm").serializeJson();
        custTrend.reflushPie1(mediaTypeList);
        custTrend.reflushPie2(mediaTypeList);
        custTrend.load();
    }
};
/*var statisticsResult = {
    //左边的柱状图
    chart: {},
    //右边的饼图
    pie: {},
    //柱状图的option
    option: {
        title : {
            text: '客户统计结果',
            subtext: '',
            x:'left'
        },
        color: ['#e5323e','#003366','#006699','#4cabce','#470024'],
        backgroundColor: "#fff",
        legend: {
            y: "8px",
            data : ["成交总额","未到款额","逾期未到款额","到期应收金额","利润"]
        },
        tooltip: {
            show: true,
            trigger: 'axis'
        },
        xAxis : [
            {
                type : 'category',
                data : []
            }
        ],
        yAxis : [
            {
                type : 'value',
                name: '(单位/元)'
            }
        ],
        series : [
            {
                "name":"成交总额",
                "type":"bar",
                "data":[]
            },
            {
                "name":"未到款额",
                "type":"bar",
                "data":[]
            },
            {
                "name":"逾期未到款额",
                "type":"bar",
                "data":[]
            },
            {
                "name":"到期应收金额",
                "type":"bar",
                "data":[]
            },
            {
                "name":"利润",
                "type":"bar",
                "data":[]
            }
        ]
    },
    //饼图的option
    pieOption: {},
    init: function(){
        statisticsResult.chart = echarts.init(document.getElementById('statisticsResultChart'));
        statisticsResult.pie = echarts.init(document.getElementById('statisticsResultPie'));
    },
    load: function(){
        statisticsResult.chart.setOption(statisticsResult.option,true);
    },
    loadPie: function(opt){
        $.extend(true,statisticsResult.pieOption,statisticsResult.option);
        var val1 = opt.series[0].data[0] || 0;
        var val2 = opt.series[1].data[0] || 0;
        var val3 = opt.series[2].data[0] || 0;
        var val4 = opt.series[3].data[0] || 0;
        var val5 = opt.series[4].data[0] || 0;
        statisticsResult.pieOption.title = {};
        statisticsResult.pieOption.xAxis = null;
        statisticsResult.pieOption.yAxis = null;
        statisticsResult.pieOption.series = [
            {
                name: '客户统计结果',
                type: 'pie',
                radius : '55%',
                center: ['50%', '60%'],
                data:[
                    {value:val1, name:'成交总额'},
                    {value:val2, name:'未到款额'},
                    {value:val3, name:'逾期未到款额'},
                    {value:val4, name:'到期应收金额'},
                    {value:val5, name:'利润'},
                ],
                itemStyle: {
                    emphasis: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0,0)'
                    }
                }
            }
        ];
        statisticsResult.pie.setOption(statisticsResult.pieOption);
    },
    loadTable: function(opt){
        var series = opt.series;
        var table = '<table style="width:100%;text-align:center;margin:60px 0 0 -20px;" class="ui-jqgrid-htable ui-common-table table table-bordered"><tbody><tr class="ui-jqgrid-labels">'
            + '<td>类型</td>'
            + '<td>' + '总额(数)' + '</td>'
            + '</tr>';
        for (var i = 0; i < opt.legend.data.length; i++) {
            var e = series[i].data[0] || "";
            table += '<tr class="ui-jqgrid-labels">'
                + '<td>' + opt.legend.data[i] + '</td>'
                + '<td>' + e + '</td>'
                + '</tr>';
        }
        table += '</tbody></table>';
        $("#statisticsResultTable").html(table);
    },
    reflush: function(dataList){
        var searchFormData = $("#searchForm").serializeJson();
        var dw = "月";
        if(searchFormData.timeQuantum == 4){
            dw = "日";
        }else if(searchFormData.timeQuantum == 5){
            dw = "";
        }

        statisticsResult.init();

        var len = statisticsResult.option.series[0].data.length;
        statisticsResult.option.xAxis[0].data = [];
        //清空原来的数据
        for(var i=0;i<len;i++){
            statisticsResult.option.series[0].data = [];
            statisticsResult.option.xAxis[0].data[i] = "";
            statisticsResult.option.series[0].data[i] = 0;
            statisticsResult.option.series[1].data[i] = 0;
            statisticsResult.option.series[2].data[i] = 0;
            statisticsResult.option.series[3].data[i] = 0;
            statisticsResult.option.series[4].data[i] = 0;
        }

        for(var i=0;i<dataList.length;i++){
            statisticsResult.option.xAxis[0].data[i] = dataList[i].month ? dataList[i].month+dw : "";
            statisticsResult.option.series[0].data[i] = dataList[i].saleAmount || 0;
            statisticsResult.option.series[1].data[i] = dataList[i].noIncomeAmount || 0;
            statisticsResult.option.series[2].data[i] = dataList[i].yqIncomeAmount || 0;
            statisticsResult.option.series[3].data[i] = dataList[i].dqysIncomeAmount || 0;
            statisticsResult.option.series[4].data[i] = dataList[i].profitAmount || 0;
        }

        //加载客户统计结果
        statisticsResult.load();

        if(searchFormData.timeQuantum == 1 || searchFormData.timeQuantum == 2){

        }else{
            statisticsResult.loadPie(statisticsResult.option);
            statisticsResult.loadTable(statisticsResult.option);
        }
    }
};*/

//客户排名
var custRanking = {
    chart: {},
    grid:{},
    option: {
        title : {
            text: '',
            subtext: '',
            x:'center'
        },
        color: ['#3398DB'],
        backgroundColor: "#fff",
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
        tooltip : {
            trigger: 'axis',
            axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis : [
            {
                type : 'category',
                data : [],
                axisTick: {
                    alignWithLabel: true
                }
            }
        ],
        yAxis : [
            {
                type : 'value'
            }
        ],
        series : [
            {
                name:'',
                type:'bar',
                data:[]
            }
        ]
    },
    custTable:{
        url: baseUrl+'/statistics/listCustStatisticsRankingByParam',
        postData: $("#searchForm").serializeJson(),
        datatype: "json",
        mtype: 'post',
        // data: mydata,
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        shrinkToFit: true,
        sortable: true,
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        prmNames: {rows: "size"},
        rowNum: 50,
        rowList: [10, 20, 50, 100],
        colNames: ['客户公司名称', '对接人ID', '对接人',  '成交金额', '未到款额','逾期款金额', '利润'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'companyName',
                index: 'companyName',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                search: true,
                hidden:false
            },
            {
                name: 'custId',
                index: 'custId',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden:true
            },
            {
                name: 'custName',
                index: 'custName',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    if (rows.userId == user.id){
                        return custRanking.getSingleLinkHtml(rows.custId,rows.custName,"cust");

                    }else {
                        if (XMflag){
                            return value == null ? "" :"****";
                        }else {
                            return custRanking.getSingleLinkHtml(rows.custId,rows.custName,"cust");

                        }
                    }
                }
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'noIncomeAmount',
                index: 'noIncomeAmount',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            },
            {
                name: 'dqysIncomeAmount',
                index: 'dqysIncomeAmount',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            },
            {
                name: 'profit',
                index: 'profit',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            }
        ],
        pager: "#custPager",
        viewrecords: true,
        caption: "客户排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    newCustTable:{
        url: baseUrl+'/statistics/listNewCustStatisticsRankingByParam',
        postData: $("#searchForm").serializeJson(),
        datatype: "json",
        mtype: 'post',
        // data: mydata,
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        shrinkToFit: true,
        sortable: true,
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        prmNames: {rows: "size"},
        rowNum: 50,
        rowList: [10, 20, 50, 100],
        colNames: ['客户公司名称', '对接人ID', '对接人',  '成交金额', '未到款额','逾期款金额', '利润'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'companyName',
                index: 'companyName',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                search: true,
                hidden:false
            },
            {
                name: 'custId',
                index: 'custId',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden:true
            },
            {
                name: 'custName',
                index: 'custName',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    if (rows.userId == user.id){
                        return custRanking.getSingleLinkHtml(rows.custId,rows.custName,"cust");
                    }else {
                        if (XMflag){
                            return value == null ? "" :"****";
                        }else {
                            return custRanking.getSingleLinkHtml(rows.custId,rows.custName,"cust");

                        }
                    }

                }
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'noIncomeAmount',
                index: 'noIncomeAmount',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            },
            {
                name: 'dqysIncomeAmount',
                index: 'dqysIncomeAmount',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            },
            {
                name: 'profit',
                index: 'profit',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            }
        ],
        pager: "#newCustPager",
        viewrecords: true,
        caption: "客户排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    initChart:function(){
        custRanking.chart = custRanking.chart = echarts.init(document.getElementById('statisticsRankingChart'));
    },
    init: function(){
        custRanking.initChart();
    },
    load: function(){
        custRanking.initChart();
        custRanking.chart.setOption(custRanking.option);
    },
    //加载右边的表格、加载图表
    loadTable:function(){
        //加载表格
        custRanking.tabTableSelect(0);//默认加载第一个tab
    },
    reflush: function(dataList){
        var rankTypeName = $("#rankType").find("option:selected").text();
        var rankType = $("#rankType").val();
        custRanking.option.title.text = rankTypeName;
        custRanking.option.series[0].name = rankTypeName;
        custRanking.option.xAxis[0].data = [];
        var len = custRanking.option.series[0].data.length;
        //清空数据
        custRanking.option.xAxis[0].data = [];
        custRanking.option.series[0].data = [];
        dataList = dataList.list || [];
        for(var i = 0;i<dataList.length;i++){
            if (XMflag){
                var companyName = dataList[i].companyName || "";
                custRanking.option.xAxis[0].data[i] = companyName;
            }else {
                var custName = dataList[i].custName || "";
                custRanking.option.xAxis[0].data[i] = custName;
            }
            // var custName = "";
            // if (dataList[i].userId == user.id ){
            //      custName = dataList[i].custName || "";
            // }else {
            //     if (XMflag){
            //         custName = "***"
            //     }else {
            //         custName = dataList[i].custName || "";
            //     }
            // }
            // custRanking.option.xAxis[0].data[i] = custName;
            if(rankType == 1){
                var saleAmount = dataList[i].saleAmount || 0;
                custRanking.option.series[0].data[i] = saleAmount;
            }else if(rankType == 2){
                var noIncomeAmount = dataList[i].noIncomeAmount || 0;
                custRanking.option.series[0].data[i] = noIncomeAmount;
            }else if(rankType == 3){
                var dqysIncomeAmount = dataList[i].dqysIncomeAmount || 0;
                custRanking.option.series[0].data[i] = dqysIncomeAmount;
            }else if(rankType == 4){
                var profitAmount = dataList[i].profit || 0;
                custRanking.option.series[0].data[i] = profitAmount;
            }
        }
        custRanking.load();
        // custRanking.loadTable();
    },
    tabTableSelect:function (index) {//右边tab页选择时，列表改变, index = tab下标
        $(".tabContent").css("display","none");
        $(".tabTitle").removeClass("layui-this");//移除tab选中
        $($(".tabTitle")[index]).addClass("layui-this");
        $("#tabTableExportBtn").data("index",index);//默认导出tab为业务排名的
        if(index == 0){//客户排名
            $("#custTableDiv").css("display","block");
            $("#custRankType").text("客户排名");
            $("#custRankType").data("custRankType",1);
            custRanking.grid.custTable = new dataGrid("custTable", custRanking.custTable, "custPager", "searchForm");
            custRanking.grid.custTable.loadGrid();
            custRanking.grid.custTable.setNavGrid();
            custRanking.grid.custTable.defaultParams = {};//清空历史表单数据
            custRanking.grid.custTable.search();
        }
        if(index == 1){//新成交客户排名
            $("#newCustTableDiv").css("display","block");
            $("#custRankType").text("新成交客户排名");
            $("#custRankType").data("custRankType",2);
            custRanking.grid.newCustTable = new dataGrid("newCustTable", custRanking.newCustTable, "newCustPager", "searchForm");
            custRanking.grid.newCustTable.loadGrid();
            custRanking.grid.newCustTable.setNavGrid();
            custRanking.grid.newCustTable.defaultParams = {};//清空历史表单数据
            custRanking.grid.newCustTable.search();
        }
        //加载图表
        custRanking.loadChart();
    },
    /**
     * 初始化或者下拉框的change事件触发
     */
    loadChart: function(){
        // layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        var searchFormData = $("#searchForm").serializeJson();
        searchFormData.rankType = $("#rankType").val();//排名类型
        var custRankUrl = "/statistics/listCustStatisticsRankingByParam";//默认客户排名
        if($("#custRankType").data("custRankType") == 2){ //新成交客户排名
            custRankUrl = "/statistics/listNewCustStatisticsRankingByParam";//新成交客户排名
        }
        $.ajax({
            url: baseUrl + custRankUrl,
            type: "post",
            data: searchFormData,
            dataType:"json",
            async: true,
            success:function(resData){
                custRanking.reflush(resData);
            }
        });
    },
    getSingleLinkHtml:function (id,value,type) { //获取单个a链接
        var html = "";
        if(id){
            value = value ? value : id;//如果value为空则展示ID
            html += "<a onclick=\"custRanking.toggleModal("+id+",'"+value+"','"+type+"');\">"+value+"</a>";
        }
        return html;
    },
    toggleModal:function (id,name,type) {
        if("cust" == type){
            var title = "["+name+"]-客户统计";
            statisticsModal.loadConfig({enterType:"cust",enterParam:{custId:id},title:title}); //加载用户配置
        }
        if("business" == type){
            var title = "["+name+"]-业务统计";
            statisticsModal.loadConfig({enterType:"business",enterParam:{currentUserId:id},title:title}); //加载用户配置
        }
        if("mediaUser" == type){
            var title = "["+name+"]-媒介统计";
            statisticsModal.loadConfig({enterType:"mediaUser",enterParam:{currentUserId:id},title:title}); //加载用户配置
        }
        if("mediaType" == type){
            var title = "["+name+"]-板块统计";
            statisticsModal.loadConfig({enterType:"mediaType",enterParam:{mediaType:id},title:title}); //加载用户配置
        }
        if("media" == type){
            var title = "["+name+"]-媒体统计";
            statisticsModal.loadConfig({enterType:"media",enterParam:{mediaId:id},title:title}); //加载用户配置
        }
        if("supplier" == type){
            var title = "["+name+"]-供应商统计";
            statisticsModal.loadConfig({enterType:"supplier",enterParam:{supplierId:id},title:title}); //加载用户配置
        }
        $("#statisticsModal").modal("toggle");
    }
};
/*
var custRanking = {
    chart: {},
    grid:{},
    option: {
        title : {
            text: '',
            subtext: '',
            x:'center'
        },
        color: ['#3398DB'],
        backgroundColor: "#fff",
        tooltip : {
            trigger: 'axis',
            axisPointer : {            // 坐标轴指示器，坐标轴触发有效
                type : 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
            }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis : [
            {
                type : 'category',
                data : [],
                axisTick: {
                    alignWithLabel: true
                }
            }
        ],
        yAxis : [
            {
                type : 'value'
            }
        ],
        series : [
            {
                name:'',
                type:'bar',
                data:[]
            }
        ]
    },
    tableObject:{
        url: baseUrl+'/statistics/statisticsRanking',
        postData: $("#searchForm").serializeJson(),
        datatype: "json",
        mtype: 'post',
        // data: mydata,
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 5,
        rowList: [10, 20, 30],
        colNames: ['客户公司名称', '对接人', '成交金额', '未到款额','逾期未到款额', '到期应收额', '利润'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'companyName',
                index: 'companyName',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                search: true,
                hidden:false
            },
            {
                name: 'custName',
                index: 'custName',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'noIncomeAmount',
                index: 'noIncomeAmount',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'yqIncomeAmount',
                index: 'yqIncomeAmount',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'dqysIncomeAmount',
                index: 'dqysIncomeAmount',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'profitAmount',
                index: 'profitAmount',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            }
        ],
        /!**
         * 翻页时保存当前页面的选中数据
         * @param pageBtn
         *!/
        onPaging:function(pageBtn){
            //跨页面选择

        },
        gridComplete: function () {

        },
        pager: "#pager",
        viewrecords: true,
        caption: "客户排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    initChart:function(){
        custRanking.chart = custRanking.chart = echarts.init(document.getElementById('statisticsRankingChart'));
    },
    init: function(){
        custRanking.initChart();
        custRanking.grid = new dataGrid("table", custRanking.tableObject , "pager", "searchForm");
        custRanking.grid.loadGrid();
    },
    load: function(){
        custRanking.initChart();
        custRanking.chart.setOption(custRanking.option);
    },
    //加载右边的表格
    loadTable:function(){
        //加载表格
        custRanking.grid.search();
    },
    reflush: function(dataList){

        var tit = $("#rankType").find("option:selected").text();
        var v = $("#rankType").val();
        custRanking.option.title.text = tit;
        custRanking.option.series[0].name = tit;

        custRanking.option.xAxis[0].data = [];

        var len = custRanking.option.series[0].data.length;
        //清空数据
        custRanking.option.xAxis[0].data = [];
        for(var i=0;i<len;i++){
            custRanking.option.xAxis[0].data[i] = "";
            custRanking.option.series[0].data[i] = 0;
        }

        dataList = dataList.list || [];

        for(var i = 0;i<dataList.length;i++){
            var custName = dataList[i].custName || "";
            var saleAmount = dataList[i].saleAmount || 0;
            var dqysIncomeAmount = dataList[i].dqysIncomeAmount || 0;
            var noIncomeAmount = dataList[i].noIncomeAmount || 0;
            var yqIncomeAmount = dataList[i].yqIncomeAmount || 0;
            var profitAmount = dataList[i].profitAmount || 0;
            custRanking.option.xAxis[0].data[i] = custName;
            if(v == 1){
                custRanking.option.series[0].data[i] = saleAmount;
            }else if(v == 2){
                custRanking.option.series[0].data[i] = noIncomeAmount;
            }else if(v == 3){
                custRanking.option.series[0].data[i] = yqIncomeAmount;
            }else if(v == 4){
                custRanking.option.series[0].data[i] = dqysIncomeAmount;
            }else if(v == 5){
                custRanking.option.series[0].data[i] = profitAmount;
            }
        }
        custRanking.load();
        custRanking.loadTable();
    },
    /!**
     * 初始化或者下拉框的change事件触发
     *!/
    search: function(){
        var searchFormData = $("#searchForm").serializeJson();
        $.ajax({
            url: baseUrl+"/statistics/statisticsRanking",
            type: "post",
            data: searchFormData,
            dataType:"json",
            success:function(resData){
                // custRanking.reflush([{custName:"张三",saleAmount:100},{custName:"李四",saleAmount:200}]);
                custRanking.reflush(resData);
            }
        });
    }
};*/
