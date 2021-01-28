$(function () {
    pageObj.init();
});

var pageObj = {
    selectedPeopleId: [],//已被选择人员id
    isSelectReady: false,//已经选择人员时为true
    currentSelectPeopleId: [],//当前modal框内已选人员id
    currentSelectPeopleName: [],//当前modal框内已选人员名称
    leftNum: 0,//pk的页面元素左侧数量
    rightNum: 0,//pk的页面元素右侧数量
    currentUploadDom: "",
    pkType: 0,
    renderICheck: function (rootDom) {
        rootDom.find('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green'
        });
    },
    //modal框内搜索
    searchPKPeople: function () {
        var rootDom = $("#peoplePKModal");

        if (pageObj.pkType === 0) {
            $.ajax({
                type: "get",
                url: baseUrl + "/user/listBusinessPart?name=" + $("#nameQc").val().trim(),
                dataType: "json",
                success: function (data) {
                    if (pageObj.selectedPeopleId.length > 0) {
                        //过滤掉已选的用户
                        data = data.filter(function (item) {
                            return !pageObj.selectedPeopleId.contains(item.id);
                        });
                    }
                    var userList = groupBy(data, function (item) {
                        return [item.deptId];
                    });
                    var html = template("peoplePKHtml", {'data': userList});
                    rootDom.find("div[data-id='people']").html(html);
                    pageObj.renderICheck(rootDom);
                }
            });
        } else {
            $.ajax({
                type: "get",
                url: baseUrl + "/dept/queryYWDeptByName?deptName=" + $("#nameQc").val().trim(),
                dataType: "json",
                success: function (data) {
                    if (pageObj.selectedPeopleId.length > 0) {
                        //过滤掉已选的用户
                        data = data.filter(function (item) {
                            return !pageObj.selectedPeopleId.contains(item.id);
                        });
                    }
                    var userList = groupBy(data, function (item) {
                        return [item.deptId];
                    });
                    var html = template("peoplePKHtml", {'data': userList});
                    rootDom.find("div[data-id='people']").html(html);
                    pageObj.renderICheck(rootDom);
                }
            });
        }
    },
    computerAndShowPkPeople: function (leftObj, rightObj) {
        //提交业务员
        var htmlDom;
        if (pageObj.leftNum <= pageObj.rightNum) {
            pageObj.leftNum++;
            htmlDom = $("#leftContent");
        } else {
            pageObj.rightNum++;
            htmlDom = $("#rightContent");
        }
        var html = template("peoplePKShow", {
            left: {
                id: leftObj.id,
                name: leftObj.name
            },
            right: {
                id: rightObj.id,
                name: rightObj.name
            }
        });
        htmlDom.append(html);
    },
    //页面事件绑定
    pageEventBind: function () {
        //提交业务员  选择下一位业务员
        $("#submitPeoplePK").on("click", function () {
            var rootDom = $("#peoplePKModal");
            var $radio = rootDom.find("input[type='radio']:checked");
            if ($radio.length === 0) {
                layer.msg(pageObj.pkType === 0 ? "请选择一位业务员" : "请选择一个部门");
                return;
            }
            var $span = $radio.parent().parent().find("span");
            pageObj.currentSelectPeopleId.push($radio.attr("data-id"));
            pageObj.currentSelectPeopleName.push($span.text());
            pageObj.selectedPeopleId.push($radio.attr("data-id"));
            $radio.parent().parent().remove();

            if (pageObj.isSelectReady) {
                pageObj.computerAndShowPkPeople({
                    id: pageObj.currentSelectPeopleId[0],
                    name: pageObj.currentSelectPeopleName[0]
                }, {
                    id: pageObj.currentSelectPeopleId[1],
                    name: pageObj.currentSelectPeopleName[1]
                });
                rootDom.modal('hide');
                pageObj.currentSelectPeopleId = [];
                pageObj.currentSelectPeopleName = [];
                pageObj.isSelectReady = false;
                $(this).text(pageObj.pkType === 0 ? "请选择下一位业务员" : "请选择下一个部门");
            } else {
                //选择下一位业务员
                $(this).text("确定");
                pageObj.isSelectReady = true;
            }
        });

        //搜索按钮
        $("#PKSearch").on("click", function () {
            pageObj.searchPKPeople();
        });

        //上传
        $("#backCustom").find("div[data-name='uploadImage']").on("click", function () {
            var $inputUploadFile = $("#inputUploadFile");
            $inputUploadFile.click();
        });

        //上传触发
        $("#inputUploadFile").on("change", function () {
            if (!$(this).val()) layer.msg("请选择需要上传的文件");
            var data = new FormData();
            data.append("file", document.getElementById("inputUploadFile").files[0]);

            var xhr = new XMLHttpRequest();
            xhr.withCredentials = true;

            xhr.addEventListener("readystatechange", function () {
                if (this.readyState === 4) {
                    var data = this.responseText;
                    $("input[name='backgroundImg']").val(data);
                    $("#backCustom").find("#largeBackImg").css("background-image", "url(" + data + ")");
                    $("#inputUploadFile").val('');
                }
            });

            xhr.open("POST", baseUrl + "/employeePerformancePK/upload");
            xhr.setRequestHeader("cache-control", "no-cache");

            xhr.send(data);
            $("#inputUploadFile").val('');
        });

        //保存 取消
        $(".savePerformance").on("click", function () {
            pageObj.submitForm();
        });
        $(".cancelPerformance").on("click", function () {
            closeCurrentTab();
        });

        $('input[type=radio][name=pkType]').on('ifChecked', function () {
            var $submitPeoplePK = $("#submitPeoplePK");
            if (this.value === '0') {
                pageObj.pkType = 0;
                $submitPeoplePK.text("请选择下一位业务员");
            } else if (this.value === '1') {
                pageObj.pkType = 1;
                $submitPeoplePK.text("请选择下一个部门");
            }
            pageObj.searchPKPeople();
            $("#leftContent").html('');
            $("#rightContent").html('');
            pageObj.leftNum = 0;
            pageObj.rightNum = 0;
            pageObj.selectedPeopleId = [];
            pageObj.currentSelectPeopleId = [];
            pageObj.currentSelectPeopleName = [];
        });
    },
    init: function () {
        var id = getQueryString("id");
        if (id) {//修改更新
            $.get(baseUrl + "/employeePerformancePK?id=" + id, function (data) {
                var item = data.data.data;
                var pkType = item.pkType;
                pageObj.pkType = pkType;
                $("input[name='pkType'][value=" + pkType + "]").iCheck("check");
                $("#submitPeoplePK").text(pkType === 0 ? "请选择下一位业务员" : "请选择下一个部门");
                //初始化表单数据
                Object.keys(item).forEach(function (key) {
                    $("#performanceForm input[name='" + key + "']").val(item[key])
                });

                //初始化pk组合
                var pkPeopleList = item.relates;
                pkPeopleList.forEach(function (item) {
                    var leftObj = {
                        id: item.leftEmployeeId,
                        name: item.leftEmployeeName
                    };
                    var rightObj = {
                        id: item.rightEmployeeId,
                        name: item.rightEmployeeName
                    };
                    pageObj.selectedPeopleId.push(item.leftEmployeeId);
                    pageObj.selectedPeopleId.push(item.rightEmployeeId);
                    pageObj.computerAndShowPkPeople(leftObj, rightObj);
                });

                var backgroundImg = item.backgroundImg;
                if (backgroundImg) {
                    $("#backCustom input[value='1']").iCheck("check");
                    $("#largeBackImg").css("background-image", "url(" + backgroundImg + ")");
                }
                pageObj.pageEventBind();
                pageObj.searchPKPeople();
            }, "json");
        } else {//保存新增
            pageObj.pageEventBind();
            pageObj.searchPKPeople();
        }
    },
    submitForm: function () {
        var $performanceForm = $("#performanceForm");
        if (!$performanceForm.valid()) return;

        var backCustomDom = $("#backCustom");
        $("div[data-name='uploadImage']").css("border", "");

        var $endDate = $("#endDate");
        var $startDate = $("#startDate");
        if (new Date($endDate.val()).getTime() - new Date($startDate.val()).getTime() <= 0) {
            layer.msg("结束时间必须比开始时间晚");
            return;
        }

        if ($("input[name='leftPeopleIds']").length === 0 && $("input[name='rightPeopleIds']").length === 0) {
            layer.msg("请选择pk组合");
            return;
        }

        //图片为默认设置还是自定义设置
        if (backCustomDom.find("input[name='backgroundImgRadio']:checked").val() !== '0' && !$("input[name='backgroundImg']").val()) {
            layer.msg("请上传自定义图片");
            $("#largeBackImg").css("border", "1px solid red");
            return;
        }

        if (backCustomDom.find("input[name='backgroundImgRadio']:checked").val() === '0') {
            $("input[name='backgroundImg']").val('');
        }

        $.ajax({
            type: "post",
            url: baseUrl + "/employeePerformancePK",
            data: $performanceForm.serializeArray(),
            success: function (data) {
                if (data.code === 1004) {
                    layer.alert(data.msg);
                    return;
                }
                var schemeList = "/employ/performance/performanceList";
                refrechPage(schemeList);
                closeCurrentTab();
            }
        })
    }
};


function groupBy(array, f) {
    var groups = {};
    array.forEach(function (o) {
        var group = JSON.stringify(f(o));
        groups[group] = groups[group] || [];
        groups[group].push(o);
    });
    return Object.keys(groups).map(function (group) {
        return groups[group];
    });
}