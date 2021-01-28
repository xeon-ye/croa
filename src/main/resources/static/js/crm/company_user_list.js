var array = [];
var queryGridName = "query_table_logs";
var XMflag = false;//是否是项目总监
var companyType = {
    changeCompanyCommon: function (typeCode, parentNode, typeNameDiv) {
        if (typeCode == 'QT') {
            $("#industry").val("");
            typeNameDiv.style.display = "block";
            if (parentNode.hasClass("col-sm-2")) {
                parentNode.removeClass("col-sm-2")
            }
            if (!parentNode.hasClass("col-sm-1")) {
                parentNode.addClass("col-sm-1")
            }
        } else if (typeCode == 'JF' || typeCode == '') {
            $("#industry").val("");
            typeNameDiv.style.display = "none";
            if (parentNode.hasClass("col-sm-1")) {
                parentNode.removeClass("col-sm-1")
            }
            if (!parentNode.hasClass("col-sm-2")) {
                parentNode.addClass("col-sm-2")
            }
        } else {
            $("#industry").val("QT");
            typeNameDiv.style.display = "none";
            if (parentNode.hasClass("col-sm-1")) {
                parentNode.removeClass("col-sm-1")
            }
            if (!parentNode.hasClass("col-sm-2")) {
                parentNode.addClass("col-sm-2")
            }
        }
        layui.use('form', function () {
            var form = layui.form;
            form.render("select");
        });
    },
    initCompanyType: function (id, name, div, key, value) {
        var parentNode = $("#" + id).parent();
        var typeNameDiv = document.getElementById(div);
        $.ajax({
            type: "get",
            url: "/dict/listByTypeCode2",
            data: {typeCode: 'CUST_TYPE'},
            dataType: "json",
            success: function (data) {
                $("#" + id).empty();
                var html = "";
                if (data != null) {
                    html += "<option value=''>请选择</option>";
                    for (var i = 0; i < data.length; i++) {
                        if (key != "" && data[i].code == key) {
                            $("#" + name).val(value);
                            html += "<option value='" + data[i].code + "' selected='selected'>" + data[i].name + "</option>";
                        } else {
                            html += "<option value='" + data[i].code + "' >" + data[i].name + "</option>";
                        }
                    }
                    $("#" + id).append(html);
                }
                companyType.changeCompanyCommon(key, parentNode, typeNameDiv);
            }
        })
    },
    changeCompanyType: function (t) {
        var typeCode = $(t).val();
        var parentNode = $(t).parent();
        var typeNameDiv = document.getElementById("typeNameDiv");
        if (typeCode == 'QT') {
            $("#typeName").val("");
        } else {
            $("#typeName").val($(t).find("option:selected").text());
        }
        companyType.changeCompanyCommon(typeCode, parentNode, typeNameDiv);
    }
};

function initTax(div, value) {
    $.ajax({
        type: "get",
        url: "/dict/listDict",
        data: {typeCode: 'tax'},
        dataType: "json",
        success: function (data) {
            $("#" + div).empty();
            var html = "<option value=''></option>";
            if (data != null) {
                for (var i = 0; i < data.length; i++) {
                    if (data[i].name == value) {
                        html += "<option selected='selected' value='" + data[i].name + "' >" + data[i].name + "</option>";
                    } else {
                        html += "<option value='" + data[i].name + "' >" + data[i].name + "</option>";
                    }
                }
                html += "</select>";
                $("#" + div).append(html);
            }
        }
    })
}

var deptId = user.dept.id;//当前用户部门ID
var deptCode = user.dept.code;//当前部门编码
var deptName = user.dept.name;//当前部门编码
var companyCode = user.dept.companyCode;//部门公司代码
var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
var searchObj = {

    //如果是财务、管理等岗位获取公司或集团的id
    getDeptId: function () {
        if (currentDeptQx || currentCompanyQx || hasRoleXT()) {
            document.getElementById("deptDiv").style.display = "block";
        } else {
            document.getElementById("deptDiv").style.display = "none";
        }
        var deptCompanyCode = user.dept.companyCode;//部门公司代码
        if ((deptCompanyCode == "JT" && (currentDeptQx || deptCode == "GL")) || hasRoleXT()) {
            requestData(null, "/dept/getRootDept", "POST", function (result) {
                var root = result.data.root;
                if (root) {
                    deptId = root.id;//整个集团的业务和媒介部
                    deptName = root.name;
                } else {
                    deptId = 517;//整个集团的业务和媒介部
                    deptName = "集团";
                }
            });
        } else if (user.currentCompanyQx || deptCode == "GL" || hasRoleXT()) {
            requestData({companyCode: deptCompanyCode}, "/dept/getCompanyByCode", "POST", function (result) {
                var company = result.data.company;
                if (company) {
                    deptId = company.id;//整个集团的业务和媒介部
                    deptName = company.name;
                }
            });
        }

        $("#deptId").val(deptId);
        $("#deptName").val(deptName);
        searchObj.loadWorker(deptId, "YW"); //查询业务员工
        return deptId;
    },
    loadWorker: function (deptId, roleType) {
        var ele = $("#userId");
        ele.empty();
        ele.append('<option value="">业务员</option>');
        if (roleType) {
            searchObj.loadDeptUser(deptId, roleType);
        }

    },
    loadDeptUser: function (deptId, roleType) {
        layui.use(['form'], function () {
            var ele = $("#userId");
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

                    }
                }
            );
            var form = layui.form;
            form.render('select');//select是固定写法 不是选择器
        });
    },
};
var deptObj = {
    init: function () {
        deptObj.loadDept();
        deptObj.loadDeptUser(deptId, "YW");
    },
    loadDept: function () {
        var ele = $("#transDeptId");
        ele.empty();
        if (deptCode == 'YW' && currentDeptQx) {
            requestData({}, "/dept/childs/" + deptId, "POST", function (result) {
                var list = result.data.list;
                if (list && list.length > 0) {
                    for (var i = 0; i < list.length; i++) {
                        ele.append("<option value=" + list[i].id + ">" + list[i].name + "</option>");
                    }
                    $("#transDeptName").val(list[0].name);
                }
            });
        } else if (deptCode == "GL" && currentCompanyQx) {
            requestData({}, "/dept/queryYWDept", "POST", function (result) {
                if (result && result.length > 0) {
                    for (var i = 0; i < result.length; i++) {
                        ele.append("<option value=" + result[i].id + ">" + result[i].name + "</option>");
                    }
                    $("#transDeptName").val(result[0].name);
                }
            });
        } else if(hasRoleXT()) {
            requestData({}, "/dept/listByCode?code=YW", "POST", function (result) {
                if (result && result.length > 0) {
                    for (var i = 0; i < result.length; i++) {
                        ele.append("<option value=" + result[i].id + ">" + result[i].name + "(" + result[i].companyCodeName + ")" + "</option>");
                    }
                    $("#transDeptName").val(result[0].name);
                }
            });
        }else{
            //没有权限
        }
        layui.use('form', function () {
            var form = layui.form;
            form.render('select');
            form.on('select(transDept)', function (data) {
                deptObj.loadDeptUser(data.value, "YW");
            });
        });
    },

    //加载部门下的业务员
    loadDeptUser: function (deptId, roleType) {
        layui.use(['form'], function () {
            var ele = $("#transUserId");
            ele.empty();
            $.ajax({
                    url: baseUrl + "/user/listUserByDeptAndRoleJT",
                    type: "post",
                    data: {deptId: deptId, roleType: roleType},
                    async: false,
                    dataType: "json",
                    success: function (data) {
                        var userList = data.data.list;
                        ele.append('<option value="">业务员</option>');
                        if (userList && userList.length > 0) {
                            for (var i = 0; i < userList.length; i++) {
                                ele.append("<option value=" + userList[i].id + ">" + userList[i].name + "</option>");
                            }
                        }
                    }
                }
            );
            var form = layui.form;
            form.render('select');//select是固定写法 不是选择器
            form.on('select(transUser)', function (data) {
                $("#transUserName").val($(this).text());
            });
        });
    },
};

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param callBackFun 成功回调方法
 */
function requestData(data, url, requestType, callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: false,
        success: callBackFun
    });
}

