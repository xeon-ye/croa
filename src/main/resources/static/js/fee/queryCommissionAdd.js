var deptId = user.dept.id;//当前用户部门ID
var deptCode = user.dept.code;//当前部门编码
var deptCompanyCode = user.dept.companyCode;//部门公司代码
var searchForm = {
    init: function () {
        searchForm.getDeptId();
        searchForm.loadDept();
        searchForm.loadWorker(deptId, "YW");
    },
    //如果是财务、管理等岗位获取公司或集团的id
    getDeptId: function () {
        var deptCode = user.dept.code;//当前部门编码
        var deptCompanyCode = user.dept.companyCode;//部门公司代码
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
    loadDept: function () {
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务 人事行政
        if (deptCode == "CW" || deptCode == "RS" || deptCode == "XZ" || deptCode == "GL" || currentDeptQx || currentCompanyQx) {
            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData()],
                onNodeSelected: function (event, data) {
                    emptySelect();
                    emptySelected();
                    layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
                    $("#businessUserId").empty();//每次选择时，先清空
                    $("#userId1").val("");//每次选择时，先清空
                    $("#userId2").val("");//每次选择时，先清空
                    $("#deptId").val(data.id);
                    $("#deptId1").val(data.id);
                    $("#deptId2").val(data.id);
                    $("#chooseDeptName").val(data.text);
                    searchForm.loadWorker(data.id, data.code);
                    $("#deptModal").modal('hide');
                    reloadSelect();
                }
            });
            $("#cleanDept").click(function () {
                emptySelect();
                emptySelected();
                layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
                $("#businessUserId").empty();//初始化
                $("#userId1").val("");
                $("#userId2").val("");
                $("#deptId").val("");
                $("#deptId1").val("");
                $("#deptId2").val("");
                $("#chooseDeptName").val("");
                searchForm.loadWorker(deptId, "YW");
                reloadSelect();
            });
        }
    },
    //加载业务员
    loadWorker: function (deptId, roleType) {
        var ele = $("#businessUserId");
        ele.empty();
        ele.append('<option value="">业务员</option>');
        //如果没有部门权限 和 公司权限 并且不是财务，则只加载当前用户
        if (hasRoleYW() && !user.currentDeptQx && !user.currentCompanyQx) {
            ele.append("<option value=" + user.id + ">" + user.name + "</option>");
        } else {
            if (roleType) {
                searchForm.loadDeptUser(deptId, roleType, "businessUserId");
            }
        }
    },
    //根据部门加载业务员
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
                        if (userList && userList.length > 0) {
                            for (var i = 0; i < userList.length; i++) {
                                ele.append("<option value=" + userList[i].id + ">" + userList[i].name + "</option>");
                            }
                        }
                        layui.form.render();
                        // 下拉框的onchange事件；
                        layui.form.on("select(users)", function (userData) {
                            $("#userId1").val(userData.value);
                            $("#userId2").val(userData.value);
                            initComm(userData.value);
                        });
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
//加载所有的业务员
// function laodYWInputSelect() {
//     //加载业务员
//     layui.use(["form"], function () {
//         $.get(baseUrl + "/user/listByTypeAndCompanyCode/YW", function (data) {
//             console.log("所有的业务员="+data)
//             layui.form.render();
//             // 下拉框的onchange事件；
//             layui.form.on("select(users)", function (userData) {
//                 layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
//                 $("#userId").val(userData.value);
//                 $("#userId1").val(userData.value);
//                 $("#userId2").val(userData.value);
//                 initComm(userData.value);
//             });
//         }, "json");
//     });
// }

function initComm(userId) {
    emptySelect();
    emptySelected();
    layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
    if (userId != "") {
        $.ajax({
                url: baseUrl + "/commission/initCommissionInfo",
                type: "post",
                data: {userId: userId},
                async: true,
                dataType: "json",
                success: function (data) {
                    if (data.data != null && data.data.entity != null) {
                        for (var attr in data.data.entity) {
                            $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);
                        }
                        reloadArticle();
                    }
                }
            }
        );
    }
}

