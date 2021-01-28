var url = "/mediaInfo";
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_medias').setGridWidth(width);
    });
    init();
    $.get(baseUrl + "/mediaPlate/0", function (data) {  //mediaType/parentId/0
        var html = "";
        $(data).each(function (i, item) {
            html += "<span style='text-align: left;' class='col-md-1' title='" + item.name + "' onclick='loadTerm(" + item.id + ",this)'>" + item.name + "</span>";
        });
        $("#mediaTypeUL>li>div").html(html);
        $("#mediaTypeUL>li>div>span:first-child").click();
    }, "json");

});


/**
 * 初始化数据
 */
function init() {
    var mediaTypeId = $("#mediaTypeVal").val();
    $("#table_medias").jqGrid({//2600
        // url: baseUrl+'/mediaInfo',
        datatype: "json",
        postData: $("#termForm").serializeJson(),
        mtype: 'get',
        altRows: true,
        altclass: 'bgColor',
        height: "auto",
        page: 1,//第一页
        rownumbers: true,
        setLabel: "序号",
        autowidth: true,//自动匹配宽度
        // width: "2000px",
        shrinkToFit: false,
        autoScroll: true,
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselectWidth: 25, //设置多选列宽度
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        // multiselect: true,
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 15,//每页显示记录数
        rowList: [15, 25, 50],//分页选项，可以下拉选择每页显示记录数
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
            }, {
                name: 'n1Data',
                hidden: true
            }, {
                name: 'n2Data',
                hidden: true
            }, {
                name: 'n3Data',
                hidden: true
            }, {
                name: 'n4Data',
                hidden: true
            }, {
                name: 'n5Data',
                hidden: true
            }, {
                name: 'n6Data',
                hidden: true
            }, {
                name: 'n7Data',
                hidden: true
            }, {
                name: 'n8Data',
                hidden: true
            },
            {
                name: 'user.name',
                hidden: true
            },
            {
                name: 'supplier.id',
                hidden: true
            },
            {
                name: 'supplier.name',
                hidden: true
            },
            {
                name: 'picPath',
                label: '媒体图标',
                editable: true,
                width: 40,
                align: "center",
                sortable: false,
                formatter: function (v, options, row) {
                    if(v==null || v=="" || v==undefined){
                        return '<img class="head-img" src="/img/mrt.png"/>';
                    }else{
                        if(v.substring(v.lastIndexOf(".")+1)=="jpg" || v.substring(v.lastIndexOf(".")+1)=="png"){
                            return '<img class="head-img" src="' + v + '"/>';
                        }
                        return '<img class="head-img" src="/img/mrt.png"/>';
                    }
                }
            },
            {
                name: 'name',
                label: '媒体名称',
                editable: true,
                width: 50,
                align: "center"
            },
            // {
            //     name: 'supplierId',
            //     label: '供应商',
            //     editable: true,
            //     hidden: true,
            //     width: 50,
            //     align: "center",
            //     formatter: function (v, options, row) {
            //         return row.supplier ? row.supplier.name : '';
            //     }
            // },
            // {
            //     name: 'contactor',
            //     label: '联系人',
            //     editable: true,
            //     width: 50,
            //     align: "center"
            // },
            // {
            //     name: 'phone',
            //     label: '联系电话',
            //     editable: true,
            //     width: 50,
            //     align: "center"
            // },
            // {
            //     name: 'qqwechat',
            //     label: 'QQ/微信',
            //     editable: true,
            //     width: 50,
            //     align: "center"
            // },
            {
                name: 'remarks',
                label: '描述',
                editable: true,
                width: 60,
                align: "center"
            },
            {
                name: 'updateDate',
                label: '更新时间',
                width: 50,
                align: "center",
                formatter: "date",
                formatoptions: {srcformat: 'Y-m-d H:i:s', newformat: 'Y-m-d'}
            },
            {
                name: 'userId',
                label: '责任人',
                width: 30,
                align: "center",
                sortable: false,
                formatter: function (v, options, row) {
                    return row.user ? row.user.name : '';
                }
            },
            {
                name: 'd1',
                label: "价格有效期间",
                hidden: true,
                editable: true,
                width: 50,
                align: "center",
                formatter: "date",
                formatoptions: {srcformat: 'Y-m-d H:i:s', newformat: 'Y-m-d'}
            },
            {
                name: 'n1',
                label: "媒体类型",
                hidden: true,
                editable: true,
                width: 40,
                align: "center",
                formatter: function (v, options, row) {
                    return row.n1Data ? row.n1Data.name : v ? v : "";
                }
            },
            {
                name: 'n2',
                label: "地区",
                editable: true,
                hidden: true,
                width: 25,
                align: "center",
                formatter: function (v, options, row) {
                    return row.n2Data ? row.n2Data.name : v ? v : "";
                }
            },
            {
                name: 'n3',
                label: "行业类型",
                hidden: true,
                width: 40,
                align: "center",
                formatter: function (v, options, row) {
                    if (row.mType == 6) {
                        if (v == 0) {
                            return "PC";
                        } else if (v == 1) {
                            return "APP";
                        } else {
                            return "不限";
                        }
                    } else if (row.mType == 12) {
                        return v == 0 ? '男' : "女";
                    } else if (row.mType == 9) {
                        return v == 0 ? '是' : '否';
                    } else {
                        return row.n3Data ? row.n3Data.name : v ? v : "";
                    }
                }
            },
            {
                name: 'n4',
                label: "资源类别筛选",
                hidden: true,
                editable: true,
                width: 50,
                align: "center",
                formatter: function (v, options, row) {
                    if (row.mType == 4 || row.mType == 10) {
                        return v == 0 ? '男' : "女";
                    } else if (row.mType == 5) {
                        if (v == 0) {
                            return "秒出";
                        } else if (v == 1) {
                            return "当天";
                        } else if (v == 2) {
                            return "隔天";
                        } else {
                            return "不限";
                        }
                    } else if (row.mType == 7) {
                        if (v == 0) {
                            return "广告";
                        } else if (v == 1) {
                            return "新闻";
                        } else {
                            return "不限";
                        }
                    } else {
                        return row.n4Data ? row.n4Data.name : v ? v : "";
                    }
                }
            },
            {
                name: 'n5',
                label: "是否500强",
                hidden: true,
                editable: true,
                width: 30,
                align: "center",
                formatter: function (v, options, row) {
                    if (row.mType == 1 || (row.mType >= 428 && row.mType <= 431)) {
                        return v == 0 ? '是' : "否";
                    } else if (row.mType == 5) {
                        if (v == 0) {
                            return "新闻源";
                        } else if (v == 1) {
                            return "网页";
                        } else {
                            return "不限";
                        }
                    } else if (row.mType == 6) {
                        if (v == 0) {
                            return "大首页";
                        } else if (v == 1) {
                            return "频道";
                        } else {
                            return "不限";
                        }
                    } else if (row.mType == 7 || row.mType == 8) {
                        if (v == 0) {
                            return "全国可见";
                        } else if (v == 1) {
                            return "智能推荐";
                        } else {
                            return "不限";
                        }
                    } else {
                        return row.n5Data ? row.n5Data.name : v ? v : "";
                    }
                }
            },
            {
                name: 'n6',
                label: "n6",
                editable: true,
                hidden: true,
                width: 25,
                align: "center",
                formatter: function (v, options, row) {
                    if (row.mType == 1 || (row.mType >= 3 && row.mType <= 8)) {
                        return v == 0 ? '是' : "否";
                    } else {
                        return row.n6Data ? row.n6Data.name : v ? v : "";
                    }
                }
            },
            {
                name: 'n7',
                label: "粉丝数",
                hidden: true,
                editable: true,
                width: 25,
                align: "center",
                formatter: function (v, options, row) {
                    if (row.mType <= 4) {
                        if (row.mType > 2) {
                            return setWeekShow(v);
                        } else {
                            return row.n7Data ? row.n7Data.name : v;
                        }
                    } else {
                        return v == 0 ? '是' : "否";
                    }
                }
            },
            {
                name: 'n8',
                label: "历史头条阅读量",
                hidden: true,
                editable: true,
                width: 30,
                align: "center",
                formatter: function (v, options, row) {
                    return row.n8Data ? row.n8Data.name : v ? v : "";
                }
            },
            {
                name: 'c1',
                label: "账号名称",
                editable: true,
                hidden: true,
                width: 50,
                align: "center"
            },
            {
                name: 'c2',
                label: "ID",
                editable: true,
                hidden: true,
                width: 50,
                align: "center",
                formatter: function (v, options, row) {

                    if ((row.mType >= 6 && row.mType <= 10) || row.mType == 5) {
                        return "<a href='" + row.c2 + "' target='_blank'>" + row.c2 + "</a>";
                    } else {
                        var html = "";
                        if (v) {
                            if (v.length > 20) {
                                var text = v.substring(0, 20);
                                html = "<span title='" + v + "'>" + text + "...</span>";
                            } else {
                                html = v;
                            }
                        }
                        return html;
                    }

                }
            },
            {
                name: 'c3',
                label: "账号介绍",
                editable: true,
                hidden: true,
                width: 50,
                align: "center",
                formatter: function (v, options, row) {
                    if (row.mType == 2 || row.mType == 12 || row.mType == 306) {

                        return "<a href='" + row.c3 + "' target='_blank'>" + row.c3 + "</a>";
                    } else if (row.mType >= 5 && row.mType <= 7) {
                        if (row.c3) {
                            return "<img style='height: 100px;width: 100px;' src='" + row.c3 + "'/>"
                        } else {
                            return "";
                        }

                    } else {

                        var html = "";
                        if (v) {
                            if (v.length > 20) {
                                var text = v.substring(0, 20);
                                html = "<span title='" + v + "'>" + text + "...</span>";
                            } else {
                                html = v;
                            }
                        }
                        return html;
                    }
                }
            },
            {
                name: 'c4',
                label: "c4",
                editable: true,
                hidden: true,
                width: 50,
                align: "center"
            },
            {
                name: 'c5',
                label: "c5",
                editable: true,
                hidden: true,
                width: 50,
                align: "center"
            },
            {
                name: 'c6',
                label: "c6",
                editable: true,
                hidden: true,
                width: 50,
                align: "center"
            },
            {
                name: 'c7',
                label: "c7",
                editable: true,
                hidden: true,
                width: 50,
                align: "center"
            },
            {
                name: 'c8',
                label: "c8",
                editable: true,
                hidden: true,
                width: 50,
                align: "center"
            },
            // {
            //     name: 'f1',
            //     label: "撰稿发布",
            //     editable: true,
            //     hidden: true,
            //     width: 30,
            //     align: "center",
            //     formatter: function (v, options, row) {
            //         if (!v)
            //             return "";
            //         return "<span class='radio-inline i-checks'><label>${f1}：</label><input name=price_" + row.id + " type='radio' value='" + v + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span></span>";
            //     }
            // },
            // {
            //     name: 'f2',
            //     label: "撰稿价格",
            //     editable: true,
            //     hidden: true,
            //     width: 30,
            //     align: "center",
            //     formatter: function (v, options, row) {
            //         if (!v)
            //             return "";
            //         return "<span class='radio-inline i-checks'><label>${f2}：</label><input name=price_" + row.id + " type='radio' value='" + v + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span></span>";
            //     }
            // },
            // {
            //     name: 'f3',
            //     label: "头条刊例价",
            //     editable: true,
            //     hidden: true,
            //     width: 30,
            //     align: "center",
            //     formatter: function (v, options, row) {
            //         if (!v)
            //             return "";
            //         return "<span class='radio-inline i-checks'><label>${f3}：</label><input name=price_" + row.id + " type='radio' value='" + v + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span></span>";
            //     }
            // },
            // {
            //     name: 'f4',
            //     label: "次条刊例价",
            //     editable: true,
            //     hidden: true,
            //     width: 30,
            //     align: "center",
            //     formatter: function (v, options, row) {
            //         if (!v)
            //             return "";
            //         return "<span class='radio-inline i-checks'><label>${f4}：</label><input name=price_" + row.id + " type='radio' value='" + v + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span></span>";
            //     }
            // },
            // {
            //     name: 'f5',
            //     label: "其他刊例价",
            //     editable: true,
            //     hidden: true,
            //     width: 30,
            //     align: "center",
            //     formatter: function (v, options, row) {
            //         if (!v)
            //             return "";
            //         return "<span class='radio-inline i-checks'><label>${f1}：</label><input name=price_" + row.id + " type='radio' value='" + v + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span></span>";
            //     }
            // },
            {
                label: "价格",
                editable: true,
                // hidden: true,
                width: 100,
                align: "left",
                sortable: false,
                formatter: function (v, options, row) {
                    var f1 = (row.f1 ? row.f1 : '');
                    var f2 = (row.f2 ? row.f2 : '');
                    var f3 = (row.f3 ? row.f3 : '');
                    var f4 = (row.f4 ? row.f4 : '');
                    var f5 = (row.f5 ? row.f5 : '');
                    var f6 = (row.f6 ? row.f6 : '');
                    var html = "";
                    // 报纸板块不显示底价和标题费用；
                    if (row.mType == 3) {
                        if (f2 != '')
                            html += "<span class='radio-inline i-checks'><label name='f2'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f2 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f2 + "</span></span><br/>";
                        if (f4 != '')
                            html += "<span class='radio-inline i-checks'><label name='f4'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f4 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f4 + "</span></span><br/>";
                        if (f5 != '')
                            html += "<span class='radio-inline i-checks'><label name='f5'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f5 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f5 + "</span></span><br/>";
                        if (f6 != '')
                            html += "<span class='radio-inline i-checks'><label name='f6'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f6 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f6 + "</span></span>";
                    } else {
                        if (f1 != '')
                            html = "<span class='radio-inline i-checks'><label name='f1'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f1 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f1 + "</span></span><br/>";
                        if (f2 != '')
                            html += "<span class='radio-inline i-checks'><label name='f2'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f2 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f2 + "</span></span><br/>";
                        if (f3 != '')
                            html += "<span class='radio-inline i-checks'><label name='f3'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f3 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f3 + "</span></span><br/>";
                        if (f4 != '')
                            html += "<span class='radio-inline i-checks'><label name='f4'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f4 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f4 + "</span></span><br/>";
                        if (f5 != '')
                            html += "<span class='radio-inline i-checks'><label name='f5'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f5 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f5 + "</span></span><br/>";
                        if (f6 != '')
                            html += "<span class='radio-inline i-checks'><label name='f6'></label>&nbsp;<input name='price_" + row.id + "' type='radio' value='" + f6 + "' onclick='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥" + f6 + "</span></span>";
                    }
                    html += "";
                    return html;
                }
            },
            {
                name: 'f6',
                label: '折扣率',
                editable: true,
                hidden: true,
                width: 30,
                align: "center",
            },
            {
                name: 'f8',
                label: '非价格实数',
                width: 20,
                align: "center",
            },
            {
                name: 'discount',
                label: '折扣率',
                width: 25,
                align: "center",
                formatter: function (v, options, row) {
                    return v ? v + " %" : "100%";
                }
            }
        ],
        pager: "#pager_medias",
        viewrecords: true,
        caption: "媒体列表",
        hidegrid: false,
        loadComplete: function (data) {
            if (getResCode(data)) {
                return;
            }
            // reData();
            // }, ondblClickRow: function (rowid, iRow, iCol, e) {
            //     //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            //     page('/mediaInfo/modifyMedia?id=' + rowid, '媒体修改' + rowid);
        },
        gridComplete: function () {
            $('.i-checks').iCheck({
                checkboxClass: 'icheckbox_square-green',
                radioClass: 'iradio_square-green',
            });
            $("#table_medias").find('.i-checks').on('ifClicked', function () {
                $(this).find("input").click();
            });
            // $(this).closest('.ui-jqgrid-view').find('div.ui-jqgrid-hdiv').width($(this).width()+15);
            // var width = $('.jqGrid_wrapper').width();
            // $(".ui-jqgrid-htable").width(width);
        }
    });
    $("#table_medias").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $("#table_medias").setSelection(4, true);
    $('#table_medias').setGridHeight(360);
}


