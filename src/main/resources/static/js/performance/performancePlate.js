$(function () {
    // 单选框、多选框美化；
    $(".i-checks").iCheck({
        checkboxClass: "icheckbox_square-green",
        radioClass: "iradio_square-green",
    });

    // 默认显示列表页面；
    $("#listElement").removeClass("col-sm-8");
    $("#listElement").addClass("col-sm-12");
    $("#formElement").hide();

    // 先隐藏上级的选择；
    changeLevel();

    // 切换层级；
    $("#plateLevel").find(".i-checks").on("ifClicked", function () {
        var level = $(this).find("input").val();
        changeLevel(level);
    });

    // 查询表单增加校验；
    $("#queryForm").validate();

    // 录入表单增加校验；
    $("#plateForm").validate();

    // 回车执行查询；
    $("body").keydown(function (evt) {
        evt = (evt) ? evt : ((window.event) ? window.event : "");
        var curKey = evt.keyCode ? evt.keyCode : evt.which;
        if (curKey == 13) {
            $("#dataSearch").click();
        }
    });

    // 查询按钮；
    $("#dataSearch").click(function () {
        if ($("#queryForm").valid()) {
            buildTree();
        }
    });

    // 构建树；
    buildTree();
});

// 切换上级显示；
function changeLevel(level) {
    if (level == undefined) {
        level = 0;
    }
    // 板块没有上级；
    if (level == 0) {
        $("#plateForm").find(".plateParent").hide();
    } else {
        $("#plateForm").find(".plateParent").show();
    }

    // 剩余分值；
    if (level == 2) {
        $("#scoreLeft").hide();
    } else {
        $("#scoreLeft").show();
    }

    loadParentData(level);
}

// 显示上级的项目；
function loadParentData(level) {
    // 清空下拉框；
    var plateObj = $("#plateForm select[name='plateParent']");
    plateObj.empty();
    // 清空选中的内容；
    var plateDataObj = $("#plateForm textarea[data='plateContent']");
    plateDataObj.val("");

    if (level != 0) {
        layui.use(["form"], function () {
            $.post(baseUrl + "/performancePlate/listPlates", {
                plateId: $("#plateForm input[name='plateId']").val(),
                plateLevel: level - 1
            }, function (data) {
                var dataValue = data.data.plates;
                if (dataValue == null) {
                    getResCode(data);
                } else {
                    if (dataValue.length > 0) {
                        var oldParent = $("#plateParent").val();
                        oldParent = oldParent.length > 0 ? oldParent : dataValue[0].plateId;
                        var oldPlateData = dataValue[0].plateContent;
                        var plateContent;
                        var plateDesc;
                        var hasData = false;
                        for (var i = 0; i < dataValue.length; i++) {
                            plateContent = dataValue[i].plateContent;
                            plateDesc = plateContent;
                            // 显示优化；
                            if (plateContent.length > 10) {
                                plateContent = plateContent.substring(0, 10);
                            }
                            if (oldParent == dataValue[i].plateId) {
                                hasData = true;
                                oldPlateData = plateDesc;
                                plateObj.append("<option data='" + plateDesc + "' value='" + dataValue[i].plateId + "' selected='selected'>" + plateContent + "</option>");
                            } else {
                                plateObj.append("<option data='" + plateDesc + "' value='" + dataValue[i].plateId + "'>" + plateContent + "</option>");
                            }
                        }
                        plateDataObj.val(oldPlateData);

                        // 项目；
                        if (level == 1) {
                            if (hasData) {
                                loadLeftScore(level, oldParent);
                            } else {
                                loadLeftScore(level, dataValue[0].plateId.toString());
                            }
                        }
                    }
                    // 初始化；
                    layui.form.render();

                    // 下拉框的onchange事件；
                    layui.form.on("select(plateParent)", function (plateData) {
                        // 更新隐藏域；
                        plateDataObj.val(getLayUISelectText(plateData, "data"));

                        if (level == 1) {
                            // 加载数据；
                            loadLeftScore(level, plateData.value);
                        }
                    });
                }
            }, "json");
        });
    } else {
        // 板块；
        loadLeftScore(level);
    }
}

