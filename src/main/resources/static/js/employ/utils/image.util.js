$(function () {
    // 样式；
    $("#uploadImage").css({
        "border": "1px solid #B6BCBF",
        "border-radius": "4px 4px 4px 4px",
        "box-shadow": "0 0 5px rgba(0, 0, 0, 0.3)"
    });
});

// 载入图片的处理；
function loadPortrait() {
    var image = $("#loadImage").val();
    if (image.length > 0) {
        loadImage(image, function () {
            $("#uploadImage").attr("src", image);
        }, function () {
            $("#uploadImage").attr("src", "/img/mrtx_1.png");
        });
    } else {
        $("#uploadImage").attr("src", "/img/mrtx_1.png");
    }
}

// 图片加载事件；
function loadImage(url, callback, errorback) {
    // 创建一个Image对象，实现图片的预下载；
    var image = new Image();
    image.src = url;

    // 如果图片已经存在于浏览器缓存，直接调用回调函数；
    if (image.complete) {
        callback.call(image);
        // 直接返回，不用再处理onload事件；
        return;
    }
    // 图片下载完毕时异步调用callback函数；
    image.onload = function () {
        // 将回调函数的this替换为Image对象；
        callback.call(image);
    };
    // 图片下载异常时异步调用callback函数；
    image.onerror = function () {
        // 将回调函数的this替换为Image对象；
        errorback.call(image);
    };
}