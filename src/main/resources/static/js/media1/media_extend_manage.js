
var mediaPlate = null; //缓存媒体板块
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var extendFormTable = $("#extendFormTable");
        extendFormTable.setGridWidth(extendFormTable.closest('.jqGrid_wrapper').width());

        var extendTermTable = $("#extendTermTable");
        extendTermTable.setGridWidth(extendTermTable.closest('.jqGrid_wrapper').width());
    });

    initPage(0); //默认显示第一个Tab
});

//Tab切换处理事件
function tabChange(index) {
    $(".tabContent").css("display","none");
    if(index == 1){
        $("#extendTerm").css("display","block");
        reflushTermTable($("#mediaPlateId1").val());
    }else {
        $("#extendForm").css("display","block");
        reflushFromTable($("#mediaPlateId").val());
    }
}

//页面初始化函数
function initPage(tabIndex) {
    if(!mediaPlate){
        requestData(null,"/mediaPlate/0","get", "json", false, function (data) {
            mediaPlate = data;
        })
    }
    initFormTable(); //初始化表单表格

    initTermTable(); //初始化条件表格

    renderMediaPlate(tabIndex); //渲染媒体板块
    $("#extendTerm").css("display","none");
}

//初始化表单表格
function initFormTable() {
    var mediaPlateId = $("#mediaPlateId").val() || 1;
    $("#extendFormTable").jqGrid({//2600
        // url: baseUrl + "/mediaForm1/list/" + mediaPlateId,
        datatype: "local",
            mtype: 'get',
            altRows: true,
            altclass: 'bgColor',
            height: "auto",
            page: 1,//第一页
            rownumbers: true,
            setLabel: "序号",
            autowidth: true,//自动匹配宽度
            gridview: true, //加速显示
            cellsubmit: "clientArray",
            viewrecords: true,  //显示总记录数
            multiselectWidth: 25, //设置多选列宽度
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            multiselect: true,
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 10,//每页显示记录数
            rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
            jsonReader: {
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: "id"
            },
            colModel: [
                {
                    name: 'id',
                    label: 'id',
                    editable: true,
                    hidden: true,
                    sorttype: "int",
                    search: true
                },
                {
                    name: 'mediaPlateId',
                    label: 'mediaPlateId',
                    editable: true,
                    hidden: true,
                    sorttype: "int",
                    search: true
                },
                {
                    name: 'sortNo',
                    label: 'sortNo',
                    editable: true,
                    hidden: true,
                    sorttype: "int",
                    search: true
                },
                {
                    name: 'mediaPlateName',
                    label: '媒体板块',
                    sortable: false,
                    editable: true,
                    width: 15,
                    align: "center"
                },
                {
                    name: 'fieldName',
                    label: '字段名称',
                    editable: true,
                    width: 10,
                    align: "center",
                    formatter: function (value, grid, rowData) {
                        if(value && value.indexOf("<n>") > 0){
                            var arr = value.split("<n>");
                            return arr[0] + "&lt;n&gt;" + arr[1];
                        }else{
                            return value;
                        }
                    }
                },
                {
                    name: 'cellCode',
                    label: '列名称',
                    editable: true,
                    width: 10,
                    align: "center",
                    formatter: function (value, grid, rowData) {
                        if(value && value.indexOf("<n>") > 0){
                            var arr = value.split("<n>");
                            return arr[0] + "&lt;n&gt;" + arr[1];
                        }else{
                            return value;
                        }
                    }
                },
                {
                    name: 'cellName',
                    label: '列描述',
                    editable: true,
                    width: 10,
                    align: "center"
                },
                {
                    name: 'typeStr',
                    label: '显示类型',
                    editable: true,
                    width: 10,
                    align: "center",
                    formatter: function (v, options, row) {
                        var text = "";
                        switch (row.type) {
                            case 'text':
                                text = '文本框';
                                break;
                            case 'link':
                                text = '链接';
                                break;
                            case 'radio':
                                text = '单选框';
                                break;
                            case 'checkbox':
                                text = '多选框'
                                break;
                            case 'select':
                                text = '下拉框';
                                break;
                            case 'textarea':
                                text = '文本域';
                                break;
                            case 'date':
                                text = '时间(年月日)';
                                break;
                            case 'datetime':
                                text = '时间(年月日时分秒)';
                                break;
                            case 'time':
                                text = '时间(时分秒 HH:MI:SS)';
                                break;
                            case 'price':
                                text = '价格';
                                break;
                            case 'file':
                                text = '文件';
                                break;
                            case 'number':
                                text = '数字';
                                break;
                        }
                        return text;
                    }
                },
                {
                    name: 'type',
                    label: '显示类型',
                    editable: true,
                    width: 6,
                    sortable: false,
                    hidden: true
                },
                {
                    name: 'sortNo',
                    label: '排序',
                    editable: true,
                    width: 6,
                    sortable: false,
                    align: "center",
                },
                {
                    name: 'disabled',
                    label: '禁用',
                    editable: true,
                    width: 10,
                    sortable: false,
                    align: "center",
                    formatter: function (v, options, row) {
                        return v == true ? "<span class='text-success' >是</span>" : "<span class='text-default' >否</span>";
                    }
                },
                {
                    name: 'dataType',
                    label: '数据类型',
                    editable: true,
                    width: 10,
                    align: "center"
                },
                {
                    name: 'required',
                    label: '必填',
                    width: 5,
                    align: "center",
                    formatter: function (v, options, row) {
                        return v == true ? "<span class='text-success' >是</span>" : "<span class='text-default' >否</span>";
                    }
                },
                {
                    name: 'extendFlag',
                    label: '扩展标识',
                    width: 10,
                    align: "center",
                    hidden: true
                },
                {
                    name: 'climbFlag',
                    label: '爬取标识',
                    width: 10,
                    align: "center",
                    hidden: true
                },
                {
                    name: 'climbFlag1',
                    label: '爬取标识',
                    width: 10,
                    align: "center",
                    formatter: function (v, options, row) {
                        //是否爬取标识：0-仅手工填写、1-仅脚本爬取、2-手工+爬取
                        if(row.climbFlag == 1){
                            return "<span class='text-success' >仅脚本爬取</span>";
                        }else if(row.climbFlag == 2){
                            return "<span class='text-success' >手工+爬取</span>";
                        }else {
                            return "<span class='text-default' >仅手工填写</span>"
                        }
                    }
                },
                {
                    name: 'extendFlag1',
                    label: '扩展标识',
                    width: 10,
                    align: "center",
                    formatter: function (v, options, row) {
                        return row.extendFlag == 1 ? "<span class='text-success' >仅供应商用</span>" : "<span class='text-default' >仅媒体用</span>";
                    }
                },
                {
                    name: 'size',
                    label: '大小',
                    width: 6,
                    align: "center",
                },
                {
                    name: 'maxlength',
                    label: '最大长度',
                    width: 6,
                    align: "center"
                },
                {
                    name: 'minlength',
                    label: '最小长度',
                    width: 6,
                    align: "center"
                },
                {
                    name: 'min',
                    label: '最大值',
                    width: 6,
                    align: "center"
                },
                {
                    name: 'max',
                    label: '最小值',
                    width: 6,
                    align: "center"
                },
                {
                    name: 'dbHtml',
                    label: 'HTML取值',
                    editable: true,
                    width: 20,
                    align: "center",
                },
                {
                    name: 'dbJson',
                    label: 'JSON取值',
                    width: 20,
                    align: "center"
                },
                {
                    name: 'dbSql',
                    label: 'SQL取值',
                    width: 20,
                    align: "center"
                },
                {
                    name: 'rule',
                    label: '规则',
                    editable: true,
                    width: 15,
                    align: "center"
                },
                {
                    name: 'version',
                    hidden: true,
                },
                {
                    name: 'remark',
                    label: "描述",
                    editable: true,
                    sortable: false,
                    width: 15,
                    align: "center"

                    // }, {
                    //     label: '操作',
                    //     width: 8,
                    //     formatter: function (v, options, row) {
                    //         return "<a class='text-warning'>删除</a>";
                    //     }
                }
            ],
            pager: "#extendFormPager",
            viewrecords: true,
            caption: "媒体扩展表单列表",
            hidegrid: false,
            loadComplete: function (data) {
                if (getResCode(data))
                    return;
            }, ondblClickRow: function (rowid, row, iCol, e) {
                var rowData = $(this).jqGrid("getRowData", rowid);
                editMediaForm(rowid, rowData);
            },
            gridComplete: function () {
                $('.i-checks').iCheck({
                    checkboxClass: 'icheckbox_square-green',
                    radioClass: 'iradio_square-green',
                });
            }
        }
    );
    $("#extendFormTable").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $("#extendFormTable").setSelection(4, true);
    var extendFormTable = $("#extendFormTable");
    extendFormTable.setGridWidth(extendFormTable.closest('.jqGrid_wrapper').width());
}

