var addHintContentObj;
var treeSetting;
var treeObj;
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#versionHintTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });

    createTable(); //表格定义
    //富文本编辑
    addHintContentObj = new KindeditorTool({
        targetEl: "#addHintContent",
        uploadUrl: "/editUpload?filePart=version",
        filterItems: ['image', 'multiimage', 'flash', 'media', 'baidumap','/']
    });
    //部门树
     treeSetting = {
        view: {
            selectedMulti: false
        },
        check: {
            enable: true,   //true / false 分别表示 显示 / 不显示 复选框或单选框
            autoCheckTrigger: false,   //true / false 分别表示 触发 / 不触发 事件回调函数
            chkStyle: "checkbox",   //勾选框类型(checkbox 或 radio）
        },
        data: {
            simpleData: {
                enable: true
            }
        }
    };
});

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,dataType,async,callBackFun, contentType) {
    var param = {
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun,
        error: function () {
            Ladda.stopAll();
        }
    };
    if(contentType){
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
}

//表格定义
function createTable() {
    var $versionHintTable = $("#versionHintTable");
    $versionHintTable.jqGrid({
        url: baseUrl + '/versionHint/list',
        datatype: "json",
        postData: $("#queryForm").serializeJson(),
        mtype: 'post',
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
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15, //每页记录数
        rowList: [15, 30, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['id','日期','dateType','日期类型','备注'],//表头
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                hidden: true,
            },
            {
                name: 'deptId',
                label: 'deptId',
                hidden: true,
            },
            {
                name: 'content',
                label: 'content',
                hidden: true,
            },
            {
                name: 'deptName',
                label: '提示部门',
                width: 60,
                editable: true,
                sortable: false
            },
            {
                name: 'title',
                label: '标题',
                width: 80,
                editable: true,
                sortable: false
            },
            {
                label: '提示内容',
                width: 150,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                   return '<div style="max-height: 200px;overflow: auto;width: 100%;">'+row.content+'</div>';
                }
            },
            {
                name: 'user.name',
                label: '创建人',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'createDate',
                label: '创建时间',
                width: 40,
                editable: true,
                sortable: false
            },
            {
                name: 'state',
                label: '状态',
                width: 30,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    if(v == 1){
                        return "已通知";
                    }else{
                        return "未通知";
                    }
                }
            },
            {
                label: '操作',
                width: 40,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    var html = "<a class='text-success' onclick='edit("+row.id+")'>编辑&nbsp;</a>";
                    if(row.state == 0){
                        html += "<a class='text-success' onclick='notice("+row.id+")'>通知&nbsp;</a>";
                    }
                    html += "<a class='text-success' onclick='del("+row.id+")'>删除&nbsp;</a>";
                    return html;
                }
            },

        ],
        pager: "#versionHintTablePaper",
        viewrecords: true,
        caption: '系统版本更新列表',
        hidegrid: false,
        gridComplete: function () {
            var width = $('#versionHintTable').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#versionHintTable').setGridWidth(width);
        },ondblClickRow: function (rowid, iRow, iCol, e) {
            view(rowid);
        }
    });
    $versionHintTable.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    // $workDateTable.setGridHeight(530);
}

