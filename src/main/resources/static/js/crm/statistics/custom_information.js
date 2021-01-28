var DockingPeople = {
    init: function () {
        DockingPeople.setTransferDept();
    },
    //用来保存页码与每页选中的行的对应关系，所有页面的选中数据
    listMap: {},
    //取消绑定
    cancelBind: function (id) {
        commonDockingPeople.cancelBind(id, custGrid);
    },
    //认领
    bind: function (id) {
        commonDockingPeople.bind(id, custGrid);
    },
    //启用和停用
    stopOrOpen: function (id, bz) {
        commonDockingPeople.stopOrOpen(id, bz, custGrid);
    },
    //如果不传rowId，则是批量指派，如果传rowId，则根据行ID指派
    assign: function (rowId) {
        var totalList = custGrid.getAllPageSelected("dockingId");
        if (rowId) {
            totalList = [];
            totalList.push(custGrid.getOneRow(rowId));
        }
        if (totalList.length < 1) {
            layer.alert("请选择联系人");
            return;
        }
        for (var i = 0; i < totalList.length; i++) {
            if (totalList[i].worker) {
                layer.alert("您选择的联系人中已经包含有负责人的联系人，请重新选择");
                return;
            }
        }
        DockingPeople.transfer(totalList, 2);
    },
    //批量交接
    batchTransfer: function () {
        var totalList = custGrid.getAllPageSelected("dockingId");
        if (totalList.length < 1) {
            layer.alert("请选择联系人");
            return;
        }
        for (var i = 0; i < totalList.length; i++) {
            if (totalList[i].worker != user.id) {
                layer.alert("您选择的联系人中包含不是您负责的联系人，请重新选择");
                return;
            }
        }
        DockingPeople.transfer(totalList, 1);
    },
    //开始交接
    transfer: function (totalList, bz) {
        $("#transferCount").val(totalList.length);
        $("#transferWorker").val($(totalList[0].workerName).text());
        layer.open({
            type: 1,
            title: "客户批量交接",
            skin: 'layui-layer-rim', //加上边框
            area: ['740px', '380px'], //宽高
            content: $("#batchTransfer")
        });
        DockingPeople.totalList = totalList;
        DockingPeople.bz = bz;
    },
    //批量交接确认
    batchTransferq: function () {
        $("#transferCountq").text($("#transferCount").val());
        $("#transferWorkerq").text($("#transferWorker").val());
        $("#transferTransferWorkerq").text($("#transferTransferWorker option:selected").text());
        $("#transferCommentq").val($("#transferComment").val());
        layer.open({
            type: 1,
            title: "客户批量交接确认",
            skin: 'layui-layer-rim', //加上边框
            area: ['740px', '300px'], //宽高
            content: $("#batchTransferq")
        });
    },
    //执行交接或指派
    batchBindDocking: function (totalList) {
        var datas = [];
        for (var i = 0; i < totalList.length; i++) {
            var temp = {};
            temp.id = totalList[i].dockingId;
            temp.worker = totalList[i].worker;
            datas.push(temp);
        }
        var newWorker = $("#transferTransferWorker").val();
        var comment = $("#transferComment").val();
        $.ajax({
            url: baseUrl + "/cust/batchBindDocking",
            type: "POST",
            data: {newWorker: newWorker, comment: comment, datas: JSON.stringify(datas), bz: DockingPeople.bz},
            dataType: "json",
            async: false,
            success: function (respData) {
                layer.closeAll();
                layer.alert(respData.data.status);
                //重新加载表格
                custGrid.reflush();
            }
        });
    },
    //设置对接人
    setSelectPeople: function () {
        $("#transferTransferWorker").empty();
        var dept = $("#transferDepartment option:selected").val();
        Views.loadDeptUser(dept, "YW", "transferTransferWorker");
    },
    //设置交接部门
    setTransferDept: function () {
        Views.loadDept("transferDepartment");
        DockingPeople.setSelectPeople();
    }
};
// var createStart = {
//     elem: '#createStart',
//     format: 'YYYY/MM/DD hh:mm:ss',
//     min: laydate.now(), //设定最小日期为当前日期
//     max: '2099-06-16 23:59:59', //最大日期
//     istime: true,
//     istoday: false,
//     choose: function (datas) {
//         createEnd.min = datas; //开始日选好后，重置结束日的最小日期
//         createEnd.start = datas //将结束日的初始值设定为开始日
//     }
// };
// var createEnd = {
//     elem: '#createEnd',
//     format: 'YYYY/MM/DD hh:mm:ss',
//     min: laydate.now(),
//     max: '2099-06-16 23:59:59',
//     istime: true,
//     istoday: false,
//     choose: function (datas) {
//         createStart.max = datas; //结束日选好后，重置开始日的最大日期
//     }
// };
// laydate(createStart);
// laydate(createEnd);

