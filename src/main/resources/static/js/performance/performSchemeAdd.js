var excludePeople = [];
var evaluationPeople = [];
var excludeUsers=[];//当前公司排除人员集合
var selectExcludeUsers=[];//选中的排除人员
var allExcludeUsers={};//{id:name,...}
var level = ['A+', 'A', 'A-', 'B+', 'B', 'B-', 'C+', 'C'];
var schType = ['月度计划', '季度计划', '年中计划', '年终计划'];


$(function () {
    if(getQueryString("type")!=null && getQueryString("type")!=undefined){
        var type = parseInt(getQueryString("type"));
        //赋值方案类型
        $("input[name='schemeType']").val(type);
        if(type==1){
            //kpl绩效方案
            $("#schemeType").html("KPI");
            loadPostData();
            addTrElem();
            $(".kpl").show();
            $(".kpl").find("input").removeAttr("disabled");
            $(".okr").hide();
            $(".okr").find("input").attr("disabled","disabled");
        }else {
            //okr绩效方案
            $("#schemeType").html("OKR");
            $("#schTotal").val(100);
            $("#remainingScore").html(100);
            loadUserData();
            addOKRTrElem();
            $(".kpl").hide();
            $(".kpl").find("input").attr("disabled","disabled");
            $(".okr").show();
            $(".okr").find("input").removeAttr("disabled");
        }
    }

    $("#schType").change(function () {
        var postName = $("#postId").find("option:selected").text();
        $("#schName").val(postName + schType[$("#schType").val()]);
    });

    $("#choseLevel").click(function () {
        initScore();
        var limit = $("#select-level-limit"), schTotal = $("#schTotal");
        var totalVal = parseInt(schTotal.val());
        if (!totalVal || totalVal === 0) {
            layer.msg("请先填入考评总分");
            $('#levelModal').modal('hide');
        } else if (totalVal > 0) {
            limit.html("不能大于考评总分" + totalVal);
        }
        $("#levelModal").modal("toggle");
    });

    $("#submitScoreLevel").click(function () {
        var totalVal = parseInt($("#schTotal").val());
        var arrMin = [];
        var arrMax = [];
        var $scoreMin = $(".score-min");
        $scoreMin.each(function () {
            arrMin.push(parseInt($(this).val()))
        });
        var $scoreMax = $(".score-max");
        $scoreMax.each(function () {
            arrMax.push(parseInt($(this).val()))
        });

        $scoreMax.removeClass("font-red");
        $scoreMin.removeClass("font-red");

        if (arrMax[0] !== totalVal) {
            layer.alert("A+级别最大值必须为" + totalVal);
            $("#Aplus").find(".score-max").addClass("font-red");
            return;
        }

        if (arrMin[7] < 0) {
            layer.alert("最小值不可为负数");
            $("#C").find(".score-min").addClass("font-red");
            return;
        }

        for (var i = 0; i < 8; i++) {
            var domEl = level[i].replace("+", "plus").replace("-", "redu");
            var $removeRed = $("#" + domEl);

            if (arrMax[i] <= arrMin[i]) {
                layer.alert("最小值不得大于等于最大值," + level[i] + "级别分数");
                $removeRed.find(".score-max").addClass("font-red");
                $removeRed.find(".score-min").addClass("font-red");
                return;
            }

            //分支交叉，空挡计算
            if (i > 0) {
                var domElForward = level[i - 1].replace("+", "plus").replace("-", "redu");
                var $removeRedForward = $("#" + domElForward);

                if (arrMax[i] > arrMin[i - 1]) {
                    layer.alert("分数值交叉," + level[i - 1] + "和" + level[i] + ","
                        + arrMax[i] + "不能大于" + arrMin[i - 1]);
                    $removeRed.find(".score-max").addClass("font-red");
                    $removeRedForward.find(".score-min").addClass("font-red");
                    return;
                }
                if (arrMax[i] < arrMin[i - 1]) {
                    layer.alert("分数值有空档," + level[i - 1] + "和" + level[i] + ","
                        + arrMax[i] + "不能小于" + arrMin[i - 1]);
                    $removeRed.find(".score-max").addClass("font-red");
                    $removeRedForward.find(".score-min").addClass("font-red");
                    return;
                }
            }
        }

        var htmlMsg = "";

        var lvJson = [];

        for (var j = 0; j < 8; j++) {
            htmlMsg += level[j] + "(" + arrMin[j] + "-" + arrMax[j] + ")" + ";";
            var lv = {
                "lv": level[j],
                "min": arrMin[j],
                "max": arrMax[j]
            };
            lvJson.push(lv);
        }
        $("#schLevelView").val(htmlMsg);

        $("#schLevel").val(JSON.stringify(lvJson));

        $('#levelModal').modal('hide');
    });

    function initScore() {
        var totalScore = $("#schTotal").val();
        var arrMin = [], arrMax = [];
        for (var i = 0; i < 8; i++) {
            arrMax.push(totalScore - 10 * i);
            arrMin.push(totalScore - 10 * (i + 1));
        }

        $.each($(".score-min"), function (index, value) {
            $(this).val(arrMin[index] <= 0 ? 0 : arrMin[index])
        });

        $.each($(".score-max"), function (index, value) {
            $(this).val(arrMax[index] <= 0 ? 0 : arrMax[index])
        });
    }
    $("#submitExcludePeople").click(function () {
        excludePeople = [];
        var btnList = $('#excludeModal').find("input[type='checkbox']").filter(function () {
            return $(this).is(":checked") && !$(this).hasClass("deptSpan")
        }).map(function () {
            var userName = $(this).attr("userName");
            var userId = $(this).attr("userId");
            var deptId = $(this).attr("deptId");
            var $span = $('<span class="col-md-1" deptId="' + deptId + '" userId="' + userId + '">' + userName + '</span>');
            $span.attr("type", 0);
            excludePeople.push(userId);
            var inputUserId = $('<input name="schUserIdLs" type="hidden" value="' + userId + '">');
            var inputUserName = $('<input name="schUserNameLs" type="hidden" value="' + userName + '">');
            $span.attr("deptId", deptId);
            $span.on("click", removeBtn);
            $span.append(inputUserId);
            $span.append(inputUserName);
            return $span
        }).get();

        if (btnList.length === 0) {
            excludePeople = [];
        }
        $("#exclude-people-btn").html(btnList)

        $('#excludeModal').modal('hide');
    });

    $("#excludePeople").click(function () {
        queryExcludePeople();
    })
});

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
//加载职位数据
function loadPostData() {
    $.ajax({
        type: "get",
        url: baseUrl + "/entry/getPostByCompanyCode",
        dataType: "json",
        success: function (data) {
            var $postId = $("#postId");

            $.each(data.data.post, function (index, value) {
                var $optionStr = $("<option></option>");
                $optionStr.html(value.name);
                $optionStr.attr("value", value.id);
                $optionStr.attr("postName", value.name);
                $postId.append($optionStr)
            });

            $postId.change(function () {
                var text = $(this).find("option:selected").text();
                $("#postName").val(text);
                $("#schName").val(text + schType[$("#schType").val()]);
            });

            var postName = $postId.find("option:eq(0)").text();
            $("#postName").val(postName);

            $("#schName").val(postName + schType[$("#schType").val()]);
        }
    });
}

