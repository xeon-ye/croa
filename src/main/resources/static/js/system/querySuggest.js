var chargeUserArray = [];
$(function () {
    layui.use('element', function(){
        var element = layui.element;
        element.on('tab(docDemoTabBrief)', function(data){
            $("#tabIndex").val(data.index);
            commonObj.tabChange(data.index);
        });
    });

    $("#query_suggest_table").jqGrid({
        url: "/dict/queryProposeDict",
        datatype: "json",
        postData: {nameQc:$("#nameQc").val(),id:$("#id").val()}, //发送数据
        mtype: "post",
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
        multiselect: false,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [
            {name: 'id', label: '编号', editable: true, hidden: true, width: 60},
            {name: 'name', label: '建议名称', editable: true, width: 80,sortable:false},
            {name: 'createId', label: '创建人id', editable: true,hidden: true, width: 80,sortable:false},
            {name: 'createUser', label: '创建人', editable: true, width: 60,sortable:false},
            {name: 'deptName', label: '建议负责人',editable: true, width: 60,sortable:false},
            {name: 'createTime', label: '创建时间', editable: true, width: 100,sortable:false,
                formatter:function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }},
            {
                name: 'operate', label: "操作", index: '', width: 80,
                formatter: function (value, grid, rows, state) {
                    var html = "<a onclick='edit(" + rows.id + ")'>编辑</a>&nbsp;&nbsp;";
                    html += "<a onclick='del(" + rows.id + ")'>删除</a>&nbsp;&nbsp;";
                    return html;
                }
            }
        ],
        pager: jQuery("#query_suggest_pager"),
        viewrecords: true,
        caption: "建议类型",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
        },
    });

    $('body').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            //回车执行查询
            $("#suggestSearch").click();
        }
    });

    $("#suggestSearch").click(function () {
        $("#query_suggest_table").emptyGridParam();
        $("#query_suggest_table").jqGrid("setGridParam", {
            postData: {nameQc:$("#nameQc").val(),id:null}
        }).trigger("reloadGrid");
    });

    $("#suggestTipsSearch").click(function () {
        $("#query_suggestTips_table").emptyGridParam();
        $("#query_suggestTips_table").jqGrid("setGridParam", {
            postData: {typeQc:$("#typeQc").val(),stateQc:$("#stateQc").val()}
        }).trigger("reloadGrid");
    });

    $("#addSuggest").click(function () {
        $("#editModal").modal("toggle");
        document.getElementById("editForm").reset();
        $("#createId").val(user.id);
        $("#createUser").val(user.name);
        $("#createTime").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
        $(".save").show();
        $(".update").hide();
        $("#id").val("");
        $("#desc").html("");
        $("#type").load();
        $("#type").find("option").each(function (i,t) {
            $(t).remove(); //清空时移除当前option
        });
    });
});

