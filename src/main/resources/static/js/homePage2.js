var sysConfigMap = {};//系统配置功能
$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';

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

    pageObj.init();

    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    var startDate = {
        elem: '#startDate',
        format: 'yyyy-MM-dd HH:mm',
        istime: true,
        istoday: true,
        type: 'datetime',
        ready: function (date) {
            this.dateTime.hours = new Date().getHours();
            this.dateTime.minutes = new Date().getMinutes();
            this.dateTime.seconds = new Date().getSeconds();
        }
    };
    var endDate = {
        elem: '#endDate',
        format: 'yyyy-MM-dd HH:mm',
        istime: true,
        istoday: false,
        type: 'datetime',
        ready: function (date) {
            this.dateTime.hours = new Date().getHours();
            this.dateTime.minutes = new Date().getMinutes();
            this.dateTime.seconds = new Date().getSeconds();
        }
    };
    laydate.render(startDate);
    laydate.render(endDate);
});

var pageObj = {
    currentMaxSeq: 0, //初始化设置当前模块数，后续需要维护(只递增)，通过该值来设置模块标题背景色
    showBlocks: ["dbsxHtml", "lhbHtml", "rcHtml", "jyHtml"],
    leftBlocks: ["dbsxHtml", "lhbHtml", "rcHtml", "jyHtml", "zytjHtml", "ggtzHtml"],//, "yxsjHtml", "jtxwHtml", "gsxwHtml"
    allBlocks: ["dbsxHtml", "lhbHtml", "rcHtml", "jyHtml", "zytjHtml", "ggtzHtml"],//, "yxsjHtml", "jtxwHtml", "gsxwHtml"
    filterBlicks:["yxsjHtml", "jtxwHtml", "gsxwHtml"],//需要排除掉的
    allBlockMap: {"dbsxHtml":"待办事项", "lhbHtml":"龙虎榜/服务榜", "rcHtml":"日程", "jyHtml":"建议/论坛", "zytjHtml":"资源推荐",
        "ggtzHtml":"集团信息", "yxsjHtml":"优秀事迹","jtxwHtml":"集团新闻", "gsxwHtml":"公司新闻"},
    backColorMap: {danger: "#ed5666",primary:"#1bb394",success:"#1d84c6",warning:"#f8ac5a"}, //标题颜色
    backColorList:["panel-danger","panel-primary","panel-success","panel-warning"], //标题样式
    addBlock: function () {
        //过滤掉已经设置的
        var residueBlocks = pageObj.allBlocks.filter(function (value) {
            return !pageObj.showBlocks.contains(value);
        });
        if(residueBlocks && residueBlocks.length > 0){
            var html = "<option value=\"\">--请选择模块--</option>";
            for(var i = 0; i < residueBlocks.length; i++){
                html += "<option value=\""+residueBlocks[i]+"\">"+pageObj.allBlockMap[residueBlocks[i]]+"</option>";
            }
            $("#block").html(html);
            var index  = layer.open({
                type: 1,
                zIndex: 9999,
                content: $("#addBlock"),
                btn: ['添加'],
                area: ['400px', '160px'],
                title: "添加模块",
                yes: function (t) {
                    if(!$("#addBlockForm").valid()) return;
                    var tepHtml = $("#block").val();
                    if (pageObj.showBlocks.contains(tepHtml)) {
                        layer.msg("很抱歉，当前模块已存在！", {time: 2000, icon: 5});
                        return;
                    }
                    pageObj.showBlocks.push(tepHtml);
                    pageObj.renderBlockItem(pageObj.currentMaxSeq); //添加模块
                    pageObj.currentMaxSeq = pageObj.currentMaxSeq+1;
                    pageObj.itemInit("block" + pageObj.currentMaxSeq, tepHtml, pageObj.showBlocks.length - 1); //添加内容
                    pageObj.updateUserModel();
                    layer.closeAll();
                }
            });
        }else {
            layer.msg("很抱歉，已经没有可添加的模块！", {time: 2000, icon: 5});
        }

    },
    itemInit: function (blockId, tmpHtml, index) {
        $("#" + blockId).html(template(tmpHtml));
        for(var i = 0; i < pageObj.backColorList.length; i++){
            if($("#" + blockId).hasClass(pageObj.backColorList[i])){
                $("#" + blockId).find(".dropdown-menu").css("backgroundColor",pageObj.backColorMap[pageObj.backColorList[i].split("-")[1]])
            }
        }
        // var blockIndex = parseInt(blockId.substring(blockId.length - 1)) - 1;
        if((index || index == 0) && tmpHtml){
            pageObj.showBlocks[index] = tmpHtml;
        }

        if (tmpHtml === "dbsxHtml") {
            itemTableObj.init();//待办事项初始化
        } else if (tmpHtml === "lhbHtml") {
            rinkingObj.init();//龙虎榜/服务榜，默认龙虎榜
        } else if (tmpHtml === "rcHtml") {
            scheduleObj.init();//日程初始化
        } else if (tmpHtml === "jyHtml") {
            adviceTopic.init();//建议初始化
        } else if (tmpHtml === "zytjHtml") {
            zytjObj.init();//资源推荐
        } else if (tmpHtml === "ggtzHtml") {
            ggtzObj.init();//公告通知
        } else if (tmpHtml === "yxsjHtml") {
            yxsjObj.init();//优秀事迹
        } else if (tmpHtml === "jtxwHtml") {
            jtxwObj.init();//集团新闻
        } else if (tmpHtml === "gsxwHtml") {
            gsxwObj.init();//公司新闻
        }
    },
    renderBlockItem: function (seq) {
        var index = seq % pageObj.backColorList.length;
        var html ="<div class=\"col-sm-6 pm0 height49\">\n" +
            "          <div style='cursor: pointer;' class=\"panel "+pageObj.backColorList[index]+" ibox left-top-panel\" id=\"block"+(seq+1)+"\" draggable=\"true\"\n" +
            "               ondrop=\"pageObj.drop(event);\" ondragover=\"pageObj.allowDrop(event);\" ondragstart=\"pageObj.drag(event);\">\n" +
            "              <div style=\"position: relative;left: 48%;top: 38%;\"\n" +
            "                   onclick=\"pageObj.chooseBlock(this)\">\n" +
            "                  <img style=\"width: 100px\" src=\"/img/xinzeng.png\">\n" +
            "              </div>\n" +
            "          </div>\n" +
            "      </div>";
        $("#contentList").append(html);
    },
    updateUserModel: function () {
        $.ajax({
            url: baseUrl + "/user/home/modal?homeModal=" + pageObj.showBlocks.toString(),
            type: "put"
        });
    },
    init: function () {
        //读取配置，决定是否隐藏显示资源推荐
        if(sysConfigMap && sysConfigMap["zytjDisplayFlag"] && sysConfigMap["zytjDisplayFlag"]["value"] == 1){
            if(!pageObj.leftBlocks.contains("zytjHtml")){
                pageObj.leftBlocks.push("zytjHtml");
            }
            if(!pageObj.allBlocks.contains("zytjHtml")){
                pageObj.allBlocks.push("zytjHtml");
            }
            pageObj.filterBlicks.remove("zytjHtml");
        }else {
            pageObj.leftBlocks.remove("zytjHtml");
            pageObj.allBlocks.remove("zytjHtml");
            if(!pageObj.filterBlicks.contains("zytjHtml")){
                pageObj.filterBlicks.push("zytjHtml");
            }
        }
        $.get(baseUrl + "/user/home/modal", function (result) {
            var homeModal = result.data.result;
            var blocks = pageObj.showBlocks;
            if (homeModal) {
                blocks = homeModal.split(",");
                //由于集团新闻、公司新闻、优秀事迹合并在公告通知中，所以此处默认将其排除
                if(blocks.contains("yxsjHtml")){
                    blocks.remove("yxsjHtml");
                }
                if(blocks.contains("jtxwHtml")){
                    blocks.remove("jtxwHtml");
                }
                if(blocks.contains("gsxwHtml")){
                    blocks.remove("gsxwHtml");
                }
                if(blocks.contains("zytjHtml")){
                    //读取配置，决定是否隐藏显示资源推荐
                    if(!(sysConfigMap && sysConfigMap["zytjDisplayFlag"] && sysConfigMap["zytjDisplayFlag"]["value"] == 1)){
                        blocks.remove("zytjHtml");
                    }
                }
                pageObj.showBlocks = blocks;
                //过滤掉已经设置的
                pageObj.leftBlocks = pageObj.leftBlocks.filter(function (value) {
                    return !blocks.contains(value);
                });
            }

            pageObj.currentMaxSeq = pageObj.showBlocks.length;
            for (var i = 0; i < pageObj.showBlocks.length; i++) {
                if (!blocks[i] || blocks[i] === "empty") {
                    blocks[i] = pageObj.leftBlocks[0];
                    pageObj.leftBlocks.remove(blocks[i]); //替换empty，并移除掉已经设置的，为下一个empty准备
                }
                pageObj.renderBlockItem(i); //添加模块
                pageObj.itemInit("block" + parseInt(i + 1), blocks[i], i); //添加内容
            }
        });
    },
    //选择模块初始化,type:选择类型，add-添加(废弃)、exchange-切换
    showBlock: function (t, type) {
        var dataId = $(t).attr("data-id");
        if (pageObj.showBlocks.contains(dataId)) {
            layer.msg("很抱歉，当前模块已存在！", {time: 2000, icon: 5});
            return;
        }
        var blockElement = $(t).parent().parent().parent().parent();
        if(type === "exchange"){
            blockElement = $(t).parent().parent().parent().parent().parent();
        }
        var currentBlock = $(blockElement).find(".ibox-title").attr("id")+"Html";
        var blockId = blockElement.attr("id");
        var index = null;
        for(var i = 0; i < pageObj.showBlocks.length; i++){
            if(pageObj.showBlocks[i] == currentBlock){
                index = i;
                break;
            }
        }
        pageObj.itemInit(blockId, dataId, index);
        pageObj.updateUserModel();
    },
    //显示模块下拉框
    chooseBlock: function (t) {
        var blockElement = $(t).parent();
        var blockId = blockElement.attr("id");
        //仅展示未选的
        $("#chooseMenu").find("li").each(function (i, li) {
            if(pageObj.showBlocks.contains($(li).attr("data-id"))){
                $(li).css("display", "none");
            }else {
                $(li).css("display", "block");
            }
        });
        $("#" + blockId).html($("#chooseMenu").html());
    },
    closeLink: function (t) {
       /* var content = $(t).closest('div.ibox');
        content.html($("#chooseBlock").html());

        var tmpHtml = $(t).parent().parent().attr("id") + "Html";

        for (var i = 0; i < pageObj.showBlocks.length; i++) {
            var block = pageObj.showBlocks[i];
            if (block === tmpHtml) pageObj.showBlocks[i] = "empty";
        }*/
        var tmpHtml = $(t).parent().parent().attr("id") + "Html";
        for (var ii = 0; ii < pageObj.showBlocks.length; ii++) {
            var block = pageObj.showBlocks[ii];
            if (block === tmpHtml){
                pageObj.showBlocks.splice(ii, 1);
            }
        }
        $(t).closest('div.height49').remove();
        pageObj.updateUserModel();
    },
    collapseLink: function (t) {
        var ibox = $(t).closest('div.ibox');
        var button = $(t).find('i');
        var content = ibox.find('div.ibox-content');
        content.slideToggle(200);
        button.toggleClass('fa-chevron-up').toggleClass('fa-chevron-down');
        ibox.toggleClass('').toggleClass('border-bottom');
        setTimeout(function () {
            ibox.resize();
            ibox.find('[id^=map-]').resize();
        }, 50);
    },
    allowDrop: function (ev) {
        ev.preventDefault();
        ev.stopPropagation();
    },
    drag: function (ev) {
        console.log("clientX = "+ ev.clientX+ ", clientY = " + ev.clientY)
        console.log($(ev.currentTarget).attr("id"));
        ev.dataTransfer.setData("Text",$(ev.currentTarget).attr("id"));
    },
    drop: function (ev) {
        var sourceBlockId = ev.dataTransfer.getData("Text");
        var sourceHtml = $("#"+sourceBlockId).html();
        var sourceBlock = $("#"+sourceBlockId).find(".ibox-title").attr("id")+"Html";
        var targetBlockId = $(ev.currentTarget).attr("id");
        var targetHtml = $("#"+targetBlockId).html();
        var targetBlock = $("#"+targetBlockId).find(".ibox-title").attr("id")+"Html";
        $("#"+sourceBlockId).html(targetHtml);
        $("#"+targetBlockId).html(sourceHtml);

        //更新交换下拉列表背景色
        for(var i = 0; i < pageObj.backColorList.length; i++){
            if($("#" + sourceBlockId).hasClass(pageObj.backColorList[i])){
                $("#" + sourceBlockId).find(".dropdown-menu").css("backgroundColor",pageObj.backColorMap[pageObj.backColorList[i].split("-")[1]])
            }
            if($("#" + targetBlockId).hasClass(pageObj.backColorList[i])){
                $("#" + targetBlockId).find(".dropdown-menu").css("backgroundColor",pageObj.backColorMap[pageObj.backColorList[i].split("-")[1]])
            }
        }
        //替换位置进行入库
        var sourceIndex = 0;
        var targetIndex = 0;
        for(var z = 0; z < pageObj.showBlocks.length; z++){
            if(pageObj.showBlocks[z] == sourceBlock){
                sourceIndex = z;
            }
            if(pageObj.showBlocks[z] == targetBlock){
                targetIndex = z;
            }
        }
        pageObj.showBlocks[sourceIndex] = targetBlock;
        pageObj.showBlocks[targetIndex] = sourceBlock;
        pageObj.updateUserModel();
    },
    dropdownClick: function (t) {
       $(t).parent().find("li").each(function (i, li) {
           var currentLi = $(li).attr("data-id");
           if(pageObj.showBlocks.contains(currentLi) || pageObj.filterBlicks.contains(currentLi)){
               $(li).css("display", "none");
           }else {
               $(li).css("display", "block");
           }
       });
    }
};

