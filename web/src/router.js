import { createRouter, createWebHistory } from 'vue-router'
import LoginView from './views/LoginView.vue'
import UsersView from './views/UsersView.vue'
import AdminLayout from './views/AdminLayout.vue'
import DashboardView from './views/DashboardView.vue'
import DevicesView from './views/DevicesView.vue'
import ReservationsView from './views/ReservationsView.vue'
import AnnouncementsView from './views/AnnouncementsView.vue'
import FeedbackView from './views/FeedbackView.vue'
import RulesView from './views/RulesView.vue'
import NotificationsView from './views/NotificationsView.vue'
import BlacklistView from './views/BlacklistView.vue'
import { auth } from './services/auth.js'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: AdminLayout,
      redirect: '/dashboard',
      meta: { requiresAuth: true, requiresSuperAdmin: true },
      children: [
        { path: 'dashboard', component: DashboardView },
        { path: 'users', component: UsersView },
        { path: 'blacklist', component: BlacklistView },
        { path: 'devices', component: DevicesView },
        { path: 'reservations', component: ReservationsView },
        { path: 'announcements', component: AnnouncementsView },
        { path: 'feedback', component: FeedbackView },
        { path: 'rules', component: RulesView },
        { path: 'notifications', component: NotificationsView },
      ],
    },
  ],
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !auth.hasToken()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresSuperAdmin) {
    const u = auth.getUser()
    if (!u || u.role !== 'super_admin') {
      auth.clear()
      return { path: '/login' }
    }
  }
  if (to.path === '/login' && auth.hasToken()) {
    return { path: '/dashboard' }
  }
  return true
})

