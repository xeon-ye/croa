var deptId = user.dept.id;//当前用户部门ID
var deptName = user.dept.name;//当前部门名称
var sysConfigMap = {}; //系统配置功能
var selectIds = [];//完善客户选中的稿件
var XMflag = false;
var searchForm = {
    init: function () {
        searchForm.getDeptId();
        searchForm.loadDept();
        searchForm.loadWorker(deptId, "YW");
    },
    //如果是财务、管理等岗位获取公司或集团的id
    getDeptId: function () {
        if (hasRoleYW() && user.companyCode == "JT" && user.currentCompanyQx) {
            requestData(null, "/dept/getRootDept", "POST", function (result) {
                var root = result.data.root;
                if (root) {
                    deptId = root.id;//整个集团的业务部
                    deptName = root.name;
                }
            });
        } else if (hasRoleYW() && user.currentCompanyQx) {
            requestData({companyCode: user.companyCode}, "/dept/getCompanyByCode", "POST", function (result) {
                var company = result.data.company;
                if (company) {
                    deptId = company.id;//整个公司的业务部
                    deptName = company.name;
                }
            });
        }
        $("#deptId").val(deptId);
        $("#deptName").val(deptName);
        return deptId;
    },
    loadDept: function () {
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        var deptDiv = document.getElementById("deptDiv");
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
        if ((hasRoleYW() || hasRoleXM()) && (currentDeptQx || currentCompanyQx)) {
            deptDiv.style.display = 'block';

            $('#treeview').treeview({
                data: [getTreeData()],
                onNodeSelected: function (event, data) {
                    $("#deptId").val(data.id);
                    $("#deptName").val(data.text);
                    $("#deptModal").modal('hide');
                    searchForm.loadWorker(data.id, data.code);
                }
            });
        }
    },
    //加载此部门下的业务员
    loadWorker: function (deptId, roleType) {
        var ele = $("#userId");
        ele.empty();
        //如果没有部门权限 和 公司权限 并且不是财务，则只加载当前用户
        if (hasRoleYW() || hasRoleXM()) {
            if (user.currentDeptQx || user.currentCompanyQx) {
                if (roleType) {
                    searchForm.loadDeptUser(deptId, roleType, "userId");
                }
            } else {
                ele.append("<option value=" + user.id + ">" + user.name + "</option>");
                layui.use('form', function () {
                    layui.form.render();
                    layui.form.on('select(userId)', function (data) {
                        $("#searchButton").trigger("click");
                    });
                });
            }
        }
    },

    loadDeptUser: function (deptId, roleType, attr) {
        var attribute = attr || 'users';
        layui.use(['form'], function () {
            var ele = $("[name=" + attribute + "]").length == 0 ? $("#" + attribute) : $("[name=" + attribute + "]");
            ele.append('<option value="">业务员</option>');
            $.ajax({
                    url: baseUrl + "/user/listUserByDeptAndRole",
                    type: "post",
                    data: {deptId: deptId, roleType: roleType},
                    async: false,
                    dataType: "json",
                    success: function (users) {
                        var userList = users.data.list;
                        if (userList && userList.length > 0) {
                            for (var i = 0; i < userList.length; i++) {
                                ele.append("<option value=" + userList[i].id + ">" + userList[i].name + "</option>");
                            }
                            layui.form.render();
                        }
                    }
                }
            );
        });
    }
};

