var addNewsContentObj;
$(function () {
    //新闻富文本编辑
    addNewsContentObj = new KindeditorTool({
        targetEl: "#content",
        uploadUrl: "/editUpload?filePart=news",
    });

    newsObj.init();
});

//新闻资讯
var newsObj = {
    getTotalUrl:"/news/getNewsTotal",
    newsListUrl:"/news/admin",
    newsType:{1:"集团新闻", 2:"公司新闻", 3:"图文新闻", 4:"优秀事迹"},
    newsTypeCls:{1:"jituan", 2:"gongsi", 3:"", 4:"youxiu"},
    newsListCallback: function (data, target, seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                if(record && record.id){
                    seq += 1;
                    html += "<tr>\n" +
                        "       <td >\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "               <input value='"+record.id+"' type=\"checkbox\" lay-skin=\"primary\">\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\""+seq+"\">\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "               "+seq+"\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\""+(newsObj.newsType[record.type] || "")+"\">\n" +
                        "           <div class=\"ellipsisContent "+(newsObj.newsTypeCls[record.type] || "")+"\">\n" +
                        "               "+(newsObj.newsType[record.type] || "")+"\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\""+(record.title || "")+"\">\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "                <a href=\"/news/admin/view/"+record.id+"\">"+(record.title || "")+"</a>\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\""+(record.createTime || "")+"\">\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "               "+(record.createTime || "")+"\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\"操作\">\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "                <button class=\"tableButton blueBtn\" type=\"button\" onclick='newsObj.editClick("+JSON.stringify(record)+");'>\n" +
                        "                    修改\n" +
                        "                </button>\n" +
                        "                <button id='del"+record.id+"' class=\"tableButton orangeBtn\" type=\"button\" onclick='newsObj.del(this,"+record.id+");'>\n" +
                        "                    删除\n" +
                        "                </button>\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "    </tr>";
                }
            });
        }
        var $parentList = target ? $(target) : $("#courseExamList");
        $parentList.html(html);
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    init: function () {
        //全选按钮初始化
        $("#newsAllChoose").removeAttr("checked");

        var param = $("#newsQueryForm").serializeJson();
        //初始化分页组件
        commonObj.requestData(param,newsObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $("#newsPager"),
                    count: data.data.total,
                    url: newsObj.newsListUrl,
                    target: $(".newsList"),
                    param: param,
                },newsObj.newsListCallback);
            }
        });
    },
    addClick:function () {
        $("#modalTitle").text("新增新闻");
        $("#newsId").val("");
        $("#newsTitle").val("");
        addNewsContentObj.setContent("");
        $("#saveBtn").css("display", "inline-block");
        $("#editBtn").css("display", "none");
        $("#addNews").modal("toggle");
    },
    add:function () {
        var content = addNewsContentObj.getContent();
        if(!content){
            layer.msg("新闻内容不能为空！", {time: 3000, icon: 5});
            return;
        }
        var param = $("#newsForm").serializeJson();
        param.content = content || "";
        startModal("#saveBtn");
        commonObj.requestData(param, "/news", "post", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code === 200){
                layer.msg("新增成功！", {time: 3000, icon: 6});
                $("#addNews").modal("toggle");
                newsObj.init();
            }else{
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    },
    editClick:function (news) {
        $("#modalTitle").text("编辑新闻");
        $("#newsId").val(news.id);
        $("#newsTitle").val(news.title);
        $("#newsType").val(news.type);
        addNewsContentObj.setContent(news.content);
        $("#saveBtn").css("display", "none");
        $("#editBtn").css("display", "inline-block");
        $("#addNews").modal("toggle");
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    edit:function () {
        var param = $("#newsForm").serializeJson();
        if(!param.id){
            layer.msg("新闻唯一标识不能为空！", {time: 3000, icon: 5});
            return;
        }
        var content = addNewsContentObj.getContent();
        if(!content){
            layer.msg("新闻内容不能为空！", {time: 3000, icon: 5});
            return;
        }
        param.content = content || "";
        startModal("#editBtn");
        commonObj.requestData(param, "/news/edit", "post", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code === 200){
                layer.msg("编辑成功！", {time: 3000, icon: 6});
                $("#addNews").modal("toggle");
                newsObj.init();
            }else{
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    },
    del:function (t, id) {
        startModal("#" + $(t).attr("id"));
        commonObj.requestData(null, "/news/" + id, "delete", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code === 200){
                layer.msg("删除成功！", {time: 3000, icon: 6});
                newsObj.init();
            }else{
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    },
    batchDel:function () {
        var $tbody = $(".newsList");
        var $inputArr = $tbody.find('input[type="checkbox"]:checked');
        if($inputArr.length < 1){
            layer.msg("很抱歉，没有选中记录进行操作！", {time: 2000, icon: 5});
            return;
        }
        var ids = [];
        $inputArr.each(function (i, input) {
            ids.push($(input).val());
        });
        var param = {ids:ids};
        startModal("#batchDelNews");
        commonObj.requestData(param, "/news/batchDel", "post", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code === 200){
                layer.msg("删除成功！", {time: 3000, icon: 6});
                newsObj.init();
            }else{
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    }
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
            error:function () {
                Ladda.stopAll();//隐藏加载按钮
            }
        };
        if(callErrorFun){
            param.error = callErrorFun;
        }
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    //获取用户权限
    getTrainPermission:function () {
        commonObj.requestData(null, "/trainSetting/getTrainPermission", "post", "json", false, function (data) {

        });
    },
    //根据权限控制tab栏目
    controlTab:function () {
        //管理员有课程审批功能
        if(commonObj.isAdmin){
            $("#courseAuditTab").css("display", "inline-block");
        }else {
            $("#courseAuditTab").css("display", "none");
        }

        //讲师和管理员有课程管理、试卷管理功能
        if(commonObj.isAdmin || commonObj.isTeacher){
            $("#courseManageTab").css("display", "inline-block");
            $("#courseExamTab").css("display", "inline-block");
        }else {
            $("#courseManageTab").css("display", "none");
            $("#courseExamTab").css("display", "none");
        }
    },
    //页面进入
    initBefore: function () {

    },
    //Tab切换处理事件
    tabChange: function (index) {
        $(".tabContent").css("display","none");
        $(".layui-tab-title").find("li").removeClass("layui-this");
        if(commonObj.tabs[index]){
            $("#"+commonObj.tabs[index].id).css("display","block");
            $("#"+commonObj.tabs[index].tabId).addClass("layui-this");
            if(commonObj.tabs[index].obj){
                commonObj.tabs[index].obj.init(); //列表展示
            }
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
                limits: config.limits || [15, 30, 50, 100],
                limit: config.limit || 15,
                jump: function (obj, first) {
                    config.param = config.param || {};
                    config.param.size = obj.limit;
                    config.param.page = obj.curr;
                    commonObj.requestData(config.param, config.url, "post", "json", true, function (data) {
                        if(callback){
                            if(config.target){
                                callback(data,config.target, (config.param.size * (config.param.page - 1)));
                            }else {
                                callback(data,null, (config.param.size * (config.param.page - 1)));
                            }
                        }
                    });
                }
            });
        });
    },
    //回车键事件
    enterEvent:function (tabIndex, methonName, event, sourceNode) {
        if((event.keyCode == '13' || event.keyCode == 13) && commonObj.tabs[tabIndex].obj && commonObj.tabs[tabIndex].obj[methonName]){
            if(sourceNode){
                commonObj.tabs[tabIndex].obj[methonName](sourceNode);
            }else {
                commonObj.tabs[tabIndex].obj[methonName]();
            }

        }
    },
}