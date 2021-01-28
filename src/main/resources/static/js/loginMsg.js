var MSG_API = "wss://localhost/member/api/ws/1";
// var MSG_API = "http://localhost/api/ws/";
$(function () {
    var socket;
    if (typeof (WebSocket) == "undefined") {
        console.log("遗憾：您的浏览器不支持WebSocket");
    } else {
        console.log("恭喜：您的浏览器支持WebSocket");
        socket = new WebSocket(SOCKET_API);
        //连接打开事件
        socket.onopen = function () {
            console.log("Socket 已打开");
            // socket.send("消息发送测试111");
        };
        //收到消息事件
        socket.onmessage = function (msg) {
            var data = JSON.parse(msg.data.replace(new RegExp("'", 'g'), "\""));
//           var data = JSON.parse(msg.data);
            alert(data);

        };
        //连接关闭事件
        socket.onclose = function () {
            console.log("Socket已关闭");
            socket.close();
        };
        //发生了错误事件
        socket.onerror = function (event) {
            alert("Socket发生了错误");
            socket.close();
        }
        //窗口关闭时，关闭连接
        window.unload = function () {
            socket.close();
        };
    }

    $("#frclose").click(function(){
        $(".tipfloat").animate({height:"hide"},800);
    })
});






var InterValObj;

function showMsg(data) {
    $(".ms_title").html(data.title);
    $(".ms_content").html("亲爱的"+data.receiveName+"你好：<br>&nbsp; &nbsp; &nbsp; &nbsp;  "+data.content);
    $(".tipfloat").animate({height:"show"},800);
    InterValObj = window.setInterval(hideAnimate, 5000); //启动计时器，1秒执行一次
}



function hideAnimate(){
    window.clearInterval(InterValObj);//停止计时器
    $(".tipfloat").animate({height:"hide"},800);
}


