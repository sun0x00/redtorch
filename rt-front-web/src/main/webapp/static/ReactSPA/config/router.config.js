export default [
  // user
  {
    path: '/user',
    component: '../layouts/UserLayout',
    routes: [
      { path: '/user/login', component: './User/Login' },
    ],
  },
  // app
  {
    path: '/',
    component: '../layouts/BasicLayout',
    routes: [
      // dashboard
      { path: '/', redirect: '/trade/basic' },
      {
        path: '/trade',
        name: 'trade',
        icon: 'dashboard',
        routes: [
          {
            path: '/trade/basic',
            name: 'basic',
            component: "./Trade/Basic/Dashboard",
            routes: [
              {
                path: '/trade/basic',
                redirect: '/trade/basic/tradeBoard',
              },
              {
                path: '/trade/basic/tradeBoard',
                component: './Trade/Basic/TradeBoard',
              },
              {
                path: '/trade/basic/positions',
                component: './Trade/Basic/Positions',
              },
              {
                path: '/trade/basic/accounts',
                component: './Trade/Basic/Accounts',
              },
              {
                path: '/trade/basic/transactions',
                component: './Trade/Basic/Transactions',
              },
              {
                path: '/trade/basic/orders',
                component: './Trade/Basic/Orders',
              },
            ],
          },
          {
            path: '/trade/multiAccount',
            name: 'multiAccount',
            component: "./Trade/MultiAccount/Dashboard",
            routes: [
              {
                path: '/trade/multiAccount',
                redirect: '/trade/MultiAccount/tradeBoard',
              },
              {
                path: '/trade/multiAccount/tradeBoard',
                component: './Trade/MultiAccount/TradeBoard',
              },
              {
                path: '/trade/multiAccount/positions',
                component: './Trade/Basic/Positions',
              },
              {
                path: '/trade/multiAccount/accounts',
                component: './Trade/Basic/Accounts',
              },
              {
                path: '/trade/multiAccount/transactions',
                component: './Trade/Basic/Transactions',
              },
              {
                path: '/trade/multiAccount/orders',
                component: './Trade/Basic/Orders',
              },
            ],
          }
        ],
      },
      {
        path: '/strategy',
        name: 'strategy',
        icon: 'form',
        routes: [
          {
            path: '/strategy/Zeus',
            name: 'zeus',
            component: './Strategy/Zeus/ZeusBoard',
          },
        ],
      },
      {
        path: '/gateway',
        name: 'gateway',
        icon: 'api',
        component: './Gateway/GatewayBoard',
      },
      // {
      //   path: '/dataRecord',
      //   name: 'dataRecord',
      //   icon: 'database',
      //   component: './DataRecord/DataRecordBoard',
      // },
      // {
      //   path: '/riskControl',
      //   name: 'riskControl',
      //   icon: 'safety-certificate',
      //   component: './RiskControl/RiskControlBoard',
      // },
      // {
      //   path: '/log',
      //   name: 'log',
      //   icon: 'file-search',
      //   component: './Log/LogBoard',
      // },
      {
        name: 'exception',
        icon: 'warning',
        path: '/exception',
        routes: [
          // exception
          {
            path: '/exception/403',
            name: 'not-permission',
            component: './Exception/403',
          },
          {
            path: '/exception/404',
            name: 'not-find',
            component: './Exception/404',
          },
          {
            path: '/exception/500',
            name: 'server-error',
            component: './Exception/500',
          },
          {
            path: '/exception/trigger',
            name: 'trigger',
            hideInMenu: true,
            component: './Exception/TriggerException',
          },
        ],
      },
      {
        component: '404',
      },
    ],
  },
];
