var XMflag = false;
var url = "";
var businessObj = {
    del: function (id) {
        var lock = true;
        layer.confirm('确认删除？', {
            btn: ['删除', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/refund/del",    //向后端请求数据的url
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            swal(data.data.message);
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        } else if (data.code == 1002){
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    }
                });
            }
        }, function () {
            return;
        });
    },
    //申请人撤回
    returnBack: function (taskId, itemId) {
        var lock = true;
        layer.confirm('确认撤回？', {
            btn: ['撤回', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/process/withdraw",    //向后端请求数据的url
                    data: {taskId: taskId, itemId: itemId},
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            swal(data.data.message);
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    }
                });
            }
        }, function () {
            return;
        });
    },
    //state=2用这个撤回
    CWReject: function (t) {
        var lock = true;
        var id = $("#editForm #id").val();
        layer.confirm('确认撤回？', {
            btn: ['撤回', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#confirm");
            startModal("#CWReject2");
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/refund/CWReject",    //向后端请求数据的url
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            $("#editModal").modal("hide");
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            swal(data.data.message);
                            approveTask($("#taskId").val(), 0, t.id, $("#desc1").val());
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
        }, function () {
            return;
        });
    },
    //state=1||state=12用这个撤回，
    CWReturn: function (t) {
        var lock = true;
        var id = $("#editForm #id").val();
        layer.confirm('确认撤回？', {
            btn: ['撤回', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#review");
            startModal("#rejected");
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/refund/CWReturn",    //向后端请求数据的url
                    data: {id: id},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            $("#editModal").modal("hide");
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            swal(data.data.message);
                            approveTask($("#taskId").val(), 0, t.id);
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
        }, function () {
            return;
        });
    },
    //flag=1审核页面，flag=-1查看页面,flag=0稿件管理跳转到查看页面,flag=2对公账户财务负责人复核出款，flag=3出纳出款
    /**
     * flag 的值对应的页面
     * 0 编辑页面
     * 1 双击查看页面
     * 2 其他页面跳转的查看页面
     * 3 审核页面
     * 4 出纳出款
     * 5 复核出款
     */
    view: function (id, flag) {
        $("#flag").val(flag);
        $("#editModal").modal({backdrop: "static"});
        $(".firstDiv").hide();
        $(".secondDiv").hide();
        $(".thirdDiv").show();
        $("#thirdStep").click();
        document.getElementById("editForm").reset();
        $("#editForm").find("input").removeClass('error');
        $("#editForm").validate().resetForm();

        if (flag == 0) {//编辑
            $("#navTabs").show();
            $("#viewAndConfirm").hide();
            $("#viewDiv").hide();
            $("#editFooter").show();
            $("#viewFooter").hide();
            $("#confirmFooter").hide();
            $("#selectAccountBtnDiv").show();
            $("#editForm .changeOnView").removeClass("col-sm-3");
            $("#editForm .changeOnView").addClass("col-sm-2");
            $("#editModal .modal-title").html("退款信息");
            $(".hideOnEdit").hide();
            $(".showOnEdit").show();
        } else {
            $("#navTabs").hide();
            $("#editFooter").hide();
            $("#selectAccountBtnDiv").hide();
            $("#editForm .changeOnView").removeClass("col-sm-2");
            $("#editForm .changeOnView").addClass("col-sm-3");
            $(".hideOnEdit").show();
            $(".showOnEdit").hide();
            if (flag == 4) {//出款
                $("#viewFooter").hide();
                $("#confirmFooter").show();
                $("#changeAccountBtn").hide();
            } else {
                if (flag == 1) {//查看
                    var returnType = getQueryString("returnType");
                    if (returnType == 5) {
                        // debugger;
                        $("#auditFalse").hide();
                        $("#confirmFooter").hide();
                        $("#editFooter").hide();
                        $("#auditTrue").show();
                        $("#details").hide();
                        $("#pass").hide();
                        $("#reject1").hide();
                    } else {
                        $("#viewFooter").show();
                        $("#auditTrue").hide();
                        $("#auditFalse").show();
                        $("#closeModal").show();//查看页面返回按钮隐藏
                        $("#goback").hide();
                        $("#review").hide();
                        $("#rejected").hide();
                        $("#confirmFooter").hide();
                        $("#rejected").hide();
                    }
                } else if (flag == -1) {
                    $("#auditFalse").hide();
                    $("#confirmFooter").hide();
                    $("#editFooter").hide();
                    $("#auditTrue").show();
                    $("#details").hide();
                    $("#pass").hide();
                    $("#reject1").hide();
                } else if (flag == 2) {//跳转的查看
                    $("#viewFooter").show();
                    $("#auditTrue").hide();
                    $("#auditFalse").show();
                    $("#closeModal").hide();
                    $("#review").hide();
                    $("#rejected").hide();
                    $("#goback").show();
                    $("#confirmFooter").hide();
                } else if (flag == 3) {//审批查看
                    $("#viewFooter").show();
                    $("#auditTrue").show();
                    $("#auditFalse").hide();
                    $("#confirmFooter").hide();
                    $("#details").data("id", id);
                } else if (flag == 5) {//确认公账出款
                    $("#viewFooter").show();
                    $("#auditTrue").hide();
                    $("#auditFalse").show();
                    $("#goback").hide();
                    $("#closeModal").show();
                    $("#review").show();
                    $("#rejected").show();
                    $("#confirmFooter").hide();
                } else if (flag == 6) {
                    $("#auditFalse").hide();
                    $("#auditTrue").hide();
                    $("#editFooter").hide();
                    $("#confirm").hide();
                    $("#CWReject2").hide();
                }
            }
        }

        $.ajax({
            type: "post",
            url: baseUrl + "/refund/view",
            data: {id: id},
            dataType: "json",
            success: function (data) {
                $("#affixDiv").empty();
                if (data.code == 200) {
                    for (var attr in data.data.entity) {
                        $("#editForm [name=" + attr + "]").val(data.data.entity[attr]);
                        if (flag == 0) {
                            if (attr == "custName" ||
                                attr == "custCompanyName" ||
                                attr == "code" ||
                                attr == "applyName" ||
                                attr == "expertPayTime" ||
                                attr == "deptName") {
                                $("#editForm [name=" + attr + "]").attr("readonly", "readonly");
                            }
                            $("#editForm [name=" + attr + "]").removeAttrs("style");
                        } else {
                            $("#editForm [name=" + attr + "]").removeAttrs("readonly");
                            $("#editForm [name=" + attr + "]").prop("style", "border:0;");
                        }
                    }
                    var titleStr = data.data.entity['title'];
                    $("#editModal .modal-title").html("<span class='text-red'>" + titleStr + "</span>")

                    if (data.data.entity["applyId"] == user.id) {
                        $("#custName").val(data.data.entity["custName"]);
                    } else {
                        if (XMflag) {
                            $("#custName").val("*****");
                        } else {
                            $("#custName").val(data.data.entity["custName"]);
                        }
                    }

                    var stateStr;
                    var dataStr = data.data.entity["state"];
                    switch (dataStr) {
                        case -1 :
                            stateStr = "审核驳回";
                            break;
                        case 0 :
                            stateStr = "已保存";
                            break;
                        case 1 :
                            stateStr = "已完成";
                            break;
                        case 2 :
                            stateStr = "审核通过";
                            break;
                        case 3 :
                            stateStr = "组长审核";
                            break;
                        case 4 :
                            stateStr = "部长审核";
                            break;
                        case 5 :
                            stateStr = "总监审核";
                            break;
                        case 6 :
                            stateStr = "财务总监审核";
                            break;
                        case 7 :
                            stateStr = "副总经理审核";
                            break;
                        case 8 :
                            stateStr = "总经理审核";
                            break;
                        case 9 :
                            stateStr = "会计确认出款";
                            break;
                        case 10 :
                            stateStr = "业务员确认";
                            break;
                        case 12 :
                            stateStr = "财务部长审核";
                            break;
                        case 16 :
                            stateStr = "出纳出款";
                            break;
                        default :
                            break;
                    }
                    if (dataStr != null) {
                        //覆盖上面已复制的数据
                        $("#editForm [name='state1']").val(stateStr);
                    }

                    $("#affixLink").empty();
                    $("#affixLink").show();
                    if(data.data.entity["affixName"] != ""){
                        var affixName = data.data.entity["affixName"].split(',');
                        var affixLink = data.data.entity["affixLink"].split(",");
                        if (affixName.length>0 && affixLink.length>0){
                            var html = "";
                            for (var i=0 ; i<affixName.length ; i++) {
                                var filePath = affixLink[i];
                                var fileName = affixName[i];
                                html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                                html += "<a href=" + filePath + " target=_blank  download='"+fileName+"'>下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase() ;
                                var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                                var fileExtArray=[".pdf",".xls",".xlsx",".ppt",".pptx",".csv",".doc",".wps",".docx",".txt",".html",".sql"];
                                if(fileName.indexOf(".")>-1){
                                    var str=fileExt + '|';
                                    if(strFilter.indexOf(str)>-1){//是图片
                                        html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";
                                    }else{
                                        if(fileExtArray.contains(fileExt)){
                                            html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                        }
                                    }
                                }else {
                                    html += "<a onclick=\"previewFile('"+fileName+"','"+filePath+"',0)\" data-id='" + filePath + "'>预览:</a><br/>";
                                }
                            }
                            $("#affixDiv").append(html);
                        }
                    }
                    if (data.data.entity["type"] == 0) {
                        $("#hideByType").hide();
                        // document.getElementById("hideByType").style.visibility="hidden";//隐藏
                        $("#editForm [name='applyAmount']").removeAttrs("readonly");
                    } else {
                        $("#hideByType").show();
                        // document.getElementById("hideByType").style.visibility="visible";//显示
                        $("#editForm [name='applyAmount']").attr("readonly", "readonly");
                        setTimeout(function () {
                            gridObj.reloadSelectedArticle(id);
                        }, 500);
                    }
                    if (flag == 4 && hasRoleCWCN() && (data.data.entity['state'] == 2 || data.data.entity['state'] == 16) || (data.data.entity['state'] == 1 && flag == 6 && hasRoleCWBZ())) {
                        $("#viewAndConfirm").show();
                        $(".showOnView").hide();
                        $(".showOnConfirm").show();
                        if ($("#payAmount").val() == 0) {
                            var applyAmount = $("#applyAmount").val() == null ? 0 : $("#applyAmount").val();
                            $("#payAmount").val(parseFloat(applyAmount));
                        }
                        $("#editForm #outAccountName").removeAttrs("style");
                        $("#editForm #payTime").removeAttrs("style");
                        $("#editForm #payTime").attr("readonly", "readonly");
                        $("#editForm #payAmount").attr("readonly", "readonly");
                        if (data.data.entity['outAccountId']) {
                            $("#editForm #outAccountSelect").val(data.data.entity['outAccountId']);
                            $("#editForm #outAccountIds").val(data.data.entity['outAccountId']);
                            layui.use(["form"], function () {
                                layui.form.render('select');
                            });
                        }
                    } else if (data.data.entity['state'] == 1 || data.data.entity['state'] == 12 || data.data.entity['state'] == 9) {//已出款，直接显示出款文本
                        $("#viewAndConfirm").show();
                        $(".showOnView").show();
                        $(".showOnConfirm").hide();
                    } else {//未出款，不显示出款账户信息
                        $("#viewAndConfirm").hide();
                    }
                } else if (data.code == 1002) {
                    swal({
                        title: "异常提示",
                        text: data.msg,
                    });
                    $("#editModal").modal("hide");
                } else {
                    if (getResCode(data))
                        return;
                }
            }
        });
    },
    //财务负责人确认出款
    checkBtoB: function () {
        var lock = true;
        layer.confirm('确认出款？', {
            btn: ['确认', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#review");//锁定按钮，防止重复提交
            startModal("#rejected");//锁定按钮，防止重复提交
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/refund/checkBtoB",    //向后端请求数据的url
                    data: {id: $("#id").val()},
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();//解锁按钮锁定
                        if (data.code == 200) {
                            $("#editModal").modal('hide');
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            swal(data.data.message);
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
        }, function () {
            return;
        });
    },

    saveStepOne: function (t) {
        var lock = true;
        startModal("#" + t.id);//锁定按钮，防止重复提交
        layer.confirm('请确认信息无误？确定后不能更改！', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/refund/saveStepOne",    //向后端请求数据的url
                    data: $("#secondForm").serializeJson(),
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();   //解锁按钮锁定
                        if (data.code == 200) {
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            setTimeout(function () {
                                businessObj.view(data.data.entity.id, 0);
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
                    },
                    error: function () {
                        Ladda.stopAll();
                    }
                });
            }
        }, function () {
            Ladda.stopAll();   //解锁按钮锁定
            return;
        });
    },

    submitHander: function (t, url, state) {
        if (!($("#applyAmount").val() > 0)) {
            swal("申请金额必须大于0");
            return;
        }
        var lock = true;
        if ($("#editForm").valid()) {
            var tips;
            if (state == 0) {
                $("#state").val(state);
                tips = "确认保存？";
            } else {
                $("#state").val(state);
                tips = "请确认提交退款信息？提交后不能取消";
            }
            layer.confirm(tips, {
                btn: ['确定', '取消'], //按钮
                shade: false //不显示遮罩
            }, function (index) {
                layer.close(index);
                //有图片添加传参
                var formData = new FormData($("#editForm")[0]);
                // var param = $("#editForm").serializeJson();
                startModal("#save");//锁定按钮，防止重复提交
                startModal("#update");//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        data: formData,
                        url: url,
                        dataType: "json",
                        async: true,
                        cache: false,
                        contentType: false,
                        processData: false,
                        success: function (data) {
                            Ladda.stopAll();   //解锁按钮锁定
                            if (data.code == 200) {
                                $("#editModal").modal('hide');
                                $("#query_table_logs").emptyGridParam();
                                $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
                                swal(data.data.message);
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
                            Ladda.stopAll();
                        }
                    });
                }
            }, function () {
                return;
            });
        }
    },

    submitHanderCW: function (t, url) {
        var lock = true;
        if ($("#outAccountIds").val() == undefined || $("#outAccountIds").val() == "" || $("#outAccountIds").val() == null) {
            swal("请先选择出款账户！")
            return;
        }
        if ($("#editForm").valid()) {
            layer.confirm('请确认退款信息？提交后不能取消！', {
                btn: ['确定', '取消'], //按钮
                shade: false //不显示遮罩
            }, function (index) {
                layer.close(index);
                var param = $("#editForm").serializeJson();
                param.desc = $("#desc1").val();
                startModal("#" + t.id);//锁定按钮，防止重复提交
                startModal("#CWReject2");//锁定按钮，防止重复提交
                if (lock) {
                    lock = false;
                    $.ajax({
                        type: "post",
                        data: param,
                        url: url,
                        dataType: "json",
                        success: function (data) {
                            Ladda.stopAll();   //解锁按钮锁定
                            if (data.code == 200) {
                                $("#editModal").modal('hide');
                                swal(data.data.message);
                                $("#outAccountIds").val("");
                                $("#query_table_logs").emptyGridParam();
                                $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
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
                            Ladda.stopAll();
                        }
                    });
                }
            }, function () {
                return;
            });
        }
    },

    reimbursementSum: function () {
        $.ajax({
            type: "post",
            data: $("#queryForm").serializeJson(),
            url: baseUrl + "/refund/reimburseSum",
            dataType: "json",
            async: false,
            success: function (resData) {
                if (resData) {
                    $("#applyAmount1").text(resData.applyAmount || 0);
                    $("#payAmount1").text(resData.payAmount || 0);
                } else {
                    $("#tj").find(".text-danger").htmleditForm(0);
                }

            }
        })
    },

    changeAccount: function () {
        var lock = true;
        if ($("#outAccountIds").val() == undefined || $("#outAccountIds").val() == "" || $("#outAccountIds").val() == null) {
            swal("请先选择出款账户！");
            return;
        }
        layer.confirm('请确认请款信息？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            var param = {
                id: $("#id").val(),
                outAccountIds: $("#outAccountIds").val(),
                payTime: $("#payTime").val(),
            };
            startModal("#changeAccountBtn");//锁定按钮，防止重复提交
            if (lock) {
                lock = false;
                $.ajax({
                    type: "post",
                    data: param,
                    url: baseUrl + "/refund/changeAccount",
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();   //解锁按钮锁定
                        if (data.code == 200) {
                            $("#editModal").modal('hide');
                            swal(data.data.message);
                            $("#outAccountIds").val("");
                            $("#query_table_logs").emptyGridParam();
                            $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
                        } else if (data.code == 1002) {
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            $("#editModal").modal('hide');
                            if (getResCode(data))
                                return;
                        }
                    },
                    error: function () {
                        Ladda.stopAll();
                    }
                });
            }
        }, function () {
            return;
        });
    }
};

