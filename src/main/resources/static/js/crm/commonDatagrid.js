function newTab(url, title) {
    var tabs = $('.page-tabs-content', top.document);
    // alert(tabs.length);
    if (tabs == null || tabs == undefined || tabs.length == 0) {
        location.href = url;
    } else {
        $('.page-tabs-content', top.document).find("a").each(function (i, d) {
            $(d).removeClass("active");
        });
        $('#content-main', top.document).find("iframe").each(function (i, d) {
            $(d).hide();
        });
        if ($(tabs).html().indexOf(url) < 0) {
            var tab = $("<a href='javascript:;' class='active J_menuTab' data-id='" + url + "'>" + title + " <i class=\"fa fa-times-circle\"></i></a>");
            tabs.append(tab);
        } else {
            $('.page-tabs-content', top.document).find("a").each(function (i, d) {
                if ($(this).attr("data-id") == url) {
                    $(this).addClass("active");
                }
            });
        }
        var iframe = '<iframe class="J_iframe" name="iframe0" width="100%" height="100%" src="' + url + '" frameborder="0" data-id="' + url + '" seamless="seamless"></iframe>';
        $('#content-main', top.document).append(iframe);
    }
}

function dataGrid(gridName,gridObject,pagerName,searchFormName){
    //表格ID
    this.gridName = gridName;
    //表格对象
    this.gridObject = gridObject;
    //表格分页ID
    this.pagerName = pagerName;
    //查询区域表单ID
    this.searchFormName = searchFormName;
    this.grid = $("#"+this.gridName);
    //保存跨页选择的数据
    this.allPageSelectedList = [];
    this.defaultParams = {};
    $.extend(true,this.defaultParams,gridObject.postData);
}

