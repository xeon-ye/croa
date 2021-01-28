//页面初始化函数
$(function () {
    //获取权限
    commonObj.getTrainPermission();

    //控制栏目
    commonObj.controlTab();

    //下拉列表渲染
    commonObj.initBefore();

    //根据权限决定第一个Tab, isAdmin > isTeacher、isViewAllCourse
    if(commonObj.isAdmin){
        commonObj.tabChange(0);
    } else if (commonObj.isTeacher || commonObj.isViewAllCourse) {
        commonObj.tabChange(1);
    }else {
        commonObj.tabChange(2);
    }
});

//课程审核
var courseAuditObj = {
    getTotalUrl:"/trainCourse/getCourseTotal",
    courseAuditListUrl:"/trainCourse/listCourseByParam",
    courseState:{0:"待审核", 1:"有效", 2:"未报名", 3:"停课", 4:"审核拒绝"},
    rangeIdListMap:{dept:[], role:[], user:[]}, //课程报名范围选择的值
    courseAuditReason:"", //课程审批原因
    courseRangeChange:function (val) {
        var title = "";
        var signStr = "";
        var defaultGroupName = "";
        if(val){
            if(val == 1){
                title = "课程报名范围(部门)";
                signStr = "dept";
                defaultGroupName = "父级部门名称";
            }else if(val == 2){
                title = "课程报名范围(角色)";
                signStr = "role";
                defaultGroupName = "角色类型名称";
                if(commonObj.roleTypeMap && Object.getOwnPropertyNames(commonObj.roleTypeMap).length < 1){
                    commonObj.requestData({typeCode:"ROLE_TYPE"}, "/dict/listByTypeCode2", "get", "json", false, function (data) {
                        if(data && data.length > 0){
                            $.each(data, function (x, roleType) {
                                commonObj.roleTypeMap[roleType.code] = roleType.name;
                            });
                        }
                    });
                }
            }else if(val == 3){
                title = "课程报名范围(用户)";
                signStr = "user";
                defaultGroupName = "用户部门名称";
            }else {
                title = "";
                signStr = "";
                courseAuditObj.rangeIdList = {dept:[], role:[], user:[]};
                courseAuditObj.init();
            }
            if(signStr){
                //如果没有值，则请求，并进行缓存
                var param = {signStr:signStr};
                if(commonObj.courseSignRangeMap[signStr+"List"] && commonObj.courseSignRangeMap[signStr+"List"].length < 1){
                    commonObj.requestData(param, "/trainCourse/listCourseRange", "post", "json", false, function (data) {
                        $.each(data, function (x, rec) {
                            commonObj.courseSignRangeMap[signStr+"List"].push(rec);
                            commonObj.courseSignRangeMap[signStr+"Map"][rec.itemValue] = rec.name;
                        });
                    });
                }
                new SysUserCompont({
                    title:title,
                    url:"/trainCourse/listCourseRange",
                    param:param,
                    defaultGroupName: defaultGroupName,
                    dataList: commonObj.courseSignRangeMap[signStr+"List"],
                    roleTypeMap: commonObj.roleTypeMap,
                    chooseDataList:courseAuditObj.rangeIdListMap[signStr],
                    zIndex: -98000,
                    resultCallBack:function (dataList) {
                        courseAuditObj.rangeIdListMap[signStr] = [];
                        $.each(dataList, function (zj, item) {
                            courseAuditObj.rangeIdListMap[signStr].push(item);
                        });
                        courseAuditObj.init();
                    },
                    endCallBack:function () {
                        courseAuditObj.rangeIdListMap[signStr] = [];
                        courseAuditObj.init();
                    }
                });
            }
        }else {
            courseAuditObj.rangeIdList = {dept:[], role:[], user:[]};
            courseAuditObj.init();
        }
    },
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
    getCourseRangeStr: function (courseEnrollFlag, signRange) {
        var result = [];
        if(signRange){
            var signRangeArr = signRange.split(",");
            $.each(signRangeArr, function (o, signRange) {
                var s = "";
                if(courseEnrollFlag == 1){
                    s = commonObj.courseSignRangeMap["deptMap"][signRange];
                }
                if(courseEnrollFlag == 2){
                    s = commonObj.courseSignRangeMap["roleMap"][signRange];
                }
                if(courseEnrollFlag == 3){
                    s = commonObj.courseSignRangeMap["userMap"][signRange].name;
                }
                if(s){
                    if(!result.contains(s)){
                        result.push(s);
                    }
                }
            });
            if(result.length < 1){
                if(!result.contains("未指定")){
                    result.push("未指定");
                }
            }
        }else {
            if(!result.contains("未指定")){
                result.push("未指定");
            }
        }
        return result.join(",");
    },
    courseAuditListCallback: function (data, target, seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                if(record && record.id){
                    seq += 1;
                    html += "<tr>\n" +
                        "        <td title=\""+seq+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                <input value='"+record.id+"' type=\"checkbox\" lay-skin=\"primary\">"+seq+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(commonObj.courseSignRangeMap["userMap"][record.createId].name || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(commonObj.courseSignRangeMap["userMap"][record.createId].name || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(record.title || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(record.title || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(commonObj.coursePlateMap[record.coursePlate] || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(commonObj.coursePlateMap[record.coursePlate] || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(commonObj.trainWayMap[record.trainWay] || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(commonObj.trainWayMap[record.trainWay] || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseAuditObj.getDateStr(record.signStartTime, record.signEndTime, 'yyyy.MM.dd hh:mm')+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseAuditObj.getDateStr(record.signStartTime, record.signEndTime, 'yyyy.MM.dd hh:mm')+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseAuditObj.getDateStr(record.trainStartTime, record.trainEndTime, 'yyyy.MM.dd hh:mm')+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseAuditObj.getDateStr(record.trainStartTime, record.trainEndTime, 'yyyy.MM.dd hh:mm')+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(record.courseEndTime || '-')+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(record.courseEndTime || '-')+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(record.trainGrade || 0)+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "               "+(record.trainGrade || 0)+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseAuditObj.getCourseRangeStr(record.courseEnrollFlag, record.signRange)+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseAuditObj.getCourseRangeStr(record.courseEnrollFlag, record.signRange)+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseAuditObj.courseState[record.state]+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseAuditObj.courseState[record.state]+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                <button class=\"tableButton blueBtn\" type=\"button\" onclick='coursePreviewObj.coursePreviewModalShow("+JSON.stringify(record)+", \"audit\");'>\n" +
                        "                    预览\n" +
                        "                </button>\n" +
                        "                <button class=\"tableButton blueBtn\" type=\"button\" onclick='courseAuditObj.audit("+record.id+", this, 2)'>\n" +
                        "                    同意\n" +
                        "                </button>\n" +
                        "                <button class=\"tableButton orangeBtn\" type=\"button\" onclick='courseAuditObj.audit("+record.id+", this, 4)'>\n" +
                        "                    拒绝\n" +
                        "                </button>\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "    </tr>";
                }
            });
        }
        var $parentList = target ? $(target) : $("#courseExamList");
        $parentList.html(html);
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    init: function () {
        //全选按钮初始化
        $("#courseAllChoose").removeAttr("checked");

        var param = $("#courseAuditForm").serializeJson();
        param.state = 0; //仅查询待审核状态的
        //课程范围值
        if(param.courseEnrollFlag){
            if(param.courseEnrollFlag == 1){
                param.rangeIdList = courseAuditObj.rangeIdListMap["dept"].join(",");
            }
            if(param.courseEnrollFlag == 2){
                param.rangeIdList = courseAuditObj.rangeIdListMap["role"].join(",");
            }
            if(param.courseEnrollFlag == 3){
                param.rangeIdList = courseAuditObj.rangeIdListMap["user"].join(",");
            }
        }
        //初始化分页组件
        commonObj.requestData(param,courseAuditObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".courseAuditListPager"),
                    count: data.data.total,
                    url: courseAuditObj.courseAuditListUrl,
                    target: $(".courseAuditList"),
                    param: param,
                },courseAuditObj.courseAuditListCallback);
            }
        });
    },
    auditReasonModalShow:function (param, url) {
        layer.open({
            type: 1,
            title: "课程审批原因",
            zIndex: 10,
            content: $("#courseAuditReasonModal").html(),
            btn: ['确定','取消'],
            area: ['550px', '210px'],
            shadeClose: false,
            resize: false,
            move: '.layui-layer-title',
            moveOut: true,
            success: function(layero, index){
                courseAuditObj.courseAuditReason = "";//重置
            },
            yes: function (index, layero) {
                courseAuditObj.courseAuditReason = $(layero[0]).find("textarea").val() || "";
                layer.close(index);
            },
            end: function(){
                //弹窗关闭统一请求
                param.rejectReason = courseAuditObj.courseAuditReason;
                //发送请求
                commonObj.requestData(param, url, "post", "json", true, function (data) {
                    if(data.code == 200){
                        layer.msg("审批成功！", {time: 3000, icon: 6});
                        courseAuditObj.init();//刷新表格
                    }else {
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }
                });
            }
        });
    },
    audit:function (id, t, auditState) {
        //auditState: 2-审批通过，设置成未报名状态、4-审批拒绝
        var param = {id:id, state:auditState};
        if(auditState == 4){
            courseAuditObj.auditReasonModalShow(param, "/trainCourse/auditState");
        }else {
            //发送请求
            commonObj.requestData(param, "/trainCourse/auditState", "post", "json", true, function (data) {
                if(data.code == 200){
                    layer.msg("审批成功！", {time: 3000, icon: 6});
                    courseAuditObj.init();//刷新表格
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
            });
        }
    },
    batchAudit:function (auditState) {
        //auditState: 2-审批通过，设置成未报名状态、4-审批拒绝
        var $tbody = $(".courseAuditList");
        var $inputArr = $tbody.find('input[type="checkbox"]:checked');
        if($inputArr.length < 1){
            layer.msg("很抱歉，没有选中记录进行操作！", {time: 2000, icon: 5});
            return;
        }
        var ids = [];
        $inputArr.each(function (i, input) {
            ids.push($(input).val());
        });
        var param = {ids:ids, state:auditState};
        if(auditState == 4){
            courseAuditObj.auditReasonModalShow(param, "/trainCourse/batchAuditState");
        }else {
            //发送请求
            commonObj.requestData(param, "/trainCourse/batchAuditState", "post", "json", true, function (data) {
                if(data.code == 200){
                    layer.msg("审批成功！", {time: 3000, icon: 6});
                    courseAuditObj.init();//刷新表格
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
            });
        }
    },
}

//课程报名
var courseSignObj = {
    getTotalUrl:"/trainCourse/getCourseTotal",
    courseSignListUrl:"/trainCourse/listCourseByParam",
    courseState:{0:"待审核", 1:"有效", 2:"未报名", 3:"停课", 4:"审核拒绝"},
    rangeIdListMap:{dept:[], role:[], user:[]}, //课程报名范围选择的值
    signUpSubmitFlagList: [], //报名按钮双击事件，数据类型: [courseId]
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
    getCourseRangeStr: function (courseEnrollFlag, signRange) {
        var result = [];
        if(signRange){
            var signRangeArr = signRange.split(",");
            $.each(signRangeArr, function (o, signRange) {
                var s = "";
                if(courseEnrollFlag == 1){
                    s = commonObj.courseSignRangeMap["deptMap"][signRange];
                }
                if(courseEnrollFlag == 2){
                    s = commonObj.courseSignRangeMap["roleMap"][signRange];
                }
                if(courseEnrollFlag == 3){
                    s = commonObj.courseSignRangeMap["userMap"][signRange].name;
                }
                if(s){
                    if(!result.contains(s)){
                        result.push(s);
                    }
                }
            });
            if(result.length < 1){
                if(!result.contains("未指定")){
                    result.push("未指定");
                }
            }
        }else {
            if(!result.contains("未指定")){
                result.push("未指定");
            }
        }
        return result.join(",");
    },
    courseManageListCallback: function (data, target, seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                if(record && record.id){
                    var signHtml = "";//, record.
                    if(!((record.signStartTime && new Date() < new Date(record.signStartTime)) || (record.signEndTime && new Date() > new Date(record.signEndTime)))){
                        signHtml = "<button class=\"tableButton blueBtn\" type=\"button\" onclick='courseSignObj.courseSignUp(this, "+record.id+");'>\n" +
                            "           报名\n" +
                            "       </button>\n";
                    }

                    seq += 1;
                    html += "<tr>\n" +
                        "        <td title=\""+(seq)+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(seq)+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(commonObj.courseSignRangeMap["userMap"][record.createId].name || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(commonObj.courseSignRangeMap["userMap"][record.createId].name || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(record.title || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(record.title || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(commonObj.coursePlateMap[record.coursePlate] || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(commonObj.coursePlateMap[record.coursePlate] || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(commonObj.trainWayMap[record.trainWay] || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(commonObj.trainWayMap[record.trainWay] || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseSignObj.getDateStr(record.signStartTime, record.signEndTime, 'yyyy.MM.dd hh:mm')+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseSignObj.getDateStr(record.signStartTime, record.signEndTime, 'yyyy.MM.dd hh:mm')+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseSignObj.getDateStr(record.trainStartTime, record.trainEndTime, 'yyyy.MM.dd hh:mm')+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseSignObj.getDateStr(record.trainStartTime, record.trainEndTime, 'yyyy.MM.dd hh:mm')+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(record.courseEndTime || '-')+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(record.courseEndTime || '-')+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(record.trainGrade || 0)+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "               "+(record.trainGrade || 0)+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseSignObj.getCourseRangeStr(record.courseEnrollFlag, record.signRange)+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseSignObj.getCourseRangeStr(record.courseEnrollFlag, record.signRange)+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseSignObj.courseState[record.state]+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseSignObj.courseState[record.state]+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                <button class=\"tableButton blueBtn\" type=\"button\" onclick='coursePreviewObj.coursePreviewModalShow("+JSON.stringify(record)+", \"signUp\")'>\n" +
                        "                    预览\n" +
                        "                </button>\n" +
                        "                "+signHtml+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "    </tr>";
                }
            });
        }
        var $parentList = target ? $(target) : $("#courseExamList");
        $parentList.html(html);
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    init: function () {
        courseSignObj.signUpSubmitFlagList = [];//重置

        var param = $("#courseManageForm").serializeJson();
        param.stateList = "1,2"; //1-有效、2-未报名可以报名
        param.sign = "sign";//报名页面标识
        //初始化分页组件
        commonObj.requestData(param,courseSignObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".courseSignListPager"),
                    count: data.data.total,
                    url: courseSignObj.courseSignListUrl,
                    target: $(".courseSignList"),
                    param: param,
                },courseSignObj.courseManageListCallback);
            }
        });
    },
    courseSignUp:function (t, courseId) {
        if(!courseSignObj.signUpSubmitFlagList.contains(courseId)){
            courseSignObj.signUpSubmitFlagList.push(courseId)//禁用
            commonObj.requestData({courseId: courseId}, "/trainCourse/courseSignUp", "post", "json", true, function (data) {
                if(data.code == 200){
                    layer.msg("课程报名成功！", {time: 3000, icon: 6});
                    $(t).closest("td").prev().attr("title", courseSignObj.courseState[1]);
                    $(t).closest("td").prev().find("div").text(courseSignObj.courseState[1]);
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                    courseSignObj.signUpSubmitFlagList.remove(courseId);//启用
                }
            },function () {
                courseSignObj.signUpSubmitFlagList.remove(courseId);//启用
            });
        }else {
            layer.msg("课程已报名！", {time: 3000, icon: 6});
        }
    }
}

//课程管理
var courseManageObj = {
    getTotalUrl:"/trainCourse/getCourseTotal",
    courseManageListUrl:"/trainCourse/listCourseByParam",
    courseState:{0:"待审核", 1:"有效", 2:"未报名", 3:"停课", 4:"审核拒绝"},
    rangeIdListMap:{dept:[], role:[], user:[]}, //课程报名范围选择的值
    courseSignRangeChooseMap: {dept:[], role:[], user:[]}, //课程报名范围选择的值
    editSubmitFlag: true, //编辑课程提交按钮事件
    adminStopSubmitFlag: true, //停课提交标识，防止双击重复提交
    courseEnrollFlagMap:{1:"dept", 2:"role", 3:"user"},
    signModalIndex: 0, //培训计划模态框
    courseRemake: null, //富文本编辑器对象
    validDateResult: {flag: true, errorTipId:"", html:""}, //时间校验结果
    courseRangeChange:function (val) {
        var title = "";
        var signStr = "";
        var defaultGroupName = "";
        if(val){
            if(val == 1){
                title = "课程报名范围(部门)";
                signStr = "dept";
                defaultGroupName = "父级部门名称";
            }else if(val == 2){
                title = "课程报名范围(角色)";
                signStr = "role";
                defaultGroupName = "角色类型名称";
                if(commonObj.roleTypeMap && Object.getOwnPropertyNames(commonObj.roleTypeMap).length < 1){
                    commonObj.requestData({typeCode:"ROLE_TYPE"}, "/dict/listByTypeCode2", "get", "json", false, function (data) {
                        if(data && data.length > 0){
                            $.each(data, function (x, roleType) {
                                commonObj.roleTypeMap[roleType.code] = roleType.name;
                            });
                        }
                    });
                }
            }else if(val == 3){
                title = "课程报名范围(用户)";
                signStr = "user";
                defaultGroupName = "用户部门名称";
            }else {
                title = "";
                signStr = "";
                courseManageObj.rangeIdList = {dept:[], role:[], user:[]};
                courseManageObj.init();
            }
            if(signStr){
                //如果没有值，则请求，并进行缓存
                var param = {signStr:signStr};
                if(commonObj.courseSignRangeMap[signStr+"List"] && commonObj.courseSignRangeMap[signStr+"List"].length < 1){
                    commonObj.requestData(param, "/trainCourse/listCourseRange", "post", "json", false, function (data) {
                        $.each(data, function (x, rec) {
                            commonObj.courseSignRangeMap[signStr+"List"].push(rec);
                            commonObj.courseSignRangeMap[signStr+"Map"][rec.itemValue] = rec.name;
                        });
                    });
                }
                new SysUserCompont({
                    title:title,
                    url:"/trainCourse/listCourseRange",
                    param:param,
                    defaultGroupName: defaultGroupName,
                    dataList: commonObj.courseSignRangeMap[signStr+"List"],
                    roleTypeMap: commonObj.roleTypeMap,
                    chooseDataList:courseManageObj.rangeIdListMap[signStr],
                    zIndex: -98000,
                    resultCallBack:function (dataList) {
                        courseManageObj.rangeIdListMap[signStr] = [];
                        $.each(dataList, function (zj, item) {
                            courseManageObj.rangeIdListMap[signStr].push(item);
                        });
                        courseManageObj.init();
                    },
                    endCallBack:function () {
                        courseManageObj.rangeIdListMap[signStr] = [];
                        courseManageObj.init();
                    }
                });
            }
        }else {
            courseManageObj.rangeIdList = {dept:[], role:[], user:[]};
            courseManageObj.init();
        }
    },
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
    getCourseRangeStr: function (courseEnrollFlag, signRange) {
        var result = [];
        if(signRange){
            var signRangeArr = signRange.split(",");
            $.each(signRangeArr, function (o, signRange) {
                var s = "";
                if(courseEnrollFlag == 1){
                    s = commonObj.courseSignRangeMap["deptMap"][signRange];
                }
                if(courseEnrollFlag == 2){
                    s = commonObj.courseSignRangeMap["roleMap"][signRange];
                }
                if(courseEnrollFlag == 3){
                    s = commonObj.courseSignRangeMap["userMap"][signRange].name;
                }
                if(s){
                    if(!result.contains(s)){
                        result.push(s);
                    }
                }
            });
            if(result.length < 1){
                if(!result.contains("未指定")){
                    result.push("未指定");
                }
            }
        }else {
            if(!result.contains("未指定")){
                result.push("未指定");
            }
        }
        return result.join(",");
    },
    courseManageListCallback: function (data, target, seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                if(record && record.id){
                    var editHtml = "";
                    //状态：0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回
                    if ((record.state == 0 || record.state == -1) && (record.createId == user.id || commonObj.isAdmin)) {
                        editHtml = "<button class=\"tableButton blueBtn\" type=\"button\" onclick='courseManageObj.editCourseModalShow("+record.id+")'>\n" +
                            "           编辑\n" +
                            "       </button>\n";
                    }

                    var stopCourseHtml = "";
                    //如果是管理员，并且课程没有结束， 则可以取消课程
                    if ((record.state == 1 || record.state == 2 || record.state == 3) && (commonObj.isAdmin && new Date(record.trainEndTime) > new Date())) {
                        stopCourseHtml += "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='courseManageObj.adminStopCourseClick(" + record.id + ")'>\n" +
                            "               取消课程\n" +
                            "             </button>\n";
                    }

                    seq += 1;
                    html += "<tr>\n" +
                        "        <td title=\""+(seq)+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(seq)+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(commonObj.courseSignRangeMap["userMap"][record.createId].name || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(commonObj.courseSignRangeMap["userMap"][record.createId].name || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(record.title || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(record.title || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(commonObj.coursePlateMap[record.coursePlate] || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(commonObj.coursePlateMap[record.coursePlate] || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(commonObj.trainWayMap[record.trainWay] || "")+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(commonObj.trainWayMap[record.trainWay] || "")+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseManageObj.getDateStr(record.signStartTime, record.signEndTime, 'yyyy.MM.dd hh:mm')+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseManageObj.getDateStr(record.signStartTime, record.signEndTime, 'yyyy.MM.dd hh:mm')+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseManageObj.getDateStr(record.trainStartTime, record.trainEndTime, 'yyyy.MM.dd hh:mm')+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseManageObj.getDateStr(record.trainStartTime, record.trainEndTime, 'yyyy.MM.dd hh:mm')+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(record.courseEndTime || '-')+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+(record.courseEndTime || '-')+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+(record.trainGrade || 0)+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "               "+(record.trainGrade || 0)+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseManageObj.getCourseRangeStr(record.courseEnrollFlag, record.signRange)+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseManageObj.getCourseRangeStr(record.courseEnrollFlag, record.signRange)+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\""+courseManageObj.courseState[record.state]+"\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                "+courseManageObj.courseState[record.state]+"\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                <button class=\"tableButton blueBtn\" type=\"button\" onclick='coursePreviewObj.coursePreviewModalShow("+JSON.stringify(record)+", \"manage\")'>\n" +
                        "                    查看详情\n" +
                        "                </button>\n" +
                        "                "+editHtml+"\n" +
                        "                " + stopCourseHtml + "\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "    </tr>";
                }
            });
        }
        var $parentList = target ? $(target) : $("#courseExamList");
        $parentList.html(html);
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    init: function () {
        var param = $("#courseManageForm").serializeJson();

        //isViewAllCourse: 查看所有课程、isAdmin：管理员  否则自己的课程， 管理员 = 查看所有课程 > 自己课程
        //如果不是管理员和查看所有课程，则不允许选择讲师姓名，且只能查询自己的
        if (commonObj.isAdmin || commonObj.isViewAllCourse) {
            $("#courseManage").find(".tearcherWrap").css("display", "block");
        }else {
            $("#courseManage").find(".tearcherWrap").css("display", "none");
            param.createId = user.id; //讲师角色只能查看自己的课程
        }
        //课程范围值
        if(param.courseEnrollFlag){
            if(param.courseEnrollFlag == 1){
                param.rangeIdList = courseManageObj.rangeIdListMap["dept"].join(",");
            }
            if(param.courseEnrollFlag == 2){
                param.rangeIdList = courseManageObj.rangeIdListMap["role"].join(",");
            }
            if(param.courseEnrollFlag == 3){
                param.rangeIdList = courseManageObj.rangeIdListMap["user"].join(",");
            }
        }
        //初始化分页组件
        commonObj.requestData(param,courseManageObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".courseManageListPager"),
                    count: data.data.total,
                    url: courseManageObj.courseManageListUrl,
                    target: $(".courseManageList"),
                    param: param,
                },courseManageObj.courseManageListCallback);
            }
        });
    },
    renderDate:function (layero) {
        var laydate = layui.laydate;
        //报名开始时间
        var signStartTime = {
            elem: $(layero[0]).find("input[name='signStartTime']")[0],
            istime: true,
            type:'datetime',
            format:'yyyy-MM-dd HH:mm',
            done:function (value,data) {
                //报名开始时间 与 截止时间 - 由于这里影响截止时间提示信息，所以需要判断报名截止时间 与 培训开始时间
                courseManageObj.validDate(value, $(layero[0]).find("input[name='signEndTime']").val(),"signStartTime-error", "signEndTime-error",layero, "报名开始时间不能大于截止时间！");
                //报名截止时间 与 培训开始时间
                courseManageObj.validDate($(layero[0]).find("input[name='signEndTime']").val(), $(layero[0]).find("input[name='trainStartTime']").val(),"signEndTime-error", "",layero, "报名截止时间不能大于培训开始时间！");
            }
        };
        laydate.render(signStartTime);
        //报名截止时间
        var signEndTime = {
            elem: $(layero[0]).find("input[name='signEndTime']")[0],
            istime: true,
            type:'datetime',
            format:'yyyy-MM-dd HH:mm',
            done:function (value,data) {
                //报名截止时间 与 报名开始时间
                courseManageObj.validDate($(layero[0]).find("input[name='signStartTime']").val(), value,"signStartTime-error", "",layero, "报名截止时间不能小于开始时间！");
                //报名截止时间 与 培训开始时间 - 由于这里影响培训开始时间提示信息，所以需要判断培训开始时间 与 截止时间
                courseManageObj.validDate(value, $(layero[0]).find("input[name='trainStartTime']").val(),"signEndTime-error", "",layero, "报名截止时间不能大于培训开始时间！");
            }
        };
        laydate.render(signEndTime);
        //培训开始时间
        var trainStartTime = {
            elem: $(layero[0]).find("input[name='trainStartTime']")[0],
            istime: true,
            type:'datetime',
            format:'yyyy-MM-dd HH:mm',
            done:function (value,data) {
                //培训开始时间 与 报名截止时间
                courseManageObj.validDate($(layero[0]).find("input[name='signEndTime']").val(), value,"signEndTime-error", "",layero, "报名截止时间不能大于培训开始时间！");
                //培训开始时间 与 截止时间 - 由于这里影响培训截止时间提示信息，所以需要判断培训截止时间 与 课程反馈时间
                courseManageObj.validDate(value, $(layero[0]).find("input[name='trainEndTime']").val(),"trainStartTime-error", "trainEndTime-error",layero, "培训开始时间不能大于截止时间！");
            }
        };
        laydate.render(trainStartTime);
        //培训截止时间
        var trainEndTime = {
            elem: $(layero[0]).find("input[name='trainEndTime']")[0],
            istime: true,
            type:'datetime',
            format:'yyyy-MM-dd HH:mm',
            done:function (value,data) {
                //培训截止时间 与 开始时间
                courseManageObj.validDate($(layero[0]).find("input[name='trainStartTime']").val(), value,"trainEndTime-error", "trainStartTime-error",layero, "培训截止时间不能小于开始时间！")
                //培训截止时间 与 课程反馈时间
                courseManageObj.validDate(value, $(layero[0]).find("input[name='courseEndTime']").val(),"courseEndTime-error", "",layero, "课程反馈截止时间不能小于培训开始时间！");
            }
        };
        laydate.render(trainEndTime);
        //课程反馈截止时间
        var courseEndTime = {
            elem: $(layero[0]).find("input[name='courseEndTime']")[0],
            istime: true,
            type:'datetime',
            format:'yyyy-MM-dd HH:mm',
            done:function (value,data) {
                //培训截止时间 与 课程反馈时间
                courseManageObj.validDate($(layero[0]).find("input[name='trainEndTime']").val(), value,"courseEndTime-error", "",layero, "课程反馈截止时间不能小于培训截止时间！");
            }
        };
        laydate.render(courseEndTime);
    },
    validDate:function (beforeTime, afterTime, errorTipId, otherTipId, layero, errorMsg) {
        var result = true; //默认校验成功
        var html = "<i class=\"fa fa-times-circle\"></i>";
        if(beforeTime && afterTime){
            if(beforeTime >= afterTime){
                result = false
            }
            if(!result){
                if(errorMsg){
                    if(errorTipId){
                        $(layero[0]).find("#"+errorTipId).css("display", "inline-block");
                        html += errorMsg;
                        $(layero[0]).find("#"+errorTipId).html(html);
                    }
                    if(otherTipId){
                        $(layero[0]).find("#"+otherTipId).css("display", "none");
                    }
                }
            }else {
                if(errorTipId){
                    $(layero[0]).find("#"+errorTipId).css("display", "none");
                }
                if(otherTipId){
                    $(layero[0]).find("#"+otherTipId).css("display", "none");
                }
            }
        }
        courseManageObj.validDateResult = {flag:result, errorTipId:errorTipId, html: html};
        return result;
    },
    validSubmitDate:function (t, jsonData) {
        var signStartTime = jsonData["signStartTime"]; //报名开始时间
        var signEndTime = jsonData["signEndTime"]; //报名截止时间
        var trainStartTime = jsonData["trainStartTime"]; //培训开始时间
        var trainEndTime = jsonData["trainEndTime"]; //培训截止时间
        var courseEndTime = jsonData["courseEndTime"]; //课程反馈截止时间
        var flag = true; //校验结果
        $(t).closest(".courseModalCommon").find("#signStartTime-error").css("display", "none");
        $(t).closest(".courseModalCommon").find("#signEndTime-error").css("display", "none");
        $(t).closest(".courseModalCommon").find("#trainStartTime-error").css("display", "none");
        $(t).closest(".courseModalCommon").find("#trainEndTime-error").css("display", "none");
        $(t).closest(".courseModalCommon").find("#courseEndTime-error").css("display", "none");
        if(signStartTime >= signEndTime){
            flag = false;
            $(t).closest(".courseModalCommon").find("#signStartTime-error").css("display", "inline-block");
            $(t).closest(".courseModalCommon").find("#signStartTime-error").html("<i class=\"fa fa-times-circle\"></i>报名开始时间大于报名截止时间");
        }else {
            $(t).closest(".courseModalCommon").find("#signStartTime-error").css("display", "none");
        }
        if(signEndTime >= trainStartTime){
            flag = false;
            $(t).closest(".courseModalCommon").find("#trainStartTime-error").css("display", "inline-block");
            $(t).closest(".courseModalCommon").find("#trainStartTime-error").html("<i class=\"fa fa-times-circle\"></i>培训开始时间小于报名截止时间");
        }else {
            $(t).closest(".courseModalCommon").find("#trainStartTime-error").css("display", "none");
        }
        if(trainStartTime >= trainEndTime){
            flag = false;
            $(t).closest(".courseModalCommon").find("#trainEndTime-error").css("display", "inline-block");
            $(t).closest(".courseModalCommon").find("#trainEndTime-error").html("<i class=\"fa fa-times-circle\"></i>培训截止时间小于培训开始时间");
        }else {
            $(t).closest(".courseModalCommon").find("#trainEndTime-error").css("display", "none");
        }
        if(trainEndTime >= courseEndTime){
            flag = false;
            $(t).closest(".courseModalCommon").find("#courseEndTime-error").css("display", "inline-block");
            $(t).closest(".courseModalCommon").find("#courseEndTime-error").html("<i class=\"fa fa-times-circle\"></i>课程反馈截止时间小于培训截止时间");
        }else {
            $(t).closest(".courseModalCommon").find("#courseEndTime-error").css("display", "none");
        }
        return flag;
    },
    courseSignRangeModalShow: function (t) {
        var courseEnrollFlag = $(t).closest("#trainSignForm").find("select[name='courseEnrollFlag']").val();
        var title = "";
        var signStr = "";
        var defaultGroupName = "";
        if(courseEnrollFlag == 1){
            title = "课程报名范围(部门)";
            signStr = "dept";
            defaultGroupName = "父级部门名称";
        }else if(courseEnrollFlag == 2){
            title = "课程报名范围(角色)";
            signStr = "role";
            defaultGroupName = "角色类型名称";
            if(commonObj.roleTypeMap && Object.getOwnPropertyNames(commonObj.roleTypeMap).length < 1){
                commonObj.requestData({typeCode:"ROLE_TYPE"}, "/dict/listByTypeCode2", "get", "json", false, function (data) {
                    if(data && data.length > 0){
                        $.each(data, function (x, roleType) {
                            commonObj.roleTypeMap[roleType.code] = roleType.name;
                        });
                    }
                });
            }
        }else if(courseEnrollFlag == 3){
            title = "课程报名范围(用户)";
            signStr = "user";
            defaultGroupName = "用户部门名称";
        }else {
            title = "";
            signStr = "";
        }
        if(signStr){
            //如果没有值，则请求，并进行缓存
            var param = {signStr:signStr};
            if(commonObj.courseSignRangeMap[signStr+"List"] && commonObj.courseSignRangeMap[signStr+"List"].length < 1){
                commonObj.requestData(param, "/trainCourse/listCourseRange", "post", "json", false, function (data) {
                    $.each(data, function (x, rec) {
                        commonObj.courseSignRangeMap[signStr+"List"].push(rec);
                        commonObj.courseSignRangeMap[signStr+"Map"][rec.itemValue] = rec.name;
                    });
                });
            }
            new SysUserCompont({
                title:title,
                url:"/trainCourse/listCourseRange",
                param:param,
                defaultGroupName: defaultGroupName,
                dataList: commonObj.courseSignRangeMap[signStr+"List"],
                roleTypeMap: commonObj.roleTypeMap,
                chooseDataList:courseManageObj.courseSignRangeChooseMap[signStr],
                zIndex: -98000,
                resultCallBack:function (dataList) {
                    courseManageObj.courseSignRangeChooseMap[signStr] = [];
                    $.each(dataList, function (zj, item) {
                        courseManageObj.courseSignRangeChooseMap[signStr].push(item);
                    });
                    courseManageObj.init();
                },
                endCallBack:function () {
                    courseManageObj.courseSignRangeChooseMap[signStr] = [];
                    courseManageObj.init();
                }
            });
        }else {

        }
    },
    courseSignRangeClear: function (t, signStr) {
        courseManageObj.courseSignRangeChooseMap[signStr] = [];//清空
        $(t).closest("form").find("select[name='courseEnrollFlag']").val(0);//重置
        $(t).closest("form").find(".courseEnrollBtn").css("display", "none");//重置
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    editCourseModalShow:function (trainCourseId) {
        if(trainCourseId){
            courseManageObj.signModalIndex  = layer.open({
                type: 1,
                title: false,
                zIndex: -99000,
                content: $("#trainSignModal").html(),
                btn: [],
                area: ['65%', '90%'],
                closeBtn: 0,
                resize: false,
                move: '.layui-layer-btn',
                moveOut: true,
                success: function(layero, index){
                    courseManageObj.editSubmitFlag = true; //提交按钮启用

                    commonObj.requestData({id:trainCourseId}, "/trainCourse/getCourseDetailById", "post", "json", false, function (data) {
                        if(data.code == 200){
                            var trainCourse  = data.data.trainCourse;

                            //默认图片
                            $(layero[0]).find("input[name='coursePic']").val(trainCourse.coursePic || "/img/train/course_pic_default.png");
                            $(layero[0]).find(".editCoursePic").css("background-image", 'url("'+(trainCourse.coursePic || "/img/train/course_pic_default.png")+'")');

                            //渲染下拉列表-培训方式
                            var trainWayHtml = "<option value=\"\">请选择培训方式</option>";
                            if(commonObj.trainWayList && commonObj.trainWayList.length > 0){
                                $.each(commonObj.trainWayList, function (i, trainSetting) {
                                    trainWayHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                                });
                            }
                            $(layero[0]).find("select[name='trainWay']").html(trainWayHtml);

                            //渲染下拉列表-培训板块
                            var trainPlateHtml = "<option value=\"\">请选择培训板块</option>";
                            if(commonObj.coursePlateList && commonObj.coursePlateList.length > 0){
                                $.each(commonObj.coursePlateList, function (i, trainSetting) {
                                    trainPlateHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                                });
                            }
                            $(layero[0]).find("select[name='coursePlate']").html(trainPlateHtml);

                            //课程范围改变事件
                            layui.use('form', function(){
                                var form = layui.form;
                                form.on('select(courseEnrollFlag)', function(data){
                                    if(data.value == 0){
                                        $(layero[0]).find(".courseEnrollBtn").css("display", "none");
                                    }else {
                                        $(layero[0]).find(".courseEnrollBtn").css("display", "block");
                                    }
                                });
                            });

                            //渲染日期控件
                            courseManageObj.renderDate(layero);

                            //富文本编辑
                            courseManageObj.courseRemake = KindEditor.create($(layero[0]).find("textarea[name='courseRemake']"), {
                                items:[
                                    'source', '|', 'undo', 'redo', '|', 'preview', 'print', 'template', 'code', 'cut', 'copy', 'paste',
                                    'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
                                    'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
                                    'superscript', 'clearhtml', 'quickformat', 'selectall', '|', 'fullscreen',
                                    'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
                                    'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'insertfile', 'table', 'hr', 'emoticons', 'pagebreak',
                                    'anchor', 'link', 'unlink', '|', 'about'
                                ],
                            });

                            //渲染数据
                            for(var key in trainCourse){
                                if(key == "courseRemake" && courseManageObj.courseRemake){
                                    courseManageObj.courseRemake.html(trainCourse[key] || "");
                                } else if(key == "signStartTime" || key == "signEndTime" || key == "trainStartTime" || key == "trainEndTime" || key == "courseEndTime"){
                                    $(layero[0]).find("input[name='"+key+"']").val(trainCourse[key] ? new Date(trainCourse[key]).format("yyyy-MM-dd hh:mm") : "");
                                } else if(key == "courseEnrollFlag"){
                                    if(trainCourse[key] == 0){
                                        $(layero[0]).find("select[name='"+key+"']").val("");
                                    }else {
                                        $(layero[0]).find("select[name='"+key+"']").val(trainCourse[key] || "");
                                        if(trainCourse.signRange){
                                            courseManageObj.courseSignRangeChooseMap[courseManageObj.courseEnrollFlagMap[trainCourse[key]]] = trainCourse.signRange.split(",");
                                        }
                                    }
                                } else {
                                    $(layero[0]).find("input[name='"+key+"']").val(trainCourse[key] || "");
                                    $(layero[0]).find("select[name='"+key+"']").val(trainCourse[key] || "");
                                }
                            }
                        }else {
                            layer.msg(data.msg, {time: 3000, icon: 5});
                            layer.close(index);
                        }
                    });
                }
            });
            //使用layui表单
            layui.use('form', function(){
                var form = layui.form;
                form.render();
            });
        }else {
            layer.msg("课程不存在！", {time: 2000, icon: 5});
        }
    },
    editCourseModalClose: function () {
        if(courseManageObj.signModalIndex){
            layer.close(courseManageObj.signModalIndex);
        }else {
            layer.closeAll();
        }
    },
    trainSignSubmit: function (t) {
        var $form = $(t).closest(".courseModalCommon").find("form");
        if(!$form.valid()){
            return;
        }
        var jsonData = $form.serializeForm();
        //校验时间
        if(!courseManageObj.validSubmitDate(t, jsonData)){
            return;
        }
        if(!jsonData["title"]){
            layer.msg("请选择课程标题！", {time: 2000, icon: 5});
            return;
        }
        if(!jsonData["coursePlate"]){
            layer.msg("请选择培训板块！", {time: 2000, icon: 5});
            return;
        }
        if(!jsonData["trainWay"]){
            layer.msg("请选择培训方式！", {time: 2000, icon: 5});
            return;
        }
        if(!jsonData["signStartTime"]){
            layer.msg("请选择课程报名开始时间！", {time: 2000, icon: 5});
            return;
        }
        if(!jsonData["signEndTime"]){
            layer.msg("请选择课程报名截止时间！", {time: 2000, icon: 5});
            return;
        }
        if(!jsonData["trainStartTime"]){
            layer.msg("请选择课程培训开始时间！", {time: 2000, icon: 5});
            return;
        }
        if(!jsonData["trainEndTime"]){
            layer.msg("请选择课程培训截止时间！", {time: 2000, icon: 5});
            return;
        }
        if(!jsonData["trainGrade"]){
            layer.msg("请输入课程学分！", {time: 2000, icon: 5});
            return;
        }
        if(!jsonData["courseEndTime"]){
            layer.msg("请选择课程反馈截止时间！", {time: 2000, icon: 5});
            return;
        }
        if (jsonData["courseEnrollFlag"] && jsonData["courseEnrollFlag"] != 0) {
            if (courseManageObj.courseSignRangeChooseMap[courseManageObj.courseEnrollFlagMap[jsonData["courseEnrollFlag"]]].length < 1) {
                jsonData["courseEnrollFlag"] = 0; //如果课程范围没有选中值，则默认
            } else {
                jsonData["courseSignRangeList"] = courseManageObj.courseSignRangeChooseMap[courseManageObj.courseEnrollFlagMap[jsonData["courseEnrollFlag"]]];
            }
        }else {
            jsonData["courseSignRangeList"] = [];
        }

        //富文本编辑器
        if(courseManageObj.courseRemake){
            courseManageObj.courseRemake.sync();//同步数据后可以直接取得textarea的value
            jsonData["courseRemake"] = $form.find("textarea[name='courseRemake']").val() || "";
        }
        //如果没有锁定按钮，则发送请求
        if(courseManageObj.editSubmitFlag){
            courseManageObj.editSubmitFlag = false;
            commonObj.requestData(JSON.stringify(jsonData), '/trainCourse/update', "post", "json", true, function (data) {
                if(data.code == 200){
                    courseManageObj.editCourseModalClose(); //关闭窗口
                    layer.msg("课程修改成功！", {time: 3000, icon: 6});
                    courseManageObj.editSubmitFlag = true;
                    courseManageObj.init();//刷新课程列表
                }else {
                    courseManageObj.editSubmitFlag = true;
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
            },function () {
                courseManageObj.editSubmitFlag = true;
            }, true);
        }
    },
    adminStopCourseClick: function (courseId) {
        if (courseManageObj.adminStopSubmitFlag) {
            courseManageObj.adminStopSubmitFlag = false;//禁用
            commonObj.requestData({courseId: courseId}, "/trainCourse/adminStopCourse", "post", "json", true, function (data) {
                if (data.code == 200) {
                    courseManageObj.adminStopSubmitFlag = true;//启用
                    layer.msg("课程取消成功", {time: 3000, icon: 6});
                    courseManageObj.init();//刷新课程管理
                } else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                    courseManageObj.adminStopSubmitFlag = true;//启用
                }
            }, function () {
                courseManageObj.adminStopSubmitFlag = true;//启用
            });
        }
    },
}

//试卷管理
var courseExamObj = {
    examModalIndex: null,
    questionModalIndex: null,
    enterExamModalIndex: null,
    markPaperModalIndex:null,
    paperTotalGrade: 0, //新增试卷：试卷总分数，读取配置，默认100
    paperSubmitFlag: false,//试卷提交标识，防止重复提交
    currentQuestionType: 1, //添加试卷时，默认单选题被选中
    courseExamListUrl: "/paper/listPaper",
    getTotalUrl:"/paper/getCountByParam",
    courseTimeSetting:{0:"不固定时间", 1:"固定时间"},
    courseWay:{0:"逐题展示", 1:"整卷展示"},
    courseState:{0:"启用", 1:"禁用"},
    questionHtmlMap:{
        1:{id:"radioQuestionItem", name:"radio"},
        2:{id:"checkQuestionItem", name:"check"},
        3:{id:"judgeQuestionItem", name:"judge"},
        4:{id:"completeQuestionItem", name:"complete"},
        5:{id:"essayQuestionItem", name:"essay"}
    },
    questionTypeMap:{1:"单项选择题",2:"多项选择题",3:"判断选择题",4:"填空题",5:"问答题"},
    questionFieldMap:{"questionTitle":"题干", "questionGrade":"分值", "questionAnswer":"正确答案", "questionAnswerDesc":"答案描述"},
    paperGradeTotal: 0, //试卷总分数，提交校验使用
    questionGradeTotal: 0, //题目总分数
    questionSeq: 0, //添加题目序号，一直累加
    questionIdList: [], //添加试卷，从题库选中的题目，用于再次题库选择时，排除已选ID
    questionBankSeq:0, //题库列表序号
    paperState:function (id, t) {
       var state = $(t).attr("data-state");
       state = state == 0 ? 1 : 0;
       commonObj.requestData({id:id, state:state}, "/paper/paperState", "post", "json", true, function (data) {
           if(data.code == 200){
               layer.msg("修改试卷状态成功！", {time: 3000, icon: 6});
               courseExamObj.init();//刷新表格
           }else {
               layer.msg(data.msg, {time: 3000, icon: 5});
           }
       });
    },
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
    courseExamListCallback: function (data, target, seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                var opHtml = "";
                //如果试卷创建人是当前用户，则可以编辑试卷内容
                if(record.createId == user.id){
                    opHtml = "<button data-state='"+record.state+"' class=\"tableButton blueBtn\" type=\"button\" onclick='courseExamObj.paperState("+record.id+", this);'>\n" +
                             "     "+(record.state == 0 ? "禁用":"启用")+"\n" +
                             " </button>\n" +
                             " <button class=\"tableButton blueBtn\" type=\"button\" onclick='courseExamObj.courseExamModalShow("+record.id+")'>\n" +
                             "     编辑\n" +
                             " </button>\n";
                }
                seq += 1;
                html += " <tr>\n" +
                    "         <td title=\""+(seq)+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+seq+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+commonObj.courseSignRangeMap["userMap"][record.createId].name+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+commonObj.courseSignRangeMap["userMap"][record.createId].name+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.paperTitle+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.paperTitle+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(record.courseTitle || "-")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(record.courseTitle || "-")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(commonObj.coursePlateMap[record.coursePlate] || "-")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(commonObj.coursePlateMap[record.coursePlate] || "-")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+courseExamObj.courseTimeSetting[record.paperTimeSetting]+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+courseExamObj.courseTimeSetting[record.paperTimeSetting]+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+courseExamObj.courseWay[record.paperWay]+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+courseExamObj.courseWay[record.paperWay]+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+courseExamObj.getDateStr(record.paperStartTime,record.paperEndTime, "yyyy.MM.dd hh:mm")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+courseExamObj.getDateStr(record.paperStartTime,record.paperEndTime, "yyyy.MM.dd hh:mm")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.paperTime+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.paperTime+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+courseExamObj.courseState[record.state]+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+courseExamObj.courseState[record.state]+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td>\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+opHtml+"\n" +
                    "                 <button class=\"tableButton orangeBtn\" type=\"button\" onclick='courseExamObj.markPaperModalShow(" + JSON.stringify(record) + ", \"" + record.courseTitle + "\")'>\n" +
                    "                     阅卷\n" +
                    "                 </button>\n" +
            /*        "                 <button class=\"tableButton blueBtn\" type=\"button\" onclick='courseExamObj.enterExamModalShow("+record.id+","+record.paperTime+",\""+record.paperTitle+"\")'>\n" +
                    "                     测试\n" +
                    "                 </button>\n" +*/
                    "             </div>\n" +
                    "         </td>\n" +
                    "     </tr>";
            });
        }
        var $parentList = target ? $(target) : $("#courseExamList");
        $parentList.html(html);
    },
    init: function () {
        var param = $("#courseExamForm").serializeJson();
        //管理员权限可以看所有试卷，讲师只能查看自己创建的试卷
        if(!commonObj.isAdmin){
            param.userId = user.id; //讲师角色只能查看自己的课程
        }

        //初始化分页组件
        commonObj.requestData(param,courseExamObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".courseExamListPager"),
                    count: data.data.total,
                    url: courseExamObj.courseExamListUrl,
                    target: $(".courseExamList"),
                    param: param,
                },courseExamObj.courseExamListCallback);
            }
        });
    },
    loadPaperInfo:function (layero, paper) {
        if(paper){
            $(layero[0]).find("input[name='id']").val(paper.id || "");//试卷主键ID
            $(layero[0]).find("input[name='paperGrade']").val(paper.paperGrade || 0);//试卷总分
            $(layero[0]).find("select[name='courseId']").val(paper.courseId || "");//试卷课程ID
            $(layero[0]).find("input[name='coursePlate']").val(paper.coursePlate || "");//试卷板块ID
            $(layero[0]).find("select[name='coursePlateSle']").val(paper.coursePlate || "");//试卷板块ID
            $(layero[0]).find("input[name='paperTitle']").val(paper.paperTitle || "");//试卷标题
            $(layero[0]).find("input[name='paperTime']").val(paper.paperTime || "");//试卷考试时长
            $(layero[0]).find("select[name='paperTimeSetting']").val(paper.paperTimeSetting || 0);//试卷试卷设置
            $(layero[0]).find("select[name='paperWay']").val(paper.paperWay || 0);//试卷展现方式
            $(layero[0]).find("input[name='paperStartTime']").val(paper.paperStartTime ? new Date(paper.paperStartTime).format("yyyy-MM-dd hh:mm") : "");//试卷考试开始时间
            $(layero[0]).find("input[name='paperEndTime']").val(paper.paperStartTime ? new Date(paper.paperEndTime).format("yyyy-MM-dd hh:mm") : "");//试卷考试截止时间
            //隐藏显示时间
            if(paper.paperTimeSetting == 0){
                $(layero[0]).find(".paperBaseContentWrap").css("height", "150px");
                $(layero[0]).find(".paperBaseContentWrap > div:last-child").css("display", "none");
            }else {
                $(layero[0]).find(".paperBaseContentWrap").css("height", "190px");
                $(layero[0]).find(".paperBaseContentWrap > div:last-child").css("display", "block");
            }
        }
    },
    loadQuestionInfo:function (layero, result) {
        for(var it = 1; it <= 5; it++){
            if(result[it] && result[it].length > 0){
                courseExamObj.currentQuestionType = it;
                courseExamObj.addOldQuestion($(layero[0]).find("#paperForm"), result[it]);
            }
        }
    },
    renderDate:function (layero) {
        var laydate = layui.laydate;
        //报名开始时间
        var paperStartTime = {
            elem: $(layero[0]).find("input[name='paperStartTime']")[0],
            istime: true,
            type:'datetime',
            format:'yyyy-MM-dd HH:mm',
            done:function (value,data) {
            }
        };
        laydate.render(paperStartTime);
        //报名截止时间
        var paperEndTime = {
            elem: $(layero[0]).find("input[name='paperEndTime']")[0],
            istime: true,
            type:'datetime',
            format:'yyyy-MM-dd HH:mm',
            done:function (value,data) {
            }
        };
        laydate.render(paperEndTime);
    },
    courseExamModalShow: function (paperId) {
        courseExamObj.examModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#addPaperModal").html(),
            btn: [],
            area: ['65%', '85%'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                $(layero[0]).find(".questionTypeWrap > span > i").text("0"); //初始化题目数量
                $(layero[0]).find(".questionTypeWrap > span").removeClass("questionActive"); //先全部移除，然后默认第一个
                $(layero[0]).find(".questionTypeWrap > span:first-child").addClass("questionActive");
                $(layero[0]).find(".questionContent").css("display", "none"); //先全部隐藏，然后默认第一个
                $(layero[0]).find(".questionContent:first-child").css("display", "block");
                courseExamObj.questionGradeTotal = 0;
                courseExamObj.questionSeq = 0;
                courseExamObj.questionIdList = [];
                courseExamObj.paperSubmitFlag = false;

                //渲染日期
                courseExamObj.renderDate(layero);

                //渲染关联课程
                commonObj.requestData(null, "/trainCourse/listTrainCourseByTeacher", "post", "json", false, function (data) {
                    var trainCourseHtml = "<option value=\"\">请选择关联课程</option>";
                    if(data && data.length > 0){
                        $.each(data, function (i, trainCourse) {
                            trainCourseHtml += "<option data-plate='"+trainCourse.coursePlate+"' value=\""+trainCourse.id+"\">"+trainCourse.title+"</option>";
                        });
                    }
                    $(layero[0]).find("select[name='courseId']").html(trainCourseHtml);
                });

                //渲染下拉列表-培训板块
                var trainPlateHtml = "<option value=\"\">请选择培训板块</option>";
                if(commonObj.coursePlateList && commonObj.coursePlateList.length > 0){
                    $.each(commonObj.coursePlateList, function (i, trainSetting) {
                        trainPlateHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                    });
                }
                $(layero[0]).find("select[name='coursePlateSle']").html(trainPlateHtml);

                //如果paperId有值，说明是编辑
                if(paperId){
                    $(layero[0]).find(".courseModalTitle").text("编辑试卷");
                    commonObj.requestData({id:paperId}, "/paper/getPaperDetailById", "post", "json", false, function (data) {
                        if(data.code == 200){
                            var result = data.data.result;
                            //试卷总分
                            courseExamObj.paperGradeTotal = result.paper.paperGrade || 0
                            //设置试卷基础信息
                            courseExamObj.loadPaperInfo(layero, result.paper);
                            //设置题目信息
                            courseExamObj.loadQuestionInfo(layero, result);
                        }else {
                            layer.msg(data.msg, {time: 2000, icon: 5});
                        }
                    });
                }else {
                    $(layero[0]).find(".courseModalTitle").text("添加试卷");
                    //读取试卷总分数，读取配置后进行缓存
                    if(!courseExamObj.paperTotalGrade){
                        commonObj.requestData({settingModule:"PAPER_GRADE"}, "/trainSetting/getMaxSeqTrainSetting", "post", "json", false, function (data) {
                            if(data && data.settingValue){
                                try {
                                    courseExamObj.paperTotalGrade = parseFloat(data.settingValue);
                                }catch (e) {
                                    courseExamObj.paperTotalGrade = 100;
                                }
                            }else {
                                courseExamObj.paperTotalGrade = 100;
                            }
                        });
                    }
                    $(layero[0]).find("input[name='paperGrade']").val(courseExamObj.paperTotalGrade || 100);//试卷总分
                    courseExamObj.paperGradeTotal = courseExamObj.paperTotalGrade || 100;
                }
                courseExamObj.currentQuestionType = $(layero[0]).find(".questionTypeWrap > span:first-child").attr("data-type");//设置当前题目类型
                //使用layui表单
                layui.use('form', function(){
                    var form = layui.form;
                    form.render("select");
                });
            }
        });
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    courseExamModalClose: function () {
        if(courseExamObj.examModalIndex){
            layer.close(courseExamObj.examModalIndex);
        }else {
            layer.closeAll();
        }
    },
    questionTypeClick:function (t) {
        if($(t).hasClass("questionActive")){
            return;
        }else {
            $(t).closest(".questionTypeWrap").find("span").removeClass("questionActive"); //先全部移除，然后默认第一个
            $(t).addClass("questionActive");
            courseExamObj.currentQuestionType = $(t).attr("data-type");
            $(t).closest("form").find(".questionContent").css("display", "none"); //先全部隐藏，然后默认第一个
            $($(t).closest("form").find(".questionContent")[courseExamObj.currentQuestionType - 1]).css("display", "block");
        }
    },
    addNewQuestion:function (t) {
        var currentTypeNum = $($(t).closest("form").find(".questionTypeWrap").find("span")[courseExamObj.currentQuestionType - 1]).find("i").text() || 0;
        currentTypeNum = parseInt(currentTypeNum) + 1;
        courseExamObj.questionSeq = courseExamObj.questionSeq + 1;
        $($(t).closest("form").find(".questionTypeWrap").find("span")[courseExamObj.currentQuestionType - 1]).find("i").text(currentTypeNum);
        $($(t).closest("form").find(".questionContent")[courseExamObj.currentQuestionType - 1])
            .append($("#"+courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].id)
                .html()
                .th("questionItem-seq",courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].id+courseExamObj.questionSeq)
                .th("question-seq", courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Seq"+courseExamObj.questionSeq)
                .th("question-seq-value", courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Seq"+courseExamObj.questionSeq)
                .th("question-id", courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Id"+courseExamObj.questionSeq)
                .th("question-title", courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Title"+courseExamObj.questionSeq)
                .th("question-grade", courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Grade"+courseExamObj.questionSeq)
                .th("question-answer", courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Answer"+courseExamObj.questionSeq)
                .th("question-answer-desc", courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"AnswerDesc"+courseExamObj.questionSeq));
        //重新给题目排序 以及颜色渲染
        $($(t).closest("form").find(".questionContent")[courseExamObj.currentQuestionType - 1])
            .find("."+courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].id)
            .each(function (j, item) {
                $(item).removeClass("backColorEven");
                $(item).removeClass("backColorOdd");
                if(j%2 == 0){
                    $(item).addClass("backColorEven");
                }else {
                    $(item).addClass("backColorOdd");
                }
                $(item).find(".seq").text(j+1);
            });

        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    addOldQuestion:function (t, questionList) {
        if(questionList && questionList.length > 0){
            var currentTypeNum = $($(t).find(".questionTypeWrap").find("span")[courseExamObj.currentQuestionType - 1]).find("i").text() || 0;
            $.each(questionList, function (o, question) {
                currentTypeNum = parseInt(currentTypeNum) + 1;
                courseExamObj.questionSeq = courseExamObj.questionSeq + 1;
                var questionItemId = courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].id+courseExamObj.questionSeq;
                var questionIdName = courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Id"+courseExamObj.questionSeq;
                var paperDetailIdName = "paperDetailIdId"+courseExamObj.questionSeq;
                var questionTitleName = courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Title"+courseExamObj.questionSeq;
                var questionGradeName = courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Grade"+courseExamObj.questionSeq;
                var questionAnswerName = courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Answer"+courseExamObj.questionSeq;
                var questionAnswerDescName = courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"AnswerDesc"+courseExamObj.questionSeq;
                $($(t).find(".questionContent")[courseExamObj.currentQuestionType - 1])
                    .append($("#"+courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].id)
                        .html()
                        .th("questionItem-seq",questionItemId)
                        .th("input-readonly","readonly")
                        .th("input-disabled","disabled")
                        .th("question-seq", courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Seq"+courseExamObj.questionSeq)
                        .th("question-seq-value", courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].name+"Seq"+courseExamObj.questionSeq)
                        .th("question-id", questionIdName)
                        .th("paper_detail_id", paperDetailIdName)
                        .th("question-title", questionTitleName)
                        .th("question-grade", questionGradeName)
                        .th("question-answer", questionAnswerName)
                        .th("question-answer-desc", questionAnswerDescName));
                var questionId = question.id;
                var paperDetailId = question.paperDetailId || "";
                var questionTitle = question.questionTitle || "";
                var questionGrade = question.questionGrade || 0;
                var questionAnswerArr = (question.questionAnswer || "").split("<^_^>");
                var questionAnswerDescArr = (question.questionAnswerDesc || "").split("<^_^>");
                var $questionItemNode = $($(t).find(".questionContent")[courseExamObj.currentQuestionType - 1]).find("div[data-id='"+questionItemId+"']");
                $questionItemNode.find("input[name='"+questionIdName+"']").val(questionId);//题目主键ID赋值
                $questionItemNode.find("input[name='"+paperDetailIdName+"']").val(paperDetailId);//试卷详情ID赋值
                $questionItemNode.find("input[name='"+questionGradeName+"']").val(questionGrade);//针对单选、多选、判断题目分数赋值
                $questionItemNode.find("textarea[name='"+questionGradeName+"']").val(questionGrade);//针对填空、问答题目分数赋值
                if(courseExamObj.currentQuestionType == 1 || courseExamObj.currentQuestionType == 2 || courseExamObj.currentQuestionType == 3){
                    $questionItemNode.find("input[name='"+questionTitleName+"']").val(questionTitle);
                    $questionItemNode.find("input[name='"+questionAnswerName+"']").each(function (jj, input) {
                        //如果正确答案包含选项答案，则选中
                        if(questionAnswerArr.contains($(input).val())){
                            $(input).attr("checked", true);
                        }else {
                            $(input).attr("checked", false);
                        }
                    });
                    $questionItemNode.find("textarea[name='"+questionAnswerDescName+"']").each(function (xl, textarea) {
                        if(xl < questionAnswerDescArr.length){
                            $(textarea).val(questionAnswerDescArr[xl]);
                        }
                    });
                }else if(courseExamObj.currentQuestionType == 4){
                    $questionItemNode.find("textarea[name='"+questionTitleName+"']").val(questionTitle);
                }else {
                    $questionItemNode.find("textarea[name='"+questionTitleName+"']").val(questionTitle);
                    $questionItemNode.find("textarea[name='"+questionAnswerDescName+"']").val(question.questionAnswerDesc || "");
                }

                courseExamObj.questionIdList.push(questionId);//题目ID保存，进行题库选择将排除这些题目
            });
            $($(t).find(".questionTypeWrap").find("span")[courseExamObj.currentQuestionType - 1]).find("i").text(currentTypeNum);
        }
        //重新给题目排序 以及颜色渲染
        $($(t).find(".questionContent")[courseExamObj.currentQuestionType - 1])
            .find("."+courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].id)
            .each(function (j, item) {
                $(item).removeClass("backColorEven");
                $(item).removeClass("backColorOdd");
                if(j%2 == 0){
                    $(item).addClass("backColorEven");
                }else {
                    $(item).addClass("backColorOdd");
                }
                $(item).find(".seq").text(j+1);
            });

        courseExamObj.calGradeTotal(t);//计算分数

        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    removeQuestion:function (t) {
        var $form = $(t).closest("form");
        var questionId = $(t).closest("."+courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].id).find("input[data-key='questionId']").val() || "";
        if(questionId){
            courseExamObj.questionIdList.remove(questionId); //移除选中的题目ID
        }
        $(t).closest("."+courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].id).remove(); //移除题目
        courseExamObj.calGradeTotal($form);//计算分数
        var currentTypeNum = $($form.find(".questionTypeWrap").find("span")[courseExamObj.currentQuestionType - 1]).find("i").text() || 0;
        currentTypeNum = parseInt(currentTypeNum) - 1;
        $($form.find(".questionTypeWrap").find("span")[courseExamObj.currentQuestionType - 1]).find("i").text(currentTypeNum);
        //重新给题目排序 以及颜色渲染
        $($form.find(".questionContent")[courseExamObj.currentQuestionType - 1])
            .find("."+courseExamObj.questionHtmlMap[courseExamObj.currentQuestionType].id)
            .each(function (j, item) {
                $(item).removeClass("backColorEven");
                $(item).removeClass("backColorOdd");
                if(j%2 == 0){
                    $(item).addClass("backColorEven");
                }else {
                    $(item).addClass("backColorOdd");
                }
                $(item).find(".seq").text(j+1);
            });
    },
    calGradeTotal: function (t) {
        var $form = $(t).closest("form");
        courseExamObj.questionGradeTotal = 0;
        $form.find(".grade").each(function (z, gradeNode) {
            courseExamObj.questionGradeTotal += parseInt($(gradeNode).val() || 0) || 0;
        });
        if(courseExamObj.questionGradeTotal > courseExamObj.paperGradeTotal){
            layer.msg("题目总分数大于试卷总分数！", {time: 2000, icon: 5});
        }
        $form.find(".questionTypeWrap").find(".questionGradeTotal").find("i").text(courseExamObj.questionGradeTotal);
    },
    renderQuestion:function (questionList) {
        var html = "";
        if(questionList && questionList.length > 0){
            $.each(questionList, function (io, question) {
                courseExamObj.questionBankSeq++;
                if(courseExamObj.currentQuestionType == 1){
                    var answerArr = question.questionAnswer.split("<^_^>");
                    var answerDescArr = question.questionAnswerDesc.split("<^_^>");
                    html += "<div class=\"radioQuestionItemShow\" onclick=\"courseExamObj.questionClick(this);\">\n" +
                        "        <input type=\"hidden\" name=\"questionType\" value=\""+question.questionType+"\"/>\n" +
                        "        <input type=\"hidden\" name=\"questionId\" value=\""+question.id+"\"/>\n" +
                        "        <div class=\"ellipsisContent questionTitleShow\">\n" +
                        "            <input type=\"hidden\" name=\"questionTitle\" value=\""+question.questionTitle+"\"/>\n" +
                        "            <span>"+courseExamObj.questionBankSeq+"、</span>\n" +
                        "            <span class=\"questionInput\">"+question.questionTitle+"</span>\n" +
                        "        </div>\n" +
                        "        <div class=\"col-sm-12\">\n" +
                        "            <input type=\"hidden\" name=\"questionAnswer\" value=\""+question.questionAnswer+"\"/>\n" +
                        "            <input type=\"hidden\" name=\"questionAnswerDesc\" value=\""+question.questionAnswerDesc+"\"/>\n" +
                        "            <div class=\"col-sm-6\">\n" +
                        "                <input type=\"radio\" value=\"A\"  class=\"questionInput\" title=\"A、\" disabled "+(answerArr.contains("A") ? "checked" : "")+">\n" +
                        "                <span class=\"questionDesc questionInput\" title='"+(answerDescArr[0] || "")+"'>"+(answerDescArr[0] || "")+"</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"col-sm-6\">\n" +
                        "                <input type=\"radio\" value=\"B\"  class=\"questionInput\" title=\"B、\" disabled "+(answerArr.contains("B") ? "checked" : "")+">\n" +
                        "                <span class=\"questionDesc questionInput\" title='"+(answerDescArr[1] || "")+"'>"+(answerDescArr[1] || "")+"</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"col-sm-6\">\n" +
                        "                <input type=\"radio\" value=\"C\"  class=\"questionInput\" title=\"C、\" disabled "+(answerArr.contains("C") ? "checked" : "")+">\n" +
                        "                <span class=\"questionDesc questionInput\" title='"+(answerDescArr[2] || "")+"'>"+(answerDescArr[2] || "")+"</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"col-sm-6\">\n" +
                        "                <input type=\"radio\" value=\"D\"  class=\"questionInput\" title=\"D、\" disabled "+(answerArr.contains("D") ? "checked" : "")+">\n" +
                        "                <span class=\"questionDesc questionInput\" title='"+(answerDescArr[3] || "")+"'>"+(answerDescArr[3] || "")+"</span>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "    </div>";
                }else if(courseExamObj.currentQuestionType == 2){
                    var answerArr = question.questionAnswer.split("<^_^>");
                    var answerDescArr = question.questionAnswerDesc.split("<^_^>");
                    html += " <div class=\"checkQuestionItemShow\" onclick=\"courseExamObj.questionClick(this);\">\n" +
                        "        <input type=\"hidden\" name=\"questionType\" value=\""+question.questionType+"\"/>\n" +
                        "        <input type=\"hidden\" name=\"questionId\" value=\""+question.id+"\"/>\n" +
                        "         <div class=\"ellipsisContent questionTitleShow\">\n" +
                        "             <input type=\"hidden\" name=\"questionTitle\" value=\""+question.questionTitle+"\"/>\n" +
                        "             <span>"+courseExamObj.questionBankSeq+"、</span>\n" +
                        "             <span class=\"questionInput\">"+question.questionTitle+"</span>\n" +
                        "         </div>\n" +
                        "         <div class=\"col-sm-12\">\n" +
                        "             <input type=\"hidden\" name=\"questionAnswer\" value=\""+question.questionAnswer+"\"/>\n" +
                        "             <input type=\"hidden\" name=\"questionAnswerDesc\" value=\""+question.questionAnswerDesc+"\"/>\n" +
                        "             <div class=\"col-sm-6\">\n" +
                        "                 <input type=\"checkbox\" value=\"A\" class=\"questionInput\" title=\"A、\" lay-skin=\"primary\" disabled "+(answerArr.contains("A") ? "checked" : "")+">\n" +
                        "                 <span class=\"questionDesc questionInput\" style='margin-top: 2px;' title='"+(answerDescArr[0] || "")+"'>"+(answerDescArr[0] || "")+"</span>\n" +
                        "             </div>\n" +
                        "             <div class=\"col-sm-6\">\n" +
                        "                 <input type=\"checkbox\" value=\"B\" class=\"questionInput\" title=\"B、\" lay-skin=\"primary\" disabled "+(answerArr.contains("B") ? "checked" : "")+">\n" +
                        "                 <span class=\"questionDesc questionInput\" style='margin-top: 2px;' title='"+(answerDescArr[0] || "")+"'>"+(answerDescArr[1] || "")+"</span>\n" +
                        "             </div>\n" +
                        "             <div class=\"col-sm-6\">\n" +
                        "                 <input type=\"checkbox\" value=\"C\" class=\"questionInput\" title=\"C、\" lay-skin=\"primary\" disabled "+(answerArr.contains("C") ? "checked" : "")+">\n" +
                        "                 <span class=\"questionDesc questionInput\" style='margin-top: 2px;' title='"+(answerDescArr[0] || "")+"'>"+(answerDescArr[2] || "")+"</span>\n" +
                        "             </div>\n" +
                        "             <div class=\"col-sm-6\">\n" +
                        "                 <input type=\"checkbox\" value=\"D\" class=\"questionInput\" title=\"D、\" lay-skin=\"primary\" disabled "+(answerArr.contains("D") ? "checked" : "")+">\n" +
                        "                 <span class=\"questionDesc questionInput\" style='margin-top: 2px;' title='"+(answerDescArr[0] || "")+"'>"+(answerDescArr[3] || "")+"</span>\n" +
                        "             </div>\n" +
                        "         </div>\n" +
                        "     </div>";
                }else if(courseExamObj.currentQuestionType == 3){
                    var answerArr = question.questionAnswer.split("<^_^>");
                    var answerDescArr = question.questionAnswerDesc.split("<^_^>");
                    html += "<div class=\"judgeQuestionItemShow\" onclick=\"courseExamObj.questionClick(this);\">\n" +
                        "        <input type=\"hidden\" name=\"questionType\" value=\""+question.questionType+"\"/>\n" +
                        "        <input type=\"hidden\" name=\"questionId\" value=\""+question.id+"\"/>\n" +
                        "        <div class=\"ellipsisContent questionTitleShow\">\n" +
                        "            <input type=\"hidden\" name=\"questionTitle\" value=\""+question.questionTitle+"\"/>\n" +
                        "            <span>"+courseExamObj.questionBankSeq+"、</span>\n" +
                        "            <span class=\"questionInput\">"+question.questionTitle+"</span>\n" +
                        "        </div>\n" +
                        "        <div class=\"col-sm-12\">\n" +
                        "            <input type=\"hidden\" name=\"questionAnswer\" value=\""+question.questionAnswer+"\"/>\n" +
                        "            <input type=\"hidden\" name=\"questionAnswerDesc\" value=\""+question.questionAnswerDesc+"\"/>\n" +
                        "            <div class=\"col-sm-6\">\n" +
                        "                <input type=\"radio\" value=\"1\" title=\"正确\" class=\"questionInput\" disabled "+(answerArr.contains(1) ? "checked" : "")+">\n" +
                        // "                <span class=\"questionDesc questionInput\" style=\"width: 98%;margin-left: unset;\">"+(answerDescArr[0] || "")+"</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"col-sm-6\">\n" +
                        "                <input type=\"radio\" value=\"0\" title=\"错误\" class=\"questionInput\" disabled "+(answerArr.contains(0) ? "checked" : "")+">\n" +
                        // "                <span class=\"questionDesc questionInput\" style=\"width: 98%;margin-left: unset;\">"+(answerDescArr[1] || "")+"</span>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "    </div>";

                }else if(courseExamObj.currentQuestionType == 4){
                    var answerArr = question.questionAnswer.split("<^_^>");
                    var answerDesc = question.questionAnswerDesc;
                    $.each(answerArr, function (xy, answer) {
                        var temp = "<span class=\"completeAnswer\">"+answer+"</span>";
                        answerDesc = answerDesc.replace("<^_^>", temp);
                    });
                    html += "<div class=\"completeQuestionItemShow\" onclick=\"courseExamObj.questionClick(this);\">\n" +
                        "        <input type=\"hidden\" name=\"questionType\" value=\""+question.questionType+"\"/>\n" +
                        "        <input type=\"hidden\" name=\"questionId\" value=\""+question.id+"\"/>\n" +
                        "        <input type=\"hidden\" name=\"questionTitle\" value=\""+question.questionTitle+"\"/>\n" +
                        "        <div class=\"questionTitleShow questionInput\" style=\"height: 100% !important;\">\n" +
                        "            "+courseExamObj.questionBankSeq+"、"+answerDesc+"\n" +
                        "        </div>\n" +
                        "    </div>";
                }else {
                    html += "<div class=\"essayQuestionItemShow\" onclick=\"courseExamObj.questionClick(this);\">\n" +
                        "        <input type=\"hidden\" name=\"questionType\" value=\""+question.questionType+"\"/>\n" +
                        "        <input type=\"hidden\" name=\"questionId\" value=\""+question.id+"\"/>\n" +
                        "        <div class=\"ellipsisContent questionTitleShow\">\n" +
                        "            <input type=\"hidden\" name=\"questionTitle\" value=\""+question.questionTitle+"\"/>\n" +
                        "            <span>"+courseExamObj.questionBankSeq+"、</span>\n" +
                        "            <span class=\"questionInput\">"+question.questionTitle+"</span>\n" +
                        "        </div>\n" +
                        "        <div class=\"essayAnswer\">\n" +
                        "            <input type=\"hidden\" name=\"questionAnswerDesc\" value=\""+question.questionAnswerDesc+"\"/>\n" +
                        "            <span style=\"color: #cec2c2;\">答案解析：</span>\n" +
                        "            <span class=\"questionInput\">\n" +
                        "                "+question.questionAnswerDesc+"\n" +
                        "            </span>\n" +
                        "        </div>\n" +
                        "    </div>";
                }
            });
        }
        return html;
    },
    loadQuestionBank:function ($questionForm, $questionSearchForm) {
        layui.use('flow', function(){
            var flow = layui.flow;
            flow.load({
                elem: $questionForm,
                isAuto: true,
                done: function(page, next){
                    //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                    //请求数据，判断当前题目类型进行加载
                    var param = $questionSearchForm.serializeJson();
                    param.questionType = courseExamObj.currentQuestionType;
                    param.page = page; //页码
                    if(courseExamObj.questionIdList && courseExamObj.questionIdList.length > 0){
                        param.excludeIds = courseExamObj.questionIdList.join(",");//已被选择的题目ID，需要排除掉
                    }
                    commonObj.requestData(param, "/question/listQuestion", "post", "json", false, function (data) {
                        next(courseExamObj.renderQuestion(data.list), page < data.pages); //如果小于总页数，则继续
                    });
                    //使用layui表单
                    layui.use('form', function(){
                        var form = layui.form;
                        form.render();
                    });
                }
            });
        });
    },
    queryQuestion:function (t) {
        var $courseModalCommon = $(t).closest(".courseModalCommon");
        //重置参数
        courseExamObj.questionBankSeq = 0; //题库列表序号重置
        $courseModalCommon.find("#questionForm").html("");
        var $courseModalCommon = $(t).closest(".courseModalCommon");
        //流加载
        courseExamObj.loadQuestionBank($courseModalCommon.find("#questionForm"),$courseModalCommon.find("#questionSearchForm"));
    },
    questionModalShow:function (t) {
        courseExamObj.questionModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -98000,
            content: $("#chooseQuestionModal").html(),
            btn: [],
            area: ['55%', '70%'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                courseExamObj.questionBankSeq = 0;//题库列表序号重置
                //设置标题
                $(layero[0]).find(".courseModalTitle").text(courseExamObj.questionTypeMap[courseExamObj.currentQuestionType] + "(点击题目选择，可多选)");

                //渲染下拉列表-培训板块
                var trainPlateHtml = "<option value=\"\">请选择所属板块</option>";
                if(commonObj.coursePlateList && commonObj.coursePlateList.length > 0){
                    $.each(commonObj.coursePlateList, function (i, trainSetting) {
                        trainPlateHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                    });
                }
                $(layero[0]).find("select[name='coursePlate']").html(trainPlateHtml);
                $(layero[0]).find("select[name='coursePlate']").val($(t).closest(".courseModalCommon").find("input[name='coursePlate']").val());//默认等于试卷板块

                //流加载
                courseExamObj.loadQuestionBank($(layero[0]).find("#questionForm"),$(layero[0]).find("#questionSearchForm"));
            }
        });
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    questionModalClose:function () {
        if(courseExamObj.questionModalIndex){
            layer.close(courseExamObj.questionModalIndex);
        }else {
            layer.closeAll();
        }
    },
    questionClick:function (t) {
        if($(t).hasClass("questionChoose")){
            $(t).removeClass("questionChoose");
        }else {
            $(t).addClass("questionChoose");
        }
    },
    questionChooseBtn:function (t) {
        var $form = $(t).closest(".courseModalCommon").find("#questionForm");
        var questionArr = [];
        $form.find(".questionChoose").each(function (i, item) {
            var questionId = $(item).find("input[name='questionId']").val();
            var questionType = $(item).find("input[name='questionType']").val();
            var questionTitle = $(item).find("input[name='questionTitle']").val();
            var questionAnswer = $(item).find("input[name='questionAnswer']").val();
            var questionAnswerDesc = $(item).find("input[name='questionAnswerDesc']").val();
            //题目没有被选择，才进行添加
            if(!courseExamObj.questionIdList.contains(questionId) && courseExamObj.currentQuestionType == questionType){
                questionArr.push({id:questionId,questionType:questionType,questionTitle:questionTitle,questionAnswer:questionAnswer,questionAnswerDesc:questionAnswerDesc});
            }
        });
        courseExamObj.questionModalClose();//关闭模态框
        courseExamObj.addOldQuestion($(".layui-layer-content").find("#paperForm"), questionArr);
    },
    handleData:function ($form) {
        var jsonData = $form.serializeForm();
        var result = {};
        if(courseExamObj.questionGradeTotal != courseExamObj.paperGradeTotal){
            layer.msg("试卷总分和题目总分不一致！", {time: 2000, icon: 5});
            return false;
        }
        if(!jsonData["courseId"]){
            layer.msg("请选择关联课程！", {time: 2000, icon: 5});
            return false;
        }
        if(!jsonData["paperTitle"]){
            layer.msg("请填写试卷标题！", {time: 2000, icon: 5});
            return false;
        }
        if(!jsonData["paperTime"]){
            layer.msg("请填写考试时长！", {time: 2000, icon: 5});
            return false;
        }
        if(jsonData["paperTimeSetting"] == 1 && !jsonData["paperStartTime"]){
            if(!jsonData["paperStartTime"]){
                layer.msg("请填写参加考试的开始时间！", {time: 2000, icon: 5});
                return false;
            }
            if(jsonData["paperEndTime"] && new Date(jsonData["paperStartTime"]) > new Date(jsonData["paperEndTime"])){
                layer.msg("参加考试的开始时间大于结束时间！", {time: 2000, icon: 5});
                return false;
            }
        }
        result["id"] = jsonData["id"]; //试卷主键ID
        result["paperGrade"] = jsonData["paperGrade"]; //试卷分值
        result["courseId"] = jsonData["courseId"]; //关联课程
        result["coursePlate"] = jsonData["coursePlate"]; //所属板块
        result["paperTitle"] = jsonData["paperTitle"]; //试卷标题
        result["paperTime"] = jsonData["paperTime"]; //考试时长
        result["paperTimeSetting"] = jsonData["paperTimeSetting"]; //时间设置
        result["paperWay"] = jsonData["paperWay"]; //试卷展现方式
        result["paperStartTime"] = jsonData["paperStartTime"]; //参与考试开始时间
        result["paperEndTime"] = jsonData["paperEndTime"]; //参与考试截止时间
        result["paperDetailList"] = [];//详情数组
        var questionItemArr = $form.find(".questionItem");
        var autoMarkFlag = 0;//是否可自动阅卷：0-是、1-否，针对于只含有单选、多选、判断题的试卷，系统自动计算成绩
        if(questionItemArr && questionItemArr.length > 0){
            for(var ki = 0; ki < questionItemArr.length; ki++){
                var questionItem = questionItemArr[ki];
                if($(questionItem).hasClass("completeQuestionItem") || $(questionItem).hasClass("essayQuestionItem")){
                    autoMarkFlag = 1;
                }
                var paperDetail = {id:"", questionSeq:"", questionId:"", questionGrade:"", question:{questionType:""}};
                var questionType = $(questionItem).attr("data-type");//题目类型
                var questionTypeName = courseExamObj.questionTypeMap[questionType];
                var questionSeq = $(questionItem).find(".seq").text();//题目序号
                var questionInputArr = $(questionItem).find(".questionInput");
                var isChooseQuestion = $(questionItem).find("input[data-key='questionId']").val() ? true : false;//是否是选择题库的题目
                if(questionInputArr && questionInputArr.length > 0){
                    for(var kk = 0; kk < questionInputArr.length; kk++){
                        var input = questionInputArr[kk];
                        var key = $(input).attr("name");
                        var name = $(input).attr("data-key");
                        var val = jsonData[key];
                        if(Object.getOwnPropertyNames(courseExamObj.questionFieldMap).contains(name) && !val && !isChooseQuestion){
                            layer.msg("【"+questionTypeName+"】第["+questionSeq+"]道题目["+courseExamObj.questionFieldMap[name]+"]未填写！", {time: 2000, icon: 5});
                            return false;
                        }
                        paperDetail.question.questionType = questionType;
                        if("questionGrade" == name || "questionId" == name || "questionSeq" == name){
                            paperDetail[name] = jsonData[key];
                        } else if(name == "paperDetailId"){
                            paperDetail.id = jsonData[key];
                        } else if("questionTitle" == name && questionType == 4){ //填空题，答案在标题里面
                            var complateAnswerArr = jsonData[key].match(new RegExp('\\[.+?\\]', "g"));
                            if((complateAnswerArr && complateAnswerArr.length > 0)){
                                var answerArr = [];
                                $.each(complateAnswerArr, function (iii, answer) {
                                    answerArr.push(answer.substring(1, answer.length - 1));
                                });
                                paperDetail.question["questionAnswer"] = answerArr.join("<^_^>");
                                paperDetail.question["questionAnswerDesc"] = jsonData[key].replace(new RegExp('\\[.+?\\]', "g"), "<^_^>");
                                paperDetail.question[name] = jsonData[key];
                            }else {
                                //如果不是选择题库的题目，则提示
                                if(!isChooseQuestion){
                                    layer.msg("【"+questionTypeName+"】第["+questionSeq+"]道题目["+courseExamObj.questionFieldMap[name]+"]格式不正确！", {time: 2000, icon: 5});
                                    return false;
                                }
                            }
                        }else {
                            if(jsonData[key] instanceof Array){
                                //单选、多选、判断题答案描述必须全部填写
                                if(name == "questionAnswerDesc" && (questionType == 1 || questionType == 2 || questionType == 3)){
                                    for(var ii = 0; ii < jsonData[key].length; ii++){
                                        if(!jsonData[key][ii] && !isChooseQuestion){
                                            layer.msg("【"+questionTypeName+"】第["+questionSeq+"]道题目["+courseExamObj.questionFieldMap[name]+"]填写不完全！", {time: 2000, icon: 5});
                                            return false;
                                        }
                                    }

                                }
                                paperDetail.question[name] = jsonData[key].join("<^_^>");
                            }else {
                                paperDetail.question[name] = jsonData[key];
                            }
                        }
                    }
                }
                result["paperDetailList"].push(paperDetail);
            }
        }
        result["autoMarkFlag"] = autoMarkFlag;
        return result;
    },
    submit: function (t) {
        var $form = $(t).closest(".courseModalCommon").find("form");
        var param = courseExamObj.handleData($form);//处理表单数据
        var url = param.id ? "/paper/edit" : "/paper/save"; //如果有试卷ID，则为编辑操作
        var msgTitle = param.id ? "编辑试卷成功！" : "创建试卷成功！";
        if(param){
            layer.confirm("请确认试卷题目信息，提交后添加的新题目将自动导入题库，编辑试卷仅能添加或移除题目，不可对题目信息进行修改！", {
                btn: ["确认", "取消"],
                shade: [0.7, '#393D49']
            }, function (index) {
                layer.close(index);
                console.log(courseExamObj.paperSubmitFlag);
                if(!courseExamObj.paperSubmitFlag){
                    courseExamObj.paperSubmitFlag = true;
                    commonObj.requestData(JSON.stringify(param), url, "post", "json", true, function (data) {
                        if(data.code == 200){
                            courseExamObj.courseExamModalClose(); //关闭窗口
                            layer.msg(msgTitle, {time: 3000, icon: 6});
                            courseExamObj.init();//刷新表格
                        }else {
                            courseExamObj.paperSubmitFlag = false;
                            layer.msg(data.msg, {time: 3000, icon: 5});
                        }
                    },function () {
                        courseExamObj.paperSubmitFlag = false;
                    }, true);
                }
            });
        }
    },
    enterExamModalShow:function (id, paperTime, paperTitle) {
        courseExamObj.enterExamModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -98000,
            content: $("#enterExamModal").html(),
            btn: [],
            area: ['440px', '400px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                $(layero[0]).find(".studentName").text(user.name);
                $(layero[0]).find(".paperTitle").text(paperTitle);
                $(layero[0]).find(".startTime").text(new Date().format('yyyy.MM.dd hh:mm:ss'));
                var millSecond = new Date().getTime() + paperTime * 60 * 1000;
                $(layero[0]).find(".endTime").text(new Date(millSecond).format('yyyy.MM.dd hh:mm:ss'));
                $(layero[0]).find(".paperTime").text(paperTime+"分钟");
                $(layero[0]).find(".enterExamBtn").attr("data-id", id);
            }
        });
    },
    enterExamModalClose:function () {
        if(courseExamObj.enterExamModalIndex){
            layer.close(courseExamObj.enterExamModalIndex);
        }else {
            layer.closeAll();
        }
    },
    enterExam: function (t) {
        var id = $(t).attr("data-id");
        if(id){
            var url = "/study/courseExam?paperId="+id;
            try {
                window.showModalDialog(url,"","dialogWidth="+(window.screen.availWidth-10)+"px;dialogHeight="+(window.screen.availHeight)+ "px");
            } catch (e) {
                window.open(url, "newwindow",'width='+(window.screen.availWidth-10)+',height='+(window.screen.availHeight)+ ',fullscreen=yes,top=0,left=0,toolbar=no,menubar=no,scrollbars=yes,resizable=no,location=no, status=no');
            }
        }else {
            layer.msg("试卷异常，请刷新页面或联系技术人员！", {time: 2000, icon: 5});
        }
        courseExamObj.enterExamModalClose();
    },
    renderMarkPaper:function (page, answerCardList, courseTitle) {
        var html = "";
        if(answerCardList && answerCardList.length > 0){
            $.each(answerCardList, function (m, answerCard) {
                var remainTime = answerCard.remainTime / 60;
                var examEndTime = answerCard.examEndTime;//考试结束时间
                var markExamHtml = "";//阅卷
                //考试还未结束不能进行阅卷
                if(examEndTime && new Date(examEndTime) < new Date()){
                    markExamHtml +=  "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='courseExamObj.markPaperClick("+answerCard.paperId+", "+answerCard.createId+", "+answerCard.paper.createId+");'>\n" +
                        "                阅卷\n" +
                        "            </button>\n";
                }else {
                    markExamHtml +=  "<button class=\"tableButton background-4BBBFD\" type=\"button\">\n" +
                        "                考试中\n" +
                        "            </button>\n";
                }
                html += "<div class=\"markItem\">\n" +
                    "        <div title=\""+((page -1) * 10 + m+1)+"\">"+((page -1) * 10 + m+1)+"</div>\n" +
                    "        <div title=\""+commonObj.courseSignRangeMap["userMap"][answerCard.createId].name+"\">"+commonObj.courseSignRangeMap["userMap"][answerCard.createId].name+"</div>\n" +
                    "        <div title=\""+answerCard.paper.paperTitle+"\">"+answerCard.paper.paperTitle+"</div>\n" +
                    "        <div title=\""+courseTitle+"\">"+courseTitle+"</div>\n" +
                    "        <div title=\""+commonObj.coursePlateMap[answerCard.paper.coursePlate]+"\">"+commonObj.coursePlateMap[answerCard.paper.coursePlate]+"</div>\n" +
                    "        <div title=\""+courseExamObj.courseWay[answerCard.paper.paperWay]+"\">"+courseExamObj.courseWay[answerCard.paper.paperWay]+"</div>\n" +
                    "        <div title=\""+answerCard.paper.paperTime+"\">"+answerCard.paper.paperTime+"</div>\n" +
                    "        <div title=\""+Math.ceil(answerCard.paper.paperTime - remainTime)+"\">"+Math.ceil(answerCard.paper.paperTime - remainTime)+"</div>\n" +
                    "        <div title=\"" + (answerCard.paperGrade || 0) + "\">" + (answerCard.paperGrade || 0) + "</div>\n" +
                    "        <div>\n" +
                    "            "+markExamHtml+"\n" +
                    "        </div>\n" +
                    "    </div>";
            });
        }
        return html;
    },
    markPaperModalShow: function (paper, courseTitle) {
        courseExamObj.markPaperModalIndex  = layer.open({
            type: 1,
            title: '考生答题卡列表',
            zIndex: -99000,
            content: $("#markPaperModal").html(),
            btn: [],
            area: ['75%', '60%'],
            closeBtn: 1,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                $(layero[0]).find("#trainPaperAnswerExportBtn").attr("data-paperId", paper.id);
                $(layero[0]).find("#trainPaperAnswerExportBtn").attr("data-courseTitle", courseTitle);
                $(layero[0]).find("#trainPaperAnswerExportBtn").attr("data-coursePlate", commonObj.coursePlateMap[paper.coursePlate]);
                //流加载
                layui.use('flow', function(){
                    var flow = layui.flow;
                    flow.load({
                        elem: $(layero[0]).find(".tbodyWrap"),
                        done: function(page, next){
                            //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                            //请求数据，判断当前题目类型进行加载
                            var param = {paperId: paper.id};
                            param.page = page; //页码
                            commonObj.requestData(param, "/answerCard/listAnswerCard", "post", "json", false, function (data) {
                                next(courseExamObj.renderMarkPaper(page, data.list,courseTitle), page < 1); //如果小于总页数，则继续
                            });
                        }
                    });
                });
            }
        });
    },
    markPaperClick:function (paperId,studentId, teacherId) {
        var teacherFlag = teacherId == user.id ? 1 : 0;//如果试卷是当前人创建的则可以阅卷，否则只能查看考试详情
        page("study/markExam?paperId="+paperId+"&studentId="+studentId+"&teacherFlag="+teacherFlag, "考试详情");
    },
    trainPaperAnswerExport: function (t) {
        var paperId = $(t).attr("data-paperId") || "";
        var courseTitle = $(t).attr("data-courseTitle") || "";
        var coursePlate = $(t).attr("data-coursePlate") || "";
        if (paperId) {
            var param = {paperId: paperId, courseTitle: courseTitle, coursePlate: coursePlate};
            var $courseModalCommon = $(t).closest(".courseModalCommon");
            var $tbodyWrap = $courseModalCommon.find(".tbodyWrap");
            //如果有数据则导出，否则提示
            if ($tbodyWrap.find(".markItem").length > 0) {
                location.href = baseUrl + "/answerCard/trainPaperAnswerExport?" + $.param(param);
            } else {
                layer.msg("没有答题卡列表数据，不能进行导出！", {time: 3000, icon: 5});
            }
        } else {
            layer.msg("试卷ID获取失败，不能进行导出！", {time: 3000, icon: 5});
        }
    }
}