//刷新表单表格
function reflushFromTable(plateId) {
    $("#extendFormTable").emptyGridParam();
    $("#extendFormTable").jqGrid('setGridParam', {
        url: baseUrl + "/mediaForm1/list/" + plateId,
        datatype: "json",
    }).trigger("reloadGrid"); //重新载入
}

//初始化条件表格
function initTermTable() {
    var mediaPlateId = $("#mediaPlateId1").val();
    $("#extendTermTable").jqGrid({
        // url: baseUrl + "/mediaTerm1/list/" + mediaPlateId,
        datatype: "local",
            mtype: 'get',
            altRows: true,
            altclass: 'bgColor',
            height: "auto",
            page: 1,//第一页
            rownumbers: true,
            setLabel: "序号",
            autowidth: true,//自动匹配宽度
            gridview: true, //加速显示
            cellsubmit: "clientArray",
            viewrecords: true,  //显示总记录数
            multiselectWidth: 25, //设置多选列宽度
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            multiselect: true,
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 10,//每页显示记录数
            rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
            jsonReader: {
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: "id"
            },
            colModel: [
                {
                    name: 'id',
                    label: 'id',
                    editable: true,
                    hidden: true,
                    sorttype: "int",
                    search: true
                },
                {
                    name: 'mediaPlateId',
                    label: 'mediaPlateId',
                    editable: true,
                    hidden: true,
                    sorttype: "int",
                    search: true
                },
                {
                    name: 'sortNo',
                    label: 'sortNo',
                    editable: true,
                    hidden: true,
                    sorttype: "int",
                    search: true
                },
                {
                    name: 'mediaPlateName',
                    label: '媒体板块',
                    sortable: false,
                    editable: true,
                    width: 15,
                    align: "center"
                },
                {
                    name: 'fieldName',
                    label: '字段名称',
                    editable: true,
                    width: 10,
                    align: "center"
                },
                {
                    name: 'cell',
                    label: '列名称',
                    editable: true,
                    width: 10,
                    align: "center"
                },
                {
                    name: 'cellName',
                    label: '列描述',
                    editable: true,
                    width: 10,
                    align: "center"
                },
                {
                    name: 'typeStr',
                    label: '显示类型',
                    editable: true,
                    width: 10,
                    align: "center",
                    formatter: function (v, options, row) {
                        var text = "";
                        switch (row.type) {
                            case 'text':
                                text = '文本框';
                                break;
                            case 'radio':
                                text = '单选框';
                                break;
                            case 'checkbox':
                                text = '多选框'
                                break;
                            case 'select':
                                text = '下拉框';
                                break;
                            case 'textarea':
                                text = '文本域';
                            case 'date':
                                text = '时间(年月日)';
                                break;
                            case 'datetime':
                                text = '时间(年月日时分秒)';
                                break;
                            case 'time':
                                text = '时间(时分秒 HH:MI:SS)';
                                break;
                            case 'price':
                                text = '价格';
                                break;
                            case 'file':
                                text = '文件';
                                break;
                            case 'number':
                                text = '数字';
                                break;
                        }
                        return text;
                    }
                },
                {
                    name: 'type',
                    label: '显示类型',
                    editable: true,
                    width: 6,
                    sortable: false,
                    hidden: true
                },
                {
                    name: 'sortNo',
                    label: '排序',
                    editable: true,
                    width: 6,
                    sortable: false,
                    align: "center",
                },
                {
                    name: 'dataType',
                    label: '数据类型',
                    editable: true,
                    width: 10,
                    align: "center"
                },
                {
                    name: 'dbHtml',
                    label: 'HTML取值',
                    editable: true,
                    width: 20,
                    align: "center",
                },
                {
                    name: 'dbJson',
                    label: 'JSON取值',
                    width: 20,
                    align: "center"
                },
                {
                    name: 'dbSql',
                    label: 'SQL取值',
                    width: 20,
                    align: "center"
                },
                {
                    name: 'remark',
                    label: "描述",
                    editable: true,
                    sortable: false,
                    width: 15,
                    align: "center"
                }
            ],
            pager: "#extendTermPager",
            viewrecords: true,
            caption: "媒体条件列表",
            hidegrid: false,
            loadComplete: function (data) {
                if (getResCode(data))
                    return;
            }, ondblClickRow: function (rowid, row, iCol, e) {
                var rowData = $(this).jqGrid("getRowData", rowid);
                editMediaTerm(rowid, rowData);
            },
            gridComplete: function () {
                $('.i-checks').iCheck({
                    checkboxClass: 'icheckbox_square-green',
                    radioClass: 'iradio_square-green',
                });
            }
        }
    );
    $("#extendTermTable").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $("#extendTermTable").setSelection(4, true);
    var extendTermTable = $("#extendTermTable");
    extendTermTable.setGridWidth(extendTermTable.closest('.jqGrid_wrapper').width());
}

