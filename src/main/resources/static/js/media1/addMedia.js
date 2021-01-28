var fh = '<div class="form-group col-md-3"><label class="col-sm-4 control-label">${labelName}:</label><div class="col-sm-8"><input type="${type}" name="${name}" placeholder="${labelName}" class="form-control"/></div></div>';
var url = "/media";
var fileUpload;
var imageUpload;

//全局参数定义
var mjUserMap = {}; //板块媒介对象，结构：{plateId: {userId: userName}}
var currentMediaForm = null; //页面当前板块FORM对象
var mediaType = {}; //缓存板块选择对应的媒体类型，结构：{plateId: {typeId: typeName}}
var mediaFormMap = {}; //缓存板块对应的扩展表单，结构：{plateId: {fromId: formName}}
var currentPriceGroupHTML = ""; //缓存当前价格分组初始化HTML
var supplierHtml = ""; //缓存当前页面供应商HTML
var currentRequiredSelectList = ["userId"]; //缓存当前表单需要必输校验的扩展下拉列表
var mediaUserPlateMap = [];
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';

    supplierHtml = $("#supplierDiv").children("div:last-child").html(); //缓存当前页面供应商HTML，需要和扩展框最后一个div对调位置

    initRule();//初始化校验规则

    initMediaType();//初始化板块
    $("#supplierId").load();

    //新增媒体图片上传
    imageUpload = new FileUpload({
        targetEl: '#imageUploadForm',
        multi: false,
        filePart: "media",
        completeCallback: function (data) {
            if (data.length > 0) {
                var filePath = data[0].file;
                $("#picPath").val(filePath);
                var $picPathPreview = $("#picPathPreview");
                $picPathPreview.attr('src', filePath);
                $picPathPreview.show();
            }
        },
        acceptSuffix: ['jpg', 'png']
    });

    //其他文件上传
    otherFileUpload = new FileUpload({
        targetEl: '#otherFileUpload',
        multi: false,
        filePart: "media",
        completeCallback: function (data) {
            swal("文件已上传完成，文件信息将显示在页面列表，请注意保存。");
            if (data.length > 0) {
                var filePath = data[0].file;
                $("#fileInfoDiv").show();
                var tableContent = "";
                var dataLength = $("#fileInfo").find("tr").length;
                tableContent += "<tr><td>" + (dataLength) + "</td>";
                tableContent += "<td><a href='" + filePath + "' target='_blank'>" + filePath + "</a></td></tr>";
                $("#fileInfo > tbody").append(tableContent);
            }
        }
    });

    //文件列表
    createFileInfo();

    mediaSupplierManageObj.addSupplierCheckRule("mediaSupplierAddModal");//供应商登记框添加校验规则
});

//初始化校验规则
function initRule() {
    var icon = "<i class='fa fa-times-circle'></i> ";
    $("#mf").validate({
        rules: {
            name: {
                required: true,
                remote: {
                    url: baseUrl + "/mediaAudit/validationMediaName", // 后台处理程序
                    type: "post", // 数据发送方式
                    dataType: "json", // 接受数据格式
                    data: { // 要传递的数据
                        "plateId": function () {
                            return $("#plateId").val();
                        },
                        "name": function () {
                            return $("#name").val();
                        },
                        "id": function () {
                            return $("#id").val() || "";
                        }
                    },
                    dataFilter: function (data) {
                        data = JSON.parse(data);
                        if (data.code == 200) {
                            return true;
                        } else {
                            $("#name").focus();
                            return false;
                        }
                    }
                }
            },
            mediaContentId: {
                required: true,
                remote: {
                    url: baseUrl + "/mediaAudit/validationMediaContentId", // 后台处理程序
                    type: "post", // 数据发送方式
                    dataType: "json", // 接受数据格式
                    data: { // 要传递的数据
                        "plateId": function () {
                            return $("#plateId").val();
                        },
                        "mediaContentId": function () {
                            return $("#mediaContentId").val();
                        },
                        "id": function () {
                            return $("#id").val() || "";
                        }
                    },
                    dataFilter: function (data) {
                        data = JSON.parse(data);
                        if (data.code == 200) {
                            return true;
                        } else {
                            $("#mediaContentId").focus();
                            return false;
                        }
                    }
                }
            },
        },
        messages: {
            name: {remote: icon + "该媒体名称已存在，请更换！"},
            mediaContentId: {remote: icon + "唯一标识已存在，请更换！"},
        }
    });
}

//媒体名称自定义规则
function setMediaNameRule(isAdd) {
    //判断是否添加规则
    if (isAdd) {
        $("#name").rules("add", {
            required: true,
            remote: {
                url: baseUrl + "/mediaAudit/validationMediaName", // 后台处理程序
                type: "post", // 数据发送方式
                dataType: "json", // 接受数据格式
                data: { // 要传递的数据
                    "plateId": function () {
                        return $("#plateId").val();
                    },
                    "name": function () {
                        return $("#name").val();
                    },
                    "id": function () {
                        return $("#id").val() || "";
                    }
                },
                dataFilter: function (data) {
                    data = JSON.parse(data);
                    if (data.code == 200) {
                        return true;
                    } else {
                        $("#name").focus();
                        return false;
                    }
                }
            }
        });
    } else {
        $("#name").rules("remove", "remote");
    }
}

//唯一标识自定义规则
function setMediaContentIdRule(isAdd) {
    //判断是否添加规则
    if (isAdd) {
        $("#mediaContentId").rules("add", {
            required: true,
            remote: {
                url: baseUrl + "/mediaAudit/validationMediaContentId", // 后台处理程序
                type: "post", // 数据发送方式
                dataType: "json", // 接受数据格式
                data: { // 要传递的数据
                    "plateId": function () {
                        return $("#plateId").val();
                    },
                    "mediaContentId": function () {
                        return $("#mediaContentId").val();
                    },
                    "id": function () {
                        return $("#id").val() || "";
                    }
                },
                dataFilter: function (data) {
                    data = JSON.parse(data);
                    if (data.code == 200) {
                        return true;
                    } else {
                        $("#mediaContentId").focus();
                        return false;
                    }
                }
            }
        });
    } else {
        $("#mediaContentId").rules("remove", "remote");
    }
}

//创建文件列表
function createFileInfo() {
    $("#fileInfo").jqGrid({
        datatype: "local",
        autowidth: true,
        height: "auto",
        colModel: [
            {
                name: 'index',
                label: '序号',
                sortable: false,
                editable: false,
                width: 10,
                align: "center"
            },
            {
                name: 'filePath',
                label: '文件地址',
                sortable: false,
                editable: false,
                width: 100,
                align: "center"
            }
        ],
        caption: "文件上传列表",
    });
    $("#fileInfoDiv").hide();
}

//得到查询参数
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]);
    return null;
}

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType, dataType, async, callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun
    });
}

/**
 * 初始化媒体板块
 */
function initMediaType() {
    requestData(null, "/mediaPlate/userId", "get", "json", false, function (data) {
        if (data == null || data == '') {
            swal("没有板块可操作！", "没有查询到板块信息，请联系管理员赋权！", "warning");
            return;
        }
        var standardHtml = "";
        var notStandardHtml = "";
        var selectHtml = "";//用于编辑窗口板块下拉列表
        var len = 1;
        // if (data.length < 12) {
        //     len = 12 / data.length;
        // }
        $(data).each(function (i, item) {
            mediaUserPlateMap.push(item.id);
            if (item.standarPlatformFlag == 1) {
                standardHtml += "<div style='width: 10%;float: left;'><span style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;width: 100%;' class='btn btn-outline plateSpan' data-standarPlatformFlag='" + item.standarPlatformFlag + "' title='" + item.name + "' data-value='" + item.id + "' onclick='setType(" + item.id + ",this)'>" + item.name + "</span></div>";
            } else {
                notStandardHtml += "<div style='width: 10%;float: left;'><span style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;width: 100%;' class='btn btn-outline plateSpan' data-standarPlatformFlag='" + item.standarPlatformFlag + "' title='" + item.name + "' data-value='" + item.id + "' onclick='setType(" + item.id + ",this)'>" + item.name + "</span></div>";
            }
            selectHtml += '<option value="' + item.id + '">' + item.name + '</option>';
        });
        //判断标准和非标准是否有，控制隐藏显示
        if (standardHtml) {
            $("#extendFormStandardPlateWrap").css("display", "flex");
            $("#extendFormStandardPlate").html(standardHtml);
        } else {
            $("#extendFormStandardPlateWrap").css("display", "none");
            $("#extendFormStandardPlate").html("");
        }
        if (notStandardHtml) {
            $("#extendFormNotStandardPlateWrap").css("display", "flex");
            $("#extendFormNotStandardPlate").html(notStandardHtml);
        } else {
            $("#extendFormNotStandardPlateWrap").css("display", "none");
            $("#extendFormNotStandardPlate").html("");
        }
        var type = getQueryString("type");
        if (type && type === '2') {
            $("#mediaPlate span[data-value='" + type + "']").click();
        } else {
            $("#mediaPlate>div:first-child>span:first-child").click();

            //如果标准有值，则先查询标准的
            if (standardHtml) {
                $("#extendFormStandardPlate > div:first-child > span:first-child").click();
            } else {
                if (notStandardHtml) {
                    $("#extendFormNotStandardPlate > div:first-child > span:first-child").click();
                }
            }
        }
    });
}

