var level = ['A+', 'A', 'A-', 'B+', 'B', 'B-', 'C+', 'C'];
var schemeType = ["月度计划", "季度计划", "年中计划", "年终计划"];
var treeSetting;
var OKRTreeSetting;
var treeObj;
$(function () {
    treeSetting = {
        view: {
            selectedMulti: false,
            showLine:true,
            showIcon: true
        },
        check: {
            enable: false,   //true / false 分别表示 显示 / 不显示 复选框或单选框
            autoCheckTrigger: false,   //true / false 分别表示 触发 / 不触发 事件回调函数
            chkStyle: "checkbox",   //勾选框类型(checkbox 或 radio）
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback : {
            onClick:pageObj.onCheck, //复选框点击事件
        },
    };

    OKRTreeSetting = {
        view: {
            selectedMulti: false,
            showLine:true,
            showIcon: true
        },
        check: {
            enable: false,   //true / false 分别表示 显示 / 不显示 复选框或单选框
            autoCheckTrigger: false,   //true / false 分别表示 触发 / 不触发 事件回调函数
            chkStyle: "checkbox",   //勾选框类型(checkbox 或 radio）
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback : {
            onClick:OKRPageObj.onCheck, //复选框点击事件
        },
    };

    //加载tab页
    layui.use('element', function(){
        var element = layui.element;
        element.on('tab(docDemoTabBrief)', function(data){
            $("#tabIndex").val(data.index);
            //默认kpl绩效考核
            commonObj.tabChange(data.index);
        });
    });

    //默认kpl绩效方案
    if(getQueryString("index")!=null && getQueryString("index")!="" && getQueryString("index")!=undefined){
        var tabIndex = parseInt(getQueryString("index"));
        $(".layui-tab ul").find("li:eq("+tabIndex+")").addClass("layui-this").siblings().removeClass("layui-this");
        commonObj.tabChange(tabIndex);
    }else {
        commonObj.initPage();
    }

    //kpl跳转新增页面
    $("#schAdd").on("click", function () {
        pageObj.addScheme();
    });

    //kpl拷贝
    $("#copyScheme").on("click",function () {
        pageObj.toCopyScheme($("#selectNode").val());
    });

    //kpl跳转编辑页面
    $("#schEdit").on("click", function () {
        pageObj.editScheme($("#selectNode").val());
    });

    //kpl跳转删除页面
    $("#deleteScheme").on("click", function () {
        pageObj.toDelScheme($("#selectNode").val());
    });

    //okr跳转新增页面
    $("#okrAdd").on("click", function () {
        OKRPageObj.addScheme();
    });

    //okr拷贝
    $("#copyOKRScheme").on("click",function () {
        OKRPageObj.toCopyScheme($("#okrNode").val());
    });

    //okr跳转编辑页面
    $("#okrSchemeEdit").on('click',function () {
        OKRPageObj.editScheme($("#okrNode").val())
    });

    //okr跳转删除页面
    $("#okrSchemeDelete").on("click", function () {
        OKRPageObj.toDelScheme($("#okrNode").val());
    });

    //删除操作
    $("#schDel").on("click", function () {
        var type = parseInt($("#performSchemeType").val());
        if(type==1){
            //kpl删除操作
            pageObj.delScheme($("#selectNode").val());
        }else{
            //okr删除操作
            OKRPageObj.delScheme($("#okrNode").val());
        }
    });

    //拷贝操作
    $("#schCopy").on("click", function () {
        var type = parseInt($("#performSchemeType").val());
        if(type==1){
            //kpl拷贝操作
            pageObj.copyScheme($("#selectNode").val());
        }else{
            //okr拷贝操作
            OKRPageObj.copyScheme($("#okrNode").val());
        }
    });
});

//多个页面使用的方法或者数据
var commonObj = {
    //后台请求方法
    requestData: function (data, url, requestType,dataType,async,callBackFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    //Tab切换处理事件
    tabChange: function (index) {
        $(".tabContent").css("display","none");
        if(index == 0){
            //kpl绩效考核
            $("#kplTab").css("display","block");
            //kpl标志
            $("#performSchemeType").val(1);
            pageObj.initTree();
        }else if(index == 1){
            //OKR绩效考核
            $("#okrTab").css("display","block");
            //okr标志
            $("#performSchemeType").val(2);
            OKRPageObj.initTree();
        }
    },
    initPage: function () {
        commonObj.tabChange(0);
    }
}

var pageObj = {
    //初始化左侧树结构
    initTree: function () {
        var nodes = [];
        $.ajax({
           type:"get",
           url:baseUrl + "/performanceScheme/postInfo",
           data:{schemeType:1},
           dataType:"json",
           async:false,
           success:function (data) {
               var schemeLs = groupBy(data.data.scheme, function (item) {
                   return item.createTime;
               });
               var num = 0;
               schemeLs.forEach(function (item) {
                   num++;
                   var nodeText = new Date(item[0].createTime).format("yyyy-MM");
                   var dateFormat = new Date().format("yyyy-MM");
                   var nodeExpand=false;
                   if(nodeText==dateFormat){
                       nodeExpand=true;
                   }else {
                       nodeExpand=false;
                   }
                   var node = {};
                   var text = new Date(item[0].createTime).format("yyyy年MM月");
                   node.id = num;
                   node.name = text;
                   node.open = nodeExpand;
                   node.pId = 0;
                   nodes.push(node);
                   item.forEach(function (child) {
                       var childNode = {};
                       childNode.id = child.schId;
                       childNode.name = child.schName;
                       childNode.pId = num;
                       childNode.schData = child;
                       childNode.icon = "/img/performanceScheme/scheme.png";
                       nodes.push(childNode);
                   });
               });
           }
        });
        treeObj = $.fn.zTree.init($("#schemeTree"),treeSetting,nodes);
    },
    //渲染数据
    renderScheme: function (scheme) {
        var schData = scheme;
        if (!schData) return;
        //初始化表单数据
        Object.keys(schData).forEach(function (key) {
            $("#schemeView span[name='" + key + "']").text(schData[key])
            if(key=="schCode"){
                $("#schemeView span[name='" + key + "']").attr("title",schData[key]);
            }
            if(key=="schUsed"){
                $("#schemeView span[name='" + key + "']").text(schData[key]==0?"启用":"禁用");
            }
        });

        //初始化合格等级
        var sufLev = "";
        for (var ii = 0; ii < level.length; ii++) {
            sufLev += level[ii] + ",";
            if (level[ii] === schData.schSuffice) break;
        }
        $("#schSufficeView").text(sufLev);

        //初始化分数
        var htmlMsg = "";
        var lvScore = JSON.parse(schData.schLevel);
        lvScore.forEach(function (item) {
            htmlMsg += item.lv + "(" + item.min + "-" + item.max + ")" + ";"
        });
        $("#schLevelView").text(htmlMsg);

        //初始化考核对象
        var excludePeople = schData.schUserId.split(",");
        var postId = schData.postId;
        $.get(baseUrl + "/user/queryUserByCondition?postId=" + postId, function (data) {
            var checkPeople = data.filter(function (item) {
                return !excludePeople.contains(item.id);
            }).map(function (item) {
                return item.name
            }).concat(",").toString();

            $("#schUserView").text(checkPeople.slice(0, checkPeople.lastIndexOf(',')));
        }, "json");

        //初始化考核小组
        var evaluation = schData.schUserName.split(",");
        var evaluationPeople = evaluation.map(function (value) {
            return value;
        }).concat(",");
        $("#evaluationUserView").text(evaluationPeople.slice(0, evaluationPeople.lastIndexOf(',')));

        //初始化考核阶段
        $("#schType").text(schemeType[schData.schType]);

        //初始化考核项目表格
        $.ajax({
            type: "get",
            dataType: "json",
            url: baseUrl + "/performanceHistory/listPlate?schId=" + schData.schId,
            success: function (data) {
                var jsData = data.data;

                var html = "";
                $(jsData.data).each(function (i, d) {
                    if (i > 0) {
                        html += "</tr>";
                        html += "<tr>";
                    } else {
                        html += "<tr>";
                    }
                    var rowspan = d.childSize > 0 ? "rowspan='" + d.childSize + "'" : "";
                    html += "<td " + rowspan + ">" + d.content + "</td>";
                    if (d.childSize > 0) {
                        $(d.child).each(function (j, dd) {
                            if (j > 0) {
                                html += "</tr>";
                                html += "<tr>";
                            }
                            var rowspan = dd.childSize > 0 ? "rowspan='" + dd.childSize + "'" : "";
                            html += "<td " + rowspan + ">" + dd.target + "</td>";
                            html += "<td " + rowspan + ">" + dd.demand + "</td>";
                            html += "<td " + rowspan + ">" + dd.content + "</td>";
                            html += "<td " + rowspan + ">" + dd.proportion + "</td>";
                            // if (dd.childSize > 0) {
                            //     $(dd.child).each(function (k, ddd) {
                            //         if (k > 0) {
                            //             html += "</tr>";
                            //             html += "<tr>";
                            //         }
                            //         html += "<td >" + ddd.content + "  (" + ddd.proportion + ")分" + "</td>";
                            //     });
                            // } else {
                            //     html += "<td></td>";
                            // }
                        })
                    } else {
                        html += "<td></td>";
                    }
                    if (jsData.data.length === 0)
                        html += "</tr>";
                });
                $("#tableContent").html(html);
            }
        });

        $("#schDesc").text(schData.schDesc);
    },
    //复选框点击事件
    onCheck: function (e, treeId, treeNode) {
        if(treeNode.schData!=null){
             $("#selectNode").val(treeNode.id);
             pageObj.renderScheme(treeNode.schData);
        }else {
            $("#selectNode").val("");
        }
    },
    addScheme: function () {
        var addScheme = "/performance/performanceSchemeAdd?type=1";
        page(addScheme, "新增KPI方案");
    },
    editScheme: function (schId) {
        if (!schId) {
            layer.msg("请选择KPI方案");
            return;
        }
        var editScheme = "/performance/performanceSchemeEdit?id=" + schId;
        page(editScheme, "编辑KPI方案");
    },
    toDelScheme:function(schId){
        if (!schId) {
            layer.msg("请选择KPI方案");
            return;
        }
        $('#deleteConfirm').modal('toggle');
    },
    delScheme: function (schId) {
        if (!schId) {
            layer.msg("请选择KPI方案");
            return;
        }
        $.ajax({
            type: "delete",
            url: baseUrl + "/performanceHistory?schId=" + schId,
            dataType: "json",
            success: function (data) {
                if(data.data.message!="" && data.data.message!=null){
                    layer.open({
                        title:"提示",
                        content:data.data.message
                    });
                }else{
                    setTimeout(function () {
                        window.location.href="/performance/performanceSchemeList?index=0";
                    }, 1500);
                }
                $('#deleteConfirm').modal('hide');
            }
        });
    },
    toCopyScheme:function(schId){
        if (!schId) {
            layer.msg("请选择KPI方案");
            return;
        }
        $('#copyConfirm').modal('toggle');
    },
    copyScheme: function (schId) {
        if (!schId) {
            layer.msg("请选择KPI方案");
            return;
        }
        $.ajax({
            type: "post",
            url: baseUrl + "/performanceScheme/copy?schId=" + schId,
            dataType: "json",
            success: function () {
                window.location.href="/performance/performanceSchemeList?index=0";
                $('#copyConfirm').modal('hide');
            }
        });
    }
};
//OKR数据封装类
var OKRPageObj = {
    //初始化左侧树结构
    initTree: function () {
        var nodes = [];
        $.ajax({
            type:"get",
            url:baseUrl + "/performanceScheme/postInfo",
            data:{schemeType:2},
            dataType:"json",
            async:false,
            success:function (data) {
                var schemeLs = groupBy(data.data.scheme, function (item) {
                    return item.createTime;
                });
                var num = 0;
                schemeLs.forEach(function (item) {
                    num++;
                    var nodeText = new Date(item[0].createTime).format("yyyy-MM");
                    var dateFormat = new Date().format("yyyy-MM");
                    var nodeExpand=false;
                    if(nodeText==dateFormat){
                        nodeExpand=true;
                    }else {
                        nodeExpand=false;
                    }
                    var node = {};
                    var text = new Date(item[0].createTime).format("yyyy年MM月");
                    node.id = num;
                    node.name = text;
                    node.open = nodeExpand;
                    node.pId = 0;
                    nodes.push(node);
                    item.forEach(function (child) {
                        var childNode = {};
                        childNode.id = child.schId;
                        childNode.name = child.schName;
                        childNode.pId = num;
                        childNode.schData = child;
                        childNode.icon = "/img/performanceScheme/scheme.png";
                        nodes.push(childNode);
                    });
                });
            }
        });
        treeObj = $.fn.zTree.init($("#okrSchemeTree"),OKRTreeSetting,nodes);
    },
    //渲染数据
    renderScheme: function (scheme) {
        var schData = scheme;
        if (!schData) return;
        //初始化表单数据
        Object.keys(schData).forEach(function (key) {
            $("#okrSchemeView span[name='" + key + "']").text(schData[key])
            if(key=="schCode"){
                $("#okrSchemeView span[name='" + key + "']").attr("title",schData[key]);
            }
            if(key=="groupNames"){
                $("#okrSchemeView #okrUserView").text(schData[key]);
            }
            if(key=="schUsed"){
                $("#okrSchemeView span[name='" + key + "']").text(schData[key]==0?"启用":"禁用");
            }
        });

        //初始化合格等级
        var sufLev = "";
        for (var ii = 0; ii < level.length; ii++) {
            sufLev += level[ii] + ",";
            if (level[ii] === schData.schSuffice) break;
        }
        $("#okrSufficeView").text(sufLev);

        //初始化分数
        var htmlMsg = "";
        var lvScore = JSON.parse(schData.schLevel);
        lvScore.forEach(function (item) {
            htmlMsg += item.lv + "(" + item.min + "-" + item.max + ")" + ";"
        });
        $("#okrLevelView").text(htmlMsg);

        //初始化考核项目表格
        $.ajax({
            type: "get",
            dataType: "json",
            url: baseUrl + "/performanceHistory/listPlate?schId=" + schData.schId,
            success: function (data) {
                var jsData = data.data;

                var html = "";
                $(jsData.data).each(function (i, d) {
                    if (i > 0) {
                        html += "</tr>";
                        html += "<tr>";
                    } else {
                        html += "<tr>";
                    }
                    html += "<td>" + d.content + "</td>"+
                            "<td>" + d.proportion + "</td>"+
                            "<td>" + d.target + "</td>"+
                            "<td>" + d.demand + "</td>";
                    if (jsData.data.length === 0)
                        html += "</tr>";
                });
                $("#okrContent").html(html);
            }
        });

        $("#okrDesc").text(schData.schDesc);
    },
    //复选框点击事件
    onCheck: function (e, treeId, treeNode) {
        if(treeNode.schData!=null){
            $("#okrNode").val(treeNode.id);
            OKRPageObj.renderScheme(treeNode.schData);
        }else {
            $("#okrNode").val("");
        }
    },
    addScheme: function () {
        var addScheme = "/performance/performanceSchemeAdd?type=2";
        page(addScheme, "新增OKR方案");
    },
    editScheme: function (schId) {
        if (!schId) {
            layer.msg("请选择OKR方案");
            return;
        }
        var editScheme = "/performance/performanceSchemeEdit?id=" + schId;
        page(editScheme, "编辑OKR方案");
    },
    toDelScheme:function(schId){
        if (!schId) {
            layer.msg("请选择OKR方案");
            return;
        }
        $('#deleteConfirm').modal('toggle');
    },
    delScheme: function (schId) {
        if (!schId) {
            layer.msg("请选择OKR方案");
            return;
        }
        $.ajax({
            type: "delete",
            url: baseUrl + "/performanceHistory?schId=" + schId,
            dataType: "json",
            success: function (data) {
                if(data.data.message!="" && data.data.message!=null){
                    layer.open({
                        title:"提示",
                        content:data.data.message
                    });
                }else{
                    setTimeout(function () {
                        window.location.href="/performance/performanceSchemeList?index=1";
                    }, 1500);
                }
                $('#deleteConfirm').modal('hide');
            }
        });
    },
    toCopyScheme:function(schId){
        if (!schId) {
            layer.msg("请选择OKR方案");
            return;
        }
        $('#copyConfirm').modal('toggle');
    },
    copyScheme: function (schId) {
        if (!schId) {
            layer.msg("请选择OKR方案");
            return;
        }
        $.ajax({
            type: "post",
            url: baseUrl + "/performanceScheme/copy?schId=" + schId,
            dataType: "json",
            success: function () {
                window.location.href="/performance/performanceSchemeList?index=1";
                $('#copyConfirm').modal('hide');
            }
        });
    }
};

function groupBy(array, f) {
    var groups = {};
    array.forEach(function (o) {
        var group = new Date(f(o)).format("yyyy-MM");
        groups[group] = groups[group] || [];
        groups[group].push(o);
    });
    return Object.keys(groups).map(function (group) {
        return groups[group];
    });
}