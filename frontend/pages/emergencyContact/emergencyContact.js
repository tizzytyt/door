const { request } = require('../../utils/request.js');

const RELATION_OPTIONS = ['父母', '配偶', '亲友', '其他'];

Page({
  data: {
    loading: false,
    list: [],
    formVisible: false,
    editingId: null,
    submitting: false,
    relationOptions: RELATION_OPTIONS,
    relationIndex: 0,
    form: {
      name: '',
      phone: ''
    }
  },

  onShow() {
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData().finally(() => wx.stopPullDownRefresh());
  },

  noop() {},

  loadData() {
    this.setData({ loading: true });
    return request({
      url: '/student/emergency-contact/list',
      method: 'GET'
    }).then((res) => {
      this.setData({ list: res || [] });
    }).catch(() => {
      this.setData({ list: [] });
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  openAdd() {
    this.setData({
      formVisible: true,
      editingId: null,
      relationIndex: 0,
      form: { name: '', phone: '' }
    });
  },

  openEdit(e) {
    const id = Number(e.currentTarget.dataset.id);
    const item = this.data.list.find((x) => Number(x.id) === id);
    if (!item) return;
    const rel = item.relation || '其他';
    let relationIndex = RELATION_OPTIONS.indexOf(rel);
    if (relationIndex < 0) relationIndex = RELATION_OPTIONS.length - 1;
    this.setData({
      formVisible: true,
      editingId: id,
      relationIndex,
      form: {
        name: item.name || '',
        phone: item.phone || ''
      }
    });
  },

  closeForm() {
    if (this.data.submitting) return;
    this.setData({ formVisible: false, editingId: null });
  },

  onFormInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: e.detail.value });
  },

  onRelationChange(e) {
    this.setData({ relationIndex: Number(e.detail.value) || 0 });
  },

  handleSave() {
    if (this.data.submitting) return;
    const name = (this.data.form.name || '').trim();
    const phone = (this.data.form.phone || '').trim();
    const relation = RELATION_OPTIONS[this.data.relationIndex] || '其他';

    if (!name) return wx.showToast({ title: '请输入姓名', icon: 'none' });
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      return wx.showToast({ title: '请输入正确手机号', icon: 'none' });
    }

    const payload = { name, phone, relation };
    if (this.data.editingId) {
      payload.id = this.data.editingId;
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '保存中...' });
    request({
      url: '/student/emergency-contact/save',
      method: 'POST',
      data: payload
    }).then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' });
      this.setData({ formVisible: false, editingId: null });
      this.loadData();
    }).finally(() => {
      wx.hideLoading();
      this.setData({ submitting: false });
    });
  },

  handleDelete(e) {
    const id = Number(e.currentTarget.dataset.id);
    if (!id) return;
    wx.showModal({
      title: '确认删除',
      content: '确定删除该紧急联系人吗？',
      success: (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '删除中...' });
        request({
          url: `/student/emergency-contact/delete/${id}`,
          method: 'POST'
        }).then(() => {
          wx.showToast({ title: '已删除', icon: 'success' });
          this.loadData();
        }).finally(() => wx.hideLoading());
      }
    });
  },

  handleCall(e) {
    const phone = (e.currentTarget.dataset.phone || '').trim();
    if (!phone) return;
    wx.makePhoneCall({
      phoneNumber: phone,
      fail: () => wx.showToast({ title: '无法拨打', icon: 'none' })
    });
  }
});
