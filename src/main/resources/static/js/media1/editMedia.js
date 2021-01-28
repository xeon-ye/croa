var fh = '<div class="form-group col-md-3"><label class="col-sm-4 control-label">${labelName}:</label><div class="col-sm-8"><input type="${type}" name="${name}" placeholder="${labelName}" class="form-control"/></div></div>';
var url = "/mediaAudit";
var imageUpload;
var mediaExtendMap = {}; //缓存查询到的扩展字段值，格式：{cell:cellValue}， 用于判断编辑媒体后是否有新增的字段
//媒体供应商对象，格式：{supplierId:{id:supplierId, name:supplierName,mediaSupplierRelateId：mediaSupplierRelateId，cell:cellValue,priceArr:[{cell:cell, cellName:cellName,cellValue:cellValue}]}}， 用于判断编辑媒体后是否有新增的字段
//    说明：mediaSupplierRelateId  -> 媒体供应商的关系主键ID
//          priceArr               -> 用于渲染初始化价格分组值
//          cell:cellValue         -> 用于保存时，判断当前供应商当前价格类型是否是新增的还是修改的
var mjUserMap = {}; //板块媒介对象，结构：{plateId: {userId: userName}}
var mediaSupplierMap = {};
var oldMediaMap = {}; //缓存媒体初始值，用于判断此次更新是否需要重新审核媒体，仅改了（图标、备注、供应商、责任人 ）字段不需要审核
var supplierHtml = ""; //缓存当前页面供应商HTML
var currentRequiredSelectList = ["userId"]; //缓存当前表单需要必输校验的扩展下拉列表
$(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    supplierHtml = $("#supplierDiv").children("div:last-child").html(); //缓存当前页面供应商HTML，需要和扩展框最后一个div对调位置

    //获取请求参数
    var mediaId = getQueryString("id");
    var mediaPlateName = getQueryString("mediaPlateName");
    var mediaName = getQueryString("mediaName");
    var standarPlatformFlag = getQueryString("standarPlatformFlag");
    requestData({id:mediaId},"/mediaAudit/getEditMediaById","post","json",false,function (data) {
        if(data.code != 200){
            swal({title: "媒体信息错误", text: data.msg, type: "warning"}, function (isConfirm) {
                if (isConfirm) {
                    closeCurrentTab();
                }
            });
            return;
        }
        var media = data.data.media;
        if (media == null || media == undefined) {
            swal({title: "媒体信息不存在", text: data.msg, type: "warning"}, function (isConfirm) {
                if (isConfirm) {
                    closeCurrentTab();
                }
            });
            return;
        }
        $("#id").val(media.id);  //保存当前媒体主键ID
        $("#state").val(media.state);  //保存当前媒体状态
        $("#companyCode").val(media.companyCode);  //保存当前媒体主键ID
        $("#mediaPlateName").text(mediaPlateName);//设置标题
        $("#mediaName").text(mediaName); //设置标题
        $("#standarPlatformFlag").val(standarPlatformFlag);

        //缓存原值
        for(var key in media){
            if(key === "mediaExtends"){    //媒体信息
                if(media.mediaExtends && media.mediaExtends.length > 0){
                    $.each(media.mediaExtends,function (i,mediaExtend) {
                        mediaExtendMap[mediaExtend.cell] = mediaExtend.cellValue;
                        oldMediaMap[mediaExtend.cell] = mediaExtend.cellValue;
                    })
                }
            }else if(key === "supplierList"){  //媒体供应商信息
                if(media.supplierList && media.supplierList.length > 0){
                    $.each(media.supplierList,function (i,supplier) {
                        var currentSupplier = {
                            id: supplier.supplierId,
                            name: supplier.supplierName,
                            relateUserId: supplier.relateUserId,
                            mediaSupplierRelateId: supplier.id,
                            enabled: supplier.enabled
                        };
                        var priceArr = new Array();
                        if(supplier.mediaPriceExtends && supplier.mediaPriceExtends.length > 0){
                            $.each(supplier.mediaPriceExtends,function (i,priceExtend) {
                                currentSupplier[priceExtend.cell] = priceExtend.cellValue || 0;
                                var price = {cell:priceExtend.cell, cellName:priceExtend.cellName, cellValue: priceExtend.cellValue || 0, cellType: priceExtend.cellType};
                                priceArr.push(price);
                            });
                        }
                        currentSupplier["priceArr"] = priceArr;
                        mediaSupplierMap[supplier.supplierId] = currentSupplier;
                        oldMediaMap[supplier.supplierId] = currentSupplier;
                    })
                }
            }else{
                oldMediaMap[key] = media[key];
            }
        }
        setType(media);
    })

    //新增媒体图片上传
    imageUpload = new FileUpload({
        targetEl: '#imageUploadForm',
        multi: false,
        filePart: "media",
        completeCallback: function (data) {
            if (data.length > 0) {
                var filePath = data[0].file;
                $("#picPath").val(filePath);
                var $picPathPreview = $("#picPathPreview");
                $picPathPreview.attr('src',  filePath);
                $picPathPreview.show();
            }
        },
        acceptSuffix: ['jpg', 'png']
    });

    //媒体标准平台根据唯一标识判重、否则根据媒体名称判重
    if(standarPlatformFlag == 1){
        addMediaContentIdRule();
    }else {
        addMediaNameRule();
    }
    mediaSupplierManageObj.addSupplierCheckRule("mediaSupplierAddModal");//供应商登记框添加校验规则
});

//媒体名称自定义规则
function addMediaNameRule() {
    var icon = "<i class='fa fa-times-circle'></i> ";
    $("#mf").validate({
        rules: {
            name: {
                required: true,
                remote: {
                    url: baseUrl + "/mediaAudit/validationMediaName", // 后台处理程序
                    type: "post", // 数据发送方式
                    dataType: "json", // 接受数据格式
                    data: { // 要传递的数据
                        "plateId": function () {
                            return $("#plateId").val();
                        },
                        "name": function () {
                            return $("#name").val();
                        },
                        "id": function () {
                            return $("#id").val() || "";
                        }
                    },
                    dataFilter: function (data) {
                        data = JSON.parse(data);
                        if (data.code == 200) {
                            return true;
                        } else {
                            $("#name").focus();
                            return false;
                        }
                    }
                }
            },
        },
        messages: {
            name: {remote: icon + "该媒体名称已存在，请更换！"},
        }
    });
}

//媒体唯一标识自定义规则
function addMediaContentIdRule() {
    var icon = "<i class='fa fa-times-circle'></i> ";
    $("#mf").validate({
        rules: {
            name: {
                required: true,
                remote: {
                    url: baseUrl + "/mediaAudit/validationMediaContentId", // 后台处理程序
                    type: "post", // 数据发送方式
                    dataType: "json", // 接受数据格式
                    data: { // 要传递的数据
                        "plateId": function () {
                            return $("#plateId").val();
                        },
                        "mediaContentId": function () {
                            return $("#mediaContentId").val();
                        },
                        "id": function () {
                            return $("#id").val() || "";
                        }
                    },
                    dataFilter: function (data) {
                        data = JSON.parse(data);
                        if (data.code == 200) {
                            return true;
                        } else {
                            $("#name").focus();
                            return false;
                        }
                    }
                }
            },
        },
        messages: {
            mediaContentId: {remote: icon + "唯一标识已存在，请更换！"},
        }
    });
}

//得到查询参数
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]);
    return null;
}

/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,dataType,async,callBackFun) {
    $.ajax({
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun
    });
}

//设置媒体表单扩展项
var mediaType = {}; //缓存板块选择对应的媒体类型
function setType(media) {
    //显示案例链接 还是媒体ID
    if($("#standarPlatformFlag").val() && $("#standarPlatformFlag").val() == 1){
        $("#name").attr("disabled", false);//标准平台，媒体名称爬取
        $("#mediaContentIdDiv").css("display", "block");
        $("#linkDiv").find("input").removeAttr("required"); //案例链接不必输
        $("#linkStar").css("display", "none");//隐藏*号
        //隐藏显示折扣率
        $("#discountDiv1").find("input").attr("disabled", true);
        $("#discountDiv1").css("display", "none");
        $("#discountDiv2").find("input").removeAttr("disabled");
        $("#discountDiv2").css("display", "block");
    }else{
        $("#name").attr("disabled", false);//非标准平台，媒体名称手填
        $("#mediaContentIdDiv").css("display", "none");
        $("#linkDiv").find("input").attr("required",true); //案例链接必输
        $("#linkStar").css("display", "inline-block");//显示*号
        //隐藏显示折扣率
        $("#discountDiv2").find("input").attr("disabled", true);
        $("#discountDiv2").css("display", "none");
        $("#discountDiv1").find("input").removeAttr("disabled");
        $("#discountDiv1").css("display", "block");
    }

    $("#plateId").val(media.plateId);
    $("#mType").val(media.plateId);
    $("#plateTypeId").val(media.plateId);
    // $("#mediaPlateId").val(media.plateId);

    //初始化页面值-公共字段
    $("#mediaForm input[name='name']").val(media.name); // 媒体名称
    $("#mediaForm input[name='no']").val(media.no); // 媒体账号ID
    var $picPathPreview = $("#picPathPreview"); //媒体图标
    var imgPath = media.picPath || "/img/mrt.png";
    $("#picPath").val(imgPath);
    $picPathPreview.attr('src', imgPath);
    $picPathPreview.show();
    $("#mediaForm input[name='platform']").val(media.platform); // 平台
    $("#mediaForm input[name='link']").val(media.link); // 案例链接
    $("#mediaForm input[name='mediaContentId']").val(media.mediaContentId); // 媒体ID
    $("#mediaForm input[name='price']").val(media.price); // 底价
    $("#mediaForm input[name='discount']").val(media.discount); // 底价
    $("#mediaForm textarea[name='remarks']").val(media.remarks); // 备注supplierId
    // $("#mediaForm select[name='supplierId']").attr("data-value",media.supplierId);
    // $("#mediaForm select[name='supplierId']").attr("data-text",media.supplierName);
    // $("#mediaForm select[name='supplierId']").load();//加载供应商
    $("#mediaForm select[name='userId']").attr("data-value",media.userId);
    $("#mediaForm select[name='userId']").load();//加载责任人

    //责任人
    var html = "<option value=\""+user.id+"\" selected>"+user.name+"</option>";
    $("#userId").html(html);
    /*if(!mjUserMap[media.plateId]){
        requestData(null,"/user/listMJByPlateId2/"+media.plateId,"get","json",false,function (data) {
            var userList = data.data.userList;
            mjUserMap[media.plateId] = userList;
            renderUser(userList);
        });
    }else{
        renderUser(mjUserMap[media.plateId]);
    }*/

    //扩展表单
    requestData(null,"/mediaForm1/" + media.plateId,"get","json",false,function (datas) {
        renderPage(datas);
    });

    //字段编辑权限渲染
    renderMediaEdit(media);

    layui.use(["form", 'element'], function () {
        layui.form.render();//layui-form
    });
}

