/**
 * 唤醒流程公共方法
 * 1、调用方式：
 *     -> 引包<script type="text/javascript" src="/js/flowable/workupProcessCompont.js"></script>
 *     -> 导入唤醒流程详情模态框<div th:include="flow/historyModal" ></div>, 如果没引入该模态框，可使用系统原有的模态框，但需要设置具体参数
 * 2、创建实例：
 * new WorkupProcessCompont({
        id: row.id,
        processType: 12,
        taskId: row.taskId
    }).render();
 * 3、参数说明：
 *             id - 需要唤醒的业务记录的ID
 *           name - 需要唤醒的业务记录的title
 *    processType - 业务流程类型
 *    processName - 业务流程类型名称
 *          param - 传入参数, 如果该值不为空，则后台需要往对象添加workupRequestParam对象
 *     filterNode - 需要过滤掉的节点
 *         taskId - 完成的业务记录对应表字段taskId值
 *            url - 请求URL，默认"/process/workupProcess"
 *   historyModal - 审核详情弹窗ID，如果没有引入templates/statisticsmodal/historyModal.html文件，则需要设置页面具体值，否则可不指定参数
 *         target - 模态框具体审核详情列表DIV的ID，如果没有引入templates/statisticsmodal/historyModal.html文件，则需要设置页面具体值，否则可不指定参数
 *     startModal - 调起唤醒组件的模态框
 *    searchBtnId - 页面搜索按钮ID，用户发起唤醒后刷新列表
 * 4、如果需要设置弹窗背景颜色，请在对应页面加入样式，例如：
 *    .layui-layer-content{
            overflow: hidden !important;
            background-color: #f7f7f7 !important;
      }
 * */
function WorkupProcessCompont(config) {
    this.id = config.id;
    this.name = config.name;
    this.processType = config.processType;
    this.processName = config.processName;
    this.param = config.param || {},
    this.filterNode = config.filterNode || [],
    this.taskId = config.taskId;
    this.url = config.url || "/process/workupProcess",
    this.historyModal = config.historyModal || '#processHisModal';
    this.target = config.target  || '#processHistory';
    this.startModal = config.startModal || "";
    this.searchBtnId = config.searchBtnId;
    this.currentNodeMap = {}; //当前可选择的节点
    this.firstNodeName = ""; //第一个节点
    this.currentProcessType = 23, //唤醒流程的类型，如果后台同意修改了，需要修改该值
    this.content = '<!--选择唤醒到指定节点-->\n' +
                   '<div id="updateGroupTypeDiv"\n' +
                   '    <div style="margin-top: 20px;margin-left: 20px;">\n' +
                   '        <form id="selectNodeForm" class="form-horizontal" method="post">\n' +
                   '            <!--当前记录ID-->\n' +
                   '            <input type="hidden" name="workupId" id="recordId">\n' +
                   '            <!--当前记录title-->\n' +
                   '            <input type="hidden" name="workupName" id="recordName">\n' +
                   '            <!--唤醒流程类型-->\n' +
                   '            <input type="hidden" name="processType" id="processType">\n' +
                   '            <!--唤醒流程类型名称-->\n' +
                   '            <input type="hidden" name="processName" id="processName">\n' +
                   '            <!--记录任务ID-->\n' +
                   '            <input type="hidden" name="workupTaskId" id="taskId">\n' +
                   '            <!--是否有网关-->\n' +
                   '            <input type="hidden" name="gatewayFlag" id="gatewayFlag">\n' +
                   '            <div class="col-md-12" style="padding-left: 0px;">\n' +
                   '                <label class="col-sm-3 control-label">\n' +
                   '                    选择唤醒节点:\n' +
                   '                </label>\n' +
                   '                <div class="col-sm-9">\n' +
                   '                    <select id="nodeSelect" name="taskDefKey" class="form-control" style="display: inline-block;"></select>\n' +
                   '                </div>\n' +
                   '            </div>\n' +
                   '            <div class="col-md-12" style="padding-left: 0px;margin-top: 10px;">\n' +
                   '                <label class="col-sm-3 control-label">\n' +
                   '                    <span class="text-red">*</span>唤醒原因:\n' +
                   '                </label>\n' +
                   '                <div class="col-sm-9 layui-form">\n' +
                   '                    <textarea id="workupReason" name="workupReason" placeholder="请输入唤醒原因" class="layui-textarea" required style="height: 50px;"></textarea>\n' +
                   '                </div>\n' +
                   '            </div>\n' +
                   '        </form>\n' +
                   '    </div>\n' +
                   '</div>';
}

