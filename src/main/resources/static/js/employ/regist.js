$(function () {
    // 推荐人信息先要隐藏；
    $(".relative").hide();

    // 默认第一条是最高学历，先隐藏是否最高学历的选项；
    $(".highEdu").hide();
    // 隐藏其他学历的输入框；
    $(".otherEducation").hide();

    // 单选框、多选框美化；
    $(".i-checks").iCheck({
        checkboxClass: "icheckbox_square-green",
        radioClass: "iradio_square-green",
    });

    // 给所有表单增加校验；
    addFormValidate();

    // 表格初始化；
    tableInit();

    // 先隐藏其他的几个表单；
    $("#formDiv > form:first").nextAll().hide();
    // 上一步按钮隐藏；
    $("#stepBackword").hide();

    //初始化公司信息
    loadCompany();

    // 如果是编辑，初始化数据；
    dataInit();
});

//初始化公司信息
function loadCompany() {
    var param = null;
    if(user && user.dept && user.dept.companyCode){
        param = {};
        param.companyCode = user.dept.companyCode;
    }
    layui.use(["form"], function () {
        var html = "<option value=''>请选择申请公司</option>";
        $.ajax({
            url: baseUrl + "/dept/listJTAllCompany",
            type: "get",
            data: param,
            dataType: "json",
            async: false,
            success: function (data) {
                if(data.code == 200 && data.data.result){
                    var resData = data.data.result;
                    $(resData).each(function (i, d) {
                        if(user && user.companyCode == d.code){
                            html += "<option selected value='" + d.code + "'>" + d.name + "</option>";
                        }else {
                            html += "<option value='" + d.code + "'>" + d.name + "</option>";
                        }
                    });
                }
            }
        });
        $("#company").html(html);
        layui.form.render();
    });
}

// 表格初始化；
function tableInit() {
    // 设置表格默认的UI样式；
    $.jgrid.defaults.styleUI = "Bootstrap";
    // 家庭成员表格初始化；
    $("#familyTable").jqGrid({
        datatype: "local",
        autowidth: true,
        height: "auto",
        colModel: [
            {
                name: "index",
                label: "序号",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "famRelation",
                label: "关系",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "famName",
                label: "姓名",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "famAge",
                label: "年龄",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "famBirthday",
                label: "生日",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "famHealth",
                label: "健康状况",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "famUnit",
                label: "单位",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "famProfession",
                label: "职业",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "famDesc",
                label: "备注",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "famOperation",
                label: "操作",
                sortable: false,
                editable: false,
                align: "center"
            }
        ],
        caption: "家庭成员",
    });

    // 教育培训经历表格初始化；
    $("#educationTable").jqGrid({
        datatype: "local",
        autowidth: true,
        height: "auto",
        colModel: [
            {
                name: "index",
                label: "序号",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "eduTime",
                label: "起止年月",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "eduCollege",
                label: "院校名称/培训机构",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "eduLocation",
                label: "学习所在地",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "eduDuration",
                label: "学制/培训时长",
                sortable: false,
                editable: false,
                align: "center",
                formatter: function (value, grid, rowData){
                    if (rowData.eduDuration == "null"){
                        return "";
                    }
                }

            },
            {
                name: "eduMajor",
                label: "专业/内容",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "eduRecord",
                label: "所获学历/资格证书",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "eduHighest",
                label: "是否最高学历",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "eduOperation",
                label: "操作",
                sortable: false,
                editable: false,
                align: "center"
            }
        ],
        caption: "教育/培训经历",
    });

    // 工作经历表格初始化；
    $("#experienceTable").jqGrid({
        datatype: "local",
        autowidth: true,
        height: "auto",
        colModel: [
            {
                name: "index",
                label: "序号",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "expTime",
                label: "起止年月",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "expCompany",
                label: "公司名称",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "expLocation",
                label: "地点",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "expProfession",
                label: "职务",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "expSalary",
                label: "薪资待遇",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "expContactor",
                label: "证明人及联系电话",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "expResignReason",
                label: "离职原因",
                sortable: false,
                editable: false,
                align: "center"
            },
            {
                name: "expOperation",
                label: "操作",
                sortable: false,
                editable: false,
                align: "center"
            }
        ],
        caption: "工作经历",
    });
}

