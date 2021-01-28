var schemeType = ["月度计划", "季度计划", "年中计划", "年终计划"];
var userType = ["开启", "禁用"];
var treeSetting;
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
    pageObj.initTree();

    $("#schDel").on("click", function () {
        pageObj.delScheme($("#selectNode").val());
    });

    $("#deleteProgram").click(function (data) {
        var proId = $("#selectNode").val();
        if (!proId) {
            layer.msg("请选择方案");
            return;
        }
        pageObj.getAllApproveData($("#selectNode").val());
        $("#deleteConfirm").modal("toggle");
    });

    $("#schAdd").on("click", function () {
        pageObj.addScheme();
    });

    $("#schCopy").on("click", function () {
        pageObj.copyScheme($("#selectNode").val());
    });

    $("#programUsed").on("click",function () {
        var proId = $("#selectNode").val();
        if (!proId) {
            layer.msg("请选择计划");
            return;
        }
        $("#usedConfirm").modal("toggle");
    });

    $("#schUsed").on("click", function () {
        //启动
        pageObj.usedScheme($("#selectNode").val(),$("#proUsed").val());
    });

    $("#schStar").on("click", function () {
        //发起流程
        pageObj.starScheme($("#selectNode").val());
    });

    $("#copyProgram").on("click", function () {
        var proId = $("#selectNode").val();
        if (!proId) {
            layer.msg("请选择计划");
            return;
        }
        $("#copyForm").find("input").removeClass('error');
        $("#copyForm").validate().resetForm();
        document.getElementById("copyForm").reset();
        $("#copyConfirm").modal("toggle");
    });

    $("#startFlow").on("click", function () {
        var proId = $("#selectNode").val();
        if (!proId) {
            layer.msg("请选择计划");
            return;
        }
        $("#starConfirm").modal("toggle");
    });
});