// 获取下属的权重累计分值；
function loadLeftScore(level, plateId) {
    // 清空剩余分值；
    $("#plateProportion").val("");
    $("#plateForm input[data='scoreLeft']").val("");
    // 顶级菜单使用自己的ID；
    if (level == 0) {
        plateId = $("#plateForm input[name='plateId']").val();
    }
    if (plateId.length > 0) {
        var params = {plateParent: plateId};
        if (level == 1) {
            params["plateId"] = $("#plateForm input[name='plateId']").val();
        }
        $.post(baseUrl + "/performancePlate/getTotal", params, function (data) {
            var dataValue = data.data.total;
            if (dataValue == null) {
                getResCode(data);
            } else {
                var reg = /^-?(?:\d+|\d{1,3}(?:,\d{3})+)?(?:\.\d+)?$/;
                var currentScore = $("#plateForm input[name='plateProportion']").val();
                if (reg.test(dataValue) && reg.test(currentScore)) {
                    $("#plateProportion").val(dataValue);
                    // 计算分值；
                    if (currentScore.length > 0 && level == 1) {
                        $("#plateForm input[data='scoreLeft']").val(parseFloat(dataValue) - parseFloat(currentScore));
                    } else {
                        $("#plateForm input[data='scoreLeft']").val(dataValue);
                    }
                }
            }
        }, "json");
    }
}

// 显示增加表单；
function addPlate(obj, plateId, plateLevel) {
    if ($("#formElement").is(":hidden")) {
        // 显示表单；
        $("#listElement").removeClass("col-sm-12");
        $("#listElement").addClass("col-sm-8");
        $("#formElement").show();
    }

    // 清空表单；
    cleanValidate($("#plateForm"));

    // 设置选中；
    var element = $(obj).prevAll("span.tip-right");
    setCurrentNode(element);

    // 设置单选框；
    plateLevel = parseInt(plateLevel) + 1
    setFormRadioChecked($("#plateForm input[name='plateLevel']"), plateLevel);

    // 设置上级ID；
    $("#plateParent").val(plateId);

    // 显示输入框
    changeLevel(plateLevel);
}

// 显示编辑表单；
function editPlate(obj) {
    alertMessage("系统处理中，请稍候。");
    if ($("#formElement").is(":hidden")) {
        // 显示表单；
        $("#listElement").removeClass("col-sm-12");
        $("#listElement").addClass("col-sm-8");
        $("#formElement").show();
    }

    // 清空表单；
    cleanValidate($("#plateForm"));

    // 获取数据；
    var oldData = JSON.parse($(obj).prev("input").val());
    for (var key in oldData) {
        $("#plateForm").find("input[type='text'][name='" + key + "']").val(oldData[key]);
        $("#plateForm").find("input[type='hidden'][name='" + key + "']").val(oldData[key]);
        $("#plateForm").find("textarea[name='" + key + "']").val(oldData[key]);
    }

    // 设置选中；
    var element = $(obj).prevAll("span.tip-right");
    setCurrentNode(element);

    // 设置单选框；
    var plateLevel = oldData["plateLevel"];
    setFormRadioChecked($("#plateForm input[name='plateLevel']"), plateLevel);

    // 设置上级ID；
    $("#plateParent").val(oldData["plateParent"]);

    // 显示输入框
    changeLevel(plateLevel);

    // 设置原来的级别；
    $("#oldLevel").val(plateLevel);

    // 设置原来的分值；
    $("#oldProportion").val(oldData.plateProportion);
}

// 删除数据；
function deletePlate(obj, plateId, plateParent) {
    // 设置选中；
    var element = $(obj).prevAll("span.tip-right");
    setCurrentNode(element);

    // 隐藏表单；
    $("#listElement").removeClass("col-sm-8");
    $("#listElement").addClass("col-sm-12");
    $("#formElement").hide();

    layer.confirm("此操作无法恢复，确认继续吗？", {
        btn: ["确认", "取消"],
        shade: [0.7, '#393D49']
    }, function (index) {
        layer.close(index);
        alertMessage("系统处理中，请稍候。");
        $.post(baseUrl + "/performancePlate/deletePlate", {plateId: plateId}, function (data) {
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                // 删除的是顶级，直接刷新；
                if (plateParent == undefined || plateParent.length == 0 || plateParent == 0) {
                    buildTree();
                } else {
                    // 删除的不是顶级则刷新指定节点；
                    var obj = $("#plateTree span[data='" + plateParent + "']");
                    buildTree(obj);
                }
                alertMessage(message);
            }
        }, "json");
    });
}

