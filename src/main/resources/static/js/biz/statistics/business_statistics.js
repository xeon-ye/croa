var deptId = user.dept.id;//当前用户部门ID
var deptCode = user.dept.code;//当前部门编码
var deptName = user.dept.name;//当前部门名称
var deptCompanyCode = user.dept.companyCode;//部门公司代码
var XMflag= false;
//查询区域
var searchForm = {
    init:function(){
        var businessId = getQueryString("businessId");//获取统计概况页跳转的请求参数
        var businessName = getQueryString("businessName");//获取统计概况页跳转的请求参数
        searchForm.loadMediaType();
        searchForm.getDeptId();
        searchForm.loadDept();
        searchForm.loadWorker(deptId,"YW",businessId,businessName);
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
    //加载媒体类型
    loadMediaType: function(){
        $.ajax({
            url: baseUrl+"/mediaPlate/0",  //mediaType?parentId=0
            dataType:"json",
            type: "get",
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
    loadWorker: function(deptId,roleType,businessId,businessName){
        var ele = $("#currentUserId");
        //如果没有部门权限，则只加载当前用户
        if(!user.currentDeptQx){
            ele.append("<option value="+user.id+">"+user.name+"</option>");
        }else {
            ele.empty();
            if(businessId){
                ele.append("<option value=" + businessId + ">" + businessName + "</option>");
                ele.val(businessId);
            }else{
                ele.append('<option value="">全部</option>');
                if(roleType){
                    searchForm.loadDeptUser(deptId,roleType,"currentUserId",businessId,businessName);
                }
            }
        }
        layui.use('form', function(){  //刷新下拉列表
            var form = layui.form;
            form.render('select');
        });
        var ywyVal = $("#currentUserId option:selected").val();
        //根据业务员加载对接人
        searchForm.loadCompanyUser(ywyVal);
    },
    loadDeptUser: function (deptId, roleType, attr) {
        var attribute = attr || 'users';
        layui.use(['form'], function () {
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
                data: {userId: userId, currentDeptId: deptId},
                dataType:"json",
                async: true,
                success:function(data){
                    var ele = $("#docPeo");
                    ele.empty();
                    ele.append('<option value="">全部</option>');
                    if(data){
                        for(var i = 0;i < data.length; i++){
                            var docPeo = data[i];
                            ele.append("<option value="+docPeo.id+">"+docPeo.name+"</option>");
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
        //加载客户统计结果
        $.ajax({
            url: baseUrl+"/businessStatistics/businessStatisticsResult",  //baseUrl+"/businessStatistics/statisticsResult"
            type: "post",
            data: searchFormData,
            dataType:"json",
            async: true,
            success:function(resData){
                $(".val-content").each(function (i, ele) {
                    var val = resData.data.result.businessStatistics[ele.id] || 0,id_ = ele.id;
                    if(id_ === 'articleNum'){
                        $(ele).text(fmtMoneyBringUnit(val,"件"));
                    }else if(id_ === 'custNum'){
                        $(ele).text(fmtMoneyBringUnit(val,"个"));
                    }else{
                        $(ele).text(fmtMoneyBringUnit(val));
                    }

                    if(id_ === 'profit' || id_ === 'noIncomeAmount'){
                        var rate = "0.00%";
                        if((resData.data.result.businessStatistics.saleAmount || 0) != 0){
                            rate = (val / resData.data.result.businessStatistics.saleAmount * 100).toFixed(2)+"%";
                        }
                        $("#"+id_+"Rate").text(rate);
                    }
                });
                // statisticsResult.reflush(resData);
                businessTrend.reflushTrend(resData.data.result.trend);
                businessTrend.reflush(resData.data.result.mediaTypeRate,resData.data.result.custTypeRate,resData.data.result.custIndustryType,resData.data.result.artTypeRate)
            }
        });

        //重新加载下面的表格
        custRanking.grid.search();

        //部门领导才有权限
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        if(((currentDeptQx || isZC()) && (user.dept.code == 'YW'|| user.dept.code == 'GL')) || user.dept.code == 'CW'){
            deptBusiness.init();
            deptSaleAmountSort.init();
            deptIncomeSort.init();
        }
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
};
//获取部门树数据
function getTreeData() {
    var deptTreeData = {};
    //具体查询
    requestData({deptId: deptId, deptCode:'YW'},"/dept/listAllDeptByIdAndCode","POST",function (result) {
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

$("document").ready(function () {
    //加载时间控件
    var issuedDateStart = {
        elem: '#issuedDateStart',
        format: 'YYYY/MM/DD',
        istime: false,
        istoday: false,
        choose:function () {
            var startTime = $("#issuedDateStart").val();
            var endTime = $("#issuedDateEnd").val();
            if(startTime && endTime && startTime > endTime){
                layer.msg("开始时间不能大于结束时间");
                $("#issuedDateStart").val("")
                return;
            }
            searchForm.search();//刷新数据
        }
    };
    laydate(issuedDateStart);
    var issuedDateEnd = {
        elem: '#issuedDateEnd',
        format: 'YYYY/MM/DD',
        istime: false,
        istoday: false,
        choose:function () {
            var startTime = $("#issuedDateStart").val();
            var endTime = $("#issuedDateEnd").val();
            if(startTime && endTime && startTime > endTime){
                layer.msg("结束时间不能小于开始时间");
                $("#issuedDateEnd").val("")
                return;
            }
            searchForm.search();//刷新数据
        }
    };
    laydate(issuedDateEnd);

    var project = projectDirector();
    if( project && project.contains(user.id)){
        XMflag =true;
        $("#XMhide").hide();
    }else{
        $("#XMhide").show();
    }

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
            searchForm.loadCompanyUser(data.value)
            searchForm.search();
            form.render('select');
        });
        //对接人改变事件
        form.on('select(cust)', function(data){
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            searchForm.search();
        });
    });

    statisticsModal.init();//初始化模态框
    //查询条件初始化
    searchForm.init();
    //客户排名初始化
    custRanking.init();
    //页面自动加载
    searchForm.search();

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
        searchForm.loadWorker(deptId, "YW");
        layui.use('form', function(){  //刷新下拉列表
            var form = layui.form;
            form.render('select');
        });
        searchForm.search();
    });
});
//统计结果
//统计趋势图
var businessTrend = {
    pie1: {},
    pie2: {},
    pie3: {},
    pie4: {},
    trend: {},
    trendOption:{
        "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E","#FF69B4"],
        "backgroundColor": "#fff",
        "legend": {"data": ["稿件数量", "客户数量", "成交总额","未到款额", "逾期款金额","利润"]},
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
                name: '(单位：件、个、元、元、元、元)'
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
                "name": "客户数量",
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
            subtext:"利润金额",
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
            text:'板块占比【利润金额】暂无数据',
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
                "center": ["55%", "50%"],
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
            text:"客户行业类型占比",
            subtext:"利润金额",
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
            text:'客户行业类型占比【利润金额】暂无数据',
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
                "name": "客户行业类型占比",
                "type": "pie",
                "radius": ["30%","45%"],
                "center": ["55%", "50%"],
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
    option3: {
        "title": {
            text:"客户公司类型占比",
            subtext:"利润金额",
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
            text:'客户公司类型占比【利润金额】暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('statisticsPie3');
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
                "name": "客户公司类型占比",
                "type": "pie",
                "radius": ["30%","45%"],
                "center": ["55%", "50%"],
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
    option4: {
        "title": {
            text:"稿件行业类型占比",
            subtext:"利润金额",
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
            text:'稿件行业类型占比【利润金额】暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('statisticsPie4');
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
                "name": "稿件行业类型占比",
                "type": "pie",
                "radius": ["30%","45%"],
                "center": ["55%", "50%"],
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
        businessTrend.pie1 = echarts.init(document.getElementById('statisticsPie1'));
        businessTrend.pie2 = echarts.init(document.getElementById('statisticsPie2'));
        businessTrend.pie3 = echarts.init(document.getElementById('statisticsPie3'));
        businessTrend.pie4 = echarts.init(document.getElementById('statisticsPie4'));
        //点击事件
        businessTrend.pie1.on('click', function (params) {
            custRanking.toggleModal(params.data.mediaTypeId,params.data.name,"mediaType");
        });
    },
    calHeight: function (option, statisticsPieId) {
        var num = option.legend.data.length;//图例数量
        var height = 14;//默认高度14
        if(num > 16){//当图例数量大于15时。图例会分成多行，进行单列算法计算
            var itemCap = option.legend.itemGap;//图例间隔
            var padding =  option.legend.padding;//图例内边距
            var eleHeight = $(statisticsPieId).innerHeight();//div总高度 = 图例高度和，计算公式：eleHeight = padding * 2 + itemCap*(num-1)+heigth*num 向下取整
            height = Math.floor((eleHeight-(padding*2 + itemCap*(num)))/num);
        }
        return height;
    },
    load: function () {
        setTimeout(function () {
            businessTrend.initChart();
            businessTrend.option1.legend.itemHeight = businessTrend.calHeight(businessTrend.option1, "#statisticsPie1");
            businessTrend.option2.legend.itemHeight = businessTrend.calHeight(businessTrend.option2, "#statisticsPie2");
            businessTrend.option3.legend.itemHeight = businessTrend.calHeight(businessTrend.option3, "#statisticsPie3");
            businessTrend.option4.legend.itemHeight = businessTrend.calHeight(businessTrend.option4, "#statisticsPie4");
            businessTrend.pie1.setOption(businessTrend.option1);
            businessTrend.pie2.setOption(businessTrend.option2);
            businessTrend.pie3.setOption(businessTrend.option3);
            businessTrend.pie4.setOption(businessTrend.option4);
        },0);
    },
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            businessTrend.trend = echarts.init(document.getElementById('statisticsTrend'));
            businessTrend.trend.setOption(businessTrend.trendOption);
            window.onresize = function () {
                businessTrend.trend.resize();
            }
        },0);
    },
    reflushPie1: function (dataMap) {
        businessTrend.option1.legend.data = [];//清空原数据
        businessTrend.option1.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            businessTrend.option1.legend.data[i] = dataMap[i].mediaTypeName;
            var serie1 = {name:dataMap[i].mediaTypeName,value:dataMap[i].profit,mediaTypeId:dataMap[i].mediaTypeId};//数据
            businessTrend.option1.series[0].data[i] = serie1;
        }
    },
    reflushPie2: function (dataMap) {
        businessTrend.option2.legend.data = [];//清空原数据
        businessTrend.option2.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            businessTrend.option2.legend.data[i] = dataMap[i].custIndustryType;
            var serie2 = {name:dataMap[i].custIndustryType,value:dataMap[i].profit};//数据
            businessTrend.option2.series[0].data[i] = serie2;
        }
    },
    reflushPie3: function (dataMap) {
        businessTrend.option3.legend.data = [];//清空原数据
        businessTrend.option3.series[0].data = [];//清空原数据
        //清空原来的数据
        var index3 = 0;
        for (var i = 0; i < dataMap.length; i++) {
            if(dataMap[i].custCompanyType != undefined){
                businessTrend.option3.legend.data[index3] = dataMap[i].custCompanyType;
                var serie3 = {name:dataMap[i].custCompanyType,value:dataMap[i].profit};//数据
                businessTrend.option3.series[0].data[index3] = serie3;
                index3++;
            }
        }
    },
    reflushPie4: function (dataMap) {
        businessTrend.option4.legend.data = [];//清空原数据
        businessTrend.option4.series[0].data = [];//清空原数据
        //清空原来的数据
        var index4 = 0;
        for (var i = 0; i < dataMap.length; i++) {
            if(dataMap[i].typeName != undefined){
                businessTrend.option4.legend.data[index4] = dataMap[i].typeName;
                var serie4 = {name:dataMap[i].typeName,value:dataMap[i].profit};//数据
                businessTrend.option4.series[0].data[index4] = serie4;
                index4++;
            }
        }
    },
    reflushTrend: function (dataList){
        //清空原来的数据
        businessTrend.trendOption.xAxis[0].data = [];
        businessTrend.trendOption.series[0].data = [];
        businessTrend.trendOption.series[1].data = [];
        businessTrend.trendOption.series[2].data = [];
        businessTrend.trendOption.series[3].data = [];
        businessTrend.trendOption.series[4].data = [];
        businessTrend.trendOption.series[5].data = [];
        //数据封装
        var timeUnit = $("#timeQuantum").val() == 1 ? "日" : "月";
        for (var i = 0; i < dataList.length; i++) {
            businessTrend.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
            businessTrend.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
            businessTrend.trendOption.series[1].data[i] = dataList[i].custNum || 0;
            businessTrend.trendOption.series[2].data[i] = dataList[i].saleAmount || 0;
            businessTrend.trendOption.series[3].data[i] = dataList[i].noIncomeAmount || 0;
            businessTrend.trendOption.series[4].data[i] = dataList[i].dqysIncomeAmount || 0;
            businessTrend.trendOption.series[5].data[i] = dataList[i].profit || 0;
        }
        //加载图表
        businessTrend.loadTrend();
    },
    reflush: function (mediaTypeList, custTypeList, custIndustryType, artTypeList) {
        businessTrend.reflushPie1(mediaTypeList);
        businessTrend.reflushPie2(custIndustryType);
        businessTrend.reflushPie3(custTypeList);
        businessTrend.reflushPie4(artTypeList);
        businessTrend.load();
    }
};
/*var statisticsResult = {
    //左边的柱状图
    chart: {},
    //柱状图的option
    option: {
        title : {
            text: '业务统计结果',
            subtext: '',
            x:'left'
        },
        backgroundColor: "#fff",
        legend: {
            y: "8px",
            data : ["登记客户数量","稿件数量","成交总额","未到款额","逾期未到款额","到期应收金额","利润"]
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
                "name":"登记客户数量",
                "type":"bar",
                "data":[]
            },
            {
                "name":"稿件数量",
                "type":"bar",
                "data":[]
            },
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

    init: function(){
        statisticsResult.chart = echarts.init(document.getElementById('statisticsResultChart'));
    },
    load: function(){
        statisticsResult.chart.setOption(statisticsResult.option,true);
    },
    reflush: function(dataList){
        var searchFormData = $("#searchForm").serializeJson();
        var dw = "";
        if(searchFormData.timeQuantum == 1){
            dw = "月";
        }else if(searchFormData.timeQuantum == 2){
            dw = "年";
        }
        statisticsResult.init();
        var len = statisticsResult.option.series[0].data.length;

        statisticsResult.option.series[0].data = [];
        statisticsResult.option.xAxis[0].data = [];
        //清空原来的数据
        for(var i=0;i<len;i++){
            statisticsResult.option.xAxis[0].data[i] = "";
            statisticsResult.option.series[0].data[i] = 0;
            statisticsResult.option.series[1].data[i] = 0;
            statisticsResult.option.series[2].data[i] = 0;
            statisticsResult.option.series[3].data[i] = 0;
            statisticsResult.option.series[4].data[i] = 0;
            statisticsResult.option.series[5].data[i] = 0;
            statisticsResult.option.series[6].data[i] = 0;
        }

        for(var i=0;i<dataList.length;i++){
            statisticsResult.option.xAxis[0].data[i] = dataList[i].month ? dataList[i].month+dw : "";
            statisticsResult.option.series[0].data[i] = dataList[i].custCount || 0;
            statisticsResult.option.series[1].data[i] = dataList[i].artCount || 0;
            statisticsResult.option.series[2].data[i] = dataList[i].saleAmount || 0;
            statisticsResult.option.series[3].data[i] = dataList[i].noIncomeAmount || 0;
            statisticsResult.option.series[4].data[i] = dataList[i].yqIncomeAmount || 0;
            statisticsResult.option.series[5].data[i] = dataList[i].dqysIncomeAmount || 0;
            statisticsResult.option.series[6].data[i] = dataList[i].profitAmount || 0;
        }
        //加载客户统计结果
        statisticsResult.load();
    }
};*/

//客户排名
var custRanking = {
    grid:{},
    tableObject:{
        url: baseUrl+'/businessStatistics/listCustForNotTrans',
        postData: $("#searchForm").serializeJson(),
        datatype: "json",
        mtype: 'get',
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
        rowNum: 50,
        rowList: [10, 20, 50, 100],
     /*   colNames: ['编号', '客户公司名称','客户公司类型','公司类型', '地区公司', '对接人信息','产品信息', '用户信息',
            '对接人姓名id','对接人姓名', '职位',"状态id" ,"状态","创建人id", "创建人","负责人id","负责人", "创建日期", "更新日期" ,
            "删除标志"],*/
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'companyId',
                index: 'companyId',
                label: 'companyId',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'companyName',
                index: 'companyName',
                label: '客户公司名称',
                editable: false,
                width: 140,
                align: "center",
                sortable: true,
                sorttype: "string",
                // cellattr: function (rowId, tv, rawObject, cm, rdata) {
                //     return "id='companyName" + rowId + "'";
                // },
                // formatter: function (a, b, rowdata) {
                //     // 公海客户只允许查看；
                //     if (rowdata.custProperty == 1) {
                //         return a;
                //     } else {
                //         var url = "javascript:page('/crm/company_all?companyId=${id}&companyName=${companyName}&op=edit','${title}')";
                //         url = url.replace("${id}", rowdata.id).replace("${companyName}", rowdata.companyName).replace("${title}", "编辑客户信息");
                //         var a = "<a href=" + url + ">" + rowdata.companyName + "</a>";
                //         return a;
                //     }
                // }
            },
            {
                name: 'type',
                index: 'type',
                label: '客户类型',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                // cellattr: function (rowId, tv, rawObject, cm, rdata) {
                //     return "id='type" + rowId + "'";
                // },
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "<span class='text-green'>企业客户</span>";
                    } else if (a == 0) {
                        return "<span>个人客户</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'standardize',
                index: 'standardize',
                label: '是否标准',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                // cellattr: function (rowId, tv, rawObject, cm, rdata) {
                //     return "id='standardize" + rowId + "'";
                // },
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "标准";
                    } else {
                        return "<span class='text-red'>非标准</span>";
                    }
                }
            },
            {
                name: 'companyUserId',
                index: 'companyUserId',
                label: '对接人id',
                editable: false,
                width: 35,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true,
            },
            {
                name: 'companyUserName',
                index: 'companyUserName',
                label: '对接人名字',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if(XMflag){
                        return a == null ? "":"*****" ;
                    }else {
                        return custRanking.getSingleLinkHtml(rowdata.companyUserId, rowdata.companyUserName,"cust");
                    }
                }
            },
            /*{
                name: 'mobile',
                index: 'mobile',
                label: '手机号',
                editable: false,
                width: 35,
                align: "center",
                sortable: false
            },
            {
                name: 'wechat',
                index: 'wechat',
                label: '微信',
                editable: false,
                width: 35,
                align: "center",
                sortable: false
            },
            {
                name: 'qq',
                index: 'qq',
                label: 'QQ',
                editable: false,
                width: 35,
                align: "center",
                sortable: false
            },*/
            {
                name: 'normalize',
                index: 'normalize',
                label: '是否规范',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "规范";
                    } else {
                        return "<span class='text-red'>非规范</span>";
                    }
                }
            },
            {
                name: 'protectStrong',
                index: 'protectStrong',
                label: '是否强保护',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                // cellattr: function (rowId, tv, rawObject, cm, rdata) {
                //     return "id='auditFlag" + rowId + "'";
                // },
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "<span style='color:#1ab394'>强保护</span>";
                    } else {
                        return "<span>弱保护</span>";
                    }
                }
            },
            {
                name: 'protectLevel',
                index: 'protectLevel',
                label: '保护登记',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 3) {
                        return "<span style='color:#1ab394'>保护(A)</span>";
                    } else if (a == 2) {
                        return "<span style='color:#1ab394'>保护(B)</span>";
                    } else if (a == 1) {
                        return "<span style='color:#1ab394'>保护(C)</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'createTime',
                index: 'createTime',
                label: '登记时间',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd hh:mm");
                }
            },
            {
                name: 'startTime',
                index: 'startTime',
                label: '开始时间',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'endTime',
                index: 'endTime',
                label: '结束时间',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'ywUserName',
                index: 'ywUserName',
                label: '负责人',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                hidden: false,
                formatter: function (value, grid, rows) {
                    if(value){
                        return custRanking.getSingleLinkHtml(rows.ywUserId, rows.ywUserName,"business");
                    }else{
                        return "";
                    }
                }
            }
        ],
        pager: jQuery("#pager"),
        viewrecords: true,
        caption: "近三个月未成交客户",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    init: function(){
        custRanking.grid = new dataGrid("table", custRanking.tableObject , "pager", "searchForm");
        custRanking.grid.loadGrid();
    },
    reflush: function(){
        custRanking.grid.reflush();
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

//部门业务量
var deptBusiness = {
    //点击功能需要保存的数组,保存当前展示的多个部门
    depts: [],
    //返回上一级需要保存的数组
    deptStack: [],
    dept: {}, //缓存当前部门列表
    init: function () {
        $.get("/dept/getFullDeptTreeByDeptId", {currentDeptId: $("#currentDeptId").val()},function (deptData) {
            $("#deptBusiness").show();
            deptBusiness.setValue(deptData.dept);
        }, "json");
    },
    chart: {},
    setValue: function (dept) {
        deptBusiness.dept = dept;
        deptBusiness.depts = [];
        var ds = ""; //当前查询部门列表
        for (var i = 0; i < dept.depts.length; i++) {
            var childDept = dept.depts[i];
            if(dept.level == 0){ //如果是部门根节点（集团），仅使用公司，或者集团业务部
                if(childDept.level = 1 && (childDept.code == 'GL' || childDept.code == 'YW')){
                    deptBusiness.depts.push(childDept);
                    ds += ("," + childDept.id);
                }
            }else{
                if(childDept.code === 'YW'){
                    deptBusiness.depts.push(childDept);
                    ds += ("," + childDept.id);
                }
            }
        }
        var searchFormData = $("#searchForm").serializeJson();
        searchFormData.list = ds;
        $.ajax({
            url: baseUrl + "/businessStatistics/everyDeptBusiness",
            data: searchFormData,
            dataType: "json",
            success: function (resData) {
                deptBusiness.reflush(resData.data.list);
            }
        });
    },
    clickEvent: function (param) {
        var deptId = param.data.deptId;
        var dept = null;
        if(deptBusiness.depts && deptBusiness.depts.length > 0){
            $.each(deptBusiness.depts,function (index,value) {
                if(value.id == deptId){
                    dept = value;
                }
            })
        }
        if (!dept || !dept.depts || dept.depts.length < 1) {
            parent.layer.alert("该部门下没有子部门");
            return;
        }
        $("#returnS").show();
        deptBusiness.deptStack.push(deptBusiness.dept);
        deptBusiness.setValue(dept);
    },
    //返回上一级
    returnS: function () {
        var dept = deptBusiness.deptStack.pop();
        if (deptBusiness.deptStack.length == 0) {
            $("#returnS").hide();
        }
        deptBusiness.setValue(dept);
    },
    option: {
        "title": {"text": "", "subtext": "", "x": "left"},
        "color": ["#e5323e", "#003366", "#006699"],
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["应收金额", "利润"]},
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
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：元、元)'
            }
        ],
        "series": [{"name": "应收金额", "type": "bar", "data": []},
            {"name": "利润", "type": "bar", "data": []}
        ]
    },
    load: function () {
        setTimeout(function () {
            deptBusiness.chart = echarts.init(document.getElementById('deptBusiness_chart'));
            deptBusiness.chart.setOption(deptBusiness.option);
            deptBusiness.chart.on("click", deptBusiness.clickEvent);
            window.onresize = function () {
                deptBusiness.chart.resize();
            }
        },0);

    },
    reflush: function (dataList) {
        var len = deptBusiness.option.series[0].data.length;
        //清空原来的数据
        deptBusiness.option.xAxis[0].data = [];
        deptBusiness.option.series[0].data = [];
        deptBusiness.option.series[1].data = [];

        for (var i = 0; i < dataList.length; i++) {
            deptBusiness.option.xAxis[0].data[i] = dataList[i].deptName || "";
            deptBusiness.option.series[0].data[i] = {value:(dataList[i].saleAmount || 0), deptId:dataList[i].deptId};
            deptBusiness.option.series[1].data[i] = {value:(dataList[i].profit || 0), deptId:dataList[i].deptId};
        }
        deptBusiness.load();
    }
};

