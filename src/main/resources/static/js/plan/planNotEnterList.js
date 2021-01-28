$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#planTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });
    searchForm.init(); //初始化条件
    createTable(); //表格定义
});

/**
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
        async: async,
        success: callBackFun
    });
}

//查询区域
var searchForm = {
    init: function () {
        searchForm.loadDept();
        searchForm.loadWorker();
        searchForm.renderGroup();
        layui.use('form', function(){
            layui.form.render('select');//layui重新渲染下拉列表
        });
    },
    loadDept:function(){
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        var deptDiv = document.getElementById("deptDiv");
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许行政 业务
        if(isXZ() || ((currentDeptQx || currentCompanyQx || isZW()) && (user.dept.code == 'YW' || user.dept.code == 'ZW' || user.dept.code == 'GL'))){
            deptDiv.style.display = 'block';

            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData()],
                onNodeSelected: function (event, data) {
                    $("#deptModal").modal('hide');
                    renderDeptAndUser(data.id, data.text);//设置部门和员工
                    reflushTable();
                }
            });
            $("#cleanDept").click(function () {
                $("#currentUserId").empty();//初始化
                $("#currentUserId").append('<option value="">全部</option>');
                $("#currentDeptId").val("");
                $("#deptName").val("");
                layui.use('form', function(){
                    layui.form.render('select');//layui重新渲染下拉列表
                });
                reflushTable();
            });
        }
    },
    //加载此部门下的业务员
    loadWorker: function(deptId,roleType){
        deptId = deptId || "";
        var ele = $("#currentUserId");
        ele.empty();
        ele.append('<option value="">全部</option>');
        //如果没有部门权限 和 公司权限，则只加载当前用户
        if(!user.currentDeptQx && !user.currentCompanyQx && !isZW() && !isXZ()){
            ele.append("<option value="+user.id+" selected>"+user.name+"</option>");
        }else {
            if(roleType){
                searchForm.loadDeptUser(deptId,roleType,"currentUserId");
            }
        }
    },
    loadDeptUser: function (deptId, roleType, attr) {
        var attribute = attr || 'users';
        layui.use(['form'], function () {
            Views.layuiForm = layui.form;
            var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
            var loadUserUrl = "/user/listUserByDeptAndRole2"; //会获取指定部门下所有子部门人员
            //如果是政委中心，则只能加载他管理部门的用户
            if(user.dept.code == "ZW"){
                loadUserUrl = "/deptZw/listZwUserByDeptAndRole"; //获取子部门及其子部门人员（排除非自己管理的子部门）
            }
            $.ajax({
                    url: baseUrl + loadUserUrl,
                    type: "post",
                    data: {deptId: deptId, roleType: roleType, deptCode: "YW"},
                    async: true,
                    dataType: "json",
                    success: function (users) {
                        var userList = users.data.list;
                        if(userList && userList.length > 0){
                            for (var i = 0; i < userList.length; i++) {
                                ele.append("<option value=" + userList[i].id + ">" + userList[i].name + "</option>");
                            }
                            Views.layuiForm.render();
                        }
                        layui.use('form', function(){
                            layui.form.render('select');//layui重新渲染下拉列表
                        });
                    }
                }
            );
        });
    },
    showDate: function(val){
        if(val == 3){
            $("#dateFormGroup").show();
        }else{
            $("#dateFormGroup").hide();
            reflushTable();
        }
    },
    renderGroup: function () {
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许行政 业务
        var param = {};
        if(!(isXZ() || ((currentDeptQx || currentCompanyQx || isZW()) && (user.dept.code == 'YW' || user.dept.code == 'GL')))){
            param.userId = user.id;
        }
        requestData(param,"/userPlan/listUserGroupByParam","post","json",false,function (data) {
            var ele = $("#userGroup");
            ele.empty();
            ele.append('<option value="">全部</option>');
            if(data && data.length > 0){
                $.each(data, function (index, group) {
                    ele.append("<option value="+group.id+">"+group.name+"</option>");
                })
            }
        });
    }
};

//判断当前用户是否业务政务
var isZW = function () {
    var roles = user.roles;//获取用户角色
    var isZW = false;//是否媒介政务
    if(roles){
        for(var i=0; i < roles.length; i++){
            if(roles[i].code == 'ZW' && roles[i].type == 'YW'){
                isZW = true;
                break;
            }
        }
    }
    return isZW;
}

//判断是否行政
var isXZ = function () {
    var roles = user.roles;//获取用户角色
    var isXZ = false;//是否媒介政务
    if(roles){
        for(var i=0; i < roles.length; i++){
            if(roles[i].type == 'XZ'){
                isXZ = true;
                break;
            }
        }
    }
    return isXZ;
}

//获取部门树数据
function getTreeData() {
    var deptTreeData = {};
    var deptId = user.dept.id;//当前用户部门ID
    var deptCode = user.dept.code;//当前部门编码
    var deptName = user.dept.name;//当前部门名称
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    var url = "/dept/listAllDeptByIdAndCode";
    if(deptCode == "ZW"){
        url = "/deptZw/listDeptTreeByZw"; //查询政委管理的部门
    }else if(deptCompanyCode == "JT" && (isXZ() || deptCode == "GL" || user.currentCompanyQx)){
        requestData(null,"/dept/getRootDept","POST","json",false,function (result) {
            var root = result.data.root;
            if (root){
                deptId = root.id;//整个集团的业务和媒介部
                deptName = root.name;
            }else{
                deptId = 517;//整个集团的业务和媒介部
                deptName = "集团";
            }
        });
    }else if(isXZ() || deptCode == "GL" || user.currentCompanyQx){
        requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST","json",false,function (result) {
            var company = result.data.company;
            if (company){
                deptId = company.id;//整个集团的业务和媒介部
                deptName = company.name;
            }
        });
    }

    //具体查询
    requestData({deptId: deptId,deptCode:'YW'},url,"POST","json",false,function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });

    //政委展示管理的部门
    if(deptCode == "ZW"){
        renderDeptAndUser(deptTreeData.id, deptTreeData.text);
    }else {
        //非普通业务人员默认展示公司
        renderDeptAndUser(deptId,deptName);//设置部门和员工
    }
    return deptTreeData;
}

//设置部门和员工
function renderDeptAndUser(deptId,deptName) {
    $("#currentDeptId").val(deptId);
    $("#deptName").val(deptName);
    searchForm.loadWorker(deptId,"YW"); //查询业务员工
    layui.use('form', function(){
        layui.form.render('select');//layui重新渲染下拉列表
    });
}

function createTable() {
    var $planTable = $("#planTable");
    $planTable.jqGrid({
        url: baseUrl + '/userPlan/listNotEnterPlanUserByParam',
        datatype: "json",
        postData: $("#queryForm").serializeJson(),
        mtype: 'post',
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: true,
        setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 50, //每页记录数
        rowList: [10,20, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colNames: ['时间','所属公司','所属部门','姓名'],//表头
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'dayTime',
                label: '时间',
                width: 100,
                editable: true,
                sortable: true,
            },
            {
                name: 'companyCodeName',
                label: '所属公司',
                width: 100,
                editable: true,
                sortable: true,
            },
            {
                name: 'deptName',
                label: '所属部门',
                width: 100,
                editable: true,
                sortable: true,
            },
            {
                name: 'userName',
                label: '姓名',
                width: 100,
                editable: true,
                sortable: true
            }
        ],
        pager: "#planTablePaper",
        viewrecords: true,
        caption: getTableTitle(),
        hidegrid: false,
        gridComplete: function () {
            var width = $('#planTable').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#planTable').setGridWidth(width);
        }
    });
    $("#planTable").jqGrid('setGroupHeaders', {
        useColSpanStyle: true,
        groupHeaders: [
            { startColumnName: 'perfoSummary', numberOfColumns: 6, titleText: '昨日总结' },
            { startColumnName: 'perfoPlan', numberOfColumns: 6, titleText: '今天计划' }
        ]
    });
    $planTable.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
}

//刷新表格
function reflushTable() {
    //刷新表格
    $("#planTable").emptyGridParam(); //清空历史查询数据
    $("#planTable").jqGrid("setCaption",getTableTitle()); //设置表格标题
    $("#planTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入

}

//获取表格标题
function getTableTitle() {
    var timeType = $("#timeQuantum").val();
    var result = "未填写计划总结（"+new Date().format("yyyy-MM-dd")+"）";
    if(timeType){
        if(timeType == 1){
            result = "未填写计划总结（本周）";
        }else if(timeType == 2){
            result = "未填写计划总结（本月）";
        }else {
            var startDate = $("#startDate").val();
            var endDate = $("#endDate").val();
            if(startDate && endDate){
                result = "未填写计划总结（"+startDate+" - "+endDate+"）";
            }else if (startDate) {
                result = "未填写计划总结（自"+startDate+"起）";
            }else if (endDate){
                result = "未填写计划总结（截止"+endDate+"）";
            }
        }
    }
    return result;
}