function getTreeData(deptId) {
    var deptTreeData = {};
    $.ajax({
        type: "POST",
        data: {deptId: deptId, deptCode: 'YW'},
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

$(document).ready(function () {

    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    var startTimeQc = {
        elem: '#startTimeQc',
        format: 'YYYY/MM/DD ',
        // min: laydate.now(), //设定最小日期为当前日期
        // max: laydate.now(),//最大日期
        istime: false,
        istoday: false,
        choose: function (datas) {
            // endTimeQc.min = datas; //开始日选好后，重置结束日的最小日期
            // endTimeQc.start = datas //将结束日的初始值设定为开始日
            var startTime = $("#startTimeQc").val();
            var endTime = $("#endTimeQc").val();
            if(startTime && endTime && startTime > endTime){
                layer.msg("开始时间不能大于结束时间");
                $("#startTimeQc").val("")
                return;
            }
        }
    };
    var endTimeQc = {
        elem: '#endTimeQc',
        format: 'YYYY/MM/DD ',
        //min: laydate.now(),
        // max: laydate.now(),
        istime: false,
        istoday: false,
        choose: function (datas) {
            // startTimeQc.max = datas; //结束日选好后，重置开始日的最大日期
            var startTime = $("#startTimeQc").val();
            var endTime = $("#endTimeQc").val();
            if(startTime && endTime && startTime > endTime){
                layer.msg("开始时间不能大于结束时间");
                $("#endTimeQc").val("")
                return;
            }
        }
    };
    laydate(startTimeQc);
    laydate(endTimeQc);

    deptObj.init();
    statisticsModal.init();//初始化模态框

    var project = projectDirector();
    if( project && project.contains(user.id)){
        XMflag =true;
    }

    $("#queryBtn").click(function () {
        gridObj.reloadCompany();
    });

    $('#queryDiv').on('keypress', function (event) {
        if (event.keyCode == "13") {
            $("#queryBtn").click();
        }
    });

    $("#transferBtn").click(function () {
        var flag = false;
        var abFlag = false;
        if (array.length == 0) {
            swal("请先勾选对接人！");
            return;
        } else {
            $("#transferBody").empty();
            var html = "";
            $.each(array, function (index, item) {
                var rowData = $("#" + queryGridName).jqGrid('getRowData', item);
                if (rowData.auditFlag == 1) {
                    flag = true;
                }
                if (array.length > 1 && (rowData.protectLevel == 3 || rowData.protectLevel == 2)) {
                    abFlag = true;
                }
                html += "<tr>";
                html += "<td style='text-align:center'>" + rowData.companyName + "</td>";
                html += "<td style='text-align:center'>" + rowData.standardize + "</td>";
                html += "<td style='text-align:center'>" + rowData.companyUserName + "</td>";
                html += "<td style='text-align:center'>" + rowData.normalize + "</td>";
                html += "<td style='text-align:center'>" + rowData.protectStrong + "</td>";
                html += "<td style='text-align:center'>" + rowData.protectLevelText + "</td>";
                html += "<td style='text-align:center'>" + rowData.ywUserName + "</td>";
                html += "</tr>";
            });
            if (flag) {
                swal("选中的客户中有保护审核中的客户，无法流转，请刷新后重新选择！");
                return;
            }
            if(abFlag){
                swal("选中的客户有A类或B类客户，并且选中了多个对接人，A、B类客户移交时请只勾选一个对接人！");
                return;
            }
            $("#transferBody").append(html);
            $("#ids").val(array.toString())
            $("#addTransferModal").modal({backdrop: "static"});
        }
    });

    searchObj.getDeptId();

    $("#selDept").click(function () {
        startModal("#selDept");
        $("#deptModal").modal('toggle');
        Ladda.stopAll();
    });
    $('#treeview').treeview({
        data: [getTreeData(deptId)],
        onNodeSelected: function (event, data) {
            $("#deptId").val(data.id);
            $("#deptName").val(data.text);
            $("#deptModal").modal('hide');
            searchObj.loadWorker(data.id, 'YW');
        }
    });

    $("#cleanDept").click(function () {
        startModal("#cleanDept");
        $("#deptId").val(deptId);
        $("#deptName").val(deptName);
        searchObj.loadWorker(deptId, 'YW');
        layer.msg("操作成功！");
        Ladda.stopAll();
    });

    $("#keyword").on('keypress', function (event) {
        if (event.keyCode == "13") {
            companyObj.check(this);
        }
    });

    $("#enterpriseBtn").click(function () {
        companyObj.natureClick(this, 1, $("#addCompanyForm input[name='companyName']").val());
    });
    $("#personalBtn").click(function () {
        companyObj.natureClick(this, 0, $("#addCompanyForm input[name='companyName']").val());
    });

    if (deptCode == 'YW' && !XMflag) {//业务部的并且不能是项目总监
        $("#addBtn").show();
    } else {
        $("#addBtn").hide();
    }

    if (hasRoleYWYG() || hasRoleYWZL() || XMflag) {
        $("#transferBtn").hide();
    } else {
        $("#transferBtn").show();
    }

    $("#companyHistoryBtn").click(function () {
        $("#viewCompanyModal").modal('hide');
        $("#companyHistoryModal").modal({backdrop: "static"});
        setTimeout(function () {
            gridObj.reloadCompanyHistory($("#viewCompanyForm [name='id']").val());
        }, 500);
    });

    $("#userHistoryBtn").click(function () {
        $("#viewCompanyModal").modal('hide');
        $("#userHistoryModal").modal({backdrop: "static"});
        setTimeout(function () {
            gridObj.reloadUserHistory($("#viewCompanyUserForm [name='id']").val());
        }, 500);
    });

    //保护审核框
    if (getQueryString("protectId") != null && getQueryString("protectId") != "" && getQueryString("protectId") != undefined) {
        companyObj.viewProtect(getQueryString("protectId"), getQueryString("flag"));
    }

    //修改公司框
    if (getQueryString("companyUserId") != null && getQueryString("companyUserId") != "" && getQueryString("companyUserId") != undefined) {
        companyObj.editCompanyBasic(getQueryString("companyUserId"));
    }
    $('#companyHistoryModal').on('hidden.bs.modal', function () {
        $("#viewCompanyModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $('#userHistoryModal').on('hidden.bs.modal', function () {
        $("#viewCompanyModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $('#addProductModal').on('hidden.bs.modal', function () {
        $("#editModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });
    $('#addConsumerModal').on('hidden.bs.modal', function () {
        $("#editModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $('#addAccountModal').on('hidden.bs.modal', function () {
        $("#editModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $('#historyModal').on('hidden.bs.modal', function () {
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $('#imgModal').on('hidden.bs.modal', function () {
        $("body").addClass("modal-open");
    });

    $('#imgCarouselModal').on('hidden.bs.modal', function () {
        $("#imgCarouselModal").modal('hide');
        $("body").addClass("modal-open");
    });

    var e = "<i class='fa fa-times-circle'></i> ";
    $("#consumerForm").validate({
        rules: {
            age: {minlength: 1, maxlength: 3},
            area: {required: !0},
        },
        messages: {
            age: {minlength: e + "年龄长度必须大于{0}个字符", maxlength: e + "年龄长度必须小于{0}个字符"},
            area: {required: e + "请输入地域分布"},
        }
    });

    $("#" + queryGridName).jqGrid({
        url: '/crm/company/list',
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
        sortname: "trackLimit",
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "companyUserId"
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
                        return "<span style='color:#1ab394'>标准</span>";
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
                        return statisticsToggle.getSingleLinkHtml(rows.companyUserId, rows.companyUserName,"cust");
                    }
                }
            },
            /*{
                name: 'mobile',
                index: 'mobile',
                label: '手机号',
                editable: false,
                width: 35,
                align: "center",
                sortable: false
            },
            {
                name: 'wechat',
                index: 'wechat',
                label: '微信',
                editable: false,
                width: 35,
                align: "center",
                sortable: false
            },
            {
                name: 'qq',
                index: 'qq',
                label: 'QQ',
                editable: false,
                width: 35,
                align: "center",
                sortable: false
            },*/
            {
                name: 'normalize',
                index: 'normalize',
                label: '是否规范',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "<span style='color:#1ab394'>规范</span>";
                    } else {
                        return "<span class='text-red'>非规范</span>";
                    }
                }
            },
            {
                name: 'auditFlag',
                index: 'auditFlag',
                label: 'auditFlag',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'auditState',
                index: 'auditState',
                label: '审核中',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                // cellattr: function (rowId, tv, rawObject, cm, rdata) {
                //     return "id='auditFlag" + rowId + "'";
                // },
                formatter: function (a, b, rowdata) {
                    if (rowdata.auditFlag == 1) {
                        return "<span class='text-red'>是</span>";
                    } else {
                        return "<span>否</span>";
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
                formatter: function (a, b, rowdata) {
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
                label: 'protectLevel',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'protectLevelText',
                index: 'protectLevelText',
                label: '保护等级',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (rowdata.protectLevel == 3) {
                        return "<span style='color:#1ab394'>保护(A)</span>";
                    } else if (rowdata.protectLevel == 2) {
                        return "<span style='color:#1ab394'>保护(B)</span>";
                    } else if (rowdata.protectLevel == 1) {
                        return "<span style='color:#1ab394'>保护(C)</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'dealFlag',
                index: 'dealFlag',
                label: '成交状态',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    if (a) {
                        return "<span style='color:#1ab394'>已成交</span>";
                    } else {
                        return "未成交";
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
            /*{
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
            },*/
            /*{
                name: 'evalTime',
                index: 'evalTime',
                label: '跟进时间',
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
            },*/
            {
                name: 'trackLimit',
                index: 'trackLimit',
                label: '跟进考核剩余天数',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                formatter: function (a, b, row) {
                    if (a == undefined) {
                        return "";
                    } else if (a < 0) {
                        return "<span class='text-red'>" + 0 + "</span>";
                    } else if (a < row.evalRemindDay) {
                        return "<span class='text-red'>" + a + "</span>";
                    } else {
                        return "<span>" + a + "</span>";
                    }
                }
            },
            /*{
                name: 'dealTime',
                index: 'dealTime',
                label: '成交时间',
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
            },*/
            {
                name: 'dealLimit',
                index: 'dealLimit',
                label: '成交考核剩余天数',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                formatter: function (a, b, row) {
                    if (a == undefined) {
                        return "";
                    } else if (a < 0) {
                        return "<span class='text-red'>" + 0 + "</span>";
                    } else if (a < row.evalRemindDay) {
                        return "<span class='text-red'>" + a + "</span>";
                    } else {
                        return "<span>" + a + "</span>";
                    }
                }
            },
            {
                name: 'ywUserName',
                index: 'ywUserName',
                label: '负责人',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                formatter: function (value, grid, rows) {
                    if(value){
                        return statisticsToggle.getSingleLinkHtml(rows.ywUserId, rows.ywUserName,"business");
                    }else{
                        return "";
                    }
                }
            },
            /*{
                name: 'devState',
                index: 'devState',
                label: '客户状态',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },*/
            {
                name: 'operator',
                label: '操作',
                editable: false,
                width: 200,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    // 如果公司不是非标准化公司增加跳转按钮
                    if(rowdata.standardize != 1  && rowdata.type == 1 && rowdata.ywUserId == user.id){
                        html += "<div style='padding:3px'>";
                        html += "&nbsp;&nbsp;";
                        var url = "/crm/queryStandardizedCompany?flag=3&companyName="+rowdata.companyName;
                        html += "<button class='btn btn-xs btn-default btn-outline' title='申请为标准公司' onclick=\"page('"+url+"','申请为标准公司');\">申请为标准公司</button>";
                        html += "</div>";
                    }
                    html += "<div style='padding:3px'>";
                    if (rowdata.ywUserId == user.id || hasRoleXT()) {
                        html += "<button class='btn btn-xs btn-primary btn-outline' type='button' title='修改公司名' onclick='companyObj.editCompanyBasic(" + rowdata.companyUserId + ");'>修改公司名</button>";
                        html += "&nbsp;&nbsp;";
                    }
                    if(!XMflag){
                        html += "<button class='btn btn-xs btn-default btn-outline' type='button' title='详情查看' onclick='companyObj.viewCompanyAll(" + rowdata.companyId + "," + rowdata.companyUserId + ");'>详情查看</button>";
                        if(rowdata.ywUserId == user.id){
                            html += "&nbsp;&nbsp;";
                            html += "<button class='btn btn-xs btn-default btn-outline' type='button' title='下拉' onclick='companyObj.dropdown(this)' >&nbsp;<span class='glyphicon glyphicon-chevron-up'></span>&nbsp;</button>";
                            html += "<button class='btn btn-xs btn-default btn-outline' type='button' title='拉起' style='display: none' onclick='companyObj.dropup(this)' >&nbsp;<span class='glyphicon glyphicon-chevron-down'></span>&nbsp;</button>";
                        }
                    }
                    html += "</div>";
                    if(rowdata.ywUserId == user.id) {
                        html += "<div style='padding:3px; display: none'>";
                        if (rowdata.auditFlag == 0) {
                            html += "<button class='btn btn-xs btn-warning btn-outline' type='button' title='抛入公海' id='throwToPublic' onclick='companyObj.addPublic(" + rowdata.companyUserId + ");'>抛入公海</button>";
                            html += "&nbsp;&nbsp;";
                        }
                        if (rowdata.normalize == 1 && rowdata.standardize == 1 && rowdata.auditFlag == 0 && rowdata.protectLevel < 3) {
                            // 标准且规范，没有申请保护，不是A类保护
                            html += "<button class='btn btn-xs btn-success btn-outline' type='button' title='申请保护' onclick='companyObj.addProtect(" + rowdata.companyId + "," + rowdata.companyUserId + "," + rowdata.protectLevel + ");'>申请保护</button>";
                            html += "&nbsp;&nbsp;";
                        }
                        html += "</div>";
                        html += "<div style='padding:3px; display: none'>";
                        html += "<button class='btn btn-xs btn-primary btn-outline' type='button' title='客户跟进' onclick='trackObj.add(" + rowdata.companyId + ",\"" + rowdata.companyName + "\"," + rowdata.companyUserId + ",\"" + rowdata.companyUserName + "\");'>客户跟进</button>";
                        html += "&nbsp;&nbsp;";
                        html += "<button class='btn btn-xs btn-success btn-outline' type='button' title='详情编辑' onclick='companyObj.editCompany(" + rowdata.companyId + "," + rowdata.companyUserId + ");'>详情编辑</button>";
                        html += "&nbsp;&nbsp;";
                        if (!rowdata.dealFlag) {
                            html += "&nbsp;<button class='btn btn-xs btn-danger btn-outline' type='button' title='删除' onclick='companyObj.delCompanyUser(" + rowdata.companyUserId + ");'>&nbsp;删除&nbsp;</button>";
                        }
                        html += "</div>";
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
            // var rowData = jQuery("#query_table_logs").jqGrid("getRowData", rowid);
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            // view(rowid, 1);
        },
        gridComplete: function () {
            //在gridComplete调用合并方法
            // var primaryKey = "companyId";
            // gridObj.MergerColSpan(queryGridName, "companyName", primaryKey);
            // gridObj.MergerColSpan(queryGridName, "standardize", primaryKey);
            // gridObj.MergerColSpan(queryGridName, "type", primaryKey);
            // gridObj.MergerColSpan(queryGridName, "auditFlag", primaryKey);
            array.length = 0;//翻页清空选中
            // if(array.length>0){//保留选中
            //     for (var i = 0; i < array.length; i++) {
            //         $(this).jqGrid('setSelection',array[i]);
            //     }
            // }

        },
        loadComplete: function (a, b, c) {
            $("#jqgh_" + queryGridName + "_cb").css("padding-right", "6px");
        },
        onSelectAll: function (aRowids, status) {
            if (status == true) {
                //循环aRowids数组，将code放入arrayNewList数组中
                $.each(aRowids, function (i, item) {
                    //已选中的先排除
                    if (!(array.indexOf(item) > -1)) {
                        gridObj.saveData(queryGridName, item);
                    }
                })
            } else {
                //循环aRowids数组，将code从arrayNewList中删除
                $.each(aRowids, function (i, item) {
                    gridObj.deleteData(queryGridName, item);
                })
            }
        },
        onSelectRow: function (rowid, status) {
            if (status == true) {
                if (!(array.indexOf(rowid) > -1)) {
                    gridObj.saveData(queryGridName, rowid);
                }
            } else {
                gridObj.deleteData(queryGridName, rowid);
            }
        }
    });
    resize("#query_pager_logs");

    $("#company_history_table_logs").jqGrid({
        url: '/crm/company/companyHistory',
        datatype: "local",
        mtype: 'POST',
        // postData: null, //发送数据
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
        rowNum: 5,//每页显示记录数
        rowList: [5, 10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                label: 'id',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'createTime',
                index: 'createTime',
                label: '操作时间',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'creator',
                index: 'creator',
                label: '操作人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'name',
                index: 'name',
                label: '公司名',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'type',
                index: 'type',
                label: '客户性质',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'typeName',
                index: 'typeName',
                label: '公司类型',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'industry',
                index: 'industry',
                label: '行业',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'area',
                index: 'area',
                label: '地区',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'brand',
                index: 'brand',
                label: '品牌',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'product',
                index: 'product',
                label: '公司产品',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'structure',
                index: 'structure',
                label: '公司结构',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
            },
            {
                name: 'purpose',
                index: 'purpose',
                label: '传播目的',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'scale',
                index: 'scale',
                label: '规模',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'advVolume',
                index: 'advVolume',
                label: '投放量',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'experience',
                index: 'experience',
                label: '传播经验',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'publicMedia',
                index: 'publicMedia',
                label: '发布媒体',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'channel',
                index: 'channel',
                label: '传播渠道',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'image',
                index: 'image',
                label: '头像',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                formatter: function (a, b, row) {
                    if (a == "" || a == null || a == undefined) {
                        return "";
                    } else {
                        var html = "<img src='" + a + "' height='30.9px' width='50px'>";
                        return html;
                    }
                }
            }
        ],
        pager: jQuery("#company_history_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#user_history_table_logs").jqGrid({
        url: '/crm/company/companyUserHistory',
        datatype: "local",
        mtype: 'POST',
        // postData: null, //发送数据
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
        rowNum: 5,//每页显示记录数
        rowList: [5, 10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                label: 'id',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'createTime',
                index: 'createTime',
                label: '修改时间',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'creator',
                index: 'creator',
                label: '修改人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'companyName',
                index: 'companyName',
                label: '公司名称',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'name',
                index: 'name',
                label: '对接人',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'mobile',
                index: 'mobile',
                label: '手机号',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'wechat',
                index: 'wechat',
                label: '微信',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
            },
            {
                name: 'qq',
                index: 'qq',
                label: 'QQ',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'custType',
                index: 'custType',
                label: '客户类型',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, row) {
                    if (a == 3) {
                        return "小型";
                    } else if (a == 2) {
                        return "中型";
                    } else if (a == 1) {
                        return "大型";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'intention',
                index: 'intention',
                label: '意向度',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'successDetail',
                index: 'successDetail',
                label: '成交详情',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'promiseDay',
                index: 'promiseDay',
                label: '到款天数',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'dept',
                index: 'dept',
                label: '所在部门',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'job',
                index: 'job',
                label: '职位',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'project',
                index: 'project',
                label: '负责项目',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'professionLevel',
                index: 'professionLevel',
                label: '专业程度',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'personality',
                index: 'personality',
                label: '性格',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'age',
                index: 'age',
                label: '年龄',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'family',
                index: 'family',
                label: '家庭',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'education',
                index: 'education',
                label: '文化程度',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'hobby',
                index: 'hobby',
                label: '爱好',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'looks',
                index: 'looks',
                label: '长相',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'prevCompany',
                index: 'prevCompany',
                label: '之前所在的公司',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'photo',
                index: 'photo',
                label: '头像',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, row) {
                    if (a == "" || a == null || a == undefined) {
                        return "";
                    } else {
                        var html = "<img src='" + a + "' height='30.9px' width='50px'>";
                        return html;
                    }
                }
            },
            {
                name: 'invoiceType',
                index: 'invoiceType',
                label: '发票类型',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, row) {
                    if (a == 2) {
                        return "专票";
                    } else if (a == 1) {
                        return "普票";
                    } else if (a == 0) {
                        return "待定";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'taxType',
                index: 'taxType',
                label: '抬头',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'invoiceTitle',
                index: 'invoiceTitle',
                label: '公司名称',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'taxCode',
                index: 'taxCode',
                label: '税号',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'address',
                index: 'address',
                label: '公司地址',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'phone',
                index: 'phone',
                label: '公司电话',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            }, {
                name: 'bankName',
                index: 'bankName',
                label: '开户行',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'bankNo',
                index: 'bankNo',
                label: '银行账号',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
        ],
        pager: jQuery("#user_history_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#company_track_table").jqGrid({
        url: '/crm/company/trackList',
        datatype: "local",
        mtype: 'POST',
        // postData: null, //发送数据
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
        multiselect: false,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 5,//每页显示记录数
        rowList: [5, 10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                label: '序号',
                editable: false,
                width: 0,
                align: "center",
                sortable: false,
                hidden: true,
            },
            {
                name: 'affixName',
                index: 'affixName',
                label: '附件名称',
                editable: false,
                width: 50,
                align: "center",
                sortable: false,
                hidden: true,
                formatter: function (affixName) {
                    $("#affixName").val(affixName);
                }
            },
            {
                name: 'name',
                index: 'name',
                label: '跟进人',
                value: 'creator',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'companyName',
                index: 'companyName',
                label: '客户公司',
                editable: false,
                width: 160,
                align: "center",
                sortable: false
            },
            {
                name: 'companyUserName',
                index: 'companyUserName',
                label: '对接人',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'trackTime',
                index: 'trackTime',
                label: '跟进时间',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'content',
                index: 'content',
                label: '跟进描述',
                editable: false,
                width: 320,
                align: "center",
                sortable: false,
            },
            {
                name: 'affixLink',
                index: 'affixLink',
                label: '附件',
                editable: false,
                width: 160,
                align: "center",
                sortable: false,
                formatter: function (affixLink) {
                    var html = "";
                    var fileName = $("#affixName").val();
                    if (fileName != null) {
                        var affixName = fileName.split(',');
                    } else {
                        return html;
                    }
                    if (affixLink != null) {
                        var Link = affixLink.split(',');
                        if (affixName.length > 0 && Link.length > 0) {
                            for (var i = 0; i < Link.length; i++) {
                                var filePath = Link[i];
                                var fileName = affixName[i].trim();
                                if (filePath === "") continue;
                                html += "<a href=" + filePath + " style=float:left;width:100%  target=_blank download='" + fileName + "'>" + fileName + "</a>";
                            }
                        }
                    }
                    return html;
                }
            },
            {
                name: 'imageLink',
                index: 'imageLink',
                label: '沟通截图',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (imageLink) {
                    var html = "";
                    if (imageLink == null || imageLink == "") {
                        html = "暂无截图";
                        return html;
                    }
                    if (imageLink != null) {
                        var Link = imageLink.split(',');
                        if (Link.length > 0) {
                            // html += "<a class='text-success' onclick='openCarouselImage(\""+ Link +"\",\"imgCarouselModal\")'>查看截图详情&nbsp;</a>";
                            html += "<img src='" + Link[0] + "' height='61.8px' width='100px' onclick='openCarouselImage(\"" + Link + "\",\"imgCarouselModal\")'>";
                        }
                    }
                    return html;
                }
            },
        ],
        pager: jQuery("#company_track_pager"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#view_product_table_logs").jqGrid({
        url: '/crm/company/productList',
        datatype: "local",
        mtype: 'POST',
        // postData: null, //发送数据
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
        rowNum: 5,//每页显示记录数
        rowList: [5, 10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                label: 'id',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'category',
                index: 'category',
                label: '所属品类',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'suitUsers',
                index: 'suitUsers',
                label: '适用人群',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'suitScene',
                index: 'suitScene',
                label: '使用场景',
                editable: false,
                width: 180,
                align: "center",
                sortable: false
            },
            {
                name: 'price',
                index: 'price',
                label: '市场定价',
                editable: false,
                width: 120,
                align: "center",
                sortable: false,
            },
            {
                name: 'packStyle',
                index: 'packStyle',
                label: '包装风格',
                editable: false,
                width: 180,
                align: "center",
                sortable: false
            },
            {
                name: 'func',
                index: 'func',
                label: '功能',
                editable: false,
                width: 180,
                align: "center",
                sortable: false
            },
            {
                name: 'creator',
                index: 'creator',
                label: '创建人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'createTime',
                index: 'createTime',
                label: '创建时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'updateUserName',
                index: 'updateUserName',
                label: '修改人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'updateTime',
                index: 'updateTime',
                label: '修改时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            // {
            //     name: 'operator',
            //     label: '操作',
            //     editable: false,
            //     width: 120,
            //     align: "center",
            //     sortable: false,
            //     formatter: function (a, b, rowdata) {
            //         var html = "";
            //         html += "<button class='btn btn-xs btn-success btn-outline' type='button' title='历史记录' onclick='productObj.changeRecord("+rowdata.id+", 1);'>历史记录</button>";
            //         return html;
            //     }
            // }
        ],
        pager: jQuery("#view_product_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });
    $("#view_consumer_table_logs").jqGrid({
        url: '/crm/company/consumerList',
        datatype: "local",
        mtype: 'POST',
        // postData: null, //发送数据
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
        rowNum: 5,//每页显示记录数
        rowList: [5, 10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                label: 'id',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'age',
                index: 'age',
                label: '年龄',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'sex',
                index: 'sex',
                label: '性别',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'area',
                index: 'area',
                label: '地域分布',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'education',
                index: 'education',
                label: '文化水平',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'topic',
                index: 'topic',
                label: '流行话题',
                editable: false,
                width: 200,
                align: "center",
                sortable: false,
            },
            {
                name: 'job',
                index: 'job',
                label: '职业',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'hobby',
                index: 'hobby',
                label: '爱好',
                editable: false,
                width: 200,
                align: "center",
                sortable: false
            },
            {
                name: 'creator',
                index: 'creator',
                label: '创建人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'createTime',
                index: 'createTime',
                label: '创建时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'updateUserName',
                index: 'updateUserName',
                label: '修改人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'updateTime',
                index: 'updateTime',
                label: '修改时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            // {
            //     name: 'operator',
            //     label: '操作',
            //     editable: false,
            //     width: 120,
            //     align: "center",
            //     sortable: false,
            //     formatter: function (a, b, rowdata) {
            //         var html = "";
            //         html += "<button class='btn btn-xs btn-success btn-outline' type='button' title='历史记录' onclick='consumerObj.changeRecord("+rowdata.id+", 1);'>历史记录</button>";
            //         return html;
            //     }
            // }
        ],
        pager: jQuery("#view_consumer_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });
    $("#edit_product_table_logs").jqGrid({
        url: '/crm/company/productList',
        datatype: "local",
        mtype: 'POST',
        // postData: null, //发送数据
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
        rowNum: 5,//每页显示记录数
        rowList: [5, 10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                label: 'id',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'category',
                index: 'category',
                label: '所属品类',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'suitUsers',
                index: 'suitUsers',
                label: '适用人群',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'suitScene',
                index: 'suitScene',
                label: '使用场景',
                editable: false,
                width: 180,
                align: "center",
                sortable: false
            },
            {
                name: 'price',
                index: 'price',
                label: '市场定价',
                editable: false,
                width: 120,
                align: "center",
                sortable: false,
            },
            {
                name: 'packStyle',
                index: 'packStyle',
                label: '包装风格',
                editable: false,
                width: 180,
                align: "center",
                sortable: false
            },
            {
                name: 'func',
                index: 'func',
                label: '功能',
                editable: false,
                width: 180,
                align: "center",
                sortable: false
            },
            {
                name: 'creator',
                index: 'creator',
                label: '创建人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'createTime',
                index: 'createTime',
                label: '创建时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'updateUserName',
                index: 'updateUserName',
                label: '修改人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'updateTime',
                index: 'updateTime',
                label: '修改时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'operator',
                label: '操作',
                editable: false,
                width: 256,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    html += "<button class='btn btn-xs btn-success btn-outline' type='button' title='编辑' onclick='productObj.view(" + rowdata.id + ", 1);'>&nbsp;编辑&nbsp;</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    html += "<button class='btn btn-xs btn-default btn-outline' type='button' title='查看' onclick='productObj.view(" + rowdata.id + ", 0);'>&nbsp;查看&nbsp;</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    html += "<button class='btn btn-xs btn-danger btn-outline' type='button' title='删除' onclick='productObj.del(" + rowdata.id + ");'>&nbsp;删除&nbsp;</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    return html;
                }
            }
        ],
        pager: jQuery("#edit_product_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });
    $("#edit_consumer_table_logs").jqGrid({
        url: '/crm/company/consumerList',
        datatype: "local",
        mtype: 'POST',
        // postData: null, //发送数据
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
        rowNum: 5,//每页显示记录数
        rowList: [5, 10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                label: 'id',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'age',
                index: 'age',
                label: '年龄',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'sex',
                index: 'sex',
                label: '性别',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'area',
                index: 'area',
                label: '地域分布',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'education',
                index: 'education',
                label: '文化水平',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'topic',
                index: 'topic',
                label: '流行话题',
                editable: false,
                width: 200,
                align: "center",
                sortable: false,
            },
            {
                name: 'job',
                index: 'job',
                label: '职业',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'hobby',
                index: 'hobby',
                label: '爱好',
                editable: false,
                width: 200,
                align: "center",
                sortable: false
            },
            {
                name: 'creator',
                index: 'creator',
                label: '创建人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'createTime',
                index: 'createTime',
                label: '创建时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'updateUserName',
                index: 'updateUserName',
                label: '修改人',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'updateTime',
                index: 'updateTime',
                label: '修改时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'operator',
                label: '操作',
                editable: false,
                width: 256,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    html += "<button class='btn btn-xs btn-success btn-outline' type='button' title='编辑' onclick='consumerObj.view(" + rowdata.id + ", 1);'>&nbsp;编辑&nbsp;</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    html += "<button class='btn btn-xs btn-default btn-outline' type='button' title='查看' onclick='consumerObj.view(" + rowdata.id + ", 0);'>&nbsp;查看&nbsp;</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    html += "<button class='btn btn-xs btn-danger btn-outline' type='button' title='删除' onclick='consumerObj.del(" + rowdata.id + ");'>&nbsp;删除&nbsp;</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    return html;
                }
            }
        ],
        pager: jQuery("#edit_consumer_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#protect_salesman_table_logs").jqGrid({
        url: '/crm/company/queryUserByCompanyId',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#queryForm").serializeJson(), //发送数据
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
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
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
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'mobile',
                index: 'mobile',
                label: '手机号',
                editable: false,
                width: 130,
                align: "center",
                sortable: false
            },
            {
                name: 'wechat',
                index: 'wechat',
                label: '微信',
                editable: false,
                width: 130,
                align: "center",
                sortable: false
            },
            {
                name: 'qq',
                index: 'qq',
                label: 'QQ',
                editable: false,
                width: 130,
                align: "center",
                sortable: false
            },
            {
                name: 'userName',
                index: 'userName',
                label: '负责人',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: false
            },
            {
                name: 'deptName',
                index: 'deptName',
                label: '所在部门',
                editable: false,
                width: 150,
                align: "center",
                sortable: false,
                hidden: false
            }
        ],
        pager: jQuery("#protect_salesman_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });
    $("#protect_table_logs").jqGrid({
        url: '/crm/company/listProtect',
        datatype: "local",
        mtype: 'POST',
        // postData: {companyId: companyId}, //发送数据
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
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                label: 'id',
                editable: false,
                width: 35,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true,
            },
            {
                name: 'companyId',
                index: 'companyId',
                label: '公司id',
                editable: false,
                width: 35,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true,
            },
            {
                name: 'companyName',
                index: 'companyName',
                label: '公司名称',
                editable: false,
                width: 140,
                align: "center",
                sortable: false
            },
            {
                name: 'protectLevel',
                index: 'protectLevel',
                label: '申请类别',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    if (a == 3) {
                        return "<span style='color: #00b7ee'>A类</span>";
                    } else if (a == 2) {
                        return "<span style='color: #0a7e07'>B类</span>";
                    } else {
                        return "<span style='color: #676A6C;'></span>";
                    }
                }
            },
            {
                name: 'applyName',
                index: 'applyName',
                label: '申请人',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'applyDeptName',
                index: 'applyDeptName',
                label: '所在部门',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'applyTime',
                index: 'applyTime',
                label: '申请时间',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'updateUserName',
                index: 'updateUserName',
                label: '更新人',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                hidden: false
            },
            {
                name: 'updateTime',
                index: 'updateTime',
                label: '更新时间',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: false
            },
            {
                name: 'state',
                index: 'state',
                label: '状态',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "<span style='color: #00b7ee'>通过</span>";
                    } else if (a == 2) {
                        return "<span style='color: #0a7e07'>客户管理员审核</span>";
                    } else if (a == 3) {
                        return "<span style='color: #0a7e07'>总经理审核</span>";
                    } else if (a == 4) {
                        return "<span style='color: #0a7e07'>集团总经理审核</span>";
                    } else if (a == -1) {
                        return "<span class='text-red'>驳回</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'operate',
                index: 'operate',
                label: '操作',
                editable: false,
                width: 98,
                align: "center",
                sortable: false,
                formatter: function (value, grid, rows) {
                    var html = "";
                    if (rows.taskId != null && rows.taskId != '') {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='showHistory(" + rows.id + ")'>&nbsp;审核详情&nbsp;</a>";
                    }
                    return html;
                }
            }
        ],
        pager: jQuery("#protect_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#transfer_table_logs").jqGrid({
        url: '/crm/company/transferList',
        datatype: "local",
        mtype: 'POST',
        // postData: {companyId: companyId}, //发送数据
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
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                label: 'id',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'companyName',
                index: 'companyName',
                label: '客户公司名称',
                editable: false,
                width: 160,
                align: "center",
                sortable: false
            },
            {
                name: 'companyUserName',
                index: 'companyUserName',
                label: '对接人',
                editable: false,
                width: 60,
                align: "center",
                sortable: false
            },
            {
                name: 'userName',
                index: 'userName',
                label: '绑定的业务员',
                editable: false,
                width: 80,
                align: "center",
                sortable: false
            },
            {
                name: 'typeIn',
                index: 'typeIn',
                label: '获取客户方式',
                editable: false,
                width: 120,
                align: "center",
                sortable: false,
                formatter: function (value, grid, rows) {
                    var html = "";
                    if (value == 0) {
                        html = "开发客户";
                    } else if (value == 1) {
                        html = "公海认领";
                    } else if (value == 2) {
                        html = "点对点流转";
                    } else if (value == 3) {
                        html = "申请保护获取";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {
                name: 'startTime',
                index: 'startTime',
                label: '获取时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false,
                formatter: function (value, grid, rows) {
                    if (rows.typeIn == 0) {
                        return rows.createTime;
                    } else {
                        return value;
                    }
                }
            },
            /*{
                name: 'name',
                index: 'name',
                label: '失去',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                formatter: function (value, grid, row) {
                    var html = "";
                    if (row.state == 1) {
                        html = "";
                    } else {
                        html = value;
                    }
                    return html;
                }
            },*/
            {
                name: 'typeOut',
                index: 'typeOut',
                label: '失去客户方式',
                editable: false,
                width: 120,
                align: "center",
                sortable: false,
                formatter: function (value, grid, row) {
                    var html = "";
                    if (row.state == 1) {
                        html = "";
                    } else {
                        html = value;
                    }
                    return html;
                }
            },
            {
                name: 'endTime',
                index: 'endTime',
                label: '失去时间',
                editable: false,
                width: 120,
                align: "center",
                sortable: false,
                formatter: function (value, grid, row) {
                    var html = "";
                    if (row.state == 1) {
                        html = "";
                    } else {
                        html = value;
                    }
                    return html;
                }
            },
            {
                name: 'remark',
                index: 'remark',
                label: '备注',
                editable: false,
                width: 158,
                align: "center",
                sortable: false,
                hidden: false
            }
        ],
        pager: jQuery("#transfer_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#view_account_table_logs").jqGrid({
        url: '/account/listPg',
        datatype: "local",
        mtype: 'POST',
        // postData: null, //发送数据
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
        rowNum: 5,//每页显示记录数
        rowList: [5, 10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {name: 'companyName', label: '公司名称', editable: true, width: 120},
            {name: 'contactor', label: '对接人', editable: true, width: 120},
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
                        return "<span style=''></span>"
                    }
                }
            },
            {name: 'phone', label: '预留电话', editable: true, width: 60}
        ],
        pager: jQuery("#view_account_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#edit_account_table_logs").jqGrid({
        url: '/account/listPg',
        datatype: "local",
        mtype: 'POST',
        // postData: null, //发送数据
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
        rowNum: 5,//每页显示记录数
        rowList: [5, 10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {name: 'companyName', label: '公司名称', editable: true, width: 120},
            {name: 'contactor', label: '对接人', editable: true, width: 120},
            {name: 'name', label: '账户名称', editable: true, width: 120},
            {name: 'owner', label: '户主', editable: true, width: 60},
            {name: 'bankNo', label: '账号', editable: true, width: 120},
            {name: 'bankName', label: '开户行', editable: true, width: 120},
            {
                name: 'accountType', label: '类型', editable: true, width: 60,
                formatter: function (a, b, rowdata) {
                    if (a == 'B2B') {
                        return "<span style=''>对公账户</span>"
                    } else if (a == 'B2C') {
                        return "<span style=''>对私账户</span>"
                    } else {
                        return "<span style=''></span>"
                    }
                }
            },
            {name: 'phone', label: '预留电话', editable: true, width: 60},
            {
                name: 'operate', label: "操作", index: '', width: 120,
                formatter: function (value, grid, rows, state) {
                    var html = "";
                    html += "<button class='btn btn-xs btn-success btn-outline' type='button' title='编辑' onclick='accountObj.view(" + rows.id + ", 1);'>&nbsp;编辑&nbsp;</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    html += "<button class='btn btn-xs btn-default btn-outline' type='button' title='查看' onclick='accountObj.view(" + rows.id + ", 0);'>&nbsp;查看&nbsp;</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    html += "<button class='btn btn-xs btn-danger btn-outline' type='button' title='删除' onclick='accountObj.del(" + rows.id + ");'>&nbsp;删除&nbsp;</button>&nbsp;&nbsp;&nbsp;&nbsp;";
                    return html;
                }
            }
        ],
        pager: jQuery("#edit_account_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });
});

var gridObj = {
    //公共调用方法合并单元格（无需修改）
    MergerRowspan: function (gridName, cellName) {
        //得到显示到界面的id集合
        var mya = $("#" + gridName + "").getDataIDs();
        //当前显示多少条
        var length = mya.length;
        for (var i = 0; i < length; i++) {
            //从上到下获取一条信息
            var before = $("#" + gridName + "").jqGrid('getRowData', mya[i]);
            //定义合并行数
            var rowSpanTaxCount = 1;
            for (j = i + 1; j <= length; j++) {
                //和上边的信息对比 如果值一样就合并行数+1 然后设置rowspan 让当前单元格隐藏
                var end = $("#" + gridName + "").jqGrid('getRowData', mya[j]);
                if (before[CellName] == end[CellName]) {
                    rowSpanTaxCount++;
                    $("#" + gridName + "").setCell(mya[j], CellName, '', {display: 'none'});
                } else {
                    rowSpanTaxCount = 1;
                    break;
                }
                $("#" + CellName + "" + mya[i] + "").attr("rowspan", rowSpanTaxCount);
            }
        }
    },
    //公共调用方法合并单元格（根据需要修改）
    MergerColSpan: function (gridName, cellName, primaryKey) {
        //得到显示到界面的id集合
        var ids = $("#" + gridName).getDataIDs();
        //当前显示多少条
        var length = ids.length;
        for (var i = 0; i < length; i++) {
            //从上到下获取一条信息
            var before = $("#" + gridName).jqGrid('getRowData', ids[i]);
            //定义合并行数
            var rowSpanTaxCount = 1;
            for (var j = i + 1; j <= length; j++) {
                //和上边的信息对比 如果值一样就合并行数+1 然后设置rowspan 让当前单元格隐藏
                var end = $("#" + gridName).jqGrid('getRowData', ids[j]);
                if (before[primaryKey] == "")
                    break;
                if (before[primaryKey] == end[primaryKey]) {
                    rowSpanTaxCount++;
                    //$("#" + gridName).setCell(ids[j], cellName, '', {display: 'none'});
                    $("#" + cellName + ids[j]).hide();
                } else {
                    rowSpanTaxCount = 1;
                    break;
                }
                $("#" + cellName + ids[i]).attr("rowspan", rowSpanTaxCount);
            }
        }
    },

    saveData: function (gridName, item) {
        array.push(item);
    },
    deleteData: function (gridName, item) {
        for (var i = 0; i < array.length; i++) {
            if (array[i] == item) {
                array.splice(i, 1);
            }
        }
    },

    reloadCompany: function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        // $("#query_table_logs").reloadCurrentData(baseUrl + "/crm/company/list", $("#queryForm").serializeJson(), "json", null, null);
    },

    reloadCompanyHistory: function (companyId) {
        $("#company_history_table_logs").emptyGridParam();
        $("#company_history_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyId: companyId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#company_history_table_logs");
    },

    reloadUserHistory: function (companyUserId) {
        $("#user_history_table_logs").emptyGridParam();
        $("#user_history_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyUserId: companyUserId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#user_history_table_logs");
    },

    reloadProductEdit: function (companyId) {
        $("#edit_product_table_logs").emptyGridParam();
        $("#edit_product_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyId: companyId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#edit_product_table_logs");
    },
    reloadConsumerEdit: function (companyId) {
        $("#edit_consumer_table_logs").emptyGridParam();
        $("#edit_consumer_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyId: companyId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#edit_consumer_table_logs");
    },

    reloadProductView: function (companyId) {
        $("#view_product_table_logs").emptyGridParam();
        $("#view_product_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyId: companyId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#view_product_table_logs");
    },
    reloadConsumerView: function (companyId) {
        $("#view_consumer_table_logs").emptyGridParam();
        $("#view_consumer_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyId: companyId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#view_consumer_table_logs");
    },
    reloadSalesman: function (companyId) {
        $("#protect_salesman_table_logs").emptyGridParam();
        $("#protect_salesman_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyId: companyId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#protect_salesman_table_logs");
    },
    reloadTrack: function (companyUserId) {
        $("#company_track_table").emptyGridParam();
        $("#company_track_table").jqGrid('setGridParam', {
            datatype: "json",
            postData: {
                companyUserId: companyUserId
            }, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#company_track_table");
    },

    reloadProtect: function (companyId) {
        $("#protect_table_logs").emptyGridParam();
        $("#protect_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyId: companyId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#protect_table_logs");
    },

    reloadTransfer: function (companyUserId) {
        $("#transfer_table_logs").emptyGridParam();
        $("#transfer_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyUserId: companyUserId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#transfer_table_logs");
    },

    reloadAccountEdit: function (companyUserId) {
        $("#edit_account_table_logs").emptyGridParam();
        $("#edit_account_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {dockingIdQc: companyUserId, typeQc: 3, companyCode: user.companyCode}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#edit_account_table_logs");
    },

    reloadAccountView: function (companyUserId) {
        $("#view_account_table_logs").emptyGridParam();
        $("#view_account_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {dockingIdQc: companyUserId, typeQc: 3, companyCode: user.companyCode}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#view_account_table_logs");
    },
};

var companyObj = {
    companySearchUrl: "/company/companySearch",
    checkCompanyUrl: "/company/checkCompany",
    currentCompanyName: "",
    regexStr: /^[1]([3-9])[0-9]{9}$/,
    firstPageTotal: 0, //第一页查询缓存表数据总数
    renderCompanyItem: function (page, pageSize, companyList) {
        var html = "";
        if (companyList && companyList.length > 0) {
            $.each(companyList, function (m, company) {
                html += "<div onmousedown='companyObj.chooseCompany(this);' class=\"companyNameItem\" title=\"" + (company.companyName || "") + "\"><span>" + (company.companyName || "") + "</span></div>";
            });
        }
        return html;
    },
    search: function (t) {
        $(t).closest("form").find(".companyWrap").html("");
        var keyword = $(t).closest("form").find("input[name='companyName']").val();
        if (!keyword) {
            if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
                $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
            }
            return;
        } else {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
        }
        layui.use('flow', function () {
            var flow = layui.flow;
            flow.load({
                elem: $(t).closest("form").find(".companyWrap"),
                isAuto: false,
                done: function (page, next) {
                    //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                    //请求数据
                    var param = {keyword: keyword};
                    param.page = page; //页码
                    param.size = 20; //每页数据条数
                    requestData(param, companyObj.companySearchUrl, "post", function (data) {
                        //第一页是从缓存表拿数据，记录数据总数
                        if (page == 1) {
                            companyObj.firstPageTotal = data.total;
                        }
                        next(companyObj.renderCompanyItem(page, param.size, data.list), page < data.pages); //如果小于总页数，则继续
                    });
                }
            });
        });
    },
    enterEvent: function (t, event) {
        if ((event.keyCode == '13' || event.keyCode == 13)) {
            companyObj.search(t);
        }
    },
    checkCompany: function (t) {
        var keyword = $(t).closest("form").find("input[name='companyName']").val();
        if (!keyword) {
            $(t).closest("form").find(".companyTipsYes").hide();
            $(t).closest("form").find(".companyTipsNo").hide();
            $(t).closest("form").find("input[name='standardize']").val(0);
            return;
        }
        //如果是个体工商户，不需要校验
        if ($(t).closest("form").find("input[name='custProperty']").val() == 1) {
            requestData({keyword: keyword}, companyObj.checkCompanyUrl, "post", function (data) {
                if (data.code == 200) {
                    $(t).closest("form").find(".companyTipsYes").show();
                    $(t).closest("form").find(".companyTipsNo").hide();
                    $(t).closest("form").find("input[name='standardize']").val(1);
                } else {
                    $(t).closest("form").find(".companyTipsYes").hide();
                    $(t).closest("form").find(".companyTipsNo").show();
                    $(t).closest("form").find("input[name='standardize']").val(0);
                }
            });
        }
        // if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
        //     $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        // }
    },
    chooseCompany: function (t) {
        $(t).closest("form").find("input[name='companyName']").val($(t).attr("title") || "");
        //隐藏弹出筛选框
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOut: function (t) {
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOver: function (t) {
        //如果有内容则展示
        if ($(t).closest("form").find(".companyWrap").find("div").length > 0) {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
            $(t).closest("form").find("input[name='companyName']").focus();
        }
    },
    //验证电话号码格式
    regexPhone: function (t) {
        if (!(companyObj.regexStr.test($(t).val()))) {
            $("#normalize").val(0);
            $("#companyUserTipsYes").hide();
            $("#companyUserTipsNo").show();
        } else {
            $("#normalize").val(1);
            $("#companyUserTipsYes").show();
            $("#companyUserTipsNo").hide();
        }
    },

    dropup: function (t) {
        $(t).hide();
        $(t).parent().find(".glyphicon-chevron-up").parent().show();
        $(t).parent().nextAll().hide();
    },

    dropdown: function (t) {
        $(t).hide();
        $(t).parent().find(".glyphicon-chevron-down").parent().show();
        $(t).parent().nextAll().show();
    },

    natureClick: function (t, val, companyName) {
        $(t).closest("div").find("button").each(function (i, btn) {
            if ($(btn).attr("id") === $(t).attr("id")) {
                if ($(btn).hasClass("btn-white")) {
                    $(btn).removeClass("btn-white");
                }
                if (!$(btn).hasClass("btn-info")) {
                    $(btn).addClass("btn-info");
                }
            } else {
                if ($(btn).hasClass("btn-info")) {
                    $(btn).removeClass("btn-info");
                }
                if (!$(btn).hasClass("btn-white")) {
                    $(btn).addClass("btn-white");
                }
            }
        });
        $("#custProperty").val(val);
        $("#addCompanyForm [name='companyName']").val(companyName);
        if (val == 1) {
            $("#addCompanyForm [name='companyName']").removeAttr("disabled");
            if (companyName != null && companyName != "" && companyName != undefined) {
                requestData({keyword: $("#addCompanyForm [name='companyName']").val()}, companyObj.checkCompanyUrl, "post", function (data) {
                    if (data.code == 200) {
                        $("#companyTipsYes").show();
                        $("#companyTipsNo").hide();
                        $("#standardize").val("1");
                    } else {
                        $("#companyTipsYes").hide();
                        $("#companyTipsNo").show();
                        $("#standardize").val("0");
                    }
                });
            } else {
                $("#companyTipsYes").hide();
                $("#companyTipsNo").hide();
            }
        } else {
            $("#standardize").val("0");//个人公司都是非标准
            $("#addCompanyForm [name='companyName']").attr("disabled", "disabled");
            $("#companyTipsYes").hide();
            $("#companyTipsNo").show();
        }
    },

    addCompany: function (t) {
        document.getElementById("addCompanyForm").reset();
        $("#addCompanyForm").find("input").removeClass('error');//清除验证标签
        $("#addCompanyForm").validate().resetForm();
        $("#addCompanyForm").find(".btn").each(function (index, item) {
            $(item).removeAttr("disabled");
        });
        $("#addCompanyForm input[name='mobile']").removeAttr("readonly");
        $("#enterpriseBtn").trigger("click");
        $("#companyTipsYes").hide();
        $("#companyTipsNo").hide();
        $("#companyUserTipsYes").hide();
        $("#companyUserTipsNo").hide();
        $("#saveCompanyUserBtn").show();
        $("#updateCompanyBasicBtn").hide();
        $("#addModal .modal-title").html("客户登记");
        $("#addModal").modal({backdrop: "static"});
    },

    editCompanyBasic: function (companyUserId) {
        $.ajax({
            type: "get",
            url: "/crm/company/getBasicById",
            data: {id: companyUserId},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    if (data.data.entity != null) {
                        if (data.data.entity['auditFlag'] == 1) {
                            swal("该客户保护审核中，无法修改客户信息，请等待审核完成后重试！");
                            return;
                        }
                        document.getElementById("addCompanyForm").reset();
                        $("#addCompanyForm").find("input").removeClass('error');//清除验证标签
                        $("#addCompanyForm").validate().resetForm();
                        $("#saveCompanyUserBtn").hide();
                        $("#updateCompanyBasicBtn").show();
                        $("#addModal .modal-title").html("修改客户基本信息");
                        $("#addModal").modal({backdrop: "static"});
                        for (var attr in data.data.entity) {
                            $("#addCompanyForm [name='" + attr + "']").val(data.data.entity[attr]);
                        }
                        var custProperty = data.data.entity['custProperty'];
                        if (custProperty == 1) {//企业客户
                            companyObj.natureClick($("#enterpriseBtn"), custProperty, data.data.entity['companyName']);
                            $("#addCompanyForm").find(".btn").each(function (index, item) {
                                $(item).attr("disabled", "disabled");
                            });
                            if (data.data.entity['standardize'] == 1) {
                                // if (user.dept.code == 'YW') {
                                //     $("#addCompanyForm [name='companyName']").attr("disabled", "disabled");
                                // }
                                $("#companyTipsYes").show();
                                $("#companyTipsNo").hide();
                            } else {
                                // $("#addCompanyForm [name='companyName']").removeAttr("disabled");
                                $("#companyTipsYes").hide();
                                $("#companyTipsNo").show();
                            }
                        } else {//个人客户
                            // $("#companyTipsYes").hide();
                            // $("#companyTipsNo").show();
                            $("#addCompanyForm").find(".btn").each(function (index, item) {
                                $(item).removeAttr("disabled");
                            });
                            companyObj.natureClick($("#personalBtn"), custProperty, data.data.entity['companyName']);
                        }

                        if (data.data.entity['normalize'] == 1) {
                            // if (user.dept.code == 'YW') {
                            //     $("#addCompanyForm [name='mobile']").attr("readonly", "readonly");
                            // }
                            $("#companyUserTipsYes").show();
                            $("#companyUserTipsNo").hide();
                        } else {
                            // if (user.dept.code == 'YW') {
                            //     $("#addCompanyForm [name='mobile']").removeAttr("readonly");
                            // }
                            $("#companyUserTipsYes").hide();
                            $("#companyUserTipsNo").show();
                        }
                    }
                }
            }
        })
    },

    editCompany: function (companyId, companyUserId) {
        $("#companyUserId").val(companyUserId);
        $.ajax({
            type: "get",
            url: "/crm/company/view",
            data: {id: companyId},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    if (data.data.entity != null) {
                        if (data.data.entity['auditFlag'] == 1) {
                            swal("该客户保护审核中，无法修改客户信息，请等待审核完成后重试！");
                            return;
                        }
                        $("#editModal").modal({backdrop: "static"});
                        document.getElementById("editCompanyForm").reset();
                        $("#editCompanyForm").find("input").removeClass('error');//清除验证标签
                        $("#editCompanyForm").validate().resetForm();
                        editNav.stepOne();
                        for (var attr in data.data.entity) {
                            $("#editCompanyForm [name='" + attr + "'][type!='radio'][type!='file']").val(data.data.entity[attr]);
                        }

                        $("#editCompanyForm input:radio").removeAttr("checked");
                        $("#editCompanyForm input:radio").parent().removeClass("checked");
                        $("#editCompanyForm input[name='type']").attr("disabled", "disabled");
                        $("#editCompanyForm input[name='type']").parent().addClass("disabled");
                        $("#editCompanyForm input[name='type'][value='" + data.data.entity['type'] + "']").attr("checked", "checked");
                        $("#editCompanyForm input[name='type'][value='" + data.data.entity['type'] + "']").parent().addClass("checked");

                        companyType.initCompanyType("typeCode", "typeName", "typeNameDiv", data.data.entity['typeCode'], data.data.entity['typeName']);
                        $("#area").empty();
                        Views.loadDistrict("area", data.data.entity['area']);
                        $("#industry").empty();
                        Views.loadIndustry("industry", data.data.entity['industry']);

                        var filePath = data.data.entity['image'];
                        $("#logoDiv").empty();
                        if (filePath != null && filePath != "") {
                            var html = "<img src='" + filePath + "' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                            $("#logoDiv").append(html);
                        }

                        setTimeout(function () {
                            gridObj.reloadProductEdit(companyId);
                            gridObj.reloadConsumerEdit(companyId);
                        }, 500);
                    }
                }
            }
        })
    },

    saveCommon: function (t) {
        var message = "企业客户公司名称：";
        var custProperty = $("#addCompanyForm [name='custProperty']").val();
        if (custProperty == 1) {
            requestData({keyword: $("#addCompanyForm [name='companyName']").val()}, companyObj.checkCompanyUrl, "post", function (data) {
                if (data.code == 200) {
                    $("#companyTipsYes").show();
                    $("#companyTipsNo").hide();
                    $("#standardize").val("1");
                    message += "<strong>标准</strong>；";
                } else {
                    $("#companyTipsYes").hide();
                    $("#companyTipsNo").show();
                    $("#standardize").val("0");
                    message += "<strong class='text-red'>非标准</strong>；";
                }
            });
        } else {
            message += "您选择的修改的是个人客户，个人客户公司名称统一为个体工商户且公司名称非标准；";
        }
        message += "手机号码：";
        var mobile = $("#addCompanyForm [name='mobile']").val();
        if (!(companyObj.regexStr.test(mobile))) {
            $("#normalize").val("0");
            $("#companyUserTipsYes").hide();
            $("#companyUserTipsNo").show();
            message += "<strong class='text-red'>非规范</strong>";
        } else {
            $("#normalize").val("1");
            $("#companyUserTipsYes").show();
            $("#companyUserTipsNo").hide();
            message += "<strong>规范</strong>！";
        }
        return message;
    },
    saveCompany: function (t, url) {
        if ($("#addCompanyForm [name='custProperty']").val() == 0) {
            $("#addCompanyForm [name='companyName']").val("个体工商户")
        } else {
            if ($("#addCompanyForm [name='companyName']").val() == "个体工商户") {
                swal("企业客户公司名称不能是个体工商户，请修改后重试！");
                return;
            }
        }
        if ($("#addCompanyForm").valid()) {
            var message = companyObj.saveCommon(t);
            var lock = true;
            layer.confirm(message, {
                title: "请确认信息是否填写正确！",
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        url: url,
                        data: $("#addCompanyForm").serializeJson(),
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                $("#addModal").modal("hide");
                                swal("操作成功！");
                                gridObj.reloadCompany();
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },

    updateCompanyBasic: function (t, url) {
        if ($("#addCompanyForm [name='custProperty']").val() == 0) {
            $("#addCompanyForm [name='companyName']").val("个体工商户")
        } else {
            if ($("#addCompanyForm [name='companyName']").val() == "个体工商户") {
                swal("企业客户公司名称不能是个体工商户，请修改后重试！");
                return;
            }
        }
        if ($("#addCompanyForm").valid()) {
            var message = companyObj.saveCommon(t);
            var lock = true;
            var formData = $("#addCompanyForm").serializeJson();
            layer.confirm(message, {
                title: "请确认信息是否填写正确！",
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        url: url,
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                $("#addModal").modal("hide");
                                swal("操作成功！");
                                gridObj.reloadCompany();
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },

    updateCompany: function (t, url) {
        if ($("#editCompanyForm").valid()) {
            var lock = true;
            var formData = new FormData($("#editCompanyForm")[0]);//序列化当前表单，并传出file类型
            layer.confirm("请确认公司信息是否正确！", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        url: baseUrl + url,
                        data: formData,
                        dataType: "json",
                        async: true,
                        cache: false,
                        contentType: false,
                        processData: false,
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                companyObj.editCompanyUser();
                                layer.msg("操作成功");
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },

    viewCompanyAll: function (companyId, companyUserId) {
        $.ajax({
            type: "get",
            url: "/crm/company/viewCompanyAll",
            data: {companyId: companyId, companyUserId: companyUserId},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    $("#viewCompanyModal").modal({backdrop: "static"});
                    document.getElementById("viewCompanyForm").reset();
                    $("#viewCompanyForm").find("input").removeClass('error');//清除验证标签
                    $("#viewCompanyForm").validate().resetForm();
                    if (data.data != null) {
                        if (data.data.company != null) {
                            for (var attr in data.data.company) {
                                $("#viewCompanyForm [name='" + attr + "'][type!='radio'][type!='file']").val(data.data.company[attr]);
                            }

                            var filePath = data.data.company['image'];
                            $("#viewLogoDiv").empty();
                            if (filePath != null && filePath != "") {
                                var html = "<img src='" + filePath + "' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                $("#viewLogoDiv").append(html);
                            }
                        }
                        if (data.data.companyUser != null) {
                            for (var attr in data.data.companyUser) {
                                $("#viewCompanyUserForm [name=" + attr + "][type!='radio'][type!='file']").prop("style", "border:0;");
                                $("#viewCompanyUserForm [name='" + attr + "'][type!='radio'][type!='file']").val(data.data.companyUser[attr]);
                            }

                            $("#viewCompanyUserForm input:radio").removeAttr("checked");
                            $("#viewCompanyUserForm input:radio").parent().removeClass("checked");
                            $("#viewCompanyUserForm input[name='custType']").attr("disabled", "disabled");
                            $("#viewCompanyUserForm input[name='custType']").parent().addClass("disabled");
                            $("#viewCompanyUserForm input[name='intention']").attr("disabled", "disabled");
                            $("#viewCompanyUserForm input[name='intention']").parent().addClass("disabled");
                            $("#viewCompanyUserForm input[name='invoiceType']").attr("disabled", "disabled");
                            $("#viewCompanyUserForm input[name='invoiceType']").parent().addClass("disabled");
                            $("#viewCompanyUserForm input[name='custType'][value='" + data.data.companyUser['custType'] + "']").attr("checked", "checked");
                            $("#viewCompanyUserForm input[name='custType'][value='" + data.data.companyUser['custType'] + "']").parent().addClass("checked");
                            $("#viewCompanyUserForm input[name='intention'][value='" + data.data.companyUser['intention'] + "']").attr("checked", "checked");
                            $("#viewCompanyUserForm input[name='intention'][value='" + data.data.companyUser['intention'] + "']").parent().addClass("checked");
                            $("#viewCompanyUserForm input[name='invoiceType'][value='" + data.data.companyUser['invoiceType'] + "']").attr("checked", "checked");
                            $("#viewCompanyUserForm input[name='invoiceType'][value='" + data.data.companyUser['invoiceType'] + "']").parent().addClass("checked");

                            if(!data.data.ywFlag && !data.data.adminFlag){
                                $("#viewCompanyUserForm [name='mobile']").val("***");
                                $("#viewCompanyUserForm [name='qq']").val("***");
                                $("#viewCompanyUserForm [name='wechat']").val("***");
                            }
                            $("#viewTaxesDiv").prop("style", "border:0;background-color: #FFFFFF");
                            initTax("viewTaxesDiv", data.data.companyUser['taxType']);

                            var filePath = data.data.companyUser['photo'];
                            $("#viewPhotoDiv").empty();
                            if (filePath != null && filePath != "") {
                                var html = "<img src='" + filePath + "' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                $("#viewPhotoDiv").append(html);
                            }
                        }

                        /*if(data.data.YwFlag){
                            document.getElementById("addTrack").style.visibility="visible";//显示
                        }else{
                            document.getElementById("addTrack").style.visibility="hidden";//隐藏
                        }*/
                        setTimeout(function () {
                            gridObj.reloadTrack(companyUserId);
                            gridObj.reloadProductView(companyId);
                            gridObj.reloadConsumerView(companyId);
                            gridObj.reloadAccountView(companyUserId);
                            gridObj.reloadProtect(companyId);
                            gridObj.reloadTransfer(companyUserId);
                        }, 500);
                    }
                }
            }
        })
    },

    editCompanyUser: function () {
        $.ajax({
            type: "get",
            url: "/crm/company/viewCompanyUser",
            data: {id: $("#companyUserId").val()},
            dataType: "json",
            success: function (data) {
                document.getElementById("editCompanyUserForm").reset();
                $("#editCompanyUserForm").find("input").removeClass('error');//清除验证标签
                $("#editCompanyUserForm").validate().resetForm();
                if (data.code == 200) {
                    editNav.stepTwo();
                    if (data.data.entity != null) {
                        for (var attr in data.data.entity) {
                            $("#editCompanyUserForm [name='" + attr + "'][type!='radio'][type!='file']").val(data.data.entity[attr]);
                        }

                        $("#editCompanyUserForm input:radio").removeAttr("checked");
                        $("#editCompanyUserForm input:radio").parent().removeClass("checked");
                        $("#editCompanyUserForm input[name='custType'][value='" + data.data.entity['custType'] + "']").attr("checked", "checked");
                        $("#editCompanyUserForm input[name='custType'][value='" + data.data.entity['custType'] + "']").parent().addClass("checked");
                        $("#editCompanyUserForm input[name='intention'][value='" + data.data.entity['intention'] + "']").attr("checked", "checked");
                        $("#editCompanyUserForm input[name='intention'][value='" + data.data.entity['intention'] + "']").parent().addClass("checked");
                        $("#editCompanyUserForm input[name='invoiceType'][value='" + data.data.entity['invoiceType'] + "']").attr("checked", "checked");
                        $("#editCompanyUserForm input[name='invoiceType'][value='" + data.data.entity['invoiceType'] + "']").parent().addClass("checked");

                        initTax("taxesDiv", data.data.entity['taxType']);

                        var filePath = data.data.entity['photo'];
                        $("#photoDiv").empty();
                        if (filePath != null && filePath != "") {
                            var html = "<img src='" + filePath + "' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                            $("#photoDiv").append(html);
                        }

                        setTimeout(function () {
                            gridObj.reloadAccountEdit(data.data.entity['id']);
                        }, 500);
                    }
                }
            }
        })
    },

    updateCompanyUser: function (t, url) {
        if ($("#editCompanyUserForm").valid()) {
            var lock = true;
            var formData = new FormData($("#editCompanyUserForm")[0]);//序列化当前表单，并传出file类型
            layer.confirm("请确认对接人信息是否正确！", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        url: baseUrl + url,
                        data: formData,
                        dataType: "json",
                        async: true,
                        cache: false,
                        contentType: false,
                        processData: false,
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                swal("操作成功");
                                $("#editModal").modal("hide");
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },

    delCompanyUser: function (companyUserId) {
        var lock = true;
        layer.confirm("确定删除该对接人？删除后无法恢复！", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/crm/company/delCompanyUser",
                    data: {companyUserId: companyUserId},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            swal("操作成功");
                            gridObj.reloadCompany();
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
                    error: function (data) {
                        swal(data.msg);
                    }
                });
            }
        }, function () {
            return;
        })
    },

    addPublic: function (companyUserId) {
        $.ajax({
            type: "get",
            url: "/crm/company/addPublic",
            data: {companyUserId: companyUserId},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    if (data.data.entity != null) {
                        if (data.data.entity['auditFlag'] == 1) {
                            swal("该客户保护审核中，请等待审核完成后重试！");
                            return;
                        }
                        $("#addPublicModal").modal({backdrop: "static"});
                        for (var attr in data.data.entity) {
                            $("#addPublicForm [name='" + attr + "']").val(data.data.entity[attr]);
                        }
                        var standardize = data.data.entity['standardize'];
                        var normalize = data.data.entity['normalize'];
                        var protectLevel = data.data.entity['protectLevel'];
                        if (standardize == 1) {
                            document.getElementById("viewPublicCompanyUserTipsYes").style.display = "";
                            document.getElementById("viewPublicCompanyUserTipsNo").style.display = "none";
                        } else {
                            document.getElementById("viewPublicCompanyUserTipsNo").style.display = "";
                            document.getElementById("viewPublicCompanyUserTipsYes").style.display = "none";
                        }
                        if (normalize == 1) {
                            document.getElementById("viewPublicCompanyTipsYes").style.display = "";
                            document.getElementById("viewPublicCompanyTipsNo").style.display = "none";
                        } else {
                            document.getElementById("viewPublicCompanyTipsNo").style.display = "";
                            document.getElementById("viewPublicCompanyTipsYes").style.display = "none";
                        }
                        switch (protectLevel) {
                            case 3:
                                $("#protectLevel").val("保护（A）");
                                break;
                            case 2:
                                $("#protectLevel").val("保护（B）");
                                break;
                            case 1:
                                $("#protectLevel").val("保护（C）");
                                break;
                            default:
                                $("#protectLevel").val("未保护");
                                break;
                        }
                        var protectStrong = data.data.entity['protectStrong'];
                        if(protectStrong == 1){
                            $("#protectStrong").val("强保护");
                        }else{
                            $("#protectStrong").val("弱保护");
                        }
                    }
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
        })
    },
    savePublic: function (t) {
        var lock = true;
        layer.confirm("确定要把该客户抛入公海？操作后无法找回！", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/crm/company/savePublic",
                    data: {companyUserId: $("#addPublicForm [name='userId']").val()},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            $("#addPublicModal").modal("hide");
                            swal("操作成功");
                            gridObj.reloadCompany();
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
                    error: function (data) {
                        Ladda.stopAll();
                        swal(data.msg);
                    }
                });
            }
        }, function () {
            return;
        })
    },

    addProtect: function (companyId, companyUserId) {
        $.ajax({
            type: "get",
            url: "/crm/company/addProtect",
            data: {companyId: companyId, companyUserId: companyUserId},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    $("#addProtectModal").modal({backdrop: "static"});
                    $("#addProtectDiv").empty();
                    if (data.data.entity != null) {
                        for (var attr in data.data.entity) {
                            $("#addProtectForm [name='" + attr + "']").val(data.data.entity[attr]);
                        }
                        var standardize = data.data.entity['standardize'];
                        var normalize = data.data.entity['normalize'];
                        if (standardize == 1) {
                            document.getElementById("viewCompanyUserTipsYes").style.display = "";
                            document.getElementById("viewCompanyUserTipsNo").style.display = "none";
                        } else {
                            document.getElementById("viewCompanyUserTipsNo").style.display = "";
                            document.getElementById("viewCompanyUserTipsYes").style.display = "none";
                        }
                        if (normalize == 1) {
                            document.getElementById("viewCompanyTipsYes").style.display = "";
                            document.getElementById("viewCompanyTipsNo").style.display = "none";
                        } else {
                            document.getElementById("viewCompanyTipsNo").style.display = "";
                            document.getElementById("viewCompanyTipsYes").style.display = "none";
                        }

                        var protectLevel = data.data.entity['protectLevel'];
                        var html = "";
                        switch (protectLevel) {
                            case 3:
                                //B申请A
                                var remainNumA = data.data.entity['remainNumA'];
                                html = "<!--B申请A开始-->\n" +
                                    "                                <div class=\"form-group\" style=\"margin-bottom:5px\">\n" +
                                    "                                    <label class=\"col-sm-2 control-label\">保护类型<span class=\"text-red\">*</span>:</label>\n" +
                                    "                                    <div class=\"col-sm-2\">\n";
                                if (remainNumA > 0) {
                                    html += "<button type=\"button\" class=\"btn btn-success btn-outline\" id=\"protectA\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;A级&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                                        "</button>\n";
                                } else {
                                    html += "<button type=\"button\" class=\"btn btn-white\" id=\"protectA\" disabled=\"disabled\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;A级&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                                        "</button>\n";
                                }
                                html += "                                    </div>\n" +
                                    "                                    <div class=\"col-sm-2\">\n" +
                                    "                                        <button type=\"button\" class=\"btn btn-white\" id=\"protectB\" disabled=\"disabled\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;B级&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                                    "                                        </button>\n" +
                                    "                                    </div>\n" +
                                    "                                    <div class=\"col-sm-2\">\n" +
                                    "                                        <button type=\"button\" class=\"btn btn-white\" id=\"protectC\" disabled=\"disabled\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;C级&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                                    "                                        </button>\n" +
                                    "                                    </div>\n" +
                                    "                                    <div class=\"col-sm-2\" style=\"padding:8.5px 0 0 5px\">\n" +
                                    "                                        <span data-toggle=\"tooltip\" data-placement=\"left\" data-html=\"true\"\n" +
                                    "                                              title=\"客户保护分为A、B、C三类，A类保护是保护公司名不参加考核需要申请批复，B类保护公司名称需要考核需要申请批复，C类保护是保护公司名称+对接人手机号需要考核不需要申请批复（默认强保护状态的客户就是进行了C类保护，C类保护不需要进行人为操作）\">\n" +
                                    "                                            <i class=\"fa fa-question-circle\"></i>\n" +
                                    "                                        </span>\n" +
                                    "                                    </div>\n" +
                                    "                                </div>\n" +
                                    "                                <div class=\"form-group\" style=\"margin-bottom:0px\" id=\"protectTipsDiv\">\n" +
                                    "                                    <label class=\"col-sm-2 control-label\"></label>\n" +
                                    "                                    <div class=\"col-sm-2\">\n";
                                if (remainNumA > 0) {
                                    html += "<span>剩余保护数：" + data.data.entity['remainNumA'] + "</span>\n";
                                } else {
                                    html += "<span class='text-red'>剩余保护数：" + data.data.entity['remainNumA'] + "</span>\n";
                                }
                                html += "                                    </div>\n" +
                                    "                                    <div class=\"col-sm-2\">\n" +
                                    "                                        <span>当前保护状态</span>\n" +
                                    "                                    </div>\n" +
                                    "                                    <div class=\"col-sm-2\">\n" +
                                    "                                        <span></span>\n" +
                                    "                                    </div>\n" +
                                    "                                </div>\n" +
                                    "                                <!--B申请A结束-->";
                                break;
                            case 2:
                                //C申请B
                                var remainNumB = data.data.entity['remainNumB'];
                                html = "<!--C申请B开始-->\n" +
                                    "                                <div class=\"form-group\" style=\"margin-bottom:5px\">\n" +
                                    "                                    <label class=\"col-sm-2 control-label\">保护类型<span class=\"text-red\">*</span>:</label>\n" +
                                    "                                    <div class=\"col-sm-2\">\n" +
                                    "                                        <button type=\"button\" class=\"btn btn-white\" id=\"protectA\" disabled=\"disabled\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;A级&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                                    "                                        </button>\n" +
                                    "                                    </div>\n" +
                                    "                                    <div class=\"col-sm-2\">\n";
                                if (remainNumB > 0) {
                                    html += "<button type=\"button\" class=\"btn btn-success btn-outline\" id=\"protectB\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;B级&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                                        "</button>\n";
                                } else {
                                    html += "<button type=\"button\" class=\"btn btn-white\" id=\"protectB\" disabled=\"disabled\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;B级&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                                        "</button>\n";
                                }
                                html += "                                   </div>\n" +
                                    "                                    <div class=\"col-sm-2\">\n" +
                                    "                                        <button type=\"button\" class=\"btn btn-white\" id=\"protectC\" disabled=\"disabled\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;C级&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\n" +
                                    "                                        </button>\n" +
                                    "                                    </div>\n" +
                                    "                                    <div class=\"col-sm-2\" style=\"padding:8.5px 0 0 5px\">\n" +
                                    "                                        <span data-toggle=\"tooltip\" data-placement=\"left\" data-html=\"true\"\n" +
                                    "                                              title=\"客户保护分为A、B、C三类，A类保护是保护公司名不参加考核需要申请批复，B类保护公司名称需要考核需要申请批复，C类保护是保护公司名称+对接人手机号需要考核不需要申请批复（默认强保护状态的客户就是进行了C类保护，C类保护不需要进行人为操作）\">\n" +
                                    "                                            <i class=\"fa fa-question-circle\"></i>\n" +
                                    "                                        </span>\n" +
                                    "                                    </div>\n" +
                                    "                                </div>\n" +
                                    "                                <div class=\"form-group\" style=\"margin-bottom:0px\" id=\"protectTipsDiv\">\n" +
                                    "                                    <label class=\"col-sm-2 control-label\"></label>\n" +
                                    "                                    <div class=\"col-sm-2\">\n" +
                                    "                                        <span>未达到保护要求</span>\n" +
                                    "                                    </div>\n" +
                                    "                                    <div class=\"col-sm-2\">\n";
                                if (remainNumB > 0) {
                                    html += "<span>剩余保护数：" + data.data.entity['remainNumB'] + "</span>\n";
                                } else {
                                    html += "<span class='text-red'>剩余保护数：" + data.data.entity['remainNumB'] + "</span>\n";
                                }
                                html += "                                    </div>\n" +
                                    "                                    <div class=\"col-sm-2\">\n" +
                                    "                                        <span>当前保护状态</span>\n" +
                                    "                                    </div>\n" +
                                    "                                </div>\n" +
                                    "                                <!--C申请B结束-->";
                                break;
                            default:
                                swal("该客户不满足申请保护的条件！");
                                break;
                        }
                        $("#addProtectDiv").append(html);
                    }
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
        })
    },

    saveProtect: function (t) {
        var lock = true;
        layer.confirm("确定对该公司申请保护？", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/crm/company/saveProtect",
                    data: $("#addProtectForm").serializeJson(),
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            swal("操作成功");
                            $("#addProtectModal").modal("hide");
                            gridObj.reloadCompany();
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
                    error: function (data) {
                        Ladda.stopAll();
                        swal(data.msg);
                    }
                });
            }
        }, function () {
            return;
        })
    },

    /**
     *
     * @param protectId
     * @param flag 1是审批，0是查看
     */
    viewProtect: function (protectId, flag) {
        $.ajax({
            type: "get",
            url: "/crm/company/viewProtect",
            data: {protectId: protectId},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    $("#viewProtectModal").modal({backdrop: "static"});
                    if (data.data.entity != null) {
                        for (var attr in data.data.entity) {
                            $("#viewProtectForm [name='" + attr + "'][type!='radio'][type!='file']").val(data.data.entity[attr]);
                        }
                        var state = data.data.entity.state;
                        if (state == -1) {
                            $("#stateDesc").val("审批驳回");
                        } else if (state == 1) {
                            $("#stateDesc").val("审批通过");
                        } else if (state == 2) {
                            $("#stateDesc").val("运营人员审批");
                        } else if (state == 3) {
                            $("#stateDesc").val("公司总经理审批");
                        } else if (state == 4) {
                            $("#stateDesc").val("集团总经理审批");
                        } else {
                            $("#stateDesc").val("");
                        }
                        if(flag == 1){
                            $("#protectSalesmanDiv").show();
                        }else{
                            $("#protectSalesmanDiv").hide();
                        }
                        setTimeout(function () {
                            gridObj.reloadSalesman(data.data.entity['companyId']);
                        }, 500);
                    }
                    if (flag == 1) {
                        $("#showHistory1").data("id", data.data.entity['id']);
                        $("#auditFoot").show();
                        $("#auditDiv").show();
                        $("#viewFoot").hide();
                    } else {
                        $("#auditFoot").hide();
                        $("#auditDiv").hide();
                        $("#viewFoot").show();
                        //flag=-1审批驳回，消除待办
                        if(flag == -1){
                            var itemId = data.data.entity["itemId"];
                            requestData({itemId: itemId}, "/items/finishItem", "get", function () {
                                if (data.code == 200) {
                                    //待办处理完成，无需处理
                                } else {
                                    swal("待办处理失败！");
                                }
                            })
                        }
                    }
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
        })
    },

    saveTransfer: function (t) {
        if ($("#addTransferForm").valid()) {
            if ($("#transDeptId").val() == "") {
                swal("未获取到接收业务员的部门！");
                return;
            }
            if ($("#transUserId").val() == "") {
                swal("未获取到接收业务员的姓名！");
                return;
            }
            var lock = true;
            layer.confirm("确定把列表中的客户流转给？", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        url: baseUrl + "/crm/company/saveTransfer",
                        data: $("#addTransferForm").serializeJson(),
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                swal("操作成功");
                                $("#addTransferModal").modal("hide");
                                gridObj.reloadCompany();
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        }
    },

    companyTrack: function (companyId, companyUserId, companyName, companyUserName) {
        document.getElementById("addCompanyTrackForm").reset();
        $("#addCompanyTrackForm").validate().resetForm();
        $("#companyId").val(companyId);
        $("#companyTrackUserId").val(companyUserId);
        $("#trackModal").modal({backdrop: "static"});
        $("#companyTrackName").val(companyName)
        $("#companyTrackUserName").val(companyUserName)
        setTimeout(function () {
            gridObj.reloadCompanyTrack(companyId, companyUserId);
        }, 500);

    }
};

var productObj = {
    common: function () {
        $("#editModal").modal("hide");
        $("#addProductModal").modal({backdrop: "static"});
        document.getElementById("productForm").reset();
        $("#productForm").find("input").removeClass('error');//清除验证标签
        $("#productForm").validate().resetForm();
    },
    add: function () {
        productObj.common();
        $("#addProductBtn").show();
        $("#editProductBtn").hide();
        $("#productForm input").removeAttr("style");
        $("#productForm input[name='companyId']").val($("#id").val());
    },
    view: function (id, flag) {
        if (flag == 0) {//查看
            $("#addProductBtn").hide();
            $("#editProductBtn").hide();
        } else {//编辑
            $("#addProductBtn").hide();
            $("#editProductBtn").show();
        }
        $.ajax({
            type: "get",
            url: "/crm/company/viewProduct",
            data: {productId: id},
            dataType: "json",
            success: function (data) {
                productObj.common();
                if (data.code == 200) {
                    if (data.data.entity != null) {
                        if (flag == 0) {
                            $("#productForm input").attr("style", "border:0;");
                        } else {
                            $("#productForm input").removeAttr("style");
                        }
                        for (var attr in data.data.entity) {
                            $("#productForm input[name='" + attr + "']").val(data.data.entity[attr]);
                        }
                    }
                }
            }
        })
    },
    del: function (id) {
        var lock = true;
        layer.confirm("确定删除该条公司产品记录！", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            if (lock) {
                lock = false;
                $.ajax({
                    type: "get",
                    url: baseUrl + "/crm/company/delProduct",
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            swal("操作成功");
                            gridObj.reloadProductEdit($("#id").val());
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
                    error: function (data) {
                        Ladda.stopAll();
                        swal(data.msg);
                    }
                });
            }
        }, function () {
            return;
        })
    },
    save: function (t) {
        if ($("#productForm").valid()) {
            var lock = true;
            var formData = $("#productForm").serializeJson();//序列化当前表单，并传出file类型
            layer.confirm("请确认公司产品信息是否正确！", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "get",
                        url: baseUrl + "/crm/company/saveProduct",
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                swal("操作成功");
                                $("#addProductModal").modal("hide");
                                gridObj.reloadProductEdit($("#id").val());
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },
    update: function (t) {
        if ($("#productForm").valid()) {
            var lock = true;
            var formData = $("#productForm").serializeJson();//序列化当前表单，并传出file类型
            layer.confirm("请确认公司产品信息是否正确！", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "get",
                        url: baseUrl + "/crm/company/updateProduct",
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                swal("操作成功");
                                $("#addProductModal").modal("hide");
                                gridObj.reloadProductEdit($("#id").val());
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },
};
var trackObj = {
    add: function (companyId, companyName, companyUserId, companyUserName) {
        document.getElementById("addTrackForm").reset();
        $("#addTrackForm").find("input").removeClass('error');//清除验证标签
        $("#addTrackForm").validate().resetForm();

        /*var companyId = $("#viewCompanyForm input[name='id']").val();
        var companyName = $("#viewCompanyForm input[name='name']").val();
        var companyUserId = $("#viewCompanyUserForm input[name='id']").val();
        var companyUserName = $("#viewCompanyUserForm input[name='name']").val();*/

        // $("#addTrackForm input[name='companyId']").val(companyId);
        $("#addTrackForm input[name='companyName']").val(companyName);
        $("#addTrackForm input[name='companyUserId']").val(companyUserId);
        $("#addTrackForm input[name='companyUserName']").val(companyUserName);
        //重载文件域
        var files = document.getElementById('files');
        files.outerHTML = files.outerHTML;
        var pics = document.getElementById('pics');
        pics.outerHTML = pics.outerHTML;

        $("#addTrackModal").modal({backdrop: "static"});
        $("#viewCompanyModal").modal("hide");
    },
    save: function (t) {
        if ($("#addTrackForm").valid()) {
            var lock = true;
            var formData = new FormData($("#addTrackForm")[0]);//序列化当前表单，并传出file类型
            layer.confirm("请确认跟进信息是否正确！", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "POST",
                        url: baseUrl + "/crm/company/saveCompanyTrack",
                        data: formData,
                        dataType: "json",
                        async: true,
                        cache: false,
                        contentType: false,
                        processData: false,
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                swal("操作成功");
                                $("#addTrackModal").modal("hide");
                                // gridObj.reloadTrack($("#addTrackForm [name='companyUserId']").val());
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },
};
var consumerObj = {
    common: function () {
        $("#editModal").modal("hide");
        $("#addConsumerModal").modal({backdrop: "static"});
        document.getElementById("consumerForm").reset();
        $("#consumerForm").find("input").removeClass('error');//清除验证标签
        $("#consumerForm").validate().resetForm();

        $("#consumerForm input:radio").removeAttr("checked");
        $("#consumerForm input:radio").removeAttr("disabled");
        $("#consumerForm input:radio").parent().removeClass("checked disabled");
    },
    add: function () {
        $("#addConsumerBtn").show();
        $("#editConsumerBtn").hide();
        consumerObj.common();
        $("#consumerForm input").removeAttr("style");
        $("#consumerForm input[name='companyId']").val($("#id").val());
    },
    view: function (id, flag) {
        if (flag == 0) {//查看
            $("#addConsumerBtn").hide();
            $("#editConsumerBtn").hide();
        } else {//编辑
            $("#addConsumerBtn").hide();
            $("#editConsumerBtn").show();
        }
        $.ajax({
            type: "get",
            url: "/crm/company/viewConsumer",
            data: {consumerId: id},
            dataType: "json",
            success: function (data) {
                consumerObj.common();
                if (data.code == 200) {
                    if (data.data.entity != null) {
                        for (var attr in data.data.entity) {
                            $("#consumerForm input[name='" + attr + "'][type!='radio']").val(data.data.entity[attr]);
                        }
                        if (flag == 0) {
                            $("#consumerForm input").attr("style", "border:0;");
                        } else {
                            $("#consumerForm input").removeAttr("style");
                        }
                        $("#consumerForm input[name='sex'][value='" + data.data.entity['sex'] + "']").attr("checked", "checked");
                        $("#consumerForm input[name='sex'][value='" + data.data.entity['sex'] + "']").parent().addClass("checked");
                        $("#consumerForm input[name='company_id']").val($("#id").val());
                        if (flag == 0) {
                            $("#consumerForm input[name='sex']").attr("disabled", "disabled");
                            $("#consumerForm input[name='sex']").parent().addClass("disabled");
                        }
                    }
                }
            }
        })
    },
    del: function (id) {
        var lock = true;
        layer.confirm("确定删除该条公司用户群体记录！", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            if (lock) {
                lock = false;
                $.ajax({
                    type: "get",
                    url: baseUrl + "/crm/company/delConsumer",
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            swal("操作成功");
                            gridObj.reloadConsumerEdit($("#id").val());
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
                    error: function (data) {
                        Ladda.stopAll();
                        swal(data.msg);
                    }
                });
            }
        }, function () {
            return;
        })
    },
    save: function (t) {
        if ($("#consumerForm").valid()) {
            var lock = true;
            var formData = $("#consumerForm").serializeJson();
            layer.confirm("请确认公司用户群体信息是否正确！", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "get",
                        url: baseUrl + "/crm/company/saveConsumer",
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                swal("操作成功");
                                $("#addConsumerModal").modal("hide");
                                gridObj.reloadConsumerEdit($("#id").val());
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },
    update: function (t) {
        if ($("#consumerForm").valid()) {
            var lock = true;
            var formData = $("#consumerForm").serializeJson();
            layer.confirm("请确认公司用户群体信息是否正确！", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "get",
                        url: baseUrl + "/crm/company/updateConsumer",
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                swal("操作成功");
                                $("#addConsumerModal").modal("hide");
                                gridObj.reloadConsumerEdit($("#id").val());
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },
};

var accountObj = {
    common: function () {
        $("#editModal").modal("hide");
        $("#addAccountModal").modal({backdrop: "static"});
        document.getElementById("accountForm").reset();
        $("#accountForm").find("input").removeClass('error');//清除验证标签
        $("#accountForm").validate().resetForm();

        $("#accountForm input:radio").removeAttr("checked");
        $("#accountForm input:radio").removeAttr("disabled");
        $("#accountForm input:radio").parent().removeClass("checked disabled");
    },
    add: function () {
        accountObj.common();

        var companyId = $("#editCompanyForm input[name='id']").val();
        var companyName = $("#editCompanyForm input[name='name']").val();
        var companyUserId = $("#editCompanyUserForm input[name='id']").val();
        var companyUserName = $("#editCompanyUserForm input[name='name']").val();

        $("#accountForm input[name='companyId']").val(companyId);
        $("#accountForm input[name='companyName']").val(companyName);
        $("#accountForm input[name='dockingId']").val(companyUserId);
        $("#accountForm input[name='contactor']").val(companyUserName);

        $("#addAccountBtn").show();
        $("#editAccountBtn").hide();
        $("#accountForm input").removeAttr("style");
        $("#accountForm input[name='companyUserId']").val($("#id").val());
    },
    view: function (id, flag) {
        $.ajax({
            type: "get",
            url: "/account/view",
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    if (data.data.entity != null) {
                        accountObj.common();
                        for (var attr in data.data.entity) {
                            $("#accountForm input[name='" + attr + "'][type!='radio']").val(data.data.entity[attr]);
                        }

                        if (flag == 0) {
                            $("#addAccountBtn").hide();
                            $("#editAccountBtn").hide();
                            $("#accountForm input[name='accountType']").attr("disabled", "disabled");
                            $("#accountForm input[name='accountType']").parent().addClass("disabled");
                            $("#accountForm input").attr("style", "border:0;");
                        } else {
                            $("#addAccountBtn").hide();
                            $("#editAccountBtn").show();
                            $("#accountForm input").removeAttr("style");
                        }
                        $("#accountForm input[name='accountType'][value='" + data.data.entity['accountType'] + "']").attr("checked", "checked");
                        $("#accountForm input[name='accountType'][value='" + data.data.entity['accountType'] + "']").parent().addClass("checked");
                    }
                }
            }
        })
    },
    del: function (id) {
        var lock = true;
        layer.confirm("确定删除该条客户银行账号记录！", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            if (lock) {
                lock = false;
                $.ajax({
                    type: "get",
                    url: baseUrl + "/account/del",
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            swal("操作成功");
                            gridObj.reloadAccountEdit($("#id").val());
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
                    error: function (data) {
                        Ladda.stopAll();
                        swal(data.msg);
                    }
                });
            }
        }, function () {
            return;
        })
    },
    save: function (t) {
        $("#companyCode").val(user.companyCode);
        if ($("#accountForm").valid()) {
            var lock = true;
            var formData = $("#accountForm").serializeJson();//序列化当前表单，并传出file类型
            layer.confirm("请确认客户银行账号信息是否正确！", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "get",
                        url: baseUrl + "/account/add",
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                swal("操作成功");
                                $("#addAccountModal").modal("hide");
                                gridObj.reloadAccountEdit($("#companyUserId").val());
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },
    update: function (t) {
        if ($("#accountForm").valid()) {
            var lock = true;
            var formData = $("#accountForm").serializeJson();//序列化当前表单，并传出file类型
            layer.confirm("请确认客户银行账户信息是否正确！", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "get",
                        url: baseUrl + "/account/edit",
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                swal("操作成功");
                                $("#addAccountModal").modal("hide");
                                gridObj.reloadAccountEdit($("#companyUserId").val());
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
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },
};

function openImage(e, modalId) {
    imgSrc = $(e).attr("src");
    imgAlt = $(e).attr("alt");
    imgSrc = imgSrc.replace(/\"/g, "");
    $("#imgDiv").attr("alt", imgAlt);
    $("#imgDiv").attr("title", imgAlt);
    $("#imgDiv").attr("src", imgSrc);
    $("#" + modalId).modal({backdrop: "static"});
}

function openCarouselImage(Link, modalId) {
    $("#trackModal").modal("hide");
    $("#carouselItem").empty();
    var html = +"";
    var imgLink = Link.split(',');

    if (imgLink.length == 1) {
        $("#imgDiv").attr("src", imgLink);
        $("#imgDiv").attr("title", "");
        $("#imgDiv").attr("alt", "");
        $("#imgModal").modal({backdrop: "static"});
    } else {
        for (var i = 0; i < imgLink.length; i++) {
            var imgSrc = imgLink[i];
            html += "<div><img src=" + imgSrc + " onclick=\"closeImg('imgCarouselModal')\" width='100%' height='500px'></div>";
        }
        $("#carouselItem").append(html);
        var carousel = null;
        layui.use('carousel', function () {
            carousel = layui.carousel;
            //建造实例
            var ins = carousel.render({
                elem: '#carousel',
                width: '100%', //设置容器宽度,
                height: '500px',
                autoplay: false,
                arrow: 'always' //始终显示箭头
                //,anim: 'updown' //切换动画方式
            });
            //重置轮播
            ins.reload({
                elem: '#carousel',
                width: '100%', //设置容器宽度,
                height: '500px',
                autoplay: false,
                arrow: 'always' //始终显示箭头
                //,anim: 'updown' //切换动画方式
            });
        });
        $("#" + modalId).modal("toggle");
    }

}

var editNav = {
    skip: function (li1, tab1, li2, tab2) {
        //第一个选项卡显示，其他的选项卡都禁用
        document.getElementById(li1).style.pointerEvents = "";
        if (!$("#" + li1).hasClass("active")) {
            $("#" + li1).addClass("active");
            $("#" + li1 + " > a").css("background-color", "aliceblue");
        }
        if (!$("#" + tab1).hasClass("active")) {
            $("#" + tab1).addClass("active");
        }

        if (li2 != undefined) {
            document.getElementById(li2).style.pointerEvents = "none";
            if ($("#" + li2).hasClass("active")) {
                $("#" + li2).removeClass("active");
                $("#" + li2 + " > a").css("background-color", "");
            }
            if ($("#" + tab2).hasClass("active")) {
                $("#" + tab2).removeClass("active");
            }
        }
    },
    stepOne: function () {
        $(".stepOneFooter").show();
        $(".stepTwoFooter").hide();
        editNav.skip("li-1", "tab-1", "li-2", "tab-2");
    },
    stepTwo: function () {
        $(".stepTwoFooter").show();
        $(".stepOneFooter").hide();
        editNav.skip("li-2", "tab-2", "li-1", "tab-1");
    },
};

//审核通过
function approve(t) {
    approveTask($("#taskId").val(), 1, t.id, $("#desc").val())
}

//审核驳回
function reject(t) {
    approveTask($("#taskId").val(), 0, t.id, $("#desc").val())
}

//审核记录查看
function showHistory(id) {
    //process详见IProcess
    $("#historyModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: "/process/history",
        data: {dataId: id, process: 33},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                $("#history").empty();
                if (data.data.data != null) {
                    var html = "";
                    html += "<div style='position: relative;z-index: 10;'>" +
                        "<div class='form-control'>" +
                        "<div class='col-sm-3 text-center'>审核节点</div>" +
                        "<div class='col-sm-3 text-center'>操作人</div>" +
                        "<div class='col-sm-3 text-center'>操作详情</div>" +
                        "<div class='col-sm-3 text-center'>操作时间</div></div>";
                    for (var i = 0; i < data.data.data.length; i++) {
                        html += "<div class='form-control'>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>" +
                            "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>" +
                            "</div>";
                    }
                    html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=33&t=" + new Date().getTime() + "' style='width: 100%; margin-top: 0px; margin-bottom: 0px; margin-left: 0px;'/></div>";
                    $("#history").append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}
//统计概况弹窗
var statisticsToggle = {
    getSingleLinkHtml: function (id, value, type) { //获取单个a链接
        var html = "";
        if (id) {
            value = value ? value : id;//如果value为空则展示ID
            html += "<a onclick=\"statisticsToggle.toggleModal(" + id + ",'" + value + "','" + type + "');\">" + value + "</a>";
        }
        return html;
    },
    toggleModal: function (id, name, type) {
        if ("cust" == type) {
            var title = "[" + name + "]-客户统计";
            statisticsModal.loadConfig({enterType: "cust", enterParam: {custId: id}, title: title}); //加载用户配置
        }
        if ("business" == type) {
            var title = "[" + name + "]-业务统计";
            statisticsModal.loadConfig({enterType: "business", enterParam: {currentUserId: id}, title: title}); //加载用户配置
        }
        if ("mediaUser" == type) {
            var title = "[" + name + "]-媒介统计";
            statisticsModal.loadConfig({enterType: "mediaUser", enterParam: {currentUserId: id}, title: title}); //加载用户配置
        }
        if ("mediaType" == type) {
            var title = "[" + name + "]-板块统计";
            statisticsModal.loadConfig({enterType: "mediaType", enterParam: {mediaType: id}, title: title}); //加载用户配置
        }
        if ("media" == type) {
            var title = "[" + name + "]-媒体统计";
            statisticsModal.loadConfig({enterType: "media", enterParam: {mediaId: id}, title: title}); //加载用户配置
        }
        if ("supplier" == type) {
            var title = "[" + name + "]-供应商统计";
            statisticsModal.loadConfig({enterType: "supplier", enterParam: {supplierId: id}, title: title}); //加载用户配置
        }
        $("#statisticsModal").modal("toggle");
    }
};