//本部门的销售额排名
var deptSaleAmountSort = {
    chart: {},
    init: function () {
        $("#deptSaleAmountSort").show();
        deptSaleAmountSort.setValue("deptSaleAmountSortBox");
    },
    setValue: function (id) {
        var searchFormData = $("#searchForm").serializeJson();
        searchFormData.tjType = id;
        $.ajax({
            url: baseUrl + "/businessStatistics/businessTop",
            data: searchFormData,
            dataType: "json",
            success: function (resData) {
                deptSaleAmountSort.reflush(resData);
            }
        });
    },
    option: {
        "title": {"text": "业务量前5名", "subtext": "", "x": "left"},
        "color": ["#e5323e", "#003366", "#006699"],
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["应收金额"]},
        "tooltip": {
            "show": true,
            trigger: 'axis'
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：元)'
            }
        ],
        "series": [
            {"name": "应收金额", "type": "bar", "data": []}
        ],
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
    },
    load: function () {
        setTimeout(function () {
            deptSaleAmountSort.chart = echarts.init(document.getElementById('deptSaleAmountSort_chart'));
            deptSaleAmountSort.chart.setOption(deptSaleAmountSort.option, true);
        },0);
    },
    reflush: function (dataList) {
        var len = deptSaleAmountSort.option.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            deptSaleAmountSort.option.series[0].data = [];
            deptSaleAmountSort.option.xAxis[0].data = [];
            deptSaleAmountSort.option.series[0].data[i] = 0;
        }

        for (var i = 0; i < dataList.length; i++) {
            deptSaleAmountSort.option.xAxis[0].data[i] = dataList[i].userName || "";
            deptSaleAmountSort.option.series[0].data[i] = dataList[i].saleAmount || 0;
        }
        deptSaleAmountSort.load();
    }
};

