function del(id) {
    if(checkUserRole(id)){
        parent.layer.confirm('确认删除？', {
            btn: ['删除', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            parent.layer.close(index);
            $.ajax({
                type: "post",
                url: baseUrl + "/role/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/role/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else {
                        layer.msg(data.msg);
                    }
                }
            });
        }, function () {
            return;
        });
    }else{
        swal("该角色下有人员不能删除");
    }
};

//加载角色类型列表
loadType();

function loadType() {
    $("#type").empty();
    $("#type2").empty();
    $.get(baseUrl + "/dict/listByTypeCode2?typeCode=ROLE_TYPE", function (data) {
        // console.log("data0="+data)
        getResCode(data);
        var html = "<option value='' ></option>";
        $(data).each(function (i, item) {
            html += "<option value='" + item.code + "' >" + item.name + "</option>";
        });
        $("#type").html(html);
        $("#type2").html(html);
    }, "json");
}

//加载职位列表
loadCode();

/**
 * 加载角色名称
 */
function CharacterName() {
    var nameQc =$("#typeQc").val()
    if (nameQc ==""){
        return;
    }else {
        $("#nameQc").empty();
        layui.use(["form"], function () {
            $.get(baseUrl + "/user/CharacterName/"+nameQc, function (data) {
                $("#nameQc").append("<option value=''>请选择</option>");
                $(data).each(function (i, d) {


                    $("#nameQc").append("<option value='" + d.name + "'>" + d.name + "</option>");
                });
                layui.form.on("select(mediaUserFilter1)",function(resultData){
                    $("#queryForm input[name='name']").val(resultData.value);
                    $("#queryForm input[name='name']").val($("#name").find("option:selected").text());

                });

                layui.form.render('select');
            }, "json");
        });}


}

function loadCode() {
    $("#code").empty();
    $("#code2").empty();
    $.get(baseUrl + "/dict/listByTypeCode2?typeCode=ROLE_CODE", function (data) {
        // console.log("data1="+data)
        getResCode(data);
        var html = "<option value='' ></option>";
        $(data).each(function (i, item) {
            html += "<option value='" + item.code + "' >" + item.name + "</option>";
        });
        $("#code").html(html);
        $("#code2").html(html);
    }, "json");
}

