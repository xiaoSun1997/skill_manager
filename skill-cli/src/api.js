import axios from 'axios';
import { getConfig } from './config.js';

export function createApi(serverUrl) {
  const config = getConfig();
  const baseURL = serverUrl || config.server;

  if (!baseURL) {
    throw new Error('请先使用 skill login 登录，或通过 --server 参数指定服务器地址');
  }

  const api = axios.create({
    baseURL: baseURL.replace(/\/+$/, ''),
    timeout: 30000
  });

  api.interceptors.request.use((reqConfig) => {
    if (config.token) {
      reqConfig.headers.Authorization = `Bearer ${config.token}`;
    }
    return reqConfig;
  });

  return api;
}
