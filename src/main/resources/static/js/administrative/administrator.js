$(function () {
    //请假添加按钮
    $("#addBtn").click(function () {
        $("#editForm").find("input").removeClass("error");
        $("#editForm").find("textarea").removeClass("error");
        $("#editForm").validate().resetForm();
        $("#editForm input[name='type']").removeAttr("checked");
        $("#editForm input[name='type']").parent().removeClass("checked");
        document.getElementById("editForm").reset();
        $("#editForm input").val('');
        zwUserList("#zwExamine");//渲染政委列表
        $("#editModal").modal('toggle');
        $(".save").show();
        $(".update").hide();
        $("#fileInfo").empty();
        $("#picInfo").empty();
        $("#id").empty();
        initializeLeaveType();
    });
    if (hasRoleXZ()) {
        $("#exportContent").show();
        $("exportList").show();
    } else {
        $("#exportContent").hide();
        $("exportList").hide();
    }


    //加班添加按钮
    $("#workAddBtn").click(function () {
        $("#workEditForm").find("input").removeClass("error");
        $("#workEditForm").find("textarea").removeClass("error");
        $("#workEditForm").validate().resetForm();
        $("#workEditForm input[name='type']").removeAttr("checked");
        $("#workEditForm input[name='type']").parent().removeClass("checked");
        document.getElementById("workEditForm").reset();
        $("#workEditForm input").val('');
        $("#workEditModal").modal('toggle');
        $(".save").show();
        $(".update").hide();
        $("#fileInfo2").empty();
        $("#picInfo2").empty();
        $("#workId").empty();
        getVacationTime1();
    });

    //外出添加按钮
    $("#outWorkAddBtn").click(function () {
        $("#outWorkEditForm").find("input").removeClass("error");
        $("#outWorkEditForm").find("textarea").removeClass("error");
        $("#outWorkEditForm").validate().resetForm();
        $("#outWorkEditForm input[name='type']").removeAttr("checked");
        $("#outWorkEditForm input[name='type']").parent().removeClass("checked");
        document.getElementById("outWorkEditForm").reset();
        $("#outWorkEditForm input").val('');
        $("#outWorkEditModal").modal('toggle');
        $(".save").show();
        $(".update").hide();
        $("#picInfo3").empty();
        $("#fileInfo3").empty();
        $("#outWorkId").empty();
    });
    $("#onBusinessPlan").click(function () {
        $("#businessConclusion").modal("hide");
        $("#viewPlan").modal({backgroud: "static"});
        $("#onBusinessAuditTrue").hide();
        $("#onBusinessAuditFalse").hide();
        $("#onBusinessCancel1").show();
    });
    $("#onBusinessConclusion").click(function () {
        $("#viewPlan").modal("hide");
        $("#businessConclusion").modal({backgroud: "static"});
        $("#onBusinessReject1").hide();
        $("#onBusinessPass1").hide();
        if($("#taskState").val() == 1){
            $("#updateReport1").hide();
        }
        if($("#taskState").val() == 22){

            $("#onBusinessTaskId3").show();
            $("#onBusinessPass1").show();
            $("#onBusinessReject1").show();

        }
    });
    $("#onBusinessCancel1").click(function () {
        $("#addPlan").modal("hide");
        $("#businessConclusion").modal({backgroud: "static"});
    })

    //出差添加按钮
    $("#onBusinessBtn").click(function () {
        $("#editorPlanFrom").find("input").removeClass("error");
        $("#editorPlanFrom").find("textarea").removeClass("error");
        $("#editorPlanFrom").validate().resetForm();
        $("#editorPlanFrom input[name='type']").removeAttr("checked");
        $("#editorPlanFrom input[name='type']").parent().removeClass("checked");
        $("#editorPlanFrom")[0].reset();
        $("#editorPlanFrom input[type='hidden']").val('');
        $("#applicationDate2").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
        $("#deptName2").val(user.dept.name);
        $("#applyName2").val(user.name);
        $("#addPlan").modal('toggle');
        $("#separate2").iCheck('check');
        //$("input[name= 'separate']").iCheck({handler:'checkbox', radioClass:'iradio_square-red'});
        $(".save").show();
        $(".update").hide();
        $(".view").hide();
        $("#fileInfo7").empty();
        $("#picInfo7").empty();
        $("#onBusinessCancel").show();
        $("#addPlan input[name='placeType']").removeAttr("checked");
        $("#addPlan input[name='placeType']").parent().removeClass("checked");
        $("#addPlan input[name='reason']").removeAttr("checked");
        $("#addPlan input[name='reason']").parent().removeClass("checked");
        $("#addPlan input[name='traffic']").removeAttr("checked");
        $("#addPlan input[name='traffic']").parent().removeClass("checked");
        // $("#addPlan input[name='separate']").removeAttr("checked");
        // $("#addPlan input[name='separate']").parent().removeClass("checked");
        $("#onBusinessAuditTrue").hide();
        $("#onBusinessAuditFalse").hide();
        $("#onBusinessFileInfo").empty();
        $("#onBusinessPicInfo").empty();
        $("#onBusinessId").empty();
        $("#fileInfo4").empty();
        $("#picInfo4").empty();
        $("#onBusinessSave").show();
        $("#attachment").show();
        $("#onBusinessFile").show();
        $("#onBusinessPic").show();
        //刷新select 下拉框
        layui.use(["form"], function () {
            layui.form.render();
        });
        selectUser();
        $("#businessConclusion").hide();
        $("#onBusinessConclusion").hide();
        $("#confirm").hide();
        $("#cs").hide();
        $("#ccinput").hide();
    });
    $("#businessType2").find(".i-checks").on("ifClicked",function () {
        var lin = $(this).find("input").val();
        changeLicence(lin);
    });
    $("#businessType").find(".i-checks").on("ifClicked",function () {
        var lin = $(this).find("input").val();
        changeLicence(lin);
    });

    $("#administrativeType").change(function () {
        if($("#administrativeType").val()==1){
            $("#stateTimeName").text("请假开始日期:");
            $("#endTimeName").text("请假结束日期:");
        }else if ($("#administrativeType").val()==2){
            $("#stateTimeName").text("加班开始日期:");
            $("#endTimeName").text("加班结束日期:");
        }else if($("#administrativeType").val()==3){
            $("#stateTimeName").text("外出开始日期:");
            $("#endTimeName").text("外出结束日期:");
        }else if ($("#administrativeType").val()==4){
            $("#stateTimeName").text("出差开始日期:");
            $("#endTimeName").text("出差结束日期:");
        }else if ($("#administrativeType").val()==""){
            $("#stateTimeName").text("流程开始日期:");
            $("#endTimeName").text("流程结束日期:");
        }

    })

    //获取下拉框
    getTax();

    //获取剩余调休时间
    //getVacationTime();

    // 查询表单增加校验；
    $("#queryForm").validate();

    // 审核表单增加校验；
    $("#approveForm").validate();


    // 设置表格默认的UI样式；
    $.jgrid.defaults.styleUI = 'Bootstrap';

    // 窗口拖拽绑定事件；
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#entryTable').setGridWidth(width);
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
            reloadTaskData(1);
        }
    });
    /*$('input[type=radio][name=isExpense]').change(function() {
        if (this.value == '1') {
            //如果是是，则跳转到借款页面
            var admId = $("#onBusinessAdministrativeId").val();
            var addScheme = "/fee/queryBorrow?admId="+admId;
            page(addScheme, "新增借款流程");
        }
        else if (this.value == 'no') {

        }
    });*/
    $("#account").click(function () {
        var addId = $("#administrativeId1").val();
        var addaccount = "/fee/expenseReimbursement?addId="+addId;
        page(addaccount,"新增报销");

    });

    // 修改查询条件表单的Lable右间距；
    $(".control-label").css({"padding": "7px 0 0 0", "margin-right": "-10px", "width": "80px"});

    //审批链接打开页面
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        var type = getQueryString("type");
        if (type == 1) {
            view(1, getQueryString("id"), getQueryString("flag"));
        } else if (type == 2) {
            timeWorkView(getQueryString("id"), getQueryString("flag"));
        } else if (type == 3) {
            outWorkView(getQueryString("id"), getQueryString("flag"));
        } else if (type == 4) {
            onBusinessView(getQueryString("id"), getQueryString("flag"),getQueryString("rtype"));
        }else if(type == 5){
            edit(getQueryString("id"),4);
        }
    }
    ;

    //导出·列表
    $("#exportList").click(function () {
        var params = $("#queryForm").serializeJson();
        location.href = "/administrative/exportList" + "?" + $.param(params);
    })
    //导出内容
    $("#exportContent").click(function () {
        if ($("#administrativeType").val() != 0) {
            var params = $("#queryForm").serializeJson();
            location.href = "/administrative/exportContent" + "?" + $.param(params);
        } else {
            swal("请先筛选出你要导的类型");
            return;
        }

    })

    // 初始化数据；
    $("#entryTable").jqGrid({
        url: baseUrl + "/administrative/getAdministrative",
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
        sortname: "entry_id",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,

        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "entry_id"
        },
        colModel: [
            {name: 'id', label: '行政流程id', hidden: true, width: 120},
            {name: 'taskId', label: '审批任务id', hidden: true, width: 120},
            {name: 'administrativeType', label: '类型id', hidden: true, width: 120},
            {name: 'administrativeName', label: '类型', width: 120},
            {name: 'title', label: '名称', width: 120},
            {name: 'beginTime', label: '开始时间', width: 120},
            {name: 'endtime', label: '结束时间', width: 120},
            {name: 'administrativeTime', label: '天数', width: 120,formatter: function (value, grid, rowData){
                if(rowData.administrativeType==4){
                    return value+"天";
                }else {
                    if(value <= 2){
                        return value+"小时";
                    }
                    else if ((value%4)>0){
                        return day = Math.floor(value / 4) * 0.5 + 0.5 +"天";
                    }else if( (value%4) == 0){
                        return day = Math.floor(value / 4) * 0.5 +"天";
                    }
                }
                // else{
                //     if(value<=2){
                //         return value+"小时";
                //     }else if(value<=4 && value>2){
                //         return "0.5天"
                //     }else if (value>8 && value <=12 ){
                //         return "1.5天"
                //     }else if (  value>=13 && (value%4)>0 ){
                //         return day = Math.floor(value / 4) * 0.5 + 0.5 +"天";
                //     } else{
                //         day = Math.ceil(value/8);
                //         return day + "天";
                //     }
                // }
                }
                },

            {
                name: 'approveState', label: '审批状态', width: 120, formatter: function (value, grid, rowData) {
                    var approveStateName;
                    switch (rowData.approveState) {
                        case -1:
                            approveStateName = '驳回';
                            break;
                        case 1:
                            approveStateName = '审核中';
                            break;
                        case 2:
                            approveStateName = '审核完成';
                            break;
                    }
                    return approveStateName == null ? "" : approveStateName;
                }
            },
            {
                name: 'createTime', label: '发起时间', width: 120, formatter: function (value, grid, rowData) {
                    if (value == null) {
                        return "";
                    } else {
                        return new Date(value).format("yyyy-MM-dd hh:mm:ss");
                    }
                }
            },
            {name: 'finishTime', label: '办结时间', width: 120},
            {name: 'empName', label: '申请人', width: 120},
            {
                name: 'operate', label: "操作", width: 250, sortable: false, index: '',
                formatter: function (value, grid, rowData) {
                    var html = "";
                    var entryId = rowData.entry_id;

                    // 如果有流程审批状态则有审批记录；
                    if (rowData.approve_state) {
                        html += '<a href="#" onclick="showHistory(' + "'" + administrativeId + "'" + ');return false;">记录</a>&nbsp;|&nbsp;';
                    }
                    if (rowData.taskId != null && rowData.taskId != '') {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='showHistory(" + rowData.id + "," + rowData.administrativeTime + "," + rowData.administrativeType + ")'>审批详情&nbsp;&nbsp;</a>";
                    }
                    if ((rowData.approveState == 1) && rowData.empId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='returnBack(" + rowData.id +"," + "\"" + rowData.taskId + "\"," + rowData.itemId + ","+rowData.administrativeType+","+rowData.administrativeTime+")'>撤回&nbsp;&nbsp;</a>";
                    }
                    if ((rowData.state == 0 || rowData.state == -1) && rowData.empId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='edit(" + rowData.id + "," + rowData.administrativeType + ")'>编辑&nbsp;&nbsp;</a>";
                    }
                    if ((rowData.state == 0 || rowData.state == -1) && rowData.empId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red;'  onclick='del(" + rowData.id + "," + rowData.administrativeType + ")'>删除&nbsp;&nbsp;</a>";
                    }
                    /*if((rowData.administrativeType == 4) && (rowData.approveState == 2) && rowData.empId == user.id){
                        html += "<a href='/administrative/onBusiness?id=" + rowData.id + "&type=1"+"' target='_blank'>出差总结</a>";
                    }
                    if((rowData.administrativeType == 4) && (rowData.approveState == 3) && rowData.empId == user.id){
                        html += "<a href='/administrative/onBusiness?id=" + rowData.id + "&type=2"+"' target='_blank'>出差总结</a>";
                    }
                    if((rowData.administrativeType == 4) && (rowData.approveState == 3) && rowData.empId != user.id){
                        html += "<a href='/administrative/onBusiness?id=" + rowData.id + "&type=3"+"' target='_blank'>出差总结</a>";
                    }*/
                    return html;
                },
            }
        ],
        pager: jQuery("#entryTableNav"),
        viewrecords: true,
        caption: "审批列表",
        add: false,
        edit: false,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            // $(".cbox").addClass("icheckbox_square-green");
        },
        loadComplete: function (a, b, c) {
            $("#entryTable").find("tr").each(function () {
                $(this).children().first().css("width", "50");
            });
        },
        ondblClickRow: function (rowid) {
            var rowData = jQuery("#entryTable").jqGrid("getRowData", rowid);
            if (rowData.administrativeType == 1) {
                view(rowData.administrativeType, rowData.id, -1);
            } else if (rowData.administrativeType == 2) {
                timeWorkView(rowData.id, -1);
            } else if (rowData.administrativeType == 3) {
                outWorkView(rowData.id, -1);
            } else if (rowData.administrativeType == 4) {
                onBusinessView(rowData.id, -1);
            }
        },
    });

    $("#entryTable").jqGrid('setLabel', 'rn', '序号', {
        'text-align': 'center',
        'vertical-align': 'middle',
        "width": "50"
    });

    $("#entryTable").setGridHeight(370);

});

