/**
 * Created by Administrator on 2018/2/3.
 */
$(document).ready(function () {
    //查询操作类型
    $.ajax({
        url: "/log/opTypes",
        type: 'get',
        beforeSend: function (xhr) {
            xhr.setRequestHeader("token", getToken());
        },
        dataType: "json",
        success: function (data) {
            if (getResCode(data))
                return;
            var html = "";
            for (var key in data) {
                html += "<option>" + data[key] + "</option>";
            }
            // data.forEach(function (value, index, args) {
            //     html += "<option>" + value + "</option>";
            // });
            $("#opType").html(html);
        }
    });

    //查询表格数据
    $.jgrid.defaults.styleUI = "Bootstrap";
    $("#logger_list").jqGrid({
        url: '/log/search',
        // url: baseUrl+'/log/all',
        datatype: "json",
        altRows: true,
        altclass: 'bgColor',
        mtype: "get",
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        postData: {},
        viewrecords: true,  //显示总记录数
        multiselect: true,  //可多选，出现多选框
        multiselectWidth: 25, //设置多选列宽度
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 20,//每页显示记录数
        rowList: [20, 25, 30],//分页选项，可以下拉选择每页显示记录数
        // colNames: ["序号", "日期", "用户", "IP地址", "模块", "操作类型", "备注"],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [{
            name: "id",
            label: "序号",
            index: "id",
            editable: true,
            width: 20,
            align: "center",
            sorttype: "int",
            sortable: true,
            search: true,
            hidden: true
        }, {
            name: "opDate",
            index: "opDate",
            label: "日期",
            editable: true,
            width: 50,
            align: "center",
            sorttype: "date",
//                formatter:"date"
            formatoptions: {srcformat: 'Y-m-d H:i:s', newformat: 'Y-m-d H:i:s'}
        }, {
            name: "user.name",
            index: "user.id",
            label: "用户",
            editable: true,
            align: "center",
            width: 25
        }, {
            name: "ip",
            index: "ip",
            label: "IP地址",
            editable: true,
            width: 45,
            align: "center"
        }, {
            name: "mac",
            index: "mac",
            label: "MAC地址",
            editable: true,
            width: 45,
            align: "center"
        }, {
            name: "module",
            label: "模块",
            editable: true,
            width: 100,
            align: "center",
            sorttype: "string"
        }, {
            name: "opType",
            label: "操作类型",
            editable: true,
            width: 20,
            align: "center",
            sorttype: "string",
            formatter: function (v, options, rowObject) {
                switch (v) {
                    case "添加":
                        return "<span class='text-success'>添加</span>";
                        break;
                    case "删除":
                        return "<span class='text-warning'>删除</span>";
                        break;
                    case "修改":
                        return "<span class='text-info'>修改</span>";
                        break;
                    case "查询":
                        return "<span class='text-danger'>查询</span>";
                        break;
                    default:
                        return "<span class='text-default'>其他</span>";
                        break;
                }
            }
        }, {
            name: "note",
            index: "note",
            label: "备注",
            editable: true,
            width: 100,
            align: "center",
            sortable: false
        }, {
            name: "args",
            index: "args",
            label: "参数",
            editable: true,
            width: 100,
            align: "center",
            sortable: false
        }, {
            name: "retVal",
            index: "retVal",
            label: "响应参数",
            editable: true,
            width: 150,
            align: "center",
            sortable: false
        }
        ],
        pager: "#logger_pager",
        rownumbers: true,
        caption: "系统日志列表",
        add: true,
        edit: true,
        addtext: "Add",
        edittext: "Edit",
        hidegrid: false,
        loadBeforeSend: function (xhr) {
            xhr.setRequestHeader("token", getToken());
        },
        loadComplete: function (data) {
            if (getResCode(data))
                return;
            $(window).bind("resize", function () {
                var width = $(".jqGrid_wrapper").width();
                $("#logger_list").setGridWidth(width)
            })
        }
    });
    $("#logger_list").setSelection(4, true);

    //删除
    $("#delData").click(function () {
        var ids = $("#logger_list").jqGrid("getGridParam", "selarrrow");
        if (ids == null || ids.length <= 0) {
            swal("请选择要删除的项");
            return;
        }
        swal({
                title: "确定删除吗？",
                text: "你将无法恢复该日志！",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "确定删除！",
                cancelButtonText: "取消删除！",
                closeOnConfirm: false,
                closeOnCancel: false
            },
            function (isConfirm) {
                if (isConfirm) {
                    var data = '[';
                    ids.forEach(function (v, i, d) {
                        data += '{"id":' + v + '},';
                    });
                    data = data.substring(0, data.length - 1);
                    data += ']';
                    $.ajax({
                        url: "/log/delBatch",
                        data: data,
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader("token", getToken());
                        },
                        type: 'post',
                        dataType: "json",
                        success: function (data) {
                            if (getResCode(data))
                                return;
                            window.refresh = true;
                            jQuery("#logger_list").jqGrid('setGridParam', {
                                page: 0
                            }).trigger('reloadGrid');
                        }
                    });
                    // swal("删除成功！", "该日志已经被删除。", "success");
                    swal({
                        title: "删除成功！",
                        text: "该日志已经被删除,2秒后关闭该窗口",
                        timer: 2000
                    });
                } else {
                    swal({
                        title: "取消成功！",
                        timer: 0
                    });
                }
            });

    });
    $("#search").click(function () {
        //使用serializeJson 必须引入cps.js文件，它可以将分页数据一起封装成json传递到控制层
        var data = $("#sd").serializeJson();
        $("#logger_list").emptyGridParam();
        $("#logger_list").jqGrid('setGridParam', {
            postData: data
        }).trigger('reloadGrid');
    });
});

/*回车进行搜索*/
$(function () {
    $('body').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            // 回车执行查询；
            $("#search").click();


        }
    });
});