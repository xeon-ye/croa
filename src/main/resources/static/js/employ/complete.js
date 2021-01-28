$(function () {
    // 获取查询参数；
    var id = getQueryString("id");
    var code = getQueryString("code");
    if (id && id.length > 0) {
        $("#id").val(id);
        $("#backHref").removeAttrs("onclick");
        $("#backHref").attr("href", "/record?id=" + id);
    } else if (code && code.length > 0) {
        $("#code").val(code);
        $("#backHref").removeAttrs("onclick");
        $("#backHref").attr("href", "/record?code=" + code);
    } else {
        alertMessage("无权限访问，即将前往登录页面。");
        setTimeout(function () {
            window.location.href = "/login";
        }, 1000);
    }

    // 增加表单校验；
    $("#completeForm").validate();
});

// 打开文件上传窗口；
function openFileUpload(obj) {
    // 清空文件；
    $("#uploadFile").val("");
    $("#uploadModal").modal({backdrop: "static"});

    // 先清空原有的属性；
    $("#completeForm").find("input[type='text']").each(function () {
        $(this).removeAttrs("upload");
    });

    // 增加属性；
    $(obj).attr("upload", true);
}

// 开始上传文件；
function beginFileUpload() {
    checkValid(function () {
        if ($("#uploadFiles").val() == "") {
            alertMessage("请选择需要上传的文件。");
        } else {
            alertMessage("处理中。");
            // 校验表单；
            var content = $("#uploadFile").val();
            var reg = /^.+(.JPEG|.jpeg|.JPG|.jpg|.GIF|.gif|.BMP|.bmp|.PNG|.png|.RAR|.rar|.DOC|.doc|.DOCX|.docx|.XLS|.xls|.XLSX|.xlsx|.ZIP|.zip)$/;
            if (content.length > 0 && reg.test(content)) {
                $("#uploadModal").modal("hide");
                var options = {
                    type: "POST",
                    dataType: "json",
                    url: baseUrl + "/entry/upload",
                    success: function (data) {
                        // 清空表单；
                        $("#uploadFile").val("");
                        var fileName = data.data.image;
                        if (fileName == null) {
                            getResCode(data);
                        } else {
                            var inputElement = $("input[type='text'][upload='true']");
                            inputElement.prev().val(fileName);
                            var inputNext = inputElement.next("a");
                            if (inputNext && inputNext.length > 0) {
                                inputNext.attr("href", fileName);
                            } else {
                                inputElement.parent().append("<a href='" + fileName + "' target='_blank'>&nbsp;点击查看</a>");
                            }
                            alertMessage("处理完成。");
                        }
                    },
                    error: function () {
                        getResCode(data);
                    }
                };
                $("#uploadForm").ajaxSubmit(options);
            } else {
                alertMessage("请选择图片、Office办公文件或压缩文件。");
            }
        }
    });
}

// 提交数据；
function saveData() {
    checkValid(function () {
        var jsonData = $("#completeForm").serializeJson();
        var jsonDataStr = JSON.stringify(jsonData);
        if (jsonDataStr.length > 2) {
            alertMessage("系统处理中，请稍候。");
            var id = $("#id").val();
            var url = "";
            if (id && id.length > 0) {
                jsonData["entryId"] = id;
                url = baseUrl + "/entryManage/completeEntry";
            } else {
                jsonData["entryValidate"] = $("#code").val();
                url = baseUrl + "/entry/completeEntry";
            }
            $.post(url, {entry: JSON.stringify(jsonData)}, function (data) {
                var message = data.data.message;
                if (message == null) {
                    getResCode(data);
                } else {
                    alertMessage(message);
                    setTimeout(function () {
                        window.location.href = $("#backHref").attr("href");
                    }, 1000);
                }
            }, "json");
        } else {
            alertMessage("请至少上传一份文件。");
        }
    });
}

// 检查权限；
function checkValid(callBackFunction) {
    // 检查是否有数据；
    if ($("#id").val().length > 0 || $("#code").val().length > 0) {
        callBackFunction();
    } else {
        alertMessage("无权限访问，即将前往登录页面。");
        setTimeout(function () {
            window.location.href = "/login";
        }, 1000);
    }
}