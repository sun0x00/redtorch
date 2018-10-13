import { routerRedux } from 'dva/router';
import { stringify } from 'qs';
import { tokenLogin} from '@/services/tokenApi';
import { getPageQuery } from '@/utils/utils';

export default {
  namespace: 'login',

  state: {
    status: undefined,
    token: sessionStorage.getItem("token"),
  },

  effects: {
    *login({ payload }, { call, put }) {
      const response = yield call(tokenLogin, payload);
      yield put({
        type: 'changeLoginStatus',
        payload: response,
      });
      sessionStorage.setItem('token', response.data);
      // Login successfully
      if (response.status === 'success') {
        const urlParams = new URL(window.location.href);
        const params = getPageQuery();
        let { redirect } = params;
        if (redirect) {
          const redirectUrlParams = new URL(redirect);
          if (redirectUrlParams.origin === urlParams.origin) {
            redirect = redirect.substr(urlParams.origin.length);
            if (redirect.startsWith('/#')) {
              redirect = redirect.substr(2);
            }
          } else {
            window.location.href = redirect;
            return;
          }
        }
        yield put(routerRedux.replace(redirect || '/'));
      }
    },

    *logout(_, { put }) {

      sessionStorage.removeItem("token")

      yield put({
        type: 'changeLoginStatus',
        payload: {
          status: false,
          token:undefined
        },
      });
      
     
      
      const params = getPageQuery();
      const { redirect } = params;
      if(!redirect){
        yield put(
          routerRedux.push({
            pathname: '/user/login',
            search: stringify({
              redirect: window.location.href,
            }),
          })
        );
      }
    },
  },

  reducers: {
    changeLoginStatus(state, { payload }) {
      return {
        ...state,
        status: payload.status,
        token: payload.data,
      };
    },
  },
};
