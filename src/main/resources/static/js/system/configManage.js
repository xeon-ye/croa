$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#sysConfigTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });

    createTable();//创建表格
    searchForm.init(); //初始化条件
    searchForm.search();
});

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

//查询区域
var searchForm = {
    init: function () {
        layui.use('form', function(){
            layui.form.render('select');//layui重新渲染下拉列表
        });
    },
    search:function () {
        reflushTable();
    }
};

//表格定义
function createTable() {
    var $sysConfigTable = $("#sysConfigTable");
    $sysConfigTable.jqGrid({
        url: baseUrl + '/sysConfig/list',
        datatype: "json",
        postData: $("#queryForm").serializeJson(),
        mtype: 'post',
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: true,
        setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        sortorder: "asc", //排序方式：倒序，本例中设置默认按id倒序排序
        sortable: true,
        multiselect: false,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10, //每页记录数
        rowList: [10, 20, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                hidden: true,
            },
            {
                name: 'configTitle',
                label: '功能名称',
                width: 100,
                editable: true,
                sortable: false
            },
            {
                name: 'configType',
                label: '配置类型',
                width: 40,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    if(v == "company_code"){
                        return "公司代码";
                    }else if(v == "user_id"){
                        return "用户ID";
                    }else if(v == "dept_id"){
                        return "部门ID";
                    }else if(v == "dept_type"){
                        return "部门类型";
                    }else if(v == "dept_code"){
                        return "部门编码";
                    }else if(v == "role_type"){
                        return "角色类型";
                    }else if(v == "role_code"){
                        return "角色编码";
                    }else if(v == "media_plate"){
                        return "媒体板块";
                    }else if(v == "media_parent_plate"){
                        return "媒体父板块";
                    }else {
                        return "其他";
                    }
                }
            },
            {
                name: 'dataType',
                label: '数据类型',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'configKey',
                label: '配置项键',
                width: 40,
                editable: true,
                sortable: false
            },
            {
                name: 'configValue',
                label: '配置项值',
                width: 150,
                editable: true,
                sortable: false
            },
            {
                name: 'configPattern',
                label: '数据规则',
                width: 40,
                editable: true,
                sortable: false
            },
            {
                name: 'user.name',
                label: '创建人',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'createDate',
                label: '创建时间',
                width: 60,
                editable: true,
                sortable: false
            },
            {
                name: 'state',
                label: '状态',
                width: 30,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    if(v == 0){
                        return "<span style='color: green;'>启用</span>";
                    }else{
                        return "<span style='color: red;'>禁用</span>";
                    }
                }
            },
            {
                label: '操作',
                width: 60,
                editable: true,
                sortable: false,
                formatter: function (v, options, row) {
                    var html = "<a class='text-success' onclick='opConfigObj.editBtnClick("+JSON.stringify(row)+")'>编辑&nbsp;</a>";
                    if(row.state == 0){
                        html += "<a class='text-success' onclick='opConfigObj.disable("+row.id+")'>禁用&nbsp;</a>";
                    }else {
                        html += "<a class='text-success' onclick='opConfigObj.enable("+row.id+")'>启用&nbsp;</a>";
                    }
                    html += "<a class='text-success' onclick='opConfigObj.del("+row.id+")'>删除&nbsp;</a>";
                    return html;
                }
            },
        ],
        pager: "#sysConfigTablePaper",
        viewrecords: true,
        caption: '系统配置参数列表',
        hidegrid: false,
        gridComplete: function () {
            var width = $('#sysConfigTable').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#sysConfigTable').setGridWidth(width);
        }
    });
    $sysConfigTable.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    // $sysConfigTable.setGridHeight(500);
}

