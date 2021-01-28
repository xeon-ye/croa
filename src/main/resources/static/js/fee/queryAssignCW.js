var arrayNewList = [];
var mediaUserPlateMap =[];
function view(id) {
    document.getElementById("viewForm").reset();
    $("#viewModal").modal({backdrop: "static"});
    $.ajax({
        type: "post",
        url: baseUrl + "/income/view",
        data: {id: id},
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.entity) {
                $("#viewForm [name=" + attr + "1]").val(data.data.entity[attr]);
                // $("[name="+attr+"]").attr("readonly","readonly");
            }
            $("#selected_article_table_logs1").jqGrid('setGridParam', {
                datatype: "json",
                postData: {incomeId: id}, //发送数据
            }).trigger("reloadGrid"); //重新载入

            $("#income_user_table_logs").jqGrid('setGridParam', {
                datatype: "json",
                postData: {id: id}, //发送数据
            }).trigger("reloadGrid"); //重新载入

            //分配领款
            $("#dispatchIncome").off("click").on("click", function () {
                var lock = true ;
                if ($("#name").val().length == 0 || $("#amount").val().length == 0) {
                    swal("请选选择领款人和金额")
                } else {
                    // console.log($("#id1").val()+"\t"+$("#unclaimedAmount1").val()+"\t"+$("#amount").val()) ;
                    // console.log($("#amount").val()>$("#unclaimedAmount1").val())
                    if ($("#amount").val() - $("#unclaimedAmount1").val() > 0.0001) {
                        swal("可分配金额不足！\n可用金额=" + $("#unclaimedAmount1").val() + "\n本次分配金额=" + $("#amount").val());
                    } else {
                        startModal("#dispatchIncome");
                        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
                        if(lock){
                            lock = false ;
                            $.ajax({
                                type: "post",
                                url: baseUrl + "/income/dispatch",    //向后端请求数据的url
                                data: {incomeId: $("#id1").val(), userId: $("#name").val(), amount: $("#amount").val()},
                                dataType: "json",
                                success: function (data) {
                                    Ladda.stopAll();
                                    if (data.code == 200) {
                                        layer.msg(data.data.message, {time: 1000, icon: 6});
                                        document.getElementById("viewForm").reset();
                                        $.ajax({
                                            type: "post",
                                            url: baseUrl + "/income/view",
                                            data: {id: $("#id1").val()},
                                            dataType: "json",
                                            success: function (data) {
                                                for (var attr in data.data.entity) {
                                                    $("#viewForm [name=" + attr + "1]").val(data.data.entity[attr]);
                                                }
                                            }
                                        });
                                        $("#assign_table_logs").emptyGridParam();
                                        $("#assign_table_logs").jqGrid('setGridParam', {
                                            datatype: "json",
                                            postData: $("#assignForm").serializeJson(), //发送数据
                                        }).trigger("reloadGrid"); //重新载入
                                        $("#income_user_table_logs").emptyGridParam();
                                        $("#income_user_table_logs").jqGrid('setGridParam', {
                                            datatype: "json",
                                            postData: {id: $("#id1").val()}, //发送数据
                                        }).trigger("reloadGrid"); //重新载入
                                        $("#selected_article_table_logs1").emptyGridParam();
                                        $("#selected_article_table_logs1").jqGrid('setGridParam', {
                                            datatype: "json",
                                            postData: {incomeId: $("#id1").val()}, //发送数据
                                        }).trigger("reloadGrid"); //重新载入
                                    } else if(data.code == 1002) {
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
                }
            })
        }
    });
}

//退回分款钱留在业务员账上，可以重新分款
function backAssign(incomeId, userId) {
    var lock = true ;
    layer.confirm('退回分款？退回后业务员可以重新分款！', {
        btn: ['退回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/income/backAssign",    //向后端请求数据的url
                data: {incomeId: incomeId, userId: userId},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        $("#assign_table_logs").emptyGridParam();
                        $("#assign_table_logs").jqGrid('setGridParam', {
                            postData: $("#assignForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#income_user_table_logs").emptyGridParam();
                        $("#income_user_table_logs").jqGrid('setGridParam', {
                            datatype: "json",
                            postData: {id: incomeId}, //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#selected_article_table_logs1").emptyGridParam();
                        $("#selected_article_table_logs1").jqGrid('setGridParam', {
                            datatype: "json",
                            postData: {incomeId: incomeId}, //发送数据
                        }).trigger("reloadGrid"); //重新载入
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
}

//退回领款钱要回去，业务员重新领款
function backIncome(incomeId, userId) {
    var lock = true ;
    layer.confirm('退回领款？退回后业务员可以重新领款！', {
        btn: ['退回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/income/backIncome",    //向后端请求数据的url
                data: {incomeId: incomeId, userId: userId},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        document.getElementById("viewForm").reset();
                        $.ajax({
                            type: "post",
                            url: baseUrl + "/income/view",
                            data: {id: incomeId},
                            dataType: "json",
                            success: function (data) {
                                for (var attr in data.data.entity) {
                                    $("#viewForm [name=" + attr + "1]").val(data.data.entity[attr]);
                                }
                            }
                        });
                        $("#assign_table_logs").emptyGridParam();
                        $("#assign_table_logs").jqGrid('setGridParam', {
                            postData: $("#assignForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#income_user_table_logs").emptyGridParam();
                        $("#income_user_table_logs").jqGrid('setGridParam', {
                            datatype: "json",
                            postData: {id: incomeId}, //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#selected_article_table_logs1").emptyGridParam();
                        $("#selected_article_table_logs1").jqGrid('setGridParam', {
                            datatype: "json",
                            postData: {incomeId: incomeId}, //发送数据
                        }).trigger("reloadGrid"); //重新载入
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
}
//单个稿件退回分款钱留在业务员账上，可以重新分款
function backAssignArticle(incomeId, articleId) {
    var lock = true ;
    layer.confirm('退回单个稿件的分款？', {
        btn: ['退回', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/income/backAssignArticle",    //向后端请求数据的url
                data: {incomeId: incomeId, articleId: articleId},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        $("#assign_table_logs").emptyGridParam();
                        $("#assign_table_logs").jqGrid('setGridParam', {
                            postData: $("#assignForm").serializeJson(), //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#income_user_table_logs").emptyGridParam();
                        $("#income_user_table_logs").jqGrid('setGridParam', {
                            datatype: "json",
                            postData: {id: incomeId}, //发送数据
                        }).trigger("reloadGrid"); //重新载入
                        $("#selected_article_table_logs1").emptyGridParam();
                        $("#selected_article_table_logs1").jqGrid('setGridParam', {
                            datatype: "json",
                            postData: {incomeId: incomeId}, //发送数据
                        }).trigger("reloadGrid"); //重新载入
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
}
function resize(table) {
    if (table == undefined) return;
    var width = $(table).parents(".jqGrid_wrapper").width();
    if (width == 0) return;
    $(table).setGridWidth(width);
}

$(document).ready(function () {
    getDept();
    var e = "<i class='fa fa-times-circle'></i> ";
    $("#assignForm").validate({
        rules: {
            tradeAmount: {number: true}
        }, message: {
            tradeAmount: {required: e + "请输入正确的进款金额"}
        }
    });
    mediaUserPlateMap = userMedaiPlateList();

    $.jgrid.defaults.styleUI = 'Bootstrap';

    $("#assign_table_logs").jqGrid({
        url: baseUrl + '/income/listPgForAssignCW',
        datatype: "json",
        mtype: 'POST',
        postData: $("#assignForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        // setLabel: "序号",
        // autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: false,
        autoScroll: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'incomeUserId', label: 'incomeUserId', editable: true, hidden: true, width: 60},
            {name: 'articleId', label: 'articleId', editable: true, hidden: true, width: 60},
            {name: 'incomeId', label: 'incomeId', editable: true, hidden: true, width: 60},
            {
                name: 'incomeCode', label: '进账编号', editable: true, width: 120,
                formatter: function (a, b, rowdata) {
                    if (!rowdata.incomeCode) {
                        return "";
                    }
                    var html = '<a href="javascript:void(0)" style="margin-right:3px;color: green;" onclick="view(' + rowdata.incomeId + ')">${content}</a>';
                    html = html.replace("${href}", "/fee/queryAssign?id=" + rowdata.incomeId);
                    html = html.replace("${content}", rowdata.incomeCode);
                    return html;
                }
            },
            {name: 'accountName', label: '收款账户', editable: true, width: 120},
            // {name: 'tradeBank', label: '付款账户', editable: true, width: 120},
            {name: 'tradeMan', label: '进账人', editable: true, width: 120},
            {name: 'tradeTime', label: '进款日期', editable: true, width: 120},
            {name: 'tradeAmount', label: '交易金额', editable: true, width: 80},
            {name: 'unclaimedAmount', label: '未领金额', editable: true, width: 80},
            {name: 'preclaimedAmount', label: '预领金额', editable: true, width: 80},
            {name: 'level', label: '等级', editable: true, width: 60},
            {name: 'createTime', label: '登记日期', editable: true, width: 120},
            {name: 'visiableDay', label: '可领天数', editable: true, hidden: true, width: 80},
            {name: 'userName', label: '业务员', editable: true, width: 80},
            {name: 'deptName', label: '部门', editable: true, width: 120},
            {name: 'receiveAmount', label: '已领款金额', editable: true, width: 80},
            {name: 'receiveTime', label: '领款时间', editable: true, width: 120},
            {name: 'assignAmount', label: '已分款金额', editable: true, width: 80},
            {name: 'remainAmount', label: '可分款金额', editable: true, width: 80},
            {name: 'companyName', label: '客户公司名称', editable: true, width: 120},
            {name: 'custName', label: '客户联系人', editable: true, width: 80},
            {
                name: 'supplierName', label: '供应商', editable: true, width: 120,
                formatter: function (a, b, rowdata) {
                    if (user.dept.companyCode == 'XH' || rowdata.companyCode == user.dept.companyCode) {
                        return rowdata.supplierName == null ? "" : rowdata.supplierName;
                    } else {
                        return "";
                    }
                }
            },
            {
              name:'supplierContactor',label:'供应商联系人',editable: true,width:60
            },
            {name: 'phone', label: '联系人手机号', editable: true, width: 60,formatter:function(value, grid, rows){
                return  supplierPhone(value, grid, rows);
            }},
            {name: 'mediaName', label: '媒体', editable: true, width: 120},
            {name: 'mediaUserName', label: '媒介', editable: true, width: 80},
            {name: 'title', label: '标题', editable: true, width: 120},
            {name: 'link', label: '链接', editable: true, width: 120},
            {name: 'issuedDate', label: '发布日期', editable: true, width: 120},
            {name: 'assignDate', label: '分款日期', editable: true, width: 120},
            {name: 'saleAmount', label: '报价', editable: true, width: 100},
            {name: 'amount', label: '已分款金额', editable: true, width: 100}
            // {
            //     name: 'operate', label: "操作", index: '',width: 180,
            //     formatter: function (value, grid, rows, state) {
            //         // var be = "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='view(" + rows.incomeId + ")'>查看</a>";
            //         // var be = '<a href="/fee/viewAccount?id='+rows.id+'" style="color:#f60" >查看</a>';
            //         //var se = '<a href="/role/del?id='+rows.id+'" style="color:#f60" >删除</a>';
            //         var de = "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='assignIncome(" + rows.incomeId + ")'>分款</a>";
            //         return  "     " + de + "        ";
            //     }
            // }
        ],

        pager: jQuery("#assign_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        // ondblClickRow: function (rowid, iRow, iCol, e) {
        //     //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
        //     // var rowData = jQuery("#assign_table_logs").jqGrid("getRowData", rowid);
        //     // view(rowData.incomeId);
        // },
        gridComplete: function () {
            var rowData = $(this).jqGrid('getRowData');
            //遍历所有的行，如果是选中的，说明它在数组中，让他选中
            for (var i = 0, n = rowData.length; i < n; i++) {
                var item = rowData[i];
                //判断是否存在数据
                if (arrayNewList.length > 0) {
                    if (arrayNewList.indexOf(item.id) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_assign_pager_logs_" + item.id).attr("checked", true);
                    }
                }
            }
        },
        loadComplete: function (xhr) {
            var array = xhr.list;
            if (arrayNewList.length > 0) {
                $.each(array, function (i, item) {
                    if (arrayNewList.indexOf(item.id) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_assign_pager_logs_" + item.id).attr("checked", true);
                    }
                });
            }

        },
        onSelectAll: function (aRowids, status) {
            if (status == true) {
                //循环aRowids数组，将code放入arrayNewList数组中
                $.each(aRowids, function (i, item) {
                    //已选中的先排除
                    if (!(arrayNewList.indexOf(item) > -1)) {
                        saveData(item);
                    }
                    // saveData(item);
                })
            } else {
                //循环aRowids数组，将code从arrayNewList中删除
                $.each(aRowids, function (i, item) {
                    deleteIndexData(item);
                })
            }

        },
        onSelectRow: function (rowid, status) {
            if (status == true) {
                if (!(arrayNewList.indexOf(rowid) > -1)) {
                    saveData(rowid);
                }
            } else {
                deleteIndexData(rowid);

            }
        }
    });

    function saveData(obj) {
        arrayNewList.push(obj);
    }

    function deleteIndexData(obj) {
        //获取obj在arrayNewList数组中的索引值
        for (i = 0; i < arrayNewList.length; i++) {
            $("#row" + obj).remove();
            if (arrayNewList[i] == obj) {
                //根据索引值删除arrayNewList中的数据
                arrayNewList.splice(i, 1);
                // i--;
            }
        }
    }

    $("#assignSearch").click(function () {
        // alert(JSON.stringify($("#role").serializeJson()));
        $("#assign_table_logs").emptyGridParam();
        $("#assign_table_logs").jqGrid('setGridParam', {
            postData: $("#assignForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });


    $("#exportBtn").click(function () {
        var params =  removeBlank($("#assignForm").serializeJson());
        location.href = "/income/exportIncomeDetail" + "?" + $.param(params);
    });

    $("#income_user_table_logs").jqGrid({
        url: baseUrl + '/income/listPgIncomeUserByIncomeId',
        datatype: "local",
        mtype: 'POST',
        // postData: {id: id}, //发送数据
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
            {name: 'id', label: 'id', editable: true, hidden: true, width: 150},
            {name: 'name', label: '领款人姓名', editable: true, width: 150},
            {name: 'deptName', label: '部门', editable: true, width: 150},
            {name: 'receiveAmount', label: '领款金额', editable: true, width: 150},
            {name: 'assignAmount', label: '已分款金额', editable: true, width: 150},
            {name: 'remainAmount', label: '剩余金额', editable: true, width: 150},
            {name: 'receiveTime', label: '领款日期', editable: true, width: 150},
            {
                name: 'operate', label: "操作", index: '', width: 290,
                formatter: function (value, grid, rows, state) {
                    var html = "";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;color:blue'  onclick='backAssign(" + rows.incomeId + "," + rows.userId + ")'>分款撤回&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;color:darkred'  onclick='backIncome(" + rows.incomeId + "," + rows.userId + ")'>领款撤回&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>";
                    return html;
                }
            }
        ],
        pager: jQuery("#income_user_pager_logs"),
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });

    $("#selected_article_table_logs1").jqGrid({
        url: baseUrl + '/income/listPgForSelectedArticle',
        datatype: "local",
        mtype: 'POST',
        // postData: {incomeId: id}, //发送数据
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
        rowList: [10, 50, 100, 500],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'code', label: '进款编号', editable: true, width: 100},
            {name: 'incomeId', label: 'incomeId', editable: true, hidden: true, width: 60},
            {name: 'articleId', label: 'articleId', editable: true, hidden: true, width: 60},
            {name: 'issuedDate', label: '发布日期', editable: true, width: 80},
            {name: 'companyName', label: '客户公司名称', editable: true, width: 100},
            {name: 'custName', label: '对接人', editable: true, width: 80},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 80},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 100},
            {name: 'title', label: '标题', editable: true, width: 120},
            {
                name: 'link', label: '链接', editable: true, width: 120,
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
            {name: 'mediaUserName', label: '媒介', editable: true, width: 80},
            {name: 'saleAmount', label: '应收/报价', editable: true, width: 80},
            {name: 'assignAmount', label: '分款金额', editable: true, width: 80},
            {name: 'assignDate', label: '分款日期', editable: true, width: 120},
            {
                name: 'operate', label: "操作", index: '', width: 80,
                formatter: function (value, grid, rows) {
                    var html = "";
                    html += "<a href='javascript:void(0)' style='height:22px;width:40px;color: blue'  onclick='backAssignArticle(" + rows.incomeId + ","+rows.articleId+")'>&nbsp;撤回分款&nbsp;</a>";
                    return html;
                },
            },
        ],
        pager: jQuery("#selected_article_pager_logs1"),
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
    });

});

//初始化业务部门
function getDept() {
    var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
    var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
    //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
    if (((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'YW' || user.dept.code == 'GL')) || user.dept.code == 'CW') {
        $("#selDept").click(function () {
            $("#deptModal").modal({backdrop: "static"});
        });
        $('#treeview').treeview({
            data: [getTreeData(isZC())],
            onNodeSelected: function (event, data) {
                $("#companyCode1").val("");//每次选择时，先清空
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


/**
 * 加载用户所拥有的媒体板块
 */
function userMedaiPlateList() {
    var mediaUserPlateMap=[];
    $.ajax({
        url: baseUrl + "/mediaPlate/userId",  //mediaType/listByUserId
        data: {"userId": user.id},
        type: "post",
        dataType: "json",
        success: function (data) {
            if (data) {
                for (var i = 0; i < data.length; i++) {
                    mediaUserPlateMap.push(data[i].id)
                }
            }
        }
    });
    return mediaUserPlateMap;
}



function supplierPhone(value, grid, rows) {
    // var flag = false;
    // if (rows.hasOwnProperty('plateIds')){
    //     var plateIds = rows.plateIds.split(",");
    //     if (plateIds) {
    //         for (var i = 0; i < plateIds.length; i++) {
    //             if (mediaUserPlateMap.contains(plateIds[i])) {
    //                 //当前用户的板块包含了该供应商的板块
    //                 flag = true;
    //             }
    //         }
    //     }
    // }

    value = rows.phone || "";
    if(value){
        // if((rows.creator == user.id)|| (flag && hasRoleMJBZ()) || (flag && hasRoleMJZZ()) || (flag && hasRoleMJZJ()) || hasRoleCW() ){
        //     return value;
        // }else {
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

