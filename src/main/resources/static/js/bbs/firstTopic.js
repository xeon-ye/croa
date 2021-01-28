$(function () {
    //展示可见公司数据
    companyInit();
    //展示可见板块数据
    forumInit(null,null);

    // 跳转管理中心页面
    $("#manageCenter").click(function () {
        var companyCode = $("#companyCode").val();
        location.href="/bbs/post_list?flag=1&companyCode="+companyCode;
    });
});

//根据公司代码查询其下所有板块
function forumInit(companyCode,t) {
    if(t!=null){
        var companyName = $(t).attr("data-name");
    }
    $("#companyName").html(companyName);
    var code = companyCode==null?user.companyCode:companyCode;
    $("#showSecondText").empty();
    $("#companyCode").val(code);
    $.get("/forum/getForumData", {companyCode: code}, function (data) {
        if (data == null || data == "" || data.data.list.length==0) {
            swal("没有论坛板块可操作！", "没有查询到论坛板块信息，请联系管理员赋权！", "warning");
            $("#showContent").empty();
            var html="<div class='noDataStyle'>" +
                "<img src='/img/bbs/zanwushuju.png'/>"+
                "<p style='position: absolute;margin-top: 130px;'><b style='font-size: 14px;color: #222222;'>暂无数据</b></p>"+
                "</div>";
            $("#showContent").append(html);
            $("#showSecondText").append(html);
            return;
        }else{
            //获取联系人信息；
            seachData();
        }
        getResCode(data);
        var list = data.data.list;
        var html = "";
        if(list.length>0){
            for (var i = 0; i < list.length; i++) {
                html += "<div class='col-xs-6 col-sm-6 col-md-4 showForumDiv'>" +
                    "<div class='showForumDiv_box'>" +
                    "<img data-name='" + list[i].name + "' data-id='"+list[i].moderator+"' style='width: 100px;height: 100px;margin-right:16px;border-radius: 5px;' onclick='goToForum(" + list[i].id + ",this)' src='" + list[i].affixLink + "'/>" +
                    "<div style='flex: 1;overflow:hidden;white-space:nowrap;text-overflow:ellipsis;'>" +
                    "<h3><a href='javascript:void(0)' data-name='" + list[i].name + "' data-id='"+list[i].moderator+"' style='font-size: 16px;color: #222222' onclick='goToForum(" + list[i].id + ",this)' title='" + list[i].name + "'>" + list[i].name + "</a></h3>"+
                    "<p class='showbanzhuStyle'>版主："+list[i].moderatorName+"</p>"+
                    "<p style='line-height: 36px;' class='noCursor'>" +
                    "<span style='display: inline-block;width: 50%'><img src='/img/bbs/zhuti.png' title='主题数' style='margin-right: 5px'/><span>"+list[i].topicNum+"</span></span>" +
                    "<span><img src='/img/bbs/huifu.png' title='回复数' style='margin-right: 5px'/><span>"+list[i].replyNum+"</span></span>" +
                    "</p>" +
                    "</div>" +
                    "</div>" +
                    "</div>";
            }
        }
        $("#showSecondText").append(html);
    });
    //首页图片
    imageShow();
}

//论坛首页展示公司数据
function companyInit() {
    if (user.companyCode=="JT") {
        $.get("/dept/listAllCompany", null, function (data) {
            var list = data.data.result;
            var html = "<li><a href='javascript:void(0)' onclick='forumInit(\"JT\",this)' data-value='JT' data-name='集团'>集团</a></li>";
            for (var i = 0; i < list.length; i++) {
                var code = list[i].code;
                var name = list[i].name;
                html += "<li><a href='javascript:void(0)' onclick='forumInit(\""+code+"\",this)' data-value='"+code+"' data-name='"+name+"'>" + name + "</a></li>";
            }
            $("#showCompanyCode").append(html);
            $("#companyCode").val(user.companyCode);
            $("#companyName").html("集团");
        })
    } else {
        var html = "<li><a href='javascript:void(0)' onclick='forumInit(\"JT\",this)' data-value='JT' data-name='集团'>集团</a></li>";
        html += "<li><a href='javascript:void(0)' onclick='forumInit(\""+user.companyCode+"\",this)' data-value='"+user.companyCode+"' data-name='"+user.dept.companyCodeName+"'>" + user.dept.companyCodeName + "</a></li>";
        $("#showCompanyCode").append(html);
        $("#companyCode").val(user.companyCode);
        $("#companyName").html(user.dept.companyCodeName);
    }
}

