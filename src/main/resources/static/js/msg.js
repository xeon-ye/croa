var MSG_API = "http://127.0.0.1/api/ws/";
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
            var title = '系统消息';
            toastr.options = {
                closeButton: true,
                debug: false,
                progressBar: true,
                positionClass: 'toast-top-right',
                showDuration: "400",
                hideDuration: "1000",
                timeOut: "600000",
                extendedTimeOut: "10000",
                showEasing: "swing",
                hideEasing: "linear",
                showMethod: "fadeIn",
                hideMethod: "fadeOut"
            };
            // if ($('#addBehaviorOnToastClick').prop('checked')) {
            //     toastr.options.onclick = function () {
            //         alert('You can perform some custom action after a toast goes away');
            //     };
            // }
            // $("#toastrOptions").text("Command: toastr[" + shortCutFunction + "](\"" + msg + (title ? "\", \"" + title : '') + "\")\n\ntoastr.options = " + JSON.stringify(toastr.options, null, 2));
            var $toast = toastr['info'](msg.data, title); // Wire up an event handler to a button in the toast, if it exists
            $toastlast = $toast;
            if ($toast.find('#okBtn').length) {
                $toast.delegate('#okBtn', 'click', function () {
                    alert('you clicked me. i was toast #' + toastIndex + '. goodbye!');
                    $toast.remove();
                });
            }
            if ($toast.find('#surpriseBtn').length) {
                $toast.delegate('#surpriseBtn', 'click', function () {
                    alert('Surprise! you clicked me. i was toast #' + toastIndex + '. You could perform an action here.');
                });
            }
        };
        //连接关闭事件
        socket.onclose = function () {
            console.log("Socket已关闭");
            socket.close();
        };
        //发生了错误事件
        socket.onerror = function () {
            alert("Socket发生了错误");
            socket.close();
        }
        //窗口关闭时，关闭连接
        window.unload = function () {
            socket.close();
        };
    }
    im();
});

function im() {
    var socket = new WebSocket(IM_API);
    //连接打开事件
    socket.onopen = function () {
        console.log("Socket 已打开");
        // socket.send("消息发送测试(From Client)");
    };
    //收到消息事件
    socket.onmessage = function (msg) {
        // showMsg(data);
        var data = JSON.parse(msg.data);
        showMsg({
            'name': data.sendName,
            'date': new Date().format("MM-dd hh:mm"),
            'msg': data.content,
            op: 'left',
            headImg: data.sendUserImage
        });
        $("#receiveUserName").text(data.sendName);
        $("#receiveUserId").val(data.sendUserId);
        $("#headImg").val(data.sendUserImage);
    };
    //连接关闭事件
    socket.onclose = function () {
        console.log("Socket已关闭");
        socket.close();
    };
    //发生了错误事件
    socket.onerror = function () {
        alert("Socket发生了错误");
        socket.close();
    }
    //窗口关闭时，关闭连接
    window.unload = function () {
        socket.close();
    };
}

function readMsg(id) {
    $.post(MSG_API + "read/" + id);
}

function sendALL(msg) {
    $.post(MSG_API + "sendAll", {"message": msg}, function (d) {
        if (d.code == 1)
            swal(d.msg, "消息发送成功", "");
    }, "json");
}

function sendMsg(sendUserId, sendName, sendUserImage, receiveUserId, receiveName, t, receiveUserImage) {
    var msg = t.val();
    // var receiveUserId=$(receiveUserId).val();
    if (receiveUserId == '') {
        // swal('请从左侧列表中选择一位好友发送消息！');
        return;
    }
    if (msg) {
        if (sendUserId == receiveUserId) {
            swal('不能自己给自己发信息！');
            return;
        }
        var data = {
            code: "100",
            content: msg,
            sendUserId: sendUserId,
            // sendUserName: sendUserName,
            sendName: sendName,
            receiveUserId: receiveUserId,
            // receiveUserName: receiveUserName,
            receiveName: receiveName,
            sendUserImage: sendUserImage,
            receiveUserImage: receiveUserImage,
            subject: "系统消息",
            title: "系统消息"
        };
        $.post(MSG_API + "imSend", data, function (d) {
            if (d.code == 1)
                swal(d.msg, "消息发送成功", "");
            showMsg({
                name: sendName,
                date: new Date().format("MM-dd hh:mm"),
                msg: msg,
                op: 'right',
                headImg: sendUserImage
            });
        }, "json");
        t.val('');
    }

}

function showMsg(data) {
    var html = '<div class="' + data.op + '"><img class="img-circle" style="width: 30px;height: 30px;float: ' + data.op + ';margin: 5px;" src="' + data.headImg + '"/>' +
        '<div class="author-name">' + data.name +
        '<small class="chat-date">' + data.date + '</small></div>' +
        '<div class="chat-message ">' + data.msg + '</div></div>';
    $("#im_content").append(html);
    $("#im_content").scrollTop($("#im_content")[0].scrollHeight);
}