var salaryPermissionFlag = false; //是否有补全和查看薪资的权限
var companyMap = {};
$(function () {
    statisticsModal.init();//初始化模态框

    // 查询表单增加校验；
    $("#queryForm").validate();

    // 离职表单增加校验；
    $("#leaveForm").validate();
    // 多选框宽度；
    $("#leaveForm").find("input[type='checkbox']").closest(".i-checks").css("width", "30%");
    // 离职交接表单增加校验；
    $("#leaveConnectForm").validate();

    // 调岗表单增加校验；
    $("#transferForm").validate();
    // 调岗交接表单增加校验；
    $("#transferConnectForm").validate();

    // 编辑表单增加校验；
    $("#editForm").validate();

    // 加载基础资源数据；
    layui.use(["form", "element"], function () {
        // loadNationData();
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
        }, "json");
    });

    // 设置表格默认的UI样式；
    $.jgrid.defaults.styleUI = 'Bootstrap';

    // 窗口拖拽绑定事件；
    $(window).bind('resize', function () {
        var width = $("#employeeTable").closest('.jqGrid_wrapper').width() || $(document).width();
        $('#employeeTable').setGridWidth(width);
        $('#familyTable').setGridWidth($('.layui-tab-content').width());
        $('#educationTable').setGridWidth($('.layui-tab-content').width());
        $('#experienceTable').setGridWidth($('.layui-tab-content').width());
    });

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
            $("#employeeTable").emptyGridParam();
            reloadEmployeeData();
        }
    });

    // 多选框的设置；
    setCheckBox();

    //加载公司信息
    loadCompany();

    // 初始化表格；
    var deptCode = user.dept.code;
    if (deptCode != "GL" && deptCode != "RS" && deptCode != "XZ") {
        loadJGrid(true);
    } else {
        loadJGrid(false);
    }

    //加载tab页
    layui.use('element', function(){
        var element = layui.element;
        element.on('tab(docDemoTabBrief)', function(data){
           if(3 == data.index){
               $("#saveSalary").css("display", "inline-block");
           }else {
               $("#saveSalary").css("display", "none");
           }
        });
    });

    //查询是否有补全和查看薪资的权限
    $.ajax({
        type: "post",
        url: baseUrl + "/employeeManage/getSalaryPermission",
        data: null,
        dataType: 'json',
        async: false,
        success: function (result) {
            if (result.code == 200) {
                salaryPermissionFlag = true;
            } else {
                salaryPermissionFlag = false;
            }
        }
    });
    if(salaryPermissionFlag){
        $("#completionSalaryTab").css("display", "inline-block");
    }else {
        $("#completionSalaryTab").css("display", "none");
    }
});

//初始化公司信息
function loadCompany() {
    $.ajax({
        url: baseUrl + "/dept/listJTAllCompany",
        type: "get",
        data: null,
        dataType: "json",
        async: false,
        success: function (data) {
            if(data.code == 200 && data.data.result){
                var resData = data.data.result;
                $(resData).each(function (i, d) {
                    companyMap[d.code] = d.name;
                });
            }
        }
    });
}

function delFamily() {
    swal({
            title: "确定删除选中数据吗？",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "确定删除！",
            closeOnConfirm: false,
        },
        function () {
            var $familyTable = $("#familyTable");
            var rowids = $familyTable.jqGrid("getGridParam", "selarrrow");     //获取选中行ids
            if (rowids.length === 0) swal({
                title: "请选择需要删除的数据",
                type: "warning"
            });

            var famIds = [];
            for (var i = 0; i < rowids.length; i++) {
                var rowData = $familyTable.jqGrid('getRowData', rowids[i]);
                famIds.push(rowData.famId);
            }

            $.ajax({
                url: baseUrl + "/employeeManage/delFamily?ids=" + famIds,
                type: "DELETE",
                success: function (result) {
                    if (result.code === 200) {
                        swal({
                            title: "删除成功",
                            type: "success"
                        });
                        $familyTable.emptyGridParam();
                        $familyTable.jqGrid('setGridParam').trigger("reloadGrid"); //重新载入
                    } else swal({
                        title: result.msg,
                        type: "error"
                    });
                }
            })
        });
}

function delEducation() {
    swal({
            title: "确定删除选中数据吗？",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "确定删除！",
            closeOnConfirm: false,
        },
        function () {
            var $educationTable = $("#educationTable");
            var rowids = $educationTable.jqGrid("getGridParam", "selarrrow");     //获取选中行ids
            if (rowids.length === 0) swal({
                title: "请选择需要删除的数据",
                type: "warning"
            });

            var eduIds = [];
            for (var i = 0; i < rowids.length; i++) {
                var rowData = $educationTable.jqGrid('getRowData', rowids[i]);
                var eduHighest = $(rowData.eduHighest).attr("value");
                if (eduHighest === '1') {
                    swal({
                        title: "最高学历不允许删除",
                        type: "error"
                    });
                    return;
                }
                eduIds.push(rowData.eduId);
            }

            $.ajax({
                url: baseUrl + "/employeeManage/delEducation?ids=" + eduIds,
                type: "DELETE",
                success: function (result) {
                    if (result.code === 200) {
                        swal({
                            title: "删除成功",
                            type: "success"
                        });
                        $educationTable.emptyGridParam();
                        $educationTable.jqGrid('setGridParam').trigger("reloadGrid"); //重新载入
                    } else swal({
                        title: result.msg,
                        type: "error"
                    });
                }
            })
        });
}

function delExperience() {
    swal({
            title: "确定删除选中数据吗？",
            type: "warning",
            showCancelButton: true,
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "确定删除！",
            closeOnConfirm: false,
        },
        function () {
            var $experienceTable = $("#experienceTable");
            var rowids = $experienceTable.jqGrid("getGridParam", "selarrrow");     //获取选中行ids
            if (rowids.length === 0) swal({
                title: "请选择需要删除的数据",
                type: "warning"
            });
            var expIds = [];
            for (var i = 0; i < rowids.length; i++) {
                var rowData = $experienceTable.jqGrid('getRowData', rowids[i]);
                expIds.push(rowData.expId);
            }

            $.ajax({
                url: baseUrl + "/employeeManage/delExperience?ids=" + expIds,
                type: "DELETE",
                success: function (result) {
                    if (result.code === 200) {
                        swal({
                            title: "删除成功",
                            type: "success"
                        });
                        $experienceTable.emptyGridParam();
                        $experienceTable.jqGrid('setGridParam').trigger("reloadGrid"); //重新载入
                    } else swal({
                        title: result.msg,
                        type: "error"
                    });
                }
            })
        });
}

function completionInfo(empId, entryId) {
    //如果有补全薪资的权限，才能查看
    if(salaryPermissionFlag){
        loadSalary(entryId);
    }
    loadFamilyTable(entryId);
    loadEducationTable(empId);
    loadExperienceTable(empId);
    var $completionModal = $("#completionModal");
    $("#completionEntryId").val(entryId);
    $completionModal.modal({backdrop: "static"});
}

//编辑修改员工薪资
function submitSalary() {
    var $completionSalaryForm = $("#completionSalaryForm");
    if (!$completionSalaryForm.valid()) return false;
    var formData = $completionSalaryForm.serializeJson();
    formData.entryId = $("#completionEntryId").val();
    startModal("#saveSalary");
    $.post(baseUrl + "/employeeManage/saveSalary", formData, function (result) {
        Ladda.stopAll();
        if("string" == typeof(result)){
            result = JSON.parse(result);
        }
        if (result.code === 200) {
            swal({
                title: "操作成功",
                type: "success"
            });
        } else {
            getResCode(result);
        }
    });
}

//编辑修改家庭成员
function submitFamily() {
    var $completionFamilyModal = $("#completionFamilyModal");
    var $completionFamilyForm = $("#completionFamilyForm");
    if (!$completionFamilyForm.valid()) return false;

    var formData = $completionFamilyForm.serializeJson();
    formData.entryId = $("#completionEntryId").val();
    $.post(baseUrl + "/entry/saveFamilyInJob", formData, function (result) {
        if (result.code === 200) {
            swal({
                title: "操作成功",
                type: "success"
            });
        } else swal({
            title: result.msg,
            type: "error"
        });
        var $familyTable = $("#familyTable");
        $familyTable.emptyGridParam();
        $familyTable.jqGrid('setGridParam').trigger("reloadGrid"); //重新载入

        $completionFamilyModal.modal('hide');
    })
}

//编辑修改教育经历
function submitEducation() {
    var $completionEducationModal = $("#completionEducationModal");
    var $completionEducationForm = $("#completionEducationForm");
    if (!$completionEducationForm.valid()) return false;

    var formData = $completionEducationForm.serializeJson();
    formData.eduHighest = $(formData.eduHighest).attr('value');
    formData.entryId = $("#completionEntryId").val();
    $.post(baseUrl + "/entry/saveEducationInJob", formData, function (result) {
        if (result.code === 200) {
            swal({
                title: "操作成功",
                type: "success"
            });
        } else swal({
            title: result.msg,
            type: "error"
        });
        var $educationTable = $("#educationTable");
        $educationTable.emptyGridParam();
        $educationTable.jqGrid('setGridParam').trigger("reloadGrid"); //重新载入

        $completionEducationModal.modal('hide');
    })
}