//初始化业务部门
function getDept() {
    var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
    var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
    var deptDiv = document.getElementById("deptDiv");
    //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
    if (((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'YW' || user.dept.code == 'GL')) || user.dept.code == 'CW') {
        deptDiv.style.display = 'block';

        $("#selDept").click(function () {
            $("#deptModal").modal({backdrop: "static"});
        });
        $('#treeview').treeview({
            data: [getTreeData(isZC())],
            onNodeSelected: function (event, data) {
                $("#deptId").val("");//每次选择时，先清空
                $("#chooseDeptName").val(data.text);
                $("#deptModal").modal('hide');
                $("#deptId").val(data.id);
            }
        });
        $("#cleanDept").click(function () {
            $("#companyCode1").val("");//清空
            $("#businessUserId").empty();//初始化
            $("#businessUserId").append('<option value="">全部</option>');
            $("#deptId").val("");
            $("#chooseDeptName").val("");
        });
    }
}

//判断当前用户是否总裁
var isZC = function () {
    var roles = user.roles;//获取用户角色
    var isZC = false;//是否总裁角色
    if (roles) {
        for (var i = 0; i < roles.length; i++) {
            if (roles[i].code == 'ZC' || roles[i].code == 'FZC') {
                isZC = true;
                break;
            }
        }
    }
    return isZC;
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
}

