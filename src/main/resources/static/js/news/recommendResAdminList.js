//编辑
function editNews(id){
    $.ajax({
        type : 'GET',
        url :  baseUrl + "/recommendRes/"+id,
        dataType : 'json',
        success : function(data) {
            if(data.code === 200){
                //富文本编辑框初始化
                $('#editResourceContent').summernote({
                    lang: 'zh-CN',
                    height: 300
                    // width: 900
                });
                $("#editNews").modal("show");
                $("#editTitle").val(data.data.data.title);
                $("#editPlateId").val(data.data.data.plateId);
                $("#editId").val(data.data.data.id);
                $("#editResourceTitle").val(data.data.data.resourceTitle);
                $("#editHref").val(data.data.data.href);
                $("#editResourceContent").code(data.data.data.content);
            }else{
                sweetAlert(data.msg)
            }
        }
    });
}
//删除
function del(id){
    $.ajax({
        type : 'delete',
        url :  baseUrl + "/recommendRes/"+id,
        dataType : 'json',
        success : function(data) {
            if(data.code === 200){
                swal({title:"",text:"删除成功", type:"success",closeOnConfirm:true},
                    function(isConfirm){
                        initNews();
                    });
            }else{
                sweetAlert(data.msg)
            }
        }
    });
}



var newsPage = 1;
$(function () {
    initNews();
    loadMediaPlate();
    //搜索
    $("#search").click(function () {
        $("#queryValue").val($("#queryName").val())
        newsPage = 0;
        initNews();
    });

    //富文本编辑框初始化
    $('#resourceContent').summernote({
        lang: 'zh-CN',
        height: 300
        // width: 900
    });

    //编辑保存
    $("#newsEditSave").click(function () {
        var content = $("#editResourceContent").code();
        $("#editContent").val(content);
        $.ajax({
            type : 'post',
            url :  baseUrl + "/recommendRes/edit",
            data : $("#editFormData").serialize(),
            dataType : 'json',
            success : function(data) {
                if(data.code === 200){
                    swal({title:"",text:"编辑成功", type:"success",closeOnConfirm:true},
                        function(isConfirm){
                            $("#editNews").modal("hide");
                            initNews();
                        });
                }else{
                    sweetAlert(data.msg)
                }
            }
        });
    })

    //新增保存
    $("#newsSave").click(function () {
        var content = $("#resourceContent").code();
        $("#content").val(content);
        $.ajax({
            type : 'POST',
            url :  baseUrl + "/recommendRes",
            data : $("#addFormData").serialize(),
            dataType : 'json',
            success : function(data) {
                if(data.code === 200){
                    swal({title:"",text:"新增成功", type:"success",closeOnConfirm:true},
                        function(isConfirm){
                            $("#addNews").modal("hide");
                            initNews();
                        });
                }else{
                    sweetAlert(data.msg)
                }
            }
        });
    });

})


function initNews(){
    $.ajax({
        type : 'POST',
        url :  baseUrl + "/recommendRes/admin?page="+newsPage,
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
        html+="<td title='"+item.resourceTitle+"' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'><a href='/recommendRes/view/"+item.id+"/true'>"+item.resourceTitle+"</td>";
        html+="<td title='"+item.title+"' style='white-space: nowrap;text-overflow: ellipsis;overflow: hidden;'><a href="+item.href+" target='_blank'>"+item.title+"</td>";
        html+="<td>"+item.plateName+"</td>";
        html+="<td>"+item.createTime+"</td>";
        html+="<td><button  class='btn btn-primary' type='button' onclick=editNews("+item.id+")>修改</button>" +
            "<button  class='btn btn-primary' type='button' style='margin-left: 8px'  onclick=del("+item.id+")>删除</button></td>";
        html+="</tr>";
    })
    $("#newsList").prepend(html);
}

function loadMediaPlate(){
    $.get("/mediaPlate/userId",function (data) {
        var html=""
        $(data).each(function (i,item){
            html+="<option value="+item.id+">"+item.name+"</option>"
        });
        $("#plateId").html(html);
        $("#editPlateId").html(html);
    })
}