/**
 * 分公司跳到列表页(显示板块)
 * @param id
 */
function goToForum(id,t) {
    var companyCode = $("#companyCode").val();
    var moderator = $(t).attr("data-id");
    var forumName = $(t).attr("data-name");
    location.href="/bbs/queryTopic?flag=4&companyCode="+companyCode+"&forumId="+id+"&moderator="+moderator+"&forumName="+forumName;
}

//初始化分页插件
function initPage() {
    jQuery.ias({
        history: false,
        container: ".content",// 文章列表上一级容器
        item: ".excerpt",// 文章列表的类
        pagination: ".pagination",// 分页导航父级容器
        next: ".next-page a",// 分页导航a标签
        trigger: "点击查看更多", // 加载更多的文字
        loader: "<div class='pagination-loading col-sm-12'><img src='/img/ias/loading.gif'/></div>",
        triggerPageThreshold: 2,
        onRenderComplete: function () {
            $(".excerpt .thumb").lazyload({
                placeholder: "/img/ias/occupying.png",
                threshold: 400
            });
            $(".excerpt img").attr("draggable", "false");
            $(".excerpt a").attr("draggable", "false");
            updateHref();
        }
    });
}

// 获取联系人信息；
function seachData() {
    // 初始化页面；
    $("#pageNum").val(1);
    // 载入联系人信息；
    $.post("/topic/queryFirstInfo", {pageNum:$("#pageNum").val()}, function (data) {
        $("#showContent").html(data);
        updateHref();
    }, "text");
    initPage();
}

// 更新链接信息；
function updateHref() {
    //动画效果
    $('.contact-box').each(function () {
        animationHover(this, 'pulse');
    });
    var page = parseInt($("#pageNum").val());
    $("#pageNum").val(page + 1);
    var pageNum = Number($("#pageNum").val());
    $(".next-page a").attr("href", "/topic/queryFirstInfo?pageNum=" + pageNum);
}

//主题图片显示
function imageShow(){
    layui.use('carousel', function(){
        var carousel = layui.carousel;
        //建造实例
        var ins=carousel.render({
            elem: '#wheel'
            ,width: '100%' //设置容器宽度
            ,height:'360px'
            ,arrow: 'hover' //始终显示箭头
            //,anim: 'updown' //切换动画方式
        });
        // $.ajaxSettings.async=false;
        var companyCode = $("#companyCode").val();
        $.get("/topic/getFirstImage",{companyCode:companyCode},function (data) {
            var imageData = data.data.topic;
            var html="";
            $("#images").empty();
            if(data && imageData.list.length>0){
                $(imageData.list).each(function (i,item) {
                    html += "<div><img alt='主题图片' src='"+item.imageUrl+"' data-value='"+item.companyCode+"' style='cursor: pointer' onclick='goToTopic("+item.id+",this)'/></div>";
                });
                $("#images").html(html);
            }else{
                html ="<div><img src='/img/bbs/pugongying.png' style='cursor: default'></div>";
                $("#images").html(html);
            }
            ins.reload({elem: "#wheel"});
        },"json");
    });
}

//跳转到论坛详情页
function goToTopic(id,t){
    var companyCode=$(t).attr("data-value");
    location.href="/bbs/showTopic?topicId="+id+"&companyCode="+companyCode;
}