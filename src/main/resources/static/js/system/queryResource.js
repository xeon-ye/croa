function chooseOne(chk) {
    //先取得同name的chekcBox的集合物件
    var obj = $("input[name='parentId']");
    $("input[name='parentId']").each(function (i, item) {
        if (item != chk) $(item).iCheck('uncheck');
        else $(item).iCheck('check');
    });
}

function del(id) {
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function () {
        $.ajax({
            type: "post",
            url: baseUrl + "/resource/del",    //向后端请求数据的url
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $("#table_logs").reloadCurrentData(baseUrl + "/resource/listPg", $("#queryForm").serializeJson(), "json", null, null);
                } else {
                    swal(data.msg) ;
                }
            }
        });
    }, function () {
        return;
    });

};

$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_logs').setGridWidth(width);
    });
    $("#table_logs").jqGrid({
        url: baseUrl + '/resource/listPg',
        datatype: "json",
        mtype: 'post',
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
            {name: 'id', label: '角色编号', editable: true, hidden: true, width: 60},
            {name: 'name', label: '菜单名称', editable: true, width: 80},
            {name: 'createTime', label: '创建时间', editable: true, width: 80,sortable: false},
            {name: 'user.name', label: '创建人', editable: true, width: 60,sortable: false},
            {
                name: 'isMenu', label: '是否菜单',sortable: false, editable: true, width: 40, formatter: function (v, grid, rows, state) {
                    return v == 0 ? "<span class='text-success'>是</span>" : "<span class='text-danger'>否</span>";
                }
            },
            {name: 'updateTime', label: '最后修改时间', editable: true, width: 80,sortable: false},
            {name: 'updateUser.name', label: '最后更新人员', editable: true, width: 60,sortable: false},
            {
                name: 'parent.name',
                label: '上级菜单',
                editable: true,
                width: 80,
                sortable: false,
                formatter: function (v, grid, rows, state) {
                    return v == "" || v == undefined ? '<span class=\'text-danger\'>一级菜单</span>' : v;
                }
            },
            {name: 'sort', label: '排序标志', editable: true, width: 40},
            {name: 'url', label: '菜单路径', editable: true, width: 180,sortable: false},
            {
                name: 'state', label: '状态', editable: true,sortable: false, width: 40, formatter: function (v, grid, rows, state) {
                    return v == 0 ? "<span class='text-success'>有效</span>" : (v == -1 ? "<span class='text-danger'>删除</span>" : "<span class='text-muted'>无效</span>");
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 100,sortable: false,
                formatter: function (value, grid, rows, state) {
                    var html = "" ;
                    html += '<a class="text-success" onclick="viewResurce(' + rows.id + ')">&nbsp;&nbsp;编辑&nbsp;&nbsp;</a>';
                    html += "<a class='text-muted'  onclick='del(" + rows.id + ")'>&nbsp;&nbsp;删除&nbsp;&nbsp;</a>";
                    if(rows.url){
                        html += "<a class='text-danger' data-toggle='modal' onclick='groupInfo(" + rows.id + ")'>&nbsp;&nbsp;分组&nbsp;&nbsp;</a>";
                    }
                    return html;
                }
            }
        ],
        pager: jQuery("#pager_logs"),
        viewrecords: true,
        caption: "菜单列表",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            //page('/role/view?id=' + rowid, '角色详情');
            // edit(rowid);
            viewResurce(rowid, 'show');
        },
    });
    $("#search").click(function () {
        // alert(JSON.stringify($("#role").serializeJson()));
        $("#table_logs").emptyGridParam();
        $("#table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $("#iconAdd").click(function () {
        var sort = $("#sort").val();
        sort++;
        $("#sort").val(sort);
    });
    $("#iconDel").click(function () {
        var sort = $("#sort").val();
        if(sort<=0){
            sort=0;
        }else{
            sort--;
        }
        $("#sort").val(sort);
    });

    $('#all').on('ifChanged', function () {
        $("#groups").find(".i-checks").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
    });
    /**
     * 加载菜单列表
     */
    $.ajax({
        type: "post",
        url: "/resource/listParentResources",
        dataType: "json",
        success: function (resData) {
            this.resData = resData;
            if (getResCode(resData)) return;
            $("#parentId").append('<option value="">请选择父级菜单</option>');
            $("#parentId").append('<option value="0">一级菜单</option>');
            $(resData.data.resources).each(function (i, item) {
                var option = '<option value="' + item.id + '">' + item.name + '</option>';
                $("#parentId").append(option);
            });
        }
    });

    loadResourceList();
    $("#changeResource").change(function () {
        var rid = $(this).val();
        groupInfo(rid);
    });

    //加载图标
    iconObj.init();
});

//下拉列表改变事件
function selectChange(t) {
    getAllMenu();
    if($(t).val() != 0 && menuMap[$(t).val()]){
        var childMenus = [];
        getChildMenuId(menuMap[$(t).val()], childMenus);
        $("#queryForm input[name='parentId']").val(childMenus.join(","));
    }else {
        $("#queryForm input[name='parentId']").val($(t).val());
    }
}

//获取所有子菜单列表
function getChildMenuId(parentMenu, childMenus) {
    childMenus.push(parentMenu.id);
    if(parentMenu.childs && parentMenu.childs.length > 0){
        $.each(parentMenu.childs, function (c, childMenu) {
            getChildMenuId(childMenu, childMenus);
        });
    }
}

