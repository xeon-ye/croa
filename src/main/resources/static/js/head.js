/**
 * Created by GZW
 */

//前端地址


// //后端地址，数据请求URL,请求JavaSrping Boot 的服务器地址
// var baseUrl = "http://localhost";
// //前端登录url ,没有登录则会自动跳转到该页面,如果不需要自动跳转到登录页面则可以给空字符串
// var LOGIN_URL = "http://localhost/login";
//后端地址，数据请求URL,请求JavaSrping Boot 的服务器地址
var baseUrl = "";
var previewUrl = "";
//前端登录url ,没有登录则会自动跳转到该页面,如果不需要自动跳转到登录页面则可以给空字符串
var LOGIN_URL = "/login";


function startModal(id) {
    if (id == undefined) {
        Ladda.create(document.querySelector(".ladda-button")).start();
    } else {
        Ladda.create(document.querySelector(id)).start();
    }
}

function getToken() {
    var token = localStorage.getItem("token");
    return token;
}

function setToken(val) {
    localStorage.setItem("token", val);
}


/**
 * 登录超时或者没有权限
 * @param data
 * @returns {boolean}
 */
function getResCode(data) {
    if (data.code == -1) {
        swal({
                title: "登录超时？",
                text: data.msg + ",您没有登录或会话已失效，请重新登录！",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "确定，进入登录页？",
                cancelButtonText: "取消，留在本页！",
                closeOnConfirm: false,
                reverseButtons: true //控制按钮反转
            },
            function (isConfirm) {
                if (isConfirm) {
                    if (LOGIN_URL != "")
                        top.location.href = LOGIN_URL;
                }
            });
        Ladda.stopAll();//隐藏加载按钮
        return true;
    }
    if (data.code == -2) {
        swal({
                title: "Token无效！",
                text: data.msg + ",请重新登录！",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "确定，进入登录页？",
                cancelButtonText: "取消，留在本页！",
                closeOnConfirm: false,
                reverseButtons: true //控制按钮反转
            },
            function (isConfirm) {
                if (isConfirm) {
                    if (LOGIN_URL != "")
                        top.location.href = LOGIN_URL;
                }
            });
        Ladda.stopAll();//隐藏加载按钮
        return true;
    }
    if (data.code == 403) {
        // swal("没有权限！", data.msg + ",抱歉！您没有该权限！", "warning");
        // return true;
        swal({
                title: "没有权限！",
                text: "抱歉！您没有该权限,请重新登录！",
                type: "warning",
                showCancelButton: true,
                confirmButtonColor: "#DD6B55",
                confirmButtonText: "确定，进入登录页？",
                cancelButtonText: "取消，留在本页！",
                closeOnConfirm: false,
                reverseButtons: true //控制按钮反转
            },
            function (isConfirm) {
                if (isConfirm) {
                    if (LOGIN_URL != "")
                        top.location.href = LOGIN_URL;
                }
            });
        Ladda.stopAll();//隐藏加载按钮
        return true;
    }
    if (data.code == 1001) {
        swal({
            title: "异常提示",
            text: data.msg,
        });
        Ladda.stopAll();//隐藏加载按钮
        return true;
    }
    // swal.close();
    //隐藏
    // $("#loadingModal").modal('hide');
    // layer.close(layerIndex);
    // Ladda.stopAll();
    return false;
}

function convert(data, format, rootName) {
    var rootArray = new Array();
    if (rootName == undefined)
        rootName = "品类目录";
    var root = {text: rootName, id: ""};
    var start = new Date().getTime();
    root["nodes"] = loadChilds(data, format);
    rootArray.push(root);
    return rootArray;
}

function loadChilds(data, format) {
    var nodeArray = new Array();
    for (var i in data) {
        if (typeof (data[i]) != "function") {
            nodeArray.push(loadChild(data[i], format));
        }
    }
    return nodeArray;
}

