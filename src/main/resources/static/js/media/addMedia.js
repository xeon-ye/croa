var fh = '<div class="form-group col-md-3"><label class="col-sm-4 control-label">${labelName}:</label><div class="col-sm-8"><input type="${type}" name="${name}" placeholder="${labelName}" class="form-control"/></div></div>';
var url = "/media";
$(function () {
    // $.get(baseUrl+"/mediaType/parentId/0?isFlag=true", function (data) {
    $.get(baseUrl + "/mediaPlate/userId", function (data) {  //mediaType/userId
        if (data == null || data == '') {
            swal("没有板块可操作！", "没有查询到板块信息，请联系管理员赋权！", "warning");
            return;
        }
        var html = "";
        $(data).each(function (i, item) {
            html += "<div class='col-md-1'><span class='btn btn-outline btn-success' title='" + item.name + "' onclick='setType(" + item.id + ",this)'>" + item.name + "</span></div>";
        });
        $("#mediaType").html(html);
        $("#mediaType>div:first-child>span:first-child").click();

    }, "json");


    // 设置表格默认的UI样式；
    $.jgrid.defaults.styleUI = 'Bootstrap';
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
        caption: "文件列表",
    });
    $("#fileInfoDiv").hide();
});

function setType(id, t) {
    $(t).parent().parent().find("div>span").each(function (i, item) {
        // console.log(t == item)
        $(item).removeClass("btn-success");
        if (t == item) {
            $(t).addClass("btn-success");
        }
    });
    $("#mType").val(id);
    $("#mTypeName").val($(t).text());
    $.get(baseUrl + "/mediaForm/list/" + id, function (datas) {
        var html = '';
        $(datas).each(function (i, data) {
            if (i % 4 == 0)
                html += '<div class="col-md-12">';
            // var colsm=data.type=="textarea"?"col-sm-4":"col-sm-3";
            html += '<div class="form-group col-sm-3"><label class="col-sm-4 control-label">';
            var required = "";
            var maxLength = data.maxlength ? "minLength=" + data.maxlength : "";
            var minLength = data.minlength ? "minLength=" + data.minlength : "";
            var size = data.size ? "size=" + data.size : "";
            var min = data.min ? "min=" + data.min : "";
            var max = data.max ? "max=" + data.max : "";
            if (data.required == 1) {
                html += '<span class="text-red"> * </span>';
                required = "required";
            }
            html += data.name + ':</label><div class="col-sm-8">';
            switch (data.type) {
                case 'radio':
                    html += (data.html == undefined ? "" : data.html).th('name', data.code).th("required", required);
                    // html += html.th("required", required);
                    break;
                case 'checkbox':
                    html += (data.html == undefined ? "" : data.html).th('name', data.code).th("required", required);
                    // html += html.th("required", required);
                    break;
                case 'select':
                    var sel = (data.html == undefined ? "" : data.html).th('name', data.code);
                    html += sel.th("required", required);
                    if (data.code == "supplierId") {
                        html += '<button type="button" class="btn btn-primary btn-circle glyphicon glyphicon-plus " id="addSupplier" onclick="addSupplier0()"></button>';
                        //console.log('#required'.);
                    }
                    break;
                case 'textarea':
                    html += '<textarea name="${name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                        ' class="form-control" style="resize:none;"></textarea>';
                    break;
                case 'datetime':
                    // html += '<input name="${name}" placeholder="${labelName}" required="${required}" size="${size}" maxlength="${maxlength}" minlength="${minlength}" class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD hh:mm:ss\'})"/>'.th('name', data.code).th('labelName', data.name);
                    html += '<input name="${name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                        ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD hh:mm:ss\'})"/>';
                    break;
                case 'price':
                    // html += '<div class="input-group m-b"><span class="input-group-addon">¥</span><input required="${required}" size="${size}" maxlength="${maxlength}" minlength="${minlength}" type="text" class="form-control" name="${name}" placeholder="${labelName}"> <span class="input-group-addon">.00</span></div>'.th('name', data.code).th('labelName', data.name);
                    html += '<div class="input-group m-b"><span class="input-group-addon">¥</span><input onkeypress="return inPrice(event)" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                        '  type="number" class="form-control" name="${name}" placeholder="${labelName}"> <span class="input-group-addon">.00</span></div>';
                    break;
                case 'file':
                    // html += '<input type="${type}" name="${name}" placeholder="${labelName}" required="${required}"  size="${size}" maxlength="${maxlength}" minlength="${minlength}" class="form-control"/>'.th('name', data.code).th('labelName', data.name).th('type', data.type);
                    html += '<input type="file" name="file" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max + ' class="form-control"/>';
                    break;
                case 'number':
                    // html += '<input type="${type}" name="${name}" placeholder="${labelName}" required="${required}"  size="${size}" maxlength="${maxlength}" minlength="${minlength}" class="form-control"/>'.th('name', data.code).th('labelName', data.name).th('type', data.type);
                    html += '<input onkeypress="return inNum(event)" type="number" name="${name}" id="${name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                        ' class="form-control"/>';
                    break;
                default:
                    // html += '<input type="${type}" name="${name}" id="${name}" placeholder="${labelName}" ' + required + maxLength + minLength + size + min + max +
                    //     ' class="form-control"/>';
                    html += '<input type="${type}" name="${name}" id="${name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                        ' class="form-control"/>';
                    break;
            }
            html = html.th('id', data.code).th('name', data.code).th('labelName', data.name).th('type', data.type);//.th('required', required).th('size', data.size).th('maxlength', data.maxlength).th('minlength', data.minlength);
            // console.log(html);
            if ((i + 1) % 4 == 0) html += '</div></div></div>';
            else html += '</div></div>';
        });
        $("#mediaForm").html(html);
        $("#mediaForm select").each(function () {
            if ($(this).attr("onload"))
                this.onload();
        });
        // radio默认选择否；
        var dataValue;
        $("#mediaForm input[type='radio']").each(function () {
            dataValue = $(this).attr("data-value");
            if (dataValue && dataValue == "${data-value}") {
                $(this).attr("data-value", "1");
                if ($(this).val() == 1) {
                    $(this).iCheck("check");
                }
            }
        });
        $('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green',
        });
    }, "json");
}

