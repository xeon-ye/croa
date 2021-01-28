function RollbackCompont(config) {
    this.target = config.target;
    this.modal = config.modal;
    this.completeCallback = config.completeCallback;
    this.taskId = config.taskId;
    this.btnName = config.btnName || "驳回选择节点";
    this.title = config.title || "驳回成功";
    this.hideFlag = config.hideFlag || false;
    this.refrechPageFlag = config.refrechPageFlag || false;
}

RollbackCompont.prototype = {
    constructor: RollbackCompont,
    _this: this,
    render: function () {
        var _this = this;
        var $target = $(_this.target);
        //如果隐藏，则不请求
        if(_this.hideFlag){
            $target.html("");
            return;
        }

        var selectDomDiv = $('<div class="col-sm-8"></div>');
        var selectDom = $('<select class="form-control height18"></select>');
        $.get(baseUrl + "/process/taskDefKey?taskId=" + _this.taskId, function (result) {
            var data = result.data.result;
            if (!data) return;
            // if (data.length === 0) $target.hide();

            data = data.map(function (item) {
                return "<option data-ownerId='"+item.ownerId+"' value='" + item.processKey + "'>" + item.processName + "-" + item.owner + "</option>";
            });
            data += "<option value='start'>发起人</option>";
            selectDom.html(data);
        }, "json");
        selectDomDiv.html(selectDom);

        var buttonDomDiv = $('<div class="col-sm-4"></div>');
        var buttonDom = $('<button type="button" class="btn btn-warning">'+_this.btnName+'</button>');
        buttonDom.on("click", function () {
            var targetNode = $target.find('select').val();
            var userId = $target.find("select").find("option:selected").attr("data-ownerId");
            userId = userId ? userId : "";
            $.get(baseUrl + "/process/rollback?taskId=" + _this.taskId + "&target=" + targetNode + "&nextUserId="+userId, function (result) {
                if (result.code === 200) {
                    swal({
                        title: _this.title,
                        type: "success"
                    });
                    $(_this.modal).modal('hide');
                    if(_this.refrechPageFlag){
                        setTimeout(function () {
                            refrechPage("/homePage");
                            closeCurrentTab();
                        }, 2000);
                    }
                } else {
                    swal({
                        title: result.msg,
                        type: "error"
                    });
                    if(_this.refrechPageFlag){
                        setTimeout(function () {
                            refrechPage("/homePage");
                            closeCurrentTab();
                        }, 3000);
                    }
                }
            }, "json");
        });
        buttonDomDiv.html(buttonDom);

        $target.append(selectDomDiv);
        $target.append(buttonDomDiv);
    }
};