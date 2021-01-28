$(function () {
    $.get(baseUrl + "/employeePerformancePK/view/allPermission", function (result) {
        //初始化全部视图
        if (result.code === 200) pageObj.initSelect();
        //仅初始化当前pk视图
        else if (result.code === 403) pageObj.renderCurrentPKView();
    }, "json");
});

var pageObj = {
    leftNums: 0,
    rightNums: 0,
    initSelect: function () {
        //初始化年
        $.get(baseUrl + "/employeePerformancePK/years", function (resultData) {
            var opts = resultData.data.years.map(function (item) {
                if (item && item.createYear)
                    return "<option value='" + item.createYear + "'>" + item.createYear + "</option>";
            });

            if (opts.length === 0) {
                layer.msg("暂无数据,请前往pk设置页面添加");
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
        $.get(baseUrl + "/employeePerformancePK/view/all?currentDate=" + year, function (data) {
            if (data.data.peopleLs.length > 0) {
                var schemeLs = groupBy(data.data.peopleLs, function (item) {
                    return item.startYear;
                });

                schemeLs.forEach(function (item) {
                    var node = {};
                    node.text = item[0].startYear;
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
                    if (item[0].startYear) nodes.push(node);
                });

                $('#pkTree').treeview(
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
            } else {
                layer.msg("暂无数据,请前往pk设置页面设置");
                $("#content").html('');
            }
        }, "json");
    },
    //渲染数据
    renderPerformance: function (performance) {
        if (!performance) return;
        var pkType = performance.pkType;
        pageObj.clearPkPerple();
        var $pkViewBackgroundCustomer = $("#pkViewBackgroundCustomer");
        var $pkViewBackground = $("#pkViewBackground");
        if (performance.backgroundImg) {
            var viewBackground = performance.backgroundImg.split("\"").join("");
            $pkViewBackgroundCustomer.css("background-image", "url(" + viewBackground + ")");
            $pkViewBackgroundCustomer.show();
            $pkViewBackground.hide();
        } else {
            $pkViewBackgroundCustomer.hide();
            $pkViewBackground.show();
        }

        if (pkType === 0) {
            $("#content").show();
            $("#contentDept").hide();
            $(".pkViewName span").text(performance.name + " (" + performance.startDate + "至" + performance.endDate + ")");

            pageObj.renderTopPeople(performance.topPeople, true);
            performance.peopleList.forEach(function (item) {
                pageObj.renderPkPeople(item, pkType);
            });
        } else if (pkType === 1) {
            $("#content").hide();
            $("#contentDept").show();
            $(".pkDeptViewName span").text(performance.name + " (" + performance.startDate + "至" + performance.endDate + ")");

            pageObj.renderTopDeptPeople(performance.topPeople);
            var html = template("deptPkViewHtml", {data: performance.peopleList});
            $("#deptDetailDiv").html(html);
        }
        pageObj.renderProgressBar();
    },
    renderCurrentPKView: function () {
        $("#leftDivTop").hide();

        var nodes = [];
        //初始化所有数据
        $.get(baseUrl + "/employeePerformancePK/view/all?queryCurrentDateTime=true", function (data) {
            if (data.data.peopleLs.length > 0) {
                var schemeLs = groupBy(data.data.peopleLs, function (item) {
                    return item.startYear;
                });

                schemeLs.forEach(function (item) {
                    var node = {};
                    node.text = item[0].startYear;
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
                    if (item[0].startYear) nodes.push(node);
                });

                $('#pkTree').treeview(
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
            } else {
                layer.msg("暂无数据,请前往pk设置页面设置");
                $("#content").html('');
            }
        }, "json");
    },
    clearPkPerple: function () {
        $("#first").html('');
        $("#firstDept").html('');
        $("#firstProfit").html('');
        $("#second").html('');
        $("#secondDept").html('');
        $("#secondProfit").html('');
        $("#third").html('');
        $("#thirdDept").html('');
        $("#thirdProfit").html('');
        $("#rightContent").html('');
        $("#leftContent").html('');
        $("#deptDetailDiv").html('');
        pageObj.leftNums = 0;
        pageObj.rightNums = 0;
    },
    renderTopPeople: function (topProfit, showProfit) {
        var top = topProfit[0];
        if (top) {
            $("#first").text(top.name);
            if (showProfit) $("#firstProfit").text("￥" + top.profit);
        }

        var second = topProfit[1];
        if (second) {
            $("#second").text(second.name);
            if (showProfit) $("#secondProfit").text("￥" + second.profit);
        }

        var third = topProfit[2];
        if (third) {
            $("#third").text(third.name);
            if (showProfit) $("#thirdProfit").text("￥" + third.profit);
        }
    },
    renderTopDeptPeople: function (topProfit) {
        var top = topProfit[0];
        if (top) {
            $("#firstDept").text(top.name);
        }

        var second = topProfit[1];
        if (second) {
            $("#secondDept").text(second.name);
        }

        var third = topProfit[2];
        if (third) {
            $("#thirdDept").text(third.name);
        }
    },
    renderPkPeople: function (peopleList, pkType) {
        peopleList.pkType = pkType;
        var html = template("pkViewHtml", peopleList);
        if (pageObj.leftNums <= pageObj.rightNums) {
            $("#leftContent").append(html);
            pageObj.leftNums++;
        } else {
            $("#rightContent").append(html);
            pageObj.rightNums++;
        }
    },
    //渲染进度条
    renderProgressBar: function () {
        layui.use('element', function () {
            var element = layui.element;
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