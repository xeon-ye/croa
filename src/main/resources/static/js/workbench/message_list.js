var messageTips = "";
var itemsObject = {
    url: baseUrl + "/message/list",
    postData: {parentTypeQC:getQueryString("parentType")},
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
    rowNum: 50,
    rowList: [50, 200, 500],
    colNames: ['id', '消息内容', '状态', '发起时间', '发起人'],
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
            name: 'content',
            index: 'content',
            editable: false,
            width: 160,
            align: "center",
            sortable: false,
            sorttype: "string"
        },
        {
            name: 'state',
            index: 'state',
            editable: false,
            width: 40,
            align: "center",
            sortable: false,
            formatter: function (d) {
                if (d == 1) {
                    return "<b style='color: red;'>未读</b>";
                } else {
                    return "<b style='color: green;'>已读</b>";
                }
            }
        },
        {
            name: 'createTime',
            index: 'createTime',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            formatter: function (d) {
                if (d) {
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
                return "";
            }
        },
        {
            name: 'initiatorWorkerName',
            index: 'initiatorWorkerName',
            editable: false,
            width: 60,
            align: "center",
            sortable: false
        }
    ],
    /**
     * 翻页时保存当前页面的选中数据
     * @param pageBtn
     */
    onPaging: function (pageBtn) {
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
    hidegrid: false
};

function reloadTaskData() {
    $("#itemsTable").emptyGridParam();
    $("#itemsTable").reloadCurrentData(baseUrl + "/message/list", $("#queryForm").serializeJson(), "json", null, function () {

    });
}

//批量已读
function readMessage(obj){
    var itemIds = $("#itemsTable").jqGrid("getGridParam","selarrrow")
    if(itemIds.length>0){
        var ids = new Array();
        $(itemIds).each(function (index,id) {
            var rowData = $("#itemsTable").jqGrid("getRowData",id);
            ids.push(rowData.id);
        })
        layer.confirm("是否确认已读？",{
            btn:["确定","取消"],
            shade:false
            },function (index) {
                layer.close(index);
                startModal("#"+$(obj).attr("id"));
                $.post("/message/updateMessage",{ids:ids},function (data) {
                    Ladda.stopAll();
                    if (data.data.message == null) {
                        getResCode(data);
                    } else {
                        layer.msg(data.data.message, {time: 2000, icon: 6});
                    }
                    // 刷新数据；
                    $("#itemsTable").jqGrid('setGridParam', {
                        postData: $("#queryForm").serializeJson()
                    }).trigger("reloadGrid");
                },"json");

        })
    }else{
        layer.msg("请选择要操作的数据。")
    }
};

//一键已读
function readAllMessage(obj) {
    if(checkIsEditMessage()){
        layer.confirm(messageTips, {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#" + $(obj).attr("id"));
            var formData = $("#queryForm").serializeForm();
            $.post("/message/agreeAllMessage",formData, function (data) {
                Ladda.stopAll();
                layer.msg(data.data.message, {time: 2000, icon: 6});
                // 刷新数据；
                $("#itemsTable").jqGrid('setGridParam', {
                    postData: $("#queryForm").serializeJson()
                }).trigger("reloadGrid");
            }, "json");
        })
    }
};

//判断是否修改消息
function checkIsEditMessage() {
    messageTips = "";
    var formData = $("#queryForm").serializeForm();
    var flag = false;
    $.ajax({
        type: "get",
        url: baseUrl + "/message/queryMessageIds",    //向后端请求数据的url
        data: formData,
        dataType: "json",
        async:false,
        success: function (data) {
            if (data.data.message == null) {
                layer.msg("没有未读消息需要进行已读处理", {time: 2000, icon: 6});
                flag =  false;
            } else {
                messageTips = data.data.message;
                flag = true;
            }
        },
        error:function (data) {
            Ladda.stopAll();
        }
    });
    return flag;
}

function typeChange() {
    $("#tabList > li").each(function (i,item) {
        $(item).removeClass("active");
        $("#tabList").find("span").css("color","");
    });
    $("#tab0").addClass("active");
    $("#itemsTable").emptyGridParam();
    $("#itemsTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson()
    }).trigger("reloadGrid");

}

$(function () {
    if (getQueryString("parentType") != null && getQueryString("parentType") != "" && getQueryString("parentType") != undefined) {
        var parentType = getQueryString("parentType");
        $("#parentTypeQC").val(parentType);
    }

    $("#tab0").click(function () {
        $("#queryForm select[name='parentTypeQC']").val("");
        $("#queryForm select[name='parentTypeQC']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tab1").click(function () {
        $("#queryForm select[name='parentTypeQC']").val("1");
        $("#queryForm select[name='parentTypeQC']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tab2").click(function () {
        $("#queryForm select[name='parentTypeQC']").val("2");
        $("#queryForm select[name='parentTypeQC']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tab3").click(function () {
        $("#queryForm select[name='parentTypeQC']").val("3");
        $("#queryForm select[name='parentTypeQC']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tab4").click(function () {
        $("#queryForm select[name='parentTypeQC']").val("0");
        $("#queryForm select[name='parentTypeQC']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
})

