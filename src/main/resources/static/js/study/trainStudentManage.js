//页面初始化函数
$(function () {
    //获取权限
    commonObj.getTrainPermission();

    //控制栏目
    commonObj.controlTab();

    //下拉列表渲染
    commonObj.initBefore();

    //默认选择第一个tab
    commonObj.tabChange(0);
});

//培训学员管理
var trainStudentInfoObj = {
    trainStudentListUrl:"/trainStudent/listStudent",
    courseState:{
        "all":"全部",
        0:{name:"正常", cls:"background-43B9FF"},
        1:{name:"迟到", cls:"background-EF7571"},
        2:{name:"早退", cls:"background-EF7571"},
        3:{name:"旷课", cls:"background-EF7571"},
        4:{name:"取消报名", cls:"background-00A2FF"},
        5:{name:"已结束", cls:"background-FFFFFF"}
    },
    studentCourseList:[],//缓存学生课程列表，对于课程详情模态框/学员角色，每次打开模态框/重新打开页面发送请求查询所有，tab切换不进行发送请求，进行数据筛选渲染即可
    cancelSignIdList:[],//取消报名ID集合
    getDateStr:function (startTime, endTime, pattern) {
        var r = "";
        if(startTime){
            r += new Date(startTime).format(pattern);
        }else {
            r += "∞ ";
        }
        if(endTime){
            r += "~" + new Date(endTime).format(pattern);
        }else {
            r += " ∞";
        }
        if(!startTime && !endTime){
            r = "-";
        }
        return r;
    },
    trainStudentListCallback:function (data, target,seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                var userData = commonObj.userMap[record.userId] || {};
                var signNum = record.signNum || 0;
                var completeTime = signNum == 0 ? 0 : ((record.completeTime || 0)/signNum).toFixed(2);
                var completeRate = signNum == 0 ? 0 : ((record.completeRate || 0)/signNum).toFixed(2);
                var integral = record.integral || 0;
                var examNum = record.examNum || 0;
                seq += 1;
                html += "<tr>\n" +
                    "        <td title=\""+seq+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+seq+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(userData.name || "")+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(userData.name || "-")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(userData.deptName || "")+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(userData.deptName || "-")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(userData.postName || "")+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(userData.postName || "-")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+completeRate+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+completeRate+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+completeTime+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+completeTime+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+examNum+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+examNum+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+integral+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+integral+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "    </tr>";
            });
        }
        var $parentList = target ? $(target) : $(".trainStudentList");
        $parentList.html(html);
    },
    init: function () {
        $(".trainStudentList").html("");//重置
        $(".trainStudentList").html("");//重置
        trainStudentInfoObj.cancelSignIdList = [];//重置
        trainStudentInfoObj.studentCourseList = [];//重置
        $(".contentOuter").find(".courseList").html("");//重置课程列表
        //学员信息
        commonObj.requestData({userId:user.id}, trainStudentInfoObj.trainStudentListUrl, "post", "json", true, function (data) {
            if(data && data.list && data.list.length > 0){
                trainStudentInfoObj.trainStudentListCallback(data, null, 0);
                //课程列表
                commonObj.requestData({userId:user.id}, "/trainStudent/listSignUpCourse", "post", "json", true, function (data) {
                    trainStudentInfoObj.studentCourseList = data;
                    trainStudentInfoObj.studentfilterCourseItem($(".contentOuter").find(".courseList"), "all");//渲染，默认展示全部
                });
            }else {
                $(".trainStudentList").html("<tr></tr>");//空行
            }
        });
    },
    tabChange:function (t) {
        var $courseTabWrap = $(t).closest(".courseTabWrap");
        $courseTabWrap.find("span").removeClass("courceTabActive");
        $(t).addClass("courceTabActive");
        trainStudentInfoObj.studentfilterCourseItem($courseTabWrap.closest(".contentOuter").find(".courseList"), $(t).attr("data-type"));
    },
    getCourseState:function (courseSign) {
        var signState = courseSign.state; //报名状态
        //报名状态正常
        if(signState == 0){
            if(new Date(courseSign.trainCourse.trainStartTime) > new Date()){
                signState = 4;//未开始-取消报名
            }
            if(new Date(courseSign.trainCourse.trainEndTime) < new Date()){
                signState = 5;//已结束
            }
        }
        return signState;
    },
    studentRenderCourseItem:function (courseSign) {
        var html = "";
        var stateHtml = "<span class=\"courseStateBtn "+trainStudentInfoObj.courseState[courseSign.courseState].cls+"\">"+trainStudentInfoObj.courseState[courseSign.courseState].name+"</span>\n";
        if(courseSign.courseState == 4){
            stateHtml = "<span onclick='trainStudentInfoObj.cancelCourseSign("+JSON.stringify(courseSign)+")' class=\"courseStateBtn "+trainStudentInfoObj.courseState[courseSign.courseState].cls+"\">"+trainStudentInfoObj.courseState[courseSign.courseState].name+"</span>\n";
        }
        if(courseSign){
           html = "<div class=\"courseItem\">\n" +
               "       <!--课程图片-->\n" +
               "       <div onclick='trainStudentInfoObj.courseDetailClick("+courseSign.trainCourse.id+")' class=\"coursePic\" style='background-image: url("+(courseSign.trainCourse.coursePic || "/img/train/course_pic_default.png")+");'></div>\n" +
               "       <!--课程内容-->\n" +
               "       <div onclick='trainStudentInfoObj.courseDetailClick("+courseSign.trainCourse.id+")' class=\"courseContentWrap\">\n" +
               "           <div class=\"courseItemWrap\">\n" +
               "               <div class=\"coursePlate\" title=\""+commonObj.coursePlateMap[courseSign.trainCourse.coursePlate]+"\">\n" +
               "                   <div class=\"ellipsisContent-100\">\n" +
               "                       "+commonObj.coursePlateMap[courseSign.trainCourse.coursePlate]+"\n" +
               "                   </div>\n" +
               "               </div>\n" +
               "               <div class=\"courseTitle\" title=\""+courseSign.trainCourse.title+"\">\n" +
               "                   <div class=\"ellipsisContent-100\">\n" +
               "                       "+courseSign.trainCourse.title+"\n" +
               "                   </div>\n" +
               "               </div>\n" +
               "           </div>\n" +
               "           <div class=\"courseItemWrap\">\n" +
               "               <div class=\"courseSignTime\" title=\""+trainStudentInfoObj.getDateStr(courseSign.trainCourse.trainStartTime, courseSign.trainCourse.trainEndTime, "yyyy.MM.dd hh:mm")+"\">\n" +
               "                   <i class=\"fa fa-calendar-minus-o\"></i>&nbsp;\n" +
               "                   <div class=\"ellipsisContent-100\">"+trainStudentInfoObj.getDateStr(courseSign.trainCourse.trainStartTime, courseSign.trainCourse.trainEndTime, "yyyy.MM.dd hh:mm")+"</div>\n" +
               "               </div>\n" +
               "               <div class=\"courseAddress\" title=\""+courseSign.trainCourse.trainAddress+"\">\n" +
               "                   <i class=\"fa fa-map-marker\"></i>&nbsp;\n" +
               "                   <div class=\"ellipsisContent-100\">\n" +
               "                       "+courseSign.trainCourse.trainAddress+"\n" +
               "                   </div>\n" +
               "               </div>\n" +
               "           </div>\n" +
               "           <div class=\"courseItemWrap\">\n" +
               "               <span>讲师：</span>\n" +
               "               <span class=\"courseTeacherName\">"+commonObj.userMap[courseSign.trainCourse.createId].name+"</span>\n" +
               "           </div>\n" +
               "       </div>\n" +
               "       <!--课程按钮-->\n" +
               "       <div class=\"courseBtnWrap\">\n" +
               "           "+stateHtml+"\n" +
               "       </div>\n" +
               "   </div>";
        }
        return html;
    },
    studentfilterCourseItem:function ($target, type) {
        //筛选
        var html = "";
        if(trainStudentInfoObj.studentCourseList && trainStudentInfoObj.studentCourseList.length > 0){
            //筛选
            $.each(trainStudentInfoObj.studentCourseList, function (l, courseSign) {
                if(type == "all"){
                    courseSign.courseState = trainStudentInfoObj.getCourseState(courseSign);
                    html += trainStudentInfoObj.studentRenderCourseItem(courseSign);
                }else {
                    if(trainStudentInfoObj.getCourseState(courseSign) == type){
                        courseSign.courseState = type;
                        html += trainStudentInfoObj.studentRenderCourseItem(courseSign);
                    }
                }
            });
        }
        $target.html(html);
    },
    courseDetailClick: function (courseId) {
        //课程详情
        coursePreviewObj.coursePreviewModalShow({id:courseId});
    },
    cancelCourseSign:function (courseSign) {
        if(!trainStudentInfoObj.cancelSignIdList.contains(courseSign.id)){
            trainStudentInfoObj.cancelSignIdList.push(courseSign.id);//禁用
            commonObj.requestData({signId: courseSign.id}, "/trainStudent/cancelCourseSign", "post", "json", true, function (data) {
                if(data.code == 200){
                    layer.msg("取消课程报名成功", {time: 3000, icon: 6});
                    trainStudentInfoObj.init();
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                    trainStudentInfoObj.cancelSignIdList.remove(courseSign.id);//启用
                }
            }, function () {
                trainStudentInfoObj.cancelSignIdList.remove(courseSign.id);//启用
            });
        }else {
            layer.msg("已取消课程报名", {time: 3000, icon: 6});
        }
    }
}

