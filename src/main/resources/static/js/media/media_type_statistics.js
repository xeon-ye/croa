//查询区域
var searchForm = {
    init: function () {
        var mediaTypeId = getQueryString("mediaTypeId");//获取统计概况页跳转的请求参数
        var mediaTypeName = getQueryString("mediaTypeName");//获取统计概况页跳转的请求参数
        searchForm.loadMediaType(mediaTypeId,mediaTypeName);
        searchForm.loadDept();
        searchForm.loadWorker();
        //如果是业务员，则不让他看到供应商排名、媒介、媒体排名
        if(user.dept.code == 'YW'){
            $("#supplierTab").css("display","none");
            $("#supplierTableDiv").css("display","none");

            $("#mediaUserTab").css("display","none");
            $("#mediaUserTableDiv").css("display","none");

            $("#mediaTab").css("display","none");
            $("#mediaTableDiv").css("display","none");
        }else{
            $("#supplierTab").css("display","inline-block");
            $("#supplierTableDiv").css("display","inline-block");

            $("#mediaUserTab").css("display","inline-block");
            $("#mediaUserTableDiv").css("display","inline-block");

            $("#mediaTab").css("display","inline-block");
            $("#mediaTableDiv").css("display","inline-block");
        }
        //如果是媒介，则不让他看到客户排名、业务员排名
        if(user.dept.code == 'MJ'){
            $("#custTab").css("display","none");
            $("#custTableDiv").css("display","none");

            if(!isMJBZ()){ //媒介部长暂时开发可看业务员Tab
                $("#businessTab").css("display","none");
                $("#businessTableDiv").css("display","none");
            }else{
                $("#businessTab").css("display","inline-block");
                $("#businessTableDiv").css("display","inline-block");
            }
        }else{
            $("#custTab").css("display","inline-block");
            $("#custTableDiv").css("display","inline-block");

            $("#businessTab").css("display","inline-block");
            $("#businessTableDiv").css("display","inline-block");
        }

        $("#mediaTypeRowSelect").val("");//初始化板块列表行选中值，该值用于其他列表查询条件
    },
    //显示或隐藏发布日期
    showIssuedDate: function (val) {
        if (val == 3) {
            $("#rowTwo").show();
            $("#issuedDateFormGroup").show();
            extendTermObj.searchBtnPlace();
        } else {
            $("#rowTwo").hide();
            $("#issuedDateFormGroup").hide();
            extendTermObj.searchBtnPlace();
            searchForm.search();
        }
    },
    loadMediaType: function(mediaTypeId,mediaTypeName){//加载媒体类型
        $.ajax({
            url: baseUrl+"/mediaPlate/0", //mediaType?parentId=0
            dataType: "json",
            async: false,
            type: "get",
            success: function(data){
                var mTypeEle = $("[name='mediaType']");
                if(data){
                    for(var i=0;i<data.length;i++){
                        var mType = data[i];
                        mTypeEle.append("<option value='${id}'>${name}</option>".replace("${id}",mType.id).replace("${name}",mType.name));
                        if(mType.id == mediaTypeId){
                            $(mTypeEle).val(mType.id);
                        }
                    }
                }else{
                    if(mediaTypeId){
                        mTypeEle.append("<option value='${id}' selected='selected'>${name}</option>".replace("${id}",mediaTypeId).replace("${name}",mediaTypeName));
                    }
                }
            }
        });
    },
    loadDept:function(){
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        var deptDiv = document.getElementById("deptDiv");
        $("#userType").val('YW');//默认业务
        if(user.dept.code == 'YW'){
            $("#deptLabel").text("业务部门:");
            $("#userLabel").text("业务员:");
        }else if(user.dept.code == 'MJ'){
            $("#deptLabel").text("媒介部门:");
            $("#userLabel").text("媒介:");
            $("#userType").val('MJ');//当用户部门是媒介部是默认用户类型为媒介人员
        }else {
            $("#deptLabel").text("公司部门:");
            $("#userLabel").text("公司员工:");
        }
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务  媒介部门
        if((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'CW' || user.dept.code == 'YW' || user.dept.code == 'MJ' || user.dept.code == 'GL')){
            deptDiv.style.display = 'block';

            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData(isZC())],
                onNodeSelected: function (event, data) {
                    $("#deptModal").modal('hide');
                    //如果是业务/媒介部，则设置业务/媒介部ID，如果是其他（JT、GL）不设置
                    if(data.code == 'YW'){
                        $("#userType").val(data.code);
                        $("#userLabel").text("业务员:")
                        renderDeptAndUser(data.id,data.text, data.code);//设置部门和员工
                    }else if(data.code == 'MJ'){
                        $("#userType").val(data.code);
                        $("#userLabel").text("媒介:")
                        renderDeptAndUser(data.id,data.text, data.code);//设置部门和员工
                    }else{//其他情况清空
                        $("#userType").val('YW');
                        $("#userLabel").text("公司人员:")
                        renderDeptAndUser(data.id,data.text, "YW");//设置部门和员工
                    }
                    layui.use('form', function(){
                        layui.form.render('select');//layui重新渲染下拉列表
                    });
                    searchForm.search();
                }
            });
            $("#cleanDept").click(function () {
                $("#userType").val('YW');
                // $("#companyCode").val("");//清空
                $("#currentUserId").empty();//初始化
                $("#currentUserId").append('<option value="">全部</option>');
                $("#currentDeptId").val("");
                $("#deptName").val("");
                layui.use('form', function(){
                    layui.form.render('select');//layui重新渲染下拉列表
                });
                searchForm.search();//重新加载
            });
        }
    },
    //加载业务员
    //加载此部门下的业务员
    loadWorker: function(deptId,roleType){
        deptId = deptId || "";
        var ele = $("#currentUserId");
        //如果没有部门权限 和 公司权限，则只加载当前用户
        if(!user.currentDeptQx && !user.currentCompanyQx){
            ele.append("<option value="+user.id+">"+user.name+"</option>");
        }else {
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
                        layui.use('form', function(){  //刷新下拉列表
                            var form = layui.form;
                            form.render('select');
                        });
                    }
                }
            );
        });
    },
    //查询
    search: function () {
        $("#mediaTypeRowSelect").val("");//初始化板块列表行选中值，该值用于其他列表查询条件
        topBox.reflush();//加载顶部统计 和 饼图
        tableListObj.init();//加载表格
    }
};

