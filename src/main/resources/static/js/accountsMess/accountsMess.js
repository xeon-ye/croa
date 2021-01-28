var offerSum = 0; //选中报价合计
var session=0 ; // 选中进账合计
var cost = 0;
var messSum = 0;
$(function () {
    layui.use('form',function () {
        var form = layui.form;
        form.on('select(releaseUser)', function(data){
            pagObj.accountsMessList();
        });
        form.on('select(state)', function(data){
            pagObj.accountsMessList();
        });


    });
    pagObj.accountsMessList();
    if (getQueryString("id") != null && getQueryString("id") != "" && getQueryString("id") != undefined){
        pagObj.addAccountsMess(getQueryString("id"), getQueryString("flag"));
    }
    //对layui表格复选框点击行数据选中。
    $(document).on("click", ".layui-table-body table.layui-table tbody tr", function () {
        var index = $(this).attr('data-index');
        var tableBox = $(this).parents('.layui-table-box');
        //存在固定列
        if (tableBox.find(".layui-table-fixed.layui-table-fixed-l").length > 0) {
            tableDiv = tableBox.find(".layui-table-fixed.layui-table-fixed-l");
        } else {
            tableDiv = tableBox.find(".layui-table-body.layui-table-main");
        }
        var checkCell = tableDiv.find("tr[data-index=" + index + "]").find("td div.laytable-cell-checkbox div.layui-form-checkbox I");
        if (checkCell.length > 0) {
            checkCell.click();
        }
    });
//对td的单击事件进行拦截停止，防止事件冒泡再次触发上述的单击事件（Table的单击行事件不会拦截，依然有效）
    $(document).on("click", "td div.laytable-cell-checkbox div.layui-form-checkbox", function (e) {
        e.stopPropagation();
    });
    var laydate = layui.laydate;
    //制度生效开始时间
    var applyStateTime ={
        elem:'#applyStateTime',
        istime:false,
        type:'date',
        istoday:true,
        format:'yyyy-MM-dd',
        done:function (value,data) {
            $("#applyStateTime").val(value);
            pagObj.accountsMessList();
        }
    };
    laydate.render(applyStateTime);
    var applyEndTime ={
        elem:'#applyEndTime',
        istime:false,
        istoday:true,
        type:'date',
        format:'yyyy-MM-dd',
        done:function (value,data) {
            $("#applyEndTime").val(value);
            pagObj.accountsMessList();

        }
    };
    laydate.render(applyEndTime);
    pagObj.applyName();

});

var commonObj ={
    imageUpload:null,
    enterEvent:function (event) {
        if (event.keyCode == '13' || event.keyCode ==13){
            pagObj.accountsMessList();
        }
    },

};

