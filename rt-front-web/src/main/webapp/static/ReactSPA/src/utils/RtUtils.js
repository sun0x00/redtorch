const numberFormat = (value, decimals)=>{
    let newValue = value;
    if (newValue === undefined) {
        return undefined;
    }
    if (Math.abs(newValue) > 10000000000) {
        return newValue.toExponential(decimals)
    }
    if (Number.isNaN(value)) {
        newValue = 0
    }
    return (newValue.toFixed(decimals));
}

const leftZeroPad=(val, minLength)=>{
    let newValue = val;
    if (typeof (newValue) !== "string"){
        newValue = String(val);
    }
    return ("000000000000000000".substring(0, minLength - newValue.length)) + newValue;
}

const sortByLogTime = (a, b) => b.logTime.replace(/:/g, "") - a.logTime.replace(/:/g, "")

const sortLogByTimestamp = (a, b) => a.logTimestamp - b.logTimestamp

const sortOrderByTimeAndID = (orderA, orderB) => {
    const dtA = `${orderA.orderDate}${orderA.orderTime}`.replace(/:/g, "").replace(/null/g, "").replace(
        /undefined/g, "");
    let dtAInt = 0
    if (dtA !== undefined && dtA !== "") {
        dtAInt = parseInt(dtA,10)
    }

    const dtB = `${orderB.orderDate}${orderB.orderTime}`.replace(/:/g, "").replace(/null/g, "").replace(
        /undefined/g, "");
    let dtBInt = 0
    if (dtB !== undefined && dtB !== "") {
        dtBInt = parseInt(dtB,10);
    }
    // console.log(orderA)
    // console.log(dtBInt)
    // console.log(dtAInt)
    return (dtAInt * 100000000 + parseInt(orderA.orderID,10)) - (dtBInt * 100000000 + parseInt(orderB.orderID,10))
}

const sortTradeByTimeAndID = (tradeA, tradeB)=>{
    const dtA = `${tradeA.tradeDate}${tradeA.tradeTime}`.replace(/:/g, "").replace(/null/g, "").replace(
        /undefined/g, "")
    let dtAInt = 0
    if (dtA !== undefined && dtA !== "") {
        dtAInt = parseInt(dtA,10);
    }

    const dtB = `${tradeB.tradeDate}${tradeB.tradeTime}`.replace(/:/g, "").replace(/null/g, "").replace(
        /undefined/g, "")
    let dtBInt = 0
    if (dtB !== undefined && dtB !== "") {
        dtBInt = parseInt(dtB,10)
    }
    // console.log(tradeA)
    // console.log(dtBInt)
    // console.log(dtAInt)
    // console.log(dtAInt * 100000000 + parseInt(tradeA.tradeID))
    // console.log(dtBInt * 100000000  + parseInt(tradeB.tradeID))

    return (dtAInt * 100000000 + parseInt(tradeA.tradeID,10)) - (dtBInt * 100000000 + parseInt(tradeB.tradeID,10))
}

const sortBySymbol = (a, b) => a.symbol.localeCompare(b.symbol);

const sortByRtAccountID = (a, b) => a.rtAccountID.localeCompare(b.rtAccountID);

const formatDate = (date, fmt) => {
    let newFmt = fmt;

    if (/(y+)/.test(newFmt)) {
        newFmt = newFmt.replace(RegExp.$1, (`${date.getFullYear()}`).substr(4 - RegExp.$1.length));
    }
    const o = {
        'M+': date.getMonth() + 1,
        'd+': date.getDate(),
        'H+': date.getHours(),
        'm+': date.getMinutes(),
        's+': date.getSeconds(),
        'S+': date.getMilliseconds()
    }

    Object.keys(o).forEach((k) => {
        if (new RegExp(`(${k})`).test(newFmt)) {
            const str = `${o[k]}`;
            if (k === 'S+') {
                newFmt = newFmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? str : leftZeroPad(str, 3));
            } else {
                newFmt = newFmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? str : leftZeroPad(str, 2));
            }
        }
    })

    return newFmt;
};


const timestampFormat = (timestamp, formatStr) => {
    const date = new Date(timestamp);
    if (formatStr) {
        return formatDate(date, formatStr)
    }
    return formatDate(date, "yyyy-MM-dd HH:mm:ss.S")
};

const sleep = (time) => new Promise((resolve) => setTimeout(resolve, time));


const roundWithStep = (value, step) => {
    let tmpStep = step;
    if(!step)(tmpStep = 1.0)
    const inv = 1.0 / tmpStep;
    return Math.round(value * inv) / inv;
}

const uuidv4 = () => {
    const uuid = ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
      )
    return uuid
  }

export {numberFormat,
    leftZeroPad,
    timestampFormat,
    sortBySymbol,
    sortOrderByTimeAndID,
    sortTradeByTimeAndID,
    sortLogByTimestamp,
    sortByLogTime,
    sleep,
    uuidv4,
    sortByRtAccountID,
    roundWithStep}