//获取部门树数据
function getTreeData(isZC) {
    var deptTreeData = {};
    var deptId = user.dept.id;//当前用户部门ID
    var deptCode = user.dept.code;//当前部门编码
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    if (deptCompanyCode == "JT" && (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL")) {
        requestData(null, "/dept/getRootDept", "POST", function (result) {
            var root = result.data.root;
            if (root) {
                deptId = root.id;//整个集团的业务和媒介部
            } else {
                deptId = 517;//整个集团的业务和媒介部
            }
        });
    } else if (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL") {
        requestData({companyCode: deptCompanyCode}, "/dept/getCompanyByCode", "POST", function (result) {
            var company = result.data.company;
            if (company) {
                deptId = company.id;//整个集团的业务和媒介部
            }
        });
    }
    //具体查询
    requestData({deptId: deptId, deptCode: 'YW'}, "/dept/listAllDeptByIdAndCode", "POST", function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

var navObj = {
    initCust: function (type) {
        //第二步返回第一步的时候没有传type，用之前选中的type值
        if (type == undefined || type == null) {//type=0时 type==''为true,所以不能加type==''这个条件
            type = $("#chooseRefundType").val();
        } else {
            $("#chooseRefundType").val(type);//全额退款type=0,部分退款type=1,其他支出type=2
        }
        if (type == 1) {
            url = baseUrl + '/refund/listPgForSelectArticle';
        } else if (type == 2) {
            url = baseUrl + '/refund/listPgForSelectArticle2';
        }
        document.getElementById("editForm").reset();
        $("#typeSec").val(type);//全额退款type=0,部分退款type=1,其他支出type=2
        $("#flag").val("");
        $(".firstDiv").show();
        $(".secondDiv").hide();
        $(".thirdDiv").hide();
        $("#firstStep").click();
        $(".modal-title").html("退款或其他支出信息");
        $("#editForm [name='expertPayTime']").attr("readonly", "readonly");
        $("#editModal").modal({backdrop: "static"});
        $("#custCompanyIdSec").val("");
        $("#custCompanyNameSec").val("");
        $("#custIdSec").val("");
        $("#custNameSec").val("");
        $("#chooseCustId").val("");//账户模态框选中的客户隐藏域
        $("#articleIdsSec").val("");
        $("#order").empty();
        setTimeout(function () {
            gridObj.reloadCust();
        }, 500);
        $("#refresh").off("click").on("click", function () {
            navObj.initCust(type);
        });
    },
    initArticle: function (t) {
        var rowid = $("#select_cust_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = $("#select_cust_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        if (rowData.custId == null || rowData.custId == undefined || rowData.custId == "") {
            swal("请先选择一个客户!");
        } else {
            $("#custCompanyIdSec").val(rowData.companyId);
            $("#custCompanyNameSec").val(rowData.companyName);
            $("#custIdSec").val(rowData.custId);
            $("#custNameSec").val(rowData.custName);
            $("#chooseCustId").val(rowData.custId);//账户模态框选中的客户隐藏域

            $("#startTimeSec").val(rowData.startTime);
            $("#endTimeSec").val(rowData.endTime);
            $("#startTimeText").html(rowData.startTime);
            $("#endTimeText").html(rowData.endTime);
            if ($("#typeSec").val() == 0) {
                businessObj.saveStepOne(t);
            } else {
                if(rowData.type == 1){//type=1时为true，表示公司客户
                    if(rowData.state){//state=1时为true表示有效
                        if(rowData.standardize == 0 || rowData.normalize == 0){
                            layer.msg("企业客户必须标准且规范才能退款，请先修改公司名！", {time: 2500, shade: [0.7, '#393D49']});
                            companyObj.editCompanyBasic(rowData.custId);
                            return;
                        }
                    }else{//state=0时为false表示失效
                        if(rowData.standardize == 0 || rowData.normalize == 0){
                            swal("该企业客户是弱保护客户，且已失效，不能退款！");
                            return;
                        }
                    }
                }else{//type=0时为false，表示个体工商户
                    if(rowData.state){//state=1时为true表示有效
                        if(rowData.normalize == 0){
                            layer.msg("个体工商户必须是规范的才能退款，请先修改公司名！", {time: 2500, shade: [0.7, '#393D49']});
                            companyObj.editCompanyBasic(rowData.custId);
                            return;
                        }
                    }else{//state=0时为false表示失效
                        if(rowData.standardize == 0 || rowData.normalize == 0){
                            swal("该个体工商户是非规范客户，且已失效，不能退款！");
                            return;
                        }
                    }
                }
                $(".firstDiv").hide();
                $(".secondDiv").show();
                $("#secondStep").click();
                //加载稿件信息
                gridObj.reloadSelectArticle(url);
            }
        }
        if ($("#typeSec").val() > 0) {
            $("#refresh").off("click").on("click", function () {
                gridObj.reloadSelectArticle(url);
            });
        }
        ;
    },
    selectArticle: function (t) {
        var rowid = $("#select_article_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = $("#select_article_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        if (rowData.id == null || rowData.id == undefined || rowData.id == "") {
            swal("请先选择一个稿件!");
        } else {
            if (!($("#refundTotal").val() > 0)) {
                swal("退款金额必须大于0");
                return;
            }
            $("#articleIdsSec").val(rowData.id);
            businessObj.saveStepOne(t);
        }
    }
}
$(document).ready(function () {
    //加载打印权限
    downLoadAndPrintObj.loadFlowPrintPermission();

    getDept();
    var orderHtml = "";
    $.jgrid.defaults.styleUI = 'Bootstrap';

    if (hasRoleCWBZ()) {
        $("#exportBtn").show();
    } else {
        $("#exportBtn").hide();
    }
    $("#exportBtn").click(function () {
        var params = removeBlank($("#queryForm").serializeJson());
        location.href = "/refund/exportRefund?" + $.param(params);
    });
    var project = projectDirector();
    if (project && project.contains(user.id)) {
        XMflag = true;
    } else {
        XMflag = false;
    }

    if (hasRoleYW()) {
        $(".addBtn").show();
    } else {
        $(".addBtn").hide();
    }
    businessObj.reimbursementSum();

    //flag=1审核，否则查看
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        businessObj.view(getQueryString("id"), getQueryString("flag"));
    }

    if (getQueryString("approveId") != null && getQueryString("approveId") != "" && getQueryString("approveId") != undefined) {
        businessObj.view(getQueryString("approveId"), 4);
    }

    if (getQueryString("editId") != null && getQueryString("editId") != "" && getQueryString("editId") != undefined) {
        businessObj.view(getQueryString("editId"), 0);
    }

    $('#accountModal').on('hidden.bs.modal', function () {
        $("#editModal").modal({backdrop: "static"});
        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $("body").addClass("modal-open");
    });

    $('#editCompanyModal').on('hidden.bs.modal', function () {
        $("#editModal").modal({backdrop: "static"});
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

    $("#queryForm select[name='stateQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#queryForm select[name='typeQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#querySearch").click(function () {
        gridObj.reloadTable();
        businessObj.reimbursementSum();
    });

    $("#custSearch").click(function () {
        gridObj.reloadCust();
    });
    $("#articleSearch").click(function () {
        $("#order").empty();
        $("#articleIdsSec").val("");
        gridObj.reloadSelectArticle(url);
    });

    $("#accountSearch").click(function () {
        gridObj.reloadAccount();
    });

    $("#refresh").click(function () {
        if ($("#flag").val()) {
            businessObj.view($("#id").val(), $("#flag").val());
        }
    });

    layui.use('form', function () {
        var form = layui.form;
        $.ajax({
            type: "post",
            url: "/account/queryCompanyAccountList",
            data: {companyCode: user.dept.companyCode},
            dataType: "json",
            success: function (data) {
                var html = "<option value=''></option>";
                for (var i = 0; i < data.length; i++) {
                    html += "<option value='" + data[i].id + "'>" + data[i].name + "</option>";
                }
                $("#editForm select[name='outAccountIds']").append(html);
                form.render('select');
                form.on('select', function (data) {
                    $("#outAccountIds").val(data.value);
                });
            }
        })
    });

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/refund/listPg',
        datatype: "json",
        mtype: 'POST',
        postData: $("#queryForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
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
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'code', label: '申请编号', editable: true, width: 120},
            {
                name: 'type', label: '类型', editable: true, width: 80,
                formatter: function (value, grid, rows) {
                    switch (rows.type) {
                        case 0 :
                            return "<span style=''>直接退款</span>";
                        case 1 :
                            return "<span style='color:blue'>稿件退款</span>";
                        case 2 :
                            return "<span style='color:red'>其他支出</span>";
                    }
                }
            },
            {name: 'title', label: '标题', editable: true, width: 120},
            {name: 'applyName', label: '申请人', editable: true, width: 60},
            {name: 'applyTime', label: '申请时间', editable: true, width: 120},
            {name: 'deptName', label: '申请部门', editable: true, width: 60},
            {name: 'custCompanyName', label: '客户公司名称', editable: true, width: 120},
            {
                name: 'custName', label: '客户联系人', editable: true, width: 80,
                formatter: function (value, grid, rows) {
                    if (rows.applyId == user.id) {
                        return value;
                    } else {
                        if (XMflag) {
                            return "******";
                        } else {
                            return value;
                        }
                    }
                }
            },
            {name: 'accountName', label: '收款人', editable: true, width: 60},
            {name: 'accountBankNo', label: '收款账号', editable: true, width: 120},
            {name: 'accountBankName', label: '收款银行', editable: true, width: 80},
            {name: 'applyAmount', label: '申请金额', editable: true, width: 80},
            {name: 'outAccountName', label: '实际出款账号', editable: true, width: 120},
            {name: 'payAmount', label: '实际出款金额', editable: true, width: 80},
            {name: 'payTime', label: '实际出款日期', editable: true, width: 80},
            {name: 'taskId', label: 'taskId', editable: true, hidden: true, width: 80},
            {name: 'state', label: 'state', editable: true, hidden: true, width: 80},
            {name: 'applyId', label: 'applyId', editable: true, hidden: true, width: 80},
            {name: 'itemId', label: 'itemId', editable: true, hidden: true, width: 80},
            {
                name: 'state1', label: '状态', editable: true, width: 80,
                formatter: function (value, grid, rows) {
                    switch (rows.state) {
                        case -1 :
                            return "<span style='color:red'>审核驳回</span>";
                        case 0 :
                            return "<span style=''>已保存</span>";
                        case 1 :
                            return "<span style=''>已完成</span>";
                        case 2 :
                            return "<span style='color:red'>审核通过</span>";
                        case 3 :
                            return "<span style='color:red'>组长审核</span>";
                        case 4 :
                            return "<span style='color:red'>部长审核</span>";
                        case 5 :
                            return "<span style='color:red'>总监审核</span>";
                        case 6 :
                            return "<span style='color:red'>财务总监审核</span>";
                        case 7 :
                            return "<span style='color:red'>副总经理审核</span>";
                        case 8 :
                            return "<span style='color:red'>总经理审核</span>";
                        case 9 :
                            return "<span style='color:red'>会计确认出款</span>";
                        case 10 :
                            return "<span style='color:red'>业务员确认</span>";
                        case 12 :
                            return "<span style='color:red'>财务部长审核</span>";
                        case 16 :
                            return "<span style='color:red'>出纳出款</span>"
                    }
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 180,
                formatter: function (value, grid, rows) {
                    var html = "";
                    //如果有打印下载权限，则展示
                    if (downLoadAndPrintObj.dowloadAndPritPermission) {
                        html += "<a href='javascript:;' onclick='downLoadAndPrintObj.viewModalShow(" + rows.id + ");'>预览&nbsp;</a>";
                    }
                    // if (rows.isOwner && rows.applyId==user.id ) {//审批通过的链接有问题，先注释
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='businessObj.view(" + rows.id + ",3)'>审批&nbsp;&nbsp;</a>";
                    // }
                    if (rows.taskId != null && rows.taskId != "") {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='showHistory(" + rows.id + ")'>审核详情 &nbsp;&nbsp;</a>";
                    }
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='businessObj.view(" + rows.id + ",0)'>编辑 &nbsp;&nbsp;</a>";
                    }
                    // html += "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='businessObj.view(" + rows.id + ",0)'>查看&nbsp;&nbsp;</a>";
                    if ((rows.state == 0 || rows.state == -1) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red;'  onclick='businessObj.del(" + rows.id + ")'>删除 &nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWCN() && (rows.state == 2 || rows.state == 16) && rows.acceptWorker == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='businessObj.view(" + rows.id + ",4)'>出纳出款&nbsp;&nbsp;</a>";
                    }
                    if (hasRoleCWKJ() && rows.state == 9 && rows.acceptWorker == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='businessObj.view(" + rows.id + ",5)'>确认公账出款</a>";
                    }
                    if (hasRoleCWBZ() && rows.state == 1) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='businessObj.view(" + rows.id + ",6)'>更改出款信息&nbsp;&nbsp;</a>";
                    }
                    //审核中才能撤回
                    if ((rows.state == 3 || rows.state == 4
                        || rows.state == 5 || rows.state == 6
                        || rows.state == 7 || rows.state == 8
                        || rows.state == 10 || rows.state == 12) && rows.applyId == user.id) {
                        html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue;'  onclick='businessObj.returnBack(" + "\"" + rows.taskId + "\"," + rows.itemId + ")'>撤回&nbsp;&nbsp;</a>";
                    }
                    // if (hasRoleCWCN()&&rows.state == 12) {
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='businessObj.CWReturn(" + rows.id + ")'>出纳撤回&nbsp;&nbsp;</a>";
                    // }
                    // if (hasRoleCWCN()&&rows.state == 2) {
                    //     html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: red'  onclick='businessObj.CWReject(" + rows.id + ")'>出纳撤回&nbsp;&nbsp;</a>";
                    // }
                    return html;
                }
            },
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            businessObj.view(rowid, 1)
        },
    });
    resize("#query_pager_logs");
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
                        return "<span style='color:#1ab394'>企业客户</span>";
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
                        return "<span style='color:#1ab394'>标准</span>";
                    } else if(rowdata.standardize == 0) {
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
                    } else if(rowdata.normalize == 0) {
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
                name: 'stateText',
                label: '状态',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    if (rowdata.state == 1){
                        return "<span style='color:#1ab394'>有效</span>";
                    }else{
                        return "<span style='color:red'>失效</span>"
                    }
                }
            },
            {
                name: 'operate',
                label: '操作',
                editable: false,
                width: 150,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if (rowdata.state == 1) {
                        html += "<button class='btn btn-xs btn-primary btn-outline' type='button' title='修改公司名' onclick='companyObj.editCompanyBasic(" + rowdata.custId + ");'>修改公司名</button>";
                    }
                    if(rowdata.standardize != 1  && rowdata.type == 1 ){
                        html += "&nbsp;&nbsp;";
                        var url = "/crm/queryStandardizedCompany?flag=3&companyName="+rowdata.companyName;
                        html += "<button class='btn btn-xs btn-default btn-outline' title='申请为标准公司' onclick=\"page('"+url+"','申请为标准公司');\">申请为标准公司</button>";
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
    $("#select_article_table_logs").jqGrid({
        // url: baseUrl+'/refund/listPgForSelectArticle',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#secondForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        // multiboxonly: true,
        beforeSelectRow: function () {
            orderHtml = "";
            $("#order").empty();
            $("#select_article_table_logs").jqGrid('resetSelection');
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
            // {name: 'no', label: '订单编号', editable: true, width: 120},
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
                    return new Date(d).format("yyyy-MM-dd");
                }
            },
            {name: 'companyName', label: '客户公司名称', editable: true, width: 120},
            {name: 'custName', label: '客户联系人', editable: true, width: 60},
            {name: 'brand', label: '品牌', editable: true, width: 80},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 60},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 120},
            {name: 'innerOuter', label: '内外部', editable: true, width: 80},
            {name: 'channel', label: '频道', editable: true, width: 80},
            {name: 'link', label: '链接', editable: true, hidden: true, width: 80},
            {
                name: 'title', label: '标题', editable: true, width: 160,
                formatter: function (v, options, row) {
                    if (v == undefined || v == null || v == "") {
                        return "";
                    } else {
                        var link = row.link;
                        if (!(link == undefined || link == null || link == "")) {
                            var str = link.substring(0, 4).toLowerCase();
                            if (str == "http") {
                                return "<a href='" + link + "' target='_blank'>" + v + "</a>";
                            } else {
                                return "<a href='//" + link + "' target='_blank'>" + v + "</a>";
                            }
                        } else {
                            return v;
                        }
                    }
                }
            },
            {name: 'num', label: '数量', editable: true, width: 60},
            {name: 'mediaUserName', label: '媒介', editable: true, width: 60},
            {name: 'saleAmount', label: '报价/应收', editable: true, width: 80},
            {name: 'incomeAmount', label: '回款', editable: true, width: 80},
            {name: 'outgoAmount', label: '成本/请款', editable: true, width: 80}
        ],
        pager: $("#select_article_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        loadComplete: function () {
            $("#cb_select_article_table_logs").hide();
        },
        onSelectRow: function (rowid, status) {
            if (status == true) {
                gridObj.saveData(rowid);
            } else {
                gridObj.deleteIndexData(rowid);
            }
        }
    });
    $("#selected_article_table_logs").jqGrid({
        url: baseUrl + '/refund/listPgForSelectedArticle',
        datatype: "local",
        mtype: 'POST',
        // postData: {id:id}, //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: false,
        // multiboxonly: true,
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
            {name: 'issuedDate', label: '发布日期', editable: true, width: 120},
            {name: 'companyName', label: '客户公司名称', editable: true, width: 120},
            {
                name: 'custName', label: '客户联系人', editable: true, width: 60,
                formatter: function (value, grid, rows) {
                    if (rows.userId == user.id) {
                        return value;
                    } else {
                        if (XMflag) {
                            return "******";
                        } else {
                            return value;
                        }
                    }
                }
            },
            {name: 'brand', label: '品牌', editable: true, width: 80},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 60},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 120},
            {name: 'innerOuter', label: '内外部', editable: true, width: 80},
            {name: 'channel', label: '频道', editable: true, width: 80},
            {name: 'mediaUserName', label: '媒介', editable: true, width: 60},
            {name: 'link', label: '链接', editable: true, hidden: true, width: 80},
            {
                name: 'title', label: '标题', editable: true, width: 160,
                formatter: function (v, options, row) {
                    if (v == undefined || v == null || v == "") {
                        return "";
                    } else {
                        var link = row.link;
                        if (!(link == undefined || link == null || link == "")) {
                            var str = link.substring(0, 4).toLowerCase();
                            if (str == "http") {
                                return "<a href='" + link + "' target='_blank'>" + v + "</a>";
                            } else {
                                return "<a href='//" + link + "' target='_blank'>" + v + "</a>";
                            }
                        } else {
                            return v;
                        }
                    }
                }
            },
            {name: 'num', label: '数量', editable: true, width: 60},
            {name: 'saleAmount', label: '报价', editable: true, width: 60},
            {name: 'incomeAmount', label: '回款', editable: true, width: 60},
            {name: 'outgoAmount', label: '成本/请款', editable: true, width: 60},
            {name: 'refundAmount', label: '退款', editable: true, width: 80},
            {name: 'otherPay', label: '其它支出', editable: true, width: 80},
            {
                name: 'state', label: '退稿状态', editable: true, width: 60, formatter: function (d) {
                    if (d == -9) {
                        return "<span class='text-red'>已删</span>";
                    }
                    return "正常";
                }
            }
        ],
        pager: $("#selected_article_pager_logs"),
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });
    $("#account_table_logs").jqGrid({
        url: baseUrl + '/account/queryCustAccount',
        datatype: "local",
        mtype: 'POST',
        // postData: {custId:custId,
        //     ownerQc:$("#ownerQc2").val(),
        //     bankNameQc:$("#bankNameQc2").val(),
        //     bankNoQc:$("#bankNoQc2").val()
        // }, //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        multiboxonly: true,
        beforeSelectRow: function () {
            $("#account_table_logs").jqGrid('resetSelection');
            return (true);
        },
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
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'companyName', label: '客户名称', editable: true, width: 240},
            {name: 'name', label: '账户名称', editable: true, width: 180},
            {name: 'owner', label: '账户户主', editable: true, width: 240},
            {name: 'bankNo', label: '银行账号', editable: true, width: 240},
            {name: 'bankName', label: '账号开户行', editable: true, width: 240},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 0},
        ],
        pager: $("#account_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });
});
var accountObj = {
    selAccount: function () {
        //加载银行账户
        gridObj.reloadAccount();
        $("#editModal").modal('hide');
        $("#accountModal").modal({backdrop: "static"});
    },
    cleanAccount: function () {
        $("#accountId").val("");
        $("#accountName").val("");
        $("#accountBankNo").val("");
        $("#accountBankName").val("");
    },
    selectAccount: function () {
        var rowid = $("#account_table_logs").jqGrid("getGridParam", "selrow");     //获取选中行id
        var rowData = jQuery("#account_table_logs").jqGrid("getRowData", rowid);   //获取选中行信息
        $("#accountId").val(rowData.id);
        $("#accountName").val(rowData.owner);
        $("#accountBankNo").val(rowData.bankNo);
        $("#accountBankName").val(rowData.bankName);
        $("#accountModal").modal('hide');
        $("#editModal").modal({backdrop: "static"});
    },
};