//编辑修改工作经历
function submitExperience() {
    var $completionExperienceModal = $("#completionExperienceModal");
    var $completionExperienceForm = $("#completionExperienceForm");
    if (!$completionExperienceForm.valid()) return false;

    var formData = $completionExperienceForm.serializeJson();
    formData.eduHighest = $(formData.eduHighest).attr('value');
    formData.entryId = $("#completionEntryId").val();
    $.post(baseUrl + "/entry/saveExperienceInJob", formData, function (result) {
        if (result.code === 200) {
            swal({
                title: "操作成功",
                type: "success"
            });
        } else swal({
            title: result.msg,
            type: "error"
        });
        var $educationTable = $("#experienceTable");
        $educationTable.emptyGridParam();
        $educationTable.jqGrid('setGridParam').trigger("reloadGrid"); //重新载入

        $completionExperienceModal.modal('hide');
    })
}

//编辑家庭成员弹出框
function editFamlily(rowId) { //编辑
    var rowData = $("#familyTable").jqGrid('getRowData', rowId);
    var $completionFamilyModal = $("#completionFamilyModal");
    var inputs = $completionFamilyModal.find("form input");
    for (var i = 0; i < inputs.length; i++) {
        var key = $(inputs[i]).attr("name");
        $(inputs[i]).val(rowData[key]);
    }
    $("#completionFamilyModal #famRelation").val(rowData['famRelation']);
    layui.form.render();

    $completionFamilyModal.modal({backdrop: "static"})
}

//编辑工作经历弹出框
function editExperience(rowId) { //编辑
    var rowData = $("#experienceTable").jqGrid('getRowData', rowId);
    var $completionExperienceModal = $("#completionExperienceModal");

    var inputs = $completionExperienceModal.find("form input");
    for (var i = 0; i < inputs.length; i++) {
        var key = $(inputs[i]).attr("name");
        $(inputs[i]).val(rowData[key]);
    }

    $completionExperienceModal.modal({backdrop: "static"})
}

//编辑教育经历弹出框
function editEducation(rowId) { //编辑
    var rowData = $("#educationTable").jqGrid('getRowData', rowId);
    var $completionEducationModal = $("#completionEducationModal");

    var inputs = $completionEducationModal.find("form input");
    for (var i = 0; i < inputs.length; i++) {
        var key = $(inputs[i]).attr("name");
        $(inputs[i]).val(rowData[key]);
    }

    $completionEducationModal.modal({backdrop: "static"})
}

//设置最高学历
function setHestest(rowId) {
    var $educationTable = $("#educationTable");
    var rowData = $educationTable.jqGrid('getRowData', rowId);

    $.post(baseUrl + "/entry/setEducationHighest", rowData, function (result) {
        if (result.code === 200) {
            swal({
                title: "最高学历设置成功",
                type: "success"
            });
            $educationTable.emptyGridParam();
            $educationTable.jqGrid('setGridParam').trigger("reloadGrid");
        } else swal({
            title: result.msg,
            type: "error"
        });
    });
}

//加载员工薪资
function loadSalary(entryId) {
    $.post(baseUrl + "/employeeManage/getEmploySalary", {entryId:entryId}, function (result) {
        if (result.code === 200) {
            if(result.data.salary){
                $("#completionSalaryForm input").each(function () {
                    $(this).val(result.data.salary[$(this).attr("name")]);
                });
            }
        } else swal({
            title: result.msg,
            type: "error"
        });
    });
}

//家庭成员表格
var familyRelationMap = {0:"父亲",1:"母亲",2:"丈夫",3:"妻子",4:"儿子",5:"女儿",6:"哥哥",7:"弟弟",8:"姐姐",9:"妹妹",10:"叔叔",11:"阿姨",12:"舅舅",13:"舅妈",14:"姑姑",15:"姑父"};
function loadFamilyTable(entryId) {
    // 初始化数据；
    var $familyTable = $("#familyTable");

    $familyTable.jqGrid({
        url: baseUrl + "/entry/family/" + entryId,
        datatype: "json",
        mtype: 'GET',
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 50,
        sortable: "true",
        sortname: "b.edu_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "eduId"
        },
        colModel: [
            {
                name: 'famId',
                index: "famId",
                label: '主键',
                hidden: true,
                sortable: false
            },
            {
                name: 'entryId',
                index: "entryId",
                label: '入职申请ID',
                hidden: true,
                sortable: false
            },
            {
                name: "famRelation",
                label: "关系",
                sortable: false,
                editable: false,
                align: "center",
                hidden: true
            },
            {
                name: "famRelation1",
                label: "关系",
                sortable: false,
                editable: false,
                align: "center",
                formatter: function (value, grid, rowData) {
                    return familyRelationMap[rowData.famRelation];
                }
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
                align: "center",
                formatter: function (value, grid, rowData) {
                    var html = "";
                    html += '<a onclick="editFamlily(' + grid.rowId + ')">编辑</a>&nbsp;';
                    return html;
                }
            }
        ],
        pager: jQuery("#familyPager"),
        viewrecords: true,
        caption: "家庭成员表",
        add: true,
        edit: true,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
            var width = $('.layui-tab-content').width();
            $('#familyTable').setGridWidth(width);
        },
        loadComplete: function (a, b, c) {

        }
    });

    $familyTable.jqGrid('setLabel', 'rn', '', {
        'text-align': 'center',
        'vertical-align': 'middle',
    });
    $familyTable.emptyGridParam();
    $familyTable.jqGrid('setGridParam',{
        url: baseUrl + "/entry/family/" + entryId
    }).trigger("reloadGrid");
}

//教育经历表格
function loadEducationTable(empId) {
    // 初始化数据；
    var $educationTable = $("#educationTable");
    $educationTable.jqGrid({
        url: baseUrl + "/entry/education/" + empId,
        datatype: "json",
        mtype: 'GET',
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 50,
        sortable: "true",
        sortname: "b.edu_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "eduId"
        },
        colModel: [
            {
                name: 'entryId',
                index: "a.entry_id",
                label: '主键',
                hidden: true,
                sortable: false
            },
            {
                name: 'eduId',
                index: "b.edu_id",
                label: '教育经历id',
                hidden: true,
                sortable: false
            },
            {
                name: 'empNum', index: "a.emp_num", editable: true,
                label: '工号', width: 120
            },
            {
                name: 'empName',
                index: "a.emp_name", label: '姓名', width: 120
            },
            {
                name: 'eduStart',
                index: "b.edu_start",
                label: '入学日期',
                width: 160,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd");
                    }else {
                        return "";
                    }
                }
            },
            {
                name: 'eduEnd',
                index: "b.edu_end",
                label: '毕业日期',
                width: 160,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd");
                    }else {
                        return "";
                    }
                }
            },
            {
                name: 'eduCollege',
                index: "b.edu_college",
                label: '院校名称',
                width: 120
            },
            {
                name: 'eduLocation', index: "b.edu_location",
                label: '地点', width: 120
            },
            {
                name: 'eduDuration',
                index: "b.edu_duration",
                label: '学制(年)',
                width: 120
            },
            {
                name: 'eduMajor',
                index: "b.edu_major",
                label: '专业',
                width: 120
            },
            {
                name: 'eduRecord',
                index: "b.edu_record",
                label: '所获学历',
                width: 120
            },
            {
                name: 'eduHighest',
                index: "b.edu_highest",
                label: '是否最高学历',
                width: 90,
                formatter: function (v) {
                    if (v === 1) {
                        return '<label value="1" style="color: red">是</label>';
                    } else {
                        return '<label value="0" style="color: grey">否</label>';
                    }
                }
            },
            {
                name: 'operate', label: "操作", width: 180, sortable: false,
                formatter: function (value, grid, rowData) {
                    var html = "";
                    html += '<a onclick="editEducation(' + grid.rowId + ')">编辑</a>&nbsp;';
                    html += '|&nbsp;<a onclick="setHestest(' + grid.rowId + ')">设置</a>';
                    return html;
                },
            }
        ],
        pager: jQuery("#educationPager"),
        viewrecords: true,
        caption: "教育经历表",
        add: true,
        edit: true,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
            var width = $('.layui-tab-content').width();
            $('#educationTable').setGridWidth(width);
        },
        loadComplete: function (a, b, c) {

        }
    });

    $educationTable.jqGrid('setLabel', 'rn', '', {
        'text-align': 'center',
        'vertical-align': 'middle',
    });
    $educationTable.emptyGridParam();
    $educationTable.jqGrid('setGridParam',{
        url: baseUrl + "/entry/education/" + empId
    }).trigger("reloadGrid");
}

