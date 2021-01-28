var existsUserMap = {};

$(document).ready(function() {

    $( "#editModal" ).draggable();
    //人员搜索按钮点击事件
    $("#userSearch").on("click", function () {
        renderUserPage();
    });
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#query_table_logs').setGridWidth(width);
    });
    $("#id").empty();
    $("#selectedDept").empty();
    $("#dictId").empty();
    $("#submitEvaluationPeople").click(function () {
        var btnList = "";
        if(existsUserMap && Object.getOwnPropertyNames(existsUserMap).length > 0){
            for(var userId in existsUserMap){
                var userName = existsUserMap[userId].userName;
                var deptId = existsUserMap[userId].deptId;
                var $span = '<div userId="'+userId+'"  deptId="'+deptId+'" title="'+userName+'" onclick="removeBtn(this);" class="userDivClass">\n' +
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
                $("#showUserDiv").html(btnList);
            }
        }
        $("#showUserDiv").html(btnList);
        $('#evaluationModal').modal('hide');
    });
     init();
    $("#search").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    })

    $("#addBtn").click(function () {
        existsUserMap = {};
        $("#selectedDept").empty();
        document.getElementById("editForm").reset();
        $("#editModal").modal("toggle");
        $("#releaseUser1").val(user.name);
        $("#publishDeptName1").val(user.deptName);
        $("#id").val("");
        $(".save").show();
        $(".update").hide();
        $("#showUserDiv").empty();
    });

    $("#selDept").click(function () {
        $("#deptModal").modal('toggle');

    });
    $("#treeview").treeview({
        data: [getTreeData()],
        onNodeSelected: function (event, data) {
            // console.log(data);
            $.ajax({
                type: "post",
                url: baseUrl + "/dept/childs/" + data.id,
                // data: {operationDeptId: $("#id").val(), deptId: data.id},
                dataType: "json",
                success: function (retData) {
                    if (retData.code == 200) {
                        var list = retData.data.list;
                        if (list != null) {
                            $(list).each(function (i, item) {
                                var html = '<div id="row_' + item.id + '" class="col-md-2 tdd" style="margin: 3px 0;" ><div style="border: 1px solid #eeece4;padding: 5px 0">' +
                                    '<input type="hidden"  name="deptIds" id="deptId_' + item.id + '" value="' + item.id + '">' +
                                    '<input type="hidden"  id="deptName_' + item.id + '" value="' + item.name + '">' + item.name + '' +
                                    '&nbsp;&nbsp;<button type="button" id="button_"+' + item.id + ' onclick="delDept(' + item.id + ')" ' +
                                    'style="padding: 0px 5px;margin-right: 5px" class="btn btn-outline btn-sm btn-danger">X</button></div></div>';
                                $("#selectedDept").append(html);
                            });
                        }

                        $("#deptModal").modal('hide');
                        $("#query_table_logs").jqGrid('setGridParam', {
                            postData: $("#searchForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            });
        }
    });



})
function tast(t) {
    $("#evaluationModal").modal('toggle');
    $("#nameQc").val("");
    renderUserPage(t);
}


function init() {
    $("#query_table_logs").jqGrid({
        url: baseUrl + "/dict/listPg",
        datatype: "json",
        // postData:$("#user").serializeJson(), //发送数据
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
            {name: 'id', label: 'id', hidden: true, width: 60, sortable: false},
            {name: 'desc',label: '税种名称', editable: true, width: 40},
            {name: 'name', label: '税种编码', editable: true, width: 40},
            {name: 'code', label: '税点', editable: true, width: 40},
            {name: 'type', label: '换算比', editable: true, width: 40},
            {name: 'createUser', label: '创建人', editable: true, width: 40},
            {name: 'createTime', label: '创建时间', editable: true, width: 40,formatter: function (d) {
                return new Date(d).format("yyyy-MM-dd hh:mm:ss");
            }},
            {name: 'updateUser', label: '更新人', editable: true, width: 40},
            {name: 'updateTime', label: '更新时间', editable: true, width: 40,formatter: function (d) {
                return new Date(d).format("yyyy-MM-dd hh:mm:ss");
            }},
            {name: 'assistantName',label:'助理负责人',editable: true, width:40},
            {name: 'deptName',label:'所属部门',editable: true, width:120},
            {name: 'operate',label:'操作',index :'',width :40,
                formatter: function (value, grid, rows, state) {
                    var html = "";
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='edit(" + rows.id + ")'>编辑&nbsp;&nbsp;</a>";
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='del(" + rows.id + ")'>删除</a>";
                    return html;
                }}
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "抬头列表",
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
function getTreeData() {
    var deptTreeData = {};
    $.ajax({
        type: "POST",
        url: baseUrl + "/dept/listForTreeViewByCompanyCode",
        dataType: "json",
        async: false,
        success: function (result) {
            var arrays = result.data.list;
            deptTreeData = arrays[0];
        }
    });
    // console.log(JSON.stringify(deptTreeData))
    return deptTreeData;
}

//移除人员
function removeBtn(t) {
    var userId = $(t).attr("userId");
    $("#userId"+userId).remove();
    $("#userName"+userId).remove();
    $(t).remove();
    delete existsUserMap[userId];
}
function submitHander(t, url, state) {
    if(!existsUserMap || Object.getOwnPropertyNames(existsUserMap).length <= 0){
        layer.msg("税种必须要选择财务助理负责人！", {time: 1000, icon: 5});
        return;
    }
    if ($("#editForm").valid()) {
        var data=$("#editForm").serializeJson();
        if(data.hasOwnProperty("inputUserId") && !Array.isArray(data.inputUserId)){
            var array = [];
            array.push(data.inputUserId);
            data.inputUserId = array;
        }
        var tips;
        if (state == 0) {
            $("#state").val(state);
            tips = "确认保存？";
            // var formData = new FormData($("#editForm")[0]);
        }  else {
            $("#state").val(state);
            tips = "确认保存？";
            // var formData = new FormData($("#editForm")[0]);
        }


        if(data.hasOwnProperty("deptIds") && !Array.isArray(data.deptIds)){
            var array = new Array();
            array.push(data.deptIds);
            data.deptIds = array;
        }
        layer.confirm(tips, {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            //有图片添加传参


            $.ajax({
                type: "post",
                url: url,
                data: JSON.stringify(data),
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        // $("#query_table_logs").jqGrid('setGridParam', {
                        //     postData: $("#queryForm").serializeJson(), //发送数据
                        // }).trigger("reloadGrid"); //重新载入
                        $("#query_table_logs").reloadCurrentData(baseUrl+ "/dict/listPg",$("#searchForm").serializeJson(),"json",null,null);
                        $("#editModal").modal('hide');
                        $("#viewModal").modal('hide');
                    } else {
                        if (getResCode(data))
                            return;
                        $("#editModal").modal('hide');

                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                    if (getResCode(data))
                        return;
                }
            });
        }, function () {
            return;
        });
    }

}
function delDept(deptId) {
    // console.log("#row_"+deptId)
    $.ajax({
        type: "post",
        url: baseUrl + "/dict/delDept",
        data: {deptId: deptId,dictId: $("#id").val()},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                // let divNodes = $("#selectedDept div");
                // if(divNodes && divNodes.length  > 0){
                //     for(let i = 0; i < divNodes.length; i++){
                //         if(divNodes[i].id === "row_"+deptId){
                //             divNodes[i].remove();
                //         }
                //     }
                // }
                if (data.data.list != null) {


                    for (var i = 0; i < data.data.list.length; i++) {
                        $("#row_" + data.data.list[i].id).remove();
                    }
                    // 获取所有数据的列；
                    var tdElements = $("#selectedDept").find(".tdd");
                    // 清空原表格；
                    $("#selectedDept").empty();
                    // 获取数据长度；
                    var length = tdElements.length;
                    // 如果还有数据则重新排序；
                    if (length > 0) {
                        // 拼接表格；
                        // var width = length >= 6 ? 100 : length % 6 * 15;
                        var width = 100;
                        var html = "<table style='margin-bottom: 15px;' width='" + width + "%'></table>";
                        $("#selectedDept").append(html);
                        // 获取表格对象；
                        var tableElement = $("#selectedDept > table");
                        tdElements.each(function (i, tdElement) {
                            if (i == 0) {
                                tableElement.append("<tr></tr>");
                                tableElement = $("#selectedDept > table").find("tr").last();
                            }
                            if (i > 0 && i % 6 == 0) {
                                tableElement.parent().append("<tr></tr>");
                                tableElement = $("#selectedDept > table").find("tr").last();
                            }
                            tableElement.append($(tdElement).clone());
                        });
                    } else {
                        // 隐藏提示；
                        $("#selectedDept").parent().next().hide();
                    }
                }

                $("#query_table_logs").jqGrid('setGridParam', {
                    postData: $("#searchForm").serializeJson(), //发送数据
                }).trigger("reloadGrid"); //重新载入
            } else {
                if (getResCode(data))
                    return;
            }
        },

    });
}
function edit (id){
    existsUserMap = {}; //清空已选人员
    $("#editModal").modal('toggle');
    $(".save").hide();
    $("#showUserDiv").empty();
    $(".update").show();
    $("#editModal").find("input[name='label']").removeAttr("checked");
    $("#editModal").find("input[name='label']").parent().removeClass("checked");
    $("#selectedDept").empty();
    $.ajax({
        type: "post",
        url: baseUrl + "/dict/editAjax",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.entity){
                $("#editModal [name=" + attr + "][type!='radio'][type!='checkbox']").val(data.data.entity[attr]);
                $("#editModal input [name=" + attr + "]").text(data.data.entity[attr]);
                if (attr == "label") {
                    var labelValue = data.data.entity[attr];
                    if (labelValue.indexOf(",") > 0) {
                        var labels = labelValue.split(",");
                        for (var i = 0; i < labels.length; i++) {
                            $("#editModal").find("input[name='label'][value='" + labels[i] + "']").attr("checked", "checked");
                            $("#editModal").find("input[name='label'][value='" + labels[i] + "']").parent().addClass("checked");
                        }
                    } else {
                        $("#editModal").find("input[name='label'][value='" + labelValue + "']").attr("checked", "checked");
                        $("#editModal").find("input[name='label'][value='" + labelValue + "']").parent().addClass("checked");
                    }
                }
            }
            var arr = data.data.list;
            var length = arr.length;
            if (length > 0) {
                var width = length >= 6 ? 100 : length % 6 * 15;
                var html = "<table style='margin-bottom: 15px;' width='" + width + "%'><tr>";
                for (var i = 0; i < length; i++) {
                    html += '<div id="row_' + arr[i].id + '" class="col-md-2 tdd" style="margin: 3px 0;"><div style="border: 1px solid #eeece4;padding: 5px 0">' +
                        '<input type="hidden"  name="deptIds"  id="deptId_' + arr[i].id + '" value="' + arr[i].id + '">' +
                        '<input type="hidden"  id="deptName_' + arr[i].id + '" value="' + arr[i].name + '">' + arr[i].name + '' +
                        '&nbsp;&nbsp;<button type="button" id="button_"+' + arr[i].id + ' onclick="delDept(' + arr[i].id + ')" ' +
                        'style="padding: 0px 5px;margin-right: 5px" class="btn btn-outline btn-sm btn-danger">X</button></div></div>';
                    if (i > 0 && i < length - 1 && ((i + 1) % 6 == 0)) {
                        html += "</tr><tr>";
                    }
                }
                html += "</tr></table>";
                $("#selectedDept").append(html);
            }
            if (data.data.listUser && data.data.listUser.length>0){
                var btnList = "";
                $.each(data.data.listUser,function (i,keyword) {
                    var userId = keyword.assistantUserId;
                    var name= keyword.name;
                    var deptId = user.deptId;
                    existsUserMap[userId]={userName: name, deptId: deptId};
                    var $span = '<div userId="'+userId+'"  deptId="'+deptId+'" title="'+name+'" onclick="removeBtn(this);" class="userDivClass">\n' +
                        '            <div class="userClass">'+name+'</div>\n' +
                        '            <div title="删除" class="deleteClass">\n' +
                        '                <i class="fa fa-trash"></i>\n' +
                        '            </div>\n' +
                        '        </div>';
                    var inputUserId = '<input id="userId'+userId+'" name="inputUserId" type="hidden" value="' + userId + '">';
                    var inputUserName = '<input id="userName'+userId+'" name="inputUserName" type="hidden" value="' + name + '">';
                    $span +=inputUserId;
                    $span +=inputUserName;
                    btnList += $span;

                })
                $("#showUserDiv").append(btnList);

            }

        }
    });

}

function del(id) {
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: baseUrl + "/dict/del",    //向后端请求数据的url
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    // $("#query_table_logs").jqGrid('setGridParam', {
                    //     postData: $("#queryForm").serializeJson(), //发送数据
                    // }).trigger("reloadGrid"); //重新载入
                    $("#query_table_logs").reloadCurrentData(baseUrl + "/dict/listPg", $("#searchForm").serializeJson(), "json", null, null);
                } else {
                    if (getResCode(data))
                        return;
                }
            }
        });
    }, function () {
        return;
    });
}

