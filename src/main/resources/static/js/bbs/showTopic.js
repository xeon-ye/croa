//得到查询参数
function fnGetQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]);
    return null;
}

$(function () {
    //模态框垂直居中
    $('#topModal').on('shown.bs.modal', function (e) {
        // 关键代码，如没将modal设置为 block，则$modala_dialog.height() 为零
        $(this).css('display', 'block');
        var modalHeight=$(window).height() / 2 - $('#topModal .modal-dialog').height() / 2;
        $(this).find('.modal-dialog').css({
            'margin-top': modalHeight
        });
    });
    $('#hightModal').on('shown.bs.modal', function (e) {
        // 关键代码，如没将modal设置为 block，则$modala_dialog.height() 为零
        $(this).css('display', 'block');
        var modalHeight=$(window).height() / 2 - $('#hightModal .modal-dialog').height() / 2;
        $(this).find('.modal-dialog').css({
            'margin-top': modalHeight
        });
    });
    $('#deleteModal').on('shown.bs.modal', function (e) {
        // 关键代码，如没将modal设置为 block，则$modala_dialog.height() 为零
        $(this).css('display', 'block');
        var modalHeight=$(window).height() / 2 - $('#deleteModal .modal-dialog').height() / 2;
        $(this).find('.modal-dialog').css({
            'margin-top': modalHeight
        });
    });

    //flag=0查看帖子详情
    if (fnGetQueryString("topicId") != null && fnGetQueryString("topicId") != "" && fnGetQueryString("topicId") != undefined) {
        var companyCode = fnGetQueryString("companyCode");
        var topicId = fnGetQueryString("topicId");
        $("#topicId").val(topicId);
        $("#companyCode").val(companyCode);
        //论坛信息显示
        queryTopic(topicId);
    }
    //查询今天的发帖信息
    queryByToday();
    //初始化评论权限
    checkCommentResource();

    /**
     * 点击管理中心跳转到管理帖子页面
     */
    $("#manageCenter").click(function () {
        var companyCode = $("#companyCode").val();
        var forumId = $("#forumId").val();
        var forumName = $("#forumName").text();
        location.href="/bbs/post_list?flag=2&companyCode="+companyCode+"&forumId="+forumId+"&forumName="+forumName;
    });
    // 去除div复制粘贴带样式问题
    initCopyContent();

    //发表评论
    $("#commentBtn").click(function () {
        var topicId = $("#topicId").val();
        //js字符（<br/>,&nbsp;处理）
        var content = $("#show").html().replace(/(<br\s*\/?>)|(&nbsp;)/gi,"");
        if ($.trim(content) != null && $.trim(content) != "" && $.trim(content) != undefined) {
            layer.confirm('确定提交该评论？', {
                btn: ['确认', '取消'], //按钮
                shade: false //不显示遮罩
            }, function (index) {
                layer.close(index);
                $.ajax({
                    type: "post",
                    url: "/topic/addTopicComment",
                    data: {content: content, topicId: topicId},
                    dataType: "json",
                    async: true,
                    success: function (data) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#queryForm #show").html("");
                        //重新加载页面
                        topicObj.initPagerPlugin();
                    }
                });
            }, function () {
                return;
            });
        } else {
            layer.open({
                title:"提示",
                content:"评论不能为空"
            });
        }
    });

    //评论表情点击事件
    $("#emotion").click(function () {
        $("#show").emoji({
            button: "#emotion",
            showTab: false,
            animation: 'slide',
            position: 'topRight',
            icons: [{
                name: "QQ表情",
                path: "/js/plugins/kindeditor/plugins/emoticons/images/",
                maxNum: 134,
                file: ".gif"
            }]
        });
    });

    //悬浮表情点击事件
    $("#showFloatDiv").emoji({
        button: "#floatEmotion",
        showTab: false,
        animation: 'slide',
        position: 'topLeft',
        icons: [{
            name: "QQ表情",
            path: "/js/plugins/kindeditor/plugins/emoticons/images/",
            maxNum: 134,
            file: ".gif"
        }]
    });
});

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