var provinces = ["北京市", "北京", "天津市", "天津", "河北", "石家庄", "唐山", "秦皇岛", "邯郸", "邢台", "保定", "张家口", "承德", "沧州", "廊坊", "衡水", "山西", "太原", "大同", "阳泉", "长治", "晋城", "朔州", "晋中", "运城", "忻州", "临汾", "吕梁", "内蒙古自治区", "呼和浩特", "包头", "乌海", "赤峰", "通辽", "鄂尔多斯", "呼伦贝尔", "巴彦淖尔", "乌兰察布", "兴安盟", "锡林郭勒盟", "阿拉善盟", "辽宁", "沈阳", "大连", "鞍山", "抚顺", "本溪", "丹东", "锦州", "营口", "阜新", "辽阳", "盘锦", "铁岭", "朝阳", "葫芦岛", "吉林", "长春", "吉林", "四平", "辽源", "通化", "白山", "松原", "白城", "延边朝鲜族自治州", "黑龙江", "哈尔滨", "齐齐哈尔", "鸡西", "鹤岗", "双鸭山", "大庆", "伊春", "佳木斯", "七台河", "牡丹江", "黑河", "绥化", "大兴安岭地区", "上海市", "上海", "江苏", "南京", "无锡", "徐州", "常州", "苏州", "南通", "连云港", "淮安", "盐城", "扬州", "镇江", "泰州", "宿迁", "浙江", "杭州", "宁波", "温州", "嘉兴", "湖州", "绍兴", "金华", "衢州", "舟山", "台州", "丽水", "安徽", "合肥", "芜湖", "蚌埠", "淮南", "马鞍山", "淮北", "铜陵", "安庆", "黄山", "滁州", "阜阳", "宿州", "六安", "亳州", "池州", "宣城", "福建", "福州", "厦门", "莆田", "三明", "泉州", "漳州", "南平", "龙岩", "宁德", "江西", "南昌", "景德镇", "萍乡", "九江", "新余", "鹰潭", "赣州", "吉安", "宜春", "抚州", "上饶", "山东", "济南", "青岛", "淄博", "枣庄", "东营", "烟台", "潍坊", "济宁", "泰安", "威海", "日照", "莱芜", "临沂", "德州", "聊城", "滨州", "菏泽", "河南", "郑州", "开封", "洛阳", "平顶山", "安阳", "鹤壁", "新乡", "焦作", "濮阳", "许昌", "漯河", "三门峡", "南阳", "商丘", "信阳", "周口", "驻马店", "湖北", "武汉", "黄石", "十堰", "宜昌", "襄阳", "鄂州", "荆门", "孝感", "荆州", "黄冈", "咸宁", "随州", "恩施土家族苗族自治州", "湖南", "长沙", "株洲", "湘潭", "衡阳", "邵阳", "岳阳", "常德", "张家界", "益阳", "郴州", "永州", "怀化", "娄底", "湘西土家族苗族自治州", "广东", "广州", "韶关", "深圳", "珠海", "汕头", "佛山", "江门", "湛江", "茂名", "肇庆", "惠州", "梅州", "汕尾", "河源", "阳江", "清远", "东莞", "中山", "潮州", "揭阳", "云浮", "广西壮族自治区", "南宁", "柳州", "桂林", "梧州", "北海", "防城港", "钦州", "贵港", "玉林", "百色", "贺州", "河池", "来宾", "崇左", "海南", "海口", "三亚", "三沙", "儋州", "重庆市", "重庆", "四川", "成都", "自贡", "攀枝花", "泸州", "德阳", "绵阳", "广元", "遂宁", "内江", "乐山", "南充", "眉山", "宜宾", "广安", "达州", "雅安", "巴中", "资阳", "阿坝藏族羌族自治州", "甘孜藏族自治州", "凉山彝族自治州", "贵州", "贵阳", "六盘水", "遵义", "安顺", "毕节", "铜仁", "黔西南布依族苗族自治州", "黔东南苗族侗族自治州", "黔南布依族苗族自治州", "云南", "昆明", "曲靖", "玉溪", "保山", "昭通", "丽江", "普洱", "临沧", "楚雄彝族自治州", "红河哈尼族彝族自治州", "文山壮族苗族自治州", "西双版纳傣族自治州", "大理白族自治州", "德宏傣族景颇族自治州", "怒江傈僳族自治州", "迪庆藏族自治州", "西藏自治区", "拉萨", "日喀则", "昌都", "林芝", "山南", "那曲地区", "阿里地区", "陕西", "西安", "铜川", "宝鸡", "咸阳", "渭南", "延安", "汉中", "榆林", "安康", "商洛", "甘肃", "兰州", "嘉峪关", "金昌", "白银", "天水", "武威", "张掖", "平凉", "酒泉", "庆阳", "定西", "陇南", "临夏回族自治州", "甘南藏族自治州", "青海", "西宁", "海东", "海北藏族自治州", "黄南藏族自治州", "海南藏族自治州", "果洛藏族自治州", "玉树藏族自治州", "海西蒙古族藏族自治州", "宁夏回族自治区", "银川", "石嘴山", "吴忠", "固原", "中卫", "新疆维吾尔自治区", "乌鲁木齐", "克拉玛依", "吐鲁番", "哈密", "昌吉回族自治州", "博尔塔拉蒙古自治州", "巴音郭楞蒙古自治州", "阿克苏地区", "克孜勒苏柯尔克孜自治州", "喀什地区", "和田地区", "伊犁哈萨克自治州", "塔城地区", "阿勒泰地区", "台湾", "香港特别行政区", "澳门特别行政区"];


