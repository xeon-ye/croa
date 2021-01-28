/*var Supplier = {
    //设置统计数据
    setArticleResult: function(){
        $.ajax({
            url: baseUrl+"/mediaUsereManagerStatistics/supplierListSort",
            data: $("#searchForm").serializeJson(),
            type: "post",
            dataType:"json",
            success: function(resData){
                resData = resData.list;
                if(resData && resData.length > 0){
                    for(var o in resData[0]){
                        $("#"+o).text(resData[0][o]);
                    }
                }else{
                    $("#tj").find(".text-danger").html(0);
                }
            }
        });
    },
    //导出全部
    exportArt: function () {
        var params = $("#searchForm").serializeJson()
        location.href="/mediaUsereManagerStatistics/exportAll?"+$.param(params);
    }
};*/
var mediaUserPlateMap = [];
//查询区域
/*var searchForm = {
    //显示或隐藏发布日期
    showIssuedDate: function (val) {
        if (val == 5) {
            $("#issuedDateFormGroup").show();
        } else {
            $("#issuedDateFormGroup").hide();
        }
    },
    //查询
    search: function () {
       grid.search();
       Supplier.setArticleResult();
    }
};*/

//查询区域
var searchForm = {
    init: function () {
        var supplierId = getQueryString("supplierId");//获取统计概况页跳转的请求参数
        var supplierName = getQueryString("supplierName");//获取统计概况页跳转的请求参数
        searchForm.loadMediaType();
        searchForm.loadDept();
        searchForm.loadWorker();
        searchForm.loadSupplier(null,supplierId,supplierName);
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
                    layui.form.render('select');//layui重新渲染下拉列表
                });
                searchForm.search();//重新加载
            });
        }
    },
    //加载媒介
    loadWorker: function (deptId,roleType) {
        var ele = $("#currentUserId");
        //如果没有部门权限，则只加载当前用户
        if (!user.currentDeptQx) {
            ele.append("<option value=" + user.id + ">" + user.name + "</option>");
        } else {
            ele.empty();
            ele.append('<option value="">全部</option>');
            if(roleType){
                searchForm.loadDeptUser(deptId,roleType,"currentUserId",searchForm.worker);
            }
        }
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
                        layui.use('form', function(){
                            var form = layui.form;
                            form.render('select');
                        });
                    }
                }
            );
        });
    },
    //根据媒介或媒介部门查询供应商列表
    loadSupplier: function(param,supplierId,supplierName){
        $.ajax(
            {
                url: baseUrl+"/mediaUsereStatistics/listSupplierByMediaUser",
                type: "post",
                data: param,
                dataType:"json",
                async:true,
                success:function(result){
                    var ele = $("#supplierId");
                    ele.empty();
                    ele.append('<option value="">全部</option>');
                    if(result.data.list && result.data.list.length > 0){
                        for(var i = 0;i < result.data.list.length; i++){
                            var supplier = result.data.list[i];
                            var phone = supplier.phone;
                            //供应商电话不允许显示
                            if(phone != null){
                                if (phone.length >= 11) {
                                    var start = phone.length > 11 ? "*****" : "****";
                                    phone = phone.substring(0, 3) + start + phone.substring(phone.length - 4, phone.length);
                                } else if (phone.length >= 3) {
                                    phone = phone[0] + "***" + phone[value.length - 1];
                                } else {
                                    phone = "**";
                                }
                            }
                            if (new Date(supplier.createTime) >= new Date("2020-08-30")){
                                ele.append("<option value="+supplier.id+">"+supplier.contactor+'-'+supplier.name+'-'+phone+"</option>");
                            }else {
                                ele.append("<option value="+supplier.id+">"+supplier.contactor+'-'+supplier.name+'-'+supplier.plateName+"</option>");
                            }
                            if(supplierId == supplier.id){
                                ele.val(supplier.id);
                            }
                        }
                    }else{
                        if(supplierId){
                            ele.append("<option value="+supplierId+">"+supplierName+"</option>");
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

        //加载供应商统计结果
        $.ajax({
            url: baseUrl + "/mediaUsereManagerStatistics/supplierStatisticsResult",
            type: "post",
            data: searchFormData,
            dataType: "json",
            async: true,
            success: function (resData) {
                $(".val-content").each(function (i, ele) {
                    var val = resData.data.result.supplierStatistics[ele.id] || 0,id_ = ele.id;
                    if(id_ === 'articleNum' || id_ === 'supplierNum'){
                        $(ele).text(fmtMoneyBringUnit(val,"件"));
                    }else{
                        $(ele).text(fmtMoneyBringUnit(val));
                    }
                    if(id_ === 'profit'){
                        var rate = "0.00%";
                        if((resData.data.result.supplierStatistics.saleAmount || 0) != 0){
                            rate = (val / resData.data.result.supplierStatistics.saleAmount * 100).toFixed(2)+"%";
                        }
                        $("#"+id_+"Rate").text(rate);
                    }
                });
                supplierTrend.reflushTrend(resData.data.result.trend);
                supplierTrend.reflush(resData.data.result.mediaTypeRate);
            }
        });

        //加载供应商列表
        tableListObj.init();

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

    renderDeptAndUser(deptId, deptName);

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
var supplierTrend = {
    pie1: {},
    pie2: {},
    trend: {},
    trendOption:{
        "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E","#FF69B4"],
        "backgroundColor": "#fff",
        "legend": {"data": ["稿件数量", "应付总金额", "已付总金额", "未付总金额","请款总金额","利润"]},
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
                "name": "应付总金额",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            },
            {
                "name": "已付总金额",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            },
            {
                "name": "未付总金额",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                // stack:"总量",
                "data": []
            },
            {
                "name": "请款总金额",
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
            subtext:"应付金额",
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
            text:'板块占比【应付金额】暂无数据',
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
        supplierTrend.pie1 = echarts.init(document.getElementById('statisticsPie1'));
        supplierTrend.pie2 = echarts.init(document.getElementById('statisticsPie2'));
        //点击事件
        supplierTrend.pie1.on('click', function (params) {
            tableListObj.toggleModal(params.data.mediaTypeId,params.data.name,"mediaType");
        });
        supplierTrend.pie2.on('click', function (params) {
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
            supplierTrend.initChart();
            supplierTrend.option1.legend.itemHeight = supplierTrend.calHeight(supplierTrend.option1, "#statisticsPie1");
            supplierTrend.option2.legend.itemHeight = supplierTrend.calHeight(supplierTrend.option2, "#statisticsPie2");
            supplierTrend.pie1.setOption(supplierTrend.option1);
            supplierTrend.pie2.setOption(supplierTrend.option2);
        },0);
    },
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            supplierTrend.trend = echarts.init(document.getElementById('statisticsTrend'));
            supplierTrend.trend.setOption(supplierTrend.trendOption);
            window.onresize = function () {
                supplierTrend.trend.resize();
            }
        },0);
    },
    reflushPie1: function (dataMap) {
        supplierTrend.option1.legend.data = [];//清空原数据
        supplierTrend.option1.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            supplierTrend.option1.legend.data[i] = dataMap[i].mediaTypeName;
            var serie1 = {name:dataMap[i].mediaTypeName,value:dataMap[i].outgoAmount,mediaTypeId:dataMap[i].mediaTypeId};//数据
            supplierTrend.option1.series[0].data[i] = serie1;
        }
    },
    reflushPie2: function (dataMap) {
        supplierTrend.option2.legend.data = [];//清空原数据
        supplierTrend.option2.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            supplierTrend.option2.legend.data[i] = dataMap[i].mediaTypeName;
            var serie2 = {name:dataMap[i].mediaTypeName,value:dataMap[i].profit,mediaTypeId:dataMap[i].mediaTypeId};//数据
            supplierTrend.option2.series[0].data[i] = serie2;
        }
    },
    reflushTrend: function (dataList){
        //清空原来的数据
        supplierTrend.trendOption.xAxis[0].data = [];
        supplierTrend.trendOption.series[0].data = [];
        supplierTrend.trendOption.series[1].data = [];
        supplierTrend.trendOption.series[2].data = [];
        supplierTrend.trendOption.series[3].data = [];
        supplierTrend.trendOption.series[4].data = [];
        supplierTrend.trendOption.series[5].data = [];
        //数据封装
        var timeUnit = $("#timeQuantum").val() == 1 ? "日" : "月";
        for (var i = 0; i < dataList.length; i++) {
            supplierTrend.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
            supplierTrend.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
            supplierTrend.trendOption.series[1].data[i] = dataList[i].outgoAmount || 0;
            supplierTrend.trendOption.series[2].data[i] = dataList[i].paid || 0;
            supplierTrend.trendOption.series[3].data[i] = dataList[i].unpaid || 0;
            supplierTrend.trendOption.series[4].data[i] = dataList[i].applyAmount || 0;
            supplierTrend.trendOption.series[5].data[i] = dataList[i].profit || 0;
        }
        //加载图表
        supplierTrend.loadTrend();
    },
    reflush: function (mediaTypeList) {
        var params = $("#searchForm").serializeJson();
        supplierTrend.reflushPie1(mediaTypeList);
        supplierTrend.reflushPie2(mediaTypeList);
        supplierTrend.load();
    }
};

