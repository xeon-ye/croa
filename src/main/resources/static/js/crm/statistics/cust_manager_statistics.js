var XMflag = false;
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
            deptDiv.style.display = 'block';

            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData(isZC())],
                onNodeSelected: function (event, data) {
                    $("#deptModal").modal('hide');
                    renderDeptAndUser(data.id, data.text);//设置部门和员工
                }
            });
            $("#cleanDept").click(function () {
               /* $("#currentUserId").empty();//初始化
                $("#currentUserId").append('<option value="">全部</option>');
                $("#currentDeptId").val("");//部门初始化
                $("#chooseDeptName").val("");//部门初始化*/
                renderDeptAndUser(deptId, deptName);
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
        $("#topChart").find(".val-content").text("");
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
            if(roles[i].code == 'ZC' || roles[i].code == 'FZC'){
                isZC = true;
                break;
            }
        }
    }
    return isZC;
}

//设置部门和员工
function renderDeptAndUser(deptId,deptName) {
    $("#currentDeptId").val(deptId);
    $("#chooseDeptName").val(deptName);
    searchForm.loadWorker(deptId,"YW"); //查询业务员工
    layui.use('form', function(){
        layui.form.render('select');//layui重新渲染下拉列表
    });
}

//获取部门树数据
var deptId = user.dept.id;//当前用户部门ID
var deptName = user.dept.name;//当前部门名称
function getTreeData(isZC) {
    var deptTreeData = {};
    var deptCode = user.dept.code;//当前部门编码
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
$().ready(function () {
    statisticsModal.init();//初始化模态框

    var project = projectDirector();
    if( project && project.contains(user.id)){
        XMflag =true;
    }
    //查询条件初始化
    searchForm.init();
    //客户排名初始化
    custRanking.init();
    //页面自动加载
    searchForm.search();
    //使用layui表单，下拉列表改变事件
    layui.use('form', function(){
        var form = layui.form;
        //时间改变事件
        form.on('select(timeQuantum)', function(data){
            searchForm.showIssuedDate(data.value);
        });
        //业务员改变事件
        form.on('select(currentUserId)', function(data){
            searchForm.search();
        });
        form.render();
    });
});
var topBox = {
    reflush: function () {
        var params = $("#searchForm").serializeJson();
        $.ajax({
            url: baseUrl + "/custManagerStatistics/topStatistics",
            data: params,
            success: function (resData) {
                $(".val-content").each(function (i, ele) {
                    var val = resData[ele.id] || 0,id_ = ele.id;
                    if(id_ === 'djkhs' || id_ === 'custNum'){
                        $(ele).text(fmtMoneyBringUnit(val,"个"));
                    }else{
                        $(ele).text(fmtMoneyBringUnit(val));
                    }
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
    // pie2: {},
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
    // option2: {
    //     "title": {},
    //     "backgroundColor": "#fff",
    //     "legend": {"y": "8px", "data": ["VIP客户", "会员客户", "普通客户"]},
    //     tooltip: {
    //         trigger: 'item',
    //         formatter: "{a} <br/>{b} : {c} ({d}%)"
    //     },
    //     "calculable": true,
    //     "xAxis": null,
    //     "yAxis": null,
    //     "series": [
    //         {
    //             "name": "客户级别", "type": "pie", "radius": "55%", "center": ["50%", "60%"],
    //             "data": [
    //                 {"value": 0, "name": "VIP客户"},
    //                 {"value": 0, "name": "会员客户"},
    //                 {"value": 0, "name": "普通客户"}],
    //             "itemStyle": {"emphasis": {"shadowBlur": 10, "shadowOffsetX": 0, "shadowColor": "rgba(0, 0, 0, 0.5)"}}
    //         }]
    // },
    /*option3: {
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
    },*/
    option4: {
        "title": {},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["企业客户", "个人客户"]},
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
        "calculable": true,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "客户类型", "type": "pie", "radius": "55%", "center": ["50%", "60%"],
                "data": [
                    {"value": 1, "name": "企业客户"},
                    {"value": 0, "name": "个人客户"}],
                "itemStyle": {"emphasis": {"shadowBlur": 10, "shadowOffsetX": 0, "shadowColor": "rgba(0, 0, 0, 0.5)"}}
            }]
    },
    option5: {
        "title": {},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["标准", "非标准"]},
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('statisticsPie5');
                if ($(id).width() - p[0]- $(id).find("div .echarts-tooltip").width()-20 <0) {
                    p[0] = p[0] - $(id).find("div .echarts-tooltip").width() -40;
                }
                return [p[0], p[1]];
            },
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": true,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "是否标准", "type": "pie", "radius": "55%", "center": ["50%", "60%"],
                "data": [
                    {"value": 1, "name": "标准"},
                    {"value": 0, "name": "非标准"}],
                "itemStyle": {"emphasis": {"shadowBlur": 10, "shadowOffsetX": 0, "shadowColor": "rgba(0, 0, 0, 0.5)"}}
            }]
    },
    option6: {
        "title": {},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["规范", "非规范"]},
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('statisticsPie6');
                if ($(id).width() - p[0]- $(id).find("div .echarts-tooltip").width()-20 <0) {
                    p[0] = p[0] - $(id).find("div .echarts-tooltip").width() -40;
                }
                return [p[0], p[1]];
            },
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": true,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "是否规范", "type": "pie", "radius": "55%", "center": ["50%", "60%"],
                "data": [
                    {"value": 1, "name": "规范"},
                    {"value": 0, "name": "非规范"}],
                "itemStyle": {"emphasis": {"shadowBlur": 10, "shadowOffsetX": 0, "shadowColor": "rgba(0, 0, 0, 0.5)"}}
            }]
    },
    option7: {
        "title": {},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["强保护", "弱保护"]},
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('statisticsPie7');
                if ($(id).width() - p[0]- $(id).find("div .echarts-tooltip").width()-20 <0) {
                    p[0] = p[0] - $(id).find("div .echarts-tooltip").width() -40;
                }
                return [p[0], p[1]];
            },
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": true,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "是否强保护", "type": "pie", "radius": "55%", "center": ["50%", "60%"],
                "data": [
                    {"value": 1, "name": "强保护"},
                    {"value": 0, "name": "弱保护"}],
                "itemStyle": {"emphasis": {"shadowBlur": 10, "shadowOffsetX": 0, "shadowColor": "rgba(0, 0, 0, 0.5)"}}
            }]
    },
    option8: {
        "title": {},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["A类保护", "B类保护", "C类保护", "不保护"]},
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('statisticsPie8');
                if ($(id).width() - p[0]- $(id).find("div .echarts-tooltip").width()-20 <0) {
                    p[0] = p[0] - $(id).find("div .echarts-tooltip").width() -40;
                }
                return [p[0], p[1]];
            },
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": true,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "是否强保护", "type": "pie", "radius": "55%", "center": ["50%", "60%"],
                "data": [
                    {"value": 3, "name": "A类保护"},
                    {"value": 2, "name": "B类保护"},
                    {"value": 1, "name": "C类保护"},
                    {"value": 0, "name": "不保护"}],
                "itemStyle": {"emphasis": {"shadowBlur": 10, "shadowOffsetX": 0, "shadowColor": "rgba(0, 0, 0, 0.5)"}}
            }]
    },
    tableObject: {
        url: baseUrl + '/custManagerStatistics/listCustByParam',
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
        rowNum: 10,
        rowList: [10, 20, 30],
        // colNames: ['客户公司名称','对接人ID', '对接人', '地区公司', '客户状态ID', '客户状态', '客户级别ID', '客户级别', '客户类型ID', '客户类型', '负责人'],
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
                formatter: function (value, grid, rows) {
                    if(XMflag){
                        return value == null ? "":"*****" ;
                    }else {
                        return custRanking.getSingleLinkHtml(rows.companyUserId, rows.companyUserName,"cust");
                    }
                }
            },
            {
                name: 'normalize',
                index: 'normalize',
                label: '是否规范',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a) {
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
                label: '强弱保护',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a) {
                    if (a == 1) {
                        return "<span style='color:#1ab394'>强保护</span>";
                    } else {
                        return "弱保护";
                    }
                }
            },
            {
                name: 'protectLevel',
                index: 'protectLevel',
                label: '保护等级',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a) {
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
                name: 'custType',
                index: 'custType',
                label: '客户类型',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (a) {
                    if (a == 1) {
                        return "大型客户";
                    } else if(a == 2){
                        return "中型客户";
                    } else if(a == 3){
                        return "小型客户";
                    } else{
                        return "";
                    }
                }
            },
            {
                name: 'createTime',
                index: 'createTime',
                label: '登记时间',
                editable: false,
                width: 100,
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
                name: 'ywUserId',
                index: 'ywUserId',
                label: '负责人id',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                hidden: true
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
        gridComplete: function () {
            var width = $('#table').closest('.jqGrid_wrapper').width();
            $('#table').setGridWidth(width);
        },
        pager: "#pager",
        viewrecords: true,
        caption: "最近新建客户",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    initChart: function () {
        custRanking.pie1 = echarts.init(document.getElementById('statisticsPie1'));
        // custRanking.pie2 = echarts.init(document.getElementById('statisticsPie2'));
        // custRanking.pie3 = echarts.init(document.getElementById('statisticsPie3'));
        custRanking.pie4 = echarts.init(document.getElementById('statisticsPie4'));
        custRanking.pie5 = echarts.init(document.getElementById('statisticsPie5'));
        custRanking.pie6 = echarts.init(document.getElementById('statisticsPie6'));
        custRanking.pie7 = echarts.init(document.getElementById('statisticsPie7'));
        custRanking.pie8 = echarts.init(document.getElementById('statisticsPie8'));
    },
    init: function () {
        custRanking.initChart();
        custRanking.grid = new dataGrid("table", custRanking.tableObject, "pager", "searchForm");
        custRanking.grid.loadGrid();
    },
    load: function () {
        custRanking.initChart();
        custRanking.pie1.setOption(custRanking.option1);
        // custRanking.pie2.setOption(custRanking.option2);
        // custRanking.pie3.setOption(custRanking.option3);
        custRanking.pie4.setOption(custRanking.option4);
        custRanking.pie5.setOption(custRanking.option5);
        custRanking.pie6.setOption(custRanking.option6);
        custRanking.pie7.setOption(custRanking.option7);
        custRanking.pie8.setOption(custRanking.option8);
    },
    reflushPie1: function (dataMap) {
        var len = custRanking.option1.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option1.series[0].data[i].value = 0;
        }

        custRanking.option1.series[0].data[0].value = dataMap.dxkh || 0;
        custRanking.option1.series[0].data[1].value = dataMap.zxkh || 0;
        custRanking.option1.series[0].data[2].value = dataMap.xxkh || 0;
    },
    // reflushPie2: function (dataMap) {
    //     var len = custRanking.option2.series[0].data.length;
    //     //清空原来的数据
    //     for (var i = 0; i < len; i++) {
    //         custRanking.option2.series[0].data[i].value = 0;
    //     }
    //
    //     custRanking.option2.series[0].data[0].value = dataMap.vipkh || 0;
    //     custRanking.option2.series[0].data[1].value = dataMap.hykh || 0;
    //     custRanking.option2.series[0].data[2].value = dataMap.ptkh || 0;
    // },
    /*reflushPie3: function (dataMap) {
        var len = custRanking.option3.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option3.series[0].data[i].value = 0;
        }

        custRanking.option3.series[0].data[0].value = dataMap.dkf || 0;
        custRanking.option3.series[0].data[1].value = dataMap.yx || 0;
        custRanking.option3.series[0].data[2].value = dataMap.ls || 0;
    },*/
    reflushPie4: function (dataMap) {
        var len = custRanking.option4.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option4.series[0].data[i].value = 0;
        }

        custRanking.option4.series[0].data[0].value = dataMap.qykh || 0;
        custRanking.option4.series[0].data[1].value = dataMap.grkh || 0;

    },
    reflushPie5: function (dataMap) {
        var len = custRanking.option5.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option5.series[0].data[i].value = 0;
        }

        custRanking.option5.series[0].data[0].value = dataMap.bzkh || 0;
        custRanking.option5.series[0].data[1].value = dataMap.fbzkh || 0;
    },
    reflushPie6: function (dataMap) {
        var len = custRanking.option6.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option6.series[0].data[i].value = 0;
        }

        custRanking.option6.series[0].data[0].value = dataMap.gfkh || 0;
        custRanking.option6.series[0].data[1].value = dataMap.fgfkh || 0;
    },
    reflushPie7: function (dataMap) {
        var len = custRanking.option7.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option7.series[0].data[i].value = 0;
        }

        custRanking.option7.series[0].data[0].value = dataMap.qbhkh || 0;
        custRanking.option7.series[0].data[1].value = dataMap.rbhkh || 0;
    },
    reflushPie8: function (dataMap) {
        var len = custRanking.option8.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            custRanking.option8.series[0].data[i].value = 0;
        }

        custRanking.option8.series[0].data[0].value = dataMap.albh || 0;
        custRanking.option8.series[0].data[1].value = dataMap.blbh || 0;
        custRanking.option8.series[0].data[2].value = dataMap.clbh || 0;
        custRanking.option8.series[0].data[3].value = dataMap.dlbh || 0;
    },
    reflush: function () {
        var params = $("#searchForm").serializeJson();
        $.ajax({
            url: baseUrl + "/custManagerStatistics/custPie",
            data: params,
            success: function (resData) {
                custRanking.reflushPie1(resData);
                // custRanking.reflushPie2(resData);
                // custRanking.reflushPie3(resData);
                custRanking.reflushPie4(resData);
                custRanking.reflushPie5(resData);
                custRanking.reflushPie6(resData);
                custRanking.reflushPie7(resData);
                custRanking.reflushPie8(resData);
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
};