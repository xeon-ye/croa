$(function () {
    //加载tab页
    layui.use('element', function(){
        var element = layui.element;
        element.on('tab(docDemoTabBrief)', function(data){
            $("#tabIndex").val(data.index);
            commonObj.tabChange(data.index);
        });
    });
    layui.use('form', function(){
        var $ = layui.jquery, form = layui.form;
        form.on('select(typeIdQc)', function(data){
            goodsManageObj.initPagerPlugin()
            form.render();
        });
        form.on('select(parentIdQc)', function(data){
            goodsTypeManageObj.initPagerPlugin();
            form.render();
        });
    });
    //加载产品分类进入js缓存里
    goodsManageObj.listGoodsTypeData();
    commonObj.initPage();
});

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
            //产品管理
            $("#goodsTab").css("display","block");
            goodsManageObj.initPagerPlugin();
            //产品管理加载产品分类数据
            goodsManageObj.loadGoodsType(null,"#typeIdQc",1);
        }else if(index == 1){
            //产品分类管理
            $("#goodsTypeTab").css("display","block");
            goodsTypeManageObj.initPagerPlugin();
            goodsManageObj.loadGoodsType(null,"#parentIdQc",0);
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

//产品对象
var goodsManageObj = {
    getTotalUrl:"/goodsType/getGoodsPageCount",
    goodsListUrl:"/goodsType/getGoodsPage",
    tempData:{
        //产品分类信息
        type:[],
        //产品信息
       secondType:{}
    },
    goodsType:{},
    listGoodsTypeData:function(){
        if(goodsManageObj.tempData.type && goodsManageObj.tempData.type.length<=0){
            commonObj.requestData(null,"/goodsType/loadGoodsTypeInfo","get","json",false,function (data) {
                goodsManageObj.tempData.type = data;
                for (var i=0;i<data.data.list.length;i++){
                    var id = data.data.list[i].id;
                    var name = data.data.list[i].name;
                    goodsManageObj.goodsType[id]=name;
                }
            });
        }
    },
    listSecondGoodsType:function(t){
        if(goodsManageObj.tempData.secondType[t]==undefined || goodsManageObj.tempData.secondType[t]==null){
            commonObj.requestData({parentId:t},"/goodsType/loadGoodsTypeByParentId","get","json",false,function (data) {
                goodsManageObj.tempData.secondType[t] = data;
            });
        }
    },
    callback: function (data) {
        var html = "";
        $("#goodsList").empty();
        if (data && data.list.length > 0) {
            $.each(data.list, function (i, record) {
                //根据分页插件计算序号
                var pageSize = data.pageSize;
                var pageNum = data.pageNum;
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                var typeName=goodsManageObj.goodsType[record.parentId]==undefined?"":goodsManageObj.goodsType[record.parentId];
                var createTime=record.createTime==null?"":record.createTime;
                html += "<tr>" +
                    "<td style=\"text-align: center\">"+(pageIndex + 1)+"</td>" +
                    "<td style=\"text-align: center\">"+typeName+"</td>" +
                    "<td style=\"text-align: center\">" + record.name + "</td>" +
                    "<td style=\"text-align: center\"'>"+record.code+"</td>" +
                    "<td style=\"text-align: center\">" + record.unit + "</td>" +
                    "<td style=\"text-align: center\">" + (record.specs||"") + "</td>" +
                    "<td style=\"text-align: center\">" + record.price + "</td>" +
                    "<td style=\"text-align: center\">" + record.createName + "</td>" +
                    "<td style=\"text-align: center\">" + createTime + "</td>" +
                    "<td style=\"text-align: center\">"+
                    "<a href='javascript:void(0)' style='height:22px;width:40px;margin-right: 20px'  onclick='goodsManageObj.showEditGoods(" + record.id + ")'>编辑</a>"+
                    "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='goodsManageObj.del(" + record.id + ")'>删除</a>"+
                    "</td></tr>";
            });
            //拼接内容
            $("#goodsList").html(html);
        }
    },
    //初始化表格数据
    initPagerPlugin: function() {
        var formData = $("#queryGoodsForm").serializeJson();
        //初始化分页组件
        commonObj.requestData(formData, goodsManageObj.getTotalUrl, "post", "json", true, function (data) {
            if (data && data.code == 200) {
                commonObj.pagerPlus({
                    param: formData,
                    elem:$("#goodsManagePager"),
                    count: data.data.total,
                    url: goodsManageObj.goodsListUrl,
                }, goodsManageObj.callback);
            }
        });
    },
    //判断产品名称是否重复
    checkNameRepeat: function(){
        var id = $("#addGoodsForm #id").val();
        var parentId = $("#addGoodsForm #parentId2").val();
        var name = $("#addGoodsForm #name").val();
        var flag = false;
        //判断产品分类不能为空
        if (parentId == "" || parentId == undefined || parentId == null) {
            layer.open({
                title:"提示",
                content:"产品分类不能为空",
            });
            flag =  false;
        }else{
            flag = true;
        }
        if(flag){
            //判断产品名称是否重复
            commonObj.requestData({id: id,name:name},"/goodsType/checkName","post","json",false,function (data) {
                if(data.code==200){
                    if(data.data.list.size>0){
                        layer.open({
                            title:"提示",
                            content:"已存在相同产品名称，请重新输入",
                        });
                        $("#addGoodsForm #name").val("");
                        flag = false;
                    }else{
                        flag = true;
                    }
                }else if(data.code==1002){
                    flag = false;
                    layer.open({
                        title:"提示",
                        content:data.msg,
                    });
                }
            })
        }
        return flag;
    },
    //判断是否可以删除
    checkDelInfo: function(id){
        var flag = false;
        $.ajax({
            type:"post",
            url:"/purchase/getPurchaseDetailsByGoodsId",
            data:{id:id},
            dataType:"json",
            async:false,
            success:function (data) {
                if(data.code==200){
                    if(data.data.result>0){
                        flag = false;
                        layer.open({
                            title:"提示",
                            content:"抱歉，该产品已被使用，删除不了",
                        });
                    }else{
                        flag = true;
                    }
                }else if(data.code==1002){
                    layer.open({
                        title:"提示",
                        content:data.msg
                    });
                    flag = false;
                }
            }
        });
        return flag;
    },
    //加载产品分类信息
    loadGoodsType: function(t,elem,flag,children){
        var html="<option value=''>请选择</option>";
        $(elem).empty();
        if(flag==0){
            //flag==0添加一级菜单选项
            html+="<option value='0'>一级菜单</option>";
        }
        layui.use(["form"], function () {
            goodsManageObj.listGoodsTypeData();
            var data = goodsManageObj.tempData.type;
            for (var i = 0; i < data.data.list.length; i++) {
                var goods = data.data.list[i];
                var selected = t == goods.id ? "selected=selected" : "";
                html += "<option value='" + goods.id + "' " + selected + ">" + goods.name + "</option>";
            }
            $(elem).append(html);
            layui.form.render();
            layui.form.on('select(firstType)',function (typeData) {
                //产品父级类型id
                var parentId = typeData.value;
                $("#typeId").empty();
                goodsManageObj.listSecondGoodsType(parentId);
                var data = goodsManageObj.tempData.secondType[parentId];
                var typeHtml = "<option value=''>请选择</option>";
                for(var i=0;i<data.data.list.length;i++){
                    var id = data.data.list[i].id;
                    var name = data.data.list[i].name;
                    typeHtml +="<option value='"+id+"'>"+name+"</option>";
                }
                $("#typeId").append(typeHtml);
                layui.form.render();
            });
        });
        if(t!=null && t!=""){
            if(children!=null && children!=""){
                goodsManageObj.loadGoodsTypeByParentId(t,children,"#typeId");
            }
        }
    },
    loadGoodsTypeByParentId: function(t,children,elem){
        var html="<option value=''>请选择</option>";
        $(elem).empty();
        layui.use(["form"], function () {
            goodsManageObj.listSecondGoodsType(t);
            var data = goodsManageObj.tempData.secondType[t];
            for (var i = 0; i < data.data.list.length; i++) {
                var goodsType = data.data.list[i];
                var selected = children == goodsType.id ? "selected=selected" : "";
                html += "<option value='" + goodsType.id + "' " + selected + ">" + goodsType.name + "</option>";
            }
            $(elem).append(html);
            layui.form.render();
        });
    },
    //跳转新增产品页面
    showAddGoods: function() {
        $("#addGoodsForm").find("input").removeClass('error');
        $("#addGoodsForm").find("select").removeClass("error");
        $("#addGoodsForm").validate().resetForm();
        document.getElementById("addGoodsForm").reset();
        $("#id").val("");
        $("#createId2").val(user.id);
        $("#createName2").val(user.name);
        $("#createTime2").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
        goodsManageObj.loadGoodsType(null,"#addGoodsForm #parentId2",1);
        $("#addGoodsForm .save").show();
        $("#addGoodsForm .edit").hide();
        $("#addGoodsModal").modal("show");
    },
    //跳转编辑产品页面
    showEditGoods: function(id){
        $("#addGoodsForm").find("input").removeClass('error');
        $("#addGoodsForm").find("select").removeClass("error");
        $("#addGoodsForm").validate().resetForm();
        document.getElementById("addGoodsForm").reset();
        $("#addGoodsForm .save").hide();
        $("#addGoodsForm .edit").show();
        $.ajax({
            type: "post",
            url: "/goodsType/editAjax",
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if(data.code==200){
                    if (data && data.data.entity != null) {
                        for (var attr in data.data.entity) {
                            $("#addGoodsForm [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                            if(attr=="id"){
                                var type = data.data.entity[attr];
                                var parentId = data.data.entity["parentId"];
                                goodsManageObj.loadGoodsType(parentId,"#addGoodsForm #parentId2",1,type);
                            }
                        }
                    }
                }else if(data.code==1002){
                    layer.open({
                        title:"提示",
                        content:data.msg,
                    })
                }
            }
        });
        $("#addGoodsModal").modal("show");
    },
    //新增编辑产品
    submitHander: function (t,url) {
        if($("#addGoodsForm").valid() && goodsManageObj.checkNameRepeat()){
            layer.confirm("确定保存产品信息",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                startModal("#"+t.id);
                var formData = $("#addGoodsForm").serializeForm();
                if(formData.parentId==undefined || formData.parentId==null || formData.parentId===""){
                    formData.parentId=0;
                }
                commonObj.requestData(formData,url,"post","json",true,function (data) {
                    if(data.code==200){
                        layer.msg(data.data.message,{icon:6,time:1000});
                        goodsManageObj.initPagerPlugin();
                        Ladda.stopAll();
                        $("#addGoodsModal").modal("hide");
                    }else if(data.code==1002){
                        layer.open({
                            title:"提示",
                            content:data.msg
                        });
                        Ladda.stopAll();
                        $("#addGoodsModal").modal("hide");
                    }
                });
            });
        }
    },
    //产品删除
    del:function (id) {
        if(goodsManageObj.checkDelInfo(id)){
            layer.confirm("确定删除产品信息",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                $.get("/goodsType/del",{id:id},function (data) {
                    if(data.code==200){
                        layer.msg(data.data.message,{icon:6,time:1000});
                    }else if(data.code==1002){
                        layer.open({
                           title:"提示",
                           content:data.msg
                        });
                    }
                    //刷新表格
                    goodsManageObj.initPagerPlugin();
                },"json");
            });
        }
    },
    //导入模板
    exportTemplate:function () {
      location.href="/goods/exportTemplate";
    },
    //跳转上传产品页面
    showUploadView: function(){
        $("#file").val("");
        $("#importGoodsModal").modal('toggle');
    },
    //导入产品信息
    importGoodsInfo: function () {
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
                    url: "/goods/batchGoodsForEasyExcel",
                    data: formData,
                    dataType: "json",
                    async: true,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        if (data.code == 200) {
                            layer.msg(data.data.message,{icon:6,time:1000});
                            goodsManageObj.initPagerPlugin();
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
                        Ladda.stopAll();
                        $("#importGoodsModal").modal('hide');
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

//产品分类对象
var goodsTypeManageObj={
    getTotalUrl:"/goodsType/getPageCount",
    goodsTypeListUrl:"/goodsType/listPg",
    callback: function (data) {
        var html = "";
        $("#goodsTypeList").empty();
        if (data && data.list.length > 0) {
            $.each(data.list, function (i, record) {
                //根据分页插件计算序号
                var pageSize = data.pageSize;
                var pageNum = data.pageNum;
                //序号
                var pageIndex = (pageNum - 1) * pageSize + i;//下标从0开始
                var typeName="";
                html += "<tr>" +
                    "<td style=\"text-align: center\">"+(pageIndex + 1)+"</td>" +
                    "<td style=\"text-align: center\">" + record.name + "</td>" +
                    "<td style=\"text-align: center\"'>"+record.code+"</td>" +
                    "<td style=\"text-align: center\">" + record.createName + "</td>" +
                    "<td style=\"text-align: center\">" + record.createTime + "</td>" +
                    "<td style=\"text-align: center\">"+
                    "<a href='javascript:void(0)' style='height:22px;width:40px;margin-right: 20px'  onclick='goodsTypeManageObj.showEditGoodsType(" + record.id + ")'>编辑</a>"+
                    "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='goodsTypeManageObj.del(" + record.id + ")'>删除</a>"+
                    "</td></tr>";
            });
            //拼接内容
            $("#goodsTypeList").html(html);
        }
    },
    //初始化表格数据
    initPagerPlugin: function () {
        var formData = $("#queryGoodsTypeForm").serializeJson();
        //初始化分页组件
        commonObj.requestData(formData, goodsTypeManageObj.getTotalUrl, "post", "json", true, function (data) {
            if (data && data.code == 200) {
                commonObj.pagerPlus({
                    param: formData,
                    elem: $("#goodsTypeManagePager"),
                    count: data.data.total,
                    url: goodsTypeManageObj.goodsTypeListUrl,
                }, goodsTypeManageObj.callback);
            }
        });
    },
    //判断产品分类是否使用
    checkDelInfo: function(id){
        var flag = false;
        $.ajax({
           type:"post",
           url:"/goodsType/checkGoodsTypeData",
           data:{parentId:id},
           dataType:"json",
           async:false,
           success:function (data) {
               if(data.code==200){
                   if(data.data.list.length>0){
                       flag=false;
                       layer.open({
                           title:"提示",
                           content:"抱歉，此产品分类已被使用，删除不了",
                       });
                   }
                   else{
                       flag = true;
                   }
               }else if(data.code==1002){
                   layer.open({
                       title:"提示",
                       content:data.msg
                   });
                   flag = false;
               }
            }
        });
        return flag;
    },
    //跳转新增产品分类信息页面
    showAddGoodsType: function () {
        $("#addGoodsTypeForm").find("input").removeClass('error');
        $("#addGoodsTypeForm").find("select").removeClass("error");
        $("#addGoodsTypeForm").validate().resetForm();
        document.getElementById("addGoodsTypeForm").reset();
        $('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green',
        });
        $("#addGoodsTypeForm #goodsTypeId").val("");
        $("#addGoodsTypeForm #createId").val(user.id);
        $("#addGoodsTypeForm #createName").val(user.name);
        $("#addGoodsTypeForm #createTime").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
        $("#addGoodsTypeForm .save").show();
        $("#addGoodsTypeForm .edit").hide();
        // $("input[name='isParent'][value='0']").iCheck("check");
        // $("input:radio[name='isParent']:checked").val()==1?$(".showType").hide():$(".showType").show();
        // $("input[name='isParent']").on("ifChecked", function (i) {
        //     var isParent = $(this).val();
        //     if (isParent == 1) {
        //         $(".showType").hide();
        //         $("select[name='parentId']").val("");
        //     } else {
        //         $(".showType").show();
        //     }
        // });
        // goodsManageObj.loadGoodsType(null,"#parentId",1);
        $("#addGoodsTypeModal").modal("show");
    },
    //跳转编辑产品分类页面
    showEditGoodsType: function(id){
        $("#addGoodsTypeForm").find("input").removeClass('error');
        $("#addGoodsTypeForm").find("select").removeClass("error");
        $("#addGoodsTypeForm").validate().resetForm();
        document.getElementById("addGoodsTypeForm").reset();
        $("#addGoodsTypeForm .save").hide();
        $("#addGoodsTypeForm .edit").show();
        $('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green',
        });
        $.ajax({
            type: "post",
            url: "/goodsType/editAjax",
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data && data.data.entity != null) {
                    for (var attr in data.data.entity) {
                        $("#addGoodsTypeForm [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                    }
                }
            }
        });
        $("#addGoodsTypeModal").modal("show");
    },
    //新增编辑产品分类
    submitHander: function (t,url) {
        if($("#addGoodsTypeForm").valid()){
            layer.confirm("确定保存产品分类信息",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                startModal("#"+t.id);
                var formData = $("#addGoodsTypeForm").serializeForm();
                commonObj.requestData(formData,url,"post","json",true,function (data) {
                    if(data.code==200){
                        layer.msg(data.data.message,{icon:6,time:1000});
                        goodsTypeManageObj.initPagerPlugin();
                        goodsManageObj.tempData.type = [];
                        goodsManageObj.tempData.secondType = {};
                        $("#addGoodsTypeModal").modal("hide");
                    }else if(data.code==1002){
                        layer.open({
                            title:"提示",
                            content:data.msg
                        });
                    }
                    Ladda.stopAll();
                });
            })
        }
    },
    //产品分类删除
    del:function (id) {
        if(goodsTypeManageObj.checkDelInfo(id)){
            layer.confirm("确定删除产品分类信息",{
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                $.get("/goodsType/del",{id:id},function (data) {
                    if(data.code==200) {
                        layer.msg(data.data.message, {icon: 6, time: 1000});
                        goodsManageObj.tempData.type = [];
                        goodsManageObj.tempData.secondType = {};
                    }else if(data.code==1002){
                        layer.open({
                           title:"提示",
                           content:data.msg
                        });
                    }
                    //刷新表格
                    goodsTypeManageObj.initPagerPlugin();
                },"json");
            });
        }
    }
}