//我的测试
var userExamObj = {
    enterExamModalIndex:0,
    getTotalUrl:"/paper/getUserExamTotal",
    userExamListUrl:"/paper/listUserExam",
    courseWay:{0:"逐题展示", 1:"整卷展示"},
    courseState:{0:"启用", 1:"禁用"},
    answerState:{0:"未批改", 1:"已批改", 2:"未考试"},
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
    isCanExam:function (record) {
        var paperStartTime = record.paperStartTime;
        var paperEndTime = record.paperEndTime;
        if(paperStartTime && new Date() < new Date(paperStartTime)){
            return false;
        }
        if(paperEndTime && new Date() > new Date(paperEndTime)){
            return false;
        }
        return true;
    },
    isExamEnd:function (record) {
        var examStartTime = record.examStartTime;
        var examEndTime = record.examEndTime;
        //判断是否在考试范围内
        if(examStartTime && new Date() < new Date(examStartTime)){
            return false;
        }
        if(examEndTime && new Date() > new Date(examEndTime)){
            return false;
        }
        return true;
    },
    userExamListCallback:function (data, target,seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                var opHtml = "";
                //如果未考试、未批改则有考试按钮，如果已批改 或者  试卷禁用 则由考试详情按钮
                if(record.answerCardState == 1){
                    opHtml = "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='userExamObj.markPaperClick("+record.id+", "+record.studentId+")'>\n" +
                             "    考试详情\n" +
                             "</button>\n";
                }else {
                    //试卷状态：0-启用、1-禁用、-9-删除，如果试卷禁用了，则可以看考试详情
                    if(record.state == 1 && record.answerCardState != 2){
                        opHtml = "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='userExamObj.markPaperClick("+record.id+", "+record.studentId+")'>\n" +
                            "    考试详情\n" +
                            "</button>\n";
                    }else if (record.state == 0) {
                        if(userExamObj.isCanExam(record) && userExamObj.isExamEnd(record)){
                            opHtml = "<button class=\"tableButton blueBtn\" type=\"button\" onclick='userExamObj.enterExamModalShow("+JSON.stringify(record)+")'>\n" +
                                "    测试\n" +
                                "</button>\n";
                        }
                    }
                }
                seq += 1;
                html += " <tr>\n" +
                    "         <td title=\""+seq+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+seq+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.paperTitle+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.paperTitle+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.courseTitle+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.courseTitle+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(commonObj.coursePlateMap[record.coursePlate] || "-")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(commonObj.coursePlateMap[record.coursePlate] || "-")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+userExamObj.courseWay[record.paperWay]+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+userExamObj.courseWay[record.paperWay]+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+userExamObj.getDateStr(record.paperStartTime,record.paperEndTime,'yyyy.MM.dd hh:mm')+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+userExamObj.getDateStr(record.paperStartTime,record.paperEndTime,'yyyy.MM.dd hh:mm')+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.paperTime+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.paperTime+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+userExamObj.courseState[record.state]+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+userExamObj.courseState[record.state]+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(record.examGrade || "0")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(record.examGrade || "0")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+userExamObj.answerState[record.answerCardState]+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+userExamObj.answerState[record.answerCardState]+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td>\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+opHtml+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "     </tr>";
            });
        }
        var $parentList = target ? $(target) : $("#userExamList");
        $parentList.html(html);
    },
    init:function () {
        //初始化分页组件
        commonObj.requestData($("#userExamForm").serializeJson(),userExamObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".userExamListPager"),
                    count: data.data.total,
                    url: userExamObj.userExamListUrl,
                    target: $(".userExamList"),
                    param: $("#userExamForm").serializeJson(),
                },userExamObj.userExamListCallback);
            }
        });
    },
    enterExamModalShow:function (record) {
        userExamObj.enterExamModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -98000,
            content: $("#enterExamModal").html(),
            btn: [],
            area: ['440px', '400px'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                //防止页面滞留没刷新，但当前时间已过参考起止时间
                if(!userExamObj.isCanExam(record)){
                    layer.msg("当前时间不在参加考试起止时间范围内，不能进行考试！", {time: 2000, icon: 5});
                    layer.close(index);
                    return;
                }
                //进入考试后会保存开始和结束时间，判断再次考试是否在这个范围内
                if(!userExamObj.isExamEnd(record)){
                    layer.msg("考试试卷已过，不能进行考试！", {time: 2000, icon: 5});
                    layer.close(index);
                    return;
                }

                $(layero[0]).find(".studentName").text(user.name);
                $(layero[0]).find(".paperTitle").text(record.paperTitle);
                //获取开始和截止时间
                var examStartTime = record.examStartTime ? new Date(record.examStartTime).format('yyyy.MM.dd hh:mm:ss') : "";
                var examEndTime = record.examEndTime ? new Date(record.examEndTime).format('yyyy.MM.dd hh:mm:ss') : "";
                if(!examStartTime){
                    examStartTime = new Date().format('yyyy.MM.dd hh:mm:ss');
                }
                if(!examEndTime){
                    var millSecond = new Date().getTime() + record.paperTime * 60 * 1000;
                    examEndTime = new Date(millSecond).format('yyyy.MM.dd hh:mm:ss');
                }
                $(layero[0]).find(".startTime").text(examStartTime);
                $(layero[0]).find(".endTime").text(examEndTime);
                $(layero[0]).find(".paperTime").text(record.paperTime+"分钟");
                $(layero[0]).find(".enterExamBtn").attr("data-id", record.id);
                $(layero[0]).find(".enterExamBtn").attr("data-studentId", user.id);
                $(layero[0]).find(".enterExamBtn").attr("data-examStartTime", examStartTime);
                $(layero[0]).find(".enterExamBtn").attr("data-examEndTime", examEndTime);
            }
        });
    },
    enterExamModalClose:function () {
        if(userExamObj.enterExamModalIndex){
            layer.close(userExamObj.enterExamModalIndex);
        }else {
            layer.closeAll();
        }
    },
    enterExam: function (t) {
        var id = $(t).attr("data-id");
        var studentId = $(t).attr("data-studentId");
        var examStartTime = $(t).attr("data-examStartTime");
        var examEndTime = $(t).attr("data-examEndTime");
        if(id){
            var url = "/study/courseExam?paperId="+id+"&studentId="+studentId+"&examStartTime="+examStartTime+"&examEndTime="+examEndTime;
            try {
                window.showModalDialog(url,"","dialogWidth="+(window.screen.availWidth-10)+"px;dialogHeight="+(window.screen.availHeight)+ "px");
            } catch (e) {
                window.open(url, "newwindow",'width='+(window.screen.availWidth-10)+',height='+(window.screen.availHeight)+ ',fullscreen=yes,top=0,left=0,toolbar=no,menubar=no,scrollbars=yes,resizable=no,location=no, status=no');
            }
        }else {
            layer.msg("试卷异常，请刷新页面或联系技术人员！", {time: 2000, icon: 5});
        }
        userExamObj.enterExamModalClose();
    },
    markPaperClick:function (paperId,studentId) {
        page("study/markExam?paperId="+paperId+"&studentId="+studentId+"&teacherFlag=0", "考试详情");
    }
}

