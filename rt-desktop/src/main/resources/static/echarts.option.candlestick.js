function setOptionCandlestick(chart,data,notMerge){

    if(notMerge === undefined || notMerge === null){
        notMerge = false;
    }

    let dataZoomStart = 0;
    let dataZoomEnd =100;
    if(data.valueList && data.valueList.length!=0 && data.valueList.length>500){
        if(data.valueList.length>500){
            dataZoomStart =  (1-500/data.valueList.length)*100
        }
    }

    var upColor = '#ec0000';
    var upBorderColor = '#8A0000';
    var downColor = '#00da3c';
    var downBorderColor = '#008F28';

    const option = {
        backgroundColor: '#fff',
        animation: false,
        legend: {
            top: 5,
            left: 'center',
            data: ['K', 'MA5', 'MA10', 'MA20', 'MA30']
        },
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
        visualMap: {
            show: false,
            seriesIndex: 5,
            dimension: 2,
            pieces: [{
                value: 1,
                color: downColor
            }, {
                value: -1,
                color: upColor
            }]
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
                // axisPointer: {
                //     label: {
                //         formatter: function (params) {
                //             var seriesValue = (params.seriesData[0] || {}).value;
                //             return params.value
                //             + (seriesValue != null
                //                 ? '\n' + echarts.format.addCommas(seriesValue)
                //                 : ''
                //             );
                //         }
                //     }
                // }
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
                name: 'K Bar',
                type: 'candlestick',
                data: data.valueList,
                itemStyle: {
                    normal: {
                        color: upColor,
                        color0: downColor,
                        borderColor: upBorderColor,
                        borderColor0: downBorderColor
                    }
                },
                barWidth:"95%",
                // tooltip: {
                //     formatter: function (param) {
                //         param = param[0];
                //         return [
                //             '日期: ' + param.name + '<hr size=1 style="margin: 3px 0">',
                //             '开盘价: ' + param.data[0] + '<br/>',
                //             '收盘价: ' + param.data[1] + '<br/>',
                //             '最低价: ' + param.data[2] + '<br/>',
                //             '最高价: ' + param.data[3] + '<br/>'
                //         ].join('');
                //     }
                // }
            },
            {
                name: 'MA5',
                type: 'line',
                symbol: 'none',
                data: data.ma5List,
                smooth: true,
                lineStyle: {
                    normal: { opacity: 0.5 }
                }
            },
            {
                name: 'MA10',
                type: 'line',
                symbol: 'none',
                data: data.ma10List,
                smooth: true,
                lineStyle: {
                    normal: { opacity: 0.5 }
                }
            },
            {
                name: 'MA20',
                type: 'line',
                symbol: 'none',
                data: data.ma20List,
                smooth: true,
                lineStyle: {
                    normal: { opacity: 0.5 }
                }
            },
            {
                name: 'MA30',
                type: 'line',
                symbol: 'none',
                data: data.ma30List,
                smooth: true,
                lineStyle: {
                    normal: { opacity: 0.5 }
                }
            },
            {
                name: '成交',
                type: 'bar',
                xAxisIndex: 1,
                yAxisIndex: 1,
                data: data.volumeDeltaList
            },
            {
                name: '持仓',
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

    function renderBrushed(params) {
        var sum = 0;
        var min = Infinity;
        var max = -Infinity;
        var countBySeries = [];


        var brushComponent = params.batch[0];

        var dataIndex = brushComponent.selected[0].dataIndex;
        
        console.log(dataIndex)
        for (var i = 0; i < dataIndex.length; i++) {
            var val = data.valueList[dataIndex[i]][1];
            sum += val;
            min = Math.min(val, min);
            max = Math.max(val, max);
        }
        console.log(max.toFixed(4))
        console.log(min.toFixed(4))

        // panel.innerHTML = [
        //     '<h3>STATISTICS:</h3>',
        //     'SUM of open: ' + (sum / rawIndices.length).toFixed(4) + '<br>',
        //     'MIN of open: ' + min.toFixed(4) + '<br>',
        //     'MAX of open: ' + max.toFixed(4) + '<br>'
        // ].join(' ');
    }


}