//请假自定义规则，补请假不能超过一天
function validationAddLeave(validDate, num, id) {
    var validateFlag = true; //默认校验成功
    var errorMessage = "";
    $.ajax({
        type: "post",
        url: baseUrl + "/administrativeLeave/validationAddLeave",
        data: {endDate: validDate, num : num, id:id},
        dataType: "json",
        async: false,
        success: function (data) {
            if (data.code != 200) {
                validateFlag = false;
                errorMessage = data.msg || "请假申请不能超过一个工作日补！";
            }
        }
    });
    if(!validateFlag){
        $("#endTime-error").css("display", "inline-block");
        $("#endTime-error").text(errorMessage);
    }else{
        $("#endTime-error").css("display", "none");
    }
    return validateFlag;
}

//公共请求方法
function requestData(data, url, requestType,dataType,async,callBackFun, contentType) {
    var param = {
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun
    };
    if(contentType){
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
}

//获取用户部门政委人员列表
function zwUserList(selectId) {
    //发起人不是部长，需要经过政委
    if(user.dept.mgrId != user.id || user.dept.level > 3){
        $("#zwDiv").css("display", "block");
        $("#zwDiv").attr("hideFlag", "show");
        requestData(null, "/deptZw/listUser/"+user.dept.id, "get", "json", false, function (data) {
            //如果有政委人员，则显示
            if(data && data.length > 0){
                $("#zwDiv").css("display", "block");
                $("#zwDiv").attr("hideFlag", "show");
                $(selectId).empty();
                layui.use(["form"], function () {
                    var html = "";
                    if(data.length > 1){
                        html += "<option value=\"\">请选择</option>";
                    }
                    $(data).each(function (i, d) {
                        html += "<option data-deptId='"+d.deptId+"' value='" + d.id + "'>" + d.name + "</option>"
                    });
                    $(selectId).html(html);
                    layui.form.render();
                });
            }else {
                $("#zwDiv").css("display", "none");
                $("#zwDiv").attr("hideFlag", "none");
            }
        });
    }else {
        $("#zwDiv").css("display", "none");
        $("#zwDiv").attr("hideFlag", "none");
    }
}


//根据部门id获取出差人员
function getEmpByDeptId(deptId) {
    $.post(baseUrl + "/user/queryUserByDeptIdONLY", {deptId: deptId}, function (data) {
        var userObj = $("#onBusinessEditForm select[name='empId']");
        // 先清空；
        userObj.empty();
        var dataValue = data;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                //新增时默认出差人为登录人
                $("#empId").val(user.id);
                $("#empName1").val(user.name);
                var oldUser = $("#empId").val();
                oldUser = oldUser.length > 0 ? oldUser : dataValue[0].id;
                for (var i = 0; i < dataValue.length; i++) {
                    if (oldUser == dataValue[i].id) {
                        userObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        userObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                }
            }
            // 初始化；
            var form = layui.form;
            layui.form.on("select(empId)", function (empId) {
                // 刷新部门的人员数据；
                $("#empName1").val($("#empIdSelect .layui-this").text());
                //getDuty(empId.value);
                layui.form.render('select');
            });
            layui.form.render();
        }
    }, "json");
}


//根据部门id获取代理人员
function getAgentByDeptId(deptId) {
    $.post(baseUrl + "/user/queryUserByDeptIdONLY", {deptId: deptId}, function (data) {
        var userObj = $("#onBusinessEditForm select[name='agentId']");
        // 先清空；
        userObj.empty();
        var dataValue = data;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                var oldUser = $("#agentId").val();
                oldUser = oldUser.length > 0 ? oldUser : dataValue[0].id;
                for (var i = 0; i < dataValue.length; i++) {
                    if (oldUser == dataValue[i].id) {
                        userObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        userObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                }
            }

            layui.form.on("select(agentId)", function (agentId) {
                $("#agentName").val($("#agentIdSelect .layui-this").text());
                layui.form.render('select');
            });
            // 初始化；
            layui.form.render();
        }
    }, "json");
}


//根据部门id获取陪同人员
function getEscortByDeptId(deptId) {
    $.post(baseUrl + "/user/queryUserByDeptIdONLY", {deptId: deptId}, function (data) {
        var userObj = $("#onBusinessEditForm select[name='escortId']");
        // 先清空；
        userObj.empty();
        var dataValue = data;
        if (dataValue == null) {
            getResCode(data);
        } else {
            if (dataValue.length > 0) {
                var oldUser = $("#escortId").val();
                oldUser = oldUser.length > 0 ? oldUser : dataValue[0].id;
                for (var i = 0; i < dataValue.length; i++) {
                    if (oldUser == dataValue[i].id) {
                        userObj.append("<option value='" + dataValue[i].id + "' selected='selected'>" + dataValue[i].name + "</option>");
                    } else {
                        userObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                    }
                }
            }
            layui.form.on("select(escortId)", function (escortId) {
                $("#escortName").val($("#escortIdSelect .layui-this").text());
                layui.form.render('select');
            });
            // 初始化；
            layui.form.render();
        }
    }, "json");
}


//通过用户id获取用户职位
function getDuty(userId) {
    $.ajax({
        type: "get",
        url: "/user/" + userId + "",
        dataType: "json",
        success: function (data) {
            $("#empDuty1").empty();
            if (data != null) {
                $("#empDuty1").val(data.postName);
            }
        }
    })
}


//获取请假下拉框
function getTax() {
    $.ajax({
        type: "get",
        url: "/administrativeLeaveType/getLeaveType",
        dataType: "json",
        success: function (data) {
            $("#leaveType").empty();
            var html = "";
            if (data != null) {
                for (var i = 0; i < data.data.leaveType.length; i++) {
                    html += "<option value='" + data.data.leaveType[i].typeId + "' >" + data.data.leaveType[i].typeName + "</option>";
                }
                html += "</select>";
                $("#leaveType").append(html);
            }
        }
    })
}

//请假类型初始化
function initializeLeaveType() {
    var v = document.getElementById("leaveType").value;
    var sbtitle = document.getElementById("vacationType");
    var annualLeave = document.getElementById("annualLeaveType");
    //当请假类型为调休的时候，显示调休的剩余时长
    if (v == "1") {
        //显示div
        sbtitle.style.display = 'block';
        annualLeave.style.display = 'none';
        //获取调休时间
        getVacationTime();
    } else if (v == "2" || v == "6" || v == "7" || v == "8" || v == "10") {
        sbtitle.style.display = 'none';
        annualLeave.style.display = 'block';
        getAnnualLeaveByTypeId();
    } else {
        annualLeave.style.display = 'none';
        sbtitle.style.display = 'none';
    }
}
//根据用户查询到出差审批人、批准人
function selectUser() {
    $.ajax({
        type:"post",
        url:"/userBusinessPlan/selectUser",
        dataType:"json",
        success:function(data){
            $("#reviewerUser").empty();
            //赋值
          $("#reviewerUser2").val(data.data.reviewerUser);
          for(var i= 0; i < data.data.approverUser.length; i++){
          $("#approverUser2").val(data.data.approverUser[i].name);}
        }
    })
}

//当请假类型为调休时，获取该员工的请假时间，并显示需要调休的时间
$(document).on("change", 'select#leaveType', function () {
    initializeLeaveType();
});

//请假模块，通过用户id，获取剩余调休时间
function getVacationTime() {
    $.ajax({
        type: "get",
        url: "/administrativeVacationTime/getVacationTime",
        dataType: "json",
        success: function (data) {
            $("#vacationTime1").empty();
            //赋值
            if (data != null) {
                $("#vacationTime1").val(data.data.vacaTime ? data.data.vacaTime.vacationTime : 0);
            }
        }
    })
}

//加班模块获取剩余调休时间
function getVacationTime1() {
    $.ajax({
        type: "get",
        url: "/administrativeVacationTime/getVacationTime",
        dataType: "json",
        success: function (data) {
            $("#vacationTime2").empty();
            //赋值
            if (data != null) {
                $("#vacationTime2").val(data.data.vacaTime == null ? 0 : data.data.vacaTime.vacationTime);
            }
        }
    })
}

//通过用户id和请假类型获取假期类型的数据
function getAnnualLeaveByTypeId() {
    $.ajax({
        type: "get",
        url: "/annualLeave/getAnnualLeaveByTypeId",
        dataType: "json",
        success: function (data) {
            $("#vacationTime1").empty();
            //赋值
            if (data.data.entity != null) {
                $("#vacationTime1").val(data.data.entity.surplusTime);
                if (data.data.entry.surplusTime == 0) {
                    //当请假类型为0的时候不让选择该类型假期（年假，婚假，陪产假，产假）
                    alert("该类型假期已经休完，请另选择请假类型！")
                }
            }
        }
    })
}