var editToolObj = new KindeditorTool({
    targetEl: "#addTipsContent",
    uploadUrl: "/editUpload?filePart=suggest",
    items:[
        'undo', 'redo', '|', 'preview', 'print', 'template',
        'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
        'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent',
         'clearhtml', 'quickformat', 'selectall', '|','/',
        'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
        'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image', 'multiimage','hr', 'emoticons'
    ]
});

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
            //建议类型管理
            $("#proposeTypeTab").css("display","block");
        }else if(index == 1){
            //建议提示内容管理
            $("#proposeContentTab").css("display","block");
            suggestTipsObj.initPage();
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
//建议提示对象
var suggestTipsObj={
    initPage:function(){
        $("#query_suggestTips_table").jqGrid({
            url: "/proposeTips/listPg",
            datatype: "json",
            postData: {typeQc:$("#typeQc").val(),stateQc:$("#stateQc").val()}, //发送数据
            mtype: "post",
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
            multiselect: false,
            multiselectWidth: 25, //设置多选列宽度
            sortable: "true",
            sortname: "id",
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 10,//每页显示记录数
            rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
            jsonReader: {
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: "id"
            },
            colModel: [
                {name: 'id', label: '提示id', editable: true, hidden: true, width: 60,sortable:false},
                {name: 'type', label: '配置类型名称', editable: true, width: 100,sortable:false,
                    formatter:function (value, grid, rows, state) {
                        if(value==1){
                            return "提示内容";
                        }else if(value==2){
                            return "制度链接";
                        }else{
                            return "其它";
                        }
                    }
                },
                {name: 'state', label: '状态', editable: true, width: 60,sortable:false,
                    formatter:function (value, grid, rows, state) {
                        if(value==0){
                            return "已保存";
                        }else if(value==1){
                            return "已启用";
                        }else if(value==2){
                            return "<span class='text-red'>已停用</span>";
                        }
                    }
                },
                {name: 'content', label: '建议提示', editable: true, width: 120,sortable:false,
                   formatter:function (value, grid, rows, state) {
                       return "<div style='overflow: hidden;white-space: nowrap;text-overflow: ellipsis;max-height: 120px'>"+value+"</div>";
                   }
                },
                {name: 'documentName', label: '制度名称',editable: true, width: 100,sortable:false,
                    formatter:function (value, grid, rows, state) {
                        return "<a href='javascript:void(0)' onclick='suggestTipsObj.goToDocument("+rows.documentId+")' target='_blank'>"+value+"</a>";
                    }
                },
                {name: 'createId', label: '创建人id',editable: true,hidden:true,width: 30,sortable:false},
                {name: 'createName', label: '创建人',editable: true, width: 80,sortable:false},
                {name: 'createTime', label: '创建时间', editable: true, width: 100,sortable:false,
                    formatter:function (d) {
                        if (!d) {
                            return "";
                        }
                        return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                    }
                },
                {name: 'companyCode', label: '公司代码',editable: true,hidden:true,width: 60,sortable:false},
                {
                    name: 'operate', label: "操作", index: '', width: 80,
                    formatter: function (value, grid, rows, state) {
                        var html="";
                        if(rows.type==1){
                            if(rows.state==0 || rows.state==2){
                                html += "<a onclick='suggestTipsObj.suggestEnable(" + rows.id + ")'>启用</a>&nbsp;&nbsp;";
                            }else {
                                var html = "<a onclick='suggestTipsObj.suggestDisable(" + rows.id + ")'>停用</a>&nbsp;&nbsp;";
                            }
                        }
                        if(rows.type==2){
                            if(rows.state==0 || rows.state==2){
                                html += "<a onclick='suggestTipsObj.documentEnable(" + rows.id + ")'>启用</a>&nbsp;&nbsp;";
                            }else {
                                var html = "<a onclick='suggestTipsObj.documentDisable(" + rows.id + ")'>停用</a>&nbsp;&nbsp;";
                            }
                        }
                        html += "<a onclick='suggestTipsObj.editSuggestTips(" + rows.id + ","+rows.type+")'>编辑</a>&nbsp;&nbsp;";
                        html += "<a onclick='suggestTipsObj.delSuggestTips(" + rows.id + ")'>删除</a>&nbsp;&nbsp;";
                        return html;
                    }
                }
            ],
            pager: jQuery("#query_suggestTips_pager"),
            viewrecords: true,
            caption: "建议提示管理",
            add: false,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false,
            ondblClickRow: function (rowid, iRow, iCol, e) {
            },
        });
    },
    goToDocument:function(id){
        page("/documentLibrary/library/view/"+id,"制度管理");
    },
    flushTable:function(){
        $("#query_suggestTips_table").emptyGridParam();
        $("#query_suggestTips_table").jqGrid("setGridParam", {
            postData: {typeQc:$("#typeQc").val(),stateQc:$("#stateQc").val()}
        }).trigger("reloadGrid");
    },
    loadDocumentList:function(t,id){
        commonObj.requestData({state:0},"/documentLibrary/getDocumentLibraryList","post","json",true,function (data) {
            var html="";
            $(t).empty();
            layui.use(["form"], function () {
                if(data.length>0){
                    for(var i=0;i<data.length;i++){
                        var selected=id==data[i].id?"selected=selected":"";
                        html+="<option value='"+data[i].id+"' "+selected+">"+data[i].libraryName+"</option>";
                    }
                    $(t).append(html);
                }
                layui.form.render();
            });
        });
    },
    showSuggestTips:function () {
        $("#editSuggestTipsModal").modal("toggle");
        document.getElementById("editSuggestTipsForm").reset();
        $("#createId2").val(user.id);
        $("#createName2").val(user.name);
        $("#createTime2").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
        $(".save").show();
        $(".update").hide();
        $("#id2").val("");
        $("#remark2").html("");
        editToolObj.setContent("");
    },
    showSuggestDocument:function(){
        $("#editSuggestDocumentModal").modal("toggle");
        document.getElementById("editSuggestDocumentForm").reset();
        $("#createId3").val(user.id);
        $("#createName3").val(user.name);
        $("#createTime3").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
        $(".save").show();
        $(".update").hide();
        $("#id3").val("");
        $("#remark3").html("");
        suggestTipsObj.loadDocumentList("#documentId",null);
    },
    checkEmpty:function(){
        var content = editToolObj.getContent();
        if(content==null || content=='' || content==undefined){
            layer.msg("建议提示不能为空");
            return false;
        }else{
            return true;
        }
    },
    submitSuggestTips:function (t,url) {
       if($("#editSuggestTipsForm").valid() && suggestTipsObj.checkEmpty()){
           layer.confirm("是否保存建议提示？",{
               btn:["确定","取消"]
           },function (index) {
               layer.close(index);
               var formData=$("#editSuggestTipsForm").serializeForm();
               commonObj.requestData(formData,url,"post","json",true,function (data) {
                   if(data.code==200){
                       layer.msg(data.data.message, {time: 1000, icon: 6});
                       suggestTipsObj.flushTable();
                       $("#editSuggestTipsModal").modal("hide");
                   }else if(data.code==1002){
                       swal({
                           title:"提示",
                           text:data.msg
                       })
                   }
               });
           })
       }
    },
    submitSuggestDocument:function (t,url) {
        if($("#editSuggestDocumentForm").valid()){
            layer.confirm("是否保存建议制度？",{
                btn:["确定","取消"]
            },function (index) {
                layer.close(index);
                var formData=$("#editSuggestDocumentForm").serializeForm();
                commonObj.requestData(formData,url,"post","json",true,function (data) {
                    if(data.code==200){
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        suggestTipsObj.flushTable();
                        $("#editSuggestDocumentModal").modal("hide");
                    }else if(data.code==1002){
                        swal({
                            title:"提示",
                            text:data.msg
                        })
                    }
                });
            })
        }
    },
    //建议提示启用
    suggestEnable:function (id) {
        commonObj.requestData({id:id,state:1},"/proposeTips/editTipsState","post","json",true,function (data) {
            if(data.code==200){
                layer.msg(data.data.message,{time:1000,icon:6});
                suggestTipsObj.flushTable();
            }else if(data.code==1002){
                swal({
                    title:"提示",
                    test:data.msg
                })
            }else {
                return;
            }
        });
    },
    //建议提示停用
    suggestDisable:function (id) {
        commonObj.requestData({id:id,state:2},"/proposeTips/editTipsState","post","json",true,function (data) {
            if(data.code==200){
                layer.msg(data.data.message,{time:1000,icon:6});
                suggestTipsObj.flushTable();
            }else if(data.code==1002){
                swal({
                    title:"提示",
                    test:data.msg
                })
            }else {
                return;
            }
        });
    },
    //建议制度启用
    documentEnable:function (id) {
        commonObj.requestData({id:id,state:1},"/proposeTips/editDocumentState","post","json",true,function (data) {
            if(data.code==200){
                layer.msg(data.data.message,{time:1000,icon:6});
                suggestTipsObj.flushTable();
            }else if(data.code==1002){
                swal({
                    title:"提示",
                    test:data.msg
                })
            }else {
                return;
            }
        });
    },
    //建议制度停用
    documentDisable:function (id) {
        commonObj.requestData({id:id,state:2},"/proposeTips/editDocumentState","post","json",true,function (data) {
            if(data.code==200){
                layer.msg(data.data.message,{time:1000,icon:6});
                suggestTipsObj.flushTable();
            }else if(data.code==1002){
                swal({
                    title:"提示",
                    test:data.msg
                })
            }else {
                return;
            }
        });
    },
    //修改建议提示跳转页面
    editSuggestTips:function(id,type){
        var elem="";
        //建议提示
        if(type==1){
            $("#editSuggestTipsModal").modal("toggle");
            document.getElementById("editSuggestTipsForm").reset();
            $(".save").hide();
            $(".update").show();
            $("#id2").val("");
            $("#remark2").html("");
            elem="#editSuggestTipsForm";
        }else{
            //制度链接
            $("#editSuggestDocumentModal").modal("toggle");
            document.getElementById("editSuggestDocumentForm").reset();
            $(".save").hide();
            $(".update").show();
            $("#id3").val("");
            $("#remark3").html("");
            elem="#editSuggestDocumentForm";
        }
        commonObj.requestData({id:id},"/proposeTips/getById","post","json",true,function (data) {
            if(data.code==200){
                if(data.data.entity!=null){
                    for(var attr in data.data.entity){
                        $(elem).find("[name="+attr+"]").val(data.data.entity[attr]);
                        if(type==1){
                            if(attr=="content"){
                                editToolObj.setContent(data.data.entity[attr]);
                            }
                        }else if(type==2){
                            if(attr=="documentId"){
                               suggestTipsObj.loadDocumentList("#documentId",data.data.entity[attr]);
                            }
                        }
                    }
                }
            }else{
                return;
            }
        })

    },
    //删除建议提示
    delSuggestTips:function (id) {
        commonObj.requestData({id:id},"/proposeTips/delSuggestTips","post","json",true,function (data) {
            if(data.code==200){
                layer.msg(data.data.message,{time:1000,icon:6});
                suggestTipsObj.flushTable();
            }else if(data.code==1002){
                swal({
                    title:"提示",
                    test:data.msg
                })
            }else {
                return;
            }
        });
    }
}

/**
 * 建议类型添加修改调用的方法
 * @param t
 * @param url
 */
function submitSuggest(t, url) {
    if($("#editForm").valid()){
        if(checkRepeat()) {
            var str = "";
            var obj = document.getElementById("type");
            //获取所有的option
            var options = obj.options;
            for (var i = 0; i < options.length; i++) {
                if (options[i].selected) {
                    str += options[i].value + ",";
                }
            }
            if (str != null && str != "") {
                layer.confirm("请确认建议", {
                    btn: ["确定", "取消"],
                    shade: false
                }, function (index) {
                    layer.close(index);
                    startModal("#" + t.id);//锁定按钮，防止重复提交
                    $("#ids").val(str);
                    // var formData = $("#editForm").serializeJson();
                    var formData = $("#editForm").serializeForm();
                    $.ajax({
                        type: "post",
                        url: url,
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                layer.msg(data.data.message, {time: 1000, icon: 6});
                                $("#query_suggest_table").jqGrid("setGridParam", {
                                    postData: {name: $("#nameQc").val(), id: null}
                                }).trigger("reloadGrid");
                                $("#editModal").modal("hide");
                            }
                        }
                    });
                }, function () {
                    return;
                });
            } else {
                swal("请选择负责人");
                return;
            }
        }else{
            swal("建议类型已存在");
        }
    }
}

