var companyMap = {};
$(function () {
    // 查询表单增加校验；
    $("#queryForm").validate();

    // 审核表单增加校验；
    $("#approveForm").validate();

    // 加载基础资源数据；
    layui.use(["form"], function () {
        $.get(baseUrl + "/entry/getNation", function (data) {
            var dataValue = data.data.nation;
            if (dataValue != null) {
                if (dataValue.length > 0) {
                    var nationObj = $("#queryForm select[data='raceId']");
                    for (var i = 0; i < dataValue.length; i++) {
                        nationObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                    // 初始化；
                    layui.form.render();
                }
            }
        }, "json");
    });

    // 设置表格默认的UI样式；
    $.jgrid.defaults.styleUI = 'Bootstrap';

    // 窗口拖拽绑定事件；
    $(window).bind('resize', function () {
        var width = $("#entryTable").closest('.jqGrid_wrapper').width() || $(document).width();
        $('#entryTable').setGridWidth(width);
    });

    // 回车执行查询；
    $("body").keydown(function (evt) {
        evt = (evt) ? evt : ((window.event) ? window.event : "");
        var curKey = evt.keyCode ? evt.keyCode : evt.which;
        if (curKey == 13) {
            $("#dataSearch").click();
        }
    });

    // 查询按钮；
    $("#dataSearch").click(function () {
        if ($("#queryForm").valid()) {
            $("#entryTable").emptyGridParam();
            reloadEntryData();
        }
    });

    //加载公司信息
    loadCompany();

    // 初始化表格；
    loadJGrid();

    // 提交录用流程的评分插件初始化；
    loadStar("stepThree");
    loadStar("stepFour");


    // 申请表单的校验；
    $("#formDiv > form").each(function () {
        $(this).validate();
    });
    // 先隐藏其他的几个表单；
    $("#formDiv > form:first").nextAll().hide();
    // 上一步按钮隐藏；
    $("#stepBackword").hide();

    //部门筛选
    $("#selDept").click(function () {
        $("#deptModal").modal('toggle');
    });
    $("#cleanDept").click(function () {
        $("#deptId").val("");
        $("#deptName").val("");
        $("#queryForm select[name='entryProfession']").empty();
        layui.form.render();
    });
    $('#treeview').treeview({
        data: [getTreeData(isZC())],
        onNodeSelected: function (event, data) {
            $("#deptId").val(data.id);
            $("#deptName").val(data.text);
            $("#deptModal").modal('hide');
            loadPost(data.id);//加载职位
        }
    });

});

//初始化公司信息
function loadCompany() {
    $.ajax({
        url: baseUrl + "/dept/listJTAllCompany",
        type: "get",
        data: null,
        dataType: "json",
        async: false,
        success: function (data) {
            if(data.code == 200 && data.data.result){
                var resData = data.data.result;
                $(resData).each(function (i, d) {
                    companyMap[d.code] = d.name;
                });
            }
        }
    });
}

//判断当前用户是否总裁
var isZC = function () {
    var roles = user.roles;//获取用户角色
    var isZC = false;//是否总裁角色
    if (roles) {
        for (var i = 0; i < roles.length; i++) {
            if (roles[i].code == 'ZC' || roles[i].code == 'FZC' || roles[i].code == 'ZW') {
                isZC = true;
                break;
            }
        }
    }
    return isZC;
}

//初始化部门
function getTreeData(isZC) {
    var deptTreeData = {};
    var deptId = user.dept.id;//当前用户部门ID
    var deptCode = user.dept.code;//当前部门编码
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    var url = "/dept/listForSonTreeView";
    if(deptCode == "ZW"){
        url = "/deptZw/listDeptTreeByZw"; //查询政委管理的部门
    }else if (deptCompanyCode == "JT" && (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL")) {
        requestData(null, "/dept/getRootDept", "POST", function (result) {
            var root = result.data.root;
            if (root) {
                deptId = root.id;//整个集团的业务和媒介部
            } else {
                deptId = 517;//整个集团的业务和媒介部
            }
        });
    } else if (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL" || deptCode == "RS" || deptCode == "XZ") {
        requestData({companyCode: deptCompanyCode}, "/dept/getCompanyByCode", "POST", function (result) {
            var company = result.data.company;
            if (company) {
                deptId = company.id;//整个集团的业务和媒介部
            }
        });
    }
    var param = {deptId: deptId};
    //如果是政委，并且是政委部门负责人，则部门树还需要包含自己部门
    if(deptCode == "ZW" && user.id == user.dept.mgrId){
        param.mgrFlag = 1; //部门负责人标识，后台根据标识判断
    }
    //具体查询
    requestData(param, url, "POST", function (result) {
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
var requestData = function (data, url, requestType, callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: false,
        success: callBackFun
    });
}

//加载职位
function loadPost(deptId){
    $.post(baseUrl + "/entry/getPost", {deptId: deptId}, function (data) {
        var postObj = $("#queryForm select[name='entryProfession']");
        // 先清空；
        postObj.empty();
        var dataValue = data.data.post;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                for (var i = 0; i < dataValue.length; i++) {
                    postObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
            }
            // 初始化；
            layui.form.render();
        }
    }, "json");
}

//薪资计算
function trialCal(){
    var totalMoney = 0;
    $(".trial").each(function () {
        var money = $(this).val();
        if (money.length > 0 && !isNaN(parseFloat(money))) {
            totalMoney += parseFloat(money);
        }
    });
    $("#trialTotal").val(totalMoney);
}
function formalCal(){
    var totalMoney = 0;
    $(".formal").each(function () {
        var money = $(this).val();
        if (money.length > 0 && !isNaN(parseFloat(money))) {
            totalMoney += parseFloat(money);
        }
    });
    $("#formalTotal").val(totalMoney);
}

// 加载表格；
var channelMap = {0:'BOSS直聘', 1:'社交媒体', 2:'离职再入职', 3:'人才市场', 4:'校园招聘', 5:'猎头推荐', 6:'内部推荐 ', 7:'其他', 8:'前程无忧', 9:'智联招聘', 10:'分子公司调岗'};
function loadJGrid() {
    // 初始化数据；
    $("#entryTable").jqGrid({
        url: baseUrl + "/entryManage/getPageEntry",
        datatype: "json",
        mtype: 'POST',
        postData: $("#queyrForm").serializeJson(),
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 50,
        sortable: "true",
        sortname: "a.entry_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "entry_id"
        },
        colModel: [
            {
                name: 'entry_id',
                index: "a.entry_id",
                label: '主键',
                hidden: true,
                width: 60,
                sortable: false
            },
            {name: 'entry_name', index: "a.entry_name", label: '姓名', width: 120},
            {
                name: 'create_time',
                index: "a.create_time",
                label: '申请日期',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return new Date(value).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {
                name: 'entry_first_dept',
                index: "a.entry_first_dept",
                label: '一级部门',
                width: 100,
                formatter: function (value, grid, rowData) {
                    if(value == 0){
                        return "业务部门"+"("+companyMap[rowData.entry_company_code]+")";
                    }else if(value == 1){
                        return "媒介部门"+"("+companyMap[rowData.entry_company_code]+")";
                    }else {
                        return "其他部门"+"("+companyMap[rowData.entry_company_code]+")";
                    }
                }
            },
            {
                name: 'entry_dept',
                index: "a.entry_dept",
                label: '二级部门',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.deptName == null ? "" : rowData.deptName;
                }
            },
            {
                name: 'entry_profession',
                index: "a.entry_profession",
                label: '申请职位',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.postName == null ? "" : rowData.postName;
                }
            },
            {name: 'emp_native', index: "b.emp_native", label: '籍贯', width: 120},
            {
                name: 'emp_gender',
                index: "b.emp_gender",
                label: '性别',
                width: 120,
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
                name: 'emp_race',
                index: "b.emp_race",
                label: '民族',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        // 少数民族颜色为橘色；
                        if (value == 1) {
                            return "<b style='color: green;'>" + rowData.raceName + "</b>";
                        } else {
                            return "<b style='color: purple;'>" + rowData.raceName + "</b>";
                        }
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'entry_marriage',
                index: "a.entry_marriage",
                label: '婚否',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: green;">未婚</b>';
                    } else if (value == 1) {
                        return '<b style="color: red;">已婚</b>';
                    } else if (value == 2) {
                        return '<b style="color: blue;">离婚</b>';
                    } else if (value == 3) {
                        return '<b style="color: black;">丧偶</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_education',
                index: "b.emp_education",
                label: '学历',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if(value == 8){
                        return '<b style="color: #80766C;">小学</b>';
                    } else if (value == 0) {
                        return '<b style="color: grey;">初中</b>';
                    } else if (value == 1) {
                        return '<b style="color: black;">高中</b>';
                    } else if (value == 2) {
                        return '<b style="color: blue;">专科</b>';
                    } else if (value == 3) {
                        return '<b style="color: orange;">本科</b>';
                    } else if (value == 4) {
                        return '<b style="color: orangered;">硕士</b>';
                    } else if (value == 5) {
                        return '<b style="color: red;">博士</b>';
                    } else if (value == 6) {
                        return '<b style="color: darkred;">博士后</b>';
                    } else if (value == 7) {
                        if (rowData.emp_education_other) {
                            return '<b style="color: purple;">' + rowData.emp_education_other + '</b>';
                        } else {
                            return '<b style="color: purple;">其他</b>';
                        }
                    } else {
                        return "";
                    }
                }
            },
            {name: 'emp_major', index: "b.emp_major", label: '专业', width: 120},
            {
                name: 'emp_birth',
                index: "b.emp_birth",
                label: '出生年月',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return value ? new Date(value).format("yyyy-MM-dd") : "";
                }
            },
            {name: 'emp_code', index: "b.emp_code", label: '身份证号码', width: 120},
            {name: 'entry_phone', index: "a.entry_phone", label: '联系电话', width: 120},
            {
                name: 'entry_channel',
                index: "a.entry_channel",
                label: '求职渠道',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return channelMap[value] || "";
                }
            },
            {
                name: 'state', index: "a.state", label: '状态', width: 120, formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: orange;">待审核</b>';
                    } else if (value == 1) {
                        return '<b style="color: red;">审核中</b>';
                    } else if (value == 2) {
                        var dataComplete = rowData.entry_complete;
                        if (dataComplete == 1) {
                            return '<b style="color: green;">入库审核</b>';
                        } else {
                            return '<b style="color: green;">完善资料</b>';
                        }
                    } else if (value == 3) {
                        return '<b style="color: blue;">已入职</b>';
                    } else if (value == 4) {
                        return '<b style="color: grey;">不予考虑</b>';
                    } else if (value == 5) {
                        return '<b style="color: black;">已离职</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'operate', label: "操作", width: 360, sortable: false,
                formatter: function (value, grid, rowData) {
                    var html = "";
                    var entryId = rowData.entry_id;
                    var state = rowData.state;
                    // 显示编辑、存档、录用按钮；
                    if (state == 0) {
                        html += "<a href='/regist?id=" + entryId + "' target='_blank'>编辑</a>&nbsp;|&nbsp;";
                        html += '<a href="#" onclick="openEmploy('+entryId+');return false;">录用</a>&nbsp;|&nbsp;';
                        html += '<a href="#" onclick="changeEntryState(' + "'" + entryId + "','1'" + ');return false;">存档</a>&nbsp;|&nbsp;';
                        // 显示审核按钮；
                    } else if (state == 2) {
                        if (rowData.entry_complete == 1) {
                            html += '<a href="#" onclick="openApprove(' + "'" + entryId + "'" + ');return false;">审核</a>&nbsp;|&nbsp;';
                        } else {
                            html += '<a href="#" onclick="noticeUser(' + "'" + entryId + "'" + ');return false;">通知</a>&nbsp;|&nbsp;';
                        }
                        // 显示恢复按钮；
                    } else if (state == 4) {
                        html += '<a href="#" onclick="changeEntryState(' + "'" + entryId + "','2'" + ');return false;">恢复</a>&nbsp;|&nbsp;';
                        // 显示入职按钮；
                    } else if (state == 5) {
                        html += '<a href="#" onclick="changeEntryState(' + "'" + entryId + "','3'" + ');return false;">入职</a>&nbsp;|&nbsp;';
                    }
                    // 如果有流程审核状态则有审核记录；
                    if (rowData.process_state) {
                        html += '<a href="#" onclick="showHistory(' + "'" + entryId + "'" + ');return false;">记录</a>&nbsp;|&nbsp;';
                    }
                    html += "<a href='/record?id=" + entryId + "' target='_blank'>查看</a>&nbsp;|&nbsp;";
                    html += '<a href="#" onclick="exportSingleData(' + "'" + entryId + "'" + ');return false;">导出</a>';
                    return html;
                },
            }
        ],
        pager: jQuery("#entryTableNav"),
        viewrecords: true,
        caption: "入职信息列表",
        add: false,
        edit: false,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
        },
        loadComplete: function (a, b, c) {
            $("#entryTable").find("tr").each(function () {
                $(this).children().first().css("width", "50");
            });
            var width = $("#entryTable").closest('.jqGrid_wrapper').width() || $(document).width();
            $('#entryTable').setGridWidth(width);
        }
    });

    $("#entryTable").jqGrid('setLabel', 'rn', '序号', {
        'text-align': 'center',
        'vertical-align': 'middle',
        "width": "50"
    });
}