//刷新表单表格
function reflushTermTable(plateId) {
    $("#extendTermTable").emptyGridParam();
    $("#extendTermTable").jqGrid('setGridParam', {
        url: baseUrl + "/mediaTerm1/list/" + plateId,
        datatype: "json",
    }).trigger("reloadGrid"); //重新载入
}

//渲染页面媒体板块
function renderMediaPlate(tabIndex) {
    if (!mediaPlate) {
        swal("没有板块可操作！", "没有查询到板块信息，请联系管理员赋权！", "warning");
        return;
    }
    var standardHtml = "";
    var notStandardHtml = "";
    $(mediaPlate).each(function (i, item) {
        if (item.standarPlatformFlag == 1) {
            standardHtml += "<div style='width: 10%;float: left;'><span style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;width: 100%;' class='btn btn-outline plateSpan' title='" + item.name + "' data-value='" + item.id + "' data-standarPlatformFlag='" + (item.standarPlatformFlag || 0) + "' onclick='setType(" + item.id + ",this)'>" + item.name + "</span></div>";
        } else {
            notStandardHtml += "<div style='width: 10%;float: left;'><span style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;width: 100%;' class='btn btn-outline plateSpan' title='" + item.name + "' data-value='" + item.id + "' data-standarPlatformFlag='" + (item.standarPlatformFlag || 0) + "' onclick='setType(" + item.id + ",this)'>" + item.name + "</span></div>";
        }
    });

    //判断标准和非标准是否有，控制隐藏显示
    if (standardHtml) {
        $("#extendFormStandardPlateWrap").css("display", "flex");
        $("#extendFormStandardPlate").html(standardHtml);
        $("#extendTermStandardPlateWrap").css("display", "flex");
        $("#extendTermStandardPlate").html(standardHtml);
    } else {
        $("#extendFormStandardPlateWrap").css("display", "none");
        $("#extendFormStandardPlate").html("");
        $("#extendTermStandardPlateWrap").css("display", "none");
        $("#extendTermStandardPlate").html("");
    }
    if (notStandardHtml) {
        $("#extendFormNotStandardPlateWrap").css("display", "flex");
        $("#extendFormNotStandardPlate").html(notStandardHtml);
        $("#extendTermNotStandardPlateWrap").css("display", "flex");
        $("#extendTermNotStandardPlate").html(notStandardHtml);
    } else {
        $("#extendFormNotStandardPlateWrap").css("display", "none");
        $("#extendFormNotStandardPlate").html("");
        $("#extendTermNotStandardPlateWrap").css("display", "none");
        $("#extendTermNotStandardPlate").html("");
    }

    //如果标准有值，则先查询标准的
    if (standardHtml) {
        changeMediaTypeBackground($("#extendFormStandardPlate > div:first-child > span:first-child")[0]);
        changeMediaTypeBackground($("#extendTermStandardPlate > div:first-child > span:first-child")[0]);
        if (tabIndex == 1) {
            $("#extendTermStandardPlate > div:first-child > span:first-child").click();
        } else {
            $("#extendFormStandardPlate > div:first-child > span:first-child").click();
        }
    } else {
        if (notStandardHtml) {
            changeMediaTypeBackground($("#extendFormNotStandardPlate > div:first-child > span:first-child")[0]);
            changeMediaTypeBackground($("#extendTermNotStandardPlate > div:first-child > span:first-child")[0]);
            if (tabIndex == 1) {
                $("#extendTermNotStandardPlate > div:first-child > span:first-child").click();
            } else {
                $("#extendFormNotStandardPlate > div:first-child > span:first-child").click();
            }
        }
    }
}