var tableListObj = {
    grid:{},
    //供应商排名列表
    supplierTable: {
        url: baseUrl + '/mediaUsereManagerStatistics/listSupplierStatisticsByParam',
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
        colNames: ['稿件类别ID','板块名称', '供应商', '供应商联系人ID','供应商联系人','联系人电话', '稿件总数','应付金额', '已付金额','未付金额','请款金额','利润'],//表头
        jsonReader: {//server返回Json解析设定
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'mediaTypeId',
                index: 'mediaTypeId',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                hidden:true
            },
            {
                name: 'mediaTypeName',
                index: 'mediaTypeName',
                editable: false,
                width:120,
                align: "center",
                sortable: true,
                formatter: function (value, grid, rows) {
                    return tableListObj.getSingleLinkHtml(rows.mediaTypeId,rows.mediaTypeName,"mediaType");
                }
            },
            {
                name: 'supplierName',
                index: 'supplierName',
                editable: false,
                align: "center",
                width:150,
                sortable: true,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    var userCompanyCode = user.dept.companyCode; //获取当前用户公司代码
                    if(rows.supplierCompanyCode == userCompanyCode){
                        return value || "";
                    }else{
                        if(value){
                            value =  value.substr(0,1)+"****";
                        }
                        return value || "";
                    }
                }
            },
            {
                name: 'supplierId',
                index: 'supplierId',
                editable: false,
                align: "center",
                sortable: true,
                search: true,
                hidden: true
            },
            {
                name: 'supplierContactor',
                index: 'supplierContactor',
                editable: false,
                align: "center",
                width:120,
                sortable: true,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    var userCompanyCode = user.dept.companyCode; //获取当前用户公司代码
                    if(rows.supplierCompanyCode == userCompanyCode){
                        return tableListObj.getSingleLinkHtml(rows.supplierId,rows.supplierContactor,"supplier");
                    }else{
                        if(value){
                            value =  value.substr(0,1)+"****";
                        }
                        return value || "";
                    }
                }
            },
            {
                name:'phone',
                index:'phone',
                editable: false,
                align: "center",
                width:60,
                sortable: true,
                formatter:function (value, grid, rows) {
                    return supplierPhone(value, grid, rows);
                }
            },
            {
                name: 'articleNum',
                index: 'articleNum',
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
                name: 'paid',
                index: 'paid',
                editable: false,
                width:100,
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
                name: 'applyAmount',
                index: 'applyAmount',
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
        pager: "#supplierPager",
        viewrecords: true,
        caption: "供应商排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    init:function () {
        //媒体列表
        tableListObj.grid.supplierRanking = new dataGrid("supplierTable", tableListObj.supplierTable, "supplierPager", "searchForm");
        tableListObj.grid.supplierRanking.loadGrid();
        tableListObj.grid.supplierRanking.setNavGrid();
        tableListObj.grid.supplierRanking.defaultParams = {};//清空历史表单数据
        tableListObj.grid.supplierRanking.search();
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

/*var gridObject = {
    url: baseUrl+'/mediaUsereManagerStatistics/supplierListSort',
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
    rowNum: 10,
    rowList: [10, 20, 30,100,1000],
    colNames: ['稿件类别', '供应商', '供应商联系人','稿件总数','应付金额', '已付金额','请款金额','利润',"操作"],
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: false
    },
    colModel: [
        {
            name: 'artType',
            index: 'artType',
            editable: false,
            width: 170,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'supName',
            index: 'supName',
            editable: false,
            width: 300,
            align: "center",
            sortable: false
        },
        {
            name: 'contactor',
            index: 'contactor',
            editable: false,
            width: 250,
            align: "center",
            sortable: false,
            sorttype: "string"
        },
        {
            name: 'artCount',
            index: 'artCount',
            editable: false,
            width: 100,
            align: "center",
            sortable: true,
            sorttype: "string"
        },
        {
            name: 'payAmount',
            index: 'payAmount',
            editable: false,
           width:200,
            align: "center",
            sortable: true,
            sorttype: "string",
            hidden: false,
            classes:'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator:",",decimalSeparator:".", prefix:"￥"}
        },
        {
            name: 'incomeAmount',
            index: 'incomeAmount',
            editable: false,
            width: 200,
            align: "center",
            sortable: true,
            classes:'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator:",",decimalSeparator:".", prefix:"￥"}
        },
        {
            name: 'applyAmount',
            index: 'applyAmount',
            editable: false,
            width: 200,
            align: "center",
            sortable: true,
            classes:'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator:",",decimalSeparator:".", prefix:"￥"}
        },
        {
            name: 'profitAmount',
            index: 'profitAmount',
            editable: false,
            width: 200,
            align: "center",
            sortable: true,
            classes:'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator:",",decimalSeparator:".", prefix:"￥"}
        },
        {
            name: 'option',
            editable: false,
            width: 200,
            align: "center",
            sortable: false,
            formatter:function (a,b,rowdata) {
                return "";
            }
        }
    ],
    /!**
     * 翻页时保存当前页面的选中数据
     * @param pageBtn
     *!/
    onPaging:function(pageBtn){

    },
    gridComplete: function () {

    },
    loadComplete: function (xhr) {
        var array = xhr.list;
        var sum = 0;
        var sum1=0;
        var sum2=0;
        var sum3=0;
        var sum4=0;

        $.each(array, function (i, item) {
            if(item.payAmount!=null) {
                sum += parseFloat(item.payAmount);
            }
            if(item.incomeAmount!=null){
               sum1 += parseFloat(item.incomeAmount);
            }
            if(item.artCount!=null){
               sum2 += parseFloat(item.artCount);
            }
            if(item.applyAmount!=null){
                sum3 += parseFloat(item.applyAmount);
            }
            if(item.profitAmount!=null){
                sum4 += parseFloat(item.profitAmount);
            }
        });
        $("#payAmountSum").html(sum.toFixed(2));
        $("#incomeAmountSum").html(sum1.toFixed(2));
        $("#artCountSum").html(sum2);
        var noPay = (sum.toFixed(2)-sum1.toFixed(2))+"";
        if(noPay.indexOf(".")==-1){
           noPay = noPay+".00"
        }
        $("#noPayAmountSum").html(noPay);
        $("#applyAmountSum").html(sum3.toFixed(2));
        $("#profitAmount").html(sum4.toFixed(2));
    },
    pager: "#pager",
    viewrecords: true,
    caption: "供应商排名",
    add: false,
    edit: false,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false,
    shrinkToFit:false,
    autoScroll: true
};*/

$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    mediaUserPlateMap=userMedaiPlateList();
});
/**
 * 加载用户所拥有的媒体板块
 */