// 重新载入数据；
function reloadEntryData() {
    $("#entryTable").reloadCurrentData(baseUrl + "/entryManage/getPageEntry", $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}

// 通知用户；
function noticeUser(entryId) {
    alertMessage("系统处理中，请稍候。");
    $.post(baseUrl + "/entryManage/entryNotice", {entryId: entryId}, function (data) {
        var message = data.data.message;
        if (message != null) {
            swal({
                title: "提示",
                text: "请通知入职申请人员在员工档案页面提交材料。",
                type: "warning",
                html: false,
            });
        } else {
            alertMessage(data.msg);
        }
    }, "json");
}

// 加载星级评分插件；
function loadStar(elementName) {
    // 提交录用流程的评分插件；
    $("#" + elementName + " .kv-fa").rating({
        theme: "krajee-fa",
        language: "zh",
        step: 1,
        filledStar: "<i class='fa fa-star'></i>",
        emptyStar: "<i class='fa fa-star-o'></i>"
    });

    // 评分插件的切换事件；
    $("#" + elementName + " .kv-fa").on("change", function () {
        var totalCount = 0;
        $("#" + elementName + " .kv-fa").each(function () {
            totalCount += parseInt($(this).val());
        });
        $("#" + elementName + " input[name='comTotalScore']").val(totalCount);
    });
}

/**
 * 修改数据的状态；
 * @param entryId：数据ID；
 * @param operate：操作类型，0为删除，1为存档备用（不予考虑），2为恢复；
 */
function changeEntryState(entryId, operate) {
    // 定义数组保存ID；
    var entryIds = new Array();
    // 判断是否为多选操作；
    if (entryId) {
        entryIds.push(entryId);
    } else {
        entryIds = $("#entryTable").jqGrid("getGridParam", "selarrrow");
    }
    // 如果多选操作没有选择数据，提示用户；
    if (entryIds.length > 0) {
        if (operate) {
            var params = {entryIds: entryIds.toString(), operate: operate};
            // 删除操作增加确认；
            if (operate == '0') {
                layer.confirm("此操作无法恢复，确认继续吗？", {
                    btn: ["确认", "取消"],
                    shade: [0.7, '#393D49']
                }, function (index) {
                    layer.close(index);
                    // 提交数据到后台；
                    updateData(params);
                });
            } else {
                // 提交数据到后台；
                updateData(params);
            }
        } else {
            alertMessage("请选择操作类型。");
        }

    } else {
        alertMessage("请选择要操作的数据。");
    }
}

// 提交数据到后台；
function updateData(params) {
    alertMessage("系统处理中，请稍候。");
    $.post(baseUrl + "/entryManage/updateEntryState", params, function (data) {
        var type = data.data.type;
        if (type != null) {
            if (type == 0) {
                alertMessage("操作类型不存在。");
            } else {
                alertMessage("操作完成。");
                reloadEntryData();
            }
        } else {
            alertMessage(data.msg);
        }
    }, "json");
}

// 数据导出；
function exportData() {
    var params = $("#queryForm").serializeJson();
    var ids = $("#entryTable").jqGrid("getGridParam", "selarrrow");
    if (ids.length > 0) {
        params.entryIds = ids;
    }
    alertMessage("系统处理中，请稍候。");
    startModal("#exportButton");
    $.post(baseUrl + "/entryManage/exportData", params, function (data) {
        Ladda.stopAll();
        if (data.data.message != null) {
            alertMessage(data.data.message);
        } else if (data.data.file != null) {
            window.location.href = data.data.file;
        } else {
            alertMessage(data.msg);
        }
    }, "json")
}

// 单条数据导出；
function exportSingleData(dataId) {
    alertMessage("系统处理中，请稍候。");
    $.post(baseUrl + "/entryManage/exportSingleData", {entryId: dataId}, function (data) {
        if (data.data.message != null) {
            alertMessage(data.data.message);
        } else if (data.data.file != null) {
            window.location.href = data.data.file;
        } else {
            alertMessage(data.msg);
        }
    }, "json")
}

// 打开审核页面；
function openApprove(entryId) {
    alertMessage("系统处理中，请稍候。");
    $.post(baseUrl + "/entryManage/getEntryFile", {entryId: entryId}, function (data) {
        var dataValue = data.data.comment;
        if (dataValue == null) {
            alertMessage("未找到相关的文件信息。");
        } else {
            var inputNext;
            var fileName;
            var commentData = dataValue.comment;
            var dataIsEmpty = commentData == null;
            if (dataIsEmpty) {
                commentData = {};
            }
            $("#approveForm").find("input").each(function () {
                $(this).val(commentData[$(this).attr("name")]);
                // 先移除；
                inputNext = $(this).next("a");
                if (inputNext && inputNext.length > 0) {
                    inputNext.remove();
                }
                fileName = dataValue[$(this).attr("data")];
                if (fileName) {
                    $(this).parent().append("<a href='" + fileName + "' target='_blank'>&nbsp;点击查看</a>");
                }
            });
            // 如果没有获取到审核记录数据，则表示该入职申请为未通过流程审核而直接修改数据库的数据；
            if (dataIsEmpty) {
                $("#approveForm").find("input[name='entryId']").val(entryId);
            }
        }
        $("#approveModal").modal({backdrop: "static"});
    }, "json")
}

// 保存审核意见；
function saveApprove(flag) {
    if ($("#approveForm").valid()) {
        alertMessage("系统处理中，请稍候。");
        var params = $("#approveForm").serializeJson();
        params["operate"] = flag;
        if (flag == '0') {
            startModal("#agreeApprove");
        } else {
            startModal("#rejectApprove");
        }
        $.post(baseUrl + "/entryManage/approveEntry", params, function (data) {
            Ladda.stopAll();
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                alertMessage(message);
            }
            $("#approveModal").modal("hide");
            reloadEntryData();
        }, "json");
    } else {
        alertMessage("请勿输入特殊字符。");
    }
}

