$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        $('#table_suppliers').setGridWidth($('#table_suppliers').closest('.jqGrid_wrapper').width());
    });

    mediaSupplierManageObj.mediaAllPlateList();
    mediaSupplierManageObj.init();
    mediaSupplierManageObj.userMediaPlateList();
});

//供应的管理
var mediaSupplierManageObj = {
    mediaUserList: [],
    mediaUserMap: {},
    mediaUserPlateMap: [],
    checkSupplierUrl: "/supplier/checkSupplier",
    addSupplierUrl: "/supplier/addSupplier",
    editSupplierUrl: "/supplier/editSupplier",
    editSupplierCompanyUrl: "/supplier/editSupplierCompany",
    delSupplierUrl: "/supplier/del",
    supplierListUrl: "/supplier/listall",
    accountListUrl: "/supplier/supplierAccountList",
    addAccountUrl: "/account/add",
    editAccountUrl: "/account/edit",
    delAccountUrl: "/account/del",
    userMediaPlateList: function () {
        $.ajax({
            url: baseUrl + "/mediaPlate/userId",  //mediaType/listByUserId
            data: {"userId": user.id},
            type: "post",
            dataType: "json",
            success: function (data) {
                if (data) {
                    for (var i = 0; i < data.length; i++) {
                        mediaSupplierManageObj.mediaUserPlateMap.push(data[i].id)
                    }
                }
            }
        });
    },
    mediaAllPlateList: function () {
        $.ajax({
            type: "post",
            url: "/mediaPlate/mediaAllPlateList",
            dataType: "json",
            success: function (data) {
                if (data.data.list && data.data.list.length > 0) {
                    layui.use('form', function () {
                        var form = layui.form;
                        form.on('select(mediaPlateId)', function (data) {
                            mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
                        });

                        form.render('select');
                    });
                    var html = "";
                    $.each(data.data.list, function (i, data) {
                        html += "<option value=\"" + data.id + "\">" + data.name + "</option>";
                    })
                    $("#mediaPlateId").append(html);
                }
            }
        })
    },
    createModalTable: function () {
        //供应商列表
        $("#table_suppliers").jqGrid({
            url: baseUrl + mediaSupplierManageObj.supplierListUrl,
            datatype: "json",
            mtype: 'POST',
            postData: $("#supplier").serializeJson(), //发送数据
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
            multiselect: false,
            multiselectWidth: 25, //设置多选列宽度
            sortable: "true",
            sortname: "update_time",
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 10,//每页显示记录数
            rowList: [10, 20, 25, 50],//分页选项，可以下拉选择每页显示记录数
            jsonReader: {
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: "id"
            },
            colModel: [
                {name: 'id', label: 'id', editable: false, width: 60, hidden: true},
                {name: 'supplierNature', label: 'supplierNature', editable: false, width: 60, hidden: true},
                {name: 'standarCompanyFlag', label: 'standarCompanyFlag', editable: false, width: 60, hidden: true},
                {name: 'standarPhoneFlag', label: 'standarPhoneFlag', editable: false, width: 60, hidden: true},
                {name:'flag',label:'flag',hidden: true},
                {name: 'contactorDesc', label: 'contactorDesc', editable: false, width: 60, hidden: true},
                {
                    name: 'userId', label: '登记者', editable: true, width: 100, hidden: true,
                    formatter: function (value, grid, rows) {
                        return rows.creator;
                    }
                },
                {
                    name: 'supplierNatureName',
                    label: '供应商性质',
                    editable: false,
                    width: 80,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if (rows.supplierNature == 1) {
                            return "个体供应商";
                        } else {
                            return "企业供应商";
                        }
                    }
                },
                {name: 'name', label: '供应商公司名称', editable: true, width: 180, sortable: false},
                {
                    name: 'standarCompanyFlagName',
                    label: '是否标准公司',
                    editable: false,
                    width: 80,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if (rows.standarCompanyFlag == 1) {
                            return "<span style='color: green;'>标准</span>";
                        } else {
                            return "<span style='color: red;'>非标准</span>";
                        }
                    }
                },
                {name: 'contactor', label: '供应商联系人', editable: true, width: 100, sortable: false},
                {
                    name: 'standarPhoneFlagName',
                    label: '是否规范联系人',
                    editable: false,
                    width: 100,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if (rows.standarPhoneFlag == 1) {
                            return "<span style='color: green;'>规范</span>";
                        } else {
                            return "<span style='color: red;'>不规范</span>";
                        }
                    }
                },
                {
                    name: 'phone',
                    label: '手机号',
                    editable: true,
                    width: 100,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        var flag = false;
                        var plateIds = rows.plateIds.split(",");
                        if (plateIds) {
                            for (var i = 0; i < plateIds.length; i++) {
                                if (mediaSupplierManageObj.mediaUserPlateMap.contains(plateIds[i])) {
                                    //当前用户的板块包含了该供应商的板块
                                    flag = true;
                                }
                            }
                        }
                        //1、仅责任人自己能看到   或者拥有板块的 组、部 、 总监
                        if ((user.id == rows.creator) || (flag && rows.flag)) {
                            return value || '';
                        } else {
                            if (value.length >= 11) {
                                var start = value.length > 11 ? "*****" : "****";
                                return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                            } else if (value.length >= 3) {
                                return value[0] + "***" + value[value.length - 1];
                            } else {
                                return "**";
                            }
                        }
                    }
                },
                {
                    name: 'qqwechat',
                    label: '微信号',
                    editable: true,
                    width: 100,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        var flag = false;
                        var plateIds = rows.plateIds.split(",");
                        if (plateIds) {
                            for (var i = 0; i < plateIds.length; i++) {
                                if (mediaSupplierManageObj.mediaUserPlateMap.contains(plateIds[i])) {
                                    //当前用户的板块包含了该供应商的板块
                                    flag = true;
                                }
                            }
                        }
                        //1、仅责任人自己能看到
                        if ((user.id == rows.creator) ||(flag && rows.flag)) {
                            return value || '';
                        } else {
                            if (value.length >= 11) {
                                var start = value.length > 11 ? "*****" : "****";
                                return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                            } else if (value.length >= 3) {
                                return value[0] + "***" + value[value.length - 1];
                            } else {
                                return "**";
                            }
                        }
                    }
                },
                {
                    name: 'qq',
                    label: 'QQ号',
                    editable: true,
                    width: 100,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        var flag = false;
                        var plateIds = rows.plateIds.split(",");
                        if (plateIds) {
                            for (var i = 0; i < plateIds.length; i++) {
                                if (mediaSupplierManageObj.mediaUserPlateMap.contains(plateIds[i])) {
                                    //当前用户的板块包含了该供应商的板块
                                    flag = true;
                                }
                            }
                        }
                        //1、仅责任人自己能看到
                        if ((user.id == rows.creator) || (flag && rows.flag)) {
                            return value || '';
                        } else {
                            if (value.length >= 11) {
                                var start = value.length > 11 ? "*****" : "****";
                                return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                            } else if (value.length >= 3) {
                                return value[0] + "***" + value[value.length - 1];
                            } else {
                                return "**";
                            }
                        }
                    }
                },

                {
                    name: 'creator', label: '责任人', editable: true, width: 60, sortable: false,
                    formatter: function (value, grid, rows) {
                        return rows.user.userName;
                    }
                },
                {
                    name: 'createTime', label: '登记时间', editable: true, width: 120, sortable: false
                },
                {
                    name: 'updateTime', label: '更新时间', editable: true, width: 120, sortable: false
                },
                {
                    name: 'state',
                    label: '状态',
                    width: 60,
                    align: "center",
                    sortable: false,
                    formatter: function (v, options, row) {
                        if (row.state == 1) {
                            return "<span style='color: red;'>禁用</span>";
                        } else {
                            return "<span style='color: green;'>启用</span>";
                        }
                    }
                },
                {
                    name: 'operate', label: "操作", index: '', width: 200, sortable: false,
                    formatter: function (value, grid, rows, state) {
                        var html = "";
                        //媒介总监可以指派
                        if (mediaSupplierManageObj.isMJZJ()) {
                            html += "<button style='margin-top: 3px;' class='btn btn-xs btn-success btn-outline' type='button' title='指派' onclick='mediaSupplierManageObj.supplierAssign(" + JSON.stringify(rows) + ", true);'>指派</button>&nbsp;";
                        }

                        //如果是企业用户，则可以编辑公司名称
                        if (rows.supplierNature != 1) {
                            if (rows.creator == user.id) {
                                html += "<button style='margin-top: 3px;' class='btn btn-xs btn-success btn-outline' type='button' title='编辑公司' onclick='mediaSupplierManageObj.editSupplierCompanyClick(" + JSON.stringify(rows) + ", 1);'>编辑公司</button>&nbsp;";
                            } else {
                                html += "<button style='margin-top: 3px;' class='btn btn-xs btn-success btn-outline' type='button' title='查看公司' onclick='mediaSupplierManageObj.editSupplierCompanyClick(" + JSON.stringify(rows) + ", 0)'>查看公司</button>&nbsp;";
                            }
                        }

                        // 都可以拥有修改
                        html += "<button style='margin-top: 3px;' class='btn btn-xs btn-success btn-outline' type='button' title='编辑' onclick='mediaSupplierManageObj.editSupplierClick(" + JSON.stringify(rows) + ", true);'>编辑</button>&nbsp;";

                        //当前组长和部长可以删除组员的供应商，启用和禁用同理
                        if (rows.creator == user.id) {
                            if (rows.state == 0) {
                                html += "<button style='margin-top: 3px;' class='btn btn-xs btn-danger btn-outline' type='button' title='禁用' onclick='mediaSupplierManageObj.changeSupplierState(this, \"stop\"," + rows.id + ")'>禁用</button>&nbsp;";
                            } else {
                                html += "<button style='margin-top: 3px;' class='btn btn-xs btn-success btn-outline' type='button' title='启用' onclick='mediaSupplierManageObj.changeSupplierState(this, \"active\"," + rows.id + ")'>启用</button>&nbsp;";
                            }
                            html += "<button style='margin-top: 3px;' class='btn btn-xs btn-danger btn-outline' type='button' title='删除' onclick='mediaSupplierManageObj.delSupplier(" + rows.id + ")'>删除</button>&nbsp;";
                        }
                        html += "<button style='margin-top: 3px;' class='btn btn-xs btn-success btn-outline' type='button' title='异动详情' onclick='supplierChangeObj.supplierChangeShow(" + rows.id + ", " + rows.creator + ")'>异动详情</button>&nbsp;";
                        return html;
                    }
                }
            ],
            pager: jQuery("#pager_suppliers"),
            viewrecords: true,
            caption: "",
            add: false,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false,
            ondblClickRow: function (rowid, iRow, iCol, e) {
                var rowData = $("#table_suppliers").jqGrid("getRowData", rowid);   //获取选中行信息
                mediaSupplierManageObj.editSupplierClick(rowData, false);//查看
            },
        });
        $("#table_suppliers").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
        commonObj.setNavGrid("table_suppliers", "pager_suppliers");


        //供应商公司编辑联系人列表
        $("#supplierContactorTable").jqGrid({
            url: baseUrl + mediaSupplierManageObj.supplierListUrl,
            datatype: "json",
            mtype: 'POST',
            altRows: true,
            altclass: 'bgColor',
            height: "auto",
            page: 1,//第一页
            rownumbers: true,
            //setLabel: "序号",
            gridview: true, //加速显示
            cellsubmit: "clientArray",
            viewrecords: true,  //显示总记录数
            multiselect: false,
            multiselectWidth: 25, //设置多选列宽度
            sortable: "true",
            sortname: "id",
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            autowidth: true,
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 10,//每页显示记录数
            rowList: [10, 20, 25, 50],//分页选项，可以下拉选择每页显示记录数
            jsonReader: {
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: "id"
            },
            colModel: [
                {name: 'id', label: 'id', editable: false, width: 60, hidden: true},
                {name: 'supplierNature', label: 'supplierNature', editable: false, width: 60, hidden: true},
                {name: 'standarCompanyFlag', label: 'standarCompanyFlag', editable: false, width: 60, hidden: true},
                {name: 'standarPhoneFlag', label: 'standarPhoneFlag', editable: false, width: 60, hidden: true},
                {name: 'name', label: 'name', editable: false, width: 60, hidden: true},
                {
                    name: 'userId', label: '登记者', editable: true, width: 100, hidden: true,
                    formatter: function (value, grid, rows) {
                        return rows.creator;
                    }
                },
                {
                    name: 'contactor',
                    index: 'contactor',
                    label: '联系人',
                    editable: false,
                    width: 100,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'standarPhoneFlagName',
                    label: '是否规范联系人',
                    editable: false,
                    width: 100,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        if (rows.standarPhoneFlag == 1) {
                            return "<span style='color: green;'>规范</span>";
                        } else {
                            return "<span style='color: red;'>不规范</span>";
                        }
                    }
                },
                {
                    name: 'phone',
                    label: '联系电话',
                    editable: true,
                    width: 100,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        //1、仅责任人自己能看到
                        if (user.id == rows.creator) {
                            return value || '';
                        } else {
                            if (value.length >= 11) {
                                var start = value.length > 11 ? "*****" : "****";
                                return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                            } else if (value.length >= 3) {
                                return value[0] + "***" + value[value.length - 1];
                            } else {
                                return "**";
                            }
                        }
                    }
                },
                {
                    name: 'qqwechat',
                    label: '微信号',
                    editable: true,
                    width: 100,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        //1、仅责任人自己能看到
                        if (user.id == rows.creator) {
                            return value || '';
                        } else {
                            if (value.length >= 11) {
                                var start = value.length > 11 ? "*****" : "****";
                                return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                            } else if (value.length >= 3) {
                                return value[0] + "***" + value[value.length - 1];
                            } else {
                                return "**";
                            }
                        }
                    }
                },
                {
                    name: 'qq',
                    label: 'QQ号',
                    editable: true,
                    width: 100,
                    sortable: false,
                    formatter: function (value, grid, rows) {
                        //1、仅责任人自己能看到
                        if (user.id == rows.creator) {
                            return value || '';
                        } else {
                            if (value.length >= 11) {
                                var start = value.length > 11 ? "*****" : "****";
                                return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                            } else if (value.length >= 3) {
                                return value[0] + "***" + value[value.length - 1];
                            } else {
                                return "**";
                            }
                        }
                    }
                },
                {
                    name: 'creator', label: '责任人', editable: true, width: 60, sortable: false,
                    formatter: function (value, grid, rows) {
                        return rows.user.userName;
                    }
                },
                {
                    name: 'state',
                    label: '状态',
                    width: 60,
                    align: "center",
                    sortable: false,
                    formatter: function (v, options, row) {
                        if (row.state == 1) {
                            return "<span style='color: red;'>禁用</span>";
                        } else {
                            return "<span style='color: green;'>启用</span>";
                        }
                    }
                },
                {
                    name: 'operate', label: "操作", index: '', width: 150, sortable: false,
                    formatter: function (value, grid, rows, state) {
                        var sName = rows.name;
                        var html = "";
                        //当前组长和部长可以删除组员的供应商，启用和禁用同理
                        if (rows.creator == user.id) {
                            html += "<button data-id='" + rows.id + "' data-supplierNature='" + rows.supplierNature + "' data-supplierName='" + rows.name + "' data-contactor='" + rows.contactor + "' data-modalId='mediaSupplierCompanyModal' class='btn btn-xs btn-success btn-outline' type='button' title='添加账户' onclick='mediaSupplierManageObj.opAccountClick(this);'>添加账户</button>&nbsp;";
                            if (rows.state == 0) {
                                html += "<button data-modalId='mediaSupplierCompanyModal' class='btn btn-xs btn-danger btn-outline' type='button' title='禁用' onclick='mediaSupplierManageObj.changeSupplierState(this, \"stop\"," + rows.id + ")'>禁用</button>&nbsp;";
                            } else {
                                html += "<button data-modalId='mediaSupplierCompanyModal' class='btn btn-xs btn-success btn-outline' type='button' title='启用' onclick='mediaSupplierManageObj.changeSupplierState(this, \"active\"," + rows.id + ")'>启用</button>&nbsp;";
                            }
                            html += "<button class='btn btn-xs btn-danger btn-outline' type='button' title='删除' onclick='mediaSupplierManageObj.delSupplier(" + rows.id + ")'>删除</button>&nbsp;";


                        }
                        return html;
                    }
                }
            ],
            pager: jQuery("#supplierContactorPaper"),
            viewrecords: true,
            caption: "",
            add: false,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false,
            gridComplete: function () {
                $('#supplierContactorTable').setGridWidth(1200);
            },
            onSelectRow: function (rowid, status) {
                var companyName = $("#supplierContactorTable").closest(".modal-content").find("form input[name='oldCompanyName']").val() || "";
                var param = {name: companyName};
                if (status == true) {
                    param["companyId"] = rowid;
                }
                mediaSupplierManageObj.reflushTable("supplierContactorAccountTable1", param);
            }
        });
        $("#supplierContactorTable").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
        $("#supplierContactorTable").setGridHeight(213);
        commonObj.setNavGrid("supplierContactorTable", "supplierContactorPaper");

        //供应商编辑账户列表
        $("#supplierContactorAccountTable").jqGrid({
            url: baseUrl + mediaSupplierManageObj.accountListUrl,
            datatype: "json",
            mtype: 'POST',
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
            multiselect: false,
            multiselectWidth: 25, //设置多选列宽度
            sortable: "true",
            sortname: "update_time",
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 10,//每页显示记录数
            rowList: [10, 20, 25, 50],//分页选项，可以下拉选择每页显示记录数
            jsonReader: {
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: "id"
            },
            colModel: [
                {
                    name: 'id',
                    index: 'id',
                    label: 'id',
                    editable: false,
                    width: 30,
                    align: "center",
                    sortable: false,
                    hidden: true
                },
                {
                    name: 'type',
                    index: 'type',
                    label: '账户归属类型',
                    editable: false,
                    width: 80,
                    align: "center",
                    sortable: false,
                    formatter: function (a, b, rowdata) {
                        if (rowdata.type == 0) {
                            return "未指定"
                        } else if (rowdata.type == 1) {
                            return "<span style='color:#0922ff'>公司账户</span>"
                        } else if (rowdata.type == 2) {
                            return "<span style='color:#fc180e'>供应商账户</span>"
                        } else if (rowdata.type == 3) {
                            return "<span style='color:#1eb519'>客户账户</span>"
                        } else if (rowdata.type == 4) {
                            return "<span style='color:#000000'>个人账户</span>"
                        }
                    }
                },
                {
                    name: 'name',
                    index: 'name',
                    label: '账户名称',
                    editable: false,
                    width: 200,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'owner',
                    index: 'owner',
                    label: '户主',
                    editable: false,
                    width: 80,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'bankNo',
                    index: 'bankNo',
                    label: '账号',
                    editable: false,
                    width: 150,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'bankName',
                    index: 'bankName',
                    label: '开户行',
                    editable: false,
                    width: 120,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'accountType',
                    index: 'accountType',
                    label: '类型',
                    editable: false,
                    width: 80,
                    align: "center",
                    sortable: false,
                    formatter: function (a, b, rowdata) {
                        var tmp = rowdata.accountType;
                        if (tmp == 'B2B') {
                            return "<span style=''>对公账户</span>"
                        } else if (tmp == 'B2C') {
                            return "<span style=''>对私账户</span>"
                        } else {
                            return "<span style=''>未指定</span>"
                        }
                    }
                },
                {
                    name: 'phone',
                    index: 'phone',
                    label: '预留电话',
                    editable: false,
                    width: 80,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'operator',
                    label: '操作',
                    editable: false,
                    width: 150,
                    align: "center",
                    sortable: false,
                    formatter: function (a, b, rowdata) {
                        var html = "";
                        if (rowdata.createUserId == user.id) {
                            html += "<button style='margin-top: 3px;' data-modalId='mediaSupplierEditModal' class='btn btn-xs btn-success btn-outline' type='button' title='编辑' onclick='mediaSupplierManageObj.opAccountClick(this, " + JSON.stringify(rowdata) + ")'>编辑</button>&nbsp;";
                            html += "<button style='margin-top: 3px;' data-modalId='mediaSupplierEditModal' class='btn btn-xs btn-danger btn-outline' type='button' title='删除' onclick='mediaSupplierManageObj.delAccount(this, " + rowdata.id + ")'>删除</button>&nbsp;";
                            html += "<button style='margin-top: 3px;' class='btn btn-xs btn-success btn-outline' type='button' title='异动详情' onclick='supplierAccountChangeObj.supplierAccountChangeShow(" + rowdata.id + ")'>异动详情</button>&nbsp;";
                        }
                        return html;
                    }
                }
            ],
            pager: $("#supplierContactorAccountPaper"),
            viewrecords: true,
            add: false,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false,
            gridComplete: function () {
                $('#supplierContactorAccountTable').setGridWidth(1200);
            }
        });
        $("#supplierContactorAccountTable").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
        $("#supplierContactorAccountTable").setGridHeight(213);
        commonObj.setNavGrid("supplierContactorAccountTable", "supplierContactorAccountTable");

        //供应商公司编辑账户列表
        $("#supplierContactorAccountTable1").jqGrid({
            url: baseUrl + mediaSupplierManageObj.accountListUrl,
            datatype: "json",
            mtype: 'POST',
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
            multiselect: false,
            multiselectWidth: 25, //设置多选列宽度
            sortable: "true",
            sortname: "update_time",
            sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
            shrinkToFit: true,
            prmNames: {rows: "size"},
            rowNum: 10,//每页显示记录数
            rowList: [10, 20, 25, 50],//分页选项，可以下拉选择每页显示记录数
            jsonReader: {
                root: "list", page: "pageNum", total: "pages",
                records: "total", repeatitems: false, id: "id"
            },
            colModel: [
                {
                    name: 'id',
                    index: 'id',
                    label: 'id',
                    editable: false,
                    align: "center",
                    sortable: false,
                    hidden: true
                },
                {
                    name: 'contactor',
                    index: 'contactor',
                    label: '供应商联系人',
                    editable: false,
                    width: 100,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'type',
                    index: 'type',
                    label: '账户归属类型',
                    editable: false,
                    width: 80,
                    align: "center",
                    sortable: false,
                    formatter: function (a, b, rowdata) {
                        if (rowdata.type == 0) {
                            return "未指定"
                        } else if (rowdata.type == 1) {
                            return "<span style='color:#0922ff'>公司账户</span>"
                        } else if (rowdata.type == 2) {
                            return "<span style='color:#fc180e'>供应商账户</span>"
                        } else if (rowdata.type == 3) {
                            return "<span style='color:#1eb519'>客户账户</span>"
                        } else if (rowdata.type == 4) {
                            return "<span style='color:#000000'>个人账户</span>"
                        }
                    }
                },
                {
                    name: 'name',
                    index: 'name',
                    label: '账户名称',
                    editable: false,
                    width: 170,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'owner',
                    index: 'owner',
                    label: '户主',
                    editable: false,
                    width: 60,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'bankNo',
                    index: 'bankNo',
                    label: '账号',
                    editable: false,
                    width: 150,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'bankName',
                    index: 'bankName',
                    label: '开户行',
                    editable: false,
                    width: 120,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'accountType',
                    index: 'accountType',
                    label: '类型',
                    editable: false,
                    width: 80,
                    align: "center",
                    sortable: false,
                    formatter: function (a, b, rowdata) {
                        var tmp = rowdata.accountType;
                        if (tmp == 'B2B') {
                            return "<span style=''>对公账户</span>"
                        } else if (tmp == 'B2C') {
                            return "<span style=''>对私账户</span>"
                        } else {
                            return "<span style=''>未指定</span>"
                        }
                    }
                },
                {
                    name: 'phone',
                    index: 'phone',
                    label: '预留电话',
                    editable: false,
                    width: 80,
                    align: "center",
                    sortable: false
                },
                {
                    name: 'operator',
                    label: '操作',
                    editable: false,
                    width: 150,
                    align: "center",
                    sortable: false,
                    formatter: function (a, b, rowdata) {
                        var html = "";
                        if (rowdata.supplierUserId == user.id) {
                            html += "<button style='margin-top: 3px;' data-modalId='mediaSupplierCompanyModal' class='btn btn-xs btn-success btn-outline' type='button' title='编辑' onclick='mediaSupplierManageObj.opAccountClick(this, " + JSON.stringify(rowdata) + ")'>编辑</button>&nbsp;";
                            html += "<button style='margin-top: 3px;' data-modalId='mediaSupplierCompanyModal' class='btn btn-xs btn-danger btn-outline' type='button' title='删除' onclick='mediaSupplierManageObj.delAccount(this, " + rowdata.id + ")'>删除</button>&nbsp;";
                        }
                        html += "<button style='margin-top: 3px;' class='btn btn-xs btn-success btn-outline' type='button' title='异动详情' onclick='supplierAccountChangeObj.supplierAccountChangeShow(" + rowdata.id + ")'>异动详情</button>&nbsp;";
                        return html;
                    }
                }
            ],
            pager: $("#supplierContactorAccountPaper1"),
            viewrecords: true,
            add: false,
            edit: true,
            addtext: 'Add',
            edittext: 'Edit',
            hidegrid: false,
            gridComplete: function () {
                $('#supplierContactorAccountTable1').setGridWidth(1200);
            }
        });
        $("#supplierContactorAccountTable1").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
        $("#supplierContactorAccountTable1").setGridHeight(131);
        commonObj.setNavGrid("supplierContactorAccountTable1", "supplierContactorAccountPaper1");
    },
    reflushTable: function (tableId, param) {
        //刷新表格
        $("#" + tableId).emptyGridParam(); //清空历史查询数据
        $("#" + tableId).jqGrid('setGridParam', {
            postData: param, //发送数据
        }).trigger("reloadGrid"); //重新载入
    },
    init: function () {
        $("#mediaSupplierAddModal").draggable();//供应商登记框可移动
        $("#mediaSupplierEditModal").draggable();//供应商编辑框可移动
        $("#mediaSupplierCompanyModal").draggable();//供应商公司编辑可移动
        $("#mediaSupplierAccountAddModal").draggable();//新增账户框可移动

        //在使用Bootstrap中模态框过程中，如果出现多层嵌套的时候，如打开模态框A，然后在A中打开模态框B，在关闭B之后，
        // 如果A的内容比较多，滚动条会消失，而变为Body的滚动条，这是由于模态框自带的遮罩的问题。
        $('#mediaSupplierAddModal').on('shown.bs.modal', function () {
            $("body").removeClass("modal-open");
        });

        $('#mediaSupplierEditModal').on('shown.bs.modal', function () {
            $("body").removeClass("modal-open");
        });

        $('#mediaSupplierCompanyModal').on('shown.bs.modal', function () {
            $("body").removeClass("modal-open");
        });

        $('#mediaSupplierAccountAddModal').on('shown.bs.modal', function () {
            $("body").removeClass("modal-open");
        });

        mediaSupplierManageObj.addSupplierCheckRule("mediaSupplierAddModal");//供应商登记框添加校验规则
        mediaSupplierManageObj.addSupplierCheckRule("mediaSupplierEditModal");//供应商编辑框添加校验规则

        //初始化页面表格
        mediaSupplierManageObj.createModalTable();

        //获取所有媒介用户
        if (mediaSupplierManageObj.mediaUserList.length < 1) {
            commonObj.requestData(null, "/user/listByType/MJ", "get", "json", false, function (data) {
                mediaSupplierManageObj.mediaUserList = data;
                if (data && data.length > 0) {
                    $(data).each(function (i, d) {
                        mediaSupplierManageObj.mediaUserMap[d.id] = d;
                    });
                }
            });
        }

        //表单渲染
        layui.use('form', function () {
            var form = layui.form;
            form.render();
        });
    },
    downTemplate: function () {
        location.href = "/supplier/downTemplate";
    },
    exportBtnClick: function () {
        var params = $("#supplier").serializeJson();
        location.href = "/supplier/export?" + $.param(params);
    },
    importSupplierBtnClick: function () {
        $("#uploadFile").val("");// 清空文件；
        $("#importModal").modal({backdrop: "static"});
    },
    importSupplier: function () {
        if ($("#uploadFile").val() == "") {
            layer.msg("请选择需要上传的文件。");
        } else {
            var filePath = $("#uploadFile").val();
            var regex = /^.+(.XLS|.xls|.XLSX|.xlsx)$/;
            if (regex.test(filePath)) {
                $.ajax({
                    type: "post",
                    url: "/supplier/importData",
                    data: new FormData($("#importForm")[0]),
                    dataType: "json",
                    async: true,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (data) {
                        $("#importModal").modal("hide");
                        if (data.code == 200) {
                            var message = "操作完成。";
                            var messageType = "success";
                            var isHtml = false;
                            if (data.data.msg != null) {
                                message = data.data.msg;
                                messageType = "warning";
                                if (data.data.file != null) {
                                    message = "<a style='color: red;font-weight: bold;' href='" + data.data.file + "'>" + message + "</a>";
                                    isHtml = true;
                                }
                            }
                            swal({
                                title: "提示",
                                text: message,
                                type: messageType,
                                html: isHtml,
                            });
                            // 刷新数据；
                            mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
                        } else {
                            if (getResCode(data)) {
                                return;
                            } else {
                                swal({
                                    title: "很抱歉，导入失败！",
                                    text: data.msg,
                                    type: "error",
                                    html: true
                                });
                            }
                        }
                    },
                    error: function (data) {
                        if (getResCode(data)) {
                            return;
                        }
                    }
                });
            } else {
                layer.msg("文件格式不正确，请选择Excel文件。");
            }
        }
    },
    changeSupplierState: function (t, type, id) {
        var tipTitle = type == 'stop' ? "禁用" : "启用";
        commonObj.requestData(null, "/supplier/" + type + "/" + id, "get", "json", true, function (data) {
            if (data.code == 200) {
                layer.msg("供应商联系人" + tipTitle + "成功！", {time: 2000, icon: 6});

                var currentModalId = $(t).attr("data-modalId") || "";

                if (currentModalId == "mediaSupplierCompanyModal") {
                    var companyName = $(t).closest(".modal-content").find("form input[name='oldCompanyName']").val() || "";
                    //供应商联系人列表刷新
                    if (companyName) {
                        mediaSupplierManageObj.reflushTable("supplierContactorTable", {name: companyName});
                    }
                } else {
                    mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
                }
            } else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    },
    delSupplier: function (id) {
        layer.confirm('确认删除？', {
            btn: ['删除', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            commonObj.requestData({id: id}, mediaSupplierManageObj.delSupplierUrl, "post", "json", true, function (data) {
                if (data.code == 200) {
                    layer.msg("删除供应商联系人成功", {time: 2000, icon: 6});
                    mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
                } else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
            });
        });
    },
    addSupplierCheckRule: function (modalId) {
        var icon = "<i class='fa fa-times-circle'></i> ";
        $("#" + modalId).find("form").validate({
            rules: {
                phone: {
                    required: true,
                    maxlength: 11,
                    remote: {
                        url: baseUrl + mediaSupplierManageObj.checkSupplierUrl, //后台处理程序
                        type: "post", //数据发送方式
                        dataType: "json", //接受数据格式
                        data: { // 要传递的数据
                            "phone": function () {
                                return $("#" + modalId).find("form input[name='phone']").val();
                            },
                            "name": function () {
                                return $("#" + modalId).find("form input[name='name']").val();
                            },
                            "id": function () {
                                return $("#" + modalId).find("form input[name='id']").val() || "";
                            }
                        },
                        dataFilter: function (data) {
                            data = JSON.parse(data);
                            if (data.code == 200) {
                                return true;
                            } else {
                                $("#" + modalId).find("form input[name='phone']").focus();
                                return false;
                            }
                        }
                    }
                },
            },
            messages: {
                phone: {remote: icon + "很抱歉，供应商公司名称未填写或者供应商公司名称和手机已经存在！"},
            }
        });
    },
    resetCompanyNatureBtn: function (modalId) {
        $("#" + modalId).find(".companyBtnCls").removeAttr("btn-white");
        if (!$("#" + modalId).find(".companyBtnCls").hasClass("btn-info")) {
            $("#" + modalId).find(".companyBtnCls").addClass("btn-info");
        }
        $("#" + modalId).find(".personBtnCls").removeAttr("btn-info");
        if (!$("#" + modalId).find(".personBtnCls").hasClass("btn-white")) {
            $("#" + modalId).find(".personBtnCls").addClass("btn-white");
        }
    },
    addSupplierClick: function (t) {
        //初始化新增页面
        $("#mediaSupplierAddModal").find("form").find("input").removeClass("error");
        $("#mediaSupplierAddModal").find("form").validate().resetForm();
        mediaSupplierManageObj.resetCompanyNatureBtn("mediaSupplierAddModal");
        $("#mediaSupplierAddModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsYes").css("display", "none");
        $("#mediaSupplierAddModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsNo").css("display", "none");
        $("#mediaSupplierAddModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsYes").css("display", "none");
        $("#mediaSupplierAddModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsNo").css("display", "none");
        var inputNameList = ["supplierNature", "standarCompanyFlag", "standarPhoneFlag"];
        $("#mediaSupplierAddModal").find("form input").each(function (i, input) {
            if (inputNameList.contains($(input).attr("name"))) {
                $(input).val("0");
            } else {
                $(input).val("");
            }
        });

        //如果有值，说明重公司编辑弹窗进来的
        if (t) {
            var supplierNature = $(t).attr("data-supplierNature") || 0;
            var supplierName = $(t).attr("data-supplierName") || "";
            var standarCompanyFlag = $(t).attr("data-standarCompanyFlag") || 0;
            $("#mediaSupplierAddModal").find("form [name='name']").val(supplierName);
            $("#mediaSupplierAddModal").find("form [name='name']").attr("readonly", true);
            //如果是个体工商户，则支持切换
            if (supplierNature == 1) {
                $("#mediaSupplierAddModal").find("form input[name='supplierNature']").closest("div").find("button").css("display", "inline-block");
                $("#mediaSupplierAddModal").find("form input[name='supplierNature']").closest("div").find("label").css("display", "none");
                var ele = $("#mediaSupplierAddModal").find("form input[name='supplierNature']").closest("div").find("button.personBtnCls")[0];
                $("#mediaSupplierAddModal").find("form [name='supplierNature']").val(0);//由于下面方法需要值不同才会触发，所以改成默认值
                companyObj.natureClick(ele, 1);//选中个体供应商按钮
            } else {
                //企业用户供应商性质不能改变
                $("#mediaSupplierAddModal").find("form input[name='supplierNature']").closest("div").find("button").css("display", "none");
                $("#mediaSupplierAddModal").find("form input[name='supplierNature']").closest("div").find("label").css("display", "inline-block");
                $("#mediaSupplierAddModal").find("form input[name='supplierNature']").closest("div").find("label").text("企业供应商");
            }
            //判断供应商公司名称是否标准
            if (standarCompanyFlag == 1) {
                $("#mediaSupplierAddModal").find("form input[name='standarCompanyFlag']").val("1");
                $("#mediaSupplierAddModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsYes").css("display", "inline-block");
            } else {
                $("#mediaSupplierAddModal").find("form input[name='standarCompanyFlag']").val("0");
                $("#mediaSupplierAddModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsNo").css("display", "inline-block");
            }

            //将前一个隐藏的模态框的ID保存
            $("#mediaSupplierAddModal").find(".modalTitle").attr("data-modalId", $(t).attr("data-modalId"));
            //将之前的模态框隐藏起来
            $("#" + $(t).attr("data-modalId")).modal("toggle");
        } else {
            $("#mediaSupplierAddModal").find("form input[name='supplierNature']").closest("div").find("button").css("display", "inline-block");
            $("#mediaSupplierAddModal").find("form input[name='supplierNature']").closest("div").find("label").css("display", "none");
            $("#mediaSupplierAddModal").find("form [name='name']").removeAttr("readonly");
            $("#mediaSupplierAddModal").find(".modalTitle").attr("data-modalId", "");
            var ele = $("#mediaSupplierAddModal").find("form input[name='supplierNature']").closest("div").find("button.companyBtnCls")[0];
            $("#mediaSupplierAddModal").find("form [name='supplierNature']").val(1);//由于下面方法需要值不同才会触发，所以改成默认值
            companyObj.natureClick(ele, 0);//选中企业供应商按钮
        }

        $("#mediaSupplierAddModal").modal("toggle");
    },
    addSupplier: function (t) {
        if (!$(t).closest(".modal-content").find("form").valid()) {
            return;
        }
        startModal("#" + $(t).attr("id"));
        commonObj.requestData(JSON.stringify($(t).closest(".modal-content").find("form").serializeForm()), mediaSupplierManageObj.addSupplierUrl, "post", "json", true, function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                layer.msg("新增供应商联系人成功！", {time: 2000, icon: 6});

                mediaSupplierManageObj.closeSupplierClick(t, true);
            } else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        }, function () {
            Ladda.stopAll();
        }, true);
    },
    closeSupplierClick: function (t, reflushFlag) {
        var lastModalId = $(t).closest(".modal-content").find(".modalTitle").attr("data-modalId") || "";
        $("#mediaSupplierAddModal").modal("toggle");//关闭当前窗口

        if (lastModalId) {
            //供应商联系人列表刷新
            var supplierName = $(t).closest(".modal-content").find("form input[name='name']").val() || "";
            if (supplierName) {
                mediaSupplierManageObj.reflushTable("supplierContactorTable", {name: supplierName});
            }
            $("#" + lastModalId).modal("toggle");
        }
        if (reflushFlag) {
            mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
        }
    },
    editSupplierClick: function (supplier, isEdit) {
        $("#mediaSupplierEditModal").find("form").find("input").removeClass("error");
        $("#mediaSupplierEditModal").find("form").validate().resetForm();
        $("#mediaSupplierEditModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsYes").css("display", "none");
        $("#mediaSupplierEditModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsNo").css("display", "none");
        $("#mediaSupplierEditModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsYes").css("display", "none");
        $("#mediaSupplierEditModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsNo").css("display", "none");
        var inputNameList = ["supplierNature", "standarCompanyFlag", "standarPhoneFlag"];
        $("#oldStandarPhoneFlag").val(supplier["standarPhoneFlag"] || "");//记录修改前的联系人规范状态，不能由规范改为非规范
        for (var key in supplier) {
            if ($("#mediaSupplierEditModal").find("form [name='" + key + "']").length > 0) {
                var val = supplier[key] || "";
                if (!val && inputNameList.contains(key)) {
                    val = 0;
                }
                $("#mediaSupplierEditModal").find("form [name='" + key + "']").val(val);

                //如果是编辑（并需要是本人）则可输入
                if (isEdit && key != "name" && supplier.creator == user.id) {
                    $("#mediaSupplierEditModal").find("form [name='" + key + "']").removeAttr("readonly");
                } else {
                    $("#mediaSupplierEditModal").find("form [name='" + key + "']").attr("readonly", true);
                }
                //对编辑数据进行（手机号，qq，微信）权限控制
                if (isEdit) {
                    if (key == "qqwechat" || key == "phone" || key == "qq") {
                        var value = supplier[key] || "";
                        var flag = false;
                        var plateIds = supplier.plateIds.split(",");
                        if (plateIds) {
                            for (var i = 0; i < plateIds.length; i++) {
                                if (mediaSupplierManageObj.mediaUserPlateMap.contains(plateIds[i])) {
                                    //当前用户的板块包含了该供应商的板块
                                    flag = true;
                                }
                            }
                        }
                        //1、仅责任人自己能看到
                        if ((user.id == supplier.creator) || (flag && supplier.flag)) {
                            $("#mediaSupplierEditModal").find("form [name='" + key + "']").val(value);
                        } else {
                            if (value.length >= 11) {
                                var start = value.length > 11 ? "*****" : "****";
                                value = value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
                                $("#mediaSupplierEditModal").find("form [name='" + key + "']").val(value);
                            } else if (value.length >= 3) {
                                value = value[0] + "***" + value[value.length - 1];
                                $("#mediaSupplierEditModal").find("form [name='" + key + "']").val(value);
                            } else {
                                $("#mediaSupplierEditModal").find("form [name='" + key + "']").val("**");
                            }
                        }
                    }
                }
            }
        }
        // 是编辑并且是本人按钮才不会禁用
        if(isEdit && supplier.creator == user.id){
            $("#mediaSupplierEditModal").find(".form-group button").attr("disabled",false);
        }else {
            $("#mediaSupplierEditModal").find(".form-group button").attr("disabled",true);
        }
        if (supplier["supplierNature"] == 1) {
            //个体供应商可以改变成企业供应商
            if (isEdit) {
                $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("button").css("display", "inline-block");
                $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("label").css("display", "none");
                var ele = $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("button.personBtnCls")[0];
                $("#mediaSupplierEditModal").find("form [name='supplierNature']").val(0);//由于下面方法需要值不同才会触发，所以改成默认值
                companyObj.natureClick(ele, 1);//选中个体供应商按钮
            } else {
                $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("button").css("display", "none");
                $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("label").css("display", "inline-block");
                $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("label").text("个体供应商");
            }
        } else {
            //企业用户供应商性质不能改变
            $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("button").css("display", "none");
            $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("label").css("display", "inline-block");
            $("#mediaSupplierEditModal").find("form input[name='supplierNature']").closest("div").find("label").text("企业供应商");
            $("#mediaSupplierEditModal").find("form input[name='name']").removeAttr("onblur");//移除公司标准校验

            //判断供应商公司名称是否标准
            if (supplier["standarCompanyFlag"] == 1) {
                $("#mediaSupplierEditModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsYes").css("display", "inline-block");
            } else {
                $("#mediaSupplierEditModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsNo").css("display", "inline-block");
            }
        }
        //判断供应商联系人是否标准
        if (supplier["standarPhoneFlag"] == 1) {
            $("#mediaSupplierEditModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsYes").css("display", "inline-block");
        } else {
            $("#mediaSupplierEditModal").find("form input[name='standarPhoneFlag']").closest("div").find(".phoneTipsNo").css("display", "inline-block");
        }

        //判断是否编辑
        if (isEdit) {
            //添加账号按钮添加数据
            $("#mediaSupplierEditModal").find("form .addAccount").css("display", "inline-block");
            $("#mediaSupplierEditModal").find("form .addAccount").attr("data-id", supplier.id);
            $("#mediaSupplierEditModal").find("form .addAccount").attr("data-supplierNature", supplier.supplierNature);
            $("#mediaSupplierEditModal").find("form .addAccount").attr("data-supplierName", supplier.name);
            $("#mediaSupplierEditModal").find("form .addAccount").attr("data-contactor", supplier.contactor);
            $("#mediaSupplierEditModal").find("form .addAccount").attr("data-modalId", "mediaSupplierEditModal");
            $("#mediaSupplierEditModal").find("#editSupplierBtn").css("display", "inline-block");
            $("#supplierContactorAccountTable").showCol("operator");//显示操作列
        } else {
            $("#mediaSupplierEditModal").find("form .addAccount").css("display", "none");
            $("#mediaSupplierEditModal").find("#editSupplierBtn").css("display", "none");
            $("#supplierContactorAccountTable").hideCol("operator");//隐藏操作列
        }

        mediaSupplierManageObj.reflushTable("supplierContactorAccountTable", {companyId: supplier.id});
        $("#mediaSupplierEditModal").modal("toggle");
    },
    editSupplier: function (t) {
        var $form = $(t).closest(".modal-content").find("form");
        if (!$form.valid()) {
            return;
        }
        var jsonData = $form.serializeForm();
        //供应商联系人不能由规范改为非规范
        if ($("#oldStandarPhoneFlag").val() == "1" && $("#oldStandarPhoneFlag").val() != jsonData.standarPhoneFlag) {
            layer.msg("供应商联系人不能由规范改为不规范！", {time: 2000, icon: 5});
            return;
        }

        startModal("#" + $(t).attr("id"));
        commonObj.requestData(JSON.stringify(jsonData), mediaSupplierManageObj.editSupplierUrl, "post", "json", true, function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                layer.msg("编辑供应商联系人成功！", {time: 2000, icon: 6});
                $("#mediaSupplierEditModal").modal("toggle");

                mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
            } else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        }, function () {
            Ladda.stopAll();
        }, true);
    },
    opAccountClick: function (t, account) {
        //初始化
        $("#mediaSupplierAccountAddModal").find("form").find("input").removeClass("error");
        $("#mediaSupplierAccountAddModal").find("form").validate().resetForm();
        $("#mediaSupplierAccountAddModal").find("form input[name='accountTypeStr']").prop("checked", "false");//移除单选框选中状态
        $("#mediaSupplierAccountAddModal").find("form input").each(function (i, input) {
            $(input).val("");
        });
        //如果有值，则编辑，否则新增
        if (account) {
            for (var key in account) {
                if (key == "accountType") {
                    $("#mediaSupplierAccountAddModal").find("form input[name='" + key + "']").val(account[key] || "");
                    if (account[key] == "B2B") {
                        $("#b2bAccount").prop("checked", "true");
                    } else {
                        $("#b2cAccount").prop("checked", "true");
                    }
                } else {
                    $("#mediaSupplierAccountAddModal").find("form input[name='" + key + "']").val(account[key] || "");
                }
            }
        } else {
            $("#mediaSupplierAccountAddModal").find("form input[name='companyId']").val($(t).attr("data-id") || "");
            $("#mediaSupplierAccountAddModal").find("form input[name='companyName']").val($(t).attr("data-supplierName") || "");
            $("#mediaSupplierAccountAddModal").find("form input[name='contactor']").val($(t).attr("data-contactor") || "");
            //供应商名称对应账户名称及户主， 账户类型-对公只有对公账户，个人只有对私账户，不需要选择；
            var supplierNature = $(t).attr("data-supplierNature") || "";
            if (supplierNature == 1) {
                $("#mediaSupplierAccountAddModal").find("form input[name='name']").val($(t).attr("data-contactor") || "");
                // $("#mediaSupplierAccountAddModal").find("form input[name='owner']").val($(t).attr("data-contactor") || "");
                $("#b2cAccount").prop("checked", "true");
                $("#mediaSupplierAccountAddModal").find("form input[name='accountType']").val("B2C");
            } else {
                $("#mediaSupplierAccountAddModal").find("form input[name='name']").val($(t).attr("data-supplierName") || "");
                // $("#mediaSupplierAccountAddModal").find("form input[name='owner']").val($(t).attr("data-supplierName") || "");
                $("#b2bAccount").prop("checked", "true");
                $("#mediaSupplierAccountAddModal").find("form input[name='accountType']").val("B2B");
            }
        }
        $("#mediaSupplierAccountAddModal").find("form input[name='companyCode']").val(user.companyCode);
        $("#mediaSupplierAccountAddModal").find("form input[name='type']").val("2");
        $("#" + $(t).attr("data-modalId")).modal("toggle");
        //将前一个隐藏的模态框的ID保存
        $("#mediaSupplierAccountAddModal").find(".modalTitle").attr("data-modalId", $(t).attr("data-modalId"));

        //表单渲染
        layui.use('form', function () {
            var form = layui.form;
            form.render();
        });
        $("#mediaSupplierAccountAddModal").modal("toggle");
    },
    opAccount: function (t) {
        if (!$(t).closest(".modal-content").find("form").valid()) {
            return;
        }
        startModal("#" + $(t).attr("id"));
        var formData = $(t).closest(".modal-content").find("form").serializeForm();
        var url = formData.id ? mediaSupplierManageObj.editAccountUrl : mediaSupplierManageObj.addAccountUrl;
        var msgTitle = formData.id ? "编辑" : "新增";
        commonObj.requestData(formData, url, "post", "json", true, function (data) {
            Ladda.stopAll();
            if (data.code == 200) {
                layer.msg(msgTitle + "供应商联系人账户成功！", {time: 2000, icon: 6});

                mediaSupplierManageObj.closeAccountClick(t, true);//关闭当前模态框
            } else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        }, function () {
            Ladda.stopAll();
        });
    },
    delAccount: function (t, accountId) {
        commonObj.requestData({id: accountId}, mediaSupplierManageObj.delAccountUrl, "post", "json", true, function (data) {
            if (data.code == 200) {
                layer.msg("删除供应商联系账户成功！", {time: 2000, icon: 6});

                var currentModalId = $(t).attr("data-modalId") || "";
                if (currentModalId) {
                    if (currentModalId == "mediaSupplierEditModal") {
                        var companyId = $(t).closest(".modal-content").find("form input[name='id']").val() || "";
                        if (companyId) {
                            mediaSupplierManageObj.reflushTable("supplierContactorAccountTable", {companyId: companyId});
                        }
                    }
                    if (currentModalId == "mediaSupplierCompanyModal") {
                        var companyName = $(t).closest(".modal-content").find("form input[name='oldCompanyName']").val() || "";
                        //供应商联系人账户列表刷新
                        if (companyName) {
                            mediaSupplierManageObj.reflushTable("supplierContactorAccountTable1", {name: companyName});
                        }
                    }
                }
            } else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        });
    },
    closeAccountClick: function (t, reflushFlag) {
        var lastModalId = $(t).closest(".modal-content").find(".modalTitle").attr("data-modalId") || "";
        $("#mediaSupplierAccountAddModal").modal("toggle");

        if (lastModalId) {
            if (reflushFlag) {
                if (lastModalId == "mediaSupplierEditModal") {
                    var companyId = $(t).closest(".modal-content").find("form input[name='companyId']").val() || "";
                    if (companyId) {
                        mediaSupplierManageObj.reflushTable("supplierContactorAccountTable", {companyId: companyId});
                    }
                }
                if (lastModalId == "mediaSupplierCompanyModal") {
                    var companyName = $(t).closest(".modal-content").find("form input[name='companyName']").val() || "";
                    //供应商联系人账户列表刷新
                    if (companyName) {
                        mediaSupplierManageObj.reflushTable("supplierContactorAccountTable1", {name: companyName});
                    }
                }
            }
            $("#" + lastModalId).modal("toggle");
        }
    },
    editSupplierCompanyClick: function (supplier, isEdit) {
        $("#mediaSupplierCompanyModal").find("form").find("input").removeClass("error");
        $("#mediaSupplierCompanyModal").find("form").validate().resetForm();
        $("#mediaSupplierCompanyModal").find(".companyWrap").html("");
        $("#mediaSupplierCompanyModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsYes").css("display", "none");
        $("#mediaSupplierCompanyModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsNo").css("display", "none");
        $("#mediaSupplierCompanyModal").find("form input[name='oldCompanyName']").val(supplier["name"] || "");
        $("#mediaSupplierCompanyModal").find("form input[name='name']").val(supplier["name"] || "");
        $("#mediaSupplierCompanyModal").find("form input[name='standarCompanyFlag']").val(supplier["standarCompanyFlag"] || "0");
        $("#oldStandarCompanyFlag").val(supplier["standarCompanyFlag"] || "");//记录修改前的公司规范状态，不能由规范改为非规范

        //判断供应商公司名称是否标准
        if (supplier["standarCompanyFlag"] == 1) {
            $("#mediaSupplierCompanyModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsYes").css("display", "inline-block");
        } else {
            $("#mediaSupplierCompanyModal").find("form input[name='standarCompanyFlag']").closest("div").find(".companyTipsNo").css("display", "inline-block");
        }

        //判断是否编辑
        if (isEdit) {
            $("#mediaSupplierCompanyModal").find("button.addSupplierCls").css("display", "inline-block");
            $("#mediaSupplierCompanyModal").find("form input[name='name']").removeAttr("readonly");
            $("#mediaSupplierCompanyModal").find("#editSupplierCompanyBtn").css("display", "flex");
            $("#mediaSupplierCompanyModal").find("form .addSupplierCls").attr("data-supplierNature", supplier["supplierNature"] || 0);
            $("#mediaSupplierCompanyModal").find("form .addSupplierCls").attr("data-supplierName", supplier["name"] || "");
            $("#mediaSupplierCompanyModal").find("form .addSupplierCls").attr("data-standarCompanyFlag", supplier["standarCompanyFlag"] || 0);
        } else {
            $("#mediaSupplierCompanyModal").find("button.addSupplierCls").css("display", "none");
            $("#mediaSupplierCompanyModal").find("form input[name='name']").attr("readonly", true);
            $("#mediaSupplierCompanyModal").find("#editSupplierCompanyBtn").css("display", "none");
        }

        //供应商联系人列表刷新
        mediaSupplierManageObj.reflushTable("supplierContactorTable", {name: supplier.name});
        //供应商联系人账户列表刷新
        mediaSupplierManageObj.reflushTable("supplierContactorAccountTable1", {name: supplier.name});

        $("#mediaSupplierCompanyModal").modal("toggle");
    },
    editSupplierCompany: function (t) {
        if (!$(t).closest(".modal-content").find("form").valid()) {
            return;
        }
        var formData = $(t).closest(".modal-content").find("form").serializeForm();
        //校验公司标准
        if ($("#oldStandarCompanyFlag").val() == "1" && $("#oldStandarCompanyFlag").val() != formData.standarCompanyFlag) {
            layer.msg("供应商公司不能由标准改为非标准！", {time: 2000, icon: 5});
            return;
        }
        startModal("#" + $(t).attr("id"));
        //不相同则提交
        if (formData.oldCompanyName != formData.name) {
            commonObj.requestData(formData, mediaSupplierManageObj.editSupplierCompanyUrl, "post", "json", true, function (data) {
                Ladda.stopAll();
                if (data.code == 200) {
                    layer.msg("编辑供应商公司成功！", {time: 2000, icon: 6});
                    $("#mediaSupplierCompanyModal").modal("toggle");

                    mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
                } else {
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }
            }, function () {
                Ladda.stopAll();
            });
        } else {
            Ladda.stopAll();
        }
    },
    isMJZJ: function () {
        var roles = user.roles;//获取用户角色
        var isMJZJ = false;//是否媒介政务
        if (roles) {
            for (var i = 0; i < roles.length; i++) {
                if (roles[i].code == 'ZJ' && roles[i].type == 'MJ') {
                    isMJZJ = true;
                    break;
                }
            }
        }
        return isMJZJ;
    },
    supplierAssign: function (supplier) {
        layer.open({
            type: 1,
            content: $("#supplierAssign"),
            btn: ['确定', '取消'],
            area: ['700px', '200px'],
            title: "供应商责任人指派",
            success: function (layero, index) {
                $(layero[0]).find("input[name='id']").val(supplier.id || "");//媒体ID

                if (mediaSupplierManageObj.mediaUserList.length < 1) {
                    commonObj.requestData(null, "/user/listByType/MJ", "get", "json", false, function (data) {
                        mediaSupplierManageObj.mediaUserList = data;
                        if (data && data.length > 0) {
                            $(data).each(function (i, d) {
                                mediaSupplierManageObj.mediaUserMap[d.id] = d;
                            });
                        }
                    });
                }
                var html = "<option value=\"\">请选择责任人</option>";
                $.each(mediaSupplierManageObj.mediaUserList, function (i, u) {
                    var selected = u.id == supplier.creator ? "selected" : "";
                    html += "<option " + selected + " value=\"" + u.id + "\">" + u.name + "</option>";
                });
                $(layero[0]).find("select").html(html);//责任人
                //使用layui表单
                layui.use('form', function () {
                    var form = layui.form;
                    form.render();
                });
            },
            yes: function (index, layero) {
                var jsonData = $(layero[0]).find("form").serializeForm();
                if (!jsonData.userId) {
                    layer.msg("请选择指派供应商责任人！", {time: 2000, icon: 5});
                    return;
                }
                commonObj.requestData(jsonData, "/supplier/updateSupplierUserId", "post", "json", true, function (data) {
                    if (data.code == 200) {
                        layer.msg("供应商责任人指派成功！", {time: 2000, icon: 6});
                        layer.close(index);
                        mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
                    } else {
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }
                });
            }
        });
    },
    enterEvent: function (event) {
        if ((event.keyCode == '13' || event.keyCode == 13)) {
            mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
        }
    }
}

