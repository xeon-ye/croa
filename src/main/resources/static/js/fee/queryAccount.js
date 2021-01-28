var companyCode = "" ;
var companyName = "" ;
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
                url: baseUrl + "/account/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/account/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message);
                    } else if (data.code == 1002) {
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
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
    $("input:radio").removeAttr("checked");
    $("input:radio").parent().removeClass("checked");
    $("#viewModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/account/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            $(".inner").hide();
            for (var attr in data.data.entity) {
                $("[name=" + attr + "1][type!=\"radio\"]").val(data.data.entity[attr]);
                // $("[name="+attr+"]").attr("readonly","readonly");
                if (attr == "type") {
                    if (data.data.entity[attr] == 1) {
                        $(".inner").show();
                    }
                }
                if (attr = "accountType") {
                    $("input[name='accountType1']").attr("disabled", "disabled");
                    $("input[name='accountType1'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $("input[name='accountType1'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
            }
            var arr = data.data.list;
            if (arr.length > 0) {
                $("#selectedDept1").empty();
                var html = "<table style='margin-bottom: 15px;'><tr>";
                var length = arr.length;
                for (var i = 0; i < length; i++) {
                    html += '<td width="15%" style="height: 45px;border: 1px solid;text-align: center;">' + arr[i].name + '</td>';
                    if (i > 0 && i < length - 1 && ((i + 1) % 6 == 0)) {
                        html += "</tr><tr>";
                    }
                }
                html += "</tr></table>";
                $("#selectedDept1").append(html);
            }
        }
    });
}

function edit(id) {
    $("#accountForm input[name='accountType']").removeAttr("checked");
    $("#accountForm input[name='accountType']").parent().removeClass("checked");
    $("#accountForm").find("input").removeClass("error");
    $("#accountForm").validate().resetForm();
    document.getElementById("accountForm").reset();
    $("#editModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/account/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            $(".inner").hide();
            for (var attr in data.data.entity) {
                $("[name=" + attr + "][type!=\"radio\"]").val(data.data.entity[attr]);
                if (attr == "type") {
                    if ($("#type").val() == 1) {
                        $(".inner").show();
                    }
                }
                if (attr == "accountType") {
                    $("#accountForm input[name='accountType'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $("#accountForm input[name='accountType'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
            }
            var arr = data.data.list;
            $("#selectedDept").empty();
            var length = arr.length;
            if (length > 0) {
                var width = length >= 6 ? 100 : length % 6 * 15;
                var html = "<table style='margin-bottom: 15px;' width='" + width + "%'><tr>";
                for (var i = 0; i < length; i++) {
                    html += '<td id="row_' + arr[i].id + '" width="15%" style="height: 45px;border: 1px solid;">' +
                        '<input type="hidden"  id="deptId_' + arr[i].id + '" value="' + arr[i].id + '">' +
                        '<input type="hidden"  id="deptName_' + arr[i].id + '" value="' + arr[i].name + '">' + arr[i].name + '' +
                        '&nbsp;&nbsp;&nbsp;&nbsp;<button type="button" id="button_"+' + arr[i].id + ' onclick="delDept(' + arr[i].id + ')" style="margin-right: 5px;">x</button></td>';
                    if (i > 0 && i < length - 1 && ((i + 1) % 6 == 0)) {
                        html += "</tr><tr>";
                    }
                }
                html += "</tr></table>";
                $("#selectedDept").append(html);
            }
        }
    });
    $("#save").hide();
    $("#update").show();
}

//得到查询参数
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]);
    return null;
}

