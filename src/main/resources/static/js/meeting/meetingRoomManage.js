var addImageUpload, editImageUpload;
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#meetingRoomTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });

    //新增会议室图片上传
    addImageUpload = new FileUpload({
        targetEl: '#addImageUploadForm',
        multi: false,
        filePart: "meeting",
        completeCallback: function (data) {
            if (data.length > 0) {
                var filePath = data[0].file;
                $("#addPicPath").val(filePath);
                var $picPathPreview = $("#picPathPreview");
                $picPathPreview.attr('src',  filePath);
            }
        },
        acceptSuffix: ['jpg', 'png']
    });
    //编辑会议室图片上传
    editImageUpload = new FileUpload({
        targetEl: '#editImageUploadForm',
        multi: false,
        filePart: "meeting",
        completeCallback: function (data) {
            if (data.length > 0) {
                var filePath = data[0].file;
                $("#editPicPath").val(filePath);
                var $picPathPreview = $("#editPicPathPreview");
                $picPathPreview.attr('src',  filePath);
            }
        },
        acceptSuffix: ['jpg', 'png']
    });

    commonObj.initPage(); //初始化界面
    commonObj.userSaveClickInit();//选择人员弹窗，人员保存按钮事件
});

//添加会议记录时初始化kindeditor编辑器
var editToolObj = new KindeditorTool({
    targetEl: "#editTool",
    uploadUrl: "/editUpload?filePart=news",
    // filterItems:["code","cut","copy","paste","anchor","about","fullscreen"],
    items:[
        'undo', 'redo', '|', 'preview', 'print', 'template',
        'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
        'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
        'superscript', 'clearhtml', 'quickformat', 'selectall', '|','/',
        'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
        'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image', 'multiimage',
        'flash', 'media', 'insertfile', 'table', 'hr', 'emoticons', 'baidumap', 'pagebreak',
        'link', 'unlink']
});

//添加会议记录时初始化kindeditor编辑器
var viewToolObj = new KindeditorTool({
    targetEl: "#viewTool",
    uploadUrl: "/editUpload?filePart=news",
    // filterItems:["code","cut","copy","paste","anchor","about","fullscreen"],
    items:[]
});

