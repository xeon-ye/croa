$(document).ready(function () {
    $.jgrid.defaults.styleUI = 'Bootstrap';
    announcements();
});

function announcements(){
    $.ajax({
        type:"post",
        url:baseUrl +"/Mediapass/notificationlist",
        dataType:"json",
        success:function (data) {
                var html = "";
            for(var i =0;i<data.list.length;i++){
                html += " <li class=\"layui-timeline-item timeLineCss\">\n" +
                    "         <i class=\"layui-icon layui-timeline-axis\">&#xe63f;</i>\n" +
                    "         <div class=\"layui-timeline-content layui-text timeCss\">\n" +
                    "             <span class=\"layui-timeline-title\">"+ new Date(data.list[i].releaseTime).format("yyyy-MM-dd")+"</span>\n" +
                    "             <div class=\"timeContent\">\n" +
                    "                 <div class=\"timeContentTitle\">\n" +
                    "                     <a href='#' onclick=linkLocation('/announcementinform/Mediapass')>发布人-"+data.list[i].releaseUser+"</a>\n" +
                    "                 </div>\n" +
                    "                 <div class=\"timeContent1\">\n" +
                    "                     <a href='#' onclick=linkLocation('/announcementinform/Mediapass?id="+data.list[i].id+"')>"+data.list[i].title+"</a>\n" +
                    "                 </div>\n" +
                    "             </div>\n" +
                    "         </div>\n" +
                    "     </li>"
            }
            $("#announcements").html(html);

        }
    })

}

function linkLocation(href) {
    page(href,"通知公告");
}
