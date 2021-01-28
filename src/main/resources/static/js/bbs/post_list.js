$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';

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

    if (getQueryString("companyCode") != null && getQueryString("companyCode") != "" && getQueryString("companyCode") != undefined) {
        var companyCode = getQueryString("companyCode");
        $("#companyCode").val(companyCode);
        var forumId = getQueryString("forumId");
        if(forumId!=null){
            $("#topicType").val(forumId);
        }
        var forumName = getQueryString("forumName");
        var flag = getQueryString("flag");
        if(flag==1){
            $("#showbbsName").hide();
        }else{
            $("#showbbsName").show();
            $("#bbsPlateName").html(forumName);
        }
    }
    //版主板块显示
    $.ajaxSettings.async = false;
    $.post("/forum/getForumByBanzu",{companyCode:getQueryString("companyCode"),forumId:getQueryString("forumId")},function (data) {
        $("#showForumData").empty();
        var html="";
        if(data && data.length>0){
            $.each(data,function (i,forum) {
                html += "<dd style='display: block;font-size:14px;color: #999999;height: 40px;line-height: 40px;text-align: center;' data-id='0' data-name='"+forum.name+"' data-value='"+forum.moderator+"' onclick='queryTopicData("+forum.id+",this)'>" + forum.name + "</dd>";
            });
            $("#showForumData").append(html);
            $("#showForumData>dd:first-child").click();
        }else{
            $("#showForumList").hide();
            $("#banzuNameTip").hide();
            topicManage.init();
            var id = getQueryString("forumId");
            $("#topicType").val(id);
        }
    },"json");

    layui.use('element', function(){
        var element = layui.element;
        element.on('tab(docDemoTabBrief)', function(data){
            topicManage.tabSelect(data.index);//当选择对应tab栏目触发表格刷新
            $("#index").attr("dataIndex",data.index);
            // element.stopPropagation();
        });
    });

    //帖子搜索
    $("#searchBtn").click(function () {
        var titleQc = $.trim($("#titleQc").val());
        if(titleQc!=null && titleQc!=""){
            $("#titleSearch").val(titleQc);
        }else{
            //置空隐藏域
            $("#titleSearch").val("");
        }
        var index = $("#index").attr("dataIndex");
        if(index==0){
            publishObj.initModeratorPager();//版主查询
        }else{
            topicManage.tabSelect(index);//非版主查询
        }
    });

    // 版主置顶
    $("#makeTop").click(function () {
        var id = $("#goTopId").val();
        goTop(id);
    })

    // 版主加精
    $("#makeHight").click(function () {
        var id = $("#hightTopicId").val();
        setHighlight(id);
    })

    $('body').bind('keyup', function (event) {
        if (event.keyCode == "13") {
            // 回车执行查询；
            $("#searchBtn").click();
        }
    });
})

//帖子管理对象
var topicManage = {
    init:function () {
        topicManage.tabSelect(1);//默认tab页为我发表的帖子
    },
    tabSelect:function (index) {//右边tab页选择时，列表改变, index = tab下标
        $(".tabContent").css("display","none");
        $(".tabTitle").removeClass("layui-this");//移除tab选中
        $($(".tabTitle")[index]).addClass("layui-this");
        $("#index").attr("dataIndex",index);
        //置空隐藏域的标题
        var titleQc = $.trim($("#titleQc").val());
        if(titleQc!=null && titleQc!=""){
            $("#titleSearch").val(titleQc);
        }else{
            //置空隐藏域
            $("#titleSearch").val("");
        }
        if(index==0){//版主管理页面
            $("#banzuDiv").css("display","block");
            //是否版主（0是，1否）
            $("#banzuFlag").val(0);
        }
        if(index == 1){//我发表的帖子1
            $("#publishDiv").css("display","block");
            $("#queryTablePager").empty();
            //是否版主（0是，1否）
            $("#banzuFlag").val(1);
            publishObj.initPagerPlugin();
        }
        if(index == 2){//我回复的帖子2
            $("#replyDiv").css("display","block");
            $("#queryTablePager").empty();
            publishObj.initReplyPager();
        }
        if(index == 3){//我赞过的帖子3
            $("#postDiv").css("display","block");
            $("#queryTablePager").empty();
            publishObj.initPagerPlugin();
        }
    }
}