//设置媒体表单扩展项
function setType(id, t) {
    var standarPlatformFlag = $(t).attr("data-standarPlatformFlag") || 0;
    var backColor = standarPlatformFlag == 1 ? "btn-primary" : "btn-danger";
    $(t).closest(".plateWrap").find(".plateSpan").each(function (i, item) {
        $(item).removeClass("btn-primary");
        $(item).removeClass("btn-danger");
        if (t == item) {
            $(t).addClass(backColor);
        }
    });
    $("#standarPlatformFlag").val($(t).attr("data-standarPlatformFlag") || 0);
    $("#mType").val(id);
    $("#plateId").val(id);
    $("#plateTypeId").val(id);
    $("#mediaPlateId").val(id);
    $("#termForm input[name='plateId']").val(id);
    $("#mTypeName").val($(t).text());
    $("#supplierId").find("option").each(function (i, t) {
        $(t).remove(); //清空时移除当前option
    });

    //每次切换板块，初始化公共数据
    $("#mf")[0].reset();//重置表单数据
    //头像初始化
    $("#picPath").val("");
    var $picPathPreview = $("#picPathPreview");
    $picPathPreview.attr('src', "");
    $picPathPreview.css("display", "none");
    //显示案例链接 还是媒体ID
    if ($(t).attr("data-standarPlatformFlag") && $(t).attr("data-standarPlatformFlag") == 1) {
        setMediaNameRule(false);//添加媒体名称校验规则
        setMediaContentIdRule(true);

        $("#mediaContentIdDiv").css("display", "block");
        $("#linkDiv").find("input").removeAttr("required"); //案例链接不必输
        $("#linkStar").css("display", "none");//隐藏*号
        //隐藏显示折扣率
        $("#discountDiv1").find("input").attr("disabled", true);
        $("#discountDiv1").css("display", "none");
        $("#discountDiv2").find("input").removeAttr("disabled");
        $("#discountDiv2").css("display", "block");
    } else {
        setMediaContentIdRule(false);//添加媒体名称校验规则
        setMediaNameRule(true);

        $("#mediaContentIdDiv").css("display", "none");
        $("#linkDiv").find("input").attr("required", true); //案例链接必输
        $("#linkStar").css("display", "inline-block");//显示*号
        //隐藏显示折扣率
        $("#discountDiv2").find("input").attr("disabled", true);
        $("#discountDiv2").css("display", "none");
        $("#discountDiv1").find("input").removeAttr("disabled");
        $("#discountDiv1").css("display", "block");
    }

    //责任人
    var html = "<option value=\"" + user.id + "\" selected>" + user.name + "</option>";
    $("#userId").html(html);
    /*if(!mjUserMap[id]){
        requestData(null,"/user/listMJByPlateId2/"+id,"get","json",false,function (data) {
            var userList = data.data.userList;
            mjUserMap[id] = userList;
            renderUser(userList);
        });
    }else{
        renderUser(mjUserMap[id]);
    }*/


    //扩展表单
    if (!mediaFormMap[id]) {
        requestData(null, "/mediaForm1/" + id, "get", "json", false, function (datas) {
            currentMediaForm = datas;
            mediaFormMap[id] = currentMediaForm; //缓存表单
            renderPage(datas);
        });
    } else {
        currentMediaForm = mediaFormMap[id];
        renderPage(currentMediaForm);
    }

    layui.use(["form", 'element'], function () {
        layui.form.render();//layui-form
    });
}

//渲染责任人
function renderUser(data) {
    var value = $("#userId").attr("data-value");
    if (!value || value == "${data-value}") {
        value = user.id;
    }
    var html = "<option value=\"\">请选择责任人</option>";
    if (data && data.length > 0) {
        $.each(data, function (i, item) {
            if (item.id == value) {
                html += "<option value=\"" + item.id + "\" selected>" + item.name + "</option>";
            } else {
                html += "<option value=\"" + item.id + "\">" + item.name + "</option>";
            }
        })
    }
    $("#userId").html(html);
    layui.use(["form"], function () {
        layui.form.render();//layui-form
    });
}