//论坛帖子对象
var topicObj={
    getTotalUrl: "/topic/queryCommentCount",
    queryTopicListUrl:"/topic/queryCommentByTopicId",
    pagerPluginElem: "queryTablePager",
    limit:20,
    callback: function (data) {
        var html = "";
        $("#commentShow").empty();
        if (data && data.list && data.list.length > 0) {
            var userId = $("#userId").val();
            var html = "";
            if (data != null) {
                //当前页数
                var pageNum = data.pageNum;
                var pageSize=data.pageSize;
                $.each(data.list, function (i, comm) {
                    var content = comm.content;
                    var id = comm.id;
                    var tips = "";
                    //计算分页楼层数
                    var startIndex=pageSize*(pageNum-1)+i;
                    if(startIndex==0){
                        tips ="沙发";
                    }else if(startIndex==1){
                        tips ="板凳";
                    }else if(startIndex==2){
                        tips ="地板";
                    }else if(startIndex==3){
                        tips ="地下室";
                    }else{
                        tips=(startIndex+1)+"楼";
                    }
                    html += "<div class='contact-box' style='font-size: 14px;color: #999999;'>" +
                        "<div style='display: inline-block;margin-right: 16px;'>" +
                        "<img alt='image' style='width: 40px;height: 40px' class='img-circle' onerror=\"this.src='/img/mrtx_2.png'\" src='" + comm.picture + "'/>" +
                        "</div>" +
                        "<div style='display: inline-block'>" +
                        "<span style='color: #3d7eff'>" + comm.userName + "</span>";
                    if (comm.userId == userId) {
                        html += "<span style='color:palevioletred;'>(楼主)</span>";
                    }
                    html += "<span id='createTime' style='margin-left: 20px;'>" + comm.createTime + "</span></div>" +
                        "<span style='display: inline-block;float:right;color:#999999;'>"+tips+"</span>"+
                        "<div style='padding-left: 60px;width: 100%;overflow:hidden;clear: both'>" + content + "</div>" +
                        // "<div id='showLine_" + id + "'><hr style='margin: 30px 0px 0px 47px;height: 1px;background-color: #eeeeee'/></div>" +
                        "<div id='showReply_" + id + "' style='width: calc(100%-60px);'></div>" +
                        "<div id='page_" + id + "' style='margin-left:100px;padding-top: 10px;width:800px;text-align: left;'></div>" +
                        "<div style='margin-left: 60px' id='show_" + id + "'>" +
                        "<input type='hidden' id='Reply_" + id + "' name='commentReplyId'/>" +
                        "<input type='hidden' id='userName_" + id + "' name='userNameRecord'/>" +
                        "<div class='replyDiv' id='replyDisplay_" + id + "' style='padding-top: 10px;display: flex;'>" +
                        // "<p id='tips" + id + "'></p>" +
                        "<textarea class='hf-text' id='comment_" + id + "' onfocus='operateReply(" + id + ",null)'></textarea>" +
                        "<button id='replyBtn_" + id + "' type='button' onclick='submitReply(" + id + ")' class='reply-button-style'>回复</button>" +
                        // "<span class='hf-nub' id='num_" + id + "'>0/500</span>" +
                        "</div>" +
                        // "<span class=\"biaoqing\" style='margin-top: 5px' data-id='" + id + "' onclick='showReplyEmotion(this)'><img src=\"/img/bbs/biaoqing.png\"/></span>" +
                        "</div>" +
                        "<div class='replyDiv' style='float: right;'>" +
                        "<a href='javascript:void(0)' onclick='commentReply(" + id + ",\"" + comm.userName + "\")'><img src='/img/bbs/huifu.png' style='margin-right: 10px'/>回复</a>" +
                        "</div>" +
                        "<hr style='margin-top: 30px;height: 1px;background-color: #eeeeee'/>" +
                        "</div>";
                });
                for (var t = 0; t < data.list.length; t++) {
                    // console.log(data.list[t].replyFlag);
                    if(data.list[t].replyFlag>0){
                        //查询评论下的所有回复
                        showReply(data.list[t].id, 1);
                    }
                }
                $("#commentShow").append(html);
                //没有评论权限的隐藏回复框
                if(commentResource==false){
                    $(".commDiv").hide();
                    $(".float-div").hide();
                    $(".replyDiv").hide();
                }
            }
        } else {
            html+="<div class='noDataStyle'><img src='/img/bbs/zanwushuju.png'/>" +
                "<p style='position: absolute;margin-top: 120px;'><b style='font-size: 14px;color: #222222;'>暂无评论</b></p>"+
                "</div>";
            $("#commentShow").append(html);
        }
        //可评论权限限制(无回复时单独处理评论悬浮框和评论框)
        if(data.list.length==0){
            if(commentResource==false){
                $(".commDiv").hide();
                $(".float-div").hide();
                $(".replyDiv").hide();
            }
        }
    },
    initPagerPlugin: function () {
        var topicId = $("#topicId").val();
        var flag = $("#lookFlag").val();
        if(flag==1){
            //只看楼主
            var id = $("#userId").val();
        }else{
            //全看
            var id = null;
        }
        //初始化分页组件
        commonObj.requestData({topicId:topicId,userId:id}, topicObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                //给评论数赋值
                $("#commNum").html(data.data.total);
                commonObj.pagerPlus({
                    param: {topicId:topicId,userId:id},
                    elem: topicObj.pagerPluginElem,
                    count: data.data.total,
                    url: topicObj.queryTopicListUrl,
                    limit: topicObj.limit,
                },topicObj.callback);
            }
        });
    }
}

