function del(id) {
    var lock = true ;
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/income/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/income/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            });
        }
    }, function () {
        return;
    });
}

function edit(id) {
    $("#editForm").find("input").removeClass('error');
    $("#editForm").validate().resetForm();
    $("input:radio").removeAttr("checked");
    $("input:radio").parent().removeClass("checked");
    $("#editModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/income/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.entity) {
                $("#editForm [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                if (attr == 'level') {
                    $("#editForm input[name='level'][value='" + data.data.entity[attr] + "']").iCheck('check');
                }
            }
            $("#editForm select[name='accountId']").empty();
            layui.use('form', function () {
                var form = layui.form;
                $.ajax({
                    type: "post",
                    url: "/account/queryCompanyAccountList",
                    data: {companyCode: user.dept.companyCode},
                    dataType: "json",
                    success: function (data1) {
                        var html = "<option value=''></option>";
                        for (var i = 0; i < data1.length; i++) {
                            if (data.data.entity['accountId'] == data1[i].id) {
                                html += "<option value='" + data1[i].id + "' selected='selected'>" + data1[i].name + "</option>";
                                $("#selected").val(data.data.entity['accountId']);
                            } else {
                                html += "<option value='" + data1[i].id + "'>" + data1[i].name + "</option>";
                            }
                        }
                        $("#editForm select[name='accountId']").append(html)

                        form.render('select');
                        form.on('select', function (data) {
                            $("#selected").val(data.value);
                        });
                    }
                })
            });
        }
    });
    $(".save").hide();
    $(".update").show();
}

function checkNumber(theObj) {
    var reg = /^[0-9](\d+)?(\.\d{1,4})?$/;
    if (reg.test(theObj)) {
        return true;
    }
    return false;
}

function receiveIncome(id) {
    $.ajax({
        type: "post",
        url: baseUrl + "/income/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            if(data.data.entity!=null){
                $("#id2").val(id);
                $("#tradeAmount2").val(data.data.entity['tradeAmount']);
                $("#unclaimedAmount2").val(data.data.entity['unclaimedAmount']);
            }
        }
    });
    $("#receiveModel").modal({backdrop: "static"});
};

function returnIncome(id) {
    var lock = true ;
    layer.confirm('您确定要退回请款？', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index)
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/income/withdraw",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/income/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            });
        }
    }, function () {
        return;
    });
}

