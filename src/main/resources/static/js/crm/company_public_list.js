var provinces = ["北京市", "北京", "天津市", "天津", "河北", "石家庄", "唐山", "秦皇岛", "邯郸", "邢台", "保定", "张家口", "承德", "沧州", "廊坊", "衡水", "山西", "太原", "大同", "阳泉", "长治", "晋城", "朔州", "晋中", "运城", "忻州", "临汾", "吕梁", "内蒙古自治区", "呼和浩特", "包头", "乌海", "赤峰", "通辽", "鄂尔多斯", "呼伦贝尔", "巴彦淖尔", "乌兰察布", "兴安盟", "锡林郭勒盟", "阿拉善盟", "辽宁", "沈阳", "大连", "鞍山", "抚顺", "本溪", "丹东", "锦州", "营口", "阜新", "辽阳", "盘锦", "铁岭", "朝阳", "葫芦岛", "吉林", "长春", "吉林", "四平", "辽源", "通化", "白山", "松原", "白城", "延边朝鲜族自治州", "黑龙江", "哈尔滨", "齐齐哈尔", "鸡西", "鹤岗", "双鸭山", "大庆", "伊春", "佳木斯", "七台河", "牡丹江", "黑河", "绥化", "大兴安岭地区", "上海市", "上海", "江苏", "南京", "无锡", "徐州", "常州", "苏州", "南通", "连云港", "淮安", "盐城", "扬州", "镇江", "泰州", "宿迁", "浙江", "杭州", "宁波", "温州", "嘉兴", "湖州", "绍兴", "金华", "衢州", "舟山", "台州", "丽水", "安徽", "合肥", "芜湖", "蚌埠", "淮南", "马鞍山", "淮北", "铜陵", "安庆", "黄山", "滁州", "阜阳", "宿州", "六安", "亳州", "池州", "宣城", "福建", "福州", "厦门", "莆田", "三明", "泉州", "漳州", "南平", "龙岩", "宁德", "江西", "南昌", "景德镇", "萍乡", "九江", "新余", "鹰潭", "赣州", "吉安", "宜春", "抚州", "上饶", "山东", "济南", "青岛", "淄博", "枣庄", "东营", "烟台", "潍坊", "济宁", "泰安", "威海", "日照", "莱芜", "临沂", "德州", "聊城", "滨州", "菏泽", "河南", "郑州", "开封", "洛阳", "平顶山", "安阳", "鹤壁", "新乡", "焦作", "濮阳", "许昌", "漯河", "三门峡", "南阳", "商丘", "信阳", "周口", "驻马店", "湖北", "武汉", "黄石", "十堰", "宜昌", "襄阳", "鄂州", "荆门", "孝感", "荆州", "黄冈", "咸宁", "随州", "恩施土家族苗族自治州", "湖南", "长沙", "株洲", "湘潭", "衡阳", "邵阳", "岳阳", "常德", "张家界", "益阳", "郴州", "永州", "怀化", "娄底", "湘西土家族苗族自治州", "广东", "广州", "韶关", "深圳", "珠海", "汕头", "佛山", "江门", "湛江", "茂名", "肇庆", "惠州", "梅州", "汕尾", "河源", "阳江", "清远", "东莞", "中山", "潮州", "揭阳", "云浮", "广西壮族自治区", "南宁", "柳州", "桂林", "梧州", "北海", "防城港", "钦州", "贵港", "玉林", "百色", "贺州", "河池", "来宾", "崇左", "海南", "海口", "三亚", "三沙", "儋州", "重庆市", "重庆", "四川", "成都", "自贡", "攀枝花", "泸州", "德阳", "绵阳", "广元", "遂宁", "内江", "乐山", "南充", "眉山", "宜宾", "广安", "达州", "雅安", "巴中", "资阳", "阿坝藏族羌族自治州", "甘孜藏族自治州", "凉山彝族自治州", "贵州", "贵阳", "六盘水", "遵义", "安顺", "毕节", "铜仁", "黔西南布依族苗族自治州", "黔东南苗族侗族自治州", "黔南布依族苗族自治州", "云南", "昆明", "曲靖", "玉溪", "保山", "昭通", "丽江", "普洱", "临沧", "楚雄彝族自治州", "红河哈尼族彝族自治州", "文山壮族苗族自治州", "西双版纳傣族自治州", "大理白族自治州", "德宏傣族景颇族自治州", "怒江傈僳族自治州", "迪庆藏族自治州", "西藏自治区", "拉萨", "日喀则", "昌都", "林芝", "山南", "那曲地区", "阿里地区", "陕西", "西安", "铜川", "宝鸡", "咸阳", "渭南", "延安", "汉中", "榆林", "安康", "商洛", "甘肃", "兰州", "嘉峪关", "金昌", "白银", "天水", "武威", "张掖", "平凉", "酒泉", "庆阳", "定西", "陇南", "临夏回族自治州", "甘南藏族自治州", "青海", "西宁", "海东", "海北藏族自治州", "黄南藏族自治州", "海南藏族自治州", "果洛藏族自治州", "玉树藏族自治州", "海西蒙古族藏族自治州", "宁夏回族自治区", "银川", "石嘴山", "吴忠", "固原", "中卫", "新疆维吾尔自治区", "乌鲁木齐", "克拉玛依", "吐鲁番", "哈密", "昌吉回族自治州", "博尔塔拉蒙古自治州", "巴音郭楞蒙古自治州", "阿克苏地区", "克孜勒苏柯尔克孜自治州", "喀什地区", "和田地区", "伊犁哈萨克自治州", "塔城地区", "阿勒泰地区", "台湾", "香港特别行政区", "澳门特别行政区"];
var keyWords = ["公司", "集团"];
$(document).ready(function () {

    var e = "<i class='fa fa-times-circle'></i> ";
    $("#queryForm").validate({
        rules: {
            companyName: {minlength: 2},
        },
        messages: {
            companyName: {minlength: e + "公司长度必须大于{0}个字符"},
        }
    });

    $("#queryBtn").click(function(){
        $("#query_table_logs").emptyGridParam();
        var companyName = $("#companyName").val().trim();
        if(provinces.indexOf(companyName) > -1 || keyWords.indexOf(companyName) > -1){
            layer.msg("该关键字不符合要求，请重新输入！");
            return;
        }
        gridObj.reloadCompany();
    });

    $('#queryDiv').on('keypress', function (event) {
        if (event.keyCode == "13") {
            $("#queryBtn").trigger("click");
        }
    });

    $("#query_table_logs").jqGrid({
        url: '/crm/company/listPublic',
        datatype: "local",
        mtype: 'POST',
        // postData: $("#queryForm").serializeJson(), //发送数据
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: true,
        //setLabel: "序号",
        autowidth: true,//自动匹配宽度
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: false,
        multiselectWidth: 25, //设置多选列宽度
        sortable: "true",
        sortname: "id",
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: false
        },
        colModel: [
            {
                name: 'companyId',
                index: 'companyId',
                label: 'companyId',
                editable: false,
                width: 30,
                align: "center",
                sortable: false,
                hidden: true
            },
            {
                name: 'companyName',
                index: 'companyName',
                label: '客户公司名称',
                editable: false,
                width: 270,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'standardize',
                index: 'standardize',
                label: '是否标准',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "<span>标准</span>";
                    } else {
                        return "<span class='text-red'>非标准</span>";
                    }
                }
            },
            {
                name: 'type',
                index: 'type',
                label: '客户类型',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "公司客户";
                    } else if(a == 0) {
                        return "个人客户";
                    } else {
                        return "";
                    }
                }
            },
            {
                name: 'companyUserId',
                index: 'companyUserId',
                label: '对接人',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                hidden: true
            },
            {
                name: 'companyUserName',
                index: 'companyUserName',
                label: '对接人',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string"
            },
            {
                name: 'normalize',
                index: 'normalize',
                label: '是否规范',
                editable: false,
                width: 90,
                align: "center",
                sortable: false,
                sorttype: "string",
                formatter: function (a, b, rowdata) {
                    if (a == 1) {
                        return "<span>规范</span>";
                    } else {
                        return "<span class='text-red'>非规范</span>";
                    }
                }
            },
            {
                name: 'operator',
                label: '操作',
                editable: false,
                width: 180,
                align: "center",
                sortable: false,
                formatter: function (a, b, rowdata) {
                    var html = "";
                    if(hasRoleYW()){
                        html += "<button class='btn btn-xs btn-success btn-outline' type='button' title='认领' onclick='companyObj.bind("+ rowdata.companyUserId + ");'>&nbsp;认领&nbsp;</button>";
                    }
                    return html;
                }
            }
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false,
        ondblClickRow: function (rowid, iRow, iCol, e) {
            // var rowData = jQuery("#query_table_logs").jqGrid("getRowData", rowid);
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            // view(rowid, 1);
        },
        gridComplete: function () {
            //在gridComplete调用合并方法
        }
    });
    resize("#query_pager_logs");
});

var gridObj = {
    reloadCompany:function (){
        $("#query_table_logs").jqGrid('setGridParam', {
            datatype: "json",
            postData: {companyName: $("#companyName").val().trim()}, //发送数据
        }).trigger("reloadGrid"); //重新载入
    },
};

var companyObj = {
    bind: function (companyUserId) {
        var lock = true;
        layer.confirm("确定要认领该客户？", {
            btn: ["确定", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            if(lock){
                lock = false;
                $.ajax({
                    type: "post",
                    url: baseUrl + "/crm/company/bind",
                    data: {companyUserId: companyUserId},
                    dataType: "json",
                    success: function (data) {
                        if(data.code == 200){
                            swal("操作成功");
                            gridObj.reloadCompany();
                        } else if(data.code == 1002){
                            swal({
                                title: "异常提示",
                                text: data.msg,
                            });
                        } else {
                            if (getResCode(data))
                                return;
                        }
                    },
                    error: function (data) {
                        swal(data.msg);
                    }
                });
            }
        }, function () {
            return;
        })
    },

};
