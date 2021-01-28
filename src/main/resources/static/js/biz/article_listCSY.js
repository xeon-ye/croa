var deptId = user.dept.id;//当前用户部门ID;
var deptName = user.dept.name;//当前部门名称
var Business = {
    //如果是财务、管理等岗位获取公司或集团的id
    getDeptId:function(){
        var deptCode = user.dept.code;//当前部门编码
        var deptCompanyCode = user.dept.companyCode;//部门公司代码
        if(deptCompanyCode == "JT" && (deptCode == "CW" || user.currentCompanyQx || deptCode == "GL")){
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
        }else if(deptCode == "CW" || user.currentCompanyQx || deptCode == "GL"){
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
            var params =  removeBlank($("#searchForm").serializeJson());
            params["right"] = 1;
            location.href = "/article/exportArticleCSY?" + $.param(params);
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

    //设置统计数据
    setArticleResult: function () {
        $("#tj").find(".text-danger").html(0);
        $.ajax({
            url: baseUrl + "/article/articleResultCSY",
            data: $("#searchForm").serializeJson(),
            type: "post",
            dataType: "json",
            success: function (resData) {
                if (resData) {
                    for (var o in resData) {
                        $("#tj #" + o).text(resData[o] == "" ? 0 : resData[o].toMoney());
                    }
                }
            }
        });
    },
};

$(document).ready(function () {
    var companyName = getQueryString("companyName");
    var custName = getQueryString("custName");
    var incomeState = getQueryString("incomeState");
    var completeStatus = getQueryString("completeStatus");
    if(custName !=null && custName !="" && custName != undefined){
        $("#searchForm [name='completeStatus']").val(completeStatus);
        $("#searchForm [name='incomeStates']").val(incomeState);
        $("#searchForm [name='companyName']").val(companyName);
        $("#searchForm [name='dockingPeopleName']").val(custName);
    }
    $("#searchForm select[name='incomeStates']").change(function () {
        $("#searchButton").trigger("click") ;
    });
    $("#searchForm select[name='invoiceStates']").change(function () {
        $("#searchButton").trigger("click") ;
    });
    $("#searchForm select[name='outgoStates']").change(function () {
        $("#searchButton").trigger("click") ;
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
    Business.getDeptId();
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
});

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

var gridObject = {
    url: baseUrl + '/article/articleListCSY',
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
            name: 'deptName',
            index: 'deptName',
            label: '业务部门',
            editable: false,
            width: 100,
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
            sortable: false
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
            name: 'companyName',
            index: 'companyName',
            label: '客户公司',
            editable: false,
            width: 115,
            align: "center",
            sortable: false,
            cellattr: function (rowId, tv, rawObject, cm, rdata) {
                return "id='companyname" + rowId + "'";
            }
        },
        {
            name: 'dockingName',
            index: 'dockingName',
            label: '对接人',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            cellattr: function (rowId, tv, rawObject, cm, rdata) {
                return "id='dockingname" + rowId + "'";
            },
            hidden: false
        },
        {
            name: 'brand',
            index: 'brand',
            label: '品牌',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false
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
            name: 'promiseDate',
            index: 'promiseDate',
            label: '客户答应到款日期',
            editable: false,
            width: 120,
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
            name: 'mediaUserName',
            index: 'mediaUserName',
            label: '媒介',
            editable: false,
            width: 70,
            align: "center",
            sortable: false
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
            name: 'mTypeName',
            index: 'mTypeName',
            label: '媒体板块',
            editable: false,
            width: 70,
            align: "center",
            sortable: false
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
            name: 'mediaName',
            index: 'mediaName',
            label: '媒体名称',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            hidden: false
        },
        {
            name: 'electricityBusinesses',
            index: 'electricityBusinesses',
            label: '电商商家',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            // hidden: According()
        },
        {
            name: 'channel',
            index: 'channel',
            label: '频道',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            // hidden: According()
        },
        {
            name: 'innerOuter',
            index: 'innerOuter',
            label: '内外部',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            // hidden: According()
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
        {name: 'title', label: '标题', editable: true, width: 160,
            formatter: function (v, options, row) {
                if (v==undefined ||v==null || v == "") {
                    return "";
                } else {
                    var link = row.link ;
                    if(!(link==undefined || link==null || link=="")){
                        var str = link.substring(0, 4).toLowerCase();
                        if (str == "http") {
                            return "<a href='" + link + "' target='_blank'>" + v + "</a>";
                        } else {
                            return "<a href='//" + link + "' target='_blank'>" + v + "</a>";
                        }
                    }else{
                        return v ;
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
            label: '回款（进账）',
            editable: false,
            width: 100,
            align: "center",
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
            sortable: false
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
                    html = "已开票";
                } else if (rowdata.invoiceStates == 2) {
                    html = "开票中";
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
            name: 'remark',
            index: 'remark',
            label: '备注',
            editable: false,
            width: 130,
            align: "center",
            sortable: false,
            hidden: false
        },
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
        grid.mergerCell('amount', primaryKey);
        grid.mergerCell('state', primaryKey);
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