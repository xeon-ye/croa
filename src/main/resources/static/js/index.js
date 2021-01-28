function startTime() {
    var today = new Date();
    var y = today.getFullYear();
    var M = today.getMonth() + 1;
    var d = today.getDate();
    var w = today.getDay();
    var h = today.getHours();
    var m = today.getMinutes();
    var s = today.getSeconds();
    var week = ['星期天', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
    // add a zero in front of numbers<10
    m = checkTime(m);
    s = checkTime(s);
    $('#DateTime').html(y + '年' + M + '月' + d + "日 " + h + ':' + m + ':' + s + ' <span class="text-warning">' + week[w] + '</span>');//可改变格式
    t = setTimeout(startTime, 1000);

    function checkTime(i) {
        if (i < 10) {
            i = "0" + i;
        }
        return i;
    }
}
var childMenuList = [];//所有菜单列表，子菜单为空
var childMenuMap = {};//所有菜单列表，子菜单为空
var menuMap = {};//缓存节点字段,含子节点
var sysConfigMap = {};//系统配置功能
var configObj={
    warehouseList:{},//仓库数据data
    warehouseData:{},//仓库数据（id，name）
    wareId:null,//缓存仓库查询条件
    type:{},//产品分类缓存数据(id,name)
    useList:[],//仓库专员集合
    listWarehouseData:function () {
        if(configObj.isEmptyObject(configObj.warehouseList)){
            requestData(null,"/warehouse/warehouseList","get","json",false,function (data) {
                configObj.warehouseList=data;
                for(var i=0;i<data.list.length;i++){
                    var id = data.list[i].id;
                    var name = data.list[i].name;
                    configObj.warehouseData[id]=name;
                }
            })
        }
    },
    listGoodsTypeData:function () {
        if(configObj.isEmptyObject(configObj.type)){
            requestData(null,"/goodsType/loadGoodsTypeInfo","post","json",false,function (data) {
                for (var i =0;i<data.data.list.length;i++){
                    var id = data.data.list[i].id;
                    var name = data.data.list[i].name;
                    configObj.type[id]=name;
                }
            });
        }
    },
    //判断集合{}是否有值
    isEmptyObject:function (obj) {
        var flag = true;
        for(var attr in obj){
            flag=false;//obj有值则不用向后台查询产品分类数据
        }
        return flag;
    }
}
/**
 * 后台请求方法
 * @param data 请求数据
 * @param url 请求路径
 * @param requestType 请求方式
 * @param dataType 数据类型
 * @param async是否异步
 * @param callBackFun 成功回调方法
 */
var requestData = function (data, url, requestType,dataType,async,callBackFun, contentType) {
    var param = {
        type: requestType,
        url: baseUrl + url,
        data: data,
        dataType: dataType,
        async: async,
        success: callBackFun,
        error: function () {
            Ladda.stopAll();
        }
    };
    if(contentType){
        param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
    }
    $.ajax(param);
}

$(function () {
    startTime();
    //发送消息按钮事件
    $(document).keyup(function (event) {
        //当且仅当聊天窗口弹出时，按enter键才可发送消息
        if (event.keyCode == 13 && $('.small-chat-box').attr("class").indexOf("active") > -1) {
            $("#sendMsg").trigger("click");
        }
    });
    $.get(baseUrl + "/user/all/1/8", {}, function (d) {
        $(d).each(function (i, user) {
            var div = '<div onclick="setUser(this,' + user.id + ',\'' + user.name + '\',\'' + user.image + '\')"><span><img class="img-circle" src="' + user.image + '"/></span><span>' + user.name + '</span></div>';
            $("#userList").append(div);
        });
    }, "json");
    message.init();
    $(".bellTips").mouseenter(function (data) {
        var children = $(this).find("li:eq(0)");
        children.addClass("open");
        $(".bellTips").mouseleave(function (data) {
            var leave = setTimeout(function () {
                children.removeClass("open");
            },300);
            children.mouseenter(function () {
                clearTimeout(leave);
                children.mouseleave(function () {
                    children.removeClass("open");
                })
            });
        });
    });
    layui.use('element', function(){
        var element = layui.element;
        element.on('tab(docDemoTabBrief)', function(data){
            message.tabSelect(data.index);//当选择对应tab栏目触发表格刷新
            $("#index").attr("dataIndex",data.index);
            //js报错终止其关闭弹出框
            // event.stopPropagation();
            element.stopPropagation();
        });
    });

    // $("#page-tabs-content").click(function () {
    $("#refresh").click(function () {
        $("#page-tabs-content a").each(function (i, item) {
            var active = $(item).hasClass("active");
            if (active) {
                var url = $(item).attr("data-id");
                var main = $('#content-main', parent.document);
                main.find("iframe").each(function (i, d) {
                    if ($(d).attr("src").indexOf(url)!=-1) {
                        $(d).attr("src", $(d).attr("src"));
                    }
                });
            }
        });
    });

    //jqgrid单元格拷贝功能
    $("#cellCopyBtn").click(function () {
        var cellCopyFlag = $(this).attr("cellCopyFlag");
        if(cellCopyFlag == 1){
            $(this).attr("cellCopyFlag", "0");
            $(this).parent().css("background-color","#fff");
        }else{
            $(this).attr("cellCopyFlag", "1");
            $(this).parent().css("background-color","#eee");
        }
    });

    //版本提示
    versionHintObj.init();
    //用户角色
    if(user.roles && user.roles.length > 0){
        var tmpRoles = [];
        $.each(user.roles, function (i, role) {
            if(role.name){
                tmpRoles.push(role.name);
            }
        });
        $("#roleName").attr("title", tmpRoles.join("/"));
    }

    //缓存用户菜单
    $.each(currentUserMenus, function (i,menu) {
        loadAllMenu(menu);
    });

    //加载菜单
    loadMenu(currentUserMenus, null);

    //缓存仓库专员的集合
    stockWarningObj.getCKZYInfo();

    //请求系统参数
    requestData(null, "/sysConfig/getAllConfig", "get", "json", false, function (data) {
        //由于日期类型为数字需要格式处理
        for(var k in data){
            if(data[k].dataType == 'date' && data[k].pattern){
                data[k].value = new Date(data[k].value).format(data[k].pattern.replace(/H/g, "h"));
            }
        }
        sysConfigMap = data;
    });

    //读取配置，决定是否隐藏显示论坛图标
    if(sysConfigMap && sysConfigMap["bbsDisplayFlag"] && sysConfigMap["bbsDisplayFlag"]["value"] == 1){
        $("#bbsIcon").css("display", "block");
    }else {
        $("#bbsIcon").css("display", "none");
    }
    //读取配置，决定系统标题背景颜色
    if (sysConfigMap && sysConfigMap["systemBackColorMap"] && sysConfigMap["systemBackColorMap"]["value"]) {
        var topNavWrap = $("#topNavWrap");
        var topNavHeader = $("#topNavHeader");
        var userCompanyCode = user.companyCode;//获取当前用户公司
        var systemBackColorMap = sysConfigMap["systemBackColorMap"]["value"] || {};
        systemBackColorMap = typeof (systemBackColorMap) == "string" ? JSON.parse(systemBackColorMap) : systemBackColorMap;
        var color = "";
        for (var key in systemBackColorMap) {
            if (systemBackColorMap[key] == userCompanyCode) {
                color = key;//缓存当前配置公司的颜色
            }
        }
        $(topNavWrap).css("background-color", color);
        $(topNavHeader).css("background-color", color);
    }
});

//拷贝菜单
function copyMenu(menu) {
    var tmp = {};
    for(var key in menu){
        tmp[key] = menu[key];
    }
    tmp.childs = [];
    return tmp;
}

//获取所有节点
function loadAllMenu(menu) {
    var tmp = copyMenu(menu);//深度拷贝
    childMenuList.push(tmp);
    childMenuMap[tmp.id] = tmp;
    menuMap[menu.id] = menu;
    if(menu.childs && menu.childs.length > 0){
        $.each(menu.childs, function (m, childMenu) {
            loadAllMenu(childMenu);
        });
    }
}

//菜单加载
function loadMenu(menus, keyword) {
    //渲染一级和二级菜单
    var html = template("menuHtml", {data:menus});

    $(".liSearch").after(html);

    //找到所有二级菜单进行后面菜单的递归算法
    $(".menuLi").find("ul a").each(function (i, a) {
        loadChildMenu(a);
    });

    //如果有筛选条件，则展开所有菜单，否则收缩菜单，如果根据关键字搜索到条件，则筛选字符高亮展示
    if(keyword){
        //所有菜单激活
        $(".menuLi .nav-second-level").each(function (i, ul) {
            $(ul).closest("li").addClass("active");
        });

        //父菜单字体高亮渲染
        $(".menuLi .nav-label").each(function (i, span) {
            $(span).closest("a").css("color", "#a7b1c2");//由于上面激活菜单会
            var text = $(span).text();
            if(text.match(new RegExp(keyword, 'g')) != null){
                text = text.replace(new RegExp(keyword, 'g'), "<span style='color: white;'>"+keyword+"</span>")
            }
            $(span).html(text);
        });
    }else {
        $('#side-menu').metisMenu();
    }

    //通过遍历给菜单项加上data-index属性
    $(".J_menuItem").each(function (index) {
        if (!$(this).attr('data-index')) {
            $(this).attr('data-index', index);
        }
    });
    //菜单点击事件
    $('.J_menuItem').on('click', menuItem);
}

//递归加载菜单
function loadChildMenu(a) {
    var childHtml = "";
    var id = $(a).attr("data-id") || "";
    if(id && menuMap[id] && menuMap[id].childs.length > 0){
        childHtml = template("childMenuHtml", {data:menuMap[id].childs});
        $(a).closest("li").append(childHtml);
        $(a).closest("li").find("ul a").each(function (i, a1) {
            loadChildMenu(a1);
        });
    }
}

//菜单筛选
function searchMenu(t) {
    var val = $(t).closest(".m-b").find("input").val();
    var menus = currentUserMenus;
    if(val){
        menus = filterMenu(val);
    }

    $(".menuLi").remove();
    loadMenu(menus, val);
}

//过滤菜单树
function filterMenu(val) {
    var treeList = [];
    var filterMenuIdList = []; //已添加的Id集合
    //遍历所有节点获取筛选的节点
    $.each(childMenuList, function (m, childMenu) {
        var menuName = childMenu.name;
        if(menuName.match(new RegExp(val, 'g')) != null){
            if(!filterMenuIdList.contains(childMenu.id)){
                filterMenuIdList.push(childMenu.id);//记录已经存在的菜单ID
            }
            addParentMenu(childMenu, filterMenuIdList);
            addChildMenu(childMenu, filterMenuIdList);
        }
    });
    //将菜单列表转换成菜单树
    $.each(filterMenuIdList, function (m, menuId) {
        var menu = childMenuMap[menuId];
        if(menu.parentId == 0){
            var tmp = copyMenu(menu);//深度拷贝
            treeList.push(tmp);
            addChildMenu1(tmp, filterMenuIdList);
        }
    });
    return treeList;
}

//获取节点父节点
function addParentMenu(currentNode, filterMenuIdList) {
    var pid = currentNode.parentId;
    if(pid && pid != 0 && childMenuMap[pid]){
        if(!filterMenuIdList.contains(childMenuMap[pid].id)){
            filterMenuIdList.push(childMenuMap[pid].id);
        }
        addParentMenu(childMenuMap[pid], filterMenuIdList);
    }
}

//获取节点子节点
function addChildMenu(currentNode, filterMenuIdList) {
    var id = currentNode.id;
    $.each(childMenuList, function (c, childNode) {
        var pid = childNode["parentId"];
        if(id == pid){
            if(!filterMenuIdList.contains(childNode.id)){
                filterMenuIdList.push(childNode.id);
            }
            addChildMenu(childNode, filterMenuIdList);
        }
    });
}

//添加子菜单
function addChildMenu1(parentMenu, filterMenuIdList) {
    var childMenus = [];
    var id = parentMenu.id;
    $.each(filterMenuIdList, function (c, menuId) {
        var pid = childMenuMap[menuId].parentId;
        if(id == pid){
            var tmp = copyMenu(childMenuMap[menuId]);//深度拷贝
            childMenus.push(tmp);
            addChildMenu1(tmp, filterMenuIdList);
        }
    });
    parentMenu["childs"] = childMenus;
}

function setUser(t, id, name, headImg) {
    $("#im-body>div:first-child>div").each(function (j, item) {
        if (item == t) $(this).css("background-color", "#eeeeee");
        else $(this).css("background-color", "white");
    });
    // $("#receiveUserName").text(name);
    $("#receiveName").text(name);
    $("#receiveUserId").val(id);
    $("#headImg").val(headImg);
}

//=================================================修改密码开始===================================================
// $.validator.setDefaults({
//     highlight: function (e) {
//         $(e).closest(".form-group").removeClass("has-success").addClass("has-error")
//     }, success: function (e) {
//         e.closest(".form-group").removeClass("has-error").addClass("has-success")
//     }, errorElement: "span", errorPlacement: function (e, r) {
//         e.appendTo(r.is(":radio") || r.is(":checkbox") ? r.parent().parent().parent() : r.parent())
//     }, errorClass: "help-block m-b-none", validClass: "help-block m-b-none"
// }),
$().ready(function () {
    var e = "<i class='fa fa-times-circle'></i> ";
    $("#form").validate({
        rules: {
            oldpassword: {required: !0},
            password1: {required: !0, minlength: 6, maxlength: 16},
            password2: {required: !0, minlength: 6, maxlength: 16, equalTo: "#password1"},
        },
        messages: {
            oldpassword: {required: e + "必填"},
            password1: {
                required: e + "请输入新密码",
                minlength: e + "密码长度必须大于{0}个字符",
                maxlength: e + "密码长度必须小于{0}个字符"
            },
            password2: {
                required: e + "请再次输入新密码",
                minlength: e + "密码长度必须大于{0}个字符",
                maxlength: e + "密码长度必须小于{0}个字符",
                equalTo: "新密码、确认新密码，两次密码输入不一致"
            },
        }
    });
    $("#submit").click(function () {
        var newpwd = $("#password1").val();
        var reg = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,.\/]).{6,16}$/;
        var flag = reg.test(newpwd);
        if (flag == false) {
            alert("新密码必须由 6-16位字母、数字、特殊符号线组成.");
            return;
        }
        var oldpassword = $("#oldpassword").val();
        var password1 = $("#password1").val();
        var password2 = $("#password2").val();
        if ($("#form").valid()) {
            if (password1 == password2) {
                $.ajax({
                    type: "post",
                    url: baseUrl + "/user/updatePassword",
                    data: {
                        oldpassword: oldpassword,
                        password1: password1,
                        password2: password2,
                        userId:user.id
                    },
                    dataType: "json",
                    success: function (data) {
                        if (data.code == 200) {
                            // layer.msg(data.data.message, {time: 1000, icon:6});
                            // setTimeout(function(){
                            //     window.parent.location.href = "/";
                            // },1000);
                            parent.layer.alert(data.data.message);
                            parent.layer.confirm('密码修改成功，请重新登录！', {
                                btn: ['确定'], //按钮
                                shade: false //不显示遮罩
                            }, function () {
                                window.location.href = "/login";
                            });
                        } else {
                            if ($("#form").valid()) {
                                parent.layer.alert(data.msg);
                                initInfo();
                                // $("#myModal").modal('hide');
                            }
                        }
                    }
                });
            } else {
                parent.layer.alert("新密码、确认新密码，两次密码输入不一致");
            }
        }

    });
    $("#cancel").click(function () {
        initInfo();
    });

    function initInfo() {
        document.getElementById("form").reset();
        $("#form").find("input").removeClass('error');
        //清除验证标签
        $("#form").validate().resetForm();
    }

});
//=================================================修改密码结束===================================================