function viewResurce(id, isShow) {
    $("#editModal .update").hide();
    $("#editModal .view").hide();
    $("#editModal .add").hide();
    $("#editModal").modal("toggle");
    // $("#editForm").reset();
    var dis = "";
    var isEdit = true;
    if (isShow == 'show') {
        dis = 'disabled="disabled"';
        isEdit = false;
    } else {
        isEdit = true;
        $("#id").val(id);
    }
    getAllMenu();//获取整个菜单树
    $.ajax({
        type: "post",
        url: baseUrl + "/resource/view",
        data: {id: id},
        dataType: "json",
        success: function (resData) {
            if (resData.code == 200) {
                var data = resData.data;
                var resource = data.resource;
                $("#editForm").find("input").removeClass('error');
                $("#editForm").validate().resetForm();
                document.getElementById("editForm").reset();
                for (var attr in resource) {
                    $("#editForm input[name=" + attr + "]:text").val(resource[attr]);
                    $("#editForm input[name=" + attr + "]:text").attr("disabled", isShow == 'show');

                    if(attr == "icon" && resource[attr]){
                        $("#faShow").attr("class", resource[attr]);
                    }
                }
                var parentId = resource.parentId;
                if (parentId == 0) {
                    $('#parentDiv').parent().parent().hide();
                    $("#parentDiv .i-checks").iCheck('uncheck');
                    // $('#editForm input[name="parentId"]').val(0);
                } else {
                    $('#parentDiv').parent().parent().show();
                }

                $("#editForm #isParentYes").iCheck(parentId == 0 ? 'check' : 'uncheck');
                $("#editForm #isParentNo").iCheck(parentId != 0 ? 'check' : 'uncheck');
                var state = resource.state;
                $("#editForm #stateYes").iCheck(state == 0 ? 'check' : 'uncheck');
                $("#editForm #stateNo").iCheck(state == 1 ? 'check' : 'uncheck');

                var isMenu = resource.isMenu;
                // $("#editForm #isMenuYes").iCheck(isMenu == 0 ? 'check' : 'uncheck');
                // $("#editForm #isMenuNo").iCheck(isMenu == 1 ? 'check' : 'uncheck');
                if (isMenu == 0) {
                    $("#editForm #isMenuYes").iCheck('check');
                    $('#iconDiv').show();
                } else {
                    $("#editForm #isMenuNo").iCheck('check');
                    $('#iconDiv').hide();
                }
                //如果为查看则设置为disabled
                if (isShow == 'show') {
                    $("#editModal .view").show();
                    $("#editForm input[type='radio']").attr("disabled", "disabled");
                } else {
                    //修改显示修改保存按钮
                    $("#editModal .update").show();
                    $("#editForm input[type='radio']").removeAttr("disabled");
                }
                renderParentResource(resource.id, (menuMap[resource.parentId] ? menuMap[resource.parentId].parentId : 0), isEdit, parentId);//渲染父级菜单
            } else {
                layer.msg(resData.msg);
            }
            $('.i-checks').iCheck({
                checkboxClass: 'icheckbox_square-green',
                radioClass: 'iradio_square-green',
            });
            $('#parentDiv .i-checks').on('ifChecked', function (event) { //ifCreated 事件应该在插件初始化之前绑定
                chooseOne(this);
            });
            $("#editForm input[name='isMenu']").on("ifChecked", function (i) {
                var isMenu = $(this).val();
                if (isMenu == 0) {
                    $('#iconDiv').show();
                } else {
                    $('#iconDiv').hide();
                }
            });
            $("#editForm input[name='isParent']").on("ifChecked", function (i) {
                // var isMenu = $("input[name='isParent']:checked").val();
                var isParent = $(this).val();
                if (isParent == 0) {
                    $('#parentDiv').parent().parent().hide();
                    $("#parentDiv .i-checks").iCheck('uncheck');
                    //如果修改了第二次所有的菜单id都为0；
                    // $('#editForm input[name="parentId"]').val(0);
                } else {
                    $('#parentDiv').parent().parent().show();
                }
            });
        }
    });
}

//判断所属菜单组不为空
function checkEmpty() {
    // var obj = document.getElementsByName("parentId");
    if ($("input[name='isParent']:checked").val() == 1) {
        var j = 0;
        $("#parentDiv .i-checks").each(function (i) {
            if (this.checked) {
                j++;
            }
        });
        if (j == 0) {
            layer.msg("所属父级菜单不能为空!", {time: 2000, icon: 5});
            return false;
        } else {
            return true;
        }
    }
    return true;
}

submitHander = function (t, url) {
    if ($("#editForm").valid() && checkEmpty()) {
        layer.confirm("请确资源信息", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            var formData = $("#editForm").serializeJson();
            formData.state = $("#editForm input[name='state']:checked").val();
            formData.isMenu = $("#editForm input[name='isMenu']:checked").val();
            if (formData.isParent == 0) {
                formData.parentId = 0;
            }
            $.ajax({
                type: "post",
                url: url,
                data: formData,
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (getResCode(data)) {
                        return;
                    }
                    if (data.code == 200) {
                        //如果添加的是菜单，则清空缓存，后面模态框重新加载父级
                        if(formData.isMenu != 1){
                            clearMenu();
                        }
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#table_logs").emptyGridParam() ;
                        $("#table_logs").jqGrid("setGridParam", {
                            // postData: $("#queryForm").serializeJson()
                        }).trigger("reloadGrid");
                        $("#editModal").modal("hide");
                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                    layer.msg(data.msg);
                }
            });
        }, function () {
            return;
        })
    }
}

