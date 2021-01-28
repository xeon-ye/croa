var existsUserMap = {}; //已选择的员工，格式：{userId : [userName，deptId]}
$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#table_batch_list");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });
    createTable(); //创建表格
    //初始化群组类型
    initGroupType();

    //增加批次按钮
    $("#addBtn").click(function () {
        existsUserMap = {};//每次新增时情况已选人员列表
        $("#editModal").modal("toggle");
        document.getElementById("editForm").reset();

        $("input[type='checkbox']").each(function (i,t) {
           $(t).iCheck('uncheck');
        });
        $("name").html();
        $("remarks").empty();
        $(".save").show();
        $(".update").hide();
        $("#saveGroupTitle").text($("#groupTypeText").text());
        $("#createName").val(user.name);
        $("#evaluation-people-btn").empty();
        $("#createTime").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
    });

    //人员搜索按钮点击事件
    $("#userSearch").on("click", function () {
        renderUserPage();
    });

    //点击保存更改
    $("#submitEvaluationPeople").click(function () {
        var btnList = "";
        if(existsUserMap && Object.getOwnPropertyNames(existsUserMap).length > 0){
            for(var userId in existsUserMap){
                var userName = existsUserMap[userId].userName;
                var deptId = existsUserMap[userId].deptId;
                var $span = '<div userId="'+userId+'" deptId="'+deptId+'" title="'+userName+'" onclick="removeBtn(this);" class="userDivClass">\n' +
                    '            <div class="userClass">'+userName+'</div>\n' +
                    '            <div title="删除" class="deleteClass">\n' +
                    '                <i class="fa fa-trash"></i>\n' +
                    '            </div>\n' +
                    '        </div>';
                var inputUserId = '<input id="userId'+userId+'" name="inputUserId" type="hidden" value="' + userId + '">';
                var inputUserName = '<input id="userName'+userId+'" name="inputUserName" type="hidden" value="' + userName + '">';
                $span  += inputUserId;
                $span  += inputUserName;
                btnList += $span;
            }
        }
        $("#evaluation-people-btn").html(btnList)
        $('#evaluationModal').modal('hide');
    });
})

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
        success: callBackFun
    };
    if(contentType){
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
}

//初始化群组类型
function initGroupType() {
    requestData(null,"/userGroupType/listAllGroupType","post","json",false, function (data) {
        if (data == null || data == '') {
            swal("没有群组类型可操作！", "没有查询到群组类型信息，请联系管理员查看！", "warning");
            return;
        }
        var html = "";
        var len = 1;
        $(data).each(function (i, item) {
            html += "<div id='groupType"+item.id+"' data-sys='"+item.isSysDefault+"' class='col-md-" + len + "'><span class='btn btn-outline btn-primary' title='" + item.name + "' data-value='" + item.id + "' ondblclick='updateGroupType(this, "+JSON.stringify(item)+")' onclick='setType(" + item.id + ",this)'>" + item.name + "</span></div>";
        });
        html += " <button id='addBtnGroup' style='margin-left: 10px;position: absolute;top: 0px;right: 10px;z-index: 10;' type=\"button\" class=\"col-md-1 btn btn-primary btn-circle glyphicon glyphicon-plus \" onclick=\"addGroupType(this)\"></button>";
        html += " <button id='del' style='margin-left: 10px;position: absolute;top: 0px;right: -30px;z-index: 10;' type=\"button\" class=\"col-md-1 btn btn-primary btn-circle glyphicon glyphicon-minus\" onclick=\"delGroupType(this)\"></button>";

        $("#groupType").html(html);

        $("#groupType>div:first-child>span:first-child").click();
    });
}

