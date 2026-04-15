<template>
  <div class="pageCard">
    <div style="display:flex; justify-content:space-between; gap: 12px; flex-wrap: wrap; align-items: center;">
      <div style="font-weight: 900; font-size: 16px;">公告管理</div>
      <div style="display:flex; gap:10px;">
        <button class="linkbtn" @click="openCreate">发布公告</button>
        <button class="linkbtn" @click="reload" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div style="margin-top: 12px; overflow:auto;">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>发布时间</th>
            <th style="width: 260px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in list" :key="a.id">
            <td>{{ a.id }}</td>
            <td>{{ a.title || '-' }}</td>
            <td>{{ a.createdAt || '-' }}</td>
            <td>
              <div style="display:flex; gap:8px; flex-wrap:wrap;">
                <button class="linkbtn" @click="openEdit(a)">编辑</button>
                <button class="linkbtn" @click="remove(a)" :disabled="busyId===a.id">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && list.length===0">
            <td colspan="4" style="color:rgba(15,23,42,0.62);">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="modalVisible" class="modalMask" @click.self="closeModal">
      <div class="modal">
        <div style="font-weight: 900; margin-bottom: 8px;">{{ editing?.id ? '编辑公告' : '发布公告' }}</div>
        <div class="field">
          <label>标题</label>
          <input class="input" v-model.trim="form.title" placeholder="请输入公告标题" />
        </div>
        <div class="field">
          <label>内容</label>
          <textarea class="input" style="min-height: 160px; resize: vertical;" v-model.trim="form.content" placeholder="请输入公告内容" />
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
const form = reactive({ title: '', content: '' })

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.listAnnouncements()
    if (res.code !== 200) throw new Error(res.msg || '加载失败')
    list.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  form.id = undefined
  form.title = ''
  form.content = ''
  modalError.value = null
  modalVisible.value = true
}

function openEdit(a) {
  editing.value = a
  form.id = a.id
  form.title = a.title
  form.content = a.content
  modalError.value = null
  modalVisible.value = true
}

function closeModal() {
  modalVisible.value = false
  modalError.value = null
}

async function submit() {
  modalError.value = null
  if (!form.title || !form.content) {
    modalError.value = '请填写标题和内容'
    return
  }
  modalLoading.value = true
  try {
    const res = form.id ? await adminApi.updateAnnouncement(form) : await adminApi.createAnnouncement(form)
    if (res.code !== 200) throw new Error(res.msg || '保存失败')
    closeModal()
    await reload()
  } catch (e) {
    modalError.value = e?.message || '网络错误'
  } finally {
    modalLoading.value = false
  }
}

async function remove(a) {
  if (!a.id) return
  busyId.value = a.id
  try {
    const res = await adminApi.deleteAnnouncement(a.id)
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
  width: min(720px, 100%);
  background: #fff;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.20);
  padding: 16px;
}
</style>

