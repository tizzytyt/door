const { request } = require('../../../utils/request.js');

function roleText(role) {
  const map = {
    student: '学生',
    admin: '管理员',
    super_admin: '超级管理员'
  };
  return map[role] || '未知';
}

function formatDateTime(dtStr) {
  if (!dtStr) return '';
  const s = dtStr.replace('T', ' ');
  return s.slice(0, 16);
}

function todayStr() {
  const d = new Date();
  const y = d.getFullYear();
  const m = `${d.getMonth() + 1}`.padStart(2, '0');
  const day = `${d.getDate()}`.padStart(2, '0');
  return `${y}-${m}-${day}`;
}

Page({
  data: {
    loading: false,
    currentRole: '',
    keyword: '',
    roleTab: 'all',
    statusTab: 'all', // all / normal / ban

    list: [],
    filteredList: [],

    // 拉黑弹窗
    banVisible: false,
    banSaving: false,
    banUser: {},
    banReason: '',
    banPermanent: true,
    banExpiryDate: todayStr(),

    createAdminVisible: false,
    createAdminSaving: false,
    caUsername: '',
    caRealName: '',
    caPhone: '',
    caPassword: '',
    caConfirmPassword: ''
  },

  onShow() {
    const me = wx.getStorageSync('user');
    this.setData({ currentRole: me && me.role ? me.role : '' });
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  loadData() {
    this.setData({ loading: true });
    return Promise.all([
      request({ url: '/admin/user/list', method: 'GET' }),
      request({ url: '/admin/blacklist/list', method: 'GET' })
    ]).then(([users, bl]) => {
      const blackMap = {};
      (bl || []).forEach((b) => {
        blackMap[b.userId] = {
          reason: b.reason,
          expiry: b.expiryDate ? formatDateTime(b.expiryDate) : ''
        };
      });

      const list = (users || []).map((u) => ({
        ...u,
        roleText: roleText(u.role),
        blackReason: blackMap[u.id] ? blackMap[u.id].reason : '',
        blackExpiry: blackMap[u.id] ? blackMap[u.id].expiry : ''
      }));

      this.setData({ list });
      this.applyFilter(list);
    }).catch((err) => {
      console.error('加载用户失败', err);
      this.setData({ list: [], filteredList: [] });
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  handleKeywordInput(e) {
    const keyword = (e.detail.value || '').trim();
    this.setData({ keyword });
    this.applyFilter();
  },

  clearKeyword() {
    this.setData({ keyword: '' });
    this.applyFilter();
  },

  switchRole(e) {
    const roleTab = e.currentTarget.dataset.role;
    this.setData({ roleTab });
    this.applyFilter();
  },

  switchStatus(e) {
    const statusTab = e.currentTarget.dataset.status;
    this.setData({ statusTab });
    this.applyFilter();
  },

  applyFilter(listData) {
    const list = listData || this.data.list;
    const kw = (this.data.keyword || '').toLowerCase();
    const { roleTab, statusTab } = this.data;

    let filtered = list;
    if (roleTab !== 'all') filtered = filtered.filter((x) => x.role === roleTab);
    if (statusTab === 'normal') filtered = filtered.filter((x) => x.status === 1);
    if (statusTab === 'ban') filtered = filtered.filter((x) => x.status === 0);
    if (kw) {
      filtered = filtered.filter((x) => {
        const a = `${x.realName || ''}`.toLowerCase();
        const b = `${x.username || ''}`.toLowerCase();
        const c = `${x.phone || ''}`.toLowerCase();
        return a.includes(kw) || b.includes(kw) || c.includes(kw);
      });
    }
    this.setData({ filteredList: filtered });
  },

  openBan(e) {
    const banUser = e.currentTarget.dataset.user;
    this.setData({
      banVisible: true,
      banSaving: false,
      banUser,
      banReason: '',
      banPermanent: true,
      banExpiryDate: todayStr()
    });
  },

  closeBan() {
    if (this.data.banSaving) return;
    this.setData({ banVisible: false });
  },

  openCreateAdmin() {
    this.setData({
      createAdminVisible: true,
      createAdminSaving: false,
      caUsername: '',
      caRealName: '',
      caPhone: '',
      caPassword: '',
      caConfirmPassword: ''
    });
  },

  closeCreateAdmin() {
    if (this.data.createAdminSaving) return;
    this.setData({ createAdminVisible: false });
  },

  onCaUsername(e) {
    this.setData({ caUsername: e.detail.value });
  },
  onCaRealName(e) {
    this.setData({ caRealName: e.detail.value });
  },
  onCaPhone(e) {
    this.setData({ caPhone: e.detail.value });
  },
  onCaPassword(e) {
    this.setData({ caPassword: e.detail.value });
  },
  onCaConfirmPassword(e) {
    this.setData({ caConfirmPassword: e.detail.value });
  },

  submitCreateAdmin() {
    if (this.data.createAdminSaving) return;
    const username = (this.data.caUsername || '').trim();
    const realName = (this.data.caRealName || '').trim();
    const phone = (this.data.caPhone || '').trim();
    const password = this.data.caPassword || '';
    const confirmPassword = this.data.caConfirmPassword || '';
    if (!username) return wx.showToast({ title: '请输入账号', icon: 'none' });
    if (!realName) return wx.showToast({ title: '请输入真实姓名', icon: 'none' });
    if (!password) return wx.showToast({ title: '请输入密码', icon: 'none' });
    if (password.length < 6) return wx.showToast({ title: '密码至少6位', icon: 'none' });
    if (password !== confirmPassword) return wx.showToast({ title: '两次密码不一致', icon: 'none' });

    this.setData({ createAdminSaving: true });
    wx.showLoading({ title: '创建中...' });
    request({
      url: '/admin/user/create-admin',
      method: 'POST',
      data: { username, realName, phone, password }
    })
      .then(() => {
        wx.showToast({ title: '已创建', icon: 'success' });
        this.setData({ createAdminVisible: false });
        this.loadData();
      })
      .finally(() => {
        wx.hideLoading();
        this.setData({ createAdminSaving: false });
      });
  },

  stopProp() {},

  handleBanReason(e) {
    this.setData({ banReason: e.detail.value });
  },

  togglePermanent(e) {
    this.setData({ banPermanent: !!e.detail.value });
  },

  handleExpiryDate(e) {
    this.setData({ banExpiryDate: e.detail.value });
  },

  confirmBan() {
    if (this.data.banSaving) return;
    const reason = (this.data.banReason || '').trim();
    if (!reason) return wx.showToast({ title: '请输入拉黑原因', icon: 'none' });

    const expiryDate = this.data.banPermanent
      ? null
      : `${this.data.banExpiryDate}T23:59:59`;

    this.setData({ banSaving: true });
    wx.showLoading({ title: '拉黑中...' });
    request({
      url: '/admin/blacklist/add',
      method: 'POST',
      data: {
        userId: this.data.banUser.id,
        reason,
        expiryDate
      }
    }).then(() => {
      wx.showToast({ title: '已拉黑', icon: 'success' });
      this.setData({ banVisible: false });
      this.loadData();
    }).finally(() => {
      wx.hideLoading();
      this.setData({ banSaving: false });
    });
  },

  unban(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认移除',
      content: '确定要将该用户移出黑名单吗？',
      success: (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '处理中...' });
        request({
          url: `/admin/blacklist/remove/${id}`,
          method: 'DELETE'
        }).then(() => {
          wx.showToast({ title: '已移除', icon: 'success' });
          this.loadData();
        }).finally(() => {
          wx.hideLoading();
        });
      }
    });
  }
  ,

  navToReservations(e) {
    const user = e.currentTarget.dataset.user;
    if (!user) return;
    const name = encodeURIComponent(user.realName || user.username || '');
    wx.navigateTo({
      url: `/pages/admin/reservations/reservations?userId=${user.id}&name=${name}`
    });
  }

  ,

  // 超级管理员：封禁/启用账号
  toggleUserStatus(e) {
    const user = e.currentTarget.dataset.user;
    if (!user) return;
    const targetStatus = user.status === 1 ? 0 : 1;
    const actionText = targetStatus === 1 ? '启用' : '封禁';
    wx.showModal({
      title: '确认操作',
      content: `确定要${actionText}账号「${user.realName || user.username}」吗？`,
      success: (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '处理中...' });
        request({
          url: '/admin/user/status',
          method: 'POST',
          data: {
            userId: user.id,
            status: targetStatus
          }
        }).then(() => {
          wx.showToast({ title: '已更新', icon: 'success' });
          this.loadData();
        }).finally(() => {
          wx.hideLoading();
        });
      }
    });
  },

  // 超级管理员：重置密码
  resetPassword(e) {
    const user = e.currentTarget.dataset.user;
    if (!user) return;
    wx.showModal({
      title: '重置密码',
      editable: true,
      placeholderText: '请输入新密码（建议6位以上）',
      success: (res) => {
        if (!res.confirm) return;
        const newPassword = (res.content || '').trim();
        if (!newPassword) return wx.showToast({ title: '新密码不能为空', icon: 'none' });
        wx.showLoading({ title: '处理中...' });
        request({
          url: '/admin/user/reset-password',
          method: 'POST',
          data: {
            userId: user.id,
            newPassword
          }
        }).then(() => {
          wx.showToast({ title: '已重置', icon: 'success' });
        }).finally(() => {
          wx.hideLoading();
        });
      }
    });
  }
});