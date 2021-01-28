$(function () {
    // 单选框、多选框美化；
    $(".i-checks").iCheck({
        checkboxClass: "icheckbox_square-green",
        radioClass: "iradio_square-green",
    });

    // 表格的背景色；
    $("#recordTable tr td").each(function () {
        var spanElement = $(this).find("span");
        if (spanElement && spanElement.length > 0) {
            return;
        } else {
            $(this).css("background-color", "#EBEBEB");
        }
    });

    // 评分插件初始化；
    $("#impression .kv-fa, #ability .kv-fa").rating({
        theme: "krajee-fa",
        language: "zh",
        step: 1,
        readonly: true,
        showClear: false,
        filledStar: "<i class='fa fa-star'></i>",
        emptyStar: "<i class='fa fa-star-o'></i>"
    });

    // 审核信息先隐藏；
    $(".comment").css("margin-left", "10%");
    $(".comment").css("margin-right", "10%");
    $(".comment").hide();
    $("#employSalaryDiv").css("margin-left", "10%");
    $("#employSalaryDiv").css("margin-right", "10%");
    $("#employSalaryDiv").hide();

    // 初始化表格的数据；
    dataInit();
});
//获取是否有查询员工薪资权限
function getSalaryPermission(){
    var salaryPermissionFlag = false;
    $.ajax({
        type: "post",
        url: baseUrl + "/employeeManage/getSalaryPermission",
        data: null,
        dataType: 'json',
        async: false,
        success: function (result) {
            if (result.code == 200) {
                salaryPermissionFlag = true;
            } else {
                salaryPermissionFlag = false;
            }
        }
    });
    return salaryPermissionFlag;
}

//加载员工薪资
function loadSalary(entryId) {
    $.post(baseUrl + "/employeeManage/getEmploySalary", {entryId:entryId}, function (result) {
        if (result.code === 200) {
            if(result.data.salary){
                $("#employSalaryDiv").css("display", "block");
                $("#employSalaryDiv input").each(function () {
                    $(this).val(result.data.salary[$(this).attr("name")]);
                });
            }else {
                $("#employSalaryDiv").css("display", "none");
            }
        }
    });
}

//初始化公司信息
function loadCompany(companyMap) {
    $.ajax({
        url: baseUrl + "/dept/listJTAllCompany",
        type: "get",
        data: null,
        dataType: "json",
        async: false,
        success: function (data) {
            if(data.code == 200 && data.data.result){
                var resData = data.data.result;
                $(resData).each(function (i, d) {
                    companyMap[d.code] = d.name;
                });
            }
        }
    });
}

