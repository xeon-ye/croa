var tableData={}; //缓存入库明细数据
var tableDataHtml =""; //入库明细列表
var applyData ={};//缓存领用明细
var applyHtml="";//出库明细列表
var users=[];//某公司所有用户
var configObj={
    htmlFlag:1,//物品库存页面标志
    layerObj:{},//物品入库对象
    putCode:[],//物品入库编号
    outStockCode:[],//物品出库编号
    repairCode:[],//物品报修编码缓存
    scrapCode:[],//物品报废编码缓存
    returnCode:[],//物品归还编码缓存
    inventoryCode:[],//库存编码缓存
    stockCheckCode:[],//盘点编码缓存
    detailsWareId:[],//采购明细id
    typeData:{},//产品分类缓存数据(data)
    type:{},//产品分类缓存数据(id,name)
    stateList:[],//领用出库时用来确定出库状态
    warehouseData:{},//仓库数据（id,name）
    warehouseList:{},//仓库数据data
    goodsData:{},//产品列表数据
    purchaseId:"", //编辑时储存最开始的采购订单id
    goodsIds:[],//缓存已选择产品ids
    //产品分类缓存数据
    listGoodsTypeData:function () {
        if(configObj.isEmptyObject(configObj.type)){
            commonObj.requestData(null,"/goodsType/loadGoodsTypeInfo","post","json",false,function (data) {
                configObj.typeData=data;
                for (var i =0;i<data.data.list.length;i++){
                    var id = data.data.list[i].id;
                    var name = data.data.list[i].name;
                    configObj.type[id]=name;
                }
            });
        }
    },
    //产品缓存数据
    listGoodsData:function(t){
        if(configObj.goodsData[t]!=null || configObj.goodsData[t]!=undefined || configObj.goodsData[t]!=''){
            commonObj.requestData({parentId:t},"/goodsType/loadGoodsTypeByParentId","post","json",false,function (data) {
                 configObj.goodsData[t]=data;
            });
        }
    },
    //加载物品入库编号
    loadPutCode:function(){
        if(configObj.putCode.length<=0){
            commonObj.requestData(null,"/inventoryStock/getOutboundCode","post","json",false,function (data) {
                configObj.putCode=data.data.code;
            });
        }
    },
    //加载物品入库编号
    loadOutStockCode:function(){
        if(configObj.outStockCode.length<=0){
            commonObj.requestData(null,"/inventoryStock/getOutStockCode","post","json",false,function (data) {
                configObj.outStockCode=data.data.code;
            });
        }
    },
    //加载物品入库编号
    loadInventoryCode:function(){
        if(configObj.inventoryCode.length<=0){
            commonObj.requestData(null,"/inventoryStock/getInventoryCode","post","json",false,function (data) {
                configObj.inventoryCode=data.data.code;
            });
        }
    },
    loadStockCheckCode:function(){
        if(configObj.stockCheckCode.length<=0){
            commonObj.requestData(null,"/inventoryCheck/getStockCheckCode","post","json",false,function (data) {
                configObj.stockCheckCode=data.data.code;
            });
        }

    },
    listWarehouseData:function(){
        if(configObj.isEmptyObject(configObj.warehouseList)){
            commonObj.requestData(null,"/warehouse/warehouseList","get","json",false,function (data) {
                configObj.warehouseList=data;
                for (var i =0;i<data.list.length;i++){
                    var id = data.list[i].id;
                    var name = data.list[i].name;
                    configObj.warehouseData[id]=name;
                }
            })
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

//页面公共处理对象
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
        if(index == 1){
            $("#outStock").css("display","block");
            loadDate("#outStockTimeQc");
            outStockObj.init(); //出库列表展示
        }else if(index == 2){
            $("#productStock").css("display","block");
            productStockObj.init(); //产品库存列表展示
            productStockObj.loadGoodsType("#typeQc");
            putStockObj.selectWarehouse("#wareIdQc",null);
        }else if(index == 3){
            $("#stockCheck").css("display","block");
            loadDate("#checkTimeQc");
            putStockObj.selectWarehouse("#warehouseQc",null);
            stockCheckObj.init(); //库存盘点列表展示
        }else if(index == 4){
            $("#stockForeWarn").css("display","block");
            productStockObj.loadGoodsType("#foreTypeIdQc");
            putStockObj.selectWarehouse("#foreWareIdQc",null);
            stockForeWarnObj.init(); //库存预警列表展示
        }else if(index == 5){
            $("#warehouseStock").css("display","block");
            loadDate("#wareTimeQc");
            warehouseObj.init(); //仓库列表展示
        }else{
            $("#putStock").css("display","block");
            loadDate("#createTimeQc");
            putStockObj.init(); //入库列表展示
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
                limits: config.limits || [20, 50, 100],
                limit: config.limit || 20,
                jump: function (obj, first) {
                    config.param = config.param || {};
                    config.param.size = obj.limit;
                    config.param.page = obj.curr;
                    //下面这段逻辑为静态测试，等有实际接口之后，使用下面注释代码代替
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
    validResData: function (data, message) {
        if (data.code == 200) {
            swal({
                title: "成功",
                text: message,
                type: "success"
            },function () {
                reflushTable(); //刷新表格
            });
        } else {
            swal({
                title: "失败",
                text: data.msg,
                type: "error"
            });
        }
    },
    //表格行鼠标移动事件
    mouseOver: function (t) {
        $(t).find(".editButton").each(function (i, btn) {
           $(btn).css("background-color", "#F2F2F2");
        });
        $(t).find(".editInput").each(function (i, btn) {
            $(btn).css("background-color", "#F2F2F2");
        });
    },
    //表格行鼠标移出时间
    mouseOut: function (t) {
        $(t).find(".editButton").each(function (i, btn) {
            $(btn).css("background-color", "white");
        });
        $(t).find(".editInput").each(function (i, btn) {
            $(btn).css("background-color", "white");
        });
    },
    loadUserData:function () {
        if(users.length<=0){
            commonObj.requestData({companyCode:user.companyCode},"/propose/listByForum","post","json",false,function (data) {
               users=data;
            });
        }
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

function loadDate(elem) {
    layui.use('laydate', function () {
        var layDate = layui.laydate;
        layDate.render({
            elem: elem,
            format: 'yyyy-MM-dd',
            istime: true,
            istoday: true,// 是否显示今天
            isclear: true, // 是否显示清空
        });
    });
}

//得到查询参数
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]);
    return null;
}

//页面初始化函数
var qrcode;
$(function () {
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    //加载tab页
    layui.use('element', function(){
        var element = layui.element;
        element.on('tab(docDemoTabBrief)', function(data){
            //如果不点击搜索按钮，进行处理
            if($(data.elem.context).find("input").length < 1){
                commonObj.tabChange(data.index);
            }
        });
    });

    //初始化默认二维码对象，需要先创建该对象，如果在批量逻辑里面进行创建时，会导致下载二维码出现问题
    qrcode =  new QRCode("qrcode",{
        text: "default",
        width : 48,
        height : 48
    });

    //使用layui表单
    layui.use('form', function(){
        var form = layui.form;
        //下拉列表改变事件产品库存
        form.on('select(typeQc)', function(data){
            productStockObj.init();
        });
        form.on('select(wareIdQc)', function(data){
            productStockObj.init();
        });
        form.on('select(stateQc)', function(data){
            productStockObj.init();
        });
        //库存盘点
        form.on('select(warehouseQc)', function(data){
            stockCheckObj.init();
        });
        form.on('select(checkStateQc)', function(data){
            stockCheckObj.init();
        });
        //库存预警
        form.on('select(foreTypeIdQc)', function(data){
            stockForeWarnObj.init();
        });
        form.on('select(foreWareIdQc)', function(data){
            stockForeWarnObj.init();
        });
        //入库
        form.on('select(putStateQc)', function(data){
            putStockObj.init()
        });
        //出库
        form.on('select(outStockState)', function(data){
            outStockObj.init();
        });
        form.render();
    });

    //加载物品分类数据
    configObj.listGoodsTypeData();

    //报修repairFlag=0:驳回,编辑1:查看,2:审核
    if (getQueryString("repairFlag") != null && getQueryString("repairFlag") != '' && getQueryString("repairFlag") != undefined) {
        var repairFlag = getQueryString("repairFlag");
        var id = getQueryString("id");
        if(repairFlag==0){
            productStockObj.viewRepair(id,0);
        }else if(repairFlag==1){
            productStockObj.viewRepair(id,1);
        }else if(repairFlag==2){
            productStockObj.viewRepair(id,2);
        }
    }

    //报废scrapFlag=0:驳回,编辑1:查看,2:审核
    if (getQueryString("scrapFlag") != null && getQueryString("scrapFlag") != '' && getQueryString("scrapFlag") != undefined) {
        var scrapFlag = getQueryString("scrapFlag");
        var id = getQueryString("id");
        if(scrapFlag==0){
            productStockObj.viewScrap(id,0);
        }else if(scrapFlag==1){
            productStockObj.viewScrap(id,1);
        }else if(scrapFlag==2){
            productStockObj.viewScrap(id,2);
        }
    }

    //归还returnFlag=0:驳回,编辑1:查看,2:审核
    if (getQueryString("returnFlag") != null && getQueryString("returnFlag") != '' && getQueryString("returnFlag") != undefined) {
        var returnFlag = getQueryString("returnFlag");
        var id = getQueryString("id");
        if(returnFlag==0){
            productStockObj.viewReturn(id,0);
        }else if(returnFlag==1){
            productStockObj.viewReturn(id,1);
        }else if(returnFlag==2){
            productStockObj.viewReturn(id,2);
        }
    }

    //物品采购导入
    $("#importInventory").click(function () {
        fileUpload.upload();
    });

    var fileUpload = new FileUpload({
        targetEl: '#word2htmlForm',
        multi: false,
        filePart: "inventory",
        completeCallback: function (data) {
            if (data.length > 0)
                $.get(baseUrl + "/inventoryStock/importInventoryData?filePath="+data[0].file,function (data) {
                    if (data.code == 200) {
                        var message = "操作完成";
                        var messageType = "success";
                        productStockObj.init();
                        var isHtml = false;
                        if(data.data.message){
                            message = data.data.message;
                            messageType = "warning";
                            if(data.data.file!=null){
                                message = "<a style='color: red;font-weight: bold' href='"+data.data.file+"'>"+message+"</a>";
                                isHtml = true;
                            }
                        }
                        swal({
                            title:"提示",
                            text:message,
                            type:messageType,
                            html:isHtml,
                        });
                    }else {
                        swal({
                            title: data.msg,
                            type: "error"
                        });
                    }
                }, "json");
        },
        acceptSuffix: ['xlsx','xls']
    });
    //默认选择第一个tab
    commonObj.tabChange(0);
});

//仓库相关操作
var warehouseObj = {
    modalIndex: null,
    getTotalUrl:"/warehouse/getPageCount",
    wareListUrl:"/warehouse/listPg",
    callback:function(data){
        var html = "";
        $(".warehouseList").empty();
        configObj.listWarehouseData();
        if (data && data.list != null) {
            $.each(data.list, function (i, obj) {
                //根据分页插件计算序号
                var pageSize = data.pageSize;
                var pageNum = data.pageNum;
                //序号
                var pageIndex = (pageNum - 1)*pageSize + i;//下标从0开始
                //将库存详情添加缓存 根据库存编号
                html += "<tr>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +(pageIndex + 1)+
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title='" + obj.code + "'>\n" +
                    "            <div class=\"overFlowDiv ellipsisContent\">\n" +
                    "                " + obj.code + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + obj.name + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + obj.userName + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + obj.address + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + obj.createName + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + (new Date(obj.createTime).format("yyyy-MM-dd")) + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td style='text-align: center'>\n";
                html+="<a href='javascript:void(0)' class='button-style' onclick='warehouseObj.editWarehouse("+obj.id+")'>编辑</a>"+
                      "<a href='javascript:void(0)' class='button-style' onclick='warehouseObj.del("+obj.id+")'>删除</a>"+
                       "</td></tr>";
            });
            $(".warehouseList").append(html);
        }
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render('checkbox');
        });
    },
    init: function () {
        var formData = $("#wareForm").serializeJson();
        commonObj.requestData(formData,warehouseObj.getTotalUrl,"post","json",true,function (data) {
            if(data && data.code==200){
                commonObj.pagerPlus({
                    param: formData,
                    elem: $(".wareListPager"),
                    count:data.data.total,
                    url: warehouseObj.wareListUrl,
                }, warehouseObj.callback);
            }
        });
    },
    warehouseModalShow: function () {
        configObj.layerObj={};
        warehouseObj.modalIndex  = layer.open({
            type: 1,
            title: '新增仓库',
            zIndex: 99999,
            content: $("#warehouseModal").html(),
            btn: ['保存','关闭'],
            area: ['60%', '50%'],
            resize: false,
            move: '.layui-layer-title',
            moveOut: true,
            success: function(layero, index){
                configObj.layerObj=layero;
                outStockObj.loadUser(null,$(layero[0]).find("select[id='wareUserId']"));
            },
            yes: function (t,layero) {
                var name=$(configObj.layerObj[0]).find("input[name='name']").val();
                if(warehouseObj.checkFormData() && warehouseObj.getSameNameList(null,name)){
                    layer.confirm("是否保存仓库信息",{
                        btn:["确定","取消"],
                        shade:false
                    },function (index) {
                        layer.close(index);
                        var jsonData = $(layero[0]).find("#warehouseForm").serializeForm()
                        //请求
                        commonObj.requestData(JSON.stringify(jsonData),"/warehouse/addWarehouse","post","json",true,function (data) {
                            if (data.code==200){
                                layer.msg(data.data.message,{time:1000,icon:6});
                                warehouseObj.closeModal();
                                warehouseObj.init();
                            }else if(data.code == 1002){
                                swal({
                                    title: "异常提示",
                                    text: data.msg,
                                });
                            }
                        },true)

                    });

                }
            }
        });
    },
    closeModal: function () {
        if (warehouseObj.modalIndex) {
            layer.close(warehouseObj.modalIndex);
        } else {
            layer.closeAll();
        }
    },
    //编辑
    editWarehouse:function(id){
        configObj.layerObj={};
        warehouseObj.modalIndex  = layer.open({
            type: 1,
            title: '编辑仓库',
            zIndex: 99999,
            content: $("#warehouseModal").html(),
            btn: ['保存','关闭'],
            area: ['60%', '50%'],
            resize: false,
            move: '.layui-layer-title',
            moveOut: true,
            success: function(layero, index){
                configObj.layerObj=layero;
                commonObj.requestData({id:id},"/warehouse/editAjax","post","json",true,function (data) {
                    for(var attr in data.data.entity){
                        $(layero[0]).find("[name="+attr+"]").val(data.data.entity[attr]);
                        if(attr=="userId"){
                            outStockObj.loadUser(data.data.entity[attr],$(layero[0]).find("select[id='wareUserId']"));
                        }
                    }
                });
            },
            yes: function (t,layero) {
                var id=$(configObj.layerObj[0]).find("input[name='id']").val();
                var name=$(configObj.layerObj[0]).find("input[name='name']").val();
                if(warehouseObj.checkFormData() && warehouseObj.getSameNameList(id,name)){
                    layer.confirm("是否修改仓库信息",{
                        btn:["确定","取消"],
                        shade:false
                    },function (index) {
                        layer.close(index);
                        var formData = $(layero[0]).find("#warehouseForm").serializeForm();
                        //请求
                        commonObj.requestData(formData,"/warehouse/editWarehouse","post","json",true,function (data) {
                            if (data.code==200){
                                layer.msg(data.data.message,{time:1000,icon:6});
                                warehouseObj.closeModal();
                                warehouseObj.init();
                            }else if(data.code == 1002){
                                swal({
                                    title: "异常提示",
                                    text: data.msg,
                                });
                            }
                        })
                    });
                }
            }
        });
    },
    del:function(id){
        if(warehouseObj.checkDeleteInfo(id)){
            layer.confirm("是否删除仓库信息",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                //请求
                requestData({id:id},"/warehouse/delWarehouse","post","json",true,function (data) {
                    if (data.code==200){
                        layer.msg(data.data.message,{time:1000,icon:6});
                        warehouseObj.closeModal();
                        warehouseObj.init();
                    }else if(data.code == 1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    }
                })
            });
        }
    },
    //判断是否可以删除
    checkDeleteInfo:function(id){
        var flag=false;
        commonObj.requestData({id:id},"/warehouse/getCountByWareId","post","json",false,function (data) {
            if(data.data.total>0){
                layer.open({
                    title:"提示",
                    content:"该仓库中含有库存不能删除"
                });
                flag=false;
            }else{
                flag=true;
            }
        });
        return flag;
    },
    //校验表单数据
    checkFormData:function () {
        var code=$(configObj.layerObj[0]).find("input[name='code']").val().trim();
        var userId=$(configObj.layerObj[0]).find("select[name='userId']").val();
        var name=$(configObj.layerObj[0]).find("input[name='name']").val().trim();
        var address=$(configObj.layerObj[0]).find("input[name='address']").val().trim();
        if(code==""){
            layer.open({
                title:"提示",
                content:"仓库编号不能为空"
            });
            return false;
        }
        if(userId==""){
            layer.open({
                title:"提示",
                content:"请选择仓库管理员"
            });
            return false;
        }
        if(name==""){
            layer.open({
                title:"提示",
                content:"请输入仓库名称"
            });
            return false;
        }
        if(address==""){
            layer.open({
                title:"提示",
                content:"请输入仓库地址"
            });
            return false;
        }
        return true;
    },
    getSameNameList:function(id,name) {
        var flag=false;
        commonObj.requestData({id:id,name:name},"/warehouse/getSameNameList","post","json",false,function (data) {
            if(data>0){
                layer.open({
                    title:"提示",
                    content:"仓库名称已存在,请重新输入"
                })
                $(configObj.layerObj[0]).find("input[name='name']").val("");
                flag = false;
            }else {
                flag = true;
            }
        });
        return flag;
    }
}