//点赞，发表帖子列表分页展示
var publishObj={
    getTotalUrl: "/topic/getPublishCount",
    queryTopicListUrl:"/topic/queryTopicManage",
    getReplyCount:"/topic/getReplyCount",
    queryListByRid:"/topic/queryReplyTopic",
    pagerPluginElem: "queryTablePager",
    limit:20,
    callback: function (data) {
        var index = $("#index").attr("dataIndex");
        var elem ;
        if(index==1){
            //我发表的
            elem=$("#publish_list");
            $("#postState").val("");
            $("#publish_list").empty();
        }else if(index==3){
            //我点赞的
            elem=$("#post_list");
            $("#postState").val(1);
            $("#post_list").empty();
        }
        var html = "";
        if(data && data.list.length>0) {
            $.each(data.list, function (i, topic) {
                html += "<tr style=\"height: 50px; line-height: 50px;border-bottom: #ebebeb 1px solid\">\n" +
                    "<td style=\"width: 30%;color:#222222;text-align:center;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;\">" +
                    "<a style=\"font-size: 12px;color:#222222;\" href=\"javascript:void(0)\" onclick='goTopic(" + topic.id + ")'>" + topic.title + "</a>\n" +
                    "</td>\n" +
                    "<td style=\"width: 10%;text-align: center;color:#222222;\">" + topic.viewNum + "</td>\n" +
                    "<td style=\"width: 10%;text-align: center;color:#222222;\">" + topic.likeNum + "</td>\n" +
                    "<td style=\"width: 10%;text-align: center;color:#222222;\">" + topic.dislikeNum + "</td>\n" +
                    "<td style=\"width: 20%;text-align: center;color:#222222;\">" + topic.createTime + "</td>\n" +
                    "<td style=\"width: 20%;text-align: center;color:#222222;\">";
                if (index == 1) {
                    html += "<a href='javascript:void(0)' style='margin-right: 10px' data-value='"+topic.id+"' data-id='"+topic.forumId+"' onclick='goToEdit(this)'><img style='width: 30px;height: 30px;' src='/img/bbs/bianji.png' alt='编辑' title='编辑'/></a>" ;
                    if (topic.sort == 9 && topic.topState == 0) {
                        //默认才允许置顶
                        html += "<a href='javascript:void(0)' style='margin-right: 10px' title='申请置顶' alt='申请置顶' onclick='applicationTop(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/zhiding.png'/></a>";
                    }
                    if (topic.level == 0 && topic.highState == 0) {
                        //普通帖时才允许加精
                        html += "<a href='javascript:void(0)' style='margin-right: 10px' title='申请加精' alt='申请加精' onclick='applicationHighlight(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/jiajing.png'/></a>";
                    }
                    html+="<a href='javascript:void(0)' style='margin-right: 10px' onclick='del(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/shanchu.png' alt='删除' title='删除'/></a>";
                } else {
                    html += "<span>" + topic.forumName + "</span>";
                }
                html += "</td></tr>";
            });
        }else{
            html+="<div class='noDataStyle'>" +
                "<img src='/img/bbs/zanwushuju.png'/>"+
                "<p style='position: absolute;margin-top: 150px;'><b style='font-size: 14px;color: #222222;'>暂无数据&nbsp;&nbsp;~</b></p>"+
                "</div>";
        }
        elem.append(html);
    },
    initPagerPlugin: function () {
        var index = $("#index").attr("dataIndex");
        if(index==1){
            //发表
            $("#postState").val("");
        }else if(index==3){
            //点赞
            $("#postState").val(1);
        }
        //我发表的帖子列表（普通成员）
        commonObj.requestData($("#termForm").serializeJson(), publishObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    param: $("#termForm").serializeJson(),
                    elem: publishObj.pagerPluginElem,
                    count: data.data.total,
                    url: publishObj.queryTopicListUrl,
                    limit: publishObj.limit,
                },publishObj.callback);
            }
        });
    },
    replyData:function(data){
        // 我回复的帖子列表
        var html = "";
        $("#reply_list").empty();
        var elem=$("#reply_list");
        if(data && data.list.length>0) {
            $.each(data.list, function (i, topic) {
                html += "<tr style=\"height: 50px; line-height: 50px;border-bottom: #ebebeb 1px solid\">\n" +
                    "<td style=\"width: 30%;color:#222222;text-align:center;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;\">" +
                    "<a style=\"font-size: 12px;color:#222222;\" href=\"javascript:void(0)\" onclick='goTopic(" + topic.id + ")'>" + topic.title + "</a>\n" +
                    "</td>\n" +
                    "<td style=\"width: 10%;text-align: center;color:#222222;\">" + topic.viewNum + "</td>\n" +
                    "<td style=\"width: 10%;text-align: center;color:#222222;\">" + topic.likeNum + "</td>\n" +
                    "<td style=\"width: 10%;text-align: center;color:#222222;\">" + topic.dislikeNum + "</td>\n" +
                    "<td style=\"width: 20%;text-align: center;color:#222222;\">" + topic.createTime + "</td>\n" +
                    "<td style=\"width: 20%;text-align: center;color:#222222;\">" + topic.forumName + "</td>" +
                    "</tr>";
            });
        }else{
            html+="<div class='noDataStyle'>" +
                "<img src='/img/bbs/zanwushuju.png'/>"+
                "<p style='position: absolute;margin-top: 150px;'><b style='font-size: 14px;color: #222222;'>暂无数据&nbsp;&nbsp;~</b></p>"+
                "</div>";
        }
        elem.append(html);
    },
    initReplyPager:function () {
        //初始化我回复的帖子分页
        commonObj.requestData($("#termForm").serializeJson(), publishObj.getReplyCount, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    param: $("#termForm").serializeJson(),
                    elem: publishObj.pagerPluginElem,
                    count: data.data.total,
                    url: publishObj.queryListByRid,
                    limit: publishObj.limit,
                },publishObj.replyData);
            }
        });
    },
    moderatorData:function (data) {
        //版主管理中心列表
        $("#banzu_list").empty();
        var html = "";
        if(data && data.list.length>0) {
            $.each(data.list, function (i, topic) {
                html += "<tr style=\"height: 50px; line-height: 50px;border-bottom: #ebebeb 1px solid\">\n" +
                    "<td style=\"width: 30%;color:#222222;text-align:center;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;\">";
                if (topic.level == 1) {
                    html += "<span><img src='/img/bbs/jing.png' style='margin-right: 10px' alt='加精' title='精华'/></span>";
                }
                if(topic.sort==0){
                    html += "<a style=\"font-size: 12px;color:#f00000;\" href=\"javascript:void(0)\" onclick='goTopic(" + topic.id + ")'>" + topic.title + "</a>\n";
                }else{
                    html += "<a style=\"font-size: 12px;color:#222222;\" href=\"javascript:void(0)\" onclick='goTopic(" + topic.id + ")'>" + topic.title + "</a>\n";
                }
                html+="</td>\n" +
                    "<td style=\"width: 10%;text-align: center;color:#222222;\">" + topic.viewNum + "</td>\n" +
                    "<td style=\"width: 10%;text-align: center;color:#222222;\">" + topic.likeNum + "</td>\n" +
                    "<td style=\"width: 10%;text-align: center;color:#222222;\">" + topic.dislikeNum + "</td>\n" +
                    "<td style=\"width: 20%;text-align: center;color:#222222;\">" + topic.createTime + "</td>\n" +
                    "<td style=\"width: 20%;text-align: center;color:#222222;\">";
                if (topic.sort == 9) {
                    if (topic.topState == 0) {
                        //默认才允许置顶
                        html += "<a href='javascript:void(0)' style='margin-right: 10px' title='置顶' alt='置顶' onclick='goTop(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/zhiding.png'/></a>";
                    } else if (topic.topState == 1) {
                        html += "<a href='javascript:void(0)' style='margin-right: 10px' title='待置顶' alt='待置顶' onclick='pendingRoof(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/zhiding.png'/></a>";
                    }
                } else {
                    html += "<a href='javascript:void(0)' style='margin-right: 10px' title='取消置顶' alt='取消置顶' onclick='notGoTop(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/yizhiding.png'/></a>";
                }
                if (topic.level == 0) {
                    if (topic.highState == 0) {
                        //普通帖时才允许加精
                        html += "<a href='javascript:void(0)' style='margin-right: 10px' title='加精' alt='加精' onclick='setHighlight(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/jiajing.png'/></a>";
                    } else if (topic.highState == 1) {
                        html += "<a href='javascript:void(0)' style='margin-right: 10px' title='待加精' alt='待加精' onclick='stickyingPost(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/jiajing.png'/></a>";
                    }
                } else {
                    html += "<a href='javascript:void(0)' style='margin-right: 10px' title='取消加精' alt='取消加精' onclick='notHighlight(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/yijiajing.png'/></a>";
                }
                html += "<a href='javascript:void(0)' style='margin-right: 10px' onclick='del(" + topic.id + ")'><img style='width: 30px;height: 30px;' src='/img/bbs/shanchu.png' alt='删除' title='删除'/></a>";
                html += "</td></tr>";
            });
        }else{
            html+="<div class='noDataStyle'>" +
                "<img src='/img/bbs/zanwushuju.png'/>"+
                "<p style='position: absolute;margin-top: 150px;'><b style='font-size: 14px;color: #222222;'>暂无数据&nbsp;&nbsp;~</b></p>"+
                "</div>";
        }
        $("#banzu_list").append(html);
    },
    initModeratorPager: function () {
        $("#queryTablePager").empty();
        //初始化版主管理中心分页
        commonObj.requestData($("#termForm").serializeJson(), publishObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    param: $("#termForm").serializeJson(),
                    elem: publishObj.pagerPluginElem,
                    count: data.data.total,
                    url: publishObj.queryTopicListUrl,
                    limit: publishObj.limit,
                },publishObj.moderatorData);
            }
        });
    }
}

