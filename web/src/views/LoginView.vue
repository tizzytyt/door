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
import { reactive, ref } from 'vue'
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
})

async function onSubmit() {
  error.value = null
  if (!form.username || !form.password) {
    error.value = '请输入账号和密码'
    return
  }

  loading.value = true
  try {
    const res = await http.post('/login', {
      username: form.username,
      password: form.password,
      role: 'super_admin',
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
  }
}
</script>

