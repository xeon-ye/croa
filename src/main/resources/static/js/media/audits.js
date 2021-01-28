var url = "/media";
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_medias').setGridWidth(width);

    });
    init();
    $.get(baseUrl + "/mediaPlate/userId", function (data) {  //mediaType/parentId/0?isFlag=true
        $.get("/media/getMediaNumbers",null,function(mdata){
            var mdataMap = {};
            for (var i in mdata) {
                mdataMap[mdata[i].type] = mdata[i].total;
            }
            var html = "";
            var mcount = 0;
            $(data).each(function (i, item) {
                mcount = mdataMap[item.id];
                mcount = mcount ? mcount : 0;
                html += "<span style='text-align: left;' class='col-md-1' title='" + item.name + "' onclick='loadTerm(" + item.id + ",this)'>" + item.name + "(" + mcount + ")" + "</span>";
            });
            $("#mediaTypeUL>li>div").html(html);
            $("#mediaTypeUL>li>div>span:first-child").click();
        },"json");
        // console.log("html after="+html)
        // $("#mediaTypeUL>li>div").html(html);
        // $("#mediaTypeUL>li>div>span:first-child").click();
    }, "json");


});

/**
 * 初始化数据
 */
function init() {
    $("#table_medias").jqGrid({//2600
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
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselect: true,
        multiselectWidth: 30, //设置多选列宽度
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        shrinkToFit: true,
        prmNames: {rows: "size"},
        rowNum: 10,//每页显示记录数
        rowList: [10, 20 , 50 , 100],//分页选项，可以下拉选择每页显示记录数
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
            },{
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
                name: 'supplier.id',
                hidden: true
            },
            {
                name: 'supplier.name',
                hidden: true
            },
            {
                name: 'user.id',
                hidden: true
            },
            {
                name: 'supplier.contactor',
                hidden: true
            },
            {
                name: 'picPath',
                label: '媒体图标',
                editable: true,
                width: 30,
                align: "center",
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
                sortable: false,
                width: 30,
                align: "center"
            },
            {
                name: 'supplierId',
                label: '供应商公司名称',
                editable: true,
                width: 40,
                align: "center",
                formatter: function (v, options, row) {
                    return row.supplier ? row.supplier.name : '';
                }
            },
            {
                name: 'supplierId',
                label: '供应商联系人',
                editable: true,
                width: 40,
                align: "center",
                formatter: function (v, options, row) {
                    return row.supplier ? row.supplier.contactor : '';
                }
            },
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
                width: 40,
                align: "center",
                formatter: "date",
                formatoptions: {srcformat: 'Y-m-d H:i:s', newformat: 'Y-m-d'}
            },
            {
                name: 'userId',
                label: '责任人',
                width: 25,
                align: "center",
                formatter: function (v, options, row) {
                    return row.user ? row.user.name : '';
                }
            },
            {
                name: 'd1',
                label: "价格有效期间",
                hidden: true,
                editable: true,
                width: 35,
                align: "center",
                formatter: "date",
                formatoptions: {srcformat: 'Y-m-d H:i:s', newformat: 'Y-m-d'}
            },
            {
                name: 'n1',
                label: "媒体类型",
                hidden: true,
                editable: true,
                width: 25,
                sortable: false,
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
                name: 'state',
                label: "审核状态",
                editable: true,
                //hidden: true,
                width: 25,
                align: "center",
                formatter: function (v, options, row) {
                    switch (v) {
                        case 0:
                            return '<span>未审核</span>';
                        case 1:
                            return '<span class="text-info">已通过</span>';
                        case 2:
                            return '<span class="text-success">审核中</span>';
                        case -1:
                            return '<span class="text-danger">已驳回</span>';
                        case -999:
                            return '<span class="text-justify">已停用</span>';
                    }
                }
            },
            {
                name: 'n3',
                label: "行业类型",
                hidden: true,
                width: 30,
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
                    } else if ( row.mType == 12) {
                        return v == 0 ? '男' : "女";
                    }else if(row.mType==9){
                        return v == 0? '是' :'否';
                    } else {
                        return row.n3Data ? row.n3Data.name : v ? v : "";
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
                        if (row.c3){
                            return  "<img style='height: 100px;width: 100px;' src='" + row.c3 + "'/>"
                        }else{
                            return "";
                        }

                    } else{
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
            //         return v;
            //         // return "<input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span>";
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
            //         return v;
            //         // return "<input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span>";
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
            //         return v;
            //         // return "<input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span>";
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
            //         return v;
            //         // return "<input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span>";
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
            //         return v;
            //         // var html = "<label>${f1}:</label><input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span><br/>";
            //     }
            // },
            {
                label: "价格",
                editable: true,
                // hidden: true,
                width: 80,
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
                    if (f1 != '')
                        html = "<label name='f1'></label><span class='text-red font-bold'>￥" + f1 + "</span>;";
                    if (f2 != '')
                        html += "<label name='f2'></label><span class='text-red font-bold'>￥" + f2 + "</span>;<br/>";
                    if (f3 != '')
                        html += "<label name='f3'></label><span class='text-red font-bold'>￥" + f3 + "</span>;";
                    if (f4 != '')
                        html += "<label name='f4'></label><span class='text-red font-bold'>￥" + f4 + "</span>;<br/>";
                    if (f5 != '')
                        html += "<label name='f5'></label><span class='text-red font-bold'>￥" + f5 + "</span>;";
                    if (f6 != '')
                        html += "<label name='f6'></label><span class='text-red font-bold'>￥" + f6 + "</span>;";
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
                name: 'f9',
                label: '非价格实数-2',
                width: 20,
                align: "center",
            },
            {
                name: 'discount',
                label: '折扣率',
                width: 20,
                align: "center",
                formatter: function (v, options, row) {
                    return v ? v + " %" : "100%";
                }
            },
            {
                name: 'commStart',
                label: '星级评分',
                width: 50,
                align: "center",
                formatter: function (v, options, row) {
                    return "<img src='/img/rating/stars-all.gif'/>";
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
                width: 25,
                align: "center",
                formatter: function (v, options, row) {
                    // return row.n5Data ? row.n5Data.name : v ? v : "";
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
                width: 25,
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

        ],
        pager: "#pager_medias",
        viewrecords: true,
        caption: "媒体列表",
        hidegrid: false,
        loadComplete: function (data) {
            if (getResCode(data))
                return;
        },

        gridComplete: function () {
            // 单选框居中；
            // $(".cbox").addClass("icheckbox_square-green");
        }
    });
    $("#table_medias").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $("#table_medias").setSelection(4, true);

    rating();
}


/**
 * 审核通过
 */
function pass(id) {
    $.get(baseUrl + "/media/pass/" + id, function (data) {
        if (data.code == 200) {
            swal("操作成功", "通过操作成功", 'success', 2000);
            reData();
        } else {
            swal("操作失败", "驳回操作失败" + data.msg, 'error', 2000);
        }
    }, "json");
}

/**
 * 审核驳回
 */
function reject(id) {
    $.get(baseUrl + "/media/reject/" + id, function (data) {
        if (data.code == 200) {
            swal("操作成功", "驳回操作成功", 'success', 2000);
            reData();
        } else {
            swal("操作失败", "驳回操作失败" + data.msg, 'error', 2000);
        }
    }, "json");
}

/**
 * 审核删除
 */
function del(id) {
    $.get(baseUrl + "/media/del/" + id, function (data) {
        if (data.code == 200) {
            swal("操作成功", "删除成功", 'success', 2000);
            reData();
        } else {
            swal("操作失败", "删除失败", 'error', 2000);
        }
    }, "json");
}

// 批量操作；
function auditOprate(obj, flag) {
    var url = "";
    var tips = "";
    // 通过；
    if (flag == 0) {
        url = "/media/passBatch";
        tips = "确认批量通过?";
    }
    // 驳回；
    if (flag == 1) {
        url = "/media/rejectBatch";
        tips = "确认批量驳回?";
    }
    // 删除；
    if (flag == 2) {
        url = "/media/deleteBatch";
        tips = "确认批量删除?";
    }
    var ids = $("#table_medias").jqGrid("getGridParam", "selarrrow");
    if (url.length > 0 && ids.length > 0) {
        // 获取选中列的数据；
        var mediaNames = new Array();
        var userIds = new Array();
        $(ids).each(function (index, id) {
            //由id获得对应数据行
            var row = $("#table_medias").jqGrid('getRowData', id);
            mediaNames.push(row.name);
            userIds.push(row["user.id"]);
        });
        layer.confirm(tips, {
            btn: ["确认", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            layer.msg("系统处理中，请稍候。");
            startModal("#" + $(obj).attr("id"));
            $.post(baseUrl + url, {
                ids: ids, mediaNames: mediaNames, userIds: userIds
            }, function (data) {
                Ladda.stopAll();
                if (data.data.message == null) {
                    getResCode(data);
                } else {
                    layer.msg(data.data.message);
                }
                // 刷新数据；
                $("#table_medias").emptyGridParam();
                $("#table_medias").jqGrid('setGridParam', {
                    postData: $("#termForm").serializeJson()
                }).trigger("reloadGrid");
            }, "json")
        }, function () {
            return;
        })
    } else {
        layer.msg("请选择要操作的数据。");
    }
}