var pageObj = {
    //初始化左侧树结构
    initTree: function () {
        var nodes = [];
        $.ajax({
            type:"get",
            url:baseUrl + "/proportion/getAllProportion",
            data:null,
            dataType:"json",
            async:false,
            success:function (data) {
                var schemeLs = groupBy(data.data.pro, function (item) {
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
                        childNode.id = child.proId;
                        childNode.name = child.proName;
                        childNode.pId = num;
                        childNode.schData = child;
                        childNode.icon = "/img/performanceScheme/plan.png";
                        nodes.push(childNode);
                    });
                });
            }
        });
        treeObj = $.fn.zTree.init($("#schemeTree"),treeSetting,nodes);
    },
    //渲染计划数据
    renderScheme: function (scheme) {
        $("#plateListView").html("");
        var schData = scheme;
        if(schData!=null){
            $("#schemeDetail").html("");
            //按id获取考核计划详情
            $.ajax({
                type: "get",
                dataType: "json",
                url: baseUrl + "/proportion/getProportionById?proId=" + schData.proId,
                success: function (data) {
                    var jsData = data.data.entity;
                    // console.log(jsData);
                    //初始化表单数据
                    Object.keys(jsData).forEach(function (key) {
                        $("#schemeView span[name='" + key + "']").text(jsData[key])
                    });
                    //初始化考核阶段
                    $("#proType").text(schemeType[jsData.proType])
                    //初始化考核备注
                    $("#proDesc").text(jsData.proDesc);
                    //初始化启用状态
                    $("#proUsed").val(jsData.proUsed);
                    $("#userType").text(userType[jsData.proUsed]);

                    //初始化统分比例
                    $("#proportion").text("自评（"+jsData.proportionSelf+"%）、上级评("+jsData.proportionLeader+"%）");
                    //初始化考核日期
                    if(jsData.proBegin!=null && jsData.proEnd!=null){
                       $("#proDate").text(new Date(jsData.proBegin).format("yyyy-MM-dd")+" 至 "+new Date(jsData.proEnd).format("yyyy-MM-dd"));
                    }else{
                        $("#proDate").text("");
                    }
                    //初始化考核方案（遍历数组）
                    if(jsData.programList.length>0){
                        jsData.programList.forEach(function(item){
                            var schName = item.schName;
                            var schemeTypeName = +item.schemeType==1?"【KPI】":"【OKR】";
                            var peopleView = item.schUserName;
                            var schSuffice = item.schSuffice;
                            var html = "";
                            html+="<tr>" +
                                "<td>"+schemeTypeName+schName+"</td>" +
                                "<td>"+peopleView +"</td>" +
                                "<td>"+schSuffice +"</td>" +
                                "</tr>";
                            $("#schemeDetail").append(html);
                        })
                    }
                }
            });
        }
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
        var addScheme = "/performance/performanceProgramAdd";
        page(addScheme, "新增计划");
    },
    editScheme: function (proId) {
        if (!proId) {
            layer.msg("请选择计划");
            return;
        }
        var editScheme = "/performance/performanceProgramAdd?id=" + proId;
        page(editScheme, "编辑计划");
    },
    getAllApproveData:function(proId){
        $.ajax({
            type: "post",
            url: baseUrl + "/performanceScore/getAllApproveData?proId=" + proId,
            dataType: "json",
            success: function (data) {
                if(data.code==200){
                    $("#showTipsContent").text(data.data.message);
                }else if(data.code==1002){
                    swal({
                        title:"异常提示",
                        text:data.msg
                    });
                }
            },
            error:function (data) {

            }
        });
    },
    delScheme: function (proId) {
        layer.confirm("确定是否删除该计划?",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: baseUrl + "/proportion/deleteProportion?proId=" + proId,
                dataType: "json",
                success: function () {
                    //刷新页面
                    window.location.href="/performance/performanceProgramList";
                    $('#deleteConfirm').modal('hide');
                }
            });
        })
    },
    copyScheme: function (proId) {
        var proId = $("#selectNode").val();
        if (!proId) {
            layer.msg("请选择计划");
            return;
        }
        if($("#copyForm").valid() && pageObj.checkProName(proId)){
            layer.confirm("是否确定拷贝该计划?", {
                btn:["确定","取消"],
                shade:false
            },function (index) {
                layer.close(index);
                var formData = $("#copyForm").serializeJson();
                formData.proId=proId;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/proportion/copyProportion",
                    data:formData,
                    dataType: "json",
                    success: function () {
                        window.location.href="/performance/performanceProgramList";
                        $('#copyConfirm').modal('hide');
                    }
                });
            });
        }
    },
    usedScheme: function (proId,proUsed) {
        if(proUsed==0){
            proUsed = 1;
        }else{
            proUsed = 0;
        }
        $.ajax({
            type: "post",
            url: baseUrl + "/proportion/updateProportionUserState",
            data:{"proId":proId,"proUsed":proUsed},
            dataType: "json",
            success: function () {
                //刷新页面
                window.location.href="/performance/performanceProgramList";
                $('#usedConfirm').modal('hide');
            }
        });
    },
    starScheme: function (proId) {
        if (!proId) {
            layer.msg("请选择计划");
            return;
        }
        //判断绩效计划是否启用
        if(pageObj.checkUsed($("#selectNode").val())){
            var index = layer.load(4,{time:20000});
            $.ajax({
                type: "post",
                url: baseUrl + "/proportion/startPerformanceProcess?proId=" + proId,
                dataType: "json",
                success: function (data) {
                    layer.close(index);
                    swal({
                        title: "提示",
                        text:data.data.message
                    });
                    // window.location.href="/performance/performanceProgramList";
                    $('#starConfirm').modal('hide');
                }
            });
        }
    },
    checkUsed:function (proId) {
        var flag=false;
        $.ajax({
            type: "post",
            url: baseUrl + "/proportion/checkUsed?proId=" + proId,
            dataType: "json",
            async:false,
            success: function (data) {
                if(data.data.entity!=null){
                   var checkFlag=data.data.flag;
                   if(!checkFlag){
                       layer.open({
                           title: "提示",
                           content:data.data.message
                       });
                       flag=checkFlag;
                       $('#starConfirm').modal('hide');
                   }else{
                       flag=checkFlag;
                   }
                }else {
                    layer.open({
                        title: "提示",
                        content:"该计划已被禁用，请启用后再发起流程！"
                    });
                    flag=false;
                    $('#starConfirm').modal('hide');
                }
            }
        });
        return flag;
    },
    checkProName:function (proId) {
        var flag=false;
        var proName=$("#copyForm input[name='proName']").val();
        $.ajax({
            type: "post",
            url: baseUrl + "/proportion/findProportionByCondition",
            data:{proId:proId,proName:proName},
            dataType: "json",
            async:false,
            success: function (data) {
                if(data.data.entity==null){
                    var beginTime=$("#copyForm input[name='proBegin']").val();
                    var endTime=$("#copyForm input[name='proEnd']").val();
                    if(beginTime=="" || endTime==""){
                        layer.open({
                            title:"提示",
                            content:"请填写考核日期"
                        });
                        flag = false;
                    }else {
                        flag = true;
                    }
                }else {
                    layer.open({
                        title: "提示",
                        content:"该计划名称已存在"
                    });
                    flag=false;
                }
            }
        });
        return flag;
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