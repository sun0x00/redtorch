function setOptionHistogram(chart,data,notMerge){

    if(notMerge === undefined || notMerge === null){
        notMerge = false;
    }

    option = {
        xAxis: {
            type: 'category',
            data: data.categoryList
        },
        yAxis: {
            type: 'value'
        },
        series: [{
            data: data.valueList,
            type: 'bar'
        }]
    };
    
    chart.setOption(option, notMerge);
}