var menuTreeList = [];//缓存整个菜单树
var parentMenuMap = {};//缓存父级节点字段，格式：{parentId, []}
var menuMap = {};//缓存节点字段
function add() {
    $("#editForm input").removeAttr("disabled");
    //清除验证标签
    $("#editForm").find("input").removeClass('error');
    $("#editForm").validate().resetForm();
    document.getElementById("editForm").reset();
    $("#editModal").modal('toggle');
    $("#editModal .add").show();
    $("#editModal .update").hide();
    $("#editModal .view").hide();

    getAllMenu();//获取整个菜单树

    showCheck();
}

//清除缓存
function clearMenu() {
    menuTreeList = [];//缓存整个菜单树
    parentMenuMap = {};//缓存节点字段，格式：{parentId, []}
    menuMap = {};//缓存节点字段
}

//请求所有菜单
function getAllMenu() {
    if(!menuTreeList || menuTreeList.length < 1){
        $.ajax({
            type: "post",
            url: baseUrl + "/resource/listAllMenu",
            dataType: "json",
            async:false,
            success: function (data) {
                if(data && data.data){
                    menuTreeList = data.data.resources;
                }
            }
        });
    }
    if(!parentMenuMap || Object.getOwnPropertyNames(parentMenuMap) < 1){
        $.each(menuTreeList, function (i, menu) {
            loadParentMenu(menu);
        });
    }
}

//获取所有节点
function loadParentMenu(menu) {
    var parentId = menu.parentId;
    if(!parentMenuMap[parentId]){
        parentMenuMap[parentId] = [];
    }
    menuMap[menu.id] = menu;
    parentMenuMap[parentId].push(menu);
    if(menu.childs && menu.childs.length > 0){
        $.each(menu.childs, function (m, childMenu) {
            loadParentMenu(childMenu);
        });
    }
}

function showCheck() {
    renderParentResource(null, 0, true, null);//渲染父级菜单
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
    $('.i-checks').on('ifChecked', function (event) { //ifCreated 事件应该在插件初始化之前绑定
        chooseOne(this)
    });
    $('#parentDiv .i-checks').on('ifChecked', function (event) { //ifCreated 事件应该在插件初始化之前绑定
        chooseOne(this);
    });
    $("#editForm input[name='isMenu']").on("ifChecked", function (i) {
        var isMenu = $(this).val();
        if (isMenu == 0) {
            $('#iconDiv').show();
        } else {
            $('#iconDiv').hide();
        }
    });
    // 默认隐藏；
    $('#iconDiv').hide();

    $("#editForm input[name='isParent']").on("ifChecked", function (i) {
        // var isMenu = $("input[name='isParent']:checked").val();
        var isParent = $(this).val();
        if (isParent == 0) {
            $('#parentDiv').parent().parent().hide();
            $("#parentDiv .i-checks").iCheck('uncheck');
            //如果修改了第二次所有的菜单id都为0；
            // $('#editForm input[name="parentId"]').val(0);
        } else {
            $('#parentDiv').parent().parent().show();
        }
    });
    $("#editForm input[name='isParent'][value='1']").iCheck("check");
}

//渲染父级菜单
function renderParentResource(id, parentPreId, isEdit, parantId) {
    var resources = parentMenuMap[parentPreId] || [];
    var html = "";
    if(isEdit && parentPreId != 0){
        var oldParentId = menuMap[parentPreId] ? menuMap[parentPreId].parentId : "";
        html = "<div class='col-sm-3' style='padding-left: 0px;color: #2c8f7b;'><label onclick='renderParentResource("+id+","+oldParentId+", "+isEdit+","+parantId+")' style='cursor: pointer;'>返回上级菜单</label><label style=\"margin-left: 2px;width: 15px;height: 15px;padding: 2px;cursor: pointer;\"><span class=\"fa fa-mail-reply\"></span></label></div>";
    }
    if(resources && resources.length > 0){
        var disable = isEdit ? "" : "disabled";
        $(resources).each(function (i, item) {
            //父级菜单排除自己
            if(!id || id != item.id){
                //判断是否可以选择下级菜单
                var display = (isEdit && item.childs && item.childs.length > 0) ? "display: inline-flex;align-items: center;justify-items: center;" : "display: none;";
                if (parantId && item.id == parantId) {
                    html += '<div class="col-sm-3" style="padding-left: 0px;"><label><input class="i-checks" type="checkbox" value="' + item.id + '"  name="parentId" checked="checked" ' + disable + '><span>' + item.name + '</span></label><label onclick="renderParentResource('+id+','+item.id+', '+isEdit+', '+parantId+')" style="margin-left: 2px;width: 15px;height: 15px;padding: 2px;cursor: pointer;'+display+'"><span class="fa fa-plus"></span></label></div>';
                } else {
                    html += '<div class="col-sm-3" style="padding-left: 0px;"><label><input class="i-checks" type="checkbox" value="' + item.id + '"  name="parentId" ' + disable + '><span>' + item.name + '</span></label><label onclick="renderParentResource('+id+','+item.id+', '+isEdit+', '+parantId+')" style="margin-left: 2px;width: 15px;height: 15px;padding: 2px;cursor: pointer;'+display+'"><span class="fa fa-plus"></span></label></div>';
                }
            }
        });
    }
    $("#parentDiv").html(html);
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
}