//多个页面使用的方法或者数据
var commonObj = {
    //缓存当前所有用户
    allUser: [],
    allXZUser: [],
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

//跳转论坛列表页面
function toTopiclist() {
    var forumName;
    var moderator;
    var companyCode = $("#companyCode").val();
    var forumId = getQueryString("forumId");
    $.get("/forum/queryForumById",{id:forumId},function (data) {
        forumName = data.data.entity["name"];
        moderator = data.data.entity["moderator"];
    },"json");
    location.href="/bbs/queryTopic?flag=4&companyCode="+companyCode+"&forumId="+forumId+"&moderator="+moderator+"&forumName="+forumName;
}

//查询版主版块信息
function queryTopicData(id,t) {
    var forumId = getQueryString("forumId");
    var moderator = $(t).attr("data-value");
    var forumName = $(t).attr("data-name");
    if(forumName!=null && forumName!=undefined){
        $("#bankuaiName").html(forumName);
    }
    if(moderator!=null && moderator!=undefined){
        $("#moderator").val(moderator);
    }
    $("#postState").val("");
    $("#banzuFlag").val(0);
    $("#topicType").val(id);
    $("#showBanzuName").html(user.name);
    //首次加载layui未赋值
    $("#index").attr("dataIndex",0);
    publishObj.initModeratorPager();
}

/**
 * 跳转帖子详情页面
 */
function goTopic(id) {
    var companyCode = $("#companyCode").val();
    layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
    location.href="/bbs/showTopic?topicId="+id+"&companyCode="+companyCode;
}

/**
 * 跳转帖子编辑页面
 * @param id
 */
function goToEdit(t){
    var id = $(t).attr("data-value");
    var forumId = $(t).attr("data-id");
    var companyCode = $("#companyCode").val();
    location.href="/bbs/queryTopic?flag=2&topicId="+id+"&companyCode="+companyCode+"&forumId="+forumId;
}

/**
 * 从管理中心返回到首页
 */
function changeUrl(){
    location.href="/bbs/firstTopic";
}

/**
 * 跳转到删帖页面
 * @param id
 */
function del(id){
    $("#deleteModal").modal("toggle");
    $("#topicId3").val(id);
    $("#delReason").val("");
}

/**
 * 判断删帖理由是否为空
 * @returns {boolean}
 */
function checkResult() {
    var delReason = $("#delReason").val();
    if($.trim(delReason)!=null && $.trim(delReason)!=""){
        return true;
    }
    return false;
}

/**
 * 删除帖子
 * @param id
 */
function delTopic(){
    var id = $("#topicId3").val();
    var delReason = $("#delReason").val();
    if(checkResult()){
        layer.confirm('确认删除？', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: baseUrl + "/topic/del",    //向后端请求数据的url
                data: {topicId: id,reason:delReason},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#deleteModal").modal("hide");
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        var index = $("#index").attr("dataIndex");
                        if(index==0){
                            publishObj.initModeratorPager();
                        }else if(index==1){
                            publishObj.initPagerPlugin();
                        }
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
 * 跳转申请置顶页面
 * @param id
 */
function applicationTop(id){
    $("#showTop").hide();
    $("#dealTopDiv").hide();
    $("#applyTop").show();
    $("#topReason").val("");
    $("#topModal").modal("toggle");
    $("#goTopId").val(id);
}

/**
 * 待置顶跳转到确认置顶页面（版主）
 * @param id
 */
function pendingRoof(id){
    $("#showTop").show();
    $("#dealTopDiv").show();
    $("#applyTop").hide();
    $("#topModal").modal("toggle");
    $("#goTopId").val(id);
    $.get("/topic/findById",{id:id},function (data) {
        $("#showTopReason").html(data.data.entity["topReason"]);
        $("#topReason").val("");
    },"json");
}

/**
 * 判断置顶理由是否为空
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
    var tips = "";
    if(state==1){
        tips = "确认申请置顶？";
    }else{
        tips = "是否拒绝置顶？";
    }
    // 判断置顶理由是否为空
    if(checkTopResult()){
        layer.confirm(tips, {
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
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        publishObj.initPagerPlugin();
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
function applicationHighlight(id){
    $("#showHight").hide();
    $("#dealHightDiv").hide();
    $("#applyHight").show();
    $("#hightReason").val("");
    $("#hightModal").modal("toggle");
    $("#hightTopicId").val(id);
}

/**
 * 待加精跳转到确认加精页面（版主）
 * @param id
 */
function stickyingPost(id){
    $("#showHight").show();
    $("#dealHightDiv").show();
    $("#applyHight").hide();
    $("#hightModal").modal("toggle");
    $("#hightTopicId").val(id);
    $.get("/topic/findById",{id:id},function (data) {
        $("#showHightReason").html(data.data.entity["highReason"]);
        $("#hightReason").val("");
    },"json");
}

/**
 * 判断加精理由是否为空
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
    var tips = "";
    if(state==1){
        tips = "确认申请加精？";
    }else{
        tips = "是否拒绝加精？";
    }
    if(checkHightResult()){
        layer.confirm(tips, {
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
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        publishObj.initPagerPlugin();
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
 * 版主置顶
 * @param id
 */
function goTop(id){
    layer.confirm('确认置顶帖子？', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: "/topic/goTop",    //向后端请求数据的url
            data: {topicId: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $("#topModal").modal("hide");
                    publishObj.initModeratorPager();
                }
            }
        });
    }, function () {
        return;
    });
}

