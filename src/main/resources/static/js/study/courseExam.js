//页面初始化函数
$(function () {
    studentExamObj.init();
});

//学员考试
var studentExamObj = {
    paper:{}, //试卷信息
    answerData: {}, //答题卡信息
    examStartTime:null,//考试开始时间
    examEndTime:null,//考试结束时间
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
    // currentQuestionType: 0, //逐题展示，当前题目类型
    lastQuestionId:0, //逐题展示，最后一题ID，当提交异常时，再次提交将会出现两条一样的记录，用于删除数据
    nextBtnClickFlag: true, //逐题展示，下一题按钮点击控制，该标记为true时，点击产生效果
    currentPaperAutoMarkFlag: 1, //是否可自动阅卷：0-是、1-否，针对于只含有单选、多选、判断题的试卷，系统自动判断设置值，默认否
    answerCard:{id:"", paperId:"", autoMarkFlag:1, remainTime: 0 ,examStartTime:null,examEndTime:null,answerCardDetailList:[]}, //学员答题卡信息
    submitClickFlag: true, //提交按钮点击标识，控制提交只能点击一次，同时，控制直接关闭浏览器发送请求，因为点击提交按钮调用window.close()也会触发onbeforeunload
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
            //如果考试开始和结束时间没有传入，则根据考试时长计算
            if(!studentExamObj.examStartTime){
                studentExamObj.examStartTime = answerData.examStartTime ? new Date(answerData.examStartTime).format('yyyy.MM.dd hh:mm:ss') : "";
            }
            if(!studentExamObj.examEndTime){
                studentExamObj.examEndTime = answerData.examEndTime ? new Date(answerData.examEndTime).format('yyyy.MM.dd hh:mm:ss') : "";
            }
            if(answerData.answerCardDetailList && answerData.answerCardDetailList.length > 0){
                $.each(answerData.answerCardDetailList, function (a, answerCardDetail) {
                    studentExamObj.answerQuestionMap[answerCardDetail.questionId] = answerCardDetail;
                });
            }
        }
    },
    init: function () {
        var paperId = commonObj.getQueryString("paperId");
        var studentId = commonObj.getQueryString("studentId");
        studentExamObj.examStartTime = commonObj.getQueryString("examStartTime");//考试开始时间
        studentExamObj.examEndTime = commonObj.getQueryString("examEndTime");//考试结束时间
        commonObj.requestData({id:paperId, orderFlag:true, examFlag: true}, "/paper/getPaperDetailById", "post", "json", false, function (data) {
            if(data.code == 200){
                var result = data.data.result;
                //保存当前试卷是否可自动阅卷标识
                studentExamObj.currentPaperAutoMarkFlag = result.paper.autoMarkFlag;

                //缓存试卷信息，并进行数据封装
                studentExamObj.handlePaper(result);
                //请求答题卡信息，判断是否存在答题卡
                commonObj.requestData({paperId:paperId,studentId:studentId, examFlag: true}, "/answerCard/getAnswerCard", "post", "json", false, function (answerData) {
                    if(answerData.code == 200){
                        //缓存答题卡信息，并进行数据封装
                        studentExamObj.handleAnswerCard(answerData.data.result);
                    }
                });
                //如果考试开始和结束时间没有传入，则根据考试时长计算
                if(!studentExamObj.examStartTime){
                    studentExamObj.examStartTime = new Date().format('yyyy.MM.dd hh:mm:ss');
                }
                if(!studentExamObj.examEndTime){
                    var millSecond = new Date().getTime() + studentExamObj.paper.paperTime * 60 * 1000;
                    studentExamObj.examEndTime = new Date(millSecond).format('yyyy.MM.dd hh:mm:ss');
                }
                //渲染试卷基本信息
                studentExamObj.renderPaper(studentExamObj.paper);
                //卷面展现方式控制
                studentExamObj.paperDisplay(studentExamObj.paper.paperWay);
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        });
        $("input[name='examStartTime']").val(studentExamObj.examStartTime);
        $("input[name='examEndTime']").val(studentExamObj.examEndTime);
    },
    paperDisplay: function (paperWay) {
        //0-逐题展示、1-整卷展示
        if(paperWay == 1){
            $(".paperWayWrap1").css("display", "none");
            $(".paperWayWrap2").css("display", "block");
            //渲染所有题目
            studentExamObj.loadAllQuestion();
        }else {
            $(".paperWayWrap1").css("display", "block");
            $(".paperWayWrap2").css("display", "none");
            //渲染单条题目
            studentExamObj.loadOneQuestion(); //加载题目
        }
    },
    renderPaper:function (paper) {
        $("input[name='paperId']").val(paper.id);
        if(studentExamObj.answerData){
            $("input[name='answerCardId']").val(studentExamObj.answerData.id);
        }
        $(".paperTitle").text(paper.paperTitle);
        $(".paperTitle").attr("title", paper.paperTitle); //试卷标题
        $(".paperGrade").text(paper.paperGrade);//试卷总分
        $(".paperQuestionNum").text(paper.questionNum + "题");//试卷题数
        $(".paperTime").text(paper.paperTime + "分钟");//试卷总时长
        //如果存在答题卡，则倒计时使用答题卡中剩余时间字段
        if(studentExamObj.answerData && studentExamObj.answerData.id){
            if(studentExamObj.examEndTime){
                commonObj.pageCountDown(Math.floor((new Date(studentExamObj.examEndTime).getTime() - new Date().getTime())/1000)); //倒计时
            }else {
                commonObj.pageCountDown(studentExamObj.answerData.remainTime || 0); //倒计时
            }
        }else {
            commonObj.pageCountDown(paper.paperTime * 60); //倒计时
        }
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
            "           <span>(<span class=\"questionGrade\">&nbsp;"+question.questionGrade+"分&nbsp;</span>)</span>\n" +
            "       </div>"
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
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"A\"  class=\"questionInput\" title=\"A、\"  "+(studentAnswer == 'A' ? 'checked' : '')+">\n" +
            "                   <span class=\"questionDesc questionInput\" title='"+(answerDescArr[0] || "")+"'>"+(answerDescArr[0] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"B\"  class=\"questionInput\" title=\"B、\" "+(studentAnswer == 'B' ? 'checked' : '')+">\n" +
            "                   <span class=\"questionDesc questionInput\" title='"+(answerDescArr[1] || "")+"'>"+(answerDescArr[1] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"C\"  class=\"questionInput\" title=\"C、\" "+(studentAnswer == 'C' ? 'checked' : '')+">\n" +
            "                   <span class=\"questionDesc questionInput\" title='"+(answerDescArr[1] || "")+"'>"+(answerDescArr[2] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"D\"  class=\"questionInput\" title=\"D、\" "+(studentAnswer == 'D' ? 'checked' : '')+">\n" +
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
            "                   <input name=\"answer"+question.id+"\" type=\"checkbox\" value=\"A\" class=\"questionInput\" title=\"A、\" lay-skin=\"primary\" "+(studentAnswerArr.contains("A") ? "checked" : "")+">\n" +
            "                   <span class=\"questionDesc questionInput\" style=\"padding-top: 0px;\" title='"+(answerDescArr[0] || "")+"'>"+(answerDescArr[0] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"checkbox\" value=\"B\" class=\"questionInput\" title=\"B、\" lay-skin=\"primary\" "+(studentAnswerArr.contains("B") ? "checked" : "")+">\n" +
            "                   <span class=\"questionDesc questionInput\" style=\"padding-top: 0px;\" title='"+(answerDescArr[1] || "")+"'>"+(answerDescArr[1] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"checkbox\" value=\"C\" class=\"questionInput\" title=\"C、\" lay-skin=\"primary\" "+(studentAnswerArr.contains("C") ? "checked" : "")+">\n" +
            "                   <span class=\"questionDesc questionInput\" style=\"padding-top: 0px;\" title='"+(answerDescArr[2] || "")+"'>"+(answerDescArr[2] || "")+"</span>\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"checkbox\" value=\"D\" class=\"questionInput\" title=\"D、\" lay-skin=\"primary\" "+(studentAnswerArr.contains("D") ? "checked" : "")+">\n" +
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
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"1\" title=\"正确\" class=\"questionInput\" "+(studentAnswer == 1 ? 'checked' : '')+">\n" +
            "               </div>\n" +
            "               <div class=\"col-sm-12 answerItem\">\n" +
            "                   <input name=\"answer"+question.id+"\" type=\"radio\" value=\"0\" title=\"错误\" class=\"questionInput\" "+(studentAnswer == 0 ? 'checked' : '')+">\n" +
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
                "        <textarea name=\"answer"+question.id+"\" placeholder=\"请输入答案\" class=\"layui-textarea questionInput\">"+((studentAnswerArr && studentAnswerArr.length >= ii+1) ? studentAnswerArr[ii] : '')+"</textarea>\n" +
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
            "           <textarea name=\"answer"+question.id+"\" placeholder=\"请输入答案\" class=\"layui-textarea questionInput\">"+(studentAnswer || "")+"</textarea>\n" +
            "       </div>";
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
    loadOneQuestion: function () {
        var question = studentExamObj.paperQuestionList[studentExamObj.currentQuestionIndex];
        var html = "";
        //渲染题目前，判断是否已答当前题，如果已答直接下一题
        if(studentExamObj.answerQuestionMap && studentExamObj.answerQuestionMap[question.id]){
            studentExamObj.currentQuestionIndex++; //下一题
            //判断是否还有题目
            if(studentExamObj.currentQuestionIndex < studentExamObj.paperQuestionList.length){
                studentExamObj.loadOneQuestion(); //加载题目
                return;
            }else {
                html += "<div style='width: 100%;height: 80px;line-height: 80px;font-size: 20px;text-align: center;color: #a7961c;'>题目已经全部答完，不允许查看已答题目</div>";
                layer.msg("题目已经全部答完，不允许查看已答题目！", {time: 2000, icon: 6});
            }
        }else {
            html += studentExamObj.renderQuestionTypeTitle(question.questionType) + "\n";
            html += "<div class=\"questionContentWrap\">\n" +
                "        <!--题目基本信息-->\n" +
                "        <div class=\"questionItem\" >\n" +
                "            <input type=\"hidden\" name=\"questionId\" value='"+question.id+"'>\n" +
                "        "+studentExamObj.renderQuestionTitle(question)+"\n"+
                "        "+studentExamObj.dispatchRenderQuestion(question, question.questionType)+"\n"+
                "        </div>\n" +
                "    </div>";
            //设置当前题目类型
            // studentExamObj.currentQuestionType = question.questionType;
        }
        $(".paperWayWrap1").find(".paperContentWrap").html(html);
        //判断是否还有下一题
        if(studentExamObj.currentQuestionIndex <= studentExamObj.paperQuestionList.length-2){
            $(".nextBtn").css("display", "block");
        }else {
            $(".nextBtn").css("display", "none");
        }
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    loadAllQuestion: function () {
        var html = "";
        for(var it = 1; it <= 5; it++){
            if(studentExamObj.questionTypeMap[it].questionList && studentExamObj.questionTypeMap[it].questionList.length > 0){
                html += studentExamObj.renderQuestionTypeTitle(it) + "\n";
                html += "<div class=\"questionContentWrap\">\n";
                $.each(studentExamObj.questionTypeMap[it].questionList, function (jx, question) {
                    html += "<!--题目基本信息-->\n" +
                        "    <div class=\"questionItem\" >\n" +
                        "    "+studentExamObj.renderQuestionTitle(question)+"\n"+
                        "    "+studentExamObj.dispatchRenderQuestion(question, question.questionType)+"\n"+
                        "    </div>\n"
                    studentExamObj.currentQuestionIndex++; //下一题
                });
                html += "    </div>";
            }
        }
        $(".paperWayWrap2").find(".paperContentWrap").html(html);
    },
    handleOneData: function () {
        var jsonData = $("#answerCardForm1").serializeForm();
        if(jsonData["answerCardId"] && !studentExamObj.answerCard.id){
            studentExamObj.answerCard.id = jsonData["answerCardId"] || "";//答题卡ID
        }
        if(!studentExamObj.answerCard.paperId){
            studentExamObj.answerCard.paperId = jsonData["paperId"] || "";//试卷ID
        }
        if(!studentExamObj.answerCard.examStartTime){
            studentExamObj.answerCard.examStartTime = jsonData["examStartTime"] ? new Date(jsonData["examStartTime"]) : "";//考试开始时间
        }
        if(!studentExamObj.answerCard.examEndTime){
            studentExamObj.answerCard.examEndTime = jsonData["examEndTime"] ? new Date(jsonData["examEndTime"]) : "";//考试结束时间
        }
        studentExamObj.answerCard.autoMarkFlag = studentExamObj.currentPaperAutoMarkFlag;//是否可自动阅卷：0-是、1-否，针对于只含有单选、多选、判断题的试卷，系统自动判断设置值，默认否

        var questionId = jsonData["questionId"];
        if(questionId){
            var studentAnswer = "";
            if(jsonData["answer"+questionId]){
                if(jsonData["answer"+questionId] instanceof Array){
                    studentAnswer = jsonData["answer"+questionId].join("<^_^>");
                }else {
                    studentAnswer = jsonData["answer"+questionId];
                }
            }
            //记录剩余时间
            studentExamObj.answerCard.remainTime = commonObj.secondTotal;//单位：秒
            //判断当前题目是否是上次最后一题ID，如果是，则进行更新处理，移除上一次保存的值
            if(studentExamObj.lastQuestionId == questionId){
                for(var ix = 0; ix < studentExamObj.answerCard.answerCardDetailList.length; ix++){
                    if(studentExamObj.answerCard.answerCardDetailList[ix].questionId == studentExamObj.lastQuestionId){
                        studentExamObj.answerCard.answerCardDetailList.remove(studentExamObj.answerCard.answerCardDetailList[ix]);
                        break;
                    }
                }
            }
            studentExamObj.answerCard.answerCardDetailList.push({questionId:questionId, studentAnswer:studentAnswer});
            //记录最后一题ID
            studentExamObj.lastQuestionId = questionId;
        }
    },
    handleAllData: function () {
        studentExamObj.answerCard = {id:"", paperId:"", autoMarkFlag:studentExamObj.currentPaperAutoMarkFlag, remainTime: 0 ,examStartTime:null,examEndTime:null,answerCardDetailList:[]}; //清空提交数据
        var jsonData = $("#answerCardForm2").serializeForm();
        studentExamObj.answerCard.id = jsonData["answerCardId"] || "";//答题卡ID
        studentExamObj.answerCard.paperId = jsonData["paperId"] || ""; //试卷ID
        studentExamObj.answerCard.examStartTime = jsonData["examStartTime"] ? new Date(jsonData["examStartTime"]) : ""; //考试开始时间
        studentExamObj.answerCard.examEndTime = jsonData["examEndTime"] ? new Date(jsonData["examEndTime"]) : ""; //考试结束时间
        // studentExamObj.answerCard.autoMarkFlag = jsonData["autoMarkFlag"] || "";//是否可自动阅卷：0-是、1-否，针对于只含有单选、多选、判断题的试卷，系统自动判断设置值，默认否

        //记录剩余时间
        studentExamObj.answerCard.remainTime = commonObj.secondTotal;//单位：秒
        if(studentExamObj.paperQuestionList && studentExamObj.paperQuestionList.length > 0){
            $.each(studentExamObj.paperQuestionList, function (index, question) {
                var studentAnswer = "";
                if(jsonData["answer"+question.id]){
                    if(jsonData["answer"+question.id] instanceof Array){
                        studentAnswer = jsonData["answer"+question.id].join("<^_^>");
                    }else {
                        studentAnswer = jsonData["answer"+question.id];
                    }
                }
                studentExamObj.answerCard.answerCardDetailList.push({questionId:question.id, studentAnswer:studentAnswer});
            });
        }
    },
    nextQuestionBtn:function () {
        //为了防止双击跳过题目，设置点击按钮后500豪秒后才能继续点击
        if(studentExamObj.nextBtnClickFlag){
            //控制下一题按钮点击效果
            studentExamObj.nextBtnClickFlag = false;
            $(".nextBtn").removeClass("nextBtnActive");
            $(".nextBtn").addClass("nextBtnDisable");
            setTimeout(function () {
                studentExamObj.nextBtnClickFlag = true; //2秒后才能继续点击
                $(".nextBtn").removeClass("nextBtnDisable");
                $(".nextBtn").addClass("nextBtnActive");
            }, 500);

            //获取当前题目用户答案，信息等等，缓存起来
            studentExamObj.handleOneData();

            studentExamObj.currentQuestionIndex++; //下一题
            if(studentExamObj.currentQuestionIndex < studentExamObj.paperQuestionList.length){
                studentExamObj.loadOneQuestion(); //加载题目
            }else {
                layer.msg("已经没有题目啦", {time: 2000, icon: 6});
            }
        }
    },
    submit: function () {
        if(studentExamObj.submitClickFlag){
            //提交按钮，点击无效
            studentExamObj.submitClickFlag = false;

            //暂停倒计时
            commonObj.intervalFlag = false;

            //0-逐题展示、1-整卷展示
            if(studentExamObj.paper.paperWay == 1){
                studentExamObj.handleAllData();
            }else {
                //最后一题，添加到提交参数中
                studentExamObj.handleOneData();
            }
            //发送请求
            studentExamObj.sendRequest( true);
        }else {
            layer.msg("试卷已提交，请不要重复操作！", {time: 2000, icon: 5});
        }
    },
    sendRequest: function (closeWinFlag) {
        if(studentExamObj.answerCard.answerCardDetailList && studentExamObj.answerCard.answerCardDetailList.length > 0){
            commonObj.requestData(JSON.stringify(studentExamObj.answerCard), "/answerCard/save", "post", "json", false, function (data) {
                if(data.code == 200){
                    layer.msg("试卷提交成功！", {time: 2000, icon: 6});
                    if(closeWinFlag){
                        window.location.href="about:blank";
                        window.opener = null;
                        window.open('', '_self');
                        window.close();
                    }
                }else {
                    //提交按钮点击有效
                    studentExamObj.submitClickFlag = true;
                    //启动倒计时
                    commonObj.intervalFlag = true;
                    layer.msg(data.msg, {time: 2000, icon: 5});
                }
            }, null, true);
        }else {
            layer.msg("试卷不存在题目，提交无效！", {time: 2000, icon: 5});
        }
    }
}

//页面公共处理对象
var commonObj = {
    secondTotal: 0, //试卷剩余总秒数
    intervalFlag: true, //点击提交按钮，时间函数将不会继续减剩余总秒数
    intervalTime: null, //循环方法返回值
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
    //考试倒计时
    renderCountDown:function () {
        var timeStr = "";
        if(commonObj.secondTotal <= 60){
            timeStr = commonObj.secondTotal + "秒";
        }else if(commonObj.secondTotal < 60*60){
            var minute = Math.floor(commonObj.secondTotal / 60);
            var second = commonObj.secondTotal % 60;
            timeStr = (minute < 10 ? ("0"+minute) : minute) + "分钟" + (second < 10 ? ("0"+second) : second) + "秒";
        }else if(commonObj.secondTotal < 24*60*60){
            var hour = Math.floor(commonObj.secondTotal / 60 / 60);
            var minute = Math.floor((commonObj.secondTotal - hour * 60 * 60) / 60);
            var second = (commonObj.secondTotal - hour * 60 * 60) % 60;
            timeStr = (hour < 10 ? ("0"+hour) : hour) + "小时" + (minute < 10 ? ("0"+minute) : minute) + "分钟" + (second < 10 ? ("0"+second) : second) + "秒";
        }else {
            var day = Math.floor(commonObj.secondTotal / 60 / 60 / 24);
            var hour = Math.floor((commonObj.secondTotal - day * 60 * 60 * 24) / 60 / 60);
            var minute = Math.floor((commonObj.secondTotal - day * 60 * 60 * 24 - hour * 60 * 60) / 60);
            var second = (commonObj.secondTotal - day * 60 * 60 * 24 - hour * 60 * 60) % 60;
            timeStr = (day < 10 ? ("0"+day) : day) + "天" + (hour < 10 ? ("0"+hour) : hour) + "小时" + (minute < 10 ? ("0"+minute) : minute) + "分钟" + (second < 10 ? ("0"+second) : second) + "秒";
        }
        //十分钟的时候提醒一次
        if(commonObj.secondTotal == 10*60){
            layer.msg("距离考试结束，还剩"+timeStr, {time: 2000, icon: 6});
        }
        $(".paperCountDown").text(timeStr);
    },
    pageCountDown: function (remainTime) {
        commonObj.secondTotal = remainTime;//剩余总秒数
        commonObj.renderCountDown(commonObj.secondTotal);//渲染总剩余时间，隔一秒开始调用时间函数
        if(commonObj.secondTotal <= 0){
            //提交试卷
            studentExamObj.submit();
            layer.msg("考试时间到，系统将自动提交试卷", {time: 2000, icon: 6});
            clearInterval(commonObj.intervalTime);//清除时间函数
        }else {
            //每秒触发
            commonObj.intervalTime = setInterval(function () {
                if(commonObj.secondTotal <= 0){
                    //提交试卷
                    studentExamObj.submit();
                    layer.msg("考试时间到，系统将自动提交试卷", {time: 2000, icon: 6});
                    clearInterval(commonObj.intervalTime);//清除时间函数
                }else {
                    //如果标识为true，则继续倒计时，否则暂停
                    if(commonObj.intervalFlag){
                        commonObj.secondTotal--;
                    }
                    commonObj.renderCountDown(commonObj.secondTotal);
                }
            }, 1000);
        }
    },
    //浏览器页面的关闭或者刷新
    pageClose:function (event) {
        event =event || window.event;//FF下window.event为空，IE下不传参数所以event1为空。
        //未点击提交按钮，业务处理
        if(studentExamObj.submitClickFlag){
            //暂停倒计时
            commonObj.intervalFlag = false;
            //提交按钮，点击无效
            studentExamObj.submitClickFlag = false;

            //0-逐题展示、1-整卷展示
            if(studentExamObj.paper.paperWay == 1){
                studentExamObj.handleAllData();
            }else {
                //最后一题，添加到提交参数中
                studentExamObj.handleOneData();
            }
            //发送请求
            studentExamObj.sendRequest(false);
        }
    },
}