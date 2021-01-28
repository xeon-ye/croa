var downLoad = function (imgArray) {
    this.arr = [];
    this.imgCount = 0;
    this.imgArray = imgArray;
    this.imgLength = imgArray.length;
    this.transfer = 0;
}
downLoad.prototype.getImg = function () {
    console.log(this.imgArray)
    var imgArray = this.imgArray;
    for (var i = 0; i < imgArray.length; i++) {
        var src = imgArray[i].src;
        imgArray[i].setAttribute('crossOrigin', 'anonymous');
        this.requestImg(imgArray[i], src);
    }
}
downLoad.prototype.requestImg = function (img, src) {
    var _this = this
    var src = src;
    img.src = src;
    img.onerror = function () {
        var timeStamp = +new Date();
        _this.requestImg(img, src + '?' + timeStamp);
    }
    img.onload = function () {
        _this.imgCount++;
    }
}
downLoad.prototype.getBase64Image = function (image, ext) {
    var canvas = document.createElement("canvas");
    canvas.width = image.width;
    canvas.height = image.height;
    var context = canvas.getContext("2d");
    context.drawImage(image, 0, 0, image.width, image.height);
    var base64 = canvas.toDataURL("image/" + ext);
    return base64;
}
downLoad.prototype.init = function (dom, target, fileName, paperSize) {
    var _this = this;
    var targetId = targetId || 'content';
    var fileName = fileName || 'content';
    var paperSize = paperSize || 'a4';
    this.getImg();
    dom.onclick = function () {
        $("#print>table>tbody>tr:nth-child(1)>td:nth-child(2)").attr("colspan", "2");
        $("#print>table>tbody>tr:nth-child(1)>td:nth-child(3)").show();
        $("#print>table>tbody>tr:nth-child(2)>td:nth-child(2)").attr("colspan", "2");
        $("#print>table>tbody>tr:nth-child(3)>td:nth-child(2)").attr("colspan", "2");
        $("#print>table>tbody>tr:nth-child(4)>td:nth-child(2)").attr("colspan", "2");
        document.body.scrollTop = document.body.scrollHeight - screen.height;
        document.body.scrollTop = 0;
        try {
            //$('.test-lists').find('.test-lists-bottom,.test-lists-sy,.test-lists-xy,.test-lists-score,.test-lists-remove').hide();
        } catch (e) {
            console.log('涓嶆槸鍐呭');
        }
        target.style.background = 'white';
        if (_this.imgCount == _this.imgArray.length) {
            if (_this.transfer == 0) {
                for (var i = 0; i < _this.imgArray.length; i++) {
                    _this.arr.push(_this.getBase64Image(_this.imgArray[i], 'png'))
                }
                for (var i = 0; i < _this.arr.length; i++) {
                    _this.imgArray[i].src = _this.arr[i];
                }
                _this.transfer = 1;
            }
            renderPDF(target, fileName, paperSize, function () {
                _this.arr = [];
                try {
                    location.reload();
                    //杞崲鎴愬姛鍚庤繕鍘熼殣钘忕殑鍐呭
                    //$('.test-lists').find('.test-lists-bottom,.test-lists-sy,.test-lists-xy,.test-lists-score,.test-lists-remove').show();
                } catch (e) {
                    console.log('涓嶆槸鍐呭');
                }
            });
        }
    }
}