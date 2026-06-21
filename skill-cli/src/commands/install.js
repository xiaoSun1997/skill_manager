import { Command } from 'commander';
import { createApi } from '../api.js';
import AdmZip from 'adm-zip';
import fs from 'fs';
import path from 'path';

export const installCmd = new Command('install')
  .description('下载并安装技能组到本地项目')
  .argument('<groupName>', '技能组名称')
  .option('--dir <target>', '目标目录（默认为当前目录）')
  .option('--server <url>', '服务器地址（可选）')
  .action(async (groupName, options) => {
    try {
      const api = createApi(options.server);
      const targetDir = options.dir ? path.resolve(options.dir) : process.cwd();
      const skillDir = path.join(targetDir, '.skill', groupName);

      console.log(`正在查找技能组 "${groupName}"...`);

      // 1. 查找技能组 UUID
      const listRes = await api.get('/api/skill/groups', {
        params: { pageNum: 1, pageSize: 100, keyword: groupName }
      });

      const data = listRes.data.data || listRes.data;
      const records = data.records || [];
      const group = records.find(r => r.name === groupName);

      if (!group) {
        console.error(`未找到技能组: ${groupName}`);
        if (records.length > 0) {
          console.log('可用的技能组:');
          records.forEach(r => console.log(`  - ${r.name}`));
        }
        process.exit(1);
      }

      console.log(`找到技能组: ${groupName} (${group.uuid})`);
      console.log('正在下载...');

      // 2. 下载 ZIP
      const downloadRes = await api.get(`/api/skill/groups/${group.uuid}/download`, {
        responseType: 'arraybuffer'
      });

      // 3. 解压到目标目录
      if (fs.existsSync(skillDir)) {
        fs.rmSync(skillDir, { recursive: true, force: true });
      }
      fs.mkdirSync(skillDir, { recursive: true });

      const zip = new AdmZip(downloadRes.data);
      zip.extractAllTo(skillDir, true);

      console.log(`✓ 技能组 "${groupName}" 已安装到 ${skillDir}`);
      console.log(`  包含 ${group.folderNames ? JSON.parse(group.folderNames).length : 0} 个子文件夹`);
    } catch (e) {
      console.error('安装失败:', e.response?.data?.message || e.message);
      process.exit(1);
    }
  });
