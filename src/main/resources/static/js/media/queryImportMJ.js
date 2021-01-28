function del(id) {
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: "/articleImport/del",    //向后端请求数据的url
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $("#query_table_logs").jqGrid('resetSelection');//清除选中
                    $("#query_table_logs").reloadCurrentData(baseUrl + "/articleImport/listPgMJ", $("#queryForm").serializeJson(), "json", null, function () {
                    });
                } else {
                    if (getResCode(data))
                        return;
                }
            }
        });
    }, function () {
        return;
    });
};

//flag=1审核页面，flag=0查看页面
function view(id) {
    $("#viewModal").modal('toggle');
    $.ajax({
        type: "post",
        url: "/articleImport/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.entity) {
                $("#viewForm [name=" + attr + "]").val(data.data.entity[attr]);
            }
        }
    });
};

$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#query_table_logs').setGridWidth(width);
        $('#select_cust_table_logs').setGridWidth(width);
    });

    var arrayNewList = new Array();
    $("#query_table_logs").jqGrid({
        url: '/articleImport/listPgMJ',
        datatype: "json",
        mtype: 'POST',
        postData: $("#queyrForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: true,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'media_type_name', label: '板块类型', editable: true, width: 60},
            {name: 'supplier_name', label: '供应商名称', editable: true, width: 80},
            {name: 'supplier_contactor', label: '供应商联系人', editable: true, width: 60},
            {name: 'media_name', label: '媒体名称', editable: true, width: 100},
            {name: 'user_name', label: '业务员', editable: true, width: 60},
            {name: 'media_user_name', label: '媒介', editable: true, width: 60},
            {
                name: 'issued_date', label: '发布日期', editable: true, width: 80,
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'title', label: '标题', editable: true, width: 120},
            {
                name: 'link', label: '链接', editable: true, width: 160, formatter: function (v, options, row) {
                    var str = row.link.substring(0, 4).toLowerCase();
                    if (str == "http") {
                        return "<a href='" + row.link + "' target='_blank'>" + row.link + "</a>";
                    } else {
                        return "<a href='//" + row.link + "' target='_blank'>" + row.link + "</a>";
                    }
                }
            },
            {name: 'num', label: '数量', editable: true, width: 40},
            {name: 'price_type', label: '价格类型', editable: true, width: 60},
            {name: 'pay_amount', label: '成本', editable: true, width: 60},
            {
                name: 'create_time', label: '导入日期', editable: true, width: 80,
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'createName', label: '导入媒介', editable: true, width: 60},
            {name: 'remark', label: '备注', editable: true, width: 120},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 80},
            {
                name: 'operate', label: "操作", index: '', width: 100,
                formatter: function (value, grid, rows) {
                    var html = "";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='edit(" + rows.id + ")'>编辑&nbsp;&nbsp;</a>";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='del(" + rows.id + ")'>删除&nbsp;&nbsp;</a>";
                    return html;
                }
            }
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            view(rowid);
        },
        gridComplete: function () {
            setArticleResult();
            // tb_init("a.thickbox, area.thickbox, input.thickbox");
            var rowIds = $("#select_article_table_logs").jqGrid('getGridParam', 'selarrrow');
            if (rowIds == undefined || rowIds == null || rowIds == "") {
            } else {
                for (var k = 0; k < rowIds.length; k++) {
                    var curRowData = jQuery("#select_article_table_logs").jqGrid('getRowData', rowIds[k]);
                    var curChk = $("#" + rowIds[k] + "").find(":checkbox");
                    curChk.attr('name', 'checkboxname');   //给每一个checkbox赋名字
                    curChk.attr('value', curRowData['code']);   //给checkbox赋值

                }
            }
        },
        loadComplete: function (arrayNewList) {
            var array = arrayNewList.list;
            // console.log(arrayNewList)
            if (arrayNewList.length > 0) {
                $.each(array, function (i, item) {
                    // console.log("*****"+item.id)
                    if (arrayNewList.indexOf(item.id.toString()) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_article_table_logs_" + item.id).attr("checked", true);
                    }
                });
            }
        },
        onSelectAll: function (aRowids, status) {
            if (status == true) {
                //循环aRowids数组，将code放入arrayNewList数组中
                $.each(aRowids, function (i, item) {
                    if (!(arrayNewList.indexOf(item) > -1)) {
                        saveData(item);
                    }
                })
            } else {
                //循环aRowids数组，将code从arrayNewList中删除
                $.each(aRowids, function (i, item) {
                    deleteIndexData(item);
                })
            }
        },
        onSelectRow: function (rowid, status) {

            if (status == true) {
                saveData(rowid);
            } else {
                deleteIndexData(rowid);
            }
        }
    });

    function saveData(obj) {
        arrayNewList.push(obj);
    }

    function deleteIndexData(obj) {
        //获取obj在arrayNewList数组中的索引值
        for (i = 0; i < arrayNewList.length; i++) {
            $("#row" + obj).remove()
            if (arrayNewList[i] == obj) {
                //根据索引值删除arrayNewList中的数据
                arrayNewList.splice(i, 1);
                // i--;
            }
        }
    }

    //设置统计数据
    function setArticleResult() {
        $.ajax({
            url: baseUrl + "/articleImport/getArticleImportSum",
            data: $("#queryForm").serializeJson(),
            type: "post",
            dataType: "json",
            success: function (resData) {
                if (resData) {
                    for (var o in resData) {
                        $("#tj #" + o).text(resData[o] == "" ? 0 : resData[o]);
                    }
                } else {
                    $("#tj").find(".text-danger").html(0);
                }
            }
        });
    }

    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $("#exportTemplate").click(function () {
        location.href = "/mediauser1/exportTemplate"; //mediaInfo/exportTemplate
    })
    $("#importBtn").click(function () {
        $("#importModal").modal('toggle');
    });

    $("#exportAll").click(function () {
        var params =  removeBlank($("#queryForm").serializeJson());
        location.href = "/articleImport/exportArticleMJ" + "?" + $.param(params);
    });
    $("#exportArticle").click(function () {
        if (!arrayNewList || arrayNewList.length <= 0) {
            swal("请选择要导出的数据");
            return;
        }
        $("#exportForm [name='datas']").val(arrayNewList.join(","));
        $("#exportForm").submit();
    });

    $("#batchDelBtn").click(function () {
        if (arrayNewList == undefined || arrayNewList.length == 0) {
            swal("请先选择稿件！");
        } else {
            layer.confirm('提示：批量删除只支持删除自己导入的稿件。确认删除？', {
                btn: ['删除', '取消'], //按钮
                shade: false //不显示遮罩
            }, function (index) {
                layer.close(index);
                $.ajax({
                    type: "post",
                    url: "/articleImport/batchDel",    //向后端请求数据的url
                    data: {ids: arrayNewList.join(",")},//数组转换成String
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/articleImport/listPgMJ", $("#queryForm").serializeJson(), "json", null, function () {
                            });
                            arrayNewList.length = 0;
                        } else if (data.code == 1111) {
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/articleImport/listPgMJ", $("#queryForm").serializeJson(), "json", null, function () {
                            });
                            arrayNewList.length = 0;
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
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
    });

});