// 根据输入的分值计算剩余权重；
function checkScore(obj) {
    var reg = /^-?(?:\d+|\d{1,3}(?:,\d{3})+)?(?:\.\d+)?$/;
    var leftScore = $("#plateProportion").val();
    var currentScore = $(obj).val();
    if (reg.test(leftScore) && reg.test(currentScore)) {
        var level = $("#plateForm input[name='plateLevel']:checked").val();
        if (level == 0) {
            var oldProportion = $("#oldProportion").val();
            if (reg.test(oldProportion)) {
                $("#plateForm input[data='scoreLeft']").val(parseFloat(leftScore) + (parseFloat(currentScore) - parseFloat(oldProportion)));
            }
        }
        if (level == 1) {
            $("#plateForm input[data='scoreLeft']").val(parseFloat(leftScore) - parseFloat(currentScore));
        }
    }
}

// 保存数据；
function savePlateData() {
    if ($("#plateForm").valid()) {
        // 校验权重；
        var dataValid = true;
        var scoreLeft = $("#plateForm input[data='scoreLeft']");
        if (scoreLeft.is(":visible")) {
            var scoreLeftValue = scoreLeft.val();
            if (scoreLeftValue.length <= 0 || parseFloat(scoreLeftValue) < 0) {
                dataValid = false;
            }
        }
        if (dataValid) {
            alertMessage("系统处理中，请稍候。");
            startModal("#savePlate");
            var params = $("#plateForm").serializeJson();
            $.post(baseUrl + "/performancePlate/savePlate", params, function (data) {
                Ladda.stopAll();
                var plate = data.data.plate;
                if (plate == null) {
                    alertMessage(data.msg);
                } else {
                    // 如果是编辑；
                    var plateId = $("#plateForm input[name='plateId']").val();
                    // 获取父级ID；
                    var plateParent = params["plateParent"];
                    if (plateId.length > 0) {
                        // 如果原来是顶级菜单或修改后的是顶级，直接刷新；
                        var oldParentId = $("#plateParent").val();
                        if (oldParentId == undefined || oldParentId.length == 0 || oldParentId == 0 || plateParent == undefined || plateParent.length == 0 || plateParent == 0) {
                            buildTree();
                        } else {
                            // 判断是往上还是往下；
                            var oldLevel = parseInt($("#oldLevel").val());
                            var plateLevel = parseInt(params["plateLevel"]);
                            // 往下修改；
                            if (oldLevel > plateLevel) {
                                // 先更新原来的上级再更新现在的上级；
                                var obj = $("#plateTree span[data='" + oldParentId + "']");
                                buildTree(obj, function () {
                                    // 刷新修改后的节点；
                                    obj = $("#plateTree span[data='" + plateParent + "']");
                                    buildTree(obj);
                                });
                                // 往上或平级修改；
                            } else {
                                // 先更新现在的上级再更新原有的上级；
                                var obj = $("#plateTree span[data='" + plateParent + "']");
                                buildTree(obj, function () {
                                    // 如果有变动；
                                    if (plateParent != oldParentId) {
                                        // 刷新修改前的节点；
                                        obj = $("#plateTree span[data='" + oldParentId + "']");
                                        buildTree(obj);
                                    }
                                });
                            }
                        }
                        // 新增；
                    } else {
                        // 更新上级；
                        // 增加的是顶级，直接刷新；
                        if (plateParent == undefined || plateParent.length == 0 || plateParent == 0) {
                            buildTree();
                        } else {
                            // 增加的不是顶级则刷新指定节点；
                            var obj = $("#plateTree span[data='" + plateParent + "']");
                            buildTree(obj);
                        }

                        // 更新ID；
                        $("#plateForm input[name='plateId']").val(plate.plateId);
                        // 更新级别；
                        $("#oldLevel").val(plate.plateLevel);
                        // 更新上级；
                        $("#plateParent").val(plate.plateParent);
                        // 更新分值；
                        $("#oldProportion").val(plate.plateProportion);
                        // 更新权重；
                        loadLeftScore(plate.plateLevel, plate.plateParent);
                    }

                    alertMessage("操作完成。");
                }
            }, "json").error(function () {
                Ladda.stopAll();//隐藏加载按钮
            });
        } else {
            alertMessage("当前权重已超出给定值，请检查。");
        }
    }
}