// 页面数据初始化；
function dataInit() {
    alertMessage("初始化中，请稍候。");
    // 编辑，获取已录入的数据；
    // 获取申请入职的信息；
    var id = getQueryString("id");
    var code = getQueryString("code");
    if ((id && id.length > 0) || (code && code.length > 0)) {
        var url = "";
        var recordUrl = "";
        var params = {};
        if (id && id.length > 0) {
            url = baseUrl + "/entryManage/getEntryInfo";
            recordUrl = "/record?id=" + id;
            params = {id: id};
            // 设置访问的类型，用于表单最后一个完成按钮的跳转链接判定；
            $("#entryType").val(0);
        } else {
            url = baseUrl + "/entry/getEntryInfo";
            recordUrl = "/record?code=" + code;
            params = {code: code};
            $("#entryType").val(1);
        }
        $.post(url, params, function (data) {
            var dataValue = data.data.entry;
            if (dataValue == null) {
                $("#viewRecord").hide();
                alertMessage("查询无结果，入职申请可能不存在或审核未通过。");
            } else {
                alertMessage("系统处理中，请稍候。");
                // 显示查看按钮；
                $("#viewRecord").attr("onclick", "window.open('" + recordUrl + "')");
                $("#viewRecord").show();

                // 审核状态；
                var numValue = dataValue.state;
                // 仅允许待审核状态下的数据进行编辑；
                if (numValue == 0) {
                    // 赋值；
                    for (var key in dataValue) {
                        $("#formDiv").find("input[type='text'][name='" + key + "']").val(dataValue[key]);
                        $("#formDiv").find("input[type='hidden'][name='" + key + "']").val(dataValue[key]);
                        $("#formDiv").find("input[type='hidden'][select='" + key + "']").val(dataValue[key]);
                        if("entryCompanyCode" == key || "entryFirstDept" == key){
                            $("#formDiv").find("select[name='"+key+"']").val(dataValue[key]);
                        }
                        if("empDate" == key && dataValue[key]){
                            $("#formDiv input[name='empDate']").val(new Date(dataValue.empDate).format("yyyy-MM-dd"))
                        }
                    }

                    // 照片；
                    $("#loadImage").val(dataValue.entryImage);
                    loadPortrait();

                    // 性别；
                    setRadioChecked("empGender", dataValue.empGender);

                    // 户口性质；
                    setRadioChecked("entryResidence", dataValue.entryResidence);

                    // 婚姻状况；
                    setRadioChecked("entryMarriage", dataValue.entryMarriage);

                    // 出生日期；
                    $("input[name='empBirth']").val(new Date(dataValue.empBirth).format("yyyy-MM-dd"));

                    // 学历；
                    var selectObj = $("#stepOne select[name='empEducation']");
                    selectObj.val(dataValue.empEducation);
                    selectObj.find("option[value='" + dataValue.empEducation + "']").attr("selected", "selected");
                    changeEducation(selectObj);

                    // 家庭成员信息；
                    showTable(dataValue.family, 1);

                    // 教育培训经历；
                    showTable(dataValue.education, 2);

                    // 工作经历；
                    showTable(dataValue.experience, 3);

                    // 驾照信息；
                    setRadioChecked("entryHasLicence", dataValue.entryHasLicence);
                    changeLicence(dataValue.entryHasLicence);

                    // 病史信息；
                    setRadioChecked("entryHasSick", dataValue.entryHasSick);
                    changeSick(dataValue.entryHasSick);

                    // 求职渠道；
                    if (dataValue.entryChannel) {
                        selectObj = $("#stepFive select[name='entryChannel']");
                        selectObj.val(dataValue.entryChannel);
                        selectObj.find("option[value='" + dataValue.entryChannel + "']").attr("selected", "selected");
                        changeChannel(selectObj);
                    }
                } else {
                    alertMessage("该入职申请正在审核中，暂时无法编辑。");
                    setTimeout(function () {
                        window.location.href = recordUrl;
                    }, 1000);
                }
            }
            // 加载基础资源数据；
            loadNationData();
        }, "json");
    } else {
        // 加载基础资源数据；
        loadNationData();
    }
}

// 获取民族数据；
function loadNationData() {
    layui.use(["form"], function () {
        $.get(baseUrl + "/entry/getNation", function (data) {
            var dataValue = data.data.nation;
            if (dataValue == null) {
                getResCode(data);
            } else {
                if (dataValue.length > 0) {
                    var oldNation = $("#nationId").val();
                    oldNation = oldNation.length > 0 ? oldNation : dataValue[0].id;
                    var nationObj = $("#stepOne select[name='empRace']");
                    for (var i = 0; i < dataValue.length; i++) {
                        if (oldNation == dataValue[i].id) {
                            nationObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                        } else {
                            nationObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                        }
                    }
                }
                // 初始化；
                layui.form.render();
            }

            // 加载部门数据；
            loadDeptData();

            // 初始化籍贯的省级信息；
            loadProvinceData();
        }, "json");
    });
}