//媒体板块类型选择事件
function setType(plateId,t) {
    changeMediaTypeBackground(t);
    if ($(t).closest(".plateWrap").attr("tabIndex") == 1) {
        $("#mediaPlateId1").val(plateId);
        reflushTermTable(plateId);
    }else{
        $("#mediaPlateId").val(plateId);
        reflushFromTable(plateId);
    }
}

//改变媒体类型颜色
function changeMediaTypeBackground(t) {
    var standarPlatformFlag = $(t).attr("data-standarPlatformFlag") || 0;
    var backColor = standarPlatformFlag == 1 ? "btn-primary" : "btn-danger";
    $(t).closest(".plateWrap").find(".plateSpan").each(function (i, item) {
        $(item).removeClass("btn-primary");
        $(item).removeClass("btn-danger");
        if (t == item) {
            $(t).addClass(backColor);
        }
    });
    if ($(t).closest(".plateWrap").attr("tabIndex") == 1) {
        $("#mediaPlateId1").val($(t).attr("data-value"));
    }else{
        $("#mediaPlateId").val($(t).attr("data-value"));
    }
}

//数据类型下拉列表改变事件
function loadDataType(t) {
    var id = $(t).parents("form").attr("id");
    var val = $(t).val();
    var dataDB = "";
    var dataDiv = "";
    var dataLabel = "";
    if(id == "mediaForm"){
        dataDB = "#dataDB";
        dataDiv = "#dataDiv";
        dataLabel = "#dataLabel";
    }else if(id == "editMediaForm"){
        dataDB = "#dataDB1";
        dataDiv = "#dataDiv1";
        dataLabel = "#dataLabel1";
    }else if(id == "mediaTerm"){
        dataDB = "#dataDB2";
        dataDiv = "#dataDiv2";
        dataLabel = "#dataLabel2";
    }else{
        dataDB = "#dataDB3";
        dataDiv = "#dataDiv3";
        dataLabel = "#dataLabel3";
    }
    if (val == "") {
        $(dataDB).removeAttr("name");
        $(dataDB).removeAttr("required");
        $(dataDiv).hide();
        return;
    }
    var name = "db" + val.firstUpper();
    var txt = val.toUpperCase() + "内容";
    $(dataLabel).text(txt);
    $(dataDB).attr("name", name);
    $(dataDB).attr("required", "required");
    $(dataDB).attr("placeholder", txt);
    $(dataDiv).show();
}