//加载考核对象数据
function loadUserData() {
    var option="<option value=''>请选择</option>";
    layui.use('form',function () {
        var form =layui.form;
        $.ajax({
            type: "get",
            url: baseUrl + "/user/queryUserByCondition",
            data:{postId:null},
            dataType: "json",
            success: function (data) {
                $.each(data, function (index, value) {
                    option += "<option value='"+value.id+"' data-value='"+value.name+"'>"+value.name+"("+value.deptName+")</option>";
                });
                $("#userIds").html(option);
                form.render();
            }
        });
        form.on("select(userIds)",function (data) {
            var userName = $(data.elem).find("option:selected").attr("data-value");
            $("input[name='groupNamesLs']").val(userName);
        });
        form.render();
    })
}

//添加一行kpl表格数据
function addTrElem() {
    var html = "<tr class='tr1'>\n" +
        "<td style='text-align: center'><span class='plus' title='添加项目' onclick='addProjectTrElem()'><i class='fa fa-plus-circle'></i></span></td>" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"project plateContent form-control\" name=\"plateContent\" autocomplete='off'></textarea>\n" +
        "<input type=\"hidden\" value=\"1\" name=\"plateId\" />" +
        "<input type=\"hidden\" value=\"0\" name=\"plateLevel\"/>" +
        "<input type=\"hidden\" value=\"0\" name=\"plateParent\"/>" +
        "<input type=\"hidden\" value=\"1\" name=\"plateOrder\" >" +
        "<input type=\"hidden\" value=\"\" name=\"plateTarget\" >" +
        "<input type=\"hidden\" value=\"\" name=\"plateDemand\" >" +
        "<input type=\"hidden\" value=\"100\" name=\"plateProportion\" >" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateTarget form-control\" name=\"plateTarget\" autocomplete='off'></textarea>\n" +
        "<input type=\"hidden\" value=\"2\" name=\"plateId\" />" +
        "<input type=\"hidden\" value=\"1\" name=\"plateLevel\"/>" +
        "<input type=\"hidden\" value=\"1\" name=\"plateParent\"/>" +
        "<input type=\"hidden\" value=\"1\" name=\"plateOrder\" >" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateDemand form-control\" name=\"plateDemand\" type=\"text\"></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"standard plateContent form-control\" name=\"plateContent\"  type=\"text\"></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<input class=\"score plateProportion form-control\" autocomplete='off' name=\"plateProportion\" oninput=\"changeScore(this)\" onkeyup=\"value=value.replace(/[^\\d.]/g,'')\" type=\"text\"/>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<span class='plus' title='指标跨行' onclick='addRowSpanTrElem(this)'><i class='fa fa-plus-circle'></i></span>" +
        "<span class='minus minus-style' title='移除此行' style='display: none;' onclick='removeRowSpanTrElem(this)'><i class='fa fa-minus-circle'></i></span>"+
        "</td>\n" +
        "</tr>";
    $("#checkTable tbody").html(html);
}