//入库相关操作
var putStockObj = {
    getTotalUrl:"/inventoryStock/getPageCount",
    putStockListUrl: "/inventoryStock/listPg",
    stagingUrl:"/inventoryStock/addOutBound",
    modalIndex: null,
    callback: function (data, target)   {
        var html = "";
        if (data && data.list && data.list.length > 0) {
            $.each(data.list, function (i, record) {
                var state = record.state;
                var msg="";
                if(state==0){
                    msg="已保存";
                }else if(state==1){
                    msg="<span style='color: #81CBFE;'>已入库</span>";
                }else if(state==-5){
                    msg="<span class='text-red'>已失效</span>";
                }
                //根据分页插件计算序号
                var pageSize = data.pageSize;
                var pageNum = data.pageNum;
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                html += "<tr>\n" +
                    "        <td title=\"" + (pageIndex + 1) + "\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + (pageIndex + 1) + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\" onclick='putStockObj.view(" + record.id + ",1)' style=\"cursor: pointer;color: #81CBFE;\">\n" +
                    "               " + record.code + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                 " + record.wareHouseName + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\" onclick=\"putStockObj.getPurchaseInfo("+record.purchaseId+")\" style=\"cursor: pointer;color: #81CBFE;\">\n" +
                    "                " + record.outBoundCode + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + record.totalMoney + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + msg + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                 " + (new Date(record.createTime).format("yyyy-MM-dd hh:mm:ss")) + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                 " + record.createName + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td style='text-align: center'>\n";
                if (record.state == 0) {
                    html += "            <div class=\"operateContent\" onclick='putStockObj.view(" + record.id + ",0)'>编辑\n" +
                            "            </div>\n" +
                            "            <div class=\"operateContent\" onclick='putStockObj.delOutbound(" + record.id + "," + record.purchaseId + ")'>删除\n" +
                            "            </div>\n" +
                            "</td></tr>\n";
                } else {
                    html += "</td></tr>\n";
                }
            });
        }
        var $parentList = target ? $(target) : $("#putStockList");
        $parentList.html(html);
    },
    init: function () {
        var formData = $("#putStockForm").serializeJson();
        commonObj.requestData(formData,putStockObj.getTotalUrl,"post","json",true,function (data) {
            if(data && data.code==200){
                commonObj.pagerPlus({
                    param: formData,
                    elem: $(".putStockListPager"),
                    count:data.data.total,
                    url: putStockObj.putStockListUrl,
                    target: $(".putStockList"),
                }, putStockObj.callback);
            }
        });
    },
    //物品入库展示页面
    putStockModalShow: function () {
        configObj.layerObj={};
        tableData={};
        tableDataHtml ="";
        configObj.purchaseId ="";
        $(".save").show();
        $(".edit").hide();
        $(".lossEffect").show();
        $("#applyAmount").text(0);
        $(".stockCheckList").html("");
        putStockObj.removeOutboundDisableStyle();
        putStockObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#putStockModal").html(),
            btn: [],
            area: ['85%', '65%'],
            offset:['70px','70px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj=layero;
                //渲染多选采购订单
                loadOrderList("/purchase/orderList",$(layero[0]).find("select[name='foreignId']")[0]);
                configObj.loadPutCode();
                putStockObj.selectWarehouse($(layero[0]).find("select[name='wareId']")[0],null);
                $("#putStockModalFrom input[name='code']").val(configObj.putCode);
                $("#putStockModalFrom input[name='createId']").val(user.id);
                $("#putStockModalFrom input[name='createName']").val(user.name);
                $("#putStockModalFrom input[name='deptId']").val(user.deptId);
                $("#putStockModalFrom input[name='deptName']").val(user.deptName);
                $("#putStockModalFrom input[name='createTime']").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                //使用layui表单
                layui.form.render();
            }
        });
    },
    closeModal: function () {
        if (putStockObj.modalIndex) {
            layer.close(putStockObj.modalIndex);
        } else {
            layer.closeAll();
        }
    },
    addWarehouseFrom: function (value) {
        var param= {};
        configObj.detailsWareId=[];
        param.id = value;
        commonObj.requestData(param,"/inventoryStock/warehousingDetail","post","json",false,function (data) {
            var html = "";
            configObj.listWarehouseData();
            $.each(data.data.list,function (i,data) {
                var wareId = data.wareId;
                var warehouseData = configObj.warehouseList;
                var html1 = "";
                for (var j = 0; j < warehouseData.list.length; j++) {
                    var id = warehouseData.list[j].id;
                    var name = warehouseData.list[j].name;
                    var selected = id == wareId ? "selected=selected" : "";
                    html1 += "<option  value='" + id + "' " + selected + ">" + name + "</option>";
                }
                html += "<tr id='detail"+data.purchaseCode+"'>" +
                    "<th>" +
                    "<div class=\"ellipsisContent\">" +(i+1)+ "</div>"+
                    "</th>"+
                    "<th>"+
                    "<div class=\"ellipsisContent\">"+data.supplierName+"</div>"+
                    "</th>"+
                    "<th>"+
                    "<div class=\"ellipsisContent\">" +configObj.type[data.parentId]+ "</div>"+
                    "</th>"+
                    "<th>"+
                    "<div class=\"ellipsisContent\">" +data.goodsName+ "</div>"+
                    "</th>"+
                    "<th>"+
                    "<div class=\"ellipsisContent\">" +data.unit+ "</div>"+
                    "</th>"+
                    "<th>"+
                    "<div class=\"ellipsisContent\">" +data.amount+ "</div>"+
                    "</th>"+
                    "<th>"+
                    "<div class=\"ellipsisContent\">" +data.price+ "</div>"+
                    "</th>"+
                    "<th>"+
                    "<div class=\"ellipsisContent\">" +data.totalMoney+ "</div>"+
                    "</th>"+
                    "<th>"+
                    "<div class=\"layui-form\">" +
                    " <select lay-filter='house' name='warehouseId"+data.id+"' class=\"warehouseId form-control\" lay-search>" +
                    "              "+html1+"\n" +
                    "</select></div>"+
                    "</th>"+
                    "</tr>";
                configObj.detailsWareId.push(data.id);
            });
            $("tbody[id='warehousingDetailTable']").html(html);
            tableDataHtml=html;
            //总计金额
            var money = data.data.list[0].money;
            $(configObj.layerObj[0]).find("span[id='applyAmount']").text(money.toFixed(2)||0);
        });
    },
    //仓库查询
    selectWarehouse:function (t,id) {
        layui.use(["form"],function () {
            configObj.listWarehouseData();
            var data = configObj.warehouseList;
            $(t).empty();
            var html="<option value=''>--请选择--</option>";
            $(data.list).each(function (i, d) {
                var selected = id == d.id ? "selected=selected" : "";
                html+="<option value='" + d.id + "' " + selected + ">" + d.name + "</option>";
            });
            $(t).append(html);
            layui.form.render();
        });
    },
    checkFormData:function(){
        var purchaseCode = $(configObj.layerObj[0]).find("select[name='foreignId']").text().trim();
        var wareId = $(configObj.layerObj[0]).find("select[name='wareId']").val();
        if(purchaseCode=="" || purchaseCode==null){
            layer.open({
                title:"提示",
                content:"请选择采购订单"
            });
            return false;
        }
        if(wareId=="" || wareId==null){
            layer.open({
                title:"提示",
                content:"请选择仓库"
            });
            return false;
        }
        var tableLength = $(configObj.layerObj[0]).find("tbody[id='warehousingDetailTable'] tr").length;
        if(tableLength<=0){
            layer.open({
                title:"提示",
                content:"物品入库信息为空，请检查采购订单是否正常"
            });
            return false;
        }
        return true;
    },
    //入库查看添加disabled属性
    addOutboundDisableStyle: function () {
        $("#putStockModalFrom [name='code']").attr("disabled", true);
        $("#putStockModalFrom [name='foreignId']").attr("disabled", true);
        $("#putStockModalFrom [name='wareId']").attr("disabled", true);
        $("#putStockModalFrom [name='rejectReason']").attr("disabled", true);
        $("#putStockModalFrom [name='deptName']").prop("disabled", true);
        $("#putStockModalFrom [name='createName']").attr("disabled", true);
        $("#putStockModalFrom [name='createTime']").attr("disabled", true);
        $("#putStockModalFrom [name='applyAmount']").attr("disabled", true);
        $(".warehouseId").attr("disabled", true);
        $("#putStockModalFrom .layui-form-select").addClass("layui-select-disabled");
        $("#putStockModalFrom .layui-select-title input").addClass("layui-disabled");
    },
    //入库编辑新增移除disabled属性
    removeOutboundDisableStyle: function () {
        $("#putStockModalFrom [name='code']").attr("disabled", false);
        $("#putStockModalFrom [name='foreignId']").attr("disabled", false);
        $("#putStockModalFrom [name='wareId']").attr("disabled", false);
        $("#putStockModalFrom [name='rejectReason']").attr("disabled", false);
        $("#putStockModalFrom [name='deptName']").prop("disabled", false);
        $("#putStockModalFrom [name='createName']").attr("disabled", false);
        $("#putStockModalFrom [name='createTime']").attr("disabled", false);
        $("#putStockModalFrom [name='applyAmount']").attr("disabled", false);
        $(".warehouseId").attr("disabled", false);
        $("#putStockModalFrom .layui-form-select").removeClass("layui-select-disabled");
        $("#putStockModalFrom .layui-select-title input").removeClass("layui-disabled");
    },
    getPurchaseInfo:function(id){
        page("/inventory/purchase_list?flag=1&id="+id,"物品采购");
        // location.href="/inventory/purchase_list?flag=1&id="+id;
    },
    //入库暂存
    submitOutbound:function (url,state) {
        if(putStockObj.checkFormData()){
            layer.confirm("是否保存入库信息？",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                //提交后使div点击事件失效
                $(configObj.layerObj[0]).find("div[id='addBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='saveBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='editBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='updateBtn']").css("pointer-events","none");
                var formData = $(configObj.layerObj[0]).find("form").serializeForm();
                var purchaseDetails=[];
                for (var i=0;i<configObj.detailsWareId.length;i++){
                    var id = configObj.detailsWareId[i];
                    var wareId = $(configObj.layerObj[0]).find("select[name = 'warehouseId"+id+"']").val();
                    var wareData=id+":"+wareId;
                    purchaseDetails.push(wareData);
                }
                formData.purchaseDetails=purchaseDetails;
                formData.state=state;
                formData.purchaseId=configObj.purchaseId;
                commonObj.requestData(formData,url,"post","json",true,function (data) {
                     if (data.code==200){
                         layer.msg(data.data.message,{time:1000,icon:6});
                         //提交成功后使div点击事件恢复
                         $(configObj.layerObj[0]).find("div[id='addBtn']").css("pointer-events","auto");
                         $(configObj.layerObj[0]).find("div[id='saveBtn']").css("pointer-events","auto");
                         $(configObj.layerObj[0]).find("div[id='editBtn']").css("pointer-events","auto");
                         $(configObj.layerObj[0]).find("div[id='updateBtn']").css("pointer-events","auto");
                         if(state==0){
                             if(putStockObj.stagingUrl==url){
                                 //新增后跳转编辑页面
                                 configObj.putCode=[];
                                 putStockObj.closeModal();
                                 putStockObj.view(data.data.entity.id,0);
                             }
                         }else{
                             //入库提交
                             configObj.putCode=[];
                             putStockObj.closeModal();
                         }
                         putStockObj.init();
                     }else if(data.code==1002){
                         swal({
                             title:"提示",
                             text:data.msg
                         });
                     }
                });
            })
        }
    },
    lossEffect:function(){
        if(putStockObj.checkFormData()){
            layer.confirm("是否使采购单失效？",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                //提交后使div点击事件失效
                $(configObj.layerObj[0]).find("div[id='addBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='saveBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='editBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='updateBtn']").css("pointer-events","none");
                $(configObj.layerObj[0]).find("div[id='lossEffect']").css("pointer-events","none");
                var formData = $(configObj.layerObj[0]).find("form").serializeForm();
                formData.purchaseId=configObj.purchaseId;
                requestData(formData,"/inventoryStock/lossEffect","post","json",true,function (data) {
                    if (data.code==200){
                        layer.msg(data.data.message,{time:1000,icon:6});
                        //提交成功后使div点击事件恢复
                        $(configObj.layerObj[0]).find("div[id='addBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='saveBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='editBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='updateBtn']").css("pointer-events","auto");
                        $(configObj.layerObj[0]).find("div[id='lossEffect']").css("pointer-events","auto");
                        putStockObj.closeModal();
                        putStockObj.init();
                    }else if(data.code==1002){
                        swal({
                            title:"提示",
                            text:data.msg
                        });
                    }
                });
            })
        }

    },
    //入库查看flag:0编辑;1查看
    view:function (id,flag) {
        configObj.layerObj={};
        tableData={};
        tableDataHtml ="";
        configObj.purchaseId ="";
        if(flag==0){//编辑
            $(".save").hide();
            $(".edit").show();
            $(".lossEffect").show();
        }else{//查看
            $(".save").hide();
            $(".edit").hide();
            $(".lossEffect").hide();
        }
        $("#applyAmount").text(0);
        $(".stockCheckList").html("");
        $("#outBoundCode").find("option").each(function (i,t) {
            $(t).remove(); //清空时移除当前option
        });
        putStockObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#putStockModal").html(),
            btn: [],
            area: ['85%', '65%'],
            offset:['70px','70px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj=layero;
                commonObj.requestData({id:id},"/inventoryStock/viewOutbound","post","json",true,function (data) {
                    var outBound = data.data.outbound;
                    var purchase = data.data.purchase;
                   for(var attr in outBound){
                       $(layero[0]).find("[name='"+attr+"'][type!='radio']").val(outBound[attr]);
                       if(attr=="wareId"){
                           putStockObj.selectWarehouse($(layero[0]).find("select[name='wareId']")[0],outBound[attr]);
                       }
                       if(attr=="foreignId"){
                           loadOrderList("/purchase/orderList2",$(layero[0]).find("select[name='foreignId']"),outBound[attr],purchase.purchaseCode);
                           putStockObj.addWarehouseFrom(outBound[attr]);
                       }
                   }
                    //flag:0编辑;1查看
                   if(flag==1){
                       putStockObj.addOutboundDisableStyle();
                   }else{
                       //编辑保存最开始订单编号，原因：切换订单时还原之前订单状态
                       configObj.purchaseId=purchase.id;
                       putStockObj.removeOutboundDisableStyle();
                   }
                });
                //使用layui表单
                layui.use('form', function(){
                    var form = layui.form;
                    form.render('select');
                });
            }
        });
    },
    //删除出入库记录
    delOutbound:function (id,purchaseId) {
        layer.confirm("是否删除该入库信息？",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            commonObj.requestData({id:id,purchaseId:purchaseId},"/inventoryStock/delOutbound","post","json",true,function (data) {
                if(data.code==200){
                    putStockObj.init();
                    lay.msg(data.data.message,{time:1000,icon:6});
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                }
            });
        })
    }
}