//设置分组
function groupInfo(id) {
    $("#groups").empty();
    layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
    $.get("/resource/view?id=" + id, function (data) {
        if (getResCode(data))
            return;
        if (data.code == 200) {
            $("#changeResource").val(data.data.resource.id);
            $("[name='resourceId']").val(data.data.resource.id);
            // $("[name='resource1']").val(data.data.resource.id);
            // $("[name='checkId']").val(data.data.resource.id);
            loadAllGroups(id);
        }
    }, "json");
}

//加载所有权限信息
function loadAllGroups(id) {
    $.get("/group/queryGroupByResourceId/" + id, function (data) {
        if (data != null && data != '') {
            var html = '';
            // 定义map用来保存选择的数据；
            var mapData = {};
            $(data).each(function (i, item) {
                var parent = item.parent;
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
                            '         <input type="hidden" name="resourceId" class="form-control" value="' + d.id + '">\n' +
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
            $("#groupModal").modal({backdrop: "static"});
        }
    }, "json");
}
function save() {
    if ($("#form").valid()) {
        startModal("#saveResourceGroup");//锁定按钮，防止重复提交
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        var resourceId = $("#resourceId").val();
        var checkId = "";
        $('input:checkbox[name="groupName"]:checked').each(function () {
            checkId += this.value + "|";
        });
        $.ajax({
            type: "post",
            url: baseUrl + "/resource/submitResourceGroup",
            data: {resourceId: resourceId, checkId: checkId},
            dataType: "json",
            success: function (data) {
                Ladda.stopAll();
                $("#groupModal").modal("hide");
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 2000, icon: 6});
                    $("#query_table_logs").reloadCurrentData(baseUrl + '/resource/listPg', $("#queryForm").serializeJson(), "json", null, null);
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

//查询角色列表给赋权页面赋值
function loadResourceList() {
    $.get("/resource/listChild", function (data) {
        if (data != null && data != '') {
            $(data).each(function (i, item) {
                // console.log(item);
                $("#changeResource").append("<option value='" + item.id + "'>" + item.name + "</option>");
            });
        }
    }, "json");
}

//矢量图标
var iconObj = {
    iconList:[
        "fa fa-adjust",
        "fa fa-adn",
        "fa fa-align-center",
        "fa fa-align-justify",
        "fa fa-align-left",
        "fa fa-align-right",
        "fa fa-amazon",
        "fa fa-ambulance",
        "fa fa-anchor",
        "fa fa-android",
        "fa fa-angellist",
        "fa fa-angle-double-down",
        "fa fa-angle-double-left",
        "fa fa-angle-double-right",
        "fa fa-angle-double-up",
        "fa fa-angle-down",
        "fa fa-angle-left",
        "fa fa-angle-right",
        "fa fa-angle-up",
        "fa fa-apple",
        "fa fa-archive",
        "fa fa-area-chart",
        "fa fa-arrow-circle-down",
        "fa fa-arrow-circle-left",
        "fa fa-arrow-circle-o-down",
        "fa fa-arrow-circle-o-left",
        "fa fa-arrow-circle-o-right",
        "fa fa-arrow-circle-o-up",
        "fa fa-arrow-circle-right",
        "fa fa-arrow-circle-up",
        "fa fa-arrow-down",
        "fa fa-arrow-left",
        "fa fa-arrow-right",
        "fa fa-arrow-up",
        "fa fa-arrows",
        "fa fa-arrows-alt",
        "fa fa-arrows-h",
        "fa fa-arrows-v",
        "fa fa-asterisk",
        "fa fa-at",
        "fa fa-automobile",
        "fa fa-backward",
        "fa fa-balance-scale",
        "fa fa-ban",
        "fa fa-bank",
        "fa fa-bar-chart",
        "fa fa-bar-chart-o",
        "fa fa-barcode",
        "fa fa-bars",
        "fa fa-battery-0",
        "fa fa-battery-1",
        "fa fa-battery-2",
        "fa fa-battery-3",
        "fa fa-battery-4",
        "fa fa-battery-empty",
        "fa fa-battery-full",
        "fa fa-battery-half",
        "fa fa-battery-quarter",
        "fa fa-battery-three-quarters",
        "fa fa-bed",
        "fa fa-beer",
        "fa fa-behance",
        "fa fa-behance-square",
        "fa fa-bell",
        "fa fa-bell-o",
        "fa fa-bell-slash",
        "fa fa-bell-slash-o",
        "fa fa-bicycle",
        "fa fa-binoculars",
        "fa fa-birthday-cake",
        "fa fa-bitbucket",
        "fa fa-bitbucket-square",
        "fa fa-bitcoin",
        "fa fa-black-tie",
        "fa fa-bold",
        "fa fa-bolt",
        "fa fa-bomb",
        "fa fa-book",
        "fa fa-bookmark",
        "fa fa-bookmark-o",
        "fa fa-briefcase",
        "fa fa-btc",
        "fa fa-bug",
        "fa fa-building",
        "fa fa-building-o",
        "fa fa-bullhorn",
        "fa fa-bullseye",
        "fa fa-bus",
        "fa fa-buysellads",
        "fa fa-cab",
        "fa fa-calculator",
        "fa fa-calendar",
        "fa fa-calendar-check-o",
        "fa fa-calendar-minus-o",
        "fa fa-calendar-o",
        "fa fa-calendar-plus-o",
        "fa fa-calendar-times-o",
        "fa fa-camera",
        "fa fa-camera-retro",
        "fa fa-car",
        "fa fa-caret-down",
        "fa fa-caret-left",
        "fa fa-caret-right",
        "fa fa-caret-square-o-down",
        "fa fa-caret-square-o-left",
        "fa fa-caret-square-o-right",
        "fa fa-caret-square-o-up",
        "fa fa-caret-up",
        "fa fa-cart-arrow-down",
        "fa fa-cart-plus",
        "fa fa-cc",
        "fa fa-cc-amex",
        "fa fa-cc-diners-club",
        "fa fa-cc-discover",
        "fa fa-cc-jcb",
        "fa fa-cc-mastercard",
        "fa fa-cc-paypal",
        "fa fa-cc-stripe",
        "fa fa-cc-visa",
        "fa fa-certificate",
        "fa fa-chain",
        "fa fa-chain-broken",
        "fa fa-check",
        "fa fa-check-circle",
        "fa fa-check-circle-o",
        "fa fa-check-square",
        "fa fa-check-square-o",
        "fa fa-chevron-circle-down",
        "fa fa-chevron-circle-left",
        "fa fa-chevron-circle-right",
        "fa fa-chevron-circle-up",
        "fa fa-chevron-down",
        "fa fa-chevron-left",
        "fa fa-chevron-right",
        "fa fa-chevron-up",
        "fa fa-child",
        "fa fa-chrome",
        "fa fa-circle",
        "fa fa-circle-o",
        "fa fa-circle-o-notch",
        "fa fa-circle-thin",
        "fa fa-clipboard",
        "fa fa-clock-o",
        "fa fa-clone",
        "fa fa-close",
        "fa fa-cloud",
        "fa fa-cloud-download",
        "fa fa-cloud-upload",
        "fa fa-cny",
        "fa fa-code",
        "fa fa-code-fork",
        "fa fa-codepen",
        "fa fa-coffee",
        "fa fa-cog",
        "fa fa-cogs",
        "fa fa-columns",
        "fa fa-comment",
        "fa fa-comment-o",
        "fa fa-comments",
        "fa fa-comments-o",
        "fa fa-commenting",
        "fa fa-commenting-o",
        "fa fa-compass",
        "fa fa-compress",
        "fa fa-connectdevelop",
        "fa fa-contao",
        "fa fa-copy",
        "fa fa-copyright",
        "fa fa-creative-commons",
        "fa fa-credit-card",
        "fa fa-crop",
        "fa fa-crosshairs",
        "fa fa-css3",
        "fa fa-cube",
        "fa fa-cubes",
        "fa fa-cut",
        "fa fa-cutlery",
        "fa fa-dashboard",
        "fa fa-dashcube",
        "fa fa-database",
        "fa fa-dedent",
        "fa fa-delicious",
        "fa fa-desktop",
        "fa fa-deviantart",
        "fa fa-diamond",
        "fa fa-digg",
        "fa fa-dollar",
        "fa fa-dot-circle-o",
        "fa fa-download",
        "fa fa-dribbble",
        "fa fa-dropbox",
        "fa fa-drupal",
        "fa fa-edit",
        "fa fa-eject",
        "fa fa-ellipsis-h",
        "fa fa-ellipsis-v",
        "fa fa-empire",
        "fa fa-envelope",
        "fa fa-envelope-o",
        "fa fa-envelope-square",
        "fa fa-eraser",
        "fa fa-eur",
        "fa fa-euro",
        "fa fa-exchange",
        "fa fa-exclamation",
        "fa fa-exclamation-circle",
        "fa fa-exclamation-triangle",
        "fa fa-expand",
        "fa fa-expeditedssl",
        "fa fa-external-link",
        "fa fa-external-link-square",
        "fa fa-eye",
        "fa fa-eye-slash",
        "fa fa-eyedropper",
        "fa fa-facebook",
        "fa fa-facebook-f",
        "fa fa-facebook-official",
        "fa fa-facebook-square",
        "fa fa-fast-backward",
        "fa fa-fast-forward",
        "fa fa-fax",
        "fa fa-feed",
        "fa fa-female",
        "fa fa-fighter-jet",
        "fa fa-file",
        "fa fa-file-archive-o",
        "fa fa-file-audio-o",
        "fa fa-file-code-o",
        "fa fa-file-excel-o",
        "fa fa-file-image-o",
        "fa fa-file-movie-o",
        "fa fa-file-o",
        "fa fa-file-pdf-o",
        "fa fa-file-photo-o",
        "fa fa-file-picture-o",
        "fa fa-file-powerpoint-o",
        "fa fa-file-sound-o",
        "fa fa-file-text",
        "fa fa-file-text-o",
        "fa fa-file-video-o",
        "fa fa-file-word-o",
        "fa fa-file-zip-o",
        "fa fa-files-o",
        "fa fa-film",
        "fa fa-filter",
        "fa fa-fire",
        "fa fa-fire-extinguisher",
        "fa fa-firefox",
        "fa fa-flag",
        "fa fa-flag-checkered",
        "fa fa-flag-o",
        "fa fa-flash",
        "fa fa-flask",
        "fa fa-flickr",
        "fa fa-floppy-o",
        "fa fa-folder",
        "fa fa-folder-o",
        "fa fa-folder-open",
        "fa fa-folder-open-o",
        "fa fa-font",
        "fa fa-fonticons",
        "fa fa-forumbee",
        "fa fa-forward",
        "fa fa-foursquare",
        "fa fa-frown-o",
        "fa fa-futbol-o",
        "fa fa-gamepad",
        "fa fa-gavel",
        "fa fa-gbp",
        "fa fa-ge",
        "fa fa-gear",
        "fa fa-gears",
        "fa fa-genderless",
        "fa fa-get-pocket",
        "fa fa-gg",
        "fa fa-gg-circle",
        "fa fa-gift",
        "fa fa-git",
        "fa fa-git-square",
        "fa fa-github",
        "fa fa-github-alt",
        "fa fa-github-square",
        "fa fa-gittip",
        "fa fa-glass",
        "fa fa-globe",
        "fa fa-google",
        "fa fa-google-plus-square",
        "fa fa-google-wallet",
        "fa fa-graduation-cap",
        "fa fa-gratipay",
        "fa fa-group",
        "fa fa-h-square",
        "fa fa-hacker-news",
        "fa fa-hand-grab-o",
        "fa fa-hand-lizard-o",
        "fa fa-hand-o-down",
        "fa fa-hand-o-left",
        "fa fa-hand-o-right",
        "fa fa-hand-o-up",
        "fa fa-hand-paper-o",
        "fa fa-hand-peace-o",
        "fa fa-hand-pointer-o",
        "fa fa-hand-rock-o",
        "fa fa-hand-scissors-o",
        "fa fa-hand-spock-o",
        "fa fa-hand-stop-o",
        "fa fa-hdd-o",
        "fa fa-header",
        "fa fa-headphones",
        "fa fa-heart",
        "fa fa-heart-o",
        "fa fa-heartbeat",
        "fa fa-history",
        "fa fa-home",
        "fa fa-hospital-o",
        "fa fa-hotel",
        "fa fa-hourglass",
        "fa fa-hourglass-1",
        "fa fa-hourglass-2",
        "fa fa-hourglass-3",
        "fa fa-hourglass-end",
        "fa fa-hourglass-half",
        "fa fa-hourglass-o",
        "fa fa-hourglass-start",
        "fa fa-houzz",
        "fa fa-html5",
        "fa fa-i-cursor",
        "fa fa-ils",
        "fa fa-image",
        "fa fa-inbox",
        "fa fa-indent",
        "fa fa-industry",
        "fa fa-info",
        "fa fa-info-circle",
        "fa fa-inr",
        "fa fa-instagram",
        "fa fa-institution",
        "fa fa-internet-explorer",
        "fa fa-intersex",
        "fa fa-ioxhost",
        "fa fa-italic",
        "fa fa-joomla",
        "fa fa-jpy",
        "fa fa-jsfiddle",
        "fa fa-key",
        "fa fa-keyboard-o",
        "fa fa-krw",
        "fa fa-language",
        "fa fa-laptop",
        "fa fa-lastfm",
        "fa fa-lastfm-square",
        "fa fa-leaf",
        "fa fa-leanpub",
        "fa fa-legal",
        "fa fa-lemon-o",
        "fa fa-level-down",
        "fa fa-level-up",
        "fa fa-life-bouy",
        "fa fa-life-buoy",
        "fa fa-life-ring",
        "fa fa-life-saver",
        "fa fa-lightbulb-o",
        "fa fa-line-chart",
        "fa fa-link",
        "fa fa-linkedin",
        "fa fa-linkedin-square",
        "fa fa-linux",
        "fa fa-list",
        "fa fa-list-alt",
        "fa fa-list-ol",
        "fa fa-list-ul",
        "fa fa-location-arrow",
        "fa fa-lock",
        "fa fa-long-arrow-down",
        "fa fa-long-arrow-left",
        "fa fa-long-arrow-right",
        "fa fa-long-arrow-up",
        "fa fa-magic",
        "fa fa-magnet",
        "fa fa-mail-forward",
        "fa fa-mail-reply",
        "fa fa-mail-reply-all",
        "fa fa-male",
        "fa fa-map",
        "fa fa-map-marker",
        "fa fa-map-o",
        "fa fa-map-pin",
        "fa fa-map-signs",
        "fa fa-mars",
        "fa fa-mars-double",
        "fa fa-mars-stroke",
        "fa fa-mars-stroke-h",
        "fa fa-mars-stroke-v",
        "fa fa-maxcdn",
        "fa fa-meanpath",
        "fa fa-medium",
        "fa fa-medkit",
        "fa fa-meh-o",
        "fa fa-mercury",
        "fa fa-microphone",
        "fa fa-microphone-slash",
        "fa fa-minus",
        "fa fa-minus-circle",
        "fa fa-minus-square",
        "fa fa-minus-square-o",
        "fa fa-mobile",
        "fa fa-mobile-phone",
        "fa fa-money",
        "fa fa-moon-o",
        "fa fa-mortar-board",
        "fa fa-motorcycle",
        "fa fa-mouse-pointer",
        "fa fa-music",
        "fa fa-navicon",
        "fa fa-neuter",
        "fa fa-newspaper-o",
        "fa fa-object-group",
        "fa fa-object-ungroup",
        "fa fa-odnoklassniki",
        "fa fa-odnoklassniki-square",
        "fa fa-opencart",
        "fa fa-openid",
        "fa fa-opera",
        "fa fa-optin-monster",
        "fa fa-outdent",
        "fa fa-paint-brush",
        "fa fa-pagelines",
        "fa fa-paper-plane",
        "fa fa-paper-plane-o",
        "fa fa-paperclip",
        "fa fa-paragraph",
        "fa fa-paste",
        "fa fa-pause",
        "fa fa-paw",
        "fa fa-paypal",
        "fa fa-pencil",
        "fa fa-pencil-square",
        "fa fa-pencil-square-o",
        "fa fa-phone",
        "fa fa-phone-square",
        "fa fa-photo",
        "fa fa-picture-o",
        "fa fa-pie-chart",
        "fa fa-pied-piper",
        "fa fa-pied-piper-alt",
        "fa fa-pinterest",
        "fa fa-pinterest-p",
        "fa fa-pinterest-square",
        "fa fa-plane",
        "fa fa-play",
        "fa fa-play-circle",
        "fa fa-play-circle-o",
        "fa fa-plug",
        "fa fa-plus",
        "fa fa-plus-circle",
        "fa fa-plus-square",
        "fa fa-plus-square-o",
        "fa fa-power-off",
        "fa fa-print",
        "fa fa-puzzle-piece",
        "fa fa-qq",
        "fa fa-qrcode",
        "fa fa-question",
        "fa fa-quote-left",
        "fa fa-quote-right",
        "fa fa-ra",
        "fa fa-random",
        "fa fa-rebel",
        "fa fa-recycle",
        "fa fa-reddit",
        "fa fa-reddit-square",
        "fa fa-refresh",
        "fa fa-registered",
        "fa fa-remove",
        "fa fa-renren",
        "fa fa-reorder",
        "fa fa-repeat",
        "fa fa-reply",
        "fa fa-reply-all",
        "fa fa-retweet",
        "fa fa-rmb",
        "fa fa-road",
        "fa fa-rocket",
        "fa fa-rotate-left",
        "fa fa-rotate-right",
        "fa fa-rouble",
        "fa fa-rss",
        "fa fa-rss-square",
        "fa fa-rub",
        "fa fa-ruble",
        "fa fa-rupee",
        "fa fa-safari",
        "fa fa-save",
        "fa fa-scissors",
        "fa fa-search",
        "fa fa-search-minus",
        "fa fa-search-plus",
        "fa fa-sellsy",
        "fa fa-send",
        "fa fa-send-o",
        "fa fa-server",
        "fa fa-share",
        "fa fa-share-alt",
        "fa fa-share-alt-square",
        "fa fa-share-square",
        "fa fa-share-square-o",
        "fa fa-shekel",
        "fa fa-sheqel",
        "fa fa-shield",
        "fa fa-ship",
        "fa fa-shirtsinbulk",
        "fa fa-shopping-cart",
        "fa fa-sign-in",
        "fa fa-sign-out",
        "fa fa-signal",
        "fa fa-simplybuilt",
        "fa fa-sitemap",
        "fa fa-skyatlas",
        "fa fa-skype",
        "fa fa-slack",
        "fa fa-sliders",
        "fa fa-slideshare",
        "fa fa-smile-o",
        "fa fa-soccer-ball-o",
        "fa fa-sort",
        "fa fa-sort-alpha-asc",
        "fa fa-sort-alpha-desc",
        "fa fa-sort-amount-asc",
        "fa fa-sort-amount-desc",
        "fa fa-sort-asc",
        "fa fa-sort-desc",
        "fa fa-sort-down",
        "fa fa-sort-numeric-asc",
        "fa fa-sort-numeric-desc",
        "fa fa-sort-up",
        "fa fa-soundcloud",
        "fa fa-space-shuttle",
        "fa fa-spinner",
        "fa fa-spoon",
        "fa fa-spotify",
        "fa fa-square",
        "fa fa-square-o",
        "fa fa-stack-exchange",
        "fa fa-stack-overflow",
        "fa fa-star",
        "fa fa-star-half",
        "fa fa-star-half-empty",
        "fa fa-star-half-full",
        "fa fa-star-half-o",
        "fa fa-star-o",
        "fa fa-steam",
        "fa fa-steam-square",
        "fa fa-step-backward",
        "fa fa-step-forward",
        "fa fa-stethoscope",
        "fa fa-sticky-note",
        "fa fa-sticky-note-o",
        "fa fa-stop",
        "fa fa-street-view",
        "fa fa-strikethrough",
        "fa fa-stumbleupon",
        "fa fa-stumbleupon-circle",
        "fa fa-subscript",
        "fa fa-subway",
        "fa fa-suitcase",
        "fa fa-sun-o",
        "fa fa-superscript",
        "fa fa-support",
        "fa fa-table",
        "fa fa-tablet",
        "fa fa-tachometer",
        "fa fa-tag",
        "fa fa-tags",
        "fa fa-tasks",
        "fa fa-taxi",
        "fa fa-television",
        "fa fa-tencent-weibo",
        "fa fa-terminal",
        "fa fa-text-height",
        "fa fa-text-width",
        "fa fa-th",
        "fa fa-th-large",
        "fa fa-th-list",
        "fa fa-thumb-tack",
        "fa fa-thumbs-down",
        "fa fa-thumbs-o-down",
        "fa fa-thumbs-o-up",
        "fa fa-thumbs-up",
        "fa fa-ticket",
        "fa fa-times",
        "fa fa-times-circle",
        "fa fa-times-circle-o",
        "fa fa-tint",
        "fa fa-toggle-down",
        "fa fa-toggle-left",
        "fa fa-toggle-off",
        "fa fa-toggle-on",
        "fa fa-toggle-right",
        "fa fa-toggle-up",
        "fa fa-trademark",
        "fa fa-train",
        "fa fa-transgender",
        "fa fa-transgender-alt",
        "fa fa-trash",
        "fa fa-trash-o",
        "fa fa-tree",
        "fa fa-trello",
        "fa fa-tripadvisor",
        "fa fa-trophy",
        "fa fa-truck",
        "fa fa-try",
        "fa fa-tty",
        "fa fa-tumblr",
        "fa fa-tumblr-square",
        "fa fa-turkish-lira",
        "fa fa-tv",
        "fa fa-twitch",
        "fa fa-twitter",
        "fa fa-twitter-square",
        "fa fa-umbrella",
        "fa fa-underline",
        "fa fa-undo",
        "fa fa-university",
        "fa fa-unlink",
        "fa fa-unlock",
        "fa fa-unlock-alt",
        "fa fa-unsorted",
        "fa fa-upload",
        "fa fa-usd",
        "fa fa-user",
        "fa fa-user-md",
        "fa fa-user-plus",
        "fa fa-user-secret",
        "fa fa-user-times",
        "fa fa-users",
        "fa fa-venus",
        "fa fa-venus-double",
        "fa fa-venus-mars",
        "fa fa-viacoin",
        "fa fa-video-camera",
        "fa fa-vimeo",
        "fa fa-vimeo-square",
        "fa fa-vine",
        "fa fa-vk",
        "fa fa-volume-down",
        "fa fa-volume-off",
        "fa fa-volume-up",
        "fa fa-warning",
        "fa fa-wechat",
        "fa fa-weibo",
        "fa fa-weixin",
        "fa fa-whatsapp",
        "fa fa-wheelchair",
        "fa fa-wifi",
        "fa fa-wikipedia-w",
        "fa fa-windows",
        "fa fa-won",
        "fa fa-wordpress",
        "fa fa-wrench",
        "fa fa-xing",
        "fa fa-xing-square",
        "fa fa-y-combinator",
        "fa fa-y-combinator-square",
        "fa fa-yahoo",
        "fa fa-yc",
        "fa fa-yc-square",
        "fa fa-yelp",
        "fa fa-yen",
        "fa fa-youtube",
        "fa fa-youtube-play",
        "fa fa-youtube-square"],
    init: function () {
        var html = "";
        $.each(iconObj.iconList, function (x, icon) {
            var icon1 = icon.substr(3);
            html += "<div data-icon='"+icon+"' class=\"iconItemWrap\" onclick='iconObj.choose(this);'>\n" +
                "        <i class=\""+icon+"\"></i>\n" +
                "        <span title=\""+icon1+"\">"+icon1+"</span>\n" +
                "    </div>";
        });
        $(".iconContentWrap").html(html);
        $("#iconPanel").addClass("iconPanelCancel");
    },
    search:function (t) {
        var val = $(t).closest(".m-b").find("input").val();
        var html = "";
        $.each(iconObj.iconList, function (x, icon) {
            var icon1 = icon.substr(3);
            //当有值并且包含关系展示，搜索条件没值则全部展示
            if((val && icon1.match(new RegExp(val, 'g')) != null) || !val){
                html += "<div data-icon='"+icon+"' class=\"iconItemWrap\" onclick='iconObj.choose(this);'>\n" +
                    "        <i class=\""+icon+"\"></i>\n" +
                    "        <span title=\""+icon1+"\">"+icon1+"</span>\n" +
                    "    </div>";
            }
        });
        $(".iconContentWrap").html(html);

    },
    choose:function (t) {
        $("input[name='icon']").val($(t).attr("data-icon") || "");
        $("input[name='icon']").closest(".m-b").find("i").attr("class", ($(t).attr("data-icon") || "fa fa-user"));
        $("#iconPanel").addClass("iconPanelCancel");

    },
    inputClick:function (t) {
        //如果隐藏则显示，反之
        if($("#iconPanel").hasClass("iconPanelCancel")){
            $("#iconPanel").removeClass("iconPanelCancel");
            $("#iconPanel").find("input").focus();
        }else {
            $("#iconPanel").addClass("iconPanelCancel");
        }
    }
}