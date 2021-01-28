var  XMflag= false;
$(function () {
    var project = projectDirector();
    if( project && project.contains(user.id)){
        XMflag =true;
    }else {
        XMflag =false;
    }
})
/**
 * 统计模态框方法：Tab的id序号和对应内容区序号一一对应
 * config、options（用户个性化设置参数，与config对应）选项说明：
 *  enabledNav: 是否启动导航栏功能，数据类型：bool, true:启动，默认false
 *  enterType: 调起模态框类型，数据类型：string，值：cust-指定客户调起、business-业务员调起、mediaUser-媒介调起、mediaType-板块调起、media-媒体维度、supplier-供应商维度
 *  enterParam:{}, //调起模态框参数  -必输
 *  title: 模态框标题
 *  tabTitles: 显示的Tab栏目标题，数据类型：{},key为Tab对应序号，例如：{1:"业务员",2:"媒介"}
 *  width: 模态框宽度，数据类型：number  默认1200px
 *  height：模态框内容区高度，数据类型：number 默认 600px
 *  firstTabSeq: 设置第一个Tab序号，数据类型：number，默认：1-业务员
 *  hiddenTabSeqs: ，需要隐藏的Tab栏目，数据类型：Array，值：1-业务员、2-媒介、3-板块、4-媒体、5-客户、6-供应商
 *  speed: 切换效果速度，数据类型：number 默认500ms
 */