//添加群组类型
function addGroupType(tt) {
    var addFlag = true;
    var index  = layer.open({
        type: 1,
        zIndex: 9999,
        content: $("#addGroupTypeDiv"),
        btn: ['保存'],
        area: ['400px', '160px'],
        title: "新增群组类型",
        yes: function (t) {
            if(!$("#addGroupTypeForm").valid()) return;
            if(addFlag){
                addFlag = false; //防止多次点击触发请求
                requestData(JSON.stringify($("#addGroupTypeForm").serializeJson()), "/userGroupType/save", "post","json",true,function (data) {
                    swal({
                        title: data.code == 200 ? "成功!" : "失败",
                        text: data.code == 200 ? "群组类型添加成功！" : data.msg,
                        type: data.code == 200 ? "success" : "error",
                        html: true
                    });
                    if(data.code == 200){
                        layer.closeAll();
                        var groupType = data.data.groupType;
                        if(groupType){
                            var html = "<div id='groupType"+groupType.id+"' data-sys='"+groupType.isSysDefault+"' class='col-md-1'><span class='btn btn-outline' title='" + groupType.name + "' data-value='" + groupType.id + "' onclick='setType(" + groupType.id + ",this)'>" + groupType.name + "</span></div>";
                            $("#groupType").append(html);
                        }
                    }else{
                        addFlag = true; //出错了，继续可以点击
                    }
                },true);
            }
        }
    });
}

//删除群组类型
function delGroupType(obj) {
    var currentTypeId = $("#groupTypeId").val();
    if(currentTypeId && $("#groupType"+currentTypeId)){
        if($("#groupType"+currentTypeId).attr("data-sys") != 1){
            layer.confirm("删除群组类型后，所有的群组都将删除，是否确定删除?", {
                btn: ["确认", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + $(obj).attr("id"));
                requestData({id:currentTypeId}, "/userGroupType/del", "post", "json", true, function (data) {
                    swal({
                        title: data.code == 200 ? "成功!" : "失败",
                        text: data.code == 200 ? "群组类型删除成功！" : data.msg,
                        type: data.code == 200 ? "success" : "error",
                        html: true
                    });
                    if(data.code == 200){
                        $("#groupType"+currentTypeId).remove();//删除当前类型
                        $("#groupType>div:first-child>span:first-child").click();//默认第一个类型
                    }
                });
                Ladda.stopAll();
            }, function () {
                return;
            })
        }else{
            swal("系统默认群组类型不允许删除！")
        }
    }
}

//修改群组类型名称
function updateGroupType($span,item) {
    //系统默认群组类型不允许修改
    if(item && item.isSysDefault != 1){
        $("#typeId").val(item.id); //设置ID
        $("#updateGroupTypeName").val(item.name); //设置名称
        var updateFlag = true;
        var index = layer.open({
            type: 1,
            zIndex: 9999,
            content: $("#updateGroupTypeDiv"),
            btn: ['保存'],
            area: ['400px', '160px'],
            title: "修改群组类型",
            yes: function (t) {
                if(!$("#updateGroupTypeForm").valid()) return;
                if(updateFlag){
                    updateFlag = false; //防止多次点击触发请求
                    requestData(JSON.stringify($("#updateGroupTypeForm").serializeJson()), "/userGroupType/update", "post","json",true,function (data) {
                        swal({
                            title: data.code == 200 ? "成功!" : "失败",
                            text: data.code == 200 ? "群组类型修改成功！" : data.msg,
                            type: data.code == 200 ? "success" : "error",
                            html: true
                        });
                        if(data.code == 200){
                            layer.closeAll();
                            var groupType = data.data.userGroupType;
                            if(groupType){
                                $($span).attr("title",groupType.name);
                                $($span).text(groupType.name);
                            }
                        }else{
                            updateFlag = true; //出错了，继续可以点击
                        }
                    },true);
                }
            }
        });
    }else{
        swal("系统默认群组类型不允许修改！")
    }
}

function setType(id, t) {
    $(t).parent().parent().find("div>span").each(function (i, item) {
        $(item).removeClass("btn-primary");
        if (t == item) {
            $(t).addClass("btn-primary");
        }
    });

    $("#groupTypeText").text($(t).attr("title"));
    $("#groupTypeText").attr("title",$(t).attr("title"));
    $("#addGroupTypeName").text($(t).attr("title"));
    $("#groupTypeId").val(id);
    $("#modalGroupTypeId").val(id); //设置模态框群组类型
    reflushTable();//刷新表格
}

