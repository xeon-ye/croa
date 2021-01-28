//页面初始化函数
$(function () {
    trainSettingObj.init();
});

//培训设置
var trainSettingObj = {
    trainSettingListUrl:"/trainSetting/listTrainSetting",
    trainSettingList:[], //列表最后的顺序
    addtrainSettingList:[],//新增的设置列表
    stateMap:{0:"启用", 1:"禁用"},
    submitLock:true, //提交按钮控制
    oldOptionName: "", //编辑之前的数据
    renderTbody: function () {
        //判断是否有新增记录，有的话显示提交按钮
        if(trainSettingObj.addtrainSettingList && trainSettingObj.addtrainSettingList.length > 0){
            $("#submitWrap").css("display", "block");
        }else {
            $("#submitWrap").css("display", "none");
        }

        var html = "";
        if(trainSettingObj.trainSettingList && trainSettingObj.trainSettingList.length > 0){
            $.each(trainSettingObj.trainSettingList, function (i, trainSetting) {
                var btnHtml = "";
                if(trainSetting.state == 0){
                    btnHtml += "<button data-state='"+trainSetting.state+"' class=\"tableButton orangeBtn\" type=\"button\" title=\"禁用\" onclick='trainSettingObj.stateBtnClick(this,"+trainSetting.id+");'>\n" +
                        "           禁用\n" +
                        "       </button>";
                }else {
                    btnHtml += "<button data-state='"+trainSetting.state+"' class=\"tableButton blueBtn\" type=\"button\" title=\"启用\" onclick='trainSettingObj.stateBtnClick(this,"+trainSetting.id+");'>\n" +
                        "           启用\n" +
                        "       </button>";
                }
                html += "<tr onmouseover=\"commonObj.mouseOver(this);\" onmouseout=\"commonObj.mouseOut(this);\">\n" +
                    "        <td title=\""+(i+1)+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(i+1)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+trainSetting.settingValue+"\">\n" +
                    "            <div class=\"ellipsisContent layui-form\">\n" +
                    "                <input data-id='"+trainSetting.id+"' type=\"text\" value=\""+trainSetting.settingValue+"\" class=\"form-control height18 readonlyInput\" readonly onkeydown='commonObj.enterEvent(event, this);'/>\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+trainSetting.settingLevel+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+trainSetting.settingLevel+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(trainSetting.ruleValue ? ("<=&nbsp;"+trainSetting.ruleValue) : "无")+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+(trainSetting.ruleValue ? ("<=&nbsp;"+trainSetting.ruleValue) : "无")+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+trainSetting.user.name+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+trainSetting.user.name+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+trainSetting.createDate+"\">\n" +
                    "            <div class=\"ellipsisContent\">\n" +
                    "                "+trainSetting.createDate+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+trainSettingObj.stateMap[trainSetting.state]+"\">\n" +
                    "            <div class=\"ellipsisContent state\">\n" +
                    "                "+trainSettingObj.stateMap[trainSetting.state]+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <button class=\"tableButton blueBtn editBtn\" type=\"button\" title=\"编辑\" onclick='trainSettingObj.editBtnClick(this);'>\n" +
                    "                编辑\n" +
                    "            </button>\n" +
                    "            "+btnHtml+"\n"+
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <button class=\"tableButton blueBtn\" type=\"button\" title=\"置顶\" onclick='trainSettingObj.move(\"TOP\","+JSON.stringify(trainSetting)+")'>\n" +
                    "                置顶\n" +
                    "            </button>\n" +
                    "            <button class=\"tableButton blueBtn\" type=\"button\" title=\"上移\" onclick='trainSettingObj.move(\"UP\","+JSON.stringify(trainSetting)+")'>\n" +
                    "                上移\n" +
                    "            </button>\n" +
                    "            <button class=\"tableButton blueBtn\" type=\"button\" title=\"下移\" onclick='trainSettingObj.move(\"DOWN\","+JSON.stringify(trainSetting)+")'>\n" +
                    "                下移\n" +
                    "            </button>\n" +
                    "            <button class=\"tableButton blueBtn\" type=\"button\" title=\"置底\" onclick='trainSettingObj.move(\"BOTTOM\","+JSON.stringify(trainSetting)+")'>\n" +
                    "                置底\n" +
                    "        </td>\n" +
                    "        <td>\n" +
                    "            <button class=\"tableButton orangeBtn\" type=\"button\" title=\"移除\" onclick='trainSettingObj.del("+trainSetting.id+");'>\n" +
                    "                移除\n" +
                    "            </button>\n" +
                    "        </td>\n" +
                    "    </tr>";
            });
        }
        $("#trainSettingList").html(html);
        $("#trainSettingList").find("input").css("background-color", "white");
    },
    init: function () {
        commonObj.requestData({settingModuleList:["TRAIN_WAY","TEACHER_INTEGRAL_RULE","STUDENT_INTEGRAL_RULE","UP_RULE","PAPER_GRADE"].join(","),orderFlag:true}, trainSettingObj.trainSettingListUrl, "post", "json",true, function (data) {
            if(data && data.length > 0){
                //对于公式除数有parentId
                $.each(data, function (i, trainSetting) {
                    if(!trainSetting.parentId){
                        if(trainSetting.settingModule == "UP_RULE"){
                                if(trainSetting.settingValue){
                                    $("#upRuleBtn").text("编辑讲师评级规则");
                                    $("#upRuleValue").val(trainSetting.settingValue);
                                }else {
                                    $("#upRuleBtn").text("设置讲师评级规则");
                                    $("#upRuleValue").val("");
                                }
                        }else if (trainSetting.settingModule == "TEACHER_INTEGRAL_RULE"){
                            if(trainSetting.settingValue){
                                $("#courseRuleBtn").text("编辑讲师单课程积分规则");
                                $("#courseRuleValue").val(trainSetting.settingValue);
                            }else {
                                $("#courseRuleBtn").text("设置讲师单课程积分规则");
                                $("#courseRuleValue").val("");
                            }
                        }else if (trainSetting.settingModule == "STUDENT_INTEGRAL_RULE"){
                            if(trainSetting.settingValue){
                                $("#studentRuleBtn").text("编辑学员积分规则");
                                $("#studentRuleValue").val(trainSetting.settingValue);
                            }else {
                                $("#studentRuleBtn").text("设置学员积分规则");
                                $("#studentRuleValue").val("");
                            }
                        }else if (trainSetting.settingModule == "PAPER_GRADE"){
                            if(trainSetting.settingValue){
                                $("#paperGradeBtn").text("编辑试卷总分数");
                                $("#paperGradeValue").val(trainSetting.settingValue);
                            }else {
                                $("#paperGradeBtn").text("设置试卷总分数");
                                $("#paperGradeValue").val("");
                            }
                        }else {
                            trainSettingObj.trainSettingList.push(trainSetting);
                        }
                    }
                });
                trainSettingObj.renderTbody();
            }
        });
    },
    selChangeEvent: function (val) {
        trainSettingObj.addtrainSettingList = [];
        if(val){
            if(val == 'TEACHER_LEVEL'){
                $("#level").attr("title", "讲师等级");
                $("#level").find(".ellipsisContent").text("讲师等级");
            }else {
                $("#level").attr("title", "展现顺序");
                $("#level").find(".ellipsisContent").text("展现顺序");
            }
            commonObj.requestData({settingModule: val,orderFlag:true}, trainSettingObj.trainSettingListUrl, "post", "json",true, function (data) {
                trainSettingObj.trainSettingList = data;
                trainSettingObj.renderTbody();
            });
        }
    },
    calIntegral: function (seq) {
        var integral = "无";
        if("TEACHER_LEVEL" == $("select[name='settingModule']").val()){
            //根据上面升级公式计算积分
            if($("#upRuleValue").val()){
                var gs = $("#upRuleValue").val().replace(/x/g, seq);
                integral = "<=&nbsp;" + eval(gs);
            }
        }
        return integral;
    },
    addBtnClick: function () {
        //如果添加的是讲师等级，需要判断是否有设置评级规则，否则不给添加
        if("TEACHER_LEVEL" == $("select[name='settingModule']").val() && !$("#upRuleValue").val()){
            layer.msg("请先设置评级规则！", {time: 2000, icon: 5});
            return;
        }

        var seq = 0;
        //如果添加新的，则新添加的顺序为原来顺序+1，否则新添加的顺序+1
        if(trainSettingObj.addtrainSettingList && trainSettingObj.addtrainSettingList.length > 0){
            seq = trainSettingObj.addtrainSettingList[trainSettingObj.addtrainSettingList.length-1] + 1;
        }else{
            var leg = trainSettingObj.trainSettingList.length;
            seq = leg > 0 ? trainSettingObj.trainSettingList[leg-1].settingLevel+1 : 0;
        }
        trainSettingObj.addtrainSettingList.push(seq);//保存起来
        var html = "<tr onmouseover=\"commonObj.mouseOver(this);\" onmouseout=\"commonObj.mouseOut(this);\">\n" +
            "           <td title=\""+($("#trainSettingList").find("tr").length + 1)+"\">\n" +
            "               <div class=\"ellipsisContent\">\n" +
            "                   "+($("#trainSettingList").find("tr").length + 1)+"\n" +
            "               </div>\n" +
            "           </td>\n" +
            "           <td title=\"请输入选项名称\">\n" +
            "               <div class=\"ellipsisContent layui-form\">\n" +
            "                   <input type=\"text\" placeholder='请输入选项名称' class=\"form-control height18 editInput\"/>\n" +
            "               </div>\n" +
            "           </td>\n" +
            "           <td title=\""+seq+"\">\n" +
            "               <div id='seq"+seq+"' class=\"ellipsisContent seq\">\n" +
            "                   "+seq+"\n" +
            "               </div>\n" +
            "           </td>\n" +
            "           <td id='integral"+seq+"' title=\""+trainSettingObj.calIntegral(seq)+"\">\n" +
            "               <div class=\"ellipsisContent\">\n" +
            "                   "+trainSettingObj.calIntegral(seq)+"\n" +
            "               </div>\n" +
            "           </td>\n"+
            "           <td title=\""+user.name+"\">\n" +
            "               <div class=\"ellipsisContent\">\n" +
            "                   "+user.name+"\n" +
            "               </div>\n" +
            "           </td>\n" +
            "           <td title=\""+new Date().format("yyyy-MM-dd hh:mm:ss")+"\">\n" +
            "               <div class=\"ellipsisContent\">\n" +
            "                   "+new Date().format("yyyy-MM-dd hh:mm:ss")+"\n" +
            "               </div>\n" +
            "           </td>\n" +
            "           <td title=\"启用\">\n" +
            "               <div class=\"ellipsisContent\">\n" +
            "                   启用\n" +
            "               </div>\n" +
            "           </td>\n" +
            "           <td>\n" +
            "           </td>\n" +
            "           <td>\n" +
            "           </td>\n" +
            "           <td>\n" +
            "               <button class=\"tableButton orangeBtn\" type=\"button\" title=\"移除\" onclick='trainSettingObj.moveBtnClick(this);'>\n" +
            "                   移除\n" +
            "               </button>\n" +
            "           </td>\n" +
            "       </tr>";
        $("#trainSettingList").append(html);
        $("#trainSettingList").find("tr:last-child").find("input").focus();
        //新增记录时，显示提交按钮
        $("#submitWrap").css("display", "block");
        trainSettingObj.submitLock = true; //启用提交
    },
    moveBtnClick:function (t, id) {
        //如果有ID则更新数据，否则为新增的还没有入库
        if(id){

        }else {
            var currentSeq = parseInt($(t).closest("tr").find(".seq").text().trim());
            $(t).closest("tr").remove();
            //重新排序新增顺序
            trainSettingObj.addtrainSettingList.remove(currentSeq);
            var tempList = [];
            $.each(trainSettingObj.addtrainSettingList, function (a, trainSeq) {
                if(trainSeq > currentSeq){
                    $("#seq"+trainSeq).text(trainSeq - 1);
                    $("#seq"+trainSeq).attr("title", trainSeq - 1);
                    //计算积分
                    var integralTmp = trainSettingObj.calIntegral(trainSeq - 1);
                    $("#integral"+trainSeq).text(integralTmp);
                    $("#integral"+trainSeq).attr("title", integralTmp);
                    $("#seq"+trainSeq).attr("id", "seq"+(trainSeq - 1));//ID需要进行修改，不然后面遍历列表会出现ID不一致情况
                    trainSeq = trainSeq - 1;
                }
                tempList.push(trainSeq);
            });
            trainSettingObj.addtrainSettingList = tempList;
        }

        //判断是否有新增记录，有的话显示提交按钮
        if(trainSettingObj.addtrainSettingList && trainSettingObj.addtrainSettingList.length > 0){
            $("#submitWrap").css("display", "block");
        }else {
            $("#submitWrap").css("display", "none");
        }

        //重新排序表格序号
        $("#trainSettingList").find("tr").each(function (i, tr) {
            $(tr).find("td:first-child").find("div").text(i+1);
        });
    },
    submitBtn:function () {
        var jsonData = {};
        jsonData.settingModule = $("select[name='settingModule']").val();//配置模块
        jsonData.settingValueList = [];//选项值
        if(trainSettingObj.addtrainSettingList && trainSettingObj.addtrainSettingList.length > 0){
            for(var k = 0; k < trainSettingObj.addtrainSettingList.length; k++){
                var val = $("#seq"+trainSettingObj.addtrainSettingList[k]).closest("tr").find("input").val();
                if(!val){
                    layer.msg("存在选项名称没有输入！", {time: 2000, icon: 5});
                    return;
                }
                jsonData.settingValueList.push(val);
            }
        }else {
            layer.msg("空数据不能进行提交！", {time: 2000, icon: 5});
            return;
        }
        if(trainSettingObj.submitLock){
            trainSettingObj.submitLock = false; //禁用提交
            commonObj.requestData(JSON.stringify(jsonData), "/trainSetting/save", "post", "json", true, function (data) {
                if(data.code == 200){
                    trainSettingObj.addtrainSettingList = [];//清空
                    trainSettingObj.selChangeEvent($("select[name='settingModule']").val());//更新表格数据
                }else {
                    trainSettingObj.submitLock = true; //启用提交
                }
            }, null, true);
        }
    },
    editBtnClick:function (t) {
        var $input = $(t).closest("tr").find("input");
        //如果文本框是编辑状态，则点击该按钮可提交
        if(!$input.attr("readonly")){
            trainSettingObj.editValue(t);
        }else {
            $input.removeAttr("readonly");
            $input.removeClass("readonlyInput");
            $input.addClass("editInput");
            $(t).closest("tr").find(".editBtn").attr("title","确定");
            $(t).closest("tr").find(".editBtn").text("确定");
            trainSettingObj.oldOptionName = $input.val();
        }
    },
    editValue:function (t) {
        var $input = $(t).closest("tr").find("input");
        //如果值没有发生改变，不用去发送请求
        if(trainSettingObj.oldOptionName == $input.val()){
            $input.attr("readonly", true);
            $input.removeClass("editInput");
            $input.addClass("readonlyInput");
            $(t).closest("tr").find(".editBtn").attr("title","编辑");
            $(t).closest("tr").find(".editBtn").text("编辑");
            trainSettingObj.oldOptionName = "";//重置
        }else{
            if(!$input.attr("readonly")){
                commonObj.requestData(JSON.stringify({id:$($input).attr("data-id"), settingValue:$($input).val()}), "/trainSetting/update", "post", "json", true,function (data) {
                    if(data.code == 200){
                        $input.attr("readonly", true);
                        $input.removeClass("editInput");
                        $input.addClass("readonlyInput");
                        $(t).closest("tr").find(".editBtn").attr("title","编辑");
                        $(t).closest("tr").find(".editBtn").text("编辑");
                        trainSettingObj.oldOptionName = "";//重置
                        layer.msg("编辑成功！", {time: 2000, icon: 6});
                    }else {
                        layer.msg(data.msg, {time: 2000, icon: 5});
                    }
                },null,true);
            }
        }
    },
    stateBtnClick:function (t, id) {
        var state = $(t).attr("data-state");
        //如果当前是启用状态，则设置为禁用
        state = state == 0 ? 1:0;
        commonObj.requestData({id:id, state:state}, "/trainSetting/updateState", "post", "json", true,function (data) {
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
    move:function (move, trainSetting) {
        commonObj.requestData({move:move, id:trainSetting.id, settingModule:trainSetting.settingModule, settingLevel:trainSetting.settingLevel}, "/trainSetting/move", "post", "json", true,function (data) {
            if(data.code == 200){
                layer.msg("顺序设置成功！", {time: 2000, icon: 6});
                trainSettingObj.selChangeEvent($("select[name='settingModule']").val());//更新表格数据
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        });
    },
    del:function (id) {
        commonObj.requestData({id:id, state:-9}, "/trainSetting/updateState", "post", "json", true,function (data) {
            if(data.code == 200){
                trainSettingObj.selChangeEvent($("select[name='settingModule']").val());//更新表格数据
                layer.msg("移除成功！", {time: 2000, icon: 6});
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        });
    },
    addUpRuleClick:function (t) {
        new FormulaConfigCompont({
            title: "讲师评级规则",
            variableDefine:{
                x:'讲师等级'
            },
            param:{settingModule:"UP_RULE",settingValue:$("#upRuleValue").val(),settingValueList:[]},
            url:"/trainSetting/save",
            successCallback:function (data) {
                $("#upRuleBtn").text("编辑讲师评级规则");
                $("#upRuleValue").val(data.settingValue);
                //如果当前列表是讲师等级，则同步刷新
                if("TEACHER_LEVEL" == $("select[name='settingModule']").val()){
                    trainSettingObj.selChangeEvent($("select[name='settingModule']").val());//更新表格数据
                }
            }
        }).render();
    },
    addCourseRuleClick:function (t) {
        new FormulaConfigCompont({
            title: "讲师单课程积分规则",
            variableDefine:{
                a:'课程总评分',
                b:'课程评分人数',
                c:'课程点赞数',
                d:'课程总完课人数',
                e:'课程吐槽人数'
            },
            param:{settingModule:"TEACHER_INTEGRAL_RULE",settingValue:$("#courseRuleValue").val(),settingValueList:[]},
            url:"/trainSetting/save",
            successCallback:function (data) {
                $("#courseRuleBtn").text("编辑讲师单课程积分规则");
                $("#courseRuleValue").val(data.settingValue);
            }
        }).render();
    },
    addStudentRuleClick:function (t) {
        new FormulaConfigCompont({
            title: "学员积分规则",
            variableDefine:{
                x:'课程学分',
                y:'学员完课状态(学员完课值为1，否则值为0)',
                z:'考试成绩',
            },
            param:{settingModule:"STUDENT_INTEGRAL_RULE",settingValue:$("#studentRuleValue").val(),settingValueList:[]},
            url:"/trainSetting/save",
            successCallback:function (data) {
                $("#studentRuleBtn").text("编辑学员积分规则");
                $("#studentRuleValue").val(data.settingValue);
            }
        }).render();
    },
    addPaperGradeClick:function (t) {
        new FormulaConfigCompont({
            title: "试卷总分数",
            variableDefine:{},
            param:{settingModule:"PAPER_GRADE",settingValue:$("#paperGradeValue").val(),settingValueList:[]},
            url:"/trainSetting/save",
            successCallback:function (data) {
                $("#paperGradeBtn").text("编辑试卷总分数");
                $("#paperGradeValue").val(data.settingValue);
            }
        }).render();
    }
}

//页面公共处理对象
var commonObj = {
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
    //得到查询参数
    getQueryString:function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]);
        return null;
    },
    //表格行鼠标移动事件
    mouseOver: function (t) {
        $(t).find("input").each(function (i, btn) {
            $(btn).css("background-color", "#F2F2F2");
        });
    },
    //表格行鼠标移出时间
    mouseOut: function (t) {
        $(t).find("input").each(function (i, btn) {
            $(btn).css("background-color", "white");
        });
    },
    //回车键事件
    enterEvent:function (event, t) {
        if((event.keyCode == '13' || event.keyCode == 13)){
            trainSettingObj.editValue(t);
        }
    }
}