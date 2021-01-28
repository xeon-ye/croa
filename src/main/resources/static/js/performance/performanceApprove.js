$(function () {
    // 表格的背景色；
    $("#scoreDetailTable tr td").each(function () {
        var spanElement = $(this).find("input");
        if (spanElement && spanElement.length > 0) {
            spanElement.css({
                "width": "100%",
                "height": "30px",
                "border": "1px",
                "background-color": "white"
            });
            spanElement.attr("disabled", "disabled");
            var textAreaElement = $(this).find("textarea");
            if (textAreaElement && textAreaElement.length > 0) {
                textAreaElement.css({
                    "width": "100%",
                    "height": "100%",
                    "border": "1px",
                    "background-color": "white",
                });
            }
        } else {
            $(this).css("background-color", "#EBEBEB");
        }
    });

    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    loadPerformanceData(getQueryString("code"));

    // 增加表单校验；
    $("#approveForm").validate();
});

// 打开选择审核的窗口；
function loadPerformanceData(code) {
    if (code && code.length > 0) {
        // 获取审核用户信息；
        $.post(baseUrl + "/performanceScore/getApproveInfo", {code: code}, function (data) {
            var dataValue = data.data.taskId;
            if (dataValue == null) {
                alertMessage("该审核已完成。");
                // 返回审核页面；
                setTimeout(function () {
                    window.location.href = '/process/queryTask';
                }, 1000);
            } else {
                // 隐藏数据；
                $("#approveForm input[type='hidden'][name='deptId']").val(data.data.deptId);
                $("#approveForm input[type='hidden'][name='taskId']").val(dataValue);

                // 赋值；
                dataValue = data.data.score;
                if (dataValue != null) {
                    for (var key in dataValue) {
                        $("#approveForm input[type='text'][name='" + key + "']").val(dataValue[key]);
                        if(key=="userName"){
                            $("#approveForm input[type='text'][name='" + key + "']").css("padding-left","10px");
                        }
                        if(key=="postName"){
                            $("#approveForm input[type='text'][name='" + key + "']").css("padding-left","10px");
                        }
                        if(key=="deptName"){
                            $("#approveForm input[type='text'][name='" + key + "']").css("padding-left","10px");
                        }
                        if(key=="scoreLevel"){
                            $("#approveForm input[type='text'][name='" + key + "']").css("padding-left","10px");
                        }
                    }
                    // 主键；
                    $("#approveForm input[type='hidden'][name='schId']").val(dataValue["schId"]);
                    $("#approveForm input[type='hidden'][name='scoreId']").val(dataValue["scoreId"]);
                    $("#approveForm span[data='scoreTotal']").html(dataValue["scoreTotal"]);
                    // 分值；
                    $("#scoreSelfTotal").val(dataValue["scoreSelf"]);
                    $("#scoreLeaderTotal").val(dataValue["scoreLeader"]);
                }

                dataValue = data.data.scheme;
                var schemeType = +dataValue.schemeType;
                $("#schemeType").val(schemeType);
                if (dataValue != null) {
                    for (var key in dataValue) {
                        $("#approveForm input[type='text'][name='" + key + "']").val(dataValue[key]);
                    }

                    $("#totalScore").html(dataValue["schTotal"]);

                    // 主键；
                    $("#approveForm input[type='hidden'][name='schName']").val(dataValue["schName"]);
                    $("#schLevel").val(dataValue["schLevel"]);
                    $("#schSuffice").val(dataValue["schSuffice"]);

                    // 考核细则；
                    var plate = dataValue.historyList;

                    // 备注；
                    var proportionSelf=data.data.proportion["proportionSelf"];
                    var proportionLeader=data.data.proportion["proportionLeader"];
                    var tips="备注：1、个人自评分占比"+proportionSelf+"%，直接领导评分占比"+proportionLeader+"%；<br\>" +
                        "      2、绩效考核有异议可以到总经办、绩效主管进行投诉。";
                    $("#scoreDetailTable").append("<tr class='desc' style='text-align: left;'><td colspan='10' style='padding:0 5px 0 5px;'>" + tips + "</td></tr>");
                    var postName = dataValue.postName;

                    dataValue = data.data.proportion;
                    if (dataValue != null) {
                        if (dataValue.proBegin && dataValue.proEnd) {
                            var proDate = dataValue.proBegin + " - " + dataValue.proEnd;
                            $("#approveForm").find("input[name='proDate']").val(proDate);
                            $("#approveForm").find("input[name='proDate']").css("padding-left","10px");
                        }
                        var userName= data.data.score["userName"];
                        // 标题；
                        $("#scoreTitle").html("【"+userName+"-绩效考核】");

                        // 权重；
                        $("#proportionSelf").val(dataValue["proportionSelf"]);
                        $("#proportionLeader").val(dataValue["proportionLeader"]);

                        if(schemeType==1){
                            // 生成kpl表格；
                            $(".kpl").show();
                            $(".okr").hide();
                            $("#variableTd").attr("colspan",5);
                            if (plate && plate.length > 0) {
                                createTable(plate);
                            } else {
                                plate = data.data.detail;
                                if (plate && plate.length > 0) {
                                    createTable(plate);
                                }
                            }
                        }else {
                            // 生成okr表格；
                            $(".kpl").hide();
                            $(".okr").show();
                            $("#variableTd").attr("colspan",7);
                            if (plate && plate.length > 0) {
                                createOKRTable(plate);
                            } else {
                                plate = data.data.detail;
                                if (plate && plate.length > 0) {
                                    createOKRTable(plate);
                                }
                            }
                            //使用layui表单
                            layui.use('form', function () {
                                var form = layui.form;
                                form.render();
                                //是否完成控制未完成原因是否必填
                                form.on('radio(isChecked)',function (data) {
                                    var target = +data.value;
                                    if(target==1){
                                        //未完成计划必填
                                        $(this).parents("tr").find(".plateDemand").attr("required","required");
                                    }else {
                                        $(this).parents("tr").find(".plateDemand").removeAttr("required");
                                    }
                                });
                            });
                        }

                        // 计划类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
                        var typeContent = ["月度计划", "季度计划", "半年计划", "年终计划"];
                        var typeContentColor = ["black", "blue", "orange", "red"];
                        $("#approveForm span[data='proType']").html(typeContent[dataValue["proType"]]);
                        $("#approveForm span[data='proType']").css("color", typeContentColor[dataValue["proType"]]);
                        $("#approveForm span[data='proType']").parent().css({
                            "padding": "0 5px 0 10px",
                            "background-color": "white",
                            "text-align": "left"
                        });
                    }
                }
                $("#rollback1").html("");
                var flag = getQueryString("flag");
                var hideFlag = true;
                if(flag==2){//审核
                    $(".approve").show();
                    $(".submit").hide();
                    hideFlag = false;
                }else if(flag==0){//编辑
                    $(".approve").hide();
                    $(".submit").show();
                }else if(flag==1){//完成
                    $(".approve").hide();
                    $(".submit").hide();
                }else {
                    $(".approve").hide();
                    $(".submit").hide();
                }

                // 审批节点；
                var processState = data.data.processState;
                //是否审核完成
                if((processState != 1 && processState != -1 && processState != -9)){
                    new RollbackCompont({
                        target: "#rollback1",
                        modal: "#performanceModal",
                        taskId: data.data.score.taskId,
                        btnName:"撤回选择节点",
                        title:"撤回成功",
                        hideFlag: hideFlag,
                        refrechPageFlag: true,
                        completeCallback: function () {
                        }
                    }).render();
                }
                if (processState != null) {
                    $("#approveForm input[name='processState']").val(processState);
                    // 个人；
                    if (processState == 15) {
                        //个人审核添加自评必填
                        $("#approveForm .self").click(function () {
                            $(this).find("input").focus();
                        });
                        $("#approveForm .remark").click(function () {
                            $(this).find("textarea[data='remark']").focus();
                        });
                        $("#approveForm .demand").click(function () {
                            $(this).find("textarea[data='demand']").focus();
                        });
                        $("#approveForm input[data='scoreLeader']").attr("disabled", "disabled");
                    }

                    // 部门；
                    if (processState == 4) {
                        $("#approveForm .leader").click(function () {
                            $(this).find("input").focus();
                        });
                        $("#approveForm input[data='scoreSelf']").attr("disabled", "disabled");
                        $("#approveForm textarea[data='remark']").attr("disabled", "disabled");
                        $("#approveForm input[data='plateTarget']").attr("disabled", "disabled");
                        $("#approveForm textarea[data='plateDemand']").attr("disabled", "disabled");
                    }

                    // 人事；
                    if (processState == 13 || processState==-1) {
                        $("#approveForm input[data='scoreSelf']").attr("disabled", "disabled");
                        $("#approveForm input[data='scoreLeader']").attr("disabled", "disabled");
                        $("#approveForm textarea[data='remark']").attr("disabled", "disabled");
                        $("#approveForm input[data='plateTarget']").attr("disabled", "disabled");
                        $("#approveForm textarea[data='plateDemand']").attr("disabled", "disabled");
                    }

                    // 增加事件；
                    $("#approveForm span[data='scoreTotal']").parent().css({
                        "padding": "0 5px 0 5px",
                        "background-color": "white",
                        "text-align": "left"
                    });
                    var reg =/^\d+(\.\d+)?$/;
                    $(".scoreValue").on('input',function () {
                        if (reg.test($(this).eq(0).val())) {
                            var idKey = $(this).eq(0).attr("name");
                            var scoreSelf = $("#approveForm").find("input[name='" + idKey + "'][data='scoreSelf']").val();
                            var scoreLeader = $("#approveForm").find("input[name='" + idKey + "'][data='scoreLeader']").val();
                            var plateScoreTotal = 0;
                            if (reg.test(scoreSelf)) {
                                plateScoreTotal += parseFloat(scoreSelf) * $("#proportionSelf").val() / 100;
                            }
                            if (reg.test(scoreLeader)) {
                                plateScoreTotal += parseFloat(scoreLeader) * $("#proportionLeader").val() / 100;
                            }
                            //总分修改
                            var plateId = idKey.replace("plate","");
                            $("#approveForm").find("input[name='total" + plateId + "'][data='scoreTotal']").val(plateScoreTotal.toFixed(2));
                            //更新自评,上级总分
                            var totalPoint =0;
                            // flag:scoreSelf自评，scoreLeader上级评
                            var flag = $(this).attr("data");
                            $(".scoreValue[data='"+flag+"']").each(function (i,item) {
                                var score = $(item).val();
                                if(score!="" && score!=undefined){
                                    score = parseFloat(score);
                                }else {
                                    score=0;
                            }
                                totalPoint += score;
                            });
                            if(flag=="scoreSelf"){
                                (totalPoint+"").indexOf(".")!=-1?$("#scoreSelf").html(totalPoint.toFixed(2)):$("#scoreSelf").html(totalPoint);
                            }else {
                                (totalPoint+"").indexOf(".")!=-1?$("#scoreLeader").html(totalPoint.toFixed(2)):$("#scoreLeader").html(totalPoint);
                            }

                            // 更新总分；
                            var totalScore=0;
                            $(".totalValue").each(function (i,item) {
                                var totalValue = $(item).val();
                                if(totalValue!="" && totalValue!=undefined){
                                    totalValue = parseFloat(totalValue);
                                }else {
                                    totalValue=parseFloat(0);
                                }
                                totalScore += totalValue;
                            });
                            (totalScore+"").indexOf(".")!=-1?$("#totalValue").html(totalScore.toFixed(2)):$("#totalValue").html(totalScore);
                        }
                    });
                }

                // 审核人；
                dataValue = data.data.user;
                if (dataValue != null && dataValue.length > 0) {
                    layui.use(["form"], function () {
                        var userObj = $("#approveForm select[name='userId']");
                        var html = "<option value=''>--请选择--</option>";
                        for (var i = 0; i < dataValue.length; i++) {
                            html+="<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>";
                        }
                        userObj.append(html);
                        // 设置用户名；
                        var userNameObj = $("#approveForm input[type='hidden'][name='userName']");
                        // 初始化；
                        layui.form.render();

                        // 下拉框的onchange事件；
                        layui.form.on("select(userId)", function (userData) {
                            // 更新隐藏域；
                            userNameObj.val(getLayUISelectText(userData));
                        });
                    });
                    $("#approveForm .approveUser").show();
                } else {
                    $("#approveForm .approveUser").hide();
                }
                $("#performanceModal").modal({backdrop: "static", keyboard: false});
            }
        }, "json");
    } else {
        alertMessage("无权限访问。");
        // 返回审核页面；
        setTimeout(function () {
            window.location.href = '/process/queryTask';
        }, 1000);
    }
}

