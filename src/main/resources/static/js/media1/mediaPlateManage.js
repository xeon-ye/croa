$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#mediaPlateTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });

    layui.use(["form"], function () {
        layui.form.render();//layui-form
    });

    createTable(); //表格定义

    // 单选框、多选框美化；
    $(".i-checks").iCheck({
        checkboxClass: "icheckbox_square-green",
        radioClass: "iradio_square-green",
    });
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
    var $mediaPlateTable= $("#mediaPlateTable");
    $mediaPlateTable.jqGrid({
        url: baseUrl + '/mediaPlate/listPlate',
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
        rowNum: 10, //每页记录数
        rowList: [10, 20, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                hidden: true,
            },
            {
                name: 'parentType',
                label: 'parentType',
                hidden: true,
            },
            {
                name: 'standarPlatformFlag',
                label: 'standarPlatformFlag',
                hidden: true,
            },
            {
                name: 'name',
                label: '板块名称',
                width: 60,
                editable: true,
                sortable: false
            },
            {
                name: 'percent',
                label: '提成百分比',
                width: 60,
                editable: true,
                sortable: false
            },
            {
                label: '父级板块类型',
                width: 60,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    if(row.parentType == 1){
                        return "网络";
                    }else{
                        return "新媒体";
                    }
                }
            },
            {
                label: '是否标准平台',
                width: 60,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    if(row.standarPlatformFlag == 1){
                        return "标准平台";
                    }else{
                        return "<span class='text-red'>非标准平台</span>";
                    }
                }
            },
            {
                label: '站内站外',
                width: 40,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    if(row.isStation){
                        return "<span class='text-red'>站内</span>";
                    }else{
                        return "站外";
                    }
                }
            },
            {
                name: 'isStation',
                width: 40,
                editable: true,
                sortable: false,
                hidden:true,
                formatter: function (v, options, row) {
                    return row.isStation;
                }
            },
            {
                name: 'createDate',
                label: '创建时间',
                width: 60,
                editable: true,
                sortable: false
            },
            {
                name: 'isDelete',
                label: '状态',
                width: 30,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    if(v == 1){
                        return "<span class='text-red'>删除</span>";
                    }else{
                        return "正常";
                    }
                }
            },
            {
                label: '操作',
                width: 80,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    var html = "";
                    if(row.isDelete == 1){
                        html += "<a class='text-danger' onclick='recover("+row.id+")'>找回&nbsp;</a>";
                    }else {
                        html += "<a class='text-success' onclick='edit("+row.id+")'>编辑&nbsp;</a>";
                        html += "<a class='text-success' onclick='del("+row.id+")'>删除&nbsp;</a>";
                    }
                    return html;
                }
            },

        ],
        pager: "#mediaPlateTablePaper",
        viewrecords: true,
        caption: '媒体板块列表',
        hidegrid: false,
        gridComplete: function () {
            var width = $('#mediaPlateTable').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#mediaPlateTable').setGridWidth(width);
        },ondblClickRow: function (rowid, iRow, iCol, e) {
            view(rowid);
        }
    });
    $mediaPlateTable.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    // $mediaPlateTable.setGridHeight(530);
}

//刷新表格
function reflushTable() {
    //刷新表格
    $("#mediaPlateTable").emptyGridParam(); //清空历史查询数据
    $("#mediaPlateTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//添加版本
function addBtnClick() {
    $("#modalTitle").text("添加媒体板块");
    $("#addFormData")[0].reset();
    $("#id").val("");
    $("#saveBtn").attr("data-url", "/mediaPlate/save");
    $("#addMediaPlateModal").modal("toggle");
}

//保存版本
function saveMediaPlate(t) {
    if(!$("#addFormData").valid()){
        return;
    }

    var url = $(t).attr("data-url");
    if(url){
        startModal("#saveBtn");
        requestData(JSON.stringify($("#addFormData").serializeForm()), url,"post", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code==200){
                layer.msg("媒体板块操作成功！", {time: 2000, icon: 6});
                $("#addMediaPlateModal").modal("toggle");
                reflushTable();
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        },true);
    }
}

//编辑版本
function edit(mediaPlateId) {
    var rowData = $("#mediaPlateTable").jqGrid("getRowData", mediaPlateId);   //获取选中行信息
    $("#modalTitle").text("编辑媒体板块");
    $("#id").val(rowData.id);
    $("#name").val(rowData.name);
    $("#percent").val(rowData.percent);
    $("#parentType").val(rowData.parentType);
    if(rowData.isStation=='true'){
        $("#isStationY").iCheck('check');
        $("#isStationN").iCheck('uncheck');
    }else {
        $("#isStationN").iCheck('check');
        $("#isStationY").iCheck('uncheck');
    }
    $("#standarPlatformFlag").val(rowData.standarPlatformFlag);
    $("#saveBtn").attr("data-url", "/mediaPlate/update");
    $("#addMediaPlateModal").modal("toggle");
}

//删除版本
function del(mediaPlateId) {
    layer.confirm('您好，确定要删除当前媒体板块！', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function () {
        layer.closeAll();
        requestData({id:mediaPlateId, state: 1}, "/mediaPlate/updateState", "post", "json", true, function (data) {
            if(data.code==200){
                layer.msg("媒体板块删除成功！", {time: 2000, icon: 6});
                reflushTable();
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        });
    }, function () {
    });
}

//删除版本
function recover(mediaPlateId) {
    layer.confirm('您好，确定要找回当前媒体板块！', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function () {
        layer.closeAll();
        requestData({id:mediaPlateId, state: 0}, "/mediaPlate/updateState", "post", "json", true, function (data) {
            if(data.code==200){
                layer.msg("媒体板块找回成功！", {time: 2000, icon: 6});
                reflushTable();
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        });
    }, function () {
    });
}
