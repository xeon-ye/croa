$(function () {
    searchForm.init(); //初始化条件
    searchForm.search();
});

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,dataType,async,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun
    });
}

//查询区域
var searchForm = {
    maxLength: 10, //列表最大值，用于导出循环次数
    exportMap: {title:"",rxbDeptBefore:[],rxbDeptAfter:[],yxkhDeptBefore:[],yxkhDeptAfter:[],rxbUserBefore:[],rxbUserAfter:[],yxkhUserBefore:[],yxkhUserAfter:[]},
    deptRxbList: [], //部门人效比列表
    deptYxkhList: [], //部门有效客户列表
    userRxbList: [], //用户人效比列表
    userYxKhList: [], //用户有效客户列表
    init: function () {
        searchForm.loadAllCompany("#companyCode");
        layui.use('form', function(){
            layui.form.render('select');//layui重新渲染下拉列表
        });
    },
    showDate: function(val){
        if(val == 3){
            $("#dateFormGroup").show();
        }else{
            $("#dateFormGroup").hide();
            searchForm.search();
        }
    },
    loadAllCompany:function(id){
        var company = user.companyCode;
        if(company == "JT"){
            $.ajax({
                url: baseUrl + "/dept/listAllCompany",
                type: "get",
                data: null,
                dataType: "json",
                async: false,
                success: function (data) {
                    var html = "<option value='' data=''>请选择公司</option>";
                    if(data.code == 200 && data.data.result){
                        var resData = data.data.result;
                        $(resData).each(function (i, d) {
                            html += "<option value='" + d.code + "' data='" + d.id + "'>" + d.name + "</option>";
                        });
                    }
                    $(id).html(html);
                }
            });
        }else{
            $(id).append("<option value='" + user.companyCode + "' data='" + user.deptId + "'>" + user.dept.companyCodeName + "</option>");
        }

    },
    getFileName:function (val, startTime, endTime) {
        var fileName = "计划排名";
        if(val == 3){
            fileName += "("+ (startTime || "?") + "-" + (endTime || "?") + ")";
        }else if(val == 2){
            fileName += "(本月)"
        }else if(val == 1){
            fileName += "(本周)"
        }else {
            fileName += "(当天)"
        }
        return fileName;
    },
    search:function () {
        var searchFormData = $("#queryForm").serializeJson();
        //初始化
        searchForm.exportMap.title = "";
        searchForm.exportMap.rxbDeptBefore = [];
        searchForm.exportMap.rxbDeptAfter = [];
        searchForm.exportMap.yxkhDeptBefore = [];
        searchForm.exportMap.yxkhDeptAfter = [];
        searchForm.exportMap.rxbUserBefore = [];
        searchForm.exportMap.rxbUserAfter = [];
        searchForm.exportMap.yxkhUserBefore = [];
        searchForm.exportMap.yxkhUserAfter = [];
        searchForm.deptRxbList = [];
        searchForm.deptYxkhList = [];
        searchForm.userRxbList = [];
        searchForm.userYxKhList = [];
        //初始化排名数量
        $(".rinkNumWrap").find("input").each(function (i, input) {
            $(input).val($(input).attr("data-value"));
        });

        //获取部门排名
        requestData(searchFormData, "/userPlan/listDeptSummary", "post", "json", true, function (data) {
            if(data.code==200){
                var result = data.data.result;
                searchForm.deptRxbList = result.rxbList;
                searchForm.deptYxkhList = result.yxkhList;
                //设置最大列表值
                if(searchForm.deptRxbList && searchForm.deptRxbList.length > searchForm.maxLength){
                    searchForm.maxLength = searchForm.deptRxbList.length;
                }
                if(searchForm.deptYxkhList && searchForm.deptYxkhList.length > searchForm.maxLength){
                    searchForm.maxLength = searchForm.deptYxkhList.length;
                }
                handlerList(result.rxbList, 3, true, $("#rxb").find(".deptBeforeThree"), searchForm.exportMap.rxbDeptBefore);//人效比前三
                handlerList(result.rxbList, 3, false, $("#rxb").find(".deptAfterThree"), searchForm.exportMap.rxbDeptAfter);//人效比后三
                handlerList(result.yxkhList, 3, true, $("#rjyxkh").find(".deptBeforeThree"), searchForm.exportMap.yxkhDeptBefore);//有效客户排名前三
                handlerList(result.yxkhList, 3, false, $("#rjyxkh").find(".deptAfterThree"),searchForm.exportMap.yxkhDeptAfter);//有效客户排名后三
            }else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
        //获取人员排名
        requestData(searchFormData, "/userPlan/listUserSummaryRanking", "post", "json", true, function (data) {
            if(data.code==200){
                var result = data.data.result;
                searchForm.userRxbList = result.rxbList;
                searchForm.userYxKhList = result.yxkhList;
                //设置最大列表值
                if(searchForm.userRxbList && searchForm.userRxbList.length > searchForm.maxLength){
                    searchForm.maxLength = searchForm.userRxbList.length;
                }
                if(searchForm.userYxKhList && searchForm.userYxKhList.length > searchForm.maxLength){
                    searchForm.maxLength = searchForm.userYxKhList.length;
                }
                handlerList(result.rxbList, 30, true, $("#rxb").find(".userBeforeTen"),searchForm.exportMap.rxbUserBefore);//人效比前三十
                handlerList(result.rxbList, 30, false, $("#rxb").find(".userAfterTen"),searchForm.exportMap.rxbUserAfter);//人效比后三十
                handlerList(result.yxkhList, 30, true, $("#rjyxkh").find(".userBeforeTen"),searchForm.exportMap.yxkhUserBefore);//有效客户排名前三十
                handlerList(result.yxkhList, 30, false, $("#rjyxkh").find(".userAfterTen"),searchForm.exportMap.yxkhUserAfter);//有效客户排名后三十
            }else {
                if(getResCode(data)){
                    return;
                }else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
            }
        });
    },
    export:function () {
        //封装导出标题
        searchForm.exportMap.title = searchForm.getFileName($("#timeQuantum").val(),$("#startDate").val(),$("#endDate").val());
        var filtContent = ",,,,"+searchForm.exportMap.title+"\n";
        filtContent += "人效比,,,,,有效客户比,,,\n";
        filtContent += "部门/前"+$("#rxb-deptBeforeThree").text()+",部门/后"+$("#rxb-deptAfterThree").text()+",个人/前"+$("#rxb-userBeforeTen").text()+"," +
            "个人/后"+$("#rxb-userAfterTen").text()+",,部门/前"+$("#rjyxkh-deptBeforeThree").text()+",部门/后"+$("#rjyxkh-deptAfterThree").text()+",个人/前" +
            ""+$("#rjyxkh-userBeforeTen").text()+",个人/后"+$("#rjyxkh-userAfterTen").text()+"\n";
        for(var i = 0; i < searchForm.maxLength; i++){
            var rxbDepDeforeName = i < searchForm.exportMap.rxbDeptBefore.length ? searchForm.exportMap.rxbDeptBefore[i] : "";
            var rxbDeptAfterName = i < searchForm.exportMap.rxbDeptAfter.length ? searchForm.exportMap.rxbDeptAfter[i] : "";
            var yxkhDeptBeforeName = i < searchForm.exportMap.yxkhDeptBefore.length ? searchForm.exportMap.yxkhDeptBefore[i] : "";
            var yxkhDeptAfterName = i < searchForm.exportMap.yxkhDeptAfter.length ? searchForm.exportMap.yxkhDeptAfter[i] : "";
            var rxbUserBeforeName = i < searchForm.exportMap.rxbUserBefore.length ? searchForm.exportMap.rxbUserBefore[i] : "";
            var rxbUserAfterName = i < searchForm.exportMap.rxbUserAfter.length ? searchForm.exportMap.rxbUserAfter[i] : "";
            var yxkhUserBeforeName = i < searchForm.exportMap.yxkhUserBefore.length ? searchForm.exportMap.yxkhUserBefore[i] : "";
            var yxkhUserAfterName = i < searchForm.exportMap.yxkhUserAfter.length ? searchForm.exportMap.yxkhUserAfter[i] : "";
            filtContent += rxbDepDeforeName+","+rxbDeptAfterName+","+rxbUserBeforeName+","+rxbUserAfterName+",,"+yxkhDeptBeforeName+","+yxkhDeptAfterName+","+yxkhUserBeforeName+","+yxkhUserAfterName+"\n";
        }
        var href = "data:text/csv;charset=utf-8,\ufeff" + filtContent;
        var aNode = document.createElement("a");
        aNode.setAttribute("href", href);
        var date = new Date().format("yyyy-MM-dd");
        aNode.setAttribute("download", "计划排名("+date+")" + ".csv");
        aNode.click();
        aNode.remove();
    },
    inputChange:function (t) {
        var listKey = $(t).attr("data-list") || "";
        var exportListKey = $(t).attr("data-exportList") || "";
        var isBefore = $(t).attr("data-isBefore") || "";
        var targetWrap = $(t).attr("data-targetWrap") || "";
        var target = $(t).attr("data-target") || "";
        var val = $(t).val() || "";
        val = isNaN(parseInt(val)) ? "" : parseInt(val);
        if(listKey && exportListKey && val && isBefore && targetWrap &&　target){
            $("#"+targetWrap+"-"+target).text(getChineseNum(val) || val);
            isBefore = isBefore == "true" ? true : false;
            searchForm.exportMap[exportListKey] = [];//清空数据
            handlerList(searchForm[listKey], val, isBefore, $("#"+targetWrap).find("."+target),searchForm.exportMap[exportListKey]);//数据处理
        }
    }
};

/**
 * 处理List集合
 * @param sourceList 集合
 * @param length 获取个数
 * @param isBefore 获取前后标识，true排名前，false排名后
 * @param $target 目标ID
 * @param 导出缓存数组
 */
function handlerList(sourceList, length, isBefore, $target, rankingList) {
    var html = "";
    if(sourceList && sourceList.length > 0){
        var targetList = [];
        //拷贝源数据
        $.each(sourceList, function (k, item) {
            targetList.push(item);
        });

        //如果返回列表总数大于参与排名人数，则前后排名不一致，如果列表总数小于参与排名人数，则前后排名一致
        if(targetList.length > length){
            if(isBefore){
                targetList = targetList.slice(0, length); //截取列表前length个元素
            }else {
                targetList = targetList.slice(targetList.length - length, targetList.length);//截取列表后length个元素
            }
        }
        var fontColor = "";
        if(!isBefore){
            targetList = targetList.reverse();
            fontColor = "fontColor";
        }
        $.each(targetList, function (i, info) {
            //如果集团用户并且没有选择公司，则显示公司
            if(user.companyCode == "JT" && !$("#companyCode").val()){
                rankingList.push(info.name + "-" + info.companyCodeName);
                html += "<div class=\"rankingContent "+fontColor+"\" title='"+info.name+"("+info.companyCodeName+")'>"+(i+1)+"、"+info.name+"("+info.companyCodeName+")</div>";
            }else {
                rankingList.push(info.name);
                html += "<div class=\"rankingContent "+fontColor+"\" title='"+info.name+"'>"+(i+1)+"、"+info.name+"</div>";
            }
        });
    }
    $target.html(html);
}

/**
 * 将数字转换成中文数字，处理到万级别的
 * @param digit 数字
 */
function getChineseNum(digit) {
    //完成将 toChineseNum， 可以将数字转换成中文大写的表示，处理到万级别，例如 toChineseNum(12345)，返回 一万二千三百四十五。
    var changeNum = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九']; //changeNum[0] = "零"
    var unit = ["", "十", "百", "千", "万"];
    digit = parseInt(digit);
    var getWan = function (temp) {
        var strArr = temp.toString().split("").reverse();
        var newNum = "";
        for (var i = 0; i < strArr.length; i++) {
            newNum = (i == 0 && strArr[i] == 0 ? "" : (i > 0 && strArr[i] == 0 && strArr[i - 1] == 0 ? "" : ((parseInt(temp) >= 10 && parseInt(temp) < 20 && i == 1) ? "" : changeNum[strArr[i]]) + (strArr[i] == 0 ? unit[0] : unit[i]))) + newNum;
        }
        return newNum;
    }
    var overWan = Math.floor(digit / 10000);
    var noWan = digit % 10000;
    if (noWan.toString().length < 4) noWan = "0" + noWan;
    return overWan ? getWan(overWan) + "万" + getWan(noWan) : getWan(digit);
}

