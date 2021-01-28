var arrayNewList = new Array();
var saleSum = 0;
var outgoSum = 0;

//保存考核计划
function submitHander(t, state) {
    var proId = document.getElementById('proId').value;
    if(proId && proId.length>0){
        url = '/proportion/updateProportion';
    }else{
        url = '/proportion/saveProportion';
    }
    if ($("#editForm").valid() && checkPlate() && checkProName()) {
        var tips;
        if (state == 0) {
            $("#state").val(state);
            tips = "确认保存？";
        }
        layer.confirm(tips, {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);//锁定按钮，防止重复提交
            var formData = new FormData($("#editForm")[0]);
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
                        var schemeList = "/performance/performanceProgramList";
                        refrechPage(schemeList);
                        closeCurrentTab();
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

//判断计划是否有方案和考核时间必填
function checkPlate() {
    var beginTime=$("input[name='proBegin']").val();
    var endTime=$("input[name='proEnd']").val();
    if(beginTime=="" || endTime==""){
        layer.open({
            title:"提示",
            content:"请填写考核日期"
        });
        return false;
    }else {
        var schId = $("input[name='schId']").val();
        if(schId!=undefined && schId!="" && schId!=null){
            return true;
        }else {
            layer.open({
                title:"提示",
                content:"请添加考核方案"
            });
            return false;
        }
    }
}
//判断计划名称是否重复
function checkProName() {
    var flag=false;
    var proName=$("#editForm input[name='proName']").val();
    $.ajax({
        type: "post",
        url: baseUrl + "/proportion/findProportionByCondition",
        data:{proId:null,proName:proName},
        dataType: "json",
        async:false,
        success: function (data) {
            if(data.data.entity==null){
                flag=true;
            }else {
                layer.open({
                    title: "提示",
                    content:"该计划名称已存在"
                });
                flag=false;
            }
        }
    });
    return flag;
}

//获取考核方案
function getPlate() {
    $("#editModal").modal('toggle');
    //初始化表格
    $("#entryTable").jqGrid({
        //方案表格
        url: baseUrl + "/performanceScheme/listPg",
        datatype: "json",
        mtype: 'GET',
        postData: $("#editForm1").serializeJson(),
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,
        rownumbers: false,
        autowidth: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 50,
        sortable: "true",
        sortname: "schId",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        // 显示序号；
        rownumbers: true,
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "schId"
        },
        colModel: [
            {name: 'schId', label: '考核方案id', hidden: true},
            {name: 'schUserId', label: '排除人员id', hidden: true},
            {name: 'schUserName', label: '排除人员姓名', hidden: true},
            {name: 'postId', label: '部门id', hidden: true},
            {name: 'schemeType', label: '方案类型', hidden: true},
            {name: 'groupIds', label: '考核对象id', hidden: true},
            {name: 'groupNames', label: '考核对象名称', hidden: true},
            {name: 'schCode', label: '方案编号', width: 120,
                formatter: function (value, grid, rowData) {
                    if (rowData.schemeType == 1) {
                        return "【KPI】"+value;
                    } else{
                        return "【OKR】"+value;
                    }
                }
            },
            {name: 'schName', label: '方案名称', width: 120},
            {name: 'schSuffice', label: '合格等级', width: 60},
            {name: 'postName', label: '适用职位', width: 60},
            {name: 'createName', label: '创建人', width: 60}
        ],
        pager: jQuery("#entryTableNav"),
        viewrecords: true,
        caption: "方案列表",
        add: false,
        edit: false,
        hidegrid: false,
        loadComplete: function (a, b, c) {
            $("#entryTable").find("tr").each(function () {
                $(this).children().first().css("width", "50");
            });
        },
        gridComplete: function () {
            var rowData = $(this).jqGrid('getRowData');
            //遍历所有的行，如果是选中的，说明它在数组中，让他选中
            for (var i = 0, n = rowData.length; i < n; i++) {
                var item = rowData[i];
                //判断是否存在数据
                if (arrayNewList.length > 0) {
                    if (arrayNewList.indexOf(item.id) > -1) {
                        //判断arrayNewList中存在item.code值时，选中前面的复选框，
                        $("#jqg_entryTable_" + item.id).attr("checked", true);
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
                        $("#jqg_entryTable_" + item.id).attr("checked", true);
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
                })
            } else {
                //循环aRowids数组，将code从arrayNewList中删除
                $.each(aRowids, function (i, item) {
                    deleteIndexData(item);
                })
            }
            $("#saleSum2").text(saleSum.toFixed(2));
            $("#outgoSum2").text(outgoSum.toFixed(2));
            var percent2 = 0;
            if (saleSum > 0) {
                percent2 = (saleSum - outgoSum) * 100 / saleSum;
            }
            $("#percent2").text(percent2.toFixed(2) + "%");
        },
        onSelectRow: function (rowid, status) {
            if (status == true) {
                if (!(arrayNewList.indexOf(rowid) > -1)) {
                    saveData(rowid);
                }
            } else {
                deleteIndexData(rowid);
            }
            $("#saleSum2").text(saleSum.toFixed(2));
            $("#outgoSum2").text(outgoSum.toFixed(2));
            var percent2 = 0;
            if (saleSum > 0) {
                percent2 = (saleSum - outgoSum) * 100 / saleSum;
            }
            $("#percent2").text(percent2.toFixed(2) + "%");
        }
    });
    // $("#entryTable").jqGrid('setLabel', 'rn', '序号', {
    //     'text-align': 'center',
    //     'vertical-align': 'middle',
    //     "width": "50"
    // });
    //将所有表单元素添加到数组中
    //全选的时候，
    $("#selectArticle").off("click").on("click", function () {
        if (arrayNewList.length == 0) {
            swal("请先选择考核方案！");
        } else {
            $("#articleIdsSec").val(arrayNewList.toString());
            saveStepOne();
        }
    });

    function saveData(obj) {
        arrayNewList.push(obj);
        var rowData = jQuery("#select_article_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
        saleSum += parseFloat(rowData.saleAmount);
        outgoSum += parseFloat(rowData.outgoAmount);
    }

    function deleteIndexData(obj) {
        //获取obj在arrayNewList数组中的索引值
        for (i = 0; i < arrayNewList.length; i++) {
            $("#row" + obj).remove()
            if (arrayNewList[i] == obj) {
                //根据索引值删除arrayNewList中的数据
                var rowData = jQuery("#select_article_table_logs").jqGrid("getRowData", obj);   //获取选中行信息
                saleSum = saleSum - parseFloat(rowData.saleAmount);
                outgoSum = outgoSum - parseFloat(rowData.outgoAmount);
                arrayNewList.splice(i, 1);
            }
        }
    }
}

//下一步
function saveStepOne() {
    layer.confirm('已选定考核方案？确定后不能更改！', {
        btn: ['确定', '取消'], //按钮
        shade: false //不显示遮罩
    }, function (index) {
        layer.close(index);
        $("#schemeDetail").html("");
        var $entryTable = $("#entryTable");
        var ids = $entryTable.jqGrid("getGridParam", "selarrrow");
        var content="";
        ids.forEach(function (id) {
            var rowData = $entryTable.jqGrid('getRowData', id);
            var schemeType = +rowData.schemeType;
            var schName = rowData.schName;
            var schSuffice = rowData.schSuffice;
            if(schemeType==1){
                //kpl绩效考核
                var excludePeople = rowData.schUserName.split(",");
                $.ajax({
                    type: "get",
                    url: baseUrl + "/user/queryUserByCondition",
                    data: {postId: rowData.postId},
                    dataType: "json",
                    async: false,
                    success: function (result) {
                        var people = result.filter(function (item) {
                            return !excludePeople.contains(item.name);
                        }).map(function (item) {
                            return item.name;
                        }).concat(",").toString();

                        var peopleId = result.filter(function (item) {
                            return !excludePeople.contains(item.name);
                        }).map(function (item) {
                            return item.id;
                        }).concat(",").toString();

                        people = people.slice(0, people.lastIndexOf(','));
                        peopleId = peopleId.slice(0, peopleId.lastIndexOf(','));
                        content += "<tr>" +
                            "<td>" + schName + "</td>" +
                            "<td>" + people +
                            "<input type='hidden' name='postId' value='" + rowData.postId + "'/>" +
                            "<input type='hidden' name='postName' value='" + rowData.postName + "'/>" +
                            "<input type='hidden' name='schUserName' value='" + people + "'/>" +
                            "<input type='hidden' name='schUserId' value='" + peopleId + "'/>" +
                            "<input type='hidden' name='schId' value='" + id + "'/>" +
                            "<input type='hidden' name='schName' value='" + schName + "'/>" +
                            "</td>" +
                            "<td>" + schSuffice + "</td>" +
                            "</tr>";
                    }
                });
            }else {
               //okr绩效考核
                content+="<tr>" +
                    "<td>"+schName+"</td>" +
                    "<td>"+rowData.groupNames +
                    "<input type='hidden' name='postId' value='"+rowData.postId+"'/>" +
                    "<input type='hidden' name='postName' value='"+rowData.postName+"'/>" +
                    "<input type='hidden' name='schUserName' value='"+rowData.groupNames+"'/>" +
                    "<input type='hidden' name='schUserId' value='"+rowData.groupIds+"'/>" +
                    "<input type='hidden' name='schId' value='"+id+"'/>" +
                    "<input type='hidden' name='schName' value='"+schName+"'/>" +
                    "</td>" +
                    "<td>"+schSuffice+"</td>" +
                    "</tr>";
            }
        });
        $("#schemeDetail").html(content);
        $('#editModal').modal('hide');
    }, function () {
        return;
    });
};



$(function () {
    layui.use("form",function () {
        layui.form.on("select(type)",function (data) {
            $("#editForm1").emptyGridParam();
            $("#entryTable").jqGrid('setGridParam', {
                postData: $("#editForm1").serializeJson(), //发送数据
            }).trigger("reloadGrid"); //重新载入
        });
        layui.form.render();
    });
    $("#schemeSearch").click(function () {
        $("#editForm1").emptyGridParam();
        $("#entryTable").jqGrid('setGridParam', {
            postData: $("#editForm1").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    });
    var proId = getQueryString("id");
    $("#proId").val(proId);
    if(proId!=null){
        $.ajax({
            type: "get",
            dataType: "json",
            url: baseUrl + "/proportion/getProportionById?proId=" + proId,
            success: function (data) {
                var jsData = data.data.entity;
                //初始化表单数据
                Object.keys(jsData).forEach(function (key) {
                    $("#editForm input[name='" + key + "'][type!='radio']").val(jsData[key])
                    if(key=="proBegin"){
                        $("#editForm input[name='" + key + "']").val(new Date(jsData[key]).format("yyyy-MM-dd"));
                    }
                    if(key=="proEnd"){
                        $("#editForm input[name='" + key + "']").val(new Date(jsData[key]).format("yyyy-MM-dd"));
                    }
                });
                setRadioState($("#editForm input[name='proUsed']"),jsData.proUsed);
                setRadioState($("#editForm input[name='proNotice']"),jsData.proNotice);
                setRadioState($("#editForm input[name='proMessage']"),jsData.proMessage);
                $("#proDesc").val(jsData.proDesc);
                $("#proType").val(jsData["proType"]);
                $("#schemeDetail").empty();
                //初始化考核方案（遍历数组）
                if(jsData.programList.length>0){
                    var html = "";
                    jsData.programList.forEach(function(item){
                        html+="<tr>" +
                            "<td>"+item.schName+"</td>" +
                            "<td>"+item.schUserName +
                            "<input type='hidden' name='postId' value='"+item.postId+"'/>" +
                            "<input type='hidden' name='postName' value='"+item.postName+"'/>" +
                            "<input type='hidden' name='schUserName' value='"+item.schUserName+"'/>" +
                            "<input type='hidden' name='schUserId' value='"+item.schUserId+"'/>" +
                            "<input type='hidden' name='schId' value='"+item.schId+"'/>" +
                            "<input type='hidden' name='schName' value='"+item.schName+"'/>" +
                            "</td>" +
                            "</tr>";
                    })
                        $("#schemeDetail").append(html);
                }
                reloadICheck()
            }
        });
    }
});

// 设置单选框；
function setRadioState(obj, inputValue) {
    obj.each(function () {
        $(this).removeAttr("checked");
        // 移除ICheck样式；
        $(this).parent().removeClass("checked");
    });
    // 空值的处理；
    inputValue = inputValue == null ? 0 : inputValue;
    obj.each(function () {
        if ($(this).val() == inputValue) {
            $(this).prop("checked", true);
            // 移除ICheck样式；
            $(this).parent().addClass("checked");
        }
    });
}

function reloadICheck() {
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green'
    });
}

$(function () {
    $(".cancelPlate").on("click", function () {
        closeCurrentTab();
    })
})











