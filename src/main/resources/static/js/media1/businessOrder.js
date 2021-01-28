var url = baseUrl + "/media1";


/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,dataType,async,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun
    });
}

//全局参数定义
var plateMap = {}; //缓存页面媒体板块，结构：{plateId: plateName}
var userMJMap = {}; //缓存页面媒介，结构：{userId: userName}
var mediaTermMap = {}; //缓存板块对应的媒体查询条件，结构：{plateId: {fromId: formObj}}
var mediaTermSelectMap = {}; //缓存板块对应的媒体下拉筛选项，结构：{plateId: [...]}
var mediaTermULDefaultHTML = ""; //缓存默认条件html
var sysConfigMap = {}; //系统配置功能
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#table_medias");
        var width = tableElement.closest('.jqGrid_wrapper').width();
        tableElement.setGridWidth(width);

        var supplierPriceNode = $("#mediaSupplierPriceTable");
        supplierPriceNode.setGridWidth(supplierPriceNode.closest('.jqGrid_wrapper').width());

        var $orderTable = $("#table_orders");
        $orderTable.setGridWidth($orderTable.closest('.jqGrid_wrapper').width());
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

    // $("#mediaSupplierPriceModal").draggable();//配置模态框可拖动 需要引入“jquery-ui-1.10.4.custom.min.css”和 “jquery-ui.min.js”
    //模态框弹出前调用的事件，可当做钩子函数-在调用 show 方法后触发。
    $('#mediaSupplierPriceModal').on('show.bs.modal', function () {
        //设置模态框样式等
        // var $modal_dialog = $(this).find(".modal-dialog");
        // var m_top = ($(window).height() - $modal_dialog.height())/2;
        // $modal_dialog.css({"margin": m_top + "px auto"});
    });
    //模态框弹出后调用的事件，可当做钩子函数-当模态框对用户可见时触发（将等待 CSS 过渡效果完成）。
    $('#mediaSupplierPriceModal').on('shown.bs.modal', function () {
        initSupplierTable(); //初始化供应商价格表
        //请求数据
        var mediaId = $("#modalMediaId").val();
        var cell = $("#modalCell").val();
        if(mediaId){
            requestData({mediaId: mediaId, cell: cell},"/media1/getMediaSupplierInfoByMediaId","post","json",false,function (data) {
                var reader = {
                    root: function(obj) { return data.data.media; }
                };
                $("#mediaSupplierPriceTable").emptyGridParam();
                $("#mediaSupplierPriceTable").setGridParam({data: data.data.media, reader: reader}).trigger('reloadGrid');
            });
        }
    });
    //模态框关闭前调用的事件，可当做钩子函数
    $('#mediaSupplierPriceModal').on('hidden.bs.modal', function () {
        //如果没有选中单选框
        if(!($("#checkFlag").val() && $("#checkFlag").val() == 1)){
           $("#"+ $("#mediaPriceRadioId").val()).iCheck('uncheck');
        }
        //销毁历史数据
        $("#modalMediaId").val("");
        $("#modalCell").val("");
        $("#mediaPriceRadioId").val("");
        $("#checkFlag").val(0);
    });

    //模态框弹出后调用的事件，可当做钩子函数-当模态框对用户可见时触发（将等待 CSS 过渡效果完成）。
    $('#mediaSupplierPriceModal1').on('shown.bs.modal', function () {
        supplierObj.initSupplierTable(); //初始化供应商价格表
        //请求数据
        var mediaId = $("#modalMediaId1").val();
        if(mediaId){
            requestData(null,"/media1/getMediaSupplierInfoByMediaId/"+mediaId,"get","json",false,function (data) {
                var reader = {
                    root: function(obj) { return data.data.media; }
                };
                $("#mediaSupplierPriceTable1").emptyGridParam();
                $("#mediaSupplierPriceTable1").setGridParam({data: data.data.media, reader: reader}).trigger('reloadGrid');
            });
        }
    });
    //模态框关闭前调用的事件，可当做钩子函数
    $('#mediaSupplierPriceModal1').on('hidden.bs.modal', function () {
        //销毁历史数据
        $("#modalMediaId1").val("");
    });

    //更多点击事件
    $("#more").click(function () {
        var iNode = $("#more > i:eq(1)");
        var classStr = iNode.attr('class');
        if(classStr.indexOf("fa-chevron-up") != -1){ //当前其他条件为隐藏
            iNode.removeClass("fa-chevron-up");
            iNode.addClass("fa-chevron-down");
            $("#otherCondition").fadeIn("slow");
        }else{
            iNode.removeClass("fa-chevron-down");
            iNode.addClass("fa-chevron-up");
            $("#otherCondition").fadeOut("slow");
        }
    });

    loadAllMediaMJ($("#userName"));//加载责任人，然后保存默认页面
    mediaTermULDefaultHTML = $("#mediaTermUL").html(); //缓存页面默认公共条件
    initMediaType(); //加载板块
    // createTable(); //初始化表格
    reflushOrderTable(); //初始化订单表

    statisticsFTModal.init();//初始化模态框
});

//加载责任人
function loadAllMediaMJ(t){
    requestData(null,"/user/listByType/MJ","get","json",false,function (data) {
        $(data).each(function (i, d) {
            userMJMap[d.id] = d.name;
            var value = $(t).attr("data-value");
            var selected = value == d.id ? "selected=selected" : "";
            $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
        });
    })
}

//加载媒体板块
function initMediaType() {
    requestData(null,"/mediaPlate/0","get","json",false, function (data) {
        if (data == null || data == '') {
            swal("没有板块可操作！", "没有查询到板块信息，请联系管理员赋权！", "warning");
            return;
        }
        var standardHtml = "";
        var notStandardHtml = "";
        $(data).each(function (i, item) {
            plateMap[item.id] = item.name;
            if (item.standarPlatformFlag == 1) {
                standardHtml += "<div style='width: 10%;float: left;'><span style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;width: 100%;' class='btn btn-outline plateSpan' title='" + item.name + "' data-value='" + item.id + "' onclick='setType(" + item.id + ",this)'>" + item.name + "</span></div>";
            } else {
                notStandardHtml += "<div style='width: 10%;float: left;'><span style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;width: 100%;' class='btn btn-outline plateSpan' title='" + item.name + "' data-value='" + item.id + "' onclick='setType(" + item.id + ",this)'>" + item.name + "</span></div>";
            }
        });
        //判断标准和非标准是否有，控制隐藏显示
        if (standardHtml) {
            $("#extendFormStandardPlateWrap").css("display", "flex");
            $("#extendFormStandardPlate").html(standardHtml);
        } else {
            $("#extendFormStandardPlateWrap").css("display", "none");
            $("#extendFormStandardPlate").html("");
        }
        if (notStandardHtml) {
            $("#extendFormNotStandardPlateWrap").css("display", "flex");
            $("#extendFormNotStandardPlate").html(notStandardHtml);
        } else {
            $("#extendFormNotStandardPlateWrap").css("display", "none");
            $("#extendFormNotStandardPlate").html("");
        }
        var type = getQueryString("type");
        if (type && type === '2') {
            $("#mediaPlate span[data-value='" + type + "']").click();
        } else {
            $("#mediaPlate>div:first-child>span:first-child").click();

            //如果标准有值，则先查询标准的
            if (standardHtml) {
                $("#extendFormStandardPlate > div:first-child > span:first-child").click();
            } else {
                if (notStandardHtml) {
                    $("#extendFormNotStandardPlate > div:first-child > span:first-child").click();
                }
            }
        }
    });
}