// 加载部门数据；
function loadDeptData() {
    //加载职位
    loadPostData($("#company").val(), $("#entryDept").val());
    // 下拉框的onchange事件；
    layui.form.on("select(deptId)", function (deptData) {
        // 刷新部门的职位数据；
        loadPostData($("#company").val(), deptData.value);
    });
    layui.form.on("select(companyCode)", function (deptData) {
        // 刷新部门的职位数据；
        loadPostData(deptData.value, $("#entryDept").val());
    });
   /* // 初始化部门数据；
    $.get(baseUrl + "/entry/getDept", function (data) {
        var dataValue = data.data.dept;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                var oldDept = $("#deptId").val();
                oldDept = oldDept.length > 0 ? oldDept : dataValue[3].id;
                var deptObj = $("#stepOne select[name='entryDept']");
                for (var i = 0; i < dataValue.length; i++) {
                    if (oldDept == dataValue[i].id) {
                        deptObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        deptObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                }

                // 初始化职位数据；
                loadPostData("XH", 0);
            }

            // 初始化；
            layui.form.render();

            // 下拉框的onchange事件；
            layui.form.on("select(deptId)", function (deptData) {
                // 刷新部门的职位数据；
                loadPostData("XH", deptData.value);
            });
        }
    }, "json");*/
}

// 获取部门的职位数据；
function loadPostData(companyCode, firstDept) {
    $.post(baseUrl + "/entry/listPostByCompanyAndDept", {companyCode: companyCode, firstDept: firstDept}, function (data) {
        var postObj = $("#stepOne select[name='entryProfession']");
        // 先清空；
        postObj.empty();
        var dataValue = data.data.post;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                var oldPost = $("#postId").val();
                oldPost = oldPost.length > 0 ? oldPost : dataValue[0].id;
                for (var i = 0; i < dataValue.length; i++) {
                    if (oldPost == dataValue[i].id) {
                        postObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        postObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                }
            }
            // 初始化；
            layui.form.render();
        }
    }, "json");
}

// 获取籍贯的省级信息；
function loadProvinceData() {
    $.post(baseUrl + "/entry/getDistrict", {areaId: 0}, function (data) {
        var dataValue = data.data.district;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                var oldProvince = $("#provinceId").val();
                oldProvince = oldProvince.length > 0 ? oldProvince : dataValue[13].area_id;
                var provinceObj = $("#stepOne select[name='empNativeProvince']");
                for (var i = 0; i < dataValue.length; i++) {
                    if (oldProvince == dataValue[i].area_id) {
                        provinceObj.append("<option value='" + dataValue[i].area_id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        provinceObj.append("<option value='" + dataValue[i].area_id + "'>" + dataValue[i].name + "</option>");
                    }
                }

                // 初始化市级数据；
                loadCityData(oldProvince);
            }
            // 初始化；
            layui.form.render();

            // 下拉框的onchange事件；
            layui.form.on("select(provinceId)", function (provinceData) {
                // 刷新市级数据；
                loadCityData(provinceData.value);
                // 更新隐藏域；
                setNative();
            });
        }
    }, "json");
}

// 获取籍贯的市级信息；
function loadCityData(areaId) {
    $.post(baseUrl + "/entry/getDistrict", {areaId: areaId}, function (data) {
        var cityObj = $("#stepOne select[name='empNativeCity']");
        // 先清空；
        cityObj.empty();
        var dataValue = data.data.district;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                var oldCity = $("#cityId").val();
                oldCity = oldCity.length > 0 ? oldCity : dataValue[0].area_id;
                for (var i = 0; i < dataValue.length; i++) {
                    if (oldCity == dataValue[i].area_id) {
                        cityObj.append("<option value='" + dataValue[i].area_id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        cityObj.append("<option value='" + dataValue[i].area_id + "'>" + dataValue[i].name + "</option>");
                    }
                }
            }
            // 初始化；
            layui.form.render();

            // 初始化时默认更新一次；
            setNative();

            // 下拉框的onchange事件；
            layui.form.on("select(cityId)", function (cityData) {
                // 更新隐藏域；
                setNative();
            });
        }
    }, "json");
}