//出库相关操作
var outStockObj = {
    getPageCount: "/inventoryStock/getOutStockPageCount",
    outStockListUrl: "/inventoryStock/getOutStockListPg",
    modalIndex: null,
    outStockListCallback: function (data, target) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                var state=record.state;
                var msg="";
                if(state==0){
                    msg="待出库";
                }else if(state==-1){
                    msg="已出库";
                }
                //根据分页插件计算序号
                var pageSize = data.pageSize;
                var pageNum = data.pageNum;
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                html += "<tr>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(pageIndex+1)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\" style=\"cursor: pointer;color: #81CBFE;\" onclick='outStockObj.view("+record.id+",1)'>\n" +
                    "                "+record.code+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\" style=\"cursor: pointer;color: #81CBFE;\" onclick='outStockObj.goToApply("+record.applyId+")'>\n" +
                    "              "+record.outBoundCode+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                 "+msg+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+record.createName+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "               "+record.deptName+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+new Date(record.createTime).format("yyyy-MM-dd hh:mm:ss")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" ;
                if (record.state == 0) {
                    html += "            <div style='width:90%;display: inline-block;text-align: center;cursor: pointer;color: #81CBFE;' onclick='outStockObj.view(" + record.id + ",0)'>\n" +
                        "编辑" +
                        "            </div>\n" +
                        "</td></tr>\n";
                } else {
                    html += "</td></tr>\n";
                }
            });
        }
        var $parentList = target ? $(target) : $("#outStockList");
        $parentList.html(html);
    },
    init: function () {
        var formData=$("#outStockForm").serializeForm();
        commonObj.requestData(formData,outStockObj.getPageCount,"post","json",true,function (data) {
            if(data && data.code==200){
                commonObj.pagerPlus({
                    param: formData,
                    elem: $(".outStockListPager"),
                    count: data.data.total,
                    url: outStockObj.outStockListUrl,
                    target: $(".outStockList"),
                },outStockObj.outStockListCallback);
            }
        })
    },
    //表格数据校验
    checkFormData:function(){
        var applyCode = $(configObj.layerObj[0]).find("select[name='foreignId']").text().trim();
        if(applyCode=="" || applyCode==null){
            layer.open({
                title:"提示",
                content:"请选择领用订单"
            });
            return false;
        }
        var tableLength = $(configObj.layerObj[0]).find("tbody[id='applyDetailsTable'] tr").length;
        if(tableLength<=0){
            layer.open({
                title:"提示",
                content:"物品出库信息为空，请检查领用订单是否正常"
            });
            return false;
        }
        // 出库数量
        var outStockAmount = $(".outStockAmount");
        for(var i=0;i<tableLength;i++){
            var outAmount = outStockAmount.eq(i).val();
            if(outAmount==""){
                layer.open({
                   title:"提示",
                   content:"出库数量不能为空"
                });
                return false;
            }
            if(outAmount<0){
                layer.open({
                    title:"提示",
                    content:"出库数量必须大于0"
                });
                return false;
            }
        }
        return true;
    },
    goToApply:function(id){
        page("/inventory/apply_list?flag=1&id="+id,"物品领用");
        // location.href="/inventory/apply_list?flag=1&id="+id;
    },
    //出库查看添加disabled属性
    addOutStockDisableStyle: function () {
        $("#outStockModalFrom [name='code']").attr("disabled", true);
        $("#outStockModalFrom [name='foreignId']").attr("disabled", true);
        $("#outStockModalFrom [name='applyName']").attr("disabled", true);
        $("#outStockModalFrom [name='deptName']").prop("disabled", true);
        $("#outStockModalFrom [name='createName']").attr("disabled", true);
        $("#outStockModalFrom [name='createTime']").attr("disabled", true);
        $(".outStockAmount").attr("disabled", true);
        $(".remark").attr("disabled", true);
        $("#outStockModalFrom .layui-form-select").addClass("layui-select-disabled");
        $("#outStockModalFrom .layui-select-title input").addClass("layui-disabled");
    },
    removeOutStockDisableStyle: function () {
        $("#outStockModalFrom [name='code']").attr("disabled", false);
        $("#outStockModalFrom [name='foreignId']").attr("disabled", false);
        $("#outStockModalFrom [name='applyName']").attr("disabled", false);
        $("#outStockModalFrom [name='deptName']").prop("disabled", false);
        $("#outStockModalFrom [name='createName']").attr("disabled", false);
        $("#outStockModalFrom [name='createTime']").attr("disabled", false);
        $(".outStockAmount").attr("disabled", false);
        $(".remark").attr("disabled", false);
        $("#outStockModalFrom .layui-form-select").removeClass("layui-select-disabled");
        $("#outStockModalFrom .layui-select-title input").removeClass("layui-disabled");
    },
    outStockModalShow: function () {
        configObj.layerObj={};
        applyData={};
        applyHtml ="";
        $(".save").show();
        $(".edit").hide();
        outStockObj.removeOutStockDisableStyle();
        outStockObj.modalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#outStockModal").html(),
            btn: [],
            area: ['85%', '65%'],
            offset:['70px','70px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                configObj.layerObj=layero;
                //使用layui表单
                layui.use('form', function(){
                    var form = layui.form;
                    form.render(); //先渲染，然后执行下面复选框事件才有效
                    $(layero[0]).find("input[name='deptId']").val(user.deptId);
                    $(layero[0]).find("input[name='deptName']").val(user.deptName);
                    $(layero[0]).find("input[name='createId']").val(user.id);
                    $(layero[0]).find("input[name='createName']").val(user.name);
                    $(layero[0]).find("input[name='createTime']").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                    //自动生成出库编号
                    configObj.loadOutStockCode();
                    $(layero[0]).find("input[name='code']").val(configObj.outStockCode);
                    loadOutOrderList("/apply/orderList",$(configObj.layerObj[0]).find("select[id='applyId']"));
                    form.render('select');
                });
            },
        });
        layui.use('form', function(){
            var form = layui.form;
            form.render(); //先渲染，然后执行下面复选框事件才有效
        });
    },
    outStockModalClose: function () {
        if(outStockObj.modalIndex){
            layer.close(outStockObj.modalIndex);
        }else {
            layer.closeAll();
        }
    },
    //出库提交
    submitOutStock: function (url) {
        //清空缓存
        configObj.stateList=[];
        if(outStockObj.checkFormData()){
            layer.confirm("是否提交出库信息？",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                //提交后使div点击事件失效
                $(configObj.layerObj[0]).find("div[id='addApplyBtn']").css("pointer-events","none");
                var formData = $(configObj.layerObj[0]).find("form").serializeForm();
                formData.applyDetails=[];
                var tableElem = $(configObj.layerObj[0]).find("tbody[id='applyDetailsTable'] tr");
                $(tableElem).each(function(i,trObj){
                    //领用明细id
                    var detailId = $(trObj).find("td:first-child").find("input[name='detailsId']").val();
                    //申请数量
                    var amount = $(trObj).find("td:nth-child(6)").find("input[name='amount']").val();
                    //出库数量
                    var outAmount = $(trObj).find("td:nth-child(7)").find("input[name='outStockAmount']").val();
                    if(outAmount==amount){
                        //出库状态-1
                        configObj.stateList.push(-1);
                    }else if(outAmount<amount){
                        //待出库状态0
                        configObj.stateList.push(0);
                    }
                    var dataInfo=detailId+":"+outAmount;
                    formData.applyDetails.push(dataInfo);
                });
                var stateArray = configObj.stateList;
                if(stateArray.contains(0)){
                    //待出库状态
                    formData.state=0;
                }else if(!stateArray.contains(0)){
                    //出库状态
                    formData.state=-1;
                }
                var foreignId=formData.foreignId;
                if(foreignId==undefined || foreignId==null){
                    formData.foreignId = $(configObj.layerObj[0]).find("select[name='foreignId']").val();
                }
                commonObj.requestData(formData,url,"post","json",true,function (data) {
                    if (data.code==200){
                        layer.msg(data.data.message,{time:1000,icon:6});
                        //提交成功后使div点击事件恢复
                        $(configObj.layerObj[0]).find("div[id='addApplyBtn']").css("pointer-events","auto");
                        //新增后跳转编辑页面
                        configObj.outStockCode=[];
                        outStockObj.outStockModalClose();
                        outStockObj.init();
                    }else if(data.code==1002){
                        swal({
                            title:"提示",
                            text:data.msg
                        });
                    }
                });
            });
        }
    },
    //根据公司代码加载用户数据
    loadUser: function (t, elem) {
        $(elem).empty();
        layui.use(["form"], function () {
            commonObj.loadUserData();
            var data = users;
            var html = "<option value=''>请选择</option>";
            if(data!=null){
                for (var i = 0; i < data.data.number; i++) {
                    var id = data.data.list[i].id;
                    var name = data.data.list[i].name;
                    // var deptName = data.data.list[i].deptName;
                    var selected = id == t ? "selected=selected" : "";
                    html += "<option value='" + id + "' " + selected + ">" + name + "</option>";
                }
            }
            $(elem).append(html);
            layui.form.render();
        });
    },
    //领用明细
    addApplyDetailsHtml:function (id) {
        configObj.listWarehouseData();
        commonObj.requestData({id:id},"/inventoryStock/applyDetailsData","post","json",false,function (data) {
            var html="";
            $.each(data.data.list,function (i,d) {
                var outAmount = (d.outAmount==undefined || d.outAmount==null)?0:d.outAmount;
                html += "<tr>\n" +
                    "    <td>\n" +
                    "        <div class=\"ellipsisContent\">\n" +(i+1)+
                    "            <input type='hidden' class='detailsId' name='detailsId' value='"+d.id+"'/>"+
                    "        </div>\n" +
                    "    </td>\n" +
                    "    <td>\n" +
                    "        <div class=\"ellipsisContent\">\n" +
                    "            " + configObj.type[d.typeId]+ "\n" +
                    "        </div>\n" +
                    "    </td>\n" +
                    "    <td>\n" +
                    "        <div class=\"ellipsisContent\">\n" +
                    "            <input type='hidden' class='goodsId' name='goodsId' value='"+d.goodsId+"'/>"+
                    "            " + d.goodsName + "\n" +
                    "        </div>\n" +
                    "    </td>\n" +
                    "    <td\n" +
                    "        <div class=\"ellipsisContent\">\n" +
                    "            " + (d.specs=="" || d.specs==undefined?"-":d.specs) + "\n" +
                    "        </div>\n" +
                    "    </td>\n" +
                    "    <td>\n" +
                    "        <div class=\"ellipsisContent\">\n" +
                    "            " + d.unit + "\n" +
                    "        </div>\n" +
                    "    </td>\n" +
                    "    <td>\n" +
                    "        <div class=\"ellipsisContent\">\n" +
                    "            <input type='hidden' class='amount' name='amount' value='"+d.amount+"'/>"+
                    "            " + d.amount + "\n" +
                    "        </div>\n" +
                    "    </td>\n" +
                    "    <td>\n" +
                    "        <div class=\"ellipsisContent\">\n" ;
                    if(d.amount==outAmount){
                        html+="            <input type='number' class='outStockAmount form-control' readonly onblur='outStockObj.dealOutAmount(this)' name='outStockAmount' value='"+outAmount+"'/>\n";
                    }else{
                        html+="            <input type='number' class='outStockAmount form-control' onblur='outStockObj.dealOutAmount(this)' name='outStockAmount' value='"+outAmount+"'/>\n";
                    }
                    html+="            <input type='hidden' class='form-control' id='outStockedNumber' value='"+outAmount+"'/>\n" +//已经出库的数量
                    "        </div>\n" +
                    "    </td>\n" +
                    "    <td>\n" +
                    "        <div class=\"ellipsisContent\">\n" +
                    "            <input type='hidden' class='goodsAmount' name='goodsAmount' data-value='"+d.goodsAmount+"' value='"+d.goodsAmount+"'/>"+
                    "            <span class='showGoodsAmount'>" + d.goodsAmount + "</span>/"+d.goodsAmount+"\n" +
                    "        </div>\n" +
                    "    </td>\n" +
                    "    <td>\n" +
                    "      <div class=\"ellipsisContent\">" +
                    "            <input type='hidden' class='userId' name='userId' value='"+d.userId+"'/>"+
                    "            " + d.userName + "\n" +
                    "       </div>" +
                    "    </td>\n" +
                    "</tr>"
            })
            applyHtml=html;
            $(configObj.layerObj[0]).find("#applyDetailsTable").html(html);
            $(configObj.layerObj[0]).find("input[name='applyName']").val(data.data.list[0].applyName);
            $(configObj.layerObj[0]).find("input[name='wareId']").val(data.data.list[0].wareId);
            $(configObj.layerObj[0]).find("input[name='wareName']").val(configObj.warehouseData[data.data.list[0].wareId]);
        });
    },
    //处理出库数量
    dealOutAmount:function (t) {
        var $trElem = $(t).parent().parent().parent();
        // 申请数量
        var goodsId= parseInt($trElem.find("input[name='goodsId']").val());
        // 申请数量
        var amount= parseInt($trElem.find("input[name='amount']").val());
        // 页面填写的出库数量
        var outStockAmount = $trElem.find("input[name='outStockAmount']").val();
        // 已经出库的产品数量
        var outStockedNumber = parseInt($trElem.find("input[id='outStockedNumber']").val());
        // 原产品的库存数量
        var oldGoodsAmount = parseInt($trElem.find("input[name='goodsAmount']").attr("data-value"));
        if(outStockAmount==""){
            layer.open({
                title:"提示",
                content:"出库数量不为空"
            });
            $trElem.find("input[name='outStockAmount']").val(0);
            return;
        }
        if(outStockAmount<0){
            layer.open({
                title:"提示",
                content:"出库数量必须大于等于0"
            });
            $trElem.find("input[name='outStockAmount']").val(0);
            return;
        }
        //防止outStockAmount为空的情况
        outStockAmount=parseInt(outStockAmount);
        //待出库数量（申请-已出库）
        var needOutStockAmount = outStockAmount-outStockedNumber;
        if(outStockAmount>amount){
            layer.open({
                title:"提示",
                content:"出库数量不可以大于申请数量"
            });
            $trElem.find("input[name='outStockAmount']").val(outStockedNumber);
            outStockObj.flushOutAmount(oldGoodsAmount,goodsId);
            return;
        }
        //待出库数量不可以大于申请数量
        if(needOutStockAmount>amount){
            layer.open({
                title:"提示",
                content:"待出库数量不可以大于申请数量"
            });
            $trElem.find("input[name='outStockAmount']").val(outStockedNumber);
            outStockObj.flushOutAmount(oldGoodsAmount,goodsId);
            return;
        }
        //待出库数量大于库存数量
        if(needOutStockAmount>oldGoodsAmount){
            layer.open({
                title:"提示",
                content:"待出库数量不可以大于库存数量"
            });
            $trElem.find("input[name='outStockAmount']").val(outStockedNumber);
            return;
        }else{
            outStockObj.flushOutAmount(oldGoodsAmount,goodsId);
        }
    },
    //刷新库存显示
    flushOutAmount:function(oldGoodsAmount,goodsId){
        //计算出库数量,更新表格数量
        var outAmount=0;
        var tableElem = $(configObj.layerObj[0]).find("tbody[id='applyDetailsTable'] tr");
        //原产品库存数
        var tempAmount=oldGoodsAmount;
        //先计算出某一产品的库存数
        $(tableElem).each(function (i,trElem) {
            //产品id
            var productId = parseInt($(trElem).find("td:nth-child(3)").find("input[name='goodsId']").val());
            //出库数量
            var CKAmount = parseInt($(trElem).find("td:nth-child(7)").find("input[name='outStockAmount']").val());
            //已出库数量
            var CKNumber = parseInt($(trElem).find("td:nth-child(7)").find("input[id='outStockedNumber']").val());
            var needOutAmount = CKAmount-CKNumber;
            //申请数量
            // var amount = parseInt($(trElem).find("td:nth-child(6)").find("input[name='amount']").val());
            if(productId==goodsId){
                if(needOutAmount>=0){
                    if(tempAmount>=needOutAmount){
                        tempAmount-=needOutAmount;
                    }else{
                        $(trElem).find("td:nth-child(7)").find("input[name='outStockAmount']").val(CKNumber);
                        layer.open({
                            title:"提示",
                            content:"库存不足，请核实库存"
                        });
                        return;
                    }
                }else{
                    $(trElem).find("td:nth-child(7)").find("input[name='outStockAmount']").val(CKNumber);
                    layer.open({
                        title:"提示",
                        content:"出库数量不能小于已出库数量"
                    });
                    return;
                }
            }
            outAmount=tempAmount;
        });
        //根据可用库存数修改相同产品所对应的的库存数
        $(tableElem).each(function (i,trElem) {
            var productId = parseInt($(trElem).find("td:nth-child(3)").find("input[name='goodsId']").val());
            if(productId==goodsId){
                $(trElem).find("td:nth-child(8)").find("input[name='goodsAmount']").val(outAmount);
                $(trElem).find("td:nth-child(8) .showGoodsAmount").text(outAmount);
            }
        });
    },
    //flag:0编辑，1查看
    view:function (id,flag) {
        configObj.layerObj={};
        applyData={};
        applyHtml ="";
        if(flag==1){
            $(".save").hide();
            $(".edit").hide();
        }else{
            $(".save").hide();
            $(".edit").show();
        }
        outStockObj.modalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#outStockModal").html(),
            btn: [],
            area: ['85%', '65%'],
            offset:['70px','70px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                configObj.layerObj=layero;
                //使用layui表单
                layui.use('form', function(){
                    var form = layui.form;
                    form.render(); //先渲染，然后执行下面复选框事件才有效
                    commonObj.requestData({id:id},"/inventoryStock/editAjax","post","json",true,function (data) {
                          var apply= data.data.apply;
                          var outbound = data.data.outbound;
                          for(var attr in outbound){
                              $(layero[0]).find("[name="+attr+"][type!='radio']").val(outbound[attr]);
                              if(attr=="foreignId"){
                                  loadOutOrderList("/apply/orderList2",$(configObj.layerObj[0]).find("select[id='applyId']"),outbound[attr],apply.applyCode);
                                  outStockObj.addApplyDetailsHtml(outbound[attr]);
                                  if(flag==1){
                                      outStockObj.addOutStockDisableStyle();
                                  }else{
                                      outStockObj.removeOutStockDisableStyle();
                                      //编辑领用单不能修改
                                      $("#outStockModalFrom [name='foreignId']").attr("disabled", true);
                                  }
                              }
                          }
                    });
                    form.render('select');
                });
            },
        });
    }
}