//多个页面使用的方法或者数据
var commonObj = {
    //缓存当前所有用户
    allUser: [],
    allXZUser: [],
    meetingRoomList: [],
    //数据请求前，替换字符串中的<>符号成为&lt;&gt;
    replaceParam: function (param) {
        if(param && Object.getOwnPropertyNames(param).length > 0){
            for(var key in param){
                if(typeof param[key] && param[key].constructor == String){ //如果数据是字符串，则进行替换
                    param[key] = param[key].replace(/</g, '&lt;').replace(/>/g, '&gt;');
                }
            }
        }
        return param;
    },
    //后台请求方法
    requestData: function (data, url, requestType,dataType,async,callBackFun, contentType) {
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
    },
    //Tab切换处理事件
    tabChange: function (index) {
        $(".tabContent").css("display","none");
        applyMeetingRoomObj.clearBubble();//切换Tab气泡弹窗隐藏

        if(index == 0){
            $("#applyMeetingRoomTab").css("display","block");
            applyMeetingRoomObj.initPagerPlugin(); //初始化预约会议室
        }else if(index == 1){
            $("#myApplyMeetingRoomTab").css("display","block");
            myApplyMeetingRoomObj.initPagerPlugin();//初始化我的预约
        }else if(index == 2){
            $("#hasApplyMeetingRoomTab").css("display","block");
            meetingRoomHasApplyObj.initPagerPlugin(); //初始化会议室管理
        }else {
            $("#meetingRoomManageTab").css("display","block");
            meetingRoomManageObj.initPagerPlugin(); //初始化会议室管理
        }
    },
    //分页插件使用
    pagerPlus: function (config,callback,type) {
        layui.use('laypage', function(){
            var laypage = layui.laypage;

            //执行一个laypage实例
            laypage.render({
                elem: config.elem //注意，这里的 test1 是 ID，不用加 # 号
                ,count: config.count || 0, //数据总数，从服务端得到
                layout: ['count','prev','page','next','refresh','limit','skip'],
                hash: true,
                limits: config.limits || [10, 20, 50, 100],
                limit: config.limit || 10,
                jump: function (obj, first) {
                    if(config.bubbleType && config.bubbleType == 'bubble'){
                        applyMeetingRoomObj.clearBubble();//切换Tab气泡弹窗隐藏
                    }
                    config.param = config.param || {};
                    config.param.size = obj.limit;
                    config.param.page = obj.curr;
                    commonObj.requestData(config.param, config.url, "post", "json", true, function (data) {
                        if(callback){
                            if(config.target){
                                callback(data,config.target, (config.param.size * (config.param.page - 1)));
                            }else {
                                callback(data,type, (config.param.size * (config.param.page - 1)));
                            }
                        }
                    });
                }
            });
        });
    },
    //用户权限
    getMeetingRoomSettingAuthUrl: "/meetingRoom/getMeetingRoomSettingAuth",
    permissAuth:function () {
        var flag = false;
        commonObj.requestData(null, commonObj.getMeetingRoomSettingAuthUrl, "get", "json", false, function (data) {
            if(data.code == 200){
                flag = true;
            }
        });
        return flag;
    },
    initPage: function () {
        commonObj.listUser();

        //渲染下拉列表-会议室列表
        if(!commonObj.meetingRoomList || commonObj.meetingRoomList.length < 1){
            commonObj.requestData(null, "/meetingRoom/listMeetingRoom", 'get', 'json',false,function (data) {
                if(data && data.length > 0){
                    $(data).each(function (i, meettingRoom) {
                        commonObj.meetingRoomList.push(meettingRoom);
                    });
                }
            });
        }
        var meetingRoomHtml = "<option value=\"\">请选择会议室</option>";
        if(commonObj.meetingRoomList && commonObj.meetingRoomList.length > 0){
            $.each(commonObj.meetingRoomList, function (i, meetingRoom) {
                meetingRoomHtml += "<option value=\""+meetingRoom.id+"\">"+meetingRoom.name+"</option>";
            });
        }
        $("#meetingRoomHasApplyForm").find("select[name='roomId']").html(meetingRoomHtml);
        //渲染下拉列表-预约人
        var userHtml = "<option value=\"\">请选择预约人</option>";
        if(commonObj.allUser && commonObj.allUser.length > 0){
            $.each(commonObj.allUser, function (i, u) {
                userHtml += "<option value=\""+u.id+"\">"+u.name+"</option>";
            });
        }
        $("#meetingRoomHasApplyForm").find("select[name='userId']").html(userHtml);

        //如果是管理员
        if(commonObj.permissAuth()){
            //设置会议室权限
            $("#settingRoomTab").css("display", "inline-block");
            $("#meetingRoomManageTab").css("display", "inline-block");
            //设置审核人
            commonObj.listXZUser();
            commonObj.renderUserSelect("#addAuditUserId", commonObj.allXZUser, "请选择会议审核人员");
            commonObj.renderUserSelect("#editAuditUserId", commonObj.allXZUser, "请选择会议审核人员")
        }else {
            //设置会议室权限
            $("#settingRoomTab").css("display", "none");
            $("#meetingRoomManageTab").css("display", "none");
        }
        //设置会议纪要人员
        commonObj.renderUserSelect("#meetSummaryId", commonObj.allUser, "请选择会议纪要人员");

        commonObj.tabChange(0);

        //模态框关闭前调用的事件，可当做钩子函数
        $('#evaluationModal').on('hidden.bs.modal', function () {
            //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
            // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
            $("body").addClass("modal-open"); //解决选择部门后，调岗弹窗不能滚动
        });

        //富文本编辑器
        // $('.newsContent').summernote({
        //     lang: 'zh-CN',
        //     height: 300
        // });
    },
    //人员下拉列表
    renderUserSelect: function (selectId, data, placeholder) {
        if(data && data.length > 0){
            var html = " <option value=\"\" selected>"+placeholder+"</option>";
            $(data).each(function (i, d) {
                html += "<option value='" + d.id + "'>" + d.name + "</option>";
            });
            $(selectId).html(html);

            layui.use(["form"], function () {
                layui.form.render('select');
            });
        }
    },
    listUserUrl: "/user/listUser",
    listXZUserUrl: "/user/listPartAll3/XZ",
    listUser: function () {
        if(!(commonObj.allUser && commonObj.allUser.length > 0)){
            commonObj.requestData(null, commonObj.listUserUrl, 'get', 'json',false,function (data) {
                if(data && data.length > 0){
                    $(data).each(function (i, d) {
                        commonObj.allUser.push(d);
                    });
                }
            });
        }
    },
    listXZUser: function () {
        if(!(commonObj.allXZUser && commonObj.allXZUser.length > 0)){
            commonObj.requestData(null, commonObj.listXZUserUrl, 'get', 'json',false,function (data) {
                if(data && data.length > 0){
                    commonObj.allXZUser = []; //清空重新设置
                    $(data).each(function (i, d) {
                        commonObj.allXZUser.push(d);
                    });
                }
            });
        }
    },
    chooseTimeRange: {}, //预约时间范围，格式：{roomApplyId: {seq1: 1, seq2: 2, startTime: 12:00, endTime: 12:30}}
    //计算时间段选择范围
    calculateRange: function (json, id, seq, ulId, liPreId) {
        if(json[id]){ //如果有值，判断有哪些值
            var seq1 = json[id].seq1;
            var seq2 = json[id].seq2;
            if(seq1 && seq2){ //都有值，则判断大小
                if(seq > seq2){
                    json[id].seq2 = seq;
                }else if(seq < seq1){
                    json[id].seq1 = seq;
                    // json[id].seq2 = seq1;
                }else {
                    if(json[id].seq1 == seq){
                        json[id].seq2 = seq;
                    }else{
                        json[id].seq1 = seq;
                    }
                }
            }else{ //判断给谁赋值
                if(seq1 && !seq2){
                    if(seq1 > seq){
                        json[id].seq1 = seq;
                        json[id].seq2 = seq1;
                    }else{
                        json[id].seq2 = seq;
                    }
                }else if(seq2 && !seq1){
                    if(seq > seq2){
                        json[id].seq1 = seq2;
                        json[id].seq2 = seq;
                    }else{
                        json[id].seq2 = seq;
                    }
                }else {
                    json[id].seq1 = seq;
                }
            }
        }else { //没有值则一定是开始值
            json[id] = {seq1: seq};
        }
        //渲染当前节点
        var seq11 = json[id].seq1;
        var seq21 = json[id].seq2;
        $("#"+ulId).find("li").removeClass("chooseTimeBg"); //移除选中效果
        $("#"+ulId).find("li").removeClass("edgeTimeBg"); //移除选中效果
        if(seq11 && seq21){
            for (var i = seq11; i <= seq21; i++){
                if(seq11 == i || seq21 == i){
                    $("#"+liPreId+id+"_"+i).addClass("chooseTimeBg edgeTimeBg");
                }else {
                    $("#"+liPreId+id+"_"+i).addClass("chooseTimeBg");
                }
            }
        }else {
            if(seq11){
                $("#"+liPreId+id+"_"+seq11).addClass("edgeTimeBg");
            }
            if(seq21){
                $("#"+liPreId+id+"_"+seq21).addClass("edgeTimeBg");
            }
        }
    },
    //添加会议页面，预约时间段
    addMeetingPageTimeSlot: function (data) {
        var html = "";
        if(data && data.meetingRoom && data.meetingRoom.timeSlotList && data.meetingRoom.timeSlotList.length > 0){
            var timeSlotList = data.meetingRoom.timeSlotList;
            var width = 100 / timeSlotList.length;
            $.each(timeSlotList,function (i,timeSlot) {
                var cls = timeSlot.passTimeFlag == "1" ? "passTimeBg" : "";
                var chooseFlag = timeSlot.passTimeFlag == "1" ? "1" : "0"; //1-不可选、0-可选
                if(timeSlot.wholeTimeFlag == "1") {
                    html += "<li onclick='commonObj.timeSlotClickFun(this);' data-html='true' data-time='"+data.startTime+"' data-seq='"+i+"' id='apply_"+data.id+"_"+i+"' data-roomApplyId='"+data.id+"'  data-flag='"+chooseFlag+"' data-startTime='"+timeSlot.startTime+"' data-endTime='"+timeSlot.endTime+"' class='"+cls+"' style='width: "+width+"%;'><span class='timeSpanLeft'>"+timeSlot.hour+"</span></li>";
                }else if(timeSlot.wholeTimeFlag == "2"){
                    html += "<li onclick='commonObj.timeSlotClickFun(this);' data-html='true' data-time='"+data.startTime+"' data-seq='"+i+"' id='apply_"+data.id+"_"+i+"' data-roomApplyId='"+data.id+"'  data-flag='"+chooseFlag+"' data-startTime='"+timeSlot.startTime+"' data-endTime='"+timeSlot.endTime+"' class='"+cls+"' style='width: "+width+"%;text-align: right;'><span class='timeSpanRight'>"+timeSlot.hour1+"</span></li>";
                }else if(timeSlot.wholeTimeFlag == "3"){
                    html += "<li onclick='commonObj.timeSlotClickFun(this);' data-html='true' data-time='"+data.startTime+"' data-seq='"+i+"' id='apply_"+data.id+"_"+i+"' data-roomApplyId='"+data.id+"'  data-flag='"+chooseFlag+"' data-startTime='"+timeSlot.startTime+"' data-endTime='"+timeSlot.endTime+"' class='"+cls+"' style='width: "+width+"%;text-align: right;'><span class='timeSpanLeft'>"+timeSlot.hour+"</span><span class='timeSpanRight'>"+timeSlot.hour1+"</span></li>";
                }else{
                    html += "<li onclick='commonObj.timeSlotClickFun(this);' data-html='true' data-time='"+data.startTime+"' data-seq='"+i+"' id='apply_"+data.id+"_"+i+"' data-roomApplyId='"+data.id+"'  data-flag='"+chooseFlag+"' data-startTime='"+timeSlot.startTime+"' data-endTime='"+timeSlot.endTime+"' class='"+cls+"' style='width: "+width+"%;'></li>";
                }
            });
            $("#addMeetingTimeSlot").html(html);
        }
    },
    //时间段点击事件
    timeSlotClickFun: function (t) {
        var dateTime = $(t).attr("data-time"); //获取预约的开始时间
        var seq = $(t).attr("data-seq"); //获取时间段序号，序号越小越靠前
        var roomApplyId = $(t).attr("data-roomApplyId"); //当前预约ID
        var chooseFlag = $(t).attr("data-flag"); //时间段是否可选：1-不可选、0-可选
        if(chooseFlag && chooseFlag == 1){
            layer.msg("当前时间已过，不能再进行选择！");
            return;
        }
        commonObj.calculateRange(commonObj.chooseTimeRange, roomApplyId, seq,"addMeetingTimeSlot","apply_"); //计算出时间范围
        //设置时间区间
        if(commonObj.chooseTimeRange[roomApplyId]){
            var seqStart = commonObj.chooseTimeRange[roomApplyId].seq1;
            var seqEnd = commonObj.chooseTimeRange[roomApplyId].seq2;
            if(seqStart && seqEnd){
                commonObj.chooseTimeRange[roomApplyId].startTime = $("#apply_"+roomApplyId+"_"+seqStart).attr("data-startTime");
                commonObj.chooseTimeRange[roomApplyId].endTime = $("#apply_"+roomApplyId+"_"+seqEnd).attr("data-endTime");
            }else if(seqStart){
                commonObj.chooseTimeRange[roomApplyId].startTime = $("#apply_"+roomApplyId+"_"+seqStart).attr("data-startTime");
                commonObj.chooseTimeRange[roomApplyId].endTime = $("#apply_"+roomApplyId+"_"+seqStart).attr("data-endTime");
            }else {
                commonObj.chooseTimeRange[roomApplyId].startTime = $("#apply_"+roomApplyId+"_"+seqEnd).attr("data-startTime");
                commonObj.chooseTimeRange[roomApplyId].endTime = $("#apply_"+roomApplyId+"_"+seqEnd).attr("data-endTime");
            }
            var date = new Date(dateTime).format('yyyy-MM-dd');
            var startTime = commonObj.chooseTimeRange[roomApplyId].startTime;
            var endTime = commonObj.chooseTimeRange[roomApplyId].endTime;
            var content = date + " " + startTime + " ~ "+endTime;
            $("#meetingStartTime").val(date + " " + startTime + ":00");
            $("#meetingEndTime").val(date + " " + endTime + ":00");
            $("#showTime").text(content);
        }

    },
    //渲染公司人员模态框
    existOrganizerUserMap: {},
    existOrganizerUserList: [],
    existHostUserMap: {},
    existHostUserList: [],
    existParkUserMap: {},
    existParkUserList: [],
    existVisitUserMap: {},
    existHeadUserMap: {},
    existCheckUserMap: {},
    renderUserPage: function (tt) {
        var divId = $(tt).attr("data-divId");
        $("#submitEvaluationPeople").attr("data-divId", divId);
        var rootDom = $("#evaluationModal");

        var listUser = "/userGroup/listBusinessPart?name=" + $("#nameQc").val().trim();
        if (divId == "visibleUser"){ //如果是可见人员，则仅从会议人员中选择
            var param = {id:$("#meetId1").val(), name: $("#nameQc").val().trim()}
            listUser ="/meeting/meetUsers?"+$.param(param);
        }
        rootDom.find("div[data-id='groups']").html(""); //清空上一次纪录
        rootDom.find('input[data-id="all"]').iCheck('uncheck');
        commonObj.requestData(null, listUser, "get", "json", true, function (data) {
            var userList = commonObj.groupBy(data, function (item) {
                return [item.deptId];
            });
            var html = template("excludePeopleHtml", {'data': userList}); //将用户数据渲染html
            rootDom.find("div[data-id='groups']").html(html);
            commonObj.reloadICheck(rootDom); //重新加载i-checks

            //部门复选框点击事件
            rootDom.find(".deptSpan").on('ifChecked', function () {
                var userInputArr = $(this).parent().parent().parent().next().find("input[type='checkbox']");
                if(userInputArr && userInputArr.length > 0){
                    $.each(userInputArr, function (index,t) {
                        $(t) .iCheck('check');
                        if(divId == "organizerUser"){
                            commonObj.existOrganizerUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if(divId == "hostUser"){
                            commonObj.existHostUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if (divId == "parkUser"){
                            commonObj.existParkUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if(divId == "visibleUser"){
                            commonObj.existVisitUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if(divId == "headUser"){
                            commonObj.existHeadUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                        }else if(divId == "checkUser"){
                            commonObj.existCheckUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
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
                        if(divId == "organizerUser"){
                            delete commonObj.existOrganizerUserMap[$(t).attr('userId')];
                        }else if(divId == "hostUser"){
                            delete commonObj.existHostUserMap[$(t).attr('userId')];
                        }else if (divId == "parkUser"){
                            delete commonObj.existParkUserMap[$(t).attr('userId')];
                        }else if(divId == "visibleUser"){
                            delete commonObj.existVisitUserMap[$(t).attr('userId')];
                        }else if(divId == "headUser"){
                            delete commonObj.existHeadUserMap[$(t).attr('userId')];
                        }else if(divId == "checkUser"){
                            delete commonObj.existCheckUserMap[$(t).attr('userId')];
                        }
                    });
                }
            });
            //全选按钮点击事件
            rootDom.find('input[data-id="all"]').on('ifChecked', function () {
                var userInputArr = rootDom.find("div[data-id='groups']").find(".i-checks");
                if(userInputArr && userInputArr.length > 0){
                    $.each(userInputArr, function (index,t) {
                        $(t) .iCheck('check');
                        if($(t).attr('userId')){
                            if(divId == "organizerUser"){
                                commonObj.existOrganizerUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                            }else if(divId == "hostUser"){
                                commonObj.existHostUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                            }else if (divId == "parkUser"){
                                commonObj.existParkUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                            }else if(divId == "visibleUser"){
                                commonObj.existVisitUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                            }else if(divId == "headUser"){
                                commonObj.existHeadUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                            }else if(divId == "checkUser"){
                                commonObj.existCheckUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                            }
                        }
                    });
                }
            });
            //全选按钮取消点击事件
            rootDom.find('input[data-id="all"]').on('ifUnchecked', function () {
                var userInputArr = rootDom.find("div[data-id='groups']").find(".i-checks");
                if(userInputArr && userInputArr.length > 0){
                    $.each(userInputArr, function (index,t) {
                        $(t) .iCheck('uncheck');
                        if(divId == "organizerUser"){
                            commonObj.existOrganizerUserMap = {};
                        }else if(divId == "hostUser"){
                            commonObj.existHostUserMap = {};
                        }else if (divId == "parkUser"){
                            commonObj.existParkUserMap = {};
                        }else if(divId == "visibleUser"){
                            commonObj.existVisitUserMap = {};
                        }else if(divId == "headUser"){
                            commonObj.existHeadUserMap = {};
                        }else if(divId == "checkUser"){
                            commonObj.existCheckUserMap = {};
                        }
                    });
                }
            });
            //用户复选框点击事件
            $(".userSpan").on('ifClicked', function () {
                var flag = $(this).is(':checked');
                if(flag){  //如果是选中状态，则删除用户
                    if(divId == "organizerUser"){
                        delete commonObj.existOrganizerUserMap[$(this).attr('userId')];
                    }else if(divId == "hostUser"){
                        delete commonObj.existHostUserMap[$(this).attr('userId')];
                    }else if (divId == "parkUser"){
                        delete commonObj.existParkUserMap[$(this).attr('userId')];
                    }else if(divId == "visibleUser"){
                        delete commonObj.existVisitUserMap[$(this).attr('userId')];
                    }else if(divId == "headUser"){
                        delete commonObj.existHeadUserMap[$(this).attr('userId')];
                    }else if(divId == "checkUser"){
                        delete commonObj.existCheckUserMap[$(this).attr('userId')];
                    }
                }else{
                    if(divId == "organizerUser"){
                        commonObj.existOrganizerUserMap[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                    }else if(divId == "hostUser"){
                        commonObj.existHostUserMap[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                    }else if (divId == "parkUser"){
                        commonObj.existParkUserMap[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                    }else if(divId == "visibleUser"){
                        commonObj.existVisitUserMap[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                    }else if(divId == "headUser"){
                        commonObj.existHeadUserMap[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                    }else if(divId == "checkUser"){
                        commonObj.existCheckUserMap[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
                    }
                }
            });

            //判断当前已选择页面
            if (divId == "organizerUser"){
                if(commonObj.existOrganizerUserMap && Object.getOwnPropertyNames(commonObj.existOrganizerUserMap).length > 0){
                    for(var key in commonObj.existOrganizerUserMap){
                        $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
                    }
                }
            }else if(divId == "hostUser"){
                if(commonObj.existHostUserMap && Object.getOwnPropertyNames(commonObj.existHostUserMap).length > 0){
                    for(var key in commonObj.existHostUserMap){
                        $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
                    }
                }
            }else if(divId == "parkUser"){
                if(commonObj.existParkUserMap && Object.getOwnPropertyNames(commonObj.existParkUserMap).length > 0){
                    for(var key in commonObj.existParkUserMap){
                        $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
                    }
                }
            }else if(divId == "visibleUser"){
                if(commonObj.existVisitUserMap && Object.getOwnPropertyNames(commonObj.existVisitUserMap).length > 0){
                    for(var key in commonObj.existVisitUserMap){
                        $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
                    }
                }
            }else if(divId == "headUser"){
                if(commonObj.existHeadUserMap && Object.getOwnPropertyNames(commonObj.existHeadUserMap).length > 0){
                    for(var key in commonObj.existHeadUserMap){
                        $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
                    }
                }
            }else if(divId == "checkUser"){
                if(commonObj.existCheckUserMap && Object.getOwnPropertyNames(commonObj.existCheckUserMap).length > 0){
                    for(var key in commonObj.existCheckUserMap){
                        $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
                    }
                }
            }
        });
    },
    //人员分组
    groupBy: function (array, f) {
        var groups = {};
        array.forEach(function (o) {
            var group = JSON.stringify(f(o));
            groups[group] = groups[group] || [];
            groups[group].push(o);
        });
        return Object.keys(groups).map(function (group) {
            return groups[group];
        });
    },
    //重新渲染ichecks
    reloadICheck: function (root) {
        root.find('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green'
        });
    },
    enterEvent:function (event, func) {
        if (event.keyCode == '13' || event.keyCode ==13){
            func();
        }
    },
    removeBtn: function (t) {
        var divId = $(t).attr("data-divId")
        var userId = $(t).attr("userId");
        $("#userId"+userId).remove();
        $("#userName"+userId).remove();
        $(t).remove();
        if (divId == "organizerUser"){
            delete commonObj.existOrganizerUserMap[userId];
        }else if(divId == "hostUser"){
            delete commonObj.existHostUserMap[userId];
        }else if(divId == "parkUser"){
            delete commonObj.existParkUserMap[userId];
        }else if (divId == "headUser"){
            delete commonObj.existHeadUserMap[userId];
        }else if (divId == "checkUser"){
            delete commonObj.existCheckUserMap[userId];
        }else if (divId == "visibleUser"){
            delete commonObj.existVisitUserMap[userId];
        }
    },
    renderMeetingUserDiv: function (divId, map, list, inputName, inputUserId, inputUserName) {
        var btnList = "";
        if(map && Object.getOwnPropertyNames(map).length > 0){
            for(var userId in map){
                var userName = map[userId].userName;
                var deptId = map[userId].deptId;
                var $span = '<div userId="'+userId+'" data-divId="'+divId+'"  deptId="'+deptId+'" title="'+userName+'" onclick="commonObj.removeBtn(this);" class="userDivClass">\n' +
                    '            <div class="userClass">'+userName+'</div>\n' +
                    '            <div title="删除" class="deleteClass">\n' +
                    '                <i class="fa fa-trash"></i>\n' +
                    '            </div>\n' +
                    '        </div>';
                var inputUserIdTemp = '<input id="userId'+userId+'" name="'+inputUserId+'" type="hidden" value="' + userId + '">';
                var inputUserNameTemp = '<input id="userName'+userId+'" name="'+inputUserName+'" type="hidden" value="' + userName + '">';

                $span  += inputUserIdTemp;
                $span  += inputUserNameTemp;
                btnList += $span;
                $("#"+divId).html(btnList);
            }
            if(list && list.length > 0){
                var html1 = "";
                for(var i = 0; i < list.length; i++){
                    html1 +='<div data-add="1" data-name="'+list[i]+'" data-divId="'+divId+'" class="userDivClass">\n' +
                        '<input name="inputUserName" type="hidden" value="'+list[i]+'" >' +
                        '<div data-name="'+list[i]+'"  onclick="commonObj.editOtherUser(this)" class="userClass">' +list[i]+'</div>\n' +
                        '<div title = "删除" data-name="'+list[i]+'" onclick="commonObj.removeOtherUser(this)" class="deleteClass">\n' +
                        '<i class="fa fa-trash"></i>\n' +
                        '</div>\n' +
                        '</div> ';
                    html1 += '<input  name="'+inputName+'" type="hidden" value="' + list[i] + '">';
                }
                $("#"+divId).append(html1);
            }

        }else {
            $("#"+divId).html("");
        }
    },
    //选择人员弹窗，人员保存按钮事件
    userSaveClickInit: function () {
        //人员搜索按钮
        $("#userSearch").on("click", function () {
            commonObj.renderUserPage($("#submitEvaluationPeople"));
        });
        //人员确定按钮
        $("#submitEvaluationPeople").click(function () {
            var divId = $("#submitEvaluationPeople").attr("data-divId");
            if (divId == "organizerUser") {
                commonObj.renderMeetingUserDiv(divId, commonObj.existOrganizerUserMap,commonObj.existOrganizerUserList, "otherOrganizer", 'inputUserId1', 'inputUserName1');
            }
            if (divId == "hostUser") {
                commonObj.renderMeetingUserDiv(divId, commonObj.existHostUserMap,commonObj.existHostUserList, "otherHost", 'inputUserId', 'inputUserName');
            }
            if (divId == "parkUser") {
                commonObj.renderMeetingUserDiv(divId, commonObj.existParkUserMap,commonObj.existParkUserList, "otherPark", 'inputUserId2', 'inputUserName2');
            }
            if (divId == "visibleUser") {
                commonObj.renderMeetingUserDiv(divId, commonObj.existVisitUserMap,null, "visibleUser", 'inputUserId', 'inputUserName');
            }
            if (divId == "headUser") {
                commonObj.renderMeetingUserDiv(divId, commonObj.existHeadUserMap,null, "headUser", 'inputUserId', 'inputUserName');
            }
            if (divId == "checkUser") {
                commonObj.renderMeetingUserDiv(divId, commonObj.existCheckUserMap,null, "checkUser", 'inputUserId1', 'inputUserName1');
            }
            $('#evaluationModal').modal('hide');
        });

    },
    //添加公司人员
    addCompanyUser: function (t) {
        //清空原数据
        commonObj.renderUserPage(t);
        $("#evaluationModal").modal('toggle');
    },
    //添加非公司人员
    addOtherUser: function (t) {
        var divId = $(t).attr("data-divId");
        $("#addDivId").val(divId);
        $("#addUser").val("");
        var index  = layer.open({
            type: 1,
            zIndex: 9999,
            content: $("#addOtherUserDiv"),
            btn: ['保存'],
            area: ['400px', '160px'],
            title: "添加非公司人员",
            yes: function (t) {
                if(!$("#addUserForm").valid()) return;
                var userName = $("#addUser").val();
                var html ='<div data-add="1" data-name="'+userName+'" data-divId="'+divId+'" class="userDivClass">\n' +
                    '<input name="inputUserName" type="hidden" value="'+userName+'" >' +
                    '<div data-name="'+userName+'"  onclick="commonObj.editOtherUser(this)" class="userClass">' +userName+'</div>\n' +
                    '<div title = "删除" data-name="'+userName+'" onclick="commonObj.removeOtherUser(this)" class="deleteClass">\n' +
                    '<i class="fa fa-trash"></i>\n' +
                    '</div>\n' +
                    '</div> ';
                if (divId == "organizerUser") {
                    commonObj.existOrganizerUserList.push(userName);
                    html += '<input  name="otherOrganizer" type="hidden" value="' + userName + '">';
                }
                if (divId == "hostUser") {
                    commonObj.existHostUserList.push(userName);
                    html += '<input  name="otherHost" type="hidden" value="' + userName + '">';
                }
                if (divId == "parkUser") {
                    commonObj.existParkUserList.push(userName);
                    html += '<input  name="otherPark" type="hidden" value="' + userName + '">';
                }
                $("#"+divId).append(html);
                layer.closeAll();
            }
        });
    },
    //移除非公司人员
    removeOtherUser: function (t) {
        layer.confirm('确认删除？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            var checkName = $(t).attr("data-name");
            var $parentDiv = $(t).parent();
            var divId = $parentDiv.attr("data-divId");
            $parentDiv.remove();
            if (checkName){
                var list = null;
                if (divId == "organizerUser") {
                    list = commonObj.existOrganizerUserList;
                }
                if (divId == "hostUser") {
                    list = commonObj.existHostUserList;
                }
                if (divId == "parkUser") {
                    list = commonObj.existParkUserList;
                }
                if(list && list.length > 0){
                    for (var i = 0; i < list.length; i++) {
                        if (list[i] == checkName) {
                            list.splice(i, 1);
                        }
                    }
                }
            };
            layer.close(index);
        }, function () {
            return;
        });
    },
    //编辑非公司人员
    editOtherUser: function (t) {
        var currentName = $(t).attr("data-name");
        $("#editUserName").val(currentName);
        var $parentDiv = $(t).parent();
        var divId = $parentDiv.attr("data-divId");
        $("#editDivId").val(divId);
        var index = layer.open({
            type: 1,
            zIndex: 9999,
            content: $("#updateOtherUserDiv"),
            btn: ['保存'],
            area: ['400px', '160px'],
            title: "修改非公司人员",
            yes: function (t1) {
                if(!$("#updateUserForm").valid()) return;
                $(t).html($("#editUserName").val());
                $(t).attr("data-name",$("#editUserName").val());
                layer.closeAll();
            }
        });
    },
    //初始化添加会议弹窗
    initMeetingModal: function () {
        $("#addMeetingForm")[0].reset();//初始化表单
        $("#hostUser").html("");
        $("#organizerUser").html("");
        $("#parkUser").html("");
        commonObj.existOrganizerUserMap = {};
        commonObj.existOrganizerUserList = [];
        commonObj.existHostUserMap = {};
        commonObj.existHostUserList = [];
        commonObj.existParkUserMap = {};
        commonObj.existParkUserList = [];
        commonObj.existVisitUserMap = {};
        commonObj.chooseTimeRange = {};
        $("#meetingStartTime").val("");
        $("#meetingEndTime").val("");
        $("#showTime").text("");
    },
    //添加会议弹窗
    addMeetingModal: function (data) {
        commonObj.initMeetingModal();
        //打开会议申请
        $("#meetRoomApplyId").val(data.data.result.id);
        $("#meetingAddress").val(data.data.result.meetingRoom.name+"("+data.data.result.meetingRoom.address+")");
        commonObj.addMeetingPageTimeSlot(data.data.result);
        commonObj.requestData(null, "/meeting/meetingorganization", 'get', 'json',false,function (data) {
            if(data && data.length > 0){
                var html = " <option value=\"\" selected>请选择关联会议</option>";
                $(data).each(function (i, d) {
                    html += "<option value='" + d.id + "'>" + d.title + "</option>";
                });
                $("#relateMeetId").html(html);
            }
        });
        layui.use(["form"], function () {
            layui.form.render('select');
        });
        $("#addMeeting").modal("toggle");
    },
    //保存会议
    meetingSaveUrl: "/meeting/addMeeting",
    saveMeeting: function () {
        if(!$("#addMeetingForm").valid()) return;
        if(!commonObj.existOrganizerUserMap || Object.getOwnPropertyNames(commonObj.existOrganizerUserMap).length <= 0){
            layer.msg("会议组织人必须有一个是本公司人员！", {time: 2000, icon: 5});
            return;
        }
        if(!commonObj.existHostUserMap || Object.getOwnPropertyNames(commonObj.existHostUserMap).length <= 0){
            layer.msg("会议主持人必须有一个是本公司人员！", {time: 2000, icon: 5});
            return;
        }
        if(!commonObj.existParkUserMap || Object.getOwnPropertyNames(commonObj.existParkUserMap).length <= 0){
            layer.msg("会议参与人员必须有一个是本公司人员！", {time: 2000, icon: 5});
            return;
        }
        if(!$("#meetingStartTime").val() || !$("#meetingEndTime").val()){
            layer.msg("请选择会议时间！", {time: 2000, icon: 5});
            return;
        }
        //请求预约
        var formData =  new FormData($("#addMeetingForm")[0]);
        if(!formData.get("meetType")){
            layer.msg("请选择会议类型！", {time: 2000, icon: 5});
            return;
        }
        if(!formData.get("meetSummaryId")){
            layer.msg("请选择会议纪要人员！", {time: 2000, icon: 5});
            return;
        }
        formData.otherHost = commonObj.existHostUserList;
        formData.otherOrganizer = commonObj.existOrganizerUserList;
        formData.otherPark = commonObj.existParkUserList;
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
        startModal("#addMeetingBtn");
        $.ajax({
            type:"post",
            url : commonObj.meetingSaveUrl,
            data : formData,
            dataType:"json",
            async:true,
            cache:false,
            contentType:false,
            processData:false,
            success:function (data) {
                Ladda.stopAll();
                swal({
                    title: data.code == 200 ? "成功!" : "失败",
                    text: data.code == 200 ? "添加会议成功！" : data.msg,
                    type: data.code == 200 ? "success" : "error",
                    html: true
                });
                if(data.code==200){
                    $("#addMeeting").modal("toggle");
                }
            },
            error: function (data) {
                Ladda.stopAll();
                swal({
                    title: data.code == 200 ? "成功!" : "失败",
                    text: data.code == 200 ? "添加会议成功！" : data.msg,
                    type: data.code == 200 ? "success" : "error",
                    html: true
                });
            }
        });
    },
    //会议详情展示
    showMeetingDetail: function (meetId) {
        var meetingDetailCompont = new MeetingDetailCompont({
            id:meetId,
            zIndex: -99000,
            //同意按钮回调函数
            agreeCallback: function (data) {
                return meetingDetailObj.meetingAcceptFun(data.data.entity.id,1);
            },
            //拒绝按钮回调函数
            refuseCallback:function (data) {
                return meetingDetailObj.meetingAcceptFun(data.data.entity.id,2);
            },
            //新增会议纪要按钮回调函数
            meetSummaryCallback:function (data) {
                meetingDetailObj.addMeetingRecordModal(data.data.entity.id,data.data.entity.title, 1);
            },
            //新增会议记录回调函数
            meetRecordCallback:function (data) {
                meetingDetailObj.addMeetingRecordModal(data.data.entity.id,data.data.entity.title,0);
            },
            //查看会议纪要按钮回调函数
            queryMeetSummaryCallback:function (data) {
                meetingDetailObj.viewMeetingRecord(data.data.entity.id, 1);
            },
            //查看会议记录按钮回调函数
            queryMeetRecordCallback:function (data) {
                meetingDetailObj.viewMeetingRecord(data.data.entity.id, 0);
            },
            //查看会议任务回调函数
            queryMeetTaskCallback:function (data) {
                meetingDetailObj.viewMeetingTask(data.data.entity.id,0);
            },
            //新增会议任务回调函数
            meetTaskCallback:function (data) {
                meetingDetailObj.addMeetingTaskModal(data.data.entity.id, data.data.entity.meetType);
            }
        });
        meetingDetailCompont.render();
    }
}

//会议详情里面事件
var meetingDetailObj = {
    getMeetingRecordTotal: "/meetingRecordViewUser/getMeetingRecordTotal",
    listMeetingRecordUrl: "/meetingRecordViewUser/meetingRecordListPg",
    getMeetingTaskTotal: "/meetingTask/getMeetingTaskTotal",
    listMeetingTaskUrl: "/meetingTask/meetingTask",
    meetingRecordCallback: function (data, target) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                html += "<tr>\n" +
                    "            <td title=\""+record.title+"\">\n" +
                    "                <div class=\"tdContent\">\n" +
                    "                    "+record.title+"\n" +
                    "                </div>\n" +
                    "            </td>\n" +
                    "            <td title=\""+record.name+"\">\n" +
                    "                <div class=\"tdContent\">\n" +
                    "                    "+record.name+"\n" +
                    "                </div>\n" +
                    "            </td>\n" +
                    "            <td>\n" +
                    "                <div class=\"tdContent\">\n" +
                    "                    "+record.content+"\n" +
                    "                </div>\n" +
                    "            </td>\n" +
                    "            <td>\n" +
                    "                <div class=\"tdContent\">\n" +
                    "                    <a href='javascript:;' onclick='meetingDetailObj.viewMeetingRecordModal("+JSON.stringify(record)+")'>查看</a>\n" +
                    "                </div>\n" +
                    "            </td>\n" +
                    "        </tr>";
            });
        }
        var $parentList = target ? $(target) : $("#meetingRecordList");
        $parentList.html(html);
    },
    getStateStr: function (task) {
        var currentTime = new Date().format("yyyy-MM-dd hh:mm:ss");
        //如果是正常状态，根据时间获取状态值
        if(task.state == 0){
            if(task.startTime > currentTime){
                return "未开始";
            }else if(task.endTime > currentTime){
                return "进行中";
            }else {
                return "已结束";
            }
        }else if(task.state == 1){
            return "已延期";
        }else if(task.state == 2){
            return "已超期";
        }else if(task.state == 3){
            return "检查通过";
        }else if(task.state == 4){
            return "检查不通过";
        }else if(task.state == 5){
            return "已取消";
        }else {
            return "";
        }
    },
    meetingTaskCallback: function (data,target) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, task) {

                html += "<tr>\n" +
                    "        <td title=\""+task.title+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+task.title+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+task.inputUserName+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+task.inputUserName+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+task.inputUserName1+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+task.inputUserName1+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+task.startTime+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+task.startTime+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+task.endTime+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+task.endTime+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+task.content+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+task.content+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingDetailObj.getStateStr(task)+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+meetingDetailObj.getStateStr(task)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "    </tr>";
            });
        }
        var $parentList = target ? $(target) : $("#meetingRecordList");
        $parentList.html(html);
    },
    //是否接受会议
    meetingAcceptFun: function (id, t) {
        var result = false;
        var param = {id:id , flag :t};
        commonObj.requestData(param, "/meeting/meetingFlag", "post", "json", false, function (data) {
            if (data.data.entity){
                result = true;
            }else {
                result = false;
            }
        });
        return result;
    },
    //添加会议记录弹窗
    addMeetingRecordModal: function (meetId, meetName, t) {
        $("#addMeetingRecordForm")[0].reset();//初始化表单
        $("#visibleUser").html("");
        editToolObj.setContent("");
        commonObj.existVisitUserMap = {};//清空选中人员
        var title = "添加会议记录";
        if(t == 0){
            title = "添加会议记录";
            $("#visibleUserDiv").css("display", "block");
        }else {
            title = "添加会议纪要";
            $("#visibleUserDiv").css("display", "none");
        }
        $("#meetId1").val(meetId);
        $("#meettingName").val(meetName);
        $("#recordType").val(t); //类型: 0-记录、1-纪要
        $("#recordTitle").text(title);
        $("#addMeetingRecordModal").modal("toggle");
    },
    //查看会议记录弹窗
    viewMeetingRecordModal: function (record) {
        if(record){
            $("#meettingName1").val(record.title);
            var html = "";
            if(record.name){
                var recordList = record.name.split(",");
                $.each(recordList, function (i, userName) {
                    html +='<div class="userDivClass">\n' +
                        '<div data-name="'+userName+'" class="userClass">' +userName+'</div>\n' +
                        '</div> ';
                });
            }
            $("#visibleUser1").html(html);
            if(record.recordType == 1){
                $("#visibleUserDiv1").css("display", "none");
                $("#recordTitle1").html("纪要详情");
            }else {
                $("#recordTitle1").html("记录详情");
                $("#visibleUserDiv1").css("display", "block");
            }
            // $("#viewMeetingRecordModal .newsContent").html(record.content);
            viewToolObj.setContent(record.content);
            $("#viewMeetingRecordModal").modal("toggle");
        }else {
            layer.msg("很抱歉，记录为空！", {time: 2000, icon: 5});
        }
    },
    //添加会议记录方法
    addMeetingRecord: function (t, url) {
        if(!$("#addMeetingRecordForm").valid()) return;
        var param = $("#addMeetingRecordForm").serializeForm();
        var title = param.recordType == 0 ? "添加会议记录成功！" : "添加会议纪要成功！";
        param.content = editToolObj.getContent();
        if(!param.content){
            layer.msg("记录内容不能为空！", {time: 2000, icon: 5});
        }
        if(commonObj.existVisitUserMap || Object.getOwnPropertyNames(commonObj.existVisitUserMap).length > 0){
            param.inputVisibleUserId = [];
            for(var key in commonObj.existVisitUserMap){
                param.inputVisibleUserId.push(key);
            }
        }
        startModal("#addMeetingRecord");
        commonObj.requestData(JSON.stringify(param), url, "post", "json",true,function (data) {
            Ladda.stopAll();
            swal({
                title: data.code == 200 ? "成功!" : "失败",
                text: data.code == 200 ? title : data.msg,
                type: data.code == 200 ? "success" : "error",
                html: true
            });
            if(data.code==200){
                $("#addMeetingRecordModal").modal("toggle");
            }
        },true);
    },
    //会议记录查看
    viewMeetingRecord: function (meetId,recordType) {
        var title = recordType == 1 ? "会议纪要列表" : "会议记录列表";
        //初始化分页组件
        var index  = layer.open({
            type: 1,
            title: false,
            zIndex: -98000,
            content: $("#meetingRecordList").html(),
            btn: [],
            area: ['90%', '80%'],
            closeBtn: 1,
            shadeClose: false,
            btn: [],
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                $(layero[0]).find(".layui-layer-btn").css("backgroundColor", "#E2E6EC");
                $(layero[0]).find("#meetingRecordTitle").text(title);
                commonObj.requestData({id:meetId,recordType:recordType}, meetingDetailObj.getMeetingRecordTotal, "post", "json", false, function (data) {
                    if(data && data.code == 200){
                        commonObj.pagerPlus({
                            elem: $(layero[0]).find(".meetingRecordListPager"),
                            count: data.data.total,
                            url: meetingDetailObj.listMeetingRecordUrl,
                            target: $(layero[0]).find(".meetingRecordList"),
                            param: {id:meetId,recordType:recordType},
                        },meetingDetailObj.meetingRecordCallback);

                    }
                });
            }
        });
    },
    //会议任务增加弹窗
    addMeetingTaskModal: function (id, meetType) {
        $("#addMeetingTaskFrom")[0].reset();//初始化表单
        $("#headUser").html("");
        $("#checkUser").html("");
        commonObj.existCheckUserMap = {};
        commonObj.existHeadUserMap = {};
        $("#meetId").val(id);
        $("#meetType1").val(meetType);
        layui.form.render();
        $("#addMeetingTaskModal").modal("toggle");
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
    //添加任务
    addMeetingTask: function (t, url) {
        var validateTaskResult = meetingDetailObj.validateTaskTime($("#taskStartTime").val(), "#taskStartTime-error", "#taskEndTime-error", 1, "#taskEndTime");
        if(!validateTaskResult) return;
        if(!$("#addMeetingTaskFrom").valid()) return;
        if(!commonObj.existHeadUserMap || Object.getOwnPropertyNames(commonObj.existHeadUserMap).length <= 0){
            layer.msg("会议任务必须有负责人！", {time: 2000, icon: 5});
            return;
        }
        if(!commonObj.existCheckUserMap || Object.getOwnPropertyNames(commonObj.existCheckUserMap).length <= 0){
            layer.msg("会议任务必须有检查人！", {time: 2000, icon: 5});
            return;
        }
        var param = $("#addMeetingTaskFrom").serializeJson();
        param.inputUserId = [];
        for(var key in commonObj.existHeadUserMap){
            param.inputUserId.push(key);
        }
        param.inputUserId1 = [];
        for(var key in commonObj.existCheckUserMap){
            param.inputUserId1.push(key);
        }
        startModal("#saveTaskBtn");
        commonObj.requestData(JSON.stringify(param), url, "post", "json",true,function (data) {
            Ladda.stopAll();
            swal({
                title: data.code == 200 ? "成功!" : "失败",
                text: data.code == 200 ? "添加会议任务" : data.msg,
                type: data.code == 200 ? "success" : "error",
                html: true
            });
            if(data.code==200){
                $("#addMeetingTaskModal").modal("toggle");
            }
        },true);
    },
    //查看会议任务
    viewMeetingTask: function (meetId, t) {
        //初始化分页组件
        var index  = layer.open({
            type: 1,
            title: false,
            zIndex: -97000,
            content: $("#meetingTaskList").html(),
            btn: [],
            area: ['90%', '80%'],
            closeBtn: 1,
            shadeClose: false,
            btn: [],
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                $(layero[0]).find(".layui-layer-btn").css("backgroundColor", "#E2E6EC");
                commonObj.requestData({id:meetId}, meetingDetailObj.getMeetingTaskTotal, "post", "json", true, function (data) {
                    if(data && data.code == 200){
                        commonObj.pagerPlus({
                            elem: $(layero[0]).find(".meetingTaskListPager"),
                            count: data.data.total,
                            url: meetingDetailObj.listMeetingTaskUrl,
                            target: $(layero[0]).find(".meetingTaskList"),
                            param: {id:meetId},
                        },meetingDetailObj.meetingTaskCallback);

                    }
                });
            }
        });
    }
}