//刷新表格
function reflushTable() {
    //刷新表格
    $("#sysConfigTable").emptyGridParam(); //清空历史查询数据
    $("#sysConfigTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//维护系统配置
var opConfigObj = {
    lookIndex: null, //记录预览窗口是否打开，有值则表示打开了
    //缓存获取数据库的数据，用于生成下拉列表选项或者表格列表
    tableData:{company_code:[], user_id:[], dept_id:[], dept_type:[], dept_code:[], role_type:[], role_code:[], media_plate:[], media_parent_plate:[{name:'网络', value:1},{name:'新媒体', value:2}]},
    listTableData:function (configType) {
        if(configType != "other" && configType != "media_parent_plate" && opConfigObj.tableData[configType] && opConfigObj.tableData[configType].length < 1){
           requestData({configType: configType}, "/sysConfig/listTableData", "get", "json", false, function (data) {
               opConfigObj.tableData[configType] = data;
           });
        }
    },
    addBtnClick: function () {
        $("#modalTitle").text("添加系统配置");
        $("#id").val("");
        $("#addSysConfigFormData")[0].reset();
        $("#saveBtn").css("display", "inline-block");
        $("#updateBtn").css("display", "none");
        $("#addSysConfigFormData input[name='configPattern']").attr("readonly", "true");
        opConfigObj.configValueShow($("#addSysConfigFormData select[name='dataType']").val(),$("#addSysConfigFormData select[name='configType']").val())
        $("#addSysConfigModal").modal("toggle");
    },
    lookConfig: function () {
        //如果有值，说明已经打开了一个预览框，则不处理
        if(opConfigObj.lookIndex){
            return;
        }
        requestData(null, "/sysConfig/getAllConfig", "get", "json", true, function (data) {
            //由于日期类型为数字需要格式处理
            for(var k in data){
                if(data[k].dataType == 'date' && data[k].pattern){
                    data[k].value = new Date(data[k].value).format(data[k].pattern.replace(/H/g, "h"));
                }
            }
            $('#result').html(syntaxHighlight(data));
            opConfigObj.lookIndex = layer.open({
                type: 1,
                title: '有效参数【预览】',
                content: $("#jsonLook").html(),
                btn: [],
                area: ['680px', '460px'],
                shade: 0,
                shadeClose: true,
                resize: false,
                move: '.layui-layer-btn,.layui-layer-title',
                moveOut: true,
                cancel:function (index, layero) {
                    opConfigObj.lookIndex = null; //关闭窗口需要进行处理为空，不清空，表示预览窗口已打开
                }
            });
        });
    },
    addJsonValue: function (key, value) {
        $("#configJsonBody").append("<tr>\n" +
            "                            <td>\n" +
            "                                <input type=\"text\" value='"+(key || '')+"' onkeyup=\"value=value.replace(/[^A-Za-z0-9]/g,'')\" class=\"form-control jsonKey\"/>\n" +
            "                            </td>\n" +
            "                            <td>\n" +
            "                                <input type=\"text\" value='"+(value || '')+"' class=\"form-control jsonValue\"/>\n" +
            "                            </td>\n" +
            "                            <td>\n" +
            "                                <button class=\"btn btn-default\" type=\"button\" onclick=\"opConfigObj.removeJsonValue(this);\">\n" +
            "                                    <i class=\"fa fa-minus\"></i>\n" +
            "                                </button>\n" +
            "                            </td>\n" +
            "                        </tr>");
    },
    addSelJsonValue:function (list, key, value) {
        if(list && list.length > 0){
            var html = "";
            $.each(list, function (i, data) {
                if(value && data.value == value){
                    html += "<option value=\""+data.value+"\" selected>"+data.name+"</option>";
                }else {
                    html += "<option value=\""+data.value+"\">"+data.name+"</option>";
                }
            });
            $("#configJsonBody").append("<tr>\n" +
                "               <td>\n" +
                "                   <input type=\"text\" value='"+(key || '')+"' onkeyup=\"value=value.replace(/[^A-Za-z0-9]/g,'')\" class=\"form-control jsonKey\"/>\n" +
                "               </td>\n" +
                "               <td class=\"layui-form\">\n" +
                "                   <select class=\"form-control height18 jsonValue\" lay-search>\n" +
                "                       "+html+"\n" +
                "                   </select>\n" +
                "               </td>\n" +
                "               <td>\n" +
                "                   <button class=\"btn btn-default\" type=\"button\" onclick=\"opConfigObj.removeJsonValue(this);\">\n" +
                "                       <i class=\"fa fa-minus\"></i>\n" +
                "                   </button>\n" +
                "               </td>\n" +
                "           </tr>");
        }else {
            opConfigObj.addJsonValue();
        }
    },
    removeJsonValue:function (t) {
        $(t).closest("tr").remove();
    },
    renderJsonValue:function (configValue) {
        var configType = $("#addSysConfigFormData select[name='configType']").val();
        //如果配置数据来自数据库，则采用下拉列表形式
        if(configValue && Object.getOwnPropertyNames(configValue).length > 0){
            for(var k in configValue){
                if(configType != "other"){
                    opConfigObj.addSelJsonValue(opConfigObj.tableData[configType], k, configValue[k]);
                }else {
                    opConfigObj.addJsonValue(k, configValue[k]);
                }
            }
        }else {
            if(configType != "other"){
                opConfigObj.addSelJsonValue(opConfigObj.tableData[configType]);
            }else {
                opConfigObj.addJsonValue();
            }
        }
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render(); //先渲染，然后执行下面复选框事件才有效
        });
    },
    renderListValue:function (list, configValue) {
        configValue = configValue || [];
        if(list && list.length > 0){
            var html = "";
            $.each(list, function (i, data) {
                html += "<tr>\n" +
                    "        <td>\n" +
                    "            <input type=\"checkbox\"  "+(configValue.contains(data.value) ? "checked" : "")+" lay-skin=\"primary\" value='"+data.value+"'>\n" +
                    "        </td>\n" +
                    "        <td title=\""+(i+1)+"\">\n" +
                    "            <div style=\"width: 98%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                    "                "+(i+1)+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+data.value+"\">\n" +
                    "            <div class='listContent' style=\"width: 98%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                    "                "+data.value+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "        <td title=\""+data.name+"\">\n" +
                    "            <div style=\"width: 98%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                    "                "+data.name+"\n" +
                    "            </div>\n" +
                    "        </td>\n" +
                    "    </tr>";
            });
            $("#configListBody").html(html);
        }else {
            $("#configListValue").css("display", "none");
            $("#configValue").css("display", "block");
        }
    },
    renderRadioValue:function (list,configValue) {
        var html = "";
        $.each(list, function (i, data) {
            html += "<tr>\n" +
                "        <td>\n" +
                "            <input type=\"checkbox\" "+(configValue == data.value ? "checked" : "")+" value='"+data.value+"' lay-skin=\"primary\" lay-filter=\"radioInput\">\n" +
                "        </td>\n" +
                "        <td title=\""+(i+1)+"\">\n" +
                "            <div style=\"width: 98%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                "                "+(i+1)+"\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td title=\""+data.value+"\">\n" +
                "            <div class='listContent' style=\"width: 98%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                "                "+data.value+"\n" +
                "            </div>\n" +
                "        </td>\n" +
                "        <td title=\""+data.name+"\">\n" +
                "            <div style=\"width: 98%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;\">\n" +
                "                "+data.name+"\n" +
                "            </div>\n" +
                "        </td>\n" +
                "    </tr>";
        });
        $("#configRadioBody").html(html);

        layui.use('form', function(){
            var form = layui.form;
            form.render(); //先渲染，然后执行下面复选框事件才有效

            form.on('checkbox(radioInput)', function(data){
                if(data.elem.checked){
                    var child = $(data.elem).parents('table').find('tbody input[type="checkbox"]');
                    child.each(function(index, item){
                        item.checked = false;
                    });
                    data.elem.checked = true;
                    $("#configValue").val(data.value);
                }else {
                    $("#configValue").val("");
                }
                form.render('checkbox');
            });
        });
    },
    configValueShow:function (dataType, configType, configValue) {
        $("#configRadioBody").html("");
        $("#configValue").val(""); //重置
        $("#configListBody").html(""); //重置
        $("#configJsonBody").html(""); //重置
        opConfigObj.listTableData(configType); //加载数据
        //如果配置类型有值，并且数据类型为List，则展示列表选择
        if(dataType == "list"){
            $("#configRadioValue").css("display", "none");
            $("#configJsonValue").css("display", "none");
            if(configType != "other"){
                $("#configValue").css("display", "none");
                $("#configListValue").css("display", "block");
                opConfigObj.renderListValue(opConfigObj.tableData[configType], configValue);
            }else {
                $("#configValue").css("display", "block");
                $("#configValue").val(configValue || "");
                $("#configListValue").css("display", "none");
            }
        }else if(dataType == "json"){
            $("#configRadioValue").css("display", "none");
            $("#configValue").css("display", "none");
            $("#configJsonValue").css("display", "block");
            $("#configListValue").css("display", "none");
            opConfigObj.renderJsonValue(configValue);
        }else {
            $("#configJsonValue").css("display", "none");
            $("#configListValue").css("display", "none");
            if(configType != "other" && opConfigObj.tableData[configType] && opConfigObj.tableData[configType].length > 0){
                $("#configRadioValue").css("display", "block");
                $("#configValue").css("display", "none");
                opConfigObj.renderRadioValue(opConfigObj.tableData[configType], configValue);
            }else {
                $("#configRadioValue").css("display", "none");
                $("#configValue").css("display", "block");
                $("#configValue").val(configValue || "");
            }
        }
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render(); //先渲染，然后执行下面复选框事件才有效
        });
    },
    save: function (url) {
        if(!$("#addSysConfigFormData").valid()){
            return;
        }
        var jsonData = $("#addSysConfigFormData").serializeForm();
        if(jsonData.dataType == "json"){
            var trArr = $("#configJsonBody").find("tr");
            if(!trArr || trArr.length < 1){
                layer.msg("请添加配置项！", {time: 2000, icon: 5});
                return;
            }
            var json = {};
            var flag = true; //默认都填写表格信息
            $.each(trArr, function (i, $input) {
                if($($input).find(".jsonKey").val() && $($input).find(".jsonValue").val()){
                    json[$($input).find(".jsonKey").val()] = $($input).find(".jsonValue").val();
                }else {
                    flag = false;
                }
            });
            if(flag){
                jsonData.configValue = JSON.stringify(json);
            }else {
                layer.msg("Json的Key或Value不能为空！", {time: 2000, icon: 5});
                return;
            }
        }
        if(jsonData.dataType == "list"){
            //读取表格内容，需要判断配置类型是否有值
            if(opConfigObj.tableData[jsonData.configType] && opConfigObj.tableData[jsonData.configType].length > 0){
                //获取列表选中的行
                var checkboxArr = $("#configListBody").find("input[type='checkbox']:checked");
                if(!checkboxArr || checkboxArr.length < 1){
                    layer.msg("请选择配置项值！", {time: 2000, icon: 5});
                    return;
                }
                var valArr = [];
                $.each(checkboxArr, function (i, $input) {
                    valArr.push($($input).val());
                });
                jsonData.configValue = valArr.join(jsonData.configPattern || ",");
            }
        }
        if(!jsonData.configValue){
            layer.msg("请填入配置项值！", {time: 2000, icon: 5});
            return;
        }
        var pattern = "";
        if(jsonData.dataType == "int"){
            pattern = /^(-?\d+)?$/;
        }else if(jsonData.dataType == "float" || jsonData.dataType == "double"){
            pattern = /^(-?\d+)(\.\d+)?$/;
        }
        if(pattern){
            if(!pattern.test(jsonData.configValue)){
                layer.msg("配置项值格式不满足数据类型要求！", {time: 2000, icon: 5});
                return;
            }
        }
        startModal("#saveBtn");
        startModal("#updateBtn");
        requestData(JSON.stringify(jsonData), url, "post", "json", true, function (data) {
            if(data.code == 200){
                $("#addSysConfigModal").modal("toggle");
            }
            opConfigObj.validResData(data, "配置参数成功！");
        },true);
        Ladda.stopAll();
    },
    editBtnClick:function (config) {
        $("#modalTitle").text("修改系统配置");
        $("#addSysConfigFormData")[0].reset();
        $("#configRadioBody").html("");
        $("#configValue").val(""); //重置
        $("#configListBody").html(""); //重置
        $("#configJsonBody").html(""); //重置
        $("#saveBtn").css("display", "none");
        $("#updateBtn").css("display", "inline-block");
        //页面值渲染
        if(config && Object.getOwnPropertyNames(config).length > 0){
            for(var k in config){
                if(k != "configValue"){
                    $("#addSysConfigFormData input[name='"+k+"']").val(config[k]);
                    $("#addSysConfigFormData select[name='"+k+"']").val(config[k]);
                    $("#addSysConfigFormData textarea[name='"+k+"']").val(config[k]);
                }else{
                    var configValue = config[k];
                    if(config["dataType"] == "list"){
                        configValue = configValue.split(config["configPattern"] || ",");
                        $("#addSysConfigFormData input[name='configPattern']").removeAttr("readonly");
                    }else{
                        if(config["dataType"] == "json"){
                            configValue = JSON.parse(configValue);
                        }
                        $("#addSysConfigFormData input[name='configPattern']").attr("readonly", "true");
                    }
                    opConfigObj.configValueShow(config["dataType"],config["configType"], configValue);
                }
            }

        }
        //使用layui表单
        layui.use('form', function(){
            var form = layui.form;
            form.render(); //先渲染，然后执行下面复选框事件才有效
        });
        $("#addSysConfigModal").modal("toggle");
    },
    enable: function (id) {
        requestData({id:id}, "/sysConfig/enable", "get", "json", true, function (data) {
            opConfigObj.validResData(data, "配置参数启用成功！");
        });
    },
    disable:function (id) {
        requestData({id:id}, "/sysConfig/disable", "get", "json", true, function (data) {
            opConfigObj.validResData(data, "配置参数禁用成功！");
        });
    },
    del:function (id) {
        requestData({id:id}, "/sysConfig/del", "get", "json", true, function (data) {
            opConfigObj.validResData(data, "配置参数删除成功！");
        });
    },
    validResData: function (data, message) {
        if (data.code == 200) {
            swal({
                title: "成功",
                text: message,
                type: "success"
            },function () {
                reflushTable(); //刷新表格
            });
        } else {
            swal({
                title: "失败",
                text: data.msg,
                type: "error"
            });
        }
    }
}

//格式化JSON数据成HTML字符串
function syntaxHighlight(json) {
    if (typeof json != 'string') {
        json = JSON.stringify(json, undefined, 2);
    }
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}