//支持扩展条件字段
var extendTermObj = {
    mediaTermMap: {},
    changeTerm: function (plateId) {
        if(plateId){
            $("#extendTerm").css("display", "block");
        }else {
            $("#extendTerm").css("display", "none");
        }
    },
    searchBtnPlace: function (btn) {
        //设置查询按钮位置
        var btnNode = $("#searchButton");
        $("#searchButton").remove();
        if(btn){
            btnNode = btn;
        }
        //如果日期区间隐藏
        if($("#extendTerm").is(':hidden')){
            //如果日期区间隐藏
            if($("#rowTwo").is(':hidden')){
                $("#rowOne").append(btnNode);
            }else {
                $("#rowTwo").append(btnNode);
            }
        }else {
            //如果日期区间隐藏
            if($("#rowTwo").is(':hidden')){
                $("#extendTerm > div:last-child").append(btnNode);
            }else {
                $("#rowTwo").append(btnNode);
            }
        }
    },
    loadTerm: function (plateId) {
        var btn = $("#searchButton");
        extendTermObj.changeTerm(plateId);
        if(plateId){
            if(!extendTermObj.mediaTermMap[plateId]){
                requestData(null,"/mediaTerm1/" + plateId,"get",function (datas) {
                    extendTermObj.mediaTermMap[plateId] = datas; //缓存媒体查询条件
                    extendTermObj.renderTermCondition(datas);//渲染页面条件
                },false);
            }else{
                extendTermObj.renderTermCondition(extendTermObj.mediaTermMap[plateId]);//渲染页面条件
            }
        }else {
            $("#extendTerm").html("");
        }
        layui.use('form', function(){
            layui.form.render('select');//layui重新渲染下拉列表
        });
        extendTermObj.searchBtnPlace(btn); //改变查询按钮位置
    },
    renderTermCondition: function (datas) {
        if(datas && datas.length > 0){
            var html = "";
            //只获取下拉列表类型
            datas = datas.filter(function (termData) {
                return termData.type == "select";
            });
            $(datas).each(function (i, term) {
                if(term.type == 'select'){
                    var cell = "cell:" + term.cell + ":" + term.type;
                    if(i % 5 == 0){
                        html += "<div class=\"form-group\" style=\"margin-top:5px;\">";
                    }
                    html += " <label class=\"col-sm-1 control-label right0\">"+term.cellName+":</label>";
                    html += "<div class=\"col-sm-1\">";
                    html += "<select name='"+cell+"' class=\"form-control height18\" lay-search lay-filter=\"extendTerm\">";
                    if(term.dbJson){  //如果dbJson字段有值则使用，否为dbsql有值
                        var json = eval(term.dbJson);
                        if (!Array.isArray(json)){
                            json = [json];
                        }
                        html += "<option value=''>全部</option>";
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            html += " <option value=\""+item.value+"\">"+text+"</option>";
                        });
                    }else{
                        var termData = term.datas;
                        if (termData && termData.length > 0) {  //如果对象存在，并且个数大于0
                            html += "<option value=''>全部</option>";
                            $.each(termData, function (i, td) {
                                html += " <option value=\""+td.id+"\">"+td.name+"</option>";
                            });
                        }
                    }
                    html += " </select>";
                    html += " </div>";
                    if(i % 5 == 4 || (datas.length-1) == i){
                        html += " </div>";
                    }
                }
            });
            $("#extendTerm").html(html);
        }
    },
    requestParam: function () {
        var json = $("#searchForm").serializeJson();
        var condition = {};
        for (var k in json){
            if (k.indexOf("cell:") > -1){
                var value = json[k];
                var kk = k.substring(5, k.length);
                var arr = kk.split(":");
                var cell = arr[0];
                var type = arr[1];
                if(!condition[cell]){
                    condition[cell] = {cell:cell, type:type, cellValue:value};
                }else {
                    condition[cell].cellValue = value;
                }
                delete json[k];
            }
        }
        var extendArr = new Array();
        if(condition && Object.getOwnPropertyNames(condition).length > 0){
            for(var key in condition){
                extendArr.push(condition[key]);
            }
        }
        json.extendParams = JSON.stringify(extendArr);
        return json;
    }
}

