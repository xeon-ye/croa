function renderLayuiForm() {
    layui.use('form', function () {
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
            _this.attr("maxValue", maxScore);
        });
    });

    $("#plateModule").on('click', function () {
        var plateId = parseInt($("#plateSelect").val());
        plate.getChilds(plateId);
    });

    $(".savePlate").on("click", function () {
        if (!$("#plateForm").valid()) return;
        var formData = $("#plateForm").serializeArray();
        if (parseInt($("#schTotal").val()) !== parseInt($("#Aplus .score-max").val())) {
            layer.msg("考核总分必须和评价等级最高分相等");
            return;
        }
        var schemeType=parseInt($("input[name='schemeType']").val());
        var tabIndex=0;
        if (schemeType == 1) {
            //kpl绩效方案
            tabIndex=0;
            var project = $(".project");//考核项目
            var plateTarget = $(".plateTarget");//考核指标
            var plateDemand = $(".plateDemand");//指标要求
            var standard = $(".standard");//评分标准
            var score = $(".score");//单项分值
            for (var i = 0; i < project.length; i++) {
                var projectVal = project.eq(i).val();
                if (projectVal == null || projectVal == "") {
                    layer.open({
                        title: "提示",
                        content: "考核项目不能为空"
                    });
                    return;
                }
            }
            for (var k = 0; k < plateTarget.length; k++) {
                var plateTargetVal = plateTarget.eq(k).val();
                var plateDemandVal = plateDemand.eq(k).val();
                var standardVal = standard.eq(k).val();
                var scoreVal = score.eq(k).val();
                if (plateTargetVal == null || plateTargetVal == "") {
                    layer.open({
                        title: "提示",
                        content: "考核指标不能为空"
                    });
                    return;
                }
                if (plateDemandVal == null || plateDemandVal == "") {
                    layer.open({
                        title: "提示",
                        content: "指标要求不能为空"
                    });
                    return;
                }
                if (standardVal == null || standardVal == "") {
                    layer.open({
                        title: "提示",
                        content: "评分标准不能为空"
                    });
                    return;
                }
                if (scoreVal == null || scoreVal == "") {
                    if (isNaN(scoreVal)) {
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
            });
        }
    });

    $(".cancelPlate").on("click", function () {
        closeCurrentTab();
    })
});

function checkRemoveScore(){
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

//多个页面使用的方法或者数据
var commonObj = {
    //数据请求前，替换字符串中的<>符号成为&lt;&gt;
    replaceParam: function (param) {
        if(param && Object.getOwnPropertyNames(param).length > 0){
            for(var key in param){
                if(typeof param[key] && param[key].constructor == String){ //如果数据是字符串，则进行替换
                    param[key] = param[key].replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            }
        }
        return param;
    },
    //后台请求方法
    requestData: function (data, url, requestType,dataType,async,callBackFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
}

var plate = {
    plateTotalScore: 0,
    plateWeights: 0,
    plateNum: 0,
    plateList: [],
    plateModule: [],
    users:[],//当前公司所有人员集合
    chooseUsers:[],//选中的考核人员
    allUsers:{},//{id:name,...}
    isEmptyObject:function (obj) {
        var flag=true;
        for(var key in obj){
            flag =  false;
        }
        return flag;
    },
    delUser:function(id){
        $("#row_"+id).remove();
        var index = plate.chooseUsers.indexOf(id);
        if(index>-1){
            plate.chooseUsers.splice(index,1);
        }
    },
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
                $("#plateSelect").empty();
                var $plateSelect = $("#plateSelect");
                $.each(data.data.plates, function (index, value) {
                    var plateId = value.plateId;
                    var plateContent = value.plateContent;
                    if(plate.plateList.contains(plateId)){
                       var option = '<option value="' + plateId + '" disabled>' + plateContent + '</option>';
                    }else{
                       var option = '<option value="' + plateId + '">' + plateContent + '</option>';
                    }
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
