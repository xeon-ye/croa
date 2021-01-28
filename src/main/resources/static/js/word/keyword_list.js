var existsKeywordMap = {}; //存储原有关键字集合，格式：{keyId : keyName}
var tempKeywordArry = [];//修改添加关键字临时集合，格式{name：name}
var tempDeleteArry = [];
$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#table_keywords_list");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });
    createTable(); //创建表格
    //初始化群组类型
    initGroupType();

    $("#cance3").click(function () {
        $("#saveKeywordModal").modal("hide");
    });

    $("#cance4").click(function () {
        $("#editKeywordModal").modal("hide");
    });

    // saveKeywordModal垂直居中显示
    $('#saveKeywordModal').on('shown.bs.modal', function (e) {
        // 关键代码，如没将modal设置为 block，则$modala_dialog.height() 为零
        $(this).css('display', 'block');
        var modalHeight=$(window).height() / 2 - $('#saveKeywordModal .modal-dialog').height() / 2;
        $(this).find('.modal-dialog').css({
            'margin-top': modalHeight
        });
    });

    // editKeywordModal垂直居中显示
    $('#editKeywordModal').on('shown.bs.modal', function (e) {
        // 关键代码，如没将modal设置为 block，则$modala_dialog.height() 为零
        $(this).css('display', 'block');
        var modalHeight=$(window).height() / 2 - $('#editKeywordModal .modal-dialog').height() / 2;
        $(this).find('.modal-dialog').css({
            'margin-top': modalHeight
        });
    });

    //增加菜单绑定按钮
    $("#addBtn").click(function () {
        $("#addModal").modal("toggle");
        document.getElementById("editForm").reset();
        $("remarks").empty();
        $("#menuName").val($("#groupTypeText").text());
        $("#companyCode").val(user.companyCode);
        $("#words").val("");
        $("#remarks").val("");
        //只考虑一个菜单先写死0（客户查询）
        $("#menuId").val(0);
        $("#createName").val(user.name);
        $("#createId").val(user.id);
        $("#createTime").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
    });
});

//初始化群组类型
function initGroupType() {
        var html = "<div id='groupType' class='col-md-1'><span class='btn btn-outline btn-primary' title='客户查询' data-value='0' onclick='setType(null,this)'>客户查询</span></div>";
        // html += " <button id='addBtnGroup' style='margin-left: 10px;position: absolute;top: 0px;right: 10px;z-index: 10;' type=\"button\" class=\"col-md-1 btn btn-primary btn-circle glyphicon glyphicon-plus \" onclick=\"addGroupType(this)\"></button>";
        // html += " <button id='del' style='margin-left: 10px;position: absolute;top: 0px;right: -30px;z-index: 10;' type=\"button\" class=\"col-md-1 btn btn-primary btn-circle glyphicon glyphicon-minus\" onclick=\"delGroupType(this)\"></button>";
        $("#groupType").html(html);
        $("#groupType>div:first-child>span:first-child").click();
}

/**
 * 展示添加关键字模态框
 * @param t
 */
function addTempWord(t) {
    $("#saveKeywordModal").modal("toggle");
    //判读是否添加是用到
    $("#groupId").val($("#id2").val());
    $("#wordName").val("");
}

/**
 * 储存临时关键字集合（去重1、集合去重，2、与老数据去重）
 */
function addKeyword() {
    var tempWord = $.trim($("#wordName").val());
    if($("#addKeywordForm").valid()) {
        layer.confirm('确认保存？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            if (checkKeyword()) {
            var html = "";
            //临时关键字去重
                var $span = '<div data-add="1" data-name="'+tempWord+'"  class="userDivClass">\n' +
                    '<input name="inputKeyName" type="hidden" value="' + tempWord + '">' +
                    '            <div data-name="'+tempWord+'" onclick="editBtn(this)" class="userClass">' + tempWord + '</div>\n' +
                    '            <div title="删除"  data-name="'+tempWord+'" onclick="removeBtn(this)"  class="deleteClass">\n' +
                    '                <i class="fa fa-trash"></i>\n' +
                    '            </div>\n' +
                    '        </div>';
                //var inputKeyName = '<input name="inputKeyName" type="hidden" value="' + tempWord + '">';
               // $span += inputKeyName;
                html += $span;
                $("#showKeywordsDiv").append(html);
                $("#saveKeywordModal").modal("hide");
            } else {
                swal("存在该关键字，添加失败");
            }
        }, function () {
            return;
        });
    }
}
//判断关键字 是否重复
function checkKeyword() {
    var flag = false;
    var keyName = "";
      keyName = $("#wordName").val();
     $("#showKeywordsDiv > div").each(function (i, t) {
        var word= $(t).attr("data-name");
        if (keyName == word){
            flag = false;
        }else {
            flag = true;

        }

     });
    return flag;

}
//判断关键字 是否重复
function checkKeyword1() {
    var flag = false;
    var keyName = "";
    keyName = $("#wordName2").val();
    $("#showKeywordsDiv > div").each(function (i, t) {
        var word= $(t).attr("data-name");
        if (keyName == word){
            flag = false;
        }else {
            flag = true
        }
    });

    return flag;
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
    $("#groupTypeId").val(0);
    reflushTable();//刷新表格
}