//工作经历表格
function loadExperienceTable(empId) {
    // 初始化数据；
    var $experienceTable = $("#experienceTable");
    $experienceTable.jqGrid({
        url: baseUrl + "/entry/experience/" + empId,
        datatype: "json",
        mtype: 'GET',
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 50,
        sortable: "true",
        sortname: "expId",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "expId"
        },
        colModel: [
            {
                name: 'entryId',
                index: "a.entry_id",
                label: '主键',
                hidden: true,
                sortable: false
            },
            {
                name: 'expId',
                index: "b.exp_id",
                label: '工作经历id',
                hidden: true,
                sortable: false
            },
            {
                name: 'empNum', index: "a.emp_num", editable: true,
                label: '工号', width: 120
            },
            {
                name: 'empName',
                index: "a.emp_name", label: '姓名', width: 120
            },
            {
                name: 'expStart',
                index: "b.exp_start",
                label: '入职日期',
                width: 160,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd");
                    }else {
                        return "";
                    }
                }
            },
            {
                name: 'expEnd',
                index: "b.exp_end",
                label: '离职日期',
                width: 160,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd");
                    }else {
                        return "";
                    }
                }
            },
            {
                name: 'expCompany',
                index: "b.exp_company",
                label: '公司名称',
                width: 120
            },
            {
                name: 'expLocation', index: "b.exp_location",
                label: '地点', width: 120
            },
            {
                name: 'expProfession',
                index: "b.exp_profession",
                label: '职务',
                width: 120
            },
            {
                name: 'expSalary',
                index: "b.exp_salary",
                label: '薪资待遇',
                width: 120
            },
            {
                name: 'expContactor',
                index: "b.exp_contactor",
                label: '联系电话',
                width: 120
            },
            {
                name: 'expResignReason',
                index: "b.exp_resign_reason",
                label: '离职原因',
                width: 120
            },
            {
                name: 'operate', label: "操作", width: 180, sortable: false,
                formatter: function (value, grid, rowData) {
                    var html = "";
                    html += '<a onclick="editExperience(' + grid.rowId + ')">编辑</a>&nbsp;';
                    return html;
                },
            }
        ],
        pager: jQuery("#experiencePager"),
        viewrecords: true,
        caption: "工作经历表",
        add: true,
        edit: true,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
            var width = $('.layui-tab-content').width();
            $('#experienceTable').setGridWidth(width);
        },
        loadComplete: function (a, b, c) {

        }
    });

    $experienceTable.jqGrid('setLabel', 'rn', '', {
        'text-align': 'center',
        'vertical-align': 'middle',
    });
    $experienceTable.emptyGridParam();
    $experienceTable.jqGrid('setGridParam',{
        url: baseUrl + "/entry/experience/" + empId
    }).trigger("reloadGrid");
}

//员工薪资
function trialCal(){
    var totalMoney = 0;
    $(".trial").each(function () {
        var money = $(this).val();
        if (money.length > 0 && !isNaN(parseFloat(money))) {
            totalMoney += parseFloat(money);
        }
    });
    $("#trialTotal").val(totalMoney);
}
function formalCal(){
    var totalMoney = 0;
    $(".formal").each(function () {
        var money = $(this).val();
        if (money.length > 0 && !isNaN(parseFloat(money))) {
            totalMoney += parseFloat(money);
        }
    });
    $("#formalTotal").val(totalMoney);
}
// 调岗前的工资计算；
function beforeCal(){
    var totalMoney = 0;
    $(".before").each(function () {
        var money = $(this).val();
        if (money.length > 0 && !isNaN(parseFloat(money))) {
            totalMoney += parseFloat(money);
        }
    });
    $("#beforeTotal").val(totalMoney);
}
// 调岗后的工资计算；
function afterCal(){
    var totalMoney = 0;
    $(".after").each(function () {
        var money = $(this).val();
        if (money.length > 0 && !isNaN(parseFloat(money))) {
            totalMoney += parseFloat(money);
        }
    });
    $("#afterTotal").val(totalMoney);
}

//统计概况弹窗
var statisticsToggle = {
    getSingleLinkHtml: function (id, value, type) { //获取单个a链接
        var html = "";
        if (id) {
            value = value ? value : id;//如果value为空则展示ID
            html += "<a onclick=\"statisticsToggle.toggleModal(" + id + ",'" + value + "','" + type + "');\">" + value + "</a>";
        }
        return html;
    },
    toggleModal: function (id, name, type) {
        if ("cust" == type) {
            var title = "[" + name + "]-客户统计";
            statisticsModal.loadConfig({enterType: "cust", enterParam: {custId: id}, title: title}); //加载用户配置
        }
        if ("business" == type) {
            var title = "[" + name + "]-业务统计";
            statisticsModal.loadConfig({enterType: "business", enterParam: {currentUserId: id}, title: title}); //加载用户配置
        }
        if ("mediaUser" == type) {
            var title = "[" + name + "]-媒介统计";
            statisticsModal.loadConfig({enterType: "mediaUser", enterParam: {currentUserId: id}, title: title}); //加载用户配置
        }
        if ("mediaType" == type) {
            var title = "[" + name + "]-板块统计";
            statisticsModal.loadConfig({enterType: "mediaType", enterParam: {mediaType: id}, title: title}); //加载用户配置
        }
        if ("media" == type) {
            var title = "[" + name + "]-媒体统计";
            statisticsModal.loadConfig({enterType: "media", enterParam: {mediaId: id}, title: title}); //加载用户配置
        }
        if ("supplier" == type) {
            var title = "[" + name + "]-供应商统计";
            statisticsModal.loadConfig({enterType: "supplier", enterParam: {supplierId: id}, title: title}); //加载用户配置
        }
        $("#statisticsModal").modal("toggle");
    }
};