//修改头像

showImage = function () {
    var docObj = document.getElementById("doc");
    var imgObjPreview = document.getElementById("preview");
    if (docObj.files && docObj.files[0]) {
        //火狐下，直接设img属性
        imgObjPreview.style.display = 'block';
        imgObjPreview.style.width = '300px';
        imgObjPreview.style.height = '300px';
        //imgObjPreview.src = docObj.files[0].getAsDataURL();
        //火狐7以上版本不能用上面的getAsDataURL()方式获取，需要一下方式
        imgObjPreview.src = window.URL.createObjectURL(docObj.files[0]);
    } else {
        //IE下，使用滤镜
        docObj.select();
        var imgSrc = document.selection.createRange().text;
        var localImagId = document.getElementById("localImag");
        //必须设置初始大小
        localImagId.style.width = "300px";
        localImagId.style.height = "300px";
        //图片异常的捕捉，防止用户修改后缀来伪造图片
        try {
            localImagId.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale)";
            localImagId.filters
                .item("DXImageTransform.Microsoft.AlphaImageLoader").src = imgSrc;
        } catch (e) {
            alert("您上传的图片格式不正确，请重新选择!");
            return false;
        }
        imgObjPreview.style.display = 'none';
        document.selection.empty();
    }
    return true;
}