// 打开选择审核的窗口；
function openEmploy(entryId) {
    alertMessage("系统处理中，请稍候。");
    // 先隐藏其他的几个表单；
    $("#formDiv > form").each(function () {
        cleanValidate($(this));
    });
    // 显示第一个表单；
    $("#stepOne").show();
    $("#formDiv > form:first").nextAll().hide();
    // 上一步按钮隐藏；
    $("#stepBackword").hide();
    // 下一步按钮文字还原；
    $("#stepForward").html('<i class="fa fa-forward"></i>&nbsp;&nbsp;下一步');

    // 清空星级评分；
    $("#formDiv").find(".kv-fa").each(function () {
        $(this).rating("reset");
    });

    // 评分的两个表单默认类型；
    $("#stepThree input[name='operate']").val(0);
    $("#stepFour input[name='operate']").val(1);

    //部门和职位初始化
    $("#stepOne").find("#empDept").val("");
    $("#stepOne").find("#empProfession").val("");
    $(".trialName").text("试用");

    // 获取审核用户信息；
    $.post(baseUrl + "/entryManage/getEmployInfo", {entryId: entryId}, function (data) {
        if(data.code == 200){
            // 基本信息；
            var　dataValue = data.data.entry;
            if (dataValue != null) {
                $("#stepOne").find("input").each(function () {
                    $(this).val(dataValue[$(this).attr("name")]);
                });

                //一级部门
                $("#stepOne").find("#entryFirstDept").val(dataValue.entryFirstDept);

                //加载二级部门
                var empDept = (data.data.hire && data.data.hire.empDept) ?  data.data.hire.empDept : dataValue.empDept;
                loadEntryDeptData(dataValue.entryFirstDept, empDept, dataValue.empProfession);

                //加载员工状态改变事件
                layui.form.on("select(entryState)", function (data) {
                    if(data.value == 1){
                        $(".trialName").text("实习");
                    }else {
                        $(".trialName").text("试用");
                    }
                });

                //最后一个表单的部门信息；
                $("#stepFive input[name='deptId']").val(dataValue.empDept);
                $("#stepFive input[name='empDeptName']").val(dataValue.empDeptName);
                // 父表主键；
                var entryId = dataValue.entryId;

                // 出生日期；
                var empBirth = dataValue.empBirth;
                if (empBirth) {
                    $("#stepOne").find("input[name='empBirth']").val(new Date(empBirth).format("yyyy-MM-dd"));
                }

                // 性别；
                var empGender = dataValue.empGender;
                if (empGender == 0) {
                    $("#empGenderText").val("女");
                } else {
                    $("#empGenderText").val("男");
                }

                // 婚姻状况；
                var entryMarriage = dataValue.empMarriage;
                if (entryMarriage >= 0 && entryMarriage <= 3) {
                    var marriage = ["未婚", "已婚", "离婚", "丧偶"];
                    $("#empMarriageText").val(marriage[entryMarriage]);
                }

                // 学历；
                var education = ["初中", "高中", "专科", "本科", "硕士", "博士", "博士后","其他","小学"];
                var entryEducation = dataValue.empEducation;
                if (entryEducation == 7) {
                    $("#empEducationText").val(dataValue.empEducationOther);
                } else {
                    if (entryEducation >= 0) {
                        $("#empEducationText").val(education[entryEducation]);
                    }
                }

                // 院校及专业；
                var collegeMajor = "";
                var entryCollege = dataValue.empCollege;
                if (entryCollege) {
                    collegeMajor += entryCollege;
                }
                var entryMajor = dataValue.empMajor;
                if (entryMajor) {
                    collegeMajor += "【" + entryMajor + "】";
                }
                $("#collegeMajor").val(collegeMajor);

                // 录用记录的日期；
                dataValue = data.data.hire;
                if (dataValue != null) {
                    $("#stepOne").find(".layer-date").each(function () {
                        var dateStr = dataValue[$(this).attr("name")];
                        if (dateStr) {
                            $(this).val(new Date(dateStr).format("yyyy-MM-dd"));
                        }
                    });
                    // 设置主键；
                    $("#stepOne input[name='hireId']").val(dataValue.hireId);
                }

                // 薪资待遇；
                dataValue = data.data.salary;
                if (dataValue != null) {
                    $("#stepTwo input").each(function () {
                        $(this).val(dataValue[$(this).attr("name")]);
                    });
                }

                // 审核信息；
                dataValue = data.data.comment;
                if (dataValue && dataValue.length > 0) {
                    var comment;
                    for (var i = 0; i < dataValue.length; i++) {
                        comment = dataValue[i];
                        // 人事面试；
                        if (comment.comType == 0) {
                            $("#stepThree input").each(function () {
                                $(this).val(comment[$(this).attr("name")]);
                                // 星级评分；
                                if ($(this).hasClass("kv-fa")) {
                                    $(this).rating("update", comment[$(this).attr("name")]);
                                }
                            });
                            $("#stepThree input[name='operate']").val(0);
                            $("#stepThree textarea[name='comAdvice']").text(comment.comAdvice);
                        }
                        // 专业测试；
                        if (comment.comType == 1) {
                            $("#stepFour input").each(function () {
                                $(this).val(comment[$(this).attr("name")]);
                                // 星级评分；
                                if ($(this).hasClass("kv-fa")) {
                                    $(this).rating("update", comment[$(this).attr("name")]);
                                }
                            });
                            $("#stepFour input[name='operate']").val(1);
                            $("#stepFour textarea[name='comAdvice']").text(comment.comAdvice);
                        }
                    }
                }

                // 设置主键；
                $("input[name='entryId']").val(entryId);
                $("#employModal").modal({backdrop: "static"});
            } else {
                alertMessage("该入职申请未找到或已完成审核。");
            }
        }else {
            alertMessage("信息获取失败，请联系管理员。");
        }



     /*   var dataValue = data.data.user;
        if (dataValue == null) {
            alertMessage("信息不存在，请刷新页面后重试。");
        } else {
            if (dataValue.length > 0) {
                // 审核人；
                for (var i = 0; i < dataValue.length; i++) {
                    userObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
                // 设置用户名；
                var userNameObj = $("#stepFive").find("input[name='userName']");
                userNameObj.val(dataValue[0].name);
                // 初始化；
                layui.form.render();

                // 下拉框的onchange事件；
                layui.form.on("select(userId)", function (userData) {
                    // 更新隐藏域；
                    userNameObj.val(getLayUISelectText(userData));
                });

                // 基本信息；
                dataValue = data.data.entry;
                if (dataValue != null) {
                    $("#stepOne").find("input").each(function () {
                        $(this).val(dataValue[$(this).attr("name")]);
                    });

                    // 最后一个表单的部门信息；
                    $("#stepFive input[name='deptId']").val(dataValue.empDept);
                    $("#stepFive input[name='empDeptName']").val(dataValue.empDeptName);
                    // 父表主键；
                    var entryId = dataValue.entryId;

                    // 出生日期；
                    var empBirth = dataValue.empBirth;
                    if (empBirth) {
                        $("#stepOne").find("input[name='empBirth']").val(new Date(empBirth).format("yyyy-MM-dd"));
                    }

                    // 性别；
                    var empGender = dataValue.empGender;
                    if (empGender == 0) {
                        $("#empGenderText").val("女");
                    } else {
                        $("#empGenderText").val("男");
                    }

                    // 婚姻状况；
                    var entryMarriage = dataValue.empMarriage;
                    if (entryMarriage >= 0 && entryMarriage <= 3) {
                        var marriage = ["未婚", "已婚", "离婚", "丧偶"];
                        $("#empMarriageText").val(marriage[entryMarriage]);
                    }

                    // 学历；
                    var education = ["初中", "高中", "专科", "本科", "硕士", "博士", "博士后","其他","小学"];
                    var entryEducation = dataValue.empEducation;
                    if (entryEducation == 7) {
                        $("#empEducationText").val(dataValue.empEducationOther);
                    } else {
                        if (entryEducation >= 0) {
                            $("#empEducationText").val(education[entryEducation]);
                        }
                    }

                    // 院校及专业；
                    var collegeMajor = "";
                    var entryCollege = dataValue.empCollege;
                    if (entryCollege) {
                        collegeMajor += entryCollege;
                    }
                    var entryMajor = dataValue.empMajor;
                    if (entryMajor) {
                        collegeMajor += "【" + entryMajor + "】";
                    }
                    $("#collegeMajor").val(collegeMajor);

                    // 录用记录的日期；
                    dataValue = data.data.hire;
                    if (dataValue != null) {
                        $("#stepOne").find(".layer-date").each(function () {
                            var dateStr = dataValue[$(this).attr("name")];
                            if (dateStr) {
                                $(this).val(new Date(dateStr).format("yyyy-MM-dd"));
                            }
                        });
                        // 设置主键；
                        $("#stepOne input[name='hireId']").val(dataValue.hireId);
                    }

                    // 薪资待遇；
                    dataValue = data.data.salary;
                    if (dataValue != null) {
                        $("#stepTwo input").each(function () {
                            $(this).val(dataValue[$(this).attr("name")]);
                        });
                    }

                    // 审核信息；
                    dataValue = data.data.comment;
                    if (dataValue && dataValue.length > 0) {
                        var comment;
                        for (var i = 0; i < dataValue.length; i++) {
                            comment = dataValue[i];
                            // 人事面试；
                            if (comment.comType == 0) {
                                $("#stepThree input").each(function () {
                                    $(this).val(comment[$(this).attr("name")]);
                                    // 星级评分；
                                    if ($(this).hasClass("kv-fa")) {
                                        $(this).rating("update", comment[$(this).attr("name")]);
                                    }
                                });
                                $("#stepThree input[name='operate']").val(0);
                                $("#stepThree textarea[name='comAdvice']").text(comment.comAdvice);
                            }
                            // 专业测试；
                            if (comment.comType == 1) {
                                $("#stepFour input").each(function () {
                                    $(this).val(comment[$(this).attr("name")]);
                                    // 星级评分；
                                    if ($(this).hasClass("kv-fa")) {
                                        $(this).rating("update", comment[$(this).attr("name")]);
                                    }
                                });
                                $("#stepFour input[name='operate']").val(1);
                                $("#stepFour textarea[name='comAdvice']").text(comment.comAdvice);
                            }
                        }
                    }

                    // 设置主键；
                    $("input[name='entryId']").val(entryId);
                    $("#employModal").modal({backdrop: "static"});
                } else {
                    alertMessage("该入职申请未找到或已完成审核。");
                }
            } else {
                alertMessage("该部门查询无负责人，请联系管理员。");
            }
        }*/
    }, "json");
}

