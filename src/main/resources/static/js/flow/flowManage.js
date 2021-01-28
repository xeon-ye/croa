var fileUpload;
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#flowTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });

    //流程定义文件上传
    fileUpload = new FileUpload({
        targetEl: '#word2htmlForm',
        multi: false,
        filePart: "process",
        requestUrl: "/process/fileUpload?filePart=",
        completeCallback: function (data) {
            if (data.length > 0){
                var fileNames = [data[0].file];
                var param = {fileNames: fileNames, uploadFlag: true};
                requestData(param, "/process/deploy", "post", "json", true, function (result) {
                    if (result.code === 200) {
                        var message = "流程部署成功";
                        var messageType = "success";
                        var isHtml = false;
                        swal({
                            title: "提示",
                            text: message,
                            type: messageType,
                            html: isHtml,
                        });
                        reflushTable(); //刷新表格
                    } else {
                        swal({
                            title: result.msg,
                            type: "error"
                        });
                    }
                });
            }
        },
        acceptSuffix: ['xml']
    });

    createTable(); //表格定义
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
var requestData = function (data, url, requestType,dataType,async,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun,
        error:function () {
            Ladda.stopAll();//隐藏加载按钮
        }
    });
}

//表格定义
function createTable() {
    var $flowTable = $("#flowTable");
    $flowTable.jqGrid({
        url: baseUrl + '/process/listProcessDefinition',
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
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10, //每页记录数
        rowList: [10,20,50],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: '部署ID',
                width: 10,
                hidden:true,
                editable: true,
                sortable: false
            },
            {
                name: 'processName',
                label: '流程名称',
                width: 80,
                hidden:false,
                editable: true,
                sortable: false
            },
            {
                name: 'processKey',
                label: '流程定义KEY',
                width: 80,
                hidden:false,
                editable: true,
                sortable: false
            },
            {
                name: 'processFileName',
                label: '流程文件名称',
                width: 120,
                hidden:false,
                editable: true,
                sortable: false
            },
            {
                name: 'deployVersion',
                label: '流程部署版本',
                width: 50,
                hidden:false,
                editable: true,
                sortable: false
            },
            {
                name: 'processDesc',
                label: '流程描述',
                width: 120,
                hidden:false,
                editable: true,
                sortable: false
            },
            {
                name: 'deployTime',
                label: '流程部署时间',
                width: 80,
                hidden:false,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    if(v){
                        return new Date(v).format("yyyy-MM-dd hh:mm:ss")
                    }else{
                        return v;
                    }
                }
            },
        ],
        pager: "#flowTablePaper",
        viewrecords: true,
        caption: "流程部署列表",
        hidegrid: false,
        gridComplete: function () {
            var width = $('#flowTable').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#flowTable').setGridWidth(width);
        }
    });
    $flowTable.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
}

//刷新表格
function reflushTable() {
    //刷新表格
    $("#flowTable").emptyGridParam(); //清空历史查询数据
    $("#flowTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入

}

//重新部署
function reloadDeploy() {
    var ids = $("#flowTable").jqGrid("getGridParam", "selarrrow");
    if(ids && ids.length > 0){
        var processFileNameArr = new Array();  //缓存当前有效文件名
        var info = new Array();  //缓存当前流程文件名不存在的
        $(ids).each(function (index, id) {
            //由id获得对应数据行
            var row = $("#flowTable").jqGrid('getRowData', id);
            if(row.processFileName){
                processFileNameArr.push(row.processFileName);
            }else{
                info.push("<span style=\"color: brown;font-size: 12px;\">很抱歉，存在["+row.processName+"]不能部署，只有流程文件名称存在才能部署！</span>")
            }
        });
        if(info.length > 0){
            var text = info.join("<br/>");
            swal({
                title: "重新部署失败!",
                text: text,
                type: "warning",
                html: true
            });
            return;
        }
        var json = {};
        json.fileNames = processFileNameArr;
        json.uploadFlag = false;
        startModal("#reloadDeploy");
        requestData(json, "/process/deploy", "post", "json", true, function (result) {
            Ladda.stopAll();
            if (result.code === 200) {
                var message = "流程部署成功";
                var messageType = "success";
                var isHtml = false;
                swal({
                    title: "提示",
                    text: message,
                    type: messageType,
                    html: isHtml,
                });
                reflushTable(); //刷新表格
            } else {
                swal({
                    title: result.msg,
                    type: "error"
                });
            }
        });
    }else{
        swal("请选择要操作的数据!");
    }
}


