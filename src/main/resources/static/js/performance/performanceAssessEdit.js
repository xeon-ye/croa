function renderLayuiForm(){
    layui.use('form', function(){
        var form = layui.form;
        form.render()
    });
}

$(function () {
    //动态修改各个考核项目最高分数
    $("#schTotal").on('input', function () {
        plate.plateTotalScore = parseInt($("#schTotal").val());

        $.each($(".plateMaxScore"), function () {
            var _this = $(this);
            var plateProportion = parseInt(_this.attr('plateProportion'));
            var maxScore = plateProportion * plate.plateTotalScore / 100;
            _this.html("该项目最高" + maxScore + "分");
            _this.attr("maxValue", maxScore)
        });
    });

    $("#plateModule").on('click', function () {
        var plateId = parseInt($("#plateSelect").val());
        plate.getChilds(plateId);
    });

    $(".editPlate").on("click", function () {
        if (!$("#plateForm").valid()) return;
        var formData = $("#plateForm").serializeArray();
        if(parseInt($("#schTotal").val()) !== parseInt($("#Aplus .score-max").val())){
            layer.msg("考核总分必须和评价等级最高分相等");
            return;
        }
        var schemeType=parseInt($("input[name='schemeType']").val());
        var tabIndex=0;
        if(schemeType == 1){
            //kpl绩效方案
            tabIndex=0;
            var project = $(".project");//考核项目
            var plateTarget = $(".plateTarget");//考核指标
            var plateDemand = $(".plateDemand");//指标要求
            var standard = $(".standard");//评分标准
            var score = $(".score");//单项分值
            for(var i =0;i<project.length;i++){
                var projectVal = project.eq(i).val();
                if(projectVal==null || projectVal==""){
                    layer.open({
                        title: "提示",
                        content: "考核项目不能为空"
                    });
                    return;
                }
            }
            for(var k =0;k<project.length;k++){
                var plateTargetVal = plateTarget.eq(k).val();
                var plateDemandVal = plateDemand.eq(k).val();
                var standardVal = standard.eq(k).val();
                var scoreVal = score.eq(k).val();
                if(plateTargetVal==null || plateTargetVal==""){
                    layer.open({
                        title: "提示",
                        content: "考核指标不能为空"
                    });
                    return;
                }
                if(plateDemandVal==null || plateDemandVal==""){
                    layer.open({
                        title: "提示",
                        content: "指标要求不能为空"
                    });
                    return;
                }
                if(standardVal==null || standardVal==""){
                    layer.open({
                        title: "提示",
                        content: "评分标准不能为空"
                    });
                    return;
                }
                if(scoreVal==null || scoreVal==""){
                    if(isNaN(scoreVal)){
                        layer.open({
                            title: "提示",
                            content: "单项分值不能为非数字"
                        });
                        return;
                    }
                    layer.open({
                        title: "提示",
                        content: "单项分值不能为空"
                    });
                    return;
                }
            }
        }else {
            tabIndex=1;
            var userIds = $("#userIds").val();
            if(userIds==null || userIds===""){
                layer.open({
                    title:"提示",
                    content:"考核对象不能为空"
                });
                return;
            }
            var plateContent = $(".plateContent");//工作目标
            var plateProportion = $(".plateProportion");//分值
            for (var k = 0; k < plateContent.length; k++) {
                var plateContentVal = plateContent.eq(k).val();
                var plateProportionVal = plateProportion.eq(k).val();
                if (plateContentVal == null || plateContentVal == "") {
                    layer.open({
                        title: "提示",
                        content: "工作目标不能为空"
                    });
                    return;
                }
                if (plateProportionVal == null || plateProportionVal == "") {
                    layer.open({
                        title: "提示",
                        content: "分值不能为空"
                    });
                    return;
                }
            }
        }
        //计算剩余分数
        if(checkRemoveScore()){
            $.ajax({
                type: "post",
                url: baseUrl + "/performanceScheme",
                data: formData,
                dataType: "json",
                success: function (data) {
                    //跳转到指定绩效方案
                    var main = $('#content-main', parent.document);
                    main.find("iframe").each(function (i, d) {
                        //修改页面跳转导致链接修改问题
                        var url = "/performance/performanceSchemeList";
                        if ($(d).attr("src").indexOf(url)!=-1) {
                            $(d).attr("src", "/performance/performanceSchemeList?index="+tabIndex);
                        }
                    });
                    closeCurrentTab();
                }
            })
        }
    });

    $(".cancelPlate").on("click", function () {
        closeCurrentTab();
    })
});