//课程详情/预览
var coursePreviewObj = {
    coursePreviewModalIndex: 0, //课程预览模态框
    currentCourseId: null, //记录当前课程ID
    currentCourseUserId: null, //记录当前课程讲师ID
    courseState:{0:"待审核", 1:"有效", 2:"未报名", 3:"停课", 4:"审核拒绝"},
    courseSignState: {0: "正常", 1: "迟到", 2: "早退", 3: "旷课"},
    courseSignMap: {}, //课程报名数据
    likeSubmitFlag: true, //点赞按钮提交标识，防止双击重复提交
    ventSubmitFlag: true, //吐槽按钮提交标识，防止双击重复提交
    scoreSubmitFlag: true, //评分按钮提交标识，防止双击重复提交
    commentSubmitFlag: true, //发表评论按钮提交标识，防止双击重复提交
    stopSubmitFlag: true, //停课提交标识，防止双击重复提交
    recoverSubmitFlag: true, //复课提交标识，防止双击重复提交
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
            coursePreviewObj.currentCourseUserId = trainCourse.createId;//课程ID
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
                "                <div class=\"userPic\" style='background-image: url("+(commentItemMap.userPic || "/img/mrtx_1.png")+")'></div>\n" +
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
                                                value: courseScore / courseScoreNum / 20
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
                    userPic:(commonObj.courseSignRangeMap.userIconMap[courseComment.createId] || "/img/mrtx_1.png"),
                    userName:commonObj.courseSignRangeMap.userMap[courseComment.createId].name,
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
    reflushCourseDetail:function ($courseModalCommon, trainCourse, showFlag) {
        commonObj.requestData({id:trainCourse.id}, "/trainCourse/getTrainCourseDeailById", "post", "json", true, function (data) {
            if(data.code == 200){
                coursePreviewObj.stopSubmitFlag  = true;//启用
                coursePreviewObj.recoverSubmitFlag = true;//启用
                //如果是讲师，则有学员管理
                if(showFlag == "manage" && data.data.trainCourse && data.data.trainCourse.createId == user.id){
                    //状态：0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回
                    var html = "";
                    if(data.data.trainCourse.state == 1){
                        html = "<div class='modalClickBtn' onclick=\"studentManageObj.studentManageModalShow(this, "+data.data.trainCourse.id+")\">\n" +
                            "           <i class=\"fa fa-group\"></i>&nbsp;\n" +
                            "           学员管理\n" +
                            "       </div>";
                    }
                    if(data.data.trainCourse.state == 2 && new Date(data.data.trainCourse.trainStartTime) > new Date()){
                        html = "<div class='modalClickBtn' onclick='coursePreviewObj.stopCourseClick(this, "+JSON.stringify(data.data.trainCourse)+", \""+showFlag+"\");'>\n" +
                            "           <i class=\"fa fa-power-off\"></i>&nbsp;\n" +
                            "           停课\n" +
                            "       </div>";
                    }
                    if(data.data.trainCourse.state == 3 && new Date(data.data.trainCourse.trainStartTime) > new Date()){
                        html = "<div class='modalClickBtn' onclick='coursePreviewObj.recoverCourseClick(this, "+JSON.stringify(data.data.trainCourse)+", \""+showFlag+"\");'>\n" +
                            "           <i class=\"fa fa-refresh\"></i>&nbsp;\n" +
                            "           复课\n" +
                            "       </div>";
                    }
                    if(html){
                        $courseModalCommon.find(".otherBtn").html(html);
                        $courseModalCommon.find(".otherBtn").css("display", "block");
                    }else {
                        $courseModalCommon.find(".otherBtn").css("display", "none");
                    }
                }
                coursePreviewObj.loadCourseData($courseModalCommon, data.data.trainCourse);//课程数据渲染
                coursePreviewObj.loadCourseCommentData($courseModalCommon, data.data.trainCourse);//课程评论渲染
            }else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
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
                coursePreviewObj.currentCourseUserIdId = null;//重置
                coursePreviewObj.likeSubmitFlag  = true;//启用
                coursePreviewObj.ventSubmitFlag  = true;//启用
                coursePreviewObj.scoreSubmitFlag  = true;//启用
                coursePreviewObj.commentSubmitFlag =  true;//启用

                //审批界面进来，仅有大纲
                if(showFlag && showFlag == "audit"){
                    $(layero[0]).find(".tabWrap > span").css("display", "none");
                    $(layero[0]).find(".tabWrap > span:first-child").css("display", "inline-block");
                    coursePreviewObj.loadCourseData(layero, trainCourse);//课程数据渲染
                }else {
                    //如果是管理栏目，可以查看报名名单
                    if (showFlag == "manage") {
                        $(layero[0]).find(".tabWrap > span:last-child").css("display", "inline-block");
                    } else {
                        $(layero[0]).find(".tabWrap > span:last-child").css("display", "none");
                    }
                    coursePreviewObj.reflushCourseDetail($(layero[0]).find(".courseModalCommon"), trainCourse,showFlag);
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

        //如果是报名名单，则请求数据, 并且有报名人数
        var courseSignNum = $(t).closest(".courseModalCommon").find(".courseSignNum").text() || 0;
        if ("courseSignUserWrap" == $(t).attr("data-target") && courseSignNum > 0) {
            coursePreviewObj.listCourseSignStudent(t);
        }
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
                        $(t).closest(".courseModalCommon").find(".courseLikeNum").text(parseInt($(t).closest(".courseModalCommon").find(".courseLikeNum").text() || 0) + 1);
                    }else {
                        $(t).find("i").removeClass("fa-thumbs-up");
                        $(t).find("i").addClass("fa-thumbs-o-up");
                        $(t).closest(".courseModalCommon").find(".courseLikeNum").text(parseInt($(t).closest(".courseModalCommon").find(".courseLikeNum").text() || 0) - 1);
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
                        $(t).closest(".courseModalCommon").find(".courseVentNum").text(parseInt($(t).closest(".courseModalCommon").find(".courseVentNum").text() || 0) + 1);
                    }else {
                        $(t).find("i").removeClass("fa-thumbs-down");
                        $(t).find("i").addClass("fa-thumbs-o-down");
                        $(t).closest(".courseModalCommon").find(".courseVentNum").text(parseInt($(t).closest(".courseModalCommon").find(".courseVentNum").text() || 0) - 1);
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
                        $(t).closest(".courseModalCommon").find(".courseCommentTotal").text(parseInt($(t).closest(".courseModalCommon").find(".courseCommentTotal").text() || 0) + 1);
                        //追加评论列表
                        $(t).closest(".courseModalCommon").find(".commentDetailList").append(coursePreviewObj.getCourseCommentItem(commentItemMap));
                        //渲染评分
                        layui.use('rate', function(){
                            var rate = layui.rate;
                            rate.render({
                                elem: $(t).closest(".courseModalCommon").find(".courseCommentItem:last-child").find(".userScore"),
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
    },
    stopCourseClick:function (t, trainCourse, showFlag) {
        if(coursePreviewObj.stopSubmitFlag){
            coursePreviewObj.stopSubmitFlag = false;//禁用
            commonObj.requestData({courseId: trainCourse.id, trainStartTime:trainCourse.trainStartTime}, "/trainCourse/stopCourse", "post", "json", true, function (data) {
                if(data.code == 200){
                    coursePreviewObj.stopSubmitFlag = true;//启用
                    layer.msg("课程停课成功", {time: 3000, icon: 6});
                    coursePreviewObj.reflushCourseDetail($(t).closest(".courseModalCommon"), trainCourse, showFlag);
                    courseManageObj.init();//刷新课程管理
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                    coursePreviewObj.stopSubmitFlag = true;//启用
                }
            }, function () {
                coursePreviewObj.stopSubmitFlag = true;//启用
            });
        }
    },
    recoverCourseClick:function (t, trainCourse,showFlag) {
        if(coursePreviewObj.recoverSubmitFlag){
            coursePreviewObj.recoverSubmitFlag = false;//禁用
            commonObj.requestData({courseId: trainCourse.id, trainStartTime:trainCourse.trainStartTime}, "/trainCourse/recoverCourse", "post", "json", true, function (data) {
                if(data.code == 200){
                    coursePreviewObj.recoverSubmitFlag = true;//启用
                    layer.msg("课程复课成功", {time: 3000, icon: 6});
                    coursePreviewObj.reflushCourseDetail($(t).closest(".courseModalCommon"), trainCourse, showFlag);
                    courseManageObj.init();//刷新课程管理
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                    coursePreviewObj.recoverSubmitFlag = true;//启用
                }
            }, function () {
                coursePreviewObj.recoverSubmitFlag = true;//启用
            });
        }
    },
    listCourseSignStudent: function (t) {
        var param = {courseId: coursePreviewObj.currentCourseId};
        //viewFlag: 1-仅看自己、2-部门报名学员、3-培训管理员/讲师自己,  判断顺序，管理员 > 自己课程 > 查看所有课程 > 普通学员 或 讲师但不是自己课程
        param.viewFlag = 1;//普通学员 或 讲师但不是自己课程
        //查看所有课程
        if (commonObj.isViewAllCourse) {
            param.viewFlag = 2;
        }
        //管理员 或 自己课程
        if (commonObj.isAdmin || (commonObj.isTeacher && user.id == coursePreviewObj.currentCourseUserId)) {
            param.viewFlag = 3;
        }
        var $courseModalCommon = $(t).closest(".courseModalCommon");
        var $tbody = $courseModalCommon.find(".tbodyWrap").find("tbody");
        $courseModalCommon.find("#signExportBtn").css("display", "none");//默认隐藏
        commonObj.requestData(param, "/trainCourse/listCourseSignStudent", "post", "json", true, function (data) {
            if (data && data.length > 0) {
                $courseModalCommon.find("#signExportBtn").css("display", "inline-block");//默认隐藏
                var html = "";
                $.each(data, function (i, signUser) {
                    var stateColor = "color-43B9FF";
                    if (signUser.state != 0) {
                        stateColor = "color-F15F5F";
                    }
                    html += "<tr>\n" +
                        "        <td title=\"" + (i + 1) + "\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                " + (i + 1) + "\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\"" + (signUser.userName || "-") + "\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                " + (signUser.userName || "-") + "\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\"" + (signUser.deptName || "-") + "\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                " + (signUser.deptName || "-") + "\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\"" + (signUser.courseTitle || "-") + "\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                " + (signUser.courseTitle || "-") + "\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\"" + (signUser.completeTime || "0") + "\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                " + (signUser.completeTime || "0") + "\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\"" + (signUser.integral || "0") + "\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                " + (signUser.integral || "0") + "\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\"" + (signUser.createDate ? new Date(signUser.createDate).format("yyyy-MM-dd hh:mm:ss") : "-") + "\">\n" +
                        "            <div class=\"ellipsisContent\">\n" +
                        "                " + (signUser.createDate ? new Date(signUser.createDate).format("yyyy-MM-dd hh:mm:ss") : "-") + "\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td title=\"" + (signUser.state == 0 ? "正常" : coursePreviewObj.courseSignMap[signUser.state]) + "\">\n" +
                        "            <div style='' class=\"ellipsisContent " + stateColor + "\">\n" +
                        "                " + (signUser.state == 0 ? "正常" : coursePreviewObj.courseSignMap[signUser.state]) + "\n" +
                        "            </div>\n" +
                        "        </td>\n" +
                        "    </tr>";
                });
                $tbody.html(html);
            }
        });
    },
    listCourseSignExport: function (t) {
        var param = {courseId: coursePreviewObj.currentCourseId};
        //viewFlag: 1-仅看自己、2-部门报名学员、3-培训管理员/讲师自己,  判断顺序，管理员 > 自己课程 > 查看所有课程 > 普通学员 或 讲师但不是自己课程
        param.viewFlag = 1;//普通学员 或 讲师但不是自己课程
        //查看所有课程
        if (commonObj.isViewAllCourse) {
            param.viewFlag = 2;
        }
        //管理员 或 自己课程
        if (commonObj.isAdmin || (commonObj.isTeacher && user.id == coursePreviewObj.currentCourseUserId)) {
            param.viewFlag = 3;
        }
        var $courseModalCommon = $(t).closest(".courseModalCommon");
        var $tbody = $courseModalCommon.find(".tbodyWrap").find("tbody");
        //如果有数据则导出，否则提示
        if ($tbody.find("tr").length > 0) {
            location.href = baseUrl + "/trainCourse/listCourseSignStudentExport?" + $.param(param);
        } else {
            layer.msg("没有报名列表数据，不能进行导出！", {time: 3000, icon: 5});
        }
    }
}

//学员管理
var studentManageObj = {
    studentManageModalIndex: 0,
    courseSignListUrl: "/trainCourse/listCourseSign",
    signState:{
        0:{name:"正常", cls:"color-43B9FF"},
        1:{name:"迟到", cls:"color-F15F5F"},
        2:{name:"早退", cls:"color-F15F5F"},
        3:{name:"旷课", cls:"color-F15F5F"}
    },
    editCourseSignStateFlagList: [], //修改报名状态提交标识，防止双击, 数据类型：[userId]
    renderTd:function (seq, record) {
        var html = "";
        if(record){
            var opHtml = "";
            var readonly = "";
            var inputStyle = "border: unset;";
            //状态：0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回
            if(record.state == 0){
                opHtml += "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='studentManageObj.editCourseSignState(this, "+JSON.stringify(record)+", 1)'>\n" +
                    "           迟到\n" +
                    "      </button>\n";
                opHtml += "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='studentManageObj.editCourseSignState(this, "+JSON.stringify(record)+", 2)'>\n" +
                    "           早退\n" +
                    "      </button>\n";
                opHtml += "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='studentManageObj.editCourseSignState(this, "+JSON.stringify(record)+", 3)'>\n" +
                    "           旷课\n" +
                    "      </button>\n";
                inputStyle = "border: 1px solid #43B9FF;"
            }else {
                readonly = "readonly";
                inputStyle = "border: unset;"
            }
            html += "    <td title=\""+seq+"\">\n" +
                "            <div class=\"ellipsisContent\">\n" +
                "                "+seq+"\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td title=\""+(commonObj.courseSignRangeMap["userMap"][record.userId].name || "-")+"\">\n" +
                "            <div class=\"ellipsisContent\">\n" +
                "                "+(commonObj.courseSignRangeMap["userMap"][record.userId].name || "-")+"\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td title=\""+(commonObj.courseSignRangeMap["userMap"][record.userId].groupName || "-")+"\">\n" +
                "            <div class=\"ellipsisContent\">\n" +
                "                "+(commonObj.courseSignRangeMap["userMap"][record.userId].groupName || "-")+"\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td title=\""+(commonObj.courseSignRangeMap["userMap"][record.userId].postName || "-")+"\">\n" +
                "            <div class=\"ellipsisContent\">\n" +
                "                "+(commonObj.courseSignRangeMap["userMap"][record.userId].postName || "-")+"\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td title=\""+(record.completeRate || 0)+"\">\n" +
                "            <div class=\"ellipsisContent\">\n" +
                "                "+(record.completeRate || 0)+"\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td title=\""+(record.completeTime || 0)+"\">\n" +
                "            <div class=\"ellipsisContent\">\n" +
                "               <input style='"+inputStyle+"' type=\"text\" value=\""+(record.completeTime || 0)+"\" "+readonly+" class=\"editInput\" onkeyup=\"value=value.replace(/[^\\d]/g,'')\">\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td title=\""+(record.integral || 0)+"\">\n" +
                "            <div class=\"ellipsisContent\">\n" +
                "                "+(record.integral || 0)+"\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td title=\""+studentManageObj.signState[record.state].name+"\">\n" +
                "            <div class=\"ellipsisContent\">\n" +
                "                <span class='"+studentManageObj.signState[record.state].cls+"'>"+studentManageObj.signState[record.state].name+"</span>\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td>\n" +
                "            <div class=\"ellipsisContent\">\n" +
                "                "+opHtml+"\n" +
                "            </div>\n" +
                "        </td>\n";
        }
        return html;
    },
    studentManageListCallback:function (dataList, target) {
        var html = "";
        if(dataList && dataList.length > 0){
            $.each(dataList, function (i, record) {
                var opHtml = "";
                //状态：0-待审批、1-有效（统计讲师积分时，仅算该状态）、2-未报名（报名人数为0）、3-停课（停课必须建立在未报名状态下）、4-审核驳回
                if(record.state == 0){
                    opHtml += "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='studentManageObj.editCourseSignState(this, "+JSON.stringify(record)+", 1)'>\n" +
                        "           迟到\n" +
                        "      </button>\n";
                    opHtml += "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='studentManageObj.editCourseSignState(this, "+JSON.stringify(record)+", 2)'>\n" +
                        "           早退\n" +
                        "      </button>\n";
                    opHtml += "<button class=\"tableButton orangeBtn\" type=\"button\" onclick='studentManageObj.editCourseSignState(this, "+JSON.stringify(record)+", 3)'>\n" +
                        "           旷课\n" +
                        "      </button>\n";
                }
                html += "<tr onmouseover=\"commonObj.mouseOver(this);\" onmouseout=\"commonObj.mouseOut(this);\">\n" +
                    "       "+studentManageObj.renderTd(i+1, record)+"\n" +
                    "    </tr>";
            });
        }
        var $parentList = target ? $(target) : $("#courseStudentList");
        $parentList.html(html);
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    studentManageModalShow:function (t, trainCourseId) {
        coursePreviewObj.coursePreviewModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -98000,
            content: $("#studentManageModal").html(),
            btn: [],
            area: ['65%', '90%'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                studentManageObj.editCourseSignStateFlagList = [];//重置

                commonObj.requestData({courseId:trainCourseId}, studentManageObj.courseSignListUrl, "post", "json", true, function (data) {
                    if(data.code == 200){
                        studentManageObj.studentManageListCallback(data.data.studentList, $(layero[0]).find(".courseStudentList"));
                    }else {
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }
                });
            }
        });
    },
    studentManageModalClose:function () {
        if(studentManageObj.studentManageModalIndex){
            layer.close(studentManageObj.studentManageModalIndex);
        }else {
            layer.closeAll();
        }
    },
    editCourseSignState:function (t, courseSign, state) {
        if(!studentManageObj.editCourseSignStateFlagList.contains(courseSign.userId)){
            studentManageObj.editCourseSignStateFlagList.push(courseSign.userId);//禁用
            var param = {
                id:courseSign.id,
                courseId:courseSign.courseId,
                userId:courseSign.userId,
                completeTime: $(t).closest("tr").find(".editInput").val() || "0",
                state: state};
            commonObj.requestData(JSON.stringify(param), "/trainCourse/courseSignState", "post", "json", true, function (data) {
                if(data.code == 200){
                    layer.msg("设置学员课程状态成功", {time: 3000, icon: 6});
                    //隐藏按钮 + 修改状态
                    $(t).closest("tr").html(studentManageObj.renderTd($(t).closest("tr").find("td:first-child").attr("title"), data.data.courseSign));
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                    studentManageObj.editCourseSignStateFlagList.remove(courseSign.userId);//启用
                }
            }, function () {
                studentManageObj.editCourseSignStateFlagList.remove(courseSign.userId);//启用
            }, true);
        }else {
            layer.msg("学员课程状态已修改", {time: 3000, icon: 6});
        }
    }
}

//页面公共处理对象
var commonObj = {
    isAdmin: false, //是否管理员，管理员可查看全部讲师，讲师只查看自己，非讲师将无数据显示
    isTeacher:false, //是否讲师, 讲师只查看自己，非讲师将无数据显示
    isViewAllCourse: false, //是否可查看所有课程，部长级及以上领导（政委、总监、总经理）需要能够查看每个课程中本部门组织下成员的报名情况；
    listTrainSettingUrl:"/trainSetting/listTrainSetting", //培训设置列表url
    coursePlateList: [], //培训板块缓存
    coursePlateMap: {}, //培训板块缓存，数据格式：{id：name}
    trainWayMap: {}, //培训方式缓存，数据格式：{id：name}
    trainWayList: [], //培训方式缓存
    roleTypeMap: {}, //角色类型缓存，课程范围弹窗使用
    courseSignRangeMap: {deptMap:{}, deptList:[],  roleMap:{}, roleList:[],  userMap:{}, userIconMap: {}, userList:[]}, //课程报名范围对应的值
    tabs:{
        0:{id:"courseAudit", tabId:"courseAuditTab", obj:courseAuditObj},
        1:{id:"courseManage", tabId:"courseManageTab", obj:courseManageObj},
        2:{id:"courseSign", tabId:"courseSignTab", obj:courseSignObj},
        3:{id:"courseExam", tabId:"courseExamTab", obj:courseExamObj},
        4:{id:"userExam", tabId:"userExamTab", obj:userExamObj},
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
    //获取用户权限
    getTrainPermission:function () {
        commonObj.requestData(null, "/trainSetting/getTrainPermission", "post", "json", false, function (data) {
            if(data){
                commonObj.isAdmin = data["admin"] || false;
                commonObj.isTeacher = data["teacher"] || false;
                commonObj.isViewAllCourse = data["viewAllCourse"] || false;
            }
        });
    },
    //根据权限控制tab栏目
    controlTab:function () {
        //管理员有课程审批功能
        if(commonObj.isAdmin){
            $("#courseAuditTab").css("display", "inline-block");
        }else {
            $("#courseAuditTab").css("display", "none");
        }

        //讲师、管理员或查看所有课程权限有课程管理功能
        if (commonObj.isAdmin || commonObj.isTeacher || commonObj.isViewAllCourse) {
            $("#courseManageTab").css("display", "inline-block");
        } else {
            $("#courseManageTab").css("display", "none");
        }

        //讲师和管理员有试卷管理功能
        if(commonObj.isAdmin || commonObj.isTeacher){
            $("#courseExamTab").css("display", "inline-block");
        }else {
            $("#courseExamTab").css("display", "none");
        }
    },
    //页面进入
    initBefore: function () {
        //缓存培训方式、培训板块，如果没有值则去请求
        if(commonObj.coursePlateList.length < 1 || commonObj.trainWayList.length < 1){
            commonObj.coursePlateList = []; //重置
            commonObj.coursePlateMap = {}; //重置
            commonObj.trainWayMap = {}; //重置
            commonObj.trainWayList = []; //重置
            commonObj.requestData({settingModuleList: "TRAIN_WAY,COURSE_PLATE",orderFlag:true}, commonObj.listTrainSettingUrl, "post", "json", false, function (data) {
                if(data && data.length > 0){
                    $.each(data, function (i, trainSetting) {
                        if("TRAIN_WAY" == trainSetting.settingModule){
                            commonObj.trainWayList.push(trainSetting);
                            commonObj.trainWayMap[trainSetting.id] = trainSetting.settingValue;
                        }else {
                            commonObj.coursePlateList.push(trainSetting);
                            commonObj.coursePlateMap[trainSetting.id] = trainSetting.settingValue;
                        }
                    });
                }
            });
        }

        //缓存课程范围对应的部门、角色、用户
        if(commonObj.courseSignRangeMap["deptList"] && commonObj.courseSignRangeMap["deptList"].length < 1){
            commonObj.requestData({signStr:"dept"}, "/trainCourse/listCourseRange", "post", "json", false, function (data) {
                if(data && data.length > 0){
                    $.each(data, function (x, rec) {
                        commonObj.courseSignRangeMap["deptList"].push(rec);
                        commonObj.courseSignRangeMap["deptMap"][rec.itemValue] = rec.name;
                    });
                }
            });
        }
        if(commonObj.courseSignRangeMap["roleList"] && commonObj.courseSignRangeMap["roleList"].length < 1){
            commonObj.requestData({signStr:"role"}, "/trainCourse/listCourseRange", "post", "json", false, function (data) {
                if(data && data.length > 0){
                    $.each(data, function (x, rec) {
                        commonObj.courseSignRangeMap["roleList"].push(rec);
                        commonObj.courseSignRangeMap["roleMap"][rec.itemValue] = rec.name;
                    });
                }
            });
        }
        if(commonObj.courseSignRangeMap["userList"] && commonObj.courseSignRangeMap["userList"].length < 1){
            commonObj.requestData({signStr:"user"}, "/trainCourse/listCourseRange", "post", "json", false, function (data) {
                if(data && data.length > 0){
                    $.each(data, function (x, rec) {
                        commonObj.courseSignRangeMap["userList"].push(rec);
                        commonObj.courseSignRangeMap["userMap"][rec.itemValue] = rec;
                        commonObj.courseSignRangeMap["userIconMap"][rec.itemValue] = rec.image;
                    });
                }
            });
        }

        //渲染下拉列表-培训方式
        var trainWayHtml = "<option value=\"\">请选择培训方式</option>";
        if(commonObj.trainWayList && commonObj.trainWayList.length > 0){
            $.each(commonObj.trainWayList, function (i, trainSetting) {
                trainWayHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
            });
        }
        $("#courseAudit").find("select[name='trainWay']").html(trainWayHtml);
        $("#courseManage").find("select[name='trainWay']").html(trainWayHtml);
        $("#courseSign").find("select[name='trainWay']").html(trainWayHtml);

        //渲染下拉列表-课程板块
        var trainPlateHtml = "<option value=\"\">请选择课程板块</option>";
        if(commonObj.coursePlateList && commonObj.coursePlateList.length > 0){
            $.each(commonObj.coursePlateList, function (i, trainSetting) {
                trainPlateHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
            });
        }
        $("#courseAudit").find("select[name='coursePlate']").html(trainPlateHtml);
        $("#courseExam").find("select[name='coursePlate']").html(trainPlateHtml);
        $("#courseManage").find("select[name='coursePlate']").html(trainPlateHtml);
        $("#courseSign").find("select[name='coursePlate']").html(trainPlateHtml);
        $("#userExam").find("select[name='coursePlate']").html(trainPlateHtml);

        //渲染下拉列表-讲师
        var userHtml = "<option value=\"\">请选择讲师</option>";
        if(commonObj.courseSignRangeMap["userList"] && commonObj.courseSignRangeMap["userList"].length > 0){
            $.each(commonObj.courseSignRangeMap["userList"], function (i, trainTeacher) {
                userHtml += "<option value=\""+trainTeacher.itemValue+"\">"+trainTeacher.name+"</option>";
            });
        }
        $("#courseAudit").find("select[name='createId']").html(userHtml);
        $("#courseManage").find("select[name='createId']").html(userHtml);
        $("#courseSign").find("select[name='createId']").html(userHtml);
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
    //回车键事件
    enterEvent:function (tabIndex, methonName, event, sourceNode) {
        if((event.keyCode == '13' || event.keyCode == 13) && commonObj.tabs[tabIndex].obj && commonObj.tabs[tabIndex].obj[methonName]){
            if(sourceNode){
                commonObj.tabs[tabIndex].obj[methonName](sourceNode);
            }else {
                commonObj.tabs[tabIndex].obj[methonName]();
            }

        }
    },
    //表格行鼠标移动事件
    mouseOver: function (t) {
        $(t).find(".editInput").each(function (i, btn) {
            $(btn).css("background-color", "#F2F2F2");
        });
    },
    //表格行鼠标移出时间
    mouseOut: function (t) {
        $(t).find(".editInput").each(function (i, btn) {
            $(btn).css("background-color", "white");
        });
    },
}