//重新渲染表单页面
function renderPage(datas) {
    $("#mediaForm > #extend").html(""); //清空
    var currentPriceFromList = new Array();
    var currentPriceHTMLList = new Array();  //缓存价格HTML
    var currentNotPriceHTMLList = new Array();  //缓存非价格HTML  到时将价格放在最后显示
    var html = '';
    $(datas).each(function (i, data) {
        var data = data;
        var required = data.required ? "required" : "";
        var maxLength = data.maxlength ? "maxlength=" + data.maxlength : "";
        var minLength = data.minlength ? "minlength=" + data.minlength : "";
        var size = data.size ? "size=" + data.size : "";
        var min = data.min ? "min=" + data.min : "";
        var max = data.max ? "max=" + data.max : "";
        var disabled = data.disabled ? "disabled=" + data.disabled : "";
        var cellName = data.cellName;
        var extendFlag = data.extendFlag; //扩展字段标识：0-仅媒体用、1-仅供应商用，对于媒体价格字段，默认供应商也可使用
        var cell = "cell:" + data.cellCode + ":" + cellName + ":" + data.type;
        disabled = (!disabled && data.climbFlag == 1) ? "disabled" : "";//是否爬取标识：0-手工填写、1-仅脚本爬取、2-手工+爬取，仅爬取的字段不可输入
        required = disabled ? "" : required;//如果不可输入，则取消必填
        html = "";//每次进来初始化
        // if (i % 4 == 0)
        //     html += '<div class="col-md-12">';
        html += '<div class="form-group col-sm-3"><label class="col-sm-4 control-label">';
        if (required) {
            html += '<span class="text-red"> * </span>';
        }
        html += cellName + ':</label><div class="col-sm-8">';
        if (extendFlag == 1) {
            currentPriceFromList.push(data);
        } else {
            switch (data.type) {
                case 'radio':
                case 'checkbox':
                    var dbHtml = "";
                    var dataClass = data.type == "radio" ? "radio-inline" : "checkbox-inline";
                    if (data.dataType && data.dataType == 'html' && data.dbHtml) {
                        dbHtml = data.dbHtml;
                    } else if (data.dataType && data.dataType == 'json' && data.dbJson) {
                        var json = eval(data.dbJson);
                        if (!Array.isArray(json)) {
                            json = [json];
                        }
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += "<label class=\"" + dataClass + " i-checks\"><input " + disabled + " cell-name=\"${cell-name}\" title='" + text + "'  type=\"" + data.type + "\" value=\"" + item.value + "\" name=\"${name}\" data-value=\"${data-value}\">" + text + "</label>";
                        });
                    } else {
                        var sqlDatas = data.datas;
                        if (sqlDatas && sqlDatas.length > 0) {  //如果对象存在，并且个数大于0
                            $.each(sqlDatas, function (i, item) {
                                dbHtml += "<label class=\"" + dataClass + " i-checks\"><input " + disabled + " cell-name=\"${cell-name}\" title='" + item.name + "'  type=\"" + data.type + "\" value=\"" + item.id + "\" name=\"${name}\" data-value=\"${data-value}\">" + item.name + "</label>";
                            });
                        }
                    }
                    html += dbHtml;
                    break;
                case 'select':
                    var dbHtml = "";
                    if (data.dataType && data.dataType == 'html' && data.dbHtml) {
                        dbHtml = data.dbHtml;
                    } else if (data.dataType && data.dataType == 'json' && data.dbJson) {
                        var json = eval(data.dbJson);
                        if (!Array.isArray(json)) {
                            json = [json];
                        }
                        dbHtml += "<select cell-name=\"${cell-name}\" name=\"${name}\" placeholder=\"${labelName}\" data-value=\"${data-value}\" " + required + " " + disabled + " class=\"form-control\">";
                        dbHtml += "<option value=\"\">请选择" + cellName + "</option>";
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += '<option value="' + item.value + '" " ' + (item.isDefault == "true" || item.isDefault == true ? "selected" : "") + '>' + text + '</option>';
                        });
                        dbHtml += "</select>";
                    } else {
                        var sqlDatas = data.datas;
                        dbHtml += "<select cell-name=\"${cell-name}\" name=\"${name}\" placeholder=\"${labelName}\" data-value=\"${data-value}\" " + required + " " + disabled + " class=\"form-control\">";
                        dbHtml += "<option value=\"\">请选择" + cellName + "</option>";
                        if (sqlDatas && sqlDatas.length > 0) {  //如果对象存在，并且个数大于0
                            $.each(sqlDatas, function (i, item) {
                                dbHtml += "<option value=\"" + item.id + "\">" + item.name + "</option>";
                            });
                        }
                        dbHtml += "</select>";
                    }
                    html += dbHtml;
                    break;
                case 'textarea':
                    html += '<textarea name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " "
                        + maxLength + " " + minLength + " " + size + " " + min + " " + max + disabled + " " + ' class="form-control" style="resize:none;"></textarea>';
                    break;
                case 'date':
                    html += '<input name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " "
                        + maxLength + " " + minLength + " " + size + " " + min + " " + max + " " + disabled + " "
                    ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD\'})"/>';
                    break;
                case 'datetime':
                    html += '<input name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " + disabled + " " +
                        size + " " + min + " " + max + ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD hh:mm:ss\'})"/>';
                    break;
                case 'time':
                    html += '<input name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " + disabled + " " +
                        size + " " + min + " " + max + ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'hh:mm:ss\'})"/>';
                    break;
                case 'price':
                    html += '<div class="input-group m-b"><span class="input-group-addon">¥</span><input onkeypress="return inPrice(event)" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max + " " + disabled + " " +
                        '  type="number" value="0" class="form-control" name="${name}" placeholder="${labelName}" cell-name="${cell-name}"> <!--<span class="input-group-addon">.00</span>--></div>';
                    currentPriceFromList.push(data);
                    //将字段保存，用于动态创建价格分组
                    break;
                case 'file':
                    html += '<input type="file" cell-name="${cell-name}" name="file" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max + " " + disabled + ' class="form-control"/>';
                    break;
                case 'number':
                    html += '<input onkeypress="return inNum(event)" cell-name="${cell-name}" type="number" name="${name}" id="${name}" placeholder="${labelName}" ' + required + " "
                        + " " + disabled + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max + ' class="form-control"/>';
                    break;
                default:
                    html += '<input type="${type}" name="${name}" cell-name="${cell-name}" id="${name}" placeholder="${labelName}" ' + required + " " + maxLength + " "
                        + " " + disabled + " " + minLength + " " + size + " " + min + " " + max + ' class="form-control"/>';
                    break;
            }
            html = html.th('id', cell).th('name', cell).th('labelName', cellName).th('type', data.type).th("cell-name", cellName).th("required", required);
            // if ((i + 1) % 4 == 0) html += '</div></div></div>';
            // else html += '</div></div>';
            html += '</div></div>';
            if (data.type != 'price') {
                currentNotPriceHTMLList.push(html);
            } else {
                currentPriceHTMLList.push(html);
            }
        }
    });
    //统一排序处理，将价格HTML添加到非价格后面
    currentNotPriceHTMLList = currentNotPriceHTMLList.concat(currentPriceHTMLList);
    var html1 = "";
    $.each(currentNotPriceHTMLList, function (i, v) {
        if (i % 4 == 0) {
            html1 += '<div class="col-md-12">';
        }
        html1 += v;
        if ((i + 1) % 4 == 0) {
            html1 += '</div>';
        }
    })

    $("#mediaForm > #extend").html(html1);

    //将供应商和扩展框中的最后一个div对调
    /* var extendLastDiv = $("#extend").children("div:last-child").children("div:last-child").html();
     $("#extend").children("div:last-child").children("div:last-child").html(supplierHtml);
     $("#supplierDiv").children("div:last-child").html(extendLastDiv)*/

    //下拉列表加载
    $("#mediaForm select").each(function () {
        if ($(this).attr("onload") && $(this).attr("id") != "supplierId")
            this.onload();
    });
    createPriceGroup(currentPriceFromList); //生成价格组

    // initPriceGroup(); //初始化价格分组

    initRadioValue(); // radio默认选择否；

    renderSelect(); //重新渲染扩展下拉列表

}

//初始化价格分组
function initPriceGroup() {
    if (currentPriceGroupHTML) { //如果有值，则说明有价格分组，否则没有
        $("#extendPriceDiv").css("display", "block");
        $("#extendPriceDiv").html(currentPriceGroupHTML); //填充默认价格组
    } else {
        $("#extendPriceDiv").css("display", "none");
    }
}

//可选下拉列表实现
function renderSelect() {
    currentRequiredSelectList = ["userId"]; //每次渲染重新初始化
    layui.use(["form", 'element'], function () {
        $("#extend").find("select").each(function (i, t) {
            $(t).parent().addClass("layui-form");
            if (!$(t).attr("lay-search")) {
                $(t).attr("lay-search", "");
            }
            if ($(t).attr("required")) {
                currentRequiredSelectList.push($(t).attr("name"));
            }
        });
        layui.form.render('select');//layui-form

        //给渲染后必输的select添加必输校验
        if (currentRequiredSelectList && currentRequiredSelectList.length > 0) {
            $.each(currentRequiredSelectList, function (i, selectName) {
                $("select[name='" + selectName + "']").parent().find("input").attr("required", true);
                $("select[name='" + selectName + "']").parent().find("input").attr("id", selectName);
            });
        }
    });
}

//更新扩展单选框
function initRadioValue() {
    var dataValue;
    $("#mediaForm input[type='radio']").each(function () {
        dataValue = $(this).attr("data-value");
        if (dataValue && dataValue == "${data-value}") {
            $(this).attr("data-value", "1");
            if ($(this).val() == 1) {
                $(this).iCheck("check");
            }
        } else {
            if ($(this).val() == 1) {
                $(this).iCheck("check");
            }
        }
    });
    $("#extendForm input[type='radio']").each(function () {
        dataValue = $(this).attr("data-value");
        if (dataValue && dataValue == "${data-value}") {
            $(this).attr("data-value", "1");
            if ($(this).val() == 1) {
                $(this).iCheck("check");
            }
        } else {
            if ($(this).val() == 1) {
                $(this).iCheck("check");
            }
        }
    });
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
}

