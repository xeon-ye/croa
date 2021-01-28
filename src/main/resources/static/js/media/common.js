function initCompany() {
    //iframe中的window对象
    companyWindow = $('#companyFrame').prop('contentWindow');
}

$(function () {
    $("#mediaName").keydown(function (evt) {
        evt = (evt) ? evt : ((window.event) ? window.event : "");
        var curKey = evt.keyCode ? evt.keyCode : evt.which;
        if (curKey == 13) {//keyCode=13是回车键
            queryMedia();
        }
    });

});

/**
 * 弹出编辑窗口
 */
function alertEdit(url, title) {
    if (title == "新增媒体供应商") {
        parent.layer.open({
            type: 2,
            title: title,
            shadeClose: false,
            shade: 0.8,
            area: ['80%', '80%'],
            content: url, //iframe的url
            end: function () {
                $("#supplierId").reload();
                // if(title.indexOf("对接人")!=-1){
                //     dockingGrid.reflush();
                // }
                // if(title.indexOf("产品")!=-1){
                //     productGrid.reflush();
                // }
                // if(title.indexOf("用户")!=-1){
                //     usersGrid.reflush();
                // }
            }
        });
    } else {
        parent.layer.open({
            type: 2,
            title: title,
            shadeClose: false,
            shade: 0.8,
            area: ['80%', '80%'],
            content: url, //iframe的url
            end: function () {
            }
        });
    }
}

//各板块标题信息
var mediaForm = new Array();

/**
 * 根据媒体板块id查询媒体条件
 * @param id 媒体板块id
 * @param t
 */
function loadTerm(id, t) {
    $("#mediaTypeText").text($(t).text());
    $("#mediaTypeVal").val(id);

    if ($(t).hasClass("text-danger")) {
        return;
    }
    //weixin
    if(id === 1){
       $("#wxIdSearch").show();
       $("#wxId").val($("#wxIdSearch").val());
    }else{
        $("#wxId").val("");
        $("#wxIdSearch").hide();

    }
    // 设置类型ID；
    $("#mediaType").val(id);
    // 清空查询条件；
    $("#state").nextAll().remove();
    $("#mediaTypeUL>li>div>span").each(function (i, item) {
        $(item).removeClass("text-danger");
        if (t == item) {
            $(t).addClass("text-danger");
        }
    });

    //根据媒体板块id查询媒体条件
    $.get(baseUrl + "/mediaTerm/" + id, function (data) {
        var html = "";
        $(data).each(function (i, item) {
            switch (item.type) {
                case 0://直接展现
                    var datas = item.datas;
                    html += "<li class='col-md-12'><label style='float:left;'>" + item.termName + "：</label><div class='col-md-11'>";
                    if (datas != null) {
                        $(datas).each(function (j, it) {
                            html += "<span class='col-md-1' title='" + it.t + "' onclick='loadMedia(\"" + it.k + "\",\"" + it.v + "\",this)'>" + it.t + "</span>"
                            if ((j + 1) % 12 == 0)
                                html += "</div><label class='col-md-1'></label><div class='col-md-11'>"
                        })
                    }
                    html += "</div></li>";
                    break;
                case 1://单选框类型
                    html += "<li class='col-md-12'><label style='float:left;'>" + item.termName + "：</label><div class='col-md-11'>";
                    switch (item.dataType) {//数据类型：0SQL,1JSON,2HTML
                        case 1:
                            var json = eval(item.json);
                            if (Array.isArray(json))
                                $.each(json, function (i, item) {
                                    var text = item.hasOwnProperty("text") ? item.text : item.value;
                                    html += "<span class='col-md-1 radio-inline i-checks'><input type='radio' name='" + item.name + "' value='" + item.value + "' /> " + text + "</span>";
                                });
                            break;
                        case 2:
                            html += item.html.th('name', item.name);
                            break;
                    }
                    html += "</div></li>";
                    break;
                case 2://复选框类型
                    var json = eval(item.json);
                    html += "<li class='col-md-12'><label style='float:left;'>" + item.termName + "：</label><div class='col-md-11'>";
                    if (Array.isArray(json))
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            html += "<span class='col-md-1 checkbox-inline i-checks'><input type='checkbox'  name='" + item.name + "' value='" + item.value + "'/> " + text + "</span>";
                        });
                    html += "</div></li>";
                    break;
                case 5://时间
                    html += "<li class='col-md-12'><label style='float:left;'>" + item.termName + "：</label><div class='col-md-11'>";
                    html += item.html.th('name', item.name);
                    html += "</div></li>";
                    break;
                default://文本框
                    html += "<li class='col-md-12'><label style='float:left;'>" + item.termName + "：</label><div class='col-md-11'>";
                    html += item.html.th('name', item.name);
                    html += "</div></li>";
                    break;

            }
            if (item.name != null)
                html = html.th('name', item.name);
        });

        $("#mediaTermUL").html(html);
        // 调整样式为居左对齐；
        $(".col-md-12 > label").css({"text-align": "left", "width": "120px", "margin": "0px", "padding": "0px"});
        $(".col-md-11 > span").css({"text-align": "left"});
        $(".col-md-2.form-inline").css("width", "330px");

        $("#mediaTermUL select").each(function () {
            if ($(this).attr("onload")) {
                this.onload();
            }
            $(this).change(function (i, sel) {
                loadMedia($(this).attr("name"), $(this).val(), sel);
            });
            $(this).css({"width": "160px"});
        });

        $("#mediaTermUL").find('.i-checks').on('ifClicked', function (event) {
            var input = $(this).find(" input");
            loadMedia(input.attr("name"), input.val(), input);
        });
        //加载板块标题名称列表
        $.get(baseUrl + "/mediaForm/list/" + id, function (data) {
            // 清空；
            mediaForm = new Array();
            $(data).each(function (i, d) {
                mediaForm[d.code] = d.name;
                // console.log(d.code+"=="+d.name);
            });
            reData();
        }, "json");

    }, "json");
}