//预定会议室
var applyMeetingRoomObj = {
    getTotalUrl: "/meetingRoom/getCountByParam",
    meetingRoomTimeSlotUrl: "/meetingRoom/listApplyMeetingRoom",
    meetingRoomApplyUrl: "/meetingRoomApply/save",
    pagerPluginElem: 'applyMeetingRoomPager',
    chooseTimeRange: {}, //预约时间范围，格式：{roomId: {seq1: 1, seq2: 2, startTime: 12:00, endTime: 12:30}}
    callback: function (data) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, meetingRoom) {
                html += "<div class=\"well\" style=\"background-color: white;padding: 10px;\">\n" +
                         "    <div class=\"meetingRoomContent\">\n" +
                         "        <div class=\"meetingRoomBg\">\n" +
                         "            <img src=\""+(meetingRoom.picPath ? meetingRoom.picPath : "/img/meetingRoom.png")+"\" width=\"78px\" height=\"78px\"/>\n" +
                         "        </div>\n" +
                         "        <div class=\"meetingRoomDetail\">\n" +
                         "            <div class=\"meetingRoomName\">\n" +
                         "                <span>"+meetingRoom.name+"</span>&nbsp;&nbsp;" +
                         (meetingRoom.auditFlag == 1 ? "<span class='auditFlagCls'>需要审核" : "<span>")+"</span>\n" +
                         "            </div>\n" +
                         "            <div class=\"meetingRoomAddress\">\n" +
                         "                <img src=\"/img/meeting/didian.png\" width=\"13\" height=\"13\"><span>"+meetingRoom.address+"</span>\n" +
                         "            </div>\n" +
                         "            <div class=\"meetingRoomDevice\">\n" +
                         "                <img src='/img/meeting/renyuan.png' width='13' height='13'/><span>"+meetingRoom.peopleNum+"人</span>" +
                         "                <img src='/img/meeting/shebei.png' width='13' height='13' style='margin-left: 15px;'/><span>"+meetingRoom.otherDevice+"</span>\n" +
                         "            </div>\n" +
                         "            <div class=\"meetingRoomRemark\">\n" +
                         "                会议描述："+meetingRoom.remarks+"\n" +
                         "            </div>\n" +
                         "        </div>\n" +
                         "    </div>\n" +
                         "    <div class=\"meetingRoomTimeSlotContent\">\n" +
                         "        <div class=\"meetingRoomTimeSlotDiv\">\n" +
                         "            <ul id='roomId"+meetingRoom.id+"' class=\"meetingRoomTimeSlotUl\" data-applyNotice='"+meetingRoom.applyNotice+"'>\n";
                if(meetingRoom.timeSlotList && meetingRoom.timeSlotList.length > 0){
                    var width = 100 / meetingRoom.timeSlotList.length;
                    $.each(meetingRoom.timeSlotList,function (i,timeSlot) {
                       var cls = (timeSlot.passTimeFlag == "1" || timeSlot.applyFlag == "1" || timeSlot.aheadFlag == "1") ? "passTimeBg" : "";
                       var chooseFlag = (timeSlot.passTimeFlag == "1" || timeSlot.applyFlag == "1" || timeSlot.aheadFlag == "1") ? "1" : "0"; //1-不可选、0-可选
                       if(timeSlot.wholeTimeFlag == "1") {
                           html += "<li onclick='applyMeetingRoomObj.liClickFun(this);' onmouseover='applyMeetingRoomObj.liMouseOver(this);' onmouseout='applyMeetingRoomObj.liMouseOut(this);' data-html='true' data-container='body' data-toggle='popover' data-placement='top' id='room_" + (meetingRoom.id + "_" + i) + "' data-seq='" + i + "' data-roomId='" + meetingRoom.id + "' data-auditFlag='" + meetingRoom.auditFlag + "' data-flag='" + chooseFlag + "' data-startTime='" + timeSlot.startTime + "' data-endTime='" + timeSlot.endTime + "' data-applyId='" + (timeSlot.applyId || "") + "' data-applyTimeSlot='" + (timeSlot.applyTimeSlot || "") + "' data-applyUserDeptName='" + (timeSlot.applyUserDeptName || "") + "' data-applyUserName='" + (timeSlot.applyUserName || "") + "' class='" + cls + "' style='width: " + width + "%;'><span class='timeSpanLeft'>" + timeSlot.hour + "</span></li>";
                       }else if(timeSlot.wholeTimeFlag == "2"){
                           html += "<li onclick='applyMeetingRoomObj.liClickFun(this);' onmouseover='applyMeetingRoomObj.liMouseOver(this);' onmouseout='applyMeetingRoomObj.liMouseOut(this);' data-html='true' data-container='body' data-toggle='popover' data-placement='top' id='room_" + (meetingRoom.id + "_" + i) + "' data-seq='" + i + "' data-roomId='" + meetingRoom.id + "' data-auditFlag='" + meetingRoom.auditFlag + "' data-flag='" + chooseFlag + "' data-startTime='" + timeSlot.startTime + "' data-endTime='" + timeSlot.endTime + "' data-applyId='" + (timeSlot.applyId || "") + "' data-applyTimeSlot='" + (timeSlot.applyTimeSlot || "") + "' data-applyUserDeptName='" + (timeSlot.applyUserDeptName || "") + "' data-applyUserName='" + (timeSlot.applyUserName || "") + "' class='" + cls + "' style='width: " + width + "%;text-align: right;'><span class='timeSpanRight'>" + timeSlot.hour1 + "</span></li>";
                       }else if(timeSlot.wholeTimeFlag == "3"){
                           html += "<li onclick='applyMeetingRoomObj.liClickFun(this);' onmouseover='applyMeetingRoomObj.liMouseOver(this);' onmouseout='applyMeetingRoomObj.liMouseOut(this);' data-html='true' data-container='body' data-toggle='popover' data-placement='top' id='room_" + (meetingRoom.id + "_" + i) + "' data-seq='" + i + "' data-roomId='" + meetingRoom.id + "' data-auditFlag='" + meetingRoom.auditFlag + "' data-flag='" + chooseFlag + "' data-startTime='" + timeSlot.startTime + "' data-endTime='" + timeSlot.endTime + "' data-applyId='" + (timeSlot.applyId || "") + "' data-applyTimeSlot='" + (timeSlot.applyTimeSlot || "") + "' data-applyUserDeptName='" + (timeSlot.applyUserDeptName || "") + "' data-applyUserName='" + (timeSlot.applyUserName || "") + "' class='" + cls + "' style='width: " + width + "%;text-align: right;'><span class='timeSpanLeft'>" + timeSlot.hour + "</span><span class='timeSpanRight'>" + timeSlot.hour1 + "</span></li>";
                       }else{
                           html += "<li onclick='applyMeetingRoomObj.liClickFun(this);' onmouseover='applyMeetingRoomObj.liMouseOver(this);' onmouseout='applyMeetingRoomObj.liMouseOut(this);' data-html='true' data-container='body' data-toggle='popover' data-placement='top' id='room_" + (meetingRoom.id + "_" + i) + "' data-seq='" + i + "' data-roomId='" + meetingRoom.id + "' data-auditFlag='" + meetingRoom.auditFlag + "' data-flag='" + chooseFlag + "' data-startTime='" + timeSlot.startTime + "' data-endTime='" + timeSlot.endTime + "' data-applyId='" + (timeSlot.applyId || "") + "' data-applyTimeSlot='" + (timeSlot.applyTimeSlot || "") + "' data-applyUserDeptName='" + (timeSlot.applyUserDeptName || "") + "' data-applyUserName='" + (timeSlot.applyUserName || "") + "' class='" + cls + "' style='width: " + width + "%;'></li>";
                       }
                    });
                }
                html +=  "            </ul>\n" +
                         "        </div>\n" +
                         "    </div>\n" +
                         "</div>";
            });
        }
        $("#applyList").html(html);
        $("[data-toggle='popover']").popover({html : true }); //气泡弹窗启用
    },
    initPagerPlugin: function () {
        //初始化数据
        applyMeetingRoomObj.chooseTimeRange = {};
        //初始化分页组件
        commonObj.requestData({applyDate: $("#applyDate").val(),address:$("#address").val()}, applyMeetingRoomObj.getTotalUrl, "post", "json", true, function (data) {
           if(data && data.code == 200){
               commonObj.pagerPlus({
                   param: {applyDate: $("#applyDate").val(),address:$("#address").val()},
                   elem: applyMeetingRoomObj.pagerPluginElem,
                   count: data.data.total,
                   url: applyMeetingRoomObj.meetingRoomTimeSlotUrl,
                   bubbleType: "bubble" //有气泡弹窗页面设置
               },applyMeetingRoomObj.callback);
           }
        });
    },
    calculateRange: function (roomId, seq) {
        /*if(applyMeetingRoomObj.chooseTimeRange[roomId]){ //如果有值，判断有哪些值
            var seq1 = applyMeetingRoomObj.chooseTimeRange[roomId].seq1;
            var seq2 = applyMeetingRoomObj.chooseTimeRange[roomId].seq2;
            if(seq1 && seq2){ //都有值，则判断大小
                if(seq > seq2){
                    applyMeetingRoomObj.chooseTimeRange[roomId].seq2 = seq;
                }else if(seq < seq1){
                    applyMeetingRoomObj.chooseTimeRange[roomId].seq1 = seq;
                    applyMeetingRoomObj.chooseTimeRange[roomId].seq2 = seq1;
                }else {
                    applyMeetingRoomObj.chooseTimeRange[roomId].seq1 = seq;
                }
            }else{ //判断给谁赋值
                if(seq1 && !seq2){
                    if(seq1 > seq){
                        applyMeetingRoomObj.chooseTimeRange[roomId].seq1 = seq;
                        applyMeetingRoomObj.chooseTimeRange[roomId].seq2 = seq1;
                    }else{
                        applyMeetingRoomObj.chooseTimeRange[roomId].seq2 = seq;
                    }
                }else if(seq2 && !seq1){
                    if(seq > seq2){
                        applyMeetingRoomObj.chooseTimeRange[roomId].seq1 = seq2;
                        applyMeetingRoomObj.chooseTimeRange[roomId].seq2 = seq;
                    }else{
                        applyMeetingRoomObj.chooseTimeRange[roomId].seq2 = seq;
                    }
                }else {
                    applyMeetingRoomObj.chooseTimeRange[roomId].seq1 = seq;
                }
            }
        }else { //没有值则一定是开始值
            applyMeetingRoomObj.chooseTimeRange[roomId] = {seq1: seq};
        }*/
        commonObj.calculateRange(applyMeetingRoomObj.chooseTimeRange, roomId, seq, "roomId"+roomId, "room_");
        //渲染当前节点
        /*var seq11 = applyMeetingRoomObj.chooseTimeRange[roomId].seq1;
        var seq21 = applyMeetingRoomObj.chooseTimeRange[roomId].seq2;
        $("#roomId"+roomId).find("li").removeClass("chooseTimeBg"); //移除选中效果
        $("#roomId"+roomId).find("li").removeClass("edgeTimeBg"); //移除选中效果
        if(seq11 && seq21){
            for (var i = seq11; i <= seq21; i++){
                if(seq11 == i || seq21 == i){
                    $("#room_"+roomId+"_"+i).addClass("chooseTimeBg edgeTimeBg");
                }else {
                    $("#room_"+roomId+"_"+i).addClass("chooseTimeBg");
                }
            }
        }else {
            if(seq11){
                $("#room_"+roomId+"_"+seq11).addClass("edgeTimeBg");
            }
            if(seq21){
                $("#room_"+roomId+"_"+seq21).addClass("edgeTimeBg");
            }
        }*/
    },
    clearBubble: function () {
        $("[data-toggle='popover']").popover("hide"); //所有气泡隐藏
    },
    bubbleCloseFun: function (t) {
        var liId = $(t).attr("data-liId");
        $("#"+liId).popover('hide'); //关闭气泡
    },
    bubbleSaveFun: function (btn) {
        var $form = $(btn).parent().parent().find("form");
        if(!$("#applyMeetingRoomForm").valid()) return;
        //请求预约
        startModal("#"+$(btn).attr("id"));
        var param = $form.serializeJson();
        param.startTime = $("#applyDate").val() + " " + param.startTime; //拼接日期时间
        param.endTime = $("#applyDate").val() + " " + param.endTime;//拼接日期时间

        //预约前进行提示
        var applyNotice = $(btn).attr("data-applyNotice");
        layer.open({
            type: 0,
            content: "温馨提示：<br/>" + (applyNotice || "无"),
            yes:function (index, layero) {
                layer.close(index);
                commonObj.requestData(JSON.stringify(param), applyMeetingRoomObj.meetingRoomApplyUrl, "post", "json", true, function (data) {
                    Ladda.stopAll();
                    if(data.code == 200){
                        layer.msg("预约会议室成功！", {time: 2000, icon: 6});
                        //关闭气泡
                        applyMeetingRoomObj.bubbleCloseFun(btn);
                        //刷新页面
                        applyMeetingRoomObj.initPagerPlugin();
                        //弹出添加会议窗口
                        commonObj.addMeetingModal(data);
                    }else {
                        layer.msg(data.msg, {time: 2000, icon: 5});
                    }
                },true);
            },
            end:function () {
                Ladda.stopAll();
            }
        });
    },
    liClickFun: function (t) {
        var seq = $(t).attr("data-seq"); //获取时间段序号，序号越小越靠前
        var roomId = $(t).attr("data-roomId"); //获取会议室ID
        var chooseFlag = $(t).attr("data-flag"); //时间段是否可选：1-不可选、0-可选
        var auditFlag = $(t).attr("data-auditFlag"); //是否需要审核
        var applyNotice = $(t).closest("ul").attr("data-applyNotice") || "所有小伙伴用完会议室后，请把会议室的桌椅归位、电脑/投影仪关闭、生活垃圾清理干净，并保持会议室整洁。期待下次会议再见。";
        if(chooseFlag && chooseFlag == 1){
            layer.msg("当前时间已过或超前或被预约！");
            return;
        }
        applyMeetingRoomObj.calculateRange(roomId, seq); //计算时间范围

        //隐藏所有弹出气泡，排除当前元素，防止两次都点击此元素
        $(t).parent().find("li").filter(function (i,li) {
            return $(t).attr("id") != $(li).attr("id");
        }).popover('destroy');

        //范围区间，设置当前气泡值
        if(applyMeetingRoomObj.chooseTimeRange[roomId]){
            var seqStart = applyMeetingRoomObj.chooseTimeRange[roomId].seq1;
            var seqEnd = applyMeetingRoomObj.chooseTimeRange[roomId].seq2;
            if(seqStart && seqEnd){
                applyMeetingRoomObj.chooseTimeRange[roomId].startTime = $("#room_"+roomId+"_"+seqStart).attr("data-startTime");
                applyMeetingRoomObj.chooseTimeRange[roomId].endTime = $("#room_"+roomId+"_"+seqEnd).attr("data-endTime");
            }else if(seqStart){
                applyMeetingRoomObj.chooseTimeRange[roomId].startTime = $("#room_"+roomId+"_"+seqStart).attr("data-startTime");
                applyMeetingRoomObj.chooseTimeRange[roomId].endTime = $("#room_"+roomId+"_"+seqStart).attr("data-endTime");
            }else {
                applyMeetingRoomObj.chooseTimeRange[roomId].startTime = $("#room_"+roomId+"_"+seqEnd).attr("data-startTime");
                applyMeetingRoomObj.chooseTimeRange[roomId].endTime = $("#room_"+roomId+"_"+seqEnd).attr("data-endTime");
            }
            var startTime = applyMeetingRoomObj.chooseTimeRange[roomId].startTime;
            var endTime = applyMeetingRoomObj.chooseTimeRange[roomId].endTime;
            var minute = (new Date("2019-10-25 "+endTime) - new Date("2019-10-25 "+startTime))/1000/60;
            var content = startTime + " ~ "+endTime+"&nbsp;&nbsp;"+minute+"分钟（共"+(minute/60)+"小时）<br>";
            $(t).attr("data-content", "<div class=\"bubbleDiv\">\n" +
                "                          <form autocomplete=\"off\" class=\"form-horizontal\" method=\"post\">\n" +
                "                              <input type=\"hidden\" name=\"meetRoomId\" value=\""+roomId+"\"><!--会议室ID-->\n" +
                "                              <input type=\"hidden\" name=\"startTime\" value=\""+startTime+"\"><!--开始时间-->\n" +
                "                              <input type=\"hidden\" name=\"endTime\" value=\""+endTime+"\"><!--结束时间-->\n" +
                "                              <input type=\"hidden\" name=\"auditFlag\" value=\""+auditFlag+"\"><!--是否需要审核-->\n" +
                "                          </form>\n" +
                "                          <div class=\"bubbleContent\">\n" +
                "                              "+content+"\n" +
                "                              (再次点击确认时间段)\n" +
                "                          </div>\n" +
                "                          <div class=\"bubbleBtn\">\n" +
                "                              <button onclick='applyMeetingRoomObj.bubbleCloseFun(this)' data-liId='"+$(t).attr("id")+"' type=\"button\" class=\"layui-btn layui-btn-primary\" >关闭</button>\n" +
                "                              <button id='applyBtn"+roomId+"' onclick='applyMeetingRoomObj.bubbleSaveFun(this)' data-liId='"+$(t).attr("id")+"' data-applyNotice='"+applyNotice+"' type=\"button\" class=\"layui-btn layui-btn-normal\" >确定</button>\n" +
                "                          </div>\n" +
                "                      </div>");
            $(t).popover('show');
        }
    },
    liMouseOver: function (t) {
        var applyId = $(t).attr("data-applyId") || "";
        //如果是被预约的时间槽，则获取该预约的时间区域，并显示预约人信息
        if (applyId) {
            var applyTimeSlot = $(t).attr("data-applyTimeSlot") || "";
            var applyUserDeptName = $(t).attr("data-applyUserDeptName") || "";
            var applyUserName = $(t).attr("data-applyUserName") || "";
            var startTime = applyTimeSlot.split("~")[0].trim();
            var endTime = applyTimeSlot.split("~")[1].trim();
            var minute = (new Date("2019-10-25 " + endTime) - new Date("2019-10-25 " + startTime)) / 1000 / 60;
            var content = startTime + " ~ " + endTime + "&nbsp;&nbsp;" + minute + "分钟（共" + (minute / 60) + "小时）<br>";
            $(t).attr("data-content", "<div class=\"bubbleDiv\">\n" +
                "                          <div class=\"bubbleContent\">\n" +
                "                              " + content + "\n" +
                "                              预约人：" + applyUserName + "<br/>所属部门：" + applyUserDeptName + "\n" +
                "                          </div>\n" +
                "                          <div class=\"bubbleBtn\">\n" +
                "                              <button onclick='applyMeetingRoomObj.bubbleCloseFun(this)' data-liId='" + $(t).attr("id") + "' type=\"button\" class=\"layui-btn layui-btn-primary\" >关闭</button>\n" +
                "                      </div>");
            $(t).popover('show');

            //渲染底部颜色
            $(t).closest("ul").find("li").each(function (i, li) {
                if ($(li).attr("data-applyId") == applyId && !$(li).hasClass("edgeTimeBg")) {
                    $(li).addClass("edgeTimeBg");
                }
            });
        }
    },
    liMouseOut: function (t) {
        var applyId = $(t).attr("data-applyId") || "";
        //如果是被预约的时间槽，则获取该预约的时间区域，并隐藏预约人信息
        if (applyId) {
            var liId = $(t).attr("id");
            $("#" + liId).popover('hide'); //关闭气泡

            //渲染底部颜色
            $(t).closest("ul").find("li").each(function (i, li) {
                if ($(li).attr("data-applyId") == applyId && $(li).hasClass("edgeTimeBg")) {
                    $(li).removeClass("edgeTimeBg");
                }
            });
        }
    }
}

