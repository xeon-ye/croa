var url = baseUrl + "/mediaAudit";

//全局参数定义
var fileUpload;
var companyMap = null; //缓存公司
var mediaTermMap = {}; //缓存板块对应的媒体查询条件，结构：{plateId: {fromId: formObj}}
var mediaTermULDefaultHTML = ""; //缓存默认条件html
var mediaUserMap = {};//缓存媒介用户
var mediaUserPlateMap= [];//用户板块集合
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    $(window).bind('resize', function () {
		var tableElement = $("#table_medias");
		var width = tableElement.closest('.jqGrid_wrapper').width() || $(document).width();
		tableElement.setGridWidth(width);

		var supplierPriceNode = $("#mediaSupplierPriceTable");
		supplierPriceNode.setGridWidth(supplierPriceNode.closest('.jqGrid_wrapper').width());
        // var width = $('.jqGrid_wrapper').width();
        // $('#table_medias').setGridWidth(width);

    });

	//媒体文件导入
	fileUpload = new FileUpload({
		targetEl: '#word2htmlForm',
		multi: false,
		filePart: "media",
		completeCallback: function (data) {
			if (data.length > 0)
				$.get(baseUrl + "/mediaAudit/importReplace?filePath="+data[0].file+"&standarPlatformFlag="+$("#standarPlatformFlag").val()+"&plateId=" + parseInt($("#plateId").val())+"&plateName="+$("#mediaTypeText").attr("title")+"&fileType="+$("#fileType").val() , function (result) {
					if (result.code === 200) {
						var message = "操作完成。";
						var messageType = "success";
						var isHtml = false;
						if (result.data.message) {
							message = result.data.message;
							messageType = "warning";
							if (result.data.file != null) {
								message = "<a style='color: red;font-weight: bold;' href='" + result.data.file + "'>" + message + "</a>";
								isHtml = true;
							}
						}
						swal({
							title: "提示",
							text: message,
							type: messageType,
							html: isHtml,
						});

						reflushTable();
						triggerPageBtnClick("/media1/mediaSupplierList","mediaListSearchBtn"); //触发媒体关系管理Tab刷新
						triggerPageBtnClick("/media1/audits","auditSearchBtn"); //触发媒体审核Tab刷新
						triggerPageBtnClick("/media/supplier_manage","search"); //触发供应商管理Tab刷新
						triggerPageBtnClick("/media1/mediaSupplierAuditList","mediaListSearchBtn"); //触发媒体关系审核Tab刷新
					} else {
						swal({
							title: result.msg,
							type: "error"
						});
					}
				}, "json");
		},
		acceptSuffix: ['xlsx','xls']
	});
	// $("#mediaSupplierPriceModal").draggable();//配置模态框可拖动 需要引入“jquery-ui-1.10.4.custom.min.css”和 “jquery-ui.min.js”

	//模态框弹出前调用的事件，可当做钩子函数-在调用 show 方法后触发。
	$('#mediaSupplierPriceModal').on('show.bs.modal', function () {
		//设置模态框样式等
		// var $modal_dialog = $(this).find(".modal-dialog");
		// var m_top = ($(window).height() - $modal_dialog.height())/2;
		// $modal_dialog.css({"margin": m_top + "px auto"});
	});
	//模态框弹出后调用的事件，可当做钩子函数-当模态框对用户可见时触发（将等待 CSS 过渡效果完成）。
	$('#mediaSupplierPriceModal').on('shown.bs.modal', function () {
		initSupplierTable(); //初始化供应商价格表
		//请求数据
		var mediaId = $("#modalMediaId").val();
		if(mediaId){
			requestData(null,"/mediaAudit/getMediaSupplierInfoByMediaId/"+mediaId,"get","json",false,function (data) {
				var reader = {
					root: function(obj) { return data.data.media; }
				};
				$("#mediaSupplierPriceTable").emptyGridParam();
				//根据板块ID判断是否显示案例链接还是ID字段
				var colModels = $("#mediaSupplierPriceTable").jqGrid('getGridParam', 'colModel');
				$(colModels).each(function (j, colModel) {
					if (colModel.name == "mediaContentId") {
						if($("#standarPlatformFlag").val() && $("#standarPlatformFlag").val() == 1){
							$("#mediaSupplierPriceTable").setGridParam().showCol(colModel.name);
						}else {
							$("#mediaSupplierPriceTable").setGridParam().hideCol(colModel.name);
						}
					}
				});
				$("#mediaSupplierPriceTable").setGridParam({data: data.data.media, reader: reader}).trigger('reloadGrid');
			});
		}
	});
	//模态框关闭前调用的事件，可当做钩子函数
	$('#mediaSupplierPriceModal').on('hidden.bs.modal', function () {
		//销毁历史数据
		$("#modalMediaId").val("");
	});

	//更多点击事件
	$("#more").click(function () {
		var iNode = $("#more > i:eq(1)");
		var classStr = iNode.attr('class');
		if(classStr.indexOf("fa-chevron-up") != -1){ //当前其他条件为隐藏
			iNode.removeClass("fa-chevron-up");
			iNode.addClass("fa-chevron-down");
			$("#otherCondition").fadeIn("slow");
		}else{
			iNode.removeClass("fa-chevron-down");
			iNode.addClass("fa-chevron-up");
			$("#otherCondition").fadeOut("slow");
		}
	});

	loadAllMediaMJ($("#userName"));//加载责任人，然后保存默认页面
	loadCopyCondition();//加载启用和拷贝查询条件
	mediaTermULDefaultHTML = $("#mediaTermUL").html();
	initMediaType(); //加载板块
    queryUserMediaPlateIds();//查询当前用户的媒体板块id
	statisticsFTModal.init();//初始化模态框
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
var requestData = function (data, url, requestType, dataType, async, callBackFun, contentType) {
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
	if (contentType) {
		param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
	}
	$.ajax(param);
}

//加载责任人
function loadAllMediaMJ(t){
	requestData(null,"/user/listByType/MJ","get","json",false,function (data) {
		$(data).each(function (i, d) {
			var value = $(t).attr("data-value");
			var selected = value == d.id ? "selected=selected" : "";
			$(t).append("<option value='" + d.id + "' " + selected + ">" + d.name + "</option>");
			mediaUserMap[d.id] = d;
		});
	})
}

//加载启用和拷贝查询条件
function loadCopyCondition() {
	//是否启用
	$("#enabledDiv").html("        <span class=\"radio-inline col-md-1 i-checks\" title=\"不限\"><input type=\"radio\" name=\"enabled\" value=\"\"/>不限</span>\n" +
		"                            <span class=\"radio-inline col-md-1 i-checks\" title=\"启用\"><input type=\"radio\" name=\"enabled\" value=\"0\"/>启用</span>\n" +
		"                            <span class=\"radio-inline col-md-1 i-checks\" title=\"停用\"><input type=\"radio\" name=\"enabled\" value=\"1\"/>停用</span>");

	//数据来源
	$("#dataSourceDiv").html("        <span class=\"radio-inline col-md-1 i-checks\" title=\"不限\"><input type=\"radio\" name=\"dataSource\" value=\"\"/>不限</span>\n" +
		"                            <span class=\"radio-inline col-md-1 i-checks\" title=\"手工导入\"><input type=\"radio\" name=\"dataSource\" value=\"0\"/>手工导入</span>\n" +
		"                            <span class=\"radio-inline col-md-1 i-checks\" title=\"自动爬取\"><input type=\"radio\" name=\"dataSource\" value=\"1\"/>自动爬取</span>");

	//是否拷贝，只给祥和员工看
	/*if("XH" == user.dept.companyCode){
		$("#copyDiv").css("display","block");
		$("#isCopyDiv").html("<span class=\"radio-inline col-md-1 i-checks\" title=\"不限\"><input type=\"radio\" name=\"isCopy\" value=\"\"/>不限</span>\n" +
			"                            <span class=\"radio-inline col-md-1 i-checks\" title=\"启用\"><input type=\"radio\" name=\"isCopy\" value=\"0\"/>自建</span>\n" +
			"                            <span class=\"radio-inline col-md-1 i-checks\" title=\"停用\"><input type=\"radio\" name=\"isCopy\" value=\"1\"/>拷贝</span>");
	}else{
		$("#copyDiv").css("display","none");
	}*/
}

//加载媒体板块
function initMediaType() {
	requestData(null,"/mediaPlate/userId","get","json",false, function (data) {
		if (data == null || data == '') {
			swal("没有板块可操作！", "没有查询到板块信息，请联系管理员赋权！", "warning");
			return;
		}
		requestData(null,"/mediaAudit/getMediaNumbers/0","get","json",false,function (mediaTotal) {
			var mediaTotalMap = {};
			$.each(mediaTotal.data.data, function (i,value) {
				mediaTotalMap[value.plateId] = value.mediaTotal;
			});
			var standardHtml = "";
			var notStandardHtml = "";
			var mcount = 0;
			$(data).each(function (i, item) {
				mcount = mediaTotalMap[item.id];
				mcount = mcount ? mcount : 0;
				if (item.standarPlatformFlag == 1) {
					standardHtml += "<div style='width: 10%;float: left;'><span style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;width: 100%;' class='btn btn-outline plateSpan' data-standarPlatformFlag='" + item.standarPlatformFlag + "' title='" + item.name + "' data-mcount='" + mcount + "' data-value='" + item.id + "' onclick='setType(" + item.id + ",this)'>" + item.name + "(" + mcount + ")" + "</span></div>";
				} else {
					notStandardHtml += "<div style='width: 10%;float: left;'><span style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;width: 100%;' class='btn btn-outline plateSpan' data-standarPlatformFlag='" + item.standarPlatformFlag + "' title='" + item.name + "' data-mcount='" + mcount + "' data-value='" + item.id + "' onclick='setType(" + item.id + ",this)'>" + item.name + "(" + mcount + ")" + "</span></div>";
				}
			});

			//判断标准和非标准是否有，控制隐藏显示
			if (standardHtml) {
				$("#extendFormStandardPlateWrap").css("display", "flex");
				$("#extendFormStandardPlate").html(standardHtml);
			} else {
				$("#extendFormStandardPlateWrap").css("display", "none");
				$("#extendFormStandardPlate").html("");
			}
			if (notStandardHtml) {
				$("#extendFormNotStandardPlateWrap").css("display", "flex");
				$("#extendFormNotStandardPlate").html(notStandardHtml);
			} else {
				$("#extendFormNotStandardPlateWrap").css("display", "none");
				$("#extendFormNotStandardPlate").html("");
			}
			var type = getQueryString("type");
			if (type && type === '2') {
				$("#mediaPlate span[data-value='" + type + "']").click();
			} else {
				$("#mediaPlate>div:first-child>span:first-child").click();

				//如果标准有值，则先查询标准的
				if (standardHtml) {
					$("#extendFormStandardPlate > div:first-child > span:first-child").click();
				} else {
					if (notStandardHtml) {
						$("#extendFormNotStandardPlate > div:first-child > span:first-child").click();
					}
				}
			}
		});
	});
	// createTable(); //初始化表格
}

//查询当前用户的媒体板块id
function queryUserMediaPlateIds() {
    $.ajax({
        url: baseUrl + "/mediaPlate/userId",
        data: {"userId": user.id},
        type: "post",
        dataType: "json",
        success: function (data) {
            if (data) {
                for (var i = 0; i < data.length; i++) {
                    mediaUserPlateMap.push(data[i].id);
                }
            }
        }
    });
}