// 加载表格；
function loadJGrid(flag) {
    // 初始化数据；
    $("#employeeTable").jqGrid({
        url: baseUrl + "/employeeManage/getPageEmployee",
        datatype: "json",
        mtype: 'POST',
        postData: $("#queyrForm").serializeJson(),
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 50,
        sortable: "true",
        sortname: "a.emp_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "emp_id"
        },
        colModel: [
            {
                name: 'emp_id',
                index: "a.emp_id",
                label: '主键',
                hidden: true,
                width: 60,
                sortable: false
            },
            {
                name: 'entry_id',
                index: "b.entry_id",
                label: 'entry表主键',
                hidden: true,
                width: 60,
                sortable: false
            },
            {name: 'emp_num', index: "a.emp_num", label: '工号', width: 120},
            {
                name: 'emp_name',
                index: "a.emp_name",
                label: '姓名',
                width: 120,
                formatter: function (value, grid, rows) {
                    var html = rows.emp_name;
                    var deptCode = user.dept.code;
                    if (deptCode == "GL" || deptCode == "RS" || deptCode == "XZ") {
                        //除人事,行政,总经办外不能看到跳转链接
                        if (rows.user_id) {
                            if (rows.deptCode == 'YW') {
                                html = statisticsToggle.getSingleLinkHtml(rows.user_id, rows.emp_name, "business");
                            }
                            if (rows.deptCode == 'MJ') {
                                html = statisticsToggle.getSingleLinkHtml(rows.user_id, rows.emp_name, "mediaUser");
                            }
                        }
                    }
                    return html;
                }
            },
            {
                name: 'emp_date',
                index: "b.emp_date",
                label: '入职日期',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd");
                    } else {
                        return new Date(rowData.create_time).format("yyyy-MM-dd");
                    }
                }
            },
            {
                name: 'emp_dept',
                index: "a.emp_dept",
                label: '所属部门',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if(rowData.company_code){
                        return rowData.deptName == null ? "" : rowData.deptName + "("+companyMap[rowData.company_code]+")";
                    }else {
                        return rowData.deptName == null ? "" : rowData.deptName;
                    }
                }
            },
            {
                name: 'emp_profession',
                index: "a.emp_profession",
                label: '当前职位',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.postName == null ? "" : rowData.postName;
                }
            },
            {
                name: 'emp_work_year',
                index: "a.emp_work_year",
                label: '司龄',
                width: 90,
                formatter: function (value, grid, rowData) {
                    return value ? value+" 年":"";
                }
            },
            {name: 'emp_native', index: "b.emp_native", label: '籍贯', width: 120},
            {
                name: 'emp_gender',
                index: "b.emp_gender",
                label: '性别',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: red;">女</b>';
                    } else if (value == 1) {
                        return '<b style="color: green;">男</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_race',
                index: "b.emp_race",
                label: '民族',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        // 少数民族颜色为橘色；
                        if (value == 1) {
                            return "<b style='color: green;'>" + rowData.raceName + "</b>";
                        } else {
                            return "<b style='color: purple;'>" + rowData.raceName + "</b>";
                        }
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_marriage',
                index: "a.emp_marriage",
                label: '婚否',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: green;">未婚</b>';
                    } else if (value == 1) {
                        return '<b style="color: red;">已婚</b>';
                    } else if (value == 2) {
                        return '<b style="color: blue;">离婚</b>';
                    } else if (value == 3) {
                        return '<b style="color: black;">丧偶</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_house',
                index: "a.emp_house",
                label: '居住情况',
                width: 100,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: green;">住家</b>';
                    } else if (value == 1) {
                        return '<b style="color: red;">公司宿舍</b>';
                    } else if (value == 2) {
                        return '<b style="color: blue;">租房</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_birth',
                index: "b.emp_birth",
                label: '出生年月',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return value ? new Date(value).format("yyyy-MM-dd") : "";
                }
            },
            {name: 'emp_birthday', index: "b.emp_birthday", label: '农历生日', width: 120},
            {name: 'emp_code', index: "b.emp_code", label: '身份证号码', width: 120, hidden: flag},
            {name: 'emp_phone', index: "a.emp_phone", label: '联系电话', width: 100},
            {name: 'emp_urgent', index: "a.emp_urgent", label: '紧急联系人', width: 100, hidden: flag},
            {name: 'emp_urgent_phone', index: "a.emp_urgent_phone", label: '有效联系电话', width: 100},
            {name: 'emp_relative_name', index: "a.emp_relative_name", label: '推荐人', width: 100},
            {
                name: 'state', index: "a.state", label: '状态', width: 120, formatter: function (value, grid, rowData) {
                    var processState = rowData.process_state;
                    var isNotApprove = !processState || processState < 3;
                    rowData.isNotApprove = isNotApprove;
                    // 有流程进行中，且流程尚未完结，显示为审核中；
                    if (isNotApprove) {
                        if (value == 0) {
                            return '<b style="color: blue;">试用</b>';
                        } else if (value == 1) {
                            return '<b style="color: green;">转正</b>';
                        } else if (value == 2) {
                            return '<b style="color: black;">离职</b>';
                        } else if (value == 3) {
                            return '<b style="color: orange;">准备交接</b>';
                        } else if (value == 4) {
                            return '<b style="color: orangered;">交接中</b>';
                        } else if(value == 5){
                            return '<b style="color: blue;">实习</b>';
                        }else {
                            return '';
                        }
                    } else {
                        return '<b style="color: red;">审核中</b>';
                    }
                }
            },
            {name: 'userName', index: "userName", label: '关联账号', width: 100},
            {
                name: 'operate', label: "操作", width: 400, sortable: false, hidden: flag,
                formatter: function (value, grid, rowData) {
                    var html = "";
                    var userId = rowData.user_id;
                    var empId = rowData.emp_id;
                    var entryId = rowData.entry_id;
                    var state = rowData.state;
                    var isNotApprove = rowData.isNotApprove;
                    // 如果有流程审核状态则有审核记录，因为没有保存流程标识，非审核状态无法查看；
                    var processId = rowData.process_id;
                    if (state != 2) {
                        html += "<a href='#' onclick='completionInfo(" + empId + "," + entryId + ");return false;'>补全</a>&nbsp;|&nbsp;";
                    }
                    if (userId) {
                        // 有流程进行中，且流程尚未完结，显示为审核中；
                        if (isNotApprove) {
                            // 交接中仅显示交接按钮；
                            if (state == 3) {
                                html += '<a href="#" onclick="openConnect(' + "'" + empId + "','" + processId + "'" + ');return false;">交接</a>&nbsp;|&nbsp;';
                            } else {
                                // 离职状态仅显示查看。
                                if (state != 2) {
                                    html += "<a href='#' onclick='openEdit(" + empId + ");return false;'>编辑</a>&nbsp;|&nbsp;";
                                    if (state == 0) {
                                        html += "<a href='#' onclick='openFormal(" + empId + ");return false;'>转正</a>&nbsp;|&nbsp;";
                                    }
                                    html += "<a href='#' onclick='openTransfer(" + empId + ");return false;'>调岗</a>&nbsp;|&nbsp;";
                                    html += "<a href='#' onclick='openLeave(" + empId + ");return false;'>离职</a>&nbsp;|&nbsp;";
                                }
                            }
                        }
                    } else {
                        if (state != 2) {
                            html += "<a href='#' onclick='grantEmployee(" + empId + ");return false;'>授权</a>&nbsp;|&nbsp;";
                            html += "<a href='#' onclick='linkEmployee(" + empId + "," + rowData.emp_dept + ");return false;'>手动关联用户</a>&nbsp;|&nbsp;";
                        }
                    }
                    if (!isNotApprove && processId) {
                        //员工多次调岗和交接流程审批记录分开
                        if(processId == 14 || processId == 15 || processId == 16){
                            //TODO 由于以前流程dataId取得是emp_id, 修改后采用主键ID，所以为了兼容查看以前的审核记录，通过发布版本时间判断
                            if(new Date(rowData.tranCreateTime) < new Date('2020-04-22')){
                                html += '<a href="#" onclick="showProcessHistory(' + "'" + empId + "','" + processId + "'" + ');return false;">记录</a>&nbsp;|&nbsp;';
                            }else {
                                html += '<a href="#" onclick="showProcessHistory(' + "'" + (empId+"_"+rowData.tranId) + "','" + processId + "'" + ');return false;">记录</a>&nbsp;|&nbsp;';
                            }
                        }else {
                            html += '<a href="#" onclick="showProcessHistory(' + "'" + empId + "','" + processId + "'" + ');return false;">记录</a>&nbsp;|&nbsp;';
                        }
                    }
                    html += "<a href='/record?id=" + rowData.entry_id + "' target='_blank'>查看</a>";
                    return html;
                },
            }
        ],
        pager: jQuery("#employeeTableNav"),
        viewrecords: true,
        caption: "员工信息列表",
        add: false,
        edit: false,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");

        },
        loadComplete: function (a, b, c) {
            $("#employeeTable").find("tr").each(function () {
                $(this).children().first().css("width", "50");
            });
            var width = $("#employeeTable").closest('.jqGrid_wrapper').width() || $(document).width();
            $('#employeeTable').setGridWidth(width);
        }
    });

    $("#employeeTable").jqGrid('setLabel', 'rn', '序号', {
        'text-align': 'center',
        'vertical-align': 'middle',
        "width": "50"
    });

    $("#employeeTable").setGridHeight(400);
}

function linkEmployee(empId, empDept) {
    $.get(baseUrl + "/employeeManage/linkEmployee",{empId:empId}, function (result) {
        if (result.code === 200) {
            //存在一个账户含有多个账号的情况
            if(result.data.users!=null && result.data.users.length>0){
                var optionStr = "<option value=''>--请选择--</option>";
                $.each(result.data.users,function (i,user) {
                   optionStr += "<option value='"+user.id+"' data-deptId='"+user.deptId+"' data-name='"+user.name+"'>"+user.deptName+"-"+user.name+"</option> ";
                });
                layer.open({
                    title:result.data.msg,
                    content:'<div class="col-sm-12" style="display:flex;justify-content: center;align-items: center">' +
                        '<label class="col-sm-4">关联用户</label>' +
                        '<div class="col-sm-8">' +
                        '<select name="range" id="range" class="form-control" lay-filter="range">' +
                        optionStr +
                        '</select>' +
                        '</div>' +
                        '</div>'
                    ,area:['360px','200px']
                    ,yes:function (index) {
                        var id = $("#range").val();
                        var userName = $("#range ").find("option:selected").attr("data-name");
                        var deptId = $("#range ").find("option:selected").attr("data-deptId");
                        if(id!="" && id!=null && id!=undefined){
                            $.get("/employeeManage/linkEmpUserId",{userId:id,userName:userName,deptId:deptId, empId:empId,empDept:empDept},function (data) {
                                if(data.code == 200){
                                    layer.msg("关联成功", {time: 2000, icon: 6});
                                }else {
                                    layer.msg(data.msg || "关联失败", {time: 3000, icon: 5});
                                }
                                $("#dataSearch").click();
                            },"json");
                            layer.close(index);
                        }else{
                            layer.msg("请选择关联用户");
                        }
                    }
                })
            }else{
                layer.msg("关联成功", {time: 2000, icon: 6});
            }
            $("#dataSearch").click();
        } else {
            layer.msg(result.msg || "关联失败", {time: 4000, icon: 5});
        }
    });
}

