<template>
  <div class="pageCard">
    <div style="display:flex; justify-content:space-between; gap: 12px; flex-wrap: wrap; align-items: center;">
      <div style="font-weight: 900; font-size: 16px;">黑名单管理</div>
      <div style="display:flex; gap:10px;">
        <button class="linkbtn" @click="openAdd">添加黑名单</button>
        <button class="linkbtn" @click="reload" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
      </div>
    </div>

    <div style="display:flex; gap:10px; flex-wrap:wrap; margin-top: 10px;">
      <input class="input" style="width: 280px; padding: 8px 10px;" v-model.trim="keyword" placeholder="关键词（账号/姓名/原因/用户ID）" />
      <select class="input" style="width: 160px; padding: 8px 10px;" v-model="expiryFilter">
        <option value="">全部</option>
        <option value="active">未过期/永久</option>
        <option value="expired">已过期</option>
      </select>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div style="margin-top: 12px; overflow:auto;">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户ID</th>
            <th>账号</th>
            <th>姓名</th>
            <th>原因</th>
            <th>到期时间</th>
            <th>创建时间</th>
            <th style="width: 140px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in filtered" :key="b.id">
            <td>{{ b.id }}</td>
            <td>{{ b.userId }}</td>
            <td>{{ b.username || '-' }}</td>
            <td>{{ b.realName || '-' }}</td>
            <td style="max-width: 260px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              {{ b.reason || '-' }}
            </td>
            <td>
              <span :style="badgeStyle(isExpired(b.expiryDate))">
                {{ b.expiryDate ? formatDt(b.expiryDate) : '永久' }}
              </span>
            </td>
            <td>{{ b.createdAt ? formatDt(b.createdAt) : '-' }}</td>
            <td>
              <button class="linkbtn" @click="remove(b)" :disabled="busyUserId===b.userId">移除</button>
            </td>
          </tr>
          <tr v-if="!loading && filtered.length === 0">
            <td colspan="8" style="color:rgba(15,23,42,0.62);">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 添加黑名单 -->
    <div v-if="modalVisible" class="modalMask" @click.self="closeModal">
      <div class="modal">
        <div style="font-weight: 900; margin-bottom: 8px;">添加黑名单</div>
        <div class="field" style="margin:0;">
          <label>用户ID</label>
          <input class="input" v-model.trim="form.userId" placeholder="请输入用户ID" />
        </div>
        <div class="field">
          <label>原因（可选）</label>
          <input class="input" v-model.trim="form.reason" placeholder="例如：多次爽约" />
        </div>
        <div class="field">
          <label>到期时间（可选，留空=永久）</label>
          <input class="input" type="datetime-local" v-model="form.expiryDate" />
        </div>
        <div v-if="modalError" class="error">{{ modalError }}</div>
        <div style="display:flex; gap:10px; margin-top: 12px;">
          <button class="btn" style="width:auto; padding: 10px 14px;" :disabled="modalLoading" @click="submit">
            {{ modalLoading ? '提交中...' : '确认添加' }}
          </button>
          <button class="linkbtn" style="padding: 10px 14px;" @click="closeModal">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { adminApi } from '../services/adminApi.js'

const loading = ref(false)
const error = ref(null)
const busyUserId = ref(null)

const list = ref([])
const keyword = ref('')
const expiryFilter = ref('')

function parseDate(v) {
  if (!v) return null
  const d = new Date(v)
  return Number.isNaN(d.getTime()) ? null : d
}

function isExpired(expiryDate) {
  const d = parseDate(expiryDate)
  if (!d) return false // 永久 or 空
  return d.getTime() <= Date.now()
}

function formatDt(v) {
  const d = parseDate(v)
  if (!d) return String(v || '')
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function badgeStyle(expired) {
  if (!expired) {
    return {
      padding: '4px 8px',
      borderRadius: '999px',
      border: '1px solid rgba(34,197,94,0.25)',
      background: 'rgba(34,197,94,0.08)',
      color: 'rgba(21,128,61,0.98)',
    }
  }
  return {
    padding: '4px 8px',
    borderRadius: '999px',
    border: '1px solid rgba(255,77,79,0.25)',
    background: 'rgba(255,77,79,0.08)',
    color: 'rgba(127,29,29,0.98)',
  }
}

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return list.value.filter((b) => {
    if (expiryFilter.value === 'active' && isExpired(b.expiryDate)) return false
    if (expiryFilter.value === 'expired' && !isExpired(b.expiryDate)) return false
    if (!kw) return true
    const hit =
      String(b.userId ?? '').toLowerCase().includes(kw) ||
      String(b.username ?? '').toLowerCase().includes(kw) ||
      String(b.realName ?? '').toLowerCase().includes(kw) ||
      String(b.reason ?? '').toLowerCase().includes(kw)
    return hit
  })
})

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.listBlacklist()
    if (res.code !== 200) throw new Error(res.msg || '加载失败')
    list.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

// 添加
const modalVisible = ref(false)
const modalLoading = ref(false)
const modalError = ref(null)
const form = reactive({ userId: '', reason: '', expiryDate: '' })

function openAdd() {
  form.userId = ''
  form.reason = ''
  form.expiryDate = ''
  modalError.value = null
  modalVisible.value = true
}

function closeModal() {
  modalVisible.value = false
  modalError.value = null
}

async function submit() {
  modalError.value = null
  const userIdNum = Number(form.userId)
  if (!form.userId || Number.isNaN(userIdNum) || userIdNum <= 0) {
    modalError.value = '请输入正确的用户ID'
    return
  }
  modalLoading.value = true
  try {
    const payload = {
      userId: userIdNum,
      reason: form.reason || null,
      // datetime-local 格式：YYYY-MM-DDTHH:mm（后端 LocalDateTime 一般可解析）
      expiryDate: form.expiryDate ? form.expiryDate : null,
    }
    const res = await adminApi.addBlacklist(payload)
    if (res.code !== 200) throw new Error(res.msg || '添加失败')
    closeModal()
    await reload()
  } catch (e) {
    modalError.value = e?.message || '网络错误'
  } finally {
    modalLoading.value = false
  }
}

// 移除
async function remove(b) {
  if (!b?.userId) return
  busyUserId.value = b.userId
  error.value = null
  try {
    const res = await adminApi.removeBlacklist(b.userId)
    if (res.code !== 200) throw new Error(res.msg || '移除失败')
    await reload()
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    busyUserId.value = null
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
  width: min(560px, 100%);
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.20);
  padding: 16px;
}
</style>

