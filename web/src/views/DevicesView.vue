<template>
  <div class="pageCard">
    <div style="display:flex; justify-content:space-between; gap: 12px; flex-wrap: wrap; align-items: center;">
      <div style="font-weight: 900; font-size: 16px;">设备管理</div>
      <div style="display:flex; gap:10px;">
        <button class="linkbtn" @click="openAdd">添加设备</button>
        <button class="linkbtn" @click="reload" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div style="margin-top: 12px; overflow:auto;">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>位置</th>
            <th>状态</th>
            <th>备注</th>
            <th style="width: 220px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="d in list" :key="d.id">
            <td>{{ d.id }}</td>
            <td>{{ d.name || '-' }}</td>
            <td>{{ d.location || '-' }}</td>
            <td>{{ statusText(d.status) }}</td>
            <td>{{ d.remark || '-' }}</td>
            <td>
              <div style="display:flex; gap:8px; flex-wrap:wrap;">
                <button class="linkbtn" @click="openEdit(d)">编辑</button>
                <button class="linkbtn" @click="remove(d)" :disabled="busyId === d.id">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && list.length === 0">
            <td colspan="6" style="color:rgba(15,23,42,0.62);">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="modalVisible" class="modalMask" @click.self="closeModal">
      <div class="modal">
        <div style="font-weight: 900; margin-bottom: 8px;">{{ editing?.id ? '编辑设备' : '添加设备' }}</div>
        <div class="field">
          <label>名称</label>
          <input class="input" v-model.trim="form.name" placeholder="例如：1号门禁" />
        </div>
        <div class="field">
          <label>位置</label>
          <input class="input" v-model.trim="form.location" placeholder="例如：1号宿舍楼" />
        </div>
        <div class="field">
          <label>状态（数字）</label>
          <input class="input" v-model.number="form.status" type="number" placeholder="例如：1" />
        </div>
        <div class="field">
          <label>备注</label>
          <input class="input" v-model.trim="form.remark" placeholder="可选" />
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
const busyId = ref(null)
const list = ref([])

const modalVisible = ref(false)
const modalLoading = ref(false)
const modalError = ref(null)
const editing = ref(null)
const form = reactive({ name: '', location: '', status: 1, remark: '' })

function statusText(status) {
  if (status === 0) return '故障/禁用'
  if (status === 1) return '正常'
  if (status === 2) return '维护中'
  return status == null ? '-' : String(status)
}

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.listDevices()
    if (res.code !== 200) throw new Error(res.msg || '加载失败')
    list.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editing.value = null
  form.id = undefined
  form.name = ''
  form.location = ''
  form.status = 1
  form.remark = ''
  modalError.value = null
  modalVisible.value = true
}

function openEdit(d) {
  editing.value = d
  form.id = d.id
  form.name = d.name
  form.location = d.location
  form.status = d.status ?? 1
  form.remark = d.remark
  modalError.value = null
  modalVisible.value = true
}

function closeModal() {
  modalVisible.value = false
  modalError.value = null
}

async function submit() {
  modalError.value = null
  if (!form.name || !form.location) {
    modalError.value = '请填写名称和位置'
    return
  }
  modalLoading.value = true
  try {
    const res = form.id ? await adminApi.updateDevice(form) : await adminApi.addDevice(form)
    if (res.code !== 200) throw new Error(res.msg || '保存失败')
    closeModal()
    await reload()
  } catch (e) {
    modalError.value = e?.message || '网络错误'
  } finally {
    modalLoading.value = false
  }
}

async function remove(d) {
  if (!d.id) return
  busyId.value = d.id
  try {
    const res = await adminApi.deleteDevice(d.id)
    if (res.code !== 200) throw new Error(res.msg || '删除失败')
    await reload()
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    busyId.value = null
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

