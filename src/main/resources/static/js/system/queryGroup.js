var treeSetting;
var treeObj;
$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_logs').setGridWidth(width);
    });
    $("#table_logs").jqGrid({
        url: baseUrl + '/group/listPg',
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
        rowNum: 15,//每页显示记录数
        rowList: [15, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'id', label: '组id', editable: true, hidden: true, width: 60},
            {name: 'name', label: '组名称', editable: true, width: 80},
            {
                name: 'parent.name',
                label: '上级组',
                editable: true,
                width: 80,
                sortable: false,
                formatter: function (v, grid, rows, state) {
                    return v == "" || v == undefined ? '<span class=\'text-danger\'>一级菜单</span>' : v;
                }
            },
            {name: 'sort', label: '排序标志', editable: true, width: 40},
            {name: 'createTime', label: '创建时间', editable: true, width: 80,sortable: false},
            {name: 'user.name', label: '创建人', editable: true, width: 60,sortable: false},
            {name: 'updateTime', label: '最后修改时间', editable: true, width: 80,sortable: false},
            {name: 'updateUser.name', label: '最后更新人员', editable: true, width: 60,sortable: false},
            {
                name: 'state', label: '状态', editable: true,sortable: false, width: 40, formatter: function (v, grid, rows, state) {
                    return v == 0 ? "<span class='text-success'>有效</span>" : "<span class='text-danger'>删除</span>";
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 120,sortable: false,
                formatter: function (value, grid, rows, state) {
                    var html = "" ;
                    html += '<a class="text-success" onclick="viewResurce(' + rows.id + ')">&nbsp;&nbsp;编辑&nbsp;&nbsp;</a>';
                    html += "<a class='text-muted'  onclick='del(" + rows.id + ")'>&nbsp;&nbsp;删除&nbsp;&nbsp;</a>";
                    if(rows.parentId > 0){
                       html += "<a class='text-warning' data-toggle='modal' onclick='groupResource(" + rows.id + ",1)'>&nbsp;&nbsp;添加资源&nbsp;&nbsp;</a>";
                       html += "<a class='text-warning' data-toggle='modal' onclick='groupRole(" + rows.id + ",1)'>&nbsp;&nbsp;绑定角色&nbsp;&nbsp;</a>";
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

    //加载一级菜单列表
    $.ajax({
        type: "post",
        url: "/group/listParentGroups",
        dataType: "json",
        success: function (resData) {
            this.resData = resData;
            if (getResCode(resData)) return;
            $("#parentId").append('<option value="">请选择</option>');
            $("#parentId").append('<option value="0">一级组</option>');
            $(resData.data.groups).each(function (i, item) {
                var option = '<option value="' + item.id + '">' + item.name + '</option>';
                $("#parentId").append(option);
            });
        }
    });

/*    loadGroupList();
    loadGroupList2();*/
    //下拉框的change事件
    $("#changeGroup").change(function () {
        var rid = $(this).val();
        groupResource(rid);
    });
    $("#changeGroup2").change(function () {
        var rid = $(this).val();
        groupRole(rid);
    });
    $('#all2').on('ifChanged', function () {
        $("#groupsDiv").find(".i-checks").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
    });

    //资源树
    treeSetting = {
        view: {
            selectedMulti: false,
            fontCss:function (treeId, treeNode) {
                //根据是否菜单，决定字体颜色
                return treeNode.isMenu == 1 ? {color:"#7a7979"} : {};
            },
            addHoverDom: function (treeId, treeNode) {
                var aObj = $("#" + treeNode.tId + "_a");
                if(treeNode.isParent){
                    //如果存在，则不添加
                    if($("#child_choose_"+treeNode.id).length < 1){
                        //全选、取消全选按钮
                        var imgHtml = "<img src='/img/radioNotCheck.png' style='border: 0px solid red;width: 16px;height: 16px;'/>&nbsp;";
                        var allChooseHtml = "<span class='node_name'>全选</span>";
                        var selfBtnHtml = "<span data-value='0' id='child_choose_"+treeNode.id+"' style='display: inline-flex;align-items: center;justify-items: center;'>&nbsp;"+imgHtml+allChooseHtml+"</span>";
                        aObj.append(selfBtnHtml);
                        if($("#child_choose_"+treeNode.id)){
                            $("#child_choose_"+treeNode.id).click(function () {
                                //是否全选：0-未全选，点击全选、1-全选，点击未全选
                                var dataValue = $(this).attr("data-value");
                                if (dataValue == 1) {//取消全选
                                    $(this).attr("data-value", "0");//设置下次全选
                                    $(this).find("img").attr("src", "/img/radioNotCheck.png");
                                    treeObj.checkNode(treeNode, false, false);
                                    var nodes = [];
                                    nodes.push(treeNode);//包含自己
                                    getChilds(treeNode, nodes);
                                    if(nodes && nodes.length > 0){
                                        $.each(nodes, function (p, node) {
                                            treeObj.checkNode(node, false, false);
                                            checkedNodeIdList.remove(node.id);//移除缓存节点
                                        });
                                    }
                                } else {//进行全选
                                    $(this).attr("data-value", "1");//设置下次全选取消
                                    $(this).find("img").attr("src", "/img/radioCheck.png");
                                    treeObj.checkNode(treeNode, true, false);
                                    var nodes = [];
                                    nodes.push(treeNode);//包含自己
                                    getChilds(treeNode, nodes);
                                    if(nodes && nodes.length > 0){
                                        $.each(nodes, function (p, node) {
                                            treeObj.checkNode(node, true, false);
                                            //缓存添加选中节点
                                            if(!checkedNodeIdList.contains(node.id)){
                                                checkedNodeIdList.push(node.id);
                                            }
                                        });
                                    }
                                }
                                renderNodeList();//渲染列表
                            });
                        }
                    }else {
                        $("#child_choose_"+treeNode.id).css("display", "inline-flex");
                    }
                }
            },
            removeHoverDom: function (treeId, treeNode) {
                $("#child_choose_"+treeNode.id).css("display", "none");
            },
        },
        check: {
            enable: true,   //true / false 分别表示 显示 / 不显示 复选框或单选框
            autoCheckTrigger: false,   //true / false 分别表示 触发 / 不触发 事件回调函数
            chkStyle: "checkbox",   //勾选框类型(checkbox 或 radio）
            chkboxType: {"Y": "", "N": ""}//表示父子节点的联动效果,这里设置不联动
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback:{
            onCheck:function (event, treeId, treeNode) {
                treeObj.checkNode(treeNode, treeNode.checked, false);
                //判断当前是否选中，选中则取消，不选中则勾选
                if(treeNode.checked){
                    if(!checkedNodeIdList.contains(treeNode.id)){
                        checkedNodeIdList.push(treeNode.id);
                    }
                }else {
                    checkedNodeIdList.remove(treeNode.id);
                }

                renderNodeList();//渲染列表
            }
        }
    };
});

//获取子节点
function getChilds(currentTreeNode, nodes) {
    if(!nodes){
        nodes = [];
    }
    if(currentTreeNode.children && currentTreeNode.children.length > 0){
        $.each(currentTreeNode.children, function (i, node) {
            nodes.push(node);
            getChilds(node, nodes);
        });
    }
}

//菜单搜索
function searchResource() {
    var keyword = $("input[name='keyword']").val();
    var filterResourceList = [];
    var filterResourceIdList = [];
    if(allNodes && allNodes.length > 0){
        $.each(allNodes, function (ix, node) {
            if(checkedNodeIdList.contains(node.id)){
                node.checked = true;
            }else {
                node.checked = false;
            }
        });
    }
    if(keyword){
        $.each(allNodes, function (ix, node) {
            if(node.name.match(new RegExp(keyword, 'g')) != null){
                if(!filterResourceIdList.contains(node.id)){
                    filterResourceIdList.push(node.id);
                }
                addParentResource(node, filterResourceIdList);//添加父级菜单
                addChildResource(node, filterResourceIdList);//添加子级菜单
            }
        });
        $.each(allNodes, function (ix, node) {
            if(filterResourceIdList.contains(node.id)){
                //节点都展开
                node["open"] = true;
                filterResourceList.push(node);
            }
        });
        treeObj = $.fn.zTree.init($("#treeDemo"),treeSetting, filterResourceList);
    }else {
        treeObj = $.fn.zTree.init($("#treeDemo"),treeSetting, allNodes);
    }
}

//获取节点父节点
function addParentResource(currentNode, filterResourceIdList) {
    var pid = currentNode["pId"];
    if(pid && pid != 0 && allNodeMap[pid]){
        if(!filterResourceIdList.contains(allNodeMap[pid].id)){
            filterResourceIdList.push(allNodeMap[pid].id);
        }
        addParentResource(allNodeMap[pid], filterResourceIdList);
    }
}

//获取节点子节点
function addChildResource(currentNode, filterResourceIdList) {
    var id = currentNode.id;
    $.each(allNodes, function (c, childNode) {
        var pid = childNode["pId"];
        if(id == pid){
            if(!filterResourceIdList.contains(childNode.id)){
                filterResourceIdList.push(childNode.id);
            }
            addChildResource(childNode, filterResourceIdList);
        }
    });
}

//渲染节点路径
function getNodePath(nodeList) {
    var path = "";
    if(nodeList && nodeList.length > 0){
        var nodeNameList = [];
        $.each(nodeList, function (x, node) {
            nodeNameList.push(node.name);
        });
        path = nodeNameList.join(" -> ");
    }
    return path;
}

//渲染节点路径列表
function renderNodeList() {
    var html = "";
    //获取选择的菜单ID
    var checkNodes = treeObj.getCheckedNodes(true);
    if(checkNodes && checkNodes.length > 0){

        $.each(checkNodes, function (c, checkNode) {
            html += "<div style=\"border-bottom: 1px solid #cecece;width: 100%;padding: 6px 12px; white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                "        "+getNodePath(checkNode.getPath())+"\n" +
                "    </div>";
        });
    }
    $("#hasResource").html(html);
}

function chooseOne(chk) {
    //先取得同name的chekcBox的集合物件
    var obj = $("input[name='parentId']");
    $("input[name='parentId']").each(function (i, item) {
        if (item != chk) $(item).iCheck('uncheck');
        else $(item).iCheck('check');
    });
}

function del(id) {
    var lock = true ;
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if(lock) {
            lock = false;
            $.ajax({
                type: "post",
                url: baseUrl + "/group/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#table_logs").reloadCurrentData(baseUrl + "/group/listPg", $("#queryForm").serializeJson(), "json", null, null);
                    } else {
                        swal(data.msg) ;
                    }
                }
            });
        }
    }, function () {
        return;
    });

};

