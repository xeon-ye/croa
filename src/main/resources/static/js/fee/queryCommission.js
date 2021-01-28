var deptId = user.dept.id;//当前用户部门ID
var deptCode = user.dept.code;//当前部门编码
var deptCompanyCode = user.dept.companyCode;//部门公司代码
var searchForm = {
    init:function () {
        searchForm.getDeptId();
        searchForm.loadDept();
        searchForm.loadWorker(deptId, "YW");
    },
    //如果是财务、管理等岗位获取公司或集团的id
    getDeptId: function () {
        if (deptCompanyCode == "JT" &&
            (deptCode == "CW" || deptCode == "RS" || deptCode == "XZ" || user.currentCompanyQx || deptCode == "GL")) {
            requestData(null, "/dept/getRootDept", "POST", function (result) {
                var root = result.data.root;
                if (root) {
                    deptId = root.id;//整个集团的业务部
                    $("#deptName").val(root.name);
                }
            });
        } else if (deptCode == "CW" || deptCode == "RS" || deptCode == "XZ" || user.currentCompanyQx || deptCode == "GL") {
            requestData({companyCode: deptCompanyCode}, "/dept/getCompanyByCode", "POST", function (result) {
                var company = result.data.company;
                if (company) {
                    deptId = company.id;//整个公司的业务部
                    $("#deptName").val(company.name);
                }
            });
        } else {
            $("#deptName").val(user.dept.name);
        }
        return deptId;
    },
    loadDept:function(){
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        var deptDiv = document.getElementById("deptDiv");
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
        if(deptCode == "CW" || deptCode == "RS" || deptCode == "XZ" || deptCode == "GL" || currentDeptQx || currentCompanyQx){
            deptDiv.style.display = 'block';

            $("#deptId").val(deptId);
            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData()],
                onNodeSelected: function (event, data) {
                    $("#deptId").val(data.id);
                    $("#chooseDeptName").val(data.text);
                    $("#deptModal").modal('hide');
                    searchForm.loadWorker(data.id,data.code);
                }
            });
            $("#cleanDept").click(function () {
                $("#userId").empty();//初始化
                $("#userId").append('<option value="">业务员</option>');
                $("#deptId").val(deptId);
                $("#chooseDeptName").val(user.dept.name);
                searchForm.loadWorker(deptId, "YW");
            });
        }
    },
    //加载此部门下的业务员
    loadWorker: function(deptId,roleType){
        var ele = $("#userId");
        ele.empty();
        //如果没有部门权限 和 公司权限 并且不是财务，则只加载当前用户
        if (hasRoleYW() && !user.currentDeptQx && !user.currentCompanyQx) {
            ele.append("<option value=" + user.id + ">" + user.name + "</option>");
            layui.use('form', function() {
                layui.form.render();
            });
        } else {
            ele.append('<option value="">业务员</option>');
            if (roleType) {
                searchForm.loadDeptUser(deptId, roleType, "userId");
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
                    async: false,
                    dataType: "json",
                    success: function (users) {
                        var userList = users.data.list;
                        if(userList && userList.length > 0){
                            for (var i = 0; i < userList.length; i++) {
                                ele.append("<option value=" + userList[i].id + ">" + userList[i].name + "</option>");
                                layui.form.render();
                            }
                        }
                    }
                }
            );
        });
    }
};

