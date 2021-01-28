/***
 * 数学计算公式定制组件弹窗
 * 1、调用方式：<script type="text/javascript" src="/js/study/formulaConfigCompont.js"></script>
 * 2、创建实例：
 * new FormulaConfigCompont({
 *        title: "课程积分规则",
 *        variableDefine:{
 *            a:'课程总评分',
 *            b:'课程评分人数',
 *            c:'课程点赞数',
 *            d:'课程总完课人数',
 *            e:'课程吐槽人数'
 *        },
 *        url:""
 *    }).render();
 *3、参数说明：
 *           title - 弹窗标题
 *  variableDefine - 公式支持的参数变量
 *     requestType - 请求方式
 *             url - 公式及除数集合的请求路径
 *            move - 弹窗拖动
 *         moveOut - 是否可拖动窗口外
 *          zIndex - 设置弹窗层级，默认：10000
 *           shade - 默认是0.3透明度的黑色背景（'#000'）。如果你想定义别的颜色，可以shade: [0.8, '#393D49']；如果你不想显示遮罩，可以shade: 0
 *      shadeClose - 是否点击遮罩层关闭窗口，默认关闭
 *          resize - 弹窗是否可缩放，默认不可缩放
 * successCallback - 提交成功回调函数
 */
function FormulaConfigCompont(config) {
    this.title = config.title || "定制计算公式";
    this.variableDefine = config.variableDefine || {};
    this.url = config.url || "";
    this.param = config.param || {},
    this.requestType = config.requestType || "post";
    this.successCallback = config.successCallback || null;
    this.move = config.move || '.layui-layer-title';
    this.moveOut = config.moveOut || true;
    this.zIndex = config.zIndex || 10;
    this.shade = config.shade || 0;
    this.shadeClose = config.shadeClose || true;
    this.resize = config.resize | false;
    this.modalIndex = null;
    this.pattern = "[^0-9\\.\\+\\-\\*\\/\\(\\)"+Object.keys(config.variableDefine || {}).join()+"]";//公式编辑框动态添加正则表达式校验
    this.content = config.content || "<form id=\"formulaForm\" class=\"layui-form\" style=\"padding-top: 20px;\">\n" +
        "        <div class=\"col-sm-12\">\n" +
        "            <div class=\"form-group col-sm-12\">\n" +
        "                <label class=\"col-sm-2 control-label\">\n" +
        "                    规则变量:\n" +
        "                </label>\n" +
        "                <div class=\"col-sm-10\">\n" +
        "                    <div id=\"propWrap\" style=\"border: 1px solid #eee;width: 100%;height: 65px;padding: 5px 5px;overflow-y: auto;\">\n" +
        "                       <!--<span style=\"border: 1px solid #f3cdcd;display: inline-block;border-radius: 5px;padding: 1px 3px;background-color: #fdecec;float: left;\">x=评论数</span>-->\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "        <div class=\"col-md-12\">\n" +
        "            <div class=\"form-group col-sm-12\">\n" +
        "                <label class=\"col-sm-2 control-label\">\n" +
        "                    编辑公式:\n" +
        "                </label>\n" +
        "                <div class=\"col-sm-10\">\n" +
        "                    <textarea id=\"formula\" name=\"formula\" placeholder=\"如：60 +a/b*0.3+c/d*100*0.1-e/d*100\" class=\"form-control\"></textarea>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "        <div class=\"col-sm-12\">\n" +
        "            <div class=\"form-group col-sm-12\">\n" +
        "                <label class=\"col-sm-2 control-label\">\n" +
        "                    公式除数:\n" +
        "                </label>\n" +
        "                <div class=\"col-sm-10\">\n" +
        "                    <div class=\"divisorShow\" style=\"border: 1px solid #eee;width: 100%;height: 160px;padding: 5px 15px;overflow-y: auto;font-size: 14px;\">\n" +
        "\n" +
        "                    </div>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </form>";

}

