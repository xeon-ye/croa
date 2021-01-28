//页面初始化函数
$(function () {
    studentExamObj.init();
});

//试卷详情
var studentExamObj = {
    teacherFlag: false, //是否讲师标识：true-讲师(阅卷)、false-学员(考试详情)
    paper:{}, //试卷信息
    answerData: {}, //答题卡信息
    answerQuestionMap:{},//答题卡列表
    paperQuestionList:[], //试卷题目集合，顺序按照后台请求集合，按照题型，试卷详情ID 正序
    seqMap:{1:"一", 2:"二", 3:"三", 4:"四", 5:"五"},
    questionTypeMap:{
        1:{name:"radio", seq:0, minTitle:"单选题", longTitle:"单项选择题", totalGrade:0, totalNum: 0, questionList:[]},
        2:{name:"check", seq:0, minTitle:"多选题", longTitle:"多项选择题", totalGrade:0, totalNum: 0, questionList:[]},
        3:{name:"judge", seq:0, minTitle:"判断题", longTitle:"判断选择题", totalGrade:0, totalNum: 0, questionList:[]},
        4:{name:"complete", seq:0, minTitle:"填空题", longTitle:"填空题", totalGrade:0, totalNum: 0, questionList:[]},
        5:{name:"essay", seq:0, minTitle:"问答题", longTitle:"问答题", totalGrade:0, totalNum: 0, questionList:[]}
    },
    currentQuestionIndex:0, //逐题展示，当前题目下标
    answerCard:{id:"", paperId:"",answerCardDetailList:[], createId:""}, //学员答题卡信息
    handlePaper:function (result) {
        studentExamObj.paper = result.paper;
        var seq = 0;
        for(var it = 1; it <= 5; it++){
            if(result[it] && result[it].length > 0){
                seq++;
                $.each(result[it], function (jj, question) {
                    studentExamObj.questionTypeMap[it].totalGrade += question.questionGrade; //题目类型分数统计
                    studentExamObj.questionTypeMap[it].totalNum += 1; //题目类型题数统计
                    studentExamObj.questionTypeMap[it].seq = seq; //题目类型的顺序
                    studentExamObj.questionTypeMap[it].questionList.push(question); //缓存题目
                    studentExamObj.paperQuestionList.push(question); //缓存题目
                });
                studentExamObj.questionTypeMap[it].longTille += "(共"+studentExamObj.questionTypeMap[it].totalNum+"题"+studentExamObj.questionTypeMap[it].totalGrade+"分)";
            }
        }
        studentExamObj.paper["questionNum"] = studentExamObj.paperQuestionList.length;
    },
    handleAnswerCard:function (answerData) {
        studentExamObj.answerData = answerData;
        if(answerData){
            if(answerData.answerCardDetailList && answerData.answerCardDetailList.length > 0){
                $.each(answerData.answerCardDetailList, function (a, answerCardDetail) {
                    studentExamObj.answerQuestionMap[answerCardDetail.questionId] = answerCardDetail;
                });
            }
        }
    },
    init: function () {
        studentExamObj.teacherFlag = commonObj.getQueryString("teacherFlag")  == 1 ? true :false;

        //讲师阅卷有提交按钮
        if(studentExamObj.teacherFlag){
            $(".submitPaperBtn").css("display", "block");
            $(".paperWayWrap2").css("margin-top", "65px");
        }else {
            $(".submitPaperBtn").css("display", "none");
            $(".paperWayWrap2").css("margin-top", "0px");
        }

        var paperId = commonObj.getQueryString("paperId");
        var studentId = commonObj.getQueryString("studentId");
        commonObj.requestData({id:paperId, orderFlag:true}, "/paper/getPaperDetailById", "post", "json", false, function (data) {
            if(data.code == 200){
                var result = data.data.result;
                //缓存试卷信息，并进行数据封装
                studentExamObj.handlePaper(result);
                //请求答题卡信息，判断是否存在答题卡
                commonObj.requestData({paperId:paperId, studentId:studentId}, "/answerCard/getAnswerCard", "post", "json", false, function (answerData) {
                    if(answerData.code == 200){
                        //缓存答题卡信息，并进行数据封装
                        studentExamObj.handleAnswerCard(answerData.data.result);
                    }
                });
                //渲染试卷基本信息
                studentExamObj.renderPaper(studentExamObj.paper);
                //渲染所有题目
                studentExamObj.loadAllQuestion();
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        });
    },
    markClick:function (t) {
        //如果没有批阅编辑框，则添加
        if($(t).closest(".questionItem").find("textarea[name='teacherRemark']").length < 1){
            var html = "<div class=\"questionAnswerDesc\">\n" +
                "           <textarea name=\"teacherRemark\" placeholder=\"请输入批阅内容\" class=\"layui-textarea questionInput\"></textarea>\n" +
                "       </div>";
            $(t).closest(".questionItem").append(html);
        }
    },
    renderPaper:function (paper) {
        $("input[name='paperId']").val(paper.id);
        $("input[name='autoMarkFlag']").val(paper.autoMarkFlag);
        if(studentExamObj.answerData){
            $("input[name='answerCardId']").val(studentExamObj.answerData.id);
        }
        $(".paperTitle").text(paper.paperTitle);
        $(".paperTitle").attr("title", paper.paperTitle); //试卷标题
        $(".paperGrade").text(paper.paperGrade + "分");//试卷总分
        $(".studnetGrade").text(studentExamObj.answerData.paperGrade + "分");//考试分数
        $(".studentTime").text(Math.ceil((paper.paperTime - studentExamObj.answerData.remainTime / 60)) + "分钟");//学员用时
        $(".paperTime").text(paper.paperTime + "分钟");//试卷总时长
    },
    renderQuestionTypeTitle: function (questionType) {
        var questionTypeItem = studentExamObj.questionTypeMap[questionType];
        var html = "<div class=\"questionTitleWrap\">\n" +
            "           "+studentExamObj.seqMap[questionTypeItem.seq]+"、"+questionTypeItem.longTitle+"(共"+questionTypeItem.totalNum+"题"+questionTypeItem.totalGrade+"分)\n" +
            "       </div>";
        return html;
    },
    renderQuestionTitle:function (question) {
        var questionTypeItem = studentExamObj.questionTypeMap[question.questionType];
        var seq = studentExamObj.currentQuestionIndex+1;
        if(question.questionType == 4){
            question.questionTitle = question.questionAnswerDesc.replace(new RegExp("<\\^_\\^>", "g"), "<span class=\"completeAnswer\"></span>");
        }
        var html = "<div class=\"questionTitleShow\">\n" +
            "           <span>"+seq+"、(<span class=\"quetionType\">&nbsp;"+questionTypeItem.minTitle+"&nbsp;</span>)</span>\n" +
            "           <span>"+question.questionTitle+"</span>\n" +
            "           <span>(<span class=\"questionGrade\">&nbsp;"+question.questionGrade+"分&nbsp;</span>)</span>\n";
        //如果是讲师角色，则可以阅卷
        if(studentExamObj.teacherFlag){
            html += "<span class=\"markBtn\" onclick='studentExamObj.markClick(this);'>\n" +
                "        <i class=\"fa fa-edit\"></i>&nbsp批阅\n" +
                "    </span>\n";
        }
        html += "   </div>"
        return html;
    },
    renderRadioQuestion:function (question) {
        var answerDescArr = (question.questionAnswerDesc || "").split("<^_^>");
        var studentAnswer = "";
        if(studentExamObj.answerQuestionMap && studentExamObj.answerQuestionMap[question.id]){
            studentAnswer = studentExamObj.answerQuestionMap[question.id].studentAnswer;
        }
        var html = "<div class=\"questionAnswerDesc\">\n" +
            "           <div class=\"col-sm-12\">\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"A\"  class=\"questionInput\" title=\"A、\"  "+(studentAnswer == 'A' ? 'checked' : '')+" disabled>\n" +
            "                   <span class=\"questionDesc questionInput\" title='"+(answerDescArr[0] || "")+"'>"+(answerDescArr[0] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"B\"  class=\"questionInput\" title=\"B、\" "+(studentAnswer == 'B' ? 'checked' : '')+" disabled>\n" +
            "                   <span class=\"questionDesc questionInput\" title='"+(answerDescArr[1] || "")+"'>"+(answerDescArr[1] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"C\"  class=\"questionInput\" title=\"C、\" "+(studentAnswer == 'C' ? 'checked' : '')+" disabled>\n" +
            "                   <span class=\"questionDesc questionInput\" title='"+(answerDescArr[1] || "")+"'>"+(answerDescArr[2] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"D\"  class=\"questionInput\" title=\"D、\" "+(studentAnswer == 'D' ? 'checked' : '')+" disabled>\n" +
            "                   <span class=\"questionDesc questionInput\" title='"+(answerDescArr[1] || "")+"'>"+(answerDescArr[3] || "")+"</span>\n" +
            "               </div>\n" +
            "           </div>\n" +
            "       </div>"
        return html;
    },
    renderCheckQuestion:function (question) {
        var answerDescArr = (question.questionAnswerDesc || "").split("<^_^>");
        var studentAnswerArr = [];
        if(studentExamObj.answerQuestionMap && studentExamObj.answerQuestionMap[question.id]){
            studentAnswerArr = studentExamObj.answerQuestionMap[question.id].studentAnswer.split("<^_^>");
        }
        var html = "<div class=\"questionAnswerDesc\">\n" +
            "           <div class=\"col-sm-12\">\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"checkbox\" value=\"A\" class=\"questionInput\" title=\"A、\" lay-skin=\"primary\" "+(studentAnswerArr.contains("A") ? "checked" : "")+" disabled>\n" +
            "                   <span class=\"questionDesc questionInput\" style=\"padding-top: 0px;\" title='"+(answerDescArr[0] || "")+"'>"+(answerDescArr[0] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"checkbox\" value=\"B\" class=\"questionInput\" title=\"B、\" lay-skin=\"primary\" "+(studentAnswerArr.contains("B") ? "checked" : "")+" disabled>\n" +
            "                   <span class=\"questionDesc questionInput\" style=\"padding-top: 0px;\" title='"+(answerDescArr[1] || "")+"'>"+(answerDescArr[1] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"checkbox\" value=\"C\" class=\"questionInput\" title=\"C、\" lay-skin=\"primary\" "+(studentAnswerArr.contains("C") ? "checked" : "")+" disabled>\n" +
            "                   <span class=\"questionDesc questionInput\" style=\"padding-top: 0px;\" title='"+(answerDescArr[2] || "")+"'>"+(answerDescArr[2] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"checkbox\" value=\"D\" class=\"questionInput\" title=\"D、\" lay-skin=\"primary\" "+(studentAnswerArr.contains("D") ? "checked" : "")+" disabled>\n" +
            "                   <span class=\"questionDesc questionInput\" style=\"padding-top: 0px;\" title='"+(answerDescArr[3] || "")+"'>"+(answerDescArr[3] || "")+"</span>\n" +
            "               </div>\n" +
            "           </div>\n" +
            "       </div>";
        return html;
    },
    renderJudgeQuestion:function (question) {
        var studentAnswer = "2";//默认0、1之外的其他值，不能为空，不然studentAnswer == 0会为真
        if(studentExamObj.answerQuestionMap && studentExamObj.answerQuestionMap[question.id]){
            studentAnswer = studentExamObj.answerQuestionMap[question.id].studentAnswer;
        }
        var html = "<div class=\"questionAnswerDesc\">\n" +
            "           <div class=\"col-sm-12\">\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"1\" title=\"正确\" class=\"questionInput\" "+(studentAnswer == 1 ? 'checked' : '')+" disabled>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"0\" title=\"错误\" class=\"questionInput\" "+(studentAnswer == 0 ? 'checked' : '')+" disabled>\n" +
            "               </div>\n" +
            "           </div>\n" +
            "       </div>";
        return html;
    },
    renderCompleteQuestion:function (question) {
        var answerNum = question.questionAnswerNum || 0;
        var studentAnswerArr = [];
        if(studentExamObj.answerQuestionMap && studentExamObj.answerQuestionMap[question.id]){
            studentAnswerArr = studentExamObj.answerQuestionMap[question.id].studentAnswer.split("<^_^>");;
        }
        var html = "";
        for(var ii = 0; ii < answerNum; ii++){
            html += "<div class=\"questionAnswerDesc input-group m-b\">\n" +
                "        <span class=\"input-group-addon\">"+(ii+1)+"</span>\n" +
                "        <textarea name=\"answer"+question.id+"\" placeholder=\"请输入答案\" class=\"layui-textarea questionInput\" readonly>"+((studentAnswerArr && studentAnswerArr.length >= ii+1) ? studentAnswerArr[ii] : '')+"</textarea>\n" +
                "    </div>";
        }
        return html;
    },
    renderEssayQuestion:function (question) {
        var questionTypeItem = studentExamObj.questionTypeMap[question.questionType];
        var seq = studentExamObj.currentQuestionIndex+1;
        var studentAnswer = "";
        if(studentExamObj.answerQuestionMap && studentExamObj.answerQuestionMap[question.id]){
            studentAnswer = studentExamObj.answerQuestionMap[question.id].studentAnswer;
        }
        var html = "<div class=\"questionAnswerDesc\">\n" +
            "           <textarea name=\"answer"+question.id+"\" placeholder=\"请输入答案\" class=\"layui-textarea questionInput\" readonly>"+(studentAnswer || "")+"</textarea>\n" +
            "       </div>";
        return html;
    },
    renderTeacherGrade:function (question) {
        var teacherGrade = 0;
        var studentAnswer = "";
        if(studentExamObj.answerQuestionMap && studentExamObj.answerQuestionMap[question.id]){
            teacherGrade = studentExamObj.answerQuestionMap[question.id].teacherGrade || 0;
            studentAnswer = studentExamObj.answerQuestionMap[question.id].studentAnswer || "";
        }
        var readonly = "";
        //如果是讲师，则问答题、填空题分数可编辑
        if(studentExamObj.teacherFlag){
            readonly = (question.questionType == 1 || question.questionType == 2 || question.questionType == 3) ? "readonly" : "";
        }else{
            readonly = "readonly"
        }
        var answerResult = question.questionAnswer == studentAnswer ? true : false;
        var questionAnswer= question.questionAnswer.replace(new RegExp("<\\^_\\^>", "g"), "");
        var tmpHtml = "";
        if(question.questionType == 1 || question.questionType == 2 || question.questionType == 3){
            if(answerResult){
                tmpHtml += " <span class=\"trueDesc\">\n" +
                    "            <i class=\"fa fa-check\"></i>\n" +
                    "            正确\n" +
                    "        </span>";
            }else {
                tmpHtml += " <span class=\"falseDesc\">\n" +
                    "            <i class=\"fa fa-close\"></i>\n" +
                    "            错误\n" +
                    "        </span>";
            }
            tmpHtml += "<span class=\"trueAnswerDesc\">\n" +
                "           正确答案：\n" +
                "       </span>\n" +
                "       <span class=\"trueAnswer\">\n" +
                "           "+(questionAnswer || '')+"\n" +
                "       </span>";
        }
        var html = "<div class=\"questionAnswerDesc\">\n" +
            "           "+tmpHtml+"\n" +
            "           <span>得分：</span>\n" +
            "           <input name='teacherGrade' type=\"text\" max='"+question.questionGrade+"' class=\"form-control teacherGrade\" "+readonly+" oninput='studentExamObj.validMaxTeacherGrade(this);' ' onkeyup=\"value=value.replace(/[^\\d.]/g,'');\" value='"+teacherGrade+"'/>\n" +
            "           <span>分</span>\n" +
            "       </div>";
        return html;
    },
    renderMarkArea:function (question) {
        var teacherRemark = "";
        if(studentExamObj.answerQuestionMap && studentExamObj.answerQuestionMap[question.id]){
            teacherRemark = studentExamObj.answerQuestionMap[question.id].teacherRemark || "";
        }
        var html = "";
        //如果有批阅信息，则展示出来，否则不展示
        if(teacherRemark){
            var readonly = studentExamObj.teacherFlag ? "" : "readonly";//如果是讲师，则可编辑
            html += "<div class=\"questionAnswerDesc\">\n" +
                "           <textarea name=\"teacherRemark\" placeholder=\"请输入批阅内容\" class=\"layui-textarea questionInput\" "+readonly+">"+teacherRemark+"</textarea>\n" +
                "    </div>";
        }
        return html;
    },
    dispatchRenderQuestion:function (question, questionType) {
        if(questionType == 1){
            return studentExamObj.renderRadioQuestion(question);
        }else if(questionType == 2){
            return studentExamObj.renderCheckQuestion(question);
        }else if(questionType == 3){
            return studentExamObj.renderJudgeQuestion(question);
        }else if(questionType == 4){
            return studentExamObj.renderCompleteQuestion(question);
        }else {
            return studentExamObj.renderEssayQuestion(question);
        }
    },
    loadAllQuestion: function () {
        var html = "";
        for(var it = 1; it <= 5; it++){
            if(studentExamObj.questionTypeMap[it].questionList && studentExamObj.questionTypeMap[it].questionList.length > 0){
                html += studentExamObj.renderQuestionTypeTitle(it) + "\n";
                html += "<div class=\"questionContentWrap\">\n";
                $.each(studentExamObj.questionTypeMap[it].questionList, function (jx, question) {
                    var answerCardDetailId = "";
                    if(studentExamObj.answerQuestionMap && studentExamObj.answerQuestionMap[question.id]){
                        answerCardDetailId = studentExamObj.answerQuestionMap[question.id].id || "";
                    }
                    html += "<!--题目基本信息-->\n" +
                        "    <div class=\"questionItem\" >\n" +
                        "       <input type=\"hidden\" name=\"answerCardDetailId\" value='"+answerCardDetailId+"'>\n"+
                        "       "+studentExamObj.renderQuestionTitle(question)+"\n"+
                        "       "+studentExamObj.dispatchRenderQuestion(question, question.questionType)+"\n"+
                        "       "+studentExamObj.renderTeacherGrade(question)+"\n"+
                        "       "+studentExamObj.renderMarkArea(question)+"\n"+
                        "    </div>\n"
                    studentExamObj.currentQuestionIndex++; //下一题
                });
                html += "    </div>";
            }
        }
        $(".paperWayWrap2").find(".paperContentWrap").html(html);
    },
    validMaxTeacherGrade:function (t) {
        var max = $(t).attr("max");
        var val = $(t).val();
        if(val > max){
            $(t).val(max);
        }
    },
    handleData: function () {
        studentExamObj.answerCard = {id:"", paperId:"",answerCardDetailList:[]};
        var jsonData = $("#answerCardForm2").serializeForm();
        studentExamObj.answerCard.id = jsonData["answerCardId"] || "";//答题卡ID
        studentExamObj.answerCard.paperId = jsonData["paperId"] || ""; //试卷ID
        studentExamObj.answerCard.createId = studentExamObj.answerData.createId;//答题人
        $(".questionItem").each(function (i, questionItem) {
            var answerCardDetailId = $(questionItem).find("input[name='answerCardDetailId']").val();
            var teacherGrade = $(questionItem).find("input[name='teacherGrade']").val() || "";
            var teacherRemark = $(questionItem).find("textarea[name='teacherRemark']").val() || "";
            studentExamObj.answerCard.answerCardDetailList.push({id:answerCardDetailId, teacherGrade:teacherGrade, teacherRemark:teacherRemark});
        });
    },
    submit: function () {
        studentExamObj.handleData();
        commonObj.requestData(JSON.stringify(studentExamObj.answerCard), "/answerCard/mark", "post", "json", false, function (data) {
            if(data.code == 200){
                layer.msg("阅卷成功！", {time: 2000, icon: 6});
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        }, null, true);
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
}