var ids = {};

/**
 * 设置价格
 * @param row
 * @param t
 */
function setPrice(row, t) {
    // 获取选中的属性；
    var checked = $(t).attr("checked");
    // 确定当前是否已选中；
    var isChecked = checked && checked == "checked";
    // 取消同级别的所有单选框的选中状态；
    $(t).closest("td").find("input").each(function () {
        $(this).attr("checked", false);
        // 移除ICheck样式；
        $(this).parent().removeClass("checked");
    });
    var id = row.id;
    // 如果已选中，则清空；
    if (isChecked) {
        // 清空ID；
        ids[id] = null;
        // 删除已列出的数据；
        $("#row" + id).remove();
        // 更新价格；
        pushPrice($("#lr"));
        sum();
        return;
    } else {
        $(t).attr("checked", true);
        // 增加ICheck样式；
        if (!$(this).parent().hasClass("checked")) {
            $(this).parent().addClass("checked");
        }
    }
    var html = ids[id];
    var priceName = $(t).parent().prev().text().replace(":", "");
    var priceColumn = $(t).parent().prev().attr("column");
    var value = t.value != undefined ? t.value : 0;
    var name = (row.user != undefined ? row.user.name ? row.user.name : '' : '');
    var lr = $('#lr').val();
    var discount = (row.discount ? row.discount : 100);
    var price = value;
    if (ids[id] == null) {
        // html = '<tr id="row' + id + '"><td>' + id + '</td><td>' + (row.n1Data != undefined ? row.n1Data.name : '无') +
        html = '<tr id="row' + id + '"><td>' + id + '</td><td>' + $("#mediaTypeText").text() +
            '<input type="hidden" name="mediaTypeName" value="' + $("#mediaTypeText").text() + '"/>' +
            '<input type="hidden" name="mediaId" value="' + row.id + '"/>' +
            '<input type="hidden" name="mediaTypeId" value="' + $("#mediaTypeVal").val() + '"/>' +
            '<input type="hidden" name="mediaName" value="' + row.name + '"/>' +
            '<input type="hidden" name="priceType" value="' + priceName + '"/>' +
            '<input type="hidden" name="priceColumn" value="' + priceColumn + '"/>' +
            '<input type="hidden" name="payAmount" value="' + (price * discount / 100) + '"/>' +
            '<input type="hidden" name="supplierId" value="' + row.supplier.id + '"/>' +
            '<input type="hidden" name="supplierName" value="' + row.supplier.name + '"/>' +
            '<input type="hidden" name="supplierContactor" value="' + row.supplier.contactor + '"/>' +
            '<input type="hidden" name="mediaUserId" value="' + row.userId + '"/>' +
            '<input type="hidden" name="mediaUserName" value="' + row.user.name + '"/>' +
            '<input type="hidden" name="industryId" value="' + row.n3 + '"/>' +
            '<input type="hidden" name="unitPrice" value="' + $(t).val() + '"/>' +
            '</td><td>' + row.name + '</td><td>' + name + '</td><td><img class="head-img" src="' + row.picPath + '"/></td><td>' + priceName + '</td><td>￥ <label>' +
            value + '</label>';
        if (row.mType == 3 && priceColumn == "f2" && row.f3) {
            html += '（标题费用￥ <span>' + row.f3 + '</span>）';
        }
        html += '</td><td><label>' + (discount) + '</label> %</td><td style="display: none;">' + row.supplier.name + '</td><td>' + row.updateDate + '</td>' +
            '<td><input name="num" onkeypress="return inNum(event)" size="5" value="1" onkeyup="updatePrice(this)"/></td><td><span class="text-red font-bold ">￥</span>' +
            '<input size="3" maxlength="5" name="price" value="' + price + '"/></td></tr>';
        ids[id] = html;//
        $("#order").append(html);
    } else {
        $("#row" + id).find("td:nth-child(6)").html(priceName);
        $("#row" + id).find("td:nth-child(7)").html('￥ <label>' + value + '</label>');
    }
    pushPrice($("#lr"));//更新价格
    sum();
}