//悬浮评论单独处理
var emotionObj={
    publishComment: function () {
        //悬浮评论
        var content = $("#showFloatDiv").html().replace(/(<br\s*\/?>)|(&nbsp;)/gi, "");
        var topicId = $("#topicId").val();
        if ($.trim(content) != null && $.trim(content) != "" && $.trim(content) != undefined) {
            layer.confirm('确定提交该评论？', {
                btn: ['确认', '取消'], //按钮
                shade: false //不显示遮罩
            }, function (index) {
                layer.close(index);
                $.ajax({
                    type: "post",
                    url: "/topic/addTopicComment",
                    data: {content: content, topicId: topicId},
                    dataType: "json",
                    async: true,
                    success: function (data) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#queryForm #showFloatDiv").html("");
                        topicObj.initPagerPlugin();
                    }
                });
            }, function () {
                return;
            });
        } else {
            layer.open({
                title: "提示",
                content: "评论不能为空"
            });
        }
    }
}


//查询当天的发帖数
function queryByToday(){
    $("#userImage").attr("src",user.image);
    var companyCode = $("#companyCode").val();
    var forumId= Number($("#forumId").val());
    var moderator = $("#moderator").val();
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

//跳转帖子详情页
function goTopic(id) {
    var companyCode = $("#companyCode").val();
    layer.msg("正在处理中，请稍候。", {time: 2000, shade: [0.7, '#393D49']});
    location.href="/bbs/showTopic?topicId="+id+"&companyCode="+companyCode;
}

var commentResource=false;
/**
 * 检查是否有评论权限
 */
function checkCommentResource(){
    // 对可评论的进行权限控制
    var topicId = $("#topicId").val();
    var userId = $("#userId").val();
    $.ajax({
        type:"post",
        url:"/topic/queryRelationFlag",
        data:{topicId:topicId,userId:userId},
        dataType:"json",
        // async: false,
        success:function (data) {
            // 获取发帖人（可以进行评论）
            var id = $("#userId").val();
            var moderator = $("#moderator").val();
            if(id==user.id || moderator==user.id){
                commentResource =  true;
            }else{
                if(data.flag==1){
                    commentResource =  true;
                }else{
                    commentResource = false;
                    // $(".commDiv").hide();
                    // $(".float-div").hide();
                    // $(".replyDiv").hide();
                }
            }
        }
    });
}

/**
 * 论坛信息显示
 * @param id
 */
function queryTopic(id) {
    $.ajax({
        type: "post",
        url: "/topic/findById",
        data: {id: id},
        dataType: "json",
        async: false,
        success: function (data) {
            for (var attr in data.data.entity) {
                $("#queryForm [name='" + attr + "']").val(data.data.entity[attr]);
                var viewNum = data.data.entity["viewNum"];
                var likeNum = data.data.entity["likeNum"];
                var dislikeNum = data.data.entity["dislikeNum"];
                $("#queryForm #viewNum3").html(viewNum);
                $("#chooseInfo #likeNum3").html(likeNum);
                $("#chooseInfo #dislikeNum3").html(dislikeNum);
                var userId = data.data.entity["userId"];
                if (attr == "id") {
                    if(viewNum>0 && userId==user.id){
                        //已有记录不需要添加查看记录
                    }else{
                        //添加查看记录，为后续点赞做铺垫
                        addTopicRecord(data.data.entity["id"]);
                    }
                }
                if(attr=="moderator"){
                    $("#moderator").val(data.data.entity[attr]);
                }
                if (attr == "title") {
                    $("#queryForm #queryTitle").html(data.data.entity[attr]);
                    $("#showTopicName").html(data.data.entity[attr]);
                }
                if (attr == "userName") {
                    $("#queryForm #userName3").html(data.data.entity[attr]);
                }
                if (attr == "createTime") {
                    $("#queryForm #createTime3").html(data.data.entity[attr]);
                }
                if (attr == "content") {
                    $("#queryForm #queryContent").html(data.data.entity[attr]);
                }
                if (attr == "affixName") {
                    var affixName = data.data.entity[attr];
                    $("#queryForm #affixName3").html(affixName);
                    $("#fileDiv").show();
                }
                if (attr == "affixLink") {
                    $("#queryForm #affixLink3").attr("href", data.data.entity[attr]);
                }
                if (attr == "forumName") {
                    $("#forumName").html(data.data.entity[attr]);
                }
                if(attr== "userId"){
                    var topicId = data.data.entity["id"];
                    $("#operateDiv").empty();
                    var html="<a style='color:#999999' id='lookOne' onclick='lookOne()' href='javascript:void(0)'>[只看楼主]&nbsp;&nbsp;</a>"+
                        "<a style='color:#999999;display: none;' id='lookAll' onclick='lookAll()' href='javascript:void(0)'>[全看]&nbsp;&nbsp;</a>";
                    if(userId==user.id){
                        var companyCode = $("#companyCode").val();
                        var forumId = Number($("#forumId").val());
                        html+="<a style='color:#999999' onclick='editUrl("+topicId+")'>[我要编辑]&nbsp;&nbsp;</a>" +
                            "<a style='color:#999999' onclick='del("+topicId+")' href='javascript:void(0)'>[删帖]&nbsp;&nbsp;</a>";
                    }
                    $("#operateDiv").append(html);
                }
                if(attr=="sort"){
                    //申请加精必须是自己发的帖子
                    var sort = data.data.entity[attr];
                    var topState = data.data.entity["topState"];
                    //已置顶和待置顶不能再申请置顶
                    if(userId==user.id){
                        if(sort==9 && topState==0){
                            $("#applyToTop").show();
                        }
                    }
                }
                if(attr="level"){
                    //申请置顶必须是自己发的帖子
                    var level = data.data.entity[attr];
                    var highState = data.data.entity["highState"];
                    if(userId==user.id){
                        if(level==0 && highState==0){
                            $("#applyToHigh").show();
                        }
                    }
                }
            }
            $("#pointName").html(user.name);
            topicObj.initPagerPlugin();
        }
    });
}

function editUrl(id) {
    var forumId = Number($("#forumId").val());
    var companyCode = $("#companyCode").val();
    location.href="/bbs/queryTopic?flag=2&topicId="+id+"&companyCode="+companyCode+"&forumId="+forumId;
}

/**
 * 只看楼主
 */
function lookOne() {
    //只看楼主标志
    $("#lookFlag").val(1);
    $("#lookOne").hide();
    $("#lookAll").show();
    topicObj.initPagerPlugin();
}

/**
 * 全看
 */
function lookAll() {
    //查看所有标志
    $("#lookFlag").val(0);
    $("#lookOne").show();
    $("#lookAll").hide();
    topicObj.initPagerPlugin();
}

/**
 * 跳转删帖模态框
 * @param id
 */
function del(id){
    $("#deleteModal").modal("toggle");
    $("#topicId3").val(id);
}

/**
 * 删帖时进行非空验证
 * @returns {boolean}
 */
function checkResult() {
    var flag = false;
    var reason = $("#reason").val();
    if($.trim(reason)!=null && $.trim(reason)!=""){
        flag =  true;
    }
    return flag;
}

/**
 * 删除帖子
 * @param id
 */
function delTopic(){
    var id = $("#topicId3").val();
    var reason = $("#reason").val();
    var forumId = $("#forumId").val();
    if(checkResult()){
        layer.confirm('确认删除？', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: baseUrl + "/topic/del",    //向后端请求数据的url
                data: {topicId: id,reason:reason},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#deleteModal").modal("hide");
                        var companyCode = $("#companyCode").val();
                        location.href="/bbs/queryTopic?flag=7&forumId="+forumId+"&companyCode="+companyCode;
                    } else {
                        swal(data.msg);
                    }
                }
            });
        }, function () {
            return;
        });
    }else{
        layer.msg("删除理由不能为空");
    }
}

