var existsUserMap = {}; // 主持人内部人员集合
var existsUserMap1 ={} ; // 组织人内部人员集合
var existsUserMap2 = {} ;  //参会内部人员集合
var existsheadUser = {}; //负责人内部
var existshcheckUser = {}; //检查人内部
var existshVisible = {} ; //任务可见人员

var showUserDivMap = []; //增加主持人外部人员集合
var showUserDivMap1 = []; //增加组织人外部人员集合
var showUserDivMap2= []; //增加参会人外部人员集合
var  meetingUrl = "";
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#meetingTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });

    $('#imgModal').on('hidden.bs.modal', function () {
        $("body").addClass("modal-open");
    });

    commonObj.tabChange(0);

    //审批链接打开页面
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        var type = getQueryString("type");
        //会议审批待办跳转 type= 1
        if(type == 1){
            view(getQueryString("id"));
        }
        //会议日程跳转
        else if(type == 2) {
            tabMeeting(getQueryString("id"),type);
        }
    };
    // $('.newsContent').summernote({
    //     lang: 'zh-CN',
    //     height: 300
    //     // width: 900
    // });
    //人员搜索按钮点击事件
    $("#userSearch").on("click", function () {
        renderUserPage();
    });

    $("#dataSearch").click(function () {
        $("#meetingTable").emptyGridParam();
        $("#meetingTable").jqGrid('setGridParam',{
            postData:$("#queryForm").serializeJson(),
        }).trigger("reloadGrid");
        resize("#meetingTable");
    });



    createTable(); //表格定义
    //点击保存更改
    $("#submitEvaluationPeople").click(function () {
        var b = $("#submitEvaluationPeople").attr("data-divId");
        var btnList = "";
        if(b=="showUserDiv"){
            if(existsUserMap && Object.getOwnPropertyNames(existsUserMap).length > 0){
                for(var userId in existsUserMap){
                    var userName = existsUserMap[userId].userName;
                    var deptId = existsUserMap[userId].deptId;
                    var $span = '<div userId="'+userId+'" data-divId="'+b+'"  deptId="'+deptId+'" title="'+userName+'" onclick="removeBtn(this);" class="userDivClass">\n' +
                        '            <div class="userClass">'+userName+'</div>\n' +
                        '            <div title="删除" class="deleteClass">\n' +
                        '                <i class="fa fa-trash"></i>\n' +
                        '            </div>\n' +
                        '        </div>';
                    var inputUserId = '<input id="userId'+userId+'" name="inputUserId" type="hidden" value="' + userId + '">';
                    var inputUserName = '<input id="userName'+userId+'" name="inputUserName" type="hidden" value="' + userName + '">';
                    $span  += inputUserId;
                    $span  += inputUserName;
                    btnList += $span;
                    $("#showUserDiv").html(btnList);
                }
                for(var i=0;i<showUserDivMap.length; i++){
                    userWord(showUserDivMap[i],0);
                }
            }

        }else if(b=="showUserDiv1"){
            if(existsUserMap1 && Object.getOwnPropertyNames(existsUserMap1).length > 0){
                for(var userId in existsUserMap1){
                    var userName = existsUserMap1[userId].userName;
                    var deptId = existsUserMap1[userId].deptId;
                    var $span = '<div userId="'+userId+'" data-divId="'+b+'"  deptId="'+deptId+'" title="'+userName+'" onclick="removeBtn(this);" class="userDivClass">\n' +
                        '            <div class="userClass">'+userName+'</div>\n' +
                        '            <div title="删除" class="deleteClass">\n' +
                        '                <i class="fa fa-trash"></i>\n' +
                        '            </div>\n' +
                        '        </div>';
                    var inputUserId1 = '<input id="userId'+userId+'" name="inputUserId1" type="hidden" value="' + userId + '">';
                    var inputUserName1 = '<input id="userName'+userId+'" name="inputUserName1" type="hidden" value="' + userName + '">';
                    $span  += inputUserId1;
                    $span  += inputUserName1;
                    btnList += $span;
                    $("#showUserDiv1").html(btnList);
                }
                for(var i=0;i<showUserDivMap1.length; i++){
                    userWord(showUserDivMap1[i],1);
                }
            }
            //负责人
        }else if(b=="showUserDiv2") {
            if (existsUserMap2 && Object.getOwnPropertyNames(existsUserMap2).length > 0) {
                for (var userId in existsUserMap2) {
                    var userName = existsUserMap2[userId].userName;
                    var deptId = existsUserMap2[userId].deptId;
                    var $span = '<div userId="' + userId + '" data-divId="' + b + '"  deptId="' + deptId + '" title="' + userName + '" onclick="removeBtn(this);" class="userDivClass">\n' +
                        '            <div class="userClass">' + userName + '</div>\n' +
                        '            <div title="删除" class="deleteClass">\n' +
                        '                <i class="fa fa-trash"></i>\n' +
                        '            </div>\n' +
                        '        </div>';
                    var inputUserId1 = '<input id="userId' + userId + '" name="inputUserId2" type="hidden" value="' + userId + '">';
                    var inputUserName1 = '<input id="userName' + userId + '" name="inputUserName2" type="hidden" value="' + userName + '">';
                    $span += inputUserId1;
                    $span += inputUserName1;
                    btnList += $span;
                    $("#showUserDiv2").html(btnList);

                }
                for (var i = 0; i < showUserDivMap2.length; i++) {
                    userWord(showUserDivMap2[i],2);
                }
            }

        }else if(b=="headUser"){
            if(existsheadUser && Object.getOwnPropertyNames(existsheadUser).length > 0){
                for(var userId in existsheadUser){
                    var userName = existsheadUser[userId].userName;
                    var deptId = existsheadUser[userId].deptId;
                    var $span = '<div userId="'+userId+'" data-divId="'+b+'"  deptId="'+deptId+'" title="'+userName+'" onclick="removeBtn(this);" class="userDivClass">\n' +
                        '            <div class="userClass">'+userName+'</div>\n' +
                        '            <div title="删除" class="deleteClass">\n' +
                        '                <i class="fa fa-trash"></i>\n' +
                        '            </div>\n' +
                        '        </div>';
                    var inputUserId = '<input id="userId'+userId+'" name="inputUserId" type="hidden" value="' + userId + '">';
                    var inputUserName = '<input id="userName'+userId+'" name="inputUserName" type="hidden" value="' + userName + '">';
                    $span  += inputUserId;
                    $span  += inputUserName;
                    btnList += $span;
                    $("#headUser").html(btnList);
                }
            }

            //检查人
        }else if(b=="checkUser"){
            if(existshcheckUser && Object.getOwnPropertyNames(existshcheckUser).length > 0){
                for(var userId in existshcheckUser){
                    var userName = existshcheckUser[userId].userName;
                    var deptId = existshcheckUser[userId].deptId;
                    var $span = '<div userId="'+userId+'" data-divId="'+b+'"  deptId="'+deptId+'" title="'+userName+'" onclick="removeBtn(this);" class="userDivClass">\n' +
                        '            <div class="userClass">'+userName+'</div>\n' +
                        '            <div title="删除" class="deleteClass">\n' +
                        '                <i class="fa fa-trash"></i>\n' +
                        '            </div>\n' +
                        '        </div>';
                    var inputUserId1 = '<input id="userId'+userId+'" name="inputUserId1" type="hidden" value="' + userId + '">';
                    var inputUserName1 = '<input id="userName'+userId+'" name="inputUserName1" type="hidden" value="' + userName + '">';
                    $span  += inputUserId1;
                    $span  += inputUserName1;
                    btnList += $span;
                    $("#checkUser").html(btnList);

                }
            }
        }else if(b == "visibleUser"){
            if(existshVisible && Object.getOwnPropertyNames(existshVisible).length > 0){
                for(var userId in existshVisible){
                    var userName = existshVisible[userId].userName;
                    var deptId = existshVisible[userId].deptId;
                    var $span = '<div userId="'+userId+'" data-divId="'+b+'"  deptId="'+deptId+'" title="'+userName+'" onclick="removeBtn(this);" class="userDivClass">\n' +
                        '            <div class="userClass">'+userName+'</div>\n' +
                        '            <div title="删除" class="deleteClass">\n' +
                        '                <i class="fa fa-trash"></i>\n' +
                        '            </div>\n' +
                        '        </div>';
                    var inputVisibleUserId = '<input id="userId'+userId+'" name="inputVisibleUserId" type="hidden" value="' + userId + '">';
                    var inputVisibleUserName = '<input id="userName'+userId+'" name="inputVisibleUserName" type="hidden" value="' + userName + '">';
                    $span  += inputVisibleUserId;
                    $span  += inputVisibleUserName;
                    btnList += $span;
                    $("#visibleUser").html(btnList);
                }
            }
        }
        $('#evaluationModal').modal('hide');
    });


});
//公共方法
var commonObj = {
    //Tab切换处理事件
    tabChange: function (index) {
        $(".tabContent").css("display","none");
        createTable();
        if(index == 0){
            $("#meetingTab").css("display","block");
            createTable(); //初始化会议管理
        }else if(index == 1) {
            $("#meetingTestTab").css("display","block");
            viewAllMeet();//初始化会议任务
        }
    },
    tabChange1: function (index) {
        $(".tabContent1").css("display","none");
        viewAllMeet();//初始化会议任务
        if(index == 0){
            $("#allMeetingTask").css("display","block");
            viewAllMeet();//初始化会议管理
        }else if(index ==1) {
            $("#meMeetingTask").css("display","block");
            meMeet();
        }else{
            $("#duMeetingTask").css("display","block");
            duMeet();
        }
    },
    //校验开始结束时间
    validateTaskTime:function (validateTime, errorTipId, otherTipId,type, otherTimeId) {
        var validateFlag = true; //默认校验成功
        var errorMessage = "";
        $(errorTipId).css("display", "none");
        $(otherTipId).css("display", "none");
        if(validateTime){
            if($(otherTimeId).val()){
                if(type == 1){  //validateTime为开始时间、otherTimeId为结束时间ID
                    var endTime = $(otherTimeId).val();
                    if(validateTime > endTime){
                        errorMessage = "开始时间不能大于结束时间";
                        validateFlag = false;
                    }
                }else {
                    var startTime = $(otherTimeId).val();
                    if(startTime > validateTime){
                        errorMessage = "结束时间不能小于开始时间";
                        validateFlag = false;
                    }
                }
            }
            if(errorMessage){
                $(errorTipId).css("display", "inline-block");
                $(errorTipId).text(errorMessage);
            }else {
                $(errorTipId).css("display", "none");
            }
        }
        return validateFlag;
    },
    permissAuth:function () {
        var flag = false;
        requestData(null, "/meetingRoom/getMeetingRoomSettingAuth", "get", "json", false, function (data) {
            if(data.code == 200){
                flag = true;
            }
        });
        return flag;
    },
}


