(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([[17],{RLKE:function(e,t,a){"use strict";var l=a("TqRt"),n=a("284h");Object.defineProperty(t,"__esModule",{value:!0}),t.default=void 0,a("IzEo");var r=l(a("bx4M"));a("5NDa");var d=l(a("5rEg")),i=l(a("lwsE")),u=l(a("W8MJ")),s=l(a("a1gu")),c=l(a("Nsbk")),o=l(a("7W2i"));a("y8nQ");var f=l(a("Vl3Y"));a("OaEy");var y=l(a("2fM7"));a("Znn+");var g,m,p=l(a("ZTPi")),w=n(a("q1tI")),h=l(a("rT5C")),v=p.default.TabPane,E=y.default.Option,I=f.default.Item,S=(g=f.default.create(),g(m=function(e){function t(e){var a;return(0,i.default)(this,t),a=(0,s.default)(this,(0,c.default)(t).call(this,e)),a.handleGatewayTypeChange=function(e){a.setState({stateGatewayType:e})},a.resetForm=function(){var e=a.props.form;e.resetFields()},a.gatewayFormRef=w.default.createRef(),a.state={stateGatewayType:void 0},a}return(0,o.default)(t,e),(0,u.default)(t,[{key:"render",value:function(){var e=this.props,t=e.form.getFieldDecorator,a=e.editGateway,l=this.state.stateGatewayType,n="",i="",u="xyz.redtorch.gateway.ctp.CtpGateway",s="CTP",c="",o="",g="",m="",S="",C="",D="",b="127.0.0.1",k=7496,G=0,N="";return null!==a&&void 0!==a?(n=a.gatewayID,i=a.gatewayDisplayName,u=a.gatewayClassName,s=null==l||void 0===l?a.gatewayType:l,"CTP"===s&&null!=a.ctpSetting&&void 0!==a.ctpSetting?(c=a.ctpSetting.userID,o=a.ctpSetting.password,g=a.ctpSetting.brokerID,m=a.ctpSetting.tdAddress,S=a.ctpSetting.mdAddress,C=a.ctpSetting.authCode,D=a.ctpSetting.userProductInfo):"IB"===s&&null!=a.ibSetting&&void 0!==a.ibSetting&&(b=a.ibSetting.host,k=a.ibSetting.port,G=a.ibSetting.clientID,N=a.ibSetting.accountCode)):null!=l&&void 0!==l&&(s=l,"CTP"===s&&(u="xyz.redtorch.gateway.ctp.CtpGateway"),"IB"===s&&(u="xyz.redtorch.gateway.ib.IbGateway")),w.default.createElement(r.default,{bordered:!1,gutter:0},w.default.createElement(f.default,{hideRequiredMark:!0,gutter:0},w.default.createElement(I,{className:h.default.formItem},t("gatewayID",{initialValue:n})(w.default.createElement(d.default,{disabled:!0,placeholder:"\u7f51\u5173ID"}))),w.default.createElement(I,{className:h.default.formItem},t("gatewayDisplayName",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u7f51\u5173\u540d\u79f0"}],initialValue:i})(w.default.createElement(d.default,{placeholder:"\u7f51\u5173\u540d\u79f0"}))),w.default.createElement(I,{className:h.default.formItem},t("gatewayClassName",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u7f51\u5173Java\u5b9e\u73b0\u7c7b"}],initialValue:u})(w.default.createElement(d.default,{placeholder:"\u7f51\u5173Java\u5b9e\u73b0\u7c7b"}))),w.default.createElement(I,{className:h.default.formItem},t("gatewayType",{initialValue:s})(w.default.createElement(y.default,{disabled:!0},w.default.createElement(E,{value:"CTP"},"CTP"),w.default.createElement(E,{value:"IB"},"IB")))),w.default.createElement(p.default,{defaultActiveKey:s,onChange:this.handleGatewayTypeChange},w.default.createElement(v,{tab:"CTP",key:"CTP"},w.default.createElement(I,{className:h.default.formItem},t("ctpSettingUserID",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u8d26\u6237ID"}],initialValue:c})(w.default.createElement(d.default,{placeholder:"\u8d26\u6237ID"}))),w.default.createElement(I,{className:h.default.formItem},t("ctpSettingPassword",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u8d26\u6237\u5bc6\u7801"}],initialValue:o})(w.default.createElement(d.default,{type:"password",placeholder:"\u5bc6\u7801"}))),w.default.createElement(I,{className:h.default.formItem},t("ctpSettingBrokerID",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u7ecf\u7eaa\u5546ID"}],initialValue:g})(w.default.createElement(d.default,{placeholder:"\u7ecf\u7eaa\u5546ID"}))),w.default.createElement(I,{className:h.default.formItem},t("ctpSettingTdAddress",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u4ea4\u6613\u5730\u5740"},{pattern:/^(tcp:\/\/)(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(:\d{2,5})$/,message:"\u5730\u5740\u4e0d\u6b63\u786e"}],initialValue:m})(w.default.createElement(d.default,{placeholder:"\u4ea4\u6613\u5730\u5740"}))),w.default.createElement(I,{className:h.default.formItem},t("ctpSettingMdAddress",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u884c\u60c5\u5730\u5740"},{pattern:/^(tcp:\/\/)(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])(:\d{2,5})$/,message:"\u5730\u5740\u4e0d\u6b63\u786e"}],initialValue:S})(w.default.createElement(d.default,{placeholder:"\u884c\u60c5\u5730\u5740"}))),w.default.createElement(I,{className:h.default.formItem},t("ctpSettingAuthCode",{initialValue:C})(w.default.createElement(d.default,{placeholder:"\u6388\u6743\u7801"}))),w.default.createElement(I,{className:h.default.formItem},t("ctpSettingUserProductInfo",{initialValue:D})(w.default.createElement(d.default,{placeholder:"\u7528\u6237\u4ea7\u54c1\u4fe1\u606f"})))),w.default.createElement(v,{tab:"IB",key:"IB"},w.default.createElement(I,{className:h.default.formItem},t("ibSettingHost",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u4e3b\u673a\u540d"}],initialValue:b})(w.default.createElement(d.default,{placeholder:"\u4e3b\u673a\u540d"}))),w.default.createElement(I,{className:h.default.formItem},t("ibSettingPort",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u7aef\u53e3"}],initialValue:k})(w.default.createElement(d.default,{placeholder:"ibSettingPort"}))),w.default.createElement(I,{className:h.default.formItem},t("ibSettingClientID",{rules:[{required:!0,message:"\u8bf7\u8f93\u5165\u5ba2\u6237\u7aefID"}],initialValue:G})(w.default.createElement(d.default,{placeholder:"\u5ba2\u6237\u7aefID"}))),w.default.createElement(I,{className:h.default.formItem},t("ibSettingAccountCode",{initialValue:N})(w.default.createElement(d.default,{placeholder:"\u8d26\u6237\u4ee3\u7801"})))))))}}]),t}(w.PureComponent))||m),C=S;t.default=C},rT5C:function(e,t,a){e.exports={tagsTitle:"antd-pro\\pages\\-gateway\\-gateway-board-tagsTitle",teamTitle:"antd-pro\\pages\\-gateway\\-gateway-board-teamTitle",tags:"antd-pro\\pages\\-gateway\\-gateway-board-tags",team:"antd-pro\\pages\\-gateway\\-gateway-board-team",tabsCard:"antd-pro\\pages\\-gateway\\-gateway-board-tabsCard",formItem:"antd-pro\\pages\\-gateway\\-gateway-board-formItem"}},yNdQ:function(e,t,a){"use strict";var l=a("TqRt"),n=a("284h");Object.defineProperty(t,"__esModule",{value:!0}),t.default=void 0,a("14J3");var r=l(a("BMrR"));a("jCWc");var d=l(a("kPKH"));a("IzEo");var i=l(a("bx4M"));a("/zsF");var u=l(a("PArb"));a("+BJd");var s=l(a("mr32"));a("+L6B");var c=l(a("2/Rp"));a("g9YV");var o=l(a("wCAj")),f=l(a("lwsE")),y=l(a("W8MJ")),g=l(a("a1gu")),m=l(a("Nsbk")),p=l(a("7W2i"));a("2qtc");var w,h,v=l(a("kLXV")),E=n(a("q1tI")),I=a("MuoO"),S=l(a("v99g")),C=l(a("rT5C")),D=l(a("RLKE")),b=a("uI15"),k=v.default.confirm,G=(w=(0,I.connect)(function(e){var t=e.login,a=e.gateway;return{login:t,gateway:a}}),w(h=function(e){function t(e){var a;return(0,f.default)(this,t),a=(0,g.default)(this,(0,m.default)(t).call(this,e)),a.gatewayFormRef=E.default.createRef(),a.state={visible:!1,editGateway:null,fetchGatewaysLoading:!1},a}return(0,p.default)(t,e),(0,y.default)(t,[{key:"componentDidMount",value:function(){this.fetchGateways()}},{key:"handleGatewayEditModalOK",value:function(){var e=this.props,t=e.dispatch,a=e.login,l=this;this.gatewayFormRef.current.validateFieldsAndScroll(function(e,n){var r;if(null==e||void 0===e.gatewayType&&void 0===e.gatewayDisplayName&&void 0===e.gatewayClassName){if("CTP"===n.gatewayType){if(null!=e&&(void 0!==e.ctpSettingBrokerID||void 0!==e.ctpSettingMdAddress||void 0!==e.ctpSettingPassword||void 0!==e.ctpSettingUserID||void 0!==e.ctpSettingMdAddress||void 0!==e.ctpSettingTdAddress))return;r={token:a.token,gatewayID:n.gatewayID,gatewayClassName:n.gatewayClassName,gatewayDisplayName:n.gatewayDisplayName,gatewayType:n.gatewayType,ctpSetting:{userID:n.ctpSettingUserID,password:n.ctpSettingPassword,brokerID:n.ctpSettingBrokerID,mdAddress:n.ctpSettingMdAddress,tdAddress:n.ctpSettingTdAddress,authCode:n.ctpSettingAuthCode,userProductInfo:n.ctpSettingUserProductInfo}}}else if("IB"===n.gatewayType){if(null!=e&&(void 0!==e.ibSettingHost||void 0!==e.ibSettingPort||void 0!==e.ibSettingClientID))return;r={token:a.token,gatewayID:n.gatewayID,gatewayClassName:n.gatewayClassName,gatewayDisplayName:n.gatewayDisplayName,gatewayType:n.gatewayType,ibsetting:{host:n.ibSettingHost,port:n.ibSettingPort,clientID:n.ibSettingClientID,accountCode:n.ibSettingAccountCode}}}t({type:"gateway/savaOrUpdateGateway",payload:r}),(0,b.sleep)(200).then(function(){l.setState({visible:!1}),l.fetchGateways()})}})}},{key:"handleGatewayEditModalCancel",value:function(){this.setState({visible:!1})}},{key:"changeGatewayConnectStatus",value:function(e){var t=this.props,a=t.dispatch,l=t.login,n=this;n.setState({fetchGatewaysLoading:!0}),e.runtimeStatus?k({title:"\u786e\u5b9a\u8981\u65ad\u5f00\u8fd9\u4e2a\u7f51\u5173\u5417?",content:e.gatewayID,onOk:function(){a({type:"gateway/changeGatewayConnectStatus",payload:{token:l.token,gatewayID:e.gatewayID}}),(0,b.sleep)(1e3).then(function(){n.setState({visible:!1,fetchGatewaysLoading:!1}),n.fetchGateways()})},onCancel:function(){n.setState({fetchGatewaysLoading:!1})}}):(a({type:"gateway/changeGatewayConnectStatus",payload:{token:l.token,gatewayID:e.gatewayID}}),(0,b.sleep)(1e3).then(function(){n.setState({visible:!1,fetchGatewaysLoading:!1}),n.fetchGateways()}))}},{key:"deleteGateway",value:function(e){var t=this.props,a=t.dispatch,l=t.login,n=this;k({title:"\u786e\u5b9a\u8981\u5220\u9664\u8fd9\u4e2a\u7f51\u5173\u5417?",content:e.gatewayID,onOk:function(){a({type:"gateway/deleteGateway",payload:{token:l.token,gatewayID:e.gatewayID}}),(0,b.sleep)(100).then(function(){n.setState({visible:!1}),n.fetchGateways()})},onCancel:function(){}})}},{key:"saveOrUpdateGateway",value:function(e){this.setState({visible:!0,editGateway:e})}},{key:"fetchGateways",value:function(){var e=this.props,t=e.dispatch,a=e.login;t({type:"gateway/fetchGateways",payload:{token:a.token}})}},{key:"render",value:function(){var e=this,t=this.props.gateway,a=this.state,l=a.visible,n=a.editGateway,f=a.fetchGatewaysLoading,y={xs:{span:24},sm:{span:24},md:{span:22,offset:1},lg:{span:20,offset:2}},g=o.default.Column;return E.default.createElement(S.default,{className:C.default.userCenter},E.default.createElement(r.default,{gutter:24},E.default.createElement(d.default,y,E.default.createElement(i.default,null,E.default.createElement(c.default,{type:"primary",icon:"plus",onClick:function(){return e.saveOrUpdateGateway()}},"\u65b0\u589e"),E.default.createElement(c.default,{style:{marginLeft:10},type:"primary",icon:"reload",loading:f,onClick:function(){return e.fetchGateways()}},"\u5237\u65b0\u72b6\u6001"),E.default.createElement(o.default,{dataSource:t.gateways,scroll:{x:880},pagination:!1,rowKey:"gatewayID"},E.default.createElement(g,{title:"\u7f51\u5173\u72b6\u6001",dataIndex:"runtimeStatus",key:"runtimeStatus",width:120,render:function(e,t){return t.runtimeStatus?E.default.createElement("span",null,E.default.createElement(s.default,{color:"green"},"\u5df2\u8fde\u63a5")):E.default.createElement("span",null,E.default.createElement(s.default,{color:"red"},"\u5df2\u65ad\u5f00"))}}),E.default.createElement(g,{title:"\u7f51\u5173ID",dataIndex:"gatewayID",key:"gatewayID",width:200}),E.default.createElement(g,{title:"\u7f51\u5173\u540d\u79f0",dataIndex:"gatewayDisplayName",key:"gatewayDisplayName",width:160}),E.default.createElement(g,{title:"\u7f51\u5173Java\u5b9e\u73b0\u7c7b",dataIndex:"gatewayClassName",key:"gatewayClassName",width:200}),E.default.createElement(g,{title:"\u64cd\u4f5c",key:"action",width:200,render:function(t,a){return a.runtimeStatus?E.default.createElement("span",null,E.default.createElement("a",{href:"",onClick:function(t){t.preventDefault(),e.changeGatewayConnectStatus(a)}},"\u65ad\u5f00"),E.default.createElement(u.default,{type:"vertical"}),E.default.createElement("a",{href:"",onClick:function(t){t.preventDefault(),e.saveOrUpdateGateway(a)}},"\u4fee\u6539"),E.default.createElement(u.default,{type:"vertical"}),E.default.createElement("a",{href:"",onClick:function(t){t.preventDefault(),e.deleteGateway(a)}},"\u5220\u9664")):E.default.createElement("span",null,E.default.createElement("a",{href:"",onClick:function(t){t.preventDefault(),e.changeGatewayConnectStatus(a)}},"\u8fde\u63a5"),E.default.createElement(u.default,{type:"vertical"}),E.default.createElement("a",{href:"",onClick:function(t){t.preventDefault(),e.saveOrUpdateGateway(a)}},"\u4fee\u6539"),E.default.createElement(u.default,{type:"vertical"}),E.default.createElement("a",{href:"",onClick:function(t){t.preventDefault(),e.deleteGateway(a)}},"\u5220\u9664"))}}))),l&&E.default.createElement(v.default,{title:"\u7f16\u8f91\u7f51\u5173",visible:l,onOk:function(){return e.handleGatewayEditModalOK()},onCancel:function(){return e.handleGatewayEditModalCancel()},okText:"\u786e\u8ba4",cancelText:"\u53d6\u6d88"},E.default.createElement(D.default,{editGateway:n,ref:this.gatewayFormRef})))))}}]),t}(E.PureComponent))||h),N=G;t.default=N}}]);