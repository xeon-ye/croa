var newsPage = 1;
$(function () {
    initNews();
    //搜索
    $("#search").click(function () {
        $("#queryValue").val($("#queryName").val())
        newsPage = 0;
        initNews();
    })
})


function initNews(){
    $.ajax({
        type : 'POST',
        url :  baseUrl + "/news/list?page="+newsPage,
        data : $("#newsFormData").serialize(),
        dataType : 'json',
        success : function(pageData) {
            var pages = pageData.pages;
            var pageNum = pageData.pageNum;
            var pageSize = pageData.size;
            var totalRows = pageData.total;
            $("#listpage")
                .bs_pagination(
                    {
                        currentPage : pageNum,
                        totalPages : pages,
                        totalRows : totalRows,
                        visiblePageLinks : 8,
                        rowsPerPage : pageSize,
                        showGoToPage : false,
                        showRowsPerPage : false,
                        showRowsInfo : false,
                        showRowsDefaultInfo : false,
                        navListContainerClass : "col-xs-12 col-sm-12 col-md-12",//使分页按钮区域占一整行
                        containerClass : "",//包含按钮的class为空，原为well，有背景
                        onLoad : function(event, data) {//分页按钮加载时
                            showPageList(pageData);
                        },
                        onChangePage : function(event, data) {//分页按钮改变时
                            newsPage = data.currentPage;
                            initNews();
                        }
                    });
        },error:function(){
        }
    });
}
function showPageList(pageData) {
    $("#newsList").empty();
    var html="";
    $(pageData.list).each(function (i,item) {
        html+="";
        html+="<tr>";
        html+="<th>"+tranferType(item.type)+"</th>";
        html+="<th title='"+item.title+"' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'><a href=/news/view/"+item.id+">"+item.title+"</th>";
        html+="<th>"+item.createTime+"</th>";
        html+="</tr>";
    })
    $("#newsList").prepend(html);
}

function tranferType(type){
    if(type === 1){
        return "<span class='jituan'>集团新闻</span>";
    }else if(type ===2){
        return "<span class='gongsi'>公司新闻</span>";
    }else if(type ===3){
        return "图文新闻";
    }else if(type ===4){
        return "<span class='youxiu'>优秀事迹</span>";
    }else{
        return "数据异常";
    }
}