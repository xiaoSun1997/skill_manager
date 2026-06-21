import fs from 'fs';
import path from 'path';
import os from 'os';

const CONFIG_DIR = path.join(os.homedir(), '.skill-cli');
const CONFIG_FILE = path.join(CONFIG_DIR, 'config.json');

export function getConfig() {
  try {
    if (!fs.existsSync(CONFIG_FILE)) {
      return { server: '', token: '', userInfo: null };
    }
    const raw = fs.readFileSync(CONFIG_FILE, 'utf-8');
    return JSON.parse(raw);
  } catch {
    return { server: '', token: '', userInfo: null };
  }
}

export function saveConfig(config) {
  if (!fs.existsSync(CONFIG_DIR)) {
    fs.mkdirSync(CONFIG_DIR, { recursive: true });
  }
  fs.writeFileSync(CONFIG_FILE, JSON.stringify(config, null, 2), 'utf-8');
}