/**
 * 推送数量
 * @param t
 */
function pushNum(t) {
    $("#order>tr>td:nth-child(11)>input").val(t.value);
    var lr = $('#lr').val();//利润率
    lr = parseInt(lr);
    var sum = 0;
    $("#order>tr").each(function (i, d) {
        var price = $(d).find("td:nth-child(7)>label").text();//成本价
        var discount = $(d).find("td:nth-child(8)>label").text();//折扣
        // console.log(discount);
        var num = $(d).find("td:nth-child(11)>input").val();//数量

        // 报纸板块的标题费用计算；
        var titlePriceElement = $(d).find("td:nth-child(7)>label").next("span");
        var titlePrice = 0;
        if (titlePriceElement && titlePriceElement.text().length > 0) {
            titlePrice = parseFloat(titlePriceElement.text());
        }
        var amount = ((price * num * discount / 100 + titlePrice) * (100 + lr)) / 100;
        sum += amount;
        $(d).find("td:nth-child(12)>input").val(amount);
    });
    $("#amount").val(sum);
    // sum()
}

/**
 * 推送价格
 * @param t
 */
function pushPrice(t) {
    var lr = $('#lr').val();//利润率+
    lr = parseInt(lr);
    var sum = 0;
    $("#order>tr>td:nth-child(12)>input").val(100 * t.value);//
    $("#order>tr").each(function (i, d) {
        var price = $(d).find("td:nth-child(7)>label").text();//成本价
        var discount = $(d).find("td:nth-child(8)>label").text();//折扣率
        var num = $(d).find("td:nth-child(11)>input").val();//数量

        var titlePriceElement = $(d).find("td:nth-child(7)>label").next("span");
        var titlePrice = 0.0;
        if (titlePriceElement && titlePriceElement.text().length > 0) {
            titlePrice = parseFloat(titlePriceElement.text());
        }
        var amount = ((price * num * discount / 100 + titlePrice) * (100 + lr)) / 100;
        sum += amount;
        $(d).find("td:nth-child(12)>input").val(amount);
    });
    $("#amount").val(sum);
    // sum();
}

