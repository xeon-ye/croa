$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    planConfigObj.init();//初始化用户部门数据
    planConfigObj.reflushPage();//刷新页面
});

//维护系统配置
var planConfigObj = {
    sysConfigMap: null,
    //缓存获取数据库的数据，用于生成下拉列表选项或者表格列表
    tableData:{user_id:[], dept_id:[]},
    chooseData:{userId:[], deptId:[]},//已选数据
    userJson:{id:null,configTitle:"每日计划总结",configType:"user_id",dataType:"list",configKey:"userPlanExcudeUser",configPattern:",",configValue:"",configDesc:"由于每日计划总结排名、人效比或者录入，有时候需要排除某些部门下人员进行，所以代码兼容配置参数功能",state:null},
    deptJson:{id:null,configTitle:"每日计划总结",configType:"dept_id",dataType:"list",configKey:"userPlanExcudeDept",configPattern:",",configValue:"",configDesc:"由于每日计划总结排名、人效比或者录入，有时候需要排除某些部门进行，所以代码兼容配置参数功能",state:null},
    listTableData:function (configType) {
        if(planConfigObj.tableData[configType] && planConfigObj.tableData[configType].length < 1){
           requestData({configType: configType}, "/sysConfig/listTableData", "get", "json", false, function (data) {
               planConfigObj.tableData[configType] = data;
           });
        }
    },
    renderListValue:function (tbodyId, chooseKey, list, configValue) {
        configValue = configValue || [];
        var html = "";
        if(list && list.length > 0){
            $.each(list, function (i, data) {
                html += "<tr>\n" +
                    "        <td>\n" +
                    "            <input data-chooseKey='"+chooseKey+"' type=\"checkbox\"  "+(configValue.contains(data.value) ? "checked" : "")+" lay-skin=\"primary\" value='"+data.value+"' lay-filter='itemCheckBox'>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(i+1)+"\">\n" +
                    "            <div style=\"width: 98%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                    "                "+(i+1)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+data.value+"\">\n" +
                    "            <div class='listContent' style=\"width: 98%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                    "                "+data.value+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+data.name+"\">\n" +
                    "            <div style=\"width: 98%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                    "                "+data.name+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "    </tr>";
            });
        }
        $(tbodyId).html(html);
        //渲染数据
        layui.use('form', function(){
            var form = layui.form;
            form.render('checkbox');
        });
    },
    init:function () {
        //加载用户数据
        planConfigObj.listTableData("user_id");
        //加载部门数据
        planConfigObj.listTableData("dept_id");
    },
    reflushPage:function (opKey) {
        if(opKey){
            planConfigObj.chooseData[opKey] = [];
        }else {
            planConfigObj.chooseData = {userId:[], deptId:[]};//已选数据重置
        }

        if(!opKey || "userId" == opKey){
            planConfigObj.userJson = {id:null,configTitle:"每日计划总结",configType:"user_id",dataType:"list",configKey:"userPlanExcudeUser",configPattern:",",configValue:"",configDesc:"由于每日计划总结排名、人效比或者录入，有时候需要排除某些部门下人员进行，所以代码兼容配置参数功能",state:null};
            //请求配置的用户数据
            requestData({configKey:"userPlanExcudeUser"}, "/sysConfig/getOneConfigByKey", "get", "json", false, function (data) {
                //如果有值
                if(data){
                    for(var key in planConfigObj.userJson){
                        planConfigObj.userJson[key] = data[key];
                    }
                    if(data.configValue) {
                        var configValueArr = data.configValue.split(data.configPattern);
                        if (configValueArr && configValueArr.length > 0) {
                            for (var i = 0; i < configValueArr.length; i++) {
                                planConfigObj.chooseData.userId.push(configValueArr[i]);
                            }
                        }
                    }
                }
            });

            //如果有配置用户，则按钮变成修改，否则新增
            if(planConfigObj.userJson.id){
                $("#userAddBtn").css("display", "none");
                $("#userUpdateBtn").css("display", "inline-block");
                $("#userDelBtn").css("display", "inline-block");
                if(planConfigObj.userJson.state == 1){
                    $("#userConfigState").text("已禁用");
                    $("#userConfigState").css("color", "red");
                    $("#userEnableBtn").css("display", "inline-block");
                    $("#userDisableBtn").css("display", "none");
                }else {
                    $("#userConfigState").text("已启用");
                    $("#userConfigState").css("color", "green");
                    $("#userEnableBtn").css("display", "none");
                    $("#userDisableBtn").css("display", "inline-block");
                }
            }else {
                $("#userAddBtn").css("display", "inline-block");
                $("#userUpdateBtn").css("display", "none");
                $("#userDelBtn").css("display", "none");
                $("#userDisableBtn").css("display", "none");
                $("#userEnableBtn").css("display", "none");
                $("#userConfigState").text("未配置");
                $("#userConfigState").css("color", "#C9C9C9");
            }
            planConfigObj.renderListValue("#userListBody", "userId", planConfigObj.tableData.user_id, planConfigObj.chooseData.userId);
        }

        if(!opKey || "deptId" == opKey){
            planConfigObj.deptJson = {id:null,configTitle:"每日计划总结",configType:"dept_id",dataType:"list",configKey:"userPlanExcudeDept",configPattern:",",configValue:"",configDesc:"由于每日计划总结排名、人效比或者录入，有时候需要排除某些部门进行，所以代码兼容配置参数功能",state:null};

            //请求配置的部门数据
            requestData({configKey:"userPlanExcudeDept"}, "/sysConfig/getOneConfigByKey", "get", "json", false, function (data) {
                //如果有值
                if(data){
                    for(var key in planConfigObj.deptJson){
                        planConfigObj.deptJson[key] = data[key];
                    }
                    if(data.configValue) {
                        var configValueArr = data.configValue.split(data.configPattern);
                        if (configValueArr && configValueArr.length > 0) {
                            for (var i = 0; i < configValueArr.length; i++) {
                                planConfigObj.chooseData.deptId.push(configValueArr[i]);
                            }
                        }
                    }
                }
            });

            //如果有配置部门，则按钮变成修改，否则新增
            if(planConfigObj.deptJson.id){
                $("#deptAddBtn").css("display", "none");
                $("#deptUpdateBtn").css("display", "inline-block");
                $("#deptDelBtn").css("display", "inline-block");
                if(planConfigObj.deptJson.state == 1){
                    $("#deptConfigState").text("已禁用");
                    $("#deptConfigState").css("color", "red");
                    $("#deptEnableBtn").css("display", "inline-block");
                    $("#deptDisableBtn").css("display", "none");
                }else {
                    $("#deptConfigState").text("已启用");
                    $("#deptConfigState").css("color", "green");
                    $("#deptDisableBtn").css("display", "inline-block");
                    $("#deptEnableBtn").css("display", "none");
                }
            }else {
                $("#deptAddBtn").css("display", "inline-block");
                $("#deptUpdateBtn").css("display", "none");
                $("#deptDelBtn").css("display", "none");
                $("#deptDisableBtn").css("display", "none");
                $("#deptEnableBtn").css("display", "none");
                $("#deptConfigState").text("未配置");
                $("#deptConfigState").css("color", "#C9C9C9");
            }
            planConfigObj.renderListValue("#deptListBody", "deptId", planConfigObj.tableData.dept_id, planConfigObj.chooseData.deptId);
        }
    },
    searchConfig:function (t, tableKey) {
        var tableList = planConfigObj.tableData[tableKey];
        var val = $(t).closest(".m-b").find("input").val();
        var result = [];
        if(val){
            $.each(tableList, function (x, item) {
                //当包含时
                if(item.name.match(new RegExp(val, 'g')) != null){
                    result.push(item);
                }
            });
        }else {
            result = tableList;
        }

        //渲染数据
        if("user_id" == tableKey){
            planConfigObj.renderListValue("#userListBody", "userId", result, planConfigObj.chooseData.userId);
        }else {
            planConfigObj.renderListValue("#deptListBody", "deptId", result, planConfigObj.chooseData.deptId);
        }
    },
    save: function (t, url, key, chooseKey) {
        planConfigObj[key].configValue = planConfigObj.chooseData[chooseKey].join(",");
        if(!planConfigObj[key].configValue){
            layer.msg("请选中记录操作！", {time: 2000, icon: 5});
            return;
        }
        startModal("#" + $(t).attr("id"));
        requestData(JSON.stringify(planConfigObj[key]), url, "post", "json", true, function (data) {
            planConfigObj.validResData(data, "计划总结设置操作成功！", chooseKey);
        },true);
        Ladda.stopAll();
    },
    enable: function (t, key) {
        var opKey = key == "deptJson" ? "deptId" : "userId";
        startModal("#" + $(t).attr("id"));
        requestData({id:planConfigObj[key].id}, "/sysConfig/enable", "get", "json", true, function (data) {
            planConfigObj.validResData(data, "计划总结设置启用成功！", opKey);
        });
        Ladda.stopAll();
    },
    disable:function (t, key) {
        var opKey = key == "deptJson" ? "deptId" : "userId";
        startModal("#" + $(t).attr("id"));
        requestData({id:planConfigObj[key].id}, "/sysConfig/disable", "get", "json", true, function (data) {
            planConfigObj.validResData(data, "计划总结设置禁用成功！", opKey);
        });
        Ladda.stopAll();
    },
    del:function (t, key) {
        var opKey = key == "deptJson" ? "deptId" : "userId";
        startModal("#" + $(t).attr("id"));
        requestData({id:planConfigObj[key].id}, "/sysConfig/del", "get", "json", true, function (data) {
            planConfigObj.validResData(data, "计划总结设置删除成功！", opKey);
        });
        Ladda.stopAll();
    },
    validResData: function (data, message, opKey) {
        if (data.code == 200) {
            layer.msg(message, {time: 2000, icon: 6});
            planConfigObj.reflushPage(opKey);
        } else {
            layer.msg(data.msg, {time: 3000, icon: 5});
        }
    }
}

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,dataType,async,callBackFun, contentType) {
    var param = {
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun,
        error: function () {
            Ladda.stopAll();
        }
    };
    if(contentType){
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
}