var mediaUserMap = {}; //缓存媒介筛选条件获取的媒介，数据格式：{userId: user}
var sysConfigMap = {}; //系统配置功能
var mediaUserPlateMap = {};//媒介拥有板块
var deptId = "";
$(document).ready(function () {
    //业务部门菜单树
    $("#selDept").click(function () {
        $("#deptModal").modal('toggle');
    });

    //获取整个集团的业务部
    getRootDept();

    $('#treeview').treeview({
        data: [getTreeData()],
        onNodeSelected: function (event, data) {
            $("#deptId").val(data.id);
            $("#deptName").val(data.text);
            $("#deptModal").modal('hide');
            loadDeptUser(data.id,"YW", $("#userId"));
        }
    });

    $("#cleanDept").click(function () {
        $("#deptId").val("");
        $("#deptName").val("");
        $("#userId").val("")
        loadDeptUser(null,"YW", $("#userId"));
        //渲染表单
        layui.use(["form"], function () {
            layui.form.render();
        });
    });

    //媒介部门菜单树
    $("#mjDeptSel").click(function () {
        $("#mjDeptModal").modal('toggle');
    });
    loadMediaDeptTree();
    $("#mjDeptClear").click(function () {
        $("#mjDeptId").val("");
        $("#mjDeptName").val("");
        $("#mediaUserId").val("");
        loadMediaUser(null, mediaUserMap);
        //渲染表单
        layui.use(["form"], function () {
            layui.form.render();
        });
    });

    $("#refresh").click(function () {
        javascript:Business.mediaModalShow1()();
    });

    loadAllCompany1("#companyCode");

    $("#mTypeName2").change(function () {
        $("#addForm [name='mediaTypeId']").val($("#mTypeName2").val());
        $("#addForm [name='mediaTypeName']").val($("#mTypeName2").find("option:selected").text());
        // listMJByMediaId();
        Business.cleanMediaInfo1();//清空媒体信息
        listPriceTypeByMediaId();
    });

    $("#mediaUserNameSec").change(function () {
        $("#addForm [name='mediaUserId']").val($("#mediaUserNameSec").val());
        $("#addForm [name='mediaUserName']").val($("#mediaUserNameSec").find("option:selected").text());
    });

    $("#mediaSearch").click(function () {
        mediaGrid.search();
    });

    $("#mediaSupplierSearch").click(function () {
        mediaSupplierGrid.search();
    });

    mediaUserMap = {}; //缓存媒介筛选条件获取的媒介，数据格式：{userId: user}
    // 加载媒介；
    loadMediaUser(null, mediaUserMap);

    //每次进入页面，清空缓存
    Business.transferMediaUserMap = {};

    //请求系统参数
    requestData(null, "/sysConfig/getAllConfig", "get", "json", false, function (data) {
        //由于日期类型为数字需要格式处理
        for(var k in data){
            if(data[k].dataType == 'date' && data[k].pattern){
                data[k].value = new Date(data[k].value).format(data[k].pattern.replace(/H/g, "h"));
            }
        }
        sysConfigMap = data;
    });
    //根据配置决定是否展示稿件生成按钮,系统稿件生成策略：0-业务员下单模式、1-媒介创建模式、2-两种模式都兼容，没有配置默认兼容两种
    if(!sysConfigMap ||  !sysConfigMap["orderCreateModel"] || sysConfigMap["orderCreateModel"]["value"] != 0){
        $("#addBtn").css("display", "inline-block");
        $("#importBtn").css("display", "inline-block");
    }else {
        $("#addBtn").css("display", "none");
        $("#importBtn").css("display", "none");
    }
});

function getRootDept() {
    if(deptId===""){
        $.ajax({
            type: "POST",
            url: baseUrl + "/dept/getRootDept",
            data:null,
            dataType: "json",
            async: false,
            success: function (result) {
                deptId = result.data.root.id;
            }
        });
    }
}

//加载媒介部门菜单树
function loadMediaDeptTree() {
    if(deptId){
        $('#mjTreeview').treeview({
            data: [getTreeDataByDeptId(deptId)],
            onNodeSelected: function (event, data) {
                $("#mjDeptId").val(data.id);
                $("#mjDeptName").val(data.text);
                $("#mjDeptModal").modal('hide');
                loadMediaUser(data.id, mediaUserMap);
            }
        });
    }
}

//加载媒介筛选条件
function loadMediaUser(deptId, mediaUserMap) {
    var param = null;
    if(deptId){
        param = {deptId: deptId};
    }
    var html = '<option value="">媒介</option>';
    requestData(param, "/user/listPastMedia", "get", "json", false, function (data) {
        if (data) {
            $(data).each(function (i, d) {
                var value = $("#mediaUserName").attr("data-value");
                var selected = value == d.id ? "selected=selected" : "";
                html += "<option value='" + d.id + "' " + selected + ">" + d.name + "</option>";
                if(mediaUserMap){
                    mediaUserMap[d.id] = d;
                }
            });
        }
    });
    $("#mediaUserName").html(html);
    //渲染表单
    layui.use(["form"], function () {
        layui.form.render();
    });
}

//通过部门ID和角色类型获取用户
function loadDeptUser(deptId, roleType, $target) {
    var html = '<option value="">业务员</option>';
    $.ajax({
            url: baseUrl + "/user/listUserByDeptAndRole3",
            type: "post",
            data: {deptId: deptId, roleType: roleType},
            async: false,
            dataType: "json",
            success: function (users) {
                var userList = users.data.list;
                if(userList && userList.length > 0){
                    for (var i = 0; i < userList.length; i++) {
                        html += "<option value=" + userList[i].id + ">" + userList[i].name + "</option>";
                    }
                }
            }
        }
    );
    $target.html(html);
    //渲染表单
    layui.use(["form"], function () {
        layui.form.render();
    });
}

function calcuteUnitPriceAdd() {
    var outgoAmount = $("#addForm [name='outgoAmount']").val() == "" ? 0 : $("#addForm [name='outgoAmount']").val();
    var otherExpenses = $("#addForm [name='otherExpenses']").val() == "" ? 0 : $("#addForm [name='otherExpenses']").val();
    var num = $("#addForm [name='num']").val() == "" ? 1 : $("#addForm [name='num']").val();
    var unitPrice = (parseFloat(outgoAmount) - parseFloat(otherExpenses)) / parseInt(num);
    if (isNaN(unitPrice)){
        $("#addForm [name='unitPrice']").val(0);
    }else {
        $("#addForm [name='unitPrice']").val(parseFloat(unitPrice).toFixed(2));
    }

}

function calcuteUnitPrice() {
    var outgoAmount = $("#editForm [name='outgoAmount']").val() == "" ? 0 : $("#editForm [name='outgoAmount']").val();
    var otherExpenses = $("#editForm [name='otherExpenses']").val() == "" ? 0 : $("#editForm [name='otherExpenses']").val();
    var num = $("#editForm [name='num']").val() == "" ? 1 : $("#editForm [name='num']").val();
    var unitPrice = (parseFloat(outgoAmount) - parseFloat(otherExpenses)) / parseInt(num);
    if (isNaN(unitPrice)){
        $("#addForm [name='unitPrice']").val(0);
    }else {
        $("#editForm [name='unitPrice']").val(parseFloat(unitPrice).toFixed(2));

    }
}

function getTreeData() {
    var deptTreeData = {};
    $.ajax({
        type: "POST",
        url: baseUrl + "/dept/listAllDeptByIdAndCode",
        data:{deptId:deptId,deptCode:"YW"},
        dataType: "json",
        async: false,
        success: function (result) {
            var arrays = result.data.list;
            if (arrays != null && arrays.length > 0)
                deptTreeData = arrays[0];
        }
    });
    // console.log(JSON.stringify(deptTreeData))
    return deptTreeData;
}

//获取部门树数据
function getTreeDataByDeptId(deptId) {
    var deptTreeData = {};
    //具体查询
    requestData({deptId: deptId, deptCode: "MJ"}, "/dept/listAllDeptByIdAndCode", "post", "json", false, function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    })
    return deptTreeData;
}

/**
 * 加载当前用户拥有的板块
 * @param attribute
 */