function setType(id, t) {
    $(t).parent().parent().find("div>span").each(function (i, item) {
        $(item).removeClass("btn-primary");
        if (t == item) {
            $(t).addClass("btn-primary");
        }
    });

    //如果是微信板块，显示微信ID搜索
    if(id == 1){
        $("#wechat").css("display","inline-block");
        $("#ks").css("display","none");
        $("#douyin").css("display","none");
    } else if(id == 445){ //如果是快手板块，显示快手ID搜索
        $("#ks").css("display","inline-block");
        $("#wechat").css("display","none");
        $("#douyin").css("display","none");
    }else if(id == 12){
        $("#ks").css("display","none");
        $("#wechat").css("display","none");
        $("#douyin").css("display","inline-block");
    }else{
        $("#wechat").css("display","none");
        $("#ks").css("display","none");
        $("#douyin").css("display","none");
    }

    $("#mediaTypeText").text($(t).text());
    $("#plateId").val(id);

    $("#wechatId").val('');//初始化微信ID
    $("#wechat").val(''); //初始化微信ID

    $("#ksId").val('');//初始化快手ID
    $("#ks").val(''); //初始化快手ID

    $("#mediaName").val('');//初始化媒体名称
    $("#mName").val('');//初始化媒体名称
    $("#userId").val('');  //初始化责任人
    // 清空之后动态加载的查询条件；
    $("#plateId").nextAll().remove();

    $("#mediaTermUL").html(mediaTermULDefaultHTML);//每次选媒体板块，重新覆盖

    //查询条件
    if(!mediaTermMap[id]){
        requestData(null,"/mediaTerm1/" + id,"get","json",false,function (datas) {
            mediaTermMap[id] = datas; //缓存媒体查询条件
            renderPageCondition(datas);//渲染页面条件
        });
    }else{
        renderPageCondition(mediaTermMap[id]);//渲染页面条件
    }
    //设置筛选项，导出筛选判断使用
    if(!mediaTermSelectMap[id] || mediaTermSelectMap[id].length <= 0){
        if(!mediaTermSelectMap[id]){
            mediaTermSelectMap[id] = new Array();
            mediaTermSelectMap[id].push("userId"); //责任人字段
        }
        $(mediaTermMap[id]).each(function (i, data) {
            var data = data;
            var cell = "cell:" + data.cell + ":" + data.type;
            if("select" == data.type && !mediaTermSelectMap[id].contains(cell)){
                mediaTermSelectMap[id].push(cell);
            }
        });
    }


    createTable(); //重新加载表格
    reflushTable();//刷新表格

    $('#otherCondition .i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    layui.use('form', function(){
        layui.form.render('select');//layui重新渲染下拉列表
    });
}

//重新渲染页面条件
function renderPageCondition(datas) {
    var html = '';
    var priceHtml = '';//缓存金额和数字范围HTML
    $(datas).each(function (i, data) {
        var data = data;
        var cellName = data.cellName;
        var cell = "cell:" + data.cell + ":" + data.type;
        switch (data.type) {
            case 'radio':
            case 'checkbox':
                html += "<li class='col-md-12'><label class=\"col-md-1\" style='float:left;'>";
                html += cellName + '：</label><div class="col-md-11" style="padding-left: 15px;">';
                var dataClass = data.type == "radio" ? "radio-inline" : "checkbox-inline";
                var dbHtml = "";
                if(data.dbJson){  //如果dbJson字段有值则使用，否为dbsql有值
                    var json = eval(data.dbJson);
                    if (Array.isArray(json)){
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += "<span class='"+dataClass+" col-md-1 i-checks' title='"+text+"' style=''><input id=\"${name}\" name=\"${name}\" cell-name=\"${cell-name}\" type='"+data.type+"' value='" + item.value + "' /> " + text + "</span>";
                        });
                    }
                }else{
                    var datas = data.datas;
                    if (datas && Object.getOwnPropertyNames(datas).length > 0) {  //如果对象存在，并且个数大于0
                        for(var key in datas){
                            dbHtml += "<span  class='"+dataClass+" col-md-1 i-checks' title='"+datas[key].name+"'><input id=\"${name}\" name=\"${name}\" cell-name=\"${cell-name}\" type='"+data.type+"' value='" + datas[key].id + "' /> " + datas[key].name + "</span>";
                        }
                    }
                }
                html += dbHtml;
                html += "</div></li>"

                break;
            case 'select':
                html += "<li class='col-md-12'><label class=\"col-md-1\" style='float:left;'>";
                html += cellName + '：</label><div class="col-md-11" style="padding: 0">';
                var dd="''";
                var dbHtml = '<span class="col-md-1" title="不限"><span class="text-danger bg-danger" style="padding: 5px" onclick="loadMedia(\''+cell+'\','+dd+',this)">不限</span></span>';
                if(data.dbJson){  //如果dbJson字段有值则使用，否为dbsql有值
                    var json = eval(data.dbJson);
                    if (Array.isArray(json)){
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += '<span class="col-md-1" title="'+text+'"><span class="" style="padding: 5px" onclick="loadMedia(\''+cell+'\','+item.value+',this)">'+text+'</span></span>';
                        });
                    }
                }else{
                    var datas = data.datas;
                    if (datas && Object.getOwnPropertyNames(datas).length > 0) {  //如果对象存在，并且个数大于0
                        for(var key in datas){
                            dbHtml += '<span class="col-md-1" title="'+datas[key].name+'"><span class="" style="padding: 5px" onclick="loadMedia(\''+cell+'\','+datas[key].id+',this)">'+datas[key].name+'</span></span>';
                        }
                    }
                }
                html += dbHtml;
                html += "</div></li>"
                break;
            case 'price':
            case 'number':
                var nameMin = cell + ":min";
                var nameMax = cell + ":max";
                priceHtml += "<span class=\"col-md-3 form-inline\">\n" +
                    "             	<input name=\"${nameMin}\" size=\"1\" cell-name=\"${cell-name}\" class=\"form-control\">" +
                    "              -<input name=\"${nameMax}\" size=\"1\" cell-name=\"${cell-name}\" class=\"form-control\">&nbsp;\n" +
                    "               <label class=\"btn btn-sm btn-danger\">"+cellName+"</label>\n" +
                    "             </span>";
                priceHtml = priceHtml.th("nameMin",nameMin).th("nameMax",nameMax);
                break;
            default:
                html += "<li class='col-md-12'><label class=\"col-md-1\" style='float:left;'>";
                html += cellName + ':</label><div class="col-md-11" style="padding: 0">';
                html += data.dbHtml;
                html += "</div></li>";
                break;
        }
        html = html.th('id', cell).th('name', cell).th('labelName', cellName).th('type', data.type).th("cell-name", cellName);
        priceHtml = priceHtml.th('id', cell).th('name', cell).th('labelName', cellName).th('type', data.type).th("cell-name", cellName);
    });
    //循环结束后，添加价格区间
    if(priceHtml){
        html += "<li class='col-md-12'><label class=\"col-md-1\" style='float:left;'> 自定义栏：</label><div class=\"col-md-11\" style='padding: 0px;'>";
       /* html += "<span class=\"col-md-3 form-inline\">\n" +
            "             	<input name=\"priceStart\" size=\"1\" cell-name=\"priceStart\" class=\"form-control\">" +
            "              -<input name=\"priceEnd\" size=\"1\" cell-name=\"priceEnd\" class=\"form-control\">&nbsp;\n" +
            "               <label class=\"btn btn-sm btn-danger\">底价</label>\n" +
            "             </span>";*/
        html += priceHtml;
        html += "<span class=\"col-md-3 form-inline\"><label class=\"btn btn-sm btn-success\" onclick=\"loadMediaData(this);\">查询</label></span>";
        html += "</div></li>";
    }


    $("#mediaTermUL").html($("#mediaTermUL").html() + html); //在公共查询条件后面追加
    // 调整样式为居左对齐；
    $(".col-md-12 > label").css({"text-align": "left", "width": "120px", "margin": "0px", "padding": "0px"});
    $(".col-md-11 > span").css({"text-align": "left"});

    //单选和复选框添加选中事件
    $("#mediaTermUL").find('.i-checks').on('ifClicked', function (event) {
        var input = $(this).find(" input");
        if(input.attr("name") != 'enabled' && input.attr("name") != 'isCopy' ){
            loadMedia(input.attr("name"), input.val(), input);
        }else{
            if(input.attr("name") != 'isCopy'){
                $("#enabled").val(input.val());//初始化是否启用
            }else{
                $("#isCopy").val(input.val());//初始化是否拷贝
            }
            reflushTable();
        }
    });
}

