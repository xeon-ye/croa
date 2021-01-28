/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,callBackFun, async) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: async || false,
        success: callBackFun
    });
}

var commonObj={
    warehouseList:{},
    //加载物品采购订单统计信息
    loadPurchaseOrderData:function () {
        if(commonObj.hasRoleCKZY()){
            var param=$("#purchaseOrderSearchForm").serializeForm();
            requestData(param,"/inventoryHome/purchaseOrderStatistics","post",function (data) {
                if(data.code==200){
                    $("#orderNum").text(data.data.entity.result["orderNum"]||0);
                    $("#orderAmount").text(data.data.entity.result["orderAmount"]||0);
                    purchaseOrderTrendObj.reflushTrend(data.data.entity.list);
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    })
                }
            },true);
        }else {
            purchaseOrderTrendObj.reflushTrend();
        }
    },
    //加载商品库存统计信息
    loadStockAnalysisData:function () {
        if(commonObj.hasRoleCKZY()){
            var param=$("#shopInventorySearchForm").serializeForm();
            requestData(param,"/inventoryHome/stockAnalysis","post",function (data) {
                if(data.code==200){
                    $("#stockMoney").text(data.data.entity.result["stockMoney"]||0);
                    $("#stockAmount").text(data.data.entity.result["stockAmount"]||0);
                    shopInventoryObj.reflushPie(data.data.entity.listPie);
                    shopInventoryObj.reflushTrend(data.data.entity.list);
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    })
                }
            });
        }else {
            shopInventoryObj.reflushPie();
            shopInventoryObj.reflushTrend();
        }
    },
    //加载出入库统计信息
    loadOutBoundData:function () {
        if(commonObj.hasRoleCKZY()){
            var param=$("#outBoundSearchForm").serializeForm();
            requestData(param,"/inventoryHome/outBoundStatistics","post",function (data) {
                if(data.code==200){
                    $("#putMoney").text(data.data.entity.result["putMoney"]||0);
                    $("#outMoney").text(data.data.entity.result["outMoney"]||0);
                    outBoundTrendObj.reflushTrend(data.data.entity.list);
                }else if(data.code==1002){
                    swal({
                        title:"提示",
                        text:data.msg
                    })
                }
            });
        }else{
            outBoundTrendObj.reflushTrend();
        }
    },
    //缓存仓库信息
    listWarehouseData:function(){
        if(commonObj.isEmptyObject(commonObj.warehouseList)){
            requestData(null,"/warehouse/warehouseList","post",function (data) {
                commonObj.warehouseList=data;
            })
        }
    },
    isEmptyObject:function (obj) {
        var flag = true;
        for(var attr in obj){
            flag=false;//obj有值则不用向后台查询产品分类数据
        }
        return flag;
    },
    hasRoleCKZY: function () {
        var len = user.roles.length;
        for (var i = 0; i < len; i++) {
            if (user.roles[i].type == 'CK' && user.roles[i].code == 'ZY') {
                return true;
            }
        }
        return false;
    }
}

$(function () {
    pageDateControlInit();//日期选择条件初始化
    shopInventoryObj.selectWarehouse($("#wareId"),null);//加载仓库
    commonObj.loadPurchaseOrderData(); //加载采购数据
    commonObj.loadStockAnalysisData(); //商品库存分析
    commonObj.loadOutBoundData(); //采购入库统计趋势图
});

//日期选择条件初始化
function pageDateControlInit() {
    var laydate = layui.laydate;
    //采购订单统计趋势
    var purchaseOrderTimeStart ={
        elem: '#purchaseOrderTimeStart',
        format: 'yyyy-MM-dd',
        istime:true,
        istoday:false,
    }
    laydate.render(purchaseOrderTimeStart);
    var purchaseOrderTimeEnd ={
        elem: '#purchaseOrderTimeEnd',
        format: 'yyyy-MM-dd',
        istime:true,
        istoday:false,
    }
    laydate.render(purchaseOrderTimeEnd);
    //商品库存分析
    var shopInventoryTimeStart ={
        elem: '#shopInventoryTimeStart',
        format: 'yyyy-MM-dd',
        istime:true,
        istoday:false,
    }
    laydate.render(shopInventoryTimeStart);
    var shopInventoryTimeEnd ={
        elem: '#shopInventoryTimeEnd',
        format: 'yyyy-MM-dd',
        istime:true,
        istoday:false,
    }
    laydate.render(shopInventoryTimeEnd);
    var outBoundTimeStart ={
        elem: '#outBoundTimeStart',
        format: 'yyyy-MM-dd',
        istime:true,
        istoday:false,
    }
    laydate.render(outBoundTimeStart);
    var outBoundTimeEnd ={
        elem: '#outBoundTimeEnd',
        format: 'yyyy-MM-dd',
        istime:true,
        istoday:false,
    }
    laydate.render(outBoundTimeEnd);
}

