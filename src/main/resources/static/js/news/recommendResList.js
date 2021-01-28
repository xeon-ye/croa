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
        url :  baseUrl + "/recommendRes/list?page="+newsPage,
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
        html+="<td title='"+item.resourceTitle+"' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'><a href='/recommendRes/view/"+item.id+"/false'>"+item.resourceTitle+"</td>";
        html+="<td title='"+item.title+"' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'><a href=\""+item.href+"\" target='_blank'>"+item.title+"</td>";
        html+="<td>"+item.plateName+"</td>";
        html+="<td>"+item.createTime+"</td>";
        html+="</tr>";
    })
    $("#newsList").prepend(html);
}
