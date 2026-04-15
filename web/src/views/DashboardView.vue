<template>
  <div class="pageCard">
    <div style="display:flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap;">
      <div>
        <div style="font-weight: 900; font-size: 16px;">数据统计</div>
        <div style="margin-top: 6px; color: rgba(15,23,42,0.62); font-size: 13px;">
          实时统计来自后端 `GET /admin/dashboard/stats`
        </div>
      </div>
      <div style="display:flex; gap:10px; align-items:center;">
        <button class="linkbtn" @click="reload" :disabled="loading">{{ loading ? '加载中...' : '刷新' }}</button>
        <a class="linkbtn" :href="exportStatsUrl" target="_blank" rel="noreferrer">导出统计Excel</a>
        <a class="linkbtn" :href="exportInsideUrl" target="_blank" rel="noreferrer">导出在校人员Excel</a>
      </div>
    </div>

    <div v-if="error" class="error">{{ error }}</div>

    <div class="grid4" style="margin-top: 14px;">
      <div class="stat">
        <div class="statLabel">总预约</div>
        <div class="statValue">{{ stats?.totalReservations ?? '-' }}</div>
      </div>
      <div class="stat">
        <div class="statLabel">待审核</div>
        <div class="statValue">{{ stats?.pendingCount ?? '-' }}</div>
      </div>
      <div class="stat">
        <div class="statLabel">成功（通过+已用）</div>
        <div class="statValue">{{ stats?.successCount ?? '-' }}</div>
      </div>
      <div class="stat">
        <div class="statLabel">用户数</div>
        <div class="statValue">{{ stats?.userCount ?? '-' }}</div>
      </div>
      <div class="stat">
        <div class="statLabel">设备数</div>
        <div class="statValue">{{ stats?.deviceCount ?? '-' }}</div>
      </div>
      <div class="stat">
        <div class="statLabel">待审核报备</div>
        <div class="statValue">{{ stats?.pendingReportCount ?? '-' }}</div>
      </div>
      <div class="stat" style="grid-column: span 2;">
        <div class="statLabel">预约状态分布（0-5）</div>
        <div style="margin-top: 8px; display:flex; gap:8px; flex-wrap:wrap;">
          <span v-for="s in [0,1,2,3,4,5]" :key="s" class="pill">
            {{ statusText(s) }}：{{ stats?.statusDist?.[String(s)] ?? 0 }}
          </span>
        </div>
      </div>
    </div>

    <div style="margin-top: 14px; display:grid; grid-template-columns: 1fr 1fr; gap: 12px;">
      <div class="stat">
        <div class="statLabel">近7天预约量</div>
        <div style="margin-top: 10px; display:grid; gap: 8px;">
          <div v-for="row in stats?.trend7 || []" :key="row.date" style="display:grid; grid-template-columns: 90px 1fr 40px; gap: 10px; align-items:center;">
            <div style="font-size:12px; color:rgba(15,23,42,0.62);">{{ row.date }}</div>
            <div style="height: 10px; border-radius: 999px; background: rgba(79,140,255,0.14); overflow:hidden;">
              <div
                :style="{
                  height: '10px',
                  width: barWidth(row.count),
                  background: 'rgba(79,140,255,0.85)',
                  borderRadius: '999px',
                }"
              />
            </div>
            <div style="text-align:right; font-weight:800;">{{ row.count }}</div>
          </div>
          <div v-if="(stats?.trend7 || []).length === 0" style="color:rgba(15,23,42,0.62); font-size:13px;">暂无数据</div>
        </div>
      </div>

      <div class="stat">
        <div class="statLabel">门禁预约 Top5</div>
        <div style="margin-top: 10px; overflow:auto;">
          <table class="table">
            <thead>
              <tr><th>门禁</th><th style="text-align:right;">次数</th></tr>
            </thead>
            <tbody>
              <tr v-for="it in stats?.topDevices || []" :key="it.name">
                <td>{{ it.name }}</td>
                <td style="text-align:right; font-weight:800;">{{ it.count }}</td>
              </tr>
              <tr v-if="(stats?.topDevices || []).length === 0">
                <td colspan="2" style="color:rgba(15,23,42,0.62);">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="stat" style="margin-top: 12px;">
      <div class="statLabel">当前在校人员（按“已使用且在预约时间段内”口径）</div>
      <div style="margin-top: 10px; overflow:auto;">
        <table class="table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>角色</th>
              <th>日期</th>
              <th>时间段</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(r, idx) in inside || []" :key="idx">
              <td>{{ r.realName || '-' }}</td>
              <td>{{ roleText(r.role) }}</td>
              <td>{{ r.reservationDate || '-' }}</td>
              <td>{{ (r.startTime || '-') + ' - ' + (r.endTime || '-') }}</td>
            </tr>
            <tr v-if="(inside || []).length === 0">
              <td colspan="4" style="color:rgba(15,23,42,0.62);">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '../services/adminApi.js'

const loading = ref(false)
const error = ref(null)

const stats = ref(null)
const inside = ref([])

const exportStatsUrl = computed(() => `/api/admin/dashboard/stats/export`)
const exportInsideUrl = computed(() => `/api/admin/dashboard/inside-people/export`)

function roleText(role) {
  if (role === 'student') return '学生'
  if (role === 'admin') return '管理员'
  if (role === 'super_admin') return '超级管理员'
  return role || '-'
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

function barWidth(count) {
  const max = Math.max(...(stats.value?.trend7 || []).map((x) => x.count), 1)
  const pct = Math.round((Math.max(count, 0) / max) * 100)
  return `${Math.max(6, pct)}%`
}

async function reload() {
  loading.value = true
  error.value = null
  try {
    const [s, i] = await Promise.all([adminApi.dashboardStats(), adminApi.insidePeople()])
    if (s.code !== 200) throw new Error(s.msg || '统计加载失败')
    if (i.code !== 200) throw new Error(i.msg || '在校人员加载失败')
    stats.value = s.data || null
    inside.value = Array.isArray(i.data) ? i.data : []
  } catch (e) {
    error.value = e?.message || '网络错误'
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

