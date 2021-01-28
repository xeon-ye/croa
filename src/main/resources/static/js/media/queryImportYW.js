var arrayNewList = new Array();

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


    // var e = "<i class='fa fa-times-circle'></i> ";
    // $("#orderForm").validate({
    //     rules: {
    //         promiseDay: {maxlength: 3}
    //     },
    //     messages: {
    //         promiseDay: {maxlength: e + "客户答应到款时间最大999天"},
    //     }
    // });
    // $(window).bind('resize', function () {
    //     var width = $('.jqGrid_wrapper').width();
    //     $('#query_table_logs').setGridWidth(width);
    //     $('#select_cust_table_logs').setGridWidth($('#custModal .jqGrid_wrapper').width());
    // });
    function resize(table) {
        if (table == undefined) return;
        var width = $(table).parents(".jqGrid_wrapper").width();
        if (width == 0) return;

        $(table).setGridWidth(width);
    }

    $("#query_table_logs").jqGrid({
        url: '/articleImport/listPgYW',
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
        sortable: "true", //排序方式：倒序，本例中设置默认按id倒序排序
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
            {name: 'media_type_name', label: '板块类型', editable: false, width: 60},
            {name: 'supplier_name', label: '供应商名称', editable: false, hidden: true, width: 120},
            {name: 'media_name', label: '媒体名称', editable: false, width: 80},
            {name: 'user_name', label: '业务员', editable: false, width: 60},
            {name: 'media_user_name', label: '媒介', editable: false, width: 60},
            {
                name: 'issued_date', label: '发布日期', editable: false, width: 100,
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'title', label: '标题', editable: false, width: 120},
            {
                name: 'link', label: '链接', editable: false, width: 120,
                formatter: function (v, options, row) {
                    return "<a href='" + row.link + "' target='_blank'>" + row.link + "</a>";
                }
            },
            {name: 'num', label: '数量', editable: false, width: 60},
            {name: 'price_type', label: '价格类型', editable: false, width: 80},
            {name: 'brand', label: '品牌(双击编辑)', editable: true, width: 80},
            {name: 'sale_amount', label: '<span class="text-red">*</span>报价(双击编辑)', editable: true, width: 120},
            {name: 'pay_amount', label: '成本', editable: false, width: 80},
            {
                name: 'create_time', label: '导入日期', editable: false, width: 100,
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'createName', label: '导入人', editable: false, width: 60},
            {name: 'remark', label: '备注', editable: false, width: 100},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 80},
            {
                name: 'operate', label: "操作", index: '', width: 60,
                formatter: function (value, grid, rows) {
                    var html = "";
                    // if(rows.state==0||rows.state==-1){
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='view(" + rows.id + ")'>查看详情&nbsp;&nbsp;</a>";
                    // }
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
        loadBeforeSend: function (xhr) {
            xhr.setRequestHeader("token", getToken());
        },
        ondblClickRow: function (rowid, iRow, iCol, e) {
            var rowData = jQuery("#query_invoice_table_logs").jqGrid("getRowData", rowid);
            $('#query_table_logs').jqGrid('editRow', rowid, {
                keys: true,		//这里按[enter]保存
                url: "/articleImport/updateAmountAndBrand",
                mtype: "post",
                restoreAfterError: true,
                extraparam: {
                    "id": rowData.id,
                    "brand": rowData.brand,
                    "saleAmount": rowData.sale_amount
                },
            });

        },
        gridComplete: function () {
            var rowData = $(this).jqGrid('getRowData');
            //遍历所有的行，如果是选中的，说明它在数组中，让他选中
            for (var i = 0, n = rowData.length; i < n; i++) {
                var item = rowData[i];
                //判断是否存在数据
                if (arrayNewList.length > 0) {
                    if (arrayNewList.indexOf(item.id) > -1) {
                        //判断arrayNewList中存在item.id，选中前面的复选框，
                        $("#jqg_query_table_logs_" + item.id).attr("checked", true);
                    }
                }
            }
        },
        loadComplete: function (arrayNewList) {
            var array = arrayNewList.list;
            if (arrayNewList.length > 0) {
                $.each(array, function (i, item) {
                    if (arrayNewList.indexOf(item.id.toString()) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_query_table_logs_" + item.id).attr("checked", true);
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
                if (!(arrayNewList.indexOf(rowid) > -1)) {
                    saveData(rowid);
                }
            } else {
                deleteIndexData(rowid);
            }
        }
    });
    resize("#query_table_logs");

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

    $("#querySearch").click(function () {
        arrayNewList.length = 0;
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });


    $("#completeBtn").click(function () {
        document.getElementById("orderForm").reset();
        if (arrayNewList == undefined || arrayNewList.length == 0) {
            swal("请先选择稿件");
        } else {
            $("#ids").val(arrayNewList.join(","));
            $("#orderModal").modal({backdrop: "static"});
            $("#selected_article_table_logs").jqGrid('setGridParam', {
                postData: {ids: $("#ids").val()}, //发送数据
            }).trigger("reloadGrid"); //重新载入
            $("#selected_article_table_logs").jqGrid({
                url: '/articleImport/queryArticleByIds',
                datatype: "json",
                mtype: 'POST',
                postData: {ids: $("#ids").val()}, //发送数据
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
                multiselect: false,
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
                    {name: 'media_type_name', label: '板块类型', editable: false, width: 60},
                    {name: 'supplier_name', label: '供应商名称', editable: false, hidden: true, width: 120},
                    {name: 'media_name', label: '媒体名称', editable: false, width: 80},
                    {name: 'user_name', label: '业务员', editable: false, width: 60},
                    {name: 'media_user_name', label: '媒介', editable: false, width: 60},
                    {
                        name: 'issued_date', label: '发布日期', editable: false, width: 100,
                        formatter: function (d) {
                            return new Date(d).format("yyyy-MM-dd");
                        }
                    },
                    {name: 'title', label: '标题', editable: false, width: 120},
                    {name: 'link', label: '链接', editable: false, width: 120},
                    {name: 'num', label: '数量', editable: false, width: 60},
                    {name: 'price_type', label: '价格类型', editable: false, width: 80},
                    {name: 'brand', label: '品牌', editable: false, width: 80},
                    {name: 'sale_amount', label: '报价', editable: false, width: 100},
                    {name: 'pay_amount', label: '成本', editable: false, width: 80},
                    {
                        name: 'create_time', label: '导入日期', editable: false, width: 100,
                        formatter: function (d) {
                            return new Date(d).format("yyyy-MM-dd");
                        }
                    },
                    {name: 'createName', label: '导入人', editable: false, width: 60},
                    {name: 'remark', label: '备注', editable: false, width: 100},
                    {name: 'id', label: 'id', editable: true, hidden: true, width: 80},
                ],
                pager: jQuery("#selected_article_pager_logs"),
                viewrecords: true,
                caption: "",
                add: false,
                edit: true,
                addtext: 'Add',
                edittext: 'Edit',
                hidegrid: false,
                gridComplete: function () {
                    setArticleResult($("#ids").val());
                },
            });
        }
    });

    //设置统计数据
    function setArticleResult(ids) {
        $.ajax({
            url: baseUrl + "/articleImport/querySumArticleByIds",
            data: {ids: ids},
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

    $("#exportArticle").click(function () {
        if (!arrayNewList || arrayNewList.length <= 0) {
            swal("请选择要导出的数据");
            return;
        }
        layer.confirm('导出临时稿件列表，只允许修改【品牌】和【报价】，修改后通过【导入报价】按钮更新数据！', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $("#exportForm [name='datas']").val(arrayNewList.join(","));
            $("#exportForm").submit();
            // var params = $("#queryForm").serializeJson() ;
            // location.href = "/articleImport/exportArticleYW"+"?"+$.param(params);
        }, function () {
            return;
        });

    });
    $("#exportAll").click(function () {
        layer.confirm('导出临时稿件列表，只允许修改【品牌】和【报价】，修改后通过【导入报价】按钮更新数据！', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            var params =  removeBlank($("#queryForm").serializeJson());
            location.href = "/articleImport/exportArticleYW" + "?" + $.param(params);
        }, function () {
            return;
        });

    });
    $("#importArticle").click(function () {
        $("#importModal").modal('toggle');
    });


    $("#select_cust_table_logs").jqGrid({
        url: '/cust/getCustDockingPeople',
        datatype: "json",
        mtype: 'POST',
        postData: $("#firstForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        multiboxonly: true,
        beforeSelectRow: beforeSelectRow,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 20, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'id', label: 'id', editable: true, hidden: true, width: 240},
            {name: 'taxType', label: 'taxType', editable: true, hidden: true, width: 120},
            {name: 'promiseDay', label: 'promiseDay', editable: true, hidden: true, width: 120},
            {name: 'invoiceType', label: 'invoiceType', editable: true, hidden: true, width: 120},
            {name: 'invoiceTitle', label: 'invoiceTitle', editable: true, hidden: true, width: 120},
            {name: 'taxCode', label: 'taxCode', editable: true, hidden: true, width: 120},
            {name: 'bankNo', label: 'bankNo', editable: true, hidden: true, width: 120},
            {name: 'bankName', label: 'bankName', editable: true, hidden: true, width: 120},
            {name: 'address', label: 'address', editable: true, hidden: true, width: 120},
            {name: 'phone', label: 'phone', editable: true, hidden: true, width: 120},
            {name: 'custCompanyId', label: 'custCompanyId', editable: true, hidden: true, width: 240},
            {name: 'custCompanyName', label: '客户公司名称', editable: true, width: 240},
            {name: 'custId', label: 'custId', editable: true, hidden: true, width: 240},
            {name: 'custName', label: '客户对接人姓名', editable: true, width: 120},
            {name: 'createWorkerName', label: '创建人', editable: true, width: 120},
            {name: 'workerName', label: '负责人', editable: true, width: 120},
            {
                name: 'createTime',
                label: '创建时间',
                editable: false,
                width: 180,
                align: "center",
                sortable: false,
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd hh:mm");
                }
            },
            {
                name: 'updateTime',
                label: '更新时间',
                editable: false,
                width: 180,
                align: "center",
                sortable: false,
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd hh:mm");
                }
            },
            {
                name: 'statusName',
                label: '状态',
                editable: false,
                width: 120,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var d = rowdata.status;
                    if (d == 0) {
                        return "<span style='color:red'>有效</span>"
                    } else if (d == 1) {
                        return "待开发";
                    } else if (d == 2) {
                        return "<span style='color:#3f51b5'>流失</span>"
                    }
                }
            },
        ],
        pager: jQuery("#select_cust_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });
    resize("#select_cust_pager_logs");

    //实现单选
    function beforeSelectRow() {
        $("#select_cust_table_logs").jqGrid('resetSelection');
        return (true);
    }

    $("#custSearch").click(function () {
        $("#select_cust_table_logs").emptyGridParam();
        $("#select_cust_table_logs").jqGrid('setGridParam', {
            postData: $("#custForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $("#selectSupplier").click(function () {
        var rowid = $("#select_cust_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = jQuery("#select_cust_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        if (rowData.id == null || rowData.id == undefined || rowData.id == "") {
            swal("请先选择一个客户!");
        } else {
            $("#companyId").val(rowData.custCompanyId);
            $("#companyName").val(rowData.custCompanyName);
            $("#custId").val(rowData.custId);
            $("#custName").val(rowData.custName);
            $("#taxType").val(rowData.taxType);
            $("#promiseDay").val(rowData.promiseDay);
            changeTax();
            // $("#select_article_table_logs").jqGrid('setGridParam', {
            //     postData: $("#secondForm").serializeJson(), //发送数据
            // }).trigger("reloadGrid"); //重新载入
            $("#custModal").modal("hide");
            $("#orderModal").modal({backdrop: "static"});
        }
    });

    $("#cancel2").click(function () {
        $("#orderModal").modal({backdrop: "static"});
        $("#custModal").modal("hide");
    })

    $("#selCust").click(function () {
        $("#orderModal").modal("hide");
        $("#custModal").modal({backdrop: "static"});
    })

    $("#cleanCust").click(function () {
        $("#companyId").val("");
        $("#companyName").val("");
        $("#custId").val("");
        $("#custName").val("");
        $("[name='taxType']").val("");
        $("#promiseDay").val("");
        $("#tax").val("");
    })

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

function checkTax(amount) {
    var reg = /^[0]\.\d{1,3}$/;
    if (amount.match(reg)) {
        return true;
    } else {
        return false;
    }
}

function checkTax(amount) {
    var reg = /^[0]\.\d{1,3}$/;
    if (amount.match(reg)) {
        return true;
    } else {
        return false;
    }
}

function checkAvgPrice(amount) {
    var reg = /^\d+$/;
    if (amount.match(reg)) {
        return true;
    } else {
        return false;
    }
}

function complete(t) {
    if ($("#orderForm").valid()) {
        var amount = $("#tax").val();
        if (!(amount == "" || checkTax(amount))) {
            swal("税点只允许输入小数，且小数位数最多为3位！");
            return;
        }
        /*var avgPrice = $("#avgPrice").val() ;
        if(!(avgPrice=="" || checkAvgPrice(avgPrice))){
            swal("均价只允许输入整数！") ;
            return ;
        }*/
        layer.confirm('提交请求？确认后不能退回！', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            var param = $("#orderForm").serialize();
            startModal("#" + t.id);//锁定按钮，防止重复提交
            $.ajax({
                type: "post",
                data: param,
                url: "/articleImport/complete",
                dataType: "json",
                success: function (data) {
                    // console.log(data) ;
                    Ladda.stopAll();   //解锁按钮锁定
                    arrayNewList.length = 0;
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 2000, icon: 6});
                        $("#select_cust_table_logs").jqGrid('setGridParam', {
                            postData: $("#custForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#query_table_logs").jqGrid('setGridParam', {
                            postData: $("#queryForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#orderModal").modal('hide');
                    } else {
                        if (getResCode(data))
                            return;
                        $("#orderModal").modal('hide');
                    }
                },
                error: function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        }, function () {
            return;
        });
    }
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


