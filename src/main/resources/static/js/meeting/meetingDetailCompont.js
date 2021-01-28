/**
 * 会议详情公共方法
 * 1、调用方式：
 *     -> 引包:
 *            <link href="/css/meeting/meetingDetail.css" rel="stylesheet">
 *            <script type="text/javascript" src="/js/meeting/meetingDetailCompont.js"></script>
 * 2、创建实例：
 * new MeetingDetailCompont({
        id: meetingId
    }).render();
 * 3、参数说明：
 *                         id - 会议ID
 *                        url - 会议详情接口地址
 *                orgUserList - 组织人列表
 *               hostUserList - 主持人列表
 *            summaryUserList - 会议纪要人列表
 *             recordUserList - 会议参与人列表
 *              agreeUserList - 同意人列表
 *             refuseUserList - 拒绝人列表
 *        notResponseUserList - 未响应人列表
 *                allUserList - 公司所有人列表
 *         notCompanyUserList - 非公司所有人列表
 *            currentUserType - 当前用户类型
 *              agreeCallback - 同意按钮回调函数
 *             refuseCallback - 拒绝按钮回调函数
 *        meetSummaryCallback - 添加会议纪要按钮回调函数
 *   queryMeetSummaryCallback - 查看会议纪要按钮回调函数
 *         meetRecordCallback - 添加会议记录回调函数
 *    queryMeetRecordCallback - 查看会议记录按钮回调函数
 *           meetTaskCallback - 添加会议任务回调函数
 *      queryMeetTaskCallback - 查看会议任务回调函数
 *                      shade - 默认是0.3透明度的黑色背景（'#000'）。如果你想定义别的颜色，可以shade: [0.8, '#393D49']；如果你不想显示遮罩，可以shade: 0
 *                 shadeClose - 是否点击遮罩层关闭窗口，默认不关闭
 *                     zIndex - 设置弹窗层级，默认：10000
 * 4、如果需要设置弹窗背景颜色，请在对应页面加入样式，例如：
 *    .layui-layer-content{
            overflow: hidden !important;
            background-color: #f7f7f7 !important;
      }
 * */
