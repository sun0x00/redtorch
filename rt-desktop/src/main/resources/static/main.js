function getQueryVariable(variable) {
    let query = window.location.search.substring(1);
    let vars = query.split("&");
    for (let i = 0; i < vars.length; i++) {
        let pair = vars[i].split("=");
        if (pair[0] == variable) { return pair[1]; }
    }
    return (false);
}

function restChartDomSize(width, height) {
    $("#chart").width(width);
    $("#chart").height(height);
}

$(document).ready(function () {

    const key = getQueryVariable("key");
    const port = getQueryVariable("port")

    let url = "/api/system/getEchartsOption"
    if(port&&port!=null){
        url = "http://127.0.0.1:"+port+"/api/system/getEchartsOption"
    }


    restChartDomSize($(window).innerWidth(), $(window).innerHeight())

    // 基于准备好的dom，初始化echarts实例
    const chart = echarts.init(document.getElementById('chart'), null, { renderer: 'svg' });

    if (!key) {
        chart.showLoading({ text: "错误!获取到的参数key为空" });
    } else {
        chart.showLoading({ text: "数据加载中..." });
        function loadData() {
            $.ajax({
                type: "POST",
                url: url,
                processData: false,
                contentType: 'application/json;charset=UTF-8',
                data: key,
                success: function (res) {
                    if (!res) {
                        chart.showLoading({ text: "数据加载错误,请求成功,回报解析错误" });
                    } else if (!res.status) {
                        chart.showLoading({ text: "数据加载错误,请求成功,服务器返回错误,错误信息" + res.message });
                    } else if (!res.voData) {
                        chart.showLoading({ text: "数据加载错误,请求成功,服务器返回空数据" });
                    } else if (!res.voData.data) {
                        chart.showLoading({ text: "数据加载错误,请求成功,服务器返回配置为空" });
                    } else {
                        console.log(res.voData.data)
                        chart.hideLoading()
                        if (res.voData.chartType === "candlestick") {
                            setOptionCandlestick(chart, res.voData.data, false)
                        }else if (res.voData.chartType === "histogram") {
                            setOptionHistogram(chart, res.voData.data, false)
                        }else if (res.voData.chartType === "tick") {
                            setOptionTick(chart, res.voData.data, false)
                        }
                        
                        
                    }
                },
                error: function (e) {
                    chart.showLoading({ text: "数据加载错误,请求失败,状态:" + e.status + ",信息:" + e.responseText });
                }
            });
        }
        loadData();
    }

    $(window).resize(function () {
        restChartDomSize($(window).innerWidth(), $(window).innerHeight())
        chart.resize()
    });

});