function tabMeeting(meetId,t) {
    var index;
    if(t==2){
        index=10;
    }else {
        index=-98000
    }
    new MeetingDetailCompont({
        id: meetId,
        zIndex: index,
        //同意按钮回调函数
        agreeCallback: function (data) {
            return meetingFlag(data.data.entity.id,1);
        },
        //拒绝按钮回调函数
        refuseCallback:function (data) {
            return meetingFlag(data.data.entity.id,2);
        },
        //新增会议纪要按钮回调函数
        meetSummaryCallback:function (data) {
            addRecord(data.data.entity.id,1);
        },
        //新增会议记录回调函数
        meetRecordCallback:function (data) {
            addRecord(data.data.entity.id,0);
        },
        //查看会议纪要按钮回调函数
        queryMeetSummaryCallback:function (data) {
            viewRecord(data.data.entity.id,1);
        },
        //查看会议记录按钮回调函数
        queryMeetRecordCallback:function (data) {
            viewRecord(data.data.entity.id,0);
        },
        //查看会议任务回调函数
        queryMeetTaskCallback:function (data) {
            viewtask(data.data.entity.id,0);
        },
        //新增会议任务回调函数
        meetTaskCallback:function (data) {
            addTask(data.data.entity.id);
        }
    }).render();
}
//审批通过
function approve(t) {
    approveTask($(t).parent().find("input[name='taskId']").val(), 1, t.id, null);
};
function reject(t) {
    approveTask($(t).parent().find("input[name='taskId']").val(), 0,t.id, null);
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
var requestData = function (data, url, requestType,dataType,async,callBackFun, contentType) {
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
//查看全部任务
function viewAllMeet() {
    var $meetingTaskTable = $("#meetingTest");
    $meetingTaskTable.jqGrid({
        url : baseUrl+'/meetingTask/meetingTaskListPg',
        datatype: "json",
        postData: $("#queryForm").serializeJson(),
        mtype: 'post',
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
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15, //每页记录数
        rowList: [15, 30, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                width: 30,
                hidden: true,
            },
            {
                name: 'meetTitle',
                label: '会议',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'title',
                label: '任务标题',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'inputUserName',
                label: '负责人',
                width: 30,
                editable: true,
                sortable: false,
            },
            {
                name: 'inputUserName1',
                label: '检查人',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'startTime',
                label: '开始时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'endTime',
                label: '结束时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'content',
                label: '备注',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'state',
                label: '状态',
                width: 30,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    var time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    var html="";
                    //会议未开始，就是会议开始时间在当前时间后面  就是大于当前
                    if(rows.startTime > time && value == 0  ){
                        html +="未开始";
                    }else if(rows.startTime < time && time < rows.endTime && value==0){
                        html +="进行中";
                    }else if (value == 0 && time>rows.endTime){
                        html +="超期";
                    } else if (value == 1) {
                        html +="已延期";
                    }else if (value == 2){
                        html +="已结束";
                    }else if (value == 3){
                        html +="检查通过";
                    }else if (value == 4){
                        html +="检查不通过";
                    }
                    else {
                        html +="";
                    }
                    return html;
                }
            },
            {
                name: 'state',
                label: '操作',
                width: 60,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    var html="";
                    var t = false;
                    if (rows.userId1){
                        var userId = rows.userId1.split(',');
                        if (userId.length>0){
                            for (var i=0;i<userId.length;i++){
                                if(userId[i]==user.id){
                                    t=true;
                                    break;
                                }else {
                                    t=false;
                                }
                            }
                        }
                    }
                    var  time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    if(value == 3){
                        html +="";
                    }else if((value == 4 || value == 1) && time>rows.endTime && user.id==rows.createId){
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='editMeetTask("+rows.id+","+rows.meetId+",\""+$meetingTaskTable.selector+"\")'> 重发 </a>"
                    } else if(t && value ==0 && time>rows.startTime) {
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='baocData("+rows.id+","+3+",\""+$meetingTaskTable.selector+"\")'> 检查通过 </a>"
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='baocData("+rows.id+","+4+",\""+$meetingTaskTable.selector+"\")'> 检查不通过 </a>"
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='delay("+rows.id+",\""+rows.endTime+"\",\""+$meetingTaskTable.selector+"\")'> 延期 </a>"
                    }else{
                        html +="";
                    }
                    return html;
                }
            }
        ],
        pager: "#meetingTestPaper",
        viewrecords: true,
        caption: "会议任务列表",
        hidegrid: false,
        gridComplete: function () {
            var width = $('#meetingTest').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#meetingTest').setGridWidth(width);
        },
        onSelectRow: function(rowid,status,e){
            var rowData = $("#meetingTest").jqGrid('getRowData',rowid);//获取当前行的数据
        },
    });
    $("#meetingTest").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入

}
//会议管理中查看会议任务列表
function meetingTask(id,t) {
    $("#meetId").val(id);
    var $meetingTask = $("#meetingTask");
    $meetingTask.jqGrid({
        url : baseUrl+'/meetingTask/meetingTask',
        datatype: "json",
        postData: {id:id},
        mtype: 'post',
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
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15, //每页记录数
        rowList: [15, 30, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                width: 30,
                hidden: true,
            },
            {
                name: 'title',
                label: '任务标题',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'inputUserName',
                label: '负责人',
                width: 30,
                editable: true,
                sortable: false,
            },
            {
                name: 'inputUserName1',
                label: '检查人',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'startTime',
                label: '开始时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'endTime',
                label: '结束时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'content',
                label: '备注',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'state',
                label: '状态',
                width: 30,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    var time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    var html="";
                    //会议未开始，就是会议开始时间在当前时间后面  就是大于当前
                    if(rows.startTime > time && value == 0  ){
                        html +="未开始";
                    }else if(rows.startTime < time && time < rows.endTime && value==0){
                        html +="进行中";
                    }else if (value == 0 && time>rows.endTime){
                        html +="超期";
                    } else if (value == 1) {
                        html +="已延期";
                    }else if (value == 2){
                        html +="已结束";
                    }else if (value == 3){
                        html +="检查通过";
                    }else if (value == 4){
                        html +="检查不通过";
                    }
                    else {
                        html +="";
                    }
                    return html;
                }
            },
            {
                name: 'state',
                label: '操作',
                width: 60,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    var html="";
                    var t = false;
                   if (rows.userId1){
                       var userId = rows.userId1.split(',');
                       if (userId.length>0){
                           for (var i=0;i<userId.length;i++){
                               if(userId[i]==user.id){
                                    t=true;
                                    break;
                               }else {
                                   t=false;
                               }
                           }
                       }
                   }
                    var  time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    if(value == 3){
                        html +="";
                    }else if((value == 4 || value == 1) || time>rows.endTime && user.id==rows.createId){
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='editMeetTask("+rows.id+","+rows.meetId+",\""+$meetingTask.selector+"\")'> 重发 </a>"
                    } else if(t && value ==0 && time>rows.startTime) {
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='baocData("+rows.id+","+3+",\""+$meetingTask.selector+"\")'> 检查通过 </a>"
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='baocData("+rows.id+","+4+",\""+$meetingTask.selector+"\")'> 检查不通过 </a>"
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='delay("+rows.id+",\""+rows.endTime+"\",\""+$meetingTask.selector+"\")'> 延期 </a>"
                    }else{
                        html +="";
                    }
                    return html;
                }
            }
        ],
        pager: "#meetingTaskPager",
        viewrecords: true,
        caption: "会议任务列表",
        hidegrid: false,
        gridComplete: function () {
            var width = $('#meetingTask').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#meetingTask').setGridWidth(width);
        },
        onSelectRow: function(rowid,status,e){
            var rowData = $("#meetingTask").jqGrid('getRowData',rowid);//获取当前行的数据
        },

    });
    $("#meetingTask").jqGrid('setGridParam', {
        postData: {id:id}, //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//会议纪要记录查看列表
function viewMeetingRecordListPg(id,t) {
    var $meetingRecord = $("#meetingRecord");
    $meetingRecord.jqGrid({
        url : baseUrl+'/meetingRecordViewUser/meetingRecordListPg',
        datatype: "json",
        postData: {id:id,recordType:$("#recordType1").val()},
        mtype: 'post',
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 10,//第一页
        rownumbers: true,
        setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15, //每页记录数
        rowList: [15, 30, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                width: 30,
                hidden: true,
            },
            {
                name: 'title',
                label: '会议名称',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'name',
                label: '可见人员',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'content',
                label: '内容',
                width: 90,
                editable: true,
                sortable: false
            }
        ],
        pager: "#meetingRecordPager",
        viewrecords: true,
        caption: "记录纪要列表",
        hidegrid: false,
        gridComplete: function () {
            var width = $('#meetingRecord').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#meetingRecord').setGridWidth(width);
        },
        onSelectRow: function(rowid,status,e){
            var rowData = $("#meetingRecord").jqGrid('getRowData',rowid);//获取当前行的数据
        },
    });
    $("#meetingRecord").jqGrid('setGridParam', {
        postData: {id:id,recordType:$("#recordType1").val()}, //发送数据
    }).trigger("reloadGrid"); //重新载入


}

//查看我的任务
function meMeet() {
    var $meetingTaskTable = $("#meetingTest1");
    $meetingTaskTable.jqGrid({
        url : baseUrl+'/meetingTask/meMeet',
        datatype: "json",
        postData: $("#queryForm").serializeJson(),
        mtype: 'post',
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
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15, //每页记录数
        rowList: [15, 30, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                width: 30,
                hidden: true,
            },
            {
                name: 'meetTitle',
                label: '会议',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'title',
                label: '任务标题',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'inputUserName',
                label: '负责人',
                width: 30,
                editable: true,
                sortable: false,
            },
            {
                name: 'inputUserName1',
                label: '检查人',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'startTime',
                label: '开始时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'endTime',
                label: '结束时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'content',
                label: '备注',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'state',
                label: '状态',
                width: 30,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    var time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    var html="";
                    //会议未开始，就是会议开始时间在当前时间后面  就是大于当前
                    if(rows.startTime > time && value == 0  ){
                        html +="未开始";
                    }else if(rows.startTime < time && time < rows.endTime && value==0){
                        html +="进行中";
                    }else if (value == 0 && time>rows.endTime){
                        html +="超期";
                    } else if (value == 1) {
                        html +="已延期";
                    }else if (value == 2){
                        html +="已结束";
                    }else if (value == 3){
                        html +="检查通过";
                    }else if (value == 4){
                        html +="检查不通过";
                    }
                    else {
                        html +="";
                    }
                    return html;
                }
            },
            {
                name: 'state',
                label: '操作',
                width: 60,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    var html="";
                    var t = false;
                    if (rows.userId1){
                        var userId = rows.userId1.split(',');
                        if (userId.length>0){
                            for (var i=0;i<userId.length;i++){
                                if(userId[i]==user.id){
                                    t=true;
                                    break;
                                }else {
                                    t=false;
                                }
                            }
                        }
                    }
                    var  time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    if(value == 3){
                        html +="";
                    }else if((value == 4 || value == 1) || time>rows.endTime && user.id==rows.createId){
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='editMeetTask("+rows.id+","+rows.meetId+",\""+$meetingTaskTable.selector+"\")'> 重发 </a>"
                    } else if(t && value ==0 && time>rows.startTime) {
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='baocData("+rows.id+","+3+",\""+$meetingTaskTable.selector+"\")'> 检查通过 </a>"
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='baocData("+rows.id+","+4+",\""+$meetingTaskTable.selector+"\")'> 检查不通过 </a>"
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='delay("+rows.id+",\""+rows.endTime+"\",\""+$meetingTaskTable.selector+"\")'> 延期 </a>"
                    }else{
                        html +="";
                    }
                    return html;
                }
            }
        ],
        pager: "#meetingTestPaper1",
        viewrecords: true,
        caption: "会议任务列表",
        hidegrid: false,
        gridComplete: function () {
            var width = $('#meetingTest1').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#meetingTest1').setGridWidth(width);
        },
        onSelectRow: function(rowid,status,e){
            var rowData = $("#meetingTest1").jqGrid('getRowData',rowid);//获取当前行的数据
        },
        // ondblClickRow: function (rowid, iRow, iCol, e) {
        //     view(rowid);
        // }
    });
    $("#meetingTest1").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入

}

