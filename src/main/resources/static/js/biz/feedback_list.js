var chooseArtIdList = [];//选中的稿件记录
var chooseArtMap = {};//选中的稿件记录
$(document).ready(function () {
    // 设置表格默认的UI样式；
    $.jgrid.defaults.styleUI = 'Bootstrap';
    // 窗口拖拽绑定事件；
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#feedBackTable').setGridWidth(width);
    });
    // 回车执行查询；
    $("body").keydown(function (evt) {
        evt = (evt) ? evt : ((window.event) ? window.event : "");
        var curKey = evt.keyCode ? evt.keyCode : evt.which;
        // keyCode=13是回车键；
        if (curKey == 13) {
            $("#dataSearch").click();
        }
    });

    // 查询按钮；
    $("#dataSearch").click(function () {
        if ($("#queryForm").valid()) {
            $("#feedBackTable").emptyGridParam();
            initData();
            reloadFeedBackData();
        }
    });

    initData();
    reloadFeedBackData();

    // 初始化客户公司信息下拉框；
    layui.use(["form"], function () {
        $.get("/cust/listUserCust", function (data) {
            var length = data.length;
            if (length > 0) {
                $("#companyId").append("<option value=''>请选择</option>")
                for (var i = 0; i < length; i++) {
                    $("#companyId").append("<option value='" + data[i].id + "'>" + data[i].company_name + "</option>");
                }
                // 设置客户名称；
                $("input[name='customerName']").val(data[0].company_name);
                // 刷新对接人信息；
                reloadDockData(data[0].id);
            }
            // 初始化；
            layui.form.render();

            // 下拉框的onchange事件；
            layui.form.on("select(companyId)", function (companyData) {
                // 更新客户公司名称；
                $("input[name='custId']").val($("#companyId").find("option:selected").text());
                // 刷新对接人；
                reloadDockData(companyData.value);
            });
        }, "json");
    });

    chooseArtIdList = [];//选中的稿件记录
    chooseArtMap = {};//选中的稿件记录
});

function initData(){
    // 初始化数据；
    $("#feedBackTable").jqGrid({
        url: baseUrl + "/article/listArtFeedback",
        datatype: "local",
        mtype: 'POST',
        postData: $("#queryForm").serializeJson(),
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,
        gridview: true,
        cellsubmit: "clientArray",
        viewrecords: true,
        multiselect: true,
        multiselectWidth: 30,
        sortable: "true",
        sortname: "artId",
        sortorder: "desc",
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,
        rowList: [10, 25, 50],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages", records: "total", repeatitems: false, id: "artId"
        },
        colModel: [
            {name: 'artId', label: 'artId', hidden: true, width: 60},
            {name: 'orderId', label: 'orderId', hidden: true, width: 60},
            {name: 'companyName', label: '客户公司名称', width: 120},
            {name: 'custName', label: '对接人名称', width: 120},
            {name: 'mediaName', label: '媒体', width: 120},
            {name: 'title', label: '稿件标题', width: 120},
            {name: 'brand', label: '稿件品牌', width: 80},
            {name: 'typeName', label: '稿件行业类型', width: 80},
            {name: 'link', label: '稿件链接', width: 120},
            {
                name: 'saleAmount',
                label: '业绩金额（含税）',
                width: 120,
                formatoptions: {thousandsSeparator: ",", decimalSeparator: ".", prefix: "￥"}
            },
            {
                name: 'issuedDate', label: '发布时间', width: 120, formatter: function (value, grid, rowData) {
                    return value == null ? "" : new Date(value).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {name: 'userName', label: '负责人（业务员）', width: 120}
        ],
        pager: jQuery("#feedBackNav"),
        viewrecords: true,
        caption: "反馈信息列表",
        add: false,
        edit: false,
        hidegrid: false,
        gridComplete: function () {
            // 单选框居中；
            $(".cbox").addClass("icheckbox_square-green");

            //默认选择或反选指定行。如果onselectrow为ture则会触发事件onSelectRow，onselectrow默认为ture
            if(chooseArtIdList && chooseArtIdList.length > 0){
                $.each(chooseArtIdList, function (i, artId) {
                    $("#feedBackTable").setSelection(artId, true);
                });
            }
        },
        onSelectRow: function (rowid, status){
            if(status){
                if(!chooseArtIdList.contains(rowid)){
                    chooseArtIdList.push(rowid);
                    chooseArtMap[rowid] = $("#feedBackTable").jqGrid('getRowData', rowid);
                }
            }else {
                chooseArtIdList.remove(rowid);
                delete chooseArtMap[rowid];
            }
        },
        onSelectAll:function(rowids,status){
            $.each(rowids, function (i, rowid) {
                if(status){
                    if(!chooseArtIdList.contains(rowid)){
                        chooseArtIdList.push(rowid);
                        chooseArtMap[rowid] = $("#feedBackTable").jqGrid('getRowData', rowid);
                    }
                }else {
                    chooseArtIdList.remove(rowid);
                    delete chooseArtMap[rowid];
                }
            });
        }
});
    $("#feedBackTable").setGridHeight(450);
}

// 重新载入数据；
function reloadFeedBackData() {
    $("#feedBackTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(),
        datatype: "json"
    }).trigger("reloadGrid");
}

