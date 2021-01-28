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

//培训讲师管理
var trainTeacherManageObj = {
    getTotalUrl:"/trainTeacher/getTrainTeacherTotal",
    trainTeacherManageListUrl:"/trainTeacher/listTrainTeacher",
    teacherModalIndex: 0, //培训讲师模态框
    teacherSubmitFlag: true, //提交按钮锁定标识
    applyTeacherMap: {}, //申请讲师缓存
    educationFlagMap:{0:"无", 1:"有"},
    sexMap:{0:"女", 1:"男"},
    renderTeacherLevel:function (intergal) {
        var levelHtml = "-";
        if(commonObj.teacherLevelList && commonObj.teacherLevelList.length > 0){
            for (var jj = 0; jj < commonObj.teacherLevelList.length; jj++){
                if(intergal < (commonObj.teacherLevelList[jj].ruleValue || 0)){
                    levelHtml = commonObj.teacherLevelList[jj].settingValue;
                    break;
                }
            }
        }
        return levelHtml;
    },
    trainTeacherManageListCallback: function (data, target,seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                seq += 1;
                html += " <tr>\n" +
                    "         <td title=\""+seq+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+seq+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.user.name+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.user.name+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+trainTeacherManageObj.renderTeacherLevel(record.teacherIntegral)+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+trainTeacherManageObj.renderTeacherLevel(record.teacherIntegral)+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(commonObj.goodAtFieldMap[record.goodAtFieldId] || "无")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(commonObj.goodAtFieldMap[record.goodAtFieldId] || "无")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(trainTeacherManageObj.educationFlagMap[record.educationFlag] || "-")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(trainTeacherManageObj.educationFlagMap[record.educationFlag] || "-")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(record.user.deptName || "-")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(record.user.deptName || "-")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(record.user.postName || "-")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(record.user.postName || "-")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(trainTeacherManageObj.sexMap[record.user.sex] || "-")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(trainTeacherManageObj.sexMap[record.user.sex] || "-")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(record.user.phone || "-")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(record.user.phone || "-")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.createDate+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.createDate+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td>\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 <button class=\"tableButton blueBtn\" type=\"button\" onclick='trainTeacherManageObj.trainTeacherModalShow("+JSON.stringify(record)+")'>\n" +
                    "                     编辑\n" +
                    "                 </button>\n" +
                    "                 <button class=\"tableButton orangeBtn\" type=\"button\" onclick='trainTeacherManageObj.trainTeacherDel("+record.id+", "+record.user.id+")'>\n" +
                    "                     删除\n" +
                    "                 </button>\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "     </tr>";
            });
        }
        var $parentList = target ? $(target) : $(".trainTeacherManageList");
        $parentList.html(html);
    },
    init: function () {
        //如果是讲师，则只能查看自己的，管理员可以查看所有讲师
        var param = $("#trainTeacherManageForm").serializeJson();
        if(!commonObj.isAdmin){
            param.userId = user.id;
        }
        //初始化分页组件
        commonObj.requestData(param,trainTeacherManageObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".trainTeacherManageListPager"),
                    count: data.data.total,
                    url: trainTeacherManageObj.trainTeacherManageListUrl,
                    target: $(".trainTeacherManageList"),
                    param: param,
                },trainTeacherManageObj.trainTeacherManageListCallback);
            }
        });
    },
    trainTeacherModalShow: function (trainTeacher) {
        trainTeacherManageObj.teacherModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#trainTeacherModal").html(),
            btn: [],
            area: ['65%', '55%'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                trainTeacherManageObj.teacherSubmitFlag = true;//启用按钮
                trainTeacherManageObj.applyTeacherMap = {}; //重置讲师缓存

                //渲染下拉列表-擅长领域
                var goodAtFieldHtml = "<option value=\"\">请选择擅长领域</option>";
                if(commonObj.goodAtFieldList && commonObj.goodAtFieldList.length > 0){
                    $.each(commonObj.goodAtFieldList, function (i, trainSetting) {
                        goodAtFieldHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                    });
                }
                $(layero[0]).find("select[name='goodAtFieldId']").html(goodAtFieldHtml);

                //判断新增还是编辑
                if(trainTeacher){
                    $(layero[0]).find(".trainModalTitle").text("编辑讲师");
                    //讲师渲染
                    $(layero[0]).find("select[name='userId']").attr("disabled", "true");
                    $(layero[0]).find("select[name='userId']").html("<option value=\""+trainTeacher.userId+"\">"+trainTeacher.user.name+"</option>");
                    $(layero[0]).find("input[name='userId']").val(trainTeacher["userId"] || "");
                    $(layero[0]).find("input[name='id']").val(trainTeacher["id"] || "");
                    if(trainTeacher["user"]){
                        $(layero[0]).find(".deptName").val(trainTeacher["user"]["deptName"] || "-");
                        $(layero[0]).find(".postName").val(trainTeacher["user"]["postName"] || "-");
                        $(layero[0]).find(".sex").val(trainTeacherManageObj.sexMap[trainTeacher["user"]["sex"]] || "-");
                        $(layero[0]).find(".phone").val(trainTeacher["user"]["phone"] || "-");
                    }
                    $(layero[0]).find("select[name='goodAtFieldId']").val(trainTeacher["goodAtFieldId"] || "");
                    $(layero[0]).find("select[name='educationFlag']").val(trainTeacher["educationFlag"] || "");
                    $(layero[0]).find("textarea[name='introduce']").val(trainTeacher["introduce"] || "");
                }else {
                    $(layero[0]).find(".trainModalTitle").text("申请讲师");

                    //渲染下拉列表-讲师名称，如果不是管理员，则只是用户自己申请，则下拉列表仅展示自己
                    if(commonObj.isAdmin){
                        //讲师渲染
                        $(layero[0]).find("select[name='userId']").removeAttr("disabled");
                        commonObj.requestData({existsFlag: false}, "/trainTeacher/listUserTeacher", "post", "json", false, function (data) {
                            var teacherHtml = "<option value=\"\">请选择讲师名称</option>";
                            if(data && data.length > 0){
                                $.each(data, function (i, trainTeacher) {
                                    teacherHtml += "<option value=\""+trainTeacher.sysUserId+"\">"+trainTeacher.name+"</option>";
                                    trainTeacherManageObj.applyTeacherMap[trainTeacher.sysUserId] = trainTeacher;
                                });
                            }
                            $(layero[0]).find("select[name='userId']").html(teacherHtml);
                        });
                    }else {
                        $(layero[0]).find("select[name='userId']").attr("disabled", "true");
                        $(layero[0]).find("select[name='userId']").html("<option value=\""+user.id+"\">"+user.name+"</option>");
                        $(layero[0]).find("input[name='userId']").val(user.id || "");
                        $(layero[0]).find(".deptName").val(user.deptName || "-");
                        $(layero[0]).find(".postName").val(user.postName || "-");
                        $(layero[0]).find(".sex").val(trainTeacherManageObj.sexMap[user.sex] || "-");
                        $(layero[0]).find(".phone").val(user.phone || "-");
                    }

                    //渲染下拉列表-讲师名称改变事件
                    layui.use('form', function(){
                        var form = layui.form;
                        form.on('select(userId)', function(data){
                            if(data.value){
                                var trainUser = trainTeacherManageObj.applyTeacherMap[data.value] || {};
                                $(layero[0]).find(".deptName").val(trainUser["deptName"] || "-");
                                $(layero[0]).find(".postName").val(trainUser["postName"] || "-");
                                $(layero[0]).find(".sex").val(trainTeacherManageObj.sexMap[trainUser["sex"]] || "-");
                                $(layero[0]).find(".phone").val(trainUser["phone"] || "-");
                            }else {
                                $(layero[0]).find(".deptName").val("-");
                                $(layero[0]).find(".postName").val("-");
                                $(layero[0]).find(".sex").val("-");
                                $(layero[0]).find(".phone").val("-");
                            }
                        });
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
    trainTeacherModalClose: function () {
        if(trainTeacherManageObj.teacherModalIndex){
            layer.close(trainTeacherManageObj.teacherModalIndex);
        }else {
            layer.closeAll();
        }
    },
    trainTeacherSubmit: function (t) {
        var $form = $(t).closest(".trainModalCommon").find("form");
        var jsonData = $form.serializeForm();
        if(!jsonData["id"] && !jsonData["userId"]){
            layer.msg("请选择请选择讲师名称！", {time: 2000, icon: 5});
            return;
        }
        var url = jsonData["id"] ? "/trainTeacher/update" : "/trainTeacher/save"; //如果有ID，则为编辑操作
        var msgTitle = jsonData.id ? "编辑培训讲师成功！" : "申请培训讲师成功！";
        //如果没有锁定按钮，则发送请求
        if(trainTeacherManageObj.teacherSubmitFlag){
            trainTeacherManageObj.teacherSubmitFlag = false;
            commonObj.requestData(JSON.stringify(jsonData), url, "post", "json", true, function (data) {
                if(data.code == 200){
                    trainTeacherManageObj.trainTeacherModalClose(); //关闭窗口
                    layer.msg(msgTitle, {time: 3000, icon: 6});
                    //如果是申请讲师，并且不是管理员，申请按钮关闭
                    if(!jsonData["id"] && !commonObj.isAdmin){
                        $("#teacherApplyBtn").css("display", "none");
                    }

                    trainTeacherManageObj.init();//刷新表格
                }else {
                    trainTeacherManageObj.teacherSubmitFlag = true;
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
            },function () {
                trainTeacherManageObj.teacherSubmitFlag = true;
            }, true);
        }

    },
    trainTeacherDel:function (id, teacherId) {
        if(user.id == teacherId){
            layer.msg("不能删除自己！", {time: 2000, icon: 5});
        }else {
            commonObj.requestData({id:id, teacherId:teacherId}, "/trainTeacher/del", "post", "json", true, function (data) {
                if(data.code == 200){
                    trainTeacherManageObj.init();//刷新表格
                    layer.msg("删除讲师成功！", {time: 2000, icon: 6});
                }else {
                    layer.msg(data.msg, {time: 2000, icon: 5});
                }
            });
        }

    }
}

//培训讲师统计
var trainTeacherStatisticsObj = {
    getTotalUrl:"/trainTeacher/getTrainTeacherTotal",
    trainTeacherStatisticsListUrl:"/trainTeacher/listTrainTeacher",
    teacherModalIndex: 0, //培训讲师模态框
    teacherSubmitFlag: true, //提交按钮锁定标识
    educationFlagMap:{0:"无", 1:"有"},
    sexMap:{0:"女", 1:"男"},
    trainTeacherStatisticsListCallback: function (data, target,seq) {
        var html = "";
        $("#teacherListExportBtn").css("display", "none");
        if(data && data.list && data.list.length > 0){
            $("#teacherListExportBtn").css("display", "inline-block");
            $.each(data.list, function (i, record) {
                var teacherComplateCourseNum = record.teacherComplateCourseNum || 0;//完课课程数
                var teacherCourseAvgScore = teacherComplateCourseNum == 0 ? "0.00" : ((record.teacherCourseAvgScore || 0) / teacherComplateCourseNum).toFixed(2);
                var teacherCourseAvgLikeNum = teacherComplateCourseNum == 0 ? "0.00" : ((record.teacherCourseLikeNum || 0) / teacherComplateCourseNum).toFixed(2);
                var teacherCourseAvgVentNum = teacherComplateCourseNum == 0 ? "0.00" : ((record.teacherCourseVentNum || 0) / teacherComplateCourseNum).toFixed(2);
                seq += 1;
                html += "<tr>\n" +
                    "        <td title=\""+seq+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+seq+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+record.user.name+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+record.user.name+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(record.teacherCourseNum || 0)+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(record.teacherCourseNum || 0)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(record.teacherIntegral || 0)+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(record.teacherIntegral || 0)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+teacherCourseAvgScore+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+teacherCourseAvgScore+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+teacherCourseAvgLikeNum+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+teacherCourseAvgLikeNum+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+teacherCourseAvgVentNum+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "               "+teacherCourseAvgVentNum+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(record.teacherCourseCommentNum || 0)+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(record.teacherCourseCommentNum || 0)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                <button class=\"tableButton blueBtn\" type=\"button\" onclick='trainTeacherStatisticsObj.trainTeacherModalShow("+JSON.stringify(record)+")'>\n" +
                    "                    查看详情\n" +
                    "                </button>\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "    </tr>";
            });
        }
        var $parentList = target ? $(target) : $(".trainTeacherStatisticsList");
        $parentList.html(html);
    },
    init: function () {
        //如果是讲师，则只能查看自己的，管理员可以查看所有讲师
        var param = $("#trainTeacherStatisticsForm").serializeJson();
        if(!commonObj.isAdmin){
            param.userId = user.id;
        }
        //初始化分页组件
        commonObj.requestData(param,trainTeacherStatisticsObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".trainTeacherStatisticsListPager"),
                    count: data.data.total,
                    url: trainTeacherStatisticsObj.trainTeacherStatisticsListUrl,
                    target: $(".trainTeacherStatisticsList"),
                    param: param,
                },trainTeacherStatisticsObj.trainTeacherStatisticsListCallback);
            }
        });
    },
    trainTeacherModalShow: function (trainTeacher) {
        trainTeacherStatisticsObj.teacherModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#trainTeacherViewModal").html(),
            btn: [],
            area: ['65%', '55%'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                //渲染下拉列表-擅长领域
                var goodAtFieldHtml = "<option value=\"\">请选择擅长领域</option>";
                if(commonObj.goodAtFieldList && commonObj.goodAtFieldList.length > 0){
                    $.each(commonObj.goodAtFieldList, function (i, trainSetting) {
                        goodAtFieldHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                    });
                }
                $(layero[0]).find("select[name='goodAtFieldId']").html(goodAtFieldHtml);

                //讲师渲染
                $(layero[0]).find("select[name='userId']").html("<option value=\""+trainTeacher.userId+"\">"+trainTeacher.user.name+"</option>");
                if(trainTeacher["user"]){
                    $(layero[0]).find(".deptName").val(trainTeacher["user"]["deptName"] || "-");
                    $(layero[0]).find(".postName").val(trainTeacher["user"]["postName"] || "-");
                    $(layero[0]).find(".sex").val(trainTeacherStatisticsObj.sexMap[trainTeacher["user"]["sex"]] || "-");
                    $(layero[0]).find(".phone").val(trainTeacher["user"]["phone"] || "-");
                }
                $(layero[0]).find("select[name='goodAtFieldId']").val(trainTeacher["goodAtFieldId"] || "");
                $(layero[0]).find("select[name='educationFlag']").val(trainTeacher["educationFlag"] || "");
                $(layero[0]).find("textarea[name='introduce']").val(trainTeacher["introduce"] || "");
            }
        });
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    trainTeacherModalClose: function () {
        if(trainTeacherStatisticsObj.teacherModalIndex){
            layer.close(trainTeacherStatisticsObj.teacherModalIndex);
        }else {
            layer.closeAll();
        }
    },
    trainTeacherStatisticsExport: function () {
        //如果是讲师，则只能查看自己的，管理员可以查看所有讲师
        var param = $("#trainTeacherStatisticsForm").serializeJson();
        if (!commonObj.isAdmin) {
            param.userId = user.id;
        }
        //如果有数据则导出，否则提示
        if ($(".trainTeacherStatisticsList").find("tr").length > 0) {
            location.href = baseUrl + "/trainTeacher/trainTeacherStatisticsExport?" + $.param(param);
        } else {
            layer.msg("没有讲师列表数据，不能进行导出！", {time: 3000, icon: 5});
        }
    }
}