//创建供应商分组
function createPriceGroup(currentPriceFromList) {
    currentPriceGroupHTML = "";
    if (currentPriceFromList && currentPriceFromList.length > 0) {
        currentPriceGroupHTML += "<div id='${divId}' supplier-id='${supplierId}' style=\"background-color: #f1f0e7;border:1px solid #eee;padding: 0px;padding-top: 10px;margin-bottom: 10px;\" class=\"col-md-12\">";
        currentPriceGroupHTML += " <div name='close' onclick=\"deleteGroup(this);\" title=\"关闭\" style=\"display:none;position: absolute;top: 1px;right: 0px;width: 20px;height: 20px;line-height: 20px;font-size: 20px;text-align: center;cursor: pointer;\">" +
            "<i class=\"fa fa-trash\"></i>" +
            "</div>";
        currentPriceGroupHTML += "<div class=\"layui-form\" style=\"display: block; z-index:10;position: absolute; top: 0px; right: 20px; width: 60px; height: 22px; line-height: 22px;cursor: pointer;\">\n" +
            "                          <input type=\"checkbox\" lay-skin=\"switch\" value='0' lay-text=\"启用|禁用\" checked>\n" +
            "                      </div>";
        var addSupplierFlag = false; //是否有加入供应商控件，加到第一个位置，只加入一次，默认无
        var priceCount = 0; // 控件行数
        $(currentPriceFromList).each(function (i, data) {
            var data = data;
            var required = "";
            var maxLength = data.maxlength ? "manLength=" + data.maxlength : "";
            var minLength = data.minlength ? "minLength=" + data.minlength : "";
            var size = data.size ? "size=" + data.size : "";
            var min = data.min ? "min=" + data.min : "";
            var max = data.max ? "max=" + data.max : "";
            var disabled = data.disabled ? "disabled=" + data.disabled : "";
            var cellName = data.cellName;
            var extendFlag = data.extendFlag; //扩展字段标识：0-仅媒体用、1-仅供应商用，对于媒体价格字段，默认供应商也可使用，如果是0则表示价格
            var cell = "cell:" + data.cellCode + ":" + cellName + ":" + data.type;
            if (priceCount % 4 == 0)
                currentPriceGroupHTML += '<div class="col-md-12">';
            if (!addSupplierFlag) {  //如果没加入供应商控件,则加入
                addSupplierFlag = true;
                priceCount++;
                currentPriceGroupHTML += $("#supplierHTML").html();
            }


            currentPriceGroupHTML += '<div data-extendFlag="${extendFlag}" cell="${cell}" class="form-group col-sm-3"><label class="col-sm-4 control-label">';
            if (data.required) {
                // currentPriceGroupHTML += '<span class="text-red"> * </span>';
                required = "required";
            }
            currentPriceGroupHTML += cellName + ':</label><div class="col-sm-8">';
            switch (data.type) {
                case 'radio':
                case 'checkbox':
                    var dbHtml = "";
                    var dataClass = data.type == "radio" ? "radio-inline" : "checkbox-inline";
                    if (data.dataType && data.dataType == 'html' && data.dbHtml) {
                        dbHtml = data.dbHtml;
                    } else if (data.dataType && data.dataType == 'json' && data.dbJson) {
                        var json = eval(data.dbJson);
                        if (!Array.isArray(json)) {
                            json = [json];
                        }
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += "<label class=\"" + dataClass + " i-checks\"><input data-extendFlag='${extendFlag}' cell-name=\"${cell-name}\" title='" + text + "'  type=\"" + data.type + "\" value=\"" + item.value + "\" name=\"${name}\" data-value=\"${data-value}\" " + required + ">" + text + "</label>";
                        });
                    } else {
                        var sqlDatas = data.datas;
                        if (sqlDatas && sqlDatas.length > 0) {  //如果对象存在，并且个数大于0
                            $.each(sqlDatas, function (i, item) {
                                dbHtml += "<label class=\"" + dataClass + " i-checks\"><input data-extendFlag='${extendFlag}' cell-name=\"${cell-name}\" title='" + item.name + "'  type=\"" + data.type + "\" value=\"" + item.id + "\" name=\"${name}\" data-value=\"${data-value}\" " + required + ">" + item.name + "</label>";
                            });
                        }
                    }
                    currentPriceGroupHTML += dbHtml;
                    break;
                case 'select':
                    var dbHtml = "";
                    if (data.dataType && data.dataType == 'html' && data.dbHtml) {
                        dbHtml = data.dbHtml;
                    } else if (data.dataType && data.dataType == 'json' && data.dbJson) {
                        var json = eval(data.dbJson);
                        if (!Array.isArray(json)) {
                            json = [json];
                        }
                        dbHtml += "<select data-extendFlag='${extendFlag}' cell-name=\"${cell-name}\" name=\"${name}\" placeholder=\"${labelName}\" data-value=\"${data-value}\" " + required + " class=\"form-control\">";
                        dbHtml += "<option value=\"\">请选择" + cellName + "</option>";
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += '<option value="' + item.value + '" " ' + (item.isDefault == "true" || item.isDefault == true ? "selected" : "") + '>' + text + '</option>';
                        });
                        dbHtml += "</select>";
                    } else {
                        var sqlDatas = data.datas;
                        dbHtml += "<select data-extendFlag='${extendFlag}' cell-name=\"${cell-name}\" name=\"${name}\" placeholder=\"${labelName}\" data-value=\"${data-value}\" " + required + " class=\"form-control\">";
                        dbHtml += "<option value=\"\">请选择" + cellName + "</option>";
                        if (sqlDatas && sqlDatas.length > 0) {  //如果对象存在，并且个数大于0
                            $.each(sqlDatas, function (i, item) {
                                dbHtml += "<option value=\"" + item.id + "\">" + item.name + "</option>";
                            });
                        }
                        dbHtml += "</select>";
                    }
                    currentPriceGroupHTML += dbHtml;
                    break;
                case 'textarea':
                    currentPriceGroupHTML += '<textarea data-extendFlag=\'${extendFlag}\' name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " "
                        + maxLength + " " + minLength + " " + size + " " + min + " " + max + ' class="form-control" style="resize:none;"></textarea>';
                    break;
                case 'date':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " "
                        + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                        ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD\'})"/>';
                    break;
                case 'datetime':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " +
                        size + " " + min + " " + max + ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD hh:mm:ss\'})"/>';
                    break;
                case 'time':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " +
                        size + " " + min + " " + max + ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'hh:mm:ss\'})"/>';
                    break;
                case 'price':
                    currentPriceGroupHTML += '<div class="input-group"><span class="input-group-addon">¥</span><input data-extendFlag=\'${extendFlag}\' onkeypress="return inPrice(event)" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                        'oninput="priceChange(this)"  type="number" class="form-control" name="${name}" placeholder="${labelName}" cell-name="${cell-name}"> <!--<span class="input-group-addon">.00</span>--></div>';
                    break;
                case 'file':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' type="file" cell-name="${cell-name}" name="file" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max + " " + disabled + ' class="form-control"/>';
                    break;
                case 'number':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' onkeypress="return inNum(event)" cell-name="${cell-name}" type="number" name="${name}" id="${name}" placeholder="${labelName}" ' + required + " "
                        + " " + disabled + maxLength + " " + minLength + " " + size + " " + min + " " + max + ' class="form-control"/>';
                    break;
                default:
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' type="${type}" name="${name}" cell-name="${cell-name}" id="${name}" placeholder="${labelName}" ' + required + " " + maxLength + " "
                        + " " + disabled + minLength + " " + size + " " + min + " " + max + ' class="form-control"/>';
                    break;
            }
            currentPriceGroupHTML = currentPriceGroupHTML.th('id', cell).th('name', cell).th('labelName', cellName).th('type', data.type).th("cell-name", cellName).th("extendFlag", extendFlag).th("cell", cell);
            if ((priceCount + 1) % 4 == 0) currentPriceGroupHTML += '</div></div></div>';
            else currentPriceGroupHTML += '</div></div>';
            priceCount++;
        });
        currentPriceGroupHTML += "</div>";
    }
}

//供应商金额框改变事件
function priceChange(target) {
    /*var name = $(target).attr("name"); //获取当前金额框name值
    //统计价格分组中当前价格类型的最小值，并设置到媒体中对应的价格类型中
    var arr = new Array();
    $("#extendPriceDiv > div").each(function (i, t) {
        var enableFalg = $(t).find("input[type='checkbox']").val() || 0;
        if(enableFalg != 1){  //禁用的供应商价格不计算
            $(t).find("input[name='"+name+"']").each(function (i, t) {
                if ($(t).val() && parseFloat($(t).val())) {
                    arr.push(parseFloat($(t).val()));
                }
            });
        }
    });
    var minPrice = "";
    if(arr.length > 0){
        minPrice = Math.min.apply(null,arr);
    }
    $("#mediaForm").find("input[name='"+name+"']").val(minPrice || 0);*/
}

/**
 * 新增供应商
 */
// function addSupplier() {
//     alertEdit('/media/supplier_edit?op=create&mediaTypeId=' + $("#mType").val(), '新增媒体供应商');
// }

/**
 * 添加价格分组
 */
function addGroup(supplierId) {
    $("#extendPriceDiv").append(currentPriceGroupHTML.th("divId", supplierId));
    layui.use(["form", 'element'], function () {
        layui.form.render();//layui-form
    });
}