var gridObj = {
    saveData: function (obj) {
        var rowData = $("#select_article_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
        orderHtml = '<tr id="row' + rowData.id + '"><td  class="hide">' + rowData.id + '</td>' +
            '<td>' + rowData.issuedDate + '</td><td>' + rowData.companyName + '</td><td>' + rowData.custName + '</td>' +
            '<td>' + rowData.brand + '</td>' +
            '<td>' + rowData.mediaTypeName + '</td>' +
            '<td>' + rowData.mediaName + '</td>' +
            '<td>' + rowData.innerOuter + '</td>' +
            '<td>' + rowData.channel + '</td>' +
            '<td>' + rowData.title + '</td>' +
            '<td>' + rowData.num + '</td><td>' + rowData.mediaUserName + '</td>' +
            '<td>' + rowData.saleAmount + '</td><td>' + rowData.incomeAmount + '</td>' +
            '<td><input style="width: 100%" type="text" name="refund_' + rowData.id + '" id="refundTotal" value="0" onkeyup="value=value.replace(/[^\\d\\.\\-]/g,\'\')"></td></tr>';
        $("#order").append(orderHtml);
    },
    deleteIndexData: function (obj) {
        $("#row" + obj).remove();
    },
    reloadTable: function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    },
    reloadCust: function () {
        $("#select_cust_table_logs").emptyGridParam();
        $("#select_cust_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#firstForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#select_cust_table_logs");
    },
    reloadSelectArticle: function (url) {
        $("#select_article_table_logs").emptyGridParam();
        $("#select_article_table_logs").jqGrid('setGridParam', {
            url: url,
            datatype: 'json',
            postData: $("#secondForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //
        resize("#select_article_table_logs");
    },
    reloadSelectedArticle: function (id) {
        $("#selected_article_table_logs").emptyGridParam();
        $("#selected_article_table_logs").jqGrid('setGridParam', {
            datatype: 'json',
            postData: {id: id}, //发送数据
        }).trigger("reloadGrid"); //
        resize("#selected_article_table_logs");
    },
    reloadAccount: function () {
        $("#account_table_logs").emptyGridParam();
        $("#account_table_logs").jqGrid('setGridParam', {
            postData: $("#accountForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#account_table_logs");
    }
};

//审核记录查看
function showHistory(id) {
    //process详见IProcess
    $("#historyModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/process/history",
        data: {dataId: id, process: 4},
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
                    html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process=4&t=" + new Date().getTime() + "' style='width: 135%; margin-left: -185px; margin-top: -24px;margin-bottom: -100px'/></div>";
                    $("#history").append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        }
    });
}

//审核通过
function approve(t) {
    approveTask($("#taskId").val(), 1, t.id,$("#desc").val())
}

//审核驳回
function reject(t) {
    approveTask($("#taskId").val(), 0, t.id,$("#desc").val(),function () {
        $("#editModal").modal("hide");
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").reloadCurrentData(baseUrl + "/refund/listPg", $("#queryForm").serializeJson(), "json", null, null);
        return 1;
    })
}

// function filePreview(t) {
//     var filePath = $(t).attr("data-id").trim();
//     var currentServerUrl = previewUrl;
//     //var currentServerUrl = "http://40848e84.ngrok.io/";
//
//     $("#docPreviewModal iframe").attr('src',"https://view.officeapps.live.com/op/view.aspx?src="+currentServerUrl+filePath);
//     $("#docPreviewModal").modal('show');
// }
//下载打印功能
var downLoadAndPrintObj = {
    dowloadAndPritPermission: false, //打印权限控制
    state: {
        "-1": "审核驳回",
        "0": "已保存",
        "1": "已完成",
        "2": "审核通过",
        "3": "组长审核",
        "4": "部长审核",
        "5": "总监审核",
        "6": "财务总监审核",
        "7": "副总经理审核",
        "8": "总经理审核",
        "9": "会计确认出款",
        "10": "业务员确认",
        "12": "财务部长审核",
        "16": "出纳出款"
    },
    type: {"0": "直接退款", "1": "稿件退款", "2": "其他支出"},
    downloadMap: {},
    loadFlowPrintPermission: function () {
        requestData(null, "/refund/getFlowPrintPermission", "post", function (data) {
            if (data.code == 200) {
                downLoadAndPrintObj.dowloadAndPritPermission = true;
                $("#batchDownload").css("display", "inline-block");
                $("#batchPrint").css("display", "inline-block");
            } else {
                downLoadAndPrintObj.dowloadAndPritPermission = false;
                $("#batchDownload").css("display", "none");
                $("#batchPrint").css("display", "none");
            }
        });
    },
    viewModalShow: function (id) {
        layer.open({
            type: 1,
            title: 0,
            zIndex: 10,
            content: $("#refundViewModal").html(),
            btn: ['下载', '打印', '取消'],
            area: ['70%', '60%'],
            closeBtn: 0,
            shadeClose: 0,
            resize: false,
            move: '.modalTitle',
            moveOut: true,
            success: function (layero, index) {
                downLoadAndPrintObj.downloadMap = {};
                requestData({id: id}, "/refund/view", "post", function (data) {
                    if (data.code == 200) {
                        var title = "【退款】" + data.data.entity["code"] + "-" + data.data.entity["applyName"] + "-" + data.data.entity["applyTime"];
                        downLoadAndPrintObj.downloadMap["modalTitle"] = title;
                        $(layero[0]).find("div .modalTitle").text(title);
                        for (var attr in data.data.entity) {
                            //退款类型
                            if (attr == 'type') {
                                $(layero[0]).find("div ." + attr).text(downLoadAndPrintObj.type[data.data.entity[attr]] || "");
                                downLoadAndPrintObj.downloadMap[attr] = downLoadAndPrintObj.type[data.data.entity[attr]] || "";
                            } else if (attr == "state") {
                                $(layero[0]).find("div ." + attr).text(downLoadAndPrintObj.state[data.data.entity[attr]] || "");
                                downLoadAndPrintObj.downloadMap[attr] = downLoadAndPrintObj.type[data.data.entity[attr]] || "";
                            } else {
                                $(layero[0]).find("div ." + attr).text(data.data.entity[attr] || "");
                                downLoadAndPrintObj.downloadMap[attr] = data.data.entity[attr] || "";
                            }
                        }
                    }
                });
                requestData({dataId: id, process: 4}, "/process/history", "post", function (data) {
                    if (data.code == 200) {
                        $(layero[0]).find(".auditTable").css("display", "flex");
                        var html = "";
                        if (data.data.data && data.data.data.length > 0) {
                            $.each(data.data.data, function (i, item) {
                                html += "<tr>\n" +
                                    "        <td>" + (item.name || "") + "</td>\n" +
                                    "        <td>" + (item.user || "") + "</td>\n" +
                                    "        <td>" + (item.desc || "") + "</td>\n" +
                                    "        <td>" + (item.time || "") + "</td>\n" +
                                    "    </tr>";
                                var descArr = item.desc.split("</");
                                item.descName = descArr && descArr.length > 0 ? descArr[0].substring(descArr[0].indexOf(">") + 1) : "";
                            });
                            downLoadAndPrintObj.downloadMap["auditTable"] = JSON.stringify(data.data.data);
                            $(layero[0]).find(".auditTable").find("tbody").html(html);
                        } else {
                            $(layero[0]).find(".auditTable").css("display", "none");
                        }
                    } else {
                        $(layero[0]).find(".auditTable").css("display", "none");
                    }
                });
            },
            yes: function (index, layero) {
                requestData(downLoadAndPrintObj.downloadMap, "/refund/downloadData", "post", function (data) {
                    if (data.code == 200) {
                        if (data.data.message) {
                            layer.msg(data.data.message, {time: 2000, icon: 5});
                        }
                        if (data.data.file) {
                            window.location.href = data.data.file;
                        }
                    } else {
                        layer.msg(data.msg, {time: 2000, icon: 5});
                    }
                });
                return false;
            },
            btn2: function (index, layero) {
                var wind = window.open("", 'newwindow', 'height=300, width=700, top=100, left=100, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=n o, status=no');
                wind.document.body.innerHTML = $(layero[0]).find("#modalContentWrap")[0].outerHTML;
                wind.print();
                return false;
            }
        });
    },
    batchPrintData: function () {
        var html = "";
        var wind1 = window.open("", 'newwindow', 'height=300, width=700, top=100, left=100, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=n o, status=no');
        requestData($("#queryForm").serializeJson(), "/refund/batchPrintData", "post", function (data) {
            if (data.code == 200) {
                if (data.data.list && data.data.list.length > 0) {
                    $.each(data.data.list, function (k, borrow) {
                        for (var attr in borrow) {
                            if (attr == "auditTable") {
                                var auditHtml = "";
                                if (borrow[attr] && borrow[attr].length > 0) {
                                    $.each(borrow[attr], function (i, item) {
                                        auditHtml += "<tr>\n" +
                                            "        <td>" + (item.name || "") + "</td>\n" +
                                            "        <td>" + (item.user || "") + "</td>\n" +
                                            "        <td>" + (item.descName || "") + "</td>\n" +
                                            "        <td>" + (item.time ? new Date(item.time).format("yyyy-MM-dd hh:mm:ss") : "") + "</td>\n" +
                                            "    </tr>";
                                    });
                                    $("#modalContentWrap").find(".auditTable").css("display", "flex");
                                    $("#modalContentWrap").find(".auditTable").find("tbody").html(auditHtml);
                                } else {
                                    $("#modalContentWrap").find(".auditTable").css("display", "none");
                                }
                            } else {
                                $("#modalContentWrap").find("." + attr).text("");
                                $("#modalContentWrap").find("." + attr).text(borrow[attr] || "");
                            }
                        }
                        html += $("#modalContentWrap")[0].outerHTML + "\n";
                    });
                } else {
                    layer.msg('无退款打印数据！', {time: 2000, icon: 6});
                    return;
                }
            } else {
                layer.msg(data.msg, {time: 2000, icon: 5});
                return;
            }
        });
        if (wind1) {
            if (html) {
                wind1.document.body.innerHTML = html;
                wind1.print();
            } else {
                wind1.close();
            }
        }
    },
    batchDownloadData: function () {
        requestData($("#queryForm").serializeJson(), "/refund/batchDownloadData", "post", function (data) {
            if (data.code == 200) {
                if (data.data.message) {
                    layer.msg(data.data.message, {time: 2000, icon: 5});
                }
                if (data.data.file) {
                    window.location.href = data.data.file;
                }
            } else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        });
    }
}

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
                        $("#editModal").modal("hide");
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