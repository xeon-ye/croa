//页面初始化函数
$(function () {

});

var companyObj = {
    companySearchUrl: "/company/companySearch",
    checkCompanyUrl:  "/company/checkCompany",
    currentCompanyName:"",
    firstPageTotal: 0, //第一页查询缓存表数据总数
    renderCompanyItem:function (page, pageSize, companyList) {
        var html = "";
        if(companyList && companyList.length > 0){
            $.each(companyList, function (m, company) {
               html += " <div class=\"companyItem\">\n" +
                   "         <div title=\"\">\n" +
                   "             <div class=\"ellipsisContent\">\n" +
                   "                 <input type=\"checkbox\" lay-skin=\"primary\" lay-filter=\"radioInput\" value=\""+(company.companyName || "")+"\"/>\n" +
                   "             </div>\n" +
                   "         </div>\n" +
                   "         <div title=\""+(page == 1 ? (m+1) : (companyObj.firstPageTotal + (page - 2) * pageSize + m + 1))+"\">\n" +
                   "             <div class=\"ellipsisContent\">\n" +
                   "                 "+(page == 1 ? (m+1) : (companyObj.firstPageTotal + (page - 2) * pageSize + m + 1))+"\n" +
                   "             </div>\n" +
                   "         </div>\n" +
                   "         <div title=\""+(company.companyName || "")+"\">\n" +
                   "             <div class=\"ellipsisContent\">\n" +
                   "                 "+(company.companyName || "")+"\n" +
                   "             </div>\n" +
                   "         </div>\n" +
                   "         <div title=\""+(company.companyLegal || "")+"\">\n" +
                   "             <div class=\"ellipsisContent\">\n" +
                   "                 "+(company.companyLegal || "")+"\n" +
                   "             </div>\n" +
                   "         </div>\n" +
                   "         <div title=\""+(company.establishDate || "")+"\">\n" +
                   "             <div class=\"ellipsisContent\">\n" +
                   "                 "+(company.establishDate || "")+"\n" +
                   "             </div>\n" +
                   "         </div>\n" +
                   "         <div title=\""+(company.companyStatus || "")+"\">\n" +
                   "             <div class=\"ellipsisContent\">\n" +
                   "                 "+(company.companyStatus || "")+"\n" +
                   "             </div>\n" +
                   "         </div>\n" +
                   "         <div title=\""+(company.registerNum || "")+"\">\n" +
                   "             <div class=\"ellipsisContent\">\n" +
                   "                 "+(company.registerNum || "")+"\n" +
                   "             </div>\n" +
                   "         </div>\n" +
                   "         <div title=\""+(company.creditCode || "")+"\">\n" +
                   "             <div class=\"ellipsisContent\">\n" +
                   "                 "+(company.creditCode || "")+"\n" +
                   "             </div>\n" +
                   "         </div>\n" +
                   "     </div>";
            });
        }
        return html;
    },
    search:function (layero) {
        $(layero[0]).find(".tbodyWrap").html("");
        var keyword = $(layero[0]).find("input[name='keyword']").val();
        if(!keyword){
            layer.msg("请输入关键字搜索！", {time: 2000, icon: 5});
            return;
        }
        layui.use('flow', function(){
            var flow = layui.flow;
            flow.load({
                elem: $(layero[0]).find(".tbodyWrap"),
                isAuto: true,
                done: function(page, next){
                    //从 layui 1.0.5 的版本开始，page是从1开始返回，初始时即会执行一次done回调。
                    //请求数据
                    var param = {keyword:keyword};
                    param.page = page; //页码
                    param.size = 20; //每页数据条数
                    companyObj.requestData(param, companyObj.companySearchUrl, "post", "json", false, function (data) {
                        //第一页是从缓存表拿数据，记录数据总数
                        if(page == 1){
                            companyObj.firstPageTotal = data.total;
                        }
                        console.log(companyObj.firstPageTotal);
                        next(companyObj.renderCompanyItem(page, param.size, data.list), page < data.pages); //如果小于总页数，则继续
                        //复选框选中事件
                        layui.use('form', function(){
                            var form = layui.form;
                            form.render(); //先渲染，然后执行下面复选框事件才有效

                            form.on('checkbox(radioInput)', function(data){
                                if(data.elem.checked){
                                    var child = $(data.elem).closest('.tbodyWrap').find('.companyItem input[type="checkbox"]');
                                    child.each(function(index, item){
                                        item.checked = false;
                                    });
                                    data.elem.checked = true;
                                    companyObj.currentCompanyName = data.value;
                                }else {
                                    companyObj.currentCompanyName = "";
                                }
                                form.render('checkbox');
                            });
                        });
                    });

                }
            });
        });
    },
    companySearch: function () {
        layer.open({
            type: 1,
            title: "企业信息搜索",
            zIndex: 10,
            content: $("#companyListModal").html(),
            btn: ['确定','取消'],
            area: ['70%', '80%'],
            shadeClose: true,
            resize: false,
            move: true,
            moveOut: true,
            success: function(layero, index){
                //搜索框添加回车事件
                $(layero[0]).find("input[name='keyword']").keypress(function (event) {
                    if(event.keyCode == '13' || event.keyCode == 13){
                        companyObj.search(layero);
                    }
                });
                //搜索按钮绑定点击事件
                $(layero[0]).find(".input-group-addon").click(function () {
                        companyObj.search(layero);
                });
            },
            yes: function (index, layero) {
                if(!companyObj.currentCompanyName){
                    layer.msg("请选择记录后操作！", {time: 2000, icon: 5});
                    return;
                }
                layer.close(index);
                $("#companyName").val(companyObj.currentCompanyName);
            }
        });
    },
    checkCompany: function () {
        var keyword = $("#companyName").val();
        if(!keyword){
            layer.msg("请输入公司名称进行校验！", {time: 2000, icon: 5});
            return;
        }
        companyObj.requestData({keyword:keyword}, companyObj.checkCompanyUrl, "post", "json", true, function (data) {
            if(data.code == 200){
                layer.msg("公司校验成功！", {time: 2000, icon: 6});
            }else {
                layer.msg(data.msg, {time: 2000, icon: 5});
            }
        });
    },
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
}