function loadChild(data, format) {
    var json = {};
    for (var key in data) {
        var value = data[key];
        var k = format[key] == undefined ? key : format[key];
        if (value != "" && value != null && value != undefined) {
            if (value instanceof Array) {
                if (value.length > 0) {
                    // 递归调用
                    json[k] = loadChilds(value, format);
                }
            } else if (value instanceof Object) {

                json[k] = loadChild(value, format);
            } else {
                json[k] = value;
            }
        }
    }
    return json;
}


function mergerData(gridName) {
    //得到显示到界面的id集合
    var ids = $("#" + gridName).getDataIDs();
    //当前显示多少条
    var length = ids.length;
    for (var i = 0; i < length; i++) {
        //从上到下获取一条信息
        var before = $("#" + gridName).jqGrid('getRowData', ids[i]);
        //定义合并行数
        var rowSpanTaxCount = 1;
        for (var j = 1; j <= length; j++) {
            //和上边的信息对比 如果值一样就合并行数+1 然后设置rowspan 让当前单元格隐藏
            var end = $("#" + gridName).jqGrid('getRowData', ids[j]);
            var bf = "", ed = "";
            $.each(arguments, function (ii, d) {
                if (ii > 0) {
                    bf += before[d];
                    ed += end[d];
                }
            });
            if (bf == ed) {
                rowSpanTaxCount++;
                $.each(arguments, function (ii, d) {
                    if (ii > 0) {
                        $("#" + gridName).setCell(ids[j], d, '', {display: 'none'});
                    }
                });
            } else {
                rowSpanTaxCount = 1;
                break;
            }
            $.each(arguments, function (ii, d) {
                if (ii > 0) {
                    $("#" + d + ids[i]).attr("rowspan", rowSpanTaxCount);
                }
            });
        }
    }
}