//获取部门树数据
function getTreeData() {
    var deptTreeData = {};
    //具体查询
    requestData({deptId: deptId, deptCode: "YW"}, "/dept/listAllDeptByIdAndCode", "POST", function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

var requestData = function (data, url, requestType, callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: false,
        success: callBackFun
    });
};
var Business = {
    exportArt: function () {
        if (selectIds.length == 0) {
            layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
            var params = removeBlank($("#searchForm").serializeJson());
            location.href = "/article/exportArticleYW?" + $.param(params);
        } else {
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            var params = {datas: selectIds.toString()};
            location.href = "/article/exportArticleYW?" + $.param(params);
        }
    },
    //设置统计数据
    setArticleResult: function () {
        $.ajax({
            url: baseUrl + "/article/articleResultYW",
            data: $("#searchForm").serializeJson(),
            type: "post",
            dataType: "json",
            success: function (resData) {
                $("#tj").find(".text-danger").html(0);
                if (resData) {
                    for (var o in resData) {
                        $("#tj #" + o).text(resData[o] == "" ? 0 : resData[o].toMoney());
                    }
                }
            }
        });
    },

    loadTax: function () {
        $.ajax({
            url: baseUrl + "/article/tax",
            dataType: "json",
            success: function (data) {
                var mTypeEle = $("[name='taxSelect']");
                mTypeEle.empty()
                if (data.length > 0) {
                    mTypeEle.append("<option value='0' name='1' id='0'>不开票</option>");
                } else {
                    swal("没有找到与你所在部门关联的税种信息，请联系财务增加抬头！");
                    return;
                }
                for (var i = 0; i < data.length; i++) {
                    var mType = data[i];
                    mTypeEle.append("<option value='${value}' name='${type}' id='${id}'>${name}</option>".replace("${value}", mType.code).replace("${type}", mType.type).replace("${id}", mType.id).replace("${name}", mType.name));
                }
                //根据配置决定是否必须开票,系统稿件开票模式：1-必须开票、2-不开票、3-两种模式都兼容，没有配置默认必须开票
                if (sysConfigMap && sysConfigMap["artTaxModel"] && sysConfigMap["artTaxModel"]["value"] == 1) {
                    mTypeEle.find("option[value=0]").remove();
                } else if (sysConfigMap && sysConfigMap["artTaxModel"] && sysConfigMap["artTaxModel"]["value"] == 2) {
                    mTypeEle.html("<option value='0' name='1' id='0'>不开票</option>");
                }
            }
        });
    },

    calcuteTax: function () {
        // 获取含税金额；
        var saleAmount = $("#saleAmount").val();
        saleAmount = isNaN(parseFloat(saleAmount)) ? 0 : parseFloat(saleAmount);
        // if(isNaN(parseFloat(saleAmount))){
        //     swal("报价金额不正确！应该是数字，实际是："+saleAmount);
        //     $("#taxes").val("") ;
        //     return ;
        // }else{
        //     saleAmount = parseFloat(saleAmount) ;
        // }
        var ratio = $("#taxTypeName").val();
        if (isNaN(parseFloat(ratio))) {
            swal("该抬头对应的换算比不正确！应该为数字，实际是：" + ratio);
            $("#taxes").val("");
            return;
        } else {
            ratio = parseFloat(ratio);
        }
        var taxPoint = $("#taxpoint").val();
        if (isNaN(parseFloat(taxPoint))) {
            swal("税点不正确！应该是小数，实际是：" + taxPoint);
            $("#taxes").val("");
            return;
        } else {
            taxPoint = parseFloat(taxPoint);
        }
        // 计算新的税金；报价*税点/换算比
        var newTaxes = Math.round(saleAmount * taxPoint * 100 / ratio) / 100;
        // 将金额显示；
        $("#taxes").val(newTaxes.toFixed(2));
    },

    importModal: function () {
        $("#importModal").modal('toggle');
    },
    importSaleAmount: function () {
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
                var formData = new FormData($("#importForm")[0]);
                startModal("#batchImportSaleAmount");//锁定按钮，防止重复提交
                $.ajax({
                    type: "post",
                    url: "/articleImport/batchSaleAmount",
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

    edit: function (artId) {
        document.getElementById("editForm").reset();
        //清除validate错误样式
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();

        $("#editModal").modal('toggle');
        $.ajax({
            url: baseUrl + "/article/editArticle",
            type: "post",
            data: {id: artId},
            dataType: "json",
            success: function (resData) {
                for (var o in resData) {
                    if (o == "innerOuter") {
                        $("#innerOuter").attr("disabled", "disabled");
                        $("#editForm").find("select[name='innerOuter']").find("option[value='" + resData[o] + "']").attr("selected", "selected");
                    } else {
                        if (o == "issuedDate") {
                            var d2 = new Date(resData[o]).format("yyyy/MM/dd");
                            $("#editForm #issuedDate").val(d2);
                            continue;
                        }
                        if (o == "promiseDate") {
                            var d2 = new Date(resData[o]).format("yyyy/MM/dd");
                            $("#editForm #promiseDate").val(d2);
                            continue;
                        }

                        var v = resData[o] || "";
                        $("#editForm [name=" + o + "]").val(v);

                        if (o == "dockingName") {
                            if (resData.userId == user.id) {
                                $("#dockingName").val(resData[o]);
                            } else {
                                if (XMflag) {
                                    $("#dockingName").val("***")
                                } else {
                                    $("#dockingName").val(resData[o]);
                                }
                            }
                        }
                        $("#editForm input[name=" + o + "]").attr("readonly", "readonly");
                    }
                }

                var commissionStates = resData["commissionStates"];
                var invoiceStates = resData["invoiceStates"];
                if (invoiceStates == 0 && commissionStates == 0) {
                    $("#editForm [name='promiseDate']").removeAttrs("disabled");
                    $("#editForm [name='saleAmount']").removeAttrs("readonly");
                    $("#editForm [name='taxSelect']").removeAttrs("disabled");
                    $("#editForm [name='typeCodeEdit']").removeAttrs("disabled");
                    //brand和remarks可能为空，就会导致上面的readonly不会执行，手动处理一下
                    $("#editForm [name='brand']").removeAttrs("readonly");
                    $("#editForm [name='remarks']").removeAttrs("readonly");
                    $("#saveButton").show();
                } else {
                    $("#editForm [name='taxSelect']").attr("disabled", "disabled");
                    $("#editForm [name='typeCodeEdit']").attr("disabled", "disabled");
                    $("#editForm [name='promiseDate']").attr("disabled", "disabled");
                    //brand和remarks可能为空，就会导致上面的readonly不会执行，手动处理一下
                    $("#editForm [name='saleAmount']").attr("readonly", "readonly");
                    $("#editForm [name='brand']").attr("readonly", "readonly");
                    $("#editForm [name='remarks']").attr("readonly", "readonly");
                    $("#saveButton").hide();
                }

                loadTypeCode("typeCodeEdit", resData["typeCode"]);
                $("#typeNameEdit").val(resData["typeName"]);

                var selectedTax = resData.taxType || 0;
                $("#taxType").val(selectedTax);

                // 初始化税种信息；
                var obj = $("[name='taxSelect']");
                var selectOption = $(obj).find("option[id='" + $("#taxType").val() + "']");
                if (!selectOption || selectOption.length == 0) {
                    selectOption = $(obj).find("option:first-child");
                }

                //如果旧的税种和新的是一样的，就不处理税种，税点和换算比了，如果不一样就更新
                var oldSelected = $("#taxSelect option:selected").attr("id");
                var newSelected = $("#taxType").val();
                if (oldSelected != newSelected) {
                    $("#taxSelect option:selected").attr("selected", false);
                    selectOption.attr("selected", true);
                }
                $("#taxpoint").val(selectOption.val());
                $("#taxTypeName").val(selectOption.attr("name"));
                Business.calcuteTax();
            }
        });
    },

    taxChange: function (t) {
        $("#taxpoint").val($(t).val());
        $("#taxType").val($(t).find("option:selected").attr("id"));
        if ($("#taxType").val() == 0) {
            $("#taxType").val('');
        }
        $("#taxTypeName").val($(t).find("option:selected").attr("name"));
        Business.calcuteTax();
    },

    update: function () {
        if ($("#editForm").valid()) {
            var param = $("#editForm").serializeForm();
            var saleAmount = $("#saleAmount").val();
            if (saleAmount) {
                if (saleAmount == 0) {
                    swal("输入金额应大于0");
                    return;
                }
            }
            startModal("#saveButton");//锁定按钮，防止重复提交
            $.ajax({
                url: baseUrl + "/article/updateArticle",
                type: "post",
                data: param,
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        swal("更新成功");
                        // $("#table").emptyGridParam();
                        $("#table").reloadCurrentData(baseUrl + "/article/articleListYW", $("#searchForm").serializeJson(), "json", null, function () {
                            if (selectIds.length > 0) {//保留选中
                                for (var i = 0; i < selectIds.length; i++) {
                                    $(this).jqGrid('setSelection', selectIds[i]);
                                }
                            }
                            Business.setArticleResult();
                        });
                        $("#editModal").modal('hide');
                    } else if (data.code == 1002) {
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                },
                error: function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        }
    }
};

//统计概况弹窗
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
};

$(document).ready(function () {
    var project = projectDirector();
    if (project && project.contains(user.id)) {
        XMflag = true;
    } else {
        XMflag = false;
    }
    searchForm.init();
    //请求系统参数
    requestData(null, "/sysConfig/getAllConfig", "get", function (data) {
        //由于日期类型为数字需要格式处理
        for (var k in data) {
            if (data[k].dataType == 'date' && data[k].pattern) {
                data[k].value = new Date(data[k].value).format(data[k].pattern.replace(/H/g, "h"));
            }
        }
        sysConfigMap = data;
    });

    $("#selDept").click(function () {
        startModal("#selDept");
        $("#deptModal").modal('toggle');
        Ladda.stopAll();
    });
    $("#cleanDept").click(function () {
        startModal("#cleanDept");
        $("#deptId").val(deptId);
        $("#deptName").val(user.dept.name);
        Ladda.stopAll();
    });

    //编辑客户加载税种
    Business.loadTax();

    //查询页统计
    Business.setArticleResult();

    statisticsModal.init();//初始化模态框

    $('#custModal').on('hidden.bs.modal', function () {
        // $("#orderModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });
    $('#editCompanyModal').on('hidden.bs.modal', function () {
        $("#custModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $("#enterpriseBtn").click(function () {
        companyObj.natureClick(this, 1, $("#addCompanyForm input[name='companyName']").val());
    });
    $("#personalBtn").click(function () {
        companyObj.natureClick(this, 0, $("#addCompanyForm input[name='companyName']").val());
    });

    $("#searchForm select[name='completeStatus']").val(0);
    $("#searchForm select[name='completeStatus']").css({color: "#00796a"})

    $("#tab0").click(function () {
        $("#searchForm select[name='completeStatus']").val("");
        $("#searchForm select[name='completeStatus']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".custState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab1").click(function () {
        $("#searchForm select[name='completeStatus']").val(0);
        $("#searchForm select[name='completeStatus']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".custState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab2").click(function () {
        $("#searchForm select[name='completeStatus']").val(1);
        $("#searchForm select[name='completeStatus']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".custState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab3").click(function () {
        $("#searchForm select[name='incomeStates']").val("");
        $("#searchForm select[name='incomeStates']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".incomeState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab4").click(function () {
        $("#searchForm select[name='incomeStates']").val(0);
        $("#searchForm select[name='incomeStates']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".incomeState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab5").click(function () {
        $("#searchForm select[name='incomeStates']").val(2);
        $("#searchForm select[name='incomeStates']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".incomeState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab6").click(function () {
        $("#searchForm select[name='incomeStates']").val(1);
        $("#searchForm select[name='incomeStates']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".incomeState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab7").click(function () {
        $("#searchForm select[name='invoiceStates']").val("");
        $("#searchForm select[name='invoiceStates']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".invoiceState").find("span").css("color", ""); //设置TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab8").click(function () {
        $("#searchForm select[name='invoiceStates']").val("0");
        $("#searchForm select[name='invoiceStates']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".invoiceState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab9").click(function () {
        $("#searchForm select[name='invoiceStates']").val("2");
        $("#searchForm select[name='invoiceStates']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".invoiceState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#tab10").click(function () {
        $("#searchForm select[name='invoiceStates']").val("1");
        $("#searchForm select[name='invoiceStates']").css({color: "#00796a"})
        $("#searchButton").trigger("click");
        $("#tabList").find(".invoiceState").find("span").css("color", ""); //设置所有TAB颜色为默认
        $(this).find("span").css("color", "red"); //设置当前选中Tab颜色为红色
    });
    $("#searchForm select[name='completeStatus']").change(function () {
        var value = $(this).val();
        $("#tabList").find(".custState").removeClass("active");
        if (value == "0") {
            $("#tab1").trigger("click");
            $("#tab1").addClass("active");
        } else if (value == "1") {
            $("#tab2").trigger("click");
            $("#tab2").addClass("active");
        } else {
            $("#tab0").trigger("click");
            $("#tab0").addClass("active");
        }
    });
    $("#searchForm select[name='incomeStates']").change(function () {
        $("#tabList").find(".incomeState").removeClass("active");
        var value = $(this).val();
        if (value == "0") {
            $("#tab4").trigger("click");
            $("#tab4").addClass("active");
        } else if (value == "1") {
            $("#tab6").trigger("click");
            $("#tab6").addClass("active");
        } else if (value == "2") {
            $("#tab5").trigger("click");
            $("#tab5").addClass("active");
        } else {
            $("#tab3").trigger("click");
            $("#tab3").addClass("active");
        }
    });
    $("#searchForm select[name='invoiceStates']").change(function () {
        $("#tabList").find(".invoiceState").removeClass("active");
        var value = $(this).val();
        if (value == "0") {
            $("#tab8").trigger("click");
            $("#tab8").addClass("active");
        } else if (value == "1") {
            $("#tab10").trigger("click");
            $("#tab10").addClass("active");
        } else if (value == "2") {
            $("#tab9").trigger("click");
            $("#tab9").addClass("active");
        } else {
            $("#tab7").trigger("click");
            $("#tab7").addClass("active");
        }
    });
    $("#searchForm select[name='refundStates']").change(function () {
        $("#searchButton").trigger("click");
    });
    $("#searchForm select[name='otherPayStates']").change(function () {
        $("#searchButton").trigger("click");
    });
    $("#searchForm select[name='commissionStates']").change(function () {
        $("#searchButton").trigger("click");
    });
    $("#searchForm select[name='year']").change(function () {
        $("#searchButton").trigger("click");
    });
    $("#searchForm select[name='month']").change(function () {
        $("#searchButton").trigger("click");
    });
    var companyName = getQueryString("companyName");
    var custName = getQueryString("custName");
    var incomeState = getQueryString("incomeState");
    var completeStatus = getQueryString("completeStatus");
    if (custName != null && custName != "" && custName != undefined) {
        $("#searchForm [name='completeStatus']").val(completeStatus);
        $("#searchForm [name='incomeStates']").val(incomeState);
        $("#searchForm [name='companyName']").val(companyName);
        $("#dockingPeopleName").val(custName);
        // $("#searchButton").trigger("click") ;
    }

    $("#typeCodeComplete").change(function () {
        var str = $(this).find("option:selected").text();
        $("#typeNameComplete").val(str);
    });
    $("#typeCodeEdit").change(function () {
        var str = $(this).find("option:selected").text();
        $("#typeCodeEdit").val($(this).val());
        $("#typeNameEdit").val(str);
    });


    Views.loadParentMediaType("mType");

    // 加载媒介；
    loadAllMJ("#mediaUserId");

    //搜索条件加载稿件行业类型
    loadTypeCode();
    //搜索条件加载客户行业类型
    loadCustCompanyCode();
    //完善客户加载稿件行业类型
    loadTypeCode("typeCodeComplete");

    //完善客户加载税种
    completeObj.getTax();

    $('#custModal').on('hidden.bs.modal', function () {
        $("#orderModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });
    $('#orderModal').on('hidden.bs.modal', function () {
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        if ($("body").find(".modal").hasClass("in")) {
            $("body").addClass("modal-open");
        }
    });

    $("#incomeTable").jqGrid({
        url: baseUrl + '/income/listPgByArticleId',
        // postData: {id: articleId},
        datatype: "local",
        mtype: 'get',
        height: "auto",
        page: 1,//第一页
        shrinkToFit: true,
        autowidth: true,
        colNames: ['进账编号', '账户名称', '进账人', '进账金额', '进账日期', '分款金额', '分款日期'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'code',
                index: 'code',
                editable: true,
                width: 115,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    // console.log(rowdata)
                    var link = '/fee/queryIncome?id=' + rowdata.id
                    return "<a href='" + link + "' target='_blank'>" + rowdata.code + "</a>";
                    // return "/fee/queryIncome?id=" + rowdata.id;
                }
            },
            {
                name: 'account_name',
                index: 'account_name',
                editable: true,
                width: 200,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'trade_man',
                index: 'trade_man',
                editable: true,
                width: 180,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'trade_amount',
                index: 'trade_amount',
                editable: true,
                width: 120,
                align: "center",
                sortable: false
            },
            {
                name: 'trade_time',
                index: 'trade_time',
                editable: true,
                width: 180,
                align: "center",
                sortable: false,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'amount',
                index: 'amount',
                editable: true,
                width: 180,
                align: "center",
                sortable: false
            },
            {
                name: 'date',
                index: 'date',
                editable: true,
                width: 180,
                align: "center",
                sortable: false,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            }
        ],
        pager: "incomePager"
    });

    $("#table").jqGrid({
        url: baseUrl + '/article/articleListYW',
        postData: $("#searchForm").serializeJson(),
        datatype: "json",
        mtype: 'get',
        // data: mydata,
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 25, //设置多选列宽度
        autoScroll: true,
        shrinkToFit: false,
        sortable: true,
        sortorder: 'asc',
        sortname: 'issuedDate',
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 50, 100, 300],
        // colNames: ['订单ID', '订单编号', '客户公司', '对接人信息','业务员','订单标题',
        //     '支付状态','稿件ID', '类别',"媒体" ,"稿件标题", "链接","发布日期","应收金额","成本","入账金额", "客户答应到款日期","实际到款日期", "媒介" ,
        //      "提成状态","开票状态","发布状态","操作"],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "artId"
        },
        colModel: [
            {
                name: 'id',
                index: 'id',
                editable: false,
                width: 30,
                align: "center",
                sortable: true,
                sorttype: "int",
                search: true,
                cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    //合并单元格
                    return "id='id" + rowId + "'";
                },
                hidden: true
            },
            {
                name: 'companyName',
                index: 'companyName',
                label: '客户公司',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'dockingId',
                index: 'dockingId',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'dockingName',
                index: 'dockingName',
                label: '对接人',
                editable: false,
                width: 70,
                align: "center",
                sortable: true,
                formatter: function (value, grid, rows) {
                    if (rows.userId == user.id) {
                        return statisticsToggle.getSingleLinkHtml(rows.dockingId, rows.dockingName, "cust");
                    } else {
                        if (XMflag) {
                            return value == null ? "" : "*****";
                        } else {
                            return statisticsToggle.getSingleLinkHtml(rows.dockingId, rows.dockingName, "cust");

                        }
                    }

                }
            },
            /*{
                name: 'state',
                index: 'state',
                label:'订单状态',
                editable: false,
                width: 70,
                align: "center",
                sortable: false,
                sorttype: "string",
                cellattr: function (rowId, tv, rawObject, cm, rdata) {
                    return "id='state" + rowId + "'";
                },
                formatter: function (d) {
                    var html = d == 1 ? "<span>已下单</span>" : "<span>未下单</span>";
                    return html;
                }
            },*/
            {
                name: 'artId',
                index: 'artId',
                label: '稿件ID',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'issuedDate',
                index: 'issuedDate',
                label: '发布日期',
                editable: false,
                width: 100,
                align: "center",
                sortable: true,
                hidden: false,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'promiseDate',
                index: 'promiseDate',
                label: '客户答应到款日期',
                editable: false,
                width: 100,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'mTypeName',
                index: 'mTypeName',
                label: '媒体板块',
                editable: false,
                width: 70,
                align: "center",
                sortable: true
            },
            {
                name: 'userId',
                index: 'userId',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'userName',
                index: 'userName',
                label: '业务员',
                editable: false,
                width: 70,
                align: "center",
                sortable: true,
                formatter: function (value, grid, rows) {
                    return statisticsToggle.getSingleLinkHtml(rows.userId, rows.userName, "business");
                }
            },
            {
                name: 'mediaUserName',
                index: 'mediaUserName',
                label: '媒介',
                editable: false,
                width: 70,
                align: "center",
                sortable: true
            },
            {
                name: 'mediaName',
                index: 'mediaName',
                label: '媒体名称',
                editable: false,
                width: 100,
                align: "center",
                sortable: true,
                hidden: false
            },
            {
                name: 'title',
                index: 'title',
                label: '稿件标题',
                editable: false,
                width: 120,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    var html = "";
                    if (d) {
                        if (d && d.length > 15) {
                            var text = d.substring(0, 15);
                            html = "<span title='" + d + "'>" + text + "...</span>";
                        } else {
                            html = d;
                        }

                    }
                    return html;
                }
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
                    if (!v) {
                        return "";
                    } else {
                        var str = row.link.substring(0, 4).toLowerCase();
                        if (str == "http") {
                            return "<a href='" + row.link + "' target='_blank'>" + row.link + "</a>";
                        } else {
                            return "<a href='//" + row.link + "' target='_blank'>" + row.link + "</a>";
                        }
                    }

                }
            },
            {
                name: 'issueStates',
                index: 'issueStates',
                label: '发布状态',
                editable: false,
                width: 70,
                align: "center",
                sortable: false,
                hidden: false,
                formatter: function (value) {
                    switch (value) {
                        case 0 :
                            return "未下单";
                        case 1 :
                            return "待安排";
                        case 2 :
                            return "进行中";
                        case 3 :
                            return "已驳回";
                        case 4 :
                            return "已发布";
                    }
                }
            },
            {
                name: 'num',
                index: 'num',
                label: '数量',
                editable: false,
                width: 50,
                sortable: true,
                align: "center",
            },
            {
                name: 'brand',
                index: 'brand',
                label: '品牌',
                editable: true,
                width: 80,
                align: "center",
                sortable: true,
                hidden: false
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                label: '应收（报价）',
                editable: true,
                width: 100,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                sortable: false
            },
            {
                name: 'incomeAmount',
                index: 'incomeAmount',
                label: '回款',
                editable: false,
                width: 100,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                sortable: true
            },
            {
                name: 'incomeDetail',
                index: 'incomeDetail',
                label: '回款详情',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var a = "";
                    if (XMflag) {
                        if (rowdata.incomeStates == 1) {
                            a = "已回款";
                        } else if (rowdata.incomeStates == 2) {
                            a = "部分回款";
                        } else {
                            a = "";
                        }
                    } else {
                        if (rowdata.incomeStates == 1) {
                            var url = "javascript:void(0) onclick='feeObj.queryIncomeId(" + rowdata.artId + ")'";
                            a = "<a href=" + url + " style='color:#337ab7'>已回款</a>";
                        } else if (rowdata.incomeStates == 2) {
                            var url = "javascript:void(0) onclick='feeObj.queryIncomeId(" + rowdata.artId + ")'";
                            a = "<a href=" + url + " style='color:#337ab7'>部分回款</a>";
                        } else {
                            a = "";
                        }
                    }
                    return a;
                }
            },
            {
                name: 'messState',
                index: 'messState',
                label: '烂账详情',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var a = "";
                    if (rowdata.messState == 1) {
                        a = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='accountMess(" + rowdata.artId + ")'>已烂账</a>";
                    } else if (rowdata.messState == 2) {
                        a = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='accountMess(" + rowdata.artId + ")'>烂账中</a>";
                    } else {
                        a = "";
                    }
                    return a;
                }
            },
            {
                name: 'OutgoDetail',
                index: 'OutgoDetail',
                label: '请款详情',
                editable: false,
                width: 80,
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
                name: 'payAmount',
                index: 'payAmount',
                label: '应付',
                editable: false,
                width: 100,
                align: "center",
                sortable: true,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                hidden: true
            },
            {
                name: 'outgoAmount',
                index: 'outgoAmount',
                label: '成本（请款）',
                editable: false,
                width: 100,
                align: "center",
                sortable: true,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                hidden: false
            },
            {
                name: 'taxes',
                index: 'taxes',
                label: '税金',
                editable: false,
                width: 80,
                align: "center",
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                sortable: true
            },
            {
                name: 'InvoiceDetail',
                index: 'InvoiceDetail',
                label: '开票详情',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.invoiceStates == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='feeObj.queryInvoiceId(" + rowdata.artId + ")'>已开票</a>";
                    } else if (rowdata.invoiceStates == 2) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='feeObj.queryInvoiceId(" + rowdata.artId + ")'>开票中</a>";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {
                name: 'refundAmount',
                index: 'refundAmount',
                label: '退款',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                hidden: false
            },
            {
                name: 'RefundDetail',
                index: 'RefundDetail',
                label: '退款详情',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.refundStates == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='feeObj.queryRefundId(" + rowdata.artId + ")'>已退款</a>";
                    } else if (rowdata.refundStates == 2) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='feeObj.queryRefundId(" + rowdata.artId + ")'>退款中</a>";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {
                name: 'otherPay',
                index: 'otherPay',
                label: '其它支出',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                hidden: false
            },
            {
                name: 'OtherPayDetail',
                index: 'OtherPayDetail',
                label: '其他支出详情',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.otherPayStates == 1) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='feeObj.queryOtherPayId(" + rowdata.artId + ")'>已支出</a>";
                    } else if (rowdata.otherPayStates == 2) {
                        html = "<a href='javascript:void(0)' style='color:#337ab7'  onclick='feeObj.queryOtherPayId(" + rowdata.artId + ")'>支出中</a>";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {
                name: 'profit',
                index: 'profit',
                label: '利润',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                hidden: false
            },
            {
                name: 'commission',
                index: 'commission',
                label: '提成',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                classes: 'text-danger',
                formatter: "currency",
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"},
                hidden: false
            },
            {
                name: 'commissionStates',
                index: 'commissionStates',
                label: '提成状态',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                hidden: true,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.commission == 1) {
                        html = "已提成";
                    } else if (rowdata.commission == 2) {
                        html = "提成中";
                    } else {
                        html = "";
                    }
                    return html;
                }
            },
            {
                name: 'year',
                index: 'year',
                label: '提成年',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                hidden: false
            },
            {
                name: 'month',
                index: 'month',
                label: '提成月',
                editable: false,
                width: 100,
                align: "center",
                sortable: true,
                hidden: false
            },
            {
                name: 'custCompanyType',
                index: 'custCompanyType',
                label: '公司类型',
                editable: false,
                width: 100,
                align: "center",
                sortable: false

            },
            {
                name: 'typeName',
                index: 'typeName',
                label: '稿件行业类型',
                editable: false,
                width: 70,
                align: "center",
                sortable: false,
                hidden: false
            },
            {
                name: 'projectId',
                index: 'projectId',
                label: '项目id',
                editable: false,
                width: 70,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'projectCode',
                index: 'projectCode',
                label: '项目编号',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                hidden: true,
                formatter: function (a, b, rowdata) {
                    if (rowdata.projectId == "" || rowdata.projectId == null || rowdata.projectId == undefined) {
                        return "";
                    } else {
                        return "<a href='javascript:void(0)' style='color:#337ab7'  onclick='queryProjectId(" + rowdata.projectId + ")'>" + rowdata.projectCode + "</a>";
                    }
                }
            },
            {
                name: 'projectName',
                index: 'projectName',
                label: '项目名称',
                editable: false,
                width: 70,
                align: "center",
                sortable: false,
                hidden: true
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
                name: 'remarks',
                index: 'remarks',
                label: '备注',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: false
            }
        ],
        pager: jQuery("#pager"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        loadComplete: function () {//创建表格执行
            $("#jqgh_table_cb").css("padding-right", "6px");
        },
        gridComplete: function () {//数据变动执行
            if (selectIds.length > 0) {//保留选中
                for (var i = 0; i < selectIds.length; i++) {
                    $(this).jqGrid('setSelection', selectIds[i]);
                }
            }
        },
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            var rowData = $("#table").jqGrid("getRowData", rowid);
            Business.edit(rowData.artId);
        },
        onSelectAll: function (aRowids, status) {
            if (status == true) {
                //循环aRowids数组，将code放入arrayNewList数组中
                $.each(aRowids, function (i, item) {
                    //已选中的先排除
                    if (!(selectIds.indexOf(item) > -1)) {
                        gridObj.saveData(item);
                    }
                })
            } else {
                //循环aRowids数组，将code从arrayNewList中删除
                $.each(aRowids, function (i, item) {
                    gridObj.deleteData(item);
                })
            }
        },
        onSelectRow: function (rowid, status) {
            if (status == true) {
                if (!(selectIds.indexOf(rowid) > -1)) {
                    gridObj.saveData(rowid);
                }
            } else {
                gridObj.deleteData(rowid);
            }
        },
        pager: "#pager",
        viewrecords: true,
        caption: "",
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#selected_article_table_logs").jqGrid({
        url: '/articleImport/queryArticleByIds',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#orderForm").serializeJson(),
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: true,
        //setLabel: "序号",
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
        rowNum: 50,//每页显示记录数
        rowList: [10, 50, 100, 500, 1000],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [
            {
                name: 'issued_date', label: '发布日期', editable: false, width: 80,
                formatter: function (d) {
                    if (d != null) {
                        return new Date(d).format("yyyy-MM-dd");
                    } else {
                        return "";
                    }
                }
            },
            {name: 'custCompanyName', label: '客户公司', editable: false, width: 100},
            {name: 'custName', label: '客户联系人', editable: false, width: 60},
            {name: 'brand', label: '品牌', editable: false, width: 80},
            {name: 'media_type_name', label: '媒体板块', editable: false, width: 60},
            {name: 'media_name', label: '媒体名称', editable: false, width: 80},
            {name: 'user_name', label: '业务员', editable: false, width: 60},
            {name: 'media_user_name', label: '媒介', editable: false, width: 60},
            {name: 'title', label: '标题', editable: false, width: 80},
            {name: 'link', label: '链接', editable: false, width: 60},
            {name: 'num', label: '数量', editable: false, width: 60},
            {name: 'price_type', label: '价格类型', editable: false, width: 60},
            {name: 'sale_amount', label: '应收/报价', editable: false, width: 60},
            {name: 'outgo_amount', label: '请款', editable: false, width: 80},
            {
                name: 'create_time', label: '导入日期', editable: false, width: 80,
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'createName', label: '导入人', editable: false, width: 60},
            {name: 'remark', label: '备注', editable: false, width: 80},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 80},
        ],
        pager: "#selected_article_pager_logs",
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

    $("#select_cust_table_logs").jqGrid({
        url: '/crm/company/listCustForYW',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#firstForm").serializeJson(), //发送数据
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
        multiboxonly: true,
        beforeSelectRow: function () {
            $("#select_cust_table_logs").jqGrid('resetSelection');
            return (true);
        },
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
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
                name: 'companyId',
                index: 'companyId',
                label: 'companyId',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'companyName',
                index: 'companyName',
                label: '客户公司名称',
                editable: false,
                width: 140,
                align: "center",
                sortable: true,
                sorttype: "string"
            },
            {
                name: 'type',
                index: 'type',
                label: 'type',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'typeText',
                index: 'typeText',
                label: '客户类型',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (rowdata.type == 1) {
                        return "<span class='text-green'>企业客户</span>";
                    } else if (rowdata.type == 0) {
                        return "<span>个人客户</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'standardize',
                index: 'standardize',
                label: 'standardize',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'standardizeText',
                index: 'standardizeText',
                label: '是否标准',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (rowdata.standardize == 1) {
                        return "标准";
                    } else if (rowdata.standardize == 0) {
                        return "<span class='text-red'>非标准</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'custId',
                index: 'custId',
                label: '对接人id',
                editable: false,
                width: 35,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true,
            },
            {
                name: 'custName',
                index: 'custName',
                label: '对接人名字',
                editable: false,
                width: 60,
                align: "center",
                sortable: true
            },
            {
                name: 'normalize',
                index: 'normalize',
                label: 'normalize',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'normalizeText',
                index: 'normalizeText',
                label: '是否规范',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (rowdata.normalize == 1) {
                        return "<span style='color:#1ab394'>规范</span>";
                    } else if (rowdata.normalize == 0) {
                        return "<span class='text-red'>非规范</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'protectStrong',
                index: 'protectStrong',
                label: '强弱保护',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "<span style='color:#1ab394'>强保护</span>";
                    } else {
                        return "弱保护";
                    }
                }
            },
            {
                name: 'protectLevel',
                index: 'protectLevel',
                label: '保护等级',
                editable: false,
                width: 60,
                align: "center",
                sortable: true,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 3) {
                        return "<span style='color:#1ab394'>A类保护</span>";
                    } else if (a == 2) {
                        return "<span style='color:#1ab394'>B类保护</span>";
                    } else if (a == 1) {
                        return "<span style='color:#1ab394'>C类保护</span>";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'startTime',
                index: 'startTime',
                label: '开始时间',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'endTime',
                index: 'endTime',
                label: '结束时间',
                editable: false,
                width: 80,
                align: "center",
                sortable: true,
                formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {
                name: 'state',
                label: 'state',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'taxType',
                index: 'taxType',
                label: 'taxType',
                editable: false,
                width: 60,
                align: "center",
                hidden: true
            },
            {
                name: 'promiseDay',
                index: 'promiseDay',
                label: 'promiseDay',
                editable: false,
                width: 60,
                align: "center",
                hidden: true
            },
            {
                name: 'stateText',
                label: '状态',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    if (rowdata.state == 1) {
                        return "<span style='color:#1ab394'>有效</span>";
                    } else {
                        return "<span style='color:red'>失效</span>"
                    }
                }
            },
            {
                name: 'operate',
                label: '操作',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.state == 1) {
                        html += "<button class='btn btn-xs btn-primary btn-outline' type='button' title='修改公司名' onclick='companyObj.editCompanyBasic(" + rowdata.custId + ");'>修改公司名</button>";
                    }
                    return html;
                }
            }
        ],
        pager: "#select_cust_pager_logs",
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        loadComplete: function () {
            $("#cb_select_cust_table_logs").hide();
        },
    });

    /*   $("#project_table_logs").jqGrid({
           url: '/project/listPg',
           datatype: "local",
           mtype: 'POST',
           // postData: $("#projectForm").serializeJson(), //发送数据
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
           multiboxonly: true,
           // beforeSelectRow: beforeSelectRow,
           multiselectWidth: 25, //设置多选列宽度
           sortable: "true",
           sortname: "id",
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
               {name: 'id', label: 'id', editable: true, hidden: true, width: 240},
               {name: 'code', label: '项目编号', editable: true, hidden: false, width: 400},
               {name: 'name', label: '项目名称', editable: true, hidden: false, width: 600}
           ],
           pager: "#project_pager_logs",
           viewrecords: true,
           caption: "",
           add: false,
           edit: true,
           addtext: 'Add',
           edittext: 'Edit',
           hidegrid: false,
       });*/
    /*//============================项目管理开始,先隐藏，等待以后是否启用==================================================
    $("#projectModalBtn").click(function () {
        projectGrid.loadGrid();
        $("currentUserId").val(user.id);
        $("#orderModal").modal("hide");
        $("#projectModal").modal({backdrop: "static"});
        resize("#project_table_logs");
    });

    $("#selectProject").click(function () {
        var rowid = $("#project_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = jQuery("#project_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        if (rowData.id == null || rowData.id == undefined || rowData.id == "") {
            swal("请先选择一个项目!");
        } else {
            $("#projectId").val(rowData.id);
            $("#projectCode").val(rowData.code);
            $("#projectName").val(rowData.name);
        }
        $("#orderModal").modal({backdrop: "static"});
        resize("#selected_article_table_logs");
        $("#projectModal").modal("hide");
    });

    $("#cleanProject").click(function () {
        $("#projectId").val("");
        $("#projectCode").val("");
        $("#projectName").val("");
    });

    $("#cancelProject").click(function () {
        $("#orderModal").modal({backdrop: "static"});
        $("#projectModal").modal("hide");
    });

    $("#projectSearch").click(function () {
        $("#project_table_logs").emptyGridParam();
        $("#project_table_logs").jqGrid('setGridParam', {
            postData: $("#projectForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
    //============================项目管理结束==================================================*/
});

var gridObj = {
    saveData: function (item) {
        selectIds.push(item);
    },
    deleteData: function (item) {
        for (var i = 0; i < selectIds.length; i++) {
            if (selectIds[i] == item) {
                selectIds.splice(i, 1);
            }
        }
    },

    reloadTable: function () {
        Business.setArticleResult();
        $("#table").emptyGridParam();
        $("#table").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#searchForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#table");
    },

    reloadIncome: function (articleId) {
        $("#incomeTable").emptyGridParam();
        $("#incomeTable").jqGrid('setGridParam', {
            datatype: "json",
            postData: {id: articleId}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#incomeTable");
    },

    reloadArticle: function () {
        $("#selected_article_table_logs").emptyGridParam();
        $("#selected_article_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#orderForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#selected_article_table_logs");
        completeObj.setSelectedArticleResult();
    },

    reloadCust: function () {
        $("#select_cust_table_logs").emptyGridParam();
        $("#select_cust_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#custForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#select_cust_table_logs");
    },
};

var completeObj = {
    completeModalBtn: function () {
        if (!selectIds || selectIds.length == 0) {
            swal("请先选择稿件")
            return;
        }

        // document.getElementById("orderForm").reset();
        //清除validate错误样式
        $("#orderForm").find("input").removeClass('error');
        $("#orderForm").validate().resetForm();
        $("#ids").val(selectIds.toString());
        $.ajax({
            url: baseUrl + "/articleImport/checkCustInfo",
            data: {ids: selectIds.toString()},
            type: "post",
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    $("#orderModal").modal({backdrop: "static"});
                    if (data.data.entity != null) {
                        if (data.data.entity['hasZeroSale']) {
                            $("#avgPrice").rules("add", {required: true});
                            $("#avgPriceSpan").addClass("text-red");
                        } else {
                            $("#avgPrice").rules("remove", "required");
                            $("#avgPriceSpan").removeClass("text-red");
                        }
                        if (data.data.entity['hasPromise']) {
                            $("#promiseDay").rules("add", {required: false});
                            $("#promiseDaySpan").removeClass("text-red");
                            $("#promiseDaySpan2").removeClass("text-danger");
                        } else {
                            $("#promiseDay").rules("add", {required: true});
                            $("#promiseDaySpan").addClass("text-red");
                            $("#promiseDaySpan2").addClass("text-danger");
                        }
                        $("#startTime").val(data.data.entity["startTime"]);
                        $("#endTime").val(data.data.entity["endTime"]);
                        $("#custStartTime").val(data.data.entity["startTime"]);
                        $("#custEndTime").val(data.data.entity["endTime"]);
                    }
                    setTimeout(function () {
                        gridObj.reloadArticle();
                    }, 500);
                } else if (data.code == 1002) {
                    swal({
                        title: "异常提示",
                        text: data.msg,
                    });
                } else {
                    if (getResCode(data))
                        return;
                }
            }
        })
    },
    //设置选中稿件的统计数据
    setSelectedArticleResult: function () {
        $.ajax({
            url: baseUrl + "/articleImport/querySumArticleByIds",
            data: {ids: $("#ids").val()},
            type: "post",
            dataType: "json",
            success: function (resData) {
                if (resData) {
                    for (var o in resData) {
                        $("#selectedtj #" + o).text(resData[o] == "" ? 0 : resData[o]);
                    }
                } else {
                    $("#selectedtj").find(".text-danger").html(0);
                }
            }
        });
    },


    complete: function () {
        if ($("#orderForm").valid()) {
            /* var amount = $("#taxPoint2").val() ;
             if(!(amount=="")){
                 swal("税点只允许输入小数，且小数位数最多为3位！") ;
                 return ;
             }*/
            var avgPrice = $("#avgPrice").val();
            if (avgPrice && avgPrice == 0) {
                swal("输入金额应大于0");
                return;
            }

            layer.confirm('提交请求？确认后不能退回！', {
                btn: ['确定', '取消'], //按钮
                shade: false //不显示遮罩
            }, function (index) {
                layer.close(index);
                $("#orderForm select[disabled]").each(function () {
                    if (parseInt($(this).val()) != -1) {
                        $(this).attr("disabled", false);
                    }
                });
                var param = $("#orderForm").serialize();
                startModal("#complete1");//锁定按钮，防止重复提交
                layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
                $.ajax({
                    type: "post",
                    data: param,
                    url: "/articleImport/complete",
                    dataType: "json",
                    success: function (data) {
                        $("#orderModal").modal('hide');
                        Ladda.stopAll();   //解锁按钮锁定
                        if (data.code == 200) {
                            swal(data.data.message);
                            selectIds.length = 0;
                            gridObj.reloadTable();
                        } else if (data.code == 1002) {
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;

                        }
                    },
                    error: function () {
                        Ladda.stopAll();//隐藏加载按钮
                    }
                });
            }, function () {
                return;
            });
        }
    },
    // 重置模态框数据
    resetModel:function(){
        $("#orderForm #companyId").val("");
        $("#orderForm #companyName1").val("");
        $("#orderForm #custId").val("");
        $("#orderForm #custName").val("");
        $("#orderForm #taxType1").val("");
        $("#brand2").val("");
        $("#typeCodeComplete").val("");
        $("#promiseDay").val("");
        $("#orderForm #taxType1").val("");
        $("#avgPrice").val("");
        $("#taxPoint2").val("");
    },
    //完善稿件的税
    changeTax: function () {
        var taxType = $("#taxType1").val();
        if (taxType != undefined && taxType != null) {
            if (taxType == 0 || taxType == "") {
                $("#orderForm #taxPoint2").val(0);
            }
            $.ajax({
                type: "post",
                url: "/dict/view",
                data: {typeCode: 'tax', name: taxType},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        if (data.data.entity != null) {
                            $("#orderForm #taxPoint2").val(data.data.entity.code);
                        }
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            })
        } else {
            $("#orderForm #taxPoint2").val("");
        }
    },
    //完善稿件的税种下拉框
    getTax: function () {
        $.ajax({
            type: "get",
            url: "/dict/listDict",
            data: {typeCode: 'tax'},
            dataType: "json",
            success: function (data) {
                $("#taxesDiv").empty();
                var html = "<select class='form-control m-b' name='taxType1' id='taxType1'>";
                // html += "<option value=''></option>";
                if (data != null) {
                    //根据配置决定是否必须开票,系统稿件开票模式：1-必须开票、2-不开票、3-两种模式都兼容，没有配置默认必须开票
                    if (sysConfigMap && sysConfigMap["artTaxModel"] && sysConfigMap["artTaxModel"]["value"] == 1) {
                        for (var i = 0; i < data.length; i++) {
                            html += "<option value='" + data[i].name + "' >" + data[i].name + "</option>";
                        }
                    } else if (sysConfigMap && sysConfigMap["artTaxModel"] && sysConfigMap["artTaxModel"]["value"] == 2) {
                        html += "<option value='0' >" + "不开票" + "</option>";
                    } else {
                        html += "<option value='0' >" + "不开票" + "</option>";
                        for (var i = 0; i < data.length; i++) {
                            html += "<option value='" + data[i].name + "' >" + data[i].name + "</option>";
                        }
                    }
                    html += "</select>";
                    $("#taxesDiv").append(html);
                }
                completeObj.changeTax();
                $("#taxType1").change(function () {
                    completeObj.changeTax();
                })
            }
        })
    }


};

var custObj = {
    custModalBtn: function () {
        startModal("#custModalBtn");
        $("#orderModal").modal("hide");
        $("#custModal").modal({backdrop: "static"});
        setTimeout(function () {
            gridObj.reloadCust();
        }, 500);
        Ladda.stopAll();
    },

    cleanCust: function () {
        startModal("#cleanCust");
        $("#orderForm #companyId").val("");
        $("#orderForm #companyName1").val("");
        $("#orderForm #custId").val("");
        $("#orderForm #custName").val("");
        $("#orderForm #taxType1").val("");
        $("#brand2").val("");
        $("#typeCodeComplete").val("");
        $("#promiseDay").val("");
        $("#orderForm #taxType1").val("");
        $("#avgPrice").val("");
        $("#taxPoint2").val("");
        Ladda.stopAll();
    },

    custSearch: function () {
        gridObj.reloadCust();
    },

    selectCust: function () {
        var rowid = $("#select_cust_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = $("#select_cust_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        if (rowData.custId == null || rowData.custId == undefined || rowData.custId == "") {
            swal("请先选择一个客户!");
        } else {
            if (rowData.normalize == 0) {
                if (rowData.state) {
                    layer.msg("手机号规范的客户才能完善客户，请先修改公司信息！", {time: 1500, shade: [0.7, '#393D49']});
                    companyObj.editCompanyBasic(rowData.custId);
                    return;
                } else {
                    swal("该客户是非规范的，并且失效了，无法使用！");
                    return;
                }
            }
            //清除validate错误样式
            $("#orderForm").find("input").removeClass('error');
            $("#orderForm").validate().resetForm();
            $("#orderForm #companyId").val(rowData.companyId);
            $("#orderForm #companyName1").val(rowData.companyName);
            $("#orderForm #custId").val(rowData.custId);
            $("#orderForm #custName").val(rowData.custName);
            $("#orderForm #taxType1").val(rowData.taxType);
            $("#orderForm #promiseDay").val(rowData.promiseDay);
            completeObj.changeTax();
            $("#custModal").modal("hide");
            $("#orderModal").modal({backdrop: "static"});
        }
    },
};

var feeObj = {
    queryIncomeId: function (articleId) {
        $("#incomeModal").modal({backdrop: "static"});
        gridObj.reloadIncome(articleId);
    },
    queryInvoiceId: function (articleId) {
        $.ajax({
            type: "post",
            url: "/invoice/queryInvoiceId",
            data: {articleId: articleId},
            dataType: "json",
            success: function (dataId) {
                if (dataId > 0) {
                    window.open("/fee/queryInvoice?flag=1&id=" + dataId);
                } else {
                    return;
                }
            }
        });
    },
    queryRefundId: function (articleId) {
        $.ajax({
            type: "post",
            url: "/refund/queryRefundId",
            data: {articleId: articleId},
            dataType: "json",
            success: function (dataId) {
                if (dataId > 0) {
                    window.open("/fee/queryRefund?flag=1&id=" + dataId);
                } else {
                    return;
                }
            }
        });
    },
    queryOtherPayId: function (articleId) {
        $.ajax({
            type: "post",
            url: "/refund/queryOtherPayId",
            data: {articleId: articleId},
            dataType: "json",
            success: function (dataId) {
                if (dataId > 0) {
                    window.open("/fee/queryRefund?flag=1&id=" + dataId);
                } else {
                    return;
                }
            }
        });
    },
};
Number.prototype.toMoney = function () {
    var num = this;
    num = num.toFixed(2);
    num = parseFloat(num);
    num = num.toLocaleString();
    return num;//返回的是字符串23,245.12保留2位小数
};

var companyObj = {
    companySearchUrl: "/company/companySearch",
    checkCompanyUrl: "/company/checkCompany",
    currentCompanyName: "",
    regexStr: /^[1]([3-9])[0-9]{9}$/,
    firstPageTotal: 0, //第一页查询缓存表数据总数
    renderCompanyItem: function (page, pageSize, companyList) {
        var html = "";
        if (companyList && companyList.length > 0) {
            $.each(companyList, function (m, company) {
                html += "<div onmousedown='companyObj.chooseCompany(this);' class=\"companyNameItem\" title=\"" + (company.companyName || "") + "\"><span>" + (company.companyName || "") + "</span></div>";
            });
        }
        return html;
    },
    search: function (t) {
        $(t).closest("form").find(".companyWrap").html("");
        var keyword = $(t).closest("form").find("input[name='companyName']").val();
        if (!keyword) {
            if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
                $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
            }
            return;
        } else {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
        }
        layui.use('flow', function () {
            var flow = layui.flow;
            flow.load({
                elem: $(t).closest("form").find(".companyWrap"),
                isAuto: false,
                done: function (page, next) {
                    //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                    //请求数据
                    var param = {keyword: keyword};
                    param.page = page; //页码
                    param.size = 20; //每页数据条数
                    requestData(param, companyObj.companySearchUrl, "post", function (data) {
                        //第一页是从缓存表拿数据，记录数据总数
                        if (page == 1) {
                            companyObj.firstPageTotal = data.total;
                        }
                        next(companyObj.renderCompanyItem(page, param.size, data.list), page < data.pages); //如果小于总页数，则继续
                    });
                }
            });
        });
    },
    enterEvent: function (t, event) {
        if ((event.keyCode == '13' || event.keyCode == 13)) {
            companyObj.search(t);
        }
    },
    checkCompany: function (t) {
        var keyword = $(t).closest("form").find("input[name='companyName']").val();
        if (!keyword) {
            $(t).closest("form").find(".companyTipsYes").hide();
            $(t).closest("form").find(".companyTipsNo").hide();
            $(t).closest("form").find("input[name='standardize']").val(0);
            return;
        }
        //如果是个体工商户，不需要校验
        if ($(t).closest("form").find("input[name='custProperty']").val() == 1) {
            requestData({keyword: keyword}, companyObj.checkCompanyUrl, "post", function (data) {
                if (data.code == 200) {
                    $(t).closest("form").find(".companyTipsYes").show();
                    $(t).closest("form").find(".companyTipsNo").hide();
                    $(t).closest("form").find("input[name='standardize']").val(1);
                } else {
                    $(t).closest("form").find(".companyTipsYes").hide();
                    $(t).closest("form").find(".companyTipsNo").show();
                    $(t).closest("form").find("input[name='standardize']").val(0);
                }
            });
        }
        // if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
        //     $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        // }
    },
    chooseCompany: function (t) {
        $(t).closest("form").find("input[name='companyName']").val($(t).attr("title") || "");
        //隐藏弹出筛选框
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOut: function (t) {
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOver: function (t) {
        //如果有内容则展示
        if ($(t).closest("form").find(".companyWrap").find("div").length > 0) {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
            $(t).closest("form").find("input[name='companyName']").focus();
        }
    },
    //验证电话号码格式
    regexPhone: function (t) {
        if (!(companyObj.regexStr.test($(t).val()))) {
            $("#normalize").val(0);
            $("#companyUserTipsYes").hide();
            $("#companyUserTipsNo").show();
        } else {
            $("#normalize").val(1);
            $("#companyUserTipsYes").show();
            $("#companyUserTipsNo").hide();
        }
    },

    dropup: function (t) {
        $(t).hide();
        $(t).parent().find(".glyphicon-chevron-down").parent().show();
        $(t).parent().nextAll().hide();
    },

    dropdown: function (t) {
        $(t).hide();
        $(t).parent().find(".glyphicon-chevron-up").parent().show();
        $(t).parent().nextAll().show();
    },

    natureClick: function (t, val, companyName) {
        $(t).closest("div").find("button").each(function (i, btn) {
            if ($(btn).attr("id") === $(t).attr("id")) {
                if ($(btn).hasClass("btn-white")) {
                    $(btn).removeClass("btn-white");
                }
                if (!$(btn).hasClass("btn-info")) {
                    $(btn).addClass("btn-info");
                }
            } else {
                if ($(btn).hasClass("btn-info")) {
                    $(btn).removeClass("btn-info");
                }
                if (!$(btn).hasClass("btn-white")) {
                    $(btn).addClass("btn-white");
                }
            }
        });
        $("#custProperty").val(val);
        $("#addCompanyForm [name='companyName']").val(companyName);
        if (val == 1) {
            $("#addCompanyForm [name='companyName']").removeAttr("disabled");
            if (companyName != null && companyName != "" && companyName != undefined) {
                requestData({keyword: $("#addCompanyForm [name='companyName']").val()}, companyObj.checkCompanyUrl, "post", function (data) {
                    if (data.code == 200) {
                        $("#companyTipsYes").show();
                        $("#companyTipsNo").hide();
                        $("#standardize").val("1");
                    } else {
                        $("#companyTipsYes").hide();
                        $("#companyTipsNo").show();
                        $("#standardize").val("0");
                    }
                });
            } else {
                $("#companyTipsYes").hide();
                $("#companyTipsNo").hide();
            }
        } else {
            $("#standardize").val("0");//个人公司都是非标准
            $("#addCompanyForm [name='companyName']").attr("disabled", "disabled");
            $("#companyTipsYes").hide();
            $("#companyTipsNo").show();
        }
    },

    editCompanyBasic: function (companyUserId) {
        $.ajax({
            type: "get",
            url: "/crm/company/getBasicById",
            data: {id: companyUserId},
            dataType: "json",
            success: function (data) {
                if (data.code == 200) {
                    if (data.data.entity != null) {
                        $("#custModal").modal("hide");
                        $("#orderModal").modal("hide");
                        $("body").addClass("modal-open");
                        $("#editCompanyModal").modal({backdrop: "static"});
                        if (data.data.entity['auditFlag'] == 1) {
                            swal("该客户保护审核中，无法修改客户信息，请等待审核完成后重试！");
                            return;
                        }
                        document.getElementById("addCompanyForm").reset();
                        $("#addCompanyForm").find("input").removeClass('error');//清除验证标签
                        $("#addCompanyForm").validate().resetForm();

                        for (var attr in data.data.entity) {
                            $("#addCompanyForm [name='" + attr + "']").val(data.data.entity[attr]);
                        }
                        var custProperty = data.data.entity['custProperty'];
                        if (custProperty == 1) {//企业客户
                            companyObj.natureClick($("#enterpriseBtn"), custProperty, data.data.entity['companyName']);
                            $("#addCompanyForm").find(".btn").each(function (index, item) {
                                $(item).attr("disabled", "disabled");
                            });
                            if (data.data.entity['standardize'] == 1) {
                                if (user.dept.code == 'YW') {
                                    $("#addCompanyForm [name='companyName']").attr("disabled", "disabled");
                                }
                                $("#companyTipsYes").show();
                                $("#companyTipsNo").hide();
                            } else {
                                $("#addCompanyForm [name='companyName']").removeAttr("disabled");
                                $("#companyTipsYes").hide();
                                $("#companyTipsNo").show();
                            }
                        } else {//个人客户
                            // $("#companyTipsYes").hide();
                            // $("#companyTipsNo").show();
                            $("#addCompanyForm").find(".btn").each(function (index, item) {
                                $(item).removeAttr("disabled");
                            });
                            companyObj.natureClick($("#personalBtn"), custProperty, data.data.entity['companyName']);
                        }

                        if (data.data.entity['normalize'] == 1 || companyObj.regexStr.test(data.data.entity['mobile'])) {
                            if (user.dept.code == 'YW') {
                                $("#addCompanyForm [name='mobile']").attr("readonly", "readonly");
                            }
                            $("#companyUserTipsYes").show();
                            $("#companyUserTipsNo").hide();
                        } else {
                            if (user.dept.code == 'YW') {
                                $("#addCompanyForm [name='mobile']").removeAttr("readonly");
                            }
                            $("#companyUserTipsYes").hide();
                            $("#companyUserTipsNo").show();
                        }
                    }
                }
            }
        })
    },

    saveCommon: function (t) {
        var message = "企业客户公司名称：";
        var custProperty = $("#addCompanyForm [name='custProperty']").val();
        if (custProperty == 1) {
            requestData({keyword: $("#addCompanyForm [name='companyName']").val()}, companyObj.checkCompanyUrl, "post", function (data) {
                if (data.code == 200) {
                    $("#companyTipsYes").show();
                    $("#companyTipsNo").hide();
                    $("#standardize").val("1");
                    message += "<strong>标准</strong>；";
                } else {
                    $("#companyTipsYes").hide();
                    $("#companyTipsNo").show();
                    $("#standardize").val("0");
                    message += "<strong class='text-red'>非标准</strong>；";
                }
            });
        } else {
            message += "您选择的修改的是个人客户，个人客户公司名称统一为个体工商户且公司名称非标准；";
        }
        message += "手机号码：";
        var mobile = $("#addCompanyForm [name='mobile']").val();
        if (!(companyObj.regexStr.test(mobile))) {
            $("#normalize").val("0");
            $("#companyUserTipsYes").hide();
            $("#companyUserTipsNo").show();
            message += "<strong class='text-red'>非规范</strong>";
        } else {
            $("#normalize").val("1");
            $("#companyUserTipsYes").show();
            $("#companyUserTipsNo").hide();
            message += "<strong>规范</strong>！";
        }
        return message;
    },
    updateCompanyBasic: function (t, url) {
        if ($("#addCompanyForm [name='custProperty']").val() == 0) {
            $("#addCompanyForm [name='companyName']").val("个体工商户")
        } else {
            if ($("#addCompanyForm [name='companyName']").val() == "个体工商户") {
                swal("企业客户公司名称不能是个体工商户，请修改后重试！");
                return;
            }
        }
        if ($("#addCompanyForm").valid()) {
            var message = companyObj.saveCommon(t);
            var lock = true;
            var formData = $("#addCompanyForm").serializeJson();
            layer.confirm(message, {
                title: "请确认信息是否填写正确！",
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                startModal("#" + t.id);//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        url: url,
                        data: formData,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();
                            if (data.code == 200) {
                                $("#editCompanyModal").modal("hide");
                                swal("操作成功！");
                                gridObj.reloadCust();
                            } else if (data.code == 1002) {
                                swal({
                                    title: "异常提示",
                                    text: data.msg,
                                });
                            } else {
                                if (getResCode(data))
                                    return;
                            }
                        },
                        error: function (data) {
                            Ladda.stopAll();
                            swal(data.msg);
                        }
                    });
                }
            }, function () {
                return;
            })
        } else {
            return;
        }
    },
};

function accountMess(articleId) {
    $.ajax({
        type: "post",
        url: "/accountMess/selectMessId",
        data: {id: articleId},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                window.open("/accountsMess/accountsMess?flag=4&id=" + data.data.messId);
            } else if (data.code == 1002) {
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
            } else {
                if (getResCode(data))
                    return;
            }

        }
    })
}

