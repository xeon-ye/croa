var common = {
    dateSelect: function (id, value) {
        $("#" + id).find("a").removeClass("hover");
        $(value).addClass("hover");
    },
    getSingleLinkHtml:function (id,value,type) { //获取单个a链接
        var html = "";
        if(id){
            value = value ? value : id;//如果value为空则展示ID
            html += "<a onclick=\"common.toggleModal("+id+",'"+value+"','"+type+"');\">"+value+"</a>";
        }
        return html;
    },
    toggleModal:function (id,name,type) {
        if("cust" == type){
            var title = "["+name+"]-客户统计";
            statisticsModal.loadConfig({enterType:"cust",enterParam:{custId:id},title:title}); //加载用户配置
        }
        if("business" == type){
            var title = "["+name+"]-业务统计";
            statisticsModal.loadConfig({enterType:"business",enterParam:{currentUserId:id},title:title}); //加载用户配置
        }
        if("mediaUser" == type){
            var title = "["+name+"]-媒介统计";
            statisticsModal.loadConfig({enterType:"mediaUser",enterParam:{currentUserId:id},title:title}); //加载用户配置
        }
        if("mediaType" == type){
            var title = "["+name+"]-板块统计";
            statisticsModal.loadConfig({enterType:"mediaType",enterParam:{mediaType:id},title:title}); //加载用户配置
        }
        if("media" == type){
            var title = "["+name+"]-媒体统计";
            statisticsModal.loadConfig({enterType:"media",enterParam:{mediaId:id},title:title}); //加载用户配置
        }
        if("supplier" == type){
            var title = "["+name+"]-供应商统计";
            statisticsModal.loadConfig({enterType:"supplier",enterParam:{supplierId:id},title:title}); //加载用户配置
        }
        $("#statisticsModal").modal("toggle");
    }
};

//部门树选择
var deptTree = {
    loadDept:function(){
        var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
        var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
        var deptDiv = document.getElementById("deptDiv");
        //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
        if(((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'YW'|| user.dept.code == 'GL')) || user.dept.code == 'CW'){
            deptDiv.style.display = 'block';

            $("#selDept").click(function () {
                $("#deptModal").modal('toggle');
            });
            $('#treeview').treeview({
                data: [getTreeData(isZC())],
                onNodeSelected: function (event, data) {
                    $("#currentDeptId").val(data.id);
                    $("#chooseDeptName").val(data.text);
                    $("#deptModal").modal('hide');
                    deptTree.reflush();//刷新数据
                }
            });
            $("#cleanDept").click(function () {
                $("#currentDeptId").val("");//部门初始化
                $("#chooseDeptName").val("");//部门初始化
                deptTree.reflush();//刷新数据
            });
        }
    },
    reflush:function () {  //刷新页面数据
        //刷新数据
        topOption.init();
        middleOption.init();
        profitSort.init();
        saleAmountSort.init();
        noIncomeSort.init();
        noIncomeSort1.init();
        deptBusiness.init();
        deptSaleAmountSort.init();
        deptIncomeSort.init();
    }
};

//判断当前用户是否总裁
var isZC = function () {
    var roles = user.roles;//获取用户角色
    var isZC = false;//是否总裁角色
    if(roles){
        for(var i=0; i < roles.length; i++){
            if(roles[i].code == 'ZC' || roles[i].code == 'FZC'){
                isZC = true;
                break;
            }
        }
    }
    return isZC;
}
//获取部门树数据
function getTreeData(isZC) {
    var deptTreeData = {};
    var deptId = user.dept.id;//当前用户部门ID
    var deptCode = user.dept.code;//当前部门编码
    var deptCompanyCode = user.dept.companyCode;//部门公司代码
    if(deptCompanyCode == "JT" && (deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL")){
        requestData(null,"/dept/getRootDept","POST",function (result) {
            var root = result.data.root;
            if (root){
                deptId = root.id;//整个集团的业务和媒介部
            }else{
                deptId = 517;//整个集团的业务和媒介部
            }
        });
    }else if(deptCode == "CW" || isZC || user.currentCompanyQx || deptCode == "GL"){
        requestData({companyCode: deptCompanyCode},"/dept/getCompanyByCode","POST",function (result) {
            var company = result.data.company;
            if (company){
                deptId = company.id;//整个集团的业务和媒介部
            }
        });
    }
    //具体查询
    requestData({deptId: deptId,deptCode:'YW'},"/dept/listAllDeptByIdAndCode","POST",function (result) {
        var arrays = result.data.list;
        if (arrays != null && arrays.length > 0)
            deptTreeData = arrays[0];
    });
    return deptTreeData;
}

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: "json",
        async: false,
        success: callBackFun
    });
}

