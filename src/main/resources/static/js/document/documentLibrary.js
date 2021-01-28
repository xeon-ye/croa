var libraryType= {};//制度类型缓存
var releaseUser ={}; // 发布人缓存
var userList ={user:[],dept:[]}; //缓存用户
var fls = false;
$(function () {
    layui.use(['element', 'form','table'], function(){
        var form = layui.form;

        form.on('select(releaseUser)', function(data){
            pageObj.libraryTable();
            pageObj.thumbnailList();

        });
        form.on('select(state)',function (data) {
            pageObj.libraryTable();
            pageObj.thumbnailList();
        });

        var element = layui.element;
        element.on('tab(docDemoTabBrief)', function(data){
            commonObj.tabChange(data.index);
        });

    });
    commonObj.tabChange(0);


    pageObj.initTree();
    //课程图片上传
    commonObj.imageUpload = new FileUpload({
        targetEl: '#imageUploadForm',
        multi: false,
        filePart: "trainPlan",
        completeCallback: function (data) {
            if (data.length > 0) {
                var filePath = data[0].file;
                $(".coursePic").css("background-image", "url(\""+filePath+"\")");
                $("input[name='thumbnailPictureLink']").val(filePath);
            }
        },
        acceptSuffix: ['jpg', 'png']
    });

    var laydate = layui.laydate;
    //制度生效开始时间
    var releaseTime ={
        elem:'#releaseTime',
        istime:false,
        type:'date',
        istoday:true,
        format:'yyyy-MM-dd',
        done:function (value,data) {
            $("#releaseTime").val(value);
            pageObj.libraryTable();
            pageObj.thumbnailList();
        }
    };
    laydate.render(releaseTime);
    var releaseTime1 ={
        elem:'#releaseTime1',
        istime:false,
        istoday:true,
        type:'date',
        format:'yyyy-MM-dd',
        done:function (value,data) {
            $("#releaseTime1").val(value);
            pageObj.libraryTable();
            pageObj.thumbnailList();
        }
    };
    laydate.render(releaseTime1);

    pageObj.renderReleaseUser("#releaseUser1");
    // var userIds = systemUser();
    if (systemUser()){
     $("#btnSystem").show()
        $(".tableHide").show();
     fls = true;
    }else {
        $("#btnSystem").hide();
        $(".tableHide").hide();
        fls=false;

    }
});