//是否媒介部长
function isMJBZ() {
    var roles = user.roles;//获取用户角色
    var isMJBZ = false;//是否媒介政务
    if(roles){
        for(var i=0; i < roles.length; i++){
            if(roles[i].code == 'BZ' && roles[i].type == 'MJ'){
                isMJBZ = true;
                break;
            }
        }
    }
    return isMJBZ;
}

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
    if(deptCompanyCode == "JT" && (deptCode == "CW" || deptCode == "GL" || isZC || user.currentCompanyQx)){
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
    }else if(deptCode == "CW" || deptCode == "GL" || isZC || user.currentCompanyQx){
        requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST",function (result) {
            var company = result.data.company;
            if (company){
                deptId = company.id;//整个集团的业务和媒介部
                deptName = company.name;
            }
        });
    }

    //非业务人员默认展示公司
    renderDeptAndUser(deptId,deptName, (deptCode == "MJ" ? "MJ" : "YW"));//设置部门和员工

    //具体查询
    requestData({deptId: deptId},"/dept/listAllMJYWByDeptId","POST",function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

//设置部门和员工
function renderDeptAndUser(deptId,deptName, roleType) {
    $("#currentDeptId").val(deptId);
    $("#deptName").val(deptName);
    searchForm.loadWorker(deptId,roleType); //查询业务员工
    layui.use('form', function(){
        layui.form.render('select');//layui重新渲染下拉列表
    });
}

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,callBackFun, async) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: async || false,
        success: callBackFun
    });
}

