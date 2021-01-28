var mediaplate=false;
var roleMap = {};
var XMZJUser = [];
function del(id) {
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        $.ajax({
            type: "post",
            url: baseUrl + "/user/del",    //向后端请求数据的url
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $("#query_table_logs").reloadCurrentData(baseUrl + "/user/listPg", $("#queryForm").serializeJson(), "json", null, null);
                } else {
                    swal(data.msg);
                }
            }
        });
    }, function () {
        return;
    });
};

function onHandover(id) {
    layer.confirm('确认交接？交接后用户将变成待交接状态！', {
        btn: ['交接', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        $.ajax({
            type: "post",
            url: baseUrl + "/user/onHandover",    //向后端请求数据的url
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $("#query_table_logs").reloadCurrentData(baseUrl + "/user/listPg", $("#queryForm").serializeJson(), "json", null, null);
                } else {
                    swal(data.msg);
                }
            }
        });
    }, function () {
        return;
    });
};

function backUser(id) {
    layer.confirm('确认找回？找回后用户状态将变有效！',{
        btn:['交接','取消'],
        shade:false
    },function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍后。",{time: 3000, shade: [0.7, '#393D49']});
        $.ajax({
            type:"post",
            url:baseUrl+"/user/back",
            data:{id:id},
            dataType:"json",
            success:function (data) {
                if (data.code==200){
                    layer.msg(data.data.message,{time:1000,icon:6});
                    $("#query_table_logs").reloadCurrentData(baseUrl + "/user/listPg", $("#queryForm").serializeJson(), "json", null, null);
                }else {
                    swal(data.msg);
                }
            }
        })
        }
    )

}

function offHandover(id) {
    layer.confirm('确认取消交接？交接后用户将取消交接状态！', {
        btn: ['取消交接', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        $.ajax({
            type: "post",
            url: baseUrl + "/user/offHandover",    //向后端请求数据的url
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $("#query_table_logs").reloadCurrentData(baseUrl + "/user/listPg", $("#queryForm").serializeJson(), "json", null, null);
                } else {
                    swal(data.msg);
                }
            }
        });
    }, function () {
        return;
    });
};

/**
 * 判断选择所属角色不为空
 * @returns {boolean}
 */
function checkEmpty() {
    var obj = $(".i-checks");
    var j = 0;
    for (var i = 0; i < obj.length; i++) {
        if (obj[i].checked) {
            j++;
        }
    }
    if (j == 0) {
        swal("角色编辑不能为空！！！");
        return false;
    } else {
        return true;
    }
}
//判断新增用户职位是否为空
function checkPostIsEmpty() {
   var postId = $("#editForm select[name='postId']").val();
   if(postId=="" || postId==null || postId==undefined){
       swal("请选择职位");
       return false;
   }
   return true;
}

saveUserRole = function (t, url) {
    //页面判断选择所属角色不为空
    if (checkEmpty()) {
        var obj = document.getElementsByName("groupId");
        var checkId = "";
        for (var i = 0; i < obj.length; i++) {
            if (obj[i].checked) {
                checkId += obj[i].value + ","
            }
        }
        var userId = $("#id2").val();
        var XMZJFlag = false ;
        if (XMZJUser.length>0){
            XMZJFlag = true;
        }
        if (checkId.substr(checkId.length - 1) == ',') {
            checkId = checkId.substr(0, checkId.length - 1);
        }
        layer.confirm("请确认角色选择？", {
            btn: ["确认", "取消"],
            shade: false,
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);
            $.ajax({
                type: "post",
                url: url,
                data: {"checkId": checkId, "userId": userId,"XMZJFlag":XMZJFlag},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/user/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        $("#editUserRoleModal").modal("hide");
                    } else {
                        swal(data.msg);
                        $("#editUserRoleModal").modal("hide");
                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                    swal(data.msg);
                }
            }, function () {
                return;
            });
        });
    }
}