//采购订单趋势
var purchaseOrderTrendObj = {
    trend: {},
    trendOption:{
        "color": ["#00A2FD", "#FFAD6B"],
        "backgroundColor": "#fff",
        "legend": {
            "data": ["订单金额", "订单数量"]
        },
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 20
            }
        },
        "tooltip": {
            "show": true,
            trigger: 'axis'
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            boundaryGap : false,
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：元、个)'
            }
        ],
        "series": [
            {
                "name": "订单金额",
                "type": "line",
                smooth: true,
                "data": []
            },
            {
                "name": "订单数量",
                "type": 'line',
                smooth: true,
                "data": []
            }
        ]
    }, //趋势图表配置
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            purchaseOrderTrendObj.trend = echarts.init(document.getElementById('purchaseOrderTrend'));
            purchaseOrderTrendObj.trend.setOption(purchaseOrderTrendObj.trendOption);
            window.onresize = function () {
                purchaseOrderTrendObj.trend.resize();
            }
        },0);
    },
    reflushTrend: function (dataList){
        //后台返回数据格式
        // dataList = [
        //     {time: "10-01", orderAmount: 609, orderNum: 202},
        //     {time: "10-02", orderAmount: 409, orderNum: 64},
        //     {time: "10-03", orderAmount: 659, orderNum: 520},
        //     {time: "10-04", orderAmount: 1209, orderNum: 12},
        //     {time: "10-05", orderAmount: 60, orderNum: 1314}
        // ];

        //清空原来的数据
        purchaseOrderTrendObj.trendOption.xAxis[0].data = [];
        purchaseOrderTrendObj.trendOption.series[0].data = [];
        purchaseOrderTrendObj.trendOption.series[1].data = [];
        //数据封装
        if(dataList!=null){
            for (var i = 0; i < dataList.length; i++) {
                purchaseOrderTrendObj.trendOption.xAxis[0].data[i] = dataList[i].time || "";
                purchaseOrderTrendObj.trendOption.series[0].data[i] = dataList[i].orderAmount || 0;
                purchaseOrderTrendObj.trendOption.series[1].data[i] = dataList[i].orderNum || 0;
            }
        }
        //加载图表
        purchaseOrderTrendObj.loadTrend();
    },
};