//添加一行okr表格数据
function addOKRTrElem() {
    var html = "<tr>\n" +
        "<td style='text-align: center'><span class='plus' title='添加项目' onclick='addOKRProjectTrElem()'><i class='fa fa-plus-circle'></i></span></td>" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"project plateContent form-control\" name=\"plateContent\" autocomplete='off'></textarea>\n" +
        "<input type=\"hidden\" value=\"1\" name=\"plateId\" />" +
        "<input type=\"hidden\" value=\"0\" name=\"plateLevel\"/>" +
        "<input type=\"hidden\" value=\"0\" name=\"plateParent\"/>" +
        "<input type=\"hidden\" value=\"1\" name=\"plateOrder\" >" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<input class=\"score plateProportion form-control\" autocomplete='off' name=\"plateProportion\" oninput=\"changeScore(this)\" onkeyup=\"value=value.replace(/[^\\d.]/g,'')\" type=\"text\"/>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateTarget form-control\" name=\"plateTarget\" autocomplete='off'></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateDemand form-control\" name=\"plateDemand\" type=\"text\"></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<span class='minus minus-style' title='移除此行' onclick='removeTrElem(this)'><i class='fa fa-minus-circle'></i></span>"+
        "</td>\n" +
        "</tr>";
    $("#okrTable tbody").html(html);
}
//修改总分
function changeTotalScore(){
    var schTotal = parseInt($("#schTotal").val());
    $("#remainingScore").html(schTotal);
}

//修改剩余分值
function changeScore(t){
    var schTotal = parseInt($("#schTotal").val());
    $.each($(".score"),function (i,score) {
        var grade = $(score).val()==""?0:$(score).val();
        schTotal-=grade;
    });
    if(schTotal<0){
        layer.msg("剩余分值不能小于0");
        $(t).val(0);
        $(t).addClass("font-red");
    }else{
        $("#remainingScore").html(schTotal);
        $(".score").removeClass("font-red");
    }
}

