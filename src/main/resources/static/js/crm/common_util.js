function getDateStr(dateSelect) {
    var dStr = "";
    switch (dateSelect) {
        case '4':
            dStr = '日';
            break;
        case '3':
            dStr = '月';
            break;
        case '1':
            dStr = '年';
            break;
    }
    return dStr;
}

$(document).ready(function () {
    //layer.open在不同分辨率下显示不居中的问题
    var f = function (obj) {
        if (obj.area) {
            var w = "";
            var h = "";
            if (obj.area[0].indexOf("%") != -1) {
                w = parseFloat(obj.area[0]) * window.screen.width / 100;
            } else {
                w = parseFloat(obj.area[0]);
            }
            if (obj.area[1].indexOf("%") != -1) {
                h = parseFloat(obj.area[1]) * window.screen.height / 100;
            } else {
                h = parseFloat(obj.area[1]);
            }
            var ow = (window.screen.width - w) / 2;
            var oh = (window.screen.height - h) / 2;
            obj.offset = [oh, ow];
        }
    };
    var tOpen = layer.open;
    var pOpen = parent.layer.open;
    layer.open = function (obj) {
        // f(obj);
        tOpen(obj);
    };
    parent.layer.open = function (obj) {
        // f(obj);
        pOpen(obj);
    }
});

//得到查询参数
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return decodeURIComponent(r[2]);
    return null;
}

//统一加上权限，是否登录验证，baseURL
var ajax = $.ajax;
$.ajax = function (obj) {
    if (obj.data) {
        for (var k in obj.data) {
            if (typeof (obj.data[k]) == "string") {
                //提交参数时去除前后空格
                obj.data[k] = obj.data[k].replace(/(^\s*)|(\s*$)/g, "");
            }
        }
    }
    var succ = obj.success;
    // var realUrl = "/"+obj.url;
    // obj.url = realUrl;
    obj.success = function (resData) {
        if (getResCode(resData)) {
            return;
        }
        succ(resData);
    };
    ajax(obj);
};

//给所有创建的echarts对象设置窗口自适应
if (typeof (echarts) != "undefined") {
    var echartsInit = echarts.init;
    var events = {};
    echarts.init = function (ele) {
        var chartObj = echartsInit(ele);
        if (events[ele]) {
            window.removeEventListener("resize", events[ele]);
        }
        events[ele] = chartObj.resize;
        window.addEventListener("resize", chartObj.resize);
        return chartObj;
    };
}