//表格定义
function createTable() {
	var termForm = $("#termForm").serializeJson();
	var $tableMedias = $("#table_medias");
	$tableMedias.jqGrid({//2600
		url: baseUrl + '/mediaAudit/listMedia',
		datatype: "json",
		postData: termForm,
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
		sortable: true,
		sortname: "id",
		multiselect: true,
		shrinkToFit: true,
		prmNames: {rows: "size"},
		rowNum: 10,//每页显示记录数
		rowList: [10, 50, 100],//分页选项，可以下拉选择每页显示记录数
		jsonReader: {
			root: "list", page: "pageNum", total: "pages",
			records: "total", repeatitems: false, id: "id"
		},
		prmNames: {
			page: "page",
			rows: "size",
			totalrows: "totalElements",
			sort: "sort",
			order: "order",
		},
		colModel: [
			{
				name: 'id',
				label: 'id',
				editable: true,
				hidden: true,
				sortable: true,
				sorttype: "int",
				search: true
			},
			// {
			// 	name: 'picPath',
			// 	label: '媒体图标',
			// 	editable: true,
			// 	width: 20,
			// 	align: "center",
			// 	formatter: function (v, options, row) {
			// 		if (!v){
			// 			return '<img class="head-img" src="/img/mrt.png"/>';
			// 		}else{
			// 			return '<img class="head-img" src="' + v + '" onerror="src=\'/img/mrt.png\'"/>';
			// 		}
			// 	}
			// },
			{
				name: 'mediaName',
				label: '媒体名称',
				editable: true,
				width: 30,
				align: "center",
				hidden: true,
				formatter: function (v, options, row) {
					return row.name;
				}
			},
			{
				name: 'name',
				label: '媒体名称',
				editable: true,
				width: 30,
				align: "center"
				// formatter: function (v, options, row) {
				// 	if (!row.link){
				// 		return v;
				// 	}else{
				// 		return "<a class='text-success' target='_blank' href='" + row.link + "'>" + v + "</a>";
				// 	}
				// }
			},
			{
				name: 'mediaContentId',
				label: '唯一标识',
				editable: true,
				width: 30,
				align: "center",
			},
			{
                name: 'link',
                label: '媒体链接',
                editable: true,
                width: 30,
                align: "center",
                formatter: function (v, options, row) {
                    if (!row.link){
                        return v;
                    }else{
                        return "<a class='text-success' target='_blank' href='" + row.link + "'>" + v + "</a>";
                    }
                }
			},
			{
				name: 'regionId',
				label: 'regionId',
				editable: true,
				hidden: true,
				sortable: true,
				sorttype: "int",
				search: true
			},
			{
				name: 'plateId',
				label: 'plateId',
				editable: true,
				hidden: true,
				sortable: true,
				sorttype: "int",
				search: true
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
				name: 'userId',
				label: '责任人',
				width: 12,
				align: "center",
				hidden: true
			},
			{
				name: 'user.userName',
				label: '责任人',
				width: 12,
				align: "center",
				hidden: false
			},
			{
				name: 'userContact',
				label: '责任人联系方式(电话/微信/QQ)',
				width: 25,
				align: "center",
				hidden: false,
				formatter: function (v, options, row) {
					if (row.user) {
						return (row.user.phone || "-") + "/" + (row.user.wechat || "-") + "/" + (row.user.qq || "-")
					} else {
						return "-";
					}
				}
			},
			{
				name: 'companyCode',
				label: '所属公司',
				width: 20,
				align: "center",
				hidden: true
			},
			{
				name: 'companyCodeName',
				label: '所属公司',
				width: 20,
				align: "center",
				hidden: false
			},
			{
				name: 'updateDate',
				label: '更新时间',
				width: 20,
				align: "center",
				formatter: "date",
				formatoptions: {srcformat: 'Y-m-d H:i:s', newformat: 'Y-m-d'},
				hidden: true
			},
			{
				name: 'enabled',
				label: '是否启用',
				width: 20,
				align: "center",
				hidden: false,
				formatter: function (v, options, row) {
					if(row.enabled == 0){
						return "<span style='color: green;'>启用</span>";
					}else{
						return "<span style='color: red;'>停用</span>";
					}
				}
			},
			{
				name: 'isCopy',
				label: '是否拷贝',
				width: 12,
				align: "center",
				hidden:true,//user.dept.companyCode == 'XH' ? false : true,
				formatter: function (v, options, row) {
					if(row.isCopy == 1){
						return "拷贝";
					}else{
						return "自建";
					}
				}
			},
			{
				label: "价格",
				name: 'priceExt',
				editable: true,
				sortable: true,
				width: 40,
				align: "left",
				hidden: false,
				formatter: function (v, options, row) {
					var html = "";
					var j = 1;
					//将底价也加进来
					// html += "<div class='col-md-12' style='text-align:center;padding: 0'><span >底价</span>:<span class='text-danger font-bold'>￥"+ row.price+"</span></div><br/>";
					var val = row.mediaExtends;
					$(val).each(function (i, item) {
						var align = j % 2 == 0 ? "right" : "left";
						if (item.type == 'price' && item.cellValue && item.cellValue != 0) {
							/*<input data-value='" + item.cellValue + "' data-id='" + row.id + "' onclick='fixContainerObj.clickPriceExt(" + row.id + "," + item.id + ")' type='radio' value='" + item.id + "' class='i-checks' name='price" + row.id + "' />*/
							html += "<div class='col-md-12' style='text-align:center;padding: 0'><span >" + item.cellName + "</span>:<span class='text-danger font-bold'>￥"+ parseFloat(item.cellValue).toFixed(2)+"</span></div>";
							if (j++ % 1 == 0) {
								html += "<br/>";
							}
						}
					});
					return html;
				}
			},
			{
				label: "扩展",
				name: 'mediaExtends',
				width: 50,
				align: "left",
				formatter: function (v, options, row) {
					var html = "";
					var j = 1;
					$(v).each(function (i, item) {
						if (item.type != 'price') {
							var value = item.cellValue;
							var text = "无";
							if(item.cellValue){
								if(item.dbType == 'select' || item.dbType == 'radio' || item.dbType == 'checkbox'){
									text = item.cellValueText;
								}else{
									text = item.cellValue;
								}
							}
							if(item.type == 'link' && "无" != text){
								html += "<div class='col-md-6' style='text-align:left;padding: 0;padding-right: 5px;'><span style='float:left' >" + item.cellName + ":</span><a class='text-success' style='float:left' target='_blank' href='"+text+"' title='"+text+"'>进入链接</a></div>";
							}else{
								html += "<div class='col-md-6' style='text-align:left;padding: 0;padding-right: 5px;'><span style='float:left' >" + item.cellName + ":</span><span class='text-danger' style='float:left'  >" + text + "</span></div>";
							}
							if (j++ % 2 == 0) {
								html += "<br/>";
							}
						}
					});
					return html;
				}
			},
			{
				name: 'state1',
				label: '审核状态',
				width: 12,
				align: "center",
				hidden: true,
				formatter: function (v, options, row) {
					return row.state;
				}
			},
			{
				name: 'state',
				label: "审核状态",
				editable: true,
				sortable: true,
				hidden: false,
				width: 12,
				align: "center",
				formatter: function (v, options, row) {
					switch (v) {
						case 0:
							return '<span class="text-muted">未审核</span>';
						case 1:
							return '<span class="text-success">已通过</span>';
						case -1:
							return '<span class="text-danger">已驳回</span>';
						case -9:
							return '<span class="text-justify">已删除</span>';
					}
				}
			},
			{
				name: 'remarks',
				label: '备注',
				editable: true,
				width: 25,
				align: "center",
				hidden: false
			},
			{
				name: 'dataSource',
				label: '数据来源',
				width: 20,
				align: "center",
				hidden: false,
				formatter: function (v, options, row) {
					if (v == 0) {
						return "<span style='color: green;'>手工导入</span>";
					} else {
						return "<span style='color: orangered;'>自动爬取</span>";
					}
				}
			},
			{
				name: 'copyRemarks',
				label: '拷贝备注',
				editable: true,
				width: 25,
				align: "center",
				hidden:true,//user.dept.companyCode == 'XH' ? false : true,
			},
			{
				label: "复投率",
				name: 'ftRecord',
				editable: true,
				sortable: true,
				width: 20,
				align: "left",
				hidden: false,
				formatter: function (v, options, row) {
					var html = "";
					var three = 0;
					var six = 0;
					var year = 0;
					if(row.ftRecord){
						three = row.ftRecord.three || 0;
						six = row.ftRecord.six || 0;
						year = row.ftRecord.year || 0;
					}
					html += "<div class='col-md-12' style='text-align:center;padding: 0'><span>近三月</span>:<span class='text-danger font-bold'>" + three+ "</span></div><br/>";
					html += "<div class='col-md-12' style='text-align:center;padding: 0'><span>近半年</span>:<span class='text-danger font-bold'>" + six+ "</span></div><br/>";
					html += "<div class='col-md-12' style='text-align:center;padding: 0'><span>近一年</span>:<span class='text-danger font-bold'>" + year+ "</span></div>";
					return html;
				}
			},
			{
				label: "操作",
				width: 30,
				hidden: false,
				formatter: function (v, options, row) {
					var html = "";
					//指派权限，只能由媒介总监
					if(isMJZJ()){
						html += "<a class='text-success' onclick='meidaAssign(" + row.id + ", \""+row.userId+"\")'>指派&nbsp;</a>";
					}
					//仅有责任人自己可以操作启用停用
					if(user.id == row.userId && row.state == 1){
						if(row.enabled == 0){
							html += "&nbsp;<a class='text-success' onclick='opMedia(\"stop\"," + row.id + ",\""+row.companyCode+"\""+","+row.userId+")'>停用&nbsp;</a>";
						}else{
							html += "&nbsp;<a class='text-success' onclick='opMedia(\"active\"," + row.id + ",\""+row.companyCode+"\""+","+row.userId+")'>启用&nbsp;</a>";
						}
					}
					html += "<a class='text-success' onclick='lookSupplierPrice(" + row.id + ", \""+(row.name || row.mediaContentId)+"\")'>价格详情&nbsp;</a>";
					html += "<a class='text-success' onclick='mediaArtStatisticsObj.lookStatisticsInfo(" + row.id + ", \""+row.name+"\")'>复投详情&nbsp;</a>";
					html += "<a class='text-success' onclick='mediaChangeObj.mediaChangeShow("+row.plateId+", "+row.id+","+row.userId+");'>异动详情&nbsp;</a>";
					//祥和部长和组长可以拷贝分公司下审核通过的媒体
					/*if(currentDeptQx && companyCode == 'XH' && row.companyCode != 'XH' && row.state == 1){
						html += "&nbsp;<a class='text-success' onclick='copyMedia(\"copyMedia\"," + row.id + ")'>拷贝&nbsp;</a>";
					}*/
					return html;
				}
			},
		],
		pager: "#pager_medias",
		viewrecords: true,
		caption: "媒体列表",
		hidegrid: false,
		loadComplete: function (data) {
			if (getResCode(data))
				return;
			var isPrice;
			$(data.list).each(function (i, item) {
				if (item.type == 'price') isPrice = true;
			});
			if (isPrice) {
				$(this).setGridParam().hideCol("priceExt");
			} else {
				$(this).setGridParam().showCol("priceExt");
			}
		},
		ondblClickRow: function (rowid, iRow, iCol, e) {
			var rowData = $("#table_medias").jqGrid("getRowData", rowid);   //获取选中行信息
            //可以进入修改界面，但是非责任人只能进行绑定供应商
            page('/media1/mediaEdit?id=' + rowid + "&standarPlatformFlag=" + $("#standarPlatformFlag").val() + "&mediaPlateName=" + $("#mediaTypeText").attr("title") + "&mediaName=" + rowData.mediaName, '媒体修改');
		},
		gridComplete: function () {
			var width = $('#table_medias').closest('.jqGrid_wrapper').width() || $(document).width();
			$('#table_medias').setGridWidth(width);

			var plateId = $("#plateId").val();
			var $plateNode = $("#mediaPlate span[data-value='"+plateId+"']");
			var mediaNum = $("#table_medias").jqGrid("getGridParam","records");
			$plateNode.attr("data-mcount",mediaNum);
			$plateNode.text($plateNode.attr("title")+"("+mediaNum+")");
			$("#mediaTypeText").text($plateNode.text());
		}
	});
	$tableMedias.jqGrid('setLabel', 'rn', '序号', {'text-align': 'center'}, '');
	$tableMedias.setSelection(4, true);
	var width = $tableMedias.closest('.jqGrid_wrapper').width() || $(document).width();
	$tableMedias.setGridWidth(width);
}

//刷新表格
function reflushTable() {
	//标准平台获取唯一标识值
    if($("#standarPlatformFlag").val() && $("#standarPlatformFlag").val() == 1){
        $("#mediaContentId").val($("#standarPlatformId").val());
    }else {
        $("#mediaContentId").val("");
    }
	$("#mediaName").val($("#mName").val()); //媒体名称
	$("#supplierName").val($("#contactName").val()); //供应商名称
	var json = $("#termForm").serializeJson();
	var condition = {};
	for (var k in json) {
		if (k.indexOf("cell:") > -1) {
			var value = json[k];
			var kk = k.substring(5, k.length);
			var arr = kk.split(":");
			var length = arr.length; //2-代表不是区间的数，3-自定义栏的区间数值（最后一个是min: 开始值，max：结束值）
			var cell = arr[0];
			var type = arr[1];
			var k3 = null;
			if(length == 3){
				k3 = arr[2];//该值为min 或 max
			}
			if(type == "checkbox" && Array.isArray(value)){ //如果是数组，则转成字符串
				value = value.join(",");
			}
			//由于区间的cell相同，所以cellValueStart 和 cellValueEnd需要放在同一个对象中
			if(!condition[cell]){
				if(k3 && k3 == 'min'){
					condition[cell] = {cell:cell, type:type, cellValueStart:value};
				}else if(k3 && k3 == 'max'){
					condition[cell] = {cell:cell, type:type, cellValueEnd:value};
				}else{
					condition[cell] = {cell:cell, type:type, cellValue:value};
				}
			}else{
				if(k3 && k3 == 'min'){
					condition[cell].cellValueStart = value;
				}else if(k3 && k3 == 'max'){
					condition[cell].cellValueEnd = value;
				}else{
					condition[cell].cellValue = value;
				}
			}
			delete json[k];
		}/*else if(k == 'wechatId'){
			condition[cell] = {cell:'wechatId', type:'text', cellValue:json[k], wechatIdFlag: 1};
			delete json[k];
		}else if(k == 'ksId'){
			condition[cell] = {cell:'ksId', type:'text', cellValue:json[k], ksIdFlag: 1};
			delete json[k];
		}*/

	}
	var extendArr = new Array();
	if(condition && Object.getOwnPropertyNames(condition).length > 0){
		for(var key in condition){
			extendArr.push(condition[key]);
		}
	}
	json.extendParams = JSON.stringify(extendArr);

	//刷新表格
	$("#table_medias").emptyGridParam(); //清空历史查询数据
	//根据板块ID判断是否显示案例链接还是ID字段
	var colModels = $("#table_medias").jqGrid('getGridParam', 'colModel');
	$(colModels).each(function (j, colModel) {
		if (colModel.name == "mediaContentId") {
			if($("#standarPlatformFlag").val() && $("#standarPlatformFlag").val() == 1){
				$("#table_medias").setGridParam().showCol(colModel.name);
			}else {
				$("#table_medias").setGridParam().hideCol(colModel.name);
			}
		}
	});
	$("#table_medias").jqGrid('setGridParam', {
		postData: json, //发送数据
	}).trigger("reloadGrid"); //重新载入
}

