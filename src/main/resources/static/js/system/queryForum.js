$(function () {
    $("#forumSearch").click(function () {
        $("#query_forum_table").emptyGridParam();
        $("#query_forum_table").jqGrid("setGridParam", {
            postData: $("#queryForum").serializeJson()
        }).trigger("reloadGrid");
    });

    var fileUploadObj=new FileUpload({
        targetEl: '#word2htmlForm',
        acceptSuffix:["gif","jpeg","png","jpg"],
        multi: false,
        filePart: "article",
        completeCallback: function (data) {
            if (data.length > 0)//oriName
            $("#affixLink").val(data[0].file);
            $("#affixName").val(data[0].oriName);
            $("#showFile").attr("href",data[0].file);
            $("#showName").html(data[0].oriName);
        }
    });

    $("#word2html").click(function () {
        fileUploadObj.upload();
    });

    $("#addForum").click(function () {
        $("#addForm").find("input").removeClass('error');
        $("#addForm").validate().resetForm();
        document.getElementById("addForm").reset();
        $("#affixLink").val("");
        $("#affixName").val("");
        $("#showFile").attr("href","");
        $("#showName").html("");
        $("#addModal").modal("toggle");
        $(".save").show();
        $(".update").hide();
        $("#userId").val(user.id);
        $("#userName").val(user.name);
        $("#createTime").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
        $("#forumCompanyCode").val(user.dept.companyCodeName);
        $("#companyCodeId").val(user.companyCode);
        $("#remark").html("");
        $("#moderator").empty();
        //默认选中胡姐
        loadUser(100193);
        // loadCompany("#forumCompanyCode",null);
    });

    /**
     * 论坛版块信息
     */
    $("#query_forum_table").jqGrid({
        url: baseUrl + '/forum/listPg',
        datatype: "json",
        postData: $("#queryForum").serializeJson(), //发送数据
        mtype: "post",
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: false,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [25, 50, 150],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [
            {name: 'id', label: '编号', editable: true, hidden: true, width: 60},
            {name: 'user_id', label: '创建人id', editable: true,hidden: true, width: 40},
            {name: 'name', label: '板块名称', editable: true, width: 80},
            {name: 'chargeMan', label: '板块负责人', editable: true, width: 60},
            {name: 'user_name', label: '创建人', editable: true, width: 60},
            {name: 'moderator', label: '负责人id', hidden: true,editable: true, width: 60},
            // {name: 'companyCodeName', label: '公司', editable: true,hidden: true, width: 60},
            {name: 'create_time', label: '创建时间', editable: true, width: 80,
                formatter:function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }},
            {name: 'update_time', label: '更新时间', editable: true, width: 80,
                formatter:function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {name: 'remark', label: '介绍', editable: true,width: 80},
            {
                name: 'operate', label: "操作", index: '', width: 80,
                formatter: function (value, grid, rows, state) {
                     var html = "<a style='color: #00e266' onclick='edit(" + rows.id + ")'>编辑</a>&nbsp;&nbsp;";
                     html += "<a style='color: #bd362f' onclick='del(" + rows.id + ")'>删除</a>&nbsp;&nbsp;";
                     return html;
                }
            }
        ],
        pager: jQuery("#query_forum_pager"),
        viewrecords: true,
        caption: "论坛板块信息",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            view(rowid);
        },
    });
});

/**
 * 长度限制
 */
function checkContent(t) {
    var len = $(t).val().length;
    if(len>=400){
        $("#showError").text("备注长度必须小于400字符");
        $("#showError").addClass("error");
        $(t).addClass("error");
    }else{
        $("#showError").text("");
        $("#showError").removeClass("error");
        $(t).removeClass("error");
    }
}

function loadUser(t){
    $("#moderator").empty();
    $("#moderator2").empty();
    layui.use(["form"], function (){
        $.get("/propose/listByForum",{companyCode:user.companyCode},function (data) {
            var html="";
            html += "<option value=''>请选择</option>";
            for (var i = 0; i < data.data.number; i++) {
                var selected = data.data.list[i].id == t ? "selected=selected" : "";
                html+= "<option value='"+data.data.list[i].id+"' "+selected+">"+data.data.list[i].name+"("+data.data.list[i].deptName+")</option>"
            }
            $("#moderator").append(html);
            $("#moderator2").append(html);
            layui.form.render();
        },"json");
    });
}

function view(id){
    $("#viewModal").modal("toggle");
    $.ajax({
        type:"post",
        url:"/forum/getById",
        data:{id:id},
        dataType:"json",
        success:function (data) {
            for (var attr in data.data.entity) {
                //范围太大会让具有相同name的input赋值
                $("#viewForm input[name='"+attr+"']").val(data.data.entity[attr]);
                if(attr=="companyCodeName"){
                    $("#viewForm #forumCompanyCode1").val(data.data.entity["companyCodeName"]);
                }
                if(attr=="moderator"){
                    loadUser(data.data.entity["moderator"]);
                }
                if(attr=="affixName"){
                    $("#viewForm #showName1").html(data.data.entity["affixName"]);
                }
                if(attr=="affixLink"){
                    $("#viewForm #showFile1").attr("href",data.data.entity["affixLink"]);
                }
                $("#viewForm #userName1").val(data.data.entity["userName"]);
                $("#viewForm #name1").val(data.data.entity["name"]);
                $("#viewForm #createTime1").val(data.data.entity["createTime"]);
                $("#viewForm #remark1").html(data.data.entity["remark"]);
            }
        }
    });
}