var statisticsModal = {
    config:{
        statisticsPermissionFlag: false, //是否有权限访问，默认没有
        enabledNav: false, //是否启动导航栏功能，数据类型：bool, true:启动，默认false
        enterType: "", //调起模态框类型：cust-指定客户调起、business-业务员调起、mediaUser-媒介调起、mediaType-板块调起、media-媒体维度、supplier-供应商维度    --必输
        enterParam:{}, //调起模态框参数  -必输
        title: "统计概况",
        userType: "YW", //查询类型，默认当前用户部门Code,，仅支持MJ和YW，不是则默认YW
        currentUserDept:"", //当前用户部门编码MJ-媒介（可链接媒介、板块、媒体、供应商）、YW-业务（仅可链接业务和客户维度）
        currentUserCompany:"", //当前用户公司代码
        tabTitles: {1:"业务员", 2:"媒介", 3:"板块", 4:"媒体", 5:"客户", 6:"供应商"},
        width: 1200, //模态框默认宽度
        height: 600, //模态框内容默认高度
        firstTabSeq: 1, //设置第一个Tab序号
        hiddenTabSeqs: [], //隐藏的Tab栏目数组
        speed: 500, //切换时间
        currentPage: 1,    //当前第几个页面，默认第一个
        totalPageNum: 1,    //总共几个页面，默认一个
        statisticsUrl:{
            cust:"crm/statistics/cust_statistics",  //客户统计
            business:"biz/statistics/business_statistics", //业务统计
            mediaUser:"mediauser/statistics/media_user_statistics", //媒介统计
            mediaType:"media/media_type_statistics", //板块统计
            media:"media/media_statistics", //媒体统计
            supplier:"mediauser/statistics/supplier_statistics" //供应商统计
        }, //跳转各个统计的URL
        statisticsName:{
            cust:"客户统计",  //客户统计
            business:"业务统计", //业务统计
            mediaUser:"媒介统计", //媒介统计
            mediaType:"板块统计", //板块统计
            media:"媒体统计", //媒体统计
            supplier:"供应商统计" //供应商统计
        } //跳转各个统计的
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
    getAjax:function (obj) {  //原生Ajax请求
        var xmlhttp;
        if(window.XMLHttpRequest){
            xmlhttp = new XMLHttpRequest();
        }else{
            xmlhttp = ActionXObject("Microsoft.XMLHTTP"); // code for IE6, IE5
        }
        xmlhttp.withCredentials = true;
        xmlhttp.addEventListener("readystatechange", function () {
            if (this.readyState == 4 && this.status == 200) {
                var data = JSON.parse(this.responseText);
                obj.callback(data);
            }
        });
        //@param 最后一个参数表示是否是异步提交
        xmlhttp.open("Get",obj.url,obj.async);
        //设置头信息
        xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
        xmlhttp.setRequestHeader("x-requested-with", "XMLHttpRequest");
        xmlhttp.setRequestHeader("cache-control", "no-cache");
        //将信息发送到服务器
        xmlhttp.send();
    },
    init: function (options) {
        statisticsModal.loadDateTimeControl();//每次仅加载一次；放到钩子函数外面加载
        statisticsModal.loadUserPermission();//判断当前用户是否有权限，每次仅加载一次
        $("#statisticsModal").draggable();//配置模态框可拖动 需要引入“jquery-ui-1.10.4.custom.min.css”和 “jquery-ui.min.js”
        //模态框弹出前调用的事件，可当做钩子函数-在调用 show 方法后触发。
        $('#statisticsModal').on('show.bs.modal', function () {
            // statisticsModal.loadConfig(options); //加载用户配置
            statisticsModal.addHiddenInput();//加载不同维度的隐藏域
            statisticsModal.initDialog();//1、初始化窗口渲染样式
        });
        //模态框弹出后调用的事件，可当做钩子函数-当模态框对用户可见时触发（将等待 CSS 过渡效果完成）。
        $('#statisticsModal').on('shown.bs.modal', function () {
            //模态框权限判断-使用原始Ajax, 有些页面$.ajax 已被修改
            if(statisticsModal.config.statisticsPermissionFlag){
                statisticsModal.queryDispatch(); //加载数据
            }else{
                swal({
                    title: "没有权限",
                    type: "warning",
                    text: "很抱歉，您没权限访问统计概况！请联系管理员！"
                });
                $("#statisticsModal").modal("hide"); //可在此处判断用户权限，决定是隐藏还是显示模态框，不显示则不调用上面加载数据方法
            }
        });
        //模态框关闭前调用的事件，可当做钩子函数
        $('#statisticsModal').on('hidden.bs.modal', function () {
            statisticsModal.initForm();//重置Form
        })
    },
    initDialog: function () {
        //1、设置窗口的渲染
        $("#modalDialog").css("width",statisticsModal.config.width + "px");
        $("#modalContent").css("width",statisticsModal.config.width + "px");
        $("#modalBody").css("height",statisticsModal.config.height + "px");
        $(".statisticsContent").css("width", statisticsModal.config.width + "px");//设置统计维度内容的宽度 == 模态框宽度
        $("#dialogTitle").text(statisticsModal.config.title); //设置模态框标题

        //如果启动导航栏下面操作有效
        if(statisticsModal.config.enabledNav){
            //2、设置Tab标题
            $.each(statisticsModal.config.tabTitles,function (key,value) {
                $("#selectType_"+key).text(value);//选项卡标题
            })

            //3、设置要隐藏的Tab
            $.each(statisticsModal.config.hiddenTabSeqs,function(index,value){
                $("#selectType_"+value).css("display","none");//隐藏选项卡
                $("#selectType_"+value).addClass("hiddenTab");//设置一个样式 该样式用于下面设置事件使用，标记被隐藏
                $("#content_"+value).css("display","none");//隐藏对应的内容区
            });

            //4、设置默认显示的第一个Tab，如果该tab为隐藏的tab则处理，tab 和 对应内容区 同步处理
            if(!statisticsModal.config.hiddenTabSeqs.contains(statisticsModal.config.firstTabSeq)){
                //处理Tab
                var firstTabNode = $("#selectType_"+statisticsModal.config.firstTabSeq);//获取Tab元素
                $("#selectType_"+statisticsModal.config.firstTabSeq).remove();//移除该元素
                $(".navStyle").prepend(firstTabNode);
                //处理内容区
                var firstContentNode = $("#content_"+statisticsModal.config.firstTabSeq);//获取Tab元素
                $("#content_"+statisticsModal.config.firstTabSeq).remove();//移除该元素
                $("#scrollContent").prepend(firstContentNode);
            }

            //5、计算目前显示的内容区数量
            var statisticsContentNum = 0; //获取统计维度（Tab）数量，用于计算父级div的宽度,仅获取display不为none的
            $(".statisticsContent").each(function (index,e) {
                if($(e).css("display") === "block"){
                    statisticsContentNum ++;
                }
            });
            statisticsModal.config.totalPageNum = statisticsContentNum;//设置总页面数量
            $("#scrollContent").css("width", (statisticsModal.config.width * statisticsModal.config.totalPageNum) + "px");//计算父容器宽度，用于产生滚动条

            //6、默认选择当前页的Tab
            statisticsModal.tabStyleChange($(".selectType").filter(function () {  //过滤掉隐藏的
                return !$(this).hasClass("hiddenTab");
            }).eq(statisticsModal.config.currentPage-1));//改变tab样式

            //7、初始化设置左右切换箭头是否展示
            statisticsModal.reflushBtn();

        }else { //隐藏导航栏  和 切换按钮
            $(".navStyle").css("display","none");
            $(".horizon-prev").css("display","none");
            $(".horizon-next").css("display","none");
            //设置内容区父容器宽度
            $("#scrollContent").css("width", (statisticsModal.config.width * statisticsModal.config.totalPageNum) + "px");//计算父容器宽度，用于产生滚动条

            //仅展示当前维度的内容
            $(".statisticsContent").css("display","none");
            $("."+statisticsModal.config.enterType).parent().css("display","block");
        }
        //8、设置各种点击事件
        statisticsModal.defineEvent();
    },
    loadConfig: function (options) {  //加载配置
        if(!options){
            return;
        }
        var deptCode = user.dept.code;//当前用户部门编码MJ-媒介、YW-业务
        if(deptCode == "MJ" || deptCode == "mj"){
            statisticsModal.config.userType = "MJ"; //如果当前用户是媒介，则关联媒介查询
        }else{
            statisticsModal.config.userType = "YW";
        }
        statisticsModal.config.currentUserDept = deptCode;
        statisticsModal.config.currentUserCompany = user.dept.companyCode;
        statisticsModal.config.enabledNav = options.enabledNav || statisticsModal.config.enabledNav; //是否启动导航栏
        statisticsModal.config.enterType = options.enterType || statisticsModal.config.enterType; //设置进入方式
        statisticsModal.config.enterParam = options.enterParam || {}; //设置进入方式
        statisticsModal.config.title = options.title || statisticsModal.config.title; //设置模态框标题
        statisticsModal.config.width = options.width || statisticsModal.config.width; //设置模态框宽度
        statisticsModal.config.height = options.height || statisticsModal.config.height; //设置模态框高度
        statisticsModal.config.firstTabSeq = options.firstTabSeq || statisticsModal.config.firstTabSeq; //设置第一个Tab序号
        statisticsModal.config.speed = options.speed || statisticsModal.config.speed; //设置切换效果速度
        if(options.tabTitles && options.tabTitles instanceof Object && !options.tabTitles instanceof Array){
            statisticsModal.config.tabTitles = options.tabTitles;  //设置Tab标题
        }
        if(options.hiddenTabSeqs && options.hiddenTabSeqs instanceof Array){
            statisticsModal.config.hiddenTabSeqs = options.hiddenTabSeqs;  //设置要隐藏的Tab
        }
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
    loadUserPermission:function () { //获取当前用户访问权限
        statisticsModal.getAjax({
            url:"/statisticsOverview/statisticsOverviewPermission",
            async:false,
            callback:function (data) {
                if(data.code == 200){
                    statisticsModal.config.statisticsPermissionFlag = true;
                }else{
                    statisticsModal.config.statisticsPermissionFlag = false;
                }
            }
        });
    },
    addHiddenInput: function () { //条件添加隐藏域，用于保存当前入口对象
        if(statisticsModal.config.enterType === 'cust'){ //指定客户方式进入
            $("#paramForm").append("<input type=\"hidden\" id=\"custId\" name=\"custId\" value=\""+statisticsModal.config.enterParam.custId+"\">");
            $("#paramForm").append("<input type=\"hidden\" id=\"userType\" name=\"userType\" value=\""+statisticsModal.config.userType+"\">")
        }
        if(statisticsModal.config.enterType === 'business'){ //指定业务员方式进入
            $("#paramForm").append("<input type=\"hidden\" id=\"businessUserId\" name=\"businessUserId\" value=\""+statisticsModal.config.enterParam.currentUserId+"\">")
            $("#paramForm").append("<input type=\"hidden\" id=\"userType\" name=\"userType\" value=\""+statisticsModal.config.userType+"\">")
        }
        if(statisticsModal.config.enterType === 'mediaUser'){ //指定媒介方式进入
            $("#paramForm").append("<input type=\"hidden\" id=\"mediaUserId\" name=\"mediaUserId\" value=\""+statisticsModal.config.enterParam.currentUserId+"\">")
            $("#paramForm").append("<input type=\"hidden\" id=\"userType\" name=\"userType\" value=\""+statisticsModal.config.userType+"\">")
        }
        if(statisticsModal.config.enterType === 'mediaType'){ //指定板块方式进入
            $("#paramForm").append("<input type=\"hidden\" id=\"mediaType\" name=\"mediaType\" value=\""+statisticsModal.config.enterParam.mediaType+"\">")
            $("#paramForm").append("<input type=\"hidden\" id=\"userType\" name=\"userType\" value=\""+statisticsModal.config.userType+"\">")
        }
        if(statisticsModal.config.enterType === 'media'){ //指定媒体方式进入
            $("#paramForm").append("<input type=\"hidden\" id=\"mediaId\" name=\"mediaId\" value=\""+statisticsModal.config.enterParam.mediaId+"\">")
            $("#paramForm").append("<input type=\"hidden\" id=\"userType\" name=\"userType\" value=\""+statisticsModal.config.userType+"\">")
        }
        if(statisticsModal.config.enterType === 'supplier'){ //指定供应商方式进入
            $("#paramForm").append("<input type=\"hidden\" id=\"supplierId\" name=\"supplierId\" value=\""+statisticsModal.config.enterParam.supplierId+"\">")
            $("#paramForm").append("<input type=\"hidden\" id=\"userType\" name=\"userType\" value=\""+statisticsModal.config.userType+"\">")
        }
    },
    initForm: function () {  //重置Form
        $("#paramForm input[type='hidden']").remove();//移除隐藏域
        $("#modalTimeQuantum").val(1);//本月
        $(".modalIssuedDate").hide();//时间区间隐藏
    },
    tabStyleChange: function (node){ //改变tab样式 ：点击后，清空所有颜色，设置自己颜色
        $(".selectType").css({
            "color":"#676A6C",
            "border-bottom":"none"
        });
        $(node).css({
            "color":"#009688",
            "border-bottom":"2px solid #009688"
        });
    },
    defineEvent: function (){  //定义页面事件
        if(statisticsModal.config.enabledNav){ //如果启动导航栏
            $(".selectType").filter(function () { //过滤掉隐藏的
                return !$(this).hasClass("hiddenTab");
            }).each(function (index,e) {  //tab点击事件
                $(e).click(function () {
                    statisticsModal.tabStyleChange(e);//改变tab样式
                    var scrollLeftValue = index * statisticsModal.config.width;
                    $('#main').animate({scrollLeft:scrollLeftValue},statisticsModal.config.speed);
                    statisticsModal.config.currentPage = index + 1;//设置当前页
                    statisticsModal.reflushBtn();//刷新切换按钮
                    console.log("当前页："+statisticsModal.config.currentPage);
                });
            });
            $(".horizon-prev").click(function () {  //上一页按钮事件
                if(statisticsModal.config.currentPage > 1){
                    statisticsModal.config.currentPage = statisticsModal.config.currentPage - 1;//页码-1
                    var scrollLeftValue = (statisticsModal.config.currentPage - 1) * statisticsModal.config.width;
                    $('#main').animate({scrollLeft:scrollLeftValue},statisticsModal.config.speed);
                    statisticsModal.reflushBtn();//刷新切换按钮
                    statisticsModal.tabStyleChange( $(".selectType").filter(function () { //过滤掉隐藏的
                        return !$(this).hasClass("hiddenTab");
                    }).eq(statisticsModal.config.currentPage-1));//改变tab样式，不能根据currentPage，然后通过Tab的ID序号来获取对应Tab，应根据位置
                }
                console.log("当前页："+statisticsModal.config.currentPage);
            });
            $(".horizon-next").click(function () {  //上一页按钮事件
                if(statisticsModal.config.currentPage < statisticsModal.config.totalPageNum){
                    statisticsModal.config.currentPage = statisticsModal.config.currentPage + 1;//页码减+1
                    var scrollLeftValue = (statisticsModal.config.currentPage - 1) * statisticsModal.config.width;
                    $('#main').animate({scrollLeft:scrollLeftValue},statisticsModal.config.speed);
                    statisticsModal.reflushBtn();//刷新切换按钮
                    statisticsModal.tabStyleChange( $(".selectType").filter(function () { //过滤掉隐藏的
                        return !$(this).hasClass("hiddenTab");
                    }).eq(statisticsModal.config.currentPage-1));//改变tab样式，不能根据currentPage，然后通过Tab的ID序号来获取对应Tab，应根据位置
                }
                console.log("当前页："+statisticsModal.config.currentPage);
            });
        }

        //时间下拉列表改变事件
        $("#modalTimeQuantum").change(function () {
            if ($(this).val() == 3) {
                $(".modalIssuedDate").show();
            } else {
                $(".modalIssuedDate").hide();
                statisticsModal.queryDispatch();
            }
        })

        //导出功能
        //1、业务维度导出
        $("#modalBusinessExportBtn").click(function () {
            var params = $("#paramForm").serializeJson();
            params.exportFileType = statisticsModal.config.enterType;
            params.exportFileName = statisticsModal.config.title + "-客户详情";
            params.exportFileName = params.exportFileName.replace("[","").replace("]","");
            location.href = "/statisticsOverview/statisticsOverviewRankingExport" + "?" + $.param(params);
        });

        //2、媒介维度导出
        $("#modalMediaUserExportBtn").click(function () {
            var params = $("#paramForm").serializeJson();
            params.exportFileType = statisticsModal.config.enterType;
            params.exportFileName = statisticsModal.config.title + "-媒体详情";
            params.exportFileName = params.exportFileName.replace("[","").replace("]","");
            location.href = "/statisticsOverview/statisticsOverviewRankingExport" + "?" + $.param(params);
        });

        //3、板块维度导出
        $("#modalMediaTypeExportBtn").click(function () {
            var params = $("#paramForm").serializeJson();
            params.exportFileType = statisticsModal.config.enterType;
            params.exportFileName = statisticsModal.config.title + "-媒体详情";
            params.exportFileName = params.exportFileName.replace("[","").replace("]","");
            location.href = "/statisticsOverview/statisticsOverviewRankingExport" + "?" + $.param(params);
        });

        //4、媒体维度导出
        $("#modalMediaExportBtn").click(function () {
            var params = $("#paramForm").serializeJson();
            params.exportFileType = statisticsModal.config.enterType;
            params.exportFileName = statisticsModal.config.title + "-客户详情";
            params.exportFileName = params.exportFileName.replace("[","").replace("]","");
            location.href = "/statisticsOverview/statisticsOverviewRankingExport" + "?" + $.param(params);
        });

        //5、客户维度导出
        $("#modalCustExporttBtn").click(function () {
            var params = $("#paramForm").serializeJson();
            params.exportFileType = statisticsModal.config.enterType;
            params.exportFileName = statisticsModal.config.title + "-媒体详情";
            params.exportFileName = params.exportFileName.replace("[","").replace("]","");
            location.href = "/statisticsOverview/statisticsOverviewRankingExport" + "?" + $.param(params);
        });

        //6、供应商维度导出
        $("#modalSupplierExportBtn").click(function () {
            var params = $("#paramForm").serializeJson();
            params.exportFileType = statisticsModal.config.enterType;
            params.exportFileName = statisticsModal.config.title + "-媒体详情";
            params.exportFileName = params.exportFileName.replace("[","").replace("]","");
            location.href = "/statisticsOverview/statisticsOverviewRankingExport" + "?" + $.param(params);
        });

    },
    reflushBtn: function () {  //刷新切换按钮淡入淡出
        var prevBtn = $(".horizon-prev");
        var nextBtn = $(".horizon-next");
        if(statisticsModal.config.totalPageNum === 1){//当总页数为1时，不显示
            prevBtn.fadeOut("slow");
            nextBtn.fadeOut("slow");
        }else if(statisticsModal.config.currentPage === 1){ //当总页数大于1，并且当前是第一页，右边箭头可点击
            prevBtn.fadeOut("slow");
            nextBtn.fadeIn("slow");
        }else if(statisticsModal.config.currentPage === statisticsModal.config.totalPageNum){//当总页数大于1，并且当前是第最后页，左边箭头可点击
            prevBtn.fadeIn("slow");
            nextBtn.fadeOut("slow");
        }else{ //按钮都可点击
            prevBtn.fadeIn("slow");
            nextBtn.fadeIn("slow");
        }
    },
    queryDispatch: function () { //业务调度
        if(statisticsModal.config.enterType === 'cust'){ //指定客户方式进入
            statisticsModal.custHandle();
        }
        if(statisticsModal.config.enterType === 'business'){ //指定业务员方式进入
            statisticsModal.businessHandle();
        }
        if(statisticsModal.config.enterType === 'mediaUser'){ //指定媒介方式进入
            statisticsModal.mediaUserHandle();
        }
        if(statisticsModal.config.enterType === 'mediaType'){ //指定板块方式进入
            statisticsModal.mediaTypeHandle();
        }
        if(statisticsModal.config.enterType === 'media'){ //指定媒体方式进入
            statisticsModal.mediaHandle();
        }
        if(statisticsModal.config.enterType === 'supplier'){ //指定供应商方式进入
           statisticsModal.supplierHandle();
        }
    },
    custHandle: function () { //enterType = cust  处理方法
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/getStatisticsById","post",function (resData) { //1、顶部统计
            $("#cust_articleNum").text(fmtMoneyBringUnit(resData.data.result['articleNum'],"件") || 0);//稿件数量
            $("#cust_saleAmount").text(fmtMoneyBringUnit(resData.data.result['saleAmount']) || 0);//报价金额
            $("#cust_payAmount").text(fmtMoneyBringUnit(resData.data.result['payAmount']) || 0);//成本金额
            $("#cust_noIncomeAmount").text(fmtMoneyBringUnit(resData.data.result['noIncomeAmount']) || 0);//未到款金额
            $("#cust_profit").text(fmtMoneyBringUnit(resData.data.result['profit']) || 0);//利润
            var noIncomeAmountRate = "0.00%";
            var profitRate = "0.00%";
            if((resData.data.result.saleAmount || 0) != 0){
                noIncomeAmountRate = ((resData.data.result['noIncomeAmount'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
                profitRate = ((resData.data.result['profit'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
            }
            $("#cust_noIncomeAmountRate").text(noIncomeAmountRate);//未到款率
            $("#cust_profitRate").text(profitRate);//利润率
        },true);
        //2、加载趋势图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listTrendStatisticsById","post",function (resData) {
            modalChartObj.cust.reflushTrend(resData.data.list)
        },true);
        //3、加载饼图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listMediaTypeStatisticsById","post",function (resData) {
            modalChartObj.cust.reflushPie(resData.data.result)
        },true);
        //4、加载表格
        modalTableListObj.cust.init();
    },
    businessHandle: function () { //enterType = business  处理方法
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/getStatisticsById","post",function (resData) { //1、顶部统计
            $("#business_articleNum").text(fmtMoneyBringUnit(resData.data.result['articleNum'],"件") || 0);//稿件数量
            $("#business_saleAmount").text(fmtMoneyBringUnit(resData.data.result['saleAmount']) || 0);//报价金额
            $("#business_payAmount").text(fmtMoneyBringUnit(resData.data.result['payAmount']) || 0);//成本金额
            $("#business_noIncomeAmount").text(fmtMoneyBringUnit(resData.data.result['noIncomeAmount']) || 0);//未到款金额
            $("#business_profit").text(fmtMoneyBringUnit(resData.data.result['profit']) || 0);//利润
            var noIncomeAmountRate = "0.00%";
            var profitRate = "0.00%";
            if((resData.data.result.saleAmount || 0) != 0){
                noIncomeAmountRate = ((resData.data.result['noIncomeAmount'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
                profitRate = ((resData.data.result['profit'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
            }
            $("#business_noIncomeAmountRate").text(noIncomeAmountRate);//未到款率
            $("#business_profitRate").text(profitRate);//利润率
        },true);
        //2、加载趋势图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listTrendStatisticsById","post",function (resData) {
            modalChartObj.business.reflushTrend(resData.data.list)
        },true);
        //3、加载饼图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listMediaTypeStatisticsById","post",function (resData) {
            modalChartObj.business.reflushPie(resData.data.result)
        },true);
        //4、加载表格
        modalTableListObj.business.init();
    },
    mediaUserHandle: function () { //enterType = mediaUser  处理方法
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/getStatisticsById","post",function (resData) { //1、顶部统计
            $("#mediaUser_articleNum").text(fmtMoneyBringUnit(resData.data.result['articleNum'],"件") || 0);//稿件数量
            $("#mediaUser_saleAmount").text(fmtMoneyBringUnit(resData.data.result['saleAmount']) || 0);//报价金额
            $("#mediaUser_payAmount").text(fmtMoneyBringUnit(resData.data.result['payAmount']) || 0);//成本金额
            $("#mediaUser_noIncomeAmount").text(fmtMoneyBringUnit(resData.data.result['noIncomeAmount']) || 0);//未到款金额
            $("#mediaUser_profit").text(fmtMoneyBringUnit(resData.data.result['profit']) || 0);//利润
            var noIncomeAmountRate = "0.00%";
            var profitRate = "0.00%";
            if((resData.data.result.saleAmount || 0) != 0){
                noIncomeAmountRate = ((resData.data.result['noIncomeAmount'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
                profitRate = ((resData.data.result['profit'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
            }
            $("#mediaUser_noIncomeAmountRate").text(noIncomeAmountRate);//未到款率
            $("#mediaUser_profitRate").text(profitRate);//利润率
        },true);
        //2、加载趋势图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listTrendStatisticsById","post",function (resData) {
            modalChartObj.mediaUser.reflushTrend(resData.data.list)
        },true);
        //3、加载饼图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listMediaTypeStatisticsById","post",function (resData) {
            modalChartObj.mediaUser.reflushPie(resData.data.result)
        },true);
        //4、加载表格
        modalTableListObj.mediaUser.init();
    },
    mediaTypeHandle: function () { //enterType = mediaType  处理方法
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/getStatisticsById","post",function (resData) { //1、顶部统计
            $("#mediaType_articleNum").text(fmtMoneyBringUnit(resData.data.result['articleNum'],"件") || 0);//稿件数量
            $("#mediaType_saleAmount").text(fmtMoneyBringUnit(resData.data.result['saleAmount']) || 0);//报价金额
            $("#mediaType_payAmount").text(fmtMoneyBringUnit(resData.data.result['payAmount']) || 0);//成本金额
            $("#mediaType_noIncomeAmount").text(fmtMoneyBringUnit(resData.data.result['noIncomeAmount']) || 0);//未到款金额
            $("#mediaType_profit").text(fmtMoneyBringUnit(resData.data.result['profit'] )|| 0);//利润
            var noIncomeAmountRate = "0.00%";
            var profitRate = "0.00%";
            if((resData.data.result.saleAmount || 0) != 0){
                noIncomeAmountRate = ((resData.data.result['noIncomeAmount'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
                profitRate = ((resData.data.result['profit'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
            }
            $("#mediaType_noIncomeAmountRate").text(noIncomeAmountRate);//未到款率
            $("#mediaType_profitRate").text(profitRate);//利润率
        },true);
        //2、加载趋势图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listTrendStatisticsById","post",function (resData) {
            modalChartObj.mediaType.reflushTrend(resData.data.list)
        },true);
        //3、加载表格
        modalTableListObj.mediaType.init();
    },
    mediaHandle: function () { //enterType = media  处理方法
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/getStatisticsById","post",function (resData) { //1、顶部统计
            $("#media_articleNum").text(fmtMoneyBringUnit(resData.data.result['articleNum'],"件") || 0);//稿件数量
            $("#media_saleAmount").text(fmtMoneyBringUnit(resData.data.result['saleAmount']) || 0);//报价金额
            $("#media_payAmount").text(fmtMoneyBringUnit(resData.data.result['payAmount']) || 0);//成本金额
            $("#media_noIncomeAmount").text(fmtMoneyBringUnit(resData.data.result['noIncomeAmount']) || 0);//未到款金额
            $("#media_profit").text(fmtMoneyBringUnit(resData.data.result['profit']) || 0);//利润
            var noIncomeAmountRate = "0.00%";
            var profitRate = "0.00%";
            if((resData.data.result.saleAmount || 0) != 0){
                noIncomeAmountRate = ((resData.data.result['noIncomeAmount'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
                profitRate = ((resData.data.result['profit'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
            }
            $("#media_noIncomeAmountRate").text(noIncomeAmountRate);//未到款率
            $("#media_profitRate").text(profitRate);//利润率
        },true);
        //2、加载趋势图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listTrendStatisticsById","post",function (resData) {
            modalChartObj.media.reflushTrend(resData.data.list)
        },true);
        //3、加载表格
        modalTableListObj.media.init();
    },
    supplierHandle: function () { //enterType = supplier  处理方法
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/getStatisticsById","post",function (resData) { //1、顶部统计
            $("#supplier_articleNum").text(fmtMoneyBringUnit(resData.data.result['articleNum'],"件") || 0);//稿件数量
            $("#supplier_saleAmount").text(fmtMoneyBringUnit(resData.data.result['saleAmount']) || 0);//报价金额
            $("#supplier_payAmount").text(fmtMoneyBringUnit(resData.data.result['payAmount']) || 0);//成本金额
            $("#supplier_noIncomeAmount").text(fmtMoneyBringUnit(resData.data.result['noIncomeAmount']) || 0);//未到款金额
            $("#supplier_profit").text(fmtMoneyBringUnit(resData.data.result['profit']) || 0);//利润
            var noIncomeAmountRate = "0.00%";
            var profitRate = "0.00%";
            if((resData.data.result.saleAmount || 0) != 0){
                noIncomeAmountRate = ((resData.data.result['noIncomeAmount'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
                profitRate = ((resData.data.result['profit'] || 0)/resData.data.result.saleAmount*100).toFixed(2)+"%";
            }
            $("#supplier_noIncomeAmountRate").text(noIncomeAmountRate);//未到款率
            $("#supplier_profitRate").text(profitRate);//利润率
        },true);
        //2、加载趋势图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listTrendStatisticsById","post",function (resData) {
            modalChartObj.supplier.reflushTrend(resData.data.list)
        },true);
        //3、加载饼图
        statisticsModal.requestData($("#paramForm").serializeJson(),"/statisticsOverview/listMediaTypeStatisticsById","post",function (resData) {
            modalChartObj.supplier.reflushPie(resData.data.result)
        },true);
        //4、加载表格
        modalTableListObj.supplier.init();
    }
};

//页面图表
var modalChartObj = {
    //客户维度：cust
    cust:{
        chart:{},  //图表对象
        trendOption:{
            "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E"],
            "backgroundColor": "#fff",
            "legend": {"data": ["稿件数量", "报价金额", "成本金额", "未到账金额","利润"]},
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
                boundaryGap : false,
                "data": []
            }],
            "yAxis": [
                {
                    "type": "value",
                    name: '(单位：件、元、元、元、元)'
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
                },
                {
                    "name": "报价金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "成本金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "未到账金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "利润",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                }
            ]
        }, //趋势图表配置
        pieOption: {
            "title": {
                text:"板块占比",
                subtext:"报价金额",
                textStyle:{
                    color: '#009688',
                    fontSize: '13'
                },
                subtextStyle:{
                    fontSize: '10'
                },
                x:"center"
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
                text:'板块占比【报价金额】暂无数据',
                textStyle:{
                    fontSize : 14
                }
            },
            tooltip: {
                trigger: 'item',
                position:function(p){
                    var id = document.getElementById('modalCustChartByMediaType');
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
                    "name": "板块占比",
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
                                    fontSize : '10',
                                    fontWeight : 'bold'
                                }
                            }
                        }
                    }
                }]
        },  //板块占比
        initTrendChart: function () { //初始化趋势图
            modalChartObj.cust.chart.trend = echarts.init(document.getElementById('modalCustChartByDate'));
        },
        initPieChart: function () { //初始化板块占比图
            modalChartObj.cust.chart.pie = echarts.init(document.getElementById('modalCustChartByMediaType'));
            //点击事件
            modalChartObj.cust.chart.pie.on('click', function (params) {
                if(statisticsModal.config.currentUserDept == 'MJ' || statisticsModal.config.currentUserDept == 'GL' || statisticsModal.config.currentUserDept == 'CW'){
                    var jumpUrl = statisticsModal.config.statisticsUrl.mediaType+"?mediaTypeId="+params.data.mediaTypeId+"&mediaTypeName="+params.data.name;
                    page(jumpUrl,statisticsModal.config.statisticsName.mediaType);
                }
            });
        },
        reflushTrend: function (dataList){
            //清空原来的数据
            modalChartObj.cust.trendOption.xAxis[0].data = [];
            modalChartObj.cust.trendOption.series[0].data = [];
            modalChartObj.cust.trendOption.series[1].data = [];
            modalChartObj.cust.trendOption.series[2].data = [];
            modalChartObj.cust.trendOption.series[3].data = [];
            modalChartObj.cust.trendOption.series[4].data = [];
            //数据封装
            var timeUnit = $("#modalTimeQuantum").val() == 1 ? "日" : "月";
            for (var i = 0; i < dataList.length; i++) {
                modalChartObj.cust.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
                modalChartObj.cust.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
                modalChartObj.cust.trendOption.series[1].data[i] = dataList[i].saleAmount || 0;
                modalChartObj.cust.trendOption.series[2].data[i] = dataList[i].payAmount || 0;
                modalChartObj.cust.trendOption.series[3].data[i] = dataList[i].noIncomeAmount || 0;
                modalChartObj.cust.trendOption.series[4].data[i] = dataList[i].profit || 0;
            }
            //加载
            modalChartObj.cust.loadTrend();
        },
        reflushPie: function (dataMap) {
            modalChartObj.cust.pieOption.legend.data = [];//清空原数据
            modalChartObj.cust.pieOption.series[0].data = [];//清空原数据
            //设置数据
            for (var i = 0; i < dataMap.length; i++) {
                modalChartObj.cust.pieOption.legend.data[i] = dataMap[i].mediaTypeName;
                var serie1 = {name:dataMap[i].mediaTypeName,value:dataMap[i].saleAmount,mediaTypeId:dataMap[i].mediaTypeId};//数据
                modalChartObj.cust.pieOption.series[0].data[i] = serie1;
            }

            //加载饼图
            modalChartObj.cust.loadPie();
        },
        loadTrend: function () { //加载趋势图
            setTimeout(function () {
                modalChartObj.cust.initTrendChart();
                modalChartObj.cust.chart.trend.setOption(modalChartObj.cust.trendOption);
            },0);
        },
        loadPie: function () { //加载饼图
            setTimeout(function () {
                modalChartObj.cust.initPieChart();
                var num = modalChartObj.cust.pieOption.legend.data.length;//图例数量
                var height = 14;//默认高度14
                if(num > 12){//当图例数量大于15时。图例会分成多行，进行单列算法计算
                    var itemCap = modalChartObj.cust.pieOption.legend.itemGap;//图例间隔
                    var padding =  modalChartObj.cust.pieOption.legend.padding;//图例内边距
                    var eleHeight = $("#modalCustChartByMediaType").innerHeight();//div总高度 = 图例高度和，计算公式：eleHeight = padding * 2 + itemCap*(num-1)+heigth*num 向下取整
                    height = Math.floor((eleHeight-(padding*2 + itemCap*(num-1)))/num);
                }
                modalChartObj.cust.pieOption.legend.itemHeight = height;
                modalChartObj.cust.chart.pie.setOption(modalChartObj.cust.pieOption);
            },0);
        }
    },
    //业务员维度：business
    business:{
        chart:{},  //图表对象
        trendOption:{
            "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E"],
            "backgroundColor": "#fff",
            "legend": {"data": ["稿件数量", "报价金额", "成本金额", "未到账金额","利润"]},
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
                boundaryGap : false,
                "data": []
            }],
            "yAxis": [
                {
                    "type": "value",
                    name: '(单位：件、元、元、元、元)'
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
                },
                {
                    "name": "报价金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "成本金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "未到账金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "利润",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                }
            ]
        }, //趋势图表配置
        pieOption: {
            "title": {
                text:"板块占比",
                subtext:"报价金额",
                textStyle:{
                    color: '#009688',
                    fontSize: '13'
                },
                subtextStyle:{
                    fontSize: '10'
                },
                x:"center"
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
                text:'板块占比【报价金额】暂无数据',
                textStyle:{
                    fontSize : 14
                }
            },
            tooltip: {
                trigger: 'item',
                position:function(p){
                    var id = document.getElementById('modalBusinessChartByMediaType');
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
                    "name": "板块占比",
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
                                    fontSize : '10',
                                    fontWeight : 'bold'
                                }
                            }
                        }
                    }
                }]
        },  //板块占比
        initTrendChart: function () { //初始化趋势图
            modalChartObj.business.chart.trend = echarts.init(document.getElementById('modalBusinessChartByDate'));
        },
        initPieChart: function () { //初始化板块占比图
            modalChartObj.business.chart.pie = echarts.init(document.getElementById('modalBusinessChartByMediaType'));
            //点击事件
            modalChartObj.business.chart.pie.on('click', function (params) {
                if(statisticsModal.config.currentUserDept == 'MJ' || statisticsModal.config.currentUserDept == 'GL' || statisticsModal.config.currentUserDept == 'CW'){
                    var jumpUrl = statisticsModal.config.statisticsUrl.mediaType+"?mediaTypeId="+params.data.mediaTypeId+"&mediaTypeName="+params.data.name;
                    page(jumpUrl,statisticsModal.config.statisticsName.mediaType);
                }
            });
        },
        reflushTrend: function (dataList){
            //清空原来的数据
            modalChartObj.business.trendOption.xAxis[0].data = [];
            modalChartObj.business.trendOption.series[0].data = [];
            modalChartObj.business.trendOption.series[1].data = [];
            modalChartObj.business.trendOption.series[2].data = [];
            modalChartObj.business.trendOption.series[3].data = [];
            modalChartObj.business.trendOption.series[4].data = [];
            //数据封装
            var timeUnit = $("#modalTimeQuantum").val() == 1 ? "日" : "月";
            for (var i = 0; i < dataList.length; i++) {
                modalChartObj.business.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
                modalChartObj.business.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
                modalChartObj.business.trendOption.series[1].data[i] = dataList[i].saleAmount || 0;
                modalChartObj.business.trendOption.series[2].data[i] = dataList[i].payAmount || 0;
                modalChartObj.business.trendOption.series[3].data[i] = dataList[i].noIncomeAmount || 0;
                modalChartObj.business.trendOption.series[4].data[i] = dataList[i].profit || 0;
            }
            //加载
            modalChartObj.business.loadTrend();
        },
        reflushPie: function (dataMap) {
            modalChartObj.business.pieOption.legend.data = [];//清空原数据
            modalChartObj.business.pieOption.series[0].data = [];//清空原数据
            //设置数据
            for (var i = 0; i < dataMap.length; i++) {
                modalChartObj.business.pieOption.legend.data[i] = dataMap[i].mediaTypeName;
                var serie1 = {name:dataMap[i].mediaTypeName,value:dataMap[i].saleAmount,mediaTypeId:dataMap[i].mediaTypeId};//数据
                modalChartObj.business.pieOption.series[0].data[i] = serie1;
            }

            //加载饼图
            modalChartObj.business.loadPie();
        },
        loadTrend: function () { //加载趋势图
            setTimeout(function () {
                modalChartObj.business.initTrendChart();
                modalChartObj.business.chart.trend.setOption(modalChartObj.business.trendOption);
            },0);
        },
        loadPie: function () { //加载饼图
            setTimeout(function () {
                modalChartObj.business.initPieChart();
                var num = modalChartObj.business.pieOption.legend.data.length;//图例数量
                var height = 14;//默认高度14
                if(num > 12){//当图例数量大于15时。图例会分成多行，进行单列算法计算
                    var itemCap = modalChartObj.business.pieOption.legend.itemGap;//图例间隔
                    var padding =  modalChartObj.business.pieOption.legend.padding;//图例内边距
                    var eleHeight = $("#modalBusinessChartByMediaType").innerHeight();//div总高度 = 图例高度和，计算公式：eleHeight = padding * 2 + itemCap*(num-1)+heigth*num 向下取整
                    height = Math.floor((eleHeight-(padding*2 + itemCap*(num-1)))/num);
                }
                modalChartObj.business.pieOption.legend.itemHeight = height;
                modalChartObj.business.chart.pie.setOption(modalChartObj.business.pieOption);
            },0);
        }
    },
    //媒介维度：mediaUser
    mediaUser:{
        chart:{},  //图表对象
        trendOption:{
            "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E"],
            "backgroundColor": "#fff",
            "legend": {"data": ["稿件数量", "报价金额", "成本金额", "未到账金额","利润"]},
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
                boundaryGap : false,
                "data": []
            }],
            "yAxis": [
                {
                    "type": "value",
                    name: '(单位：件、元、元、元、元)'
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
                },
                {
                    "name": "报价金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "成本金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "未到账金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "利润",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                }
            ]
        }, //趋势图表配置
        pieOption: {
            "title": {
                text:"板块占比",
                subtext:"报价金额",
                textStyle:{
                    color: '#009688',
                    fontSize: '13'
                },
                subtextStyle:{
                    fontSize: '10'
                },
                x:"center"
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
                text:'板块占比【报价金额】暂无数据',
                textStyle:{
                    fontSize : 14
                }
            },
            tooltip: {
                trigger: 'item',
                position:function(p){
                    var id = document.getElementById('modalMediaUserChartByMediaType');
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
                    "name": "板块占比",
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
                                    fontSize : '10',
                                    fontWeight : 'bold'
                                }
                            }
                        }
                    }
                }]
        },  //板块占比
        initTrendChart: function () { //初始化趋势图
            modalChartObj.mediaUser.chart.trend = echarts.init(document.getElementById('modalMediaUserChartByDate'));
        },
        initPieChart: function () { //初始化板块占比图
            modalChartObj.mediaUser.chart.pie = echarts.init(document.getElementById('modalMediaUserChartByMediaType'));
            //点击事件
            modalChartObj.mediaUser.chart.pie.on('click', function (params) {
                if(statisticsModal.config.currentUserDept == 'MJ' || statisticsModal.config.currentUserDept == 'GL' || statisticsModal.config.currentUserDept == 'CW'){
                    var jumpUrl = statisticsModal.config.statisticsUrl.mediaType+"?mediaTypeId="+params.data.mediaTypeId+"&mediaTypeName="+params.data.name;
                    page(jumpUrl,statisticsModal.config.statisticsName.mediaType);
                }
            });
        },
        reflushTrend: function (dataList){
            //清空原来的数据
            modalChartObj.mediaUser.trendOption.xAxis[0].data = [];
            modalChartObj.mediaUser.trendOption.series[0].data = [];
            modalChartObj.mediaUser.trendOption.series[1].data = [];
            modalChartObj.mediaUser.trendOption.series[2].data = [];
            modalChartObj.mediaUser.trendOption.series[3].data = [];
            modalChartObj.mediaUser.trendOption.series[4].data = [];
            //数据封装
            var timeUnit = $("#modalTimeQuantum").val() == 1 ? "日" : "月";
            for (var i = 0; i < dataList.length; i++) {
                modalChartObj.mediaUser.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
                modalChartObj.mediaUser.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
                modalChartObj.mediaUser.trendOption.series[1].data[i] = dataList[i].saleAmount || 0;
                modalChartObj.mediaUser.trendOption.series[2].data[i] = dataList[i].payAmount || 0;
                modalChartObj.mediaUser.trendOption.series[3].data[i] = dataList[i].noIncomeAmount || 0;
                modalChartObj.mediaUser.trendOption.series[4].data[i] = dataList[i].profit || 0;
            }
            //加载
            modalChartObj.mediaUser.loadTrend();
        },
        reflushPie: function (dataMap) {
            modalChartObj.mediaUser.pieOption.legend.data = [];//清空原数据
            modalChartObj.mediaUser.pieOption.series[0].data = [];//清空原数据
            //设置数据
            for (var i = 0; i < dataMap.length; i++) {
                modalChartObj.mediaUser.pieOption.legend.data[i] = dataMap[i].mediaTypeName;
                var serie1 = {name:dataMap[i].mediaTypeName,value:dataMap[i].saleAmount,mediaTypeId:dataMap[i].mediaTypeId};//数据
                modalChartObj.mediaUser.pieOption.series[0].data[i] = serie1;
            }

            //加载饼图
            modalChartObj.mediaUser.loadPie();
        },
        loadTrend: function () { //加载趋势图
            setTimeout(function () {
                modalChartObj.mediaUser.initTrendChart();
                modalChartObj.mediaUser.chart.trend.setOption(modalChartObj.mediaUser.trendOption);
            },0);
        },
        loadPie: function () { //加载饼图
            setTimeout(function () {
                modalChartObj.mediaUser.initPieChart();
                var num = modalChartObj.mediaUser.pieOption.legend.data.length;//图例数量
                var height = 14;//默认高度14
                if(num > 12){//当图例数量大于15时。图例会分成多行，进行单列算法计算
                    var itemCap = modalChartObj.mediaUser.pieOption.legend.itemGap;//图例间隔
                    var padding =  modalChartObj.mediaUser.pieOption.legend.padding;//图例内边距
                    var eleHeight = $("#modalMediaUserChartByMediaType").innerHeight();//div总高度 = 图例高度和，计算公式：eleHeight = padding * 2 + itemCap*(num-1)+heigth*num 向下取整
                    height = Math.floor((eleHeight-(padding*2 + itemCap*(num-1)))/num);
                }
                modalChartObj.mediaUser.pieOption.legend.itemHeight = height;
                modalChartObj.mediaUser.chart.pie.setOption(modalChartObj.mediaUser.pieOption);
            },0);
        }
    },
    //板块维度：mediaType
    mediaType:{
        chart:{},  //图表对象
        trendOption:{
            "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E"],
            "backgroundColor": "#fff",
            "legend": {"data": ["稿件数量", "报价金额", "成本金额", "未到账金额","利润"]},
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
                boundaryGap : false,
                "data": []
            }],
            "yAxis": [
                {
                    "type": "value",
                    name: '(单位：件、元、元、元、元)'
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
                },
                {
                    "name": "报价金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "成本金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "未到账金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "利润",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                }
            ]
        }, //趋势图表配置
        initTrendChart: function () { //初始化趋势图
            modalChartObj.mediaType.chart.trend = echarts.init(document.getElementById('modalMediaTypeChartByDate'));
        },
        reflushTrend: function (dataList){
            //清空原来的数据
            modalChartObj.mediaType.trendOption.xAxis[0].data = [];
            modalChartObj.mediaType.trendOption.series[0].data = [];
            modalChartObj.mediaType.trendOption.series[1].data = [];
            modalChartObj.mediaType.trendOption.series[2].data = [];
            modalChartObj.mediaType.trendOption.series[3].data = [];
            modalChartObj.mediaType.trendOption.series[4].data = [];
            //数据封装
            var timeUnit = $("#modalTimeQuantum").val() == 1 ? "日" : "月";
            for (var i = 0; i < dataList.length; i++) {
                modalChartObj.mediaType.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
                modalChartObj.mediaType.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
                modalChartObj.mediaType.trendOption.series[1].data[i] = dataList[i].saleAmount || 0;
                modalChartObj.mediaType.trendOption.series[2].data[i] = dataList[i].payAmount || 0;
                modalChartObj.mediaType.trendOption.series[3].data[i] = dataList[i].noIncomeAmount || 0;
                modalChartObj.mediaType.trendOption.series[4].data[i] = dataList[i].profit || 0;
            }
            //加载
            modalChartObj.mediaType.loadTrend();
        },
        loadTrend: function () { //加载趋势图
            setTimeout(function () {
                modalChartObj.mediaType.initTrendChart();
                modalChartObj.mediaType.chart.trend.setOption(modalChartObj.mediaType.trendOption);
            },0);
        }
    },
    //媒体维度：media
    media:{
        chart:{},  //图表对象
        trendOption:{
            "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E"],
            "backgroundColor": "#fff",
            "legend": {"data": ["稿件数量", "报价金额", "成本金额", "未到账金额","利润"]},
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
                boundaryGap : false,
                "data": []
            }],
            "yAxis": [
                {
                    "type": "value",
                    name: '(单位：件、元、元、元、元)'
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
                },
                {
                    "name": "报价金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "成本金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "未到账金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "利润",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                }
            ]
        }, //趋势图表配置
        initTrendChart: function () { //初始化趋势图
            modalChartObj.media.chart.trend = echarts.init(document.getElementById('modalMediaChartByDate'));
        },
        reflushTrend: function (dataList){
            //清空原来的数据
            modalChartObj.media.trendOption.xAxis[0].data = [];
            modalChartObj.media.trendOption.series[0].data = [];
            modalChartObj.media.trendOption.series[1].data = [];
            modalChartObj.media.trendOption.series[2].data = [];
            modalChartObj.media.trendOption.series[3].data = [];
            modalChartObj.media.trendOption.series[4].data = [];
            //数据封装
            var timeUnit = $("#modalTimeQuantum").val() == 1 ? "日" : "月";
            for (var i = 0; i < dataList.length; i++) {
                modalChartObj.media.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
                modalChartObj.media.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
                modalChartObj.media.trendOption.series[1].data[i] = dataList[i].saleAmount || 0;
                modalChartObj.media.trendOption.series[2].data[i] = dataList[i].payAmount || 0;
                modalChartObj.media.trendOption.series[3].data[i] = dataList[i].noIncomeAmount || 0;
                modalChartObj.media.trendOption.series[4].data[i] = dataList[i].profit || 0;
            }
            //加载
            modalChartObj.media.loadTrend();
        },
        loadTrend: function () { //加载趋势图
            setTimeout(function () {
                modalChartObj.media.initTrendChart();
                modalChartObj.media.chart.trend.setOption(modalChartObj.media.trendOption);
            },0);
        }
    },
    //供应商维度：supplier
    supplier:{
        chart:{},  //图表对象
        trendOption:{
            "color": ["#EE7383", "#72C7D9", "#FFB148","#A5D16F","#2FA82E"],
            "backgroundColor": "#fff",
            "legend": {"data": ["稿件数量", "报价金额", "成本金额", "未到账金额","利润"]},
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
                boundaryGap : false,
                "data": []
            }],
            "yAxis": [
                {
                    "type": "value",
                    name: '(单位：件、元、元、元、元)'
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
                },
                {
                    "name": "报价金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "成本金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "未到账金额",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                },
                {
                    "name": "利润",
                    type: 'line',
                    // stack:"总量",
                    smooth: true,
                    itemStyle: {normal: {areaStyle: {type: 'default'}}},
                    "data": []
                }
            ]
        }, //趋势图表配置
        pieOption: {
            "title": {
                text:"板块占比",
                subtext:"报价金额",
                textStyle:{
                    color: '#009688',
                    fontSize: '13'
                },
                subtextStyle:{
                    fontSize: '10'
                },
                x:"center"
            },
            "backgroundColor": "#fff",
            noDataLoadingOption:{
                effect:'bar',
                text:'板块占比【报价金额】暂无数据',
                textStyle:{
                    fontSize : 14
                }
            },
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
                    var id = document.getElementById('modalSupplierChartByMediaType');
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
                    "name": "板块占比",
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
                                    fontSize : '10',
                                    fontWeight : 'bold'
                                }
                            }
                        }
                    }
                }]
        },  //板块占比
        initTrendChart: function () { //初始化趋势图
            modalChartObj.supplier.chart.trend = echarts.init(document.getElementById('modalSupplierChartByDate'));
        },
        initPieChart: function () { //初始化板块占比图
            modalChartObj.supplier.chart.pie = echarts.init(document.getElementById('modalSupplierChartByMediaType'));
            //点击事件
            modalChartObj.supplier.chart.pie.on('click', function (params) {
                if(statisticsModal.config.currentUserDept == 'MJ' || statisticsModal.config.currentUserDept == 'GL' || statisticsModal.config.currentUserDept == 'CW'){
                    var jumpUrl = statisticsModal.config.statisticsUrl.mediaType+"?mediaTypeId="+params.data.mediaTypeId+"&mediaTypeName="+params.data.name;
                    page(jumpUrl,statisticsModal.config.statisticsName.mediaType);
                }
            });
        },
        reflushTrend: function (dataList){
            //清空原来的数据
            modalChartObj.supplier.trendOption.xAxis[0].data = [];
            modalChartObj.supplier.trendOption.series[0].data = [];
            modalChartObj.supplier.trendOption.series[1].data = [];
            modalChartObj.supplier.trendOption.series[2].data = [];
            modalChartObj.supplier.trendOption.series[3].data = [];
            modalChartObj.supplier.trendOption.series[4].data = [];
            //数据封装
            var timeUnit = $("#modalTimeQuantum").val() == 1 ? "日" : "月";
            for (var i = 0; i < dataList.length; i++) {
                modalChartObj.supplier.trendOption.xAxis[0].data[i] = dataList[i].time ? dataList[i].time + timeUnit : "";
                modalChartObj.supplier.trendOption.series[0].data[i] = dataList[i].articleNum || 0;
                modalChartObj.supplier.trendOption.series[1].data[i] = dataList[i].saleAmount || 0;
                modalChartObj.supplier.trendOption.series[2].data[i] = dataList[i].payAmount || 0;
                modalChartObj.supplier.trendOption.series[3].data[i] = dataList[i].noIncomeAmount || 0;
                modalChartObj.supplier.trendOption.series[4].data[i] = dataList[i].profit || 0;
            }
            //加载
            modalChartObj.supplier.loadTrend();
        },
        reflushPie: function (dataMap) {
            modalChartObj.supplier.pieOption.legend.data = [];//清空原数据
            modalChartObj.supplier.pieOption.series[0].data = [];//清空原数据
            //设置数据
            for (var i = 0; i < dataMap.length; i++) {
                modalChartObj.supplier.pieOption.legend.data[i] = dataMap[i].mediaTypeName;
                var serie1 = {name:dataMap[i].mediaTypeName,value:dataMap[i].saleAmount,mediaTypeId:dataMap[i].mediaTypeId};//数据
                modalChartObj.supplier.pieOption.series[0].data[i] = serie1;
            }

            //加载饼图
            modalChartObj.supplier.loadPie();
        },
        loadTrend: function () { //加载趋势图
            setTimeout(function () {
                modalChartObj.supplier.initTrendChart();
                modalChartObj.supplier.chart.trend.setOption(modalChartObj.supplier.trendOption);
            },0);
        },
        loadPie: function () { //加载饼图
            setTimeout(function () {
                modalChartObj.supplier.initPieChart();
                var num = modalChartObj.supplier.pieOption.legend.data.length;//图例数量
                var height = 14;//默认高度14
                if(num > 12){//当图例数量大于15时。图例会分成多行，进行单列算法计算
                    var itemCap = modalChartObj.supplier.pieOption.legend.itemGap;//图例间隔
                    var padding =  modalChartObj.supplier.pieOption.legend.padding;//图例内边距
                    var eleHeight = $("#modalSupplierChartByMediaType").innerHeight();//div总高度 = 图例高度和，计算公式：eleHeight = padding * 2 + itemCap*(num-1)+heigth*num 向下取整
                    height = Math.floor((eleHeight-(padding*2 + itemCap*(num-1)))/num);
                }
                modalChartObj.supplier.pieOption.legend.itemHeight = height;
                modalChartObj.supplier.chart.pie.setOption(modalChartObj.supplier.pieOption);
            },0);
        }
    }
};