// 构建树；
function buildTree(obj, callbackFunction) {
    var map = {};
    var icon = "";
    if (obj) {
        $(obj).attr("init", 1);
        // 先清空下级；
        $(obj).parent().find("ul").remove();
        $(obj).parent().append("<ul><li><img title='loader.gif' src='/js/performance/tree/img/loader.gif' alt='图片丢失'></li></ul>");
        map = {"plateParent": $(obj).attr("data")};
        icon = "icon-plus-sign";
    } else {
        icon = "icon-folder-close";
        // 搜索点击；
        if ($("#queryForm").valid()) {
            map.plateContent = $("#queryForm input[name='plateContent']").val();
        }
        if (map.plateContent.length == 0) {
            map.plateContent = undefined;
            map.plateLevel = 0;
        }
    }
    // 获取数据；
    $.post(baseUrl + "/performancePlate/listPlates", map, function (data) {
        var plates = data.data.plates;
        if (plates == null) {
            getResCode(data);
        } else {
            if (plates.length > 0) {
                var treeContent = "<ul>";
                var length = plates.length;
                var plateId;
                var plateContent;
                var plateDesc;
                var plateLevel;
                for (var i = 0; i < length; i++) {
                    plateId = plates[i].plateId;
                    plateContent = plates[i].plateContent;
                    // 提示信息；
                    plateDesc = plateContent;

                    // 显示优化；
                    if (plateContent.length > 20) {
                        plateContent = plateContent.substring(0, 20);
                    }

                    plateContent += "（";
                    plateContent += plates[i].plateProportion;
                    plateLevel = plates[i].plateLevel;
                    if (plateLevel != 2) {
                        plateContent += "%";
                    }
                    plateContent += "）";

                    treeContent += "<li class='parent_li'><span data='";
                    treeContent += plateId;
                    treeContent += "' init='0' onselectstart='return false;' style='-moz-user-select:none;' onclick='glorifyTree(this);' class='";
                    if (obj) {
                        if (i % 4 == 0) {
                            treeContent += "badge badge-info ";
                        } else if (i % 4 == 1) {
                            treeContent += "badge badge-success ";
                        } else if (i % 4 == 2) {
                            treeContent += "badge badge-warning ";
                        } else {
                            treeContent += "badge badge-important ";
                        }
                    }
                    treeContent += "tip-right' title='";
                    treeContent += plateDesc;
                    treeContent += "'><i class='";
                    treeContent += icon;
                    treeContent += "'></i>";
                    treeContent += plateContent;
                    treeContent += "</span>";
                    treeContent += buildButton(plates[i]);
                    treeContent += "</li>";
                }
                treeContent += "</ul>";
                if (obj) {
                    // 追加子节点并更新图标；
                    $(obj).parent().find("img").closest("ul").replaceWith(treeContent);
                    var liElement = $(obj).find(" > i");
                    if (liElement.hasClass("icon-folder-close")) {
                        liElement.removeClass("icon-folder-close").addClass("icon-folder-open");
                    } else {
                        liElement.removeClass("icon-plus-sign").removeClass("icon-leaf").addClass("icon-minus-sign");
                    }
                } else {
                    $("#plateTree").html(treeContent);
                }
                $(".tip-right").tooltip({placement: "right"});
                // 更新悬停；
                treeHover();
            } else {
                // 没有下级则设置为叶子；
                if (obj) {
                    $(obj).parent().find("img").closest("ul").remove();
                    var liElement = $(obj).find(" > i");
                    if (liElement.hasClass("icon-folder-close")) {
                        liElement.removeClass("icon-folder-close").addClass("icon-leaf");
                    } else {
                        liElement.removeClass("icon-plus-sign").addClass("icon-leaf");
                    }
                } else {
                    $("#plateTree").html("<div style='text-align:center;'>查询无结果</div>");

                    // 清空表单；
                    cleanValidate($("#plateForm"));

                    // 显示表单；
                    $("#listElement").removeClass("col-sm-12");
                    $("#listElement").addClass("col-sm-8");
                    $("#formElement").show();
                }
            }
        }

        // 如果有回调；
        if (callbackFunction) {
            callbackFunction();
        }
    }, "json");
}

