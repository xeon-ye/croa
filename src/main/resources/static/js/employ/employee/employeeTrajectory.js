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
        url: baseUrl + "/employeeTrajectory/getPageTrajectory",
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
        sortname: "traj_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "traj_id"
        },
        colModel: [
            {
                name: 'traj_id',
                index: "traj_id",
                label: '主键',
                hidden: true,
                width: 60,
                sortable: false
            },
            {name: 'emp_num', index: "emp_num", label: '工号', width: 120},
            {name: 'emp_name', index: "emp_name", label: '姓名', width: 120},
            {
                name: 'emp_dept',
                index: "emp_dept",
                label: '所属部门',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.emp_dept_name == null ? "" : rowData.emp_dept_name;
                }
            },
            {
                name: 'emp_profession',
                index: "emp_profession",
                label: '当前职位',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.emp_profession_name == null ? "" : rowData.emp_profession_name;
                }
            },
            {
                name: 'emp_content',
                index: "emp_content",
                label: '内容记录',
                width: 120
            },
            {
                name: 'create_id',
                index: "create_id",
                label: '操作人员',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.create_name == null ? "" : rowData.create_name;
                }
            },
            {
                name: 'create_time',
                index: "create_time",
                label: '操作时间',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd hh:mm:ss");
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_transaction',
                index: "emp_transaction",
                label: '事务类型',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: orange;">入职</b>';
                    } else if (value == 1) {
                        return '<b style="color: red;">录用</b>';
                    } else if (value == 2) {
                        return '<b style="color: green;">转正</b>';
                    } else if (value == 3) {
                        return '<b style="color: grey;">离职</b>';
                    } else if (value == 4) {
                        return '<b style="color: blue;">调岗</b>';
                    } else if (value == 5) {
                        return '<b style="color: purple;">交接</b>';
                    } else if (value == 6) {
                        return '<b style="color: black;">其他</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_operate',
                index: "emp_operate",
                label: '操作类型',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: orange;">提起申请</b>';
                    } else if (value == 1) {
                        return '<b style="color: green;">申请通过</b>';
                    } else if (value == 2) {
                        return '<b style="color: red;">申请被拒</b>';
                    } else if (value == 3) {
                        return '<b style="color: blue;">内容变更</b>';
                    } else {
                        return "";
                    }
                }
            }
        ],
        pager: jQuery("#employeeTableNav"),
        viewrecords: true,
        caption: "员工轨迹记录",
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
    $("#employeeTable").reloadCurrentData(baseUrl + "/employeeTrajectory/getPageTrajectory", $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}