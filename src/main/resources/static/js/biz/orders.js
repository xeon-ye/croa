$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_orders').setGridWidth(width);
    });
    init();
    loadArticle();
    $('#orderForm .i-checks ').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
    $('#orderForm .i-checks').on("ifChecked", function () {
        reload($(this).val());
    });
});

function reload(state) {
    var data = $("#orderForm").serializeJson();
    data.state = state;
    $("#table_articles").emptyGridParam();
    $("#table_articles").jqGrid('setGridParam', {
        url: baseUrl+'/order/list',
        postData: data,
    }).trigger("reloadGrid");
}

function init() {
    $("#table_orders").jqGrid({//2600
        url: baseUrl+'/order/list',
        datatype: "json",
        mtype: 'get',
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
        multiselectWidth: 25, //设置多选列宽度
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        // multiselect: true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15,//每页显示记录数
        rowList: [15, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [
            {
                name: 'id',
                label: 'id',
                editable: true,
                hidden: true,
                sorttype: "int",
                search: true
            },
            {
                name: 'no',
                label: '订单编号',
                editable: true,
                width: 50,
                align: "center"
            },
            {
                name: 'user.name',
                label: '创建人',
                editable: true,
                width: 30,
                align: "center"
            },
            {
                name: 'createDate',
                label: '创建时间',
                width: 40,
            },
            {
                name: 'companyName',
                label: '公司名称',
                editable: true,
                width: 50,
                align: "center"
            },
            {
                name: 'userName',
                label: '对接人姓名',
                editable: true,
                width: 30,
                align: "center"
            },
            {
                name: 'amount',
                label: '订单金额',
                editable: true,
                width: 25,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {
                    thousandsSeparator: ",",
                    decimalSeparator: ".",
                    prefix: "￥"
                },
            },
            {
                name: 'title',
                label: '订单标题',
                editable: true,
                width: 60,
                align: "center",
                formatter: function (v, options, row) {
                    return v;
                }
            },
            {
                name: 'saleAmount',//应收
                label: '客户报价',
                editable: true,
                width: 25,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},

            },
            {
                name: 'state',
                label: '订单状态',
                editable: true,
                width: 20,
                align: "center",
                formatter: function (v, options, row) {
                    // 订单状态:0未下单、1已下单
                    return v == 0 ? '<span class="text-danger">未下单</span>' : '<span class="text-success">已下单</span>';
                }
            },
            {
                name: 'updateDate',
                label: '答应到款时间',
                width: 40,
            },
            {
                name: 'updateUser.name',
                label: '最后更新人',
                width: 30,
                align: "center"
            },
            {
                name: 'desc',
                label: '订单描述',
                width: 80,
                align: "center"
            },
            {
                name: 'option',
                label: '操作',
                editable: false,
                width: 36,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    html += "<a href='javascript:Business.edit(" + rowdata.artId + "," + rowdata.commissionStates + "," + rowdata.invoiceStates + ")' style='margin-right:3px;color:#337ab7'>编辑</a>";
                    if (rowdata.issueStates != 4) {
                        html += "<a href='javascript:Business.deleteArt(" + rowdata.artId + ")' style='margin-right:3px;color:#337ab7'>删除</a>";
                    }
                    return html;
                }
            }

        ],
        pager: "#pager_orders",
        viewrecords: true,
        caption: "订单列表",
        hidegrid: false,
        loadComplete: function (data) {
            // alert(JSON.stringify(data));
            if (getResCode(data))
                return;
        }, ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            page('/order/getById/' + rowid, '媒体下单' + rowid);
        }, onSelectRow: function (rowId, row, col, e) {
            $("#table_articles").emptyGridParam();
            $("#table_articles").jqGrid('setGridParam', {
                url:  "/article/list/" + rowId,
            }).trigger("reloadGrid");
            // }, onClickRow: function (rowId, row, col, e) {
            //     $("#table_articles").emptyGridParam();
            //     $("#table_articles").jqGrid('setGridParam', {
            //         url:  "/article/list/" + rowId,
            //     }).trigger("reloadGrid");
        }
    });
    $("#table_orders").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $("#table_orders").setSelection(4, true);
}