function MeetingDetailCompont(config) {
    this.id = config.id;
    this.url = "/meeting/editMeeting";
    this.orgUserList = [];
    this.hostUserList = [];
    this.summaryUserList = [];
    this.recordUserList = [];
    this.agreeUserList = [];
    this.refuseUserList = [];
    this.notResponseUserList = [];
    this.allUserList = [];
    this.notCompanyUserList = [];
    this.currentUserType = []; //0-会议参与人员、1-会议组织者、2-会议主持人、5-会议纪要人员
    this.meetingTypeMap = {1:'年会',2:'年中大会',3:'月总结大会',4:'复盘会',5:'业务交流会',6:'部长会',7:'部长月总结大会',8:'部门会',
        9:'述职会',10:'股东会',11:'董事会',12:'战略研讨会',13:'管理经营会',14:'其他临时会'};
    this.agreeCallback = config.agreeCallback;
    this.refuseCallback = config.refuseCallback;
    this.meetSummaryCallback = config.meetSummaryCallback;
    this.queryMeetSummaryCallback = config.queryMeetSummaryCallback;
    this.meetRecordCallback = config.meetRecordCallback;
    this.queryMeetRecordCallback = config.queryMeetRecordCallback;
    this.meetTaskCallback = config.meetTaskCallback;
    this.queryMeetTaskCallback = config.queryMeetTaskCallback;
    this.shade = config.shade || 0.3,
    this.shadeClose = config.shadeClose || false;
    this.zIndex = config.zIndex || 10000;
    this.content = '<div class="rootDiv">\n' +
        '                    <div class="btnDiv">\n' +
        '                        <div id="meeingNotEnd" class="notCloseBtn">\n' +
        '                            <button id="agreeBtn" type="button" class="layui-btn">\n' +
        '                                同意\n' +
        '                            </button>\n' +
        '                            <button id="refuseBtn" type="button" class="layui-btn layui-btn-primary btnCls">\n' +
        '                                拒绝\n' +
        '                            </button>\n' +
        '                        </div>\n' +
        '                        <div id="meeingEnd" class="notCloseBtn" style="display: none;">\n' +
        '                            <button type="button" class="btn btnCls">\n' +
        '                                已结束\n' +
        '                            </button>\n' +
        '                        </div>\n' +
        '                        <div class="btnCloseDiv">\n' +
        '                            <button id="closeBtn" type="button" class="close"><i class="fa fa-close"></i></button>\n' +
        '                        </div>\n' +
        '\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="会议类型">\n' +
        '                            <img src="/img/meeting/leixing.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div id="meetType" class="itemContentDiv meetType itemContentEllipsis" title="">\n' +
        '                            \n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="会议时间">\n' +
        '                            <img src="/img/meeting/shijian.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div id="meetTime" class="itemContentDiv itemContentEllipsis" title="">\n' +
        '                            \n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="会议名称">\n' +
        '                            <img src="/img/meeting/mingcheng.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div id="meetName" class="itemContentDiv itemContentEllipsis" title="">\n' +
        '                            \n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="会议地址">\n' +
        '                            <img src="/img/meeting/didian.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div id="meetAddress" class="itemContentDiv itemContentEllipsis" title="">\n' +
        '                            \n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv" style="height: 100px;">\n' +
        '                        <div class="itemIconDiv" title="参会人员">\n' +
        '                            <img src="/img/meeting/renyuan.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div class="itemContentDiv">\n' +
        '                            <div class="meetingUser">\n' +
        '                                <ul>\n' +
        '                                    <li class="liFontCls liFontActiveCls" id="allUser" title="全部(0)">全部(0)</li>\n' +
        '                                    <li class="liFontCls" id="orgUser" title="组织人(0)">组织人(0)</li>\n' +
        '                                    <li class="liFontCls" id="hostUser" title="主持人(0)">主持人(0)</li>\n' +
        '                                    <li class="liFontCls" id="summaryUser" title="纪要人(0)">纪要人(0)</li>\n' +
        '                                    <li class="liFontCls" id="recordUser" title="参与人(0)">参与人(0)</li>\n' +
        '                                    <li class="liFontCls" id="agreeUser" title="已接受(0)">已接受(0)</li>\n' +
        '                                    <li class="liFontCls" id="refuseUser" title="已拒绝(0)">已拒绝(0)</li>\n' +
        '                                    <li class="liFontCls" id="notResponseUser" title="未响应(0)">未响应(0)</li>\n' +
        '                                    <li class="liFontCls" id="notCompanyUser" title="非公司(0)">非公司(0)</li>\n' +
        '                                </ul>\n' +
        '                            </div>\n' +
        '                            <div id="users" class="meetingUserName">\n' +
        '                                \n' +
        '                            </div>\n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="会议内容">\n' +
        '                            <img src="/img/meeting/neirong.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div id="meetContent" class="itemContentDiv itemContentEllipsis">\n' +
        '                            \n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="关联会议">\n' +
        '                            <img src="/img/meeting/guanlian.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div id="relateMeetName" class="itemContentDiv itemContentEllipsis">\n' +
        '                          \n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="会议纪要">\n' +
        '                            <img src="/img/meeting/huiyijiyao.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div id="meetSummaryDiv" class="itemContentDiv">\n' +
        '                            <div class="itemContentTitle" style="width: 60% !important;">\n' +
        '                               会议纪要\n' +
        '                            </div>\n' +
        '                            <div id="queryMeetSummaryOp" class="itemContentLink" style="width: 20% !important;">\n' +
        '                                查看会议纪要&nbsp;<i class="fa fa-search"></i>\n' +
        '                            </div>\n' +
        '                            <div id="meetSummaryOp" class="itemContentLink" style="width: 20% !important;">\n' +
        '                                添加会议纪要&nbsp;<i class="fa fa-plus"></i>\n' +
        '                            </div>\n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="会议纪要">\n' +
        '                            <img src="/img/meeting/huiyijiyao.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div id="meetRecordDiv" class="itemContentDiv">\n' +
        '                            <div class="itemContentTitle" style="width: 60% !important;">\n' +
        '                                会议记录\n' +
        '                            </div>\n' +
        '                            <div id="queryMeetRecordOp" class="itemContentLink" style="width: 20% !important;">\n' +
        '                                查看会议记录&nbsp;<i class="fa fa-search"></i>\n' +
        '                            </div>\n' +
        '                            <div id="meetRecordOp" class="itemContentLink" style="width: 20% !important;">\n' +
        '                                添加会议记录&nbsp;<i class="fa fa-plus"></i>\n' +
        '                            </div>\n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="会议任务">\n' +
        '                            <img src="/img/meeting/renwu.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div class="itemContentDiv">\n' +
        '                            <div class="itemContentTitle" style="width: 60% !important;">\n' +
        '                                会议任务\n' +
        '                            </div>\n' +
        '                            <div id="queryMeetTaskOp" class="itemContentLink" style="width: 20% !important;">\n' +
        '                                查看会议任务&nbsp;<i class="fa fa-search"></i>\n' +
        '                            </div>\n' +
        '                            <div id="meetTaskOp" class="itemContentLink" style="width: 20% !important;">\n' +
        '                                添加会议任务&nbsp;<i class="fa fa-plus"></i>\n' +
        '                            </div>\n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="itemDiv">\n' +
        '                        <div class="itemIconDiv" title="附件">\n' +
        '                            <img src="/img/meeting/fujian.png" width="18" height="18">\n' +
        '                        </div>\n' +
        '                        <div class="itemContentDiv itemContentEllipsis">\n' +
        '                            <div class="attachmentTitle">附件</div>' +
        '                            <div id="attachment" class="attachmentContent">' +
        '                            </div>\n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                </div>';
}

