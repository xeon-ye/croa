var configObj={
    deptTreeData:{},
    loadDeptTreeData:function () {
        //获取从首页进入的公司代码
        var checkCompanyCode = getQueryString("companyCode");
        // 只有自己公司代码是JT,进入的公司代码也为JT，组织架构才为集团架构，否则根据自己公司代码展示部门组织
        var flag = (checkCompanyCode=='JT' && user.companyCode=='JT')?1:0; //flag=1为集团架构，否则为分公司架构
        if(isEmptyObject(configObj.deptTreeData)){
            requestData({flag:Number(flag),companyCode:checkCompanyCode},"/topic/queryDeptTree","get","json",false,function (data) {
                configObj.deptTreeData=data;
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

$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    //发帖附件上传js
    var fileUploadObj=new FileUpload({
        targetEl: '#word2htmlForm',
        multi: false,
        filePart: "article",
        completeCallback: function (data) {
            if (data.length > 0)//oriName
            $("#affixLink").val(data[0].file);
            $("#affixName").val(data[0].oriName);
            $("#showFile").attr("href",data[0].file);
            $("#showName").html(data[0].oriName);
        }
    });

    /**
     * 主题图片上传对象
     * @type {FileUpload}
     */
    var imageUploadObj=new FileUpload({
        targetEl: '#imageUploadForm',
        multi: false,
        filePart: "bbs",
        acceptSuffix:["jpg","png","gif","jpeg"],
        completeCallback: function (data) {
            if (data.length > 0)//oriName
                $("#imageUrl").val(data[0].file);
            $("#imageShowUrl").attr("href",data[0].file);
            $("#imageName").html(data[0].oriName);
            $("#imageTips").val(data[0].oriName);
        }
    });

    //关闭发帖页面
    $("#closeBtn").click(function () {
        //去除可见人员，可评论人员数据残留
        $("#editForm #users").val("");
        $("#editForm #commentUsers").val("");
        $("#editForm #comm").val("");
        $("#editModal").modal("hide");
    });

    //关闭发帖权限设置页面
    $("#cancel3").click(function () {
        $("#editForm #users").val("");
        $("#editForm #commentUsers").val("");
        $("#editForm #comm").val("");
        //重新渲染发帖设置及主题图设置的单选按钮
        initRadioState();
        $("#queryResourceModal").modal("hide");
    });

    //渲染ichecks
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    $("#word2html").click(function () {
        fileUploadObj.upload();
    });

    $("#imageUpload").click(function () {
        imageUploadObj.upload();
    });

    /**
     * 帖子可见赋值
     */
    $("#saveTopicUser").click(function () {
        //清空历史数据
        $("#editForm #users").val("");
        $("#editForm #commentUsers").val("");
        $("#editForm #comm").val("");
        //lookFlag=1部分可见,2全部可见
        var lookFlag = $("#lookSetting").val();
        if(lookFlag==1){
            var obj = document.getElementsByName("userIds");
            //可见ids
            var str ="";
            //可评论ids
            var checkStr = "";
            if(obj!=null){
                $.each(obj,function (i,item) {
                    if (obj[i].checked) {
                        checkStr += obj[i].value +",";
                    }
                    str += item.value +",";
                })
            }
            //为全部可见评论人员赋值
            if(str!=null && str!=""){
                //为帖子可见人员赋值
                $("#editForm #users").val(str);
                //为帖子可评论人员赋值
                $("#editForm #commentUsers").val(checkStr);
            }else{
                layer.open({
                    title:"提示",
                    content:"请选择可见人员"
                });
                return;
            }
        }else{
            var obj = document.getElementsByName("commIds");
            //全部可见评论人员ids
            var ids ="";
            if(obj!=null){
                $.each(obj,function (i,item) {
                    ids += item.value +",";
                })
            }
            //为全部可见评论人员赋值
            if(ids!=null && ids!=""){
                $("#editForm #comm").val(ids);
            }else{
                layer.open({
                   title:"提示",
                   content:"请选择可评论人员"
                });
                return;
            }
        }
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        //为编辑时初始化可见单选框状态，帖子可见设置展示
        initRadioState();
        $("#queryResourceModal").modal("hide");
    });

    //帖子可见设置展示
    $('#editForm input:radio[name="viewSetting"]').on('ifChanged', function (event) {
        if ($('#editForm input:radio[name="viewSetting"]:checked').val() == 0) {
            //如果是是，则跳转到帖子可见设置
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            //1部分可见展示部门树
            showDeptUser(1);
        }else{
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            //2全部可见展示部门树
            showDeptUser(2);
        }
    });

    //主题图设置展示
    $('#editForm input:radio[name="imageFlag"]').on('ifChanged', function (event) {
        // console.log($('#editForm input:radio[name="imageFlag"]:checked').val());
        if ($('#editForm input:radio[name="imageFlag"]:checked').val() == 1) {
            //若选择上传主题图片，展示上传图片框
            $("#imageDiv").show();
        }else{
            $("#imageDiv").hide();
            $("#imageUrl").val("");
            $("#imageShowUrl").attr("href","");
            $("#imageName").html("");
            $("#imageTips").val("");
        }
    });

    if (getQueryString("flag") != null && getQueryString("flag") != "" && getQueryString("flag") != undefined) {
        var flag = getQueryString("flag");
        var topicId = getQueryString("topicId");
        var companyCode = getQueryString("companyCode");
        var forumId = getQueryString("forumId");
        if(flag==2){
            //编辑帖子信息
            $("#companyCode").val(companyCode);
            $("#topicTypeVal").val(forumId);
            //可见单选按钮状态设置
            dealState(topicId);
            edit(topicId);
        }else if(flag==7){
            //删除帖子
            $.get("/forum/queryForumById",{id:forumId},function (data) {
                $("#showbbsType").html(data.data.entity["name"]);
            },"json");
            // topicObj.initPagerPlugin();
        }else if(flag==4){
            //跳转到分公司
            $("#companyCode").val(companyCode);
        }else if(flag==9){
            //分公司跳转到集团
            $("#companyCode").val(companyCode);
        }
    }
    //获取板块名称及版主
    var forumName=getQueryString("forumName");
    var moderator=getQueryString("moderator");
    if(forumName!=null){
        $("#showbbsType").html(forumName);
        $("#forumName").val(forumName);
    }
    $("#moderator").val(moderator);
    //分页初始化
    topicObj.initPagerPlugin();
    //判断用户是否有发帖权限
    showCompanyResource();
    //更新统计当日发帖数及热门推荐的帖子信息
    queryByToday();

    /**
     * 点击管理中心跳转到管理帖子页面
     */
    $("#manageCenter").click(function () {
        var companyCode = getQueryString("companyCode");
        var forumId = getQueryString("forumId");
        var forumName = getQueryString("forumName")|| $("#showbbsType").html();
        location.href="/bbs/post_list?flag=2&companyCode="+companyCode+"&forumId="+forumId+"&forumName="+forumName;
    });

    $("#addBtn").click(function () {
        $("#editForm").find("input").removeClass('error');
        $("#editForm").find("select").removeClass("error");
        $("#editForm").validate().resetForm();
        document.getElementById("editForm").reset();
        $("#editModal").modal("toggle");
        $("#userId").val(user.id);
        $("#userName").val(user.name);
        $("#createTime").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
        $("#forumId").empty();
        //清空附件缓存
        $("#affixLink").val("");
        $("#affixName").val("");
        $("#showFile").attr("href","");
        $("#showName").html("");
        //清除出题图缓存
        $("#imageDiv").hide();
        $("#imageUrl").val("");
        $("#imageShowUrl").attr("href","");
        $("#imageName").html("");
        $("#imageTips").val("");
        //清空论坛内容
        editToolObj.setContent("");
        //论坛板块id
        var companyCode = getQueryString("companyCode");
        $("#companyCode2").val(companyCode);
        var tid = $("#topicTypeVal").val();
        loadAllForum("#forumId", tid);
        // setFormRadioChecked($("#editForm input[name='viewSetting']"),1);
        setFormRadioChecked($("#editForm input[name='imageFlag']"),0);
        $(".save").show();
        $(".update").hide();
    });
    //搜索
    $("#searchBtn").click(function () {
        var titleQc = $.trim($("#titleQc").val());
        if(titleQc!="" && titleQc!=null){
            searchHtml();
        }else{
            topicObj.initPagerPlugin();
        }
    });
});

var editToolObj = new KindeditorTool({
    targetEl: "#editTool",
    uploadUrl: "/editUpload?filePart=bbs",
    // filterItems:["code","cut","copy","paste","plainpaste","flash","media","anchor","link","unlink","about"],
    items:[
        'undo', 'redo', '|', 'preview', 'print', 'template',
        'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
        'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
        'superscript', 'clearhtml', 'quickformat', 'selectall', '|','/',
        'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
        'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image', 'multiimage',
        'media', 'insertfile', 'table', 'hr', 'emoticons', 'baidumap', 'pagebreak', 'link', 'unlink'
    ]
});

//判断{}是否为空
function isEmptyObject(obj) {
    for (var key in obj){
        return false;//返回false，不为空对象
    }
    return true;//返回true，为空对象
}


/**
 * ztree展示部门树组织
 * t 1代表部分可见，2全部可见
 */
function showDeptUser(t) {
    var setting = {
        view: {
            selectedMulti: false
        },
        check: {
            enable: true,   //true表示显示,false表示不显示
            autoCheckTrigger: false, //事件回调函数:true表示 触发,false表示不触发
            chkStyle: "checkbox",   //勾选框类型(checkbox 或 radio）
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback : {
            beforeCheck:true,
            onCheck:onCheck, //复选框点击事件
        },
    };
    $("#all").parent().removeClass("checked");
    $("#all").removeAttr("checked");
    if(t==1){//t 1代表部分可见，2全部可见
       $("#showTitle").show();
       $("#showCommTips").hide();
    }else {
        $("#showTitle").hide();
        $("#showCommTips").show();
    }
    $("#lookSetting").val(t);
    if(isEmptyObject(configObj.deptTreeData)){
        configObj.loadDeptTreeData();
    }
    var data = configObj.deptTreeData;
    // $.get("/topic/queryDeptTree",{flag:Number(flag),companyCode:checkCompanyCode},function (data) {
    $(function(){
        $.fn.zTree.init($("#treeDemo"),setting,data);
    });
    $("#showDeptUser").empty();
    $("#queryResourceModal").modal({backdrop: "static"});
    // },"json")
}

//复选框点击事件
function onCheck(e,treeId,treeNode) {
    //1代表部分可见，2全部可见
    var lookFlag = $("#lookSetting").val();
    var treeObj = $.fn.zTree.getZTreeObj("treeDemo"),//获取treeDemo对象
        nodes = treeObj.getCheckedNodes(true),//获取其中选中的所有节点
        v = "";
    for (var i = 0; i < nodes.length; i++) {
        //查询是子节点并且是人员的数据
        if(!nodes[i].isParent && nodes[i].icon=='/img/user.png'){
            v += nodes[i].id + ":"+ nodes[i].name+",";
        }
    }
    var data = "";
    var html ="";
    $("#showDeptUser").empty();
    if(v!=null && v!="") {
        var str = v.split(",");
        if(str!=null){
            for (var i = 0; i < str.length; i++) {
                data = str[i].split(":");
                // 1代表部分可见，2全部可见
                if(lookFlag==1){
                    if (data[1] != null && data[1] != undefined) {
                        html += "<div class='col-md-1' style='border: #eeece4 1px solid;padding:10px 0px;margin: 5px;text-align: center;white-space: nowrap;text-overflow: ellipsis;  overflow: hidden;'>" +
                            "<input type='checkbox' class='i-checks' name='userIds' value='" + data[0] + "'/>" +
                            "<span name='userNames' style='font-size: 14px;' title='"+data[1]+"'>" + data[1] + "</span>" +
                            "</div>";
                    }
                }else{
                    if (data[1] != null && data[1] != undefined) {
                        html += "<div class='col-md-1' style='border: #eeece4 1px solid;padding:10px 0px;margin: 5px;text-align: center;white-space: nowrap;text-overflow: ellipsis;  overflow: hidden;'>" +
                            "<input type='hidden' name='commIds' value='" + data[0] + "'/>" +
                            "<span name='commNames' style='font-size: 14px;' title='"+data[1]+"'>" + data[1] + "</span>" +
                            "</div>";
                    }
                }
            }
        }
    }
    //展示评论设置的数据
    $("#showDeptUser").append(html);
    $("#all").parent().removeClass("checked");
    $("#all").removeAttr("checked");
    // 重新渲染ichecks
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
    //范围要控制好，初始化需在正确的范围
    $("#all").on('ifChanged', function (data) {
        $("#showDeptUser").find(".i-checks").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
    });
}

// 设置单选框的选中状态
function setFormRadioChecked(obj, inputValue) {
    obj.each(function () {
        $(this).removeAttr("checked");
        // 移除ICheck样式；
        $(this).parent().removeClass("checked");
    });
    obj.each(function () {
        if ($(this).val() == inputValue) {
            $(this).prop("checked", true);
            // 移除ICheck样式；
            $(this).parent().addClass("checked");
        }
    });
}

//初始化发帖权限，主题图设置的单选按钮状态
function initRadioState() {
    //发帖权限
    $('#editForm input:radio[name="viewSetting"]').on('ifChanged', function (event) {
        if ($('#editForm input:radio[name="viewSetting"]:checked').val() == 0) {
            //清除可见人员，评论人员数据（部分可见）
            $("#editForm #comm").val("");
            //如果是是，则跳转到部分可见权限设置页面
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            //部分可见展示部门树架构
            showDeptUser(1);
        }else{
            //清除可见人员，评论人员数据（全部可见）
            $("#editForm #users").val("");
            $("#editForm #commentUsers").val("");
            //如果是否，则跳转到全部可见权限设置页面
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            //全部可见展示部门树架构
            showDeptUser(2);
        }
    });
    //主题图片
    $('#editForm input:radio[name="imageFlag"]').on('ifChanged', function (event) {
        // console.log($('#editForm input:radio[name="imageFlag"]:checked').val());
        if ($('#editForm input:radio[name="imageFlag"]:checked').val() == 1) {
            //若选择上传主题图片，展示上传图片框
            $("#imageDiv").show();
        }else{
            //清除主题图片数据
            $("#imageDiv").hide();
            $("#imageUrl").val("");
            $("#imageShowUrl").attr("href","");
            $("#imageName").html("");
            $("#imageTips").val("");
        }
    });
}

/**
 * 判断用户是否有发帖权限
 */
function showCompanyResource(){
    //判断是否有权限发帖
    if(hasCompanyCodeJT()){
        $("#addBtn").show();
    }else{
        $("#addBtn").hide();
    }
    var forumId = getQueryString("forumId");
    $("#topicTypeVal").val(forumId);
    // 设置论坛板块id查询
    $("#topicType").val(forumId);
    $("#userImage").attr("src",user.image);
}


/**
 * 所有,置顶,加精查询
 * @param id
 */
function searchTopic(id,t) {
    //为查询时赋值
    $("#searchState").val(id);
    if ($(t).hasClass("text-focus")) {
        return;
    }
    $("#queryTopicState>a").each(function (i,item) {
        $(item).removeClass("touchfocus");
        if(t==item){
            $(item).addClass("touchfocus");
        }
    });
    topicObj.initPagerPlugin();
}

/**
 * 判断富文本编辑器是否为空
 * @returns {boolean}
 */
function checkEmpty(){
    var content = editToolObj.getContent();
    if(content=="" || content==null || content==undefined){
        layer.msg("论坛内容不能为空");
        return false;
    }else{
        //判断内容里是否有图片
        var flag = imageRegex(content);
        var imageFlag = flag?1:0;
        $("#imageSign").val(imageFlag);
        return true;
    }
}

/**
 * 主题图选是，校验图片路径是否存在
 * @returns {boolean}
 */
function checkTopicImage(){
    var formData = $("#editForm").serializeJson();
    var imageUrl = formData.imageUrl;
    var imageFlag = formData.imageFlag;
    var viewFlag = formData.viewSetting;
    if(viewFlag!=null && viewFlag!="" && viewFlag!=undefined){
        //imageFlag==1：主题图片标志为是
        if(imageFlag==1){
            if (imageUrl==null || imageUrl==""){
                layer.msg("主题图不能为空");
                return false;
            }else{
                return true;
            }
        }else{
            return true;
        }
    }else{
        layer.msg("可见设置不能为空");
        return false;
    }
}

/**
 * 添加编辑提交方法
 * @param t
 * @param url
 * @param state
 */
function submitForm(t, url) {
    var content = editToolObj.getContent();
    $("#editTool").val(content);
    if ($("#editForm").valid() && checkEmpty() && checkTopicImage()) {
        layer.confirm("请确认帖子信息", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            var users = $("#users").val();
            //防止后台插入数据报null
            if(users==null || users==""){
                $("#users").val(user.id);
            }else if(users!=null && users!=""){
                if(users.indexOf(user.id)==-1){
                    $("#users").val(users+user.id);
                }
            }
            $.ajax({
                type: "post",
                url: url,
                data: JSON.stringify($("#editForm").serializeJson()),
                dataType: "json",
                async: true,
                cache: false,
                contentType: "application/json;charset=utf-8",
                processData: false,
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        //查询当天的发帖数
                        queryByToday();
                        topicObj.initPagerPlugin();
                        //去除可见人员，可评论人员数据残留
                        $("#editForm #users").val("");
                        $("#editForm #commentUsers").val("");
                        $("#editForm #comm").val("");
                        $("#editModal").modal("hide");
                    } else {
                        $("#editModal").modal("hide");
                    }
                }
            });
        }, function () {
            return;
        })
    } else {
        return;
    }
}

//多个页面使用的方法或者数据
var commonObj = {
    //后台请求方法
    requestData: function (data, url, requestType, dataType, async, callBackFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if (contentType) {
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    //分页插件使用
    pagerPlus: function (config, callback, type) {
        layui.use('laypage', function () {
            var laypage = layui.laypage;
            //执行一个laypage实例
            laypage.render({
                elem: config.elem //注意，这里的 test1 是 ID，不用加 # 号
                , count: config.count || 0, //数据总数，从服务端得到
                layout: ['count', 'prev', 'page', 'next', 'refresh', 'limit', 'skip'],
                hash: true,
                limits: config.limits || [20,50, 100],
                limit: config.limit || 20,
                jump: function (obj, first) {
                    config.param = config.param || {};
                    config.param.size = obj.limit;
                    config.param.page = obj.curr;
                    commonObj.requestData(config.param, config.url, "post", "json", true, function (data) {
                        if (callback) {
                            if (config.target) {
                                callback(data, config.target);
                            } else {
                                callback(data, type);
                            }
                        }
                    });
                }
            });
        });
    }
}

/**
 * 论坛搜索页面展示
 */
function searchHtml() {
    var forumId = getQueryString("forumId");
    var titleQc = $.trim($("#titleQc").val());
    var searchState =$("#searchState").val();
    var companyCode = getQueryString("companyCode");
    var forumName = $("#showbbsType").text();
    var moderator = getQueryString("moderator");
    $("#themelist").empty();
    $("#searchDiv").empty();
    $("#showTopicHtml").hide();
    $("#queryTopicState").hide();
    $("#showSearchHtml").show();
    //queryFlag:1搜索标志，0默认列表查询
    $.post("/topic/listPg",{companyCode:companyCode,forumId:forumId,moderator:moderator,titleQc:titleQc,searchState:searchState,queryFlag:1},function (data) {
        var html = "";
        if (data && data.list && data.list.length > 0) {
            $.each(data.list, function (i, topic) {
                var title = topic.title;
                title = title.replace(new RegExp(titleQc,"g"),"<span style='color: #f00000'>"+titleQc+"</span>");
                var content = topic.content;
                content = content.replace(new RegExp(titleQc,"g"),"<span style='color: #f00000'>"+titleQc+"</span>");
                var createTime = new Date(topic.createTime).format("yyyy-MM-dd");
                html += "<div class='showSearchContent'><h3 style='height: 40px;line-height: 40px;color: #222222'>" +
                    "<input type='hidden' name='topicId' value='" + topic.id + "'/>";
                if (topic.level == 1) {
                    html += "<span><img src='/img/bbs/jing.png' style='margin-right: 10px' alt='加精' title='精华'/></span>";
                }
                html += "<a style='font-size: 18px;color: #222222' onclick='goTopic(" + topic.id + ")' href='javascript:void(0)'>" + title + "</a>";
                if (topic.hot >= 75) {
                    html += "<span><img src='/img/bbs/huo.png' style='margin-left:10px' title='热度'/></span>";
                }
                html += "</h3>" +
                    "<div class='imageFlag'>"+content+"</div>" +
                    "<span style='color: #222222'>"+ forumName + "</span>"+
                    "<span style='float: right;color: #888888'>"+ createTime + "</span>"+
                    "</div>";
            })
            $("#searchDiv").append(html);
            //无数据页面展示
        }else{
            html+="<div style='display: flex;height: calc(100% - 60px);justify-content: center;align-items: center'>" +
                "<img src='/img/bbs/zanwushuju.png'/>"+
                "<p style='position: absolute;margin-top: 150px;'><b style='font-size: 14px;color: #222222;'>抱歉，没有与  “"+titleQc+"”  相关的帖子噢~</b></p>"+
                "<p style='position: absolute;margin-top: 200px;font-size: 12px;color: #222222;'><span style='font-size: 14px;color: #222222;'>相关建议</span>：看看输入的文字是否有误！</p>"+
                "</div>";
            $("#searchDiv").append(html);
        }
    },"json")
}

//论坛列表对象
var topicObj={
    getTotalUrl: "/topic/getTopicCount",//论坛帖子总数
    queryTopicListUrl:"/topic/listPg",//查询论坛帖子列表
    pagerPluginElem: "queryTablePager",
    limit:8,//
    callback: function (data) {
        var html = "";
        $("#themelist").empty();
        if (data && data.list && data.list.length > 0) {
            $.each(data.list, function (i, topic) {
                // var imgReg = /<img.*?(?:>|\/>)/gi;
                //是否有图片标志
                var imageSign = topic.imageSign;
                //计算发帖时间并转换成文字
                var tips = calcDateMinute(new Date(topic.createTime).getTime(), new Date().getTime());
                html += "<li>" +
                    "<img alt='image' class='theme-img' onerror=\"this.src='/img/mrtx_2.png'\" src='" + topic.picture + "'/>" +
                    "<div class='theme-r'>" +
                    "<input type='hidden' name='topicId' value='" + topic.id + "'/><h3>";
                if (topic.level == 1) {
                    html += "<span><img src='/img/bbs/jing.png' style='margin-right: 10px' alt='加精' title='精华'/></span>";
                }
                if (topic.sort == 0) {
                    html += "<a class='theme-title' style='color: #f00000' onclick='goTopic(" + topic.id + ")' href='javascript:void(0)'>" + topic.title + "</a>";
                } else {
                    html += "<a class='theme-title' onclick='goTopic(" + topic.id + ")' href='javascript:void(0)'>" + topic.title + "</a>";
                }
                if(imageSign==1){
                    html += "<span><img src='/img/bbs/tupian.png' style='margin-left:10px' alt='图片' title='图片'/></span>";
                }
                if(topic.hot>=75){
                    html += "<span><img src='/img/bbs/huo.png' style='margin-left:10px' title='热度'/></span>";
                }
                html += "</h3><p style='height: 25px;line-height: 25px;'>" +
                    "<span style='font-size: 14px;'>" + topic.userName + "</span>&nbsp;&nbsp;<b>·</b>&nbsp;&nbsp;<span style='font-size: 14px;'>" + tips + "</span>" +
                    "<span class='numb'><img src='/img/bbs/nozan.png' title='灌水数'/>&nbsp;" + topic.dislikeNum + "</span>" +
                    "<span class='numb'><img src='/img/bbs/zan.png' title='点赞数'/>&nbsp;" + topic.likeNum + "</span>" +
                    "<span class='numb'><img src='/img/bbs/yuedu.png' title='阅读数'/>&nbsp;" + topic.viewNum + "</span>" +
                    "<span class='numb'><img src='/img/bbs/huifu.png' title='评论数'/>&nbsp;" + topic.commNum + "</span>" +
                    "</p>" +
                    "</div>" +
                    "</li>";
            })
            $("#themelist").append(html);
        }else{
            html+="<div class='noDataStyle'>" +
                "<img src='/img/bbs/zanwushuju.png'/>"+
                "<p style='position: absolute;margin-top: 130px;'><b style='font-size: 14px;color: #222222;'>暂无数据</b></p>"+
                "</div>";
            $("#themelist").append(html);
        }
    },
    initPagerPlugin: function () {
        var forumId = getQueryString("forumId");
        var searchState =$("#searchState").val();
        var companyCode = getQueryString("companyCode");
        var moderator = getQueryString("moderator")||$("#moderator").val();
        $("#showTopicHtml").show();
        $("#queryTopicState").show();
        $("#showSearchHtml").hide();
        //初始化分页组件
        commonObj.requestData({companyCode:companyCode,forumId:forumId,moderator:moderator}, topicObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    param: {companyCode:companyCode,forumId:forumId,moderator:moderator,titleQc:null,searchState:searchState,queryFlag:0},
                    elem: topicObj.pagerPluginElem,
                    count: data.data.total,
                    url: topicObj.queryTopicListUrl,
                    limit: topicObj.limit,
                },topicObj.callback);
            }
        });
    }
}