// 重新载入数据；
function reloadEmployeeData() {
    $("#employeeTable").reloadCurrentData(baseUrl + "/employeeManage/getPageEmployee", $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}

// 绑定账号；
function grantEmployee(empId) {
    alertMessage("系统处理中，请稍候。");
    $.post(baseUrl + "/employeeManage/grantEmployee", {empId: empId}, function (data) {
        var message = data.data.message;
        if (message == null) {
            alertMessage(data.msg);
        } else {
            alertMessage(message);
            reloadEmployeeData();
        }
    }, "json");
}

// 打开转正的流程提交窗口；
function openFormal(empId) {
    alertMessage("系统处理中，请稍候。");
    // 重置表单；
    cleanValidate($("#formalForm"));
    $.post(baseUrl + "/employeeManage/getEmployeeFormal", {empId: empId}, function (data) {
        var dataValue = data.data.employee;
        if (dataValue == null) {
            alertMessage("信息不存在，请刷新页面后重试。");
        } else {
            var userId = dataValue["userId"];
            if (userId) {
                $("#formalForm").find("input").each(function () {
                    $(this).val(dataValue[$(this).attr("name")]);
                });
                var empDate = dataValue["empDate"];
                if (empDate) {
                    $("#formalForm input[name='empDate']").val(new Date(empDate).format("yyyy-MM-dd"));
                }
                $("#formalForm input[name='userName']").val(dataValue["empName"]);
                $("#formalModal").modal({backdrop: "static"});
            } else {
                alertMessage("该员工没有绑定系统登录账号，请检查。");
            }
        }
    }, "json");
}

// 提交转正流程审核；
function startFormal() {
    alertMessage("系统处理中，请稍候。");
    startModal("#startFormal");
    $.post(baseUrl + "/employeeManage/startFormal", $("#formalForm").serializeJson(), function (data) {
        Ladda.stopAll();
        var message = data.data.message;
        if (message == null) {
            alertMessage(data.msg);
        } else {
            alertMessage(message);
        }
        $("#formalModal").modal("hide");
        reloadEmployeeData();
    }, "json");
}

// 打开离职的流程提交窗口；
function openLeave(empId) {
    //如果是业务员，打开窗口之前判断是否有未交接的客户
    $.post(baseUrl + "/employeeManage/getCustByEmpId", {empId: empId}, function (data) {
        var custNum = data.data.custNum;
        if (custNum > 0) {
            swal("友情提示!", "该员工还有未交接的客户，不能离职！", "warning");
            return;
        } else {
            alertMessage("系统处理中，请稍候。");
            // 重置表单；
            cleanValidate($("#leaveForm"));
            // 先隐藏其他补充的输入框；
            $("#leaveForm textarea[name='leaveCompanyOther']").closest(".form-group").hide();
            $("#leaveForm textarea[name='leavePersonOther']").closest(".form-group").hide();
            $("#leaveForm textarea[name='otherReasonRemark']").closest(".form-group").hide();
            // 先清空下拉框；
            var userObj = $("#leaveForm select[name='userId']");
            userObj.empty();
            $.post(baseUrl + "/employeeManage/getEmployeeLeave", {empId: empId}, function (data) {
                var dataValue = data.data.employee;
                if (dataValue == null) {
                    alertMessage("信息不存在，请刷新页面后重试。");
                } else {
                    var user = dataValue["user"];
                    if (Object.keys(user).length > 0) {
                        var leaveData = dataValue["leave"];
                        var empState;
                        if (leaveData) {
                            dataValue = leaveData;
                            empState = dataValue["empState"];
                        } else {
                            empState = dataValue["state"];
                        }
                        // 获取信息；
                        $("#leaveForm").find("input[type='text']").each(function () {
                            $(this).val(dataValue[$(this).attr("name")]);
                        });
                        $("#leaveForm").find("input[type='hidden']").each(function () {
                            $(this).val(dataValue[$(this).attr("name")]);
                        });
                        $("#leaveForm").find("textarea").each(function () {
                            $(this).val(dataValue[$(this).attr("name")]);
                        });

                        // 员工状态；
                        if (empState != undefined) {
                            $("#leaveForm").find("input[name='empState']").val(empState);
                            if (empState == 0) {
                                $("#leaveForm").find("input[data='empState']").val("试用期");
                            }
                            if (empState == 5) {
                                $("#leaveForm").find("input[data='empState']").val("实习");
                            }
                            if (empState == 1) {
                                $("#leaveForm").find("input[data='empState']").val("转正");
                            }
                        }

                        // 入职日期；
                        var empDate = dataValue["empDate"];
                        if (empDate) {
                            $("#leaveForm input[name='empDate']").val(new Date(empDate).format("yyyy-MM-dd"));
                        }

                        // 离职日期；
                        var leaveDate = dataValue["leaveDate"];
                        if (leaveDate) {
                            $("#leaveForm input[name='leaveDate']").val(new Date(leaveDate).format("yyyy-MM-dd"));
                        }

                        // 设置单选框；
                        var leaveType = dataValue["leaveType"];
                        if (leaveType == undefined) {
                            leaveType = 1;
                        }
                        // 性质，离职缘由；
                        setFormRadioChecked($("#leaveForm input[name='leaveType']"), leaveType);
                        changeLeaveContent(leaveType);
                        var leaveTypeContent = dataValue["leaveTypeContent"];
                        if (leaveTypeContent) {
                            $("#leaveForm select[name='leaveTypeContent']").find("option[value='" + leaveTypeContent + "']").attr("selected", "selected");
                        }

                        // 设置公司原因复选框；
                        setDataRequired("leaveCompany", "leaveCompanyOther", dataValue["leaveCompany"]);

                        // 设置个人原因复选框；
                        setDataRequired("leavePerson", "leavePersonOther", dataValue["leavePerson"]);

                        // 设置其他原因复选框；
                        setDataRequired("otherReason", "otherReasonRemark", dataValue["otherReason"]);

                        // 审核人；
                        for (var i = 0; i < user.length; i++) {
                            userObj.append("<option value='" + user[i].id + "'>" + user[i].name + "</option>");
                        }
                        // 设置用户名；
                        var userNameObj = $("#leaveForm input[name='userName']");
                        userNameObj.val(user[0].name);

                        // 初始化；
                        layui.form.render();

                        // 下拉框的onchange事件；
                        layui.form.on("select(userId)", function (userData) {
                            // 更新隐藏域；
                            userNameObj.val(getLayUISelectText(userData));
                        });
                        $("#leaveModal").modal({backdrop: "static"});
                    } else {
                        alertMessage("该员工所在的部门没有审批人，请检查。");
                    }
                }
            }, "json");
        }
    }, "json");
}

// 提交离职流程审核；
function startLeave() {
    if ($("#leaveForm").valid()) {
        var formData = $("#leaveForm").serializeJson();
        // 多选内容至少勾选一项；
        var leaveCompany = formData.leaveCompany;
        var leavePerson = formData.leavePerson;
        var otherReason = formData.otherReason;
        if (leaveCompany || leavePerson || otherReason) {
            alertMessage("系统处理中，请稍候。");
            startModal("#startLeave");
            $.post(baseUrl + "/employeeManage/startLeave", $("#leaveForm").serialize(), function (data) {
                Ladda.stopAll();
                var message = data.data.message;
                if (message == null) {
                    alertMessage(data.msg);
                } else {
                    alertMessage(message);
                }
                $("#leaveModal").modal("hide");
                reloadEmployeeData();
            }, "json");
        } else {
            alertMessage("请至少勾选一项原因。");
        }
    }
}

// 打开调岗的流程提交窗口；
function openTransfer(empId) {
    alertMessage("系统处理中，请稍候。");
    // 重置表单；
    cleanValidate($("#transferForm"));
    $.post(baseUrl + "/employeeManage/getEmployeeTransfer", {empId: empId}, function (data) {
        var dataValue = data.data.employee;
        if (dataValue == null) {
            alertMessage("信息不存在，请刷新页面后重试。");
        } else {
            var userId = dataValue["userId"];
            if (userId) {
                var transfer = dataValue["transfer"];
                // 如果已有记录，从数据中获取；

                // 获取员工状态；
                var empState;
                if (transfer) {
                    dataValue = transfer;
                    empState = dataValue["empState"];
                    var state = dataValue["state"];
                    //审核中不允许修改
                    $("#transferForm .before").attr("readonly", "true");
                } else {
                    empState = dataValue["state"];
                    // 获取薪资待遇数据；
                    var salary = dataValue["salary"];
                    if (salary) {
                        var state = dataValue["state"];
                        // 试用期/实习和转正获取的薪资数据不同；
                        if (state == 0 || state == 5) {
                            $("#transferForm input[name='beforeSalary']").val(salary.trialSalary);
                            $("#transferForm input[name='beforePost']").val(salary.trialPost);
                            $("#transferForm input[name='beforePerformance']").val(salary.trialPerformance);
                            $("#transferForm input[name='beforeOther']").val(salary.trialOther);
                        } else {
                            $("#transferForm input[name='beforeSalary']").val(salary.formalSalary);
                            $("#transferForm input[name='beforePost']").val(salary.formalPost);
                            $("#transferForm input[name='beforePerformance']").val(salary.formalPerformance);
                            $("#transferForm input[name='beforeOther']").val(salary.formalOther);
                        }
                        $("#transferForm .before").attr("readonly", "true");
                    } else {
                        $("#transferForm .before").removeAttr("readonly");
                    }
                }
                // 赋值；
                for (var key in dataValue) {
                    $("#transferForm").find("input[name='" + key + "']").val(dataValue[key]);
                    $("#transferForm").find("input[data='" + key + "']").val(dataValue[key]);
                    $("#transferForm").find("textarea[name='" + key + "']").val(dataValue[key]);
                }

                // 员工状态；
                if (empState != undefined) {
                    $("#transferForm").find("input[name='empState']").val(empState);
                    if (empState == 0) {
                        $("#transferForm").find("input[data='empState']").val("试用期");
                    }
                    if (empState == 5) {
                        $("#transferForm").find("input[data='empState']").val("实习");
                    }
                    if (empState == 1) {
                        $("#transferForm").find("input[data='empState']").val("转正");
                    }
                }

                // 入职日期；
                var empDate = dataValue["empDate"];
                if (empDate) {
                    $("#transferForm input[name='empDate']").val(new Date(empDate).format("yyyy-MM-dd"));
                } else {
                    $("#transferForm input[name='empDate']").val(new Date(dataValue["createTime"]).format("yyyy-MM-dd hh:mm:ss"));
                }
                // 执行日期；
                var transDate = dataValue["transDate"];
                if (transDate) {
                    $("#transferForm input[name='transDate']").val(new Date(transDate).format("yyyy-MM-dd"));
                }

                // 审批人；
                $("#transferForm input[name='userId']").val(userId);
                $("#transferForm input[name='userName']").val(dataValue["empName"]);

                // 加载部门、职位数据；
                //loadTransferDept();
                //清除下拉联动框隐藏域数据
                $("#roleId").val("");
                $("#afterDept").val("");
                $("#afterDeptName").val("");
                $("#afterProfession").val("");
                $("#afterProfessionName").val("");

                $("#transferModal").modal({backdrop: "static"});
            } else {
                alertMessage("该员工没有绑定系统登录账号，请检查。");
            }
        }
    }, "json");
}

// 提交离职流程审核；
function startTransfer() {
    if ($("#transferForm").valid()) {
        var formData = $("#transferForm").serializeJson();
        var deptName = formData.afterDeptName;
        var postName = formData.afterProfessionName;
        var roleType = $("#roleType").find("option:selected").val();
        var roleName = $("#roleName").find("option:selected").val();
        if (deptName && postName) {
            if(roleName && roleType){
                formData.roleType=$("#roleType").find("option:selected").text();
                formData.roleName=$("#roleName").find("option:selected").text();
                alertMessage("系统处理中，请稍候。");
                startModal("#startTransfer");
                $.post(baseUrl + "/employeeManage/startTransfer", formData, function (data) {
                    Ladda.stopAll();
                    var message = data.data.message;
                    if (message == null) {
                        alertMessage(data.msg);
                    } else {
                        alertMessage(message);
                    }
                    $("#transferModal").modal("hide");
                    reloadEmployeeData();
                }, "json");
            }else{
                alertMessage("角色类型和角色名称不能为空。");
            }
        } else {
            alertMessage("转入部门和职位不能为空。");
        }
    }
}

// 设置多选框的一些选项；
function setCheckBox() {
    // 单选框、多选框美化；
    $(".i-checks").iCheck({
        checkboxClass: "icheckbox_square-green",
        radioClass: "iradio_square-green",
    });

    // 个人和公司原因切换；
    changeLeaveContent(1);
    $("#typeElement").find(".i-checks").on("ifClicked", function () {
        var leaveType = $(this).find("input").val();
        changeLeaveContent(leaveType);
    });

    // 设置表单选中时关联的输入框必填；
    $("#leaveForm input[type='checkbox']").parent().on("ifChecked", function () {
        var obj = $(this).find("input");
        setRequired(obj.attr("name"), obj.val(), true);
    });
    $("#leaveForm input[type='checkbox']").parent().on("ifUnchecked", function () {
        var obj = $(this).find("input");
        setRequired(obj.attr("name"), obj.val(), false);
    });
}

// 切换个人和公司的离职性质内容；
function changeLeaveContent(leaveType) {
    var selectObj = $("#leaveForm select[name='leaveTypeContent']");
    // 先清空；
    selectObj.empty();
    if (leaveType == 0) {
        selectObj.append("<option value='0'>终止试用</option>");
        selectObj.append("<option value='1'>公司辞退</option>");
        selectObj.append("<option value='2'>终止续签</option>");
    } else {
        selectObj.append("<option value='4'>试用辞退</option>");
        selectObj.append("<option value='5'>个人辞职</option>");
        selectObj.append("<option value='6'>合同期满</option>");
    }
}

// 离职其他原因的处理；
function setDataRequired(inputName, textAreaName, checkData) {
    // 先清空选中；
    $("#leaveForm textarea[name='" + textAreaName + "']").rules("remove", "required");
    var companyArray;
    if (checkData) {
        companyArray = checkData.split(",");
        // 如果有其他选项，设置必填；
        var length = companyArray.length;
        // 设置必填；
        setRequired(inputName, companyArray[length - 1], true);
    }
    // 设置选中；
    setFormCheckBoxChecked($("#leaveForm input[name='" + inputName + "']"), companyArray);
}

// 设置必填；
function setRequired(inputName, inputValue, flag) {
    var processRequired;
    var textAreaElement;
    if (inputName == "leaveCompany") {
        processRequired = inputValue == 8;
        textAreaElement = "leaveCompanyOther";
    }
    if (inputName == "leavePerson") {
        processRequired = inputValue == 7;
        textAreaElement = "leavePersonOther";
    }
    if (inputName == "otherReason") {
        processRequired = inputValue == 3;
        textAreaElement = "otherReasonRemark";
    }
    // 判断操作类型；
    if (flag) {
        if (processRequired) {
            var obj = $("#leaveForm textarea[name='" + textAreaElement + "']");
            obj.rules("add", {required: true});
            obj.closest(".form-group").show();
        }
    } else {
        if (processRequired) {
            var obj = $("#leaveForm textarea[name='" + textAreaElement + "']");
            obj.rules("remove", "required");
            obj.closest(".form-group").hide();
            obj.removeClass("error");
            obj.next("label").remove();
        }
    }
}

// // 加载部门数据；
// function loadTransferDept() {
//     // 初始化部门数据；
//     $.get(baseUrl + "/entry/getDept", function (data) {
//         var dataValue = data.data.dept;
//         if (dataValue == null) {
//             getResCode(data);
//         } else {
//             if (dataValue.length > 0) {
//                 var oldDept = $("#transferForm input[data='afterDept']").val();
//                 oldDept = oldDept.length > 0 ? oldDept : dataValue[0].id;
//                 var oldDeptName;
//                 var deptObj = $("#transferForm select[name='afterDept']");
//                 for (var i = 0; i < dataValue.length; i++) {
//                     if (oldDept == dataValue[i].id) {
//                         oldDeptName = dataValue[i].name;
//                         deptObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
//                     } else {
//                         deptObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
//                     }
//                 }
//                 // 初始化职位数据；
//                 loadTransferPost(oldDept);
//
//                 // 设置部门名称；
//                 var deptNameObj = $("#transferForm input[name='afterDeptName']");
//                 if (!oldDeptName) {
//                     oldDeptName = dataValue[0].name;
//                 }
//                 deptNameObj.val(oldDeptName);
//             }
//
//             // 初始化；
//             layui.form.render();
//
//             // 下拉框的onchange事件；
//             layui.form.on("select(afterDept)", function (deptData) {
//                 deptNameObj.val(getLayUISelectText(deptData));
//                 // 刷新部门的职位数据；
//                 loadTransferPost(deptData.value);
//             });
//         }
//     }, "json");
// }

// 获取部门的职位数据；
function loadTransferPost(deptId) {
    $.post(baseUrl + "/entry/getPost", {deptId: deptId}, function (data) {
        var postObj = $("#transferForm select[name='afterProfession']");
        // 先清空；
        postObj.empty();
        // 设置职位名称；
        var postNameObj = $("#transferForm input[name='afterProfessionName']");
        postNameObj.val("");
        var dataValue = data.data.post;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                var oldPost = $("#transferForm input[data='afterProfession']").val();
                oldPost = oldPost.length > 0 ? oldPost : dataValue[0].id;
                var oldPostName;
                for (var i = 0; i < dataValue.length; i++) {
                    if (oldPost == dataValue[i].id) {
                        oldPostName = dataValue[i].name;
                        postObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        postObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                }

                // 更新职位名称；
                if (!oldPostName) {
                    oldPostName = dataValue[0].name;
                }
                postNameObj.val(oldPostName);
            }

            // 初始化；
            layui.form.render();

            // 下拉框的onchange事件；
            layui.form.on("select(afterProfession)", function (postData) {
                // 更新隐藏域；
                postNameObj.val(getLayUISelectText(postData));
            });
        }
    }, "json");
}

