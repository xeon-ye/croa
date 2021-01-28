$(document).ready(function () {
    getDept();
    $.jgrid.defaults.styleUI = 'Bootstrap';

    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });
    $("#queryForm select[name='stateQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#queryForm select[name='companyCodeQc']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#queryForm select[name='invoiceType']").change(function () {
        $("#querySearch").trigger("click");
    });
    $("#querySearch").click(function () {
        $("#query_table_logs").emptyGridParam();
        $("#query_table_logs").jqGrid('setGridParam', {
            postData: $("#queryForm").serializeJson(), //发送数据
        }).trigger("reloadGrid"); //重新载入
        calculationOfTotal();
    });

    calculationOfTotal();

    $("#query_table_logs").jqGrid({
        url: baseUrl + '/outgo/resetListPg',
        datatype: "json",
        mtype: 'POST',
        postData: $("#queryForm").serializeJson(), //发送数据
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
        multiselect: false,
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
        colModel: [
            {name: 'id', label: 'id', editable: true, width: 80,hidden:true},
            {name: 'code', label: '请款编号', editable: true, width: 80,
            formatter:function (value,grid,rows) {
               var  html ="";
                html += "<a href='#' onclick=linkLocation('/fee/queryOutgo?id="+rows.id+"&flag=1&type=2')>"+value+"</a>"
                return html;
            }
            },
            {name: 'deptName', label: '申请部门', editable: true, width: 80},
            {name: 'applyName', label: '申请人', editable: true, width: 80},
            {name: 'applyTime', label: '申请时间', editable: true, width: 80},
            {name: 'payTime', label: '出款时间', editable: true, width: 80,},
            {name: 'accountName', label: '收款人', editable: true, width: 80},
            {name: 'supplierName', label: '供应商公司', editable: true, width: 80},
            {name: 'outgoAmountSum', label: '合计成本', editable: true, hidden: false, width: 80},
            {name: 'actualCost', label: '请款成本', editable: true, width: 80},
            {name: 'applyAmount', label: '实际请款', editable: true, width: 80},
            {name: 'costEraseAmount', label: '请款成本抹零', editable: true,width: 80,
              formatter:function (value,grid,rows) {
                  return "<span style='color:green'>"+value+"</span>";
              }
            },
            {name: 'outgoEraseAmount', label: '实际请款抹零', editable: true,width: 80,
                formatter:function (value,grid,rows) {
                    return "<span style='color:green'>"+value+"</span>";
                }},
            {name: 'outgoTax', label: '税点', editable: true, width: 80,
                formatter:function(value,grid,rows){
                if(rows.invoiceFlag==1){
                    if (value!=null){
                        return parseFloat(value)*100;
                    }else {
                        return "" ;
                    }
                }else {
                    return "";
                }
                }
            },
            {name: 'taxAmount', label: '税金', editable: true,  width: 80,
                formatter:function(value,grid,rows){
                    if(rows.invoiceFlag==1){
                        return value;
                     }else {
                        return "";
                    }
                }},
            {name: 'invoiceRise', label: '进票抬头', editable: true, width: 80},
            {name: 'backfillTime', label: '回填时间', editable: true,  width: 80,
                formatter: function (value, grid, rows) {
                if (value){
                    return  new Date(value).format("yyyy-MM-dd hh:mm:ss");
                }else{
                    return "";
                }
            }
            },
            {name: 'invoiceType', label: '专/普票', editable: true, width: 80,
            formatter: function (v,grid,rows) {
                if (v){
                    switch (v){
                        case 1:
                            return "普票";
                        case 2:
                            return "专票";
                    }
                }else {
                    return "";
                }

            }
            },
            {name: 'invoiceTax', label: '票面税点', editable: true,  width: 80},
            {name: 'invoiceCode', label: '发票编号', editable: true, width: 80}
        ],
        pager: jQuery("#query_pager_logs"),
        viewrecords: true,
        caption: "",
        add: false,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
    });

});

//计算合计价格
function calculationOfTotal() {
    $.ajax({
        type:"post",
        data:$("#queryForm").serializeJson(),
        url:baseUrl+"/outgo/calculationOfTotal",
        dataType:"json",
        async:false,
        success:function (resDate) {
            if (resDate){
                $("#costCombined").text(fmtMoneyBringUnit(resDate.outgoamount) || 0);
                $("#costEraseAmountSum").text(fmtMoneyBringUnit(resDate.costEraseAmountSum) || 0);
                $("#outgoEraseAmountSum").text(fmtMoneyBringUnit(resDate.outgoEraseAmountSum) || 0);
                $("#taxesTotalSum").text(fmtMoneyBringUnit(resDate.taxesTotalSum) || 0);
                $("#actualRequestSum").text(fmtMoneyBringUnit(resDate.payAmount) || 0);
            }else {
                $("#tj").find(".text-danger").htmleditForm(0);
            }

        }
    })

    
}
//请款code 跳转
function linkLocation(href) {
    page(href,"请款管理");
}
//初始化业务部门
function getDept(){
    var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
    var currentCompanyQx = user.currentCompanyQx;//当前用户是否有公司权限，ZJ、ZJL、FZ
    var deptDiv = document.getElementById("deptDiv");
    //当前用户有公司或部门权限时，业务部门可选展示，公司管理者  并且 只允许财务 业务
    if(((currentDeptQx || currentCompanyQx || isZC()) && (user.dept.code == 'MJ'|| user.dept.code == 'GL')) || user.dept.code == 'CW'){
        deptDiv.style.display = 'block';
        $("#selDept").click(function () {
            $("#deptModal").modal({backdrop: "static"});
        });
        $('#treeview').treeview({
            data: [getTreeData(isZC())],
            onNodeSelected: function (event, data) {
                $("#companyCode1").val("");//每次选择时，先清空
                $("#deptId1").val("");//每次选择时，先清空
                $("#chooseDeptName").val(data.text);
                $("#deptModal").modal('hide');
                $("#deptId1").val(data.id);
                $("#companyCode1").val(data.companyCode);
                $("#businessUserId").empty();//初始化
                $("#businessUserId").append('<option value="">全部</option>');
            }
        });
        $("#cleanDept").click(function () {
            $("#companyCode1").val("");//清空
            $("#businessUserId").empty();//初始化
            $("#businessUserId").append('<option value="">全部</option>');
            $("#deptId1").val("");
            $("#chooseDeptName").val("");
        });
    }
}
//获取部门树数据
function getTreeData() {
    var deptTreeData = {};
    $.ajax({
        type: "POST",
        url: baseUrl + "/dept/listDeptAllMJ",
        dataType: "json",
        async: false,
        success: function (result) {
            var arrays = result.data.list;
            if (arrays != null && arrays.length > 0)
                deptTreeData = arrays[0];
        }
    });
    return deptTreeData;
}
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