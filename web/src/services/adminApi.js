import { http } from './http.js'

export const adminApi = {
  // 用户
  async listUsers() {
    const res = await http.get('/admin/user/list')
    return res.data
  },
  async updateUserStatus(userId, status) {
    const res = await http.post('/admin/user/status', { userId, status })
    return res.data
  },
  async resetUserPassword(userId, newPassword) {
    const res = await http.post('/admin/user/reset-password', { userId, newPassword })
    return res.data
  },
  async createAdmin(payload) {
    const res = await http.post('/admin/user/create-admin', payload)
    return res.data
  },

  // 黑名单
  async listBlacklist() {
    const res = await http.get('/admin/blacklist/list')
    return res.data
  },
  async addBlacklist(payload) {
    const res = await http.post('/admin/blacklist/add', payload)
    return res.data
  },
  async removeBlacklist(userId) {
    const res = await http.delete(`/admin/blacklist/remove/${userId}`)
    return res.data
  },

  // 统计
  async dashboardStats() {
    const res = await http.get('/admin/dashboard/stats')
    return res.data
  },
  async insidePeople() {
    const res = await http.get('/admin/dashboard/inside-people')
    return res.data
  },

  // 设备
  async listDevices() {
    const res = await http.get('/admin/device/list')
    return res.data
  },
  async addDevice(payload) {
    const res = await http.post('/admin/device/add', payload)
    return res.data
  },
  async updateDevice(payload) {
    const res = await http.post('/admin/device/update', payload)
    return res.data
  },
  async deleteDevice(id) {
    const res = await http.delete(`/admin/device/delete/${id}`)
    return res.data
  },

  // 预约
  async pendingReservations() {
    const res = await http.get('/admin/reservation/pending')
    return res.data
  },
  async allReservations() {
    const res = await http.get('/admin/reservation/list')
    return res.data
  },
  async auditReservation(payload) {
    const res = await http.post('/admin/reservation/audit', payload)
    return res.data
  },

  // 公告
  async listAnnouncements() {
    const res = await http.get('/admin/announcement/list')
    return res.data
  },
  async createAnnouncement(payload) {
    const res = await http.post('/admin/announcement/create', payload)
    return res.data
  },
  async updateAnnouncement(payload) {
    const res = await http.post('/admin/announcement/update', payload)
    return res.data
  },
  async deleteAnnouncement(id) {
    const res = await http.delete(`/admin/announcement/delete/${id}`)
    return res.data
  },

  // 反馈
  async listFeedback() {
    const res = await http.get('/admin/feedback/list')
    return res.data
  },
  async updateFeedback(payload) {
    const res = await http.post('/admin/feedback/update', payload)
    return res.data
  },

  // 规则
  async listRules() {
    const res = await http.get('/admin/rules/list')
    return res.data
  },
  async updateRule(payload) {
    const res = await http.post('/admin/rules/update', payload)
    return res.data
  },

  // 通知
  async repairUnreadList() {
    const res = await http.get('/admin/notification/repair-unread-list')
    return res.data
  },
  async repairUnreadCount() {
    const res = await http.get('/admin/notification/repair-unread-count')
    return res.data
  },
  async markNotificationRead(id) {
    const res = await http.post(`/admin/notification/read/${id}`)
    return res.data
  },
}