//表格定义
function createTable() {
    $("#table_keywords_list").jqGrid({
        url: baseUrl + "/menuWords/listPg",
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
            {name:'menu_name',label:'菜单名称',hidden:false,width:20,sortable:true},
            {name:'permission_type',label:'关键字类型',hidden:false,width:30,sortable:false,
            formatter:function (d) {
                if(d==0){
                    return "屏蔽";
                }else{
                    return "允许";
                }
            }},
            {name:'remarks',label:'关键字描述',hidden:false,width:30,sortable:false},
            {name:'keyword',label:'关键字',hidden:false,width:150,sortable:false},
            {name: 'operate',label:'操作',index :'',width :40,sortable:false,
                formatter: function (value, grid, rows, state) {
                    var html = "";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='edit(" + rows.id + ")'>编辑&nbsp;&nbsp;</a>";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='del(" + rows.id + ")'>删除</a>";
                    return html;
                }}
        ],
        pager: jQuery("#pager_keywords_list_list"),
        viewrecords: true,
        caption: "关键字列表",
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
    $("#keywords").val($("#groupName").val());
    //刷新表格
    $("#table_keywords_list").emptyGridParam();
    $("#table_keywords_list").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//删除关键字
function del(id) {
    layer.confirm('确认删除？', {
        btn: ['删除', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $.ajax({
            type: "post",
            url: baseUrl + "/menuWords/del",    //向后端请求数据的url
            data: {id: id},
            dataType: "json",
            success: function (data) {
                if(data.code==200){
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    reflushTable(); //刷新表格
                }
            }
        });
    }, function () {
        return;
    });
}

/**
 * 添加关键字操作
 * @param t
 * @param url
 */
function saveKeyword(t,url) {
    if ($("#addForm").valid()){
        if(checkRepeat()) {
            var formData = $("#addForm").serializeJson();
            layer.confirm("确定保存", {
                btn: ['确定', '取消'],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);
                $.ajax({
                    type: "post",
                    url: url,
                    data: formData,
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            reflushTable();
                            $("#addModal").modal('hide');
                        } else {
                            if (getResCode(data))
                                return;
                            $("#addModal").modal('hide');
                        }
                    },
                    error:function () {
                        Ladda.stopAll();//隐藏加载按钮
                    }
                })
            })
        }else{
            swal("屏蔽，允许规则只能存在一条");
        }
    }
}

/**
 * 修改关键字操作
 * @param t
 * @param url
 */
function editKeyword(t,url) {
    if ($("#editForm").valid()){
        //判断添加值是否重复
        var formData = $("#editForm").serializeJson();
        if(formData.hasOwnProperty("inputKeyId") && !Array.isArray(formData.inputKeyId)){
            var array =[];
            array.push(formData.inputKeyId);
            formData.inputKeyId=array;
        }
        if(formData.hasOwnProperty("inputKeyName") && !Array.isArray(formData.inputKeyName)){
            var array =[];
            array.push(formData.inputKeyName);
            formData.inputKeyName=array;
        }
        layer.confirm("确定保存", {
            btn: ['确定', '取消'],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);
            $.ajax({
                type: "post",
                url: url,
                data: JSON.stringify(formData),
                dataType: "json",
                contentType: 'application/json;charset=utf-8',
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        reflushTable();
                        $("#editModal").modal('hide');
                    } else {
                        if (getResCode(data))
                            return;
                        $("#editModal").modal('hide');
                    }
                },
                error:function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            })
        })
    }
}

/**
 * 修改临时关键字
 */
