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
        url: baseUrl + "/employeeTransfer/getPageTransfer",
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
        sortname: "tran_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "tran_id"
        },
        colModel: [
            {
                name: 'tran_id',
                index: "tran_id",
                label: '主键',
                hidden: true,
                width: 60,
                sortable: false
            },
            {name: 'emp_num', index: "emp_num", label: '工号', width: 120},
            {name: 'emp_name', index: "emp_name", label: '姓名', width: 120},
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
                name: 'after_dept',
                index: "after_dept",
                label: '调岗部门',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.after_dept_name == null ? "" : rowData.after_dept_name;
                }
            },
            {
                name: 'after_profession',
                index: "after_profession",
                label: '调岗职位',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return rowData.after_profession_name == null ? "" : rowData.after_profession_name;
                }
            },
            {
                name: 'trans_date',
                index: "trans_date",
                label: '执行日期',
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
                        return '<b style="color: black;">调岗完成</b>';
                    } else if (value == 4) {
                        return '<b style="color: orange;">准备交接</b>';
                    } else if (value == 5) {
                        return '<b style="color: orangered;">交接中</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'operate', label: "操作", width: 120, sortable: false,
                formatter: function (value, grid, rowData) {
                    var html = "";
                    // if (rowData.state == 1) {
                    //     html += "<a href='#' onclick='return false;'>交接</a>&nbsp;|&nbsp;";
                    // }
                    //TODO 由于以前流程dataId取得是emp_id, 修改后采用主键ID，所以为了兼容查看以前的审核记录，通过发布版本时间判断
                    if(new Date(rowData.create_time) < new Date('2020-04-22')){
                        html += '<a href="#" onclick="showHistory(' + "'" + rowData.emp_id + "'" + ');return false;">审核记录</a>';
                    }else {
                        html += '<a href="#" onclick="showHistory(' + "'" + (rowData.emp_id+"_"+rowData.tran_id)+ "'" + ');return false;">审核记录</a>';
                    }
                    return html;
                },
            }
        ],
        pager: jQuery("#employeeTableNav"),
        viewrecords: true,
        caption: "员工调岗记录",
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
    $("#employeeTable").reloadCurrentData(baseUrl + "/employeeTransfer/getPageTransfer", $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}

// 审核记录查看；
function showHistory(dataId) {
    showProcessHistory(dataId, 14);
}