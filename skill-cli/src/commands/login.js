import { Command } from 'commander';
import axios from 'axios';
import { saveConfig, getConfig } from '../config.js';

export const loginCmd = new Command('login')
  .description('登录到 Skill DB Manager 服务器')
  .requiredOption('--server <url>', '服务器地址，例如 http://localhost:8080')
  .requiredOption('--username <name>', '用户名')
  .requiredOption('--password <pass>', '密码')
  .action(async (options) => {
    try {
      const api = axios.create({ baseURL: options.server.replace(/\/+$/, '') });
      const res = await api.post('/api/cli/auth/token', {
        username: options.username,
        password: options.password
      });

      if (res.data.code !== 0 && res.data.code !== 200) {
        console.error('登录失败:', res.data.message || '认证失败');
        process.exit(1);
      }

      const data = res.data.data || res.data;
      saveConfig({
        server: options.server,
        token: data.accessToken,
        userInfo: data.userInfo
      });

      console.log(`✓ 登录成功，欢迎 ${data.userInfo.nickname || data.userInfo.username}！`);
    } catch (e) {
      console.error('登录失败:', e.response?.data?.message || e.message);
      process.exit(1);
    }
  });
