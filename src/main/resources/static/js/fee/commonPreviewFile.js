/***
 * 附件预览公共类
 * 1、调用方式：<script type="text/javascript" th:src="@{//js/fee/commonPreviewFile.js(v=${version})}"></script>
 * 2、使用方法
 * 直接调用previewFile（）
 * 3、参数说明
 *       title - 弹窗标题
 *    fileName - 附件名称
 *    filePath - 附件路径
 *        flag - 图片标志
 * requestType - 请求方式
 *         url - 请求路径
 *        move - 弹窗移动
 *     moveOut - 是否可拖动窗口外
 *     zIndex  -设置弹窗层级，默认：10000
 *      shade - 默认是0.3透明度的黑色背景（'#000'）。如果你想定义别的颜色，可以shade: [0.8, '#393D49']；如果你不想显示遮罩，可以shade: 0
 * shadeClose - 是否点击遮罩层关闭窗口，默认关闭
 *     resize - 弹窗是否可缩放，默认不可缩放
 * successCallback - 提交成功回调函数
 */
function CommonPreviewFile(config) {
    this.title = config.title || "文件预览";
    this.fileName = config.fileName || "";
    this.filePath = config.filePath || "";
    this.flag = config.flag || "";
    this.url = config.url || "";
    this.param = config.param || {};
    this.requestType = config.requestType || "post";
    this.successCallback = config.successCallback || null;
    this.move = config.move || '.layui-layer-title';
    this.moveOut = config.moveOut || true;
    this.zIndex = config.zIndex || 10;
    this.shade = config.shade || 0;
    this.shadeClose = config.shadeClose || true;
    this.resize = config.resize | false;
    this.modalIndex = null;
    this.content = config.content || "<div id='fileDiv' class='ibox-content' style='height: 100%;width:100%;padding: 0px;'></div>";
}

CommonPreviewFile.prototype = {
   constructor: CommonPreviewFile,
    _this: this,
    requestData: function (data, url, requestType, dataType, async, callBackFun, callErrorFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        }
        if (callErrorFun) {
            param.error = callErrorFun;
        }
        if (contentType) {
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    render:function () {
        var _this = this;
        var $parentDiv = $("<div></div>");
        $parentDiv.html(_this.content);
        _this.modalIndex = layer.open({
            type: 1,
            title: _this.title,
            content: $parentDiv.html(),
            btn: [],
            area: ["1080px","560px"],
            shade: _this.shade,
            shadeClose: _this.shadeClose,
            resize: _this.resize,
            move: _this.move,
            moveOut: _this.moveOut,
            success: function (layero, index) {
                if(_this.flag==0){
                    var ext=_this.fileName.substring(_this.fileName.lastIndexOf(".")+1);
                    if(ext.toLowerCase()=="pdf" || ext.toLowerCase()=="html"){
                        var html="<iframe width='100%' height='496' style='border: 0px;' src='"+_this.filePath+"'></iframe>";
                        $(layero[0]).find("div[id='fileDiv']").html(html);
                        $(layero[0]).find(".layui-layer-content").css("overflow","hidden");
                        $(layero[0]).find(".layui-layer-btn").css("background-color","#f1f1f1");
                    }else{
                        var index = layer.load(4,{time:10000});
                        $.ajax({
                            type: "post",
                            url: baseUrl +"/reimbursement/previewFile",
                            data: {fileName:_this.fileName,filePath:_this.filePath},
                            dataType: "json",
                            success:function (data) {
                                layer.close(index);
                                if(data.code==200){
                                    if(data.data.ext=="txt"){
                                        $(layero[0]).find("div[id='fileDiv']").html(data.data.stream);
                                        $(layero[0]).find(".layui-layer-content").css("overflow","auto");
                                        $(layero[0]).find(".layui-layer-btn").css("background-color","#f1f1f1");
                                    }else if(data.data.ext=="csv"){
                                        var $tableELem = $("<table border=\"1px\" align=\"center\" width=\"100%\"\n" +
                                            "style=\"text-align: center;line-height: 30px;font-weight: bold;\"></table>");
                                        var content = "";
                                        if(data.data.title!=null){
                                            var title = data.data.title.split(",");
                                            content+="<tr>";
                                            for(var i=0;i<title.length;i++){
                                                content+="<td>"+title[i]+"</td>";
                                            }
                                            content+="</tr>";
                                        }
                                        if(data.data.content!=null){
                                            for(var j=0;j<data.data.content.length;j++){
                                                content+="<tr>";
                                                var text = data.data.content[j];
                                                var rowData = text.split(",");
                                                for(var k=0;k<rowData.length;k++){
                                                    content+="<td>"+rowData[k]+"</td>";
                                                }
                                                content+="</tr>";
                                            }
                                        }
                                        $tableELem.append(content);
                                        $(layero[0]).find("div[id='fileDiv']").html($tableELem);
                                        $(layero[0]).find(".layui-layer-content").css("overflow","auto");
                                        $(layero[0]).find(".layui-layer-btn").css("background-color","#f1f1f1");
                                    }else {
                                        var html="<iframe width='100%' height='496' style='border: 0px;' src='"+data.data.stream+"'></iframe>";
                                        $(layero[0]).find("div[id='fileDiv']").html(html);
                                        $(layero[0]).find(".layui-layer-content").css("overflow","hidden");
                                        $(layero[0]).find(".layui-layer-btn").css("background-color","#f1f1f1");
                                    }
                                }else if(data.code==1002){
                                    swal({
                                        title:"提示",
                                        text:data.msg
                                    });
                                }
                            },
                            error:function (data) {
                                layer.close(index);
                            }
                        });
                    }
                }else{
                    var html="<img alt='"+_this.fileName+"' style='display: block;margin: 0 auto;max-height: 100%' src='"+_this.filePath+"'/>";
                    $(layero[0]).find("div[id='fileDiv']").html(html);
                    $(layero[0]).find(".layui-layer-content").css("overflow","auto");
                    $(layero[0]).find(".layui-layer-btn").css("background-color","#f1f1f1");
                }
            },
        });
    }
}

//文件预览方法：flag：1图片格式，0文件格式
function previewFile(fileName,filePath,flag) {
    new CommonPreviewFile({
        title:"文件预览",
        fileName:fileName,
        filePath:filePath,
        flag:flag,
        param:{}
    }).render();
}