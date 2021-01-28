//查询区域
var searchForm = {
    init: function () {
        var mediaId = getQueryString("mediaId");//获取统计概况页跳转的请求参数
        var mediaName = getQueryString("mediaName");//获取统计概况页跳转的请求参数
        searchForm.loadMediaType();
        searchForm.loadMedia(null,mediaId,mediaName);
        searchForm.loadDept();
        searchForm.loadWorker();
        //如果是业务员，则不让他看到供应商排名、媒介排名
        if(user.dept.code == 'YW'){
            $("#supplierTab").css("display","none");
            $("#supplierTableDiv").css("display","none");

            $("#mediaUserTab").css("display","none");
            $("#mediaUserTableDiv").css("display","none");
        }else{
            $("#supplierTab").css("display","inline-block");
            $("#supplierTableDiv").css("display","inline-block");

            $("#mediaUserTab").css("display","inline-block");
            $("#mediaUserTableDiv").css("display","inline-block");
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

        $("#mediaRowSelect").val("");//初始化板块列表行选中值，该值用于其他列表查询条件
    },
    //显示或隐藏发布日期
    showIssuedDate: function (val) {
        var btnNode = $("#searchButton");
        if (val == 3) {
            $("#issuedDateFormGroup").show();
            $("#searchButton").remove();
            $("#rowTwo").append(btnNode);
        } else {
            $("#issuedDateFormGroup").hide();
            $("#searchButton").remove();
            $("#rowOne").append(btnNode);
            searchForm.search();
        }
    },
    loadMediaType: function(){//加载媒体类型
        $.ajax({
            url: baseUrl+"/mediaPlate/0",   //mediaType?parentId=0
            dataType:"json",
            async: false,
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
                        // $("#deptId").val(data.id);
                        $("#userType").val(data.code);
                        $("#userLabel").text("业务员:");
                        renderDeptAndUser(data.id,data.text, data.code);//设置部门和员工
                    }else if(data.code == 'MJ'){
                        // $("#deptId").val(data.id);
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
                // $("#deptId").val("");
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
    loadMedia:function (mType,mediaId,mediaName) {
        var mediaNode = $("#mediaId");
        if(mType){
            requestData({plateId:mType},"/mediaStatistics/listMediaByType","post",function (result) {
                mediaNode.empty();
                mediaNode.append('<option value="">全部</option>');
                if(result.data.list && result.data.list.length > 0){
                    for (var i = 0; i < result.data.list.length; i++) {
                        mediaNode.append("<option value=" + result.data.list[i].id + ">" + result.data.list[i].name + "</option>");
                        if(result.data.list[i].id == mediaId){
                            mediaNode.val(mediaId);
                        }
                    }
                }
                layui.use('form', function(){  //刷新下拉列表
                    var form = layui.form;
                    form.render('select');
                });
            }, true)
        }else{
            if(mediaId){
                mediaNode.append("<option value=" + mediaId + ">" + mediaName + "</option>");
                mediaNode.val(mediaId);
            }
        }
    },
    //查询
    search: function () {
        $("#mediaRowSelect").val("");//初始化板块列表行选中值，该值用于其他列表查询条件
        topBox.reflush();//加载顶部统计
        tableListObj.init();//加载表格
    }
};

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
        var params = $("#searchForm").serializeJson();
        //左边顶部盒子
        requestData(params,"/mediaStatistics/getMediaStatistics","post",function (resData) {
            $(".val-content").each(function (i, ele) {
                var val = resData.data.result.mediaStatistics[ele.id] || 0,id_ = ele.id;
                if(id_ === 'articleNum'){
                    $(ele).text(fmtMoneyBringUnit(val,"件"));
                }else if(id_ === 'custNum'){
                    $(ele).text(fmtMoneyBringUnit(val,"个"));
                }else{
                    $(ele).text(fmtMoneyBringUnit(val));
                }
                if(id_ === 'profit' || id_ === 'noIncomeAmount'){
                    var rate = "0.00%";
                    if((resData.data.result.mediaStatistics.saleAmount || 0) != 0){
                        rate = (val / resData.data.result.mediaStatistics.saleAmount * 100).toFixed(2)+"%";
                    }
                    $("#"+id_+"Rate").text(rate);
                }
            });
        },true);
        //加载趋势图
        requestData(params,"/mediaStatistics/listStatisticsByDate","post",function (resData) {
            mediaTyleRate.reflushTrend(resData.data.list);//加载图表
        },true);
    }
};

//板块图表
var mediaTyleRate = {
    trend: {},
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
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            mediaTyleRate.trend = echarts.init(document.getElementById('statisticsTrend'));
            mediaTyleRate.trend.setOption(mediaTyleRate.trendOption);
            window.onresize = function () {
                mediaTyleRate.trend.resize();
            }
        },0);
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
};