//请款详情
function queryOutgoId(articleId) {
    //  rows.id +","+rows.mediaTypeId+","+rows.parentType+",\""+rows.companyCodet+"\","+rows.processType
    $.ajax({
        type: "get",
        url: "/article/getFeeOutgo",
        data: {articleId: articleId},
        dataType: "json",
        success: function (data) {
            if (data) {
                showHistory(data.id, data.mediaTypeId, data.parentType, data.companyCodet, data.processType, data.code, data.applyName);
            } else {
                return;
            }
        }
    });
}

//审核记录查看
function showHistory(id, mediaTypeId, parentType, creatorcompanyCode, processType, outgoCode, applyName) {
    var process;
    var coma = processCompanyCode();
    if (coma && coma.contains(creatorcompanyCode)) {
        process = 24;
    } else {
        if (processType) {
            process = processType
        } else {
            //1是网络
            if (parentType == 1) {
                if (mediaTypeId && mediaTypeId == 8) {
                    process = 22;
                } else {
                    process = 3;
                }
            }
            //2是新媒体
            else {
                if ((mediaTypeId && mediaTypeId == 3)) {
                    process = 25;
                } else {
                    process = 3;
                }
            }
        }
    }
    $("#historyModal .modal-title").html(applyName + " " + outgoCode + " 审核详情")
    //process详见IProcess
    $("#historyModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/process/history",
        data: {dataId: id, process: process},
        dataType: "json",
        success: function (data) {
            if (data.code == 200) {
                $("#history").empty();
                if (data.data.data != null) {
                    var html = "";
                    html += "<div style='position: relative;z-index: 10;'>" +
                        "<div class='form-control'>" +
                        "<div class='col-sm-3 text-center'>审核节点</div>" +
                        "<div class='col-sm-3 text-center'>操作人</div>" +
                        "<div class='col-sm-3 text-center'>操作详情</div>" +
                        "<div class='col-sm-3 text-center'>操作时间</div></div>";
                    for (var i = 0; i < data.data.data.length; i++) {
                        html += "<div class='form-control'>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>" +
                            "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>" +
                            "</div>";
                    }
                    if (process == 24) {
                        html += "</div><div class='col-sm-12 text-center' style='position:relative ' ><img src='/process/getImage?dataId=" + id + "&process=" + process + "&t=" + new Date().getTime() + "' style='width: 136%; margin-left: -193px; margin-top: -100px; margin-bottom: -100px; '/></div>";
                    } else if (process == 22) {
                        html += "</div><div class='col-sm-12 text-center' style='position:relative ' ><img src='/process/getImage?dataId=" + id + "&process=" + process + "&t=" + new Date().getTime() + "' style='width: 136%; margin-left: -193px; margin-top: -100px; margin-bottom: -100px; '/></div>";
                    } else if (process == 25) {
                        html += "</div><div class='col-sm-12 text-center' style='position:relative ' ><img src='/process/getImage?dataId=" + id + "&process=" + process + "&t=" + new Date().getTime() + "' style='width: 136%; margin-left: -193px; margin-top: -100px; margin-bottom: -100px; '/></div>";
                    } else {
                        html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=" + process + "&t=" + new Date().getTime() + "' style='width: 130%; margin-left: -165px; margin-top: -100px; margin-bottom: -100px; '/></div>";
                    }
                    $("#history").append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
};