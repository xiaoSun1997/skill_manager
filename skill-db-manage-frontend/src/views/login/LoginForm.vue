<template>
  <form class="login-form" @submit.prevent="handleSubmit">
    <div class="input-group">
      <div class="input-field" :class="{ focused: focusedField === 'username', filled: formData.username }">
        <input
          v-model="formData.username"
          type="text"
          id="username"
          autocomplete="username"
          @focus="focusedField = 'username'"
          @blur="focusedField = ''"
          placeholder=" "
        />
        <label for="username">用户名</label>
        <div class="input-border"></div>
        <span class="input-glow"></span>
      </div>
      <p v-if="errors.username" class="error-text">{{ errors.username }}</p>
    </div>

    <div class="input-group">
      <div class="input-field" :class="{ focused: focusedField === 'password', filled: formData.password }">
        <input
          v-model="formData.password"
          :type="showPassword ? 'text' : 'password'"
          id="password"
          autocomplete="current-password"
          @focus="focusedField = 'password'"
          @blur="focusedField = ''"
          placeholder=" "
        />
        <label for="password">密码</label>
        <div class="input-border"></div>
        <span class="input-glow"></span>
        <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
          <Icon :icon="showPassword ? 'ri:eye-off-line' : 'ri:eye-line'" width="16" height="16" />
        </button>
      </div>
      <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
    </div>

    <div class="form-options">
      <label class="remember-me">
        <input type="checkbox" v-model="rememberMe" />
        <span class="checkmark"></span>
        <span class="label-text">记住我</span>
      </label>
      <button type="button" class="forgot-link">忘记密码？</button>
    </div>

    <button type="submit" class="submit-btn" :disabled="loading">
      <span class="btn-text">{{ loading ? '验证中...' : '登录系统' }}</span>
      <span class="btn-arrow">
        <Icon icon="ri:arrow-right-line" width="18" height="18" />
      </span>
      <div class="btn-loader" v-if="loading">
        <span></span><span></span><span></span>
      </div>
    </button>
  </form>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { Icon } from '@iconify/vue'

const emit = defineEmits(['login'])
const loading = ref(false)
const showPassword = ref(false)
const rememberMe = ref(false)
const focusedField = ref('')

const formData = reactive({ username: '', password: '' })
const errors = reactive({ username: '', password: '' })

function validate() {
  let valid = true
  if (!formData.username) { errors.username = '请输入用户名'; valid = false }
  else { errors.username = '' }
  if (!formData.password) { errors.password = '请输入密码'; valid = false }
  else { errors.password = '' }
  return valid
}

async function handleSubmit() {
  if (!validate()) return
  loading.value = true
  try {
    emit('login', {
      username: formData.username,
      password: formData.password,
      loginType: 'password'
    })
  } catch (e) {
    // handled by parent
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-form {
  width: 100%;
}

.input-group {
  margin-bottom: 20px;
}

.input-field {
  position: relative;
}

.input-field input {
  width: 100%;
  height: 48px;
  padding: 16px 14px 4px;
  background: rgba(255, 255, 255, 0.03);
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.input-field label {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(255, 255, 255, 0.3);
  font-size: 14px;
  pointer-events: none;
  transition: all 0.25s ease;
  z-index: 2;
}

.input-field.focused label,
.input-field.filled label {
  top: 10px;
  transform: translateY(0);
  font-size: 11px;
  color: rgba(99, 102, 241, 0.7);
}

.input-border {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: #6366F1;
  transition: all 0.3s ease;
  border-radius: 0 0 8px 8px;
  z-index: 3;
}

.input-field.focused .input-border {
  left: 0;
  width: 100%;
}

.input-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.05);
}

.input-field.focused .input-glow {
  opacity: 1;
}

.toggle-password {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.2);
  font-size: 16px;
  cursor: pointer;
  z-index: 4;
  padding: 4px;
}

.toggle-password:hover {
  color: rgba(255, 255, 255, 0.5);
}

.error-text {
  color: #f53f3f;
  font-size: 11px;
  margin-top: 4px;
  padding-left: 4px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.remember-me input {
  display: none;
}

.checkmark {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  position: relative;
}

.remember-me input:checked + .checkmark {
  background: #6366F1;
  border-color: #6366F1;
}

.remember-me input:checked + .checkmark::after {
  content: '✓';
  color: #fff;
  font-size: 11px;
  font-weight: bold;
}

.label-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.forgot-link {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s;
  font-family: inherit;
}

.forgot-link:hover {
  color: rgba(99, 102, 241, 0.7);
}

.submit-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  font-family: inherit;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 30px rgba(99, 102, 241, 0.3);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-text {
  transition: opacity 0.3s;
}

.btn-arrow {
  font-size: 18px;
  transition: transform 0.3s;
}

.submit-btn:hover:not(:disabled) .btn-arrow {
  transform: translateX(4px);
}

.btn-loader {
  display: flex;
  gap: 4px;
}

.btn-loader span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
  animation: bounce 0.6s infinite alternate;
}

.btn-loader span:nth-child(2) { animation-delay: 0.2s; }
.btn-loader span:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  from { transform: translateY(0); opacity: 0.5; }
  to { transform: translateY(-6px); opacity: 1; }
}
</style>
