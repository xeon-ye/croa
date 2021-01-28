$(function () {
    SupplierOption.showDetail();
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_accounts').setGridWidth(width);
    });
    $("#mediaTypeId").attr("data-value", SupplierOption.mediaTypeId);
    loadMediaModeType("#mediaTypeId");  //加载媒体板块类型
    if (SupplierOption.mediaTypeId > 0)
        $('#mediaTypeId').attr("disabled", true);  //设置板块不可更换
    if (SupplierOption.op == "create") {
        SupplierOption.supplierId = "1";
    } else if (SupplierOption.op == "edit") {
        //alert(SupplierOption.supplierId);
        //alert(SupplierOption.mediaTypeId);
        init();       //加载账户信息
    }
    //$("#custId").change();

    var width = window.screen.width;
    if (width < 1500) {
        $("textarea").addClass("c");
    }
});

function init() {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_accounts').setGridWidth(width);
    });
    $("#table_accounts").jqGrid({
        url: baseUrl + '/account/listPg',
        datatype: "json",
        postData: {typeQc: 2, companyId: SupplierOption.supplierId},
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
        multiselectWidth: 25, //设置多选列宽度
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        // multiselect: true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15,//每页显示记录数
        rowList: [10, 15, 25, 50],//分页选项，可以下拉选择每页显示记录数
        jsonReader: {
            root: "list", page: "pageNum", total: "pages",
            records: "total", repeatitems: false, id: "id"
        },
        colModel: [
            {
                name: 'id',
                label: 'id',
                editable: true,
                hidden: true,
                sorttype: "int",
                search: true
            },
            {
                name: 'type',
                label: '账户归属类型',
                editable: false,
                width: 30,
                align: "left",
                sorttype: "int",
                formatter: function (a, b, rowdata) {
                    var d = rowdata.type;
                    if (d == 0) {
                        return "未指定"
                    } else if (d == 1) {
                        return "<span style='color:#0922ff'>公司账户</span>"
                    } else if (d == 2) {
                        return "<span style='color:#fc180e'>供应商账户</span>"
                    } else if (d == 3) {
                        return "<span style='color:#1eb519'>客户账户</span>"
                    } else if (d == 4) {
                        return "<span style='color:#000000'>个人账户</span>"
                    }
                }
            },
            {
                name: 'companyName',
                label: '公司名称',
                editable: true,
                width: 40,
                align: "left"
            },
            {
                name: 'name',
                label: '账号名称',
                editable: true,
                width: 40,
                align: "left"
            },
            {
                name: 'bankNo',
                label: '银行账号',
                editable: true,
                width: 40,
                align: "left"
            },
            {
                name: 'bankName',
                label: '开户行名称',
                editable: true,
                width: 40,
                align: "left"

            },
            {
                name: 'phone',
                label: '联系电话',
                editable: true,
                width: 20,
                align: "left"
            },
            {
                name: 'creator',
                label: '账户登记者',
                editable: true,
                width: 20,
                align: "left"
            },
            // {
            //     name: 'option',
            //     label: '操作',
            //     editable: false,
            //     width: 30,
            //     align: "center",
            //     sortable: false,
            //     formatter:function (a,b,rowdata) {
            //         var html = "";
            //         //如果负责人是自己
            //         if(user.id == rowdata.creator){
            //             html += "<a href='javascript:DockingPeople.cancelBind("+rowdata.id+")' style='margin-right:3px;color:#337ab7'>解绑</a>";
            //         }
            //         if(user.id == rowdata.creator){
            //             if(rowdata.deleteFlag != 1){
            //                 html += "<a href='javascript:DockingPeople.stopOrOpen("+rowdata.id+",1)' style='margin-right:3px;color:#337ab7'>停用</a>";
            //             }else{
            //                 html += "<a href='javascript:DockingPeople.stopOrOpen("+rowdata.id+",0)' style='margin-right:3px;color:#337ab7'>启用</a>";
            //             }
            //         }
            //         if(!rowdata.creator){
            //             html += "<a href='javascript:DockingPeople.bind("+rowdata.id+")' style='margin-right:3px;color:#337ab7'>认领</a>";
            //             html += "<a href='javascript:DockingPeople.assign("+b.rowId+")' style='margin-right:3px;color:#337ab7;'>指派</a>";
            //         }
            //         return html;
            //     }
            // }
        ],
        pager: jQuery("#pager_accounts"),
        viewrecords: true,
        caption: "账户列表",
        add: false,
        edit: true,
        //addtext: 'Add',
        //edittext: 'Edit',
        hidegrid: false,
        loadComplete: function (data) {
            if (getResCode(data))
                return;
        },
        ondblClickRow: function (rowid, iRow, iCol, e) {
            //edit(rowid);
            // page('/account/edit?id=' + rowid, '账户编辑');
        },
    });
    // .navGrid('#pager_accounts',{add:true,edit:true,del:true,view:true, del:false},
    // {},// use default settings for edit
    // {}, // use default settings for add
    // {},  // delete instead that del:false we need this
    // {multipleSearch : true}, // enable the advanced searching
    // {closeOnEscape:true} /* allow the view dialog to be closed when user press ESC key*/
    // );
    //.navButtonAdd("#pager_accounts",{ caption:"新增账号", buttonicon:"ui-icon-newwin", onClickButton:function(){alert("Deleting Row");}, position: "last", title:"增加一个账号，同时将账号绑定在当前供应商/联系人名下", cursor: "pointer",id:"createAccount"});
    //.jqGrid('navButtonAdd',"#pager_accounts",{ caption:"del", buttonicon:"ui-icon-newwin", onClickButton:function(){alert("Deleting Row");}, position: "first"});
    //setNavGrid();
    $("#table_accounts").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $("#table_accounts").setSelection(4, true);
    //设置下面的按钮

    //$("#table_accounts").addOptionButton([{"text": "新增","href": "javascript:alertEdit('/crm/docking_edit?companyId=1&companyName=测试账号-公司名称','新增账户')"}],"dockingButtons");
    // $("#table_orders").jqGrid('navGrid', '#pager_orders', {
    //     edit: true,
    //     add: true,
    //     del: true,
    //     search: true
    // }, {
    //     height: 200,
    //     reloadAfterSubmit: true
    // });
    // $('#table_orders').setGridHeight(360);
}