/**
 * js判断图片是否存在
 * @param content
 * @returns {boolean}
 */
function imageRegex(content) {
    var imgRegex = /<img.*?(?:>|\/>)/gi;
    var flag = false;
    if(content!=null && content!=""){
        if(content.indexOf("data:image/jpeg;base64,")!=-1 || content.indexOf("data:image/png;base64,")!=-1 || content.indexOf("data:image/gif;base64,")!=-1 || imgRegex.test(content)){
            flag=true;
        }
    }else{
        flag=false
    }
    return flag;
}

/**
 * 跳转到帖子详情页
 */
function goTopic(id) {
    var companyCode = getQueryString("companyCode");
    layer.msg("正在处理中，请稍候。", {time: 2000, shade: [0.7, '#393D49']});
    location.href="/bbs/showTopic?topicId="+id+"&companyCode="+companyCode;
}

/**
 * 查询分公司下所有的论坛板块
 * @param t
 * @param id
 */
function loadAllForum(t,id){
    var companyCode = getQueryString("companyCode");
    $.get("/forum/findForumByCode",{companyCode:companyCode},function (data) {
        if (data == null || data == '' || data.data.list.length==0) {
            $("#showTopicNum").html(0);
            $("#showTotalTopicNum").html(0);
            $("#showTodayNum").html(0);
            $("#showYourTopicNum").html(0);
            $("#addBtn").hide();
            swal("没有论坛板块可操作！", "没有查询到论坛板块信息，请联系管理员赋权！", "warning");
            return;
        }
        getResCode(data);
        var html = "";
        html = "<option value=''>请选择</option>";
        var list = data.data.list;
        $(list).each(function (i, item) {
            var selected = item.id==id?"selected=selected":"";
            html += "<option value='"+item.id+"'"+selected+">"+item.name+"</option>";
        });
        $(t).append(html);
    }, "json");
}

