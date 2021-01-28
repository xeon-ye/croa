/**
 * 流程审核，；
 * @param taskId：任务ID；
 * @param flag：是否同意，true为同意，false为拒绝；
 * @param elementId：按钮的ID；
 * @param desc：审核备注；
 * @param afterApproveTask：回调函数；
 */
function approveTask(taskId, flag, elementId, desc, afterApproveTask) {
    layer.msg("系统处理中，请稍候。");
    startModal("#" + elementId);
    $.post(baseUrl + "/process/apply", {taskIds: taskId, agree: flag, desc: desc}, function (data) {
        Ladda.stopAll();
        // console.log(JSON.stringify(data))
        if (data.data.message == null) {
            getResCode(data);
        } else {
            layer.msg(data.data.message);
        }

        // 回调定义的函数；
        if (afterApproveTask) {
            var result =  afterApproveTask();
            // 返回值为1时，代表不需要跳转页面，不走下面的方法
            if(result && result == 1){
                return;
            }
        }

        // 返回审核页面；
        setTimeout(function () {
            // 审核后刷新首页；
            var returnType= getQueryString("returnType");
            if(returnType==3){
                // refrechPage('/process/queryTask');
                triggerPageBtnClick("/process/queryTask","dataSearch");
                closeCurrentTab();
            }else if(returnType == 4){
                refrechPage("/homePage");
                closeCurrentTab();
            }
            else {
                refrechPage("/workbench/workbench");
                closeCurrentTab();

            }
            // 如果打开了审批任务列表页，则跳转至该页面，否则关闭标签；
            // var url = "/process/queryTask";
            // var main = $('#content-main', parent.document).html();
            // // 在xml或html中，&会被转成&amp；
            // if (!main || (main.replace(/&amp;/g, "&").indexOf(url) >= 0)) {
            //     window.location.href = url;
            // } else {
            //     closeCurrentTab();
            // }
        }, 1000);
    }, "json").error(function () {
        Ladda.stopAll();//隐藏加载按钮
    });
}

/**
 * 流程审核（可设置下一个网关值）；
 * @param taskId：任务ID；
 * @param flag：是否同意，true为同意，false为拒绝；
 * @param elementId：按钮的ID；
 * @param nextGatewayValue：下一个网关值
 * @param desc：审核备注；
 * @param afterApproveTask：回调函数；
 */
function approveTask1(taskId, flag, elementId, nextGatewayValue,  desc, afterApproveTask) {
    layer.msg("系统处理中，请稍候。");
    startModal("#" + elementId);
    $.post(baseUrl + "/process/apply1", {taskIds: taskId, agree: flag, desc: desc, nextGatewayValue: nextGatewayValue}, function (data) {
        Ladda.stopAll();
        // console.log(JSON.stringify(data))
        if (data.data.message == null) {
            getResCode(data);
        } else {
            layer.msg(data.data.message);
        }

        // 回调定义的函数；
        if (afterApproveTask) {
            afterApproveTask();
        }

        // 返回审核页面；
        setTimeout(function () {
            // 审核后刷新首页；
            var returnType= getQueryString("returnType");
            if(returnType==3){
                // refrechPage('/process/queryTask');
                triggerPageBtnClick("/process/queryTask","dataSearch");
                closeCurrentTab();
            }else {
                refrechPage("/workbench/workbench");
                closeCurrentTab();

            }
            // 如果打开了审批任务列表页，则跳转至该页面，否则关闭标签；
            // var url = "/process/queryTask";
            // var main = $('#content-main', parent.document).html();
            // // 在xml或html中，&会被转成&amp；
            // if (!main || (main.replace(/&amp;/g, "&").indexOf(url) >= 0)) {
            //     window.location.href = url;
            // } else {
            //     closeCurrentTab();
            // }
        }, 1000);
    }, "json").error(function () {
        Ladda.stopAll();//隐藏加载按钮
    });
}