/**
 * 版主加精
 * @param id
 */
function setHighlight(id){
    layer.confirm('确认加精帖子？', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: "/topic/addLevel",    //向后端请求数据的url
            data: {topicId: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    $("#hightModal").modal("hide");
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    publishObj.initModeratorPager();
                }
            }
        });
    }, function () {
        return;
    });
}

/**
 * 取消置顶
 * @param id
 */
function notGoTop(id){
    layer.confirm('确认取消置顶？', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: "/topic/notGoTop",    //向后端请求数据的url
            data: {topicId: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    publishObj.initModeratorPager();
                }
            }
        });
    }, function () {
        return;
    });
}

/**
 * 取消加精
 * @param id
 */
function notHighlight(id){
    layer.confirm('确认取消加精？', {
        btn: ['确认', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: "/topic/notAddLevel",    //向后端请求数据的url
            data: {topicId: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    publishObj.initModeratorPager();
                }
            }
        });
    }, function () {
        return;
    });
}

/**
 * 长度限制
 */
function checkContent(t) {
    var len = $(t).val().length;
    if(len>=100){
        $("#showError").text("长度必须小于100字符");
        $("#showError").addClass("error");
        $(t).addClass("error");
    }else{
        $("#showError").text("");
        $("#showError").removeClass("error");
        $(t).removeClass("error");
    }
}

/**
 * 长度限制
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