//产品库存相关操作
var productStockObj = {
    modalIndex: null,
    approveIndex:null,
    productStockListUrl: "/goods/listPg",
    getTotalUrl:"/goods/getPageCount",
    saveRepairUrl:"/repair/saveRepair",
    saveScrapUrl:"/scrap/saveScrap",
    saveReturnUrl:"/return/saveReturn",
    callback: function (data) {
        var html = "";
        $(".productStockList").empty();
        configObj.listWarehouseData();
        if (data && data.list != null) {
            $.each(data.list, function (i, obj) {
                //根据分页插件计算序号
                var pageSize = data.pageSize;
                var pageNum = data.pageNum;
                var stateTips = "";
                var specs = (obj.specs == undefined || obj.specs == "") ? "-" : obj.specs;
                var createTime = new Date(obj.createTime).format("yyyy-MM-dd hh:mm:ss");
                switch (obj.state) {
                    case -1:
                        stateTips = "使用中";
                        break;
                    case 0:
                        stateTips = "库存";
                        break;
                    case 1:
                        stateTips = "报修";
                        break;
                    case 2:
                        stateTips = "<span class='text-red'>报废</span>";
                        break;
                    case 3:
                        stateTips = "<span>归还</span>";
                        break;
                    default:
                        break;
                }
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                //将库存详情添加缓存 根据库存编号
                html += "<tr>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                <input type=\"checkbox\" lay-skin=\"primary\">" + (pageIndex + 1) + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title='" + obj.code + "'>\n" +
                    "            <div class=\"overFlowDiv ellipsisContent\" onclick='productStockObj.productStockModalShow(" + obj.id + ")' style=\"cursor: pointer;color: #81CBFE;\">\n" +
                    "                " + obj.code + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + configObj.type[obj.typeId] + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + obj.goodsName + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + specs + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + obj.unit + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + obj.number + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + obj.price + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + (obj.userName==undefined?"":obj.userName) + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div title='"+createTime+"' class=\"ellipsisContent\">\n" +
                    "                " + createTime + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                " + configObj.warehouseData[obj.warehouseId] + "\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "               " + stateTips + " \n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td style='text-align: center'>\n";
                //库存状态
                if (obj.state == 0) {
                    html += "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.repair('" + obj.id + "')\">报修</a>" +
                        "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.scrap('" + obj.id + "')\">报废</a>";
                }
                //出库已完成的流程并且不能是报废((出库库存只能由使用人处理))
                if ((obj.state == -1 || obj.recordState==1) && obj.state != 2 && obj.state != 0 && obj.userId==user.id) {
                    html += "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.repair('" + obj.id + "')\">报修</a>" +
                        "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.return('" + obj.id + "')\">归还</a>" +
                        "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.scrap('" + obj.id + "')\">报废</a>";
                }
                //报修(仓库管理员只能操作自己发起的流程)
                if (obj.state == 1 && obj.operateUserId==user.id) {
                    if (obj.taskId != null && obj.taskId != "" && obj.recordState != 1) {
                        html += "<a href='javascript:void(0)' class='button-style'  onclick=\"productStockObj.showHistory('" + obj.foreignId + "',29)\">审核详情</a>";
                    }
                    if (obj.recordState == 0 || obj.recordState == -1) {
                        html += "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.viewRepair('" + obj.foreignId + "',0)\">编辑</a>" +
                            "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.delRepair('" + obj.foreignId + "')\">删除</a>";
                    }
                    if(obj.recordState==1){
                        html += "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.repair('" + obj.id + "')\">报修</a>" +
                            "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.scrap('" + obj.id + "')\">报废</a>";
                    }
                }
                //报废(仓库管理员只能操作自己发起的流程)
                if (obj.state == 2 && obj.operateUserId==user.id) {
                    if (obj.taskId != null && obj.taskId != "") {
                        html += "<a href='javascript:void(0)' class='button-style'  onclick=\"productStockObj.showHistory('" + obj.foreignId + "',30)\">审核详情</a>";
                    }
                    if (obj.recordState == 0 || obj.recordState == -1) {
                        html += "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.viewScrap('" + obj.foreignId + "',0)\">编辑</a>" +
                            "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.delScrap('" + obj.foreignId + "')\">删除</a>";
                    }
                }
                //归还
                if (obj.state == 3 && obj.operateUserId==user.id) {
                    if (obj.taskId != null && obj.taskId != "" && obj.recordState != 1) {
                        html += "<a href='javascript:void(0)' class='button-style'  onclick=\"productStockObj.showHistory('" + obj.foreignId + "',31)\">审核详情</a>";
                    }
                    if (obj.recordState == 0 || obj.recordState == -1) {
                        html += "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.viewReturn('" + obj.foreignId + "',0)\">编辑</a>" +
                            "<a href='javascript:void(0)' class='button-style' onclick=\"productStockObj.delReturn('" + obj.foreignId + "')\">删除</a>";
                    }
                }
                html += "</td></tr>";
            });
            $(".productStockList").append(html);
            //使用layui表单
            layui.use('form', function(){
                var $ = layui.jquery, form = layui.form;
                //全选
                form.on('checkbox(stockAllChoose)', function(data){
                    var child = $(data.elem).parents('.listContentOuter').find('tbody input[type="checkbox"]');
                    child.each(function(index, item){
                        item.checked = data.elem.checked;
                    });
                    form.render('checkbox');
                });
            });
            layui.form.render();
        }
    },
    init:function () {
        var formData = $("#productStockForm").serializeJson();
        commonObj.requestData(formData,productStockObj.getTotalUrl,"post","json",true,function (data) {
            if(data && data.code==200){
                commonObj.pagerPlus({
                    param: formData,
                    limits:[20,50,100,200],
                    limit:20,
                    elem: $(".productStockListPager"),
                    count:data.data.total,
                    url: productStockObj.productStockListUrl,
                }, productStockObj.callback);
            }
        });
    },
    //加载物品分类信息
    loadGoodsType:function(t) {
        layui.use("form",function (data) {
            var form = layui.form;
            configObj.listGoodsTypeData();
            var data = configObj.typeData;
            var html="<option value=''>--请选择--</option>";
            $(t).empty();
            for(var i=0;i<data.data.list.length;i++){
                var typeData=data.data.list[i];
                html+="<option value='"+typeData.id+"'>"+typeData.name+"</option>";
            }
            $(t).append(html);
            layui.form.render();
            form.on('select(goodsType)',function (data) {
                var typeId=data.value;
                configObj.listGoodsData(typeId);
                var goodsData=configObj.goodsData[typeId];
                $(configObj.layerObj[0]).find("select[name='goodsId']").empty();
                var typeHtml="<option value=''>请选择</option>";
                for(var i=0;i<goodsData.data.list.length;i++){
                    var id=goodsData.data.list[i].id;
                    var name=goodsData.data.list[i].name;
                    typeHtml+="<option value='"+id+"'>"+name+"</option>";
                }
                $(configObj.layerObj[0]).find("select[name='goodsId']").append(typeHtml);
                layui.form.render();
            });
        });
    },
    //核实表单数据
    checkFormData:function(){
        var typeId =$(configObj.layerObj[0]).find("select[name='typeId']").val();
        var goodsId=$(configObj.layerObj[0]).find("select[name='goodsId']").val();
        var number=$(configObj.layerObj[0]).find("input[name='number']").val();
        var warehouseId=$(configObj.layerObj[0]).find("select[name='warehouseId']").val();
        if(typeId==""){
            layer.open({
               title:"提示",
               content:"请选择产品分类"
            });
            return false;
        }
        if(goodsId==""){
            layer.open({
               title:"提示",
               content:"请选择产品名称"
            });
            return false;
        }
        if(number==""){
            layer.open({
                title:"提示",
                content:"请输入数量"
            });
            return false;
        }
        if(number<0){
            layer.open({
                title:"提示",
                content:"数量必须大于0"
            });
            return false;
        }
        if(warehouseId==""){
            layer.open({
                title:"提示",
                content:"请选择仓库"
            });
            return false;
        }
        return true;
    },
    submitHander:function(){
        if(productStockObj.checkFormData()){
            layer.confirm("是否提交库存？",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                  layer.close(index);
                  $(configObj.layerObj[0]).find("div[id='submitButton']").css("pointer-events","none");
                  var formData=$(configObj.layerObj[0]).find("form").serializeForm();
                  commonObj.requestData(formData,"/goods/addGoodsBatch","post","json",true,function (data) {
                      if(data.code==200){
                          $(configObj.layerObj[0]).find("div[id='submitButton']").css("pointer-events","none");
                          layer.msg(data.data.message,{time:1000,icon:6});
                          productStockObj.closeModal();
                          productStockObj.init();
                      }
                  });
            })
        }
    },
    printQrCode: function () {
        var $tbody = $(".productStockList");
        var $inputArr = $tbody.find('input[type="checkbox"]:checked');
        if($inputArr.length < 1){
            layer.msg("很抱歉，没有选中记录进行操作！", {time: 2000, icon: 5});
            return;
        }
        // 初始化一个zip打包对象
        var zip = new JSZip();
        $inputArr.each(function (i, input) {
            //获取库存编号
            var stockNum = $(input).closest("td").next().find(".ellipsisContent").text();
            var goodsName = $(input).closest("tr").find("td:eq(3)").find(".ellipsisContent").text();
            if(stockNum){
                stockNum = stockNum.trim(); //情况
                goodsName = goodsName.trim();
                qrcode.makeCode(stockNum+"    :    "+goodsName); //设置二维码值
                //下载二维码
                var imgNode = $("#qrcode").find("img");
                //添加二维码数据
                zip.file(stockNum+".jpg", productStockObj.dataURLtoBlob(imgNode.attr("src")), {base64: true});
            }
        });
        //生成zip文件并下载
        zip.generateAsync({type:"blob"}).then(function(content) {
            // 下载的文件名
            var filename = 'qrcode.zip';
            // 创建隐藏的可下载链接
            var eleLink = document.createElement('a');
            eleLink.download = filename;
            eleLink.style.display = 'none';
            // 下载内容转变成blob地址
            eleLink.href = URL.createObjectURL(content);
            // 触发点击
            document.body.appendChild(eleLink);
            eleLink.click();
            // 然后移除
            document.body.removeChild(eleLink);
        });
    },
    //将base64转换为blob
    dataURLtoBlob: function(dataurl) {
        var arr = dataurl.split(','),
            mime = arr[0].match(/:(.*?);/)[1],
            bstr = atob(arr[1]),
            n = bstr.length,
            u8arr = new Uint8Array(n);
        while (n--) {
            u8arr[n] = bstr.charCodeAt(n);
        }
        return new Blob([u8arr], { type: mime });
    },
    //库存详情弹框
    productStockModalShow: function (id) {
        productStockObj.modalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: 1,
            content: $("#productStockModal").html(),
            btn: [],
            area: ['86%', '75%'],
            offset:['100px','120px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                commonObj.requestData({id:id},"/goods/queryByInventoryId","post","json",true,function (data) {
                    var recordList = data.data.list;
                    if(recordList!=null && recordList.length>0){
                        for(var attr in recordList[0]){
                            $(layero[0]).find("[name="+attr+"]").val(recordList[0][attr]);
                            if(attr=="warehouseId"){
                                configObj.listWarehouseData();
                                $(layero[0]).find("[name='warehouseId']").val(configObj.warehouseData[recordList[0][attr]]);
                                $(layero[0]).find("[name='code']").attr("title",recordList[0]["code"]);
                            }
                            if(attr=="createTime"){
                                var createTime = recordList[0][attr];
                                $(layero[0]).find("[name='createTime']").val(new Date(createTime).format("yyyy-MM-dd hh:mm:ss"));
                            }
                            if(attr=="typeId"){
                                configObj.listGoodsTypeData();
                                var typeName = configObj.type[recordList[0][attr]];
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
                                    "<td style='text-align: center'>"+(item.operateName==undefined?"":item.operateName)+"</td>"+
                                    "<td style='text-align: center'>"+time+"</td>"+
                                    "<td style='text-align: center'>"+stateTips+"</td>"+
                                    "</tr>";
                                //出库状态，并且是往最后一条数据上加
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
                            $(layero[0]).find("table[id='stockCheckList'] tbody").html(html);
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
                                "<td style='text-align: center'>"+(userName==undefined?"":userName)+"</td>"+
                                "<td style='text-align: center'>"+createTime+"</td>"+
                                "<td style='text-align: center'>"+stateSpan+"</td>"+
                                "</tr>";
                            $(layero[0]).find("table[id='stockCheckList'] tbody").html(inventoryHtml);
                        }
                    }else{
                        $(layero[0]).find("table[id='stockCheckList'] tbody").html("");
                    }
                });
            }
        });
    },
    //新增库存
    viewInventoryModal: function () {
        configObj.layerObj={};
        configObj.inventoryCode=[];
        productStockObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#viewInventoryModal").html(),
            btn: [],
            area: ['85%', '65%'],
            offset:['70px','70px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj=layero;
                configObj.loadInventoryCode();
                $(layero[0]).find("input[name='code']").val(configObj.inventoryCode);
                productStockObj.loadGoodsType($(layero[0]).find("select[name='typeId']"));
                $("#viewInventoryForm input[name='createId']").val(user.id);
                $("#viewInventoryForm input[name='createName']").val(user.name);
                $("#viewInventoryForm input[name='createTime']").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                //加载仓库列表
                putStockObj.selectWarehouse($(layero[0]).find("select[name='warehouseId']"),null);
                //使用layui表单
                layui.form.render();
            }
        });
    },
    closeModal: function () {
        if(productStockObj.modalIndex){
            layer.close(productStockObj.modalIndex);
        }else {
            layer.closeAll();
        }
    },
    //关闭审核详情弹出层
    closeApproveDiv:function(){
        if(productStockObj.approveIndex){
            layer.close(productStockObj.approveIndex);
        }
    },
    //导出模板
    downTemplate:function () {
        location.href = "/inventoryStock/exportTemplate";
    },
    //导入库存
    importInventory:function () {
        fileUpload.upload();
    },
    //导出库存
    exportInventoryDetail:function () {
        var params = $("#productStockForm").serializeJson();
        location.href = "/inventoryStock/exportInventoryDetail" + "?" + $.param(params);
    },
    viewBtnClick:function (config) {
        $("#productStockModal").modal("toggle");
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render(); //先渲染，然后执行下面复选框事件才有效
        });

    },
    //关闭审核详情弹出窗
    closeApproveModal:function () {
        if(productStockObj.approveIndex){
            layer.close(productStockObj.approveIndex);
        }else{
            //如果你想关闭最新弹出的层，直接获取layer.index即可
            layer.close(layer.index);
        }
    },
    //自动生成报修编号
    loadRepairCode:function(t){
        if(configObj.repairCode.length==0){
            requestData(null,"/repair/getRepairCode","post","json",true,function (data) {
                if (data.code==200){
                    $(t[0]).find("input[name='code']").val(data.data.code);
                    configObj.repairCode=data.data.code;
                }
            })
        }else{
            $(t[0]).find("input[name='code']").val(configObj.repairCode);
        }
    },
    //自动生成报废编号
    loadScrapCode:function(t){
        if(configObj.scrapCode.length==0){
            requestData(null,"/scrap/getScrapCode","post","json",true,function (data) {
                if (data.code==200){
                    $(t[0]).find("input[name='code']").val(data.data.code);
                    configObj.scrapCode=data.data.code;
                }
            })
        }else{
            $(t[0]).find("input[name='code']").val(configObj.scrapCode);
        }
    },
    //自动生成归还编号
    loadReturnCode:function(t){
        if(configObj.returnCode.length==0){
            requestData(null,"/return/getReturnCode","post","json",true,function (data) {
                if (data.code==200){
                    $(t[0]).find("input[name='code']").val(data.data.code);
                    configObj.returnCode=data.data.code;
                }
            })
        }else{
            $(t[0]).find("input[name='code']").val(configObj.returnCode);
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
        productStockObj.approveIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 99999,
            content: $("#showHistoryModal2").html(),
            btn: [],
            area: ['66%', '60%'],
            offset:['120px','250px'],
            closeBtn: 0,
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
    //展示物品报修数据
    repair:function (id) {
        configObj.layerObj={};
        productStockObj.removeDisabledStyle();
        productStockObj.modalIndex = layer.open({
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
                                "<td style='text-align: center'>"+configObj.type[obj.typeId]+"</td>" +
                                "<td style='text-align: center'>"+obj.goodsName+"</td>" +
                                "<td style='text-align: center'>"+(obj.specs==""?"-":obj.specs)+"</td>" +
                                "<td style='text-align: center'>"+obj.unit+"</td>" +
                                "<td style='text-align: center'>"+obj.number+"</td>" +
                                "<td style='text-align: center'>"+(obj.returnTime==null?"无":obj.returnTime)+"</td>" +
                                "</tr>";
                        });
                        productStockObj.loadRepairCode(layero);
                        $(layero[0]).find("table[id='showDetailsTable'] tbody").append(html);
                    }
                });
            }
        });
    },
    //查看物品报修信息flag：0驳回，1查看，2审核
    viewRepair:function (id,flag) {
        configObj.layerObj={};
        productStockObj.modalIndex = layer.open({
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
                                                    "<td style='text-align: center'>"+configObj.type[obj.typeId]+"</td>" +
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
                    productStockObj.removeDisabledStyle();
                }else if(flag==1){
                    productStockObj.addDisabledStyle();
                }else{
                    productStockObj.addDisabledStyle();
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
                    configObj.repairCode=[];
                    productStockObj.init();
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                    productStockObj.closeModal();
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
        if(productStockObj.checkFormEmpty()){
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
                            if(productStockObj.saveRepairUrl==url){
                                configObj.repairCode=[];
                                productStockObj.closeModal();
                                productStockObj.init();
                                productStockObj.viewRepair(data.data.entity.id,0);
                            }else{
                                productStockObj.closeModal();
                                productStockObj.init();
                            }
                        }else {
                            productStockObj.closeModal();
                            productStockObj.init();
                            configObj.repairCode=[];
                        }
                        layer.msg(data.data.message,{icon:6,time:1000});
                    }else if(data.code==1002){
                        swal({
                            title:"提示",
                            text:data.msg
                        });
                        productStockObj.closeModal();
                    }
                });
            })
        }
    },
    //物品归还
    return:function (id) {
        configObj.layerObj={};
        productStockObj.removeDisabledStyle();
        productStockObj.modalIndex = layer.open({
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
                                "<td style='text-align: center'>"+configObj.type[obj.typeId]+"</td>" +
                                "<td style='text-align: center'>"+obj.goodsName+"</td>" +
                                "<td style='text-align: center'>"+(obj.specs==""?"-":obj.specs)+"</td>" +
                                "<td style='text-align: center'>"+obj.unit+"</td>" +
                                "<td style='text-align: center'>"+obj.number+"</td>" +
                                "<td style='text-align: center'>"+(obj.returnTime==null?"无":obj.returnTime)+"</td>" +
                                "</tr>";
                        });
                        productStockObj.loadReturnCode(layero);
                        $(layero[0]).find("table[id='showDetailsTable3'] tbody").append(html);
                    }
                });
            }
        });
    },
    //查看物品归还信息flag：0驳回，1查看，2审核
    viewReturn:function (id,flag) {
        configObj.layerObj={};
        productStockObj.modalIndex = layer.open({
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
                                                    "<td style='text-align: center'>"+configObj.type[obj.typeId]+"</td>" +
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
                    productStockObj.removeDisabledStyle();
                }else if(flag==1){
                    productStockObj.addDisabledStyle();
                }else{
                    productStockObj.addDisabledStyle();
                }
            }
        });
    },
    //提交归还申请
    submitReturn(t,url,state){//校验表格数据
        if(productStockObj.checkFormEmpty()){
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
                            if(productStockObj.saveReturnUrl==url){
                                configObj.returnCode=[];
                                productStockObj.closeModal();
                                productStockObj.init();
                                productStockObj.viewReturn(data.data.entity.id,0);
                            }else{
                                productStockObj.closeModal();
                                productStockObj.init();
                            }
                        }else {
                            productStockObj.closeModal();
                            productStockObj.init();
                            configObj.returnCode=[];
                        }
                        layer.msg(data.data.message,{icon:6,time:1000});
                    }else if(data.code==1002){
                        swal({
                            title:"提示",
                            text:data.msg
                        });
                        productStockObj.closeModal();
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
                    configObj.scrapCode=[];
                    productStockObj.init();
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                    productStockObj.closeModal();
                }
            });
        })

    },
    //物品报废
    scrap:function (id) {
        configObj.layerObj={};
        productStockObj.removeDisabledStyle();
        productStockObj.modalIndex = layer.open({
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
                                "<td style='text-align: center'>"+configObj.type[obj.typeId]+"</td>" +
                                "<td style='text-align: center'>"+obj.goodsName+"</td>" +
                                "<td style='text-align: center'>"+(obj.specs==""?"-":obj.specs)+"</td>" +
                                "<td style='text-align: center'>"+obj.unit+"</td>" +
                                "<td style='text-align: center'>"+obj.number+"</td>" +
                                "<td style='text-align: center'>"+(obj.returnTime==null?"无":obj.returnTime)+"</td>" +
                                "</tr>";
                        });
                        productStockObj.loadScrapCode(layero);
                        $(layero[0]).find("table[id='showDetailsTable2'] tbody").append(html);
                    }
                });
            }
        });
    },
    //查看物品报废信息flag：0驳回，1查看，2审核
    viewScrap:function (id,flag) {
        configObj.layerObj={};
        productStockObj.modalIndex = layer.open({
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
                                                    "<td style='text-align: center'>"+configObj.type[obj.typeId]+"</td>" +
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
                    productStockObj.removeDisabledStyle();
                }else if(flag==1){
                    productStockObj.addDisabledStyle();
                }else{
                    productStockObj.addDisabledStyle();
                }
            }
        });
    },
    //提交报废
    submitScrap(t,url,state){//校验表格数据
        if(productStockObj.checkFormEmpty()){
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
                            if(productStockObj.saveScrapUrl==url){
                                configObj.scrapCode=[];
                                productStockObj.closeModal();
                                productStockObj.init();
                                productStockObj.viewScrap(data.data.entity.id,0);
                            }else{
                                productStockObj.closeModal();
                                productStockObj.init();
                            }
                        }else {
                            productStockObj.closeModal();
                            productStockObj.init();
                            configObj.scrapCode=[];
                        }
                        layer.msg(data.data.message,{icon:6,time:1000});
                    }else if(data.code==1002){
                        swal({
                            title:"提示",
                            text:data.msg
                        });
                        productStockObj.closeModal();
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
                    configObj.scrapCode=[];
                    productStockObj.init();
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                    productStockObj.closeModal();
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

//库存盘点相关操作
var stockCheckObj = {
    //选择产品表单对象
    tempLayerObj:{},
    getTotalUrl:"/inventoryCheck/getPageCount",
    stockCheckListUrl: "/inventoryCheck/listPg",
    getGoodsTotalUrl:"/goods/getTotalAmount",
    goodsListUrl:"/goods/queryGoodsData",
    goodsLimit:50,
    modalIndex:null,
    productIndex:null,
    //库存盘点列表
    callback: function (data) {
        var html = "";
        var pageNum=data.pageNum;
        var pageSize=data.pageSize;
        $(".stockCheckList").empty();
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                html += "<tr>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    (pageIndex+1) +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\" style=\"cursor: pointer;color: #81CBFE;\" onclick='stockCheckObj.view("+record.id+",1)'>\n" +
                    record.code +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    record.title +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    (configObj.warehouseData[record.wareId]==undefined?"总仓库":configObj.warehouseData[record.wareId]) +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    record.createName +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    (new Date(record.createTime)).format("yyyy-MM-dd hh:mm:ss") +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    record.remark +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    // "                <a class='submitButton' href='javaScrap:void(0)' onclick='stockCheckObj.view("+record.id+",0)'>编辑</a>"+
                    "                <a class='submitButton' href='javaScrap:void(0)' onclick='stockCheckObj.delStockCheck("+record.id+")'>删除</a>"+
                    "            </div>\n" +
                    "        </td>\n" +
                    "   </tr>";
            });
        }
        $(".stockCheckList").append(html);
    },
    //库存盘点初始化
    init: function () {
        var formData = $("#stockCheckForm").serializeJson();
        commonObj.requestData(formData,stockCheckObj.getTotalUrl,"post","json",true,function (data) {
            if(data && data.code==200){
                commonObj.pagerPlus({
                    param: formData,
                    elem: $(".stockCheckListPager"),
                    count:data.data.total,
                    url: stockCheckObj.stockCheckListUrl,
                }, stockCheckObj.callback);
            }
        });
    },
    //选择产品数据列表
    goodsCallback: function (data) {
        var html = "";
        var pageNum=data.pageNum;
        var pageSize=data.pageSize;
        $(".productList").empty();
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                html += "<tr>\n" +
                    "        <td style='text-align: center'>\n" +
                    "             <input type=\"checkbox\" class='productId' name=\"productId\" value='"+record.id+"' lay-skin=\"primary\" lay-filter='radioInput'/>"+
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    (pageIndex+1) +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    record.goodsCode +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    record.goodsName +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    (record.specs==undefined?"-":record.specs) +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    record.unit +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    record.price +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    record.amount +
                    "            </div>\n" +
                    "        </td>\n" +
                    "   </tr>";
            });
        }
        $(".productList").append(html);
        layui.form.render();
        layui.use('form', function(){
            var $ = layui.jquery, form = layui.form;
            //全选
            form.on('checkbox(allChoose)', function(data){
                var child = $(data.elem).parents('table').find('tbody input[type="checkbox"]');
                child.each(function(index, item){
                    item.checked = data.elem.checked;
                });
                form.render('checkbox');
            });
        });
    },
    //选择产品初始化
    goodsInit: function (wareId) {
        var formData = $(stockCheckObj.tempLayerObj[0]).find("form").serializeJson();
        formData.wareId=wareId;
        commonObj.requestData(formData,stockCheckObj.getGoodsTotalUrl,"post","json",true,function (data) {
            if(data && data.code==200){
                commonObj.pagerPlus({
                    param: formData,
                    elem: $(".productListPager"),
                    count:data.data.total,
                    url: stockCheckObj.goodsListUrl,
                    limit:stockCheckObj.goodsLimit,
                    limits:[50,100,150],
                }, stockCheckObj.goodsCallback);
            }
        });
    },
    //已选中产品列表
    selectGoodsInit: function (wareId) {
        var formData=$(configObj.layerObj[0]).find("form[id='stockCheckForm']").serializeForm();
        // var checkId = $(configObj.layerObj[0]).find("input[name='id']").val();
        // formData.checkId=checkId;
        formData.ids=configObj.goodsIds;
        formData.wareId=wareId;
        stockCheckObj.removeDisabledStyle();
        requestData(formData,"/goods/getGoodsList","post","json",true,function (data) {
            var html="";
            $(".stockCheckList3").empty();
            if(data && data.list!=null){
                var pageNum=data.pageNum;
                var pageSize=data.pageSize;
                $(data.list).each(function (i, record) {
                    var pageIndex=(pageNum-1)*pageSize+i;
                    html += "<tr>" +
                        "<td style='text-align: center'>" + (pageIndex + 1) + "</td>" +
                        "<td style='text-align: center'>" + record.goodsCode + "</td>" +
                        "<td style='text-align: center'>" +
                        "<div title='"+record.goodsName+"' style='overflow:hidden;white-space:nowrap;text-overflow: ellipsis;'>"+record.goodsName+"</div>"+
                        "<input type='hidden' name='goodsId' class='goodsId form-control' value='" + record.id + "'/>" +
                        "</td>" +
                        "<td style='text-align: center'>" + (record.specs==undefined?"-":record.specs) + "</td>" +
                        "<td style='text-align: center'>" + record.price + "</td>" +
                        "<td style='text-align: center'>" + record.amount +
                        "<input type='hidden' name='stockAmount' class='stockAmount form-control' value='" + record.amount + "'/>" +
                        "</td>" +
                        "<td style='text-align: center'>" +
                        "<input type='number' name='checkAmount' class='checkAmount form-control' oninput='stockCheckObj.editProfitLoss(this)' onkeyup=\"value=value.replace(/[^\\d]/g,'')\"/>" +
                        "</td>" +
                        "<td style='text-align: center'>" +
                        "<input type='number' name='profitAmount' readonly class='profitAmount form-control' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" value='0'/>" +
                        "</td>" +
                        "<td style='text-align: center'>" +
                        "<input type='number' name='lossAmount' readonly class='lossAmount form-control' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" value='0'/>" +
                        "</td>" +
                        "<td style='text-align: center'>" +
                        "<input type='text' name='remark2' class='remark form-control'/>" +
                        "</td>" +
                        "<td style='text-align: center'>" +
                        "<button type=\"button\" data-id='"+record.id+"' class=\"delBtn btn btn-white btn-xs\" onclick=\"stockCheckObj.removeCheck(this);\">" +
                        "<i class=\"glyphicon glyphicon-minus\"></i>" +
                        "</button>" +
                        "</td>" +
                        "</tr>";
                });
                $(".stockCheckList3").append(html);
            }
        });
    },
    //展示新增盘点单页面
    addStockCheckShow: function () {
        configObj.goodsIds=[];
        configObj.layerObj={};
        $(".save").show();
        $(".edit").hide();
        $(".view").hide();
        $(".modalTableTitle").show();
        $(".stockCheckList3").empty();
        stockCheckObj.removeDisabledStyle();
        stockCheckObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#stockCheckModal").html(),
            btn: [],
            area: ['85%', '75%'],
            offset:['70px','70px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj=layero;
                configObj.loadStockCheckCode();
                putStockObj.selectWarehouse($(layero[0]).find("select[name='wareId']")[0],null);
                $("#stockCheckFrom input[name='code']").val(configObj.stockCheckCode);
                $("#stockCheckFrom input[name='createId']").val(user.id);
                $("#stockCheckFrom input[name='createName']").val(user.name);
                $("#stockCheckFrom input[name='createTime']").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                //使用layui表单
                layui.form.on("select(wareIdFilter)",function (data) {
                    var wareId=data.value;
                    stockCheckObj.selectGoodsInit(wareId);
                });
                layui.form.render();
            }
        });
    },
    //展示选择产品页面
    addProductModalShow: function () {
        var wareId=$(configObj.layerObj[0]).find("select[name='wareId']").val();
        stockCheckObj.tempLayerObj={};
        $(".productList").html("");
        stockCheckObj.productIndex = layer.open({
            type: 1,
            title: false,
            zIndex: -90000,
            content: $("#selectProductModal").html(),
            btn: [],
            area: ['75%', '65%'],
            offset:['100px','120px'],//左上
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                stockCheckObj.tempLayerObj=layero;
                productStockObj.loadGoodsType($(layero[0]).find("select[name='typeIdQc']"));
                if(wareId==""){
                    $(layero[0]).find("input[name='warehouseName']").val("总仓库");
                    $(layero[0]).find("input[name='warehouseId']").val("");
                }else{
                    configObj.listWarehouseData();
                    $(layero[0]).find("input[name='warehouseName']").val(configObj.warehouseData[wareId]);
                    $(layero[0]).find("input[name='warehouseId']").val(wareId);
                }
                //选择产品
                stockCheckObj.goodsInit(wareId);
                var form = layui.form;
                form.on('select(typeIdQc)', function(data){
                    stockCheckObj.goodsInit(wareId);
                });
                //使用layui表单
                layui.form.render();
            }
        });
    },
    //选择产品保存
    selectProduct:function(){
        var ids=$(stockCheckObj.tempLayerObj[0]).find("input[name='productId']:checked");
        if(ids.length>0){
            layer.confirm("确定盘点这些产品？",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                for(var i=0;i<ids.length;i++){
                    if(!configObj.goodsIds.contains(ids[i].value)){
                        configObj.goodsIds.push(ids[i].value);
                    }
                }
                var wareId=$(stockCheckObj.tempLayerObj[0]).find("input[name='warehouseId']").val();
                stockCheckObj.selectGoodsInit(wareId);
                stockCheckObj.closeProductModal();
            });
        }else{
            layer.open({
                title:"提示",
                content:"请选择产品"
            });
        }
    },
    //库存盘点修改时，修改盘盈或者盘亏数量
    editProfitLoss:function(t){
       var $trElem=$(t).parent().parent();
        //库存数量
        var stockAmount=$trElem.find("td:eq(5) input").val();
        //盘点数量
        var checkAmount=$(t).val();
        if(checkAmount>=0 && checkAmount!=""){
            var amount=checkAmount-stockAmount;
            if(amount>0){
                //盘盈数量
                $trElem.find("td:eq(7) input").val(amount);
                //盘亏数量
                $trElem.find("td:eq(8) input").val(0);
            }else {
                //盘盈数量
                $trElem.find("td:eq(7) input").val(0);
                //盘亏数量
                $trElem.find("td:eq(8) input").val(-amount);
            }
        }else{
            //盘盈数量
            $trElem.find("td:eq(7) input").val(0);
            //盘亏数量
            $trElem.find("td:eq(8) input").val(0);
        }
    },
    //校验表单数据
    checkFormData:function(){
        var flag=true;
        var title=$(configObj.layerObj[0]).find("input[name='title']").val().trim();
        if(title==""){
            layer.open({
               title:"提示",
               content:"标题不能为空"
            });
            flag=false;
            return false;
        }
        var tableElem=$(configObj.layerObj[0]).find("table[id='selectProductTable'] tbody").find("tr");
        if(tableElem.length>0) {
            for (var i = 0; i < tableElem.length; i++) {
                var checkAmount = $(tableElem[i]).find("td:eq(6) input").val();
                var profitAmount = $(tableElem[i]).find("td:eq(7) input").val();
                var lossAmount = $(tableElem[i]).find("td:eq(8) input").val();
                if (checkAmount == "") {
                    layer.open({
                        title: "提示",
                        content: "盘点数量不能为空"
                    });
                    flag = false;
                    return false;
                }
                if (checkAmount < 0) {
                    layer.open({
                        title: "提示",
                        content: "盘点数量不能小于0"
                    });
                    flag = false;
                    $(tableElem[i]).find("td:eq(6) input").val(0);
                    return false;
                }
                if (profitAmount == "") {
                    layer.open({
                        title: "提示",
                        content: "盘盈数量不能为空"
                    });
                    flag = false;
                    $(tableElem[i]).find("td:eq(7) input").val(0);
                    return false;
                }
                if (profitAmount < 0) {
                    layer.open({
                        title: "提示",
                        content: "盘盈数量不能小于0"
                    });
                    flag = false;
                    $(tableElem[i]).find("td:eq(7) input").val(0);
                    return false;
                }
                if (lossAmount == "") {
                    layer.open({
                        title: "提示",
                        content: "盘亏数量不能为空"
                    });
                    flag = false;
                    $(tableElem[i]).find("td:eq(8) input").val(0);
                    return false;
                }
                if (lossAmount < 0) {
                    layer.open({
                        title: "提示",
                        content: "盘亏数量不能小于0"
                    });
                    flag = false;
                    $(tableElem[i]).find("td:eq(8) input").val(0);
                    return false;
                }
            }
        }else{
            layer.open({
                title:"提示",
                content:"请选择盘点产品"
            });
            flag=false;
            return false;
        }
        return flag;
    },
    //添加盘点单
    submitStockCheck:function(t,url){
        if(stockCheckObj.checkFormData()){
            layer.confirm("确定保存盘点单？",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                $(configObj.layerObj[0]).find("div[id='addCheckBtn']").css("pointer-events","none");
                var formData = new FormData($(configObj.layerObj[0]).find("form")[0]);//序列化当前表单，并传出file类型
                $.ajax({
                    type: "post",
                    url: "/inventoryCheck/saveInventoryCheck",
                    data: formData,
                    dataType: "json",
                    async: true,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            $(configObj.layerObj[0]).find("div[id='addCheckBtn']").css("pointer-events","auto");
                            //提交清空盘点编码缓存
                            configObj.stockCheckCode=[];
                            stockCheckObj.closeModal();
                            stockCheckObj.init();
                        } else if (data.code == 1002) {
                            swal({
                                title: "提示",
                                text: data.msg,
                            });
                            stockCheckObj.closeModal();
                        }
                    }
                });
            });
        }
    },
    //添加disable属性
    addDisabledStyle:function(){
        $("#stockCheckFrom [name='title']").attr("disabled", true);
        $(".checkWareId").attr("disabled", true);
        $("#stockCheckFrom [name='file']").attr("disabled", true);
        $("#stockCheckFrom [name='remark']").attr("disabled", true);
        $("#stockCheckFrom [name='checkAmount']").attr("disabled", true);
        $("#stockCheckFrom [name='remark2']").attr("disabled", true);
        $("#stockCheckFrom .delBtn").attr("disabled", true);
        $("#stockCheckFrom .layui-form-select").addClass("layui-select-disabled");
        $("#stockCheckFrom .layui-select-title input").addClass("layui-disabled");
    },
    //移除disable属性
    removeDisabledStyle:function(){
        $("#stockCheckFrom [name='title']").attr("disabled", false);
        $(".checkWareId").attr("disabled", false);
        $("#stockCheckFrom [name='file']").attr("disabled", false);
        $("#stockCheckFrom [name='remark']").attr("disabled", false);
        $("#stockCheckFrom [name='checkAmount']").attr("disabled", false);
        $("#stockCheckFrom [name='remark2']").attr("disabled", false);
        $("#stockCheckFrom .delBtn").attr("disabled", false);
        $("#stockCheckFrom .layui-form-select").removeClass("layui-select-disabled");
        $("#stockCheckFrom .layui-select-title input").removeClass("layui-disabled");
    },
    //flag0编辑，1查看（编辑未做，编辑的库存是实时的，但是盘点单记录了库存数量（可以删除后再新增））
    view:function(id,flag){
        configObj.goodsIds=[];
        stockCheckObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#stockCheckModal").html(),
            btn: [],
            area: ['85%', '75%'],
            offset:['70px','70px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                configObj.layerObj=layero;
                $(".stockCheckList3").empty();
                requestData({id:id},"/inventoryCheck/editAjax","post","json",true,function (data) {
                    if(data.code==200){
                        if(data && data.data.entity!=null){
                            for(var attr in data.data.entity){
                                $(configObj.layerObj[0]).find("[name='"+attr+"']").val(data.data.entity[attr]);
                                if(attr=="wareId"){
                                    putStockObj.selectWarehouse($(configObj.layerObj[0]).find("[name='wareId']"),data.data.entity[attr]);
                                }
                                if(attr=="affixName"){
                                    $(configObj.layerObj[0]).find(".affixDiv").text(data.data.entity[attr]);
                                }
                                if(attr=="affixLink"){
                                    $(configObj.layerObj[0]).find(".affixDiv").attr("href",data.data.entity[attr]);
                                }
                                if(attr=="details"){
                                    var html="";
                                    var details=data.data.entity[attr];
                                    $(details).each(function (i,record) {
                                        if(flag==0){
                                            configObj.goodsIds.push(record.goodsId);
                                        }
                                        //加载库存盘点明细列表
                                        var checkAmount=record.checkAmount==undefined?"":record.checkAmount;
                                        var profitAmount=record.profitAmount==undefined?0:record.profitAmount;
                                        var lossAmount=record.lossAmount==undefined?0:record.lossAmount;
                                        var remark=record.remark==undefined?"":record.remark;
                                        html += "<tr>" +
                                            "<td style='text-align: center'>" + (i + 1) + "</td>" +
                                            "<td style='text-align: center'>" + record.goodsCode + "</td>" +
                                            "<td style='text-align: center'>" +
                                            "<div title='"+record.goodsName+"' style='overflow:hidden;white-space:nowrap;text-overflow: ellipsis;'>"+record.goodsName+"</div>"+
                                            "<input type='hidden' name='goodsId' class='goodsId form-control' value='" + record.goodsId + "'/>" +
                                            "</td>" +
                                            "<td style='text-align: center'>" + (record.specs==undefined?"-":record.specs) + "</td>" +
                                            "<td style='text-align: center'>" + record.price + "</td>" +
                                            "<td style='text-align: center'>" + record.stockAmount +
                                            "<input type='hidden' name='stockAmount' class='stockAmount form-control' value='" + record.stockAmount + "'/>" +
                                            "</td>" +
                                            "<td style='text-align: center'>" +
                                            "<input type='number' name='checkAmount' class='checkAmount form-control' value='"+checkAmount+"' oninput='stockCheckObj.editProfitLoss(this)' onkeyup=\"value=value.replace(/[^\\d]/g,'')\"/>" +
                                            "</td>" +
                                            "<td style='text-align: center'>" +
                                            "<input type='number' name='profitAmount' readonly class='profitAmount form-control' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" value='"+profitAmount+"'/>" +
                                            "</td>" +
                                            "<td style='text-align: center'>" +
                                            "<input type='number' name='lossAmount' readonly class='lossAmount form-control' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" value='"+lossAmount+"'/>" +
                                            "</td>" +
                                            "<td style='text-align: center'>" +
                                            "<input type='text' name='remark2' class='remark form-control' value='"+remark+"'/>" +
                                            "</td>" +
                                            "<td style='text-align: center'>" +
                                            "<button type=\"button\" data-id='"+record.goodsId+"' class=\"delBtn btn btn-white btn-xs\" onclick=\"stockCheckObj.removeCheck(this);\">" +
                                            "<i class=\"glyphicon glyphicon-minus\"></i>" +
                                            "</button>" +
                                            "</td>" +
                                            "</tr>";
                                    });
                                    $(".stockCheckList3").append(html);
                                }
                            }
                        }
                        if(flag==0){
                            //编辑
                            $(".edit").show();
                            $(".save").hide();
                            $(".view").hide();
                            $(".modalTableTitle").show();
                            stockCheckObj.removeDisabledStyle();
                            //使用layui表单
                            layui.form.on("select(wareIdFilter)",function (data) {
                                var wareId=data.value;
                                stockCheckObj.selectGoodsInit(wareId);
                            });
                        }else{
                            //查看
                            $(".edit").hide();
                            $(".save").hide();
                            $(".view").show();
                            $(".modalTableTitle").hide();
                            stockCheckObj.addDisabledStyle();
                        }
                    } else if (data.code == 1002) {
                        swal({
                            title: "提示",
                            text: data.msg,
                        });
                    }
                });
                layui.form.render();
            }
        });
    },
    //删除库存盘点
    delStockCheck:function(id){
        layer.confirm("确定删除盘点单？",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: "/inventoryCheck/delInventoryCheck",
                data: {id:id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        stockCheckObj.init();
                    } else if (data.code == 1002) {
                        swal({
                            title: "提示",
                            text: data.msg,
                        });
                    }
                }
            });
        });
    },
    removeCheck:function(t){
        var goodsId=$(t).attr("data-id");
        var index =  configObj.goodsIds.indexOf(goodsId.toString())==-1? configObj.goodsIds.indexOf(Number(goodsId)): configObj.goodsIds.indexOf(goodsId.toString());
        if(index>-1){
            configObj.goodsIds.splice(index,1);
        }
        $(t).parent().parent().remove();
        var tableLength = $("#selectProductTable tbody").find("tr").length;
        for (var i = 1; i <= tableLength; i++) {
            $(configObj.layerObj[0]).find("#selectProductTable tbody").find("tr:eq("+(i-1)+") td:first").text(i);
        }
    },
    closeModal: function () {
        if (stockCheckObj.modalIndex) {
            layer.close(stockCheckObj.modalIndex);
        } else {
            layer.closeAll();
        }
    },
    closeProductModal: function () {
        if (stockCheckObj.productIndex) {
            layer.close(stockCheckObj.productIndex);
        }
    },
    //库存盘点导出
    exportStockCheck:function () {
        var params = $("#stockCheckForm").serializeJson();
        location.href = "/inventoryCheck/exportStockCheck?"+ $.param(params);
    }
}