/**
 * 加载角色名称
 */
function CharacterName() {
    var nameQc =$("#roleType").val()
    if (nameQc ==""){
        return;
    }else {
        $("#roleName").empty();
        layui.use(["form"], function () {
            $.get(baseUrl + "/user/getRoleByType/",{nameQc:nameQc},function (data) {
                $("#roleName").append("<option value=''>请选择</option>");
                $(data).each(function (i, d) {
                    $("#roleName").append("<option value='" + d.id + "'>" + d.name + "</option>");
                });
                layui.form.on("select(mediaUserFilter1)",function(resultData){
                    $("#transferForm input[name='roleId']").val(resultData.id);
                    $("#transferForm input[name='roleId']").val($("#roleName").find("option:selected").val());
                });
                layui.form.render('select');
            }, "json");
        });
    }
}

// 打开交接的流程提交窗口；
function openConnect(empId, processId) {
    alertMessage("系统处理中，请稍候。");
    // 离职；
    if (processId == 13 || processId == 15) {
        openLeaveConnect(empId, processId)
    }
    // 调岗；
    if (processId == 14 || processId == 16) {
        openTransferConnect(empId, processId)
    }
}

// 打开离职交接的流程提交窗口；
function openLeaveConnect(empId, processId) {
    // 重置表单；
    cleanValidate($("#leaveConnectForm"));
    $.post(baseUrl + "/employeeManage/getEmployeeConnect", {empId: empId, processId: processId}, function (data) {
        var dataValue = data.data.employee;
        if (dataValue == null) {
            alertMessage("信息不存在，请刷新页面后重试。");
        } else {
            var userId = dataValue["userId"];
            if (userId) {
                var connect = dataValue["connect"];
                if (connect) {
                    dataValue = connect;
                    $("#leaveConnectForm").find("input").each(function () {
                        $(this).val(dataValue[$(this).attr("name")]);
                    });

                    // 表单类型；
                    var conId = dataValue["conId"];
                    // 没有提交记录；
                    if (conId == undefined) {
                        // 类型和关联数据；
                        $("#leaveConnectForm").find("input[name='conType']").val(0);
                        $("#leaveConnectForm").find("input[name='conData']").val(dataValue["leaveId"]);

                        // 通知时间；
                        var createTime = dataValue["createTime"];
                        if (createTime) {
                            $("#leaveConnectForm input[name='conDate']").val(new Date(createTime).format("yyyy-MM-dd"));
                        }

                        // 离职日期；
                        var leaveDate = dataValue["leaveDate"];
                        if (leaveDate) {
                            $("#leaveConnectForm input[name='completeDate']").val(new Date(leaveDate).format("yyyy-MM-dd"));
                        }
                    } else {
                        // 通知时间；
                        var conDate = dataValue["conDate"];
                        if (conDate) {
                            $("#leaveConnectForm input[name='conDate']").val(new Date(conDate).format("yyyy-MM-dd"));
                        }

                        // 离职日期；
                        var completeDate = dataValue["completeDate"];
                        if (completeDate) {
                            $("#leaveConnectForm input[name='completeDate']").val(new Date(completeDate).format("yyyy-MM-dd"));
                        }
                    }

                    // 员工状态；
                    var empState = dataValue["empState"];
                    if (empState != undefined) {
                        $("#leaveConnectForm").find("input[name='empState']").val(empState);
                        if (empState == 0) {
                            $("#leaveConnectForm").find("input[data='empState']").val("试用期");
                        }
                        if (empState == 5) {
                            $("#leaveConnectForm").find("input[data='empState']").val("实习");
                        }
                        if (empState == 1) {
                            $("#leaveConnectForm").find("input[data='empState']").val("转正");
                        }
                    }

                    // 入职日期；
                    var empDate = dataValue["empDate"];
                    if (empDate) {
                        $("#leaveConnectForm input[name='empDate']").val(new Date(empDate).format("yyyy-MM-dd"));
                    }

                    // 性质；
                    var leaveType = dataValue["leaveType"];
                    if (leaveType == 0) {
                        $("#leaveConnectForm").find("input[data='leaveType']").val("公司劝退");
                    } else {
                        $("#leaveConnectForm").find("input[data='leaveType']").val("个人离职");
                    }

                    // 离职缘由；
                    var reasonArray = ["终止试用", "公司辞退", "终止续签", "试用辞退", "个人辞职", "合同期满"];
                    $("#leaveConnectForm").find("input[data='leaveTypeContent']").val(reasonArray[dataValue["leaveTypeContent"]]);

                    // 时间类型；
                    $("#leaveConnectForm").find("input[name='conDateType']").val(1);
                    $("#leaveConnectForm").find("input[data='conDateType']").val("公司通知时间");

                    // 审批人；
                    $("#leaveConnectForm input[name='userId']").val(userId);
                    $("#leaveConnectForm input[name='userName']").val(dataValue["empName"]);
                    $("#leaveConnectModal").modal({backdrop: "static"});
                } else {
                    alertMessage("信息不存在，请刷新页面后重试。");
                }
            } else {
                alertMessage("该员工没有绑定系统登录账号，请检查。");
            }
        }
    }, "json");
}

