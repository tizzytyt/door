const { request } = require('../../../utils/request.js');

function displayNameForKey(key, desc) {
  const map = {
    max_reservations_per_day: '每日预约次数上限',
    reservation_lead_time_minutes: '提前预约时间（分钟）',
    reservation_time_range: '可预约时间段（如 18:00-23:00）',
    reservation_duration_minutes: '默认预约时长（分钟）'
  };
  if (map[key]) return map[key];
  if (desc && desc.length <= 18) return desc;
  return key;
}

Page({
  data: {
    loading: false,
    savingAll: false,
    keyword: '',

    list: [],
    filteredList: [],
    changedCount: 0
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
      url: '/admin/rules/list',
      method: 'GET'
    }).then((res) => {
      const list = (res || []).map((it) => ({
        ...it,
        displayName: displayNameForKey(it.configKey, it.description),
        originalValue: it.configValue,
        editValue: it.configValue,
        changed: false,
        saving: false
      }));
      this.setData({ list });
      this.applyFilter(list);
      this.recalcChanged(list);
    }).catch((err) => {
      console.error('加载规则失败', err);
      this.setData({ list: [], filteredList: [], changedCount: 0 });
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

  applyFilter(listData) {
    const list = listData || this.data.list;
    const kw = (this.data.keyword || '').toLowerCase();
    if (!kw) {
      this.setData({ filteredList: list });
      return;
    }
    const filteredList = list.filter((x) => {
      const a = `${x.displayName || ''}`.toLowerCase();
      const b = `${x.description || ''}`.toLowerCase();
      const c = `${x.configKey || ''}`.toLowerCase();
      return a.includes(kw) || b.includes(kw) || c.includes(kw);
    });
    this.setData({ filteredList });
  },

  recalcChanged(listData) {
    const list = listData || this.data.list;
    const changedCount = list.filter((x) => x.changed).length;
    this.setData({ changedCount });
  },

  handleValueInput(e) {
    const key = e.currentTarget.dataset.key;
    const value = e.detail.value;
    const list = this.data.list.map((it) => {
      if (it.configKey !== key) return it;
      const changed = `${value}` !== `${it.originalValue}`;
      return { ...it, editValue: value, changed };
    });
    this.setData({ list });
    this.applyFilter(list);
    this.recalcChanged(list);
  },

  resetOne(e) {
    const key = e.currentTarget.dataset.key;
    const list = this.data.list.map((it) => {
      if (it.configKey !== key) return it;
      return { ...it, editValue: it.originalValue, changed: false };
    });
    this.setData({ list });
    this.applyFilter(list);
    this.recalcChanged(list);
  },

  saveOne(e) {
    const key = e.currentTarget.dataset.key;
    const target = this.data.list.find((x) => x.configKey === key);
    if (!target) return;
    if (!target.changed) return wx.showToast({ title: '未修改', icon: 'none' });

    const listSaving = this.data.list.map((it) => (
      it.configKey === key ? { ...it, saving: true } : it
    ));
    this.setData({ list: listSaving });
    this.applyFilter(listSaving);

    return request({
      url: '/admin/rules/update',
      method: 'POST',
      data: {
        configKey: key,
        configValue: target.editValue
      }
    }).then(() => {
      wx.showToast({ title: '已保存', icon: 'success' });
      const list = this.data.list.map((it) => {
        if (it.configKey !== key) return it;
        return {
          ...it,
          originalValue: it.editValue,
          configValue: it.editValue,
          changed: false,
          saving: false
        };
      });
      this.setData({ list });
      this.applyFilter(list);
      this.recalcChanged(list);
    }).catch(() => {
      const list = this.data.list.map((it) => (
        it.configKey === key ? { ...it, saving: false } : it
      ));
      this.setData({ list });
      this.applyFilter(list);
    });
  },

  saveAll() {
    if (this.data.savingAll) return;
    const changed = this.data.list.filter((x) => x.changed);
    if (changed.length === 0) return;

    wx.showModal({
      title: '确认保存',
      content: `将保存 ${changed.length} 条已修改规则，是否继续？`,
      success: (res) => {
        if (!res.confirm) return;
        this.doSaveAll(changed);
      }
    });
  },

  doSaveAll(changed) {
    this.setData({ savingAll: true });
    wx.showLoading({ title: '保存中...' });

    // 顺序保存，简单可靠
    const seq = changed.reduce((p, item) => {
      return p.then(() => request({
        url: '/admin/rules/update',
        method: 'POST',
        data: {
          configKey: item.configKey,
          configValue: item.editValue
        }
      }));
    }, Promise.resolve());

    seq.then(() => {
      wx.showToast({ title: '保存完成', icon: 'success' });
      this.loadData();
    }).finally(() => {
      wx.hideLoading();
      this.setData({ savingAll: false });
    });
  }
});