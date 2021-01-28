function view() {
    $("#myModal").modal('toggle');
}
function loadAccountInfo2() {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $("#account_table_logs2").jqGrid({
        url: baseUrl + '/user/listMgr',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#innerAccount").serializeJson(), //发送数据
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
        multiselect: true,
        multiboxonly: true,
        multiselectWidth: 25, //设置多选列宽度
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
        // colNames: ['角色类型', '角色名称', '角色描述', '操作'],
        colModel: [
            {name: 'deptName', label: '部门名称', editable: true, width: 240},
            {name: 'name', label: '用户名', editable: true, width: 240},
            // {name: 'balance', label: '账号开户行', editable: true, width: 240},
            {name: 'id', label: 'id', editable: true, hidden: true, width: 0},
        ],
        pager: "#account_pager_logs2",
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        //实现单选
        beforeSelectRow: function () {
            $("#account_table_logs2").jqGrid('resetSelection');
            return (true);
        }
    });
}
function resize(table) {
    if (table == undefined) return;
    var width = $(table).parents(".jqGrid_wrapper").width();
    if (width == 0) return;
    $(table).setGridWidth(width);
}
function reloadAccountTable() {
    $("#account_table_logs2").emptyGridParam() ;
    $("#account_table_logs2").jqGrid('setGridParam', {
        postData: $("#innerAccount").serializeJson(), //发送数据
        datatype: 'json',
    }).trigger("reloadGrid"); //重新载入
    resize("#account_table_logs2");
}

function xz1() {
    var rowid = $("#account_table_logs2").jqGrid("getGridParam", "selrow");     //获取选中行id
    var rowData = jQuery("#account_table_logs2").jqGrid("getRowData", rowid);   //获取选中行信息
    $("#mgrId").val(rowData.id);
    $("#mgrName").val(rowData.name);
    $("#myModal").modal('toggle');
    $("#accountModal2").modal('hide');
} 
 
function fgld() {
    var rowid = $("#account_table_logs2").jqGrid("getGridParam", "selrow");     //获取选中行id
    var rowData = jQuery("#account_table_logs2").jqGrid("getRowData", rowid);   //获取选中行信息
    $("#mgrLeaderId").val(rowData.id);
    $("#mgrLeaderName").val(rowData.name);
    $("#myModal").modal('toggle');
    $("#accountModal2").modal('hide');
}
function xz11() {
    var rowid = $("#account_table_logs2").jqGrid("getGridParam", "selrow");     //获取选中行id
    var rowData = jQuery("#account_table_logs2").jqGrid("getRowData", rowid);   //获取选中行信息
    $("#mgrId1").val(rowData.id);
    $("#mgrName1").val(rowData.name);
    $("#myModal").modal('toggle');
    $("#accountModal2").modal('hide');
}
function fgld1() {
    var rowid = $("#account_table_logs2").jqGrid("getGridParam", "selrow");     //获取选中行id
    var rowData = jQuery("#account_table_logs2").jqGrid("getRowData", rowid);   //获取选中行信息
    $("#mgrLeaderId1").val(rowData.id);
    $("#mgrLeaderName1").val(rowData.name);
    $("#myModal").modal('toggle');
    $("#accountModal2").modal('hide');
}

function submitHander(t,url) {
    if ($("#editForm").valid()) {
        // alert($("#form").serialize());
        var param = $("#editForm").serializeJson();
        startModal("#"+t.id);//锁定按钮，防止重复提交
        // startModal("#update");//锁定按钮，防止重复提交
        // alert(JSON.stringify(param));
        $.ajax({
            type: "post",
            url: url,
            data: param,
            dataType: "json",
            success: function (data) {
                Ladda.stopAll();   //解锁按钮锁定
                $("#account_table_logs2").jqGrid('setGridParam', {
                   // postData: $("#queryForm").serializeJson(), //发送数据
                }).trigger("reloadGrid"); //重新载入
                $("#myModal").modal('hide');
            },
            error: function () {
                Ladda.stopAll();//隐藏加载按钮
            }
        });
    }
}

/**
 * 加载公司下的所有职位
 */
function loadCompanyPost(companyCode) {
    $("#postDiv").empty();
    $.get(baseUrl + "/user/queryCompanyPost",{"companyCode":companyCode}, function (data) {
        getResCode(data);
        var html = "";
        $(data).each(function (i, item) {
            html += "<span class='col-md-2'><input type='checkbox' class='i-checks' name='postId' data-name='"+item.name+"' data-id='" + item.id + "' value='" + item.id + "' />" + item.name + "</span>";
        });
        $("#postDiv").html(html);
        $('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green',
        });
    }, "json");
}

/**
 * 部门职位绑定不为空
 * @returns {boolean}
 */
function checkEmpty() {
    var obj = $(".i-checks");
    var j = 0;
    for (var i = 0; i < obj.length; i++) {
        if (obj[i].checked) {
            j++;
        }
    }
    if (j == 0) {
        swal("职位绑定不能为空");
        return false;
    } else {
        return true;
    }
}

/**
 * 部门添加职位
 */