//商品库存分析
var shopInventoryObj = {
    pie: {},
    trend: {},
    pieOption: {
        title : {
            subtext: '商品库存结构图',
            subtextStyle: {
                fontSize: 12,
                fontStyle: 'normal',
                fontWeight: 'normal',
            },
            x:'center',
            y:'bottom'
        },
        "backgroundColor": "#fff",
        "legend": {
            x:'left',
            y:'center',
            padding: 5,
            itemHeight:14,
            itemGap: 5,
            orient : 'vertical',
            "data": []
        },
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 20
            }
        },
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('shopInventoryPie');
                if ($(id).width() - p[0]- $(id).find("div .echarts-tooltip").width()-20 <0) {
                    p[0] = p[0] - $(id).find("div .echarts-tooltip").width() -40;
                }
                return [p[0], p[1]];
            },
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": false,
        "xAxis": null,
        "yAxis": null,
        "series": [
            {
                "name": "商品库存结构图",
                "type": "pie",
                "radius": ["30%","45%"],
                "center": ["60%", "50%"],
                "data": [],
                "itemStyle": {
                    normal : {
                        label : {
                            show : false
                        },
                        labelLine : {
                            show : false
                        }
                    },
                    emphasis : {
                        label : {
                            show : true,
                            position : 'center',
                            textStyle : {
                                fontSize : '14',
                                fontWeight : 'bold'
                            }
                        }
                    }
                }
            }]
    },
    trendOption:{
        "color": ["#00A2FD", "#FFAD6B"],
        "backgroundColor": "#fff",
        title : {
            subtext: '商品库存',
            subtextStyle: {
                fontSize: 12,
                fontStyle: 'normal',
                fontWeight: 'normal',
            },
            x:'center',
            y:'bottom'
        },
        "legend": {
            "data": ["库存金额","库存数量"]
        },
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 20
            }
        },
        "tooltip": {
            "show": true,
            trigger: 'axis'
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            boundaryGap : false,
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：元、个)'
            }
        ],
        "series": [
            {
                "name": "库存金额",
                "type": "line",
                smooth: true,
                "data": []
            },
            {
                "name": "库存数量",
                "type": "line",
                smooth: true,
                "data": []
            }
        ]
    }, //趋势图表配置
    calHeight: function (option, statisticsPieId) {
        var num = option.legend.data.length;//图例数量
        var height = 14;//默认高度14
        if(num > 15){//当图例数量大于15时。图例会分成多行，进行单列算法计算
            var itemCap = option.legend.itemGap;//图例间隔
            var padding =  option.legend.padding;//图例内边距
            var eleHeight = $(statisticsPieId).innerHeight();//div总高度 = 图例高度和，计算公式：eleHeight = padding * 2 + itemCap*(num-1)+heigth*num 向下取整
            height = Math.floor((eleHeight-(padding*2 + itemCap*(num)))/num);
        }
        return height;
    },
    loadPie: function () {
        setTimeout(function () {
            shopInventoryObj.pie = echarts.init(document.getElementById('shopInventoryPie'));
            shopInventoryObj.pieOption.legend.itemHeight = shopInventoryObj.calHeight(shopInventoryObj.pieOption, "#shopInventoryPie");
            shopInventoryObj.pie.setOption(shopInventoryObj.pieOption);
        },0);
    },
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            shopInventoryObj.trend = echarts.init(document.getElementById('shopInventoryTrend'));
            shopInventoryObj.trend.setOption(shopInventoryObj.trendOption);
            window.onresize = function () {
                shopInventoryObj.trend.resize();
            }
        },0);
    },
    reflushPie: function (dataList) {
        shopInventoryObj.pieOption.legend.data = [];//清空原数据
        shopInventoryObj.pieOption.series[0].data = [];//清空原数据
        //数据加载
        var index = 0;
        if(dataList!=null){
            for (var i = 0; i < dataList.length; i++) {
                if(dataList[i].name != undefined){
                    shopInventoryObj.pieOption.legend.data[index] = dataList[i].name;
                    var serie = {name:dataList[i].name,value:dataList[i].stockAmount};//数据
                    shopInventoryObj.pieOption.series[0].data[index] = serie;
                    index++;
                }
            }
        }
        //加载图表
        shopInventoryObj.loadPie();
    },
    reflushTrend: function (dataList){
        //清空原来的数据
        shopInventoryObj.trendOption.xAxis[0].data = [];
        shopInventoryObj.trendOption.series[0].data = [];
        shopInventoryObj.trendOption.series[1].data = [];
        //数据封装
        if(dataList!=null){
            for (var i = 0; i < dataList.length; i++) {
                shopInventoryObj.trendOption.xAxis[0].data[i] = dataList[i].time || "";
                shopInventoryObj.trendOption.series[0].data[i] = dataList[i].stockMoney || 0;
                shopInventoryObj.trendOption.series[1].data[i] = dataList[i].stockAmount || 0;
            }
        }
        //加载图表
        shopInventoryObj.loadTrend();
    },
    //加载仓库
    selectWarehouse:function (t,id) {
        layui.use(["form"],function () {
            commonObj.listWarehouseData();
            var data = commonObj.warehouseList;
            $(t).empty();
            var html="<option value=''>全部仓库</option>";
            $(data.list).each(function (i, d) {
                var selected = id == d.id ? "selected=selected" : "";
                html+="<option value='" + d.id + "' " + selected + ">" + d.name + "</option>";
            });
            $(t).append(html);
            layui.form.render();
        });
    }
};

