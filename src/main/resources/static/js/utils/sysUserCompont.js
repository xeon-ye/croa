/***
 * 系统数据多选组件（signStr: dept-部门、role-角色、user-用户）
 * 1、调用方式：  <link href="/css/utils/sysUserCompont.css" rel="stylesheet">
                <script src="/js/utils/sysUserCompont.js"></script>
 * 2、创建实例：
 *    new SysUserCompont({
 *      title:title,
 *      url:"/trainCourse/listCourseRange",
 *      param:{signStr:“'user'},
 *      defaultGroupName: "默认分组名称",
 *      dataList: [...],
 *      roleTypeMap: {type:name},
 *      chooseDataList:[...],
 *      zIndex: -98000,
 *      resultCallBack:function (dataList) {
 *          //点击确认按钮处理逻辑
 *      },
 *      endCallBack:function () {
 *          //弹窗关闭处理逻辑
 *      }
 *    });
 *3、参数说明：
 *             title - 弹窗标题
 *  defaultGroupName - 默认分组名称，当分组名称不存在时使用，默认：父级名称
 *             param - 请求数据, signStr属性必须配置，支持：dept-部门、role-角色、user-用户
 *               url - 请求地址
 *       roleTypeMap - 系统角色的类型，针对于角色从js缓存获取，避免每次请求
 *          dataList - 渲染数据，如果不为空，则直接取，不用发送请求（针对于js缓存数据情况，不用每次弹窗发送请求），否则，根据url路径请求
 *    chooseDataList - 已选数据集合，用于复选框选中
 *    resultCallBack - 点击确定按钮，处理函数，结果返回
 *       endCallBack - 弹窗关闭处理逻辑
 *              area - 弹窗大小尺寸，数据类型：Array
 *               btn - 按钮集合，数据类型：Array
 *              move - 弹窗拖动
 *           moveOut - 是否可拖动窗口外
 *            zIndex - 设置弹窗层级，默认：10000
 *             shade - 默认是0.3透明度的黑色背景（'#000'）。如果你想定义别的颜色，可以shade: [0.8, '#393D49']；如果你不想显示遮罩，可以shade: 0
 *        shadeClose - 是否点击遮罩层关闭窗口，默认关闭
 *            resize - 弹窗是否可缩放，默认不可缩放
 */
function SysUserCompont(config) {
    this.title = config.title || "系统数据";
    this.defaultGroupName = config.defaultGroupName || "父级名称";
    this.param = config.param || {};
    this.url = config.url || "";
    this.roleTypeMap = config.roleTypeMap || {}; //系统角色的类型，针对于角色从js缓存获取，避免每次请求
    this.dataList = config.dataList || []; //渲染数据，如果不为空，则直接取，不用发送请求（针对于js缓存数据情况，不用每次弹窗发送请求），否则，根据url路径请求
    this.chooseDataList = config.chooseDataList || []; //已选数据集合
    this.resultCallBack = config.resultCallBack || null; //点击确定按钮，处理函数，结果返回
    this.endCallBack = config.endCallBack || null; //弹窗关闭处理逻辑
    this.zIndex = config.zIndex || 10;
    this.area = config.area || ['62%', '75%'];
    this.btn = config.btn || ['确定','取消'];
    this.shadeClose = config.shadeClose || false;
    this.resize = config.resize || false;
    this.move = config.move || '.layui-layer-title';
    this.moveOut = config.moveOut || true;
    this.content = "<div class=\"sysUserModalContentWrap layui-form\">\n" +
        "        <!--搜索条件-->\n" +
        "        <div class=\"sysUserSearchWrap\">\n" +
        "            <div>\n" +
        "                <input name=\"name\" placeholder=\"请输入关键字搜索\" class=\"form-control\"/>\n" +
        "            </div>\n" +
        "            <div>\n" +
        "                <button class=\"btn btn-primary\" type=\"button\">\n" +
        "                    <i class=\"fa fa-search\"></i>&nbsp;搜索\n" +
        "                </button>\n" +
        "                <input class='allChoose' type=\"checkbox\"  title=\"全选\" lay-skin=\"primary\" lay-filter='allChoose'>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "        <!--搜索内容-->\n" +
        "        <div class=\"sysUserContentWrap\">\n" +
        "        </div>\n" +
        "    </div>";

    this.render();//渲染页面
}

