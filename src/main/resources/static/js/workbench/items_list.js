var itemsObject = {
    url: baseUrl+'/items/list',
    postData: {transactionState: getQueryString("transactionState")},
    datatype: "json",
    mtype: 'get',
    // data: mydata,
    height: "auto",
    page: 1,//第一页
    autowidth: true,
    rownumbers: false,
    gridview: true,
    viewrecords: true,
    multiselect: true,
    shrinkToFit: true,
    prmNames: {rows: "size"},
    rowNum: 10,
    rowList: [10, 20, 30],
    colNames: ['id','工作名称', '工作类型' ,'发起人', '开始时间', '接收时间', '处理期限'],
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: false
    },
    colModel: [
        {
            name: 'id',
            index: 'id',
            editable: false,
            width: 30,
            align: "center",
            sortable: false,
            sorttype: "int",
            search: false,
            hidden: true
        },
        {
            name: 'itemName',
            index: 'itemName',
            editable: false,
            width: 90,
            align: "center",
            sortable: false,
            sorttype: "string",
            formatter: function (a, b, rowdata) {
                var a = "<a ${f}>${text}</a>";
                var f = "";
                if (rowdata.transactionState == 1) {
                    f = rowdata.transactionAddress ? "href=\"javascript:page('" + rowdata.transactionAddress + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                } else {
                    f = rowdata.finishAddress ? "href=\"javascript:page('" + rowdata.finishAddress + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                }
                var text = rowdata.itemName || "";
                a = a.replace("${f}", f).replace("${text}", text);
                return a;
            }
        },
        {
            name: 'workType',
            index: 'workType',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            /*formatter: function (a, b, rowdata) {
                var a = "<a ${f}>${text}</a>";
                var f = "";
                if (rowdata.transactionState == 1) {
                    f = rowdata.transactionAddress ? "href=\"javascript:page('" + rowdata.transactionAddress + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                } else {
                    f = rowdata.finishAddress ? "href=\"javascript:page('" + rowdata.finishAddress + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                }
                var text = rowdata.workType || "";
                a = a.replace("${f}", f).replace("${text}", text);
                return a;
            }*/
        },
        {
            name: 'initiatorWorkerName',
            index: 'initiatorWorkerName',
            editable: false,
            width: 100,
            align: "center",
            sortable: false
        },
        {
            name: 'startTime',
            index: 'startTime',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            formatter:function (d) {
                if(d){
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
                return "";
            }
        },
        {
            name: 'createTime',
            index: 'createTime',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            formatter:function (d) {
                if(d){
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
                return "";
            }
        },
        {
            name: 'endTime',
            index: 'endTime',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            formatter:function (d) {
                if(d){
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
                return "";
            }
        }
    ],
    /**
     * 翻页时保存当前页面的选中数据
     * @param pageBtn
     */
    onPaging:function(pageBtn){
        //跨页面选择
        itemsGrid.setPageSelected("id");
    },
    gridComplete: function () {
        //跨页面选择
        itemsGrid.getPageSelectedSet("id");
    },
    pager: "#itemsPager",
    viewrecords: true,
    caption: null,
    add: false,
    edit: false,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false,
};
$(document).ready(function () {
    //设置隐藏表单的值
    var transactionState = getQueryString("transactionState");
    $("#transactionState").val(transactionState);

    if(transactionState == 1){
        $("#todoDiv").show();
    }else{
        $("#todoDiv").hide();
    }
    $('body').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            //回车执行查询
            $("#searchButton").click();
        }
    });
    $("#batchBtn").click(function () {
        var idss = [] ;
        var ids = $("#itemsTable").jqGrid("getGridParam", "selarrrow");
        if(ids.length == 0){
            swal({
                title: "请先勾选待办事项！",
            });
            return ;
        }
        var size = ids.length ;
        var tips = "你以选中"+size+"条待办，确认把这些待办变成已办？";
        $(ids).each(function (index, id){
            var rowData = $("#itemsTable").jqGrid("getRowData", id);   //获取选中行信息
            idss.push(rowData.id);
        });

        layer.confirm(tips, {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#batchBtn");//锁定按钮，防止重复提交
            $.ajax({
                type: "post",
                url: "/items/batchFinishItems",
                data: {ids:idss.toString()},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    if (data.code == 200) {
                        layer.msg(data.data.message);
                        $("#itemsTable").reloadCurrentData(baseUrl + "/items/list", $("#items").serializeJson(), "json", null, null);
                    } else {
                        if (getResCode(data))
                            return;
                    }
                },
                error:function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        }, function () {
            return;
        });

    })
});