//对接人账号操作
DockingPeopleAccount = {
    op: getQueryString("op"),
    init: function () {
        var dockingId = getQueryString("dockingId") || "";
        var companyId = getQueryString("companyId") || "";
        var companyName = getQueryString("companyName") || "";
        var custName = getQueryString("custName") || "";
        var type = 3;
        $("#tips").html("客户") ;
        $("[name='dockingId']").val(dockingId);
        $("[name='type']").val(type);
        // $("[name='typeQc']").val(type);
        $("[name='typeQc']").empty();
        $("[name='typeQc']").append("<option value='"+type+"' selected='selected'>客户账户</option>");
        $("[name='companyName']").val(companyName);
        $("[name='companyNameQc']").val(companyName);
        $("[name='companyNameQc']").prop("readonly","readonly");
        $("[name='contactorQc']").val(custName);
        $("[name='contactorQc']").prop("readonly","readonly");
        $("[name='contactor']").val(custName);
        $("[name='companyId']").val(companyId);
        $(".inner").hide();
    },
};
//供应商账号操作
SupplierAccount = {
    op: getQueryString("op"),
    init: function () {
        var supplierId = getQueryString("supplierId") || "";
        var companyId = getQueryString("companyId") || "";
        var companyName = getQueryString("companyName") || "";
        var contactor = getQueryString("contactor") || "";
        var type = 2;
        $("#tips").html("供应商") ;
        $("[name='type']").val(type);
        // $("[name='typeQc']").val(type);
        // $("[name='typeQc']").find("option[value!='"+type+"']").remove() ;
        $("[name='typeQc']").empty();
        $("[name='typeQc']").append("<option value='"+type+"' selected='selected'>供应商账户</option>");
        $("[name='companyName']").val(companyName);
        $("[name='companyNameQc']").val(companyName);
        $("[name='companyNameQc']").prop("readonly","readonly");
        $("[name='contactor']").val(contactor);
        $("[name='contactorQc']").val(contactor);
        $("[name='contactorQc']").prop("readonly","readonly");
        $("[name='companyId']").val(companyId);
        $(".inner").hide();
    }
};