//页面表格
var modalTableListObj = {
    cust:{
        grid:{},
        table:{
            url: baseUrl + '/statisticsOverview/listMediaStatisticsByCustId',
            postData: $("#paramForm").serializeJson(),
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
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 50, //每页记录数
            rowList: [10,20, 50,100],//每页记录数可选列表
            colNames: ['媒体ID','媒体名称','板块ID','板块名称', '稿件数量', '报价金额', '成本金额', '未到款金额','利润','业务员','媒介','供应商'],//表头
            jsonReader: {//server返回Json解析设定
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: false
            },
            colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
                {
                    name: 'mediaId',
                    index: 'mediaId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'mediaName',
                    index: 'mediaName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getSingleLinkHtml(rows.mediaId,rows.mediaName,"media");
                    }
                },
                {
                    name: 'mediaTypeId',
                    index: 'mediaTypeId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: false,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'mediaTypeName',
                    index: 'mediaTypeName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getSingleLinkHtml(rows.mediaTypeId,rows.mediaTypeName,"mediaType");
                    }
                },
                {
                    name: 'articleNum',
                    index: 'articleNum',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string"
                },
                {
                    name: 'saleAmount',
                    index: 'saleAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'payAmount',
                    index: 'payAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'noIncomeAmount',
                    index: 'noIncomeAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'profit',
                    index: 'profit',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'businessUser',
                    index: 'businessUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"business");
                    }
                },
                {
                    name: 'mediaUser',
                    index: 'mediaUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"mediaUser");
                    }
                },
                {
                    name: 'supplierUser',
                    index: 'supplierUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'YW'){
                            if(value){
                                return "****";
                            }else{
                                return "";
                            }
                        }else {
                            return modalTableListObj.getMultipleLinkHtml(value,"supplier");
                        }
                    }
                }
            ],
            pager: "#modalCustPager",
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
                modalTableListObj.cust.grid = new dataGrid("modalCustTable", modalTableListObj.cust.table, "modalCustPager", "paramForm");
                modalTableListObj.cust.grid.loadGrid();
                modalTableListObj.cust.grid.setNavGrid();
                modalTableListObj.cust.grid.defaultParams = {};//清空历史表单数据
                modalTableListObj.cust.grid.search();
            },0);

        }
    },
    business:{
        grid:{},
        table:{
            url: baseUrl + '/statisticsOverview/listCustStatisticsBybusinessId',
            postData: $("#paramForm").serializeJson(),
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
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 50, //每页记录数
            rowList: [10,20, 50,100],//每页记录数可选列表
            colNames: ['客户ID','客户名称','客户公司ID','客户公司名称', '稿件数量', '报价金额', '成本金额', '未到款金额','利润','板块名称','媒体名称','媒介','供应商'],//表头
            jsonReader: {//server返回Json解析设定
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: false
            },
            colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
                {
                    name: 'custId',
                    index: 'custId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'custName',
                    index: 'custName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                         var useId= $("#paramForm").serializeJson()["businessUserId"];
                        if (useId == user.id){
                            if(statisticsModal.config.currentUserDept == 'MJ'){
                                if(value){
                                    value =  value.substr(0,1)+"****";
                                }
                                return value || "";
                            }else {
                                return modalTableListObj.getSingleLinkHtml(rows.custId,rows.custName,"cust");
                            }
                        }else {
                            if (XMflag){
                                return "****";
                            }else {
                                if(statisticsModal.config.currentUserDept == 'MJ'){
                                    if(value){
                                        value =  value.substr(0,1)+"****";
                                    }
                                    return value || "";
                                }else {
                                    return modalTableListObj.getSingleLinkHtml(rows.custId,rows.custName,"cust");
                                }
                            }

                        }

                    }
                },
                {
                    name: 'custCompanyId',
                    index: 'custCompanyId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'custCompanyName',
                    index: 'custCompanyName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'MJ'){
                            if(value){
                                value =  value.substr(0,1)+"****";
                            }
                            return value || "";
                        }else {
                            return value || "";
                        }
                    }
                },
                {
                    name: 'articleNum',
                    index: 'articleNum',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string"
                },
                {
                    name: 'saleAmount',
                    index: 'saleAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'payAmount',
                    index: 'payAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'noIncomeAmount',
                    index: 'noIncomeAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'profit',
                    index: 'profit',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'mediaType',
                    index: 'mediaType',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"mediaType");
                    }
                },
                {
                    name: 'media',
                    index: 'media',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"media");
                    }
                },
                {
                    name: 'mediaUser',
                    index: 'mediaUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"mediaUser");
                    }
                },
                {
                    name: 'supplierUser',
                    index: 'supplierUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'YW'){
                            if(value){
                                return "****";
                            }else{
                                return "";
                            }
                        }else {
                            return modalTableListObj.getMultipleLinkHtml(value,"supplier");
                        }
                    }
                }
            ],
            pager: "#modalBusinessPager",
            viewrecords: true,
            caption: "客户详情",
            add: true,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false
        },
        init:function () {
            //板块列表排名
            setTimeout(function () {
                modalTableListObj.business.grid = new dataGrid("modalBusinessTable", modalTableListObj.business.table, "modalBusinessPager", "paramForm");
                modalTableListObj.business.grid.loadGrid();
                modalTableListObj.business.grid.setNavGrid();
                modalTableListObj.business.grid.defaultParams = {};//清空历史表单数据
                modalTableListObj.business.grid.search();
            },0);

        }
    },
    mediaUser:{
        grid:{},
        table:{
            url: baseUrl + '/statisticsOverview/listMediaStatisticsByMediaUserId',
            postData: $("#paramForm").serializeJson(),
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
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 50, //每页记录数
            rowList: [10,20, 50,100],//每页记录数可选列表
            colNames: ['媒体ID','媒体名称','板块ID','板块名称', '稿件数量', '报价金额', '成本金额', '未到款金额','利润','业务员','客户','供应商'],//表头
            jsonReader: {//server返回Json解析设定
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: false
            },
            colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
                {
                    name: 'mediaId',
                    index: 'mediaId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'mediaName',
                    index: 'mediaName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getSingleLinkHtml(rows.mediaId,rows.mediaName,"media");
                    }
                },
                {
                    name: 'mediaTypeId',
                    index: 'mediaTypeId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: false,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'mediaTypeName',
                    index: 'mediaTypeName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getSingleLinkHtml(rows.mediaTypeId,rows.mediaTypeName,"mediaType");
                    }
                },
                {
                    name: 'articleNum',
                    index: 'articleNum',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string"
                },
                {
                    name: 'saleAmount',
                    index: 'saleAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'payAmount',
                    index: 'payAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'noIncomeAmount',
                    index: 'noIncomeAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'profit',
                    index: 'profit',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'businessUser',
                    index: 'businessUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"business");
                    }
                },
                {
                    name: 'cust',
                    index: 'cust',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'MJ'){
                            if(value){
                                return "****";
                            }else{
                                return "";
                            }
                        }else {
                            return modalTableListObj.getMultipleLinkHtml(value,"cust");
                        }
                    }
                },
                {
                    name: 'supplierUser',
                    index: 'supplierUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'YW'){
                            if(value){
                                return "****";
                            }else{
                                return "";
                            }
                        }else {
                            return modalTableListObj.getMultipleLinkHtml(value,"supplier");
                        }
                    }
                }
            ],
            pager: "#modalMediaUserPager",
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
                modalTableListObj.mediaUser.grid = new dataGrid("modalMediaUserTable", modalTableListObj.mediaUser.table, "modalMediaUserPager", "paramForm");
                modalTableListObj.mediaUser.grid.loadGrid();
                modalTableListObj.mediaUser.grid.setNavGrid();
                modalTableListObj.mediaUser.grid.defaultParams = {};//清空历史表单数据
                modalTableListObj.mediaUser.grid.search();
            },0);

        }
    },
    mediaType:{
        grid:{},
        table:{
            url: baseUrl + '/statisticsOverview/listMediaStatisticsByMediaTypeId',
            postData: $("#paramForm").serializeJson(),
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
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 50, //每页记录数
            rowList: [10,20, 50,100],//每页记录数可选列表
            colNames: ['媒体ID','媒体名称', '稿件数量', '报价金额', '成本金额', '未到款金额','利润','业务员','媒介','客户','供应商'],//表头
            jsonReader: {//server返回Json解析设定
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: false
            },
            colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
                {
                    name: 'mediaId',
                    index: 'mediaId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'mediaName',
                    index: 'mediaName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getSingleLinkHtml(rows.mediaId,rows.mediaName,"media");
                    }
                },
                {
                    name: 'articleNum',
                    index: 'articleNum',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string"
                },
                {
                    name: 'saleAmount',
                    index: 'saleAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'payAmount',
                    index: 'payAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'noIncomeAmount',
                    index: 'noIncomeAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'profit',
                    index: 'profit',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'businessUser',
                    index: 'businessUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"business");
                    }
                },
                {
                    name: 'mediaUser',
                    index: 'mediaUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"mediaUser");
                    }
                },
                {
                    name: 'cust',
                    index: 'cust',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if (XMflag){
                            return "****";
                        }else {
                            if(statisticsModal.config.currentUserDept == 'MJ'){
                                if(value){
                                    return "****";
                                }else{
                                    return "";
                                }
                            }else {
                                return modalTableListObj.getMultipleLinkHtml(value,"cust");
                            }
                        }


                    }
                },
                {
                    name: 'supplierUser',
                    index: 'supplierUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'YW'){
                            if(value){
                                return "****";
                            }else{
                                return "";
                            }
                        }else {
                            return modalTableListObj.getMultipleLinkHtml(value,"supplier");
                        }
                    }
                }
            ],
            pager: "#modalMediaTypePager",
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
                modalTableListObj.mediaType.grid = new dataGrid("modalMediaTypeTable", modalTableListObj.mediaType.table, "modalMediaTypePager", "paramForm");
                modalTableListObj.mediaType.grid.loadGrid();
                modalTableListObj.mediaType.grid.setNavGrid();
                modalTableListObj.mediaType.grid.defaultParams = {};//清空历史表单数据
                modalTableListObj.mediaType.grid.search();
            },0);

        }
    },
    media:{
        grid:{},
        table:{
            url: baseUrl + '/statisticsOverview/listCustStatisticsByMediaId',
            postData: $("#paramForm").serializeJson(),
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
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 50, //每页记录数
            rowList: [10,20, 50,100],//每页记录数可选列表
            colNames: ['客户ID','客户名称','客户公司ID','客户公司名称', '稿件数量', '报价金额', '成本金额', '未到款金额','利润','板块名称','业务员','媒介','供应商'],//表头
            jsonReader: {//server返回Json解析设定
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: false
            },
            colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
                {
                    name: 'custId',
                    index: 'custId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'custName',
                    index: 'custName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'MJ'){
                            if(value){
                                value =  value.substr(0,1)+"****";
                            }
                            return value || "";
                        }else {
                            return modalTableListObj.getSingleLinkHtml(rows.custId,rows.custName,"cust");
                        }
                    }
                },
                {
                    name: 'custCompanyId',
                    index: 'custCompanyId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'custCompanyName',
                    index: 'custCompanyName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'MJ'){
                            if(value){
                                value =  value.substr(0,1)+"****";
                            }
                            return value || "";
                        }else {
                            return value || "";
                        }
                    }
                },
                {
                    name: 'articleNum',
                    index: 'articleNum',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string"
                },
                {
                    name: 'saleAmount',
                    index: 'saleAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'payAmount',
                    index: 'payAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'noIncomeAmount',
                    index: 'noIncomeAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'profit',
                    index: 'profit',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'mediaType',
                    index: 'mediaType',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"mediaType");
                    }
                },
                {
                    name: 'businessUser',
                    index: 'businessUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"business");
                    }
                },
                {
                    name: 'mediaUser',
                    index: 'mediaUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"mediaUser");
                    }
                },
                {
                    name: 'supplierUser',
                    index: 'supplierUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'YW'){
                            if(value){
                                return "****";
                            }else{
                                return "";
                            }
                        }else {
                            return modalTableListObj.getMultipleLinkHtml(value,"supplier");
                        }
                    }
                }
            ],
            pager: "#modalMediaPager",
            viewrecords: true,
            caption: "客户详情",
            add: true,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false
        },
        init:function () {
            //板块列表排名
            setTimeout(function () {
                modalTableListObj.media.grid = new dataGrid("modalMediaTable", modalTableListObj.media.table, "modalMediaPager", "paramForm");
                modalTableListObj.media.grid.loadGrid();
                modalTableListObj.media.grid.setNavGrid();
                modalTableListObj.media.grid.defaultParams = {};//清空历史表单数据
                modalTableListObj.media.grid.search();
            },0);

        }
    },
    supplier:{
        grid:{},
        table:{
            url: baseUrl + '/statisticsOverview/listMediaStatisticsBySupplierId',
            postData: $("#paramForm").serializeJson(),
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
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 50, //每页记录数
            rowList: [10,20, 50,100],//每页记录数可选列表
            colNames: ['媒体ID','媒体名称','板块ID','板块名称', '稿件数量', '报价金额', '成本金额', '未到款金额','利润','业务员','媒介','客户'],//表头
            jsonReader: {//server返回Json解析设定
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: false
            },
            colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
                {
                    name: 'mediaId',
                    index: 'mediaId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'mediaName',
                    index: 'mediaName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getSingleLinkHtml(rows.mediaId,rows.mediaName,"media");
                    }
                },
                {
                    name: 'mediaTypeId',
                    index: 'mediaTypeId',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: false,
                    hidden:true,
                    sorttype: "string"
                },
                {
                    name: 'mediaTypeName',
                    index: 'mediaTypeName',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string",
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getSingleLinkHtml(rows.mediaTypeId,rows.mediaTypeName,"mediaType");
                    }
                },
                {
                    name: 'articleNum',
                    index: 'articleNum',
                    editable: false,
                    align: "center",
                    width:100,
                    sortable: true,
                    sorttype: "string"
                },
                {
                    name: 'saleAmount',
                    index: 'saleAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'payAmount',
                    index: 'payAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'noIncomeAmount',
                    index: 'noIncomeAmount',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'profit',
                    index: 'profit',
                    editable: false,
                    width:100,
                    align: "center",
                    sortable: true
                },
                {
                    name: 'businessUser',
                    index: 'businessUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"business");
                    }
                },
                {
                    name: 'mediaUser',
                    index: 'mediaUser',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        return modalTableListObj.getMultipleLinkHtml(value,"mediaUser");
                    }
                },
                {
                    name: 'cust',
                    index: 'cust',
                    editable: false,
                    width:150,
                    align: "center",
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if(statisticsModal.config.currentUserDept == 'MJ'){
                            if(value){
                                return "****";
                            }else{
                                return "";
                            }
                        }else {
                            return modalTableListObj.getMultipleLinkHtml(value,"cust");
                        }
                    }
                }
            ],
            pager: "#modalSupplierPager",
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
                modalTableListObj.supplier.grid = new dataGrid("modalSupplierTable", modalTableListObj.supplier.table, "modalSupplierPager", "paramForm");
                modalTableListObj.supplier.grid.loadGrid();
                modalTableListObj.supplier.grid.setNavGrid();
                modalTableListObj.supplier.grid.defaultParams = {};//清空历史表单数据
                modalTableListObj.supplier.grid.search();
            },0);

        }
    },
    getMultipleLinkHtml: function (str, type) { //获取多个a链接
        var html = "";
        if(str){
            var strArr = str.split(",");//分割链接信息
            $.each(strArr,function (index, obj) {
                if(index != 0 && index % 2 === 0){ //两个名称为一组
                    html += "<br/>";
                }
                var index = obj.indexOf('->');
                if(index != -1){
                    var objArr = obj.split('->');
                    var id = objArr[0];
                    var name = objArr[1];
                    var companyCode = "";
                    if(objArr && objArr.length == 3){
                        companyCode = objArr[2];
                    }
                    name = name || id;
                    if(statisticsModal.config.currentUserDept == 'MJ'){ //媒介可以跳转媒介、板块、媒体、供应商等统计
                        if(type == 'mediaUser' || type == 'mediaType' || type == 'media' || type == 'supplier'){
                            if(type != 'supplier' || (type == 'supplier' && companyCode == statisticsModal.config.currentUserCompany)){ //供应商特殊处理，仅展示自己公司的
                                var jumpUrl = statisticsModal.config.statisticsUrl[type]+"?"+type+"Id="+id+"&"+type+"Name="+name;
                                html += "<a title='"+name+"' style='margin-right: 5px;' href=\"javascript:page('"+jumpUrl+"','"+statisticsModal.config.statisticsName[type]+"');\">"+name+"</a>";
                            }else{
                                if(!html){
                                    html += "<span style='margin-right: 5px;' title=''>****</span>";
                                }
                            }
                        }else{
                            html += "<span style='margin-right: 5px;' title=\""+name+"\">"+name+"</span>";
                        }
                    }else if(statisticsModal.config.currentUserDept == 'YW'){ // 业务可以跳转业务、客户等统计
                        if(type == 'cust' || type == 'business'){
                            var jumpUrl = statisticsModal.config.statisticsUrl[type]+"?"+type+"Id="+id+"&"+type+"Name="+name;
                            html += "<a title='"+name+"' style='margin-right: 5px;' href=\"javascript:page('"+jumpUrl+"','"+statisticsModal.config.statisticsName[type]+"');\">"+name+"</a>";
                        }else{
                            html += "<span style='margin-right: 5px;' title=\""+name+"\">"+name+"</span>";
                        }
                    }else if(statisticsModal.config.currentUserDept == 'GL' || statisticsModal.config.currentUserDept == 'CW'){ //管理和财务可以跳转任意统计
                        if(type == 'supplier' && companyCode != statisticsModal.config.currentUserCompany) {  //供应商特殊处理，仅展示自己公司的
                            if(!html){
                                html += "<span style='margin-right: 5px;' title=''>****</span>";
                            }
                        }else {
                            var jumpUrl = statisticsModal.config.statisticsUrl[type]+"?"+type+"Id="+id+"&"+type+"Name="+name;
                            html += "<a title='"+name+"' style='margin-right: 5px;' href=\"javascript:page('"+jumpUrl+"','"+statisticsModal.config.statisticsName[type]+"');\">"+name+"</a>";
                        }
                    }else {
                        html += "<span style='margin-right: 5px;' title=\""+name+"\">"+name+"</span>";
                    }
                }else{
                    html += "<span style='margin-right: 5px;' title=\""+obj+"\">"+obj+"</span>";
                }
            });
        }
        return html;
    },
    getSingleLinkHtml:function (id,value,type) { //获取单个a链接
        var html = "";
        if(id){
            value = value ? value : id;//如果value为空则展示ID
            if(statisticsModal.config.currentUserDept == 'MJ'){ //媒介可以跳转媒介、板块、媒体、供应商等统计
                if(type == 'mediaUser' || type == 'mediaType' || type == 'media' || type == 'supplier'){
                    var jumpUrl = statisticsModal.config.statisticsUrl[type]+"?"+type+"Id="+id+"&"+type+"Name="+value;
                    html += "<a title='"+value+"' style='margin-right: 5px;' href=\"javascript:page('"+jumpUrl+"','"+statisticsModal.config.statisticsName[type]+"');\">"+value+"</a>";
                }else{
                    html += "<span style='margin-right: 5px;' title=\""+value+"\">"+value+"</span>";
                }
            }else if(statisticsModal.config.currentUserDept == 'YW'){ // 业务可以跳转业务、客户等统计
                if(type == 'cust' || type == 'business'){
                    var jumpUrl = statisticsModal.config.statisticsUrl[type]+"?"+type+"Id="+id+"&"+type+"Name="+value;
                    html += "<a title='"+value+"' style='margin-right: 5px;' href=\"javascript:page('"+jumpUrl+"','"+statisticsModal.config.statisticsName[type]+"');\">"+value+"</a>";
                }else{
                    html += "<span style='margin-right: 5px;' title=\""+value+"\">"+value+"</span>";
                }
            }else if(statisticsModal.config.currentUserDept == 'GL' || statisticsModal.config.currentUserDept == 'CW'){ //管理和财务可以跳转任意统计
                var jumpUrl = statisticsModal.config.statisticsUrl[type]+"?"+type+"Id="+id+"&"+type+"Name="+value;
                html += "<a title='"+value+"' style='margin-right: 5px;' href=\"javascript:page('"+jumpUrl+"','"+statisticsModal.config.statisticsName[type]+"');\">"+value+"</a>";
            }else {
                html += "<span style='margin-right: 5px;' title=\""+value+"\">"+value+"</span>";
            }
        }
        return html;
    }
};