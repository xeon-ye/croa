var deptId = user.dept.id;//当前用户部门ID;
var deptName = user.dept.name;//当前部门名称
var Business = {
    //如果是财务、管理等岗位获取公司或集团的id
    getDeptId:function(){
        var deptCode = user.dept.code;//当前部门编码
        var deptCompanyCode = user.dept.companyCode;//部门公司代码
        if(deptCompanyCode == "JT" && (deptCode == "CW" || deptCode == "RS" || deptCode == "GL")){
            requestData(null,"/dept/getRootDept","POST","json",false,function (result) {
                var root = result.data.root;
                if (root){
                    deptId = root.id;//整个集团的业务和媒介部
                    deptName = root.name;
                }
            });
        }else if(deptCode == "CW" || deptCode == "GL" || deptCode == "RS"){
            requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST","json",false,function (result) {
                var company = result.data.company;
                if (company){
                    deptId = company.id;//整个集团的业务和媒介部
                    deptName = company.name;
                }
            });
        }

        $("#deptId").val(deptId);
        $("#deptName").val(deptName);
        Business.loadWorker(deptId,"YW"); //查询业务员工
        return deptId ;
    },
    loadWorker: function(deptId,roleType){
        deptId = deptId || "";
        var ele = $("#userId");
        ele.empty();
        ele.append('<option value="">业务员</option>');
        if(roleType){
            Business.loadDeptUser(deptId,roleType,"userId",null);
        }
        layui.use('form', function(){
            layui.form.render('select');//layui重新渲染下拉列表
        });
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
                            }
                        }
                    }
                }
            );
        });
    },
    exportArt: function () {
        var totalList = grid.getAllPageSelected("artId");
        if (!totalList || totalList.length <= 0) {
            layer.alert("请选择要导出的数据");
            return;
        }
        var datas = [];
        for (var i = 0; i < totalList.length; i++) {
            var o = totalList[i];
            var temp = {};
            temp.id = o.artId;
            datas.push(temp);
        }
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        $("[name='datas']").val(JSON.stringify(datas));
        $("#exportForm").submit();
    },
    exportAll: function () {
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        var params =  removeBlank($("#searchForm").serializeJson());
        params["right"] = 0;
        location.href = "/article/exportArticle?" + $.param(params);
    },
    //设置统计数据
    setArticleResult: function () {
        $("#tj").find(".text-danger").html(0);
        $.ajax({
            url: baseUrl + "/article/articleResult",
            data: $("#searchForm").serializeJson(),
            type: "post",
            dataType: "json",
            success: function (resData) {
                if (resData) {
                    for (var o in resData) {
                        $("#tj #" + o).text(resData[o] == "" ? 0 : resData[o].toMoney());
                    }
                    var saleAmountSum = resData['saleAmountSum']||100 ;
                    var incomeAmountSum = resData['incomeAmountSum']||0 ;
                    var incomeRate = incomeAmountSum*100/saleAmountSum ;
                    $("#tj #incomeRate").text(incomeRate.toFixed(2));
                }
            }
        });
    }
};
//进账详情
function queryIncomeId(articleId) {
    $("#incomeModal").modal({backdrop: "static"});
    gridObj.reloadIncome(articleId);
}
//请款详情
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
//开票详情
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
//退款详情
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
//其他支出详情
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