edit = function () {
    $("#editModal").modal("toggle");
    $("input:radio").removeAttr("checked");
    $("input:radio").parent().removeClass("checked");
    $.ajax({
        type: "post",
        url: baseUrl + "/user/editUserSelf",
        dataType: "json",
        success: function (data) {
            for (var attr in data.data.user) {
                $("input[name=" + attr + "][type!='radio']").attr("value", data.data.user[attr]);
                if (attr === "sex") {
                    $("input[name='sex'][type='hidden']").val(data.data.user[attr]);
                    $("input[name='sex'][type='radio']").attr("disabled", "disabled");
                    $("input[name='sex'][value='" + data.data.user[attr] + "']").attr("checked", "checked");
                    $("input[name='sex'][value='" + data.data.user[attr] + "']").parent().addClass("checked");
                }
                if (attr === "isMgr") {
                    $("input[name='isMgr'][type='hidden']").val(data.data.user[attr]);
                    $("input[name='isMgr'][type='radio']").attr("disabled", "disabled");
                    $("input[name='isMgr'][value='" + data.data.user[attr] + "']").attr("checked", "checked");
                    $("input[name='isMgr'][value='" + data.data.user[attr] + "']").parent().addClass("checked");
                }
                if (attr === "remark") {
                    $("#remark").val(data.data.user[attr]);
                }
            }
        }
    });
}