//库存预警相关操作
var stockForeWarnObj={
    getTotalUrl:"/checkDetails/getPageCount",
    stockForeWarnListUrl: "/checkDetails/listPg",
    modalIndex:null,
    //库存预警列表
    callback: function (data) {
        var html = "";
        var pageNum=data.pageNum;
        var pageSize=data.pageSize;
        $(".stockForeWarnList").empty();
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                var amount=record.amount;
                var maxAmount=record.stockMaxAmount==undefined?0:record.stockMaxAmount;
                var minAmount=record.stockMinAmount==undefined?0:record.stockMinAmount;
                var tips="";
                if(amount<=minAmount && minAmount>0){
                    tips="<span class='text-red'>库存不足</span>";
                }else if(amount>=maxAmount && maxAmount>0){
                    tips="<span class='text-red'>库存过多</span>";
                }else if((amount>minAmount && minAmount>0) || (amount<maxAmount && maxAmount>0)){
                    tips="<span>库存正常</span>";
                }else{
                    tips="";
                }
                //仓库configObj.warehouseData[record.wareId]
                html += "<tr onmouseover='commonObj.mouseOver(this)' onmouseout='commonObj.mouseOut(this)'>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +(pageIndex+1)+"</div>\n"+
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +record.code+"</div>\n"+
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +configObj.type[record.typeId]+"</div>\n"+
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +record.goodsName+"</div>\n"+
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +(record.specs==undefined?"":record.specs)+"</div>\n"+
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +record.unit+"</div>\n"+
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +record.amount+"</div>\n"+
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +tips+"</div>\n"+
                    "        </td>\n" +
                    "        <td style='text-align: center'>\n" +
                    "            <div data-id='"+record.goodsId+"' data-value=''>\n" +
                    "                <input type=\"text\" readonly class=\"editInput\" value='"+maxAmount+"' onkeyup=\"value=value.replace(/[^\\d]/g,'')\">" +
                    "                <button class=\"editButton\" style='margin-left: 5px' type=\"button\" onclick=\"stockForeWarnObj.editStockMaxValue(this);\"><i class=\"fa fa-edit\"></i></button>" +
                    "                <button class=\"submitButton2\" style='margin-left: 5px' type=\"button\" onclick=\"stockForeWarnObj.submitStockMaxValue(this);\">确定</button>" +
                    "            </div>" +
                    "        </td>\n" +
                    "        <td style='text-align: center'>\n" +
                    "            <div data-id='"+record.goodsId+"' data-value=''>\n" +
                    "                <input type=\"text\" readonly class=\"editInput\" value='"+minAmount+"' onkeyup=\"value=value.replace(/[^\\d]/g,'')\">" +
                    "                <button class=\"editButton\" style='margin-left: 5px' type=\"button\" onclick=\"stockForeWarnObj.editStockMinValue(this);\"><i class=\"fa fa-edit\"></i></button>" +
                    "                <button class=\"submitButton2\" style='margin-left: 5px' type=\"button\" onclick=\"stockForeWarnObj.submitStockMinValue(this);\">确定</button>" +
                    "            </div>" +
                    "        </td>\n" +
                    "   </tr>";
            });
        }
        $(".stockForeWarnList").append(html);
    },
    //库存预警初始化
    init: function () {
        var formData = $("#stockForeWareForm").serializeJson();
        commonObj.requestData(formData,stockForeWarnObj.getTotalUrl,"post","json",true,function (data) {
            if(data && data.code==200){
                commonObj.pagerPlus({
                    param: formData,
                    elem: $(".stockForeWarnPager"),
                    limits:[50,100,200,500],
                    limit:50,
                    count:data.data.total,
                    url: stockForeWarnObj.stockForeWarnListUrl,
                }, stockForeWarnObj.callback);
            }
        });
    },
    //修改库存最大值
    editStockMaxValue: function (t) {
        var $parentDiv = $(t).parent();
        var editValue=parseInt($parentDiv.find(".editInput").val());
        $parentDiv.attr("data-value", editValue); //缓存编辑值
        $parentDiv.find(".editButton").css("display", "none");
        $parentDiv.find(".editInput").css("border", "1px solid #E6E6E6");
        $parentDiv.find(".editInput").removeAttr("readonly");
        $parentDiv.find(".editInput").focus();
        $parentDiv.find(".submitButton2").css("display", "inline-block");
    },
    //提交库存最大值
    submitStockMaxValue: function (t) {
        var $parentDiv = $(t).parent();
        //1、比较修改后的值是否一致，一致则不需要进行发送请求进行编辑
        //编辑时的缓存值
        var editMaxValue=$parentDiv.attr("data-value");
        var minValue=$(t).parent().parent().next().find(".editInput").val();
        //实际填写的库存值
        if($parentDiv.find(".editInput").val()==""){
            layer.open({
                title:"提示",
                content:"库存最大值不能为空"
            });
            $parentDiv.find(".editInput").val(0);
            return;
        }
        var writeMaxValue=parseInt($parentDiv.find(".editInput").val());
        if(writeMaxValue<minValue){
            layer.open({
                title:"提示",
                content:"库存最大值必须大于库存最小值"
            });
            return;
        }
        if(editMaxValue!=writeMaxValue){
            //2、发送请求到后台进行修改
            var id=$parentDiv.attr("data-id");
            requestData({id:id,stockMaxAmount:writeMaxValue},"/goodsType/editStockAmount","post","json",true,function (data) {
                if(data.code==200){
                    layer.msg(data.data.message,{time:1000,icon:6});
                    //3、判断请求是否失败，失败的话还原成原来的值，成功把title进行修改
                    $parentDiv.attr("title", writeMaxValue);
                    $parentDiv.find(".editInput").val(writeMaxValue);
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                    $parentDiv.attr("title", editMaxValue);
                    $parentDiv.find(".editInput").val(editMaxValue);
                }
            });
            stockForeWarnObj.init();
        }else{
            $parentDiv.find(".editInput").val(editMaxValue);
        }
        //4、还原控件成不可编辑状态
        $parentDiv.find(".editButton").css("display", "inline-block");
        $parentDiv.find(".editInput").css("border", "unset");
        $parentDiv.find(".editInput").attr("readonly", true);
        $parentDiv.find(".submitButton2").css("display", "none");
    },
    //修改库存最小值
    editStockMinValue: function (t) {
        var $parentDiv = $(t).parent();
        var editValue=parseInt($parentDiv.find(".editInput").val());
        $parentDiv.attr("data-value", editValue); //缓存编辑值
        $parentDiv.find(".editButton").css("display", "none");
        $parentDiv.find(".editInput").css("border", "1px solid #E6E6E6");
        $parentDiv.find(".editInput").removeAttr("readonly");
        $parentDiv.find(".editInput").focus();
        $parentDiv.find(".submitButton2").css("display", "inline-block");
    },
    //提交库存最小值
    submitStockMinValue: function (t) {
        var $parentDiv = $(t).parent();
        //1、比较修改后的值是否一致，一致则不需要进行发送请求进行编辑
        //编辑时的缓存值
        var editMinValue=$parentDiv.attr("data-value");
        var maxValue=$(t).parent().parent().prev().find(".editInput").val();
        //实际填写的库存值
        if($parentDiv.find(".editInput").val()==""){
            layer.open({
                title:"提示",
                content:"库存最小值不能为空"
            });
            $parentDiv.find(".editInput").val(0);
            return;
        }
        var writeMinValue=parseInt($parentDiv.find(".editInput").val());
        if(writeMinValue>maxValue){
            layer.open({
                title:"提示",
                content:"库存最小值必须小于库存最大值"
            });
            return;
        }
        if(editMinValue!=writeMinValue){
            //2、发送请求到后台进行修改
            var id=$parentDiv.attr("data-id");
            requestData({id:id,stockMinAmount:writeMinValue},"/goodsType/editStockAmount","post","json",true,function (data) {
                if(data.code==200){
                    layer.msg(data.data.message,{time:1000,icon:6});
                    //3、判断请求是否失败，失败的话还原成原来的值，成功把title进行修改
                    $parentDiv.attr("title", writeMinValue);
                    $parentDiv.find(".editInput").val(writeMinValue);
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    });
                    $parentDiv.attr("title", editMinValue);
                    $parentDiv.find(".editInput").val(editMinValue);
                }
            });
            stockForeWarnObj.init();
        }else{
            $parentDiv.find(".editInput").val(editMinValue);
        }
        //4、还原控件成不可编辑状态
        $parentDiv.find(".editButton").css("display", "inline-block");
        $parentDiv.find(".editInput").css("border", "unset");
        $parentDiv.find(".editInput").attr("readonly", true);
        $parentDiv.find(".submitButton2").css("display", "none");
    },
    closeModal:function () {
        if(stockForeWarnObj.modalIndex){
            layer.close(stockForeWarnObj.modalIndex);
        }else {
            layer.closeAll();
        }
    },
    exportForeWarning:function () {
        var params = $("#stockForeWareForm").serializeJson();
        if(params.foreWareIdQc==undefined || params.foreWareIdQc==null || params==""){
            params.foreWareIdQc=null;
        }
        location.href = "/checkDetails/exportForeWarning?"+ $.param(params);
    }
}