/**
 * 从详情页返回到列表页
 */
function changeUrl(){
    var forumId = Number($("#forumId").val());
    var companyCode = $("#companyCode").val();
    var forumName = $("#forumName").text();
    var moderator = $("#moderator").val();
    location.href="/bbs/queryTopic?flag=4&companyCode="+companyCode+"&forumId="+forumId+"&forumName="+forumName+"&moderator="+moderator;
}

/**
 * 点赞操作
 * @param state
 */
function vote(state){
    //判断是否点赞,灌水(自己不能给自己点赞，灌水)
    var userId = $("#userId").val();
    if(userId!=user.id){
        //检查点赞是否重复
        if(checkDuplicate(state)){
            //点赞操作
            submitHander(state);
        }
    }else{
        if(state==1){
            layer.open({
                title: '温馨提示'
                ,content: '亲，自己顶自己是不对的！'
            });
        }else{
            layer.open({
                title: '温馨提示'
                ,content: '亲，自己踩自己是不对的！'
            });
        }
    }
}

/**
 * 检查点赞是否重复
 */
function checkDuplicate(state){
    var flag = false;
    var id = $("#topicId").val();
    $.ajax({
        type:"post",
        url:"/topic/checkInfo",
        data:{topicId:id},
        dataType:"json",
        async:false,
        success:function (data) {
            if(state==1){
                if(data.data.flag==1){
                    layer.open({
                        title: '温馨提示'
                        ,content: '您已经赞过此主题了'
                    });
                    flag = false;
                }else{
                    flag = true;
                }
            }else if(state==2){
                if(data.data.flag==2){
                    layer.open({
                        title: '温馨提示'
                        ,content: '您已经踩过此主题了'
                    });
                    flag = false;
                }else{
                    flag = true;
                }
            }
        }
    });
    return flag;
}