//刷新表格
function reflushTable() {
    //刷新表格
    $("#versionHintTable").emptyGridParam(); //清空历史查询数据
    $("#versionHintTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//添加版本
function addBtnClick() {
    $("#modalTitle").text("添加系统版本提示");
    addHintContentObj.readonly(false);
    $("#title").removeAttr("readonly");
    $("#saveBtn").css("display", "inline-block");
    $("#id").val("");
    $("#title").val("");
    addHintContentObj.setContent('');
    requestData(null,"/versionHint/listDeptTree", "get", "json", false, function (data) {
        treeObj = $.fn.zTree.init($("#treeDemo"),treeSetting,data);
    });
    $("#addVersionHintModal").modal("toggle");
}

//保存版本
function saveVersionHint() {
    if(!$("#addFormData").valid()){
        return;
    }
    var content = addHintContentObj.getContent();
    if(!content){
        layer.msg("日志内容不能为空！", {time: 2000, icon: 5});
        return;
    }
    var deptList = treeObj.getCheckedNodes(true); //获取所有选中的节点
    if(!deptList || deptList.length < 1){
        layer.msg("通知部门不能为空！", {time: 2000, icon: 5});
        return;
    }
    var deptArr = new Array();
    var deptNameArr = new Array();
    $.each(deptList, function (i, deptNode) {
       deptArr.push(deptNode.id);
       deptNameArr.push(deptNode.name);
    });
    var param = {id:$("#id").val(), title:$("#title").val(), deptName: deptNameArr.join(","),content:content, deptIds:deptArr};
    startModal("#saveBtn");
    requestData(JSON.stringify(param), "/versionHint/save","post", "json", true, function (data) {
        Ladda.stopAll();
        var message = param.id ? "系统版本提示编辑成功！" : "系统版本提示添加成功！"
        swal({
            title: data.code == 200 ? "成功!" : "失败",
            text: data.code == 200 ? message : data.msg,
            type: data.code == 200 ? "success" : "error",
            html: true
        });
        if(data.code==200){
            $("#addVersionHintModal").modal("toggle");
            reflushTable();
        }
    },true);
}

//编辑版本
function edit(hintId) {
    var rowData = $("#versionHintTable").jqGrid("getRowData", hintId);   //获取选中行信息
    $("#modalTitle").text("编辑系统版本提示");
    $("#id").val(rowData.id);
    $("#title").val(rowData.title);
    addHintContentObj.setContent(rowData.content);
    addHintContentObj.readonly(false);
    $("#title").removeAttr("readonly");
    $("#saveBtn").css("display", "inline-block");
    requestData(null,"/versionHint/listDeptTree", "get", "json", false, function (data) {
        $.each(data, function (i,deptNode) {
            var deptArr = rowData.deptId.split(',');
            if(deptArr.contains(deptNode.id)){
                deptNode.checked = true;
            }
        });
        treeObj = $.fn.zTree.init($("#treeDemo"),treeSetting,data);
    });
    $("#addVersionHintModal").modal("toggle");
}

//通知版本
function notice(hintId) {
    requestData({id:hintId}, "/versionHint/notice", "get", "json", true, function (data) {
        if (data.code == 200) {
            swal({
                title: "成功",
                text: "版本提示通知成功！",
                type: "success"
            },function () {
                reflushTable(); //刷新表格
            });
        } else {
            swal({
                title: "失败",
                text: data.msg,
                type: "error"
            });
        }
    });
}

//查看版本
function view(hintId) {
    var rowData = $("#versionHintTable").jqGrid("getRowData", hintId);   //获取选中行信息
    $("#modalTitle").text("查看系统版本提示");
    $("#title").val(rowData.title);
    addHintContentObj.setContent(rowData.content);
    addHintContentObj.readonly(true);//禁用
    $("#title").attr("readonly", true);//禁用
    $("#saveBtn").css("display", "none"); //inline-block
    requestData(null,"/versionHint/listDeptTree", "get", "json", false, function (data) {
        $.each(data, function (i,deptNode) {
            var deptArr = rowData.deptId.split(',');
            if(deptArr.contains(deptNode.id)){
                deptNode.checked = true;
            }
            deptNode.chkDisabled = true;//设置选择框被禁用
        });
        treeObj = $.fn.zTree.init($("#treeDemo"),treeSetting,data);
    });
    $("#addVersionHintModal").modal("toggle");
}

//删除版本
function del(hintId) {
    layer.confirm('您好，确定要删除当前版本提示！', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function () {
        layer.closeAll();
        requestData({id:hintId}, "/versionHint/del", "get", "json", true, function (data) {
            if (data.code == 200) {
                swal({
                    title: "成功",
                    text: "版本提示删除成功！",
                    type: "success"
                },function () {
                    reflushTable(); //刷新表格
                });
            } else {
                swal({
                    title: "失败",
                    text: data.msg,
                    type: "error"
                });
            }
        });
    }, function () {
    });
}