//培训学员管理
var trainStudentManageObj = {
    getTotalUrl:"/trainStudent/getStudentTotal",
    trainStudentListUrl:"/trainStudent/listStudent",
    courseListModalIndex: 0,
    signState:{
        "all":"全部",
        0:{name:"正常", cls:"background-43B9FF"},
        1:{name:"迟到", cls:"background-EF7571"},
        2:{name:"早退", cls:"background-EF7571"},
        3:{name:"旷课", cls:"background-EF7571"}
    },
    studentCourseList:[],//缓存学生课程列表，对于课程详情模态框/学员角色，每次打开模态框/重新打开页面发送请求查询所有，tab切换不进行发送请求，进行数据筛选渲染即可
    getDateStr:function (startTime, endTime, pattern) {
        var r = "";
        if(startTime){
            r += new Date(startTime).format(pattern);
        }else {
            r += "∞ ";
        }
        if(endTime){
            r += "~" + new Date(endTime).format(pattern);
        }else {
            r += " ∞";
        }
        if(!startTime && !endTime){
            r = "-";
        }
        return r;
    },
    trainStudentListCallback:function (data, target,seq) {
        var html = "";
        $("#studentListExportBtn").css("display", "none");
        if(data && data.list && data.list.length > 0){
            $("#studentListExportBtn").css("display", "inline-block");//有数据展示导出按钮
            $.each(data.list, function (i, record) {
                var userData = commonObj.userMap[record.userId] || {};
                var signNum = record.signNum || 0;
                var completeTime = signNum == 0 ? 0 : ((record.completeTime || 0)/signNum).toFixed(2);
                var completeRate = signNum == 0 ? 0 : ((record.completeRate || 0)/signNum).toFixed(2);
                var integral = record.integral || 0;
                var examNum = record.examNum || 0;
                seq += 1;
                html += "<tr>\n" +
                    "        <td title=\""+seq+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+seq+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(userData.name || "")+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(userData.name || "-")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(userData.deptName || "")+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(userData.deptName || "-")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(userData.postName || "")+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(userData.postName || "-")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+completeRate+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+completeRate+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+completeTime+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+completeTime+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+examNum+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+examNum+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+integral+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+integral+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                <button class=\"tableButton blueBtn\" type=\"button\" onclick=\"trainStudentManageObj.courseListModalShow("+record.userId+");\">\n" +
                    "                    课程详情\n" +
                    "                </button>\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "    </tr>";
            });
        }
        var $parentList = target ? $(target) : $(".trainStudentList");
        $parentList.html(html);
    },
    init: function () {
        //如果是讲师，则只能查看自己的学员，管理员可以查看所有学员
        var param = $("#trainStudentForm").serializeJson();
        if(!commonObj.isAdmin){
            param.teacherId = user.id;
        }
        //初始化分页组件
        commonObj.requestData(param,trainStudentManageObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".trainStudentListPager"),
                    count: data.data.total,
                    url: trainStudentManageObj.trainStudentListUrl,
                    target: $(".trainStudentList"),
                    param: param,
                },trainStudentManageObj.trainStudentListCallback);
            }
        });
    },
    tabChange:function (t) {
        var $courseTabWrap = $(t).closest(".courseTabWrap");
        $courseTabWrap.find("span").removeClass("courceTabActive");
        $(t).addClass("courceTabActive");
        trainStudentManageObj.filterCourseItem($courseTabWrap.closest(".trainModalCommon").find(".courseList"), $(t).attr("data-type"));
    },
    renderCourseItem:function (courseSign) {
        var html = "";
        if(courseSign){
            html = "<div class=\"courseItem\" style=\"height: 130px;\" onclick='trainStudentManageObj.courseDetailClick("+courseSign.trainCourse.id+");'>\n" +
                "       <!--课程图片-->\n" +
                "       <div class=\"coursePic\" style='width: 214px;height: 120px;background-image: url("+(courseSign.trainCourse.coursePic || "/img/train/course_pic_default.png")+");'></div>\n" +
                "       <!--课程内容-->\n" +
                "       <div class=\"courseContentWrap\">\n" +
                "           <div class=\"courseItemWrap\">\n" +
                "               <div class=\"coursePlate\" title=\""+commonObj.coursePlateMap[courseSign.trainCourse.coursePlate]+"\" style=\"max-width: 140px;\">\n" +
                "                   <div class=\"ellipsisContent-100\">\n" +
                "                       "+commonObj.coursePlateMap[courseSign.trainCourse.coursePlate]+"\n" +
                "                   </div>\n" +
                "               </div>\n" +
                "               <div class=\"courseTitle\" title=\""+courseSign.trainCourse.title+"\">\n" +
                "                   <div class=\"ellipsisContent-100\">\n" +
                "                       "+courseSign.trainCourse.title+"\n" +
                "                   </div>\n" +
                "               </div>\n" +
                "           </div>\n" +
                "           <div class=\"courseItemWrap\">\n" +
                "               <div class=\"courseScore\">\n" +
                "                   <span data-score='"+courseSign.score+"' class=\"cousreTotalStar\"></span>\n" +
                "               </div>\n" +
                "               <div class=\"courseLike\">\n" +
                "                   <i class=\"fa fa-thumbs-up\"></i>&nbsp;\n" +
                "                   <div>点赞数</div>&nbsp;\n" +
                "                   <div class=\"courseLikeNum\">"+courseSign.likeFlag+"</div>\n" +
                "               </div>\n" +
                "               <div class=\"courseVent\">\n" +
                "                   <i class=\"fa fa-thumbs-down\"></i>&nbsp;\n" +
                "                   <div>吐槽数</div>&nbsp;\n" +
                "                   <div class=\"courseVentNum\">"+courseSign.ventFlag+"</div>\n" +
                "               </div>\n" +
                "               <div class=\"intergal\" title=\"积分\">\n" +
                "                   <i class=\"fa fa-ils\"></i>&nbsp;\n" +
                "                   <div>积分</div>&nbsp;\n" +
                "                   <div class=\"intergal\">"+courseSign.integral+"</div>\n" +
                "               </div>\n" +
                "           </div>\n" +
                "           <div class=\"courseItemWrap\">\n" +
                "               <div class=\"courseSignTime\" title=\""+trainStudentManageObj.getDateStr(courseSign.trainCourse.trainStartTime, courseSign.trainCourse.trainEndTime, "yyyy.MM.dd hh:mm")+"\" style=\"max-width: 140px;\">\n" +
                "                   <i class=\"fa fa-calendar-minus-o\"></i>&nbsp;\n" +
                "                   <div class=\"ellipsisContent-100\">"+trainStudentManageObj.getDateStr(courseSign.trainCourse.trainStartTime, courseSign.trainCourse.trainEndTime, "yyyy.MM.dd hh:mm")+"</div>\n" +
                "               </div>\n" +
                "               <div class=\"courseAddress\" title=\""+courseSign.trainCourse.trainAddress+"\">\n" +
                "                   <i class=\"fa fa-map-marker\"></i>&nbsp;\n" +
                "                   <div class=\"ellipsisContent-100\">\n" +
                "                       "+courseSign.trainCourse.trainAddress+"\n" +
                "                   </div>\n" +
                "               </div>\n" +
                "           </div>\n" +
                "           <div class=\"courseItemWrap\">\n" +
                "               <span>讲师：</span>\n" +
                "               <span class=\"courseTeacherName\">"+commonObj.userMap[courseSign.trainCourse.createId].name+"</span>\n" +
                "           </div>\n" +
                "       </div>\n" +
                "       <!--课程按钮-->\n" +
                "       <div class=\"courseBtnWrap\">\n" +
                "           <span class=\"courseStateBtn "+trainStudentManageObj.signState[courseSign.state].cls+"\">"+trainStudentManageObj.signState[courseSign.state].name+"</span>\n" +
                "       </div>\n" +
                "   </div>";
        }
        return html;
    },
    filterCourseItem: function ($target, type) {
        var html = "";
        if(trainStudentManageObj.studentCourseList && trainStudentManageObj.studentCourseList.length > 0){
            //筛选
            $.each(trainStudentManageObj.studentCourseList, function (l, courseSign) {
                if(type == "all"){
                    html += trainStudentManageObj.renderCourseItem(courseSign);
                }else {
                    if(courseSign.state == type){
                        html += trainStudentManageObj.renderCourseItem(courseSign);
                    }
                }
            });
        }
        $target.html(html);

        //渲染课程评分
        layui.use('rate', function(){
            var rate = layui.rate;
            $target.find(".courseItem").each(function (k, courseItem) {
                rate.render({
                    elem: $(courseItem).find(".cousreTotalStar"),
                    half: true, //开启半星
                    readonly:true,
                    value:($(courseItem).find(".cousreTotalStar").attr("data-score") || 0) / 20
                });
            });
        });
    },
    courseListModalShow:function (studentId) {
        trainStudentManageObj.courseListModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#courseListModal").html(),
            btn: [],
            area: ['65%', '90%'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                trainStudentManageObj.studentCourseList = [];//清空

                var param = {userId:studentId};
                //判断是否是讲师 还是 管理员，如果是讲师则只看自己涉及到的学员，学员完成率等数据也只统计和自己课程相关的
                if(!commonObj.isAdmin){
                    param.teacherId = user.id
                }
                commonObj.requestData(param, "/trainStudent/listSignUpCourse", "post", "json", true, function (data) {
                    trainStudentManageObj.studentCourseList = data;
                    trainStudentManageObj.filterCourseItem($(layero[0]).find(".courseList"), "all");//渲染，默认展示全部
                });
            }
        });
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    courseListModalClose:function () {
        if(trainStudentManageObj.courseListModalIndex){
            layer.close(trainStudentManageObj.courseListModalIndex);
        }else {
            layer.closeAll();
        }
    },
    courseDetailClick: function (courseId) {
        //课程详情
        coursePreviewObj.coursePreviewModalShow({id:courseId});
    },
    trainStudentExport: function () {
        //如果是讲师，则只能查看自己的学员，管理员可以查看所有学员
        var param = $("#trainStudentForm").serializeJson();
        if (!commonObj.isAdmin) {
            param.teacherId = user.id;
        }
        //如果有数据则导出，否则提示
        if ($(".trainStudentList").find("tr").length > 0) {
            location.href = baseUrl + "/trainStudent/trainStudentExport?" + $.param(param);
        } else {
            layer.msg("没有学员列表数据，不能进行导出！", {time: 3000, icon: 5});
        }
    }
}