/**
 * 帖子编辑
 * @param id
 */
function edit(id) {
    $("#editForm").find("input").removeClass('error');
    $("#editForm").find("select").removeClass("error");
    $("#editForm").validate().resetForm();
    document.getElementById("editForm").reset();
    $(".save").hide();
    $(".update").show();
    $("#showFile").attr("href","");
    $("#showName").html("");
    $.ajax({
        type:"post",
        url:"/topic/findById",
        data:{id:id},
        // async:false,
        dataType:"json",
        success:function (data) {
            for (var attr in data.data.entity){
                //范围太大会让具有相同name的input赋值
                $("#editForm input[name='"+attr+"']").val(data.data.entity[attr]);
                if(attr=="content"){
                    editToolObj.setContent(data.data.entity[attr]);
                }
                if(attr=="forumId"){
                    $("#forumId").empty();
                    loadAllForum("#forumId",data.data.entity[attr]);
                }
                if(attr=="affixName"){
                    $("#affixName").html(data.data.entity[attr]);
                    $("#showName").html(data.data.entity[attr]);
                }
                if(attr=="affixLink"){
                    $("#affixLink").attr("href", data.data.entity[attr]);
                    $("#showFile").attr("href",data.data.entity[attr]);
                }
                if(attr=="forumName"){
                    $("#showbbsType").html(data.data.entity[attr]);
                }
                if(attr=="moderator"){
                    $("#moderator").html(data.data.entity[attr]);
                }
            }
        }
    })
    $("#editModal").modal("toggle");
}