submitHander = function (t, url) {
    if ($("#editForm").valid() && checkRepeatUserName()) {
        layer.confirm("请确认用户信息", {
            btn: ["确认", "取消"],
            shade: false
        }, function (index) {
            layer.close(index);
            startModal("#" + t.id);
            var formData = $("#editForm").serializeJson();
            $.ajax({
                type: "post",
                url: url,
                data: formData,
                dataType: "json",
                success: function (data) {
                    Ladda.stopAll();
                    if (data.code == 200) {
                        layer.msg(data.data.message, {time: 1000, icon: 6})
                        $("#editModal").modal("hide");
                    } else {
                        layer.msg(data.msg)
                    }
                },
                error: function (data) {
                    Ladda.stopAll();//隐藏加载按钮
                    layer.msg(data.msg);
                    $("#editModal").modal("hide");
                }
            });
        }, function () {
            return;
        });
    }
}

/**
 * 用户名去重
 * @returns {boolean}
 */
function checkRepeatUserName() {
    var flag = false;
    $.ajax({
        type: "post",
        url: "/user/checkDuplicateUserName",
        data: {id:$("#checkUserId").val(),name:$("#userName").val()},
        dataType: "json",
        async:false,
        success: function (data) {
            if (data.data.userState.sum>0){
                var html ="";
                html +="该用户名存在"+data.data.userState.sum+"个相同用户名，"
                if (data.data.userState.state){
                    html += "其中"+data.data.userState.state+"个状态有效"
                }
                if (data.data.userState.delState){
                    html += "，其中"+data.data.userState.delState+"个状态无效"
                }
                swal({
                    title:"重复提醒",
                    text: html,
                });
                return;

            }else {
                flag=true;
            }
        }
    });
    return flag;
}

$(document).ready(function () {
    var e = "<i class='fa fa-times-circle'></i> ";
    $("#editForm").validate({
        rules: {
            userName: {required: true},
            name: {required: true},
            email: {email: true},
            qq: {checkQQ: true},
            phone: {required: true,checkPhone: true}
        },
        messages: {
            userName: {required: e + "请输入用户名"},
            name: {required: e + "请输入姓名"},
            phone: {required: e + "请输入电话"}
        }
    });
    //自定义正则表达式验证方法
    $.validator.addMethod("checkPhone", function (value, element, params) {
        // var checkPhone = /^((([0]\d{2,3}-)?\d{7,8})|([1]\d{10}))$/;
        var checkPhone = /^[1]([3-9])[0-9]{9}$/;
        return this.optional(element) || (checkPhone.test(value));
    }, "请输入正确的11位手机号码！");
    $.validator.addMethod("checkQQ", function (value, element, params) {
        var checkQQ = /^[1-9][0-9]{4,19}$/;
        return this.optional(element) || (checkQQ.test(value));
    }, "请输入正确的QQ号码！");

    $('.i-checks').iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
    });

    $("#submit2").click(function () {
        var fileName = $("#doc").val();
        var ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();//文件后缀
        var availableExt = "gif|jpg|jpeg|png";//可用的图片格式
        if (!fileName || !ext || availableExt.indexOf(ext) == -1) {
            layer.msg("请选择图片格式文件上传(jpg,png,gif,jpeg等)");
            return;
        }
        var formData = new FormData($("#changePhotoForm")[0]);
        $.ajax({
            type: "post",
            url: baseUrl + "/user/saveImage",
            data: formData,
            dataType: "json",
            async: false,
            cache: false,
            contentType: false,
            processData: false,
            success: function (data) {
                if (data.code == 200) {
                    layer.msg(data.data.message, {time: 1000, icon: 6});
                    //更新登入页面三个头像缓存数据-开始
                    var users = localStorage.getItem("users");
                    var json = [];
                    if (users != null && users != 'null') {
                        json = JSON.parse(users);
                        for (var k in json) {//遍历json数组时，这么写p为索引，0,1
                            if (json[k].name == data.data.user.userName) {
                                json[k].image = data.data.user.image;//修改头像
                            }
                        }
                        localStorage.setItem("users", JSON.stringify(json));
                    }
                    //更新登入页面三个头像缓存数据-结束
                    setTimeout(function () {
                        window.parent.location.href = "/";
                    }, 1000);
                } else {
                    layer.msg(data.msg);
                }
            }
        });
    });
    $("#cancel2").click(function () {
        // window.parent.location.href = "/";
    });
})
//消息类型数量显示
function messageNumberInit() {
    $.get("/message/getParentTypeNumber",null,function (data) {
        var tempMap = {};
        // 未读消息总数
        var total=0;
        //对查到的消息类型数量默认0条
        $("#showMessageNum1").html(0);
        $("#showMessageNum2").html(0);
        $("#showMessageNum3").html(0);
        $("#showMessageNum0").html(0);
        $(data).each(function (i,item) {
            tempMap[item.state]=item.number;
            //重新赋值
            $("#showMessageNum"+item.state).html(item.number);
            total += parseInt(item.number);
        });
        $("#showMessageNum").html(total);
    })
}

