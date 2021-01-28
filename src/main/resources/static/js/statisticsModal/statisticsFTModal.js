/**
 * 统计模态框方法：Tab的id序号和对应内容区序号一一对应
 * config、options（用户个性化设置参数，与config对应）选项说明：
 *  enabledNav: 是否启动导航栏功能，数据类型：bool, true:启动，默认false
 *  enterType: 调起模态框类型，数据类型：string，值：cust-指定客户调起、business-业务员调起、mediaUser-媒介调起、mediaType-板块调起、media-媒体维度、supplier-供应商维度
 *  enterParam:{}, //调起模态框参数
 *  title: 模态框标题
 *  width: 模态框宽度，数据类型：number  默认1200px
 *  height：模态框内容区高度，数据类型：number 默认 600px
 */
var statisticsFTModal = {
    config:{
        enterParam:{}, //调起模态框参数
        title: "媒体复投详情",
        mediaName: "", //媒体名称
        mediaId: "", //媒体ID
        currentUserDept:"", //当前用户部门编码MJ-媒介（可链接媒介、板块、媒体、供应商）、YW-业务（仅可链接业务和客户维度）
        currentUserCompany:"", //当前用户公司代码
        width: 1400, //模态框默认宽度
        height: 300, //模态框内容默认高度
    },
    requestData: function (data, url, requestType,callBackFun,async) { //数据请求方法
        $.ajax({
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: "json",
            async: async,
            success: callBackFun
        });
    },
    init: function (options) {
        statisticsFTModal.loadDateTimeControl();//每次仅加载一次；放到钩子函数外面加载
        $("#statisticsFTModal").draggable();//配置模态框可拖动 需要引入“jquery-ui-1.10.4.custom.min.css”和 “jquery-ui.min.js”
        //模态框弹出前调用的事件，可当做钩子函数-在调用 show 方法后触发。
        $('#statisticsFTModal').on('show.bs.modal', function () {
            // statisticsFTModal.loadConfig(options); //加载用户配置
            statisticsFTModal.addHiddenInput();//加载不同维度的隐藏域
            statisticsFTModal.initDialog();//1、初始化窗口渲染样式
        });
        //模态框弹出后调用的事件，可当做钩子函数-当模态框对用户可见时触发（将等待 CSS 过渡效果完成）。
        $('#statisticsFTModal').on('shown.bs.modal', function () {
            statisticsFTModal.queryDispatch(); //加载数据
        });
        //模态框关闭前调用的事件，可当做钩子函数
        $('#statisticsFTModal').on('hidden.bs.modal', function () {
            statisticsFTModal.initForm();//重置Form
        })
    },
    initDialog: function () {
        //1、设置窗口的渲染
        $("#modalDialog").css("width",statisticsFTModal.config.width + "px");
        $("#modalContent").css("width",statisticsFTModal.config.width + "px");
        $("#modalBody").css("height",statisticsFTModal.config.height + "px");
        $(".statisticsContent").css("width", statisticsFTModal.config.width + "px");//设置统计维度内容的宽度 == 模态框宽度
        $("#statisticsFTModal #dialogTitle").text(statisticsFTModal.config.title); //设置模态框标题

        //8、设置各种点击事件
        statisticsFTModal.defineEvent();
    },
    loadConfig: function (options) {  //加载配置
        if(!options){
            return;
        }
        var deptCode = user.dept.code;//当前用户部门编码MJ-媒介、YW-业务
        statisticsFTModal.config.currentUserDept = deptCode;
        statisticsFTModal.config.currentUserCompany = user.dept.companyCode;
        statisticsFTModal.config.enterParam = options.enterParam || {}; //设置进入参数
        statisticsFTModal.config.mediaName = options.mediaName; //设置媒体名称
        statisticsFTModal.config.mediaId = options.mediaId; //设置媒体ID
        if(statisticsFTModal.config.mediaName){
            statisticsFTModal.config.title = "["+statisticsFTModal.config.mediaName+"]媒体复投详情"; //设置模态框标题
        }else {
            statisticsFTModal.config.title = options.title || statisticsFTModal.config.title; //设置模态框标题
        }

        statisticsFTModal.config.width = options.width || statisticsFTModal.config.width; //设置模态框宽度
        statisticsFTModal.config.height = options.height || statisticsFTModal.config.height; //设置模态框高度
    },
    loadDateTimeControl: function () { //加载时间控件
        var modalIssuedDateStart = {
            elem: '#modalIssuedDateStart',
            format: 'YYYY/MM/DD',
            istime: false,
            istoday: false,
            choose:function () {
                var startTime = $("#modalIssuedDateStart").val();
                var endTime = $("#modalIssuedDateEnd").val();
                if(startTime && endTime && startTime > endTime){
                    layer.msg("开始时间不能大于结束时间");
                    $("#modalIssuedDateStart").val("")
                }
            }
        };
        laydate(modalIssuedDateStart);
        var modalIssuedDateEnd = {
            elem: '#modalIssuedDateEnd',
            format: 'YYYY/MM/DD',
            istime: false,
            istoday: false,
            choose:function () {
                var startTime = $("#modalIssuedDateStart").val();
                var endTime = $("#modalIssuedDateEnd").val();
                if(startTime && endTime && startTime > endTime){
                    layer.msg("结束时间不能小于开始时间");
                    $("#modalIssuedDateEnd").val("")
                }
            }
        };
        laydate(modalIssuedDateEnd);
    },
    addHiddenInput: function () { //条件添加隐藏域，用于保存当前入口对象
        $("#statisticsFTModal #paramForm").append("<input type=\"hidden\" id=\"mediaId\" name=\"mediaId\" value=\""+statisticsFTModal.config.mediaId+"\">");
        $("#statisticsFTModal #paramForm").append("<input type=\"hidden\" id=\"mediaName\" name=\"mediaName\" value=\""+statisticsFTModal.config.mediaName+"\">")
    },
    initForm: function () {  //重置Form
        $("#statisticsFTModal #paramForm input[type='hidden']").remove();//移除隐藏域
        $("#modalTimeQuantum").val(1);//近7天
        $(".modalIssuedDate").hide();//时间区间隐藏
    },
    defineEvent: function (){  //定义页面事件
        //时间下拉列表改变事件
        $("#modalTimeQuantum").change(function () {
            if ($(this).val() == 4) {
                $(".modalIssuedDate").show();
            } else {
                $(".modalIssuedDate").hide();
                statisticsFTModal.queryDispatch();
            }
        })

        //导出功能
        $("#modalExportBtn").click(function () {
            var params = $("#statisticsFTModal #paramForm").serializeJson();
            location.href = "/media1/batchFTExport" + "?" + $.param(params);
        });
    },
    queryDispatch: function () { //业务调度
        statisticsFTModal.handle();
    },
    handle: function () { //enterType = cust  处理方法
        //1、加载趋势图
        statisticsFTModal.requestData($("#statisticsFTModal #paramForm").serializeJson(),"/media1/listMediaFT","post",function (resData) {
            fTModalChartObj.reflushTrend(resData)
        },true);
        //2、加载表格
        /*if(statisticsFTModal.config.currentUserDept == 'MJ'){
            fTModalTableListObj.mj.init();
        }else {
            fTModalTableListObj.yw.init();
        }*/

    }
};

