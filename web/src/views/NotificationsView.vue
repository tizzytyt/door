<template>
  <div class="pageCard">
    <div style="display:flex; justify-content:space-between; gap: 12px; flex-wrap: wrap; align-items: center;">
      <div>
        <div style="font-weight: 900; font-size: 16px;">通知中心</div>
        <div style="margin-top: 6px; color: rgba(15,23,42,0.62); font-size: 13px;">
          报修提醒（未读）：{{ unreadCount ?? '-' }}
        </div>
      </div>
      <button class="linkbtn" @click="reload" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div style="margin-top: 12px; overflow:auto;">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>内容</th>
            <th>时间</th>
            <th style="width: 140px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="n in list" :key="n.id">
            <td>{{ n.id }}</td>
            <td>{{ n.title || '-' }}</td>
            <td style="max-width: 420px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              {{ n.content || '-' }}
            </td>
            <td>{{ n.createdAtText || '-' }}</td>
            <td>
              <button class="linkbtn" @click="markRead(n)" :disabled="busyId===n.id">标记已读</button>
            </td>
          </tr>
          <tr v-if="!loading && list.length===0">
            <td colspan="5" style="color:rgba(15,23,42,0.62);">暂无未读通知</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { adminApi } from '../services/adminApi.js'

const loading = ref(false)
const error = ref(null)
const busyId = ref(null)
const list = ref([])
const unreadCount = ref(null)

async function reload() {
  loading.value = true
  error.value = null
  try {
    const [countRes, listRes] = await Promise.all([adminApi.repairUnreadCount(), adminApi.repairUnreadList()])
    if (countRes.code !== 200) throw new Error(countRes.msg || '加载失败')
    if (listRes.code !== 200) throw new Error(listRes.msg || '加载失败')
    unreadCount.value = countRes.data ?? 0
    list.value = Array.isArray(listRes.data) ? listRes.data : []
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

async function markRead(n) {
  if (!n.id) return
  busyId.value = n.id
  error.value = null
  try {
    const res = await adminApi.markNotificationRead(n.id)
    if (res.code !== 200) throw new Error(res.msg || '操作失败')
    await reload()
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    busyId.value = null
  }
}

onMounted(reload)
</script>