function addDeptPost(){
    if (checkEmpty()) {
        var obj = $(".i-checks");
        var checkId = "";
        var deptId = $("#deptPostForm #deptId").val();
        for (var i in obj) {
            if (obj[i].checked) {
                checkId += obj[i].value + ",";
            }
        }
        if (checkId.substr(checkId.length - 1) == ',') {
            checkId = checkId.substr(0, checkId.length - 1);
        }
        layer.confirm("请确认部门职位？", {
            btn: ["确认", "取消"],
            shade: false,
        }, function (index) {
            layer.close(index);
            $.ajax({
                type: "post",
                url: "/user/insertDeptPost",
                data: {"postId": checkId, "deptId": deptId},
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6});
                        $("#deptPostModal").modal("hide");
                    }
                }
            }, function () {
                return;
            });
        });
    }
}

/**
 * 部门添加时校验JT代码唯一性
 */
function checkCompanyCode() {
    var companyCode = $("#companyCode1").val();
    var deptCode = $("#deptCode1").val();
    if(companyCode=="JT" && deptCode=="GL"){
        layer.msg("若添加分公司，公司代码需要变更");
        $("#companyCode1").val("");
    }
}

/**
 * 部门添加时更改输入框状态
 */
function changeDeptInfo(){
    var companyName = $("#companyName").val();
    var companyCode = $("#companyCode").val();
    var deptCode = $("#deptCode1").val();
    var level = $("#level1").val();
    if(deptCode=="GL"){
        //level=0时需要清除readonly
        if(level==0){
            $("#companyName1").val("");
            $("#companyCode1").val("");
            $("#companyName1").removeAttr("readonly");
            $("#companyCode1").removeAttr("readonly");
        }else{
            $("#companyName1").val("");
            $("#companyCode1").val("");
        }
    }else{
        //level=0时需要添加readonly，防止添加集团部门时修改公司代码
        if(level==0){
            $("#companyName1").val(companyName);
            $("#companyCode1").val(companyCode);
            $("#companyName1").attr("readonly","readonly");
            $("#companyCode1").attr("readonly","readonly");
        }else{
            $("#companyName1").val(companyName);
            $("#companyCode1").val(companyCode);
        }
    }
}

/**
 * 添加公司代码时判断公司代码是否重复
 */
function checkRepeat(){
    var flag = false;
    var companyCode = $("#companyCode1").val();
    var deptCode = $("#deptCode1").val();
    $.ajax({
        type: "post",
        url: "/dept/checkDeptCompanyCode",
        data: {companyCode:companyCode},
        dataType: "json",
        async:false,
        success: function (data) {
            //分公司进行公司名称去重
            if(deptCode=="GL"){
                if (data.data.list.length > 0) {
                    flag = false;
                    layer.msg("该公司代码已存在，请更改后再试");
                    $("#companyCode1").val("");
                } else {
                    flag = true;
                }
            }else{
                //子部门不需要去重
                flag = true;
            }
        }
    });
    return flag;
}

/**
 * 新增公司移除部门领导人，部门分管理领导必填
 */
function removeRequired(){
    $(".saveCompanyFlag").hide();
    $("#mgrId1").prop("required",false);
    $("#mgrName1").prop("required",false);
    $("#mgrLeaderId1").prop("required",false);
    $("#mgrLeaderName1").prop("required",false);
}
/**
 * 新增公司添加部门领导人，部门分管理领导必填
 */
function addRequired(){
    $(".saveCompanyFlag").show();
    $("#mgrId1").attr("prop",true);
    $("#mgrName1").attr("prop",true);
    $("#mgrLeaderId1").attr("prop",true);
    $("#mgrLeaderName1").attr("prop",true);
}