//提交审核
function submitApprove() {
    var scoreId=$("input[name='scoreId']").val();
    $.ajax({
        type:"post",
        url:baseUrl + "/performanceScore/startPerformance",
        data:{scoreId: scoreId},
        dataType:"json",
        success:function (data) {
            layer.msg(data.data.message,{time:1000,icon:6});
        }
    });
    setTimeout(function () {
        closeCurrentTab();
        refrechPage("/homePage");
    }, 2000);
    $("#performanceModal").modal("hide");
}

// 同意审核；
function agreeApprove() {
    var processState = $("#approveForm input[name='processState']").val();
    // 人事直接提交即可；
    if (processState == 13) {
        // 提交流程；
        completeApprove(0);
    } else {
        if ($("#approveForm").valid() && checkScore(processState) && checkApproveUser()) {
            var formData = $("#approveForm").serializeJson();
            // 个人；
            if (processState == 15) {
                var scoreTotal = 0;
                $("#approveForm input[data='scoreSelf']").each(function () {
                    scoreTotal += parseFloat($(this).val()) * $("#proportionSelf").val() / 100;
                });
                formData["scoreSelfTotal"] = scoreTotal;
            }
            // 部长；
            if (processState == 4) {
                var scoreTotal = 0;
                $("#approveForm input[data='scoreLeader']").each(function () {
                    scoreTotal += parseFloat($(this).val()) * $("#proportionLeader").val() / 100;
                });
                formData["scoreLeaderTotal"] = scoreTotal;
            }
            var result = checkScoreLevel();
            formData["scoreLevel"] = result["scoreLevel"];
            formData["beSuffice"] = result["beSuffice"];
            $.post(baseUrl + "/performanceScore/saveScore", {scoreData: JSON.stringify(formData)}, function (data) {
                Ladda.stopAll();
                var message = data.data.message;
                if (message == null) {
                    alertMessage(data.msg);
                } else {
                    // 提交流程；
                    completeApprove(0);
                }
            }, "json");
        }
    }
}