//渲染责任人
function renderUser(data){
    var value = $("#userId").attr("data-value");
    if (!value || value == "${data-value}") {
        value = user.id;
    }
    var html = "";
    if(data && data.length > 0){
        $.each(data, function (i, item) {
            if(item.id == value){
                html += "<option value=\""+item.id+"\" selected>"+item.name+"</option>";
            }else{
                html += "<option value=\""+item.id+"\">"+item.name+"</option>";
            }
        })
    }
    $("#userId").html(html);
    layui.use(["form"], function () {
        layui.form.render();//layui-form
    });
}

//渲染表单页面
function renderPage(datas) {
    var currentPriceFromList = new Array();
    var currentPriceHTMLList = new Array();  //缓存价格HTML
    var currentNotPriceHTMLList = new Array();  //缓存非价格HTML  到时将价格放在最后显示
    var html = '';
    $(datas).each(function (i, data) {
        var data = data;
        var required = data.required ? "required" : "";
        var maxLength = data.maxlength ? "maxlength=" + data.maxlength : "";
        var minLength = data.minlength ? "minlength=" + data.minlength : "";
        var size = data.size ? "size=" + data.size : "";
        var min = data.min ? "min=" + data.min : "";
        var max = data.max ? "max=" + data.max : "";
        var disabled = data.disabled ? "disabled=" + data.disabled : "";
        disabled = (!disabled && data.climbFlag == 1) ? "disabled" : "";//是否爬取标识：0-手工填写、1-仅脚本爬取、2-手工+爬取，仅爬取的字段不可输入
        required = disabled ? "" : required;//如果不可输入，则取消必填

        var cellName = data.cellName;
        var extendFlag = data.extendFlag; //扩展字段标识：0-仅媒体用、1-仅供应商用，对于媒体价格字段，默认供应商也可使用
        var cell = "cell:" + data.cellCode + ":" + cellName + ":" + data.type;
        var cellValue = mediaExtendMap[data.cellCode]; //获取扩展值
        cellValue = cellValue ? cellValue : "";
        html = "";//每次进来初始化
        // if (i % 4 == 0)
        //     html += '<div class="col-md-12">';
        html += '<div class="form-group col-sm-3"><label class="col-sm-4 control-label">';
        if (required) {
            html += '<span class="text-red"> * </span>';
        }
        html += cellName + ':</label><div class="col-sm-8">';
        if(extendFlag == 1){
            currentPriceFromList.push(data);
        }else {
            switch (data.type) {
                case 'radio':
                case 'checkbox':
                    var dbHtml = "";
                    var dataClass = data.type == "radio" ? "radio-inline" : "checkbox-inline";
                    if(data.dataType && data.dataType == 'html' && data.dbHtml){
                        dbHtml = data.dbHtml;
                    }else if(data.dataType && data.dataType == 'json' && data.dbJson){
                        var json = eval(data.dbJson);
                        if (!Array.isArray(json)){
                            json = [json];
                        }
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += "<label class=\""+dataClass+" i-checks\"><input "+disabled+" cell-name=\"${cell-name}\" title='"+text+"'  type=\""+data.type+"\" value=\""+item.value+"\" name=\"${name}\" data-value=\"${data-value}\">"+text+"</label>";
                        });
                    }else {
                        var sqlDatas = data.datas;
                        if (sqlDatas && sqlDatas.length > 0) {  //如果对象存在，并且个数大于0
                            $.each(sqlDatas,function (i,item) {
                                dbHtml += "<label class=\""+dataClass+" i-checks\"><input "+disabled+" cell-name=\"${cell-name}\" title='"+item.name+"'  type=\""+data.type+"\" value=\""+item.id+"\" name=\"${name}\" data-value=\"${data-value}\">"+item.name+"</label>";
                            });
                        }
                    }
                    html += dbHtml;
                    break;
                case 'select':
                    var dbHtml = "";
                    if(data.dataType && data.dataType == 'html' && data.dbHtml){
                        dbHtml = data.dbHtml;
                    }else if(data.dataType && data.dataType == 'json' && data.dbJson){
                        var json = eval(data.dbJson);
                        if (!Array.isArray(json)){
                            json = [json];
                        }
                        dbHtml += "<select lay-search cell-name=\"${cell-name}\" name=\"${name}\" placeholder=\"${labelName}\" data-value=\"${data-value}\" ${required}  "+disabled+" class=\"form-control\">";
                        dbHtml += "<option value=\"\">请选择"+cellName+"</option>";
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += '<option value="' + item.value + '" " ' + (item.isDefault == "true" || item.isDefault == true ? "selected" : "") + '>' + text + '</option>';
                        });
                        dbHtml += "</select>";
                    }else {
                        var sqlDatas = data.datas;
                        dbHtml += "<select lay-search cell-name=\"${cell-name}\" name=\"${name}\" placeholder=\"${labelName}\" data-value=\"${data-value}\" ${required} "+disabled+" class=\"form-control\">";
                        dbHtml += "<option value=\"\">请选择"+cellName+"</option>";
                        if (sqlDatas && sqlDatas.length > 0) {  //如果对象存在，并且个数大于0
                            $.each(sqlDatas,function (i,item) {
                                dbHtml += "<option value=\""+item.id+"\">"+item.name+"</option>";
                            });
                        }
                        dbHtml += "</select>";
                    }
                    html += dbHtml;
                    break;
                case 'textarea':
                    html += '<textarea name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " "
                        + maxLength + " " + minLength + " " + size + " " + min + " " + max + " " + disabled + ' class="form-control" style="resize:none;">' + cellValue + '</textarea>';
                    break;
                case 'date':
                    html += '<input cellType="date" name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " "
                        + maxLength + " " + minLength + " " + size + " " + min + " " + max + " " + disabled + " "
                        ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD\'})" value="'+cellValue+'"/>';
                    break;
                case 'datetime':
                    html += '<input cellType="date" name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " + disabled + " " +
                        size + " " + min + " " + max + ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD hh:mm:ss\'})" value="'+cellValue+'"/>';
                    break;
                case 'time':
                    html += '<input cellType="date" name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " + disabled + " " +
                        size + " " + min + " " + max + ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'hh:mm:ss\'})" value="'+cellValue+'"/>';
                    break;
                case 'price':
                    html += '<div class="input-group m-b"><span class="input-group-addon">¥</span><input onkeypress="return inPrice(event)" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max + " " + disabled + " " +
                        '  type="number" class="form-control" name="${name}" placeholder="${labelName}" cell-name="${cell-name}" value="'+cellValue+'"> <!--<span class="input-group-addon">.00</span>--></div>';
                    currentPriceFromList.push(data); //缓存价格初始控件
                    break;
                case 'file':
                    html += '<input type="file" cell-name="${cell-name}" name="file" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max + " " + disabled + ' class="form-control"/>';
                    break;
                case 'number':
                    html += '<input onkeypress="return inNum(event)" cell-name="${cell-name}" type="number" name="${name}" id="${name}" placeholder="${labelName}" ' + required + " "
                        + " " + disabled + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max + ' class="form-control" value="'+cellValue+'"/>';
                    break;
                default:
                    html += '<input type="${type}" name="${name}" cell-name="${cell-name}" id="${name}" placeholder="${labelName}" ' + required + " " + maxLength + " "
                        + " " + disabled + " " + minLength + " " + size + " " + min + " " + max + ' class="form-control" value="'+cellValue+'"/>';
                    break;
            }
            html = html.th('id', cell).th('name', cell).th('labelName', cellName).th('type', data.type).th("cell-name", cellName).th("data-value",cellValue).th("required", required);;
            html += '</div></div>';
            if(data.type != 'price'){
                currentNotPriceHTMLList.push(html);
            }else{
                currentPriceHTMLList.push(html);
            }
        }

        // if ((i + 1) % 4 == 0) html += '</div></div></div>';
        // else html += '</div></div>';
    });
    //统一排序处理，将价格HTML添加到非价格后面
    currentNotPriceHTMLList = currentNotPriceHTMLList.concat(currentPriceHTMLList);
    var html1 = "";
    $.each(currentNotPriceHTMLList,function (i,v) {
        if (i % 4 == 0){
            html1 += '<div class="col-md-12">';
        }
        html1 += v;
        if ((i + 1) % 4 == 0){
            html1 += '</div>';
        }
    });
    $("#mediaForm > #extend").html(html1);

    //下拉列表加载事件
    $("#mediaForm select").each(function () {
        if ($(this).attr("onload")){  //设置下拉列表值（数据库数据）
            this.onload();
        }else{
            if($(this).attr("data-value")){  //设置下拉列表值（非数据库数据）
                var value = $(this).attr("data-value");
                $(this).find("option").each(function () {
                    if ($(this).val() == value) {
                        $(this).attr("selected",true);
                    }
                });
            }
        }
    });

    // radio默认选择否；
    var dataValue;
    $("#mediaForm input[type='radio']").each(function () {
        dataValue = $(this).attr("data-value");
        $(this).iCheck(dataValue == $(this).val() ? "check" : 'uncheck');
    });

    //checkbox选择默认值
    var dataValue;
    $("#mediaForm input[type='checkbox']").each(function (index,d) {
        var dataValue = $(d).attr("data-value");
        if(dataValue.indexOf(",") != -1){
            var arr = dataValue.split(",");
            $(arr).each(function (index,item) {
                if(item == $(d).val()){
                    $(d).attr("checked",true);
                }
            })
        }else{
            if(dataValue == $(d).val()){
                $(d).attr("checked",true);
            }
        }
    });

    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    createPriceGroup(currentPriceFromList); //生成默认价格组HTML

    if(currentPriceGroupHTML){ //如果有值，则说明有价格分组，否则没有，目前一定有
        $("#extendPriceDiv").css("display","block");
        //判断是否有供应商
        if(mediaSupplierMap && Object.getOwnPropertyNames(mediaSupplierMap).length > 0){
            for(var supplierId in mediaSupplierMap){
                //1、填充默认价格组
                $("#extendPriceDiv").append(currentPriceGroupHTML.th("divId","supplierPrice"+supplierId));
                //2、赋值
                $("#supplierPrice"+supplierId).find("input[name='supplierName']").val(mediaSupplierMap[supplierId].name);// 给当前分组供应商赋值
                $("#supplierPrice"+supplierId).attr("supplier-id",supplierId); // 给当前分组供应商赋值
                //3、是否启用
                $("#supplierPrice"+supplierId).find("input[lay-skin='switch']").val(mediaSupplierMap[supplierId].enabled);
                if(mediaSupplierMap[supplierId].enabled == 0){
                    $("#supplierPrice"+supplierId).find("input[lay-skin='switch']").attr("checked",true);
                }else {
                    $("#supplierPrice"+supplierId).find("input[lay-skin='switch']").attr("checked",false);
                }

                var supplierPriceArr = mediaSupplierMap[supplierId].priceArr;
                $.each(supplierPriceArr,function (i, supplierField) {
                    var name = "cell:" + supplierField.cell + ":" + supplierField.cellName + ":" +supplierField.cellType ;
                    if(supplierField.cellType == "select"){
                        var $select = $("#extendPriceDiv").children("div:last-child").find("select");
                        if($select.attr("onload")){
                            $select.onload();
                        }else {
                            $select.find("option").each(function () {
                                if ($(this).val() == supplierField.cellValue) {
                                    $(this).attr("selected",true);
                                }
                            });
                        }
                    } else if(supplierField.cellType == "radio"){
                        if(supplierField.cellValue){
                            $("#extendPriceDiv").children("div:last-child").find("input[type='radio']").each(function () {
                                $(this).iCheck(supplierField.cellValue == $(this).val() ? "check" : 'uncheck');
                            });
                        }
                    } else if(supplierField.cellType == "checkbox"){
                        if(supplierField.cellValue){
                            var arr = [];
                            if(supplierField.cellValue.indexOf(",") != -1){
                                arr = supplierField.cellValue.split(",");
                            } else {
                                arr.push(supplierField.cellValue);
                            }
                            $("#extendPriceDiv").children("div:last-child").find("input[type='checkbox']").each(function (index,d) {
                                if(arr.contains($(d).val())){
                                    $(d).attr("checked",true);
                                }
                            });
                        }else {
                            $("#extendPriceDiv").children("div:last-child").find("input[type='checkbox']").each(function (index,d) {
                                $(d).attr("checked",false);
                            });
                        }
                    } else {
                        $("#extendPriceDiv").children("div:last-child").find("input[name='"+name+"']").val(supplierField.cellValue);
                    }
                });
            }
            $('.i-checks').iCheck({
                checkboxClass: 'icheckbox_square-green',
                radioClass: 'iradio_square-green',
            });
            //删除分组按钮控制
            if($("#extendPriceDiv").children("div").size() > 1){
                $("#extendPriceDiv").find("div[name='close']").css("display","block"); //所有关闭按钮显示
            }else {
                $("#extendPriceDiv").children("div:first-child").find("div[name='close']").css("display","none");//当仅有一个分组时，分组右上角的删除按钮隐藏
            }
            reflushGroupBackColor(); //刷新分组背景颜色
        }else{
           /* $("#extendPriceDiv").html(currentPriceGroupHTML); //填充默认价格组
            $("#extendPriceDiv").find("div[name='close']").css("display","none"); //所有关闭按钮显示*/
        }
    }else {
        $("#extendPriceDiv").css("display","none");
    }

    renderSelect(); //重新渲染扩展下拉列表
}