// function loadCompany(t,code){
//     $.post(baseUrl + "/dept/findAllCompany",{level:1}, function (data) {
//         $(t).append("<option value=''>请选择</option>");
//         $(data).each(function (i, d) {
//             var selected = d.companyCode==code?"selected=selected":"";
//             $(t).append("<option value='"+d.companyCode+"'"+selected+">" + d.companyCodeName + "</option>");
//         });
//     }, "json");
// }

function edit(id){
    $("#addForm").find("input").removeClass('error');
    $("#addForm").validate().resetForm();
    document.getElementById("addForm").reset();
    $(".save").hide();
    $(".update").show();
    $("#addModal").modal("toggle");
    $("#remark").html("");
    $("#affixLink").val("");
    $("#affixName").val("");
    $("#showFile").attr("href","");
    $("#showName").html("");
    $.ajax({
        type:"post",
        url:"/forum/getById",
        data:{id:id},
        dataType:"json",
        success:function (data) {
            if(data.code==200){
                for (var attr in data.data.entity) {
                    //范围太大会让具有相同name的input赋值
                    $("#addForm input[name='"+attr+"']").val(data.data.entity[attr]);
                    if(attr=="companyCodeName"){
                        $("#addForm #forumCompanyCode").val(data.data.entity["companyCodeName"]);
                    }
                    if(attr=="companyCode"){
                        $("#addForm #companyCodeId").val(data.data.entity["companyCode"]);
                    }
                    if(attr=="moderator"){
                        loadUser(data.data.entity["moderator"]);
                    }
                    if(attr=="affixName"){
                        $("#addForm #showName").html(data.data.entity["affixName"]);
                        $("#addForm #affixName").html(data.data.entity["affixName"]);
                    }
                    if(attr=="affixLink"){
                        $("#addForm #showFile").attr("href",data.data.entity["affixLink"]);
                        $("#addForm #affixLink").attr("href",data.data.entity["affixLink"]);
                    }
                    $("#addForm #userName").val(data.data.entity["userName"]);
                    $("#addForm #name").val(data.data.entity["name"]);
                    $("#addForm #createTime").val(data.data.entity["createTime"]);
                    $("#addForm #remark").html(data.data.entity["remark"]);
                }
            }
        }
    });
}

/**
 * 判断论坛板块是否重复
 * @returns {boolean}
 */
function hasForum() {
    var flag = false;
    $.ajax({
        type: "post",
        url: "/forum/hasForum",
        data: {companyCode:$("#companyCodeId").val(),name:$("#name").val(),id:$("#id").val()},
        dataType: "json",
        async:false,
        success: function (data) {
            if(data==1){
                flag = true;
            }else{
                flag = false;
                layer.msg("论坛板块不能重复");
            }
        }
    });
return flag;
}

/**
 * 检查板块负责人不为空
 * @returns {boolean}
 */
function checkEmpty(){
    var flag = false;
    var moderator = $("#moderator").val();
    if(moderator!=null && moderator!=""){
        flag = true;
    }else{
        layer.msg("请选择板块负责人");
        flag = false;
    }
    return flag;
}

/**
 * 判断是否可以删除
 * @param id
 * @returns {boolean}
 */
function checkDelete(id) {
    var flag=false;
    $.ajax({
        type:"post",
        url:"/topic/findByForumId",
        data:{forumId:id},
        dataType:"json",
        //同步防止走两个线程
        cache:false,
        async:false,
        success:function (data) {
            if(data.data.number>0){
                //板块下有帖子，不允许删除
                flag = false;
                swal("该论坛板块下有帖子不可删除","","warning");
            }else {
                flag = true;
            }
        }
    })
    return flag;
}

/**
 * 删除方法
 * @param id
 */
function del(id) {
    if(checkDelete(id)){
        layer.confirm('确认删除？', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: baseUrl + "/forum/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_forum_table").jqGrid("setGridParam", {
                            postData: $("#queryForum").serializeJson()
                        }).trigger("reloadGrid");
                    } else {
                        swal(data.msg);
                    }
                }
            });
        }, function () {
            return;
        });
    }
};


/**
 * 添加编辑提交方法
 * @param t
 * @param url
 * @param state
 */
function submitForm(t, url) {
    if ($("#addForm").valid() && hasForum() && checkEmpty()) {
        layer.confirm("请确认论坛板块信息", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            var formData = $("#addForm").serializeJson();
            $.ajax({
                type: "post",
                url: url,
                data: formData,
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_forum_table").jqGrid("setGridParam", {
                            postData: $("#queryForum").serializeJson()
                        }).trigger("reloadGrid");
                        $("#addModal").modal("hide");
                    } else {
                        swal(data.msg);
                        $("#addModal").modal("hide");
                    }
                }
            });
        }, function () {
            return;
        })
    } else {
        return;
    }
}