var firstOrder=2;
var secondOrder=100;
//添加kpl项目数据
function addProjectTrElem() {
    firstOrder++;
    secondOrder++;
    var html = "<tr class='tr"+firstOrder+"'>\n" +
        "<td style='text-align: center'><span class='plus' title='添加项目' onclick='addProjectTrElem(this)'><i class='fa fa-plus-circle'></i></span></td>" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"project plateContent form-control\" name=\"plateContent\" autocomplete='off'></textarea>\n" +
        "<input type=\"hidden\" value=\""+firstOrder+"\" name=\"plateId\" />" +
        "<input type=\"hidden\" value=\"0\" name=\"plateLevel\"/>" +
        "<input type=\"hidden\" value=\"0\" name=\"plateParent\"/>" +
        "<input type=\"hidden\" value=\""+firstOrder+"\" name=\"plateOrder\" >" +
        "<input type=\"hidden\" value=\"\" name=\"plateTarget\" >" +
        "<input type=\"hidden\" value=\"\" name=\"plateDemand\" >" +
        "<input type=\"hidden\" value=\"100\" name=\"plateProportion\" >" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateTarget form-control\" name=\"plateTarget\" autocomplete='off'></textarea>\n" +
        "<input type=\"hidden\" value=\""+secondOrder+"\" name=\"plateId\" />" +
        "<input type=\"hidden\" value=\"1\" name=\"plateLevel\"/>" +
        "<input type=\"hidden\" value=\""+firstOrder+"\" name=\"plateParent\"/>" +
        "<input type=\"hidden\" value=\"2\" name=\"plateOrder\" >" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateDemand form-control\" name=\"plateDemand\" type=\"text\"></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"standard plateContent form-control\" name=\"plateContent\"  type=\"text\"></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<input class=\"score plateProportion form-control\" name=\"plateProportion\" autocomplete='off' oninput=\"changeScore(this)\" onkeyup=\"value=value.replace(/[^\\d.]/g,'')\" type=\"text\"/>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<span class='plus plus-style' title='指标跨行' onclick='addRowSpanTrElem(this)'><i class='fa fa-plus-circle'></i></span>" +
        "<span class='minus minus-style' title='移除此行' onclick='removeRowSpanTrElem(this)'><i class='fa fa-minus-circle'></i></span>"+
        "</td>\n" +
        "</tr>";
    $("#checkTable tbody").append(html);
}
//添加okr项目数据
function addOKRProjectTrElem() {
    firstOrder++;
    var html = "<tr>\n" +
        "<td style='text-align: center'><span class='plus' title='添加项目' onclick='addOKRProjectTrElem(this)'><i class='fa fa-plus-circle'></i></span></td>" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"project plateContent form-control\" name=\"plateContent\" autocomplete='off'></textarea>\n" +
        "<input type=\"hidden\" value=\""+firstOrder+"\" name=\"plateId\" />" +
        "<input type=\"hidden\" value=\"0\" name=\"plateLevel\"/>" +
        "<input type=\"hidden\" value=\"0\" name=\"plateParent\"/>" +
        "<input type=\"hidden\" value=\"1\" name=\"plateOrder\" >" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<input class=\"score plateProportion form-control\" name=\"plateProportion\" autocomplete='off' oninput=\"changeScore(this)\" onkeyup=\"value=value.replace(/[^\\d.]/g,'')\" type=\"text\"/>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateTarget form-control\" name=\"plateTarget\" autocomplete='off'></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateDemand form-control\" name=\"plateDemand\" type=\"text\"></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<span class='minus minus-style' title='移除此行' onclick='removeTrElem(this)'><i class='fa fa-minus-circle'></i></span>"+
        "</td>\n" +
        "</tr>";
    $("#okrTable tbody").append(html);
}