//可选下拉列表实现
function renderSelect() {
    currentRequiredSelectList = ["userId"]; //每次渲染重新初始化
    layui.use(["form", 'element'], function () {
        $("#extend").find("select").each(function (i,t) {
            $(t).parent().addClass("layui-form");
            if(!$(t).attr("lay-search")){
                $(t).attr("lay-search", "");
            }
            if($(t).attr("required")){
                currentRequiredSelectList.push($(t).attr("name"));
            }
        });
        layui.form.render('select');//layui-form

        //给渲染后必输的select添加必输校验
        if(currentRequiredSelectList && currentRequiredSelectList.length > 0){
            $.each(currentRequiredSelectList, function (i,selectName) {
                $("select[name='"+selectName+"']").parent().find("input").attr("required",true);
                $("select[name='"+selectName+"']").parent().find("input").attr("id",selectName);
            });
        }
    });
}

//创建价格分组
function createPriceGroup(currentPriceFromList) {
    currentPriceGroupHTML = "";
    if(currentPriceFromList && currentPriceFromList.length > 0){
        currentPriceGroupHTML += "<div id='${divId}' supplier-id='${supplierId}' style=\"background-color: #f1f0e7;border:1px solid #eee;padding: 0px;padding-top: 10px;margin-bottom: 10px;\" class=\"col-md-12\">";
        currentPriceGroupHTML += " <div name='close' onclick=\"deleteGroup(this);\" title=\"关闭\" style=\"display:none;position: absolute;top: 1px;right: 0px;width: 20px;height: 20px;line-height: 20px;font-size: 20px;text-align: center;cursor: pointer;\">" +
            "<i class=\"fa fa-trash\"></i>" +
            "</div>";
        currentPriceGroupHTML += "<div class=\"layui-form\" style=\"display: block; z-index:10;position: absolute; top: 0px; right: 20px; width: 60px; height: 22px; line-height: 22px;cursor: pointer;\">\n" +
            "                          <input type=\"checkbox\" lay-skin=\"switch\" value='0' lay-text=\"启用|禁用\" checked>\n" +
            "                      </div>";
        var addSupplierFlag = false; //是否有加入供应商控件，加到第一个位置，只加入一次，默认无
        var priceCount = 0; // 控件行数
        $(currentPriceFromList).each(function (i, data) {
            var data = data;
            var required = "";
            var maxLength = data.maxlength ? "manLength=" + data.maxlength : "";
            var minLength = data.minlength ? "minLength=" + data.minlength : "";
            var size = data.size ? "size=" + data.size : "";
            var min = data.min ? "min=" + data.min : "";
            var max = data.max ? "max=" + data.max : "";
            var disabled = data.disabled ? "disabled=" + data.disabled : "";
            var cellName = data.cellName;
            var extendFlag = data.extendFlag; //扩展字段标识：0-仅媒体用、1-仅供应商用，对于媒体价格字段，默认供应商也可使用，如果是0则表示价格
            var cell = "cell:" + data.cellCode + ":" + cellName + ":" + data.type;
            if (priceCount % 4 == 0)
                currentPriceGroupHTML += '<div class="col-md-12">';
            if(!addSupplierFlag){  //如果没加入供应商控件,则加入
                addSupplierFlag = true;
                priceCount ++;
                currentPriceGroupHTML += $("#supplierHTML").html();
            }


            currentPriceGroupHTML += '<div data-extendFlag="${extendFlag}" cell="${cell}" class="form-group col-sm-3"><label class="col-sm-4 control-label">';
            if (data.required) {
                // currentPriceGroupHTML += '<span class="text-red"> * </span>';
                required = "required";
            }
            currentPriceGroupHTML += cellName + ':</label><div class="col-sm-8">';
            switch (data.type) {
                case 'radio':
                case 'checkbox':
                    var dbHtml = "";
                    var dataClass = data.type == "radio" ? "radio-inline" : "checkbox-inline";
                    if(data.dataType && data.dataType == 'html' && data.dbHtml){
                        dbHtml = data.dbHtml;
                    }else if(data.dataType && data.dataType == 'json' && data.dbJson){
                        var json = eval(data.dbJson);
                        if (!Array.isArray(json)){
                            json = [json];
                        }
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += "<label class=\""+dataClass+" i-checks\"><input data-extendFlag='${extendFlag}' cell-name=\"${cell-name}\" title='"+text+"'  type=\""+data.type+"\" value=\""+item.value+"\" name=\"${name}\" data-value=\"${data-value}\" "+required+">"+text+"</label>";
                        });
                    }else {
                        var sqlDatas = data.datas;
                        if (sqlDatas && sqlDatas.length > 0) {  //如果对象存在，并且个数大于0
                            $.each(sqlDatas,function (i,item) {
                                dbHtml += "<label class=\""+dataClass+" i-checks\"><input data-extendFlag='${extendFlag}' cell-name=\"${cell-name}\" title='"+item.name+"'  type=\""+data.type+"\" value=\""+item.id+"\" name=\"${name}\" data-value=\"${data-value}\" "+required+">"+item.name+"</label>";
                            });
                        }
                    }
                    currentPriceGroupHTML += dbHtml;
                    break;
                case 'select':
                    var dbHtml = "";
                    if(data.dataType && data.dataType == 'html' && data.dbHtml){
                        dbHtml = data.dbHtml;
                    }else if(data.dataType && data.dataType == 'json' && data.dbJson){
                        var json = eval(data.dbJson);
                        if (!Array.isArray(json)){
                            json = [json];
                        }
                        dbHtml += "<select data-extendFlag='${extendFlag}' cell-name=\"${cell-name}\" name=\"${name}\" placeholder=\"${labelName}\" data-value=\"${data-value}\" "+required+" class=\"form-control\">";
                        dbHtml += "<option value=\"\">请选择"+cellName+"</option>";
                        $.each(json, function (i, item) {
                            var text = item.hasOwnProperty("text") ? item.text : item.value;
                            dbHtml += '<option value="' + item.value + '" " ' + (item.isDefault == "true" || item.isDefault == true ? "selected" : "") + '>' + text + '</option>';
                        });
                        dbHtml += "</select>";
                    }else {
                        var sqlDatas = data.datas;
                        dbHtml += "<select data-extendFlag='${extendFlag}' cell-name=\"${cell-name}\" name=\"${name}\" placeholder=\"${labelName}\" data-value=\"${data-value}\" "+required+" class=\"form-control\">";
                        dbHtml += "<option value=\"\">请选择"+cellName+"</option>";
                        if (sqlDatas && sqlDatas.length > 0) {  //如果对象存在，并且个数大于0
                            $.each(sqlDatas,function (i,item) {
                                dbHtml += "<option value=\""+item.id+"\">"+item.name+"</option>";
                            });
                        }
                        dbHtml += "</select>";
                    }
                    currentPriceGroupHTML += dbHtml;
                    break;
                case 'textarea':
                    currentPriceGroupHTML += '<textarea data-extendFlag=\'${extendFlag}\' name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " "
                        + maxLength + " " + minLength + " " + size + " " + min + " " + max + ' class="form-control" style="resize:none;"></textarea>';
                    break;
                case 'date':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " "
                        + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                        ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD\'})"/>';
                    break;
                case 'datetime':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " +
                        size + " " + min + " " + max + ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'YYYY-MM-DD hh:mm:ss\'})"/>';
                    break;
                case 'time':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' name="${name}" cell-name="${cell-name}" placeholder="${labelName}" ' + required + " " + maxLength + " " + minLength + " " +
                        size + " " + min + " " + max + ' class="form-control layer-date laydate-icon" onclick="laydate({istime: true, format: \'hh:mm:ss\'})"/>';
                    break;
                case 'price':
                    currentPriceGroupHTML += '<div class="input-group"><span class="input-group-addon">¥</span><input data-extendFlag=\'${extendFlag}\' onkeypress="return inPrice(event)" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                        'oninput="priceChange(this)"  type="number" class="form-control" name="${name}" placeholder="${labelName}" cell-name="${cell-name}"> <!--<span class="input-group-addon">.00</span>--></div>';
                    break;
                case 'file':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' type="file" cell-name="${cell-name}" name="file" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max + " " + disabled + ' class="form-control"/>';
                    break;
                case 'number':
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' onkeypress="return inNum(event)" cell-name="${cell-name}" type="number" name="${name}" id="${name}" placeholder="${labelName}" ' + required + " "
                        + " " + disabled + maxLength + " " + minLength + " " + size + " " + min + " " + max + ' class="form-control"/>';
                    break;
                default:
                    currentPriceGroupHTML += '<input data-extendFlag=\'${extendFlag}\' type="${type}" name="${name}" cell-name="${cell-name}" id="${name}" placeholder="${labelName}" ' + required + " " + maxLength + " "
                        + " " + disabled + minLength + " " + size + " " + min + " " + max + ' class="form-control"/>';
                    break;
            }
            currentPriceGroupHTML = currentPriceGroupHTML.th('id', cell).th('name', cell).th('labelName', cellName).th('type', data.type).th("cell-name", cellName).th("extendFlag",extendFlag).th("cell",cell);
            if ((priceCount + 1) % 4 == 0) currentPriceGroupHTML += '</div></div></div>';
            else currentPriceGroupHTML += '</div></div>';
            priceCount ++;
        });
        currentPriceGroupHTML += "</div>";
    }
    /*if(currentPriceFromList && currentPriceFromList.length > 0){
        currentPriceGroupHTML += "<div id='${divId}' supplier-id='${supplierId}' style=\"background-color: #f1f0e7;border:1px solid #eee;padding: 0px;padding-top: 10px;margin-bottom: 10px;\" class=\"col-md-12\">";
        currentPriceGroupHTML += " <div name='close' onclick=\"deleteGroup(this);\" title=\"关闭\" style=\"display:none;position: absolute;top: 1px;right: 0px;width: 20px;height: 20px;line-height: 20px;font-size: 20px;text-align: center;cursor: pointer;\">" +
            "<i class=\"fa fa-trash\"></i>" +
            "</div>";
        currentPriceGroupHTML += "<div class=\"layui-form\" style=\"display: block; z-index:10;position: absolute; top: 0px; right: 20px; width: 60px; height: 22px; line-height: 22px;cursor: pointer;\">\n" +
            "                          <input type=\"checkbox\" lay-skin=\"switch\" value='' lay-text=\"启用|禁用\" checked>\n" +
            "                      </div>";
        var addSupplierFlag = false; //是否有加入供应商控件，加到第一个位置，只加入一次，默认无
        var priceCount = 0; // 控件行数
        $(currentPriceFromList).each(function (i, data) {
            var data = data;
            var required = "";
            var maxLength = data.maxlength ? "manLength=" + data.maxlength : "";
            var minLength = data.minlength ? "minLength=" + data.minlength : "";
            var size = data.size ? "size=" + data.size : "";
            var min = data.min ? "min=" + data.min : "";
            var max = data.max ? "max=" + data.max : "";
            var cellName = data.cellName;
            if (priceCount % 4 == 0)
                currentPriceGroupHTML += '<div class="col-md-12">';
            if(!addSupplierFlag){  //如果没加入供应商控件,则加入
                addSupplierFlag = true;
                priceCount ++;
                currentPriceGroupHTML += $("#supplierHTML").html();
            }
            currentPriceGroupHTML += '<div class="form-group col-sm-3"><label class="col-sm-4 control-label">';
            if (data.required) {
                currentPriceGroupHTML += '<span class="text-red"> * </span>';
                required = "required";
            }
            var cell = "cell:" + data.cellCode + ":" + cellName + ":" + data.type;
            currentPriceGroupHTML += cellName + ':</label><div class="col-sm-8">';
            currentPriceGroupHTML += '<div class="input-group"><span class="input-group-addon">¥</span><input onkeypress="return inPrice(event)" ' + required + " " + maxLength + " " + minLength + " " + size + " " + min + " " + max +
                'oninput="priceChange(this)"  type="number" class="form-control" name="${name}" placeholder="${labelName}" cell-name="${cell-name}"> <!--<span class="input-group-addon">.00</span>--></div>';
            currentPriceGroupHTML = currentPriceGroupHTML.th('id', cell).th('name', cell).th('labelName', cellName).th('type', data.type).th("cell-name", cellName);
            if ((priceCount + 1) % 4 == 0) currentPriceGroupHTML += '</div></div></div>';
            else currentPriceGroupHTML += '</div></div>';
            priceCount ++;
        });
        currentPriceGroupHTML += "</div>";
    }*/
}

