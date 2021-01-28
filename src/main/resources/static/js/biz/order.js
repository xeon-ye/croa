var dockingPeopleMap = {};//客户公司对接人缓存，数据格式：{custId:[]}
var mediaUserMap = {};//媒介缓存，数据格式：{plateId:[]}
var sysConfigMap = {}; //系统配置功能
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_orders').setGridWidth(width);
    });
    dockingPeopleMap = {};//客户公司对接人缓存，数据格式：{custId:[]}
    mediaUserMap = {};//媒介缓存，数据格式：{plateId:[]}
    init();
});

// function texPoint() {
//     var html = '&nbsp;<select></select>';
//     $.ajax({
//         url:  '/dict/list',
//         type: 'get',
//         data: {'typeCode': 'tax'},
//         success: function (data) {
//             $(data).each(function (i, d) {
//                 console.log(d)
//                 html += '<option value="' + d.code + '">' + d.name + '</option>';
//             })
//             html += '</select>';
//             return html;
//         }
//     });
//     return html;
// }

function init() {
    $("#orderForm").validate({
        rules: {
            companyId: {required: true},
            custId: {required: true},
            title: {required: true},
            amountDate: {required: true}
        }, messages: {
            companyId: {required: "请选择客户公司"},
            custId: {required: "请选择客户公司对接人"},
            title: {required: "请输入订单标题"},
            amountDate: {required: "请输入到款时间"}
        }
    });

    //请求系统参数
    commonObj.requestData(null, "/sysConfig/getAllConfig", "get", "json", false, function (data) {
        //由于日期类型为数字需要格式处理
        for(var k in data){
            if(data[k].dataType == 'date' && data[k].pattern){
                data[k].value = new Date(data[k].value).format(data[k].pattern.replace(/H/g, "h"));
            }
        }
        sysConfigMap = data;
    });
    //根据配置决定是否必须开票,系统稿件开票模式：1-必须开票、2-不开票、3-两种模式都兼容，没有配置默认必须开票
    if(sysConfigMap &&  sysConfigMap["artTaxModel"] && sysConfigMap["artTaxModel"]["value"] == 1){
       $("#taxes").find("option[value=0]").remove();
    }else if(sysConfigMap &&  sysConfigMap["artTaxModel"] && sysConfigMap["artTaxModel"]["value"] == 2){
        $("#taxes").html(" <option value=\"0\">不开票</option>");
    }

    $("#table_orders").jqGrid({//2600
        url: baseUrl + '/article/list/' + $("#orderId").val(),
        datatype: "json",
        // postData: $("#termForm").serializeJson(),
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
                name: 'mediaTypeId',
                label: 'mediaTypeId',
                editable: true,
                hidden: true,
                sorttype: "int",
                search: true
            },
            {
                name: "filePath",
                hidden: true
            },
            {
                name: 'mediaTypeName',
                label: '稿件类别',
                editable: true,
                width: 20,
                align: "center"
            },
            {
                name: 'mediaName',
                label: '媒体名称',
                editable: true,
                width: 40,
                align: "center"
            },
            {
                name: 'priceType',
                hidden: true,
                label: '价格类型'
            },
            {
                name: 'supplierName',
                label: '供应商名称',
                editable: true,
                width: 40,
                align: "center",
                hidden:true
            },
            {
                name: 'num',
                label: '数量',
                editable: true,
                width: 20,
                align: "center",
                classes: 'text-danger',
                formatter: function (v, options, row) {
                    return v || 0;
                }
            },
            {
                name: 'payAmount',
                label: '成本价',
                editable: true,
                width: 20,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},

            },
            {
                name: 'mediaUserId',
                hidden: true,
            },
            {
                name: 'mediaId',
                hidden: true,
            },
            {
                name: 'mediaUserName',
                label: '责任人',
                editable: true,
                width: 36,
                align: "center",
                formatter: function (v, options, row) {
                    return v;
                }
            },
            {
                name: 'saleAmount',//应收
                label: '客户报价',
                editable: true,
                width: 20,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},

            },
            {
                name: 'taxeType',
                label: '抬头' + $("#taxesDiv").html(),
                editable: true,
                width: 26,
                sortable: false,
                align: "center",
                formatter: function (v, options, row) {
                    var sel = $("#taxes").html();
                    $("#taxesDiv").remove();
                    return "<select name='tax' id='tax_" + row.id + "' class='form-control' onchange='setTax(" + row.id + ",this.value)'>" + sel + "</select>";
                }
            },
            {
                name: 'taxes',
                label: '税金',
                editable: true,
                width: 20,
                align: "center",
                classes: 'text-danger',
                formatter: function (v, options, row) {
                    return "￥" + (v ? v : (row.saleAmount * $("#taxes").val()).toFixed(3));
                }
            },
            {
                name: 'taxPoint',
                label: '税点',
                editable: true,
                width: 20,
                align: "center",
                classes: 'text-danger',
                formatter: function (v, options, row) {
                    return v ? v : $("#taxes").val();
                }
            },
            {
                name: 'amount',
                label: '含税价',
                editable: true,
                width: 23,
                align: "center",
                classes: 'text-danger',
                formatter: function (v, options, row) {
                    return "￥" + (v ? v : (row.saleAmount + row.saleAmount * $("#taxes").val()).toFixed(3));
                }
            },
            {
                name: 'promiseDate',
                label: '答应到款时间',
                width: 30,
                // hidden: true
            },
            {
                name: 'custName',
                label: '客户公司名称',
                editable: true,
                width: 50,
                align: "center",
                formatter: function (v, options, row) {
                    return $("#companyId").find("option:selected").text();
                }
            },
            {
                name: 'dockingPeople',
                label: '客户对接人',
                width: 25,
                align: "center",
                formatter: function (v, options, row) {
                    return $("#custId").find("option:selected").text();
                }
            },
            {
                name: 'link',
                label: '链接',
                width: 60,
                align: "center",
                formatter: function (v, options, row) {
                    return v ? v : '';
                }
            },
            {
                name: 'title',
                label: '标题',
                width: 60,
                align: "center",
                formatter: function (v, options, row) {
                    return '<a href="' + (row.link ? row.link : "") + '">' + (v ? v : "") + '</a>';
                    // return (v ? v : "");
                    // return "www.baidu.com";
                }
            }

        ],
        pager: "#pager_orders",
        viewrecords: true,
        caption: "媒体列表",
        hidegrid: false,
        loadComplete: function (data) {
            if (getResCode(data))
                return;
        }, ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            // page('/media/modifyMedia?id=' + rowid, '媒体修改' + rowid);
        }, onSelectRow: function (rowId, row, col, e) {
            $("#articleId").val(rowId);
            // $("#articleTitle").removeAttrs("disabled");
            // $("#link").removeAttrs("disabled");
            // $("#file").removeAttrs("disabled");
            // $("#amountDate").removeAttrs("disabled");
            $("input:disabled,select:disabled").removeAttrs("disabled");
            var a = $("#table_orders").getCell(rowId, "title");
            $("#articleTitle").attr("data-href", $(a).attr("href"));
            $("#articleTitle").val($(a).text());
            $("#link").val($("#table_orders").getCell(rowId, "link"));
            $("#amountDate").val($("#table_orders").getCell(rowId, "promiseDate"));
            var htmlPath = $("#table_orders").getCell(rowId, "filePath");
            // $("#file").val(htmlPath);
            if (htmlPath) {
                $.ajax({
                    url: baseUrl + "/article/loadHtml",
                    type: 'get',
                    data: {"htmlPath": htmlPath},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200)
                            $("#articleContent").code(data.data.stream);
                        else {
                            $("#articleContent").code("");
                        }
                    }
                });
            } else {
                $("#articleContent").code("");
            }

            //根据板块查询媒介并渲染媒介下拉列表
            //判断是否有缓存客户对接人，有的话不查询
            var mediaTypeId = $("#table_orders").getCell(rowId, "mediaTypeId");
            var mediaUserId = $("#table_orders").getCell(rowId, "mediaUserId");
            if(!mediaUserMap[mediaTypeId] || mediaUserMap[mediaTypeId].length < 1){
                commonObj.requestData(null, "/user/listMJByMediaTypeId/"+mediaTypeId, "get", "json", false, function (data) {
                    if (data.code == 200) {
                        var list = data.data.listMJByMediaTypeId;
                        mediaUserMap[mediaTypeId] = list;
                    }
                });
            }
            $("#mediaUserId").empty();
            $(mediaUserMap[mediaTypeId]).each(function (i, item) {
                var sel = item.id == mediaUserId ? "selected='selected'" : "";
                $("#mediaUserId").append('<option value="' + item.id + '" ' + sel + '>' + item.name + '</option>');
            });

            //渲染表单
            layui.use('form', function(){
                var form = layui.form;
                form.render();
            });
        }
    });
    $("#table_orders").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $("#table_orders").setSelection(4, true);
}