function loadMessage(){
    $.ajax({
        url: baseUrl + "/message/list",
        type: "post",
        dataType: "json",
        data: {state: 1},
        success: function (resData) {
            var totalCount = resData.total;
            $("#showMessageNum").html(totalCount);
            if (resData.list) {
                $("#message_list").empty();
                for (var i = 0; i < resData.list.length; i++) {
                    var id = resData.list[i].id;
                    // var image = resData.list[i].image || "/img/mrtx_2.png";
                    // image = image.replace("\\images\\", "/images/");
                    var content = resData.list[i].content || "";
                    var createTime = resData.list[i].createTime ? new Date(resData.list[i].createTime).format("yyyy-MM-dd hh:mm:ss") : "";
                    // var state = resData.list[i].state == 1 ?
                    //     '&nbsp;<b style="color: red;">未读</b>'
                    //     : '&nbsp;<b style="color: green;">已读</b>';
                    var contentColor = resData.list[i].state == 1 ? "black" : "rgb(104, 107, 109)";

                    var message = '<li style="height:42px;margin:10px 0px 0px 2px;">' +
                        '<div data-stopPropagation="true" style="cursor:pointer;color:${contentColor};" onclick="readMessage(this, ${id},\'${content}\',\'${stateo}\')">' +
                        // '<img alt="image" onerror="this.src=\'/img/mrtx_2.png\'" class="img-circle col-sm-3" src="${image}" style="vertical-align: top;width:40px;height:40px;padding:0;margin:0">' +
                        '<div style="display: inline-block;height:40px;margin:0px 0px 0px 20px;overflow:hidden; white-space:nowrap; text-overflow:ellipsis" title="${content}">' +
                        '<span style="font-size: 12px;color: #555555;width:250px;display: inline-block;overflow:hidden; white-space:nowrap; text-overflow:ellipsis">${content}</span><br>' +
                        '<span style="color: #555555;">${createTime}' +
                        // '${state}' +
                        '</span></div>' +
                        '</li>' +
                        '<li class="divider" style="margin: 0px;color: #999999"></li>' +
                        '</div>';
                    message = message.replace("${id}", id).replace(/\${content}/g, content).replace("${content}", content).replace("${createTime}", createTime).replace("${stateo}", resData.list[i].state).replace("${contentColor}", contentColor);
                    $("#message_list").append(message);
                }
            }
        }
    });
}

function loadMessage2(){
    //tab页下标
    var tabIndex = $("#index").attr("dataIndex");
    var type;
    // 默认待办
    if(tabIndex==0 || tabIndex==undefined || tabIndex==null){
        //1待办
        type=1;
    }else if(tabIndex==1){
        //2提醒
        type=2;
    }else if(tabIndex==2){
        //3通知
        type=3;
    }else if(tabIndex==3){
        type=0;
    }
    messageNumberInit();
    $.ajax({
        url: baseUrl + "/message/list",
        type: "post",
        dataType: "json",
        data: {state: 1,parentTypeQC:type},
        success: function (resData) {
            //清空消息div
            var t="";
            //tab切换div显示
            if(tabIndex==null || tabIndex==0){
                t="#message_list1";
            }else if(tabIndex==1){
                t="#message_list2";
            }else if(tabIndex==2){
                t="#message_list3"
            }else if(tabIndex==3){
                t="#message_list0";
            }
            $(t).empty();
            if (resData.list && resData.list.length>0) {
                for (var i = 0; i < resData.list.length; i++) {
                    var id = resData.list[i].id;
                    var content = resData.list[i].content || "";
                    //title显示语句
                    var content2 = content.replace(new RegExp("\"","g"),"");
                    //匹配[]中的数据
                    var test =  /\[(.+?)\]/g;
                    //js将[]里的数据添加span标签，之后进行css颜色区分
                    content = content.replace(test,"[<span>$1</span>]");
                    content = content.replace(new RegExp("\"","g"),"");
                    var createTime = resData.list[i].createTime ? new Date(resData.list[i].createTime).format("yyyy-MM-dd hh:mm:ss") : "";
                    var url = resData.list[i].url ? resData.list[i].url : "";
                    var urlName = resData.list[i].urlName ? resData.list[i].urlName : "";
                    var contentColor = resData.list[i].state == 1 ? "black" : "rgb(104, 107, 109)";
                    var message = '<li style="height:48px;padding:8px 0px 0px 2px;">' +
                        '<div style="cursor:pointer;color:${contentColor};" onclick="readMessage(this, ${id},\'${content}\',\'${stateo}\')">' +
                        '<input type="hidden" id="url'+id+'" value="${url}">'+
                        '<input type="hidden" id="urlName'+id+'" value="${urlName}">'+
                        '<div style="display: inline-block;height:40px;margin:0px 0px 0px 20px;overflow:hidden; white-space:nowrap; text-overflow:ellipsis" title="${content2}">' +
                        '<span class="messageColor" style="font-size: 12px;color: #555555;width:280px;display: inline-block;overflow:hidden; white-space:nowrap; text-overflow:ellipsis">${content}</span><br>' +
                        '<span style="color: #555555;">${createTime}' +
                        '</span></div>' +
                        '</li>' +
                        '<li class="divider" style="margin: 0px;color: #999999"></li>' +
                        '</div>';
                    message = message.replace("${id}", id).replace(/\${content}/g, content).replace("${content}", content).replace("${content2}", content2).replace("${createTime}", createTime).replace("${stateo}", resData.list[i].state).replace("${contentColor}", contentColor).replace("${url}",url).replace("${urlName}",urlName);
                    $(t).append(message);
                }
            }
        }
    });
    // $("body").on('click','[data-stopPropagation]',function (e) {
    //     e.stopPropagation();
    // });
}
/**
 * 查看更多消息
 */
function moreMessage() {
    var type = $(".layui-this").attr("data-id");
    page("/workbench/message_list?parentType="+type, "我的消息");
}

function goToCompany() {
    page("/bbs/firstTopic","论坛");
}

/**
 * 阅读消息
 * @param obj
 * @param messageId
 * @param content
 * @param state
 */
function readMessage(obj, messageId, content, state) {
    var oldMessageId = $("#messageId").val();
    var url = $("#url"+messageId).val();
    var urlName = $("#urlName"+messageId).val();
    if (oldMessageId && oldMessageId == messageId) {
        loadMessage2();
        return;
    } else {
        $("#messageId").val(messageId);
    }
    //已读
    if (state != 1) {
        // layer.msg(content, {time: 1500});
        return;
    }
    $.ajax({
            type: "post",
            url: "/message/readMessage",
            data: {id: messageId},
            dataType: "json",
            success: function (resData) {
                if (resData.code == 200) {
                    // layer.msg(content, {time: 2500});
                    if(url!="" && url!=null && url!=undefined){
                        //消息链接跳转审核查看页面
                        if(url.indexOf("itemId=")!=-1){
                            var urls = url.split("itemId=");
                            var itemId = urls[1];
                            $.get("/items/queryItemStateById",{itemId:itemId},function (data) {
                                //待办
                                if(data==1){
                                    page(url,urlName);
                                //已办
                                }else if(data==2){
                                    layer.msg("该审核已完成");
                                }else{

                                }
                            });
                        }else{
                            page(url,urlName);
                        }
                    }else{
                        //消息链接跳转消息管理页面
                        moreMessage();
                    }
                    loadMessage2();
                }
            }
        }
    );
}