/**
 * 删除价格分组
 * @param t
 */
function deleteGroup(t) {
    var supplierId = $(t).parent().attr("supplier-id");//移除下拉列表对应供应商
    if (supplierId) {
        $("#supplierId").find("option").each(function (i, op) {
            if ($(op).val() == supplierId) {
                $(op).remove(); //清空时移除当前option
                $(t).parent().remove(); //移除分组
            }
        })
    }
    /*if($("#extendPriceDiv > div").length > 1){
        var supplierId = $(t).parent().attr("supplier-id");//移除下拉列表对应供应商
        if(supplierId){
            $("#supplierId").find("option").each(function (i,op) {
                if($(op).val() == supplierId){
                    $(op).remove(); //清空时移除当前option
                    $(t).parent().remove(); //移除分组
                }
            })
        }
        //当价格分组只剩下一个时，删除按钮隐藏
        if($("#extendPriceDiv > div").length == 1){
            $("#extendPriceDiv").children("div:first-child").find("div[name='close']").css("display","none");//当仅有一个分组时，分组右上角的删除按钮隐藏
        }
    }else{
        layer.msg("必须存在一个价格分组！");
    }*/
    reflushMediaMinPrice();
    reflushGroupBackColor();//设置分组背景颜色
}

//刷新分组背景颜色
function reflushGroupBackColor() {
    $("#extendPriceDiv > div").each(function (index, div) {
        if ((index % 2) == 0) {
            $(div).css("backgroundColor", "#f1f0e7");
        } else {
            $(div).css("backgroundColor", "#e1efed");
        }
    })
}

//更新媒体最低价格
function reflushMediaMinPrice() {
    // $("#extendPriceDiv").children("div:first-child").find("input").trigger("input"); //只要第一个价格组触发input事件
}