function editAjax() {
    var tempWord = $.trim($("#wordName2").val());
    var id = $("#keyId2").val();
    if($("#editKeywordForm").valid()) {
        layer.confirm('确认保存？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            if (checkKeyword1()) {
                for (var keyId in existsKeywordMap) {
                    // if (tempWord != existsKeywordMap[keyId].keyName) {
                        //临时关键字去重
                        if(tempKeywordArry.length==0){
                            $("#keyName"+id).val(tempWord);
                            $("#key"+id).text(tempWord);
                            $("div[data-id='"+id+"']").attr("data-name",tempWord);
                            //设定修改标志
                            if(tempWord!=existsKeywordMap[keyId]){
                                $("div[data-id='"+id+"']").attr("data-edit",1);
                            }
                            existsKeywordMap[id].keyName=tempWord;
                        }else if(tempKeywordArry.length>0){
                            if(tempKeywordArry.indexOf(tempWord)==-1){
                                $("#keyName"+id).val(tempWord);
                                $("#key"+id).text(tempWord);
                                $("div[data-id='"+id+"']").attr("data-name",tempWord);
                                if(tempWord!=existsKeywordMap[keyId].keyName){
                                    $("div[data-id='"+id+"']").attr("data-edit",1);
                                }
                                //修改集合为id的关键字名称
                                existsKeywordMap[id].keyName=tempWord;
                            }
                        }
                    // } else {
                    //     swal("存在该关键字，修改失败");
                    // }
                }
                $("#editKeywordModal").modal("hide");
            } else {
                swal("存在该关键字，修改失败");
            }
        }, function () {
            return;
        });
    }
}

/**
 * 判断关键字屏蔽允许是否存在
 * @returns {boolean}
 */
function checkRepeat() {
    var flag = false;
    $.ajax({
        type:"post",
        url:"/menuWords/checkRepeat",
        data:$("#addForm").serializeJson(),
        dataType:"json",
        async:false,
        success:function (data) {
            if(data==false){
                flag = false;
            }else{
                flag = true;
            }
        }
    })
    return flag;
}

//移除关键字
function removeBtn(t) {
    layer.confirm('确认删除？', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        var $parentDiv = $(t).parent();
            $parentDiv.remove();
        layer.close(index);
    }, function () {
        return;
    });
}

//跳转到修改关键字页面
function editBtn(t) {
    var keyId = $(t).attr("data-id");
    if(keyId!=null && keyId!="" && keyId!=undefined){
        $("#wordName2").val($(t).attr("data-name"));
        $("#groupId2").val($("#id2").val());
        $("#keyId2").val(keyId);
        $("#editKeywordModal").modal("toggle");
    }else{
        swal("新增的关键字不能立即修改");
    }
}

// 编辑功能菜单回显数据
function edit(id) {

    existsKeywordMap = {}; //清空数据库缓存数据
    tempKeywordArry = []; //清空缓存数据
    tempDeleteArry = []; //清空缓存数据
    $("#editModal").modal('toggle');
    $("#createName2").val(user.name);
    $("#editModal").find("input").removeAttr("checked");
    $("#editModal").find("input").parent().removeClass("checked");
    $("#showKeywordsDiv").empty();
    $("#permissionType").val("");
    $.ajax({
        type:"post",
        url:baseUrl + "/menuWords/getById",
        dataType:"json",
        data:{id:id},
        success:function (data){
            if(data.code == 200){
                if(data.data.entity){

                    for (var attr in data.data.entity){
                        if (attr == "permissionType") {
                            $("#editForm").find("select[name='permissionType']").val(data.data.entity[attr]);
                        }if (attr == "remarks"){

                            $("#remarks2").val(data.data.entity[attr]);

                        }
                        else {
                            $("#editModal input[name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                        }

                    }
                }
                if(data.data.list && data.data.list.length){
                    var btnList = "";
                    $.each(data.data.list, function (i, keyword) {
                        var keyId = keyword.id ;
                        var name = keyword.name;
                        existsKeywordMap[keyId] = name;
                        var $span = '<div data-id="'+keyId+'" data-name="'+name+'"  class="userDivClass">\n' +
                            '<input id="keyId'+keyId+'" name="inputKeyId" type="hidden" value="' + keyId + '">' +
                            '<input id="keyName'+keyId+'" name="inputKeyName" type="hidden" value="' + name + '">' +
                            '            <div data-id="'+keyId+'" data-name="'+name+'" title="'+name+'" onclick="editBtn(this)" class="userClass"  id="key'+keyId+'">'+name+'</div>\n' +
                            '            <div data-id="'+keyId+'" data-name="'+name+'" onclick="removeBtn(this);" title="删除" class="deleteClass">\n' +
                            '                <i class="fa fa-trash"></i>\n' +
                            '           </div>\n'+
                            '        </div>'
                        // var inputKeyId = '<input id="keyId'+keyId+'" name="inputKeyId" type="hidden" value="' + keyId + '">';
                        // var inputKeyName = '';
                        // $span  += inputKeyId;
                        // $span  += inputKeyName;
                        btnList += $span;
                    })
                    $("#showKeywordsDiv").html(btnList)
                }
            }
        }
    });
}