dataGrid.prototype = {

    getOneRow: function(rowId){
        return this.grid.jqGrid('getRowData', rowId);
    },
    //加载表格
    loadGrid: function(){
        var t = this;
        $.jgrid.defaults.styleUI = 'Bootstrap';
        //自适应宽度
        $(window).bind('resize', function () {
            var tableElement = $('#' + t.gridName);
            var width = tableElement.closest('.jqGrid_wrapper').width();
            tableElement.setGridWidth(width);
        });
        this.gridObject.postData = $.extend(true,this.gridObject.postData,$("#" + this.searchFormName).serializeJson());
        $("#" + this.gridName).jqGrid(this.gridObject);
    },
    //搜索
    search: function(page){
        this.allPageSelectedList = [];
        page = page ? page : 1;
        var params = $("#" + this.searchFormName).serializeJson();
        $.extend(true,params,this.defaultParams);
        var oldPostData = $("#"+this.gridName).jqGrid("getGridParam", "postData");
        if(oldPostData){
            $.each(oldPostData,function (k, v) {
                delete oldPostData[k];
            });
        }
        jQuery("#" + this.gridName).jqGrid('setGridParam',{url:this.gridObject.url,postData:params,page: page}).trigger("reloadGrid");
    },
    //刷新表格
    reflush:function(){
        //刷新时要获取到当前页码再搜索
        //this.search($("#"+this.gridName).jqGrid("getGridParam", "postData").page);
        this.allPageSelectedList = [];
        var oldPostData = $("#"+this.gridName).jqGrid("getGridParam", "postData");
        jQuery("#" + this.gridName).jqGrid('setGridParam',{url:this.gridObject.url,postData:oldPostData}).trigger("reloadGrid");
    },
    //导出查询到的内容
    exportAll: function(exportUrl,data){
        //查询条件
        var params =  removeBlank($("#" + this.searchFormName).serializeJson());
        //附加参数
        if(data){
            $.extend(params,data);
        }
        location.href = exportUrl+"?"+$.param(params);
    },
    //添加上方的操作按钮
    addOptionButton: function(buttons,buttonsArea){
        var classes = ["btn-success","btn-primary","btn-default","btn-warning","btn-danger"];
        if($("#"+buttonsArea+" .buttonsId").length < 1){
            var htmlStr = "<br/><div class='buttonsId' style='width:100%;height:25px;clear:left;'>" +
                "</div>";
            $("#"+buttonsArea).css({height:"80px"}).append(htmlStr);
        }
        var buttonsEle = $("#"+buttonsArea+" .buttonsId");
        for(var i=0;i<buttons.length;i++){
            var b = buttons[i];
            var cla = classes[i%5];
            buttonsEle.append(("<a class='btn ${cla}' style='margin-left:3px;' href="+b.href+">"+b.text+"</a>").replace("${cla}",cla));
        }
        buttonsEle.find("a").eq(0).css({clear:"left"});
    },
    //设置下面的操作按钮
    setNavGrid: function(var1,var2){
        var op = var1 || {edit: false, add: false, del: false, search: false};
        var op2 = var2 || {height: 200, reloadAfterSubmit: true};
        $("#" + this.gridName).jqGrid('navGrid', '#' + this.pagerName, op, op2);
    },
    //合并单元格
    mergerCell: function(cellName,primaryKey){
        var gridName = this.gridName;
        //得到显示到界面的id集合
        var ids = $("#" + gridName).getDataIDs();
        //当前显示多少条
        var length = ids.length;
        for (var i = 0; i < length; i++) {
            //从上到下获取一条信息
            var before = $("#" + gridName).jqGrid('getRowData', ids[i]);
            //定义合并行数
            var rowSpanTaxCount = 1;
            for (var j = i+1; j <= length; j++) {
                //和上边的信息对比 如果值一样就合并行数+1 然后设置rowspan 让当前单元格隐藏
                var end = $("#" + gridName).jqGrid('getRowData', ids[j]);
                if (before[primaryKey] == "")
                    break;
                if (before[primaryKey] == end[primaryKey]) {
                    rowSpanTaxCount++;
                    //$("#" + gridName).setCell(ids[j], cellName, '', {display: 'none'});
                    $("#"+cellName+ids[j]).hide();
                } else {
                    rowSpanTaxCount = 1;
                    break;
                }
                $("#" + cellName + ids[i]).attr("rowspan", rowSpanTaxCount);
            }
        }
    },
    //跨页选择
    /**
     * 设置选中行，在翻页时需要把之前选中的重新选中
     */
    setSelected: function(row){
        var row = $("#"+this.gridName).find("#"+row);
        row.attr("aria-selected","true").addClass("success").find("input[type='checkbox']").click();
    },
    /**
     * 获取单页内选中行
     */
    getSelecteds: function(){
        var rowList = [];
        var that = this;
        //获取多选到的id集合
        var ids = $("#"+this.gridName).jqGrid("getGridParam", "selarrrow");
        //遍历访问这个集合
        $(ids).each(function (index, id){
            //由id获得对应数据行
            var row = $("#"+that.gridName).jqGrid('getRowData', id);
            rowList.push(row);
        });
        return rowList;
    },
    /**
     * 获取单页内未选择行
     */
    getNoSelecteds :function(id){
        var noSelected = [];
        var arr = this.getSelecteds();
        var rows = this.grid.jqGrid('getRowData');//获取当前页面数据
        for(var i=0;i<rows.length;i++){
            var b = false;
            for(var j=0;j<arr.length;j++){
                if(arr[j][id] == rows[i][id]){
                    b = true;
                }
            }
            if(!b){
                noSelected.push(rows[i]);
            }
        }
        return noSelected;
    },
    /**
     * 通过键删除
     * @param id
     * @param row
     */
    removeById :function(id,row){
        for(var i=0;i<this.allPageSelectedList.length;i++){
            if(this.allPageSelectedList[i][id] == row[id]){
                this.allPageSelectedList.splice(i,1);
            }
        }
    },
    /**
     * 保存当前页面选中的数据在allPageSelectedList中
     */
    setPageSelected: function(id){
        var re_page = this.grid.getGridParam('page');//获取返回的当前页
        var arr = this.getSelecteds();
        var noSelected = this.getNoSelecteds(id);

        for(var i=0;i<arr.length;i++){
            if(!this.getExists(id,arr[i])){
                this.allPageSelectedList.push(arr[i]);
            }else{
                this.removeById(id,arr[i]);
                this.allPageSelectedList.push(arr[i]);
            }
        }
        for(var i=0;i<noSelected.length;i++){
            if(this.getExists(id,noSelected[i])){
                this.removeById(id,noSelected[i]);
            }
        }
    },
    /**
     * 判断数据是否被选中过
     * @param id
     * @param row
     * @returns {boolean}
     */
    getExists :function(id,row){
        for(var i=0;i<this.allPageSelectedList.length;i++){
            if(this.allPageSelectedList[i][id] == row[id]){
                return true;
            }
        }
        return false;
    },
    /**
     * 得到选中的数据并重新在页面上勾选
     */
    getPageSelectedSet: function(id){
        var rows = this.grid.jqGrid('getRowData');//获取当前页面数据
        for(var i=0;i<rows.length;i++){
            if(this.getExists(id,rows[i])){
                this.setSelected(i+1);
            }
        }
    },
    /**
     * 得到所有跨页选择到的数据
     */
    getAllPageSelected: function(id){
        this.setPageSelected(id);
        return this.allPageSelectedList;
    }
    //跨页选择

};