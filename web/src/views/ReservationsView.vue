<template>
  <div class="pageCard">
    <div style="display:flex; justify-content:space-between; gap: 12px; flex-wrap: wrap; align-items: center;">
      <div style="font-weight: 900; font-size: 16px;">预约审核 / 查询</div>
      <div style="display:flex; gap:10px; align-items:center;">
        <select class="input" style="width: 180px; padding: 8px 10px;" v-model="mode">
          <option value="pending">待审核</option>
          <option value="all">全部预约</option>
        </select>
        <button class="linkbtn" @click="reload" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
        <a class="linkbtn" :href="exportUrl" target="_blank" rel="noreferrer">导出Excel</a>
      </div>
    </div>

    <div style="display:flex; gap:10px; flex-wrap:wrap; margin-top: 10px;">
      <input class="input" style="width: 260px; padding: 8px 10px;" v-model.trim="filters.keyword" placeholder="关键词（姓名/门禁/事由）" />
      <select class="input" style="width: 160px; padding: 8px 10px;" v-model="filters.status">
        <option value="">全部状态</option>
        <option v-for="s in [0,1,2,3,4,5]" :key="s" :value="String(s)">{{ statusText(s) }}</option>
      </select>
      <input class="input" style="width: 160px; padding: 8px 10px;" v-model.trim="filters.date" placeholder="日期(YYYY-MM-DD)" />
      <input class="input" style="width: 160px; padding: 8px 10px;" v-model.trim="filters.userId" placeholder="用户ID" />
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div style="margin-top: 12px; overflow:auto;">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>门禁</th>
            <th>日期</th>
            <th>时间段</th>
            <th>状态</th>
            <th>事由</th>
            <th style="width: 260px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in filtered" :key="r.id">
            <td>{{ r.id }}</td>
            <td>{{ r.realName || '-' }}</td>
            <td>{{ r.deviceName || '-' }}</td>
            <td>{{ r.reservationDate || '-' }}</td>
            <td>{{ timeText(r) }}</td>
            <td>{{ statusText(r.status ?? -1) }}</td>
            <td>{{ r.reason || '-' }}</td>
            <td>
              <div style="display:flex; gap:8px; flex-wrap:wrap;">
                <button class="linkbtn" @click="openDetail(r)">详情</button>
                <button v-if="mode==='pending'" class="linkbtn" @click="openAudit(r, 1)" :disabled="busyId === r.id">通过</button>
                <button v-if="mode==='pending'" class="linkbtn" @click="openAudit(r, 2)" :disabled="busyId === r.id">拒绝</button>
              </div>
            </td>
          </tr>
          <tr v-if="!loading && filtered.length === 0">
            <td colspan="8" style="color:rgba(15,23,42,0.62);">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 详情/审核 -->
    <div v-if="detailVisible" class="modalMask" @click.self="closeDetail">
      <div class="modal">
        <div style="display:flex; justify-content:space-between; align-items:center; gap:10px;">
          <div style="font-weight: 900;">预约详情</div>
          <button class="linkbtn" @click="closeDetail">关闭</button>
        </div>

        <div style="margin-top:10px; display:grid; gap:8px; font-size:13px;">
          <div><b>ID：</b>{{ current?.id }}</div>
          <div><b>用户ID：</b>{{ current?.userId ?? '-' }}</div>
          <div><b>姓名：</b>{{ current?.realName || '-' }}</div>
          <div><b>门禁：</b>{{ current?.deviceName || '-' }}</div>
          <div><b>日期：</b>{{ current?.reservationDate || '-' }}</div>
          <div><b>时间段：</b>{{ timeText(current) }}</div>
          <div><b>状态：</b>{{ statusText(current?.status ?? -1) }}</div>
          <div><b>事由：</b>{{ current?.reason || '-' }}</div>
          <div><b>审核意见：</b>{{ current?.auditOpinion || '-' }}</div>
        </div>

        <div v-if="auditVisible" style="margin-top:12px;">
          <div class="field" style="margin:0;">
            <label>审核意见（可选）</label>
            <input class="input" v-model.trim="auditOpinion" placeholder="填写审核意见" />
          </div>
          <div v-if="auditError" class="error">{{ auditError }}</div>
          <div style="display:flex; gap:10px; margin-top: 12px;">
            <button class="btn" style="width:auto; padding: 10px 14px;" :disabled="auditLoading" @click="submitAudit">
              {{ auditLoading ? '提交中...' : (auditStatus===1 ? '确认通过' : '确认拒绝') }}
            </button>
            <button class="linkbtn" style="padding: 10px 14px;" @click="auditVisible=false">取消</button>
          </div>
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
const busyId = ref(null)
const mode = ref('pending')
const list = ref([])

