$(function () {
    // 载入图片的处理；
    loadPortrait();
});

// 开始上传文件；
function uploadFile() {
    // 校验表单；
    var content = $("#uploadFile").val();
    var reg = /^.+(.JPEG|.jpeg|.JPG|.jpg|.GIF|.gif|.BMP|.bmp|.PNG|.png)$/;
    if (content.length > 0 && reg.test(content)) {
        $("#uploadModal").modal("hide");
        var options = {
            type: "POST",
            dataType: "json",
            url: getUploadURL(),
            success: function (data) {
                // 清空表单；
                $("#uploadFile").val("");
                handleFile(data.data.image);
            },
            error: function () {
                getResCode(data);
            }
        };
        $("#uploadForm").ajaxSubmit(options);
    } else {
        alertMessage("请选择图片文件。");
    }
}

// 文件上传完毕的回调；
function handleFile(data) {
    var field = getUploadField();
    var reg = /^.+(.JPEG|.jpeg|.JPG|.jpg|.GIF|.gif|.BMP|.bmp|.PNG|.png)$/;
    if (data == "fail") {
        $("#uploadModal").modal({backdrop: "static"});
        alertMessage("上传图片不能为空。");
    } else if (reg.test(data)) {
        loadImage(data, function () {
            $("input[name='" + field + "']").val(data);
            $("#uploadImage").attr("src", data);
            alertMessage("图片上传成功。");
        }, function () {
            $("input[name='" + field + "']").val("");
            $("#uploadImage").attr("src", "/img/mrtx_1.png");
            alertMessage("图片丢失。");
        });
    } else {
        getResCode(data);
    }
}