//页面图表
var fTModalChartObj = {
    chart:{},  //图表对象
    trendOption:{
        "color": ["#EE7383"],
        "backgroundColor": "#fff",
        "legend": {"data": ["稿件数量"]},
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
        "tooltip": {
            "show": true,
            trigger: 'axis'
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            axisLabel:{
                interval:0,
                margin:2,
                rotate: 0
            },
            boundaryGap : false,
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：件)'
            }
        ],
        "series": [
            {
                "name": "稿件数量",
                "type": "line",
                // stack:"总量",
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            }
        ]
    }, //趋势图表配置
    initTrendChart: function () { //初始化趋势图
        fTModalChartObj.chart.trend = echarts.init(document.getElementById('modalChartByDate'));
    },
    reflushTrend: function (dataList){
        //清空原来的数据
        fTModalChartObj.trendOption.xAxis[0].data = [];
        fTModalChartObj.trendOption.series[0].data = [];
        //数据封装
        for (var i = 0; i < dataList.length; i++) {
            fTModalChartObj.trendOption.xAxis[0].data[i] = dataList[i].ftDate ? dataList[i].ftDate + "月" : "";
            fTModalChartObj.trendOption.series[0].data[i] = dataList[i].artNum || 0;
        }
        //加载
        fTModalChartObj.loadTrend();
    },
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            fTModalChartObj.initTrendChart();
            fTModalChartObj.chart.trend.setOption(fTModalChartObj.trendOption);
            window.onresize = function () {
                fTModalChartObj.chart.trend.resize();
            }
        },0);
    },
};