//我的预定
var myApplyMeetingRoomObj = {
    getTotalUrl: "/meetingRoomApply/getMyMeetingApplyTotal",
    myApplyMeetingRoomUrl: "/meetingRoomApply/listMyMeetingApply",
    delApplyMeetingRoomUrl: "/meetingRoomApply/del",
    addMeetingPageUrl: "/meetingRoomApply/addMeetingPage",
    noticeMeetingUserUrl: "/meetingRoomApply/noticeMeetingUser",
    newPagerPluginElem: 'myNewApplyMeetingRoomPager',
    oldPagerPluginElem: 'myOldApplyMeetingRoomPager',
    getApplyTimeRange: function (startTime, endTime) {
        if(startTime && endTime){
            return new Date(startTime).format("yyyy年MM月dd日 hh:mm") + " ~ " + ("00:00" == new Date(endTime).format("hh:mm") ? "24:00" : new Date(endTime).format("hh:mm"));
        }else{
            return "";
        }
    },
    getProgressFlag: function (startTime) { //会议是否在进行中
        if (startTime && startTime < new Date()){
            return true;
        } else {
            return false;
        }
    },
    callback: function (data,type) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, meetingRoomApply) {
                var stateStr = "";
                var opStr = "";
                if(type && type == 1){ //已结束，不需要状态等字段
                    opStr += "<a onclick='myApplyMeetingRoomObj.clickDetailFun("+meetingRoomApply.meetId+")' href=\"javascript:;\" title=\"会议详情\">会议详情</a>&nbsp;&nbsp;";
                }else{
                    opStr += "<a onclick='myApplyMeetingRoomObj.cancelApply("+meetingRoomApply.id+",\""+meetingRoomApply.meetingRoom.name+"\",\""+myApplyMeetingRoomObj.getApplyTimeRange(meetingRoomApply.startTime, meetingRoomApply.endTime)+"\");' href=\"javascript:;\" title=\"取消预订\">取消预订</a>&nbsp;&nbsp;";
                    if(meetingRoomApply.state == 1){
                        if(myApplyMeetingRoomObj.getProgressFlag(meetingRoomApply.startTime)){ //0-不在进行中、1-进行中
                            stateStr += "<div class=\"applyMeetingRoomState progressState\">\n" +
                                "           进行中\n" +
                                "       </div>";
                        }else{
                            opStr += "<a href=\"javascript:;\" title=\"通知参会人\" onclick='myApplyMeetingRoomObj.noticeMeetingUser("+meetingRoomApply.id+")'>通知参会人</a>&nbsp;&nbsp;";
                        }
                        opStr += "<a onclick='myApplyMeetingRoomObj.clickDetailFun("+meetingRoomApply.meetId+")' href=\"javascript:;\" title=\"会议详情\">会议详情</a>&nbsp;&nbsp;";
                        stateStr += "<div title='预约成功' class=\"applyMeetingRoomState successState\">\n" +
                            "           预约成功\n" +
                            "       </div>";
                    }else if(meetingRoomApply.state == 2){
                        stateStr += "<div title='审核中' class=\"applyMeetingRoomState progressState\">\n" +
                            "           审核中\n" +
                            "       </div>";
                        opStr += "<a href=\"javascript:;\" title=\"审核详情\" onclick='myApplyMeetingRoomObj.showHistory("+meetingRoomApply.meetId+", 21);'>审核详情</a>&nbsp;&nbsp;";
                    }else if(meetingRoomApply.state == -1){
                        stateStr += "<div class=\"applyMeetingRoomState lockState\">\n" +
                            "           审核驳回\n" +
                            "       </div>";
                    }else {
                        stateStr += "<div title='锁定中' class=\"applyMeetingRoomState lockState\">\n" +
                            "           锁定中\n" +
                            "       </div>";
                        stateStr += "<div title='会议室锁定中，请尽快填写会议内容' class=\"lockStateDesc\">（会议室锁定中，请尽快填写会议内容）</div>";
                        opStr += "<a onclick='myApplyMeetingRoomObj.addMeetingClick("+meetingRoomApply.id+");' href=\"javascript:;\" title=\"新增会议\" style='color: #F68D81;'>新增会议</a>&nbsp;&nbsp;";
                    }
                }
                html += "<li>\n" +
                    "        <div class=\"applyMeetingRoomBg\">\n" +
                    "            <img src=\""+(meetingRoomApply.meetingRoom.picPath ? meetingRoomApply.meetingRoom.picPath : "/img/meetingRoom.png")+"\" width=\"78px\" height=\"78px\"/>\n" +
                    "        </div>\n" +
                    "        <div class=\"applyMeetingRoomContent\">\n" +
                    "            <div class=\"applyMeetingRoomDetail\">\n" +
                    "                <div class=\"applyMeetingRoomName\">\n" +
                    "                    <div title=\""+meetingRoomApply.meetingRoom.name+"\" class=\"applyMeetingRoomTitle\">\n" +
                    "                        "+meetingRoomApply.meetingRoom.name+"\n" +
                    "                    </div>\n" +
                    "                    <div title=\""+meetingRoomApply.meetingRoom.address+"\" class=\"applyMeetingRoomAddress\">\n" +
                    "                        （"+meetingRoomApply.meetingRoom.address+"）\n" +
                    "                    </div>\n" +
                    "                    "+stateStr+"\n" +
                    "                </div>\n" +
                    "                <div title=\""+myApplyMeetingRoomObj.getApplyTimeRange(meetingRoomApply.startTime, meetingRoomApply.endTime)+"\" class=\"applyMeetingRoomTime\">\n" +
                    "                 <img src='/img/meeting/shijian.png' width='15' height='15'/><span>"+myApplyMeetingRoomObj.getApplyTimeRange(meetingRoomApply.startTime, meetingRoomApply.endTime)+"</span>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "            <div class=\"applyMeetingRoomOp\">\n" +
                                    opStr +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </li>";
            });
        }
        if(type && type == 1){ //已结束，不需要状态等字段
            $("#oldMeetingRoomList").html(html);
        }else {
            $("#newMeetingRoomList").html(html);
        }
    },
    initPagerPlugin: function () {
        //初始化已预定分页组件
        commonObj.requestData({}, myApplyMeetingRoomObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: myApplyMeetingRoomObj.newPagerPluginElem,
                    count: data.data.total,
                    url: myApplyMeetingRoomObj.myApplyMeetingRoomUrl,
                },myApplyMeetingRoomObj.callback);
            }
        });

        //初始化已结束预定分页组件
        commonObj.requestData({oldFlag:1}, myApplyMeetingRoomObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    param: {oldFlag:1}, //已结束列表
                    elem: myApplyMeetingRoomObj.oldPagerPluginElem,
                    count: data.data.total,
                    url: myApplyMeetingRoomObj.myApplyMeetingRoomUrl,
                },myApplyMeetingRoomObj.callback,1);
            }
        });
    },
    cancelApply: function (id, name, timeRange) {
        layer.confirm('会议室：'+name+"<br>时间："+timeRange, {
            title: '取消预订',
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function () {
            layer.closeAll();
            commonObj.requestData({id: id}, myApplyMeetingRoomObj.delApplyMeetingRoomUrl, "post", "json", true, function (data) {
                swal({
                    title: data.code == 200 ? "成功!" : "失败",
                    text: data.code == 200 ? "取消预订成功！" : data.msg,
                    type: data.code == 200 ? "success" : "error",
                    html: true
                });
                if(data.code == 200){
                    myApplyMeetingRoomObj.initPagerPlugin();
                }
            });
        }, function () {
        });
    },
    noticeMeetingUser: function (id) {
        commonObj.requestData({meetApplyId: id}, myApplyMeetingRoomObj.noticeMeetingUserUrl, "post", "json", true, function (data) {
            if(data.code == 200){
                layer.msg("已通知所有会议相关人员！", {time: 2000, icon: 6})
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5})
            }
        });
    },
    addMeetingClick: function (id) {
        commonObj.requestData({id: id}, myApplyMeetingRoomObj.addMeetingPageUrl, "post", "json", true, function (data) {
            if(data.code == 200){
                //弹出添加会议窗口
                commonObj.addMeetingModal(data);
            }
        });
    },
    clickDetailFun: function (id) {
        if(id){
            commonObj.showMeetingDetail(id);
        }else {
            layer.msg("当前会议室未绑定会议", {time: 2000, icon: 5})
        }
    },
    showHistory: function (id, processType) {
        $("#historyModal").modal({backdrop: "static"});
        $.post(baseUrl + "/process/history", {dataId: id, process: processType}, function (data) {
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
                html += "<img src='/process/getImage?dataId=" + id + "&process=" + processType + "&t=" + new Date().getTime() + "' style='width: 100%; margin-left: 13px; margin-top: -280px; margin-bottom: -100px;'/></div>";
                $("#history").append(html);
            }
        }, "json");
    }
}

