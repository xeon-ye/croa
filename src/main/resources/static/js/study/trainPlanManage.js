//页面初始化函数
$(function () {
    //获取权限
    commonObj.getTrainPermission();

    //控制栏目
    commonObj.controlTab();

    //下拉列表渲染
    commonObj.initBefore();

    //根据权限决定第一个Tab
    if(commonObj.isAdmin){
        commonObj.tabChange(0);
    }else {
        commonObj.tabChange(1);
    }
});

//培训计划管理
var trainPlanManageObj = {
    getTotalUrl:"/trainPlan/getTrainPlanTotal",
    trainPlanManageListUrl:"/trainPlan/listTrainPlan",
    planModalIndex: 0, //培训计划模态框
    trainPlanSubmitFlag: true, //提交按钮锁定标识
    coursewareKeepMap:{0:"不存档", 1:"存档"},
    trainStateMap:{0:"启用", 1:"禁用"},
    trainPlanManageListCallback: function (data, target, seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                var btnHtml = "";
                if(record.state == 0){
                    btnHtml += " <button data-state='"+record.state+"' class=\"tableButton orangeBtn\" type=\"button\" onclick='trainPlanManageObj.stateBtnClick(this, "+record.id+");'>\n" +
                        "            禁用\n" +
                        "        </button>";
                }else {
                    btnHtml += " <button data-state='"+record.state+"' class=\"tableButton blueBtn\" type=\"button\" onclick='trainPlanManageObj.stateBtnClick(this, "+record.id+");'>\n" +
                        "            启用\n" +
                        "        </button>";
                }
                seq += 1;
                html += " <tr>\n" +
                    "         <td title=\""+seq+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                "+seq+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.title+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.title+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.trainTimeDesc+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.trainTimeDesc+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(commonObj.coursePlateMap[record.coursePlate] || "无")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(commonObj.coursePlateMap[record.coursePlate] || "无")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(commonObj.trainWayMap[record.trainWay] || "无")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(commonObj.trainWayMap[record.trainWay] || "无")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(trainPlanManageObj.coursewareKeepMap[record.coursewareKeep] || "无")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(trainPlanManageObj.coursewareKeepMap[record.coursewareKeep] || "无")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.remake+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                "+record.remake+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+trainPlanManageObj.trainStateMap[record.state]+"\">\n" +
                    "             <div class=\"ellipsisContent state\">\n" +
                    "                 "+trainPlanManageObj.trainStateMap[record.state]+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td>\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 <button class=\"tableButton blueBtn\" type=\"button\" onclick='trainPlanManageObj.trainPlanModalShow("+JSON.stringify(record)+");'>\n" +
                    "                     编辑\n" +
                    "                 </button>\n" +
                    "                 "+btnHtml+" \n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "     </tr>";
            });
        }
        var $parentList = target ? $(target) : $(".trainPlanManageList");
        $parentList.html(html);
    },
    init: function () {
        //初始化分页组件
        commonObj.requestData($("#trainPlanManageForm").serializeJson(),trainPlanManageObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".trainPlanManageListPager"),
                    count: data.data.total,
                    url: trainPlanManageObj.trainPlanManageListUrl,
                    target: $(".trainPlanManageList"),
                    param: $("#trainPlanManageForm").serializeJson(),
                },trainPlanManageObj.trainPlanManageListCallback);
            }
        });
    },
    trainPlanModalShow: function (trainPlan) {
        trainPlanManageObj.planModalIndex  = layer.open({
            type: 1,
            title: false,
            zIndex: -99000,
            content: $("#trainPlanModal").html(),
            btn: [],
            area: ['65%', '45%'],
            closeBtn: 0,
            resize: false,
            move: '.layui-layer-btn',
            moveOut: true,
            success: function(layero, index){
                trainPlanManageObj.trainPlanSubmitFlag = true;//启用按钮

                //渲染下拉列表-培训方式
                var trainWayHtml = "<option value=\"\">请选择培训方式</option>";
                if(commonObj.trainWayList && commonObj.trainWayList.length > 0){
                    $.each(commonObj.trainWayList, function (i, trainSetting) {
                        trainWayHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                    });
                }
                $(layero[0]).find("select[name='trainWay']").html(trainWayHtml);

                //渲染下拉列表-培训板块
                var trainWayHtml = "<option value=\"\">请选择培训板块</option>";
                if(commonObj.coursePlateList && commonObj.coursePlateList.length > 0){
                    $.each(commonObj.coursePlateList, function (i, trainSetting) {
                        trainWayHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                    });
                }
                $(layero[0]).find("select[name='coursePlate']").html(trainWayHtml);

                //如果trainPlan有值，说明是编辑
                if(trainPlan){
                    $(layero[0]).find(".trainModalTitle").text("编辑培训计划");
                    for(var key in trainPlan){
                        $(layero[0]).find("input[name='"+key+"']").val(trainPlan[key] || "");
                        $(layero[0]).find("select[name='"+key+"']").val(trainPlan[key]);
                        $(layero[0]).find("textarea[name='"+key+"']").val(trainPlan[key] || "");
                    }
                }else {
                    $(layero[0]).find(".trainModalTitle").text("添加培训计划");
                }
            }
        });
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    trainPlanModalClose: function () {
        if(trainPlanManageObj.planModalIndex){
            layer.close(trainPlanManageObj.planModalIndex);
        }else {
            layer.closeAll();
        }
    },
    trainPlanSubmit: function (t) {
        var $form = $(t).closest(".trainModalCommon").find("form");
        if(!$form.valid()){
            return;
        }
        var jsonData = $form.serializeForm();
        if(!jsonData["coursePlate"]){
            layer.msg("请选择培训板块！", {time: 2000, icon: 5});
            return;
        }
        if(!jsonData["trainWay"]){
            layer.msg("请选择培训方式！", {time: 2000, icon: 5});
            return;
        }
        var url = jsonData.id ? "/trainPlan/update" : "/trainPlan/save"; //如果有ID，则为编辑操作
        var msgTitle = jsonData.id ? "编辑培训计划成功！" : "添加培训计划成功！";
        //如果没有锁定按钮，则发送请求
        if(trainPlanManageObj.trainPlanSubmitFlag){
            trainPlanManageObj.trainPlanSubmitFlag = false;
            commonObj.requestData(JSON.stringify(jsonData), url, "post", "json", true, function (data) {
                if(data.code == 200){
                    trainPlanManageObj.trainPlanModalClose(); //关闭窗口
                    layer.msg(msgTitle, {time: 3000, icon: 6});
                    trainPlanManageObj.init();//刷新表格
                }else {
                    trainPlanManageObj.trainPlanSubmitFlag = true;
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
            },function () {
                trainPlanManageObj.trainPlanSubmitFlag = true;
            }, true);
        }
    },
    stateBtnClick:function (t, id) {
        var state = $(t).attr("data-state");
        //如果当前是启用状态，则设置为禁用
        state = state == 0 ? 1:0;
        commonObj.requestData({id:id, state:state}, "/trainPlan/updateState", "post", "json", true,function (data) {
            if(data.code == 200){
                if(state == 0){
                    $(t).attr("title","禁用");
                    $(t).text("禁用");
                    $(t).removeClass("blueBtn");
                    $(t).addClass("orangeBtn");
                    $(t).closest("tr").find(".state").text("启用");
                    $(t).closest("tr").find(".state").closest("td").attr("title","启用");
                    $(t).attr("data-state", state);//设置新值
                }else {
                    $(t).attr("title","启用");
                    $(t).text("启用");
                    $(t).removeClass("orangeBtn");
                    $(t).addClass("blueBtn");
                    $(t).closest("tr").find(".state").text("禁用");
                    $(t).closest("tr").find(".state").closest("td").attr("title","禁用");
                    $(t).attr("data-state", state);//设置新值
                }
                layer.msg("编辑状态成功！", {time: 2000, icon: 6});
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        });

    },
}

//培训计划报名
var trainPlanSignObj = {
    getTotalUrl:"/trainPlan/getTrainPlanTotal",
    trainPlanManageListUrl:"/trainPlan/listTrainPlan",
    signPlanSubmitFlag: true, //提交按钮锁定标识
    coursewareKeepMap:{0:"不存档", 1:"存档"},
    trainStateMap:{0:"启用", 1:"禁用"},
    courseEnrollFlagMap:{1:"dept", 2:"role", 3:"user"},
    courseRemake: null, //富文本编辑器对象
    roleTypeMap: {}, //角色类型缓存，课程范围弹窗使用
    courseSignRangeMap:{dept:[], role:[], user:[]}, //课程范围弹窗数据缓存，避免每次弹窗请求
    courseSignRangeChooseMap: {dept:[], role:[], user:[]}, //课程报名范围选择的值
    signModalIndex: 0, //培训计划模态框
    validDateResult: {flag: true, errorTipId:"", html:""}, //时间校验结果
    trainPlanSignListCallback: function (data, target, seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                seq += 1;
                html += " <tr>\n" +
                    "         <td title=\""+seq+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                "+seq+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.title+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.title+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.trainTimeDesc+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+record.trainTimeDesc+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(commonObj.coursePlateMap[record.coursePlate] || "无")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(commonObj.coursePlateMap[record.coursePlate] || "无")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(commonObj.trainWayMap[record.trainWay] || "无")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(commonObj.trainWayMap[record.trainWay] || "无")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+(trainPlanSignObj.coursewareKeepMap[record.coursewareKeep] || "无")+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 "+(trainPlanSignObj.coursewareKeepMap[record.coursewareKeep] || "无")+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+record.remake+"\">\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                "+record.remake+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td title=\""+trainPlanSignObj.trainStateMap[record.state]+"\">\n" +
                    "             <div class=\"ellipsisContent state\">\n" +
                    "                 "+trainPlanSignObj.trainStateMap[record.state]+"\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "         <td>\n" +
                    "             <div class=\"ellipsisContent\">\n" +
                    "                 <button class=\"tableButton blueBtn\" type=\"button\" onclick='trainPlanSignObj.trainSignModalShow("+JSON.stringify(record)+");'>\n" +
                    "                     报名\n" +
                    "                 </button>\n" +
                    "             </div>\n" +
                    "         </td>\n" +
                    "     </tr>";
            });
        }
        var $parentList = target ? $(target) : $(".trainPlanSignList");
        $parentList.html(html);
    },
    init: function () {
        var param = $("#trainPlanSignForm").serializeJson();
        param.state = 0;//仅查看启用的
        //初始化分页组件
        commonObj.requestData(param,trainPlanSignObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $(".trainPlanSignListPager"),
                    count: data.data.total,
                    url: trainPlanSignObj.trainPlanManageListUrl,
                    target: $(".trainPlanSignList"),
                    param: param,
                },trainPlanSignObj.trainPlanSignListCallback);
            }
        });
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
        trainPlanSignObj.validDateResult = {flag:result, errorTipId:errorTipId, html: html};
        return result;
    },
    validSubmitDate:function (t, jsonData) {
        var signStartTime = jsonData["signStartTime"]; //报名开始时间
        var signEndTime = jsonData["signEndTime"]; //报名截止时间
        var trainStartTime = jsonData["trainStartTime"]; //培训开始时间
        var trainEndTime = jsonData["trainEndTime"]; //培训截止时间
        var courseEndTime = jsonData["courseEndTime"]; //课程反馈截止时间
        var flag = true; //校验结果
        $(t).closest(".trainModalCommon").find("#signStartTime-error").css("display", "none");
        $(t).closest(".trainModalCommon").find("#signEndTime-error").css("display", "none");
        $(t).closest(".trainModalCommon").find("#trainStartTime-error").css("display", "none");
        $(t).closest(".trainModalCommon").find("#trainEndTime-error").css("display", "none");
        $(t).closest(".trainModalCommon").find("#courseEndTime-error").css("display", "none");
        if(signStartTime >= signEndTime){
            flag = false;
            $(t).closest(".trainModalCommon").find("#signStartTime-error").css("display", "inline-block");
            $(t).closest(".trainModalCommon").find("#signStartTime-error").html("<i class=\"fa fa-times-circle\"></i>报名开始时间大于报名截止时间");
        }else {
            $(t).closest(".trainModalCommon").find("#signStartTime-error").css("display", "none");
        }
        if(signEndTime >= trainStartTime){
            flag = false;
            $(t).closest(".trainModalCommon").find("#trainStartTime-error").css("display", "inline-block");
            $(t).closest(".trainModalCommon").find("#trainStartTime-error").html("<i class=\"fa fa-times-circle\"></i>培训开始时间小于报名截止时间");
        }else {
            $(t).closest(".trainModalCommon").find("#trainStartTime-error").css("display", "none");
        }
        if(trainStartTime >= trainEndTime){
            flag = false;
            $(t).closest(".trainModalCommon").find("#trainEndTime-error").css("display", "inline-block");
            $(t).closest(".trainModalCommon").find("#trainEndTime-error").html("<i class=\"fa fa-times-circle\"></i>培训截止时间小于培训开始时间");
        }else {
            $(t).closest(".trainModalCommon").find("#trainEndTime-error").css("display", "none");
        }
        if(trainEndTime >= courseEndTime){
            flag = false;
            $(t).closest(".trainModalCommon").find("#courseEndTime-error").css("display", "inline-block");
            $(t).closest(".trainModalCommon").find("#courseEndTime-error").html("<i class=\"fa fa-times-circle\"></i>课程反馈截止时间小于培训截止时间");
        }else {
            $(t).closest(".trainModalCommon").find("#courseEndTime-error").css("display", "none");
        }
        return flag;
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
                trainPlanSignObj.validDate(value, $(layero[0]).find("input[name='signEndTime']").val(),"signStartTime-error", "signEndTime-error",layero, "报名开始时间不能大于截止时间！");
                //报名截止时间 与 培训开始时间
                trainPlanSignObj.validDate($(layero[0]).find("input[name='signEndTime']").val(), $(layero[0]).find("input[name='trainStartTime']").val(),"signEndTime-error", "",layero, "报名截止时间不能大于培训开始时间！");
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
                trainPlanSignObj.validDate($(layero[0]).find("input[name='signStartTime']").val(), value,"signStartTime-error", "",layero, "报名截止时间不能小于开始时间！");
                //报名截止时间 与 培训开始时间 - 由于这里影响培训开始时间提示信息，所以需要判断培训开始时间 与 截止时间
                trainPlanSignObj.validDate(value, $(layero[0]).find("input[name='trainStartTime']").val(),"signEndTime-error", "",layero, "报名截止时间不能大于培训开始时间！");
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
                trainPlanSignObj.validDate($(layero[0]).find("input[name='signEndTime']").val(), value,"signEndTime-error", "",layero, "报名截止时间不能大于培训开始时间！");
                //培训开始时间 与 截止时间 - 由于这里影响培训截止时间提示信息，所以需要判断培训截止时间 与 课程反馈时间
                trainPlanSignObj.validDate(value, $(layero[0]).find("input[name='trainEndTime']").val(),"trainStartTime-error", "trainEndTime-error",layero, "培训开始时间不能大于截止时间！");
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
                trainPlanSignObj.validDate($(layero[0]).find("input[name='trainStartTime']").val(), value,"trainEndTime-error", "trainStartTime-error",layero, "培训截止时间不能小于开始时间！")
                //培训截止时间 与 课程反馈时间
                trainPlanSignObj.validDate(value, $(layero[0]).find("input[name='courseEndTime']").val(),"courseEndTime-error", "",layero, "课程反馈截止时间不能小于培训开始时间！");
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
                trainPlanSignObj.validDate($(layero[0]).find("input[name='trainEndTime']").val(), value,"courseEndTime-error", "",layero, "课程反馈截止时间不能小于培训截止时间！");
            }
        };
        laydate.render(courseEndTime);
    },
    trainSignModalShow: function (trainPlan) {
        trainPlanSignObj.signModalIndex  = layer.open({
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
                trainPlanSignObj.signPlanSubmitFlag = true; //提交按钮启用

                //默认图片
                $(layero[0]).find("input[name='coursePic']").val("/img/train/course_pic_default.png");
                $(layero[0]).find(".coursePic").css("background-image", 'url("/img/train/course_pic_default.png")');

                //渲染下拉列表-培训方式
                var trainWayHtml = "<option value=\"\">请选择培训方式</option>";
                if(commonObj.trainWayList && commonObj.trainWayList.length > 0){
                    $.each(commonObj.trainWayList, function (i, trainSetting) {
                        trainWayHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                    });
                }
                $(layero[0]).find("select[name='trainWay']").html(trainWayHtml);

                //渲染下拉列表-培训板块
                var trainWayHtml = "<option value=\"\">请选择培训板块</option>";
                if(commonObj.coursePlateList && commonObj.coursePlateList.length > 0){
                    $.each(commonObj.coursePlateList, function (i, trainSetting) {
                        trainWayHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
                    });
                }
                $(layero[0]).find("select[name='coursePlate']").html(trainWayHtml);

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
                trainPlanSignObj.renderDate(layero);

                //富文本编辑
                trainPlanSignObj.courseRemake = KindEditor.create($(layero[0]).find("textarea[name='courseRemake']"), {
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

                if(trainPlan){
                    for(var key in trainPlan){
                        if("coursePlate" == key || "trainWay" == key){
                            $(layero[0]).find("input[name='"+key+"']").val(trainPlan[key] || "");
                            $(layero[0]).find("select[name='"+key+"']").val(trainPlan[key] || "");
                        }
                    }
                }else {
                    layer.msg("培训计划不存在！", {time: 2000, icon: 5});
                    trainPlanSignObj.signModalIndex = 0;
                    layer.close(index);
                }
            }
        });
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    trainSignModalClose: function () {
        if(trainPlanSignObj.signModalIndex){
            layer.close(trainPlanSignObj.signModalIndex);
        }else {
            layer.closeAll();
        }
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
            if(trainPlanSignObj.roleTypeMap && Object.getOwnPropertyNames(trainPlanSignObj.roleTypeMap).length < 1){
                commonObj.requestData({typeCode:"ROLE_TYPE"}, "/dict/listByTypeCode2", "get", "json", false, function (data) {
                    if(data && data.length > 0){
                        $.each(data, function (x, roleType) {
                            trainPlanSignObj.roleTypeMap[roleType.code] = roleType.name;
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
            if(trainPlanSignObj.courseSignRangeMap[signStr] && trainPlanSignObj.courseSignRangeMap[signStr].length < 1){
                commonObj.requestData(param, "/trainCourse/listCourseRange", "post", "json", false, function (data) {
                    trainPlanSignObj.courseSignRangeMap[signStr] = data;
                });
            }

            new SysUserCompont({
                title:title,
                url:"/trainCourse/listCourseRange",
                param:param,
                defaultGroupName: defaultGroupName,
                dataList: trainPlanSignObj.courseSignRangeMap[signStr],
                roleTypeMap: trainPlanSignObj.roleTypeMap,
                chooseDataList:trainPlanSignObj.courseSignRangeChooseMap[signStr],
                zIndex: -98000,
                resultCallBack:function (dataList) {
                    trainPlanSignObj.courseSignRangeChooseMap[signStr] = [];
                    $.each(dataList, function (zj, item) {
                        trainPlanSignObj.courseSignRangeChooseMap[signStr].push(item);
                    });
                },
                endCallBack:function () {
                    trainPlanSignObj.courseSignRangeClear(t, signStr);
                }
            });
        }
    },
    courseSignRangeClear: function (t, signStr) {
        trainPlanSignObj.courseSignRangeChooseMap[signStr] = [];//清空
        $(t).closest("form").find("select[name='courseEnrollFlag']").val(0);//重置
        $(t).closest("form").find(".courseEnrollBtn").css("display", "none");//重置
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    trainSignSubmit: function (t) {
        var $form = $(t).closest(".trainModalCommon").find("form");
        if(!$form.valid()){
            return;
        }
        var jsonData = $form.serializeForm();
        //校验时间
        if(!trainPlanSignObj.validSubmitDate(t, jsonData)){
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
        if(jsonData["courseEnrollFlag"] != 0 && trainPlanSignObj.courseSignRangeChooseMap[trainPlanSignObj.courseEnrollFlagMap[jsonData["courseEnrollFlag"]]].length < 1){
            jsonData["courseEnrollFlag"] = 0; //如果课程范围没有选中值，则默认
        }else {
            jsonData["courseSignRangeList"] = trainPlanSignObj.courseSignRangeChooseMap[trainPlanSignObj.courseEnrollFlagMap[jsonData["courseEnrollFlag"]]];
        }
        //富文本编辑器
        if(trainPlanSignObj.courseRemake){
            trainPlanSignObj.courseRemake.sync();//同步数据后可以直接取得textarea的value
            jsonData["courseRemake"] = $form.find("textarea[name='courseRemake']").val() || "";
        }

        //如果没有锁定按钮，则发送请求
        if(trainPlanSignObj.signPlanSubmitFlag){
            trainPlanSignObj.signPlanSubmitFlag = false;
            commonObj.requestData(JSON.stringify(jsonData), '/trainCourse/signUp', "post", "json", true, function (data) {
                if(data.code == 200){
                    trainPlanSignObj.trainSignModalClose(); //关闭窗口
                    layer.msg("报名成功！", {time: 3000, icon: 6});
                }else {
                    trainPlanSignObj.signPlanSubmitFlag = true;
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
            },function () {
                trainPlanSignObj.signPlanSubmitFlag = true;
            }, true);
        }
    },
}

//页面公共处理对象
var commonObj = {
    isAdmin: false, //是否管理员，管理员可查看全部讲师，讲师只查看自己，非讲师将无数据显示
    isTeacher:false, //是否讲师, 讲师只查看自己，非讲师将无数据显示
    listTrainSettingUrl:"/trainSetting/listTrainSetting", //培训设置列表url
    coursePlateList: [], //培训板块缓存
    coursePlateMap: {}, //培训板块缓存，数据格式：{id：name}
    trainWayMap: {}, //培训方式缓存，数据格式：{id：name}
    trainWayList: [], //培训方式缓存
    tabs:{
        0:{id:"trainPlanManage", tabId:"trainPlanManageTab", obj:trainPlanManageObj},
        1:{id:"trainPlanSign", tabId:"trainPlanSignTab", obj:trainPlanSignObj},
    },
    imageUpload:null,
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
        //如果有管理员，则展示培训计划管理
        if(commonObj.isAdmin){
            $("#trainPlanManageTab").css("display", "inline-block");
        }else {
            $("#trainPlanManageTab").css("display", "none");
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

        //渲染下拉列表-培训方式
        var trainWayHtml = "<option value=\"\">请选择培训方式</option>";
        if(commonObj.trainWayList && commonObj.trainWayList.length > 0){
            $.each(commonObj.trainWayList, function (i, trainSetting) {
                trainWayHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
            });
        }
        $("#trainPlanManage").find("select[name='trainWay']").html(trainWayHtml);
        $("#trainPlanSign").find("select[name='trainWay']").html(trainWayHtml);

        //渲染下拉列表-培训板块
        var trainWayHtml = "<option value=\"\">请选择培训板块</option>";
        if(commonObj.coursePlateList && commonObj.coursePlateList.length > 0){
            $.each(commonObj.coursePlateList, function (i, trainSetting) {
                trainWayHtml += "<option value=\""+trainSetting.id+"\">"+trainSetting.settingValue+"</option>";
            });
        }
        $("#trainPlanManage").find("select[name='coursePlate']").html(trainWayHtml);
        $("#trainPlanSign").find("select[name='coursePlate']").html(trainWayHtml);

        //课程图片上传
        commonObj.imageUpload = new FileUpload({
            targetEl: '#imageUploadForm',
            multi: false,
            filePart: "trainPlan",
            completeCallback: function (data) {
                if (data.length > 0) {
                    var filePath = data[0].file;
                    $(".coursePic").css("background-image", "url(\""+filePath+"\")");
                    $("input[name='coursePic']").val(filePath);
                }
            },
            acceptSuffix: ['jpg', 'png']
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