var message = {
    init:function () {
        //默认待办消息
        message.tabSelect(0);//默认tab页为待办
    },
    tabSelect:function (index) {//右边tab页选择时，列表改变, index = tab下标
        $(".tabContent").css("display","none");
        $(".tabTitle").removeClass("layui-this");//移除tab选中
        $($(".tabTitle")[index]).addClass("layui-this");
        $("#index").attr("dataIndex",index);
        if(index == 0){//待办1
            $("#pt1Div").css("display","block");
            loadMessage2();
        }
        if(index == 1){//提醒2
            $("#pt2Div").css("display","block");
            loadMessage2();
        }
        if(index == 2){//通知3
            $("#pt3Div").css("display","block");
            loadMessage2();
        }
        if(index == 3){//其它0
            $("#pt4Div").css("display","block");
            loadMessage2();
        }
    }
}

//用户绑定相关
var userBinding = {
    bindingUserUrl: "/userBinding/binding",
    exchangeUserListUrl: "/userBinding/bindingUserList",
    exchangeUserUrl: "/userBinding/exchangeUser",
    cancelBindingUrl: "/userBinding/cancelBinding",
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
    eyeClick: function (t) {
        if($(t).hasClass("glyphicon-eye-open")){
            $(t).parent().find("input:eq(0)").attr("type",'password');
            $(t).removeClass("glyphicon-eye-open");
            $(t).addClass("glyphicon-eye-close");
        } else{
            $(t).parent().find("input:eq(0)").attr("type",'text');
            $(t).removeClass("glyphicon-eye-close");
            $(t).addClass("glyphicon-eye-open");
        }
    },
    bindingUser: function () {
        var btnFlag = false;
        var index  = layer.open({
            type: 1,
            title: '关联用户',
            zIndex: 10000,
            content: $("#bindingUserDiv"),
            btn: ['关联','取消'],
            area: ['480px', '330px'],
            shadeClose: false,
            resize: false,
            move: '.layui-layer-title',
            moveOut: true,
            success: function(layero, index){
               //弹窗创建事件
                $("#bindingUserForm")[0].reset();
            },
            yes: function () {
                if(!$("#bindingUserForm").valid()){
                    return;
                }
                if(!btnFlag){
                    btnFlag = true;
                    userBinding.requestData(JSON.stringify($("#bindingUserForm").serializeForm()), userBinding.bindingUserUrl, "post", "json", true, function (data) {
                        if(data.code == 200){
                            layer.closeAll();
                            layer.msg("关联用户成功！", {time: 2000, icon: 6});
                        }else {
                            btnFlag = false;
                            if(data.msg){
                                layer.msg(data.msg, {time: 3000, icon: 5});
                            }else {
                                layer.msg("关联用户失败！", {time: 3000, icon: 5});
                            }
                        }
                    },true);
                }
            }
        });
    },
    exchangeUserList: function () {
        var index  = layer.open({
            type: 1,
            title: '切换用户<span style="font-size: 10px;color: red;">(点击对应用户进行切换)</span>',
            zIndex: 10000,
            content: $("#bindingUserListDiv"),
            btn: [],
            area: ['850px', '340px'],
            shadeClose: false,
            closeBtn: 1,
            resize: false,
            move: '.layui-layer-title',
            moveOut: true,
            success: function(layero, index){
                userBinding.requestData(null, userBinding.exchangeUserListUrl, "post", "json", true, function (data) {
                    if(data.code && data.code == 403){
                        layer.msg("很抱歉，您没有用户切换关联权限，请联系管理人员！", {time: 3000, icon: 5});
                    }else {
                        if(data && data.length > 0){
                            var html = "";
                            $("#bindingUnionId").text(data[0].unionId);
                            $.each(data, function (i, bindingUser) {
                                if(user.id != bindingUser.user.id){
                                    var imgSrc = bindingUser.user.image || "/img/mrtx_1.png";
                                    var deptName = bindingUser.user.companyCode+"-"+bindingUser.user.deptName;
                                    html += "<div class=\"excerpt\">\n" +
                                        "        <div class=\"contact-box boxBg\" style=\"padding: 5px;\">\n" +
                                        "        <input type=\"hidden\" name=\"userId\" value='"+bindingUser.user.id+"'>" +
                                        "            <a href=\"javascript:;\">\n" +
                                        "                <div class=\"imgDiv\" onclick='userBinding.exchangeUserClick("+bindingUser.user.id+")'>\n" +
                                        "                    <img alt=\"图片丢失\" onerror=\"this.src='/img/mrtx_1.png'\" class=\"img-circle m-t-xs img-responsive\" src=\""+imgSrc+"\">\n" +
                                        "                </div>\n" +
                                        "                <div class=\"userInfoDiv\" onclick='userBinding.exchangeUserClick("+bindingUser.user.id+")'>\n" +
                                        "                    <h3>\n" +
                                        "                        <strong>"+bindingUser.user.name+"</strong>\n" +
                                        "                    </h3>\n" +
                                        "                    <address>\n" +
                                        "                        用户名：<span title='"+bindingUser.user.userName+"'>"+bindingUser.user.userName+"</span><br>\n" +
                                        "                        部门：<span title='"+deptName+"'>"+deptName+"</span>\n" +
                                        "                    </address>\n" +
                                        "                </div>\n" +
                                        "                <div class=\"unBinding\" onclick='userBinding.cancelBinding("+bindingUser.user.id+", this)'>\n" +
                                        "                    <span>解<br>绑</span>\n" +
                                        "                </div>\n" +
                                        "                <div class=\"clearfix\"></div>\n" +
                                        "            </a>\n" +
                                        "        </div>\n" +
                                        "    </div>";
                                }
                            });
                            $("#user_list").html(html);
                        }else {
                            $("#bindingUnionId").html("<span style='color: red;font-size: 10px;'>暂未关联用户</span>");
                        }
                    }
                });
            },
        });
    },
    exchangeUserClick: function (userId) {
        if(!userId){
            layer.msg("切换用户ID为空，请联系技术人员！", {time: 3000, icon: 5});
        }else {
            layer.msg('用户切换中...', {time: 10000, shade: [0.7, '#393D49']})
        }
        var param = {userId: userId};
        userBinding.requestData(param, userBinding.exchangeUserUrl, "post", "json", true, function (data) {
            if(data.code == 200){
                window.location.href = "/";
            }else {
                if(data.msg){
                    layer.msg(data.msg, {time: 3000, icon: 5});
                }else {
                    layer.msg("切换用户失败！", {time: 3000, icon: 5});
                }
            }
        });
    },
    cancelBinding: function (userId, t) {
        if(!userId){
            layer.msg("切换用户ID为空，请联系技术人员！", {time: 3000, icon: 5});
            return;
        }
        var index = layer.confirm('您确定要解绑该用户吗？', {
            btn: ['确定', '取消'], //按钮
            shade: false //不显示遮罩
        }, function () {
            layer.close(index);
            userBinding.requestData({userId: userId}, userBinding.cancelBindingUrl, "post", "json", true, function (data) {
                if(data.code == 200){
                    $(t).closest(".excerpt").remove();
                    layer.msg("用户解绑成功！", {time: 2000, icon: 6});
                }else {
                    if(data.msg){
                        layer.msg(data.msg, {time: 3000, icon: 5});
                    }else {
                        layer.msg("用户解绑失败！", {time: 3000, icon: 5});
                    }
                }
            });
        }, function () {
        });
    }
}