var gridObject = {
    url: baseUrl + '/cust/getAllDockingPeople',
    postData: $("#cust").serializeJson(),
    datatype: "local",
    mtype: "post",
    // data: mydata,
    height: "auto",
    page: 1,//第一页
    autowidth: true,
    rownumbers: true,
    gridview: true,
    viewrecords: true,
    multiselect: true,
    shrinkToFit: true,
    prmNames: {rows: "size"},
    rowNum: 10,
    rowList: [10, 20, 30],
    colNames: ['客户登记编号', '客户公司名称', '登记日期', '负责人', '单位企业项目名称', '成交详情', '意向度',
        '是否重复'],
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: false
    },
    colModel: [
        {
            name: 'id',
            index: 'id',
            editable: false,
            width: 30,
            align: "center",
            sortable: false,
            sorttype: "int",
            search: true,
            hidden: true
        },
        {
            name: 'custcompanyName',
            index: 'custcompanyName',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            sorttype: "string",
            search: true,
        },
        {
            name: 'create_time',
            index: 'create_time',
            editable: false,
            width: 30,
            align: "center",
            sortable: true,
            formatter: function (d) {
                return new Date(d).format("yyyy-MM-dd ");
            }
        },
        {
            name: 'create_worker_name',
            index: 'create_worker_name',
            editable: false,
            width: 90,
            align: "center",
            sortable: false,
            sorttype: "string",
        },
        {
            name: 'fproject',
            index: 'fproject',
            editable: false,
            width: 60,
            align: "center",
            sortable: false,
            sorttype: "string"
        },
        {
            name: 'success_detail',
            index: 'success_detail',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
        },
        {
            name: 'like_level',
            index: 'like_level',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
        },
        {
            name: 'repeat_flag',
            index: 'repeat_flag',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            sorttype: "string",
            formatter: function (d) {
                if (d == 1) {
                    return "<span style='color: red'>是</span>";
                } else {
                    return "<span>否</span>";
                    ;
                }
            }
        }
    ],
    /**
     * 翻页时保存当前页面的选中数据
     * @param pageBtn
     */
    onPaging: function (pageBtn) {
        //跨页面选择
        custGrid.setPageSelected("dockingId");
    },
    gridComplete: function () {
        var primaryKey = "id";
        custGrid.mergerCell('id', primaryKey);
        custGrid.mergerCell('companyName', primaryKey);
        custGrid.mergerCell('areaName', primaryKey);
        custGrid.mergerCell('product', primaryKey);
        custGrid.mergerCell('custUsers', primaryKey);
        custGrid.mergerCell('dockingPeople', primaryKey);
        //跨页面选择
        // custGrid.getPageSelectedSet("dockingId");
    },
    pager: "#custPager",
    viewrecords: true,
    caption: "保护客户列表",
    add: false,
    edit: false,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false
};