/**
 * 点赞点击触发的方法
 * @param t
 * @param url
 */
function submitHander(state) {
    var topicId = $("#queryForm #topicId").val();
    $.ajax({
        type:"post",
        url:"/topic/updateTopicRecord",
        data:{state:state,topicId:topicId},
        dataType:"json",
        async:false,
        success:function (data) {
            //修改成功后调用点赞数量统计
            if (data.data.entity != null) {
                $("#queryForm #viewNum3").html(data.data.viewNum);
                $("#chooseInfo #likeNum3").html(data.data.likeNum);
                $("#chooseInfo #dislikeNum3").html(data.data.dislikeNum);
            }
        }
    });
}

/**
 * 添加帖子记录
 * @param topicId
 */
function addTopicRecord(topicId) {
    $.ajax({
        type:"post",
        url:"/topic/addTopicRecord",
        data:{topicId:topicId},
        dataType:"json",
        // async:false,
        success:function (data) {
            //添加成功后调用的函数
        }
    });
}

/**
 * 添加回复信息
 */
function submitReply(id) {
    //obj.replace(/(<br\s*\/?>)|(&nbsp;)/gi,"")
    var content = $("#comment_"+id).val();
    var replyId = $("#Reply_"+id).val();
    var replyFlag = $("#replyFlag").val();
    <!--回复标志，1：只回复自己，2：回复自己或他人-->
    if(replyFlag==1){
        replyId="";
    }
    if($.trim(content)!=null && $.trim(content)!=''){
        layer.confirm('确认回复该评论？', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: "/topic/saveReply",    //向后端请求数据的url
                data: {id:id,content:content,replyId:replyId},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        //提交回复清空回复框
                        $("#comment_"+id).val("");
                        //将回复id清除
                        $("#Reply_"+id).val("");
                        //显示评论id下的所有回复信息
                        showReply(id,1);
                    } else {
                        swal(data.msg);
                    }
                }
            });
        }, function () {
            return;
        });
    }else{
        layer.msg("回复不能为空");
    }
}

