var commonTypeCode = 'CUST_PROTECT_';
var num = "NUM_";
var eval = "EVAL_";
var audit = "AUDIT_";
var levels = ["A", "B", "C"];
$(document).ready(function () {
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    var e = "<i class='fa fa-times-circle'></i> ";
    // $("#queryForm").validate({
    //     rules: {
    //         age: {minlength: 1, maxlength: 3},
    //         area: {required: !0},
    //     },
    //     messages: {
    //         age: {minlength: e + "年龄长度必须大于{0}个字符", maxlength: e + "年龄长度必须小于{0}个字符"},
    //         area: {required: e + "请输入地域分布"},
    //     }
    // });

    protObj.view($("#level").val());

    $("#classA").click(function () {
        protObj.common("A");
    });
    $("#classB").click(function () {
        protObj.common("B");
    });
    $("#classC").click(function () {
        protObj.common("C");
    });
});
var protObj = {
    common: function(level){
        $.each( levels, function(index, item){
            if(item == level){
                $("#level").val(level);
                protObj.view(item);
                $("#class"+item).attr("disabled", "disabled");
                if($("#class"+item).hasClass("btn-white")){
                    $("#class"+item).removeClass("btn-white");
                }
                if(!$("#class"+item).hasClass("btn-success")){
                    $("#class"+item).addClass("btn-success");
                }
            }else{
                $("#class"+item).removeAttr("disabled");
                if($("#class"+item).hasClass("btn-success")){
                    $("#class"+item).removeClass("btn-success");
                }
                if(!$("#class"+item).hasClass("btn-white")){
                    $("#class"+item).addClass("btn-white");
                }
            }
        });
    },
    view: function (level) {
        $.ajax({
            type: "get",
            url: "/dict/listByTypeCode2",
            data: {typeCode: commonTypeCode + level},
            dataType: "json",
            success: function (list) {
                //先清空，
                $("#queryForm [name='num']").val("");
                $("#queryForm input:radio").removeAttr("checked");
                $("#queryForm input:radio").parent().removeClass("checked");
                //填充数据
                if(list != null && list.length > 0){
                    for(var i=0; i< list.length; i++){
                        if(list[i].code == num + level){
                            $("#queryForm [name='num']").val(list[i].type);
                            continue;
                        }
                        if(list[i].code == eval + level){
                            $("#queryForm input[name='eval'][value='" + list[i].type + "']").iCheck("check");
                            continue;
                        }
                        if(list[i].code == audit + level){
                            $("#queryForm input[name='audit'][value='" + list[i].type + "']").iCheck("check");
                            continue;
                        }
                    }
                }
            }
        });
        $.ajax({
            type: "get",
            url: "/dict/listByTypeCode2",
            data: {typeCode: "CUST_TRANSFER"},
            dataType: "json",
            success: function (list) {
                //填充数据
                if(list != null && list.length > 0){
                    for(var i=0; i< list.length; i++){
                        var code = list[i].code;
                        switch (code) {
                            case "TRACK_EVAL_DAY":
                                $("#queryForm [name='TRACK_EVAL_DAY']").val(list[i].type);
                                break;
                            case "DEAL_EVAL_DAY":
                                $("#queryForm [name='DEAL_EVAL_DAY']").val(list[i].type);
                                break;
                            case "EVAL_REMIND_DAY":
                                $("#queryForm [name='EVAL_REMIND_DAY']").val(list[i].type);
                                break;
                            case "TO_BLACK_TIMES":
                                $("#queryForm [name='TO_BLACK_TIMES']").val(list[i].type);
                                break;
                            case "CLAIM_TIMES_DAY":
                                $("#queryForm [name='CLAIM_TIMES_DAY']").val(list[i].type);
                                break;
                            case "CLAIM_START_TIME":
                                $("#queryForm [name='CLAIM_START_TIME']").val(list[i].type);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        })
    },

    save: function (t) {
        if($("#queryForm").valid()){
            var lock = true;
            layer.confirm("确认提交更改？", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if(lock){
                    lock = false;
                    $.ajax({
                        type: "post",
                        url: baseUrl + "/dict/updateCompanyParam",
                        data: $("#queryForm").serializeJson(),
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            swal("操作成功");
                            $("#editModal").modal("hide");
                        },
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        }else{
            return;
        }
    }
};