//新增表单数据
function saveMediaForm() {
    var cellCode = $("#cellCode").val();
    $("#fieldName").val(cellCode);
    if (!$("#mediaForm").valid()) return;
    requestData($("#mediaForm").serializeJson(), "/mediaForm1", "post", "json", true, function (data) {
        if (data.code == 200) {
            swal("成功", "添加成功！", "success");
            $("#mediaForm")[0].reset();
            $("#dataDiv").hide();
            reflushFromTable( $("#mediaPlateId").val());
        }else{
            swal("添加异常", data.msg, "error");
        }
    });
}

//编辑媒体表单
function editMediaForm(rowId, rowData) {
    //初始化下拉列表
    $("#formDataType").val("");

    $("#editMediaFormModal").modal("toggle");
    var dataType = rowData.dataType;
    $("#name").text("-" + rowData.cellName);
    for (var k in rowData) {
        var value = rowData[k];
        if (k != "cellCode" && k != "fieldName" && value.checkHtml()) { //由于列名可能存在 <1>、<n>标识，会被判断成HTML，导致$(value)报错，所以过滤这两项
            value = $(value).text();
        }
        $("#editMediaForm input[class!='i-checks'][name='" + k + "']").val(value);
        $("#editMediaForm textarea[name='" + k + "']").text(value);
        $("#editMediaForm [name='" + k + "'][class='i-checks']").each(function () {
            var val = this.value == 1 ? "是" : "否";
            if (this.value == value || value == val) {
                $(this).iCheck('check');
            }
        });
        $("#editMediaForm select[name='" + k + "'] option").each(function () {
            if (this.value == value) {
                $(this).attr("selected",true);
            } else {
                $(this).removeAttr("selected");
            }
        });
    }
    if (dataType == "") {
        $("#editMediaForm #dataDB1").removeAttr("name");
        $("#editMediaForm #dataDB1").removeAttr("required");
        $("#editMediaForm #dataDiv1").hide();
        return;
    }
    var name = "db" + dataType.firstUpper();
    var txt = dataType.toUpperCase() + "内容";
    $("#editMediaForm #dataLabel1").text(txt);
    $("#editMediaForm select[name='type'] option").each(function () {
        if (this.text == rowData['type']) {
            this.selected = true;
        }
    });
    $("#editMediaForm select[name='dataType']").val(dataType);
    $("#editMediaForm #dataDiv1").show();
    $("#editMediaForm #dataDB1").attr("name", name);
    $("#editMediaForm #dataDB1").attr("required", "required");
    $("#editMediaForm #dataDB1").attr("placeholder", txt);
    $("#editMediaForm #dataDB1").val(rowData[name]);

    //表单渲染
    layui.use('form', function () {
        var form = layui.form;
        form.render();
    });
}