function loadMedia(k, v, t, max) {
    var target = $(t);
    // 重复点击视为取消；
    // 文字点击；
    if (target.hasClass("text-danger")) {
        target.removeClass("text-danger");
        // 先删除原有的同name条件；
        $("#termForm > input[name='" + k + "']").remove();
        $("#termForm > input[name='" + k + "_max']").remove();

        reData();
        return;
    }
    // 多选框和单选框；
    if (target.prop(("tagName")) == "INPUT" && target.parent().hasClass("checked")) {
        // 多选框要重载数据；
        if (target.attr("type") == "checkbox") {
            // 先删除原有的同name条件；
            $("#termForm > input[name='" + k + "'][value='" + v + "']").remove();
            $("#termForm > input[name='" + k + "_max'][value='" + max + "']").remove();
            reData();
        }
        return;
    }
    // 必须有值才进行刷新；
    if (v || max) {
        // 文字点击；
        if (target.hasClass("col-md-1")) {
            // 移除同类型的选中的样式；
            target.closest("li").find(".text-danger").each(function () {
                $(this).removeClass("text-danger");
            });
            target.addClass("text-danger");

            // 先删除原有的同name条件；
            $("#termForm > input[name='" + k + "']").remove();
            $("#termForm > input[name='" + k + "_max']").remove();
        }
        // 单选框，多选框，按钮；
        if (target.hasClass("btn") || k == "userId" || target.attr("type") == "radio") {
            // 先删除原有的同name条件；
            $("#termForm > input[name='" + k + "']").remove();
            $("#termForm > input[name='" + k + "_max']").remove();
        }

        // 拼接查询条件；
        if (v) {
            var input = "<input name='" + k + "' value='" + v + "'/>";
        }
        if (max) {
            input += "<input name='" + k + "_max' value='" + max + "'/>";
        }
        $("#termForm").append(input);
        reData();
    } else {
        // 按钮，下拉框，单选框；
        if (target.hasClass("btn") || k == "userId" || target.attr("type") == "radio") {
            // 先删除原有的同name条件；
            $("#termForm > input[name='" + k + "']").remove();
            $("#termForm > input[name='" + k + "_max']").remove();

            reData();
        } else {
            layer.msg("请输入查询内容。");
        }
    }
}

