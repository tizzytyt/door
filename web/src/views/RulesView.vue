<template>
  <div class="pageCard">
    <div style="display:flex; justify-content:space-between; gap: 12px; flex-wrap: wrap; align-items: center;">
      <div style="font-weight: 900; font-size: 16px;">规则配置</div>
      <button class="linkbtn" @click="reload" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div style="margin-top: 12px; overflow:auto;">
      <table class="table">
        <thead>
          <tr>
            <th>描述</th>
            <th>Value</th>
            <th style="width: 140px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in list" :key="r.configKey">
            <td style="color:rgba(15,23,42,0.62);">{{ r.description || '-' }}</td>
            <td>
              <input class="input" style="padding: 8px 10px;" v-model="editMap[r.configKey || '']" />
            </td>
            <td>
              <button class="linkbtn" @click="save(r)" :disabled="busyKey===r.configKey">保存</button>
            </td>
          </tr>
          <tr v-if="!loading && list.length===0">
            <td colspan="3" style="color:rgba(15,23,42,0.62);">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '../services/adminApi.js'

const loading = ref(false)
const error = ref(null)
const list = ref([])
const busyKey = ref(null)
const editMap = reactive({})

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = await adminApi.listRules()
    if (res.code !== 200) throw new Error(res.msg || '加载失败')
    list.value = Array.isArray(res.data) ? res.data : []
    for (const r of list.value) {
      const k = r.configKey || ''
      if (k) editMap[k] = String(r.configValue ?? '')
    }
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

async function save(r) {
  const k = r.configKey || ''
  if (!k) return
  busyKey.value = k
  error.value = null
  try {
    const res = await adminApi.updateRule({ configKey: k, configValue: editMap[k] })
    if (res.code !== 200) throw new Error(res.msg || '保存失败')
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    busyKey.value = null
  }
}

onMounted(reload)
</script>

