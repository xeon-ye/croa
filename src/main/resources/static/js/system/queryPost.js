$(function () {
    //加载职位信息
    loadDeptPost();
    //添加职位
    $("#addPostBtn").click(function () {
        document.getElementById("editPostForm").reset();
        $("#editPostForm").find("input").removeClass('error');
        $("#editPostForm").validate().resetForm();
        $("#editPostModal").modal("toggle");
        $("#editPostForm #postRemark").empty();
        $(".save").show();
        $(".update").hide();
        $("#editPostForm #id2").val("");
        $("#companyCode2").val(user.companyCode);
        $("#createTime").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
    });

    $("#deptPostSearch").click(function () {
        $("#dept_post_table").emptyGridParam();
        $("#dept_post_table").jqGrid('setGridParam', {
            postData: {companyCode:null,postNameQC:$("#postNameQC").val()}, //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
});

/**
 * 新增编辑核实职位是否重复
 * @param id
 * @param companyCode
 */
function checkPost(){
    var flag = false;
    var id = $("#id2").val();
    var name = $("#PostNames").val();
    if(name!=null && name!=""){
        $.ajax({
            type:"post",
            url:"/user/getPostInfo",
            data:{name:name,id:id},
            dataType:"json",
            async:false,
            success:function (data) {
                if(data.data.flag==2){
                    flag=true;
                }else{
                    //已存在该职位
                    flag=false;
                }
            }
        });
    }else {
        //职位为空不进行去重处理
        flag = true;
    }
    return flag;
}

/**
 * 判断是否可以删除职位
 * @param id
 */
function checkDelPost(id){
    var flag = false;
        $.ajax({
            type:"post",
            url:"/user/findDelPostById",
            data:{id:id},
            dataType:"json",
            async:false,
            success:function (data) {
                if(data.data.flag==1){
                    flag=true;
                }else{
                    flag=false;
                }
            }
        });
    return flag;
}

/**
 * 新增编辑职位
 * @param t
 * @param url
 */
function submitHander2(t,url){
    if($("#editPostForm").valid()){
        if(checkPost()){
            layer.confirm("请确认用户信息", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                var formData = $("#editPostForm").serializeForm();
                var name =  $("#postNameQC").val();
                $.ajax({
                    type: "post",
                    url: url,
                    data: formData,
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            $("#dept_post_table").jqGrid("setGridParam", {
                                postData: {companyCode:null,postNameQC:name}
                            }).trigger("reloadGrid");
                            $("#editPostModal").modal("hide");
                        }
                    }
                });
            }, function () {
                return;
            })
        }else{
            swal("该职位已存在");
        }
    }else{
        return;
    }
}

/**
 * 编辑职位
 * @param id
 */
function editPost(id){
    document.getElementById("editPostForm").reset();
    $("#editPostForm").find("input").removeClass('error');
    $("#editPostForm").validate().resetForm();
    $("#editPostModal").modal("toggle");
    $(".update").show();
    $(".save").hide();
    var deptId = $("#editForm #id").val();
    $("#editPostForm #deptId2").val(deptId);
    var companyCode = $("#addForm #companyCode1").val();
    $("#editPostForm #companyCode2").val(companyCode);
    $.post("/user/getPostById",{id:id},function (data) {
        for(var attr in data.data.entity){
            $("#editPostForm input[name='"+attr+"']").val(data.data.entity[attr]);
            if(attr=="remark"){
                $("#editPostForm #postRemark").html(data.data.entity[attr]);
            }
        }
    });
}

/**
 * 查看职位
 * @param id
 */
function viewPost(id){
    document.getElementById("viewForm").reset();
    $("#viewModal").modal("toggle");
    $(".update").show();
    $(".save").hide();
    $.post("/user/getPostById",{id:id},function (data) {
        for(var attr in data.data.entity){
            $("#viewForm input[name='"+attr+"']").val(data.data.entity[attr]);
            if(attr=="remark"){
                $("#viewForm #remark").html(data.data.entity[attr]);
            }
        }
    });
}

/**
 * 职位删除
 */
function delPost(id){
    if(checkDelPost(id)){
        layer.confirm('确认删除？', {
            btn: ['删除', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: baseUrl + "/user/delDeptPost",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#dept_post_table").emptyGridParam() ;
                        $("#dept_post_table").jqGrid('setGridParam', {
                            postData: {companyCode:$("#editForm #companyCode").val(),postNameQC:$("#deptPostForm #postNameQC").val()}, //发送数据
                        }).trigger("reloadGrid"); //重新载入
                    }
                }
            });
        }, function () {
            return;
        });
    }else {
        swal("该职位已与部门关联，不能删除");
    }
}

/**
 * 部门职位显示
 */
function loadDeptPost(){
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $("#dept_post_table").jqGrid({
        url:'/user/getCompanyPost',
        datatype: "json",
        mtype: 'POST',
        postData: {companyCode:null,postNameQC:$("#postNameQC").val()}, //发送数据
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
        multiselect: true,
        // multiboxonly: true,只能单选
        multiselectWidth: 25, //设置多选列宽度
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
        colModel: [
            {name: 'id', label: '职位id', hidden:true, editable: true, width: 30},
            {name: 'name', label: '职位', editable: true, width: 80},
            {name: 'code', label: '职位编码', editable: true, width: 60},
            {name: 'remark', label: '职位描述', editable: true, width: 120},
            {name: 'create_time', label: '创建时间', editable: true, width: 100,
                formatter:function (d) {
                    if(!d){
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {name: 'operation', label: '操作', editable: true, width: 100,
                formatter:function (value, grid, rows, state) {
                    var html = "";
                    html+= "<a href='javascript:void(0)' onclick='editPost("+rows.id+")'>编辑</a>&nbsp;&nbsp;";
                    html+= "<a href='javascript:void(0)' onclick='delPost("+rows.id+")'>删除</a>";
                    return html;
                }
            }
        ],
        pager: "#dept_post_pager",
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            viewPost(rowid);
        }
    });
}