var gridObject1 = {
    url: baseUrl + '/cust/getAllDockingPeopleNotProtect',
    postData: $("#cust").serializeJson(),
    datatype: "local",
    mtype: 'post',
    // data: mydata,
    height: "auto",
    page: 1,//第一页
    autowidth: true,
    rownumbers: true,
    gridview: true,
    viewrecords: true,
    multiselect: true,
    shrinkToFit: true,
    prmNames: {rows: "size"},
    rowNum: 10,
    rowList: [10, 20, 30],
    colNames: ['客户登记编号', '客户公司名称', '登记日期', '负责人', "不建议理由", '单位企业项目名称', '成交详情', '意向度',
        '是否重复'],
    jsonReader: {
        root: "list", page: "pageNum", total: "pages",
        records: "total", repeatitems: false, id: false
    },
    colModel: [
        {
            name: 'id',
            index: 'id',
            editable: false,
            width: 30,
            align: "center",
            sortable: false,
            sorttype: "int",
            search: true,
            hidden: true
        },
        {
            name: 'custcompanyName',
            index: 'custcompanyName',
            editable: false,
            width: 100,
            align: "center",
            sortable: false,
            sorttype: "string",
            search: true,
        },
        {
            name: 'create_time',
            index: 'create_time',
            editable: false,
            width: 30,
            align: "center",
            sortable: true,
            formatter: function (d) {
                return new Date(d).format("yyyy-MM-dd ");
            }
        },
        {
            name: 'create_worker_name',
            index: 'create_worker_name',
            editable: false,
            width: 90,
            align: "center",
            sortable: false,
            sorttype: "string",
        },
        {
            name: 'reason',
            index: 'reason',
            editable: false,
            width: 90,
            align: "center",
            sortable: false,
            sorttype: "string",
        },
        {
            name: 'fproject',
            index: 'fproject',
            editable: false,
            width: 60,
            align: "center",
            sortable: false,
            sorttype: "string"
        },
        {
            name: 'success_detail',
            index: 'success_detail',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
        },
        {
            name: 'like_level',
            index: 'like_level',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
        },
        {
            name: 'repeat_flag',
            index: 'repeat_flag',
            editable: false,
            width: 80,
            align: "center",
            sortable: false,
            sorttype: "string",
            formatter: function (d) {
                if (d == 1) {
                    return "<span style='color: red'>是</span>";
                } else {
                    return "<span>否</span>";
                    ;
                }
            }
        }
    ],
    /**
     * 翻页时保存当前页面的选中数据
     * @param pageBtn
     */
    onPaging: function (pageBtn) {
        //跨页面选择
        custGrid1.setPageSelected("dockingId");
    },
    gridComplete: function () {
        var primaryKey = "id";
        custGrid1.mergerCell('id', primaryKey);
        custGrid1.mergerCell('companyName', primaryKey);
        custGrid1.mergerCell('areaName', primaryKey);
        custGrid1.mergerCell('product', primaryKey);
        custGrid1.mergerCell('custUsers', primaryKey);
        custGrid1.mergerCell('dockingPeople', primaryKey);
        //跨页面选择
        // custGrid.getPageSelectedSet("dockingId");
    },
    pager: "#custPager1",
    viewrecords: true,
    caption: "不建议跟进客户列表",
    add: false,
    edit: false,
    addtext: 'Add',
    edittext: 'Edit',
    hidegrid: false,
    ondblClickRow: function (rowid, iRow, iCol, e) {
        //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
        var rowData = jQuery("#custTable1").jqGrid("getRowData", rowid);
        view(rowData.id);
    }
};

