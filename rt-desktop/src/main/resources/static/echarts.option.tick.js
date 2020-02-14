function setOptionTick(chart,data,notMerge){

    if(notMerge === undefined || notMerge === null){
        notMerge = false;
    }

    let dataZoomStart = 0;
    let dataZoomEnd =100;
    if(data.lastPriceValueList && data.lastPriceValueList.length!=0 && data.lastPriceValueList.length>500){
        if(data.lastPriceValueList.length>500){
            dataZoomStart =  (1-500/data.lastPriceValueList.length)*100
        }
    }
    const option = {
        backgroundColor: '#fff',
        animation: false,
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross'
            },
            backgroundColor: 'rgba(245, 245, 245, 0.8)',
            borderWidth: 1,
            borderColor: '#ccc',
            padding: 10,
            textStyle: {
                color: '#000'
            },
            position: function (pos, params, el, elRect, size) {
                var obj = { top: 25 };
                obj[['left', 'right'][+(pos[0] < size.viewSize[0] / 2)]] = 30;
                return obj;
            }
            // extraCssText: 'width: 170px'
        },
        axisPointer: {
            link: { xAxisIndex: 'all' },
            label: {
                backgroundColor: '#777'
            }
        },
        toolbox: {
            feature: {
                dataZoom: {
                    yAxisIndex: false
                },
                brush: {
                    type: ['lineX', 'clear']
                }
            }
        },
        brush: {
            xAxisIndex: 'all',
            brushLink: 'all',
            outOfBrush: {
                colorAlpha: 0.1
            }
        },
        grid: [
            {
                top:25,
                left: 65,
                right: 65,
                height: '68%'
            },
            {
                left: 65,
                right: 65,
                top: '75%',
                height: '20%',
            }
        ],
        xAxis: [
            {
                type: 'category',
                data: data.categoryList,
                scale: true,
                boundaryGap: true,
                axisLine: { onZero: false },
                splitLine: { show: false },
                splitNumber: 20,
                min: 'dataMin',
                max: 'dataMax',
                axisPointer: {
                    z: 100
                }
            },
            {
                type: 'category',
                gridIndex: 1,
                data: data.categoryList,
                scale: true,
                boundaryGap: true,
                axisLine: { onZero: false },
                axisTick: { show: false },
                splitLine: { show: false },
                axisLabel: { show: false },
                splitNumber: 20,
                min: 'dataMin',
                max: 'dataMax'

            }
        ],
        yAxis: [
            {
                scale: true,
                splitArea: {
                    show: true
                }
            },
            {
                scale: true,
                gridIndex: 1,
                splitNumber: 2,
                min: 0,
                // axisLabel: { show: false },
                // axisLine: { show: false },
                // axisTick: { show: false },
                // splitLine: { show: false }
            },
            {
                scale: true,
                gridIndex: 1,
                position: "right",
                splitNumber: 2,
                // axisLabel: { show: false },
                // axisLine: { show: false },
                // axisTick: { show: false },
                // splitLine: { show: false }
            }
        ],
        dataZoom: [
            {
                type: 'inside',
                xAxisIndex: [0, 1],
                start: dataZoomStart,
                end: dataZoomEnd
            },
            {
                show: true,
                showDataShadow: false,
                xAxisIndex: [0, 1],
                type: 'slider',
                top: "95%",
                start: dataZoomStart,
                end: dataZoomEnd
            }
        ],
        series: [
            {
                name: '最新价',
                type: 'line',
                symbol: 'none',
                data: data.lastPriceValueList,
                smooth: false,
            },
            {
                name: '成交量',
                type: 'bar',
                xAxisIndex: 1,
                yAxisIndex: 1,
                data: data.volumeDeltaList
            },
            {
                name: '持仓量',
                type: 'line',
                xAxisIndex: 1,
                yAxisIndex: 2,
                smooth: false,
                data: data.openInterestList
            }
        ]
    };


    chart.setOption(option, notMerge);

    chart.on('brushSelected', renderBrushed);
}