//页面表格
/*
var fTModalTableListObj = {
    yw:{
        grid:{},
        table:{
            url: baseUrl + '/media1/listMediaFTByPage',
            postData: $("#statisticsFTModal #paramForm").serializeJson(),
            datatype: "json",
            mtype: 'post',
            height: "auto",
            page: 1,//第一页
            autowidth: true,
            rownumbers: true,
            gridview: true,
            viewrecords: true,
            multiselect: false,
            sortable: true,
            sortorder: "asc", //排序方式：升序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 50, //每页记录数
            rowList: [10,20, 50,100],//每页记录数可选列表
            jsonReader: {//server返回Json解析设定
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: false
            },
            colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
                {
                    name: 'ftTime',
                    index: 'ftTime',
                    label: '发布日期',
                    editable: false,
                    align: "center",
                    width:80,
                    sortable: true,
                    sorttype: "string"
                },
                {
                    name: 'mediaName',
                    index: 'mediaName',
                    label: '媒体名称',
                    editable: false,
                    align: "center",
                    width:120,
                    sortable: false
                },
                {
                    name: 'title',
                    index: 'title',
                    label: '稿件标题',
                    editable: false,
                    align: "center",
                    width:120,
                    sortable: false
                },
                {
                    name: 'link',
                    index: 'link',
                    label: '稿件链接',
                    editable: false,
                    align: "center",
                    width:120,
                    sortable: false,
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
                    name: 'incomeState',
                    index: 'incomeState',
                    label: '回款状态',
                    editable: false,
                    align: "center",
                    width:60,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(value == 1){
                            return "已回款";
                        }else if(value == 2){
                            return "部分回款";
                        }else {
                            return "未回款";
                        }
                    }
                },
                {
                    name: 'invoiceState',
                    index: 'invoiceState',
                    label: '开票状态',
                    editable: false,
                    align: "center",
                    width:60,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(value == 1){
                            return "已开票";
                        }else if(value == 2){
                            return "开票中";
                        }else {
                            return "未开票";
                        }
                    }
                },
                {
                    name: 'issueState',
                    index: 'issueState',
                    label: '发布状态',
                    editable: false,
                    align: "center",
                    width:60,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(value == 4){
                            return "已发布";
                        }else if(value == 3){
                            return "已驳回";
                        }else if(value == 2){
                            return "进行中";
                        }else if(value == 1){
                            return "待安排";
                        }else {
                            return "未下单";
                        }
                    }
                },
            ],
            pager: "#modalDetailPager",
            viewrecords: true,
            caption: "媒体详情",
            add: true,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false
        },
        init:function () {
            //板块列表排名
            setTimeout(function () {
                fTModalTableListObj.yw.grid = new dataGrid("modalDetailTable", fTModalTableListObj.yw.table, "modalDetailPager", "paramForm");
                fTModalTableListObj.yw.grid.loadGrid();
                fTModalTableListObj.yw.grid.setNavGrid();
                fTModalTableListObj.yw.grid.defaultParams = {};//清空历史表单数据
                fTModalTableListObj.yw.grid.search();
            },0);

        }
    },
    mj:{
        grid:{},
        table:{
            url: baseUrl + '/media1/listMediaFTByPage',
            postData: $("#statisticsFTModal #paramForm").serializeJson(),
            datatype: "json",
            mtype: 'post',
            height: "auto",
            page: 1,//第一页
            autowidth: true,
            rownumbers: true,
            gridview: true,
            viewrecords: true,
            multiselect: false,
            sortable: true,
            sortorder: "asc", //排序方式：升序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 50, //每页记录数
            rowList: [10,20, 50,100],//每页记录数可选列表
            jsonReader: {//server返回Json解析设定
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: false
            },
            colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
                {
                    name: 'ftTime',
                    index: 'ftTime',
                    label: '发布日期',
                    editable: false,
                    align: "center",
                    width:80,
                    sortable: true,
                    sorttype: "string"
                },
                {
                    name: 'mediaName',
                    index: 'mediaName',
                    label: '媒体名称',
                    editable: false,
                    align: "center",
                    width:120,
                    sortable: false
                },
                {
                    name: 'supplierContactor',
                    index: 'supplierContactor',
                    label: '供应商名称',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: false,
                },
                {
                    name: 'supplierName',
                    index: 'supplierName',
                    label: '供应商联系人',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: false,
                },
                {
                    name: 'title',
                    index: 'title',
                    label: '稿件标题',
                    editable: false,
                    align: "center",
                    width:120,
                    sortable: false
                },
                {
                    name: 'link',
                    index: 'link',
                    label: '稿件链接',
                    editable: false,
                    align: "center",
                    width:120,
                    sortable: false,
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
                    name: 'outgoState',
                    index: 'outgoState',
                    label: '请款状态',
                    editable: false,
                    align: "center",
                    width:60,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(value == 1){
                            return "已请款";
                        }else if(value == 2){
                            return "请款中";
                        }else {
                            return "未请款";
                        }
                    }
                },
                {
                    name: 'issueState',
                    index: 'issueState',
                    label: '发布状态',
                    editable: false,
                    align: "center",
                    width:60,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(value == 4){
                            return "已发布";
                        }else if(value == 3){
                            return "已驳回";
                        }else if(value == 2){
                            return "进行中";
                        }else if(value == 1){
                            return "待安排";
                        }else {
                            return "未下单";
                        }
                    }
                },
            ],
            pager: "#modalDetailPager",
            viewrecords: true,
            caption: "媒体详情",
            add: true,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false
        },
        init:function () {
            //板块列表排名
            setTimeout(function () {
                fTModalTableListObj.mj.grid = new dataGrid("modalDetailTable", fTModalTableListObj.mj.table, "modalDetailPager", "paramForm");
                fTModalTableListObj.mj.grid.loadGrid();
                fTModalTableListObj.mj.grid.setNavGrid();
                fTModalTableListObj.mj.grid.defaultParams = {};//清空历史表单数据
                fTModalTableListObj.mj.grid.search();
            },0);

        }
    }
};*/
