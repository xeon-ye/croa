$(function () {
    // 底部信息栏样式；
    $(".footer").css({"width": "100%", "bottom": "0px", "margin": 0, "position": "fixed"});

    // 模态框头部的底框线移除；
    $(".modal-header").css("border-bottom", "0px");

    // 修改查询条件表单的Lable右间距；
    $(".control-label").css({"padding": "7px 0 0 0", "margin-right": "-10px", "width": "80px"});

    // 模态框的按钮间距；
    $(".modal-footer .btn + .btn").css("margin-left", "15px");
});

/**
 * 关闭当前页面；
 */
function closePage() {
    var userAgent = navigator.userAgent;
    if (userAgent.indexOf("Firefox") != -1 || userAgent.indexOf("Chrome") != -1) {
        window.location.href = "about:blank";
    } else if (userAgent.indexOf('Android') > -1 || userAgent.indexOf('Linux') > -1) {
        window.opener = null;
        window.open('about:blank', '_self', '').close();
    } else {
        window.opener = null;
        window.open("about:blank", "_self");
    }
    window.close();
}

// 得到查询参数；
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]);
    return null;
}

// 提示信息增加遮罩层；
function alertMessage(content) {
    layer.msg(content, {time: 1000, shade: [0.7, '#393D49']});
}

// 审核记录查看；
function  showProcessHistory(dataId, processId) {
    $.post(baseUrl + "/process/history", {dataId: dataId, process: processId}, function (data) {
        $("#history").empty();
        if (data.data.data == null) {
            alertMessage(data.msg);
        } else {
            var html = "<div class='form-control'>";
            html += "<div class='col-sm-3 text-center'>审核节点</div>";
            html += "<div class='col-sm-3 text-center'>操作人</div>";
            html += "<div class='col-sm-3 text-center'>操作详情</div>";
            html += "<div class='col-sm-3 text-center'>操作时间</div></div>";
            for (var i = 0; i < data.data.data.length; i++) {
                html += "<div class='form-control' style='margin-top: -1px;'>";
                html += "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>";
                html += "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>";
                html += "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>";
                html += "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>";
                html += "</div>";
            }
            html += "<div class='col-sm-12 text-center' style='position:relative'>";
            html += "<img src='/process/getImage?dataId=" + dataId + "&process=" + processId + "&t=" + new Date().getTime() + "' style='width: 120%; margin-left: -40px; margin-bottom: -100px;'/></div>";
            $("#history").append(html);
            $("#historyModal").modal({backdrop: "static"});
        }
    }, "json");
}

// 提交审核意见；
function completeApprove(flag) {
    if ($("#approveForm").valid()) {
        var opinion = false;
        var elementId = "rejectEmploy";
        if (flag == 0) {
            elementId = "agreeEmploy";
            opinion = true;
        }
        // 最后一个节点使用默认即可；
        if ($("#approveForm .approveUser").is(":hidden") || flag==1) {
            approveTask($("#approveForm input[name='taskId']").val(), opinion, elementId, $("#approveForm textarea[name='desc']").val());
        } else {
            $("#approveForm input[name='agree']").val(opinion)
            startModal("#" + elementId);
            $.post(baseUrl + "/entryManage/completeApprove", {approveData: JSON.stringify($("#approveForm").serializeJson())}, function (data) {
                Ladda.stopAll();
                var message = data.data.message;
                if (message == null) {
                    alertMessage(data.msg);
                } else {
                    alertMessage(message);
                }
                $("#employModal").modal("hide");
                // 返回审核页面；
                setTimeout(function () {
                    refrechPage("/homePage");
                    closeCurrentTab();
                }, 2000);
            }, "json");
        }
    }
}

// 返回审核页面；
function returnApprove() {
    //获取当前tab栏目
    var currrentTab = $(".J_menuTab.active", window.top.document);
    //跳转审批任务页面
    page("/process/queryTask","审批任务");
    //关闭原审核页面
    if (currrentTab.length > 0) {
        currrentTab.find(".fa.fa-times-circle").click();
    }
    // window.location.href = '/process/queryTask';
}

// 清空表单校验；
function cleanValidate(targetForm) {
    targetForm[0].reset();
    // 隐藏域单独清空；
    targetForm.find("input[type='hidden']").val("");
    targetForm.find(".form-control").removeClass("success");
    targetForm.find(".form-control").removeClass("error");
    targetForm.find(".form-control + .error").remove();
}

