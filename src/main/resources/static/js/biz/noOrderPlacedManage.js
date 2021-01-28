var sysConfigMap = {}; //系统配置功能
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#orderTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });

    //请求系统参数
    requestData(null, "/sysConfig/getAllConfig", "get", "json", false, function (data) {
        //由于日期类型为数字需要格式处理
        for(var k in data){
            if(data[k].dataType == 'date' && data[k].pattern){
                data[k].value = new Date(data[k].value).format(data[k].pattern.replace(/H/g, "h"));
            }
        }
        sysConfigMap = data;
    });
    //根据配置决定是否展示稿件生成按钮,系统稿件生成策略：0-业务员下单模式、1-媒介创建模式、2-两种模式都兼容，没有配置默认兼容两种
    if(sysConfigMap &&  sysConfigMap["orderCreateModel"] && sysConfigMap["orderCreateModel"]["value"] == 1){
        layer.alert('系统不支持下单模式，如需要请配置！', function(index){
            layer.close(index);
            closeCurrentTab();
        });
    }

    searchForm.init(); //初始化条件
    createTable();//创建表格
    searchForm.search();
});



//查询区域
var searchForm = {
    init: function () {
        //加载板块
        requestData(null, "/mediaPlate/0", "get", "json", true, function (data) {
            var html = " <option value=\"\">请选择媒体板块</option>";
            if(data && data.length > 0){
                $(data).each(function (i, mediaPlate) {
                    html +=  "<option value='" + mediaPlate.id + "'>" + mediaPlate.name + "</option>";
                });
            }
            $("select[name='mediaTypeId']").html(html);

            //渲染表单
            layui.use('form', function(){
                layui.form.render("select");
            });
        });

        //加载媒介
        requestData(null, "/user/listByType/MJ", "get", "json", true, function (data) {
            var html = " <option value=\"\">请选择媒介</option>";
            if(data && data.length > 0){
                $(data).each(function (i, d) {
                    html +=  "<option value='" + d.id + "'>" + d.name + "</option>";
                });
            }
            $("select[name='mediaUserId']").html(html);

            //渲染表单
            layui.use('form', function(){
                layui.form.render("select");
            });
        });


    },
    search:function () {
        reflushTable();
    },
    enterEvent:function (event) {
        if(event.keyCode == '13' || event.keyCode == 13){
            searchForm.search();
        }
    }
};

//订单表
var orderTableObj = {
    grid:{},
    orderTable:{
        url: baseUrl + '/order/listOrderByNotPlaced',
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
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15, //每页记录数
        rowList: [15, 30, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                hidden: true,
            },
            {
                name: 'no',
                label: '订单编号',
                width: 125,
                editable: true,
                sortable: false,
                cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    //合并单元格
                    return "id='no" + rowId + "'";
                },
            },
            {
                name: 'mediaTypeName',
                label: '媒体板块',
                width: 80,
                editable: true,
                sortable: false
            },
            {
                name: 'mediaName',
                label: '媒体名称',
                width: 130,
                editable: true,
                sortable: false
            },
            {
                name: 'mediaUserName',
                label: '媒介',
                width: 80,
                editable: true,
                sortable: false
            },
            {
                name: 'num',
                label: '稿件数量',
                width: 60,
                editable: true,
                sortable: false
            },
            {
                name: 'saleAmount',
                label: '客户报价',
                width: 80,
                editable: true,
                sortable: false
            },
            {
                name: 'priceType',
                label: '价格类型',
                width: 100,
                editable: true,
                sortable: false
            },
            {
                name: 'pay_amount',
                label: '成本价',
                width: 80,
                editable: true,
                sortable: false
            },
            {
                name: 'unitPrice',
                label: '单价',
                width: 80,
                editable: true,
                sortable: false
            },
            {
                name: 'createTime',
                label: '创建时间',
                width: 100,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    return v ? new Date(v).format("yyyy-MM-dd hh:mm:ss") : "";
                }
            },
            {
                name: 'amount',
                label: '订单总价',
                width: 80,
                editable: true,
                sortable: false,
                cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    //合并单元格
                    return "id='amount" + rowId + "'";
                },
            },
            {
                name:'op',
                label: '操作',
                width: 120,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    var html = "<a class='text-success' onclick='submitOrder("+row.id+");'>提交订单&nbsp;</a>";
                    html += "<a class='text-success' onclick='cancelOrder("+row.id+");'>取消订单&nbsp;</a>";
                    return html;
                },
                cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    //合并单元格
                    return "id='op" + rowId + "'";
                },
            },
        ],
        pager: "#orderTablePaper",
        viewrecords: true,
        caption: '未下单订单表',
        hidegrid: false,
        gridComplete: function () {
            var primaryKey = "id";
            orderTableObj.grid.mergerCell('no', primaryKey);
            orderTableObj.grid.mergerCell('amount', primaryKey);
            orderTableObj.grid.mergerCell('op', primaryKey);

            var width = $('#orderTable').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#orderTable').setGridWidth(width);
        }
    }
}

//表格定义
function createTable() {
    orderTableObj.grid = new dataGrid("orderTable", orderTableObj.orderTable);
    orderTableObj.grid.loadGrid();
    var $orderTable = $("#orderTable");
    $orderTable.setGridWidth($orderTable.closest('.jqGrid_wrapper').width());
    $orderTable.setGridHeight(525);
}

//刷新表格
function reflushTable() {
    //刷新表格
    $("#orderTable").emptyGridParam(); //清空历史查询数据
    $("#orderTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//提交订单
function submitOrder(orderId) {
    page("/order/getById/" + orderId, "媒体下单-订单详情");
}

//取消订单
function cancelOrder(orderId) {
    requestData(null, "/order/del/" + orderId, "get", "json", true, function (data) {
        if(data.code == 200){
            layer.msg("订单取消成功！", {time: 2000, icon: 6});
            reflushTable();//刷新列表
        }else {
            layer.msg(data.msg, {time: 3000, icon: 5});
        }
    });
}

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,dataType,async,callBackFun, contentType) {
    var param = {
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun,
        error: function () {
            Ladda.stopAll();
        }
    };
    if(contentType){
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
}