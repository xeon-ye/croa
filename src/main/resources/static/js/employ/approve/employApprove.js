$(function () {
    loadEmployData(getQueryString("code"));

    // 设置表格默认的UI样式；
    $.jgrid.defaults.styleUI = 'Bootstrap';

    // 增加表单校验；
    $("#approveForm").validate();

    // 提交录用流程的评分插件；
    $("#approveForm .kv-fa").rating({
        theme: "krajee-fa",
        language: "zh",
        step: 1,
        readonly: true,
        showClear: false,
        filledStar: "<i class='fa fa-star'></i>",
        emptyStar: "<i class='fa fa-star-o'></i>"
    });
});

// 打开选择审核的窗口；
function loadEmployData(code) {
    if (code && code.length > 0) {
        // 获取审核用户信息；
        $.post(baseUrl + "/entryManage/getApproveInfo", {code: code}, function (data) {
            var dataValue = data.data.taskId;
            if (dataValue == null) {
                alertMessage("该审核已完成。");
                // 返回审核页面；
                setTimeout(function () {
                    window.location.href = '/process/queryTask';
                }, 1000);
            } else {
                // 隐藏数据；
                $("#approveForm input[name='deptId']").val(data.data.deptId);
                $("#approveForm input[name='taskId']").val(dataValue);
                // 审核数据；
                dataValue = data.data.hire;
                for (var key in dataValue) {
                    $("#approveForm").find("input[type='text'][name='" + key + "']").val(dataValue[key]);
                }

                // 录用信息的初始化；
                $("#approveForm").find(".layer-date").each(function () {
                    var dateStr = dataValue[$(this).attr("name")];
                    if (dateStr) {
                        $(this).val(new Date(dateStr).format("yyyy-MM-dd"));
                    }
                });

                // 试用期的时间样式微调；
                $("#approveForm input[name='trialBegin']").css("display", "inline");
                $("#approveForm input[name='trialEnd']").css("display", "inline");

                // 性别；
                var empGender = dataValue.empGender;
                if (empGender == 0) {
                    $("#empGenderText").val("女");
                } else {
                    $("#empGenderText").val("男");
                }

                // 婚姻状况；
                var entryMarriage = dataValue.empMarriage;
                if (entryMarriage >= 0 && entryMarriage <= 3) {
                    var marriage = ["未婚", "已婚", "离婚", "丧偶"];
                    $("#empMarriageText").val(marriage[entryMarriage]);
                }

                // 学历；
                var education = ["初中", "高中", "专科", "本科", "硕士", "博士", "博士后","其他","小学"];
                var entryEducation = dataValue.empEducation;
                if (entryEducation == 7) {
                    $("#empEducationText").val(dataValue.empEducationOther);
                } else {
                    if (entryEducation >= 0) {
                        $("#empEducationText").val(education[entryEducation]);
                    }
                }

                // 院校及专业；
                var collegeMajor = "";
                var entryCollege = dataValue.empCollege;
                if (entryCollege) {
                    collegeMajor += entryCollege;
                }
                var entryMajor = dataValue.empMajor;
                if (entryMajor) {
                    collegeMajor += "【" + entryMajor + "】";
                }
                $("#collegeMajor").val(collegeMajor);

                // 薪资信息的初始化；
                var salary = data.data.salary;
                if (salary != null) {
                    $("#approveForm").find("input[number='true']").each(function () {
                        $(this).val(salary[$(this).attr("name")]);
                    });
                }

                // 评分信息的初始化；
                dataValue = data.data.comment;
                if (dataValue && dataValue.length > 0) {
                    var comment;
                    for (var i = 0; i < dataValue.length; i++) {
                        comment = dataValue[i];
                        // 人事面试；
                        if (comment.comType == 0) {
                            $("#impression input").each(function () {
                                $(this).val(comment[$(this).attr("name")]);
                                // 星级评分；
                                if ($(this).hasClass("kv-fa")) {
                                    $(this).rating("update", comment[$(this).attr("name")]);
                                }
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
                    }
                }

                // 审核人；
                dataValue = data.data.user;
                if (dataValue != null && dataValue.length > 0) {
                    layui.use(["form"], function () {
                        var userObj = $("#approveForm select[name='userId']");
                        for (var i = 0; i < dataValue.length; i++) {
                            userObj.append("<option value='" + dataValue[i].id + "'>" + dataValue[i].name + "</option>");
                        }
                        // 设置用户名；
                        var userNameObj = $("#approveForm").find("input[name='userName']");
                        userNameObj.val(dataValue[0].name);
                        // 初始化；
                        layui.form.render();

                        // 下拉框的onchange事件；
                        layui.form.on("select(userId)", function (userData) {
                            // 更新隐藏域；
                            userNameObj.val(getLayUISelectText(userData));
                        });
                    });
                    $("#approveForm .approveUser").show();
                } else {
                    $("#approveForm .approveUser").hide();
                }
                $("#employModal").modal({backdrop: "static", keyboard: false});
            }
        }, "json");
    } else {
        alertMessage("无权限访问。");
        // 返回审核页面；
        setTimeout(function () {
            window.location.href = '/process/queryTask';
        }, 1000);
    }
}