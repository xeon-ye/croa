

//获取个人排名
function getSelfRanking(){
    $.ajax({
        type: "get",
        data:{companyCode: user.dept.companyCode, timeQuantum: 1},
        url: "/rankingUsereStatistics/getSelfRanking",
        dataType: "json",
        async: true,
        success: function (data) {
            $("#selfRanking").empty();
            data = data || 0;
            $("#selfRanking").val(parseFloat(data));
        }
    })
}

//查询区域
var searchForm = {
    //显示或隐藏发布日期
    showIssuedDate: function (val) {
        if (val == 3) {
            $("#issuedDateFormGroup").show();
        } else {
            $("#issuedDateFormGroup").hide();
        }
    },
    loadAllCompany:function(id){
        var company = user.companyCode;
        if(company == "JT"){
            $.ajax({
                url: baseUrl + "/dept/listAllCompany",
                type: "get",
                data: null,
                dataType: "json",
                async: false,
                success: function (data) {
                    if(data.code == 200 && data.data.result){
                        var resData = data.data.result;
                        $(resData).each(function (i, d) {
                            $(id).append("<option value='" + d.code + "' data='" + d.id + "'>" + d.name + "</option>");
                        });
                    }
                }
            });
        }else{
            $(id).append("<option value='" + user.companyCode + "' data='" + user.deptId + "'>" + user.dept.companyCodeName + "</option>");
        }

    },
    //查询
    search: function () {
        var searchFormData = $("#searchForm").serializeJson();
        var param1 = $.extend({level:3}, searchFormData);//部门排名
        var param2 = $.extend({level:4}, searchFormData);//部门组排名
        //加载部内排名
        $.ajax({
            url: baseUrl + "/rankingUsereStatistics/getDeptRanking",
            type: "post",
            data: param1,
            dataType: "json",
            async: true,
            success: function (resData) {
                statisticsResult.reflush(resData);
            }
        });
        //加载组内排名
        $.ajax({
            url: baseUrl + "/rankingUsereStatistics/getDeptRanking",
            type: "post",
            data: param2,
            dataType: "json",
            async: true,
            success: function (resData) {
                supplierResult.reflush(resData);
            }
        });
        //加载业务员前30名人员排名
        $.ajax({
            url: baseUrl + "/rankingUsereStatistics/getSalesmanRanking",
            type: "post",
            data: searchFormData,
            dataType: "json",
            async: true,
            success: function (resData) {
                supplierTable.reflush(resData.rankingList);
            }
        });
    }
};

//部内排名统计结果
var statisticsResult = {
    //左边的柱状图
    chart: {},
    //柱状图的option
    option: {
        "title": {"text": "部内排名（本图仅表示名次）", "subtext": "", "x": "left"},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["名次"]},
        "tooltip": {
            "show": true,
             formatter: function (params,ticket,callback) {
                return params.name;
            }
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            "data": []
        }],
        "yAxis": [
            {
                type: "value",
                name: '(名次)',
                axisLabel:{
                    show:false
                }
            }
        ],
        "series": [{"name": "排名", "type": "bar", "data": []}
        ]
    },
    init: function () {
        statisticsResult.chart = echarts.init(document.getElementById('statisticsResultChart'));
    },
    load: function () {
        statisticsResult.chart.setOption(statisticsResult.option, true);
    },
    reflush: function (dataList) {
        statisticsResult.init();
        var len = statisticsResult.option.series[0].data.length;
        statisticsResult.option.series[0].data = [];
        statisticsResult.option.xAxis[0].data = [];
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            statisticsResult.option.xAxis[0].data[i] = "";
            statisticsResult.option.series[0].data[i] = 0;
        }

        for (var i = 0; i < dataList.length; i++) {
            statisticsResult.option.xAxis[0].data[i] = dataList[i].deptName;
            statisticsResult.option.series[0].data[i] = dataList[i].rownum
        }
        //加载排名统计结果
        statisticsResult.load();
    }
};

//组内排名统计结果
var supplierResult = {
    //左边的柱状图
    chart: {},
    //柱状图的option
    option: {
        "title": {"text": "组内排名（本图仅表示名次）", "subtext": "", "x": "left"},
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["名次"]},
        "tooltip": {
            "show": true,
            formatter: function (params,ticket,callback) {
                return params.name;
            }
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(名次)',
                axisLabel:{
                    show:false
                }
            }
        ],

        "series": [{
            "name": "排名",
            "type": "bar",
            "data": []
        }]
    },

    init: function () {
        supplierResult.chart = echarts.init(document.getElementById('supplierResultChart'));
    },
    load: function () {
        supplierResult.chart.setOption(supplierResult.option, true);
    },

    reflush: function (dataList) {
        supplierResult.init();
        var len = supplierResult.option.series[0].data.length;
        supplierResult.option.series[0].data = [];
        supplierResult.option.xAxis[0].data = [];
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            supplierResult.option.xAxis[0].data[i] = "";
            supplierResult.option.series[0].data[i] = 0;
        }

        for (var i = 0; i < dataList.length; i++) {
            supplierResult.option.xAxis[0].data[i] = dataList[i].deptName;
            supplierResult.option.series[0].data[i] = dataList[i].rownum
        }
        //加载排名统计结果
        supplierResult.load();
    }
};

//个人排名
var supplierTable = {
    reflush: function (dataList) {
        var table = '<table style="width:100%;overflow-y: scroll;text-align:center;margin:20px 0 0 -20px;" class="ui-jqgrid-htable ui-common-table table table-bordered"><tbody>'
            + '<tr class="ui-jqgrid-labels">'
            + '<td>排名</td>'
            + '<td>' + '姓名' + '</td>'
            + '<td>' + '利润' + '</td>'
            + '</tr>';
        for (var i = 0; i < dataList.length; i++) {
            if(i<=9){
                table += '<tr class="ui-jqgrid-labels">'
                    + '<td>' + (i+1) + '</td>'
                    + '<td>' + dataList[i].user_name + '</td>'
                    + '<td>' + dataList[i].profit + '</td>'
                    + '</tr>';
            }else{
                table += '<tr class="ui-jqgrid-labels">'
                    + '<td>' + (i+1) + '</td>'
                    + '<td>' + dataList[i].user_name + '</td>'
                    + '<td>' + "--" + '</td>'
                    + '</tr>';
            }
        }
        table += '</tbody></table>';
        $("#supplierResultTable").html(table);
    }
};