// 增加表单校验；
function addFormValidate() {
    // 申请表单的校验；
    $("#formDiv > form").each(function () {
        $(this).validate();
    });
    $("#empCode").blur(function () {
        if($.trim($("#empCode").val()) != "") {
            var date = "";
            if($("#empCode").val().length == 15) {
                date =  '19'+$("#empCode").val().substr(6,2)+'-'+$("#empCode").val().substr(8,2)+'-'+$("#empCode").val().substr(10,2);
                } else if ($("#empCode").val().length == 18) {
                     date =  $("#empCode").val().substr(6,4)+'-'+$("#empCode").val().substr(10,2)+'-'+$("#empCode").val().substr(12,2);
                }
            $("#empBirth").val(date);
        }
    })
    // 身份证的异步校验；
    $("input[name='empCode']").rules("add", {
        remote: {
            url: "/entry/checkInfo",
            type: "post",
            dataType: "json",
            data: {
                entryId: function () {
                    return $("#id").val();
                },
                entryCompanyCode: function () {
                    return $("#company").val();
                }
            }
        }
    });

    // 病史信息；
    $(".sick").hide();
    $("#sickElement").find(".i-checks").on("ifClicked", function () {
        var sick = $(this).find("input").val();
        changeSick(sick);
    });

    // 切换驾照；
    $("#licenceElement").find(".i-checks").on("ifClicked", function () {
        var lin = $(this).find("input").val();
        changeLicence(lin);
    });

    // 获取推荐人信息；
    $(".empRelative").blur(function () {
        var enableCheck = true;
        var params = {};
        $(".empRelative").each(function () {
            var inputValue = $(this).val();
            if (inputValue.length <= 0) {
                enableCheck = false;
            }
            params[$(this).attr("name")] = inputValue;
        });
        if (enableCheck) {
            $.post(baseUrl + "/entry/checkRelative", params, function (data) {
                if (data) {
                    alertMessage("输入的推荐人信息不存在，请核对。");
                }
            }, "json");
        }
    });
}

// 设置单选框；
function setRadioChecked(inputName, inputValue) {
    setFormRadioChecked($("input[name='" + inputName + "']"), inputValue);
}

// 打开文件上传窗口；
function openFileUpload() {
    // 清空文件；
    $("#uploadFile").val("");
    $("#uploadModal").modal({backdrop: "static"});
}

// 开始上传文件；
function beginFileUpload() {
    if ($("#uploadFiles").val() == "") {
        alertMessage("请选择需要上传的文件。");
    } else {
        alertMessage("处理中。");
        uploadFile();
    }
}

// 设置籍贯信息；
function setNative() {
    var nativeStr = "";
    $(".native").each(function () {
        nativeStr += $(this).find("option:selected").text();
    });
    $("input[name='empNative']").val(nativeStr);
}

// 切换学历；
function changeEducation(obj) {
    var edu = $(obj).val();
    // 其他方式，显示自定义的输入框；
    if (edu == 7) {
        $(".otherEducation").show();
        $("input[name='empEducationOther']").rules("add", {required: true});
    } else {
        $(".otherEducation").hide();
        $("input[name='empEducationOther']").rules("remove", "required");
    }
}

// 切换驾照信息；
function changeLicence(dataValue) {
    if (!dataValue || dataValue == 0) {
        $(".licence").show();
        $(".licence").find("input").each(function () {
            $(this).rules("add", {required: true});
        });
    } else {
        $(".licence").hide();
        $(".licence").find("input").each(function () {
            $(this).rules("remove", "required");
        });
    }
}

// 切换病史信息；
function changeSick(dataValue) {
    if (!dataValue || dataValue == 0) {
        $(".sick").hide();
        $("input[name='entrySick']").rules("remove", "required");
    } else {
        $(".sick").show();
        $("input[name='entrySick']").rules("add", {required: true});
    }
}

// 显示输入表单，用于增加单条数据；
function showInputForm(obj) {
    var objForm = $(obj).closest("form");
    // 第一步，清空除了入职申请ID之外的其他数据和校验；
    cleanElementValidate(objForm.find(".formElement"));
    // 第二步，显示输入表单和保存按钮，隐藏成员列表和下一步的按钮；
    objForm.find(".formElement").show();
    objForm.find(".listElement").hide();
    // 第三部，切换按钮显示；
    // 判断是否有忽略属性；
    var ignore = objForm.attr("ignore");
    if (ignore && ignore.length > 0) {
        // 有数据则显示取消按钮，否则显示上一步按钮；
        if (ignore == "true") {
            $("#cancelInput").show();
            $("#stepBackword").hide();
        } else {
            $("#stepBackword").show();
        }
    }
    $("#saveData").show();
    $("#stepForward").hide();
}

