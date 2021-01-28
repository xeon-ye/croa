//初始化数据
$(function () {
    //计划添加按钮
    $("#addBtn").click(function () {
        $("#editForm").find("input").removeClass("error");
        $("#editForm").find("textarea").removeClass("error");
        $("#editForm").validate().resetForm();
        $("input[name='type']").removeAttr("checked");
        $("input[name='type']").parent().removeClass("checked");
        document.getElementById("editForm").reset();
        // $("input").val('');
        $("#editModal").modal('toggle');
        $(".save").show();
        $(".update").hide();
        $("#proId").empty();
    });


    // 初始化数据；
    $("#entryTable").jqGrid({
        url: baseUrl + "/proportion/selectProportion",
        datatype: "json",
        mtype: 'POST',
        postData: $("#queyrForm").serializeJson(),
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,
        rownumbers: false,
        autowidth: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 50,
        sortable: "true",
        sortname: "entry_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "entry_id"
        },
        colModel: [
            {name: 'proId', label: '考核计划id', hidden: true, width: 120},
            {name: 'proCode', label: '计划编码', width: 120},
            {name: 'proName', label: '计划名称', width: 120},
            {
                name: 'proBegin', label: '开始时间', width: 120, formatter: function (value, grid, rowData) {
                return new Date(value).format("yyyy-MM-dd hh:mm:ss");
            }
            },
            {
                name: 'proEnd', label: '结束时间', width: 120, formatter: function (value, grid, rowData) {
                return new Date(value).format("yyyy-MM-dd hh:mm:ss");
            }
            },
            {name: 'createName', label: '创建人', width: 120},
            {
                name: 'operate', label: "操作", width: 250, sortable: false, index: '',
                formatter: function (value, grid, rowData) {
                    var html = "";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='edit(" + rowData.proId + "," + rowData.administrativeType + ")'>编辑&nbsp;&nbsp;</a>";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red;'  onclick='del(" + rowData.proId + ")'>删除&nbsp;&nbsp;</a>";
                    return html;
                },
            }
        ],
        pager: jQuery("#entryTableNav"),
        viewrecords: true,
        caption: "计划列表",
        add: false,
        edit: false,
        hidegrid: false,
        loadComplete: function (a, b, c) {
            $("#entryTable").find("tr").each(function () {
                $(this).children().first().css("width", "50");
            });
        },
        //双加查看
        ondblClickRow: function (rowid) {
            var rowData = jQuery("#entryTable").jqGrid("getRowData", rowid);
            view(rowData.proId);
        },
    });
    $("#entryTable").jqGrid('setLabel', 'rn', '序号', {
        'text-align': 'center',
        'vertical-align': 'middle',
        "width": "50"
    });
});

//查看考核计划
function view(proId) {
    $("#viewModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/proportion/getProportionById",
        data: {
            proId: proId
        },
        dataType: "json",
        success: function (data) {
            if (data == null) {
                alert("没有查到相关数据");
            }
            $("#proId").val(data.data.entity.proId);
            $("#proName").val(data.data.entity.proName);
            $("#proBegin").val(data.data.entity.proBegin);
            $("#proEnd").val(data.data.entity.proEnd);
            $("#proportionUnification").val(data.data.entity.beginTime);
            $("#timeWorkEndTime").val(data.data.entity.endTime);
            $("#timeWorkReason").val(data.data.entity.reason);
            $("#timeWorkTaskId1").val(data.data.entity.taskId);
        }
    });
};

//编辑考核计划信息
function edit(id) {
    var URL= baseUrl + "/proportion/getProportionById";
    $("input[name='type']").removeAttr("checked");
    $("input[name='type']").parent().removeClass("checked");
    $.ajax({
        type: "post",
        url: URL,
        data: {proId: id},
        dataType: "json",
        success: function (data) {
            console.log(data)
            for (var attr in data.data.entity) {
                $("[name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                if (attr == "type") {
                    $("input[name='type'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $("input[name='type'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
            }
        }
    });
    $(".save").hide();
    $(".update").show();
}

//删除考核计划信息
function del(id) {
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        var URL = baseUrl + "/proportion/deleteProportion";
        $.ajax({
            type: "post",
            url: URL,    //向后端请求数据的url
            data: {proId: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $("#entryTable").reloadCurrentData(baseUrl + "/proportion/selectProportion", $("#queryForm").serializeJson(), "json", null, null);
                } else {
                    if (getResCode(data))
                        return;
                }
            }
        });
    }, function () {
        return;
    });
}

//保存考核计划
function submitHander(t, url, state) {
    if ($("#editForm").valid()) {
        var tips;
        if (state == 0) {
            $("#state").val(state);
            tips = "确认保存？";
        }
        layer.confirm(tips, {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            var formData;
            formData = new FormData($("#editForm")[0]);

            $.ajax({
                type: "post",
                url: url,
                data: formData,
                dataType: "json",
                async: true,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#entryTable").jqGrid('setGridParam', {
                            postData: $("#queryForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                    if (getResCode(data))
                        return;
                }
            });
        }, function () {
            return;
        });
    }
}

//获取考核方案
function getPlate(proType) {
    $("#viewModal").modal({backdrop: "static"});
    //初始化表格
    $("#entryTable").jqGrid({
        //方案表格
        url: baseUrl + "/proportion/selectProportion",
        datatype: "json",
        mtype: 'POST',
        postData: $("#queyrForm").serializeJson(),
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,
        rownumbers: false,
        autowidth: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 50,
        sortable: "true",
        sortname: "entry_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "entry_id"
        },
        colModel: [
            {name: 'proId', label: '考核方案id', hidden: true, width: 120},
            {name: 'proCode', label: '方案编码', width: 120},
            {name: 'proName', label: '方案名称', width: 120},
            {
                name: 'proBegin', label: '开始时间', width: 120, formatter: function (value, grid, rowData) {
                return new Date(value).format("yyyy-MM-dd hh:mm:ss");
            }
            },
            {
                name: 'proEnd', label: '结束时间', width: 120, formatter: function (value, grid, rowData) {
                return new Date(value).format("yyyy-MM-dd hh:mm:ss");
            }
            },
            {name: 'createName', label: '创建人', width: 120},
        ],
        pager: jQuery("#entryTableNav"),
        viewrecords: true,
        caption: "方案列表",
        add: false,
        edit: false,
        hidegrid: false,
        loadComplete: function (a, b, c) {
            $("#entryTable").find("tr").each(function () {
                $(this).children().first().css("width", "50");
            });
        },
        //双加查看
        ondblClickRow: function (rowid) {
            var rowData = jQuery("#entryTable").jqGrid("getRowData", rowid);
            view(rowData.proId);
        },
    });
}

