Views = {
//     /**
//      * 加载所有业务用户列表
//      * @param t
//      */
//     function loadAllYW(t) {
//     $.get(baseUrl + "/user/listByType/YW", function (data) {
//         $(data).each(function (i, d) {
//             console.log(d);
//             var value = $(t).attr("data-value");
//             var selected = value == d.id ? "selected=selected" : "";
//             $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
//         });
//     }, "json");
// }
    loadAllYW: function (attr, idVal) {
        var attribute = attr || 'salesmanName';
        layui.use(['form'], function () {
            Views.layuiForm = layui.form;
            $.ajax(
                {
                    url: baseUrl + "/user/listByType/YW",
                    type: "get",
                    dataType: "json",
                    success: function (data) {
                        if (data) {
                            var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
                            for (var i = 0; i < data.length; i++) {
                                var obj = data[i];
                                if (obj.id == idVal) {
                                    ele.append("<option selected='selected' value='${id}'>${name}</option>".replace("${id}", obj.id).replace("${name}", obj.name));
                                    continue;
                                }
                                ele.append("<option value='${id}'>${name}</option>".replace("${id}", obj.id).replace("${name}", obj.name));
                            }
                            Views.layuiForm.render();
                        }
                    }
                }
            );
        });
    },
    /**
     * 根据id值加载媒体
     * @param attr
     * @param idVal
     */
    loadParentMediaType: function (attr, idVal) {
        var attribute = attr || 'mType';
        $.ajax({
            url: baseUrl + "/mediaPlate/0",  //mediaType?parentId=0
            type: "get",
            dataType: "json",
            success: function (data) {
                if (data) {
                    var mTypeEle = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
                    for (var i = 0; i < data.length; i++) {
                        var mType = data[i];
                        if (mType.id == idVal) {
                            mTypeEle.append("<option selected='selected' value='${id}'>${name}</option>".replace("${id}", mType.id).replace("${name}", mType.name));
                            continue;
                        }
                        mTypeEle.append("<option value='${id}'>${name}</option>".replace("${id}", mType.id).replace("${name}", mType.name));
                    }
                }
            }
        });
    },
    //加载地区，带搜索功能,要引入layui
    loadDistrict: function (attr, idVal) {
        var attribute = attr || 'area';
        layui.use(['form'], function () {
            Views.layuiForm = layui.form;
            $.ajax({
                url: baseUrl + "/district/all",
                type: "get",
                dataType: "json",
                success: function (data) {
                    if (data) {
                        var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
                        for (var i = 0; i < data.length; i++) {
                            var obj = data[i];
                            if (obj.id == idVal) {
                                ele.append("<option selected='selected' value='${id}'>${name}</option>".replace("${id}", obj.id).replace("${name}", obj.name));
                                continue;
                            }
                            ele.append("<option value='${id}'>${name}</option>".replace("${id}", obj.id).replace("${name}", obj.name));
                        }
                        Views.layuiForm.render();
                    }
                }
            });
        });
    },
    //加载行业
    loadIndustry: function (attr, idVal) {
        var attribute = attr || 'industry';
        layui.use(['form'], function () {
            Views.layuiForm = layui.form;
            $.ajax(
                {
                    url: baseUrl + "/dict/listByTypeCode2",
                    data: {typeCode: "industry"},
                    type: "get",
                    dataType: "json",
                    success: function (data) {
                        if (data) {
                            var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
                            ele.append("<option value=''>请选择</option>");
                            for (var i = 0; i < data.length; i++) {
                                var obj = data[i];
                                if (obj.code == idVal) {
                                    ele.append("<option selected='selected' value='${id}'>${name}</option>".replace("${id}", obj.code).replace("${name}", obj.name));
                                    continue;
                                }
                                ele.append("<option value='${id}'>${name}</option>".replace("${id}", obj.code).replace("${name}", obj.name));
                            }
                            Views.layuiForm.render();
                        }
                    }
                }
            );
        });
    },
    //加载部门
    loadDept: function (attr, idVal, type) {
        var attribute = attr || 'dept';
        $.ajax(
            {
                url: baseUrl + "/dept/listByType",
                async: false,
                type: "post",
                data: {type: type},
                dataType: "json",
                success: function (data) {
                    if (data) {
                        var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
                        for (var i = 0; i < data.length; i++) {
                            var obj = data[i];
                            if (obj.id == idVal) {
                                ele.append("<option selected='selected' value='${id}'>${name}</option>".replace("${id}", obj.id).replace("${name}", obj.name));
                                continue;
                            }
                            ele.append("<option value='${id}'>${name}</option>".replace("${id}", obj.id).replace("${name}", obj.name));
                        }
                    }
                }
            }
        );
    },
    /**
     * 加载部门下的某种角色用户
     * @param currentDeptId
     * @param roleType
     * @param attr
     * @param idVal
     */
    loadDeptUser: function (deptId, roleType, attr, idVal) {
        var attribute = attr || 'users';
        layui.use(['form'], function () {
            Views.layuiForm = layui.form;
            var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
            $.ajax({
                    // url: baseUrl + "/statistics/deptUsers",
                    url: baseUrl + "/user/deptUsers",
                    type: "post",
                    data: {deptId: deptId, roleType: roleType},
                    async: false,
                    dataType: "json",
                    success: function (users) {
                        for (var i = 0; i < users.length; i++) {
                            // if (users[i].id == idVal) {
                            //     ele.append("<option value=" + users[i].id + " selected=selected>" + users[i].name + "</option>");
                            //     continue;
                            // }
                            ele.append("<option value=" + users[i].id + ">" + users[i].name + "</option>");
                            Views.layuiForm.render();
                        }
                    }
                }
            );
        });
    },

    listMJByMediaTypeId: function (mediaType, attr, idVal) {
        var attribute = attr || 'users';
        var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
        if (!idVal) {
            idVal = $(ele).attr("data-value");
            if (idVal == "${data-value}") {
                idVal = user.id;
            }
        }
        $.ajax(
            {
                url: baseUrl + "/user/listPastMJByMediaTypeId/" + mediaType,
                type: "get",
                async: false,
                dataType: "json",
                success: function (resData) {
                    var users = resData.data.listMJByMediaTypeId;
                    for (var i = 0; i < users.length; i++) {
                        if (users[i].id == idVal) {
                            ele.append("<option value=" + users[i].id + " selected=selected>" + users[i].name + "</option>");
                            continue;
                        }
                        ele.append("<option value=" + users[i].id + ">" + users[i].name + "</option>");
                    }
                }
            }
        );
    }
};