//待办事项初始化
var itemTableObj = {
    itemTable: {
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
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    var a = "<a ${f} style='overflow: hidden;white-space: nowrap;text-overflow: ellipsis;width: 240px;display: inline-block;'>${text}</a>";
                    var f = "";
                    //type=4做审核管理跳转区分  ，
                    var url = "&returnType=4";
                    if (rowdata.transactionState === 1) {
                        url = (rowdata.transactionAddress && rowdata.transactionAddress.indexOf("?") > 0) ? "&returnType=4" : "?returnType=4";
                        f = rowdata.transactionAddress ? "href=\"javascript:page('" + rowdata.transactionAddress + url + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                    } else {
                        url = (rowdata.finishAddress && rowdata.finishAddress.indexOf("?") > 0) ? "&returnType=4" : "?returnType=4";
                        f = rowdata.finishAddress ? "href=\"javascript:page('" + rowdata.finishAddress + url + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                    }
                    var text = rowdata.itemName || "";
                    a = a.replace("${f}", f).replace("${text}", text);
                    return a;
                }
            },
            {
                name: 'workType',
                index: 'workType',
                editable: false,
                width: 130,
                align: "center",
                sortable: false,
                /*formatter: function (a, b, rowdata) {
                    var a = "<a ${f}>${text}</a>";
                    var f = "";
                    //type=4做审核管理跳转区分  ，
                    var url = "&returnType=4";
                    if (rowdata.transactionState === 1) {
                        url = (rowdata.transactionAddress && rowdata.transactionAddress.indexOf("?") > 0) ? "&returnType=4" : "?returnType=4";
                        f = rowdata.transactionAddress ? "href=\"javascript:page('" + rowdata.transactionAddress + url + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                    } else {
                        url = (rowdata.finishAddress && rowdata.finishAddress.indexOf("?") > 0) ? "&returnType=4" : "?returnType=4";
                        f = rowdata.finishAddress ? "href=\"javascript:page('" + rowdata.finishAddress + url + "','" + rowdata.workType + "')\"" : "href=\"javascipt:void(0)\"";
                    }
                    var text = rowdata.workType || "";
                    a = a.replace("${f}", f).replace("${text}", text);
                    return a;
                }*/
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
        pager: "",
        viewrecords: true,
        caption: null,
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    init: function () {
        //创建待办表格对象
        itemsGrid1 = new dataGrid("itemsTable1", itemTableObj.itemTable, "", "db");
        //加载待办表格
        itemsGrid1.loadGrid();
        //设置待办下面的按钮
        itemsGrid1.setNavGrid();
        //刷新待办任务
        setInterval("itemsGrid1.reflush({transactionState: 1})", 1000 * 60 * 10);
    },
    //事项更多
    more: function (transactionState) {
        var title = "待办事项"
        if (transactionState === 2) {
            title = "已办事项";
        }
        page("/workbench/items_list?transactionState=" + transactionState, title);
    }
};
//资源推荐
var zytjObj = {
    more: function () {
        page("/news/recommedResList", "资源推荐");
    },
    init: function () {
        $.ajax({
            type: 'post',
            url: baseUrl + "/recommendRes/index",
            dataType: 'json',
            success: function (data) {
                var $resourceContentDiv = $("#resourceContentDiv");
                var html = "<ul>";
                $(data).each(function (i, item) {
                    html += "<li>\n" +
                        "        <span title='" + item.plateName + "' class=\"plate text_space\" style='display: block;float: left;max-width: 22%;'>" + item.plateName + "</span>\n" +
                        "        <span title='" + item.resourceTitle + "' class='text_space' style='display: block;float: left;max-width: 50%;padding-left: 5px;padding-right: 5px;'>" +
                        "           <a href='/recommendRes/view/" + item.id + "/false'>" + item.resourceTitle + "</a></span>\n" +
                        "        <span title='" + item.title + "' class=\"resourceTitle1 text_space\" style='display: block;float: left;max-width: 25%;'>\n" +
                        "            <img src='/img/home/lianjie.png' height='12' width='12' /><a href=\"" + item.href + "\" target='_blank'>" + item.title + "</a>\n" +
                        "        </span>\n" +
                        "    </li>";
                });
                html += "</ul>";
                $resourceContentDiv.html(html);
            }
        });
    }
};
//公告通知
var ggtzObj = {
    init: function () {
        $.ajax({
            type: "post",
            url: baseUrl + "/Mediapass/notificationlist",
            dataType: "json",
            success: function (data) {
                var html = "";
                for (var i = 0; i < data.list.length; i++) {
                    html += " <li class=\"layui-timeline-item timeLineCss\">\n" +
                        "         <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
                        "         <div class=\"layui-timeline-content layui-text timeCss\">\n" +
                        "             <span class=\"layui-timeline-title\">" + new Date(data.list[i].releaseTime).format("yyyy-MM-dd") + "</span>\n" +
                        "             <div class=\"timeContent\">\n" +
                        "                 <div class=\"timeContentTitle\">\n" +
                        "                     <a href='#' onclick=ggtzObj.linkLocation('/announcementinform/Mediapass')>发布人-" + data.list[i].releaseUser + "</a>\n" +
                        "                 </div>\n" +
                        "                 <div class=\"timeContent1\">\n" +
                        "                     <a href='#' onclick=ggtzObj.linkLocation('/announcementinform/Mediapass?id=" + data.list[i].id + "&title=" + data.list[i].title + "')>" + data.list[i].title + "</a>\n" +
                        "                 </div>\n" +
                        "             </div>\n" +
                        "         </div>\n" +
                        "     </li>"
                }
                $("#announcements").html(html);
            }
        })
    },
    linkLocation: function (href) {
        page(href, "通知公告");
    },
    tabChange:function (currentActiveId, currentContentId) {
        //选择标识全部隐藏
        $("#ggtz .tabActive").css("display", "none");
        $("#ggtz").parent().find(".jtxx").css("display", "none");
        $(currentActiveId).css("display", "block");
        $(currentContentId).css("display", "block");
    },
    announceMent:function () {
        ggtzObj.tabChange("#ggtzTabActive", "#annNoticeDiv");
        $.ajax({
            type:"post",
            url:baseUrl +"/Mediapass/notificationlist",
            dataType:"json",
            success:function (data) {
                var html = "";
                for(var i =0;i<data.list.length;i++){
                    html += " <li class=\"layui-timeline-item timeLineCss\">\n" +
                        "         <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
                        "         <div class=\"layui-timeline-content layui-text timeCss\">\n" +
                        "             <span class=\"layui-timeline-title\">"+ new Date(data.list[i].releaseTime).format("yyyy-MM-dd")+"</span>\n" +
                        "             <div class=\"timeContent\">\n" +
                        "                 <div class=\"timeContentTitle\">\n" +
                        "                     <a href='#' onclick=ggtzObj.linkLocation('/announcementinform/Mediapass')>发布人-"+data.list[i].releaseUser+"</a>\n" +
                        "                 </div>\n" +
                        "                 <div class=\"timeContent1\">\n" +
                        "                     <a href='#' onclick=ggtzObj.linkLocation('/announcementinform/Mediapass?id="+data.list[i].id+"')>"+data.list[i].title+"</a>\n" +
                        "                 </div>\n" +
                        "             </div>\n" +
                        "         </div>\n" +
                        "     </li>"
                }
                $("#announcements").html(html);

            }
        })
    },
    documentList:function () {
        ggtzObj.tabChange("#zdTabActive", "#documentHtmlDiv");
        $.ajax({
            type:"post",
            url:baseUrl+"/documentLibrary/selectLibraryList",
            dataType:"json",
            data:{homeFlag:"1"},
            success:function (data) {
                var html = "";
                if(data && data.list && data.list.length  > 0){
                    for(var i =0;i < data.list.length;i++){
                        html += " <li class=\"layui-timeline-item timeLineCss\">\n" +
                            "         <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
                            "         <div class=\"layui-timeline-content layui-text timeCss\">\n" +
                            "             <span class=\"layui-timeline-title\">"+ new Date(data.list[i].releaseTime).format("yyyy-MM-dd")+"</span>\n" +
                            "             <span class=\"layui-timeline-title\">&nbsp;&nbsp;制度发布人- "+ data.list[i].releaseName+"</span>\n" +
                            "             <div class=\"timeContent\">\n" +
                            "                 <div class=\"timeContentTitle\">\n" +
                            "                     <a href=/documentLibrary/library/view/"+data.list[i].id+">"+data.list[i].libraryName+"</a>\n" +
                            "                 </div>\n" +
                            "             </div>\n" +
                            "         </div>\n" +
                            "     </li>"
                    }
                }
                $("#documentHtml").html(html);
            }
        })
    },
    jtxwListLoad:function () {
        ggtzObj.tabChange("#jtxwTabActive", "#jtxwContentDiv");
        $.ajax({
            type: 'get',
            url: baseUrl + "/news/search/1",
            dataType: 'json',
            success: function (data) {
                var html = "<ul>";
                $(data.list).each(function (i, item) {
                    html += "<li class='text_space' style='padding-left: 15px;'><a href=/news/view/" + item.id + ">" + item.title + "</a></li>";
                });
                html += "</ul>";
                $("#jtxwContentDiv").html(html);
            }
        });
    },
    gsxwListLoad:function () {
        ggtzObj.tabChange("#gsxwTabActive", "#gsxwContentDiv");
        $.ajax({
            type: 'get',
            url: baseUrl + "/news/search/2",
            dataType: 'json',
            success: function (data) {
                var html = "<ul>";
                $(data.list).each(function (i, item) {

                    html += "<li class='text_space' style='padding-left: 15px;'><a href=/news/view/" + item.id + ">" + item.title + "</a></li>";

                });
                html += "</ul>";
                $("#gsxwContentDiv").html(html);
            }
        });
    },
    yxsjListLoad:function () {
        ggtzObj.tabChange("#yxsjTabActive", "#yxsjContentDiv");
        $.ajax({
            type: 'get',
            url: baseUrl + "/news/search/4",
            dataType: 'json',
            success: function (data) {
                var html = "<ul>";
                $(data.list).each(function (i, item) {
                    html += "<li class='text_space' style='padding-left: 15px;'><a href=/news/view/" + item.id + ">" + item.title + "</a></li>";
                });
                html += "</ul>";
                $("#yxsjContentDiv").html(html);
            }
        });
    },
};
//优秀事迹
var yxsjObj = {
    init: function () {
        $.ajax({
            type: 'get',
            url: baseUrl + "/news/search/4",
            dataType: 'json',
            success: function (data) {
                var html = "<ul>";
                $(data.list).each(function (i, item) {
                    html += "<li class='text_space' style='padding-left: 15px;'><a href=/news/view/" + item.id + ">" + item.title + "</a></li>";
                });
                html += "</ul>";
                $("#fineThingContentDiv").html(html);
            }
        });
    }
};
//集团新闻
var jtxwObj = {
    init: function () {
        $.ajax({
            type: 'get',
            url: baseUrl + "/news/search/1",
            dataType: 'json',
            success: function (data) {
                var html = "<ul>";
                $(data.list).each(function (i, item) {
                    html += "<li class='text_space' style='padding-left: 15px;'><a href=/news/view/" + item.id + ">" + item.title + "</a></li>";
                });
                html += "</ul>";
                $("#groupNewsContentDiv").html(html);
            }
        });
    }
};
//公司新闻
var gsxwObj = {
    init: function () {
        $.ajax({
            type: 'get',
            url: baseUrl + "/news/search/2",
            dataType: 'json',
            success: function (data) {
                var html = "<ul>";
                $(data.list).each(function (i, item) {

                    html += "<li class='text_space' style='padding-left: 15px;'><a href=/news/view/" + item.id + ">" + item.title + "</a></li>";

                });
                html += "</ul>";
                $("#companyNewsContentDiv").html(html);
            }
        });
    }
};
//龙虎榜、服务榜
var rinkingObj = {
    param: {companyCode: user.dept.companyCode, timeQuantum: 1},//获取本月数据
    deptMonthParam:{companyCode: user.dept.companyCode, timeQuantum: 1, level:3},//部门月排名
    deptWeekParam: {companyCode: user.dept.companyCode, timeQuantum: 1, level: 3},//部门周排名
    groupWeekParam: {companyCode: user.dept.companyCode, timeQuantum: 1, level: 4},//部门组周排名
    isGroup: user.dept.companyCode == "JT" ? true : false, //是否集团用户
    selfRinkingIdMap: {
        1: "one",
        2: "two",
        3: "three",
        4: "four",
        5: "five",
        6: "six",
        7: "senven",
        8: "eight",
        9: "nine",
        10: "ten"
    },
    weekRinkingSeqMap:{0: "selfRankingOne", 1: "selfRankingTwo", 2: "selfRankingThree"},
    init: function () {
        rinkingObj.businessRinking(); //龙虎榜
        // rinkingObj.mediaUserRinking(); //服务榜
    },
    lhbTabClick: function () {
        $("#businessRankingTabActive").css("display", "block");
        $("#businessRankingContent").css("display", "block");
        $("#mediaUserTabActive").css("display", "none");
        $("#mediaUserRankingContent").css("display", "none");
        rinkingObj.businessRinking(); //龙虎榜
    },
    fwbTabClick: function () {
        $("#mediaUserTabActive").css("display", "block");
        $("#mediaUserRankingContent").css("display", "block");
        $("#businessRankingTabActive").css("display", "none");
        $("#businessRankingContent").css("display", "none");
        rinkingObj.mediaUserRinking(); //服务榜
    },
    businessRinking: function () {
        $(".rinkingMonthText").text(new Date().getMonth() + 1);//设置排名标题
        $("#weekRinkingTime").html(((new Date().getMonth() + 1) < 10 ? "0" + (new Date().getMonth() + 1) : (new Date().getMonth() + 1)) + "月" + (new Date().getDate() < 10 ? "0" + new Date().getDate() : new Date().getDate()) + "日");

        rinkingObj.selfRinking(); //个人排名/我的排名
        rinkingObj.deptRinking(); //部门排名（月排名、周排名）
        //根据配置决定是否展示组排名
        if(sysConfigMap &&  sysConfigMap["groupRinkingFlag"] && sysConfigMap["groupRinkingFlag"]["value"] == 1){
            rinkingObj.groupRinking(); //组周排名
            $("#weekGroupRinking").css("display", "block");//展示
        }else {
            $("#weekGroupRinking").css("display", "none");//展示
        }
    },
    mediaUserRinking: function () {
        //目前还没有
    },
    selfRinking: function () {
        requestData(rinkingObj.param, "/rankingUsereStatistics/getSalesmanRanking", "post", "json", true, function (data) {
            $("#mySelfRinking").text(parseInt(data.selfRanking || 0)); //个人排名
            if(data.rankingList && data.rankingList.length > 0){
                var length = data.rankingList.length <= 10 ? data.rankingList.length : 10; //前10名
                for(var index = 1; index <= length; index++){
                    $("#"+rinkingObj.selfRinkingIdMap[index]).html(data.rankingList[index-1].user_name);
                }
            }
        });
    },
    deptRinking: function () {
        requestData(rinkingObj.deptMonthParam, "/rankingUsereStatistics/getDeptRanking", "post", "json", true, function (data) {
            if (data && data.length > 0) {
                data.sort(rinkingObj.sortData);
                var html = "";
                $(data).each(function (i, d) {
                    var deptName = rinkingObj.isGroup ? d.deptName + "(" + d.companyCodeName + ")" : d.deptName;
                    if (i == 0) {
                        $("#oneDept").html(deptName);
                    }
                    if (i == 1) {
                        $("#twoDept").html(deptName);
                    }
                    if (i == 2) {
                        $("#threeDept").html(deptName);
                    }
                    if (i > 2) { //story114:首页中部门排名时间改成按月【KFXQ-ZNXT-077】
                        var divClass = rinkingObj.weekRinkingSeqMap[i] || "selfRankingSeq";
                        var deptName = (rinkingObj.isGroup ? d.deptName + "(" + d.companyCodeName + ")" : d.deptName);
                        html += "<div title='" + deptName + "'>\n" +
                            "        <div class=\"" + divClass + "\">" + (i + 1) + "</div>\n" +
                            "        <div class=\"selfRankingText\">\n" +
                            "            " + deptName + "\n" +
                            "        </div>\n" +
                            "    </div>";
                    }
                });
                $("#weekDeptRinking").html(html);
            }
        });
        /*requestData(rinkingObj.deptWeekParam, "/rankingUsereStatistics/getDeptRanking", "post", "json", true, function (data) {
            if (data && data.length > 0) {
                var html = "";
                data.sort(rinkingObj.sortData);
                $(data).each(function (i, d) {
                    var divClass = rinkingObj.weekRinkingSeqMap[i] || "selfRankingSeq";
                    var deptName = (rinkingObj.isGroup ? d.deptName + "(" + d.companyCodeName + ")" : d.deptName);
                    html += "<div title='"+deptName+"'>\n" +
                        "        <div class=\""+divClass+"\">"+(i+1)+"</div>\n" +
                        "        <div class=\"selfRankingText\">\n" +
                        "            "+deptName+"\n" +
                        "        </div>\n" +
                        "    </div>";
                    // html += "<div class=\"weekDeptRinking\">"+deptName+"</div>";
                });
                $("#weekDeptRinking").html(html);
            }
        });*/
    },
    groupRinking: function () {
        requestData(rinkingObj.groupWeekParam, "/rankingUsereStatistics/getDeptRanking", "post", "json", true, function (data) {
            if (data && data.length > 0) {
                var html = "";
                data.sort(rinkingObj.sortData);
                $(data).each(function (i, d) {
                    var divClass = rinkingObj.weekRinkingSeqMap[i] || "selfRankingSeq";
                    var deptName = (rinkingObj.isGroup ? d.deptName + "(" + d.companyCodeName + ")" : d.deptName);
                    html += "<div title='"+deptName+"'>\n" +
                        "        <div class=\""+divClass+"\">"+(i+1)+"</div>\n" +
                        "        <div class=\"selfRankingText\">\n" +
                        "            "+deptName+"\n" +
                        "        </div>\n" +
                        "    </div>";
                });
                $("#weekGroupRinking").html(html);
            }
        });
    },
    sortData: function (a, b) {
        return b.rownum - a.rownum;
    }
};
//日程
var scheduleObj = {
    monthMap: {
        1: "一月",
        2: "二月",
        3: "三月",
        4: "四月",
        5: "五月",
        6: "六月",
        7: "七月",
        8: "八月",
        9: "九月",
        10: "十月",
        11: "十一月",
        12: "十二月"
    },
    weekMap: {1: "周日", 2: "周一", 3: "周二", 4: "周三", 5: "周四", 6: "周五", 7: "周六"},
    remindSelectMap: {
        0: {0: "无", 1: "日程当天", 2: "1天前", 3: "2天前", 4: "1周前"},
        1: {0: "无", 1: "日程开始时", 2: "5分钟前", 3: "15分钟前", 4: "30分钟前", 5: "1小时前", 6: "2小时前"}
    }, //根据是否全天决定提醒下拉列表值
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    currentDay: new Date().getFullYear() + "-" + ((new Date().getMonth() + 1) < 10 ? "0" + (new Date().getMonth() + 1) : (new Date().getMonth() + 1))
        + "-" + (new Date().getDate() < 10 ? "0" + new Date().getDate() : new Date().getDate()),
    init: function () {
        scheduleObj.getCalendar();

        //单选和复选框添加选中事件
        $("#addScheduleFrom").find('.i-checks').on('ifClicked', function (event) {
            var $input = $(this).find(" input");
            //设置提醒下拉列表值
            var selectMap = scheduleObj.remindSelectMap[$input.val()];
            if (selectMap && Object.getOwnPropertyNames(selectMap).length > 0) {
                $("#remindFlag").empty();
                for (var key in selectMap) {
                    var option = "";
                    if (key == 0) {
                        option = " <option value=\"" + key + "\" selected=\"selected\">" + selectMap[key] + "</option>";
                    } else {
                        option = " <option value=\"" + key + "\">" + selectMap[key] + "</option>";
                    }
                    $("#remindFlag").append(option);
                }
            }
        });
    },
    initRadio: function () {
        $("#addScheduleFrom input[type='radio']").each(function () {
            if ($(this).attr("checked")) {
                $(this).iCheck("check");
            } else {
                $(this).iCheck("uncheck");
            }
        });
        $('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green',
        });
    },
    getCalendar: function () {
        var param = {year: scheduleObj.year, month: scheduleObj.month};
        requestData(param, "/schedule/getCalendar", "post", "json", false, function (data) {
            var html = "";
            $.each(data, function (i, dayMap) {
                //dayFlag: -1-上个月日期、0-当前月日期、1-当前天日期、2-下个月日期
                var dayClass = (dayMap.dayFlag == -1 || dayMap.dayFlag == 2) ? "otherMonthDay" : "currentMonthDay";
                dayClass = dayMap.dayFlag == 1 ? "dateClick planCalenderDayActive" : dayClass; //当天
                if ((dayMap.week == 1 || dayMap.week == 7) && dayMap.dayFlag == 0 && dayMap.dayFlag != 1) { //如果是周末，并且当月日期，则字体红色
                    dayClass += " zmDay";
                }

                if (dayMap.dayFlag == 1) { //设置左边日期
                    $("#planListDay").html(dayMap.monthCH + "&nbsp;" + scheduleObj.weekMap[dayMap.week]);
                    scheduleObj.loadScheduleList(dayMap.date);//根据指定日期加载日程列表
                }
                html += "<li data-point='" + dayMap.point + "' data-date='" + dayMap.date + "' data-monthCH='" + dayMap.monthCH + "' data-week='" + dayMap.week + "'>\n" +
                    "     <div onclick='scheduleObj.dateClick(this);' class=\"planCalenderDayText\">\n" +
                    "         <div class=\"" + dayClass + "\">" + dayMap.day + "</div>\n" +
                    "     </div>\n";
                if (dayMap.point == 1) {
                    var pointClass = dayMap.dayFlag == 1 ? "whitePoint" : "bluePoint"; //如果是当天，则白色
                    pointClass = dayMap.date < scheduleObj.currentDay ? "greyPoint" : pointClass; //如果是已过时间则为灰色，否则蓝色
                    html += "     <div onclick='scheduleObj.dateClick(this);' class=\"planCalenderDayPoint " + pointClass + "\">.</div>\n";
                }
                html += "    </li>";
            });
            $("#calenderMonth").text(scheduleObj.monthMap[scheduleObj.month]);  //设置月
            $("#calenderYear").text(scheduleObj.year + "年"); //设置年
            $("#scheduleCalender").html(html); //设置日
        });
    },
    nextMonth: function () {
        scheduleObj.year = scheduleObj.month == 12 ? scheduleObj.year + 1 : scheduleObj.year;
        scheduleObj.month = scheduleObj.month == 12 ? 1 : scheduleObj.month + 1;
        scheduleObj.getCalendar();
    },
    lastMonth: function () {
        scheduleObj.year = scheduleObj.month == 1 ? scheduleObj.year - 1 : scheduleObj.year;
        scheduleObj.month = scheduleObj.month == 1 ? 12 : scheduleObj.month - 1;
        scheduleObj.getCalendar();
    },
    dateClick: function (t) {
        var $div = $(t).parent().children("div:first-child").children("div:first-child");
        $(".planCalenderDayText div:first-child").removeClass("planCalenderDayActive");
        $div.addClass("planCalenderDayActive");
        //重新渲染日期下方点的颜色
        $("#scheduleCalender .planCalenderDayPoint").each(function (i, t) {
            if ($(t).parent().attr("data-point") == 1) {
                $(t).removeClass("whitePoint");
                $(t).removeClass("bluePoint");
                $(t).removeClass("greyPoint");
                if ($(t).parent().children("div:first-child").children("div:first-child").hasClass("planCalenderDayActive")) {
                    $(t).addClass("whitePoint");
                } else {
                    if ($(t).parent().attr("data-date") < scheduleObj.currentDay) {
                        $(t).addClass("greyPoint");
                    } else {
                        $(t).addClass("bluePoint");
                    }
                }
            }
        });

        var dataDate = $(t).parent().attr("data-date");
        var dataMonthCH = $(t).parent().attr("data-monthCH");
        var dataWeek = $(t).parent().attr("data-week");
        // $("#scheduleCalender").find("li > div div:first-child").removeClass("planCalenderDayActive");
        $("#planListDay").html(dataMonthCH + "&nbsp;" + scheduleObj.weekMap[dataWeek]);
        scheduleObj.loadScheduleList(dataDate);
    },
    loadScheduleList: function (dataDate) { //根据指定日期加载日程列表
        //左边日程列表
        requestData({date: dataDate}, "/schedule/listScheduleByDate", "post", "json", true, function (data) {
            //首先添加默认日程，当前固定死
            var defaultSchedule = $("#defaultList").html();
            var $scheduleList = $("#scheduleList");
            $scheduleList.html(defaultSchedule);
            if (data && data.length > 0) {
                var html = "";
                $(data).each(function (i,v) {
                    if(v.isAllDay == 0){ //0-全天
                        html = " <li>\n" +
                            "       <div class=\"jyAddTitle\">全天</div>\n";
                        if(v.jumpUrl){
                            html += "       <div class=\"jyAddContent text_space\" style='color: #337ab7;width: 85%;' onclick='page(\""+v.jumpUrl+"\",\""+v.jumpTitle+"\")' title='"+v.name+"'>"+v.name+"</div>\n";
                        }else{
                            html += "       <div class=\"jyAddContent text_space\" style='width: 85%;' title='"+v.name+"'>"+v.name+"</div>\n";
                        }
                        html += "    </li>";
                    }else {
                        var startDate = new Date(v.startDate).format("hh:mm");
                        var endDate = new Date(v.endDate).format("hh:mm");
                        html = "<li>\n" +
                            "       <div class=\"jyAddTitle\">\n" +
                            "           <div class=\"planAddTime1\">\n" +
                            "               "+startDate+"\n" +
                            "           </div>\n" +
                            "           <div class=\"planAddTime2\">\n" +
                            "               "+endDate+"\n" +
                            "           </div>\n" +
                            "       </div>\n";
                        if(v.jumpUrl){
                            html += "       <div class=\"jyAddContent text_space\" style='color: #337ab7;width: 85%;' onclick='page(\""+v.jumpUrl+"\",\""+v.jumpTitle+"\")' title='"+v.name+"'>"+v.name+"</div>\n";
                        }else{
                            html += "       <div class=\"jyAddContent text_space\" style='width: 85%' title='"+v.name+"'>"+v.name+"</div>\n";
                        }
                        html += "    </li>";
                    }
                    $scheduleList.append(html);
                });
            }

            scheduleObj.showDetail(); //日程详情展示
        });
    },
    addSchedule: function () {
        $("#addScheduleFrom").find("input").removeClass('error');
        $("#addScheduleFrom").validate().resetForm();
        $("#addScheduleFrom")[0].reset();
        scheduleObj.initRadio(); //初始化单选按钮
        //设置默认时间
        // $("#startDate").val(new Date().format("yyyy-MM-dd hh:mm"));
        // $("#endDate").val(new Date().format("yyyy-MM-dd hh:mm"));
        $("#addSchedule").modal("toggle");
    },
    saveSchedule: function () {
        if (!$("#addScheduleFrom").valid()) return;
        startModal("#saveBtn");
        requestData(JSON.stringify($("#addScheduleFrom").serializeJson()), "/schedule/save", "post", "json", true, function (data) {
            Ladda.stopAll();
            swal({
                title: data.code == 200 ? "成功!" : "失败",
                text: data.code == 200 ? "新建日程成功！" : data.msg,
                type: data.code == 200 ? "success" : "error",
                html: true
            });
            if (data.code == 200) {
                $("#addSchedule").modal("toggle");
                scheduleObj.getCalendar(); //重新刷新日历表
            }
        }, true);
    },
    isRoleByCodeAndType: function (code, type) {
        var roles = user.roles;//获取用户角色
        var result = false;
        if (roles && roles.length > 0) {
            for (var i = 0; i < roles.length; i++) {
                if (roles[i].type == type && (code == 'all' || roles[i].code == code)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    },
    isAccess: function (authInfo) {
        var isAccess = false; //默认没有权限跳转
        var auths = authInfo.split(",");
        if (auths.contains('all')) { //所有人都可以访问
            isAccess = true;
        } else {  //根据权限判断
            for (var i = 0; i < auths.length; i++) {
                var type = auths[i].split("-")[0];
                var code = auths[i].split("-")[1];
                if (scheduleObj.isRoleByCodeAndType(code, type)) {
                    isAccess = true;
                    break;
                }
            }
        }
        return isAccess;
    },
    gotoPage: function (url, title, t) {
        var authInfo = $(t).attr("data-auth");//authInfo:权限控制，all-表示对应链接所有人可访问，否则格式为role表的type-code格式，可逗号分割。
        if (authInfo) {
            if (scheduleObj.isAccess(authInfo)) {
                page(url, title);
            } else {
                layer.msg("很抱歉，您没有访问权限！")
            }
        }

    },
    showDetail: function () {
        //日程权限控制
        $("#scheduleList li").each(function (i, tt) {
            var authInfo1 = $(tt).attr("data-auth");
            if (authInfo1) {
                if (scheduleObj.isAccess(authInfo1)) { //如果没权限则不展示
                    //如果有权限，并且是每日计划添加时，需要判断今天是需要填写
                    if($(tt).attr("data-type") == 'userPlan'){
                        requestData({workDate: scheduleObj.currentDay}, "/workDate/getWorkDateByDate", "post", "json", true, function (data) {
                            if(data){ //如果有查询到日期，则判断是否是工作日
                                if(data.dateType == 1 || data.dateType == 2){
                                    $(tt).css("display", "none");
                                }else {
                                    $(tt).css("display", "block");
                                }
                            }else{ //否则，判断是否是周末
                                if(new Date().getDay() == 0 || new Date().getDay() == 6){
                                    $(tt).css("display", "none");
                                }else{
                                    $(tt).css("display", "block");
                                }
                            }
                        });
                    }else{
                        $(tt).css("display", "block");
                    }
                } else {
                    $(tt).css("display", "none");
                }
            }
        })
    }
}
//论坛、建议
var adviceTopic = {
    companyCode: user.companyCode,
    adviceTable: {
        url: baseUrl + '/propose/listPgByself',
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
        colNames: ['id', '建议类型', '提出人', '录入时间', '部门', '状态', '问题描述'],
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
                search: false,
                hidden: true
            },
            {
                name: 'adviceType',
                index: 'adviceType',
                editable: false,
                width: 60,
                align: "left",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'name',
                index: 'name',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'entryTime',
                index: 'entryTime',
                editable: false,
                width: 120,
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
                name: 'deptName',
                index: 'deptName',
                editable: false,
                width: 80,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'state',
                index: 'state',
                editable: false,
                width: 60,
                align: "center",
                sortable: false,
                sorttype: "int",
                formatter: function (d) {
                    if (d == 0) {
                        return "<span style='color: red'>未处理</span>";
                    } else if (d == 1) {
                        return "<span>已处理</span>";
                    } else if (d == 2) {
                        return "<span>处理中</span>"
                    } else if (d == 3) {
                        return "<span>已确认</span>"
                    } else if (d == 4) {
                        return "<span style='color: #953b39'>已驳回</span>"
                    }
                }
            },
            {
                name: 'problemDescription',
                index: 'problemDescription',
                editable: false,
                width: 200,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (a, b, rows) {
                    //如果要跳转到建议管理页面的话，状态不好控制
                    return "<a style='width:200px;display: inline-block;overflow:hidden; white-space:nowrap; text-overflow:ellipsis'  title='"+rows.problemDescription+"' href='javascript:page(\"/propose/propose_list?flag=3&id="+rows.id+"\",\"建议查看\")'>"+rows.problemDescription+"</a>";
                }
            }
        ],
        pager: "",
        viewrecords: true,
        caption: null,
        add: false,
        edit: false,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    },
    //论坛列表展示
    queryTopic: function () {
        $.ajax({
            type: "post",
            url: "/topic/queryTopicByWorkbench",
            data: null,
            dataType: "json",
            success: function (data) {
                $("#showTopicContent").empty();
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    html += '<li>' +
                        '<div class="luntanListIcon" style="margin-top: 0px;">' +
                        '<img src="' + data[i].picture + '" onerror="this.src=\'/img/mrtx_2.png\'" style="width:45px;height:45px;margin-top: -2px;">' +
                        '</div>' +
                        '<div style="height: 30px;line-height: 30px;">' +
                        '<div class="luntanListTitle" style="margin-left: 30px;border: 0px;display: inline-block;overflow:hidden; white-space:nowrap; text-overflow:ellipsis">' +
                        "<a onclick='goTopic(" + data[i].id + "," + data[i].forum_id + ",\"" + data[i].company_code + "\")' href='javascript:void(0)'>" + data[i].title + "</a>" +
                        '</div>' +
                        '</div>' +
                        '<div class="luntanContent" style="margin-left: 22px;">' +
                        '<div class="luntanUser">' +
                        '<span style="color: #f8ac59;">' + data[i].forumName + '</span>' +
                        '<span>&nbsp;&nbsp;|&nbsp;&nbsp;</span>' +
                        '<span>' + data[i].user_name + '&nbsp;&nbsp;' + new Date(data[i].create_time).format("yyyy-MM-dd hh:mm:ss") + '</span>' +
                        '</div>' +
                        '<div class="luntanNum">' +
                        '<div class="luntanNum1" title="阅读数">' +
                        '<div class="luntanGive"> </div>' +
                        '<div style="float: left;">' + data[i].viewNum + '</div>' +
                        '</div>' +
                        '<div class="luntanNum1" title="点赞数">' +
                        '<div class="luntanLike"> </div>' +
                        '<div style="float: left;">' + data[i].likeNum + '</div>' +
                        '</div>' +
                        '<div class="luntanNum1" title="灌水数">' +
                        '<div class="luntanguanshui"> </div>' +
                        '<div style="float: left;">' + data[i].dislikeNum + '</div>' +
                        '</div>' +
                        '</div>' +
                        '</div>' +
                        '</li>';
                }
                $("#showTopicContent").append(html);
            }
        });
    },
    //事项更多
    // more: function () {
    //     var goFlag = $("#goFlag").val();
    //     if (goFlag == 1) {
    //         //建议管理
    //         page("/propose/propose_list", "建议查询");
    //     } else {
    //         //论坛管理
    //         page("/bbs/queryTopic?companyCode=" + user.companyCode, "论坛管理");
    //     }
    // }
    init: function () {
        //读取配置，决定是否隐藏显示论坛
        if(sysConfigMap && sysConfigMap["bbsDisplayFlag"] && sysConfigMap["bbsDisplayFlag"]["value"] == 1){
            $("#luntanTab").css("display", "block");
            adviceTopic.luntanClick();
        }else {
            $("#luntanTab").css("display", "none");
            adviceTopic.jianyiClick();
        }
    },
    tabClick: function () {
        //建议管理tab页切换
        $("#jianyiTab").click(function (t) {
            $("#jianyiTabActive").css("display", "block");
            $("#adviceContent").css("display", "block");
            $("#luntanTabActive").css("display", "none");
            $("#topicContent").css("display", "none");
            adviceTopic.queryAdvice();
        });
        //论坛管理tab页切换
        $("#luntanTab").click(function (t) {
            $("#jianyiTabActive").css("display", "none");
            $("#adviceContent").css("display", "none");
            $("#luntanTabActive").css("display", "block");
            $("#topicContent").css("display", "block");
            adviceTopic.queryTopic();
        });
    },
    jianyiClick:function(){
        $("#jianyiTabActive").css("display", "block");
        $("#adviceContent").css("display", "block");
        $("#luntanTabActive").css("display", "none");
        $("#topicContent").css("display", "none");
        adviceTopic.queryAdvice();
    },
    luntanClick:function(){
        $("#jianyiTabActive").css("display", "none");
        $("#adviceContent").css("display", "none");
        $("#luntanTabActive").css("display", "block");
        $("#topicContent").css("display", "block");
        adviceTopic.queryTopic();
    },
    //建议列表展示
    queryAdvice: function () {
        $.ajax({
            type: "post",
            url: "/propose/listPgByself",
            data: null,
            dataType: "json",
            success: function (data) {
                $("#query_advice_table").empty();
                var html = "";
                $.each(data.list,function (i,propose) {
                    var id = propose.id;
                    var state = propose.state;
                    var date = propose.entryTime;
                    var desc = propose.problemDescription;
                    var str="";
                    var entryTime="";
                    if (state == 0) {
                        str="<span style='color: red'>未处理</span>";
                    } else if (state == 1) {
                        str="<span>已处理</span>";
                    } else if (state == 2) {
                        str="<span>处理中</span>"
                    } else if (state == 3) {
                        str="<span>已确认</span>"
                    } else if (state == 4) {
                        str="<span style='color: #953b39'>已驳回</span>"
                    }
                    if(date){
                        entryTime=new Date(date).format("yyyy-MM-dd hh:mm:ss");
                    }else{
                        entryTime="";
                    }
                    var tips= "<a style='width:200px;display: inline-block;overflow:hidden; white-space:nowrap; text-overflow:ellipsis'  title='"+desc+"' href='javascript:page(\"/propose/propose_list?flag=3&id="+id+"\",\"建议查看\")'>"+desc+"</a>";
                    html += "<li>" +
                        "<ul style=\"height: 40px; line-height: 40px;border-bottom: #ebebeb 1px solid;color: #999999\">" +
                        "<li style=\"float: left;width: 15%;text-align: center\">" + propose.adviceType + "</li>" +
                        "<li style=\"float: left;width: 15%;text-align: center\">" + propose.name + "</li>" +
                        "<li style=\"float: left;width: 15%;text-align: center\">" + entryTime + "</li>" +
                        "<li style=\"float: left;width: 15%;text-align: center\">" + propose.deptName + "</li>" +
                        "<li style=\"float: left;width: 10%;text-align: center\">" + str + "</li>" +
                        "<li style=\"float: left;width: 30%;text-align: center\">" + tips + "</li>" +
                        "</ul>" +
                        "</li>";
                });
                $("#query_advice_table").append(html);
            }
        });

        //创建待办表格对象
        // adviceGrid = new dataGrid("query_advice_table", adviceTopic.adviceTable, "", "db");
        // //加载待办表格
        // adviceGrid.loadGrid();
        // //设置待办下面的按钮
        // adviceGrid.setNavGrid();query_advice_table
    },

}

function goTopic(id, forumId, companyCode) {
    page("/bbs/showTopic?topicId=" + id + "&companyCode=" + companyCode + "&forumId=" + forumId, "论坛详情");
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
        success: callBackFun,
        error: function () {
            Ladda.stopAll();//隐藏加载按钮
        }
    };
    if (contentType) {
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
};