var topOption = {
    init: function () {
        //统计当天信息
        common.dateSelect('artCountBox',$("#artCountDay"));//初始化
        topOption.setValue("artCountBox", "4");

        common.dateSelect('saleAmountBox',$("#saleAmountDay"));//初始化
        topOption.setValue("saleAmountBox", "4");

        common.dateSelect('incomeAmountBox',$("#incomeAmountDay"));//初始化
        topOption.setValue("incomeAmountBox", "4");

        common.dateSelect('cjPeoBox',$("#cjPeoDay"));//初始化
        topOption.setValue("cjPeoBox", "4");
    },
    //设置统计数值
    setValue: function (id, dateSelect) {
        $.ajax({
            url: baseUrl + "/businessManagerStatistics/topOptionSetValue",
            data: {tjType: id, dateSelect: dateSelect,currentDeptId:$("#currentDeptId").val()},
            dataType: "json",
            success: function (resData) {
                var obj = resData.length > 0 ? resData[0] : {};
                if (obj == null) {
                    obj = {};
                }
                topOption.set(id, obj);
            }
        })
    },
    set: function (id, obj) {
        var value = obj.value || 0;
        var changeValue = obj.changeValue || 0;

        var colorUp = "rgba(35, 198, 200, 1)";
        var colorDown = "rgb(237, 85, 101)";
        var iconUp = "fa-level-up";
        var iconDown = "fa-level-down";

        var colorValue = colorUp;
        var iconValue = iconUp;
        if (changeValue < 0) {
            colorValue = colorDown;
            iconValue = iconDown;
        }
        changeValue = (changeValue * 100).toFixed(1).replace("-", "") + "%";

        $("#" + id).find(".value").html(value);
        $("#" + id).find(".colorValue").css({"color": colorValue});
        $("#" + id).find(".changeValue").html(changeValue);
        $("#" + id).find(".iconValue").removeClass(iconUp);
        $("#" + id).find(".iconValue").removeClass(iconDown);
        $("#" + id).find(".iconValue").addClass(iconValue);
    }
};

var middleOption = {
    chart: {},
    init: function () {
        common.dateSelect('middleBox',$("#middleDay"));//初始化
        middleOption.setValue("middleBox", "4");
    },
    setValue: function (id, dateSelect) {
        $.ajax({
            url: baseUrl + "/businessManagerStatistics/topOptionSetValue",
            data: {tjType: id, dateSelect: dateSelect,currentDeptId:$("#currentDeptId").val()},
            dataType: "json",
            success: function (resData) {
                var dw = getDateStr(dateSelect);
                middleOption.reflush(resData, dw);
            }
        });
    },
    option: {
        "title": {"text": "", "subtext": "", "x": "left"},
        "color": ["#e5323e", "#003366", "#006699","rgb(237, 85, 101)"],
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["稿件数量", "应收金额", "入账金额", "利润"]},
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
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：件、元、元)'
            }
        ],
        "series": [{"name": "稿件数量", "type": "bar", "data": []},
            {
                "name": "应收金额",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            },
            {
                "name": "入账金额",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            },
            {
                "name": "利润",
                type: 'line',
                smooth: true,
                itemStyle: {normal: {areaStyle: {type: 'default'}}},
                "data": []
            }
        ]
    },
    load: function () {
        middleOption.chart = echarts.init(document.getElementById('meddle_chart'));
        middleOption.chart.setOption(middleOption.option, true);
    },
    reflush: function (dataList, dw) {
        //清空原来的数据
        middleOption.option.xAxis[0].data = [];
        middleOption.option.series[0].data = [];
        middleOption.option.series[1].data = [];
        middleOption.option.series[2].data = [];
        middleOption.option.series[3].data = [];

        for (var i = 0; i < dataList.length; i++) {
            middleOption.option.xAxis[0].data[i] = dataList[i].sj ? dataList[i].sj + dw : "";
            middleOption.option.series[0].data[i] = dataList[i].artCount || 0;
            middleOption.option.series[1].data[i] = dataList[i].saleAmount || 0;
            middleOption.option.series[2].data[i] = dataList[i].incomeAmount || 0;
            middleOption.option.series[3].data[i] = dataList[i].profit || 0;
        }
        middleOption.load();
    }
};