//下拉列表级联
function loadEntryDeptData(entryFirstDept, empDept, empProfession) {
    var entryCompanyCode = $("#entryCompanyCode").val() ? $("#entryCompanyCode").val() : user.companyCode;
    //刷新二级部门
    loadEntryDept(entryCompanyCode, entryFirstDept, empDept,empProfession);

    // 下拉框的onchange事件；
    layui.form.on("select(entryFirstDept)", function (deptData) {
        //刷新二级部门
        loadEntryDept(entryCompanyCode, deptData.value, empDept,empProfession);
    });
    layui.form.on("select(empDept)", function (deptData) {
        //第五步录取部门
        $("#stepFive input[name='deptId']").val(deptData.value);
        $("#stepFive input[name='empDeptName']").val($("#empDept").find("option:selected").text());
        entryFirstDept = $("#entryFirstDept").val() ? $("#entryFirstDept").val() : entryFirstDept;
        empProfession = $("#empProfession").val() ? $("#empProfession").val() : empProfession;
        // 刷新部门的职位数据；
        loadEntryPostData(entryCompanyCode, entryFirstDept, deptData.value, empProfession);
    });
}
//获取二级部门
function loadEntryDept(companyCode, entryFirstDept, empDept, empProfession) {
    var url = "/entryManage/listDeptByFirstDept";
    var param = {companyCode: companyCode, firstDept: entryFirstDept};
    $.post(baseUrl + url, param, function (data) {
        var deptObj = $("#stepOne select[name='empDept']");
        // 先清空；
        deptObj.empty();
        var dataValue = data.data.dept;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                for (var i = 0; i < dataValue.length; i++) {
                    if (empDept == dataValue[i].id) {
                        deptObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        deptObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                }
            }
            // 初始化；
            layui.form.render();
            empDept = $("#empDept").val() ? $("#empDept").val() : empDept;
            empProfession = $("#empProfession").val() ? $("#empProfession").val() : empProfession;
            //第五步录取部门
            $("#stepFive input[name='deptId']").val($("#empDept").val());
            $("#stepFive input[name='empDeptName']").val($("#empDept").find("option:selected").text());
            // 刷新部门的职位数据；
            loadEntryPostData(companyCode, entryFirstDept, empDept, empProfession);
        }
    }, "json");
}
// 获取部门的职位数据；
function loadEntryPostData(companyCode, entryFirstDept, empDept, empProfession) {
    var url = "/entry/listPostByCompanyAndDept";
    var param = {companyCode: companyCode, firstDept: entryFirstDept};
    if(empDept){
        url = "/entry/getPost";
        param = {deptId: empDept}
    }
    $.post(baseUrl + url, param, function (data) {
        var postObj = $("#stepOne select[name='empProfession']");
        // 先清空；
        postObj.empty();
        var dataValue = data.data.post;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                for (var i = 0; i < dataValue.length; i++) {
                    if (empProfession == dataValue[i].id) {
                        postObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        postObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                }
            }
            // 初始化；
            layui.form.render();
        }
    }, "json");
}
//更新最后审核人
function loadDeptLeader(deptId){
    $.post(baseUrl + "/entryManage/listLeaderByDeptId", {deptId: deptId}, function (data) {
        var postObj = $("#stepFive select[name='userId']");
        // 先清空；
        postObj.empty();
        var dataValue = data.data.users;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                // 审核人；
                for (var i = 0; i < dataValue.length; i++) {
                    postObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
                // 设置用户名；
                var userNameObj = $("#stepFive").find("input[name='userName']");
                userNameObj.val(dataValue[0].name);
                // 初始化；
                layui.form.render();

                // 下拉框的onchange事件；
                layui.form.on("select(userId)", function (userData) {
                    // 更新隐藏域；
                    userNameObj.val(getLayUISelectText(userData));
                });
            }
            // 初始化；
            layui.form.render();
        }
    }, "json");
}

