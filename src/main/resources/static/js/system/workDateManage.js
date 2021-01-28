$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var tableElement = $("#workDateTable");
        var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
        tableElement.setGridWidth(width);
    });

    createTable(); //表格定义
    tabFromObj.tabTableSelect(0); //Tab默认展示新增
    scheduleObj.init();//日历初始化
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
            Ladda.stopAll();//隐藏加载按钮
        }
    };
    if(contentType){
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
}

//时间区间隐藏显示
function showDate(val){
    if(val == 4){
        $("#dateFormGroup").show();
    }else{
        $("#dateFormGroup").hide();
        reflushTable();
    }
}

//获取表格标题
function getTableTitle() {
    var timeType = $("#timeQuantum").val();
    var result = "日期列表（本月）";
    if(timeType){
        if(timeType == 1){
            result = "日期列表（本周）";
        }else if(timeType == 2){
            result = "日期列表（本月）";
        }else if(timeType == 3){
            result = "日期列表（本年）";
        }else {
            var startDate = $("#startDate1").val();
            var endDate = $("#endDate1").val();
            if(startDate && endDate){
                result = "日期列表（"+startDate+" 至 "+endDate+"）";
            }else if (startDate) {
                result = "日期列表（自"+startDate+"起）";
            }else if (endDate){
                result = "日期列表（截止"+endDate+"）";
            }
        }
    }
    return result;
}

//表格定义
function createTable() {
    var $workDateTable = $("#workDateTable");
    $workDateTable.jqGrid({
        url: baseUrl + '/workDate/listWorkDate',
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
        rowNum: 15, //每页记录数
        rowList: [15, 30, 50,100],//每页记录数可选列表
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        // colNames: ['id','日期','dateType','日期类型','备注'],//表头
        colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
            {
                name: 'id',
                label: 'id',
                width: 30,
                hidden: true,
            },
            {
                name: 'workDate',
                label: '日期',
                width: 30,
                editable: true,
                sortable: true,
                formatter: function (v, options, row) {
                   return new Date(v).format("yyyy-MM-dd");
                }
            },
            {
                name: 'dateTypeValue',
                label: '日期类型',
                width: 30,
                hidden: true,
                formatter: function (v, options, row) {
                    return row.dateType;
                }
            },
            {
                name: 'dateType',
                label: '日期类型',
                width: 30,
                editable: true,
                sortable: true ,
                formatter: function (v, options, row) {
                    if(row.dateType == 1){
                        return "休息日";
                    }else if(row.dateType == 2){
                        return "法定节假日";
                    }else {
                        return "工作日";
                    }
                }
            },
            {
                name: 'createDate',
                label: '创建时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'updateDate',
                label: '更新时间',
                width: 30,
                editable: true,
                sortable: false
            },
            {
                name: 'remarks',
                label: '备注',
                editable: true,
                sortable: false
            },

        ],
        pager: "#workDateTablePaper",
        viewrecords: true,
        caption: getTableTitle(),
        hidegrid: false,
        gridComplete: function () {
            var width = $('#workDateTable').closest('.jqGrid_wrapper').width() || $(document).width();
            $('#workDateTable').setGridWidth(width);
        },
        onSelectRow: function(rowid,status,e){
           var rowData = $("#workDateTable").jqGrid('getRowData',rowid);//获取当前行的数据
            tabFromObj.tabTableSelect(1, {id:rowData.id,workDate:rowData.workDate,dateType:rowData.dateTypeValue, remarks:rowData.remarks});
       }
    });
    $workDateTable.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $workDateTable.setGridHeight(530);
}

//刷新表格
function reflushTable() {
    //刷新表格
    $("#workDateTable").emptyGridParam(); //清空历史查询数据
    $("#workDateTable").jqGrid("setCaption",getTableTitle()); //设置表格标题
    $("#workDateTable").jqGrid('setGridParam', {
        postData: $("#queryForm").serializeJson(), //发送数据
    }).trigger("reloadGrid"); //重新载入
}