/*回车进行搜索*/
$(function () {
    $('body').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            // 回车执行查询；
            $("#accountSearch2").click();
            $("#querySearch").click();
            $("#accountSearch").click();
        }
    });
});

function edit(id) {
    $("#editModal").modal("toggle");
    document.getElementById("editForm").reset();
    $("#editForm").find("input").removeClass('error');//清除验证标签
    $("#editForm").validate().resetForm();
    $.ajax({
        type: "post",
        url: "/articleImport/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {

            for (var attr in data.data.entity) {
                $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);

            }
            // console.log(data.data.entity["mediaTypeId"]) ;
            setType(data.data.entity["mediaTypeId"], data.data.entity['priceType']);
        }
    });
}

function setType(mediaTypeId, name) {
    $("#priceTypeDiv").empty();
    console.log("mediaTypeId=" + mediaTypeId)
    $.get(baseUrl + "/mediaForm/queryPriceColumnsByTypeId/" + mediaTypeId, function (datas) {
        var html = '';
        html += "<select class='form-control' name='priceType'> ";
        $(datas).each(function (i, data) {
            if (data.name == name) {
                html += "<option value='" + data.name + "' selected='selected'>" + data.name + "</option>";
            } else {
                html += "<option value='" + data.name + "'>" + data.name + "</option>";

            }
        });
        html += "</select>";
        $("#priceTypeDiv").append(html)
    }, "json");
}