// 编辑表格的数据信息；
function editTableData(obj) {
    var objForm = $(obj).closest("form");
    // 先清空数据；
    cleanElementValidate(objForm.find(".formElement"));

    var tableData = JSON.parse($(obj).prev("input").val());
    // 获取值；
    var formElement = objForm.find(".formElement");
    for (var key in tableData) {
        formElement.find("input[type='text'][name='" + key + "']").val(tableData[key]);
        formElement.find("input[type='hidden'][name='" + key + "']").val(tableData[key]);
        if("famRelation" == key){
            formElement.find("select[name='" + key + "']").val(tableData[key]);
        }
    }
    layui.form.render();
    // 切换表单和按钮显示；
    objForm.find(".formElement").show();
    objForm.find(".listElement").hide();
    $("#cancelInput").show();
    $("#saveData").show();
    $("#stepBackword").hide();
    $("#stepForward").hide();
}

// 删除表格信息；
function deleteTableData(obj, entryId, dataId, deleteType) {
    layer.confirm("此操作无法恢复，确认继续吗？", {
        btn: ["确认", "取消"],
        shade: [0.7, '#393D49']
    }, function (index) {
        layer.close(index);
        var deleteUrl = "";
        var param = {};
        var tableElement = null;
        if (deleteType == 1) {
            deleteUrl = baseUrl + "/entry/deleteFamily";
            param = {entryId: entryId, famId: dataId};
            tableElement = $("#familyTable");
        }
        if (deleteType == 2) {
            deleteUrl = baseUrl + "/entry/deleteEducation";
            param = {entryId: entryId, eduId: dataId};
            tableElement = $("#educationTable");
        }
        if (deleteType == 3) {
            deleteUrl = baseUrl + "/entry/deleteExperience";
            param = {entryId: entryId, expId: dataId};
            tableElement = $("#experienceTable");
        }
        if (deleteUrl.length > 0) {
            alertMessage("系统处理中，请稍候。");
            $.post(deleteUrl, param, function (data) {
                // 修改合并列；
                var rowCount = tableElement.find("tr").length;

                // 如果删除了最后一条数据，更新表单状态并显示；
                if (rowCount == 1) {
                    var objForm = $(obj).closest("form");
                    objForm.attr("ignore", "false");
                    // 更新表单状态；
                    showInputForm(obj);
                }

                // 删除整行数据；
                $(obj).closest("tr").remove();

                alertMessage(data.data.message);
            }, "json");
        } else {
            alertMessage("操作类型不存在。");
        }
    }, function () {
        return;
    });
}

// 取消增加或编辑；
function cancelInput() {
    // 获取当前显示的表单对象；
    var obj = $("#formDiv > form:visible");
    $(obj).find(".formElement").hide();
    $(obj).find(".listElement").show();
    $("#cancelInput").hide();
    $("#saveData").hide();
    $("#stepBackword").show();
    $("#stepForward").show();
}

// 显示多记录的表格数据；
function showTable(arrayData, tableType) {
    var dataLength = arrayData.length;
    if (arrayData && dataLength > 0) {
        // 家庭成员信息；
        if (tableType == 1) {
            $("#stepTwo").attr("ignore", "true");

            var familyHtml = createFamilyTable(arrayData, dataLength);

            if (familyHtml.length > 0) {
                // 先移除原来的；
                $("#familyTable > tbody").html("");
                $("#familyTable > tbody").append(familyHtml);
            }
        }

        // 教育培训经历信息；
        if (tableType == 2) {
            $("#stepThree").attr("ignore", "true");

            var educationHtml = createEducationTable(arrayData, dataLength);

            if (educationHtml.length > 0) {
                // 先移除原来的；
                $("#educationTable > tbody").html("");
                $("#educationTable > tbody").append(educationHtml);
            }
        }

        // 工作经历信息；
        if (tableType == 3) {
            $("#stepFour").attr("ignore", "true");

            var experienceHtml = createExperienceTable(arrayData, dataLength);

            if (experienceHtml.length > 0) {
                // 先移除原来的；
                $("#experienceTable > tbody").html("");
                $("#experienceTable > tbody").append(experienceHtml);
            }
        }
    }
}

// 设置最高学历；
function setEducationHighest(obj, entryId, eduId, eduCollege, eduMajor) {
    alertMessage("系统处理中，请稍候。");
    $.post(baseUrl + "/entry/setEducationHighest", {
        entryId: entryId,
        eduId: eduId,
        eduCollege: eduCollege,
        eduMajor: eduMajor
    }, function (data) {
        showTable(data.data.education, 2);
        alertMessage(data.data.message);
    }, "json");
}