$(document).ready(function () {
    var type = 4;
    $("#tips").html("个人") ;
    if(hasRoleCWCN()||hasRoleCWKJ()||hasRoleCWBZ()||hasRoleCWZZ()||hasRoleCWZJ()){
        type = 1;
        $("#tips").html("公司") ;
        $("[name='typeQc']").empty();
        $("[name='typeQc']").append("<option value='"+type+"' selected='selected'>公司账户</option>");
    }
    $("[name='type']").val(type);
    // $("[name='typeQc']").val(type);
    //来源于对接人的账号添加
    if (DockingPeopleAccount.op == "docking") {
        DockingPeopleAccount.init();
    }
    //来源于对接人的账号添加
    if (SupplierAccount.op == "supplier") {
        SupplierAccount.init();
    }

    $.jgrid.defaults.styleUI = 'Bootstrap';

    $("#accountForm").validate({
        rules: {
            phone: {checkPhone: true},
        }

    });
    $.validator.addMethod("checkPhone", function (value, element, params) {
        var checkPhone = /^((([0]\d{2,3}-)?\d{7,8})|([1]\d{10}))$/;
        return this.optional(element) || (checkPhone.test(value));
    }, "请输入正确的手机号码！");

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/account/listPg',
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
            {
                name: 'type', label: '账户类型', editable: true, width: 60,
                formatter: function (a, b, rowdata) {
                    var tmp = rowdata.type;
                    if (tmp == 0) {
                        return "<span style=''>未指定</span>"
                    } else if (tmp == 1) {
                        return "<span style='color: red;'>公司账户</span>"
                    } else if (tmp == 2) {
                        return "<span style='color: blue;'>供应商账户</span>"
                    } else if (tmp == 3) {
                        return "<span style=''>客户账户</span>"
                    } else if (tmp == 4) {
                        return "<span style=''>个人账户</span>"
                    }
                }
            },
            {name: 'companyName', label: '公司名称', editable: true, width: 120},
            {name: 'contactor', label: '联系人', editable: true, width: 120},
            {name: 'name', label: '账户名称', editable: true, width: 120},
            {name: 'owner', label: '户主', editable: true, width: 60},
            {name: 'bankNo', label: '账号', editable: true, width: 120},
            {name: 'bankName', label: '开户行', editable: true, width: 120},
            {
                name: 'accountType', label: '类型', editable: true, width: 60,
                formatter: function (a, b, rowdata) {
                    var tmp = rowdata.accountType;
                    if (tmp == 'B2B') {
                        return "<span style=''>对公账户</span>"
                    } else if (tmp == 'B2C') {
                        return "<span style=''>对私账户</span>"
                    } else {
                        return "<span style=''>未指定</span>"
                    }
                }
            },
            {name: 'phone', label: '预留电话', editable: true, width: 60},
            {name: 'deptNames', label: '所属部门', editable: true, width: 240},
            {
                name: 'operate', label: "操作", index: '', width: 120,
                formatter: function (value, grid, rows, state) {
                    var html = "";
                    var flag = false;

                    if (rows.creator == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='edit(" + rows.id + ")'>编辑&nbsp;&nbsp;</a>";
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='del(" + rows.id + ")'>删除</a>";
                    } else {
                        for (var i = 0; i < user.roles.length; i++) {
                            var role = user.roles[i];
                            if (role.code == "BZ" && role.type == "CW" && rows.type == 1) {
                                flag = true;
                            }
                        }
                        if (flag == true) {
                            html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='edit(" + rows.id + ")'>编辑&nbsp;&nbsp;</a>";
                            html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='del(" + rows.id + ")'>删除</a>";
                        }
                    }
                    return html;
                }
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
            view(rowid);
        },
    });

    // $.validator.addMethod("checkPhone", function (value, element, params) {
    //     var checkPhone = /^((([0]\d{2,3})?-\d{7,8})|([1]\d{10}))$/;
    //     return this.optional(element) || (checkPhone.test(value));
    // }, "请输入正确的手机号码！");

    $("#search").click(function () {
        if($("#typeQc").val()==""){
            swal("请先选择账户类型！");
            return ;
        }
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $.get(baseUrl + "/dict/listByTypeCode2?typeCode=COMPANY_CODE", function (data) {
        $(data).each(function (i, d) {
            if(user.companyCode == d.code){
                $("[name='companyName']").val(d.name);
                // $("[name='companyNameQc']").val(d.name);
                $("[name='companyCode']").val(d.code);
                companyCode = d.code ;
                companyName = d.name ;
            }
        });
    }, "json");

    if(!hasRoleJT()){
        $("#addBtn").show();
    }else{
        $("#addBtn").hide();
    }
    $("#addBtn").click(function () {
        $("#accountForm").find("input").removeClass("error");
        $("#accountForm").validate().resetForm();
        document.getElementById("accountForm").reset();
        $("[name='companyName']").val(companyName);
        $("[name='companyNameQc']").val(companyName);
        $("[name='companyCode']").val(companyCode);
        $("[name='companyId']").val(0);
        if (DockingPeopleAccount.op == "docking") {
            DockingPeopleAccount.init();
        }
        if (SupplierAccount.op == "supplier") {
            SupplierAccount.init();
        }
        // $("input").val('');
        $("#editModal").modal({backdrop: "static"});
        $("#save").show();
        $("#update").hide();
        $(".inner").hide();
    });


    $(".selCompany").click(function () {
        $("#companyModal").modal({backdrop: "static"});
    });


    $("#selDept").click(function () {
        $("#editModal").modal("hide");
        $("#deptModal").modal({backdrop: "static"});
    });
    $("#cleanDept").click(function () {
        $("#deptId").val("");
        $("#deptName").val("");
    });
    $('#treeview').treeview({
        data: [getTreeData()],
        onNodeSelected: function (event, data) {
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            $.ajax({
                type: "post",
                url: baseUrl + "/account/insertAccountDept",
                data: {accountId: $("#id").val(), deptId: data.id},
                dataType: "json",
                success: function (retData) {
                    $("#deptModal").modal('hide');
                    $("#editModal").modal({backdrop: "static"});
                    if (retData.code == 200) {
                        if (retData.data.list != null && retData.data.list.length > 0) {
                            for (var i = 0; i < retData.data.list.length; i++) {
                                var html = '<td id="row_' + retData.data.list[i].id + '" width="15%" style="height: 45px;border: 1px solid;">' +
                                    '<input type="hidden"  id="deptId_' + retData.data.list[i].id + '" value="' + retData.data.list[i].id + '">' +
                                    '<input type="hidden"  id="deptName_' + retData.data.list[i].id + '" value="' + retData.data.list[i].name + '">' + retData.data.list[i].name + '' +
                                    '&nbsp;&nbsp;&nbsp;&nbsp;<button type="button" id="button_"+' + retData.data.list[i].id + ' onclick="delDept(' + retData.data.list[i].id + ')" style="margin-right: 5px;">x</button></td>';
                                // 获取插入位置的对象；
                                var targetElement = $("#selectedDept > table").find("tr").last();
                                // 如果已有5个以上则另起一行；
                                var length = targetElement.children().length;
                                if (length > 5) {
                                    $(targetElement).parent().append("<tr>" + html + "</tr>");
                                } else {
                                    if (length > 0) {
                                        $(targetElement).append(html);
                                    } else {
                                        $("#selectedDept").append("<table style='margin-bottom: 15px;'><tr>" + html + "</tr></table>");
                                        // 显示提示；
                                        $("#selectedDept").parent().next().show();
                                    }
                                }
                                // 调整宽度；
                                // 如果没有找到则为1；
                                length = length == 0 ? 1 : $("#selectedDept").find("td").length;
                                var width = length >= 6 ? 100 : length % 6 * 15;
                                $("#selectedDept > table").attr("width", width + "%");
                            }
                            swal(retData.data.message);
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/account/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        }else{
                            swal("该部门已关联，无需重复关联！")
                        }
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            });
        }
    });
});

function delDept(deptId) {
    layer.msg("正在处理中，请稍候。", {time: 1000, shade: [0.7, '#393D49']});
    $.ajax({
        type: "post",
        url: baseUrl + "/account/deleteAccountDept",
        data: {accountId: $("#id").val(), deptId: deptId},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                if (data.data.list != null) {
                    for (var i = 0; i < data.data.list.length; i++) {
                        $("#row_" + data.data.list[i].id).remove();
                    }
                    // 获取所有数据的列；
                    var tdElements = $("#selectedDept").find("td");
                    // 清空原表格；
                    $("#selectedDept").empty();
                    // 获取数据长度；
                    var length = tdElements.length;
                    // 如果还有数据则重新排序；
                    if (length > 0) {
                        // 拼接表格；
                        var width = length >= 6 ? 100 : length % 6 * 15;
                        var html = "<table style='margin-bottom: 15px;' width='" + width + "%'></table>";
                        $("#selectedDept").append(html);
                        // 获取表格对象；
                        var tableElement = $("#selectedDept > table");
                        tdElements.each(function (i, tdElement) {
                            if (i == 0) {
                                tableElement.append("<tr></tr>");
                                tableElement = $("#selectedDept > table").find("tr").last();
                            }
                            if (i > 0 && i % 6 == 0) {
                                tableElement.parent().append("<tr></tr>");
                                tableElement = $("#selectedDept > table").find("tr").last();
                            }
                            tableElement.append($(tdElement).clone());
                        });
                    } else {
                        // 隐藏提示；
                        $("#selectedDept").parent().next().hide();
                    }
                }
                swal(data.data.message);
                $("#query_table_logs").reloadCurrentData(baseUrl + "/account/listPg", $("#queryForm").serializeJson(), "json", null, null);
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}

function submitHander(t, url) {
    var lock = true ;
    if ($("#accountForm").valid()) {
        var param = $("#accountForm").serializeForm();
        startModal("#" + t.id);//锁定按钮，防止重复提交
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: url,
                data: param,
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    $("#editModal").modal('hide');
                    if (data.code == 200) {
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/account/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message);
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
                    Ladda.stopAll();
                }
            });
        }
    }
}

function getTreeData() {
    var deptTreeData = {};
    $.ajax({
        type: "POST",
        url: baseUrl + "/dept/listForTreeView",
        dataType: "json",
        async: false,
        success: function (result) {
            var arrays = result.data.list;
            deptTreeData = arrays[0];
        }
    });
    return deptTreeData;
}