// 提交离职交接流程；
function startLeaveConnect() {
    if ($("#leaveConnectForm").valid()) {
        alertMessage("系统处理中，请稍候。");
        startModal("#startLeaveConnect");
        $.post(baseUrl + "/employeeManage/startConnect", $("#leaveConnectForm").serializeJson(), function (data) {
            Ladda.stopAll();
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                alertMessage(message);
            }
            $("#leaveConnectModal").modal("hide");
            reloadEmployeeData();
        }, "json");
    }
}

// 打开调岗交接的流程提交窗口；
function openTransferConnect(empId, processId) {
    // 重置表单；
    cleanValidate($("#transferConnectForm"));
    $.post(baseUrl + "/employeeManage/getEmployeeConnect", {empId: empId, processId: processId}, function (data) {
        var dataValue = data.data.employee;
        if (dataValue == null) {
            alertMessage("信息不存在，请刷新页面后重试。");
        } else {
            var userId = dataValue["userId"];
            if (userId) {
                var connect = dataValue["connect"];
                if (connect) {
                    dataValue = connect;
                    $("#transferConnectForm").find("input").each(function () {
                        $(this).val(dataValue[$(this).attr("name")]);
                    });

                    // 表单类型；
                    var conId = dataValue["conId"];
                    // 没有提交记录；
                    if (conId == undefined) {
                        // 类型和关联数据；
                        $("#transferConnectForm").find("input[name='conType']").val(1);
                        $("#transferConnectForm").find("input[name='conData']").val(dataValue["tranId"]);

                        // 通知时间；
                        var createTime = dataValue["createTime"];
                        if (createTime) {
                            $("#transferConnectForm input[name='conDate']").val(new Date(createTime).format("yyyy-MM-dd"));
                        }

                        // 执行日期；
                        var transDate = dataValue["transDate"];
                        if (transDate) {
                            $("#transferConnectForm input[name='completeDate']").val(new Date(transDate).format("yyyy-MM-dd"));
                        }
                    } else {
                        // 通知时间；
                        var conDate = dataValue["conDate"];
                        if (conDate) {
                            $("#transferConnectForm input[name='conDate']").val(new Date(conDate).format("yyyy-MM-dd"));
                        }

                        // 离职日期；
                        var completeDate = dataValue["completeDate"];
                        if (completeDate) {
                            $("#transferConnectForm input[name='completeDate']").val(new Date(completeDate).format("yyyy-MM-dd"));
                        }
                    }

                    // 员工状态；
                    var empState = dataValue["empState"];
                    if (empState != undefined) {
                        $("#transferConnectForm").find("input[name='empState']").val(empState);
                        if (empState == 0) {
                            $("#transferConnectForm").find("input[data='empState']").val("试用期");
                        }
                        if (empState == 5) {
                            $("#transferConnectForm").find("input[data='empState']").val("实习");
                        }
                        if (empState == 1) {
                            $("#transferConnectForm").find("input[data='empState']").val("转正");
                        }
                    }

                    // 入职日期；
                    var empDate = dataValue["empDate"];
                    if (empDate) {
                        $("#transferConnectForm input[name='empDate']").val(new Date(empDate).format("yyyy-MM-dd"));
                    }

                    // 时间类型；
                    $("#transferConnectForm").find("input[name='conDateType']").val(1);
                    $("#transferConnectForm").find("input[data='conDateType']").val("公司通知时间");

                    // 审批人；
                    $("#transferConnectForm input[name='userId']").val(userId);
                    $("#transferConnectForm input[name='userName']").val(dataValue["empName"]);
                    $("#transferConnectModal").modal({backdrop: "static"});
                } else {
                    alertMessage("信息不存在，请刷新页面后重试。");
                }
            } else {
                alertMessage("该员工没有绑定系统登录账号，请检查。");
            }
        }
    }, "json");
}

// 提交调岗交接流程；
function startTransferConnect() {
    if ($("#transferConnectForm").valid()) {
        alertMessage("系统处理中，请稍候。");
        startModal("#startTransferConnect");
        $.post(baseUrl + "/employeeManage/startConnect", $("#transferConnectForm").serializeJson(), function (data) {
            Ladda.stopAll();
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                alertMessage(message);
            }
            $("#transferConnectModal").modal("hide");
            reloadEmployeeData();
        }, "json");
    }
}

// 删除员工；
function deleteEmployee() {
    var empIds = $("#employeeTable").jqGrid("getGridParam", "selarrrow");
    // 如果多选操作没有选择数据，提示用户；
    if (empIds.length > 0) {
        layer.confirm("此操作无法恢复，确认继续吗？", {
            btn: ["确认", "取消"],
            shade: [0.7, '#393D49']
        }, function (index) {
            layer.close(index);
            alertMessage("系统处理中，请稍候。");
            // 提交数据到后台；
            $.post(baseUrl + "/employeeManage/deleteEmployee", {empIds: empIds.toString()}, function (data) {
                var message = data.data.message;
                if (message == null) {
                    alertMessage(data.msg);
                } else {
                    alertMessage(message);
                }
                reloadEmployeeData();
            }, "json");
        });
    } else {
        alertMessage("请选择要操作的数据。");
    }
}