var topBox = {
    reflush: function () {
        var params = extendTermObj.requestParam();//$("#searchForm").serializeJson();
        //左边顶部盒子
        requestData(params,"/mediaStatistics/getMediaTypeStatistics","post",function (resData) {
            $(".val-content").each(function (i, ele) {
                var val = resData.data.result.mediaTypeStatistics[ele.id] || 0,id_ = ele.id;
                if(id_ === 'articleNum'){
                    $(ele).text(fmtMoneyBringUnit(val,"件"));
                }else if(id_ === 'custNum'){
                    $(ele).text(fmtMoneyBringUnit(val,"个"));
                }else{
                    $(ele).text(fmtMoneyBringUnit(val));
                }
                if(id_ === 'profit' || id_ === 'noIncomeAmount'){
                    var rate = "0.00%";
                    if((resData.data.result.mediaTypeStatistics.saleAmount || 0) != 0){
                        rate = (val / resData.data.result.mediaTypeStatistics.saleAmount * 100).toFixed(2)+"%";
                    }
                    $("#"+id_+"Rate").text(rate);
                }
            });
            mediaTyleRate.reflush(resData.data.result.mediaTypeList);//加载图表
        },true);
        //加载趋势图
        requestData(params,"/mediaStatistics/listStatisticsByDate","post",function (resData) {
            mediaTyleRate.reflushTrend(resData.data.list);//加载图表
        },true);
    }
};