function changeTdStyle(t){
    var newScore = $(t).val();
    if(newScore!='' && newScore!=null){
        var reg= /^[0-9]+.?\d*$/;
        if(!reg.test(newScore)){
            return $(t).val("").focus();
        }
        $(t).parent().removeClass("input-border-red");
        $(t).parent().find("span").remove();
    }else {
        $(t).parent().addClass("input-border-red");
        if($(t).parent().find('span').length==0){
            $(t).parent().append("<span>必填</span>");
        }
    }
}

//校验分数
function checkScore(state) {
    var flag=true;
    //个人
    if (state == 15) {
        $(".self").each(function (i,item) {
            var score = $(item).find("input[data='scoreSelf']").val();
            var standard = parseInt($(item).find("input").attr("data-value"));
            if(score!="" && score!=undefined){
                // score = parseInt(score);
                $(item).removeClass("input-border-red");
                $(item).find("span").remove();
                //判断是否有错误
                flag = ($(".input-border-red").length == 0);
            }else {
                $(item).addClass("input-border-red");
                if($(item).find('span').length==0){
                    $(item).append("<span>必填</span>");
                }
                $(".input-border-red").find("input").eq(0).focus();
                flag = false;
            }
        });
    }
    // 部长；
    if (state == 4) {
        $(".leader").each(function (i,item) {
            var score = $(item).find("input[data='scoreLeader']").val();
            var standard = parseInt($(item).find("input").attr("data-value"));
            if(score!="" && score!=undefined){
                // score = parseInt(score);
                $(item).removeClass("input-border-red");
                $(item).find("span").remove();
                flag = ($(".input-border-red").length == 0);
            }else {
                $(item).addClass("input-border-red");
                if($(item).find('span').length==0){
                    $(item).append("<span>必填</span>");
                }
                $(".input-border-red").find("input").eq(0).focus();
                flag = false;
            }
        });
    }
    return flag;
}

function checkApproveUser(){
   var userId = $("select[name='userId']").val();
   if(userId=="" || userId==null || userId==undefined){
       swal("请选择审核人");
       return false;
   }
   return true;
}

// 拒绝审核；
function rejectApprove() {
    completeApprove(1);
}

// 计算评分等级和判断是否合格；
function checkScoreLevel() {
    var schLevel = JSON.parse($("#schLevel").val());
    var schSuffice = $("#schSuffice").val();
    var levelData;
    var levelContent = "";
    var levelIndex;
    var sufficeIndex;
    var score = parseFloat($("#totalValue").text());
    // 遍历；
    for (var i = 0; i < schLevel.length; i++) {
        levelData = schLevel[i];
        if (score >= levelData.min && score < levelData.max) {
            levelContent = levelData.lv;
            levelIndex = i;
        }
        if (schSuffice == levelData.lv) {
            sufficeIndex = i;
        }
    }
    var result = {};
    result["scoreLevel"] = levelContent;
    result["beSuffice"] = levelIndex <= sufficeIndex;
    return result;
}