/**
 * 新增供应商
 */
function addSupplier0() {
    alertEdit('/media/supplier_edit?op=create&mediaTypeId=' + $("#mType").val(), '新增媒体供应商');
}

/**
 * 媒体保存
 */
function saveMedia() {
    $.get(baseUrl + "/media/isDuplicateForName", {
        mType: $("#mType").val(),
        mediaName: $("#name").val()
    }, function (data) {
        if (data.code == 200) {
            // var mf = $("#mf").serializeJson();
            if ($("#mf").valid()) {
                // $.post(baseUrl+"/media", mf, function (d) {
                //     // if (d.code == 200) {
                //     swal({
                //         title: "提示!",
                //         text: d.code == 200 ? "媒体保存成功！" : d.msg,
                //         type: d.code == 200 ? "success" : "error",
                //     });
                //     // }
                // });
                var formData = new FormData($('#mf')[0]);
                // formData.append('file', $('#file')[0].files[0]);
                $.ajax({
                    url: baseUrl + "/media",
                    type: 'POST',
                    cache: false,
                    data: formData,
                    dataType: "json",
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        swal({
                            title: "提示!",
                            text: data.data.message == null ? "媒体保存成功！" : data.data.message,
                            type: data.data.message == null ? "success" : "error",
                        });
                        $("input[type='radio']").iCheck('uncheck');
                        $("input[type='checkbox']").iCheck('uncheck');
                        $("#mf")[0].reset();
                        $("#mf").find("span").find(".help-block m-b-none").remove();
                        $("#mf").find(".form-control").removeClass("help-block m-b-none");
                        $("#mf").find(".form-group").removeClass("has-error");
                        $("#mf").find(".form-group").removeClass("has-success");
                    }
                }).done(function (res) {
                }).fail(function (res) {
                });
            }
        } else {
            swal({
                title: "提示!",
                text: data.code == 200 ? "媒体不存在，可以新增！" : "媒体已存在，请更换媒体名称？",
                type: data.code == 200 ? "success" : "error",
            });
        }
    }, "json");
}