/**
 * 加载未入库的采购订单
 * @param url 采购订单数据接口url
 * @param t  关联采购订单所在元素
 * @param id 采购id
 * @param code 采购订单编号
 */
function loadOrderList(url,t,id,code) {
    var selected = null;
    if(id!=null){
        selected = new Array();
        var temp = {id:id, text:code};
        selected.push(temp);
    }
    var options = {
        url: url,      // 数据接口url
        size: 30, // 每次加载的数据条数
        value: "id",  // 下拉框value字段名称
        name: ["purchaseCode"], // 下拉框显示字段名称
        selected: selected,  // 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
        params: {id:id,code:null},
        placeholder: "请选择采购订单",
        outboundFlag:1 //采购标志
    };
    ajaxSelect2($(t), options);
}

/**
 * 加载未入库的领用订单
 * @param url 领用订单数据接口url
 * @param t  关联领用订单所在元素
 * @param id 领用id
 * @param code 领用编号
 */
function loadOutOrderList(url,t,id,code) {
    var selected = null;
    if(id!=null){
        selected = new Array();
        var temp = {id:id, text:code};
        selected.push(temp);
    }
    var options = {
        url: url,      // 数据接口url
        size: 30, // 每次加载的数据条数
        value: "id",  // 下拉框value字段名称
        name: ["applyCode"], // 下拉框显示字段名称
        selected: selected,  // 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
        params: {id:id,code:null},
        placeholder: "请选择领用订单",
        outboundFlag:0  //领用标志
    };
    ajaxSelect2($(t), options);
}