function setType(id, t) {
	var standarPlatformFlag = $(t).attr("data-standarPlatformFlag") || 0;
	var backColor = standarPlatformFlag == 1 ? "btn-primary" : "btn-danger";
	$(t).closest(".plateWrap").find(".plateSpan").each(function (i, item) {
		$(item).removeClass("btn-primary");
		$(item).removeClass("btn-danger");
		if (t == item) {
			$(t).addClass(backColor);
		}
	});

	layer.close(artMediaSupplierReplaceObj.modalIndex);
	$("#standarPlatformFlag").val($(t).attr("data-standarPlatformFlag") || 0);//设置是否平台标准
	$("#mediaTypeText").text($(t).text());
	$("#mediaTypeText").attr("title",$(t).attr("title"));
	$("#plateId").val(id);

	$("#mediaContentId").val(""); //清空上一次内容
	$("#standarPlatformId").val(''); //初始化唯一标识
	$("#mediaName").val('');//初始化媒体名称
	$("#mName").val(''); //初始化媒体名称
	$("#supplierName").val('');//初始化供应商名称
	$("#contactName").val(''); //初始化供应商名称
	$("#enabled").val('');//初始化是否启用
	$("#dataSource").val('');//初始化数据来源
	$("#isCopy").val('');//初始化是否拷贝
	$("#userId").val('');  //初始化责任人
	// 清空之后动态加载的查询条件；
	$("#plateId").nextAll().remove();

	//如果是标准平台，唯一标识搜索
	if($("#standarPlatformFlag").val() && $("#standarPlatformFlag").val() == 1){
		$("#standarPlatformId").css("display","inline-block");
	}else{
		$("#standarPlatformId").css("display","none");
	}

	$("#mediaTermUL").html(mediaTermULDefaultHTML);//每次选媒体板块，重新覆盖

	//所属公司
/*	if(!companyMap){
		requestData(null,"/dept/listAllCompany","get","json",false,function (data) {
			companyMap = data.data.result;
			renderCompany(companyMap);
		});
	}else {
		renderCompany(companyMap);
	}*/

	//查询条件
	if(!mediaTermMap[id]){
		requestData(null,"/mediaTerm1/" + id,"get","json",false,function (datas) {
			mediaTermMap[id] = datas; //缓存媒体查询条件
			renderPageCondition(datas);//渲染页面条件
		});
	}else{
		renderPageCondition(mediaTermMap[id]);//渲染页面条件
	}
	createTable();//重新创建表格
	reflushTable(); //刷新表格数据

	$('.i-checks').iCheck({
		checkboxClass: 'icheckbox_square-green',
		radioClass: 'iradio_square-green',
	});

	layui.use('form', function(){
		layui.form.render('select');//layui重新渲染下拉列表
	});
}

//所属公司
/*function renderCompany(data) {
	var dd="''";
	var html = '<span class="col-md-1"><span class="text-danger bg-danger" style="padding: 5px" onClick="setCompanyType('+dd+',this)">不限</span></span>';
	$(data).each(function (i, item) {
		html += " <span class=\"col-md-1\"><span class=\"\" style=\"padding: 5px\" onclick='setCompanyType(\""+item.code+"\",this)'>"+item.name+"</span></span>";
	})
	$("#companyType").html(html);
}*/

//重新渲染页面条件
function renderPageCondition(datas) {
	var html = '';
	var priceHtml = '';//缓存金额和数字范围HTML
	$(datas).each(function (i, data) {
		var data = data;
		var cellName = data.cellName;
		var cell = "cell:" + data.cell + ":" + data.type;
		switch (data.type) {
			case 'radio':
			case 'checkbox':
				html += "<li class='col-md-12'><label class=\"col-md-1\" style='float:left;'>";
				html += cellName + '：</label><div class="col-md-11" style="padding-left: 15px;">';
				var dataClass = data.type == "radio" ? "radio-inline" : "checkbox-inline";
				var dbHtml = "";
				if(data.dbJson){  //如果dbJson字段有值则使用，否为dbsql有值
					var json = eval(data.dbJson);
					if (!Array.isArray(json)){
						json = [json];
					}
					$.each(json, function (i, item) {
						var text = item.hasOwnProperty("text") ? item.text : item.value;
						dbHtml += "<span class='"+dataClass+" col-md-1 i-checks' title='"+text+"' style=''><input id=\"${name}\" name=\"${name}\" cell-name=\"${cell-name}\" type='"+data.type+"' value='" + item.value + "' /> " + text + "</span>";
					});
				}else{
					var datas = data.datas;
					if (datas && Object.getOwnPropertyNames(datas).length > 0) {  //如果对象存在，并且个数大于0
						for(var key in datas){
							dbHtml += "<span  class='"+dataClass+" col-md-1 i-checks' title='"+datas[key].name+"'><input id=\"${name}\" name=\"${name}\" cell-name=\"${cell-name}\" type='"+data.type+"' value='" + datas[key].id + "' /> " + datas[key].name + "</span>";
						}
					}
				}
				html += dbHtml;
				html += "</div></li>"

				break;
			case 'select':
				html += "<li class='col-md-12'><label class=\"col-md-1\" style='float:left;'>";
				html += cellName + '：</label><div class="col-md-11" style="padding: 0">';
				var dd="''";
				var dbHtml = '<span class="col-md-1" title="不限"><span class="text-danger bg-danger" style="padding: 5px" onclick="loadMedia(\''+cell+'\','+dd+',this)">不限</span></span>';
				if(data.dbJson){  //如果dbJson字段有值则使用，否为dbsql有值
					var json = eval(data.dbJson);
					if (!Array.isArray(json)){
						json = [json];
					}
					$.each(json, function (i, item) {
						var text = item.hasOwnProperty("text") ? item.text : item.value;
						dbHtml += '<span class="col-md-1" title="'+text+'"><span class="" style="padding: 5px" onclick="loadMedia(\''+cell+'\','+item.value+',this)">'+text+'</span></span>';
					});
				}else{
					var datas = data.datas;
					if (datas && datas.length > 0) {  //如果对象存在，并且个数大于0
						for(var key in datas){
							dbHtml += '<span class="col-md-1" title="'+datas[key].name+'"><span class="" style="padding: 5px" onclick="loadMedia(\''+cell+'\','+datas[key].id+',this)">'+datas[key].name+'</span></span>';
						}
					}
				}
				html += dbHtml;
				html += "</div></li>"
				break;
			case 'price':
			case 'number':
				var nameMin = cell + ":min";
				var nameMax = cell + ":max";
				priceHtml += "<span class=\"col-md-3 form-inline\">\n" +
				"             	<input name=\"${nameMin}\" size=\"1\" cell-name=\"${cell-name}\" class=\"form-control\">" +
                "              -<input name=\"${nameMax}\" size=\"1\" cell-name=\"${cell-name}\" class=\"form-control\">&nbsp;\n" +
				"               <label class=\"btn btn-sm btn-danger\">"+cellName+"</label>\n" +
				"             </span>";
				priceHtml = priceHtml.th("nameMin",nameMin).th("nameMax",nameMax);
				break;
			default:
				html += "<li class='col-md-12'><label class=\"col-md-1\" style='float:left;'>";
				html += cellName + ':</label><div class="col-md-11" style="padding: 0">';
				html += data.dbHtml;
				html += "</div></li>";
				break;
		}
		html = html.th('id', cell).th('name', cell).th('labelName', cellName).th('type', data.type).th("cell-name", cellName);
		priceHtml = priceHtml.th('id', cell).th('name', cell).th('labelName', cellName).th('type', data.type).th("cell-name", cellName);
	});
	//循环结束后，添加价格区间
	if(priceHtml){
		html += "<li class='col-md-12'><label class=\"col-md-1\" style='float:left;'> 自定义栏：</label><div class=\"col-md-11\" style='padding: 0px;'>";
		/*html += "<span class=\"col-md-3 form-inline\">\n" +
			"             	<input name=\"priceStart\" size=\"1\" cell-name=\"priceStart\" class=\"form-control\">" +
			"              -<input name=\"priceEnd\" size=\"1\" cell-name=\"priceEnd\" class=\"form-control\">&nbsp;\n" +
			"               <label class=\"btn btn-sm btn-danger\">底价</label>\n" +
			"             </span>";*/
		html += priceHtml;
		html += "<span class=\"col-md-3 form-inline\"><label class=\"btn btn-sm btn-success\" onclick=\"loadMediaData(this);\">查询</label></span>";
		html += "</div></li>";
	}

	$("#mediaTermUL").html($("#mediaTermUL").html() + html); //在公共查询条件后面追加
	// 调整样式为居左对齐；
	$(".col-md-12 > label").css({"text-align": "left", "width": "120px", "margin": "0px", "padding": "0px"});
	$(".col-md-11 > span").css({"text-align": "left"});

	//单选和复选框添加选中事件
	$("#mediaTermUL").find('.i-checks').on('ifClicked', function (event) {
		var input = $(this).find("input");
		if (input.attr("name") != 'enabled' && input.attr("name") != 'isCopy' && input.attr("name") != 'dataSource') {
			loadMedia(input.attr("name"), input.val(), input);
		}else{
			if (input.attr("name") == "enabled") {
				$("#enabled").val(input.val());//初始化是否启用
			} else if (input.attr("name") == "dataSource") {
				$("#dataSource").val(input.val());//初始化是否启用
			}else{
				$("#isCopy").val(input.val());//初始化是否拷贝
			}
			reflushTable();
		}
	});
}

//根据公司查询
function setCompanyType(code,t){
	$(t).parent().parent().parent().find("div>span>span").each(function (i, item) {
		if (t == item) {
			if($(item).hasClass("text-danger")){ //如果是页面点击span，则再次点击移除
				$(item).removeClass("text-danger bg-danger");
				$(item).parent().siblings(":first").find("span").addClass("text-danger bg-danger");
				code = "";
			}else{
				$(item).removeClass("text-danger bg-danger");
				$(t).addClass("text-danger bg-danger");
			}
		}else{
			$(item).removeClass("text-danger bg-danger");
		}
	});
	$("#companyCode").val(code);
	reflushTable();
}

//加载查询条件
function loadMedia(name, value, target) {
	//判断是否有值，有值则重新添加
	if(value){
		if ($(target)[0].tagName.toLowerCase() == 'span') { //如果是页面点击span，则再次点击移除
			$("#termForm > input[name='" + name + "']").remove(); // 先删除原有的同name条件；
			if($(target).hasClass("text-danger")){ //如果已经是点击状态，则再次点击取消该条件
				$(target).removeClass("text-danger bg-danger");
				$(target).parent().siblings(":first").find("span").addClass("text-danger bg-danger");
			}else{
				$(target).parent().parent().find("span").removeClass("text-danger bg-danger");
				$(target).addClass("text-danger bg-danger");
				var input = "<input type='hidden' name='" + name + "' value='" + value + "'/>";
				$("#termForm").append(input);
			}
		}else if($(target)[0].tagName.toLowerCase() == 'input' && $(target).attr("type") == "checkbox"){
			if(!$(target).parent().hasClass("checked")){  //判断是否已被选中，未被选中，新增
				var input = "<input type='hidden' name='" + name + "' value='" + value + "'/>";
				$("#termForm").append(input);
			}else{
				$("#termForm input[name='"+name+"']").each(function (i,node) {
					if($(node).val() == value){
						$(node).remove();
					}
				})
			}
		}else{
			$("#termForm > input[name='" + name + "']").remove(); // 先删除原有的同name条件；
			var input = "<input type='hidden' name='" + name + "' value='" + value + "'/>";
			$("#termForm").append(input);
		}
	}else{
		// 先删除原有的同name条件；
		$("#termForm > input[name='" + name + "']").remove();
		$(target).parent().parent().find("span").removeClass("text-danger bg-danger");
		$(target).addClass("text-danger bg-danger");
	}
	reflushTable();
}