// 拼接家庭成员表格信息；
var familyRelationMap = {0:"父亲",1:"母亲",2:"丈夫",3:"妻子",4:"儿子",5:"女儿",6:"哥哥",7:"弟弟",8:"姐姐",9:"妹妹",10:"叔叔",11:"阿姨",12:"舅舅",13:"舅妈",14:"姑姑",15:"姑父"};
function createFamilyTable(family, familyLength) {
    var familyData;
    var familyHtml = "";
    for (var i = 0; i < familyLength; i++) {
        familyData = family[i];
        familyHtml += "<tr><td>" + (i + 1) + "</td>";
        familyHtml += "<td>" + familyRelationMap[familyData.famRelation] + "</td>";
        familyHtml += "<td>" + familyData.famName + "</td>";
        familyHtml += "<td>" + familyData.famAge + "</td>";
        familyHtml += "<td>" + new Date(familyData.famBirthday).format("yyyy-MM-dd") + "</td>";
        familyHtml += "<td>" + (familyData.famHealth ? familyData.famHealth : "") + "</td>";
        familyHtml += "<td>" + familyData.famUnit + "</td>";
        familyHtml += "<td>" + familyData.famProfession + "</td>";
        familyHtml += "<td>" + familyData.famDesc + "</td>";
        familyHtml += '<td><input type="hidden" value=' + "'" + JSON.stringify(familyData) + "'" + '/><a href="#" onclick="editTableData(this);return false;">编辑</a>';
        familyHtml += '&nbsp;|&nbsp;<a href="#" onclick="deleteTableData(this,' + "'" + familyData.entryId + "','" + familyData.famId + "',1" + ');return false;">删除</a></td></tr>';
    }
    return familyHtml;
}

// 拼接教育培训经历表格信息；
function createEducationTable(education, educationLength) {
    var educationData;
    var educationHtml = "";
    for (var i = 0; i < educationLength; i++) {
        educationData = education[i];
        educationData.eduStart = new Date(educationData.eduStart).format("yyyy-MM-dd");
        educationData.eduEnd = new Date(educationData.eduEnd).format("yyyy-MM-dd");
        educationHtml += "<tr><td>" + (i + 1) + "</td>";
        educationHtml += "<td>" + educationData.eduStart + "&nbsp;-&nbsp;" + educationData.eduEnd + "</td>";
        educationHtml += "<td>" + educationData.eduCollege + "</td>";
        educationHtml += "<td>" + educationData.eduLocation + "</td>";
        educationHtml += "<td>" + (educationData.eduDuration ? educationData.eduDuration : "") + "</td>";
        educationHtml += "<td>" + educationData.eduMajor + "</td>";
        educationHtml += "<td>" + educationData.eduRecord + "</td>";
        // 最高学历不允许删除；
        if (educationData.eduHighest == 1) {
            educationHtml += '<td><b style="color: green;">是</b></td>';
            educationHtml += '<td><input type="hidden" value=' + "'" + JSON.stringify(educationData) + "'" + '/><a href="#" onclick="editTableData(this);return false;">编辑</a></tr>';
        } else {
            educationHtml += '<td><b style="color: red;">否</b></td>';
            educationHtml += '<td><input type="hidden" value=' + "'" + JSON.stringify(educationData) + "'" + '/><a href="#" onclick="editTableData(this);return false;">编辑</a>';
            educationHtml += '&nbsp;|&nbsp;<a href="#" onclick="setEducationHighest(this,' + "'" + educationData.entryId + "','" + educationData.eduId + "','" + educationData.eduCollege + "','" + educationData.eduMajor + "'" + ');return false;">设置</a>';
            educationHtml += '&nbsp;|&nbsp;<a href="#" onclick="deleteTableData(this,' + "'" + educationData.entryId + "','" + educationData.eduId + "',2" + ');return false;">删除</a></td></tr>';
        }
    }
    return educationHtml;
}