APPUtil = {
    /**
     * 获取用户
     * @param deptId
     */
    getUser: function (deptId) {
        $.ajax({
            url: baseUrl + "/user/queryUserByDeptId",
            data: {deptId: deptId},
            dataType: "json",
            success: function (resData) {
                // console.log(resData);
            }
        });
    }
};

/**
 * 稿件管理返回按钮事件；
 */
function goBackHtml() {
    window.history.back();
    return false;
}

/**
 * 审核页面返回按钮事件；
 */
function goBackProcess() {
    var returnType = getQueryString("returnType");
    //审批任务返回到审批管理页
    if (returnType == 3) {
        // refrechPage('/process/queryTask');
        triggerPageBtnClick("/process/queryTask", "dataSearch");
        closeCurrentTab();
    } else {
        refrechPage('/workbench/workbench');
        closeCurrentTab();
    }
    // window.location.href="/workbench/workbench";


}

/**
 * 加载地区
 * @param t
 */
function loadDistrict(t) {
    // alert($(t).attr("hideVal"));
    $(t).parent().addClass("layui-form");
    $(t).attr("lay-verify", "");
    $(t).attr("lay-search", "");
    layui.use(["form"], function () {
        $.get(baseUrl + "/district/all", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                // var value = media != undefined ? media[name] : "";
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载媒体类型
 * @param t
 */
function loadMediaType(t) {
    // alert($(t).attr("hideVal"));
    $.get(baseUrl + "/mediaType1/" + $("#mType").val(), function (data) {  ///mediaType/parentId/
        $(data).each(function (i, d) {
            var value = $(t).attr("data-value");
            // var value = media != undefined ? media[name] : "";
            var selected = value == d.id ? "selected=selected" : "";
            $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
        });
    }, "json");
}

/**
 * 加载行业类型
 * @param t
 */
// function loadIndustry(t) {
//     // alert($(t).attr("hideVal"));
//     $.get(baseUrl + "/industry/list?mediaTypeId=" + $("#mType").val(), function (data) {
//         $(data).each(function (i, d) {
//             // var name = $(t).attr("name");
//             // var value = media != undefined ? media[name] : "";
//             var value = $(t).attr("data-value");
//             var selected = value == d.id ? "selected=selected" : "";
//             $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
//         });
//     }, "json");
// }

/**
 * 加载所有用户
 * @param t
 */
function loadAllUser(t) {
    $.get(baseUrl + "/user/list", function (data) {
        $(data).each(function (i, d) {
            var value = $(t).attr("data-value");
            var selected = value == d.id ? "selected=selected" : "";
            $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
        });
    }, "json");
}

/**
 * 加载所有公司名称
 * @param t
 */
function loadAllCompany(t) {
    $.get(baseUrl + "/dept/listJTAllCompany", null, function (data) {
        if (data.code == 200 && data.data.result) {
            $(data.data.result).each(function (i, d) {
                // var value = $(t).attr("data-value");
                // var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.code + "' data='" + d.id + "'>" + d.name + "</option>");
            });
        }
    }, "json");
}

function loadAllCompany1(t) {
    $.get(baseUrl + "/dept/listJTAllCompany", null, function (data) {
        $(t).append("<option value=''>全部</option>");
        if (data.code == 200 && data.data.result) {
            $(data.data.result).each(function (i, d) {
                // var value = $(t).attr("data-value");
                // var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.code + "' data='" + d.id + "'>" + d.name + "</option>");
            });
        }
    }, "json");
}

/**
 * 加载所有用户 带搜索框
 * @param t
 */
function loadAllUserSearch(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/list", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

///只看自己本公司员工
function loadAllUserSearch3(t,id) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listUser", function (data) {
            $(data).each(function (i, d) {
                if (id==null){
                    var value = $(t).attr("data-value");
                    var selected = value == d.id ? "selected=selected" : "";
                    $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
                }else {
                    var selected = id == d.id ? "selected=selected" : "";
                    $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
                }

            });
            layui.form.render();
        }, "json");
    });
}

