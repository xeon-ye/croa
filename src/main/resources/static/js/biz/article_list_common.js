function loadTypeCode(attr, value){
    var attribute = attr || 'typeCode';
    $.ajax({
        type: "get",
        url: "/dict/listByTypeCode2",
        data: {typeCode: 'industry'},
        dataType: "json",
        success: function (data) {
            $("#"+attribute).empty();
            var html = "<option value=''>稿件行业</option>";
            if (data != null) {
                for (var i = 0; i < data.length; i++) {
                    if(data[i].code == value){
                        html += "<option value='" + data[i].code + "' selected='selected'>" + data[i].name + "</option>";
                    }else{
                        html += "<option value='" + data[i].code + "' >" + data[i].name + "</option>";
                    }
                }
                $("#"+attribute).append(html);
            }
        }
    });
}

function loadCustCompanyCode(attr) {
    var attribute = attr || 'custCompanyCode';
    $.ajax({
        type: "get",
        url: "/dict/listByTypeCode2",
        data: {typeCode: 'CUST_TYPE'},
        dataType: "json",
        success: function (data) {
            $("#"+attribute).empty();
            var html = "<option value=''>公司类型</option>";
            if (data != null) {
                for (var i = 0; i < data.length; i++) {
                    html += "<option value='" + data[i].code + "' >" + data[i].name + "</option>";
                }
                $("#"+attribute).append(html);
            }
        }
    });
}