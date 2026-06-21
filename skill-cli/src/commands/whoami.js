import { Command } from 'commander';
import { getConfig } from '../config.js';

export const whoamiCmd = new Command('whoami')
  .description('显示当前登录用户信息')
  .action(() => {
    const config = getConfig();

    if (!config.token) {
      console.log('未登录，请使用 skill login 命令登录');
      return;
    }

    console.log('\n当前登录信息:\n');
    console.log('服务器:  '.padEnd(12) + (config.server || '(未设置)'));
    console.log('用户名:  '.padEnd(12) + (config.userInfo?.username || '(未知)'));
    console.log('昵称:    '.padEnd(12) + (config.userInfo?.nickname || '(未知)'));
    console.log('Token:   '.padEnd(12) + config.token.substring(0, 20) + '...');
  });
