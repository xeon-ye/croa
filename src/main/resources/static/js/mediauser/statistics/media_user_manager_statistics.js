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
        if(((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'MJ'|| user.dept.code == 'GL')) || user.dept.code == 'CW'){
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
        supplier.init();
        media.init();
        outgo.init();
        artType.init();
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
    requestData({deptId: deptId,deptCode:'MJ'},"/dept/listAllDeptByIdAndCode","POST",function (result) {
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
        //默认加载天的数据
        common.dateSelect('artCountBox',$("#artCountDay"));//初始化
        topOption.setValue("artCountBox", "4");

        //默认加载天的数据
        common.dateSelect('payAmountBox',$("#payAmountDay"));//初始化
        topOption.setValue("payAmountBox", "4");

        //默认加载天的数据
        common.dateSelect('outgoAmountBox',$("#outgoAmountDay"));//初始化
        topOption.setValue("outgoAmountBox", "4");

        //默认加载天的数据
        common.dateSelect('qkAmountBox',$("#qkAmountDay"));//初始化
        topOption.setValue("qkAmountBox", "4");
    },
    //设置统计数值
    setValue: function (id, dateSelect) {
        $.ajax({
            url: baseUrl+"/mediaUsereManagerStatistics/topOptionSetValue",
            data: {tjType: id, dateSelect: dateSelect,currentDeptId:$("#currentDeptId").val()},
            dataType:"json",
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
        //默认加载天的数据
        common.dateSelect('middleBox',$("#middleDay"));//初始化
        middleOption.setValue("middleBox", "4");
    },
    setValue: function (id, dateSelect) {
        $.ajax({
            url: baseUrl+"/mediaUsereManagerStatistics/topOptionSetValue",
            data: {tjType: id, dateSelect: dateSelect,currentDeptId:$("#currentDeptId").val()},
            dataType:"json",
            success: function (resData) {
                var dw = getDateStr(dateSelect);
                middleOption.reflush(resData,dw);
            }
        });
    },
    option: {
        "title": {"text": "", "subtext": "", "x": "left"},
        "color": ["#e5323e", "#003366", "#006699"],
        "backgroundColor": "#fff",
        "legend": {"y": "8px", "data": ["稿件数量", "应付金额"]},
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
                name: '(单位：件、元)'
            }
        ],
        "series": [{"name": "稿件数量", "type": "bar", "data": []},
            {"name": "应付金额", "type": "bar", "data": []}
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
        middleOption.chart = echarts.init(document.getElementById('meddle_chart'));
        middleOption.chart.setOption(middleOption.option, true);
    },
    reflush: function (dataList,dw) {
        var len = middleOption.option.series[0].data.length;
        //清空原来的数据
        for (var i = 0; i < len; i++) {
            middleOption.option.series[0].data = [];
            middleOption.option.xAxis[0].data = [];
            middleOption.option.series[0].data[i] = 0;
            middleOption.option.series[1].data[i] = 0;
        }

        for (var i = 0; i < dataList.length; i++) {
            middleOption.option.xAxis[0].data[i] = dataList[i].sj ? dataList[i].sj+dw : "";
            middleOption.option.series[0].data[i] = dataList[i].artCount || 0;
            middleOption.option.series[1].data[i] = dataList[i].payAmount || 0;
        }
        middleOption.load();
    }
};

var supplier = {
    gridObj: {
        url: baseUrl+'/mediaUsereManagerStatistics/supplierSort',
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
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 23,
        rowList: [10, 20, 30],
        colNames: ['供应商ID','供应商名称', '应付金额', '已付金额', '变化趋势'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'supId',
                index: 'supId',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden:true
            },
            {
                name: 'supName',
                index: 'supName',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return common.getSingleLinkHtml(rows.supId,rows.supName,"supplier");
                }
            },
            {
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
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
                name: 'changePayAmount',
                index: 'changePayAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var changeValue = rowdata.changePayAmount || 0;
                    var colorUp = "rgba(35, 198, 200, 1)";
                    var colorDown = "rgb(237, 85, 101)";

                    if (changeValue < 0) {
                        colorValue = colorDown;
                        changeValue = (changeValue * 100).toFixed(1) + "%";
                    } else {
                        colorValue = colorUp;
                        changeValue = "+" + (changeValue * 100).toFixed(1) + "%";
                    }
                    var html = '<span style="color:${colorValue}">${changeValue}</span>';
                    html = html.replace("${colorValue}", colorValue).replace("${changeValue}", changeValue);
                    return html;
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
        Views.loadParentMediaType("artType");
        //默认加载天的数据
        common.dateSelect('supplierBox',$("#supplierDay"));//初始化
        $("#supplierForm").find("[name='dateSelect']").val("4");
        $("#supplierForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        supplier.grid = new dataGrid("supplierTable", supplier.gridObj, "", "supplierForm");
        supplier.grid.loadGrid();
        supplier.grid.defaultParams = {}; //清空历史表单数据
        supplier.grid.search();
    },
    reflush: function (dateSelect) {
        $("#supplierForm").find("[name='dateSelect']").val(dateSelect);
        $("#supplierForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        supplier.grid.defaultParams = {}; //清空历史表单数据
        supplier.grid.search();
    }
};

var media = {
    gridObj: {
        url: baseUrl+'/mediaUsereManagerStatistics/supplierSort',
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
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 23,
        rowList: [10, 20, 30],
        colNames: ['媒体名称ID','媒体名称', '应付金额', '已付金额', '变化趋势'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden:true
            },
            {
                name: 'medName',
                index: 'medName',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return common.getSingleLinkHtml(rows.medId,rows.medName,"media");
                }
            },
            {
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
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
                name: 'changePayAmount',
                index: 'changePayAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var changeValue = rowdata.changePayAmount || 0;
                    var colorUp = "rgba(35, 198, 200, 1)";
                    var colorDown = "rgb(237, 85, 101)";

                    if (changeValue < 0) {
                        colorValue = colorDown;
                        changeValue = (changeValue * 100).toFixed(1) + "%";
                    } else {
                        colorValue = colorUp;
                        changeValue = "+" + (changeValue * 100).toFixed(1) + "%";
                    }
                    var html = '<span style="color:${colorValue}">${changeValue}</span>';
                    html = html.replace("${colorValue}", colorValue).replace("${changeValue}", changeValue);
                    return html;
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
        common.dateSelect('mediaBox',$("#mediaDay"));//初始化
        $("#mediaForm").find("[name='dateSelect']").val("4");
        $("#mediaForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        media.grid = new dataGrid("mediaTable", media.gridObj, "", "mediaForm");
        media.grid.loadGrid();
        media.grid.defaultParams = {}; //清空历史表单数据
        media.grid.search();
    },
    reflush: function (dateSelect) {
        $("#mediaForm").find("[name='dateSelect']").val(dateSelect);
        $("#mediaForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        media.grid.search();
    }
};

var outgo = {
    gridObj: {
        url: baseUrl+'/mediaUsereManagerStatistics/outgoSort',
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
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 23,
        rowList: [10, 20, 30],
        colNames: ['供应商名称ID','供应商名称', '申请金额', '请款金额', '最近请款时间'],
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'supplierId',
                index: 'supplierId',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                hidden:true
            },
            {
                name: 'supplierName',
                index: 'supplierName',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (value, grid, rows) {
                    return common.getSingleLinkHtml(rows.supplierId,rows.supplierName,"supplier");
                }
            },
            {
                name: 'applyAmount',
                index: 'applyAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'payAmount',
                index: 'payAmount',
                editable: false,
                width: 100,
                align: "center",
                sortable: false
            },
            {
                name: 'applyTime',
                index: 'applyTime',
                editable: false,
                width: 100,
                align: "center",
                sortable: false,
                formatter: function (d) {
                    return new Date(d).format("yyyy-MM-dd");
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
        common.dateSelect('outgoBox',$("#outgoDay"));//初始化
        $("#outgoForm").find("[name='dateSelect']").val("4");
        $("#outgoForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        outgo.grid = new dataGrid("outgoTable", outgo.gridObj, "", "outgoForm");
        outgo.grid.loadGrid();
        outgo.grid.defaultParams = {}; //清空历史表单数据
        outgo.grid.search();
    },
    reflush: function (dateSelect) {
        $("#outgoForm").find("[name='dateSelect']").val(dateSelect);
        $("#outgoForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        outgo.grid.search();
    }
};

var artType = {
    chart:{},
    option: {
        "title": {},
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
        tooltip: {
            trigger: 'item',
            position:function(p){
                var id = document.getElementById('artTypeChart');
                if ($(id).width() - p[0]- $(id).find("div .echarts-tooltip").width()-20 <0) {
                    p[0] = p[0] - $(id).find("div .echarts-tooltip").width() -40;
                }
                return [p[0], p[1]];
            },
            formatter: "{a} <br/>{b} : {c} ({d}%)"
        },
        "calculable": true,
        "xAxis": null,
        "yAxis": null,
        "series": [],
        noDataLoadingOption:{
            effect:'bar',
            text:'暂无数据',
            textStyle:{
                fontSize : 14
            }
        },
        "series": [
            {
                "name": "稿件数",
                "type": "pie",
                "radius": ["35%","50%"],
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
                                fontSize : '10',
                                fontWeight : 'bold'
                            }
                        }
                    }
                }
            }]
    },
    init: function () {
        //默认加载当天数据
        common.dateSelect('artTypeBox',$("#artTypeDay"));//初始化
        artType.loadPie('4');
    },
    loadPie: function (dateSelect) {
        $("#artTypeForm").find("[name='dateSelect']").val(dateSelect);
        $("#artTypeForm").find("[name='currentDeptId']").val($("#currentDeptId").val());
        /*artType.option.title = {};
        artType.option.series = [
            {
                name: '',
                type: 'pie',
                radius: '55%',
                center: ['50%', '60%'],
                data: [

                ],
                itemStyle: {
                    emphasis: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ];*/
        $.ajax({
            url: baseUrl+"/mediaUsereManagerStatistics/artTypeFb",
            data:$("#artTypeForm").serializeJson(),
            dataType:"json",
            success:function(resData){
                artType.option.legend.data = [];
                artType.option.series[0].data = [];
                for(var i=0;i<resData.length;i++){
                    var name = resData[i].name || "";
                    var count = resData[i].count || 0;
                    var obj = {value:count,name:name,mediaTypeId:resData[i].id};
                    artType.option.series[0].data.push(obj);
                    artType.option.legend.data.push(name);
                }
                setTimeout(function () {
                    artType.chart = echarts.init(document.getElementById('artTypeChart'));
                    //点击事件
                    artType.chart.on('click', function (params) {
                        common.toggleModal(params.data.mediaTypeId,params.data.name,"mediaType");
                    });
                    var num = artType.option.legend.data.length;//图例数量
                    var height = 14;//默认高度14
                    if(num > 11){//当图例数量大于15时。图例会分成多行，进行单列算法计算
                        var itemCap = artType.option.legend.itemGap;//图例间隔
                        var padding =  artType.option.legend.padding;//图例内边距
                        var eleHeight = $("#artTypeChart").innerHeight();//div总高度 = 图例高度和，计算公式：eleHeight = padding * 2 + itemCap*(num-1)+heigth*num 向下取整
                        height = Math.floor((eleHeight-(padding*2 + itemCap*(num-1)))/num);
                    }
                    artType.option.legend.itemHeight = height;
                    artType.chart.setOption(artType.option);
                },0);
            }
        });
    }
};