//供应商金额框改变事件
function priceChange(target) {
    /*var name = $(target).attr("name"); //获取当前金额框name值
    //统计价格分组中当前价格类型的最小值，并设置到媒体中对应的价格类型中
    var arr = new Array();
    $("#extendPriceDiv > div").each(function (i, t) {
        var enableFalg = $(t).find("input[type='checkbox']").val() || 0;
        if(enableFalg != 1){  //禁用的供应商价格不计算
            $(t).find("input[name='"+name+"']").each(function (i, t) {
                if ($(t).val() && parseFloat($(t).val())) {
                    arr.push(parseFloat($(t).val()));
                }
            });
        }
    });
    var minPrice = "";
    if(arr.length > 0){
       minPrice = Math.min.apply(null,arr);
    }
    $("#mediaForm").find("input[name='"+name+"']").val(minPrice || 0);*/
}

/**
 * 添加价格分组
 */
function addGroup(supplierId) {
    $("#extendPriceDiv").append(currentPriceGroupHTML.th("divId",supplierId));
    layui.use(["form", 'element'], function () {
        layui.form.render();//layui-form
    });
}

/**
 * 删除价格分组
 * @param t
 */
function deleteGroup(t) {
    var supplierId = $(t).parent().attr("supplier-id");//移除下拉列表对应供应商
    if(supplierId){
        $("#supplierId").find("option").each(function (i,op) {
            if($(op).val() == supplierId){
                $(op).remove(); //清空时移除当前option
                $(t).parent().remove(); //移除分组
            }
        })
    }
    /*if($("#extendPriceDiv > div").length > 1){
        var supplierId = $(t).parent().attr("supplier-id");//移除下拉列表对应供应商
        if(supplierId){
            $("#supplierId").find("option").each(function (i,op) {
                if($(op).val() == supplierId){
                    $(op).remove(); //清空时移除当前option
                    $(t).parent().remove(); //移除分组
                }
            })
        }
        //当价格分组只剩下一个时，删除按钮隐藏
        if($("#extendPriceDiv > div").length == 1){
            $("#extendPriceDiv").children("div:first-child").find("div[name='close']").css("display","none");//当仅有一个分组时，分组右上角的删除按钮隐藏
        }
    }else{
        layer.msg("必须存在一个价格分组！");
    }*/
    reflushGroupBackColor(); //刷新分组背景颜色
    reflushMediaMinPrice();
}

