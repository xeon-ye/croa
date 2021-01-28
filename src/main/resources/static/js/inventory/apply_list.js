var userArray=[];
//参数缓存对象
var configObj={
    htmlFlag:2,//物品使用页面标志
    //采购领用按钮Id
    applyElem:"saveApplyBtn",
    layerObj:{},//物品采购表单对象
    typeData:{},
    warehouseList:{},//仓库数据（id,name）
    warehouseData:{},//仓库数据（data）
    tempData:{
        type:[],//产品分类缓存数据
        goods:{},//产品缓存数据
        applyCode:[],//领用编码缓存
        repairCode:[],//物品报修编码缓存
        scrapCode:[],//物品报废编码缓存
        returnCode:[],//物品归还编码缓存
        users:[],//用户数据
    },
    //产品分类缓存数据
    listGoodsTypeData:function () {
        if(configObj.tempData.type && configObj.tempData.type.length<=0){
            requestData(null,"/goodsType/loadGoodsTypeInfo","post","json",false,function (data) {
                configObj.tempData.type = data;
                for (var i =0;i<data.data.list.length;i++){
                    var id = data.data.list[i].id;
                    var name = data.data.list[i].name;
                    configObj.typeData[id]=name;
                }
            });
        }
    },
    //产品缓存数据
    listGoodsData: function (typeId) {
        if(configObj.tempData.goods[typeId]==null || configObj.tempData.goods[typeId]=="" || configObj.tempData.goods[typeId]==undefined){
            requestData({parentId:typeId},"/goodsType/loadGoodsTypeByParentId","post","json",false,function (data) {
                configObj.tempData.goods[typeId] = data;
            });
        }
    },
    listWarehouseData:function(){
        if(configObj.isEmptyObject(configObj.warehouseList)){
            commonObj.requestData(null,"/warehouse/warehouseList","get","json",false,function (data) {
                configObj.warehouseData=data;
                for (var i =0;i<data.list.length;i++){
                    var id = data.list[i].id;
                    var name = data.list[i].name;
                    configObj.warehouseList[id]=name;
                }
            })
        }
    },
    loadUserData:function () {
        if(configObj.tempData.users.length<=0){
            requestData({companyCode:user.companyCode},"/propose/listByForum","post","json",false,function (data) {
                configObj.tempData.users=data;
            });
        }
    },
    isEmptyObject:function (obj) {
        var flag = true;
        for(var attr in obj){
            flag=false;//obj有值则不用向后台查询产品分类数据
        }
        return flag;
    }
}

$(function () {
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    //加载tab页
    layui.use('element', function(){
        var element = layui.element;
        element.on('tab(tabFilter)', function(data){
            $("#tabIndex").val(data.index);
            commonObj.tabChange(data.index);
        });
    });

    //领用flag=0:驳回,编辑1:查看,2:审核
    if (getQueryString("flag") != null && getQueryString("flag") != '' && getQueryString("flag") != undefined) {
        var flag = getQueryString("flag");
        var id = getQueryString("id");
        if (flag == 0) {
            applyObj.view(id, 0);
        } else if (flag == 1) {
            applyObj.view(id, 1);
        } else if (flag == 2) {
            applyObj.view(id, 2);
        }
    }

    //报修repairFlag=0:驳回,编辑1:查看,2:审核
    if (getQueryString("repairFlag") != null && getQueryString("repairFlag") != '' && getQueryString("repairFlag") != undefined) {
        var repairFlag = getQueryString("repairFlag");
        var id = getQueryString("id");
        if(repairFlag==0){
            productUsedObj.viewRepair(id,0);
        }else if(repairFlag==1){
            productUsedObj.viewRepair(id,1);
        }else if(repairFlag==2){
            productUsedObj.viewRepair(id,2);
        }
    }

    //报废scrapFlag=0:驳回,编辑1:查看,2:审核
    if (getQueryString("scrapFlag") != null && getQueryString("scrapFlag") != '' && getQueryString("scrapFlag") != undefined) {
        var scrapFlag = getQueryString("scrapFlag");
        var id = getQueryString("id");
        if(scrapFlag==0){
            productUsedObj.viewScrap(id,0);
        }else if(scrapFlag==1){
            productUsedObj.viewScrap(id,1);
        }else if(scrapFlag==2){
            productUsedObj.viewScrap(id,2);
        }
    }

    //归还returnFlag=0:驳回,编辑1:查看,2:审核
    if (getQueryString("returnFlag") != null && getQueryString("returnFlag") != '' && getQueryString("returnFlag") != undefined) {
        var returnFlag = getQueryString("returnFlag");
        var id = getQueryString("id");
        if(returnFlag==0){
            productUsedObj.viewReturn(id,0);
        }else if(returnFlag==1){
            productUsedObj.viewReturn(id,1);
        }else if(returnFlag==2){
            productUsedObj.viewReturn(id,2);
        }
    }

    //领用明细添加事件
    $(document).on('click',"#btn_addCheck",function (data) {
        applyObj.addCheck(configObj.layerObj);
    });

    layui.use('form', function(){
        var $ = layui.jquery, form = layui.form;
        form.on('select(typeQc)', function(data){
            refreshTableData();
            form.render();
        });
        form.on('select(stateQc)', function(data){
            refreshTableData();
            form.render();
        });
        form.on('select(stateQc2)', function(data){
            refreshTableData();
            form.render();
        });
    });

    //加载产品分类进入缓存里
    configObj.listGoodsTypeData();

    //初始化页面
    commonObj.initPage();
});

//根据tabIndex刷新表格数据
function refreshTableData() {
    var index = $("#tabIndex").val();
    if(index==0){
        //刷新物品领用数据
        applyObj.initPagerPlugin();
    }else{
        //刷新物品使用数据
        productUsedObj.initPagerPlugin();
    }
}

