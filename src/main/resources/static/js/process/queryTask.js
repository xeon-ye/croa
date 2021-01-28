$(document).ready(function () {
    // 查询表格的校验；
    $("#queryForm").validate({
        rules: {
            urgencyLevel: {digits: true},
            dateStart: {date: true},
            dateEnd: {date: true},
            expectTimeStart: {date: true},
            expectTimeEnd: {date: true}
        }, messages: {
            urgencyLevel: {digits: "请输入正确的紧急程度。"},
            dateStart: {date: "请输入正确的开始日期。"},
            dateEnd: {date: "请输入正确的结束日期。"},
            expectTimeStart: {date: "请输入正确的开始日期。"},
            expectTimeEnd: {date: "请输入正确的结束日期。"}
        }
    });

    // 时间控件；
    var dateStart = {
        elem: '#dateStart',
        format: 'YYYY/MM/DD ',
        // min: laydate.now(), //设定最小日期为当前日期
        // max: laydate.now(),//最大日期
        istime: true,
        istoday: false,
        choose: function (datas) {
            var startTime = $("#dateStart").val();
            var endTime = $("#dateEnd").val();
            if(startTime && endTime && startTime > endTime){
                layer.msg("开始时间不能大于结束时间");
                $("#dateStart").val("")
                return;
            }
            // dateEnd.min = datas; //开始日选好后，重置结束日的最小日期
            // dateEnd.start = datas //将结束日的初始值设定为开始日
        }
    };
    var dateEnd = {
        elem: '#dateEnd',
        format: 'YYYY/MM/DD ',
        //min: laydate.now(),
        // max: laydate.now(),
        istime: true,
        istoday: false,
        choose: function (datas) {
            var startTime = $("#dateStart").val();
            var endTime = $("#dateEnd").val();
            if(startTime && endTime && startTime > endTime){
                layer.msg("开始时间不能大于结束时间");
                $("#dateEnd").val("")
                return;
            }
            // dateStart.max = datas; //结束日选好后，重置开始日的最大日期
        }
    };

    laydate(dateStart);
    laydate(dateEnd);

    // 时间控件；
    var expectTimeStart = {
        elem: '#expectTimeStart',
        format: 'YYYY/MM/DD ',
        // min: laydate.now(), //设定最小日期为当前日期
        // max: laydate.now(),//最大日期
        istime: true,
        istoday: false,
        choose: function (datas) {
            var startTime = $("#expectTimeStart").val();
            var endTime = $("#expectTimeEnd").val();
            if(startTime && endTime && startTime > endTime){
                layer.msg("开始时间不能大于结束时间");
                $("#expectTimeStart").val("")
                return;
            }
            // expectTimeEnd.min = datas; //开始日选好后，重置结束日的最小日期
            // expectTimeEnd.start = datas //将结束日的初始值设定为开始日
        }
    };
    var expectTimeEnd = {
        elem: '#expectTimeEnd',
        format: 'YYYY/MM/DD ',
        //min: laydate.now(),
        // max: laydate.now(),
        istime: true,
        istoday: false,
        choose: function (datas) {
            var startTime = $("#expectTimeStart").val();
            var endTime = $("#expectTimeEnd").val();
            if(startTime && endTime && startTime > endTime){
                layer.msg("开始时间不能大于结束时间");
                $("#expectTimeEnd").val("")
                return;
            }
            // expectTimeStart.max = datas; //结束日选好后，重置开始日的最大日期
        }
    };

    laydate(expectTimeStart);
    laydate(expectTimeEnd);

    // 设置表格默认的UI样式；
    $.jgrid.defaults.styleUI = 'Bootstrap';

    // 窗口拖拽绑定事件；
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#taskTable').setGridWidth(width);
    });

    // 回车执行查询；
    $("body").keydown(function (evt) {
        evt = (evt) ? evt : ((window.event) ? window.event : "");
        var curKey = evt.keyCode ? evt.keyCode : evt.which;
        if (curKey == 13) {//keyCode=13是回车键
            $("#dataSearch").click();
        }
    });
    dataReject1();
    // 查询按钮；
    $("#dataSearch").click(function () {
        if ($("#queryForm").valid()) {
            $("#taskTable").emptyGridParam();
            reloadTaskData();
            reloadTaskData1();
        }
    });

    // // 批量同意按钮；
    // $("#dataAgree").click(function () {
    //     var role =detailsProcess();
    //     $(user.roles).each(function (i,item) {
    //         if(role.contains(item.code)){
    //             //财务出纳、财务会计
    //             layer.confirm("是否需要填写账户信息，如需填写请到审核详情页面进行审核", {
    //                 btn: ["确定", "取消"],
    //                 shade: false
    //             }, function (index) {
    //                 layer.close(index);
    //                 approveData(true);
    //             }, function () {
    //                 return;
    //             })
    //         }else{
    //             //其他角色
    //             approveData(true);
    //         }
    //     })
    //
    // });
    $("#dataAgree").click(function () {
        approveData(true);
    });

    // 批量拒绝按钮；
    $("#dataReject").click(function () {
        if(isCWCN()){
            //财务出纳
            layer.confirm("是否需要填写账户信息，如需填写请到审核详情页面进行审核", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                approveData(false);
            }, function () {
                return;
            })
        }else{
            //其他角色
            approveData(false);
        }
    });


    $("#dataReject1").click(function (){
        approveData1(false);
    });

    $.ajax({
        type: "post",
        url: baseUrl + "/mobile/listInvoiceNotAuditByParam",
        datatype: "json",
        data: {
            sidx: 'taskId',
            sord: 'desc',
            size: 10,
            page: 1,
            typeId:4
        },
        success: function (data) {
            // console.log(data);
        }
    })

    // 初始化数据；
    $("#taskTable").jqGrid({
        url: baseUrl + "/process/list",
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
        sortname: "taskId",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        // 显示序号；
        rownumbers: true,
        rowNum: 10,
        rowList: [10, 25, 50],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "taskId"
        },
        colModel: [
            {name: 'taskId', label: 'taskId', hidden: true, width: 60, sortable: false},
            {name: 'processName', label: '审核项目', width: 100, sortable: false},
            {name: 'dataName', label: '项目名称', width: 200, sortable: false},
            {name: 'state',label:'流程状态',width: 200, hidden:true},
            {name: 'process',label:'流程类型',hidden:true},
            {name: 'itemId',label:'待办id',hidden:true},
            {name: 'dataId',label:'任务id',hidden:true},
            {name: 'dataName',label:'任务标题',hidden:true},
            {
                name: 'urgencyLevel',
                label: '紧急程度',
                width: 60,
                sortable: false,
                formatter: function (value, grid, rowData) {
                    if (rowData.expectTime) {
                        var leftTime = new Date(rowData.expectTime) - new Date();
                        var fiveHour = 5 * 3600 * 1000;
                        var tenHour = 10 * 3600 * 1000;
                        // 小于5小时为紧急；
                        if (leftTime < fiveHour) {
                            rowData.timeColor = "red";
                            return "<b style='color:red;'>紧急</b>";
                            // 5-10小时之间为较急；
                        } else if (leftTime < tenHour && leftTime >= fiveHour) {
                            rowData.timeColor = "darkorange";
                            return "<b style='color:darkorange;'>较急</b>";
                            // 超过10小时为普通；
                        } else {
                            rowData.timeColor = "green";
                            return "<b style='color:green;'>普通</b>";
                        }
                        // 没有期望日期设置为普通；
                    } else {
                        return "<b style='color:green;'>普通</b>";
                    }
                }
            },
            {
                name: 'expectTime',
                label: '剩余时间',
                hidden: true,
                width: 120,
                sortable: false,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        // 计算剩余时间；
                        var leftTime = new Date(value) - new Date();
                        // 换算时间；
                        var hour = parseInt(leftTime / (3600 * 1000), 10);
                        var minute = parseInt((leftTime % (3600 * 1000)) / (60 * 1000), 10);
                        var second = parseInt(((leftTime % (3600 * 1000)) % (60 * 1000)) / 1000, 10);
                        var leftTimeText = "";
                        // 超过一天；
                        if (hour > 24) {
                            leftTimeText = "<b style='color:green;'>超过一天</b>";
                        } else {
                            // 超时；
                            if (leftTime < 0 || (hour == 0 && minute == 0 && second == 0)) {
                                leftTimeText = "<b style='color:red;'>已超时</b>";
                            } else {
                                // 格式化显示；
                                if (hour > 0) {
                                    hour = hour >= 10 ? hour : "0" + hour;
                                    hour += " : ";
                                } else {
                                    hour = "";
                                }
                                if (minute > 0) {
                                    minute = minute >= 10 ? minute : "0" + minute;
                                    minute += " : ";
                                } else {
                                    minute = "";
                                }
                                second = second >= 10 ? second : "0" + second;
                                // 获取前一列传递过来的颜色；
                                leftTimeText = "<b style='color:" + rowData.timeColor + ";'>" + hour + minute + second + "</b>";
                            }
                        }
                        return leftTimeText;
                    } else {
                        return "";
                    }
                }
            },
            {name: 'userName', label: '提交人员', width: 60, sortable: false},
            {name: 'initiatorDeptName', label: '提交部门', width: 80, sortable: false},
            {
                name: 'processDate',
                label: '提交日期',
                width: 80,
                sortable: false,
                formatter: function (value, grid, rowData) {
                    return new Date(value).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {
                name: 'taskStopTime',
                label: '停留时间',
                width: 80,
                sortable: false
            },
            {
                name: 'expectTime',
                label: '期望日期',
                width: 80,
                sortable: false,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd hh:mm:ss");
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'operate', label: "操作", width: 80 , sortable: false,
                formatter: function (value, grid, rowData) {
                    var url = rowData.processUrl;
                    var roles = user.roles;
                    for (var i=0; i<roles.length; i++){
                            if(roles[i].code=='CN' && rowData.state==16){
                                url=rowData.cashierUrl;
                                break;
                            }else if (roles[i].code=='KJ' && rowData.process!=23 && rowData.state==9){
                                url=rowData.accUrl;
                                break;
                            }else if(rowData.approveUser == user.id && rowData.state==26){
                                url=rowData.backUrl;
                                break;
                            } else if ((rowData.process==1 && rowData.state==11 &&roles[i].code =='ZL')){
                                url=rowData.assUrl;
                                break;
                            }
                            else {
                                url = rowData.processUrl;
                            }
                    }

                    // $(user.roles).each(function (i,item) {
                    //     debugger;
                    //     if(item.code=='CN' && item.state==16){
                    //         url=rowData.cashierUrl;
                    //     }else if (item.code=='KJ' && rowData.process!=23 && rowData.state==9){
                    //         url=rowData.accUrl;
                    //     }else if(rowData.approveUser == user.id && rowData.state==26){
                    //         url=rowData.backUrl;
                    //     }else {
                    //         url = rowData.processUrl;
                    //     }
                    // });
                    //type=3做审核管理跳转区分  ，
                    //url += "&returnType=3";
                    url +=(url && url.indexOf("?")>0) ? "&returnType=3" : "?returnType=3" ;
                    // var html = "<a href='" + url + "'>审核</a>";
                    var html = "<a title='"+rowData.processName+"' href=\"javascript:page('"+url+"','"+rowData.processName+"');\">审核</a>";
                    return html;
                },
            }
        ],
        pager: jQuery("#taskTableNav"),
        viewrecords: true,
        caption: "审核列表",
        add: false,
        edit: false,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
        },
        loadComplete: function (a, b, c) {
            $("#taskTable").find("tr").each(function () {
                $(this).children().first().css("width", "50");
            });
        }
    });

    $("#taskTable").jqGrid('setLabel', 'rn', '序号', {
        'text-align': 'center',
        'vertical-align': 'middle',
        "width": "50"
    });

    $("#approved").jqGrid({
        url: baseUrl + "/process/theApproved",
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
        multiselectWidth: 50,
        sortable: "true",
        sortname: "taskId",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        // 显示序号；
        rownumbers: true,
        rowNum: 10,
        rowList: [10, 25, 50],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "taskId"
        },
        colModel: [
            {name: 'taskId', label: 'taskId', hidden: true, width: 60, sortable: false},
            {name: 'processName', label: '审核项目', width: 100, sortable: false},
            {name: 'dataName', label: '项目名称', width: 200, sortable: false},
            {
                name: 'urgencyLevel',
                label: '紧急程度',
                width: 60,
                sortable: false,
                formatter: function (value, grid, rowData) {
                    if (rowData.expectTime) {
                        var leftTime = new Date(rowData.expectTime) - new Date();
                        var fiveHour = 5 * 3600 * 1000;
                        var tenHour = 10 * 3600 * 1000;
                        // 小于5小时为紧急；
                        if (leftTime < fiveHour) {
                            rowData.timeColor = "red";
                            return "<b style='color:red;'>紧急</b>";
                            // 5-10小时之间为较急；
                        } else if (leftTime < tenHour && leftTime >= fiveHour) {
                            rowData.timeColor = "darkorange";
                            return "<b style='color:darkorange;'>较急</b>";
                            // 超过10小时为普通；
                        } else {
                            rowData.timeColor = "green";
                            return "<b style='color:green;'>普通</b>";
                        }
                        // 没有期望日期设置为普通；
                    } else {
                        return "<b style='color:green;'>普通</b>";
                    }
                }
            },
            {
                name: 'expectTime',
                label: '剩余时间',
                hidden: true,
                width: 120,
                sortable: false,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        // 计算剩余时间；
                        var leftTime = new Date(value) - new Date();
                        // 换算时间；
                        var hour = parseInt(leftTime / (3600 * 1000), 10);
                        var minute = parseInt((leftTime % (3600 * 1000)) / (60 * 1000), 10);
                        var second = parseInt(((leftTime % (3600 * 1000)) % (60 * 1000)) / 1000, 10);
                        var leftTimeText = "";
                        // 超过一天；
                        if (hour > 24) {
                            leftTimeText = "<b style='color:green;'>超过一天</b>";
                        } else {
                            // 超时；
                            if (leftTime < 0 || (hour == 0 && minute == 0 && second == 0)) {
                                leftTimeText = "<b style='color:red;'>已超时</b>";
                            } else {
                                // 格式化显示；
                                if (hour > 0) {
                                    hour = hour >= 10 ? hour : "0" + hour;
                                    hour += " : ";
                                } else {
                                    hour = "";
                                }
                                if (minute > 0) {
                                    minute = minute >= 10 ? minute : "0" + minute;
                                    minute += " : ";
                                } else {
                                    minute = "";
                                }
                                second = second >= 10 ? second : "0" + second;
                                // 获取前一列传递过来的颜色；
                                leftTimeText = "<b style='color:" + rowData.timeColor + ";'>" + hour + minute + second + "</b>";
                            }
                        }
                        return leftTimeText;
                    } else {
                        return "";
                    }
                }
            },
            {name: 'userName', label: '提交人员', width: 60, sortable: false},
            {name: 'initiatorDeptName', label: '提交部门', width: 80, sortable: false},
            {
                name: 'processDate',
                label: '提交日期',
                width: 80,
                sortable: false,
                formatter: function (value, grid, rowData) {
                    return new Date(value).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {
                name: 'expectTime',
                label: '期望日期',
                width: 80,
                sortable: false,
                formatter: function (value, grid, rowData) {
                    if (value) {
                        return new Date(value).format("yyyy-MM-dd hh:mm:ss");
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'operate', label: "操作", width:80, sortable: false,
                formatter: function (value, grid, rowData) {
                    // console.log(rowData.state);
                    var url = rowData.dataUrl
                    $(user.roles).each(function (i,item) {
                        if(item.code=='CN'){
                            url=rowData.dataUrl;
                        }
                    });
                    //type=3做审核管理跳转区分  ，
                    url += "&returnType=5";
                    // var html = "<a href='" + url + "'>审核</a>";
                    if(rowData.state && rowData.dataUrl.indexOf("flag=")!=-1){
                        var html = "<a title='"+rowData.processName+"' href=\"javascript:page('"+url+"','"+rowData.processName+"');\">详情查看</a>";
                        return html;

                    }else {
                        return "";

                    }
                },
            }
        ],
        pager: jQuery("#approvedNav"),
        viewrecords: true,
        caption: "已审核列表",
        add: false,
        edit: false,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
        },
        loadComplete: function (a, b, c) {
            $("#taskTable").find("tr").each(function () {
                $(this).children().first().css("width", "50");
            });
        }
    });

    $("#taskTable").jqGrid('setLabel', 'rn', '序号', {
        'text-align': 'center',
        'vertical-align': 'middle',
        "width": "50"
    });

    //listDeptUser("#userName");

});