//表格定义
function createTable() {
    $("#table_batch_list").jqGrid({
        url: baseUrl + "/userGroup/listPg",
        datatype: "json",
        postData:$("#queryForm").serializeJson(), //发送数据
        mtype: "post",
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        multiselect: true,
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselectWidth: 50, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['用户名','姓名', '职位', '电话', '操作'],
        colModel: [
            {name:'id',label:'id',hidden:true,width:60,sortable:false},
            {name:'name',label:'群组名称',hidden:false,width:20,sortable:true},
            {name:'remarks',label:'群组描述',hidden:false,width:40,sortable:false},
            {name:'userName',label:'群组人员',hidden:false,width:150,sortable:false},
            {name: 'operate',label:'操作',index :'',width :40,sortable:false,
                formatter: function (value, grid, rows, state) {
                    var html = "";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='edit(" + rows.id + ")'>编辑&nbsp;&nbsp;</a>";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='del(" + rows.id + ")'>删除</a>";
                    return html;
                }}
        ],
        pager: jQuery("#pager_batch_list"),
        viewrecords: true,
        caption: "群组列表",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");
        },
    });
}

//刷新表格
function reflushTable() {
    $("#name1").val($("#groupName").val());
    //刷新表格
    $("#table_batch_list").emptyGridParam();
    $("#table_batch_list").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//删除批次
function del(id) {
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: baseUrl + "/userGroup/del",    //向后端请求数据的url
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    reflushTable(); //刷新表格
                } else {
                    if (!getResCode(data)){
                        swal({
                            type: "error",
                            title: data.msg
                        });
                    }
                }
            }
        });
    }, function () {
        return;
    });
}

function  submitHander(t,url,state) {
    if ($("#editForm").valid()){
        var  tips;
        if (state==0){
            tips = "确认保存？";
        }else{
            tips = "确认保存？";
        }
        $("#state").val(state);
        var formData= $("#editForm").serializeArray();
        if(formData.hasOwnProperty("inputUserId") && !Array.isArray(formData.inputUserId)){
            var array = [];
            array.push(formData.inputUserId);
            formData.inputUserId = array;
        }
        layer.confirm(tips,{
            btn:['确定','取消'],
            shade:false
        },function (index) {
            layer.close(index);
            startModal("#"+t.id);
            $.ajax({
                type:"post",
                url:url,
                data:formData,
                dataType:"json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#table_batch_list").reloadCurrentData(baseUrl + "/userGroup/listPg", $("#searchForm").serializeJson(), "json", null, null);
                        $("#editModal").modal('hide');
                        $("#viewModal").modal('hide');
                    } else {
                        if (getResCode(data))
                            return;
                        $("#editModal").modal('hide');

                    }
                },
            })
        })
    }

}