//更新表单数据
function updateMediaForm() {
    var cellCode = $("#editMediaForm input[name='cellCode']").val();
    $("#editMediaForm input[name='fieldName']").val(cellCode);
    var data = $("#editMediaForm").serializeJson();
    data.extendFlag = $("#editMediaForm input[name='extendFlag']:checked").val();
    data.climbFlag = $("#editMediaForm input[name='climbFlag']:checked").val();
    requestData(JSON.stringify(data),"/mediaForm1","put","json",true,function (data) {
        if (data.code == 200) {
            swal({
                title: "成功",
                text: "更新成功！",
                type: "success"
            },function () {
                $("#editMediaFormModal").modal("toggle");
                $("#editMediaForm")[0].reset();
                $("#editMediaForm #dataDiv1").hide();
                reflushFromTable( $("#mediaPlateId").val());
            });
        } else {
            swal("更新失败！", data.msg, "error");
        }
    });
}

//批量删除表单
function batchDelMediaForm() {
    var rowIds = $("#extendFormTable").jqGrid("getGridParam", "selarrrow");
    if(!rowIds || rowIds.length <= 0){
        swal("提示", "请选择要删除的媒体表单数据!", "warning");
        return;
    }
    requestData(JSON.stringify(rowIds), "/mediaForm1", "delete", "json", true, function (data) {
        if (data.code == 200) {
            swal({
                title: "成功",
                text: "删除成功！",
                type: "success"
            },function () {
                reflushFromTable( $("#mediaPlateId").val());
            });
        } else {
            swal("删除失败！", data.msg, "error");
        }
    });
}