//页面公共处理对象
var commonObj = {
    imageUpload:null,
    //后台请求方法
    requestData: function (data, url, requestType,dataType,async,processData,callBackFun, contentType) {
        var param = {
            type: requestType,
            url: baseUrl + url,
            data: data,
            dataType: dataType,
            async: async,
            processData:processData,
            success: callBackFun,
        };
        if(contentType){
            param.contentType = 'application/json;charset=utf-8'; //设置请求头信息
        }
        $.ajax(param);
    },

    //分页插件使用
    pagerPlus: function (config,callback,type) {
        layui.use('laypage', function(){
            var laypage = layui.laypage;
            //执行一个laypage实例
            laypage.render({
                elem: config.elem //注意，这里的 test1 是 ID，不用加 # 号
                ,count: config.count || 0, //数据总数，从服务端得到
                layout: ['count','prev','page','next','refresh','limit','skip'],
                hash: true,
                limits: config.limits || [10, 15, 20, 50, 100],
                limit: config.limit || 10,
                jump: function (obj, first) {
                    config.param = config.param || {};
                    config.param.size = obj.limit;
                    config.param.page = obj.curr;
                    //下面这段逻辑为静态测试，等有实际接口之后，使用下面注释代码代替
                    commonObj.requestData(config.param, config.url, "post", "json", true,true,function (data) {
                        if(callback){
                            if(config.target){
                                callback(data,config.target);
                            }else {
                                callback(data,type);
                            }
                        }
                    });
                }
            });
        });
    },
    //Tab切换处理事件
    tabChange: function (index) {
        $(".pictureContent").css("display","none");
        $(".modalTableContent").css("display","block");
        if(index == 0){
            $(".modalTableContent").css("display","block");
            $(".pictureContent").css("display","none");
            pageObj.libraryTable();
        }else if(index == 1) {
            $(".modalTableContent").css("display","none");
            $(".pictureContent").css("display","block");
            pageObj.thumbnailList();
        }
    },
    tabChange3:function (index) {
        $(".noReadyTable").css("display","none");
        $(".havaReadyTable").css("display","block");
        if(index == 0){
            $(".noReadyTable").css("display","none");
            $(".havaReadyTable").css("display","block");
            pageObj.havaReadyTable();
        }else if(index == 1) {
            $(".havaReadyTable").css("display","none");
            $(".noReadyTable").css("display","block");
            pageObj.noReadyTable();
        }
    },
    enterEvent:function (event) {
        if (event.keyCode == '13' || event.keyCode ==13){
            pageObj.libraryTable();
            pageObj.thumbnailList();
        }
    }

};
var pageObj={
    modalIndex: null,
    courseRemake:null,
    typeId:[],
    roleTypeMap:{},
    libraryId:[],
    btnFlag:true,
    courseSignRangeMap:{dept:[],deptList:[],  role:[], roleList:[], user:[],userList:[]},
    courseSignRangeChooseMap:{dept:[], role:[], user:[]},
    fileMap:[],
    fileLink:[],
    libraryReady:{},
    libraryTypeId:[],
    libraryNotReady:{},
    libraryState:{"-9":"已删除",0:"已发布",1:"已保存",2:"已失效"},
    levelState :{"A":"重要","B":"中等","C":"一般"},
    libraryTypeUrl:"/documentLibrary/libraryType",
    releaseUser:"/documentLibrary/releaseUser",
    //加载制度类型树
    initTree:function () {
        var tyepTreeData = [];
        commonObj.requestData(null,pageObj.libraryTypeUrl,"post","json",false,false,function (data) {
            libraryType = data.data.list;
            pageObj.libraryTypeId =[];
            var arrays = data.data.list;
            if (arrays != null && arrays.length > 0){
                arrays.forEach(function (group) {
                    if (group.level==1){
                        tyepTreeData.push(group);
                    }
                })
            }
            $('#schemeTree').treeview(
                {
                    icon:"glyphicon glyphicon-stop",
                    selectedIcon:"glyphicon glyphicon-stop",
                    color:"#000000",
                    levels : 1,
                    backColor:"#FFFFFF",
                    selectable:true,
                    state: {
                        checked:true,
                        disabled:true,
                        expanded:true,
                        selected:true
                    },
                    tags: ['available'],
                    showCheckbox:false,//是否显示多选
                    data: tyepTreeData,
                    onNodeSelected: function (event, data) {
                        pageObj.libraryTypeId= data;
                        pageObj.libraryTable();
                        pageObj.thumbnailList();

                    },
                    onNodeUnselected :function (event, data) {
                        pageObj.libraryTypeId =[];
                        pageObj.libraryTable();
                        pageObj.thumbnailList();
                    }
                }
            );
        },true);

    },
    noReadyTable:function () {
        $(".noReadyTable").html("");
        if(pageObj.libraryNotReady.length>0){
            var html1 = "";
            pageObj.libraryNotReady.forEach(function (data) {
                html1 += "<div class=\"userDivClass\">\n" +
                    "   <div id="+data.id+" class=\"userClass\">" + data.name + "</div>\n" +
                    "     </div>";
            });
            $(".noReadyTable").append(html1);
        }
    },
    havaReadyTable:function () {
        $(".havaReadyTable").html("");
        if(pageObj.libraryReady.length>0){
            var html1 = "";
            pageObj.libraryReady.forEach(function (data) {
                    html1 += "<div class=\"userDivClass\">\n" +
                        "   <div id="+data.id+" class=\"userClass\">" + data.name + "</div>\n" +
                        "     </div>";
                });
                $(".havaReadyTable").append(html1);
        }
    },
    libraryTable:function (value) {
        var param = $("#documentLibraryForm").serializeJson();
        param['typeId'] =pageObj.libraryTypeId ? pageObj.libraryTypeId.id : '';
        layui.use(['table','laypage'], function(){
            var table = layui.table;
            table.render({
                    elem:'#demo1',
                    method:'post',
                    where:param,
                    autoSort: false,
                    url:"/documentLibrary/list",
                    title:"制度类表",
                    page:true,
                    totalRow:false,
                     skin:'nob',
                // toolbar:true,
                    cols:[[
                        {type: 'numbers',title:'序号', fixed: 'left'},
                        {field: 'id', title: 'ID',  sort: true, fixed: 'left',hide:true},
                        {field: 'libraryCode', title: '制度编号', sort: true, fixed: 'left',templet:function (d) {
                            return '<div class="ellipsisContent textOver">'+d.libraryCode+ '</div>'
                        }},
                        {field: 'version', title: '版次',  fixed: 'left'},
                        {field: 'level', title: '等级',  sort: true, fixed: 'left',templet:function (d) {
                            return '<div class="ellipsisContent textOver">'+ pageObj.levelState[d.level]+'</div>'
                        }},
                        {field: 'libraryName', title: '制度名称', sort: true, fixed: 'left',templet:function (d) {
                            var ht ='';
                            if (d.attachment.length>0){
                                ht ='<img src="/img/train/tpo.png">';
                            }
                           return'<div class="ellipsisContent textOver">'+ht+'<a href=/documentLibrary/library/view/'+d.id+'>'+d.libraryName+'</a></div>'
                        }},
                        {field: 'releaseDept', title: '发布部门',  fixed: 'left',templet:function (d) {
                            return '<div class="ellipsisContent textOver">'+ userList["dept"][d.releaseDept]+'</div>';
                        }},
                        {field: 'libraryCode', title: '发布人', fixed: 'left',templet:function (d) {
                            return '<div class="ellipsisContent textOver">'+ userList["user"][d.releaseUser]+'</div>';
                        }},
                        {field: 'createId', title: '创建人',  fixed: 'left',templet:function (d) {
                            return  '<div class="ellipsisContent textOver">'+ userList["user"][d.createId]+'</div>';
                        }},
                        {field: 'releaseTime', title: '发布时间', sort: true, width:200, fixed: 'left',templet:function (d) {
                            return new Date(d.releaseTime).format("yyyy-MM-dd hh:mm:ss");
                        }},
                        {field: 'state', title: '状态',   fixed: 'left',templet:function (d) {
                            // return pageObj.libraryState[d.state]
                            switch (d.state){
                                case -9:
                                    return "<span style = 'color:red'>"+pageObj.libraryState[d.state]+"</span>";
                                case 0:
                                    return "<span style = ''>"+pageObj.libraryState[d.state]+"</span>";
                                case 1:
                                    return "<span style = ''>"+pageObj.libraryState[d.state]+"</span>";
                                case 2:
                                    return "<span style = ''>"+pageObj.libraryState[d.state]+"</span>";
                            }
                        }},
                        {field: 'state', title: '操作',  fixed: 'left',width:200,templet:function (d) {
                            var btnHtml = "";
                            btnHtml += " <button data-state='"+d.state+"' class=\"tableButton blueBtn\" type=\"button\"  onclick='pageObj.readingStatistical("+d.id+")'>\n" +
                                "            阅读统计\n" +
                                "        </button>";
                            if (d.state == 1){
                                btnHtml += " <button data-state='"+d.state+"' class=\"tableButton orangeBtn\" type=\"button\"  onclick='pageObj.delLibrary("+d.id+")'>\n" +
                                    "            删除\n" +
                                    "        </button>";
                                btnHtml += " <button data-state='"+d.state+"' class=\"tableButton blueBtn\" type=\"button\" onclick='pageObj.addLibraryModal("+d.id+")'>\n" +
                                    "            编辑\n" +
                                    "        </button>";

                            }else if(d.state== 0){
                                btnHtml += " <button data-state='"+d.state+"' class=\"tableButton orangeBtn\" type=\"button\" onclick='pageObj.delLibrary("+d.id+")'>\n" +
                                    "            删除\n" +
                                    "        </button>";
                                btnHtml += " <button data-state='"+d.state+"' class=\"tableButton blueBtn\" type=\"button\" onclick='pageObj.updatefailure("+d.id+")'>\n" +
                                    "            失效\n" +
                                    "        </button>";

                            }else if (d.state==-9){
                                btnHtml += " <button data-state='"+d.state+"' class=\"tableButton blueBtn\" type=\"button\" onclick='pageObj.addLibraryModal("+d.id+")'>\n" +
                                    "            编辑\n" +
                                    "        </button>";
                            } else{
                                btnHtml += " <button data-state='"+d.state+"' class=\"tableButton orangeBtn\" type=\"button\"  onclick='pageObj.delLibrary("+d.id+")'>\n" +
                                    "            删除\n" +
                                    "        </button>";
                                btnHtml += " <button data-state='"+d.state+"' class=\"tableButton blueBtn\" type=\"button\" onclick='pageObj.addLibraryModal("+d.id+")'>\n" +
                                    "            编辑\n" +
                                    "        </button>";

                            }

                            return btnHtml;
                        }},
                    ]],
                    done:function (res,curr, count) {
                        tdTitle();
                        if (res){
                            $.each(res.data,function (i,d) {
                                if (d.effectiveEndTime){
                                    var newDate =new Date();
                                    var effectiveEndTime = new Date(d.effectiveEndTime);
                                    if (effectiveEndTime<newDate && d.state==0){
                                        pageObj.updateLibraryState(d.id);
                                    }
                                }
                                if (!fls){
                                    $("[data-field='level']").css('display','none');
                                    $("[data-field='version']").css('display','none');
                                    $("[data-field='createId']").css('display','none');
                                    $("[data-field='state']").css('display','none');
                                    $("[data-field='libraryCode']").css('width','20%');
                                    $("[data-field='libraryName']").css('width','30%');
                                    $("[data-field='releaseDept']").css('width','20%');
                                }
                            })
                        }
                    }

                });
            //监听排序事件
            table.on('sort(test)', function(obj){ //注：sort 是工具条事件名，test 是 table 原始容器的属性 lay-filter="对应的值"
                table.reload('demo1', {
                    initSort: obj //记录初始排序，如果不设的话，将无法标记表头的排序状态。
                    ,where: { //请求参数（注意：这里面的参数可任意定义，并非下面固定的格式）
                        field: obj.field //排序字段
                        ,order: obj.type //排序方式
                    },
                });

                // layer.msg('服务端排序。order by '+ obj.field + ' ' + obj.type);
            });

        })
    },
    //关闭弹框
    putStockModalClose:function () {
        if (pageObj.modalIndex){
            layer.close(pageObj.modalIndex);
        }else {
            layer.closeAll();
        }
    },
    //阅读统计
    readingStatistical:function (id) {
        pageObj.modalIndex = layer.open({
            type :1,
            title:false,
            zIndex:-9000,
            area: ['50%', '60%'],
            content:$("#readyingModal").html(),
            btn:[],
            move: '.layui-layer-btn',
            moveOut: true,
            success:function (layero,index) {
                pageObj.libraryReady ={};
                pageObj.libraryNotReady ={};
                commonObj.requestData({id:id},"/documentLibrary/checkList","post","json",false,true,function (data) {
                    if(data.code== 200){
                        pageObj.libraryReady = data.data.libraryReadyList;
                        pageObj.libraryNotReady  = data.data.libraryNotReadyList;
                        if (data.data.state == 0){
                            $(layero[0]).find("li[name='haveReadNum']").text("已阅("+data.data.libraryReadyList.length+")");
                            $(layero[0]).find("li[name='notReadNum']").text("未阅("+data.data.libraryNotReadyList.length+")");
                        }else {
                            $(layero[0]).find("li[name='notReadNum']").text("未阅");
                            $(layero[0]).find("li[name='notReadNum']").css("pointer-events", "none")
                            $(layero[0]).find("li[name='haveReadNum']").text("已阅("+data.data.libraryReadyList.length+")");
                        }

                        var myChart = echarts.init($(layero[0]).find("#peopleReadyProportion")[0]);
                        myChart.setOption({
                            "calculable": false,
                            tooltip: {
                                enterable:true,
                                trigger: 'item',
                                formatter: '{a} <br/>{b}: {c} ({d}%)'
                            },
                            color:['#673ab7','#759aa0'],
                            legend: {
                                orient: 'vertical',
                                left: 10,
                                data: ['已阅', '未阅']
                            },
                            series: [
                                {
                                    type:'pie',
                                    radius: ["30%","45%"],
                                    center: ["50%", "50%"],
                                    avoidLabelOverlap: false,
                                    label: {
                                        show: false,
                                        position: 'center'
                                    },
                                    labelLine: {
                                        show: false
                                    },
                                    data:[
                                        {value:data.data.libraryReadyList.length,name:'已阅'},
                                        {value:data.data.libraryNotReadyList.length,name:'未阅'}
                                    ],
                                    itemStyle: {
                                        normal : {
                                            areaStyle: {type: 'default'},
                                            label : {
                                                show : false
                                            },
                                            labelLine : {
                                                show : false
                                            }
                                        },
                                        emphasis : {
                                            label : {
                                                show : true,
                                                position : 'center',
                                                textStyle : {
                                                    fontSize : '14',
                                                    fontWeight : 'bold'
                                                }
                                            }
                                        }
                                    }
                                }
                            ]


                        })

                    }

                });
                commonObj.tabChange3(0);
                layui.use(['element','form'],function () {
                    var form = layui.form;
                    var element = layui.element;
                    element.on('tab(readyFlag)',function (data) {
                        commonObj.tabChange3(data.index);
                    })
                });
            }
        })
    },

    //打开制度新增弹框以及编辑
    addLibraryModal:function (id) {
        pageObj.modalIndex = layer.open({
            type:1,
            title:false,
            zIndex:-9000,
            area: ['65%', '80%'],
            content:$("#libraryModal").html(),
            btn:[],
            move: '.layui-layer-btn',
            moveOut: true,
            success:function (layero,index) {
                pageObj.btnFlag = true;//启用按钮
                //默认图片
                $(layero[0]).find("input[name='thumbnailPictureLink']").val("/img/train/pic_.png");
                $(layero[0]).find(".coursePic").css("background-image", 'url("/img/train/course_pic_default.png")');
                $(layero[0]).find(".deptShow").hide();
                $(layero[0]).find(".roleShow").hide();
                $(layero[0]).find(".workAgeShow").hide();
                $(layero[0]).find(".blackListShow").hide();
                $(layero[0]).find(".whiteListShow").hide();
                $(layero[0]).find(".hybridFlag").hide();
                $(layero[0]).find(".singleFlag").hide();
                $(layero[0]).find("#showDeptId").html("");
                $(layero[0]).find("#showRoleId").html("");
                $(layero[0]).find("#showblacklist").html("");
                $(layero[0]).find("#showWhiteList").html("");

                //清空选中权限
                pageObj.courseSignRangeChooseMap={dept:[], role:[], user:[]};
                pageObj.courseSignRangeMap={dept:[],deptList:[],  role:[], roleList:[], user:[],userList:[]};

                //富文本编辑
                pageObj.courseRemake = KindEditor.create($(layero[0]).find("#libraryContent"), {
                    items:[
                        'source', '|', 'undo', 'redo', '|', 'preview', 'print', 'template', 'code', 'cut', 'copy', 'paste',
                        'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
                        'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
                        'superscript', 'clearhtml', 'quickformat', 'selectall', '|', 'fullscreen', '/',
                        'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
                        'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'image', 'multiimage',
                        'flash', 'media', 'insertfile', 'table', 'hr', 'emoticons', 'baidumap', 'pagebreak',
                        'anchor', 'link', 'unlink', '|', 'about'
                    ],
                    uploadJson:"/editUpload?filePart=library",
                    fileManagerJson:'',
                    allowFileManager: true
                });
                layui.use('form',function () {
                    var form = layui.form;
                    form.render('select');
                    form.render('checkbox');
                    form.render('radio');
                    form.on('checkbox(flag)',function (data) {
                        pageObj.permissionsShowTable(layero,data.value);
                    });
                    form.on('radio(separateFlag)',function (data) {
                        if (data.value==0){
                            $(layero[0]).find(".hybridFlag").show();
                            $(layero[0]).find(".singleFlag").hide();
                            $('input[name=flag]').prop('checked', false);
                            $(layero[0]).find(".deptShow").hide();
                            $(layero[0]).find(".roleShow").hide();
                            $(layero[0]).find(".workAgeShow").hide();
                            $(layero[0]).find(".blackListShow").hide();
                            $(layero[0]).find(".whiteListShow").hide();
                            form.render('checkbox');
                        }else {
                            $(layero[0]).find(".singleFlag").show();
                            $(layero[0]).find(".hybridFlag").hide();
                            $('input[name=flag]').prop('checked', false);
                            $(layero[0]).find(".deptShow").hide();
                            $(layero[0]).find(".roleShow").hide();
                            $(layero[0]).find(".workAgeShow").hide();
                            $(layero[0]).find(".blackListShow").hide();
                            $(layero[0]).find(".whiteListShow").hide();
                            form.render('checkbox');
                        }
                    })
                });
                //加载制度类型
                if (libraryType &&  libraryType.length>0 ){
                    var html ="";
                    html+="<option value=\"\">请选择</option>";
                    $.each(libraryType, function (i,data) {
                        html += "<option value=\""+data.id+"\">"+data.text+"</option>";
                    });
                    $(layero[0]).find("select[name='typeId']").html(html);
                }
                //增加这个可以防止选项被遮挡住
                layui.form.render('select');
                //加载发布人
                if (releaseUser &&  releaseUser.length>0 ){
                    var html ="";
                    html+="<option value=\"\">请选择</option>";
                    $.each(releaseUser, function (i,data) {
                        html += "<option date-deptId=\""+data.deptId+"\" value=\""+data.id+"\">"+data.name+"</option>";
                    });
                    $(layero[0]).find("select[name='releaseUser']").html(html);
                }
                layui.form.render('select');
                //加载制度生效时间。
                pageObj.renderDate(layero);

                //id 有值为编辑 否则为新增
                if (id){
                    $(layero[0]).find(".stockModalTitle").text("编辑制度");
                    $(layero[0]).find("#libraryCode").attr("readonly","readonly");
                    //增加用户、部门、角色缓存  用于编辑
                    pageObj.selectUser();
                    var parpm ={id:id};
                    commonObj.requestData(JSON.stringify(parpm),"/documentLibrary/selectLibrary","post","json",false,false,function (data) {
                        if (data.code==200){
                            for (var attr in data.data.tDocumentLibrary){
                                $(layero[0]).find("input[name ='"+attr+"'][type!='checkbox'][type!='radio'][name !='workAgeMin'][name !='workAgeMax']").val(data.data.tDocumentLibrary[attr] || "");
                                $(layero[0]).find("select[name='"+attr+"']").val(data.data.tDocumentLibrary[attr]);
                                layui.form.render("select");
                                pageObj.courseRemake.html(data.data.tDocumentLibrary["content"]);
                           if (attr=="workAgeFlag" && (data.data.tDocumentLibrary[attr] ==0||data.data.tDocumentLibrary[attr] ==1)  ){
                               $(layero[0]).find(".hybridFlag").show();
                               $(layero[0]).find(".workAgeCheck").prop("checked", "checked");
                               pageObj.permissionsShowTable(layero,5);
                               $(layero[0]).find("input[name='workAgeFlag'][value='"+data.data.tDocumentLibrary["workAgeFlag"]+"']").prop("checked", "checked");
                               if (data.data.tDocumentLibrary["workAgeFlag"] ==0){
                                   $(layero[0]).find("input[name='workAgeMin']").val(data.data.tDocumentLibrary["workAgeMin"]/12);
                                   $(layero[0]).find("input[name='workAgeMax']").val(data.data.tDocumentLibrary["workAgeMax"]/12);
                               }else {
                                   $(layero[0]).find("input[name='workAgeMin']").val(data.data.tDocumentLibrary["workAgeMin"]);
                                   $(layero[0]).find("input[name='workAgeMax']").val(data.data.tDocumentLibrary["workAgeMax"]);
                               }

                           }
                                //编辑附件的渲染
                                if(attr == "attachment"){
                                    if(data.data.tDocumentLibrary[attr] === "") continue;
                                    var affixName = data.data.tDocumentLibrary[attr].split(',');
                                    var affixLink = data.data.tDocumentLibrary["attachmentLink"].split(",");
                                    if (affixName.length>0 && affixLink.length>0){
                                        var html ="";
                                        for (var i=0;i<affixName.length ;i++){
                                            var filePath = affixLink[i];
                                            var fileName = affixName[i];
                                            pageObj.fileMap.push(fileName);
                                            pageObj.fileLink.push(filePath);
                                            html +="<div id='file"+i+"'>";
                                            html += "<span>" + fileName + "</span>&nbsp;&nbsp;&nbsp;&nbsp;";
                                            html += "<a href=" + filePath + " target=_blank  download="+fileName+">下载:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                            html +="<a onclick='pageObj.deleteFile(this,"+data.data.tDocumentLibrary["id"]+")' datab='file"+i+"' data-id='"+fileName+"' data='"+filePath+"' > 删除:</a>&nbsp;&nbsp;|&nbsp;&nbsp;";
                                            var fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase() ;
                                            var strFilter=".jpeg|.gif|.jpg|.png|.bmp|.pic|" ;
                                            if (fileName.indexOf(".")>-1){
                                                var str=fileExt + '|';
                                                if (strFilter.indexOf(str)>-1){
                                                    html += "<img alt='" + fileName + "' src='"+filePath+"' height='61.8px' width='100px' onclick='openImage(this,\"imgModal\")'><br/>";

                                                }else {
                                                    html += "<a onclick='filePreview(this)' data-id='" + filePath + "'>预览:</a><br/>";

                                                }
                                            }else {
                                                html += "<a onclick='filePreview(this)' data-id='" + filePath + "'>预览:</a><br/>";

                                            }
                                            html +="</div>";
                                        }
                                        $(layero[0]).find("#affixDiv").append(html);
                                    }
                                }
                            }
                            if(data.data.selectRangId){
                                for(var attr in data.data.selectRangId){
                                   if ((attr == "1" || attr == "2") && data.data.selectRangId[attr].length>0){
                                       $(layero[0]).find("input[name='separateFlag'][value='0']").prop("checked", "checked");
                                       $(layero[0]).find(".hybridFlag").show();
                                       if (attr=="1"){
                                           $(layero[0]).find(".deptCheck").prop("checked", "checked");
                                           data.data.selectRangId[attr].forEach(function (d) {
                                               pageObj.courseSignRangeChooseMap["dept"].push(d);
                                           });
                                           pageObj.btnEvaluationSubmit($(layero[0]).find("#savaDeptId"),data.data.selectRangId[attr]);

                                       }else if (attr=="2"){
                                           $(layero[0]).find(".roleCheck").prop("checked", "checked");
                                           data.data.selectRangId[attr].forEach(function (d) {
                                               pageObj.courseSignRangeChooseMap["role"].push(d);
                                           });
                                           pageObj.btnEvaluationSubmit($(layero[0]).find("#saveRoleId"),data.data.selectRangId[attr]);
                                       }
                                   }else if ((attr=="3" || attr=="4") && data.data.selectRangId[attr].length>0){
                                       $(layero[0]).find("input[name='separateFlag'][value='1']").prop("checked", "checked");
                                       $(layero[0]).find(".singleFlag").show();
                                       if (attr=="3"){
                                           $(layero[0]).find(".blackCheck").prop("checked", "checked");
                                           data.data.selectRangId[attr].forEach(function (d) {

                                               pageObj.courseSignRangeChooseMap["user"].push(d);
                                           });
                                           pageObj.btnEvaluationSubmit($(layero[0]).find("#savablacklist"),data.data.selectRangId[attr]);

                                       }else if (attr =="4") {
                                           $(layero[0]).find(".whiteCheck").prop("checked", "checked");
                                           data.data.selectRangId[attr].forEach(function (d) {
                                               pageObj.courseSignRangeChooseMap["user"].push(d);
                                           });
                                           pageObj.btnEvaluationSubmit($(layero[0]).find("#savaWhiteList"),data.data.selectRangId[attr]);

                                       }

                                   }
                                    layui.form.render("radio");
                                    layui.form.render("checkbox");
                                    pageObj.permissionsShowTable(layero,attr);
                                }
                            }

                        }
                    },true);
                }else {
                    $(layero[0]).find(".stockModalTitle").text("添加制度");

                }
            }
        })

    },
    //权限按钮的控制
    permissionsShowTable:function (layero,data) {
        $(layero[0]).find(".permissions").show();
        if (data==1 && $(".deptCheck").is(":checked")){
            $(layero[0]).find(".deptShow").show();
        } else if (data == 2 && $(".roleCheck").is(":checked")){
            $(layero[0]).find(".roleShow").show();
        }else if (data == 3 && $(".blackCheck").is(":checked")){
            $(layero[0]).find(".blackListShow").show();
            $(layero[0]).find(".whiteListShow").hide();
            $('.whiteCheck').prop('checked', false);
            layui.form.render('checkbox');
        }else if (data == 4 && $(".whiteCheck").is(":checked")){
            $(layero[0]).find(".whiteListShow").show();
            $(layero[0]).find(".blackListShow").hide();
            $('.blackCheck').prop('checked', false);
            layui.form.render('checkbox');
        }else if (data == 5 && $(".workAgeCheck").is(":checked")){
            $(layero[0]).find(".workAgeShow").show();
        }else if(data==1){
            $(layero[0]).find(".deptShow").hide();
        }else if(data==2){
                $(layero[0]).find(".roleShow").hide();
        }else if(data==3){
            $(layero[0]).find(".blackListShow").hide();
        }else if(data==4){
            $(layero[0]).find(".whiteListShow").hide();
        }else if(data==5){
            $(layero[0]).find(".workAgeShow").hide();
        }
    },
    //加载发布人
    renderReleaseUser:function () {
      commonObj.requestData(null,pageObj.releaseUser,"post","json",false,false,function (data) {
          if (data.data.userList && data.data.userList.length>0){
              var html="";
              releaseUser=data.data.userList;
              html+="<option value=\"\">请选择</option>";
              $.each(data.data.userList , function (i,data) {
                  html += "<option date-deptId=\""+data.deptId+"\" value=\""+data.id+"\">"+data.name+"</option>";
                  userList["user"][data.id]=data.name;
                  userList["dept"][data.deptId]=data.deptName
              });
              $("#schemeView").find("select[name='releaseUser']").html(html);
          }
      },true);

    },
    //加载制度生效时间
    renderDate:function (layero) {
        var laydate = layui.laydate;
        //制度生效开始时间
        var effectiveStartTime ={
            elem: $(layero[0]).find("input[name='effectiveStartTime']")[0],
            istime:true,
            type:'datetime',
            format:'yyyy-MM-dd HH:mm:ss',
            done:function (value,data) {

            }
        };
        laydate.render(effectiveStartTime);

        //制度生效结束时间
        var effectiveEndTime ={
            elem: $(layero[0]).find("input[name='effectiveEndTime']")[0],
            istime:true,
            type:'datetime',
            format:'yyyy-MM-dd HH:mm:ss',
            done:function (value,data) {

            }
        };
        laydate.render(effectiveEndTime);
    },
    //新增制度弹框，新增按钮。
    submitLibrary:function (t,btnState) {
        var $form = $(t).closest(".stockModalCommon").find("form");
        if(!$form.valid()){
            return;
        }
        var param = new FormData($form[0]);
        if($($form[0]).find("select[name='typeId']").val() == "" ){
                layer.msg("请选择制度类型！", {time: 2000, icon: 5});
                return;
        }

        if($($form[0]).find("select[name='level']").val() == "" ){
            layer.msg("请选择制度等级！", {time: 2000, icon: 5});
            return;
        }

        if($($form[0]).find("select[name='releaseUser']").val() == "" ){
            layer.msg("请选择制度发布人！", {time: 2000, icon: 5});
            return;
        }
        var regu = /^[A-Z]/;
        if (!regu.exec($($form[0]).find("input[name='version']").val())){
                layer.msg("请输入正确的版次", {time: 2000, icon: 5});
                return;
        }
        pageObj.courseRemake.sync();//同步数据后可以直接取得textarea的value
        param.append("content",$form.find("textarea[name = 'libraryContent']").val() || "" );
        if (!$form.find("textarea[name = 'libraryContent']").val()){
                layer.msg("请填写制度内容！", {time: 2000, icon: 5});
                return;
        }
        param.append("documentPermissionDetailsList",JSON.stringify(pageObj.courseSignRangeChooseMap));
        // param["documentPermissionDetailsList"]=pageObj.courseSignRangeChooseMap;
        param.append("releaseDept",$form.find("select[name='releaseUser'] option:selected").attr("date-deptId"));
        param.append("state",$("input[name=state]").val());
        param.append("btnState",btnState);
        param.append("workAgeFlag",$("input[name=workAgeFlag]").val());
        layer.confirm('确认提交?',{
                btn:['确认','取消'],
            },function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
            $.ajax({
                type: "post",
                url: "/documentLibrary/addLibrary",
                data: param,
                dataType: "json",
                async: true,
                cache: false,
                contentType: false,
                processData: false,
                success: function (data) {
                    if (data.code == 200) {
                        pageObj.libraryTable();
                        pageObj.putStockModalClose();
                        layer.msg(data.data.message);
                    } else if (data.code == 1002) {
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                }
            });
        })
    },
    deleteFile:function (t,id) {
        var file = $(t).attr("data-id");
        var fileLink = $(t).attr("data");
        var datab = $(t).attr("datab");
        pageObj.fileMap.remove(file);
        pageObj.fileLink.remove(fileLink);
        var fl= pageObj.fileMap.join(',');
        var link = pageObj.fileLink.join(',');
        var id = id;
        commonObj.requestData({file:fl,fileLink:link,id:id},"/documentLibrary/deleteFile","post","json",false,true,function (data) {
            if (data.code==200){
                $("#"+datab+"").empty();
                layer.msg(data.data.message);
            }else if (data.code == 1002) {
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
            }
        })
    },
    //制度权限，渲染权限
    courseSignRangeModalShow:function (t) {
        var b = $(t).attr("data-divId");
        var title = "";
        var signStr = "";
        var defaultGroupName = "";
        if (b=="showDeptId"){
            title = "制度有效范围(部门)";
            signStr = "dept";
            defaultGroupName = "父级部门名称";
        }else if(b=="showRoleId"){
            title = "制度有效范围(角色)";
            signStr = "role";
            defaultGroupName = "角色类型名称";
            if(pageObj.roleTypeMap && Object.getOwnPropertyNames(pageObj.roleTypeMap).length < 1){
                commonObj.requestData({typeCode:"ROLE_TYPE"}, "/dict/listByTypeCode2", "get", "json", false,true,function (data) {
                    if(data && data.length > 0){
                        $.each(data, function (x, roleType) {
                            pageObj.roleTypeMap[roleType.code] = roleType.name;
                        });
                    }
                });
            }
        }else if(b=="showblacklist"){
            title = "制度无效范围(用户)";
            signStr = "user";
            defaultGroupName = "用户部门名称";
        }else if(b =="showWhiteList"){
            title = "制度有效范围(用户)";
            signStr = "user";
            defaultGroupName = "用户部门名称";
        } else {
            title = "";
            signStr = "";
        }
        if (signStr){
            //如果没有值，则请求，并进行缓存
            var param = {signStr:signStr};
            if(pageObj.courseSignRangeMap[signStr+"List"] && pageObj.courseSignRangeMap[signStr].length<1 ){
                commonObj.requestData(param,"/documentLibrary/listPermissions","post","json",false,true,function (data) {
                    $.each(data,function (i,d) {
                        pageObj.courseSignRangeMap[signStr+"List"].push(d);
                        pageObj.courseSignRangeMap[signStr][d.itemValue]=d.name;
                    });

                })
            }
            new SysUserCompont({
                title:title,
                url:"/documentLibrary/listPermissions",
                param:param,
                defaultGroupName: defaultGroupName,
                dataList: pageObj.courseSignRangeMap[signStr+"List"],
                roleTypeMap: pageObj.roleTypeMap,
                chooseDataList:pageObj.courseSignRangeChooseMap[signStr],
                resultCallBack:function (dataList) {
                    pageObj.courseSignRangeChooseMap[signStr] = [];
                    $.each(dataList, function (zj, item) {
                        pageObj.courseSignRangeChooseMap[signStr].push(item);
                    });
                    pageObj.btnEvaluationSubmit(t,dataList);
                },

            });
        }

    },
    // 将选中中的权限，渲染到新增弹框列表
    btnEvaluationSubmit:function (t,data) {
        var b = $(t).attr("data-divId");
        var btnList = "";
        if (b=="showDeptId"){
            if (data && Object.getOwnPropertyNames(data).length>0){
                data.forEach(function (value) {
                    var deptId = value;
                    var deptName =pageObj.courseSignRangeMap["dept"][value];
                    var $span = '<div  data-divId="'+b+'"  deptId="'+deptId+'" title="'+deptName+'" class="userDivClass">\n' +
                        '            <div class="userClass">'+deptName+'</div>\n' +
                        '        </div>';
                    btnList += $span;
                    $("#putStockModalFrom [name='showDeptId']").html(btnList);
                });
            }
        }else if(b=="showRoleId"){
            if (data && Object.getOwnPropertyNames(data).length>0){
                data.forEach(function (value) {
                    var role = value;
                    var roleName =pageObj.courseSignRangeMap["role"][value];
                    var $span = '<div data-divId="'+b+'"  deptId="'+role+'" title="'+roleName+'" class="userDivClass">\n' +
                        '            <div class="userClass">'+roleName+'</div>\n' +
                        '        </div>';
                    btnList += $span;
                    $("#putStockModalFrom [name='showRoleId']").html(btnList);
                });
            }
        }else if(b=="showblacklist"){
            if (data && Object.getOwnPropertyNames(data).length>0) {
                data.forEach(function (value) {
                    var userId = value;
                    var userName =pageObj.courseSignRangeMap["user"][value];
                    var $span = '<div data-divId="'+b+'"  deptId="'+userId+'" title="'+userName+'" class="userDivClass">\n' +
                        '            <div class="userClass">'+userName+'</div>\n' +
                        '        </div>';
                    btnList += $span;
                    $("#putStockModalFrom [name='showblacklist']").html(btnList);

                })
            }
        }else if(b=="showWhiteList"){
            if (data && Object.getOwnPropertyNames(data).length>0) {
                data.forEach(function (value) {
                    var userId = value;
                    var userName =pageObj.courseSignRangeMap["user"][value];
                    var $span = '<div  data-divId="'+b+'"  deptId="'+userId+'" title="'+userName+'" class="userDivClass">\n' +
                        '            <div class="userClass">'+userName+'</div>\n' +
                        '        </div>';
                    btnList += $span;
                    $("#putStockModalFrom [name='showWhiteList']").html(btnList);

                })
            }
        }

    },
    //新增制度类型。
    addTypeBtn:function () {
        if ($('#addName').val()==""){
            layer.msg("类型名称不能为空！", {time: 2000, icon: 5});
            return;
        }

        $('#addOperation-dialog').modal('hide')
        //静态添加节点
        var parentNode = $('#schemeTree').treeview('getSelected');

        var node = {
            text: $('#addName').val()
        };

        $('#schemeTree').treeview('addNode', [node, parentNode]);
        parentNode.forEach(function (d) {
           node["parentId"] = d.id;
        });

        commonObj.requestData(JSON.stringify(node),"/documentLibrary/addType","post","json",false,false,function (data) {
            if (data.code==200){
                pageObj.initTree();
                pageObj.libraryTable();
                pageObj.putStockModalClose();
                layer.msg(data.data.message);
            }else if (data.code == 1002){
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
            }else {
                if (getResCode(data))
                    return;
                $("#addOperation-dialog").modal('hide');
            }

        },true);
    },
    //编辑制度类型
    editTypeModal:function (t) {
        if (t==1){
            $("#myModalLabel").html("编辑");
            $("#Save").hide();
            $("#edit").show();
            var node = $('#schemeTree').treeview('getSelected');
            if (node.length == 0) {
                swal( '请选择要修改的节点');
                return;
            };
            node.forEach(function (v) {
                $('#addName').val(v.text);
            });
            $('#addOperation-dialog').modal('show');
        }else {
            $("#Save").show();
            $("#edit").hide();
            $("#myModalLabel").html("新增");
            $('#addName').val('');
            $('#addOperation-dialog').modal('show');
        }
    },
    //编辑制度类型确认按钮
    editTypeBtn:function () {
        if ($('#addName').val()==""){
            layer.msg("类型名称不能为空！", {time: 2000, icon: 5});
            return;
        }
        $('#addOperation-dialog').modal('hide');
        var parentNode = $('#schemeTree').treeview('getSelected');
        var  param ={};
        param["typeId"] = parentNode[0].id;
        param["typeName"] = $("#addName").val();
        commonObj.requestData(JSON.stringify(param),"/documentLibrary/editTypeName","post","json",false,false,function (data) {
            if (data.code==200){
                pageObj.initTree();
                pageObj.libraryTable();
                pageObj.putStockModalClose();
                layer.msg(data.data.message);
            }else if (data.code == 1002){
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
            }else {
                if (getResCode(data))
                    return;
                $("#addOperation-dialog").modal('hide');
            }

        },true);
    },
    //删除制度类型
    delTypeBtn:function () {
        var parentNode = $('#schemeTree').treeview('getSelected');
        if (parentNode.length == 0) {
            swal( '请选择要删除的节点');
            return;
        };
        pageObj.typeId=[];
        var typeId =parentNode[0].id
        pageObj.typeId.push(typeId);
        pageObj.selectType(parentNode,function (d) {
            return d.id;
        });
        var parm={typeId1:pageObj.typeId};
        if (pageObj.queryType()){
            layer.confirm('确认删除？',{
                btn:['删除','取消'],
            },function (index) {
                layer.close(index);
                layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
                commonObj.requestData(JSON.stringify(parm),"/documentLibrary/delTypeName","post","json",false,false,function (data) {
                    if (data.code==200){
                        pageObj.initTree();
                        pageObj.libraryTable();
                        layer.msg(data.data.message);
                    }else if (data.code == 1002) {
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    }
                },true)
            })
        }
        
    },
    //将制度类型的子节点类型进行封装成一个数组
    selectType:function (arr,f) {
        arr.forEach(function (d) {
            if (d.nodes && d.nodes.length>0){
                d.nodes.forEach(function (v) {
                    var group = f(v);
                    pageObj.typeId.push(group);
                });
              return pageObj.selectType(d.nodes,function (d) {
                  return d.id;
              });
            }
        });

    },
    //制度类型删除判断，其子类型是否包含制度
    queryType:function () {
        var flag = false;
        var pama={};
        pama["typeArr"]=pageObj.typeId;
        //判断所选择的节点类型中及子类型下是否有制度
        commonObj.requestData(JSON.stringify(pama),"/documentLibrary/selectTypeFlag","post","json",false,false,function (data) {
            if (data.code == 200){
                if (data.data.map1.sum>0){
                    flag=false;
                    swal({
                        title:"友情提醒",
                        text: "选择要删除的类型下含有制度，不可删除",
                    });
                    return ;
                }else
                    flag =true;
            }
        },true);
        return flag;
    },
    updateLibraryState:function (id) {
        commonObj.requestData(id,"/documentLibrary/updatefailure","post","json",false,false,function (data) {
            if (data.code==200){
                pageObj.libraryTable();
            }else if(data.code== 1002){
                swal({
                    title: "异常提示",
                    text: data.msg,
                });
            }
        },true)
    },
    updatefailure:function (id) {
      layer.confirm('确定该制度失效？',{
          btn:['失效','取消'],
      },function (index) {
          layer.close(index);
          layer.msg("正在处理中，请稍后。",{time:3000,shade:[0.7,'#393D49']});
          commonObj.requestData(id,"/documentLibrary/updatefailure","post","json",false,false,function (data) {
              if (data.code==200){
                  pageObj.libraryTable();
                  layer.msg(data.data.message);
              }else if(data.code== 1002){
                  swal({
                      title: "异常提示",
                      text: data.msg,
                  });
              }
          },true)
      })
    },

    //删除制度
    delLibrary:function (id) {
        layer.confirm('确认删除？',{
            btn:['删除','取消'],
        },function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
            commonObj.requestData(id,"/documentLibrary/delLibrary","post","json",false,false,function (data) {
                if (data.code==200){
                    pageObj.libraryTable();
                    layer.msg(data.data.message);
                }else if(data.code== 1002){
                    swal({
                        title: "异常提示",
                        text: data.msg,
                    });
                }
            },true)
        });
    },
    //加载制度缩略图
    thumbnailList:function (value) {
        var param = $("#documentLibraryForm").serializeJson();
        param['typeId'] =pageObj.libraryTypeId ? pageObj.libraryTypeId.id : '';
        commonObj.requestData(param,"/documentLibrary/getLibraryTotal","post","json",false,true,function (data) {
            if (data && data.code==200){
                commonObj.pagerPlus({
                    elem: $(".putStockListPager1"),
                    count:data.data.total,
                    url: "/documentLibrary/selectLibraryList",
                    target: $(".stockCheckList1"),
                    param: param,
                }, pageObj.selectthumbListHtml);
            }
        });
        // commonObj.pagerPlus({
        //     elem:$(".putStockListPager"),
        //     count:10,
        //     url:"/documentLibrary/selectLibraryList",
        //     target: $(".stockCheckList"),
        //     param: {typeId: value ? value.id : '' },
        // },pageObj.selectthumbListHtml);
    },
    //制度缩略图列表
    selectthumbListHtml:function (data) {
        var html="";
        var typeName ="";
        $.each(data.list,function (i,d){
                $.each(libraryType, function (i,data) {
                    if (data.id == d.typeId){
                        typeName = data.text;
                    }
                });
                if (fls){
                    html +="<div class=\"col-sm-4 iboxtable iboxtable"+d.id+"\"  style=\"background-image:url("+d.thumbnailPictureLink+");background-repeat:inherit;\">\n" +
                        " <div class=\"ibox-con\">\n" +
                        "        <div class=\"ibox-tit\">\n" +
                        "            <input value="+typeName+" class=\"form-control\" style=\"font-size: 11px;height: 21px;border: unset;  \" readonly/>\n" +
                        "        </div>\n" +
                        "        <div class='textOver' style=\"text-align: center;\">\n" +
                        "            <ul>\n" +
                        "                <b><span style=\"font-size: 20px; \"> <a  href=/documentLibrary/library/view/"+d.id+">"+d.libraryName+"</a></span></b>\n" +
                        "                <li>\n" +
                        "                    <span style=\"\">发布人："+userList["user"][d.releaseUser]+"</span>\n" +
                        "                    <span style=\"\">发布时间："+new Date(d.releaseTime).format("yyyy-MM-dd")+"</span>\n" +
                        "                </li>\n" +
                        "            </ul>\n" +
                        "        </div>\n" +
                        "    </div>\n"
                        +  "    <div class=\"ibox-foot\">\n" +
                        "        <ul>\n" +
                        "            <li>\n" +
                        "                <div style=\"float: left; color: white ;\"><span>No."+d.libraryCode+"</span> </div>\n" +
                        "                <div style=\"float: left; color: white; margin-left: 73px;\"><span>版次:"+d.version+"</span></div>\n" +
                        "                <div style=\"float: right; color: white\"><span>等级:"+d.level+"</span></div>\n" +
                        "            </li>\n" +
                        "        </ul>\n" +
                        "    </div>\n"
                        +"</div>"
                }else {
                    html +="<div class=\"col-sm-4 iboxtable iboxtable"+d.id+"\"  style=\"background-image:url("+d.thumbnailPictureLink+");background-repeat:inherit;\">\n" +
                        " <div class=\"ibox-con\">\n" +
                        "        <div class=\"ibox-tit\">\n" +
                        "            <input value="+typeName+" class=\"form-control\" style=\"font-size: 11px;height: 21px;border: unset;  \" readonly/>\n" +
                        "        </div>\n" +
                        "        <div class='textOver' style=\"text-align: center;\">\n" +
                        "            <ul>\n" +
                        "                <b><span style=\"font-size: 20px; \"> <a href=/documentLibrary/library/view/"+d.id+">"+d.libraryName+"</a></span></b>\n" +
                        "                <li>\n" +
                        "                    <span style=\"\">发布人："+userList["user"][d.releaseUser]+"</span>\n" +
                        "                    <span style=\"\">发布时间："+new Date(d.releaseTime).format("yyyy-MM-dd")+"</span>\n" +
                        "                </li>\n" +
                        "            </ul>\n" +
                        "        </div>\n" +
                        "    </div>\n"
                        +"</div>"
                }


            $(".iboxtable"+d.id).css("background-image","url("+d.thumbnailPictureLink+")");

        });
        $("#picList").html(html);
    },
    selectUser:function () {
        var arr = ["user","dept","role"];
        arr.forEach(function (d) {
            var signStr=d;
            var param = {signStr:signStr};
            commonObj.requestData(param,"/documentLibrary/listPermissions","post","json",false,true,function (data) {
                $.each(data,function (i,d) {
                    pageObj.courseSignRangeMap[signStr+"List"].push(d);
                    pageObj.courseSignRangeMap[signStr][d.itemValue]=d.name;
                });

            })
        })

    }
};
// function systemUser() {
//     var userIds ;
//     $.ajax({
//         type:"get",
//         url:baseUrl+"/sysConfig/getAllConfig",
//         dataType:"json",
//         async:false,
//         success:function (data) {
//             if (data && data.systemUser){
//                 userIds=data.systemUser.value;
//             }
//         }
//     });
//     return userIds;
// }

function systemUser() {
    var len = user.roles.length;
    for (var i = 0 ; i<len ; i++){
        if(user.roles[i].type == 'ZD' && user.roles[i].code == 'ZY'){
            return true;
        }
    }
    return false;
}

//表格内容过长截取
function tdTitle(){
    $('th').each(function(index,element){
        $(element).attr('title',$(element).text());
    });
    $('td').each(function(index,element){
        $(element).attr('title',$(element).text());
    });
};