/**
 * 编辑时为可见状态按钮赋值
 * @param id
 */
function dealState(id) {
    $.get("/topic/queryTopicRelation",{topicId:id},function (data) {
        // flag=0部分可见，flag=1全部可见
        if(data.data.flag==0){
            //部分可见
            setFormRadioChecked($("#editForm input[name='viewSetting']"),0);
        }else if(data.data.flag==1){
            //全部可见
            setFormRadioChecked($("#editForm input[name='viewSetting']"),1);
        }
        if(data.data.imageState=="yes"){
            setFormRadioChecked($("#editForm input[name='imageFlag']"),1);
            $("#imageDiv").show();
            $("#imageUrl").val(data.data.topic.imageUrl);
            $("#imageShowUrl").attr("href",data.data.topic.imageUrl);
            $("#imageName").html(data.data.topic.imageName);
            $("#imageTips").val(data.data.topic.imageName);
        }else{
            setFormRadioChecked($("#editForm input[name='imageFlag']"),0);
            $("#imageUrl").val("");
            $("#imageShowUrl").attr("href","");
            $("#imageName").html("");
            $("#imageTips").val("");
        }
    },"json")
}

/**
 * 查询当天的发帖数
 */
function queryByToday(){
    var companyCode = getQueryString("companyCode");
    var forumId= $("#topicType").val();
    var moderator = getQueryString("moderator");
    $.post("/topic/queryTopicByToday",{companyCode:companyCode,forumId:forumId,moderator:moderator},function (data) {
        $("#showTopicNum").html(data.first);
        $("#showTotalTopicNum").html(data.second);
        $("#showTodayNum").html(data.one);
        $("#showYourTopicNum").html(data.two);
        $("#showPublicTopic").empty();
        var html ="";
        for(var i =0;i<data.list.length;i++){
            html+="<li class='li-cell-style'>";
            if(i+1<=3){
                html+="<span id='num"+(i+1)+"' class='bbs_number commonTodayStyle'>"+(i+1)+"</span><a class='hotTitleStyle' onclick='goTopic("+data.list[i].id+")' href='javascript:void(0)'>"+data.list[i].title+"</a>";
            }else{
                html+="<span id='num"+(i+1)+"' class='commonTodayStyle'>"+(i+1)+"</span><a class='hotTitleStyle' onclick='goTopic("+data.list[i].id+")' href='javascript:void(0)'>"+data.list[i].title+"</a>";
            }
            html+="</li>";
        }
        $("#showPublicTopic").append(html);
    },"json");
}