SysUserCompont.prototype = {
    constructor: SysUserCompont,
    _this: this,
    modalIndex: 0,
    dataGroup:{}, //存放列表数据分组，数据格式：{groupId: {groupId:1, groupName:n, group:[{itemValue:val, name:name}]}}
    filterDataList: [], //筛选后的数据，针对于dataList有值
    currentChooseDataList: [], //当前操作数据，点击确定后才会进行赋值
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
    handleData:function (data) {
        var _this = this;
        _this.dataGroup = {}; //重置
        if(data && data.length > 0){
            $.each(data, function (i, map) {
                var groupName = _this.defaultGroupName;
                if(_this.param.signStr == "dept"){
                    groupName = _this.defaultGroupName;
                }else if(_this.param.signStr == "role"){
                    groupName = _this.roleTypeMap[map.groupId] || _this.defaultGroupName;
                }else {
                    groupName = map.groupName || _this.defaultGroupName;
                }
                if(!_this.dataGroup[map.groupId]){
                    _this.dataGroup[map.groupId] = {groupId:map.groupId, groupName:groupName, group:[]};
                }
                if(_this.param.signStr == "dept" && _this.dataGroup[map.id]){
                    _this.dataGroup[map.id].groupName = map.name;
                }
                _this.dataGroup[map.groupId].group.push({itemValue:map.itemValue, name:map.name});
            });
        }
    },
    renderItem:function (layero) {
        var _this = this;
        var groupHtml = "";
        for(var key in _this.dataGroup){
            //没有分组名称的分组数据不进行统计
            if(_this.dataGroup[key].group.length > 0){
                var cls = "";//有效的
                var groupChecked = ""; //是否选中
                /*//如果是部门，并且组名称 == 默认组名称，点击分组全选时，不把分组对应的父级包含返回回去，否则不包含
                if(_this.param.signStr == "dept" && _this.defaultGroupName != _this.dataGroup[key].groupName){
                    cls = "validCheckbox";
                }
                //如果已选列表中包含，则选中
                if(_this.currentChooseDataList && _this.currentChooseDataList.contains(key)){
                    groupChecked = "checked";
                }*/
                groupHtml += "<div class=\"sysUserGroupWrap\">\n" +
                    "           <div class=\"sysUserGroupTitle\" title=\""+_this.dataGroup[key].groupName+"\">\n" +
                    "               <input class='groupCls' type=\"checkbox\" title=\""+_this.dataGroup[key].groupName+"\" value='"+key+"' "+groupChecked+" lay-skin=\"primary\" lay-filter='groupChoose'>\n" +
                    "           </div>\n" +
                    "           <div class=\"sysUserGroupItemWrap\">";
                if(_this.dataGroup[key].group && _this.dataGroup[key].group.length > 0){
                    $.each(_this.dataGroup[key].group, function (z, item) {
                        var itemChecked = "";
                        //如果已选列表中包含，则选中
                        if(_this.currentChooseDataList && _this.currentChooseDataList.contains(item.itemValue)){
                            itemChecked = "checked";
                        }

                        groupHtml += "<div class=\"sysUserContentItem\" title=\""+item.name+"\">\n" +
                            "             <input class='validCheckbox' type=\"checkbox\"  title=\""+item.name+"\" value='"+item.itemValue+"' "+itemChecked+" lay-skin=\"primary\" lay-filter='itemChoose'>\n" +
                            "         </div>";
                    });
                }
                groupHtml += "  </div>\n" +
                    "         </div>";
            }
        }
       $(layero[0]).find(".sysUserContentWrap").html(groupHtml);
    },
    search:function (layero) {
        var _this = this;
        _this.param.name = $(layero[0]).find("input[name='name']").val() || "";
        //如果dataList有值，则不发送请求，否则根据请求
        if(_this.dataList && _this.dataList.length > 0){
            _this.filterDataList = []; //重置过滤后的数据
            $.each(_this.dataList, function (j, item) {
                if((_this.param.name && item.name.match(new RegExp(_this.param.name, 'g')) != null) || !_this.param.name){
                    _this.filterDataList.push(item);
                }
            });
            _this.handleData(_this.filterDataList);
            _this.renderItem(layero);
        }else {
            _this.requestData(_this.param, _this.url, "post", "json", false, function (data) {
                _this.handleData(data);
                _this.renderItem(layero);
            });
        }

        //渲染全选/组按钮是否选中
        _this.syncGroupRender(layero); //渲染分组按钮是否选中
        _this.syncAllRender(layero); //渲染全选按钮是否选中

        //渲染生成的复选框
        layui.use('form', function(){
            var form = layui.form;
            form.render('checkbox');
        });
    },
    handleEvent:function (layero, elem, parentNodeCls) {
        var _this = this;
        var childs = [];
        if(parentNodeCls == "sysUserContentWrap"){
            childs = $(layero[0]).find("."+parentNodeCls).find("input[type='checkbox']");
        }else {
            childs = $(elem).closest("."+parentNodeCls).find(".sysUserGroupItemWrap").find("input[type='checkbox']");
            //父级选中 并且 class含有validCheckbox字符，则添加，否则移除
            if(elem.checked){
                if($(elem).hasClass("validCheckbox") && !_this.currentChooseDataList.contains($(elem).val())){
                    _this.currentChooseDataList.push($(elem).val());
                }
            }else {
                _this.currentChooseDataList.remove($(elem).val());
            }
        }
        childs.each(function(index, item){
            item.checked = elem.checked;
            //选中，则添加，否则移除
            if(item.checked){
                //仅对子项元素操作
                if($(item).hasClass("validCheckbox") && !_this.currentChooseDataList.contains($(item).val())){
                    _this.currentChooseDataList.push($(item).val());
                }
            }else {
                //仅对子项元素操作
                if($(item).hasClass("validCheckbox")){
                    _this.currentChooseDataList.remove($(item).val());
                }
            }
        });
    },
    syncGroupRender:function (layero, elem) {
        var groupList = []; //分组管理
        if(elem){
            groupList.push({
                groupElem: $(elem).closest(".sysUserGroupWrap").find(".groupCls")[0],
                groupItemChilds:$(elem).closest(".sysUserGroupWrap").find(".validCheckbox")
            });
        }else {
            $(layero[0]).find(".groupCls").each(function (xz, groupItem) {
                groupList.push({
                    groupElem: groupItem,
                    groupItemChilds:$(groupItem).closest(".sysUserGroupWrap").find(".validCheckbox")
                });
            });
        }
        if(groupList && groupList.length > 0){
            $.each(groupList, function (z, group) {
                var groupFlag = true;
                //判断子元素，是否全选，全选则全选按钮选中，否则不选中
                for(var kx = 0; kx < group.groupItemChilds.length; kx++){
                    if(!$(group.groupItemChilds[kx])[0].checked){
                        groupFlag = false;
                        break;
                    }
                }
                group.groupElem.checked = groupFlag;
            });
        }
    },
    syncAllRender:function (layero) {
        var allChilds = $(layero[0]).find(".validCheckbox");
        var allFlag = true;
        //判断子元素，是否全选，全选则全选按钮选中，否则不选中
        for(var kx = 0; kx < allChilds.length; kx++){
            if(!$(allChilds[kx])[0].checked){
                allFlag = false;
                break;
            }
        }
        $(layero[0]).find(".allChoose")[0].checked = allFlag;
    },
    render:function () {
        var _this = this;

        //数据拷贝
        _this.currentChooseDataList = [];
        if(_this.chooseDataList && _this.chooseDataList.length > 0){
            $.each(_this.chooseDataList, function (zz, item) {
                _this.currentChooseDataList.push(item);
            });
        }

        _this.modalIndex  = layer.open({
            type: 1,
            title: _this.title,
            zIndex: _this.zIndex,
            content: _this.content,
            btn: _this.btn,
            area: _this.area,
            shadeClose: _this.shadeClose,
            resize: _this.resize,
            move: _this.move,
            moveOut: _this.moveOut,
            success: function(layero, index){
                //按钮点击事件
                $(layero[0]).find("button").click(function () {
                    _this.search(layero);
                });

               _this.search(layero);

               //添加事件监听-顶部全选
               layui.use('form', function(){
                   var form = layui.form;
                   form.on('checkbox(allChoose)', function(data){
                       _this.handleEvent(layero, data.elem, "sysUserContentWrap");
                       form.render('checkbox');
                   });
               });
               //添加事件监听-分组全选
               layui.use('form', function(){
                   var form = layui.form;
                   form.on('checkbox(groupChoose)', function(data){
                       _this.handleEvent(layero, data.elem, "sysUserGroupWrap");
                       _this.syncAllRender(layero); //渲染全选按钮是否选中
                       form.render('checkbox');
                   });
               });
               //添加事件监听-子项选择
               layui.use('form', function(){
                   var form = layui.form;
                   form.on('checkbox(itemChoose)', function(data){
                       //选中，则添加，否则移除
                       if(data.elem.checked){
                           if(!_this.currentChooseDataList.contains($(data.elem).val())){
                               _this.currentChooseDataList.push($(data.elem).val());
                           }
                       }else {
                           _this.currentChooseDataList.remove($(data.elem).val());
                       }
                       _this.syncGroupRender(layero, data.elem); //渲染分组按钮是否选中
                       _this.syncAllRender(layero); //渲染全选按钮是否选中
                       form.render('checkbox');
                   });
               });
            },
            yes: function (index, layero) {
                if(_this.currentChooseDataList && _this.currentChooseDataList.length > 0){
                    if(_this.resultCallBack){
                        _this.resultCallBack(_this.currentChooseDataList);
                    }
                    if(_this.modalIndex){
                        layer.close(_this.modalIndex);
                    }else {
                        layer.close(index);
                    }
                }else {
                    layer.msg("必须选择有效数据！", {time: 2000, icon: 5});
                }
            },
            end: function(){
                //当没有选择值时
                if((!_this.currentChooseDataList || _this.currentChooseDataList.length < 1) && _this.endCallBack){
                    _this.endCallBack();
                }
            }
        });
    }
}