//版本提示相关
var versionHintObj = {
    init: function () {
        if(user && user.id){
            userBinding.requestData(null,"/versionHint/listAllVersionHint","get", "json", true, function (data) {
                if(data && data[user.id] && data[user.id].length > 0){
                    var index  = layer.open({
                        type: 1,
                        title: '版本更新提示',
                        zIndex: 10000,
                        content: $("#versionHintDiv"),
                        btn: ['不再提示','下次提示'],
                        area: ['750px', '440px'],
                        shadeClose: false,
                        resize: false,
                        move: '.layui-layer-title',
                        moveOut: true,
                        success: function(layero, index){
                            $(layero).find(".layui-layer-content").css("height", "350px");
                            var html = "";
                            $.each(data[user.id], function (i, versionHint) {
                                html += "<li class=\"layui-timeline-item timeLineCss\">\n" +
                                    "                <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
                                    "                <div class=\"layui-timeline-content layui-text timeCss\">\n" +
                                    "                    <span class=\"layui-timeline-title timeContentTitle\">"+versionHint.title+"("+new Date(versionHint.time).format('yyyy-MM-dd hh:mm:ss')+")</span>\n" +
                                    "                    <div class=\"timeContent\">\n" +
                                    "                            "+versionHint.content+"\n" +
                                    "                    </div>\n" +
                                    "                </div>\n" +
                                    "            </li>";
                            });
                            $(layero).find("#announcements").html(html);
                        },
                        yes: function (index, layero) {
                            userBinding.requestData(null, "/versionHint/closeHint", "get", "json", true, function (data) {
                                if (data.code == 200) {
                                    layer.msg("此次版本更新将不再提示！", {time: 1000, icon: 6});
                                } else {
                                    layer.msg(data.msg, {time: 3000, icon: 5});
                                }
                            });
                            layer.closeAll();
                            suggestHintObj.init(index,layero);
                        },
                        cancel: function(){
                            //右上角关闭(下次提示)回调
                            suggestHintObj.init();
                        }
                    });
                }else{
                    //无版本提示弹出建议提示
                    suggestHintObj.init();
                }
            });
        }
    },
    searchHistoryVersion:function (t, createDate) {
        var param = $("#historyVersionForm").serializeJson();
        if(createDate){
            param["createDate"] = createDate;
        }
        var historyAnnouncements = $(t).closest(".historyVersiontDiv").find("#historyAnnouncements");
        userBinding.requestData(param,"/versionHint/historyVersionHint","post", "json", true, function (data) {
            versionHintObj.renderHistoryVersionHint(data, historyAnnouncements);
        });
    },
    renderHistoryVersionHint: function (data, historyAnnouncements) {
        var html = "";
        if(data && data.length > 0){
            $.each(data, function (i, versionHint) {
                html += "<li class=\"layui-timeline-item timeLineCss\">\n" +
                    "                <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
                    "                <div class=\"layui-timeline-content layui-text timeCss\">\n" +
                    "                    <span class=\"layui-timeline-title timeContentTitle\">"+versionHint.title+"("+new Date(versionHint.time).format('yyyy-MM-dd hh:mm:ss')+")</span>\n" +
                    "                    <div class=\"timeContent\">\n" +
                    "                            "+versionHint.content+"\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "            </li>";
            });
        }
        $(historyAnnouncements).html(html);
    },
    historyVersionHint:function () {
        userBinding.requestData(null,"/versionHint/historyVersionHint","post", "json", true, function (data) {
            if(data && data.length > 0){
                var index  = layer.open({
                    type: 1,
                    title: '历史版本提示',
                    zIndex: 10000,
                    content: $("#historyVersionHintDiv"),
                    btn: 0,
                    area: ['750px', '440px'],
                    shadeClose: false,
                    resize: false,
                    move: '.layui-layer-title',
                    moveOut: true,
                    success: function(layero, index){
                        var laydate = layui.laydate;
                        //提示时间
                        var createDate = {
                            elem: $(layero[0]).find("input[name='createDate']")[0],
                            istime: true,
                            type:'date',
                            format:'yyyy-MM-dd',
                            done:function (value,data) {
                                versionHintObj.searchHistoryVersion($(layero[0]).find("input[name='createDate']"), value);
                            }
                        };
                        laydate.render(createDate);
                        $(layero).find(".layui-layer-content").css("height", "350px");
                        versionHintObj.renderHistoryVersionHint(data,  $(layero).find("#historyAnnouncements"));
                    },
                });
            }
        });
    }
}