//获取表单json对象（含值为空的字段）
function getFormJson(formId) {
    var o = {};
    $.each($(formId).serializeArray(), function () {
        if (o[this.name]) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
}

/**
 * 媒体保存
 */
function saveMedia(pid) {
    var id = '#mf';
    if (pid != null && pid != undefined)
        id = "#" + pid;
    var requiredFlag = false; //必输校验
    if (!$(id).valid()) {
        requiredFlag = true;
    }
    //价格组必输校验
    if (!$("#extendForm").valid()) {
        requiredFlag = true;
        //如果有价格必输提示，则放到div外面，避免样式出错
        $("#extendForm").find("input[type='number']").each(function (i, inputNode) {
            if ($(inputNode).attr("required")) {
                var $errorLabel = $(inputNode).parent().find("label.error");
                $(inputNode).parent().after($errorLabel);
            }
        })
    }
    //校验layui下拉列表必输，需要放在表单valid方法后面
    if (currentRequiredSelectList && currentRequiredSelectList.length > 0) {
        $.each(currentRequiredSelectList, function (i, selectName) {
            var $input = $("select[name='" + selectName + "']").parent().find("input");
            if (!$("select[name='" + selectName + "']").val()) { //如果必输项没有值，则弹出提示
                requiredFlag = true;
                $input.parent().find("label.error").remove();
                $input.after("<label id='${name}-error' for='${name}' class='error' style='display: inline-block;'><i class=\"fa fa-times-circle\"></i>  必填</label>".th("name", selectName));
            } else {
                $input.parent().find("label.error").remove();
            }
        });
    }

    //如果供应商有必输提示，则移动该提示信息到按钮后面，否则布局会歪一点
    supplierRequiredTip();

    if (requiredFlag) {
        return;
    }
    reflushMediaMinPrice(); //重新更新最新价格

    // var data = $(id).toJSON();
    var json = getFormJson(id);
    var array = new Array();
    var mediaPriceFlag = false;// 默认校验不通过，价格至少得输入一个价格
    for (var k in json) {
        if (k.indexOf("cell:") > -1) {
            var kk = k.substring(5, k.length);
            var k1 = kk.split(":")[0];
            var k2 = kk.split(":")[1];
            var k3 = kk.split(":")[2];
            var value = json[k];
            if ("select" == k3) {
                var cellValueText = $("select[name='" + k + "'] option:selected").text();
                array.push({
                    cell: k1,
                    cellName: k2,
                    cellValue: value,
                    cellValueText: cellValueText,
                    dbType: k3,
                    type: k3
                });
            } else if ("radio" == k3) {
                var cellValueText = $("input:radio[name='" + k + "']:checked ").parent().parent().text();
                array.push({
                    cell: k1,
                    cellName: k2,
                    cellValue: value,
                    cellValueText: cellValueText,
                    dbType: k3,
                    type: k3
                });
            } else if ("checkbox" == k3) {
                var cellValueText = new Array();
                $("input:checkbox[name='" + k + "']:checked ").each(function (i, node) {
                    cellValueText.push($(node).parent().parent().text())
                });
                if (Array.isArray(value)) {
                    array.push({
                        cell: k1,
                        cellName: k2,
                        cellValue: value.join(","),
                        cellValueText: cellValueText.join(","),
                        dbType: k3,
                        type: k3
                    });
                } else {
                    array.push({
                        cell: k1,
                        cellName: k2,
                        cellValue: value,
                        cellValueText: cellValueText.join(","),
                        dbType: k3,
                        type: k3
                    });
                }
            } else if ("price" == k3) {
                value = value || 0;
                if (value > 0) {
                    mediaPriceFlag = true; //校验通过
                }
                array.push({cell: k1, cellName: k2, cellValue: value, dbType: k3, type: k3});
            } else {
                array.push({cell: k1, cellName: k2, cellValue: value, dbType: k3, type: k3});
            }
            delete json[k];
        }
    }
    json["mediaExtends"] = array;  //媒体数据

    /*if(!mediaPriceFlag){
        swal({
            title: "媒体价格校验失败",
            text: "媒体至少得输入一个价格，且必须大于0！",
            type: "warning",
            html: true
        });
        return;
    }*/

    //下面统计供应商价格
    var validErrorInfo = new Array(); //校验供应商的价格分组至少得输入一个价格
    var supplierArr = new Array(); //供应商列表

    //如果媒体有选择供应商联系人
    if (json.supplierId) {
        if (Array.isArray(json.supplierId)) {
            supplierArr = json.supplierId;
        } else {
            supplierArr.push(json.supplierId);
        }
    }

    var supplierList = new Array();
    $.each(supplierArr, function (i, id) {
        var obj = {supplierId: id};
        if (currentPriceGroupHTML) { //存在价格分组，目前一定存在
            var validFlag = false; // 默认校验不通过，价格分组至少得输入一个价格
            obj.supplierName = $("#supplierPrice" + id).find("input[name='supplierName']").val();
            obj.enabled = $("#supplierPrice" + id).find("input[type='checkbox']").val() || '0';
            var priceArray = new Array();  //当有多个价格时，才使用，否则能将价格存储array中
            $("#supplierPrice" + id).find("div[data-extendFlag]").each(function (index, divObj) { //遍历金额控件
                var name = $(divObj).attr("cell");
                if (name.indexOf("cell:") > -1) { //有值
                    var name1 = name.substring(5, name.length);
                    var cell = name1.split(":")[0];
                    var cellName = name1.split(":")[1];
                    var type = name1.split(":")[2];
                    if ("select" == type) {
                        var value = $(divObj).find("select[name='" + name + "'] option:selected").val() || "";
                        var cellValueText = $(divObj).find("select[name='" + name + "'] option:selected").text();
                        priceArray.push({
                            cell: cell,
                            cellName: cellName,
                            cellValue: value,
                            cellType: type,
                            type: type,
                            cellValueText: cellValueText
                        });
                    } else if ("radio" == type) {
                        var value = $(divObj).find("input:radio[name='" + name + "']:checked ").val() || "";
                        var cellValueText = $(divObj).find("input:radio[name='" + name + "']:checked ").parent().parent().text();
                        priceArray.push({
                            cell: cell,
                            cellName: cellName,
                            cellValue: value,
                            cellType: type,
                            type: type,
                            cellValueText: cellValueText
                        });
                    } else if ("checkbox" == type) {
                        var cellValue = new Array();
                        var cellValueText = new Array();
                        $(divObj).find("input:checkbox[name='" + name + "']:checked ").each(function (i, node) {
                            cellValue.push($(node).val());
                            cellValueText.push($(node).parent().parent().text());
                        });
                        priceArray.push({
                            cell: cell,
                            cellName: cellName,
                            cellValue: cellValue.join(","),
                            cellType: type,
                            type: type,
                            cellValueText: cellValueText.join(",")
                        });
                    } else if ("price" == type) {
                        var value = $(divObj).find("input").val() || 0;
                        if (value > 0) {
                            validFlag = true; //校验通过
                        }
                        priceArray.push({cell: cell, cellName: cellName, cellValue: value, cellType: type, type: type});
                    } else {
                        var value = $(divObj).find("input").val() || '';
                        priceArray.push({cell: cell, cellName: cellName, cellValue: value, cellType: type, type: type});
                    }
                }
            });
            if (!validFlag) {
                validErrorInfo.push("供应商[" + obj.supplierName + "]至少得输入一个价格！")
            }

            obj.mediaPriceExtends = priceArray;
        } else {
            obj.supplierName = $("#supplier" + id).val();
        }
        supplierList.push(obj);
    });
    json["supplierList"] = supplierList;
    delete json["supplierId"];
    delete json["supplierName"];

    //如果有供应商价格，则必须输入一个价格
    if (supplierArr && supplierArr.length > 0 && validErrorInfo.length > 0) {
        swal({
            title: "供应商价格校验失败",
            text: validErrorInfo.join("<br>"),
            type: "warning",
            html: true
        });
        return;
    }

    startModal("#" + $("#saveBtn").attr("id"));
    $.ajax({
        url: baseUrl + "/mediaAudit/save",
        type: 'POST',
        cache: false,
        contentType: 'application/json;charset=utf-8', //设置请求头信息
        data: JSON.stringify(json),
        dataType: "json",
        success: function (data) {
            Ladda.stopAll();
            swal({
                title: data.code == 200 ? "成功!" : "失败",
                text: data.code == 200 ? "媒体保存成功！" : data.msg,
                type: data.code == 200 ? "success" : "error",
                html: true
            });

            if (data.code == 200) {
                $(id)[0].reset();//重置表单数据
                initRadioValue();//初始化单选
                //头像初始化
                $("#picPath").val("");
                var $picPathPreview = $("#picPathPreview");
                $picPathPreview.attr('src', "");
                $picPathPreview.css("display", "none");

                // initPriceGroup(); //初始化价格分组
                $("#extendPriceDiv").html(""); //填充默认价格组

                $("#supplierId").find("option").remove(); //供应商清空

                triggerPageBtnClick("/media1/mediaList", "mediaListSearchBtn"); //触发媒体管理Tab刷新
                triggerPageBtnClick("/media1/audits", "auditSearchBtn"); //触发媒体审核Tab刷新
                triggerPageBtnClick("/media1/mediaSupplierAuditList", "mediaListSearchBtn"); //触发媒体关系审核Tab刷新
                triggerPageBtnClick("/media1/mediaSupplierList", "mediaListSearchBtn"); //触发媒体关系Tab刷新
            }
        }
    }).done(function (res) {
    }).fail(function (res) {
    });
}

// 下载模板；
function downTemplate() {
    var fileName = $("#mTypeName").val();
    var plateId = $("#plateId").val();
    var standarPlatformFlag = $("#standarPlatformFlag").val() || 0;
    location.href = baseUrl + "/mediaAudit/exportTemplate?standarPlatformFlag=" + standarPlatformFlag + "&plateId=" + plateId + "&fileName=" + fileName;
}

//批量上传
function batchUpload() {
    var index = layer.open({
        type: 1,
        zIndex: 9999,
        content: $("#fileTypeDiv"),
        btn: ['确认上传'],
        area: ['400px', '160px'],
        title: "选择导入文件类型",
        yes: function (t) {
            var fileType = $('#fileTypeDiv').find("input[type='radio']:checked").val();
            if (!fileType) {
                swal("请选择文件类型");
            } else {
                $("#fileType").val(fileType);
                // 清空文件；
                $("#file").val("");
                $("#importModal").modal('toggle');
                layer.close(index);
            }
        }
    });
}

//批量导入提交
function batchImportSub() {
    if (document.getElementById("file").value == "") {
        swal("请选上传excel！");
    } else {
        var filePath = document.getElementById("file").value;
        var fileExt = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
        var flag = false;
        if (fileExt.match(/^(.xls|.xlsx)$/)) {
            flag = true;
        }
        if (flag) {
            startModal("#submitImportBtn");//锁定按钮，防止重复提交
            var formData = new FormData($("#importForm")[0]);
            formData.append("standarPlatformFlag", $("#standarPlatformFlag").val())
            formData.append("plateId", parseInt($("#plateId").val()))
            formData.append("plateName", $("#mTypeName").val())
            formData.append("fileType", $("#fileType").val())
            $.ajax({
                type: "post",
                url: "/mediaAudit/importMedia1",
                data: formData,
                dataType: "json",
                async: true,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    Ladda.stopAll();
                    $("#importModal").modal('hide');
                    if (data.code == 200) {
                        var message = "操作完成。";
                        var messageType = "success";
                        var isHtml = false;
                        if (data.data.message) {
                            message = data.data.message;
                            messageType = "warning";
                            if (data.data.file != null) {
                                message = "<a style='color: red;font-weight: bold;' href='" + data.data.file + "'>" + message + "</a>";
                                isHtml = true;
                            }
                        }
                        swal({
                            title: "提示",
                            text: message,
                            type: messageType,
                            html: isHtml,
                        });
                        triggerPageBtnClick("/media1/mediaList", "mediaListSearchBtn"); //触发媒体管理Tab刷新
                        triggerPageBtnClick("/media1/audits", "auditSearchBtn"); //触发媒体审核Tab刷新
                        triggerPageBtnClick("/media/supplier_manage", "search"); //触发供应商管理Tab刷新
                        triggerPageBtnClick("/media1/mediaSupplierAuditList", "mediaListSearchBtn"); //触发媒体关系审核Tab刷新
                        triggerPageBtnClick("/media1/mediaSupplierList", "mediaListSearchBtn"); //触发媒体关系Tab刷新
                    } else {
                        swal({
                            title: data.msg,
                            type: "error"
                        });
                    }
                },
                error: function (data) {
                    // console.log(data) ;
                    Ladda.stopAll();
                    if (getResCode(data))
                        return;
                }
            });
        } else {
            swal("文件格式不正确，只能上传excel文件！");
        }
    }
}

//供应商必输提示控制
function supplierRequiredTip() {
    /*if(!$("#supplierId").val()){
        $("#supplierId").parent().find("label.error").remove();
        $("#addSupplierBtn").after("<label id='supplierId-error' for='supplierId' class='error' style='display: inline-block;'><i class=\"fa fa-times-circle\"></i>  必填</label>");
    }else{
        $("#supplierId").parent().find("label.error").remove();
    }*/
}

//select2加载供应商信息-此处需要个性化操作，所以写到此处（head.js中的对多选操作有点问题）
function loadMediaSupplier(t) {
    var options = {
        url: "/supplier/listSupplierByPlateCompany",      // 数据接口url
        size: 30, // 每次加载的数据条数
        value: "id",  // 下拉框value字段名称
        name: ["name", "contactor", "phone"], // 下拉框显示字段名称
        selected: null,  // 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
        params: {},
        placeholder: "请选择供应商"
    };
    ajaxSelect2($(t), options);
}

/**
 * ajax获取select2下拉框数据（带鼠标滚动分页）
 * @param obj 下拉框对象；
 * @param options 选项，包含如下字段：
 * url 数据接口url
 * size 每次加载的数据条数
 * name 下拉框显示字段名称
 * value 下拉框value字段名称
 * placeholder 默认显示的文字
 * selected 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
 * formatResult 返回结果回调函数，可以在该回调中，自定义下拉框数据的显示样式，比如：加入图片等
 * templateSelection 选中项回调，该参数必须与formatResult参数搭配使用
 * 注意点1 : 后端接口需返回 data（具体数据）和 total（总页数）两个字段
 * 注意点2 : 两个自定义的回调函数中，必须要把处理结果return回来，如果没有传入formatResult参数，则采用默认的显示样式
 */
function ajaxSelect2(obj, options, formatResult, formatSelected) {
    var value = options["value"];
    var name = options["name"];
    var flag = (typeof formatResult === "function") ? true : false;
    var select2Option = {
        language: "zh-CN",
        multiple: true,                         // 多选，默认false
        allowClear: true,
        placeholder: options["placeholder"] || "请选择",
        ajax: {
            url: options["url"],
            type: "post",
            dataType: "json",
            delay: 250,
            data: function (params) {
                var optionParams = options["params"];
                //动态获取板块类型
                // optionParams.mediaTypeId = $("#mType").val();
                // 搜索框内输入的内容
                optionParams.name = params.term;
                // 当前页
                optionParams.page = params.page || 1;
                // 每页显示多少条记录，默认10条
                optionParams.size = options["size"] || 10;
                // 传递到后端的参数
                return optionParams;
            },
            cache: true,
            processResults: function (res, params) {
                params.page = params.page || 1;
                var cbData = [];
                if (flag) {
                    cbData = res.list;
                } else {
                    if (res.pages >= params.page) {
                        var data = res.list;
                        var len = data.length;
                        var text;
                        for (var i = 0; i < len; i++) {
                            text = data[i][name[0]];
                            for (var j = 1; j < 2; j++) {
                                var flag = false;
                                var plateIds = data[i].plateIds.split(",");
                                if (plateIds) {
                                    for (var b = 0; b < plateIds.length; b++) {
                                        if (mediaUserPlateMap.contains(plateIds[b])) {
                                            flag = true;
                                        }
                                    }

                                }
                                //如果供应商责任人为当前用户，则手机号不加密
                                if ((data[i]["creator"] && data[i]["creator"] == user.id) || (flag && data[i]["flag"])) {
                                    text += "(" + data[i][name[j]] + "-" + (data[i][name[2]] || "") + ")";
                                } else {
                                    text += "(" + data[i][name[j]] + "-" + buildPhone(data[i][name[2]]) + ")";
                                }
                            }
                            cbData.push({"id": data[i][value], "text": text});
                        }
                    }
                }
                return {
                    results: cbData,
                    pagination: {
                        more: params.page < res.total
                    }
                };
            }
        },
        escapeMarkup: function (markup) {
            // 字符转义处理
            return markup;
        },
        // 最少输入N个字符才开始检索，如果想在点击下拉框时加载数据，请设置为 0
        minimumInputLength: 0
    };
    if (flag) {
        select2Option.templateResult = formatResult;
        select2Option.templateSelection = formatSelected;
    }
    var $select = obj;
    $select.select2(select2Option);

    //取消选择（点击清空和再次点击时触发）
    $select.on("select2:unselect", function (e) {
        var currentId = e.params.data.id; //获取当前操作的ID
        $select.find("option").each(function (i, t) {
            if ($(t).val() == currentId) {
                $(t).remove(); //清空时移除当前option
            }
        });
        //判断价格分组是否存在，存在则删除分组，并且保留最后一个分组，否则，删除供应商名称隐藏域
        if (currentPriceGroupHTML) {
            $("#supplierPrice" + currentId).remove();
            /*if($("#extendPriceDiv > div").length > 1){  //超过一个分组，可删除当前供应商分组
                $("#supplierPrice"+currentId).remove();
            }else{  //最后一个分组，则清空所有input值
                $("#supplierPrice"+currentId).find("input").val("0");// 给当前分组供应商赋值
                $("#supplierPrice"+currentId).attr("supplier-id",""); // 给当前分组供应商赋值
                //当仅有一个分组时，分组右上角的删除按钮隐藏
                $("#supplierPrice"+currentId).find("div[name='close']").css("display","none");
            }
            //当删除后，只剩下一个价格分组时，删除按钮隐藏
            if($("#extendPriceDiv > div").length == 1){
                $("#extendPriceDiv").children("div:first-child").find("div[name='close']").css("display","none");//当仅有一个分组时，分组右上角的删除按钮隐藏
            }*/
        } else {
            $("#supplier" + currentId).remove(); //移除供应商名称隐藏域
        }
        // console.log($select.val()); //$select.val() 得到的是select中option中值组成的数组
        supplierRequiredTip(); //供应商必输提示
        reflushGroupBackColor();//设置分组背景颜色
        reflushMediaMinPrice();
    });

    //选择下拉列表值触发
    $select.on("select2:select", function (e) {
        var values = $select.val();
        var currentId = e.params.data.id;
        var currentText = e.params.data.text;
        // var lastIndex = currentText.lastIndexOf("(");
        // currentText = lastIndex != -1 ? currentText.substr(lastIndex+1,(currentText.lastIndexOf(")")-lastIndex-1)) : currentText;
        //判断价格分组是否存在，存在则添加分组，否则，添加供应商名称隐藏域
        if (currentPriceGroupHTML) {
            addGroup("supplierPrice" + currentId); //创建价格分组
            $("#supplierPrice" + currentId).find("input[name='supplierName']").val(currentText || "");// 给当前分组供应商赋值
            $("#supplierPrice" + currentId).find("input[name='supplierName']").attr("title", currentText || "");// 给当前分组供应商赋值
            $("#supplierPrice" + currentId).attr("supplier-id", currentId); // 给当前分组供应商赋值
            $("#extendPriceDiv").find("div[name='close']").css("display", "block"); //所有关闭按钮显示
            /*if(values && values.length > 1){
                addGroup("supplierPrice"+currentId); //创建价格分组
                $("#supplierPrice"+currentId).find("input[name='supplierName']").val(currentText || "");// 给当前分组供应商赋值
                $("#supplierPrice"+currentId).find("input[name='supplierName']").attr("title", currentText || "");// 给当前分组供应商赋值
                $("#supplierPrice"+currentId).attr("supplier-id",currentId); // 给当前分组供应商赋值
                $("#extendPriceDiv").find("div[name='close']").css("display","block"); //所有关闭按钮显示
            }else{
                $("#extendPriceDiv").children("div:first-child").attr("id","supplierPrice"+currentId); //给第一个分组ID重新赋值
                $("#extendPriceDiv").children("div:first-child").attr("supplier-id",currentId); //给第一个分组ID重新赋值
                $("#extendPriceDiv").children("div:first-child").find("input[name='supplierName']").val(currentText || "");//给第一个供应商赋值
                $("#extendPriceDiv").children("div:first-child").find("input[name='supplierName']").attr("title", currentText || "");//给第一个供应商赋值
                $("#extendPriceDiv").children("div:first-child").find("div[name='close']").css("display","none");//当仅有一个分组时，分组右上角的删除按钮隐藏
            }*/
            reflushGroupBackColor();//设置分组背景颜色
        } else {
            $("#extendPriceDiv").append(" <input id=\"supplier" + currentId + "\" value=\"" + currentText + "\" name=\"supplierName\" type=\"hidden\">");
        }
        supplierRequiredTip(); //供应商必输提示
    });
    if (!flag) {
        // 默认选中项设置
        var html = '';
        var values = [];
        var selected = options['selected'];
        if (selected) {
            $.each(selected, function (index, item) {
                values.push(item.id);
                html += '<option value="' + item.id + '">' + item.text + '</option>';
            });
            $select.append(html);
            $select.val(values).trigger('change'); //触发select的改变事件
        }
    }
}

//手机号封装
function buildPhone(value) {
    value = value || "";
    if (value) {
        if (value.length >= 11) {
            var start = value.length > 11 ? "*****" : "****";
            return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
        } else if (value.length >= 3) {
            return value[0] + "***" + value[value.length - 1];
        } else {
            return "**";
        }
    } else {
        return "";
    }
}

//供应的管理
var mediaSupplierManageObj = {
    checkSupplierUrl: "/supplier/checkSupplier",
    addSupplierUrl: "/supplier/addSupplier",
    addSupplierCheckRule: function (modalId) {
        var icon = "<i class='fa fa-times-circle'></i> ";
        $("#" + modalId).find("form").validate({
            rules: {
                phone: {
                    required: true,
                    maxlength: 11,
                    remote: {
                        url: baseUrl + mediaSupplierManageObj.checkSupplierUrl, //后台处理程序
                        type: "post", //数据发送方式
                        dataType: "json", //接受数据格式
                        data: { // 要传递的数据
                            "phone": function () {
                                return $("#" + modalId).find("form input[name='phone']").val();
                            },
                            "name": function () {
                                return $("#" + modalId).find("form input[name='name']").val();
                            },
                            "id": function () {
                                return $("#" + modalId).find("form input[name='id']").val() || "";
                            }
                        },
                        dataFilter: function (data) {
                            data = JSON.parse(data);
                            if (data.code == 200) {
                                return true;
                            } else {
                                $("#" + modalId).find("form input[name='phone']").focus();
                                return false;
                            }
                        }
                    }
                },
            },
            messages: {
                phone: {remote: icon + "很抱歉，供应商联系人和手机已经存在！"},
            }
        });
    },
    resetCompanyNatureBtn: function (modalId) {
        $("#" + modalId).find(".companyBtnCls").removeAttr("btn-white");
        if (!$("#" + modalId).find(".companyBtnCls").hasClass("btn-info")) {
            $("#" + modalId).find(".companyBtnCls").addClass("btn-info");
        }
        $("#" + modalId).find(".personBtnCls").removeAttr("btn-info");
        if (!$("#" + modalId).find(".personBtnCls").hasClass("btn-white")) {
            $("#" + modalId).find(".personBtnCls").addClass("btn-white");
        }
    },
    addSupplierClick: function () {
        //初始化新增页面
        $("#mediaSupplierAddModal").find("form").find("input").removeClass("error");
        $("#mediaSupplierAddModal").find("form").validate().resetForm();
        mediaSupplierManageObj.resetCompanyNatureBtn("mediaSupplierAddModal");
        $("#mediaSupplierAddModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsYes").css("display", "none");
        $("#mediaSupplierAddModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsNo").css("display", "none");
        $("#mediaSupplierAddModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsYes").css("display", "none");
        $("#mediaSupplierAddModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsNo").css("display", "none");
        var inputNameList = ["supplierNature", "standarCompanyFlag", "standarPhoneFlag"];
        $("#mediaSupplierAddModal").find("form input").each(function (i, input) {
            if (inputNameList.contains($(input).attr("name"))) {
                $(input).val("0");
            } else {
                $(input).val("");
            }
        });
        $("#mediaSupplierAddModal").modal("toggle");
    },
    addSupplier: function (t) {
        if (!$(t).closest(".modal-content").find("form").valid()) {
            return;
        }
        startModal("#" + $(t).attr("id"));
        commonObj.requestData(JSON.stringify($(t).closest(".modal-content").find("form").serializeForm()), mediaSupplierManageObj.addSupplierUrl, "post", "json", true, function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                layer.msg("新增供应商联系人成功！", {time: 2000, icon: 6});
                $("#mediaSupplierAddModal").modal("toggle");

                mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
            } else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        }, null, true);
    },
}