function reloadSelect() {
    var selectForm = $("#selectArticle").serializeJson();
    var historyDeptValue = $('#editForm input:radio[name="historyDept"]:checked').val();
    //如果勾选了历史部门，并且选了业务员和部门，要把业务员的历史部门稿件显示出来，把deptId参数去掉
    if (historyDeptValue == 0 && $('#userId1').val() > 0 && $("#deptId1").val() > 0) {
        delete selectForm['deptId'];
    }
    $("#select_article_table_logs").jqGrid('setGridParam', {
        datatype: "json",
        postData: selectForm, //发送数据
    }).trigger("reloadGrid"); //重新载入
}

function reloadSelected() {
    var selectedForm = $("#selectedArticle").serializeJson();
    var historyDeptValue = $('#editForm input:radio[name="historyDept"]:checked').val();
    //如果勾选了历史部门，并且选了业务员和部门，要把业务员的历史部门稿件显示出来，把deptId参数去掉
    if (historyDeptValue == 0 && $('#userId2').val() > 0 && $("#deptId2").val() > 0) {
        delete selectedForm['deptId'];
    }
    $("#selected_article_table_logs").jqGrid('setGridParam', {
        datatype: "json",
        postData: selectedForm, //发送数据
    }).trigger("reloadGrid"); //重新载入
}

function reloadArticle() {
    reloadSelect();
    reloadSelected();
}

function emptySelect() {
    $("#select_article_table_logs").emptyGridParam();
}