/**
 * 显示评论的所有回复信息
 * @param id
 */
function showReply(id,pageNum){
    var flag = $("#lookFlag").val();
    //查看标志lookFlag,1只看楼主，2查看所有
    if(flag==1){
        var userId = $("#userId").val();
    }else{
        var userId = null;
    }
    $.ajax({
        type: "post",
        url: "/topic/queryReply",//向后端请求数据的url
        data: {commentId:id,userId:userId,pageNum:pageNum},
        dataType: "json",
        // async: false,
        success: function (data) {
            var html="";
            var replyPage="";
            $("#showReply_"+id).empty();
            $("#page_"+id).empty();
            if(data.list.length>0){
                $.each(data.list,function (i,reply) {
                    if(reply!=null){
                        var text = reply.content;
                        var userName = reply.userName;
                        var id = reply.id;
                        //帖子楼主
                        var userId = $("#userId").val();
                        if(userName!=null && userName!=""){
                            html +="<div style='color: #999999;font-size: 14px;padding-bottom: 5px;'>" +
                                "<input type='hidden' id='rid' value='"+id+"'>"+
                                "<div style='display:inline-block;'>" +
                                "<div style='display: inline-block;margin-right: 16px'>" +
                                "<img alt='image' class='img-circle' style='width: 40px;height: 40px;' onerror=\"this.src='/img/mrtx_2.png'\" src='"+reply.picture+"'/>" +
                                "</div>"+
                                "<div style='display: inline-block'>"+
                                "<span style='color: #3d7eff;' id='replyUserName_"+id+"' value='"+reply.replyUserName+"'>"+reply.replyUserName+"</span>" ;
                            if(reply.replyUserId==userId){
                                html+="<span style='color:palevioletred;'>(楼主)</span>&nbsp;&nbsp;<span style='color: #3d7eff;'>回复</span>&nbsp;&nbsp;<span style='color: #3d7eff;'>"+userName+"</span>&nbsp;&nbsp;";
                            }else{
                                html+="&nbsp;&nbsp;<span style='color: #3d7eff;'>回复</span>&nbsp;&nbsp;<span style='color: #3d7eff;'>"+userName+"</span>&nbsp;&nbsp;";
                            }
                            html+= "<span id='createTime' style='margin-left:16px;'>" +reply.createTime +"</span></div></div>"+
                                "<div style='display: inline-block;margin-left: 16px' class='replyDiv'>" +
                                " <a href='javascript:void(0)' onclick='operateReply("+reply.commId+","+id+")'>回复</a>" +
                                "</div>"+
                                "<div style='margin-left: 60px;' class='content'>"+text+"</div>"+
                                "</div>";
                        }else{
                            html += "<div style='color: #999999;font-size: 14px;'>" +
                                "<input type='hidden' id='rid' value='"+id+"'>"+
                                "<div style='display:inline-block;'><div style='display: inline-block;margin-right: 16px'>" +
                                "<img alt='image' class='img-circle' style='width: 40px;height: 40px;' onerror=\"this.src='/img/mrtx_2.png'\" src='"+reply.picture+"'/>" +
                                "</div>"+
                                "<div style='display: inline-block'>"+
                                "<span style='color: #3d7eff;' id='replyUserName_"+id+"' value='"+reply.replyUserName+"'>"+reply.replyUserName+"</span>";
                            if(reply.replyUserId==userId){
                                html+="<span style='color:palevioletred;'>(楼主)</span>";
                            }
                            html+= "<span id='createTime' style='margin-left:16px;'>" +reply.createTime +"</span></div></div>"+
                                "<div style='display: inline-block;margin-left: 16px' class='replyDiv'>" +
                                " <a href='javascript:void(0)' onclick='operateReply("+reply.commId+","+id+")'>回复</a>" +
                                "</div>"+
                                "<div style='margin-left: 60px;' class='content'>"+text+"</div>"+
                                "</div>";
                        }
                    }
                });
                $("#showReply_"+id).append(html);
                //回复分页
                if(data.pages>1){
                    for(var j=1;j<=data.pages;j++){
                        if(pageNum==j){
                            replyPage += "<b>"+pageNum+"</b>&nbsp;&nbsp;&nbsp;&nbsp;";
                        }else{
                            replyPage += "<a href='javascript:void(0)' onclick='showReply("+id+","+j+")'>"+j+"</a>&nbsp;&nbsp;&nbsp;&nbsp;";
                        }
                    }
                    replyPage +="总共有  "+data.total+"  回复，共  "+data.pages+ " 页，当前为第  "+data.pageNum+"  页";
                    $("#page_"+id).append(replyPage);
                }
                $("#showReply_"+id).removeClass("reply-nodata-style");
                $("#showReply_"+id).addClass("reply-div-style");
                //控制评论回复按钮显示权限
                if(commentResource==false){
                    $(".commDiv").hide();
                    $(".-div").hide();
                    $(".replyDiv").hide();
                }
            }else{
                $("#showReply_"+id).removeClass("reply-div-style");
                $("#showReply_"+id).addClass("reply-nodata-style");
            }
        }
    });
}