function viewResurce(id, isShow) {
    $("#editModal .update").hide();
    $("#editModal .view").hide();
    $("#editModal .add").hide();
    $("#editModal").modal("toggle");
    // $("#editForm").reset();
    var dis = "";
    if (isShow == 'show') {
        dis = 'disabled="disabled"';
    } else {
        $("#id").val(id);
    }
    $.ajax({
        type: "post",
        url: baseUrl + "/group/view",
        data: {id: id},
        dataType: "json",
        success: function (resData) {
            if (resData.code == 200) {
                var data = resData.data;
                var group = data.entity;
                $("#editForm").find("input").removeClass('error');
                $("#editForm").validate().resetForm();
                document.getElementById("editForm").reset();
                for (var attr in group) {
                    $("#editForm input[name=" + attr + "]:text").val(group[attr]);
                    $("#editForm input[name=" + attr + "]:text").attr("disabled", isShow == 'show');
                }
                var parentId = group.parentId;
                if (parentId == 0) {
                    $('#parentDiv').parent().hide();
                    $("#parentDiv .i-checks").iCheck('uncheck');
                    // $('#editForm input[name="parentId"]').val(0);
                    $('#url').removeAttr("required");
                    $('#url').removeAttr("aria-required");
                } else {
                    $('#parentDiv').parent().show();
                }

                $("#editForm #isParentYes").iCheck(parentId == 0 ? 'check' : 'uncheck');
                $("#editForm #isParentNo").iCheck(parentId != 0 ? 'check' : 'uncheck');
                var state = group.state;
                $("#editForm #stateYes").iCheck(state == 0 ? 'check' : 'uncheck');
                $("#editForm #stateNo").iCheck(state == 1 ? 'check' : 'uncheck');

                var isMenu = group.isMenu;
                // $("#editForm #isMenuYes").iCheck(isMenu == 0 ? 'check' : 'uncheck');
                // $("#editForm #isMenuNo").iCheck(isMenu == 1 ? 'check' : 'uncheck');
                if (isMenu == 0) {
                    $("#editForm #isMenuYes").iCheck('check');
                    $('#iconDiv').show();
                } else {
                    $("#editForm #isMenuNo").iCheck('check');
                    $('#iconDiv').hide();
                }
                $("#parentDiv").empty();
                //如果为查看则设置为disabled
                if (isShow == 'show') {
                    $("#editModal .view").show();
                    $("#editForm input[type='radio']").attr("disabled", "disabled");
                } else {
                    //修改显示修改保存按钮
                    $("#editModal .update").show();
                    $("#editForm input[type='radio']").removeAttr("disabled");
                }
                $(data.menus).each(function (i, item) {
                    if (group != null && group.parentId != null && item.id == group.parentId) {
                        var str = '<div class="col-sm-2"><label><input class="i-checks" type="checkbox" value="' + item.id + '" id="parentId" name="parentId" checked="checked" ' + dis + '><span>' + item.name + '</span></label> </div>';
                    } else {
                        var str = '<div class="col-sm-2"><label><input class="i-checks" type="checkbox" value="' + item.id + '" id="parentId" name="parentId" ' + dis + '><span>' + item.name + '</span></label> </div>';
                    }
                    $("#parentDiv").append(str);
                });
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
                    $('#parentDiv').parent().hide();
                    $("#parentDiv .i-checks").iCheck('uncheck');
                    //如果修改了第二次所有的菜单id都为0；
                    // $('#editForm input[name="parentId"]').val(0);
                    $('#url').removeAttr("required");
                    $('#url').removeAttr("aria-required");
                } else {
                    $('#parentDiv').parent().show();
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
            swal("所属菜单不能为空！！！");
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
            // formData.state = $("#editForm input[name='state']:checked").val();
            // formData.isMenu = $("#editForm input[name='isMenu']:checked").val();
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

var resData = null;

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
    if (resData != null) {
        showCheck(resData);
    } else {
        $.ajax({
            type: "post",
            url: baseUrl + "/group/listParentGroups",
            dataType: "json",
            success: function (resData) {
                showCheck(resData);
            }
        });
    }
}

function showCheck(resData) {
    var data = resData.data;
    $("#parentDiv").empty();
    $(data.groups).each(function (i, item) {
        var str = '<div  class="col-sm-2"><label><input class="i-checks" type="checkbox" value="' + item.id + '" name="parentId" id="parentId"><span>' + item.name + '</span></label> </div>';
        $("#parentDiv").append(str);
    });
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
        var isParent = $(this).val();
        if (isParent == 1) {
            $('#parentDiv').parent().show();
        } else {
            $('#parentDiv').parent().hide();
            $("#parentDiv .i-checks").iCheck('uncheck');
            //如果修改了第二次所有的菜单id都为0；
            // $('#editForm input[name="parentId"]').val(0);
            $('#url').removeAttr("required");
            $('#url').removeAttr("aria-required");
        }
    });
    $("#editForm input[name='isParent'][value='1']").iCheck("check");
}

//设置默认选中角色
function groupResource(id,state) {
    layer.msg("正在处理中，请稍候。", {time: 2000, shade: [0.7, '#393D49']})
    if(state=1){
        $("#changeGroup").empty();
        loadGroupList(id,state);
    }
    $.get("/group/view?id=" + id, function (data) {
        if (getResCode(data))
            return;
        if (data.code == 200) {
            loadAllResources(id);
        }
    }, "json");
}

//加载所有权限信息
function loadAllResources1(id) {
    $.get("/resource/queryResourceByGroupId/" + id, function (data) {
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
                mapData[parent.id] = 9;
                $(resourceList).each(function (i, d) {
                    if (d != undefined) {
                        var checked = d.checkInfo ? 'checked="checked"' : "";
                        if (checked.length > 0) {
                            mapData[parent.id] = 1;
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
            $("#resourceModal").modal({backdrop: "static"});
        }
    }, "json");
}

//加载所有权限信息
var checkedNodeIdList = [];
var allNodes = [];
var allNodeMap = {};
function loadAllResources(id) {
    checkedNodeIdList = [];//每次刷新初始化
    allNodes = [];//所有节点
    allNodeMap = {};
    $("#hasResource").html("");
    $.get("/resource/queryResourceByGroupId/" + id, function (data) {
        allNodes = data;
        if(allNodes && allNodes.length > 0){
            $.each(allNodes, function (ix, node) {
                allNodeMap[node.id] = node;
            });
        }
        treeObj = $.fn.zTree.init($("#treeDemo"),treeSetting, allNodes);
        //获取默认选择的菜单ID
        var checkNodes = treeObj.getCheckedNodes(true);
        if(checkNodes && checkNodes.length > 0){
            var html = "";
            $.each(checkNodes, function (c, checkNode) {
                html += "<div style=\"border-bottom: 1px solid #cecece;width: 100%;padding: 6px 12px; white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                    "        "+getNodePath(checkNode.getPath())+"\n" +
                    "    </div>";
                if(!checkedNodeIdList.contains(checkNode.id)){
                    checkedNodeIdList.push(checkNode.id);
                }
            });
            $("#hasResource").html(html);
        }
        $("#resourceModal").modal({backdrop: "static"});
    }, "json");
}

function saveResource() {
    if ($("#form").valid()) {
        startModal("#saveGroupResource");//锁定按钮，防止重复提交
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        var groupId = $("#groupId").val();
        var checkId = "";
        if(checkedNodeIdList && checkedNodeIdList.length > 0){
            checkId = checkedNodeIdList.join("|");
        }
        $.ajax({
            type: "post",
            url: baseUrl + "/group/submitGroupResource",
            data: {groupId: groupId, checkId: checkId},
            dataType: "json",
            success: function (data) {
                Ladda.stopAll();
                $("#resourceModal").modal("hide");
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 2000, icon: 6});
                    $("#query_table_logs").reloadCurrentData(baseUrl + '/group/listPg', $("#queryForm").serializeJson(), "json", null, null);
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
function loadGroupList(id,state) {
    $.get("/group/listAllChild", function (data) {
        if (data != null && data != '') {
            $(data).each(function (i, item) {
                $("#changeGroup").append("<option value='" + item.id + "'>" + item.name + "</option>");
            });
            $("#groupId").val(id);
            if(state==1){
                $("#changeGroup").find("option[value='"+id+"']").prop("selected",true);
            }
        }
    }, "json");
}

function loadGroupList2(id,state) {
    $.get("/group/listAllChild", function (data) {
        if (data != null && data != '') {
            $(data).each(function (i, item) {
                $("#changeGroup2").append("<option value='" + item.id + "'>" + item.name + "</option>");
            });
            $("#groupId2").val(id);
            if(state==1){
                // $("#changeGroup2").val(id);
                $("#changeGroup2").find("option[value='"+id+"']").prop("selected",true);
            }
        }
    }, "json");
}

//设置默认选中角色
function groupRole(id,state) {
    layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']})
    if(state==1){
        $("#changeGroup2").empty();
        loadGroupList2(id,state);
    }
    $.get("/group/view?id=" + id, function (data) {
        if (getResCode(data))
            return;
        if (data.code == 200) {
            loadAllRoles(id);
        }
    }, "json");
}

//反向赋权加载所有角色及选中的角色
function loadAllRoles(id) {
    $("#roleModal").modal({backdrop: "static"});
    $("#groupsDiv").empty();
    $.ajax({
        type: "post",
        url: baseUrl + "/role/editGroupRole",
        data: {"id": id},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                $("#roleForm input[name='id']").val(id);
                var str = "" ;
                for (var i = 0; i < data.data.allRole.length; i++) {
                    var flag = false ;
                    for (var j = 0; j < data.data.selRole.length; j++) {
                        if (data.data.allRole[i].id == data.data.selRole[j].id) {
                            flag = true ;
                            break ;
                        }
                    };
                    //flag=true表示选中，否则未选中
                    if(flag){
                        str += '<div  class="col-sm-3"><label><input class="i-checks" type="checkbox" value="' + data.data.allRole[i].id + '" name="groupId" checked="checked"><span>' + data.data.allRole[i].name + '</span></label> </div>';
                    }else{
                        str += '<div  class="col-sm-3"><label><input class="i-checks" type="checkbox" value="' + data.data.allRole[i].id + '" name="groupId"><span>' + data.data.allRole[i].name + '</span></label> </div>';
                    }
                }
                $("#groupsDiv").append(str);
            } else {
                swal(data.msg);
                $("#roleModal").modal("hide");
            }
            $('#groupsDiv .i-checks').iCheck({
                checkboxClass: 'icheckbox_square-green',
                radioClass: 'iradio_square-green',
            });
        }
    });
}

//反向赋权保存操作
function saveRole() {
    if ($("#form").valid()) {
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        startModal("#saveGroupRole");//锁定按钮，防止重复提交
        var groupId = $("#groupId2").val();
        var checkId = "";
        $('input:checkbox[name="groupId"]:checked').each(function () {
            checkId += this.value + "|";
        });
        $.ajax({
            type: "post",
            url: baseUrl + "/group/submitGroupRole",
            data: {groupId: groupId, checkId: checkId},
            dataType: "json",
            success: function (data) {
                Ladda.stopAll();
                $("#roleModal").modal("hide");
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 2000, icon: 6});
                    $("#query_table_logs").reloadCurrentData(baseUrl + '/group/listPg', $("#queryForm").serializeJson(), "json", null, null);
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