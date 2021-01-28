$(function () {
    // 初始化表格的数据；
    dataInit();
});

//获取当前时间
function getNowFormatDate() {
    var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
        + " " + date.getHours() + seperator2 + date.getMinutes()
        + seperator2 + date.getSeconds();

    $("#reportDate").val(currentdate);
}

// 初始化数据；
function dataInit() {
    var id = getQueryString("id");
    var type= getQueryString("type");
    var url = "";
    if(type==1){
        url = baseUrl + "/onBusiness/getOnBusinessByAdmId";
        getNowFormatDate();
        $(".save").show();
        $(".update").hide();
    }else{
        url = baseUrl + "/onbusinessReport/getReport";
        $(".save").hide();
        if(type == 2){
            $(".update").show();
        }else {
            $(".update").hide();
        }
    }
    if (id && id.length > 0) {
        var params = {};
        params = {id: id};
        $.post(url, params, function (data) {
            console.log(data);
            $("#editForm input[name='type']").removeAttr("checked");
            $("#editForm input[name='type']").parent().removeClass("checked");
            var dataValue = data.data.entity;
            if (dataValue == null) {
                $("#message").html("*（未获取到相关数据）");
                alertMessage("未获取到相关数据");
            } else {
                // 获取值；
                for (var key in dataValue) {
                    $("#editForm [name=" + key + "][type!='radio']").val(dataValue[key]);
                }
            }
        }, "json");
    } else {
        alertMessage("无权限访问，即将前往登录页面。");
        setTimeout(function () {
            window.location.href = "/login";
        }, 1000);
    }
}


//保存申请信息
function submitHander(t, url, state) {
    if ($("#editForm").valid()) {
        var tips;
        if (state == 0) {
            $("#state").val(state);
            tips = "确认保存？";
        }
        layer.confirm(tips, {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            var formData;
            formData = new FormData($("#editForm")[0]);
            $.ajax({
                type: "post",
                url: url,
                data: formData,
                dataType: "json",
                async: true,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        var schemeList = "/administrative/administrative";
                        refrechPage(schemeList);
                        closeCurrentTab();
                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                    if (getResCode(data))
                        return;
                }
            });
        }, function () {
            return;
        });
    }
}