//页面公共处理对象
var commonObj = {
    isAdmin: false, //是否管理员，管理员可查看全部讲师，讲师只查看自己，非讲师将无数据显示
    isTeacher:false, //是否讲师, 讲师只查看自己，非讲师将无数据显示
    listTrainSettingUrl:"/trainSetting/listTrainSetting", //培训设置列表url
    goodAtFieldList: [], //擅长领域缓存
    goodAtFieldMap: {}, //擅长领域缓存，数据格式：{id：name}
    teacherLevelMap: {}, //讲师等级缓存，数据格式：{id：{name:'等级描述', level:'层级', value:'积分值'}}
    teacherLevelList: [], //讲师等级缓存
    teacherListUrl:"/trainTeacher/listUserTeacher", //讲师列表Url
    teacherList:[],
    tabs:{
        0:{id:"trainTeacherManage", tabId:"trainTeacherManageTab", obj:trainTeacherManageObj},
        1:{id:"trainTeacherStatistics", tabId:"trainTeacherStatisticsTab", obj:trainTeacherStatisticsObj},
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
        //如果有管理员 或者 讲师 权限，则展示讲师统计栏目
        if(commonObj.isAdmin || commonObj.isTeacher){
            $("#trainTeacherStatisticsTab").css("display", "inline-block");
        }else {
            $("#trainTeacherStatisticsTab").css("display", "none");
        }

        //如果已经是讲师了，则申请讲师按钮隐藏
        if(commonObj.isTeacher){
            $("#teacherApplyBtn").css("display", "none");
        }else {
            $("#teacherApplyBtn").css("display", "block");
        }
    },
    //页面进入
    initBefore: function () {
        //缓存培训方式、培训板块，如果没有值则去请求
        if(commonObj.goodAtFieldList.length < 1 || commonObj.teacherLevelList.length < 1){
            commonObj.goodAtFieldList = []; //重置
            commonObj.goodAtFieldMap = {}; //重置
            commonObj.teacherLevelMap = {}; //重置
            commonObj.teacherLevelList = []; //重置
            commonObj.requestData({settingModuleList: "GOOD_AT_FIELD,TEACHER_LEVEL",orderFlag:true}, commonObj.listTrainSettingUrl, "post", "json", false, function (data) {
                if(data && data.length > 0){
                    $.each(data, function (i, trainSetting) {
                        if("GOOD_AT_FIELD" == trainSetting.settingModule){
                            commonObj.goodAtFieldList.push(trainSetting);
                            commonObj.goodAtFieldMap[trainSetting.id] = trainSetting.settingValue;
                        }else {
                            commonObj.teacherLevelList.push(trainSetting);
                            commonObj.teacherLevelMap[trainSetting.id] = {name:trainSetting.settingValue, level:trainSetting.settingLevel, value:(trainSetting.ruleValue || null)};
                        }
                    });
                }
            });
        }

        //缓存讲师
        if(commonObj.teacherList.length < 1){
            commonObj.teacherList = []; //重置
            //如果不是管理员，只查询自己
            if(commonObj.isAdmin){
                commonObj.requestData({existsFlag: true}, commonObj.teacherListUrl, "post", "json", false, function (data) {
                    if(data && data.length > 0){
                        $.each(data, function (i, trainTeacher) {
                            commonObj.teacherList.push(trainTeacher);
                        });
                    }
                });
            }else {
                commonObj.teacherList.push(user);
            }
        }

        //渲染下拉列表-擅长领域
        var goodAtFieldHtml = "<option value=\"\">请选择擅长领域</option>";
        if(commonObj.goodAtFieldList && commonObj.goodAtFieldList.length > 0){
            $.each(commonObj.goodAtFieldList, function (i, trainSetting) {
                goodAtFieldHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
            });
        }
        $("#trainTeacherManage").find("select[name='goodAtFieldId']").html(goodAtFieldHtml);

        //渲染下拉列表-等级
        var teacherLevelHtml = "<option value=\"\">请选择讲师等级</option>";
        if(commonObj.teacherLevelList && commonObj.teacherLevelList.length > 0){
            $.each(commonObj.teacherLevelList, function (i, trainSetting) {
                teacherLevelHtml += "<option value=\""+i+"\">"+trainSetting.settingValue+"</option>";
            });
        }
        $("#trainTeacherManage").find("select[name='teacherLevel']").html(teacherLevelHtml);


        //渲染下拉列表-讲师，部门
        var teacherHtml = "<option value=\"\">请选择讲师</option>";
        var deptHtml = "<option value=\"\">请选择部门</option>";
        if(commonObj.teacherList && commonObj.teacherList.length > 0){
            $.each(commonObj.teacherList, function (i, trainTeacher) {
                teacherHtml += "<option value=\""+trainTeacher.sysUserId+"\">"+trainTeacher.name+"</option>";
                deptHtml += "<option value=\""+trainTeacher.deptId+"\">"+trainTeacher.deptName+"</option>";
            });
        }
        $("#trainTeacherManage").find("select[name='userId']").html(teacherHtml);
        $("#trainTeacherManage").find("select[name='deptId']").html(deptHtml);
        $("#trainTeacherStatistics").find("select[name='userId']").html(teacherHtml);
        $("#trainTeacherStatistics").find("select[name='deptId']").html(deptHtml);
        
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