/**
 * ajax获取select2下拉框数据（带鼠标滚动分页）
 * @param obj 下拉框对象；
 * @param options 选项，包含如下字段：
 * url 数据接口url
 * size 每次加载的数据条数
 * name 下拉框显示字段名称
 * value 下拉框value字段名称
 * placeholder 默认显示的文字
 * outboundFlag 出入库标志 1采购 -1领用
 * selected 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
 * formatResult 返回结果回调函数，可以在该回调中，自定义下拉框数据的显示样式，比如：加入图片等
 * templateSelection 选中项回调，该参数必须与formatResult参数搭配使用
 * 注意点1 : 后端接口需返回 data（具体数据）和 total（总页数）两个字段
 * 注意点2 : 两个自定义的回调函数中，必须要把处理结果return回来，如果没有传入formatResult参数，则采用默认的显示样式
 */
function ajaxSelect2(obj, options, formatResult, formatSelected) {
    var value = options["value"];
    var name = options["name"];
    var outboundFlag = options["outboundFlag"];
    var flag = (typeof formatResult === "function") ? true : false;
    var select2Option = {
        language: "zh-CN",
        allowClear: true,
        placeholder: options["placeholder"] || "请选择",
        ajax: {
            url: options["url"],
            type: "post",
            dataType: "json",
            delay: 250,
            data: function (params) {
                var optionParams = options["params"];
                // 搜索框内输入的内容
                optionParams.name = params.term;
                // 当前页
                optionParams.page = params.page || 1;
                // 每页显示多少条记录，默认10条
                optionParams.size = options["size"] || 10;
                // 传递到后端的参数
                return optionParams;
            },
            cache: true,
            processResults: function (res, params) {
                params.page = params.page || 1;
                var cbData = [];
                if (flag) {
                    cbData = res.list;
                } else {
                    if (res.pages >= params.page) {
                        var data = res.list;
                        var len = data.length;
                        var text;
                        for (var i = 0; i < len; i++) {
                            text = data[i][name[0]];
                            for (var j = 1; j < name.length; j++) {
                                text += "(" + data[i][name[j]] + ")"
                            }
                            cbData.push({"id": data[i][value], "text": text});
                            if(outboundFlag == 1){
                                tableData[data[i].purchaseCode]=data[i];
                            }else{
                                applyData[data[i].applyCode]=data[i];
                            }
                        }
                    }
                }
                return {
                    results: cbData,
                    pagination: {
                        more: params.page < res.total
                    }
                };
            }
        },
        escapeMarkup: function (markup) {
            // 字符转义处理
            return markup;
        },
        // 最少输入N个字符才开始检索，如果想在点击下拉框时加载数据，请设置为 0
        minimumInputLength: 0,
        // 多选 - 设置最多可以选择多少项
        maximumSelectionLength:2,
    };
    if (flag) {
        select2Option.templateResult = formatResult;
        select2Option.templateSelection = formatSelected;
    }
    var $select = obj;
    $select.select2(select2Option);
    //选择下拉列表值触发
    $select.on("select2:select",function(e){
        var values = $select.val();
        var currentId = e.params.data.id;
        if(outboundFlag==1){
            //如果已有采购明细,则删除前者
            if(tableDataHtml){
                $(".stockCheckList").html("")
            }
            //入库明细增加
            putStockObj.addWarehouseFrom(currentId);
        }else{
            //如果已有采购明细,则删除前者
            if(applyHtml){
                $(".stockCheckList2").html("")
            }
            //入库明细增加
            outStockObj.addApplyDetailsHtml(currentId);
        }
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render('select');
        });
    });

    if (!flag) {
        // 默认选中项设置
        var html = '';
        var values = [];
        var selected = options['selected'];
        if (selected) {
            $.each(selected, function (index, item) {
                values.push(item.id);
                html += '<option value="' + item.id + '">' + item.text + '</option>';
            });
            $select.append(html);
            $select.val(values).trigger('change');
        }
    }
}

//刷新表格
function reflushTable() {
    //刷新表格
    $("#putStock").emptyGridParam(); //清空历史查询数据
    $("#putStock").jqGrid('setGridParam', {
        postData: $("#putStockForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}