function editUserRole(id) {
    $("#groupDiv").empty();
    $("#editUserRoleModal").modal("toggle");
    $.ajax({
        type: "post",
        url: baseUrl + "/user/editUserRole",
        data: {"id": id},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                var str="";
                $("#editUserRoleForm input[name='id']").val(id);
                document.getElementById("editUserRoleForm").reset();
                for (var i = 0; i < data.data.allRole.length; i++) {
                    str = '<div  class="col-sm-4"><label><input class="i-checks" data-id="'+data.data.allRole[i].type+''+data.data.allRole[i].code+'" type="checkbox" value="' + data.data.allRole[i].id + '" name="groupId"><span>' + data.data.allRole[i].name + '</span></label> </div>';
                    $("#groupDiv").append(str);
                }
            } else {
                swal(data.msg);
                $("#editUserRoleModal").modal("hide");
            }
            $('.i-checks').iCheck({
                checkboxClass: 'icheckbox_square-green',
                radioClass: 'iradio_square-green',
            });
            for (var i=0;i<data.data.selRole.length ; i++){
                var typeCode = data.data.selRole[i].type + data.data.selRole[i].code;
                roleMap[data.data.selRole[i].id] =typeCode;
                $("#editUserRoleForm input[name='groupId'][value='"+data.data.selRole[i].id+"']").iCheck("check");
                $("#editUserRoleForm input[name='groupId'] [value='"+data.data.selRole[i].id+"']").parent().iCheck('check');
                if ("XMZJ" == typeCode){
                    XMZJUser.push(id);
                }
            }
            $(".i-checks").on('ifClicked',function (event) {
                var flag = $(this).is(':checked');
                var d=  $(event.target).attr("data-id");
                var ids = event.target.defaultValue;
                if (d == "XMZJ"){
                    if ( !flag){
                        XMZJUser.push(id);
                    }else {
                        XMZJUser = [];
                    }
                }
            });
        }
    });
};

function resetPassword(id) {
    //清除验证标签
    $("#resetForm").find("input").removeClass('error');
    $("#resetForm").validate().resetForm();
    document.getElementById("resetForm").reset();
    $("#myModal").modal('toggle');
    $("#id").val(id);
};

/**
 * 赋权媒体板块不为空
 * @returns {boolean}
 */
function checkMediaEmpty() {
    var obj = $(".i-checks");
    var k = 0;
    for (var i = 0; i < obj.length; i++) {
        if (obj[i].checked) {
            k++;
        }
    }
    if (k == 0) {
        swal("赋权媒体板块不能为空！");
        return false;
    } else {
        return true;
    }
}

/**
 * 加载媒体类型列表
 */
function loadMediaType() {
    $.get(baseUrl + "/mediaPlate/0", function (data) {     //mediaType/parentId/0
        getResCode(data);
        var html = "";
        $(data).each(function (i, item) {
            html += "<span class='col-md-2'><input type='checkbox' class='i-checks' name='typeId' data-id='" + item.id + "' value='" + item.id + "' />" + item.name + "</span>";
        });
        $("#mediaType").html(html);
        $('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green',
        });
    }, "json");
}

/**
 * 保存用户媒体板块
 */
function saveMediaType() {
    if (checkMediaEmpty()) {
        var data = $("#mediaTypeForm").serializeJson();
        $.ajax({
            type: "post",
            url: baseUrl + "/user/mediaType",
            data: {"param": JSON.stringify(data)},
            dataType: "json",
            // contentType: "application/json",
            success: function (data) {
                if (data.code == 200) {
                    swal("更新成功", "板块分配成功!", "success");
                    $("#query_table_logs").jqGrid("setGridParam", {
                        postData: $("#queryFrom").serializeJson()
                    }).trigger("reloadGrid");
                    $("#mediaTypeModal").modal("hide");
                } else {
                    swal("板块分配失败", "板块分配成功!", "warning");
                }
            }
        });
    }
}

/**
 * 显示媒体类型弹框
 * @param id UserId
 * @param id deptId
 */