// 自定义栏的查询方法；
function loadMediaData(obj) {
	$(obj).closest("div").find("input").each(function () {
		//底价特别处理
		if($(this).attr("name") == "priceStart" || $(this).attr("name") == "priceEnd"){
			$("#termForm #"+$(this).attr("name")).val($(this).val());
		} else {
			// 先移除；
			$("#termForm > input[name='" + $(this).attr("name") + "']").remove();
			// 如果有值则存储；
			if ($(this).val().length > 0) {
				$("#termForm").append($(this).clone());
			}
		}
	});
	reflushTable();
}

//查看供应商价格
function lookSupplierPrice(mediaId, mediaName) {
	$("#modalMediaId").val(mediaId); //设置媒体ID
	$("#dialogTitle").html("["+mediaName+"]-供应商价格"); //设置模态框标题
	$("#mediaSupplierPriceModal").modal("toggle");
}

//拷贝分公司媒体
function copyMedia(type, id) {
	requestData(null,"/mediaAudit/" + type + "/" + id, "get", "json", true,function (data) {
		copyFlag = false;
		if(type == "copyMediaRelate"){
			$("#mediaSupplierPriceModal").modal("toggle");
		}
		if (data.code == 200) {
			layer.msg("成功");
			reflushTable();//刷新表格
			triggerPageBtnClick("/media1/mediaSupplierList","mediaListSearchBtn"); //触发媒体管理Tab刷新
			triggerPageBtnClick("/media1/audits","auditSearchBtn"); //触发媒体审核Tab刷新
			triggerPageBtnClick("/media/supplier_manage","search"); //触发供应商管理Tab刷新
			triggerPageBtnClick("/media1/mediaSupplierAuditList","mediaListSearchBtn"); //触发媒体关系审核Tab刷新
		} else {
			if (getResCode(data)) {
				return;
			}else{
				swal({
					title: "很抱歉，媒体拷贝失败！",
					text: data.msg,
					type: "error",
					html: true
				});
			}
		}
	});
}

//启用和停用媒体
function opMedia(type, id, mediaCompanyCode, userId) {
	var opName = type == "active" ? "启用" : "停用"; //操作名称
	var errorInfo = "";
	if(user.id != userId){
		errorInfo = "只有该媒体责任人才能" + opName;
	}
	if(errorInfo){
		swal({
			title: "很抱歉，媒体操作失败！",
			text: errorInfo,
			type: "warning",
			html: true
		});
		return;
	}
	requestData(null,"/mediaAudit/" + type + "/" + id + "/" + ($("#standarPlatformFlag").val() || 0), "get", "json", true,function (data) {
		if (data.code == 200) {
			layer.msg("成功");
			reflushTable();//刷新表格
			triggerPageBtnClick("/media1/audits","auditSearchBtn"); //触发媒体审核Tab刷新
		} else {
			if (getResCode(data)) {
				return;
			}else{
				swal({
					title: "很抱歉，媒体操作失败！",
					text: data.msg,
					type: "error",
					html: true
				});
			}
		}
	});

}