//出入库库统计趋势图
var outBoundTrendObj = {
    trend: {},
    trendOption:{
        "color": ["#EE7383", "#72C7D9","#00A2FD", "#FFAD6B"],
        "backgroundColor": "#fff",
        "legend": {
            "data": ["入库金额","入库单数量","出库金额","出库单数量"]
        },
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 20
            }
        },
        "tooltip": {
            "show": true,
            trigger: 'axis'
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            boundaryGap : false,
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：元、个、元、个)'
            }
        ],
        "series": [
            {
                "name": "入库金额",
                "type": "line",
                smooth: true,
                "data": []
            },
            {
                "name": "入库数量",
                "type": "line",
                smooth: true,
                "data": []
            },
            {
                "name": "出库金额",
                "type": 'line',
                smooth: true,
                "data": []
            },
            {
                "name": "出库数量",
                "type": 'line',
                smooth: true,
                "data": []
            }
        ]
    }, //趋势图表配置
    loadTrend: function () { //加载趋势图
        setTimeout(function () {
            outBoundTrendObj.trend = echarts.init(document.getElementById('outBoundTrend'));
            outBoundTrendObj.trend.setOption(outBoundTrendObj.trendOption);
            window.onresize = function () {
                outBoundTrendObj.trend.resize();
            }
        },0);
    },
    reflushTrend: function (dataList){
        //后台返回数据格式
        // dataList = [
        //     {date: "10-01", putMoney: 609, putNum: 202},
        //     {date: "10-02", putMoney: 409, putNum: 64},
        //     {date: "10-03", putMoney: 659, putNum: 520},
        //     {date: "10-04", putMoney: 1209, putNum: 12},
        //     {date: "10-05", putMoney: 60, putNum: 1314}
        // ];

        //清空原来的数据
        outBoundTrendObj.trendOption.xAxis[0].data = [];
        outBoundTrendObj.trendOption.series[0].data = [];
        outBoundTrendObj.trendOption.series[1].data = [];
        outBoundTrendObj.trendOption.series[2].data = [];
        outBoundTrendObj.trendOption.series[3].data = [];
        //数据封装
        if(dataList!=null){
            for (var i = 0; i < dataList.length; i++) {
                outBoundTrendObj.trendOption.xAxis[0].data[i] = dataList[i].time || "";
                outBoundTrendObj.trendOption.series[0].data[i] = dataList[i].putMoney || 0;
                outBoundTrendObj.trendOption.series[1].data[i] = dataList[i].putAmount || 0;
                outBoundTrendObj.trendOption.series[2].data[i] = dataList[i].outMoney || 0;
                outBoundTrendObj.trendOption.series[3].data[i] = dataList[i].outAmount || 0;
            }
        }
        //加载图表
        outBoundTrendObj.loadTrend();
    },
};

//物品出库统计趋势图
// var purchaseOutTrendObj = {
//     trend: {},
//     trendOption:{
//         "color": ["#00A2FD", "#FFAD6B"],
//         "backgroundColor": "#fff",
//         "legend": {
//             "data": ["出库金额", "出库个数"]
//         },
//         noDataLoadingOption:{
//             effect:'bar',
//             text:'暂无数据',
//             textStyle:{
//                 fontSize : 20
//             }
//         },
//         "tooltip": {
//             "show": true,
//             trigger: 'axis'
//         },
//         calculable: true,
//         "xAxis": [{
//             "type": "category",
//             boundaryGap : false,
//             "data": []
//         }],
//         "yAxis": [
//             {
//                 "type": "value",
//                 name: '(单位：元、个)'
//             }
//         ],
//         "series": [
//             {
//                 "name": "出库金额",
//                 "type": "line",
//                 smooth: true,
//                 "data": []
//             },
//             {
//                 "name": "出库个数",
//                 type: 'line',
//                 smooth: true,
//                 "data": []
//             }
//         ]
//     }, //趋势图表配置
//     loadTrend: function () { //加载趋势图
//         setTimeout(function () {
//             purchaseOutTrendObj.trend = echarts.init(document.getElementById('purchaseOutTrend'));
//             purchaseOutTrendObj.trend.setOption(purchaseOutTrendObj.trendOption);
//             window.onresize = function () {
//                 purchaseOutTrendObj.trend.resize();
//             }
//         },0);
//     },
//     reflushTrend: function (dataList){
//         //后台返回数据格式
//         dataList = [
//             {date: "10-01", outMoney: 609, outNum: 202},
//             {date: "10-02", outMoney: 409, outNum: 64},
//             {date: "10-03", outMoney: 659, outNum: 520},
//             {date: "10-04", outMoney: 1209, outNum: 12},
//             {date: "10-05", outMoney: 60, outNum: 1314}
//         ];
//
//         //清空原来的数据
//         purchaseOutTrendObj.trendOption.xAxis[0].data = [];
//         purchaseOutTrendObj.trendOption.series[0].data = [];
//         purchaseOutTrendObj.trendOption.series[1].data = [];
//         //数据封装
//         for (var i = 0; i < dataList.length; i++) {
//             purchaseOutTrendObj.trendOption.xAxis[0].data[i] = dataList[i].date || "";
//             purchaseOutTrendObj.trendOption.series[0].data[i] = dataList[i].outMoney || 0;
//             purchaseOutTrendObj.trendOption.series[1].data[i] = dataList[i].outNum || 0;
//         }
//         //加载图表
//         purchaseOutTrendObj.loadTrend();
//     },
// };