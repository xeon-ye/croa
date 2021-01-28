var deptId = user.deptId;
var deptName = user.dept.name;
$(function () {
    //初始化部门树
    getDept();
    // 为 客户管理员时，隐藏添加按钮
    if(hasRoleKHGLY()){
        $("#addStandardizedCompanyBtn").attr("style","display:none");
    }
    $("#query_table_logs").jqGrid({
        url: baseUrl + '/standardizedCompany/listPg',
        datatype: "json",
        mtype: 'GET',
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
        colModel: [
            {name: 'no', label: '标准化公司申请编号', editable: true, width: 100, sortable: false},
            {name: 'companyName', label: '公司名称', editable: true, width: 100, sortable: false},
            {name: 'applyId', label: '申请人id', editable: true, width: 60, hidden: true, sortable: false},
            {name: 'applyName', label: '申请人', editable: true, width: 60, sortable: false},
            {name: 'deptName', label: '所属部门', editable: true, width: 80, sortable: false},
            {
                name: 'applyTime', label: '申请日期', editable: true, width: 100, sortable: false,
                formatter: function (value, grid, rows) {
                    if (value) {
                        return value;
                    } else {
                        return "";
                    }
                }
            },
            {name: 'taskId', label: 'taskId', editable: true, hidden: true, width: 80, sortable: false},
            {name: 'itemId', label: 'itemId', editable: true, hidden: true, width: 80, sortable: false},
            {
                name: 'state', label: '状态', editable: true, width: 60, sortable: false,
                formatter: function (value, grid, rows) {
                    switch (rows.state) {
                        case -1 :
                            return "<span style='color:red'>审核驳回</span>";
                            break;
                        case 0 :
                            return "<span style=''>已保存</span>";
                            break;
                        case 1 :
                            return "<span style=''>已完成</span>";
                            break;
                        case 2 :
                            return "<span style='color:red'>审核通过</span>";
                            break;
                        case 37 :
                            return "<span style='color:red'>客户管理员审核</span>";
                            break;
                        case 41 :
                            return "<span style='color:red'>部门负责人审核</span>";
                            break;
                        default:
                            break;
                    }
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 120, sortable: false,
                formatter: function (value, grid, rows) {
                    var html = "";
                    if (rows.taskId != null && rows.taskId != '') {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='showHistory(" + rows.id + ")'>审核详情&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='view(" + rows.id + ",0)'>编辑&nbsp;&nbsp;</a>";
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='delStandardizedCompany(" + rows.id + ")'>删除&nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 41 || rows.state == 37) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='returnBack(" + "\"" + rows.taskId + "\"," + rows.itemId + ")'>&nbsp;撤回&nbsp;</a>";
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
            view(rowid, 1);
        }
    });

    if (getQueryString("flag") != undefined && getQueryString("flag") != null) {
        var flag = getQueryString("flag");
        if (getQueryString("id") != undefined && getQueryString("id") != null) {
            var id = getQueryString("id");
            if (flag == 0) {
                //编辑跳转flag:0编辑，2审核，1已完成
                view(id, 0);
            } else if (flag == 1) {
                view(id, 1);
            } else {
                view(id, 2)
            }
        }
        var companyName = getQueryString("companyName")
        if(companyName){
            if (flag == 3) {
                add(companyName);
            }
        }
    }
});

//申请人撤回
function returnBack(taskId, itemId) {
    var lock = true;
    layer.confirm('确认撤回？', {
        btn: ['撤回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if (lock) {
            lock = false;
            $.ajax({
                type: "post",
                url: baseUrl + "/process/withdraw",    //向后端请求数据的url
                data: {taskId: taskId, itemId: itemId},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        reflushTable();
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

//初始化业务部门
function getDept() {
    var deptDiv = document.getElementById("deptDiv");
    if (user.dept.code == 'CW' || user.isMgr == 1) {
        getDeptId();
        deptDiv.style.display = 'block';
        $("#selDept").click(function () {
            $("#deptModal").modal({backdrop: "static"});
        });
        $('#treeview').treeview({
            data: [getTreeData(deptId)],
            onNodeSelected: function (event, data) {
                $("#deptId").val(data.id);
                $("#chooseDeptName").val(data.text);
                $("#deptModal").modal('hide');
            }
        });
        $("#cleanDept").click(function () {
            $("#deptId").val(deptId);
            $("#chooseDeptName").val(deptName);
        });
    } else {
        $("#deptId").val(deptId);
        $("#chooseDeptName").val(deptName);
    }
}

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

//获取部门树数据
function getTreeData(deptId) {
    var deptTreeData = {};
    $.ajax({
        type: "GET",
        url: baseUrl + "/dept/treeByDeptId?deptId=" + deptId,
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

function getDeptId() {
    if (user.dept.code == 'CW') {
        requestData(null, "/dept/getRootDept", "get", function (data) {
            var root = data.data.root;
            if (root) {
                deptId = root.id;//整个集团的业务和媒介部
                deptName = root.name
            }
        })
    }
    $("#deptId").val(deptId);
    $("#chooseDeptName").val(deptName);
    return deptId;
}

//标准化公司申请提交
function commitApprove(t, url, state) {
    if ($("#editStandardizedCompanyForm").valid()) {
        var param = $("#editStandardizedCompanyForm").serializeForm();
        param.state = state;
        startModal("#" + t.id);
        $.ajax({
            url: baseUrl + url,
            type: "post",
            data: param,
            dataType: "json",
            async: true,
            success: function (resData) {
                Ladda.stopAll();
                if (resData.code == 200) {
                    layer.msg(resData.data.message, {time: 1000, icon: 6});
                    reflushTable();
                } else if (resData.code == 1002) {
                    swal({
                        title: "异常提示",
                        text: resData.msg,
                    });
                } else {
                    swal(resData.msg);
                }
                $("#editStandardizedCompanyModel").modal('hide');
            },
            error: function () {
                Ladda.stopAll();//隐藏加载按钮
            }
        });
    }
}

// 新增页面
function add(companyName) {
    document.getElementById("editStandardizedCompanyForm").reset();
    $("#editStandardizedCompanyForm").find("input").removeClass('error');//清除验证标签
    $("#editStandardizedCompanyForm").validate().resetForm();
    $("#remarks").text('')
    $("#itemId").val('')
    $("#id").val('')
    $("#companyName").removeAttr("disabled");
    $("#viewFooter").hide();
    $("#addStandardizedCompany").show();
    $("#saveStandardizedCompany").show();
    if(companyName){
        $("#companyName").val(companyName)
    }
    $("#editStandardizedCompanyModel").modal("toggle");
}

//编辑跳转flag0编辑，2审核，1已完成
function view(id, flag) {
    document.getElementById("editStandardizedCompanyForm").reset();
    $("#editStandardizedCompanyForm").find("input").removeClass('error');//清除验证标签
    $("#editStandardizedCompanyForm").validate().resetForm();
    $.ajax({
        url: baseUrl + "/standardizedCompany/findById",
        type: "get",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                var entity = data.data.result;
                if (entity) {
                    for (var attr in entity) {
                        $("input[name=" + attr + "]").val(entity[attr]);
                        if (attr == 'remarks') {
                            $("#remarks").text(entity[attr]);
                        }
                    }
                }
                if (flag == 0) {//编辑
                    $("#companyName").removeAttr("disabled");
                    $("#viewFooter").hide();
                    $("#addStandardizedCompany").show();
                    $("#saveStandardizedCompany").show();
                } else if (flag == 1) {//查看
                    $("#companyName").attr("disabled", "disabled");
                    $("#viewFooter").hide();
                    $("#addStandardizedCompany").hide();
                    $("#saveStandardizedCompany").hide();
                } else if (flag == 2) {//审核
                    $("#showHistory1").attr("data-id", id);
                    $("#companyName").attr("disabled", "disabled");
                    $("#editFooter").hide();
                    $("#viewFooter").show();
                }
                $("#editStandardizedCompanyModel").modal("toggle");
            } else if (data.code == 1002) {
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
                $("#editStandardizedCompanyModel").modal("toggle");
            } else {
                swal(data.msg);
                $("#editStandardizedCompanyModel").modal("toggle");
            }
        }
    });
}

//删除报价功能
function delStandardizedCompany(id) {
    layer.confirm("是否删除该标准化公司申请？", {
        btn: ["确定", "取消"],
        shade: false
    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: "/standardizedCompany/delStandardizedCompany",
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    reflushTable();
                } else if (data.code == 1002) {
                    swal({
                        title: "异常提示",
                        text: data.msg,
                    });
                }
            }
        })
    })
}

//查询功能
function reflushTable() {
    //刷新表格
    $("#query_table_logs").emptyGridParam(); //清空历史查询数据
    $("#query_table_logs").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//审核记录查看
function showHistory(id) {
    //process详见IProcess
    $("#historyModal").modal('toggle');
    $.ajax({
        type: "post",
        url: baseUrl + "/process/history",
        data: {dataId: id, process: 37},
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
                    html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=37&t=" + new Date().getTime() + "' style='width: 121%; margin-left: -42px; margin-top: 33px;margin-bottom: -100px;'/></div>";
                    $("#history").append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}

//审核通过
function approve(t) {
    approveTask($("#taskId").val(), 1, t.id, $("#desc").val());
    reflushTable();
}

//审核驳回
function reject(t) {
    approveTask($("#taskId").val(), 0, t.id, $("#desc").val());
    reflushTable();
}