$(document).ready(function () {


    /**
     * 下拉框的change事件
     */
    $("#changeRole").change(function () {
        var rid = $(this).val();
        roleInfo(rid);
    })

    var roleName = "";
    var type = "";
    var code = "";
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.fadeInRight .jqGrid_wrapper').width();
        $('#query_table_logs').setGridWidth(width);

        //查看角色弹窗表格宽度设置
        var viewWidth = $('#viewModal .jqGrid_wrapper').width();
        $('#user_table_logs').setGridWidth(viewWidth);
    });

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/role/listPg',
        datatype: "json",
        mtype: 'POST',
        postData: $("#queryForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        rownumbers: false,
        page: 1,//第一页
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
            {name: 'id', label: '角色编号', editable: true, hidden: true, width: 60},
            {name: 'type', label: '角色类型', editable: true, width: 80},
            {name: 'code', label: '职位', editable: true, width: 60},
            {name: 'name', label: '角色名称', editable: true, width: 140},
            {name: 'createTime', label: '创建时间', editable: true, width: 120},
            {name: 'user.name', label: '创建人', editable: true, width: 80},
            // {
            //     name: 'state', label: '状态', editable: true, width: 30, formatter: function (v, grid, rows, state) {
            //         return v > -1 ? "<span class='text-success'>有效</span>" : (v == -9 ? "<span class='text-danger'>已删除</span>" : "<span class='text-muted'>无效</span>");
            //     }
            // },
            {name: 'remark', label: '角色描述', editable: true, width: 100},
            {name: 'updateTime', label: '最后修改时间', editable: true, width: 120},
            // {name: 'leader_flag', label: '是否是领导', editable: true, width: 280},
            {
                name: 'operate', label: "操作", index: '', width: 160,
                formatter: function (value, grid, rows, state) {
                    var be = "<a class='text-info' onclick='view(" + rows.id + ")'>&nbsp;&nbsp;查看&nbsp;&nbsp;</a>";
                    var de = "<a class='text-muted'  onclick='del(" + rows.id + ")'>&nbsp;&nbsp;删除&nbsp;&nbsp;</a>";
                    // var se = "<a class='text-danger' data-toggle='modal' data-target='#roleModal' onclick='editRoleResource(" + rows.id + ")'>赋权</a>";
                    var se = "<a class='text-danger' data-toggle='modal' onclick='roleInfo(" + rows.id + ")'>&nbsp;&nbsp;赋权&nbsp;&nbsp;</a>";
                    return "     " + be + "     " + de + "    " + se + "    ";
                }
            }
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "角色列表",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            edit(rowid);
        }
    });

    //赋权关闭
    $("#cance3").click(function () {
        $("#roleModal").modal("hide");
    });

    $("#search").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });


    $("#add").click(function () {
        document.getElementById("editForm").reset();
        $("#type option").removeAttr("selected");
        $("#code option").removeAttr("selected");
        $("#editModal").modal("toggle");
        $(".save").show();
        $(".update").hide();
        selChange();
    })

    edit = function (id) {
        $.ajax({
            type: "post",
            url: baseUrl + "/role/view",
            data: {"id": id},
            dataType: "json",
            success: function (data) {
                $("#type option").removeAttr("selected");
                $("#code option").removeAttr("selected");
                $("#editModal").modal("toggle");
                $(".save").hide();
                $(".update").show();
                for (var attr in data.data.entity) {
                    $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);
                    if (attr == "type") {
                        $("#editForm #type option[value='" + data.data.entity[attr] + "']").attr("selected", "selected");
                    }
                    if (attr = "code") {
                        $("#editForm #code option[value='" + data.data.entity[attr] + "']").attr("selected", "selected");
                    }
                }
                type = $("#type").find("option:selected").text();
                code = $("#code").find("option:selected").text();
                selChange();
            }
        });
    }
    view = function (id) {
        $("#user_table_logs").emptyGridParam();
        $.ajax({
            type: "post",
            url: baseUrl + "/role/view",
            data: {"id": id},
            dataType: "json",
            success: function (data) {
                $("#viewModal").modal("toggle");
                $("#name1").val("");
                $("#deptName1").val("");
                for (var attr in data.data.entity) {
                    $("#viewForm [name=" + attr + "2" + "]").val(data.data.entity[attr]);
                    if (attr == "type") {
                        $("#viewForm #type2 option[value='" + data.data.entity[attr] + "']").attr("selected", "selected");
                    }
                    if (attr = "code") {
                        $("#viewForm #code2 option[value='" + data.data.entity[attr] + "']").attr("selected", "selected");
                    }
                }
                $("#roleId").val(id);
                $("#user_table_logs").jqGrid("setGridParam", {
                    postData: $("#userForm").serializeJson()
                }).trigger("reloadGrid");
            }
        });
    }

    $("#user_table_logs").jqGrid({
        url: baseUrl + '/role/queryUserByRoleId',
        datatype: "json",
        mtype: 'POST',
        postData: {roleId: $("#roleId").val()}, //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        rownumbers: false,
        page: 1,//第一页
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
            {name: 'userName', label: '用户名', editable: true, width: 80},
            {name: 'name', label: '姓名', editable: true, width: 80},
            {name: 'deptName', label: '部门', editable: true, width: 120},
            {name: 'phone', label: '电话', editable: true, width: 120},
            {name: 'qq', label: 'qq', editable: true, width: 120},
            {name: 'wechat', label: '微信', editable: true, width: 120},
            {name: 'createTime', label: '创建时间', editable: true, width: 120},
            {name: 'loginTime', label: '最近一次登录时间', editable: true, width: 120},
            {name: 'remark', label: '备注', editable: true, width: 240},
            {name: 'updateTime', label: '最后修改时间', editable: true, width: 120},
        ],
        pager: jQuery("#user_pager_logs"),
        viewrecords: true,
        caption: "该角色用户列表",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });
    $("#userSearch").click(function () {
        $("#user_table_logs").emptyGridParam();
        $("#user_table_logs").jqGrid('setGridParam', {
            postData: $("#userForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    function selChange() {
        $("#type").change(function () {
            type = $("#type").find("option:selected").text();
            if ($("#type").val() != "" && $("#code").val() != "") {
                roleName = type + code;
                $("#name").val(roleName);
            }
        })

        $("#code").change(function () {
            code = $("#code").find("option:selected").text();
            if ($("#type").val() != "" && $("#code").val() != "") {
                roleName = type + code;
                $("#name").val(roleName);
            }
        })
    }

    loadRoleList();

    $("#all").iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
    $('#all').on('ifChanged', function () {
        $("#groups").find(".i-checks").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
    });
    // loadCompany("#companyCodeOption") ;
});

submitHander = function (t, url) {
    if ($("#editForm").valid()) {
        layer.confirm("请确认角色信息", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            $.ajax({
                type: "post",
                url: baseUrl + "/role/checkName",
                data: {id: $("#id").val(), name: $("#name").val()},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    //判断角色名是否重复
                    if (data.data.flag) {
                        var formData = $("#editForm").serializeForm();
                        $.ajax({
                            type: "post",
                            url: url,
                            data: formData,
                            dataType: "json",
                            success: function (data) {
                                if (data.code == 200) {
                                    layer.msg(data.data.message, {time: 1000, icon: 6});
                                    $("#editModal").modal("hide");
                                    $("#query_table_logs").emptyGridParam();
                                    $("#query_table_logs").reloadCurrentData(baseUrl + '/role/listPg', $("#queryForm").serializeJson(), "json", null, null);
                                } else {
                                    swal(data.msg)
                                }
                            },
                            error: function (data) {
                                swal(data.msg)
                            }
                        });
                    } else {
                        swal("角色名重复！")
                    }

                },
                error: function (data) {
                    Ladda.stopAll();
                    swal(data.msg)
                }
            });
        }, function () {
            return;
        });
    }
}