// 数据导出；
function exportData() {
    startModal("#exportButton");
    if(chooseArtIdList && chooseArtIdList.length > 0){
        var list = [];
        $.each(chooseArtIdList, function (i, artId) {
            list.push(chooseArtMap[artId]);
        });
        handlerCSV(list);
        Ladda.stopAll();
    }else {
        var param = $("#queryForm").serializeJson();
        if(!param["companyId"]){
            layer.msg("必须选择客户公司才能进行导出！", {time: 3000, icon: 5});
            Ladda.stopAll();//隐藏加载按钮
            return;
        }
        $.post(baseUrl + "/article/exportData", param, function (data) {
            Ladda.stopAll();
            if(data.code == 200){
                handlerCSV(data.data.list);
            }else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        }, "json").error(function () {
            Ladda.stopAll();//隐藏加载按钮
        });
    }
}

//清空选中
function clearChooseData() {
    if(chooseArtIdList && chooseArtIdList.length > 0){
        $.each(chooseArtIdList, function (i, artId) {
            $("#feedBackTable").setSelection(artId, false);
        });
    }
    chooseArtIdList = [];//选中的稿件记录
    chooseArtMap = {};//选中的稿件记录
}

// 获取对接人信息；
function reloadDockData(companyId) {
    // 获取对接人信息；
    $.get("/dockingPeople/listByCustId/" + companyId, function (sonData) {
        // 先清空；
        $("#dockingId").empty();
        $("#dockingId").append("<option value=''> 请选择</option>");
        for (var i = 0; i < sonData.length; i++) {
            $("#dockingId").append("<option value='" + sonData[i].id + "'>" + sonData[i].custName + "</option>");
        }
        layui.form.render();
    }, "json");
}

//导出处理
function handlerCSV(list) {
    if(!list || list.length < 1){
        layer.msg("没有找到相关数据，请检查！", {time: 3000, icon: 5});
    }
    var filtContent = ",,,,反馈列表,\n";
    filtContent += "客户公司名称,对接人名称,媒体,稿件标题,稿件品牌,稿件行业类型,稿件链接,业绩金额(含税),发布时间,负责人(业务员)\n";
    $.each(list, function (i, item) {
        filtContent += item.companyName + "," + item.custName + "," + item.mediaName + "," + item.title + "," + item.brand +
            "," + item.typeName + "," + item.link + "," + item.saleAmount + "," + (item.issuedDate ? new Date(item.issuedDate).format("yyyy.MM.dd hh:mm:ss") : "") + "," + item.userName +"\n";
    });
    var href = "data:text/csv;charset=utf-8,\ufeff" + filtContent;
    var aNode = document.createElement("a");
    aNode.setAttribute("href", href);
    var date = new Date().format("yyyy-MM-dd");
    aNode.setAttribute("download", "反馈列表.csv");
    aNode.click();
    aNode.remove();
}