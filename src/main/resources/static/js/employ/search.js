$(function () {
    // 表单增加校验；
    $("#searchForm").validate();
    $("#recoverForm").validate();

    // 回车执行查询；
    $("#validateCode").keydown(function (evt) {
        evt = (evt) ? evt : ((window.event) ? window.event : "");
        var curKey = evt.keyCode ? evt.keyCode : evt.which;
        if (curKey == 13) {
            searchData();
        }
    });
});

// 跳转到档案查询页面；
function searchData() {
    if ($("#searchForm").valid()) {
        window.location.href = "/record?code=" + $("#validateCode").val();
    }
}

// 刷新验证码；
function refreshCode() {
    $("#imageCode").attr("src", "/code/image?random=" + new Date().getTime());
    // 清空已有查询码并获取焦点；
    $("#code").val("");
    $("#code").focus();
}

// 找回查询码；
function recoverData() {
    if ($("#recoverForm").valid()) {
        alertMessage("系统处理中，请稍候。");
        $.post(baseUrl + "/entry/getEntryValidate", $("#recoverForm").serializeJson(), function (data) {
            if (data.data.entryValidate == null) {
                // 失败了需要刷新验证码；
                refreshCode();
                alertMessage(data.data.message);
            } else {
                window.location.href = "/record?code=" + data.data.entryValidate;
            }
        }, "json");
    }
}