// 重新载入数据；
function reloadTaskData() {
    // $("#taskTable").jqGrid('setGridParam', $("#queryForm").serializeJson()).trigger("reloadGrid");
    // reloadGrid("taskTable","queryForm");
    $("#taskTable").reloadCurrentData(baseUrl + "/process/list", $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}

// 重新载入数据；
function reloadTaskData1() {
    // $("#taskTable").jqGrid('setGridParam', $("#queryForm").serializeJson()).trigger("reloadGrid");
    // reloadGrid("taskTable","queryForm");
    $("#approved").reloadCurrentData(baseUrl + "/process/theApproved", $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}

//财务批量拒绝流程
function  approveData1(flag) {
    var ids = $("#taskTable").jqGrid("getGridParam", "selarrrow");
    var rows = $("#taskTable").jqGrid('getRowData');
    var param ={list:[]};
    if (ids && ids.length>0){
        for (var j =0 ; j<rows.length; j++){
            var taskId = rows[j].taskId || "";
            var process = rows[j].process || "";
            if (ids.contains(taskId) && taskId && process ){
                param.list.push(taskId+":"+process);
            }
        }
    }else {
        alertMessage("请选择要操作的数据。");
    }
    if (ids.length>0){
        alertMessage("系统处理中，请稍候。");
        startModal("#dataReject1");
        param.itemIds = ids.toString();
        param.agree=flag;
        param.desc = $("#desc").val();
        param.listStr = param.list.join(",");
        $.post(baseUrl + "/process/refused",param,function (data) {
            Ladda.stopAll();
            if (data.code == 1001) {
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
                return true;
            }
            if (data.code == 1002) {
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
                return true;
            }
            if (data.data.message == null) {
                getResCode(data);
            } else {
                alertMessage(data.data.message);
            }
            reloadTaskData();
            reloadTaskData1();
        }, "json")
    }
}

// 批量操作数据；
function approveData(flag) {
    var flag1 = true;
    var ids = $("#taskTable").jqGrid("getGridParam", "selarrrow");
    //获取选中流程详情
    var rows = $("#taskTable").jqGrid('getRowData');
    if (ids.length > 0) {
        alertMessage("系统处理中，请稍候。");
        if (flag == true) {
            for(var i=0;i<ids.length ; i++){
                for (var j =0 ; j<rows.length; j++){
                    if (ids[i]==rows[j].taskId){
                        //请款出纳节点，不是请款唤醒流程，就要到审核详情页审核  或者  请款财务会计节点不是唤醒流程也是要到审核详情页审核
                            var role =detailsProcess();
                            $(user.roles).each(function (i,item) {
                                if ( rows[j].process != 23 && role && role.contains(item.code)) {
                                // if (rows[j].state == 16 && role.contains(item.code)) {
                                    swal({
                                        title: "系统提示",
                                        text: "需要确认出款信息，请到详情页审核通过"
                                    });
                                     flag1 = false;
                                }

                            });
                            //如果是绩效考核需要填写考核分数，不能批量审核
                        if (rows[j].process == 19) {
                            setTimeout(function () {
                                swal({
                                    title: "系统提示",
                                    text: "绩效流程需要进行考核打分，请点击列表中的审核进行审批"
                                });
                            },1500);
                            flag1 = false;
                        }

                    }

                }
            }
            startModal("#dataAgree");
        } else {
            startModal("#dataReject");
        }
        if(flag1){
            $.post(baseUrl + "/process/apply", {
                taskIds: ids.toString(),
                agree: flag,
                desc: $("#desc").val()
            }, function (data) {
                Ladda.stopAll();
                if (data.code == 1001) {
                    swal({
                        title: "异常提示",
                        text: data.msg,
                    });
                    return true;
                }
                if (data.code == 1002) {
                    swal({
                        title: "异常提示",
                        text: data.msg,
                    });
                    return true;
                }
                if (data.data.message == null) {
                    getResCode(data);
                } else {
                    alertMessage(data.data.message);
                }
                reloadTaskData();
                reloadTaskData1();
            }, "json").error(function () {
                Ladda.stopAll();//隐藏加载按钮
            });
        }else {
            Ladda.stopAll();//隐藏加载按钮
        }

    } else {
        alertMessage("请选择要操作的数据。");
    }
}

// 图片上传的地址（同时也必须是保存该数据的字段名称，此限制主要用于图片展示）；
function getUploadUrl() {
    return "inform_image";
}
function getDept() {
    var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
    var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
    var deptDiv = document.getElementById("deptDiv");
    //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务  媒介部门
    if ((currentDeptQx || currentCompanyQx || isZC()) || user.dept.code == 'CW') {
        $("#selDept").click(function () {
            $("#deptModal").modal('toggle');
        });
        $('#treeview').treeview({
            data: [getTreeData(isZC())],
            onNodeSelected: function (event, data) {
                $("#companyCode1").val("");//每次选择时，先清空
                $("#initiatorDeptName").val("");//每次选择时，先清空
                $("#chooseDeptName").val(data.text);
                $("#deptModal").modal('hide');
                $("#initiatorDeptName").val(data.text);
                $("#userType").val(data.code);
            }
        });
        $("#cleanDept").click(function () {
            $("#userType").val("");
            $("#companyCode1").val("");//清空
            $("#initiatorDeptName").val("");
            $("#chooseDeptName").val("");
        });
    }
}
//获取部门树数据
function getTreeData(isZC) {
    var deptTreeData = {};
    var deptId = user.dept.id;//当前用户部门ID
    var deptCode = user.dept.code;//当前部门编码
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    if (deptCompanyCode == "JT" && (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL")) {
        requestData(null, "/dept/getRootDept", "POST", function (result) {
            var root = result.data.root;
            if (root) {
                deptId = root.id;//整个集团的业务和媒介部
            } else {
                deptId = 517;//整个集团的业务和媒介部
            }
        });
    } else if (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL") {
        requestData({companyCode: deptCompanyCode}, "/dept/getCompanyByCode", "POST", function (result) {
            var company = result.data.company;
            if (company) {
                deptId = company.id;//整个集团的业务和媒介部
            }
        });
    }
    //具体查询
    requestData({deptId: deptId}, "/dept/listForSonTreeView", "POST", function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
    /*var deptTreeData = {};
    $.ajax({
        type: "POST",
        url: baseUrl + "/dept/listForSonTreeView",
        data: {deptId: deptId},
        dataType: "json",
        async: false,
        success: function (result) {
            var arrays = result.data.list;
            if (arrays != null && arrays.length > 0) {
                deptTreeData = arrays[0];
            }
        }
    });
    return deptTreeData;*/
}
//判断当前用户是否总裁
var isZC = function () {
    var roles = user.roles;//获取用户角色
    var isZC = false;//是否总裁角色
    if (roles) {
        for (var i = 0; i < roles.length; i++) {
            if (roles[i].code == 'ZC' || roles[i].code == 'FZC') {
                isZC = true;
                break;
            }
        }
    }
    return isZC;
}

function isCWCN() {
    var roles = user.roles;
    var flag = false;
    if(roles!=null && roles.length>0){
        for(var i =0;i<roles.length;i++){
            if(roles[i].code=='CN' && roles[i].type=='CW'){
                flag=true;
                break;
            }else{
                flag=false;
            }
        }
    }
    return flag;
}

function isCWKJ() {
    var roles = user.roles;
    var flag = false;
    if(roles!=null && roles.length>0){
        for(var i =0;i<roles.length;i++){
            if(roles[i].code=='KJ' && roles[i].type=='CW'){
                flag=true;
                break;
            }else{
                flag=false;
            }
        }
    }
    return flag;
}



function isCW() {
    var roles = user.roles;
    var flag = false;
    if(roles!=null && roles.length>0){
        for(var i =0;i<roles.length;i++){
            if(roles[i].type=='CW'){
                flag=true;
                break;
            }else{
                flag=false;
            }
        }
    }
    return flag;
}

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

function dataReject1() {
    if(isCW()){
        $("#dataReject1").show();
        $("#dataReject").hide();

    }else {
        $("#dataReject1").hide();
        $("#dataReject").show();
    }

}

function detailsProcess() {
    var configurationRole;
    $.ajax({
        type:"get",
        url:baseUrl+"/sysConfig/getAllConfig",
        dataType:"json",
        async:false,
        success:function (data) {
            if (data && data.details){
                configurationRole=data.details.value;
            }
        }
    });
    return configurationRole;
}