//加载查询条件
function loadMedia(name, value, target) {
    //判断是否有值，有值则重新添加
    if(value){
        if ($(target)[0].tagName.toLowerCase() == 'span') { //如果是页面点击span，则再次点击移除
            $("#termForm > input[name='" + name + "']").remove(); // 先删除原有的同name条件；
            if($(target).hasClass("text-danger")){ //如果已经是点击状态，则再次点击取消该条件
                $(target).removeClass("text-danger bg-danger");
                $(target).parent().siblings(":first").find("span").addClass("text-danger bg-danger");
            }else{
                $(target).parent().parent().find("span").removeClass("text-danger bg-danger");
                $(target).addClass("text-danger bg-danger");
                var input = "<input type='hidden' name='" + name + "' value='" + value + "'/>";
                $("#termForm").append(input);
            }
        }else if($(target)[0].tagName.toLowerCase() == 'input' && $(target).attr("type") == "checkbox"){
            if(!$(target).parent().hasClass("checked")){  //判断是否已被选中，未被选中，新增
                var input = "<input type='hidden' name='" + name + "' value='" + value + "'/>";
                $("#termForm").append(input);
            }else{
                $("#termForm input[name='"+name+"']").each(function (i,node) {
                    if($(node).val() == value){
                        $(node).remove();
                    }
                })
            }
        }else{
            $("#termForm > input[name='" + name + "']").remove(); // 先删除原有的同name条件；
            var input = "<input type='hidden' name='" + name + "' value='" + value + "'/>";
            $("#termForm").append(input);
        }
    }else{
        // 先删除原有的同name条件；
        $("#termForm > input[name='" + name + "']").remove();
        $(target).parent().parent().find("b").removeClass("text-danger bg-danger");
        $(target).addClass("text-danger bg-danger");
    }
    reflushTable();
}

// 自定义栏的查询方法；
function loadMediaData(obj) {
    $(obj).closest("div").find("input").each(function () {
        //底价特别处理
        if($(this).attr("name") == "priceStart" || $(this).attr("name") == "priceEnd"){
            $("#termForm #"+$(this).attr("name")).val($(this).val());
        } else {
            // 先移除；
            $("#termForm > input[name='" + $(this).attr("name") + "']").remove();
            // 如果有值则存储；
            if ($(this).val().length > 0) {
                $("#termForm").append($(this).clone());
            }
        }
    });
    reflushTable();
}