function emptySelected() {
    $("#selected_article_table_logs").emptyGridParam();
}

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType, callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: false,
        success: callBackFun
    });
};
$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    searchForm.init();//初始化部门树
    $("#select_article_table_logs").jqGrid({
        url: baseUrl + '/commission/queryArticleByCommStates',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#selectArticle").serializeJson(), //发送数据
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
        multiselect: true,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100, 500],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'media_type_name', label: '类别', editable: true, width: 60},
            {name: 'user_name', label: '业务员', editable: true, width: 60},
            {name: 'media_user_name', label: '媒介', editable: true, width: 60},
            {name: 'company_name', label: '客户公司', editable: true, width: 60},
            {
                name: 'issued_date', label: '发布日期', editable: true, width: 80,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'media_name', label: '媒体', editable: true, width: 60},
            {name: 'title', label: '标题', editable: true, width: 80},
            {name: 'inner_outer', label: '内外部', editable: true, width: 60},
            {name: 'channel', label: '频道', editable: true, width: 60},
            {name: 'electricity_businesses', label: '电商商家', editable: true, width: 60},
            {name: 'sale_amount', label: '应收（报价）', editable: true, width: 60},
            {name: 'income_amount', label: '回款金额', editable: true, width: 60},
            {
                name: 'incomeDetail',
                index: 'incomeDetail',
                label: '回款详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var a = "";
                    if (rowdata.income_states == 1) {
                        var url = "javascript:void(0) onclick='queryIncomeId(" + rowdata.id + ")'";
                        a = "<a href=" + url + " style='color:#337ab7'>已回款</a>";
                    } else if (rowdata.income_states == 2) {
                        var url = "javascript:void(0) onclick='queryIncomeId(" + rowdata.id + ")'";
                        a = "<a href=" + url + " style='color:#337ab7'>部分回款</a>";
                    } else {
                        a = "";
                    }
                    return a;
                }
            },
            {name: 'outgo_amount', label: '成本（请款）', editable: true, width: 60},
            {
                name: 'OutgoDetail',
                index: 'OutgoDetail',
                label: '请款详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.outgo_states == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOutgoId(" + rowdata.id + ")'>已请款</a>";
                    } else if (rowdata.outgo_states == 2) {
                        html = "<a href='javascript:void(0)' style='color:red'  onclick='queryOutgoId(" + rowdata.id + ")'>请款中</a>";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {name: 'taxes', label: '税金', editable: true, width: 60},
            {
                name: 'invoice_states', label: '开票状态', editable: true, width: 60,
                formatter: function (a, b, rowdata) {
                    if (rowdata.invoice_states == 1) {
                        return "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryInvoiceId(" + rowdata.id + ")'>已开票</a>";
                    } else if (rowdata.invoice_states == 2) {
                        return "<a href='javascript:void(0)' style='color:red'  onclick='queryInvoiceId(" + rowdata.id + ")'>开票中</a>";
                    } else {
                        return "<span style=''>未开票</span>";
                    }
                }
            },
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'user_id', label: 'userId', editable: false, hidden: true, width: 60},
            {name: 'refund_amount', label: '退款', editable: true, width: 60},
            {
                name: 'RefundDetail',
                index: 'RefundDetail',
                label: '退款详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.refund_states == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryRefundId(" + rowdata.id + ")'>已退款</a>";
                    } else if (rowdata.refund_states == 2) {
                        html = "<a href='javascript:void(0)' style='color:red'  onclick='queryRefundId(" + rowdata.id + ")'>退款中</a>";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {name: 'other_pay', label: '其他支出', editable: true, width: 60},
            {
                name: 'otherPayDetail',
                index: 'otherPayDetail',
                label: '其他支出详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.other_pay_states == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOtherPayId(" + rowdata.id + ")'>已支出</a>";
                    } else if (rowdata.other_pay_states == 2) {
                        html = "<a href='javascript:void(0)' style='color:red'  onclick='queryOtherPayId(" + rowdata.id + ")'>支出中</a>";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {name: 'profit', label: '利润', editable: true, width: 60,},
            {name: 'commission', label: '提成', editable: true, width: 60},
            {name: 'income_code', label: '进账编号', editable: true, width: 60},
            {name: 'amountDetail', label: '回款明细', editable: true, width: 140},
            {name: 'income_account', label: '进账账户', editable: true, width: 60},
            {name: 'income_man', label: '进账人', editable: true, width: 60},
            {
                name: 'income_date', label: '进账时间', editable: true, width: 60,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'income_total_amount', label: '进账总金额', editable: true, width: 60},
            {name: 'outgo_code', label: '请款编号', editable: true, width: 60},
            {name: 'outgo_total_amount', label: '请款总金额', editable: true, width: 60},
            // {
            //     name: 'operate', label: "操作", index: '', width: 110,
            //     formatter: function (value, grid, rows) {
            //         var html = "";
            //         html += "<a href='javascript:void(0)' style='height:22px;width:40px;color:blue;'  onclick='register(" + rows.id + ")'>登记&nbsp;&nbsp;</a>";
            //         return html;
            //     },
            // },
        ],
        pager: jQuery("#select_article_pager_logs"),
        viewrecords: true,
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        loadComplete: function (data) {
            if (getResCode(data))
                return;
        }
    });
    layer.msg("正在处理中。。。<br/>请等待表格数据加载完成后再操作", {time: 3000, shade: [0.7, '#393D49']});
    reloadSelect();
    resize("#select_article_pager_logs");
    $("#selected_article_table_logs").jqGrid({
        url: baseUrl + '/commission/queryArticleByYearAndMonth',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#selectedArticle").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽
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
        rowList: [10, 50, 100, 500],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'media_type_name', label: '类别', editable: true, width: 60},
            {name: 'user_name', label: '业务员', editable: true, width: 60},
            {name: 'media_user_name', label: '媒介', editable: true, width: 60},
            {name: 'company_name', label: '客户公司', editable: true, width: 60},
            {
                name: 'issued_date', label: '发布日期', editable: true, width: 60,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    // return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'media_name', label: '媒体', editable: true, width: 60},
            {name: 'title', label: '标题', editable: true, width: 80},
            {name: 'inner_outer', label: '内外部', editable: true, width: 60},
            {name: 'channel', label: '频道', editable: true, width: 60},
            {name: 'electricity_businesses', label: '电商商家', editable: true, width: 60},
            {name: 'sale_amount', label: '应收（报价）', editable: true, width: 60},
            {name: 'income_amount', label: '回款金额', editable: true, width: 60},
            {
                name: 'incomeDetail',
                index: 'incomeDetail',
                label: '回款详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var a = "";
                    if (rowdata.income_states == 1) {
                        var url = "javascript:void(0) onclick='queryIncomeId(" + rowdata.id + ")'";
                        a = "<a href=" + url + " style='color:#337ab7'>已回款</a>";
                    } else if (rowdata.income_states == 2) {
                        var url = "javascript:void(0) onclick='queryIncomeId(" + rowdata.id + ")'";
                        a = "<a href=" + url + " style='color:#337ab7'>部分回款</a>";
                    } else {
                        a = "";
                    }
                    return a;
                }
            },
            {name: 'outgo_amount', label: '成本（请款）', editable: true, width: 60},
            {
                name: 'OutgoDetail',
                index: 'OutgoDetail',
                label: '请款详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.outgo_states == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOutgoId(" + rowdata.id + ")'>已请款</a>";
                    } else if (rowdata.outgo_states == 2) {
                        html = "<a href='javascript:void(0)' style='color:red'  onclick='queryOutgoId(" + rowdata.id + ")'>请款中</a>";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {name: 'taxes', label: '税金', editable: true, width: 60},
            {
                name: 'invoice_states', label: '开票状态', editable: true, width: 60,
                formatter: function (a, b, rowdata) {
                    if (rowdata.invoice_states == 1) {
                        return "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryInvoiceId(" + rowdata.id + ")'>已开票</a>";
                    } else if (rowdata.invoice_states == 2) {
                        return "<a href='javascript:void(0)' style='color:red'  onclick='queryInvoiceId(" + rowdata.id + ")'>开票中</a>";
                    } else {
                        return "<span style=''>未开票</span>";
                    }
                }
            },
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'user_id', label: 'userId', editable: false, hidden: true, width: 60},
            {name: 'refund_amount', label: '退款', editable: true, width: 60},
            {
                name: 'RefundDetail',
                index: 'RefundDetail',
                label: '退款详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.refund_states == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryRefundId(" + rowdata.id + ")'>已退款</a>";
                    } else if (rowdata.refund_states == 2) {
                        html = "<a href='javascript:void(0)' style='color:red'  onclick='queryRefundId(" + rowdata.id + ")'>退款中</a>";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {name: 'other_pay', label: '其他支出', editable: true, width: 60},
            {
                name: 'otherPayDetail',
                index: 'otherPayDetail',
                label: '其他支出详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.other_pay_states == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOtherPayId(" + rowdata.id + ")'>已支出</a>";
                    } else if (rowdata.other_pay_states == 2) {
                        html = "<a href='javascript:void(0)' style='color:red'  onclick='queryOtherPayId(" + rowdata.id + ")'>支出中</a>";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {name: 'profit', label: '利润', editable: true, width: 60,},
            // {name: 'profitPercent', label: '利润占比', editable: true, width: 80},
            {name: 'commission', label: '提成', editable: true, width: 60},
            {
                name: 'commission_date', label: '提成时间', editable: true, width: 60,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'income_code', label: '进账编号', editable: true, width: 60},
            {name: 'amountDetail', label: '回款明细', editable: true, width: 140},
            {name: 'income_account', label: '进账账户', editable: true, width: 60},
            {name: 'income_man', label: '进账人', editable: true, width: 60},
            {
                name: 'income_date', label: '进账时间', editable: true, width: 60,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'income_total_amount', label: '进账总金额', editable: true, width: 60},
            {name: 'outgo_code', label: '请款编号', editable: true, width: 60},
            {name: 'outgo_total_amount', label: '请款总金额', editable: true, width: 60},
            // {
            //     name: 'operate', label: "操作", index: '', width: 110,
            //     formatter: function (value, grid, rows) {
            //         var html = "";
            //         // html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='view("+rows.id+","+rows.user_id+","+rows.year+","+rows.month+")'>提成详情&nbsp;&nbsp;</a>";
            //         html += "<a href='javascript:void(0)' style='height:22px;width:40px;color:blue;'  onclick='registerOff(" + rows.id + ")'>取消登记&nbsp;&nbsp;</a>";
            //         return html;
            //     },
            // },
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
                return;
        }
    });
    resize("#selected_article_pager_logs");

    $("#exportUnRegister").click(function () {
        var params =  removeBlank($("#selectArticle").serializeJson());
        // location.href = "/commission/exportUnRegister?" + $.param(params);
        // startModal("#exportUnRegister");
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        location.href = "/commission/exportUnRegisterNew?" + $.param(params);
        // $.post(baseUrl + "/commission/exportUnRegisterNew", $("#selectArticle").serializeJson(), function (data) {
        //     Ladda.stopAll();
        //     if (data.data.message != null) {
        //         layer.msg(data.data.message, {time: 1000, shade: [0.7, '#393D49']});
        //     } else if (data.data.file != null) {
        //         window.location.href = data.data.file;
        //     } else {
        //         layer.msg(data.msg);
        //     }
        // }, "json")
    });

    $("#exportRegister").click(function () {
        var params =  removeBlank($("#selectedArticle").serializeJson());
        // layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        // location.href = "/commission/exportRegisterNew?" + $.param(params);

        startModal("#BatchOutBtn");
        startModal("#exportRegister");
        startModal("#selectedArticleSearch");
        $.post(baseUrl + "/commission/exportRegisterNew", params, function (data) {
            Ladda.stopAll();
            if (data.data.message != null) {
                layer.msg(data.data.message, {time: 1000, shade: [0.7, '#393D49']});
            } else if (data.data.file != null) {
                //10条的重点关注
                if(data.data.size === 10){
                    swal({
                            title: "",
                            text: "本次导出稿件数量：" + data.data.size + "，请核对！" ,
                            type: "warning",
                            showCancelButton: true,
                            confirmButtonColor: "#FF0000",
                            confirmButtonText: "确认无误，开始下载！",
                            cancelButtonText: "取消，重新核对！",
                            closeOnConfirm: false,
                            reverseButtons: true //控制按钮反转
                        },
                        function (isConfirm) {
                            if (isConfirm) {
                                window.location.href = data.data.file;
                                swal.close();
                            } else {
                                swal.close();
                            }
                        });
                }else{
                    swal({
                            title: "",
                            text: "本次导出稿件数量：" + data.data.size + "，请核对！" ,
                            type: "info",
                            showCancelButton: true,
                            confirmButtonColor: "#2a45dd",
                            confirmButtonText: "确认无误，开始下载！",
                            cancelButtonText: "取消，重新核对！",
                            closeOnConfirm: false,
                            reverseButtons: true //控制按钮反转
                        },
                        function (isConfirm) {
                            if (isConfirm) {
                                window.location.href = data.data.file;
                                swal.close();
                            } else {
                                swal.close();
                            }
                        });
                }
            } else {
                layer.msg(data.msg);
            }
        }, "json")
    });

    $("#selectArticleSearch").click(function () {
        emptySelect();
        reloadSelect();
    });

    $("#selectedArticleSearch").click(function () {
        emptySelected();
        reloadSelected();
    });

    $("#BatchInBtn").click(function () {
        var lock = true;
        if ($("#userId1").val() == "" || $("#userId1").val() == null) {
            swal("请先选择业务员！");
            return;
        }
        var ids = $("#select_article_table_logs").jqGrid("getGridParam", "selarrrow");
        if (ids == null || ids.length <= 0) {
            swal("请先选择要登记的稿件");
            return;
        } else {
            startModal("#BatchInBtn");//锁定按钮，防止重复提交
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: "/commission/batchRegister",    //向后端请求数据的url
                    data: {ids: ids.toString(), userId: $("#userId1").val()},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            for (var attr in data.data.entity) {
                                $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);
                            }
                            reloadArticle();
                        } else if (data.code == 1002) {
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    },
                    error: function () {
                        Ladda.stopAll();//隐藏加载按钮
                    }
                });
            }
        }
    });

    $("#BatchOutBtn").click(function () {
        var lock = true;
        // layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if ($("#userId1").val() == "" || $("#userId1").val() == null) {
            swal("请先选择业务员！");
            return;
        }
        var ids = $("#selected_article_table_logs").jqGrid("getGridParam", "selarrrow");
        if (ids == null || ids.length <= 0) {
            swal("请先选择要取消登记的稿件");
            return;
        } else {
            startModal("#BatchOutBtn");//锁定按钮，防止重复提交
            startModal("#exportRegister");//锁定按钮，防止重复提交
            startModal("#selectedArticleSearch");//锁定按钮，防止重复提交
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: "/commission/batchRegisterOff",    //向后端请求数据的url
                    data: {ids: ids.toString(), userId: $("#userId1").val()},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            for (var attr in data.data.entity) {
                                $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);
                            }
                            reloadArticle();
                        } else if (data.code == 1002) {
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    },
                    error: function () {
                        Ladda.stopAll();//隐藏加载按钮
                    }
                });
            }
        }
    });
});