//刷新分组背景颜色
function reflushGroupBackColor() {
    $("#extendPriceDiv > div").each(function (index, div) {
        if((index % 2) == 0){
            $(div).css("backgroundColor","#f1f0e7");
        }else{
            $(div).css("backgroundColor","#e1efed");
        }
    })
}

//更新媒体最低价格
function reflushMediaMinPrice() {
    // $("#extendPriceDiv").children("div:first-child").find("input").trigger("input"); //只要第一个价格组触发input事件
}

/**
 * 新增供应商
 */
/*function addSupplier0() {
    alertEdit('/media/supplier_edit?op=create&mediaTypeId=' + $("#mType").val(), '新增媒体供应商');
}*/

//获取表单json对象（含值为空的字段）
function  getFormJson(formId) {
    var o = {};
    $.each($(formId).serializeArray(), function () {
        if (o[this.name]) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
}

/**
 * 媒体更新
 */
function updateMedia() {
    var id = '#mf';
    var requiredFlag = false; //必输校验
    if(!$(id).valid()){
        requiredFlag = true;
    }
    //价格组必输校验
    if(!$("#extendForm").valid()){
        requiredFlag = true;
        //如果有价格必输提示，则放到div外面，避免样式出错
        $("#extendForm").find("input[type='number']").each(function (i, inputNode) {
            if($(inputNode).attr("required")){
                var $errorLabel = $(inputNode).parent().find("label.error");
                $(inputNode).parent().after($errorLabel);
            }
        })
    }
    //校验layui下拉列表必输，需要放在表单valid方法后面
    if(currentRequiredSelectList && currentRequiredSelectList.length > 0){
        $.each(currentRequiredSelectList, function (i, selectName) {
            var $input = $("select[name='"+selectName+"']").parent().find("input");
            if(!$("select[name='"+selectName+"']").val()){ //如果必输项没有值，则弹出提示
                requiredFlag = true;
                $input.parent().find("label.error").remove();
                $input.after("<label id='${name}-error' for='${name}' class='error' style='display: inline-block;'><i class=\"fa fa-times-circle\"></i>  必填</label>".th("name",selectName));
            }else{
                $input.parent().find("label.error").remove();
            }
        });
    }

    //如果供应商有必输提示，则移动该提示信息到按钮后面，否则布局会歪一点
    supplierRequiredTip();

    if(requiredFlag){
        return;
    }
    reflushMediaMinPrice(); //重新更新最新价格
    // var data = $(id).toJSON();
    var json = getFormJson(id);
    var array = new Array();
    var auditsFlag = false; //媒体基本信息审核字段：媒体某些字段修改不需要审批
    var mediaPriceFlag = false;// 默认校验不通过，价格至少得输入一个价格
    for (var k in json) {
        if (k.indexOf("cell:") > -1) {
            var kk = k.substring(5, k.length);
            var k1 = kk.split(":")[0];
            var k2 = kk.split(":")[1];
            var k3 = kk.split(":")[2];
            var value = json[k];
            var mediaExtend = null;
            if("select" == k3){
                var cellValueText = $("select[name='"+k+"'] option:selected").text();
                mediaExtend = {cell: k1, cellName: k2, cellValue: value, cellValueText:cellValueText, dbType: k3, type: k3};
            }else if("radio" == k3){
                var cellValueText = $("input:radio[name='"+k+"']:checked ").parent().parent().text();
                mediaExtend = {cell: k1, cellName: k2, cellValue: value, cellValueText:cellValueText, dbType: k3, type: k3};
            }else if("checkbox" == k3){
                var cellValueText = new Array();
                $("input:checkbox[name='"+k+"']:checked ").each(function (i, node) {
                    cellValueText.push($(node).parent().parent().text())
                });
                if(value instanceof Array){
                    mediaExtend = {cell: k1, cellName: k2, cellValue: value.join(","), cellValueText:cellValueText.join(","), dbType: k3, type: k3};
                }else{
                    mediaExtend = {cell: k1, cellName: k2, cellValue: value, cellValueText:cellValueText.join(","), dbType: k3, type: k3};
                }
            }else if("price" == k3){
                value = value || 0;
                if(value > 0){
                    mediaPriceFlag = true; //校验通过
                }
                mediaExtend = {cell: k1, cellName: k2, cellValue: value, dbType: k3, type: k3};
            }else{
                mediaExtend = {cell: k1, cellName: k2, cellValue: value, dbType: k3, type: k3};
            }

            //判断是否需要审核，不需要使用最低价进行判断，最低价维护通过修改的关系审核通过后进行维护
            if(!auditsFlag && "price" != k3){ //当判断已需要审核时，不需要再赋值了，否则会覆盖
                auditsFlag = oldMediaMap[k1] == mediaExtend.cellValue ? false : true;  //判断旧值是否修改，决定是否需要重新审核
            }
            if(mediaExtendMap[k1]  == undefined){ //如果原来没有，则新增
                auditsFlag = true;
                mediaExtend.editAddStatus = true;
            }
            array.push(mediaExtend);
            delete json[k];
        }
        //仅改了（图标、备注、供应商、责任人 ）字段不需要审核
        if(!auditsFlag && k != 'remarks' && k != 'userId' && k != 'supplierId' && k != 'supplierName' && k != 'picPath' && (k.indexOf("cell:") == -1) && json[k] != oldMediaMap[k]){
            auditsFlag = true;
        }
    }
    json["mediaExtends"] = array;

    /*if(!mediaPriceFlag){
        swal({
            title: "媒体价格校验失败",
            text: "媒体至少得输入一个价格，且必须大于0！",
            type: "warning",
            html: true
        });
        return;
    }*/

    //下面统计供应商价格
    var validErrorInfo = new Array(); //校验供应商的价格分组至少得输入一个价格
    var supplierArr = new Array(); //供应商列表

    //如果媒体有选择供应商联系人
    if(json.supplierId){
        if(Array.isArray(json.supplierId)){
            supplierArr = json.supplierId;
        }else{
            supplierArr.push(json.supplierId);
        }
    }

    var supplierList = new Array();
    $.each(supplierArr,function (i,id) {
        var obj = {supplierId: id};
        //1、判断供应商是否新增还是修改
        if(!mediaSupplierMap[id]){ //新增
            obj.editAddStatus = true; //新增的供应商分组
            obj.auditsFlag = true; //关系需要审核
            // if(!auditsFlag)  auditsFlag = true; //需要审核
        }else{
            obj.id = mediaSupplierMap[id].mediaSupplierRelateId; //设置关系ID
        }
        if(currentPriceGroupHTML){ //存在价格分组，目前一定存在
            var validFlag = false; // 默认校验不通过，价格分组至少得输入一个价格
            obj.supplierName = $("#supplierPrice"+id).find("input[name='supplierName']").val();
            obj.enabled = $("#supplierPrice"+id).find("input[type='checkbox']").val() || '0';//是否启用，默认启用
            if(mediaSupplierMap[id] && mediaSupplierMap[id].enabled != obj.enabled){ //启用状态发生改变
                obj.editEnableStatus = true; //启用状态改变
                obj.auditsFlag = true; //关系需要审核
                // if(!auditsFlag)  auditsFlag = true; //需要审核
            }
            var priceArray = new Array();  //当有多个价格时，才使用，否则能将价格存储array中
            $("#supplierPrice"+id).find("div[data-extendFlag]").each(function (index,divObj) { //遍历金额控件
                var name = $(divObj).attr("cell");
                if (name.indexOf("cell:") > -1) { //有值
                    var name1 = name.substring(5, name.length);
                    var cell = name1.split(":")[0];
                    var cellName = name1.split(":")[1];
                    var type = name1.split(":")[2];
                    var supplierFieldObj = {};
                    if("select" == type){
                        var value = $(divObj).find("select[name='"+name+"'] option:selected").val() || "";
                        var cellValueText = value ? $(divObj).find("select[name='"+name+"'] option:selected").text() : "";
                        supplierFieldObj = {cell: cell, cellName: cellName, cellValue: value, cellType: type, type: type, cellValueText:cellValueText};
                    }else if("radio" == type){
                        var value = $(divObj).find("input:radio[name='"+name+"']:checked ").val() || "";
                        var cellValueText = $(divObj).find("input:radio[name='"+name+"']:checked ").parent().parent().text();
                        supplierFieldObj = {cell: cell, cellName: cellName, cellValue: value, cellType: type, type: type, cellValueText:cellValueText};
                    }else if("checkbox" == type){
                        var cellValue = new Array();
                        var cellValueText = new Array();
                        $(divObj).find("input:checkbox[name='"+name+"']:checked ").each(function (i, node) {
                            cellValue.push($(node).val());
                            cellValueText.push($(node).parent().parent().text());
                        });
                        supplierFieldObj = {cell: cell, cellName: cellName, cellValue: cellValue.join(","), cellType: type, type: type, cellValueText:cellValueText.join(",")};
                    }else if("price" == type){
                        var value = $(divObj).find("input").val() || 0;
                        if(value > 0){
                            validFlag = true; //校验通过
                        }
                        supplierFieldObj = {cell: cell, cellName: cellName, cellValue: value, cellType: type, type: type};
                    }else{
                        var value = $(divObj).find("input").val() || '';
                        supplierFieldObj = {cell: cell, cellName: cellName, cellValue: value, cellType: type, type: type};
                    }
                    //如果供应商价格分组不是新增的，则设置媒体供应商关系主键ID
                    if(!obj.editAddStatus){
                        supplierFieldObj.mediaSupplierRelateId = mediaSupplierMap[id].mediaSupplierRelateId;
                        if(!obj.auditsFlag){
                            obj.auditsFlag = mediaSupplierMap[id][cell] == value ? false : true;  //判断旧值是否修改，决定是否需要重新审核
                        }
                        if(mediaSupplierMap[id][cell] == undefined){ //原来没有则为新增的扩展价格
                            supplierFieldObj.editAddStatus = true;
                        }
                    }
                    priceArray.push(supplierFieldObj);
                }
            });
            /*$("#supplierPrice"+id).find("input[type='number']").each(function (index,price) { //遍历金额控件
                var name = $(price).attr("name");
                var value = $(price).val() || 0;
                if (name.indexOf("cell:") > -1) { //有值
                    if(value > 0){
                        validFlag = true; //校验通过
                    }
                    var name1 = name.substring(5, name.length);
                    var cell = name1.split(":")[0];
                    var cellName = name1.split(":")[1];
                    var type = name1.split(":")[2];
                    var supplierPriceObj = {cell: cell, cellName: cellName, cellValue: value, cellType: type, type: type};
                    //如果供应商价格分组不是新增的，则设置媒体供应商关系主键ID
                    if(!obj.editAddStatus){
                        supplierPriceObj.mediaSupplierRelateId = mediaSupplierMap[id].mediaSupplierRelateId;
                        if(!obj.auditsFlag){
                            obj.auditsFlag = mediaSupplierMap[id][cell] == value ? false : true;  //判断旧值是否修改，决定是否需要重新审核
                        }
                        if(mediaSupplierMap[id][cell] == undefined){ //原来没有则为新增的扩展价格
                            supplierPriceObj.editAddStatus = true;
                        }
                    }
                    priceArray.push(supplierPriceObj);
                }
            });*/
            if(!validFlag){
                validErrorInfo.push("供应商["+obj.supplierName+"]至少得输入一个价格！")
            }
            obj.mediaPriceExtends = priceArray;
        }else{
            obj.supplierName = $("#supplier"+id).val();
        }
        supplierList.push(obj);
    });
    json["supplierList"] = supplierList;

    //计算出删除的供应商
    if(mediaSupplierMap && Object.getOwnPropertyNames(mediaSupplierMap).length > 0){
        var deleteSupplierList = new Array();
        for(var id in mediaSupplierMap){
            if(!supplierArr.contains(id)){
                deleteSupplierList.push(mediaSupplierMap[id].mediaSupplierRelateId)
            }
        }
        if(deleteSupplierList.length > 0){
            // if(!auditsFlag)  auditsFlag = true; //需要审核
            json["deleteSupplierRelateIds"] = deleteSupplierList;
        }
    }

    delete json["supplierId"];
    delete json["supplierName"];

    //如果有供应商价格，则必须输入一个价格
    if(supplierArr && supplierArr.length > 0 && validErrorInfo.length > 0){
        swal({
            title: "价格校验失败",
            text: validErrorInfo.join("<br>"),
            type: "warning",
            html: true
        });
        return;
    }
    json.auditsFlag = auditsFlag; //媒体是否需要审核标识
    // if(!auditsFlag){
    //     alert("不需要审核");
    // }else{
    //     alert("需要审核");
    // }
    // return;

    startModal("#" + $("#saveBtn").attr("id"));
    $.ajax({
        url: baseUrl + "/mediaAudit/update",
        type: 'POST',
        cache: false,
        contentType: 'application/json;charset=utf-8', //设置请求头信息
        data: JSON.stringify(json),
        dataType: "json",
        success: function (data) {
            Ladda.stopAll();
            swal({
                title: data.code == 200 ? "成功!" : "失败",
                text: data.code == 200 ? "媒体更新成功！" : data.msg,
                type: data.code == 200 ? "success" : "error",
            }, function (isConfirm) {
                if (data.code == 200) {
                    triggerPageBtnClick("/media1/mediaList","mediaListSearchBtn"); //触发媒体管理Tab刷新
                    triggerPageBtnClick("/media1/audits","auditSearchBtn"); //触发媒体审核Tab刷新
                    triggerPageBtnClick("/media1/mediaSupplierAuditList","mediaListSearchBtn"); //触发媒体关系审核Tab刷新
                    triggerPageBtnClick("/media1/mediaSupplierList","mediaListSearchBtn"); //触发媒体关系Tab刷新
                    closeCurrentTab();
                }
            });
            // document.location.reload();//刷新当前页面
        }
    }).done(function (res) {
    }).fail(function (res) {
    });
}


//供应商必输提示控制
function supplierRequiredTip() {
    /*if(!$("#supplierId").val()){
        $("#supplierId").parent().find("label.error").remove();
        $("#addSupplierBtn").after("<label id='supplierId-error' for='supplierId' class='error' style='display: inline-block;'><i class=\"fa fa-times-circle\"></i>  必填</label>");
    }else{
        $("#supplierId").parent().find("label.error").remove();
    }*/
}

//select2加载供应商信息-此处需要个性化操作，所以写到此处（head.js中的对多选操作有点问题）
function loadMediaSupplier(t) {
    var selected = null;
    if(mediaSupplierMap && Object.getOwnPropertyNames(mediaSupplierMap).length > 0){
        selected = new Array();
        for(var supplierId in mediaSupplierMap){
            var temp = {id:supplierId, text:mediaSupplierMap[supplierId].name}
            selected.push(temp);
        }
    }
    var options = {
        url: "/supplier/listSupplierByPlateCompany",      // 数据接口url
        size: 30, // 每次加载的数据条数
        value: "id",  // 下拉框value字段名称
        name: ["name", "contactor" , "phone"], // 下拉框显示字段名称
        selected: selected,  // 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
        params: {},
        placeholder: "请选择供应商"
    };
    ajaxSelect2($(t), options);
}
/**
 * ajax获取select2下拉框数据（带鼠标滚动分页）
 * @param obj 下拉框对象；
 * @param options 选项，包含如下字段：
 * url 数据接口url
 * size 每次加载的数据条数
 * name 下拉框显示字段名称
 * value 下拉框value字段名称
 * placeholder 默认显示的文字
 * selected 默认选中项，格式：[{id:1,text:"选项1"},{id:2,text:"选项2"}]
 * formatResult 返回结果回调函数，可以在该回调中，自定义下拉框数据的显示样式，比如：加入图片等
 * templateSelection 选中项回调，该参数必须与formatResult参数搭配使用
 * 注意点1 : 后端接口需返回 data（具体数据）和 total（总页数）两个字段
 * 注意点2 : 两个自定义的回调函数中，必须要把处理结果return回来，如果没有传入formatResult参数，则采用默认的显示样式
 */
function ajaxSelect2(obj, options, formatResult, formatSelected) {
    var value = options["value"];
    var name = options["name"];
    var flag = (typeof formatResult === "function") ? true : false;
    var select2Option = {
        language: "zh-CN",
        multiple: true,                         // 多选，默认false
        allowClear: true,
        placeholder: options["placeholder"] || "请选择",
        ajax: {
            url: options["url"],
            type: "post",
            dataType: "json",
            delay: 250,
            data: function (params) {
                var optionParams = options["params"];
                //动态获取板块类型
                // optionParams.mediaTypeId = $("#mType").val();
                // 搜索框内输入的内容
                optionParams.name = params.term;
                // 当前页
                optionParams.page = params.page || 1;
                // 每页显示多少条记录，默认10条
                optionParams.size = options["size"] || 10;
                // 传递到后端的参数
                return optionParams;
            },
            cache: true,
            processResults: function (res, params) {
                params.page = params.page || 1;
                var cbData = [];
                if (flag) {
                    cbData = res.list;
                } else {
                    if (res.pages >= params.page) {
                        var data = res.list;
                        var len = data.length;
                        var text;
                        for (var i = 0; i < len; i++) {
                            text = data[i][name[0]];
                            for (var j = 1; j < 2; j++) {
                                //如果供应商责任人为当前用户，则手机号不加密
                                if (data[i]["creator"] && data[i]["creator"] == user.id) {
                                    text += "(" + data[i][name[j]] + "-" + (data[i][name[2]] || "") + ")";
                                } else {
                                    text += "(" + data[i][name[j]] + "-" + buildPhone(data[i][name[2]]) + ")";
                                }
                            }
                            cbData.push({"id": data[i][value], "text": text});
                        }
                    }
                }
                return {
                    results: cbData,
                    pagination: {
                        more: params.page < res.total
                    }
                };
            }
        },
        escapeMarkup: function (markup) {
            // 字符转义处理
            return markup;
        },
        // 最少输入N个字符才开始检索，如果想在点击下拉框时加载数据，请设置为 0
        minimumInputLength: 0
    };
    if (flag) {
        select2Option.templateResult = formatResult;
        select2Option.templateSelection = formatSelected;
    }
    var $select = obj;
    $select.select2(select2Option);

    //取消选择（点击清空和再次点击时触发）
    $select.on("select2:unselect",function(e){
        var currentId = e.params.data.id; //获取当前操作的ID
        var supplierTmp = mediaSupplierMap[currentId] || {};
        //如果媒体供应商关系创建人 = 当前用户，可以删除，否则不行
        if (supplierTmp.relateUserId != user.id) {
            return;
        }

        $select.find("option").each(function (i,t) {
            if($(t).val() == currentId){
                $(t).remove(); //清空时移除当前option
            }
        });
        //判断价格分组是否存在，存在则删除分组，并且保留最后一个分组，否则，删除供应商名称隐藏域
        if(currentPriceGroupHTML){
            $("#supplierPrice"+currentId).remove();
            /*if($("#extendPriceDiv > div").length > 1){  //超过一个分组，可删除当前供应商分组
                $("#supplierPrice"+currentId).remove();
            }else{  //最后一个分组，则清空所有input值
                $("#supplierPrice"+currentId).find("input").val("0");// 给当前分组供应商赋值
                $("#supplierPrice"+currentId).attr("supplier-id",""); // 给当前分组供应商赋值
                //当仅有一个分组时，分组右上角的删除按钮隐藏
                $("#supplierPrice"+currentId).find("div[name='close']").css("display","none");
            }
            //当删除后，只剩下一个价格分组时，删除按钮隐藏
            if($("#extendPriceDiv > div").length == 1){
                $("#extendPriceDiv").children("div:first-child").find("div[name='close']").css("display","none");//当仅有一个分组时，分组右上角的删除按钮隐藏
            }*/
        }else {
            $("#supplier"+currentId).remove(); //移除供应商名称隐藏域
        }

        supplierRequiredTip(); //供应商必输提示
        reflushGroupBackColor(); //刷新分组背景颜色
        reflushMediaMinPrice();
    });

    //选择下拉列表值触发
    $select.on("select2:select",function(e){
        var values = $select.val();
        var currentId = e.params.data.id;
        var currentText = e.params.data.text;
        // var lastIndex = currentText.lastIndexOf("(");
        // currentText = lastIndex != -1 ? currentText.substr(lastIndex+1,(currentText.lastIndexOf(")")-lastIndex-1)) : currentText;
        //判断价格分组是否存在，存在则添加分组，否则，添加供应商名称隐藏域
        if(currentPriceGroupHTML){
            addGroup("supplierPrice"+currentId); //创建价格分组
            $("#supplierPrice"+currentId).find("input[name='supplierName']").val(currentText || "");// 给当前分组供应商赋值
            $("#supplierPrice"+currentId).find("input[name='supplierName']").attr("title", currentText || "");// 给当前分组供应商赋值
            $("#supplierPrice"+currentId).attr("supplier-id",currentId); // 给当前分组供应商赋值
            $("#extendPriceDiv").find("div[name='close']").css("display","block"); //所有关闭按钮显示
            /*if(values && values.length > 1){
                addGroup("supplierPrice"+currentId); //创建价格分组
                $("#supplierPrice"+currentId).find("input[name='supplierName']").val(currentText || "");// 给当前分组供应商赋值
                $("#supplierPrice"+currentId).find("input[name='supplierName']").attr("title", currentText || "");// 给当前分组供应商赋值
                $("#supplierPrice"+currentId).attr("supplier-id",currentId); // 给当前分组供应商赋值
                $("#extendPriceDiv").find("div[name='close']").css("display","block"); //所有关闭按钮显示
            }else{
                $("#extendPriceDiv").children("div:first-child").attr("id","supplierPrice"+currentId); //给第一个分组ID重新赋值
                $("#extendPriceDiv").children("div:first-child").attr("supplier-id",currentId); //给第一个分组ID重新赋值
                $("#extendPriceDiv").children("div:first-child").find("input[name='supplierName']").val(currentText || "");//给第一个供应商赋值
                $("#extendPriceDiv").children("div:first-child").find("input[name='supplierName']").attr("title", currentText || "");//给第一个供应商赋值
                $("#extendPriceDiv").children("div:first-child").find("div[name='close']").css("display","none");//当仅有一个分组时，分组右上角的删除按钮隐藏
            }*/
            reflushGroupBackColor(); //刷新分组背景颜色
        }else{
            $("#extendPriceDiv").append(" <input id=\"supplier"+currentId+"\" value=\""+currentText+"\" name=\"supplierName\" type=\"hidden\">");
        }

        supplierRequiredTip(); //供应商必输提示
    });
    if (!flag) {
        // 默认选中项设置
        var html = '';
        var values = [];
        var selected = options['selected'];
        if (selected) {
            $.each(selected, function (index, item) {
                values.push(item.id);
                html += '<option value="' + item.id + '">' + item.text + '</option>';
            });
            $select.append(html);
            $select.val(values).trigger('change'); //触发select的改变事件
        }
    }
}

//手机号封装
function buildPhone(value) {
    value = value || "";
    if(value){
        if(value.length >= 11){
            var start = value.length > 11 ? "*****" : "****";
            return value.substring(0, 3) + start + value.substring(value.length - 4, value.length);
        }else if(value.length >= 3){
            return value[0] + "***" + value[value.length - 1];
        }else {
            return "**";
        }
    }else {
        return "";
    }
}

//供应的管理
var mediaSupplierManageObj = {
    checkSupplierUrl: "/supplier/checkSupplier",
    addSupplierUrl: "/supplier/addSupplier",
    addSupplierCheckRule:function (modalId) {
        var icon = "<i class='fa fa-times-circle'></i> ";
        $("#"+modalId).find("form").validate({
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
                                return $("#"+modalId).find("form input[name='phone']").val();
                            },
                            "name": function () {
                                return $("#"+modalId).find("form input[name='name']").val();
                            },
                            "id": function () {
                                return $("#"+modalId).find("form input[name='id']").val() || "";
                            }
                        },
                        dataFilter: function (data) {
                            data = JSON.parse(data);
                            if (data.code == 200) {
                                return true;
                            } else {
                                $("#"+modalId).find("form input[name='phone']").focus();
                                return false;
                            }
                        }
                    }
                },
            },
            messages: {
                phone: {remote: icon + "很抱歉，供应商联系人和手机已经存在！"},
            }
        });
    },
    resetCompanyNatureBtn:function (modalId) {
        $("#"+modalId).find(".companyBtnCls").removeAttr("btn-white");
        if(!$("#"+modalId).find(".companyBtnCls").hasClass("btn-info")){
            $("#"+modalId).find(".companyBtnCls").addClass("btn-info");
        }
        $("#"+modalId).find(".personBtnCls").removeAttr("btn-info");
        if(!$("#"+modalId).find(".personBtnCls").hasClass("btn-white")){
            $("#"+modalId).find(".personBtnCls").addClass("btn-white");
        }
    },
    addSupplierClick:function () {
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
            if(inputNameList.contains($(input).attr("name"))){
                $(input).val("0");
            }else {
                $(input).val("");
            }
        });
        $("#mediaSupplierAddModal").modal("toggle");
    },
    addSupplier:function (t) {
        if(!$(t).closest(".modal-content").find("form").valid()){
            return;
        }
        startModal("#" + $(t).attr("id"));
        commonObj.requestData(JSON.stringify($(t).closest(".modal-content").find("form").serializeForm()), mediaSupplierManageObj.addSupplierUrl, "post", "json", true, function (data) {
            Ladda.stopAll();
            if(data.code == 200){
                layer.msg("新增供应商联系人成功！", {time: 2000, icon: 6});
                $("#mediaSupplierAddModal").modal("toggle");

                mediaSupplierManageObj.reflushTable('table_suppliers', $('#supplier').serializeJson());
            }else {
                layer.msg(data.msg, {time: 3000, icon: 5});
            }
        }, null, true);
    },
}