function showMediaType(id, deptId) {
    $(".i-checks").iCheck("uncheck");
    $("#mediaTypeModal").modal('toggle');
    $("#userId").val(id);
    $("#departId").val(deptId);
    $.ajax({
        type: "post",
        url: baseUrl + "/mediaPlate/userId",  //mediaType/listByUserId
        data: {"userId": id},
        dataType: "json",
        // contentType: "application/json",
        success: function (data) {
            // console.log(JSON.stringify(data))
            $(data).each(function (j, type) {
                // console.log(JSON.stringify(type))
                $("#mediaType>span").each(function (i, d) {
                    var dataId = $(d).find("input").attr("data-id");
                    if (type.id == dataId) {
                        $(d).find("input").iCheck('check');
                        return true;
                    }
                });
            });
        }
    });
};

$(document).ready(function () {
    loadMediaType();//加载媒体类型
    loadRoles($("#roleId"));//加载角色
    var e = "<i class='fa fa-times-circle'></i> ";
    $("#editForm").validate({
        rules: {
            no: {maxlength: 20},
            userName: {
                required: !0, checkUserName: true,
                // remote: {
                //     url: baseUrl + "/user/checkUserName", // 后台处理程序
                //     type: "post", // 数据发送方式
                //     dataType: "json", // 接受数据格式
                //     data: { // 要传递的数据
                //         "id": function () {
                //             return $("#editForm input[name='id']").val();
                //             ;
                //         },
                //         "userName": function () {
                //             return $("#userName").val();
                //         }
                //     },
                //     message: "用户名重复！",
                //     dataFilter: function (data) {
                //         //返回值是string，需要转换成json
                //         var obj = JSON.parse(data);
                //         return obj;
                //     }
                // }
            },
            name: {required: !0, minlength: 2, maxlength: 50},
            email: {email: true},
            qq: {checkQQ: true},
            phone: {checkPhone: true},
            // sex: {required: true},
        },
        messages: {
            no: {maxlength: e + "工号长度必须小于{0}个字符"},
            userName: {
                required: e + "请输入用户名",
                minlength: e + "用户名长度必须大于{0}个字符",
                maxlength: e + "用户名长度必须小于{0}个字符",
                // remote: e + "用户名重复"
            },
            name: {required: e + "请输入姓名", minlength: e + "姓名长度必须大于{0}个字符", maxlength: e + "姓名长度必须小于{0}个字符"},
            // sex: {required: e + "请选择性别"}
        }
    });
    //自定义正则表达式验证方法
    $.validator.addMethod("checkUserName", function (value, element, params) {
        var checkUserName = /^[a-zA-Z]{1}\w{1,49}$/;
        return this.optional(element) || (checkUserName.test(value));
    }, "请输入正确的用户名，首个字符为字母，其他为字母、数字和下划线，长度2-50！");
    $.validator.addMethod("checkPhone", function (value, element, params) {
        var checkPhone = /^((([0]\d{2,3}-)?\d{7,8})|([1]\d{10}))$/;
        return this.optional(element) || (checkPhone.test(value));
    }, "请输入正确的手机号码！");
    $.validator.addMethod("checkQQ", function (value, element, params) {
        var checkQQ = /^[1-9][0-9]{4,19}$/;
        return this.optional(element) || (checkQQ.test(value));
    }, "请输入正确的QQ号码！");

    $.jgrid.defaults.styleUI = 'Bootstrap';
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#query_table_logs').setGridWidth(width);
    });
    $('body').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            // 回车执行查询；
            $("#search").click();
        }
    });
    //媒体板块权限设置
    $.ajax({
        type:"post",
        url:"/user/getRoleForMediaPlate",
        data:null,
        dataType:"json",
        async:false,
        success:function (data) {
            if(data.code==200){
                mediaplate=true;
            }else{
                mediaplate=false;
            }
        }
    });

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/user/listPg',
        datatype: "json",
        postData: $("#queryForm").serializeJson(), //发送数据
        mtype: "post",
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
        // colNames: ['用户名','姓名', '职位', '电话', '操作'],
        colModel: [
            {name: 'id', label: '编号', editable: true, hidden: true, width: 60},
            {
                name: 'image', label: '头像', width: 30,
                formatter: function (value, grid, rows, state) {
                    value = (value == null || value == "") ? "/img/mrtx_2.png" : value.replace("\\images\\", "/images/")
                    return '<img style="width: 38px;height: 38px" src="' + value + '" onerror="this.src=\'/img/mrtx_2.png\'"/>';
                }
            },
            {name: 'no', label: '工号', editable: true, hidden: true, width: 40},
            {name: 'mJ', label: '是否媒介', hidden: true},
            {name: 'userName', label: '账号', editable: true, width: 40},
            {name: 'name', label: '姓名', editable: true, width: 30},
            {name: 'phone', label: '电话', editable: true, width: 40},
            {name: 'qq', label: 'QQ', editable: true, width: 35},
            {name: 'wechat', label: '微信', editable: true, width: 40},
            {name: 'companyCode', label: '公司', editable: true, width: 40},
            {name: 'deptName', label: '部门', editable: true, width: 40},
            {name: 'postName', label: '职务', editable: true, width: 40},
            {
                name: 'roles', label: '角色', editable: true, width: 60,
                formatter: function (value, grid, rows, state) {
                        var html = "";
                        $(value).each(function (i, role) {
                            if(role){
                                if (role.name =="默认角色"){
                                return "";
                            }
                            html += role.name + "/";
                            }
                        });
                        if (html.length > 0)
                            html = html.substr(0, html.length - 1);
                        return html;


                }
            },
            {
                name: 'state', label: '状态', editable: true, width: 30,
                formatter: function (value, grid, rows, state) {
                    //状态,0有效启用，1失效禁用 -1删除
                    switch (value) {
                        case 1:
                            return '<span class="text-success">有效</span>';
                        case -9:
                            return '<span class="text-danger">删除</span>';
                        // default:
                        //     return '<span class="text-success">有效</span>';
                    }
                }
            },
            {
                name: 'handoverState', label: '交接状态', editable: true, width: 30,
                formatter: function (value, grid, rows, state) {
                    if(value==1){
                        return '<span class="text-danger">待交接</span>';
                    }else{
                        return '<span class="text-success">正常</span>';
                    }
                }
            },
            {name: 'createTime', label: '创建时间', editable: true, width: 60},
            {name: 'updateTime', label: '最后更新时间', editable: true, width: 60},
            {name: 'loginTime', label: '最后登录时间', editable: true, width: 60},
            {name: 'loginIp', label: '最后登录Ip', editable: true, width: 45},
            {name: 'deptId', label: 'deptId', hidden: true},
            // {name: 'user.name', label: '上级姓名',editable: true,width: 60},
            {
                name: 'operate', label: "操作", index: '', width: 160,
                formatter: function (value, grid, rows, state) {
                    var handoverState = rows.handoverState ;
                    var html = "" ;
                    if (rows.state == 1) {
                        html += "<a class='text-muted' onclick='del(" + rows.id + ")'>删除</a>&nbsp;&nbsp;";
                        html += "<a class='text-warning' onclick='resetPassword(" + rows.id + ")'>重置密码</a>&nbsp;&nbsp;";
                        html += "<a class='text-danger' onclick='editUserRole(" + rows.id + ")'>角色编辑</a>&nbsp;&nbsp;";
                        if (rows.mJ && mediaplate) {
                            html += "<a class='text-navy' onclick='showMediaType(" + rows.id + "," + rows.deptId + ")' >媒体板块赋权&nbsp;&nbsp;</a>";
                        }
                        if(handoverState==1){
                            html += "<a class='text-muted' onclick='offHandover(" + rows.id + ")'>取消交接</a>&nbsp;&nbsp;";
                        }else{
                            html += "<a class='text-muted' onclick='onHandover(" + rows.id + ")'>&nbsp;交接&nbsp;</a>&nbsp;&nbsp;";
                        }
                    }
                    if (rows.state==-9){
                        html += "<a class='text-danger' onclick='backUser(" + rows.id + ")'>找回</a>&nbsp;&nbsp;";
                    }
                    return html;
                }
            }
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "用户列表",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            edit(rowid);
        },
    });
    function randomPassword(size) {
        var seed = new Array('A','B','C','D','E','F','G','H','I','J','K','L','M','N','P','Q','R','S','T','U','V','W','X','Y','Z',
            'a','b','c','d','e','f','g','h','i','j','k','m','n','p','Q','r','s','t','u','v','w','x','y','z',
            '2','3','4','5','6','7','8','9'
        );//数组
        seedlength = seed.length;//数组长度
        var createPassword = '';
        for (i=0;i<size;i++) {
            j = Math.floor(Math.random()*seedlength);
            createPassword += seed[j];
        }
        return createPassword;
    }



    $("#search").click(function () {
        // alert(JSON.stringify($("#user").serializeJson()));
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    // 加载部门数据；
    $('#treeview').treeview({
        onNodeSelected: function (event, data) {
            // console.log(data);
            $("#deptId").val(data.id);
            $("#deptName").val(data.text);
            $("#deptModal").modal('hide');

            loadPostData(data.id);
        }
    });

    $("#addBtn").click(function () {
        //清除验证标签
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();
        document.getElementById("editForm").reset();
        $("#editForm #name").removeAttrs("readonly","readonly");
        // 先清空公司数据；
        var companyObj = $("#editForm select[name='companyId']");
        companyObj.empty();
        // 清空职务数据；
        $("#editForm select[name='postId']").empty();
        $("#postId").val("");
        $("#postName").val("");
        $("#pass").val(randomPassword(7));
        // 加载公司数据；
        layui.use(["form"], function () {
            var param = null;
            //祥和和集团行政可以添加分公司的人员
            if(user.companyCode != "XH" && user.companyCode != "JT"){
                param = {companyCode: user.companyCode}
            }
            $.get(baseUrl + "/dept/listJTAllCompany", param, function (data) {
                if (data == null) {
                    getResCode(data);
                } else {
                    if (data.data.result && data.data.result.length > 0) {
                        for (var i = 0; i < data.data.result.length; i++) {
                            companyObj.append("<option value='" + data.data.result[i].id + "' data='" + data.data.result[i].code + "'>" + data.data.result[i].name + "</option>");
                        }
                        // 获取公司代码；
                        var companyCodeObj = $("#editForm input[name='companyCode']");
                        companyCodeObj.val(data.data.result[0].code);
                        // 加载部门数据；
                        $('#treeview').treeview({
                            data: [getTreeData1(data.data.result[0].id)]
                        });
                    }
                    // 初始化；
                    layui.form.render();
                    // 下拉框的onchange事件；
                    layui.form.on("select(companyId)", function (companyData) {
                        // 清空数据；
                        $("#deptId").val("");
                        $("#deptName").val("");
                        $("#postId").val("");
                        $("#editForm select[name='postId']").empty();
                        $("#editForm input[name='postName']").val("");
                        // 初始化；
                        layui.form.render();
                        // 更新隐藏域；
                        companyCodeObj.val(getLayUISelectText(companyData, "data"));
                        // 加载部门数据；
                        $('#treeview').treeview({
                            data: [getTreeData1(companyData.value)]
                        });
                    });
                    $("#editModal").modal("toggle");
                    $(".save").show();
                    $(".update").hide();
                }
            }, "json");
        });
    });

    //获取客户保护，媒体板块赋权可以操作的人员
    // function hasRoleForUser() {
    //     var len = user.roles.length;
    //     for (var i = 0; i < len; i++) {
    //         if ((user.roles[i].type == 'XT' && user.roles[i].code == 'BZ') || (user.roles[i].type == 'XT' && user.roles[i].code == 'ZY')) {
    //             return true;
    //             break;
    //         }
    //     }
    //     return false;
    // }

    function edit(id) {
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();
        $("#editForm #name").attr("readonly","readonly");
        $.ajax({
            type: "post",
            url: baseUrl + "/user/view",
            data: {"id": id},
            dataType: "json",
            success: function (data) {
                if (data.data.user == null) {
                    swal("失败", "该用户已删除。", "warning");
                } else {
                    for (var attr in data.data.user) {
                        $("#editForm input[name=" + attr + "][type!='radio']").val(data.data.user[attr]);
                        if (attr === "sex") {
                            setFormRadioChecked($("#editForm input[name='sex']"), data.data.user[attr]);
                        }
                        if (attr === "isMgr") {
                            setFormRadioChecked($("#editForm input[name='isMgr']"), data.data.user[attr]);
                        }
                        if (attr === "remark") {
                            $("#editForm #remark").val(data.data.user[attr]);
                        }
                    }
                    $("#postId").val(data.data.user["postId"]);
                    $("#companyCode").val(data.data.user["companyCode"]);

                    // 获取下拉框对象；
                    var companyObj = $("#editForm select[name='companyId']");
                    companyObj.empty();
                    // 加载公司数据；
                    layui.use(["form"], function () {
                        $.post(baseUrl + "/dept/allCompany", {level: 1}, function (data) {
                            if (data == null) {
                                getResCode(data);
                            } else {
                                if (data.length > 0) {
                                    var oldCompany = $("#companyCode").val();
                                    if (oldCompany.length <= 0) {
                                        oldCompany = data[0].companyCode;
                                    }
                                    var oldDept = data[0].id;
                                    for (var i = 0; i < data.length; i++) {
                                        if (oldCompany == data[i].companyCode) {
                                            oldDept = data[i].id;
                                            companyObj.append("<option value='" + data[i].id + "' data='" + data[i].companyCode + "' selected='selected'>" + data[i].name + "</option>");
                                        } else {
                                            companyObj.append("<option value='" + data[i].id + "' data='" + data[i].companyCode + "' >" + data[i].name + "</option>");
                                        }
                                    }

                                    // 获取公司代码；
                                    var companyCodeObj = $("#editForm input[name='companyCode']");
                                    companyCodeObj.val(oldCompany);

                                    // 加载部门数据；
                                    $('#treeview').treeview({
                                        data: [getTreeData1(oldDept)]
                                    });

                                    // 加载职务数据；
                                    loadPostData($("#deptId").val());
                                }
                                // 初始化；
                                layui.form.render();

                                // 下拉框的onchange事件；
                                layui.form.on("select(companyId)", function (companyData) {
                                    // 清空数据；
                                    $("#deptId").val("");
                                    $("#deptName").val("");

                                    // 更新隐藏域；
                                    companyCodeObj.val(getLayUISelectText(companyData, "data"));

                                    $("#postId").val("");
                                    $("#editForm select[name='postId']").empty();
                                    $("#editForm input[name='postName']").val("");
                                    // 初始化；
                                    layui.form.render();

                                    // 加载部门数据；
                                    $('#treeview').treeview({
                                        data: [getTreeData1(companyData.value)]
                                    });
                                });
                            }

                            $("#editModal").modal("toggle");
                            $(".save").hide();
                            $(".update").show();
                        }, "json");
                    });
                }
            }
        });
    }

    submitHander = function (t, url) {
        if ($("#editForm").valid() && checkPostIsEmpty() && checkRepeatName() && checkRepeatUserName()) {
            //姓名去除前后空格
            var name = $("#editForm #name").val();
            $("#editForm #name").val($.trim(name));
            var formData = $("#editForm").serializeJson();
            layer.confirm("请确认用户信息", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                $.ajax({
                    type: "post",
                    url: url,
                    data: formData,
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            $("#query_table_logs").jqGrid("setGridParam", {
                                postData: $("#queryForm").serializeJson()
                            }).trigger("reloadGrid");
                            $("#editModal").modal("hide");
                        } else {
                            swal(data.msg);
                            $("#editModal").modal("hide");
                        }
                    },
                    error: function (data) {
                        Ladda.stopAll();
                        swal(data.msg);
                    }
                });
            }, function () {
                return;
            })
        } else {
            return;
        }
    }

    $("#resetForm").validate({
        rules: {
            password: {required: !0, minlength: 6, maxlength: 16},
        },
        messages: {
            password: {required: e + "请输入密码", minlength: e + "密码长度必须大于{0}个字符", maxlength: e + "密码长度必须小于{0}个字符"},
        }
    });
    $("#resetPwd").click(function () {
        var newpwd = $("#password").val();
        var reg = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,.\/]).{6,16}$/;
        var flag = reg.test(newpwd);
        if(flag == false){
            alert("新密码必须由 6-16位字母、数字、特殊符号线组成.");
            return ;
        }
        startModal("#resetPwd");//锁定按钮，防止重复提交
        if ($("#resetForm").valid()) {
            // console.log(JSON.stringify($("#deptForm").serializeJson()))
            $.ajax({
                type: "post",
                url: baseUrl + "/user/resetPassword",
                data: $("#resetForm").serializeJson(),
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    if (data.code == 200) {
                        $("#myModal").modal('hide');
                        $("#password").val();
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        // $("#myModal").modal('toggle');
                    } else {
                        // $("#password").val() ;
                        layer.msg(data.msg);
                        $("#myModal").modal('hide');
                    }
                }
            });
        }

    });
    $("#selDept").click(function () {
        $("#flage").val(0);
        $("#deptModal").modal('toggle');
    })
    $("#cleanDept").click(function () {
        if($("#flage").val()==0){
            $("#deptId").val("");
            $("#deptName").val("");
        }
    })

});

