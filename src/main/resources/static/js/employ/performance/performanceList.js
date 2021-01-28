$(function () {
    pageObj.initSelect();

    $("#schDel").on("click", function () {
        pageObj.delScheme($("#selectNode").val())
    });

    $("#schEdit").on("click", function () {
        pageObj.editScheme($("#selectNode").val())
    });

    $("#schAdd").on("click", function () {
        pageObj.addScheme()
    });

    $("#schCopy").on("click", function () {
        pageObj.copyScheme($("#selectNode").val())
    })
});

var pageObj = {
    initSelect: function () {
        //初始化年
        $.get(baseUrl + "/employeePerformancePK/years", function (resultData) {
            var opts = resultData.data.years.map(function (item) {
                if (item && item.createYear)
                    return "<option value='" + item.createYear + "'>" + item.createYear + "</option>";
            });

            if (opts.length === 0) {
                $("#content").html('');
                return;
            }
            //初始化下拉框
            var $yearSelect = $("#yearSelect");
            $yearSelect.append(opts);
            $yearSelect.find("option[value='-1']").attr("selected", true);

            pageObj.renderSelect();
            pageObj.initTree();
        });
    },
    //初始化左侧树结构
    initTree: function () {
        var nodes = [];
        //初始化所有数据
        var year = $("#yearSelect").val();
        if (year === '-1') year = "";

        $.get(baseUrl + "/employeePerformancePK/all?currentDate=" + year, function (data) {
            if (data.data.data.length > 0) {
                var schemeLs = groupBy(data.data.data, function (item) {
                    var createYear = item.createYear;
                    if (!createYear) createYear = '拷贝';
                    return createYear;
                });

                schemeLs.forEach(function (item) {
                    var node = {};
                    node.text = item[0].createYear ? item[0].createYear
                        : '<span style="color: red">拷贝设置，请修改时间，否则无效</span>';
                    node.selectable = false;
                    node.state = {
                        disabled: true
                    };
                    node.nodes = [];
                    item.forEach(function (child) {
                        node.nodes.push({
                            text: child.name,
                            data: child
                        })
                    });
                    nodes.push(node);
                });

                $('#performanceTree').treeview(
                    {
                        data: nodes,
                        onNodeSelected: function (event, data) {
                            if (!data.data) return;

                            var $selectNode = $("#selectNode");
                            if (data.data.id === parseInt($selectNode.val())) return;
                            $selectNode.val(data.data.id);
                            pageObj.renderPerformance(data.data);
                        }
                    }
                );
            }
        }, "json");
    },
    //渲染数据
    renderPerformance: function (performance) {
        $("#peoplePKShowView").html('');
        if (!performance) return;

        //初始化表单数据
        Object.keys(performance).forEach(function (key) {
            $("#performanceView span[name='" + key + "']").text(performance[key])
        });

        performance.relates.forEach(function (item) {
            var pkObj = {
                left: item.leftEmployeeName,
                right: item.rightEmployeeName
            };

            var html = template("peoplePKShow", {'data': pkObj});
            $("#peoplePKShowView").append(html);
        });

        var backImg = performance.backgroundImg ? performance.backgroundImg : "/img/performance/background_default.png";
        $("#backgroundImg").css("background-image", "url(" + backImg + ")");
    },
    addScheme: function () {
        page("/employ/performance/performanceDetail", "新增pk设置");
    },
    editScheme: function (performanceId) {
        if (!performanceId) {
            layer.msg("请选择pk设置");
            return;
        }
        var editScheme = "/employ/performance/performanceDetail?id=" + performanceId;
        page(editScheme, "编辑pk设置");
    },
    delScheme: function (performanceId) {
        if (!performanceId) {
            layer.msg("请选择pk设置");
            return;
        }
        $.ajax({
            type: "delete",
            url: baseUrl + "/employeePerformancePK?performanceId=" + performanceId,
            dataType: "json",
            success: function () {
                pageObj.initTree();
                $('#deleteConfirm').modal('hide');
            }
        });
    },
    copyScheme: function (performanceId) {
        if (!performanceId) {
            layer.msg("请选择pk设置");
            return;
        }
        $.ajax({
            type: "post",
            url: baseUrl + "/employeePerformancePK/copy?performanceId=" + performanceId,
            dataType: "json",
            success: function () {
                pageObj.initTree();
                $('#copyConfirm').modal('hide');
            }
        });
    },
    //渲染下拉框
    renderSelect: function () {
        layui.use('form', function () {
            var form = layui.form;
            form.on('select(yearSelect)', function () {
                pageObj.initTree();
            });
        });
    },
};

function groupBy(array, f) {
    var groups = {};
    array.forEach(function (o) {
        var group = JSON.stringify(f(o));
        groups[group] = groups[group] || [];
        groups[group].push(o);
    });
    return Object.keys(groups).map(function (group) {
        return groups[group];
    });
}