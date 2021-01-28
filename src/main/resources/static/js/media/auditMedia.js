$(function () {
    var id = $.getUrlParam('id');
    $.ajax({
        url: '/media/' + id,
        dataType: 'json',
        type: 'get',
        success: function (data) {
            var media = data.data.media;
            if (media == null || media == undefined) {
                swal("没有查到相关媒体信息！");
                return;
            }
            $("#mType").val(media.mType);
            $("#id").val(media.id);
            setType(media);
        }
    });
});

function setType(media) {
    $.get(baseUrl + "/mediaForm/list/" + media.mType, function (datas) {
        var html = '';
        $(datas).each(function (i, data) {
            if (i % 4 == 0)
                html += '<div class="col-md-12">';
            var value = media[data.code] == undefined ? "" : media[data.code];
            html += '<div class="form-group col-sm-3"><label class="col-sm-6 control-label"><span class="text-red"> * </span>';
            html += data.name + ':</label><div class="col-sm-6 control-label" style="text-align: left">' + value + '</div></div>';
            if ((i + 1) % 4 == 0) html += '</div>';
        });
        $("#mediaForm").html(html);
    }, "json");
}

function pass() {
    $.ajax({
        url: baseUrl + "/media/pass/33bd64eb-ef12-11e8-8a59-e0d55e04a42a",
        type: 'post',
        success: function (d) {
            swal({
                title: "提示!",
                text: d.code == 200 ? "媒体审核已驳回！" : d.msg,
                type: d.code == 200 ? "success" : "error",
            }, function (isConfirm) {
                location.reload();
            });
        }
    });
}

function reject() {
    $.ajax({
        url: baseUrl + "/media/reject/33bd64eb-ef12-11e8-8a59-e0d55e04a42a",
        type: 'post',
        success: function (d) {
            swal({
                title: "提示!",
                text: d.code == 200 ? "媒体审核通过！" : d.msg,
                type: d.code == 200 ? "success" : "error",
            }, function (isConfirm) {
                location.reload();
            });
        }
    });
}