//添加kpl项目跨行数据
function addRowSpanTrElem(t){
    firstOrder++;
    secondOrder++;
    $(t).css("pointer-events","none");
    //获取需要跨行的行元素
    var trElem = $(t).parent().parent();
    //获取跨行的id
    var parentId = $(trElem).find("td:nth-child(2) input[name='plateId']").val();
    var html = "<tr class='tr"+parentId+"'>\n" +
        "<td style='text-align: center'><span class='plus' title='添加项目' onclick='addProjectTrElem(this)'><i class='fa fa-plus-circle'></i></span></td>" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"project plateContent form-control\" name=\"plateContent\" autocomplete='off'></textarea>\n" +
        "<input type=\"hidden\" value=\""+firstOrder+"\" name=\"plateId\" />" +
        "<input type=\"hidden\" value=\"0\" name=\"plateLevel\"/>" +
        "<input type=\"hidden\" value=\""+parentId+"\" name=\"plateParent\"/>" +
        "<input type=\"hidden\" value=\"1\" name=\"plateOrder\" >" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateTarget form-control\" name=\"plateTarget\" autocomplete='off'></textarea>\n" +
        "<input type=\"hidden\" value='"+secondOrder+"' name=\"plateId\" />" +
        "<input type=\"hidden\" value=\"1\" name=\"plateLevel\"/>" +
        "<input type=\"hidden\" value=\""+parentId+"\" name=\"plateParent\"/>" +
        "<input type=\"hidden\" value=\"1\" name=\"plateOrder\" >" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"plateDemand form-control\" name=\"plateDemand\" type=\"text\"></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<textarea class=\"standard plateContent form-control\" name=\"plateContent\"  type=\"text\"></textarea>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<input class=\"score plateProportion form-control\" name=\"plateProportion\" autocomplete='off' oninput=\"changeScore(this)\" onkeyup=\"value=value.replace(/[^\\d.]/g,'')\" type=\"text\"/>\n" +
        "</td>\n" +
        "<td style='text-align: center;'>\n" +
        "<span class='minus' title='移除此行' onclick='removeRowSpanTrElem(this)'><i class='fa fa-minus-circle'></i></span>"+
        "</td>\n" +
        "</tr>";
    //在相同class行的最后一行添加数据
    var lastIndex =  $(".tr"+parentId).size()-1;
    $(".tr"+parentId+":eq("+lastIndex+")").after(html);
    //获取需要跨行的td
    var tdElem = $(trElem).find("td:first-child");
    var rowspan = $(tdElem).attr("rowspan")==undefined?1:$(tdElem).attr("rowspan");
    var index = parseInt(rowspan)+1;
    //跨行
    $(tdElem).attr("rowspan",index);
    //获取需要跨行的td
    var tdElem2 = $(trElem).find("td:nth-child(2)");
    var rowspan2 = $(tdElem2).attr("rowspan")==undefined?1:$(tdElem2).attr("rowspan");
    var index2 = parseInt(rowspan2)+1;
    //跨行
    $(tdElem2).attr("rowspan",index2);
    var trArray = $(".tr"+parentId);
    var trSize=parseInt($(trArray).size());
    //如果跨行去除第一行的移除按钮
    if(trSize>1){
        var secondTdElem = $(".tr"+parentId+":eq(0)").find("td:nth-child(7)");
        $(secondTdElem).find(".minus").css("display","none");
        $(secondTdElem).find(".plus").removeClass("plus-style");
    }
    //移除多余td
    $.each(trArray,function (i,item) {
       var tdLength = $(item).find("td").size();
       if(i>0 && tdLength==7){
           //移除第一个td
           $(item).find("td:eq(0)").remove();
           //移除原来第二个td
           $(item).find("td:eq(0)").remove();
       }
    });
    $(t).css("pointer-events","auto");
}