const filters = reactive({ keyword: '', status: '', date: '', userId: '' })

const exportUrl = computed(() => {
  const qs = new URLSearchParams()
  if (filters.keyword) qs.set('keyword', filters.keyword)
  if (filters.status) qs.set('status', filters.status)
  if (filters.date) qs.set('date', filters.date)
  if (filters.userId) qs.set('userId', filters.userId)
  const q = qs.toString()
  return `/api/admin/reservation/export${q ? `?${q}` : ''}`
})

const filtered = computed(() => {
  const kw = filters.keyword.trim().toLowerCase()
  return list.value.filter((r) => {
    if (filters.status && String(r.status ?? '') !== filters.status) return false
    if (filters.date && String(r.reservationDate ?? '') !== filters.date) return false
    if (filters.userId && String(r.userId ?? '') !== filters.userId) return false
    if (!kw) return true
    const hit =
      String(r.realName ?? '').toLowerCase().includes(kw) ||
      String(r.deviceName ?? '').toLowerCase().includes(kw) ||
      String(r.reason ?? '').toLowerCase().includes(kw)
    return hit
  })
})

function timeText(r) {
  if (!r) return '-'
  const s = (r.startTime || '').toString().slice(0, 5) || '-'
  const e = (r.endTime || '').toString().slice(0, 5) || '-'
  return `${s} - ${e}`
}

function statusText(status) {
  switch (status) {
    case 0:
      return '待审核'
    case 1:
      return '已通过'
    case 2:
      return '已拒绝'
    case 3:
      return '已使用'
    case 4:
      return '已取消'
    case 5:
      return '已失效'
    default:
      return '未知'
  }
}

async function reload() {
  loading.value = true
  error.value = null
  try {
    const res = mode.value === 'pending' ? await adminApi.pendingReservations() : await adminApi.allReservations()
    if (res.code !== 200) throw new Error(res.msg || '加载失败')
    list.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

// 详情/审核弹窗
const detailVisible = ref(false)
const current = ref(null)
function openDetail(r) {
  current.value = r
  auditVisible.value = false
  detailVisible.value = true
}
function closeDetail() {
  detailVisible.value = false
  current.value = null
}

const auditVisible = ref(false)
const auditStatus = ref(1)
const auditOpinion = ref('')
const auditLoading = ref(false)
const auditError = ref(null)

function openAudit(r, status) {
  openDetail(r)
  auditStatus.value = status
  auditOpinion.value = ''
  auditError.value = null
  auditVisible.value = true
}

async function submitAudit() {
  if (!current.value?.id) return
  auditLoading.value = true
  auditError.value = null
  busyId.value = current.value.id
  try {
    const res = await adminApi.auditReservation({
      id: current.value.id,
      status: auditStatus.value,
      auditOpinion: auditOpinion.value || undefined,
    })
    if (res.code !== 200) throw new Error(res.msg || '提交失败')
    closeDetail()
    await reload()
  } catch (e) {
    auditError.value = e?.message || '网络错误'
  } finally {
    auditLoading.value = false
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