//设置分页下面的操作按钮
function setNavGrid(var1, var2) {
    var op = var1 || {edit: true, add: true, del: true, search: false};
    var op2 = var2 || {height: 200, reloadAfterSubmit: true};
    $("#table_accounts").jqGrid('navGrid', "#pager_accounts", op, op2);
}

function onloadDockingPeople(t) {
    $.ajax({
        url: "/dockingPeople/listByCustId/" + $(t).val(),
        type: "get",
        dataType: "json",
        success: function (data) {
            $("#dockpeople").html("");
            $(data).each(function (i, d) {
                $("#dockpeople").append("<option value='" + d.id + "'>" + d.custName + "</option>");
            });
            var ids = $("#table_accounts").getDataIDs();
            var rowNum = ids.length;
            for (var i = 0; i < rowNum; i++) {
                $("#table_accounts").setCell(ids[i], "custName", $(t).find("option:selected").text());
                $("#table_accounts").setCell(ids[i], "dockingPeople", $("#dockpeople").find("option:selected").text());
                // $("#table_orders").setCell(ids[i], "taxPoint", 1);
            }
        }
    });
}

function setDockingPeople(t) {
    var ids = $("#table_accounts").getDataIDs();
    var rowNum = ids.length;
    for (var i = 0; i < rowNum; i++) {
        $("#table_accounts").setCell(ids[i], "dockingPeople", $(t).find("option:selected").text());
    }
}

function setTaxes(t) {
    var ids = $("#table_accounts").getDataIDs();
    var rowNum = ids.length;
    var tax = $(t).val();
    for (var i = 0; i < rowNum; i++) {
        var saleAmount = $("#table_accounts").getCell(ids[i], "saleAmount");//报价
        $("#table_accounts").setCell(ids[i], "taxPoint", tax);
        var taxes = parseFloat(saleAmount) * tax;//计算税金
        // console.log(parseFloat(saleAmount) + taxes);
        // console.log(taxes + '----------');
        $("#table_accounts").setCell(ids[i], 'taxes', taxes, {color: 'red'});
        $("#table_accounts").setCell(ids[i], "amount", (parseFloat(saleAmount) + taxes), {color: 'red'});//含税价
    }
}

function setTax(rowId, v) {
    alert($("#table_accounts").getCell(rowId, "taxPoint"));
    // $("#table_orders").setCell(rowId, "taxPoint", 123);
    $("#table_accounts").setCell(rowId, "taxPoint", "132");
}