function loadTypeByUserId(attribute) {
    $.ajax({
        url: baseUrl + "/mediaPlate/userId",  //mediaType/listByUserId
        data: {"userId": user.id},
        type: "post",
        dataType: "json",
        success: function (data) {
            if (data) {
                var mTypeEle = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
                for (var i = 0; i < data.length; i++) {
                    var mType = data[i];
                    mediaUserPlateMap[mType.id] = mType;
                    mTypeEle.append("<option value='${id}'>${name}</option>".replace("${id}", mType.id).replace("${name}", mType.name));
                }
            }
        }
    });
}

/**
 * 通过板块加载媒介
 * @param t
 */
function listMJByMediaId() {
    //新增媒体仅能给自己增加
    $("#mediaUserNameSec").html("<option value='" + user.id + "'>" + user.name + "</option>");
    $("#addForm [name='mediaUserId']").val(user.id);
    $("#addForm [name='mediaUserName']").val(user.name);
    /*var mediaTypeId = $("#mediaTypeId2").val()
    if (mediaTypeId == "") {
        return;
    } else {
        $("#mediaUserNameSec").empty();
        layui.use(["form"], function () {
            $.get(baseUrl + "/user/listPastMJByMediaTypeId2/" + mediaTypeId, function (data) {
                $("#mediaUserNameSec").append("<option value=''>请选择</option>");
                $(data.data.listMJByMediaTypeId).each(function (i, d) {
                    $("#mediaUserNameSec").append("<option value='" + d.id + "'>" + d.name + "</option>");
                });

                // 下拉框的onchange事件；
                layui.form.on("select(mediaUserFilter)", function (resultData) {
                    $("#addForm input[name='mediaUserId']").val(resultData.value);
                    $("#addForm input[name='mediaUserName']").val($("#mediaUserNameSec").find("option:selected").text());
                });
                layui.form.render();
            }, "json");
        });
    }*/
}

/**
 * 通过板块加载价格类型
 * @param t
 */
function listPriceTypeByMediaId() {
    var mediaTypeId = $("#mediaTypeId2").val()
    if (mediaTypeId == "") {
        return;
    } else {
        $("#priceType").empty();
        layui.use(["form"], function () {
            $.get(baseUrl + "/mediaForm1/listPriceTypeByPlateId/" + mediaTypeId, function (data) {
                $("#priceType").append("<option value=''>请选择</option>");
                $(data.data.mediaPrices).each(function (i, d) {
                    $("#priceType").append("<option value='" + d.cellName + "'>" + d.cellName + "</option>");
                });
                layui.form.render();
            }, "json");
        });
    }
}