WorkupProcessCompont.prototype = {
    constructor: WorkupProcessCompont,
    _this: this,
    requestData: function (data, url, requestType,dataType,async,callBackFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            success: callBackFun
        };
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },
    render: function () {
        var _this = this;
        var $parentContent = $("<div></div>");
        $parentContent.html(_this.content);
        $parentContent.find("#recordId").val(_this.id);
        $parentContent.find("#recordName").val(_this.name);
        $parentContent.find("#processType").val(_this.processType);
        $parentContent.find("#processName").val(_this.processName);
        $parentContent.find("#taskId").val(_this.taskId);

        //设置下拉列表值
        _this.requestData(null, "/process/getProcessTaskDefKey/"+_this.taskId, "get", "json", false, function (data) {
            if(data.data.result.length > 0){
               var $select = $parentContent.find("#nodeSelect");
               var gatewayFlag = false; //默认没有流程网关
               $select.empty();
               //如果有禁止选择所有节点标识
               if(_this.filterNode.contains("AllNode")){
                   $select.append('<option value="">-金额改动较大，无法选择节点-</option>');
               }else{
                   $select.append('<option value="">--请选择节点--</option>');
               }
               $.each(data.data.result, function (i, taksDefData) {
                    if(taksDefData.type == "userTask" && !_this.filterNode.contains(taksDefData.taskDefKey)){
                        //排除掉第一个节点（组长/部长） 并且  不包含排除所有标识
                        if(_this.firstNodeName && !_this.filterNode.contains("AllNode")){
                            $select.append('<option value="'+taksDefData.taskDefKey+'">'+taksDefData.taskName+'</option>');
                        }
                        if(!_this.firstNodeName){
                            _this.firstNodeName = taksDefData.taskName;
                        }
                        _this.currentNodeMap[taksDefData.taskDefKey] = taksDefData.taskName;

                    }
                    if(!gatewayFlag && taksDefData.type == "exclusiveGateway"){
                        gatewayFlag = true; //只要设置了，就不需要再判断了
                    }
               });
                $parentContent.find("#gatewayFlag").val(gatewayFlag);
            }
        });
        var addFlag = true;
        layer.open({
            type: 1,
            title: "流程唤醒",
            content: $parentContent.html(),
            btn: ['保存'],
            area: ['510px', '290px'],
            zIndex: 10000,
            yes: function (t) {
                if(!$("#selectNodeForm").valid()) return;
                if(addFlag){
                    addFlag = false; //防止多次点击触发请求
                    var param = $("#selectNodeForm").serializeJson();
                    _this.param["workupReason"] = param["workupReason"] || "无"; //记录唤醒原因
                    if(!param.taskDefKey){
                        //如果有组长审核节点，并且没有选择组长，则提示错误
                        if(_this.param.groupLeaderFlag == 1 && !_this.param.mediaGroupLeader){
                            layer.closeAll();
                            swal("请选择组长审核人！");
                            return;
                        }
                        param.taskDefKey = "";
                        _this.param.jumpNode = _this.firstNodeName;
                    }else {
                        if((_this.param.groupLeaderFlag == 1 && !_this.param.mediaGroupLeader) || (param.taskDefKey == 'groupLeaderApprove' && !_this.param.mediaGroupLeader)){
                            layer.closeAll();
                            swal("请选择组长审核人！");
                            return;
                        }
                        _this.param.jumpNode = _this.currentNodeMap[param.taskDefKey];
                    }
                    delete _this.param.groupLeaderFlag; //删除该标识，后台不使用
                    if(_this.param && Object.getOwnPropertyNames(_this.param).length > 0){
                        _this.param['editJson'] = JSON.stringify(_this.param);
                        _this.param['workupRequestParam'] = {};
                        for(var key in param){
                            _this.param.workupRequestParam[key] = param[key];
                        }
                    }else {
                        _this.param = Object.assign(_this.param,param);
                    }
                    _this.requestData(JSON.stringify(_this.param), _this.url, "post","json",true,function (data) {
                        swal({
                            title: data.code == 200 ? "成功!" : "失败",
                            text: data.code == 200 ? "唤醒流程已启动！" : data.msg,
                            type: data.code == 200 ? "success" : "error",
                            html: true
                        });
                        if(data.code == 200){
                            layer.closeAll();
                            if(_this.startModal){
                                $(_this.startModal).modal("toggle");
                            }
                            if(_this.searchBtnId){
                                $(_this.searchBtnId).click();//刷新页面列表
                            }
                        }else{
                            addFlag = true; //出错了，继续可以点击
                        }
                    },true);
                }

            }
        });

    },
    showHistory: function () {
        var _this = this;
        $(_this.historyModal).modal('toggle');
        _this.requestData({dataId: _this.id, process: _this.currentProcessType}, "/process/history", "post","json", true,function (data) {
            if (data.code == 200) {
                $(_this.target).empty();
                if (data.data.data != null) {
                    var html = "";
                    html += "<div style='position: relative;z-index: 10;'><div class='form-control'>" +
                        "<div class='col-sm-3 text-center'>审核节点</div>" +
                        "<div class='col-sm-3 text-center'>操作人</div>" +
                        "<div class='col-sm-3 text-center'>操作详情</div>" +
                        "<div class='col-sm-3 text-center'>操作时间</div></div>";
                    for (var i = 0; i < data.data.data.length; i++) {
                        html += "<div class='form-control'>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].name + "</div>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].user + "</div>" +
                            "<div class='col-sm-3 text-center' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'>" + data.data.data[i].desc + "</div>" +
                            "<div class='col-sm-3 text-center'>" + data.data.data[i].time + "</div>" +
                            "</div>";
                    }
                    html += "</div><div class='col-sm-12 text-center' style='position:relative'>" +
                        "<img src='/process/getImage?dataId=" + _this.id + "&process="+_this.currentProcessType+"&t=" + new Date().getTime() + "' " +
                        "style='width: 105%; margin-top: -90px;margin-bottom: -130px;'/>" +
                        "</div>";
                    $(_this.target).append(html);
                }
            } else {
                if (getResCode(data))
                    return;
            }
        });
    }
};