// 获取部门的职位数据；
function loadPostData(deptId) {
    $.post(baseUrl + "/entry/getPost", {deptId: deptId}, function (data) {
        var postObj = $("#editForm select[name='postId']");
        // 先清空；
        postObj.empty();
        var dataValue = data.data.post;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                var oldPost = $("#postId").val();
                oldPost = oldPost.length > 0 ? oldPost : dataValue[0].id;
                var oldPostName = dataValue[0].name;
                for (var i = 0; i < dataValue.length; i++) {
                    if (oldPost == dataValue[i].id) {
                        oldPostName = dataValue[i].name;
                        postObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        postObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                }

                // 设置职务名称；
                var postNameObj = $("#editForm input[name='postName']");
                postNameObj.val(oldPostName);
            }

            layui.use(["form"], function () {
                // 初始化；
                layui.form.render();

                // 下拉框的onchange事件；
                layui.form.on("select(postId)", function (postData) {
                    // 更新隐藏域；
                    postNameObj.val(getLayUISelectText(postData));
                });
            });

        }
    }, "json");
}

/**
 * 用户姓名去重
 */
function checkRepeatName() {
    var flag= false;
    $.ajax({
        type: "post",
        url: "/user/checkDuplicateName",
        data: {id:$("#checkUserId").val(),name:$("#editForm #name").val()},
        dataType: "json",
        async:false,
        success: function (data) {
            if (data.code==200){
                if (data.data.userState.sum>0){
                    flag=false;
                     var html ="";
                    html +="该姓名存在"+data.data.userState.sum+"个同名姓名，"
                    if (data.data.userState.state){
                        html += "其中"+data.data.userState.state+"个状态有效"
                    }
                    if (data.data.userState.delState){
                        html += "，其中"+data.data.userState.delState+"个状态无效"
                    }
                    swal({
                        title:"重复提醒",
                        text: html,
                    });
                    return;

                }else {
                    flag=true;
                }
            }
            if (data.code==1002){
                swal({
                    title: "重复提醒",
                    text: data.msg,
                });
            }
            if (getResCode(data))
                return;
        }

    });
    return flag;
}

