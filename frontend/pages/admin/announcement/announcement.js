const { request } = require('../../../utils/request.js');

function formatDateTime(dtStr) {
  if (!dtStr) return '';
  const s = String(dtStr).replace('T', ' ');
  return s.slice(0, 16);
}

function makePreview(content) {
  const s = (content || '').replace(/\s+/g, ' ').trim();
  if (!s) return '';
  return s.length > 60 ? s.slice(0, 60) + '...' : s;
}

Page({
  data: {
    list: [],
    submitting: false,
    form: {
      id: null,
      title: '',
      content: ''
    }
  },

  onShow() {
    this.loadList();
  },

  loadList() {
    wx.showLoading({ title: '加载中...' });
    return request({
      url: '/admin/announcement/list',
      method: 'GET'
    })
      .then((res) => {
        const list = (res || []).map((it) => ({
          ...it,
          createdAtText: formatDateTime(it.createdAt),
          preview: makePreview(it.content)
        }));
        this.setData({ list });
      })
      .catch((err) => {
        console.error('加载公告列表失败', err);
        this.setData({ list: [] });
      })
      .finally(() => {
        wx.hideLoading();
      });
  },

  onInputTitle(e) {
    this.setData({ 'form.title': e.detail.value });
  },

  onInputContent(e) {
    this.setData({ 'form.content': e.detail.value });
  },

  resetForm() {
    this.setData({
      form: { id: null, title: '', content: '' }
    });
  },

  submit() {
    if (this.data.submitting) return;
    const title = (this.data.form.title || '').trim();
    const content = (this.data.form.content || '').trim();
    if (!title) return wx.showToast({ title: '请输入标题', icon: 'none' });
    if (!content) return wx.showToast({ title: '请输入内容', icon: 'none' });

    const isEdit = !!this.data.form.id;
    const url = isEdit ? '/admin/announcement/update' : '/admin/announcement/create';
    const payload = isEdit
      ? { id: this.data.form.id, title, content }
      : { title, content };

    this.setData({ submitting: true });
    wx.showLoading({ title: isEdit ? '保存中...' : '发布中...' });

    request({
      url,
      method: 'POST',
      data: payload
    })
      .then(() => {
        wx.showToast({ title: isEdit ? '已保存' : '已发布', icon: 'success' });
        this.resetForm();
        this.loadList();
      })
      .catch((err) => {
        console.error('提交公告失败', err);
      })
      .finally(() => {
        wx.hideLoading();
        this.setData({ submitting: false });
      });
  },

  editOne(e) {
    const id = e.currentTarget.dataset.id;
    const item = (this.data.list || []).find((x) => String(x.id) === String(id));
    if (!item) return;
    this.setData({
      form: {
        id: item.id,
        title: item.title || '',
        content: item.content || ''
      }
    });
    wx.pageScrollTo({ scrollTop: 0, duration: 200 });
  },

  removeOne(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.showModal({
      title: '提示',
      content: '确定要删除该公告吗？',
      success: (res) => {
        if (!res.confirm) return;
        wx.showLoading({ title: '删除中...' });
        request({
          url: `/admin/announcement/delete/${id}`,
          method: 'DELETE'
        })
          .then(() => {
            wx.showToast({ title: '已删除', icon: 'success' });
            if (String(this.data.form.id) === String(id)) {
              this.resetForm();
            }
            this.loadList();
          })
          .finally(() => {
            wx.hideLoading();
          });
      }
    });
  },

  gotoDetail(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({
      url: `/pages/admin/announcementDetail/announcementDetail?id=${id}`
    });
  }
});

