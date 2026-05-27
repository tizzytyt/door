<template>
  <div class="container">
    <div class="card">
      <div class="title">
        <h1>超级管理员登录</h1>
      </div>

      <form @submit.prevent="onSubmit">
        <div class="field">
          <label>账号</label>
          <input class="input" v-model.trim="form.username" autocomplete="username" placeholder="请输入账号" />
        </div>

        <div class="field">
          <label>密码</label>
          <input
            class="input"
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            placeholder="请输入密码"
          />
        </div>

        <div class="field">
          <label>验证码</label>
          <div class="captchaRow">
            <input
              class="input captchaInput"
              v-model.trim="form.captchaCode"
              maxlength="4"
              autocomplete="off"
              placeholder="请输入验证码"
            />
            <img
              v-if="captchaImage"
              class="captchaImg"
              :src="captchaImage"
              alt="验证码"
              title="点击刷新"
              @click="loadCaptcha"
            />
            <span v-else class="captchaPlaceholder" @click="loadCaptcha">加载中</span>
          </div>
          <button type="button" class="linkbtn captchaRefresh" @click="loadCaptcha">看不清？换一张</button>
        </div>

        <button class="btn" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <div v-if="error" class="error">
          {{ error }}
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { http } from '../services/http.js'
import { auth } from '../services/auth.js'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const error = ref(null)

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
})

const captchaKey = ref('')
const captchaImage = ref('')

async function loadCaptcha() {
  try {
    const res = await http.get('/captcha')
    if (res.data?.code !== 200) {
      error.value = res.data?.msg || '验证码加载失败'
      return
    }
    captchaKey.value = res.data?.data?.captchaKey || ''
    captchaImage.value = res.data?.data?.captchaImage || ''
    form.captchaCode = ''
    error.value = null
  } catch (e) {
    error.value = e?.response?.data?.msg || e?.message || '验证码加载失败'
  }
}

onMounted(loadCaptcha)

async function onSubmit() {
  error.value = null
  if (!form.username || !form.password) {
    error.value = '请输入账号和密码'
    return
  }
  if (!form.captchaCode || !captchaKey.value) {
    error.value = '请输入验证码'
    return
  }

  loading.value = true
  try {
    const res = await http.post('/login', {
      username: form.username,
      password: form.password,
      role: 'super_admin',
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode,
    })

    if (res.data?.code !== 200) {
      error.value = res.data?.msg || '登录失败'
      return
    }

    const token = res.data?.data?.token
    const user = res.data?.data?.user
    if (!token) {
      error.value = '未获取到 token'
      return
    }
    if (user?.role && user.role !== 'super_admin') {
      error.value = '该账号不是超级管理员'
      return
    }

    auth.setSession(token, user)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.replace(redirect)
  } catch (e) {
    const msg = e?.response?.data?.msg || e?.message || '网络错误'
    error.value = msg
  } finally {
    loading.value = false
    await loadCaptcha()
  }
}
</script>

<style scoped>
.captchaRow {
  display: flex;
  gap: 10px;
  align-items: center;
}
.captchaInput {
  flex: 1;
  min-width: 0;
}
.captchaImg {
  width: 120px;
  height: 40px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.captchaPlaceholder {
  width: 120px;
  height: 40px;
  display: grid;
  place-items: center;
  font-size: 12px;
  color: #94a3b8;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.captchaRefresh {
  margin-top: 8px;
  padding: 0;
  font-size: 13px;
  color: #2563eb;
  background: none;
  border: none;
  cursor: pointer;
}
</style>

