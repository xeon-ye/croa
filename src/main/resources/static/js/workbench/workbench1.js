$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    init();
    $("#searchButton").click(function () {
        $("#table_Announcement_list").emptyGridParam();
        $("#table_Announcement_list").jqGrid('setGridParam', {
            postData: $("#searchForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    })
});

function init() {
    $("#table_Announcement_list").jqGrid({
        url: '/Mediapass/notificationlist',
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
        //multiselect: true,
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
            {name: 'id', label: 'id', hidden: true, width: 60, sortable: false},
            {
                name: 'releaseUser', label: '公布人', editable: true, width: 20,
                formatter: function (a, b, rowdata) {
                    var url = "javascript:page('/announcementinform/Mediapass','" + "通知公告" + "')";
                  //  url = url.replace("${id}", rowdata.id).replace("${releaseUser}", rowdata.releaseUser).replace("${title}", "通知公告");
                    var a = "<a href=" + url + " ' target='_blank'>" + rowdata.releaseUser + "</a>";

                    return a;

                }
            },
            {name: 'title', label: '标题', editable: true, width: 20,
                formatter: function (a, b, rowdata) {
                var url = "javascript:page('/announcementinform/Mediapass?id=${id}&title=${title}','" + "通知公告" + "')";
                url = url.replace("${id}", rowdata.id).replace("${releaseUser}", rowdata.title).replace("${title}", "通知公告");
                var a = "<a href=" + url + " ' target='_blank'>" + rowdata.title + "</a>";

                return a;

            }
            },
            {
                name: 'releaseTime', label: '发布时间', width: 20, formatter: function (d) {
                    if (!d) {
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd");
                }
            },

        ],
        pager: jQuery("#pager_Announcement_list"),
        viewrecords: true,
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

var ItemsGridObject1 = {
    url: baseUrl + '/items/list',
    postData: {},
    datatype: "json",
    mtype: 'get',
    // data: mydata,
    height: "auto",
    page: 1,//第一页
    autowidth: true,
    rownumbers: false,
    gridview: true,
    viewrecords: true,
    multiselect: false,
    shrinkToFit: true,
    prmNames: {rows: "size"},
    rowNum: 10,
    rowList: [10, 20, 30],
    colNames: ['id', '工作名称', '工作类型', '开始时间', '发起人'],
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: false
    },
    ondblClickRow: function (row) {

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
            search: false,
            hidden: true
        },
        {
            name: 'itemName',
            index: 'itemName',
            editable: false,
            width: 460,
            align: "left",
            sortable: false,
            sorttype: "string"
        },
        {
            name: 'workType',
            index: 'workType',
            editable: false,
            width: 130,
            align: "center",
            sortable: false,
            formatter: function (a, b, rowdata) {
                var a = "<a ${f}>${text}</a>";
                var f = "";
                if (rowdata.transactionState == 1) {
                    f = rowdata.transactionAddress ? "href=\"javascript:page('" + rowdata.transactionAddress + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                } else {
                    f = rowdata.finishAddress ? "href=\"javascript:page('" + rowdata.finishAddress + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                }
                var text = rowdata.workType || "";
                a = a.replace("${f}", f).replace("${text}", text);
                return a;
            }
        },
        {
            name: 'startTime',
            index: 'startTime',
            editable: false,
            width: 170,
            align: "center",
            sortable: false,
            formatter: function (d) {
                if (d) {
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
                return "";
            }
        },
        {
            name: 'initiatorWorkerName',
            index: 'initiatorWorkerName',
            editable: false,
            width: 110,
            align: "center",
            sortable: false
        }
    ],
    pager: "#",
    viewrecords: true,
    caption: null,
    add: false,
    edit: false,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false
};

ItemsGridObject2 = {};
$.extend(true, ItemsGridObject2, ItemsGridObject1);

var Items = {
    init: function () {
        //创建待办表格对象
        itemsGrid1 = new dataGrid("itemsTable1", ItemsGridObject1, "", "db");
        //加载待办表格
        itemsGrid1.loadGrid();
        //设置待办下面的按钮
        itemsGrid1.setNavGrid();
        //刷新待办任务
        setInterval("itemsGrid1.reflush({transactionState: 1})", 1000 * 60 * 10);

        //创建已办表格对象
        itemsGrid2 = new dataGrid("itemsTable2", ItemsGridObject2, "", "yb");
        //加载已办表格
        itemsGrid2.loadGrid();
        //设置已办下面的按钮
        itemsGrid2.setNavGrid();
        //刷新已办任务
        setInterval("itemsGrid2.reflush({transactionState: 2})", 1000 * 60 * 10);
    },
    //事项更多
    more: function (transactionState) {
        var title = "待办事项"
        if (transactionState == 2) {
            title = "已办事项";
        }
        page("/workbench/items_list?transactionState=" + transactionState, title);
    }
};

var Message = {
    init: function () {
        Message.loadMessage();
        setInterval("Message.loadMessage()", 1000 * 60 * 10);
    },

    /**
     * 查看更多消息
     */
    more: function () {
        page("/workbench/message_list", "我的消息");
    },
    announce: function () {

        page("/announcementinform/Mediapass", "公告通知")
    },

    loadMessage: function () {
        $.ajax({
            url: baseUrl + "/message/list",
            type: "post",
            dataType: "json",
            data: {state: 1},
            success: function (resData) {
                if (resData.list) {
                    $("#message_ul").empty();
                    for (var i = 0; i < resData.list.length; i++) {
                        var id = resData.list[i].id;
                        var image = resData.list[i].image || "/img/mrtx_2.png";
                        image = image.replace("\\images\\", "/images/");
                        var content = resData.list[i].content || "";
                        var createTime = resData.list[i].createTime ? new Date(resData.list[i].createTime).format("yyyy-MM-dd") : "";
                        var state = resData.list[i].state == 1 ?
                            '&nbsp;<b style="color: red;">未读</b>'
                            : '&nbsp;<b style="color: green;">已读</b>';
                        var contentColor = resData.list[i].state == 1 ? "black" : "rgb(104, 107, 109)";

                        var message = '<div style="border-bottom:solid rgb(221,221,221) 1px;cursor:pointer;color:${contentColor};" onclick="Message.readMessage(this, ${id},\'${content}\',\'${stateo}\')">' +
                            '<li style="height:44px;margin:2px 0 0 2px;">' +
                            '<img alt="image" onerror="this.src=\'/img/mrtx_2.png\'" class="img-circle col-sm-3" src="${image}" style="vertical-align: top;width:40px;height:40px;padding:0;margin:0">' +
                            '<div class="col-sm-9" style="display: inline-block;height:40px;margin:4px 0 0 2px;padding:0;overflow:hidden; white-space:nowrap; text-overflow:ellipsis" title="${content}">' +
                            '<span>${content}</span><br>' +
                            '<span>发布于：${createTime}' +
                            '${state}' +
                            '</span></div>' +
                            '</li>' +
                            '</div>';

                        message = message.replace("${id}", id).replace("${image}", image).replace(/\${content}/g, content).replace("${content}", content).replace("${createTime}", createTime).replace(/\${state}/g, state).replace("${stateo}", resData.list[i].state).replace("${contentColor}", contentColor);

                        $("#message_ul").append(message);
                    }
                }
            }
        });
    },
    readMessage: function (obj, messageId, content, state) {
        var oldMessageId = $("#messageId").val();
        if (oldMessageId && oldMessageId == messageId) {
            Message.loadMessage();
            return;
        } else {
            $("#messageId").val(messageId);
        }
        //已读
        if (state != 1) {
            layer.msg(content, {time: 2000});
            return;
        }
        $.ajax({
                type: "post",
                url: "/message/readMessage",
                data: {id: messageId},
                dataType: "json",
                success: function (resData) {
                    if (resData.code == 200) {
                        layer.msg(content, {time: 2000})
                        // layer.alert(content);
                        Message.loadMessage();
                    }
                }
            }
        );
    }
};
//通讯录
var Reporter = {
    init: function () {
        Reporter.loadUsers();
    },
    loadUsers: function () {
        $.ajax({
            url: baseUrl + "/user/queryUserInfo",
            type: "post",
            dataType: "json",
            data: {page: 1, size: 8},
            success: function (resData) {
                if (resData.list) {
                    $("#users_ul").empty();
                    for (var i = 0; i < resData.list.length; i++) {
                        var image = resData.list[i].image || "/img/mrtx_2.png";
                        image = image.replace("\\images\\", "/images/");
                        var sexIcon = resData.list[i].sex == 0 ? "fa-venus" : "fa-mars";
                        var sexColor = resData.list[i].sex == 0 ? "rgb(237, 86, 102)" : "rgb(29, 132, 198)";
                        var name = resData.list[i].name || "";
                        var roleName = resData.list[i].roleName || "";
                        var phone = resData.list[i].phone || "";
                        var email = resData.list[i].email || "";

                        var userElement = '<div style="border-bottom:solid rgb(221,221,221) 2px;overflow:hidden; white-space:nowrap; text-overflow:ellipsis">' +
                            '<li style="height:36px;margin:6px 0 3px 5px;">' +
                            '<div id="col1" class="col-sm-3"><img alt="image" onerror="this.src=\'/img/mrtx_2.png\'" class="img-circle" src="${image}"' +
                            'style="width:32px;height:32px;vertical-align: middle"/>' +
                            '<span style="font-weight: bold;margin: 0 0 0 10px;">${name}</span>' +
                            '<span class="icon fa ${sexIcon}"' +
                            'style="color:${sexColor};margin:0 0 0 6px;"></span></div>' +
                            '<div id="col2" class="col-sm-3" style="overflow:hidden; white-space:nowrap; text-overflow:ellipsis" title="${roleName}"><span style="margin:2px 0 0 0px;">${roleName}</span></div>' +
                            '<div id="col3" class="col-sm-2" style="padding:0;margin:8px 0 0 -20px;"><span class="icon fa fa-phone"' +
                            'style="font-size:14px;color:rgb(221, 221, 221);margin:0 0 0 30px;"></span>' +
                            '<span style="margin: 0 0 0 0">${phone}</span></div>' +
                            '<div id=col4 class="col-sm-4" style="padding:0;margin:8px 0 0 0;"><span class="icon fa fa-envelope"' +
                            'style="font-size:14px;color:rgb(221, 221, 221);margin:0 0 0 30px;"></span>' +
                            '<span style="margin: 0 0 0 0">${email}</span></div>' +
                            '</li>' +
                            '</div>';

                        userElement = userElement.replace("${image}", image).replace("${name}", name).replace("${roleName}", roleName).replace("${roleName}", roleName).replace("${phone}", phone).replace("${email}", email).replace("${sexIcon}", sexIcon).replace("${sexColor}", sexColor);

                        $("#users_ul").append(userElement);
                    }
                }
            }
        });
    },
    more: function () {
        page("/workbench/users_list", "联系人");
    }
};