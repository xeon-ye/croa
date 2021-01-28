var url = baseUrl + "/media";
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
        var width = $('.jqGrid_wrapper').width();
        $('#table_medias').setGridWidth(width);

    });
    init();
    // $.get(baseUrl+"/mediaType/parentId/0?isFlag=true", function (data) {
    $.get(baseUrl + "/mediaPlate/userId", function (data) {  //mediaType/userId
        if (data == null || data == '') {
            swal("没有板块可操作！", "没有查询到板块信息，请联系管理员赋权！", "warning");
            return;
        }
        getResCode(data);
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
    // 是否媒介部的领导；
    // var flag = user.currentDeptQx;
    // // 没有部门领导权限则移除；
    // if (!flag) {
    //     $("#operateDiv").remove();
    // }
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
        gridview: true, //加速显示
        cellsubmit: "clientArray",
        viewrecords: true,  //显示总记录数
        multiselectWidth: 25, //设置多选列宽度
        sortorder: "desc", //排序方式：倒序，本例中设置默认按id倒序排序
        multiselect: true,
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
                name: 'user.id',
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
                name: 'supplier.contactor',
                hidden: true
            },
            {
                name: 'picPath',
                label: '媒体图标',
                sortable:false,
                editable: true,
                width: 40,
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
                name: 'supplierId',
                label: '供应商公司名称',
                editable: true,
                width: 40,
                sortable:false,
                align: "center",
                formatter: function (v, options, row) {
                        if (user.dept.companyCode == 'XH'){
                            if(row.supplier) return row.supplier.name;
                        }else if(row.supplier.companyCode == user.dept.companyCode ){
                            if(row.supplier) return row.supplier.name;
                        }else {
                            return "";
                        }
                    }
                    //return row.supplier ? row.supplier.name : '';

            },
            {
                name: 'supplierId',
                label: '供应商联系人',
                sortable:false,
                editable: true,
                width: 40,
                align: "center",
                formatter: function (v, options, row) {
                    if (user.dept.companyCode == 'XH'){
                        if(row.supplier) return row.supplier.contactor;
                    } else if (row.supplier.companyCode == user.dept.companyCode){
                        return row.supplier.contactor;
                    } else {
                        return "";
                    }


                }
            },
            {
                name: 'state',
                label: "审核状态",
                editable: true,
                sortable:false,
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
                width: 25,
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
                        return v == 0 ? '是' : "否";
                    }
                    else {
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
                    }else {
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
                name: 'c3',
                label: "账号介绍",
                editable: true,
                hidden: true,
                width: 40,
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
            //         return "<input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span>";
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
            //         return "<input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span>";
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
            //         return "<input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span>";
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
            //         return "<input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span>";
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
            //         var html = "<label>${f1}:</label><input name=price_" + row.id + " type='radio' value='" + v + "' onchange='setPrice(" + JSON.stringify(row) + ",this)'/><span class='text-red font-bold'>￥ " + v + "</span><br/>";
            //     }
            // },
            {
                label: "价格",
                editable: true,
                // hidden: true,
                sortable:false,
                width: 120,
                align: "left",
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
            }
        ],
        pager: "#pager_medias",
        viewrecords: true,
        caption: "媒体列表",
        hidegrid: false,
        loadComplete: function (data) {
            if (getResCode(data))
                return;
            // reData();
        }, ondblClickRow: function (rowid, iRow, iCol, e) {
            //双击行时触发。rowid：当前行id；iRow：当前行索引位置；iCol：当前单元格位置索引；e:event对象
            page('/media/modifyMedia?id=' + rowid, '媒体修改' + rowid);
        },
        gridComplete: function () {
            // if (flag) {
            //     // 单选框居中；
            //     $(".cbox").addClass("icheckbox_square-green");
            // }
            var width = $('.jqGrid_wrapper').width();
            $('#table_medias').setGridWidth(width);
        }
    });
    $("#table_medias").jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
    $("#table_medias").setSelection(4, true);
    // $('#table_medias').setGridHeight(360);
}

// 批量操作数据；
function mediaDelete() {
    // 判断是否媒介部领导；
    var flag = user.currentDeptQx;
    if (flag) {
        var ids = $("#table_medias").jqGrid("getGridParam", "selarrrow");
        if (ids.length > 0) {
            // 获取选中列的数据；
            var mediaNames = new Array();
            var userIds = new Array();
            $(ids).each(function (index, id) {
                //由id获得对应数据行
                var row = $("#table_medias").jqGrid('getRowData', id);
                mediaNames.push(row.name);
                userIds.push(row["user.id"]);
            });
            layer.confirm("确认批量删除?", {
                btn: ["删除", "取消"],
                shade: false
            }, function (index) {
                layer.close(index);
                layer.msg("系统处理中，请稍候。");
                startModal("#mediaDelete");
                $.post(baseUrl + "/media/batchDelete", {
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
            });
        } else {
            layer.msg("请选择要操作的数据。");
        }
    }else {
        var totalList = $("#table_medias").jqGrid("getGridParam", "selarrrow");
        //var totalList = grid.getAllPageSelected("id");
        if (!totalList || totalList.length < 1) {
            layer.alert("请选择要删除的数据");
            return;
        }
        var datas = [];
        for (var i = 0; i < totalList.length; i++) {
            var o = totalList[i];
            var temp = {};
            temp.id = o;
            datas.push(temp);
        }
        layer.confirm('您确定要删除该媒体吗？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function (index) {
            layer.close(index);
            $.ajax({
                url: baseUrl + "/media/userDelete",
                type: "POST",
                data: {datas: JSON.stringify(datas)},
                dataType: "json",
                async: false,
                success: function (respData) {
                    // layer.closeAll();
                    if (respData.code == 200) {
                        layer.msg(respData.data.message, {time: 1000, icon: 6});
                        //grid.reflush();
                    } else {
                        layer.alert(respData.msg);
                    }
                    //重新加载表格
                    // grid.reflush();
                    $("#table_medias").emptyGridParam();
                    $("#table_medias").jqGrid('setGridParam', {
                        postData: $("#termForm").serializeJson()
                    }).trigger("reloadGrid");
                }
            });
        }, function () {
            return;
        });
    // else {
    //     layer.msg("权限不足。");
    // }
}
}