//表格定义
function createTable() {
    var termForm = $("#termForm").serializeJson();
    var $tableMedias = $("#table_medias");
    $tableMedias.jqGrid({//2600
        url: baseUrl + '/media1/listMedia',
        datatype: "json",
        postData: termForm,
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
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        sortname: "id",
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
                sortable: true,
                sorttype: "int",
                search: true
            },
            {
                name: 'plateId',
                label: 'plateId',
                editable: true,
                hidden: true,
                sortable: true,
                sorttype: "int",
                search: true
            },
            {
                name: 'user',
                label: 'user',
                editable: true,
                hidden: true,
                sortable: true,
                sorttype: "int",
                search: true
            },
            {
                name: 'userId',
                label: '责任人',
                width: 12,
                align: "center",
                hidden: true
            },
            {
                name: 'picPath',
                label: '媒体图标',
                editable: true,
                width: 20,
                align: "center",
                formatter: function (v, options, row) {
                    if (v == undefined)
                        return '<img class="head-img" src="/img/mrt.png"/>';
                    return '<img class="head-img" src="' + v + '" onerror="src=\'/img/mrt.png\'"/>';
                }
            },
            {
                name: 'name',
                label: '媒体名称',
                editable: true,
                width: 30,
                align: "center",
              /*  formatter: function (v, options, row) {
                    if (!row.link){
                        return v;
                    }else{
                        return "<a class='text-success' target='_blank' href='" + row.link + "'>" + v + "</a>";
                    }
                }*/
            },
            {
                name: 'mediaContentId',
                label: '媒体ID',
                editable: true,
                width: 30,
                align: "center",
            },
            {
                name: 'link',
                label: '媒体链接',
                editable: true,
                width: 30,
                align: "center",
                formatter: function (v, options, row) {
                    if (!row.link){
                        return v;
                    }else{
                        return "<a class='text-success' target='_blank' href='" + row.link + "'>" + v + "</a>";
                    }
                }
            },
            {
                name: 'user.userName',
                label: '责任人',
                width: 12,
                align: "center",
                hidden: false
            },
            {
                label: "扩展",
                name: 'mediaExtends',
                width: 50,
                align: "left",
                formatter: function (v, options, row) {
                    var html = "";
                    var j = 1;
                    $(v).each(function (i, item) {
                        if (item.type != 'price') {
                            var value = item.cellValue;
                            var text = "无";
                            if(item.cellValue){
                                if(item.dbType == 'select' || item.dbType == 'radio' || item.dbType == 'checkbox'){
                                    text = item.cellValueText;
                                }else{
                                    text = item.cellValue;
                                }
                            }
                            if(item.type == 'link' && "无" != text){
                                html += "<div class='col-md-6' style='text-align:left;padding: 0;padding-right: 5px;'><span style='float:left' >" + item.cellName + ":</span><a class='text-success' style='float:left' target='_blank' href='"+text+"' title='"+text+"'>进入链接</a></div>";
                            }else{
                                html += "<div class='col-md-6' style='text-align:left;padding: 0;padding-right: 5px;'><span style='float:left' >" + item.cellName + ":</span><span class='text-danger' style='float:left'  >" + text + "</span></div>";
                            }
                            if (j++ % 2 == 0) {
                                html += "<br/>";
                            }
                        }
                    });
                    return html;
                }
            },
            {
                label: "价格",
                name: 'priceExt',
                editable: true,
                sortable: true,
                width: 40,
                align: "left",
                hidden: false,
                formatter: function (v, options, row) {
                    var html = "";
                    //将底价也加进来
                    // html += "<div class='col-md-12' style='text-align:center;padding: 0'><span >底价</span>:<span class='text-danger font-bold'>￥"+ row.price+"</span></div><br/>";
                    var val = row.mediaExtends;
                    var priceCellArr = [];
                    $(val).each(function (i, item) {
                        if (item.type == 'price' && item.cellValue && item.cellValue != 0) {
                            priceCellArr.push(item.cell);
                        }
                    });
                    $(val).each(function (i, item) {
                        if (item.type == 'price' && item.cellValue && item.cellValue != 0) {
                            //cell中含有->标识：并且是正确的格式，并且字段中含有主价格格式，表示这个价格是另一个价格附属，不能让业务员下单选择
                            var cellArr = item.cell.split("->");
                            if(!(cellArr.length == 2 && cellArr[1] && (priceCellArr.contains(cellArr[1]+"<1>"+cellArr[0]) || priceCellArr.contains(cellArr[1]+"<n>"+cellArr[0])))){
                                //根据媒体价格字段和媒体ID确定唯一性,将cell中的“<1>、<n>”替换成“-”，不然js报错
                                var inputId = (item.cell+"-"+row.id).replace("<1>","-").replace("<n>","-");
                                html += "<div class='col-md-12' style='text-align:center;padding: 0'>";
                                //如果订单列表有，则选中，用于翻页后单选框选中
                                if(orderTableObj.data["media"+row.id] && orderTableObj.data["media"+row.id].priceColumn == item.cell){
                                    html += "<input id='"+inputId+"' checked data-value='" + item.cellValue + "' data-id='" + row.id + "' onclick='mediaRadioCheckedFun("+row.id+",\""+row.name+"\",\""+item.cell+"\", this)' type='radio' value='" + item.id + "' class='i-checks' name='mediaPrice" + row.id + "' />";
                                }else{
                                    html += "<input id='"+inputId+"' data-value='" + item.cellValue + "' data-id='" + row.id + "' onclick='mediaRadioCheckedFun("+row.id+",\""+row.name+"\",\""+item.cell+"\", this)' type='radio' value='" + item.id + "' class='i-checks' name='mediaPrice" + row.id + "' />";
                                }
                                html += "<span>" + item.cellName + "</span>:<span class='text-danger font-bold'>￥" + parseFloat(item.cellValue).toFixed(2)+ "</span></div>";
                                html += "<br/>";
                            }
                        }
                    });
                    return html;
                }
            },
            {
                name: 'remarks',
                label: '备注',
                editable: true,
                width: 25,
                align: "center",
                hidden: false
            },
            {
                name: 'discount',
                label: '折扣率',
                width: 20,
                align: "center",
                formatter: function (v, options, row) {
                    return v ? v + " %" : "100%";
                }
            },
            {
                label: "复投率",
                name: 'ftRecord',
                editable: true,
                sortable: true,
                width: 20,
                align: "left",
                hidden: false,
                formatter: function (v, options, row) {
                    var html = "";
                    var three = 0;
                    var six = 0;
                    var year = 0;
                    if(row.ftRecord){
                        three = row.ftRecord.three || 0;
                        six = row.ftRecord.six || 0;
                        year = row.ftRecord.year || 0;
                    }
                    html += "<div class='col-md-12' style='text-align:center;padding: 0'><span>近三月</span>:<span class='text-danger font-bold'>" + three+ "</span></div><br/>";
                    html += "<div class='col-md-12' style='text-align:center;padding: 0'><span>近半年</span>:<span class='text-danger font-bold'>" + six+ "</span></div><br/>";
                    html += "<div class='col-md-12' style='text-align:center;padding: 0'><span>近一年</span>:<span class='text-danger font-bold'>" + year+ "</span></div>";
                    return html;
                }
            },
            {
                label: "操作",
                width: 30,
                hidden: false,
                formatter: function (v, options, row) {
                    var html = "";
                    html += "<a class='text-success' onclick='mediaArtStatisticsObj.lookStatisticsInfo(" + row.id + ", \""+row.name+"\")'>复投详情&nbsp;</a>";
                    html += "<a class='text-success' onclick='supplierObj.lookSupplierInfo(" + row.id + ", \""+row.name+"\")'>供应商信息</a>";
                    return html;
                }
            },
        ],
        pager: "#pager_medias",
        viewrecords: true,
        caption: "资源列表",
        hidegrid: false,
        loadComplete: function (data) {
            if (getResCode(data))
                return;
            var isPrice;
            $(data.list).each(function (i, item) {
                if (item.type == 'price') isPrice = true;
            });
            if (isPrice) {
                $(this).setGridParam().hideCol("priceExt");
            } else {
                $(this).setGridParam().showCol("priceExt");
            }
        },
       /* onSelectRow: function(rowid,status,e){
            var rowData = $("#table_medias").jqGrid('getRowData',rowid);//获取当前行的数据
            lookSupplierPrice(rowData.id, rowData.name);
        },*/
        gridComplete: function () {
            var width = $("#table_medias").closest('.jqGrid_wrapper').width() || $(document).width();
            $('#table_medias').setGridWidth(width);
            $('#table_medias .i-checks').iCheck({
                checkboxClass: 'icheckbox_square-green',
                radioClass: 'iradio_square-green',
            });

            $("#table_medias").find('.i-checks').on('ifClicked', function () {
                $(this).click();
            });
        }
    });
    $tableMedias.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    // $tableMedias.setSelection(4, true);
    var width =  $tableMedias.closest('.jqGrid_wrapper').width();
    $tableMedias.setGridWidth(width);
    // $tableMedias.setGridHeight(360);
}

//含有ID字段的媒体板块，后期若添加新的板块，需要维护
var containIdPlateMap = {
    1:"微信ID",
    12:"抖音ID",
    445:"快手ID"
};