// 拼接教育培训经历表格信息；
function createExperienceTable(experience, experienceLength) {
    var experienceData;
    var experienceHtml = "";
    for (var i = 0; i < experienceLength; i++) {
        experienceData = experience[i];
        experienceData.expStart = new Date(experienceData.expStart).format("yyyy-MM-dd");
        experienceData.expEnd = new Date(experienceData.expEnd).format("yyyy-MM-dd");
        experienceHtml += "<tr><td>" + (i + 1) + "</td>";
        experienceHtml += "<td>" + experienceData.expStart + "&nbsp;-&nbsp;" + experienceData.expEnd + "</td>";
        experienceHtml += "<td>" + experienceData.expCompany + "</td>";
        experienceHtml += "<td>" + experienceData.expLocation + "</td>";
        experienceHtml += "<td>" + experienceData.expProfession + "</td>";
        experienceHtml += "<td>" + experienceData.expSalary + "</td>";
        experienceHtml += "<td>" + experienceData.expContactor + "</td>";
        experienceHtml += "<td>" + experienceData.expResignReason + "</td>";
        experienceHtml += '<td><input type="hidden" value=' + "'" + JSON.stringify(experienceData) + "'" + '/><a href="#" onclick="editTableData(this);return false;">编辑</a>';
        experienceHtml += '&nbsp;|&nbsp;<a href="#" onclick="deleteTableData(this,' + "'" + experienceData.entryId + "','" + experienceData.expId + "',3" + ');return false;">删除</a></td></tr>';
    }
    return experienceHtml;
}

// 推荐人；
var placeholderMap = {1:"请输入社交媒体名称", 3:"请输入人才市场名称", 4:"请输入校园名称",5:"请输入猎头公司名称/猎头", 7:"请输入招聘渠道", 10:"请输入子公司名称"}
function changeChannel(obj) {
    var channel = $(obj).val();
    //0-BOSS/BOSS直聘，1-社交媒体，2-离职再入职，3-人才市场，4-校园招聘，5-猎头推荐，6-内部推荐 ，7-其他，8-前程/前程无忧，9-智联/智联招聘、10-分子公司调岗
    if(channel == 1 || channel == 3 || channel == 4 || channel == 5 || channel == 6 || channel == 7 || channel == 10){
        $("input[name='entryChannelName']").attr("placeholder", placeholderMap[channel]);
        $("input[name='entryChannelName']").val($(obj).find("option:selected").text());
        $("input[name='entryChannelName']").attr("readonly", false);
    }else{
        $("input[name='entryChannelName']").val($(obj).find("option:selected").text());
        $("input[name='entryChannelName']").attr("readonly", true);
    }

    // 内部推荐的方式，要显示推荐人信息填写框；
    if (channel == 6) {
        $(".relative").show();
        $(".relative").find("input").each(function () {
            $(this).rules("add", {required: true})
        });
    } else {
        $(".relative").hide();
        $(".relative").find("input").each(function () {
            $(this).rules("remove", "required")
        });
    }
}

// 图片保存的字段名称；
function getUploadField() {
    return "entryImage";
}

// 图片上传的URL；
function getUploadURL() {
    return baseUrl + "/entry/upload";
}

// 上一步；
function stepBackword() {
    // 获取当前显示的表单对象；
    var obj = $("#formDiv > form:visible");
    var objPrev = obj.prev();
    // 如果有下一个；
    if (objPrev && objPrev.length > 0) {
        obj.hide();
        objPrev.show();
        // 显示上一步按钮；
        $("#stepForward").show();

        // 判断是否有忽略属性；
        checkIgnoreState(objPrev);

        // 如果是第一个；
        if (objPrev.attr("id") == $("#formDiv > form:first").attr("id")) {
            $("#stepBackword").hide();
        }
    } else {
        $("#stepBackword").hide();
    }
    $("#stepForward").html('<i class="fa fa-forward"></i>&nbsp;&nbsp;下一步');
}

// 直接下一步；
function ignoreStepForward(obj) {
    var objNext = obj.next();
    // 如果有下一个；
    if (objNext && objNext.length > 0) {
        // 显示上一步按钮；
        $("#stepBackword").show();

        // 如果是最后一个；
        if (objNext.attr("id") == $("#formDiv > form:last").attr("id")) {
            $("#stepForward").html('<i class="fa fa-check"></i>&nbsp;&nbsp;完成');
        } else {
            $("#stepForward").html('<i class="fa fa-forward"></i>&nbsp;&nbsp;下一步');
        }

        // 判断是否有忽略属性；
        checkIgnoreState(objNext);

        // 表单切换；
        obj.hide();
        objNext.show();
    } else {
        // 最后一步直接跳转到查看页面；
        setTimeout(function () {
            var entryType = $("#entryType").val();
            if (entryType == 0) {
                window.location.href = "/record?id=" + $("#id").val();
            } else {
                window.location.href = "/record?code=" + $("#code").val();
            }
        }, 1000);
    }
}