//多个页面使用的方法或者数据
var commonObj = {
    //后台请求方法
    requestData: function (data, url, requestType,dataType,async,callBackFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    //Tab切换处理事件
    tabChange: function (index) {
        $(".tabContent").css("display","none");
        if(index == 0){
            //我领用的
            $("#applyTab").css("display","block");
            applyObj.cleanApplyData();
            applyObj.initPagerPlugin();
            layui.use('laydate', function () {
                var createTime = layui.laydate;
                createTime.render({
                    elem: '#createTimeQc',
                    format: 'yyyy-MM-dd',
                    istime: true,
                    istoday: true,// 是否显示今天
                    isclear: true, // 是否显示清空
                });
            });
        }else if(index == 1){
            //我使用的
            $("#productUsedTab").css("display","block");
            productUsedObj.cleanUsedData();
            //加载产品分类数据
            applyObj.loadGoodsType(null,null,"#typeQc");
            productUsedObj.initPagerPlugin();
        }
    },
    //分页插件使用
    pagerPlus: function (config,callback,type) {
        layui.use('laypage', function(){
            var laypage = layui.laypage;
            //执行一个laypage实例
            laypage.render({
                elem: config.elem //注意，这里的 test1 是 ID，不用加 # 号
                ,count: config.count || 0, //数据总数，从服务端得到
                layout: ['count','prev','page','next','refresh','limit','skip'],
                hash: true,
                limits: config.limits || [10, 20, 50, 100],
                limit: config.limit || 10,
                jump: function (obj, first) {
                    if(config.bubbleType && config.bubbleType == 'bubble'){
                        applyMeetingRoomObj.clearBubble();//切换Tab气泡弹窗隐藏
                    }
                    config.param = config.param || {};
                    config.param.size = obj.limit;
                    config.param.page = obj.curr;
                    commonObj.requestData(config.param, config.url, "post", "json", true, function (data) {
                        if(callback){
                            if(config.target){
                                callback(data,config.target);
                            }else {
                                callback(data,type);
                            }
                        }
                    });
                }
            });
        });
    },
    initPage: function () {
        commonObj.tabChange(0);
    }
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

//声明我领用的序号
var orderNumber = 0;

//物品领用对象
var applyObj = {
    getTotalUrl: "/apply/getPageCount",
    applyManageListUrl: "/apply/getPageInfo",
    pagerPluginElem: 'applyPager',
    modalIndex: null,//物品领用弹出层下标
    approveIndex:null,//物品领用审批详情下标
    limit: 10,
    callback: function (data) {
        $("#applyManageList").empty();
        var html = "";
        if (data && data.list.length > 0) {
            $.each(data.list, function (i, apply) {
                var stateTips = "";
                switch (apply.state) {
                    case -1 :
                        stateTips = "<span style='color: red'>审核驳回</span>";
                        break;
                    case 0 :
                        stateTips = "已保存";
                        break;
                    case 1 :
                        stateTips = "已完成";
                        break;
                    case 2 :
                        stateTips = "审核通过";
                        break;
                    case 4 :
                        stateTips = "<span style='color: red'>部长审核</span>";
                        break;
                    case 6 :
                        stateTips = "<span>部分出库</span>";
                        break;
                    case 7 :
                        stateTips = "<span style='color: #70C5FB'>已出库</span>";
                        break;
                    case 14 :
                        stateTips = "<span style='color: red;'>行政部长审核</span>";
                        break;
                    default :
                        break;
                }
                //根据分页插件计算序号
                var pageSize = data.pageSize;
                var pageNum = data.pageNum;
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                html += "<tr>" +
                    "<td style=\"text-align: center\">" + (pageIndex + 1) + "</td>" +
                    "<td style=\"text-align: center\" class='ellipsisContent'><span style='color: #00a2ff;cursor: pointer' onclick=\"applyObj.view('" + apply.id + "',1)\">" + apply.applyCode + "</span></td>" +
                    "<td style=\"text-align: center\" class='ellipsisContent' title='"+apply.title+"'>" + apply.title + "</td>" +
                    "<td style=\"text-align: center\">" + apply.userName + "</td>" +
                    "<td style=\"text-align: center\">" + apply.createTime + "</td>" +
                    "<td style=\"text-align: center\"><span title='"+apply.purpose+"' class='overFlowDiv' style='width:150px;display:inline-block;'>" + apply.purpose + "</span></td>" +
                    "<td style=\"text-align: center\">" + stateTips + "</td>" +
                    "<td style=\"text-align: center\">";
                if (apply.taskId != null && apply.taskId != "") {
                    html += "<a href='javascript:void(0)' class='submitButton'  onclick=\"applyObj.showHistory('" + apply.id + "')\">审核详情</a>";
                }
                if ((apply.state == 0 || apply.state == -1) && apply.userId == user.id) {
                    html += "<a href='javascript:void(0)' class='submitButton' onclick=\"applyObj.view('" + apply.id + "',0)\">编辑</a>";
                }
                if ((apply.state == 0 || apply.state == -1) && apply.userId == user.id) {
                    html += "<a href='javascript:void(0)' class='submitButton' onclick=\"applyObj.delApply('" + apply.id + "')\">删除</a>";
                }
                html += "</td></tr>";
            });
            //拼接内容
            $("#applyManageList").append(html);
        }
    },
    initPagerPlugin: function () {
        var formData = $("#applyForm").serializeJson();
        //初始化分页组件
        commonObj.requestData(formData, applyObj.getTotalUrl, "get", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    param: formData,
                    elem: applyObj.pagerPluginElem,
                    count: data.data.total,
                    url: applyObj.applyManageListUrl,
                },applyObj.callback);
            }
        });
    },
    //清除采购查询数据
    cleanApplyData: function () {
        $("#nameQc").val("");
        $("#stateQc").val("");
    },
    closeModal:function () {
        if(applyObj.modalIndex){
            layer.close(applyObj.modalIndex);
        }else {
            layer.closeAll();
        }
    },
    //关闭审核详情弹出层
    closeApproveDiv:function(){
        if(applyObj.approveIndex){
            layer.close(applyObj.approveIndex);
        }
    },
    //自动生成领用编号
    loadApplyCode:function(t){
        if(configObj.tempData.applyCode.length==0){
            requestData(null,"/apply/getApplyCode","post","json",true,function (data) {
                if (data.code==200){
                    $(t[0]).find("input[name='applyCode']").val(data.data.code);
                    configObj.tempData.applyCode=data.data.code;
                }else{
                    layer.open({
                        title:"提示",
                        content:data.msg,
                    });
                }
            })
        }else{
            $(t[0]).find("input[name='applyCode']").val(configObj.tempData.applyCode);
        }
    },
    //添加领用明细方法
    addCheck: function (layero,code, type, goodsId,stockAmount,amount,price,totalMoney,unit, handleId, time, flag) {
        orderNumber = orderNumber + 1;
        <!-- 判断形参内是否有实际的值 -->
        if (type == null || typeof(type) == undefined) {
            type = "";
        }
        if (goodsId == null || typeof(goodsId) == undefined) {
            goodsId = "";
        }
        if (code == null || typeof(code) == undefined) {
            code = "";
        }
        if (unit == null || typeof(unit) == undefined) {
            unit = "";
        }
        if (stockAmount == null || typeof(stockAmount) == undefined) {
            stockAmount = "";
        }
        if (amount == null || typeof(amount) == undefined) {
            amount = "";
        }
        if (price == null || typeof(price) == undefined) {
            price = "";
        }
        if (totalMoney == null || typeof(totalMoney) == undefined) {
            totalMoney = "";
        }
        if (handleId == null || typeof(handleId) == undefined) {
            handleId = "";
        }
        if (time == null || typeof(time) == undefined) {
            time = "";
        }
        //产生随机数，作为加载用户的标志no（或者以时分秒为标志）
        var no = Math.round(Math.random() * 10000);
        <!-- 添加领用明细记录 -->
        var strContent = "<tr class='checkNum" + orderNumber + "' id='tr"+no+"'>\n" +
            "<td align=\"center\">\n" +
            "" + orderNumber + "\n" +
            "</td>\n" +
            "<td style='text-align: center;' class=\"layui-form\">\n" +
            "<select lay-search lay-filter=\"typeId\" class=\"type form-control\" data-id='"+no+"' id='type_" + no + "' name=\"type\">" +
            "</select>\n" +
            "</td>\n" +
            "<td align=\"center\">\n" +
            "<select class=\"goodsId form-control\" name=\"goodsId\" lay-search lay-filter=\"goodsId\" data-value data-id='"+no+"' id='goodsId_"+no+"'></select>\n" +
            "</td>\n" +
            "<td align=\"center\">\n" +
            "<input class=\"stockAmount form-control\" id=\"stockAmount\" value='" + stockAmount + "' readonly/>\n" +
            "</td>\n" +
            "<td align=\"center\">\n" +
            "<input class=\"unit form-control\" name=\"unit\" value='" + unit + "' />\n" +
            "</td>\n" +
            "<td align=\"center\">\n" +
            "<input class=\"amount form-control\" name=\"amount\" onkeyup=\"value=value.replace(/[^\\d]/g,'')\" onchange=\"applyObj.calculatePrice();\" type=\"number\" value='" + amount + "'/>\n" +
            "</td>\n" +
            "<td align=\"center\">\n" +
            "<input class=\"price form-control\" name=\"price\" readonly type=\"number\" value='" + price + "' onkeyup=\"value=value.replace(/[^\\d\\.\\-]/g,'')\" onchange=\"applyObj.calculatePrice();\"/>\n" +
            "</td>\n" +
            "<td align=\"center\">\n" +
            "<input class=\"totalMoney form-control\" name=\"totalMoney\" readonly type=\"number\" value='" + totalMoney + "'/>\n" +
            "</td>\n" +
            "<td align=\"center\">\n" +
            "<select class=\"handleId form-control\" lay-search name=\"handleId\" id='userId_" + no + "'></select>\n" +
            "</td>\n" +
            "<td align=\"center\">\n" +
            "<input class=\"returnDate form-control\" id='date_" + no + "' name=\"returnDate\" value='" + time + "' />\n" +
            "</td>\n";
        if ($("#flag").val() == -2 || $("#flag").val() == -3) {
            strContent += "<td align='center'>" +
                "<button type=\"button\" id=\"btn_removeCheck_" + orderNumber + "\" name=\"btn_removeCheck\" class=\"btn btn-white btn-xs\" onclick=\"applyObj.removeCheck(this);\">" +
                "<i class=\"glyphicon glyphicon-minus\"></i>" +
                "</button>" +
                "</td>\n";
        } else {
            strContent += "<td align='center'>" +
                "<button type=\"button\" id=\"btn_removeCheck_" + orderNumber + "\" name=\"btn_removeCheck\" class=\"btn btn-white btn-xs\" disabled='disabled'>" +
                "<i class=\"glyphicon glyphicon-minus\"></i>" +
                "</button>" +
                "</td>\n";
        }
        strContent += "</tr>";
        var checkTable = $(layero[0]).find("table[id='checkTable']");
        checkTable.append(strContent);
        var userElem = $(layero[0]).find("select[id='userId_"+no+"']")[0];
        //加载领用人
        applyObj.loadUser(handleId,userElem);
        var typeElem = $(layero[0]).find("select[id='type_"+no+"']")[0];
        //加载产品分类数据
        applyObj.loadGoodsType(type,goodsId,typeElem,no);
        var dateElem = $(layero[0]).find("input[id='date_"+no+"']")[0];
        layui.use('laydate', function () {
            var returnDate = layui.laydate;
            returnDate.render({
                elem: dateElem,
                format: 'yyyy-MM-dd',
                istime: true,
                istoday: true,// 是否显示今天
                isclear: true, // 是否显示清空
            });
        });
    },
    calculatePrice:function(){
        //单价
        var price = $(".price");
        //数量
        var amount = $(".amount");
        //单行金额
        var total = $(".totalMoney");
        var sum = 0;
        //遍历每一行的数据，算出每一行的总价
        for (var i = 0; i < price.length; i++) {
            var money = price.eq(i).val();
            var number = amount.eq(i).val();
            if (isNaN(money)) {
                swal("输入的单价不合法");
                price.eq(i).val(0);
                money = 0;
            }
            var totalMoney = parseFloat((money * number).toFixed(2));
            total.eq(i).val(totalMoney);
            sum += totalMoney;
        }
        $(configObj.layerObj[0]).find("input[name='money']").val(sum.toFixed(2));
    },
    //删除领用明细记录
    removeCheck: function (t) {
        $(t).parent().parent().remove();
        //移除采购明细时，序号重新计算
        var tableLength = $("#checkTable tr").length;
        for (var i = 1; i <= tableLength; i++) {
            $(configObj.layerObj[0]).find("table[id='checkTable']").find("tr:eq("+(i-1)+") td:first").text(i);
        }
        //重置添加采购明细的序号(全部删除，重新添加时重置序号)
        if(tableLength==0){
            orderNumber=0;
        }else{
            orderNumber=tableLength;
        }
        applyObj.calculatePrice();
    },
    //加载产品分类信息
    loadGoodsType: function(t,goodsId,elem,no){//t产品分类id，goodsId产品id
        var data = configObj.tempData.type;
        var html="<option value=''>请选择</option>";
        $(elem).empty();
        layui.use(["form"], function () {
            for (var i = 0; i < data.data.list.length; i++) {
                var goodsType = data.data.list[i];
                var selected = t == goodsType.id ? "selected=selected" : "";
                html += "<option value='" + goodsType.id + "' " + selected + ">" + goodsType.name + "</option>";
            }
            $(elem).append(html);
            layui.form.render();
            layui.form.on("select(typeId)", function (typeData) {
                var no = typeData.elem.attributes["data-id"].nodeValue;
                // $("#goodsId_"+no).empty();
                $(configObj.layerObj[0]).find("select[id='goodsId_"+no+"']").empty();
                //产品分类id
                var typeId = typeData.value;
                //向缓冲区中查询该产品分类id是否存在，存在则直接调用数据，否则向后台获取产品信息
                if(typeId==""){
                   layui.form.render();
                   return;
                }
                configObj.listGoodsData(typeId);
                var goodsData = configObj.tempData.goods[typeId];
                var goodsHtml="<option value=''>请选择</option>";
                for (var j = 0; j < goodsData.data.list.length; j++) {
                    var goods = goodsData.data.list[j];
                    var selected = t == goods.id ? "selected=selected" : "";
                    goodsHtml += "<option value='" + goods.id + "' " + selected + ">" + goods.name +"</option>";
                }
                $("#goodsId_"+no).append(goodsHtml);
                //储存父级id
                $("#goodsId_"+no).attr("data-value",typeId);
                layui.form.render();
            });
            layui.form.on("select(goodsId)", function (typeData) {
                var no = typeData.elem.attributes["data-id"].nodeValue;
                //产品id
                var goodsId = typeData.value;
                var wareId=$(configObj.layerObj[0]).find("select[name='wareId']").val();
                requestData({id: goodsId,wareId:wareId}, "/goodsType/getStockDataById", "get", "json", true, function (data) {
                var goods=data.data.entity;
                if (goods != null) {
                    $("#tr" + no + " .stockAmount").val(goods["stockAmount"]);
                    $("#tr" + no + " .unit").val(goods["unit"]);
                    $("#tr" + no + " .price").val(goods["price"]);
                } else {
                    $("#tr" + no + " .stockAmount").val(0);
                    $("#tr" + no + " .unit").val("");
                    $("#tr" + no + " .price").val(0);
                }
                applyObj.calculatePrice();
                });
            });
            // 如果产品分类id不为null,则给产品赋值
            if(t!=null && t!=""){
                if(goodsType!=null &&  goodsType!=""){
                    applyObj.loadGoods(t,goodsId,"#goodsId_"+no);
                }
            }
        });
    },
    loadGoods:function(parentId,t,elem){//parentId产品分类id,t产品id
        layui.use(["form"], function () {
            var html="";
            //向缓冲区中查询该产品分类id是否存在，存在则直接调用数据，否则向后台获取产品信息
            $(elem).empty();
            configObj.listGoodsData(parentId);
            var data = configObj.tempData.goods[parentId];
            for (var i = 0; i < data.data.list.length; i++) {
                var goods = data.data.list[i];
                var selected = t == goods.id ? "selected=selected" : "";
                html += "<option value='" + goods.id + "' " + selected + ">" + goods.name + "</option>";
            }
            $(elem).append(html);
            layui.form.render();
        });
    },
    //根据公司代码加载用户数据
    loadUser: function (t, elem) {
        $(elem).empty();
        layui.use(["form"], function () {
            configObj.loadUserData();
            var data = configObj.tempData.users;
            var html = "<option value=''>请选择</option>";
            if (data != null) {
                for (var i = 0; i < data.data.number; i++) {
                    var id = data.data.list[i].id;
                    var name = data.data.list[i].name;
                    var selected = id == t ? "selected=selected" : "";
                    html += "<option value='" + id + "' " + selected + ">" + name +"</option>";
                }
            }
            $(elem).append(html);
            layui.form.render();
        });
    },
    //加载仓库
    loadWarehouse:function (t,id) {
        layui.use(["form"],function () {
            configObj.listWarehouseData();
            var data = configObj.warehouseData;
            $(t).empty();
            var html="";
            $(data.list).each(function (i, d) {
                var selected = id == d.id ? "selected=selected" : "";
                html+="<option value='" + d.id + "' " + selected + ">" + d.name + "</option>";
            });
            $(t).append(html);
            layui.form.render();
        });
    },
    //领用添加disabled属性
    addApplyDisableStyle: function () {
        $("#addApplyForm [name='applyCode']").attr("disabled", true);
        $("#addApplyForm [name='title']").attr("disabled", true);
        $("#addApplyForm [name='userName']").attr("disabled", true);
        $("#addApplyForm [name='purpose']").attr("disabled", true);
        $("#addApplyForm [name='money']").attr("disabled", true);
        $("#addApplyForm [name='wareId']").attr("disabled", true);
        $("#addApplyForm [name='createTime']").attr("disabled", true);
        $("#addApplyForm [name='desc']").attr("disabled", true);
        $("#addApplyForm [name='deptName']").attr("disabled", true);
        $("#addApplyForm [name='file']").attr("disabled", true);
        $("#addApplyForm [name='code']").attr("disabled", true);
        $("#addApplyForm [name='type']").attr("disabled", true);
        $("#addApplyForm [name='goodsId']").attr("disabled", true);
        $("#addApplyForm [name='unit']").attr("disabled", true);
        $("#addApplyForm [name='amount']").attr("disabled", true);
        $("#addApplyForm [name='handleId']").attr("disabled", true);
        $("#addApplyForm [name='returnDate']").attr("disabled", true);
        $("#addApplyForm [name='remark']").attr("disabled", true);
        $("#addApplyForm #btn_addCheck").attr("disabled", true);
        $("#addApplyForm .layui-form-select").addClass("layui-select-disabled");
        $("#addApplyForm .layui-select-title input").addClass("layui-disabled");
    },
    //领用移除disabled属性
    removeApplyDisableStyle: function () {
        $("#addApplyForm [name='applyCode']").attr("disabled", false);
        $("#addApplyForm [name='title']").attr("disabled", false);
        $("#addApplyForm [name='userName']").attr("disabled", false);
        $("#addApplyForm [name='purpose']").attr("disabled", false);
        $("#addApplyForm [name='money']").attr("disabled", false);
        $("#addApplyForm [name='wareId']").attr("disabled", false);
        $("#addApplyForm [name='createTime']").attr("disabled", false);
        $("#addApplyForm [name='desc']").attr("disabled", false);
        $("#addApplyForm [name='deptName']").attr("disabled", false);
        $("#addApplyForm [name='file']").attr("disabled", false);
        $("#addApplyForm [name='code']").attr("disabled", false);
        $("#addApplyForm [name='type']").attr("disabled", false);
        $("#addApplyForm [name='goodsId']").attr("disabled", false);
        $("#addApplyForm [name='unit']").attr("disabled", false);
        $("#addApplyForm [name='amount']").attr("disabled", false);
        $("#addApplyForm [name='handleId']").attr("disabled", false);
        $("#addApplyForm [name='returnDate']").attr("disabled", false);
        $("#addApplyForm [name='remark']").attr("disabled", false);
        $("#addApplyForm #btn_addCheck").attr("disabled", false);
        $("#addApplyForm .layui-form-select").removeClass("layui-select-disabled");
        $("#addApplyForm .layui-select-title input").removeClass("layui-disabled");
    },
    showSaveApply:function(){
        configObj.layerObj={};
        //移除disabled属性
        applyObj.removeApplyDisableStyle();
        applyObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 90000,
            content: $("#saveApplyModal").html(),
            btn: [],
            area: ['94%', '80%'],
            offset:['60px','54px'],
            closeBtn: 0,
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj = layero;
                //新增
                orderNumber = 0;
                $(layero[0]).find("input[id='taskId']").val("");
                $(layero[0]).find("input[id='itemId']").val("");
                $(layero[0]).find("a[id='affixDiv']").attr("href","");
                $(layero[0]).find("a[id='affixDiv']").html("");
                //隐藏附件div
                $(".affixDiv").empty();
                $(".affixDiv").hide();
                $(layero[0]).find("input[name='userId']").val(user.id);
                $(layero[0]).find("input[name='userName']").val(user.name);
                $(layero[0]).find("input[name='createTime']").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                //清空详情展示区
                $("#checkTable tbody").html("");
                $("#flag").val(-3);//flag=-2和-3允许增加删除详情
                $(".save").show();
                $(".edit").hide();
                $(".approve").hide();
                $("#showHistory1").hide();
                $(".licence").hide();
                $(".viewFooter").hide();
                $(".editFooter").show();
                //加载领用编号
                applyObj.loadApplyCode(layero);
                //加载仓库
                applyObj.loadWarehouse($(layero[0]).find("select[name='wareId']"),null);
                //添加一行空的物品采购记录
                applyObj.addCheck(layero);
                layui.use('form', function(){
                    var form = layui.form;
                    //下拉列表改变事件
                    form.on('select(wareId)', function(data){
                        applyObj.flushTable();
                    });
                    form.render();
                });

            }
        });
    },
    //flag=0:驳回,编辑1:查看,2:审核
    view:function(id,flag){
        configObj.layerObj={};
        applyObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 90000,
            content: $("#saveApplyModal").html(),
            btn: [],
            area: ['94%', '80%'],
            offset:['60px','54px'],
            closeBtn: 0,
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                orderNumber = 0;
                //清空物品领用详情展示区
                $(layero[0]).find("table[id='checkTable'] tbody").html("");
                configObj.layerObj = layero;
                if (flag == 0) {
                    $("#showHistory1").hide();
                    $(".licence").hide();
                    $(".viewFooter").hide();
                    $(".editFooter").show();
                    $(".approve").hide();
                    $("#flag").val(-3);//flag=-2和-3允许增加删除详情
                    $("#btn_addCheck").removeAttrs("disabled");
                    $("#btn_removeCheck").removeAttrs("disabled");
                    $(".save").hide();
                    $(".edit").show();
                } else if (flag == 1) {//单击查看页面
                    $("#showHistory1").hide();
                    $(".licence").show();
                    $(".viewFooter").show();
                    $(".editFooter").hide();
                    $(".approve").hide();
                    $(".save").hide();
                    $(".edit").show();
                    $("#flag").val(0);//增加按钮不允许操作
                } else if (flag == 2) {//审核
                    $("#showHistory1").data("id", id);
                    $(".licence").show();
                    $(".viewFooter").show();
                    $(".editFooter").hide();
                    $(".approve").show();
                    $(".save").hide();
                    $(".edit").hide();
                    $("#flag").val(0);
                }
                requestData({id:id},"/apply/getByWareIdAndApplyId","post","json",true,function (data) {
                    if(data.code==200){
                        if(data && Object.getOwnPropertyNames(data.data.entity).length>0){
                            var entity = data.data.entity;
                            for(var attr in entity){
                                $(layero[0]).find("input[name='"+attr+"'][type!='radio']").val(entity[attr]);
                                if (attr == "affixName") {
                                    $("#affixLink").empty();
                                    $("#affixLink").show();
                                    $(layero[0]).find("div[id='affixDiv']").empty();
                                    $(layero[0]).find("div[id='affixDiv']").show();
                                    if(data.data.entity[attr] === "") continue;
                                    var affixName = data.data.entity[attr].split(',');
                                    var affixLink = data.data.entity["affixLink"].split(",");
                                    if (affixName.length>0 && affixLink.length>0){
                                        var html = "";
                                        for (var i=0 ; i<affixName.length ; i++) {

                                            var filePath = affixLink[i];
                                            var fileName = affixName[i];
                                            html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                                            html += "<a href=" + filePath + " target=_blank  download='"+fileName+"'>下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase() ;
                                            var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                                            var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                                            if(fileName.indexOf(".")>-1){
                                                var str=fileExt + '|';
                                                if(strFilter.indexOf(str)>-1){//是图片
                                                    html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                                }else{
                                                    if(fileExtArray.contains(fileExt)){
                                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                                    }
                                                }
                                            }else {
                                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                            }
                                        }
                                        $(layero[0]).find("div[id='affixDiv']").append(html);
                                    }
                                }
                                if(attr=="wareId"){
                                    applyObj.loadWarehouse($(layero[0]).find("select[name='wareId']"),entity[attr]);
                                }
                            }
                            var details = entity.details;
                            if(details.length>0){
                                for (var i =0;i<details.length;i++){
                                    var code=details[i].code==undefined?"":details[i].code;
                                    var type=details[i].type;
                                    var goodsId=details[i].goodsId;
                                    var unit=details[i].unit==undefined?"":details[i].unit;
                                    var stockAmount=details[i].stockAmount;
                                    var amount=details[i].amount;
                                    var price=details[i].price;
                                    var totalMoney=details[i].totalMoney;
                                    var userId=details[i].userId;
                                    var returnTime =details[i].returnTime==undefined?"":details[i].returnTime ;
                                    applyObj.addCheck(layero,code,type,goodsId,stockAmount,amount,price,totalMoney,unit,userId,returnTime,1);
                                }
                            }
                            if (flag == 0) {
                                //编辑移除disabled属性
                                applyObj.removeApplyDisableStyle();
                            } else if (flag == 1) {
                                //查看添加disabled属性
                                applyObj.addApplyDisableStyle();
                            } else if (flag == 2) {
                                //审核添加disabled属性
                                applyObj.addApplyDisableStyle();
                                $("#addApplyForm #remark").attr("disabled", false);
                            }
                        }
                    }
                });
            }
        });
    },
    //物品领用添加明细限制
    checkApplyEmpty: function (elem) {
        var title = $(elem[0]).find("input[name='title']").val();
        var purpose = $(elem[0]).find("input[name='purpose']").val();
        var type = $("#addApplyForm .type");
        var name = $("#addApplyForm .goodsId");
        var amount = $("#addApplyForm .amount");
        var price = $("#addApplyForm .price");//产品单价
        var handleId = $("#addApplyForm .handleId");
        var returnDate = $("#addApplyForm .returnDate");
        if(title =="" || title==null){
            layer.open({
                title: "提示",
                content: "标题不能为空"
            });
            return false;
        }
        if(purpose =="" || purpose==null){
            layer.open({
                title: "提示",
                content: "产品用途不能空"
            });
            return false;
        }
        var tableLength = $("#checkTable tr").length;
        if(tableLength==0){
            layer.open({
                title: "提示",
                content: "请至少添加一条领用明细信息"
            });
            return false;
        }
        for (var i = 0; i < type.length; i++) {
            var typeVal = type.eq(i).val();
            var nameVal = name.eq(i).val();
            var amountVal = amount.eq(i).val();
            var priceVal = price.eq(i).val();//单价
            var handleIdVal = handleId.eq(i).val();
            if (typeVal == "" || typeVal == null) {
                layer.open({
                    title: "提示",
                    content: "请选择产品分类"
                });
                return false;
            }
            if (nameVal == "" || nameVal == null) {
                layer.open({
                    title: "提示",
                    content: "请选择产品名称"
                });
                return false;
            }
            if (amountVal == "" || amountVal == null) {
                layer.open({
                    title: "提示",
                    content: "请输入数量"
                });
                return false;
            }
            if (amountVal <= 0) {
                layer.open({
                    title: "提示",
                    content: "数量必须大于0"
                });
                amount.eq(i).val(0);
                return false;
            }
            //单价校验
            if (priceVal == null || priceVal == "") {
                layer.open({
                    title: "提示",
                    content: "单价不能为空"
                });
                return false;
            }
            if (priceVal == 0) {
                layer.open({
                    title: "提示",
                    content: "单价不能为0"
                });
                return false;
            }
            if (handleIdVal == "" || handleIdVal == null) {
                layer.open({
                    title: "提示",
                    content: "请选择使用人"
                });
                return false;
            }
        }
        return true;
    },
    flushTable:function(){
         var $trElem=$(configObj.layerObj[0]).find("table[id='checkTable'] tbody tr");
         if($trElem.length>0){
             $($trElem).each(function (i,object) {
                 var goodsId=$(object).find("td:nth-child(3) select").val();
                 var wareId=$(configObj.layerObj[0]).find("select[name='wareId']").val();
                 requestData({id:goodsId,wareId:wareId},"/goods/getStockAmountById","post","json",false,function (data) {
                     $(object).find("td:nth-child(4) input").val(data.data.amount);
                 });
             });
         }
    },
    //领用新增编辑功能
    submitApply: function (t, url,state) {
        var formElem = $(t).closest(".stockModalCommon");
        if (applyObj.checkApplyEmpty(formElem)) {
            layer.confirm("是否保存物品领用信息？", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                $(formElem[0]).find("div[id='editBtn']").css("pointer-events","none");
                $(formElem[0]).find("div[id='editApplyBtn']").css("pointer-events","none");
                $(formElem[0]).find("div[id='addBtn']").css("pointer-events","none");
                $(formElem[0]).find("div[id='saveApplyBtn']").css("pointer-events","none");
                $(formElem[0]).find("input[name='state']").val(state);
                var formData = new FormData(formElem.find("form")[0]);//序列化当前表单，并传出file类型
                $.ajax({
                    type: "post",
                    url: url,
                    data: formData,
                    dataType: "json",
                    async: true,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            $(formElem[0]).find("div[id='editBtn']").css("pointer-events","auto");
                            $(formElem[0]).find("div[id='editApplyBtn']").css("pointer-events","auto");
                            $(formElem[0]).find("div[id='addBtn']").css("pointer-events","auto");
                            $(formElem[0]).find("div[id='saveApplyBtn']").css("pointer-events","auto");
                            if(state==4){
                                //领用提交清空领用编码缓存
                                configObj.tempData.applyCode=[];
                                applyObj.closeModal();
                            }else{
                                //领用新增保存操作
                                // if(t.id==configObj.applyElem){}
                                configObj.tempData.applyCode=[];
                                applyObj.closeModal();
                                //新增跳转编辑页面(防止添加重复数据)
                                applyObj.view(data.data.entity.id,0);
                            }
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            applyObj.initPagerPlugin();
                        } else if(data.code==1002){
                            swal({
                               title:"提示",
                               text:data.msg
                            });
                            applyObj.closeModal();
                            applyObj.initPagerPlugin();
                        }
                    },error:function (data) {
                        Ladda.stopAll();
                    }
                });
            }, function () {
                return;
            })
        }
    },
    delApply:function(id){
        layer.confirm("是否需要删除该记录？", {
            btn: ["确定", "取消"],
            shade: false,
        }, function (index) {
            layer.close(index);
            requestData({id:id},"/apply/delApply","post","json",true,function (data) {
                if(data.code==200){
                    layer.msg(data.data.message,{time:1000,icon:6});
                    applyObj.initPagerPlugin();
                }else{
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                }
            });
        });
    },
    showHistory: function (id) {
        //process详见IProcess
        $("#historyModal").modal('toggle');
        $.ajax({
            type: "post",
            url: "/process/history",
            data: {dataId: id, process: 28},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    $("#history").empty();
                    if (data.data.data != null) {
                        var html = "";
                        html += "<div style='position: relative;z-index: 10;'>" +
                            "<div class='form-control'>" +
                            "<div class='col-sm-3 text-center'>审核节点</div>" +
                            "<div class='col-sm-3 text-center'>操作人</div>" +
                            "<div class='col-sm-3 text-center'>操作详情</div>" +
                            "<div class='col-sm-3 text-center'>操作时间</div></div>";
                        for (var i = 0; i < data.data.data.length; i++) {
                            html += "<div class='form-control'>" +
                                "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>" +
                                "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>" +
                                "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>" +
                                "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>" +
                                "</div>";
                        }
                        html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=28&t=" + new Date().getTime() + "' style='width: 135%; margin-left: -175px; margin-top: -120px;margin-bottom: -100px;'/></div>";
                        $("#history").append(html);
                    }
                } else {
                    if (getResCode(data))
                        return;
                }
            }, error: function (data) {
                Ladda.stopAll();
            }
        });
    },
    showHistory2:function(){
        applyObj.approveIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 99999,
            content: $("#showHistoryModal").html(),
            btn: [],
            area: ['66%', '60%'],
            offset:['120px','250px'],
            closeBtn: 0,
            btn: [],
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                var id = $(configObj.layerObj[0]).find("input[name='id']").val();
                $.ajax({
                    type: "post",
                    url: "/process/history",
                    data: {dataId: id, process: 28},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            $("#history").empty();
                            if (data.data.data != null) {
                                var html = "";
                                html += "<div style='position: relative;z-index: 10;'>" +
                                    "<div class='form-control'>" +
                                    "<div class='col-sm-3 text-center'>审核节点</div>" +
                                    "<div class='col-sm-3 text-center'>操作人</div>" +
                                    "<div class='col-sm-3 text-center'>操作详情</div>" +
                                    "<div class='col-sm-3 text-center'>操作时间</div></div>";
                                for (var i = 0; i < data.data.data.length; i++) {
                                    html += "<div class='form-control'>" +
                                        "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>" +
                                        "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>" +
                                        "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>" +
                                        "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>" +
                                        "</div>";
                                }
                                html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=28&t=" + new Date().getTime() + "' style='margin-bottom: -100px;margin-top: -90px;margin-left: -175px'/></div>";
                                $(layero[0]).find("div[id='showHistoryDiv']").append(html);
                            }
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    }, error: function (data) {
                        Ladda.stopAll();
                    }
                });
            }
        });
    },
    //审核通过
    approve: function (t) {
        var taskId = $(configObj.layerObj[0]).find("input[name='taskId']").val();
        var remark = $(configObj.layerObj[0]).find("input[name='remark']").val();
        approveTask(taskId, 1, t.id, remark);
    },
    //审核驳回
    reject: function (t) {
        var taskId = $(configObj.layerObj[0]).find("input[name='taskId']").val();
        var remark = $(configObj.layerObj[0]).find("input[name='remark']").val();
        approveTask(taskId, 0, t.id, remark);
    },
}

