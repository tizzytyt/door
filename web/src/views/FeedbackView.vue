<template>
  <div class="pageCard">
    <div style="display:flex; justify-content:space-between; gap: 12px; flex-wrap: wrap; align-items: center;">
      <div style="font-weight: 900; font-size: 16px;">报修反馈</div>
      <button class="linkbtn" @click="reload" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div style="margin-top: 12px; overflow:auto;">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户</th>
            <th>设备</th>
            <th>内容</th>
            <th>状态</th>
            <th>管理员回复</th>
            <th style="width: 220px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="f in list" :key="f.id">
            <td>{{ f.id }}</td>
            <td>{{ f.realName || f.userId || '-' }}</td>
            <td>{{ f.deviceName || f.deviceId || '-' }}</td>
            <td style="max-width: 260px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              {{ f.content || '-' }}
            </td>
            <td>{{ statusText(f.status) }}</td>
            <td style="max-width: 220px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              {{ f.adminReply || '-' }}
            </td>
            <td>
              <button class="linkbtn" @click="openHandle(f)">处理</button>
            </td>
          </tr>
          <tr v-if="!loading && list.length===0">
            <td colspan="7" style="color:rgba(15,23,42,0.62);">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="modalVisible" class="modalMask" @click.self="closeModal">
      <div class="modal">
        <div style="font-weight: 900; margin-bottom: 6px;">处理反馈</div>
        <div style="color: rgba(15,23,42,0.62); font-size: 13px;">
          #{{ current?.id }} / {{ current?.realName || current?.userId || '-' }} / {{ current?.deviceName || current?.deviceId || '-' }}
        </div>
        <div class="field">
          <label>处理状态（数字）</label>
          <input class="input" v-model.number="form.status" type="number" placeholder="例如：1" />
        </div>
        <div class="field">
          <label>管理员回复</label>
          <textarea class="input" style="min-height: 120px; resize: vertical;" v-model.trim="form.adminReply" placeholder="填写回复内容" />
        </div>
        <div v-if="modalError" class="error">{{ modalError }}</div>
        <div style="display:flex; gap:10px; margin-top: 12px;">
          <button class="btn" style="width:auto; padding: 10px 14px;" :disabled="modalLoading" @click="submit">
            {{ modalLoading ? '提交中...' : '保存' }}
          </button>
          <button class="linkbtn" style="padding: 10px 14px;" @click="closeModal">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '../services/adminApi.js'

const loading = ref(false)
const error = ref(null)
const list = ref([])

const modalVisible = ref(false)
const modalLoading = ref(false)
const modalError = ref(null)
const current = ref(null)
const form = reactive({ status: 1, adminReply: '' })

function statusText(status) {
  if (status === 0) return '待处理'
  if (status === 1) return '处理中/已处理'
  if (status === 2) return '已关闭'
  return status == null ? '-' : String(status)
}

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.listFeedback()
    if (res.code !== 200) throw new Error(res.msg || '加载失败')
    list.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

function openHandle(f) {
  current.value = f
  form.status = f.status ?? 1
  form.adminReply = f.adminReply || ''
  modalError.value = null
  modalVisible.value = true
}

function closeModal() {
  modalVisible.value = false
  current.value = null
  modalError.value = null
}

async function submit() {
  modalError.value = null
  if (!current.value?.id) return
  modalLoading.value = true
  try {
    const res = await adminApi.updateFeedback({
      id: current.value.id,
      status: Number(form.status),
      adminReply: form.adminReply || '',
    })
    if (res.code !== 200) throw new Error(res.msg || '保存失败')
    closeModal()
    await reload()
  } catch (e) {
    modalError.value = e?.message || '网络错误'
  } finally {
    modalLoading.value = false
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
  width: min(720px, 100%);
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.20);
  padding: 16px;
}
</style>