/**
 * 设置答应到款时间
 * @param v
 */
function setDate(v) {
    var ids = $("#table_orders").getDataIDs();
    // console.log(ids);
    for (var i = 0; i < ids.length; i++) {
        $("#table_orders").setCell(ids[i], "promiseDate", v);
    }
}

/**
 * 更改媒介人员
 * @param v
 */
function setMediaUserId(t) {
    var rowId = $("#articleId").val();
    $("#table_orders").setCell(rowId, "mediaUserId", $(t).val());
    $("#table_orders").setCell(rowId, "mediaUserName", $(t).find("option:selected").text());
}

//选择客户公司名称
function onloadCompany(t) {
    var custId = $(t).val();
    if (custId && custId.length > 0) {
        $("#companyName").val($(t).find("option:selected").text());
        //判断是否有缓存客户对接人，有的话不查询
        if(!dockingPeopleMap[$(t).val()] || dockingPeopleMap[$(t).val()].length < 1){
            commonObj.requestData(null, "/dockingPeople/listByCustId/"+$(t).val(), "get", "json", false, function (data) {
                dockingPeopleMap[$(t).val()] = data;
            });
        }
        $("#custId").html("");
        $(dockingPeopleMap[$(t).val()]).each(function (i, d) {
            var user = d.user;
            $("#custId").append("<option value='" + d.id + "' data-user-id='" + user.id + "' data-user-name='" + user.name + "' data-dept-id='" + user.dept.id + "'>" + d.custName + "</option>");
        });
        setCustId($("#custId"));
        var ids = $("#table_orders").getDataIDs();
        var rowNum = ids.length;
        for (var i = 0; i < rowNum; i++) {
            $("#table_orders").setCell(ids[i], "custName", $(t).find("option:selected").text());
            $("#table_orders").setCell(ids[i], "dockingPeople", $("#custId").find("option:selected").text());
        }
    }
    //渲染表单
    layui.use('form', function(){
        var form = layui.form;
        form.render();
    });
}