// 清空标签内的校验；
function cleanElementValidate(targetForm) {
    targetForm.find("input[type='hidden']").val("");
    targetForm.find("input[type='text']").val("");
    targetForm.find(".form-control").removeClass("success");
    targetForm.find(".form-control").removeClass("error");
    targetForm.find(".form-control + .error").remove();
}

// 获取民族数据；
function loadNationData() {
    $.get(baseUrl + "/entry/getNation", function (data) {
        var dataValue = data.data.nation;
        if (dataValue != null) {
            if (dataValue.length > 0) {
                var nationObj = $("#queryForm select[data='raceId']");
                for (var i = 0; i < dataValue.length; i++) {
                    nationObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
                // 初始化；
                layui.form.render();
            }
        }
        // 初始化部门数据；
        loadDeptData();
    }, "json");
}

// 加载部门数据；
function loadDeptData() {
    // 初始化部门数据；
    $.get(baseUrl + "/entry/getDept", function (data) {
        var dataValue = data.data.dept;
        if (dataValue != null) {
            if (dataValue.length > 0) {
                var deptObj = $("#queryForm select[data='deptId']");
                for (var i = 0; i < dataValue.length; i++) {
                    deptObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
                // 初始化；
                layui.form.render();
            }
            // 初始化职位数据；
            loadPostData();
        }
    }, "json");
}

// 获取部门的职位数据；
function loadPostData() {
    $.post(baseUrl + "/entry/getPost", {deptId: null}, function (data) {
        var dataValue = data.data.post;
        if (dataValue != null) {
            if (dataValue.length > 0) {
                var postObj = $("#queryForm select[data='postId']");
                for (var i = 0; i < dataValue.length; i++) {
                    postObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
                // 初始化；
                layui.form.render();
            }
        }
    }, "json");
}

// 设置单选框；
function setFormRadioChecked(obj, inputValue) {
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

// 设置复选框；
function setFormCheckBoxChecked(obj, valueArray) {
    obj.each(function () {
        $(this).removeAttr("checked");
        // 移除ICheck样式；
        $(this).parent().removeClass("checked");
    });
    // 空值的处理；
    if (valueArray && valueArray != null) {
        var inputValue;
        for (var i = 0; i < valueArray.length; i++) {
            inputValue = valueArray[i];
            obj.each(function () {
                if ($(this).val() == inputValue.trim()) {
                    $(this).prop("checked", true);
                    // 移除ICheck样式；
                    $(this).parent().addClass("checked");
                }
            });
        }
    }
}

// 获取LayUI下拉框选中的文本；
function getLayUISelectText(selectData, name) {
    var options = selectData.elem.options;
    var selectedIndex = selectData.elem.selectedIndex;
    if (options && selectedIndex != undefined) {
        if (name) {
            return options[selectedIndex].attributes[name].value;
        } else {
            return options[selectedIndex].text;
        }
    } else {
        return "";
    }
}

// kpl绩效考核的表格合并；
function createTable(plate) {
    $("#scoreSelf").html("");
    $("#scoreLeader").html("");
    $("#totalValue").html("");
    if (plate) {
        var plateLength = plate.length;
        if (plateLength > 0) {
            var plateData;
            var plateHtml = "";
            var firstPlate = new Array();
            var secondPlate = new Array();
            // var thirdPlate = new Array();
            for (var i = 0; i < plateLength; i++) {
                plateData = plate[i];
                if (plateData.plateLevel == 0) {
                    firstPlate.push(plateData);
                }
                if (plateData.plateLevel == 1) {
                    secondPlate.push(plateData);
                }
            }
            var firstId;
            var secondId;
            var num;
            //计算自评总分
            var totalSelfScore = 0;
            // 计算上级总分
            var totalLeaderScore = 0;
            //权重分数
            var totalProportionScore = 0;
            var rowspanMap = {};
            for (var i = 0; i < firstPlate.length; i++) {
                firstId = firstPlate[i].plateId;
                num = 0;
                for (var j = 0; j < secondPlate.length; j++) {
                    secondId = secondPlate[j].plateId;
                    if (firstId == secondPlate[j].plateParent) {
                        if (num > 0) {
                            plateHtml += "</tr><tr class='detail'>";
                        }else{
                            plateHtml += "<tr class='detail'><td width='10%' id='plate" + firstId + "'>"+firstPlate[i].plateContent+"</td>";
                        }
                        plateHtml += "<td id='plate" + secondId + "' width='15%'>" +secondPlate[j].plateTarget +"</td>" +
                            "<td id='plate" + secondId + "' width='15%'>" +secondPlate[j].plateDemand +"</td>" +
                            "<td id='plate" + secondId + "' colspan='2' width='20%'>" +secondPlate[j].plateContent +"</td>" +
                            "<td id='plate" + secondId + "' width='7%'>" +
                            "<input type='hidden' name='scoreKey' value='" + secondPlate[j].plateId + "'/>"+secondPlate[j].plateProportion +
                            "</td>" ;
                        var secondPlateScore = 0;
                        if (secondPlate[j].scoreSelf || secondPlate[j].scoreSelf == 0) {
                            secondPlateScore += (secondPlate[j].scoreSelf * $("#proportionSelf").val() / 100);
                            totalSelfScore += secondPlate[j].scoreSelf;
                            plateHtml += "</td><td width='7%' class='self plate" + secondId + "'><input type='text' autocomplete='off' data-value='"+secondPlate[j].plateProportion+"' onblur='changeTdStyle(this)' onkeyup=\"value=value.replace(/[^\\d\\.]/g,'')\" class='scoreValue' data='scoreSelf' name='plate" + secondId + "' value='" + secondPlate[j].scoreSelf + "'/>";
                        } else {
                            totalSelfScore += secondPlate[j].scoreSelf;
                            plateHtml += "</td><td width='7%' class='self plate" + secondId + "'><input type='text' autocomplete='off' data-value='"+secondPlate[j].plateProportion+"' onblur='changeTdStyle(this)' onkeyup=\"value=value.replace(/[^\\d\\.]/g,'')\"  class='scoreValue' data='scoreSelf' name='plate" + secondId + "'/>";
                        }
                        if (secondPlate[j].scoreLeader || secondPlate[j].scoreLeader == 0) {
                            secondPlateScore += (secondPlate[j].scoreLeader * $("#proportionLeader").val() / 100);
                            totalLeaderScore += secondPlate[j].scoreLeader;
                            plateHtml += "</td><td width='7%' class='leader plate" + secondId + "'><input type='text' class='scoreValue' data='scoreLeader' autocomplete='off' data-value='"+secondPlate[j].plateProportion+"' onblur='changeTdStyle(this)' onkeyup=\"value=value.replace(/[^\\d\\.]/g,'')\" name='plate" + secondId + "' value='" + secondPlate[j].scoreLeader + "'/>";
                        } else {
                            totalLeaderScore += secondPlate[j].scoreLeader;
                            plateHtml += "</td><td width='7%' class='leader plate" + secondId + "'><input type='text' class='scoreValue' data='scoreLeader' autocomplete='off' data-value='"+secondPlate[j].plateProportion+"' onblur='changeTdStyle(this)' onkeyup=\"value=value.replace(/[^\\d\\.]/g,'')\" name='plate" + secondId + "'/>";
                        }
                        if(secondPlate[j].scoreTotal){
                            totalProportionScore += parseFloat(secondPlate[j].scoreTotal);
                        }
                        plateHtml += "</td><td width='7%' class='plate" + secondId + "'><input type='text' autocomplete='off' value='"+(secondPlate[j].scoreTotal==undefined?"":secondPlate[j].scoreTotal)+"' class='totalValue' data='scoreTotal' name='total" + secondId + "' readonly/></td>"+
                            "<td width='10%' class='remark plate" + secondId + "'><textarea cols='16' rows='3' type='text' autocomplete='off' class='remark' data='remark' name='remark" + secondId + "'>"+(secondPlate[j].remark==undefined?"":secondPlate[j].remark)+"</textarea>";
                        num++;
                    }
                }
                plateHtml += "</tr>";
                if (num > 1) {
                    rowspanMap["plate" + firstId] = num;
                }
            }
            $("#scoreContent").after(plateHtml);
            if(totalSelfScore!=null && totalSelfScore!=""){
                if((totalSelfScore+"").indexOf(".")!=-1){
                    totalSelfScore=totalSelfScore.toFixed(2);
                }
            }
            $("#scoreSelf").html(totalSelfScore||"");
            if(totalLeaderScore!=null && totalLeaderScore!=""){
                if((totalLeaderScore+"").indexOf(".")!=-1) {
                    totalLeaderScore = totalLeaderScore.toFixed(2);
                }
            }
            $("#scoreLeader").html(totalLeaderScore||"");
            if(totalProportionScore!=null && totalProportionScore!=""){
                if((totalProportionScore+"").indexOf(".")!=-1) {
                    totalProportionScore = totalProportionScore.toFixed(2);
                }
            }
            $("#totalValue").html(totalProportionScore||"");
            // 合并
            for (var key in rowspanMap) {
                $("#" + key).attr("rowspan", rowspanMap[key]);
            }
            $("#scoreDetailTable tr td").each(function () {
                var spanElement = $(this).find("input");
                if (spanElement && spanElement.length > 0 && (spanElement.hasClass("scoreValue") || spanElement.hasClass("totalValue"))) {
                    spanElement.css({
                        "width": "100%",
                        "height": rowspanMap[key] * 30 + "px",
                        "border": "1px",
                        "background-color": "white",
                        "text-align": "center"
                    });
                }
                var textAreaElement = $(this).find("textarea");
                if (textAreaElement && textAreaElement.length > 0 && (textAreaElement.hasClass("remark"))) {
                    textAreaElement.css({
                        "width": "100%",
                        "height": "100%",
                        "border": "1px",
                        "padding": "0 5px",
                        "background-color": "white",
                    });
                }
            });
        }
    }
}

// okr绩效考核的表格合并；
function createOKRTable(plate) {
    $("#scoreSelf").html("");
    $("#scoreLeader").html("");
    $("#totalValue").html("");
    if (plate) {
        var plateLength = plate.length;
        if (plateLength > 0) {
            var plateData;
            var plateHtml = "";
            var firstPlate = new Array();
            for (var i = 0; i < plateLength; i++) {
                plateData = plate[i];
                if (plateData.plateLevel == 0) {
                    firstPlate.push(plateData);
                }
            }
            var firstId;
            //计算自评总分
            var totalSelfScore = 0;
            // 计算上级总分
            var totalLeaderScore = 0;
            //权重分数
            var totalProportionScore = 0;
            for (var i = 0; i < firstPlate.length; i++) {
                firstId = firstPlate[i].plateId;
                var target = +firstPlate[i].plateTarget;
                plateHtml += "<tr class='detail'><td width='26%' colspan='2' id='plate" + firstId + "'>" + firstPlate[i].plateContent + "</td>"+
                    "<td id='plate" + firstId + "' width='10%'>" +
                    "<input type='hidden' name='scoreKey' value='" + firstPlate[i].plateId + "'/>" + firstPlate[i].plateProportion +
                    "</td>"+
                    "<td id='plate" + firstId + "' width='10%' colspan='2'>" +
                    "<div style='padding: 0px' class='layui-form'>"+
                    "<input type='radio' name='plateTarget" + firstId + "' data='plateTarget' value='0' "+(target==0?"checked=checked":"")+" lay-filter='isChecked' title='是'/>" +
                    "<input type='radio' name='plateTarget" + firstId + "' data='plateTarget' value='1' "+(target==1?"checked=checked":"")+" lay-filter='isChecked' title='否'/>" +
                    "</div></td>" +
                    "<td width='15%' colspan='2'><textarea style='width: 100%;height: 100%;' cols='16' rows='3' type='text' autocomplete='off' class='plateDemand' data='plateDemand' name='plateDemand" + firstId + "'>"+(firstPlate[i].plateDemand==undefined?"":firstPlate[i].plateDemand)+"</textarea></td>" ;
                var secondPlateScore = 0;
                if (firstPlate[i].scoreSelf || firstPlate[i].scoreSelf == 0) {
                    secondPlateScore += (firstPlate[i].scoreSelf * $("#proportionSelf").val() / 100);
                    totalSelfScore += firstPlate[i].scoreSelf;
                    plateHtml += "</td><td width='10%' class='self plate" + firstId + "'><input type='text' autocomplete='off' data-value='" + firstPlate[i].plateProportion + "' onblur='changeTdStyle(this)' onkeyup=\"value=value.replace(/[^\\d\\.]/g,'')\" class='scoreValue' data='scoreSelf' name='plate" + firstId + "' value='" + firstPlate[i].scoreSelf + "'/>";
                } else {
                    totalSelfScore += firstPlate[i].scoreSelf;
                    plateHtml += "</td><td width='10%' class='self plate" + firstId + "'><input type='text' autocomplete='off' data-value='" + firstPlate[i].plateProportion + "' onblur='changeTdStyle(this)' onkeyup=\"value=value.replace(/[^\\d\\.]/g,'')\"  class='scoreValue' data='scoreSelf' name='plate" + firstId + "'/>";
                }
                if (firstPlate[i].scoreLeader || firstPlate[i].scoreLeader == 0) {
                    secondPlateScore += (firstPlate[i].scoreLeader * $("#proportionLeader").val() / 100);
                    totalLeaderScore += firstPlate[i].scoreLeader;
                    plateHtml += "</td><td width='10%' class='leader plate" + firstId + "'><input type='text' class='scoreValue' data='scoreLeader' autocomplete='off' data-value='" + firstPlate[i].plateProportion + "' onblur='changeTdStyle(this)' onkeyup=\"value=value.replace(/[^\\d\\.]/g,'')\" name='plate" + firstId + "' value='" + firstPlate[i].scoreLeader + "'/>";
                } else {
                    totalLeaderScore += firstPlate[i].scoreLeader;
                    plateHtml += "</td><td width='10%' class='leader plate" + firstId + "'><input type='text' class='scoreValue' data='scoreLeader' autocomplete='off' data-value='" + firstPlate[i].plateProportion + "' onblur='changeTdStyle(this)' onkeyup=\"value=value.replace(/[^\\d\\.]/g,'')\" name='plate" + firstId + "'/>";
                }
                if (firstPlate[i].scoreTotal) {
                    totalProportionScore += parseFloat(firstPlate[i].scoreTotal);
                }
                plateHtml += "</td><td width='10%' class='plate" + firstId + "'><input type='text' autocomplete='off' value='" + (firstPlate[i].scoreTotal == undefined ? "" : firstPlate[i].scoreTotal) + "' class='totalValue' data='scoreTotal' name='total" + firstId + "' readonly/></td>" ;
                plateHtml += "</tr>";
            }
            $("#okrContent").after(plateHtml);
            if(totalSelfScore!=null && totalSelfScore!=""){
                if((totalSelfScore+"").indexOf(".")!=-1){
                    totalSelfScore=totalSelfScore.toFixed(2);
                }
            }
            $("#scoreSelf").html(totalSelfScore||"");
            if(totalLeaderScore!=null && totalLeaderScore!=""){
                if((totalLeaderScore+"").indexOf(".")!=-1) {
                    totalLeaderScore = totalLeaderScore.toFixed(2);
                }
            }
            $("#scoreLeader").html(totalLeaderScore||"");
            if(totalProportionScore!=null && totalProportionScore!=""){
                if((totalProportionScore+"").indexOf(".")!=-1) {
                    totalProportionScore = totalProportionScore.toFixed(2);
                }
            }
            $("#totalValue").html(totalProportionScore||"");
            $("#scoreDetailTable tr td").each(function () {
                var spanElement = $(this).find("input");
                if (spanElement && spanElement.length > 0 && (spanElement.hasClass("scoreValue") || spanElement.hasClass("totalValue"))) {
                    spanElement.css({
                        "width": "100%",
                        "height": "60px",
                        "border": "1px",
                        "background-color": "white",
                        "text-align": "center"
                    });
                }
                var textAreaElement = $(this).find("textarea");
                if (textAreaElement && textAreaElement.length > 0 && (textAreaElement.hasClass("remark") || textAreaElement.hasClass("plateDemand"))) {
                    textAreaElement.css({
                        "width": "100%",
                        "height": "100%",
                        "border": "1px",
                        "padding": "0 5px",
                        "background-color": "white",
                    });
                }
            });
            $('.i-checks').iCheck({
                checkboxClass: 'icheckbox_square-green',
                radioClass: 'iradio_square-green',
            });
        }
    }
}