function checkFileExt(ext) {
    if (!ext.match(/^.+(.xls|.xlsx)$/)) {
        return false;
    }
    return true;
}

function submitHander(t, url) {
    if (document.getElementById("file").value == "") {
        swal("请选上传excel！");
    } else {
        var filePath = document.getElementById("file").value;
        var fileExt = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
        if (!checkFileExt(fileExt)) {
            var formData = new FormData($("#importForm")[0]);
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
                    // Ladda.stopAll() ;
                    // console.log(data) ;
                    $("#importModal").modal('hide');
                    if (data.code == 200) {
                        $("#query_table_logs").jqGrid('setGridParam', {
                            postData: $("#queryForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        swal({title: data.data.message, type: "success"});
                    } else if (data.code == 1002) {
                        var str = "";
                        if (data.msg.length > 200) {
                            str = data.msg.slice(0, 200) + "...";
                        } else {
                            str = data.msg;
                        }
                        swal({
                                title: "导入失败，成功导入0条！",
                                text: str,
                                type: "error",
                                showCancelButton: true,
                                confirmButtonColor: "#2a45dd",
                                confirmButtonText: "下载失败详情！",
                                cancelButtonText: "直接关闭！",
                                closeOnConfirm: false,
                                reverseButtons: true //控制按钮反转
                            },
                            function (isConfirm) {
                                if (isConfirm) {
                                    var isIE = (navigator.userAgent.indexOf('MSIE') >= 0);
                                    if (isIE) {
                                        var strHTML = data.msg;
                                        var winSave = window.open();
                                        winSave.document.open("text", "utf-8");
                                        winSave.document.write(strHTML);
                                        winSave.document.execCommand("SaveAs", true, "导入失败详情.txt");
                                        winSave.close();
                                    } else {
                                        var elHtml = data.msg;
                                        var mimeType = 'text/plain';
                                        $('#createInvote').attr('href', 'data:' + mimeType + ';charset=utf-8,' + encodeURIComponent(elHtml));
                                        document.getElementById('createInvote').click();
                                    }
                                    swal.close();
                                } else {
                                    swal.close();
                                }
                            });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                },
                error: function (data) {
                    // console.log(data) ;
                    // Ladda.stopAll() ;
                    if (getResCode(data))
                        return;
                }
            });
        }
    }
}

function submitHander2(t, url) {
    if ($("#editForm").valid()) {
        layer.confirm('确认修改？确认后不能退回！', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            var param = $("#editForm").serializeJson();
            startModal("#" + t.id);//锁定按钮，防止重复提交
            $.ajax({
                type: "post",
                data: param,
                url: url,
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();   //解锁按钮锁定
                    if (data.code == 200) {

                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        // $("#query_table_logs").jqGrid('setGridParam', {
                        //     postData: $("#queryForm").serializeJson(), //发送数据
                        // }).trigger("reloadGrid"); //重新载入
                        $("#query_table_logs").reloadCurrentData(baseUrl + "/articleImport/listPgMJ", $("#queryForm").serializeJson(), "json", null, function () {
                        });
                        $("#editModal").modal('hide');
                    } else {
                        if (getResCode(data))
                            return;
                        $("#editModal").modal('hide');
                    }
                },
                error: function () {
                    Ladda.stopAll();
                }
            });
        }, function () {
            return;
        });
    }
}