// 切换表单的按钮显示；
function checkIgnoreState(obj) {
    // 判断是否有忽略属性；
    var ignore = obj.attr("ignore");
    // 未填写数据，显示保存按钮；
    if (ignore && ignore.length > 0 && ignore == "false") {
        // 显示表单元素，隐藏列表元素；
        $(obj).find(".formElement").show();
        $(obj).find(".listElement").hide();

        $("#saveData").show();
        $("#stepForward").hide();
    } else {
        $(obj).find(".formElement").hide();
        $(obj).find(".listElement").show();

        $("#saveData").hide();
        $("#stepForward").show();
    }
}

// 下一步；
function stepForward() {
    // 获取当前显示的表单对象；
    var obj = $("#formDiv > form:visible");
    // 判断是否有忽略属性；
    var ignore = obj.attr("ignore");
    if (ignore && ignore.length > 0) {
        ignoreStepForward(obj);
    } else {
        saveData(obj, function () {
            ignoreStepForward(obj);
        });
    }
}

// 多数据的保存；
function saveMultiData() {
    // 获取当前显示的表单对象；
    var obj = $("#formDiv > form:visible");

    // 数据提交成功，需要显示下一步按钮，并切换页面显示；
    saveData(obj, function (dataValue) {
        // 切换表单和按钮显示；
        obj.find(".formElement").hide();
        obj.find(".listElement").show();

        $("#cancelInput").hide();
        $("#saveData").hide();
        $("#stepBackword").show();
        $("#stepForward").show();

        // 根据返回值判断调用的函数；
        // 获取家庭成员信息；
        if (dataValue.famId != null) {
            showTable(dataValue.family, 1);
        }

        // 获取教育培训经历；
        if (dataValue.eduId != null) {
            showTable(dataValue.education, 2);
        }

        // 获取工作经历；
        if (dataValue.expId != null) {
            showTable(dataValue.experience, 3);
        }
    });
}

// 提交数据；
function saveData(obj, callbackFunction) {
    // 如果表单校验通；
    if (obj.valid()) {
        var jsonData = obj.serializeJson();
        //判断是否选择公司和职位
        if("stepOne" == $(obj).attr("id")){
            if(!jsonData.entryCompanyCode){
                layer.msg("请选择申请公司！", {time: 3000, icon: 5});
                return;
            }
            if(!jsonData.entryProfession){
                layer.msg("请选择申请职位！", {time: 2000, icon: 5});
                return;
            }
        }
        //判断是否推荐人正确
        if("stepFive" == $(obj).attr("id")){
            if(jsonData.entryChannel == 6){
                var validFlag = true;
                $.ajax({
                    url: baseUrl + "/entry/checkRelative",
                    type: "post",
                    data: jsonData,
                    dataType: "json",
                    async: false,
                    success: function (data) {
                        validFlag = !data;
                    }
                });
                if(!validFlag){
                    alertMessage("输入的推荐人信息不存在，请核对。");
                    return;
                }
            }
        }

        alertMessage("系统处理中，请稍候。");
        var key = obj.attr("key");
        var params = {};
        // 如果有特殊处理的，转化为Json字符串；
        if (key && key.length > 0) {
            params[key] = JSON.stringify(jsonData);
        } else {
            params = jsonData;
        }
        $.post(baseUrl + obj.attr("action"), params, function (data) {
            // 获取返回的主键信息；
            var dataValue = data.data;
            if (data.code == 200 && dataValue != null) {
                // 更新隐藏域内容；
                for (var key in dataValue) {
                    $("#formDiv").find("input[type='hidden'][name='" + key + "']").val(dataValue[key]);
                }

                // 后台人员手动编辑无需处理；
                var entryType = $("#entryType").val();
                // 如果有状态信息返回，设置求职渠道信息，2为离职员工；
                if (dataValue.state != null && dataValue.state == 2 && entryType == 1) {
                    // 隐藏该行；
                    $(".channel").hide();
                    // 设置选中的值为2；
                    var selectObj = $("#stepFive select[name='entryChannel']");
                    selectObj.val(2);
                    selectObj.find("option[value='2']").attr("selected", "selected");
                    changeChannel(selectObj);
                    // 设置渠道名称；
                    $("input[name='entryChannelName']").val("离职再入职");
                }

                callbackFunction(dataValue);

                alertMessage("信息已保存。");
            } else {
                getResCode(data);
            }
        }, "json");
    } else {
        return;
    }
}