function view(id) {
    $.get("/user/queryUserCust", {dockingPeople: id}, function (data) {
        $("#order").empty();
        $("#editModal").modal("toggle");
        for (var i = 0; i < data.data.list.length; i++) {
            var html = '<tr><td class="hide">' + data.data.list[i].userId + '</td>' +
                '<td>' + data.data.list[i].userName + '</td><td>' + data.data.list[i].reason + '</td><td>' + data.data.list[i].updateTime + '</td>';
            $("#order").append(html);
        }
    }, "json");
}

$(function () {
    $("#searchButton").click(function () {
        var jsonString = $.trim($("#custcompanyName").val());
        //判断输入的客户名称是否包含省市
        if(provinces.contains(jsonString)){
            //包含则屏蔽（清除上一次数据）
            $("#custTable").jqGrid("clearGridData");
            $("#custTable1").jqGrid("clearGridData");
            return;
        }
        $("#custTable").emptyGridParam();
        $("#custTable1").emptyGridParam();
        // var searchResult = $("#cust").serializeJson();
        // var jsonString = JSON.stringify(searchResult).replace(/{|}/g, '');
        if (jsonString.length>=2) {
            $("#custTable1").jqGrid('setGridParam', {
                datatype: "json",
                postData: $("#cust").serializeJson()
            }).trigger("reloadGrid");
            $("#custTable").jqGrid('setGridParam', {
                datatype: "json",
                postData: $("#cust").serializeJson()
            }).trigger("reloadGrid");
            // queryByCompanyName(jsonString);
        } else {
            layer.msg("客户公司名称至少输入两个字符");
            $("#custTable1").jqGrid('setGridParam', {
                datatype: "local",
                postData: $("#cust").serializeJson()
            }).trigger("reloadGrid");
            $("#custTable").jqGrid('setGridParam', {
                datatype: "local",
                postData: $("#cust").serializeJson()
            }).trigger("reloadGrid");
        }
    });
});

// 客户查询未保护客户查询提示（不区分公司）
// function queryByCompanyName(name) {
//     $.post("/cust/queryByCompanyName",{companyName:name},function (data) {
//         layer.alert(data.data.message);
//     },"json");
// }

// 数据导出；
function exportData() {
    var params = $("#cust").serializeJson();
    var ids = $("#custTable").jqGrid("getGridParam", "selarrrow");
    if (ids.length > 0) {
        params.ids = ids;
    }
    layer.msg("系统处理中，请稍候。");
    startModal("#exportButton");
    $.post(baseUrl + "/cust/dataExport", params, function (data) {
        Ladda.stopAll();
        if (data.data.message != null) {
            layer.msg(data.data.message, {time: 1000, shade: [0.7, '#393D49']});
        } else if (data.data.file != null) {
            window.location.href = data.data.file;
        } else {
            layer.msg(data.msg);
        }
    }, "json")
}
