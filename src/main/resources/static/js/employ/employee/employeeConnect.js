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
        url: baseUrl + "/employeeConnect/getPageConnect",
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
        sortname: "con_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "con_id"
        },
        colModel: [
            {
                name: 'con_id',
                index: "con_id",
                label: '主键',
                hidden: true,
                width: 60,
                sortable: false
            },
            {name: 'emp_num', index: "emp_num", label: '工号', width: 120},
            {name: 'emp_name', index: "emp_name", label: '姓名', width: 120},
            {
                name: 'con_type',
                index: "con_type",
                label: '类型',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: orange;">离职</b>';
                    } else if (value == 1) {
                        return '<b style="color: green;">调岗</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'emp_state',
                index: "emp_state",
                label: '员工状态',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: blue;">试用</b>';
                    } else if (value == 1) {
                        return '<b style="color: green;">转正</b>';
                    } else if (value == 2) {
                        return '<b style="color: black;">离职</b>';
                    } else {
                        return '';
                    }
                }
            },
            {
                name: 'emp_date',
                index: "emp_date",
                label: '入职日期',
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
                name: 'con_date',
                index: "con_date",
                label: '交接日期',
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
                name: 'complete_date',
                index: "complete_date",
                label: '完成日期',
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
                name: 'dept_leader',
                index: "dept_leader",
                label: '上级领导',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.dept_leader_name == null ? "" : rowData.dept_leader_name;
                }
            },
            {
                name: 'create_id',
                index: "create_id",
                label: '申请人员',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.create_name == null ? "" : rowData.create_name;
                }
            },
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
                name: 'state', index: "state", label: '状态', width: 120, formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: orange;">审核中</b>';
                    } else if (value == 1) {
                        return '<b style="color: green;">审核通过</b>';
                    } else if (value == 2) {
                        return '<b style="color: red;">审核拒绝</b>';
                    } else if (value == 3) {
                        return '<b style="color: grey;">交接完成</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'operate', label: "操作", width: 120, sortable: false,
                formatter: function (value, grid, rowData) {
                    var html = "";
                    //TODO 由于以前流程dataId取得是emp_id, 修改后采用主键ID，所以为了兼容查看以前的审核记录，通过发布版本时间判断
                    if(new Date(rowData.create_time) < new Date('2020-04-22')){
                        html += '<a href="#" onclick="showHistory(' + "'" + rowData.emp_id + "','" + rowData.con_type + "'" + ');return false;">审核记录</a>';
                    }else {
                        html += '<a href="#" onclick="showHistory(' + "'" + (rowData.emp_id+"_"+rowData.con_id) + "','" + rowData.con_type + "'" + ');return false;">审核记录</a>';
                    }
                    return html;
                },
            }
        ],
        pager: jQuery("#employeeTableNav"),
        viewrecords: true,
        caption: "员工交接记录",
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
    $("#employeeTable").reloadCurrentData(baseUrl + "/employeeConnect/getPageConnect", $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}

// 审核记录查看；
function showHistory(dataId, type) {
    if (type == 0) {
        showProcessHistory(dataId, 15);
    }
    if (type == 1) {
        showProcessHistory(dataId, 16);
    }
}