//公司筛选
var companyObj = {
    companySearchUrl: "/company/companySearch",
    checkCompanyUrl:  "/company/checkCompany",
    currentCompanyName:"",
    firstPageTotal: 0, //第一页查询缓存表数据总数
    natureClick:function (t, supplierNature) {
        $(t).closest("form").find(".companyWrap").html("");
        //发生改变才进行处理
        if(supplierNature != $(t).closest("form").find("input[name='supplierNature']").val()){
            if(supplierNature == 1){
                $(t).closest("form").find("input[name='name']").val("个体供应商");
                $(t).closest("form").find("input[name='name']").attr("readonly", true);
                //设置公司名称非标准
                $(t).closest("form").find(".companyTipsYes").hide();
                $(t).closest("form").find(".companyTipsNo").hide();
                $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
            }else {
                $(t).closest("form").find("input[name='name']").val("");
                $(t).closest("form").find("input[name='name']").removeAttr("readonly");
            }
            //改变按钮颜色
            $(t).closest("div").find("button").each(function (i, btn) {
                $(btn).removeClass("btn-info");
                if(!$(btn).hasClass("btn-white")){
                    $(btn).addClass("btn-white");
                }
            });
            $(t).removeClass("btn-white");
            if(!$(t).hasClass("btn-info")){
                $(t).addClass("btn-info");
            }
            $(t).closest("form").find("input[name='supplierNature']").val(supplierNature);
        }
    },
    renderCompanyItem:function (page, pageSize, companyList) {
        var html = "";
        if(companyList && companyList.length > 0){
            $.each(companyList, function (m, company) {
                html += "<div onmousedown='companyObj.chooseCompany(this);' class=\"companyNameItem\" title=\""+(company.companyName || "")+"\"><span>"+(company.companyName || "")+"</span></div>";
            });
        }
        return html;
    },
    search:function (t) {
        $(t).closest("form").find(".companyWrap").html("");
        var keyword = $(t).closest("form").find("input[name='name']").val();
        if(!keyword){
            if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")){
                $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
            }
            return;
        }else {
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
        }
        layui.use('flow', function(){
            var flow = layui.flow;
            flow.load({
                elem: $(t).closest("form").find(".companyWrap"),
                isAuto: false,
                done: function(page, next){
                    //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                    //请求数据
                    var param = {keyword:keyword};
                    param.page = page; //页码
                    param.size = 20; //每页数据条数
                    commonObj.requestData(param, companyObj.companySearchUrl, "post", "json", false, function (data) {
                        //第一页是从缓存表拿数据，记录数据总数
                        if(page == 1){
                            companyObj.firstPageTotal = data.total;
                        }
                        next(companyObj.renderCompanyItem(page, param.size, data.list), page < data.pages); //如果小于总页数，则继续
                    });
                }
            });
        });
    },
    enterEvent:function (t, event) {
        if((event.keyCode == '13' || event.keyCode == 13)){
            companyObj.search(t);
        }
    },
    checkCompany: function (t) {
        var keyword = $(t).closest("form").find("input[name='name']").val();
        if(!keyword){
            $(t).closest("form").find(".companyTipsYes").hide();
            $(t).closest("form").find(".companyTipsNo").hide();
            $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
            return;
        }
        //如果是个体工商户，不需要校验
        if($(t).closest("form").find("input[name='supplierNature']").val() != 1){
            commonObj.requestData({keyword:keyword}, companyObj.checkCompanyUrl, "post", "json", false, function (data) {
                if(data.code == 200){
                    $(t).closest("form").find(".companyTipsYes").show();
                    $(t).closest("form").find(".companyTipsNo").hide();
                    $(t).closest("form").find("input[name='standarCompanyFlag']").val(1);
                }else {
                    $(t).closest("form").find(".companyTipsYes").hide();
                    $(t).closest("form").find(".companyTipsNo").show();
                    $(t).closest("form").find("input[name='standarCompanyFlag']").val(0);
                }
            });
        }
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")){
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    chooseCompany:function (t) {
        $(t).closest("form").find("input[name='name']").val($(t).attr("title") || "");
        //隐藏弹出筛选框
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")){
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOut:function (t) {
        if (!$(t).closest("form").find(".companyWrap").hasClass("companyPanelCancel")){
            $(t).closest("form").find(".companyWrap").addClass("companyPanelCancel");
        }
    },
    mourseOver:function (t) {
        //如果有内容则展示
        if($(t).closest("form").find(".companyWrap").find("div").length > 0){
            $(t).closest("form").find(".companyWrap").removeClass("companyPanelCancel");
            $(t).closest("form").find("input[name='name']").focus();
        }
    },
    //验证电话号码格式
    checkPhone: function (t) {
        var telPatten = /^[1]([3-9])[0-9]{9}$/;
        if(telPatten.test($(t).val())){
            $(t).closest("form").find(".phoneTipsYes").show();
            $(t).closest("form").find(".phoneTipsNo").hide();
            $(t).closest("form").find("input[name='standarPhoneFlag']").val(1);
        }else {
            $(t).closest("form").find(".phoneTipsYes").hide();
            $(t).closest("form").find(".phoneTipsNo").show();
            $(t).closest("form").find("input[name='standarPhoneFlag']").val(0);
        }
    },
}

//公共方法
var commonObj = {
    requestData: function (data, url, requestType,dataType,async,callBackFun,callErrorFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if(callErrorFun){
            param.error = callErrorFun;
        }
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    isMJBZ:function () {
        var roles = user.roles;//获取用户角色
        var isMJBZ = false;//是否媒介政务
        if(roles){
            for(var i=0; i < roles.length; i++){
                if(roles[i].code == 'BZ' && roles[i].type == 'MJ'){
                    isMJBZ = true;
                    break;
                }
            }
        }
        return isMJBZ;
    }
}

//编辑权限控制，媒体责任人可修改媒体信息，媒体供应商关系责任人可以修改供应商价格信息
function renderMediaEdit(media) {
    var mediaUserId = media.userId || "";
    //如果媒体责任人是当前用户，支持编辑，否则不支持
    if (mediaUserId == user.id) {
        $("#mediaForm").find("button").css("display", "inline-block");//上传图标按钮显示
        $("#mediaForm").find("textarea").removeAttr("disabled");
        $("#mediaForm").find("input[type!='radio'][type!='checkbox'][cellType!='date']").removeAttr("readonly");
        $("#mediaForm").find("input[type='radio']").removeAttr("disabled");
        $("#mediaForm").find("input[type='checkbox']").removeAttr("disabled");
        $("#mediaForm").find("input[cellType='date']").removeAttr("disabled");
        $("#mediaForm").find("select[id!='supplierId']").removeAttr("disabled");
        //禅道bug1888: 修改别人的媒体，不能搜索供应商
        $("#mediaForm").find("select[id='supplierId']").closest("div").find("li.select2-search > input.select2-search__field").removeAttr("readonly");
    } else {
        $("#mediaForm").find("button").css("display", "none");//上传图标按钮隐藏
        $("#mediaForm").find("textarea").attr("disabled", true);
        $("#mediaForm").find("input[type!='radio'][type!='checkbox'][cellType!='date']").attr("readonly", true);
        $("#mediaForm").find("input[type='radio']").attr("disabled", true);
        $("#mediaForm").find("input[type='checkbox']").attr("disabled", true);
        $("#mediaForm").find("input[cellType='date']").attr("disabled", true);
        $("#mediaForm").find("select[id!='supplierId']").attr("disabled", true);
        //禅道bug1888: 修改别人的媒体，不能搜索供应商
        $("#mediaForm").find("select[id='supplierId']").closest("div").find("li.select2-search > input.select2-search__field").removeAttr("readonly");
    }
    //如果供应商关系创建人是当前用户，支持编辑，否则不支持
    if (media && media.supplierList && media.supplierList.length > 0) {
        $.each(media.supplierList, function (it, supplierData) {
            var relateUserId = supplierData.relateUserId || "";
            var supplierId = supplierData.supplierId || "";
            if (relateUserId == user.id) {
                $("#" + "supplierPrice" + supplierId).find("div[name='close']").css("display", "block");//关闭按钮显示
                $("#" + "supplierPrice" + supplierId).find("textarea").removeAttr("disabled");
                $("#" + "supplierPrice" + supplierId).find("input[type!='radio'][type!='checkbox'][cellType!='date']").removeAttr("readonly");
                $("#" + "supplierPrice" + supplierId).find("input[type='radio']").removeAttr("disabled");
                $("#" + "supplierPrice" + supplierId).find("input[type='checkbox']").removeAttr("disabled");
                $("#" + "supplierPrice" + supplierId).find("input[cellType='date']").removeAttr("disabled");
                $("#" + "supplierPrice" + supplierId).find("select").removeAttr("disabled");
            } else {
                $("#" + "supplierPrice" + supplierId).find("div[name='close']").css("display", "none");//关闭按钮隐藏
                $("#" + "supplierPrice" + supplierId).find("textarea").attr("disabled", true);
                $("#" + "supplierPrice" + supplierId).find("input[type!='radio'][type!='checkbox'][cellType!='date']").attr("readonly", true);
                $("#" + "supplierPrice" + supplierId).find("input[type='radio']").attr("disabled", true);
                $("#" + "supplierPrice" + supplierId).find("input[type='checkbox']").attr("disabled", true);
                $("#" + "supplierPrice" + supplierId).find("input[cellType='date']").attr("disabled", true);
                $("#" + "supplierPrice" + supplierId).find("select").attr("disabled", true);
            }
        });
    }
}