//查看督办任务
function duMeet() {
    var $meetingTaskTable = $("#meetingTest2");
    $meetingTaskTable.jqGrid({
        url : baseUrl+'/meetingTask/duMeet',
        datatype: "json",
        postData: $("#queryForm").serializeJson(),
        mtype: 'post',
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
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15, //每页记录数
        rowList: [15, 30, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                width: 30,
                hidden: true,
            },
            {
                name: 'meetTitle',
                label: '会议',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'title',
                label: '任务标题',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'inputUserName',
                label: '负责人',
                width: 30,
                editable: true,
                sortable: false,
            },
            {
                name: 'inputUserName1',
                label: '检查人',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'startTime',
                label: '开始时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'endTime',
                label: '结束时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'content',
                label: '备注',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'state',
                label: '状态',
                width: 30,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    var time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    var html="";
                    //会议未开始，就是会议开始时间在当前时间后面  就是大于当前
                    if(rows.startTime > time && value == 0  ){
                        html +="未开始";
                    }else if(rows.startTime < time && time < rows.endTime && value==0){
                        html +="进行中";
                    }else if (value == 0 && time>rows.endTime){
                        html +="超期";
                    } else if (value == 1) {
                        html +="已延期";
                    }else if (value == 2){
                        html +="已结束";
                    }else if (value == 3){
                        html +="检查通过";
                    }else if (value == 4){
                        html +="检查不通过";
                    }
                    else {
                        html +="";
                    }
                    return html;
                }
            },
            {
                name: 'state',
                label: '操作',
                width: 60,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    var html="";
                    var t = false;
                    if (rows.userId1){
                        var userId = rows.userId1.split(',');
                        if (userId.length>0){
                            for (var i=0;i<userId.length;i++){
                                if(userId[i]==user.id){
                                    t=true;
                                    break;
                                }else {
                                    t=false;
                                }
                            }
                        }
                    }
                    var  time = new Date().format("yyyy-MM-dd hh:mm:ss");
                    if(value == 3){
                        html +="";
                    }else if((value == 4 || value == 1) && time>rows.endTime && user.id==rows.createId){
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='editMeetTask("+rows.id+","+rows.meetId+",\""+$meetingTaskTable.selector+"\")'> 重发 </a>"
                    } else if(t && value ==0 &&time>rows.startTime) {
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='baocData("+rows.id+","+3+",\""+$meetingTaskTable.selector+"\")'> 检查通过 </a>"
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='baocData("+rows.id+","+4+",\""+$meetingTaskTable.selector+"\")'> 检查不通过 </a>"
                        html +="<a href=' javascript:void(0)' style='height: 22px;width: 40px;' onclick='delay("+rows.id+",\""+rows.endTime+"\",\""+$meetingTaskTable.selector+"\")'> 延期 </a>"
                    }else{
                        html +="";
                    }
                    return html;
                }
            }
        ],
        pager: "#meetingTestPaper2",
        viewrecords: true,
        caption: "会议任务列表",
        hidegrid: false,
        gridComplete: function () {
            var width = $('#meetingTest2').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#meetingTest2').setGridWidth(width);
        },
        onSelectRow: function(rowid,status,e){
            var rowData = $("#meetingTest2").jqGrid('getRowData',rowid);//获取当前行的数据
        },
    });
    $("#meetingTest2").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入

}
//会议管理
function createTable() {
    // if (commonObj.permissAuth()){
    //     //说明是会议管理员
    //     meetingUrl ='/meeting/meetingAllList';
    // }else {
    //     meetingUrl ='';
    // }
    var $meetingTable = $("#meetingTable");
    $meetingTable.jqGrid({
        url: baseUrl + '/meeting/meetingListPg',
        datatype: "json",
        postData: $("#queryForm").serializeJson(),
        mtype: 'post',
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
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15, //每页记录数
        rowList: [15, 30, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                width: 30,
                hidden: true,
            },
            {
                name: 'address',
                label: '会议室地址',
                width: 45,
                editable: true,
                sortable: false,
                formatter:function (value,grid,rows) {
                    var html ="";
                    if(rows.meetRoomId){
                       html+= rows.meetRoomName+"("+rows.meetRoomAddress+")";
                    }else {
                        html += value;
                    }
                    return html;
                }
            },
            {
                name: 'title',
                label: '会议名称',
                width: 45,
                editable: true,
                sortable: false
            },
            // {
            //     name: '',
            //     label: '会议日期',
            //     width: 30,
            //     editable: true,
            //     sortable: false
            // },
            {
                name: 'startTime',
                label: '开始时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'endTime',
                label: '结束时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'userName',
                label: '预定人',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'createDate',
                label: '会议室预定时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'auditUserName',
                label: '审批人',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'otherSum',
                label: '邀约人数',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'acceptSum',
                label: '接受人数',
                width:30,
                editable: true,
                sortable: false
            },
            {
                name: '',
                label: '会议时长（分钟）',
                width: 30,
                editable: true,
                sortable: false,
                formatter:function (value,grid, rows) {
                    var html ="";
                    var startTime = rows.startTime;
                    var endTime = rows.endTime;
                    var html = ((new Date(endTime)- new Date(startTime))/1000/60).toFixed(0);
                    return html;
                }
            },
            {
              name:'relateMeetId',
                label:'关联会议',
                width:30,
                editable:true,
                sortable: false,
                formatter:function (value,grid,rows) {
                    var html="";
                    if(value){
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='tabMeeting("+value+")'>查看关联会议&nbsp;&nbsp;</a>";

                    }else {
                        html +="";
                    }
                    return html;
                }
            },
            {
                name: '',
                label: '会议任务',
                width: 30,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    var html="";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='viewtask(" + rows.id + ","+0+")'>查看任务&nbsp;&nbsp;</a>";
                   // html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='viewRecord(" + rows.id + ","+1+")'>查看会议纪要&nbsp;&nbsp;</a>";
                    //html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='view(" + rows.id + ")'>查看审批&nbsp;&nbsp;</a>";
                    return html;
                }
            },
            {
                name: 'state',
                label: '状态',
                width: 30,
                editable: true,
                sortable: false,
                formatter:function (value ,grid, rows) {
                    if(value == -1){
                        return "审核驳回";
                    }else if(value == 2){
                        return "审核中";
                    }else {
                        return "正常";
                    }
                }
            },
            {
                name:'',
                label:'操作',
                width:30,
                editable:true,
                sortable:false,
                formatter:function (value,grid,rows) {
                    var html="";
                    if(rows.meetRoomId==null && user.id==rows.createId){
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='editMeeting(" + rows.id + ")'>编辑&nbsp;&nbsp;</a>";

                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='delMeeting(" + rows.id + ")'>删除&nbsp;&nbsp;</a>";

                    }


                    return html;
                }

            }
        ],
        pager: "#meetingPaper",
        viewrecords: true,
        caption: "会议列表",
        hidegrid: false,
        gridComplete: function () {
            var width = $('#meetingTable').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#meetingTable').setGridWidth(width);
        },
        onSelectRow: function(rowid,status,e){
           var rowData = $("#meetingTable").jqGrid('getRowData',rowid);//获取当前行的数据
       },
        ondblClickRow: function (rowid, iRow, iCol, e) {
            tabMeeting(rowid);
            }
    });
    $("#meetingTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入

}
//刷新表格
function reflushTable() {
    //刷新表格
    $("#meetingTable").emptyGridParam(); //清空历史查询数据
    $("#meetingTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}
//添加按钮点击事件
function addBtnClick(t) {
    //清空列表人员
    existsUserMap = {}; // 主持人内部人员集合
    existsUserMap1 ={} ; // 组织人内部人员集合
     existsUserMap2 = {} ;  //参会内部人员集合
     existsheadUser = {}; //负责人内部
    showUserDivMap=[];
    showUserDivMap1=[];
    showUserDivMap2=[];
     existshcheckUser = {} //检查人内部
    $("#addMeetingRoom").modal("toggle");
    document.getElementById("addMeetingFrom").reset();
    $("#addMeetingFrom").find("input").removeClass("error");
    $("#addMeetingFrom").find("textarea").removeClass("error");
    $("input[type='checkbox']").each(function (i,t) {
        $(t).iCheck('uncheck');
    });
    $("#showUserDiv").empty();
    $("#showUserDiv1").empty();
    $("#showUserDiv2").empty();
    $("#affixDiv").empty();
    $(".view").hide();
    $("#wordName").val("");
    $(".add").show();
    $(".edit").hide();
    //刷新select 下拉框
    layui.use(["form"], function () {
        layui.form.render();
    });

}
//添加公司内部人员
function tast(t) {
    $("#evaluationModal").modal('toggle');
    $("#nameQc").val("");
    renderUserPage(t);
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
var requestData = function (data, url, requestType,dataType,async,callBackFun, contentType) {
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

//渲染人员选择页面
function renderUserPage(t) {
    $("#submitEvaluationPeople").attr("data-divId",$(t).attr("data-divId"));
    var b = $("#submitEvaluationPeople").attr("data-divId");
    var rootDom = $("#evaluationModal");

    if (b=="visibleUser"){
        //如果是从会议记录中点进的话 设置记录可见人员 公司内部人员列表只显示会议的参与人员（会议相关人员都要显示）
        var param = {id:$("#meetId1").val(), name: $("#nameQc").val().trim()};
        var listUser ="/meeting/meetUsers?"+$.param(param);
    }else {
        var listUser = "/userGroup/listBusinessPart?name=" + $("#nameQc").val().trim();
    }

    //渲染人员页面
    requestData(null, listUser, "get","json",false,function (data) {
        var userList = groupBy(data, function (item) {
            return [item.deptId];
        });
        var html = template("excludePeopleHtml", {'data': userList}); //将用户数据渲染html
        rootDom.find("div[data-id='groups']").html(html);
        rootDom.find('input[data-id="all"]').iCheck('uncheck');
        reloadICheck(rootDom); //重新加载i-checks

        //部门复选框点击事件
        rootDom.find(".deptSpan").on('ifChecked', function () {
            var userInputArr = $(this).parent().parent().parent().next().find("input[type='checkbox']");
            if(userInputArr && userInputArr.length > 0){
                $.each(userInputArr, function (index,t) {
                    $(t) .iCheck('check');
                    if(b=="showUserDiv"){
                        existsUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                    }else if(b=="showUserDiv1"){
                        existsUserMap1[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                    }else if (b=="showUserDiv2"){
                        existsUserMap2[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                    }else if (b == "headUser"){
                        existsheadUser[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                    }else if (b == "checkUser"){
                        existshcheckUser[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                    }else if (b == "visibleUser"){
                        existshVisible[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId:$(t).attr('deptId')};
                    }
                })
            }

        });
        //部门复选框取消点击事件
        rootDom.find(".deptSpan").on('ifUnchecked', function () {
            var userInputArr = $(this).parent().parent().parent().next().find("input[type='checkbox']");
            if(userInputArr && userInputArr.length > 0){
                $.each(userInputArr, function (index,t) {
                    $(t) .iCheck('uncheck');
                    if(b=="showUserDiv"){
                        delete existsUserMap[$(t).attr('userId')];
                    }else if(b=="showUserDiv1"){
                        delete existsUserMap1[$(t).attr('userId')];
                    }else if (b=="showUserDiv2"){
                        delete existsUserMap2[$(t).attr('userId')];
                    }else if (b == "headUser"){
                        delete existsheadUser[$(t).attr('userId')];
                    }else if (b == "checkUser"){
                        delete existshcheckUser[$(t).attr('userId')];
                    }else if (b == "visibleUser"){
                        delete existshVisible[$(t).attr('userId')];
                    }

                })
            }
        });
        //全选按钮点击事件
        rootDom.find('input[data-id="all"]').on('ifChecked', function () {
            var userInputArr = rootDom.find("div[data-id='groups']").find(".i-checks");
            if(userInputArr && userInputArr.length > 0){
                $.each(userInputArr, function (index,t) {
                    $(t) .iCheck('check');
                    if($(t).attr('userId')){
                        if (b=="showUserDiv"){
                            existsUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if(b=="showUserDiv1"){
                            existsUserMap1[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if (b=="showUserDiv2"){
                            existsUserMap2[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if (b == "headUser"){
                            existsheadUser[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if (b == "checkUser"){
                            existshcheckUser[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if (b == "visibleUser"){
                            existshVisible[$(t).attr('userId')] = {userName:$(t).attr('userName'),deptId:$(t).attr('deptId')};
                        }

                    }
                })
            }
        });
        //全选按钮取消点击事件
        rootDom.find('input[data-id="all"]').on('ifUnchecked', function () {
            var userInputArr = rootDom.find("div[data-id='groups']").find(".i-checks");
            if(userInputArr && userInputArr.length > 0){
                $.each(userInputArr, function (index,t) {
                    $(t) .iCheck('uncheck');
                    if (b=="showUserDiv"){
                        existsUserMap = {};
                    }else if(b=="showUserDiv1"){
                        existsUserMap1 = {};
                    }else if(b=="showUserDiv2"){
                        existsUserMap2 = {};
                    }else if(b == "headUser"){
                        existsheadUser ={};
                    }else if(b == "checkUser"){
                        existshcheckUser={};
                    }else if(b== "visibleUser"){
                        existshVisible={};
                    }

                })
            }
        });

        //用户复选框点击事件
        $(".userSpan").on('ifClicked', function () {
            var flag = $(this).is(':checked');
            if(flag){  //如果是选中状态，则删除用户
                if (b=="showUserDiv"){
                    delete existsUserMap[$(this).attr('userId')];
                }else if(b=="showUserDiv1"){
                    delete existsUserMap1[$(this).attr('userId')];
                }else if(b=="showUserDiv2"){
                    delete existsUserMap2[$(this).attr('userId')];
                }else if(b == "headUser"){
                    delete existsheadUser [$(this).attr('userId')];
                }else if(b == "checkUser"){
                    delete existshcheckUser[$(this).attr('userId')];
                }else if(b == "visibleUser"){
                    delete existshVisible[$(this).attr('userId')];
                }

            }else{
                if (b=="showUserDiv"){
                    existsUserMap[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                }else if(b=="showUserDiv1"){
                    existsUserMap1[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                }else if(b=="showUserDiv2"){
                    existsUserMap2[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};

                }else if(b == "headUser"){
                    existsheadUser [$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                }else if(b == "checkUser"){
                    existshcheckUser[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                }else if (b == "visibleUser"){
                    existshVisible[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                }
            }
        });
    })

    //判断当前已选择页面
    if (b=="showUserDiv"){
        if(existsUserMap && Object.getOwnPropertyNames(existsUserMap).length > 0){
            for(var key in existsUserMap){
                $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
            }
        }
    }else if(b=="showUserDiv1"){
        if(existsUserMap1 && Object.getOwnPropertyNames(existsUserMap1).length > 0){
            for(var key in existsUserMap1){
                $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
            }
        }
    }else if(b=="showUserDiv2"){
        if(existsUserMap2 && Object.getOwnPropertyNames(existsUserMap2).length > 0){
            for(var key in existsUserMap2){
                $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
            }
        }
    }else if(b == "headUser"){
        if(existsheadUser && Object.getOwnPropertyNames(existsheadUser).length > 0){
            for(var key in existsheadUser){
                $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
            }
        }
    }else if(b == "checkUser"){
        if(existshcheckUser && Object.getOwnPropertyNames(existshcheckUser).length > 0){
            for(var key in existshcheckUser){
                $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
            }
        }
    }else if (b == "visibleUser"){
        if (existshVisible && Object.getOwnPropertyNames(existshVisible).length > 0){
            for(var key in existshVisible){
                $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
            }
        }
    }
}

//人员部门分组
function groupBy(array, f) {
    var groups = {};
    array.forEach(function (o) {
        var group = JSON.stringify(f(o));
        groups[group] = groups[group] || [];
        groups[group].push(o);
    });
    return Object.keys(groups).map(function (group) {
        return groups[group];
    });
}
//重新加载i-checks
function reloadICheck(root) {
    root.find('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green'
    });
}

//新增会议任务  1增加  2、编辑
function addMeetingTask(t, url,flag,tableId) {
    if ($("#addMeetingTestFrom").valid()) {
        if (flag == 1) {
            var validateTaskResult = commonObj.validateTaskTime($("#startDate2").val(), "#startDate2-error", "#endDate2-error", 1, "#endDate2");
            if (!validateTaskResult) return;
            if(!existsheadUser || Object.getOwnPropertyNames(existsheadUser).length <= 0){
                layer.msg("设置会议任务负责人员！", {time: 1000, icon: 5});
                return;
            }
            if(!existshcheckUser || Object.getOwnPropertyNames(existshcheckUser).length <= 0){
                layer.msg("设置会议任务检查人员！", {time: 1000, icon: 5});
                return;
            }
            //var formData = new FormData($("#addMeetingTestFrom")[0]);
            var formData = $("#addMeetingTestFrom").serializeJson();
            if (formData.hasOwnProperty("headUser") && !Array.isArray(formData.headUser)) {
                var array = [];
                array.push(formData.headUser);
                formData.inputUserId = array;
            }
            if (formData.hasOwnProperty("checkUser") && !Array.isArray(formData.checkUser)) {
                var array = [];
                array.push(formData.checkUser);
                formData.inputUserId1 = array;
            }
        } else if (flag == 2) {
            var formData = $("#addMeetingTestFrom").serializeJson();
            formData.state = 0 ;
        }
        var tips = "确认保存？";
        layer.confirm(tips, {
            btn: ['确定', '取消'],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#saveBtn");
            $.ajax({
                type: "post",
                url: url,
                contentType: "application/json; charset=utf-8",
                data : JSON.stringify(formData),
                dataType: "json",
                async: true,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $(tableId).jqGrid('setGridParam', {
                            postData: {id:$("#meetId2").val()}, //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#addMeetingTest").modal('hide');
                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        swal(data.data.message);
                        $("#meetingTest").reloadCurrentData(baseUrl + "/meetingTask/meetingTask", $("#meetId").val(), "json", null, null);
                    } else if (data.code == 1002) {
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


//新增会议记录
function addRecord1(t, url) {
    if($("#recordType").val()==0){
        if(!existshVisible || Object.getOwnPropertyNames(existshVisible).length <= 0){
            layer.msg("设置会议可见人员！", {time: 1000, icon: 5});
            return;
        }
    }
    if (editToolObj.getContent()==""){
        layer.msg("请填写会议记录内容！", {time: 1000, icon: 5});
        return;
    }

    // if($(".newsContent").code()=="<p><br></p>"){
    //     layer.msg("请填写会议记录内容！", {time: 1000, icon: 5});
    //     return;
    // }
    if ($("#addFormData").valid()){
        var tips = "确认保存？";
        var formData = $("#addFormData").serializeJson();
        //var  formData =  new FormData($("#addFormData")[0]);
        if (formData.hasOwnProperty("inputVisibleUserId") && !Array.isArray(formData.inputVisibleUserId)){
            var array = [];
            array.push(formData.inputVisibleUserId);
            formData.inputVisibleUserId = array;
        }
        formData.meetId = $("#meetId1").val();
        formData.recordType = $("#recordType").val();
        formData.content = editToolObj.getContent();
        layer.confirm(tips,{
            btn:['确定','取消'],
            shade:false
        },function(index){
            layer.close(index);
            startModal("#saveBtn");
            $.ajax({
                type:"post",
                url : url,
                contentType: "application/json; charset=utf-8",
                data : JSON.stringify(formData),
                dataType:"json",
                async:true,
                cache:false,
                contentType:false,
                processData:false,
                success:function (data) {
                    Ladda.stopAll();
                    if(data.code==200){
                        layer.msg(data.data.message,{time:1000 ,icon :6});
                        $("#meetingTest").jqGrid('setGridParam', {
                            postData: $("#queryForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#addMeetingRecordModal").modal('hide');
                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                    if(data.code == 200){
                        swal(data.data.message);
                        $("#meetingTest").reloadCurrentData(baseUrl + "/meetingTask/meetingTaskListPg", $("#queryForm").serializeJson(), "json", null, null);
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
        },function () {
            return;
        });
    }
}

//新增会议
function addMeeting(t,url) {
    if ($("#startDate1").val()>$("#endDate1").val()){
        swal("会议结束时间要大于开始时间!");
        return;
    };
    if ($("#empName").val()== ""){
        swal("请选择会议纪要人!");
        return;
    };
    if($("#meetType").val()== ""){
        swal("请选择会议类型!");
        return;
    };

    if(!existsUserMap || Object.getOwnPropertyNames(existsUserMap).length <= 0){
        layer.msg("会议任务必须有主持人！", {time: 1000, icon: 5});
        return;
    }
    if(!existsUserMap1 || Object.getOwnPropertyNames(existsUserMap1).length <= 0){
        layer.msg("会议任务必须有组织人！", {time: 1000, icon: 5});
        return;
    }
    if(!existsUserMap2 || Object.getOwnPropertyNames(existsUserMap2).length <= 0){
        layer.msg("会议任务必须有参与人！", {time: 1000, icon: 5});
        return;
    }
    if ($("#addMeetingFrom").valid()){
        var  formData =  new FormData($("#addMeetingFrom")[0]);
        if (formData.hasOwnProperty("otherHost") && !Array.isArray(formData.otherHost)){
            var array = [];
            array.push(formData.otherHost);
            formData.otherHost = array;
        }
        if (formData.hasOwnProperty("otherOrganizer") && !Array.isArray(formData.otherOrganizer)){
            var array = [];
            array.push(formData.otherOrganizer);
            formData.otherOrganizer = array;
        }
        if (formData.hasOwnProperty("otherPark") && !Array.isArray(formData.otherPark)){
            var array = [];
            array.push(formData.otherPark);
            formData.otherPark = array;
        }
        if(formData.hasOwnProperty("inputUserId") && !Array.isArray(formData.inputUserId)){
            var array = [];
            array.push(formData.inputUserId);
            formData.inputUserId = array;
        }
        if(formData.hasOwnProperty("inputUserId1") && !Array.isArray(formData.inputUserId1)){
            var array = [];
            array.push(formData.inputUserId1);
            formData.inputUserId1 = array;
        }
        if(formData.hasOwnProperty("inputUserId2") && !Array.isArray(formData.inputUserId2)){
            var array = [];
            array.push(formData.inputUserId2);
            formData.inputUserId2 = array;
        }
        var tips = "确认保存？";
       layer.confirm(tips,{
           btn:['确定','取消'],
           shade:false
       },function(index){
           layer.close(index);
           startModal("#saveBtn");
           $.ajax({
               type:"post",
               url : url,
               data : formData,
               dataType:"json",
               async:true,
               cache:false,
               contentType:false,
               processData:false,
               success:function (data) {
                   Ladda.stopAll();
                   if(data.code==200){
                       layer.msg(data.data.message,{time:1000 ,icon :6});
                       $("#meetingTable").jqGrid('setGridParam', {
                           postData: $("#queryForm").serializeJson(), //发送数据
                       }).trigger("reloadGrid"); //重新载入
                       $("#addMeetingRoom").modal('hide');
                   }
               },
               error: function (data) {
                   Ladda.stopAll();
                   if(data.code == 200){
                       swal(data.data.message);
                       $("#meetingTable").reloadCurrentData(baseUrl + meetingUrl, $("#queryForm").serializeJson(), "json", null, null);
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
       },function () {
           return;
       });
    }
}
//弹出新增外部人员框
function addExternal(t) {
    $("#saveExternalModal").modal("toggle");
    $("#wordName").val("");
    $("#saveExternal").attr("data-divId",$(t).attr("data-divId"));
}

//人员显示遍历
function userWord(t,x) {
    var b;
    if (x!=null){
        if (x==0){
            b="showUserDiv"
        }else if(x==1){
            b="showUserDiv1"
        }else if (x==2){
            b="showUserDiv2"
        }
    }else {
         b = $("#saveExternal").attr("data-divId");
    }
        var html ="";
        var  $span ='<div data-add="1" data-name="'+t+'" data-divId="'+b+'" class="userDivClass">\n' +
            '<input name="inputUserName" type="hidden" value="'+t+'" >' +
            '<div data-name="'+t+'"  onclick="editBtn(this)" class="userClass">' +t+'</div>\n' +
            '<div title = "删除" data-name="'+t+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
            '<i class="fa fa-trash"></i>\n' +
            '</div>\n' +
            '</div> ';

        $("#saveExternalModal").modal("hide");
        if (b=="showUserDiv"){
            var otherHost = '<input  name="otherHost" type="hidden" value="' + t + '">';
            $span += otherHost;
            html +=$span;
            $("#showUserDiv").append(html);
        }else if(b=="showUserDiv1"){
            var otherOrganizer = '<input  name="otherOrganizer" type="hidden" value="' + t + '">';
            $span += otherOrganizer;
            html +=$span;
            $("#showUserDiv1").append(html);
        }else if(b=="showUserDiv2"){
            var otherPark = '<input  name="otherPark" type="hidden" value="' + t + '">';
            $span += otherPark;
            html +=$span;
            $("#showUserDiv2").append(html);
        }
}
//添加外部用户
function addUserword() {
    var b = $("#saveExternal").attr("data-divId");
    var tempWord = $.trim($("#wordName").val());
    if ($("#addExternalForm").valid()){
        layer.confirm('确认保存？',{
            btn:['确认','取消'],
            shade:false
        },function (index) {
            layer.close(index);
            if (checkUserWord(b,tempWord)){
                var html ="";
                var  $span ='<div data-add="1" data-name="'+tempWord+'" data-divId="'+b+'" class="userDivClass">\n' +
                    '<input name="inputUserName" type="hidden" value="'+tempWord+'" >' +
                    '<div data-name="'+tempWord+'"  onclick="editBtn(this)" class="userClass">' +tempWord+'</div>\n' +
                    '<div title = "删除" data-name="'+tempWord+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
                    '<i class="fa fa-trash"></i>\n' +
                    '</div>\n' +
                    '</div> ';


                $("#saveExternalModal").modal("hide");
                if (b=="showUserDiv"){
                    var otherHost = '<input  name="otherHost" type="hidden" value="' + tempWord + '">';
                    $span += otherHost;
                    html +=$span;
                    $("#showUserDiv").append(html);
                    showUserDivMap.push(tempWord);
                }else if(b=="showUserDiv1"){
                    var otherOrganizer = '<input  name="otherOrganizer" type="hidden" value="' + tempWord + '">';
                    $span += otherOrganizer;
                    html +=$span;
                    $("#showUserDiv1").append(html);
                    showUserDivMap1.push(tempWord);
                }else if(b=="showUserDiv2"){
                    var otherPark = '<input  name="otherPark" type="hidden" value="' + tempWord + '">';
                    $span += otherPark;
                    html +=$span;
                    $("#showUserDiv2").append(html);
                    showUserDivMap2.push(tempWord);
                }
            }else {
                swal("存在该用户，添加失败");
            }
            },function () {
            return;
            });
    }

}
//判断用户是否重复
function checkUserWord(b,tempWord) {
      var flag = false;
      var  userName = "";
           userName = $("#wordName").val();
           if (b=="showUserDiv"){
               if ($("#showUserDiv > div").length>0){
                   $("#showUserDiv > div").each(function (i,t) {
                       var word = $(t).attr("data-name");
                       if (userName == word){
                           flag = false ;
                       }else  {
                           flag = true;
                       }
                   });
               }else {
                   flag=true;
               }
           }

    if (b=="showUserDiv1"){
        if ($("#showUserDiv1 > div").length>0){
            $("#showUserDiv1 > div").each(function (i,t) {
                var word = $(t).attr("data-name");
                if (userName == word){
                    flag = false ;
                }else  {
                    flag = true;
                }
            });
        }else {
            flag=true;
        }
    }
    if (b=="showUserDiv2"){
        if ($("#showUserDiv2 > div").length>0){
            $("#showUserDiv2 > div").each(function (i,t) {
                var word = $(t).attr("data-name");
                if (userName == word){
                    flag = false ;
                }else  {
                    flag = true;
                }
            });
        }else {
            flag=true;
        }
    }

    return flag;
}

//编辑临时名
function  editBtn(t) {
    var keyId =$(t).attr("data-id");
    if(keyId != null && keyId!="" && keyId != undefined){
        $("#").val($(t).attr("data-name"));
        $("#").val($("#").val());
        $("#saveExternalModal").val();
        $("").modal("toggle");
    }else {
        swal("新增的关键字不能立即修改")
    }

}
//移除人员
function removeBtn(t) {
    var b = $(t).attr("data-divId")
    var userId = $(t).attr("userId");
    $("#userId"+userId).remove();
    $("#userName"+userId).remove();
    $(t).remove();
    if (b=="showUserDiv"){
        delete existsUserMap[userId];
    }else if(b=="showUserDiv1"){
        delete existsUserMap1[userId];
    }else if(b=="showUserDiv2"){
        delete existsUserMap2[userId];
    }else if (b=="visibleUser"){
        delete existshVisible[userId];
    }else if (b=="checkUser"){
        delete existshcheckUser[userId];
    }else if (b=="headUser"){
        delete existsheadUser[userId];
    }
}

//移除零时姓名
function removeBtn1(t) {
    layer.confirm('确认删除？', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        var $parentDiv = $(t).parent();
        $parentDiv.remove();
        if ($(t).attr("data-name")){
            checkName($parentDiv);
            $("input[value="+$(t).attr("data-name")+"]").remove();
        };
        layer.close(index);
    }, function () {
        return;
    });
}
//临时姓名去除
function checkName(t) {
    var checkName = $(t).attr("data-name");
    var b = $(t).attr("data-divId")
    if (b=="showUserDiv"){
        for (var i = 0; i < showUserDivMap.length; i++) {
            if (showUserDivMap[i] == checkName) {
                showUserDivMap.splice(i, 1);
            }
        }
    }else if(b=="showUserDiv1"){

        for (var i = 0; i < showUserDivMap1.length; i++) {
            if (showUserDivMap1[i] == checkName) {
                showUserDivMap1.splice(i, 1);
            }
        }
    }else if(b=="showUserDiv2"){
        for (var i = 0; i < showUserDivMap2.length; i++) {
            if (showUserDivMap2[i] == checkName) {
                showUserDivMap2.splice(i, 1);
            }
        }
    }
}
//会议任务增加
function addTask(id) {
    $("#addMeetingTest").modal({backdrop:"static"});
    existsheadUser = {};
    existshcheckUser = {};
    $("#headUser").empty();
    $("#checkUser").empty();
    $(".edit").hide();
    $("#meetId").val(id);
    document.getElementById("addMeetingTestFrom").reset();
    $("#addMeetingTestFrom").find("input").removeClass('error');
    $("#addMeetingTestFrom").validate().resetForm();
    meetType(id);

}

function meetType(id) {
    $.ajax({
        type:"post",
        url:baseUrl +"/meeting/editMeeting",
        data:{id:id},
        dataType:"json",
        success:function (data) {
            if (data.code==200) {
                if (data.data.entity) {
                    for ( var attr in  data.data.entity  ){
                        if(attr =="meetType"){
                            var mT;
                            switch (data.data.entity[attr]){
                                case 1:
                                    mT = '年会';
                                    break ;
                                case 2:
                                    mT = '年中大会';
                                    break ;
                                case 3:
                                    mT = '月总结大会';
                                    break ;
                                case 4:
                                    mT = '复盘会';
                                    break ;
                                case 5:
                                    mT = '业务交流会';
                                    break ;
                                case 6:
                                    mT = '部长会';
                                    break ;
                                case 7:
                                    mT = '部长月总结大会';
                                    break ;
                                case 8:
                                    mT = '部门会';
                                    break ;
                                case 9:
                                    mT = '述职会';
                                    break ;
                                case 10:
                                    mT = '股东会';
                                    break ;
                                case 11:
                                    mT = '董事会';
                                    break ;
                                case 12:
                                    mT = '战略研讨会';
                                    break ;
                                case 13:
                                    mT = '管理经营会';
                                    break ;
                                case 14:
                                    mT = '其他临时会';
                                    break ;
                            }
                           $("#meetType1").val(mT);

                        }
                        if (attr =="title"){
                            $("#title1").val(data.data.entity[attr]);
                        }
                    }

                }
            }
        }
    })
}
//会议任务查看
function viewtask(id,t) {
    $("#meetingTastModal").modal("toggle");
    $("#meetId2").val(id);
    meetingTask(id,t);
}

function editMeeting(id) {
    $("#addMeetingRoom").modal({backdrop:"static"});
    document.getElementById("addMeetingFrom").reset();
    existsUserMap = {}; //清空已选人员
    existsUserMap1 = {}; //清空已选人员
    existsUserMap2= {}; //清空已选人员
    showUserDivMap={};
    showUserDivMap1={};
    showUserDivMap2={};

    $("#showUserDiv1").empty();
    $("#showUserDiv2").empty();
    $("#showUserDiv").empty();

    $(".add").hide();
    $(".edit").show();
    $.ajax({
        type:"post",
        url: baseUrl + "/meeting/editMeeting",
        data:{id: id},
        dataType:"json",
        success:function (data) {
            if(data.code==200){
                for (var  attr in data.data.entity){
                    $("#addMeetingFrom [name=" + attr + "]").val(data.data.entity[attr]);
                  if (attr == "meetSummaryId"){
                        loadAllUserSearch3($("#empName"),data.data.entity[attr]);
                    }
                    if (attr == "attachmentName") {
                        $("#affixDiv").empty();
                        $("#affixDiv").show();
                        //后台返回去的数据转换成集合后，遍历显示
                        if (data.data.entity[attr]) {
                            if(data.data.entity[attr] === "") continue;
                            var pic = data.data.entity[attr].split(",");
                            var picLink = data.data.entity["attachmentLink"].split(",");
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
                                            html += "<img alt='" + picName + "' src='"+picPath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                        }else{
                                            if(fileExtArray.contains(fileExt)){
                                                html += "<a onclick=\"previewFile('"+picName+"','"+picPath.trim()+"',0)\" data-id='" + picPath + "'>预览:</a><br/>";
                                            }
                                        }
                                    }else {
                                        html += "<a onclick=\"previewFile('"+picName+"','"+picPath.trim()+"',0)\" data-id='" + picPath + "'>预览:</a><br/>";
                                    }
                                }
                                $("#affixDiv").append(html);
                            }
                        }
                    }
                }

            if (data.data.list && data.data.list.length>0){
                var btnList = "" ;
                $.each(data.data.list,function(i,keyword){
                    var keyId = keyword.id ;
                    var name = keyword.name;
                    var userType = keyword.userType;
                    var  $span ='<div data-add="1" data-name="'+name+'" userId="'+keyword.userId+'" deptId="'+keyword.deptId+'" onclick="removeBtn(this)" class="userDivClass">\n' +
                        '<input name="inputUserName" type="hidden" value="'+name+'" >' +
                        '<div data-name="'+name+'"   class="userClass">' +name+'</div>\n' +
                        '<div title = "删除" data-name="'+name+'"  class="deleteClass">\n' +
                         '<i class="fa fa-trash"></i>\n' +
                        '</div>\n' +
                        '</div> '
                    if (userType == 1 ){
                        existsUserMap1[keyword.userId] = {userName: name, deptId: keyword.deptId};
                        var inputUserId1 ='<input id="userId'+keyword.userId+'" name="inputUserId1" type="hidden" value="' + keyword.userId + '">';
                        btnList = '<input  type="hidden" value="' + name + '">';
                        btnList += inputUserId1;
                        btnList += $span;
                        $("#showUserDiv1").append(btnList);
                    }
                    if (userType == 2 ){
                        existsUserMap[keyword.userId] = {userName: name, deptId: keyword.deptId};
                        var inputUserId ='<input id="userId'+keyword.userId+'" name="inputUserId" type="hidden" value="' + keyword.userId + '">';
                        var btnList = '<input  type="hidden" value="' + name + '">';
                        btnList += $span;
                        btnList += inputUserId;

                        $("#showUserDiv").append(btnList);
                    }
                    if (userType== 0){
                        existsUserMap2[keyword.userId] = {userName: name, deptId: keyword.deptId};
                        var btnList = '<input type="hidden" value="' + name + '">';
                        var inputUserId2 ='<input id="userId'+keyword.userId+'" name="inputUserId2" type="hidden" value="' + keyword.userId + '">';
                        btnList += $span;
                        btnList += inputUserId2;
                        $("#showUserDiv2").append(btnList);
                    }
                });
            }
            if (data.data.map) {
                if (data.data.map.otherOrganizer) {
                    for (var i = 0; i < data.data.map.otherOrganizer.length; i++) {
                        var name = data.data.map.otherOrganizer[i];
                        var html = "";
                        var $span = '<div data-add="1" data-name="' + name + '" data-divId="showUserDiv1" class="userDivClass">\n' +
                            '<input name="inputUserName" type="hidden" value="' + name + '" >' +
                            '<div data-name="' + name + '"  class="userClass">' + name + '</div>\n' +
                            '<div title = "删除" data-name="'+name+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
                            '<i class="fa fa-trash"></i>\n' +
                            '</div>\n' +
                            '</div> ';
                        html += $span;
                        var otherOrganizer = '<input  name="otherOrganizer" type="hidden" value="' + name + '">';
                        html +=otherOrganizer ;
                        $("#showUserDiv1").append(html);
                    }
                }
                if (data.data.map.otherHost) {
                    for (var i = 0; i < data.data.map.otherHost.length; i++) {
                        var name = data.data.map.otherHost[i];
                        var html = "";
                        var $span = '<div data-add="1" data-name="' + name + '" data-divId="showUserDiv" class="userDivClass">\n' +
                            '<input name="inputUserName" type="hidden" value="' + name + '" >' +
                            '<div data-name="' + name + '"   class="userClass">' + name + '</div>\n' +
                            '<div title = "删除" data-name="'+name+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
                            '<i class="fa fa-trash"></i>\n' +
                            '</div>\n' +
                            '</div> ';
                        html += $span;
                        var otherHost = '<input  name="otherHost" type="hidden" value="' + name + '">';
                        html +=otherHost ;
                        $("#showUserDiv").append(html);
                    }
                }
                if (data.data.map.otherPark) {
                    for (var i = 0; i < data.data.map.otherPark.length; i++) {
                        var name = data.data.map.otherPark[i];
                        var html = "";
                        var $span = '<div data-add="1" data-name="' + name + '" data-divId="showUserDiv2" class="userDivClass">\n' +
                            '<input name="inputUserName" type="hidden" value="' + name + '" >' +
                            '<div data-name="' + name + '"  class="userClass">' + name + '</div>\n' +
                            '<div title = "删除" data-name="'+name+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
                            '<i class="fa fa-trash"></i>\n' +
                            '</div>\n' +
                            '</div> ';
                        html += $span;
                        var otherPark = '<input  name="otherPark" type="hidden" value="' + name + '">';
                        html +=otherPark ;
                        $("#showUserDiv2").append(html);
                    }
                }
            }
            }else if(data.code == 1002){
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
                $("#editModal").modal("hide");
            } else {
                if (getResCode(data))
                    return;
            }
        }

    })
}

function view (id){
    $("#viewMeetingRoom").modal({backdrop:"static"});
    document.getElementById("viewMeetingFrom").reset();
    $("#viewMeetingFrom").find("input").attr("disabled", "disabled");
    $("#viewMeetingFrom").find("select").attr("disabled", "disabled");
    $("#viewMeetingFrom").find("textarea").attr("disabled", "disabled");
    $("#viewMeetingFrom").validate().resetForm();
    // $(".add").hide();
    $.ajax({
        type:"post",
        url: baseUrl + "/meeting/editMeeting",
        data:{id: id},
        dataType:"json",
        success:function (data) {
            if (data.code==200){
                if (data.data.entity){
                    for (var attr in data.data.entity){
                        $("#viewMeetingFrom [name=" + attr + "]").val(data.data.entity[attr]);
                        if (attr == 'taskId'){
                            $("#taskId2").val(data.data.entity['taskId'])
                        }
                        if(attr =="meetType"){
                            var mT;
                            switch (data.data.entity[attr]){
                                case 1:
                                    mT = '年会';
                                    break ;
                                case 2:
                                    mT = '年中大会';
                                    break ;
                                case 3:
                                    mT = '月总结大会';
                                    break ;
                                case 4:
                                    mT = '复盘会';
                                    break ;
                                case 5:
                                    mT = '业务交流会';
                                    break ;
                                case 6:
                                    mT = '部长会';
                                    break ;
                                case 7:
                                    mT = '部长月总结大会';
                                    break ;
                                case 8:
                                    mT = '部门会';
                                    break ;
                                case 9:
                                    mT = '述职会';
                                    break ;
                                case 10:
                                    mT = '股东会';
                                    break ;
                                case 11:
                                    mT = '董事会';
                                    break ;
                                case 12:
                                    mT = '战略研讨会';
                                    break ;
                                case 13:
                                    mT = '管理经营会';
                                    break ;
                                case 14:
                                    mT = '其他临时会';
                                    break ;
                            }
                            $("#meetTypeView").val(mT);

                        }
                        if (attr == "attachmentName") {
                            $("#affixDivView").empty();
                            $("#affixDivView").show();
                            //后台返回去的数据转换成集合后，遍历显示
                            if (data.data.entity[attr]) {
                                if(data.data.entity[attr] === "") continue;
                                var pic = data.data.entity[attr].split(",");
                                var picLink = data.data.entity["attachmentLink"].split(",");
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
                                                html += "<img alt='" + picName + "' src='"+picPath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                            }else{
                                                if(fileExtArray.contains(fileExt)){
                                                    html += "<a onclick=\"previewFile('"+picName+"','"+picPath.trim()+"',0)\" data-id='" + picPath + "'>预览:</a><br/>";
                                                }
                                            }
                                        }else {
                                            tml += "<a onclick=\"previewFile('"+picName+"','"+picPath.trim()+"',0)\" data-id='" + picPath + "'>预览:</a><br/>";
                                        }
                                    }
                                    $("#affixDivView").append(html);
                                }
                            }
                        }
                    }

                }
                if (data.data.list && data.data.list.length>0){
                    var btnList = "" ;
                    $.each(data.data.list,function(i,keyword){
                        var keyId = keyword.id ;
                        var name = keyword.name;
                        var userType = keyword.userType;
                        var  $span ='<div data-add="1" data-name="'+name+'" data-divId="'+userType+'" class="userDivClass">\n' +
                            '<input name="inputUserName" type="hidden" value="'+name+'" >' +
                            '<div data-name="'+name+'"  onclick="editBtn(this)" class="userClass">' +name+'</div>\n' +
                            // '<div title = "删除" data-name="'+name+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
                          //  '<i class="fa fa-trash"></i>\n' +
                            '</div>\n' +
                            '</div> '
                        if (userType == 1 ){
                            btnList = '<input  type="hidden" value="' + name + '">';
                            btnList += $span;
                            $("#showUserDivView").append(btnList);
                        }
                        if (userType == 2 ){
                            var btnList = '<input  type="hidden" value="' + name + '">';
                            btnList += $span;
                            $("#showUserDiv1View").append(btnList);
                        }
                        if (userType== 0){
                            var btnList = '<input type="hidden" value="' + name + '">';
                            btnList += $span;
                            $("#showUserDiv2View").append(btnList);
                        }
                    });
                }
                if (data.data.map){
                    if (data.data.map.otherOrganizer ){
                        for(var i =0 ; i<data.data.map.otherOrganizer.length ;i++){
                            var name =data.data.map.otherOrganizer[i] ;
                            var html ="";
                            var  $span ='<div data-add="1" data-name="'+name+'" class="userDivClass">\n' +
                                '<input name="inputUserName" type="hidden" value="'+name+'" >' +
                                '<div data-name="'+name+'"  class="userClass">' +name+'</div>\n' +
                                // '<div title = "删除" data-name="'+name+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
                               //// '<i class="fa fa-trash"></i>\n' +
                                '</div>\n' +
                                '</div> ';
                            html += $span;
                            $("#showUserDiv1View").append(html);
                        }
                    }
                    if (data.data.map.otherHost ){
                        for(var i =0 ; i<data.data.map.otherHost.length ;i++){
                            var name =data.data.map.otherHost[i] ;
                            var html ="";
                            var  $span ='<div data-add="1" data-name="'+name+'" class="userDivClass">\n' +
                                '<input name="inputUserName" type="hidden" value="'+name+'" >' +
                                '<div data-name="'+name+'"   class="userClass">' +name+'</div>\n' +
                                // '<div title = "删除" data-name="'+name+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
                               // '<i class="fa fa-trash"></i>\n' +
                                '</div>\n' +
                                '</div> ';
                            html += $span;
                            $("#showUserDivView").append(html);
                        }
                    }
                    if (data.data.map.otherPark ){
                        for(var i =0 ; i<data.data.map.otherPark.length ;i++){
                            var name =data.data.map.otherPark[i] ;
                            var html ="";
                            var  $span ='<div data-add="1" data-name="'+name+'" class="userDivClass">\n' +
                                '<input name="inputUserName" type="hidden" value="'+name+'" >' +
                                '<div data-name="'+name+'"  class="userClass">' +name+'</div>\n' +
                                // '<div title = "删除" data-name="'+name+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
                                // '<i class="fa fa-trash"></i>\n' +
                                '</div>\n' +
                                '</div> ';
                            html += $span;
                            $("#showUserDiv2View").append(html);
                        }
                    }
                }

            }else if(data.code == 1002){
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
                $("#editModal").modal("hide");
            } else {
                if (getResCode(data))
                    return;
            }
        }

    });
};

//添加会议记录
function addRecord(meetId,t){
    meetType(meetId);
    $("#visibleUser").empty();
    $("#addMeetingRecordModal").modal({backdrop:"static"});
   // $(".newsContent").code("<p><br></p>");
    editToolObj.setContent("");
     existshVisible ={};
    if (t==1){
        $(".add").hide();
        $(".minutes").show();
        $(".record").hide();
    }else {
        $(".add").show();
        $(".minutes").hide();
        $(".record").show();
    }
    $("#meetId1").val(meetId)
    //类型、0记录、1纪要
    $("#recordType").val(t);
}
//会议记录查看
function viewRecord(meetId,t){
    $("#meetingRecordModal").modal({backdrop:"static"});
    if (t==1){
        $(".minutes").show();
        $(".record").hide();
    }else {
        $(".minutes").hide();
        $(".record").show();
    }
    $("#recordType1").val(t);
    viewMeetingRecordListPg(meetId,t)
}
//关联会议的查询
function meetingorganization(t) {
        $.get(baseUrl+"/meeting/meetingorganization",function (data) {
            $(t).append("<option value=''>请选择</option>");
            $(data).each(function (i,d) {
                // var value = $(t).attr("data-value");
                // var selected = value == d.id ? "selected=selected":"";
                $(t).append("<option value='" + d.id + "'>" + d.title + "</option>");
            });
        },"json");
}

//會議用户是否同意
function meetingFlag(id,t) {
    var result = false;
    $.ajax({
        type:"post",
        url:baseUrl + "/meeting/meetingFlag",
        data:{id:id , flag :t},
        dataType:"json",
        async:false,
        success: function (data) {
            if (data.data.entity){
                result = true;
            }else {
                result = false;
            }
        }
    });
    return result;
}
//会议延期
function delay(id,endTime,buttonId) {
    $("#meetingDelayModal").modal("toggle");
    $("#endDelayDate1").val("");
    $("#oldEndTime").val(endTime);
    $("#taskId1").val(id);
    $("#buttonId").val(buttonId)
}
//会议任务日期更改保存
function baocData(t,state,tableId) {
    var formaData;
    //延期
    if (state == 1){
        $("#state").val(state);
        if($("#endDelayDate1").val() < $("#oldEndTime").val()){
            swal("延期时间要大于结束时间，请重新选择!");
            return;
        }
         formaData =$("#meetingdelayFrom").serializeJson();
    }
    //取消
   else {
        formaData = {id: t, state: state};
    }
    layer.confirm('确定保存？',{
        btn:['确认',"取消"],
        shade:false
    },function (index) {
        layer.close(index);
        $.ajax({
            type:"post",
            url:baseUrl +"/meetingRecordViewUser/meetingDelay",
            contentType: "application/json; charset=utf-8",
            data:JSON.stringify(formaData),
            dataType:"json",
            success:function (data) {
                Ladda.stopAll();
                if (data.code == 200 ){
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    $(tableId).jqGrid('setGridParam', {
                        postData:{id:$("#meetId2").val()}, //发送数据
                    }).trigger("reloadGrid"); //重新载入
                    $("#meetingDelayModal").modal('hide');
                }
            },
            error:function (data) {
                Ladda.stopAll();
                if (getResCode(data))
                    return;
            }
        });
    }, function () {
        return;
    });
}

function editMeetTask(id,meetId,tableId) {
    $(".add").hide();
    $(".edit").show();
    $("#checkUser").empty();
    $("#headUser").empty();
    $("#addMeetingTest").modal("toggle");
    $("#tableId").val(tableId)
    meetType(meetId);
    $.ajax({
        type:"post",
        url:baseUrl +"/meetingTask/getMeetingTask",
        data:{id:id},
        dataType:"json",
        success:function (data) {
            if (data.code== 200){
                if (data.data.entity){
                    for (var attr in data.data.entity){
                        $("#addMeetingTestFrom [name=" + attr + "]").val(data.data.entity[attr]);
                    }
                }
                if (data.data.list && data.data.list.length>0){
                    var btnList = "" ;
                    $.each(data.data.list,function(i,keyword){
                        var keyId = keyword.userId ;
                        var name = keyword.name;
                        var userType ;
                        if (keyword.userType ==3){
                            userType ="headUser";
                        }else {
                            userType ="checkUser";
                        }
                        var  $span ='<div data-add="1" data-name="'+name+'" data-divId="'+userType+'" class="userDivClass">\n' +
                            '<input name="inputUserName" type="hidden" value="'+name+'" >' +
                            '<div data-name="'+name+'"  class="userClass">' +name+'</div>\n' +
                            // '<div title = "删除" data-name="'+name+'" onclick="removeBtn1(this)" class="deleteClass">\n' +
                            //  '<i class="fa fa-trash"></i>\n' +
                            // '</div>\n' +
                            '</div> '
                        if (userType == "headUser" ){
                            btnList = '<input  type="hidden" value="' + name + '">';
                            btnList += $span;
                            $("#headUser").append(btnList);
                        }
                        if (userType == "checkUser" ){
                            var btnList = '<input  type="hidden" value="' + name + '">';
                            btnList += $span;
                            $("#checkUser").append(btnList);
                        }
                    });
                }
            }
        }
    })
}

function  delMeeting(id) {
    var lock = true ;
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/meeting/delMeeting",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        $("#meetingTable").emptyGridParam();
                        $("#meetingTable").reloadCurrentData(baseUrl + meetingUrl, $("#queryForm").serializeJson(), "json", null, null);
                        swal(data.data.message);
                    } else if(data.code == 1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            });
        }
    }, function () {
        return;
    });
}

var editToolObj = new KindeditorTool({
    targetEl: "#editTool",
    uploadUrl: "/editUpload?filePart=kindEditor"
});
//得到查询参数
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]);
    return null;
}