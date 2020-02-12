function setOptionVolOPIchangeHistogram(chart,data,notMerge){

    if(notMerge === undefined || notMerge === null){
        notMerge = false;
    }

    option = {
        grid: [
            {left: 65, right: 65, top:20 ,height: '43%'},
            {left: 65, right: 65, top:"50%", height: '43%'},
        ],
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
            link: { xAxisIndex: [0,1] },
            label: {
                backgroundColor: '#777'
            }
        },
        // tooltip: {
        //     formatter: 'Group {a}: ({c})'
        // },
        xAxis: [
            {
                type: 'category',
                gridIndex: 0,
                data: data.categoryList
            },
            {
                type: 'category',
                gridIndex: 1,
                data: data.categoryList
            },
        ],
        yAxis: [
            {gridIndex: 0, type: 'value'},
            {gridIndex: 1, type: 'value'},
        ],
        series: [
    
            {
                name: '成交',
                type: 'bar',
                xAxisIndex: 0,
                yAxisIndex: 0,
                data: data.volumeChangeValueList,
            },{
                name: '开平',
                type: 'bar',
                xAxisIndex: 1,
                yAxisIndex: 1,
                data: data.opiChangeValueList,
            }
        ]
    };
    
    chart.setOption(option, notMerge);
}