//公司筛选
var companyObj = {
    companySearchUrl: "/company/companySearch",
    checkCompanyUrl: "/company/checkCompany",
    currentCompanyName: "",
    firstPageTotal: 0, //第一页查询缓存表数据总数
    natureClick: function (t, supplierNature) {
        $(t).closest("form").find(".companyWrap").html("");
        //发生改变才进行处理
        if (supplierNature != $(t).closest("form").find("input[name='supplierNature']").val()) {
            if (supplierNature == 1) {
                $(t).closest("form").find("input[name='name']").val("个体供应商");
                $(t).closest("form").find("input[name='name']").attr("readonly", true);
                //设置公司名称非标准
                $(t).closest("form").find(".companyTipsYes").hide();
                $(t).closest("form").find(".companyTipsNo").hide();
                $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
            } else {
                $(t).closest("form").find("input[name='name']").val("");
                $(t).closest("form").find("input[name='name']").removeAttr("readonly");
            }
            //改变按钮颜色
            $(t).closest("div").find("button").each(function (i, btn) {
                $(btn).removeClass("btn-info");
                if (!$(btn).hasClass("btn-white")) {
                    $(btn).addClass("btn-white");
                }
            });
            $(t).removeClass("btn-white");
            if (!$(t).hasClass("btn-info")) {
                $(t).addClass("btn-info");
            }
            $(t).closest("form").find("input[name='supplierNature']").val(supplierNature);
        }
    },
    renderCompanyItem: function (page, pageSize, companyList) {
        var html = "";
        if (companyList && companyList.length > 0) {
            $.each(companyList, function (m, company) {
                html += "<div onmousedown='companyObj.chooseCompany(this);' class=\"companyNameItem\" title=\"" + (company.companyName || "") + "\"><span>" + (company.companyName || "") + "</span></div>";
            });
        }
        return html;
    },
    search: function (t) {
        $(t).closest("form").find(".companyWrap").html("");
        var keyword = $(t).closest("form").find("input[name='name']").val();
        if (!keyword) {
            if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
                $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
            }
            return;
        } else {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
        }
        layui.use('flow', function () {
            var flow = layui.flow;
            flow.load({
                elem: $(t).closest("form").find(".companyWrap"),
                isAuto: false,
                done: function (page, next) {
                    //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                    //请求数据
                    var param = {keyword: keyword};
                    param.page = page; //页码
                    param.size = 20; //每页数据条数
                    commonObj.requestData(param, companyObj.companySearchUrl, "post", "json", false, function (data) {
                        //第一页是从缓存表拿数据，记录数据总数
                        if (page == 1) {
                            companyObj.firstPageTotal = data.total;
                        }
                        next(companyObj.renderCompanyItem(page, param.size, data.list), page < data.pages); //如果小于总页数，则继续
                    });
                }
            });
        });
    },
    enterEvent: function (t, event) {
        if ((event.keyCode == '13' || event.keyCode == 13)) {
            companyObj.search(t);
        }
    },
    checkCompany: function (t) {
        var keyword = $(t).closest("form").find("input[name='name']").val();
        if (!keyword) {
            $(t).closest("form").find(".companyTipsYes").hide();
            $(t).closest("form").find(".companyTipsNo").hide();
            $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
            return;
        }
        //如果是个体工商户，不需要校验
        if ($(t).closest("form").find("input[name='supplierNature']").val() != 1) {
            commonObj.requestData({keyword: keyword}, companyObj.checkCompanyUrl, "post", "json", false, function (data) {
                if (data.code == 200) {
                    $(t).closest("form").find(".companyTipsYes").show();
                    $(t).closest("form").find(".companyTipsNo").hide();
                    $(t).closest("form").find("input[name='standarCompanyFlag']").val(1);
                } else {
                    $(t).closest("form").find(".companyTipsYes").hide();
                    $(t).closest("form").find(".companyTipsNo").show();
                    $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
                }
            });
        }
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    chooseCompany: function (t) {
        $(t).closest("form").find("input[name='name']").val($(t).attr("title") || "");
        //隐藏弹出筛选框
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOut: function (t) {
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")) {
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOver: function (t) {
        //如果有内容则展示
        if ($(t).closest("form").find(".companyWrap").find("div").length > 0) {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
            $(t).closest("form").find("input[name='name']").focus();
        }
    },
    //验证电话号码格式
    checkPhone: function (t) {
        var telPatten = /^[1]([3-9])[0-9]{9}$/;
        if (telPatten.test($(t).val())) {
            $(t).closest("form").find(".phoneTipsYes").show();
            $(t).closest("form").find(".phoneTipsNo").hide();
            $(t).closest("form").find("input[name='standarPhoneFlag']").val(1);
        } else {
            $(t).closest("form").find(".phoneTipsYes").hide();
            $(t).closest("form").find(".phoneTipsNo").show();
            $(t).closest("form").find("input[name='standarPhoneFlag']").val(0);
        }
    },
}

//公共方法
var commonObj = {
    requestData: function (data, url, requestType, dataType, async, callBackFun, callErrorFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if (callErrorFun) {
            param.error = callErrorFun;
        }
        if (contentType) {
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    isMJBZ: function () {
        var roles = user.roles;//获取用户角色
        var isMJBZ = false;//是否媒介政务
        if (roles) {
            for (var i = 0; i < roles.length; i++) {
                if (roles[i].code == 'BZ' && roles[i].type == 'MJ') {
                    isMJBZ = true;
                    break;
                }
            }
        }
        return isMJBZ;
    },
    setNavGrid: function (gridName, pagerName, var1, var2) {
        var op = var1 || {edit: false, add: false, del: false, search: false};
        var op2 = var2 || {height: 200, reloadAfterSubmit: true};
        $("#" + gridName).jqGrid('navGrid', '#' + pagerName, op, op2);
    }
}

//媒体供应商异动详情
var supplierChangeObj = {
    supplierFieldList: [
        {cellCode: "creator", cellName: "责任人"},
        {cellCode: "supplierNature", cellName: "供应商性质"},
        {cellCode: "name", cellName: "供应商公司名称"},
        {cellCode: "standarCompanyFlag", cellName: "是否标准公司"},
        {cellCode: "contactor", cellName: "供应商联系人"},
        {cellCode: "standarPhoneFlag", cellName: "是否规范联系人"},
        {cellCode: "phone", cellName: "手机号"},
        {cellCode: "qqwechat", cellName: "微信号"},
        {cellCode: "qq", cellName: "QQ号"},
        {cellCode: "contactorDesc", cellName: "备注"},
        {cellCode: "state", cellName: "状态"}
    ],//供应商字段
    supplierChangeList: [],//缓存供应商异动列表
    renderMediaSupplierField: function (layero) {
        var html = "";
        $.each(supplierChangeObj.supplierFieldList, function (i, field) {
            html += "<div class=\"fieldItem\" title=\"" + field.cellName + "\" onclick='supplierChangeObj.renderSupplierChange(this, \"" + field.cellCode + "\");'>\n" +
                "        <div class=\"ellipsisContent\">" + field.cellName + "</div>\n" +
                "    </div>";
        });
        $(layero[0]).find(".fieldItemWrap").html(html);
    },
    renderSupplierChangeLi: function (userId, cellCode, supplierChange) {
        var moreBtnHtml = "";
        var firstRowHtml = "";
        var otherRowHtml = "";
        var returnFlag = false;//返回标识，判断是否返回数据，默认返回空，如果cellCode有值，需要判断变更字段中是否包含该字段，没包含则返回空
        var changeContent = JSON.parse(supplierChange["changeContent"])["change"];
        //多个更改字段才有更多按钮
        if (changeContent["fieldList"] && changeContent["fieldList"].length > 0) {
            moreBtnHtml = "<div class=\"moreChangeBtn\" onclick=\"supplierChangeObj.moreBtnClick(this);\">\n" +
                "                  <i class=\"text-danger\">更多&nbsp;</i>\n" +
                "                  <i class=\"fa fa-chevron-up\"></i>\n" +
                "              </div>";
            $.each(changeContent["fieldList"], function (i, fieldChange) {
                //如果cellCode有值，需要判断变更字段中是否包含该字段，没包含则返回空，cellCode为空时返回
                if (!cellCode || (cellCode && fieldChange.cell == cellCode)) {
                    returnFlag = true;
                }
                //责任人
                if ("creator" == fieldChange.cell) {
                    fieldChange.oldCellValue = fieldChange.oldCellValue ? (mediaSupplierManageObj.mediaUserMap[fieldChange.oldCellValue] ? mediaSupplierManageObj.mediaUserMap[fieldChange.oldCellValue].name : fieldChange.oldCellValue) : "空";
                    fieldChange.newCellValue = fieldChange.newCellValue ? (mediaSupplierManageObj.mediaUserMap[fieldChange.newCellValue] ? mediaSupplierManageObj.mediaUserMap[fieldChange.newCellValue].name : fieldChange.newCellValue) : "空";
                }

                //对于非责任人查看，展示加密后的手机号
                if ("qqwechat" == fieldChange.cell || "phone" == fieldChange.cell || "qq" == fieldChange.cell) {
                    //如果是责任人
                    if (userId == user.id) {
                        fieldChange.oldCellText = fieldChange.oldCellValue || fieldChange.oldCellText;
                        fieldChange.newCellText = fieldChange.newCellValue || fieldChange.newCellText;
                    } else {
                        fieldChange.oldCellValue = fieldChange.oldCellText || "空";
                        fieldChange.newCellValue = fieldChange.newCellText || "空";
                    }
                }

                //没有选择字段 或者  选择字段等于该字段，第一行显示字段
                if ((!cellCode && i == 0) || (cellCode && fieldChange.cell == cellCode)) {
                    firstRowHtml = "<div class=\"firstRowItemWrap\" title='" + ((fieldChange.oldCellText || fieldChange.oldCellValue) || '空') + "->" + ((fieldChange.newCellText || fieldChange.newCellValue) || '空') + "'>\n" +
                        "               <div class=\"fieldName\" title='" + fieldChange.cellName + "'>" + fieldChange.cellName + "</div>\n" +
                        "               <div class=\"fieldChangeVal\">\n" +
                        "                   <span class=\"oldValue\">：" + ((fieldChange.oldCellText || fieldChange.oldCellValue) || '空') + "</span>\n" +
                        "                   <span>-></span>\n" +
                        "                   <span class=\"newValue\">" + ((fieldChange.newCellText || fieldChange.newCellValue) || '空') + "</span>\n" +
                        "               </div>\n" +
                        "           </div>";
                } else {
                    otherRowHtml += "<div class=\"otherRowItem\" title='" + ((fieldChange.oldCellText || fieldChange.oldCellValue) || '空') + "->" + ((fieldChange.newCellText || fieldChange.newCellValue) || '空') + "'>\n" +
                        "               <div class=\"fieldName\" title='" + fieldChange.cellName + "'>" + fieldChange.cellName + "</div>\n" +
                        "               <div class=\"fieldChangeVal\">\n" +
                        "                   <span class=\"oldValue\">：" + ((fieldChange.oldCellText || fieldChange.oldCellValue) || '空') + "</span>\n" +
                        "                   <span>-></span>\n" +
                        "                   <span class=\"newValue\">" + ((fieldChange.newCellText || fieldChange.newCellValue) || '空') + "</span>\n" +
                        "               </div>\n" +
                        "           </div>";
                }
            });
        }
        var titleTmp = supplierChange.supplierName + "(" + supplierChange.supplierContactor + ")" + "-" + changeContent.opDesc + "（" + supplierChange.createDate + "）";
        var html = "<li class=\"layui-timeline-item timeLineCss\">\n" +
            "           <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
            "           <div class=\"layui-timeline-content layui-text timeCss\">\n" +
            "               <div class=\"layui-timeline-title\" style=\"display: flex;justify-content: space-between;padding-right: 20px;\">\n" +
            "                   <div title='" + titleTmp + "' style='width: 70%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + titleTmp + "</div>\n" +
            "                   <div>异动人：" + mediaSupplierManageObj.mediaUserMap[supplierChange.userId].name + "</div>\n" +
            "               </div>" +
            "               <div class=\"timeContent\" onclick='supplierChangeObj.recoverClick(this, " + (supplierChange.id || "") + ", \"" + changeContent.op + "\");'>\n" +
            "                   <div class=\"firstRowContentWrap\">\n" +
            "                       " + firstRowHtml + "\n" +
            "                       " + moreBtnHtml + "\n" +
            "                   </div>\n" +
            "                   <div class=\"otherRowContentWrap\">\n" +
            "                       " + otherRowHtml + "\n" +
            "                   </div>\n" +
            "               </div>\n" +
            "           </div>\n" +
            "       </li>";

        if (returnFlag) {
            return html;
        } else {
            return "";
        }
    },
    renderSupplierChange: function (t, cellCode) {
        var userId = $(t).closest(".modalContentWrap").find(".fieldLabel").attr("data-userId") || "";
        //如果有值，则说明是点击某个字段则改变字体颜色
        if (cellCode) {
            $(t).closest(".fieldItemWrap").find(".fieldItem").removeClass("fieldItemActive");
            $(t).addClass("fieldItemActive");
        }
        var html = "";
        if (supplierChangeObj.supplierChangeList && supplierChangeObj.supplierChangeList.length > 0) {
            $.each(supplierChangeObj.supplierChangeList, function (i, supplierChange) {
                html += supplierChangeObj.renderSupplierChangeLi(userId, cellCode, supplierChange);
            });
        }
        $(t).closest(".modalContentWrap").find("#announcements").html(html);
    },
    supplierChangeShow: function (supplierId, userId) {
        layer.open({
            type: 1,
            title: '供应商异动详情',
            content: $("#supplierChange").html(),
            btn: ['取消'],
            area: ["55%", "80%"],
            shade: 0,
            shadeClose: true,
            resize: false,
            move: '.layui-layer-title',
            moveOut: true,
            success: function (layero, index) {
                //设置责任人
                $(layero[0]).find(".fieldLabel").attr("data-userId", userId);
                //修改字段名称
                $(layero[0]).find(".fieldLabel").text("供应商全部字段");
                //绑定事件
                $(layero[0]).find(".fieldLabel").click(function () {
                    supplierChangeObj.renderSupplierChange(this);
                });
                supplierChangeObj.renderMediaSupplierField(layero);//渲染字段
                commonObj.requestData({supplierIds: supplierId}, "/supplier/listSupplierChange", "post", "json", true, function (data) {
                    supplierChangeObj.supplierChangeList = data;
                    supplierChangeObj.renderSupplierChange($(layero[0]).find("#announcements"));
                });
            }
        });
    },
    moreBtnClick: function (t) {
        var iNode = $(t).find("i:eq(1)");
        var classStr = iNode.attr('class');
        if (classStr.indexOf("fa-chevron-up") != -1) { //当前其他条件为隐藏
            iNode.removeClass("fa-chevron-up");
            iNode.addClass("fa-chevron-down");
            $(t).closest(".timeContent").find(".otherRowContentWrap").fadeIn("slow");
        } else {
            iNode.removeClass("fa-chevron-down");
            iNode.addClass("fa-chevron-up");
            $(t).closest(".timeContent").find(".otherRowContentWrap").fadeOut("slow");
        }
    },
    recoverClick: function (t, id, op) {
        var $announcements = $(t).closest("#announcements");
        if ($(t).hasClass("timeContentActive")) {
            $(t).removeClass("timeContentActive");
        } else {
            //移除其他选中的
            $announcements.find(".timeContent").removeClass("timeContentActive");
            $(t).addClass("timeContentActive");
        }
    }
}

//媒体供应商账户异动详情
var supplierAccountChangeObj = {
    supplierAccountFieldList: [
        {cellCode: "companyName", cellName: "供应商公司名称"},
        {cellCode: "contactor", cellName: "供应商联系人"},
        {cellCode: "name", cellName: "账户名称"},
        {cellCode: "bankNo", cellName: "账号"},
        {cellCode: "bankName", cellName: "开户行"},
        {cellCode: "owner", cellName: "户主"},
        {cellCode: "phone", cellName: "预留电话"}
    ],//供应商账户字段
    supplierAccountChangeList: [],//缓存供应商异动列表
    renderSupplierAccountField: function (layero) {
        var html = "";
        $.each(supplierAccountChangeObj.supplierAccountFieldList, function (i, field) {
            html += "<div class=\"fieldItem\" title=\"" + field.cellName + "\" onclick='supplierAccountChangeObj.renderSupplierAccountChange(this, \"" + field.cellCode + "\");'>\n" +
                "        <div class=\"ellipsisContent\">" + field.cellName + "</div>\n" +
                "    </div>";
        });
        $(layero[0]).find(".fieldItemWrap").html(html);
    },
    renderSupplierAccountChangeLi: function (cellCode, accountChange) {
        var moreBtnHtml = "";
        var firstRowHtml = "";
        var otherRowHtml = "";
        var returnFlag = false;//返回标识，判断是否返回数据，默认返回空，如果cellCode有值，需要判断变更字段中是否包含该字段，没包含则返回空
        var changeContent = JSON.parse(accountChange["changeContent"])["change"];
        //多个更改字段才有更多按钮
        if (changeContent["fieldList"] && changeContent["fieldList"].length > 0) {
            moreBtnHtml = "<div class=\"moreChangeBtn\" onclick=\"supplierAccountChangeObj.moreBtnClick(this);\">\n" +
                "                  <i class=\"text-danger\">更多&nbsp;</i>\n" +
                "                  <i class=\"fa fa-chevron-up\"></i>\n" +
                "              </div>";
            $.each(changeContent["fieldList"], function (i, fieldChange) {
                //如果cellCode有值，需要判断变更字段中是否包含该字段，没包含则返回空，cellCode为空时返回
                if (!cellCode || (cellCode && fieldChange.cell == cellCode)) {
                    returnFlag = true;
                }
                //没有选择字段 或者  选择字段等于该字段，第一行显示字段
                if ((!cellCode && i == 0) || (cellCode && fieldChange.cell == cellCode)) {
                    firstRowHtml = "<div class=\"firstRowItemWrap\" title='" + ((fieldChange.oldCellText || fieldChange.oldCellValue) || '空') + "->" + ((fieldChange.newCellText || fieldChange.newCellValue) || '空') + "'>\n" +
                        "               <div class=\"fieldName\" title='" + fieldChange.cellName + "'>" + fieldChange.cellName + "</div>\n" +
                        "               <div class=\"fieldChangeVal\">\n" +
                        "                   <span class=\"oldValue\">：" + ((fieldChange.oldCellText || fieldChange.oldCellValue) || '空') + "</span>\n" +
                        "                   <span>-></span>\n" +
                        "                   <span class=\"newValue\">" + ((fieldChange.newCellText || fieldChange.newCellValue) || '空') + "</span>\n" +
                        "               </div>\n" +
                        "           </div>";
                } else {
                    otherRowHtml += "<div class=\"otherRowItem\" title='" + ((fieldChange.oldCellText || fieldChange.oldCellValue) || '空') + "->" + ((fieldChange.newCellText || fieldChange.newCellValue) || '空') + "'>\n" +
                        "               <div class=\"fieldName\" title='" + fieldChange.cellName + "'>" + fieldChange.cellName + "</div>\n" +
                        "               <div class=\"fieldChangeVal\">\n" +
                        "                   <span class=\"oldValue\">：" + ((fieldChange.oldCellText || fieldChange.oldCellValue) || '空') + "</span>\n" +
                        "                   <span>-></span>\n" +
                        "                   <span class=\"newValue\">" + ((fieldChange.newCellText || fieldChange.newCellValue) || '空') + "</span>\n" +
                        "               </div>\n" +
                        "           </div>";
                }
            });
        }
        var titleTmp = accountChange.accountName + "(" + accountChange.accountOwner + ")" + "-" + changeContent.opDesc + "（" + accountChange.createDate + "）";
        var html = "<li class=\"layui-timeline-item timeLineCss\">\n" +
            "           <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
            "           <div class=\"layui-timeline-content layui-text timeCss\">\n" +
            "               <div class=\"layui-timeline-title\" style=\"display: flex;justify-content: space-between;padding-right: 20px;\">\n" +
            "                   <div title='" + titleTmp + "' style='width: 70%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + titleTmp + "</div>\n" +
            "                   <div>异动人：" + mediaSupplierManageObj.mediaUserMap[accountChange.userId].name + "</div>\n" +
            "               </div>" +
            "               <div class=\"timeContent\" onclick='supplierAccountChangeObj.recoverClick(this, " + (accountChange.id || "") + ", \"" + changeContent.op + "\");'>\n" +
            "                   <div class=\"firstRowContentWrap\">\n" +
            "                       " + firstRowHtml + "\n" +
            "                       " + moreBtnHtml + "\n" +
            "                   </div>\n" +
            "                   <div class=\"otherRowContentWrap\">\n" +
            "                       " + otherRowHtml + "\n" +
            "                   </div>\n" +
            "               </div>\n" +
            "           </div>\n" +
            "       </li>";

        if (returnFlag) {
            return html;
        } else {
            return "";
        }
    },
    renderSupplierAccountChange: function (t, cellCode) {
        //如果有值，则说明是点击某个字段则改变字体颜色
        if (cellCode) {
            $(t).closest(".fieldItemWrap").find(".fieldItem").removeClass("fieldItemActive");
            $(t).addClass("fieldItemActive");
        }
        var html = "";
        if (supplierAccountChangeObj.supplierAccountChangeList && supplierAccountChangeObj.supplierAccountChangeList.length > 0) {
            $.each(supplierAccountChangeObj.supplierAccountChangeList, function (i, accountChange) {
                html += supplierAccountChangeObj.renderSupplierAccountChangeLi(cellCode, accountChange);
            });
        }
        $(t).closest(".modalContentWrap").find("#announcements").html(html);
    },
    supplierAccountChangeShow: function (accountIds) {
        layer.open({
            type: 1,
            title: '供应商账户异动详情',
            content: $("#supplierChange").html(),
            btn: ['取消'],
            area: ["55%", "80%"],
            shade: 0,
            shadeClose: true,
            resize: false,
            move: '.layui-layer-title',
            moveOut: true,
            success: function (layero, index) {
                //修改字段名称
                $(layero[0]).find(".fieldLabel").text("供应商账户全部字段");
                //绑定事件
                $(layero[0]).find(".fieldLabel").click(function () {
                    supplierAccountChangeObj.renderSupplierAccountChange(this);
                });
                supplierAccountChangeObj.renderSupplierAccountField(layero);//渲染字段
                commonObj.requestData({accountIds: accountIds}, "/supplier/listSupplierAccountChange", "post", "json", true, function (data) {
                    supplierAccountChangeObj.supplierAccountChangeList = data;
                    supplierAccountChangeObj.renderSupplierAccountChange($(layero[0]).find("#announcements"));
                });
            }
        });
    },
    moreBtnClick: function (t) {
        var iNode = $(t).find("i:eq(1)");
        var classStr = iNode.attr('class');
        if (classStr.indexOf("fa-chevron-up") != -1) { //当前其他条件为隐藏
            iNode.removeClass("fa-chevron-up");
            iNode.addClass("fa-chevron-down");
            $(t).closest(".timeContent").find(".otherRowContentWrap").fadeIn("slow");
        } else {
            iNode.removeClass("fa-chevron-down");
            iNode.addClass("fa-chevron-up");
            $(t).closest(".timeContent").find(".otherRowContentWrap").fadeOut("slow");
        }
    },
    recoverClick: function (t, id, op) {
        var $announcements = $(t).closest("#announcements");
        if ($(t).hasClass("timeContentActive")) {
            $(t).removeClass("timeContentActive");
        } else {
            //移除其他选中的
            $announcements.find(".timeContent").removeClass("timeContentActive");
            $(t).addClass("timeContentActive");
        }
    }
}