var pagObj ={
    checkedArticleList:[],//缓存选中的稿件
    articleList:{},
    checkedDocking:{},
    articlePageNum:[],
    messState:{0:"正常","-1":"已驳回",2:"已保存",1:"已完成",4:"部门领导审核",5:"业务总监审核",6:"财务总监审核"},
    modalIndex:null,
    putStockModalClose:function(){
        if (pagObj.modalIndex){
            layer.close(pagObj.modalIndex);
        }else {
            layer.closeAll();
        }
    },
    closeModal:function () {
        layer.closeAll();
        pagObj.accountsMessList();
    },
    submitAudit:function (layero) {
        var param =$(layero[0]).find("#messDetailsForm").serializeForm();
        if ($(layero[0]).find("#title").val() == ""){
            layer.msg("请输入烂账标题！", {time: 2000, icon: 5});
            return;
        }
        layer.confirm("确认提交？",{
            btn:['确认','取消'],
        },function (index) {
            layer.close(index);
            $(layero[0]).find("#btnSubmitAudit").attr("disabled",true);
            layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
            $.ajax({
                type:'post',
                url:'/accountMess/addMessList',
                data:param,
                dataType:"json",
                success:function (data) {
                    $(layero[0]).find("#btnSubmitAudit").attr("disabled",false);
                    if(data.code == 200){
                        pagObj.putStockModalClose();
                        pagObj.accountsMessList();
                        layer.msg(data.data.message);
                    }else if (data.code == 1002){
                        swal({
                            title:"异常提示",
                            text:data.msg,
                        })
                    }
                },
                error: function (data) {
                    Ladda.stopAll();
                }
            })
        })

    },
    //新增烂账点击
    //flag 2为审核 4 为查看详情
    addAccountsMess:function (id,flag) {
        pagObj.modalIndex = layer.open({
            type:1,
            title:false,
            zIndex:100000,
            area: ['89%', '89%'],
            content:$("#accountsMessModal").html(),
            btn:[],
            closeBtn:false,
            move:'.layui-layer-btn',
            moveOut:true,
            success:function (layero,index) {
                if (id){
                    $(layero[0]).find("#firstDiv").hide();
                    $(layero[0]).find("#accountsMessArtic").hide();
                    $(layero[0]).find("#messDetails").show();
                    var param = {id:id};
                    $.ajax({
                        type:'post',
                        url:'/accountMess/selectMessDetails',
                        data:JSON.stringify(param),
                        dataType:"json",
                        async:true,
                        cache:false,
                        contentType:false,
                        processData:false,
                        success:function (data) {
                            if (data.code == 200){
                                for(var attr in data.data.accountsMess){
                                    $(layero[0]).find("input[name= '"+attr+"'] ").val(data.data.accountsMess[attr] || "");
                                    $(layero[0]).find("span[name = '"+attr+"']").text(data.data.accountsMess[attr]);
                                    if (attr == "note"){
                                        $(layero[0]).find("textarea[name= '"+attr+"'] ").val(data.data.accountsMess[attr] || "");
                                    }
                                    if (attr == "state"){
                                        var state;
                                        var dataState = data.data.accountsMess[attr];
                                        switch (dataState){
                                            case -1:
                                                state  = "审核驳回";
                                                break;
                                            case 0:
                                                state = "正常";
                                                break;
                                            case 1:
                                                state = "已完成";
                                                break;
                                            case 2:
                                                state = "已保存";
                                                break;
                                            case 4:
                                                state = "部门领导审核";
                                                break;
                                            case 5:
                                                state = "业务总监审核";
                                                break;
                                            case 6:
                                                state = "财务总监审核";
                                                break;
                                            default :
                                                break;
                                        }
                                        if (state != null){
                                            $(layero[0]).find("input[name='stateQC']").val(state);
                                        }
                                    }
                                }
                                if (flag == 2){
                                    $(layero[0]).find("#reject1").show();
                                    $(layero[0]).find("#pass1").show();
                                    $(layero[0]).find("#btnSubmitAudit").hide();
                                    $(layero[0]).find("#title").attr("readonly","readonly");

                                    $(layero[0]).find("#pass1").off('click').on('click',function () {
                                        pagObj.pass(layero)
                                    });
                                    $(layero[0]).find("#reject1").off('click').on('click',function () {
                                        pagObj.reject(layero)
                                    })
                                }else if (flag == 4){
                                    $(layero[0]).find("#reject1").hide();
                                    $(layero[0]).find("#pass1").hide();
                                    $(layero[0]).find("#btnSubmitAudit").hide();
                                    $(layero[0]).find("#title").attr("readonly","readonly");
                                } else {
                                    $(layero[0]).find("#reject1").hide();
                                    $(layero[0]).find("#pass1").hide();
                                    $(layero[0]).find("#btnSubmitAudit").show();
                                }

                                pagObj.checkedArticleList = data.data.articleList;
                                pagObj.checkedArticleListTable(layero);
                                $(layero[0]).find("#btnSubmitAudit").off("click").on('click',function () {
                                    pagObj.submitAudit(layero);
                                });


                            }


                        }

                    })

                }else {
                    pagObj.checkedArticleList =[];
                    pagObj.checkedDocking ={};
                    //加载该用户的客户列表
                    pagObj.dockingList(layero);
                    $(layero[0]).find("#accountsMessArtic").hide();
                    $(layero[0]).find("#messDetails").hide();

                    $(layero[0]).find("#custSearch").off("click").on('click',function () {
                        pagObj.dockingList(layero);
                    })
                }
            }
        })
    },
    //当前用户客户列表
    dockingList:function (layero) {
        $(layero[0]).find("#accountsMessArtic").hide();
        $(layero[0]).find("#firstDiv").show();
        $(layero[0]).find("#messDetails").hide();
        var param = $(layero[0]).find("#firstForm").serializeJson();
        var id = $(layero[0]).find("#dockingListTable")[0];
        layui.use(['table','laypage'],function () {
            var form = layui.form;
            var laypage = layui.laypage;
            var table = layui.table;
            form.render('checkbox');
            table.render({
                elem:id,
                method:'post',
                where:param,
                autoSort: false,
                url:"/accountMess/dockingListTable",
                title:"客户列表",
                totalRow:false,
                cols:[[
                    {type:'radio'},
                    {field: 'dockingId', title: 'ID',  sort: false,hide:true},
                    {field: 'id', title: 'ID',  sort: false,hide:true},
                    {field: 'companyName', title: '客户公司名称', sort: false,unresize:false},
                    {field: 'typeText', title: '客户类型', sort: false, unresize:false,templet:function (d) {
                        if (d.type == 1) {
                            return "<span style='color:#1ab394'>企业客户</span>";
                        } else if (d.type == 0) {
                            return "<span>个人客户</span>";
                        } else {
                            return "";
                        }
                    }},
                    {field: 'standardizeText', title: '是否标准', sort: false, unresize:false,templet:function (d) {
                        if (d.standardize == 1) {
                            return "<span style='color:#1ab394'>标准</span>";
                        } else if(d.standardize == 0) {
                            return "<span class='text-red'>非标准</span>";
                        } else {
                            return "";
                        }
                    }},
                    {field: 'custName', title: '对接人', sort: false,  unresize:false},
                    {field: 'normalizeText', title: '是否规范', sort: false,templet:function (d) {
                        if (d.normalize == 1) {
                            return "<span style='color:#1ab394'>规范</span>";
                        } else if(d.normalize == 0) {
                            return "<span class='text-red'>非规范</span>";
                        } else {
                            return "";
                        }
                    }},
                    {field: 'protectStrong', title: '强弱保护', sort: false,templet:function (d) {
                        if (d == 1) {
                            return "<span style='color:#1ab394'>强保护</span>";
                        } else {
                            return "弱保护";
                        }
                    }
                    },
                    {field: 'startTime', title: '开始时间', sort: false,templet:function (d) {
                        return '<div class="ellipsisContent textOver">'+ new Date(d.startTime).format("yyyy-MM-dd hh:mm") + '</div>';
                    }},
                    {field: 'endTime', title: '结束时间',sort: false,templet:function (d) {
                        return  '<div class="ellipsisContent textOver">' +new Date(d.endTime).format("yyyy-MM-dd hh:mm") +'</div>';
                    }},
                    {field: 'state', title: '状态', sort: false,templet:function (d) {
                        var d = d.state;
                        if (d == 1){
                            return "<span style='color:#1ab394'>有效</span>";
                        }else{
                            return "<span style='color:red'>失效</span>"
                        }
                    }},
                ]],
                page: true,
                done:function (res,curr, count) {
                    tdTitle();
                    $('th').css({'color': 'black','font-weight':'bold'});

                    for (var i = 0 ;i <res.data.length; i++){
                        if (res.data[i].custId == pagObj.checkedDocking["dockingId"]){
                            //翻页选中
                            $(layero[0]).find('tr[data-index=' + i + '] input[type="radio"]').trigger("click");
                        }
                    }

                    //客户选择 下一步点击事件
                    $(layero[0]).find("#selectSupplier").off("click").on('click',function () {
                        if ($.isEmptyObject(pagObj.checkedDocking)){
                            layer.msg("请先选择客户", {time: 2000, icon: 5});
                            return;
                        }else{
                            //加载未回款、部分回款稿件列表
                            pagObj.selectArticleList(layero);
                            pagObj.readerDate(layero);

                        }
                    })

                }
            });
            table.on('radio(test1)',function (obj) {
                pagObj.checkedDocking["custName"] = obj.data.custName;
                pagObj.checkedDocking["dockingId"] = obj.data.dockingId;
                pagObj.checkedDocking["companyName"] = obj.data.companyName;
                pagObj.checkedDocking["companyId"] = obj.data.id;
            });
            table.on('row(test1)' ,function (obj) {
                //选中行样式
                obj.tr.addClass('layui-table-click').siblings().removeClass('layui-table-click');
                //选中radio样式
                obj.tr.find('i[class="layui-anim layui-icon"]').trigger("click");
            })
        })
    },
    //加载时间控件
    readerDate:function (layero) {
        var laydate = layui.laydate;
        //制度生效开始时间
        var releaseTime ={
            elem:$(layero[0]).find("#releaseTime")[0],
            istime:false,
            type:'date',
            istoday:true,
            format:'yyyy-MM-dd',
            done:function (value,data) {
                $(layero[0]).find("#releaseTime").val(value);
                pagObj.selectArticleList(layero);
            }
        };
        laydate.render(releaseTime);
        var releaseTime1 ={
            elem:$(layero[0]).find("#releaseTime1")[0],
            istime:false,
            istoday:true,
            type:'date',
            format:'yyyy-MM-dd',
            done:function (value,data) {
                $(layero[0]).find("#releaseTime1").val(value);
                pagObj.selectArticleList(layero);
            }
        };
        laydate.render(releaseTime1);
    },
    //选中申请烂账列表
    checkedArticleListTable:function (layero) {
        var id = $(layero[0]).find("#checkedArticleTable")[0];
        var AListData =[];
        pagObj.checkedArticleList.forEach(function (t) {
            if (t !=null){
                AListData.push(t);
            }
        });
        layui.use(['table','laypage'],function () {
            var form = layui.form;
            var laypage = layui.laypage;
            var table = layui.table;
            form.render('checkbox');
            table.render({
                elem:id,
                method:'post',
                autoSort: false,
                title:"烂账",
                data:AListData,
                totalRow:false,
                cellMinWidth: 101,
                cols:[[
                    {field: 'artId', title: 'ID',  sort: false,hide:true},
                    {field: 'companyName', title: '客户公司名称', sort: false, unresize:false},
                    {field: 'dockingName', title: '客户联系人', sort: false, unresize:false},
                    {field: 'title', title: '标题', sort: false, templet:function (d) {
                        return  '<div class="ellipsisContent textOver">'+d.title +'</div>';
                    }},
                    {field: 'brand', title: '品牌', sort: false, },
                    {field: 'userName', title: '业务员',  sort: false},
                    {field: 'mediaName', title: '媒体名称', sort: false},
                    {field: 'innerOuter', title: '内外部',  sort: false},
                    {field: 'channel', title: '频道',  sort: false},
                    {field: 'mediaUserName',title: '媒介', sort: false},
                    {field: 'num', title: '数量',sort: false},
                    {field: 'issuedDate',  title: '发布日期', sort: false,templet:function (d) {
                        return  '<div class="ellipsisContent textOver">'+  new Date(d.issuedDate).format("yyyy-MM-dd hh:mm") + '</div>';
                    }},
                    {field: 'saleAmount',title: '应收（报价）', sort: false},
                    {field: 'incomeAmount', title: '回款金额', sort: false},
                    {field: 'outgoAmount', title: '成本', sort: false},
                ]],
                page: true,
                done:function () {
                    tdTitle();
                    $('th').css({'color': 'black','font-weight':'bold'});

                }
            });
        })

    },
    //烂账申请列表
    accountsMessList:function () {
        var param = $("#queryForm").serializeJson();
            // param['typeId'] =value ? value.id : '';
        layui.use(['table','laypage'], function(){
            var table = layui.table;
            var laypage= layui.laypage;
            table.render({
                elem:'#demo1',
                method:'post',
                where:param,
                autoSort: false,
                url:"/accountMess/accountMessListTable",
                title:"制度类表",
                totalRow:false,
                // toolbar:true,
                cols:[[
                    {type: 'numbers',title:'序号', fixed: 'left'},
                    {field: 'id', title: 'ID',  sort: false, fixed: 'left',hide:true},
                    {field: 'code', title: '烂账编号',  align: 'center', sort: false, fixed: 'left'},
                    {field: 'title', title: '标题', sort: false, fixed: 'left'},
                    {field: 'applyName', title: '申请人', sort: false, fixed: 'left'},
                    {field: 'applyTime', title: '申请日期', sort: false, fixed: 'left'},
                    {field: 'custCompanyName', title: '客户公司名称', sort: false, fixed: 'left'},
                    {field: 'custName', title: '联系人', sort: false, fixed: 'left'},
                    {field: 'messSum', title: '烂账合计金额', sort: false, fixed: 'left'},
                    {field: 'costSum', title: '成本合计金额', sort: false, fixed: 'left'},
                    {field: 'state', title: '状态', sort: false, fixed: 'left',templet:function (d) {
                        switch (d.state){
                            case -1:
                                return "<span style = 'color:red'>"+pagObj.messState[d.state]+"</span>";
                            case 0:
                                return "<span style = 'color:red'>"+pagObj.messState[d.state]+"</span>";
                            case 1:
                                return "<span style = ''>"+pagObj.messState[d.state]+"</span>";
                            case 2:
                                return "<span style =''>"+pagObj.messState[d.state]+"</span>";
                            case 4:
                                return "<span style = 'color:red'>"+pagObj.messState[d.state]+"</span>";
                            case 5:
                                return "<span style = 'color:red'>"+pagObj.messState[d.state]+"</span>";
                            case 6:
                                return "<span style = 'color:red'>"+pagObj.messState[d.state]+"</span>";

                        }
                    }},
                    {field:'',title:'操作',sort:false, fixex : 'left',templet:function (d) {
                        var btnHtml = "";
                        if (d.state != 2 ){
                            btnHtml += " <button data-state='"+d.state+"' class=\"tableButton blueBtn\" type=\"button\" onclick='pagObj.showHistory("+d.id+")'>\n" +
                                "            审核详情\n" +
                                "        </button>";
                        }
                        if ((d.state ==2 || d.state==-1) && d.applyId == user.id){
                            btnHtml += " <button data-state='"+d.state+"' class=\"tableButton blueBtn\" type=\"button\" onclick='pagObj.addAccountsMess("+d.id+")'>\n" +
                                "            编辑\n" +
                                "        </button>";
                        }
                        if((d.state ==2 || d.state==-1) && d.applyId == user.id){
                            btnHtml += " <button data-state='"+d.state+"' class=\"tableButton orangeBtn\" type=\"button\"  onclick='pagObj.deleteMess("+d.id+")'>\n" +
                                "            删除\n" +
                                "        </button>";
                        }


                        if ( d.applyId == user.id &&  d.state>2){
                            btnHtml += " <button data-state='"+d.state+"' class=\"tableButton blueBtn\" type=\"button\" onclick='pagObj.returnBack(" + "\"" + d.taskId + "\"," + d.itemId + ")'>\n" +
                                "            撤回\n" +
                                "        </button>";
                        }
                        return btnHtml;
                    }
                    }
                ]],
                page:{theme: '#1c84c6'},
                done:function (layero,index) {
                    $('th').css({'color': 'black','font-weight':'bold'});
                }

            });
            table.on('rowDouble(test)',function (obj) {
                pagObj.addAccountsMess(obj.data.id,4);
            });



        })
    },
    //符合烂账稿件列表
    selectArticleList:function (layero) {
        pagObj.checkedArticleList =[];
         offerSum = 0; //选中报价合计
         session=0 ; // 选中进账合计
         cost = 0;
         messSum = 0;
        $(layero[0]).find("#saleSum2").text(0);
        $(layero[0]).find("#incomeSum2").text(0);
        $(layero[0]).find("#outgoSum2").text(0);
        $(layero[0]).find("#messSum").text(0);
        $(layero[0]).find("#accountsMessArtic").show();
        $(layero[0]).find("#firstDiv").hide();
        $(layero[0]).find("#messDetails").hide();
        var id = $(layero[0]).find("#articleTable")[0];
        var param = $(layero[0]).find("#accountsMessArticForm").serializeJson();
        param["companyId"] = pagObj.checkedDocking["companyId"],
            param["dockingId"] = pagObj.checkedDocking["dockingId"],
        layui.use(['table','laypage','form'], function(){
            var form = layui.form;
            var table = layui.table;
            form.render('checkbox');
            table.render({
                elem:id,
                method:'post',
                where:param,
                autoSort: false,
                url:"/accountMess/accountsMessList",
                title:"烂账",
                totalRow:false,
                cols:[[
                    {type:'checkbox'},
                    {field: 'artId', title: 'ID',  sort: false,hide:true},
                    {field: 'companyName', title: '客户公司名称', sort: false, width: 200,unresize:false},
                    {field: 'dockingName', title: '客户联系人', sort: false, width: 120, unresize:false},
                    {field: 'title', title: '标题',width: 120, sort: false, templet:function (d) {
                        return '<div class="ellipsisContent textOver">' +d.title +'</div>';
                    }},
                    {field: 'brand', title: '品牌', sort: false,  width: 120},
                    {field: 'userName', title: '业务员',sort: false},
                    {field: 'mediaName', title: '媒体名称',  width: 120,sort: false},
                    {field: 'innerOuter', title: '内外部',sort: false},
                    {field: 'channel', title: '频道', sort: false},
                    {field: 'mediaUserName',  title: '媒介', sort: false},
                    {field: 'num', title: '数量',sort: false},
                    {field: 'issuedDate',  width: 120,title: '发布日期', sort: false,templet:function (d) {
                        return  '<div class="ellipsisContent textOver">'+ new Date(d.issuedDate).format("yyyy-MM-dd hh:mm")+'</div>';
                    }},
                    {field: 'saleAmount',title: '应收（报价）', sort: false},
                    {field: 'incomeAmount',title: '回款金额', sort: false},
                    {field: 'outgoAmount',  title: '成本', sort: false},
                ]],
                page: true,
                done:function (res,curr, count) {
                    tdTitle();
                    $('th').css({'color': 'black','font-weight':'bold'});

                    var len = res.data.length;
                    //緩存稿件分頁當前頁碼
                    pagObj.articlePageNum =curr;
                    //缓存页码上的稿件
                    pagObj.articleList[curr] = {};
                    pagObj.articleList[curr] =res.data;
                    //给选中的稿件增加checked
                    var chooseNum = 0;
                    if (pagObj.checkedArticleList.length){
                        for (var i=0; i<res.data.length;i++){
                            for (var j = 0 ;j<pagObj.checkedArticleList.length ;j++){
                                if (pagObj.checkedArticleList[j]!=null && res.data[i].artId ==pagObj.checkedArticleList[j].artId){
                                    $(layero[0]).find('tr[data-index=' + i + '] input[type="checkbox"]').attr('checked', true);
                                    $(layero[0]).find('tr[data-index=' + i + '] input[type="checkbox"]').next().addClass('layui-form-checked');
                                    chooseNum ++;
                                }
                            }
                        }
                    }
                    if (len != 0 && chooseNum == len){
                        $(layero[0]).find('input[lay-filter="layTableAllChoose"]').prop('checked', true);
                        $(layero[0]).find('input[lay-filter="layTableAllChoose"]').next().addClass('layui-form-checked');
                    }

                    // 下一步点击事件
                    $(layero[0]).find("#invoice").off("click").on('click',function () {
                        var list=[];
                        pagObj.checkedArticleList.forEach(function (t) {
                            if (t!==null){
                                list.push(t);
                            }
                        });
                        if (list.length >0 ){
                            //将选中稿件信息填入数据库  及生成烂账code
                            pagObj.addMessInformation(layero);

                        }else {
                            layer.msg("请选择烂账稿件！", {time: 2000, icon: 5});
                            return;
                        }
                    });

                    //上一步
                    $(layero[0]).find("#backStepOne").off("click").on('click',function () {
                        pagObj.checkedDocking ={};
                        pagObj.checkedArticleList=[];
                        pagObj.dockingList(layero);

                    });
                    $(layero[0]).find("#search").off("click").on('click',function () {
                        pagObj.selectArticleList(layero);
                    })
                }
            });
            table.on('checkbox(test1)', function(obj){

                if (obj.checked && obj.type =="one"){
                    //单选中一条稿件
                    pagObj.checkedArticleList.push(obj.data);
                    offerSum += parseFloat(obj.data.saleAmount);
                    session += parseFloat(obj.data.incomeAmount);
                    cost += parseFloat(obj.data.outgoAmount);


                }else if (obj.checked && obj.type =="all"){
                    pagObj.checkedArticleList=[];
                    offerSum=0;
                    session= 0;
                    cost = 0;
                    // 全选
                        pagObj.articleList[pagObj.articlePageNum].forEach(function (d) {
                            offerSum += parseFloat(d.saleAmount);
                            session += parseFloat(d.incomeAmount);
                            cost += parseFloat(d.outgoAmount);
                            pagObj.checkedArticleList.push(d);
                        })
                }else if (!obj.checked && obj.type =="one" ) {
                    //取消单选
                    pagObj.checkedArticleList.forEach(function (d,i) {
                        if (d.artId == obj.data.artId){
                            offerSum =offerSum - parseFloat(obj.data.saleAmount);
                            session =session - parseFloat(obj.data.incomeAmount);
                            cost =cost - parseFloat(obj.data.outgoAmount);
                            delete pagObj.checkedArticleList[i]

                        }
                    })
                }else if (!obj.checked && obj.type =="all"){
                    //取消全选
                    if (pagObj.articleList[pagObj.articlePageNum] != null && pagObj.checkedArticleList!=null ){
                        for (var i =0 ; i< pagObj.articleList[pagObj.articlePageNum].length ;i++){
                            for (var j =0 ; j< pagObj.checkedArticleList.length ; j++){
                                if (pagObj.checkedArticleList[j] !=null && pagObj.articleList[pagObj.articlePageNum][i].artId == pagObj.checkedArticleList[j].artId){
                                    offerSum =offerSum - parseFloat(pagObj.checkedArticleList[j].saleAmount);
                                    session =session - parseFloat(pagObj.checkedArticleList[j].incomeAmount);
                                    cost =cost - parseFloat(pagObj.checkedArticleList[j].outgoAmount);
                                    delete pagObj.checkedArticleList[j];
                                }
                            }
                        }

                    }
                }
                //合计计算金额
                $(layero[0]).find("#saleSum2").text(offerSum.toFixed(2));
                $(layero[0]).find("#incomeSum2").text(session.toFixed(2));
                $(layero[0]).find("#outgoSum2").text(cost.toFixed(2));
                messSum =offerSum - session;
                $(layero[0]).find("#messSum").text(messSum.toFixed(2));
            });

        })

    },
    //下一步
    addMessInformation:function (layero) {
        var param ={};
        var  artId =[];
        pagObj.checkedArticleList.forEach(function (t) {
            if (t!=null){
                artId.push(t.artId);
            }
        });
        param["artId"] = artId;
        param["offerSum"] =offerSum;
        param["session"] = session;
        param["cost"] = cost;
        param["messSum"] =messSum;
        param["custName"] = pagObj.checkedDocking["custName"];
        param["dockingId"] =  pagObj.checkedDocking["dockingId"];
        param["companyName"] = pagObj.checkedDocking["companyName"];
        param["companyId"] = pagObj.checkedDocking["companyId"];
        param["releaseStateTime1"] =$(layero[0]).find("input[name='releaseStateTime1']").val();
        param["releaseEndTime1"] = $(layero[0]).find("input[name='releaseEndTime1']").val()
        layer.confirm('确认选中稿件信息？',{
            btn:['确定','取消'],
        },function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
            $.ajax({
                type:"post",
                url:"/accountMess/saveMessArticle",
                data:JSON.stringify(param),
                dataType:"json",
                async:true,
                cache:false,
                contentType:false,
                processData: false,
                success:function (data) {
                    if (data.code==200){
                        for (var attr in data.data.accountsMess){
                            $(layero[0]).find("input[name = '"+attr+"']").val(data.data.accountsMess[attr]);
                            $(layero[0]).find("span[name = '"+attr+"']").text(data.data.accountsMess[attr]);
                        }
                        pagObj.checkedArticleListTable(layero);
                        $(layero[0]).find("#reject1").hide();
                        $(layero[0]).find("#pass1").hide();
                        $(layero[0]).find("#btnSubmitAudit").show();
                        $(layero[0]).find("#accountsMessArtic").hide();
                        $(layero[0]).find("#messDetails").show();
                        $(layero[0]).find("#btnSubmitAudit").off("click").on('click',function () {
                            pagObj.submitAudit(layero);
                        })


                    }else if (data.code==1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    }
                }
            })
        })

    },
    //审核详情
    showHistory:function (id) {
        $("#historyModal").modal({backdrop:"static"});
        var process = 32
        $.ajax({
            type:"post",
            url:"/process/history",
            data:{dataId:id,process:process},
            dataType:"json",
            success:function (data) {
                if (data.code == 200) {
                    $("#history").empty();
                    if (data.data.data != null) {
                        var html = "";
                        html += "<div style='position: relative;z-index: 10;'>" +
                            "<div class='form-control'>" +
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
                        html += "</div><div class='col-sm-12 text-center' style='position:relative'><img src='/process/getImage?dataId=" + id + "&process="+process+"&t=" + new Date().getTime() + "' style='width: 100%; margin-top: 8px; margin-bottom: -100px; margin-left: 38px;'/></div>";
                        $("#history").append(html);
                    }
                } else {
                    if (getResCode(data))
                        return;
                }
            }
        })

    },
    //烂账删除
    deleteMess:function (id){
        layer.confirm('确认删除？',{
            btn:['删除','取消'],
        },function (index) {
            layer.close(index);
            layer.msg("正在处理中，请稍候。", {time: 3000, shade: [0.7, '#393D49']});
            $.ajax({
                type:"post",
                data:{id:id},
                url:"/accountMess/deleteMess",
                dataType:"json",
                success:function (data) {
                    if (data.code==200){
                        pagObj.accountsMessList();
                        layer.msg(data.data.message);
                    }else if (data.code==1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    }
                    
                }
            })
        })

    },
    //烂账撤回
    returnBack:function (taskId,itemId) {
    var lock = true;
    layer.confirm('确认撤回申请？',{
        btn:["撤回","取消"],
        shade:false,
    },function(index){
        layer.close(index);
        layer.msg("正在处理中，请稍候。", {time: 1500, shade: [0.7, '#393D49']});
        if(lock){
            lock = false ;
            $.ajax({
                type: "post",
                url: baseUrl + "/process/withdraw",    //向后端请求数据的url
                data: {taskId: taskId, itemId: itemId},
                dataType: "json",
                success: function (data) {
                    if (data.code == 200) {
                        swal(data.data.message);
                        pagObj.accountsMessList();
                    } else if(data.code == 1002){
                        swal({
                            title: "异常提示",
                            text: data.msg,
                        });
                    } else {
                        if (getResCode(data))
                            return;
                    }
                }
            });
        }
    },function () {
        return;
    })
    },
    reject: function (layero) {
        approveTask($(layero[0]).find("input[name='taskId']").val(),0,$(layero[0]).find("#reject1")[0].id,null);
    },
    pass: function (layero) {
        approveTask($(layero[0]).find("input[name='taskId']").val(),1,$(layero[0]).find("#pass1")[0].id,null);

    },
    //加载申请人
    applyName:function () {
        $.ajax({
            type:"post",
            url:"/documentLibrary/releaseUser",
            dataType:"json",
            async:true,
            success:function (data) {
                layui.use('form',function () {
                    var form = layui.form;

                if (data.data.userList && data.data.userList.length>0){
                    var html="";
                    html+="<option value=\"\">请选择</option>";
                    $.each(data.data.userList , function (i,data) {
                        html += "<option date-deptId=\""+data.deptId+"\" value=\""+data.id+"\">"+data.name+"</option>";
                    });
                    $("#queryForm").find("select[name='releaseUser']").html(html);
                }
                form.render('select');
                });
            }
        })
    }


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