//公司筛选
var companyObj = {
    companySearchUrl: "/company/companySearch",
    checkCompanyUrl: "/company/checkCompany",
    currentCompanyName: "",
    firstPageTotal: 0, //第一页查询缓存表数据总数
    natureClick: function (t, supplierNature) {
        $(t).closest("form").find(".companyWrap").html("");
        //发生改变才进行处理
        if (supplierNature != $(t).closest("form").find("input[name='supplierNature']").val()) {
            if (supplierNature == 1) {
                $(t).closest("form").find("input[name='name']").val("个体供应商");
                $(t).closest("form").find("input[name='name']").attr("readonly", true);
                //设置公司名称非标准
                $(t).closest("form").find(".companyTipsYes").hide();
                $(t).closest("form").find(".companyTipsNo").hide();
                $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
            } else {
                $(t).closest("form").find("input[name='name']").val("");
                $(t).closest("form").find("input[name='name']").removeAttr("readonly");
            }
            //改变按钮颜色
            $(t).closest("div").find("button").each(function (i, btn) {
                $(btn).removeClass("btn-info");
                if (!$(btn).hasClass("btn-white")) {
                    $(btn).addClass("btn-white");
                }
            });
            $(t).removeClass("btn-white");
            if (!$(t).hasClass("btn-info")) {
                $(t).addClass("btn-info");
            }
            $(t).closest("form").find("input[name='supplierNature']").val(supplierNature);
        }
    },
    renderCompanyItem: function (page, pageSize, companyList) {
        var html = "";
        if (companyList && companyList.length > 0) {
            $.each(companyList, function (m, company) {
                html += "<div onmousedown='companyObj.chooseCompany(this);' class=\"companyNameItem\" title=\"" + (company.companyName || "") + "\"><span>" + (company.companyName || "") + "</span></div>";
            });
        }
        return html;
    },
    search: function (t) {
        $(t).closest("form").find(".companyWrap").html("");
        var keyword = $(t).closest("form").find("input[name='name']").val();
        if (!keyword) {
            if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
                $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
            }
            return;
        } else {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
        }
        layui.use('flow', function () {
            var flow = layui.flow;
            flow.load({
                elem: $(t).closest("form").find(".companyWrap"),
                isAuto: false,
                done: function (page, next) {
                    //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                    //请求数据
                    var param = {keyword: keyword};
                    param.page = page; //页码
                    param.size = 20; //每页数据条数
                    commonObj.requestData(param, companyObj.companySearchUrl, "post", "json", false, function (data) {
                        //第一页是从缓存表拿数据，记录数据总数
                        if (page == 1) {
                            companyObj.firstPageTotal = data.total;
                        }
                        next(companyObj.renderCompanyItem(page, param.size, data.list), page < data.pages); //如果小于总页数，则继续
                    });
                }
            });
        });
    },
    enterEvent: function (t, event) {
        if ((event.keyCode == '13' || event.keyCode == 13)) {
            companyObj.search(t);
        }
    },
    checkCompany: function (t) {
        var keyword = $(t).closest("form").find("input[name='name']").val();
        if (!keyword) {
            $(t).closest("form").find(".companyTipsYes").hide();
            $(t).closest("form").find(".companyTipsNo").hide();
            $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
            return;
        }
        //如果是个体工商户，不需要校验
        if ($(t).closest("form").find("input[name='supplierNature']").val() != 1) {
            commonObj.requestData({keyword: keyword}, companyObj.checkCompanyUrl, "post", "json", false, function (data) {
                if (data.code == 200) {
                    $(t).closest("form").find(".companyTipsYes").show();
                    $(t).closest("form").find(".companyTipsNo").hide();
                    $(t).closest("form").find("input[name='standarCompanyFlag']").val(1);
                } else {
                    $(t).closest("form").find(".companyTipsYes").hide();
                    $(t).closest("form").find(".companyTipsNo").show();
                    $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
                }
            });
        }
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    chooseCompany: function (t) {
        $(t).closest("form").find("input[name='name']").val($(t).attr("title") || "");
        //隐藏弹出筛选框
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOut: function (t) {
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOver: function (t) {
        //如果有内容则展示
        if ($(t).closest("form").find(".companyWrap").find("div").length > 0) {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
            $(t).closest("form").find("input[name='name']").focus();
        }
    },
    //验证电话号码格式
    checkPhone: function (t) {
        var telPatten = /^[1]([3-9])[0-9]{9}$/;
        if (telPatten.test($(t).val())) {
            $(t).closest("form").find(".phoneTipsYes").show();
            $(t).closest("form").find(".phoneTipsNo").hide();
            $(t).closest("form").find("input[name='standarPhoneFlag']").val(1);
        } else {
            $(t).closest("form").find(".phoneTipsYes").hide();
            $(t).closest("form").find(".phoneTipsNo").show();
            $(t).closest("form").find("input[name='standarPhoneFlag']").val(0);
        }
    },
}

//公共方法
var commonObj = {
    requestData: function (data, url, requestType, dataType, async, callBackFun, callErrorFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if (callErrorFun) {
            param.error = callErrorFun;
        }
        if (contentType) {
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    isMJBZ: function () {
        var roles = user.roles;//获取用户角色
        var isMJBZ = false;//是否媒介政务
        if (roles) {
            for (var i = 0; i < roles.length; i++) {
                if (roles[i].code == 'BZ' && roles[i].type == 'MJ') {
                    isMJBZ = true;
                    break;
                }
            }
        }
        return isMJBZ;
    }
}