//板块图表
var mediaTyleRate = {
    pie1: {},
    pie2: {},
    trend: {},
    option1: {
        "title": {
            text:"板块占比",
            subtext:"报价金额",
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
            text:'板块占比【报价金额】暂无数据',
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
            subtext:"成本金额",
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
            text:'板块占比【成本金额】暂无数据',
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
    trendOption:{
        "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E","#FF69B4"],
        "backgroundColor": "#fff",
        "legend": {"data": ["稿件数量", "成交客户数量","报价金额", "成本金额", "未到账金额","利润"]},
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
                "name": "成交客户数量",
                type: 'line',
                // stack:"总量",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            },
            {
                "name": "报价金额",
                type: 'line',
                // stack:"总量",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            },
            {
                "name": "成本金额",
                type: 'line',
                // stack:"总量",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            },
            {
                "name": "未到账金额",
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
    init: function () {
        mediaTyleRate.initChart();
    },
    initChart: function () {
        mediaTyleRate.pie1 = echarts.init(document.getElementById('statisticsPie1'));
        mediaTyleRate.pie2 = echarts.init(document.getElementById('statisticsPie2'));
        //点击事件
        mediaTyleRate.pie1.on('click', function (params) {
            tableListObj.toggleModal(params.data.mediaTypeId,params.data.name,"mediaType");
        });
        mediaTyleRate.pie2.on('click', function (params) {
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
            mediaTyleRate.initChart();
            mediaTyleRate.option1.legend.itemHeight = mediaTyleRate.calHeight(mediaTyleRate.option1, "#statisticsPie1");
            mediaTyleRate.option2.legend.itemHeight = mediaTyleRate.calHeight(mediaTyleRate.option2, "#statisticsPie2");
            mediaTyleRate.pie1.setOption(mediaTyleRate.option1);
            mediaTyleRate.pie2.setOption(mediaTyleRate.option2);
        },0);
    },
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            mediaTyleRate.trend = echarts.init(document.getElementById('statisticsTrend'));
            mediaTyleRate.trend.setOption(mediaTyleRate.trendOption);
            window.onresize = function () {
                mediaTyleRate.trend.resize();
            }
        },0);
    },
    reflushPie1: function (dataMap) {
        mediaTyleRate.option1.legend.data = [];//清空原数据
        mediaTyleRate.option1.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            mediaTyleRate.option1.legend.data[i] = dataMap[i].mediaTypeName;
            var serie1 = {name:dataMap[i].mediaTypeName,value:dataMap[i].saleAmount,mediaTypeId:dataMap[i].mediaTypeId};//数据
            mediaTyleRate.option1.series[0].data[i] = serie1;
        }
    },
    reflushPie2: function (dataMap) {
        mediaTyleRate.option2.legend.data = [];//清空原数据
        mediaTyleRate.option2.series[0].data = [];//清空原数据
        //清空原来的数据
        for (var i = 0; i < dataMap.length; i++) {
            mediaTyleRate.option2.legend.data[i] = dataMap[i].mediaTypeName;
            var serie2 = {name:dataMap[i].mediaTypeName,value:dataMap[i].payAmount,mediaTypeId:dataMap[i].mediaTypeId};//数据
            mediaTyleRate.option2.series[0].data[i] = serie2;
        }
    },
    reflushTrend: function (dataList){
        //清空原来的数据
        mediaTyleRate.trendOption.xAxis[0].data = [];
        mediaTyleRate.trendOption.series[0].data = [];
        mediaTyleRate.trendOption.series[1].data = [];
        mediaTyleRate.trendOption.series[2].data = [];
        mediaTyleRate.trendOption.series[3].data = [];
        mediaTyleRate.trendOption.series[4].data = [];
        mediaTyleRate.trendOption.series[5].data = [];
        //数据封装
        var timeUnit = $("#timeQuantum").val() == 1 ? "日" : "月";
        for (var i = 0; i < dataList.length; i++) {
            mediaTyleRate.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
            mediaTyleRate.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
            mediaTyleRate.trendOption.series[1].data[i] = dataList[i].custNum || 0;
            mediaTyleRate.trendOption.series[2].data[i] = dataList[i].saleAmount || 0;
            mediaTyleRate.trendOption.series[3].data[i] = dataList[i].payAmount || 0;
            mediaTyleRate.trendOption.series[4].data[i] = dataList[i].noIncomeAmount || 0;
            mediaTyleRate.trendOption.series[5].data[i] = dataList[i].profit || 0;
        }
        //加载图表
        mediaTyleRate.loadTrend();
    },
    reflush: function (medioTypeList) {
        var params = $("#searchForm").serializeJson();
        mediaTyleRate.reflushPie1(medioTypeList);
        mediaTyleRate.reflushPie2(medioTypeList);
        mediaTyleRate.load();
    }
};

//页面列表
var tableListObj = {
    grid:{},
    //板块排名列表
    mediaTypeTable: {
        url: baseUrl + '/mediaStatistics/listStatisticsByMediaType',
        postData: extendTermObj.requestParam(),
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
        colNames: ['板块ID','板块名称', '稿件数量', '业务员数量', '媒介数量', '供应商数量', '客户数量', '报价', '成本','利润'],//表头
        jsonReader: {//server返回Json解析设定
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'mediaTypeId',
                index: 'mediaTypeId',
                editable: false,
                align: "center",
                sortable: false,
                search: true,
                hidden: true
            },
            {
                name: 'mediaTypeName',
                index: 'mediaTypeName',
                editable: false,
                align: "center",
                width: 150,
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
                name: 'businessUserNum',
                index: 'businessUserNum',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            },
            {
                name: 'mediaUserNum',
                index: 'mediaUserNum',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            },
            {
                name: 'supplierNum',
                index: 'supplierNum',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            },
            {
                name: 'custNum',
                index: 'custNum',
                editable: false,
                width: 80,
                align: "center",
                sortable: true
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                editable: false,
                align: "center",
                width: 100,
                sortable: true
            },
            {
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                align: "center",
                width: 100,
                sortable: true
            },
            {
                name: 'profit',
                index: 'profit',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            }
        ],
        onSelectRow: function(rowid,status,e){//实现点击选中，再次点击取消选中
            if(status){//true表示选中，则取消选中
                var rowData = $("#mediaTypeTable").jqGrid('getRowData',rowid);//获取当前行的数据
                $("#mediaTypeRowSelect").val(rowData.mediaTypeId);//设置板块列表行选中值，该值用于其他列表查询条件

            }else{
                $("#mediaTypeTable").jqGrid('resetSelection');
                $("#mediaTypeRowSelect").val("");//设置板块列表不选中，其他列表查询所有
            }
            /*tableListObj.mediaTable.caption = rowData.mediaTypeName+"媒体列表排名";
            tableListObj.businessTable.caption = rowData.mediaTypeName+"业务员列表排名";
            tableListObj.mediaUserTable.caption = rowData.mediaTypeName+"媒介列表排名";
            tableListObj.custTable.caption = rowData.mediaTypeName+"客户列表排名";
            tableListObj.supplierTable.caption = rowData.mediaTypeName+"供应商列表排名";*/
            tableListObj.mediaTypeRowSelect();//媒体列表查询
            tableListObj.tabTableSelect(0);//业务员排名查询
        },
        pager: "#mediaTypePager",
        viewrecords: true,
        caption: "板块列表排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    //媒体排名列表
    mediaTable: {
        url: baseUrl + '/mediaStatistics/listStatisticsByMedia',
        postData: extendTermObj.requestParam(),
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
        colNames: ['媒体ID','媒体名称', '稿件数量', '业务员数量', '媒介数量', '供应商数量', '客户数量', '报价', '成本','利润'],//表头
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
                name: 'articleNum',
                index: 'articleNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'businessUserNum',
                index: 'businessUserNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'mediaUserNum',
                index: 'mediaUserNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'supplierNum',
                index: 'supplierNum',
                editable: false,
                width:100,
                align: "center",
                sortable: true
            },
            {
                name: 'custNum',
                index: 'custNum',
                editable: false,
                width: 100,
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
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                width:100,
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
    //业务员排名列表
    businessTable: {
        url: baseUrl + '/mediaStatistics/listStatisticsByBusiness',
        postData: extendTermObj.requestParam(),
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
        colNames: ['业务员ID','业务员名称', '稿件数量', '媒介数量', '供应商数量', '客户数量', '报价', '成本','利润'],//表头
        jsonReader: {//server返回Json解析设定
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'businessUserId',
                index: 'businessUserId',
                editable: false,
                align: "center",
                sortable: false,
                search: true,
                hidden: true
            },
            {
                name: 'businessUserName',
                index: 'businessUserName',
                editable: false,
                width: 120,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    if(user.dept.code == 'MJ'){
                        return rows.businessUserName || "";
                    }else {
                        return tableListObj.getSingleLinkHtml(rows.businessUserId,rows.businessUserName,"business");
                    }
                }
            },
            {
                name: 'articleNum',
                index: 'articleNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'mediaUserNum',
                index: 'mediaUserNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'supplierNum',
                index: 'supplierNum',
                editable: false,
                width: 120,
                align: "center",
                sortable: true
            },
            {
                name: 'custNum',
                index: 'custNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
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
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'profit',
                index: 'profit',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            }
        ],
        pager: "#businessPager",
        viewrecords: true,
        caption: "业务员列表排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    //媒介排名列表
    mediaUserTable: {
        url: baseUrl + '/mediaStatistics/listStatisticsByMediaUser',
        postData: extendTermObj.requestParam(),
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
        colNames: ['媒介ID','媒介名称', '稿件数量', '业务员数量', '供应商数量', '客户数量', '报价', '成本','利润'],//表头
        jsonReader: {//server返回Json解析设定
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'mediaUserId',
                index: 'mediaUserId',
                editable: false,
                align: "center",
                sortable: true,
                search: true,
                hidden: true
            },
            {
                name: 'mediaUserName',
                index: 'mediaUserName',
                editable: false,
                width: 150,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    if(user.dept.code == 'YW'){
                        return rows.mediaUserName || "";
                    }else {
                        return tableListObj.getSingleLinkHtml(rows.mediaUserId,rows.mediaUserName,"mediaUser");
                    }
                }
            },
            {
                name: 'articleNum',
                index: 'articleNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'businessUserNum',
                index: 'businessUserNum',
                editable: false,
                width: 120,
                align: "center",
                sortable: true
            },
            {
                name: 'supplierNum',
                index: 'supplierNum',
                editable: false,
                width: 120,
                align: "center",
                sortable: true
            },
            {
                name: 'custNum',
                index: 'custNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
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
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'profit',
                index: 'profit',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            }
        ],
        pager: "#mediaUserPager",
        viewrecords: true,
        caption: "媒介列表排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    //供应商排名列表
    supplierTable: {
        url: baseUrl + '/mediaStatistics/listStatisticsBySupplier',
        postData: extendTermObj.requestParam(),
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
        colNames: ['供应商ID','供应商名称', '稿件数量', '业务员数量', '媒介数量', '客户数量', '报价', '成本','利润'],//表头
        jsonReader: {//server返回Json解析设定
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
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
                name: 'supplierName',
                index: 'supplierName',
                editable: false,
                width: 120,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    var userCompanyCode = user.dept.companyCode; //获取当前用户公司代码
                    if(rows.supplierCompanyCode == userCompanyCode){
                        return tableListObj.getSingleLinkHtml(rows.supplierId,rows.supplierName,"supplier");
                    }else{
                        if(value){
                            value =  value.substr(0,1)+"****";
                        }
                        return value || "";
                    }
                }
            },
            {
                name: 'articleNum',
                index: 'articleNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'businessUserNum',
                index: 'businessUserNum',
                editable: false,
                width: 120,
                align: "center",
                sortable: true
            },
            {
                name: 'mediaUserNum',
                index: 'mediaUserNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'custNum',
                index: 'custNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
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
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'profit',
                index: 'profit',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            }
        ],
        pager: "#supplierPager",
        viewrecords: true,
        caption: "供应商列表排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    //客户排名列表
    custTable: {
        url: baseUrl + '/mediaStatistics/listStatisticsByCust',
        postData: extendTermObj.requestParam(),
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
        colNames: ['客户公司ID','客户公司','客户ID','客户名称', '稿件数量', '业务员数量', '媒介数量', '供应商数量', '报价', '成本','利润'],//表头
        jsonReader: {//server返回Json解析设定
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'custCompanyId',
                index: 'custCompanyId',
                editable: false,
                align: "center",
                sortable: true,
                search: true,
                hidden: true
            },
            {
                name: 'custCompanyName',
                index: 'custCompanyName',
                editable: false,
                width: 120,
                align: "center",
                sortable: true,
                search: true,
                hidden: false
            },
            {
                name: 'custId',
                index: 'custId',
                editable: false,
                align: "center",
                sortable: true,
                search: true,
                hidden: true
            },
            {
                name: 'custName',
                index: 'custName',
                editable: false,
                width: 100,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return tableListObj.getSingleLinkHtml(rows.custId,rows.custName,"cust");
                }
            },
            {
                name: 'articleNum',
                index: 'articleNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'businessUserNum',
                index: 'businessUserNum',
                editable: false,
                width: 120,
                align: "center",
                sortable: true
            },
            {
                name: 'mediaUserNum',
                index: 'mediaUserNum',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'supplierNum',
                index: 'supplierNum',
                editable: false,
                width: 120,
                align: "center",
                sortable: true
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
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'profit',
                index: 'profit',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            }
        ],
        pager: "#custPager",
        viewrecords: true,
        caption: "客户列表排名",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    init:function () {
        //板块列表排名
        tableListObj.grid.mediaTypeRanking = new dataGrid("mediaTypeTable", tableListObj.mediaTypeTable, "mediaTypePager", "searchForm");
        tableListObj.grid.mediaTypeRanking.loadGrid();
        tableListObj.grid.mediaTypeRanking.setNavGrid();
        tableListObj.grid.mediaTypeRanking.defaultParams = extendTermObj.requestParam();//重置默认数据
        tableListObj.grid.mediaTypeRanking.search();
        // tableListObj.mediaTypeRowSelect();//默认查询与板块无关
        //如果是媒介，默认媒介排名
        if(user.dept.code == 'MJ'){
            tableListObj.tabTableSelect(1);//默认tab页为媒介
        }else{
            tableListObj.tabTableSelect(0);//默认tab页为业务排名
        }
    },
    mediaTypeRowSelect:function () {//板块列表行选择时，查询媒体列表

    },
    tabTableSelect:function (index) {//右边tab页选择时，列表改变, index = tab下标
        $(".tabContent").css("display","none");
        $(".tabTitle").removeClass("layui-this");//移除tab选中
        $($(".tabTitle")[index]).addClass("layui-this");
        $("#tabTableExportBtn").attr("dataIndex",index);//默认导出tab为业务排名的
        if(index == 0){//业务员排名
            $("#businessTableDiv").css("display","block");
            tableListObj.grid.businessTableRanking = new dataGrid("businessTable", tableListObj.businessTable, "businessPager", "searchForm");
            tableListObj.grid.businessTableRanking.loadGrid();
            tableListObj.grid.businessTableRanking.setNavGrid();
            tableListObj.grid.businessTableRanking.defaultParams =  extendTermObj.requestParam();//重置默认数据
            tableListObj.grid.businessTableRanking.search();
        }
        if(index == 1){//媒介排名
            $("#mediaUserTableDiv").css("display","block");
            tableListObj.grid.mediaUserTableRanking = new dataGrid("mediaUserTable", tableListObj.mediaUserTable, "mediaUserPager", "searchForm");
            tableListObj.grid.mediaUserTableRanking.loadGrid();
            tableListObj.grid.mediaUserTableRanking.setNavGrid();
            tableListObj.grid.mediaUserTableRanking.defaultParams = extendTermObj.requestParam();//重置默认数据
            tableListObj.grid.mediaUserTableRanking.search();
        }
        if(index == 2){////媒体列表
            $("#mediaTableDiv").css("display","block");
            tableListObj.grid.mediaRanking = new dataGrid("mediaTable", tableListObj.mediaTable, "mediaPager", "searchForm");
            tableListObj.grid.mediaRanking.loadGrid();
            tableListObj.grid.mediaRanking.setNavGrid();
            tableListObj.grid.mediaRanking.defaultParams = extendTermObj.requestParam();//重置默认数据
            tableListObj.grid.mediaRanking.search();
        }
        if(index == 3){//客户排名
            $("#custTableDiv").css("display","block");
            tableListObj.grid.custTableRanking = new dataGrid("custTable", tableListObj.custTable, "custPager", "searchForm");
            tableListObj.grid.custTableRanking.loadGrid();
            tableListObj.grid.custTableRanking.setNavGrid();
            tableListObj.grid.custTableRanking.defaultParams = extendTermObj.requestParam();//重置默认数据
            tableListObj.grid.custTableRanking.search();
        }
        if(index == 4){//供应商排名
            $("#supplierTableDiv").css("display","block");
            tableListObj.grid.supplierTableRanking = new dataGrid("supplierTable", tableListObj.supplierTable, "supplierPager", "searchForm");
            tableListObj.grid.supplierTableRanking.loadGrid();
            tableListObj.grid.supplierTableRanking.setNavGrid();
            tableListObj.grid.supplierTableRanking.defaultParams = extendTermObj.requestParam();//重置默认数据
            tableListObj.grid.supplierTableRanking.search();
        }
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