$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    layui.use('colorpicker', function () {
        var colorpicker = layui.colorpicker;
        //渲染
        colorpicker.render({
            elem: '#test1'  //绑定元素
        });
    });

    $.get("/dict/listByTypeCode2",{typeCode:"DEPT_CODE"},function (data) {
        var html = "";
        for (var i = 0; i<data.length;i++){
            html += "<option value='"+data[i].code+"'>"+data[i].name+"</option>";
        }
        $("#deptCode").append(html);
        $("#deptCode1").append(html);
    },"json")

    loadAccountInfo2();
    $("#selMgr").click(function () {
        $("#mgrCompanyCode").val($("#companyCode").val());
        $("#myModal").modal('hide');
        $("#accountModal2").modal('toggle');
        reloadAccountTable();
        document.getElementById("selectAccount").onclick = xz1;
    });
    $("#selMgr1").click(function () {
        $("#mgrCompanyCode").val($("#companyCode1").val());
        $("#myModal").modal('hide');
        $("#accountModal2").modal('toggle');
        reloadAccountTable();
        document.getElementById("selectAccount").onclick = xz11;
    });
    $("#selMgrLeader").click(function () {
        $("#mgrCompanyCode").val($("#companyCode").val());
        $("#myModal").modal('hide');
        $("#accountModal2").modal('toggle')
        reloadAccountTable();
        document.getElementById("selectAccount").onclick = fgld;
    });
    $("#selMgrLeader1").click(function () {
        $("#mgrCompanyCode").val($("#companyCode1").val());
        $("#myModal").modal('hide');
        $("#accountModal2").modal('toggle');
        reloadAccountTable();
        document.getElementById("selectAccount").onclick = fgld1;
    });
    $(".closeMgr").click(function () {
        $("#myModal").modal('toggle');
        $("#accountModal2").modal('hide');
    });
    /**
     * 选择绑定职位调用的方法
     */
    $("#selDeptPost").click(function(){
        $("#accountModal2").modal('hide');
        var deptId = $("#editForm #id").val();
        $("#deptPostForm #deptId").val(deptId);
        var companyCode = $("#editForm #companyCode").val();
        $("#deptPostForm #deptCompanyCode").val(companyCode);
        $("#deptPostModal").modal('toggle');
        $(".i-checks").iCheck("uncheck");
        $.ajax({
            type: "post",
            url: baseUrl + "/user/queryDeptPost",
            data: {"companyCode":companyCode,"deptId":deptId},
            dataType: "json",
            success: function (data) {
                $(data).each(function (j, type) {
                    $("#postDiv>span").each(function (i, d) {
                        var dataId = $(d).find("input").attr("data-id");
                        if (type.id == dataId) {
                            $(d).find("input").iCheck('check');
                            return true;
                        }
                    });
                });
            }
        });
    });
    $("#accountSearch2").click(function () {
        reloadAccountTable();
    });

    $("#deptPostSearch").click(function () {
        // swal($("#postNameQC").val());
        $("#dept_post_table").jqGrid('setGridParam', {
            postData: {companyCode:$("#editForm #companyCode").val(),postNameQC:$("#postNameQC").val()}, //发送数据
        }).trigger("reloadGrid"); //重新载入
    });

    $.ajax({
        url: baseUrl+"/dept/listByCompany",
        type: "post",
        dataType: "json",
        success: function (data) {
            require.config({
                paths: {
                    echarts: 'http://echarts.baidu.com/build/dist'
                }
            });
            require(
                [
                    'echarts',
                    'echarts/chart/tree' // 使用树状图就加载tree模块，按需加载
                ],
                function (ec) {
                    // 定义setTimeout执行方法
                    var TimeFn = null;
                    var ecConfig = require('echarts/config');
                    // 基于准备好的dom，初始化echarts图表
                    var myChart = ec.init(document.getElementById('main_orgStructure'));
                    myChart.on("click", clickFun);
                    myChart.on(ecConfig.EVENT.DBLCLICK, dblclickFun);
                    myChart.hideLoading();
                    // 为echarts对象加载数据
                    myChart.setOption(
                        option = {
                            series: [
                                {
                                    name: '组织架构图',
                                    color: '#000',
                                    type: 'tree',
                                    roam: true,//是否开启鼠标缩放和平移漫游。默认不开启。如果只想要开启缩放或者平移，可以设置成 'scale' 或者 'move'。设置成 true 为都开启
                                    rootLocation: {x: 'center', y: '15%'}, // 根节点位置  {x: 'center',y: 10}
                                    nodePadding: 20,
                                    layerPadding: 40,
                                    borderColor: 'black',
                                    // initialTreeDepth:3,
                                    // expandAndCollapse: true,
                                    // orient: 'vertical',
                                    // symbol: 'image://http://localhost/img/deptBg.png', //'circle', 'rect', 'roundRect', 'triangle', 'diamond', 'pin', 'arrow', 'none'
                                    symbol: 'side', //'circle', 'rect', 'roundRect', 'triangle', 'diamond', 'pin', 'arrow', 'none'
                                    itemStyle: {
                                        normal: {
                                            color: '#EC5565',
                                            // color: '#E86C79',
                                            // rotate: -90,
                                            label: {
                                                color: '#000',
                                                show: true,
                                                position: 'right',
                                                position: 'inside',
                                                textStyle: {
                                                    color: '#FFFFFF',//标签颜色
                                                    fontSize: 15,
                                                }
                                            },
                                            lineStyle: {
                                                color: '#ccc',
                                                width: 1,
                                                type: 'broken' // 'curve'|'broken'|'solid'|'dotted'|'dashed'
                                            }
                                        }
                                    },
                                    label: {
                                        color: '#000',
                                        // backgroundColor:'#000',
                                        // shadowColor:'#000',
                                        normal: {
                                            show: false,
                                            // rotate: -90,
                                            verticalAlign: 'middle',
                                            align: 'right',
                                            fontSize: 9
                                        }
                                    },

                                    data: data.data.list,
                                    top: '5%',
                                    left: '2%',
                                    bottom: '5%',
                                    right: '5%',
                                    symbolSize: [100, 30],
                                    showAllSymbol: true,

                                }
                            ]
                        });
                    // myChart.on('click', function (param) {
                    //     console.log(param)
                    //     alert(param);
                    // });
                    if (option && typeof option === "object") {
                        myChart.setOption(option, true);
                    }
                    //点击后收缩节点
                    function dblclickFun(param) {
                        // 取消上次延时未执行的方法
                        clearTimeout(TimeFn);
                        //双击事件的执行代码
                        //callChartListMethodReverse(this, 'onclick', param);
                        if (typeof param.seriesIndex == 'undefined') {
                            return;
                        }
                        if (param.data) {
                            var ecData = param;
                            var option = this.getOption();
                            var myChart = this;
                            if (ecData && ecData.seriesIndex != null) {
                                //this._messageCenter.dispatch(ecConfig.EVENT.CLICK, param.event, ecData, this);
                                //实现tree点击收缩
                                a(ecData, option, myChart);
                            }
                        }
                    }
                    //关键点！,点击后进入修改页面
                    function clickFun(param) {
                        //获取系统参数
                        sysTitleBackColorObj.init();

                        // 取消上次延时未执行的方法
                        $("#editForm").find("input").removeClass('error');
                        $("#editForm").validate().resetForm();
                        $("#addForm").find("input").removeClass('error');
                        $("#addForm").validate().resetForm();
                        clearTimeout(TimeFn);
                        //执行延时
                        TimeFn = setTimeout(function(){
                            //do function在此处写单击事件要执行的代码
                            // console.log(param) ;
                            if (typeof param.seriesIndex == 'undefined') {
                                return;
                            }
                            if (param.type == 'click') {
                                $("#mgrCompanyCode").val("");
                                $("#myModal").modal('toggle');
                                //加载指定公司的职位
                                loadCompanyPost(param.data.companyCode);
                                // $(".modal-body").find("option").removeAttr("selected") ;
                                //防止绑定部门职位数据残留
                                $("#postId").val("");
                                $("#postName").val("");
                                $("#name").val(param.name);
                                $("#name1").val("");
                                $("#companyCode").val(param.data.companyCode);
                                $("#companyName").val(param.data.companyName);
                                $("#selZW").attr("data-companyCode",param.data.companyCode);
                                $("#selZW").attr("data-deptId",param.data.value);
                                $("#deptCode").val(param.data.code);
                                $("#code").val(param.data.code);
                                $("#type").val(param.data.type);
                                $("#id").val(param.data.value);
                                $("#level").val(param.data.level);
                                $("#parentId").val(param.data.pid);
                                $("#mgrName").val(param.data.mgrName);
                                $("#mgrId").val(param.data.mgrId);
                                $("#mgrLeaderId").val(param.data.mgrLeaderId);
                                $("#mgrLeaderName").val(param.data.mgrLeaderName);
                                //子部门信息
                                $("#parentId1").val(param.data.value);
                                $("#level1").val(param.data.level);
                                $("#deptCode1").val(param.data.code);
                                $("#deptCode1").removeAttr("disabled");
                                $("#companyCode1").val(param.data.companyCode);
                                $("#companyName1").val(param.data.companyName);
                                $("#type1").val(param.data.type);
                                $("#showOnLevel").hide() ;
                                $("#showOnLevel2").hide() ;
                                switch(param.data.level)
                                {
                                    case 0:
                                        //集团公司增加子部门时，需要允许code可编辑，
                                        $("#companyCode").prop("readonly","");
                                        $("#companyCode1").prop("readonly","");
                                        $("#companyName").prop("readonly","");
                                        $("#companyName1").prop("readonly","");
                                        if(param.data.code=="GL"){
                                            $("#companyCode1").val("");
                                            $("#companyName1").val("");
                                            //新增公司移除部门领导，分管领导必填
                                            removeRequired();
                                        }else{
                                            //部门领导，分管领导添加必填
                                            addRequired();
                                        }
                                        $("#MJType").hide() ;
                                        $("#MJType1").hide() ;
                                        $("#showOnLevel").show() ;
                                        $("#showOnLevel2").show() ;
                                        //如果是集团公司，添加子部门管理类型展示
                                        $("#deptCode1").find("option[value='GL']").css("display", "block");
                                        break;
                                    case 1:
                                        //level=1时code一旦确定，子部门的code就复制父部门的code，所以有子部门就不宜修改了
                                        // $("#companyCode").prop("readonly","")
                                        $("#companyCode").prop("readonly","readonly");
                                        $("#companyCode1").prop("readonly",'readonly');
                                        $("#companyName").prop("readonly",'readonly');
                                        $("#companyName1").prop("readonly",'readonly');
                                        $("#MJType").hide() ;
                                        $("#MJType1").hide() ;
                                        //部门领导，分管领导添加必填
                                        addRequired();
                                        //如果是分公司，添加子部门移除管理类型
                                        $("#deptCode1").val("");
                                        $("#deptCode1").find("option[value='GL']").css("display", "none");
                                        break;
                                    case 2:
                                        $("#companyCode").prop("readonly",'readonly');
                                        $("#companyCode1").prop("readonly",'readonly');
                                        $("#companyName").prop("readonly",'readonly');
                                        $("#companyName1").prop("readonly","readonly");
                                        $("#MJType").hide() ;
                                        //部门领导，分管领导添加必填
                                        addRequired();
                                        //level=1时，要显示是新媒体还是网络媒介
                                        if(param.data.code == 'MJ'){
                                            $("#deptCode1").val(param.data.code) ;
                                            $("#MJType1").show() ;
                                            $("#types1").removeAttr('disabled');
                                        }else{
                                            $("#MJType1").hide() ;
                                        }
                                        //子部门和父级部门code 一致
                                        $("#deptCode1").prop("disabled", true);
                                        break;
                                    default:
                                        $("#companyCode").prop("readonly",'readonly');
                                        $("#companyCode1").prop("readonly",'readonly');
                                        $("#companyName").prop("readonly",'readonly');
                                        $("#companyName1").prop("readonly",'readonly');
                                        //部门领导，分管领导添加必填
                                        addRequired();
                                        if(param.data.code=='MJ'){
                                            $("#MJType1").show() ;
                                            $("#MJType").show() ;
                                            $("#types").prop("disabled",'disabled')
                                            $("#types1").prop("disabled",'disabled')
                                            $("#MJType").find("option[value='"+param.data.type+"']").prop('selected','selected');
                                            $("#MJType1").find("option[value='"+param.data.type+"']").prop('selected','selected');
                                        }else{
                                            $("#MJType").hide() ;
                                            $("#MJType1").hide() ;
                                        }
                                        //子部门和父级部门code 一致
                                        $("#deptCode1").prop("disabled", true);
                                }

                                //如果是公司节点，则展示系统背景颜色选择
                                if (param.data.code == "GL") {
                                    $("#sysBackColorWrap").css("display", "block");
                                    var defaultColor = "";
                                    if (sysTitleBackColorObj.oldSysconfig) {
                                        var systemBackColorMap = sysTitleBackColorObj.oldSysconfig.configValue || {};
                                        systemBackColorMap = typeof (systemBackColorMap) == "string" ? JSON.parse(systemBackColorMap) : systemBackColorMap;
                                        for (var key in systemBackColorMap) {
                                            if (systemBackColorMap[key] == param.data.companyCode) {
                                                defaultColor = key;//缓存当前配置公司的颜色
                                            }
                                        }
                                    }
                                    layui.use('colorpicker', function () {
                                        var colorpicker = layui.colorpicker;
                                        //渲染
                                        colorpicker.render({
                                            elem: '#sysBackColor',
                                            color: defaultColor,
                                            change: function (color) {//当颜色在选择器中发生选择改变时，会进入 change 回调
                                                //js在iframe子页面获取父页面元素
                                                var topNavWrap = window.parent.document.getElementById("topNavWrap");
                                                var topNavHeader = window.parent.document.getElementById("topNavHeader");
                                                $(topNavWrap).css("background-color", color);
                                                $(topNavHeader).css("background-color", color);
                                            },
                                            done: function (color) {//点击颜色选择器的“确认”和“清除”按钮，均会触发 done 回调
                                                //如果有值，则更新，否则新增
                                                if (sysTitleBackColorObj.oldSysconfig) {
                                                    sysTitleBackColorObj.update(color, param.data.companyCode);
                                                } else {
                                                    sysTitleBackColorObj.save(color, param.data.companyCode);
                                                }
                                            }
                                        });
                                    });
                                } else {
                                    $("#sysBackColorWrap").css("display", "none");
                                }
                            }
                        },300);
                    }
                });
        }
    })
    //=================================================节点维护开始===================================================
    // $.validator.setDefaults({
    //     highlight: function (e) {
    //         $(e).closest(".form-group").removeClass("has-success").addClass("has-error")
    //     }, success: function (e) {
    //         e.closest(".form-group").removeClass("has-error").addClass("has-success")
    //     }, errorElement: "span", errorPlacement: function (e, r) {
    //         e.appendTo(r.is(":radio") || r.is(":checkbox") ? r.parent().parent().parent() : r.parent())
    //     }, errorClass: "help-block m-b-none", validClass: "help-block m-b-none"
    // }),
    var e = "<i class='fa fa-times-circle'></i> ";
    $("#deptForm").validate({
        rules: {
            name: {
                required: !0, minlength: 2, maxlength: 16,
                remote: {
                    url: baseUrl+"/dept/checkName", // 后台处理程序
                    type: "post", // 数据发送方式
                    dataType: "json", // 接受数据格式
                    data: { // 要传递的数据
                        "id": function () {
                            return $("#id").val();
                        },
                        "name": function () {
                            return $("#name").val();
                        },
                        "childName": function () {
                            return "";
                        }
                    },
                    dataFilter: function (data) {
                        //返回值是string，需要转换成json
                        var obj = JSON.parse(data)
                        if (obj.data.flag) {
                            return true;
                        } else {
                            return false;
                        }

                    }
                }
            },
            childName: {
                required: !0, minlength: 2, maxlength: 16,
                remote: {
                    url: baseUrl+"/dept/checkName", // 后台处理程序
                    type: "post", // 数据发送方式
                    dataType: "json", // 接受数据格式
                    data: { // 要传递的数据
                        "id": function () {
                            return $("#id").val();
                        },
                        "name": function () {
                            return "";
                        },
                        "childName": function () {
                            return $("#childName").val();
                        }
                    },
                    dataFilter: function (data) {
                        //返回值是string，需要转换成json
                        var obj = JSON.parse(data)
                        if (obj.data.flag) {
                            return true;
                        } else {
                            return false;
                        }

                    }
                }
            },
        },
        // messages: {
        //     name: {
        //         required: e + "请输入部门名称",
        //
        //         remote: e + "部门名称重复"
        //     },
        //     childName: {
        //         required: e + "请输入部门名称",
        //
        //         remote: e + "部门名称重复"
        //     },
        // }
    });
    $("#edit").click(function () {
        if ($("#editForm").valid()) {
            layer.confirm("请确认部门信息", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
                // console.log(JSON.stringify($("#deptForm").serializeJson()))
                $.ajax({
                    type: "post",
                    url: baseUrl+"/dept/edit",
                    data: $("#editForm").serializeJson(),
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            // $("#myModal").modal('toggle');
                            setTimeout(function () {
                                window.location.reload();
                            }, 1000);
                        } else {
                            layer.msg(data.msg);
                        }
                    }
                });
            }, function () {
                return;
            })

        }
    });
    $("#del").click(function () {
        layer.confirm('确认删除？删除该部门会把相应的下属部门一起删除，请确认后操作！', {
            btn: ['删除', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
            $.ajax({
                type: "post",
                url: baseUrl+"/dept/del",
                data: {id:$("#id").val()},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        layer.msg("部门删除成功！", {time: 1000, icon: 6});
                        // $("#myModal").modal('toggle');
                        setTimeout(function () {
                            window.location.reload();
                        }, 1000);
                    } else {
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }
                }
            });
        }, function () {
            return;
        });
    });
    $("#add").click(function () {
        if ($("#addForm").valid() && checkRepeat()) {
            if($("#types1").val()!=""){
                $("#type1").val($("#types1").val());
            }else {
                $("#type1").val("");
            }
            var param = $("#addForm").serializeJson();
            param.code1 = $("#deptCode1").val();
            //防止多次点击添加多个部门
            layer.confirm("请确认部门信息", {
                btn: ["确定", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
                // console.log(JSON.stringify($("#deptForm").serializeJson()))
                $.ajax({
                    type: "post",
                    url: baseUrl + "/dept/addChild",
                    data: param,
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            layer.msg(data.data.message, {time: 1000, icon: 6});
                            // $("#myModal").modal('toggle');
                            setTimeout(function () {
                                window.location.reload();
                            }, 1000);

                        } else {
                            layer.msg(data.msg);
                        }
                    }
                });

            }, function () {
                return;
            })
        }

    });
    //=================================================节点维护结束===================================================

    //绑定政委相关代码
    zwModalObj.userSaveClickInit();
});