/**
 * 设置默认选中角色
 * @param id
 */
function roleInfo(id) {
    layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
    $.ajax({
        type: "get",
        url: baseUrl + "/role/view?id=" + id,
        data: null,
        dataType: "json",
        async: true,
        cache: false,
        success: function (data) {
            if (getResCode(data))
                return;
            if (data.code == 200) {
                $("#changeRole").val(data.data.entity.id);
                $("[name='roleId']").val(data.data.entity.id);
                $("[name='roleId1']").val(data.data.entity.id);
                $("[name='checkId']").val(data.data.entity.id);
                loadAllRoles(id);
            }
        }
    });

    // $.get("/role/view?id=" + id, function (data) {
    //     if (getResCode(data))
    //         return;
    //     if (data.code == 200) {
    //         $("#changeRole").val(data.data.entity.id);
    //         $("[name='roleId']").val(data.data.entity.id);
    //         $("[name='roleId1']").val(data.data.entity.id);
    //         $("[name='checkId']").val(data.data.entity.id);
    //         loadAllRoles(id);
    //     }
    // }, "json");
}

/**
 * 查询角色列表给赋权页面赋值
 */
function loadRoleList() {
    $.get("/role/list", function (data) {
        if (data != null && data != '') {
            $(data).each(function (i, item) {
                // console.log(item);
                $("#changeRole").append("<option value='" + item.id + "'>" + item.name + "</option>");
            });
        }
    }, "json");
}


/**
 * 加载所有权限信息
 * @param id
 */
