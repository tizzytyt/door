<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="brandTitle">门禁预约管理</div>
        <div class="brandSub">超级管理员</div>
      </div>

      <nav class="nav">
        <RouterLink class="navItem" to="/dashboard" active-class="active">数据统计</RouterLink>
        <RouterLink class="navItem" to="/users" active-class="active">用户管理</RouterLink>
        <RouterLink class="navItem" to="/blacklist" active-class="active">黑名单管理</RouterLink>
        <RouterLink class="navItem" to="/devices" active-class="active">设备管理</RouterLink>
        <RouterLink class="navItem" to="/reservations" active-class="active">预约审核/查询</RouterLink>
        <RouterLink class="navItem" to="/announcements" active-class="active">公告管理</RouterLink>
        <RouterLink class="navItem" to="/feedback" active-class="active">报修反馈</RouterLink>
        <RouterLink class="navItem" to="/rules" active-class="active">规则配置</RouterLink>
        <RouterLink class="navItem" to="/notifications" active-class="active">通知中心</RouterLink>
      </nav>
    </aside>

    <main class="main">
      <div class="topbar" style="width: 100%;">
        <div style="display:flex; gap:10px; align-items:center;">
          <div class="pill">已登录：{{ userLabel }}</div>
        </div>
        <button class="linkbtn" @click="logout">退出登录</button>
      </div>

      <div class="content">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { auth } from '../services/auth.js'

const router = useRouter()
const user = auth.getUser()

const userLabel = computed(() => {
  const name = user?.realName || user?.username || '未知用户'
  return `${name} (${user?.role || '-'})`
})

function logout() {
  auth.clear()
  router.replace('/login')
}
</script>

<style scoped>
.layout{
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px 1fr;
  background: #fff;
}
.sidebar{
  border-right: 1px solid rgba(15, 23, 42, 0.10);
  padding: 14px;
}
.brand{
  padding: 10px 10px 14px 10px;
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  margin-bottom: 12px;
}
.brandTitle{
  font-weight: 900;
  letter-spacing: 0.2px;
}
.brandSub{
  margin-top: 6px;
  font-size: 12px;
  color: rgba(15,23,42,0.62);
}
.nav{
  display: grid;
  gap: 8px;
}
.navItem{
  text-decoration: none;
  padding: 10px 10px;
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(15, 23, 42, 0.02);
  color: rgba(15, 23, 42, 0.86);
}
.navItem.active{
  border-color: rgba(79,140,255,0.35);
  background: rgba(79,140,255,0.10);
  color: rgba(30,64,175,0.95);
}
.main{
  padding: 14px;
}
.content{
  margin-top: 12px;
}
@media (max-width: 900px){
  .layout{ grid-template-columns: 1fr; }
  .sidebar{ position: sticky; top: 0; background:#fff; z-index:1; }
}
</style>

