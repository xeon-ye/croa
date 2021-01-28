/**
 * Created by Administrator on 2018/2/3.
 */
$(document).ready(function () {
    $.jgrid.defaults.styleUI = "Bootstrap";
    $(window).bind("resize", function () {
        var width = $(".jqGrid_wrapper").width();
        $("#user_list").setGridWidth(width);
    })

    $("#search").click(function () {
        //使用serializeJson 必须引入cps.js文件，它可以将分页数据一起封装成json传递到控制层
        var data = $("#sd").serializeJson();
        $("#user_list").emptyGridParam();
        $("#user_list").jqGrid('setGridParam', {
            url: baseUrl + "/user/search/",
            postData: data
        }).trigger('reloadGrid');
    });
    loadUsers();
});

function loadUsers() {
    //查询表格数据
    $("#user_list").jqGrid({
        url: baseUrl + "/user/search/",
        postData: $("#sd").serializeJson(),
        datatype: "json",
        altRows: true,
        altclass: 'bgColor',
        mtype: "get",
        height: "auto",
        page: 1,//第一页
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,  //可多选，出现多选框
        multiselectWidth: 25, //设置多选列宽度
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15,//每页显示记录数
        rowList: [15, 25, 30],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "content",
            page: "number+1",
            total: "totalPages",
            records: "totalElements",
            repeatitems: false,
            id: "userId"
        },
        colModel: [{
            label: "序号",
            name: "userId",
            width: 30,
            align: "center",
            hidden: true
        }, {
            label: "区域",
            name: "region",
            width: 30,
            align: "center",
            formatter: function (v, options, row) {
                return v != undefined && v != null ? v.name : "";
            }
        }, {
            label: "用户名/账号",
            name: "userName",
            width: 30,
            align: "center"
        }, {
            label: "手机号",
            name: "phone",
            width: 30,
            align: "center"
        }, {
            label: "昵称",
            name: "name",
            width: 30,
            align: "center"
        }, {
            label: "角色",
            name: "role",
            width: 30,
            align: "center",
            formatter: function (v, options, row) {
                var type = $("#userType").val();
                if (type == 1) {
                    return row.region == null || row.region.parent == null ? "总管理员" : "区域管理员";
                }
                return "";
            }
        }, {
            label: "状态",
            name: "state",
            width: 30,
            align: "center",
            formatter: function (v, options, row) {
                switch (v) {
                    case 0:
                        return "<span class='text-success'>有效</span>";
                        break;
                    default:
                        return "<span class='text-danger'>禁用</span>";
                        break;
                }
            }
        }, {
            label: "操作",
            align: "center",
            width: 50,
            formatter: function (v, options, row) {
                var html = row.state == 0 ? "<a onclick='modify(" + row.userId + ")' class='col-sm-6 text-warning' title='修改密码'>修改密码</a>" : "<span class='col-sm-6 text-warning' title='修改密码'>修改密码</span>";
                html += row.state == 0 ? "<a onclick='disable(" + row.userId + ",-1)' class='col-sm-6 text-danger' title='禁用'>禁用</a>" : "<a onclick='disable(" + row.userId + ",0)' class='col-sm-6 text-success' title='启用'>启用</a>";
                // html += "<a href='/resource/' class='col-sm-3 text-success' title='编辑权限'>编辑权限</a>";
                // html += "<a onclick='del(" + row.userId + ")' class='col-sm-3 text-success' title='删除'>删除</a>";
                return html;
            }
        }],
        pager: "#user_pager",
        rownumbers: true,
        // caption: "系统日志列表",
        add: true,
        edit: true,
        addtext: "Add",
        edittext: "Edit",
        hidegrid: false
    });
}

function switchData(type) {
    if (type != null && type != undefined)
        $("#userType").val(type);
    switch (type) {
        case 1:
            $("#user_list").jqGrid("showCol", "role");
            break;
        case 2:
        case 3:
            $("#user_list").jqGrid("hideCol", "role");
            break;
    }
    var data = $("#sd").serializeJson();
    $("#user_list").emptyGridParam();
    $("#user_list").jqGrid('setGridParam', {
        url: baseUrl + "/user/search/",
        postData: data
    }).trigger('reloadGrid');
    var width = $(".jqGrid_wrapper").width();
    $("#user_list").setGridWidth(width);
}

function disable(id, state) {
    var type = state == 0 ? '启用' : '禁用';
    swal({
            title: type + "用户",
            text: "确认" + type + "该用户吗？，禁用后将不能登录！",
            // type: "input",
            type: "warning",
            showCancelButton: true,
            closeOnConfirm: false,
            confirmButtonText: "确认",
            cancelButtonText: "取消",
            inputPlaceholder: "输入原因",
        },
        function (msg) {
            $.post(baseUrl + "/user/updateState", {"userId": id, "state": state}, function (data) {
                if (data.code == 200) {
                    swal(type + "成功！", type + "成功", "success");
                } else {
                    swal(type + "失败！", type + "失败", "error");
                }
                switchData();
            }, "json")
        });
}

function del(id) {
    swal({
            title: "删除用户",
            text: "确认删除该用户吗？，删除的数据将不能恢复！",
            // type: "input",
            type: "warning",
            showCancelButton: true,
            closeOnConfirm: false,
            confirmButtonText: "确认",
            cancelButtonText: "取消",
            inputPlaceholder: "输入原因",
        },
        function (msg) {
            $.get(baseUrl + "/user/delete/" + id, function (data) {
                if (data.code == 200) {
                    swal("删除成功！", "删除成功", "success");
                } else {
                    swal("删除失败！", "删除失败" + data.msg, "error");
                }
                switchData();
            }, "json");
        });
}

function modify(id) {
    swal({
        title: "修改密码",
        text: "确认修改密码吗？点击确定后无法返回！",
        type: "input",
        showCancelButton: true,
        confirmButtonColor: "#DD6B55",
        confirmButtonText: "确认",
        cancelButtonText: "取消",
        closeOnConfirm: false
    }, function (pwd) {
        if (pwd === false) return false;
        if (pwd === "") {
            swal.showInputError("请输入要修改的新密码!");
            return false
        }
        $.post(baseUrl + "/user/updateInfo", {"userId": id, "password": pwd}, function (data) {
            var code = data.code;
            if (code == 200) {
                swal("修改成功！", "恭喜你，密码修成功。", "success");
            } else {
                swal("修改失败！", data.msg, "error");
            }
            switchData();
        }, "json")
    });
}