/**
 * 核实建议类型是否重复
 * @returns {boolean}
 */
function checkRepeat() {
    var flag = false;
    $.ajax({
        type: "post",
        url: "/dict/queryProposeDict",
        data: {nameQc:$("#name").val(),id:$("#id").val()},
        dataType: "json",
        async:false,
        success: function (data) {
            if(data.list.length>0){
                flag = false;
            }else{
                flag = true;
            }
        }
    });
return flag;
}

/**
 * 删除建议类型
 * @param id
 */
function del(id){
    if(checkHasPropose(id)){
        layer.confirm("确认删除建议", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            $.ajax({
                type: "post",
                url: "/dict/delAdvice",
                data: {id:id},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $("#query_suggest_table").jqGrid("setGridParam", {
                        postData: {name: $("#nameQc").val(), id: null}
                    }).trigger("reloadGrid");
                }
            });
        }, function () {
            return;
        });
    }else{
        layer.msg("该建议类型下有建议不能删除");
    }
}

function checkHasPropose(id) {
    var flag = false;
    $.ajax({
        type: "post",
        url: "/propose/queryAdviceById",
        data: {id:id},
        dataType: "json",
        async:false,
        success: function (data) {
            if(data.data.number>0){
                flag = false;
            }else{
                flag = true;
            }
        }
    });
    return flag;
}

