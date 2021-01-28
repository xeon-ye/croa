/**
 * Created by Administrator on 2018/2/3.
 */
$(document).ready(function () {
    //查询操作类型
    // $.ajax({
    //     url:  "/resource/search",
    //     type: 'get',
    //     beforeSend: function (xhr) {
    //         xhr.setRequestHeader("token", getToken());
    //     },
    //     dataType: "json",
    //     success: function (data) {
    //         if (getResCode(data))
    //             return;
    //         var html = "";
    //         data.forEach(function (value, index, args) {
    //             html += "<option>" + value + "</option>";
    //         });
    //         $("#opType").html(html);
    //     }
    // });

    //查询表格数据
    $.jgrid.defaults.styleUI = "Bootstrap";
    $("#resource_list").jqGrid({
        url:  "/resource/search",
        postData: $("#resourceForm").serializeJson(),
        datatype: "json",
        altRows: true,
        altclass: 'bgColor',
        mtype: "post",
        height: "auto",
        page: 1,//第一页
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,  //可多选，出现多选框
        multiselectWidth: 25, //设置多选列宽度
        shrinkToFit: true,
        prmNames: {rows: "size"}, rowNum: 15,//每页显示记录数
        rowList: [15, 25, 30],//分页选项，可以下拉选择每页显示记录数
        // colNames: ["序号", "日期", "用户", "IP地址", "模块", "操作类型", "备注"],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [{
            name: "id",
            index: "id",
            width: 20,
            align: "center",
            sorttype: "int",
            sortable: true,
            search: true,
            hidden: true
        }, {
            name: "name",
            label: "资源名称",
            editable: true,
            width: 50,
            align: "center",
            editrules: {required: true}
        }, {
            name: "url",
            label: "资源路径",
            editable: true,
            width: 100,
            align: "center",
            editrules: {required: true}
        }, {
            name: "creator",
            label: "创建人",
            width: 80,
            align: "center",
            sorttype: "string"
        }, {
            name: "createTime",
            label: "创建时间",
            // editable: true,
            width: 80,
            align: "center",
            sorttype: "string",
            // unformat: function (cellvalue, options, cell) {
            //     setTimeout(function () {
            //         $(cell).find('input[type=text]').addClass('form-control layer-date laydate-icon');
            //         $(cell).find('input[type=text]').click(function () {
            //             laydate({istime: true, format: 'YYYY-MM-DD hh:mm:ss'});
            //         });
            //     }, 0);
            // }
        }, {
            name: "state",
            label: "资源状态",
            width: 20,
            align: "center",
            editable: true,
            sorttype: "string",
            edittype: 'checkbox',
            editoptions: {value: "0:1"},
            unformat: function (val, options, cell) {
                setTimeout(function () {
                    $(cell).find('input[type=checkbox]').addClass('form-control layer-date laydate-icon');
                    $(cell).find('input[type=checkbox]').iCheck(val == '有效' ? "check" : 'uncheck');
                }, 0);
            },
            formatter: function (v, options, rowObject) {
                switch (v) {
                    case 0:
                        return "<span class='text-success'>有效</span>";
                        break;
                    case 1:
                        return "<span class='text-warning'>无效</span>";
                        break;
                    case '0':
                        return "<span class='text-success'>有效</span>";
                        break;
                    case '1':
                        return "<span class='text-warning'>无效</span>";
                        break;

                }
            }
        }, {
            name: "操作",
            width: 50,
            align: "center",
            formatter: function (v, options, row) {
                return "<button class='btn btn-default btn-sm' onclick='del("+row.rowId+")'>删除</button>&nbsp;&nbsp;&nbsp;<button class='btn btn-danger btn-sm'>编辑</button>"
            }
        }
        ],
        pager: "#resource_pager",
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
        },
        // onSelectRow: function (id) {
        ondblClickRow: function (id) {
            // if (lastSel != null && lastSel != "" && lastSel != id) {
            //     $("#resource_list").jqGrid('saveRow', lastSel);//保存上一行
            // }
            if (id && id !== lastSel) {
                $("#resource_list").restoreRow(lastSel);
                lastSel = id;
            }
            // $("#resource_list").editRow(id, true);
            // var state = $("#" + id + "_state").is(':checked');
            // alert(state);
            $('#resource_list').jqGrid('editRow', id, {
                keys: true,		//这里按[enter]保存
                url:  "/resource/update",
                mtype: "post",
                restoreAfterError: true,
                extraparam: {
                    "id": id,
                    "url": $("#" + id + "_url").val(),
                    "name": $("#" + id + "_name").val(),
                    "state": $("#" + id + "_state").val()
                },
                // oneditfunc: function (rowid) {
                //     // alert( $("#" + id + "_state").is(':checked'));
                //     console.log(rowid);
                // },
                // succesfunc: function (response) {
                //     alert("save success");
                //     return true;
                // },
                // errorfunc: function (rowid, res) {
                //     console.log(rowid);
                //     console.log(res);
                // }
            });

        },
    });
    var lastSel;
    $("#resource_list").setSelection(4, true);
    $(window).bind("resize", function () {
        var width = $(".jqGrid_wrapper").width();
        $("#resource_list").setGridWidth(width)
    })
    //删除
    $("#delData").click(function () {
        var ids = $("#resource_list").jqGrid("getGridParam", "selarrrow");
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
                        url:  "/log/delBatch",
                        data: data,
                        dataType:"json",
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader("token", getToken());
                        },
                        type: 'post',
                        success: function (data) {
                            if (getResCode(data))
                                return;
                            window.refresh = true;
                            jQuery("#resource_list").jqGrid('setGridParam', {
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
        $("#query_table_logs").emptyGridParam() ;
        var data = $("#resourceForm").serializeJson();
        alert(JSON.stringify(data));
        $("#resource_list").emptyGridParam();
        $("#resource_list").jqGrid('setGridParam', {
            postData: data
        }).trigger('reloadGrid');
    });
});