//加载当前公司下政委公司人员
var zwModalObj = {
    //后台请求方法
    requestData: function (data, url, requestType,dataType,async,callBackFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    //选择人员弹窗，人员保存按钮事件
    userSaveClickInit: function () {
        //绑定政委点击事件
        $("#selZW").click(function () {
            //清空原数据
            zwModalObj.existsZwUserList = [];
            zwModalObj.changeZwUserMap = {};
            $("#deptAsync") .iCheck('uncheck'); //将复选框默认不选中
            //获取当前部门已绑定的政委
            zwModalObj.requestData(null, "/deptZw/"+$(this).attr("data-deptId"), "get", "json",false,function (data) {
               if(data && data.length > 0){
                   $.each(data, function (index, deptZw) {
                       zwModalObj.existsZwUserList.push(deptZw.userId);
                   });
               }
            });
            zwModalObj.renderUserPage(this);
            $("#evaluationModal").modal('toggle');
        });
        //人员搜索按钮
        $("#userSearch").on("click", function () {
            zwModalObj.renderUserPage($("#selZW"));
        });
        //人员确定按钮
        $("#submitEvaluationPeople").click(function () {
            var deptAsync = $("#deptAsync").is(':checked');
            var param = {deptId: $("#selZW").attr("data-deptId"), deptAsync:deptAsync, addUserList: [], delUserList: []};
            if(zwModalObj.changeZwUserMap && Object.getOwnPropertyNames(zwModalObj.changeZwUserMap).length > 0){
                for(var userId in zwModalObj.changeZwUserMap){
                    if(zwModalObj.changeZwUserMap[userId] == "add"){
                        param.addUserList.push(userId);
                    }
                    if(zwModalObj.changeZwUserMap[userId] == "del"){
                        param.delUserList.push(userId);
                    }
                }
            }
            zwModalObj.requestData(JSON.stringify(param), "/deptZw/bindingDeptZw", "post", "json", true, function (data) {
                if(data.code == 200){
                    layer.msg("绑定政委成功！", {time: 3000, icon: 6});
                }else {
                    if(data.msg){
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }else {
                        layer.msg("很抱歉，绑定政委失败，请联系管理员！", {time: 3000, icon: 5});
                    }
                }
            },true);
            $('#evaluationModal').modal('hide');
        });

    },
    existsZwUserList: [],
    changeZwUserMap: {},
    renderUserPage: function (tt) {
        var rootDom = $("#evaluationModal");
        var listUser = "/deptZw/listUserByDeptAndRole?name=" + $("#nameQc").val().trim()+"&deptCode=ZW&roleCode=ZW&companyCode="+$(tt).attr("data-companyCode");
        rootDom.find("div[data-id='groups']").html(""); //清空上一次纪录
        rootDom.find('input[data-id="all"]').iCheck('uncheck');
        zwModalObj.requestData(null, listUser, "post", "json", true, function (data) {
            var userList = zwModalObj.groupBy(data, function (item) {
                return [item.deptId];
            });
            var html = template("excludePeopleHtml", {'data': userList}); //将用户数据渲染html
            rootDom.find("div[data-id='groups']").html(html);
            zwModalObj.reloadICheck(rootDom); //重新加载i-checks

            //部门复选框点击事件
            rootDom.find(".deptSpan").on('ifChecked', function () {
                var userInputArr = $(this).parent().parent().parent().next().find("input[type='checkbox']");
                if(userInputArr && userInputArr.length > 0){
                    $.each(userInputArr, function (index,t) {
                        $(t) .iCheck('check');
                        //如果已绑定列表中包含当前用户，则不记录
                        if(!zwModalObj.existsZwUserList.contains($(t).attr('userId'))){
                            zwModalObj.changeZwUserMap[$(t).attr('userId')] = "add";
                        }else {
                            delete zwModalObj.changeZwUserMap[$(t).attr('userId')];
                        }
                    })
                }

            });
            //部门复选框取消点击事件
            rootDom.find(".deptSpan").on('ifUnchecked', function () {
                var userInputArr = $(this).parent().parent().parent().next().find("input[type='checkbox']");
                if(userInputArr && userInputArr.length > 0){
                    $.each(userInputArr, function (index,t) {
                        $(t) .iCheck('uncheck');
                        //如果已绑定列表中包含当前用户，则标记删除
                        if(zwModalObj.existsZwUserList.contains($(t).attr('userId'))){
                            zwModalObj.changeZwUserMap[$(t).attr('userId')] = "del";
                        }else {
                            delete zwModalObj.changeZwUserMap[$(t).attr('userId')];
                        }
                    });
                }
            });
            //全选按钮点击事件
            rootDom.find('input[data-id="all"]').on('ifChecked', function () {
                var userInputArr = rootDom.find("div[data-id='groups']").find(".i-checks");
                if(userInputArr && userInputArr.length > 0){
                    $.each(userInputArr, function (index,t) {
                        $(t) .iCheck('check');
                        if($(t).attr('userId')){
                            //如果已绑定列表中包含当前用户，则不记录
                            if(!zwModalObj.existsZwUserList.contains($(t).attr('userId'))){
                                zwModalObj.changeZwUserMap[$(t).attr('userId')] = "add";
                            }else {
                                delete zwModalObj.changeZwUserMap[$(t).attr('userId')];
                            }
                        }
                    });
                }
            });
            //全选按钮取消点击事件
            rootDom.find('input[data-id="all"]').on('ifUnchecked', function () {
                var userInputArr = rootDom.find("div[data-id='groups']").find(".i-checks");
                if(userInputArr && userInputArr.length > 0){
                    $.each(userInputArr, function (index,t) {
                        $(t) .iCheck('uncheck');
                        //如果已绑定列表中包含当前用户，则标记删除
                        if(zwModalObj.existsZwUserList.contains($(t).attr('userId'))){
                            zwModalObj.changeZwUserMap[$(t).attr('userId')] = "del";
                        }else {
                            delete zwModalObj.changeZwUserMap[$(t).attr('userId')];
                        }
                    });
                }
            });
            //用户复选框点击事件
            $(".userSpan").on('ifClicked', function () {
                var flag = $(this).is(':checked');
                if(flag){  //如果是选中状态，则删除用户
                    //如果已绑定列表中包含当前用户，则标记删除
                    if(zwModalObj.existsZwUserList.contains($(this).attr('userId'))){
                        zwModalObj.changeZwUserMap[$(this).attr('userId')] = "del";
                    }else {
                        delete zwModalObj.changeZwUserMap[$(this).attr('userId')];
                    }
                }else{
                    //如果已绑定列表中包含当前用户，则不记录
                    if(!zwModalObj.existsZwUserList.contains($(this).attr('userId'))){
                        zwModalObj.changeZwUserMap[$(this).attr('userId')] = "add";
                    }else {
                        delete zwModalObj.changeZwUserMap[$(this).attr('userId')];
                    }
                }
            });

            //判断当前已选择页面
            if(zwModalObj.existsZwUserList && zwModalObj.existsZwUserList.length > 0){
                for(var key in zwModalObj.existsZwUserList){
                    $("#evaluationModal").find("div[data-id='groups'] input[userId='" + zwModalObj.existsZwUserList[key] + "']").iCheck("check");
                }
            }
        });
    },
    //人员分组
    groupBy: function (array, f) {
        var groups = {};
        array.forEach(function (o) {
            var group = JSON.stringify(f(o));
            groups[group] = groups[group] || [];
            groups[group].push(o);
        });
        return Object.keys(groups).map(function (group) {
            return groups[group];
        });
    },
    //重新渲染ichecks
    reloadICheck: function (root) {
        root.find('.i-checks').iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green'
        });
    },
}