function view(id) {
    $("#refreshId").val(id);
    $("input:radio").removeAttr("checked");
    $("input:radio").parent().removeClass("checked");
    $("#viewModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/income/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.entity) {
                $("#viewForm [name=" + attr + "1][type!='radio']").val(data.data.entity[attr]);
                if (attr == 'level') {
                    $("#viewForm input[name='level1']").attr("disabled", "disabled");
                    $("#viewForm input[name='level1'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $("#viewForm input[name='level1'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                    if (data.data.entity[attr] == 'C') {
                        $("#hideOnC").addClass("hide");
                    } else {
                        $("#hideOnC").removeClass("hide");
                    }
                }
            }
            $("#selected_article_table_logs1").jqGrid('setGridParam', {
                datatype:"json",
                postData: {incomeId: id}, //发送数据
            }).trigger("reloadGrid"); //重新载入

            $("#income_user_table_logs").jqGrid('setGridParam', {
                datatype:"json",
                postData: {id: id}, //发送数据
            }).trigger("reloadGrid"); //重新载入
            //分配领款
            $("#dispatchIncome").off("click").on("click", function () {
                var lock = true ;
                if ($("#name").val().length == 0 || $("#amount").val().length == 0) {
                    swal("请选选择领款人和金额")
                } else {
                    if ($("#amount").val() - $("#unclaimedAmount1").val() > 0.0001) {
                        swal("可分配金额不足！\n可用金额=" + $("#unclaimedAmount1").val() + "\n本次分配金额=" + $("#amount").val());
                    } else{
                        startModal("#dispatchIncome");
                        if(lock){
                            lock = false ;
                            $.ajax({
                                type: "post",
                                url: baseUrl + "/income/dispatch",    //向后端请求数据的url
                                data: {incomeId: $("#id1").val(), userId: $("#name").val(), amount: $("#amount").val()},
                                dataType: "json",
                                success: function (data) {
                                    Ladda.stopAll() ;
                                    $("input:radio").removeAttr("checked");
                                    $("input:radio").parent().removeClass("checked");
                                    if (data.code == 200) {
                                        layer.msg(data.data.message, {time: 1000, icon: 6});
                                        document.getElementById("viewForm").reset();
                                        $.ajax({
                                            type: "post",
                                            url: baseUrl + "/income/view",
                                            data: {id: $("#id1").val()},
                                            dataType: "json",
                                            success: function (data) {
                                                for (var attr in data.data.entity) {
                                                    $("#viewForm [name=" + attr + "1][type!='radio']").val(data.data.entity[attr]);
                                                    if (attr == 'level') {
                                                        $("input[name='level1'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                                                        $("input[name='level1'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                                                        if (data.data.entity[attr] == 'C') {
                                                            $("#hideOnC").addClass("hide");
                                                        } else {
                                                            $("#hideOnC").removeClass("hide");
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        $("#query_table_logs").reloadCurrentData(baseUrl + "/income/listPg", $("#queryForm").serializeJson(), "json", null, null);
                                        $("#income_user_table_logs").jqGrid('setGridParam', {
                                            datatype:"json",
                                            postData: {id: $("#id1").val()}, //发送数据
                                        }).trigger("reloadGrid"); //重新载入
                                    } else if(data.code == 1002) {
                                        swal({
                                            title: "异常提示",
                                            text: data.msg,
                                        });
                                    }else{
                                        if (getResCode(data))
                                            return;
                                    }
                                }
                            });
                        }
                    }
                }
            })
        }
    });
}
$(document).ready(function () {
    var e = "<i class='fa fa-times-circle'></i> ";
    $("#queryForm").validate({
        rules: {
            tradeAmountQc: {number: true}
        }, message: {
            tradeAmountQc: {required: e + "请输入正确的进款金额"}
        }
    });
    $("#editForm").validate({
        rules: {
            tradeAmount: {number: true}
        }, message: {
            tradeAmount: {required: e + "请输入正确的进款金额"}
        }
    });

    $("#refresh").click(function () {

        view($("#refreshId").val());
    });
    $.jgrid.defaults.styleUI = 'Bootstrap';

    function resize(table) {
        var width = $(table).parent().width();
        $(table).setGridWidth(width);
    }

    //flag=1审核，否则查看
    if (getQueryString("id") == null || getQueryString("id") == "" || getQueryString("id") == undefined) {

    } else {
        view(getQueryString("id"));
    }


    $("#query_table_logs").jqGrid({
        url: baseUrl + '/income/listPg',
        datatype: "json",
        mtype: 'POST',
        postData: $("#queryForm").serializeJson(), //发送数据
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
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'code', label: '进账编号', editable: true, width: 120},
            {name: 'account_name', label: '账户名称', editable: true, width: 180},
            // {name: 'bank_no', label: '银行账号', editable: true, width: 160},
            // {name: 'tradeTime',index: 'tradeTime', label: '进账日期',editable: true,width: 180,formatoptions: {srcformat: 'Y-m-d H:i:s', newformat: 'Y-m-d H:i:s'}},
            {
                name: 'trade_time',
                label: '进账日期',
                editable: true,
                width: 120,
                sortable: "true",
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'trade_man', label: '进账人', editable: true, width: 180},
            // {name: 'trade_bank', label: '进账银行账号', editable: true, width: 160},
            {name: 'trade_amount', label: '进账金额', editable: true, width: 80},
            {name: 'creator', label: 'creator', editable: true, hidden: true, width: 80},
            {name: 'unclaimed_amount', label: '未领金额', editable: true, width: 80},
            {name: 'preclaimed_amount', label: '预领金额', editable: true, width: 80},
            {
                name: 'create_time',
                label: '录入日期',
                editable: true,
                width: 120,
                sortable: "true",
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd hh:mm");
                }
            },
            {name: 'level', label: '等级', editable: true, width: 60},
            // {name: 'visiable_day', label: '可领天数', editable: true, width: 80},
            {name: 'receiveInfo', label: '领款人姓名', editable: true, width: 240},
            {
                name: 'operate', label: "操作", index: '', width: 180,
                formatter: function (value, grid, rows) {
                    var html = "";
                    if (user.id == rows.creator && rows.preclaimed_amount == 0) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='edit(" + rows.id + ")'>编辑&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleYW() && rows.unclaimed_amount > 0) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='receiveIncome(" + rows.id + "," + rows.trade_amount + "," + rows.unclaimed_amount + ")'>领款&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleYW() && rows.preclaimed_amount > 0) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='returnIncome(" + rows.id + ")'>领款退回&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCW() && (rows.preclaimed_amount == 0 || rows.preclaimed_amount == 0.0)) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red;'  onclick='del(" + rows.id + ")'>删除&nbsp;&nbsp;</a>";
                    }
                    return html;
                },
            }
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
    });
    resize("#query_pager_logs");

    if (hasRoleCWCN()) {
        $("#showFowCW").show();
    }else if (hasRoleCWBZ()){
        $("#showFowCW").show();
        $("#importBtn").hide();
        $("#exportTemplate").hide();
        $("#addBtn").hide();
    }
    else {
        $("#showFowCW").hide();
    }

    // 重新载入数据；
    function reloadTaskData() {
        $("#query_table_logs").reloadCurrentData(baseUrl + "/income/listPg", $("#queryForm").serializeJson(), "json", null, function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
        });
    }

    $("#receive").click(function () {
        var id = $("#id2").val();
        var unclaimedAmount = $("#unclaimedAmount2").val();
        var preclaimedAmount = $("#preclaimedAmount2").val();
        if (checkNumber(preclaimedAmount)) {
            if (parseFloat(unclaimedAmount) < parseFloat(preclaimedAmount)) {
                swal("金额过大！");
                return;
            } else {
                startModal("#receive");
                layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
                $.ajax({
                    type: "post",
                    url: baseUrl + "/income/receive",    //向后端请求数据的url
                    data: {id: id, amount: preclaimedAmount},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            swal(data.data.message);
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/income/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            $("#receiveModel").modal('hide');
                            document.getElementById("receiveForm").reset();
                        } else if(data.code == 1002) {
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        }else{
                            if (getResCode(data))
                                return;
                        }
                    }
                });
            }
        } else {
            swal("请输入数字！")
            return;
        }
    });
    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
    $("#addBtn").click(function () {
        document.getElementById("editForm").reset();
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();
        $("input:radio").removeAttr("checked");
        $("input:radio").parent().removeClass("checked");
        $("#editModal").modal({backdrop: "static"});
        $(".save").show();
        $(".update").hide();
        $("#editForm select[name='accountId']").empty();
        layui.use('form', function () {
            var form = layui.form;
            $.ajax({
                type: "post",
                url: "/account/queryCompanyAccountList",
                data: {companyCode: user.dept.companyCode},
                dataType: "json",
                success: function (data) {
                    var html = "<option value=''></option>";
                    for (var i = 0; i < data.length; i++) {
                        html += "<option value='" + data[i].id + "'>" + data[i].name + "</option>";
                    }
                    $("#editForm select[name='accountId']").append(html);
                    form.render('select');
                    form.on('select', function (data) {
                        $("#selected").val(data.value);
                    });
                }
            })
        });
    });

    $("#exportTemplate").click(function () {
        location.href = "/income/exportTemplate";
    });
    $("#importBtn").click(function () {
        $("#importModal").modal({backdrop: "static"});
    });
    $("#exportBtn").click(function () {
        var params =  removeBlank($("#queryForm").serializeJson());
        location.href = "/income/exportIncome" + "?" + $.param(params);
    });

    // =========================================弹框选择账户信息======================================
    $("#account_table_logs").jqGrid({
        url: baseUrl + '/account/queryCompanyAccount',
        datatype: "json",
        mtype: 'POST',
        postData: $("#innerAccount").serializeJson(), //发送数据
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
        multiboxonly: true,
        beforeSelectRow: beforeSelectRow,
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
            {name: 'name', label: '账户名称', editable: true, width: 240},
            {name: 'owner', label: '账户户主', editable: true, width: 240},
            {name: 'bankNo', label: '银行账号', editable: true, width: 240},
            {name: 'bankName', label: '账号开户行', editable: true, width: 360},
            // {name: 'balance', label: '账号开户行', editable: true, width: 240},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 0},
        ],
        pager: jQuery("#account_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });
    resize("#account_pager_logs")

    //实现单选
    function beforeSelectRow() {
        $("#account_table_logs").jqGrid('resetSelection');
        return (true);
    }


    $("#accountSearch").click(function () {
        $("#account_table_logs").emptyGridParam();
        $("#account_table_logs").jqGrid('setGridParam', {
            postData: $("#innerAccount").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $("#selAccount").click(function () {
        $("#accountModal").modal({backdrop: "static"});
    });
    $(".cleanAccount").click(function () {
        $("#accountId").val("");
        $("#accountName").val("");
        $("#bankNo").val("");
    });

    $("#selectAccount").click(function () {
        var rowid = $("#account_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = $("#account_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        $("#accountId").val(rowid);
        $("#accountName").val(rowData.name);
        $("#bankNo").val(rowData.bankNo);
        $("#accountModal").modal('hide');
        document.getElementById("innerAccount").reset();
    });

    $("#selected_article_table_logs1").jqGrid({
        url: baseUrl + '/income/listPgForSelectedArticle',
        datatype: "local",
        mtype: 'POST',
        postData: {incomeId: id}, //发送数据
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
        // multiboxonly: true,
        // beforeSelectRow: beforeSelectRow,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'code', label: '进款编号', editable: true, width: 120},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {
                name: 'issuedDate', label: '发布日期', editable: true, width: 80,
                formatter: function (d) {
                    if (d == null) {
                        return "";
                    } else {
                        return new Date(d).format("yyyy-MM-dd");
                    }
                }
            },
            {name: 'companyName', label: '客户公司名称', editable: true, width: 120},
            {name: 'custName', label: '对接人', editable: true, width: 60},
            {name: 'brand', label: '品牌', editable: true, width: 80},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 80},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 80},
            {name: 'title', label: '标题', editable: true, width: 120},
            {
                name: 'link', label: '链接', editable: true, width: 120,
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
            {name: 'mediaUserName', label: '媒介', editable: true, width: 60},
            {name: 'saleAmount', label: '应收/报价', editable: true, width: 60},
            {name: 'assignAmount', label: '分款金额', editable: true, width: 60},
            {name: 'assignDate', label: '分款日期', editable: true, width: 120}
        ],
        pager: jQuery("#selected_article_pager_logs1"),
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });

    $("#income_user_table_logs").jqGrid({
        url: baseUrl + '/income/listPgIncomeUserByIncomeId',
        datatype: "local",
        mtype: 'POST',
        postData: {id: id}, //发送数据
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
        // multiboxonly: true,
        // beforeSelectRow: beforeSelectRow,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'name', label: '领款人姓名', editable: true, width: 120},
            {name: 'deptName', label: '部门', editable: true, width: 120},
            {name: 'receiveAmount', label: '领款金额', editable: true, width: 120},
            {name: 'assignAmount', label: '已分款金额', editable: true, width: 120},
            {name: 'remainAmount', label: '剩余金额', editable: true, width: 120},
            {
                name: 'receiveTime', label: '领款日期', editable: true, width: 240,
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd ");
                }
            },
        ],
        pager: jQuery("#income_user_pager_logs"),
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });
});

