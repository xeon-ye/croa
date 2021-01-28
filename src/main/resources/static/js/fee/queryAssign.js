var arrayNewList = [];
var businessObj = {
    reimbursementSum: function() {
        $.ajax({
            type:"post",
            data:$("#assignForm").serializeJson(),
            url: baseUrl + "/income/reimburseSum",
            dataType:"json",
            async:false,
            success:function (resData) {
                $("#tj").find(".text-danger").text("0");
                if (resData){
                    $("#tradeAmount2").text(resData.tradeAmount || 0);
                    $("#receiveAmount2").text(resData.receiveAmount || 0);
                    $("#assignAmount2").text(resData.assignAmount || 0);
                    $("#remainAmount2").text(resData.remainAmount || 0);

                }
            }
        })
    },
    view: function (id) {
        $("#refreshId").val(id);
        $("#viewModal").modal({backdrop: "static"});
        $.ajax({
            type: "post",
            url: baseUrl + "/income/assignView",
            data: {incomeId: id},
            dataType: "json",
            success: function (data) {
                for (var attr in data.data.entity) {
                    $("#viewForm [name=" + attr + "1]").val(data.data.entity[attr]);
                    // $("[name="+attr+"]").attr("readonly","readonly");
                }
                gridObj.reloadSelected(id);
            }
        });
    },

    assignIncome: function (id) {
        $("#refreshArticleId").val(id);
        $("#incomeId").val(id);
        arrayNewList.length = 0;
        $("#order").empty();
        $("#sumPoints").empty();
        $("#sumAmount").empty();
        $("#selectArticleModel").modal({backdrop: "static"});
        setTimeout(function () {
            gridObj.reloadSelect();
        },500);
        $.ajax({
            type: "post",
            url: baseUrl + "/income/assignView",
            data: {incomeId: id},
            dataType: "json",
            success: function (data) {
                for (var attr in data.data.entity) {
                    $("#viewAssignForm [name=" + attr + "]").val(data.data.entity[attr]);
                }
            }
        });
    },

    assignConfirm: function () {
        var lock = true ;
        var total = 0;
        for (var i = 0; i < arrayNewList.length; i++) {
            var index = arrayNewList[i];
            var sale = $("#sale_" + index).val();//报价
            var incomed = $("#incomed_" + index).val();//已付
            var income = $("#income_" + index).val();//分款金额
            if (!commonTools.checkNumber(income)) {
                swal("格式不正确！", "只能输入数字。输入的类型为：" + income + "+:+" + typeof (income), "warning");
                return;
            }
            total = parseFloat(total) + parseFloat(income);
        }
        if (parseFloat($("#remainAmount").val()) < Math.round(parseFloat(total) * 100) / 100) {
            swal("金额不足", "可用余额为" + $("#remainAmount").val() + ",分配金额为" + parseFloat(total).toFixed(2) + "，分配金额不能大于可用余额", "warning");
            return;
        }
        $("#ids").val(arrayNewList.toString());
        layer.confirm("请确认分款？", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#selectAssign");
            if(lock){
                lock = false ;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/income/assignArticle",
                    data: $("#selectedForm").serializeJson(),
                    dataType: "json",
                    success: function (data) {
                        Ladda.stopAll();
                        if (data.code == 200) {
                            swal(data.data.message);
                            $("#order").empty();
                            arrayNewList.length = 0;//清空选中的稿件id
                            // document.getElementById("selectArticleForm").reset();
                            $("#selectArticleModel").modal('hide');

                            $("#assign_table_logs").reloadCurrentData(baseUrl + "/income/listPgForAssign", $("#assignForm").serializeJson(), "json", null, null);

                            gridObj.reloadSelect();
                        } else if(data.code == 1002){
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    },
                    error:function () {
                        Ladda.stopAll();//隐藏加载按钮
                    }
                });
            }
        }, function () {
            return;
        })
    }
}


