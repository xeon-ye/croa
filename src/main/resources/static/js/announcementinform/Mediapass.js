var addNewsContentObj;
$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_Announcement_list').setGridWidth(width);
    });

    //新闻富文本编辑
    addNewsContentObj = new KindeditorTool({
        targetEl: "#newsContent",
        uploadUrl: "/editUpload?filePart=news",
    });

    //获取权限
    commonObj.getTrainPermission();

    mediaPassObj.courseRemake = KindEditor.create($("#content"),{
        items:[
            'source', '|', 'undo', 'redo', '|', 'preview', 'print', 'template', 'code', 'cut', 'copy', 'paste',
            'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
            'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
            'superscript', 'clearhtml', 'quickformat', 'selectall', '|', 'fullscreen', '/',
            'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
            'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image', 'multiimage',
            'flash', 'media', 'insertfile', 'table', 'hr', 'emoticons', 'baidumap', 'pagebreak',
            'anchor', 'link', 'unlink', '|', 'about'
        ],
        uploadJson:"/editUpload?filePart=library",
        fileManagerJson:'',
        allowFileManager: true
    })


    //判断是否有权限
    if(!commonObj.permisstionMap || Object.getOwnPropertyNames(commonObj.permisstionMap) < 1){
        layer.msg("没有权限！", {time: 3000, icon: 5});
        closeCurrentTab();
        return;
    }

    //控制Tab展示
    commonObj.controlTab();

    //获取外部跳转链接
    var pageTab = getQueryString("pageTab");
    if(pageTab){
        //通知公告
        if(pageTab == "mediaPassTab" && commonObj.permisstionMap["mediaPass"]){
            mediaPassObj.initBefore();
            commonObj.tabChange(0);
        }
        //新闻资讯
        if(pageTab == "newsTab" && commonObj.permisstionMap["newsManage"]){
            commonObj.tabChange(1);
        }
    }else {
        //通知公告
        if(commonObj.permisstionMap["mediaPass"]){
            mediaPassObj.initBefore();
            commonObj.tabChange(0);
        }else if(commonObj.permisstionMap["newsManage"]){
            commonObj.tabChange(1);
        }
    }
});