//新增条件数据
function saveMediaTerm() {
    var cell = $("#cell").val();
    $("#fieldName1").val(cell);
    if (!$("#mediaTerm").valid()) return;
    requestData($("#mediaTerm").serializeJson(), "/mediaTerm1", "post", "json", true, function (data) {
        if (data.code == 200) {
            swal("成功", "添加成功！", "success");
            $("#mediaTerm")[0].reset();
            $("#dataDiv2").hide();
            reflushTermTable($("#mediaPlateId1").val());
        }else{
            swal("添加异常", data.msg, "error");
        }
    });
}

//编辑媒体条件
function editMediaTerm(rowId, rowData) {
    //初始化下拉列表
    $("#termDataType").val("");

    $("#editMediaTermModal").modal("toggle");
    var dataType = rowData.dataType;
    $("#name1").text("-" + rowData.cellName);
    for (var k in rowData) {
        var value = rowData[k];
        if (value.checkHtml()) {
            value = $(value).text();
        }
        $("#editMediaTerm input[class!='i-checks'][name='" + k + "']").val(value);
        $("#editMediaTerm textarea[name='" + k + "']").text(value);
        $("#editMediaTerm [name='" + k + "'][class='i-checks']").each(function () {
            var val = this.value == 1 ? "是" : "否";
            if (this.value == value || value == val) {
                $(this).iCheck('check');
            }
        });
        $("#editMediaTerm select[name='" + k + "'] option").each(function () {
            if (this.value == value) {
                $(this).attr("selected",true);
            } else {
                $(this).removeAttr("selected");
            }
        });
    }
    if (dataType == "") {
        $("#editMediaTerm #dataDB3").removeAttr("name");
        $("#editMediaTerm #dataDB3").removeAttr("required");
        $("#editMediaTerm #dataDiv3").hide();
        return;
    }
    var name = "db" + dataType.firstUpper();
    var txt = dataType.toUpperCase() + "内容";
    $("#editMediaTerm #dataLabel3").text(txt);
    $("#editMediaTerm select[name='type'] option").each(function () {
        if (this.text == rowData['type']) {
            this.selected = true;
        }
    });
    $("#editMediaTerm select[name='dataType']").val(dataType);
    $("#editMediaTerm #dataDiv3").show();
    $("#editMediaTerm #dataDB3").attr("name", name);
    $("#editMediaTerm #dataDB3").attr("required", "required");
    $("#editMediaTerm #dataDB3").attr("placeholder", txt);
    $("#editMediaTerm #dataDB3").val(rowData[name]);
    //表单渲染
    layui.use('form', function () {
        var form = layui.form;
        form.render();
    });
}

//更新条件数据
function updateMediaTerm() {
    var cell = $("#editMediaTerm input[name='cell']").val();
    $("#editMediaTerm input[name='fieldName']").val(cell);
    var data = $("#editMediaTerm").serializeJson();
    requestData(JSON.stringify(data), "/mediaTerm1", "put", "json", true, function (data) {
        if (data.code == 200) {
            swal({
                title: "成功",
                text: "更新成功！",
                type: "success"
            },function () {
                $("#editMediaTermModal").modal("toggle");
                $("#editMediaTerm")[0].reset();
                $("#editMediaTerm #dataDiv3").hide()
                reflushTermTable($("#mediaPlateId1").val());
            });
        } else {
            swal("更新失败！", data.msg, "error");
        }
    });
}

//批量删除条件
function batchDelMediaTerm() {
    var rowIds = $("#extendTermTable").jqGrid("getGridParam", "selarrrow");
    if(!rowIds || rowIds.length <= 0){
        swal("提示", "请选择要删除的媒体表单数据!", "warning");
        return;
    }
    requestData(JSON.stringify(rowIds), "/mediaTerm1", "delete", "json", true, function (data) {
        if (data.code == 200) {
            swal({ title: "成功", text: "删除成功！", type: "success"
            },function () {
                reflushTermTable($("#mediaPlateId1").val());
            });
        } else {
            swal("删除失败！", data.msg, "error");
        }
    });
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
var requestData = function (data, url, requestType,dataType,async,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun
    });
}