//移除kpl跨行数据
function removeRowSpanTrElem(t) {
    //获取需要跨行的行元素
    var trElem = $(t).parent().parent();
    //获取跨行的id
    var parentId = $(trElem).find("td:first-child input[name='plateParent']").val();
    //第一行
    var trElem = $(".tr"+parentId+":eq(0)");
    //获取需要跨行的td
    var tdElem = $(trElem).find("td:first-child");
    var rowspan = $(tdElem).attr("rowspan")==undefined?1:$(tdElem).attr("rowspan");
    var index = parseInt(rowspan);
    if(index>1){
        //移除指定行数据
        $(tdElem).attr("rowspan",(index-1));
        $(t).parent().parent().remove();
        if(index==2){
            var secondTdElem = $(trElem).find("td:nth-child(7)");
            //如果有两行，移除一行显示移除按钮
            $(secondTdElem).find(".minus").css("display","inline-block");
            $(secondTdElem).find(".plus").addClass("plus-style");
        }
    }else{
        //移除整行数据
        $(t).parent().parent().remove();
    }
    //获取需要跨行的td
    var tdElem2 = $(trElem).find("td:nth-child(2)");
    var rowspan2 = $(tdElem2).attr("rowspan")==undefined?1:$(tdElem2).attr("rowspan");
    var index2 = parseInt(rowspan2);
    if(index2>1){
        //移除指定行数据
        $(tdElem2).attr("rowspan",(index2-1));
        $(t).parent().parent().remove();
        if(index==2){
            var secondTdElem = $(trElem).find("td:nth-child(7)");
            //如果有两行，移除一行显示移除按钮
            $(secondTdElem).find(".minus").css("display","inline-block");
            $(secondTdElem).find(".plus").addClass("plus-style");
        }
    }else{
        //移除整行数据
        $(t).parent().parent().remove();
    }
}
//移除okr指定行数据
function removeTrElem(t) {
    var trElemLength = $("#okrTable tbody").find("tr").length;
    if(trElemLength>1){
        //移除行元素
        $(t).parent().parent().remove();
    }else {
        layer.open({
           title:"提示",
           content:"表格至少含有一行数据"
        });
    }
}
//核查分数范围
function checkScoreRange(t,index) {
    var score = $(t).val();
    if(score==null || score=="" || score==undefined){
        layer.open({
           title:"提示",
           content:"分数不能为空"
        });
        return;
    }
    var totalVal = parseInt($("#schTotal").val());
    var arrMin = [];
    var arrMax = [];
    var $scoreMin = $(".score-min");
    $scoreMin.each(function () {
        arrMin.push(parseInt($(this).val()));
    });
    var $scoreMax = $(".score-max");
    $scoreMax.each(function () {
        arrMax.push(parseInt($(this).val()));
    });

    $scoreMax.removeClass("font-red");
    $scoreMin.removeClass("font-red");

    if (arrMax[0] !== totalVal) {
        layer.alert("A+级别最大值必须为" + totalVal);
        $("#Aplus").find(".score-max").addClass("font-red");
        return;
    }
    if (arrMin[7] < 0) {
        layer.alert("最小值不可为负数");
        $("#C").find(".score-min").addClass("font-red");
        return;
    }
    //判断是否是最小值
    var flag = $(t).hasClass("score-min");
    if(flag){//最小值
        if(index<=6){
            //上一级元素
            var domElAfter = level[index+1].replace("+", "plus").replace("-", "redu");
            var $removeRedAfter = $("#" + domElAfter);
            //本级元素
            var domEl = level[index].replace("+", "plus").replace("-", "redu");
            var $removeRed = $("#" + domEl);
            //最小值必须大于下一级的最大值，并且小于本级的最大值
            if(score>= arrMax[index+1] && score<=arrMax[index]){
                $removeRedAfter.find(".score-max").val(parseInt(score));
            }else{
                layer.alert("分数值交叉,此分数在" + level[index+1] + "和" + level[index] + "评价等级之间,该分数必须大于"
                            + arrMax[index+1] + "并且必须小于" + arrMax[index]);
                $removeRed.find(".score-min").addClass("font-red");
            }
        }
    }else{//最大值
        if(index>=1){
            //上一级元素
            var domElForward = level[index-1].replace("+", "plus").replace("-", "redu");
            var $removeRedForward = $("#" + domElForward);
            //本级元素
            var domEl = level[index].replace("+", "plus").replace("-", "redu");
            var $removeRed = $("#" + domEl);
            //最大值必须大于本级的最小值，并且小于上一级的最小值
            if(score>= arrMin[index] && score<=arrMin[index-1]){
                $removeRedForward.find(".score-min").val(parseInt(score));
            }else{
                layer.alert("分数值交叉,此分数在" + level[index] + "和" + level[index-1] + "的评价等级之间,该分数必须大于"
                    + arrMin[index] + "并且必须小于" + arrMin[index-1]);
                $removeRed.find(".score-max").addClass("font-red");
            }
        }
    }

    for (var i = 0; i < 8; i++) {
        var domEl = level[i].replace("+", "plus").replace("-", "redu");
        var $removeRed = $("#" + domEl);

        if (arrMax[i] <= arrMin[i]) {
            layer.alert("最小值不得大于等于最大值," + level[i] + "级别分数");
            $removeRed.find(".score-max").addClass("font-red");
            $removeRed.find(".score-min").addClass("font-red");
            return;
        }
    }
    var htmlMsg = "";
    var lvJson = [];
    for (var j = 0; j < 8; j++) {
        htmlMsg += level[j] + "(" + arrMin[j] + "-" + arrMax[j] + ")" + ";"
        var lv = {
            "lv": level[j],
            "min": arrMin[j],
            "max": arrMax[j]
        };
        lvJson.push(lv);
    }
    $("#schLevelView").val(htmlMsg);

    $("#schLevel").val(JSON.stringify(lvJson));
}


