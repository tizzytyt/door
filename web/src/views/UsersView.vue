<template>
  <div class="container" style="place-items: start center;">
    <div class="topbar">
      <div style="display:flex; gap:10px; align-items:center;">
        <div style="font-weight:800;">用户管理</div>
        <div class="pill">已登录：{{ userLabel }}</div>
      </div>
      <div style="display:flex; gap:10px; align-items:center;">
        <button class="linkbtn" @click="openCreateAdmin">创建管理员</button>
        <button class="linkbtn" @click="goDashboard">返回首页</button>
        <button class="linkbtn" @click="logout">退出登录</button>
      </div>
    </div>

    <div class="card" style="max-width: 960px; margin-top: 14px;">
      <div style="display:flex; gap:10px; flex-wrap:wrap; align-items:end;">
        <div class="field" style="margin:0; flex:1; min-width: 220px;">
          <label>关键词（账号/姓名/手机号）</label>
          <input class="input" v-model.trim="filters.keyword" placeholder="输入关键词筛选" />
        </div>
        <div class="field" style="margin:0; width: 200px;">
          <label>角色</label>
          <select class="input" v-model="filters.role">
            <option value="">全部</option>
            <option value="student">学生</option>
            <option value="admin">管理员</option>
            <option value="super_admin">超级管理员</option>
          </select>
        </div>
        <div class="field" style="margin:0; width: 160px;">
          <label>状态</label>
          <select class="input" v-model="filters.status">
            <option value="">全部</option>
            <option value="1">启用</option>
            <option value="0">封禁</option>
          </select>
        </div>
        <div style="display:flex; gap:10px;">
          <button class="linkbtn" @click="reload" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
        </div>
      </div>

      <div v-if="error" class="error">{{ error }}</div>

      <div style="overflow:auto; margin-top: 14px;">
        <table style="width:100%; border-collapse: collapse; font-size: 13px;">
          <thead>
            <tr style="text-align:left; border-bottom: 1px solid rgba(15, 23, 42, 0.10);">
              <th style="padding:10px 8px;">ID</th>
              <th style="padding:10px 8px;">账号</th>
              <th style="padding:10px 8px;">姓名</th>
              <th style="padding:10px 8px;">角色</th>
              <th style="padding:10px 8px;">手机号</th>
              <th style="padding:10px 8px;">状态</th>
              <th style="padding:10px 8px; width: 260px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="u in filtered"
              :key="u.id"
              style="border-bottom: 1px solid rgba(15, 23, 42, 0.06);"
            >
              <td style="padding:10px 8px;">{{ u.id }}</td>
              <td style="padding:10px 8px;">{{ u.username }}</td>
              <td style="padding:10px 8px;">{{ u.realName || '-' }}</td>
              <td style="padding:10px 8px;">{{ roleText(u.role) }}</td>
              <td style="padding:10px 8px;">{{ u.phone || '-' }}</td>
              <td style="padding:10px 8px;">
                <span
                  :style="{
                    padding: '4px 8px',
                    borderRadius: '999px',
                    border: '1px solid rgba(15,23,42,0.12)',
                    background: u.status === 1 ? 'rgba(34,197,94,0.08)' : 'rgba(255,77,79,0.08)',
                    color: u.status === 1 ? 'rgba(21,128,61,0.98)' : 'rgba(127,29,29,0.98)',
                  }"
                >
                  {{ u.status === 1 ? '启用' : '封禁' }}
                </span>
              </td>
              <td style="padding:10px 8px;">
                <div style="display:flex; gap:8px; flex-wrap:wrap;">
                  <button class="linkbtn" @click="openResetPwd(u)" :disabled="busyId === u.id">重置密码</button>
                  <button
                    class="linkbtn"
                    @click="toggleStatus(u)"
                    :disabled="busyId === u.id || u.role === 'super_admin'"
                    :title="u.role === 'super_admin' ? '不允许操作超级管理员' : ''"
                  >
                    {{ u.status === 1 ? '封禁' : '启用' }}
                  </button>
                </div>
              </td>
            </tr>

            <tr v-if="!loading && filtered.length === 0">
              <td colspan="7" style="padding: 14px 8px; color: rgba(15,23,42,0.62);">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 创建管理员 -->
    <div v-if="createAdminVisible" class="modalMask" @click.self="createAdminVisible = false">
      <div class="modal">
        <div style="font-weight: 800; margin-bottom: 10px;">创建管理员账号</div>
        <div class="field">
          <label>账号</label>
          <input class="input" v-model.trim="createAdminForm.username" placeholder="例如：admin001" />
        </div>
        <div class="field">
          <label>密码</label>
          <input class="input" type="password" v-model="createAdminForm.password" placeholder="至少6位" />
        </div>
        <div class="field">
          <label>姓名</label>
          <input class="input" v-model.trim="createAdminForm.realName" placeholder="管理员姓名" />
        </div>
        <div class="field">
          <label>手机号（可选）</label>
          <input class="input" v-model.trim="createAdminForm.phone" placeholder="11位手机号或留空" />
        </div>
        <div v-if="createAdminError" class="error">{{ createAdminError }}</div>
        <div style="display:flex; gap:10px; margin-top: 12px;">
          <button class="btn" style="width:auto; padding: 10px 14px;" :disabled="createAdminLoading" @click="submitCreateAdmin">
            {{ createAdminLoading ? '提交中...' : '创建' }}
          </button>
          <button class="linkbtn" style="padding: 10px 14px;" @click="createAdminVisible = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 重置密码 -->
    <div v-if="resetPwdVisible" class="modalMask" @click.self="closeResetPwd">
      <div class="modal">
        <div style="font-weight: 800; margin-bottom: 6px;">重置密码</div>
        <div style="color: rgba(15,23,42,0.62); font-size: 13px; margin-bottom: 10px;">
          账号：<b>{{ resetPwdUser?.username }}</b>（ID：{{ resetPwdUser?.id }}）
        </div>
        <div class="field">
          <label>新密码</label>
          <input class="input" type="password" v-model="resetPwdForm.newPassword" placeholder="输入新密码（至少6位）" />
        </div>
        <div v-if="resetPwdError" class="error">{{ resetPwdError }}</div>
        <div style="display:flex; gap:10px; margin-top: 12px;">
          <button class="btn" style="width:auto; padding: 10px 14px;" :disabled="resetPwdLoading" @click="submitResetPwd">
            {{ resetPwdLoading ? '提交中...' : '确认重置' }}
          </button>
          <button class="linkbtn" style="padding: 10px 14px;" @click="closeResetPwd">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '../services/auth.js'
