/*
调用方式:
    引包:
    <link href="/static/css/plugins/dropzone/dropzone.css" rel="stylesheet">
    <script src="/static/js/plugins/dropzone/dropzone.js"></script>
    <script src="/static/js/fileUpload.js"></script>

1,创建对象
指定元素id例如  <form style="display: none" class="dropzone" id="test"></form>
设置回调方法
根据自己业务执行对应操作
var fileUpload = new FileUpload({
    targetEl: '#test',
    completeCallback: function (data) {
    // data为数组格式为
    //  [
    //      {
    //          file:服务器路径,
    //          oriName:上传的文件名
    //      },
    //      {
    //          file:服务器路径,
    //          oriName:上传的文件名
    //      }
    //  ]
        alert(JSON.stringify(data));
    },
    acceptSuffix: ['doc'], //非必须，如果不传则不限制上传文件格式
});
fileUpload.upload()
*/
$(function () {
    Dropzone.autoDiscover = false;
});

function FileUpload(config) {
    this.targetEl = config.targetEl;//目标容器
    this.completeCallback = config.completeCallback;//传入上传完成后回调方法,参数为上传成功后的文件路径数组
    this.deleteCallback = config.deleteCallback;//传入删除回调方法,参数为被删除文件的文件路径
    this.multi = config.multi === undefined ? true : config.multi;//默认可多文件上传，如需限制传入false
    this.effectiveFileList = [];//当前成功上传文件数组
    this.acceptSuffix = config.acceptSuffix;//默认不限制上传文件格式,如需要限制 数组形式传入后缀
    this.layerIndex = undefined;//弹出层id
    this.autoClose = config.autoClose === undefined ? true : config.autoClose;//是否上传完成后自动关闭弹出层,可通过obj.layerIndex手动关闭
    this.filePart = config.filePart ? config.filePart : ""; //上传文件目录
    this.requestUrl = config.requestUrl ? config.requestUrl : "/fileUpload?filePart="; //请求路径
    this.dropZone = this.initFileUpload();//初始化dropZone
}

FileUpload.prototype = {
    constructor: FileUpload,
    isAccept: function () {
        return this.dropZone.getRejectedFiles().length === 0;
    },
    filePath: function () {
        return this.effectiveFileList;
    },
    initFileUpload: function () {
        var objThis = this;
        var myDropzone = new Dropzone(objThis.targetEl, {
            url: baseUrl + objThis.requestUrl + objThis.filePart,
            autoProcessQueue: false,
            parallelUploads: 5,
            accept: function (file, done) {
                if (!objThis.acceptSuffix) {
                    done();
                    return;
                }

                var fileName = file.name;
                var suffixIndex = fileName.lastIndexOf(".");
                //处理带后缀的文件
                if (suffixIndex > -1) {
                    var suffix = fileName.substring(suffixIndex + 1);
                    if (!objThis.acceptSuffix.contains(suffix)) {
                        layer.msg("格式不兼容", {
                            icon: 2, time: 1500
                        });
                        done("格式不兼容");
                        return;
                    }
                }
                //限制后缀但是传入的是无后缀文件
                if (suffixIndex === -1 && objThis.acceptSuffix.length > 0) {
                    layer.msg("格式不兼容", {
                        icon: 2, time: 1500
                    });
                    done("格式不兼容");
                    return;
                }
                done();
            }
        });

        myDropzone.on("addedfile", function (file) {
            //单文件模式
            if (!objThis.multi) {
                while (this.files[1]) {
                    this.removeFile(this.files[0]);
                }
            }

            var removeButton = Dropzone.createElement("<button class='btn btn-primary'>删除文件</button>");
            var _this = this;
            removeButton.addEventListener("click", function (e) {
                e.preventDefault();
                e.stopPropagation();
                _this.removeFile(file);
                var fileId = $(this).next().attr('image-id');
                if (!fileId) return;
                objThis.effectiveFileList = objThis.effectiveFileList.filter(function (value) {
                    return value.file !== fileId;
                });

                if (objThis.deleteCallback) objThis.deleteCallback(fileId);
            });
            file.previewElement.appendChild(removeButton);
        });

        myDropzone.on("success", function (file, serverResponse) {
            if (!serverResponse.data.result) {
                layer.msg(serverResponse.msg ? serverResponse.msg : "文件上传失败,请联系客服反馈问题", {
                    icon: 2, time: 1500
                });
                return;
            }
            if (!objThis.multi) {
                objThis.effectiveFileList = [];
            }
            objThis.effectiveFileList.push(serverResponse.data.result[0]);
            file.previewElement.append(Dropzone.createElement('<input type="hidden" image-id="' + serverResponse.data.result[0].file + '"/>'));
        });

        myDropzone.on("queuecomplete", function () {
            //队列中文件上传完毕后回调
            //如果上传队列中还有未上传文件
            if (objThis.dropZone.getQueuedFiles().length > 0) return;
            //全部文件已上传  并且文件类型都正确
            if (objThis.isAccept()) {
                layer.load(2, {time: 1500});
                layer.msg('上传成功', {
                    icon: 6,
                    time: 1500
                }, function () {
                    if (objThis.completeCallback) objThis.completeCallback(objThis.effectiveFileList);
                    if (objThis.autoClose && objThis.isAccept()) {
                        if (objThis.layerIndex) {
                            layer.close(objThis.layerIndex); //如果有则关闭指定窗口
                        } else {
                            layer.closeAll(); //关闭所有窗口
                        }
                    }
                });
            }
        });

        return myDropzone;
    },
    upload: function () {
        var objThis = this;
        //每次重新打开窗口时，清除上一次缓存的文件
        $(objThis.targetEl).find("div").each(function (i,ttt) {
            if($(ttt).hasClass("dz-image-preview") || $(ttt).hasClass("dz-file-preview")){
                $(ttt).find("button").click();
            }
        });
        objThis.layerIndex = layer.open({
            type: 1,
            content: $(objThis.targetEl),
            btn: ['确认上传'],
            area: ['700px', '452px'],
            title: objThis.acceptSuffix ? "上传文件【格式限制:<label style='color: red'>" + objThis.acceptSuffix + "</label>】"
                : "上传文件【<label style='color: red'>不限格式</label>】",
            yes: function () {
                if (objThis.dropZone.getQueuedFiles().length === 0) {
                    layer.msg("请选择文件上传", {
                        icon: 2, time: 1500
                    });
                    return;
                }
                objThis.dropZone.processQueue();
            }
        });
        return objThis.layerIndex;
    }
};