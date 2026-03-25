const { request } = require('../../../utils/request.js');

function statusText(status) {
  if (status === 1) return '正常';
  if (status === 2) return '维护中';
  if (status === 0) return '故障';
  return '未知';
}

Page({
  data: {
    loading: false,
    saving: false,

    keyword: '',
    statusTab: -1, // -1全部 1正常 2维护 0故障

    list: [],
    filteredList: [],

    formVisible: false,
    formMode: 'add', // add / edit
    statusOptions: ['正常', '维护中', '故障'],
    form: {
      id: null,
      name: '',
      location: '',
      statusIndex: 0,
      description: ''
    }
  },

  onShow() {
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  loadData() {
    this.setData({ loading: true });
    return request({
      url: '/admin/device/list',
      method: 'GET'
    }).then((res) => {
      const list = (res || []).map((it) => ({
        ...it,
        statusText: statusText(it.status)
      }));
      this.setData({ list });
      this.applyFilter(list);
    }).catch((err) => {
      console.error('加载设备失败', err);
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

  switchStatusTab(e) {
    const statusTab = parseInt(e.currentTarget.dataset.tab, 10);
    this.setData({ statusTab });
    this.applyFilter();
  },

  applyFilter(listData) {
    const list = listData || this.data.list;
    const kw = (this.data.keyword || '').toLowerCase();
    const { statusTab } = this.data;

    let filtered = list;
    if (statusTab !== -1) filtered = filtered.filter((x) => x.status === statusTab);
    if (kw) {
      filtered = filtered.filter((x) => {
        const a = `${x.name || ''}`.toLowerCase();
        const b = `${x.location || ''}`.toLowerCase();
        return a.includes(kw) || b.includes(kw);
      });
    }
    this.setData({ filteredList: filtered });
  },

  openAdd() {
    this.setData({
      formVisible: true,
      formMode: 'add',
      form: {
        id: null,
        name: '',
        location: '',
        statusIndex: 0,
        description: ''
      }
    });
  },

  openEdit(e) {
    const item = e.currentTarget.dataset.item;
    const statusIndex = item.status === 1 ? 0 : item.status === 2 ? 1 : 2;
    this.setData({
      formVisible: true,
      formMode: 'edit',
      form: {
        id: item.id,
        name: item.name || '',
        location: item.location || '',
        statusIndex,
        description: item.description || ''
      }
    });
  },

  closeForm() {
    if (this.data.saving) return;
    this.setData({ formVisible: false });
  },

  stopProp() {},

  handleFormInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      form: {
        ...this.data.form,
        [field]: e.detail.value
      }
    });
  },

  handleStatusPicker(e) {
    this.setData({
      form: {
        ...this.data.form,
        statusIndex: parseInt(e.detail.value, 10)
      }
    });
  },

  getFormStatusCode() {
    // statusOptions: ['正常','维护中','故障'] -> [1,2,0]
    const idx = this.data.form.statusIndex;
    if (idx === 0) return 1;
    if (idx === 1) return 2;
    return 0;
  },

  saveForm() {
    if (this.data.saving) return;
    const { formMode, form } = this.data;
    const name = (form.name || '').trim();
    const location = (form.location || '').trim();
    if (!name) return wx.showToast({ title: '请输入设备名称', icon: 'none' });
    if (!location) return wx.showToast({ title: '请输入设备位置', icon: 'none' });

    const payload = {
      name,
      location,
      status: this.getFormStatusCode(),
      description: (form.description || '').trim()
    };
    if (formMode === 'edit') payload.id = form.id;

    const url = formMode === 'add' ? '/admin/device/add' : '/admin/device/update';
    this.setData({ saving: true });
    wx.showLoading({ title: '保存中...' });
    request({
      url,
      method: 'POST',
      data: payload
    }).then(() => {
      wx.showToast({ title: '保存成功', icon: 'success' });
      this.setData({ formVisible: false });
      this.loadData();
    }).finally(() => {
      wx.hideLoading();
      this.setData({ saving: false });
    });
  },

  handleDelete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '删除后无法恢复，是否继续？',
      success: (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '删除中...' });
        request({
          url: `/admin/device/delete/${id}`,
          method: 'DELETE'
        }).then(() => {
          wx.showToast({ title: '已删除', icon: 'success' });
          this.loadData();
        }).finally(() => {
          wx.hideLoading();
        });
      }
    });
  }
});