/**
 * 修改建议类型
 * @param id
 */
function edit(id){
     $("#editModal").modal("toggle");
     $(".update").show();
     $(".save").hide();
     $("#type").find("option").each(function (i,t) {
        $(t).remove(); //清空时移除当前option
     });
     //获取编辑回显的数据
    $.ajax({
        type: "post",
        url: "/propose/showUsers",
        data: {id:id},
        dataType: "json",
        async:false,
        success: function (data) {
           var list = data.data.list;
            chargeUserArray=[];
           for(var i=0;i<list.length;i++){
               //将用户类赋给数组，多熟悉js数组的用法
               chargeUserArray[i] = list[i];
           }
        }
    });
    $.ajax({
         type: "post",
         url: "/dict/editAjax",
         data: {id:id},
         dataType: "json",
        async:false,
         success: function (data) {
             for(var attr in data.data.entity){
                 $("#editForm [name='"+attr+"']").val(data.data.entity[attr]);
                 if(attr=="desc"){
                     $("#editForm #desc").empty();
                     $("#editForm #desc").html(data.data.entity[attr]);
                 }
                 if(attr=="createTime"){
                     var time = data.data.entity[attr];
                     $("#editForm #createTime").val(new Date(time).format("yyyy-MM-dd hh:mm:ss"));
                 }
             }
         }
     });
    $("#type").load();
}

