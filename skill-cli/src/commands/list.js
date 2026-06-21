import { Command } from 'commander';
import { createApi } from '../api.js';

export const listCmd = new Command('list')
  .description('列出所有可用的技能组')
  .option('--server <url>', '服务器地址（可选，使用已保存的配置）')
  .action(async (options) => {
    try {
      const api = createApi(options.server);
      const res = await api.get('/api/skill/groups', {
        params: { pageNum: 1, pageSize: 100 }
      });

      const data = res.data.data || res.data;
      const records = data.records || [];

      if (records.length === 0) {
        console.log('暂无技能组');
        return;
      }

      console.log('\n可用技能组列表:\n');
      console.log('名称'.padEnd(30) + '描述');
      console.log('-'.repeat(60));

      for (const group of records) {
        const desc = (group.description || '').substring(0, 40);
        console.log(group.name.padEnd(30) + desc);
      }

      console.log(`\n共 ${records.length} 个技能组`);
    } catch (e) {
      console.error('获取技能组列表失败:', e.response?.data?.message || e.message);
      process.exit(1);
    }
  });