var Business = {
    transferMediaUserMap: {}, //媒介缓存，数据格式：{plateId:[]}
    deleteArt: function (artId) {
        layer.confirm('您确定要删除吗？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                url: baseUrl + "/article/deleteArticle",
                type: "post",
                data: {artId: artId},
                dataType: "json",
                success: function (resData) {
                    if (resData.code == 200) {
                        grid.reflush();
                        // layer.msg(resData.data.message, {time: 1000, icon: 6});
                        // $("#table").reloadCurrentData(baseUrl + "/mediauser/list", $("#searchForm").serializeJson(), "json", null, function () {
                        // });
                    } else {
                        layer.alert(resData.msg);
                    }
                }
            });
        }, function () {
            return;
        });
    },
    alertEdit: function (url, title) {
        parent.layer.open({
            type: 2,
            title: title,
            shadeClose: false,
            shade: 0.8,
            area: ['60%', '40%'],
            content: url,
            end: function () {
                grid.reflush();
            }
        });
    },
    returnDown: function (artId) {
        layer.confirm('您确定要驳回该稿件吗？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function () {
            $.ajax({
                url: baseUrl + "/mediauser1/turnDown",
                type: "post",
                data: {id: artId},
                dataType: "json",
                success: function (resData) {
                    if (resData.code == 200) {
                        layer.msg("驳回成功！", {time: 3000, icon: 6});
                        grid.reflush();
                    } else {
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }
                }
            });
        }, function () {

        });
    },
    arrange: function (artId, orderId, artTitle) {
        layer.confirm('您确定要安排该稿件吗？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function () {
            $.ajax({
                url: baseUrl + "/mediauser1/arrange",
                type: "post",
                data: {id: artId, orderId: orderId, title: artTitle},
                dataType: "json",
                success: function (resData) {
                    if (resData.code == 200) {
                        layer.msg("安排成功！", {time: 3000, icon: 6});
                        grid.reflush();
                    } else {
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }
                }
            });
        }, function () {

        });
    },
    //弹出发布框
    publish: function (artId, link, issuedDate, title, outgoAmount) {
        var publishEditForm = $("#publishEditForm");
        //清除验证标签
        publishEditForm.find("input").removeClass('error');
        publishEditForm.validate().resetForm();
        document.getElementById("publishEditForm").reset();//无法清除隐藏域的值
        publishEditForm.find("[name='id']").val(artId || "");
        publishEditForm.find("[name='issuedDate']").val(new Date(issuedDate || new Date()).format("yyyy-MM-dd"));
        publishEditForm.find("[name='link']").val(link || "");
        publishEditForm.find("[name='title']").val(title || "");
        publishEditForm.find("[name='outgoAmount']").val(outgoAmount || "");
        publishEditForm.find("[name='updatePrice']").iCheck('uncheck');
        $("#publishEditModal").modal('toggle');
        //如果发布稿件时单价比对应媒体类型的价格单价浮动超过5%时，则询问是否同时修改媒体价格的审核
        //如果确定则发起修改媒体价格的审核
        $.ajax({
            url: "/mediauser1/priceFloat",
            data: {id: artId},
            dataType: "json",
            success: function (resData) {
                //不能更新报价
                if (!resData.data.b) {
                    $("[name='updatePrice']").attr("disabled", "disabled");
                } else {
                    $("[name='updatePrice']").removeAttrs("disabled");
                }
            }
        });
        $('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green'
        });
    },
    //执行发布操作
    zxPublish: function () {
        var formData = $("#publishEditForm").serializeJson();
        if ($("#publishEditForm").valid()) {
            $.ajax({
                url: baseUrl + "/mediauser1/publish",
                type: "post",
                data: formData,
                dataType: "json",
                success: function (resData) {
                    if (resData.code == 200) {
                        layer.msg("稿件发布成功！", {time: 3000, icon: 6});
                        grid.reflush();
                    } else {
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }
                    setTimeout("$('#publishEditModal').modal('toggle');", 1000);
                }
            });
        }
    },
    //设置对接人
    setSelectPeople: function (mTypeId) {
        $("#mediaUserId").empty();
        var dept = $("#transferDepartment option:selected").val();
        Views.listMJByMediaTypeId(mTypeId, "mediaUserId");
    },
    //设置交接部门
    setTransferDept: function () {
        Views.loadDept("transferDepartment");
        Business.setSelectPeople();
    },
    //移交
    yj: function (artId, mediaTypeId) {
        layer.open({
            type: 1,
            title: "稿件移交",
            zIndex: 10,
            content: $("#artTransferModal").html(),
            btn: ['确定','取消'],
            area: ['550px', '180px'],
            shadeClose: false,
            resize: false,
            move: '.layui-layer-title',
            moveOut: true,
            success: function(layero, index){
                $(layero[0]).find("input[name='artId']").val(artId || "");
                if(!Business.transferMediaUserMap[mediaTypeId] || Business.transferMediaUserMap[mediaTypeId].length < 1){
                    $.ajax(
                        {
                            url: baseUrl + "/user/listPastMJByMediaTypeId/" + mediaTypeId,
                            type: "get",
                            async: false,
                            dataType: "json",
                            success: function (resData) {
                                var users = resData.data.listMJByMediaTypeId;
                                Business.transferMediaUserMap[mediaTypeId] = users;
                            }
                        }
                    );
                }
                $(layero[0]).find("select[name='mediaUserId']").empty();
                if(Business.transferMediaUserMap[mediaTypeId] && Business.transferMediaUserMap[mediaTypeId].length > 0){
                    $(layero[0]).find("input[name='mediaUserName']").val(Business.transferMediaUserMap[mediaTypeId][0].name  || "");
                    $(Business.transferMediaUserMap[mediaTypeId]).each(function (i, item) {
                        $(layero[0]).find("select[name='mediaUserId']").append('<option value="'+item.id+'">' + item.name + '</option>');
                    });
                }
                //渲染表单
                layui.use('form', function(){
                    var form = layui.form;
                    form.render();

                    //媒介
                    form.on('select(mediaUserId)', function(data){
                        $(layero[0]).find("input[name='mediaUserName']").val($(data.elem).find("option:selected").text() || "");
                    });
                });
            },
            yes: function (index, layero) {
                $.ajax({
                    url: baseUrl + "/mediauser1/yj",
                    data: $(layero[0]).find("form").serializeJson(),
                    dataType: "json",
                    success: function (resData) {
                        if (resData.code == 200) {
                            layer.msg("移交成功！", {time: 3000, icon: 6});
                            grid.reflush();
                            layer.close(index);
                        } else {
                            layer.msg(data.msg, {time: 3000, icon: 5});
                        }
                    }
                });
            }
        });
    },
    batchDelete: function () {
        var totalList = grid.getAllPageSelected("artId");
        if (!totalList || totalList.length < 1) {
            layer.alert("请选择要删除的数据");
            return;
        }
        var datas = [];
        for (var i = 0; i < totalList.length; i++) {
            var o = totalList[i];
            if (o.issueStates == 4) {
                layer.alert("您选择了已发布的稿件，请重新选择");
                return;
            }
            var temp = {};
            temp.id = o.artId;
            datas.push(temp);
        }
        layer.confirm('您确定要删除该稿件吗？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                url: baseUrl + "/article/batchDelete",
                type: "POST",
                data: {datas: JSON.stringify(datas)},
                dataType: "json",
                async: false,
                success: function (respData) {
                    // layer.closeAll();
                    if (respData.code == 200) {
                        layer.msg(respData.data.message, {time: 1000, icon: 6});
                        grid.reflush();
                    } else {
                        layer.alert(respData.msg);
                    }
                    //重新加载表格
                    // grid.reflush();
                }
            });
        }, function () {
            return;
        });
    },
    exportArt: function () {
        var totalList = grid.getAllPageSelected("artId");
        if (!totalList || totalList.length <= 0) {
            swal("请选择要导出的数据");
            return;
        }
        var datas = [];
        for (var i = 0; i < totalList.length; i++) {
            var o = totalList[i];
            var temp = {};
            temp.id = o.artId;
            datas.push(temp);
        }
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        $("[name='datas']").val(JSON.stringify(datas));
        $("#exportForm").submit();
    },
    exportAll: function () {
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        var params =  removeBlank($("#searchForm").serializeJson());
        location.href = "/article/exportArticleMJ?" + $.param(params);
    },
    //设置统计数据
    setArticleResult: function () {
        $.ajax({
            url: baseUrl + "/article/articleResultMJ",
            data: $("#searchForm").serializeJson(),
            type: "post",
            dataType: "json",
            success: function (resData) {
                if (resData) {
                    for (var o in resData) {
                        // console.log(typeof resData[o])
                        $("#tj #" + o).text(resData[o] == "" ? 0 : resData[o].toMoney());
                    }
                } else {
                    $("#tj").find(".text-danger").htmleditForm(0);
                }
            }
        });
    },
    exportTemplate: function () {
        location.href = "/mediauser1/exportTemplate"; //mediaInfo/exportTemplate
    },
    importBtn: function () {
        // 清空文件；
        $("#file").val("");
        $("#importModal").modal('toggle');
    },
    batchImport: function () {
        if (document.getElementById("file").value == "") {
            swal("请选上传excel！");
        } else {
            var filePath = document.getElementById("file").value;
            var fileExt = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
            var flag = false;
            if (fileExt.match(/^(.xls|.xlsx)$/)) {
                flag = true;
            }
            if (flag) {
                startModal("#submitImportBtn");//锁定按钮，防止重复提交
                var formData = new FormData($("#importForm")[0]);
                $.ajax({
                    type: "post",
                    url: "/articleImport/batchOrderForEasyExcel",
                    data: formData,
                    dataType: "json",
                    async: true,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        Ladda.stopAll();
                        // console.log(data) ;
                        $("#importModal").modal('hide');
                        if (data.code == 200) {
                            $("#table").jqGrid('setGridParam', {
                                postData: $("#searchForm").serializeJson(), //发送数据
                            }).trigger("reloadGrid"); //重新载入
                            swal({title: data.data.message, type: "success"});
                        } else if (data.code == 1002) {
                            var str = "";
                            if (data.msg.length > 200) {
                                str = data.msg.slice(0, 200) + "...";
                            } else {
                                str = data.msg;
                            }
                            swal({
                                    title: "导入失败，成功导入0条！",
                                    text: str,
                                    type: "error",
                                    showCancelButton: true,
                                    confirmButtonColor: "#2a45dd",
                                    confirmButtonText: "下载失败详情！",
                                    cancelButtonText: "直接关闭！",
                                    closeOnConfirm: false,
                                    reverseButtons: true //控制按钮反转
                                },
                                function (isConfirm) {
                                    if (isConfirm) {
                                        var isIE = (navigator.userAgent.indexOf('MSIE') >= 0);
                                        if (isIE) {
                                            var strHTML = data.msg;
                                            var winSave = window.open();
                                            winSave.document.open("text", "utf-8");
                                            winSave.document.write(strHTML);
                                            winSave.document.execCommand("SaveAs", true, "导入失败详情.txt");
                                            winSave.close();
                                        } else {
                                            var elHtml = data.msg;
                                            var mimeType = 'text/plain';
                                            $('#createInvote').attr('href', 'data:' + mimeType + ';charset=utf-8,' + encodeURIComponent(elHtml));
                                            document.getElementById('createInvote').click();
                                        }
                                        swal.close();
                                    } else {
                                        swal.close();
                                    }
                                });
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    },
                    error: function (data) {
                        // console.log(data) ;
                        Ladda.stopAll();
                        if (getResCode(data))
                            return;
                    }
                });
            } else {
                swal("文件格式不正确，只能上传excel文件！");
            }
        }
    },
    addBtn: function () {
        // $("#mediaTypeId2").val("");
        // $("#mediaUserNameSec").empty();
        $("#priceType").empty();
        listMJByMediaId();
        $("#addForm").find("input").removeClass('error');
        $("#addForm").find("select").removeClass('error');
        $("#addForm").validate().resetForm();
        document.getElementById("addForm").reset();
        $("#addModal").modal({backdrop: "static"});
    },
    batchUpdateBtn: function () {
        var totalList = grid.getAllPageSelected("artId");
        if (!totalList || totalList.length < 1) {
            layer.alert("请选择要修改的数据");
            return;
        }

        var ids = [];
        for(var index in totalList){
            ids[index] = totalList[index].artId;
        }

        $("#batchUpdateIds").val(ids.join(','));
        $("#batchUpdateForm").find("input").removeClass('error');
        $("#batchUpdateForm").find("select").removeClass('error');
        $("#batchUpdateForm").validate().resetForm();
        document.getElementById("batchUpdateForm").reset();
        $("#batchUpdateModal").modal({backdrop: "static"});
    },
    mediaModalShow: function () {
        $("#opType").val(0);//编辑稿件选择媒体
        $("#plateId").val($("#mTypeId").val());
        mediaGrid.loadGrid();
        $("#media_table_logs").emptyGridParam();
        //根据是否标准板块决定是否展示唯一标识
        var colModels = $("#media_table_logs").jqGrid('getGridParam', 'colModel');
        $(colModels).each(function (j, colModel) {
            //如果是标准板块，则展示唯一标识
            if (colModel.name == "mediaContentId") {
                if(mediaUserPlateMap[$("#mTypeId").val()] && mediaUserPlateMap[$("#mTypeId").val()]["standarPlatformFlag"] == 1){
                    $("#media_table_logs").setGridParam().showCol(colModel.name);
                }else {
                    $("#media_table_logs").setGridParam().hideCol(colModel.name);
                }
            }
        });
        $("#media_table_logs").jqGrid('setGridParam', {
            postData: $("#mediaForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        $("#editModal").modal("hide");
        $("#mediaModal").modal({backdrop: "static"});
    },
    mediaModalShow1: function () {
        $("#opType").val(1);//新增稿件选择媒体
        if ($("#mediaTypeId2").val() == "") {
            layer.msg("请先选择板块！", {time: 2000, icon: 5});
            return;
        }
        $("#plateId").val($("#mediaTypeId2").val());
        mediaGrid.loadGrid();
        $("#media_table_logs").emptyGridParam();
        //根据是否标准板块决定是否展示唯一标识
        var colModels = $("#media_table_logs").jqGrid('getGridParam', 'colModel');
        $(colModels).each(function (j, colModel) {
            //如果是标准板块，则展示唯一标识
            if (colModel.name == "mediaContentId") {
                if(mediaUserPlateMap[$("#mediaTypeId2").val()] && mediaUserPlateMap[$("#mediaTypeId2").val()]["standarPlatformFlag"] == 1){
                    $("#media_table_logs").setGridParam().showCol(colModel.name);
                    $("#mediaContentIdWrap").css("display", "block");
                }else {
                    $("#media_table_logs").setGridParam().hideCol(colModel.name);
                    $("#mediaContentIdWrap").css("display", "none");
                }
            }
        });
        $("#media_table_logs").jqGrid('setGridParam', {
            postData: $("#mediaForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        $("#addModal").modal("hide");
        $("#mediaModal").modal({backdrop: "static"});
    },
    mediaSupplierModalShow: function (t) {
        $("#opType1").val(0);//编辑稿件选择媒体
        if(!$(t).closest("form").find("input[name='mediaId']").val()){
            layer.msg("请先选择媒体！", {time: 2000, icon: 5});
            return;
        }
        $("#mediaId").val($(t).closest("form").find("input[name='mediaId']").val());
        mediaSupplierGrid.loadGrid();
        $("#mediaSupplierTable").emptyGridParam();
        //根据是否标准板块决定是否展示唯一标识
        var colModels = $("#mediaSupplierTable").jqGrid('getGridParam', 'colModel');
        $(colModels).each(function (j, colModel) {
            //如果是标准板块，则展示唯一标识
            if (colModel.name == "mediaContentId") {
                if(mediaUserPlateMap[$("#mTypeId").val()] && mediaUserPlateMap[$("#mTypeId").val()]["standarPlatformFlag"] == 1){
                    $("#mediaSupplierTable").setGridParam().showCol(colModel.name);
                }else {
                    $("#mediaSupplierTable").setGridParam().hideCol(colModel.name);
                }
            }
        });
        $("#mediaSupplierTable").jqGrid('setGridParam', {
            postData: $("#mediaSupplierForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        $("#editModal").modal("hide");
        $("#mediaSupplierModal").modal({backdrop: "static"});
    },
    mediaSupplierModalShow1: function (t) {
        $("#opType1").val(1);//新增稿件选择媒体
        if(!$(t).closest("form").find("input[name='mediaId']").val()){
            layer.msg("请先选择媒体！", {time: 2000, icon: 5});
            return;
        }
        $("#mediaId").val($(t).closest("form").find("input[name='mediaId']").val());
        mediaSupplierGrid.loadGrid();
        $("#mediaSupplierTable").emptyGridParam();
        //根据是否标准板块决定是否展示唯一标识
        var colModels = $("#mediaSupplierTable").jqGrid('getGridParam', 'colModel');
        $(colModels).each(function (j, colModel) {
            //如果是标准板块，则展示唯一标识
            if (colModel.name == "mediaContentId") {
                if(mediaUserPlateMap[$("#mediaTypeId2").val()] && mediaUserPlateMap[$("#mediaTypeId2").val()]["standarPlatformFlag"] == 1){
                    $("#mediaSupplierTable").setGridParam().showCol(colModel.name);
                }else {
                    $("#mediaSupplierTable").setGridParam().hideCol(colModel.name);
                }
            }
        });
        $("#mediaSupplierTable").jqGrid('setGridParam', {
            postData: $("#mediaSupplierForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        $("#addModal").modal("hide");
        $("#mediaSupplierModal").modal({backdrop: "static"});
    },
    cleanMediaInfo: function () {
        $("#editForm [name='mediaId']").val("");
        $("#editForm [name='mediaName']").val("");
        $("#editForm [name='supplierId']").val("");
        $("#editForm [name='supplierName']").val("");
        $("#editForm [name='supplierContactor']").val("");
        $("#editForm #supplierPhone").val("");
    },
    cleanMediaInfo1: function () {
        $("#addForm [name='mediaId']").val("");
        $("#addForm [name='mediaName']").val("");
        $("#addForm [name='mediaPersonId']").val("");
        $("#addForm [name='supplierId']").val("");
        $("#addForm [name='supplierName']").val("");
        $("#addForm [name='supplierContactor']").val("");
        $("#addForm #supplierPhoneAdd").val("");
        $("#addForm [name='supplierPersonId']").val("");
    },
    cleanMediaSupplierInfo: function () {
        $("#editForm [name='supplierId']").val("");
        $("#editForm [name='supplierName']").val("");
        $("#editForm [name='supplierContactor']").val("");
        $("#editForm #supplierPhone").val("");
    },
    cleanMediaSupplierInfo1: function () {
        $("#addForm [name='supplierId']").val("");
        $("#addForm [name='supplierName']").val("");
        $("#addForm [name='supplierContactor']").val("");
        $("#addForm [name='supplierPersonId']").val("");
        $("#addForm #supplierPhoneAdd").val("");
    },
    selectMedia: function () {
        if ($("#opType").val() == 0) {//opType=0编辑
            var id = $('#media_table_logs').jqGrid('getGridParam', 'selrow');
            var rowData = $("#media_table_logs").jqGrid('getRowData', id);
            //如果选择的媒体 == 已选媒体，则供应商信息不清除，否则清除，进行重新选择
            if(rowData.mediaId != $("#editForm [name='mediaId']").val()){
                $("#editForm [name='supplierId']").val("");
                $("#editForm [name='supplierName']").val("");
                $("#editForm [name='supplierContactor']").val("");
                $("#editForm #supplierPhone").val("");
            }
            $("#editForm [name='mediaId']").val(rowData.mediaId);
            $("#editForm [name='mediaName']").val(rowData.mediaName || rowData.mediaContentId);
            $("#mediaModal").modal("hide");
            $("#editModal").modal({backgroud: "static"});
        } else {//opType=1新增
            var id = $('#media_table_logs').jqGrid('getGridParam', 'selrow');
            var rowData = $("#media_table_logs").jqGrid('getRowData', id);
            //如果选择的媒体 == 已选媒体，则供应商信息不清除，否则清除，进行重新选择
            if(rowData.mediaId != $("#addForm [name='mediaId']").val()){
                $("#addForm [name='supplierId']").val("");
                $("#addForm [name='supplierName']").val("");
                $("#addForm [name='supplierContactor']").val("");
                $("#addForm #supplierPhoneAdd").val("");
            }
            $("#addForm [name='mediaId']").val(rowData.mediaId);
            $("#addForm [name='mediaName']").val(rowData.mediaName || rowData.mediaContentId);
            $("#addForm [name='mediaPersonId']").val(rowData.userId);
            $("#mediaModal").modal("hide");
            $("#addModal").modal({backgroud: "static"});
        }

    },
    cancelMedia: function () {
        if ($("#opType").val() == 1) {
            $("#mediaModal").modal("hide");
            $("#addModal").modal({backgroud: "static"});
        } else {
            $("#mediaModal").modal("hide");
            $("#editModal").modal({backgroud: "static"});
        }

    },
    selectMediaSupplier: function () {
        if ($("#opType1").val() == 0) {//opType=0编辑
            var id = $('#mediaSupplierTable').jqGrid('getGridParam', 'selrow');
            var rowData = $("#mediaSupplierTable").jqGrid('getRowData', id);
            $("#editForm [name='supplierId']").val(rowData.supplierId);
            $("#editForm [name='supplierName']").val(rowData.supplierCompanyName);
            $("#editForm [name='supplierContactor']").val(rowData.supplierName);
            $("#editForm #supplierPhone").val(rowData.phoneStr);
            $("#mediaSupplierModal").modal("hide");
            $("#editModal").modal({backgroud: "static"});
        } else {//opType=1新增
            var id = $('#mediaSupplierTable').jqGrid('getGridParam', 'selrow');
            var rowData = $("#mediaSupplierTable").jqGrid('getRowData', id);
            $("#addForm [name='supplierId']").val(rowData.supplierId);
            $("#addForm [name='supplierName']").val(rowData.supplierCompanyName);
            $("#addForm [name='supplierContactor']").val(rowData.supplierName);
            $("#addForm #supplierPhoneAdd").val(rowData.phoneStr);
            $("#addForm [name='supplierPersonId']").val(rowData.supplierUserId);
            $("#mediaSupplierModal").modal("hide");
            $("#addModal").modal({backgroud: "static"});
        }
    },
    cancelMediaSupplier: function () {
        if ($("#opType1").val() == 1) {
            $("#mediaSupplierModal").modal("hide");
            $("#addModal").modal({backgroud: "static"});
        } else {
            $("#mediaSupplierModal").modal("hide");
            $("#editModal").modal({backgroud: "static"});
        }

    },
};

/**
 * 统计概况弹窗方法
 */
var statisticsToggle = {
    getSingleLinkHtml: function (id, value, type) { //获取单个a链接
        var html = "";
        if (id) {
            value = value ? value : id;//如果value为空则展示ID
            html += "<a onclick=\"statisticsToggle.toggleModal(" + id + ",'" + value + "','" + type + "');\">" + value + "</a>";
        }
        return html;
    },
    toggleModal: function (id, name, type) {
        if ("cust" == type) {
            var title = "[" + name + "]-客户统计";
            statisticsModal.loadConfig({enterType: "cust", enterParam: {custId: id}, title: title}); //加载用户配置
        }
        if ("business" == type) {
            var title = "[" + name + "]-业务统计";
            statisticsModal.loadConfig({enterType: "business", enterParam: {currentUserId: id}, title: title}); //加载用户配置
        }
        if ("mediaUser" == type) {
            var title = "[" + name + "]-媒介统计";
            statisticsModal.loadConfig({enterType: "mediaUser", enterParam: {currentUserId: id}, title: title}); //加载用户配置
        }
        if ("mediaType" == type) {
            var title = "[" + name + "]-板块统计";
            statisticsModal.loadConfig({enterType: "mediaType", enterParam: {mediaType: id}, title: title}); //加载用户配置
        }
        if ("media" == type) {
            var title = "[" + name + "]-媒体统计";
            statisticsModal.loadConfig({enterType: "media", enterParam: {mediaId: id}, title: title}); //加载用户配置
        }
        if ("supplier" == type) {
            var title = "[" + name + "]-供应商统计";
            statisticsModal.loadConfig({enterType: "supplier", enterParam: {supplierId: id}, title: title}); //加载用户配置
        }
        $("#statisticsModal").modal("toggle");
    }
}

var gridObject = {
    url: baseUrl + '/article/articleListMJ',
    postData: $("#searchForm").serializeJson(),
    datatype: "json",
    mtype: 'get',
    // data: mydata,
    height: "auto",
    page: 1,//第一页
    autowidth: true,
    rownumbers: true,
    gridview: true,
    viewrecords: true,
    multiselect: true,
    multiselectWidth: 25, //设置多选列宽度
    sortable: "true",
    sortname: "art.id",
    sortorder: "desc",
    shrinkToFit: true,
    prmNames: {rows: "size"},
    rowNum: 10,
    rowList: [10, 20, 50, 100],
    // colNames: ['订单ID', '订单编号', '客户公司',
    //     // '标题', '金额',
    //     '支付状态', '稿件ID', '类别ID', '类别', "媒体", "稿件标题",  "链接",
    //     "发布日期", "客户报价", "媒介", "供应商名称","供应商联系人", "业务员",
    //     "提成状态", "开票状态", "状态", "操作"],
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: false
    },
    colModel: [
        {
            name: 'id',
            index: 'id',
            editable: false,
            width: 30,
            align: "center",
            sortable: false,
            sorttype: "int",
            search: true,
            cellattr: function (rowId, tv, rawObject, cm, rdata) {
                //合并单元格
                return "id='id" + rowId + "'";
            },
            hidden: true
        },
        {
            name: 'state',
            index: 'state',
            label: '状态',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            sorttype: "string",
            cellattr: function (rowId, tv, rawObject, cm, rdata) {
                return "id='state" + rowId + "'";
            },
            formatter: function (d) {
                var html = d == 1 ? "已下单" : "未下单";
                return html;
            },
            hidden: true
        },
        {
            name: 'artId',
            index: 'artId',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            sorttype: "string",
            hidden: true
        },
        {
            name: 'mTypeId',
            index: 'mTypeId',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            sorttype: "string",
            hidden: true
        },
        {
            name: 'supplierCompanyCode',
            index: 'supplierCompanyCode',
            hidden: true
        },
        {
            name: 'mTypeName',
            index: 'mTypeName',
            label: '媒体板块',
            editable: false,
            width: 70,
            align: "center",
            sortable: true,
            formatter: function (value, grid, rows) {
                return statisticsToggle.getSingleLinkHtml(rows.mTypeId, rows.mTypeName, "mediaType");
            }
        },
        {name: 'mediaId', label: 'mediaId', editable: true, hidden: true, width: 0},
        {
            name: 'mediaName',
            index: 'mediaName',
            label: '媒体名称',
            editable: false,
            width: 80,
            align: "center",
            sortable: true,
            hidden: false,
            formatter: function (value, grid, rows) {
                return statisticsToggle.getSingleLinkHtml(rows.mediaId, rows.mediaName, "media");
            }
        },
        {
            name: 'supplierName',
            index: 'supplierName',
            label: '供应商名称',
            editable: false,
            width: 80,
            align: "center",
            sortable: true
            // formatter: function (v, options, row) {
            //     ///console.log(row.supplierCompanyCode)
            //     return flag ? v : (user.companyCode == row.supplierCompanyCode ? v : "");
            // }

        },
        {name: 'supplierId', label: 'supplierId', editable: true, hidden: true, width: 0},
        {
            name: 'supplierContactor',
            index: 'supplierContactor',
            label: '供应商联系人',
            editable: false,
            width: 70,
            align: "center",
            sortable: true,
            formatter: function (v, options, row) {
                if (v) {
                    return statisticsToggle.getSingleLinkHtml(row.supplierId, v, "supplier");
                } else {
                    return "";
                }
            }
        },
        {
            name: 'supplierPhone',
            label: '供应商电话',
            editable: true,
            width: 120,
            align: "center",
            formatter: function (value, grid, rows) {
                if(rows){
                    var value = rows.supplierPhone;
                    if(value){
                        if (value.length >= 11) {
                            var start = value.length > 11 ? "*****" : "****";
                            return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                        } else if (value.length >= 3) {
                            return value[0] + "***" + value[value.length - 1];
                        } else {
                            return "**";
                        }
                    }else{
                        return "";
                    }
                }else{
                    return "";
                }
            }
        },
        {
            name: 'electricityBusinesses',
            index: 'electricityBusinesses',
            label: '电商商家',
            editable: false,
            width: 70,
            align: "center",
            sortable: true
        },
        {
            name: 'channel',
            index: 'channel',
            label: '频道',
            editable: false,
            width: 70,
            align: "center",
            sortable: true

        },
        {
            name: 'innerOuter',
            index: 'innerOuter',
            label: '内外部',
            editable: false,
            width: 70,
            align: "center",
            sortable: true
        },
        {
            name: 'dockingName',
            index: 'dockingName',
            label: '客户状态',
            editable: false,
            width: 60,
            align: "center",
            sortable: true,
            formatter: function (d) {
                if (!d) {
                    return "<span class='text-red'>未完善</span>";
                }
                return "已完善";
            }
        },
        {
            name: 'companyCodeName',
            index: 'usercompanyCodeNameName',
            label: '业务员所在公司',
            editable: false,
            width: 70,
            align: "center",
            sortable: true
        },
        {name: 'userId', label: 'userId', editable: true, hidden: true, width: 0},
        {
            name: 'userName',
            index: 'userName',
            label: '业务员',
            editable: false,
            width: 70,
            align: "center",
            sortable: true
        },
        {name: 'mediaUserId', label: 'mediaUserId', editable: true, hidden: true, width: 0},
        {
            name: 'mediaUserName',
            index: 'mediaUserName',
            label: '媒介',
            editable: false,
            width: 70,
            align: "center",
            sortable: true,
            formatter: function (value, grid, rows) {
                return statisticsToggle.getSingleLinkHtml(rows.mediaUserId, rows.mediaUserName, "mediaUser");
            }
        },
        {
            name: 'title',
            index: 'title',
            label: '标题',
            editable: false,
            width: 100,
            align: "center",
            sortable: true
            // formatter: function (v, options, row) {
            //     return "<a style='color: #337ab7;' href='" + row.link + "' target='_blank'>" + row.title + "</a>";
            // }
        },
        {
            name: 'link',
            index: 'link',
            label: '链接',
            editable: false,
            width: 100,
            align: "center",
            sortable: true,
            formatter: function (v, options, row) {
                return "<a style='color: #337ab7;' href='" + row.link + "' target='_blank'>" + row.link + "</a>";
            }
        },
        {
            name: 'issuedDate',
            index: 'issuedDate',
            label: '发布日期',
            editable: false,
            width: 80,
            align: "center",
            sortable: true,
            hidden: false,
            formatter: function (d) {
                if (!d) {
                    return "";
                }
                // return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                return new Date(d).format("yyyy-MM-dd");
            }
        },
        {
            name: 'unitPrice',
            index: 'unitPrice',
            label: '单价',
            editable: false,
            width: 80,
            align: "center",
            sortable: true,
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"}

        },
        {
            name: 'num',
            index: 'num',
            label: '数量',
            editable: false,
            width: 60,
            align: "center",
            sortable: true
        },
        {
            name: 'saleAmount',
            index: 'saleAmount',
            label: '报价',
            editable: false,
            width: 80,
            align: "center",
            sortable: true,
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"}

        },
        {
            name: 'priceType',
            index: 'priceType',
            label: '价格类型',
            editable: false,
            width: 70,
            align: "center",
            sortable: true
        },
        {
            name: 'payAmount',
            index: 'payAmount',
            label: '执行前价格',
            editable: false,
            width: 70,
            align: "center",
            sortable: true,
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"}

        },
        {
            name: 'outgoAmount',
            index: 'outgoAmount',
            label: '执行后金额',
            editable: false,
            width: 70,
            align: "center",
            sortable: true,
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"}

        },
        {
            name: 'priceDifference',
            index: 'priceDifference',
            label: '差价',
            editable: false,
            width: 70,
            align: "center",
            sortable: true,
            classes: 'text-danger',
            formatter: function (a, b, rowdata) {
                return rowdata.payAmount == 0 ? 0 : rowdata.payAmount - rowdata.outgoAmount;
            },
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"}

        },
        {
            name: 'otherExpenses',
            index: 'otherExpenses',
            label: '其他费用（不计入成本）',
            editable: false,
            width: 60,
            align: "center",
            sortable: false,
            classes: 'text-danger',
            formatter: "currency",
            formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"}
        },
        {
            name: 'OutgoDetail',
            index: 'OutgoDetail',
            label: '请款详情',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var html = "";
                if (rowdata.outgoStates == 1) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOutgoId(" + rowdata.artId + ")'>已请款</a>";
                } else if (rowdata.outgoStates == 2) {
                    html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryOutgoId(" + rowdata.artId + ")'>请款中</a>";
                } else {
                    html = "";
                }
                return html;
            }
        },
        {
            name: 'commissionStates',
            index: 'commissionStates',
            label: '提成状态',
            editable: false,
            width: 70,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var html = "";
                if (a == 0) {
                    html = "";
                } else {
                    html = "<span style='color:#337ab7'>已提成</span>";
                }
                return html;
            }
        },
        {
            name: 'issueStates',
            index: 'issueStates',
            label: '发布状态',
            editable: false,
            width: 60,
            align: "center",
            sortable: true,
            hidden: false,
            formatter: function (a, b, rowdata) {
                var html = "";
                if (rowdata.issueStates == 0) {
                    html = "未下单";
                }
                if (rowdata.issueStates == 1) {
                    html = "待安排";
                }
                if (rowdata.issueStates == 2) {
                    html = "进行中";
                }
                if (rowdata.issueStates == 3) {
                    html = "已驳回";
                }
                if (rowdata.issueStates == 4) {
                    html = "已发布";
                }
                return html;
            }
        },
        {
            name: 'remarks',
            index: 'remarks',
            label: '备注',
            editable: false,
            width: 60,
            align: "center",
            sortable: false
        },
        {
            name: 'option',
            editable: false,
            label: '操作',
            width: 130,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
                var manageDeptList = user.deptIdSet;//获取当前用户的部门及其子部门
                var currentArtMediaDeptId = mediaUserMap[rowdata.mediaUserId] ? mediaUserMap[rowdata.mediaUserId].deptId : "";//当前稿件媒介的部门ID
                var html = "";
                if (rowdata.issueStates == 1) {
                    //针对于下单模式，上级和自己可以操作
                    if((currentDeptQx && manageDeptList.contains(currentArtMediaDeptId)) || user.id == rowdata.mediaUserId){
                        html += "<a href='javascript:Business.returnDown(" + rowdata.artId + ")' style='margin-right:3px;color:#337ab7'>驳回&nbsp;</a>";
                        html += "<a href='javascript:Business.arrange(" + rowdata.artId + ","+rowdata.id+", \""+rowdata.title+"\")' style='margin-right:3px;color:#337ab7'>安排&nbsp;</a>";
                        html += "<a href='javascript:Business.yj(" + rowdata.artId + "," + rowdata.mTypeId + ")' style='margin-right:3px;color:#337ab7'>移交&nbsp;</a>";
                        html += "<a href='javascript:Business.deleteArt(" + rowdata.artId + ")' style='margin-right:3px;color:#337ab7'>删除&nbsp;</a>";
                    }
                } else if (rowdata.issueStates == 2) {
                    //针对于下单模式，上级和自己可以操作
                    if((currentDeptQx && manageDeptList.contains(currentArtMediaDeptId)) || user.id == rowdata.mediaUserId){
                        html += "<a href='javascript:edit(" + rowdata.artId + ")' style='margin-right:3px;color:#337ab7'>编辑&nbsp;</a>";
                        html += "<a href='javascript:Business.publish(" + rowdata.artId + ",\"" + rowdata.link + "\"," + rowdata.issuedDate + ",\"" + rowdata.title + "\","+rowdata.outgoAmount+")' style='margin-right:3px;color:#337ab7'>发布&nbsp;</a>";
                        html += "<a href='javascript:Business.deleteArt(" + rowdata.artId + ")' style='margin-right:3px;color:#337ab7'>删除&nbsp;</a>";
                    }
                } else if (rowdata.issueStates == 4) {
                    html += "<a href='javascript:edit(" + rowdata.artId + ")' style='margin-right:3px;color:#337ab7'>编辑&nbsp;</a>";
                    html += "<a href='javascript:Business.deleteArt(" + rowdata.artId + ")' style='margin-right:3px;color:#337ab7'>删除&nbsp;</a>";
                }
                return html;
            }
        }
    ],
    loadComplete: function () {//创建表格执行
        $("#jqgh_table_cb").css("padding-right", "6px");
    },
    /**
     * 翻页时保存当前页面的选中数据
     * @param pageBtn
     */
    onPaging: function (pageBtn) {
        //跨页面选择
        grid.setPageSelected("artId");
    },
    gridComplete: function () {
        //跨页面选择
        grid.getPageSelectedSet("artId");
        Business.setArticleResult();
    },
    ondblClickRow: function (rowid, iRow, iCol, e) {
        //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
        var rowData = jQuery("#table").jqGrid("getRowData", rowid);
        view(rowData.artId);
    },
    pager: "#pager",
    viewrecords: true,
    // caption: "媒介查询",
    add: false,
    edit: false,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false
};

var mediaGridObject = {
    url: baseUrl + '/media1/listMediaByParam',
    datatype: "json",
    mtype: 'POST',
    postData: $("#mediaForm").serializeJson(), //发送数据
    altRows: true,
    altclass: 'bgColor',
    height: "auto",
    page: 1,//第一页
    rownumbers: true,
    setLabel: "序号",
    autowidth: true,//自动匹配宽度
    gridview: true, //加速显示
    cellsubmit: "clientArray",
    viewrecords: true,  //显示总记录数
    multiselect: false,
    multiboxonly: true,
    multiselectWidth: 25, //设置多选列宽度
    sortable: "true",
    // sortname: "id",
    sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
    shrinkToFit: true,
    prmNames: {rows: "size"},
    rowNum: 10,//每页显示记录数
    rowList: [10, 20, 50],//分页选项，可以下拉选择每页显示记录数
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: "id"
    },
    // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
    colModel: [
        {
            name: 'plateName',
            label: '媒体板块',
            editable: true,
            width: 120
        },
        {
            name: 'mediaContentId',
            label: '唯一标识',
            editable: true,
            width: 240,
            align: "center",
        },
        {
            name: 'mediaName',
            label: '媒体名称',
            editable: true,
            width: 240
        },
        {name: 'mediaId', label: 'mediaId', editable: true, hidden: true, width: 0},
        {name: 'userId', label: 'userId', editable: true, hidden: true, width: 0},
        {name: 'plateId', label: 'plateId', editable: true, hidden: true, width: 0},
    ],
    pager: "#media_pager_logs",
    viewrecords: true,
    caption: "",
    add: false,
    edit: true,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false,
    gridComplete: function () {
        var mediaTableLogs = $("#media_table_logs");
        mediaTableLogs.setGridWidth(mediaTableLogs.closest('.jqGrid_wrapper').width());
    }
};

var mediaSupplierGridObject = {
    url: baseUrl + '/media1/listMediaSupplierByParam',
    datatype: "json",
    mtype: 'POST',
    postData: $("#mediaSupplierForm").serializeJson(), //发送数据
    altRows: true,
    altclass: 'bgColor',
    height: "auto",
    page: 1,//第一页
    rownumbers: true,
    setLabel: "序号",
    autowidth: true,//自动匹配宽度
    gridview: true, //加速显示
    cellsubmit: "clientArray",
    viewrecords: true,  //显示总记录数
    multiselect: false,
    multiboxonly: true,
    multiselectWidth: 25, //设置多选列宽度
    sortable: "true",
    // sortname: "id",
    sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
    shrinkToFit: true,
    prmNames: {rows: "size"},
    rowNum: 10,//每页显示记录数
    rowList: [10, 20, 50],//分页选项，可以下拉选择每页显示记录数
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: "id"
    },
    // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
    colModel: [
        {
            name: 'plateName',
            label: '媒体板块',
            editable: true,
            width: 120
        },
        {
            name: 'mediaContentId',
            label: '媒体唯一标识',
            editable: true,
            width: 240,
            align: "center",
        },
        {
            name: 'mediaName',
            label: '媒体名称',
            editable: true,
            width: 240
        },
        {
            name: 'supplierCompanyName',
            label: '供应商名称',
            editable: true,
            width: 240
        },
        {
            name: 'supplierName',
            label: '供应商联系人',
            editable: true,
            width: 150
        },
        {
            name: 'phoneStr',
            label: '供应商电话',
            editable: false,
            width: 100,
            sortable:false,
            formatter: function (value, grid, rows) {
                value = rows.phone || "";
                if(value){
                    if(value.length >= 11){
                        var start = value.length > 11 ? "*****" : "****";
                        return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                    }else if(value.length >= 3){
                        return value[0] + "***" + value[value.length - 1];
                    }else {
                        return "**";
                    }
                }else {
                    return "";
                }
            }
        },
        {name: 'supplierUserId', label: 'supplierUserId', editable: true, hidden: true, width: 0},
        {name: 'mediaId', label: 'mediaId', editable: true, hidden: true, width: 0},
        {name: 'supplierId', label: 'supplierId', editable: true, hidden: true, width: 0},
    ],
    pager: "#mediaSupplierTablePaper",
    viewrecords: true,
    caption: "",
    add: false,
    edit: true,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false,
    gridComplete: function () {
        var $mediaSupplierTable = $("#mediaSupplierTable");
        $mediaSupplierTable.setGridWidth($mediaSupplierTable.closest('.jqGrid_wrapper').width());
    }
};

//通过artid获取附件
function getFiles(artId) {
    $("#fileInfo1").empty();
    $.ajax({
        url: baseUrl + "/mediaFiles/getFilesByArticleId",
        type: "post",
        data: {articId: artId},
        dataType: "json",
        success: function (resData) {
            var data = resData.data.entity;
            // console.log(data);
            for (var o in data) {
                if (o == "filesName") {
                    $("#fileInfo1").empty();
                    //后台返回去的数据转换成集合后，遍历显示
                    var flies = data.filesName.split(",");
                    var filesLink = data.filesLink.split(",");
                    if (flies.length > 0 && filesLink.length > 0) {
                        var html = "";
                        for (var i = 0; i < flies.length; i++) {
                            var filePath = filesLink[i];
                            var fileName = flies[i];
                            html += "<a href=" + filePath + " target=_blank >" + fileName + "</a><br/>";
                        }
                        $("#fileInfo1").append(html);
                    }
                }

            }
        }
    });
}
function edit(artId) {
    getFiles(artId);
    $("#editForm").find("input").removeClass('error');
    $("#editForm").validate().resetForm();
    document.getElementById("editForm").reset();
    $("#editModal").modal('toggle');
    $("#editForm").find("select[name='innerOuter']").find("option").removeAttrs("selected");
    $.ajax({
        url: baseUrl + "/article/editArticle",
        type: "post",
        data: {id: artId},
        dataType: "json",
        success: function (resData) {
            for (var o in resData) {
                if (o == "innerOuter") {
                    $("#editForm").find("select[name='innerOuter']").find("option[value='" + resData[o] + "']").attr("selected", "selected");
                } else {
                    var v = resData[o] || "";
                    $("#editForm [name=" + o + "]").val(v);
                    $("#editForm [name=" + o + "]").attr("readonly", "readonly");
                    if (o == "issuedDate") {
                        var d2 = new Date(resData[o]).format("yyyy/MM/dd");
                        $("#editForm [name='issuedDate']").val(d2);
                        // 保存原来的发布日期，用于后台计算新的到款日期；
                        $("#editForm [name='oldIssuedDate']").val(resData[o]);
                        continue;
                    }
                    if (o == "promiseDate") {
                        // 保存原来的到款日期，用于后台计算新的到款日期；
                        $("#editForm [name='oldPromiseDate']").val(resData[o]);
                    }
                    if (o == "unitPrice" || o == "otherExpenses" || o == "payAmount") {
                        $("#editForm [name=" + o + "]").val(v == 0 ? 0.0 : v);
                        continue;
                    }
                    /*if (o == "company_code") {
                        if (resData[o] == user.companyCode || user.companyCode == 'XH') {
                            $("#editForm [name='supplierName']").val(resData.supplierName);
                            $("#editForm [name='supplierContactor']").val(resData.supplierContactor);
                        } else {
                            $("#editForm [name='supplierName']").val("");
                            $("#editForm [name='supplierContactor']").val("");
                            $("#editForm #supplierPhone").val("");
                        }
                    }*/
                    if (o == "supplierPhone") {
                        if (resData[o]) {
                            var flag = false;
                            var value = resData[o];
                            if(value){
                                if (value.length >= 11) {
                                    var start = value.length > 11 ? "*****" : "****";
                                    value = value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                                } else if (value.length >= 3) {
                                    value = value[0] + "***" + value[value.length - 1];
                                } else {
                                    value = "**";
                                }
                            }else{
                                value = "";
                            }
                            $("#supplierPhone").val(value);
                        } else {
                            $("#supplierPhone").val("");
                        }

                    }
                }
            }
            var outgoStates = resData["outgoStates"];
            var commissionStates = resData["commissionStates"];
            if (outgoStates != 0 && commissionStates != 0) {//已提成已请款
                $("#editForm [name='remarks']").attr("readonly", "readonly");
                // $("#editForm input[name='issuedDate']").attr("disabled", "disabled");
                $("#saveButton").hide();
                $("#editForm .selectMediaBtns").css("display", "none");
                $("#selectMediaSupplierBtns").hide();
            } else if (outgoStates != 0 && commissionStates == 0) {//已请款未提成
                $("#editForm [name='remarks']").removeAttrs("readonly");
                $("#editForm input[name='link']").removeAttrs("readonly");
                // $("#editForm input[name='issuedDate']").removeAttrs("disabled");
                $("#saveButton").show();
                $("#editForm .selectMediaBtns").css("display", "none");
                $("#selectMediaSupplierBtns").hide();
            } else if (outgoStates == 0 && commissionStates != 0) {//未请款已提成
                $("#editForm [name='remarks']").removeAttrs("readonly");
                $("#editForm input[name='link']").removeAttrs("readonly");
                // $("#editForm input[name='issuedDate']").removeAttrs("disabled");
                $("#saveButton").show();
                $("#editForm .selectMediaBtns").css("display", "none");
                $("#selectMediaSupplierBtns").hide();
            } else {//未请款未提成
                $("#editForm input[name='title']").removeAttrs("readonly");
                $("#editForm input[name='link']").removeAttrs("readonly");
                $("#editForm input[name='payAmount']").removeAttrs("readonly");
                $("#editForm input[name='otherExpenses']").removeAttrs("readonly");
                $("#editForm input[name='outgoAmount']").removeAttrs("readonly");
                $("#editForm input[name='num']").removeAttrs("readonly");
                // $("#editForm input[name='issuedDate']").removeAttrs("disabled");
                $("#editForm [name='remarks']").removeAttrs("readonly");
                $("#editForm input[name='innerOuter']").removeAttrs("readonly");
                $("#editForm input[name='channel']").removeAttrs("readonly");
                $("#editForm input[name='electricityBusinesses']").removeAttrs("readonly");
                $("#saveButton").show();
                $("#editForm .selectMediaBtns").css("display", "inline-block");
                $("#selectMediaSupplierBtns").show();
            }
            $("#artId").removeAttrs("disabled");
        }
    });
}
function view(artId) {
    getFiles(artId);
    document.getElementById("editForm").reset();
    $("#editModal").modal('toggle');
    $.ajax({
        url: baseUrl + "/article/editArticle",
        type: "post",
        data: {id: artId},
        dataType: "json",
        success: function (resData) {
            for (var o in resData) {
                var v = resData[o] || "";
                $("#editForm [name=" + o + "]").val(v);
                $("#editForm [name=" + o + "]").attr("readonly", "readonly");
                if (o == "issuedDate") {
                    var d2 = new Date(resData[o]).format("yyyy/MM/dd");
                    $("#editForm [name='issuedDate']").val(d2);
                    // $("#editForm [name='issuedDate']").attr("disabled", "disabled");
                    continue;
                }
                if (o == "unitPrice" || o == "otherExpenses" || o == "payAmount") {
                    $("#editForm [name=" + o + "]").val(v == 0 ? 0.0 : v);
                    continue;
                }
                if (o == "company_code") {
                    if (resData[o] == user.companyCode || user.companyCode == 'XH') {
                        $("#editForm [name='supplierName']").val(resData.supplierName);
                        $("#editForm [name='supplierContactor']").val(resData.supplierContactor);
                    } else {
                        $("#editForm [name='supplierName']").val("");
                        $("#editForm [name='supplierContactor']").val("");
                    }
                }
                if (o == "supplierPhone") {
                    if (resData[o]) {
                        var value = resData[o];
                        if(value){
                            if (value.length >= 11) {
                                var start = value.length > 11 ? "*****" : "****";
                                value = value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                            } else if (value.length >= 3) {
                                value = value[0] + "***" + value[value.length - 1];
                            } else {
                                value = "**";
                            }
                        }else{
                            value = "";
                        }
                        $("#supplierPhone").val(value);
                    } else {
                        $("#supplierPhone").val("");
                    }
                }
            }
            $("#editForm [name='remarks']").attr("readonly", "readonly");
            $("#saveButton").hide();
            $("#editForm .selectMediaBtns").css("display", "none");
            $("#selectMediaSupplierBtns").hide();
        }
    });
}
function update() {
    var param = new FormData($("#editForm")[0]);
    if ($("#editForm").valid()) {
        startModal("#saveButton");
        $.ajax({
            url: baseUrl + "/article/updateArticle",
            type: "post",
            data: param,
            dataType: "json",
            async: true,
            cache: false,
            contentType: false,
            processData: false,
            success: function (resData) {
                Ladda.stopAll();
                if (resData.code == 200) {
                    swal("更新成功");
                    $("#editModal").modal('hide');
                    // $("#table").emptyGridParam();
                    $("#table").reloadCurrentData(baseUrl + "/article/articleListMJ", $("#searchForm").serializeJson(), "json", null, null);
                    // $("#table").reloadCurrentData(baseUrl + "/mediauser/list", $("#searchForm").serializeJson(), "json", null, function () {
                    //     Business.setArticleResult();
                    // });
                } else if(resData.code == 1002){
                    swal({
                        title: "异常提示",
                        text: resData.msg,
                    });
                } else {
                    swal(data.msg);
                }
            },
            error:function () {
                Ladda.stopAll();//隐藏加载按钮
            }
        });
    }
}
function insert() {
    if ($("#addForm [name='userId']").val() == "") {
        swal("请先选择业务员!");
        return;
    }
    if ($("#addForm [name='mediaUserNameSec']").val() == "") {
        swal("请选择媒介!");
        return;
    }
    var param = new FormData($("#addForm")[0]);
    if ($("#addForm").valid()) {
        startModal("#insertButton");//锁定按钮，防止重复提交
        $.ajax({
            url: baseUrl + "/article/addArticle",
            type: "post",
            data: param,
            dataType: "json",
            async: true,
            cache: false,
            contentType: false,
            processData: false,
            success: function (data) {
                Ladda.stopAll();
                if (data.code == 200) {
                    $("#addModal").modal('hide');
                    swal(data.data.message);
                    $("#table").emptyGridParam();
                    $("#table").reloadCurrentData(baseUrl + "/article/articleListMJ", $("#searchForm").serializeJson(), "json", null,function () {
                        Business.setArticleResult();
                    });
                } else {
                    swal(data.msg);
                }
            },
            error:function () {
                Ladda.stopAll();//隐藏加载按钮
            }
        });
    }
}

function batchUpdate() {
    var userId = $("#batchUpdateForm [name='userId']").val();
    var title = $("#batchUpdateForm [name='title']").val();
    var outgoAmount = $("#batchUpdateForm [name='outgoAmount']").val();
    var issuedDate = $("#batchUpdateForm [name='issuedDate']").val();
    var remarks = $("#batchUpdateForm [name='remarks']").val();
    if(userId == "" && title == "" && outgoAmount == "" && issuedDate == "" && remarks == ""){
        swal("没有找到更改内容，请确认后重试！");
        return;
    }
    var param = $("#batchUpdateForm").serializeJson();
    if ($("#batchUpdateForm").valid()) {
        startModal("#batchUpdateButton");//锁定按钮，防止重复提交
        $.ajax({
            url: baseUrl + "/article/batchEditArticle",
            type: "post",
            data: param,
            dataType: "json",
            success: function (data) {
                Ladda.stopAll();
                if (data.code == 200) {
                    $("#batchUpdateModal").modal('hide');
                    swal(data.data.message);
                    $("#table").emptyGridParam();
                    $("#table").reloadCurrentData(baseUrl + "/article/articleListMJ", $("#searchForm").serializeJson(), "json", null,function () {
                        Business.setArticleResult();
                    });
                } else {
                    swal(data.msg);
                }
            },
            error:function () {
                Ladda.stopAll();//隐藏加载按钮
            }
        });
    }

}
function queryOutgoId(articleId) {
    $.ajax({
        type: "post",
        url: "/outgo/queryOutgoId",
        data: {articleId: articleId},
        dataType: "json",
        success: function (dataId) {
            if (dataId > 0) {
                window.open("/fee/queryOutgo?flag=1&id=" + dataId);
                // return ("/fee/queryOutgo?flag=0&id=" + dataId);
            } else {
                return;
            }
        }
    });
}
/**
 * 后台请求方法
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 * @param contentType 请求头，默认无
 */
var requestData = function (data, url, requestType, dataType, async, callBackFun, contentType) {
    var param = {
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun
    };
    if (contentType) {
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
};