//初始化供应商价格表
function initSupplierTable(){
	supplierPriceTableObj.grid = new dataGrid("mediaSupplierPriceTable", supplierPriceTableObj.supplierPriceTable);
	supplierPriceTableObj.grid.loadGrid();
	var supplierPriceNode = $("#mediaSupplierPriceTable");
	supplierPriceNode.setGridWidth(supplierPriceNode.closest('.jqGrid_wrapper').width());
}
//媒体供应商价格表对象
var supplierPriceTableObj = {
	grid: {},
	supplierPriceTable: {
		datatype: "local",
		height: "auto",
		page: 1,//第一页
		autowidth: true,
		rownumbers: true,
		gridview: true,
		viewrecords: true,
		multiselect: false,
		sortable: true,
		shrinkToFit: true,
		// colNames: ['媒体ID','媒体公司代码','媒体名称','供应商','供应商联系人','价格','是否拷贝','拷贝备注','操作'],//表头
		colModel: [  //这里会根据index去解析jsonReader中root对象的属性，填充cell
			{
				name: 'mediaId',
				label: '媒体Id',
				editable: true,
				hidden: true,
				sortable: true,
				sorttype: "int",
				search: true
			},
			{
				name: 'userId',
				label: '责任人Id',
				editable: true,
				hidden: true,
				sortable: true,
				sorttype: "int",
				search: true
			},
			{
				name: 'companyCode',
				label: '媒体公司代码',
				editable: true,
				hidden: true,
				sortable: true,
				sorttype: "int",
				search: true
			},
			{
				name: 'mediaName',
				label: '媒体名称',
				editable: true,
				width: 40,
				align: "center",
				cellattr: function (rowId, tv, rawObject, cm, rdata) {
					//合并单元格，通过ID来判断是否合并
					return "id='mediaName" + rowId + "'";
				}
			},
			{
				name: 'mediaContentId',
				label: '唯一标识',
				editable: true,
				width: 30,
				align: "center",
			},
			{
				name: 'supplierCompanyName',
				label: '供应商',
				editable: true,
				width: 30,
				align: "center",
				// cellattr: function (rowId, tv, rawObject, cm, rdata) {
				// 	//合并单元格，通过ID来判断是否合并
				// 	return "id='supplierCompanyName" + rawObject.supplierId + "'";
				// },
				formatter: function (value, grid, rowData) {
				/*	var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
					var companyCode = user.dept.companyCode; //获取公司代码
					//祥和的组长和部长能看到分公司的供应商名称、联系人，普通媒介，只能看到本公司，当前板块的。分公司只能看到自己公司的
					if(rowData.companyCode == companyCode){ //自己公司
						return value;
					}else{ //其他公司的媒体，只有祥和的部长和组长能看到供应商
						if(companyCode == "XH" && currentDeptQx){
							return value;
						}else{
							return "******";
						}
					}*/
					return value || "";
				}
			},
            {
                name: 'supplierName',
                label: '供应商联系人',
                editable: true,
                width: 30,
                align: "center",
                // cellattr: function (rowId, tv, rawObject, cm, rowData) {
                // 	//合并单元格，通过ID来判断是否合并
                // 	return "id='supplierName" + rawObject.supplierId + "'";
                // },
                formatter: function (value, grid, rowData) {
                    /*var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
                    var companyCode = user.dept.companyCode; //获取公司代码
                    //祥和的组长和部长能看到分公司的供应商名称、联系人，普通媒介，只能看到本公司，当前板块的。分公司只能看到自己公司的
                    if(rowData.companyCode == companyCode){ //自己公司
                        return value;
                    }else{ //其他公司的媒体，只有祥和的部长和组长能看到供应商
                        if(companyCode == "XH" && currentDeptQx){
                            return value;
                        }else{
                            return "******";
                        }
                    }*/
                    return value || "";
                }
            },
            {
                name: 'supplierPhone',
                label: '供应商电话',
                editable: true,
                width: 30,
                align: "center",
                formatter: function (value, grid, rows) {
                    if(rows.prices && rows.prices.length > 0){
                        var flag = false;
                        value = rows.prices[0].supplierPhone;
                        var plateIds="";
                        if(rows.prices[0].plateIds!=null){
                            plateIds = rows.prices[0].plateIds.split(",");//供应商对应的媒体板块id
                        }
                        var creator = rows.prices[0].supplierCreator;//供应商负责人
                        if (plateIds) {
                            for (var i = 0; i < plateIds.length; i++) {
                                if (mediaUserPlateMap.contains(plateIds[i])) {
                                    //当前用户的板块包含了该供应商的板块
                                    flag = true;
                                }
                            }
                        }
                        //1、仅责任人自己能看到   或者拥有板块的 组、部 、 总监
                        if ((user.id == creator) || (flag && rows.prices[0].flag)) {
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
                    }else{
                        return "";
                    }
                }
            },
			{
				name: 'supplireCompanyCodeName',
				label: '供应商所属公司',
				editable: true,
				width: 30,
				align: "center",
				hidden:true
			},
			{
				label: "价格",
				name: 'prices',
				editable: true,
				sortable: true,
				width: 50,
				align: "left",
				hidden: false,
				formatter: function (v, options, row) {
					var html = "";
					var val = row.prices;
					var j = 1;
					$(val).each(function (i, item) {
						if (item.cellType == "price" && item.cellValue && item.cellValue != 0) {
							/*<input data-value='" + item.cellValue + "' data-id='" + row.id + "' onclick='fixContainerObj.clickPriceExt(" + row.id + "," + item.id + ")' type='radio' value='" + item.id + "' class='i-checks' name='price" + row.id + "' />*/
							html += "<div class='col-md-12' style='text-align:center;padding: 0'><span >" + item.cellName + "</span>:<span class='text-danger font-bold'>￥"+ parseFloat(item.cellValue).toFixed(2)+"</span></div>";
							if (j++ % 1 == 0) {
								html += "<br/>";
							}
						}
					});
					return html;
				}
			},
			{
				label: "扩展字段",
				name: 'extendFields',
				editable: true,
				sortable: true,
				width: 70,
				align: "left",
				hidden: false,
				formatter: function (v, options, row) {
					var html = "";
					var j = 1;
					$(row.prices).each(function (i, item) {
						if (item.cellType && item.cellType != 'price') {
							var text = "无";
							if(item.cellValue){
								if(item.cellType == 'select' || item.cellType == 'radio' || item.cellType == 'checkbox'){
									text = item.cellValueText;
								}else{
									text = item.cellValue;
								}
							}
							if(item.cellType == 'link' && "无" != text){
								html += "<div class='col-md-6' style='text-align:left;padding: 0;padding-right: 5px;'><span style='float:left' >" + item.cellName + ":</span><a class='text-success' style='float:left' target='_blank' href='"+text+"' title='"+text+"'>进入链接</a></div>";
							}else{
								html += "<div class='col-md-6' style='text-align:left;padding: 0;padding-right: 5px;'><span style='float:left' >" + item.cellName + ":</span><span class='text-danger' style='float:left'  >" + text + "</span></div>";
							}
							if (j++ % 2 == 0) {
								html += "<br/>";
							}
						}
					});
					return html;
				}
			},
			{
				name: 'enabled',
				label: '是否启用',
				width: 20,
				align: "center",
				formatter: function (v, options, row) {
					if(row.enabled == 0){
						return "<span style='color: green;'>启用</span>";
					}else{
						return "<span style='color: red;'>停用</span>";
					}
				}
			},
			{
				name: 'supplierRelateState',
				label: '审核状态',
				width: 20,
				align: "center",
				formatter: function (v, options, row) {
					switch (v) {
						case 0:
							return '<span class="text-muted">未审核</span>';
						case 1:
							return '<span class="text-success">已通过</span>';
						case -1:
							return '<span class="text-danger">已驳回</span>';
						case -9:
							return '<span class="text-justify">已删除</span>';
					}
				}
			},
			{
				name: 'isCopy',
				label: '是否拷贝',
				width: 20,
				align: "center",
				hidden: true,//user.dept.companyCode == 'XH' ? false : true,
				formatter: function (v, options, row) {
					if(row.isCopy == 1){
						return "拷贝";
					}else{
						return "自建";
					}
				}
			},
			{
				name: 'copyRemarks',
				label: '拷贝备注',
				editable: true,
				width: 30,
				align: "center",
				hidden: true,//user.dept.companyCode == 'XH' ? false : true
			},
			{
				label: "操作",
				width: 20,
				hidden: false,
				formatter: function (v, options, row) {
					//只有祥和的部长或组长有拷贝分公司媒体权限，拷贝的媒体必须审核通过
					// var currentDeptQx = user.currentDeptQx;//当前用户是否有部门权限，含组长
					// var companyCode = user.dept.companyCode; //获取公司代码
					var html = "";
					//媒体和媒体关系都审核通过，祥和部长和组长可以进行拷贝
					/*if(currentDeptQx && companyCode == 'XH' && row.companyCode != 'XH' && row.state == 1 && row.supplierRelateState == 1){
						html += "&nbsp;<a class='text-success' onclick='copyMedia(\"copyMediaRelate\"," + row.relateId + ")'>拷贝</a>";
					}*/
					html += "<a class='text-success' onclick='mediaSupplierChangeObj.mediaSupplierChangeShow("+row.relateId+","+row.userId+");'>异动详情&nbsp;</a>";
					return html;
				}
			},
		],
		// pager: "#mediaSupplierPricePager",
		viewrecords: true,
		// caption: "媒体供应商价格",
		add: true,
		edit: true,
		addtext: 'Add',
		edittext: 'Edit',
		hidegrid: false,
		gridComplete: function () {
			var primaryKey = "id";
			supplierPriceTableObj.grid.mergerCell('mediaName', primaryKey);
		/*	supplierPriceTableObj.grid.mergerCell('supplierName', primaryKey);
			supplierPriceTableObj.grid.mergerCell('supplierCompanyName', primaryKey);*/
		}
	}
};

//是否媒介总监
function isMJZJ() {
	var roles = user.roles;//获取用户角色
	var isMJZJ = false;//是否媒介政务
	if(roles){
		for(var i=0; i < roles.length; i++){
			if(roles[i].code == 'ZJ' && roles[i].type == 'MJ'){
				isMJZJ = true;
				break;
			}
		}
	}
	return isMJZJ;
}

//媒体责任人指派
function meidaAssign(mediaId, userId) {
	layer.open({
		type: 1,
		content: $("#mediaAssign"),
		btn: ['确定','取消'],
		area: ['700px', '200px'],
		title: "媒体责任人指派",
		success: function(layero, index){
			$(layero[0]).find("input[name='id']").val(mediaId || "");//媒体ID
			var html = "<option value=\"\">请选择责任人</option>";
			if(mediaUserMap && Object.getOwnPropertyNames(mediaUserMap).length > 0){
				for(var key in mediaUserMap){
					var selected = mediaUserMap[key].id == userId ? "selected" : "";
					html += "<option "+selected+" value=\""+mediaUserMap[key].id+"\">"+mediaUserMap[key].name+"</option>";
				}
			}
			$(layero[0]).find("select").html(html);//责任人
			//使用layui表单
			layui.use('form', function(){
				var form = layui.form;
				form.render();
			});
		},
		yes: function (index, layero) {
			var jsonData = $(layero[0]).find("form").serializeForm();
			if(!jsonData.userId){
				layer.msg("请选择指派媒介责任人！", {time: 2000, icon: 5});
				return;
			}
			requestData(jsonData, "/mediaAudit/updateMediaUserId", "post", "json", true, function (data) {
				if(data.code == 200){
					layer.msg("媒体责任人指派成功！", {time: 2000, icon: 6});
					layer.close(index);
					reflushTable();
				}else {
					layer.msg(data.msg, {time: 3000, icon: 5});
				}
			});
		}
	});
}

//批量操作
function batchDelete(obj) {
	var url = "/mediaAudit/deleteBatch";
	var tips = "确认批量删除?";
	var info = new Array();  //缓存当前不能删除的
	var ids = $("#table_medias").jqGrid("getGridParam", "selarrrow");
	if (url.length > 0 && ids.length > 0) {
		// 获取选中列的数据；
		var mediaNames = new Array();
		var userIds = new Array();
		$(ids).each(function (index, id) {
			//由id获得对应数据行
			var row = $("#table_medias").jqGrid('getRowData', id);
			if(row.userId != user.id){
				info.push("<span style=\"color: brown;font-size: 12px;\">很抱歉，存在[" + (row.name || row.mediaContentId) + "]不能删除，只有该媒体的责任人才能删除！</span>")
				ids.splice(0,1); //从数据中删除当前ID
			}else{
				mediaNames.push(row.name);
				userIds.push(row.userId);
			}
		});
		layer.confirm(tips, {
			btn: ["确认", "取消"],
			shade: false
		}, function (index) {
			layer.close(index);
			// layer.msg("系统处理中，请稍候。");
			startModal("#" + $(obj).attr("id"));
			if(ids.length > 0){
				var param = { ids: ids, mediaNames: mediaNames, userIds: userIds, standarPlatformFlag: $("#standarPlatformFlag").val() || 0};
				requestData(param, baseUrl + url, "get","json",true,function (data) {
					Ladda.stopAll();
					if (data.code == 200) {
						var text = "";
						if(info.length > 0){
							text += info.join("<br/>");
						}
						swal({
							title: "媒体删除成功!",
							text: text,
							type: "success",
							html: true
						});
						triggerPageBtnClick("/media1/audits","auditSearchBtn"); //触发媒体审核Tab刷新
						triggerPageBtnClick("/media1/mediaSupplierList","mediaListSearchBtn"); //触发媒体关系管理Tab刷新
						triggerPageBtnClick("/media1/mediaSupplierAuditList","mediaListSearchBtn"); //触发媒体关系审核Tab刷新
					} else {
						if (getResCode(data)) {
							return;
						}else{
							swal({
								title: "很抱歉，媒体操作失败！",
								text: data.msg,
								type: "error",
								html: true
							});
						}
					}
					// 刷新数据；
					reflushTable();
				});
			}else{
				var text = "";
				if(info.length > 0){
					text +=  info.join("<br/>");
				}
				swal({
					title: "请选择您可操作的数据",
					text: text,
					type: "warning",
					html: true
				});
				Ladda.stopAll();
			}
		}, function () {
			return;
		})
	} else {
		layer.msg("请选择要操作的数据。");
	}
}

//批量导出选择的
function batchChooseExport() {
	var ids = $("#table_medias").jqGrid("getGridParam", "selarrrow");
	if(ids && ids.length > 0){
		var info = new Array();  //缓存当前不能删除的
		var companyCode = user.dept.companyCode; //当前用户公司代码
		$(ids).each(function (index, id) {
			//由id获得对应数据行
			var row = $("#table_medias").jqGrid('getRowData', id);
			//不能导出分公司的媒体
			if(row.companyCode != companyCode){
				info.push("<span style=\"color: brown;font-size: 12px;\">很抱歉，存在["+row.name+"]不能导出，只有该媒体所在公司媒介才能导出！</span>")
				ids.splice(0,1); //从数据中删除当前ID
			}
		});
		if(info.length > 0){
			var text = info.join("<br/>");
			swal({
				title: "媒体选择导出失败!",
				text: text,
				type: "error",
				html: true
			});
			return;
		}
		var json = {};
		json.mediaIds = ids;
		json.plateId = $("#plateId").val();
		json.standarPlatformFlag = $("#standarPlatformFlag").val();
		json.plateName = $("#mediaTypeText").attr("title");
		location.href = baseUrl+"/mediaAudit/batchChooseMediaExport?"+$.param(json);
	}else{
		swal("请选择要操作的数据!");
	}

}

//批量导出全部
function batchExport() {
	//标准平台获取唯一标识值
	if($("#standarPlatformFlag").val() && $("#standarPlatformFlag").val() == 1){
		$("#mediaContentId").val($("#standarPlatformId").val());
	}else {
		$("#mediaContentId").val("");
	}
	$("#mediaName").val($("#mName").val()); //媒体名称
	$("#supplierName").val($("#contactName").val()); //供应商名称
	var json = $("#termForm").serializeJson();
	var condition = {};
	for (var k in json) {
		if (k.indexOf("cell:") > -1) {
			var value = json[k];
			var kk = k.substring(5, k.length);
			var arr = kk.split(":");
			var length = arr.length; //2-代表不是区间的数，3-自定义栏的区间数值（最后一个是min: 开始值，max：结束值）
			var cell = arr[0];
			var type = arr[1];
			var k3 = null;
			if(length == 3){
				k3 = arr[2];//该值为min 或 max
			}
			if(type == "checkbox" && Array.isArray(value)){ //如果是数组，则转成字符串
				value = value.join(",");
			}
			//由于区间的cell相同，所以cellValueStart 和 cellValueEnd需要放在同一个对象中
			if(!condition[cell]){
				if(k3 && k3 == 'min'){
					condition[cell] = {cell:cell, type:type, cellValueStart:value};
				}else if(k3 && k3 == 'max'){
					condition[cell] = {cell:cell, type:type, cellValueEnd:value};
				}else{
					condition[cell] = {cell:cell, type:type, cellValue:value};
				}
			}else{
				if(k3 && k3 == 'min'){
					condition[cell].cellValueStart = value;
				}else if(k3 && k3 == 'max'){
					condition[cell].cellValueEnd = value;
				}else{
					condition[cell].cellValue = value;
				}
			}
			delete json[k];
		}/*else if(k == 'wechatId'){
			condition[cell] = {cell:'wechatId', type:'text', cellValue:json[k], wechatIdFlag: 1};
			delete json[k];
		}else if(k == 'ksId'){
			condition[cell] = {cell:'ksId', type:'text', cellValue:json[k], ksIdFlag: 1};
			delete json[k];
		}
*/
	}
	var extendArr = new Array();
	if(condition && Object.getOwnPropertyNames(condition).length > 0){
		for(var key in condition){
			extendArr.push(condition[key]);
		}
	}
	json.extendParams = JSON.stringify(extendArr);
	json.plateId = $("#plateId").val();
	json.standarPlatformFlag = $("#standarPlatformFlag").val();
	json.plateName = $("#mediaTypeText").attr("title");
	location.href = baseUrl+"/mediaAudit/batchExport?"+$.param(json);
}

//批量导出选择的异动
function batchChangeChooseExport() {
    var ids = $("#table_medias").jqGrid("getGridParam", "selarrrow");
    if(ids && ids.length > 0){
        var info = new Array();  //缓存当前不能删除的
        var companyCode = user.dept.companyCode; //当前用户公司代码
        $(ids).each(function (index, id) {
            //由id获得对应数据行
            var row = $("#table_medias").jqGrid('getRowData', id);
            //不能导出分公司的媒体
            if(row.companyCode != companyCode){
                info.push("<span style=\"color: brown;font-size: 12px;\">很抱歉，存在["+row.name+"]不能导出，只有该媒体所在公司媒介才能导出！</span>")
                ids.splice(0,1); //从数据中删除当前ID
            }
        });
        if(info.length > 0){
            var text = info.join("<br/>");
            swal({
                title: "媒体异动选择导出失败!",
                text: text,
                type: "error",
                html: true
            });
            return;
        }
        var json = {};
        json.mediaIds = ids;
        json.plateName = $("#mediaTypeText").attr("title");
        location.href = baseUrl+"/mediaAudit/batchChangeChooseExport?"+$.param(json);
    }else{
        swal("请选择要操作的数据!");
    }

}

//批量导出异动全部
function batchChangeExport() {
	//标准平台获取唯一标识值
	if($("#standarPlatformFlag").val() && $("#standarPlatformFlag").val() == 1){
		$("#mediaContentId").val($("#standarPlatformId").val());
	}else {
		$("#mediaContentId").val("");
	}
	$("#mediaName").val($("#mName").val()); //媒体名称
	$("#supplierName").val($("#contactName").val()); //供应商名称
	var json = $("#termForm").serializeJson();
	var condition = {};
	for (var k in json) {
		if (k.indexOf("cell:") > -1) {
			var value = json[k];
			var kk = k.substring(5, k.length);
			var arr = kk.split(":");
			var length = arr.length; //2-代表不是区间的数，3-自定义栏的区间数值（最后一个是min: 开始值，max：结束值）
			var cell = arr[0];
			var type = arr[1];
			var k3 = null;
			if(length == 3){
				k3 = arr[2];//该值为min 或 max
			}
			if(type == "checkbox" && Array.isArray(value)){ //如果是数组，则转成字符串
				value = value.join(",");
			}
			//由于区间的cell相同，所以cellValueStart 和 cellValueEnd需要放在同一个对象中
			if(!condition[cell]){
				if(k3 && k3 == 'min'){
					condition[cell] = {cell:cell, type:type, cellValueStart:value};
				}else if(k3 && k3 == 'max'){
					condition[cell] = {cell:cell, type:type, cellValueEnd:value};
				}else{
					condition[cell] = {cell:cell, type:type, cellValue:value};
				}
			}else{
				if(k3 && k3 == 'min'){
					condition[cell].cellValueStart = value;
				}else if(k3 && k3 == 'max'){
					condition[cell].cellValueEnd = value;
				}else{
					condition[cell].cellValue = value;
				}
			}
			delete json[k];
		}/*else if(k == 'wechatId'){
			condition[cell] = {cell:'wechatId', type:'text', cellValue:json[k], wechatIdFlag: 1};
			delete json[k];
		}else if(k == 'ksId'){
			condition[cell] = {cell:'ksId', type:'text', cellValue:json[k], ksIdFlag: 1};
			delete json[k];
		}
*/
	}
	var extendArr = new Array();
	if(condition && Object.getOwnPropertyNames(condition).length > 0){
		for(var key in condition){
			extendArr.push(condition[key]);
		}
	}
	json.extendParams = JSON.stringify(extendArr);
	json.plateName = $("#mediaTypeText").attr("title");
	location.href = baseUrl+"/mediaAudit/mediaChangeBatchExport?"+$.param(json);
}

//批量替换
function batchImport() {
	var index = layer.open({
		type: 1,
		zIndex: 9999,
		content: $("#fileTypeDiv"),
		btn: ['确认上传'],
		area: ['400px', '160px'],
		title: "选择导入文件类型",
		success: function (layero, index) {
			$(layero[0]).find(".layui-layer-btn").css("margin-top", "-20px");
		},
		yes: function (t) {
			var fileType = $('#fileTypeDiv').find("input[type='radio']:checked").val();
			if(!fileType){
				swal("请选择文件类型");
			}else{
				$("#fileType").val(fileType);
				fileUpload.upload();
				layer.close(index);
			}
		}
	});
}

//复投统计
var mediaArtStatisticsObj = {
	lookStatisticsInfo: function (mediaId, mediaName) {
		statisticsFTModal.loadConfig({mediaId:mediaId, mediaName: mediaName}); //加载用户配置
		$("#statisticsFTModal").modal("toggle");
	}
}

//媒体异动详情
var mediaChangeObj = {
	mediaCommonFieldList:[
		{cellCode:"name",cellName:"媒体名称"},
		{cellCode:"link",cellName:"媒体链接"},
		{cellCode:"mediaContentId",cellName:"唯一标识"},
		{cellCode:"userId",cellName:"责任人"},
		{cellCode:"discount",cellName:"折扣率"},
		{cellCode:"remarks",cellName:"备注"},
		{cellCode:"enabled",cellName:"是否可用"},
		{cellCode:"isDelete",cellName:"是否删除"},
	],//媒体公共字段
	mediaExtendMap:{},//媒体扩展字段缓存
	mediaChangeList:[],//缓存媒体异动列表
	chooseMediaChangeId: null,//选中的异动ID
	chooseMediaChangeOp: null,//选中的异动操作，对于新增的媒体，不让恢复，让他自己去删除
	userFlag: 0,//选中的异动操作，是否有责任人异动:0-无、1-有
	renderMediaField:function (layero, plateId, mediaExtendList) {
		var html = "";
		var priceHtml = "";
		$.each(mediaChangeObj.mediaCommonFieldList, function (i, extend) {
			html += "<div class=\"fieldItem\" title=\""+extend.cellName+"\" onclick='mediaChangeObj.renderMediaChange(this, \""+extend.cellCode+"\");'>\n" +
				"        <div class=\"ellipsisContent\">"+extend.cellName+"</div>\n" +
				"    </div>";
		});
		if(mediaExtendList && mediaExtendList.length > 0){
			$.each(mediaExtendList, function (i, extend) {
				if(extend.extendFlag != 1){
					if(extend.type != 'price'){
						html += "<div class=\"fieldItem\" title=\""+extend.cellName+"\" onclick='mediaChangeObj.renderMediaChange(this, \""+extend.cellCode+"\");'>\n" +
							"        <div class=\"ellipsisContent\">"+extend.cellName+"</div>\n" +
							"    </div>";
					}else {
						priceHtml += "<div class=\"fieldItem\" title=\""+extend.cellName+"\" onclick='mediaChangeObj.renderMediaChange(this, \""+extend.cellCode+"\");'>\n" +
							"        <div class=\"ellipsisContent\">"+extend.cellName+"</div>\n" +
							"    </div>";
					}
				}
			});
		}
		html += priceHtml;
		$(layero[0]).find(".fieldItemWrap").html(html);
	},
	renderMediaChangeLi:function (cellCode, mediaChange) {
		var moreBtnHtml = "";
		var firstRowHtml = "";
		var otherRowHtml = "";
		var returnFlag = false;//返回标识，判断是否返回数据，默认返回空，如果cellCode有值，需要判断变更字段中是否包含该字段，没包含则返回空
		var changeContent = JSON.parse(mediaChange["changeContent"])["change"];
		var userFlag = 0;//是否有责任人异动:0-无、1-有
		//多个更改字段才有更多按钮
		if(changeContent["fieldList"] &&　changeContent["fieldList"].length > 0){
			moreBtnHtml = "<div class=\"moreChangeBtn\" onclick=\"mediaChangeObj.moreBtnClick(this);\">\n" +
				"                  <i class=\"text-danger\">更多&nbsp;</i>\n" +
				"                  <i class=\"fa fa-chevron-up\"></i>\n" +
				"              </div>";
			$.each(changeContent["fieldList"], function (i, fieldChange) {
				//如果cellCode有值，需要判断变更字段中是否包含该字段，没包含则返回空，cellCode为空时返回
				if(!cellCode || (cellCode && fieldChange.cell == cellCode)){
					returnFlag = true;
				}
				//责任人
				if("userId" == fieldChange.cell){
					userFlag = 1;
					fieldChange.oldCellValue = fieldChange.oldCellValue ? (mediaUserMap[fieldChange.oldCellValue] ? mediaUserMap[fieldChange.oldCellValue].name : fieldChange.oldCellValue): "空";
					fieldChange.newCellValue = fieldChange.newCellValue ? (mediaUserMap[fieldChange.newCellValue] ? mediaUserMap[fieldChange.newCellValue].name : fieldChange.newCellValue): "空";
				}
				//没有选择字段 或者  选择字段等于该字段，第一行显示字段
				if((!cellCode && i == 0) || (cellCode && fieldChange.cell == cellCode)){
					firstRowHtml = "<div class=\"firstRowItemWrap\" title='"+((fieldChange.oldCellText || fieldChange.oldCellValue) || '空')+"->"+((fieldChange.newCellText || fieldChange.newCellValue) || '空')+"'>\n" +
						"               <div class=\"fieldName\" title='"+fieldChange.cellName+"'>"+fieldChange.cellName+"</div>\n" +
						"               <div class=\"fieldChangeVal\">\n" +
						"                   <span class=\"oldValue\">："+((fieldChange.oldCellText || fieldChange.oldCellValue) || '空')+"</span>\n" +
						"                   <span>-></span>\n" +
						"                   <span class=\"newValue\">"+((fieldChange.newCellText || fieldChange.newCellValue) || '空')+"</span>\n" +
						"               </div>\n" +
						"           </div>";
				}else {
					otherRowHtml += "<div class=\"otherRowItem\" title='"+((fieldChange.oldCellText || fieldChange.oldCellValue) || '空')+"->"+((fieldChange.newCellText || fieldChange.newCellValue) || '空')+"'>\n" +
						"               <div class=\"fieldName\" title='"+fieldChange.cellName+"'>"+fieldChange.cellName+"</div>\n" +
						"               <div class=\"fieldChangeVal\">\n" +
						"                   <span class=\"oldValue\">："+((fieldChange.oldCellText || fieldChange.oldCellValue) || '空')+"</span>\n" +
						"                   <span>-></span>\n" +
						"                   <span class=\"newValue\">"+((fieldChange.newCellText || fieldChange.newCellValue) || '空')+"</span>\n" +
						"               </div>\n" +
						"           </div>";
				}
			});
		}
		var titleTmp = mediaChange.mediaName + "-"+changeContent.opDesc+"（"+mediaChange.createDate+"）";
		var html = "<li class=\"layui-timeline-item timeLineCss\">\n" +
			"           <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
			"           <div class=\"layui-timeline-content layui-text timeCss\">\n" +
			"               <div class=\"layui-timeline-title\" style=\"display: flex;justify-content: space-between;padding-right: 20px;\">\n" +
			"                   <div title='"+titleTmp+"' style='width: 70%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>"+titleTmp+"</div>\n" +
			"                   <div>异动人："+mediaUserMap[mediaChange.userId].name+"&nbsp;&nbsp;&nbsp;&nbsp;审核人："+mediaChange.auditUserName+"</div>\n" +
			"               </div>" +
			"               <div class=\"timeContent\" onclick='mediaChangeObj.recoverClick(this,"+userFlag+", "+mediaChange.id+", \""+changeContent.op+"\");'>\n" +
			"                   <div class=\"firstRowContentWrap\">\n" +
			"                       "+firstRowHtml+"\n" +
			"                       "+moreBtnHtml+"\n" +
			"                   </div>\n" +
			"                   <div class=\"otherRowContentWrap\">\n" +
			"                       "+otherRowHtml+"\n" +
			"                   </div>\n" +
			"               </div>\n" +
			"           </div>\n" +
			"       </li>";

		if(returnFlag){
			return html;
		}else {
			return "";
		}
	},
	renderMediaChange:function (t, cellCode) {
		//如果有值，则说明是点击某个字段则改变字体颜色
		if(cellCode){
			$(t).closest(".fieldItemWrap").find(".fieldItem").removeClass("fieldItemActive");
			$(t).addClass("fieldItemActive");
		}
		var html = "";
		if(mediaChangeObj.mediaChangeList && mediaChangeObj.mediaChangeList.length > 0){
			$.each(mediaChangeObj.mediaChangeList, function (i, mediaChange) {
				html += mediaChangeObj.renderMediaChangeLi(cellCode, mediaChange);
			});
		}
		$(t).closest(".modalContentWrap").find("#announcements").html(html);
	},
	mediaChangeShow: function (plateId, mediaIds, userId) {
		layer.open({
			type: 1,
			title: '媒体异动详情',
			content: $("#mediaChange").html(),
			btn: ['恢复','取消'],
			area: ["55%", "80%"],
			shade: 0,
			shadeClose: true,
			resize: false,
			move: '.layui-layer-title',
			moveOut: true,
			success: function (layero, index) {
				//绑定事件
				$(layero[0]).find(".fieldLabel").click(function () {
					mediaChangeObj.renderMediaChange(this);
				});
				//获取媒体扩展字段
				if(!mediaChangeObj.mediaExtendMap[plateId] || mediaChangeObj.mediaExtendMap[plateId].length < 1){
					requestData(null, "/mediaAudit/listMediaField/"+plateId, "post", "json", false, function (data) {
						mediaChangeObj.mediaExtendMap[plateId] = data;
					});
				}
				mediaChangeObj.renderMediaField(layero, plateId, mediaChangeObj.mediaExtendMap[plateId]);//渲染字段
				requestData({mediaIds:mediaIds}, "/mediaAudit/listMediaChange", "post", "json", true, function (data) {
					mediaChangeObj.mediaChangeList = data;
					mediaChangeObj.renderMediaChange($(layero[0]).find("#announcements"));
				});
			},
			yes:function (index, layero) {
				//判断是否有选中要恢复的数据
				if(!mediaChangeObj.chooseMediaChangeId){
					layer.msg("请选择要恢复的异动记录！", {time: 2000, icon: 5});
					return;
				}
				//如果不是责任人则不能恢复
				if(user.id != userId){
					layer.msg("仅有媒体责任人自己能进行恢复！", {time: 2000, icon: 5});
					return;
				}else {
					//如果是恢复，则判断是否有责任人改动，如果有的话则不能恢复
					if(mediaChangeObj.userFlag == 1){
						layer.msg("异动数据中存在责任人变动，不能进行恢复！", {time: 2000, icon: 5});
						return;
					}
				}
				//判断是否是新增的媒体产生的异动，是的话不提供恢复，让他自己去删除
				if(mediaChangeObj.chooseMediaChangeOp == 'add'){
					layer.msg("新增媒体产生的异动记录不允许恢复，请直接去删除媒体！", {time: 2000, icon: 5});
					return;
				}
				requestData({id:mediaChangeObj.chooseMediaChangeId, standarPlatformFlag: $("#standarPlatformFlag").val() || 0}, "/mediaAudit/mediaChangeRecover", "post", "json", true, function (data) {
					if(data.code == 200){
						layer.msg("恢复成功！", {time: 2000, icon: 6});
						layer.close(index);
						reflushTable();
					}else {
						layer.msg(data.msg, {time: 2000, icon: 5});
					}
				});
			}
		});
	},
	moreBtnClick: function (t) {
		var iNode = $(t).find("i:eq(1)");
		var classStr = iNode.attr('class');
		if(classStr.indexOf("fa-chevron-up") != -1){ //当前其他条件为隐藏
			iNode.removeClass("fa-chevron-up");
			iNode.addClass("fa-chevron-down");
			$(t).closest(".timeContent").find(".otherRowContentWrap").fadeIn("slow");
		}else{
			iNode.removeClass("fa-chevron-down");
			iNode.addClass("fa-chevron-up");
			$(t).closest(".timeContent").find(".otherRowContentWrap").fadeOut("slow");
		}
	},
	recoverClick:function (t,userFlag, id, op) {
		var $announcements = $(t).closest("#announcements");
		if($(t).hasClass("timeContentActive")){
			$(t).removeClass("timeContentActive");
			mediaChangeObj.chooseMediaChangeId = null;//再次点击移除选中
			mediaChangeObj.chooseMediaChangeOp = null;
			mediaChangeObj.userFlag = null;
		}else {
			//移除其他选中的
			$announcements.find(".timeContent").removeClass("timeContentActive");
			$(t).addClass("timeContentActive");
			mediaChangeObj.chooseMediaChangeId = id;//点击选中
			mediaChangeObj.chooseMediaChangeOp = op;
			mediaChangeObj.userFlag = userFlag;
		}
	}

}

//媒体供应商异动详情
var mediaSupplierChangeObj = {
	mediaSupplierCommonFieldList:[
		{cellCode:"enabled",cellName:"是否可用"},
		{cellCode:"isDelete",cellName:"是否删除"},
	],//媒体公共字段
	mediaSupplierExtendMap:{},//媒体扩展字段缓存
	mediaSupplierChangeList:[],//缓存媒体异动列表
	chooseMediaSupplierChangeId: null,//选中的异动ID
	chooseMediaSupplierChangeOp: null,//选中的异动操作，对于新增的媒体，不让恢复，让他自己去删除
	renderMediaSupplierField:function (layero, plateId, mediaSupplierExtendList) {
		var html = "";
		$.each(mediaSupplierChangeObj.mediaSupplierCommonFieldList, function (i, extend) {
			html += "<div class=\"fieldItem\" title=\""+extend.cellName+"\" onclick='mediaSupplierChangeObj.renderMediaSupplierChange(this, \""+extend.cellCode+"\");'>\n" +
				"        <div class=\"ellipsisContent\">"+extend.cellName+"</div>\n" +
				"    </div>";
		});
		if(mediaSupplierExtendList && mediaSupplierExtendList.length > 0){
			$.each(mediaSupplierExtendList, function (i, extend) {
				//展示仅供应商使用 和 仅媒体的价格
				if(extend.extendFlag == 1 || extend.type == 'price'){
					html += "<div class=\"fieldItem\" title=\""+extend.cellName+"\" onclick='mediaSupplierChangeObj.renderMediaSupplierChange(this, \""+extend.cellCode+"\");'>\n" +
						"        <div class=\"ellipsisContent\">"+extend.cellName+"</div>\n" +
						"    </div>";
				}
			});
		}
		$(layero[0]).find(".fieldItemWrap").html(html);
	},
	renderMediaSupplierChangeLi:function (cellCode, mediaSupplierChange) {
		var moreBtnHtml = "";
		var firstRowHtml = "";
		var otherRowHtml = "";
		var returnFlag = false;//返回标识，判断是否返回数据，默认返回空，如果cellCode有值，需要判断变更字段中是否包含该字段，没包含则返回空
		var changeContent = JSON.parse(mediaSupplierChange["changeContent"])["change"];
		//多个更改字段才有更多按钮
		if(changeContent["fieldList"] &&　changeContent["fieldList"].length > 0){
			moreBtnHtml = "<div class=\"moreChangeBtn\" onclick=\"mediaSupplierChangeObj.moreBtnClick(this);\">\n" +
				"                  <i class=\"text-danger\">更多&nbsp;</i>\n" +
				"                  <i class=\"fa fa-chevron-up\"></i>\n" +
				"              </div>";
			$.each(changeContent["fieldList"], function (i, fieldChange) {
				//如果cellCode有值，需要判断变更字段中是否包含该字段，没包含则返回空，cellCode为空时返回
				if(!cellCode || (cellCode && fieldChange.cell == cellCode)){
					returnFlag = true;
				}
				//责任人
				if("userId" == fieldChange.cell){
					fieldChange.oldCellValue = fieldChange.oldCellValue ? (mediaUserMap[fieldChange.oldCellValue] ? mediaUserMap[fieldChange.oldCellValue].name : fieldChange.oldCellValue): "空";
					fieldChange.newCellValue = fieldChange.newCellValue ? (mediaUserMap[fieldChange.newCellValue] ? mediaUserMap[fieldChange.newCellValue].name : fieldChange.newCellValue): "空";
				}
				//没有选择字段 或者  选择字段等于该字段，第一行显示字段
				if((!cellCode && i == 0) || (cellCode && fieldChange.cell == cellCode)){
					firstRowHtml = "<div class=\"firstRowItemWrap\" title='"+((fieldChange.oldCellText || fieldChange.oldCellValue) || '空')+"->"+((fieldChange.newCellText || fieldChange.newCellValue) || '空')+"'>\n" +
						"               <div class=\"fieldName\" title='"+fieldChange.cellName+"'>"+fieldChange.cellName+"</div>\n" +
						"               <div class=\"fieldChangeVal\">\n" +
						"                   <span class=\"oldValue\">："+((fieldChange.oldCellText || fieldChange.oldCellValue) || '空')+"</span>\n" +
						"                   <span>-></span>\n" +
						"                   <span class=\"newValue\">"+((fieldChange.newCellText || fieldChange.newCellValue) || '空')+"</span>\n" +
						"               </div>\n" +
						"           </div>";
				}else {
					otherRowHtml += "<div class=\"otherRowItem\" title='"+((fieldChange.oldCellText || fieldChange.oldCellValue) || '空')+"->"+((fieldChange.newCellText || fieldChange.newCellValue) || '空')+"'>\n" +
						"               <div class=\"fieldName\" title='"+fieldChange.cellName+"'>"+fieldChange.cellName+"</div>\n" +
						"               <div class=\"fieldChangeVal\">\n" +
						"                   <span class=\"oldValue\">："+((fieldChange.oldCellText || fieldChange.oldCellValue) || '空')+"</span>\n" +
						"                   <span>-></span>\n" +
						"                   <span class=\"newValue\">"+((fieldChange.newCellText || fieldChange.newCellValue) || '空')+"</span>\n" +
						"               </div>\n" +
						"           </div>";
				}
			});
		}
		var titleTmp = mediaSupplierChange.mediaName + "-"+changeContent.opDesc+"（"+mediaSupplierChange.createDate+"）";
		var html = "<li class=\"layui-timeline-item timeLineCss\">\n" +
			"           <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
			"           <div class=\"layui-timeline-content layui-text timeCss\">\n" +
			"               <div class=\"layui-timeline-title\" style=\"display: flex;justify-content: space-between;padding-right: 20px;\">\n" +
			"                   <div title='"+titleTmp+"' style='width: 70%;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>"+titleTmp+"</div>\n" +
			"                   <div>异动人："+mediaUserMap[mediaSupplierChange.userId].name+"&nbsp;&nbsp;&nbsp;&nbsp;审核人："+mediaSupplierChange.auditUserName+"</div>\n" +
			"               </div>" +
			"               <div class=\"timeContent\" onclick='mediaSupplierChangeObj.recoverClick(this, "+mediaSupplierChange.id+", \""+changeContent.op+"\");'>\n" +
			"                   <div class=\"firstRowContentWrap\">\n" +
			"                       "+firstRowHtml+"\n" +
			"                       "+moreBtnHtml+"\n" +
			"                   </div>\n" +
			"                   <div class=\"otherRowContentWrap\">\n" +
			"                       "+otherRowHtml+"\n" +
			"                   </div>\n" +
			"               </div>\n" +
			"           </div>\n" +
			"       </li>";

		if(returnFlag){
			return html;
		}else {
			return "";
		}
	},
	renderMediaSupplierChange:function (t, cellCode) {
		//如果有值，则说明是点击某个字段则改变字体颜色
		if(cellCode){
			$(t).closest(".fieldItemWrap").find(".fieldItem").removeClass("fieldItemActive");
			$(t).addClass("fieldItemActive");
		}
		var html = "";
		if(mediaSupplierChangeObj.mediaSupplierChangeList && mediaSupplierChangeObj.mediaSupplierChangeList.length > 0){
			$.each(mediaSupplierChangeObj.mediaSupplierChangeList, function (i, mediaSupplierChange) {
				html += mediaSupplierChangeObj.renderMediaSupplierChangeLi(cellCode, mediaSupplierChange);
			});
		}
		$(t).closest(".modalContentWrap").find("#announcements").html(html);
	},
	mediaSupplierChangeShow: function (relateIds, userId) {
		layer.open({
			type: 1,
			title: '媒体供应商异动详情',
			content: $("#mediaChange").html(),
			btn: ['恢复','取消'],
			area: ["55%", "80%"],
			shade: 0,
			shadeClose: true,
			resize: false,
			move: '.layui-layer-title',
			moveOut: true,
			success: function (layero, index) {
				var plateId = $("#plateId").val();

				//修改字段名称
				$(layero[0]).find(".fieldLabel").text("供应商全部字段");
				//绑定事件
				$(layero[0]).find(".fieldLabel").click(function () {
					mediaSupplierChangeObj.renderMediaSupplierChange(this);
				});


				//获取媒体扩展字段
				if(!mediaSupplierChangeObj.mediaSupplierExtendMap[plateId] || mediaSupplierChangeObj.mediaSupplierExtendMap[plateId].length < 1){
					requestData(null, "/mediaAudit/listMediaField/"+plateId, "post", "json", false, function (data) {
						mediaSupplierChangeObj.mediaSupplierExtendMap[plateId] = data;
					});
				}
				mediaSupplierChangeObj.renderMediaSupplierField(layero, plateId, mediaSupplierChangeObj.mediaSupplierExtendMap[plateId]);//渲染字段
				requestData({relateIds:relateIds}, "/mediaAudit/listMediaSupplierChange", "post", "json", true, function (data) {
					mediaSupplierChangeObj.mediaSupplierChangeList = data;
					mediaSupplierChangeObj.renderMediaSupplierChange($(layero[0]).find("#announcements"));
				});
			},
			yes:function (index, layero) {
				//判断是否有选中要恢复的数据
				if(!mediaSupplierChangeObj.chooseMediaSupplierChangeId){
					layer.msg("请选择要恢复的异动记录！", {time: 2000, icon: 5});
					return;
				}
				//如果不是责任人则不能恢复
				if(user.id != userId){
					layer.msg("仅有媒体责任人自己能进行恢复！", {time: 2000, icon: 5});
					return;
				}
				//判断是否是新增的媒体产生的异动，是的话不提供恢复，让他自己去删除
				if(mediaSupplierChangeObj.chooseMediaSupplierChangeOp == 'add'){
					layer.msg("新增媒体供应商产生的异动记录不允许恢复，请直接去删除媒体！", {time: 2000, icon: 5});
					return;
				}
				requestData({id:mediaSupplierChangeObj.chooseMediaSupplierChangeId, standarPlatformFlag: $("#standarPlatformFlag").val() || 0}, "/mediaAudit/mediaSupplierChangeRecover", "post", "json", true, function (data) {
					if(data.code == 200){
						layer.msg("恢复成功！", {time: 2000, icon: 6});
						layer.close(index);
						reflushTable();
					}else {
						layer.msg(data.msg, {time: 2000, icon: 5});
					}
				});
			}
		});
	},
	moreBtnClick: function (t) {
		var iNode = $(t).find("i:eq(1)");
		var classStr = iNode.attr('class');
		if(classStr.indexOf("fa-chevron-up") != -1){ //当前其他条件为隐藏
			iNode.removeClass("fa-chevron-up");
			iNode.addClass("fa-chevron-down");
			$(t).closest(".timeContent").find(".otherRowContentWrap").fadeIn("slow");
		}else{
			iNode.removeClass("fa-chevron-down");
			iNode.addClass("fa-chevron-up");
			$(t).closest(".timeContent").find(".otherRowContentWrap").fadeOut("slow");
		}
	},
	recoverClick:function (t, id, op) {
		var $announcements = $(t).closest("#announcements");
		if($(t).hasClass("timeContentActive")){
			$(t).removeClass("timeContentActive");
			mediaSupplierChangeObj.chooseMediaSupplierChangeId = null;//再次点击移除选中
			mediaSupplierChangeObj.chooseMediaSupplierChangeOp = null;
		}else {
			//移除其他选中的
			$announcements.find(".timeContent").removeClass("timeContentActive");
			$(t).addClass("timeContentActive");
			mediaSupplierChangeObj.chooseMediaSupplierChangeId = id;//点击选中
			mediaSupplierChangeObj.chooseMediaSupplierChangeOp = op;
		}
	}
}

//稿件媒体供应商替换
var artMediaSupplierReplaceObj = {
	modalIndex: 0,
	artListUrl: "/mediaAudit/listHistoryArtByParam",
	mediaListUrl: "/media1/listMediaByParam",
	supplierListUrl: "/media1/listMediaSupplierByParam",
	replaceModalShow: function () {
		artMediaSupplierReplaceObj.modalIndex = layer.open({
			type: 1,
			title: '稿件媒体供应商替换',
			content: $("#artMediaSupplierReplace").html(),
			btn: ['替换', '取消'],
			area: ["65%", "55%"],
			shade: 0,
			shadeClose: true,
			resize: false,
			move: '.layui-layer-title',
			moveOut: true,
			success: function (layero, index) {
				$(layero[0]).find("#mediaTypeId").val($("#plateId").val());
				$(layero[0]).find("#mediaPlateName").val($("#mediaTypeText").attr("title") || 0);
				//如果是标准媒体，则展示唯一标识，否则隐藏
				if ($("#standarPlatformFlag").val() == 1) {
					$(layero[0]).find(".mediaContentIdWrap").css("display", "block");
				} else {
					$(layero[0]).find(".mediaContentIdWrap").css("display", "none");
				}
				//使用layui表单
				layui.use('form', function () {
					var form = layui.form;
					form.render();
				});
				//每次重新定义表格对象
				var $table = $("<table id=\"artMediaSupplierTable\"></table>");
				var $paper = $("<div id=\"artMediaSupplierTablePaper\"></div>");
				$(layero[0]).find(".jqGrid_wrapper").append($table);
				$(layero[0]).find(".jqGrid_wrapper").append($paper);
				artMediaSupplierReplaceObj.createArtTable(layero[0]);
			},
			yes: function (index, layero) {
				var mediaPlateId = $(layero[0]).find("#mediaTypeId").val();
				if (!mediaPlateId) {
					layer.msg("媒体板块不存在！", {time: 2000, icon: 5});
					return;
				}
				var rowId = $(layero[0]).find("#artMediaSupplierTable").jqGrid("getGridParam", "selrow");
				if (!rowId) {
					layer.msg("请选择稿件记录！", {time: 2000, icon: 5});
					return;
				}
				var row = $(layero[0]).find("#artMediaSupplierTable").jqGrid('getRowData', rowId);
				if (row && !row.artIds) {
					layer.msg("历史稿件ID不存在！", {time: 2000, icon: 5});
					return;
				}
				if (row && !row.mediaId) {
					layer.msg("稿件没有关联媒体不允许替换！", {time: 2000, icon: 5});
					return;
				}
				if (row && !row.supplierId) {
					layer.msg("稿件没有关联供应商不允许替换！", {time: 2000, icon: 5});
					return;
				}
				var mediaId = $(layero[0]).find("input[name='mediaId']").val();
				if (!mediaId) {
					layer.msg("请选择替换媒体！", {time: 2000, icon: 5});
					return;
				}
				var supplierId = $(layero[0]).find("select[name='supplierId']").val();
				if (!supplierId) {
					layer.msg("请选择替换供应商！", {time: 2000, icon: 5});
					return;
				}
				var param = {
					articleIdList: row.artIds,
					oldMediaId: row.mediaId,
					oldMediaName: row.mediaName,
					oldSupplierId: row.supplierId,
					oldSupplierName: row.supplierName,
					oldSupplierContactor: row.supplierContactor,
					plateId: mediaPlateId,
					newMediaId: mediaId,
					newSupplierId: supplierId
				}
				requestData(JSON.stringify(param), "/mediaAudit/artMediaSupplierReplace", "post", "json", true, function (data) {
					if (data.code == 200) {
						layer.msg("历史稿件媒体供应商替换成功！", {time: 2000, icon: 6});
						artMediaSupplierReplaceObj.reflushArtTable($(layero[0]).find("table"));
					} else {
						layer.msg(data.msg, {time: 2000, icon: 5});
					}
				}, true);
			}
		});
	},
	createArtTable: function (t) {
		var artReplaceWrapEle = $(t).find(".artReplaceWrap");
		var param = {
			mediaTypeId: ($(artReplaceWrapEle).find("#mediaTypeId").val() || ""),
			keyword: ($(artReplaceWrapEle).find("#keyword").val() || "")
		};
		//供应商公司编辑账户列表
		$(artReplaceWrapEle).find("#artMediaSupplierTable").jqGrid({
			url: baseUrl + artMediaSupplierReplaceObj.artListUrl,
			datatype: "json",
			mtype: 'POST',
			postData: param,
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
			shrinkToFit: true,
			prmNames: {rows: "size"},
			rowNum: 10,//每页显示记录数
			rowList: [10, 20, 25, 50],//分页选项，可以下拉选择每页显示记录数
			jsonReader: {//server返回Json解析设定
				root: "list", page: "pageNum", total: "pages",
				records: "total", repeatitems: false, id: "artIds"
			},
			colModel: [
				{
					name: 'mediaTypeId',
					index: 'mediaTypeId',
					label: 'mediaTypeId',
					editable: false,
					align: "center",
					sortable: false,
					hidden: true
				},
				{
					name: 'mediaId',
					index: 'mediaId',
					label: 'mediaId',
					editable: false,
					align: "center",
					sortable: false,
					hidden: true
				},
				{
					name: 'supplierId',
					index: 'supplierId',
					label: 'supplierId',
					editable: false,
					align: "center",
					sortable: false,
					hidden: true
				},
				{
					name: 'artIds',
					index: 'artIds',
					label: 'artIds',
					editable: false,
					align: "center",
					sortable: false,
					hidden: true
				},
				{
					name: 'mediaTypeName',
					index: 'mediaTypeName',
					label: '媒体板块',
					editable: false,
					width: 60,
					align: "center",
					sortable: false
				},
				{
					name: 'mediaName',
					index: 'mediaName',
					label: '媒体名称',
					editable: false,
					width: 130,
					align: "center",
					sortable: false
				},
				{
					name: 'supplierName',
					index: 'supplierName',
					label: '供应商公司名称',
					editable: false,
					width: 130,
					align: "center",
					sortable: false
				},
				{
					name: 'supplierContactor',
					index: 'supplierContactor',
					label: '供应商联系人',
					editable: false,
					width: 80,
					align: "center",
					sortable: false
				},
				{
					name: 'artNum',
					index: 'artNum',
					label: '稿件数量',
					editable: false,
					width: 60,
					align: "center",
					sortable: false
				},
			],
			pager: $(artReplaceWrapEle).find("#artMediaSupplierTablePaper"),
			viewrecords: true,
			add: false,
			edit: true,
			addtext: 'Add',
			edittext: 'Edit',
			hidegrid: false,
			gridComplete: function () {
				$(artReplaceWrapEle).find("#artMediaSupplierTable").setGridWidth($(artReplaceWrapEle).find(".jqGrid_wrapper").width());
			}
		});
		$(artReplaceWrapEle).find("#artMediaSupplierTable").jqGrid('setLabel', 'rn', '序号', {
			'text-align': 'center',
			'vertical-align': 'middle',
		});
		$(artReplaceWrapEle).find("#artMediaSupplierTable").setGridHeight(229);
	},
	reflushArtTable: function (t) {
		var artReplaceWrapEle = $(t).closest(".artReplaceWrap");
		var param = {
			mediaTypeId: $(artReplaceWrapEle).find("#mediaTypeId").val() || "",
			keyword: $(artReplaceWrapEle).find("#keyword").val() || ""
		};
		//刷新表格
		$(artReplaceWrapEle).find("#artMediaSupplierTable").emptyGridParam(); //清空历史查询数据
		$(artReplaceWrapEle).find("#artMediaSupplierTable").jqGrid('setGridParam', {
			postData: param, //发送数据
		}).trigger("reloadGrid"); //重新载入
	},
	artEnterEvent: function (t, event) {
		if ((event.keyCode == '13' || event.keyCode == 13)) {
			artMediaSupplierReplaceObj.reflushArtTable(t);
		}
	},
	mediaInputClick: function (t) {
		var searchEle = $(t).closest(".mediaFormWrap").find(".mediaSearchWrap");
		//如果隐藏则显示，反之
		if ($(searchEle).hasClass("mediaSearchWrapCancel")) {
			searchEle.removeClass("mediaSearchWrapCancel");
			searchEle.find("input").focus();
			//判断是否有数据，没数据则查询
			if ($(t).closest(".mediaFormWrap").find(".mediaSearchWrap").find("li").length < 1) {
				artMediaSupplierReplaceObj.mediaSearch(t);
			}
		} else {
			searchEle.addClass("mediaSearchWrapCancel");
		}
	},
	mourseOut: function (t) {
		if (!$(t).closest(".artReplaceWrap").find(".mediaSearchWrap").hasClass("mediaSearchWrapCancel")) {
			$(t).closest(".artReplaceWrap").find(".mediaSearchWrap").addClass("mediaSearchWrapCancel");
		}
	},
	mourseOver: function (t) {
		//如果有内容则展示
		if ($(t).closest(".artReplaceWrap").find(".mediaSearchWrap").hasClass("mediaSearchWrapCancel")) {
			$(t).closest(".artReplaceWrap").find(".mediaSearchWrap").removeClass("mediaSearchWrapCancel");
		}
		$(t).closest(".artReplaceWrap").find(".mediaSearchWrap").find("input").focus();
		//判断是否有数据，没数据则查询
		if ($(t).closest(".artReplaceWrap").find(".mediaSearchWrap").find("li").length < 1) {
			artMediaSupplierReplaceObj.mediaSearch(t);
		}
	},
	renderMedia: function (mediaList) {
		var html = "";
		if (mediaList && mediaList.length > 0) {
			$.each(mediaList, function (j, media) {
				html += "<li data-mediaId=\"" + media.mediaId + "\" data-mediaContentId=\"" + media.mediaContentId + "\" title=\"" + (media.mediaName || media.mediaContentId) + "\" class=\"mediaItem\" onclick=\"artMediaSupplierReplaceObj.mediaItemClick(this);\">\n" +
					"    	" + (media.mediaName || media.mediaContentId) + "\n" +
					"    </li>";
			});
		}
		return html;
	},
	mediaSearch: function (t) {
		var $artReplaceWrap = $(t).closest(".artReplaceWrap");
		$artReplaceWrap.find("#mediaFlow").html("");//清空历史数据
		layui.use('flow', function () {
			var flow = layui.flow;
			flow.load({
				elem: $artReplaceWrap.find("#mediaFlow")[0],
				isAuto: true,
				done: function (page, next) {
					//从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
					//请求数据，判断当前题目类型进行加载
					var param = {
						plateId: ($($artReplaceWrap).find("#mediaTypeId").val() || ""),
						mediaName: ($($artReplaceWrap).find("#searchMediaName").val() || "")
					};
					param.page = page; //页码
					param.size = 3;
					requestData(param, artMediaSupplierReplaceObj.mediaListUrl, "post", "json", false, function (data) {
						next(artMediaSupplierReplaceObj.renderMedia(data.list), page < data.pages); //如果小于总页数，则继续
					});
				}
			});
		});
	},
	mediaEnterEvent: function (t, event) {
		if ((event.keyCode == '13' || event.keyCode == 13)) {
			artMediaSupplierReplaceObj.mediaSearch(t);
		}
	},
	mediaItemClick: function (t) {
		var artReplaceWrapEle = $(t).closest(".artReplaceWrap");
		var mediaId = $(t).attr("data-mediaId") || "";
		var mediaName = $(t).attr("title") || "";
		var mediaContentId = $(t).attr("data-mediaContentId") || "";
		//关闭筛选框
		if (!$(artReplaceWrapEle).find(".mediaSearchWrap").hasClass("mediaSearchWrapCancel")) {
			$(artReplaceWrapEle).find(".mediaSearchWrap").addClass("mediaSearchWrapCancel");
		}

		//判断是否选择相同媒体，如果是，则不改变供应商
		if (mediaId != $(artReplaceWrapEle).find("input[name='mediaId']").val()) {
			//媒体赋值
			$(artReplaceWrapEle).find("input[name='mediaId']").val(mediaId);
			$(artReplaceWrapEle).find("input[name='mediaName']").val(mediaName);
			$(artReplaceWrapEle).find("input[name='mediaContentId']").val(mediaContentId);
			//清除供应商
			$(artReplaceWrapEle).find("input[name='supplierContactor']").val("");
			$(artReplaceWrapEle).find("input[name='phone']").val("");
			//渲染供应商下拉列表
			artMediaSupplierReplaceObj.renderSupplier(t, mediaId);
		}
	},
	renderSupplier: function (t, mediaId) {
		var artReplaceWrapEle = $(t).closest(".artReplaceWrap");
		requestData({
			mediaId: mediaId,
			page: 1,
			size: 1000
		}, artMediaSupplierReplaceObj.supplierListUrl, "post", "json", false, function (data) {
			var html = "<option value=\"\">请选择供应商</option>";
			if (data && data.list && data.list.length > 0) {
				$.each(data.list, function (i, supplier) {
					//责任人自己可以看到手机号
					var phone = supplier.supplierUserId == user.id ? (supplier.phone || "") : artMediaSupplierReplaceObj.buildPhone(supplier.phone);
					var supplierCompanyName = supplier.supplierCompanyName || "";
					if (phone) {
						supplierCompanyName += "(" + phone + ")";
					}
					html += "<option data-supplierContactor=\"" + supplier.supplierName + "\" data-phone=\"" + phone + "\" value=\"" + supplier.supplierId + "\">" + supplierCompanyName + "</option>"
				});
			}
			$(artReplaceWrapEle).find("select[name='supplierId']").html(html);
		});
		//课程范围改变事件
		layui.use('form', function () {
			var form = layui.form;
			//供应商改变事件
			form.on('select(supplierId)', function (data) {
				if (data.value) {
					var supplierContactor = $(data.elem).find("option:selected").attr("data-supplierContactor") || "";
					var phone = $(data.elem).find("option:selected").attr("data-phone") || "";
					$(data.elem).closest(".artReplaceWrap").find("input[name='supplierContactor']").val(supplierContactor);
					$(data.elem).closest(".artReplaceWrap").find("input[name='phone']").val(phone);
				} else {
					$(data.elem).closest(".artReplaceWrap").find("input[name='supplierContactor']").val("");
					$(data.elem).closest(".artReplaceWrap").find("input[name='phone']").val("");
				}
			});
			layui.form.render();//layui重新渲染下拉列表
		});
	},
	buildPhone: function (value) {
		value = value || "";
		if (value) {
			if (value.length >= 11) {
				var start = value.length > 11 ? "*****" : "****";
				return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
			} else if (value.length >= 3) {
				return value[0] + "***" + value[value.length - 1];
			} else {
				return "**";
			}
		} else {
			return "";
		}
	}
}