function submitHander(t, url) {
    if ($("#selected").val() == undefined || $("#selected").val() == "" || $("#selected").val() == null) {
        swal("请先选择账户！");
        return;
    }
    var value = "";//级别不能为空
    $.each($("input[name='level']"), function (i, n) {//参数i为遍历索引值，n为当前的遍历对象.
        if ($(this).is(":checked")) {
            value = $(this).val();
        }
    });
    if (value == null || value == "") {
        swal("请先选择级别！");
        return;
    }

    if ($("#editForm").valid()) {
        var param = $("#editForm").serializeJson();
        startModal("#" + t.id);//锁定按钮，防止重复提交
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        $.ajax({
            type: "post",
            url: url,
            data: param,
            dataType: "json",
            success: function (data) {
                Ladda.stopAll();   //解锁按钮锁定
                if (data.code == 200) {
                    swal(data.data.message);
                    $("#query_table_logs").reloadCurrentData(baseUrl + "/income/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    $("#editModal").modal('hide');
                } else if(data.code == 1002) {
                    swal({
                        title: "异常提示",
                        text: data.msg,
                    });
                }else{
                    if (getResCode(data))
                        return;
                }
            },
            error: function () {
                Ladda.stopAll();
            }
        });
    }
}

function checkFileExt(ext) {
    if (!ext.match(/^.+(.xls|.xlsx)$/)) {
        return false;
    }
    return true;
}

function handleImport(t, url) {
    if (document.getElementById("file").value == "") {
        swal("请选上传excel！");
    } else {
        var filePath = document.getElementById("file").value;
        var fileExt = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
        if (!checkFileExt(fileExt)) {
            var formData = new FormData($("#importForm")[0]);
            startModal("#batchImport");//锁定按钮，防止重复提交
            $.ajax({
                type: "post",
                url: url,
                data: formData,
                dataType: "json",
                async: true,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    Ladda.stopAll();
                    $("#importModal").modal('hide');
                    if (data.code == 200) {
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/income/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal({title: data.data.message, type: "success"});
                    } else if (data.code == 1002) {
                        var str = "";
                        if (data.msg.length > 200) {
                            str = data.msg.slice(0, 200) + "...";
                        } else {
                            str = data.msg;
                        }
                        swal({
                                title: "导入失败，成功导入0条！",
                                text: str,
                                type: "error",
                                showCancelButton: true,
                                confirmButtonColor: "#2a45dd",
                                confirmButtonText: "下载失败详情！",
                                cancelButtonText: "直接关闭！",
                                closeOnConfirm: false,
                                reverseButtons: true //控制按钮反转
                            },
                            function (isConfirm) {
                                if (isConfirm) {
                                    var isIE = (navigator.userAgent.indexOf('MSIE') >= 0);
                                    if (isIE) {
                                        var strHTML = data.msg;
                                        var winSave = window.open();
                                        winSave.document.open("text", "utf-8");
                                        winSave.document.write(strHTML);
                                        winSave.document.execCommand("SaveAs", true, "导入失败详情.txt");
                                        winSave.close();
                                    } else {
                                        var elHtml = data.msg;
                                        var mimeType = 'text/plain';
                                        $('#createInvote').attr('href', 'data:' + mimeType + ';charset=utf-8,' + encodeURIComponent(elHtml));
                                        document.getElementById('createInvote').click();
                                    }
                                    swal.close();
                                } else {
                                    swal.close();
                                }
                            });
                    } else if(data.code == 1003){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    }else{
                        if (getResCode(data))
                            return;
                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                    if (getResCode(data))
                        return;
                }
            });
        }
    }
}