//部门回款排名
var deptIncomeSort = {
    chart: {},
    init: function () {
        deptIncomeSort.setValue("deptIncomeSortBox");
    },
    setValue: function (id) {
        var searchFormData = $("#searchForm").serializeJson();
        searchFormData.tjType = id;
        $.ajax({
            url: baseUrl + "/businessStatistics/businessTop",
            data: searchFormData,
            dataType: "json",
            success: function (resData) {
                deptIncomeSort.reflush(resData);
            }
        });
    },
    option: {
        "title": {"text": "回款前5名", "subtext": "", "x": "left"},
        "color": ["#e5323e", "#003366", "#006699"],
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["入账金额"]},
        "tooltip": {
            "show": true,
            trigger: 'axis'
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：元)'
            }
        ],
        "series": [
            {"name": "入账金额", "type": "bar", "data": []}
        ],
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
    },
    load: function () {
        setTimeout(function () {
            deptIncomeSort.chart = echarts.init(document.getElementById('deptIncomeSort_chart'));
            deptIncomeSort.chart.setOption(deptIncomeSort.option, true);
        },0);
    },
    reflush: function (dataList) {
        var len = deptIncomeSort.option.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            deptIncomeSort.option.series[0].data = [];
            deptIncomeSort.option.xAxis[0].data = [];
            deptIncomeSort.option.series[0].data[i] = 0;
        }

        for (var i = 0; i < dataList.length; i++) {
            deptIncomeSort.option.xAxis[0].data[i] = dataList[i].userName || "";
            deptIncomeSort.option.series[0].data[i] = dataList[i].incomeAmount || 0;
        }
        deptIncomeSort.load();
    }
};