$(document).ready(function () {
    Business.getDeptId();
    var companyName = getQueryString("companyName");
    var custName = getQueryString("custName");
    var incomeState = getQueryString("incomeState");
    var completeStatus = getQueryString("completeStatus");
    if(custName !=null && custName !="" && custName != undefined){//从未到款点击进入就不需要查看前30天这个条件
        $("#searchForm [name='completeStatus']").val(completeStatus);
        $("#searchForm [name='incomeStates']").val(incomeState);
        $("#searchForm [name='companyName']").val(companyName);
        $("#searchForm [name='dockingPeopleName']").val(custName);
        // $("#searchButton").trigger("click") ;
    }else{
        $('#issuedDateStart').val(laydate.now(-30, 'YYYY/MM/DD'));
    }

    Views.loadParentMediaType("mType");

    // 加载媒介；
    loadAllMJ("#mediaUserId");

    //加载稿件行业类型
    loadTypeCode();
    //加载客户行业类型
    loadCustCompanyCode();

    //创建表格对象
    grid = new dataGrid("table", gridObject, "pager", "searchForm");
    //加载表格
    grid.loadGrid();
    // $("#table").setGridHeight(210);
    statisticsModal.init();//初始化模态框
    $('#queryDiv').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            //回车执行查询
            $("#searchButton").click();
        }
    });
    $('#detailDiv').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            $("#detailSearch").click();
        }
    });
    $("#searchButton").click(function () {
        $("#table").emptyGridParam();
        $("#table").jqGrid('setGridParam', {
            postData: $("#searchForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $("#selDept").click(function () {
        $("#deptModal").modal('toggle');
    });

    $('#treeview').treeview({
        data: [getTreeData(deptId)],
        onNodeSelected: function (event, data) {
            $("#deptId").val(data.id);
            $("#deptName").val(data.text);
            $("#deptModal").modal('hide');
            Business.loadWorker(data.id,'YW');
        }
    });

    $("#cleanDept").click(function () {
        $("#deptId").val(deptId);
        $("#deptName").val(deptName);
    });

    $("#detail_table_logs").jqGrid({
        url: baseUrl + '/articleHistory/queryArticleChangeSingle',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#detailForm").serializeJson(), //发送数据
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
        // sortable: "false",
        // sortname: "id",
        // sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        // autoScroll: true,
        // scroll:true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['部门名称','部门id',
        //     '业绩（含税）', '回款','税金','退款', '其它支出', '成本（不含税）','利润','提成',
        // ],
        colModel: [
            {name: 'mediaTypeName', label: '媒体板块', editable: true, align: 'center', width: 80},
            {name: 'mediaName', label: '媒体名称', editable: true, align: 'center', width: 140},
            // {name: 'mediaUserName', label: '媒介', editable: true, align: 'center', width: 100},
            // {name: 'deptName', label: '业务部门', editable: true, align: 'center', width: 100},
            {name: 'userName', label: '业务员', editable: true, align: 'center', width: 80},
            {name: 'title', label: '标题', editable: true, align: 'center', width: 180},
            // {name: 'link', label: '链接', editable: true, align: 'center', width: 100},
            // {name: 'issuedDate', label: '发布日期', editable: true, hidden: false, width: 100},
            {
                name: 'alterSale', label: '业绩（含税）', editable: true, width: 100, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterIncome', label: '回款', editable: true, width: 100, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterTax', label: '税金', editable: true, width: 100, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterRefund', label: '退款', editable: true, width: 100, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterOtherPay',
                label: '其它支出',
                editable: true,
                width: 100,
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterOutgo',
                label: '成本（不含税）',
                editable: true,
                width: 100,
                classes: '',
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterProfit',
                label: '利润',
                editable: true,
                width: 100,
                classes: '',
                align: 'center',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {
                name: 'alterComm', label: '提成', editable: true, width: 100, align: 'center', formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: "."},
            },
            {name: 'createTime', label: '修改日期', editable: true, width: 150, align: 'center'},
            {name: 'editDesc', label: '修改方式', editable: true, align: 'center', width: 120}
        ],
        loadComplete: function () {
            var width = $('#detail_table_logs').parents(".jqGrid_wrapper").width();
            $('#detail_table_logs').setGridWidth(width);

            var rows = $("#detail_table_logs").jqGrid("getRowData");
            var alterSale = 0;
            var alterIncome = 0;
            var alterTax = 0;
            var alterRefund = 0;
            var alterOtherPay = 0;
            var alterOutgo = 0;
            var alterProfit = 0;
            var alterComm = 0;
            for (var i = 0, l = rows.length; i < l; i++) {
                alterSale += (rows[i].alterSale - 0);
                alterIncome += (rows[i].alterIncome - 0);
                alterTax += (rows[i].alterTax - 0);
                alterRefund += (rows[i].alterRefund - 0);
                alterOtherPay += (rows[i].alterOtherPay - 0);
                alterOutgo += (rows[i].alterOutgo - 0);
                alterProfit += (rows[i].alterProfit - 0);
                alterComm += (rows[i].alterComm - 0);
            }
            $("#detail_table_logs").jqGrid("footerData", "set", {
                title: "合计",
                alterSale, alterSale, alterIncome, alterIncome, alterTax, alterTax, alterRefund, alterRefund,
                alterOtherPay, alterOtherPay, alterOutgo, alterOutgo, alterProfit, alterProfit, alterComm, alterComm
            });
        },
        pager: jQuery("#detail_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        footerrow: true
    });

    $("#detailSearch").click(function () {
        gridObj.reloadDetail();
        aggregateAmountDetail();
    });
    // $("#exportDetailBtn").click(function () {
    //     var params = $("#detailForm").serializeJson();
    //     location.href = "/articleHistory/exportArticleChangeSingle" + "?" + $.param(params);
    // });

    $("#incomeTable").jqGrid({
        url: baseUrl + '/income/listPgByArticleId',
        // postData: {id: articleId},
        datatype: "local",
        mtype: 'get',
        height: "auto",
        page: 1,//第一页
        shrinkToFit: false,
        autowidth: true,
        colNames: ['进账编号', '账户名称', '进账人', '进账金额', '进账日期', '分款金额', '分款日期'],
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
                    var link = '/fee/queryIncome?id=' + rowdata.id
                    return "<a href='" + link + "' target='_blank'>" + rowdata.code + "</a>";
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
                formatter: function (d) {
                    if (!d) {
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
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            }
        ],
        // pager: "incomePager"
    });
});

var gridObj = {
    reloadTable: function () {
        $("#table").emptyGridParam();
        $("#table").jqGrid('setGridParam', {
            postData: $("#searchForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#table");
    },
    reloadIncome: function (articleId) {
        $("#incomeTable").emptyGridParam();
        $("#incomeTable").jqGrid('setGridParam', {
            datatype: "json",
            postData: {id: articleId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#incomeTable")
    },
    reloadDetail: function () {
        $("#detail_table_logs").emptyGridParam();
        $("#detail_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#detailForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#detail_table_logs")
    }
};

function aggregateAmountDetail() {
    $("#detailTj").find(".text-danger").html(0);
    $.ajax({
        type: "post",
        data: $("#detailForm").serializeJson(),
        url: baseUrl + "/articleHistory/queryArticleChangeSingleSum",
        dataType: "json",
        async: false,
        success: function (resDate) {
            if (resDate) {
                $("#sale").text(fmtMoneyBringUnit(resDate.sale) || 0);
                $("#income").text(fmtMoneyBringUnit(resDate.income) || 0);
                $("#tax").text(fmtMoneyBringUnit(resDate.tax) || 0);
                $("#refund").text(fmtMoneyBringUnit(resDate.refund) || 0);
                $("#otherPay").text(fmtMoneyBringUnit(resDate.otherPay) || 0);
                $("#outgo").text(fmtMoneyBringUnit(resDate.outgo) || 0);
                $("#profit").text(fmtMoneyBringUnit(resDate.profit) || 0);
                $("#comm").text(fmtMoneyBringUnit(resDate.comm) || 0);
            }
        }
    })
}

function getTreeData(deptId) {
    var deptTreeData = {};
    $.ajax({
        type: "POST",
        data: {deptId: deptId,deptCode:'YW'},
        url: baseUrl + "/dept/listAllDeptByIdAndCode",
        dataType: "json",
        async: false,
        success: function (result) {
            var arrays = result.data.list;
            if (arrays != null && arrays.length > 0)
                deptTreeData = arrays[0];
        }
    });
    return deptTreeData;
}

//统计概况弹窗
var statisticsToggle = {
    getSingleLinkHtml:function (id,value,type) { //获取单个a链接
        var html = "";
        if(id){
            value = value ? value : id;//如果value为空则展示ID
            html += "<a onclick=\"statisticsToggle.toggleModal("+id+",'"+value+"','"+type+"');\">"+value+"</a>";
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

var gridObject = {
    url: baseUrl + '/article/articleList',
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
    sortable: true,
    sortorder: 'desc',
    sortname: 'artId',
    multiselect: true,
    prmNames: {rows: "size"},
    rowNum: 10,
    rowList: [10, 20, 50, 100],
    // colNames: ['订单ID', '订单编号', '客户公司', '对接人信息',
    //     // '订单标题', '订单金额',
    //     '订单状态','品牌', '稿件ID', '类别', "业务员", "媒介员", "媒体名称","供应商","供应商对接人",  "稿件标题",  "发布日期", "链接", "数量", "应收金额", "税金", "入账金额", "客户答应到款日期",
    //     "入账详情", "请款详情", "开票详情", "退款详情", "提成状态", "开票状态", "发布状态", "价格类型", "支付金额",
    //     // "利润率",
    //     // "支付日期", "支付账号",
    //     "备注", "提成", "退款"
    //     // , "操作"
    // ],
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: false
    },
    colModel: [
        {
            name: 'id',
            index: 'id',
            editable: false,
            width: 30,
            align: "center",
            sortable: false,
            sorttype: "int",
            search: true,
            cellattr: function (rowId, tv, rawObject, cm, rdata) {
                //合并单元格
                return "id='id" + rowId + "'";
            },
            hidden: true
        },
        {
            name: 'createTime',
            index: 'createTime',
            label: '创建时间',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            hidden: false,
            formatter: function (d) {
                if (!d) {
                    return "";
                }
                return new Date(d).format("yyyy-MM-dd");
            }
        },
        {
            name: 'deptName',
            index: 'deptName',
            label: '业务部门',
            editable: false,
            width: 70,
            align: "center",
            sortable: false
        },
        {
            name: 'userId',
            index: 'userId',
            editable: false,
            width: 90,
            align: "center",
            sortable: false,
            sorttype: "string",
            hidden:true
        },
        {
            name: 'userName',
            index: 'userName',
            label: '业务员',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            formatter: function (value, grid, rows) {
                return statisticsToggle.getSingleLinkHtml(rows.userId,rows.userName,"business");
            }
        },
        {
            name: 'mediaUserId',
            index: 'mediaUserId',
            editable: false,
            width: 90,
            align: "center",
            sortable: false,
            sorttype: "string",
            hidden:true
        },
        {
            name: 'mediaUserName',
            index: 'mediaUserName',
            label: '媒介',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            formatter: function (value, grid, rows) {
                return statisticsToggle.getSingleLinkHtml(rows.mediaUserId,rows.mediaUserName,"mediaUser");
            }
        },
        {
            name: 'dockingId',
            index: 'dockingId',
            editable: false,
            width: 90,
            align: "center",
            sortable: false,
            sorttype: "string",
            hidden:true
        },
        {
            name: 'dockingName',
            index: 'dockingName',
            label: '客户对接人',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            cellattr: function (rowId, tv, rawObject, cm, rdata) {
                return "id='dockingname" + rowId + "'";
            },
            formatter: function (value, grid, rows) {
                return statisticsToggle.getSingleLinkHtml(rows.dockingId,rows.dockingName,"cust");
            },
            hidden: false
        },
        {
            name: 'brand',
            index: 'brand',
            label: '品牌',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'companyName',
            index: 'companyName',
            label: '客户公司',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            cellattr: function (rowId, tv, rawObject, cm, rdata) {
                return "id='companyname" + rowId + "'";
            }
        },
        {
            name: 'custCompanyType',
            index: 'custCompanyType',
            label: '公司类型',
            editable: false,
            width: 100,
            align: "center",
            sortable: false
        },
        {
            name: 'artId',
            index: 'artId',
            label: '稿件ID',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            sorttype: "string",
            hidden: true
        },
        {
            name: 'mTypeId',
            index: 'mTypeId',
            editable: false,
            width: 90,
            align: "center",
            sortable: false,
            sorttype: "string",
            hidden:true
        },
        {
            name: 'mediaId',
            index: 'mediaId',
            editable: false,
            width: 90,
            align: "center",
            sortable: false,
            sorttype: "string",
            hidden:true
        },
        {
            name: 'mTypeName',
            index: 'mTypeName',
            label: '媒体板块',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            formatter: function (value, grid, rows) {
                return statisticsToggle.getSingleLinkHtml(rows.mTypeId,rows.mTypeName,"mediaType");
            }
        },
        {
            name: 'mediaName',
            index: 'mediaName',
            label: '媒体名称',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false,
            formatter: function (value, grid, rows) {
                return statisticsToggle.getSingleLinkHtml(rows.mediaId,rows.mediaName,"media");
            }
        },
        {
            name: 'title',
            index: 'title',
            label: '稿件标题',
            editable: false,
            width: 120,
            align: "center",
            sortable: false,
            formatter: function (d) {
                var html = "";
                if (d) {
                    if (d && d.length > 15) {
                        var text = d.substring(0, 15);
                        html = "<span title='" + d + "'>" + text + "...</span>";
                    } else {
                        html = d;
                    }

                }
                return html;
            }

        },
        {
            name: 'link',
            index: 'link',
            label: '链接',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            formatter: function (v, options, row) {
                if (!v) {
                    return "";
                } else {
                    var str = row.link.substring(0, 4).toLowerCase();
                    if (str == "http") {
                        return "<a href='" + row.link + "' target='_blank'>" + row.link + "</a>";
                    } else {
                        return "<a href='//" + row.link + "' target='_blank'>" + row.link + "</a>";
                    }
                }

            }
        },
        {
            name: 'typeName',
            index: 'typeName',
            label: '稿件行业类型',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'issuedDate',
            index: 'issuedDate',
            label: '发布日期',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false,
            formatter: function (d) {
                if (!d) {
                    return "";
                }
                return new Date(d).format("yyyy-MM-dd");
            }
        },
        {
            name: 'num',
            index: 'num',
            label: '数量',
            editable: false,
            width: 50,
            align: "center",
            sortable: false
        },
        {
            name: 'saleAmount',
            index: 'saleAmount',
            label: '应收（报价）',
            editable: false,
            width: 100,
            align: "center",
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            sortable: false
        },
        {
            name: 'incomeAmount',
            index: 'incomeAmount',
            label: '回款金额',
            editable: false,
            width: 100,
            align: "center",
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            sortable: false
        },
        {
            name: 'incomeDetail',
            index: 'incomeDetail',
            label: '回款详情',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var a = "";
                if (rowdata.incomeStates == 1) {
                    var url = "javascript:void(0) onclick='queryIncomeId(" + rowdata.artId + ")'";
                    a = "<a href=" + url + " style='color:#337ab7'>已回款</a>";
                } else if (rowdata.incomeStates == 2) {
                    var url = "javascript:void(0) onclick='queryIncomeId(" + rowdata.artId + ")'";
                    a = "<a href=" + url + " style='color:#337ab7'>部分回款</a>";
                } else {
                    a = "";
                }
                return a;
            }
        },
        {
            name: 'messState',
            index: 'messState',
            label: '烂账详情',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var a = "";
                if (rowdata.messState == 1) {
                    a = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='accountMess(" + rowdata.artId + ")'>已烂账</a>";
                } else if (rowdata.messState == 2) {
                    a = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='accountMess(" + rowdata.artId + ")'>烂账中</a>";
                } else {
                    a = "";
                }
                return a;
            }
        },
        {
            name: 'promiseDate',
            index: 'promiseDate',
            label: '客户答应到款日期',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            formatter: function (d) {
                if (!d) {
                    return "";
                }
                return new Date(d).format("yyyy-MM-dd");
            }
        },
        {
            name: 'incomeCode',
            index: 'incomeCode',
            label: '进账编号',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'incomeAccount',
            index: 'incomeAccount',
            label: '进账账号',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'incomeMan',
            index: 'incomeMan',
            label: '进账人',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'incomeDate',
            index: 'incomeDate',
            label: '进账时间',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false,
            formatter: function (d) {
                if (!d) {
                    return "";
                }
                return new Date(d).format("yyyy-MM-dd");
            }
        },
        {
            name: 'incomeTotalAmount',
            index: 'incomeTotalAmount',
            label: '进账金额合计',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            hidden: false
        },
        {
            name: 'assignDate',
            index: 'assignDate',
            label: '分款时间',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false,
            formatter: function (d) {
                if (!d) {
                    return "";
                }
                return new Date(d).format("yyyy-MM-dd");
            }
        },
        {
            name: 'taxes',
            index: 'taxes',
            label: '税金',
            editable: false,
            width: 80,
            align: "center",
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            sortable: false
        },
        {
            name: 'InvoiceDetail',
            index: 'InvoiceDetail',
            label: '开票详情',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var html = "";
                if (rowdata.invoiceStates == 1) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryInvoiceId(" + rowdata.artId + ")'>已开票</a>";
                } else if (rowdata.invoiceStates == 2) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryInvoiceId(" + rowdata.artId + ")'>开票中</a>";
                } else {
                    html = "";
                }
                return html;
            }
        },
        {
            name: 'refundAmount',
            index: 'refundAmount',
            label: '退款金额',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            hidden: false
        },
        {
            name: 'RefundDetail',
            index: 'RefundDetail',
            label: '退款详情',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var html = "";
                if (rowdata.refundStates == 1) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryRefundId(" + rowdata.artId + ")'>已退款</a>";
                } else if (rowdata.refundStates == 2) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryRefundId(" + rowdata.artId + ")'>退款中</a>";
                } else {
                    html = "";
                }
                return html;
            }
        },
        {
            name: 'otherPay',
            index: 'otherPay',
            label: '其它支出',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            hidden: false
        },
        {
            name: 'otherPayDetail',
            index: 'otherPayDetail',
            label: '其他支出详情',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var html = "";
                if (rowdata.otherPayStates == 1) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOtherPayId(" + rowdata.artId + ")'>已支出</a>";
                } else if (rowdata.otherPayStates == 2) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOtherPayId(" + rowdata.artId + ")'>进行中</a>";
                } else {
                    html = "";
                }
                return html;
            }
        },
        {
            name: 'outgoAmount',
            index: 'outgoAmount',
            label: '成本（请款）',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            hidden: false
        },
        {
            name: 'outgoCode',
            index: 'outgoCode',
            label: '请款编号',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'outgoTotalAmount',
            index: 'outgoTotalAmount',
            label: '请款金额合计',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            hidden: false
        },
        {
            name: 'profit',
            index: 'profit',
            label: '利润',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            classes: 'text-danger',
            formatter: "currency",
            hidden: false
        },
        {
            name: 'commission',
            index: 'commission',
            label: '提成',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            classes: 'text-danger',
            formatter: "currency",
            hidden: false
        },
        {
            name: 'year',
            index: 'year',
            label: '提成年',
            editable: false,
            width: 60,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'month',
            index: 'month',
            label: '提成月',
            editable: false,
            width: 60,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'commissionStates',
            index: 'commissionStates',
            label: '提成状态',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: true,
            formatter: function (a, b, rowdata) {
                var html = "";
                if (rowdata.commission == 1) {
                    html = "已提成";
                } else if (rowdata.commission == 2) {
                    html = "提成中";
                } else {
                    html = "未提成";
                }
                return html;
            }
        },
        {
            name: 'supplierName',
            index: 'supplierName',
            label: '供应商名称',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'supplierContactor',
            index: 'supplierContactor',
            label: '供应商对接人',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'supplierPhone',
            index: 'supplierPhone',
            label: '供应商电话',
            editable: false,
            width: 120,
            align: "center",
            sortable: false,
            hidden: false,
            formatter: function (value, grid, rows) {
                if(rows){
                    var value = rows.supplierPhone;
                    if(value){
                        if (value.length >= 11) {
                            var start = value.length > 11 ? "*****" : "****";
                            return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                        } else if (value.length >= 3) {
                            return value[0] + "***" + value[value.length - 1];
                        } else {
                            return "**";
                        }
                    }else{
                        return "";
                    }
                }else{
                    return "";
                }
            }
        },
        {
            name: 'issueStates',
            index: 'issueStates',
            label: '发布状态',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false,
            formatter: function (value) {
                switch (value) {
                    case 0 :
                        return "未下单";
                    case 1 :
                        return "已下单";
                    case 2 :
                        return "进行中";
                    case 3 :
                        return "已驳回";
                    case 4 :
                        return "已发布";
                }
            }
        },
        {
            name: 'payAmount',
            index: 'payAmount',
            label: '应付金额',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            hidden: true
        },
        {
            name: 'OutgoDetail',
            index: 'OutgoDetail',
            label: '请款详情',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var html = "";
                if (rowdata.outgoStates == 1) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOutgoId(" + rowdata.artId + ")'>已请款</a>";
                } else if (rowdata.outgoStates == 2) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOutgoId(" + rowdata.artId + ")'>请款中</a>";
                } else {
                    html = "";
                }
                return html;
            }
        },
        /*{
            name: 'projectCode',
            index: 'projectCode',
            label: '项目编号',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            hidden: false,
            formatter: function (a, b, rowdata) {
                if (rowdata.projectId == "" || rowdata.projectId == null ||  rowdata.projectId == undefined) {
                    return "";
                }else{
                    return "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryProjectId(" + rowdata.projectId + ")'>"+rowdata.projectCode+"</a>";
                }
            }
        },
        {
            name: 'projectName',
            index: 'projectName',
            label: '项目名称',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            hidden: false
        },*/
        {
            name: 'electricityBusinesses',
            index: 'electricityBusinesses',
            label: '电商商家',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'channel',
            index: 'channel',
            label: '频道',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'innerOuter',
            index: 'innerOuter',
            label: '内外部',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'remarks',
            index: 'remarks',
            label: '备注',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'operator',
            index: 'operator',
            label: '异动详情',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false,
            formatter: function (value, grid, rows) {
                var html = "";
                if (rows.alterFlag==1) {
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue'  onclick='detail(" + rows.artId + ")'>异动详情&nbsp;&nbsp;</a>";
                }
                return html;
            }
        }
    ],
    /**
     * 翻页时保存当前页面的选中数据
     * @param pageBtn
     */
    onPaging: function (pageBtn) {
        //跨页面选择
        grid.setPageSelected("artId");
    },
    loadComplete: function () {
        var primaryKey = "id";
        grid.mergerCell('id', primaryKey);
        grid.mergerCell('companyname', primaryKey);
        grid.mergerCell('dockingname', primaryKey);
        //跨页面选择
        grid.getPageSelectedSet("artId");

        Business.setArticleResult();
    },
    pager: "#pager",
    viewrecords: true,
    caption: "",
    add: false,
    edit: false,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false,
    shrinkToFit: false,
    autoScroll: true
};
Number.prototype.toMoney = function () {
    var num = this;
    num = num.toFixed(2);
    num = parseFloat(num);
    num = num.toLocaleString();
    return num;//返回的是字符串23,245.12保留2位小数
}
var requestData = function (data, url, requestType,dataType,async,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun
    });
};

function detail(artId) {
    $("#artId").val(artId);
    $("#detailSearch").trigger("click");
    $("#detailModal").modal({backdrop: "static"});
}

function accountMess(articleId){
    $.ajax({
        type:"post",
        url:"/accountMess/selectMessId",
        data:{id:articleId},
        dataType:"json",
        success:function (data) {
            if (data.code==200){
                window.open("/accountsMess/accountsMess?flag=4&id=" + data.data.messId);
            }else if (data.code== 1002){
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
            } else {
                if (getResCode(data))
                    return;
            }

        }
    })
}