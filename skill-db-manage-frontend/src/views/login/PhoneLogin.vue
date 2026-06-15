<template>
  <form class="login-form" @submit.prevent="handleSubmit">
    <div class="input-group">
      <div class="input-field" :class="{ focused: focusedField === 'phone', filled: formData.phone }">
        <input
          v-model="formData.phone"
          type="tel"
          id="phone"
          maxlength="11"
          autocomplete="tel"
          @focus="focusedField = 'phone'"
          @blur="focusedField = ''"
          placeholder=" "
        />
        <label for="phone">手机号</label>
        <div class="input-border"></div>
        <span class="input-glow"></span>
      </div>
      <p v-if="errors.phone" class="error-text">{{ errors.phone }}</p>
    </div>

    <div class="input-group">
      <div class="input-field code-field" :class="{ focused: focusedField === 'code', filled: formData.code }">
        <input
          v-model="formData.code"
          type="text"
          id="code"
          maxlength="6"
          autocomplete="one-time-code"
          @focus="focusedField = 'code'"
          @blur="focusedField = ''"
          placeholder=" "
        />
        <label for="code">验证码</label>
        <div class="input-border"></div>
        <span class="input-glow"></span>
        <button
          type="button"
          class="send-code-btn"
          :disabled="countdown > 0"
          @click="handleSendCode"
        >
          {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
        </button>
      </div>
      <p v-if="errors.code" class="error-text">{{ errors.code }}</p>
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
import { Message } from '@arco-design/web-vue'
import { sendSmsCode } from '@/api/auth'

const emit = defineEmits(['login'])
const loading = ref(false)
const countdown = ref(0)
const focusedField = ref('')
let timer = null

const formData = reactive({ phone: '', code: '' })
const errors = reactive({ phone: '', code: '' })

function validate() {
  let valid = true
  if (!formData.phone) { errors.phone = '请输入手机号'; valid = false }
  else if (!/^1[3-9]\d{9}$/.test(formData.phone)) { errors.phone = '手机号格式不正确'; valid = false }
  else { errors.phone = '' }

  if (!formData.code) { errors.code = '请输入验证码'; valid = false }
  else { errors.code = '' }
  return valid
}

async function handleSendCode() {
  if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
    Message.warning('请输入正确的手机号')
    return
  }
  try {
    await sendSmsCode(formData.phone)
    Message.success('验证码已发送')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e) {
    Message.error(e.message || '发送失败')
  }
}

async function handleSubmit() {
  if (!validate()) return
  loading.value = true
  try {
    emit('login', { phone: formData.phone, code: formData.code })
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

.code-field input {
  padding-right: 110px;
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

.send-code-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: 1px solid rgba(99, 102, 241, 0.2);
  color: #6366F1;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  z-index: 4;
  transition: all 0.2s;
  font-family: inherit;
  white-space: nowrap;
}

.send-code-btn:hover:not(:disabled) {
  background: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.4);
}

.send-code-btn:disabled {
  color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.08);
  cursor: not-allowed;
}

.error-text {
  color: #f53f3f;
  font-size: 11px;
  margin-top: 4px;
  padding-left: 4px;
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
  margin-top: 8px;
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