function queryIncomeId(articleId) {
    $("#incomeModal").modal({backdrop: "static"});
    $("#incomeTable").jqGrid('setGridParam', {
        postData: {id: articleId}, //发送数据
    }).trigger("reloadGrid"); //重新载入
    $("#incomeTable").jqGrid({
        url: baseUrl + '/income/listPgByArticleId',
        postData: {id: articleId},
        datatype: "json",
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

function register(ids) {
    if ($("#userId1").val() == "" || $("#userId1").val() == null) {
        swal("请先选择业务员！");
        return;
    }
    layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
    $.ajax({
        type: "post",
        url: "/commission/batchRegister",    //向后端请求数据的url
        data: {ids: ids, userId: $("#userId1").val()},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                layer.msg(data.data.message, {time: 1000, icon: 6});
                for (var attr in data.data.entity) {
                    $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);
                }
                $("#select_article_table_logs").emptyGridParam();
                $("#select_article_table_logs").jqGrid('setGridParam', {
                    postData: $("#selectArticle").serializeJson(), //发送数据
                }).trigger("reloadGrid"); //重新载入
                $("#selected_article_table_logs").emptyGridParam();
                $("#selected_article_table_logs").jqGrid('setGridParam', {
                    postData: $("#selectedArticle").serializeJson(), //发送数据
                }).trigger("reloadGrid"); //重新载入
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}

function registerOff(ids) {
    if ($("#userId1").val() == "" || $("#userId1").val() == null) {
        swal("请先选择业务员！");
        return;
    }
    layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
    $.ajax({
        type: "post",
        url: "/commission/batchRegisterOff",    //向后端请求数据的url
        data: {ids: ids, userId: $("#userId1").val()},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                layer.msg(data.data.message, {time: 1000, icon: 6});
                for (var attr in data.data.entity) {
                    $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);
                }
                $("#select_article_table_logs").emptyGridParam();
                $("#select_article_table_logs").jqGrid('setGridParam', {
                    postData: $("#selectArticle").serializeJson(), //发送数据
                }).trigger("reloadGrid"); //重新载入
                $("#selected_article_table_logs").emptyGridParam();
                $("#selected_article_table_logs").jqGrid('setGridParam', {
                    postData: $("#selectedArticle").serializeJson(), //发送数据
                }).trigger("reloadGrid"); //重新载入
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}