MeetingDetailCompont.prototype = {
    constructor: MeetingDetailCompont,
    _this: this,
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
    getTimeRange: function (startTime, endTime) {
        if(startTime && endTime){
            var weekMap = {0:"周日",1:"周一",2:"周二",3:"周三",4:"周四",5:"周五",6:"周六"};
            var week = weekMap[new Date(startTime).getDay()];
            return new Date(startTime).format("yyyy年MM月dd日 "+week+" hh:mm") + " ~ " + ("00:00" == new Date(endTime).format("hh:mm") ? "24:00" : new Date(endTime).format("hh:mm"));
        }else{
            return "";
        }
    },
    render: function () {
        var _this = this;
        var $parentContent = $("<div></div>");
        $parentContent.html(_this.content);

        //处理会议详情返回值
        _this.requestData({id: _this.id}, _this.url, "get", "json", false, function (data) {
            if(data.code == 200 && data.data){
                //统计非公司人员
                if(data.data.map){
                    if(data.data.map.otherHost && data.data.map.otherHost.length > 0){
                        $.each(data.data.map.otherHost, function (i, userName) {
                            _this.notCompanyUserList.push(userName);
                        });
                    }
                    if(data.data.map.otherOrganizer && data.data.map.otherOrganizer.length > 0){
                        $.each(data.data.map.otherOrganizer, function (i, userName) {
                            _this.notCompanyUserList.push(userName);
                        });
                    }
                    if(data.data.map.otherPark && data.data.map.otherPark.length > 0){
                        $.each(data.data.map.otherPark, function (i, userName) {
                            _this.notCompanyUserList.push(userName);
                        });
                    }
                }
                //分类统计公司人员
                if(data.data.list && data.data.list.length > 0){
                    $.each(data.data.list, function (i, tempUser) {
                        //排除已添加的
                        if(!_this.allUserList.contains(tempUser.name)){
                            _this.allUserList.push(tempUser.name);
                        }

                        //是否接受会议：0-未响应、1-接受，2-拒绝
                        if(tempUser.acceptFlag == 1){
                            if(!_this.agreeUserList.contains(tempUser.name)){
                                _this.agreeUserList.push(tempUser.name);
                            }
                        }else if(tempUser.acceptFlag == 2){
                            if(!_this.refuseUserList.contains(tempUser.name)){
                                _this.refuseUserList.push(tempUser.name);
                            }
                        }else {
                            if(!_this.notResponseUserList.contains(tempUser.name)){
                                _this.notResponseUserList.push(tempUser.name);
                            }
                        }
                       //0-会议参与人员、1-会议组织者、2-会议主持人、5-会议纪要人员
                       if(tempUser.userType == 1){
                           if(!_this.orgUserList.contains(tempUser.name)){
                               _this.orgUserList.push(tempUser.name);
                           }
                       }else if(tempUser.userType == 2){
                           if(!_this.hostUserList.contains(tempUser.name)){
                               _this.hostUserList.push(tempUser.name);
                           }
                       }else if(tempUser.userType == 5){
                           if(!_this.summaryUserList.contains(tempUser.name)){
                               _this.summaryUserList.push(tempUser.name);
                           }
                       }else {
                           if(!_this.recordUserList.contains(tempUser.name)){
                               _this.recordUserList.push(tempUser.name);
                           }
                       }
                       //判断当前用户类型:0-会议参与人员、1-会议组织者、2-会议主持人、5-会议纪要人员
                       if(user.id == tempUser.userId && tempUser.meetState ==1){
                           if(!_this.currentUserType.contains(tempUser.userType)){
                               _this.currentUserType.push(tempUser.userType);
                           }
                           if(tempUser.userType == 1 || tempUser.userType == 2 || tempUser.userType == 5){
                               $parentContent.find("#agreeBtn").text("已接受");
                               $parentContent.find("#agreeBtn").attr("clickFlag", 0);
                               $parentContent.find("#refuseBtn").css("display", "none");
                           }else{
                               $parentContent.find("#refuseBtn").css("display", "inline-block");
                               if(tempUser.acceptFlag == 1){
                                   $parentContent.find("#agreeBtn").attr("clickFlag", 0);
                                   $parentContent.find("#agreeBtn").text("已接受");
                                   $parentContent.find("#refuseBtn").attr("clickFlag", 1);
                                   $parentContent.find("#refuseBtn").text("拒绝");
                               } else if(tempUser.acceptFlag == 2){
                                   $parentContent.find("#refuseBtn").attr("clickFlag", 0);
                                   $parentContent.find("#refuseBtn").text("已拒绝");
                                   $parentContent.find("#agreeBtn").attr("clickFlag", 1);
                                   $parentContent.find("#agreeBtn").text("同意");
                               }else {
                                   $parentContent.find("#agreeBtn").attr("clickFlag", 1);
                                   $parentContent.find("#agreeBtn").text("同意");
                                   $parentContent.find("#refuseBtn").attr("clickFlag", 1);
                                   $parentContent.find("#refuseBtn").text("拒绝");
                               }
                           }
                       }
                    });
                }
                //信息赋值
                var entity = data.data.entity;
                if(entity){
                    if(entity.endTime <= new Date()){
                        $parentContent.find("#meeingNotEnd").css("display", "none");
                        $parentContent.find("#meeingEnd").css("display", "block");
                    }else{
                        $parentContent.find("#meeingNotEnd").css("display", "block");
                        $parentContent.find("#meeingEnd").css("display", "none");
                    }
                    $parentContent.find("#meetType").text(_this.meetingTypeMap[entity.meetType]);
                    $parentContent.find("#meetType").attr("title",_this.meetingTypeMap[entity.meetType]);

                    $parentContent.find("#meetTime").text(_this.getTimeRange(entity.startTime, entity.endTime));
                    $parentContent.find("#meetTime").attr("title",_this.getTimeRange(entity.startTime, entity.endTime));

                    $parentContent.find("#meetName").text(entity.title);
                    $parentContent.find("#meetName").attr("title",entity.title);

                    if (entity.meetRoomId){
                        $parentContent.find("#meetAddress").text(entity.meetingRoom.address);
                        $parentContent.find("#meetAddress").attr("title",entity.meetingRoom.address);
                    }else {
                        $parentContent.find("#meetAddress").text(entity.address);
                        $parentContent.find("#meetAddress").attr("title",entity.address);
                    }

                    $parentContent.find("#orgUser").text("组织人("+_this.orgUserList.length+")");
                    $parentContent.find("#orgUser").attr("title","组织人("+_this.orgUserList.length+")");
                    $parentContent.find("#hostUser").text("主持人("+_this.hostUserList.length+")");
                    $parentContent.find("#hostUser").attr("title","主持人("+_this.hostUserList.length+")");
                    $parentContent.find("#summaryUser").text("纪要人("+_this.summaryUserList.length+")");
                    $parentContent.find("#summaryUser").attr("title","纪要人("+_this.summaryUserList.length+")");
                    $parentContent.find("#recordUser").text("参与人("+_this.recordUserList.length+")");
                    $parentContent.find("#recordUser").attr("title","参与人("+_this.recordUserList.length+")");
                    $parentContent.find("#agreeUser").text("已接受("+_this.agreeUserList.length+")");
                    $parentContent.find("#agreeUser").attr("title","已接受("+_this.agreeUserList.length+")");
                    $parentContent.find("#refuseUser").text("已拒绝("+_this.refuseUserList.length+")");
                    $parentContent.find("#refuseUser").attr("title","已拒绝("+_this.refuseUserList.length+")");
                    $parentContent.find("#notResponseUser").text("未响应("+_this.notResponseUserList.length+")");
                    $parentContent.find("#notResponseUser").attr("title","未响应("+_this.notResponseUserList.length+")");
                    $parentContent.find("#allUser").text("全部("+_this.allUserList.length+")");
                    $parentContent.find("#allUser").attr("title","全部("+_this.allUserList.length+")");
                    $parentContent.find("#notCompanyUser").text("非公司("+_this.notCompanyUserList.length+")");
                    $parentContent.find("#notCompanyUser").attr("title","非公司("+_this.notCompanyUserList.length+")");
                    $parentContent.find("#users").html(_this.allUserList.join(",&nbsp;")); //默认展示全部人员

                    $parentContent.find("#meetContent").text(entity.content);
                    $parentContent.find("#meetContent").attr("title",entity.content);

                    if(data.data.relateMeeting){
                        $parentContent.find("#relateMeetName").text(data.data.relateMeeting.title);
                        $parentContent.find("#relateMeetName").attr("title",data.data.relateMeeting.title);
                    }

                    //如果仅仅是会议纪要人员，则仅可填写会议纪要，否则可能填写会议纪要或者记录
                    if(_this.currentUserType.contains(5) && _this.currentUserType.length == 1){
                        $parentContent.find("#meetSummaryOp").css("display", "block");
                        $parentContent.find("#meetRecordOp").css("display", "none");
                    }else if(_this.currentUserType.contains(5)){
                        $parentContent.find("#meetSummaryOp").css("display", "block");
                        $parentContent.find("#meetRecordOp").css("display", "block");
                    }else {
                        $parentContent.find("#meetSummaryOp").css("display", "none");
                        $parentContent.find("#meetRecordOp").css("display", "block");
                    }

                    if(_this.currentUserType.contains(1)){
                        $parentContent.find("#meetTaskOp").css("display", "block");
                    }else{
                        $parentContent.find("#meetTaskOp").css("display", "none");
                    }

                    if(entity.attachmentName && entity.attachmentLink){
                        var nameArr = entity.attachmentName.split(",");
                        var linkArr = entity.attachmentLink.split(",");
                        var strFilter = ".jpeg|.gif|.jpg|.png|.bmp|.pic|";
                        var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                        var html = "";
                        for(var j=0; j < linkArr.length; j++){
                            var fileName = nameArr[j];
                            var filePath = linkArr[j];
                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                            html += '<div class="attachmentOp" title="'+fileName+'">';
                            html += '   <a href="'+filePath+'" target="_blank">下载</a>&nbsp;&nbsp;';
                            // html += '   <a href="javascript:;" onclick="filePreview(this);" data-id="'+linkArr[j]+'">预览</a>';
                            if (fileName.indexOf(".") > -1) {
                                var str = fileExt + '|';
                                if (strFilter.indexOf(str) > -1) {//是图片
                                    html += "<img alt='" + fileName + "' src='" + filePath + "' height='32px' width='30px' onclick='openImage(this,\"imgModal\")'><br/>";
                                } else {
                                    if(fileExtArray.contains(fileExt)){
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览</a><br/>";
                                    }
                                }
                            } else {
                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览</a><br/>";
                            }
                            html += '</div>';
                        }
                         $parentContent.find("#attachment").html(html);
                    }
                }

                //如果当前操作人，不涉及当前会议，则不允许有任何操作
                if(_this.currentUserType.length < 1){
                    $parentContent.find("#meeingNotEnd").css("display", "none");
                    $parentContent.find("#meeingEnd").css("display", "none");
                    $parentContent.find("#meetSummaryOp").css("display", "none");
                    $parentContent.find("#meetRecordOp").css("display", "none");
                    $parentContent.find("#meetTaskOp").css("display", "none");
                }

                var index  = layer.open({
                    type: 1,
                    title: false,
                    zIndex: _this.zIndex,
                    content: $parentContent.html(),
                    btn: [],
                    area: ['645px', '590px'],
                    closeBtn: 0,
                    shade: _this.shade,
                    shadeClose: _this.shadeClose,
                    btn: [],
                    resize: false,
                    move: '.layui-layer-btn',
                    moveOut: true,
                    success: function(layero, index){
                        $(layero[0]).find(".layui-layer-btn").css("backgroundColor", "#E2E6EC");
                        //关闭按钮
                        $(layero[0]).find("#closeBtn").click(function () {
                            layer.close(index);
                        });
                        //同意按钮
                        $(layero[0]).find("#agreeBtn").click(function () {
                            if($(this).attr("clickFlag") == 1 || $(this).attr("clickFlag") == "1"){
                                if(_this.agreeCallback){
                                    if(_this.agreeCallback(data)){
                                        $(layero[0]).find("#agreeBtn").attr("clickFlag", 0);
                                        $(layero[0]).find("#agreeBtn").text("已接受");
                                        $(layero[0]).find("#refuseBtn").attr("clickFlag", 1);
                                        $(layero[0]).find("#refuseBtn").text("拒绝");
                                    }
                                }
                            }
                        });
                        //拒绝按钮
                        $(layero[0]).find("#refuseBtn").click(function () {
                            if($(this).attr("clickFlag") == 1 || $(this).attr("clickFlag") == "1"){
                                if(_this.refuseCallback){
                                    if(_this.refuseCallback(data)){
                                        $(layero[0]).find("#refuseBtn").attr("clickFlag", 0);
                                        $(layero[0]).find("#refuseBtn").text("已拒绝");
                                        $(layero[0]).find("#agreeBtn").attr("clickFlag", 1);
                                        $(layero[0]).find("#agreeBtn").text("接受");
                                    }
                                }
                            }
                        });
                        //参与人员Tab
                        $(layero[0]).find("li").click(function (e) {
                            var liId = $(e.currentTarget).attr("id");
                            $(e.currentTarget).parent().find("li").removeClass("liFontActiveCls");
                            $(e.currentTarget).addClass("liFontActiveCls");
                            if(liId == "orgUser"){
                                $(e.currentTarget).text("组织人("+_this.orgUserList.length+")");
                                $(e.currentTarget).attr("title","组织人("+_this.orgUserList.length+")");
                                $(layero[0]).find("#users").html(_this.orgUserList.join(",&nbsp;"));
                            }
                            if(liId == "hostUser"){
                                $(e.currentTarget).text("主持人("+_this.hostUserList.length+")");
                                $(e.currentTarget).attr("title","主持人("+_this.hostUserList.length+")");
                                $(layero[0]).find("#users").html(_this.hostUserList.join(",&nbsp;"));
                            }
                            if(liId == "summaryUser"){
                                $(e.currentTarget).text("纪要人("+_this.summaryUserList.length+")");
                                $(e.currentTarget).attr("title","纪要人("+_this.summaryUserList.length+")");
                                $(layero[0]).find("#users").html(_this.summaryUserList.join(",&nbsp;"));
                            }
                            if(liId == "recordUser"){
                                $(e.currentTarget).text("参与人("+_this.recordUserList.length+")");
                                $(e.currentTarget).attr("title","参与人("+_this.recordUserList.length+")");
                                $(layero[0]).find("#users").html(_this.recordUserList.join(",&nbsp;"));
                            }
                            if(liId == "agreeUser"){
                                $(e.currentTarget).text("已接受("+_this.agreeUserList.length+")");
                                $(e.currentTarget).attr("title","已接受("+_this.agreeUserList.length+")");
                                $(layero[0]).find("#users").html(_this.agreeUserList.join(",&nbsp;"));
                            }
                            if(liId == "refuseUser"){
                                $(e.currentTarget).text("已拒绝("+_this.refuseUserList.length+")");
                                $(e.currentTarget).attr("title","已拒绝("+_this.refuseUserList.length+")");
                                $(layero[0]).find("#users").html(_this.refuseUserList.join(",&nbsp;"));
                            }
                            if(liId == "notResponseUser"){
                                $(e.currentTarget).text("未响应("+_this.notResponseUserList.length+")");
                                $(e.currentTarget).attr("title","未响应("+_this.notResponseUserList.length+")");
                                $(layero[0]).find("#users").html(_this.notResponseUserList.join(",&nbsp;"));
                            }
                            if(liId == "allUser"){
                                $(e.currentTarget).text("全部("+_this.allUserList.length+")");
                                $(e.currentTarget).attr("title","全部("+_this.allUserList.length+")");
                                $(layero[0]).find("#users").html(_this.allUserList.join(",&nbsp;"));
                            }
                            if(liId == "notCompanyUser"){
                                $(e.currentTarget).text("非公司("+_this.notCompanyUserList.length+")");
                                $(e.currentTarget).attr("title","非公司("+_this.notCompanyUserList.length+")");
                                $(layero[0]).find("#users").html(_this.notCompanyUserList.join(",&nbsp;"));
                            }
                        });
                        //添加会议纪要按钮
                        $(layero[0]).find("#meetSummaryOp").click(function () {
                            if(_this.meetSummaryCallback){
                                if(_this.meetSummaryCallback(data)){

                                }
                            }
                        });
                        //查看会议纪要按钮
                        $(layero[0]).find("#queryMeetSummaryOp").click(function () {
                            if(_this.queryMeetSummaryCallback){
                                if(_this.queryMeetSummaryCallback(data)){

                                }
                            }
                        });
                        //添加会议记录按钮
                        $(layero[0]).find("#meetRecordOp").click(function () {
                            if(_this.meetRecordCallback){
                                if(_this.meetRecordCallback(data)){

                                }
                            }
                        });
                        //查看会议记录按钮
                        $(layero[0]).find("#queryMeetRecordOp").click(function () {
                            if(_this.queryMeetRecordCallback){
                                if(_this.queryMeetRecordCallback(data)){

                                }
                            }
                        });
                        //添加会议任务按钮
                        $(layero[0]).find("#meetTaskOp").click(function () {
                            if(_this.meetTaskCallback){
                                if(_this.meetTaskCallback(data)){

                                }
                            }
                        });
                        //查看会议任务按钮
                        $(layero[0]).find("#queryMeetTaskOp").click(function () {
                            if(_this.queryMeetTaskCallback){
                                if(_this.queryMeetTaskCallback(data)){

                                }
                            }
                        });
                    }
                });
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5})
            }
        });
    }
};