//刷新表格
function reflushTable() {
    /*$("#wechatId").val($("#wechat").val());//微信ID
    $("#ksId").val($("#ks").val());//快手ID*/
    if($("#plateId").val() == 1){
        $("#mediaContentId").val($("#wechat").val());
    }else if($("#plateId").val() == 12){
        $("#mediaContentId").val($("#douyin").val());
    }else if($("#plateId").val() == 445){
        $("#mediaContentId").val($("#ks").val());
    }else {
        $("#mediaContentId").val("");
    }
    $("#mediaName").val($("#mName").val()); //媒体名称
    $("#supplierName").val($("#contactName").val()); //供应商名称
    var json = $("#termForm").serializeJson();
    var condition = {};
    for (var k in json) {
        if (k.indexOf("cell:") > -1) {
            var value = json[k];
            var kk = k.substring(5, k.length);
            var arr = kk.split(":");
            var length = arr.length; //2-代表不是区间的数，3-自定义栏的区间数值（最后一个是min: 开始值，max：结束值）
            var cell = arr[0];
            var type = arr[1];
            var k3 = null;
            if(length == 3){
                k3 = arr[2];//该值为min 或 max
            }
            if(type == "checkbox" && Array.isArray(value)){ //如果是数组，则转成字符串
                value = value.join(",");
            }
            //由于区间的cell相同，所以cellValueStart 和 cellValueEnd需要放在同一个对象中
            if(!condition[cell]){
                if(k3 && k3 == 'min'){
                    condition[cell] = {cell:cell, type:type, cellValueStart:value};
                }else if(k3 && k3 == 'max'){
                    condition[cell] = {cell:cell, type:type, cellValueEnd:value};
                }else{
                    condition[cell] = {cell:cell, type:type, cellValue:value};
                }
            }else{
                if(k3 && k3 == 'min'){
                    condition[cell].cellValueStart = value;
                }else if(k3 && k3 == 'max'){
                    condition[cell].cellValueEnd = value;
                }else{
                    condition[cell].cellValue = value;
                }
            }
            delete json[k];
        }else if(k == 'wechatId'){
            condition[cell] = {cell:'wechatId', type:'text', cellValue:json[k], wechatIdFlag: 1};
            delete json[k];
        }else if(k == 'ksId'){
            condition[cell] = {cell:'ksId', type:'text', cellValue:json[k], ksIdFlag: 1};
            delete json[k];
        }

    }
    var extendArr = new Array();
    if(condition && Object.getOwnPropertyNames(condition).length > 0){
        for(var key in condition){
            extendArr.push(condition[key]);
        }
    }
    json.extendParams = JSON.stringify(extendArr);
    //刷新表格
    $("#table_medias").emptyGridParam(); //清空历史查询数据
    //根据板块ID判断是否显示案例链接还是ID字段
    var colModels = $("#table_medias").jqGrid('getGridParam', 'colModel');
    $(colModels).each(function (j, colModel) {
        if (colModel.name == "mediaContentId") {
            if(containIdPlateMap[json.plateId]){
                if(colModel.name == "mediaContentId"){
                    $("#table_medias").jqGrid('setLabel', colModel.name, containIdPlateMap[json.plateId]);
                    $("#table_medias").setGridParam().showCol(colModel.name);
                }
            }else {
                if(colModel.name == "mediaContentId"){
                    $("#table_medias").setGridParam().hideCol(colModel.name);
                }
            }
        }
    });
    $("#table_medias").jqGrid('setGridParam', {
        postData: json, //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//媒体表格单选框单击事件
function mediaRadioCheckedFun(mediaId, mediaName, cell,t) {
    var isChecked = $(t).is(':checked'); // 判断当前是否被选中
    if(!isChecked){  //如果未选中，则弹出供应商价格窗口
        $("#modalCell").val(cell); //设置媒体价格类型
        $("#mediaPriceRadioId").val($(t).attr("id")); //设置媒体价格单选框ID
        lookSupplierPrice(mediaId,mediaName);
    }else{ //否则，取消单选框，并且将订单列表对应媒体删除
        $(t).iCheck('uncheck');
        if(orderTableObj.data["media" + mediaId]){
            delete orderTableObj.data["media" + mediaId];
            calcuSum(); //计算总价
        }
    }
}

//查看供应商价格
function lookSupplierPrice(mediaId, mediaName) {
    $("#modalMediaId").val(mediaId); //设置媒体ID
    $("#dialogTitle").html("["+mediaName+"]-供应商价格"); //设置模态框标题
    $("#mediaSupplierPriceModal").modal("toggle");
}

//供应商价格表单选框点击事件
function supplierRadioCheckedFun(row, item,subItem){
    $("#checkFlag").val(1); //设置有单选框被选中
    var discount = parseFloat(row.discount) || 100;
    var rate = parseFloat($("#rate").val() || 0);//获取利润值
    //这里的data的属性名采用字符而不是媒体ID数字，可以解决for..in..循环遍历时，按照添加属性顺序遍历，方式订单列表添加顺序错误
    orderTableObj.data["media" + row.mediaId] = {
        //稿件存储数据
        mediaTypeId: row.plateId,
        mediaTypeName: plateMap[row.plateId] || row.plateId,
        mediaId: row.mediaId,
        mediaName: row.mediaName,
        priceType: item.cellName,
        priceColumn: item.cell,
        payAmount: Math.round((parseFloat(item.cellValue) * discount / 100) * 100) / 100,  //成本，保留两位小数
        supplierId: row.supplierId,
        supplierName: row.supplierCompanyName,
        supplierContactor: row.supplierName,
        mediaUserId: row.userId,
        mediaUserName: userMJMap[row.userId] || row.userId,
        unitPrice: Math.round(parseFloat(item.cellValue) * 100) / 100,  //媒体单价，保留两位小数
        num: 1,
        price: Math.round((parseFloat(item.cellValue) * parseFloat(row.discount) / 100  * (1 + rate/100)) * 100) / 100,  //报价，保留两位小数
        //订单列表展示数据
        picPath: row.picPath,
        discount: discount,
        updateDate: row.updateDate,
        costPrice: item.cellValue,  //成本价
    };
    if(subItem && subItem.cellValue && subItem.cellValue != 0){ //如果有副价格，则额外用字段标识
        orderTableObj.data["media" + row.mediaId].subPrice = Math.round(parseFloat(subItem.cellValue) * 100) / 100; //副价格，保留两位小数
        orderTableObj.data["media" + row.mediaId].costPrice = item.cellValue + "(" + subItem.cellName + "￥" + parseFloat(subItem.cellValue).toFixed(2) + ")";
        if(item.cell.indexOf("<1>") > 0){
            orderTableObj.data["media" + row.mediaId].op = "+";
        }else if(item.cell.indexOf("<n>") > 0){
            orderTableObj.data["media" + row.mediaId].op = "*";
        }
        orderTableObj.data["media" + row.mediaId].price =  orderTableObj.data["media" + row.mediaId].price +  orderTableObj.data["media" + row.mediaId].subPrice;
    }
    calcuSum(); //计算总价
    $("#mediaSupplierPriceModal").modal("toggle");
}

//初始化供应商价格表
function initSupplierTable(){
    supplierPriceTableObj.grid = new dataGrid("mediaSupplierPriceTable", supplierPriceTableObj.supplierPriceTable);
    supplierPriceTableObj.grid.loadGrid();
    var supplierPriceNode = $("#mediaSupplierPriceTable");
    supplierPriceNode.setGridWidth(supplierPriceNode.closest('.jqGrid_wrapper').width());
}
//媒体供应商价格表对象
var supplierPriceTableObj = {
    grid: {},
    supplierPriceTable: {
        datatype: "local",
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        sortable: true,
        shrinkToFit: true,
        colNames: ['媒体ID','媒体公司代码','媒体名称','供应商','供应商联系人','价格'],//表头
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'mediaId',
                label: '媒体Id',
                editable: true,
                hidden: true,
                sortable: true,
                sorttype: "int",
                search: true
            },
            {
                name: 'companyCode',
                label: '媒体公司代码',
                editable: true,
                hidden: true,
                sortable: true,
                sorttype: "int",
                search: true
            },
            {
                name: 'mediaName',
                label: '媒体名称',
                editable: true,
                width: 30,
                align: "center",
                cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    //合并单元格，通过ID来判断是否合并
                    return "id='mediaName" + rowId + "'";
                }
            },
            {
                name: 'supplierCompanyName',
                label: '供应商',
                editable: true,
                width: 20,
                align: "center",
               /* cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    //合并单元格，通过ID来判断是否合并
                    return "id='supplierCompanyName" + rawObject.supplierId + "'";
                },*/
                formatter: function (value, grid, rowData) {
                    if(value){
                        value =  value.substr(0,1)+"****";
                    }
                    return value || "";
                }
            },
            {
                name: 'supplierName',
                label: '供应商联系人',
                editable: true,
                width: 20,
                align: "center",
                /*cellattr: function (rowId, tv, rawObject, cm, rowData) {
                    //合并单元格，通过ID来判断是否合并
                    return "id='supplierName" + rawObject.supplierId + "'";
                },*/
                formatter: function (value, grid, rowData) {
                    if(value){
                        value =  value.substr(0,1)+"****";
                    }
                    return value || "";
                }
            },
            {
                label: "价格",
                name: 'prices',
                editable: true,
                sortable: true,
                width: 60,
                align: "left",
                hidden: false,
                formatter: function (v, options, row) {
                    /*var html = "";
                    var val = row.prices;
                    $(val).each(function (i, item) {
                        if (item.cellValue && item.cellValue != 0) {
                            /!*<input data-value='" + item.cellValue + "' data-id='" + row.id + "' onclick='fixContainerObj.clickPriceExt(" + row.id + "," + item.id + ")' type='radio' value='" + item.id + "' class='i-checks' name='price" + row.id + "' />*!/
                            html += "<div class='col-md-12' style='text-align:center;padding: 0'>" +
                                "<input data-value='" + item.cellValue + "' data-id='" + row.id + "' onclick='supplierRadioCheckedFun("+JSON.stringify(row)+","+JSON.stringify(item)+")' type='radio' value='" + item.id + "' class='i-checks' name='supplierPrice" + row.mediaId + "' />" +
                                "<span >" + item.cellName + "</span>:<span class='text-danger font-bold'>￥"+ parseFloat(item.cellValue).toFixed(2)+"</span></div>";
                            if (i++ % 2 == 0) {
                                html += "<br/>";
                            }
                        }
                    });*/
                    return renderSupplierPriceCol(row);
                }
            }
        ],
        // pager: "#mediaSupplierPricePager",
        viewrecords: true,
        // caption: "媒体供应商价格",
        add: true,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        gridComplete: function () {
            var primaryKey = "id";
            supplierPriceTableObj.grid.mergerCell('mediaName', primaryKey);
            /*supplierPriceTableObj.grid.mergerCell('supplierName', primaryKey);
            supplierPriceTableObj.grid.mergerCell('supplierCompanyName', primaryKey);*/

            $('#mediaSupplierPriceTable .i-checks').iCheck({
                checkboxClass: 'icheckbox_square-green',
                radioClass: 'iradio_square-green',
            });

            $("#mediaSupplierPriceTable").find('.i-checks').on('ifClicked', function () {
                $(this).click();
            });
        }
    }
};

