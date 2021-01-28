//缓存系统资源键值对
var sysResourceMap = {};
(function ($) {
    $.fn.extend({
        /**
         * serializeJson通用序列化方法，会把value为空的字段筛掉
         */
        serializeJson: function () {
            var o = {};
            $.each(this.serializeArray(), function () {
                if (this.value) {
                    if (o[this.name]) {
                        if (!o[this.name].push) {
                            o[this.name] = [o[this.name]];
                        }
                        o[this.name].push(this.value || '');
                    } else {
                        o[this.name] = this.value || '';
                    }
                }
            });
            return o;
        },
        /**
         * serializeForm通用序列化方法，会把value为空的字段保留
         */
        serializeForm: function () {
            var o = {};
            $.each(this.serializeArray(), function () {
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
        },
        toJSON: function () {
            var json = {};
            $.each(this.serializeArray(), function () {
                var name = this.name;
                var value = this.value;
                if (value && value != 0) {
                    var paths = this.name.split(".");
                    var len = paths.length;
                    var obj = json;
                    $.each(paths, function (i, e) {
                        if (i == len - 1) {
                            if (obj[e]) {
                                if (!obj[e].push) {
                                    obj[e] = [obj[e]];
                                }
                                obj[e].push(value || '');
                            } else {
                                obj[e] = value || '';
                            }
                        } else {
                            if (!obj[e]) {
                                obj[e] = {};
                            }
                        }
                        obj = json[e];
                    });
                }
            });
            return json;
        },
        //搜索前清空jqGrid参数
        emptyGridParam: function () {
            $(this).jqGrid('clearGridData');  //清空表格  数据
            var postData = $(this).jqGrid("getGridParam", "postData");
            if (postData != null && postData != "") {
                $.each(postData, function (k, v) {
                    delete postData[k];
                });
            }
        },
        /**
         * 刷新当前页数据
         * @param url 请求地址
         * @param data 数据
         * @param datatype 数据类型
         */
        reloadCurrentData: reloadCurrentData
    });
    $.jgrid.extend({
        /**
         * 刷新当前页数据
         * @param url 请求地址
         * @param data 数据
         * @param datatype 数据类型
         */
        reloadCurrentData: reloadCurrentData

    });

    //jqgrid表格默认单元格点击功能，重写会覆盖掉
    $.jgrid.defaults.onCellSelect = function (rowid,iCol,cellcontent,e){
        var $cellCopyBtn = $(window.parent.document.getElementById('cellCopyBtn'));
        var copyFlag = $cellCopyBtn.attr("cellCopyFlag"); //导航栏拷贝标识值，1-拷贝、0-不拷贝
        if(copyFlag == 1){
            var $input = $("<input>");
            var text = e.target.innerText || "";
            if(text){  //每次拷贝完默认点击不会拷贝，不然会增加内存负担
                $input.val(text.trim().replace(/&nbsp;/ig,' '));
                $(e.target).append($input)
                $input[0].select();
                document.execCommand("copy"); // 执行浏览器复制命令
                $input.remove();
                $cellCopyBtn.attr("cellCopyFlag", "0"); //设置不拷贝
                $cellCopyBtn.parent().css("background-color","#fff");  //拷贝按钮背景颜色还原
            }
        }
    }

    //缓存系统资源键值对
    if(sysResourceMap && Object.getOwnPropertyNames(sysResourceMap).length < 1){
        $.ajax({
            type: 'get',
            url: "/resource/list",
            data: null,
            dataType: 'json',
            async: false,
            success: function (data) {
                if(data && data.length > 0){
                    $.each(data, function (ii, resource) {
                        //缓存有url的资源
                        if(resource.url){
                            sysResourceMap[resource.url] = resource.name;
                        }
                    });
                }
            }
        });
    }

    //处理键盘事件，由于火狐和360浏览器页面直接backspace键会存在退出系统问题
    function doKey(e) {
        var ev = e || window.event; //获取event对象
        var obj = ev.target || ev.srcElement; //获取事件源
        var tagName = (obj.tagName || "").toLocaleLowerCase();//获取事件元素，如果是body则不允许退出系统
        if (ev.keyCode == 8 && tagName == "body") {
            return false;
        }
    }

    //禁止后退键 作用于IE、Chrome、Firefox、Opera
    document.onkeydown = doKey;

})(jQuery);

/**
 * 刷新当前页数据
 * @param url 请求地址
 * @param data 数据
 * @param datatype 数据类型
 * @param loadComplete 回调函数
 * @param gridComplete 回调函数
 */
function reloadCurrentData(url, data, dataType, loadComplete, gridComplete) {
    var page = $(this).jqGrid('getGridParam', 'page');//获取当前页
    var params = {
        page: page
    }
    if (url != undefined && url != '')
        params['url'] = baseUrl + url;
    if (dataType != undefined && dataType != '')
        params['dataType'] = dataType;
    if (data != undefined && data != '')
        params['postData'] = data;
    if (loadComplete != undefined && loadComplete != '')
        params['loadComplete'] = loadComplete;
    if (gridComplete != undefined && gridComplete != '')
        params['gridComplete'] = gridComplete;
    $(this).jqGrid('setGridParam', params).trigger("reloadGrid");
}

function page1(url, title) {
    var tabs = $('#page-tabs-content', parent.document);
    var main = $('#content-main', parent.document);
    if (tabs == null || tabs == undefined || tabs.length == 0) {
        location.href = url;
    } else {
        //在xml或html中，&会被转成&amp;
        if (main.html().replace(/&amp;/g, "&").indexOf(url) < 0) {
            tabs.find("a").each(function (i, d) {
                $(d).removeClass("active");
            });
            main.find("iframe").each(function (i, d) {
                $(d).hide();
            });
            if ($(tabs).html().indexOf(url) < 0) {
                var tab = $("<a href='javascript:;' class='active J_menuTab' data-id='" + url + "'>" + title + " <i class=\"fa fa-times-circle\"></i></a>");
                tabs.append(tab);
            } else {
                tabs.find("a").each(function (i, d) {
                    if ($(this).attr("data-id") == url) {
                        $(this).addClass("active");
                    }
                });
            }
            var frame = '<iframe class="J_iframe" name="iframe0" width="100%" height="100%" src="' + url + '" frameborder="0" data-id="' + url + '" seamless="seamless"></iframe>';
            main.append(frame);
        } else {
            swal("该链接已经开启一个窗口，请关闭后再打开！")
        }
    }
}

//系统跳转优化（禅道bug1715）：
//1、历史逻辑见上面page1方法
//2、配合static/js/contabs.js 中 menuItem方法（菜单点击事件）修改，对于menuItem中逻辑，iframe链接不同则刷新链接
//3、对于新的page方法，每次调用时，iframe链接进行刷新，tab 和 iframe 中的属性data-id采用不带参数的url与菜单保持一致，这样才能和菜单点击事件menuItem逻辑呼应
//4、tab的标题与资源标题保持一致，不能有特殊的
function page(url, title) {
    var tabs = $('#page-tabs-content', parent.document);
    var main = $('#content-main', parent.document);
    if (tabs == null || tabs == undefined || tabs.length == 0) {
        location.href = url;
    } else {
        var tmpUrl = url;//获取路径，排除参数
        //如果链接有参数，则移除参数
        if(url.indexOf("?") > 0){
            tmpUrl = url.substring(0, url.indexOf("?"));//获取路径，排除参数
        }
        var existsTabFlag = false;//默认不存在TAB
        tabs.find("a").each(function (i, d) {
            if ($(this).attr("data-id") == tmpUrl) {
                existsTabFlag = true;//存在Tab
            }
        });

        //判断是否存在当前Tab页，存在则内容替换，不存在则新增
        if(existsTabFlag){
            //激活当前存在的TAB，其他兄弟节点隐藏
            tabs.find("a").each(function (i, d) {
                if ($(this).attr("data-id") == tmpUrl) {
                    $(this).addClass("active").siblings('.J_menuTab').removeClass('active');;
                }
            });
            //当前iframe显示，其他兄弟节点隐藏，刷新iframe的链接为最新链接
            main.find("iframe").each(function (ii, iframe) {
                if ($(this).data('id') == tmpUrl) {
                    $(this).attr("src", url);//更新最新链接
                    $(this).show().siblings('.J_iframe').hide();
                }
            });
        }else {
            //将当前所有的Tab页面隐藏
            tabs.find("a").each(function (i, d) {
                $(d).removeClass("active");
            });
            main.find("iframe").each(function (i, d) {
                $(d).hide();
            });

            //如果有系统资源，使用资源的标题作为tab标题
            if(sysResourceMap && Object.getOwnPropertyNames(sysResourceMap).length > 0){
                title = sysResourceMap[tmpUrl] || title;
            }

            var tab = $("<a href='javascript:;' class='active J_menuTab' data-id='" + tmpUrl + "'>" + title + " <i class=\"fa fa-times-circle\"></i></a>");
            tabs.append(tab);
            var frame = '<iframe class="J_iframe" name="iframe0" width="100%" height="100%" src="' + url + '" frameborder="0" data-id="' + tmpUrl + '" seamless="seamless"></iframe>';
            main.append(frame);
        }
    }
}

/**
 * 刷新指定页面
 * @param url
 */
function refrechPage(url) {
    // alert(url);
    var main = $('#content-main', parent.document);
    main.find("iframe").each(function (i, d) {
        if ($(d).attr("src") == url) {
            $(d).attr("src", url);
        }
    });
}

Number.prototype.toMoney = function () {
    var num = this;
    num = num.toFixed(2);
    num = parseFloat(num);
    num = num.toLocaleString();
    return num;//返回的是字符串23,245.12保留2位小数
}
Date.prototype.format = function (fmt) {
    if (this == undefined || this == null) {
        console.error("时间出错");
        return;
    }
    var o = {
        "M+": this.getMonth() + 1,                 //月份
        "d+": this.getDate(),                    //日
        "h+": this.getHours(),                   //小时
        "m+": this.getMinutes(),                 //分
        "s+": this.getSeconds(),                 //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds()             //毫秒
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
}
Array.prototype.contains = function (needle) {
    for (i in this) {
        if (this[i] == needle) return true;
    }
    return false;
}
String.prototype.th = function (old, d) {
    return this.replace(new RegExp("\\${" + old + "}", "g"), d);
}

String.prototype.firstUpper = function () {
    return this.replace(/\b\w+\b/g, function (word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    });
}

String.prototype.checkHtml = function () {
    var reg = /<[^>]+>/g;
    return reg.test(this);

}

$.getUrlParam = function (name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r == null) {
        r = parent.location.search.substr(1).match(reg);
    }
    if (r != null) return unescape(r[2]);
    return null;
}

function upload(t, fileId) {
    var value = $(t).val();
    if (value != null && value != undefined && value != '') {
        var formData = new FormData();
        var image = $(t).get(0).files[0];
        formData.append("image", image);
        $.ajax({
            url: baseUrl + "/upload",
            data: formData,
            cache: false,
            processData: false,
            contentType: false,
            type: "post",
            success: function (data) {
                // $("#headimgThum").attr("src", data.data.src);
                $(t).next(".uploader").html("<img src='" + data.data.src + "' width='200px' height='120px'/>");
                $(t).next(".uploader").css("padding-top", 0);
                $("#" + fileId).val(data.data.src);
            }
        });
    }
}

/**
 * 只可以输入数字 包括小数点
 * @param e
 * @returns {boolean}
 */
function inNum(evt) {
    evt = (evt) ? evt : ((window.event) ? window.event : "");
    var curKey = evt.keyCode ? evt.keyCode : evt.which;
    return curKey >= 46 && curKey < 58;
}

/**
 * 只可以输入金额 包括小数点
 * @param e
 * @returns {boolean}
 */
function inPrice(evt) {
    evt = (evt) ? evt : ((window.event) ? window.event : "");
    var curKey = evt.keyCode ? evt.keyCode : evt.which;
    return curKey >= 46 && curKey < 58;
}

function msgCenter() {
    // $("#msgPlan").modal("show");
}

// 解决四维运算,js计算失去精度的问题

/**
 * 加法
 * @param arg
 * @returns {number}
 */
Number.prototype.add = function (arg) {
    var r1, r2, m;
    try {
        r1 = this.toString().split(".")[1].length
    } catch (e) {
        r1 = 0
    }
    try {
        r2 = arg.toString().split(".")[1].length
    } catch (e) {
        r2 = 0
    }
    m = Math.pow(10, Math.max(r1, r2))
    return (this * m + arg * m) / m
}
/**
 * 减法
 * @param arg
 * @returns {*}
 */
Number.prototype.sub = function (arg) {
    return this.add(-arg);
}
/**
 * 乘法
 * @param arg
 * @returns {number}
 */
Number.prototype.mul = function (arg) {
    var m = 0, s1 = this.toString(), s2 = arg.toString();
    try {
        m += s1.split(".")[1].length
    } catch (e) {
    }
    try {
        m += s2.split(".")[1].length
    } catch (e) {
    }
    return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m)
}
/**
 * 除法
 * @param arg
 * @returns {number}
 */
Number.prototype.div = function (arg) {
    var t1 = 0, t2 = 0, r1, r2;
    try {
        t1 = this.toString().split(".")[1].length
    } catch (e) {
    }
    try {
        t2 = arg.toString().split(".")[1].length
    } catch (e) {
    }
    with (Math) {
        r1 = Number(this.toString().replace(".", ""))
        r2 = Number(arg.toString().replace(".", ""))
        return (r1 / r2) * pow(10, t2 - t1);
    }
}

/**
 * 触发指定Tab页面，指定按钮的点击事件
 * @param url 页面URL
 * @param buttonId 按钮ID
 */
function triggerPageBtnClick(url, buttonId) {
    var main = $('#content-main', parent.document);
    main.find("iframe").each(function (i, d) {
        if ($(d).attr("src") == url) {
            $(d).contents().find('#' + buttonId).click();
        }
    });
}
Number.prototype.fmtMoney=fmtMoney;

String.prototype.fmtMoney=fmtMoney;

function fmtMoney() {
    var num=this;
    var n = parseFloat(num).toFixed(2);
    var re = /(\d{1,3})(?=(\d{3})+(?:\.))/g;
    return n.replace(re, "$1,");
}

function fmtMoneyBringUnit(count,unit) {
    var fmtCount = count.fmtMoney();
    if(!unit){
        return fmtCount + "元";
    }
    return fmtCount.substring(0,fmtCount.length - 3) + unit;
}

// 提示信息增加遮罩层；
function shadeMessage(content, time) {
    if (!time) time = 10000;
    return layer.msg(content, {time: time, shade: [0.7, '#393D49']});
}

function closeShade(index) {
    if (index)
        layer.close(index);
    var shade = $('.layui-layer-shade');
    if (shade)
        shade.remove();
}