// 设置单选框；
function setRadioState(obj, inputValue) {
    obj.each(function () {
        $(this).removeAttr("checked");
        // 移除ICheck样式；
        $(this).parent().removeClass("checked");
    });
    // 空值的处理；
    inputValue = inputValue == null ? 0 : inputValue;
    obj.each(function () {
        if ($(this).val() == inputValue) {
            $(this).prop("checked", true);
            // 移除ICheck样式；
            $(this).parent().addClass("checked");
        }
    });
}

function checkRemoveScore() {
    var schTotal = parseInt($("#schTotal").val());
    $.each($(".score"),function (i,score) {
        var grade = $(score).val()==""?0:$(score).val();
        schTotal-=grade;
    });
    $("#remainingScore").html(schTotal);
    var remainingScore=parseInt($("#remainingScore").text());
    if(remainingScore>0){
        layer.open({
            title:"提示",
            content:"剩余分值必须为0"
        });
        return false;
    }
    if(schTotal<0){
        layer.open({
            title:"提示",
            content:"剩余分值不能小于0"
        });
        return false;
    }
    return true;
}

var plate = {
    plateTotalScore: 0,
    plateWeights: 0,
    plateNum: 0,
    plateList: [],
    plateModule: [],
    setTotalScore: function () {
        plate.plateTotalScore = parseInt($("#schTotal").val());
    },
    //获取板块信息
    getLv0: function () {
        $.ajax({
            type: "get",
            url: baseUrl + "/performancePlate/listCompletePlates?plateLevel=0",
            dataType: "json",
            success: function (data) {
                var $plateSelect = $("#plateSelect");

                $.each(data.data.plates, function (index, value) {
                    var plateId = value.plateId;
                    var plateContent = value.plateContent;
                    var option = '<option value="' + plateId + '">' + plateContent + '</option>';
                    $plateSelect.append(option);
                });
                renderLayuiForm();
            }
        });
    },
    reloadWeight: function () {
        var weight = 0;
        $.each($(".plateProportion"), function () {
            weight += parseInt($(this).val());
        });
        plate.computeWeight();
    },
    getChilds: function (parentId) {
        $.ajax({
            type: "get",
            url: baseUrl + "/performancePlate/child?plateId=" + parentId,
            dataType: "json",
            success: function (data) {
                plate.plateNum++;
                var plateData = data.data;
                plate.plateWeights += plateData.parent.plateProportion;
                plate.plateList.push(parentId);
                plate.plateModule.push({
                    "plateId": plateData.parent.plateId,
                    "plateContent": plateData.parent.plateContent,
                    "plateProportion": plateData.parent.plateProportion
                });

                var html = template("plateHtml",
                    {
                        'data': plateData,
                        'num': plate.plateNum,
                        "totalScore": parseInt($("#schTotal").val()),
                        "totalWeight": plate.plateWeights
                    });
                $("#plateView").append(html);
            }
        });
    },
    initChilds: function (parentId, schId, plateLv) {
        $.ajax({
            type: "get",
            url: baseUrl + "/performanceHistory/plate",
            data: {"plateId": parentId, "schId": schId, "plateLevel": plateLv},
            dataType: "json",
            success: function (data) {
                plate.plateNum++;
                var plateData = data.data;
                plate.plateWeights += plateData.parent.plateProportion;
                plate.plateList.push(parentId);
                plate.plateModule.push({
                    "plateId": plateData.parent.plateId,
                    "plateContent": plateData.parent.plateContent,
                    "plateProportion": plateData.parent.plateProportion
                });

                var html = template("plateHtml",
                    {
                        'data': plateData,
                        'num': plate.plateNum,
                        "totalScore": parseInt($("#schTotal").val()),
                        "totalWeight": plate.plateWeights
                    });
                $("#plateView").append(html);

                plate.computeWeight();
                plate.computePlateModule();
            }
        });
    },
    computeWeight: function () {
        $(".plateMaxWeight").html("当前方案剩余权重:" + (100 - plate.plateWeights) + "%");
    },
    computePlateModule: function () {
        //填入考核板块
        var plateModuleStr = plate.plateModule.map(function (value) {
            return value.plateContent + "(" + value.plateProportion + ")"
        }).concat("").toString();
        $("#schComponent").val(plateModuleStr);
    },
    changePower: function () {

    }
};
