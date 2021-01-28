$(function () {
    layui.use('form', function(){
        var form = layui.form;
        //时间改变事件
        form.on('select(platform)', function(data){
            queryMediaBrand();
        });
        form.render();
    });

    layui.use('form', function(){
        layui.form.render();//layui重新渲染下拉列表
    });

    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_media_brand').setGridWidth(width);
    });

    $('#queryForm').on('keypress', function (event) {
        if (event.keyCode == "13") {
            $("#mediaBrandSearch").click();
        }
    });

    createTable();

    createViewTable();
});

function resize(table) {
    if (table == undefined) return;
    var width = $(table).parents(".jqGrid_wrapper").width();
    if (width == 0) return;
    $(table).setGridWidth(width);
}

function createTable() {
    var formData = $("#queryForm").serializeJson();
    var $tableMedias = $("#table_media_brand");
    $tableMedias.jqGrid({
        url: baseUrl + '/mediaBrand/listPg',
        datatype: "json",
        postData: formData,
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
        sortable: true,
        sortname: "",
        multiselect: true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15,//每页显示记录数
        rowList: [15, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        prmNames: {
            page: "page",
            rows: "size",
            totalrows: "totalElements",
            sort: "sort",
            order: "order",
        },
        colModel: [
            {
                name: 'id',
                label: 'id',
                editable: true,
                hidden: true,
                sortable: false,
                sorttype: "int",
                search: true
            },
            {
                name: 'brandName',
                label: '品牌名称',
                editable: true,
                sortable: false,
                width: 30,
                align: "center",
                formatter: function (v, options, row) {
                    return "<a href='javascript:void(0)' onclick='jumpHtml("+row.platform+",\""+row.brandName+"\")'>"+v+"</a>";
                }
            },
            {
                name: 'platform',
                label: '投放平台',
                editable: true,
                sortable: false,
                width: 20,
                align: "center",
                formatter: function (v, options, row) {
                    if(v==1){
                        return "抖音";
                    }else if(v==2){
                        return "小红书";
                    }else {
                        return "微博";
                    }
                }
            },
            {
                name: 'times',
                label: '投放次数',
                width: 20,
                align: "center",
                sortable: true,
            },
        ],
        pager: "#pager_media_brand",
        viewrecords: true,
        caption: "媒体品牌列表",
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            var rowData = $("#table_media_brand").jqGrid("getRowData", rowid);   //获取选中行信息
            //可以进入查看页面

        },

    });
    $tableMedias.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $tableMedias.setSelection(10, true);
}

function createViewTable() {
    var $tableMedias = $("#brand_view_table");
    $tableMedias.jqGrid({
        url: baseUrl + '/mediaBrand/listPgForView',
        datatype: "json",
        postData: null,
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
        sortable: true,
        sortname: "",
        multiselect: true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        prmNames: {
            page: "page",
            rows: "size",
            totalrows: "totalElements",
            sort: "sort",
            order: "order",
        },
        colModel: [
            {
                name: 'id',
                label: 'id',
                editable: true,
                hidden: true,
                sortable: false,
                sorttype: "int",
                search: true
            },
            {
                name: 'brandName',
                label: '品牌名称',
                editable: true,
                sortable: false,
                width: 30,
                align: "center",
                formatter: function (v, options, row) {
                    return v;
                }
            },
            {
                name: 'platform',
                label: '投放平台',
                editable: true,
                sortable: false,
                width: 20,
                align: "center",
                formatter: function (v, options, row) {
                    if(v==1){
                        return "抖音";
                    }else if(v==2){
                        return "小红书";
                    }else {
                        return "微博";
                    }
                }
            },
            {
                name: 'userName',
                label: '投放账号名称',
                editable: true,
                sortable: false,
                width: 30,
                align: "center"
            },
            {
                name: 'times',
                label: '投放次数',
                width: 20,
                align: "center",
                sortable: true,
            },
        ],
        pager: "#brand_view_pager",
        viewrecords: true,
        caption: "媒体品牌列表",
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {

        },
    });
    $tableMedias.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $tableMedias.setSelection(10, true);
}

//跳转页面
function jumpHtml(plateId,brandName) {
    $("#brandViewModal").modal('toggle');
    $("#brandViewForm input[name='brandNameQc']").val(brandName);
    $("#brandViewForm input[name='platformQc']").val(plateId);
    $("#brandViewForm input[name='userNameQc']").val("");
    var plateFormName = "";
    if(plateId==1){
        plateFormName = "抖音";
    }else if(plateId==2){
        plateFormName = "小红书";
    }else if(plateId==3){
        plateFormName = "微博";
    }
    $("#brandViewForm #platformName").val(plateFormName);
    queryBrandView();
    resize("#brand_view_table");
}

//刷新表格
function queryMediaBrand() {
    //刷新表格
    $("#table_media_brand").emptyGridParam(); //清空历史查询数据
    $("#table_media_brand").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeForm(), //发送数据
    }).trigger("reloadGrid"); //重新载入
    resize("#table_media_brand");
}

//刷新账号详情表格
function queryBrandView() {
    //刷新表格
    $("#brand_view_table").emptyGridParam(); //清空历史查询数据
    $("#brand_view_table").jqGrid('setGridParam', {
        postData: $("#brandViewForm").serializeForm(), //发送数据
    }).trigger("reloadGrid"); //重新载入
    resize("#brand_view_table");
}