//选择客户
function setCustId(t) {
    var sel = $(t).find("option:selected");
    $("#custName").val(sel.text());
    var userId = sel.attr("data-user-id");
    var userName = sel.attr("data-user-name");
    var deptId = sel.attr("data-dept-id");
    $("#userId").val(userId);
    $("#userName").val(userName);
    $("#deptId").val(deptId);
    var ids = $("#table_orders").getDataIDs();
    var rowNum = ids.length;
    for (var i = 0; i < rowNum; i++) {
        $("#table_orders").setCell(ids[i], "dockingPeople", sel.text());
    }
    //渲染表单
    layui.use('form', function(){
        var form = layui.form;
        form.render();
    });
}

//选择稿件行业类型
function setTypeCode(t) {
    var sel = $(t).find("option:selected");
    $("input[name='typeName']").val(sel.text());
}

function setTaxes(t) {
    var ids = $("#table_orders").getDataIDs();
    var rowNum = ids.length;
    var tax = $(t).val();
    for (var i = 0; i < rowNum; i++) {
        $("#table_orders").setCell(ids[i], "taxPoint", tax);
        var saleAmount = $("#table_orders").getCell(ids[i], "saleAmount");//报价
        var taxes = parseFloat(saleAmount) * parseFloat(tax);//计算税金
        $("#table_orders").setCell(ids[i], 'taxes', parseFloat(taxes).toFixed(3));
        $("#table_orders").setCell(ids[i], "amount", (parseFloat(saleAmount) + parseFloat(taxes)).toFixed(3));//含税价
    }
    // $("select[name='tax']").val(tax);
    //在表格中每一行显示统一修改后的税种text
    var text = $(t).find("option:selected").text();
    //根据text设置选中状态
    $("select[name='tax'] option").each(function () {
        if ($(this).text() == text) {
            $(this).prop("selected", true);
        }
        // else
        //     $(this).prop("selected", false);
    });
    //$("select[name='tax'] option:selected").focus();
    //$("select[name='tax']").find("option:contains('" + text + "')").attr("selected", true);
    // $("select[name='tax']").text(text);
}

function setTax(rowId, v, i) {
    $("#table_orders").setCell(rowId, "taxPoint", v);
    var saleAmount = $("#table_orders").getCell(rowId, "saleAmount");//报价
    var taxes = parseFloat(saleAmount) * v;//计算税金
    $("#table_orders").setCell(rowId, 'taxes', parseFloat(taxes).toFixed(3));
    $("#table_orders").setCell(rowId, "amount", (parseFloat(saleAmount) + parseFloat(taxes)).toFixed(3));//含税价
}

function setArticleTitle(t) {
    // var rowId = $(t).attr("data-rowId");
    var rowId = $("#articleId").val();
    $("#table_orders").setCell(rowId, "title", $(t).val());
}