$.validator.setDefaults({
    highlight: function (element) {
        $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
    },
    success: function (element) {
        element.closest('.form-group').removeClass('has-error').addClass('has-success');
    },
    errorElement: "span",
    errorPlacement: function (error, element) {
        if (element.is(":radio") || element.is(":checkbox")) {
            error.appendTo(element.parent().parent().parent());
        } else {
            error.appendTo(element.parent());
        }
    },
    errorClass: "help-block m-b-none",
    validClass: "help-block m-b-none"
});

$().ready(function () {
    var icon = "<i class='fa fa-times-circle'></i> ";
    $("#mf").validate({
        rules: {
            name: {
                required: true,
                minlength: 2,
                remote: {
                    url: baseUrl + "/media/isDuplicateForName", // 后台处理程序
                    type: "get", // 数据发送方式
                    dataType: "json", // 接受数据格式
                    data: { // 要传递的数据
                        "mType": function () {
                            return $("#mType").val();
                        },
                        "mediaName": function () {
                            return $("#name").val();
                        }
                    },
                    dataFilter: function (data) {
                        data = JSON.parse(data);
                        if (data.code != 200) {
                            $("#name").focus();
                            return false;
                        } else {
                            return true;
                        }

                    }
                }
            },

        },
        messages: {
            name: {minlength: icon + "用户名必须两个字符以上", remote: icon + "该媒体名称已存在，请更换！"},
        }
    });
});

// 下载模板；
function downTemplate() {
    location.href = "/media/downTemplate?mediaType=" + $("#mType").val() + "&mediaTypeName=" + $("#mTypeName").val();
}

// 打开模板上传窗口；
function openUpload() {
    // 清空文件；
    $("#uploadFile").val("");
    $("#importModal").modal({backdrop: "static"});
}

// 开始上传文件；
function beginUpload() {
    if ($("#uploadFile").val() == "") {
        layer.msg("请选择需要上传的文件。");
    } else {
        var filePath = $("#uploadFile").val();
        var regex = /^.+(.XLS|.xls)$/;
        if (regex.test(filePath)) {
            $("#importForm input[name='mediaType']").val($("#mType").val());
            $("#importForm input[name='mediaTypeName']").val($("#mTypeName").val());
            $.ajax({
                type: "post",
                url: "/media/importData",
                data: new FormData($("#importForm")[0]),
                dataType: "json",
                async: true,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    $("#importModal").modal("hide");
                    if (data.code == 200) {
                        var message = "操作完成。";
                        var messageType = "success";
                        var isHtml = false;
                        if (data.data.msg != null) {
                            message = data.data.msg;
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
                    } else {
                        if (getResCode(data)) {
                            return;
                        }
                    }
                },
                error: function (data) {
                    if (getResCode(data)) {
                        return;
                    }
                }
            });
        } else {
            layer.msg("文件格式不正确，请选择Excel文件。");
        }
    }
}

// 打开文件上传窗口；
function openFileUpload() {
    // 清空文件；
    $("#uploadFiles").val("");
    $("#uploadModal").modal({backdrop: "static"});
}

// 开始上传文件；
function beginFileUpload() {
    if ($("#uploadFiles").val() == "") {
        layer.msg("请选择需要上传的文件。");
    } else {
        $.ajax({
            type: "post",
            url: "/media/fileUpload",
            data: new FormData($("#uploadForm")[0]),
            dataType: "json",
            async: true,
            cache: false,
            contentType: false,
            processData: false,
            success: function (data) {
                $("#uploadModal").modal("hide");
                if (data.code == 200) {
                    layer.msg("文件已上传完成，文件信息将显示在页面下方，请注意保存。");
                    var files = data.data.data;
                    if (files != null && files.length > 0) {
                        $("#fileInfoDiv").show();
                        var tableContent = "";
                        var dataLength = $("#fileInfo").find("tr").length;
                        var filePath;
                        for (var i = 0; i < files.length; i++) {
                            filePath = files[i];
                            tableContent += "<tr><td>" + (dataLength + i) + "</td>";
                            tableContent += "<td><a href='" + filePath + "' target='_blank'>" + filePath + "</a></td></tr>";
                        }
                        $("#fileInfo > tbody").append(tableContent);
                    }
                } else {
                    if (getResCode(data)) {
                        return;
                    }
                }
            },
            error: function (data) {
                if (getResCode(data)) {
                    return;
                }
            }
        });
    }
}