//页面列表
var tableListObj = {
    grid:{},
    //媒体排名列表
    mediaTable: {
        url: baseUrl + '/mediaStatistics/listStatisticsByMedia',
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
        colNames: ['媒体板块ID','板块名称','媒体ID','媒体名称', '稿件数量', '业务员数量', '媒介数量', '供应商数量', '客户数量', '报价', '成本','利润'],//表头
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
                width:150,
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
        onSelectRow: function(rowid,status,e){
            if(status){//true表示选中，则取消选中
                var rowData = $("#mediaTable").jqGrid('getRowData',rowid);//获取当前行的数据
                $("#mediaRowSelect").val(rowData.mediaId);//设置媒体列表行选中值，该值用于其他列表查询条件

            }else{
                $("#mediaTable").jqGrid('resetSelection');
                $("#mediaRowSelect").val("");//设置媒体列表不选中，其他列表查询所有
            }
            tableListObj.tabTableSelect(0);//业务员排名查询
        },
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
        //媒体列表
        tableListObj.grid.mediaRanking = new dataGrid("mediaTable", tableListObj.mediaTable, "mediaPager", "searchForm");
        tableListObj.grid.mediaRanking.loadGrid();
        tableListObj.grid.mediaRanking.setNavGrid();
        tableListObj.grid.mediaRanking.defaultParams = {};//清空历史表单数据
        tableListObj.grid.mediaRanking.search();

        //如果是媒介，默认媒介排名
        if(user.dept.code == 'MJ'){
            tableListObj.tabTableSelect(1);//默认tab页为媒介
        }else{
            tableListObj.tabTableSelect(0);//默认tab页为业务排名
        }

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
            tableListObj.grid.businessTableRanking.defaultParams = {};//清空历史表单数据
            tableListObj.grid.businessTableRanking.search();
        }
        if(index == 1){//媒介排名
            $("#mediaUserTableDiv").css("display","block");
            tableListObj.grid.mediaUserTableRanking = new dataGrid("mediaUserTable", tableListObj.mediaUserTable, "mediaUserPager", "searchForm");
            tableListObj.grid.mediaUserTableRanking.loadGrid();
            tableListObj.grid.mediaUserTableRanking.setNavGrid();
            tableListObj.grid.mediaUserTableRanking.defaultParams = {};//清空历史表单数据
            tableListObj.grid.mediaUserTableRanking.search();
        }
        if(index == 2){//客户排名
            $("#custTableDiv").css("display","block");
            tableListObj.grid.custTableRanking = new dataGrid("custTable", tableListObj.custTable, "custPager", "searchForm");
            tableListObj.grid.custTableRanking.loadGrid();
            tableListObj.grid.custTableRanking.setNavGrid();
            tableListObj.grid.custTableRanking.defaultParams = {};//清空历史表单数据
            tableListObj.grid.custTableRanking.search();
        }
        if(index == 3){//供应商排名
            $("#supplierTableDiv").css("display","block");
            tableListObj.grid.supplierTableRanking = new dataGrid("supplierTable", tableListObj.supplierTable, "supplierPager", "searchForm");
            tableListObj.grid.supplierTableRanking.loadGrid();
            tableListObj.grid.supplierTableRanking.setNavGrid();
            tableListObj.grid.supplierTableRanking.defaultParams = {};//清空历史表单数据
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