/**
 * 用户名去重
 */
function checkRepeatUserName() {
    var flag = false;
    $.ajax({
        type: "post",
        url: "/user/checkDuplicateUserName",
        data: {id:$("#checkUserId").val(),name:$("#userName").val()},
        dataType: "json",
        async:false,
        success: function (data) {
            if (data.code==200){
                if (data.data.userState.sum>0){
                    flag=false;
                    var html ="";
                    html +="该用户名存在"+data.data.userState.sum+"个相同用户名，"
                    if (data.data.userState.state){
                        html += "其中"+data.data.userState.state+"个状态有效"
                    }
                    if (data.data.userState.delState){
                        html += "，其中"+data.data.userState.delState+"个状态无效"
                    }
                    swal({
                        title:"重复提醒",
                        text: html,
                    });
                    return;

                }else {
                    flag=true;
                }
            }
            if (data.code==1002){
                swal({
                    title: "重复提醒",
                    text: data.msg,
                });
            }
            if (getResCode(data))
                return;
        }
    });
    return flag;
}

function getTreeData1(deptId) {
    var deptTreeData = {};
    $.ajax({
        type: "POST",
        url: baseUrl + "/dept/listForSonTreeView",
        data: {deptId: deptId},
        dataType: "json",
        async: false,
        success: function (result) {
            var arrays = result.data.list;
            if (arrays != null && arrays.length > 0) {
                deptTreeData = arrays[0];
            }
        }
    });
    return deptTreeData;
}