// 审核记录查看；
function showHistory(dataId) {
    showProcessHistory(dataId, 6);
}

// 上一步；
function stepBackword() {
    // 获取当前显示的表单对象；
    var obj = $("#formDiv > form:visible");
    var objPrev = obj.prev();
    // 如果有下一个；
    if (objPrev && objPrev.length > 0) {
        $("#title").html(objPrev.attr("desc"));
        obj.hide();
        objPrev.show();
        // 显示上一步按钮；
        $("#stepForward").show();

        // 如果是第一个；
        if (objPrev.attr("id") == $("#formDiv > form:first").attr("id")) {
            $("#stepBackword").hide();
        }
    } else {
        $("#stepBackword").hide();
    }
    $("#stepForward").html('<i class="fa fa-forward"></i>&nbsp;&nbsp;下一步');
}

// 下一步；
function stepForward() {
    // 获取当前显示的表单对象；
    var obj = $("#formDiv > form:visible");
    // 如果表单校验通；
    if (obj.valid()) {
        var jsonData = obj.serializeJson();
        //判断是否选择部门和职位
        if("stepOne" == $(obj).attr("id")){
            if(!$("#entryFirstDept").val()){
                layer.msg("请选择一级部门！", {time: 3000, icon: 5});
                return;
            }
            if(!jsonData.empDept){
                layer.msg("请选择录用部门！", {time: 3000, icon: 5});
                return;
            }
            if(!jsonData.empProfession){
                layer.msg("请选择职位！", {time: 2000, icon: 5});
                return;
            }
            jsonData.empDeptName = $("#empDept").find("option:selected").text();
            jsonData.empProfessionName = $("#empProfession").find("option:selected").text();
        }

        alertMessage("系统处理中，请稍候。");
        startModal("#stepForward");
        $.post(baseUrl + obj.attr("action"), jsonData, function (data) {
            Ladda.stopAll();
            // 获取返回的主键信息；
            var dataValue = data.data;
            if (dataValue != null) {
                // 更新隐藏域内容；
                for (var key in dataValue) {
                    obj.find("input[type='hidden'][name='" + key + "']").val(dataValue[key]);
                }

                var objNext = obj.next();
                // 如果有下一个；
                if (objNext && objNext.length > 0) {
                    // 显示上一步按钮；
                    $("#stepBackword").show();

                    // 如果是最后一个；
                    if (objNext.attr("id") == $("#formDiv > form:last").attr("id")) {
                        loadDeptLeader($("#empDept").val()); //加载审核人
                        $("#stepForward").html('<i class="fa fa-check"></i>&nbsp;&nbsp;提交');
                    } else {
                        $("#stepForward").html('<i class="fa fa-forward"></i>&nbsp;&nbsp;下一步');
                    }

                    // 表单切换；
                    $("#title").html(objNext.attr("desc"));
                    obj.hide();
                    objNext.show();
                    alertMessage("信息已保存。");
                } else {
                    var message = data.data.message;
                    if (message == null) {
                        alertMessage(data.msg);
                    } else {
                        alertMessage(message);
                    }
                    $("#employModal").modal("hide");
                    reloadEntryData();
                }
            } else {
                getResCode(data);
            }
        }, "json");
    } else {
        return;
    }
}