/**
 * 判断当前用户是否有显示发帖按钮的权限
 * @returns {boolean}
 */
function hasCompanyCodeJT() {
    var companyCode = getQueryString("companyCode");
    if(companyCode=='JT' && user.companyCode=='JT'){
        //发集团帖子
        return true;
    }else if(companyCode=='JT' && user.companyCode!='JT'){
        //不是集团的不能发集团帖子
        return false;
    }else if(companyCode!='JT' && user.companyCode=='JT'){
        //集团不能发分公司帖子
        return false;
    }else if(companyCode!='JT' && user.companyCode!='JT'){
        //发分公司帖子
        return true;
    }
}

// 计算两个日期之间的分钟数
function calcDateMinute(startDate,endDate){
    var diffValue = new Date(endDate).getTime() - new Date(startDate).getTime();
    var tips = "";
    var minute = 1000*60;
    var hour = minute * 60;
    var day = hour * 24;
    var week = day * 7;
    var month = day * 30;
    var year = day * 365;
    if(diffValue<0){
        return;
    }
    // 计算 差距时间除以 指定时间段的毫秒数
    var yearC = diffValue / year;
    var monthC = diffValue / month;
    var weekC = diffValue / week;
    var dayC = diffValue / day;
    var hourC = diffValue / hour;
    var minC = diffValue / minute;
    //2分钟以前
    if(yearC>=1){
        tips = "发布于"+parseInt(yearC)+"年前";
    }else if(monthC>=1){
        tips = "发布于"+parseInt(monthC)+"月前";
    }else if(weekC>=1){
        tips = "发布于"+parseInt(weekC)+"周前";
    }else if(dayC>=1){
        tips = "发布于"+parseInt(dayC)+"天前";
    }else if(hourC>=1){
        tips = "发布于"+parseInt(hourC)+"小时前";
    }else if(minC>=1){
        if(minC<=2){
            tips = "发布于刚刚";
        }else if(2<minC && minC<=5){
            tips = "发布于5分钟前";
        }else{
            tips = "发布于"+parseInt(minC)+"分钟前";
        }
    }else{
        tips = "发布于刚刚";
    }
    return tips;
}