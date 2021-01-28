/*
//查询区域
var searchForm = {
    init: function () {
        searchForm.loadDept();
        searchForm.loadWorker();
    },
    //显示或隐藏发布日期
    showIssuedDate: function (val) {
        if (val == 5) {
            $("#issuedDateFormGroup").show();
        } else {
            $("#issuedDateFormGroup").hide();
            searchForm.search();
        }
    },
    loadDept:function(){
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        var deptDiv = document.getElementById("deptDiv");
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
        if(((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'YW'|| user.dept.code == 'GL')) || user.dept.code == 'CW'){
            //deptDiv.style.display = 'block';
            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData(isZC())],
                onNodeSelected: function (event, data) {
                    $("#currentDeptId").val(data.id);
                    $("#chooseDeptName").val(data.text);
                    $("#deptModal").modal('hide');
                    //如果是业务/媒介部，则设置业务/媒介部ID，如果是其他（JT、GL）不设置
                    if(data.code == 'YW'){
                        searchForm.loadWorker(data.id,data.code);
                    }else{//其他情况清空
                        $("#currentUserId").empty();//初始化
                        $("#currentUserId").append('<option value="">全部</option>');
                    }
                }
            });
            $("#cleanDept").click(function () {
                $("#currentUserId").empty();//初始化
                $("#currentUserId").append('<option value="">全部</option>');
                $("#currentDeptId").val("");//部门初始化
                $("#chooseDeptName").val("");//部门初始化
            });
        }
    },
    //加载业务员
    //加载此部门下的业务员
    loadWorker: function(deptId,roleType){
        deptId = deptId || "";
        var ele = $("#currentUserId");
        //如果没有部门权限 和 公司权限 并且不是财务，则只加载当前用户
        if(!user.currentDeptQx && !user.currentCompanyQx && user.dept.code != 'CW'){
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
    //查询
    search: function () {
        topBox.reflush();
        custRanking.reflush();
        custRanking.loadTable();
    }
};

//判断当前用户是否总裁
var isZC = function () {
    var roles = user.roles;//获取用户角色
    var isZC = false;//是否总裁角色
    if(roles){
        for(var i=0; i < roles.length; i++){
            if(roles[i].code == 'ZC'){
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
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    if(deptCompanyCode == "JT" && (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL")){
        requestData(null,"/dept/getRootDept","POST",function (result) {
            var root = result.data.root;
            if (root){
                deptId = root.id;//整个集团的业务和媒介部
            }else{
                deptId = 517;//整个集团的业务和媒介部
            }
        });
    }else if(deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL"){
        requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST",function (result) {
            var company = result.data.company;
            if (company){
                deptId = company.id;//整个集团的业务和媒介部
            }
        });
    }
    //具体查询
    requestData({deptId: deptId,deptCode:'YW'},"/dept/listAllDeptByIdAndCode","POST",function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

/!**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param callBackFun 成功回调方法
 *!/
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

var topBox = {
    reflush: function () {
        var params = $("#searchForm").serializeJson();
        $.ajax({
            url: baseUrl + "/employeeManage/topStatistics",
            data: params,
            success: function (resData) {
                $(".val-content").each(function (i, ele) {
                    var val = resData[ele.id] || 0;
                    $(ele).text(val);
                });
            }
        });

        var arr = user.deptIdSet;
        var deptsStr = "";
        for (var i = 0; i < arr.length; i++) {
            deptsStr += (arr[i] + ",");
        }

        $.ajax({
            url: baseUrl + "/custManagerStatistics/everyDeptUserCount",
            data: {list: deptsStr},
            success: function (resData) {
                $("#usersContent").empty();
                var html = '<table>';
                for (var i = 0; i < resData.length; i++) {
                    html += '<tr><td style="width: 35%">${deptName}</td>' +
                        '<td style="width: 32.5%"><i class="icon fa fa-venus text-success"></i>&nbsp;' +
                        '<span><b class="text-warning">${nan}</b>&nbsp;人</span></td>' +
                        '<td style="width: 32.5%"><i class="icon fa fa-mars text-danger" ></i>&nbsp;' +
                        '<span><b class="text-warning">${nv}</b>&nbsp;人</span></td></tr>';
                    html = html.replace("${deptName}", (resData[i].deptName || ""));
                    html = html.replace("${nan}", (resData[i].nan == undefined ? 0 : resData[i].nan));
                    html = html.replace("${nv}", (resData[i].nv == undefined ? 0 : resData[i].nv));
                }
                html += "</table>";
                $("#usersContent").html(html);
            }
        });
    }
};

//客户排名
var custRanking = {
    pie1: {},
    pie2: {},
    pie3: {},
    pie4: {},
    grid: {},
    option1: {
        "title": {},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["大型客户", "中型客户", "小型客户"]},
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": true,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "客户类型", "type": "pie", "radius": "55%", "center": ["50%", "60%"],
                "data": [
                    {"value": 0, "name": "大型客户"},
                    {"value": 0, "name": "中型客户"},
                    {"value": 0, "name": "小型客户"}],
                "itemStyle": {"emphasis": {"shadowBlur": 10, "shadowOffsetX": 0, "shadowColor": "rgba(0, 0, 0, 0.5)"}}
            }]
    },
    option2: {
        "title": {},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["VIP客户", "会员客户", "普通客户"]},
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": true,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "客户级别", "type": "pie", "radius": "55%", "center": ["50%", "60%"],
                "data": [
                    {"value": 0, "name": "VIP客户"},
                    {"value": 0, "name": "会员客户"},
                    {"value": 0, "name": "普通客户"}],
                "itemStyle": {"emphasis": {"shadowBlur": 10, "shadowOffsetX": 0, "shadowColor": "rgba(0, 0, 0, 0.5)"}}
            }]
    },
    option3: {
        "title": {},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["待开发", "有效", "流失"]},
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": true,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "客户状态", "type": "pie", "radius": "55%", "center": ["50%", "60%"],
                "data": [
                    {"value": 0, "name": "待开发"},
                    {"value": 0, "name": "有效"},
                    {"value": 0, "name": "流失"}],
                "itemStyle": {"emphasis": {"shadowBlur": 10, "shadowOffsetX": 0, "shadowColor": "rgba(0, 0, 0, 0.5)"}}
            }]
    },
    tableObject: {
        url: baseUrl + '/employeeManage/getEmployee',
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
        rowNum: 15,
        rowList: [10, 20, 30],
        colNames: ['头像','姓名', '账号', '性别', '员工状态', '推荐人'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'image',
                index: 'image',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                search: true,
                hidden: false
            },
            {
                name: 'emp_name',
                index: "emp_name",
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: false,
                formatter: function (value, grid, rows) {
                    var html = rows.emp_name;
                    if(rows.user_id){
                        if(rows.deptCode == 'YW'){
                            html = statisticsToggle.getSingleLinkHtml(rows.user_id,rows.emp_name,"business");
                        }
                        if(rows.deptCode == 'MJ'){
                            html = statisticsToggle.getSingleLinkHtml(rows.user_id,rows.emp_name,"mediaUser");
                        }
                    }
                    return html;
                }
            },
            {
                name: 'user_name',
                index: "user_name",
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'emp_gender',
                index: "emp_gender",
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: red;">女</b>';
                    } else if (value == 1) {
                        return '<b style="color: green;">男</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'state',
                index: "state",
                width: 120,
                editable: false,
                align: "center",
                sortable: false,
                hidden: false,
                formatter: function (value, grid, rowData) {
                    var processState = rowData.process_state;
                    var isNotApprove = !processState || processState < 3;
                    rowData.isNotApprove = isNotApprove;
                    // 有流程进行中，且流程尚未完结，显示为审核中；
                    if (isNotApprove) {
                        if (value == 0) {
                            return '<b style="color: blue;">试用</b>';
                        } else if (value == 1) {
                            return '<b style="color: green;">转正</b>';
                        } else if (value == 2) {
                            return '<b style="color: black;">离职</b>';
                        } else if (value == 3) {
                            return '<b style="color: orange;">准备交接</b>';
                        } else if (value == 4) {
                            return '<b style="color: orangered;">交接中</b>';
                        } else {
                            return '';
                        }
                    } else {
                        return '<b style="color: red;">审核中</b>';
                    }
                }
            },
            {
                name: 'emp_relative_name',
                index: 'emp_relative_name',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            }

        ],
        /!**
         * 翻页时保存当前页面的选中数据
         * @param pageBtn
         *!/
        onPaging: function (pageBtn) {
            //跨页面选择
        },
        gridComplete: function () {
        },
        pager: "#pager",
        viewrecords: true,
        caption: "最近入职员工",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    initChart: function () {
        custRanking.pie1 = echarts.init(document.getElementById('statisticsPie1'));
        custRanking.pie2 = echarts.init(document.getElementById('statisticsPie2'));
        custRanking.pie3 = echarts.init(document.getElementById('statisticsPie3'));
    },
    init: function () {
        custRanking.initChart();
        custRanking.grid = new dataGrid("table", custRanking.tableObject, "pager", "searchForm");
        custRanking.grid.loadGrid();
    },
    load: function () {
        custRanking.initChart();
        custRanking.pie1.setOption(custRanking.option1);
        custRanking.pie2.setOption(custRanking.option2);
        custRanking.pie3.setOption(custRanking.option3);
    },
    reflushPie1: function (dataMap) {
        var searchFormData = $("#searchForm").serializeJson();
        var len = custRanking.option1.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option1.series[0].data[i].value = 0;
        }

        custRanking.option1.series[0].data[0].value = dataMap.dxkh || 0;
        custRanking.option1.series[0].data[1].value = dataMap.zxkh || 0;
        custRanking.option1.series[0].data[2].value = dataMap.xxkh || 0;
    },
    reflushPie2: function (dataMap) {
        var searchFormData = $("#searchForm").serializeJson();
        var len = custRanking.option2.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option2.series[0].data[i].value = 0;
        }

        custRanking.option2.series[0].data[0].value = dataMap.vipkh || 0;
        custRanking.option2.series[0].data[1].value = dataMap.hykh || 0;
        custRanking.option2.series[0].data[2].value = dataMap.ptkh || 0;
    },
    reflushPie3: function (dataMap) {
        var searchFormData = $("#searchForm").serializeJson();
        var len = custRanking.option3.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option3.series[0].data[i].value = 0;
        }

        custRanking.option3.series[0].data[0].value = dataMap.dkf || 0;
        custRanking.option3.series[0].data[1].value = dataMap.yx || 0;
        custRanking.option3.series[0].data[2].value = dataMap.ls || 0;
    },
    reflush: function () {
        var params = $("#searchForm").serializeJson();
        $.ajax({
            url: baseUrl + "/custManagerStatistics/custPie",
            data: params,
            success: function (resData) {
                custRanking.reflushPie1(resData);
                custRanking.reflushPie2(resData);
                custRanking.reflushPie3(resData);
                custRanking.load();
            }
        });
    },


    //加载左边的表格
    loadTable: function () {
        //加载表格
        custRanking.grid.search();
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
};*/