// 设置按钮；
function buildButton(plate) {
    var treeContent = "";
    var plateId = plate.plateId;

    // 非底层允许增加；
    if (plate.plateLevel != 2) {
        treeContent += "&nbsp;<span class='badge badge-primary' onclick=";
        treeContent += '"addPlate(this,';
        treeContent += "'";
        treeContent += plateId;
        treeContent += "','";
        treeContent += plate.plateLevel;
        treeContent += "');";
        treeContent += '"';
        treeContent += ">增加</span>";
    }

    treeContent += "&nbsp;<input type='hidden' value='";
    treeContent += JSON.stringify(plate);
    treeContent += "'/>";

    treeContent += "<span class='badge badge-warning' onclick=";
    treeContent += '"editPlate(this);"';
    treeContent += ">编辑</span>";

    treeContent += "&nbsp;<span class='badge badge-danger' onclick=";
    treeContent += '"deletePlate(this,';
    treeContent += "'";
    treeContent += plateId;
    treeContent += "','";
    treeContent += plate.plateParent;
    treeContent += "');";
    treeContent += '"';
    treeContent += ">删除</span>";

    return treeContent;
}

// 树增加样式和事件；
function glorifyTree(obj) {
    if ($(obj).attr("init") == 0) {
        buildTree(obj);
    } else {
        var liElement = $(obj).find(" > i");
        var children = $(obj).parent("li.parent_li").find(" > ul > li");
        if (children.length == 0 || children.is(":visible")) {
            children.hide();
            if (liElement.hasClass("icon-folder-open")) {
                liElement.removeClass("icon-folder-open").addClass("icon-folder-close");
            } else {
                liElement.removeClass("icon-minus-sign").addClass("icon-plus-sign");
            }
        } else {
            children.show();
            if (liElement.hasClass("icon-folder-close")) {
                liElement.removeClass("icon-folder-close").addClass("icon-folder-open");
            } else {
                liElement.removeClass("icon-plus-sign").addClass("icon-minus-sign");
            }
        }
    }
}

// 设置当前节点样式；
function setCurrentNode(obj) {
    var element = $("#plateTree").find(".current_node");
    if (element) {
        element.removeClass("current_node");
        if (element.hasClass("badge")) {
            element.css({"background": "", "border": "1px solid #999999", "color": "#000000"});
        } else {
            element.css({"background": "", "border": "1px solid #999999", "color": "#333333"});
        }
    }

    $(obj).addClass("current_node");
    $(obj).css({"background": "#4F4F4F", "border": "1px solid #94a0b4", "color": "#FFFFFF"});
}

// 鼠标悬停变色；
function treeHover() {
    $(".tree li.parent_li > span.tip-right").hover(function () {
        var treeNodes = $(this).parent("li.parent_li").find("span.tip-right");
        treeNodes.css({"background": "#4F4F4F", "border": "1px solid #94a0b4", "color": "#FFFFFF"});
    }, function () {
        var treeNodes = $(this).parent("li.parent_li").find("span.tip-right");
        treeNodes.each(function () {
            if (!$(this).hasClass("current_node")) {
                if ($(this).hasClass("badge")) {
                    $(this).css({"background": "", "border": "1px solid #999999", "color": "#000000"});
                } else {
                    $(this).css({"background": "", "border": "1px solid #999999", "color": "#333333"});
                }
            }
        });
    });
}