// 重新载入数据；
function reloadTaskData(t) {
    $("#entryTable").emptyGridParam();
    $("#entryTable").reloadCurrentData(baseUrl + "/administrative/getAdministrative1/"+t, $("#queryForm").serializeJson(), "json", null, function () {
        // 单选框居中；
        $(".cbox").addClass("icheckbox_square-green");
    });
}

//flag=1审批页面，flag=-1查看页面
function view(administrativeType, administrativeId, flag) {
    if (flag == 1) {
        $("#auditTrue").show();
        $("#auditFalse").hide();
    }else  if(flag==-1){
        $("#auditTrue").hide();
        $("#auditFalse").show();
        var returnType= getQueryString("returnType");
        if(returnType==5){
            $("#cancel1").hide();
            $("#goback").show();
        }else{
            $("#cancel1").show();
            $("#goback").hide();
        }
    }
    else if(flag){
        $("#auditTrue").show();
        $("#auditFalse").hide();
        $("#reject1").hide();
        $("#pass1").hide();
    }
    else {
        $("#auditTrue").hide();
        $("#auditFalse").show();
        //查看页面返回按钮隐藏
        $("#cancel1").show();
        $("#goback").hide();
        $("#review").hide();
    }
    $("#viewModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/administrative/getAdministrativeDetail",
        data: {
            administrativeType: administrativeType,
            administrativeId: administrativeId
        },
        dataType: "json",
        success: function (data) {
            if (data == null) {
                alert("没有查到相关数据");
            }
            for (var attr in data.data.entity) {
                if (attr == "picture") {
                    $("#picInfo1").empty();
                    //后台返回去的数据转换成集合后，遍历显示
                    if (data.data.entity[attr].indexOf(",") > -1) {
                        var pic = data.data.entity[attr].split(",");
                        var picLink = data.data.entity["pictureLink"].split(",");
                        if (pic.length > 0 && picLink.length > 0) {
                            var html = "";
                            for (var i = 0; i < pic.length; i++) {
                                var picPath = picLink[i];
                                var picName = pic[i];
                                html += "<img alt='" + picName + "' src='"+picPath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'>";
                                // html += "<img src=" + picPath + " alt=" + picName + " width=100 height=100>";
                            }
                            $("#picInfo1").append(html);
                        }
                    }
                }

                // if (attr == "attachment") {
                //     $("#fileInfo1").empty();
                //     //后台返回去的数据转换成集合后，遍历显示
                //     var attachment = data.data.entity[attr].split(",");
                //     var attachmentLink = data.data.entity["attachmentLink"].split(",");
                //     if (attachment.length > 0 && attachmentLink.length > 0) {
                //         var html = "";
                //         for (var i = 0; i < attachment.length; i++) {
                //             var filePath = attachmentLink[i];
                //             var fileName = attachment[i];
                //             html += "<a href=" + filePath + " target=_blank >" + fileName + "</a><br/>";
                //         }
                //         $("#fileInfo1").append(html);
                //     }
                // }
                if (attr == "attachment") {
                    $("#fileInfo1").empty();
                    $("#fileInfo1").show();
                    //后台返回去的数据转换成集合后，遍历显示
                    if(data.data.entity[attr] === "") continue;
                    var attachment = data.data.entity[attr].split(",");
                    var attachmentLink = data.data.entity["attachmentLink"].split(",");
                    var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                    var strFilter = ".jpeg|.gif|.jpg|.png|.bmp|.pic|";
                    if (attachment.length > 0 && attachmentLink.length > 0) {
                        for (var i = 0; i < attachment.length; i++) {
                            var html = "";
                            var filePath = attachmentLink[i];
                            var fileName = attachment[i];
                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                            html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                            html += "<a href=" + filePath + " target=_blank >下载</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                            if (fileName.indexOf(".") > -1) {
                                var str = fileExt + '|';
                                if (strFilter.indexOf(str) > -1) {//是图片
                                    html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                } else {
                                    if(fileExtArray.contains(fileExt)){
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                    }
                                }
                            } else {
                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                            }
                            $("#fileInfo1").append(html);
                        }
                    }
                }
            }
            $("#title1").val(data.data.entity.title);
            $("#titl").val(data.data.entity.empName + "的请假申请");
            $("#administrativeId").val(data.data.entity.id);
            var dStr = "";
            switch (data.data.entity.leaveType) {
                case 1:
                    dStr = '调休';
                    break;
                case 2:
                    dStr = '年假';
                    break;
                case 3:
                    dStr = '事假';
                    break;
                case 4:
                    dStr = '病假';
                    break;
                case 5:
                    dStr = '工伤假';
                    break;
                case 6:
                    dStr = '产假';
                    break;
                case 7:
                    dStr = '陪产假';
                    break;
                case 8:
                    dStr = '婚假';
                    break;
                case 9:
                    dStr = '丧假';
                    break;
                case 10:
                    dStr = '哺乳假';
                    break;
            }
            $("#leaveType1").val(dStr);
            $("#createName").val(data.data.entity.empName);
            //$("#leaveDays1").val(data.data.entity.leaveDays == null ? 0 : data.data.entity.leaveDays + "天");
            $("#leaveDays1").val(data.data.entity.leaveTime == null ? 0 : data.data.entity.leaveTime + "小时");
            $("#beginTime1").val(data.data.entity.beginTime);
            $("#endTime1").val(data.data.entity.endTime);
            $("#reason1").val(data.data.entity.reason);
            $("#taskId1").val(data.data.entity.taskId);
            //getFlowElement(data.data.entity.taskId);
            //当请假天数大于等于3天（24小时）时，行政总监审批，需要设置网关，审批通过并结束按钮
            if(hasRoleXZZJ() && data.data.entity.leaveTime > 16){
                $("#pass2").css("display", "inline-block");
                $("#pass1").attr("data-gateway", 1); //有网关
                $("#reject1").attr("data-gateway", 1); //有网关
            }else{
                $("#pass1").removeAttr("data-gateway"); //无网关
                $("#pass2").css("display", "none");
            }

            new RollbackCompont({
                target: "#rollback1",
                modal: "#viewModal",
                taskId: data.data.entity.taskId,
                completeCallback: function () {
                }
            }).render();
        }
    });
};


//加班查看页面，flag=1审批页面，flag=-1查看页面
function timeWorkView(administrativeId, flag) {
    if (flag == 1) {
        $("#timeWorkAuditTrue").show();
        $("#timeWorkAuditFalse").hide();
    }else  if(flag==-1){
        $("#timeWorkAuditTrue").hide();
        $("#timeWorkAuditFalse").show();
        var returnType= getQueryString("returnType");
        if(returnType==5){
            $("#timeWorkCancel").hide();
            $("#timeWorkGoback").show();
        }else{
            $("#timeWorkCancel").show();
            $("#timeWorkGoback").hide();
        }
    }
    else {
        $("#timeWorkAuditTrue").hide();
        $("#timeWorkAuditFalse").show();
        //查看页面返回按钮隐藏
        $("#timeWorkCancel").show();
        $("#timeWorkGoback").hide();
    }
    $("#timeWorkViewModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/administrative/getAdministrativeDetail",
        data: {
            administrativeType: 2,
            administrativeId: administrativeId
        },
        dataType: "json",
        success: function (data) {
            if (data == null) {
                alert("没有查到相关数据");
            }
            for (var attr in data.data.entity) {
                if (attr == "picture") {
                    $("#timeWorkPicInfo").empty();
                    //后台返回去的数据转换成集合后，遍历显示
                    if (data.data.entity[attr].indexOf(",") > -1) {
                        var pic = data.data.entity[attr].split(",");
                        var picLink = data.data.entity["pictureLink"].split(",");
                        if (pic.length > 0 && picLink.length > 0) {
                            var html = "";
                            for (var i = 0; i < pic.length; i++) {
                                var picPath = picLink[i];
                                var picName = pic[i];
                                html += "<img alt='" + picName + "' src='"+picPath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'>";
                                // html += "<img src=" + picPath + " alt=" + picName + " width=100 height=100>";
                            }
                            $("#timeWorkPicInfo").append(html);
                        }
                    }
                }

                // if (attr == "attachment") {
                //     $("#timeWorkFileInfo").empty();
                //     //后台返回去的数据转换成集合后，遍历显示
                //     if (data.data.entity[attr].indexOf(",") > -1) {
                //         var attachment = data.data.entity[attr].split(",");
                //         var attachmentLink = data.data.entity["attachmentLink"].split(",");
                //         if (attachment.length > 0 && attachmentLink.length > 0) {
                //             var html = "";
                //             for (var i = 0; i < attachment.length; i++) {
                //                 var filePath = attachmentLink[i];
                //                 var fileName = attachment[i];
                //                 html += "<a href=" + filePath + " target=_blank >" + fileName + "</a><br/>";
                //             }
                //             $("#timeWorkFileInfo").append(html);
                //         }
                //     }
                // }
                if (attr == "attachment") {
                    $("#timeWorkFileInfo").empty();
                    $("#timeWorkFileInfo").show();
                    //后台返回去的数据转换成集合后，遍历显示
                    if(data.data.entity[attr] === "") continue;
                    var attachment = data.data.entity[attr].split(",");
                    var attachmentLink = data.data.entity["attachmentLink"].split(",");
                    if (attachment.length > 0 && attachmentLink.length > 0) {
                        var html = "";
                        for (var i = 0; i < attachment.length; i++) {
                            var filePath = attachmentLink[i];
                            var fileName = attachment[i];
                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                            html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                            html += "<a href=" + filePath + " target=_blank >下载</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                            var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                            var strFilter = ".jpeg|.gif|.jpg|.png|.bmp|.pic|";
                            if (fileName.indexOf(".") > -1) {
                                var str = fileExt + '|';
                                if (strFilter.indexOf(str) > -1) {//是图片
                                    html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                } else {
                                    if(fileExtArray.contains(fileExt)){
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                    }
                                }
                            } else {
                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                            }
                        }
                        $("#timeWorkFileInfo").append(html);
                    }
                }

            }
            $("#timeWorkTitle").val(data.data.entity.title);
            $("#workAdministrativeId").val(data.data.entity.id);
            $("#timeWorkCreateName").val(data.data.entity.empName);
            $("#timeWorkDays").val(data.data.entity.workTime == null ? 0 : data.data.entity.workTime + "小时");
            $("#timeWorkBeginTime1").val(data.data.entity.beginTime);
            $("#timeWorkEndTime").val(data.data.entity.endTime);
            $("#timeWorkReason").val(data.data.entity.reason);
            $("#timeWorkTaskId1").val(data.data.entity.taskId);

            new RollbackCompont({
                target: "#rollbackTimeWork",
                modal: "#timeWorkViewModal",
                taskId: data.data.entity.taskId,
                completeCallback: function () {
                }
            }).render();
        }
    });
};