//获取部门树数据
function getTreeData() {
    var deptTreeData = {};
    //具体查询
    requestData({deptId: deptId, deptCode: "YW"}, "/dept/listAllDeptByIdAndCode", "POST", function (result) {
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

function view(id) {
    $("#articleModal").modal({backdrop: "static"});
    $.ajax({
        type: "get",
        url: baseUrl+"/commission/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            emptySelected();
            for (var attr in data.data.entity) {
                $("#commForm [name=" + attr + "]").val(data.data.entity[attr]);

                if(attr=="userId"){
                    $("#userId2").val(data.data.entity[attr]) ;
                }
                if(attr=="year"){
                    $("#year2").val(data.data.entity[attr]) ;
                }
                if(attr=="month"){
                    $("#month2").val(data.data.entity[attr]) ;
                }
            }
            reloadSelected();
        }
    });

}

/**
 * 财务批量确认提成
 */
function confirmOprate(){
    var lock = true ;
    var ids = $("#query_table_logs").jqGrid("getGridParam", "selarrrow");
    if(ids && ids.length>0){
        layer.confirm("请确认批量提成",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            startModal("#confirmAll");
            if(lock){
                lock = false ;
                $.ajax({
                    url:"/commission/confirmOprate",
                    method:"post",
                    data:{ids:ids},
                    dataType:"json",
                    success:function (data) {
                        Ladda.stopAll();
                        if(data.code == 200){
                            layer.msg(data.data.message,{time:1000,icon:6});
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/commission/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        }else{
                            if (getResCode(data))
                                return ;
                        }
                    },
                    error: function () {
                        Ladda.stopAll();//隐藏加载按钮
                    }
                });
            }
        },function () {
            return;
        })
    }else{
        swal("请选中需发放的提成");
    }
}

function confirm(id) {
    var lock = true ;
    layer.confirm('把提成数据发送给业务员确认？', {
        btn: ['发送', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $("#query_table_logs").emptyGridParam() ;
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl+"/commission/confirm",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    layer.close(index);
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/commission/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else {
                        if (getResCode(data))
                            return ;
                    }
                }
            });
        }
    }, function () {
        return ;
    });
}

function pass(id) {
    var lock = true  ;
    layer.confirm('您已确定提成数据没有问题？', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $("#query_table_logs").emptyGridParam() ;
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl+"/commission/pass",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    layer.close(index);
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/commission/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else {
                        if (getResCode(data))
                            return ;
                    }
                }
            });
        }
    }, function () {
        return ;
    });
}
function reject(id) {
    var lock = true ;
    layer.confirm('提成数据有问题？退回财务复核！', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $("#query_table_logs").emptyGridParam() ;
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl+"/commission/reject",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    layer.close(index);
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/commission/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else {
                        if (getResCode(data))
                            return ;
                    }
                }
            });
        }
    }, function () {
        return ;
    });
}
function release(id) {
    var lock = true ;
    layer.confirm('发放提成？', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $("#query_table_logs").emptyGridParam() ;
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl+"/commission/release",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    layer.close(index);
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/commission/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else {
                        if (getResCode(data))
                            return ;
                    }
                }
            });
        }
    }, function () {
        return ;
    });
}

