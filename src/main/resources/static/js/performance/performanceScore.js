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
        $('#scoreTable').setGridWidth(width);
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
            $("#scoreTable").emptyGridParam();
            reloadEmployeeData();
        }
    });

    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

// 初始化表格；
    loadJGrid();
});

// 加载表格；
function loadJGrid() {
    // 初始化数据；
    $("#scoreTable").jqGrid({
        url: baseUrl + "/performanceScore/getPageScore",
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
        sortname: "score_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "score_id"
        },
        colModel: [
            {
                name: 'scoreId',
                index: "score_id",
                label: '主键',
                hidden: true,
                width: 60,
                sortable: false
            },
            {name: 'proDesc', index: "pro_id", label: '考核计划', width: 120},
            {name: 'schDesc', index: "sch_id", label: '考核方案', width: 120},
            {name: 'userName', index: "user_id", label: '姓名', width: 120},
            {
                name: 'userGender',
                index: "user_gender",
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
                name: 'deptName',
                index: "dept_id",
                label: '所属部门',
                width: 120
            },
            {
                name: 'postName',
                index: "post_id",
                label: '当前职位',
                width: 120
            },
            {name: 'scoreSelf', index: "score_self", label: '个人自评', width: 120},
            {name: 'scoreLeader', index: "score_leader", label: '上级评分', width: 120},
            // {name: 'scoreGroup', index: "score_group", label: '小组评分',hidden:true, width: 120},
            {name: 'scoreTotal', index: "score_total", label: '总分', width: 120},
            {name: 'scoreLevel', index: "score_level", label: '评分等级', width: 120},
            {
                name: 'beSuffice',
                index: "be_suffice",
                label: '是否合格',
                width: 120,
                formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: red;">否</b>';
                    } else if (value == 1) {
                        return '<b style="color: green;">是</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'createTime',
                index: "create_time",
                label: '考核日期',
                width: 120,
                formatter: function (value, grid, rowData) {
                    return new Date(value).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {
                name: 'state', index: "state", label: '状态', width: 120, formatter: function (value, grid, rowData) {
                    if (value == 0) {
                        return '<b style="color: orange;">考核中</b>';
                    } else if (value == 1) {
                        return '<b style="color: green;">考核完成</b>';
                    } else if (value == 2) {
                        return '<b style="color: red;">审核拒绝</b>';
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'operate', label: "操作", width: 200, sortable: false,
                formatter: function (value, grid, rowData) {
                    var html = "";
                    var state = rowData.state;
                    if (state == 2) {
                        html += '<a href="#" onclick="startPerformance(' + "'" + rowData.scoreId + "'" + ');return false;">提交</a>&nbsp;|&nbsp;';
                    }
                    html += '<a href="#" onclick="openDetail(' + "'" + rowData.scoreId + "'" + ');return false;">详情</a>&nbsp;|&nbsp;';
                    html += '<a href="#" onclick="showHistory(' + "'" + rowData.scoreId + "'" + ');return false;">记录</a>';
                    return html;
                },
            }
        ],
        pager: jQuery("#scoreTableNav"),
        viewrecords: true,
        caption: "员工绩效考核结果",
        add: false,
        edit: false,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
        },
        loadComplete: function (a, b, c) {
            $("#scoreTable").find("tr").each(function () {
                $(this).children().first().css("width", "50");
            });
        }
    });

    // 序号美化；
    $("#scoreTable").jqGrid('setLabel', 'rn', '序号', {
        'text-align': 'center',
        'vertical-align': 'middle',
        "width": "50"
    });

    // 表格的背景色；
    $("#scoreDetailTable tr td").each(function () {
        var spanElement = $(this).find("input");
        if (spanElement && spanElement.length > 0) {
            spanElement.css({
                "width": "100%",
                "height": "30px",
                "border": "0px",
                "padding": "0 5px 0 5px",
                "background-color": "white"
            });
            spanElement.attr("disabled", "disabled");
        } else {
            $(this).css("background-color", "#EBEBEB");
        }
    });
}

// 重新载入数据；
function reloadEmployeeData() {
    $("#scoreTable").reloadCurrentData(baseUrl + "/performanceScore/getPageScore", $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}

// 查看评分详情；
function openDetail(dataId) {
    alertMessage("系统处理中，请稍候。");
    // 重置表单；
    cleanValidate($("#scoreForm"));
    // 删除备注；
    $(".desc").remove();
    // 删除考核详情；
    $(".detail").remove();
    $.post(baseUrl + "/performanceScore/getScoreInfo", {scoreId: dataId}, function (data) {
        var dataValue = data.data.score;
        if (dataValue == null) {
            alertMessage("信息不存在，请刷新页面后重试。");
        } else {
            // 赋值；
            for (var key in dataValue) {
                $("#scoreForm").find("input[type='text'][name='" + key + "']").val(dataValue[key]);
            }

            // 考核方案；
            dataValue = data.data.scheme;
            var schemeType = +dataValue.schemeType;
            if (dataValue != null) {
                for (var key in dataValue) {
                    $("#scoreForm").find("input[type='text'][name='" + key + "']").val(dataValue[key]);
                }

                $("#totalScore").html(dataValue["schTotal"]);
            }

            // 考核计划；
            dataValue = data.data.proportion;
            if (dataValue != null) {
                if (dataValue.proBegin && dataValue.proEnd) {
                    $("#scoreForm").find("input[type='text'][name='proDate']").val(dataValue.proBegin + " - " + dataValue.proEnd);
                }
                // 备注；
                var tips="备注：1、个人自评分占比"+dataValue["proportionSelf"]+"%，直接领导评分占比"+dataValue["proportionLeader"]+"%；<br\>" +
                    "      2、绩效考核有异议可以到总经办、绩效主管进行投诉。";
                $("#scoreDetailTable").append("<tr class='desc' style='text-align: left;'><td colspan='10' style='padding:0 5px 0 5px;'>" + tips + "</td></tr>");

                // 权重；
                $("#proportionSelf").val(dataValue["proportionSelf"]);
                $("#proportionLeader").val(dataValue["proportionLeader"]);

                // 计划类型，0为月度计划，1为季度计划，2为年中计划，3为年终计划；
                var typeContent = ["月度计划", "季度计划", "半年计划", "年终计划"];
                var typeContentColor = ["black", "blue", "orange", "red"];
                $("#scoreForm span[data='proType']").html(typeContent[dataValue["proType"]]);
                $("#scoreForm span[data='proType']").css("color", typeContentColor[dataValue["proType"]]);
                $("#scoreForm span[data='proType']").parent().css({
                    "padding": "0 5px 0 5px",
                    "background-color": "white",
                    "text-align": "left"
                });


                var proName = dataValue.proName;
                var userName= data.data.score["userName"];
                // 标题；
                $("#scoreTitle").html("【"+userName+"-绩效考核】考核表");

                dataValue = data.data.detail;
                if (dataValue != null) {
                    // 构建表格；
                    if(schemeType==1){
                        // 生成kpl表格；
                        $(".kpl").show();
                        $(".okr").hide();
                        $("#variableTd").attr("colspan",5);
                        createTable(dataValue);
                    }else {
                        // 生成okr表格；
                        $(".kpl").hide();
                        $(".okr").show();
                        $("#variableTd").attr("colspan",7);
                        createOKRTable(dataValue);
                        $("#scoreModal input[data='plateTarget']").attr("disabled", "disabled");
                    }

                    layui.use('form', function () {
                        var form = layui.form;
                        form.render();
                    });

                    // 禁用输入框；
                    $("#scoreDetailTable tr td").each(function () {
                        var spanElement = $(this).find("input");
                        if (spanElement && spanElement.length > 0) {
                            spanElement.attr("disabled", "disabled");
                        }
                        var textElement = $(this).find("textarea");
                        if (textElement && textElement.length > 0) {
                            textElement.attr("disabled", "disabled");
                        }
                    });
                }
            }

            $("#scoreModal").modal({backdrop: "static"});
        }
    }, "json");
}

// 重新提交流程；
function startPerformance(dataId) {
    alertMessage("系统处理中，请稍候。");
    $.post(baseUrl + "/performanceScore/startPerformance", {scoreId: dataId}, function (data) {
        var message = data.data.message;
        if (message == null) {
            alertMessage(data.msg);
        } else {
            alertMessage(message);
        }
        reloadEmployeeData();
    }, "json");
}

// 审核记录查看；
function showHistory(dataId) {
    showProcessHistory(dataId, 19);
}