//日历
var scheduleObj = {
    monthMap:{1:"一月",2:"二月",3:"三月",4:"四月",5:"五月",6:"六月",7:"七月",8:"八月",9:"九月",10:"十月",11:"十一月",12:"十二月"},
    weekMap:{1:"周日",2:"周一",3:"周二",4:"周三",5:"周四",6:"周五",7:"周六"},
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    currentDay: new Date().getFullYear() + "-" + ((new Date().getMonth() + 1) < 10 ? "0"+(new Date().getMonth() + 1) : (new Date().getMonth() + 1))
        + "-" + (new Date().getDate() < 10 ? "0"+new Date().getDate() : new Date().getDate()),
    init: function () {
        scheduleObj.getCalendar();
    },
    getCalendar: function () {
        var param = {year:scheduleObj.year, month:scheduleObj.month};
        requestData(param,"/workDate/getCalendar", "post","json", false,function (data) {
            var html = "";
            $.each(data, function (i,dayMap) {
                //dayFlag: -1-上个月日期、0-当前月日期、1-当前天日期、2-下个月日期
                var dayClass = (dayMap.dayFlag == -1 || dayMap.dayFlag == 2) ? "otherMonthDay" : "currentMonthDay";
                dayClass = dayMap.dayFlag == 1 ? "dateClick" : dayClass; //当天
                if((dayMap.week == 1 || dayMap.week == 7) && dayMap.dayFlag == 0 && dayMap.dayFlag != 1){ //如果是周末，并且当月日期，则字体红色
                    dayClass += " zmDay";
                }
                //setFlag: 是否有添加进入工作日表:0-未添加、1-已添加
                html += "<li data-id='"+dayMap.id+"' data-dateType='"+dayMap.dateType+"' data-remarks='"+(dayMap.remarks || "")+"' data-date='"+dayMap.date+"' data-monthCH='"+dayMap.monthCH+"' data-week='"+dayMap.week+"'>\n" +
                    "     <div onclick='scheduleObj.dateClick(this);' class=\"planCalenderDayText\">\n" +
                    "         <div class=\""+dayClass+"\">"+dayMap.day+"</div>\n";
                //日期类型： 0-工作日、1-休息日、2-法定节假日，默认周末休息
                if(dayMap.dateType == 1){
                    html += " <div class=\"jqDay\">\n" +
                        "       休\n" +
                        "     </div>";
                }
                if(dayMap.dateType == 2){
                    html += " <div class=\"jqDay\">\n" +
                        "       法\n" +
                        "     </div>";
                }
                html +=  "    </div></li>";
            });
            $("#calenderMonth").text(scheduleObj.monthMap[scheduleObj.month]);  //设置月
            $("#calenderYear").text(scheduleObj.year + "年"); //设置年
            $("#scheduleCalender").html(html); //设置日
        });
    },
    nextMonth:function () {
        scheduleObj.year = scheduleObj.month == 12 ? scheduleObj.year + 1 : scheduleObj.year;
        scheduleObj.month = scheduleObj.month == 12 ? 1 : scheduleObj.month + 1;
        scheduleObj.getCalendar();
    },
    lastMonth: function () {
        scheduleObj.year = scheduleObj.month == 1 ? scheduleObj.year - 1 : scheduleObj.year;
        scheduleObj.month = scheduleObj.month == 1 ? 12 : scheduleObj.month - 1;
        scheduleObj.getCalendar();
    },
    dateClick: function (t){
        var $div = $(t).parent().children("div:first-child").children("div:first-child");
        var selectFlag = false;
        if($div.hasClass("planCalenderDayActive")){ //如果是已选中状态，则取消选中
            selectFlag = true;
        }
        $(".planCalenderDayText div:first-child").removeClass("planCalenderDayActive");
        if(!selectFlag){
            $div.addClass("planCalenderDayActive");
        }
        //设置下方切换表单
        var id = $(t).parent().attr("data-id");
        var workDate = $(t).parent().attr("data-date"); //当前日期
        var dateType = $(t).parent().attr("data-dateType"); //日期类型
        var remarks = $(t).parent().attr("data-remarks") || ""; //日期类型
        if(id && !selectFlag){ //如果有值，并且是选中状态
            tabFromObj.tabTableSelect(1, {id:id,workDate:workDate,dateType:dateType, remarks:remarks});
        }else {
            tabFromObj.tabTableSelect(0, {workDate:workDate,dateType:dateType, remarks:remarks}, selectFlag);
        }
    },
}