//通知公告
var mediaPassObj = {
    courseRemake:null,
    initBefore:function () {
        //人事行政显示新增按钮
        if (mediaPassObj.hasRoleXZRS()) {
            $("#addBtn").show();
        } else {
            $("#addBtn").hide();
        }

        $(".i-checks").on('ifChange', function (event) {
            alert($(this).val());
        });

        //flag=1审核，否则查看
        if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
            var id = getQueryString("id");
            var flag = getQueryString("flag");
            if (flag == 1) {
                mediaPassObj.view(id,1);
            } else {
                mediaPassObj.view(id,2);
            }
        }

        //加载部门树
        mediaPassObj.loadDeptTree();

        //加载表格
        mediaPassObj.createTable();
    },
    init: function () {
        $("#table_Announcement_list").emptyGridParam();
        $("#table_Announcement_list").jqGrid('setGridParam', {
            postData: $("#searchForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    },
    loadDeptTree:function () {
        $("#treeview").treeview({
            data: [mediaPassObj.getTreeData()],
            onNodeSelected: function (event, data) {
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
                                    var html = '<div id="row_' + item.id + '" class="col-md-2 tdd" style="margin: 3px;" >' +
                                        '<div style="border: 1px solid #eeece4;padding: 5px 0;display:  flex;align-items:  center;justify-content:  center;">' +
                                        '<input type="hidden"  name="deptIds" id="deptId_' + item.id + '" value="' + item.id + '">' +
                                        '<input type="hidden"  id="deptName_' + item.id + '" value="' + item.name + '">' +
                                        '<span>'+item.name+'&nbsp;&nbsp;</span>' +
                                        '<button type="button" id="button_"+' + item.id + ' onclick="mediaPassObj.delDept(' + item.id + ')" ' +
                                        'style="padding: 0px 5px;margin-right: 5px" class="btn btn-outline btn-sm btn-danger">X</button></div></div>';
                                    $("#selectedDept").append(html);
                                });
                            }
                            $("#deptModal").modal('hide');
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    }
                });
            }
        });
    },
    createTable:function () {
        $("#table_Announcement_list").jqGrid({
            url: baseUrl + "/Mediapass/notificationlist",
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
            multiselect: false,
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
            colModel: [
                {name: 'id', label: 'id', hidden: true, width: 60, sortable: false},
                {name: 'no', label: '通知编号', editable: true, width: 60},
                {name: 'title', label: '标题', editable: true, width: 60},
                {
                    name: 'transactionType', label: '事务类型', width: 30,
                    formatter: function (value, grid, rows) {
                        switch (rows.transactionType) {
                            case 1:
                                return "通知";
                            case 2:
                                return "公示";
                            case 3:
                                return "分享";
                            case 4:
                                return "提问";


                        }

                    }
                },
                {name: 'releaseUser', label: '公布人', editable: true, width: 40},
                {name: 'releaseTime', label: '发布时间',editable: true, width: 40,
                    formatter: function (d) {
                        if(d != null)
                        {return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                        }else {
                            return "";
                        }
                    }},
                // {name: 'releaseTimeEnd', label: '发布截止时间', width: 40,
                //     formatter: function (d) {
                //         if(d != null)
                //         {return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                //         }else {
                //             return "";
                //         }
                //     }
                // },
                {
                    name: 'resourceType', label: '资源类型', editable: true, width: 40,
                    formatter: function (value, grid, rows) {
                        switch (rows.resourceType) {
                            case 1:
                                return "web页面";

                            case 2:
                                return "文件";

                            case 3:
                                return "图片";

                            case 4:
                                return "插图";

                            case 5:
                                return "电影";

                            case 6:
                                return "书籍";

                        }
                    }
                },
                {name: 'label', label: '标签', editable: true, width: 30},
                {
                    name: 'state', label: '资源状态', editable: true, width: 40,
                    formatter: function (value, grid, rows) {
                        if (value == -9) {
                            return '<b style="color: orange;">失效</b>';
                        } else if (value == 0) {
                            return '<b style="color: blue;">已保存</b>';
                        } else if (value == 4) {
                            return '<b style="color: red;">已发布</b>';
                        } else if (value == 1) {
                            return '<b style="color: #0d8ddb ">归档</b>';
                        } else {
                            return "";
                        }
                    }
                },
                {
                    name: 'operate', label: "操作", index: '', width: 120,
                    formatter: function (value, grid, rows) {
                        var html = "";
                        if (rows.createId == user.id && rows.state!=-9) {
                            html += "<a href= 'javascript:void(0)' style='height:22px;width:40px;' onclick='mediaPassObj.del(" + rows.id + ")'>删除&nbsp;</a>";
                            if (rows.state == 0 || rows.state == 1) {
                                html += "<a href= 'javascript:void(0)' style='height:22px;width:40px;' onclick='mediaPassObj.edit(" + rows.id + ")'>编辑&nbsp;</a>";
                            }
                        }
                        return html;
                    }
                }

            ],
            pager: jQuery("#pager_Announcement_list"),
            viewrecords: true,
            caption: "公告通知列表",
            add: false,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false,
            gridComplete: function () {
                // 单选框居中；
                $(".cbox").addClass("icheckbox_square-green");
                var width = $('.jqGrid_wrapper').width();
                $('#table_Announcement_list').setGridWidth(width);
            },
            ondblClickRow: function (rowid, iRow, iCol, e) {
                //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
                mediaPassObj.view(rowid,0);
            },
        });
        $("#table_Announcement_list").setGridHeight(440);
    },
    addBtnClick:function () {
        $("#selectedDept").empty();
        document.getElementById("editForm").reset();
        $("#mandatory2").iCheck('check');
        $("#editModal").modal("toggle");
        $("#releaseUser1").val(user.name);
        $("#affixDiv1").empty();
        $("#affixDiv").empty();
        mediaPassObj.courseRemake.html("")
        $("#publishDeptName1").val(user.deptName);
        $(".save").show();
        $(".update").hide();
    },
    selDeptClick:function () {
        $("#deptModal").modal('toggle');
    },
    del:function (id) {
        layer.confirm('确认删除？', {
            btn: ['删除', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: baseUrl + "/Mediapass/del",    //向后端请求数据的url
                data: {id: id},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#table_Announcement_list").reloadCurrentData(baseUrl + "/Mediapass/notificationlist", $("#searchForm").serializeJson(), "json", null, null);
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            });
        }, function () {
            return;
        });
    },
    edit:function (id) {
        $("#editModal").modal('toggle');
        $(".save").hide();
        $(".update").show();
        $("#editModal").find("input[name='label']").removeAttr("checked");
        $("#editModal").find("input[name='label']").parent().removeClass("checked");
        $("#editModal input:radio").removeAttr("checked");
        $("#editModal input:radio").parent().removeClass("checked");
        $("#selectedDept").empty();
        $.ajax({
            type: "post",
            url: baseUrl + "/Mediapass/editAjax",
            data: {id: id},
            dataType: "json",
            success: function (data) {
                for (var attr in data.data.entity) {
                    if (attr == "transactionType") {
                        $("#editForm").find("select[name='transactionType']").find("option[value='" + data.data.entity[attr] + "']").attr("selected", "selected");
                    }else if (attr == "content"){
                        mediaPassObj.courseRemake.html(data.data.entity["content"]);
                    }
                    else if (attr == "resourceType") {
                        $("#editForm").find("select[name='resourceType']").find("option[value='" + data.data.entity[attr] + "']").attr("selected", "selected");
                    } else {
                        $("[name=" + attr + "][type!='radio'][type!='checkbox']").val(data.data.entity[attr]);
                        $("input [name=" + attr + "]").text(data.data.entity[attr]);
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
                        if (attr == "releaseTime" || attr == "releaseTimeEnd") {
                            var dateValue = data.data.entity[attr];
                            if (dateValue) {
                                $("#editModal").find("input[name='" + attr + "']").val(new Date(dateValue).format("yyyy-MM-dd hh:mm:ss"));
                            }
                        }
                        if(attr=="mandatory"){
                            $("#editForm input[name='mandatory'][value='" + data.data.entity[attr] + "']").prop("checked", "checked");
                            $("#editForm input[name='mandatory'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                        }
                        if (attr=="attachment"){
                            $("#affixDiv").empty();
                            $("#affixDiv").show();
                            if (data.data.entity[attr] === "") continue;
                            var affixName = data.data.entity[attr].split(',');
                            var affixLink = data.data.entity["attachmentLink"].split(",");
                            if (affixName.length>0 && affixLink.length>0){
                                var html= "";
                                for (var i=0; i<affixName.length ; i++){
                                    var filePath = affixLink[i];
                                    var fileName = affixName[i];
                                    if (fileName === "" || filePath === "") continue;
                                    html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                                    html += "<a href=" + filePath + " target=_blank download='" + fileName + "'>下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                    var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                                    var strFilter = ".jpeg|.gif|.jpg|.png|.bmp|.pic|";
                                    var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                                    if (fileName.indexOf(".") > -1) {
                                        var str = fileExt + '|';
                                        if (strFilter.indexOf(str) > -1) {//是图片
                                            html += "<img alt='" + fileName + "' src='" + filePath + "' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                        } else {
                                            if(fileExtArray.contains(fileExt)){
                                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                            }
                                        }
                                    } else {
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                    }
                                }
                                $("#affixDiv").append(html);
                            }
                        }
                        // if (attr == "mandatory") {
                        //     $("input[name='mandatory']").attr("disabled", "disabled");
                        //     $("input[name='mandatory'][value='" + data.data.entity[attr] + "']").attr("checked", "checked");
                        //     $("input[name='mandatory'][value='" + data.data.entity[attr] + "']").parent().addClass("checked");
                        // }
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
                            '&nbsp;&nbsp;<button type="button" id="button_"+' + arr[i].id + ' onclick="mediaPassObj.delDept(' + arr[i].id + ')" ' +
                            'style="padding: 0px 5px;margin-right: 5px" class="btn btn-outline btn-sm btn-danger">X</button></div></div>';
                        if (i > 0 && i < length - 1 && ((i + 1) % 6 == 0)) {
                            html += "</tr><tr>";
                        }
                    }
                    html += "</tr></table>";
                    $("#selectedDept").append(html);
                }
                $("#releaseUser1").val(user.name);
                $("#publishDeptName1").val(user.deptName);
            }
        });
    },
    submitHander:function (t, url, state) {
        mediaPassObj.courseRemake.sync();
        if ($("#editForm").valid()) {
            var tips;
            if (state == 0) {
                $("#state").val(state);
                tips = "确认保存？";
                var formData = new FormData($("#editForm")[0]);
            } else if (state == 1) {
                $("#state").val(state);
                tips = "请确认通知信息？提交后不能取消";
                var formData = new FormData($("#viewForm")[0]);
            } else {
                $("#state").val(state);
                tips = "确认保存？";
                var len = document.getElementsByName("deptIds").length;
                if(len>0){
                    var formData = new FormData($("#editForm")[0]);
                }else {
                    swal("请选择生效部门");
                    return;
                }

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
                    data: formData,
                    dataType: "json",
                    async: true,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            $("#table_Announcement_list").reloadCurrentData(baseUrl + "/Mediapass/notificationlist", $("#searchForm").serializeJson(), "json", null, null);
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
    },
    view:function (id,flag) {
        //双击查看
        if (flag == 0){
            $(".save").hide();
        }
        //审批确认
        else if (flag == 1){
            $(".save").show();
        }
        else if (flag == 2){
            $(".save").hide();
        }

        $("#viewModal").modal('toggle');
        $.ajax({
            type: "post",
            url: baseUrl + "/Mediapass/editAjax",
            data: {id: id},
            dataType: "json",
            success: function (data) {
                for (var attr in data.data.entity) {
                    if (attr == "transactionType") {
                        $("#transactionType2").attr("disabled", "disabled");
                        $("#viewForm").find("select[name='transactionType']").find("option[value='" + data.data.entity[attr] + "']").attr("selected", "selected");
                    } else if (attr == "content"){
                        $("#content1").html(data.data.entity[attr]);
                    }
                    else if(attr == "title"){
                        //查看的标题抬头
                        $("[name=" + attr + "]").text(data.data.entity[attr]);
                        $("#title").html(data.data.entity[attr]);
                    }
                    else {
                        $("[name=" + attr + "][type!='radio']").val(data.data.entity[attr]);
                        //console.log(data.data.entity['transactionType']);
                        //查看的标题抬头
                        //$("[name=" + attr + "]").text(data.data.entity[attr]);
                        if (attr == "releaseTime") {
                            var dateValue = data.data.entity[attr];
                            if (dateValue) {
                                $("#viewModal").find("input[name='" + attr + "']").val(new Date(dateValue).format("yyyy-MM-dd hh:mm:ss"));
                            }
                        }
                        if (attr=="attachment"){
                            $("#affixDiv1").empty();
                            $("#affixDiv1").show();
                            if (data.data.entity[attr] === "") continue;
                            var affixName = data.data.entity[attr].split(',');
                            var affixLink = data.data.entity["attachmentLink"].split(",");
                            if (affixName.length>0 && affixLink.length>0){
                                var html= "";
                                for (var i=0; i<affixName.length ; i++){
                                    var filePath = affixLink[i];
                                    var fileName = affixName[i];
                                    if (fileName === "" || filePath === "") continue;
                                    html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                                    html += "<a href=" + filePath + " target=_blank download='" + fileName + "'>下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                    var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                                    var strFilter = ".jpeg|.gif|.jpg|.png|.bmp|.pic|";
                                    var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                                    if (fileName.indexOf(".") > -1) {
                                        var str = fileExt + '|';
                                        if (strFilter.indexOf(str) > -1) {//是图片
                                            html += "<img alt='" + fileName + "' src='" + filePath + "' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                        } else {
                                            if(fileExtArray.contains(fileExt)){
                                                html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                            }
                                        }
                                    } else {
                                        html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                    }
                                }
                                $("#affixDiv1").append(html);
                            }
                        }
                    }
                }
            }
        });
    },
    getTreeData:function () {
        var deptTreeData = {};
        $.ajax({
            type: "POST",
            url: baseUrl + "/dept/listForTreeView",
            dataType: "json",
            async: false,
            success: function (result) {
                var arrays = result.data.list;
                deptTreeData = arrays[0];
            }
        });
        return deptTreeData;
    },
    delDept:function (deptId) {
        $.ajax({
            type: "post",
            url: baseUrl + "/Mediapass/delDeptAccountDept",
            data: {operationDeptId: deptId, deptId: $("#id").val()},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    if (data.data.list && data.data.list.length > 0) {
                        for (var i = 0; i < data.data.list.length; i++) {
                            $("#row_" + data.data.list[i].id).remove();
                        }
                    }
                } else {
                    if (getResCode(data))
                        return;
                }
            },

        });
    },
    hasRoleXZRS:function () {
        var len = user.roles.length;
        for (var i = 0; i < len; i++) {
            if (user.roles[i].type == 'XZ' || user.roles[i].type == 'RS') {
                return true;
            }
        }
        return false;
    }
}

//新闻资讯
var newsObj = {
    getTotalUrl:"/news/getNewsTotal",
    newsListUrl:"/news/admin",
    newsType:{1:"集团新闻", 2:"公司新闻", 3:"图文新闻", 4:"优秀事迹"},
    newsTypeCls:{1:"jituan", 2:"gongsi", 3:"", 4:"youxiu"},
    newsListCallback: function (data, target, seq) {
        var html = "";
        if(data && data.list && data.list.length > 0){
            $.each(data.list, function (i, record) {
                if(record && record.id){
                    seq += 1;
                    html += "<tr>\n" +
                        "       <td >\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "               <input value='"+record.id+"' type=\"checkbox\" lay-skin=\"primary\">\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\""+seq+"\">\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "               "+seq+"\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\""+(newsObj.newsType[record.type] || "")+"\">\n" +
                        "           <div class=\"ellipsisContent "+(newsObj.newsTypeCls[record.type] || "")+"\">\n" +
                        "               "+(newsObj.newsType[record.type] || "")+"\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\""+(record.title || "")+"\">\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "                <a href=\"/news/admin/view/"+record.id+"\">"+(record.title || "")+"</a>\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\""+(record.createTime || "")+"\">\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "               "+(record.createTime || "")+"\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "       <td title=\"操作\">\n" +
                        "           <div class=\"ellipsisContent\">\n" +
                        "                <button class=\"tableButton blueBtn\" type=\"button\" onclick='newsObj.editClick("+JSON.stringify(record)+");'>\n" +
                        "                    修改\n" +
                        "                </button>\n" +
                        "                <button id='del"+record.id+"' class=\"tableButton orangeBtn\" type=\"button\" onclick='newsObj.del(this,"+record.id+");'>\n" +
                        "                    删除\n" +
                        "                </button>\n" +
                        "           </div>\n" +
                        "       </td>\n" +
                        "    </tr>";
                }
            });
        }
        var $parentList = target ? $(target) : $("#courseExamList");
        $parentList.html(html);
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    init: function () {
        //全选按钮初始化
        $("#newsAllChoose").removeAttr("checked");

        var param = $("#newsQueryForm").serializeJson();
        //初始化分页组件
        commonObj.requestData(param,newsObj.getTotalUrl, "post", "json", true, function (data) {
            if(data && data.code == 200){
                commonObj.pagerPlus({
                    elem: $("#newsPager"),
                    count: data.data.total,
                    url: newsObj.newsListUrl,
                    target: $(".newsList"),
                    param: param,
                },newsObj.newsListCallback);
            }
        });
    },
    addClick:function () {
        $("#modalTitle").text("新增新闻");
        $("#newsId").val("");
        $("#newsTitle").val("");
        addNewsContentObj.setContent("");
        $("#saveBtn").css("display", "inline-block");
        $("#editBtn").css("display", "none");
        $("#addNews").modal("toggle");
    },
    add:function () {
        var content = addNewsContentObj.getContent();
        if(!content){
            layer.msg("新闻内容不能为空！", {time: 3000, icon: 5});
            return;
        }
        var param = $("#newsForm").serializeJson();
        param.content = content || "";
        startModal("#saveBtn");
        commonObj.requestData(param, "/news", "post", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code === 200){
                layer.msg("新增成功！", {time: 3000, icon: 6});
                $("#addNews").modal("toggle");
                newsObj.init();
            }else{
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    },
    editClick:function (news) {
        $("#modalTitle").text("编辑新闻");
        $("#newsId").val(news.id);
        $("#newsTitle").val(news.title);
        $("#newsType").val(news.type);
        addNewsContentObj.setContent(news.content);
        $("#saveBtn").css("display", "none");
        $("#editBtn").css("display", "inline-block");
        $("#addNews").modal("toggle");
        layui.use('form', function(){
            var form = layui.form;
            form.render();
        });
    },
    edit:function () {
        var param = $("#newsForm").serializeJson();
        if(!param.id){
            layer.msg("新闻唯一标识不能为空！", {time: 3000, icon: 5});
            return;
        }
        var content = addNewsContentObj.getContent();
        if(!content){
            layer.msg("新闻内容不能为空！", {time: 3000, icon: 5});
            return;
        }
        param.content = content || "";
        startModal("#editBtn");
        commonObj.requestData(param, "/news/edit", "post", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code === 200){
                layer.msg("编辑成功！", {time: 3000, icon: 6});
                $("#addNews").modal("toggle");
                newsObj.init();
            }else{
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    },
    del:function (t, id) {
        startModal("#" + $(t).attr("id"));
        commonObj.requestData(null, "/news/" + id, "delete", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code === 200){
                layer.msg("删除成功！", {time: 3000, icon: 6});
                newsObj.init();
            }else{
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    },
    batchDel:function () {
        var $tbody = $(".newsList");
        var $inputArr = $tbody.find('input[type="checkbox"]:checked');
        if($inputArr.length < 1){
            layer.msg("很抱歉，没有选中记录进行操作！", {time: 2000, icon: 5});
            return;
        }
        var ids = [];
        $inputArr.each(function (i, input) {
            ids.push($(input).val());
        });
        var param = {ids:ids};
        startModal("#batchDelNews");
        commonObj.requestData(param, "/news/batchDel", "post", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code === 200){
                layer.msg("删除成功！", {time: 3000, icon: 6});
                newsObj.init();
            }else{
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    }
}

//页面公共处理对象
var commonObj = {
    permisstionMap:{},
    tabs:{
        0:{id:"mediaPassWrap", tabId:"mediaPassTab", obj:mediaPassObj},
        1:{id:"newsWrap", tabId:"newsTab", obj:newsObj},
    },
    //后台请求方法
    requestData: function (data, url, requestType,dataType,async,callBackFun, callErrorFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun,
            error:function () {
                Ladda.stopAll();//隐藏加载按钮
            }
        };
        if(callErrorFun){
            param.error = callErrorFun;
        }
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    //获取用户权限
    getTrainPermission:function () {
        commonObj.requestData(null, "/Mediapass/getResourcePermission", "post", "json", false, function (data) {
            commonObj.permisstionMap = data;
        });
    },
    //根据权限控制tab栏目
    controlTab:function () {
        //有公告通知权限
        if(commonObj.permisstionMap && commonObj.permisstionMap["mediaPass"]){
            $("#mediaPassTab").css("display", "inline-block");
        }else {
            $("#mediaPassTab").css("display", "none");
        }

        //有新闻资讯权限
        if(commonObj.permisstionMap && commonObj.permisstionMap["newsManage"]){
            $("#newsTab").css("display", "inline-block");
        }else {
            $("#newsTab").css("display", "none");
        }
    },
    //Tab切换处理事件
    tabChange: function (index) {
        $(".tabContent").css("display","none");
        $(".layui-tab-title").find("li").removeClass("layui-this");
        if(commonObj.tabs[index]){
            $("#"+commonObj.tabs[index].id).css("display","block");
            $("#"+commonObj.tabs[index].tabId).addClass("layui-this");
            if(commonObj.tabs[index].obj){
                commonObj.tabs[index].obj.init(); //列表展示
            }
        }
    },
    //分页插件使用
    pagerPlus: function (config,callback,type) {
        layui.use('laypage', function(){
            var laypage = layui.laypage;

            //执行一个laypage实例
            laypage.render({
                elem: config.elem //注意，这里的 test1 是 ID，不用加 # 号
                ,count: config.count || 0, //数据总数，从服务端得到
                layout: ['count','prev','page','next','refresh','limit','skip'],
                hash: true,
                limits: config.limits || [15, 30, 50, 100],
                limit: config.limit || 15,
                jump: function (obj, first) {
                    config.param = config.param || {};
                    config.param.size = obj.limit;
                    config.param.page = obj.curr;
                    commonObj.requestData(config.param, config.url, "post", "json", true, function (data) {
                        if(callback){
                            if(config.target){
                                callback(data,config.target, (config.param.size * (config.param.page - 1)));
                            }else {
                                callback(data,null, (config.param.size * (config.param.page - 1)));
                            }
                        }
                    });
                }
            });
        });
    },
    //回车键事件
    enterEvent:function (tabIndex, methonName, event, sourceNode) {
        if((event.keyCode == '13' || event.keyCode == 13) && commonObj.tabs[tabIndex].obj && commonObj.tabs[tabIndex].obj[methonName]){
            if(sourceNode){
                commonObj.tabs[tabIndex].obj[methonName](sourceNode);
            }else {
                commonObj.tabs[tabIndex].obj[methonName]();
            }
        }
    },
}