var profitSort = {
    gridObj: {
        url: baseUrl + '/businessManagerStatistics/profitSort',
        postData: {},
        datatype: "json",
        mtype: 'get',
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        sortable: true,
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 19,
        rowList: [10, 20, 30],
        colNames: ['业务员ID', '业务员', '应收金额', '利润', '趋势'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'businessUserId',
                index: 'businessUserId',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                hidden: true,
                sorttype: "string"
            },
            {
                name: 'businessUserName',
                index: 'businessUserName',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return common.getSingleLinkHtml(rows.businessUserId,rows.businessUserName,"business");
                }
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'profit',
                index: 'profit',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'trend',
                index: 'trend',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var trend = rowdata.trend;
                    if (trend < 0) {
                        trend = Math.abs((trend * 100)).toFixed(1) + "%";
                        return '<span class="icon fa iconValue fa-level-down" style="color:rgb(237, 85, 101);"></span>&nbsp;<span style="color:rgb(237, 85, 101);">'+trend+'</span>';
                    } else if(trend > 0){
                        trend = (trend * 100).toFixed(1) + "%";
                        return '<span class="icon fa iconValue fa-level-up" style="color:rgba(35, 198, 200, 1);">&nbsp;</span><span style="color:rgba(35, 198, 200, 1);">'+trend+'</span>';
                    }else{
                        trend = "--";
                        return '<span>'+trend+'</span>';
                    }
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
    grid: {},
    init: function () {
        //默认加载天的数据
        common.dateSelect('profitSortBox',$("#profitSortDay"));//初始化
        $("#profitSortForm").find("[name='dateSelect']").val("4");
        $("#profitSortForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        profitSort.grid = new dataGrid("profitSortTable", profitSort.gridObj, "", "profitSortForm");
        profitSort.grid.loadGrid();
        profitSort.grid.defaultParams = {}; //清空历史表单数据
        profitSort.grid.search();
    },
    reflush: function (dateSelect) {
        $("#profitSortForm").find("[name='dateSelect']").val(dateSelect);
        $("#profitSortForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        profitSort.grid.defaultParams = {}; //清空历史表单数据
        profitSort.grid.search();
    }
};

var saleAmountSort = {
    gridObj: {
        url: baseUrl + '/businessManagerStatistics/custSaleAmountSort',
        postData: {},
        datatype: "json",
        mtype: 'get',
        // data: mydata,
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        sortable: true,
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 19,
        rowList: [10, 20, 30],
        colNames: ['客户ID','客户名称', '应收金额', '到款金额', '变化趋势'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'custId',
                index: 'custId',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden:true
            },
            {
                name: 'custName',
                index: 'custName',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return common.getSingleLinkHtml(rows.custId,rows.custName,"cust");
                }
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'incomeAmount',
                index: 'incomeAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'trend',
                index: 'trend',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var trend = rowdata.trend;
                    if (trend < 0) {
                        trend = Math.abs((trend * 100)).toFixed(1) + "%";
                        return '<span class="icon fa iconValue fa-level-down" style="color:rgb(237, 85, 101);"></span>&nbsp;<span style="color:rgb(237, 85, 101);">'+trend+'</span>';
                    } else if(trend > 0){
                        trend = (trend * 100).toFixed(1) + "%";
                        return '<span class="icon fa iconValue fa-level-up" style="color:rgba(35, 198, 200, 1);">&nbsp;</span><span style="color:rgba(35, 198, 200, 1);">'+trend+'</span>';
                    }else{
                        trend = "--";
                        return '<span>'+trend+'</span>';
                    }
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
    grid: {},
    init: function () {
        //默认加载天的数据
        common.dateSelect('saleAmountSortBox',$("#saleAmountSortDay"));//初始化
        $("#saleAmountSortForm").find("[name='dateSelect']").val("4");
        $("#saleAmountSortForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        saleAmountSort.grid = new dataGrid("saleAmountSortTable", saleAmountSort.gridObj, "", "saleAmountSortForm");
        saleAmountSort.grid.loadGrid();
        saleAmountSort.grid.defaultParams = {}; //清空历史表单数据
        saleAmountSort.grid.search();
    },
    reflush: function (dateSelect) {
        $("#saleAmountSortForm").find("[name='dateSelect']").val(dateSelect);
        $("#saleAmountSortForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        saleAmountSort.grid.defaultParams = {}; //清空历史表单数据
        saleAmountSort.grid.search();
    }
};

var noIncomeSort = {
    gridObj: {
        url: baseUrl + '/businessManagerStatistics/custNoIncomeSort',
        postData: {},
        datatype: "json",
        mtype: 'get',
        // data: mydata,
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        sortable: true,
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 19,
        rowList: [10, 20, 30],
        colNames: ['客户ID','客户名称', '应收金额', '未到款金额', '变化趋势'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'custId',
                index: 'custId',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden:true
            },
            {
                name: 'custName',
                index: 'custName',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return common.getSingleLinkHtml(rows.custId,rows.custName,"cust");
                }
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'noIncomeAmount',
                index: 'noIncomeAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'trend',
                index: 'trend',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var trend = rowdata.trend;
                    if (trend < 0) {
                        trend = Math.abs((trend * 100)).toFixed(1) + "%";
                        return '<span class="icon fa iconValue fa-level-down" style="color:rgb(237, 85, 101);"></span>&nbsp;<span style="color:rgb(237, 85, 101);">'+trend+'</span>';
                    } else if(trend > 0){
                        trend = (trend * 100).toFixed(1) + "%";
                        return '<span class="icon fa iconValue fa-level-up" style="color:rgba(35, 198, 200, 1);">&nbsp;</span><span style="color:rgba(35, 198, 200, 1);">'+trend+'</span>';
                    }else{
                        trend = "--";
                        return '<span>'+trend+'</span>';
                    }
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
    grid: {},
    init: function () {
        //默认加载天的数据
        common.dateSelect('noIncomeSortBox',$("#noIncomeSortDay"));//初始化
        $("#noIncomeSortForm").find("[name='dateSelect']").val("4");
        $("#noIncomeSortForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        noIncomeSort.grid = new dataGrid("noIncomeSortTable", noIncomeSort.gridObj, "", "noIncomeSortForm");
        noIncomeSort.grid.loadGrid();
        noIncomeSort.grid.defaultParams = {}; //清空历史表单数据
        noIncomeSort.grid.search();
    },
    reflush: function (dateSelect) {
        $("#noIncomeSortForm").find("[name='dateSelect']").val(dateSelect);
        $("#noIncomeSortForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        noIncomeSort.grid.defaultParams = {}; //清空历史表单数据
        noIncomeSort.grid.search();
    }
};

var noIncomeSort1 = {
    gridObj: {
        url: baseUrl + '/businessManagerStatistics/businessNoIncomeSort',
        postData: {},
        datatype: "json",
        mtype: 'get',
        // data: mydata,
        height: "auto",
        page: 1,//第一页
        autowidth: true,
        rownumbers: true,
        gridview: true,
        viewrecords: true,
        multiselect: false,
        sortable: true,
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 19,
        rowList: [10, 20, 30],
        colNames: ['业务员Id','业务员', '应收金额', '未到款金额', '变化趋势'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'businessUserId',
                index: 'businessUserId',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden:true
            },
            {
                name: 'businessUserName',
                index: 'businessUserName',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return common.getSingleLinkHtml(rows.businessUserId,rows.businessUserName,"business");
                }
            },
            {
                name: 'saleAmount',
                index: 'saleAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'noIncomeAmount',
                index: 'noIncomeAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: true
            },
            {
                name: 'trend',
                index: 'trend',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var trend = rowdata.trend;
                    if (trend < 0) {
                        trend = Math.abs((trend * 100)).toFixed(1) + "%";
                        return '<span class="icon fa iconValue fa-level-down" style="color:rgb(237, 85, 101);"></span>&nbsp;<span style="color:rgb(237, 85, 101);">'+trend+'</span>';
                    } else if(trend > 0){
                        trend = (trend * 100).toFixed(1) + "%";
                        return '<span class="icon fa iconValue fa-level-up" style="color:rgba(35, 198, 200, 1);">&nbsp;</span><span style="color:rgba(35, 198, 200, 1);">'+trend+'</span>';
                    }else{
                        trend = "--";
                        return '<span>'+trend+'</span>';
                    }
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
    grid: {},
    init: function () {
        //默认加载天的数据
        common.dateSelect('noIncomeSortBox1',$("#noIncomeSortDay1"));//初始化
        $("#noIncomeSortForm1").find("[name='dateSelect']").val("4");
        $("#noIncomeSortForm1").find("[name='currentDeptId']").val($("#currentDeptId").val());
        noIncomeSort1.grid = new dataGrid("noIncomeSortTable1", noIncomeSort1.gridObj, "", "noIncomeSortForm1");
        noIncomeSort1.grid.loadGrid();
        noIncomeSort1.grid.defaultParams = {}; //清空历史表单数据
        noIncomeSort1.grid.search();
    },
    reflush: function (dateSelect) {
        $("#noIncomeSortForm1").find("[name='dateSelect']").val(dateSelect);
        $("#noIncomeSortForm1").find("[name='currentDeptId']").val($("#currentDeptId").val());
        noIncomeSort1.grid.defaultParams = {}; //清空历史表单数据
        noIncomeSort1.grid.search();
    }
};

//部门业务量
var deptBusiness = {
    //下钻功能需要保存的数组,保存当前展示的多个部门
    depts: [],
    //返回上一级需要保存的数组
    deptStack: [],
    dateSelect: '4',
    dept: {},
    init: function () {
        $.get("/dept/getFullDeptTreeByDeptId", {currentDeptId: $("#currentDeptId").val()},function (deptData) {
            $("#deptBusiness").show();
            //默认加载天
            deptBusiness.setValue(deptData.dept, deptBusiness.dateSelect);
        }, "json");
    },
    chart: {},
    setValue: function (dept, dateSelect) {
        deptBusiness.dept = dept;
        deptBusiness.dateSelect = dateSelect;
        deptBusiness.depts = [];
        var ds = "";
        for (var i = 0; i < dept.depts.length; i++) {
            if(dept.level == 0){ //如果是部门根节点（集团）
                deptBusiness.depts.push(dept.depts[i]);
                ds += ("," + dept.depts[i].id);
            }else{
                if(dept.depts[i].code === 'YW'){
                    deptBusiness.depts.push(dept.depts[i]);
                    ds += ("," + dept.depts[i].id);
                }
            }
        }
        $.ajax({
            url: baseUrl + "/businessManagerStatistics/everyDeptBusiness",
            data: {list: ds, dateSelect: dateSelect},
            dataType: "json",
            success: function (resData) {
                deptBusiness.reflush(resData.data.list);
            }
        });
    },
    clickEvent: function (param) {
        var deptId = param.data.deptId;
        var dept = null;
        if(deptBusiness.depts && deptBusiness.depts.length > 0){
            $.each(deptBusiness.depts,function (index,value) {
                if(value.id == deptId){
                    dept = value;
                }
            })
        }
        console.log(user);
        if (!dept || !dept.depts || dept.depts.length < 1) {
            parent.layer.alert("该部门下没有子部门");
            return;
        }
        $("#returnS").show();
        deptBusiness.deptStack.push(deptBusiness.dept);
        deptBusiness.setValue(dept, deptBusiness.dateSelect);
    },
    //返回上一级
    returnS: function () {
        var dept = deptBusiness.deptStack.pop();
        if (deptBusiness.deptStack.length == 0) {
            $("#returnS").hide();
        }
        deptBusiness.setValue(dept, deptBusiness.dateSelect);
    },
    option: {
        "title": {"text": "", "subtext": "", "x": "left"},
        "color": ["#e5323e", "#003366", "#006699"],
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["应收金额", "利润"]},
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
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：元、元)'
            }
        ],
        "series": [{"name": "应收金额", "type": "bar", "data": []},
            {"name": "利润", "type": "bar", "data": []}
        ]
    },
    load: function () {
        deptBusiness.chart = echarts.init(document.getElementById('deptBusiness_chart'));
        deptBusiness.chart.setOption(deptBusiness.option, true);
        deptBusiness.chart.on("click", deptBusiness.clickEvent);
    },
    reflush: function (dataList) {
        var len = deptBusiness.option.series[0].data.length;
        //清空原来的数据
        deptBusiness.option.xAxis[0].data = [];
        deptBusiness.option.series[0].data = [];
        deptBusiness.option.series[1].data = [];

        for (var i = 0; i < dataList.length; i++) {
            deptBusiness.option.xAxis[0].data[i] = dataList[i].deptName || "";
            deptBusiness.option.series[0].data[i] = {value:(dataList[i].saleAmount || 0), deptId:dataList[i].deptId};
            deptBusiness.option.series[1].data[i] = {value:(dataList[i].profit || 0), deptId:dataList[i].deptId};
        }
        deptBusiness.load();
    }
};

//本部门的销售额排名
var deptSaleAmountSort = {
    chart: {},
    init: function () {
        $("#deptSaleAmountSort").show();
        common.dateSelect('deptSaleAmountSortBox',$("#deptSaleAmountSortDay"));//初始化
        deptSaleAmountSort.setValue("deptSaleAmountSortBox", "4");
    },
    setValue: function (id, dateSelect) {
        $.ajax({
            url: baseUrl + "/businessManagerStatistics/businessTop",
            data: {deptId: $("#currentDeptId").val(), tjType: id, dateSelect: dateSelect},
            dataType: "json",
            success: function (resData) {
                deptSaleAmountSort.reflush(resData);
            }
        });
    },
    option: {
        "title": {"text": "业务量前5名", "subtext": "", "x": "left"},
        "color": ["#e5323e", "#003366", "#006699"],
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["应收金额"]},
        "tooltip": {
            "show": true,
            trigger: 'axis'
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：元)'
            }
        ],
        "series": [
            {"name": "应收金额", "type": "bar", "data": []}
        ],
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
    },
    load: function () {
        deptSaleAmountSort.chart = echarts.init(document.getElementById('deptSaleAmountSort_chart'));
        deptSaleAmountSort.chart.setOption(deptSaleAmountSort.option, true);
    },
    reflush: function (dataList) {
        var len = deptSaleAmountSort.option.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            deptSaleAmountSort.option.series[0].data = [];
            deptSaleAmountSort.option.xAxis[0].data = [];
            deptSaleAmountSort.option.series[0].data[i] = 0;
        }

        for (var i = 0; i < dataList.length; i++) {
            deptSaleAmountSort.option.xAxis[0].data[i] = dataList[i].userName || "";
            deptSaleAmountSort.option.series[0].data[i] = dataList[i].saleAmount || 0;
        }
        deptSaleAmountSort.load();
    }
};

//部门回款排名
var deptIncomeSort = {
    chart: {},
    init: function () {
        deptIncomeSort.setValue("deptIncomeSortBox", "4");
    },
    setValue: function (id, dateSelect) {
        $.ajax({
            url: baseUrl + "/businessManagerStatistics/businessTop",
            data: {deptId: $("#currentDeptId").val(), tjType: id, dateSelect: dateSelect},
            dataType: "json",
            success: function (resData) {
                deptIncomeSort.reflush(resData);
            }
        });
    },
    option: {
        "title": {"text": "回款前5名", "subtext": "", "x": "left"},
        "color": ["#e5323e", "#003366", "#006699"],
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["入账金额"]},
        "tooltip": {
            "show": true,
            trigger: 'axis'
        },
        calculable: true,
        "xAxis": [{
            "type": "category",
            "data": []
        }],
        "yAxis": [
            {
                "type": "value",
                name: '(单位：元)'
            }
        ],
        "series": [
            {"name": "入账金额", "type": "bar", "data": []}
        ],
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
    },
    load: function () {
        deptIncomeSort.chart = echarts.init(document.getElementById('deptIncomeSort_chart'));
        deptIncomeSort.chart.setOption(deptIncomeSort.option, true);
    },
    reflush: function (dataList) {
        var len = deptIncomeSort.option.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            deptIncomeSort.option.series[0].data = [];
            deptIncomeSort.option.xAxis[0].data = [];
            deptIncomeSort.option.series[0].data[i] = 0;
        }

        for (var i = 0; i < dataList.length; i++) {
            deptIncomeSort.option.xAxis[0].data[i] = dataList[i].userName || "";
            deptIncomeSort.option.series[0].data[i] = dataList[i].incomeAmount || 0;
        }
        deptIncomeSort.load();
    }
};