//渲染人员选择页面
function renderUserPage() {
    var rootDom = $("#evaluationModal");
    var listUser = "/userGroup/listBusinessPart?name=" + $("#nameQc").val().trim();
    //渲染人员页面
    requestData(null, listUser, "get","json",false,function (data) {
        var userList = groupBy(data, function (item) {
            return [item.deptId];
        });
        var html = template("excludePeopleHtml", {'data': userList}); //将用户数据渲染html
        rootDom.find("div[data-id='groups']").html(html);
        reloadICheck(rootDom); //重新加载i-checks

        //部门复选框点击事件
        rootDom.find(".deptSpan").on('ifChecked', function () {
            var userInputArr = $(this).parent().parent().parent().next().find("input[type='checkbox']");
            if(userInputArr && userInputArr.length > 0){
                $.each(userInputArr, function (index,t) {
                    $(t) .iCheck('check');
                    existsUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                })
            }

        });
        //部门复选框取消点击事件
        rootDom.find(".deptSpan").on('ifUnchecked', function () {
            var userInputArr = $(this).parent().parent().parent().next().find("input[type='checkbox']");
            if(userInputArr && userInputArr.length > 0){
                $.each(userInputArr, function (index,t) {
                    $(t) .iCheck('uncheck');
                    delete existsUserMap[$(t).attr('userId')];
                })
            }
        });
        //全选按钮点击事件
        rootDom.find('input[data-id="all"]').on('ifChecked', function () {
            var userInputArr = rootDom.find("div[data-id='groups']").find(".i-checks");
            if(userInputArr && userInputArr.length > 0){
                $.each(userInputArr, function (index,t) {
                    $(t) .iCheck('check');
                    if($(t).attr('userId')){
                        existsUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                    }
                })
            }
        });
        //全选按钮取消点击事件
        rootDom.find('input[data-id="all"]').on('ifUnchecked', function () {
            var userInputArr = rootDom.find("div[data-id='groups']").find(".i-checks");
            if(userInputArr && userInputArr.length > 0){
                $.each(userInputArr, function (index,t) {
                    $(t) .iCheck('uncheck');
                    existsUserMap = {};
                })
            }
        });

        //用户复选框点击事件
        $(".userSpan").on('ifClicked', function () {
            var flag = $(this).is(':checked');
            if(flag){  //如果是选中状态，则删除用户
                delete existsUserMap[$(this).attr('userId')];
            }else{
                existsUserMap[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};
            }
        });
    })

    //判断当前已选择页面
    if(existsUserMap && Object.getOwnPropertyNames(existsUserMap).length > 0){
        for(var key in existsUserMap){
            $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
        }
    }
}

//重新加载i-checks
function reloadICheck(root) {
    root.find('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green'
    });
}

//人员部门分组
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

//移除人员
function removeBtn(t) {
    var userId = $(t).attr("userId");
    $("#userId"+userId).remove();
    $("#userName"+userId).remove();
    $(t).remove();
    delete existsUserMap[userId];
}

//添加人员点击事件
function addUser() {
    $("#evaluationModal").modal('toggle');
    renderUserPage();
}

//编辑操作
function  edit(id) {
    existsUserMap = {}; //清空已选人员
    $("#editModal").modal('toggle');
    $("#createName").val(user.name);
    $(".save").hide();
    $(".update").show();
    $("#updateGroupTitle").text($("#groupTypeText").text());
    $("#editModal").find("input[name='label']").removeAttr("checked");
    $("#editModal").find("input[name='label']").parent().removeClass("checked");
    $("#evaluation-people-btn").empty();
    $.ajax({
        type:"post",
        url:baseUrl + "/userGroup/edit",
        dataType:"json",
        data:{id:id},
        success:function (data){
            if(data.code == 200){
                if(data.data.entity){
                    for (var attr in data.data.entity){
                        $("#editModal [name=" + attr + "][type!='radio'][type!='checkbox']").val(data.data.entity[attr]);
                        $("#editModal input [name=" + attr + "]").text(data.data.entity[attr]);
                    }
                }
                if(data.data.list && data.data.list.length){
                    var btnList = "";
                    $.each(data.data.list, function (i, user) {
                        var userId = user.id ;
                        var userName = user.name;
                        var deptId = user.deptId;
                        existsUserMap[userId] = {userName: userName, deptId: deptId};
                        var $span = '<div userId="'+userId+'" deptId="'+deptId+'" onclick="removeBtn(this);" class="userDivClass">\n' +
                            '            <div class="userClass">'+userName+'</div>\n' +
                            '            <div title="删除" class="deleteClass">\n' +
                            '                <i class="fa fa-trash"></i>\n' +
                            '            </div>\n' +
                            '        </div>';
                        var inputUserId = '<input id="userId'+userId+'" name="inputUserId" type="hidden" value="' + userId + '">';
                        var inputUserName = '<input id="userName'+userId+'" name="inputUserName" type="hidden" value="' + userName + '">';
                        $span  += inputUserId;
                        $span  += inputUserName;
                        btnList += $span;
                    })
                    $("#evaluation-people-btn").html(btnList)
                }
            }else{
                swal({
                    title: "失败",
                    text: data.msg,
                    type: "error"
                });
            }
        }
    })


}