function queryEvaluationPeople() {
    var rootDom = $("#evaluationModal");
    //排除人员
    var name = rootDom.find("input[data-id='nameQc']").val();
    var listUser = name ? "/user/list?companyCode=" + user.companyCode + "&state=1&name=" + name
        : "/user/list?companyCode=" + user.companyCode + "&state=1";
    $.get(baseUrl + listUser, function (data) {
        var userList = groupBy(data, function (item) {
            return [item.deptId];
        });

        var html = template("excludePeopleHtml", {'data': userList});
        rootDom.find("div[data-id='groups']").html(html);
        reloadICheck(rootDom);

        rootDom.find(".deptSpan").on('ifChecked', function () {
            $(this).parent().parent().parent().next().find("input[type='checkbox']")
                .iCheck($(this).is(':checked') ? 'check' : 'uncheck');
        });

        rootDom.find(".deptSpan").on('ifUnchecked', function () {
            $(this).parent().parent().parent().next().find("input[type='checkbox']")
                .iCheck($(this).is(':checked') ? 'check' : 'uncheck');
        });

        rootDom.find('input[data-id="all"]').on('ifChecked', function () {
            rootDom.find("div[data-id='groups']").find(".i-checks").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
        });

        rootDom.find('input[data-id="all"]').on('ifUnchecked', function () {
            rootDom.find("div[data-id='groups']").find(".i-checks").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
        });

    }, "json");
}