// 自定义栏的查询方法；
function loadMediaData(obj) {
    $(obj).closest("div").find("input").each(function () {
        // 先移除；
        $("#termForm > input[name='" + $(this).attr("name") + "']").remove();
        // 如果有值则存储；
        if ($(this).val().length > 0) {
            $("#termForm").append($(this).clone());
        }
    });
    reData();
}

/**
 * 刷新数据
 */
function reData() {
    if (url == undefined)
        url = "/mediaInfo";
    $("#table_medias").emptyGridParam();
    var colModel = $("#table_medias").jqGrid('getGridParam', 'colModel');
    $(colModel).each(function (j, d) {
        if (d.name) {
            // console.log(d.name)
            var name = mediaForm[d.name];
            // console.log(d.name+"=="+name)
            var flag = true;
            if (name) {
                $("#table_medias").jqGrid('setLabel', d.name, name);
                flag = false;
                $("#table_medias").setGridParam().showCol(d.name);
            } else {
                flag = true;
                if (j > 2)
                    $("#table_medias").setGridParam().hideCol(d.name);
            }

            if (j > 7 && d.name.indexOf('f') < 0 && d.name.lastIndexOf("Data") < 0) {
                if (flag && d.name != 'operator' && d.name != 'state')
                    $("#table_medias").setGridParam().hideCol(d.name);
                else
                    $("#table_medias").setGridParam().showCol(d.name);
            }
        }
    });
    var params = $("#termForm").serializeJson();
    $("#table_medias").jqGrid('setGridParam', {
        url: url,
        postData: params,
        loadComplete: function (gridData) {
            if (getResCode(gridData))
                return;
            for (var key in mediaForm) {
                if (key.indexOf('f') > -1) {
                    $("#table_medias label[name=" + key + "]").each(function () {
                        $(this).text(mediaForm[key] + ":");
                        $(this).attr("column", key);
                    });
                }
            }
        }
    }).trigger("reloadGrid");
    // $("#table_medias").closest(".ui-jqgrid-bdiv").css({ 'overflow-y' : 'scroll' });
    // $("#table_medias").reloadCurrentData(url, params, null, function (gridData) {
    //     if (getResCode(gridData))
    //         return;
    //     for (var key in mediaForm) {
    //         if (key.indexOf('f') > -1) {
    //             $("#table_medias label[name=" + key + "]").each(function () {
    //                 $(this).text(mediaForm[key] + ":");
    //                 $(this).attr("column", key);
    //             });
    //         }
    //     }
    // });


    var width = $('.jqGrid_wrapper').width();
    $('#table_medias').setGridWidth(width);
    // $('#table_medias').closest('.ui-jqgrid-view').find('div.ui-jqgrid-hdiv').width($(this).width()+15);
    // $("#table_medias").closest(".ui-jqgrid-bdiv").css({"overflow-x": "scroll"});
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
}

// 刷新供应商数据；
/*function loadSupplierData() {
    loadSupplier($("select[name='supplierId']"));
}*/

// 设置星期的显示；
function setWeekShow(v) {
    var array = ["", "周一", "周二", "周三", "周四", "周五", "周六", "周日"];
    var dataContent = v.toString();
    if (dataContent.length > 1) {
        var days = "";
        for (var i = 0; i < dataContent.length; i++) {
            days += array[parseInt(dataContent.charAt(i))] + "，";
        }
        return days.substr(0, days.length - 1);
    } else {
        return array[v];
    }
}

/**
 * 查询媒体
 */
function queryMedia() {
    $("#wxId").val($("#wxIdSearch").val());
    $("#name").val($("#mediaName").val());
    $("#supplierName").val($("#supplier").val());
    reData();
}