function loadAllRoles(id) {
    $.ajax({
        type: "get",
        url: baseUrl + "/group/queryGroupByRoleId/" + id,
        data: null,
        dataType: "json",
        async: true,
        cache: false,
        success: function (data) {
            if (data != null && data != '') {
                var html = '';
                $("#groups").empty();
                // 定义map用来保存选择的数据；
                var mapData = {};
                $(data).each(function (i, item) {
                    var parent = item.parentList;
                    var groupList = item.groupList;
                    html += ' <div class="col-sm-12  level2Div f_line "  style=" border-bottom: 1px dashed #eee;padding: 8px; width: 1200px; margin-left:30px ">\n' +
                        ' <div class="col-sm-2"style="height: 40px" >\n' +
                        '     <input type="hidden" name="groupId" class="form-control" value="' + parent.id + '">\n' +
                        '     <input type="checkbox" class="i-checks level2" style="position: relative"  value="' + parent.id + '">\n' +
                        '     <b style="font-size: 16px;">' + parent.name + '</b>\n' +
                        '     <input type="hidden" readonly="readonly" id="name"\n' +
                        '            name="name" class="form-control"></div>\n' +
                        ' <div class="col-sm-10">\n';
                    mapData[parent.id] = 0;
                    $(groupList).each(function (i, d) {
                        if (d != undefined) {
                            var checked = d.checkInfo ? 'checked="checked"' : "";
                            if (checked.length > 0) {
                                mapData[parent.id] = 1;
                            }
                            html += '     <div class="col-sm-3 level3Div">\n' +
                                '         <input type="hidden" name="groupId" class="form-control" value="' + d.id + '">\n' +
                                '         <input type="checkbox" class="i-checks level3" name="groupName" value="' + d.id + '" ' + checked + '>\n' +
                                '         <i></i><span>' + d.name + '</span></div>\n';
                            $(".level3").iCheck(d.checkInfo ? 'check' : 'uncheck');
                        }
                    });
                    html += ' </div></div>';
                });
                $("#groups").append(html);
                var dataValue;
                for (var key in mapData) {
                    dataValue = mapData[key];
                    if (dataValue == 1) {
                        $("input[type='checkbox'][value='" + key + "']").attr("checked", true);
                    }
                }
                $("#groups").find(".i-checks").iCheck({
                    checkboxClass: 'icheckbox_square-green',
                    radioClass: 'iradio_square-green',
                });
                $('.level2').on('ifChanged', function (event) {
                    var dom = $(this).parent().parent().next();
                    dom.find(" .level3").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
                });
                $("#roleModal").modal({backdrop: "static"});
            }
        }
    });
    /*$.get("/resource/queryResourceByRoleId/" + id, function (data) {
        if (data != null && data != '') {
            var html = '';
            $("#groups").empty();
            // 定义map用来保存选择的数据；
            var mapData = {};
            $(data).each(function (i, item) {
                var parent = item.parentList;
                var resourceList = item.resourceList;
                html += ' <div class="col-sm-12  level2Div f_line "  style=" border-bottom: 1px dashed #eee;padding: 8px; width: 1200px; margin-left:30px ">\n' +
                    ' <div class="col-sm-2"style="height: 40px" >\n' +
                    '     <input type="hidden" name="resourceId" class="form-control" value="' + parent.id + '">\n' +
                    '     <input type="checkbox" class="i-checks level2" style="position: relative"  value="' + parent.id + '">\n' +
                    '     <b style="font-size: 16px;">' + parent.name + '</b>\n' +
                    '     <input type="hidden" readonly="readonly" id="name"\n' +
                    '            name="name" class="form-control"></div>\n' +
                    ' <div class="col-sm-10">\n';
                mapData[parent.id] = 1;
                $(resourceList).each(function (i, d) {
                    if (d != undefined) {
                        var checked = d.checkInfo ? 'checked="checked"' : "";
                        if (checked.length <= 0) {
                            mapData[parent.id] = 0;
                        }
                        html += '     <div class="col-sm-3 level3Div">\n' +
                            '         <input type="hidden" name="resourceId" class="form-control" value="' + d.id + '">\n' +
                            '         <input type="checkbox" class="i-checks level3" name="resourceName" value="' + d.id + '" ' + checked + '>\n' +
                            '         <i></i><span>' + d.name + '</span></div>\n';
                        $(".level3").iCheck(d.checkInfo ? 'check' : 'uncheck');
                    }
                });
                html += ' </div></div>';
            });
            $("#groups").append(html);
            var dataValue;
            for (var key in mapData) {
                dataValue = mapData[key];
                if (dataValue == 1) {
                    $("input[type='checkbox'][value='" + key + "']").attr("checked", true);
                }
            }
            $("#groups").find(".i-checks").iCheck({
                checkboxClass: 'icheckbox_square-green',
                radioClass: 'iradio_square-green',
            });
            $('.level2').on('ifChanged', function (event) {
                var dom = $(this).parent().parent().next();
                dom.find(" .level3").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
            });
            $("#roleModal").modal({backdrop: "static"});
        }
    }, "json");*/
}

function save() {
    if ($("#form").valid()) {
        startModal("#saveRoleGroup");//锁定按钮，防止重复提交
        var roleId1 = $("#roleId1").val();
        var checkId = "";
        $('input:checkbox[name="groupName"]:checked').each(function () {
            checkId += this.value + "|";
        });
        $.ajax({
            type: "post",
            url: baseUrl + "/role/submitRoleGroup",
            data: {roleId: roleId1, checkId: checkId},
            dataType: "json",
            success: function (data) {
                Ladda.stopAll();
                $("#roleModal").modal("hide");
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 2000, icon: 6});
                    $("#query_table_logs").reloadCurrentData(baseUrl + '/role/listPg', $("#queryForm").serializeJson(), "json", null, null);
                } else {
                    swal(data.msg);
                }
            },
            error: function () {
                Ladda.stopAll();//隐藏加载按钮
            }
        });
    }
}

/**
 * 检测删除角色时是否有用户
 * @param id
 */
function checkUserRole(id) {
    var flag = false;
    $.ajax({
        type: "post",
        url: baseUrl + "/role/getUserByRoleId",
        data: {roleId: id},
        dataType: "json",
        async:false,
        success: function (data) {
            if(data.data.number>0){
                flag = false;
            }else {
                flag= true;
            }
        }
    });
    return flag;
}