//外出查看页面，flag=1审批页面，flag=-1查看页面
function outWorkView(administrativeId, flag) {
    if (flag == 1) {
        $("#outWorkAuditTrue").show();
        $("#outWorkAuditFalse").hide();
    } else if (flag == -1){
        $("#outWorkAuditTrue").hide();
        $("#outWorkAuditFalse").show();
        var returnType= getQueryString("returnType");
        if(returnType==5){
            $("#outWorkCancel1").hide();
            $("#outWorkGoback").show();
        }else{
            $("#outWorkCancel1").show();
            $("#outWorkGoback").hide();
        }
    }
    else {
        $("#outWorkAuditTrue").hide();
        $("#outWorkAuditFalse").show();
        //查看页面返回按钮隐藏
        $("#outWorkCancel1").show();
        $("#outWorkGoback").hide();
    }
    $("#outWorkViewModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/administrative/getAdministrativeDetail",
        data: {
            administrativeType: 3,
            administrativeId: administrativeId
        },
        dataType: "json",
        success: function (data) {
            if (data == null) {
                alert("没有查到相关数据");
            }
            for (var attr in data.data.entity) {
                if (attr == "picture") {
                    $("#outWorkPicInfo1").empty();
                    //后台返回去的数据转换成集合后，遍历显示
                    if (data.data.entity[attr].indexOf(",") > -1) {
                        if(data.data.entity[attr] === "") continue;
                        var pic = data.data.entity[attr].split(",");
                        var picLink = data.data.entity["pictureLink"].split(",");
                        if (pic.length > 0 && picLink.length > 0) {
                            var html = "";
                            for (var i = 0; i < pic.length; i++) {
                                var picPath = picLink[i];
                                var picName = pic[i];
                                html += "<img alt='" + picName + "' src='"+picPath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'>";
                                // html += "<img src=" + picPath + " alt=" + picName + " width=100 height=100>";
                            }
                            $("#outWorkPicInfo1").append(html);
                        }
                    }

                }

                // if (attr == "attachment") {
                //     $("#outWorkFileInfo1").empty();
                //     //后台返回去的数据转换成集合后，遍历显示
                //     var attachment = data.data.entity[attr].split(",");
                //     var attachmentLink = data.data.entity["attachmentLink"].split(",");
                //     if (attachment.length > 0 && attachmentLink.length > 0) {
                //         var html = "";
                //         for (var i = 0; i < attachment.length; i++) {
                //             var filePath = attachmentLink[i];
                //             var fileName = attachment[i];
                //             html += "<a href=" + filePath + " target=_blank >" + fileName + "</a><br/>";
                //         }
                //         $("#outWorkFileInfo1").append(html);
                //     }
                // }
                if (attr == "attachment") {
                    $("#outWorkFileInfo1").empty();
                    $("#outWorkFileInfo1").show();
                    //后台返回去的数据转换成集合后，遍历显示
                    if(data.data.entity[attr] === "") continue;
                    var attachment = data.data.entity[attr].split(",");
                    var attachmentLink = data.data.entity["attachmentLink"].split(",");
                    if (attachment.length > 0 && attachmentLink.length > 0) {
                        var html = "";
                        for (var i = 0; i < attachment.length; i++) {
                            var filePath = attachmentLink[i];
                            var fileName = attachment[i];
                            var strFilter = ".jpeg|.gif|.jpg|.png|.bmp|.pic|";
                            var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                            html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                            html += "<a href=" + filePath + " target=_blank >下载</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                            if (fileName.indexOf(".") > -1) {
                                var str = fileExt + '|';
                                if (strFilter.indexOf(str) > -1) {//是图片
                                    html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                } else {
                                    if(fileExtArray.contains(fileExt)){
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                    }
                                }
                            } else {
                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                            }
                            // $.ajax({
                            //     url: baseUrl + "/filePreview?filepath=" + filePath,
                            //     success: function (result) {
                            //         if (result.code && result.code !== 200) {
                            //             swal({
                            //                 title: result.msg,
                            //                 type: "error"
                            //             });
                            //         } else {
                            //             html += "<a href=" + result.data.result + " target=_blank >预览:" + fileName + "</a><br/>";
                            //         }
                            //     },
                            //     type: "get",
                            //     async: false
                            // });
                        }
                        $("#outWorkFileInfo1").append(html);
                    }
                }
            }
            $("#outWorkTitle1").val(data.data.entity.title);
            $("#outWorkId1").val(data.data.entity.id);
            $("#outWorkCreateName").val(data.data.entity.empName);
            $("#outWorkLeaveDays").val(data.data.entity.days == null ? 0 : data.data.entity.days);
            $("#outWorkBeginTime1").val(data.data.entity.beginTime);
            $("#outWorkEndTime1").val(data.data.entity.endTime);
            $("#outWorkReason1").val(data.data.entity.reason);
            $("#outWorkTaskId1").val(data.data.entity.taskId);

            new RollbackCompont({
                target: "#rollbackOutWork",
                modal: "#outWorkViewModal",
                taskId: data.data.entity.taskId,
                completeCallback: function () {
                }
            }).render();
        }
    });
};