//渲染供应商价格列
function renderSupplierPriceCol(row) {
    var html = "";
    var val = row.prices;
    var mainItem = null;
    var subItem = null;
    var itemArr = [];
    var priceCellArr = [];
    $(val).each(function (i, item) {
        if (item.cellValue && item.cellValue != 0) {
            priceCellArr.push(item.cell);
        }
    });
    $(val).each(function (i, item) {
        var cellArr = item.cell.split("->");
        if(item.cell.indexOf("<1>") > 0 || item.cell.indexOf("<n>") > 0){
            mainItem = item;
            itemArr.push(mainItem); //为了保证顺序，主价格入库，附价格不入
        }else if(cellArr.length == 2 && cellArr[1] && (priceCellArr.contains(cellArr[1]+"<1>"+cellArr[0]) || priceCellArr.contains(cellArr[1]+"<n>"+cellArr[0]))){
            subItem = item;
        }else{
            itemArr.push(item);
        }
    });
    var j = 1;
    $(itemArr).each(function (i,item1) {
        if (item1.cellValue && item1.cellValue != 0) {
            html += "<div class='col-md-12' style='text-align:center;padding: 0'>";
            if(item1 == mainItem && subItem && subItem.cellValue && subItem.cellValue != 0){
                var subTitle = "(" + subItem.cellName + "￥" + parseFloat(subItem.cellValue).toFixed(2) + ")";
                html += "<input data-value='" + item1.cellValue + "' data-id='" + row.id + "' onclick='supplierRadioCheckedFun("+JSON.stringify(row)+","+JSON.stringify(item1)+","+JSON.stringify(subItem)+")' type='radio' value='" + item1.id + "' class='i-checks' name='supplierPrice" + row.mediaId + "' />";
                html += "<span >" + (item1.cellName + subTitle) + "</span>:<span class='text-danger font-bold'>￥"+ parseFloat(item1.cellValue).toFixed(2)+"</span></div>";
            }else{
                html += "<input data-value='" + item1.cellValue + "' data-id='" + row.id + "' onclick='supplierRadioCheckedFun("+JSON.stringify(row)+","+JSON.stringify(item1)+")' type='radio' value='" + item1.id + "' class='i-checks' name='supplierPrice" + row.mediaId + "' />";
                html += "<span >" + item1.cellName + "</span>:<span class='text-danger font-bold'>￥"+ parseFloat(item1.cellValue).toFixed(2)+"</span></div>";
            }
            if (j++ % 2 == 0) {
                html += "<br/>";
            }
        }
    })
    return html;
}

//刷新订单表
function reflushOrderTable() {
    orderTableObj.grid = new dataGrid("table_orders", orderTableObj.orderTable);
    orderTableObj.grid.loadGrid();
    var $orderTable = $("#table_orders");
    $orderTable.setGridWidth($orderTable.closest('.jqGrid_wrapper').width());
    //渲染数据
    orderTableObj.list = []; //初始化
    for(var key in orderTableObj.data){
        orderTableObj.data[key].amount = Math.round(orderTableObj.amount * 100) / 100;  //保留两位小数
        orderTableObj.list.push(orderTableObj.data[key]);
    }
    var reader = {
        root: function(obj) { return orderTableObj.list; }
    };

    var scrollTop = $(document).scrollTop(); //由于更新表格会导致页面滚动条重新置顶，所以刷新前先缓存滚动条位置
    $orderTable.emptyGridParam();
    $orderTable.setGridParam({data: orderTableObj.list, reader: reader}).trigger('reloadGrid');
    $(document).scrollTop(scrollTop); //重置页面滚动条位置
}
//订单表格对象
var orderTableObj = {
    grid: {},
    amount: 0, //订单总价
    data: {},  //媒体Map数据
    list: [],  //媒体列表
    orderTable: {
        datatype: "local",
        height: "auto",
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        shrinkToFit: true,
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'mediaId',
                label: '媒体Id',
                editable: true,
                hidden: false,
                width: 20,
                sortable:false
            },
            {
                name: 'picPath',
                label: '图标',
                editable: true,
                hidden: false,
                width: 20,
                sortable:false,
                formatter: function (v, options, row) {
                    if (!v){
                        return '<img class="head-img" src="/img/mrt.png"/>';
                    }else{
                        return '<img class="head-img" src="' + v + '" onerror="src=\'/img/mrt.png\'"/>';
                    }
                }
            },
            {
                name: 'mediaTypeName',
                label: '媒体类型',
                editable: true,
                hidden: false,
                width: 20,
                sortable:false,
            },
            {
                name: 'mediaName',
                label: '媒体名称',
                editable: true,
                hidden: false,
                width: 40,
                sortable:false,
            },
            {
                name: 'mediaUserName',
                label: '责任人',
                editable: true,
                hidden: false,
                width: 20,
                sortable:false,
            },
            {
                name: 'priceType',
                label: '价格类型',
                editable: true,
                hidden: false,
                width: 30,
                sortable:false
            },
            {
                name: 'costPrice',
                label: '成本价',
                editable: true,
                hidden: false,
                width: 40,
                sortable:false,
                formatter: function (v, options, row) {
                    return "￥" + v;
                }
            },
            {
                name: 'discount',
                label: '折扣率',
                editable: true,
                width: 20,
                hidden: false,
                sortable:false,
                formatter: function (v, options, row) {
                    return v ? v + " %" : "100%";
                }
            },
            {
                name: 'updateDate',
                label: '修改日期',
                editable: true,
                hidden: false,
                width: 30,
                sortable:false,
            },
            {
                name: 'num',
                label: '数量&nbsp;<input style="text-align: center;" size="5" id="num"  onkeypress="return inNum(event)" oninput="pushNum(this)" value="1"/>',
                editable: true,
                hidden: false,
                width: 40,
                sortable:false,
                formatter: function (v, options, row) {
                    return "<input style='text-align: center;' oninput='editFun(\"num\", "+row.mediaId+", this)' onkeypress=\"return inNum(event)\" size=\"5\" value=\""+v+"\"/>";
                }
            },
            {
                name: 'price',
                label: '客户报价<input style="text-align: center;" onkeypress="return inNum(event)" id="rate" size="5" maxlength="5" oninput="pushPrice(this)" value="0"/>%利润率',
                editable: true,
                hidden: false,
                width: 60,
                sortable:false,
                formatter: function (v, options, row) {
                    return "<span class=\"text-red font-bold \">￥</span><input class='custPrice' style='text-align: center;width: 100px;' oninput='editFun(\"price\", "+row.mediaId+", this)'  onkeypress=\"return inNum(event)\" size=\"3\" maxlength=\"14\" value='"+v+"'/>";
                }
            },
            {
                label: "操作",
                width: 30,
                hidden: false,
                formatter: function (v, options, row) {
                    return "<a class='text-danger' onclick='delOrderRow("+row.mediaId+");'>删除</a>";
                }
            },
            {
                name: 'amount',
                label: '合计',
                editable: true,
                hidden: false,
                width: 20,
                sortable:false,
                cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    //合并单元格，通过ID来判断是否合并
                    return "id='amount1'";
                },
                formatter: function (v, options, row) {
                    return "￥" + v;
                }
            },
        ],
        viewrecords: true,
        caption: "订单列表",
        add: true,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        gridComplete: function () {
            var primaryKey = "id";
            orderTableObj.grid.mergerCell('amount', primaryKey);
        }
    }
};