//tab表单操作
var tabFromObj = {
    tabTableSelect: function (index, workDate, selectFlag) {
        $(".tabContent").css("display","none");
        $(".tabTitle").removeClass("layui-this");//移除tab选中
        $($(".tabTitle")[index]).addClass("layui-this");
        if(index == 0){ //范围编辑
            $("#rangEditFormDiv").css("display","block");
            $("#rangeEditForm")[0].reset();
        }
        if(index == 1){ //编辑
            $("#editFormDiv").css("display","block");
            $("#editForm")[0].reset();
            if(workDate){
                $("#id").val(workDate.id);
                $("#editWorkDate").val(workDate.workDate);
                $("#editDateType").val(workDate.dateType);
                $("#editRemarks").val(workDate.remarks);
            }
        }
        if(index == 2){ //默认值
            $("#defaultFormDiv").css("display","block");
            $("#defaultForm")[0].reset();
        }
    },
    batchEditWorkDate: function () {
        if (!$("#rangeEditForm").valid()) return;
        startModal("#batchEditBtn");
        requestData(JSON.stringify($("#rangeEditForm").serializeJson()), "/workDate/batchEdit", "post", "json", true, function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                swal({
                    title: "成功",
                    text: "范围编辑日期成功！",
                    type: "success"
                },function () {
                    $("#rangeEditForm")[0].reset();//重置表单数据
                    scheduleObj.getCalendar();//刷新日历
                    reflushTable(); //刷新表格
                });
            } else {
                swal({
                    title: "失败",
                    text: data.msg,
                    type: "error"
                });
            }
        },true);
    },
    eidtWorkDate: function () {
        if (!$("#editForm").valid()) return;
        startModal("#editBtn");
        requestData(JSON.stringify($("#editForm").serializeJson()), "/workDate/edit", "post", "json", true, function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                swal({
                    title: "成功",
                    text: "编辑日期成功！",
                    type: "success"
                },function () {
                    scheduleObj.getCalendar();//刷新日历
                    reflushTable(); //刷新表格
                });
            } else {
                swal({
                    title: "失败",
                    text: data.msg,
                    type: "error"
                });
            }
        },true);
    },
    initWorkDate: function () {
        if (!$("#defaultForm").valid()) return;
        //提示两次
        layer.confirm('您好，请确认重置的日期范围，重置成功后，原设置日期将使用默认规则，周一至周五为工作日，周末为休息日！', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function () {
            layer.confirm('您好，请再次确认重置日期范围，确认无误后请点击确定！', {
                btn: ['确定', '取消'], //按钮
                shade: false //不显示遮罩
            }, function () {
                layer.closeAll();
                startModal("#defaultBtn");
                requestData(JSON.stringify($("#defaultForm").serializeJson()), "/workDate/initBatchSave", "post", "json", true, function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        swal({
                            title: "成功",
                            text: "初始化日期成功！",
                            type: "success"
                        },function () {
                            $("#defaultForm")[0].reset();//重置表单数据
                            scheduleObj.getCalendar();//刷新日历
                            reflushTable(); //刷新表格
                        });
                    } else {
                        swal({
                            title: "失败",
                            text: data.msg,
                            type: "error"
                        });
                    }
                },true);
            }, function () {
            });
        }, function () {
        });
    }
}