FormulaConfigCompont.prototype = {
    constructor: FormulaConfigCompont,
    _this: this,
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
    //校验数学公式是否正确
    checkFormula: function (str) {
        try {
            var tmp = str.replace(/[a-zA-Z]+[0-9]*/g, 10);
            eval(tmp);
            return true;
        } catch(err) {
            return false;
        }
    },
    //获取数学公式中的所有除数，如果有存在/(..)/..情况，也算一个组合除数
    listDivisor:function (str, pattern, divisorArr) {
        var _this = this;
        var arr = str.match(new RegExp(pattern, 'g'));
        if(arr && arr.length > 0){
            $.each(arr, function (i, s) {
                s = s.substring(1); //排除第一个/符号
                divisorArr.push(s); //添加到数组中，可能存在异常公式，后面会进行统一校验处理
                //如果除数是被（）包含的运算，存在/(..)/..情况，则获取单个有效除数，采用堆栈形式
                if(s.indexOf("(") == 0){
                    var stock = []; //当stock.length == 0时，说明（ == ）数量
                    var tmp1 = "";
                    var tmp2 = "";
                    for(var j = 0; i < s.length; j++){
                        if("(" == s[j]){
                            stock.push(s[j]);
                        }
                        if(")" == s[j]){
                            stock.pop();
                        }
                        if(stock.length == 0){ //找到除数
                            tmp1 = s.substring(0, j+1);
                            break;
                        }
                    }
                    divisorArr.push(tmp1);
                    if(tmp1.length < s.length){
                        tmp2 = s.substring(tmp1.length, s.length);
                    }
                    if(tmp1 && tmp1.indexOf("/") != -1){
                        _this.listDivisor(tmp1, pattern, divisorArr);
                    }
                    if(tmp2 && tmp2.indexOf("/") != -1){
                        _this.listDivisor(tmp2, pattern, divisorArr);
                    }
                }
            });
        }
    },
    //渲染除数出来
    renderDivisor:function (layero) {
        var _this = this;
        var divisorArr1 = [];
        var divisorResult = [];
        var formula = $(layero[0]).find("#formula").val();
        var pattern = "\\/\\(.+\\)|\\/[a-zA-z]+[0-9]*";
        if(formula && _this.checkFormula(formula)){
            _this.listDivisor(formula, pattern, divisorArr1); //包含括号
        }else {
            layer.msg("公式语法错误，请检查！", {time: 2000, icon: 5});
            return false;
        }
        if(divisorArr1 && divisorArr1.length > 0){
            $.each(divisorArr1, function (j, s2) {
                if(!divisorResult.contains(s2) && _this.checkFormula(s2)){
                    divisorResult.push(s2);
                }
            });
        }
        //排序，按照字符长度从小到大
        divisorResult.sort(function (a, b) {
            return a.length - b.length;
        });
        $(layero[0]).find(".divisorShow").html(divisorResult.join(",<br/>"));
        return divisorResult;
    },
    //提交按钮锁，防止双击多次提交
    submitLock:true,
    render: function () {
        var _this = this;
        var $parentContent = $("<div></div>");
        $parentContent.html(_this.content);
        _this.modalIndex = layer.open({
            type: 1,
            title: _this.title+'【支持运算符：+,-,*,/,()】',
            content: $parentContent.html(),
            btn: ['保存','取消'],
            area: ['680px', '445px'],
            shade: _this.shade,
            shadeClose: _this.shadeClose,
            resize: _this.resize,
            move: _this.move,
            moveOut: _this.moveOut,
            success: function (layero, index) {
                $(layero[0]).find(".divisorShow").html("");
                if(_this.variableDefine && Object.getOwnPropertyNames(_this.variableDefine).length > 0){
                    var html = "";
                    for(var prop in _this.variableDefine){
                        html += "" +
                            "<span style=\"border: 1px solid #f3cdcd;display: inline-block;border-radius: 5px;padding: 1px 3px;margin-right:5px;margin-top: 5px;background-color: #fdecec;float: left;\">" +
                            prop + "&nbsp;=&nbsp;" + _this.variableDefine[prop] +
                            "</span>\n";
                    }
                    $(layero[0]).find("#propWrap").html(html);
                }
                //判断是否已有传入参数，有的话直接渲染出来，进行编辑
                if(_this.param.settingValue){
                    //赋值
                    $(layero[0]).find("#formula").val(_this.param.settingValue);

                    //解析除数
                    _this.renderDivisor(layero);
                }
                //限制文本框输入的字符，必须满足规则变量
                $(layero[0]).find("#formula").keyup(function () {
                    $(this).val($(this).val().replace(new RegExp(_this.pattern, "g"), ""));
                });
            },
            yes:function (index, layero) {
                //解析除数
                var divisorResult = _this.renderDivisor(layero);
                //如果解析出错，就结束
                if(!divisorResult){
                    return;
                }

                //判断是否修改了
                if(_this.param.settingValue && _this.param.settingValue == $(layero[0]).find("#formula").val()){
                    layer.msg(_this.title + "，未修改数据", {time: 2000, icon: 6});
                    return
                }

                //发送请求
                if(_this.submitLock){
                    _this.submitLock = false;//锁定提交
                    _this.param.settingValue = $(layero[0]).find("#formula").val();//公式
                    _this.param.settingValueList = divisorResult;//除数
                    _this.requestData(JSON.stringify(_this.param), _this.url, _this.requestType, "json", true,function (data) {
                        if(data.code == 200){
                            if(_this.successCallback){
                                data.settingValue = _this.param.settingValue;//公式
                                data.settingValueList = divisorResult;//除数
                                layer.msg(_this.title + "，操作成功", {time: 2000, icon: 6});
                                _this.successCallback(data);
                            }
                            if(_this.modalIndex){
                                layer.close(_this.modalIndex);
                            }else {
                                layer.closeAll();
                            }
                        }else {
                            _this.submitLock = true; //启用提交按钮
                            _this.param.settingValue = '';
                            layer.msg(_this.title + "，" + data.msg, {time: 2000, icon: 5});
                        }
                    },function () {
                        _this.param.settingValue = '';
                        _this.submitLock = true; //启用提交按钮
                    },true);
                }
            }
        });
    }
}