var chargeUsers = [];//获取建议的负责人
var adviceIds = [];//当前人是那些建议类别的负责人
var configObj={
    //缓存当前公司所有用户
    users:{},
    timeRange:{1:"一个月",2:"两个月",3:"三个月"},
    loadUsers:function () {
        if(configObj.isEmptyObject(configObj.users)){
            requestData({companyCode:user.companyCode},"/propose/listByForum","post","json",false,function (data) {
                if(data.code==200){
                    if(data.data.number>0){
                        for(var i=0;i<data.data.list.length;i++){
                            var id = data.data.list[i].id;
                            var name = data.data.list[i].name;
                            configObj.users[id]=name;
                        }
                    }
                }
            });
        }
    },
    // 建议权限
    loadSuggestPermission:function(){
        requestData(null,"/propose/showUsersByCompanyCode","post","json",false,function (data) {
            chargeUsers = [];
            adviceIds = [];
            var list = data.data.list;
            var ids = data.data.ids;
            for(var i =0;i<list.length;i++){
                chargeUsers[i] = list[i];
                adviceIds[i] = ids[i];
            }
        });
    },
    isEmptyObject:function (obj) {
        var flag=true;
        for(var key in obj){
            flag =  false;
        }
        return flag;
    }
}

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,dataType,async,callBackFun, contentType) {
    var param = {
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun,
        error: function () {
            Ladda.stopAll();
        }
    };
    if(contentType){
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
}

$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#query_table_logs').setGridWidth(width);
        $('#query_table_logs2').setGridWidth(width);
    });
    //建议录入添加默认信息
    init();
    //建议权限
    configObj.loadSuggestPermission();

    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined) {
        var flag = getQueryString("flag");
        var id = getQueryString("id");
        if(flag==1){
            //处理建议
            handlePropose(id);
        }else if(flag==2){
            //确认处理结果
            view(id,1);
        }else{
            //建议查看
            view(id,0);
        }
    }

    $("#find").click(function () {
        $("#query_table_logs3").emptyGridParam();
        $("#query_table_logs3").jqGrid('setGridParam', {
            postData: $("#queryCount").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    })

    $("#search").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    })

    $("#querySearch").click(function () {
        $("#query_table_logs2").emptyGridParam();
        $("#query_table_logs2").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
    })

    $("#addBtn").click(function () {
        $("#savePropose").find("textarea").removeClass('error');
        $("#savePropose").find("select").removeClass('error');
        $("#savePropose").validate().resetForm();
        document.getElementById("savePropose").reset();
        Ladda.stopAll();
        $("#name2").val(user.name);
        $("#userId2").val(user.id);
        $("#deptId2").val(user.deptId);
        $("#deptName2").val(user.deptName);
        // $("#entryTime2").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
        $("#proposeType2").removeAttr("disabled");
        $("#proposeType2").empty();
        $.get("/propose/queryProposeType",{typeCode:"PROPOSE_TYPE"},function (data) {
            var html = "<option value=''>--请选择--</option>" ;
            for (var i=0;i<data.data.number;i++){
                html += "<option value='"+data.data.list[i].id+"'>"+data.data.list[i].name+"</option>";
            }
            if(data.data.number==0){
                layer.msg("没有建议类型，请联系管理员");
                return ;
            }
            $("#proposeType2").append(html);
        },"json");
        $(".update").hide();
        $(".save").show();
        $("#saveModal").modal("toggle");
    });

    $("#timeSection").click(function () {
        setTimeSection("#startTimeSection","#endTimeSection");
    });

    //工作台，日程建议填写进入
    if(getQueryString("op") == 'add'){
        $("#addBtn").click();
    }

    //导出建议信息
    $("#exportPropose").click(function(){
        var params = $("#queryForm").serializeJson();
        location.href="/propose/exportPropose?"+$.param(params);
    });

    $("#exportNoPropose").click(function () {
        var params = $("#queryCount").serializeJson();
        location.href="/propose/exportUserNoPropose?"+$.param(params);
    });
    //建议查询tab页切换
    $("#tab0").click(function () {
        $("#queryForm select[name='proposeState']").val("");
        $("#queryForm select[name='proposeState']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tab1").click(function () {
        $("#queryForm select[name='proposeState']").val("0");
        $("#queryForm select[name='proposeState']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tab2").click(function () {
        $("#queryForm select[name='proposeState']").val("2");
        $("#queryForm select[name='proposeState']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tab3").click(function () {
        $("#queryForm select[name='proposeState']").val("1");
        $("#queryForm select[name='proposeState']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tab4").click(function () {
        $("#queryForm select[name='proposeState']").val("3");
        $("#queryForm select[name='proposeState']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tab5").click(function () {
        $("#queryForm select[name='proposeState']").val("4");
        $("#queryForm select[name='proposeState']").css({color: "#00796a"})
        $("#search").trigger("click") ;
        $("#tabList").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    //建议管理tab页切换
    $("#tabChild0").click(function () {
        $("#queryManageParam select[name='proposeState']").val("");
        $("#queryManageParam select[name='proposeState']").css({color: "#00796a"})
        $("#querySearch").trigger("click") ;
        $("#tabList2").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tabChild1").click(function () {
        $("#queryManageParam select[name='proposeState']").val("0");
        $("#queryManageParam select[name='proposeState']").css({color: "#00796a"})
        $("#querySearch").trigger("click") ;
        $("#tabList2").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tabChild2").click(function () {
        $("#queryManageParam select[name='proposeState']").val("2");
        $("#queryManageParam select[name='proposeState']").css({color: "#00796a"})
        $("#querySearch").trigger("click") ;
        $("#tabList2").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tabChild3").click(function () {
        $("#queryManageParam select[name='proposeState']").val("1");
        $("#queryManageParam select[name='proposeState']").css({color: "#00796a"})
        $("#querySearch").trigger("click") ;
        $("#tabList2").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tabChild4").click(function () {
        $("#queryManageParam select[name='proposeState']").val("3");
        $("#queryManageParam select[name='proposeState']").css({color: "#00796a"})
        $("#querySearch").trigger("click") ;
        $("#tabList2").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });
    $("#tabChild5").click(function () {
        $("#queryManageParam select[name='proposeState']").val("4");
        $("#queryManageParam select[name='proposeState']").css({color: "#00796a"})
        $("#querySearch").trigger("click") ;
        $("#tabList2").find("span").css("color",""); //设置所有TAB颜色为默认
        $(this).find("span").css("color","red"); //设置当前选中Tab颜色为红色
    });

    //建议管理初始化统计时间区间
    queryProposeTimeSection("#startMonth","#endMonth");

    //缓存当前公司用户数据
    configObj.loadUsers();

//建议查询
$("#query_table_logs").jqGrid({
        url: '/propose/listPgByself',
        datatype: "json",
        postData:$("#queryForm").serializeJson(), //发送数据
        mtype: "post",
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        // multiselect: true,
        // multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 25,//每页显示记录数
        rowList: [25, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [
            {name: 'id', label: '建议编号', editable: true, hidden: true, width: 60},
            {name: 'userId', label: '提出人id', editable: true, hidden: true, width: 30},
            {name: 'adviceType', label: '建议类型', editable: true,width: 40},
            {name: 'proposeType', label: 'proposeType',hidden: true, editable: true,width: 60},
            {name: 'name', label: '提出人', editable: true, width: 40},
            {name: 'year', label: '年', editable: true,hidden: true, width: 30},
            {name: 'month', label: '月', editable: true,hidden: true, width: 30},
            {name: 'entryTime', label: '录入时间', editable: true, width: 60},
            {name: 'updateTime', label: '修改时间', editable: true, width: 60},
            {name: 'deptId', label: '部门id', editable: true, width: 30 ,hidden:true},
            {name: 'deptName', label: '部门', editable: true, width: 60},
            {
                name: 'state', label: '状态', editable: true, width: 50,
                formatter: function (value, grid, rows, state) {
                    //状态：0未处理，1已处理，2处理中
                    if(value==0){
                        return "<span style='color: red'>未处理</span>"
                    }else if(value==1){
                        return "<span>已处理</span>"
                    }else if(value==2){
                        return "<span>处理中</span>"
                    }else if(value==3){
                        return "<span>已确认</span>"
                    }else{
                        return "<span style='color: #953b39'>已驳回</span>"
                    }                }
            },
            {name: 'problemDescription', label: '问题描述', editable: true, width: 160,
                formatter: function (d) {
                    return "<span style='width:300px;display: inline-block;overflow:hidden; white-space:nowrap; text-overflow:ellipsis' title='"+d+"'>"+d+"</span>";
                }
            },
            {name: 'handleResult', label: '处理结果', editable: true, width: 80,
                formatter: function (value, grid, rows, state) {
                    if(value==1){
                        return "已处理"
                    }else if(value==2){
                        return "处理中"
                    }else if(value==3){
                        if(configObj.users[rows.appointPerson]!=null && configObj.users[rows.appointPerson]!="" && configObj.users[rows.appointPerson]!=undefined){
                            return "指定给其他人："+configObj.users[rows.appointPerson];
                        }else{
                            return "指定给其他人";
                        }
                    }else{
                        return "";
                    }
                }
            },
            {name: 'handlePerson', label: '解决人', editable: true, width: 40},
            {name: 'handleTime', label: '解决时间', editable: true, width: 60},
            {
                name: 'operate', label: "操作", index: '', width: 100,
                formatter: function (value, grid, rows, state) {
                    var html="";
                    if(rows.state==0 && rows.userId==user.id){
                        html += "<a onclick='updatePropose(" + rows.id + ")'>修改</a>&nbsp;&nbsp;&nbsp;&nbsp;";
                        html += "<a onclick='deletePropose(" + rows.id + ")'>删除</a>&nbsp;&nbsp;&nbsp;&nbsp;";
                    }
                    if(rows.state==1 && rows.userId==user.id){
                        html += "<a onclick='view(" + rows.id + ",1)'>确认</a>&nbsp;&nbsp;";
                    }
                    if(rows.state==2){
                        //指定人是本人或者处理结果是处理中并且处理人是本人的显示处理链接
                        if(rows.appointPerson==user.id || (rows.handleResult==2 && rows.handlePerson==user.name)){
                            html += "<a onclick='handlePropose(" + rows.id + ")'>处理</a>&nbsp;&nbsp;&nbsp;&nbsp;";
                        }
                    }else if(rows.state==0 || rows.state==4){
                        //负责人查询处理建议(负责人的在自己板块录的建议如何处理-->建议管理胡姐处理)
                        if(hasRoleToHandleAdvice() && hasAdviceType(rows.proposeType)){
                            html += "<a onclick='handlePropose(" + rows.id + ")'>处理</a>&nbsp;&nbsp;&nbsp;&nbsp;";
                        }
                    }
                    return html;
                }
            }
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "建议列表",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            view(rowid,0);
        },
    });

//建议管理(js未分离)
$("#query_table_logs2").jqGrid({
        url: '/propose/listPg',
        datatype: "json",
        postData:$("#queryForm").serializeJson(), //发送数据
        mtype: "post",
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: false,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        // multiselect: true,
        // multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 25,//每页显示记录数
        rowList: [25, 50,100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [
            {name: 'id', label: '建议编号', editable: true, hidden: true, width: 60},
            {name: 'user_id', label: '提出人id', editable: true, hidden: true, width: 40},
            {name: 'adviceType', label: '建议类型', editable: true,width: 40},
            {name: 'name', label: '提出人', editable: true, width: 40},
            {name: 'year', label: '年', editable: true,hidden: true, width: 30},
            {name: 'month', label: '月', editable: true,hidden: true, width: 30},
            {name: 'entry_time', label: '录入时间', editable: true, width: 60,
                formatter:function (d) {
                    if(!d){
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {name: 'update_time', label: '修改时间', editable: true, width: 60,
                formatter:function (d) {
                    if(!d){
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {name: 'dept_id', label: '部门id', editable: true, width: 30 ,hidden:true},
            {name: 'dept_name', label: '部门', editable: true, width: 40},
            {
                name: 'state', label: '状态', editable: true, width: 40,
                formatter: function (value, grid, rows, state) {
                    //状态：0未处理，1已处理，2处理中
                    if(value==0){
                        return "<span style='color: red'>未处理</span>"
                    }else if(value==1){
                        return "<span>已处理</span>"
                    }else if(value==2){
                        return "<span>处理中</span>"
                    }else if(value==3){
                        return "<span>已确认</span>"
                    }else{
                        return "<span style='color: #953b39'>已驳回</span>"
                    }
                }
            },
            {name: 'problem_description', label: '问题描述', editable: true, width: 160,
                formatter: function (d) {
                    if(d!=undefined){
                        return "<span style='width:300px;display: inline-block;overflow:hidden; white-space:nowrap; text-overflow:ellipsis' title='"+d+"'>"+d+"</span>";
                    }else{
                        return "";
                    }
                }
            },
            {name: 'handle_result', label: '处理结果', editable: true, width: 80,
                formatter: function (value, grid, rows, state) {
                    if(value==1){
                        return "已处理"
                    }else if(value==2){
                        return "处理中"
                    }else if(value==3){
                        if(configObj.users[rows.appoint_person]!=null && configObj.users[rows.appoint_person]!="" && configObj.users[rows.appoint_person]!=undefined){
                            return "指定给其他人："+configObj.users[rows.appoint_person];
                        }else{
                            return "指定给其他人";
                        }
                    }else{
                        return "";
                    }
                }
            },
            {name: 'handle_person', label: '解决人', editable: true, width: 40},
            {name: 'handle_time', label: '解决时间', editable: true, width: 60,
                formatter:function (d) {
                    if(!d){
                        return "";
                    }
                    return new Date(d).format("yyyy-MM-dd hh:mm:ss");
                }
            },
            {
                name: 'operate', label: "操作", index: '', width: 100,
                formatter: function (value, grid, rows, state) {
                    var html="";
                    if((rows.state==0 && rows.user_id==user.id) || rows.state==1 || rows.state==3){
                        //建议管理未处理，已处理，已确认可删除
                        // html += "<a style='color:blue' onclick='updatePropose(" + rows.id + ")'><b>修改</b></a>&nbsp;&nbsp;";
                        html += "<a style='' onclick='delPropose(" + rows.id + ")'>删除</a>&nbsp;&nbsp;&nbsp;&nbsp;";
                    }
                    if(rows.state==1 && rows.user_id==user.id){
                        html += "<a onclick='view(" + rows.id + ",1)'>确认</a>&nbsp;&nbsp;";
                    }
                    if((rows.state==0  || rows.state==4 || rows.handle_result==2) && hasRoleForPropose()){
                            html += "<a style='' onclick='handlePropose(" + rows.id + ")'>处理</a>&nbsp;&nbsp;&nbsp;&nbsp;";
                    }else if(rows.state==2){
                        if(rows.appoint_person==user.id|| (rows.handleResult==2 && rows.handlePerson==user.name)){
                            html += "<a style='' onclick='handlePropose(" + rows.id + ")'>处理</a>&nbsp;&nbsp;&nbsp;&nbsp;";
                        }
                    }
                    return html;
                }
            }
        ],
        pager: jQuery("#query_pager_logs2"),
        viewrecords: true,
        // caption: "建议列表",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            view(rowid,0);
        },
    });
});

//未建议人员显示
var gridObject={
    url: '/user/queryNoProposeUser',
    datatype: "json",
    postData:$("#queryCount").serializeJson(), //发送数据
    mtype: "post",
    altRows: true,
    altclass: 'bgColor',
    height: "auto",
    page: 1,//第一页
    rownumbers: true,
    rownumWidth:160,
    setLabel: "序号",
    autowidth: true,//自动匹配宽度
    gridview: true, //加速显示
    cellsubmit: "clientArray",
    viewrecords: true,  //显示总记录数
    // multiselect: true,
    // multiselectWidth: 25, //设置多选列宽度
    sortable: "true",
    sortname: "id",
    sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
    shrinkToFit: true,
    prmNames: {rows: "size"},
    rowNum: 25,//每页显示记录数
    rowList: [25, 50,100,500],//分页选项，可以下拉选择每页显示记录数
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: "id"
    },
    colModel: [
        {name: 'deptId', label: '部门id', editable: true,align: "center",
            cellattr: function (rowId, tv, rawObject, cm, rdata) {
                //合并单元格
                return "id='deptId" + rowId + "'";
            },
            hidden: true
        },
        {name: 'deptName', label: '部门', editable: false,align: "center",sortable:true,
            cellattr: function (rowId, tv, rawObject, cm, rdata) {
                return "id='deptName" + rowId + "'";
            },
        },
        {name: 'userName', label: '未录建议人', editable: false},
    ],
    pager: jQuery("#query_pager_logs3"),
    viewrecords: true,
    caption: "默认查询未录入建议人员信息",
    add: false,
    edit: true,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false,
    gridComplete: function () {
        var primaryKey = "deptId";
        grid.mergerCell('deptId',primaryKey);
        grid.mergerCell('deptName',primaryKey);
        //跨页面选择
        grid.getPageSelectedSet("deptId");
    },
    loadComplete: function (a, b, c) {
        jQuery("#query_table_logs3").jqGrid('setLabel', 'rn', '序号', {
            'text-align': 'center',
            'vertical-align': 'middle',
        });
    },
    pager: "#query_pager_logs3",
    viewrecords: true,
    // caption: "业务查询",
    add: false,
    edit: false,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false
}

//设置统计时间区间，加载时间数据
function setTimeSection(start,end) {
   $("#setTimeSectionModal").modal("toggle");
    $("#startTimeSection").val("");
    $("#endTimeSection").val("");
    var data=configObj.timeRange;
    var state=$("#timeRange").val();
    layui.use(["form"], function (){
        var html="<option value=\"\">请选择</option>";
        for(var i=1;i<=3;i++){
            var selected = i==state?"selected=selected":"";
            html+=" <option value="+i+" "+selected+">"+data[i]+"</option>\n" ;
        }
        $("#timeState").html(html);
        $("#createName").val(user.name);
        $("#createDeptName").val(user.deptName);
        dealTimeSection(state,start,end,0);
        layui.form.render();
        layui.form.on('select(timeFilter)',function (data) {
            if(data.value!=""){//默认本月
               dealTimeSection(data.value,start,end,0);
            }else{
                $("#startTimeSection").val("");
                $("#endTimeSection").val("");
            }
        });
    });
}

//处理时间区间(t：计算前一个时间的参数，startElem:开始时间元素，endELem：结束时间元素，flag:加载时间区间日期组件1是0否)
function dealTimeSection(t,startElem,endElem,flag) {
    var adjust=0;
    if(t==1){//默认本月
        adjust=1;
    }else if(t==2){//默认前一个月到本月
        adjust=0;
    }else if(t==3){//默认前两个月到本月
        adjust=-1;
    }else {
        adjust=1;
    }
    var nowDays = new Date();
    var start = nowDays.getFullYear()+'/'+(nowDays.getMonth()+parseInt(adjust))+'/1';//前两个月的第一天
    var year = nowDays.getFullYear();
    var month = nowDays.getMonth()+1;
    if(month==0){
        month=12;
        year=year-1;
    }
    if (month < 10) {
        month = "0" + month;
    }
    var myDate = new Date(year, month, 0);
    var end = year + "/" + month + "/" + myDate.getDate();//本月的最后一天
    $(startElem).val(start);
    $(endElem).val(end);
    if(flag==1){//加载时间区间日期组件
        var startMonth = {
            elem: '#startMonth',
            format: 'YYYY/MM/DD ',
            istime: true,
            istoday: false
        };
        var endMonth = {
            elem: '#endMonth',
            format: 'YYYY/MM/DD ',
            istime: true,
            istoday: false
        };
        laydate(startMonth);
        laydate(endMonth);
    }
}

//提交建议时间区间设置
function submitTimeSection(t){
    var state=$("#timeState").val();
    var start=$("#startTimeSection").val();
    var end=$("#endTimeSection").val();
    if(state!=""){
       layer.confirm("确定修改建议管理统计时间配置?",{
               btn:["确定","取消"],
               shade:false
           },function (index) {
               layer.close(index);
               startModal("#"+t.id);
               requestData({state:state,startTime:start,endTime:end},"/proposeTips/setProposeTimeSection","post","json",true,function (data) {
               if(data.code==200){
                   layer.msg(data.data.message,{time:1000,icon:6});
                   Ladda.stopAll();
                   //建议管理初始化统计时间区间
                   queryProposeTimeSection("#startMonth","#endMonth");
                   $("#query_table_logs3").emptyGridParam();
                   $("#query_table_logs3").jqGrid('setGridParam', {
                       postData: $("#queryCount").serializeJson(), //发送数据
                   }).trigger("reloadGrid"); //重新载入
                   $("#setTimeSectionModal").modal("hide");
               }else if(data.code==1002){
                   swal({
                       title:"提示",
                       text:data.msg
                   });
                   Ladda.stopAll();
               }
           });
       });
    }else{
        layer.open({
            title:"提示",
            content:"请选择建议管理时间范围!"
        });
    }
}

//查询建议时间范围(赋值时间范围)
function queryProposeTimeSection(start,end) {
    requestData(null,"/proposeTips/queryTipsByType","post","json",false,function (data) {
        if(data.code==200){
            var state=data.data.state;
            $("#timeRange").val(state);
            dealTimeSection(state,start,end,1);
        }else if(data.code==1002){
            swal({
                title:"提示",
                text:data.msg
            });
        }
    })
}

//跳转建议制度
function goToDocument() {
    $.ajax({
        type:"post",
        url:"/proposeTips/getDocumentUrl",
        data:null,
        dataType:"json",
        async:false,
        success:function (data) {
            if(data.code==200){
                var id = data.data.id;
                if(id!=null){
                    page("/documentLibrary/library/view/"+id,"制度管理");
                }else{
                    layer.msg("请先配置制度跳转链接",{time:1000,icon:5});
                }
            }
        }
    });

}

//建议查询刷新表格
function queryProposeList(){
    $("#tabList > li").each(function (i,item) {
        $(item).removeClass("active");
        $("#tabList").find("span").css("color","");
    })
    $("#tab0").addClass("active");
    $("#query_table_logs").emptyGridParam();
    $("#query_table_logs").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//建议管理刷新表格
function queryProposeManage() {
    $("#tabList2 > li").each(function (i,item) {
        $(item).removeClass("active");
        $("#tabList2").find("span").css("color","");
    })
    $("#tabChild0").addClass("active");
    $("#query_table_logs2").emptyGridParam();
    $("#query_table_logs2").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

/**
 * 删除建议（建议查询）
 * @param id
 */
function deletePropose(id){
    if(getByDelAdviceState(id)){
        layer.confirm("确认删除该建议？",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: "/propose/deletePropose",
                data: {id: id},
                dataType: "json",
                async:false,
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_table_logs").jqGrid("setGridParam",{
                            postData:$("#queryForm").serializeJson()
                        }).trigger("reloadGrid");
                        $("#query_table_logs2").jqGrid("setGridParam",{
                            postData:$("#queryForm").serializeJson()
                        }).trigger("reloadGrid");
                    } else {
                        layer.msg("删除失败", {time: 1000, icon: 6});
                    }
                }
            });
        })
    }
}

/**
 * 删除建议（建议管理：所有建议都可以删除，不管页面状态对不对，只限未处理，已处理）
 * @param id
 */
function delPropose(id){
        layer.confirm("确认删除该建议？",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: "/propose/cutPropose",
                data: {id: id},
                dataType: "json",
                async:false,
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#query_table_logs").jqGrid("setGridParam",{
                            postData:$("#queryForm").serializeJson()
                        }).trigger("reloadGrid");
                        $("#query_table_logs2").jqGrid("setGridParam",{
                            postData:$("#queryForm").serializeJson()
                        }).trigger("reloadGrid");
                    } else {
                        layer.msg("删除失败", {time: 1000, icon: 6});
                    }
                }
            });
        })
}

function loadUser(t){
    layui.use(["form"], function (){
        $.get("/propose/queryByCompanyCode",null,function (data) {
            var html="<option value=''>--请选择--</option>";
            $("#appointPerson").empty();
            $("#appointPerson3").empty();
            for (var i = 0; i < data.data.number; i++) {
                var selected = data.data.list[i].id == t ? "selected=selected" : "";
                html+= "<option value='"+data.data.list[i].id+"'"+selected+">"+data.data.list[i].name+"("+data.data.list[i].deptName+")</option>"
            }
            $("#appointPerson").append(html);
            $("#appointPerson3").append(html);
            layui.form.render();
        },"json");
    });
}

/**
 * 判断删除建议时状态是否正常
 * @param id
 * @returns {boolean}
 */
function getByDelAdviceState(id){
    var flag = false;
    $.ajax({
        type:"post",
        url:"/propose/getById",
        data:{proposeId:id},
        dataType:"json",
        async:false,
        success:function (data) {
            var state = data.data.propose["state"];
            if(state!=0){
                layer.msg("该状态不支持修改");
                $("#query_table_logs2").jqGrid("setGridParam",{
                    postData:$("#queryForm").serializeJson()
                }).trigger("reloadGrid");
                $("#query_table_logs").jqGrid("setGridParam",{
                    postData:$("#queryForm").serializeJson()
                }).trigger("reloadGrid");
                flag = false;
            }else{
                flag = true;
            }
        }
    });
    return flag;
}

/**
 * 判断确认，驳回建议时状态是否正常
 * @param id
 * @returns {boolean}
 */
function getByConfimAdviceState(id){
    var flag = false;
    $.ajax({
        type:"post",
        url:"/propose/getById",
        data:{proposeId:id},
        dataType:"json",
        async:false,
        success:function (data) {
            var state = data.data.propose["state"];
            if(state==3 || state==4){
                layer.msg("该状态不支持修改");
                $("#viewModal").modal("hide");
                //强制刷新
                goBackProcess();
                flag = false;
                return ;
            }else{
                flag = true;
            }
        }
    });
    return flag;
}

/**
 * 处理建议
 * @param id
 */
function handlePropose(id) {
    $("#updateForm").find("textarea").removeClass('error');
    $("#updateForm").find("select").removeClass('error');
    $("#updateForm").validate().resetForm();
    document.getElementById("updateForm").reset();
    $("#show").hide();
    $.ajax({
        type:"post",
        url:"/propose/getById",
        data:{proposeId:id},
        dataType:"json",
        async:false,
        success:function (data) {
            var state = data.data.propose["state"];
            if(state==0 || state==2 || state==4){
                if(state==2){
                    $("#query_table_logs2").jqGrid("setGridParam",{
                        postData:$("#queryForm").serializeJson()
                    }).trigger("reloadGrid");
                    $("#query_table_logs").jqGrid("setGridParam",{
                        postData:$("#queryForm").serializeJson()
                    }).trigger("reloadGrid");
                }
                $("#showHistoryAdvice").empty();
                for(var attr in data.data.propose){
                    $("input[name='"+attr+"']").attr("readonly","readonly");
                    $("input[name='"+attr+"']").val(data.data.propose[attr]);
                    $("#handlePerson").val(user.name);
                    if(attr=="problemDescription"){
                        $("#problemDescription").attr("readonly","readonly");
                        $("#problemDescription").val(data.data.propose[attr]);
                    }
                    if(attr=="expectSolution"){
                        $("#expectSolution").attr("readonly","readonly");
                        $("#expectSolution").val(data.data.propose[attr]);
                    }
                    if(attr=="handleAdvice"){
                        // $("#handleAdvice4").val(data.data.propose[attr]);
                        $("#handleAdvice4").val("");
                    }
                    if(attr=="proposeType"){
                        itemShow();
                        $("#proposeType").val(data.data.propose[attr]);
                        $("#adviceType").val(data.data.propose[attr]);
                    }
                    if(attr=="handleResult"){
                        // 修改时由自己选择处理结果
                        // $("#handleResult").val(data.data.propose[attr]);
                        // if(data.data.propose[attr]==3){
                        //     $("#show").show();
                        //     加载指定人信息
                        //     loadUser(null);
                        // }
                    }
                    if(attr=="appointPerson"){
                        $("#appointPerson").val("");
                    }
                }
                var count = data.data.number;
                var result = data.data.propose["handleAdvice"]!=""?data.data.propose["handleAdvice"]:"无";
                if(count>0){
                    if(count==1){
                        var html = "<span style='font-size: 12px;color: #999999;'>"+data.data.list[0].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[0].createDate+"</span>" +
                            "<p style='font-size: 14px;'>"+data.data.list[0].remark+"</p><br/>";
                    }else if(count>1){
                        var html = "<span style='font-size: 12px;color: #999999;'>"+data.data.list[0].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[0].createDate+"</span>" +
                            "<p style='font-size: 14px;'>"+data.data.list[0].remark+"</p><a href='javascript:void(0)' onclick='changeHistoryAdvice(0,1,"+id+")'>展开</a><br/>";
                    }
                    $("#showHistoryAdvice").append(html);
                }else{
                    $("#showHistoryAdvice").append("<p style='font-size: 14px;color:#999999'>"+result+"</p>");
                }
                $("#updateModal").modal("toggle");
            }else{
                layer.msg("该状态不支持修改");
                $("#query_table_logs2").jqGrid("setGridParam",{
                    postData:$("#queryForm").serializeJson()
                }).trigger("reloadGrid");
                $("#query_table_logs").jqGrid("setGridParam",{
                    postData:$("#queryForm").serializeJson()
                }).trigger("reloadGrid");
            }
        }
    });
    layui.use('form', function(){
        var form = layui.form;
        form.render();
    });
}

/**
 * 历史处理建议的展开与收缩
 * @param state
 * @param id
 * @param adviceId
 */
function changeHistoryAdvice(state,id,adviceId) {
    $.ajax({
        type:"post",
        url:"/propose/getById",
        data:{proposeId:adviceId},
        dataType:"json",
        async:false,
        success:function (data) {
            var size = data.data.number;
            var html = "";
            if(id==1){
                $("#showHistoryAdvice").empty();
                if(size>0){
                    if(state==0){
                        for(var i=0;i<size;i++){
                            if(i==size-1){
                                html += "<span style='font-size: 12px;color: #999999;'>"+data.data.list[i].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[i].createDate+"</span><p style='font-size: 14px;'>"+data.data.list[i].remark+"</p>";
                            }else{
                                html += "<span style='font-size: 12px;color: #999999;'>"+data.data.list[i].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[i].createDate+"</span><p style='font-size: 14px;'>"+data.data.list[i].remark+"</p><br/>";
                            }
                        }
                        html+="<a href='javascript:void(0)' onclick='changeHistoryAdvice(1,1,"+adviceId+")'>收起</a>"
                    }else{
                        html = "<span style='font-size: 12px;color: #999999;'>"+data.data.list[0].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[0].createDate+"</span>" +
                            "<p style='font-size: 14px;'>"+data.data.list[0].remark+"</p><a href='javascript:void(0)' onclick='changeHistoryAdvice(0,1,"+adviceId+")'>展开</a><br/>";
                    }
                }else if(size<=0){
                    $("#showHistoryAdvice").append("<p style='font-size: 14px;color:#999999'>无</p>");
                }
                $("#showHistoryAdvice").append(html);
            }else{
                $("#showHistoryAdvice3").empty();
                if(size>0){
                    if(state==0){
                        for(var i=0;i<size;i++){
                            if(i==size-1){
                                html += "<span style='font-size: 12px;color: #999999;'>"+data.data.list[i].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[i].createDate+"</span><p style='font-size: 14px;'>"+data.data.list[i].remark+"</p>";
                            }else{
                                html += "<span style='font-size: 12px;color: #999999;'>"+data.data.list[i].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[i].createDate+"</span><p style='font-size: 14px;'>"+data.data.list[i].remark+"</p><br/>";
                            }
                        }
                        html+="<a href='javascript:void(0)' onclick='changeHistoryAdvice(1,3,"+adviceId+")'>收起</a>"
                    }else{
                        html = "<span style='font-size: 12px;color: #999999;'>"+data.data.list[0].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[0].createDate+"</span>" +
                            "<p style='font-size: 14px;'>"+data.data.list[0].remark+"</p><a href='javascript:void(0)' onclick='changeHistoryAdvice(0,3,"+adviceId+")'>展开</a><br/>";
                    }
                }else if(size<=0){
                    $("#showHistoryAdvice3").append("<p style='font-size: 14px;color:#999999'>无</p>");
                }
                $("#showHistoryAdvice3").append(html);
            }
            // $("#updateModal").modal("toggle");
        }
    });

}

function updatePropose(id){
    $(".update").show();
    $(".save").hide();
    $("#savePropose").find("textarea").removeClass('error');
    $("#savePropose").find("select").removeClass('error');
    $("#savePropose").validate().resetForm();
    document.getElementById("savePropose").reset();
    $.ajax({
        type:"post",
        url:"/propose/getById",
        data:{proposeId:id},
        dataType:"json",
        async:false,
        success:function (data) {
            var state = data.data.propose["state"];
            if(state==0){
                for(var attr in data.data.propose){
                    $("input[name='"+attr+"']").val(data.data.propose[attr]);
                    if(attr=="problemDescription"){
                        $("#problemDescription2").val(data.data.propose[attr]);
                    }
                    if(attr=="expectSolution"){
                        $("#expectSolution2").val(data.data.propose[attr]);
                    }
                    if(attr=="proposeType"){
                        $("#proposeType2").attr("disabled","disabled");
                        listForSelect("#proposeType2");
                        $("#proposeType2").val(data.data.propose[attr]);
                    }
                    if(attr=="updateTime"){
                        $("#updateTime6").val(new Date().format("yyyy-MM-dd hh:mm:ss"));
                    }
                }
                $("#saveModal").modal("toggle");
            }else{
                layer.msg("该状态不支持修改");
                $("#query_table_logs2").jqGrid("setGridParam",{
                    postData:$("#queryForm").serializeJson()
                }).trigger("reloadGrid");
                $("#query_table_logs").jqGrid("setGridParam",{
                    postData:$("#queryForm").serializeJson()
                }).trigger("reloadGrid");
            }
        }
    });
}

// 建议录入时调用
// function submitHander(t,url){
//     if($("#savePropose").valid()){
//         layer.confirm("是否保存该建议",{
//             btn:["确定","取消"],
//             shade:false
//         },function (index) {
//             layer.close(index);
//             startModal("#"+t.id);
//             var formData = $("#savePropose").serializeJson();
//             $.ajax({
//                 type:"post",
//                 url:url,
//                 data:formData,
//                 dataType:"json",
//                 success:function (data) {
//                     if(data.code==200){
//                         Ladda.stopAll();
//                         layer.msg(data.data.message,{time:1000,icon:6});
//                         location.href="/propose/propose_list";
//                     }else{
//                         layer.msg("保存失败",{time:1000,icon:6});
//                     }
//                 }
//             });
//         },function () {
//             return;
//         });
//     }
// }

/**
 * 判断指定人下拉框是否为空
 * @returns {boolean}
 */
function checkEmpty() {
    var handleResult = $("#handleResult2").val();
    if(handleResult==3){
        var id = $("#appointPerson").val();
        if(id=="" || id==null){
            $("#message").html("请选择指定人");
            return false;
        }else{
            return true;
        }
    }else{
        return true;
    }
}

//建议处理
function submitHander2(t,url,flag){
    var formData="";
    var tips="";
    var proposeId = $("#viewForm #proposeId").val();
    //flag==1处理建议(建议管理)，flag==0确认建议，flag==2处理建议(建议查询)
    if(flag==1){
        $("#message").html("");
        if($("#updateForm").valid() && checkEmpty()){
            tips="是否处理该建议？";
            formData=$("#updateForm").serializeJson();
            common(t,url,tips,formData,flag);
        }
    }else if(flag==0){
        if(getByConfimAdviceState(proposeId)){
            tips="确认该建议";
            $("#state").val(3);
            formData=$("#viewForm").serializeJson();
            common(t,url,tips,formData,flag);
        }
    }else if(flag==2){
        $("#message").html("");
        if($("#updateForm").valid() && checkEmpty()){
            tips="是否处理该建议？";
            formData=$("#updateForm").serializeJson();
            common(t,url,tips,formData,flag);
        }
    }else{
        if(getByConfimAdviceState(proposeId)){
            if($("#viewForm").valid()){
                tips="是否驳回该处理结果？";
                $("#state").val(4);
                formData=$("#viewForm").serializeJson();
                common(t,url,tips,formData,flag);
            }
        }
    }
}

function common(t,url,tips,formData,flag){
    layer.confirm(tips,{
        btn:["确定","取消"],
        shade:false
    },function (index) {
        layer.close(index);
        startModal("#"+t.id);
        $.ajax({
            type:"post",
            url:url,
            data:formData,
            dataType:"json",
            success:function (data) {
                if(data.code==200){
                    Ladda.stopAll();
                    layer.msg(data.data.message,{time:1000,icon:6});
                    $("#query_table_logs2").jqGrid("setGridParam",{
                        postData:$("#queryForm").serializeJson()
                    }).trigger("reloadGrid");
                    $("#query_table_logs").jqGrid("setGridParam",{
                        postData:$("#queryForm").serializeJson()
                    }).trigger("reloadGrid");
                    //0确认,1,建议管理处理,2建议查询处理，3驳回
                    if(flag==1 || flag==2){
                        $("#updateModal").modal("hide");
                    }else{
                        $("#viewModal").modal("hide");
                    }
                }else{
                    layer.msg("保存失败",{time:1000,icon:6});
                }
            },
            error: function () {
                Ladda.stopAll();//隐藏加载按钮
            }
        });
    },function () {
        return;
    });

}

//修改建议
function submitHander3(t,url){
    if($("#savePropose").valid()){
        layer.confirm("确认该建议",{
            btn:["确定","取消"],
            shade:false
        },function (index) {
            layer.close(index);
            startModal("#"+t.id);
            var formData = $("#savePropose").serializeJson();
            $.ajax({
                type:"post",
                url:url,
                data:formData,
                dataType:"json",
                success:function (data) {
                    if(data.code==200){
                        Ladda.stopAll();
                        layer.msg(data.data.message,{time:1000,icon:6});
                        $("#query_table_logs").jqGrid("setGridParam",{
                            postData:$("#queryForm").serializeJson()
                        }).trigger("reloadGrid");
                        $("#saveModal").modal("hide");
                    }else if(data.code==1002){
                        swal({
                            title: "提示",
                            text: data.msg,
                        });
                    }else{
                        layer.msg("保存失败",{time:1000,icon:6});
                    }
                },
                error: function () {
                    Ladda.stopAll();//隐藏加载按钮
                }
            });
        },function () {
            return;
        });
    }
}

function view(id,tag){
    document.getElementById("viewForm").reset();
    $("#viewModal").modal("toggle");
    if(tag==0){
        $("#appointPerson3").val("");
        //查看建议详情
        $(".view").show();
        $(".confirm").hide();
    }else if(tag==1){
        //确认,驳回建议详情
        $(".view").hide();
        $(".confirm").show();
    }
    $.get("/propose/getById",{proposeId:id},function (data) {
        for(var attr in data.data.propose){
            $("input[name='"+attr+"']").attr("readonly", "readonly");
            $("input[name='"+attr+"']").val(data.data.propose[attr]);
            if(attr=="problemDescription"){
                $("#problemDescription3").attr("readonly", "readonly");
                $("#problemDescription3").val(data.data.propose[attr]);
            }
            if(attr=="expectSolution"){
                $("#expectSolution3").attr("readonly", "readonly");
                $("#expectSolution3").val(data.data.propose[attr]);
            }
            if(attr=="handleAdvice"){
                $("#handleAdvice3").attr("readonly", "readonly");
                // $("#handleAdvice3").val(data.data.propose[attr]);
                $("#handleAdvice3").val("");
            }
            if(attr=="proposeType"){
                $("#proposeType3").attr("disabled", "disabled");
                $("#proposeType3").val(data.data.propose[attr]);
            }
            if(attr=="handleResult"){
                $("#handleResult3").attr("disabled", "disabled");
                $("#handleResult3").val(data.data.propose[attr]);
            }
            if(attr=="appointPerson"){
                $("#appointPerson3").attr("disabled", "disabled");
                if(data.data.propose["handleResult"]==3){
                    $("#showView").show();
                    loadUser(data.data.propose[attr]);
                }
            }
            if(attr=="id"){
                $("#proposeId").val(data.data.propose[attr]);
            }
        }
        $("#showHistoryAdvice3").empty();
        var count = data.data.number;
        var result = data.data.propose["handleAdvice"]!=""?data.data.propose["handleAdvice"]:"无";
        if(count>0){
            if(count==1){
                var html = "<span style='font-size: 12px;color: #999999;'>"+data.data.list[0].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[0].createDate+"</span>" +
                    "<p style='font-size: 14px;'>"+data.data.list[0].remark+"</p><br/>";
            }else if(count>1){
                var html = "<span style='font-size: 12px;color: #999999;'>"+data.data.list[0].createName+"</span>&nbsp;&nbsp;<span style='font-size: 12px;color: #999999'>"+data.data.list[0].createDate+"</span>" +
                    "<p style='font-size: 14px;'>"+data.data.list[0].remark+"</p><a href='javascript:void(0)' onclick='changeHistoryAdvice(0,3,"+id+")'>展开</a><br/>";
            }
            $("#showHistoryAdvice3").append(html);
        }else{
            $("#showHistoryAdvice3").append("<p style='font-size: 14px;color:#999999'>"+result+"</p>");
        }
    },"json");
}

function changePerson(){
    var id = $("#appointPerson").val();
    if(id!=null || id!=""){
        $("#message").html("");
    }
}

function handleChange(){
    var handResult = $("#handleResult2").val();
    if(handResult==3){
        $("#show").show();
        loadUser(null);
    }else{
        if(handResult==4){
            $("#handleAdvice4").attr("required",true);
        }else{
            $("#updateForm").find("input").removeClass('error');
            $("#updateForm").validate().resetForm();
            // $("#handleAdvice4").attr("required",false);
        }
        $("#show").hide();
    }
}

function init(){
    var typeCode="PROPOSE_TYPE";
    $.get("/propose/queryProposeType",{typeCode:typeCode},function (data) {
        var html = "" ;
        for (var i=0;i<data.data.number;i++){
            html += "<option value='"+data.data.list[i].id+"'>"+data.data.list[i].name+"</option>";
        }
        $("#proposeType").append(html);
        $("#proposeType3").append(html);
        $("#proposeTypeQc").append(html);
    },"json");
}

/**
 * 根据t来赋指定下拉框值
 * @param t
 */
function listForSelect(t){
    var typeCode="PROPOSE_TYPE";
    $.get("/propose/queryProposeType",{typeCode:typeCode},function (data) {
        var html = "<option value=''>--请选择--</option>" ;
        for (var i=0;i<data.data.number;i++){
            html += "<option value='"+data.data.list[i].id+"'>"+data.data.list[i].name+"</option>";
        }
        $(t).append(html);
    },"json");
}

function itemShow(){
    var typeCode="PROPOSE_TYPE";
    $.ajax({
        type:"post",
        url:"/propose/queryProposeType",
        data:{typeCode:typeCode},
        dataType:"json",
        async:false,
        success:function (data) {
            var html = "" ;
            for (var i=0;i<data.data.number;i++){
                html += "<option value='"+data.data.list[i].id+"'>"+data.data.list[i].name+"</option>";
            }
            $("#proposeType").append(html);
        }
    });
}

//获取建议管理可以操作的人员
function hasRoleForPropose(){
    var flag = false;
    var len = user.roles.length;
    for (var i =0;i<len;i++){
        if(user.roles[i].type=='XZ' || user.roles[i].type=='ZJB' || user.roles[i].type=='FZ'){
            flag = true;
            break;
        }
    }
    return flag;
}

/**
 * 负责人处理建议的权限
 */
function hasRoleToHandleAdvice(){
    var flag = false;
    for(var i = 0;i<chargeUsers.length;i++){
        var chargeMan = chargeUsers[i].id;
        if(chargeMan==user.id){
            flag = true;
            break;
        }else{
            flag = false;
        }
    }
    return flag;
}

/**
 * 负责人只处理拥有的建议类型的建议
 */
function hasAdviceType(id){
    var flag = false;
    for(var i = 0;i<adviceIds.length;i++){
        var adviceId = adviceIds[i];
        if(adviceId==id){
            flag = true;
            break;
        }else{
            flag = false;
        }
    }
    return flag;
}