//删除订单行数据
function delOrderRow(mediaId) {
    if(orderTableObj.data["media" + mediaId]){
        delete orderTableObj.data["media" + mediaId];
        calcuSum(); //计算总价

        //将资源列表对应的单选框取消掉
        $("#table_medias").find("tr[id='"+mediaId+"']").find("input[name='mediaPrice"+mediaId+"']").iCheck('uncheck');
    }
}

//修改事件
function editFun(type, mediaId, t) {
    if("num" == type){
        updateNum(mediaId, t);
    }else{
        updatePrice(mediaId,t);
    }
}

//推送数量
function pushNum(t) {
    var num = parseInt($(t).val() || 0); //获取当前数量
    var rate = parseFloat($("#rate").val() || 0);//获取利润值
    var sum = 0;
    if(orderTableObj.data && Object.getOwnPropertyNames(orderTableObj.data).length > 0){
        for(var mediaId in orderTableObj.data){
            orderTableObj.data[mediaId].num = num; //重新设置数量
            var price = 0;
            if(orderTableObj.data[mediaId].subPrice && orderTableObj.data[mediaId].op && orderTableObj.data[mediaId].op == "+"){
                price = ((orderTableObj.data[mediaId].unitPrice * num * orderTableObj.data[mediaId].discount / 100 + orderTableObj.data[mediaId].subPrice) * (100+rate)) / 100;
            }else if(orderTableObj.data[mediaId].subPrice && orderTableObj.data[mediaId].op && orderTableObj.data[mediaId].op == "*"){
                price = ((orderTableObj.data[mediaId].unitPrice * num * orderTableObj.data[mediaId].discount / 100 + orderTableObj.data[mediaId].subPrice * num) * (100+rate)) / 100;
            }else {
                price = ((orderTableObj.data[mediaId].unitPrice * num * orderTableObj.data[mediaId].discount / 100) * (100+rate)) / 100;
            }
            orderTableObj.data[mediaId].price = Math.round(price * 100) / 100;//保留两位小数
            sum += orderTableObj.data[mediaId].price; //统计总价
        }
        orderTableObj.amount = sum;
        reflushOrderTable();
    }
}

//推送价格
function pushPrice(t) {
    var rate = parseFloat($(t).val() || 0); //获取当前利润
    var sum = 0;
    if(orderTableObj.data && Object.getOwnPropertyNames(orderTableObj.data).length > 0){
        for(var mediaId in orderTableObj.data){
            var price = 0;
            if(orderTableObj.data[mediaId].subPrice && orderTableObj.data[mediaId].op && orderTableObj.data[mediaId].op == "+"){
                price = ((orderTableObj.data[mediaId].unitPrice * orderTableObj.data[mediaId].num * orderTableObj.data[mediaId].discount / 100 + orderTableObj.data[mediaId].subPrice) * (100+rate)) / 100;
            }else if(orderTableObj.data[mediaId].subPrice && orderTableObj.data[mediaId].op && orderTableObj.data[mediaId].op == "*"){
                price = ((orderTableObj.data[mediaId].unitPrice * orderTableObj.data[mediaId].num * orderTableObj.data[mediaId].discount / 100 + orderTableObj.data[mediaId].subPrice * orderTableObj.data[mediaId].num) * (100+rate)) / 100;
            }else {
                price = ((orderTableObj.data[mediaId].unitPrice * orderTableObj.data[mediaId].num * orderTableObj.data[mediaId].discount / 100) * (100+rate)) / 100;
            }
            orderTableObj.data[mediaId].price = Math.round(price * 100) / 100; //保留两位小数
            sum += orderTableObj.data[mediaId].price; //统计总价
        }
        orderTableObj.amount = sum;
        reflushOrderTable();
    }
}

//更新数量
function updateNum(mediaId, t) {
    var currentNum = parseInt($(t).val() || 0); //获取当前数量
    var rate = parseFloat($("#rate").val() || 0);//获取利润值
    var key = "media" + mediaId; //生成key
    orderTableObj.data[key].num = currentNum; //重新设置数量
    var price = 0;
    if(orderTableObj.data[key].subPrice && orderTableObj.data[key].op && orderTableObj.data[key].op == "+"){
        price = ((orderTableObj.data[key].unitPrice * currentNum * orderTableObj.data[key].discount / 100 + orderTableObj.data[key].subPrice) * (100+rate)) / 100;
    }else if(orderTableObj.data[key].subPrice && orderTableObj.data[key].op && orderTableObj.data[key].op == "*"){
        price = ((orderTableObj.data[key].unitPrice * currentNum * orderTableObj.data[key].discount / 100 + orderTableObj.data[key].subPrice * currentNum) * (100+rate)) / 100;
    }else {
        price = ((orderTableObj.data[key].unitPrice * currentNum * orderTableObj.data[key].discount / 100) * (100+rate)) / 100;
    }
    orderTableObj.data[key].price = Math.round(price * 100) / 100; //保留两位小数

    //实时更新当前行价格
    $(t).closest("tr").find(".custPrice").val(orderTableObj.data[key].price);

    calcuSum("rowInput"); //计算总价
}

//更新价格
function updatePrice(mediaId,t) {
    var key = "media" + mediaId; //生成key
    var currentPrice = Math.round((parseFloat($(t).val() || 0)) * 100) / 100;
    orderTableObj.data[key].price = currentPrice;
    //计算单价
    orderTableObj.data[key].unitPrice = (currentPrice / orderTableObj.data[key].discount * 100 / orderTableObj.data[key].num).toFixed(2);
    calcuSum("rowInput"); //计算总价
}

//计算订单总价
function calcuSum(opType) {
    if(orderTableObj.data && Object.getOwnPropertyNames(orderTableObj.data).length > 0){
        var sum = 0;
        for(var mediaId in orderTableObj.data){
            sum += orderTableObj.data[mediaId].price; //统计总价
        }
        orderTableObj.amount = sum;
    }
    //判断是否有值 并且 修改行数据的"数量 或 客户报价"，不重新渲染表格，直接修改元素
    if(opType && opType == "rowInput"){
        $("#table_orders").find("tr[id='1']").find("td[id='amount1']").text("￥"+orderTableObj.amount);
    }else {
        reflushOrderTable();
    }
}

//验证订单信息
function checkOrderInfo() {
    var flag = true; //默认通过
    //校验利润率
    var rate = $("#rate").val();
    if(!rate || rate == '0' || parseFloat(rate) == 0){
        $("#rate").css("border","1px solid red");
        $("#rate").focus();
        swal("请输入利润率");
        return false;
    }
    //订单列表
    var errorInfo = [];
    if(orderTableObj.data && Object.getOwnPropertyNames(orderTableObj.data).length > 0){
        for(var mediaId in orderTableObj.data){
            var price = orderTableObj.data[mediaId].price;
            var num = orderTableObj.data[mediaId].num;
            if(!num || num == 0){
                errorInfo.push("<span style=\"color: brown;font-size: 16px;\">媒体ID【"+orderTableObj.data[mediaId].mediaId+"】数量必须输入</span>")
            }else if(!price || price == 0){
                errorInfo.push("<span style=\"color: brown;font-size: 16px;\">媒体ID【"+orderTableObj.data[mediaId].mediaId+"】价格必须输入</span>")
            }
        }
    }else{
        swal("请选择媒体");
        flag = false;
    }
    if(errorInfo.length > 0){
        flag = false;
        var text = errorInfo.join("<br/>");
        swal({
            title: "很抱歉，订单列表存在问题",
            text: text,
            type: "warning",
            html: true
        });
    }
    return flag;
}