// 打开编辑页面的窗口；
function openEdit(empId) {
    // 民族信息；
    var raceObj = $("#editForm select[name='empRace']");
    var editRaceHtml = raceObj.html();
    if (editRaceHtml.length == 45) {
        raceObj.append($("#queryForm select[name='empRace']").html());
    } else {
        var selectedOption = raceObj.find("option:selected");
        if (selectedOption && selectedOption.val().length > 0) {
            selectedOption.attr("selected", false);
        }
    }

    // 籍贯信息；
    var provinceObj = $("#editForm select[name='empNativeProvince']");
    if (provinceObj.html().length == 45) {
        loadProvinceData();
    } else {
        selectedOption = provinceObj.find("option:selected");
        if (selectedOption && selectedOption.val().length > 0) {
            selectedOption.attr("selected", false);
        }
    }
    alertMessage("系统处理中，请稍候。");
    // 重置表单；
    cleanValidate($("#editForm"));
    $.post(baseUrl + "/employeeManage/getEmployeeEdit", {empId: empId}, function (data) {
        var dataValue = data.data.employee;
        if (dataValue == null) {
            alertMessage("信息不存在，请刷新页面后重试。");
        } else {
            $("#editForm").find("input[type='text']").each(function () {
                $(this).val(dataValue[$(this).attr("name")]);
            });
            $("#editForm").find("textarea").each(function () {
                $(this).val(dataValue[$(this).attr("name")]);
            });

            // 出生日期；
            var empBirth = dataValue["empBirth"];
            if (empBirth) {
                $("#editForm input[name='empBirth']").val(new Date(empBirth).format("yyyy-MM-dd"));
            }

            // 第一次签订日期；
            var empContractDate = dataValue["empContractDate"];
            if (empContractDate) {
                $("#editForm input[name='empContractDate']").val(new Date(empContractDate).format("yyyy-MM-dd"));
            }

            // 第二次签订日期；
            var empTwoContractDate = dataValue["empTwoContractDate"];
            if (empTwoContractDate) {
                $("#editForm input[name='empTwoContractDate']").val(new Date(empTwoContractDate).format("yyyy-MM-dd"));
            }

            // 第三次签订日期；
            var empThreeContractDate = dataValue["empThreeContractDate"];
            if (empThreeContractDate) {
                $("#editForm input[name='empThreeContractDate']").val(new Date(empThreeContractDate).format("yyyy-MM-dd"));
            }

            // 设置主键
            $("#editForm").find("input[name='empId']").val(empId);
            $("#editForm").find("input[name='basId']").val(dataValue["basId"]);

            // 籍贯信息；
            $("#editForm").find("input[name='empNative']").val(dataValue["empNative"]);
            var oldProvince = dataValue["empNativeProvince"];
            $("#provinceId").val(oldProvince);
            $("#cityId").val(dataValue["empNativeCity"]);

            // 设置民族选中；
            raceObj.find("option[value='" + dataValue["empRace"] + "']").attr("selected", "selected");
            // 设置籍贯省选中；
            provinceObj.find("option[value='" + oldProvince + "']").attr("selected", "selected");
            // 加载籍贯市；
            loadCityData(oldProvince);

            // 性别；
            setFormRadioChecked($("#editForm input[name='empGender']"), dataValue["empGender"]);
            // 婚姻状态；
            setFormRadioChecked($("#editForm input[name='empMarriage']"), dataValue["empMarriage"]);
            // 居住情况；
            setFormRadioChecked($("#editForm input[name='empHouse']"), dataValue["empHouse"]);
            // 承诺书签署；
            setFormRadioChecked($("#editForm input[name='empCompliance']"), dataValue["empCompliance"]);

            $("#editModal").modal({backdrop: "static"});
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
                oldProvince = oldProvince.length > 0 ? oldProvince : dataValue[0].area_id;
                var provinceObj = $("#editForm select[name='empNativeProvince']");
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
        var cityObj = $("#editForm select[name='empNativeCity']");
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

// 设置籍贯信息；
function setNative() {
    var nativeStr = "";
    $(".native").each(function () {
        nativeStr += $(this).find("option:selected").text();
    });
    $("#editForm input[name='empNative']").val(nativeStr);
}

// 员工的新增和编辑；
function startEdit() {
    if ($("#editForm").valid()) {
        alertMessage("系统处理中，请稍候。");
        startModal("#startEdit");
        $.post(baseUrl + "/employeeManage/updateEmployee", {employeeInfo: JSON.stringify($("#editForm").serializeJson())}, function (data) {
            Ladda.stopAll();
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                alertMessage(message);
            }
            $("#editModal").modal("hide");
            reloadEmployeeData();
        }, "json");
    }
}

// 设置年龄；
function setAge(age) {
    $("#editForm").find("input[name='empAge']").val(age);
}

// 根据输入的日期计算年龄；
function countAge(strBirthday) {
    var returnAge;
    var strBirthdayArr = strBirthday.split("-");
    var birthYear = strBirthdayArr[0];
    var birthMonth = strBirthdayArr[1];
    var birthDay = strBirthdayArr[2];

    var today = new Date();
    var nowYear = today.getFullYear();
    var nowMonth = today.getMonth() + 1;
    var nowDay = today.getDate();

    if (nowYear == birthYear) {
        //同年，则为0岁；
        returnAge = 0;
    } else {
        // 年之差；
        var ageDiff = nowYear - birthYear;
        if (ageDiff > 0) {
            if (nowMonth == birthMonth) {
                // 日之差；
                var dayDiff = nowDay - birthDay;
                if (dayDiff < 0) {
                    returnAge = ageDiff - 1;
                } else {
                    returnAge = ageDiff;
                }
            } else {
                // 月之差；
                var monthDiff = nowMonth - birthMonth;
                if (monthDiff < 0) {
                    returnAge = ageDiff - 1;
                } else {
                    returnAge = ageDiff;
                }
            }
        } else {
            // 返回0 表示出生日期输入错误，晚于今天；
            returnAge = 0;
        }
    }
    return returnAge;//返回周岁年龄
}

//触发input控件
function batchImportFileF() {
    $("#batchImportFile").click();
}

function templateEmployee() {
    location.href = baseUrl + "/employeeManage/template"
}

//提交文件
function batchImportFileUpload() {
    if (document.getElementById("batchImportFile").value == "") {
        swal("请选上传excel！");
    } else {
        var filePath = document.getElementById("batchImportFile").value;
        var fileExt = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
        var flag = false;
        if (fileExt.match(/^(.xls|.xlsx)$/)) {
            flag = true;
        }
        if (flag) {
            var data = new FormData();
            data.append("file", document.getElementById("batchImportFile").files[0]);

            var xhr = new XMLHttpRequest();
            xhr.withCredentials = true;

            xhr.addEventListener("readystatechange", function () {
                if (this.readyState === 4) {
                    var data = JSON.parse(this.responseText);
                    if (data.code === 200) {
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
                            html: isHtml
                        });
                        reloadEmployeeData();
                    } else {
                        // layer.alert(data.msg);
                        swal({
                            title: "导入失败",
                            text: data.msg,
                            type: "error"
                        });
                    }
                }
            });

            xhr.open("POST", baseUrl + "/entryManage/importEmployeeData");
            xhr.setRequestHeader("x-requested-with", "XMLHttpRequest");
            xhr.setRequestHeader("cache-control", "no-cache");

            xhr.send(data);

            $("#batchImportFile").val('');
        } else {
            swal("文件格式不正确，只能上传excel文件！");
        }
    }
}

//加载职位
function loadPost(deptId){
    $.post(baseUrl + "/entry/getPost", {deptId: deptId}, function (data) {
        var postObj = $("#queryForm select[name='empProfession']");
        // 先清空；
        postObj.empty();
        var dataValue = data.data.post;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                for (var i = 0; i < dataValue.length; i++) {
                    postObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                }
            }
            // 初始化；
            layui.form.render();
        }
    }, "json");
}
//部门选择
$(document).ready(function () {
    $("#selDept").click(function () {
        $("#deptModal").modal('toggle');
    });
    $("#selDept1").click(function () {
        $("#postDeptModal").modal('toggle');
    });
    $('#treeview').treeview({
        data: [getTreeData(isZC())],
        onNodeSelected: function (event, data) {
            $("#deptId").val(data.id);
            $("#deptName").val(data.text);
            $("#deptModal").modal('hide');
            loadPost(data.id);//加载职位
        }
    });
    $('#postTreeview').treeview({
        data: [getTreeData(isZC())],
        onNodeSelected: function (event, data) {
            $("#transferForm input[name='afterDept']").val(data.id)
          //  $("#afterDept").val(data.id);
            //$("#afterDeptName").val(data.text);
            $("#transferForm input[name='afterDeptName']").val(data.text)
            $("#deptName1").val(data.text);
            loadTransferPost(data.id);
            $("#postDeptModal").modal('hide');
            //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
            // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
            $("body").addClass("modal-open"); //解决选择部门后，调岗弹窗不能滚动
        }
    });

    $("#cleanDept").click(function () {
        $("#deptId").val("");
        $("#deptName").val("");
        $("#queryForm select[name='empProfession']").empty();
        layui.form.render();
    });
    $("#cleanDept1").click(function () {
        $("#afterDept").val("");
        $("#afterDeptName").val("");
        $("#deptName1").val("");
    });
})
//判断当前用户是否总裁
var isZC = function () {
    var roles = user.roles;//获取用户角色
    var isZC = false;//是否总裁角色
    if (roles) {
        for (var i = 0; i < roles.length; i++) {
            if (roles[i].code == 'ZC' || roles[i].code == 'FZC' || roles[i].code == 'ZW') {
                isZC = true;
                break;
            }
        }
    }
    return isZC;
}

//初始化部门
function getTreeData(isZC) {
    var deptTreeData = {};
    var deptId = user.dept.id;//当前用户部门ID
    var deptCode = user.dept.code;//当前部门编码
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    var url = "/dept/listForSonTreeView";
    if(deptCode == "ZW"){
        url = "/deptZw/listDeptTreeByZw"; //查询政委管理的部门
    }else if (deptCompanyCode == "JT" && (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL")) {
        requestData(null, "/dept/getRootDept", "POST", function (result) {
            var root = result.data.root;
            if (root) {
                deptId = root.id;//整个集团的业务和媒介部
            } else {
                deptId = 517;//整个集团的业务和媒介部
            }
        });
    } else if (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL" || deptCode == "RS" || deptCode == "XZ") {
        requestData({companyCode: deptCompanyCode}, "/dept/getCompanyByCode", "POST", function (result) {
            var company = result.data.company;
            if (company) {
                deptId = company.id;//整个集团的业务和媒介部
            }
        });
    }
    var param = {deptId: deptId};
    //如果是政委，并且是政委部门负责人，则部门树还需要包含自己部门
    if(deptCode == "ZW" && user.id == user.dept.mgrId){
        param.mgrFlag = 1; //部门负责人标识，后台根据标识判断
    }
    //具体查询
    requestData(param, url, "POST", function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType, callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: false,
        success: callBackFun
    });
}