//系统标题背景颜色
var sysTitleBackColorObj = {
    oldSysconfig: null,
    defaultSysConfig: {
        id: null,
        configTitle: "系统标题背景颜色",
        configType: "company_code",
        dataType: "json",
        configKey: "systemBackColorMap",
        configPattern: null,
        configValue: {},
        configDesc: "由于系统含有很多公司，有些用户可能有不同公司的账号，很容易搞混系统，为了方便区分，在系统顶部增加颜色区分，不同公司可配置不同颜色。",
        state: 0
    },
    init: function () {
        //请求配置的系统标题背景颜色数据
        if (!sysTitleBackColorObj.oldSysconfig) {
            zwModalObj.requestData({configKey: "systemBackColorMap"}, "/sysConfig/getOneConfigByKey", "get", "json", false, function (data) {
                sysTitleBackColorObj.oldSysconfig = data;
            });
        }
    },
    save: function (color, companyCode) {
        //如果有值则选中了，否则清除
        if (color) {
            sysTitleBackColorObj.defaultSysConfig.configValue[color] = companyCode;
            zwModalObj.requestData(JSON.stringify(sysTitleBackColorObj.defaultSysConfig), "/sysConfig/save", "post", "json", true, function (data) {
                if (data.code == 200) {
                    sysTitleBackColorObj.oldSysconfig = data.data.data;
                    layer.msg("系统标题背景颜色设置成功！", {time: 2000, icon: 6});
                } else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
                sysTitleBackColorObj.renderBackColor();//重新渲染系统背景颜色
            }, true);
        }
    },
    update: function (color, companyCode) {
        var systemBackColorMap = sysTitleBackColorObj.oldSysconfig.configValue || {};
        systemBackColorMap = typeof (systemBackColorMap) == "string" ? JSON.parse(systemBackColorMap) : systemBackColorMap;
        if (systemBackColorMap) {
            var existsKey = [];//已经使用的颜色
            var existsKeyValue = [];//已经使用的颜色
            var currentCompanyKey = null;
            var requestFlag = false;//是否发送请求，默认不发送
            for (var key in systemBackColorMap) {
                existsKey.push(key);
                existsKeyValue.push(key + "-" + systemBackColorMap[key]);
                if (systemBackColorMap[key] == companyCode) {
                    currentCompanyKey = key;//缓存当前配置公司的颜色
                }
            }
            //如果有值则选中了，否则清除
            if (color) {
                //判断颜色是否被使用
                if (existsKey.contains(color)) {
                    //判断颜色是否被自己使用还是其他使用，自己使用则不更新操作
                    var tmpKey = color + "-" + companyCode;
                    if (!existsKeyValue.contains(tmpKey)) {
                        layer.msg("该背景颜色已经被其他公司使用，请重新选择！", {time: 3000, icon: 5});
                        return;
                    }
                } else {
                    //如果公司有配置，删除原配置，重新添加
                    if (currentCompanyKey) {
                        delete systemBackColorMap[currentCompanyKey];
                    }
                    systemBackColorMap[color] = companyCode;
                    requestFlag = true;
                }
            } else {
                //如果公司有配置，则清空
                if (currentCompanyKey) {
                    delete systemBackColorMap[currentCompanyKey];
                    requestFlag = true;
                }
            }
            //发送请求
            if (requestFlag) {
                sysTitleBackColorObj.oldSysconfig.configValue = systemBackColorMap;
                zwModalObj.requestData(JSON.stringify(sysTitleBackColorObj.oldSysconfig), "/sysConfig/update", "post", "json", true, function (data) {
                    if (data.code == 200) {
                        layer.msg("系统标题背景颜色更新成功！", {time: 2000, icon: 6});
                    } else {
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }
                    sysTitleBackColorObj.renderBackColor();//重新渲染系统背景颜色
                }, true);
            } else {
                sysTitleBackColorObj.renderBackColor();//重新渲染系统背景颜色
            }
        } else {
            sysTitleBackColorObj.renderBackColor();//重新渲染系统背景颜色
        }
    },
    renderBackColor: function () {
        //js在iframe子页面获取父页面元素
        var topNavWrap = window.parent.document.getElementById("topNavWrap");
        var topNavHeader = window.parent.document.getElementById("topNavHeader");
        var userCompanyCode = user.companyCode;//获取当前用户公司
        var systemBackColorMap = sysTitleBackColorObj.oldSysconfig.configValue || {};
        systemBackColorMap = typeof (systemBackColorMap) == "string" ? JSON.parse(systemBackColorMap) : systemBackColorMap;
        var color = "";
        for (var key in systemBackColorMap) {
            if (systemBackColorMap[key] == "JT") {
                color = key;//缓存当前配置公司的颜色
            }
        }
        $(topNavWrap).css("background-color", color);
        $(topNavHeader).css("background-color", color);
    }
}