function loadArticle() {
    $("#table_articles").jqGrid({//2600
        datatype: "json",
        mtype: 'get',
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
        multiselectWidth: 25, //设置多选列宽度
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        // multiselect: true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15,//每页显示记录数
        rowList: [15, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [
            {
                name: 'id',
                label: 'id',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                sorttype: "int",
                hidden: true,
            },
            {
                name: 'artId',
                label: '稿件ID',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true
                // },
                // {
                //     name: 'mTypeName',
                //     label: '类别',
                //     editable: false,
                //     width: 70,
                //     align: "center",
                //     sortable: false
            },
            {
                name: 'mediaUserName',
                label: '媒介员',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'mediaName',
                label: '媒体名称',
                editable: false,
                width: 70,
                align: "center",
                sortable: false,
                hidden: false
            },
            {
                name: 'title',
                label: '稿件标题',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'brand',
                label: '品牌',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                hidden: false
            },
            {
                name: 'issuedDate',
                label: '发布日期',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: false,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'link',
                label: '链接',
                editable: false,
                width: 70,
                align: "center",
                sortable: false
            },
            {
                name: 'num',
                label: '数量',
                editable: false,
                width: 50,
                align: "center",
                sortable: false
            },
            {
                name: 'saleAmount',
                label: '应收金额',
                editable: false,
                width: 70,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                sortable: false
            },
            {
                name: 'taxes',
                label: '税金',
                editable: false,
                width: 50,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                sortable: false
            },
            {
                name: 'incomeAmount',
                label: '入账金额',
                editable: false,
                width: 70,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                sortable: false
            },
            {
                name: 'incomeDetail',
                label: '入账详情',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var url = "javascript:Business.incomeDetail(" + rowdata.artId + ")";
                    var a = "<a href=" + url + ">入账详情</a>";
                    return a;
                }
            },
            {
                name: 'commissionStates',
                label: '提成状态',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'invoiceStates',
                label: '开票状态',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'issueStates',
                label: '发布状态',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'priceColumn',
                label: '支付单价',
                editable: false,
                width: 70,
                align: "center",
                sortable: false,
                hidden: false,
                classes: 'text-danger',
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "$"},
                formatter: function (a, b, rowdata) {
                    var html = rowdata[rowdata.priceColumn] ? (rowdata[rowdata.priceColumn]).toFixed(2) : "";
                    return html;
                }
            },
            {
                name: 'payAmount',
                label: '支付金额',
                editable: false,
                width: 70,
                align: "center",
                sortable: false,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                hidden: false
            },
            {
                name: 'supplierName',
                label: '发稿渠道商',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: false
            },
            {
                name: 'lrl',
                label: '利润率',
                editable: false,
                width: 50,
                align: "center",
                sortable: false,
                classes: 'text-danger',
                formatter: "currency",
                hidden: false
            },
            {
                name: 'payTime',
                label: '支付日期',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: false,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'outAccountName',
                label: '支付账号',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: false
            },
            {
                name: 'payRemark',
                label: '备注',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: false
            },
            {
                name: 'commission',
                label: '提成',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                classes: 'text-danger',
                formatter: "currency",
                hidden: false
            },
            {
                name: 'rePayAmount',
                label: '退款',
                editable: false,
                width: 70,
                align: "center",
                sortable: false,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                hidden: false
            },
            // {
            //     name: 'option',
            //     label: '操作',
            //     editable: false,
            //     width: 100,
            //     align: "center",
            //     sortable: false,
            //     formatter: function (a, b, rowdata) {
            //         var html = "";
            //         html += "<a href='javascript:Business.edit(" + rowdata.artId + "," + rowdata.commissionStates + "," + rowdata.invoiceStates + ")' style='margin-right:3px;color:#337ab7'>编辑</a>";
            //         if (rowdata.issueStates != 4) {
            //             html += "<a href='javascript:Business.deleteArt(" + rowdata.artId + ")' style='margin-right:3px;color:#337ab7'>删除</a>";
            //         }
            //         return html;
            //     }
            // }
        ],
        pager: "#pager_articles",
        viewrecords: true,
        caption: "订单列表",
        hidegrid: false,
        loadComplete: function (data) {
            // alert(JSON.stringify(data));
            if (getResCode(data))
                return;
        }
    });
}