//版本提示相关
var suggestHintObj = {
    init: function () {
        if (user && user.id) {
            userBinding.requestData(null, "/proposeTips/querySuggestHintData", "get", "json", false, function (data) {
                if (data.code == 200) {
                    var list = data.data.list;
                    if (list.contains(user.id)) {
                        var message = "";
                        if (data.data.content != null && data.data.content != '' && data.data.content != undefined) {
                            message = data.data.content;
                        } else {
                            message = "<div style='text-align: center;padding: 120px 50px;'>请按时填写建议，谢谢！</div>";
                        }
                        $("#showSuggestContent").empty();
                        var index = layer.open({
                            type: 1,
                            title: "建议提示",
                            zIndex: 5000,
                            content: $("#suggestWarnDiv").html(),
                            btn: [],
                            area: ['750px', '440px'],
                            resize: true,
                            move: '.layui-layer-btn',
                            moveOut: true,
                            success: function (layero, index) {
                                $(layero).find("div[id='showSuggestContent']").html(message);
                                //用户弹出清除建议提示缓存
                                userBinding.requestData(null, "/proposeTips/updateProposeCache", "get", "json", false, function (data) {

                                });
                            },
                            cancel: function () {
                                //右上角关闭回调
                                layer.closeAll();
                            }
                        });
                    }
                }
            });
        }
    },
}


//库存预警相关
var stockWarningObj = {
    modalIndex:null,
    tempLayerObj:{},
    init: function () {
        var wareId = $(stockWarningObj.tempLayerObj[0]).find("select[name='wareId']").val();
        $(".showStockMaxWarnInfo").empty();
        $(".showStockMinWarnInfo").empty();
        //库存最大值预警
        var stockMaxHtml = "";
        requestData({wareId: wareId}, "/goodsType/getStockMaxWarnData", "post", "json", false, function (data) {
            if (data && data.list != null) {
                $.each(data.list, function (i, record) {
                    stockMaxHtml += "<tr>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (i + 1) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + configObj.type[record.typeId] +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + record.goodsName +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (configObj.warehouseData[configObj.wareId] == undefined ? "总仓库" : configObj.warehouseData[configObj.wareId]) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (record.specs == undefined ? "" : record.specs) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + record.unit +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + record.amount +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (record.stockMaxAmount == undefined ? 0 : record.stockMaxAmount) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (record.stockMinAmount == undefined ? 0 : record.stockMinAmount) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "   </tr>";
                });
            }
        });
        $(".showStockMaxWarnInfo").append(stockMaxHtml);
        //库存最小值预警
        var stockMinHtml = "";
        requestData({wareId: wareId}, "/goodsType/getStockMinWarnData", "post", "json", false, function (data2) {
            if (data2 && data2.list != null) {
                $.each(data2.list, function (i, record) {
                    stockMinHtml += "<tr>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (i + 1) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + configObj.type[record.typeId] +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + record.goodsName +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (configObj.warehouseData[configObj.wareId] == undefined ? "总仓库" : configObj.warehouseData[configObj.wareId]) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (record.specs == undefined ? "" : record.specs) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + record.unit +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + record.amount +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (record.stockMaxAmount == undefined ? 0 : record.stockMaxAmount) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "        <td>\n" +
                        "            <div class=\"ellipsisContent\">\n" + (record.stockMinAmount == undefined ? 0 : record.stockMinAmount) +
                        "            </div>\n" +
                        "        </td>\n" +
                        "   </tr>";
                });
            }
        });
        $(".showStockMinWarnInfo").append(stockMinHtml);
    },
    //展示库存预警信息
    showStockWarningModal:function(){
        stockWarningObj.tempLayerObj={};
        stockWarningObj.modalIndex = layer.open({
            type: 1,
            title: false,
            zIndex: 10000,
            content: $("#stockWarnDiv"),
            btn: [],
            area: ['70%', '60%'],
            shadeClose: false,
            resize: false,
            move: '.layui-layer-title',
            moveOut: true,
            success: function (layero, index) {
                stockWarningObj.tempLayerObj=layero;
                configObj.listWarehouseData();
                stockWarningObj.selectWarehouse($(stockWarningObj.tempLayerObj[0]).find("select[name='wareId']"), null);
                configObj.listGoodsTypeData();
                stockWarningObj.init();
                //使用layui表单
                layui.use('form', function(){
                    var form = layui.form;
                    form.on('select(wareId)', function(data){
                        configObj.wareId=data.value;
                        stockWarningObj.init();
                    });
                    form.render();
                })
            }
        });

    },
    //加载仓库信息
    selectWarehouse:function (t,id) {
        layui.use(["form"],function () {
            configObj.listWarehouseData();
            var data = configObj.warehouseList;
            $(t).empty();
            var html="<option value=''>总仓库</option>";
            $(data.list).each(function (i, d) {
                var selected = id == d.id ? "selected=selected" : "";
                html+="<option value='" + d.id + "' " + selected + ">" + d.name + "</option>";
            });
            $(t).append(html);
            layui.form.render();
        });
    },
    //获取仓库专员集合
    getCKZYInfo:function () {
        if(configObj.useList.length<=0){
            requestData(null,"/user/getCKZYInfo","post", "json", false, function (data) {
                if(data && data.data.list!=null){
                    for (var i=0;i<data.data.list.length;i++){
                        var userId=data.data.list[i].id;
                        configObj.useList.push(userId);
                    }
                }
            });
        }
        if (configObj.useList.contains(user.id)) {
             $("#showStockWarningDiv").show();
        }else{
             $("#showStockWarningDiv").hide();
        }
    },
    //关闭库存预警模态框
    closeModal:function () {
        if(stockWarningObj.modalIndex){
            stockWarningObj.closeModal(stockWarningObj.modalIndex);
        }else {
            stockWarningObj.closeAll();
        }
    }
}