function loadAllUserSearch1(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listUser", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.name + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载所有媒介用户列表
 * @param t
 */
function loadAllMJ(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listByType/MJ", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载所有媒介用户列表
 * @param t
 */
function loadUserByType(t, type) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listByType/" + type, function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载拥有相同媒体板块的媒介用户列表
 * @param t
 */
function loadMJByMediaType(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listByMediaTypeUserId", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载当前用户拥有的板块
 * @param attribute
 */
function loadTypeByUserId(attribute) {
    $.ajax({
        url: baseUrl + "/mediaPlate/userId",  ///mediaType/listByUserId
        data: {"userId": user.id},
        type: "post",
        dataType: "json",
        success: function (data) {
            if (data) {
                var mTypeEle = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
                for (var i = 0; i < data.length; i++) {
                    var mType = data[i];
                    mTypeEle.append("<option value='${id}'>${name}</option>".replace("${id}", mType.id).replace("${name}", mType.name));
                }
            }
        }
    });
}

/**
 * 加载本公司的业务员，祥和加载所有业务员
 * @param t
 */
function listPastMedia(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listPastMedia", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载本公司的业务员，祥和加载所有业务员
 * @param t
 */
function listDeptUser(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listDeptUser", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.name + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载所有部门
 *
 */
function listAllDept(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.name + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}


/**
 * 加载公司人事行政人员
 * @param t
 */
function administrativePersonnel(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/administrativePersonnel", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value'" + d.id + "'" + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    })
}

/**
 * 加载所有业务用户列表
 * @param t
 */
function loadAllYW(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listByType/YW", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载角色类型
 */
function roleType(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/roleType", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.code + "'>" + d.name + "</option>");
            });
            layui.form.render();
            layui.form.on("select(mediaUserFilter)", function (mediaUserFilter) {
                // 更新隐藏域；
                CharacterName();
            });
        }, "json");
    });
}

/**
 * 审批任务
 * 部长可以查询到自己部门的所有员工
 */


/**
 * 加载所有业务用户列表
 * @param t
 */