/**
 * 回复框焦点事件（回复自己或别人）
 * @param id 评论id
 * @param replyId 回复id
 */
function operateReply(id,replyId){
    // initCopyContent();
    if (replyId != null) {
        var replyUserName = $("#replyUserName_" + replyId).text();
        if (replyUserName != null && replyUserName != "") {
            $("#comment_" + id).attr("placeholder","回复 " + replyUserName + " : ");
        }
        //2回复自己或别人
        $("#replyFlag").val(2);
        $("#Reply_" + id).val(replyId);
    }
}
//回复操作（回复自己）
function commentReply(id,userName){
    if (userName != null && userName != "") {
        $("#comment_" + id).attr("placeholder","回复 " + userName + " : ");
        $("#userName_" + id).val(userName);
        //1只能回复自己
        $("#replyFlag").val(1);
    }
}

/**
 * 评论长度限制
 */
function checkContent(t) {
    var len = $(t).val().length;
    if (len >= 500) {
        $("#showError").text("评论长度必须小于500字符");
        $("#showError").addClass("error");
        $(t).addClass("error");
    } else {
        $("#showError").text("");
        $("#showError").removeClass("error");
        $(t).removeClass("error");
    }
}


/**
 * 评论长度限制
 */
function checkContent2(t) {
    var len = $(t).val().length;
    if(len>=100){
        $("#showError2").text("长度必须小于100字符");
        $("#showError2").addClass("error");
        $(t).addClass("error");
    }else{
        $("#showError2").text("");
        $("#showError2").removeClass("error");
        $(t).removeClass("error");
    }
}

/**
 * 长度限制
 */
function checkContent3(t) {
    var len = $(t).val().length;
    if(len>=500){
        $("#showError3").text("长度必须小于500字符");
        $("#showError3").addClass("error");
        $(t).addClass("error");
    }else{
        $("#showError3").text("");
        $("#showError3").removeClass("error");
        $(t).removeClass("error");
    }
}