/**
 * 加载所有用户
 * @param t
 * */
function loadUser(t) {
    var selected = null;
    if(chargeUserArray && chargeUserArray.length>0){
        // console.log(chargeUserArray);
        selected = new Array();
        for(var i=0;i<chargeUserArray.length;i++){
            var temp = {id:chargeUserArray[i].id, text:chargeUserArray[i].name}
            selected.push(temp);
        }
    }
    var options = {
        url: "/propose/listByAdvice",      // 数据接口url
        size: 30, // 每次加载的数据条数
        value: "id",  // 下拉框value字段名称
        name: ["name","deptName"], // 下拉框显示字段名称
        selected: selected,  // 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
        params: {name:$("#type").text()},
        placeholder: "请选择负责人"
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
 * selected 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
 * formatResult 返回结果回调函数，可以在该回调中，自定义下拉框数据的显示样式，比如：加入图片等
 * templateSelection 选中项回调，该参数必须与formatResult参数搭配使用
 * 注意点1 : 后端接口需返回 data（具体数据）和 total（总页数）两个字段
 * 注意点2 : 两个自定义的回调函数中，必须要把处理结果return回来，如果没有传入formatResult参数，则采用默认的显示样式
 */
function ajaxSelect2(obj, options, formatResult, formatSelected) {
    var value = options["value"];
    var name = options["name"];
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
                        }
                        // console.log(cbData);
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