//出差查看页面，flag=1审批页面，flag=-1查看页面
function onBusinessView(administrativeId, flag,rtype) {
    $("#onBusinessEditForm input[name='placeType']").removeAttr("checked");
    $("#onBusinessEditForm input[name='placeType']").parent().removeClass("checked");
    $("#onBusinessEditForm input[name='reason']").removeAttr("checked");
    $("#onBusinessEditForm input[name='reason']").parent().removeClass("checked");
    $("#onBusinessEditForm input[name='traffic']").removeAttr("checked");
    $("#onBusinessEditForm input[name='traffic']").parent().removeClass("checked");
    $("#onBusinessEditForm input[name='separate']").removeAttr("checked");
    $("#onBusinessEditForm input[name='separate']").parent().removeClass("checked");

    if (rtype==1&&flag==-1){
        $("#updateReport1").hide();
        $("#confirm1").hide();
        $("#confirm").hide();
        $("#viewPlan").modal("hide");
        $("#addPlan").modal("hide");
        $("#businessConclusion").modal('toggle');
        $("#onBusinessPass1").hide();
        $("#onBusinessReject1").hide();
        $("#onBusinessPlan").hide();
    }else {
    if (flag == 1) {
        $("#onBusinessFileInfo1").empty();
        $("#onBusinessPicInfo1").empty();
        $("#attachment").hide();
        $("#onBusinessCancel").hide();
        $(".save").hide();
        $(".update").hide();
        $(".view").show();

        $("#onBusinessAuditTrue").show();
        $("#onBusinessAuditFalse").hide();
        $("#cs2").hide();
        $("#ccinput2").hide();
        $("#confirm").hide();
        $("#ccinput").hide();
    }else if(flag==-1){
        var taskState = $("#taskState").val();
        $("#onBusinessCancel1").hide();
        $("#cs").hide();
        $("#cc").hide();
        $("#confirm").hide();
        $("#confirm1").hide();
        $(".save").hide();
        $(".update").hide();
        $(".view").show();
        $("#onBusinessCancel").show();
        $("#onBusinessConclusion").show()
        $("#onBusinessAuditFalse").show();
        $("#attachment").hide();
        $("#onBusinessAuditTrue").hide();
        var returnType= getQueryString("returnType");
        if(returnType==5){
            $("#onBusinessCancel1").hide();
            $("#onBusinessGoback").show();
        }else{
            $("#onBusinessCancel1").show();
            $("#onBusinessGoback").hide();
        }
//抄送待办flag=6
    }else if(flag==6){
        $("#onBusinessCancel1").hide();
        $(".save").hide();
        $(".update").hide();
        $("#onBusinessCancel").show();
        $("#onBusinessConclusion").show()
        $("#onBusinessAuditFalse").hide();
        $("#attachment").hide();
        $("#onBusinessAuditTrue").hide();

    }
    else if(flag==7){
        $("#onBusinessCancel1").hide();
        $(".save").hide();
        $(".update").hide();
        $("#cs").hide();
        $("#confirm").hide();
        $("#ccinput").hide();
        $("#onBusinessCancel").show();
        $("#onBusinessConclusion").show()
        $("#onBusinessAuditFalse").hide();
        $("#attachment").hide();
        $("#onBusinessAuditTrue").hide();

    }
    else if(flag==2){
        $("#onBusinessCancel1").hide();
        $(".save").hide();
        $(".update").hide();
        $("#cs").hide();
        $("#confirm").hide();
        $("#confirm1").hide();
        $("#ccinput").hide();
        $("#onBusinessCancel").show();
        $("#onBusinessConclusion").show()
        $("#onBusinessAuditFalse").hide();
        $("#attachment").hide();
        $("#onBusinessAuditTrue").hide();

    }
    else {
        $("#onBusinessAuditTrue").hide();
        $("#onBusinessAuditFalse").show();

        //查看页面返回按钮隐藏
        $("#onBusinessCancel1").show();
        $("#onBusinessGoback").hide();
    }
    }
    selectUser();
    $.ajax({
        type: "post",
        url: baseUrl + "/administrative/getAdministrativeDetail",
        data: {
            administrativeType: 4,
            administrativeId: administrativeId
        },
        dataType: "json",
        success: function (data) {
            if (data == null) {
                alert("没有查到相关数据");
            }
            if (data.data.entity["empId"] == user.id && flag == 1) {
                $("#onBusinessReport").show();
                $("#report").show();
                $("#onBusinessAuditTrue").hide();
                $("#onBusinessAuditFalse").hide();
            }
            for (var attr in data.data.entity) {
                $("#viewPlan").find("input[type='hidden'][name='" + attr + "']").val(data.data.entity[attr]);
                $("#viewPlan [name=" + attr + "][type='text']").val(data.data.entity[attr]);
                $("#businessConclusion").find("input[type='hidden'][name='" + attr + "']").val(data.data.entity[attr]);
                $("#businessConclusion").find("input[type='text'][name='" + attr + "']").val(data.data.entity[attr]);
                $("#planId").val(data.data.entity["id"]);
                if (attr == "picture") {
                    $("#onBusinessPicInfo1").empty();
                    $("#onBusinessPicInfo1").show();
                    //后台返回去的数据转换成集合后，遍历显示
                    if (data.data.entity[attr]) {
                        if(data.data.entity[attr] === "") continue;
                        var pic = data.data.entity[attr].split(",");
                        var picLink = data.data.entity["pictureLink"].split(",");
                        if (pic.length > 0 && picLink.length > 0) {
                            var html = "";
                            for (var i = 0; i < pic.length; i++) {
                                var picPath = picLink[i];
                                var picName = pic[i];
                                html += "<span>" + picName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                                html += "<a href=" + picPath + " target=_blank  download="+picName+">下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                var fileExt = picName.substring(picName.lastIndexOf(".")).toLowerCase() ;
                                var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                                var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                                if(picName.indexOf(".")>-1){
                                    var str=fileExt + '|';
                                    if(strFilter.indexOf(str)>-1){//是图片
                                        html += "<img alt='" + picName + "' src='"+picPath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'/><br/>";
                                    }else{
                                        if(fileExtArray.contains(fileExt)){
                                            html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                        }
                                    }
                                }else {
                                    html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                }
                            }
                            $("#onBusinessPicInfo1").append(html);
                        }
                    }
                }
                if(attr=="attachment"){
                    $("#onBusinessFileInfo1").empty();
                    $("#onBusinessFileInfo1").show();
                    if(data.data.entity[attr] === "") continue;
                    var affixName = data.data.entity[attr].split(',');
                    var affixLink = data.data.entity["attachmentLink"].split(",");
                    if (affixName.length>0 && affixLink.length>0){
                        var html = "";
                        for (var i=0 ; i<affixName.length ; i++) {

                            var filePath = affixLink[i];
                            var fileName = affixName[i];
                            html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                            html += "<a href=" + filePath + " target=_blank  download="+fileName+">下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase() ;
                            var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                            var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                            if(fileName.indexOf(".")>-1){
                                var str=fileExt + '|';
                                if(strFilter.indexOf(str)>-1){//是图片
                                    html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'/><br/>";
                                }else{
                                    if(fileExtArray.contains(fileExt)){
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                    }
                                }
                            }else {
                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                            }
                        }
                        $("#onBusinessFileInfo1").append(html);

                    }
                }
                if (attr == "pic") {
                    $("#onBusinessPicInfo2").empty();
                    $("#onBusinessPicInfo2").show();
                    //后台返回去的数据转换成集合后，遍历显示
                    if (data.data.entity[attr]) {
                        if(data.data.entity[attr] === "") continue;
                        var pic = data.data.entity[attr].split(",");
                        var picLink = data.data.entity["picLink"].split(",");
                        if (pic.length > 0 && picLink.length > 0) {
                            var html = "";
                            for (var i = 0; i < pic.length; i++) {
                                var picPath = picLink[i];
                                var picName = pic[i];
                                html += "<span>" + picName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                                html += "<a href=" + picPath + " target=_blank  download="+picName+">下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                var fileExt = picName.substring(picName.lastIndexOf(".")).toLowerCase() ;
                                var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                                var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                                if(picName.indexOf(".")>-1){
                                    var str=fileExt + '|';
                                    if(strFilter.indexOf(str)>-1){//是图片
                                        html += "<img alt='" + picName + "' src='"+picPath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'/><br/>";
                                    }else{
                                        if(fileExtArray.contains(fileExt)){
                                            html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                        }
                                    }
                                }else {
                                    html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                }
                            }
                            $("#onBusinessPicInfo2").append(html);
                        }
                    }
                }
                if(attr=="attach"){
                    $("#onBusinessFileInfo2").empty();
                    $("#onBusinessFileInfo2").show();
                    if(data.data.entity[attr] === "") continue;
                    var affixName = data.data.entity[attr].split(',');
                    var affixLink = data.data.entity["attachLink"].split(",");

                    if (affixName.length>0 && affixLink.length>0){
                        var html = "";
                        for (var i=0 ; i<affixName.length ; i++) {

                            var filePath = affixLink[i];
                            var fileName = affixName[i];
                            html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                            html += "<a href=" + filePath + " target=_blank  download="+fileName+">下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase() ;
                            var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                            var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                            if(fileName.indexOf(".")>-1){
                                var str=fileExt + '|';
                                if(strFilter.indexOf(str)>-1){//是图片
                                    html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'/><br/>";
                                }else{
                                    if(fileExtArray.contains(fileExt)){
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                    }
                                }
                            }else {
                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                            }
                        }
                        $("#onBusinessFileInfo2").append(html);

                    }
                }
                //出差
                if (attr=="separate"){
                    changeLicence(data.data.entity[attr]);
                }
                if (attr=="note"){
                    $("#note").val(data.data.entity[attr]);
                }
                if (attr=="target"){
                    $("#target").val(data.data.entity[attr]);
                }
                if (attr=="trip"){
                    $("#trip").val(data.data.entity[attr]);
                }
                if (attr=="costBudget"){
                    $("#costBudget").val(data.data.entity[attr]);
                }
                if (attr=="travelStateTime"){
                    $("#travelStateTime").val(data.data.entity[attr]);
                    $("#travelStateTime1").val(data.data.entity[attr]);
                }
                if (attr=="travelEndTime"){
                    $("#travelEndTime").val(data.data.entity[attr]);
                    $("#travelEndTime1").val(data.data.entity[attr]);
                }
                if (attr=="conclusion"){
                    $("#conclusion").val(data.data.entity[attr]);
                }
                if (attr == "placeType") {
                    $("input[name='placeType'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $("input[name='placeType'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
                if (attr == "reason") {
                    $(" input[name='reason'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $(" input[name='reason'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
                if (attr == "traffic") {
                    $(" input[name='traffic'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $(" input[name='traffic'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
                if (attr == "separate") {
                    $(" input[name='separate'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $(" input[name='separate'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
                if(attr=="taskState" && rtype !=1){
                    if (data.data.entity[attr] == 8 && flag==1 ){
                        $("#cs2").show();
                        $("#ccinput2").show();
                    }
                    //流程状态=21，到出差人提交出差总结节点(驳回指定流程，消息存在李总提交出差报告情况)
                    var applyId = data.data.entity["applyId"];
                    if (data.data.entity[attr] == 21 && applyId==user.id && flag== 1 ){
                        $("#businessConclusion").modal('toggle');
                        $("#onBusinessPass1").hide();
                        $("#confirm1").hide();
                        $("#cs2").hide();
                        $("#ccinput2").hide();
                        $("#cs1").hide();
                        $("#ccinput1").hide();
                        $("#onBusinessReject1").hide();
                        // $("#businessConclusion input[name='type']").removeAttr("checked");
                        // $("#businessConclusion input[name='type']").parent().removeClass("checked");
                        //流程状态=22，到总经理复审，显示总结框
                    }else if( data.data.entity[attr] == 22  && flag==1){
                        $("#conclusionFile").hide();
                        $("#businessConclusion").modal('toggle');
                        $("#confirm1").hide();
                        $("#updateReport1").hide();
                        $("#reimbursement").hide();
                        $("#businessConclusion input[name='type']").removeAttr("checked");
                        $("#businessConclusion input[name='type']").parent().removeClass("checked");
                    }
                    else {
                        if (flag== 7){
                            $("#businessConclusion").modal({backdrop: "static"});
                            $("#onBusinessReject1").hide();
                            $("#onBusinessPass1").hide();
                        }else {
                            $("#viewPlan").modal({backdrop: "static"});
                        }

                        //当流程走完了，双击查看流程
                        if(data.data.entity[attr]== null && data.data.entity["state"]==1){
                            $("#updateReport1").hide();
                            $("#onBusinessConclusion").show();
                            $("#back").hide();
                        }else {
                            $("#onBusinessConclusion").hide();
                        }
                        $("#viewPlan input[name='type']").removeAttr("checked");
                        $("#viewPlan input[name='type']").parent().removeClass("checked");
                    }

                }
                $("#onBusinessTaskId1").val(data.data.entity.taskId);
            }
            new RollbackCompont({
                target: "#rollbackOnBusiness",
                modal: "#viewPlan",
                taskId: data.data.entity.taskId,
                completeCallback: function () {
                }
            }).render();
        }
    });
};


//获取驳回的审批节点
function getFlowElement(dataId) {
    var processType = 8;
    dataId = $.getUrlParam("id");
    $.post(baseUrl + "/administrative/getFlowElement", {dataId: dataId, process: processType}, function (data) {
        if (data.data.data == null) {
            alertMessage(data.msg);
        } else {
            var data = data.data.data;
            $("#task").empty();
            //赋值
            if (data != null) {
                var html = "<option value=''>选择要驳回的节点</option>";
                $(data).each(function (i, item) {
                    html += "<option>" + item.name + "-" + item.user + "</option>";
                });
                $("#task").html(html);
                // $("#task").val(data.data.vacaTime.vacationTime);
            }
        }
    }, "json");

    // $.post(baseUrl + "/administrative/getFlowElement", {dataId: dataId, process: processType}, function (data) {
    //     if (data.data.data == null) {
    //         alertMessage(data.msg);
    //     } else {
    //         $("#task").empty();
    //         //赋值
    //         if (data != null) {
    //             var data = data.data.data;
    //             $("#task").empty();
    //             //赋值
    //             if (data != null) {
    //                 var html = "<option value=''>选择要驳回的节点</option>";
    //                 $(data).each(function (i, item) {
    //                     html += "<option>" + item.name + "-" + item.user + "</option>";
    //                 });
    //                 $("#task").html(html);
    //                 // $("#task").val(data.data.vacaTime.vacationTime);
    //             }
    //         }
    //     }
    // }, "json");
}

// 保存审批意见；
function saveApprove(flag) {
    alertMessage("系统处理中，请稍候。");
    if ($("#approveForm").valid()) {
        var params = $("#approveForm").serializeJson();
        params["operate"] = flag;
        $.post(baseUrl + "/entryManage/approveEntry", params, function (data) {
            var message = data.data.message;
            if (message == null) {
                alertMessage(data.msg);
            } else {
                alertMessage(message);
            }
            $("#approveModal").modal("hide");
            reloadTaskData(2);
        }, "json");
    } else {
        alertMessage("请勿输入特殊字符。");
    }
}

//申请人撤回
function returnBack(id,taskId, itemId,administrativeType,administrativeTime) {
    layer.confirm('确认撤回？', {
        btn: ['撤回', '取消'], //按钮
        shade: false //不显示遮罩

    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: baseUrl + "/process/administrativeWithdrawal",    //向后端请求数据的url
            data: {adminId:id,taskId: taskId, itemId: itemId,administrativeType:administrativeType,administrativeTime:administrativeTime},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    swal(data.data.message);
                    $("#entryTable").reloadCurrentData(baseUrl + "/administrative/getAdministrative", $("#queryForm").serializeJson(), "json", null, null);
                }else if(data.code==1002){
                    swal({
                        title: "异常提示",
                        text: data.msg,
                    });
                }
                else {
                    if (getResCode(data))
                        return;
                }
            }
        });
    }, function () {
        return;
    });
}

// 审批记录查看；
function showHistory(dataId, administrativeTime, type) {

    $("#historyModal").modal({backdrop: "static"});
    var processType;
    if (type == 1) {
        if (administrativeTime <= 8) {
            processType = 7;
        } else if (administrativeTime > 8 && administrativeTime <= 16) {
            processType = 8;
        } else if (administrativeTime > 16) {
            processType = 9;
        }
    } else if (type == 2) {
        processType = 18;
    } else if (type == 3) {
        processType = 17;
    } else if (type == 4) {
        processType = 20;
    }
    $.post(baseUrl + "/process/history", {dataId: dataId, process: processType}, function (data) {
        $("#history").empty();
        if (data.data.data == null) {
            alertMessage(data.msg);
        } else {
            var html = "<div style='position: relative;z-index: 10;'>";
            html += "<div class='form-control'>";
            html += "<div class='col-sm-3 text-center'>审核节点</div>";
            html += "<div class='col-sm-3 text-center'>操作人</div>";
            html += "<div class='col-sm-3 text-center'>操作详情</div>";
            html += "<div class='col-sm-3 text-center'>操作时间</div></div>";
            for (var i = 0; i < data.data.data.length; i++) {
                html += "<div class='form-control' style='margin-top: -1px;'>";
                html += "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>";
                html += "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>";
                html += "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>";
                html += "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>";
                html += "</div>";
            }
            html += "</div><div class='col-sm-12 text-center' style='position:relative'>";
            html += "<img src='/process/getImage?dataId=" + dataId + "&process=" + processType + "&t=" + new Date().getTime() + "' style='width: 120%; margin-left: -100px; margin-top: -100px; margin-bottom: -100px;'/></div>";
            $("#history").append(html);
        }
    }, "json");
}

//保存申请信息
function submitHander(t, url, state, type) {
    if(type == 7&& $("#editorPlanFrom [name='travelUser']").val() == ""){
        swal("请选择工作责任人!");
        return;
    }
    if ($(t.form).valid()) {
        //如果是请假类型，需要校验是否符合规则
        if(type == 1 && !validationAddLeave($("#endTime").val(), 1,$("#administrativeId").val())){
            return;
        }

        var tips;
        if (state == 0) {
            $("#state").val(state);
            tips = "确认保存？";
        } else {
            $("#state").val(state);
            tips = "请确认申请信息？提交后不能取消";
        }
        layer.confirm(tips, {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            var formData;
            if (type == 1) {
                formData = new FormData($("#editForm")[0]);
                if("show" == $("#zwDiv").attr("hideFlag")){
                    if(!$("#zwExamine").find("option:selected").val()){
                        layer.msg("请选择政委审核人员！", {time: 2000, icon: 5});
                        return;
                    }
                    formData.append("nextUser",$("#zwExamine").find("option:selected").val());
                    formData.append("nextUserName",$("#zwExamine").find("option:selected").text());
                    formData.append("nextUserDept",$("#zwExamine").find("option:selected").attr("data-deptId"));
                }
            } else if (type == 2) {
                formData = new FormData($("#workEditForm")[0]);
            } else if (type == 3) {
                formData = new FormData($("#outWorkEditForm")[0]);
            } else if (type == 4) {
                formData = new FormData($("#onBusinessEditForm")[0]);
            } else if (type == 5) {
                formData = new FormData($("#businessConclusionFrom")[0]);
            }else if (type == 6) {

                var itemId =0;
                itemId ==$("#itemConclusionId").val();
                formData = new FormData($("#businessConclusionFrom")[0]);
            }else if (type == 7) {
                formData = new FormData($("#editorPlanFrom")[0]);
            }
            startModal("#" + t.id);//锁定按钮，防止重复提交
            $.ajax({
                type: "post",
                url: url,
                data: formData,
                dataType: "json",
                async: true,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#entryTable").jqGrid('setGridParam', {
                            postData: $("#queryForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        if (type == 1) {
                            $("#editModal").modal('hide');
                        } else if (type == 2) {
                            $("#workEditModal").modal('hide');
                        } else if (type == 3) {
                            $("#outWorkEditModal").modal('hide');
                        } else if (type == 4) {
                            $("#viewPlan").modal('hide');
                        } else if (type == 5) {
                            // var test = $("#onBusinessTaskId2").val();
                            $("#businessConclusion").modal('hide');
                            approveTask(data.data.entity.taskId, 1, t.id);
                        } else if (type == 6) {
                        $("#businessConclusion").modal('hide');
                        } else if (type == 7) {
                            $("#addPlan").modal('hide');
                        }
                    } else {
                        if (getResCode(data))
                            return;
                        if (type == 1) {
                            $("#editModal").modal('hide');
                        } else if (type == 2) {
                            $("#workEditModal").modal('hide');
                        } else if (type == 3) {
                            $("#outWorkEditModal").modal('hide');
                        } else if (type == 4) {
                            $("#viewPlan").modal('hide');
                        } else if (type == 5) {
                            $("#businessConclusion").modal('hide');
                        }
                    }
                },
                error: function (data) {

                    Ladda.stopAll();
                    if(data.code == 200){
                        swal(data.data.message);
                        $("#entryTable").reloadCurrentData(baseUrl + "/administrative/getAdministrative", $("#queryForm").serializeJson(), "json", null, null);
                    }else if(data.code==1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    }
                    if (getResCode(data))
                        return;
                }
            });
        }, function () {
            return;
        });
    }
}

//跳转到报销申请页面
function jumpSubmitHander(){
    var admId = $("#administrativeId1").val();
    var taskState = $("#taskState").val();
    $.ajax({
        type:"post",
        url:"/userBusinessPlan/getReimbursement",
        dataType:"json",
        data:{admId: admId},
        success:function (data) {
            if (data == 0 && taskState !=21){
                swal("友情提示!", "该出差流程未填写报销", "warning");
                return;
            }else{
                var addScheme = "/fee/expenseReimbursement?admId="+admId;
                page(addScheme, "报销流程");
            }

        }
    })


}

//编辑信息
function edit(id, type) {
    var URL;
    if (type == 1) {
        //请假
        URL = baseUrl + "/administrativeLeave/getLeaByAdmId";
        zwUserList("#zwExamine");//渲染政委列表
        $("#editModal").modal('toggle');
        $("#editForm input[name='type']").removeAttr("checked");
        $("#editForm input[name='type']").parent().removeClass("checked");
    } else if (type == 2) {
        //加班
        URL = baseUrl + "/workTime/getTimeworkByAdmId";
        $("#workEditModal").modal('toggle');
        $("#workEditForm input[name='type']").removeAttr("checked");
        $("#workEditForm input[name='type']").parent().removeClass("checked");

    } else if (type == 3) {
        //外出
        URL = baseUrl + "/outWork/getOutWorkByAdmId";
        $("#outWorkEditModal").modal('toggle');
        $("#outWorkEditForm input[name='type']").removeAttr("checked");
        $("#outWorkEditForm input[name='type']").parent().removeClass("checked");
    } else if (type == 4) {
        URL = baseUrl + "/userBusinessPlan/getBussiness";
        $("#addPlan").modal('toggle');
        $("#editorPlanFrom input[name='type']").removeAttr("checked");
        $("#editorPlanFrom input[name='type']").parent().removeClass("checked");
        $("#addPlan input[name='separate']").removeAttr("checked");
        $("#addPlan input[name='separate']").parent().removeClass("checked");
        $("#onBusinessFile").val('');
        $("#onBusinessPic").val('');
        $("#loanQcDiv").show();
        $(".save").hide();
        $(".view").hide();
        $("#onBusinessConclusion").hide();
        $("#confirm").hide();
        $("#cs").hide();
        $("#ccinput").hide();
        $("#attachment").show();
        $("#onBusinessCancel").show();
        $("#onBusinessAuditFalse").hide();
        // $('#onBusinessEditForm input:radio[name="isExpense"]').off('ifChecked').on('ifChecked', function (event) {
        //     if ($('#onBusinessEditForm input:radio[name="isExpense"]:checked').val() == 1) {
        //         //如果是是，则跳转到借款页面
        //         var admId = $("#onBusinessAdministrativeId").val();
        //         var addScheme = "/fee/queryBorrow?admId="+admId;
        //         page(addScheme, "新增借款流程");
        //     } else {
        //
        //     }
        // });



    }
    $.ajax({
        type: "post",
        url: URL,
        data: {id: id},
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.entity) {
                if (type == 1) {
                    //请假
                    $("#editForm [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                } else if (type == 2) {
                    //加班
                    $("#workEditForm [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);

                } else if (type == 3) {
                    //外出
                    $("#outWorkEditForm [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                } else if (type == 4) {
                    $("#editorPlanFrom [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                    //出差
                    if (attr=="separate"){
                        changeLicence(data.data.entity[attr]);
                    }
                    if(attr=="travelUser"){
                        $("#editorPlanFrom select[name='travelUser']").val(data.data.entity[attr]);
                        $("#travelUser2").val(data.data.entity[attr]);
                    }
                    if (attr === "empId") {
                        var oldempDept = data.data.entity["empDept"];
                        getEmpByDeptId(oldempDept);
                        $("#editorPlanFrom select[name='empId']").val(data.data.entity[attr]);
                        $("#empId").val(data.data.entity[attr]);
                    } else if (attr === "agentId") {
                        var oldempDept = data.data.entity["agentDept"];
                        getAgentByDeptId(oldempDept);
                        $("#editorPlanFrom select[name='agentId']").val(data.data.entity[attr]);
                        $("#agentId").val(data.data.entity[attr]);
                    } else if (attr === "escortId") {
                        var oldempDept = data.data.entity["escortDept"];
                        getEscortByDeptId(oldempDept);
                        $("#editorPlanFrom select[name='escortId']").val(data.data.entity[attr]);
                        $("#escortId").val(data.data.entity[attr]);
                    } else {
                        $("#editorPlanFrom [name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                    }
                }
                if (attr == "placeType") {
                    $("#editorPlanFrom input[name='placeType'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $("#editorPlanFrom input[name='placeType'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
                if (attr == "reason") {
                    $("#editorPlanFrom input[name='reason'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $("#editorPlanFrom input[name='reason'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
                if (attr == "traffic") {
                    $("#editorPlanFrom input[name='traffic'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $("#editorPlanFrom input[name='traffic'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
                if (attr == "separate") {
                    $("#editorPlanFrom input[name='separate'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                    $("#editorPlanFrom input[name='separate'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                }
                if(attr=="picture"){
                    if (type == 1) {
                        $("#picInfo").empty();
                    } else if (type == 2) {
                        $("#picInfo2").empty();
                    } else if (type == 3) {
                        $("#picInfo3").empty();
                    } else if (type == 4) {
                        $("#picInfo7").empty();
                    }
                    if(data.data.entity[attr] === "") continue;
                    var affixName = data.data.entity[attr].split(',');
                    var affixLink = data.data.entity["pictureLink"].split(",");

                    if (affixName.length>0 && affixLink.length>0){
                        var html = "";
                        for (var i=0 ; i<affixName.length ; i++) {

                            var filePath = affixLink[i];
                            var fileName = affixName[i];
                            html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                            html += "<a href=" + filePath + " target=_blank  download="+fileName+">下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase() ;
                            var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                            var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                            if(fileName.indexOf(".")>-1){
                                var str=fileExt + '|';
                                if(strFilter.indexOf(str)>-1){//是图片
                                    html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'/><br/>";
                                }else{
                                    if(fileExtArray.contains(fileExt)){
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                    }
                                }
                            }else {
                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                            }
                        }
                        if (type == 1) {
                            $("#picInfo").append(html);
                        } else if (type == 2) {
                            $("#picInfo2").append(html);
                        } else if (type == 3) {
                            $("#picInfo3").append(html);
                        } else if (type == 4) {
                            $("#picInfo7").append(html);
                        }

                    }
                }
                // if (attr == "picture") {
                //     if (type == 1) {
                //         $("#picInfo").empty();
                //     } else if (type == 2) {
                //         $("#picInfo2").empty();
                //     } else if (type == 3) {
                //         $("#picInfo3").empty();
                //     } else if (type == 4) {
                //         $("#picInfo4").empty();
                //     }
                //     //后台返回去的数据转换成集合后，遍历显示
                //     if (data.data.entity[attr].indexOf(",") > -1) {
                //         var pic = data.data.entity[attr].split(",");
                //         var picLink = data.data.entity["pictureLink"].split(",");
                //         if (pic.length > 0 && picLink.length > 0) {
                //             var html = "";
                //             for (var i = 0; i < pic.length; i++) {
                //                 var picPath = picLink[i];
                //                 var picName = pic[i];
                //                 html += "<img src=" + picPath + " alt=" + picName + " width=100 height=100>";
                //             }
                //             if (type == 1) {
                //                 $("#picInfo").append(html);
                //             } else if (type == 2) {
                //                 $("#picInfo2").append(html);
                //             } else if (type == 3) {
                //                 $("#picInfo3").append(html);
                //             } else if (type == 4) {
                //                 $("#picInfo4").append(html);
                //             }
                //         }
                //     }
                // }
                if(attr=="attachment"){
                    if (type == 1) {
                        $("#fileInfo").empty();
                    } else if (type == 2) {
                        $("#fileInfo2").empty();
                    } else if (type == 3) {
                        $("#fileInfo3").empty();
                    } else if (type == 4) {
                        $("#fileInfo7").empty();
                    }
                    if(data.data.entity[attr] === "") continue;
                    var affixName = data.data.entity[attr].split(',');
                    var affixLink = data.data.entity["attachmentLink"].split(",");

                    if (affixName.length>0 && affixLink.length>0){
                        var html = "";
                        for (var i=0 ; i<affixName.length ; i++) {

                            var filePath = affixLink[i];
                            var fileName = affixName[i];
                            html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                            html += "<a href=" + filePath + " target=_blank  download="+fileName+">下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase() ;
                            var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                            var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                            if(fileName.indexOf(".")>-1){
                                var str=fileExt + '|';
                                if(strFilter.indexOf(str)>-1){//是图片
                                    html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                }else{
                                    if(fileExtArray.contains(fileExt)){
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                    }
                                }
                            }else {
                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                            }
                        }
                        if (type == 1) {
                            $("#fileInfo").append(html);
                        } else if (type == 2) {
                            $("#fileInfo2").append(html);
                        } else if (type == 3) {
                            $("#fileInfo3").append(html);
                        } else if (type == 4) {
                            $("#fileInfo7").append(html);
                        }

                    }
                }
                // if (attr == "attachment") {
                //     if (type == 1) {
                //         $("#fileInfo").empty();
                //     } else if (type == 2) {
                //         $("#fileInfo2").empty();
                //     } else if (type == 3) {
                //         $("#fileInfo3").empty();
                //     } else if (type == 4) {
                //         $("#fileInfo4").empty();
                //     }
                //     //后台返回去的数据转换成集合后，遍历显示
                //     var attachment = data.data.entity[attr].split(",");
                //     var attachmentLink = data.data.entity["attachmentLink"].split(",");
                //     if (attachment.length > 0 && attachmentLink.length > 0) {
                //         var html = "";
                //         for (var i = 0; i < attachment.length; i++) {
                //             var filePath = attachmentLink[i];
                //             var fileName = attachment[i];
                //             html += "<a href=" + filePath + " target=_blank >" + fileName + "</a><br/>";
                //         }
                //         if (type == 1) {
                //             $("#fileInfo").append(html);
                //         } else if (type == 2) {
                //             $("#fileInfo2").append(html);
                //         } else if (type == 3) {
                //             $("#fileInfo3").append(html);
                //         } else if (type == 4) {
                //             $("#fileInfo4").append(html);
                //         }
                //     }
                // }
            }
            if (type == 1) {
                initializeLeaveType();
            }
            if (type == 4) {
                //如果是出差，下拉框初始化
                layui.use(["form", 'element'], function () {
                    layui.form.render();//layui-form
                });
            }
        }
    });
    $(".save").hide();
    $(".update").show();
    if (type == 2) {
        //如果是加班，则获取剩余调休时间
        getVacationTime1();
    }

}
//删除请假信息
function del(id, type) {
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        var URL;
        if (type == 1) {
            //请假
            URL = baseUrl + "/administrativeLeave/deleteLeaByAdmId";
        } else if (type == 2) {
            //加班
            URL = baseUrl + "/workTime/deleteTimeworkByAdmId";
        } else if (type == 3) {
            //外出
            URL = baseUrl + "/outWork/deleteOutWorkByAdmId";
        } else if (type == 4) {
            //出差
            URL = baseUrl + "/userBusinessPlan/deleteBussiness";
        }
        $.ajax({
            type: "post",
            url: URL,    //向后端请求数据的url
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $("#entryTable").reloadCurrentData(baseUrl + "/administrative/getAdministrative", $("#queryForm").serializeJson(), "json", null, null);
                } else {
                    if (getResCode(data))
                        return;
                }
            }
        });
    }, function () {
        return;
    });
}

//审批通过
function approve(t, type) {
    if (type == 1) {
        if(t.id == "pass2"){
            approveTask1($("#taskId1").val(), 1, t.id, false,$("#desc").val());
        }else{
            if($(t).attr("data-gateway") == 1){ //请假三天，需要设置网关
                approveTask1($("#taskId1").val(), 1, t.id, true,$("#desc").val());
            }else{
                approveTask($("#taskId1").val(), 1, t.id,$("#desc").val());
            }
        }
    } else if (type == 2) {
        approveTask($("#timeWorkTaskId1").val(), 1, t.id,$("#desc2").val());
    } else if (type == 3) {
        approveTask($("#outWorkTaskId1").val(), 1, t.id,$("#desc3").val());
    } else if (type == 4) {
        approveTask($("#onBusinessTaskId1").val(), 1, t.id,$("#desc4").val());
        if($("#taskState").val()==8){
            cc($("#cc2").val(),$("#onBusinessAdministrativeId").val())
        }else if($("#taskState1").val()==22){
            cc1($("#cc1").val(),$("#administrativeId1").val())
        }

    }
}
//审批驳回
function reject(t, type) {
    if (type == 1) {
        if($("#reject1").attr("data-gateway") == 1){ //请假三天，需要设置网关
            approveTask1($("#taskId1").val(), 0, t.id, true,$("#desc").val());
        }else{
            approveTask($("#taskId1").val(), 0, t.id,$("#desc").val());
        }
    } else if (type == 2) {
        approveTask($("#timeWorkTaskId1").val(), 0, t.id,$("#desc2").val());
    } else if (type == 3) {
        approveTask($("#outWorkTaskId1").val(), 0, t.id,$("#desc3").val());
    } else if (type == 4) {
        approveTask($("#onBusinessTaskId1").val(), 0, t.id,$("#desc4").val());
        if($("#taskState").val() == 22){
            selectUser1($("#planId").val());
        }
    }
}

//计算加班时间
function getWorkTime(beginTime, endTime) {

    var days;
    var hours;
    var date;
    var beginTime = beginTime.toString();
    var endTime = endTime.toString();
    var stWorkTime = 8.5;
    var enWrokTime = 18;
    var freeTimeMon = 12;
    var freeTimeAft = 13.5;
    var freeTime = freeTimeAft - freeTimeMon;

    var beginArr = beginTime.split(" ");
    var beginMonth = parseInt(beginArr[0].split("-")[1]);
    var beginDay = parseInt(beginArr[0].split("-")[2]);
    var beginHours = parseInt(beginArr[1].split(":")[0]);
    var beginMin = parseInt(beginArr[1].split(":")[1]);

    var endArr = endTime.split(" ");
    var endMonth = parseInt(endArr[0].split("-")[1]);
    var endDay = parseInt(endArr[0].split("-")[2]);
    var endHours = parseInt(endArr[1].split(":")[0]);
    var endMin = parseInt(endArr[1].split(":")[1]);


    //同一个月
    if (beginMonth == endMonth) {
        //未跨天
        if (beginDay == endDay) {
            if (endHours < beginHours) {
                days = 0;
                hours = 0;
            } else if ((beginHours == stWorkTime) && (endHours == enWrokTime)) {
                days = 1;
                hours = 8;
            } else {
                //开始时间在上午
                if (beginHours < freeTimeMon) {
                    //结束时间在上午或者中午
                    if (endHours <= freeTimeMon) {
                        hours = endHours - beginHours;
                    } else if (endHours >= freeTimeMon && endHours <= freeTimeAft) {
                        //结束时间在中午
                        hours = freeTimeMon - beginHours;
                    } else {
                        //结束时间在下午
                        hours = endHours - beginHours - freeTime;
                    }
                }
                //开始时间在午休时间
                else if (beginHours >= freeTimeMon && beginHours <= freeTimeAft) {
                    //如果结束时间在中午
                    if (endHours >= freeTimeMon && endHours <= freeTimeAft) {
                        hours = 0;
                    } else {
                        //结束时间在下午
                        hours = endHours - freeTimeAft;
                    }
                }
                //开始时间在下午
                else {
                    hours = endHours - beginHours;
                }
                //事假按天计算
                if (hours <= 4 && hours > 2) {
                    days = 0.5;
                } else if (hours > 4) {
                    days = 1;
                } else {
                    days = 0;
                }
            }
        } else {
            var day = getDays(beginTime, endTime);
            var stdate;
            var endate;


            //如果请假开始时间早于上班开始时间
            if (beginHours < stWorkTime) {
                beginHours = stWorkTime;
            }
            if (endHours > enWrokTime) {
                endHours = enWrokTime;
            }
            //开始时间在上午
            if (beginHours < freeTimeMon) {
                stdate = enWrokTime - beginHours - freeTime;
            }
            //开始时间在中午
            else if (beginHours >= freeTimeMon && beginHours <= freeTimeAft) {
                stdate = enWrokTime - freeTimeAft;
            }
            //开始时间在下午
            else {
                stdate = 24 - beginHours;
            }
            // //结束时间在上班开始前
            if (endHours < stWorkTime) {
                endate = endHours;
            }
            //结束时间在上午
             else if (endHours < freeTimeMon && endHours >= stWorkTime) {
                endate = endHours - stWorkTime;
            }
            //结束时间在中午
            else if (endHours >= freeTimeMon && endHours <= freeTimeAft) {
                endate = freeTimeMon - stWorkTime;
            }
            //结束时间在下午
            else {
                endate = endHours - stWorkTime - freeTime;
            }
            //事假，每天请假时间2-4小时按半天计算，4小时以上按1天计算
            if (stdate <= 2) {
                if (endate > 2 && endate <= 4) {
                    days = day - 0.5;
                } else if (endate > 4) {
                    days = day;
                } else {
                    days = 0;
                }
            } else if (stdate > 2 && stdate <= 4) {
                if (endate <= 2) {
                    days = day - 0.5;
                } else if (endate > 2 && endate <= 4) {
                    days = day;
                } else {
                    days = day + 0.5;
                }
            } else {
                if (endate <= 2) {
                    days = day;
                } else if (endate > 2 && endate <= 4) {
                    days = day + 0.5;
                } else {
                    days = day + 1;
                }
            }
            //判断跨越的天数
            hours = (day - 1) * 8 + stdate + endate;
        }
    } else {
        var day = getDays(beginTime, endTime);
        var stdate;
        var endate;

        //如果请假开始时间早于上班开始时间
        if (beginHours < stWorkTime) {
            beginHours = stWorkTime;
        }
        if (endHours > enWrokTime) {
            endHours = enWrokTime;
        }
        //开始时间在上午
        if (beginHours < freeTimeMon) {
            stdate = enWrokTime - beginHours - freeTime;
        }
        //开始时间在中午
        else if (beginHours >= freeTimeMon && beginHours <= freeTimeAft) {
            stdate = enWrokTime - freeTimeAft;
        }
        //开始时间在下午
        else {
            stdate = enWrokTime - beginHours;
        }
        //结束时间在上班开始前
        if (endHours < stWorkTime) {
            endate = 0;
        }
        //结束时间在上午
        else if (endHours < freeTimeMon && endHours >= stWorkTime) {
            endate = endHours - stWorkTime;
        }
        //结束时间在中午
        else if (endHours >= freeTimeMon && endHours <= freeTimeAft) {
            endate = freeTimeMon - stWorkTime;
        }
        //结束时间在下午
        else {
            endate = endHours - stWorkTime - freeTime;
        }
        //事假，每天请假时间2-4小时按半天计算，4小时以上按1天计算
        if (stdate <= 2) {
            if (endate > 2 && endate <= 4) {
                days = day - 0.5;
            } else if (endate > 4) {
                days = day;
            } else {
                days = 0;
            }
        } else if (stdate > 2 && stdate <= 4) {
            if (endate <= 2) {
                days = day - 0.5;
            } else if (endate > 2 && endate <= 4) {
                days = day;
            } else {
                days = day + 0.5;
            }
        } else {
            if (endate <= 2) {
                days = day;
            } else if (endate > 2 && endate <= 4) {
                days = day + 0.5;
            } else {
                days = day + 1;
            }
        }
        //判断跨越的天数
        hours = (day - 1) * 8 + stdate + endate;
    }
    date = {"days": days, "hours": hours}
    return date;
}

//计算请假时间
function getTotal(beginTime, endTime) {
    var days;
    var hours;
    var date;
    var beginTime = beginTime.toString();
    var endTime = endTime.toString();
    var stWorkTime = 8.5;
    var enWrokTime = 18;
    var freeTimeMon = 12;
    var freeTimeAft = 13.5;
    var freeTime = freeTimeAft - freeTimeMon;

    var beginArr = beginTime.split(" ");
    var beginMonth = parseInt(beginArr[0].split("-")[1]);
    var beginDay = parseInt(beginArr[0].split("-")[2]);
    var beginHours = parseInt(beginArr[1].split(":")[0]);
    var beginMin = parseInt(beginArr[1].split(":")[1]);

    var endArr = endTime.split(" ");
    var endMonth = parseInt(endArr[0].split("-")[1]);
    var endDay = parseInt(endArr[0].split("-")[2]);
    var endHours = parseInt(endArr[1].split(":")[0]);
    var endMin = parseInt(endArr[1].split(":")[1]);
    //模糊计算分钟
    var otherHours=0;
    if ((beginHours > 8 && beginHours < 12 && endHours>8 && endHours<12) || (beginHours > 13.5 && beginHours < 18 && endHours>13.5 && endHours<18) || (beginHours > 8 && beginHours < 12 && endHours>13.5 && endHours<18)) {
        var betweenMin = endMin - beginMin;
        if (betweenMin > 0) {
            if (betweenMin > 30) {
                otherHours = 1;
            } else if (betweenMin > 0 && betweenMin <=30) {
                otherHours = 0.5;
            }
        } else {
            if (betweenMin < 0 && betweenMin >= -30) {
                otherHours = -0.5;
            } else if (betweenMin < -30) {
                otherHours = -1;
            }
        }
    }else if((beginHours<=8 && endHours>8 && endHours<12) || (beginHours<=8 && endHours>13.5 && endHours<18)){
        var betweenMin = endMin;
        if (betweenMin > 0) {
            if (betweenMin > 30) {
                otherHours = 1;
            } else if (betweenMin > 0 && betweenMin <= 30) {
                otherHours = 0.5;
            }
        }
    }

    //同一个月
    if (beginMonth == endMonth) {
        //未跨天
        if (beginDay == endDay) {
            //如果请假开始时间早于上班开始时间
            if (beginHours < stWorkTime) {
                beginHours = stWorkTime;
            }
            if (endHours > enWrokTime) {
                endHours = enWrokTime;
            }
            if (endHours < beginHours) {
                days = 0;
                hours = 0;
            } else if ((beginHours == stWorkTime) && (endHours == enWrokTime)) {
                days = 1;
                hours = 8;
            } else {
                //开始时间在上午
                if (beginHours < freeTimeMon) {
                    //结束时间在上午或者中午
                    if (endHours < freeTimeMon) {
                        hours = endHours - beginHours + otherHours;
                    } else if (endHours >= freeTimeMon && endHours <= freeTimeAft) {
                        //结束时间在中午
                        var tempHours = 0;
                        if (beginHours > 8 && beginHours < 12) {
                            if (beginMin>0 && beginMin <= 30) {
                                tempHours = 0.5;
                            } else if (beginMin > 30) {
                                tempHours = 1;
                            }
                        }
                        hours = freeTimeMon - beginHours - tempHours;
                    } else {
                        //结束时间在下午
                        hours = endHours - beginHours - freeTime + otherHours;
                    }
                }
                //开始时间在午休时间
                else if (beginHours >= freeTimeMon && beginHours <= freeTimeAft) {
                    //如果结束时间在中午
                    if (endHours >= freeTimeMon && endHours <= freeTimeAft) {
                        hours = 0;
                    } else {
                        //结束时间在下午
                        var tempHours = 0;
                        if(endHours<18){
                            if (endMin>0 && endMin <= 30) {
                                tempHours = 0.5;
                            } else if (endMin > 30) {
                                tempHours = 1;
                            }
                        }
                        hours = endHours - freeTimeAft + tempHours;
                    }
                }
                //开始时间在下午
                else {
                    var tempHours = 0;
                    if(endHours<18){
                        var tempMin= endMin-beginMin;
                        if (tempMin>0) {
                           if(tempMin>0 && tempMin<=30){
                               tempHours=0.5;
                           }else if(tempMin>30){
                               tempHours=1;
                           }
                        }else{
                            if(tempMin<0 && tempMin>=-30){
                                tempHours=-0.5;
                            }else if(tempMin<-30){
                                tempHours=-1;
                            }
                        }
                    }else if(endHours>=18){
                        var tempMin=-beginMin;
                        if(tempMin<0 && tempMin>=-30){
                            tempHours=-0.5;
                        }else if(tempMin<-30){
                            tempHours=-1;
                        }
                    }
                    hours = endHours - beginHours + tempHours;
                }
                //事假按天计算
                if (hours <= 4 && hours > 2) {
                    days = 0.5;
                } else if (hours > 4) {
                    days = 1;
                } else {
                    days = 0;
                }
            }
        }
        //跨天
        else {
            var day = getDays(beginTime, endTime);
            var stdate;
            var endate;
            //如果请假开始时间早于上班开始时间
            if (beginHours < stWorkTime) {
                beginHours = stWorkTime;
            }
            if (endHours > enWrokTime) {
                endHours = enWrokTime;
            }
            //开始时间在上午
            if (beginHours < freeTimeMon) {
                stdate = enWrokTime - beginHours - freeTime;
            }
            //开始时间在中午
            else if (beginHours >= freeTimeMon && beginHours <= freeTimeAft) {
                stdate = enWrokTime - freeTimeAft;
            }
            //开始时间在下午
            else {
                stdate = enWrokTime - beginHours;
            }
            //结束时间在上班开始前
            if (endHours < stWorkTime) {
                endate = 0;
            }
            //结束时间在上午
            else if (endHours < freeTimeMon && endHours >= stWorkTime) {
                endate = endHours - stWorkTime;
            }
            //结束时间在中午
            else if (endHours >= freeTimeMon && endHours <= freeTimeAft) {
                endate = freeTimeMon - stWorkTime;
            }
            //结束时间在下午
            else {
                endate = endHours - stWorkTime - freeTime;
            }
            //事假，每天请假时间2-4小时按半天计算，4小时以上按1天计算
            if (stdate <= 2) {
                if (endate > 2 && endate <= 4) {
                    days = day - 0.5;
                } else if (endate > 4) {
                    days = day;
                } else {
                    days = 0;
                }
            } else if (stdate > 2 && stdate <= 4) {
                if (endate <= 2) {
                    days = day - 0.5;
                } else if (endate > 2 && endate <= 4) {
                    days = day;
                } else {
                    days = day + 0.5;
                }
            } else {
                if (endate <= 2) {
                    days = day;
                } else if (endate > 2 && endate <= 4) {
                    days = day + 0.5;
                } else {
                    days = day + 1;
                }
            }
            //判断跨越的天数
            hours = (day - 1) * 8 + stdate + endate;
        }
    }
    //跨月
    else {
        var day = getDays(beginTime, endTime);
        var stdate;
        var endate;

        //如果请假开始时间早于上班开始时间
        if (beginHours < stWorkTime) {
            beginHours = stWorkTime;
        }
        if (endHours > enWrokTime) {
            endHours = enWrokTime;
        }
        //开始时间在上午
        if (beginHours < freeTimeMon) {
            stdate = enWrokTime - beginHours - freeTime;
        }
        //开始时间在中午
        else if (beginHours >= freeTimeMon && beginHours <= freeTimeAft) {
            stdate = enWrokTime - freeTimeAft;
        }
        //开始时间在下午
        else {
            stdate = enWrokTime - beginHours;
        }
        //结束时间在上班开始前
        if (endHours < stWorkTime) {
            endate = 0;
        }
        //结束时间在上午
        else if (endHours < freeTimeMon && endHours >= stWorkTime) {
            endate = endHours - stWorkTime;
        }
        //结束时间在中午
        else if (endHours >= freeTimeMon && endHours <= freeTimeAft) {
            endate = freeTimeMon - stWorkTime;
        }
        //结束时间在下午
        else {
            endate = endHours - stWorkTime - freeTime;
        }
        //事假，每天请假时间2-4小时按半天计算，4小时以上按1天计算
        if (stdate <= 2) {
            if (endate > 2 && endate <= 4) {
                days = day - 0.5;
            } else if (endate > 4) {
                days = day;
            } else {
                days = 0;
            }
        } else if (stdate > 2 && stdate <= 4) {
            if (endate <= 2) {
                days = day - 0.5;
            } else if (endate > 2 && endate <= 4) {
                days = day;
            } else {
                days = day + 0.5;
            }
        } else {
            if (endate <= 2) {
                days = day;
            } else if (endate > 2 && endate <= 4) {
                days = day + 0.5;
            } else {
                days = day + 1;
            }
        }
        //判断跨越的天数
        hours = (day - 1) * 8 + stdate + endate;
    }
    date = {"days": days, "hours": hours}
    return date;
}

//当请假类型是陪产假等请假类型的时候通过开始时间计算结束时间
function getEndTime(beginTime) {
    var v = document.getElementById("leaveType").value;
    if (v == "6" || v == "7" || v == "8" || v == "10") {
        var d1 = new Date(beginTime);
        var d2 = new Date(d1);
        var d3 = new Date(d2.setDate(d1.getDate() + 3)).format("yyyy-MM-dd hh:mm:ss");
        $("#endTime").attr("value", d3);
        getTotal(beginTime, d3);
    }
}

//判断开始时间小于结束时间
function checkDate(beginTime, endTime) {
    var start = new Date(beginTime.replace("-", "/").replace("-", "/"));
    var end = new Date(endTime.replace("-", "/").replace("-", "/"));
    if (end < start) {
        alert('结束日期不能小于开始日期！');
        return false;
    } else {
        return true;
    }
}

//两个日期之间的天数,只精确到天
function getDays(strDateStart, strDateEnd) {
    var beginArr = strDateStart.split(" ");
    var endArr = strDateEnd.split(" ");
    var strSeparator = "-"; //日期分隔符
    var oDate1;
    var oDate2;
    var iDays;
    oDate1 = beginArr[0].split(strSeparator);
    oDate2 = endArr[0].split(strSeparator);
    var strDateS = new Date(oDate1[0] + "-" + oDate1[1] + "-" + oDate1[2]);
    var strDateE = new Date(oDate2[0] + "-" + oDate2[1] + "-" + oDate2[2]);
    iDays = parseInt(Math.abs(strDateS - strDateE) / 1000 / 60 / 60 / 24);
    return iDays;
}

function reloadICheck() {
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green'
    });
}

function hasRoleXZ() {
    var len = user.roles.length;
    for (var i = 0; i < len; i++) {
        if (user.roles[i].type == 'XZ') {
            return true;
        }
    }
    return false;
}

//是否行政总监
function hasRoleXZZJ() {
    var len = user.roles.length;
    for (var i = 0; i < len; i++) {
        if (user.roles[i].type == 'XZ' && user.roles[i].code == 'ZJ') {
            return true;
        }
    }
    return false;
}

function rollback(t) {
    var taskId = $(t).parent().parent().parent().parent().find("input[name='taskId']").val();
    var target = $(t).parent().parent().find("select").val();
    $.get(baseUrl + "/process/rollback?taskId=" + taskId + "&target=" + target, function (result) {
        if (result.code === 200) {
            swal({
                title: "驳回成功",
                type: "success"
            });
        } else {
            swal({
                title: result.msg,
                type: "error"
            });
        }
    }, "json");
}

//审批驳回，将总结state =-9
function selectUser1(planId) {
    $.ajax({
        type:"post",
        url:"/userBusinessPlan/deleteConclusion",
        dataType:"json",
        data: {id: planId},
    })
}


// 切换單獨出差；
function changeLicence(dataValue) {
    if (!dataValue || dataValue == 1) {
        $(".licence").hide();
        $(".licence").find("input").each(function () {

        });
    } else {
        $(".licence").show();
        $(".licence").find("input").each(function () {
        });
    }
}

//抄送发待办
function cc(userId,administrativeId) {
    $.ajax({
        type:"post",
        url:"/userBusinessPlan/addItem",
        dataType:"json",
        data: {
            administrativeId: administrativeId,
            userId:userId
        },
        success:function (data) {
            $("#itemId").val(data.id)
        }
    })
    
}
//抄送发待办
function cc1(userId,administrativeId) {
    $.ajax({
        type:"post",
        url:"/userBusinessPlan/addConlusionItem",
        dataType:"json",
        data: {
            administrativeId: administrativeId,
            userId:userId
        },
        success:function (data) {
            $("#itemId1").val(data.id)
        }
    })

}