/**
 * 更新价格
 * @param t
 */
function updatePrice(t) {
    var num = $(t).val();
    var tr = $(t).parent().parent();
    var lr = $('#lr').val();//利润率+
    lr = parseInt(lr);
    var price = tr.find("td:nth-child(7)>label").text();//成本价
    var titlePriceElement = tr.find("td:nth-child(7)>label").next("span");
    var titlePrice = 0;
    if (titlePriceElement && titlePriceElement.text().length > 0) {
        titlePrice = parseFloat(titlePriceElement.text());
    }
    var discount = tr.find("td:nth-child(8)>label").text();//折扣率
    var amount = ((price * num * discount / 100 + titlePrice) * (100 + lr)) / 100;
    tr.find("td:nth-child(12)>input").val(amount);
    sum();
}

/**
 * 计算总量
 */
function sum() {
    var sum = 0;
    $("#order>tr").each(function (i, d) {
        var amount = $(d).find("td:nth-child(12)>input").val();//数量
        sum += parseInt(amount);
    });
    $("#amount").val(sum);
}

/**
 * 验证订单信息
 * @returns {boolean}
 */
function checkOrderInfo() {
    var flag = true;
    var lr1 = $("#lr").val();
    if (lr1 == null || lr1 == undefined || lr1 == 0) {
        swal("请输入利润率");
        $("#lr").focus();
        $("#lr").css("border", "1px solid red");
        flag = false;
    }
    $("input[name='price']").each(function (i, item) {
        var lr = $(item).val();
        if (lr == null || lr == undefined || lr == 0) {
            swal("请输入利润率");
            $(item).focus();
            $(item).css("border", "1px solid red");
            flag = false;
        }
    });
    $("input[name='num']").each(function (i, item) {
        var num = $(item).val();
        if (num == null || num == undefined || num == 0) {
            swal("请输入数量");
            $(item).focus();
            $(item).css("border", "1px solid red");
            flag = false;
        }
    });
    return flag;
}

/**
 * 提交订单
 */
function save() {
    if (checkOrderInfo() && $("#order").find("tr").length > 0) {
        var json = $("#articleForm").serializeJson();
        $.ajax({
            // url:  "order/add",
            url: baseUrl + "/order",
            type: "post",
            data: {"param": JSON.stringify(json)},
            // data: JSON.stringify(json),
            // contentType:"application/json;charset=utf-8",
            dataType: "json",
            success: function (data) {
                // alert(JSON.stringify(data));
                if (data.code == 200) {
                    // location.href = "/order/getById/" + data.data.orderId;
                    page("/order/getById/" + data.data.orderId, "媒体下单-订单详情");
                } else {
                    swal(data.msg);
                }
                setTimeout(function () {
                    window.location.href = '/media/order';
                }, 1000);
            }
        });
    }
}

/**
 * 媒体方案导出；
 */
function exportData() {
    if (checkOrderInfo() && $("#order").find("tr").length > 0) {
        layer.msg("系统处理中，请稍候。");
        startModal("#exportButton");
        $.post(baseUrl + "/order/dataExport", {"params": JSON.stringify($("#articleForm").serializeJson())}, function (data) {
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
}