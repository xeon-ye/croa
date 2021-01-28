var configObj={
    //采购新增按钮Id
    purchaseElemId:"savePurchaseBtn",
    layerObj:{},//物品采购表单对象
    layerSupplierObj:{},//物品供应商表单对象
    payMethod:{0:"微信",1:"支付宝",2:"银行卡"},
    purchaseIds:[],
    selectedIds:[],//采购已经选中的id，产品展示时移除
    tempData:{
        type:[],//产品分类缓存数据
        goods:{},//产品缓存数据
        goodsDetail:{},//产品详情缓存数据
        purchaseCode:[],//采购编码缓存数据
        supplier:[],//供应商数据
        users:[],//用户数据
    },
    //产品分类缓存数据
    listGoodsTypeData:function () {
        if(configObj.tempData.type && configObj.tempData.type.length<=0){
            requestData(null,"/goodsType/loadGoodsTypeInfo","post","json",false,function (data) {
                configObj.tempData.type = data;
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
    listGoodsDetailData:function(goodsId){
        if(configObj.tempData.goodsDetail[goodsId]==null || configObj.tempData.goodsDetail[goodsId]=="" || configObj.tempData.goodsDetail[goodsId]==undefined){
            requestData({id:goodsId},"/goodsType/editAjax","post","json",false,function (data) {
                configObj.tempData.goodsDetail[goodsId] = data;
            });
        }
    },
    loadSupplierData:function () {
        if(configObj.tempData.supplier.length<=0){
            requestData(null,"/purchaseSupplier/getSupplierList","post","json",false,function (data) {
                configObj.tempData.supplier=data;
            });
        }
    },
    loadUserData:function () {
        if(configObj.tempData.users.length<=0){
            requestData({companyCode:user.companyCode},"/propose/listByForum","post","json",false,function (data) {
                configObj.tempData.users=data;
            });
        }
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
        element.on('tab(docDemoTabBrief)', function(data){
            //如果不点击搜索按钮，进行处理
            if($(data.elem.context).find("input").length<1){
                $("#tabIndex").val(data.index);
                commonObj.tabChange(data.index);
            }
        });
    });

    //flag=0:驳回,编辑1:查看,2:审核
    if (getQueryString("flag") != null && getQueryString("flag") != '' && getQueryString("flag") != undefined) {
        var flag = getQueryString("flag");
        var id = getQueryString("id");
        if (flag == 0) {
            buyingManageObj.view(id, 0);
        } else if (flag == 1) {
            buyingManageObj.view(id, 1);
        } else if (flag == 2) {
            buyingManageObj.view(id, 2);
        }
    }

    //采购明细点击添加事件
    $(document).on('click',"#btn_addCheck",function (data) {
        buyingManageObj.addCheck(configObj.layerObj);
    });

    layui.use('form', function(){
        var $ = layui.jquery, form = layui.form;
        //全选
        form.on('checkbox(allChoose)', function(data){
            var child = $(data.elem).parents('table').find('tbody input[type="checkbox"]');
            child.each(function(index, item){
                if(!item.disabled){
                    item.checked = data.elem.checked;
                }
            });
            form.render('checkbox');
        });
        form.on('select(levelQc)', function(data){
            refreshTableData();
            form.render();
        });
        form.on('select(typeQc)', function(data){
            refreshTableData();
            form.render();
        });
        form.on('select(payMethodQc)', function(data){
            refreshTableData();
            form.render();
        });
        form.on('select(stateQc)', function(data){
            refreshTableData();
            form.render();
        });
    });

    //判断是否有供应商操作权限
    if(hasRoleXZ()){
        $("#supplierSetting").show();
        $("#purchaseButtonDiv").show();
    }else {
        $("#supplierSetting").hide();
        $("#purchaseButtonDiv").hide();
    }

    var fileUpload = new FileUpload({
        targetEl: '#word2htmlForm',
        multi: false,
        filePart: "inventory",
        completeCallback: function (data) {
            if (data.length > 0)
                $.get(baseUrl + "/purchase/importPurchaseData?filePath="+data[0].file,function (data) {
                    if (data.code == 200) {
                        var message = "操作完成";
                        var messageType = "success";
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
                        buyingManageObj.cleanPurchaseData();
                        buyingManageObj.initPagerPlugin();
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

    //物品采购导入
    $("#word2html").click(function () {
        fileUpload.upload();
    });

    //加载产品分类进入缓存里
    configObj.listGoodsTypeData();
    //初始化页面
    commonObj.initPage();
});

function hasRoleXZ() {
    var len = user.roles.length ;
    for(var i=0;i<len;i++){
        if((user.roles[i].type=='XZ' && user.roles[i].code=='BZ') || user.roles[i].type=='CK' && user.roles[i].code=='ZY'){
            return true ;
        }
    }
    return false ;
}

//根据tabIndex刷新表格数据
function refreshTableData() {
    var index = $("#tabIndex").val();
    if(index==0){
        //刷新物品采购数据
        buyingManageObj.initPagerPlugin();
    }else{
        //刷新供应商数据
        supplierManageObj.initPagerPlugin();
    }
}

//编辑供应商时为单选框赋值
function setRadioState(obj,val){
    $(obj).each(function (i,item) {
        $(item).removeAttr("checked");
        $(item).parent().removeClass("checked");
    });
    $(obj).each(function (j,m) {
        if($(m).val()==val){
            $(m).prop("checked",true);
            $(m).parent().addClass("checked");
        }
    });
}

//多个页面使用的方法或者数据
var commonObj = {
    //数据请求前，替换字符串中的<>符号成为&lt;&gt;
    replaceParam: function (param) {
        if(param && Object.getOwnPropertyNames(param).length > 0){
            for(var key in param){
                if(typeof param[key] && param[key].constructor == String){ //如果数据是字符串，则进行替换
                    param[key] = param[key].replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            }
        }
        return param;
    },
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
            //采购管理
            $("#buyingManageTab").css("display","block");
            $("#keyWordsQc").attr("placeholder","采购编号");
            buyingManageObj.initPagerPlugin();
        }else if(index == 1){
            //供应商管理
            $("#supplierManageTab").css("display","block");
            $("#keyWordsQc").attr("placeholder","联系人,联系方式");
            buyingManageObj.loadGoodsType(null,null,"#supplierManageForm #typeQc");
            supplierManageObj.initPagerPlugin();
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

//供应商管理对象
var supplierManageObj = {
    getTotalUrl: "/purchaseSupplier/getPageCount",
    supplierManageListUrl: "/purchaseSupplier/listPg",
    pagerPluginElem: 'supplierManagePager',
    modalIndex: null,
    callback: function (data) {
        $("#supplierManageList").empty();
        var html = "";
        if (data && data.list.length > 0) {
            $.each(data.list, function (i, supplier) {
                //供应商资质
                var levelName = "";
                if (supplier.level == 0) {
                    levelName = "普通";
                } else if (supplier.level == 1) {
                    levelName = "中等";
                } else if (supplier.level == 2) {
                    levelName = "优质";
                }
                //支付方式
                var payMethodName = "";
                //支付账号
                var payAccount = "";
                if (supplier.payMethod == 0) {
                    payMethodName = "微信";
                    payAccount = (supplier.weixin == null || supplier.weixin == "") ? "" : supplier.weixin;
                } else if (supplier.payMethod == 1) {
                    payMethodName = "支付宝";
                    payAccount = (supplier.zhifubao == null || supplier.zhifubao == "") ? "" : supplier.zhifubao;
                } else if (supplier.payMethod == 2) {
                    payMethodName = "银行卡";
                    payAccount = (supplier.bankNo == null || supplier.bankNo == "") ? "" : supplier.bankNo;
                }
                var contactName = (supplier.contactName == null || supplier.contactName == "") ? "" : supplier.contactName;
                var contactPhone = (supplier.contactPhone == null || supplier.contactPhone == "") ? "" : supplier.contactPhone;
                var obj = JSON.stringify(supplier);
                html += "<tr>" +
                    "<td style=\"text-align: center\"><span style='color: #00a2ff;cursor: pointer' onclick='supplierManageObj.showSaveSupplier("+obj+",0)'>" + supplier.code + "</span></td>" +
                    "<td style=\"text-align: center\">" + supplier.name + "</td>" +
                    "<td style=\"text-align: center\">" + levelName + "</td>" +
                    "<td style=\"text-align: center\">" + supplier.typeName + "</td>" +
                    "<td style=\"text-align: center\">" + payMethodName + "</td>" +
                    "<td style=\"text-align: center\">" + payAccount + "</td>" +
                    "<td style=\"text-align: center\">" + contactName + "</td>" +
                    "<td style=\"text-align: center\">" + contactPhone + "</td>" +
                    "<td style=\"text-align: center\">" +
                    "<a href='javascript:void(0)' class='submitButton' onclick='supplierManageObj.showSaveSupplier("+obj+",1)'>编辑</a>" +
                    "<a href='javascript:void(0)' class='submitButton' onclick=\"supplierManageObj.delSupplier('" + supplier.id + "')\">删除</a>" +
                    "</td>" +
                    "</tr>";
            });
            //拼接内容
            $("#supplierManageList").html(html);
        }
    },
    initPagerPlugin: function () {
        var formData = $("#supplierManageForm").serializeJson();
        var keyWords = $("#keyWordsQc").val();
        formData.keyWords = keyWords;
        //初始化分页组件
        commonObj.requestData(formData, supplierManageObj.getTotalUrl, "post", "json", true, function (data) {
            if (data && data.code == 200) {
                commonObj.pagerPlus({
                    param: formData,
                    elem: supplierManageObj.pagerPluginElem,
                    count: data.data.total,
                    url: supplierManageObj.supplierManageListUrl,
                    bubbleType: "bubble" //有气泡弹窗页面设置
                }, supplierManageObj.callback);
            }
        });
    },
    //加载供应商信息
    loadSupplier: function (t, elem) {
        $(elem).empty();
        var html = "<option value=''>请选择</option>";
        layui.use(["form"], function () {
            // $.get("/purchaseSupplier/getSupplierList", null, function (data) {
                configObj.loadSupplierData();
                var data = configObj.tempData.supplier;
                if(data!=null && data!=undefined){
                    for (var i = 0; i < data.data.number; i++) {
                        var id = data.data.list[i].id;
                        var name = data.data.list[i].name;
                        var selected = id == t ? "selected=selected" : "";
                        html += "<option value='" + id + "' " + selected + ">" + name + "</option>"
                    }
                }
                $(elem).append(html);
                layui.form.render();
            // }, "json");
        });
    },
    //供应商支付方式切换
    showPayMethod: function (layer0,index) {
        var payMethod = $(layer0[0]).find("input:radio[name='payMethod']:checked").val();
        if (payMethod == 0) {
            $(".weixin").show();
            $(".zhifubao").hide();
            $(".bank").hide();
        } else if (payMethod == 1) {
            $(".weixin").hide();
            $(".zhifubao").show();
            $(".bank").hide();
        } else if (payMethod == 2) {
            $(".weixin").hide();
            $(".zhifubao").hide();
            $(".bank").show();
        }
    },
    //清除供应商查询数据
    cleanSupplierData: function () {
        $("#levelQc").val("");
        $("#typeQc").val("");
        $("#nameQc").val("");
        $("#payMethodQc").val("");
        $("#keyWordsQc").val("");
    },
    //关闭供应商modal
    closeModal:function(){
        if(supplierManageObj.modalIndex){
            layer.close(supplierManageObj.modalIndex);
        }else {
            layer.closeAll();
        }
    },
    // 判断供应商名称是否重复
    checkSupplierNameRepeat: function () {
        var name = $(configObj.layerSupplierObj[0]).find("input[name='name']").val();
        var id = $(configObj.layerSupplierObj[0]).find("input[name='id']").val();
        var flag = false;
        requestData({name: name, id: id},"/purchaseSupplier/getPurchaseSupplierByName","post","json",false,function (data) {
            if (data.code == 200) {
                if (data.data.number > 0) {
                    layer.open({
                        title: "提示",
                        content: "此供应商名称已经存在"
                    });
                    $(configObj.layerSupplierObj[0]).find("input[name='name']").val("");
                    flag = false;
                } else {
                    flag = true;
                }
            }
        });
        return flag;
    },
    showSaveSupplier:function(supplier,flag){
        $("#addSupplierForm #purchaseSupplierId").val("");
        supplierManageObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#saveSupplierModal").html(),
            btn: [],
            area: ['86%', '75%'],
            offset:['100px','116px'],
            closeBtn: 0,
            btn: [],
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                if(supplier==null && supplier==undefined){
                    //物品供应商表单对象
                    configObj.layerSupplierObj = layero;
                    $(".save").show();
                    $(".edit").hide();
                    //新增默认微信
                    layui.form.val('example', {
                        payMethod: 0,
                    });
                    configObj.listGoodsTypeData();
                    buyingManageObj.loadGoodsType(null,null,$(layero[0]).find("select[name='type']"));
                    //初始化支付方式单选框
                    supplierManageObj.showPayMethod(layero,index);
                    //移除查看disabled属性
                    supplierManageObj.removeSupplierDisableStyle();
                }else{
                    configObj.layerSupplierObj = layero;
                    $(".save").hide();
                    $(".edit").show();
                    for (var attr in supplier) {
                        $(layero[0]).find("[name=" + attr + "][type!='radio']").val(supplier[attr] || "");
                    }
                    configObj.listGoodsTypeData();
                    buyingManageObj.loadGoodsType(supplier["type"], null, $(layero[0]).find("select[name='type']"));
                    layui.form.val('example', {
                        "type": supplier["type"]
                    });
                    layui.form.render();
                    var payWay = supplier["payMethod"];
                    $(layero[0]).find("input[name='payMethod'][value='0']").attr("checked",payWay==0?true:false);
                    $(layero[0]).find("input[name='payMethod'][value='1']").attr("checked",payWay==1?true:false);
                    $(layero[0]).find("input[name='payMethod'][value='2']").attr("checked",payWay==2?true:false);
                    supplierManageObj.showPayMethod(layero, index);
                    if (flag == 0) {
                        //查看添加disabled属性
                        supplierManageObj.addSupplierDisableStyle();
                        //隐藏保存按钮
                        $(layero[0]).find("div[id='editSupplierBtn']").hide();
                    } else if (flag == 1) {
                        //编辑移除disabled属性
                        supplierManageObj.removeSupplierDisableStyle();
                        //显示保存按钮
                        $(layero[0]).find("div[id='editSupplierBtn']").show();
                    }
                }
                //使用layui表单
                layui.use('form', function () {
                    var form = layui.form;
                    form.render();
                    //供应商支付方式切换初始化
                    form.on('radio(isChecked)',function (data) {
                        supplierManageObj.showPayMethod(layero,index);
                    });
                });
                layui.form.verify({
                    name:function(value){
                      if(value==null || value==""){
                          return "供应商名称不能为空";
                      }
                    },
                    level:function(value){
                        if(value==null || value==""){
                            return "请选择供应商资质";
                        }
                    },
                    type:function(value){
                        if(value==null || value==""){
                            return "请选择产品分类";
                        }
                    },
                    address:function(value){
                        if(value==null || value==""){
                            return "供应商地址不能为空";
                        }
                    },
                    contactName:function(value){
                        if(value==null || value==""){
                            return "联系人姓名不能为空";
                        }
                    },
                    contactPhone:function(value){
                        if(value==null || value==""){
                            return "联系方式不能为空";
                        }
                    },
                    email: function(value){
                        var reg = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
                        if(value!=null && value!=''){
                            if (!reg.test(value)) {
                                return "邮箱格式不正确";
                                $(layero[0]).find("input[name='email']").val("");
                            }
                        }
                    }
                });
                layui.form.on('submit(addSupplier)',function (data) {
                    var url = data.elem.attributes["data-url"].nodeValue;
                    supplierManageObj.submitSupplier(data,url);
                });
            }
        });
    },
    //判断该供应商下是否有采购记录
    checkDelete: function (id) {
        var flag = false;
        $.ajax({
            type: "post",
            url: "/purchase/getPurchaseBySupplierId",
            data: {supplierId: id},
            dataType: "json",
            async: false,
            success: function (data) {
                if (data.data.number > 0) {
                    //供应商下有采购记录，不允许删除
                    layer.open({
                        title: "提示",
                        content: "此供应商下有采购记录不可删除"
                    });
                    flag = false;
                } else {
                    flag = true;
                }
            }
        });
        return flag;
    },
    //供应商新增编辑功能
    submitSupplier: function (data, url) {
        if(supplierManageObj.checkSupplierNameRepeat()){
            layer.confirm("是否保存供应商信息？", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + data.elem.id);//锁定按钮，防止重复提交
                var formData = data.field;
                $.ajax({
                    type: "post",
                    url: url,
                    data: formData,
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            supplierManageObj.initPagerPlugin();
                            supplierManageObj.closeModal();
                        } else {
                            supplierManageObj.closeModal();
                        }
                        configObj.tempData.supplier=[];
                    },
                    error:function (data) {
                        Ladda.stopAll();
                    }
                });
            }, function () {
                return;
            })
        }
    },
    //删除物品供应商信息
    delSupplier: function (id) {
        //判断供应商下是否有采购记录
        if (supplierManageObj.checkDelete(id)) {
            layer.confirm("确定删除该记录？", {
                btn: ["确定", "取消"],
                shade: false,
            }, function (index) {
                layer.close(index);
                $.ajax({
                    type: "post",
                    url: "/purchaseSupplier/delPurchaseSupplier",
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            layer.msg(data.data.message, {icon: 6, time: 1000});
                            //清除供应商查询数据
                            supplierManageObj.cleanSupplierData();
                            supplierManageObj.initPagerPlugin();
                        } else if (data.code == 1002) {
                            swal({
                                title: "提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;
                        }
                        configObj.tempData.supplier=[];
                    },error:function (data) {
                        Ladda.stopAll();
                    }
                })
            });
        }
    },
    //供应商添加disabled属性
    addSupplierDisableStyle: function () {
        $("#addSupplierForm [name='name']").attr("disabled", true);
        $("#addSupplierForm [name='level']").attr("disabled", true);
        $("#addSupplierForm [name='type']").attr("disabled", true);
        $("#addSupplierForm [name='desc']").attr("disabled", true);
        $("#addSupplierForm [name='address']").attr("disabled", true);
        $("#addSupplierForm [name='contactName']").attr("disabled", true);
        $("#addSupplierForm [name='contactPhone']").attr("disabled", true);
        $("#addSupplierForm [name='contactSex']").attr("disabled", true);
        $("#addSupplierForm [name='deptName']").attr("disabled", true);
        $("#addSupplierForm [name='position']").attr("disabled", true);
        $("#addSupplierForm [name='email']").attr("disabled", true);
        $("#addSupplierForm [name='supplierId']").attr("disabled", true);
        $("#addSupplierForm [name='payMethod']").attr("disabled", true);
        $("#addSupplierForm [name='weixin']").attr("disabled", true);
        $("#addSupplierForm [name='zhifubao']").attr("disabled", true);
        $("#addSupplierForm [name='bankName']").attr("disabled", true);
        $("#addSupplierForm [name='accountName']").attr("disabled", true);
        $("#addSupplierForm [name='bankNo']").attr("disabled", true);
        $("#addSupplierForm [name='bankPlace']").attr("disabled", true);
    },
    //供应商移除disabled属性
    removeSupplierDisableStyle: function () {
        $("#addSupplierForm [name='name']").attr("disabled", false);
        $("#addSupplierForm [name='level']").attr("disabled", false);
        $("#addSupplierForm [name='type']").attr("disabled", false);
        $("#addSupplierForm [name='desc']").attr("disabled", false);
        $("#addSupplierForm [name='address']").attr("disabled", false);
        $("#addSupplierForm [name='contactName']").attr("disabled", false);
        $("#addSupplierForm [name='contactPhone']").attr("disabled", false);
        $("#addSupplierForm [name='contactSex']").attr("disabled", false);
        $("#addSupplierForm [name='deptName']").attr("disabled", false);
        $("#addSupplierForm [name='position']").attr("disabled", false);
        $("#addSupplierForm [name='email']").attr("disabled", false);
        $("#addSupplierForm [name='supplierId']").attr("disabled", false);
        $("#addSupplierForm [name='payMethod']").attr("disabled", false);
        $("#addSupplierForm [name='weixin']").attr("disabled", false);
        $("#addSupplierForm [name='zhifubao']").attr("disabled", false);
        $("#addSupplierForm [name='bankName']").attr("disabled", false);
        $("#addSupplierForm [name='accountName']").attr("disabled", false);
        $("#addSupplierForm [name='bankNo']").attr("disabled", false);
        $("#addSupplierForm [name='bankPlace']").attr("disabled", false);
    },
    //导出供应商信息
    exportSupplierDetail: function () {
        var params = $("#supplierManageForm").serializeJson();
        var keyWords = $("#keyWordsQc").val();
        params.keyWords = keyWords;
        location.href = "/purchaseSupplier/exportPurchaseSupplier" + "?" + $.param(params);
    },
    //供应商导入模板
    downTemplate: function () {
        location.href = "/purchaseSupplier/exportTemplate";
    },
    //跳转上传供应商页面
    showUploadView: function(){
        $("#file").val("");
        $("#importSupplierModal").modal('toggle');
    },
    //导入供应商信息
    importSupplierDetails: function () {
        if (document.getElementById("file").value == "") {
            swal("请选上传excel！");
        } else {
            var filePath = document.getElementById("file").value;
            var fileExt = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
            var flag = false;
            if (fileExt.match(/^(.xls|.xlsx)$/)) {
                flag = true;
            }
            if (flag) {
                startModal("#submitImportBtn");//锁定按钮，防止重复提交
                var formData = new FormData($("#importForm")[0]);
                $.ajax({
                    type: "post",
                    url: "/purchaseSupplier/batchSupplierForEasyExcel",
                    data: formData,
                    dataType: "json",
                    async: true,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        Ladda.stopAll();
                        $("#importModal").modal('hide');
                        if (data.code == 200) {
                            supplierManageObj.initPagerPlugin();
                            $("#importSupplierModal").modal('hide');
                            swal({title: data.data.message, type: "success"});
                        } else if (data.code == 1002) {
                            swal({
                                title: "提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    },
                    error: function (data) {
                        Ladda.stopAll();
                        if (getResCode(data))
                            return;
                    }
                });
            } else {
                swal("文件格式不正确，只能上传excel文件！");
            }
        }
    }
}
<!--声明序号-->
var orderNumber = 0;
//采购管理对象
var buyingManageObj = {
    getTotalUrl: "/purchase/getPageCount",
    buyingManageListUrl: "/purchase/getPageInfo",
    pagerPluginElem: 'buyingManagePager',
    modalIndex: null,//采购新增编辑弹出层下标
    approveIndex: null,//审核详情弹出层下标
    limit: 10,
    callback: function (data) {
        var html = "";
        $("#buyingManageList").empty();
        if (data && data.list.length > 0) {
            $.each(data.list, function (i, purchase) {
                //状态
                var stateTips = "";
                switch (purchase.state) {
                    case -5 :
                        stateTips = "<span style='color: red'>已失效</span>";
                        break;
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
                        stateTips = "入库暂存";
                        break;
                    case 7 :
                        stateTips = "已入库";
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
                var money = (purchase.money == undefined || purchase.money==null)?"":purchase.money;
                var disabled="";
                if(purchase.reimbursementId=="" || purchase.reimbursementId==null){
                    disabled="";
                    if(purchase.state==7){
                        disabled="";
                    }else {
                        disabled="disabled=disabled";
                    }
                }else{
                    disabled="disabled=disabled";
                }
                var bxCode=purchase.BXCode==undefined?"":purchase.BXCode;
                html += "<tr>" +
                    "<td style=\"text-align: center\"><input type=\"checkbox\" name=\"buyingId\" value='"+purchase.id+"' "+disabled+" lay-skin=\"primary\"/>"+(pageIndex + 1)+"</td>" +
                    "<td style=\"text-align: center\" title='"+purchase.purchaseCode+"'><div style='color: #00a2ff;cursor: pointer;' class='ellipsisContent' onclick=\"buyingManageObj.view('" + purchase.id + "',1)\">" + purchase.purchaseCode + "</div></td>" +
                    "<td style=\"text-align: center\" class='ellipsisContent' title='"+purchase.title+"'>" + purchase.title + "</td>" +
                    "<td style=\"text-align: center\" title='"+bxCode+"'><div style='color: #00a2ff;cursor: pointer;' class='ellipsisContent' onclick=\"buyingManageObj.goToReimbursement('" + purchase.reimbursementId + "')\">" + bxCode + "</div></td>" +
                    "<td style=\"text-align: center\">" + purchase.buyerName + "</td>" +
                    "<td style=\"text-align: center\">" + (purchase.buyTime||"") + "</td>" +
                    "<td style=\"text-align: center\">" + money + "</td>" +
                    "<td style=\"text-align: center\">" + purchase.userName + "</td>" +
                    "<td style=\"text-align: center\">" + stateTips + "</td>" +
                    "<td style=\"text-align: center\">";
                if (purchase.taskId != null && purchase.taskId != "") {
                    html += "<a href='javascript:void(0)' class='submitButton'  onclick=\"buyingManageObj.showHistory('" + purchase.id + "')\">审核详情</a>";
                }
                if ((purchase.state == 0 || purchase.state == -1) && purchase.userId == user.id) {
                    html += "<a href='javascript:void(0)' class='submitButton' onclick=\"buyingManageObj.view('" + purchase.id + "',0)\">编辑</a>";
                }
                if ((purchase.state == 0 || purchase.state == -1) && purchase.userId == user.id) {
                    html += "<a href='javascript:void(0)' class='submitButton' onclick=\"buyingManageObj.delProduct('" + purchase.id + "')\">删除</a>";
                }
                html += "</td></tr>";
            });
            //拼接内容
            $("#buyingManageList").html(html);
            layui.use('form', function(){
                var form = layui.form;
                form.render(); //先渲染，然后执行下面复选框事件才有效
            });
        }
    },
    initPagerPlugin: function () {
        var formData = $("#queryBuyingForm").serializeJson();
        var keyWords = $("#keyWordsQc").val();
        formData.keyWords = keyWords;
        //初始化分页组件
        commonObj.requestData(formData, buyingManageObj.getTotalUrl, "post", "json", true, function (data) {
            if (data && data.code == 200) {
                commonObj.pagerPlus({
                    param: formData,
                    elem: buyingManageObj.pagerPluginElem,
                    count: data.data.total,
                    url: buyingManageObj.buyingManageListUrl,
                    target:$("#buyingManageList"),
                }, buyingManageObj.callback);
            }
        });
    },
    //根据公司代码加载用户数据
    loadUser: function (t, elem) {
        $(elem).empty();
        layui.use(["form"], function () {
            // $.get("/propose/listByForum", {companyCode: user.companyCode}, function (data) {
                configObj.loadUserData();
                var data = configObj.tempData.users;
                var html = "";
                html += "<option value=''>请选择</option>";
                if(data!=null){
                    for (var i = 0; i < data.data.number; i++) {
                        var id = data.data.list[i].id;
                        var name = data.data.list[i].name;
                        var selected = id == t ? "selected=selected" : "";
                        html += "<option value='" + id + "' " + selected + ">"+name+"</option>";
                    }
                }
                $(elem).append(html);
                layui.form.render();
            // }, "json");
        });
    },
    loadCWBZData:function(elem){
        $(elem).empty();
        layui.use(["form"], function () {
            var html = "";
            requestData(null,"/user/secretary","get","json",true,function (data) {
                if(data!=null && data.length>0){
                    for (var i = 0; i < data.length; i++) {
                        var id = data[i].id;
                        var name = data[i].name;
                        html += "<option value='" + id + "'>"+name+"</option>";
                    }
                }
                $(elem).append(html);
            })
            layui.form.render();
        });
    },
    //自动生成采购编号
    loadPurchaseCode:function(t){
        if(configObj.tempData.purchaseCode.length<=0){
            requestData(null,"/purchase/getPurchaseCode","post","json",true,function (data) {
                if (data.code==200){
                    $(t).find("input[name='purchaseCode']").val(data.data.code);
                    configObj.tempData.purchaseCode=data.data.code;
                }else{
                    layer.open({
                        title:"提示",
                        content:data.msg,
                    });
                }
            })
        }else{
            $(t).find("input[name='purchaseCode']").val(configObj.tempData.purchaseCode);
        }
    },
    //关闭采购新增编辑弹出层
    closeModal:function(){
        if(buyingManageObj.modalIndex){
            layer.close(buyingManageObj.modalIndex);
        }else {
            layer.closeAll();
        }
    },
    //关闭审核详情弹出层
    closeApproveDiv:function(){
      if(buyingManageObj.approveIndex){
          layer.close(buyingManageObj.approveIndex);
      }
    },
    //物品申请添加明细限制
    checkEmpty: function ($stockModalCommon){
        var title = $($stockModalCommon[0]).find(".title").val();//标题
        var buyerId = $($stockModalCommon[0]).find(".buyerId").val();//采购员
        var buyTime = $($stockModalCommon[0]).find(".buyTime").val();//采购时间
        var type = $(".type");//产品分类
        var goodsId = $(".goodsId");//产品名称
        var price = $(".price");//产品单价
        var amount = $(".amount");//产品数量
        var payMethod = $(".payMethod");//产品支付方式
        var supplierId = $(".supplierId");//产品供应商
        if(title==null || title==""){
            layer.open({
                title: "提示",
                content: "采购标题不能为空"
            });
            return false;
        }
        if(buyerId==null || buyerId==""){
            layer.open({
                title: "提示",
                content: "请选择采购员"
            });
            return false;
        }
        if(buyTime==null || buyTime==""){
            layer.open({
                title: "提示",
                content: "请选择采购时间"
            });
            return false;
        }
        //物品明细数据不能为空
        var tableLength = $("#checkTable tr").length;
        if (tableLength<=0) {
            layer.open({
                title: "提示",
                content: "请至少添加一条采购明细信息"
            });
            return false;
        }
        //物品明细字段校验
        for (var i = 0; i < type.length; i++) {
            var typeVal = type.eq(i).val();//产品分类id
            var goodsIdVal = $(goodsId).eq(i).find("option:selected").val();//产品名称id
            var priceVal = price.eq(i).val();//单价
            var count = amount.eq(i).val();//数量
            var supplierIdVal = supplierId.eq(i).val();//供应商id
            var payMethodVal = payMethod.eq(i).val();//支付方式
            if (typeVal == "" || typeVal == null) {
                layer.open({
                    title: "提示",
                    content: "产品分类不能为空"
                });
                return false;
            }
            if (goodsIdVal == "" || goodsIdVal == null) {
                layer.open({
                    title: "提示",
                    content: "产品名称不能为空"
                });
                return false;
            }
            //数量校验
            if (count == null || count == "") {
                layer.open({
                    title: "提示",
                    content: "数量不能空"
                });
                return false;
            }
            if (count <= 0) {
                layer.open({
                    title: "提示",
                    content: "数量必须大于0"
                });
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
            if (priceVal <=0) {
                layer.open({
                    title: "提示",
                    content: "单价必须大于0"
                });
                return false;
            }
            if (supplierIdVal == null || supplierIdVal == "") {
                layer.open({
                    title: "提示",
                    content: "供应商不能为空"
                });
                return false;
            }
            if (payMethodVal == null || payMethodVal == "") {
                layer.open({
                    title: "提示",
                    content: "请选择支付方式"
                });
                return false;
            }
        }
        return true;
    },
    //清除采购查询数据
    cleanPurchaseData: function () {
        $("#buyingTime").val("");
        $("#buyingName").val("");
        $("#stateQc").val("");
        $("#keyWordsQc").val("");
    },
    //物品采购添加disabled属性
    addPurchaseDisableStyle: function () {
        $("#addPurchaseForm [name='title']").attr("disabled", true);
        $("#addPurchaseForm [name='userName']").attr("disabled", true);
        $("#addPurchaseForm [name='createTime']").attr("disabled", true);
        $("#addPurchaseForm [name='buyerId']").prop("disabled", true);
        $("#addPurchaseForm [name='buyTime']").attr("disabled", true);
        $("#addPurchaseForm .layui-form-select").addClass("layui-select-disabled");
        $("#addPurchaseForm .layui-select-title input").addClass("layui-disabled");
        $("#addPurchaseForm [name='file']").attr("disabled", true);
        $("#addPurchaseForm [id='rejectReason']").attr("disabled", true);
        $("#addPurchaseForm [name='money']").attr("disabled", true);
        $("#addPurchaseForm [name='desc']").attr("disabled", true);
        $("#addPurchaseForm [name='type']").prop("disabled", true);
        $("#addPurchaseForm [name='goodsId']").attr("disabled", true);
        $("#addPurchaseForm [name='unit']").attr("disabled", true);
        $("#addPurchaseForm [name='specs']").attr("disabled", true);
        $("#addPurchaseForm [name='amount']").attr("disabled", true);
        $("#addPurchaseForm [name='price']").attr("disabled", true);
        $("#addPurchaseForm [name='totalMoney']").attr("disabled", true);
        $("#addPurchaseForm [name='supplierId']").attr("disabled", true);
        $("#addPurchaseForm [name='payMethod']").attr("disabled", true);
        $("#addPurchaseForm #btn_addCheck").attr("disabled", true);
        $("#addPurchaseForm #remark").attr("disabled", true);
        $("#addPurchaseForm [name='btn_removeCheck']").attr("disabled", true);
        $("#addPurchaseForm .licence").attr("disabled", true);
    },
    //物品采购移除disabled属性
    removePurchaseDisableStyle: function () {
        $("#addPurchaseForm [name='title']").attr("disabled", false);
        $("#addPurchaseForm [name='userName']").attr("disabled", false);
        $("#addPurchaseForm [name='createTime']").attr("disabled", false);
        $("#addPurchaseForm .layui-form-select").removeClass("layui-select-disabled");
        $("#addPurchaseForm .layui-select-title input").removeClass("layui-disabled");
        $("#addPurchaseForm [name='buyerId']").prop('disabled', false);
        $("#addPurchaseForm [name='buyTime']").attr("disabled", false);
        $("#addPurchaseForm [name='file']").attr("disabled", false);
        $("#addPurchaseForm [id='rejectReason']").attr("disabled", false);
        $("#addPurchaseForm [name='money']").attr("disabled", false);
        $("#addPurchaseForm [name='desc']").attr("disabled", false);
        $("#addPurchaseForm [name='type']").prop("disabled", false);
        $("#addPurchaseForm [name='goodsId']").attr("disabled", false);
        $("#addPurchaseForm [name='unit']").attr("disabled", false);
        $("#addPurchaseForm [name='specs']").attr("disabled", false);
        $("#addPurchaseForm [name='amount']").attr("disabled", false);
        $("#addPurchaseForm [name='price']").attr("disabled", false);
        $("#addPurchaseForm [name='totalMoney']").attr("disabled", false);
        $("#addPurchaseForm [name='supplierId']").attr("disabled", false);
        $("#addPurchaseForm [name='payMethod']").attr("disabled", false);
        $("#addPurchaseForm #btn_addCheck").attr("disabled", false);
        $("#addPurchaseForm #remark").attr("disabled", false);
        $("#addPurchaseForm [name='btn_removeCheck']").attr("disabled", false);
        $("#addPurchaseForm .licence").attr("disabled", false);
    },
    //审核记录查看
    showHistory: function (id) {
        //process详见IProcess
        $("#historyModal").modal('toggle');
        $.ajax({
            type: "post",
            url: "/process/history",
            data: {dataId: id, process: 27},
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
                        html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=27&t=" + new Date().getTime() + "' style='width: 135%; margin-left: -175px; margin-top: -120px;margin-bottom: -100px;'/></div>";
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
        buyingManageObj.approveIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 99999,
            content: $("#showHistoryModal").html(),
            btn: [],
            area: ['66%', '60%'],
            offset:['120px','220px'],
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
                    data: {dataId: id, process: 27},
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
                                html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=27&t=" + new Date().getTime() + "' style='margin-bottom: -100px;margin-top: -90px;margin-left: -175px'/></div>";
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
        var state = $(configObj.layerObj[0]).find("input[name='state']").val();
        //行政部长审核通过抄送给财务部长
        if(state==14){
            //抄送人
            var userId = $(configObj.layerObj[0]).find("select[id='notify']").val();
            var purchaseId = $(configObj.layerObj[0]).find("input[name='id']").val();
            var title = $(configObj.layerObj[0]).find("input[name='title']").val();
            requestData({userId:userId,purchaseId:purchaseId,title:title},"/purchase/addItem","post","json",true,function (data) {
            });
        }
        approveTask(taskId, 1, t.id, remark);
    },
    //审核驳回
    reject: function (t) {
        var taskId = $(configObj.layerObj[0]).find("input[name='taskId']").val();
        var remark = $(configObj.layerObj[0]).find("input[name='remark']").val();
        approveTask(taskId, 0, t.id, remark);
    },
    confirm:function(){
        layer.confirm("是否确认？", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            var itemId = $(configObj.layerObj[0]).find("input[name='itemId']").val();
            $.get("/purchase/purchaseConfirm",{itemId:itemId},function (data) {
                if(data.code==200){
                    layer.msg(data.data.message,{time:1000,icon:6});
                }
            },"json");
            setTimeout(function () {
                // 审核后刷新首页；
                refrechPage("/homePage");
                closeCurrentTab();
            }, 1000);
            buyingManageObj.closeModal();
        }, function () {
            return;
        })
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
                //下拉框标志
                var no = typeData.elem.attributes["data-id"].nodeValue;
                //产品分类id
                var typeId = typeData.value;
                //选中产品后切换产品分类，移除之前选中的产品
                var beforeId = $(typeData.elem).closest("#tr"+no).find("td:nth-child(3) select").attr("data-before-id");
                if(beforeId!=""){
                    buyingManageObj.removeSelectGoodsId(beforeId);
                    buyingManageObj.refreshTable(typeId);
                }
                $("#goodsId_"+no).empty();
                buyingManageObj.loadGoods(typeId,null,"#goodsId_"+no);
            });
            layui.form.on("select(goodsId)", function (typeData) {
                var no = typeData.elem.attributes["data-id"].nodeValue;
                var parentId=typeData.elem.attributes["data-value"].nodeValue;
                //产品id
                var goodsId = typeData.value;
                //之前选中的产品
                var beforeGoodsId=typeData.elem.attributes["data-before-id"].nodeValue;
                if(goodsId==""){
                    //移除选中的产品
                    buyingManageObj.removeSelectGoodsId(beforeGoodsId);
                    //刷新表格
                    buyingManageObj.refreshTable(parentId);
                    $("#tr" + no + " .specs").val("");
                    $("#tr" + no + " .unit").val("");
                    $("#tr" + no + " .amount").val(1);
                    $("#tr" + no + " .price").val(0);
                    $("#tr" + no + " .totalMoney").val(0);
                }
                if(goodsId!=""){
                    configObj.listGoodsDetailData(goodsId);
                    // requestData({id: goodsId}, "/goodsType/editAjax", "get", "json", true, function(data) {
                        var goods = configObj.tempData.goodsDetail[goodsId].data.entity;
                        if(goods!=null){
                            $("#tr" + no + " .specs").val(goods["specs"]||"");
                            $("#tr" + no + " .unit").val(goods["unit"]||"");
                            $("#tr" + no + " .amount").val(1);
                            $("#tr" + no + " .price").val(goods["price"]);
                            $("#tr" + no + " .totalMoney").val(goods["price"]);
                        }
                    // });
                    //移除选中的产品
                    buyingManageObj.removeSelectGoodsId(beforeGoodsId);
                    //添加选中的产品
                    buyingManageObj.addSelectGoods();
                    //刷新表格
                    buyingManageObj.refreshTable(parentId);
                    //之前产品不为空时赋值
                    $("#goodsId_"+no).attr("data-before-id",goodsId);
                }
                buyingManageObj.calculatePrice();
            });
            // 如果产品分类id不为null,则给产品赋值
            if(t!=null && t!=""){//t产品分类id，goodsId产品id
                if(goodsId!=null &&  goodsId!=""){
                    buyingManageObj.loadGoods(t,goodsId,"#goodsId_"+no);
                }
            }
        });
    },
    loadGoods:function(parentId,t,elem){//parentId产品分类id,t产品id
        layui.use(["form"], function () {
            var html="<option value=''>请选择</option>";
            //向缓冲区中查询该产品分类id是否存在，存在则直接调用数据，否则向后台获取产品信息
            $(elem).empty();
            configObj.listGoodsData(parentId);
            var data = configObj.tempData.goods[parentId];
            for (var i = 0; i < data.data.list.length; i++) {
                var id = data.data.list[i].id;
                var name = data.data.list[i].name;
                var selectedIds = configObj.selectedIds;
                var selected = t == id ? "selected=selected" : "";
                if(selectedIds.indexOf(id.toString())<=-1 && !selectedIds.contains(id)){
                    html += "<option value='" + id + "' " + selected + ">" + name + "</option>";
                }else {
                    if(selected!=""){
                        html += "<option value='" + id + "' " + selected + ">" + name + "</option>";
                    }else {
                        html += "<option value='" + id + "' " + selected + " disabled>" + name + "</option>";
                    }
                }
            }
            $(elem).append(html);
            //产品绑定产品分类id
            $(elem).attr("data-value",parentId);
            //产品绑定上一产品id
            if(t!=null && t!=""){
                $(elem).attr("data-before-id",t);
            }
            layui.form.render();
        });
    },
    //添加选中的产品id
    addSelectGoods:function(){
         var selectElem = $(configObj.layerObj[0]).find("table[id='checkTable']").find("tr td:nth-child(3) select");
         var tableLength = $("#checkTable tr").length;
         for(var i=0;i<tableLength;i++){
            var goodsId = $(selectElem)[i].value;//产品id
            //遍历去重
             if(!configObj.selectedIds.contains(goodsId) && goodsId!=""){
                 configObj.selectedIds.push(goodsId);
             }
         }
    },
    //移除选中的产品id
    removeSelectGoodsId:function(id){
        //id可能为字符串或数字
        var index = configObj.selectedIds.indexOf(id.toString())==-1?configObj.selectedIds.indexOf(Number(id)):configObj.selectedIds.indexOf(id.toString());
        if(index>-1){
            configObj.selectedIds.splice(index,1);
        }
    },
    //更新表格选择产品的隐显状态（1、删减采购明细时调用，2、切换产品时调用）
    refreshTable:function(typeId){
        var selectElem = $(configObj.layerObj[0]).find("table[id='checkTable']").find("tr td:nth-child(3) select");
        var tableLength = $("#checkTable tr").length;
        for(var i=0;i<tableLength;i++){
            var parentId = $(selectElem)[i].attributes["data-value"].nodeValue;//产品分类id
            var elemNo = $(selectElem)[i].attributes["data-id"].nodeValue;//产品id
            if(typeId==parentId){
                var goodsId = $(selectElem)[i].value;//产品id
                buyingManageObj.loadGoods(parentId,goodsId,"#goodsId_"+elemNo);
            }
        }
    },
    loadPayMethod:function(elem,value){
        var data = configObj.payMethod;
        if(data!=null){
            layui.use(["form"], function () {
                $(elem).empty();
                var html = "<option value=''>请选择</option>";
                for (var i = 0;i<3;i++){
                    var selected = i==value?"selected=selected":"";
                    html+="<option value='"+i+"' "+selected+">"+data[i]+"</option>";
                }
                $(elem).append(html);
                layui.form.render();
            });
        }
    },
    <!-- 添加一行申请记录方法 -->
    addCheck: function (layero,type, goodsId, specs , unit, amount, price, totalMoney,supplierId ,payMethod) {
        orderNumber = orderNumber + 1;
        <!-- 判断形参内是否有实际的值 -->
        if (type == null || type == "" || typeof(type) == undefined) {
            type = "";
        }
        if (goodsId == null || goodsId == "" || typeof(goodsId) == undefined) {
            goodsId = "";
        }
        if (specs == null || specs == "" || typeof(specs) == undefined) {
            specs = "";
        }
        if (unit == null || unit == "" || typeof(unit) == undefined) {
            unit = "";
        }
        if (amount == null || amount === "" || typeof(amount) == undefined) {
            amount = 1;
        }
        if (price == null || price === "" || typeof(price) == undefined) {
            price = 0;
        }
        if (totalMoney == null || totalMoney == "" || typeof(totalMoney) == undefined) {
            totalMoney = "";
        }
        if (supplierId == null || supplierId == "" || typeof(supplierId) == undefined) {
            supplierId = "";
        }
        if (payMethod == null || typeof(payMethod) == undefined) {
            payMethod = "";
        }
        //产生随机数，作为加载用户的标志no（或者以时分秒为标志）
        var no = Math.round(Math.random() * 10000);
        <!-- 添加一行物品申请记录 -->
        var strContent = "<tr class='active checkNum" + orderNumber + "' id='tr" + no + "'>\n" +
            "<td style='text-align: center;'>\n" +
            orderNumber +
            "</td>\n" +
            "<td style='text-align: center;' class=\"layui-form\">\n" +
            "<select lay-search lay-filter=\"typeId\" class=\"type form-control\" data-id='"+no+"' id='type_" + no + "' name=\"type\">" +
            "</select>\n" +
            "</td>\n" +
            "<td style='text-align: center;' class=\"layui-form\">\n" +
            "<select lay-search lay-filter=\"goodsId\" class=\"goodsId form-control\" data-before-id data-value data-id='"+no+"' id='goodsId_" + no + "' name=\"goodsId\">" +
            "</select>\n" +
            "</td>\n" +
            "<td style='text-align: center;'>\n" +
            "<input class=\"specs form-control\" name=\"specs\" autocomplete='off' value='" + specs + "' >\n" +
            "</td>\n" +
            "<td style='text-align: center;'>\n" +
            "<input class=\"unit form-control\" name=\"unit\" autocomplete='off' value='" + unit + "' >\n" +
            "</td>\n" +
            "<td style='text-align: center;'>\n" +
            "<input class=\"amount form-control\" name=\"amount\" onchange=\"buyingManageObj.calculatePrice();\" value='" + amount + "' type=\"number\"/>\n" +
            "</td>\n" +
            "<td style='text-align: center;'>\n" +
            "<input class=\"price form-control\" name=\"price\" value='" + price + "' onkeyup=\"value=value.replace(/[^\\d\\.\\-]/g,'')\" type=\"text\" onchange=\"buyingManageObj.calculatePrice();\"/>\n" +
            "</td>\n" +
            "<td style='text-align: center;'>\n" +
            "<input class=\"totalMoney form-control\" name=\"totalMoney\" value='" + totalMoney + "' onkeyup=\"value=value.replace(/[^\\d]/g,'')\" type=\"text\"/>\n" +
            "</td>\n" +
            "<td style='text-align: center;' class=\"layui-form\">\n" +
            "<select lay-search lay-filter=\"supplierId\" class=\"supplierId form-control\" data-id='"+no+"' id='supplierId_" + no + "' name=\"supplierId\">" +
            "</select>\n" +
            "</td>\n" +
            "<td style='text-align: center;'>\n" +
            "<select class=\"payMethod form-control\" id='payMethod_"+no+"' name=\"payMethod\">" +
            "</select>\n" +
            "</td>\n";
        if ($("#flag").val() == -2 || $("#flag").val() == -3) {
            strContent += "<td style='text-align: center;'>" +
                "<button type=\"button\" id=\"btn_removeCheck_" + orderNumber + "\" name=\"btn_removeCheck\" class=\"btn btn-white btn-xs\" onclick=\"buyingManageObj.removeCheck(this);\">" +
                "<i class=\"glyphicon glyphicon-minus\"></i>" +
                "</button>" +
                "</td>\n";
        } else {
            strContent += "<td style='text-align: center;'>" +
                "<button type=\"button\" id=\"btn_removeCheck_" + orderNumber + "\" name=\"btn_removeCheck\" class=\"btn btn-white btn-xs\" disabled='disabled'>" +
                "<i class=\"glyphicon glyphicon-minus\"></i>" +
                "</button>" +
                "</td>\n";
        }
        strContent += "</tr>";
        var checkTable = $(layero[0]).find("table[id='checkTable']");
        checkTable.append(strContent);
        var typeElem = $(layero[0]).find("select[id='type_"+no+"']");
        var supplierElem = $(layero[0]).find("select[id='supplierId_"+no+"']");
        var payElem = $(layero[0]).find("select[id='payMethod_"+no+"']");
        //编辑加载产品分类
        buyingManageObj.loadGoodsType(type,goodsId,typeElem,no);
        //编辑加载供应商
        supplierManageObj.loadSupplier(supplierId,supplierElem);
        //编辑加载支付方式
        buyingManageObj.loadPayMethod(payElem,payMethod);
    },
    <!-- 删除一行申请记录 -->
    removeCheck: function (t) {
        var elem = $(t).parent().parent();
        var beforeId = $(elem[0]).find("td:nth-child(3)").children("select").attr("data-before-id");
        //选中的id
        var id = $(elem[0]).find("td:nth-child(3)").children("select").val()==null?beforeId:$(elem[0]).find("td:nth-child(3)").children("select").val();
        var typeId = $(elem[0]).find("td:nth-child(3)").children("select").attr("data-value");//选中的产品分类id
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
        //计算价格
        buyingManageObj.calculatePrice();
        buyingManageObj.removeSelectGoodsId(id);
        buyingManageObj.refreshTable(typeId);
    },
    //计算总金额(price,number)
    calculatePrice: function () {
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
        // $("#money").val(sum.toFixed(2));
    },
    //跳转新增采购页面
    showSavePurchase: function () {
        configObj.selectedIds=[];
        configObj.layerObj={};
        buyingManageObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 99999,
            content: $("#savePurchaseModal").html(),
            btn: [],
            area: ['96%', '90%'],
            offset:['30px','30px'],
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
                //隐藏附件div
                $("#affixDiv").empty();
                $("#affixDiv").hide();
                $(layero[0]).find("input[name='userId']").val(user.id);
                $(layero[0]).find("input[name='userName']").val(user.name);
                $(layero[0]).find("input[name='createTime']").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                //清空详情展示区
                $("#checkTable tbody").html("");
                $("#flag").val(-3);//flag=-2和-3允许增加删除详情
                $(".save").show();
                $(".edit").hide();
                $(".approve").hide();
                $(".send").hide();
                $(".rejectReason").hide();
                $("#showHistory1").hide();
                $(".licence").hide();
                $(".viewFooter").hide();
                $(".editFooter").show();
                //加载采购编号
                buyingManageObj.loadPurchaseCode(layero);
                //添加一行空的物品采购记录
                buyingManageObj.addCheck(layero);
                //加载用户
                buyingManageObj.loadUser(null, $(layero[0]).find("select[name='buyerId']")[0]);
                //移除disabled属性
                buyingManageObj.removePurchaseDisableStyle();

                layui.use('laydate', function () {
                    var layDate = layui.laydate;
                    layDate.render({
                        elem: $(layero[0]).find("input[name='buyTime']")[0],
                        format: 'yyyy-MM-dd',
                        istime: true,
                        istoday: true,// 是否显示今天
                        isclear: true, // 是否显示清空
                    });
                });
                layui.form.render();
            }
        });
    },
    //flag=0驳回,编辑 1 查看, 2 审核
    view: function (id, flag) {
        configObj.selectedIds=[];
        configObj.layerObj={};
        buyingManageObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 99999,
            content: $("#savePurchaseModal").html(),
            btn: [],
            area: ['96%', '90%'],
            offset:['30px','30px'],
            closeBtn: 0,
            btn: [],
            resize: true,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function (layero, index) {
                orderNumber = 0;
                //清空采购详情展示区
                $("#checkTable tbody").html("");
                configObj.layerObj=layero;
                if (flag == 0) {
                    $("#showHistory1").hide();
                    $(".licence").hide();
                    $(".viewFooter").hide();
                    $(".editFooter").show();
                    $(".confirm").hide();
                    $(".approve").hide();
                    $(".send").hide();
                    $(".rejectReason").hide();
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
                    $(".confirm").hide();
                    $(".approve").hide();
                    $(".send").hide();
                    $(".rejectReason").show();
                    $(".save").hide();
                    $(".edit").show();
                    $("#flag").val(0);//增加按钮不允许操作
                } else if (flag == 2) {//审核
                    $("#showHistory1").data("id", id);
                    $(".licence").show();
                    $(".viewFooter").show();
                    $(".editFooter").hide();
                    $(".confirm").hide();
                    $(".approve").show();
                    $(".send").hide();
                    $(".rejectReason").hide();
                    $(".save").hide();
                    $(".edit").hide();
                    $("#flag").val(0);
                }
                requestData({id:id},"/purchase/editAjax","post","json",true,function (data) {
                    if (data && data.data.entity != null) {
                        for (var attr in data.data.entity) {
                            $(layero[0]).find("[name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                            if (attr == "buyerId") {
                                //加载用户
                                buyingManageObj.loadUser(data.data.entity[attr], $(layero[0]).find("select[name='buyerId']"));
                            }
                            if (attr == "rejectReason") {
                                $(layero[0]).find("input[id='rejectReason']").val(data.data.entity[attr]);
                            }
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
                            if(attr=="buyTime"){
                                layui.use('laydate', function () {
                                    var layDate = layui.laydate;
                                    layDate.render({
                                        elem: $(layero[0]).find("input[name='buyTime']")[0],
                                        format: 'yyyy-MM-dd',
                                        istime: true,
                                        istoday: true,// 是否显示今天
                                        isclear: true, // 是否显示清空
                                    });
                                });
                                var buyTime = data.data.entity[attr];
                                if(buyTime==null || buyTime=="" || buyTime==undefined){
                                    $(layero[0]).find("input[name='buyTime']").val("");
                                }else{
                                    $(layero[0]).find("input[name='buyTime']").val(new Date(buyTime).format("yyyy-MM-dd"));
                                }
                            }
                            if(attr=="state"){
                                if(data.data.entity["state"]==14){
                                    //行政部长审核显示财务抄送框
                                    $(".send").show();
                                    buyingManageObj.loadCWBZData($(layero[0]).find("select[id='notify']"));
                                }
                                //审核完成，入库暂存，已入库
                                if(data.data.entity["state"]==1 || data.data.entity["state"]==6 || data.data.entity["state"]==7){
                                    // 财务确认
                                    if(flag!=1){
                                        //查看不用确认
                                        $(".send").hide();
                                        $(".rejectReason").hide();
                                        $(".confirm").show();
                                        $(".approve").hide();
                                        $(".licence").hide();
                                    }
                                }
                            }
                        }
                        var details = data.data.entity.purchaseDetails;
                        if (details.length > 0) {
                            for (var j = 0; j < details.length; j++) {
                                var ids = details[j].goodsId;//产品名称id
                                if(!configObj.selectedIds.contains(ids)){
                                    //先储存id到缓存中
                                    configObj.selectedIds.push(ids);
                                }
                            }
                            //之后一步一步遍历采购明细
                            for (var i = 0; i < details.length; i++) {
                                var type = details[i].type;//产品分类
                                var goodsId = details[i].goodsId;//产品名称id
                                var specs = details[i].specs;
                                var unit = details[i].unit;
                                var amount = details[i].amount;
                                var price = details[i].price;
                                var totalMoney = details[i].totalMoney;
                                var supplierId = details[i].supplierId;
                                var payMethod = details[i].payMethod;
                                buyingManageObj.addCheck(layero, type, goodsId, specs, unit, amount, price, totalMoney, supplierId, payMethod);
                            }
                            var money = $(layero[0]).find("input[name='money']").val();
                            if(money=="" || money==null || money==undefined){
                                //计算价格
                                buyingManageObj.calculatePrice();
                            }
                            if (flag == 0) {
                                //编辑添加disabled属性
                                buyingManageObj.removePurchaseDisableStyle();
                            } else if (flag == 1) {
                                //查看添加disabled属性
                                buyingManageObj.addPurchaseDisableStyle();
                            } else if (flag == 2) {
                                //审核添加disabled属性
                                buyingManageObj.addPurchaseDisableStyle();
                                $("#addPurchaseForm #remark").attr("disabled", false);
                            }
                        }
                    }else {
                        swal({
                              title:"提示",
                              text:data.msg
                        });
                    }
                });
            }
        })
    },
    goToReimbursement:function(id){
       page("/fee/expenseReimbursement?flag=-1&id="+id, "费用报销");
    },
    //删除物品采购记录
    delProduct: function (id) {
        layer.confirm("是否需要删除该记录？", {
            btn: ["确定", "取消"],
            shade: false,
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: "/purchase/delPurchase",
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {icon: 6, time: 1000});
                        //清除采购查询数据
                        buyingManageObj.cleanPurchaseData();
                        buyingManageObj.initPagerPlugin();
                    } else if (data.code == 1002) {
                        swal({
                            title: "提示",
                            text: data.msg,
                        });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }, error: function (data) {
                    Ladda.stopAll();
                }
            })
        });
    },
    //物品采购新增编辑功能
    submitProduct: function (t, url, state) {
        var formElem = $(t).closest(".stockModalCommon");
        if (buyingManageObj.checkEmpty(formElem)) {
            layer.confirm("是否保存采购信息？", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                $(formElem[0]).find("div[id='editBtn']").css("pointer-events","none");
                $(formElem[0]).find("div[id='editPurchaseBtn']").css("pointer-events","none");
                $(formElem[0]).find("div[id='addBtn']").css("pointer-events","none");
                $(formElem[0]).find("div[id='savePurchaseBtn']").css("pointer-events","none");
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
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            $(formElem[0]).find("div[id='editBtn']").css("pointer-events","auto");
                            $(formElem[0]).find("div[id='editPurchaseBtn']").css("pointer-events","auto");
                            $(formElem[0]).find("div[id='addBtn']").css("pointer-events","auto");
                            $(formElem[0]).find("div[id='savePurchaseBtn']").css("pointer-events","auto");
                            if (state == 4) {
                                //采购提交清空采购编码缓存
                                buyingManageObj.closeModal();
                                configObj.tempData.purchaseCode=[];
                            } else {
                                //添加采购记录清空采购编码缓存
                                // if(t.id==configObj.purchaseElemId){}                               // }
                                configObj.tempData.purchaseCode=[];
                                buyingManageObj.closeModal();
                                //新增跳转编辑页面(防止添加重复数据)
                                buyingManageObj.view(data.data.entity.id,0);
                            }
                            buyingManageObj.cleanPurchaseData();
                            buyingManageObj.initPagerPlugin();
                        } else if (data.code == 1002) {
                            swal({
                                title: "提示",
                                text: data.msg,
                            });
                            buyingManageObj.closeModal();
                        }
                    }, error: function (data) {
                        Ladda.stopAll();
                    }
                });
            }, function () {
                return;
            })
        }
    },
    showUploadView: function () {
        $("#buyingFile").val("");
        $("#importBuyingModal").modal('toggle');
    },
    //导出物品采购信息
    exportPurchaseDetail: function () {
        var params = $("#queryBuyingForm").serializeJson();
        var keyWords = $("#keyWordsQc").val();
        params.keyWords = keyWords;
        location.href = "/purchase/exportPurchaseDetail" + "?" + $.param(params);
    },
    //导入模板
    downTemplate: function () {
        location.href = "/purchase/exportTemplate";
    },
    //报销关联订单
    relatedReimbursement:function () {
        var tdElem = $("#purchaseTable tbody").find("input[type='checkbox']:checked");
        var ids=[];
        if(tdElem.length>0){
            for(var i=0;i<tdElem.length;i++){
                ids.push(tdElem[i].value);
            }
            requestData({ids:ids},"/purchase/checkRelatedReimbursement","post","json",true,function (data) {
               if(data.code==200){
                  if(data.data.list.length>0){
                      layer.open({
                         title:"提示",
                         content:"选中的采购单中存在已报销的采购单"
                      });
                      buyingManageObj.initPagerPlugin();
                  }else {
                      page("/fee/expenseReimbursement?purchaseFlag=1&ids="+ids, "费用报销");
                  }
               }
            });
        }else{
            layer.msg("很抱歉，没有选中记录进行操作！", {time: 2000, icon: 5});
            return;
        }
    }
}