$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';

    if(getQueryString("id")!=null&&getQueryString("id")!=""&&getQueryString("id")!=undefined){
        view(getQueryString("id")) ;
    }

    if(hasRoleCWKJ()||hasRoleCWBZ()||hasRoleRS()||hasRoleXZ()){
        $("#showFowCW").show() ;
    }else{
        $("#showFowCW").hide() ;
    }

    searchForm.init();//初始化部门树

    $("#query_table_logs").jqGrid({
        url: baseUrl+'/commission/listPg',
        datatype: "json",
        mtype: 'POST',
        postData: $("#queryForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: true,
        // setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'year', label: '年', editable: true, width: 60},
            {name: 'month', label: '月', editable: true, width: 60,},
            {name: 'id', label: 'id', editable: true,hidden:true, width: 60},
            {name: 'user_id', label: 'userId', editable: false,hidden:true, width: 60},
            {name: 'name', label: '业务员姓名', editable: true, width: 60},
            {name: 'dept_name', label: '所属部门', editable: true, width: 120},
            {name: 'sale', label: '应收', editable: true, width: 80},
            {name: 'income', label: '回款', editable: true, width: 80},
            {name: 'outgo', label: '请款', editable: true, width: 80},
            {name: 'refund', label: '退款', editable: true, width: 80},
            {name: 'other_expense', label: '其他支出', editable: true, width: 80},
            {name: 'tax_expense', label: '税额', editable: true, width: 80},
            {name: 'profit', label: '利润', editable: true, width: 80,},
            // {name: 'profitPercent', label: '利润占比', editable: true, width: 80},
            {name: 'comm', label: '提成', editable: true, width: 80},
            // {name: 'commPercent', label: '提成比例', editable: true, width: 80},
            {name: 'state', label: '状态', editable: true, width: 80,
                formatter:function (value) {
                    switch (value) {
                        case -1 :
                            return "<span style='color:red'>审核驳回</span>";
                        case 0 :
                            return "<span style=''>已保存</span>";
                        case 1 :
                            return "<span style=''>已完成</span>";
                        case 2 :
                            return "<span style='color:red'>审核通过</span>";
                        case 3 :
                            return "<span style='color:red'>组长审核</span>";
                        case 4 :
                            return "<span style='color:red'>部长审核</span>";
                        case 5 :
                            return "<span style='color:red'>总监审核</span>";
                        case 6 :
                            return "<span style='color:red'>财务总监审核</span>";
                        case 7 :
                            return "<span style='color:red'>副总经理审核</span>";
                        case 8 :
                            return "<span style='color:red'>总经理审核</span>";
                        case 9 :
                            return "<span style='color:red'>会计审核</span>";
                        case 10 :
                            return "<span style='color:red'>业务员确认</span>";
                    }
                }
            },
            {
                name: 'operate', label: "操作", index: '',
                formatter: function (value, grid, rows) {
                    var html = "" ;
                    if((hasRoleCW()||hasRoleRS()||hasRoleXZ())&&(rows.state==0||rows.state==-1)){
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='confirm(" + rows.id + ")'>发起确认&nbsp;&nbsp;</a>";
                    }
                    if(hasRoleYW()&&rows.state==10){
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red;'  onclick='pass(" + rows.id + ")'>确认通过&nbsp;&nbsp;</a>";
                    }
                    if(hasRoleYW()&&rows.state==10){
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red;'  onclick='reject(" + rows.id + ")'>驳回&nbsp;&nbsp;</a>";
                    }
                    if((hasRoleCW()||hasRoleRS()||hasRoleXZ())&&rows.state==2){
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='release(" + rows.id + ")'>发放提成</a>";
                    }
                    return html;
                },
            },
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            view(rowid);
        },
        loadComplete: function (data) {
            if (getResCode(data))
                return ;
        }
    });
    resize("#query_table_logs");

    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam() ;
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $("#addBtn").click(function () {
        if(hasRoleCWKJ()||hasRoleCWBZ()||hasRoleRS()||hasRoleXZ()){
            window.open("/fee/queryCommissionAdd") ;
        }else{
            swal("当前用户没有权限登记提成！");
            return;
        }
    });

    $("#exportAll").click(function () {
        var params =  removeBlank($("#queryForm").serializeJson()) ;
        location.href = "/commission/exportAll"+"?"+$.param(params);
    });

    //查看页面导出
    $("#exportRegister").click(function () {
        var params =  removeBlank($("#selectedArticleForm").serializeJson());
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        location.href = "/commission/exportRegisterNew?" + $.param(params);
    });

    $("#selected_article_table_logs").jqGrid({
        url: baseUrl+'/commission/queryArticleByYearAndMonth',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#selectedArticleForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: false,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'media_type_name', label: '媒体板块', editable: true, width: 60},
            {name: 'user_name', label: '业务员', editable: true, width: 60},
            {name: 'media_user_name', label: '媒介', editable: true, width: 60},
            {name: 'company_name', label: '客户公司', editable: true, width: 60},
            // {name: 'cust_name', label: '对接人', editable: true, width: 60},
            {name: 'issued_date', label: '发布日期', editable: true, width: 80,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'media_name', label: '媒体', editable: true, width: 60},
            {name: 'title', label: '标题', editable: true, width: 60},
            {name: 'link', label: '链接', editable: true, width: 60,
                formatter: function (v, options, row) {
                    return "<a style='color: #337ab7;' href='" + row.link + "' target='_blank'>" + row.link + "</a>";
                }},
            {name: 'sale_amount', label: '应收（报价）', editable: true, width: 60},
            {name: 'income_amount', label: '回款金额', editable: true, width: 60},
            {
                name: 'incomeDetail',
                index: 'incomeDetail',
                label:'回款详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var a ="";
                    if(rowdata.income_states==1){
                        var url = "javascript:void(0) onclick='queryIncomeId(" + rowdata.id + ")'";
                        a = "<a href=" + url + " style='color:#337ab7'>已回款</a>";
                    }else if(rowdata.income_states==2){
                        var url = "javascript:void(0) onclick='queryIncomeId(" + rowdata.id + ")'";
                        a = "<a href=" + url + " style='color:#337ab7'>部分回款</a>";
                    }else{
                        a = "" ;
                    }
                    return a;
                }
            },
            {name: 'outgo_amount', label: '成本（请款）', editable: true, width: 60},
            {
                name: 'OutgoDetail',
                index: 'OutgoDetail',
                label:'请款详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.outgo_states == 1) {
                        if(hasRoleCW()){
                            html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOutgoId(" + rowdata.id + ")'>已请款</a>";
                        }else{
                            html = "已请款";
                        }
                    }else if(rowdata.outgo_states==2){
                        if(hasRoleCW()){
                            html = "<a href='javascript:void(0)' style='color:red'  onclick='queryOutgoId(" + rowdata.id + ")'>请款中</a>";
                        }else{
                            html = "请款中" ;
                        }
                    }else{
                        html = "" ;
                    }
                    return html;
                }
            },
            {name: 'taxes', label: '税金', editable: true, width: 60},
            {name: 'invoice_states', label: '开票详情', editable: true, width: 60,
                formatter: function (a, b, rowdata) {
                    if(rowdata.invoice_states==1){
                        return "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryInvoiceId(" + rowdata.id + ")'>已开票</a>" ;
                    }else if(rowdata.invoice_states==2){
                        return "<a href='javascript:void(0)' style='color:red'  onclick='queryInvoiceId(" + rowdata.id + ")'>开票中</a>" ;
                    }else{
                        return "<span style=''>未开票</span>" ;
                    }
                }
            },
            {name: 'id', label: 'id', editable: true,hidden:true, width: 60},
            {name: 'user_id', label: 'userId', editable: false,hidden:true, width: 60},
            {name: 'refund_amount', label: '退款', editable: true, width: 60},
            {
                name: 'RefundDetail',
                index: 'RefundDetail',
                label:'退款详情',
                editable: false,
                width: 50,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.refund_states == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryRefundId(" + rowdata.id + ")'>已退款</a>";
                    }else if(rowdata.refund_states == 2){
                        html = "<a href='javascript:void(0)' style='color:red'  onclick='queryRefundId(" + rowdata.id + ")'>退款中</a>";
                    }else {
                        html = "";
                    }
                    return html;
                }
            },
            {name: 'other_pay', label: '其他支出', editable: true, width: 60},
            {
                name: 'otherPayDetail',
                index: 'otherPayDetail',
                label:'支出详情',
                editable: false,
                width: 50,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.other_pay_states == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOtherPayId(" + rowdata.id + ")'>已支出</a>";
                    }else if(rowdata.other_pay_states == 2){
                        html = "<a href='javascript:void(0)' style='color:red'  onclick='queryOtherPayId(" + rowdata.id + ")'>支出中</a>";
                    }else {
                        html = "";
                    }
                    return html;
                }
            },
            {name: 'profit', label: '利润', editable: true, width: 60,},
            // {name: 'profitPercent', label: '利润占比', editable: true, width: 80},
            {name: 'commission', label: '提成', editable: true, width: 60},
            ],
        pager: jQuery("#selected_article_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        loadComplete: function (data) {
            if (getResCode(data))
                return ;
        }
    });
    resize("#selected_article_pager_logs");

    $("#selectedArticleSearch").click(function () {
        emptySelected();
        reloadSelected();
    })
});
function reloadSelected() {
    $("#selected_article_table_logs").jqGrid('setGridParam', {
        datatype:"json",
        postData: $("#selectedArticleForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}
function emptySelected() {
    $("#selected_article_table_logs").emptyGridParam() ;
}

function queryIncomeId (articleId) {
    $("#incomeModal").modal({backdrop: "static"}) ;
    $("#incomeTable").jqGrid('setGridParam', {
        postData: {id:articleId}, //发送数据
    }).trigger("reloadGrid"); //重新载入
    $("#incomeTable").jqGrid({
        url: baseUrl+'/income/listPgByArticleId',
        postData: {id:articleId},
        datatype: "json",
        mtype: 'get',
        height: "auto",
        page: 1,//第一页
        shrinkToFit: false,
        autowidth: true,
        colNames: ['进账编号','账户名称','进账人','进账金额', '进账日期','分款金额','分款日期'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'code',
                index: 'code',
                editable: true,
                width: 115,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    // console.log(rowdata)
                    var link = '/fee/queryIncome?id='+rowdata.id
                    return "<a href='"+link+"' target='_blank'>" + rowdata.code + "</a>";
                    // return "/fee/queryIncome?id=" + rowdata.id;
                }
            },
            {
                name: 'account_name',
                index: 'account_name',
                editable: true,
                width: 200,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'trade_man',
                index: 'trade_man',
                editable: true,
                width: 180,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'trade_amount',
                index: 'trade_amount',
                editable: true,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'trade_time',
                index: 'trade_time',
                editable: true,
                width: 180,
                align: "center",
                sortable: false,
                formatter:function (d) {
                    if(!d){
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'amount',
                index: 'amount',
                editable: true,
                width: 180,
                align: "center",
                sortable: false
            },
            {
                name: 'date',
                index: 'date',
                editable: true,
                width: 180,
                align: "center",
                sortable: false,
                formatter:function (d) {
                    if(!d){
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            }
        ],
        // pager: "incomePager"
    });
}

function queryOutgoId(articleId) {
    $.ajax({
        type: "post",
        url: "/outgo/queryOutgoId",
        data: {articleId: articleId},
        dataType: "json",
        success: function (dataId) {
            if (dataId > 0) {
                window.open("/fee/queryOutgo?flag=1&id=" + dataId);
                // return ("/fee/queryOutgo?flag=0&id=" + dataId);
            } else {
                return;
            }
        }
    });
}

function queryInvoiceId(articleId) {
    $.ajax({
        type: "post",
        url: "/invoice/queryInvoiceId",
        data: {articleId: articleId},
        dataType: "json",
        success: function (dataId) {
            if (dataId > 0) {
                window.open("/fee/queryInvoice?flag=1&id=" + dataId);
            } else {
                return;
            }
        }
    });
}

function queryRefundId(articleId) {
    $.ajax({
        type: "post",
        url: "/refund/queryRefundId",
        data: {articleId: articleId},
        dataType: "json",
        success: function (dataId) {
            if (dataId > 0) {
                window.open("/fee/queryRefund?flag=1&id=" + dataId);
            } else {
                return;
            }
        }
    });
}

function queryOtherPayId(articleId) {
    $.ajax({
        type: "post",
        url: "/refund/queryOtherPayId",
        data: {articleId: articleId},
        dataType: "json",
        success: function (dataId) {
            if (dataId > 0) {
                window.open("/fee/queryRefund?flag=1&id=" + dataId);
            } else {
                return;
            }
        }
    });
}