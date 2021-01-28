$(function () {
    // 载入联系人信息；
    seachData();

    // 浏览器最大化、最小化事件监听
    window.onresize = function () {
        // 调整头像图片尺寸；
        $(".contact-box").find("img").css("height", $("#box0").width());
    };

    // 回车执行查询；
    $("#dataName").keydown(function (evt) {
        evt = (evt) ? evt : ((window.event) ? window.event : "");
        var curKey = evt.keyCode ? evt.keyCode : evt.which;
        if (curKey == 13) {
            $("#dataSearch").click();
        }
    });
});

// 初始化；
function initPage() {
    jQuery.ias({
        history: false,
        container: ".content",
        item: ".excerpt",
        pagination: ".pagination",
        next: ".next-page a",
        trigger: "点击查看更多",
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

// 更新链接信息；
function updateHref() {
    $("#user_list").fadeIn(1000, function () {
        // 动态设置图片尺寸；
        $(".contact-box").find("img").css("height", $("#box0").width());
    });
    //动画效果
    $('.contact-box').each(function () {
        animationHover(this, 'pulse');
    });
    var page = parseInt($("#pageNum").val());
    $("#pageNum").val(page + 1);
    var queryData = $("#user_form").serialize();
    if (queryData.length > 0) {
        $(".next-page a").attr("href", "/user/listUserInfo?" + queryData);
    }
}

// 获取联系人信息；
function seachData() {
    // 初始化页面；
    $("#pageNum").val(1);
    // 载入联系人信息；
    $.post("/user/listUserInfo", $("#user_form").serialize(), function (data) {
        $("#user_list").html(data);
        updateHref();
    }, "text");

    initPage();
}