$(document).ready(function () {
    var e = "<i class='fa fa-times-circle'></i> ";
    $("#assignForm").validate({
        rules: {
            tradeAmountQc: {number: true}
        }, message: {
            tradeAmountQc: {required: e + "请输入正确的进款金额"}
        }
    });
    $("#refresh").click(function () {
        businessObj.view($("#refreshId").val());
    });

    businessObj.reimbursementSum();
    $("#refreshArticle").click(function () {
        businessObj.assignIncome($("#refreshArticleId").val());
    });
    $.jgrid.defaults.styleUI = 'Bootstrap';

    $("#invoiceStates").change(function () {
        $("#selectArticleSearch").trigger("click");
    })
    $("#assign_table_logs").jqGrid({
        url: baseUrl + '/income/listPgForAssign',
        datatype: "json",
        mtype: 'POST',
        postData: $("#assignForm").serializeJson(), //发送数据
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
        rowNum: 10,//每页显示记录数
        rowList: [10, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'incomeId', label: 'incomeId', editable: true, hidden: true, width: 60},
            {name: 'code', label: '进账编号', editable: true, width: 100},
            {name: 'accountName', label: '账号名称', editable: true, width: 160},
            {name: 'tradeTime', label: '进账日期', editable: true, width: 100},
            {name: 'tradeMan', label: '进账人', editable: true, width: 140},
            // {name: 'tradeBank', label: '进账银行账号', editable: true, width: 120},
            {name: 'tradeAmount', label: '进账金额', editable: true, width: 120},
            {name: 'receiveTime', label: '领款日期', editable: true, width: 120},
            {name: 'receiveAmount', label: '已领金额', editable: true, width: 100},
            {name: 'assignAmount', label: '已分款金额', editable: true, width: 100},
            {name: 'remainAmount', label: '可用金额', editable: true, width: 100,
                formatter: function (d) {
                    if (d>0) {
                        return "<span style='color:#72C7D9'>"+d+"</span>"
                    }else{
                        return d;
                    }
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 100,
                formatter: function (value, grid, rows, state) {
                    var de = "";
                    if(rows.remainAmount>0){
                        de  = "<a href='javascript:void(0)' style='height:22px;width:40px;'  onclick='businessObj.assignIncome(" + rows.incomeId + ")'>分款</a>";
                        return "     " + de + "        ";
                    }
                    return de;
                }
            }
        ],
        pager: jQuery("#assign_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            var rowData = jQuery("#assign_table_logs").jqGrid("getRowData", rowid);
            businessObj.view(rowData.incomeId);
        },
    });
    resize("#assign_pager_logs");

    $("#selectArticle_table_logs").jqGrid({
        url: baseUrl + '/income/queryArticleForAssign',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#selectArticleForm").serializeJson(), //发送数据
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
        multiselect: true,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15,//每页显示记录数
        rowList: [15, 50, 200, 500, 1000],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },

        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'id', label: 'id', editable: true, hidden: true, width: 80},
            {
                name: 'issued_date', label: '发布日期', editable: true, width: 100,
                formatter: function (d) {
                    if (d == null) {
                        return "";
                    } else {
                        return new Date(d).format("yyyy-MM-dd");
                    }
                }
            },
            {name: 'companyName', label: '客户公司名称', editable: true, width: 180},
            {name: 'custName', label: '对接人', editable: true, width: 80},
            {name: 'brand', label: '品牌', editable: true, width: 80},
            {name: 'media_type_name', label: '媒体板块', editable: true, width: 80},
            {name: 'media_name', label: '媒体名称', editable: true, width: 120},
            {
                name: 'title', label: '标题', editable: true, width: 160,
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
                name: 'link', label: '链接', editable: true, width: 160,
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
            {name: 'num', label: '数量', editable: true, width: 60},
            {name: 'media_user_name', label: '媒介', editable: true, width: 80},
            {name: 'sale_amount', label: '报价/应收', editable: true, width: 80},
            {name: 'income_amount', label: '已分款', editable: true, width: 80}
        ],
        pager: jQuery("#selectArticle_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        gridComplete: function () {
            var rowData = $(this).jqGrid('getRowData');
            //遍历所有的行，如果是选中的，说明它在数组中，让他选中
            for (var i = 0, n = rowData.length; i < n; i++) {
                var item = rowData[i];
                //判断是否存在数据
                if (arrayNewList.length > 0) {
                    if (arrayNewList.indexOf(item.id) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_selectArticle_table_logs_" + item.id).attr("checked", true);
                    }
                }
            }
        },
        loadComplete: function (xhr) {
            $("#jqgh_selectArticle_table_logs_cb").css("padding-right", "6px");
            // 清空统计金额；
            var array = xhr.list;
            if (arrayNewList.length > 0) {
                $.each(array, function (i, item) {
                    if (arrayNewList.indexOf(item.id.toString()) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_selectArticle_table_logs_" + item.id).attr("checked", true);
                    }
                });
            }

        },
        onSelectAll: function (aRowids, status) {
            if (status == true) {
                //循环aRowids数组，将code放入arrayNewList数组中
                $.each(aRowids, function (i, item) {
                    if (!(arrayNewList.indexOf(item) > -1)) {
                        gridObj.saveData(item);
                    }
                })
            } else {
                //循环aRowids数组，将code从arrayNewList中删除
                $.each(aRowids, function (i, item) {
                    gridObj.deleteIndexData(item);
                })
            }

        },
        onSelectRow: function (rowid, status) {
            if (status == true) {
                if (!(arrayNewList.indexOf(rowid) > -1)) {
                    gridObj.saveData(rowid);
                }
            } else {
                gridObj.deleteIndexData(rowid);
            }
        }
    });

    $("#selectedArticle_table_logs").jqGrid({
        url: baseUrl + '/income/listPgForSelectedArticle',
        datatype: "local",
        mtype: 'POST',
        // postData: {incomeId: id}, //发送数据
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
        // multiselectWidth: 25, //设置多选列宽度
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
            {name: 'id', label: 'id', editable: true, hidden: true, width: 60},
            {
                name: 'issuedDate', label: '发布日期', editable: true, width: 80,
                formatter: function (d) {
                    if (d == null) {
                        return "";
                    } else {
                        return new Date(d).format("yyyy-MM-dd");
                    }
                }
            },
            {name: 'companyName', label: '客户公司名称', editable: true, width: 120},
            {name: 'custName', label: '对接人', editable: true, width: 60},
            {name: 'brand', label: '品牌', editable: true, width: 80},
            {name: 'mediaTypeName', label: '媒体板块', editable: true, width: 80},
            {name: 'mediaName', label: '媒体名称', editable: true, width: 80},
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
            {name: 'mediaUserName', label: '媒介', editable: true, width: 60},
            {name: 'saleAmount', label: '应收/报价', editable: true, width: 60},
            {name: 'assignAmount', label: '分款金额', editable: true, width: 60},
            {name: 'assignDate', label: '分款日期', editable: true, width: 120}
        ],
        pager: jQuery("#selectedArticle_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

});
var gridObj = {
    reloadAssign: function () {
        $("#assign_table_logs").emptyGridParam();
        $("#assign_table_logs").jqGrid('setGridParam', {
            postData: $("#assignForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        businessObj.reimbursementSum();
    },

    reloadSelect: function () {
        $("#selectArticle_table_logs").emptyGridParam();
        $("#selectArticle_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: $("#selectArticleForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#selectArticle_table_logs");
    },

    reloadSelected: function (id) {
        $("#selectedArticle_table_logs").emptyGridParam();
        $("#selectedArticle_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {incomeId: id}, //发送数据
        }).trigger("reloadGrid"); //重新载入
        resize("#selectedArticle_table_logs");
    },

    saveData: function (obj) {
        arrayNewList.push(obj);
        var rowData = jQuery("#selectArticle_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
        var income = parseFloat(rowData.sale_amount).toFixed(2) - parseFloat(rowData.income_amount).toFixed(2);
        var html = '<tr id="row' + rowData.id + '"><td  class="hide">' + rowData.id + '</td>' +
            '<td>' + rowData.issued_date + '</td><td>' + rowData.companyName + '</td><td>' + rowData.custName + '</td>' +
            '<td>' + rowData.brand + '</td>' +
            '<td>' + rowData.media_type_name + '</td>' +
            '<td>' + rowData.media_name + '</td>' +
            '<td style="overflow: hidden;white-space: nowrap;text-overflow: ellipsis;" title="' + rowData.title + '">' + rowData.title + '</td>' +
            '<td style="overflow: hidden;white-space: nowrap;text-overflow: ellipsis;" title="' + rowData.link + '">' + rowData.link + '</td>' +
            '<td>' + rowData.num + '</td><td>' + rowData.media_user_name + '</td>' +
            '<td><input type="hidden" name="sale_' + rowData.id + '" id="sale_' + rowData.id + '" value="' + rowData.sale_amount + '">' + rowData.sale_amount + '</td>' +
            '<td><input type="hidden" name="incomed_id" id="incomed_' + rowData.id + '" value="' + rowData.income_amount + '">' + rowData.income_amount + '</td>' +
            '<td><input type="text" style="width: 100%" oninput="commonTools.sumPoints()" onkeyup="this.value=this.value.toString().match(/^\\d+(?:\\.\\d{0,2})?/)" name="income_' + rowData.id + '" id="income_' + rowData.id + '" value="' + parseFloat(income).toFixed(2) + '"></td></tr>';
        $("#order").append(html);
        //计算选中项的报价合计
        gridObj.dealData();
        commonTools.sumPoints();
    },
     dealData: function () {
         var sum = 0;
         $("#order>tr").each(function (i, d) {
             var amount = $(d).find("td:nth-child(12)>input").val();
             sum += parseFloat(amount);
         });
         $("#sumAmount").text(sum.toFixed(2));
     },

    deleteIndexData: function (obj) {
        //获取obj在arrayNewList数组中的索引值
        for (i = 0; i < arrayNewList.length; i++) {
            $("#row" + obj).remove()
            if (arrayNewList[i] == obj) {
                //根据索引值删除arrayNewList中的数据
                arrayNewList.splice(i, 1);
                // i--;
            }
        }
        gridObj.dealData();
        commonTools.sumPoints();
    }
};

var commonTools = {
    checkNumber: function (t) {
        var reg = /^[0-9](\d+)?(\.\d{1,4})?$/;
        if (reg.test(t)) {
            return true;
        }
        return false;
    },
    sumPoints: function () {
        var sum = 0;
        $("#order>tr").each(function (i, d) {
            var amount = $(d).find("td:nth-child(14)>input").val();
            sum += parseFloat(amount);
        });
        $("#sumPoints").text(sum.toFixed(2));
    }
};