//获取请求参数
function getRequertParam(resultKey) {
    var json = {};
    for(var mediaId in orderTableObj.data){
        for(var key in orderTableObj.data[mediaId]){
            //排除仅用于订单表显示的属性
            if(key != 'picPath' && key != 'discount' && key != 'updateDate' && key != 'costPrice' && key != 'amount'){
                if (json[key]) {
                    if (!json[key].push) {
                        json[key] = [json[key]];
                    }
                    json[key].push(orderTableObj.data[mediaId][key]);
                } else {
                    json[key] = orderTableObj.data[mediaId][key];
                }
            }
        }
    }
    var result = {};
    result[resultKey] = JSON.stringify(json);
    return result;
}

//提交订单
function save() {
    if (checkOrderInfo()) {
        layer.msg("系统处理中，请稍候。");
        startModal("#saveBtn");
        var data = getRequertParam("param"); //获取请求参数
        requestData(data,"/order","post","json",true,function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                orderTableObj.data = {};//清空订单列表
                $("#rate").val(0); //初始化利率
                reflushTable();
                reflushOrderTable(); //刷新订单表
                page("/order/getById/" + data.data.orderId, "媒体下单-订单详情");
            } else {
                if (getResCode(data)) {
                    return;
                }else{
                    swal({
                        title: "很抱歉，操作失败！",
                        text: data.msg,
                        type: "error",
                        html: true
                    });
                }
            }
        })
    }
}

//媒体方案导出
function exportData() {
    if (checkOrderInfo()) {
        layer.msg("系统处理中，请稍候。");
        startModal("#exportButton");
        var data = getRequertParam("params"); //获取请求参数
        requestData(data,"/order/dataExport","post","json",true,function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                if(data.data.file){
                    window.location.href = data.data.file;
                }else if (data.data.message) {
                    layer.msg(data.data.message, {time: 1000, shade: [0.7, '#393D49']});
                } else {
                    layer.msg(data.msg);
                }
            } else {
                if (getResCode(data)) {
                    return;
                }else{
                    swal({
                        title: "很抱歉，操作失败！",
                        text: data.msg,
                        type: "error",
                        html: true
                    });
                }
            }
        })
    }
}

//供应商信息-针对于供应商扩展字段，排除价格信息
var supplierObj = {
    grid: {},
    supplierInfoTable: {
        datatype: "local",
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        sortable: true,
        shrinkToFit: true,
        // colNames: ['媒体ID','媒体公司代码','媒体名称','供应商','供应商联系人','价格','是否拷贝','拷贝备注','操作'],//表头
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'mediaId',
                label: '媒体Id',
                editable: true,
                hidden: true,
                sortable: true,
                sorttype: "int",
                search: true
            },
            {
                name: 'mediaName',
                label: '媒体名称',
                editable: true,
                width: 40,
                align: "center",
                cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    //合并单元格，通过ID来判断是否合并
                    return "id='mediaName" + rowId + "'";
                }
            },
            {
                name: 'supplierCompanyName',
                label: '供应商',
                editable: true,
                width: 30,
                align: "center",
                cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    //合并单元格，通过ID来判断是否合并
                    return "id='supplierCompanyName" + rawObject.supplierId + "'";
                },
                formatter: function (value, grid, rowData) {
                    return "******";
                }
            },
            {
                name: 'supplierName',
                label: '供应商联系人',
                editable: true,
                width: 30,
                align: "center",
                cellattr: function (rowId, tv, rawObject, cm, rowData) {
                    //合并单元格，通过ID来判断是否合并
                    return "id='supplierName" + rawObject.supplierId + "'";
                },
                formatter: function (value, grid, rowData) {
                    return "******";
                }
            },
            {
                label: "价格",
                name: 'prices',
                editable: true,
                sortable: true,
                width: 50,
                align: "left",
                hidden: false,
                formatter: function (v, options, row) {
                    var html = "";
                    var val = row.prices;
                    var j = 1;
                    $(val).each(function (i, item) {
                        if (item.cellType == "price" && item.cellValue && item.cellValue != 0) {
                            /*<input data-value='" + item.cellValue + "' data-id='" + row.id + "' onclick='fixContainerObj.clickPriceExt(" + row.id + "," + item.id + ")' type='radio' value='" + item.id + "' class='i-checks' name='price" + row.id + "' />*/
                            html += "<div class='col-md-12' style='text-align:center;padding: 0'><span >" + item.cellName + "</span>:<span class='text-danger font-bold'>￥"+ parseFloat(item.cellValue).toFixed(2)+"</span></div>";
                            if (j++ % 1 == 0) {
                                html += "<br/>";
                            }
                        }
                    });
                    return html;
                }
            },
            {
                label: "扩展字段",
                name: 'extendFields',
                editable: true,
                sortable: true,
                width: 70,
                align: "left",
                hidden: false,
                formatter: function (v, options, row) {
                    var html = "";
                    var j = 1;
                    $(row.prices).each(function (i, item) {
                        if (item.cellType && item.cellType != 'price') {
                            var text = "无";
                            if(item.cellValue){
                                if(item.cellType == 'select' || item.cellType == 'radio' || item.cellType == 'checkbox'){
                                    text = item.cellValueText;
                                }else{
                                    text = item.cellValue;
                                }
                            }
                            if(item.cellType == 'link' && "无" != text){
                                html += "<div class='col-md-6' style='text-align:left;padding: 0;padding-right: 5px;'><span style='float:left' >" + item.cellName + ":</span><a class='text-success' style='float:left' target='_blank' href='"+text+"' title='"+text+"'>进入链接</a></div>";
                            }else{
                                html += "<div class='col-md-6' style='text-align:left;padding: 0;padding-right: 5px;'><span style='float:left' >" + item.cellName + ":</span><span class='text-danger' style='float:left'  >" + text + "</span></div>";
                            }
                            if (j++ % 2 == 0) {
                                html += "<br/>";
                            }
                        }
                    });
                    return html;
                }
            },
        ],
        viewrecords: true,
        add: true,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        gridComplete: function () {
            var primaryKey = "id";
            supplierObj.grid.mergerCell('mediaName', primaryKey);
            supplierObj.grid.mergerCell('supplierName', primaryKey);
            supplierObj.grid.mergerCell('supplierCompanyName', primaryKey);
        }
    },
    lookSupplierInfo: function (mediaId, mediaName) {
        $("#modalMediaId1").val(mediaId); //设置媒体ID
        $("#dialogTitle1").html("["+mediaName+"]-供应商信息"); //设置模态框标题
        $("#mediaSupplierPriceModal1").modal("toggle");
    },
    initSupplierTable: function () {
        supplierObj.grid = new dataGrid("mediaSupplierPriceTable1", supplierObj.supplierInfoTable);
        supplierObj.grid.loadGrid();
        var supplierPriceNode1 = $("#mediaSupplierPriceTable1");
        supplierPriceNode1.setGridWidth(supplierPriceNode1.closest('.jqGrid_wrapper').width());
    }
}

//复投统计
var mediaArtStatisticsObj = {
    lookStatisticsInfo: function (mediaId, mediaName) {
        statisticsFTModal.loadConfig({mediaId:mediaId, mediaName: mediaName}); //加载用户配置
        $("#statisticsFTModal").modal("toggle");
    }
}