function listPart(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listPart/YW", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

function listPartAll(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listPartAll/YW", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载财务部长用户
 * @param t
 */
function secretary(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/secretary", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 查询未移交的业务员
 * @param t
 */
function listPartAll2(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listPartAll2/YW", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}

/**
 * 加载所有业务用户列表
 * @param t
 */
function loadAllYWByCompanyCode(t) {
    layui.use(["form"], function () {
        $.get(baseUrl + "/user/listByTypeAndCompanyCode/YW", function (data) {
            $(data).each(function (i, d) {
                var value = $(t).attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
            });
            layui.form.render();
        }, "json");
    });
}
//项目总监
function projectDirector() {
    var value ;
    $.ajax({
        type:"get",
        url:baseUrl+"/sysConfig/getAllConfig",
        dataType:"json",
        async:false,
        success:function (data) {
            if (data && data.projectDirector){
                value=data.projectDirector.value;
            }
        }
    });
    return value;

}

function processCompanyCode() {
    var company;
    $.ajax({
        type:"get",
        url:baseUrl+"/sysConfig/getAllConfig",
        dataType:"json",
        async:false,
        success:function (data) {
                if (data && data.process){
                   company=data.process.value;
                }
        }
    });
    return company;
}

/**
 * 加载查询某种角色类型和职位类型 如媒介部长 下拉框对象,MJ,BZ
 * @param t
 */
function listByTypeAndCode(t, type, code) {
    $.get(baseUrl + "/user/listByTypeAndCode/" + type + "/" + code, function (data) {
        $(data).each(function (i, d) {
            var value = $(t).attr("data-value");
            var selected = value == d.id ? "selected=selected" : "";
            $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
        });
    }, "json");
}


/**
 * 加载部门
 * @param t
 */
function loadDepart(t) {
    // $.get(baseUrl+"/user/all", function (data) {
    //     $(data.list).each(function (i, d) {
    //         $(t).append("<option value='" + d.userId + "'>" + d.userName + "</option>");
    $(t).append("<option value='1'>业务部1部</option>");
    $(t).append("<option value='2'>媒介部</option>");
    //     });
    // }, "json");
}

/**
 * 加载资源类别
 * @param t
 */
function loadScreen(t, media) {
    $.get(baseUrl + "/mediaScreen/listByMediaTypeId/" + $("#mType").val(), function (data) {
        $(data).each(function (i, d) {
            var value = $(t).attr("data-value");
            var selected = value == d.id ? "selected=selected" : "";
            $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
        });
    }, "json");
}

/**
 * 加载媒体名称
 * @param t
 */
function loadMediaName(t, media) {
    $.get(baseUrl + "/mediaName/list", {mediaTypeId: $("#mType").val()}, function (data) {
        $(data).each(function (i, d) {
            var value = $(t).attr("data-value");
            var selected = value == d.id ? "selected=selected" : "";
            $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
        });
    }, "json");
}


/**
 * 按版块类型加载所有媒体供应商
 * @param t
 * */
function loadSupplier(t) {
    // $(t).parent().addClass("layui-form");
    // $(t).attr("lay-verify", "");
    // $(t).attr("lay-search", "");
    // layui.use(["form"], function () {
    //     $.get(baseUrl + "/supplier/list", {mediaTypeId: $("#mType").val()}, function (data) {
    //         $(data).each(function (i, d) {
    //             var value = $(t).attr("data-value");
    //             var selected = value == d.id ? "selected=selected" : "";
    //             $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "(" + d.contactor + ")" + "</option>");
    //             // 初始化；
    //             layui.form.render();
    //         });
    //     }, "json");
    // });
    var id = $(t).attr("data-value");
    id = id == "${data-value}" ? "" : id;
    var text = $(t).attr("data-text");
    text = text == "${data-text}" ? "" : text;
    if (id.length < 1 || text.length < 1) {
        id = "";
        text = "";
    }
    var options = {
        url: "/supplier/listMediaSupplier",      // 数据接口url
        size: 30, // 每次加载的数据条数
        value: "id",  // 下拉框value字段名称
        name: ["name", "contactor"], // 下拉框显示字段名称
        selected: [{id: id, text: text}],  // 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
        params: {mediaTypeId: $("#mType").val()},
        placeholder: "请选择供应商"
    };
    ajaxSelect2($(t), options);
}

/**
 * ajax获取select2下拉框数据（带鼠标滚动分页）
 * @param obj 下拉框对象；
 * @param options 选项，包含如下字段：
 * url 数据接口url
 * size 每次加载的数据条数
 * name 下拉框显示字段名称
 * value 下拉框value字段名称
 * placeholder 默认显示的文字
 * selected 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
 * formatResult 返回结果回调函数，可以在该回调中，自定义下拉框数据的显示样式，比如：加入图片等
 * templateSelection 选中项回调，该参数必须与formatResult参数搭配使用
 * 注意点1 : 后端接口需返回 data（具体数据）和 total（总页数）两个字段
 * 注意点2 : 两个自定义的回调函数中，必须要把处理结果return回来，如果没有传入formatResult参数，则采用默认的显示样式
 */
function ajaxSelect2(obj, options, formatResult, formatSelected) {
    var value = options["value"];
    var name = options["name"];
    var flag = (typeof formatResult === "function") ? true : false;
    var select2Option = {
        language: "zh-CN",
        allowClear: true,
        placeholder: options["placeholder"] || "请选择",
        ajax: {
            url: options["url"],
            type: "post",
            dataType: "json",
            delay: 250,
            data: function (params) {
                var optionParams = options["params"];
                // 搜索框内输入的内容
                optionParams.name = params.term;
                // 当前页
                optionParams.page = params.page || 1;
                // 每页显示多少条记录，默认10条
                optionParams.size = options["size"] || 10;
                // 传递到后端的参数
                return optionParams;
            },
            cache: true,
            processResults: function (res, params) {
                params.page = params.page || 1;
                var cbData = [];
                if (flag) {
                    cbData = res.list;
                } else {
                    if (res.pages >= params.page) {
                        var data = res.list;
                        var len = data.length;
                        var text;
                        for (var i = 0; i < len; i++) {
                            text = data[i][name[0]];
                            for (var j = 1; j < name.length; j++) {
                                text += "(" + data[i][name[j]] + ")"
                            }
                            cbData.push({"id": data[i][value], "text": text});
                        }
                    }
                }
                return {
                    results: cbData,
                    pagination: {
                        more: params.page < res.total
                    }
                };
            }
        },
        escapeMarkup: function (markup) {
            // 字符转义处理
            return markup;
        },
        // 最少输入N个字符才开始检索，如果想在点击下拉框时加载数据，请设置为 0
        minimumInputLength: 0
    };
    if (flag) {
        select2Option.templateResult = formatResult;
        select2Option.templateSelection = formatSelected;
    }
    var $select = obj;
    $select.select2(select2Option);
    if (!flag) {
        // 默认选中项设置
        var html = '';
        var values = [];
        var selected = options['selected'];
        if (selected) {
            $.each(selected, function (index, item) {
                values.push(item.id);
                html += '<option value="' + item.id + '">' + item.text + '</option>';
            });
            $select.append(html);
            $select.val(values).trigger('change');
        }
    }
}

function selSupplier(t) {
    $("#supplierName").val($(t).find("option:selected").text());
}

/**
 * 加载所有媒体板块
 * @param t
 * */
function loadMediaModeType(t) {
    $.get(baseUrl + "/mediaPlate/userId", function (data) {  //mediaType/userId
        $(data).each(function (i, d) {
            var value = $(t).attr("data-value");
            var selected = value == d.id ? "selected=selected" : "";
            $(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
        });
    }, "json");
}

/**
 * 加载角色
 */
function loadRoles(t) {
    $.get(baseUrl + "/role/list", function (data) {
        $(data).each(function (i, d) {
            $(t).append("<option value='" + d.id + "'>" + d.name + "</option>");
        });
    }, "json");
}

/**
 * 加载公司
 */
function loadCompany(t) {
    $.get(baseUrl + "/dict/listByTypeCode2?typeCode=COMPANY_CODE", function (data) {
        $(t).append("<option value=''>请选择公司</option>")
        $(data).each(function (i, d) {
            $(t).append("<option value='" + d.code + "'>" + d.name + "</option>");
        });
    }, "json");
}

/**
 * 当前菜单颜色高亮；
 */
function setMenuStyle(obj) {
    // 先查询是否已存在高亮的菜单；
    var element = $(".currentMenu");
    // 移除父菜单高亮；
    var parentMenu = element.closest(".menuLi");
    // 设置父菜单样式；
    parentMenu.find("a").css("color", "#A7B1C2");
    if (element) {
        element.css("color", "#A7B1C2");
        element.removeClass("currentMenu");
    }

    // 当前菜单高亮；
    var currentMenu = $(obj);
    currentMenu.addClass("currentMenu");
    currentMenu.css("color", "#FFFFFF");
    // 设置父菜单高亮
    setMenuParentStyle(currentMenu, "#FFFFFF");
}

//递归设置父菜单样式
function setMenuParentStyle(element, color) {
    var parentMenu = element.parent();
    // 设置父菜单样式；
    parentMenu.children("a").css("color", color);
    if(!$(parentMenu).hasClass("menuLi")){
        setMenuParentStyle(parentMenu, color);
    }
}

/**
 * 与标签对应；
 */
function matchTab(obj) {
    // 获取标签的链接；
    var currentUrl = $(obj).attr("data-id");
    var currentMenu;
    var parentMenu;
    // 先清除所有一级和二级菜单的展开样式；
    $(".J_menuItem").each(function () {
        // 二级菜单；
        parentMenu = $(this).closest("ul");
        if (parentMenu.hasClass("in")) {
            parentMenu.removeClass("in");
        }
        // 一级菜单；
        if (parentMenu.parent().hasClass("active")) {
            parentMenu.parent().removeClass("active");
            parentMenu.parent().children("a").css("color", "#A7B1C2");
        }
    });

    // 开始设置属性；
    $(".J_menuItem").each(function () {
        currentMenu = $(this);
        // 判断是否为当前标签的菜单；
        if (currentMenu.attr("href") == currentUrl) {
            currentMenu.addClass("currentMenu");
            currentMenu.css("color", "#FFFFFF");

            // 判断当前菜单的级别；
            parentMenu = currentMenu.closest("ul");
            // 设置父菜单样式；
            if (parentMenu.hasClass("nav-second-level")) {
                parentMenu.parent().children().addClass("in")
                parentMenu.parent().addClass("active");
                parentMenu.parent().children("a").css("color", "#FFFFFF");
            } else {
                currentMenu.parent().children().addClass("in")
                currentMenu.parent().addClass("active");
            }
        } else {
            currentMenu.css("color", "#A7B1C2");
            currentMenu.removeClass("currentMenu");
        }
    });
}

$(document).ready(function () {
    try {
        $.jgrid.defaults.styleUI = 'Bootstrap';
    } catch (e) {
        console.log(e);
    }
    var width = window.screen.width;
    //忽略的url
    var ignoreUrls = ["/login", "/biz/statistics/business_manager_statistics",
        "/mediauser/statistics/media_user_manager_statistics", "/crm/statistics/cust_statistics",
        "/mediauser/statistics/media_user_statistics", "/biz/statistics/business_statistics"];
    var b = false;
    for (var i = 0; i < ignoreUrls.length; i++) {
        if ((window.location.href.indexOf(ignoreUrls[i]) != -1)) {
            b = true;
        }
    }
    if (!b && width < 1500) {
        $("body").css({zoom: 0.8});
    }
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
});

// 关闭当前标签；
function closeCurrentTab() {
    var currrentTab = $(".J_menuTab.active", window.top.document);
    // 如果是在系统内则关闭Tab，否则回退；
    if (currrentTab.length > 0) {
        currrentTab.find(".fa.fa-times-circle").click();
    } else {
        history.back();
    }
}

function filePreview(t) {
    var index = layer.msg('系统加载中', {time: 10000, shade: [0.7, '#393D49']});
    var filePath = $(t).attr("data-id").trim();
    $.ajax({
        url: "http://47.99.68.8:39000/pdfConvert?inputFile=" + filePath,
        type: "post",
        jsonp: 'callback',//自定义
        jsonpClaaback: "success_jsonpCallback",//用户定义的callback函数，没有定义的话会jQuery会自动生成以jQuery开头的函数
        success: function (data) {
            if (index)
                layer.close(index);
            var shade = $('.layui-layer-shade');
            if (shade)
                shade.remove();

            if (data.msg) {
                swal({
                    type: "error",
                    title: data.msg
                });
                return;
            }
            // $("#docPreviewModal iframe").attr('src', baseUrl + data.data);
            // $("#docPreviewModal").modal('show');
            window.open(baseUrl + data.data);
        },
        dataType: "jsonp"
    });
}

function openImage(e, modalId) {
    imgSrc = $(e).attr("src");
    imgAlt = $(e).attr("alt");
    imgSrc = imgSrc.replace(/\"/g, "");
    $("#imgDiv").attr("alt", imgAlt);
    $("#imgDiv").attr("title", imgAlt);
    $("#imgDiv").attr("src", imgSrc);
    $("#" + modalId).modal("toggle");
}

function closeImg(modalId) {
    $("#" + modalId).modal("hide");
}

Array.prototype.indexOf = function (val) {
    for (var i = 0; i < this.length; i++) {
        if (this[i] === val) return i;
    }
    return -1;
};
Array.prototype.remove = function (val) {
    var index = this.indexOf(val);
    if (index > -1) {
        this.splice(index, 1);
    }
};

/**
 * 去除参数前后空格
 * @param obj
 * @returns {*}
 */
function removeBlank(obj){
    if (obj) {
        for (var k in obj) {
            if (typeof (obj[k]) == "string") {
                //提交参数时去除前后空格
                obj[k] = obj[k].replace(/(^\s*)|(\s*$)/g, "");
            }
        }
    }
    return obj;
}