//会议室预约记录
var meetingRoomHasApplyObj = {
    getTotalUrl: "/meetingRoomApply/getMeetingRoomHasApplyTotal",
    meetingRoomHasApplyUrl: "/meetingRoomApply/listMeetingRoomHasApply",
    getApplyTimeRange: function (startTime, endTime) {
        if(startTime && endTime){
            return new Date(startTime).format("yyyy年MM月dd日 hh:mm") + " ~ " + ("00:00" == new Date(endTime).format("hh:mm") ? "24:00" : new Date(endTime).format("hh:mm"));
        }else{
            return "";
        }
    },
    getProgressFlag: function (meetingRoomApply) { //状态：-1: 审核驳回、0-锁定（未填写会议室）、2-审核中（针对需要审核的会议室）、1-预约成功、-9-删除
        if(meetingRoomApply.endTime && meetingRoomApply.endTime < new Date()){
            return "";
        }else {
            if(meetingRoomApply.state == -1){
                return "<div title='审核驳回' class=\"applyMeetingRoomState lockState\">\n" +
                    "      审核驳回\n" +
                    "   </div>";
            }else if(meetingRoomApply.state == 2){
                return "<div title='审核中' class=\"applyMeetingRoomState progressState\">\n" +
                    "      审核中\n" +
                    "   </div>";
            }else if(meetingRoomApply.state == 1){
                if (meetingRoomApply.startTime && new Date(meetingRoomApply.startTime) < new Date()){
                    return "<div title='进行中' class=\"applyMeetingRoomState progressState\">\n" +
                        "      进行中\n" +
                        "   </div>";
                } else {
                    return "<div title='预约成功' class=\"applyMeetingRoomState successState\">\n" +
                        "      预约成功\n" +
                        "   </div>";
                }
            }else {
                return "<div title='锁定中' class=\"applyMeetingRoomState lockState\">\n" +
                       "    锁定中\n" +
                       "</div>" +
                       "<div title='会议室锁定中，请通知预约人尽快填写会议内容' class=\"lockStateDesc\">（会议室锁定中，请通知预约人尽快填写会议内容）</div>";
            }
        }
    },
    callback: function (data) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, meetingRoomApply) {
                var stateStr = meetingRoomHasApplyObj.getProgressFlag(meetingRoomApply);
                var opStr = "";
                if(!stateStr){
                    opStr = "<img src=\"/img/meeting/complete.png\" width=\"70\" height=\"38\"/>";
                }
                html += "<li>\n" +
                    "        <div class=\"applyMeetingRoomBg\">\n" +
                    "            <img src=\""+(meetingRoomApply.picPath ? meetingRoomApply.picPath : "/img/meetingRoom.png")+"\" width=\"78px\" height=\"78px\"/>\n" +
                    "        </div>\n" +
                    "        <div class=\"applyMeetingRoomContent\">\n" +
                    "            <div class=\"applyMeetingRoomDetail\">\n" +
                    "                <div class=\"applyMeetingRoomName\">\n" +
                    "                    <div title=\""+(meetingRoomApply.roomName || "")+"\" class=\"applyMeetingRoomTitle\">\n" +
                    "                        "+(meetingRoomApply.roomName || "")+"\n" +
                    "                    </div>\n" +
                    "                    <div title=\""+(meetingRoomApply.address || "")+"\" class=\"applyMeetingRoomAddress\">\n" +
                    "                        （"+(meetingRoomApply.address || "")+"）\n" +
                    "                    </div>\n" +
                    "                    "+stateStr+"\n" +
                    "                </div>\n" +
                    "                <div title=\"预约时间："+(meetingRoomHasApplyObj.getApplyTimeRange(meetingRoomApply.startTime, meetingRoomApply.endTime) || "")+"\" class=\"applyMeetingRoomTime\">\n" +
                    "                    <img src=\"/img/meeting/shijian.png\" width=\"15\" height=\"15\"><span>"+(meetingRoomHasApplyObj.getApplyTimeRange(meetingRoomApply.startTime, meetingRoomApply.endTime) || "")+"</span>\n" +
                    "                </div>\n" +
                    "                <div class=\"meetingRoomDevice\">\n" +
                    "                    <span style=\"padding: 0px;\">预约人:</span>\n" +
                    "                    <span style=\"padding: 0px;\">"+(meetingRoomApply.name || "-")+"</span>\n" +
                    "                    <span style=\"padding: 0px;margin-left: 20px;\">所属部门:</span>\n" +
                    "                    <span style=\"padding: 0px;\">"+(meetingRoomApply.deptName || "-")+"</span>\n" +
                    "                    <span style=\"padding: 0px;margin-left: 20px;\">会议名称:</span>\n" +
                    "                    <span style=\"padding: 0px;\">"+(meetingRoomApply.title || "暂未添加会议")+"</span>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "            <div class=\"applyMeetingRoomOp\">\n" +
                    "                "+opStr+"\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </li>";
            });
        }
        $("#meetingRoomHasApplyList").html(html);
    },
    initPagerPlugin: function (applyDate) {
        var param = $("#meetingRoomHasApplyForm").serializeJson();
        if(applyDate){
            param.applyDate = applyDate;
        }
        //初始化已预定分页组件
        commonObj.requestData(param, meetingRoomHasApplyObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: "meetingRoomHasApplyPager",
                    param:param,
                    count: data.data.total,
                    url: meetingRoomHasApplyObj.meetingRoomHasApplyUrl,
                },meetingRoomHasApplyObj.callback);
            }
        });
    },
}