function userMedaiPlateList() {
    var mediaUserPlateMap=[];
    $.ajax({
        url: baseUrl + "/mediaPlate/userId",  //mediaType/listByUserId
        data: {"userId": user.id},
        type: "post",
        dataType: "json",
        success: function (data) {
            if (data) {
                for (var i = 0; i < data.length; i++) {
                    mediaUserPlateMap.push(data[i].id)
                }
            }
        }
    });
    return mediaUserPlateMap;
}



function supplierPhone(value, grid, rows) {
    // var flag = false;
    // if (rows.hasOwnProperty('mediaTypeId')){
    //     var plateIds = rows.mediaTypeId;
    //     if (plateIds) {
    //             if (mediaUserPlateMap.contains(plateIds)) {
    //                 //当前用户的板块包含了该供应商的板块
    //                 flag = true;
    //             }
    //
    //     }
    // }

    value = rows.phone || "";
    if(value){
        // if((rows.supplierCreator == user.id)||(flag && hasRoleMJBZ()) || (flag && hasRoleMJZZ()) || (flag && hasRoleMJZJ()) || hasRoleCW() ){
        //     return value;
        // }else {
            if(value.length >= 11){
                var start = value.length > 11 ? "*****" : "****";
                return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
            }else if(value.length >= 3){
                return value[0] + "***" + value[value.length - 1];
            }else {
                return "**";
            }

    }else {
        return "";
    }
}