/**
 * 跳转申请置顶页面
 * @param id
 */
function applicationTop(){
    var id = $("#topicId").val();
    $("#applyTop").show();
    $("#topModal").modal("toggle");
    $("#goTopId").val(id);
}

/**
 * 检查置顶理由是否为空
 * @returns {boolean}
 */
function checkTopResult() {
    var topReason = $("#topReason").val();
    if($.trim(topReason)!=null && $.trim(topReason)!=""){
        return true;
    }
    return false;
}

/**
 * 申请置顶操作
 * @param id
 */
function topTopic(state){
    var id = $("#goTopId").val();
    var topReason = $("#topReason").val();
    if(checkTopResult()){
        layer.confirm('确认申请置顶？', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url:  "/topic/updateTop",    //向后端请求数据的url
                data: {topicId: id,topReason:topReason,state:state},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#topModal").modal("hide");
                        //申请置顶成功不能再申请
                        $("#applyToTop").hide();
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                    } else {
                        swal(data.msg);
                    }
                }
            });
        }, function () {
            return;
        });
    }else{
        layer.msg("置顶理由不能为空");
    }
}

/**
 * 跳转到申请加精页面
 * @param id
 */
function applicationHighlight(){
    var id = $("#topicId").val();
    $("#applyHight").show();
    $("#hightModal").modal("toggle");
    $("#hightTopicId").val(id);
}

/**
 * 核查加精理由不为空
 * @returns {boolean}
 */
function checkHightResult() {
    var hightReason = $("#hightReason").val();
    if($.trim(hightReason)!=null && $.trim(hightReason)!=""){
        return true;
    }
    return false;
}

/**
 * 申请加精操作
 * @param id
 */
function hightTopic(state){
    var id = $("#hightTopicId").val();
    var hightReason = $("#hightReason").val();
    if(checkHightResult()){
        layer.confirm('确认申请加精？', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url:  "/topic/updateLevel",    //向后端请求数据的url
                data: {topicId: id,highReason:hightReason,state:state},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#hightModal").modal("hide");
                        //申请加精成功不能再申请
                        $("#applyToHigh").hide();
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                    } else {
                        swal(data.msg);
                    }
                }
            });
        }, function () {
            return;
        });
    }else{
        layer.msg("加精理由不能为空");
    }
}

/**
 * 去除div复制粘贴带样式问题
 */
function initCopyContent(){
    $('[contenteditable]').each(function() {
        // 干掉IE http之类地址自动加链接
        try {
            document.execCommand("AutoUrlDetect", false, false);
        } catch (e) {}

        $(this).on('paste', function(e) {
            e.preventDefault();
            var text = null;
            // 剪贴板
            if(window.clipboardData && clipboardData.setData) {
                // IE从剪贴板获取指定格式的数据。
                text = window.clipboardData.getData('text');
            } else {
                text = (e.originalEvent || e).clipboardData.getData('text/plain') || prompt('在这里输入文本');
            }
            if (document.body.createTextRange) {
                // document.selection : IE
                // window.getSelection() ：Chrome
                if (document.selection) {
                    textRange = document.selection.createRange();
                } else if (window.getSelection) {
                    sel = window.getSelection();
                    var range = sel.getRangeAt(0);

                    // 创建临时元素，使得TextRange可以移动到正确的位置
                    var tempEl = document.createElement("span");
                    tempEl.innerHTML = "&#FEFF;";
                    range.deleteContents();
                    range.insertNode(tempEl);
                    textRange = document.body.createTextRange();
                    textRange.moveToElementText(tempEl);
                    tempEl.parentNode.removeChild(tempEl);
                }
                textRange.text = text;
                textRange.collapse(false);
                textRange.select();
            } else {
                // Chrome之类浏览器
                document.execCommand("insertText", false, text);
            }
            //清除clipboardData实例（防止实例+1）
            this.clipboardData.destroy();
        });
    });
}