//排除人员展示框
function queryExcludePeople() {
    //排除人员
    var postId = $("#postId").val();
    var param = {state: 1, companyCode: user.companyCode};
    if (postId) param.postId = parseInt(postId);

    //如果没有值，则请求，并进行缓存
    commonObj.requestData(param,"/performanceScheme/listUserByParam", "post", "json", false, function (data) {
        excludeUsers = data;
        if (data && data.length > 0) {
            for (var i = 0; i < data.length; i++) {
                var id = data[i].id;
                var name = data[i].name;
                allExcludeUsers[id] = name;
            }
        }
    });
    new SysUserCompont({
        title:"排除人员",
        url:"/performanceScheme/listUserByParam",
        param:param,
        defaultGroupName: "",
        dataList: excludeUsers,
        roleTypeMap: null,
        chooseDataList:selectExcludeUsers,
        zIndex: 1,
        resultCallBack:function (dataList) {
            selectExcludeUsers=[];
            $("#exclude-people-btn").empty();
            $.each(dataList, function (zj, item) {
               selectExcludeUsers.push(item);
            });
            var list=selectExcludeUsers;
            if (list && list.length>0) {
                var html="";
                for(var i=0;i<list.length;i++){
                    html+= '<div id="row_' + list[i] + '" class="col-md-2 tdd" style="margin: 3px;" >' +
                        '<div style="border: 1px solid #eeece4;padding: 5px 0;display:  flex;align-items:  center;justify-content:  center;">' +
                        '<input type="hidden"  name="schUserIdLs" id="userId_' + list[i] + '" value="' + list[i] + '">' +
                        '<input type="hidden" name="schUserNameLs"  id="userName_' + list[i] + '" value="' + allExcludeUsers[list[i]] + '">' +
                        '<span style="overflow: hidden;text-overflow: ellipsis;white-space: nowrap" title="'+allExcludeUsers[list[i]]+'">'+allExcludeUsers[list[i]]+'&nbsp;&nbsp;</span>' +
                        '<button type="button" id="button_"+' + list[i] + ' onclick="delUser(' + list[i] + ')" ' +
                        'style="padding: 0px 5px;margin-right: 5px" class="btn btn-outline btn-sm btn-danger">X</button></div></div>';

                }
                $("#exclude-people-btn").html(html);
            }
            layui.use('form', function(){
                var form = layui.form;
                form.render();
            });
        },
        endCallBack:function () {
            layui.use('form', function(){
                var form = layui.form;
                form.render();
            });
        }
    });
    // $.ajax({
    //     type: "get",
    //     url: baseUrl + listUser,
    //     dataType: "json",
    //     data: param,
    //     success: function (data) {
    //         var userList = groupBy(data, function (item) {
    //             return [item.deptId];
    //         });
    //
    //         var html = template("excludePeopleHtml", {'data': userList});
    //         rootDom.find("div[data-id='groups']").html(html);
    //         reloadICheck(rootDom);
    //
    //         rootDom.find(".deptSpan").on('ifChecked', function () {
    //             $(this).parent().parent().parent().next().find("input[type='checkbox']")
    //                 .iCheck($(this).is(':checked') ? 'check' : 'uncheck');
    //         });
    //
    //         rootDom.find(".deptSpan").on('ifUnchecked', function () {
    //             $(this).parent().parent().parent().next().find("input[type='checkbox']")
    //                 .iCheck($(this).is(':checked') ? 'check' : 'uncheck');
    //         });
    //
    //         rootDom.find('input[data-id="all"]').on('ifChecked', function () {
    //             rootDom.find("div[data-id='groups']").find(".i-checks").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
    //         });
    //
    //         rootDom.find('input[data-id="all"]').on('ifUnchecked', function () {
    //             rootDom.find("div[data-id='groups']").find(".i-checks").iCheck($(this).is(':checked') ? 'check' : 'uncheck');
    //         });
    //     }
    // })
}

function delUser(id){
    $("#row_"+id).remove();
    var index = selectExcludeUsers.indexOf(id.toString());
    if(index>-1){
        selectExcludeUsers.splice(index,1);
    }
}

function removeBtn() {
    var userId = $(this).attr("userId");
    var i = $(this).attr("type");
    // var deptId = "dept" + $(this).attr("deptId");
    // $("input[sign='" + deptId + "']").iCheck('uncheck');
    var newArray = [];
    if (i == 0) {
        excludePeople.forEach(function (value) {
            if (value != userId)
                newArray.push(value)
            else
                $("#excludeModal").find("div[data-id='groups'] input[userId='" + value + "']").iCheck("uncheck");
        });
        excludePeople = newArray;
    } else {
        evaluationPeople.forEach(function (value) {
            if (value != userId)
                newArray.push(value)
            else
                $("#evaluationModal").find("div[data-id='groups'] input[userId='" + value + "']").iCheck("uncheck");
        });
        evaluationPeople = newArray;
    }

    $(this).remove();
}

function reloadICheck(root) {
    root.find('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green'
    });
}

function groupBy(array, f) {
    var groups = {};
    array.forEach(function (o) {
        if(o.handoverState == 0){ //排除掉交接人员
            var group = JSON.stringify(f(o));
            groups[group] = groups[group] || [];
            groups[group].push(o);
        }
    });
    return Object.keys(groups).map(function (group) {
        return groups[group];
    });
}