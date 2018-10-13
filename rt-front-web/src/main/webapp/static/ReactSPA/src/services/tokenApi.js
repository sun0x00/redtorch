import request from '@/utils/request';

export async function tokenLogin(params) {
  return request('/api/token/login', {
    method: 'POST',
    body: params,
  });
}

export async function tokenValidate(params) {
  return request('/api/token/validate', {
    method: 'POST',
    body: params,
  });
}