function setLink(t) {
    // var rowId = $(t).attr("data-rowId");
    var rowId = $("#articleId").val();
    $("#table_orders").setCell(rowId, "link", $(t).val());
    // var title = $("#table_orders").getCell(rowId, "title");
    // title = $(title).attr("href", $(t).val());
    // $("#table_orders").getCell(rowId, "title", title);
}

/**
 * 提交订单
 */
function saveOrder() {
    var dataValid = true;
    var ids = $("#table_orders").getDataIDs();
    var json = [];
    var orderAmount = 0;
    for (var i = 0, rowNum = ids.length; i < rowNum; i++) {
        var taxes = $("#table_orders").getCell(ids[i], "taxes");//税金
        var amount = $("#table_orders").getCell(ids[i], "amount");//saleAmount 含税价
        var taxPoint = $("#table_orders").getCell(ids[i], "taxPoint");
        var title = $("#table_orders").getCell(ids[i], "title");
        var link = $("#table_orders").getCell(ids[i], "link");
        var promiseDate = $("#table_orders").getCell(ids[i], "promiseDate");
        var mediaUserId = $("#table_orders").getCell(ids[i], "mediaUserId");
        var mediaUserName = $("#table_orders").getCell(ids[i], "mediaUserName");
        var taxType = $("#tax_" + ids[i]).find("option:selected").attr("id");
        var mid = $("#mediaUserId").val();
        //console.log(mid);
        // var custId = $("#custId").val();
        // var companyId = $("#companyId").val();
        json[i] = {
            id: ids[i],
            taxes: taxes.replace('￥', ''),
            saleAmount: amount.replace('￥', ''),
            taxPoint: taxPoint.replace('￥', ''),
            title: $(title).text(),
            link: link,
            promiseDate: promiseDate,
            mediaUserId: mediaUserId,
            mediaUserName: mediaUserName,
            taxType: taxType,
        };
        orderAmount += parseFloat(amount.replace('￥', ''));

        // if (title.length <= 0 || link.length <= 0 || promiseDate.length <= 0 || mediaUserId.length <= 0 || mediaUserName.length <= 0) {
        //     dataValid = false;
        // }
        // var state = $("#mediaUserId").attr("disabled");
        if (promiseDate.length <= 0 || mediaUserId.length <= 0 || mediaUserName.length <= 0 || mid == null) {
            dataValid = false;
        }
    }
    if ($("#orderForm").valid() && dataValid) {
        var order = $("#orderForm").serializeJson();
        order.articles = json;
        order.amount = orderAmount;
        $.ajax({
            url: baseUrl + '/order/update',
            type: 'put',
            data: JSON.stringify(order),
            contentType: "application/json",
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg("订单提交成功！", {time: 2000, icon: 6});
                    setTimeout(function () {
                        closeCurrentTab();
                    }, 2000);
                } else {
                    layer.msg("订单提交失败！", {time: 3000, icon: 5});
                }
            }
        });
    } else {
        layer.msg("请完善订单消息并为每个媒体指定媒介！", {time: 3000, icon: 5});
    }
}

function upFile() {
    // $("#articleContent").html("asdfasdf");
    // $('#articleContent').code("asdfasdf");
    // uploadForm();
    if ($("#file").val()) {
        $.ajax({
            url: baseUrl + '/article/upload',
            type: 'POST',
            cache: false,
            data: new FormData($('#uploadForm')[0]),
            processData: false,
            contentType: false,
            dataType: "json",
            // beforeSend: function () {
            //     uploading = true;
            // },
            success: function (data) {
                if (data.code == 200) {
                    swal('提示！', '上传成功！', 'success', 1000);
                    // console.log(data.data.stream);
                    $("#articleContent").code(data.data.stream);
                } else {
                    swal('提示！', '上传失败！', 'error', 1000);
                }
            }
        });
    }

}

/**
 * 取消订单
 */
function cancelOrder() {
    $.get(baseUrl + "/order/del/" + $("#orderId").val(), function (data) {
        if(data.code == 200){
            layer.msg("订单取消成功！", {time: 2000, icon: 6});
            setTimeout(function () {
                closeCurrentTab();
            }, 2000);
        }else {
            layer.msg(data.msg, {time: 3000, icon: 5});
        }
    })
}

//页面公共处理对象
var commonObj = {
    //后台请求方法
    requestData: function (data, url, requestType,dataType,async,callBackFun, callErrorFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun,
        };
        if(callErrorFun){
            param.error = callErrorFun;
        }
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    }
}