//设置会议室
var meetingRoomManageObj = {
    getTotalUrl: "/meetingRoom/getCountByParam",
    listMeetingRoomUrl: "/meetingRoom/listMeetingRoom",
    addMeetingRoomUrl: "/meetingRoom/save",
    delMeetingRoomUrl: "/meetingRoom/del",
    updateMeetingRoomUrl: "/meetingRoom/update",
    getMeetingTotal: "/meeting/getMeetingTotal",
    listRoomMeetingListUrl: "/meeting/listMeetingByRoomId",
    pagerPluginElem: 'meetingRoomManagePager',
    meetingListPaperElem: "meetingListPager",
    getMeetingStateStr:function (state) {
        if(state == -1){
            return "审核驳回";
        }
        if(state == 1){
            return "预约成功";
        }
        if(state == 2){
            return "审核中";
        }
        if(state == -9){
            return "删除";
        }
        return "";
    },
    getApplyTimeRange: function (startTime, endTime) {
        if(startTime && endTime){
            return new Date(startTime).format("hh:mm") + " ~ " + (new Date(endTime).format("hh:mm") == "00:00" ? "24:00" : new Date(endTime).format("hh:mm"));
        }else{
            return "";
        }
    },
    getMeetingUserGroup: function (tempMeet) {
        var userGroup = {otherOrg: 0, otherHost: 0, otherPark: 0, acceptNum: 0, refuseNum: 0, notResponseNum: 0, allUserNum: 0};
        if(tempMeet){
            var tempAllUserList = new Array();
            if(tempMeet.otherOrganizer){
                userGroup.otherOrg = tempMeet.otherOrganizer.split(",").length;
            }
            if(tempMeet.otherHost){
                userGroup.otherHost = tempMeet.otherHost.split(",").length;
            }
            if(tempMeet.otherPark){
                userGroup.otherPark = tempMeet.otherPark.split(",").length;
            }
            if(tempMeet.acceptUserList && tempMeet.acceptUserList.length > 0){
                var tempAcceptUserList = new Array();
                for(var i = 0; i < tempMeet.acceptUserList.length; i++){
                    if(!tempAcceptUserList.contains(tempMeet.acceptUserList[i].userId)){
                        tempAcceptUserList.push(tempMeet.acceptUserList[i].userId);
                    }
                    if(!tempAllUserList.contains(tempMeet.acceptUserList[i].userId)){
                        tempAllUserList.push(tempMeet.acceptUserList[i].userId);
                    }
                }
                userGroup.acceptNum = tempAcceptUserList.length;
            }
            if(tempMeet.refuseUserList && tempMeet.refuseUserList.length > 0){
                var tempRefuseUserList = new Array();
                for(var j = 0; j < tempMeet.refuseUserList.length; j++){
                    if(!tempRefuseUserList.contains(tempMeet.refuseUserList[j].userId)){
                        tempRefuseUserList.push(tempMeet.refuseUserList[j].userId);
                    }
                    if(!tempAllUserList.contains(tempMeet.refuseUserList[j].userId)){
                        tempAllUserList.push(tempMeet.refuseUserList[j].userId);
                    }
                }
                userGroup.refuseNum = tempRefuseUserList.length;
            }
            if(tempMeet.notResponseUserList && tempMeet.notResponseUserList.length > 0){
                var tempNotResponseUserList = new Array();
                for(var z = 0; z < tempMeet.notResponseUserList.length; z++){
                    if(!tempNotResponseUserList.contains(tempMeet.notResponseUserList[z].userId)){
                        tempNotResponseUserList.push(tempMeet.notResponseUserList[z].userId);
                    }
                    if(!tempAllUserList.contains(tempMeet.notResponseUserList[z].userId)){
                        tempAllUserList.push(tempMeet.notResponseUserList[z].userId);
                    }
                }
                userGroup.notResponseNum = tempNotResponseUserList.length;
            }
            userGroup.allUserNum = userGroup.otherOrg + userGroup.otherHost + userGroup.otherPark + tempAllUserList.length;
        }
        return userGroup;
    },
    getMeetingAudit: function (tempMeet) {
        var result = "否";
        if(tempMeet){
            if(tempMeet.auditFlag == 1){
              if(tempMeet.approverUserName){
                  result = tempMeet.approverUserName;
              }else {
                  result = "是";
              }
            }
        }
        return result;
    },
    callback: function (data, target, seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, meetingRoom) {
                html += "<tr>\n" +
                    "        <td title=\""+(seq + (i + 1))+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+(seq + (i + 1))+"\n" +
                    "            </div>\n" +
                    "        </td>"+
                    "        <td title=\""+meetingRoom.name+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "               "+meetingRoom.name+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingRoom.otherDevice+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+meetingRoom.otherDevice+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingRoom.peopleNum+"人\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+meetingRoom.peopleNum+"人\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingRoom.address+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+meetingRoom.address+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(meetingRoom.enabled == 0 ? "是" : "否")+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+(meetingRoom.enabled == 0 ? "是" : "否")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingRoomManageObj.getMeetingAudit(meetingRoom)+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+meetingRoomManageObj.getMeetingAudit(meetingRoom)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <a onclick='meetingRoomManageObj.updateMeetingRoomFun("+JSON.stringify(meetingRoom)+")' href=\"javascript:;\">设置</a>&nbsp;&nbsp;\n" +
                    "            <a onclick='meetingRoomManageObj.delMeetingRoom("+meetingRoom.id+",\""+meetingRoom.name+"\")' href=\"javascript:;\" style=\"color: red;\">删除</a>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                 <a onclick='meetingRoomManageObj.clickDetailFun("+meetingRoom.id+")' href=\"javascript:;\">详情</a>\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "    </tr>";
            });
        }
        var $parentList = target ? $(target) : $("#meetingRoomList");
        $parentList.html(html);
    },
    meetingCallback: function (data, target) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, meeting) {
                html += "<tr>\n" +
                    "        <td title=\""+meeting.title+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    // "               <a href='javascript:;' onclick='commonObj.showMeetingDetail("+meeting.meetId+")'>"+meeting.title+"</a>\n" +
                    "               "+meeting.title+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingRoomManageObj.getMeetingStateStr(meeting.meetState)+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "               "+meetingRoomManageObj.getMeetingStateStr(meeting.meetState)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+new Date(meeting.startTime).format("yyyy-MM-dd")+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+new Date(meeting.startTime).format("yyyy-MM-dd")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingRoomManageObj.getApplyTimeRange(meeting.startTime, meeting.endTime)+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "               "+meetingRoomManageObj.getApplyTimeRange(meeting.startTime, meeting.endTime)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meeting.createName+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+meeting.createName+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+new Date(meeting.meetRoomCreateTime).format("yyyy-MM-dd hh:mm")+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+new Date(meeting.meetRoomCreateTime).format("yyyy-MM-dd hh:mm")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingRoomManageObj.getMeetingAudit(meeting)+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+meetingRoomManageObj.getMeetingAudit(meeting)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingRoomManageObj.getMeetingUserGroup(meeting).allUserNum+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "               "+meetingRoomManageObj.getMeetingUserGroup(meeting).allUserNum+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+meetingRoomManageObj.getMeetingUserGroup(meeting).acceptNum+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "                "+meetingRoomManageObj.getMeetingUserGroup(meeting).acceptNum+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+((new Date(meeting.endTime) - new Date(meeting.startTime))/1000/60)+"\">\n" +
                    "            <div class=\"tdContent\">\n" +
                    "               "+((new Date(meeting.endTime) - new Date(meeting.startTime))/1000/60)+"分钟\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "    </tr>";
            });
        }
        var $parentList = target ? $(target) : $("#meetingList");
        $parentList.html(html);
        // $("#meetingRoomDetailList").modal("toggle");
    } ,
    initPagerPlugin: function () {
        //初始化分页组件
        commonObj.requestData({}, applyMeetingRoomObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: meetingRoomManageObj.pagerPluginElem,
                    count: data.data.total,
                    limits:[15,30,50,100],
                    limit:15,
                    target: $("#meetingRoomList"),
                    url: meetingRoomManageObj.listMeetingRoomUrl,
                },meetingRoomManageObj.callback);
            }
        });

    },
    validateOpenTime: function (validateTime, unit, errorTipId, otherTipId, type, otherTimeId) {  //校验会议室新增/编辑开放时间是否满足最小粒度的整数倍
        var validateFlag = true; //默认校验成功
        var errorMessage = "";
        $(errorTipId).css("display", "none");
        $(otherTipId).css("display", "none");
        if(validateTime){
            //先判断范围是否正确
            if($(otherTimeId).val()){
                var startTime = "2019-10-23 ";
                var endTime = "2019-10-23 ";
                if(type == 1){ //1-开始时间
                    startTime = startTime + validateTime;
                    endTime = endTime + $(otherTimeId).val();
                    if(startTime > endTime){
                        errorMessage = "开始时间不能大于结束时间";
                        validateFlag = false;
                    }
                }else{
                    startTime = startTime + $(otherTimeId).val();
                    endTime = endTime + validateTime;
                    if(startTime > endTime){
                        errorMessage = "结束时间不能小于开始时间";
                        validateFlag = false;
                    }
                }
            }
            if(validateFlag){ //如果上面校验通过，还需要校验时间
                unit = unit || 30; //不填写默认30
                var minute = parseInt(validateTime.split(":")[1]);
                if(minute != 0 && minute % unit != 0){
                    errorMessage = "每日开放时间分钟必须是可预订最小时间段的整数倍";
                    validateFlag = false;
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
    calculateOnceTime: function (t) { //计算单次预定最长时间
        if($(t).hasClass("unit")){
            var unit = $(t).text() || 30;
            var numStr = $(t).parent().find("input[class='oneTimeCls']").val();
            var num = isNaN(parseInt(numStr)) ? 0 : parseInt(numStr); //进行转换，如果是小数则取整，如果是非数字则为0
            var result = unit * num;
            $(t).parent().find("input[class='oneTimeCls']").val(num); //重新赋值
            $(t).parent().find("input[name='onceTime']").val(result);
        }else{
            var unit = $(t).parent().find("span[class='unit']").text() || 30;
            var num = isNaN(parseInt($(t).val())) ? 0 : parseInt($(t).val()); //进行转换，如果是小数则取整，如果是非数字则为0
            var result = unit * num;
            $(t).val(num); //重新赋值
            $(t).parent().find("input[name='onceTime']").val(result);
        }
    },
    addMeetingRoomBtnFun: function () {
        $("#addMeetingRoomFrom")[0].reset();
        $("#addUnit").text(30);
        $("#addMeetingRoom").modal("toggle");
    },
    addMeetingRoom: function () {
        var validateRoomResult = meetingRoomManageObj.validateOpenTime($("#openTimeStart").val(), $("addUnit").text(), "#openTimeStart-error", "#editOpenTimeEnd-error", 1, "#openTimeEnd");
        if(!validateRoomResult) return;
        if (!$("#addMeetingRoomFrom").valid()) return;
        startModal("#saveBtn");
        commonObj.requestData(JSON.stringify(commonObj.replaceParam($("#addMeetingRoomFrom").serializeJson())), meetingRoomManageObj.addMeetingRoomUrl, "post", "json", true, function (data) {
            Ladda.stopAll();
            swal({
                title: data.code == 200 ? "成功!" : "失败",
                text: data.code == 200 ? "添加会议室成功！" : data.msg,
                type: data.code == 200 ? "success" : "error",
                html: true
            });
            if(data.code == 200){
                $("#addMeetingRoom").modal("toggle");
                meetingRoomManageObj.initPagerPlugin();
            }
        },true);
    },
    delMeetingRoom: function (id, name) {
        layer.confirm('是否确认删除会议室：'+name+'？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function () {
            layer.closeAll();
            commonObj.requestData({id: id}, meetingRoomManageObj.delMeetingRoomUrl, "post", "json", true, function (data) {
                swal({
                    title: data.code == 200 ? "成功!" : "失败",
                    text: data.code == 200 ? "删除会议室成功！" : data.msg,
                    type: data.code == 200 ? "success" : "error",
                    html: true
                });
                if(data.code == 200){
                    meetingRoomManageObj.initPagerPlugin();
                }
            });
        }, function () {
        });
    },
    updateMeetingRoomFun: function (meetingRoom) {
        if(meetingRoom){
            var onceTime = null;
            var unit = null;
            $("#id").val(meetingRoom.id);
            for(var key in meetingRoom){
                $("#editMeetingRoomFrom").find("[name='"+key+"']").val(meetingRoom[key]);
                if(key == "picPath"){
                    $("#editPicPathPreview").attr("src", meetingRoom.picPath);
                }
                if(key == "onceTime" && meetingRoom[key]){ //如果设置最小单位，则将计算单次最长时间的单位同步设置
                    onceTime = meetingRoom[key];
                }
                if(key == "meetUnit" && meetingRoom[key]){ //如果设置最小单位，则将计算单次最长时间的单位同步设置
                    $("#editUnit").text(meetingRoom[key]);
                    unit = meetingRoom[key] || 30;
                }
                if(key == "enabled" && meetingRoom[key] == 0){
                    $("#editEnabled").prop("checked",true);
                    layui.form.render("checkbox");
                }
                if(key == "auditFlag" && meetingRoom[key] == 1){
                    $("#editAuditFlag").prop("checked",true);
                    layui.form.render("checkbox");
                    $("#editAuditUser").css("display", "block");
                }
                if(key == "auditFlag" && meetingRoom[key] == 0){
                    $("#editAuditFlag").prop("checked",false);
                    layui.form.render("checkbox");
                    $("#editAuditUser").css("display", "none");
                }
                //下拉列表重新渲染
                layui.use(["form", 'element'], function () {
                    layui.form.render();//layui-form
                });
            }
            if(onceTime){
                $("#editNum").val(onceTime / unit);
            }
            layui.use(["form"], function () {
                layui.form.render();
            });
        }
        $("#editMeetingRoom").modal("toggle");
    },
    updateMeetingRoom: function () {
        var validateRoomResult = meetingRoomManageObj.validateOpenTime($("#editOpenTimeStart").val(), $("editUnit").text(), "#editOpenTimeStart-error", "#editOpenTimeEnd-error", 1, "#editOpenTimeEnd"); //校验
        if(!validateRoomResult) return;
        if (!$("#editMeetingRoomFrom").valid()) return;
        startModal("#editBtn");
        var editForm = $("#editMeetingRoomFrom").serializeJson();
        editForm.auditFlag = $("#editAuditFlag").attr("value");
        commonObj.requestData(JSON.stringify(commonObj.replaceParam(editForm)), meetingRoomManageObj.updateMeetingRoomUrl, "post", "json", true, function (data) {
            Ladda.stopAll();
            swal({
                title: data.code == 200 ? "成功!" : "失败",
                text: data.code == 200 ? "修改会议室成功！" : data.msg,
                type: data.code == 200 ? "success" : "error",
                html: true
            });
            if(data.code == 200){
                $("#editMeetingRoom").modal("toggle");
                meetingRoomManageObj.initPagerPlugin();
            }
        },true);
    },
    clickDetailFun: function (id) {
        $("#roomId").val(id);
        //初始化分页组件
        var index  = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#meetingList").html(),
            btn: [],
            area: ['90%', '80%'],
            closeBtn: 1,
            shadeClose: false,
            btn: [],
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                $(layero[0]).find(".layui-layer-btn").css("backgroundColor", "#E2E6EC");
                commonObj.requestData({meetRoomId:id}, meetingRoomManageObj.getMeetingTotal, "post", "json", true, function (data) {
                    if(data && data.code == 200){
                        commonObj.pagerPlus({
                            elem: $(layero[0]).find(".meetingListPager"),
                            count: data.data.total,
                            url: meetingRoomManageObj.listRoomMeetingListUrl,
                            target: $(layero[0]).find(".meetingList"),
                            param: {meetRoomId: id},
                        },meetingRoomManageObj.meetingCallback);

                    }
                });
            }
        });

    },
    exportMeetingList: function () {
        var params = {meetRoomId: $("#roomId").val()};
        location.href = "/meetingRoom/exportMeetingList" + "?" + $.param(params);
    },
    exportMeetingRoomList: function () {
        location.href = "/meetingRoom/exportMeetingRoomList";
    }
}
