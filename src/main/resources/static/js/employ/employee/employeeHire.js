$(function () {
    // 查询表单增加校验；
    $("#queryForm").validate();

    // 加载基础资源数据；
    layui.use(["form"], function () {
        loadDeptData();
    });

    // 设置表格默认的UI样式；
    $.jgrid.defaults.styleUI = 'Bootstrap';

    // 窗口拖拽绑定事件；
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#employeeTable').setGridWidth(width);
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

    // 初始化表格；
    loadJGrid();
});

// 加载表格；
function loadJGrid() {
    // 初始化数据；
    $("#employeeTable").jqGrid({
        url: baseUrl + "/employeeHire/getPageHire",
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
        multiselect: false,
        multiselectWidth: 50,
        sortable: "true",
        sortname: "hire_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "hire_id"
        },
        colModel: [
            {
                name: 'hire_id',
                index: "hire_id",
                label: '主键',
                hidden: true,
                width: 60,
                sortable: false
            },
            {name: 'emp_num', index: "emp_num", label: '工号', width: 120},
            {name: 'emp_name', index: "emp_name", label: '姓名', width: 120},
            {
                name: 'create_time',
                index: "create_time",
                label: '申请日期',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd hh:mm:ss");
                    } else {
                        return new Date(rowData.create_time).format("yyyy-MM-dd hh:mm:ss");
                    }
                }
            },
            {
                name: 'emp_expect_date',
                index: "emp_expect_date",
                label: '到岗日期',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd");
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'trial_begin',
                index: "trial_begin",
                label: '试用期',
                width: 120,
                formatter: function (value, grid, rowData) {
                    var endDate = rowData.trial_end;
                    if (value && endDate) {
                        return new Date(value).format("yyyy-MM-dd") + " - " + new Date(endDate).format("yyyy-MM-dd");
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_dept',
                index: "emp_dept",
                label: '录用部门',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.emp_dept_name == null ? "" : rowData.emp_dept_name;
                }
            },
            {
                name: 'emp_profession',
                index: "emp_profession",
                label: '安排职位',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.emp_profession_name == null ? "" : rowData.emp_profession_name;
                }
            },
            {
                name: 'emp_gender',
                index: "emp_gender",
                label: '性别',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        // return '<b style="color: red;">女</b>';
                        return "女";
                    } else if (value == 1) {
                        // return '<b style="color: green;">男</b>';
                        return "男";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_marriage',
                index: "emp_marriage",
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
                name: 'emp_birth',
                index: "emp_birth",
                label: '出生年月',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return value ? new Date(value).format("yyyy-MM-dd") : "";
                }
            },
            {
                name: 'emp_education',
                index: "emp_education",
                label: '学历',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b>初中</b>';
                    } else if (value == 1) {
                        return '<b>高中</b>';
                    } else if (value == 2) {
                        return '<b>专科</b>';
                    } else if (value == 3) {
                        return '<b>本科</b>';
                    } else if (value == 4) {
                        return '<b>硕士</b>';
                    } else if (value == 5) {
                        return '<b>博士</b>';
                    } else if (value == 6) {
                        return '<b>博士后</b>';
                    } else if (value == 7) {
                        var otherEducaiton = rowData.emp_education_other;
                        if (!otherEducaiton) {
                            otherEducaiton = "其他";
                        }
                        return '<b>' + otherEducaiton + '</b>';
                    } else {
                        return "";
                    }
                }
            },
            {name: 'emp_college', index: "emp_college", label: '学院', width: 120},
            {name: 'emp_major', index: "emp_major", label: '专业', width: 120},
            {
                name: 'state', index: "state", label: '状态', width: 120, formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: orange;">审核中</b>';
                    } else if (value == 1) {
                        return '<b style="color: green;">审核通过</b>';
                    } else if (value == 2) {
                        return '<b style="color: red;">审核拒绝</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'operate', label: "操作", width: 120, sortable: false,
                formatter: function (value, grid, rowData) {
                    var html = '<a href="#" onclick="showHistory(' + "'" + rowData.entry_id + "'" + ');return false;">审核记录</a>';
                    return html;
                },
            }
        ],
        pager: jQuery("#employeeTableNav"),
        viewrecords: true,
        caption: "员工录用记录",
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
        }
    });

    $("#employeeTable").jqGrid('setLabel', 'rn', '序号', {
        'text-align': 'center',
        'vertical-align': 'middle',
        "width": "50"
    });
}

// 重新载入数据；
function reloadEmployeeData() {
    $("#employeeTable").reloadCurrentData(baseUrl + "/employeeHire/getPageHire", $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}

// 审核记录查看；
function showHistory(dataId) {
    showProcessHistory(dataId, 6);
}