// 初始化数据；
var familyRelationMap = {0:"父亲",1:"母亲",2:"丈夫",3:"妻子",4:"儿子",5:"女儿",6:"哥哥",7:"弟弟",8:"姐姐",9:"妹妹",10:"叔叔",11:"阿姨",12:"舅舅",13:"舅妈",14:"姑姑",15:"姑父"};
function dataInit() {
    alertMessage("初始化中，请稍候。");
    // 获取申请入职的信息；
    var id = getQueryString("id");
    var code = getQueryString("code");
    if ((id && id.length > 0) || (code && code.length > 0)) {
        var url = "";
        var editUrl = "";
        var completeUrl = "";
        var params = {};
        if (id && id.length > 0) {
            url = baseUrl + "/entryManage/getEntryInfo";
            editUrl = "/regist?id=" + id;
            completeUrl = "/complete?id=" + id;
            params = {id: id};
        } else {
            url = baseUrl + "/entry/getEntryInfo";
            editUrl = "/regist?code=" + code;
            completeUrl = "/complete?code=" + code;
            params = {code: code};
        }
        $.post(url, params, function (data) {
            var dataValue = data.data.entry;
            if (dataValue == null) {
                $("#message").html("*（查询无结果，入职申请可能不存在或审核未通过）");
                alertMessage("查询无结果，入职申请可能不存在或审核未通过。");
            } else {
                alertMessage("系统处理中，请稍候。");
                //如果有权限查询薪资，则查询
                if(getSalaryPermission()){
                    $("#employSalaryDiv").css("display", "block");
                    loadSalary(dataValue.entryId);
                }else {
                    $("#employSalaryDiv").css("display", "none");
                }

                var companyMap = {};
                loadCompany(companyMap);//加载公司部门

                //如果没有选择第二部门，则展示第一部门
                if(!dataValue.dept){
                    var firstDept = "";
                    if(dataValue['entryFirstDept'] == 0){
                        firstDept = "业务部门";
                    }else if(dataValue['entryFirstDept'] == 1){
                        firstDept = "媒介部门";
                    }else {
                        firstDept = "其他部门";
                    }
                    $("#recordTable").find("span[name='dept']").html(firstDept +"("+companyMap[dataValue['entryCompanyCode']]+")");
                    $("#recordTable").find("span[name='dept']").attr("title",firstDept +"("+companyMap[dataValue['entryCompanyCode']]+")");
                }

                // 获取值；
                for (var key in dataValue) {
                    if("dept" == key){
                        $("#recordTable").find("span[name='" + key + "']").html(dataValue[key]+"("+companyMap[dataValue['entryCompanyCode']]+")");
                        $("#recordTable").find("span[name='" + key + "']").attr("title",dataValue[key]+"("+companyMap[dataValue['entryCompanyCode']]+")");
                    }else {
                        $("#recordTable").find("span[name='" + key + "']").html(dataValue[key]);
                    }
                }

                // 性别；
                var numValue = dataValue.empGender;
                if (numValue == 0) {
                    $("#empGender").html('<span style="color: red;">女</span>');
                } else {
                    $("#empGender").html('<span style="color: green;">男</span>');
                }

                // 户口性质；
                numValue = dataValue.entryResidence;
                if (numValue == 0) {
                    $("#entryResidence").html('<span style="color: green;">城镇户口</span>');
                } else {
                    $("#entryResidence").html('<span style="color: red;">农村户口</span>');
                }

                // 照片；
                $("#loadImage").val(dataValue.entryImage);
                loadPortrait();

                // 学历；
                numValue = dataValue.empEducation;
                var education = ["初中", "高中", "专科", "本科", "硕士", "博士", "博士后","其他","小学"];
                var educationColor = ["grey", "black", "blue", "orange", "orangered", "red", 'darkred','purple','#80766C'];
                if (numValue == 7) {
                    $("#empEducation").html('<span style="color: purple;">' + dataValue.empEducationOther + '</span>');
                } else {
                    if (numValue >= 0) {
                        $("#empEducation").html('<span style="color: ' + educationColor[numValue] + ';">' + education[numValue] + '</span>');
                    }
                }

                // 婚姻状况；
                var marriage = ["未婚", "已婚", "离婚", "丧偶"];
                var marriageColor = ["green", "red", "blue", "black"];
                numValue = dataValue.entryMarriage;
                if (numValue >= 0 && numValue <= 3) {
                    $("#entryMarriage").html('<span style="color: ' + marriageColor[numValue] + ';">' + marriage[numValue] + '</span>');
                }

                // 家庭成员；
                var family = dataValue.family;
                var familyLength = family.length;
                if (family && familyLength > 0) {
                    var familyHtml = "";
                    var familyData;
                    for (var i = 0; i < familyLength; i++) {
                        familyData = family[i];
                        familyHtml += '<tr><td colspan="2">' + familyRelationMap[familyData.famRelation] + '</td>';
                        familyHtml += '<td>' + familyData.famName + '</td>';
                        familyHtml += '<td>' + familyData.famAge + '</td>';
                        familyHtml += '<td>' + (familyData.famBirthday ? new Date(familyData.famBirthday).format("yyyy-MM-dd") : "") + '</td>';
                        familyHtml += '<td colspan="2">' + familyData.famUnit + '</td>';
                        familyHtml += '<td>' + (familyData.famHealth ? familyData.famHealth : "") + '</td>';
                        familyHtml += '<td>' + familyData.famProfession + '</td>';
                        familyHtml += '<td colspan="2">' + familyData.famDesc + '</td></tr>';
                    }

                    if (familyHtml.length > 0) {
                        $("#family").after(familyHtml);
                        $("#family").remove();
                        $("#familyTitle").attr("rowspan", familyLength + 1);
                    }
                }

                // 教育经历；
                var education = dataValue.education;
                var educationLength = education.length;
                if (education && educationLength > 0) {
                    var educationData;
                    // 学历；
                    var educationHtml = "";
                    for (var i = 0; i < educationLength; i++) {
                        educationData = education[i];
                        educationHtml += '<tr><td colspan="2">' + new Date(educationData.eduStart).format("yyyy.MM.dd") + '&nbsp;-&nbsp;' + new Date(educationData.eduEnd).format("yyyy.MM.dd") + '</td>';
                        educationHtml += '<td colspan="2">' + educationData.eduCollege + '</td>';
                        educationHtml += '<td colspan="2">' + educationData.eduLocation + '</td>';
                        educationHtml += '<td>' + (educationData.eduDuration || "") + '</td>';
                        educationHtml += '<td>' + educationData.eduMajor + '</td>';
                        educationHtml += '<td colspan="2">' + educationData.eduRecord + '</td></tr>';
                    }
                    $("#education").after(educationHtml);
                    $("#education").remove();
                    $("#educationTitle").attr("rowspan", educationLength+1);
                    $("#education").html(educationHtml);
                }

                // 工作经历；
                var experience = dataValue.experience;
                var experienceLength = experience.length;
                if (experience && experienceLength > 0) {
                    var experienceHtml = "";
                    var experienceData;
                    for (var i = 0; i < experienceLength; i++) {
                        experienceData = experience[i];
                        experienceHtml += '<tr><td colspan="2">' + new Date(experienceData.expStart).format("yyyy.MM.dd") + '&nbsp;-&nbsp;' + new Date(experienceData.expEnd).format("yyyy.MM.dd") + '</td>';
                        experienceHtml += '<td colspan="2">' + experienceData.expCompany + '</td>';
                        experienceHtml += '<td colspan="2">' + experienceData.expLocation + '</td>';
                        experienceHtml += '<td>' + experienceData.expProfession + '</td>';
                        experienceHtml += '<td>' + experienceData.expSalary + '</td>';
                        experienceHtml += '<td>' + experienceData.expContactor + '</td>';
                        experienceHtml += '<td>' + experienceData.expResignReason + '</td></tr>';
                    }

                    if (experienceHtml.length > 0) {
                        $("#experience").after(experienceHtml);
                        $("#experience").remove();
                        $("#experienceTitle").attr("rowspan", experienceLength + 1);
                    }
                }

                // 是否有驾照；
                numValue = dataValue.entryHasLicence;
                if (numValue == 0) {
                    $("#entryHasLicence").html('<span style="color: green;">是</span>');
                } else {
                    $("#entryHasLicence").html('<span style="color: red;">否</span></td>');
                    $("#entryHasLicence").parent().nextAll().remove();
                    $("#entryHasLicence").parent().attr("colspan", 8);
                }

                // 是否有曾经病史；
                numValue = dataValue.entryHasSick;
                if (numValue == 1) {
                    $("#entryHasSick").html('<span style="color: red;">是</span></td>');
                } else {
                    //没有获取到病史信息默认为否
                    $("#entryHasSick").html('<span style="color: green;">否</span>');
                    $("#entryHasSick").parent().nextAll().remove();
                    $("#entryHasSick").parent().attr("colspan", 8);
                }

                // 求职途径；
                numValue = dataValue.entryChannel;
                var channel = ['BOSS直聘', '社交媒体', '离职再入职', '人才市场', '校园招聘', '猎头推荐', '内部推荐 ', '其他', '前程无忧', '智联招聘', '分子公司调岗'];
                var channelColor = ["orange", "red", "green", "grey", "blue", "black", "purple", "grey","orange","orange","green"];
                if (numValue >= 0 && numValue <= 10) {
                    // 如果不是内部推荐则隐藏推荐人信息；
                    if (numValue == 6) {
                        $("#otherColumn").attr("rowspan", "5");
                        $("#relative").show();
                    } else {
                        $("#otherColumn").attr("rowspan", "4");
                        $("#relative").hide();
                    }
                    $("#entryChannel").html('<span style="color: ' + channelColor[numValue] + ';">' + channel[numValue] + '</span>');
                }

                // 当前状态；
                var state = ["待审核", "审核中", "同意录用", "已入职", "不予考虑", "已离职"];
                var stateColor = ["orange", "red", "green", "blue", "grey", "black"];
                numValue = dataValue.state;
                if (numValue >= 0 && numValue <= 5) {
                    $("#state").css("color", stateColor[numValue]);
                    $("#state").html(state[numValue]);

                    // 待审核状态下的数据允许编辑；
                    if (numValue == 0) {
                        $("#editEntry").attr("onclick", "window.open('" + editUrl + "')");
                        $("#editEntry").show();
                    } else {
                        $("#editEntry").hide();
                    }

                    // 审核通过后，如果资料不完整，显示完善资料按钮；
                    if (numValue == 2 && dataValue.entryComplete == 0) {
                        $("#completeEntry").attr("onclick", "window.open('" + completeUrl + "')");
                        $("#completeEntry").show();
                    } else {
                        $("#completeEntry").hide();
                    }
                }

                // 创建时间；
                $("#empDate").html(new Date(dataValue.empDate).format("yyyy-MM-dd"));

                // 评分信息的初始化；
                var commentData = dataValue.comment;
                if (commentData && commentData.length > 0) {
                    var comment;
                    for (var i = 0; i < commentData.length; i++) {
                        comment = commentData[i];
                        // 人事面试；
                        if (comment.comType == 0) {
                            $("#impression input").each(function () {
                                $(this).val(comment[$(this).attr("name")]);
                                // 星级评分；
                                if ($(this).hasClass("kv-fa")) {
                                    $(this).rating("update", comment[$(this).attr("name")]);
                                }
                            });
                            $("#entryFile input").each(function () {
                                $(this).val(comment[$(this).attr("name")]);
                            });
                            $("#entryFile textarea").each(function () {
                                $(this).val(comment[$(this).attr("name")]);
                            });
                            $("#impression textarea[name='comAdvice']").text(comment.comAdvice);
                        }
                        // 专业测试；
                        if (comment.comType == 1) {
                            $("#ability input").each(function () {
                                $(this).val(comment[$(this).attr("name")]);
                                // 星级评分；
                                if ($(this).hasClass("kv-fa")) {
                                    $(this).rating("update", comment[$(this).attr("name")]);
                                }
                            });
                            $("#ability textarea[name='comAdvice']").text(comment.comAdvice);
                        }
                        // 部长意见；
                        if (comment.comType == 2) {
                            if (comment.comFigure == 0) {
                                $("#opinion").val("同意录用");
                            } else {
                                $("#opinion").val("不予考虑");
                            }
                            $("leaderDesc").text(comment.comAdvice);
                        }
                    }
                    $(".comment").show();
                } else {
                    $(".comment").hide();
                }

                commentData = dataValue.file;
                if (commentData) {
                    var fileName;
                    $("#entryFile input").each(function () {
                        fileName = commentData[$(this).attr("data")];
                        if (fileName) {
                            $(this).parent().append("<a href='" + fileName + "' target='_blank'>&nbsp;点击查看</a>");
                        }
                    });
                }
            }
        }, "json");
    } else {
        alertMessage("无权限访问，即将前往登录页面。");
        setTimeout(function () {
            window.location.href = "/login";
        }, 1000);
    }
}