import { adminApi } from '../services/adminApi.js'

const router = useRouter()
const me = auth.getUser()

const userLabel = computed(() => {
  const name = me?.realName || me?.username || '未知用户'
  return `${name} (${me?.role || '-'})`
})

const loading = ref(false)
const error = ref(null)
const busyId = ref(null)
const users = ref([])

const filters = reactive({
  keyword: '',
  role: '',
  status: '',
})

const filtered = computed(() => {
  const kw = filters.keyword.trim().toLowerCase()
  return users.value.filter((u) => {
    if (filters.role && u.role !== filters.role) return false
    if (filters.status && String(u.status ?? 1) !== filters.status) return false
    if (!kw) return true
    const hit =
      String(u.username ?? '').toLowerCase().includes(kw) ||
      String(u.realName ?? '').toLowerCase().includes(kw) ||
      String(u.phone ?? '').toLowerCase().includes(kw)
    return hit
  })
})

function roleText(role) {
  if (role === 'student') return '学生'
  if (role === 'admin') return '管理员'
  if (role === 'super_admin') return '超级管理员'
  return role || '-'
}

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.listUsers()
    if (res.code !== 200) {
      error.value = res.msg || '加载失败'
      return
    }
    users.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = e?.response?.data?.msg || e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

async function toggleStatus(u) {
  if (!u?.id) return
  busyId.value = u.id
  try {
    const next = u.status === 1 ? 0 : 1
    const res = await adminApi.updateUserStatus(u.id, next)
    if (res.code !== 200) {
      error.value = res.msg || '操作失败'
      return
    }
    u.status = next
  } catch (e) {
    error.value = e?.response?.data?.msg || e?.message || '网络错误'
  } finally {
    busyId.value = null
  }
}

function goDashboard() {
  router.push('/dashboard')
}

function logout() {
  auth.clear()
  router.replace('/login')
}

// 创建管理员
const createAdminVisible = ref(false)
const createAdminLoading = ref(false)
const createAdminError = ref(null)
const createAdminForm = reactive({ username: '', password: '', realName: '', phone: '' })

function openCreateAdmin() {
  createAdminError.value = null
  createAdminForm.username = ''
  createAdminForm.password = ''
  createAdminForm.realName = ''
  createAdminForm.phone = ''
  createAdminVisible.value = true
}

async function submitCreateAdmin() {
  createAdminError.value = null
  if (!createAdminForm.username || !createAdminForm.password || !createAdminForm.realName) {
    createAdminError.value = '请填写账号、密码、姓名'
    return
  }
  if (createAdminForm.password.length < 6) {
    createAdminError.value = '密码至少6位'
    return
  }
  createAdminLoading.value = true
  try {
    const res = await adminApi.createAdmin({
      username: createAdminForm.username,
      password: createAdminForm.password,
      realName: createAdminForm.realName,
      phone: createAdminForm.phone || undefined,
    })
    if (res.code !== 200) {
      createAdminError.value = res.msg || '创建失败'
      return
    }
    createAdminVisible.value = false
    await reload()
  } catch (e) {
    createAdminError.value = e?.response?.data?.msg || e?.message || '网络错误'
  } finally {
    createAdminLoading.value = false
  }
}

// 重置密码
const resetPwdVisible = ref(false)
const resetPwdLoading = ref(false)
const resetPwdError = ref(null)
const resetPwdUser = ref(null)
const resetPwdForm = reactive({ newPassword: '' })

function openResetPwd(u) {
  resetPwdError.value = null
  resetPwdUser.value = u
  resetPwdForm.newPassword = ''
  resetPwdVisible.value = true
}

function closeResetPwd() {
  resetPwdVisible.value = false
  resetPwdUser.value = null
  resetPwdForm.newPassword = ''
  resetPwdError.value = null
}

async function submitResetPwd() {
  resetPwdError.value = null
  if (!resetPwdUser.value?.id) return
  if (!resetPwdForm.newPassword) {
    resetPwdError.value = '请输入新密码'
    return
  }
  if (resetPwdForm.newPassword.length < 6) {
    resetPwdError.value = '密码至少6位'
    return
  }
  resetPwdLoading.value = true
  try {
    const res = await adminApi.resetUserPassword(resetPwdUser.value.id, resetPwdForm.newPassword)
    if (res.code !== 200) {
      resetPwdError.value = res.msg || '重置失败'
      return
    }
    closeResetPwd()
  } catch (e) {
    resetPwdError.value = e?.response?.data?.msg || e?.message || '网络错误'
  } finally {
    resetPwdLoading.value = false
  }
}

onMounted(reload)
</script>

<style scoped>
.modalMask{
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  display: grid;
  place-items: center;
  padding: 20px;
}
.modal{
  width: min(520px, 100%);
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.20);
  padding: 16px;
}
</style>