function renderUserPage(t) {
    var rootDom = $("#evaluationModal");
    var listUser = "/taxUser/assistantUser?name=" + $("#nameQc").val().trim();
    requestData(null,listUser,"get","json",false,function (data) {
        userList = groupBy(data,function (item) {
            return [item.deptId];
        });
        var html = template("excludePeopleHtml",{'data':userList});
        rootDom.find("div[data-id = 'groups']").html(html);
        reloadICheck(rootDom);

        rootDom.find(".deptSpan").on('ifChecked',function () {
            var userInputArr = $(this).parent().parent().parent().next().find("input[type='checkbox']");
            if (userInputArr && userInputArr.length>0){
                $.each(userInputArr,function (index,t) {
                    $(t) .iCheck('check');
                    existsUserMap[$(t).attr('userId')] = {userName: $(t).attr('userName'), deptId: $(t).attr('deptId')};
                })
            }
        });
        rootDom.find(".deptSpan").on('ifUnchecked',function () {
            var userInputArr = $(this).parent().parent().parent().next().find("input[type='checkbox']");
            if (userInputArr && userInputArr.length>0){
                $.each(userInputArr, function (index,t) {
                    $(t) .iCheck('uncheck');
                    delete existsUserMap[$(t).attr('userId')];
                })

            }

        });
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
        $(".userSpan").on('ifClicked', function () {
            var flag = $(this).is(':checked');
            if(flag){
                delete existsUserMap[$(this).attr('userId')];
            }else {
                existsUserMap[$(this).attr('userId')] = {userName: $(this).attr('userName'), deptId: $(this).attr('deptId')};

            }
        });
            })
    if(existsUserMap && Object.getOwnPropertyNames(existsUserMap).length > 0){
        for(var key in existsUserMap){
            $("#evaluationModal").find("div[data-id='groups'] input[userId='" + key + "']").iCheck("check");
        }
    }
}

var requestData = function (data,url,requestType,dataType,async,callBackFun,contentType) {
    var param = {
        type:requestType,
        url:baseUrl+url,
        data:data,
        dataType:dataType,
        async:async,
        success:callBackFun
    };
    if (contentType){
        param.contentTypt='application/json;charset=utf-8'
    }
    $.ajax(param);
}

function groupBy(array,f) {
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

function reloadICheck(root) {
    root.find('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass:'iradio_square-green'
    });
}