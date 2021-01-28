//查询区域
var searchForm = {
    init: function () {
        var mediaUserId = getQueryString("mediaUserId");//获取统计概况页跳转的请求参数
        var mediaUserName = getQueryString("mediaUserName");//获取统计概况页跳转的请求参数
        searchForm.loadMediaType();
        searchForm.loadDept();
        searchForm.loadWorker(null,null,mediaUserId,mediaUserName);
        searchForm.loadSupplier();
    },
    //加载媒体类型
    loadMediaType: function () {
        $.ajax({
            url: baseUrl + "/mediaPlate/0",  //mediaType?parentId=0
            type: "get",
            dataType: "json",
            success: function (data) {
                if (data) {
                    var mTypeEle = $("[name='mediaType']");
                    for (var i = 0; i < data.length; i++) {
                        var mType = data[i];
                        mTypeEle.append("<option value='${id}'>${name}</option>".replace("${id}", mType.id).replace("${name}", mType.name));
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
        if(((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'MJ'|| user.dept.code == 'GL')) || user.dept.code == 'CW'){
            deptDiv.style.display = 'block';

            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData(isZC())],
                onNodeSelected: function (event, data) {
                    $("#deptModal").modal('hide');

                    renderDeptAndUser(data.id, data.text);//设置部门和员工

                    searchForm.loadSupplier({currentDeptId:data.id});
                    searchForm.search();
                }
            });
            $("#cleanDept").click(function () {
                $("#currentUserId").empty();//初始化
                $("#currentUserId").append('<option value="">全部</option>');
                $("#currentDeptId").val("");//部门初始化
                $("#chooseDeptName").val("");//部门初始化
                layui.use('form', function(){
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
        if (!user.currentDeptQx) {
            ele.append("<option value=" + user.id + ">" + user.name + "</option>");
        } else {
            ele.empty();
            if(mediaUserId){
                ele.append("<option value=" + mediaUserId + ">" + mediaUserName + "</option>");
                ele.val(mediaUserId);
            }else{
                ele.append('<option value="">全部</option>');
                if(roleType){
                    searchForm.loadDeptUser(deptId,roleType,"currentUserId",searchForm.worker);
                }
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
    //根据媒介或媒介部门查询供应商列表
    loadSupplier: function(param){
        $.ajax(
            {
                url: baseUrl+"/mediaUsereStatistics/listSupplierByMediaUser",
                type: "post",
                data: param,
                dataType:"json",
                async: true,
                success:function(result){
                    var ele = $("#supplierId");
                    ele.empty();
                    ele.append('<option value="">全部</option>');
                    if(result.data.list){
                        for(var i = 0;i < result.data.list.length; i++){
                            var supplier = result.data.list[i];
                            ele.append("<option value="+supplier.id+">"+supplier.contactor+"</option>");
                        }
                    }
                    layui.use('form', function(){
                        var form = layui.form;
                        form.render('select');
                    });
                }
            }
        );
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

        /*if (searchFormData.timeQuantum == 1 || searchFormData.timeQuantum == 2) {
            $("#statisticsResultTable").hide();
            $("#statisticsResultPie").hide();
            $("#statisticsResultChart").attr("class", "col-sm-12");

            $("#supplierResultTable").hide();
            $("#supplierResultPie").hide();
            $("#supplierResultChart").attr("class", "col-sm-12");
        } else {
            $("#statisticsResultTable").show();
            $("#statisticsResultPie").show();
            $("#statisticsResultChart").attr("class", "col-sm-6");

            $("#supplierResultTable").show();
            $("#supplierResultPie").show();
            $("#supplierResultChart").attr("class", "col-sm-6");
        }*/
        //加载媒介统计结果
        $.ajax({
            url: baseUrl + "/mediaUsereStatistics/mediaUserResult",
            type: "post",
            data: searchFormData,
            dataType: "json",
            async: true,
            success: function (resData) {
                //statisticsResult.reflush([{month:"5月份",saleAmount:50,noIncomeAmount:30,yqIncomeAmount:20,dqysIncomeAmount:80}]);
                // statisticsResult.reflush(resData);
                $(".val-content").each(function (i, ele) {
                    var val = resData.data.result.mediaUserStatistics[ele.id] || 0,id_ = ele.id;
                    if(id_ === 'articleNum'){
                        $(ele).text(fmtMoneyBringUnit(val,"件"));
                    }else if(id_ === 'supplierNum'){
                        $(ele).text(fmtMoneyBringUnit(val,"个"));
                    }else{
                        $(ele).text(fmtMoneyBringUnit(val));
                    }
                    if(id_ === 'profit'){
                        var rate = "0.00%";
                        if((resData.data.result.mediaUserStatistics.saleAmount || 0) != 0){
                            rate = (val / resData.data.result.mediaUserStatistics.saleAmount * 100).toFixed(2)+"%";
                        }
                        $("#"+id_+"Rate").text(rate);
                    }
                });
                mediaUserTrend.reflushTrend(resData.data.result.trend);
                mediaUserTrend.reflush(resData.data.result.mediaTypeRate,resData.data.result.artTypeRate);
            }
        });

        //加载媒体列表
        tableListObj.init();

        //加载供应商统计结果
        /*$.ajax({
            url: baseUrl + "/mediaUsereStatistics/supplierResult",
            type: "post",
            data: searchFormData,
            dataType: "json",
            success: function (resData) {
                //supplierResult.reflush([{month:"5月份",artCount:50,payAmount:30,outgoAmount:20,qkAmount:80}]);
                supplierResult.reflush(resData);
            }
        });*/
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
function getTreeData(isZC) {
    var deptTreeData = {};
    var deptId = user.dept.id;//当前用户部门ID
    var deptCode = user.dept.code;//当前部门编码
    var deptName = user.dept.name;//当前部门名称
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    if(deptCompanyCode == "JT" && (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL")){
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
    }else if(deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL"){
        requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST",function (result) {
            var company = result.data.company;
            if (company){
                deptId = company.id;//整个集团的业务和媒介部
                deptName = company.name;
            }
        });
    }

    //非业务人员默认展示公司
    renderDeptAndUser(deptId,deptName);//设置部门和员工

    //具体查询
    requestData({deptId: deptId,deptCode:'MJ'},"/dept/listAllDeptByIdAndCode","POST",function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

//设置部门和员工
function renderDeptAndUser(deptId,deptName) {
    $("#currentDeptId").val(deptId);
    $("#chooseDeptName").val(deptName);
    searchForm.loadWorker(deptId,"MJ"); //查询媒介员工
    layui.use('form', function(){
        layui.form.render('select');//layui重新渲染下拉列表
    });
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
    pie1: {},
    pie2: {},
    pie3: {},
    pie4: {},
    trend: {},
    trendOption:{
        "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E","#FF69B4","#EE7383"],
        "backgroundColor": "#fff",
        "legend": {"data": ["稿件数量", "登记供应商数量", "合作金额", "请款金额","已支付金额", "未支付金额","利润"]},
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
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            },
            {
                "name": "登记供应商数量",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            },
            {
                "name": "合作金额",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            },
            {
                "name": "请款金额",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            },
            {
                "name": "已支付金额",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            },
            {
                "name": "未支付金额",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            },
            {
                "name": "利润",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            }
        ]
    }, //趋势图表配置
    option1: {
        "title": {
            text:"板块占比",
            subtext:"合作金额",
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
            text:'板块占比【合作金额】暂无数据',
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
    option3: {
        "title": {
            text:"稿件行业类型占比",
            subtext:"合作金额",
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
            text:'稿件行业类型占比【合作金额】暂无数据',
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
                "name": "稿件行业类型占比",
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
    option4: {
        "title": {
            text:"稿件行业类型占比",
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
            text:'稿件行业类型占比【利润】暂无数据',
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
        mediaUserTrend.pie1 = echarts.init(document.getElementById('statisticsPie1'));
        mediaUserTrend.pie2 = echarts.init(document.getElementById('statisticsPie2'));
        mediaUserTrend.pie3 = echarts.init(document.getElementById('statisticsPie3'));
        mediaUserTrend.pie4 = echarts.init(document.getElementById('statisticsPie4'));
        //点击事件
        mediaUserTrend.pie1.on('click', function (params) {
            tableListObj.toggleModal(params.data.mediaTypeId,params.data.name,"mediaType");
        });
        mediaUserTrend.pie2.on('click', function (params) {
            tableListObj.toggleModal(params.data.mediaTypeId,params.data.name,"mediaType");
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
            mediaUserTrend.initChart();
            mediaUserTrend.option1.legend.itemHeight = mediaUserTrend.calHeight(mediaUserTrend.option1, "#statisticsPie1");
            mediaUserTrend.option2.legend.itemHeight = mediaUserTrend.calHeight(mediaUserTrend.option2, "#statisticsPie2");
            mediaUserTrend.option3.legend.itemHeight = mediaUserTrend.calHeight(mediaUserTrend.option3, "#statisticsPie3");
            mediaUserTrend.option4.legend.itemHeight = mediaUserTrend.calHeight(mediaUserTrend.option4, "#statisticsPie4");
            mediaUserTrend.pie1.setOption(mediaUserTrend.option1);
            mediaUserTrend.pie2.setOption(mediaUserTrend.option2);
            mediaUserTrend.pie3.setOption(mediaUserTrend.option3);
            mediaUserTrend.pie4.setOption(mediaUserTrend.option4);
        },0);
    },
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            mediaUserTrend.trend = echarts.init(document.getElementById('statisticsTrend'));
            mediaUserTrend.trend.setOption(mediaUserTrend.trendOption);
            window.onresize = function () {
                mediaUserTrend.trend.resize();
            }
        },0);
    },
    reflushPie1: function (dataMap) {
        mediaUserTrend.option1.legend.data = [];//清空原数据
        mediaUserTrend.option1.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            mediaUserTrend.option1.legend.data[i] = dataMap[i].mediaTypeName;
            var serie1 = {name:dataMap[i].mediaTypeName,value:dataMap[i].outgoAmount,mediaTypeId:dataMap[i].mediaTypeId};//数据
            mediaUserTrend.option1.series[0].data[i] = serie1;
        }
    },
    reflushPie2: function (dataMap) {
        mediaUserTrend.option2.legend.data = [];//清空原数据
        mediaUserTrend.option2.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            mediaUserTrend.option2.legend.data[i] = dataMap[i].mediaTypeName;
            var serie2 = {name:dataMap[i].mediaTypeName,value:dataMap[i].profit,mediaTypeId:dataMap[i].mediaTypeId};//数据
            mediaUserTrend.option2.series[0].data[i] = serie2;
        }
    },
    reflushPie3: function (dataMap) {
        mediaUserTrend.option3.legend.data = [];//清空原数据
        mediaUserTrend.option3.series[0].data = [];//清空原数据
        //清空原来的数据
        var index3 = 0;
        for (var i = 0; i < dataMap.length; i++) {
            if(dataMap[i].typeName != undefined){
                mediaUserTrend.option3.legend.data[index3] = dataMap[i].typeName;
                var serie1 = {name:dataMap[i].typeName,value:dataMap[i].outgoAmount};//数据
                mediaUserTrend.option3.series[0].data[index3] = serie1;
                index3++;
            }
        }
    },
    reflushPie4: function (dataMap) {
        mediaUserTrend.option4.legend.data = [];//清空原数据
        mediaUserTrend.option4.series[0].data = [];//清空原数据
        //清空原来的数据
        var index4 = 0;
        for (var i = 0; i < dataMap.length; i++) {
            if(dataMap[i].typeName != undefined){
                mediaUserTrend.option4.legend.data[index4] = dataMap[i].typeName;
                var serie2 = {name:dataMap[i].typeName,value:dataMap[i].profit};//数据
                mediaUserTrend.option4.series[0].data[index4] = serie2;
                index4++;
            }
        }
    },
    reflushTrend: function (dataList){
        //清空原来的数据
        mediaUserTrend.trendOption.xAxis[0].data = [];
        mediaUserTrend.trendOption.series[0].data = [];
        mediaUserTrend.trendOption.series[1].data = [];
        mediaUserTrend.trendOption.series[2].data = [];
        mediaUserTrend.trendOption.series[3].data = [];
        mediaUserTrend.trendOption.series[4].data = [];
        mediaUserTrend.trendOption.series[5].data = [];
        mediaUserTrend.trendOption.series[6].data = [];
        //数据封装
        var timeUnit = $("#timeQuantum").val() == 1 ? "日" : "月";
        for (var i = 0; i < dataList.length; i++) {
            mediaUserTrend.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
            mediaUserTrend.trendOption.series[0].data[i] = fmtMoneyBringUnit(dataList[i].articleNum,true) || 0;
            mediaUserTrend.trendOption.series[1].data[i] = fmtMoneyBringUnit(dataList[i].supplierNum,true) || 0;
            mediaUserTrend.trendOption.series[2].data[i] = dataList[i].outgoAmount || 0;
            mediaUserTrend.trendOption.series[3].data[i] = dataList[i].applyAmount || 0;
            mediaUserTrend.trendOption.series[4].data[i] = dataList[i].outgoAmount || 0;
            mediaUserTrend.trendOption.series[5].data[i] = dataList[i].paid || 0;
            mediaUserTrend.trendOption.series[6].data[i] = dataList[i].profit || 0;
        }
        //加载图表
        mediaUserTrend.loadTrend();
    },
    reflush: function (mediaTypeList, artTypeList) {
        var params = $("#searchForm").serializeJson();
        mediaUserTrend.reflushPie1(mediaTypeList);
        mediaUserTrend.reflushPie2(mediaTypeList);
        mediaUserTrend.reflushPie3(artTypeList);
        mediaUserTrend.reflushPie4(artTypeList);
        mediaUserTrend.load();
    }
};
/*var statisticsResult = {
    //左边的柱状图
    chart: {},
    //右边的饼图
    pie: {},
    //柱状图的option
    option: {
        "title": {"text": "媒介统计结果", "subtext": "", "x": "left"},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["登记供应商数量", "合作金额总额", "未支付金额", "利润"]},
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
                name: '(单位/元)'
            }
        ],
        "series": [{"name": "登记供应商数量", "type": "bar", "data": []},
            {"name": "合作金额总额", "type": "bar", "data": []},
            {"name": "未支付金额", "type": "bar", "data": []},
            {"name": "利润", "type": "bar", "data": []}
        ]
    },
    //饼图的option
    pieOption: {},
    init: function () {
        statisticsResult.chart = echarts.init(document.getElementById('statisticsResultChart'));
        statisticsResult.pie = echarts.init(document.getElementById('statisticsResultPie'));
    },
    load: function () {
        statisticsResult.chart.setOption(statisticsResult.option, true);
    },
    loadPie: function (opt) {
        $.extend(true, statisticsResult.pieOption, statisticsResult.option);
        var val1 = opt.series[0].data[0] || 0;
        var val2 = opt.series[1].data[0] || 0;
        var val3 = opt.series[2].data[0] || 0;
        var val4 = opt.series[3].data[0] || 0;
        statisticsResult.pieOption.title = {};
        statisticsResult.pieOption.xAxis = null;
        statisticsResult.pieOption.yAxis = null;
        statisticsResult.pieOption.series = [
            {
                name: '媒介统计结果',
                type: 'pie',
                radius: '55%',
                center: ['50%', '60%'],
                data: [
                    {value: val1, name: '登记供应商数量'},
                    {value: val2, name: '合作金额总额'},
                    {value: val3, name: '未支付金额'},
                    {value: val4, name: '利润'}
                ],
                itemStyle: {
                    emphasis: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ];
        statisticsResult.pie.setOption(statisticsResult.pieOption);
    },
    loadTable: function (opt) {
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
    reflush: function (dataList) {
        var searchFormData = $("#searchForm").serializeJson();
        var dw = "月";
        if (searchFormData.timeQuantum == 4) {
            dw = "日";
        } else if (searchFormData.timeQuantum == 5) {
            dw = "";
        }

        statisticsResult.init();

        var len = statisticsResult.option.series[0].data.length;
        statisticsResult.option.series[0].data = [];
        statisticsResult.option.xAxis[0].data = [];
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            statisticsResult.option.xAxis[0].data[i] = "";
            statisticsResult.option.series[0].data[i] = 0;
            statisticsResult.option.series[1].data[i] = 0;
            statisticsResult.option.series[2].data[i] = 0;
            statisticsResult.option.series[3].data[i] = 0;
        }

        for (var i = 0; i < dataList.length; i++) {
            statisticsResult.option.xAxis[0].data[i] = dataList[i].month ? dataList[i].month + dw : "";
            statisticsResult.option.series[0].data[i] = dataList[i].djgyssl || 0;
            statisticsResult.option.series[1].data[i] = dataList[i].hzjeze || 0;
            statisticsResult.option.series[2].data[i] = dataList[i].wzfje || 0;
            statisticsResult.option.series[3].data[i] = dataList[i].profitAmount || 0;
        }

        //加载客户统计结果
        statisticsResult.load();

        if (searchFormData.timeQuantum == 1 || searchFormData.timeQuantum == 2) {

        } else {
            statisticsResult.loadPie(statisticsResult.option);
            statisticsResult.loadTable(statisticsResult.option);
        }
    }
};*/

//媒体列表
var tableListObj = {
    grid:{},
    //媒体排名列表
    mediaTable: {
        url: baseUrl + '/mediaUsereStatistics/listMediaUserMediaStatisticsByParam',
        postData: $("#searchForm").serializeJson(),
        datatype: "json",
        mtype: 'post',
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        sortable: true,
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 50, //每页记录数
        rowList: [10,20, 50,100],//每页记录数可选列表
        colNames: ['媒体ID','媒体名称','媒体板块ID','板块名称', '稿件数量', '供应商数量', '报价金额', '合作金额','请款金额','已支付金额','未支付金额','利润'],//表头
        jsonReader: {//server返回Json解析设定
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'mediaId',
                index: 'mediaId',
                editable: false,
                align: "center",
                sortable: true,
                search: true,
                hidden: true
            },
            {
                name: 'mediaName',
                index: 'mediaName',
                editable: false,
                align: "center",
                width:150,
                sortable: true,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return tableListObj.getSingleLinkHtml(rows.mediaId,rows.mediaName,"media");
                }
            },
            {
                name: 'mediaTypeId',
                index: 'mediaTypeId',
                editable: false,
                align: "center",
                sortable: true,
                search: true,
                hidden: true
            },
            {
                name: 'mediaTypeName',
                index: 'mediaTypeName',
                editable: false,
                align: "center",
                width:120,
                sortable: true,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return tableListObj.getSingleLinkHtml(rows.mediaTypeId,rows.mediaTypeName,"mediaType");
                }
            },
            {
                name: 'articleNum',
                index: 'articleNum',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            },
            {
                name: 'supplierNum',
                index: 'supplierNum',
                editable: false,
                width:150,
                align: "center",
                sortable: true
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                editable: false,
                width:100,
                align: "center",
                sortable: true
            },
            {
                name: 'outgoAmount',
                index: 'outgoAmount',
                editable: false,
                width:100,
                align: "center",
                sortable: true
            },
            {
                name: 'applyAmount',
                index: 'applyAmount',
                editable: false,
                width:100,
                align: "center",
                sortable: true
            },
            {
                name: 'paid',
                index: 'paid',
                editable: false,
                width:120,
                align: "center",
                sortable: true
            },
            {
                name: 'unpaid',
                index: 'unpaid',
                editable: false,
                width:120,
                align: "center",
                sortable: true
            },
            {
                name: 'profit',
                index: 'profit',
                editable: false,
                width:100,
                align: "center",
                sortable: true
            }
        ],
        pager: "#mediaPager",
        viewrecords: true,
        caption: "媒体列表排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    init:function () {
        //媒体列表
        tableListObj.grid.mediaRanking = new dataGrid("mediaTable", tableListObj.mediaTable, "mediaPager", "searchForm");
        tableListObj.grid.mediaRanking.loadGrid();
        tableListObj.grid.mediaRanking.setNavGrid();
        tableListObj.grid.mediaRanking.defaultParams = {};//清空历史表单数据
        tableListObj.grid.mediaRanking.search();
    },
    getSingleLinkHtml:function (id,value,type) { //获取单个a链接
        var html = "";
        if(id){
            value = value ? value : id;//如果value为空则展示ID
            html += "<a onclick=\"tableListObj.toggleModal("+id+",'"+value+"','"+type+"');\">"+value+"</a>";
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
}

//供应商统计结果
/*var supplierResult = {
    //左边的柱状图
    chart: {},
    //右边的饼图
    pie: {},
    //柱状图的option
    option: {
        "title": {"text": "供应商统计结果", "subtext": "", "x": "left"},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["稿件总数（已发布）", "应付金额", "已付金额", "请款金额", "利润"]},
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
                name: '(单位/元)'
            }
        ],
        "series": [{"name": "稿件总数（已发布）", "type": "bar", "data": []},
            {"name": "应付金额", "type": "bar", "data": []},
            {"name": "已付金额", "type": "bar", "data": []},
            {"name": "请款金额", "type": "bar", "data": []},
            {"name": "利润", "type": "bar", "data": []}
        ]
    },
    //饼图的option
    pieOption: {},
    init: function () {
        supplierResult.chart = echarts.init(document.getElementById('supplierResultChart'));
        supplierResult.pie = echarts.init(document.getElementById('supplierResultPie'));
    },
    load: function () {
        supplierResult.chart.setOption(supplierResult.option, true);
    },
    loadPie: function (opt) {
        $.extend(true, supplierResult.pieOption, supplierResult.option);
        var val1 = opt.series[0].data[0] || 0;
        var val2 = opt.series[1].data[0] || 0;
        var val3 = opt.series[2].data[0] || 0;
        var val4 = opt.series[3].data[0] || 0;
        var val5 = opt.series[4].data[0] || 0;
        supplierResult.pieOption.title = {};
        supplierResult.pieOption.xAxis = null;
        supplierResult.pieOption.yAxis = null;
        supplierResult.pieOption.series = [
            {
                name: '媒介统计结果',
                type: 'pie',
                radius: '55%',
                center: ['50%', '60%'],
                data: [
                    {value: val1, name: '稿件总数（已发布）'},
                    {value: val2, name: '应付金额'},
                    {value: val3, name: '已付金额'},
                    {value: val4, name: '请款金额'},
                    {value: val5, name: '利润'}
                ],
                itemStyle: {
                    emphasis: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0, 0)'
                    }
                }
            }
        ];
        supplierResult.pie.setOption(supplierResult.pieOption);
    },
    loadTable: function (opt) {
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
        $("#supplierResultTable").html(table);
    },
    reflush: function (dataList) {
        var searchFormData = $("#searchForm").serializeJson();
        var dw = "月";
        if (searchFormData.timeQuantum == 4) {
            dw = "日";
        } else if (searchFormData.timeQuantum == 5) {
            dw = "";
        }

        supplierResult.init();

        var len = supplierResult.option.series[0].data.length;
        supplierResult.option.series[0].data = [];
        supplierResult.option.xAxis[0].data = [];
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            supplierResult.option.xAxis[0].data[i] = "";
            supplierResult.option.series[0].data[i] = 0;
            supplierResult.option.series[1].data[i] = 0;
            supplierResult.option.series[2].data[i] = 0;
            supplierResult.option.series[3].data[i] = 0;
            supplierResult.option.series[4].data[i] = 0;
        }

        for (var i = 0; i < dataList.length; i++) {
            supplierResult.option.xAxis[0].data[i] = dataList[i].month ? dataList[i].month + dw : "";
            supplierResult.option.series[0].data[i] = dataList[i].artCount || 0;
            supplierResult.option.series[1].data[i] = dataList[i].payAmount || 0;
            supplierResult.option.series[2].data[i] = dataList[i].outgoAmount || 0;
            supplierResult.option.series[3].data[i] = dataList[i].qkAmount || 0;
            supplierResult.option.series[4].data[i] = dataList[i].profitAmount || 0;
        }

        //加载客户统计结果
        supplierResult.load();

        if (searchFormData.timeQuantum == 1 || searchFormData.timeQuantum == 2) {

        } else {
            supplierResult.loadPie(supplierResult.option);
            supplierResult.loadTable(supplierResult.option);
        }
    }
};*/