//课程详情/预览
var coursePreviewObj = {
    coursePreviewModalIndex: 0, //课程预览模态框
    currentCourseId: null, //记录当前课程ID
    courseState:{0:"待审核", 1:"有效", 2:"未报名", 3:"停课", 4:"审核拒绝"},
    courseSignMap: {}, //课程报名数据
    likeSubmitFlag: true, //点赞按钮提交标识，防止双击重复提交
    ventSubmitFlag: true, //吐槽按钮提交标识，防止双击重复提交
    scoreSubmitFlag: true, //评分按钮提交标识，防止双击重复提交
    commentSubmitFlag: true, //发表评论按钮提交标识，防止双击重复提交
    getDateStr:function (startTime, endTime, pattern) {
        var r = "";
        if(startTime){
            r += new Date(startTime).format(pattern);
        }else {
            r += "∞ ";
        }
        if(endTime){
            r += "~" + new Date(endTime).format(pattern);
        }else {
            r += " ∞";
        }
        if(!startTime && !endTime){
            r = "-";
        }
        return r;
    },
    getCourseState: function (trainCourse) {
        var stateHtml = "";
        if(trainCourse.state == 0 || trainCourse.state == 2 || trainCourse.state == 3){
            stateHtml = "<div class=\"courseState background-6BB9DD\" title=\""+coursePreviewObj.courseState[trainCourse.state]+"\">\n" +
                "             "+coursePreviewObj.courseState[trainCourse.state]+"\n" +
                "         </div>";
        }else if(trainCourse.state == 1){
            var stateDesc = coursePreviewObj.courseState[trainCourse.state];
            var stateBackColor = "background-00A2FF";
            if(new Date() < new Date(trainCourse.trainStartTime)){
                stateDesc = "未开始";
            }else if(new Date() < new Date(trainCourse.trainEndTime)){
                stateDesc = "进行中";
            }else {
                stateDesc = "已完成";
                stateBackColor = "background-EEEEEE";
            }
            stateHtml = "<div class=\"courseState "+stateBackColor+"\" title=\""+stateDesc+"\">\n" +
                "             "+stateDesc+"\n" +
                "         </div>";
        }else {
            stateHtml = "<div class=\"courseState background-F15F5F\" title=\""+coursePreviewObj.courseState[trainCourse.state]+"\">\n" +
                "             "+coursePreviewObj.courseState[trainCourse.state]+"\n" +
                "         </div>";
        }
        return stateHtml;
    },
    loadCourseData:function (layero, trainCourse) {
        if(trainCourse){
            coursePreviewObj.currentCourseId = trainCourse.id;//课程ID
            $(layero[0]).find(".coursePic").css("background-image", "url(\""+(trainCourse.coursePic || "/img/train/course_pic_default.png")+"\")");//课程图片
            $(layero[0]).find(".coursePlate").text(commonObj.coursePlateMap[trainCourse.coursePlate]);//课程板块
            $(layero[0]).find(".coursePlate").attr("title", commonObj.coursePlateMap[trainCourse.coursePlate]);//课程板块
            $(layero[0]).find(".courseTitle").text(trainCourse.title  || "");//课程标题
            $(layero[0]).find(".courseTitle").attr("title", trainCourse.title  || "");//课程标题
            $(layero[0]).find(".cousreScoreNum").text(trainCourse.courseScoreNum || 0);//课程评分数
            $(layero[0]).find(".courseLikeNum").text(trainCourse.courseLikeNum || 0);//点赞数
            $(layero[0]).find(".courseVentNum").text(trainCourse.courseVentNum || 0);//吐槽数
            $(layero[0]).find(".courseSignTime").find("div:last-child").text(coursePreviewObj.getDateStr(trainCourse.trainStartTime, trainCourse.trainEndTime, "yyyy.MM.dd hh:mm"));//课程培训时间
            $(layero[0]).find(".courseSignTime").attr("title", coursePreviewObj.getDateStr(trainCourse.trainStartTime, trainCourse.trainEndTime, "yyyy.MM.dd hh:mm"));//课程培训时间
            $(layero[0]).find(".courseAddress").find("div:last-child").text(trainCourse.trainAddress  || "");//课程培训地址
            $(layero[0]).find(".courseAddress").attr("title", trainCourse.trainAddress  || "");//课程培训地址
            $(layero[0]).find(".courseTeacherName").text(trainCourse.user.name  || "");//讲师名称
            $(layero[0]).find(".courseStateWrap").html(coursePreviewObj.getCourseState(trainCourse));//状态展示
            $(layero[0]).find(".courseScoreValue").text(trainCourse.trainGrade || 0);//课程学分
            $(layero[0]).find(".courseSignNum").text(trainCourse.courseSignNum || 0);//报名人数
            //如果课程未结束，完课人数为0
            if(trainCourse.state == 1 && new Date() < new Date(trainCourse.trainEndTime)){
                $(layero[0]).find(".courseCompleteNum").text(0);//完课人数
            }else {
                $(layero[0]).find(".courseCompleteNum").text(trainCourse.courseCompleteNum || 0);//完课人数
            }
            $(layero[0]).find(".courseCommentTotal").text(trainCourse.courseCommentNum || 0);//评论数
            $(layero[0]).find(".courseRemake").html(trainCourse.courseRemake || "");//课程大纲
            //课程评分均值
            layui.use('rate', function(){
                var rate = layui.rate;
                rate.render({
                    elem: $(layero[0]).find(".cousreTotalStar"),
                    half: true, //开启半星
                    readonly:true,
                    value: (trainCourse.courseScoreNum == 0 ? 0 : (trainCourse.courseScore / trainCourse.courseScoreNum / 20))
                });

            });
        }
    },
    getCourseCommentItem:function (commentItemMap) {
        var html = "";
        if(commentItemMap){
            html = "<div class=\"courseCommentItem\">\n" +
                "        <div class=\"userWrap\">\n" +
                "            <!--用户头像+姓名-->\n" +
                "            <div class=\"userItem\" title=\""+commentItemMap.userName+"\">\n" +
                "                <div class=\"userPic\" onerror=\"this.src='/img/mrtx_1.png'\" style='background-image: url("+(commentItemMap.userPic || "/img/mrtx_1.png")+")'></div>\n" +
                "                <div class=\"userName\">\n" +
                "                    <div class=\"ellipsisContent\">"+commentItemMap.userName+"</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "            <!--评分-->\n" +
                "            <div data-score='"+commentItemMap.courseScore+"' class=\"userScore\"></div>\n" +
                "        </div>\n" +
                "        <!--评论内容-->\n" +
                "        <div class=\"courseCommentInfo\">\n" +
                "            <!--评论内容-->\n" +
                "            <div class=\"courseCommentContent\">\n" +
                "                "+commentItemMap.courseComment+"\n" +
                "            </div>\n" +
                "            <div class=\"courseCommentTime\">\n" +
                "                "+commentItemMap.createDate+"\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>";
        }
        return html;
    },
    loadCourseCommentData:function (layero, trainCourse) {
        coursePreviewObj.courseSignMap = {};

        //课程报名缓存
        if(trainCourse.courseSignList && trainCourse.courseSignList.length > 0){
            $.each(trainCourse.courseSignList, function (i, courseSign) {
                coursePreviewObj.courseSignMap[courseSign.userId] = courseSign;
            });
        }

        var courseSign = coursePreviewObj.courseSignMap[user.id];//获取用户课程报名

        //判断是否是学员，如果是学员，则可以点赞、吐槽、评分、评论，否则讲师和未报名课程的也不可
        if(user.id == trainCourse.createId || !courseSign){
            $(layero[0]).find(".courseComment").css("display", "none");
        }else {
            $(layero[0]).find(".courseComment").css("display", "flex");
            //判断学员报名状态是否正常，如果是1-迟到、2-早退、3-旷课，则不能进行点赞、吐槽、评分，不展示
            if(courseSign && courseSign.state == 0){
                $(layero[0]).find(".studentSignState0").css("display", "flex");//显示
                $(layero[0]).find(".cousreStar").css("display", "inline-block");//显示

                //设置当前用户是否已点赞
                if(courseSign && courseSign.likeFlag == 1){
                    $(layero[0]).find(".likeBtn").addClass("fa-thumbs-up");
                    $(layero[0]).find(".likeBtn").removeClass("fa-thumbs-o-up");
                }else {
                    $(layero[0]).find(".likeBtn").removeClass("fa-thumbs-up");
                    $(layero[0]).find(".likeBtn").addClass("fa-thumbs-o-up");
                }
                //设置当前用户是否已评分
                if(courseSign && courseSign.ventFlag == 1){
                    $(layero[0]).find(".ventBtn").removeClass("fa-thumbs-o-down");
                    $(layero[0]).find(".ventBtn").addClass("fa-thumbs-down");
                }else {
                    $(layero[0]).find(".ventBtn").removeClass("fa-thumbs-down");
                    $(layero[0]).find(".ventBtn").addClass("fa-thumbs-o-down");
                }
                //已评分，则不允许再次评分
                if(courseSign && courseSign.scoreFlag == 1){
                    coursePreviewObj.scoreSubmitFlag  = false;//禁用
                    layui.use('rate', function(){
                        var rate = layui.rate;
                        rate.render({
                            elem: $(layero[0]).find(".cousreStar"),
                            half: true, //开启半星
                            setText:function (value) {
                                this.span.text(value * 20 + "分");
                            },
                            readonly:true,
                            text:true,
                            value:(courseSign.score || 0) / 20
                        });
                    });
                }else {
                    //设置评分
                    layui.use('rate', function(){
                        var rate = layui.rate;
                        rate.render({
                            elem: $(layero[0]).find(".cousreStar"),
                            half: true, //开启半星
                            text:true,
                            choose: function(value){
                                if(coursePreviewObj.scoreSubmitFlag){
                                    coursePreviewObj.scoreSubmitFlag  = false;//禁用
                                    //请求成功设置评分只读
                                    commonObj.requestData({courseId: coursePreviewObj.currentCourseId, score:parseFloat(value) * 20}, "/trainCourse/courseSetScore", "post", "json", true, function (data) {
                                        if(data.code == 200){
                                            rate.render({
                                                elem: $(layero[0]).find(".cousreStar"),
                                                half: true, //开启半星
                                                value:value,
                                                text:true,
                                                setText:function (value) {
                                                    this.span.text((parseFloat(value) * 20) + "分");
                                                },
                                                readonly:true
                                            });

                                            //保存评分到当前用户报名缓存
                                            coursePreviewObj.courseSignMap[user.id].score = parseFloat(value) * 20;

                                            var courseScore = trainCourse.courseScore + parseFloat(value) * 20;//总评分
                                            var courseScoreNum = trainCourse.courseScoreNum + 1;//总评分数量
                                            //课程评分均值
                                            rate.render({
                                                elem: $(layero[0]).find(".cousreTotalStar"),
                                                half: true, //开启半星
                                                readonly:true,
                                                value: courseScore / courseScoreNum /20
                                            });
                                            //评分数量
                                            $(layero[0]).find(".cousreScoreNum").text(courseScoreNum);
                                        }else {
                                            layer.msg(data.msg, {time: 3000, icon: 5});
                                        }
                                        coursePreviewObj.scoreSubmitFlag  = true;//启用
                                    }, function () {
                                        coursePreviewObj.scoreSubmitFlag  = true;//启用
                                    });
                                }
                            }
                        });
                    });
                }
            }else {
                $(layero[0]).find(".studentSignState0").css("display", "none");//显示
                $(layero[0]).find(".cousreStar").css("display", "none");//显示
            }

        }

        //渲染评论列表
        if(trainCourse.courseCommentList && trainCourse.courseCommentList.length > 0){
            var html = "";
            $.each(trainCourse.courseCommentList, function (i, courseComment) {
                var commentItemMap = {
                    userPic:(commonObj.userMap[courseComment.createId].image || "/img/mrtx_1.png"),
                    userName:commonObj.userMap[courseComment.createId].name,
                    courseScore:(coursePreviewObj.courseSignMap[courseComment.createId].score || 0),
                    courseComment:courseComment.courseComment,
                    createDate:courseComment.createDate
                };
                html += coursePreviewObj.getCourseCommentItem(commentItemMap);
            });
            $(layero[0]).find(".commentDetailList").html(html);
        }

        //渲染评分
        layui.use('rate', function(){
            var rate = layui.rate;
            $(layero[0]).find(".courseCommentItem").each(function (k, courseCommentItem) {
                rate.render({
                    elem: $(courseCommentItem).find(".userScore"),
                    half: true, //开启半星
                    readonly:true,
                    value:($(courseCommentItem).find(".userScore").attr("data-score") || 0) / 20
                });
            });
        });
    },
    coursePreviewModalShow:function (trainCourse, showFlag) {
        coursePreviewObj.coursePreviewModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#coursePreviewModal").html(),
            btn: [],
            area: ['65%', '90%'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                coursePreviewObj.currentCourseId = null;//重置
                coursePreviewObj.likeSubmitFlag  = true;//启用
                coursePreviewObj.ventSubmitFlag  = true;//启用
                coursePreviewObj.scoreSubmitFlag  = true;//启用
                coursePreviewObj.commentSubmitFlag =  true;//启用

                //审批界面进来，不能有评论栏目
                if(showFlag){
                    $(layero[0]).find(".tabWrap").find("span:last-child").css("display", "none");
                    coursePreviewObj.loadCourseData(layero, trainCourse);//课程数据渲染
                }else {
                    $(layero[0]).find(".tabWrap").find("span:last-child").css("display", "inline-block");
                    commonObj.requestData({id:trainCourse.id}, "/trainCourse/getTrainCourseDeailById", "post", "json", true, function (data) {
                        if(data.code == 200){
                            coursePreviewObj.loadCourseData(layero, data.data.trainCourse);//课程数据渲染
                            coursePreviewObj.loadCourseCommentData(layero, data.data.trainCourse);//课程评论渲染
                        }else {
                            layer.msg(data.msg, {time: 3000, icon: 5});
                            layer.close(index);
                        }
                    });
                }
            }
        });
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    coursePreviewModalClose:function () {
        if(coursePreviewObj.coursePreviewModalIndex){
            layer.close(coursePreviewObj.coursePreviewModalIndex);
        }else {
            layer.closeAll();
        }
    },
    tabChange:function (t) {
        var $courseContentWrap = $(t).closest(".courseContentWrap");
        $courseContentWrap.find(".tabContentWrap > div").css("display", "none");//都隐藏
        $courseContentWrap.find(".tabWrap > span").removeClass("courceTabActive");
        $(t).addClass("courceTabActive");
        $courseContentWrap.find("."+$(t).attr("data-target")).css("display", "flex");//显示点击的
    },
    likeClick:function (t) {
        if(coursePreviewObj.likeSubmitFlag){
            coursePreviewObj.likeSubmitFlag = false;//禁用
            commonObj.requestData({courseId: coursePreviewObj.currentCourseId}, "/trainCourse/courseLike", "post", "json", true, function (data) {
                if(data.code == 200){
                    //如果未点赞，则进行点赞，否则，取消点赞
                    if($(t).find("i").hasClass("fa-thumbs-o-up")){
                        $(t).find("i").removeClass("fa-thumbs-o-up");
                        $(t).find("i").addClass("fa-thumbs-up");
                        $(t).closest(".trainModalCommon").find(".courseLikeNum").text(parseInt($(t).closest(".trainModalCommon").find(".courseLikeNum").text() || 0) + 1);
                    }else {
                        $(t).find("i").removeClass("fa-thumbs-up");
                        $(t).find("i").addClass("fa-thumbs-o-up");
                        $(t).closest(".trainModalCommon").find(".courseLikeNum").text(parseInt($(t).closest(".trainModalCommon").find(".courseLikeNum").text() || 0) - 1);
                    }
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
                coursePreviewObj.likeSubmitFlag = true;//启用
            },function () {
                coursePreviewObj.likeSubmitFlag = true;//启用
            });
        }
    },
    ventClick:function (t) {
        if(coursePreviewObj.ventSubmitFlag){
            coursePreviewObj.ventSubmitFlag = false;//禁用
            commonObj.requestData({courseId: coursePreviewObj.currentCourseId}, "/trainCourse/courseVent", "post", "json", true, function (data) {
                if(data.code == 200){
                    //如果未吐槽，则进行吐槽，否则，取消吐槽
                    if($(t).find("i").hasClass("fa-thumbs-o-down")){
                        $(t).find("i").removeClass("fa-thumbs-o-down");
                        $(t).find("i").addClass("fa-thumbs-down");
                        $(t).closest(".trainModalCommon").find(".courseVentNum").text(parseInt($(t).closest(".trainModalCommon").find(".courseVentNum").text() || 0) + 1);
                    }else {
                        $(t).find("i").removeClass("fa-thumbs-down");
                        $(t).find("i").addClass("fa-thumbs-o-down");
                        $(t).closest(".trainModalCommon").find(".courseVentNum").text(parseInt($(t).closest(".trainModalCommon").find(".courseVentNum").text() || 0) - 1);
                    }
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
                coursePreviewObj.ventSubmitFlag = true;//启用
            },function () {
                coursePreviewObj.ventSubmitFlag = true;//启用
            });
        }
    },
    publishComment:function (t) {
        if(coursePreviewObj.commentSubmitFlag){
            coursePreviewObj.commentSubmitFlag = false;//禁用
            var comment = $(t).closest(".columnContentWrap").find("textarea[name='courseComment']").val() || "";
            if(comment){
                var param = {courseId: coursePreviewObj.currentCourseId, parentId: 0, courseComment:comment};
                commonObj.requestData(JSON.stringify(param), "/trainCourse/courseSetComment", "post", "json", true, function (data) {
                    if(data.code == 200){
                        layer.msg("发表成功！", {time: 3000, icon: 6});
                        $(t).closest(".columnContentWrap").find("textarea[name='courseComment']").val("");
                        var commentItemMap = {
                            userPic:(user.image || "/img/mrtx_1.png"),
                            userName:user.name,
                            courseScore:(coursePreviewObj.courseSignMap[user.id].score || 0),
                            courseComment:comment,
                            createDate:new Date().format("yyyy-MM-dd hh:mm:ss")
                        };
                        //设置评论数量
                        $(t).closest(".trainModalCommon").find(".courseCommentTotal").text(parseInt($(t).closest(".trainModalCommon").find(".courseCommentTotal").text() || 0) + 1);
                        //追加评论列表
                        $(t).closest(".trainModalCommon").find(".commentDetailList").append(coursePreviewObj.getCourseCommentItem(commentItemMap));
                        //渲染评分
                        layui.use('rate', function(){
                            var rate = layui.rate;
                            rate.render({
                                elem: $(t).closest(".trainModalCommon").find(".courseCommentItem:last-child").find(".userScore"),
                                half: true, //开启半星
                                readonly:true,
                                value:commentItemMap.courseScore / 20
                            });
                        });
                    }else {
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }
                    coursePreviewObj.commentSubmitFlag = true;//启用
                },function () {
                    coursePreviewObj.commentSubmitFlag = true;//启用
                }, true);
            }else {
                layer.msg("请填写评论内容！", {time: 3000, icon: 5});
                coursePreviewObj.commentSubmitFlag = true;//启用
            }
        }
    }
}

//页面公共处理对象
var commonObj = {
    isAdmin: false, //是否管理员，管理员可查看全部讲师，讲师只查看自己，非讲师将无数据显示
    isTeacher:false, //是否讲师, 讲师只查看自己，非讲师将无数据显示
    listUserUrl:"/trainTeacher/listUser",
    listTrainSettingUrl:"/trainSetting/listTrainSetting", //培训设置列表url
    userList:[],
    userMap:{},
    deptIdList:[],
    coursePlateMap: {}, //培训板块缓存，数据格式：{id：name}
    tabs:{
        0:{id:"trainStudentInfo", tabId:"trainStudentInfoTab", obj:trainStudentInfoObj},
        1:{id:"trainStudentManage", tabId:"trainStudentManageTab", obj:trainStudentManageObj},
    },
    //后台请求方法
    requestData: function (data, url, requestType,dataType,async,callBackFun, callErrorFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun,
        };
        if(callErrorFun){
            param.error = callErrorFun;
        }
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    //Tab切换处理事件
    tabChange: function (index) {
        $(".tabContent").css("display","none");
        $(".layui-tab-title").find("li").removeClass("layui-this");
        if(commonObj.tabs[index]){
            $("#"+commonObj.tabs[index].id).css("display","block");
            $("#"+commonObj.tabs[index].tabId).addClass("layui-this");
            if(commonObj.tabs[index].obj){
                commonObj.tabs[index].obj.init(); //列表展示
            }
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
                limits: config.limits || [15, 30, 50, 100],
                limit: config.limit || 15,
                jump: function (obj, first) {
                    config.param = config.param || {};
                    config.param.size = obj.limit;
                    config.param.page = obj.curr;
                    commonObj.requestData(config.param, config.url, "post", "json", true, function (data) {
                        if(callback){
                            if(config.target){
                                callback(data,config.target, (config.param.size * (config.param.page - 1)));
                            }else {
                                callback(data,null, (config.param.size * (config.param.page - 1)));
                            }
                        }
                    });
                }
            });
        });
    },
    //获取用户权限
    getTrainPermission:function () {
        commonObj.requestData(null, "/trainSetting/getTrainPermission", "post", "json", false, function (data) {
            if(data){
                commonObj.isAdmin = data["admin"] || false;
                commonObj.isTeacher = data["teacher"] || false;
            }
        });
    },
    //根据权限控制tab栏目
    controlTab:function () {
        //如果有管理员 或者 讲师 权限，则展示学员管理栏目
        if(commonObj.isAdmin || commonObj.isTeacher){
            $("#trainStudentManageTab").css("display", "inline-block");
        }else {
            $("#trainStudentManageTab").css("display", "none");
        }
    },
    //页面进入
    initBefore: function () {
        //系统用户
        if(commonObj.userList && commonObj.userList.length < 1){
            commonObj.userMap = {}; //重置
            commonObj.requestData(null, commonObj.listUserUrl, "post", "json", false, function (data) {
                if(data && data.length > 0){
                    $.each(data, function (i, trainStudent) {
                        commonObj.userList.push(trainStudent);
                        commonObj.userMap[trainStudent.sysUserId] = trainStudent;
                    });
                }
            });
        }

        //如果是学员，则不展示搜索栏目，否则是讲师或者管理员展示
        if(commonObj.isAdmin || commonObj.isTeacher){
            $(".searchParamOuter").css("display", "block");
            //渲染下拉列表-讲师，部门
            var studentHtml = "<option value=\"\">请选择学员</option>";
            var deptHtml = "<option value=\"\">请选择部门</option>";
            commonObj.deptIdList = [];
            if(commonObj.userList && commonObj.userList.length > 0){
                $.each(commonObj.userList, function (i, trainStudent) {
                    studentHtml += "<option value=\""+trainStudent.sysUserId+"\">"+trainStudent.name+"</option>";
                    if(!commonObj.deptIdList.contains(trainStudent.deptId)){
                        commonObj.deptIdList.push(trainStudent.deptId);
                        deptHtml += "<option value=\""+trainStudent.deptId+"\">"+trainStudent.deptName+"</option>";
                    }
                });
            }
            $("#trainStudentManage").find("select[name='userId']").html(studentHtml);
            $("#trainStudentManage").find("select[name='deptId']").html(deptHtml);
        }else {
            $(".searchParamOuter").css("display", "none");
        }

        //课程板块
        if(commonObj.coursePlateMap && Object.getOwnPropertyNames(commonObj.coursePlateMap).length < 1){
            commonObj.requestData({settingModuleList: "COURSE_PLATE",orderFlag:true}, commonObj.listTrainSettingUrl, "post", "json", false, function (data) {
                if(data && data.length > 0){
                    $.each(data, function (i, trainSetting) {
                        commonObj.coursePlateMap[trainSetting.id] = trainSetting.settingValue;
                    });
                }
            });
        }

        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    //回车键事件
    enterEvent:function (tabIndex, methonName, event, sourceNode) {
        if((event.keyCode == '13' || event.keyCode == 13) && commonObj.tabs[tabIndex].obj && commonObj.tabs[tabIndex].obj[methonName]){
            if(sourceNode){
                commonObj.tabs[tabIndex].obj[methonName](sourceNode);
            }else {
                commonObj.tabs[tabIndex].obj[methonName]();
            }

        }
    }
}