// 设置单选框；
function setFormRadioChecked(obj, inputValue) {
    obj.each(function () {
        $(this).removeAttr("checked");
        // 移除ICheck样式；
        $(this).parent().removeClass("checked");
    });
    // 空值的处理；
    inputValue = inputValue == null ? 0 : inputValue;
    obj.each(function () {
        if ($(this).val() == inputValue) {
            $(this).prop("checked", true);
            // 移除ICheck样式；
            $(this).parent().addClass("checked");
        }
    });
}

// 获取LayUI下拉框选中的文本；
function getLayUISelectText(selectData, name) {
    var options = selectData.elem.options;
    var selectedIndex = selectData.elem.selectedIndex;
    if (options && selectedIndex != undefined) {
        if (name) {
            return options[selectedIndex].attributes[name].value;
        } else {
            return options[selectedIndex].text;
        }
    } else {
        return "";
    }
}


//部门选择
$(function () {
    $('#treeview').treeview({
        data: [getTreeData(isZC())],
        onNodeSelected: function (event, data) {
            if($("#flage").val()==1){
                $("#deptId1").val(data.id);
                $("#deptName1").val(data.text);
                $("#deptModal").modal('hide');
            }
        }
    });

    $("#selDept1").click(function () {
        $("#flage").val(1);
        $("#deptModal").modal('toggle');
        $('#treeview').treeview({
            data: [getTreeData(isZC())],
            onNodeSelected: function (event, data) {
                if($("#flage").val()==1){
                    $("#deptId1").val(data.id);
                    $("#deptName1").val(data.text);
                    $("#deptModal").modal('hide');
                }
            }
        });
    });
    $("#cleanDept1").click(function () {
        $("#deptId1").val("");
        $("#deptName1").val("");
    });
})
//判断当前用户是否总裁
var isZC = function () {
    var roles = user.roles;//获取用户角色
    var isZC = false;//是否总裁角色
    if(roles){
        for(var i=0; i < roles.length; i++){
            if(roles[i].code == 'ZC' || roles[i].code == 'FZC'){
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
    if(deptCompanyCode == "JT" ||deptCompanyCode == "XH"){
        requestData(null,"/dept/getRootDept","POST",function (result) {
            var root = result.data.root;
            if (root){
                deptId = root.id;//整个集团的业务和媒介部
            }else{
                deptId = 517;//整个集团的业务和媒介部
            }
        });
    }else if(deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL" ||deptCode == "RS" ||deptCode == "XZ" ||deptCode == "JS"){
        requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST",function (result) {
            var company = result.data.company;
            if (company){
                deptId = company.id;//整个集团的业务和媒介部
            }
        });
    }
    //具体查询
    requestData({deptId: deptId},"/dept/listForSonTreeView","POST",function (result) {
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
var requestData = function (data, url, requestType,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: false,
        success: callBackFun
    });
}