//物品使用对象
var productUsedObj = {
    modalIndex:null,
    approveIndex:null,
    getTotalUrl: "/apply/getUserApplyCount",
    productUsedListUrl: "/apply/getUserApplyData",
    saveRepairUrl:"/repair/saveRepair",
    saveScrapUrl:"/scrap/saveScrap",
    saveReturnUrl:"/return/saveReturn",
    pagerPluginElem: 'productUsedPager',
    callback: function (data) {
        var html = "";
        if (data && data.list.length > 0) {
            $(data.list).each(function (i, obj) {
                var processStateTips = "";//流程状态
                var stateTips = "";//流程类型
                if(obj.recordState!=undefined && obj.recordState!=-9){
                    switch (obj.recordState) {
                        case 0 :
                            processStateTips = "已保存";
                            break;
                        case -1 :
                            processStateTips = "<span class='text-red'>已驳回</span>";
                            break;
                        case 1 :
                            processStateTips = "已完成";
                            break;
                        case 15 :
                            processStateTips = "审核中";
                            break;
                        case 29 :
                            processStateTips = "审核中";
                            break;
                        default :
                            break;
                    }
                }else{
                    switch (obj.state) {
                        case -1 :
                            processStateTips = "使用中";
                            break;
                        case 1 :
                            processStateTips = "已报修";
                            break;
                        case 2 :
                            processStateTips = "已报废";
                            break;
                        case 3 :
                            processStateTips = "已归还";
                            break;
                        default :
                            break;
                    }
                }
                switch (obj.state) {
                    case 1 :
                        stateTips = "报修";
                        break;
                    case 2 :
                        stateTips = "<span class='text-red'>报废</span>";
                        break;
                    case 3 :
                        stateTips = "归还";
                        break;
                    default:
                        break;
                }
                //根据分页插件计算序号
                var pageSize = data.pageSize;
                var pageNum = data.pageNum;
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                html += "<tr>" +
                    "<td style=\"text-align: center\">" + (pageIndex + 1) + "</td>" +
                    "<td style=\"text-align: center\"><div style='color: #00a2ff;cursor: pointer;' class='overFlowDiv' title='"+obj.code+"' onclick=\"productUsedObj.showProductHistory('" + obj.id + "')\">" + obj.code + "</div></td>" +
                    "<td style=\"text-align: center\">" + (configObj.typeData[obj.typeId]==undefined?"":configObj.typeData[obj.typeId]) + "</td>" +
                    "<td style=\"text-align: center\">" + obj.goodsName + "</td>" +
                    "<td style=\"text-align: center\">"+(obj.specs=="" || obj.specs==undefined?"-":obj.specs)+"</td>" +
                    "<td style=\"text-align: center\">"+obj.unit+"</td>" +
                    "<td style=\"text-align: center\">"+obj.number+"</td>" +
                    "<td style=\"text-align: center\">"+obj.userName+"</td>" +
                    "<td style=\"text-align: center\">"+(obj.returnTime==null?"-":obj.returnTime)+"</td>" +
                    "<td style=\"text-align: center\">" + stateTips + "</td>" +
                    "<td style=\"text-align: center\">" + processStateTips + "</td>" +
                    "<td style=\"text-align: center\">";
                //出库已完成的流程并且不能是报废(出库库存只能由使用人处理)
                if ((obj.state == -1 || obj.recordState==1) && obj.state!=2 && obj.userId==user.id) {
                    html += "<a href='javascript:void(0)' class='submitButton' onclick=\"productUsedObj.repair('" + obj.id + "')\">报修</a>"+
                        "<a href='javascript:void(0)' class='submitButton' onclick=\"productUsedObj.return('" + obj.id + "')\">归还</a>"+
                        "<a href='javascript:void(0)' class='submitButton' onclick=\"productUsedObj.scrap('" + obj.id + "')\">报废</a>";
                }
                //只能操作自己发起的流程（报修）
                if (obj.state == 1 && obj.operateUserId==user.id) {
                    if (obj.taskId != null && obj.taskId != "" && obj.recordState!=1) {
                        html += "<a href='javascript:void(0)' class='submitButton'  onclick=\"productUsedObj.showHistory('" + obj.foreignId + "',29)\">审核详情</a>";
                    }
                    if(obj.recordState==0 || obj.recordState==-1){
                        html +="<a href='javascript:void(0)' class='submitButton' onclick=\"productUsedObj.viewRepair('" + obj.foreignId + "',0)\">编辑</a>"+
                            "<a href='javascript:void(0)' class='submitButton' onclick=\"productUsedObj.delRepair('" + obj.foreignId + "')\">删除</a>";
                    }
                }
                //只能操作自己发起的流程（报废）
                if (obj.state == 2 && obj.operateUserId==user.id) {
                    if (obj.taskId != null && obj.taskId != "") {
                        html += "<a href='javascript:void(0)' class='submitButton'  onclick=\"productUsedObj.showHistory('" + obj.foreignId + "',30)\">审核详情</a>";
                    }
                    if(obj.recordState==0 || obj.recordState==-1){
                        html +="<a href='javascript:void(0)' class='submitButton' onclick=\"productUsedObj.viewScrap('" + obj.foreignId + "',0)\">编辑</a>"+
                            "<a href='javascript:void(0)' class='submitButton' onclick=\"productUsedObj.delScrap('" + obj.foreignId + "')\">删除</a>";
                    }
                }
                //只能操作自己发起的流程（归还）
                if (obj.state == 3 && obj.operateUserId==user.id) {
                    if (obj.taskId != null && obj.taskId != "" && obj.recordState!=1) {
                        html += "<a href='javascript:void(0)' class='submitButton'  onclick=\"productUsedObj.showHistory('" + obj.foreignId + "',31)\">审核详情</a>";
                    }
                    if(obj.recordState==0 || obj.recordState==-1){
                        html +="<a href='javascript:void(0)' class='submitButton' onclick=\"productUsedObj.viewReturn('" + obj.foreignId + "',0)\">编辑</a>"+
                            "<a href='javascript:void(0)' class='submitButton' onclick=\"productUsedObj.delReturn('" + obj.foreignId + "')\">删除</a>";
                    }
                }
                html += "</td></tr>";
            });
            //拼接内容
            $("#productUsedList").html(html);
        }else{
            //拼接内容
            $("#productUsedList").html("");
        }
    },
    initPagerPlugin: function () {
        var formData = $("#productUsedForm").serializeJson();
        //初始化分页组件
        commonObj.requestData(formData, productUsedObj.getTotalUrl, "get", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    param: formData,
                    elem: productUsedObj.pagerPluginElem,
                    count: data.data.total,
                    url: productUsedObj.productUsedListUrl,
                },productUsedObj.callback);
            }
        });
    },
    cleanUsedData:function(){
        $("#typeQc").val("");
        $("#stateQc2").val("");
    },
    //关闭弹出窗
    closeModal:function () {
        if(productUsedObj.modalIndex){
            layer.close(productUsedObj.modalIndex);
        }else {
            layer.closeAll();
        }
    },
    //关闭审核详情弹出窗
    closeApproveModal:function () {
        if(productUsedObj.approveIndex){
            layer.close(productUsedObj.approveIndex);
        }else{
            //如果你想关闭最新弹出的层，直接获取layer.index即可
            layer.close(layer.index);
        }
    },
    //自动生成报修编号
    loadRepairCode:function(t){
        if(configObj.tempData.repairCode.length==0){
            requestData(null,"/repair/getRepairCode","post","json",true,function (data) {
                if (data.code==200){
                    $(t[0]).find("input[name='code']").val(data.data.code);
                    configObj.tempData.repairCode=data.data.code;
                }
            })
        }else{
            $(t[0]).find("input[name='code']").val(configObj.tempData.repairCode);
        }
    },
    //自动生成报废编号
    loadScrapCode:function(t){
        if(configObj.tempData.scrapCode.length==0){
            requestData(null,"/scrap/getScrapCode","post","json",true,function (data) {
                if (data.code==200){
                    $(t[0]).find("input[name='code']").val(data.data.code);
                    configObj.tempData.scrapCode=data.data.code;
                }
            })
        }else{
            $(t[0]).find("input[name='code']").val(configObj.tempData.scrapCode);
        }
    },
    //自动生成归还编号
    loadReturnCode:function(t){
        if(configObj.tempData.returnCode.length==0){
            requestData(null,"/return/getReturnCode","post","json",true,function (data) {
                if (data.code==200){
                    $(t[0]).find("input[name='code']").val(data.data.code);
                    configObj.tempData.returnCode=data.data.code;
                }
            })
        }else{
            $(t[0]).find("input[name='code']").val(configObj.tempData.returnCode);
        }
    },
    checkFormEmpty:function(){
        var title = $(configObj.layerObj[0]).find("input[name='title']").val();
        if(title==null || title==""){
            layer.open({
                title:"提示",
                content:"标题不能为空"
            });
            return false;
        }
        return true;
    },
    showHistory: function (id,processId) {
        $("#historyModal").modal('toggle');
        $.ajax({
            type: "post",
            url: "/process/history",
            data: {dataId: id, process: processId},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    $("#history").empty();
                    if (data.data.data != null) {
                        var html = "";
                        html += "<div style='position: relative;z-index: 10;'>" +
                            "<div class='form-control'>" +
                            "<div class='col-sm-3 text-center'>审核节点</div>" +
                            "<div class='col-sm-3 text-center'>操作人</div>" +
                            "<div class='col-sm-3 text-center'>操作详情</div>" +
                            "<div class='col-sm-3 text-center'>操作时间</div></div>";
                        for (var i = 0; i < data.data.data.length; i++) {
                            html += "<div class='form-control'>" +
                                "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>" +
                                "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>" +
                                "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>" +
                                "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>" +
                                "</div>";
                        }
                        html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process="+processId+"&t=" + new Date().getTime() + "' style='width: 135%; margin-left: -175px; margin-top: -120px;margin-bottom: -100px;'/></div>";
                        $("#history").append(html);
                    }
                } else {
                    if (getResCode(data))
                        return;
                }
            }, error: function (data) {
                Ladda.stopAll();
            }
        });
    },
    showHistory2:function(processId){
        productUsedObj.approveIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 99999,
            content: $("#showHistoryModal2").html(),
            btn: [],
            area: ['66%', '60%'],
            offset:['120px','250px'],
            closeBtn: 0,
            btn: [],
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                var id = $(configObj.layerObj[0]).find("input[name='id']").val();
                $.ajax({
                    type: "post",
                    url: "/process/history",
                    data: {dataId: id, process: processId},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            $("#history").empty();
                            if (data.data.data != null) {
                                var html = "";
                                html += "<div style='position: relative;z-index: 10;'>" +
                                    "<div class='form-control'>" +
                                    "<div class='col-sm-3 text-center'>审核节点</div>" +
                                    "<div class='col-sm-3 text-center'>操作人</div>" +
                                    "<div class='col-sm-3 text-center'>操作详情</div>" +
                                    "<div class='col-sm-3 text-center'>操作时间</div></div>";
                                for (var i = 0; i < data.data.data.length; i++) {
                                    html += "<div class='form-control'>" +
                                        "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>" +
                                        "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>" +
                                        "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>" +
                                        "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>" +
                                        "</div>";
                                }
                                html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process="+processId+"&t=" + new Date().getTime() + "' style='margin-bottom: -100px;margin-top: -90px;margin-left: -175px'/></div>";
                                $(layero[0]).find("div[id='showHistoryDiv2']").append(html);
                            }
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    }, error: function (data) {
                        Ladda.stopAll();
                    }
                });
            }
        });
    },
    //展示库存流程记录
    showProductHistory:function(id){
        configObj.layerObj={};
        productUsedObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 99900,
            content: $("#viewInventoryDetailsModal").html(),
            btn: [],
            area: ['86%', '75%'],
            offset:['100px','120px'],
            closeBtn: 0,
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                commonObj.requestData({id:id},"/goods/queryByInventoryId","post","json",true,function (data) {
                    var recordList = data.data.list;
                    if(recordList!=null && recordList.length>0){
                        for(var attr in recordList[0]){
                            $(layero[0]).find("[name="+attr+"]").val(recordList[0][attr]);
                            if(attr=="warehouseId"){
                                configObj.listWarehouseData();
                                var warehouseData = configObj.warehouseList;
                                $(layero[0]).find("[name='warehouseId']").val(warehouseData[recordList[0][attr]]);
                                $(layero[0]).find("[name='code']").attr("title",recordList[0]["code"]);
                            }
                            if(attr=="createTime"){
                                var createTime = recordList[0][attr];
                                $(layero[0]).find("[name='createTime']").val(new Date(createTime).format("yyyy-MM-dd hh:mm:ss"));
                            }
                            if(attr=="typeId"){
                                var typeName = configObj.typeData[recordList[0][attr]];
                                $(layero[0]).find("[name='typeName']").text(typeName);
                            }
                            if(attr=="goodsName"){
                                var goodsName = recordList[0][attr];
                                $(layero[0]).find("[name='goodsName']").text(goodsName);
                            }
                            if(attr=="specs"){
                                var specs = recordList[0][attr]==""?"-":recordList[0][attr];
                                $(layero[0]).find("[name='specs']").text(specs);
                            }
                            if(attr=="unit"){
                                var unit = recordList[0][attr];
                                $(layero[0]).find("[name='unit']").text(unit);
                            }
                            if(attr=="number"){
                                var number = recordList[0][attr];
                                $(layero[0]).find("[name='number']").text(number);
                            }
                            if(attr=="price"){
                                var price = recordList[0][attr];
                                $(layero[0]).find("[name='price']").text(price.toFixed(2));
                            }
                        }
                        if(recordList[0]["recordState"]!=null && recordList[0]["recordState"]!=undefined){
                            var html="";
                            $(recordList).each(function (i,item) {
                                var stateTips="";
                                var time = new Date(item.operateTime).format("yyyy-MM-dd hh:mm:ss");
                                var updateTime = new Date(item.updateTime).format("yyyy-MM-dd hh:mm:ss");
                                var userName = item.userName==undefined?"":item.userName;
                                var type = item.type;//流程类型1报修2报废3归还
                                var recordState = item.recordState;//库存记录状态1已报修2报修中
                                if(type==1){
                                    if(recordState==0 || recordState==-1){
                                        stateTips="报修保存";
                                    }else if(recordState==29 || recordState==15){
                                        stateTips="报修中";
                                    }else if(recordState==1 || recordState==5){
                                        stateTips="<span class='divStyle'>已报修</span>";
                                    }
                                }else if(type==2){
                                    if(recordState==0 || recordState==-1){
                                        stateTips="报废保存";
                                    }else if(recordState==29 || recordState==15){
                                        stateTips="报废中";
                                    }else if(recordState==1 || recordState==5){
                                        stateTips="<span class='divStyle'>已报废</span>";
                                    }
                                }else if(type==3){
                                    if(recordState==0 || recordState==-1){
                                        stateTips="归还保存";
                                    }else if(recordState==29 || recordState==15){
                                        stateTips="归还中";
                                    }else if(recordState==1 || recordState==5){
                                        stateTips="<span class='divStyle'>已归还</span>";
                                    }
                                }
                                html+="<tr>" +
                                    "<td style='text-align: center'>"+(i+1)+"</td>"+
                                    "<td style='text-align: center'>"+item.code+"</td>"+
                                    "<td style='text-align: center'>"+item.operateName+"</td>"+
                                    "<td style='text-align: center'>"+time+"</td>"+
                                    "<td style='text-align: center'>"+stateTips+"</td>"+
                                    "</tr>";
                                if(item.state==-1 && i==recordList.length-1){
                                    html+="<tr>" +
                                        "<td style='text-align: center'>"+(i+2)+"</td>"+
                                        "<td style='text-align: center'>"+item.code+"</td>"+
                                        "<td style='text-align: center'>"+userName+"(使用人)</td>"+
                                        "<td style='text-align: center'>"+updateTime+"</td>"+
                                        "<td style='text-align: center'>使用中</td>"+
                                        "</tr>";
                                }
                            });
                            $(layero[0]).find("table[id='showDetailsTable5'] tbody").html(html);
                        }else {
                            var createTime = new Date(recordList[0]["createTime"]).format("yyyy-MM-dd hh:mm:ss");
                            var code = recordList[0]["code"];
                            var userName = recordList[0]["userName"];
                            var state = recordList[0]["state"];
                            var stateSpan="";
                            if(state==0){
                                stateSpan="库存中";
                            }else {
                                stateSpan="使用中";
                            }
                           var inventoryHtml="<tr>" +
                                "<td style='text-align: center'>"+1+"</td>"+
                                "<td style='text-align: center' class='overFlowDiv' title='"+code+"'>"+code+"</td>"+
                                "<td style='text-align: center'>"+userName+"</td>"+
                                "<td style='text-align: center'>"+createTime+"</td>"+
                                "<td style='text-align: center'>"+stateSpan+"</td>"+
                                "</tr>";
                            $(layero[0]).find("table[id='showDetailsTable5'] tbody").html(inventoryHtml);
                        }
                    }else{
                        $(layero[0]).find("table[id='showDetailsTable5'] tbody").html("");
                    }
                });
            }
        });
    },
    //展示物品报修数据
    repair:function (id) {
        configObj.layerObj={};
        productUsedObj.removeDisabledStyle();
        productUsedObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 90000,
            content: $("#saveRepairModal").html(),
            btn: [],
            area: ['80%', '75%'],
            offset:['60px','120px'],
            closeBtn: 0,
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj = layero;
                $(layero[0]).find("input[name='taskId']").val("");
                $(layero[0]).find("input[name='itemId']").val("");
                $(layero[0]).find("input[name='userId']").val(user.id);
                $(layero[0]).find("input[name='userName']").val(user.name);
                $(layero[0]).find("input[name='deptName']").val(user.deptId);
                $(layero[0]).find("input[name='deptName']").val(user.deptName);
                $(layero[0]).find("input[name='createTime']").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                //清空详情展示区
                $("#showDetailsTable tbody").html("");
                $(".save").show();
                $(".edit").hide();
                $(".approve").hide();
                $("#showHistory").hide();
                $(".licence").hide();
                $(".viewFooter").hide();
                $(".editFooter").show();
                $(layero[0]).find("input[name='inventoryId']").val(id);
                requestData({id:id},"/goods/editAjax","post","json",true,function (data) {
                    if (data.code == 200) {
                        var html = "";
                        $(data.data.entity).each(function (i, obj) {
                            html = "<tr>" +
                                "<td style='text-align: center'>"+1+"</td>" +
                                "<td style='text-align: center;' class='overFlowDiv' title='"+obj.code+"'>"+obj.code+"</td>" +
                                "<td style='text-align: center'>"+configObj.typeData[obj.typeId]+"</td>" +
                                "<td style='text-align: center'>"+obj.goodsName+"</td>" +
                                "<td style='text-align: center'>"+(obj.specs==""?"-":obj.specs)+"</td>" +
                                "<td style='text-align: center'>"+obj.unit+"</td>" +
                                "<td style='text-align: center'>"+obj.number+"</td>" +
                                "<td style='text-align: center'>"+(obj.returnTime==null?"无":obj.returnTime)+"</td>" +
                                "</tr>";
                        });
                        productUsedObj.loadRepairCode(layero);
                        $(layero[0]).find("table[id='showDetailsTable'] tbody").append(html);
                    }
                });
            }
        });
    },
    //查看物品报修信息flag：0驳回，1查看，2审核
    viewRepair:function (id,flag) {
        configObj.layerObj={};
        productUsedObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 90000,
            content: $("#saveRepairModal").html(),
            btn: [],
            area: ['80%', '75%'],
            offset:['60px','120px'],
            closeBtn: 0,
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj = layero;
                $(layero[0]).find("input[name='taskId']").val("");
                $(layero[0]).find("input[name='itemId']").val("");
                //清空详情展示区
                if(flag==0){
                    $("#showDetailsTable tbody").html("");
                    $(".save").hide();
                    $(".edit").show();
                    $(".approve").hide();
                    $("#showHistory").hide();
                    $(".licence").hide();
                    $(".viewFooter").hide();
                    $(".editFooter").show();
                }else if(flag==1){
                    $("#showDetailsTable tbody").html("");
                    $(".save").hide();
                    $(".edit").hide();
                    $(".approve").hide();
                    $("#showHistory").hide();
                    $(".licence").hide();
                    $(".viewFooter").show();
                    $(".editFooter").hide();
                }else if(flag==2){
                    $("#showDetailsTable tbody").html("");
                    $(".save").hide();
                    $(".edit").hide();
                    $(".approve").show();
                    $("#showHistory").show();
                    $(".licence").show();
                    $(".viewFooter").show();
                    $(".editFooter").hide();
                }
                requestData({id:id},"/repair/getById","post","json",true,function (data) {
                    if (data.code == 200) {
                        if(data.data.entity!=null){
                            for(var attr in data.data.entity){
                                $(layero[0]).find("input[name="+attr+"][type!='radio']").val(data.data.entity[attr]);
                                if(attr=="remark"){
                                    $(layero[0]).find("[name="+attr+"][type!='radio']").html(data.data.entity[attr]);
                                }
                                if(attr=="inventoryId"){
                                    requestData({id:data.data.entity[attr]},"/goods/editAjax","post","json",false,function (object) {
                                        if (object.code == 200) {
                                            var html = "";
                                            $(object.data.entity).each(function (i, obj) {
                                                html = "<tr>" +
                                                    "<td style='text-align: center'>"+1+"</td>" +
                                                    "<td style='text-align: center;' class='overFlowDiv'>"+obj.code+"</td>" +
                                                    "<td style='text-align: center'>"+configObj.typeData[obj.typeId]+"</td>" +
                                                    "<td style='text-align: center'>"+obj.goodsName+"</td>" +
                                                    "<td style='text-align: center'>"+(obj.specs==""?"-":obj.specs)+"</td>" +
                                                    "<td style='text-align: center'>"+obj.unit+"</td>" +
                                                    "<td style='text-align: center'>"+obj.number+"</td>" +
                                                    "<td style='text-align: center'>"+(obj.returnTime==null?"无":obj.returnTime)+"</td>" +
                                                    "</tr>";
                                            });
                                            $(layero[0]).find("table[id='showDetailsTable'] tbody").append(html);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
                if(flag==0){
                    productUsedObj.removeDisabledStyle();
                }else if(flag==1){
                    productUsedObj.addDisabledStyle();
                }else{
                    productUsedObj.addDisabledStyle();
                }
            }
        });
    },
    //删除报修
    delRepair:function(id){
        layer.confirm("是否删除报修申请",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            requestData({id:id},"/repair/deleteRepair","post","json",true,function (data) {
                if (data.code==200){
                    layer.msg(data.data.message,{icon:6,time:1000});
                    configObj.tempData.repairCode=[];
                    productUsedObj.initPagerPlugin();
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                    productUsedObj.closeModal();
                }
            });
        })

    },
    addDisabledStyle:function () {
        $(configObj.layerObj[0]).find("input[name='title']").attr("disabled",true);
        $(configObj.layerObj[0]).find("[name='remark']").attr("disabled",true);
    },
    removeDisabledStyle:function () {
        $(configObj.layerObj[0]).find("input[name='title']").attr("disabled",false);
        $(configObj.layerObj[0]).find("[name='remark']").attr("disabled",false);
    },
    //报修申请
    submitRepair(t,url,state){//校验表格数据
        if(productUsedObj.checkFormEmpty()){
            layer.confirm("是否报修该产品",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                var formData=$(configObj.layerObj[0]).find("form").serializeForm();
                formData.state=state;
                formData.htmlFlag=configObj.htmlFlag;
                $(configObj.layerObj[0]).find("div[id='updateOperateBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='editOperateBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='saveOperateBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='addOperateBtn']").css("pointer-events","none");
                requestData(formData,url,"post","json",true,function (data) {
                    if (data.code==200){
                        $(configObj.layerObj[0]).find("div[id='updateOperateBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='editOperateBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='saveOperateBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='addOperateBtn']").css("pointer-events","auto");
                        if(state==0){
                            if(productUsedObj.saveRepairUrl==url){
                                configObj.tempData.repairCode=[];
                                productUsedObj.closeModal();
                                productUsedObj.initPagerPlugin();
                                productUsedObj.viewRepair(data.data.entity.id,0);
                            }else{
                                productUsedObj.closeModal();
                                productUsedObj.initPagerPlugin();
                            }
                        }else {
                            productUsedObj.closeModal();
                            productUsedObj.initPagerPlugin();
                            configObj.tempData.repairCode=[];
                        }
                        layer.msg(data.data.message,{icon:6,time:1000});
                    }else if(data.code==1002){
                        swal({
                            title:"提示",
                            text:data.msg
                        });
                        productUsedObj.closeModal();
                    }
                });
            })
        }
    },
    //物品归还
    return:function (id) {
        configObj.layerObj={};
        productUsedObj.removeDisabledStyle();
        productUsedObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 90000,
            content: $("#saveReturnModal").html(),
            btn: [],
            area: ['80%', '75%'],
            offset:['60px','120px'],
            closeBtn: 0,
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj = layero;
                $(layero[0]).find("input[name='userId']").val(user.id);
                $(layero[0]).find("input[name='userName']").val(user.name);
                $(layero[0]).find("input[name='deptName']").val(user.deptId);
                $(layero[0]).find("input[name='deptName']").val(user.deptName);
                $(layero[0]).find("input[name='createTime']").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                //清空详情展示区
                $("#showDetailsTable3 tbody").html("");
                $(".save").show();
                $(".edit").hide();
                $(".approve").hide();
                $("#showHistory3").hide();
                $(".licence").hide();
                $(".viewFooter").hide();
                $(".editFooter").show();
                $(layero[0]).find("input[name='inventoryId']").val(id);
                requestData({id:id},"/goods/editAjax","post","json",true,function (data) {
                    if (data.code == 200) {
                        var html = "";
                        $(data.data.entity).each(function (i, obj) {
                            html = "<tr>" +
                                "<td style='text-align: center'>"+1+"</td>" +
                                "<td style='text-align: center'>"+obj.code+"</td>" +
                                "<td style='text-align: center'>"+configObj.typeData[obj.typeId]+"</td>" +
                                "<td style='text-align: center'>"+obj.goodsName+"</td>" +
                                "<td style='text-align: center'>"+(obj.specs==""?"-":obj.specs)+"</td>" +
                                "<td style='text-align: center'>"+obj.unit+"</td>" +
                                "<td style='text-align: center'>"+obj.number+"</td>" +
                                "<td style='text-align: center'>"+(obj.returnTime==null?"无":obj.returnTime)+"</td>" +
                                "</tr>";
                        });
                        productUsedObj.loadReturnCode(layero);
                        $(layero[0]).find("table[id='showDetailsTable3'] tbody").append(html);
                    }
                });
            }
        });
    },
    //查看物品归还信息flag：0驳回，1查看，2审核
    viewReturn:function (id,flag) {
        configObj.layerObj={};
        productUsedObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 90000,
            content: $("#saveReturnModal").html(),
            btn: [],
            area: ['80%', '75%'],
            offset:['60px','120px'],
            closeBtn: 0,
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj = layero;
                $(layero[0]).find("input[name='taskId']").val("");
                $(layero[0]).find("input[name='itemId']").val("");
                //清空详情展示区
                if(flag==0){
                    $("#showDetailsTable3 tbody").html("");
                    $(".save").hide();
                    $(".edit").show();
                    $(".approve").hide();
                    $("#showHistory3").hide();
                    $(".licence").hide();
                    $(".viewFooter").hide();
                    $(".editFooter").show();
                }else if(flag==1){
                    $("#showDetailsTable3 tbody").html("");
                    $(".save").hide();
                    $(".edit").hide();
                    $(".approve").hide();
                    $("#showHistory3").hide();
                    $(".licence").hide();
                    $(".viewFooter").show();
                    $(".editFooter").hide();
                }else if(flag==2){
                    $("#showDetailsTable2 tbody").html("");
                    $(".save").hide();
                    $(".edit").hide();
                    $(".approve").show();
                    $("#showHistory3").show();
                    $(".licence").show();
                    $(".viewFooter").show();
                    $(".editFooter").hide();
                }
                requestData({id:id},"/return/getById","post","json",true,function (data) {
                    if (data.code == 200) {
                        if(data.data.entity!=null){
                            for(var attr in data.data.entity){
                                $(layero[0]).find("input[name="+attr+"][type!='radio']").val(data.data.entity[attr]);
                                if(attr=="remark"){
                                    $(layero[0]).find("[name="+attr+"][type!='radio']").html(data.data.entity[attr]);
                                }
                                if(attr=="inventoryId"){
                                    requestData({id:data.data.entity[attr]},"/goods/editAjax","post","json",false,function (object) {
                                        if (object.code == 200) {
                                            var html = "";
                                            $(object.data.entity).each(function (i, obj) {
                                                html = "<tr>" +
                                                    "<td style='text-align: center'>"+1+"</td>" +
                                                    "<td style='text-align: center' class='overFlowDiv' title='"+obj.code+"'>"+obj.code+"</td>" +
                                                    "<td style='text-align: center'>"+configObj.typeData[obj.typeId]+"</td>" +
                                                    "<td style='text-align: center'>"+obj.goodsName+"</td>" +
                                                    "<td style='text-align: center'>"+(obj.specs==""?"-":obj.specs)+"</td>" +
                                                    "<td style='text-align: center'>"+obj.unit+"</td>" +
                                                    "<td style='text-align: center'>"+obj.number+"</td>" +
                                                    "<td style='text-align: center'>"+(obj.returnTime==null?"无":obj.returnTime)+"</td>" +
                                                    "</tr>";
                                            });
                                            $(layero[0]).find("table[id='showDetailsTable3'] tbody").append(html);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
                if(flag==0){
                    productUsedObj.removeDisabledStyle();
                }else if(flag==1){
                    productUsedObj.addDisabledStyle();
                }else{
                    productUsedObj.addDisabledStyle();
                }
            }
        });
    },
    //提交归还申请
    submitReturn(t,url,state){//校验表格数据
        if(productUsedObj.checkFormEmpty()){
            layer.confirm("是否归还该产品",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                var formData=$(configObj.layerObj[0]).find("form").serializeForm();
                formData.state=state;
                formData.htmlFlag=configObj.htmlFlag;
                $(configObj.layerObj[0]).find("div[id='updateReturnBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='editReturnBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='saveReturnBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='addReturnBtn']").css("pointer-events","none");
                requestData(formData,url,"post","json",true,function (data) {
                    if (data.code==200){
                        $(configObj.layerObj[0]).find("div[id='updateReturnBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='editReturnBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='saveReturnBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='addReturnBtn']").css("pointer-events","auto");
                        if(state==0){
                            if(productUsedObj.saveReturnUrl==url){
                                configObj.tempData.returnCode=[];
                                productUsedObj.closeModal();
                                productUsedObj.initPagerPlugin();
                                productUsedObj.viewReturn(data.data.entity.id,0);
                            }else{
                                productUsedObj.closeModal();
                                productUsedObj.initPagerPlugin();
                            }
                        }else {
                            productUsedObj.closeModal();
                            productUsedObj.initPagerPlugin();
                            configObj.tempData.returnCode=[];
                        }
                        layer.msg(data.data.message,{icon:6,time:1000});
                    }else if(data.code==1002){
                        swal({
                            title:"提示",
                            text:data.msg
                        });
                        productUsedObj.closeModal();
                    }
                });
            })
        }
    },
    //删除归还申请
    delReturn:function(id){
        layer.confirm("是否删除报废申请",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            requestData({id:id},"/return/delReturn","post","json",true,function (data) {
                if (data.code==200){
                    layer.msg(data.data.message,{icon:6,time:1000});
                    configObj.tempData.scrapCode=[];
                    productUsedObj.initPagerPlugin();
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                    productUsedObj.closeModal();
                }
            });
        })

    },
    //物品报废
    scrap:function (id) {
        configObj.layerObj={};
        productUsedObj.removeDisabledStyle();
        productUsedObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 90000,
            content: $("#saveScrapModal").html(),
            btn: [],
            area: ['80%', '75%'],
            offset:['60px','120px'],
            closeBtn: 0,
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj = layero;
                $(layero[0]).find("input[name='userId']").val(user.id);
                $(layero[0]).find("input[name='userName']").val(user.name);
                $(layero[0]).find("input[name='deptName']").val(user.deptId);
                $(layero[0]).find("input[name='deptName']").val(user.deptName);
                $(layero[0]).find("input[name='createTime']").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                //清空详情展示区
                $("#showDetailsTable2 tbody").html("");
                $(".save").show();
                $(".edit").hide();
                $(".approve").hide();
                $("#showHistory2").hide();
                $(".licence").hide();
                $(".viewFooter").hide();
                $(".editFooter").show();
                $(layero[0]).find("input[name='inventoryId']").val(id);
                requestData({id:id},"/goods/editAjax","post","json",true,function (data) {
                    if (data.code == 200) {
                        var html = "";
                        $(data.data.entity).each(function (i, obj) {
                            html = "<tr>" +
                                "<td style='text-align: center'>"+1+"</td>" +
                                "<td style='text-align: center' class='overFlowDiv' title='"+obj.code+"'>"+obj.code+"</td>" +
                                "<td style='text-align: center'>"+configObj.typeData[obj.typeId]+"</td>" +
                                "<td style='text-align: center'>"+obj.goodsName+"</td>" +
                                "<td style='text-align: center'>"+(obj.specs==""?"-":obj.specs)+"</td>" +
                                "<td style='text-align: center'>"+obj.unit+"</td>" +
                                "<td style='text-align: center'>"+obj.number+"</td>" +
                                "<td style='text-align: center'>"+(obj.returnTime==null?"无":obj.returnTime)+"</td>" +
                                "</tr>";
                        });
                        productUsedObj.loadScrapCode(layero);
                        $(layero[0]).find("table[id='showDetailsTable2'] tbody").append(html);
                    }
                });
            }
        });
    },
    //查看物品报废信息flag：0驳回，1查看，2审核
    viewScrap:function (id,flag) {
        configObj.layerObj={};
        productUsedObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 90000,
            content: $("#saveScrapModal").html(),
            btn: [],
            area: ['80%', '75%'],
            offset:['60px','120px'],
            closeBtn: 0,
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj = layero;
                $(layero[0]).find("input[name='taskId']").val("");
                $(layero[0]).find("input[name='itemId']").val("");
                //清空详情展示区
                if(flag==0){
                    $("#showDetailsTable2 tbody").html("");
                    $(".save").hide();
                    $(".edit").show();
                    $(".approve").hide();
                    $("#showHistory2").hide();
                    $(".licence").hide();
                    $(".viewFooter").hide();
                    $(".editFooter").show();
                }else if(flag==1){
                    $("#showDetailsTable2 tbody").html("");
                    $(".save").hide();
                    $(".edit").hide();
                    $(".approve").hide();
                    $("#showHistory2").hide();
                    $(".licence").hide();
                    $(".viewFooter").show();
                    $(".editFooter").hide();
                }else if(flag==2){
                    $("#showDetailsTable2 tbody").html("");
                    $(".save").hide();
                    $(".edit").hide();
                    $(".approve").show();
                    $("#showHistory2").show();
                    $(".licence").show();
                    $(".viewFooter").show();
                    $(".editFooter").hide();
                }
                requestData({id:id},"/scrap/getById","post","json",true,function (data) {
                    if (data.code == 200) {
                        if(data.data.entity!=null){
                            for(var attr in data.data.entity){
                                $(layero[0]).find("input[name="+attr+"][type!='radio']").val(data.data.entity[attr]);
                                if(attr=="remark"){
                                    $(layero[0]).find("[name="+attr+"][type!='radio']").html(data.data.entity[attr]);
                                }
                                if(attr=="inventoryId"){
                                    requestData({id:data.data.entity[attr]},"/goods/editAjax","post","json",false,function (object) {
                                        if (object.code == 200) {
                                            var html = "";
                                            $(object.data.entity).each(function (i, obj) {
                                                html = "<tr>" +
                                                    "<td style='text-align: center'>"+1+"</td>" +
                                                    "<td style='text-align: center' class='overFlowDiv' title='"+obj.code+"'>"+obj.code+"</td>" +
                                                    "<td style='text-align: center'>"+configObj.typeData[obj.typeId]+"</td>" +
                                                    "<td style='text-align: center'>"+obj.goodsName+"</td>" +
                                                    "<td style='text-align: center'>"+(obj.specs==""?"-":obj.specs)+"</td>" +
                                                    "<td style='text-align: center'>"+obj.unit+"</td>" +
                                                    "<td style='text-align: center'>"+obj.number+"</td>" +
                                                    "<td style='text-align: center'>"+(obj.returnTime==null?"无":obj.returnTime)+"</td>" +
                                                    "</tr>";
                                            });
                                            $(layero[0]).find("table[id='showDetailsTable2'] tbody").append(html);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
                if(flag==0){
                    productUsedObj.removeDisabledStyle();
                }else if(flag==1){
                    productUsedObj.addDisabledStyle();
                }else{
                    productUsedObj.addDisabledStyle();
                }
            }
        });
    },
    //提交报废
    submitScrap(t,url,state){//校验表格数据
        if(productUsedObj.checkFormEmpty()){
            layer.confirm("是否报废该产品",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                var formData=$(configObj.layerObj[0]).find("form").serializeForm();
                formData.state=state;
                formData.htmlFlag=configObj.htmlFlag;
                $(configObj.layerObj[0]).find("div[id='updateScrapBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='editScrapBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='saveScrapBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='addScrapBtn']").css("pointer-events","none");
                requestData(formData,url,"post","json",true,function (data) {
                    if (data.code==200){
                        $(configObj.layerObj[0]).find("div[id='updateScrapBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='editScrapBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='saveScrapBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='addScrapBtn']").css("pointer-events","auto");
                        if(state==0){
                            if(productUsedObj.saveScrapUrl==url){
                                configObj.tempData.scrapCode=[];
                                productUsedObj.closeModal();
                                productUsedObj.initPagerPlugin();
                                productUsedObj.viewScrap(data.data.entity.id,0);
                            }else{
                                productUsedObj.closeModal();
                                productUsedObj.initPagerPlugin();
                            }
                        }else {
                            productUsedObj.closeModal();
                            productUsedObj.initPagerPlugin();
                            configObj.tempData.scrapCode=[];
                        }
                        layer.msg(data.data.message,{icon:6,time:1000});
                    }else if(data.code==1002){
                        swal({
                            title:"提示",
                            text:data.msg
                        });
                        productUsedObj.closeModal();
                    }
                });
            })
        }
    },
    //删除报废
    delScrap:function(id){
        layer.confirm("是否删除报废申请",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            requestData({id:id},"/scrap/deleteScrap","post","json",true,function (data) {
                if (data.code==200){
                    layer.msg(data.data.message,{icon:6,time:1000});
                    configObj.tempData.scrapCode=[];
                    productUsedObj.initPagerPlugin();
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                    productUsedObj.closeModal();
                }
            });
        })

    },
    //审核通过
    approve: function (t) {
        var taskId = $(configObj.layerObj[0]).find("input[name='taskId']").val();
        var remark = $(configObj.layerObj[0]).find("input[name='description']").val();
        approveTask(taskId, 1, t.id, remark);
    },
    //审核驳回
    reject: function (t) {
        var taskId = $(configObj.layerObj[0]).find("input[name='taskId']").val();
        var remark = $(configObj.layerObj[0]).find("input[name='description']").val();
        approveTask(taskId, 0, t.id, remark);
    },
}